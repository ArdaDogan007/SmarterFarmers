package net.mehvahdjukaar.smarterfarmers.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.SpecialPlantable;

public class SFPlatformStuffImpl {

    public static boolean isValidSeed(ItemStack item, Villager villager) {
        if (item.getItem() instanceof SpecialPlantable pl) {
            return pl.villagerCanPlantItem(villager);
        }
        return item.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    public static boolean trySpecialPlant(ServerLevel level, BlockPos aboveFarmlandPos, ItemStack itemToPlant, Villager villager) {
        if (itemToPlant.getItem() instanceof SpecialPlantable sp) {
            if (sp.villagerCanPlantItem(villager) && sp.canPlacePlantAtPosition(itemToPlant, level, aboveFarmlandPos, Direction.DOWN)) {
                sp.spawnPlantAtPosition(itemToPlant, level, aboveFarmlandPos, Direction.DOWN);
                return true;
            }
        }
        return false;
    }

    public static boolean tillBlock(BlockState state, BlockPos belowPos, ServerLevel level) {
        UseOnContext c = new UseOnContext(level, null, InteractionHand.MAIN_HAND,
                Items.IRON_HOE.getDefaultInstance(),
                new BlockHitResult(belowPos.getCenter(), Direction.UP, belowPos, false));
        BlockState newState = state.getToolModifiedState(c, ItemAbilities.HOE_TILL, false);
        if (newState != null && newState != state) {
            level.setBlock(belowPos, newState, 11);
            return true;
        }
        return false;
    }


}
