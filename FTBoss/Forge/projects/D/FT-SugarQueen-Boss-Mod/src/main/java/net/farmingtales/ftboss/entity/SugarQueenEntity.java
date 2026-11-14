package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.farmingtales.ftboss.boss.BossBars;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.ArrayList;

/** 糖霜女皇: 继承铁魔法施法实体基类 -> 获得 MagicData/施法驱动, 本体可直接施铁魔法法术 */
public class SugarQueenEntity extends AbstractSpellCastingMob implements net.farmingtales.ftboss.integ.spells.RegularBossCastHost {
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_ACTION =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SugarQueenEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_SIGNATURE_CAST =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SugarQueenEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState signatureCast =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_SIGNATURE_CAST);
    private final net.farmingtales.ftboss.boss.ai.BossMeleeWindup meleeWindup =
            new net.farmingtales.ftboss.boss.ai.BossMeleeWindup(this, this.actionState, 5, 14, 3.2);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, new net.minecraft.nbt.CompoundTag());
        this.entityData.define(DATA_SIGNATURE_CAST, new net.minecraft.nbt.CompoundTag());
    }

    @Override
    public void startSignatureCast(net.farmingtales.ftboss.util.BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(net.farmingtales.ftboss.integ.spells.RegularBossCastHost.action(kind),
                net.farmingtales.ftboss.integ.spells.RegularBossCastHost.visualTicks(castTicks));
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        this.meleeWindup.start(target);
        return false;
    }

    @Override
    public void initiateCastSpell(io.redspace.ironsspellbooks.api.spells.AbstractSpell spell, int spellLevel) {
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

    // ===== 整合包外来效果(由 Farm and Charm / Brewin' and Chewin' 提供, 运行时查找, 不硬依赖) =====
    /** farm_and_charm:sweets -> "甜蜜": 提升移速/攻速/攻击力 */
    private static final ResourceLocation SWEETS_ID = new ResourceLocation("farm_and_charm", "sweets");
    /** brewinandchewin:sweet_heart -> "甜蜜之心": 基于饱和度的额外治疗 */
    private static final ResourceLocation SWEET_HEART_ID = new ResourceLocation("brewinandchewin", "sweet_heart");

    private static MobEffect cachedSweets;
    private static MobEffect cachedSweetHeart;
    private static boolean effectsWarned = false;
    private boolean auraLogged = false;
    /** 战斗BGM 是否已开始(避免重复播放) */
    private boolean musicStarted = false;

    private int auraCounter = 0;

    // ===== 克隆体(2026-09-11 按用户要求"允许玩家召唤同种多个boss"): 第二只起用简化 AI 自己打 =====
    /** 是否克隆体(由 SugarQueenBoss 在额外召唤时打标) */
    private boolean cloneQueen = false;
    private ServerBossEvent cloneBar;
    /**
     * 【本体血条】(2026-09-14 v110 修复)。
     *
     * <p>以前**只有克隆体**建条(`tickCloneBrain` 里的 {@code cloneBar}), 本体一条都没有 —— 后果不只是
     * "看不见血条": {@link net.farmingtales.ftboss.boss.BossBars#forEach} 只遍历**登记过血条的实体**,
     * 而 `BossHandler` 正是在那个 forEach 里跑 {@code BossScaling.tick}(按场上人数 1.25^(n-1) 缩放血量)
     * 与 {@code BossEmpower.tick}(+25% 移速/+30% 攻击/+4 护甲/+0.3 击退抗性) ——
     * 于是糖霜女皇本体一直是"裸装"上阵, 和其它 7 位主 Boss 待遇不一致。
     *
     * <p>现在和克隆体对称: **懒创建 + 自己 tick**(见 {@link #tickBossBar()}), 40 格内玩家可见。
     * 想退回旧行为: 注释掉 {@code tickBossBar()} 里的 {@code BossBars.track} 那一行即可。
     */
    private ServerBossEvent bossBar;
    /** 本体血条的可见半径(格) */
    private static final double BAR_RANGE = 48.0;
    private int cloneCastCd = 40;
    private int cloneMeleeCd = 0;

    public void setCloneQueen(boolean v) {
        this.cloneQueen = v;
    }

    public boolean isCloneQueen() {
        return this.cloneQueen;
    }

    public SugarQueenEntity(EntityType<? extends SugarQueenEntity> type, Level level) {
        super(type, level);
    }

    // ===== GeckoLib 动画: 姜饼人国王(覆盖施法实体的默认控制器) =====
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.king.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.king.walk");
    private static final RawAnimation ANIM_CAST = RawAnimation.begin().thenPlay("animation.king.scepter_raise").thenLoop("long_cast");
    private static final RawAnimation ANIM_SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.king.signature_cast");
    private static final RawAnimation ANIM_SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.king.signature_self_cast");
    private static final RawAnimation ANIM_HURT = RawAnimation.begin().thenPlay("animation.king.hurt");
    /** 2026-09-10 补线: 权杖砸击 + 倒下(动画文件里一直有, 之前没人调用) */
    private static final RawAnimation ANIM_SLAM = RawAnimation.begin().thenPlay("animation.king.scepter_slam");
    private static final RawAnimation ANIM_DEATH = RawAnimation.begin().thenPlay("animation.king.death");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "gingerbread_ctrl", 6, state -> {
            this.actionState.prepare(state);
            this.signatureCast.prepare(state);
            if (this.isDeadOrDying()) {
                state.setAnimation(ANIM_DEATH);                       // 1.8s 倒下; 死亡时间同款, 不会被腰斩
            } else if (this.signatureCast.active()) {
                state.setAnimation("signature_self_cast".equals(this.signatureCast.name())
                        ? ANIM_SIGNATURE_SELF : ANIM_SIGNATURE_CAST);
            } else if ("melee".equals(this.actionState.name())) {
                state.setAnimation(ANIM_SLAM);                        // 权杖砸击(近身挥击时)
            } else if (this.isCasting()) {
                if (!state.isCurrentAnimation(ANIM_CAST)) state.setAnimation(ANIM_CAST);
            } else if (this.hurtTime > 2) {
                if (!state.isCurrentAnimation(ANIM_HURT)) state.setAnimation(ANIM_HURT);
            } else if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0008) {
                if (!state.isCurrentAnimation(ANIM_WALK)) state.setAnimation(ANIM_WALK);
            } else if (!state.isCurrentAnimation(ANIM_IDLE)) {
                state.setAnimation(ANIM_IDLE);
            }
            return software.bernie.geckolib.core.object.PlayState.CONTINUE;
        }));
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // 传奇怪物式限伤: 单次上限 + 连击递减 + 反逃课距离
        float gated = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
        if (gated <= 0F) return false;
        boolean hit = super.hurt(source, gated);
        if (hit) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            this.meleeWindup.tick(super::doHurtTarget);
            net.farmingtales.ftboss.boss.ai.BossDamageGate.tick(this);
            net.farmingtales.ftboss.boss.ai.BossRage.tickBoss(this);
            net.farmingtales.ftboss.boss.ai.TrapBreaker.tick(this);
            net.farmingtales.ftboss.boss.ai.BossUnstuck.tick(this);
            net.farmingtales.ftboss.boss.ai.MinionBlessing.tick(this, 40);
            tickSweetAura();
            tickPurgeDebuffs();
            tickMusic();
            if (this.cloneQueen) tickCloneBrain();
            else tickBossBar();                 // v110: 本体自己的血条(克隆体走 tickCloneBrain 里的那条)
        }
    }

    /** 本体血条: 懒创建 + 进度 + 按距离增删观众(克隆体不建, 它们有自己的 cloneBar) */
    private void tickBossBar() {
        if (this.bossBar == null) {
            this.bossBar = new ServerBossEvent(Component.translatable("boss.ftboss.sugar_queen"),
                    BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);
            this.bossBar.setVisible(true);
            net.farmingtales.ftboss.boss.BossBars.track(this, this.bossBar);
        }
        if (!this.isAlive()) {
            net.farmingtales.ftboss.boss.BossBars.untrack(this, this.bossBar);
            this.bossBar = null;
            return;
        }
        this.bossBar.setProgress(Math.max(0f, Math.min(1f, this.getHealth() / Math.max(1f, this.getMaxHealth()))));
        boolean want = false;
        for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(BAR_RANGE))) {
            if (p.isAlive() && this.distanceToSqr(p) < BAR_RANGE * BAR_RANGE) {
                this.bossBar.addPlayer(p);
                want = true;
            } else {
                this.bossBar.removePlayer(p);
            }
        }
        if (this.bossBar.isVisible() != want) this.bossBar.setVisible(want);
    }

    /**
     * 克隆体的简化 AI(2026-09-11): 悬停接近最近玩家 → 周期性随机施法 → 贴身挥杖。
     * 剧情化的波次召唤/图腾/冲刺只属于"本体"(由 {@code SugarQueenBoss} 静态大脑驱动)。
     */
    private void tickCloneBrain() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        // Boss 条(每个克隆体自己的)
        if (this.cloneBar == null) {
            this.cloneBar = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PINK,
                    BossEvent.BossBarOverlay.PROGRESS);
            BossBars.track(this, this.cloneBar);
            this.cloneBar.setVisible(true);
        }
        if (!this.isAlive()) {
            BossBars.untrack(this, this.cloneBar);
            return;
        }
        this.cloneBar.setProgress(this.getHealth() / this.getMaxHealth());

        net.minecraft.world.entity.LivingEntity target = net.farmingtales.ftboss.boss.ai.BossTargets.combatTarget(this, 32);
        if (target == null) return;
        this.setTarget(target);
        // 悬停接近(目标上方 2.2 格)
        double dx = target.getX() - this.getX();
        double dy = (target.getY() + 2.2) - this.getY();
        double dz = target.getZ() - this.getZ();
        double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dl > 2.0) {
            double step = Math.min(0.25, dl);
            net.farmingtales.ftboss.util.BossCollision.move(this,
                    new Vec3(dx / dl * step, dy / dl * step, dz / dl * step));
        }
        this.getLookControl().setLookAt(target, 30f, 30f);
        // 施法
        if (--this.cloneCastCd <= 0 && !this.isCasting()) {
            this.cloneCastCd = net.farmingtales.ftboss.boss.NonManorFinaleDifficulty.acceleratedTicks(
                    this, 70 + this.getRandom().nextInt(40));
            net.farmingtales.ftboss.integ.WizardSummon.castRandom(
                    (io.redspace.ironsspellbooks.api.entity.IMagicEntity) this, target, this.getRandom(),
                    3 + net.farmingtales.ftboss.boss.ai.BossRage.tierOf(this));
        }
        // 贴身挥杖
        this.cloneMeleeCd--;
        if (this.cloneMeleeCd <= 0 && this.distanceTo(target) < 3.0) {
            this.cloneMeleeCd = 30;
            this.doHurtTarget(target);
        }
    }

    // ===== 战斗BGM: 糖霜女皇(姜饼人国王外观), 与茶树共用同一首曲子(全局调度只播一份) =====
    /** 战斗BGM: 开战后每 tick 向全局调度器登记(调度器按血量推进三幕剧乐幕) */
    private void tickMusic() {
        if (!net.farmingtales.ftboss.sound.FTSounds.MUSIC_ENABLED) return;
        if (!this.isAlive()) return;   // 死亡动画期间 tick 仍在跑, 不能再请求音乐
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (!this.musicStarted) {
            // 有玩家靠近才算开战
            if (sl.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(40.0)).isEmpty()) return;
            this.musicStarted = true;
        }
        net.farmingtales.ftboss.sound.BossMusic.request(sl, this);
    }

    /** 停止BGM: 真死/被移除时调用(全部 Boss 退场后音乐才停) */
    public void stopMusic() {
        if (!this.musicStarted) return;
        this.musicStarted = false;
        if (this.level() instanceof ServerLevel sl) {
            net.farmingtales.ftboss.sound.BossMusic.release(sl, this);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.stopMusic();
        super.remove(reason);
    }

    /** 每 10 tick 补一次常驻 buff(持续 600t=30s, 剩余 <120t 即续) */
    private void tickSweetAura() {
        if (++auraCounter < 10) return;
        auraCounter = 0;
        if (cachedSweets == null) cachedSweets = ForgeRegistries.MOB_EFFECTS.getValue(SWEETS_ID);
        if (cachedSweetHeart == null) cachedSweetHeart = ForgeRegistries.MOB_EFFECTS.getValue(SWEET_HEART_ID);
        if (cachedSweets == null || cachedSweetHeart == null) {
            if (!effectsWarned) {
                effectsWarned = true;
                FTBossMod.LOGGER.warn("[FTBoss] 找不到外来效果(缺 mod?): sweets={}, sweet_heart={}", cachedSweets, cachedSweetHeart);
            }
            return;
        }
        effectsWarned = false;
        keepEffect(cachedSweets, 2);    // 甜蜜 III 级(amp=2)
        keepEffect(cachedSweetHeart, 0); // 甜蜜之心 I 级(amp=0)
        if (!auraLogged) {
            auraLogged = true;
            FTBossMod.LOGGER.info("[FTBoss] 糖霜女皇已获得常驻效果: 甜蜜(farm_and_charm:sweets) + 甜蜜之心(brewinandchewin:sweet_heart), 负面效果免疫开启");
        }
    }

    private void keepEffect(MobEffect fx, int amp) {
        MobEffectInstance cur = getEffect(fx);
        if (cur == null || cur.getDuration() < 120) {
            addEffect(new MobEffectInstance(fx, 600, amp, false, false, true));
        }
    }

    /** 每 20 tick 清扫残留的负面效果(双保险: 进场前已有/漏网之鱼也清掉; 新增拦截走 Forge 事件) */
    private void tickPurgeDebuffs() {
        if (tickCount % 20 != 0) return;
        for (MobEffectInstance inst : new ArrayList<>(getActiveEffects())) {
            if (!inst.getEffect().isBeneficial()) {
                removeEffect(inst.getEffect());
            }
        }
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(BossWalkingSpeed.speed(this, speed));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 720.0)
                .add(Attributes.ARMOR, 2.5)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19: 统一 0.5
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }
}
