package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCameraMod;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
            ),
            index = 2
    )
    private Matrix4f orthoFrustumProjMat(Matrix4f projMat) {
        if (OrthoCameraMod.CONFIG.enabled) {
            return OrthoCameraMod.createOrthoMatrix(1.0F, 20.0F);
        }
        return projMat;
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
            ),
            index = 6
    )
    private Matrix4f orthoProjMat(Matrix4f projMat, @Local(argsOnly = true) DeltaTracker tickCounter) {
        if (OrthoCameraMod.CONFIG.enabled) {
            Matrix4f mat = OrthoCameraMod.createOrthoMatrix(tickCounter.getGameTimeDeltaPartialTick(false), 0.0F);
            RenderSystem.setProjectionMatrix(mat, VertexSorting.ORTHOGRAPHIC_Z);
            return mat;
        }
        return projMat;
    }

    @ModifyExpressionValue(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;conjugate(Lorg/joml/Quaternionf;)Lorg/joml/Quaternionf;"
            )
    )
    private Quaternionf modifyRotation(Quaternionf original, @Local(argsOnly = true) DeltaTracker tickCounter) {
        if (OrthoCameraMod.CONFIG.enabled && OrthoCameraMod.CONFIG.fixed) {
            float delta = tickCounter.getGameTimeDeltaPartialTick(false);
            return original.rotationXYZ(
                    OrthoCameraMod.CONFIG.getFixedPitch(delta) * Mth.DEG_TO_RAD,
                    OrthoCameraMod.CONFIG.getFixedYaw(delta) * Mth.DEG_TO_RAD - Mth.PI,
                    0.0F
            );
        }
        return original;
    }
}
