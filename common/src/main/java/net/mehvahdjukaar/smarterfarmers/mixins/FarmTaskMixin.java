package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.CountOrderedSortedMap;
import net.mehvahdjukaar.smarterfarmers.SFPlatformStuff;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = {HarvestFarmland.class}, priority = 500)
public abstract class FarmTaskMixin {


    @Shadow
    private BlockPos aboveFarmlandPos;
    @Shadow
    private long nextOkStartTime;
    @Final
    @Shadow
    private List<BlockPos> validFarmlandAroundVillager;
    @Shadow
    private int timeWorkedSoFar;

    @Shadow
    protected abstract BlockPos getValidFarmland(ServerLevel serverLevel);


    private boolean canHarvest(BlockState state) {
        Block b = state.getBlock();
        if (state.isAir()) return false;
        return ((b instanceof CropBlock crop && crop.isMaxAge(state)) ||
                b instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) == 2 ||
                hardcodedCheckMaxAge(state, b)); //if previous didnt catch it (some mods dont extend crop block)
    }

    private static boolean hardcodedCheckMaxAge(BlockState state, Block b) {
        return SFPlatformStuff.isPlantable(state) && (
                checkAge(state, BlockStateProperties.AGE_1, 1) ||
                        checkAge(state, BlockStateProperties.AGE_2, 2) ||
                        checkAge(state, BlockStateProperties.AGE_3, 3) ||
                        checkAge(state, BlockStateProperties.AGE_4, 4) ||
                        checkAge(state, BlockStateProperties.AGE_5, 5) ||
                        checkAge(state, BlockStateProperties.AGE_7, 7)
        );
    }

    private static boolean checkAge(BlockState state, IntegerProperty property, int max) {
        return state.hasProperty(property) && state.getValue(property) == max;
    }

    private boolean canSpecialBreak(BlockState state) {
        return state.is(SmarterFarmers.SPECIAL_HARVESTABLE) || canBreakNoReplant(state);
    }

    private boolean canBreakNoReplant(BlockState state) {
        return state.is(SmarterFarmers.NO_REPLANT);
    }

    private boolean canPlantOn(BlockState state) {
        return state.getBlock() instanceof FarmBlock ||
                state.is(SmarterFarmers.VALID_FARMLAND);
    }

    @Inject(method = {"start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V"}, at = {@At("HEAD")})
    public void start(ServerLevel pLevel, Villager pEntity, long pGameTime, CallbackInfo ci) {
        if (pGameTime > this.nextOkStartTime && this.aboveFarmlandPos != null) {
            pEntity.setItemSlot(EquipmentSlot.MAINHAND, this.getHoe(pEntity));
        }
    }

    @Inject(method = {"stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V"}, at = {@At("HEAD")})
    public void stop(ServerLevel pLevel, Villager pEntity, long pGameTime, CallbackInfo ci) {
        pEntity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    protected ItemStack getHoe(Villager pEntity) {
        return switch (pEntity.getVillagerData().getLevel()) {
            default -> Items.IRON_HOE.getDefaultInstance();
            case 1 -> Items.WOODEN_HOE.getDefaultInstance();
            case 2 -> Items.STONE_HOE.getDefaultInstance();
            case 4 -> Items.GOLDEN_HOE.getDefaultInstance();
            case 5 -> Items.DIAMOND_HOE.getDefaultInstance();
            case 6 -> Items.NETHERITE_HOE.getDefaultInstance();
        };
    }

    /**
     * @author MehVahdJukaar
     * @reason Smarter Farmers Mod, overhauled farm task logic
     */
    @Overwrite
    protected boolean validPos(BlockPos pPos, ServerLevel pLevel) {
        BlockState cropState = pLevel.getBlockState(pPos);
        BlockState farmState = pLevel.getBlockState(pPos.below());
        return ((cropState.isAir() || canHarvest(cropState)) && canPlantOn(farmState)) ||
                canSpecialBreak(cropState) && (canPlantOn(farmState) || farmState.is(Blocks.DIRT));

    }


    //TODO: this broke in 1.18 for modded stuff. redo from scratch (?)

    /**
     * Basically an overwrite. Not using that cause of fabric api mixins
     *
     * @author MehVahdJukaar
     * @reason Smarter Farmers Mod, overhauled farm task logic
     */
    //@Overwrite
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V",
            at = @At("HEAD"), cancellable = true)
    public void tick(ServerLevel level, Villager villager, long l, CallbackInfo ci) {
        if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerToCenterThan(villager.position(), 1.0D)) {
            if (this.aboveFarmlandPos != null && l > this.nextOkStartTime) {
                BlockState toHarvest = level.getBlockState(this.aboveFarmlandPos);
                Block block = toHarvest.getBlock();
                BlockPos belowPos = this.aboveFarmlandPos.below();

                Item toReplace = Items.AIR;

                if(!toHarvest.isAir()) {

                    //break crop
                    if (canSpecialBreak(toHarvest)) {
                        level.destroyBlock(this.aboveFarmlandPos, true, villager);
                        var below = level.getBlockState(belowPos);
                        if (below.is(Blocks.DIRT)) {
                            level.setBlock(belowPos, Blocks.FARMLAND.defaultBlockState(), 11);
                            level.playSound(null, belowPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        if (canBreakNoReplant(toHarvest)) {
                            this.timeWorkedSoFar++;
                            //dont replant pumpkins
                            ci.cancel();
                            return;
                        }
                    } else if (this.canHarvest(toHarvest)) {
                        toReplace = block.asItem();
                        level.destroyBlock(this.aboveFarmlandPos, true, villager);
                    }
                    //if(CaveVines.hasGlowBerries(toHarvest)){
                    //    CaveVines.use(toHarvest, level, this.aboveFarmlandPos);
                    //}
                }

                BlockState farmlandBlock = level.getBlockState(belowPos);

                //check if block is empty to replant
                if (level.getBlockState(this.aboveFarmlandPos).isAir() && canPlantOn(farmlandBlock)) {
                    SimpleContainer inventory = villager.getInventory();


                    ItemStack itemStack = ItemStack.EMPTY;
                    boolean canPlant = false;
                    int ind = -1;
                    if (toReplace != Items.AIR && toReplace instanceof BlockItem) {
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            if (itemStack.getItem() == toReplace) {
                                canPlant = true;
                                ind = i;
                                break;
                            }
                        }
                    }

                    //normal behavior
                    if (!canPlant) {

                        CountOrderedSortedMap<Block> map = new CountOrderedSortedMap<>();

                        map.add(level.getBlockState(aboveFarmlandPos.north()).getBlock());
                        map.add(level.getBlockState(aboveFarmlandPos.south()).getBlock());
                        map.add(level.getBlockState(aboveFarmlandPos.east()).getBlock());
                        map.add(level.getBlockState(aboveFarmlandPos.west()).getBlock());
                        List<Block> surroundingBlocks = new ArrayList<>();
                        map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e -> surroundingBlocks.add(e.getKey()));


                        Map<Block, Integer> availableSeeds = new HashMap<>();
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            Item it = itemStack.getItem();
                            var cc = SFPlatformStuff.getCropFromSeed(level, aboveFarmlandPos, it);
                            if (cc != null) {
                                availableSeeds.put(cc, i);
                            }
                        }

                        for (Block b : surroundingBlocks) {
                            if (availableSeeds.containsKey(b)) {
                                ind = availableSeeds.get(b);
                                canPlant = true;
                                itemStack = inventory.getItem(ind);
                                break;
                            }
                        }
                        if (!canPlant) {
                            Optional<Integer> opt = availableSeeds.values().stream().findFirst();
                            if (opt.isPresent()) {
                                ind = opt.get();
                                canPlant = true;
                                itemStack = inventory.getItem(ind);
                            }
                        }
                    }


                    if (canPlant) {

                        level.setBlock(aboveFarmlandPos, SFPlatformStuff.getPlant(level, aboveFarmlandPos, itemStack), 3);

                        level.playSound(null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            inventory.setItem(ind, ItemStack.EMPTY);
                        }
                    }

                }

                if (block instanceof CropBlock cropBlock && !cropBlock.isMaxAge(toHarvest)) {
                    this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
                    this.aboveFarmlandPos = this.getValidFarmland(level);
                    if (this.aboveFarmlandPos != null) {
                        this.nextOkStartTime = l + 20L;
                        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
                        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
                    }
                }
            }

            ++this.timeWorkedSoFar;
        }
        ci.cancel();
    }


}

