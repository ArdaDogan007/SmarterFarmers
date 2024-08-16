package net.mehvahdjukaar.smarterfarmers.mixins.neoforge;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {


    public VillagerMixin(EntityType<? extends AbstractVillager> arg, Level arg2) {
        super(arg, arg2);
    }

    @Shadow public abstract VillagerData getVillagerData();

    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        //prevents trampling
        if (this.getVillagerData().getProfession() == VillagerProfession.FARMER) return false;
        return super.canTrample(state, pos, fallDistance);
    }

}

