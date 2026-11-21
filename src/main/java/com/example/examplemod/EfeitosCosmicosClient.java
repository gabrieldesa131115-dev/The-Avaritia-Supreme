package com.example.examplemod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class EfeitosCosmicosClient {
    private static final List RAIOS = new ArrayList();
    private static final List CORTES = new ArrayList();
    private static final Random RAND = new Random();

    private static class RaioDiagonal {
        final List pontos = new ArrayList();
        final List ramos = new ArrayList();
        int vida = 18;
        final int vidaMax = 18;
    }

    private static class CorteLuz {
        Vec3 pos;
        final Vec3 vel;
        final Vec3 dir;
        final Vec3 lado;
        final float inclinacao;
        int vida = 14;
        final int vidaMax = 14;

        CorteLuz(Vec3 pos, Vec3 vel, Vec3 dir, Vec3 lado, float inclinacao) {
            this.pos = pos;
            this.vel = vel;
            this.dir = dir;
            this.lado = lado;
            this.inclinacao = inclinacao;
        }
    }

    public static void spawnRaioDiagonal(Vec3 alvo) {
        RaioDiagonal r = new RaioDiagonal();
        double ang = RAND.nextDouble() * Math.PI * 2.0;
        Vec3 topo = alvo.add(Math.cos(ang) * 22.0, 42.0, Math.sin(ang) * 22.0);
        int segs = 16;
        for (int i = 0; !(i > segs); i++) {
            double t = (double) i / segs;
            Vec3 base = topo.lerp(alvo, t);
            double jitter = (i == 0 || i == segs) ? 0.0 : 1.8;
            Vec3 p = base.add((RAND.nextDouble() - 0.5) * jitter, (RAND.nextDouble() - 0.5) * jitter, (RAND.nextDouble() - 0.5) * jitter);
            r.pontos.add(p);
            if (i == 5 || i == 9 || i == 12) {
                List ramo = new ArrayList();
                ramo.add(p);
                Vec3 dirRamo = new Vec3((RAND.nextDouble() - 0.5) * 4.5, -3.5, (RAND.nextDouble() - 0.5) * 4.5);
                ramo.add(p.add(dirRamo));
                ramo.add(p.add(dirRamo.scale(1.9)).add((RAND.nextDouble() - 0.5) * 2.0, -2.0, (RAND.nextDouble() - 0.5) * 2.0));
                r.ramos.add(ramo);
            }
        }
        RAIOS.add(r);
    }

    public static void spawnCorteDiagonal(Player p, boolean segundoInvertido) {
        Vec3 dir = p.getLookAngle().normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 lado = dir.cross(up);
        if (lado.lengthSqr() == 0) lado = new Vec3(1, 0, 0);
        else lado = lado.normalize();
        Vec3 origem = p.getEyePosition().add(dir.scale(1.8));
        float inc = segundoInvertido ? -0.65F : 0.65F;
        CORTES.add(new CorteLuz(origem, dir.scale(2.4), dir, lado, inc));
    }
      @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator itR = RAIOS.iterator();
        while (itR.hasNext()) {
            RaioDiagonal r = (RaioDiagonal) itR.next();
            r.vida--;
            if (!(r.vida > 0)) itR.remove();
        }
        Iterator itC = CORTES.iterator();
        while (itC.hasNext()) {
            CorteLuz c = (CorteLuz) itC.next();
            c.pos = c.pos.add(c.vel);
            c.vida--;
            if (!(c.vida > 0)) itC.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (RAIOS.isEmpty() && CORTES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f mat = pose.last().pose();

        for (Object obj : RAIOS) {
            RaioDiagonal r = (RaioDiagonal) obj;
            float alpha = (float) r.vida / r.vidaMax;
            desenharFeixe(buf, mat, r.pontos, 0.55F, 160, 40, 255, (int) (110 * alpha));
            desenharFeixe(buf, mat, r.pontos, 0.28F, 0, 220, 255, (int) (190 * alpha));
            desenharFeixe(buf, mat, r.pontos, 0.11F, 255, 255, 255, (int) (255 * alpha));
            for (Object ramoObj : r.ramos) {
                List ramo = (List) ramoObj;
                desenharFeixe(buf, mat, ramo, 0.22F, 140, 80, 255, (int) (140 * alpha));
                desenharFeixe(buf, mat, ramo, 0.07F, 255, 255, 255, (int) (230 * alpha));
            }
        }

        for (Object obj : CORTES) {
            CorteLuz c = (CorteLuz) obj;
            float alpha = (float) c.vida / c.vidaMax;
            desenharArcoCorte(buf, mat, c, 4.6F, 1.1F, 150, 30, 255, (int) (130 * alpha));
            desenharArcoCorte(buf, mat, c, 4.4F, 0.55F, 0, 210, 255, (int) (200 * alpha));
            desenharArcoCorte(buf, mat, c, 4.2F, 0.18F, 255, 255, 255, (int) (255 * alpha));
        }

        tess.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        pose.popPose();
    }

    private static void desenharFeixe(BufferBuilder buf, Matrix4f mat, List pts, float larg, int r, int g, int b, int a) {
        for (int i = 0; !(i >= pts.size() - 1); i++) {
            Vec3 p1 = (Vec3) pts.get(i);
            Vec3 p2 = (Vec3) pts.get(i + 1);
            quad(buf, mat, p1.x - larg, p1.y, p1.z, p1.x + larg, p1.y, p1.z, p2.x + larg, p2.y, p2.z, p2.x - larg, p2.y, p2.z, r, g, b, a);
            quad(buf, mat, p1.x, p1.y, p1.z - larg, p1.x, p1.y, p1.z + larg, p2.x, p2.y, p2.z + larg, p2.x, p2.y, p2.z - larg, r, g, b, a);
        }
    }

    private static void desenharArcoCorte(BufferBuilder buf, Matrix4f mat, CorteLuz c, float raio, float espessura, int r, int g, int b, int a) {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 eixoU = c.lado.scale(Math.cos(c.inclinacao)).add(up.scale(Math.sin(c.inclinacao))).normalize();
        int passos = 14;
        for (int i = 0; !(i >= passos); i++) {
            double a1 = -1.1 + (2.2 * i / passos);
            double a2 = -1.1 + (2.2 * (i + 1) / passos);
            Vec3 d1 = c.dir.scale(Math.cos(a1)).add(eixoU.scale(Math.sin(a1)));
            Vec3 d2 = c.dir.scale(Math.cos(a2)).add(eixoU.scale(Math.sin(a2)));
            Vec3 ext1 = c.pos.add(d1.scale(raio));
            Vec3 ext2 = c.pos.add(d2.scale(raio));
            Vec3 int1 = c.pos.add(d1.scale(raio - espessura));
            Vec3 int2 = c.pos.add(d2.scale(raio - espessura));
            quad(buf, mat, int1.x, int1.y, int1.z, ext1.x, ext1.y, ext1.z, ext2.x, ext2.y, ext2.z, int2.x, int2.y, int2.z, r, g, b, a);
        }
    }

    private static void quad(BufferBuilder buf, Matrix4f mat, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int r, int g, int b, int a) {
        buf.vertex(mat, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) x3, (float) y3, (float) z3).color(r, g, b, a).endVertex();
        buf.vertex(mat, (float) x4, (float) y4, (float) z4).color(r, g, b, a).endVertex();
    }
                             }

