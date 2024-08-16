package net.mehvahdjukaar.smarterfarmers.mixins;


import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Villager.class)
public interface VillagerAccessor {


    @Mutable
    @Accessor("FOOD_POINTS")
    static void setFoodPoints(Map<Item, Integer> map) {
    }
}

