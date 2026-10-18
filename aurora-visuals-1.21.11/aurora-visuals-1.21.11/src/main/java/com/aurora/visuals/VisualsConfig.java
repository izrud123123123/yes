package com.aurora.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VisualsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("aurora_visuals.json");
    private static VisualsConfig instance = new VisualsConfig();

    // Menu
    public boolean customMenu = true;
    public int menuAccent = 0x4DA3FF;

    // Particles
    public boolean particlesEnabled = false;
    public ParticleSpawner.Type particleType = ParticleSpawner.Type.END_ROD;
    public ParticleSpawner.Mode particleMode = ParticleSpawner.Mode.AURA;
    public int particleDensity = 3;
    public float particleRadius = 1.5f;
    public float particleSize = 1.0f;
    public int particleColor = 0xFF55FF;
    public boolean particleRainbow = false;

    // Sky
    public boolean skyEnabled = false;
    public int skyColor = 0x7A3CFF;
    public boolean skyRainbow = false;

    // Time (client-side only, 0-24000)
    public boolean timeEnabled = false;
    public int timeOfDay = 6000;

    // Skeleton ESP
    public boolean espEnabled = false;
    public boolean espThroughWalls = true;
    public boolean espShowSelf = false;
    public boolean espRainbow = false;
    public int espColor = 0xFFFFFF;
    public float espWidth = 2.0f;
    public int espRange = 64;

    public static VisualsConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                VisualsConfig loaded = GSON.fromJson(Files.readString(FILE), VisualsConfig.class);
                if (loaded != null) instance = loaded;
            }
        } catch (Exception e) {
            System.err.println("[Aurora] Failed to load config: " + e);
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[Aurora] Failed to save config: " + e);
        }
    }
}
