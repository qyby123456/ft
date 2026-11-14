/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ManorSkillTextQuiet;
import net.farmingtales.ftboss.boss.ai.ManorTargetPolicy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.EasterPearlEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.xueli_melee.MeleePolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ManorLivingTargets {
    private static final String NEXT_PEARL = "ftboss:AirbornePearlAfter";

    private ManorLivingTargets() {
    }

    private static Entity actor(Entity entity) {
        Entity entity2;
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            entity2 = projectile.m_19749_();
        } else {
            entity2 = entity;
        }
        return entity2;
    }

    public static boolean creative(LivingEntity livingEntity) {
        Player player;
        return livingEntity instanceof Player && (player = (Player)livingEntity).m_7500_();
    }

    public static void tell(LivingEntity livingEntity, Component component, boolean bl) {
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            ManorSkillTextQuiet.deliver(player, component, bl);
        }
    }

    public static boolean valid(Entity entity, LivingEntity livingEntity) {
        Entity entity2 = ManorLivingTargets.actor(entity);
        return entity2 != null && BossTargets.valid(livingEntity) && !livingEntity.m_5833_() && livingEntity != entity2 && !(livingEntity instanceof ArmorStand) && livingEntity.m_9236_() == entity2.m_9236_() && !entity2.m_7307_((Entity)livingEntity) && !livingEntity.m_7307_(entity2) && (!BossAggro.isBossSide(entity2) || !BossAggro.isBossSide((Entity)livingEntity));
    }

    public static boolean canHit(Entity entity, Entity entity2) {
        LivingEntity livingEntity;
        return entity2 instanceof LivingEntity && ManorLivingTargets.valid(entity, livingEntity = (LivingEntity)entity2);
    }

    public static List<LivingEntity> nearby(Level level, Entity entity) {
        Entity entity2 = ManorLivingTargets.actor(entity);
        if (entity2 == null || entity2.m_9236_() != level) {
            return List.of();
        }
        return level.m_6443_(LivingEntity.class, entity2.m_20191_().m_82400_(64.0), livingEntity -> {
            Mob mob;
            Mob mob2;
            return ManorLivingTargets.valid(entity2, livingEntity) && (livingEntity instanceof Player || entity2 instanceof Mob && ((mob2 = (Mob)entity2).m_5448_() == livingEntity || mob2.m_21188_() == livingEntity) || livingEntity instanceof Mob && (mob = (Mob)livingEntity).m_5448_() == entity2);
        });
    }

    public static LivingEntity resolve(Level level, UUID uUID) {
        Player player;
        ServerLevel serverLevel;
        if (uUID == null) {
            return null;
        }
        if (level instanceof ServerLevel) {
            serverLevel = (ServerLevel)level;
            player = serverLevel.m_8791_(uUID);
        } else {
            player = level.m_46003_(uUID);
        }
        Player player2 = player;
        return player2 instanceof LivingEntity ? (serverLevel = (LivingEntity)player2) : null;
    }

    public static LivingEntity flywheelTarget(Entity entity) {
        Mob mob;
        Entity entity2 = ManorLivingTargets.actor(entity);
        if (entity2 instanceof Mob && ManorLivingTargets.valid(entity2, (mob = (Mob)entity2).m_5448_()) && mob.m_5448_().m_20280_(entity2) <= 576.0) {
            return mob.m_5448_();
        }
        mob = null;
        double d = 576.0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby(entity.m_9236_(), entity)) {
            double d2 = livingEntity.m_20280_(entity2);
            if (!(d2 <= d)) continue;
            d = d2;
            mob = livingEntity;
        }
        return mob;
    }

    public static boolean airborne(LivingEntity livingEntity) {
        return livingEntity != null && !livingEntity.m_20096_() && !livingEntity.m_20069_() && !livingEntity.m_20077_() && !livingEntity.m_6147_();
    }

    public static boolean hasRanged(int n) {
        return n == 0 || n == 1 || n == 2 || n == 4 || n == 5 || n == 6 || n == 7 || n == 10 || n == 11;
    }

    public static ManorFiveCombatRules.Skill rangedFive(int n) {
        return switch (n) {
            case 0 -> ManorFiveCombatRules.Skill.YOUZI_SEAL;
            case 1 -> ManorFiveCombatRules.Skill.DANDAN_STAR;
            case 4 -> ManorFiveCombatRules.Skill.LEMON_ACID;
            case 7 -> ManorFiveCombatRules.Skill.SHADOW_CAGE;
            default -> null;
        };
    }

    public static ManorFiveCombatRules.Skill airborneFive(ManorFiveCombatRules.Skill skill, LivingEntity livingEntity, int n) {
        if (!ManorLivingTargets.airborne(livingEntity)) {
            return skill;
        }
        ManorFiveCombatRules.Skill skill2 = ManorLivingTargets.rangedFive(n);
        return skill2 == null ? skill : skill2;
    }

    public static boolean preferRanged(boolean bl, EasterBossEntity easterBossEntity) {
        return ManorTargetPolicy.preferRanged(MeleePolicy.preferRanged(bl || ManorLivingTargets.hasRanged(easterBossEntity.getVariant()) && ManorLivingTargets.airborne(easterBossEntity.m_5448_()), bl, easterBossEntity), bl, easterBossEntity);
    }

    public static int airborneSpell(int n, LivingEntity livingEntity) {
        return ManorLivingTargets.airborne(livingEntity) && n != 9 && n != 10 ? 1 : n;
    }

    public static boolean airbornePearl(EasterBossEntity easterBossEntity) {
        ServerLevel serverLevel;
        LivingEntity livingEntity;
        block7: {
            block6: {
                livingEntity = easterBossEntity.m_5448_();
                if (!ManorLivingTargets.airborne(livingEntity)) {
                    return false;
                }
                Level level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block6;
                serverLevel = (ServerLevel)level;
                if (ManorLivingTargets.valid((Entity)easterBossEntity, livingEntity) && !ManorLivingTargets.hasRanged(easterBossEntity.getVariant()) && easterBossEntity.getVariant() != 9 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && !easterBossEntity.m_21525_() && easterBossEntity.m_6084_() && !easterBossEntity.m_20147_() && !easterBossEntity.isPerformingSkill() && !ManorFight.isFinaleMember((LivingEntity)easterBossEntity) && !HiddenBossIntro.isActing(easterBossEntity.m_20148_()) && !(easterBossEntity.m_20280_((Entity)livingEntity) <= 4.0)) break block7;
            }
            return true;
        }
        long l = serverLevel.m_46467_();
        if (l < easterBossEntity.getPersistentData().m_128454_(NEXT_PEARL)) {
            return true;
        }
        EasterPearlEntity easterPearlEntity = new EasterPearlEntity((Level)serverLevel, (LivingEntity)easterBossEntity);
        Vec3 vec3 = easterBossEntity.m_146892_().m_82520_(0.0, -0.2, 0.0);
        Vec3 vec32 = livingEntity.m_146892_().m_82546_(vec3);
        double d = Math.max(0.1, vec32.m_82553_());
        easterPearlEntity.m_6034_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
        easterPearlEntity.m_6686_(vec32.f_82479_ / d, vec32.f_82480_ / d + 0.12, vec32.f_82481_ / d, 1.5f, 0.0f);
        ManorFightMembers.inherit((Entity)easterBossEntity, (Entity)easterPearlEntity);
        if (serverLevel.m_7967_((Entity)easterPearlEntity)) {
            easterBossEntity.getPersistentData().m_128356_(NEXT_PEARL, l + (long)easterBossEntity.getChallengeDifficulty().acceleratedTicks(100));
            easterBossEntity.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        }
        return true;
    }

    public static List nearby(Level level, Entity entity, AABB aABB) {
        Entity entity2 = ManorLivingTargets.actor(entity);
        if (entity2 == null || entity2.m_9236_() != level) {
            return List.of();
        }
        return level.m_6443_(LivingEntity.class, aABB, livingEntity -> {
            Mob mob;
            Mob mob2;
            return ManorLivingTargets.valid(entity2, livingEntity) && (livingEntity instanceof Player || entity2 instanceof Mob && ((mob2 = (Mob)entity2).m_5448_() == livingEntity || mob2.m_21188_() == livingEntity) || livingEntity instanceof Mob && (mob = (Mob)livingEntity).m_5448_() == entity2);
        });
    }
}

