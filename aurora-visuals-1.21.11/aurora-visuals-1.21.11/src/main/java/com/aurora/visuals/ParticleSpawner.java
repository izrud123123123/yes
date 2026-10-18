package com.aurora.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class ParticleSpawner {
    private ParticleSpawner() {}

    public enum Type {
        END_ROD("End Rod"), FLAME("Flame"), SOUL_FIRE("Soul Fire"), SNOWFLAKE("Snowflake"),
        HEART("Hearts"), CRIT("Crit"), ENCHANT("Enchant"), PORTAL("Portal"),
        SPARKLES("Sparkles"), NOTE("Notes"), CLOUD("Cloud"), DUST("Colored Dust");

        private final String label;
        Type(String label) { this.label = label; }
        public String label() { return label; }

        public ParticleOptions create(int rgb, float size) {
            return switch (this) {
                case END_ROD -> ParticleTypes.END_ROD;
                case FLAME -> ParticleTypes.FLAME;
                case SOUL_FIRE -> ParticleTypes.SOUL_FIRE_FLAME;
                case SNOWFLAKE -> ParticleTypes.SNOWFLAKE;
                case HEART -> ParticleTypes.HEART;
                case CRIT -> ParticleTypes.CRIT;
                case ENCHANT -> ParticleTypes.ENCHANT;
                case PORTAL -> ParticleTypes.PORTAL;
                case SPARKLES -> ParticleTypes.HAPPY_VILLAGER;
                case NOTE -> ParticleTypes.NOTE;
                case CLOUD -> ParticleTypes.CLOUD;
                case DUST -> new DustParticleOptions(rgb & 0xFFFFFF, size);
            };
        }
    }

    public enum Mode {
        AURA("Aura"), HALO("Halo"), TRAIL("Feet Trail"), SPIRAL("Spiral");

        private final String label;
        Mode(String label) { this.label = label; }
        public String label() { return label; }
    }

    public static void tick(Minecraft mc) {
        VisualsConfig c = VisualsConfig.get();
        if (!c.particlesEnabled || mc.level == null || mc.player == null || mc.isPaused()) return;

        LocalPlayer p = mc.player;
        RandomSource r = mc.level.random;
        long t = mc.level.getGameTime();
        double R = c.particleRadius;
        double tau = Math.PI * 2;

        for (int i = 0; i < c.particleDensity; i++) {
            double x, y, z, vx = 0, vy = 0, vz = 0;

            switch (c.particleMode) {
                case AURA: {
                    double a = r.nextDouble() * tau;
                    double u = r.nextDouble() * 2 - 1;
                    double s = Math.sqrt(1 - u * u);
                    x = p.getX() + Math.cos(a) * s * R;
                    y = p.getY(0.5) + u * R;
                    z = p.getZ() + Math.sin(a) * s * R;
                    vy = 0.01;
                    break;
                }
                case HALO: {
                    double a = t * 0.25 + i * tau / Math.max(1, c.particleDensity);
                    double rad = Math.max(0.35, R * 0.35);
                    x = p.getX() + Math.cos(a) * rad;
                    y = p.getY() + p.getBbHeight() + 0.25;
                    z = p.getZ() + Math.sin(a) * rad;
                    break;
                }
                case TRAIL: {
                    x = p.getX() + (r.nextDouble() - 0.5) * 0.5;
                    y = p.getY() + 0.05;
                    z = p.getZ() + (r.nextDouble() - 0.5) * 0.5;
                    vy = 0.02;
                    break;
                }
                default: { // SPIRAL
                    double a = t * 0.3 + i * 0.7;
                    double h = ((t * 0.05) + i * 0.15) % 1.0;
                    double rad = Math.max(0.4, R * 0.6);
                    x = p.getX() + Math.cos(a) * rad;
                    y = p.getY() + h * (p.getBbHeight() + 0.4);
                    z = p.getZ() + Math.sin(a) * rad;
                    break;
                }
            }

            int rgb = c.particleRainbow
                    ? Mth.hsvToRgb(((t * 4 + i * 30) % 360) / 360f, 1f, 1f)
                    : c.particleColor;

            mc.level.addParticle(c.particleType.create(rgb, c.particleSize), x, y, z, vx, vy, vz);
        }
    }
}
