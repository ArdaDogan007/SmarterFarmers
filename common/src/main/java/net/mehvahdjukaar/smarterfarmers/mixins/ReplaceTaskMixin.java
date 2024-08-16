package net.mehvahdjukaar.smarterfarmers.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.smarterfarmers.SFHarvestFarmland;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerGoalPackages.class)
public class ReplaceTaskMixin {

    @ModifyExpressionValue(method = "getWorkPackage", at= @At(value = "NEW", target = "()Lnet/minecraft/world/entity/ai/behavior/HarvestFarmland;"))
    private static HarvestFarmland aa(HarvestFarmland original){

        return new SFHarvestFarmland();
    }
}
