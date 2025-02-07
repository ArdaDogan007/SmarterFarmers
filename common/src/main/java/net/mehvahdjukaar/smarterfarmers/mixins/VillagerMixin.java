package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.SFPlatformStuff;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

    @Final
    @Shadow
    public static Map<Item, Integer> FOOD_POINTS;

    @Shadow public abstract VillagerData getVillagerData();

    protected VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = {"wantsToPickUp"}, at = {@At("HEAD")}, cancellable = true)
    private void wantsToPickUp(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Item i = stack.getItem();
        if (FOOD_POINTS.containsKey(i)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
        //prevent non farmers from stealing seeds
        else if (SFPlatformStuff.isValidSeed(i)) {
            boolean grab = smarterfarmers$isFarmer() && this.getInventory().canAddItem(stack);
            cir.setReturnValue(grab);
            cir.cancel();
        }
    }

    @Unique
    private boolean smarterfarmers$isFarmer() {
        return this.getVillagerData().getProfession() == VillagerProfession.FARMER;
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource pSource) {
        if (pSource == this.damageSources().sweetBerryBush() && smarterfarmers$isFarmer()) return true;
        return super.isInvulnerableTo(pSource);
    }

    @Inject(method = "handleEntityEvent", at = @At(value = "HEAD"))
    public void smarterFarmers$addEatingParticles(byte pId, CallbackInfo ci) {
        if (pId == EntityEvent.FOX_EAT) { //using this one
            if (this.level().isClientSide) {
                //copied from haunted harvest
                SmarterFarmers.spawnEatingParticles(this);
            }
        }
    }

}

