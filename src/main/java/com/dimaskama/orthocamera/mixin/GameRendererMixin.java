package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;setupFrustum(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Matrix4f;)V"
            ),
            index = 2
    )
    private Matrix4f orthoFrustumProjMat(Matrix4f projMat) {
        if (OrthoCamera.isEnabled()) {
            return OrthoCamera.createOrthoMatrix(1.0F, 20.0F);
        }
        return projMat;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V"
            ),
            index = 7
    )
    private Matrix4f orthoProjMat(Matrix4f projMat, @Local(argsOnly = true) float tickDelta) {
        if (OrthoCamera.isEnabled()) {
            Matrix4f mat = OrthoCamera.createOrthoMatrix(tickDelta, 0.0F);
            RenderSystem.setProjectionMatrix(mat);
            return mat;
        }
        return projMat;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V",
                    ordinal = 2
            ),
            index = 0
    )
    private Quaternion modifyPitch(Quaternion quaternion, @Local(argsOnly = true) float tickDelta) {
        if (OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed) {
            return Vec3f.POSITIVE_X.getDegreesQuaternion(OrthoCamera.CONFIG.getFixedPitch(tickDelta));
        }
        return quaternion;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V",
                    ordinal = 3
            ),
            index = 0
    )
    private Quaternion modifyYaw(Quaternion quaternion, @Local(argsOnly = true) float tickDelta) {
        if (OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed) {
            return Vec3f.POSITIVE_Y.getDegreesQuaternion(OrthoCamera.CONFIG.getFixedYaw(tickDelta));
        }
        return quaternion;
    }
}
