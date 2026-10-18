package com.aurora.visuals.mixin;

import com.aurora.visuals.VisualsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.11 moved sky/fog colours into environment attributes. We tint SKY_COLOR and FOG_COLOR
 * here, scaled by the vanilla value's brightness so night, dusk and rain still darken the sky.
 */
@Mixin(EnvironmentAttributeProbe.class)
public class EnvironmentAttributeProbeMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private <Value> void aurora$tint(EnvironmentAttribute<Value> attribute, float partialTick,
                                     CallbackInfoReturnable<Value> cir) {
        VisualsConfig c = VisualsConfig.get();
        if (!c.skyEnabled) return;
        if (attribute != EnvironmentAttributes.SKY_COLOR && attribute != EnvironmentAttributes.FOG_COLOR) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != Level.OVERWORLD) return;

        Object original = cir.getReturnValue();
        if (!(original instanceof Integer orig)) return;

        int rgb = c.skyRainbow
                ? Mth.hsvToRgb((System.currentTimeMillis() % 8000L) / 8000f, 0.8f, 1f)
                : c.skyColor;

        int or = (orig >> 16) & 255, og = (orig >> 8) & 255, ob = orig & 255;
        float k = Math.max(or, Math.max(og, ob)) / 255f;

        int r = (int) (((rgb >> 16) & 255) * k);
        int g = (int) (((rgb >> 8) & 255) * k);
        int b = (int) ((rgb & 255) * k);
        int result = (orig & 0xFF000000) | (r << 16) | (g << 8) | b;

        cir.setReturnValue((Value) (Object) Integer.valueOf(result));
    }
}
