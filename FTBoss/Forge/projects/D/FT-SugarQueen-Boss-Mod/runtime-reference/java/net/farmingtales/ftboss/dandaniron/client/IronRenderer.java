/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.dandaniron.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.OptimizedSpellVisual;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.client.render.dandan.DandanMotion;
import net.farmingtales.ftboss.client.render.reapflat.FlatReapMotion;
import net.farmingtales.ftboss.dandaniron.IronClock;
import net.farmingtales.ftboss.dandaniron.IronGeometry;
import net.farmingtales.ftboss.dandaniron.IronLifecycle;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.farmingtales.ftboss.dandanironmotion.client.IronActor;
import net.farmingtales.ftboss.dandanrun.client.RunActor;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class IronRenderer {
    private static final Map<String, Model> MODELS = new HashMap<String, Model>();
    private static final List<IronPacket> ACTIVE = new ArrayList<IronPacket>();
    private static final RenderType AREA = RenderType.m_110473_((ResourceLocation)new ResourceLocation("ftboss", "textures/fx/dandan/materials.png"));
    private static final RenderType REAP = RenderType.m_110473_((ResourceLocation)new ResourceLocation("ftboss", "reap_flat/materials.png"));
    private static ClientLevel world;
    private static final double[][] UV;

    private IronRenderer() {
    }

    public static void receive(IronPacket ironPacket) {
        RunActor.notice(ironPacket);
        IronActor.receive(ironPacket);
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || !ironPacket.valid() || !ironPacket.visual().dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return;
        }
        if (clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
        }
        if (ironPacket.cancelled()) {
            ACTIVE.removeIf(ironPacket2 -> IronRenderer.same(ironPacket2, ironPacket) && IronLifecycle.cancels(ironPacket2.visual().phase().name(), ironPacket2.cast(), ironPacket.cast()));
            return;
        }
        for (IronPacket ironPacket3 : ACTIVE) {
            if (!IronRenderer.same(ironPacket3, ironPacket) || !IronLifecycle.stale(ironPacket3.visual().phase().name(), ironPacket3.cast(), ironPacket3.event(), ironPacket.visual().phase().name(), ironPacket.cast(), ironPacket.event())) continue;
            return;
        }
        if (ironPacket.visual().phase() != SpellVisualRules.Phase.BEAM) {
            ACTIVE.removeIf(ironPacket2 -> IronRenderer.same(ironPacket2, ironPacket) && IronLifecycle.replaces(ironPacket2.visual().phase().name(), ironPacket2.cast(), ironPacket.visual().phase().name(), ironPacket.cast()));
        }
        if (ACTIVE.size() >= 64) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(ironPacket);
    }

    private static boolean same(IronPacket ironPacket, IronPacket ironPacket2) {
        return ironPacket.visual().owner().equals(ironPacket2.visual().owner()) && ironPacket.visual().spell() == ironPacket2.visual().spell();
    }

    public static List<?> exclude(List<?> list, RenderLevelStageEvent renderLevelStageEvent, ClientLevel clientLevel) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || clientLevel == null) {
            return list;
        }
        ArrayList arrayList = new ArrayList();
        for (Object obj : list) {
            if (obj instanceof OptimizedSpellVisual) {
                Entity entity;
                OptimizedSpellVisual optimizedSpellVisual = (OptimizedSpellVisual)obj;
                if (IronPacket.matches(optimizedSpellVisual.packet().spell().id) && (entity = clientLevel.m_6815_(optimizedSpellVisual.packet().ownerId())) instanceof EasterBossEntity) {
                    EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
                    if (entity.m_20148_().equals(optimizedSpellVisual.packet().owner()) && easterBossEntity.getVariant() == 1 && !easterBossEntity.isMini() && !easterBossEntity.isGiant()) continue;
                }
            }
            arrayList.add(obj);
        }
        return arrayList;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || clientLevel != world) {
            ACTIVE.clear();
            world = clientLevel;
            return;
        }
        ACTIVE.removeIf(ironPacket -> IronRenderer.expired(ironPacket, clientLevel.m_46467_()));
    }

    private static boolean expired(IronPacket ironPacket, double d) {
        Entity entity = world.m_6815_(ironPacket.visual().ownerId());
        if (entity == null || !entity.m_20148_().equals(ironPacket.visual().owner()) || !entity.m_6084_()) {
            return true;
        }
        return IronClock.opacity(ironPacket.visual().phase() == SpellVisualRules.Phase.CHARGE, d, ironPacket.start(), ironPacket.ticks(), ironPacket.event()) <= 0.0;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || world == null || Minecraft.m_91087_().f_91073_ != world || ACTIVE.isEmpty() || MODELS.size() != 3) {
            return;
        }
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, true);
        try {
            IronRenderer.draw(renderLevelStageEvent);
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    private static void draw(RenderLevelStageEvent renderLevelStageEvent) {
        Record record;
        Object object;
        SpellVisualPacket spellVisualPacket;
        IronPacket ironPacket22;
        Object object22;
        int n;
        Minecraft minecraft = Minecraft.m_91087_();
        double d = (float)world.m_46467_() + (minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick());
        ACTIVE.removeIf(ironPacket -> IronRenderer.expired(ironPacket, d));
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 4000 : (n = particleStatus == ParticleStatus.DECREASED ? 8000 : 12000);
        int n3 = particleStatus == ParticleStatus.MINIMAL ? 1 : (particleStatus == ParticleStatus.DECREASED ? 2 : 3);
        int n4 = 0;
        int n5 = 0;
        ArrayList<IronPacket> arrayList = new ArrayList<IronPacket>(ACTIVE);
        arrayList.sort(Comparator.comparingDouble(ironPacket -> ironPacket.visual().start().m_82557_(vec3)));
        ArrayList<AABB> arrayList2 = new ArrayList<AABB>();
        for (Object object22 : world.m_6907_()) {
            if (!(object22.m_20182_().m_82557_(vec3) < 9216.0)) continue;
            arrayList2.add(object22.m_20191_().m_82400_(0.04));
        }
        ArrayList arrayList3 = new ArrayList();
        block1: for (IronPacket ironPacket22 : arrayList) {
            List<Panel> list;
            spellVisualPacket = ironPacket22.visual();
            if (spellVisualPacket.start().m_82557_(vec3) > 9216.0 && spellVisualPacket.end().m_82557_(vec3) > 9216.0) continue;
            object = world.m_6815_(spellVisualPacket.ownerId());
            record = MODELS.get(spellVisualPacket.spell().id);
            double d2 = IronClock.opacity(spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE, d, ironPacket22.start(), ironPacket22.ticks(), ironPacket22.event());
            double d3 = Math.max(((Model)record).span() * (double)ironPacket22.scale(), spellVisualPacket.start().m_82554_(spellVisualPacket.end())) + 2.0;
            if (!renderLevelStageEvent.getFrustum().m_113029_(new AABB(spellVisualPacket.start(), spellVisualPacket.end()).m_82400_(d3))) continue;
            if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
                if (n5++ >= 16) continue;
                list = IronRenderer.beam(ironPacket22, d2, vec3);
            } else {
                if (n4 >= n3 || arrayList3.size() + ((Model)record).faces().size() > n) continue;
                list = IronRenderer.body(ironPacket22, (Model)record, (Entity)object, d, d2, particleStatus != ParticleStatus.MINIMAL, vec3);
                ++n4;
            }
            AABB aABB = object.m_20191_().m_82400_(0.04);
            for (Panel panel : list) {
                if (arrayList3.size() >= n) continue block1;
                AABB aABB2 = IronRenderer.bounds(panel.points());
                double d4 = panel.alpha();
                boolean bl2 = aABB2.m_82381_(aABB);
                if (!bl2) {
                    for (AABB aABB3 : arrayList2) {
                        if (!aABB2.m_82381_(aABB3)) continue;
                        bl2 = true;
                        break;
                    }
                }
                if (!((d4 *= IronClock.personOpacity(bl, bl2)) >= 0.012)) continue;
                arrayList3.add(new Panel(panel.points(), panel.uv(), panel.material(), (float)d4, panel.pipeline(), panel.distance()));
            }
        }
        arrayList3.sort(Comparator.comparingDouble(Panel::distance).reversed());
        object22 = minecraft.m_91269_().m_110104_();
        ironPacket22 = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        spellVisualPacket = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        object = arrayList3.iterator();
        while (object.hasNext()) {
            record = (Panel)object.next();
            IronRenderer.emit(object22.m_6299_(((Panel)record).pipeline()), (Panel)record, (Matrix4f)ironPacket22, (Matrix3f)spellVisualPacket, vec3);
        }
        object22.m_109912_(REAP);
        object22.m_109912_(AREA);
    }

    private static List<Panel> body(IronPacket ironPacket, Model model, Entity entity, double d, double d2, boolean bl, Vec3 vec3) {
        double d3;
        Face face2;
        boolean bl2 = ironPacket.visual().phase() == SpellVisualRules.Phase.CHARGE;
        boolean bl3 = model.id().equals("candle_reap");
        double d4 = IronClock.clock(bl2, d, ironPacket.start(), ironPacket.ticks());
        double d5 = bl2 ? 12.0 * IronClock.progress(d, ironPacket.start(), ironPacket.ticks()) : d - (double)ironPacket.start();
        double d6 = bl2 ? 0.72 + 0.28 * IronClock.smooth(IronClock.progress(d, ironPacket.start(), ironPacket.ticks())) : 1.0;
        double d7 = model.span() / model.nativeSpan() * d6;
        double[] dArray = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] dArray2 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        ArrayList<double[][]> arrayList = new ArrayList<double[][]>();
        double[] dArray3 = FlatReapMotion.rotation(d4, 30);
        DandanMotion.WeaponPose weaponPose = DandanMotion.weaponPose(d4);
        for (Face face2 : model.faces()) {
            double[][] dArray4 = new double[4][3];
            for (int i = 0; i < 4; ++i) {
                double[] dArray5 = IronGeometry.point(face2.points()[i], face2.pivot(), face2.bone(), bl3, face2.emitted() >= 0.0, dArray3, weaponPose, d5, d7, ironPacket.scale());
                dArray4[i] = dArray5;
                if (!(face2.emitted() < 0.0)) continue;
                for (int j = 0; j < 3; ++j) {
                    dArray[j] = Math.min(dArray[j], dArray5[j]);
                    dArray2[j] = Math.max(dArray2[j], dArray5[j]);
                }
            }
            arrayList.add(dArray4);
        }
        Vec3 vec32 = bl2 && bl3 ? entity.m_20318_(Minecraft.m_91087_().m_91104_() ? 0.0f : (float)(d % 1.0)) : ironPacket.visual().start();
        face2 = ironPacket.visual().end().m_82546_(ironPacket.visual().start());
        double d8 = Math.hypot(((Vec3)face2).f_82479_, ((Vec3)face2).f_82481_);
        double d9 = d8 < 1.0E-8 ? 0.0 : ((Vec3)face2).f_82479_ / d8;
        double d10 = d8 < 1.0E-8 ? 1.0 : ((Vec3)face2).f_82481_ / d8;
        double d11 = FlatReapMotion.placementLift(dArray[1], dArray2[1], model.height());
        double d12 = d3 = model.anchor().equals("FRONT") ? 0.55 + (double)(entity.m_20205_() / 2.0f) - dArray[2] : 0.0;
        if (model.anchor().equals("ABOVE")) {
            d11 = Math.max(d11, (double)entity.m_20206_() + 0.65 - dArray[1]);
        }
        double[] dArray6 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
        ArrayList<Panel> arrayList2 = new ArrayList<Panel>();
        for (int i = 0; i < model.faces().size(); ++i) {
            double[][] dArray7;
            Face face3 = model.faces().get(i);
            if (face3.detail() && !bl) continue;
            double d13 = d2 * (double)face3.opacity();
            if (face3.emitted() >= 0.0) {
                d13 *= bl2 ? 0.0 : FlatReapMotion.trailAlpha(d4, face3.emitted());
            }
            if (bl2 && face3.detail()) {
                d13 *= 0.28 + 0.72 * IronClock.smooth(IronClock.progress(d, ironPacket.start(), ironPacket.ticks()));
            }
            if (!bl2 && ironPacket.visual().phase() == SpellVisualRules.Phase.PULSE && face3.bone() >= 10) {
                d13 *= 0.84 + 0.16 * Math.cos(d5 * 0.7 - (double)(face3.bone() - 10) * 1.256);
            }
            if (d13 < 0.012) continue;
            for (double[] dArray8 : dArray7 = (double[][])arrayList.get(i)) {
                IronGeometry.worldInto(dArray8, dArray6, face3.emitted() >= 0.0 ? 0.0 : d11, face3.emitted() >= 0.0 ? (double)(entity.m_20205_() / 2.0f) - 0.3 : d3, d9, d10);
            }
            arrayList2.add(new Panel(dArray7, face3.uv(), face3.material(), (float)d13, model.pipeline(), IronRenderer.bounds(dArray7).m_82399_().m_82557_(vec3)));
        }
        return arrayList2;
    }

    private static List<Panel> beam(IronPacket ironPacket, double d, Vec3 vec3) {
        Vec3 vec32 = ironPacket.visual().start();
        Vec3 vec33 = ironPacket.visual().end().m_82546_(vec32);
        double d2 = vec33.m_82553_();
        if (d2 < 0.01) {
            return List.of();
        }
        Vec3 vec34 = vec33.m_82541_();
        Vec3 vec35 = vec34.m_82537_(Math.abs(vec34.f_82480_) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).m_82541_().m_82490_(0.018);
        Vec3 vec36 = vec34.m_82537_(vec35).m_82541_().m_82490_(0.008);
        ArrayList<Panel> arrayList = new ArrayList<Panel>();
        boolean bl = ironPacket.visual().spell().id.equals("candle_reap");
        for (int i = 0; i < 8; ++i) {
            Object object;
            Object object2;
            int n;
            double[][][] dArray = new double[2][4][3];
            for (n = 0; n < 2; ++n) {
                object2 = vec32.m_82549_(vec33.m_82490_((double)(i + n) / 8.0));
                for (int j = 0; j < 4; ++j) {
                    Vec3 vec37 = object2.m_82549_(vec35.m_82490_(j < 2 ? -1.0 : 1.0)).m_82549_(vec36.m_82490_(j == 0 || j == 3 ? -1.0 : 1.0));
                    dArray[n][j] = new double[]{vec37.f_82479_, vec37.f_82480_, vec37.f_82481_};
                }
            }
            for (n = 0; n < 4; ++n) {
                object2 = new double[][]{dArray[0][n], dArray[1][n], dArray[1][(n + 1) % 4], dArray[0][(n + 1) % 4]};
                arrayList.add(new Panel((double[][])object2, UV, bl ? 10 : (i % 2 == 0 ? 10 : 11), (float)(d * 0.55), bl ? REAP : AREA, IronRenderer.bounds((double[][])object2).m_82399_().m_82557_(vec3)));
            }
            if (i != 0 && i != 7) continue;
            if (i == 0) {
                object = dArray[0];
            } else {
                double[][] dArrayArray = new double[4][];
                dArrayArray[0] = dArray[1][3];
                dArrayArray[1] = dArray[1][2];
                dArrayArray[2] = dArray[1][1];
                object = dArrayArray;
                dArrayArray[3] = dArray[1][0];
            }
            double[][] dArray2 = object;
            arrayList.add(new Panel(dArray2, UV, 10, (float)(d * 0.55), bl ? REAP : AREA, IronRenderer.bounds(dArray2).m_82399_().m_82557_(vec3)));
        }
        return arrayList;
    }

    private static AABB bounds(double[][] dArray) {
        double[] dArray2 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] dArray3 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double[] dArray4 : dArray) {
            for (int i = 0; i < 3; ++i) {
                dArray2[i] = Math.min(dArray2[i], dArray4[i]);
                dArray3[i] = Math.max(dArray3[i], dArray4[i]);
            }
        }
        return new AABB(dArray2[0], dArray2[1], dArray2[2], dArray3[0], dArray3[1], dArray3[2]);
    }

    private static double[] vector(AABB aABB) {
        return new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_};
    }

    private static void emit(VertexConsumer vertexConsumer, Panel panel, Matrix4f matrix4f, Matrix3f matrix3f, Vec3 vec3) {
        double[][] dArray = panel.points();
        Vec3 vec32 = new Vec3(dArray[1][0] - dArray[0][0], dArray[1][1] - dArray[0][1], dArray[1][2] - dArray[0][2]);
        Vec3 vec33 = new Vec3(dArray[2][0] - dArray[0][0], dArray[2][1] - dArray[0][1], dArray[2][2] - dArray[0][2]);
        Vec3 vec34 = vec32.m_82537_(vec33).m_82541_();
        for (int i = 0; i < 4; ++i) {
            vertexConsumer.m_252986_(matrix4f, (float)(dArray[i][0] - vec3.f_82479_), (float)(dArray[i][1] - vec3.f_82480_), (float)(dArray[i][2] - vec3.f_82481_)).m_85950_(1.0f, 1.0f, 1.0f, panel.alpha()).m_7421_((float)(((double)(panel.material() % 8 * 16 + 2) + panel.uv()[i][0] * 11.0) / 128.0), (float)(((double)(panel.material() / 8 * 16 + 2) + panel.uv()[i][1] * 11.0) / 32.0)).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(matrix3f, (float)vec34.f_82479_, (float)vec34.f_82480_, (float)vec34.f_82481_).m_5752_();
        }
    }

    static {
        UV = new double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}};
        try {
            for (String string : List.of("candle_reap", "candle_prison", "candle_eclipse")) {
                boolean bl = string.equals("candle_reap");
                Object object = bl ? "/assets/ftboss/reap_flat/model.json" : "/assets/ftboss/dandan/" + string + ".json";
                InputStream inputStream = IronRenderer.class.getResourceAsStream((String)object);
                try {
                    if (inputStream == null) {
                        throw new IllegalStateException("Missing final Dandan Iron model: " + (String)object);
                    }
                    JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                    JsonObject jsonObject2 = jsonObject.getAsJsonObject("scene");
                    ArrayList<Face> arrayList = new ArrayList<Face>();
                    double[] dArray = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
                    double[] dArray2 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
                    for (JsonElement jsonElement : jsonObject.getAsJsonArray("faces")) {
                        int n;
                        JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                        double[][] dArray3 = new double[4][3];
                        double[][] dArray4 = new double[4][2];
                        double[] dArray5 = new double[3];
                        for (n = 0; n < 3; ++n) {
                            dArray5[n] = jsonObject3.getAsJsonArray("pivot").get(n).getAsDouble();
                        }
                        for (n = 0; n < 4; ++n) {
                            int n2;
                            for (n2 = 0; n2 < 3; ++n2) {
                                double d = jsonObject3.getAsJsonArray("points").get(n).getAsJsonArray().get(n2).getAsDouble();
                                if (!Double.isFinite(d)) {
                                    throw new IllegalStateException("Nonfinite final Dandan geometry");
                                }
                                dArray3[n][n2] = d;
                                dArray[n2] = Math.min(dArray[n2], d);
                                dArray2[n2] = Math.max(dArray2[n2], d);
                            }
                            for (n2 = 0; n2 < 2; ++n2) {
                                dArray4[n][n2] = jsonObject3.getAsJsonArray("uv").get(n).getAsJsonArray().get(n2).getAsDouble();
                            }
                        }
                        arrayList.add(new Face(dArray3, dArray4, dArray5, jsonObject3.get("bone").getAsInt(), jsonObject3.get("material").getAsInt(), jsonObject3.get("opacity").getAsFloat(), jsonObject3.get("group").getAsString().equals("detail"), jsonObject3.has("emitted") ? jsonObject3.get("emitted").getAsDouble() : -1.0));
                    }
                    double d = bl ? jsonObject2.get("nativeSpan").getAsDouble() : Math.max(dArray2[0] - dArray[0], Math.max(dArray2[1] - dArray[1], dArray2[2] - dArray[2]));
                    MODELS.put(string, new Model(string, jsonObject2.get("anchor").getAsString(), jsonObject2.get("span").getAsDouble(), jsonObject2.get("height").getAsDouble(), d, arrayList, bl ? REAP : AREA));
                }
                finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            }
        }
        catch (Exception exception) {
            MODELS.clear();
            LogUtils.getLogger().error("Cannot load final Dandan Iron adaptation", (Throwable)exception);
        }
    }

    private record Model(String id, String anchor, double span, double height, double nativeSpan, List<Face> faces, RenderType pipeline) {
    }

    private record Panel(double[][] points, double[][] uv, int material, float alpha, RenderType pipeline, double distance) {
    }

    private record Face(double[][] points, double[][] uv, double[] pivot, int bone, int material, float opacity, boolean detail, double emitted) {
    }
}

