package com.aurora.visuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class AuroraClient implements ClientModInitializer {
    public static final String MOD_ID = "aurora_visuals";
    public static KeyMapping openMenu;

    @Override
    public void onInitializeClient() {
        VisualsConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + MOD_ID + ".menu", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openMenu.consumeClick()) {
                mc.setScreen(new VisualsScreen(mc.screen));
            }
            ParticleSpawner.tick(mc);
        });

        SkeletonEsp.register();
    }
}
