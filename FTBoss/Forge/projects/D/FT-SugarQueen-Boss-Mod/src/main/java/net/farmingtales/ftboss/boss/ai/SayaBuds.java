package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.List;

/**
 * 沙耶(茶树古树)二阶段专属:「茶芽庇佑」。
 *
 * <p>进入二阶段时召唤 {@value #BUD_COUNT} 株 <b>守护茶芽</b>(沿用之前的小茶树模型 camellia_sprout),
 * 它们持续为沙耶输送茶心之力(再生/吸收/抗性)；
 * <b>只要还有一株存活, 沙耶完全免疫伤害</b>, 全部清空后护盾破裂、沙耶才可被击杀。
 * 全过程通过聊天栏 + 动作栏向附近玩家提示(含剩余株数)。</p>
 */
public final class SayaBuds {

    /** 守护茶芽数量(2026-09-10 加强沙耶: 4 → 5) */
    private static final int BUD_COUNT = 5;
    /** 判定范围(格) */
    private static final double SCAN = 64.0;
    /** 玩家提示范围(格) */
    private static final double PLAYER_RANGE = 48.0;
    /** 茶芽给自己灌 buff 的间隔(tick)(加强沙耶: 60 → 40) */
    private static final int BUFF_CD = 40;
    /** 动作栏提醒间隔(tick) */
    private static final int REMIND_CD = 100;
    /** 被打时提示的间隔(tick) */
    private static final int BLOCK_NOTIFY_CD = 40;

    private final net.minecraft.world.entity.Mob boss;
    private final java.util.function.Predicate<net.minecraft.world.entity.Entity> owned;

    private boolean shield = false;
    private int buffCd = BUFF_CD;
    private int remindCd = 20;
    private int blockCd = 0;

    public SayaBuds(TeaTreeBossEntity boss) {
        this(boss, summon -> SummonSweep.belongsTo(boss, summon));
    }

    public SayaBuds(net.minecraft.world.entity.Mob boss, java.util.function.Predicate<net.minecraft.world.entity.Entity> owned) {
        this.boss = boss;
        this.owned = owned;
    }

    /** 是否处于护盾状态(免伤中) */
    public boolean shielded() {
        return this.shield;
    }

    /** 存活守护茶芽数 */
    public int aliveBuds() {
        return collectBuds().size();
    }

    /** 进入二阶段: 召唤守护茶芽, 开启免伤护盾 */
    public void trigger() {
        this.shield = true;
        this.buffCd = 30;
        this.remindCd = 20;
        this.blockCd = 0;
        if (!(this.boss.level() instanceof ServerLevel sl)) return;

        int spawned = 0;
        for (int i = 0; i < BUD_COUNT; i++) {
            TeaMinionEntity bud = FTEntities.TEA_MINION.get().create(sl);
            if (bud == null) continue;
            double angle = (Math.PI * 2.0 / BUD_COUNT) * i + this.boss.getRandom().nextDouble() * 0.4;
            double r = 4.5 + this.boss.getRandom().nextDouble() * 2.5;
            bud.setPos(this.boss.getX() + Math.cos(angle) * r, this.boss.getY(),
                    this.boss.getZ() + Math.sin(angle) * r);
            bud.makeGuardBud();
            bud.setCustomName(Component.translatable("entity.ftboss.guard_bud"));
            if (this.boss.getTarget() != null) bud.setTarget(this.boss.getTarget());
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this.boss, bud);
            sl.addFreshEntity(bud);
            Fx.particles(sl, bud.getX(), bud.getY() + 0.7, bud.getZ(), 18, ParticleTypes.SPORE_BLOSSOM_AIR, 0.35, 0.03);
            Fx.particles(sl, bud.getX(), bud.getY() + 0.7, bud.getZ(), 10, ParticleTypes.HAPPY_VILLAGER, 0.3, 0.05);
            spawned++;
        }

        Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BEACON_POWER_SELECT, 1.5f, 0.7f);
        Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.CONDUIT_ACTIVATE, 1.0f, 1.1f);
        Fx.ring(sl, this.boss.getX(), this.boss.getY() + 0.2, this.boss.getZ(), 5.5, 40, ParticleTypes.HAPPY_VILLAGER, 0.02);
        broadcast("msg.ftboss.saya_shield", spawned);
        FTBossMod.LOGGER.info("[FTBoss] 沙耶 二阶段: 茶芽庇佑启动({} 株), 清空前完全免伤", spawned);
    }

    /** 每 tick 调用(服务端, 非小妖本体) */
    public void tick() {
        if (this.boss.isDeadOrDying()) return;

        if (!this.shield) {
            // 区块重载/重生后重新接管: 场上仍有守护茶芽 → 恢复免伤
            if (this.boss.level() instanceof ServerLevel && aliveBuds() > 0) {
                this.shield = true;
                FTBossMod.LOGGER.info("[FTBoss] 沙耶 茶芽庇佑重新接管({} 株)", aliveBuds());
            }
            return;
        }

        int alive = aliveBuds();
        if (alive <= 0) {
            this.shield = false;
            if (this.boss.level() instanceof ServerLevel sl) {
                Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BEACON_DEACTIVATE, 1.5f, 0.8f);
                Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.GLASS_BREAK, 1.4f, 0.7f);
                Fx.particles(sl, this.boss.getX(), this.boss.getY() + 1.4, this.boss.getZ(), 45, ParticleTypes.CRIT, 0.9, 0.12);
                broadcast("msg.ftboss.saya_shield_break");
            }
            FTBossMod.LOGGER.info("[FTBoss] 沙耶 守护茶芽尽毁, 庇佑破除");
            return;
        }

        // 护盾能量粒子
        if (this.boss.tickCount % 5 == 0) {
            Fx.dust(this.boss.level(), this.boss.getX(), this.boss.getY() + 1.1, this.boss.getZ(),
                    4, 0.20F, 0.75F, 0.45F, 1.0, 0.01);
        }
        if (--this.buffCd <= 0) {
            this.buffCd = BUFF_CD;
            channelBuffs();
        }
        if (this.blockCd > 0) this.blockCd--;
        if (--this.remindCd <= 0) {
            this.remindCd = REMIND_CD;
            remind(alive);
        }
    }

    /** 被攻击但处于护盾状态: 给攻击者一个动作栏反馈(限频) */
    public void onBlocked(DamageSource source) {
        if (this.blockCd > 0) return;
        this.blockCd = BLOCK_NOTIFY_CD;
        if (!(source.getEntity() instanceof ServerPlayer p)) return;
        p.displayClientMessage(Component.translatable("msg.ftboss.saya_shield_hit", aliveBuds()), true);
        Fx.sound(this.boss.level(), this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(),
                SoundEvents.SHIELD_BLOCK, 1.0f, 1.3f);
    }

    /** 二命重生/死亡清理: 移除守护茶芽并解除护盾 */
    public void reset() {
        this.shield = false;
        if (this.boss.level() instanceof ServerLevel) {
            for (TeaMinionEntity bud : collectBuds()) bud.discard();
        }
    }

    // ---- 内部 ----

    /** 守护茶芽为沙耶输送茶心之力(2026-09-10 深夜按用户要求"减少一点buff数量": 3 种 → 2 种, 去掉吸收) */
    private void channelBuffs() {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        grant(new MobEffectInstance(MobEffects.REGENERATION, 140, 1, false, false, true));
        grant(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 1, false, false, true));
        for (TeaMinionEntity bud : collectBuds()) {
            Fx.particles(sl, bud.getX(), bud.getY() + 0.7, bud.getZ(), 8, ParticleTypes.SPORE_BLOSSOM_AIR, 0.25, 0.02);
            Fx.particles(sl, bud.getX(), bud.getY() + 1.0, bud.getZ(), 5, ParticleTypes.HAPPY_VILLAGER, 0.25, 0.04);
        }
        Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.3f);
    }

    private void grant(MobEffectInstance effect) {
        if (this.boss instanceof net.farmingtales.ftboss.entity.EasterBossEntity lemon) lemon.lemonSacrifice().grant(effect);
        else this.boss.addEffect(effect);
    }

    private void remind(int alive) {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(PLAYER_RANGE))) {
            p.displayClientMessage(Component.translatable("msg.ftboss.saya_shield_remind", alive), true);
        }
    }

    private void broadcast(String key, Object... args) {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(PLAYER_RANGE))) {
            p.displayClientMessage(Component.translatable(key, args), false);
        }
    }

    private List<TeaMinionEntity> collectBuds() {
        List<TeaMinionEntity> out = new ArrayList<>();
        if (this.boss.level().isClientSide()) return out;
        for (TeaMinionEntity m : this.boss.level().getEntitiesOfClass(TeaMinionEntity.class,
                this.boss.getBoundingBox().inflate(SCAN))) {
            if (m.isAlive() && m.isGuardBud() && this.owned.test(m)) out.add(m);
        }
        return out;
    }
}
