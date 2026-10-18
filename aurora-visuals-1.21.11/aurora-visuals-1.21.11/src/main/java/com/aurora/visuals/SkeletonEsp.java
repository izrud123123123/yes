package com.aurora.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws a stick-figure skeleton for each player using vanilla's Gizmos system.
 * "Through walls" maps to GizmoProperties#setAlwaysOnTop.
 */
public final class SkeletonEsp {
    private SkeletonEsp() {}

    public static void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> render());
    }

    private static void render() {
        VisualsConfig c = VisualsConfig.get();
        Minecraft mc = Minecraft.getInstance();
        if (!c.espEnabled || mc.level == null || mc.player == null) return;

        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        int rgb = c.espRainbow
                ? Mth.hsvToRgb((System.currentTimeMillis() % 4000L) / 4000f, 1f, 1f)
                : c.espColor;
        int col = 0xFF000000 | (rgb & 0xFFFFFF);
        double rangeSq = (double) c.espRange * c.espRange;

        for (Player p : mc.level.players()) {
            if (p == mc.player && !(c.espShowSelf && !mc.options.getCameraType().isFirstPerson())) continue;
            if (p.isSpectator() || !p.isAlive()) continue;
            if (p.distanceToSqr(mc.player) > rangeSq) continue;
            drawPlayer(p, pt, col, c);
        }
    }

    private static void drawPlayer(Player p, float pt, int col, VisualsConfig c) {
        Vec3 base = p.getPosition(pt);

        // Swimming / crawling / elytra: just one line through the hitbox
        if (p.getPose() != Pose.STANDING && p.getPose() != Pose.CROUCHING) {
            AABB bb = p.getBoundingBox();
            Vec3 center = bb.getCenter();
            line(new Vec3(center.x, bb.minY, center.z), new Vec3(center.x, bb.maxY, center.z), col, c);
            return;
        }

        double yaw = Math.toRadians(Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot));
        Vec3 fwd = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 left = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));

        boolean sneak = p.isCrouching();
        double s = sneak ? 0.85 : 1.0;
        double lean = sneak ? 0.18 : 0.0;

        float limbPos = p.walkAnimation.position(pt);
        float limbSpeed = Math.min(1f, p.walkAnimation.speed(pt));
        double swing = Math.cos(limbPos * 0.6662f) * 1.4 * limbSpeed;

        Vec3 headC = pt(base, left, fwd, 0, 1.58 * s, lean);
        Vec3 neck = pt(base, left, fwd, 0, 1.38 * s, lean);
        Vec3 pelvis = pt(base, left, fwd, 0, 0.72 * s, 0);
        Vec3 lSh = pt(base, left, fwd, 0.3, 1.35 * s, lean);
        Vec3 rSh = pt(base, left, fwd, -0.3, 1.35 * s, lean);
        Vec3 lHip = pt(base, left, fwd, 0.12, 0.72 * s, 0);
        Vec3 rHip = pt(base, left, fwd, -0.12, 0.72 * s, 0);

        // Spine, shoulders, hips, neck
        line(neck, pelvis, col, c);
        line(lSh, rSh, col, c);
        line(lHip, rHip, col, c);
        line(neck, headC, col, c);

        // Head ring + look direction
        double r = 0.22;
        Vec3 prev = null;
        for (int i = 0; i <= 12; i++) {
            double t = i * Math.PI * 2 / 12;
            Vec3 cur = headC.add(Math.cos(t) * r, 0, Math.sin(t) * r);
            if (prev != null) line(prev, cur, col, c);
            prev = cur;
        }
        float headYaw = Mth.rotLerp(pt, p.yHeadRotO, p.yHeadRot);
        float pitch = Mth.lerp(pt, p.xRotO, p.getXRot());
        line(headC, headC.add(Vec3.directionFromRotation(pitch, headYaw).scale(0.5)), col, c);

        // Arms (swing opposite to same-side leg) and legs
        limb(lSh, fwd, 0.62 * s, -swing * 0.8, -swing * 0.8 + 0.25, col, c);
        limb(rSh, fwd, 0.62 * s, swing * 0.8, swing * 0.8 + 0.25, col, c);
        limb(lHip, fwd, 0.72 * s, swing, swing - Math.abs(swing) * 0.4, col, c);
        limb(rHip, fwd, 0.72 * s, -swing, -swing - Math.abs(swing) * 0.4, col, c);
    }

    private static Vec3 pt(Vec3 base, Vec3 left, Vec3 fwd, double l, double y, double f) {
        return base.add(left.x * l + fwd.x * f, y, left.z * l + fwd.z * f);
    }

    private static void limb(Vec3 origin, Vec3 fwd, double len, double a1, double a2, int col, VisualsConfig c) {
        Vec3 mid = origin.add(fwd.scale(Math.sin(a1) * len / 2)).add(0, -Math.cos(a1) * len / 2, 0);
        Vec3 end = mid.add(fwd.scale(Math.sin(a2) * len / 2)).add(0, -Math.cos(a2) * len / 2, 0);
        line(origin, mid, col, c);
        line(mid, end, col, c);
    }

    private static void line(Vec3 a, Vec3 b, int col, VisualsConfig c) {
        GizmoProperties props = Gizmos.line(a, b, col, c.espWidth);
        if (c.espThroughWalls) props.setAlwaysOnTop();
    }
}
