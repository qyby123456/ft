package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.ButcherPhaseRules;
import net.farmingtales.ftboss.boss.ai.ButcherPhases;

import net.farmingtales.ftboss.boss.BossBars;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/**
 * 屠夫: 肉类近战Boss(套皮卫道士, 客户端用自制 ButcherBossModel 渲染)
 * 用卫道士而非猪灵蛮兵: 蛮兵在主世界会僵尸化; 卫道士无此问题
 * 行为/阶段/召唤由 boss.MeatBoss 静态大脑驱动(模板同 SugarQueenBoss)
 *
 * <p><b>2026-09-10 追加顶部 Boss 血条</b>：此前屠夫只有屏幕下方的 actionbar 血量提示，
 * 一旦实体没同步到客户端，玩家看到的就是"既没模型也没血条、再召唤还说已在战斗中"。
 * 现在与鼠群/沙耶/酒桶王一致：实体入场即登记 {@link ServerBossEvent}，
 * 「有没有血条」直接等价于「实体在不在场」，可一眼判断。
 */
public class MeatMountainEntity extends Vindicator {
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_ACTION =
            net.minecraft.network.syncher.SynchedEntityData.defineId(MeatMountainEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private final net.farmingtales.ftboss.boss.ai.ButcherCombat combat =
            new net.farmingtales.ftboss.boss.ai.ButcherCombat(this, this.actionState);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, new net.minecraft.nbt.CompoundTag());
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (target instanceof net.minecraft.server.level.ServerPlayer player
                && net.farmingtales.ftboss.boss.ai.BossTargets.valid(player)) setTarget(player);
        return false;
    }

    public String getButcherAction() {
        return this.actionState.name();
    }

    public float getButcherActionAge(float partialTick) {
        return this.actionState.elapsed(partialTick);
    }

    public boolean isButcherActing() {
        return this.actionState.active();
    }

    public void equipMeatLeg() {
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new net.minecraft.world.item.ItemStack(net.farmingtales.ftboss.item.FTBossItems.TURKEY_LEGS.get()));
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, net.minecraft.world.item.ItemStack.EMPTY);
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0f);
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.OFFHAND, 0f);
        this.setCanPickUpLoot(false);
        this.setLeftHanded(false);
    }

    /**
     * 2026-09-11 按用户要求"去除 Boss 燃烧动画避免阻碍观察":
     * 着火时不再往模型上叠火焰图层(火焰伤害照旧), 免得模型/动画被挡住看不清。
     */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    /** 血条显示半径(格) */
    private static final double BOSS_BAR_RANGE = 48.0;

    private final ServerBossEvent bossBar;
    /** 是否已向 BGM 调度器登记(2026-09-10: 屠夫此前**从未**请求过音乐, 三首新曲因此"像是没导入") */
    private boolean musicStarted = false;

    public MeatMountainEntity(EntityType<? extends MeatMountainEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.goalSelector.addGoal(0, this.combat);
        this.bossBar = new ServerBossEvent(
                Component.translatable("boss.ftboss.butcher"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
    }

    // ===== 战斗BGM: 屠夫专属《肉山祭坛》(与沙耶/姜饼王同一套全局调度, 只播一份) =====

    /** 开战后每 tick 向全局调度器登记(有玩家靠近才算开战) */
    private void tickMusic() {
        if (!net.farmingtales.ftboss.sound.FTSounds.MUSIC_ENABLED) return;
        if (!this.isAlive()) return;
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        if (!this.musicStarted) {
            if (sl.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class,
                    this.getBoundingBox().inflate(40.0)).isEmpty()) return;
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
        this.stopMusic();
        super.remove(reason);
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public int getButcherPhase() {
        if (this.level().isClientSide) {
            int phase = this.entityData.get(DATA_ACTION).getInt("visual_phase");
            return phase >= 1 && phase <= 3 ? phase : 1;
        }
        return ButcherPhases.phase(this);
    }

    public void syncButcherVisualPhase() {
        if (this.level().isClientSide) return;
        int phase = ButcherPhases.phase(this);
        var previous = this.entityData.get(DATA_ACTION);
        if (previous.getInt("visual_phase") == phase) return;
        var next = previous.copy();
        next.putInt("visual_phase", phase);
        this.entityData.set(DATA_ACTION, next);
    }

    public void resetButcherCombat() {
        this.combat.reset();
    }

    /** 统一清理: 隐藏 + 移除所有玩家(客户端收到 REMOVE 包才会销毁血条) */
    public void hideBossBar() {
        this.bossBar.setVisible(false);
        this.bossBar.removeAllPlayers();
    }

    /** 每 tick 维护血条: 进度 / 名称 / 附近玩家 / 死亡注销(与 RatBossEntity 同款) */
    private void tickBossBar() {
        if (!this.isAlive()) {
            BossBars.untrack(this, this.bossBar);
            return;
        }
        float hp = this.getHealth();
        float max = this.getMaxHealth();
        this.bossBar.setProgress(max <= 0F ? 0F : Math.max(0F, Math.min(1F, hp / max)));
        Component want = this.getCustomName() != null
                ? this.getCustomName()
                : Component.translatable("boss.ftboss.butcher");
        want = want.copy().append(Component.literal(" §7·阶段 " + getButcherPhase() + "/3"
                + (getButcherPhase() == ButcherPhaseRules.RECOVERY ? " ·剩余 " + ButcherPhases.remainingSeconds(this) + "秒" : "")));
        if (!want.getString().equals(this.bossBar.getName().getString())) {
            this.bossBar.setName(want);
        }
        boolean visible = false;
        for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(BOSS_BAR_RANGE))) {
            if (this.distanceToSqr(p) < BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
                this.bossBar.addPlayer(p);
                visible = true;
            } else {
                this.bossBar.removePlayer(p);
            }
        }
        if (this.bossBar.isVisible() != visible) this.bossBar.setVisible(visible);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // 传奇怪物式限伤: 单次上限 + 连击递减 + 反逃课距离
        float gated = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
        if (gated <= 0F) return false;
        if (!this.level().isClientSide && ButcherPhases.dodge(this, source)) return false;
        ButcherPhases.beginDamage(this, source);
        try {
            boolean hit = super.hurt(source, gated);
            if (hit) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
            return hit;
        } finally {
            ButcherPhases.endDamage(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (this.tickCount == 1 || this.tickCount % 20 == 0) {
                if (!getMainHandItem().is(net.farmingtales.ftboss.item.FTBossItems.TURKEY_LEGS.get())
                        || !getOffhandItem().isEmpty() || isLeftHanded()) equipMeatLeg();
            }
            this.limitChaseSpeed();
            ButcherPhases.tick(this);
            this.syncButcherVisualPhase();
            this.combat.update();
            if (this.cloneButcher && !this.cloneGoalsReady) initCloneButcher();
            net.farmingtales.ftboss.boss.ai.BossDamageGate.tick(this);
            net.farmingtales.ftboss.boss.ai.TrapBreaker.tick(this);
            net.farmingtales.ftboss.boss.ai.BossUnstuck.tick(this);
            net.farmingtales.ftboss.boss.ai.MinionBlessing.tick(this, 40);
            tickBossBar();
            tickMusic();
        }
        if (!level().isClientSide && tickCount % 20 == 0) {
            for (MobEffectInstance inst : new ArrayList<>(getActiveEffects())) {
                if (!inst.getEffect().isBeneficial()) removeEffect(inst.getEffect());
            }
        }
    }

    // ===== 克隆体(2026-09-11 按用户要求"允许玩家召唤同种多个boss"): 第二只起用原版近战 AI 自己打 =====
    private boolean cloneButcher = false;
    private boolean cloneGoalsReady = false;

    public void setCloneButcher(boolean v) {
        this.cloneButcher = v;
    }

    public boolean isCloneButcher() {
        return this.cloneButcher;
    }

    /** 克隆体: 补上近战/索敌 AI + 双持武器(本体由 MeatBoss 静态大脑驱动, 不走 AI) */
    private void initCloneButcher() {
        this.cloneGoalsReady = true;
        try {
            /* 2026-09-12 v66 防崩兜底: 近战 AI(MeleeAttackGoal) 每次挥拳都要读 ATTACK_DAMAGE;
               老存档里那些"用错构造器注册出来的屠夫"身上没有这个属性 → 会直接抛
               IllegalArgumentException 让服务端崩掉。所以先确认属性存在, 不在就只给"看着玩家"这类安全 goal。 */
            boolean canMelee = this.getAttribute(Attributes.ATTACK_DAMAGE) != null;
            if (canMelee) {
                this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this,
                        net.farmingtales.ftboss.boss.ai.ButcherCombatRules.chaseSpeed(false), true));
            } else {
                net.farmingtales.ftboss.FTBossMod.LOGGER.warn(
                        "[FTBoss] 屠夫复制体缺少 ATTACK_DAMAGE 属性(老存档实体) -> 不启用近战 AI, 避免崩服");
            }
            this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this,
                    net.minecraft.world.entity.player.Player.class, 24.0F));
            this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
            if (canMelee) {
                this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        this, net.minecraft.world.entity.player.Player.class, true));
            }
            this.equipMeatLeg();
            net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss] 屠夫复制体 AI 就绪(肉腿技能)");
        } catch (Throwable t) {
            net.farmingtales.ftboss.FTBossMod.LOGGER.warn("[FTBoss] 屠夫复制体 AI 初始化失败: {}", t.toString());
        }
    }

    private void limitChaseSpeed() {
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        var limitId = java.util.UUID.fromString("c08dc1b1-a4f5-447b-8d45-6d801bb09b54");
        speed.setBaseValue(net.farmingtales.ftboss.boss.ai.ButcherCombatRules.BASE_SPEED);
        var previous = speed.getModifier(limitId);
        double current = speed.getValue() / (previous == null ? 1.0 : 1.0 + previous.getAmount());
        double maximum = net.farmingtales.ftboss.boss.ai.ButcherCombatRules.MAX_SPEED;
        double adjustment = current > maximum ? maximum / current - 1 : 0;
        if (previous != null && Math.abs(previous.getAmount() - adjustment) < 1.0E-8) return;
        speed.removeModifier(limitId);
        if (adjustment < 0) speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                limitId, "ftboss_butcher_speed_limit", adjustment,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(BossWalkingSpeed.speed(this, speed));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, ButcherPhaseRules.BASE_HEALTH)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.ATTACK_DAMAGE, 5.5)
                .add(Attributes.ATTACK_SPEED, 1.15)
                .add(Attributes.MOVEMENT_SPEED, net.farmingtales.ftboss.boss.ai.ButcherCombatRules.BASE_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19: 允许被击退
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }
}
