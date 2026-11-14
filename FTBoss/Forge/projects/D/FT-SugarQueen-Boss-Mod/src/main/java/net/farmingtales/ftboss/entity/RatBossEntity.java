package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 鼠群: 大老鼠Boss(GeckoLib big_rat 模型), 小老鼠为 small_rat(共用动画) */
public class RatBossEntity extends Monster implements GeoAnimatable {
    private static final net.minecraft.network.syncher.EntityDataAccessor<CompoundTag> DATA_ACTION =
            net.minecraft.network.syncher.SynchedEntityData.defineId(RatBossEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private final net.farmingtales.ftboss.boss.ai.BossMeleeWindup meleeWindup =
            new net.farmingtales.ftboss.boss.ai.BossMeleeWindup(this, this.actionState, 5, 10, 1.8);
    private boolean swarmPending;
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_MINION =
            net.minecraft.network.syncher.SynchedEntityData.defineId(RatBossEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, new CompoundTag());
        this.entityData.define(DATA_MINION, false);
    }
    /** 2026-09-12 按用户要求: 鼠群**每攻击到一个生物就回 1 滴血** */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (!this.isMinion()) return false;
        this.meleeWindup.start(target);
        return false;
    }

    private boolean biteTarget(net.minecraft.world.entity.LivingEntity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !this.level().isClientSide) this.heal(1.0f);
        return hit;
    }

    public void tickEncounterMelee() {
        this.meleeWindup.tick(this::biteTarget);
    }


    /**
     * 2026-09-11 按用户要求"去除 Boss 燃烧动画避免阻碍观察":
     * 着火时不再往模型上叠火焰图层(火焰伤害照旧), 免得模型/动画被挡住看不清。
     */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);
    private static final double BOSS_BAR_RANGE = 48.0;
    private static final String MINION_TAG = "rat_minion";
    private final ServerBossEvent bossBar;
    /** 是否已向 BGM 调度器登记(2026-09-10: 鼠群此前从未请求过音乐) */
    private boolean musicStarted = false;
    private int swarmCd = 60;
    private int cheeseCd = 40;
    private MobEffect cheeseEffect;
    private boolean cheeseLogged = false;

    /** 全服存活的大老鼠(主人)UUID: 小老鼠据此判定自己是不是残党, 不受距离限制 */
    private static final java.util.Set<UUID> LEADERS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public RatBossEntity(EntityType<? extends RatBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.bossBar = new ServerBossEvent(
                Component.translatable("boss.ftboss.rat_swarm"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
    }

    /** 统一清理: 隐藏 + 移除所有玩家(客户端收到 REMOVE 包才会销毁血条) */
    public void hideBossBar() {
        this.bossBar.setVisible(false);
        this.bossBar.removeAllPlayers();
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public boolean isMinion() {
        return this.entityData.get(DATA_MINION);
    }

    public void setMinion(boolean v) {
        this.entityData.set(DATA_MINION, v);
        var movement = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) movement.setBaseValue(v ? 0.36 : BossWalkingSpeed.BASE_SPEED);
        this.getPersistentData().putBoolean(MINION_TAG, v);
        this.refreshDimensions();   // 小老鼠尺寸不同, 标记变化后立即刷新碰撞箱
    }

    @Override
    public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_MINION.equals(accessor)) this.refreshDimensions();
    }

    /**
     * 尺寸分档: 大老鼠 0.6x0.975; 小老鼠只有模型的 0.25x0.56 格,
     * 沿用大老鼠的碰撞箱会导致"打到空气"(实测碰撞箱比模型大 2.4 倍), 这里按角色给尺寸。
     */
    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        return this.isMinion()
                ? net.minecraft.world.entity.EntityDimensions.scalable(0.4F, 0.6F)
                : net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 0.975F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(MINION_TAG, isMinion());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        // 2026-09-10: 补齐动画文件里原本从没被调用的 walk / jump / startle
        // (只剩 animation.rat.squeeze 未接: 那是"钻缝挤压"姿态, 当前玩法用不到, 留作备用)
        registrar.add(new AnimationController<>(this, "rat_move", 4, state -> {
            this.actionState.prepare(state);
            if (this.isDeadOrDying()) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.startle"));   // 无专用死亡动画, 用受惊收尾
                return PlayState.CONTINUE;
            }
            if ("melee".equals(this.actionState.name())) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.bite"));
                return PlayState.CONTINUE;
            }
            if ("call".equals(this.actionState.name())) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.call"));
                return PlayState.CONTINUE;
            }
            if (this.hurtTime > 0) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.rat.startle"));
                return PlayState.CONTINUE;
            }
            if (!this.onGround()) {
                state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.jump"));
                return PlayState.CONTINUE;
            }
            double spd = this.getDeltaMovement().horizontalDistanceSqr();
            if (spd > 0.02) state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.run"));
            else if (spd > 0.0008) state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.walk"));
            else state.setAnimation(RawAnimation.begin().thenLoop("animation.rat.idle"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setMinion(tag.getBoolean(MINION_TAG));
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(BossWalkingSpeed.speed(this, speed));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)   // 2026-09-12 v44 按用户澄清: 食物系恢复原值
                .add(Attributes.ARMOR, 2.5)
                .add(Attributes.ATTACK_DAMAGE, 4.5)
                .add(Attributes.ATTACK_SPEED, 1.6)      // 小僵尸式高速撕咬
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19: 统一 0.5
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true)); // 小僵尸式追击撕咬
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.9));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.blocksDamage(this, source, amount)) return false;
        // 传奇怪物式限伤: 单次上限 + 连击递减 + 反逃课距离(小老鼠由公共血池代扣)
        if (!this.isMinion()) {
            amount = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
            if (amount <= 0F) return false;
        }
        boolean hit = super.hurt(source, amount);
        if (hit && !this.isMinion()) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
        // 小老鼠绝不显示血条(它们的 ServerBossEvent 不参与显示, 只有大老鼠允许)
        if (hit && !this.isMinion()) this.bossBar.setVisible(true);
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.tick(this)) return;
        this.meleeWindup.tick(this::biteTarget);
        // 传奇怪物式反馈: 大老鼠才进入狂暴/限伤状态(小老鼠是公共血池的化身)
        if (!this.isMinion()) {
            net.farmingtales.ftboss.boss.ai.BossDamageGate.tick(this);
            net.farmingtales.ftboss.boss.ai.BossRage.tickBoss(this);
            net.farmingtales.ftboss.boss.ai.TrapBreaker.tick(this);
            net.farmingtales.ftboss.boss.ai.BossUnstuck.tick(this);
            net.farmingtales.ftboss.boss.ai.MinionBlessing.tick(this, 40);
            tickMusic();
        }
        if (this.isMinion()) {
            // 小老鼠: 无Boss条, 无debuff; 若主人已不在(死亡/被移除/卸载), 自动消散
            this.hideBossBar();
            if (this.tickCount % 20 == 0) {
                boolean hasLeader = net.farmingtales.ftboss.boss.ai.LemonSacrifice.ratLeader(this) != null;
                var it = LEADERS.iterator();
                while (it.hasNext()) {
                    UUID u = it.next();
                    net.minecraft.world.entity.Entity e = this.level() instanceof net.minecraft.server.level.ServerLevel sl
                            ? sl.getEntity(u) : null;
                    if (e == null) {
                        it.remove();
                    } else if (e instanceof RatBossEntity r && !r.isMinion() && r.isAlive()) {
                        hasLeader = true;
                    }
                }
                if (!hasLeader) { this.kill(); return; }
            }
            for (MobEffectInstance inst : new ArrayList<>(this.getActiveEffects())) {
                if (!inst.getEffect().isBeneficial()) this.removeEffect(inst.getEffect());
            }
            return;
        }
        if (this.tickCount % 20 == 0) {
            for (MobEffectInstance inst : new ArrayList<>(this.getActiveEffects())) {
                if (!inst.getEffect().isBeneficial()) this.removeEffect(inst.getEffect());
            }
        }
        if (!this.isAlive()) {
            BossBars.untrack(this, this.bossBar);
            return;
        }
        // 共享血条: 大老鼠血量即公共血池
        LEADERS.add(this.getUUID());
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        boolean want = false;
        for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(BOSS_BAR_RANGE))) {
            if (this.distanceToSqr(p) < BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
                this.bossBar.addPlayer(p);
                want = true;
            } else {
                this.bossBar.removePlayer(p);
            }
        }
        if (this.bossBar.isVisible() != want) this.bossBar.setVisible(want);
        // 召唤小老鼠 + 维护
        if (--this.swarmCd <= 0 && !this.actionState.active()) {
            this.swarmCd = (this.getHealth() < this.getMaxHealth() * 0.5) ? 65 : 100;
            this.actionState.start("call", 18);
            this.swarmPending = true;
        }
        if (this.swarmPending && this.actionState.elapsed(0) >= 9) {
            this.swarmPending = false;
            this.maintainSwarm();
            net.farmingtales.ftboss.util.Fx.ring(this.level(), this.getX(), this.getY() + 0.1,
                    this.getZ(), 3.0, 20, net.minecraft.core.particles.ParticleTypes.POOF, 0.02);
        }
        if (--this.cheeseCd <= 0) {
            this.cheeseCd = 160;   // 2026-09-11 自身 buff 减量: 间隔 80 -> 160
            this.applyCheeseBuff();
        }
    }

    /**
     * 维持鼠潮, 满血/同目标, 并回满漏网小老鼠。
     * <p>2026-09-10 按用户要求加强: 上限 3/5 → <b>6/9</b>, 每轮最多补 3 → <b>4</b> 只,
     * 且**每次召唤按召唤数量给首领回血**(每只 +3 HP, 即"召唤时回复血量")。
     */
    private void maintainSwarm() {
        int alive = 0;
        for (RatBossEntity r : this.level().getEntitiesOfClass(RatBossEntity.class,
                this.getBoundingBox().inflate(48))) {
            if (r.isMinion() && r.isAlive() && net.farmingtales.ftboss.boss.ai.SummonSweep.belongsTo(this, r)) {
                alive++;
                // 2026-09-10 按用户要求: 不再把小老鼠治满 —— 鼠群的"回血"只归首领(公共血池),
                // 小老鼠保持残血, 想回血只能靠首领重新召唤新的
                r.setTarget(this.getTarget());
                r.setPersistenceRequired();
            }
        }
        int cap = (this.getHealth() < this.getMaxHealth() * 0.5) ? 16 : 12; // 2026-09-12 按用户要求再提升: 12/8 -> 16/12
        int spawned = 0;
        for (int i = alive; i < cap && i < alive + 6; i++) {   // 2026-09-12: 每轮最多补 6 只
            RatBossEntity baby = (RatBossEntity) this.getType().create(this.level());
            if (baby == null) continue;
            double a = Math.random() * Math.PI * 2;
            double r = 2.5 + Math.random() * 4;
            baby.setMinion(true);
            net.farmingtales.ftboss.boss.ai.MinionBlessing.mark(baby, this.getUUID());   // v112: 带主人
            baby.setPos(this.getX() + Math.cos(a) * r, this.getY(), this.getZ() + Math.sin(a) * r);
            baby.setCustomName(Component.translatable("entity.ftboss.rat_minion"));
            baby.setCustomNameVisible(true);
            baby.setPersistenceRequired();
            baby.setHealth(baby.getMaxHealth());
            if (this.getTarget() != null) baby.setTarget(this.getTarget());
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, baby);
            this.level().addFreshEntity(baby);
            spawned++;
        }
        // 召唤回血: 每召唤一只回 3 点(封顶满血)
        if (spawned > 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(Math.min(spawned * 3.0f, this.getMaxHealth() - this.getHealth()));
        }
        FTBossMod.LOGGER.info("[FTBoss] 鼠群鼠潮: 在场小老鼠 {} 只 (本轮召唤 {} 只, 公共血池 {}/{})",
                alive + spawned, spawned, (int) this.getHealth(), (int) this.getMaxHealth());
    }

    /** 奶酪buff: 优先应用带 cheese 注册名的增益(奶酪mod), 否则只用**速度**(2026-09-11 按用户要求"自身buff减少": 去掉再生与吸收) */
    private void applyCheeseBuff() {
        if (this.cheeseEffect == null) {
            for (MobEffect fx : net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValues()) {
                var key = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(fx);
                if (key != null && key.getPath().toLowerCase().contains("cheese") && fx.isBeneficial()) {
                    this.cheeseEffect = fx;
                    break;
                }
            }
        }
        if (this.cheeseEffect != null) {
            this.addEffect(new MobEffectInstance(this.cheeseEffect, 400, 0, false, false, true));
            if (!this.cheeseLogged) {
                this.cheeseLogged = true;
                FTBossMod.LOGGER.info("[FTBoss] 鼠群奶酪buff: {}", net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(this.cheeseEffect));
            }
        } else {
            // 自身 buff 减量后只留速度(原来还挂再生+吸收)
            this.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 400, 0, false, false, true));
            if (!this.cheeseLogged) {
                this.cheeseLogged = true;
                FTBossMod.LOGGER.info("[FTBoss] 鼠群自身buff: 未找到奶酪效果, 仅使用 速度(2026-09-11 已按用户要求减量)");
            }
        }
    }

    /** 血池归零: 大老鼠死亡时小老鼠全部陪葬 */
    @Override
    public void die(DamageSource source) {
        net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.cleanup(this);
        BossBars.untrack(this, this.bossBar);
        LEADERS.remove(this.getUUID());
        if (!this.level().isClientSide) {
            for (RatBossEntity r : this.level().getEntitiesOfClass(RatBossEntity.class,
                    this.getBoundingBox().inflate(64))) {
                if (r.isMinion() && r.isAlive() && net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.legacyBelongsTo(this, r)) r.kill();
            }
        }
        super.die(source);
    }

    // ===== 战斗BGM: 鼠群专属《鼠潮夜行》 =====
    /** 只有大老鼠(首领)请求音乐; 小老鼠是同类型实体, 不能各自请求(否则会叠放) */
    private void tickMusic() {
        if (this.isMinion()) return;
        if (!net.farmingtales.ftboss.sound.FTSounds.MUSIC_ENABLED) return;
        if (!this.isAlive()) return;
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        if (!this.musicStarted) {
            if (sl.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(40.0)).isEmpty()) return;
            this.musicStarted = true;
        }
        net.farmingtales.ftboss.sound.BossMusic.request(sl, this);
    }

    /** 真死/被移除时退场(全部 Boss 都退场后音乐才停) */
    public void stopMusic() {
        if (!this.musicStarted) return;
        this.musicStarted = false;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            net.farmingtales.ftboss.sound.BossMusic.release(sl, this);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.cleanupRemoved(this, reason);
        BossBars.untrack(this, this.bossBar);
        this.stopMusic();
        LEADERS.remove(this.getUUID());
        // 大老鼠被移除(含 /kill、虚空、区块卸载)时也收拾残党
        if (!this.level().isClientSide && !this.isMinion()) {
            for (RatBossEntity r : this.level().getEntitiesOfClass(RatBossEntity.class,
                    this.getBoundingBox().inflate(64))) {
                if (r.isMinion() && r.isAlive() && net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.legacyBelongsTo(this, r)) r.kill();
            }
        }
        super.remove(reason);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }
}
