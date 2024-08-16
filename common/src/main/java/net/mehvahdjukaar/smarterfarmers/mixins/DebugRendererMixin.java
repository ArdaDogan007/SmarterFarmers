package net.mehvahdjukaar.smarterfarmers.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.smarterfarmers.FarmTaskDebugRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow
    @Final
    public PathfindingRenderer pathfindingRenderer;

    @Shadow
    @Final
    public BrainDebugRenderer brainDebugRenderer;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderDebug(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
        pathfindingRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        brainDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        FarmTaskDebugRenderer.INSTANCE.render(poseStack, bufferSource, camX, camY, camZ);
    }
}
