package com.aurora.visuals;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class VisualsScreen extends Screen {
    private enum Tab {
        PARTICLES("Particles"), SKY("Sky"), TIME("Time"), ESP("Skeleton ESP"), MENU("Menu");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private final Screen parent;
    private Tab tab = Tab.PARTICLES;
    private int slot;

    public VisualsScreen(Screen parent) {
        super(Component.literal("Aurora Visuals"));
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return false; // keep the world running so you can preview changes live
    }

    @Override
    protected void init() {
        VisualsConfig c = VisualsConfig.get();

        Tab[] tabs = Tab.values();
        int tabW = 80;
        int x0 = width / 2 - (tabs.length * (tabW + 4) - 4) / 2;
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            Button b = Button.builder(Component.literal(t.label), btn -> { tab = t; rebuildWidgets(); })
                    .bounds(x0 + i * (tabW + 4), 12, tabW, 20).build();
            b.active = t != tab;
            addRenderableWidget(b);
        }

        slot = 0;
        switch (tab) {
            case PARTICLES -> buildParticles(c);
            case SKY -> buildSky(c);
            case TIME -> buildTime(c);
            case ESP -> buildEsp(c);
            case MENU -> buildMenu(c);
        }

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(width / 2 - 100, height - 28, 200, 20).build());
    }

    private void buildParticles(VisualsConfig c) {
        toggle("Particles", () -> c.particlesEnabled, v -> c.particlesEnabled = v);
        cycle("Type", ParticleSpawner.Type.values(), () -> c.particleType, v -> c.particleType = v, ParticleSpawner.Type::label);
        cycle("Mode", ParticleSpawner.Mode.values(), () -> c.particleMode, v -> c.particleMode = v, ParticleSpawner.Mode::label);
        slider("Density", 1, 20, c.particleDensity, true, v -> c.particleDensity = (int) v);
        slider("Radius", 0.5, 5, c.particleRadius, false, v -> c.particleRadius = (float) v);
        slider("Dust Size", 0.5, 3, c.particleSize, false, v -> c.particleSize = (float) v);
        toggle("Rainbow", () -> c.particleRainbow, v -> c.particleRainbow = v);
        slot++; // keep colour sliders on their own rows
        colorSliders(() -> c.particleColor, v -> c.particleColor = v);
    }

    private void buildSky(VisualsConfig c) {
        toggle("Sky Changer", () -> c.skyEnabled, v -> c.skyEnabled = v);
        toggle("Rainbow Sky", () -> c.skyRainbow, v -> c.skyRainbow = v);
        colorSliders(() -> c.skyColor, v -> c.skyColor = v);
    }

    private void buildTime(VisualsConfig c) {
        toggle("Time Changer", () -> c.timeEnabled, v -> c.timeEnabled = v);
        slider("Time", 0, 24000, c.timeOfDay, true, v -> c.timeOfDay = (int) v);
        preset("Sunrise", 23000, c);
        preset("Day", 1000, c);
        preset("Noon", 6000, c);
        preset("Sunset", 12500, c);
        preset("Night", 14000, c);
        preset("Midnight", 18000, c);
    }

    private void buildEsp(VisualsConfig c) {
        toggle("Skeleton ESP", () -> c.espEnabled, v -> c.espEnabled = v);
        toggle("Through Walls", () -> c.espThroughWalls, v -> c.espThroughWalls = v);
        toggle("Show Self (F5)", () -> c.espShowSelf, v -> c.espShowSelf = v);
        toggle("Rainbow", () -> c.espRainbow, v -> c.espRainbow = v);
        slider("Line Width", 1, 6, c.espWidth, false, v -> c.espWidth = (float) v);
        slider("Range", 16, 256, c.espRange, true, v -> c.espRange = (int) v);
        colorSliders(() -> c.espColor, v -> c.espColor = v);
    }

    private void buildMenu(VisualsConfig c) {
        toggle("Custom Title Screen", () -> c.customMenu, v -> c.customMenu = v);
        slot++;
        colorSliders(() -> c.menuAccent, v -> c.menuAccent = v);
    }

    // ---------- layout helpers ----------
    private int cx() { return width / 2 - 156 + (slot % 2) * 162; }
    private int cy() { return 44 + (slot / 2) * 24; }

    private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        int x = cx(), y = cy();
        slot++;
        addRenderableWidget(Button.builder(onOff(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(onOff(name, get.getAsBoolean()));
        }).bounds(x, y, 150, 20).build());
    }

    private static Component onOff(String name, boolean v) {
        return Component.literal(name + ": " + (v ? "ON" : "OFF"));
    }

    private <E extends Enum<E>> void cycle(String name, E[] values, Supplier<E> get, Consumer<E> set, Function<E, String> label) {
        int x = cx(), y = cy();
        slot++;
        addRenderableWidget(Button.builder(Component.literal(name + ": " + label.apply(get.get())), b -> {
            E next = values[(get.get().ordinal() + 1) % values.length];
            set.accept(next);
            b.setMessage(Component.literal(name + ": " + label.apply(next)));
        }).bounds(x, y, 150, 20).build());
    }

    private void slider(String name, double min, double max, double cur, boolean integer, DoubleConsumer set) {
        int x = cx(), y = cy();
        slot++;
        addRenderableWidget(new ValueSlider(x, y, 150, name, min, max, cur, integer, set));
    }

    private void colorSliders(IntSupplier get, IntConsumer set) {
        String[] names = {"Red", "Green", "Blue"};
        int[] shifts = {16, 8, 0};
        for (int i = 0; i < 3; i++) {
            final int sh = shifts[i];
            int x = cx(), y = cy();
            slot++;
            addRenderableWidget(new ValueSlider(x, y, 150, names[i], 0, 255, (get.getAsInt() >> sh) & 255, true, v -> {
                int cur = get.getAsInt();
                set.accept((cur & ~(255 << sh)) | (((int) v & 255) << sh));
            }));
        }
    }

    private void preset(String name, int ticks, VisualsConfig c) {
        int x = cx(), y = cy();
        slot++;
        addRenderableWidget(Button.builder(Component.literal(name), b -> {
            c.timeEnabled = true;
            c.timeOfDay = ticks;
            rebuildWidgets();
        }).bounds(x, y, 150, 20).build());
    }

    @Override
    public void removed() {
        VisualsConfig.save();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private static class ValueSlider extends AbstractSliderButton {
        private final String name;
        private final double min, max;
        private final boolean integer;
        private final DoubleConsumer set;

        ValueSlider(int x, int y, int w, String name, double min, double max, double cur, boolean integer, DoubleConsumer set) {
            super(x, y, w, 20, Component.empty(), (cur - min) / (max - min));
            this.name = name;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.set = set;
            updateMessage();
        }

        private double real() {
            return min + (max - min) * value;
        }

        @Override
        protected void updateMessage() {
            double v = real();
            setMessage(Component.literal(name + ": " + (integer ? String.valueOf(Math.round(v)) : String.format("%.1f", v))));
        }

        @Override
        protected void applyValue() {
            set.accept(integer ? Math.round(real()) : real());
        }
    }
}
