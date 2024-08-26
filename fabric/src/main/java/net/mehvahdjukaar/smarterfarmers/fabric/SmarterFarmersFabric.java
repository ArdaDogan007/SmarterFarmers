package net.mehvahdjukaar.smarterfarmers.fabric;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

public class SmarterFarmersFabric implements ModInitializer {

    private static boolean hasInitialized = false;

    @Override
    public void onInitialize() {
        SmarterFarmers.commonInit();
        CommonLifecycleEvents.TAGS_LOADED.register((resourceManager, tagManager) -> {
            if (!hasInitialized) {
                hasInitialized = true;

                ImmutableList.Builder<Item> builder = ImmutableList.builder();
                builder.addAll(WorkAtComposter.COMPOSTABLE_ITEMS);
                for (ItemLike entry : ComposterBlock.COMPOSTABLES.keySet()) {
                    if (!Villager.FOOD_POINTS.containsKey(entry.asItem())) {
                        builder.add(entry.asItem());
                    }
                }
                WorkAtComposter.COMPOSTABLE_ITEMS = builder.build();
            }
        });
    }
}
