package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.QizaiSkyNovaRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 祁崽 第八招「天陨星裁」的服务端演出 —— 2026-09-20 v189 按用户要求新增。
 *
 * <p>用户原话：「给祁崽做一个巨型铁魔法法术以及特效：飞上天空进入无法选中模式，
 * 蓄力5秒后，对半径48格的玩家造成500点魔法伤害」。
 *
 * <p><b>状态机</b>：{@code 起跳 RISE -> 蓄力 CHARGE(5 秒) -> 落地结算 IMPACT -> 收招 RECOVER -> 结束}。
 * 由 {@code boss/ai/QizaiMagic} 每 tick 驱动（它只在 {@code kind == QizaiRules.SKY_NOVA} 时
 * 把 tick 交给这里），所以**不需要自己挂事件**、也不会与其它技能抢状态。
 *
 * <p><b>「无法选中」怎么落地</b>（口径：完全无法选中也无法伤害）：
 * 起跳那一刻起 {@code setInvulnerable(true)} + 清空目标 + 每 tick 保持清空目标 ——
 * 于是玩家打不动它、它也不锁人。**落地结算前**立刻恢复，
 * 免得结算完还挂着无敌（那会变成"打完还能白嫖几秒"）。
 *
 * <p><b>伤害口径</b>：固定 {@value QizaiSkyNovaRules#DAMAGE} 点魔法伤害，
 * **不走** {@code manorSkillDamage}（那个会取 max(基础, 近战×倍率)，还会吃难度倍率）——
 * 用户要的是"就是 500"，所以直接 {@code hurt(间接魔法)}。
 */
public final class QizaiSkyNova {

    /* 阶段：0 空闲 / 1 起跳 / 2 蓄力 / 3 落地结算 / 4 收招 */
    private static final int IDLE = 0;
    private static final int RISE = 1;
    private static final int CHARGE = 2;
    private static final int IMPACT = 3;
    private static final int RECOVER = 4;

    private final EasterBossEntity boss;
    private int phase = IDLE;
    private int ticks;
    private int impacts;
    private int repeatTicks;
    /**
     * ★ 2026-09-21 用户要求「终结难度下祁崽法术施法速度翻倍，cd减半」，并选择"补齐例外"：
     * 本招的升空与蓄力原先**刻意不吃** {@code acceleratedTicks}（当初用户要的是确定的"蓄力 5 秒"），
     * 现在改成**也过难度** —— 普通难度仍是 20 + 100 tick，终结难度则是 10 + 50 tick。
     * 这两个字段存"本次演出实际用多少 tick"，状态机、cast 动作与 cue（客户端靠它算进度）全部读它们。
     */
    private int riseTicks = QizaiSkyNovaRules.RISE_TICKS;
    private int chargeTicks = QizaiSkyNovaRules.CHARGE_TICKS;
    /** 起跳前站的位置（落地后回到这里，免得悬在半空） */
    private Vec3 origin = Vec3.ZERO;
    /** 空中悬停点 */
    private Vec3 apex = Vec3.ZERO;
    private long castStarted;

    public QizaiSkyNova(EasterBossEntity boss) { this.boss = boss; }

    public boolean busy() { return this.phase != IDLE; }

    /** 起手。返回 false = 没起来（调用方保持原状态）。 */
    public boolean begin(ServerLevel level, LivingEntity target) {
        if (busy() || level != boss.level()) return false;
        if (boss.getVariant() != EasterBossEntity.QIZAI_VARIANT || !boss.isAlive() || boss.isMini() || boss.isGiant()) {
            return false;
        }
        this.origin = boss.position();
        this.castStarted = level.getGameTime();
        this.apex = this.origin.add(0, QizaiSkyNovaRules.RISE_HEIGHT, 0);
        this.phase = RISE;
        /* ★ 2026-09-21: 升空与蓄力也过难度（终结难度各减半），见字段注释。 */
        this.riseTicks = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiSkyNovaRules.RISE_TICKS));
        this.chargeTicks = Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(QizaiSkyNovaRules.CHARGE_TICKS));
        this.ticks = this.riseTicks;
        this.impacts = 0;
        this.repeatTicks = 0;
        /* 起跳就把"无法选中"挂上：无敌 + 不再索敌。 */
        boss.setInvulnerable(true);
        boss.setTarget(null);
        boss.getNavigation().stop();
        boss.playCast(this.riseTicks + this.chargeTicks, 0);
        cue(level, false);
        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.6F, 0.6F);
        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.4F, 0.7F);
        announce(level, "§5【天陨星裁】§7祁崽升上高空 —— §c快散开，它要来了！");
        FTBossMod.LOGGER.info("[FTBoss][Qizai] 「天陨星裁」起手: 悬停于 {} 格高, 升空 {} tick, 蓄力 {} tick",
                String.format("%.1f", QizaiSkyNovaRules.RISE_HEIGHT), this.riseTicks, this.chargeTicks);
        return true;
    }

    /**
     * 每 tick 驱动。返回 true = 还在演出中（调用方不要再跑别的技能逻辑）。
     */
    public boolean tick(ServerLevel level) {
        net.farmingtales.ftboss.balance.ManorExpansionHooks.nova(boss, level, origin, phase);
        if (phase == IDLE) return false;
        /* 演出途中本体没了/被移除 -> 直接收拾干净，别把无敌留在场上。 */
        if (!boss.isAlive() || boss.isRemoved()) { cancel(); return false; }
        /* 不可打断契约：直到完整收招前，每 tick 都重申保护，避免通用解冻/控制逻辑剥掉状态。 */
        boss.setInvulnerable(true);

        switch (phase) {
            case RISE -> {
                /* 平滑升空：按剩余比例插值，收尾时精确落到悬停点。 */
                double progress = 1.0 - (double) ticks / Math.max(1, riseTicks);
                Vec3 target = origin.add(0, QizaiSkyNovaRules.RISE_HEIGHT * progress, 0);
                boss.moveTo(target.x, target.y, target.z, boss.getYRot(), boss.getXRot());
                boss.setDeltaMovement(Vec3.ZERO);
                boss.hurtMarked = true;
                level.sendParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY(), boss.getZ(),
                        6, 0.5, 0.5, 0.5, 0.02);
                boss.setTarget(null);           // 保持"无法选中"
                if (--ticks <= 0) {
                    phase = CHARGE;
                    ticks = chargeTicks;
                    level.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                            SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.8F, 0.5F);
                }
            }
            case CHARGE -> {
                /* 钉在悬停点（重力/击退都不许把它拽下来），并持续维持"无法选中"。 */
                boss.moveTo(apex.x, apex.y, apex.z, boss.getYRot() + 6.0F, boss.getXRot());
                boss.setDeltaMovement(Vec3.ZERO);
                boss.hurtMarked = true;
                boss.setTarget(null);
                /* 蓄力期间靠 setInvulnerable(true) 挡伤害，血量不需要额外处理
                   （也不该在演出中途回血，否则"打空的血"会被这 5 秒奶回来）。 */
                /* 每 4 tick 补一次 cue（客户端视觉靠它取"开始时刻/总时长"） */
                if (ticks % 4 == 0) cue(level, false);
                /* 越接近落地，粒子越密、音调越紧 */
                double urgency = 1.0 - (double) ticks / Math.max(1, chargeTicks);
                int count = 4 + (int) (urgency * 14);
                level.sendParticles(ParticleTypes.ENCHANT, apex.x, apex.y, apex.z, count,
                        1.2 + urgency * 2.0, 1.2 + urgency * 2.0, 1.2 + urgency * 2.0, 0.35 + urgency * 0.5);
                level.sendParticles(ParticleTypes.END_ROD, apex.x, apex.y, apex.z, 2,
                        0.3 + urgency, 0.3 + urgency, 0.3 + urgency, 0.02);
                if (ticks % 10 == 0) {
                    level.playSound(null, apex.x, apex.y, apex.z, SoundEvents.AMETHYST_BLOCK_CHIME,
                            SoundSource.HOSTILE, 1.0F, (float) (0.8 + urgency * 1.4));
                }
                if (--ticks <= 0) {
                    phase = IMPACT;
                    ticks = QizaiSkyNovaRules.RECOVERY_TICKS + QizaiRules.repeatTicks();
                    impacts = 1;
                    impact(level);
                }
            }
            case IMPACT, RECOVER -> {
                if (impacts < QizaiRules.RELEASES && ++repeatTicks >= QizaiRules.REPEAT_INTERVAL) {
                    repeatTicks = 0;
                    impacts++;
                    impact(level);
                }
                if (--ticks <= 0) {
                    settle(level);
                    phase = IDLE;
                    boss.setQizaiCue(new CompoundTag());
                    return false;
                }
            }
            default -> { }
        }
        return true;
    }

    /** 落地：先解除"无法选中"，再按固定值结算半径 48 格的玩家。 */
    private void impact(ServerLevel level) {
        boss.setInvulnerable(true);
        /* 落回地面（结算中心按**地面**算，不然 48 格半径会以空中为球心，脚下反而打不到）。 */
        Vec3 landing = origin;
        boss.moveTo(landing.x, landing.y, landing.z, boss.getYRot(), boss.getXRot());
        boss.setDeltaMovement(Vec3.ZERO);
        boss.hurtMarked = true;

        cue(level, true);
        level.playSound(null, landing.x, landing.y, landing.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.2F, 0.5F);
        level.playSound(null, landing.x, landing.y, landing.z,
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.6F, 0.6F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, landing.x, landing.y + 0.5, landing.z, 4, 3, 0.5, 3, 0);
        level.sendParticles(ParticleTypes.FLASH, landing.x, landing.y + 1.0, landing.z, 3, 2, 0.5, 2, 0);

        int hits = 0;
        double radiusSq = QizaiSkyNovaRules.RADIUS * QizaiSkyNovaRules.RADIUS;
        for (Player player : level.players()) {
            if (!(player instanceof ServerPlayer sp) || !sp.isAlive() || sp.isRemoved()
                    || sp.isCreative() || sp.isSpectator()) continue;
            Vec3 delta = sp.position().subtract(landing);
            /* 水平半径按用户给的 48 格；垂直给足容差，免得站在坑里/塔上被漏掉。 */
            if (delta.horizontalDistanceSqr() > radiusSq) continue;
            if (Math.abs(delta.y) > QizaiSkyNovaRules.VERTICAL_TOLERANCE) continue;
            sp.invulnerableTime = 0;            // 必中，不被无敌帧吃掉
            sp.hurt(level.damageSources().indirectMagic(boss, boss), QizaiSkyNovaRules.DAMAGE);
            hits++;
        }
        announce(level, hits > 0
                ? "§5【天陨星裁】§7星裁落下 —— §c" + hits + " §7人被击中（" + (int) QizaiSkyNovaRules.DAMAGE + " 点魔法伤害）"
                : "§5【天陨星裁】§7星裁落空 —— §a所有人都在 " + (int) QizaiSkyNovaRules.RADIUS + " 格之外");
        FTBossMod.LOGGER.info("[FTBoss][Qizai] 「天陨星裁」落地结算: 命中 {} 人, 半径 {} 格, 固定 {} 点魔法伤害",
                hits, (int) QizaiSkyNovaRules.RADIUS, (int) QizaiSkyNovaRules.DAMAGE);
    }

    /** 收招：确保回到地面、无敌已解除、目标可重新锁定。 */
    private void settle(ServerLevel level) {
        boss.setInvulnerable(false);
        Vec3 landing = origin;
        boss.moveTo(landing.x, landing.y, landing.z, boss.getYRot(), boss.getXRot());
        boss.setDeltaMovement(Vec3.ZERO);
        boss.hurtMarked = true;
        level.sendParticles(ParticleTypes.CLOUD, landing.x, landing.y + 0.4, landing.z, 24, 1.2, 0.3, 1.2, 0.05);
    }

    /** 中断：任何异常路径都要保证"无敌不残留、本体回到地面"。 */
    public void cancel() {
        net.farmingtales.ftboss.balance.ManorExpansionHooks.nova(boss, null, origin, 0);
        if (phase == IDLE) return;
        boolean airborne = phase == RISE || phase == CHARGE;
        phase = IDLE;
        ticks = 0;
        impacts = 0;
        repeatTicks = 0;
        boss.setInvulnerable(false);
        if (airborne && boss.level() instanceof ServerLevel level) {
            /* 只把"悬停"这一个状态收拾掉；不做落点搜寻找，原地降回起跳高度即可。 */
            Vec3 landing = origin;
            boss.moveTo(landing.x, landing.y, landing.z, boss.getYRot(), boss.getXRot());
            boss.setDeltaMovement(Vec3.ZERO);
            boss.hurtMarked = true;
            boss.setQizaiCue(new CompoundTag());
            level.sendParticles(ParticleTypes.CLOUD, landing.x, landing.y + 0.4, landing.z, 16, 1.0, 0.3, 1.0, 0.04);
        }
    }

    /** 把演出状态写进 cue 给客户端渲染（沿用祁崽既有的那套 cue 通道）。 */
    private void cue(ServerLevel level, boolean released) {
        CompoundTag cue = new CompoundTag();
        cue.putInt("qizai", QizaiRules.SKY_NOVA);
        cue.putLong("start", released ? level.getGameTime() : castStarted);
        cue.putInt("duration", released ? QizaiSkyNovaRules.RECOVERY_TICKS
                : riseTicks + chargeTicks);
        cue.putBoolean("released", released);
        cue.putInt("volley", impacts);
        cue.putDouble("radius", QizaiSkyNovaRules.RADIUS);
        cue.putDouble("x", origin.x);
        cue.putDouble("y", origin.y);
        cue.putDouble("z", origin.z);
        cue.putDouble("apex", apex.y);
        cue.putInt("phase", phase);
        cue.putInt("left", Math.max(0, ticks));
        boss.setQizaiCue(cue);
    }

    private void announce(ServerLevel level, String text) {
        Component message = Component.literal(text);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(boss) <= (QizaiSkyNovaRules.RADIUS + 16) * (QizaiSkyNovaRules.RADIUS + 16)) {
                player.displayClientMessage(message, true);   // v182 口径：只进行动栏，不刷聊天栏
            }
        }
    }
}
