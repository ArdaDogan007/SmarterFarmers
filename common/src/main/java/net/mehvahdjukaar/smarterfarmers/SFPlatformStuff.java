package net.mehvahdjukaar.smarterfarmers;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

public class SFPlatformStuff {

    @Contract
    @ExpectPlatform
    public static boolean isValidSeed(ItemStack item, Villager villager) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean tillBlock(BlockState below, BlockPos belowPos, ServerLevel level) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean trySpecialPlant(ServerLevel level, BlockPos aboveFarmlandPos, ItemStack itemToPlant, Villager villager) {
        throw new AssertionError();
    }
}
