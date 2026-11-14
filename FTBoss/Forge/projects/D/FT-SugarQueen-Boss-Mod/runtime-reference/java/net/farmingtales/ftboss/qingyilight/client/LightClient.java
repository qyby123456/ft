/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.qingyilight.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyilight.LightGeometry;
import net.farmingtales.ftboss.qingyilight.LightPacket;
import net.farmingtales.ftboss.qingyilight.LightQueue;
import net.farmingtales.ftboss.qingyilight.LightRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class LightClient {
    private static final LightQueue QUEUE = new LightQueue();
    private static final Map<UUID, ArrayDeque<Vec3>> HISTORY = new HashMap<UUID, ArrayDeque<Vec3>>();
    private static ClientLevel activeWorld;

    private LightClient() {
    }

    private static void synchronize(ClientLevel clientLevel) {
        if (clientLevel != activeWorld) {
            QUEUE.clear();
            HISTORY.clear();
            activeWorld = clientLevel;
        }
    }

    public static void accept(LightPacket lightPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        LightClient.synchronize(clientLevel);
        if (clientLevel != null && clientLevel.m_46472_().m_135782_().equals((Object)lightPacket.dimension())) {
            QUEUE.accept(lightPacket, clientLevel.m_46467_());
        }
    }

    private static boolean validOwner(ClientLevel clientLevel, LightPacket lightPacket) {
        EasterBossEntity easterBossEntity;
        Entity entity = clientLevel.m_6815_(lightPacket.ownerId());
        return entity == null || entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).m_20148_().equals(lightPacket.owner()) && easterBossEntity.getVariant() == 5 && !easterBossEntity.isMini() && easterBossEntity.m_6084_() && !easterBossEntity.m_21525_();
    }

    public static CompoundTag legacyCue(EasterBossEntity easterBossEntity) {
        return LightClient.filterLegacyCue(easterBossEntity.getSkillCue(), easterBossEntity);
    }

    public static CompoundTag filterLegacyCue(CompoundTag compoundTag, EasterBossEntity easterBossEntity) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        LightClient.synchronize(clientLevel);
        if (clientLevel != null && easterBossEntity.getVariant() == 5 && QUEUE.snapshot(clientLevel.m_46467_()).stream().anyMatch(lightPacket -> lightPacket.owner().equals(easterBossEntity.m_20148_()) && lightPacket.kind() <= 3 && lightPacket.phase() == (compoundTag.m_128471_("released") ? 1 : 0) && lightPacket.born() == compoundTag.m_128454_("start") && Math.abs(lightPacket.radius() - compoundTag.m_128459_("radius")) < 0.001)) {
            return new CompoundTag();
        }
        return compoundTag;
    }

    public static void tick() {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        LightClient.synchronize(clientLevel);
        if (clientLevel == null) {
            return;
        }
        QUEUE.removeIf(lightPacket -> !LightClient.validOwner(clientLevel, lightPacket) || lightPacket.entityId() >= 0 && (clientLevel.m_6815_(lightPacket.entityId()) == null && clientLevel.m_46467_() - lightPacket.born() > 40L || clientLevel.m_6815_(lightPacket.entityId()) != null && (!clientLevel.m_6815_(lightPacket.entityId()).m_20148_().equals(lightPacket.entity()) || clientLevel.m_6815_(lightPacket.entityId()).m_213877_())));
        List<LightPacket> list = QUEUE.snapshot(clientLevel.m_46467_());
        HISTORY.keySet().removeIf(uUID -> list.stream().noneMatch(lightPacket -> uUID.equals(lightPacket.entity())));
        for (LightPacket lightPacket2 : list) {
            Entity entity;
            if (lightPacket2.entityId() < 0 || !LightClient.validOwner(clientLevel, lightPacket2) || (entity = clientLevel.m_6815_(lightPacket2.entityId())) == null || !entity.m_20148_().equals(lightPacket2.entity()) || !HISTORY.containsKey(lightPacket2.entity()) && HISTORY.size() >= 48) continue;
            ArrayDeque arrayDeque = HISTORY.computeIfAbsent(lightPacket2.entity(), uUID -> new ArrayDeque());
            Vec3 vec3 = entity.m_20182_();
            if (!arrayDeque.isEmpty() && ((Vec3)arrayDeque.getLast()).m_82557_(vec3) > 16.0) {
                arrayDeque.clear();
            }
            if (arrayDeque.isEmpty() || ((Vec3)arrayDeque.getLast()).m_82557_(vec3) > 1.0E-6) {
                arrayDeque.addLast(vec3);
            }
            while (arrayDeque.size() > 7) {
                arrayDeque.removeFirst();
            }
        }
    }

    public static List<Draw> scenes(float f) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        LightClient.synchronize(clientLevel);
        ArrayList<Draw> arrayList = new ArrayList<Draw>();
        if (clientLevel == null) {
            return arrayList;
        }
        for (LightPacket lightPacket : QUEUE.snapshot(clientLevel.m_46467_())) {
            Object object;
            double d;
            if (!LightClient.validOwner(clientLevel, lightPacket) || (d = (double)((float)clientLevel.m_46467_() + f - (float)lightPacket.born())) < 0.0) continue;
            Vec3 vec3 = lightPacket.origin();
            Vec3 vec32 = lightPacket.direction();
            Entity entity = clientLevel.m_6815_(lightPacket.ownerId());
            if (lightPacket.kind() == 4 || lightPacket.kind() == 9) {
                if (entity == null) continue;
                vec3 = entity.m_20318_(f);
            }
            if (lightPacket.entityId() >= 0) {
                object = clientLevel.m_6815_(lightPacket.entityId());
                if (object == null || !object.m_20148_().equals(lightPacket.entity()) || object.m_213877_()) continue;
                vec3 = object.m_20318_(f);
                vec32 = object.m_20184_().m_82541_();
            }
            object = LightGeometry.frame(lightPacket.kind(), lightPacket.phase(), d, lightPacket.duration(), lightPacket.radius(), lightPacket.pulse());
            arrayList.add(new Draw(lightPacket, vec3, vec32, (LightGeometry.Frame)object));
            ArrayDeque<Vec3> arrayDeque = HISTORY.get(lightPacket.entity());
            if (arrayDeque == null || arrayDeque.size() < 2) continue;
            Vec3[] vec3Array = (Vec3[])arrayDeque.toArray(Vec3[]::new);
            ArrayList<LightGeometry.Layer> arrayList2 = new ArrayList<LightGeometry.Layer>();
            for (int i = 1; i < vec3Array.length; ++i) {
                if (!LightRules.trailSegment(vec3Array[i].m_82557_(vec3Array[i - 1]))) continue;
                ArrayList<double[]> arrayList3 = new ArrayList<double[]>();
                Vec3 vec33 = vec3Array[i - 1].m_82546_(vec3);
                Vec3 vec34 = vec3Array[i].m_82546_(vec3);
                LightGeometry.prism(arrayList3, new double[]{vec33.f_82479_, vec33.f_82480_, vec33.f_82481_}, new double[]{vec34.f_82479_, vec34.f_82480_, vec34.f_82481_}, 0.012 + 0.008 * (double)i / (double)vec3Array.length);
                arrayList2.add(new LightGeometry.Layer(false, "actual_entity_history", 0.12 + 0.38 * (double)i / (double)vec3Array.length, lightPacket.kind() == 8 ? 12687320 : 9562611, arrayList3));
            }
            arrayList.add(new Draw(lightPacket, vec3, new Vec3(0.0, 0.0, 1.0), new LightGeometry.Frame(arrayList2)));
        }
        return arrayList;
    }

    public record Draw(LightPacket packet, Vec3 anchor, Vec3 direction, LightGeometry.Frame geometry) {
    }
}

