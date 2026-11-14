/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.youzi.spells.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.farmingtales.ftboss.youzi.facing.YouziFacing;
import net.farmingtales.ftboss.youzi.spells.YouziSpellAssets;
import net.farmingtales.ftboss.youzi.spells.YouziSpellGeometry;
import net.farmingtales.ftboss.youzi.spells.YouziSpellPacket;
import net.farmingtales.ftboss.youzi.spells.YouziSpellTiming;
import net.farmingtales.ftboss.youzi.spells.YouziStarProjectile;
import net.farmingtales.ftboss.youzi.spells.YouziVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class YouziSpellVisual {
    public static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "youzi_spells/youzi_spell_atlas.png");
    private static final ResourceLocation RIBBON = new ResourceLocation("ftboss", "youzi_spells/effigy_flight_ribbon.png");
    private static final RenderType MODEL_TYPE = RenderType.m_234338_((ResourceLocation)ATLAS);
    private static final RenderType TRAIL_TYPE = RenderType.m_234338_((ResourceLocation)RIBBON);
    private static final Map<String, Active> ACTIVE = new LinkedHashMap<String, Active>();
    private static final Map<Integer, Star> QUEUED = new LinkedHashMap<Integer, Star>();
    private static final Map<Integer, ArrayDeque<YouziHomingFlight.Vector>> HISTORY = new LinkedHashMap<Integer, ArrayDeque<YouziHomingFlight.Vector>>();
    private static final List<Impact> IMPACTS = new ArrayList<Impact>();
    private static ClientLevel world;
    private static int budget;

    public static boolean supports(String string) {
        return string.equals("effigy_volley") || string.equals("effigy_ward") || string.equals("effigy_procession");
    }

    private static void synchronize() {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel != world) {
            ACTIVE.clear();
            QUEUED.clear();
            HISTORY.clear();
            IMPACTS.clear();
            world = clientLevel;
        }
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        if (spellVisualPacket == null || !spellVisualPacket.valid() || !YouziSpellVisual.supports(spellVisualPacket.spell().id) || YouziSpellAssets.geometry() == null) {
            return false;
        }
        YouziSpellVisual.synchronize();
        if (world == null || !world.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        long l = world.m_46467_();
        String string = String.valueOf(spellVisualPacket.owner()) + ":" + spellVisualPacket.spell().id;
        if (ACTIVE.containsKey(string) && YouziSpellVisual.ACTIVE.get((Object)string).authoritative) {
            return true;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
            if (IMPACTS.size() >= 24) {
                IMPACTS.remove(0);
            }
            IMPACTS.add(new Impact(YouziSpellVisual.vector(spellVisualPacket.end()), l));
            return true;
        }
        String string2 = String.valueOf(spellVisualPacket.owner()) + ":" + spellVisualPacket.spell().id;
        Active active = ACTIVE.get(string2);
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.PULSE && active != null && active.packet.phase() != SpellVisualRules.Phase.CHARGE) {
            active.packet = spellVisualPacket;
            active.refreshed = l;
            return true;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE && active != null && active.packet.phase() == SpellVisualRules.Phase.CHARGE) {
            active.packet = spellVisualPacket;
            active.refreshed = l;
            return true;
        }
        if (ACTIVE.size() >= 12 && !ACTIVE.containsKey(string2)) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(string2, new Active(spellVisualPacket, l));
        return true;
    }

    public static void acceptAuthoritative(YouziSpellPacket youziSpellPacket) {
        boolean bl;
        YouziSpellVisual.synchronize();
        SpellVisualPacket spellVisualPacket = youziSpellPacket.visual();
        if (world == null || !world.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension()) || !YouziSpellVisual.supports(spellVisualPacket.spell().id)) {
            return;
        }
        long l = world.m_46467_();
        if (!YouziSpellTiming.eventFresh(l, youziSpellPacket.sentTick(), spellVisualPacket.phase().name())) {
            return;
        }
        if (spellVisualPacket.phase() == SpellVisualRules.Phase.BEAM) {
            if (IMPACTS.size() >= 24) {
                IMPACTS.remove(0);
            }
            IMPACTS.add(new Impact(YouziSpellVisual.vector(spellVisualPacket.end()), youziSpellPacket.sentTick()));
            return;
        }
        String string = String.valueOf(spellVisualPacket.owner()) + ":" + spellVisualPacket.spell().id;
        Active active = ACTIVE.get(string);
        if (active != null && active.authoritative && !YouziSpellTiming.ordered(youziSpellPacket.sentTick(), spellVisualPacket.phase().name(), active.refreshed, active.packet.phase().name())) {
            return;
        }
        boolean bl2 = bl = active != null && (spellVisualPacket.phase() == SpellVisualRules.Phase.PULSE && active.packet.phase() != SpellVisualRules.Phase.CHARGE || spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE && active.packet.phase() == SpellVisualRules.Phase.CHARGE);
        Active active2 = bl ? active : new Active(spellVisualPacket, spellVisualPacket.phase() == SpellVisualRules.Phase.CHARGE ? youziSpellPacket.castStarted() : youziSpellPacket.sentTick());
        active2.packet = spellVisualPacket;
        active2.refreshed = youziSpellPacket.sentTick();
        active2.castStarted = youziSpellPacket.castStarted();
        active2.castDuration = youziSpellPacket.castDuration();
        active2.serverScale = youziSpellPacket.scale();
        active2.authoritative = true;
        if (ACTIVE.size() >= 12 && !ACTIVE.containsKey(string)) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }
        ACTIVE.put(string, active2);
    }

    public static void queue(YouziStarProjectile youziStarProjectile, float f) {
        YouziSpellVisual.synchronize();
        if (QUEUED.size() < 64) {
            QUEUED.put(youziStarProjectile.m_19879_(), new Star(youziStarProjectile, f));
        }
    }

    private static LivingEntity owner(Active active) {
        LivingEntity livingEntity;
        Entity entity = world.m_6815_(active.packet.ownerId());
        return entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).m_6084_() && livingEntity.m_20148_().equals(active.packet.owner()) ? livingEntity : null;
    }

    private static boolean expired(Active active, long l) {
        LivingEntity livingEntity = YouziSpellVisual.owner(active);
        return YouziSpellTiming.expired(active.packet.spell().id, active.packet.phase().name(), l, active.started, active.refreshed, livingEntity != null, livingEntity != null && (livingEntity.m_21023_(MobEffects.f_19606_) || livingEntity.m_21023_(MobEffects.f_19617_)), livingEntity == null ? 0.0 : YouziSpellVisual.vector(livingEntity.m_20182_()).subtract(YouziSpellVisual.vector(active.packet.start())).length());
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        YouziSpellVisual.synchronize();
        if (world == null) {
            return;
        }
        ACTIVE.values().removeIf(active -> YouziSpellVisual.expired(active, world.m_46467_()));
        IMPACTS.removeIf(impact -> world.m_46467_() - impact.tick() > 4L);
        HISTORY.keySet().removeIf(n -> !(world.m_6815_(n.intValue()) instanceof YouziStarProjectile));
        for (Entity entity : world.m_104735_()) {
            if (!(entity instanceof YouziStarProjectile) || HISTORY.size() >= 64 && !HISTORY.containsKey(entity.m_19879_())) continue;
            ArrayDeque arrayDeque = HISTORY.computeIfAbsent(entity.m_19879_(), n -> new ArrayDeque());
            arrayDeque.addLast(YouziSpellVisual.vector(entity.m_20182_()));
            while (arrayDeque.size() > 3) {
                arrayDeque.removeFirst();
            }
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            YouziSpellVisual.clear();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        YouziSpellVisual.clear();
    }

    private static void clear() {
        ACTIVE.clear();
        QUEUED.clear();
        HISTORY.clear();
        IMPACTS.clear();
        world = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        YouziSpellVisual.synchronize();
        YouziSpellGeometry youziSpellGeometry = YouziSpellAssets.geometry();
        if (world == null || youziSpellGeometry == null || ACTIVE.isEmpty() && QUEUED.isEmpty() && IMPACTS.isEmpty()) {
            QUEUED.clear();
            return;
        }
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, true);
        try {
            double d;
            YouziHomingFlight.Vector vector;
            double d2;
            ArrayDeque<YouziHomingFlight.Vector> arrayDeque;
            Iterator<Object> iterator;
            YouziHomingFlight.Vector vector2 = YouziSpellVisual.vector(renderLevelStageEvent.getCamera().m_90583_());
            Vector3f vector3f = renderLevelStageEvent.getCamera().m_253058_();
            YouziHomingFlight.Vector vector3 = new YouziHomingFlight.Vector(vector3f.x(), vector3f.y(), vector3f.z());
            boolean bl2 = !renderLevelStageEvent.getCamera().m_90594_();
            Matrix4f matrix4f = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
            MultiBufferSource.BufferSource bufferSource = Minecraft.m_91087_().m_91269_().m_110104_();
            VertexConsumer vertexConsumer = bufferSource.m_6299_(MODEL_TYPE);
            budget = 6000;
            for (Star object22 : QUEUED.values()) {
                iterator = YouziSpellVisual.vector(object22.entity().m_20318_(object22.partial()));
                YouziHomingFlight.Vector vector4 = YouziSpellVisual.vector(object22.entity().m_20184_()).normalize();
                youziSpellGeometry.seed(object22.entity().scale(), arg_0 -> YouziSpellVisual.lambda$render$0(vertexConsumer, matrix4f, (YouziHomingFlight.Vector)((Object)iterator), vector4, vector2, vector3, bl2, arg_0));
            }
            List<Active> list = ACTIVE.values().stream().filter(active -> !YouziSpellVisual.expired(active, world.m_46467_())).sorted(Comparator.comparingDouble(active -> YouziSpellVisual.vector(active.packet.start()).subtract(vector2).length())).toList();
            Iterator iterator2 = list.iterator();
            while (iterator2.hasNext()) {
                double d3;
                iterator = (Active)iterator2.next();
                LivingEntity livingEntity = YouziSpellVisual.owner((Active)((Object)iterator));
                arrayDeque = ((Active)((Object)iterator)).packet.spell().id;
                boolean bl3 = ((String)((Object)arrayDeque)).equals("effigy_procession");
                YouziHomingFlight.Vector vector5 = YouziSpellVisual.vector(bl3 ? ((Active)((Object)iterator)).packet.start() : livingEntity.m_20318_(renderLevelStageEvent.getPartialTick()));
                if (vector5.subtract(vector2).length() > 64.0 || budget < 1200) continue;
                d2 = Math.max(0.0f, ((float)world.m_46467_() + renderLevelStageEvent.getPartialTick() - (float)((Active)((Object)iterator)).started) / 20.0f);
                MagicData magicData = MagicData.getPlayerMagicData((LivingEntity)livingEntity);
                double d4 = ((Active)((Object)iterator)).authoritative && ((Active)((Object)iterator)).castDuration > 0 ? (double)(((float)world.m_46467_() + renderLevelStageEvent.getPartialTick() - (float)((Active)((Object)iterator)).castStarted) / (float)((Active)((Object)iterator)).castDuration) : (double)(magicData.getCastCompletionPercent() + renderLevelStageEvent.getPartialTick() / (float)Math.max(1, magicData.getCastDuration()));
                YouziSpellTiming.Sample sample = YouziSpellTiming.sample((String)((Object)arrayDeque), ((Active)((Object)iterator)).packet.phase() == SpellVisualRules.Phase.CHARGE, d2, d4, youziSpellGeometry.length((String)((Object)arrayDeque), "charge"), youziSpellGeometry.length((String)((Object)arrayDeque), "release"));
                vector = YouziSpellVisual.vector(((Active)((Object)iterator)).packet.end()).subtract(YouziSpellVisual.vector(((Active)((Object)iterator)).packet.start()));
                d = YouziFacing.modelYaw(Math.toDegrees(Math.atan2(-vector.coordinateX(), -vector.coordinateZ())), livingEntity, ((Active)((Object)iterator)).packet, renderLevelStageEvent.getPartialTick());
                double d5 = d3 = ((Active)((Object)iterator)).authoritative ? ((Active)((Object)iterator)).serverScale : BossSpellTuning.radiusScale(livingEntity);
                int n = vector5.subtract(vector2).length() > 32.0 ? 4 : (vector5.subtract(vector2).length() > 16.0 ? 2 : 1);
                youziSpellGeometry.sample((String)((Object)arrayDeque), sample.channel(), sample.seconds(), d3, bl3, sample.channel().equals("charge"), n, quad -> YouziSpellVisual.draw(vertexConsumer, matrix4f, YouziSpellVisual.transformed(quad, vector4, d), vector2, vector3, bl2, sample.opacity()));
            }
            bufferSource.m_109912_(MODEL_TYPE);
            VertexConsumer vertexConsumer2 = bufferSource.m_6299_(TRAIL_TYPE);
            budget = 1600;
            iterator = list.iterator();
            while (iterator.hasNext()) {
                Active active2 = (Active)iterator.next();
                if (!active2.packet.spell().id.equals("effigy_procession") || active2.packet.phase() == SpellVisualRules.Phase.CHARGE || ((YouziHomingFlight.Vector)((Object)(arrayDeque = YouziSpellVisual.vector(active2.packet.start())))).subtract(vector2).length() > 64.0) continue;
                double d6 = active2.authoritative ? active2.serverScale : BossSpellTuning.radiusScale(YouziSpellVisual.owner(active2));
                d2 = ((float)world.m_46467_() + renderLevelStageEvent.getPartialTick() - (float)active2.started) / 20.0f;
                double d7 = YouziVisibility.smooth((60.0 - d2 * 20.0) / 8.0) * 0.24;
                for (int i = 0; i < 64; ++i) {
                    double d8 = Math.PI * 2 * (double)i / 64.0;
                    d = Math.PI * 2 * (double)(i + 1) / 64.0;
                    YouziHomingFlight.Vector[] vectorArray = new YouziHomingFlight.Vector[]{((YouziHomingFlight.Vector)((Object)arrayDeque)).add(new YouziHomingFlight.Vector(Math.cos(d8) * 4.0 * d6, 0.025, Math.sin(d8) * 4.0 * d6)), ((YouziHomingFlight.Vector)((Object)arrayDeque)).add(new YouziHomingFlight.Vector(Math.cos(d) * 4.0 * d6, 0.025, Math.sin(d) * 4.0 * d6)), ((YouziHomingFlight.Vector)((Object)arrayDeque)).add(new YouziHomingFlight.Vector(Math.cos(d) * 3.95 * d6, 0.025, Math.sin(d) * 3.95 * d6)), ((YouziHomingFlight.Vector)((Object)arrayDeque)).add(new YouziHomingFlight.Vector(Math.cos(d8) * 3.95 * d6, 0.025, Math.sin(d8) * 3.95 * d6))};
                    YouziSpellVisual.draw(vertexConsumer2, matrix4f, new YouziSpellGeometry.Quad(vectorArray, new double[][]{{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}}, d7, 1.0, true), vector2, vector3, bl2, 1.0);
                }
            }
            for (Star star : QUEUED.values()) {
                arrayDeque = HISTORY.get(star.entity().m_19879_());
                if (arrayDeque == null) continue;
                ArrayList<YouziHomingFlight.Vector> arrayList = new ArrayList<YouziHomingFlight.Vector>(arrayDeque);
                arrayList.add(YouziSpellVisual.vector(star.entity().m_20318_(star.partial())));
                for (int i = 1; i < arrayList.size(); ++i) {
                    YouziHomingFlight.Vector vector6 = (YouziHomingFlight.Vector)arrayList.get(i - 1);
                    YouziHomingFlight.Vector vector7 = (YouziHomingFlight.Vector)arrayList.get(i);
                    YouziHomingFlight.Vector vector8 = vector7.subtract(vector6).cross(vector2.subtract(vector6)).normalize().multiply(0.065 * star.entity().scale());
                    YouziSpellGeometry.Quad quad2 = new YouziSpellGeometry.Quad(new YouziHomingFlight.Vector[]{vector6.add(vector8), vector7.add(vector8), vector7.subtract(vector8), vector6.subtract(vector8)}, new double[][]{{0.0, 1.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 0.0}}, 0.55 * (double)i / (double)arrayList.size(), 1.0, true);
                    YouziSpellVisual.draw(vertexConsumer2, matrix4f, quad2, vector2, vector3, bl2, 1.0);
                }
            }
            for (Impact impact : IMPACTS) {
                double d9 = ((float)world.m_46467_() + renderLevelStageEvent.getPartialTick() - (float)impact.tick()) / 20.0f;
                if (d9 < 0.0 || d9 >= 0.18) continue;
                double d10 = 0.08 + d9 / 0.18 * 0.3;
                for (int i = 0; i < 8; ++i) {
                    double d11 = Math.PI * 2 * (double)i / 8.0;
                    YouziHomingFlight.Vector vector9 = new YouziHomingFlight.Vector(Math.cos(d11), 0.25, Math.sin(d11));
                    YouziHomingFlight.Vector vector10 = impact.point().add(vector9.multiply(d10));
                    vector = impact.point().add(vector9.multiply(d10 + 0.12));
                    YouziHomingFlight.Vector vector11 = vector9.cross(vector2.subtract(vector10)).normalize().multiply(0.018);
                    YouziSpellVisual.draw(vertexConsumer2, matrix4f, new YouziSpellGeometry.Quad(new YouziHomingFlight.Vector[]{vector10.add(vector11), vector.add(vector11), vector.subtract(vector11), vector10.subtract(vector11)}, new double[][]{{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}}, 1.0 - d9 / 0.18, 1.0, true), vector2, vector3, bl2, 1.0);
                }
            }
            bufferSource.m_109912_(TRAIL_TYPE);
        }
        finally {
            QUEUED.clear();
            SpellBloomRenderer.end(bl);
        }
    }

    private static YouziSpellGeometry.Quad transformed(YouziSpellGeometry.Quad quad, YouziHomingFlight.Vector vector, double d) {
        YouziHomingFlight.Vector[] vectorArray = new YouziHomingFlight.Vector[4];
        for (int i = 0; i < 4; ++i) {
            vectorArray[i] = YouziSpellGeometry.rotate(quad.points()[i], new YouziHomingFlight.Vector(0.0, d, 0.0)).add(vector);
        }
        return new YouziSpellGeometry.Quad(vectorArray, quad.uv(), quad.opacity(), quad.emission(), quad.accent());
    }

    private static YouziSpellGeometry.Quad oriented(YouziSpellGeometry.Quad quad, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2) {
        YouziHomingFlight.Vector vector3 = vector2.cross(new YouziHomingFlight.Vector(0.0, 1.0, 0.0));
        if (vector3.length() < 1.0E-6) {
            vector3 = vector2.cross(new YouziHomingFlight.Vector(1.0, 0.0, 0.0));
        }
        vector3 = vector3.normalize();
        YouziHomingFlight.Vector vector4 = vector3.cross(vector2).normalize();
        YouziHomingFlight.Vector[] vectorArray = new YouziHomingFlight.Vector[4];
        for (int i = 0; i < 4; ++i) {
            YouziHomingFlight.Vector vector5 = quad.points()[i];
            vectorArray[i] = vector.add(vector3.multiply(vector5.coordinateX())).add(vector4.multiply(vector5.coordinateY())).subtract(vector2.multiply(vector5.coordinateZ()));
        }
        return new YouziSpellGeometry.Quad(vectorArray, quad.uv(), quad.opacity(), quad.emission(), quad.accent());
    }

    private static void draw(VertexConsumer vertexConsumer, Matrix4f matrix4f, YouziSpellGeometry.Quad quad, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, boolean bl, double d) {
        if (budget-- <= 0) {
            return;
        }
        YouziHomingFlight.Vector vector3 = quad.points()[0].add(quad.points()[1]).add(quad.points()[2]).add(quad.points()[3]).multiply(0.25);
        float f = (float)(quad.opacity() * d * YouziVisibility.opacity(vector3, vector, vector2, bl));
        if ((double)f < 0.005) {
            return;
        }
        YouziHomingFlight.Vector vector4 = quad.points()[1].subtract(quad.points()[0]).cross(quad.points()[2].subtract(quad.points()[0])).normalize();
        for (int i = 0; i < 4; ++i) {
            YouziHomingFlight.Vector vector5 = quad.points()[i].subtract(vector);
            vertexConsumer.m_252986_(matrix4f, (float)vector5.coordinateX(), (float)vector5.coordinateY(), (float)vector5.coordinateZ()).m_85950_(1.0f, 1.0f, 1.0f, f).m_7421_((float)quad.uv()[i][0], (float)quad.uv()[i][1]).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_5601_((float)vector4.coordinateX(), (float)vector4.coordinateY(), (float)vector4.coordinateZ()).m_5752_();
        }
    }

    private static YouziHomingFlight.Vector vector(Vec3 vec3) {
        return new YouziHomingFlight.Vector(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
    }

    private static /* synthetic */ void lambda$render$0(VertexConsumer vertexConsumer, Matrix4f matrix4f, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, YouziHomingFlight.Vector vector3, YouziHomingFlight.Vector vector4, boolean bl, YouziSpellGeometry.Quad quad) {
        YouziSpellVisual.draw(vertexConsumer, matrix4f, YouziSpellVisual.oriented(quad, vector, vector2), vector3, vector4, bl, 1.0);
    }

    private static final class Active {
        SpellVisualPacket packet;
        final long started;
        long refreshed;
        long castStarted;
        int castDuration;
        double serverScale = 1.0;
        boolean authoritative;

        Active(SpellVisualPacket spellVisualPacket, long l) {
            this.packet = spellVisualPacket;
            this.started = this.refreshed = l;
        }
    }

    private record Impact(YouziHomingFlight.Vector point, long tick) {
    }

    private record Star(YouziStarProjectile entity, float partial) {
    }
}

