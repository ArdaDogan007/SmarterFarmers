package net.mehvahdjukaar.smarterfarmers.forge;

import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(SmarterFarmers.MOD_ID)
public class SmarterFarmersForge {

    public SmarterFarmersForge(IEventBus bus) {
        SmarterFarmers.commonInit();
    }

    @SubscribeEvent
    public void mobGriefing(EntityMobGriefingEvent event) {
        if(event.getEntity() instanceof Villager && SmarterFarmers.PICKUP_FOOD.get()){
            event.setResult(EntityMobGriefingEvent.Result.ALLOW);
        }
    }
}

