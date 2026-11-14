/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.PacketDistributor$TargetPoint
 */
package net.farmingtales.ftboss.combat.xueli;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.XueliMaids;
import net.farmingtales.ftboss.combat.xueli.CombatNetwork;
import net.farmingtales.ftboss.combat.xueli.CombatPacket;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class XueliCombatServer {
    private static final Map<EasterBossEntity, Long> SHIELDED = new WeakHashMap<EasterBossEntity, Long>();
    private static final Map<EasterBossEntity, Long> HITS = new WeakHashMap<EasterBossEntity, Long>();

    private XueliCombatServer() {
    }

    private static ServerLevel level(EasterBossEntity easterBossEntity) {
        ServerLevel serverLevel;
        Level level;
        return easterBossEntity != null && easterBossEntity.getVariant() == 10 && (level = easterBossEntity.m_9236_()) instanceof ServerLevel ? (serverLevel = (ServerLevel)level) : null;
    }

    private static void send(EasterBossEntity easterBossEntity, int n, Vec3 vec3, double d, int n2, double d2, float f, List<CombatPacket.Anchor> list) {
        ServerLevel serverLevel = XueliCombatServer.level(easterBossEntity);
        if (serverLevel == null) {
            return;
        }
        CombatPacket combatPacket = new CombatPacket(serverLevel.m_46472_().m_135782_(), easterBossEntity.m_19879_(), easterBossEntity.m_20148_(), n, serverLevel.m_46467_(), vec3, d, n2, d2, f, list);
        if (combatPacket.valid()) {
            CombatNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 64.0, serverLevel.m_46472_())), (Object)combatPacket);
        }
    }

    private static double shieldRadius(EasterBossEntity easterBossEntity) {
        return Math.max(1.3, (double)easterBossEntity.m_20205_() / Math.sqrt(2.0) + 0.38);
    }

    public static void maids(EasterBossEntity easterBossEntity, boolean bl) {
        ServerLevel serverLevel = XueliCombatServer.level(easterBossEntity);
        if (serverLevel == null) {
            return;
        }
        long l = serverLevel.m_46467_();
        if (!bl || !easterBossEntity.m_6084_() || easterBossEntity.m_213877_()) {
            if (SHIELDED.remove(easterBossEntity) != null) {
                XueliCombatServer.send(easterBossEntity, 1, easterBossEntity.m_20182_(), XueliCombatServer.shieldRadius(easterBossEntity), 17, 0.0, easterBossEntity.m_146908_(), List.of());
            }
            return;
        }
        Long l2 = SHIELDED.get(easterBossEntity);
        if (l2 != null && l - l2 < 4L) {
            return;
        }
        SHIELDED.put(easterBossEntity, l);
        List<CombatPacket.Anchor> list = serverLevel.m_45976_(Mob.class, easterBossEntity.m_20191_().m_82400_(48.0)).stream().filter(mob -> mob.m_6084_() && XueliMaids.isGuard((Entity)mob) && mob.getPersistentData().m_128403_("ftboss_xueli_guard_owner") && easterBossEntity.m_20148_().equals(mob.getPersistentData().m_128342_("ftboss_xueli_guard_owner"))).limit(6L).map(mob -> new CombatPacket.Anchor(mob.m_19879_(), mob.m_20148_(), mob.m_20182_())).toList();
        XueliCombatServer.send(easterBossEntity, 0, easterBossEntity.m_20182_(), XueliCombatServer.shieldRadius(easterBossEntity), 12, 0.0, -easterBossEntity.m_146908_(), list);
    }

    public static void blocked(EasterBossEntity easterBossEntity, DamageSource damageSource, boolean bl) {
        ServerLevel serverLevel = XueliCombatServer.level(easterBossEntity);
        if (serverLevel == null || !bl) {
            return;
        }
        long l = serverLevel.m_46467_();
        Long l2 = HITS.get(easterBossEntity);
        if (l2 != null && l - l2 < 4L) {
            return;
        }
        HITS.put(easterBossEntity, l);
        Entity entity = damageSource == null ? null : damageSource.m_7639_();
        float f = entity == null ? easterBossEntity.m_146908_() : (float)Math.toDegrees(Math.atan2(entity.m_20185_() - easterBossEntity.m_20185_(), entity.m_20189_() - easterBossEntity.m_20189_()));
        XueliCombatServer.send(easterBossEntity, 2, easterBossEntity.m_20182_(), XueliCombatServer.shieldRadius(easterBossEntity), 9, 0.0, f, List.of());
    }

    public static void charge(EasterBossEntity easterBossEntity, int n) {
        if (XueliCombatServer.level(easterBossEntity) == null) {
            return;
        }
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        if (compoundTag.m_128456_() || compoundTag.m_128471_("released")) {
            return;
        }
        Vec3 vec3 = new Vec3(compoundTag.m_128459_("x"), compoundTag.m_128459_("y"), compoundTag.m_128459_("z"));
        XueliCombatServer.send(easterBossEntity, n, vec3, n == 7 ? 8.0 : 4.5, compoundTag.m_128451_("duration"), 0.0, easterBossEntity.m_146908_(), List.of());
    }

    public static void impact(EasterBossEntity easterBossEntity, int n) {
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        Vec3 vec3 = n == 8 ? new Vec3(compoundTag.m_128459_("x"), compoundTag.m_128459_("y"), compoundTag.m_128459_("z")) : easterBossEntity.m_20182_();
        XueliCombatServer.send(easterBossEntity, n, vec3, n == 8 ? 8.0 : 4.5, n == 8 ? 15 : 12, 0.0, easterBossEntity.m_146908_(), List.of());
    }

    public static void roll(EasterBossEntity easterBossEntity, int n) {
        if (n > 0 && XueliCombatServer.level(easterBossEntity) != null && easterBossEntity.f_19797_ % 2 == 0) {
            Vec3 vec3 = easterBossEntity.m_20184_();
            float f = vec3.f_82479_ * vec3.f_82479_ + vec3.f_82481_ * vec3.f_82481_ > 1.0E-4 ? (float)Math.toDegrees(Math.atan2(vec3.f_82479_, vec3.f_82481_)) : -easterBossEntity.m_146908_();
            XueliCombatServer.send(easterBossEntity, 5, easterBossEntity.m_20182_(), 3.0, Math.min(n, 12), 0.0, f, List.of());
        }
    }

    public static void crush(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        XueliCombatServer.target(easterBossEntity, livingEntity, 6, 60);
    }

    public static void stun(EasterBossEntity easterBossEntity, LivingEntity livingEntity, int n) {
        XueliCombatServer.target(easterBossEntity, livingEntity, 11, n);
    }

    private static void target(EasterBossEntity easterBossEntity, LivingEntity livingEntity, int n, int n2) {
        if (XueliCombatServer.level(easterBossEntity) == null || livingEntity == null || !livingEntity.m_6084_()) {
            return;
        }
        XueliCombatServer.send(easterBossEntity, n, livingEntity.m_20182_(), 1.0, Math.min(200, Math.max(1, n2)), 0.0, livingEntity.m_146908_(), List.of(new CombatPacket.Anchor(livingEntity.m_19879_(), livingEntity.m_20148_(), livingEntity.m_20182_())));
    }

    public static void fuse(EasterBossEntity easterBossEntity, int n) {
        if (n > 0 && easterBossEntity.isMini() && easterBossEntity.f_19797_ % 2 == 0) {
            XueliCombatServer.send(easterBossEntity, 9, easterBossEntity.m_20182_(), 0.8, Math.min(n, 12), Math.max(0.0, Math.min(1.0, 1.0 - (double)n / 100.0)), easterBossEntity.m_146908_(), List.of());
        }
    }

    public static void burst(EasterBossEntity easterBossEntity, boolean bl) {
        if (easterBossEntity.isMini()) {
            XueliCombatServer.send(easterBossEntity, 10, easterBossEntity.m_20182_(), bl ? 3.375 : 4.5, 15, 0.0, easterBossEntity.m_146908_(), List.of());
        }
    }

    public static void stunTell(LivingEntity livingEntity, Component component, boolean bl, EasterBossEntity easterBossEntity) {
        if (easterBossEntity.getVariant() != 10) {
            ManorLivingTargets.tell(livingEntity, component, bl);
        }
    }
}

