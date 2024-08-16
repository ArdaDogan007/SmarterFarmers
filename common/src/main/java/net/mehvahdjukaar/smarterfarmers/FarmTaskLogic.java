package net.mehvahdjukaar.smarterfarmers;

import net.mehvahdjukaar.moonlight.api.block.IBeeGrowable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FarmTaskLogic {


    //TODO: make them hold seeds
    public static ItemStack getHoe(Villager pEntity) {
        return switch (pEntity.getVillagerData().getLevel()) {
            case 1 -> Items.WOODEN_HOE.getDefaultInstance();
            case 2 -> Items.STONE_HOE.getDefaultInstance();
            case 4 -> Items.GOLDEN_HOE.getDefaultInstance();
            case 5 -> Items.DIAMOND_HOE.getDefaultInstance();
            case 6 -> Items.NETHERITE_HOE.getDefaultInstance();
            default -> Items.IRON_HOE.getDefaultInstance();
        };
    }

    public static boolean isCropMature(BlockState state,  BlockPos pos,Level level) {
        Block b = state.getBlock();
        if (state.isAir()) return false;
        if(b instanceof IBeeGrowable bg){
            return bg.isPlantFullyGrown(state, pos, level);
        }
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

    // plants that can be harvested on any soil
    public static boolean canAlwaysHarvest(BlockState state) {
        return state.is(SmarterFarmers.SPECIAL_HARVESTABLE) || canBreakNoReplant(state);
    }

    public static boolean canBreakNoReplant(BlockState state) {
        return state.is(SmarterFarmers.NO_REPLANT);
    }

    public static boolean isValidFarmland(Block block) {
        return block instanceof FarmBlock || block.builtInRegistryHolder().is(SmarterFarmers.VALID_FARMLAND);
    }

}
