package net.mehvahdjukaar.smarterfarmers.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/**
 * Author: MehVahdJukaar
 */
@Mod(SmarterFarmers.MOD_ID)
public class SmarterFarmersForge {

    public SmarterFarmersForge(IEventBus bus) {
        RegHelper.startRegisteringFor(bus);
        SmarterFarmers.commonInit();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void mobGriefing(EntityMobGriefingEvent event) {
        if (event.getEntity() instanceof Villager && SmarterFarmers.PICKUP_FOOD.get()) {
            event.setCanGrief(true);
        }
    }
}

