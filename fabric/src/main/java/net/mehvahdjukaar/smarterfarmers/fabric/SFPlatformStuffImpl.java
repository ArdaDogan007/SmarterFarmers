package net.mehvahdjukaar.smarterfarmers.fabric;

import net.fabricmc.fabric.mixin.content.registry.FarmerWorkTaskAccessor;
import net.fabricmc.fabric.mixin.content.registry.HoeItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SFPlatformStuffImpl {

    public static boolean isValidSeed(ItemStack item, Villager villager) {
        return item.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    public static boolean tillBlock(BlockState state, BlockPos belowPos, ServerLevel level) {
        UseOnContext c = new UseOnContext(level, null, InteractionHand.MAIN_HAND,
                Items.IRON_HOE.getDefaultInstance(),
                new BlockHitResult(belowPos.getCenter(), Direction.UP, belowPos, false));
        var a = HoeItemAccessor.getTillingActions().get(state.getBlock());
        if (a != null && a.getFirst().test(c)) {
            a.getSecond().accept(c);
            return level.getBlockState(belowPos) != state;
        }
        return false;
    }

    public static boolean trySpecialPlant(ServerLevel level, BlockPos aboveFarmlandPos, ItemStack itemToPlant, Villager villager) {
        return false;
    }


}
