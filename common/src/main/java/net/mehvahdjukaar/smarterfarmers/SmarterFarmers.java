package net.mehvahdjukaar.smarterfarmers;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.smarterfarmers.mixins.VillagerAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
public class SmarterFarmers {

    public static final String MOD_ID = "smarterfarmers";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static final boolean QUARK = PlatHelper.isModLoaded("quark");

    public static final TagKey<Block> SPECIAL_HARVESTABLE = TagKey.create(Registries.BLOCK, res("harvestable_plant"));
    public static final TagKey<Block> HARVESTABLE_ON_DIRT_NO_REPLANT = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "harvestable_on_dirt_no_replant"));
    public static final TagKey<Block> HARVESTABLE_ON_DIRT = TagKey.create(Registries.BLOCK, res("harvestable_on_dirt"));
    public static final TagKey<Block> VALID_FARMLAND = TagKey.create(Registries.BLOCK, res("farmer_plantable_on"));
    public static final TagKey<Block> FARMLAND_DIRT = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "farmland_dirt"));
    public static final TagKey<Item> EAT_BLACKLIST = TagKey.create(Registries.ITEM, res("villagers_cant_eat"));

    public static final Supplier<Boolean> PICKUP_FOOD;
    public static final Supplier<Boolean> EAT_FOOD;
    public static final Supplier<Boolean> DEBUG_RENDERERS;
    public static final Supplier<Integer> TIME_TO_HARVEST;

    public static final ModConfigHolder CONFIG;

    static {
        ConfigBuilder builder = ConfigBuilder.create(MOD_ID, ConfigType.COMMON);

        builder.push("general");
        PICKUP_FOOD = builder.comment("If true, villagers will pick up food items from the regardless of mob griefing gamerule. Needed since with mob griefing on they wont be able to breed.")
                .define("pickup_food_override", true);
        EAT_FOOD = builder.comment("If true, villagers will eat food items they pick up. Eating food will heal them")
                .define("eat_food", true);
        TIME_TO_HARVEST = builder.comment("Time for a farmer to harvest a crop once it reached its destination")
                .define("time_to_harvest", 40, 1, 1000);
        DEBUG_RENDERERS = PlatHelper.isDev() ? () -> true :
                builder.comment("If true, will render debug info for farmers. Only works in single player")
                        .define("debug_renderer", false);

        builder.pop();

        CONFIG = builder.build();
    }

    public static void commonInit() {
        MoonlightEventsHelper.addListener(SmarterFarmers::onVillagerBrainInitialize, IVillagerBrainEvent.class);
        PlatHelper.addCommonSetup(SmarterFarmers::setup);
    }

    public static void setup() {
        //TODO: use quark recipe crawl to convert crop->seed or crop->food
        //make them craft stuff in work at composter
        try {
            Map<Item, Integer> newMap = new HashMap<>(Villager.FOOD_POINTS);

            for (Item i : BuiltInRegistries.ITEM) {
                FoodProperties foodProperties = i.components().get(DataComponents.FOOD);
                if (foodProperties != null && i.components().getOrDefault(DataComponents.RARITY, Rarity.COMMON) != Rarity.COMMON
                        // villagers are vegetarian!
                        && !i.builtInRegistryHolder().is(EAT_BLACKLIST)
                        // ignore container items
                        && !i.hasCraftingRemainingItem()
                        && i.getDefaultMaxStackSize() > 1) {
                    newMap.put(i, (int) Math.max(1, foodProperties.nutrition() * 2 / 3f));
                }
            }
            VillagerAccessor.setFoodPoints(newMap);
        } catch (Exception e) {
            LOGGER.warn("Failed to add custom foods to villagers");
        }
    }


    @EventCalled
    public static void onVillagerBrainInitialize(IVillagerBrainEvent event) {
        //babies do not eat
        // this also mean they will need a reload after they have grown up...
        if (EAT_FOOD.get()) {
            event.addTaskToActivity(Activity.MEET, Pair.of(7, new EatFoodGoal(100, 140)));
        }
    }

    public static void spawnEatingParticles(AbstractVillager villager) {
        Vec3 pos = new Vec3(0, 0, 0.4);
        //pos = pos.xRot(pOwner.getXRot() * ((float) Math.PI / 180F));
        //particle accuracy is shit because yRot isn't synced properly. being a server side mod we can't do better
        pos = pos.yRot((-villager.yBodyRot) * ((float) Math.PI / 180F));
        pos = pos.add(villager.getX(), villager.getEyeY(), villager.getZ());
        ItemStack stack = villager.getMainHandItem();
        Level level = villager.level();
        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
                pos.x + Mth.randomBetween(level.random, -0.05f, 0.05f),
                pos.y - 0.4 + Mth.randomBetween(level.random, -0.05f, 0.05f),
                pos.z + Mth.randomBetween(level.random, -0.05f, 0.05f),
                0.03, 0.05, 0.03);

    }
}
