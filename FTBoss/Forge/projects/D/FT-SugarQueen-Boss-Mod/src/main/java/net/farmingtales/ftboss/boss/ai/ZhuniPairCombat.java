package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.turret.ZhuniStage;
import net.farmingtales.ftboss.turret.ZhuniTurretDrops;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public final class ZhuniPairCombat {
    private static final String XUANLAN_DEFEATED = "ftboss.zhuniPairXuanlanDefeated";
    private static final String ZHUNI_DEFEATED = "ftboss.zhuniPairBirdDefeated";

    private ZhuniPairCombat() {}

    public static boolean paired(EasterBossEntity boss) {
        return boss.getVariant() == 6 && ManorBalanceHooks.mainBoss(boss)
                && ManorFightMembers.ownerId(boss) == null && ManorFightMembers.fightId(boss) != null
                && !ManorFight.isFinaleMember(boss);
    }

    public static boolean xuanlanDefeated(EasterBossEntity boss) {
        return paired(boss) && boss.getPersistentData().getBoolean(XUANLAN_DEFEATED);
    }

    public static boolean zhuniDefeated(EasterBossEntity boss) {
        return paired(boss) && boss.getPersistentData().getBoolean(ZHUNI_DEFEATED);
    }

    public static boolean normalize(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity boss) || !paired(boss) || entity.level().isClientSide) return false;
        var maximum = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximum == null) return true;
        float ratio = boss.getMaxHealth() > 0 ? boss.getHealth() / boss.getMaxHealth() : 1;
        if (maximum.getBaseValue() != ZhuniPairRules.HEALTH) {
            maximum.setBaseValue(ZhuniPairRules.HEALTH);
            boss.setHealth(xuanlanDefeated(boss) ? 1 : ratio * boss.getMaxHealth());
        }
        boss.getPersistentData().putDouble("ftboss_base_hp", ZhuniPairRules.HEALTH);
        boss.getPersistentData().putDouble("ftboss_manor_health_balance", ZhuniPairRules.HEALTH);
        return true;
    }

    public static double maximumHealth(double ordinary, LivingEntity entity) {
        return entity instanceof EasterBossEntity boss && paired(boss)
                ? ZhuniPairRules.HEALTH : ManorRaidCombat.scaleMaximumHealth(ordinary, entity);
    }

    public static int flywheelLimit(EasterBossEntity boss) {
        return ZhuniPairRules.summons(zhuniDefeated(boss), ZhuniPairRules.FLYWHEELS);
    }

    public static boolean deferDeath(EasterBossEntity boss, DamageSource source) {
        if (!(boss.level() instanceof ServerLevel level) || !paired(boss) || zhuniDefeated(boss)
                || !ManorFight.hasActive(ManorFightMembers.fightId(boss))) return false;
        if (xuanlanDefeated(boss)) {
            boss.setHealth(1);
            return true;
        }
        boss.getPersistentData().putBoolean(XUANLAN_DEFEATED, true);
        boss.setHealth(1);
        boss.setTarget(null);
        boss.getNavigation().stop();
        boss.setDeltaMovement(Vec3.ZERO);
        boss.setNoAi(true);
        boss.setInvulnerable(true);
        boss.setInvisible(true);
        boss.setManorIntroHidden(true);
        boss.noPhysics = true;
        for (var entity : level.getAllEntities()) {
            if (entity instanceof FlywheelProjectileEntity wheel && wheel.getOwner() == boss) wheel.discard();
        }
        level.sendParticles(ParticleTypes.POOF, boss.getX(), boss.getY() + 1, boss.getZ(), 24, 0.4, 0.8, 0.4, 0.02);
        ZhuniTurretDrops.reinforce(boss, level);
        announce(boss, level, "§b玄蓝已倒下！§a竹鸟的炮塔部署数量翻倍。");
        ZhuniStage.syncBossBar(boss);
        return true;
    }

    public static void birdDefeated(EasterBossEntity boss, DamageSource source) {
        if (!paired(boss) || zhuniDefeated(boss)) return;
        boss.getPersistentData().putBoolean(ZHUNI_DEFEATED, true);
        if (xuanlanDefeated(boss)) {
            boss.setInvulnerable(false);
            boss.setHealth(0);
            boss.die(source);
        } else if (boss.level() instanceof ServerLevel level) {
            announce(boss, level, "§a竹鸟已倒下！§b玄蓝的飞轮数量翻倍。");
            replenish(boss, level);
            ZhuniStage.syncBossBar(boss);
        }
    }

    public static void replenish(EasterBossEntity boss, ServerLevel level) {
        if (!zhuniDefeated(boss) || xuanlanDefeated(boss) || !boss.isAlive() || boss.isManorIntroHidden()
                || boss.isNoAi() || boss.isInvisible() || !ManorFight.hasActive(ManorFightMembers.fightId(boss))) return;
        int existing = 0;
        for (var entity : level.getAllEntities()) {
            if (entity instanceof FlywheelProjectileEntity wheel && !wheel.isRemoved() && wheel.getOwner() == boss) existing++;
        }
        int limit = flywheelLimit(boss);
        for (int index = existing; index < limit; index++) {
            var wheel = new FlywheelProjectileEntity(level, boss);
            wheel.beginOrbit(Math.PI * 2 * index / limit);
            ManorFightMembers.inherit(boss, wheel);
            if (!level.addFreshEntity(wheel)) break;
        }
    }

    public static void recordBirdDamage(EasterBossEntity boss, float before, float after, DamageSource source) {
        if (!(boss.level() instanceof ServerLevel level) || !paired(boss)
                || !(source.getEntity() instanceof ServerPlayer player)) return;
        var fight = ManorFightMembers.fightId(boss);
        double damage = ManorRaidRules.acceptedDamage(before, after);
        if (!ManorFight.hasActive(fight) || !boss.getUUID().equals(ManorFight.currentBoss(fight)) || damage <= 0) return;
        var data = ManorContributionData.get(level.getServer());
        var ledger = data.observe(fight, boss.getUUID());
        ledger.recordKind(ManorAdaptiveShield.damageKind(source), damage);
        ledger.record(boss.getUUID(), player.getUUID(), damage);
        data.setDirty();
        ManorFight.participate(boss, player);
    }

    public static boolean turretNoAi(EasterBossEntity boss) { return boss.isNoAi() && !xuanlanDefeated(boss); }
    public static boolean turretInvulnerable(EasterBossEntity boss) { return boss.isInvulnerable() && !xuanlanDefeated(boss); }
    public static boolean turretInvisible(EasterBossEntity boss) { return boss.isInvisible() && !xuanlanDefeated(boss); }
    public static boolean turretIntroHidden(EasterBossEntity boss) { return boss.isManorIntroHidden() && !xuanlanDefeated(boss); }

    private static void announce(EasterBossEntity boss, ServerLevel level, String message) {
        for (var player : ManorBalanceHooks.players(boss, level)) player.sendSystemMessage(Component.literal(message));
    }
}
