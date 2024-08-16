package net.mehvahdjukaar.smarterfarmers.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.smarterfarmers.FarmTaskDebugRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderDebug(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
        FarmTaskDebugRenderer.INSTANCE.render(poseStack, bufferSource, camX, camY, camZ);
    }
}
