/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.skill;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.qizaicross.TemporaryTriads;
import net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard;
import net.farmingtales.ftboss.skill.qizaiparty.MultiplayerCrystals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class QizaiCrystals {
    private static final Map<ServerLevel, Map<UUID, Formation>> ACTIVE = new WeakHashMap<ServerLevel, Map<UUID, Formation>>();

    private QizaiCrystals() {
    }

    public static boolean validOwner(EasterBossEntity boss) {
        return !(boss == null || !boss.m_6084_() || boss.m_213877_() || boss.m_21525_() && !boss.isQizaiUninterruptibleCasting() || boss.getVariant() != 11 || boss.isMini() || boss.isGiant() || boss.m_20147_() && !boss.isQizaiProtectedCasting() || ManorFight.isFinaleMember((LivingEntity)boss));
    }

    public static boolean target(EasterBossEntity easterBossEntity, Entity entity) {
        LivingEntity livingEntity;
        if (!ManorLivingTargets.canHit((Entity)easterBossEntity, entity)) {
            return false;
        }
        return entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).m_6084_() && !livingEntity.m_213877_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_() && livingEntity.m_9236_() == easterBossEntity.m_9236_() && easterBossEntity.m_20280_((Entity)livingEntity) <= 4096.0;
    }

    private static Formation formation(EasterBossEntity boss) {
        Formation formation;
        Level level;
        if (boss != null && (level = boss.m_9236_()) instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            formation = (Formation)ACTIVE.getOrDefault(level2, Map.of()).get(boss.m_20148_());
        } else {
            formation = null;
        }
        return formation;
    }

    public static List<QizaiCrystalEntity> members(EasterBossEntity boss) {
        Formation formation = QizaiCrystals.formation(boss);
        return CrossGuard.members(MultiplayerCrystals.members(TemporaryTriads.members(formation == null ? List.of() : Arrays.stream(formation.crystals).filter(crystal -> crystal != null && crystal.m_6084_() && !crystal.m_213877_()).toList(), boss), boss), boss);
    }

    public static boolean current(QizaiCrystalEntity crystal) {
        Formation formation = QizaiCrystals.formation(crystal.getOwner());
        return CrossGuard.accept(MultiplayerCrystals.accept(TemporaryTriads.accept(formation != null && formation.crystals[crystal.role()] == crystal, crystal), crystal), crystal);
    }

    public static LivingEntity combatTarget(EasterBossEntity boss) {
        if (!QizaiCrystals.validOwner(boss)) {
            return null;
        }
        Formation formation = QizaiCrystals.formation(boss);
        if (QizaiCrystals.target(boss, (Entity)boss.m_5448_())) {
            if (formation != null) {
                formation.target = boss.m_5448_();
            }
            return boss.m_5448_();
        }
        if (formation != null && QizaiCrystals.target(boss, (Entity)formation.target)) {
            return formation.target;
        }
        Level level = boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel level2 = (ServerLevel)level;
        return level2.m_6907_().stream().filter(player -> QizaiCrystals.target(boss, (Entity)player)).min(Comparator.comparingDouble(arg_0 -> ((EasterBossEntity)boss).m_20280_(arg_0))).orElse(null);
    }

    public static void tick(EasterBossEntity boss) {
        Level level = boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            MultiplayerCrystals.tick(boss);
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (!QizaiCrystals.validOwner(boss)) {
            QizaiCrystals.cancel(boss);
            MultiplayerCrystals.tick(boss);
            return;
        }
        Map forms = ACTIVE.computeIfAbsent(level2, ignored -> new HashMap());
        Formation formation = (Formation)forms.get(boss.m_20148_());
        LivingEntity target = QizaiCrystals.combatTarget(boss);
        if (target == null) {
            if (formation != null && ++formation.idle >= 100) {
                QizaiCrystals.cancel(boss);
            }
            MultiplayerCrystals.tick(boss);
            return;
        }
        if (formation == null) {
            if (forms.size() >= 64) {
                MultiplayerCrystals.tick(boss);
                return;
            }
            formation = new Formation();
            forms.put(boss.m_20148_(), formation);
        }
        formation.target = target;
        formation.idle = 0;
        long now = level2.m_46467_();
        for (int role = 0; role < 3; ++role) {
            QizaiCrystalEntity crystal = formation.crystals[role];
            if (crystal != null && (!crystal.m_6084_() || crystal.m_213877_() || level2.m_8791_(crystal.m_20148_()) != crystal)) {
                crystal.m_146870_();
                formation.crystals[role] = null;
                formation.ready[role] = now + 240L;
            }
            if (formation.crystals[role] != null || now < formation.ready[role]) continue;
            QizaiCrystalEntity summoned = new QizaiCrystalEntity(level2, boss, role);
            double angle = (double)role * Math.PI * 2.0 / 3.0;
            Vec3 spawn = boss.m_20182_().m_82520_(Math.cos(angle) * 2.0, 2.8, Math.sin(angle) * 2.0);
            summoned.m_146884_(spawn);
            if (!level2.m_45786_((Entity)summoned) || !level2.m_7967_((Entity)summoned)) {
                formation.ready[role] = now + 20L;
                continue;
            }
            formation.crystals[role] = summoned;
        }
        MultiplayerCrystals.tick(boss);
    }

    public static void cancel(EasterBossEntity boss) {
        Level level = boss.m_9236_();
        if (level instanceof ServerLevel) {
            Formation formation;
            ServerLevel level2 = (ServerLevel)level;
            Map<UUID, Formation> forms = ACTIVE.get(level2);
            Formation formation2 = formation = forms == null ? null : forms.remove(boss.m_20148_());
            if (formation != null) {
                formation.close();
            }
        }
        TemporaryTriads.cancel(boss);
        MultiplayerCrystals.cancel(boss);
        CrossGuard.cancel(boss);
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        EasterBossEntity boss;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof EasterBossEntity && ((boss = (EasterBossEntity)livingEntity).getVariant() == 11 || QizaiCrystals.formation(boss) != null)) {
            QizaiCrystals.tick(boss);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void defend(LivingHurtEvent event) {
        EasterBossEntity boss;
        Object object;
        if (event.isCanceled() || !((object = event.getEntity()) instanceof EasterBossEntity) || QizaiCrystals.combatTarget(boss = (EasterBossEntity)object) == null || event.getSource().m_269533_(DamageTypeTags.f_268738_) || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0f) {
            return;
        }
        for (QizaiCrystalEntity crystal : QizaiCrystals.members(boss)) {
            if (crystal.role() != 2 || !(crystal.m_20280_((Entity)boss) <= 256.0)) continue;
            event.setAmount(event.getAmount() - CrossGuard.absorb(crystal.absorb(event.getAmount() * 0.35f), event.getAmount(), crystal));
            break;
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)livingEntity;
            QizaiCrystals.cancel(boss);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)entity;
            QizaiCrystals.cancel(boss);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        LevelEvent.Unload event;
        MultiplayerCrystals.unload(unload);
        Map<UUID, Formation> forms = ACTIVE.remove(event.getLevel());
        if (forms != null) {
            forms.values().forEach(Formation::close);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        MultiplayerCrystals.stop();
        ACTIVE.clear();
    }

    private static final class Formation {
        final QizaiCrystalEntity[] crystals = new QizaiCrystalEntity[3];
        final long[] ready = new long[3];
        LivingEntity target;
        int idle;

        private Formation() {
        }

        void close() {
            for (QizaiCrystalEntity crystal : this.crystals) {
                if (crystal == null) continue;
                crystal.m_146870_();
            }
        }
    }
}

