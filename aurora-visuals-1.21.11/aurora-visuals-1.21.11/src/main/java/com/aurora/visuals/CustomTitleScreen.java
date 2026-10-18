package com.aurora.visuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomTitleScreen extends Screen {
    private final List<float[]> motes = new ArrayList<>();

    public CustomTitleScreen() {
        super(Component.literal("Main Menu"));
    }

    @Override
    protected void init() {
        motes.clear();
        Random rnd = new Random(42);
        for (int i = 0; i < 70; i++) {
            // xFraction, yPixels, speed, phase, brightness
            motes.add(new float[]{rnd.nextFloat(), rnd.nextFloat() * height, 4 + rnd.nextFloat() * 16,
                    rnd.nextFloat() * 6.28f, rnd.nextFloat()});
        }

        int bw = 200, bh = 22, x = width / 2 - bw / 2, y = height / 2 - 5;
        addRenderableWidget(Button.builder(Component.translatable("menu.singleplayer"),
                b -> minecraft.setScreen(new SelectWorldScreen(this))).bounds(x, y, bw, bh).build());
        y += 26;
        addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"),
                b -> minecraft.setScreen(new JoinMultiplayerScreen(this))).bounds(x, y, bw, bh).build());
        y += 26;
        addRenderableWidget(Button.builder(Component.literal("Visuals"),
                b -> minecraft.setScreen(new VisualsScreen(this))).bounds(x, y, 97, bh).build());
        addRenderableWidget(Button.builder(Component.translatable("menu.options"),
                b -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))).bounds(x + 103, y, 97, bh).build());
        y += 26;
        addRenderableWidget(Button.builder(Component.translatable("menu.quit"),
                b -> minecraft.stop()).bounds(x, y, bw, bh).build());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    /** Vanilla background (panorama / blur) is replaced by our own in render(). */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int accent = VisualsConfig.get().menuAccent & 0xFFFFFF;

        // Backdrop
        g.fillGradient(0, 0, width, height, 0xFF06080F, 0xFF16213E);
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        for (float[] m : motes) {
            float y = ((m[1] - t * m[2]) % height + height) % height;
            float x = m[0] * width + (float) Math.sin(t * 0.5f + m[3]) * 6f;
            int a = (int) (40 + 90 * m[4]);
            int s = m[2] > 12 ? 2 : 1;
            g.fill((int) x, (int) y, (int) x + s, (int) y + s, (a << 24) | accent);
        }

        // Widgets (buttons)
        super.render(g, mouseX, mouseY, delta);

        // Logo
        int baseY = height / 2 - 5 - 80;
        String logo = "AURORA";
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(width / 2f, baseY);
        pose.scale(4f, 4f);
        g.drawString(font, logo, -font.width(logo) / 2, 0, 0xFF000000 | accent, true);
        pose.popMatrix();

        String sub = "V  I  S  U  A  L  S";
        g.drawString(font, sub, width / 2 - font.width(sub) / 2, baseY + 40, 0xFF8892A6, true);
        g.drawString(font, "Aurora Visuals  -  Right Shift in-game for settings", 6, height - 14, 0xFF59627A, true);
    }
}
