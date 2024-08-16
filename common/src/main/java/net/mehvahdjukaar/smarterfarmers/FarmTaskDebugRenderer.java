package net.mehvahdjukaar.smarterfarmers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.WeakHashMap;

public class FarmTaskDebugRenderer implements DebugRenderer.SimpleDebugRenderer {

    public static FarmTaskDebugRenderer INSTANCE = new FarmTaskDebugRenderer();

    private final WeakHashMap<Villager, SFHarvestFarmland> villagerFarmTasks = new WeakHashMap<>();


    public void clear() {
        this.villagerFarmTasks.clear();
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
        this.doRender(poseStack, bufferSource);
        if (!this.mc().player.isSpectator()) {
            //this.updateLastLookedAtUuid();

        }

    }

    private Camera getCamera() {
        return Minecraft.getInstance().gameRenderer.getMainCamera();
    }

    private Minecraft mc() {
        return Minecraft.getInstance();
    }

    private void doRender(PoseStack poseStack, MultiBufferSource multiBufferSource) {
        this.villagerFarmTasks.entrySet().forEach((entry) -> {
            if (this.isPlayerCloseEnoughToMob(entry.getKey())) {
                this.renderInfo(poseStack, multiBufferSource, entry.getValue(), entry.getKey());
            }

        });

    }

    private void renderInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, SFHarvestFarmland task, Villager villager) {
        boolean selected = true;
        int i = 0;
        renderTextOverMob(poseStack, multiBufferSource, villager.position(), i++,
                "Status", task.active ? 0x009900 : 0x990000, 0.03F);

        renderTextOverMob(poseStack, multiBufferSource, villager.position(), i++,
                "Plant Timer: " + (task.plantTimer),
                -1, 0.03F);
        for (int j = 0; j < villager.getInventory().getContainerSize(); j++) {
            var ii = villager.getInventory().getItem(j);
            if (!ii.isEmpty()) {
                renderTextOverMob(poseStack, multiBufferSource, villager.position(), i++,
                        ii.toString(), 0x99aa00, 0.03F);
            }
        }

        if (task.aboveFarmlandPos != null)
            highlightPos(poseStack, multiBufferSource, task.aboveFarmlandPos,
                    1.0F, 1.0F, 1.0F, -0.2f);

        if(task.farmlandAround != null)
        for (var v : List.copyOf(task.farmlandAround)) {
            BlockPos p = v.getFirst().below();
            switch (v.getSecond()) {
                case HARVEST:
                    highlightPos(poseStack, multiBufferSource, p,
                            0.4F, 1, 0.2F, 0.03f);
                    break;
                case HARVEST_AND_REPLANT:
                    highlightPos(poseStack, multiBufferSource, p,
                            1, 1, 0.2F, 0.05f);
                    break;
                case PLANT:
                    highlightPos(poseStack, multiBufferSource, p,
                            1, 0.4F, 0.2F, 0.05f);
                    break;
            }
        }
    }


    private static void highlightPos(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos,
                                     float r, float g, float b, float scale) {
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, scale, r, g, b, 0.6f);
    }


    private static void renderTextOverMob(PoseStack poseStack, MultiBufferSource multiBufferSource, Position position, int i, String string, int color, float scale) {
        double x = position.x();
        double y = position.y() + 2.4 + (double) i * 0.25;
        double z = position.z();
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, x, y, z, color, scale, false, 0.5F, true);
    }

    private boolean isPlayerCloseEnoughToMob(Villager villager) {
        Player player = this.mc().player;
        return villager.closerThan(player, 30.0);
    }


    public void trackTask(Villager entity, SFHarvestFarmland sfHarvestFarmland) {
        this.villagerFarmTasks.put(entity, sfHarvestFarmland);
    }
}
