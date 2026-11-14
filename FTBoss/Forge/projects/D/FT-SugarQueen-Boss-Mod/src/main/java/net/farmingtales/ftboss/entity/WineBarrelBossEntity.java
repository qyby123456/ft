package net.farmingtales.ftboss.entity;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BarrelTactics;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.farmingtales.ftboss.item.BarrelCrossbowItem;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 酒魔·酒桶王: 固定在原地的巨型酒桶Boss(Blockbench 模型), 自带紫色Boss条; 常驻葡萄酒效果 */
public class WineBarrelBossEntity extends AbstractSpellCastingMob implements net.farmingtales.ftboss.integ.spells.RegularBossCastHost {
    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        if (this.isCasting()) return;
        super.initiateCastSpell(spell, spellLevel);
        net.farmingtales.ftboss.integ.spells.BossSpellTuning.fasterLegacyCast(this, spell);
    }


    /**
     * 2026-09-11 按用户要求"去除 Boss 燃烧动画避免阻碍观察":
     * 着火时不再往模型上叠火焰图层(火焰伤害照旧), 免得模型/动画被挡住看不清。
     */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
    private static final double BOSS_BAR_RANGE = 48.0;
    private static final EntityDataAccessor<Integer> CROSSBOW_PULL_TICKS =
            SynchedEntityData.defineId(WineBarrelBossEntity.class, EntityDataSerializers.INT);
    private int previousCrossbowPullTicks;
    private static final EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_ACTION =
            SynchedEntityData.defineId(WineBarrelBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private static final EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_SIGNATURE_CAST =
            SynchedEntityData.defineId(WineBarrelBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState signatureCast =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_SIGNATURE_CAST);
    private static final int SLAM_TICKS = 24, SLAM_HIT_TICK = 10, SPRAY_TICKS = 50, SPRAY_HIT_TICK = 10;
    private int lastVisualState = ST_IDLE;
    private String pendingVolley = "";
    private UUID volleyTarget;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CROSSBOW_PULL_TICKS, 0);
        this.entityData.define(DATA_ACTION, new net.minecraft.nbt.CompoundTag());
        this.entityData.define(DATA_SIGNATURE_CAST, new net.minecraft.nbt.CompoundTag());
    }

    @Override
    public void startSignatureCast(net.farmingtales.ftboss.util.BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(net.farmingtales.ftboss.integ.spells.RegularBossCastHost.action(kind),
                net.farmingtales.ftboss.integ.spells.RegularBossCastHost.visualTicks(castTicks));
    }

    public float getCrossbowPull(float partialTick) {
        int current = this.entityData.get(CROSSBOW_PULL_TICKS);
        if (current == 0) return 0.0F;
        return Mth.lerp(partialTick, this.previousCrossbowPullTicks, current) / BarrelCrossbowItem.CHARGE_TICKS;
    }

    // 葡萄酒附加效果: vinery:armor_effect(护甲) + vinery:health_effect(生命)
    private MobEffect wineArmor;
    private MobEffect wineHealth;
    private MobEffect wineResist;
    private MobEffect wineLuck;
    private int auraTimer = 0;
    private int stage = 0;
    // 2026-09-10: 按用户要求"加强攻击性"——初始冷却整体下调(实际节奏在 tick 的重置处)
    private int summonCd = 150;
    private int castCd = 40;
    /** 火焰弹冷却(2026-09-10 按用户要求新增) */
    private int fireballCd = 60;
    /** 酒桶炮弹齐射冷却(2026-09-10 深夜按用户要求新增) */
    private int cannonCd = 140;
    private final List<AbstractSpell> spellQueue = new ArrayList<>();
    private final List<UUID> minions = new ArrayList<>();
    private final ServerBossEvent bossBar;
    /** 是否已向 BGM 调度器登记(2026-09-10: 酒桶王此前从未请求过音乐) */
    private boolean musicStarted = false;

    // ===== GeckoLib 动作状态机 =====
    private static final int ST_IDLE = 0, ST_WINDUP = 1, ST_CHARGE = 2, ST_SLAM = 3, ST_POP = 4;
    private int bState = ST_IDLE;
    private int bTimer = 0;
    private int chargeCd = 110;
    private int popCd = 80;
    private int mistCd = 0;
    private double chargeDx = 0, chargeDz = 0;

    /** 特效/命中编排组件(见 boss.ai.BarrelTactics) */
    private final BarrelTactics tactics = new BarrelTactics(this);

    private static final RawAnimation A_IDLE = RawAnimation.begin().thenLoop("animation.barrel_boss.idle");
    private static final RawAnimation A_WINDUP = RawAnimation.begin().thenPlay("animation.barrel_boss.windup");
    private static final RawAnimation A_ROLL = RawAnimation.begin().thenLoop("animation.barrel_boss.roll");
    private static final RawAnimation A_POP = RawAnimation.begin().thenPlay("animation.barrel_boss.spray_attack");
    private static final RawAnimation A_SLAM = RawAnimation.begin().thenPlay("animation.barrel_boss.slam");
    private static final RawAnimation A_HURT = RawAnimation.begin().thenPlay("animation.barrel_boss.hurt");
    private static final RawAnimation A_DEATH = RawAnimation.begin().thenPlay("animation.barrel_boss.death");
    private static final RawAnimation A_SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.barrel_boss.signature_cast");
    private static final RawAnimation A_SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.barrel_boss.signature_self_cast");

    public WineBarrelBossEntity(EntityType<? extends WineBarrelBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 80;
        this.bossBar = new ServerBossEvent(
                Component.translatable("boss.ftboss.wine_barrel"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 800.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)   // 固定不移动
                .add(Attributes.ATTACK_DAMAGE, 5.5)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19: 允许被击退
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        super.registerControllers(registrar);
        registrar.add(new AnimationController<>(this, "barrel_ctrl", 4, state -> {
            this.actionState.prepare(state);
            this.signatureCast.prepare(state);
            int visualState = this.actionState.active() ? Integer.parseInt(this.actionState.name()) : ST_IDLE;
            if (this.isAlive() && this.signatureCast.active()) {
                state.setAnimation("signature_self_cast".equals(this.signatureCast.name())
                        ? A_SIGNATURE_SELF : A_SIGNATURE_CAST);
                return software.bernie.geckolib.core.object.PlayState.CONTINUE;
            }
            if (this.isAlive() && visualState == ST_IDLE && this.isCasting()) {
                return software.bernie.geckolib.core.object.PlayState.STOP;
            }
            double spd = this.getDeltaMovement().horizontalDistanceSqr();
            if (this.isDeadOrDying()) {
                if (!state.isCurrentAnimation(A_DEATH)) state.setAnimation(A_DEATH);
            } else if (this.hurtTime > 2 && visualState == ST_IDLE && !this.isCasting()) {
                if (!state.isCurrentAnimation(A_HURT)) state.setAnimation(A_HURT);
            } else if (visualState == ST_WINDUP) {
                if (!state.isCurrentAnimation(A_WINDUP)) state.setAnimation(A_WINDUP);
            } else if (visualState == ST_CHARGE || (visualState == ST_IDLE && spd > 0.012)) {
                if (!state.isCurrentAnimation(A_ROLL)) state.setAnimation(A_ROLL);
            } else if (visualState == ST_SLAM) {
                if (!state.isCurrentAnimation(A_SLAM)) state.setAnimation(A_SLAM);
            } else if (visualState == ST_POP) {
                if (!state.isCurrentAnimation(A_POP)) state.setAnimation(A_POP);
            } else if (!state.isCurrentAnimation(A_IDLE)) {
                state.setAnimation(A_IDLE);
            }
            return software.bernie.geckolib.core.object.PlayState.CONTINUE;
        }));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.0, false));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 火焰主题: 受到火焰伤害时回复75%对应伤害
        // 传奇怪物式限伤: 单次上限 + 连击递减 + 反逃课距离
        amount = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
        if (amount <= 0F) return false;
        String mid = source.getMsgId();
        boolean fireHit = mid.contains("fire") || mid.contains("lava") || mid.contains("burn") || mid.contains("blaze");
        if (!this.level().isClientSide && fireHit && amount > 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(amount * 0.75f);
        }
        boolean hit = super.hurt(source, amount);
        if (hit) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
        if (hit) {
            this.bossBar.setVisible(true);
            if (!this.level().isClientSide) {
                // 受伤反馈: 木板断裂声
                this.level().playSound(null, this.getX(), this.getY() + 0.6, this.getZ(),
                        SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 0.9f, 0.6f);
            }
        }
        return hit;
    }

    @Override
    public void tick() {
        this.previousCrossbowPullTicks = this.entityData.get(CROSSBOW_PULL_TICKS);
        super.tick();
        if (!this.level().isClientSide) {
            net.farmingtales.ftboss.boss.ai.BossDamageGate.tick(this);
            net.farmingtales.ftboss.boss.ai.BossRage.tickBoss(this);
            net.farmingtales.ftboss.boss.ai.TrapBreaker.tick(this);
            net.farmingtales.ftboss.boss.ai.BossUnstuck.tick(this);
            tickPurge();
            tickWineAura();
            tickWineCombat();
            tickCrossbow();          // v108: 手里的酒桶弩 空弦 <-> 装填
            tickBarrelActions();
            if (this.bState != this.lastVisualState) {
                this.actionState.start(Integer.toString(this.bState), this.bTimer);
                this.lastVisualState = this.bState;
            }
            tickMusic();
        }
        if (!this.isAlive()) {
            BossBars.untrack(this, this.bossBar);
            return;
        }
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        boolean wantVisible = false;
        for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(BOSS_BAR_RANGE))) {
            if (this.distanceToSqr(p) < BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
                this.bossBar.addPlayer(p);
                wantVisible = true;
            } else {
                this.bossBar.removePlayer(p);
            }
        }
        if (this.bossBar.isVisible() != wantVisible) this.bossBar.setVisible(wantVisible);
    }

    private void tickPurge() {
        if (this.tickCount % 20 != 0) return;
        for (MobEffectInstance inst : new ArrayList<>(this.getActiveEffects())) {
            if (!inst.getEffect().isBeneficial()) this.removeEffect(inst.getEffect());
        }
    }

    /** 常驻葡萄酒buff: vinery:armor_effect + vinery:health_effect(缺mod自动跳过) */
    private void tickWineAura() {
        if (++this.auraTimer < 10) return;
        this.auraTimer = 0;
        if (this.wineArmor == null) this.wineArmor = ForgeRegistries.MOB_EFFECTS.getValue(
                new net.minecraft.resources.ResourceLocation("vinery", "armor_effect"));
        if (this.wineHealth == null) this.wineHealth = ForgeRegistries.MOB_EFFECTS.getValue(
                new net.minecraft.resources.ResourceLocation("vinery", "health_effect"));
        keep(this.wineArmor, 0);
        keep(this.wineHealth, 1);
    }

    private void keep(MobEffect fx, int amp) {
        if (fx == null) return;
        MobEffectInstance cur = this.getEffect(fx);
        if (cur == null || cur.getDuration() < 120) {
            this.addEffect(new MobEffectInstance(fx, 600, amp, false, false, true));
        }
    }

    /** 阶段酒buff: 2/3血与1/3血各进一阶段 -> 新增一种酒buff并使已有效果+1级 */
    private void applyStage() {
        float ratio = this.getHealth() / this.getMaxHealth();
        int want = ratio < 0.34 ? 2 : ratio < 0.67 ? 1 : 0;
        if (want <= this.stage) return;
        this.stage = want;
        ensureWineEffects();
        // 已有酒buff +1级
        MobEffect[] all = {this.wineArmor, this.wineHealth, this.wineResist, this.wineLuck};
        for (MobEffect fx : all) {
            if (fx != null) {
                MobEffectInstance cur = this.getEffect(fx);
                if (cur != null) {
                    this.addEffect(new MobEffectInstance(fx, 600, Math.min(4, cur.getAmplifier() + 1), false, false, true));
                }
            }
        }
        // 新增本阶段酒buff: stage=1加resist, stage=2加luck
        MobEffect next = want == 1 ? this.wineResist : this.wineLuck;
        if (next != null) {
            this.addEffect(new MobEffectInstance(next, 600, want - 1, false, false, true));
        }
        FTBossMod.LOGGER.info("[FTBoss] 酒魔·酒桶王 阶段{}: 酒buff升级+新buff", want);
    }

    private void ensureWineEffects() {
        if (this.wineArmor == null) this.wineArmor = find("armor_effect");
        if (this.wineHealth == null) this.wineHealth = find("health_effect");
        if (this.wineResist == null) this.wineResist = find("resistance_effect");
        if (this.wineLuck == null) this.wineLuck = find("luck_effect");
    }

    private MobEffect find(String id) {
        return ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation("vinery", id));
    }

    /** 【酒桶弩】齐射前的"上弦完成"预告时长(tick) —— 见 {@link #tickCrossbow()} */
    public static final int CROSSBOW_READY_LEAD = 20;
    /** 当前手里的弩是不是"装填完成"那一版(用来避免每 tick 都 setItemSlot 刷装备包) */
    private boolean crossbowReady = false;

    /**
     * 【酒桶弩】的外观状态机(2026-09-14 v108 按用户要求导入酒桶王的武器)。
     *
     * <p>平时手里是**空弦**那版模型; 距离下一次酒桶炮弹齐射 {@value #CROSSBOW_READY_LEAD} tick
     * (即 1 秒)以内时换成**装填完成(ready)**那一版 —— 玩家看到它"上弦"就知道要开炮了。
     * 只有状态变化(或手里没弩)时才 {@code setItemSlot}, 不会每 tick 刷装备同步包。
     */
    private void tickCrossbow() {
        int pullTicks = this.bState == ST_IDLE && this.isAlive()
                ? Mth.clamp(CROSSBOW_READY_LEAD + BarrelCrossbowItem.CHARGE_TICKS - this.cannonCd,
                        0, BarrelCrossbowItem.CHARGE_TICKS) : 0;
        this.entityData.set(CROSSBOW_PULL_TICKS, pullTicks);
        boolean wantReady = this.bState == ST_IDLE && this.cannonCd <= CROSSBOW_READY_LEAD;
        var hand = this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        boolean holding = hand.is(net.farmingtales.ftboss.item.FTBossItems.BARREL_CROSSBOW.get())
                || hand.is(net.farmingtales.ftboss.item.FTBossItems.BARREL_CROSSBOW_READY.get());
        if (wantReady == this.crossbowReady && holding) return;
        this.crossbowReady = wantReady;
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new net.minecraft.world.item.ItemStack(wantReady
                        ? net.farmingtales.ftboss.item.FTBossItems.BARREL_CROSSBOW_READY.get()
                        : net.farmingtales.ftboss.item.FTBossItems.BARREL_CROSSBOW.get()));
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0f);   // 弩不掉落(纯外观)
    }

    private void tickWineCombat() {
        if (this.isDeadOrDying()) return;
        applyStage();
        LivingEntity target = net.farmingtales.ftboss.boss.ai.BossTargets.combatTarget(this, 24);
        if (target == null) return;
        this.setTarget(target);
        this.tickDrunkenAura();
        if (this.fireballCd > 0) this.fireballCd--;
        if (this.cannonCd > 0) this.cannonCd--;
        if (this.summonCd > 0) this.summonCd--;
        tickIronMagic(target);
        if (this.bState != ST_IDLE) return;
        if (this.cannonCd <= 0) {
            this.cannonCd = this.stage >= 2 ? 110 : 170;
            startVolley("cannon", target);
            return;
        }
        if (this.summonCd <= 0) {
            this.summonCd = this.stage >= 1 ? 150 : 210;
            startVolley("summon", target);
            return;
        }
        if (this.fireballCd <= 0) {
            this.fireballCd = this.stage >= 2 ? 45 : 70;
            startVolley("fireball", target);
            return;
        }
    }

    private void tickIronMagic(LivingEntity target) {
        if (this.isCasting()) return;
        if (this.spellQueue.isEmpty()) {
            if (--this.castCd <= 0) {
                this.castCd = net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.acceleratedTicks(
                        this, (this.stage >= 2) ? 34 : 58);
                fillSpellQueue(target);
            }
        } else if (!((IMagicEntity) this).isCasting()) {
            castNextSpell(target);
        }
        // 火焰弹: 每 70t(二阶段 45t) 喷一颗火球
        // 酒桶炮弹齐射: 每 170t(二阶段 110t) 抛 3~5 颗会爆炸的酒桶(威力=闪电苦力怕)
        // 召唤: 烈焰人+啤酒人(少量), 同步Boss酒buff
    }

    private void tickDrunkenAura() {
        if (this.isNoAi() || !this.isAlive()) return;
        if (this.tickCount % 30 == 0) {
            for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(7))) {
                if (p.isAlive() && !p.isCreative() && !p.isSpectator()
                        && !this.isAlliedTo(p) && this.distanceToSqr(p) <= 49 && this.hasLineOfSight(p)) {
                    MobEffect drunk = ForgeRegistries.MOB_EFFECTS.getValue(
                            new net.minecraft.resources.ResourceLocation("brewery", "drunk"));
                    if (drunk != null) p.addEffect(new MobEffectInstance(drunk, 200, 0, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, false, false, true));
                    // 微醺(brewinandchewin:tipsy) V 级: 酒雾把人熏醉
                    MobEffect tipsy = ForgeRegistries.MOB_EFFECTS.getValue(
                            new net.minecraft.resources.ResourceLocation("brewinandchewin", "tipsy"));
                    if (tipsy != null) p.addEffect(new MobEffectInstance(tipsy, 240, 4, false, false, true));
                }
            }
        }
    }

    /**
     * 酒桶炮弹齐射(2026-09-10 深夜按用户要求新增): 一次抛 3~5 颗会爆炸的酒桶,
     * 走抛物线、落点带随机偏移; 爆炸威力 = 闪电苦力怕(6), 但不毁方块。
     */
    private void launchCannonballs(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl) || target == null || !target.isAlive()) return;
        int n = 3 + this.random.nextInt(3);   // 3~5 颗
        for (int i = 0; i < n; i++) {
            double offX = (this.random.nextDouble() - 0.5) * 7.0;
            double offZ = (this.random.nextDouble() - 0.5) * 7.0;
            double dx = (target.getX() + offX) - this.getX();
            double dz = (target.getZ() + offZ) - this.getZ();
            double dist = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            double dy = target.getEyeY() - (this.getY() + 1.8);
            double hSpeed = 1.05;
            double flight = dist / hSpeed;                       // 大约飞行 tick 数
            double vy = dy / flight + 0.5 * 0.045 * flight;      // 抛物线补偿
            // 发射口: 桶口朝目标方向偏一点
            double px = this.getX() + dx / dist * 2.2;
            double pz = this.getZ() + dz / dist * 2.2;
            var ball = new BarrelCannonballEntity(this.level(), this);
            ball.setPos(px, this.getY() + 1.9, pz);
            ball.setDeltaMovement(dx / dist * hSpeed, vy, dz / dist * hSpeed);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, ball);
            sl.addFreshEntity(ball);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 1.6f, 0.7f);
        part(this.getX(), this.getY() + 1.9, this.getZ(), 18, ParticleTypes.LARGE_SMOKE, 0.5, 0.05);
        FTBossMod.LOGGER.info("[FTBoss] 酒桶王炮弹齐射: {} 颗 (爆炸威力 = 闪电苦力怕 {})",
                n, BarrelCannonballEntity.EXPLOSION_POWER);
    }

    /** 火焰弹(iroin's 魔法火球, 不带爆破坏方块): 直击伤害随狂暴层级提高 */
    private void shootFireball(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        try {
            var f = new io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball(this.level(), this);
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - (this.getY() + 1.6);
            double dz = target.getZ() - this.getZ();
            var dir = new net.minecraft.world.phys.Vec3(dx, dy, dz).normalize();
            f.setPos(this.getX() + dir.x * 1.8, this.getY() + 1.6, this.getZ() + dir.z * 1.8);
            f.setDamage(11f + net.farmingtales.ftboss.boss.ai.BossRage.tierOf(this) * 4f);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, f);
            this.level().addFreshEntity(f);
            f.shoot(dir);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.2f, 0.85f);
            part(f.getX(), f.getY(), f.getZ(), 10, ParticleTypes.FLAME, 0.2, 0.02);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 酒桶王火焰弹异常: {}", t.toString());
        }
    }

    /** 高级火系法术连射: 取火系/岩浆系为主, 每次3-5连发不同法术; 二阶段起优先史诗/传说 */
    private void fillSpellQueue(LivingEntity target) {
        List<AbstractSpell> pool = new ArrayList<>();
        for (AbstractSpell s : WizardSummon.spellPool()) {
            if (WizardSummon.isFire(s)) pool.add(s);
        }
        if (pool.isEmpty()) return;
        // 狂暴(二阶段/三阶段) 时 70% 概率改从"史诗+传说"里挑
        int rage = net.farmingtales.ftboss.boss.ai.BossRage.tierOf(this);
        if (rage >= 1 && this.random.nextFloat() < 0.7f) {
            List<AbstractSpell> rare = new ArrayList<>();
            for (AbstractSpell s : pool) {
                if (WizardSummon.rarityOf(s, s.getMaxLevel()) >= 3) rare.add(s);   // 3=EPIC 4=LEGENDARY
            }
            if (!rare.isEmpty()) pool = rare;
        }
        int n = 3 + this.random.nextInt(3); // 3~5
        for (int i = 0; i < n && !pool.isEmpty(); i++) {
            this.spellQueue.add(pool.remove(this.random.nextInt(pool.size())));
        }
    }

    private void castNextSpell(LivingEntity target) {
        if (this.spellQueue.isEmpty() || target == null || !target.isAlive()) {
            this.spellQueue.clear();
            return;
        }
        AbstractSpell s = this.spellQueue.remove(0);
        if (!net.farmingtales.ftboss.integ.spells.BossSpellPacing.tryLegacyCast(this, s)) return;
        // 狂暴等级越高法术等级越高: 3 -> 4 -> 5 (2026-09-10 按用户要求)
        int lvl = Math.max(s.getMinLevel(), Math.min(4 + net.farmingtales.ftboss.boss.ai.BossRage.tierOf(this), s.getMaxLevel()));
        try {
            try {
                Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                Object inst = tdc.getConstructor(Player.class).newInstance(target);
                Class<?> mdCls = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.MagicData");
                Object md = mdCls.getMethod("getPlayerMagicData", net.minecraft.world.entity.LivingEntity.class)
                        .invoke(null, (net.minecraft.world.entity.LivingEntity) this);
                mdCls.getMethod("setAdditionalCastData", Class.forName("io.redspace.ironsspellbooks.api.spells.CastData"))
                        .invoke(md, inst);
            } catch (Throwable ignored) {
            }
            ((IMagicEntity) this).initiateCastSpell(s, lvl);
            FTBossMod.LOGGER.info("[FTBoss] 酒魔施法: {} lv{} 目标={}", s.getSpellId(), lvl,
                    target.getName().getString());
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 酒魔施法异常: {}", t.toString());
        }
    }

    private void summonMinions(LivingEntity target) {
        try {
            ensureWineEffects();
            // 2026-09-11 按用户要求: 用自制「小酒桶」生物替换原版烈焰人召唤
            for (int i = 0; i < 2 + this.random.nextInt(2); i++) {
                SmallBarrelEntity minion = FTEntities.BARREL_MINION.get().create(this.level());
                if (minion == null) continue;
                placeAndBoost(minion, target, "§6小酒桶");
            }
            // 少量啤酒人(Let's Do Brewery)
            try {
                EntityType<?> beer = ForgeRegistries.ENTITY_TYPES.getValue(
                        new net.minecraft.resources.ResourceLocation("brewery", "beer_elemental"));
                if (beer != null) {
                    EntityType<?> e = beer;
                    var raw = e.create(this.level());
                    if (raw instanceof Mob beerMob) {
                        placeAndBoost(beerMob, target, "§5啤酒人");
                    }
                }
            } catch (Throwable ignored) {
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 酒魔召唤异常: {}", t.toString());
        }
    }

    private void placeAndBoost(Mob m, LivingEntity target, String name) {
        double a = this.random.nextDouble() * Math.PI * 2;
        double r = 2.5 + this.random.nextDouble() * 3.5;
        m.setPos(this.getX() + Math.cos(a) * r, this.getY() + 0.5, this.getZ() + Math.sin(a) * r);
        m.setCustomName(Component.literal(name));
        m.setCustomNameVisible(true);
        m.setGlowingTag(true);
        if (target != null) m.setTarget(target);
        // 同步Boss酒buff
        ensureWineEffects();
        for (MobEffect fx : new MobEffect[]{this.wineArmor, this.wineHealth, this.wineResist, this.wineLuck}) {
            if (fx != null) {
                MobEffectInstance cur = this.getEffect(fx);
                if (cur != null) m.addEffect(new MobEffectInstance(fx, cur.getDuration(), cur.getAmplifier(), false, false, true));
            }
        }
        net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, m);
        this.level().addFreshEntity(m);
        this.minions.add(m.getUUID());
    }

    // ===== 动作 / 粒子 / 音效 =====
    private ServerLevel sl() {
        return this.level() instanceof ServerLevel s ? s : null;
    }

    private void fx(double x, double y, double z, int n, float r, float g, float b, double spread, double speed) {
        net.farmingtales.ftboss.util.Fx.dust(this.level(), x, y, z, n, r, g, b, spread, speed);
    }

    private void part(double x, double y, double z, int n, net.minecraft.core.particles.SimpleParticleType type,
                      double spread, double speed) {
        net.farmingtales.ftboss.util.Fx.particles(this.level(), x, y, z, n, type, spread, speed);
    }

    private void snd(net.minecraft.sounds.SoundEvent e, float vol, float pitch) {
        net.farmingtales.ftboss.util.Fx.sound(this.level(), this.getX(), this.getY() + 0.6, this.getZ(), e, vol, pitch);
    }

    /** 主动作状态机: 待机→蓄力→滚动冲撞→砸地; 待机→开塞喷射 */
    private void tickBarrelActions() {
        if (this.isDeadOrDying()) return;
        if (this.bState != ST_IDLE) {
            float yaw = (float) Math.toDegrees(Math.atan2(-this.chargeDx, this.chargeDz));
            this.setYRot(yaw);
            this.setYBodyRot(yaw);
            this.setYHeadRot(yaw);
        }
        if (--this.mistCd <= 0) { this.mistCd = 10; this.tactics.mist(); }
        switch (this.bState) {
            case ST_IDLE -> {
                LivingEntity t = this.tactics.nearestPlayer(22);
                if (this.chargeCd-- <= 0 && t != null) {
                    lockFacing(t);
                    this.bState = ST_WINDUP;
                    this.bTimer = 10;
                    snd(SoundEvents.WOOD_STEP, 1.1f, 0.55f);
                    part(this.getX(), this.getY() + 0.2, this.getZ(), 8, ParticleTypes.POOF, 0.7, 0.03);
                } else if (this.popCd-- <= 0 && t != null) {
                    lockFacing(t);
                    this.pendingVolley = "";
                    this.bState = ST_POP;
                    this.bTimer = 50;      // v112: 与 spray_attack 动画全长(50t = 2.5s)对齐(原 18 会把动画砍掉 2/3)
                }
            }
            case ST_WINDUP -> {
                if (this.bTimer % 2 == 0) this.tactics.warnCharge(this.chargeDx, this.chargeDz);
                if (--this.bTimer <= 0) {
                    this.bState = ST_CHARGE;
                    this.bTimer = 22;
                }
            }
            case ST_CHARGE -> {
                this.setDeltaMovement(this.chargeDx * 0.42, this.getDeltaMovement().y, this.chargeDz * 0.42);
                this.hasImpulse = true;
                if (this.tickCount % 3 == 0) this.tactics.rollTrail();
                if (this.tickCount % 6 == 0) snd(SoundEvents.WOOD_STEP, 1.5f, 0.45f);
                if (this.tickCount % 8 == 0) this.tactics.hitPlayers(2.9, 8.0f, 1.1);
                if (--this.bTimer <= 0 || this.horizontalCollision) {
                    this.bState = ST_SLAM;
                    this.bTimer = SLAM_TICKS;
                    this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                }
            }
            case ST_SLAM -> {
                if (this.bTimer > SLAM_TICKS - SLAM_HIT_TICK && this.bTimer % 2 == 0) this.tactics.warnSlam();
                if (this.bTimer == SLAM_TICKS - SLAM_HIT_TICK) this.tactics.slam();
                if (--this.bTimer <= 0) { this.bState = ST_IDLE; this.chargeCd = 130; }
            }
            case ST_POP -> {
                /* v112: 结算时机跟着新的 50t 总长平移 —— 原来是 bTimer=18 时的第 4 tick 结算, 现在 50-4=46 */
                if (this.bTimer == SPRAY_TICKS - SPRAY_HIT_TICK) releaseVolley();
                if (this.pendingVolley.isEmpty() && this.bTimer <= 40 && this.bTimer >= 10 && this.bTimer % 4 == 0) {
                    this.tactics.sprayTrail();
                }
                if (--this.bTimer <= 0) { this.bState = ST_IDLE; this.popCd = 100; }
            }
            default -> this.bState = ST_IDLE;
        }
    }

    private void lockFacing(LivingEntity target) {
        Vec3 direction = target.position().subtract(this.position()).multiply(1, 0, 1).normalize();
        this.chargeDx = direction.x;
        this.chargeDz = direction.z;
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    private void startVolley(String kind, LivingEntity target) {
        lockFacing(target);
        this.pendingVolley = kind;
        this.volleyTarget = target.getUUID();
        this.bState = ST_POP;
        this.bTimer = SPRAY_TICKS;
    }

    private void releaseVolley() {
        if (this.pendingVolley.isEmpty()) {
            this.tactics.corkSpray();
            return;
        }
        if (!(this.level() instanceof ServerLevel server)) return;
        if (!(server.getEntity(this.volleyTarget) instanceof LivingEntity target)
                || !net.farmingtales.ftboss.boss.ai.BossTargets.valid(target) || this.distanceToSqr(target) > 32 * 32) return;
        switch (this.pendingVolley) {
            case "cannon" -> launchCannonballs(target);
            case "fireball" -> shootFireball(target);
            case "summon" -> summonMinions(target);
            default -> { }
        }
    }

    // 特效与命中编排已移至 boss.ai.BarrelTactics(见 this.tactics)

    // ===== 战斗BGM: 酒桶王专属《醉乡酒馆》(2026-09-10 补: 此前从未请求过音乐) =====
    private void tickMusic() {
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
        BossBars.untrack(this, this.bossBar);
        this.stopMusic();
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            for (UUID u : this.minions) {
                var e = sl.getEntity(u);
                /* 2026-09-12 v58: 用 discard() 而不是 kill() —— kill() 走 hurt(), 会被限伤/无敌帧/死亡事件拦掉,
                   这正是别处"幽灵召唤物"的来源(屠夫的清场注释也写了这条) */
                if (e != null) e.discard();
            }
            /* v58 按用户反馈「召唤物死不干净」: 分身诱饵(不在 minions 名单里)也随主消散 */
            net.farmingtales.ftboss.boss.ai.SummonSweep.clear(this,
                    net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE,
                    net.farmingtales.ftboss.entity.WineBarrelDecoyEntity.class,
                    net.farmingtales.ftboss.entity.SmallBarrelEntity.class);
        }
        this.minions.clear();
        super.remove(reason);
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }
}
