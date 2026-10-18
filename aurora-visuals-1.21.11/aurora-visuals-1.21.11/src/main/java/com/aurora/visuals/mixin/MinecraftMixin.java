package com.aurora.visuals.mixin;

import com.aurora.visuals.CustomTitleScreen;
import com.aurora.visuals.VisualsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    /** Swap the vanilla title screen for ours. */
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen aurora$replaceTitle(Screen screen) {
        if (!VisualsConfig.get().customMenu) return screen;

        if (screen != null && screen.getClass() == TitleScreen.class) {
            return new CustomTitleScreen();
        }
        // vanilla falls back to a TitleScreen when screen == null and no level is loaded
        if (screen == null && ((Minecraft) (Object) this).level == null) {
            return new CustomTitleScreen();
        }
        return screen;
    }
}
