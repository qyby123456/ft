package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 小酒桶(2026-09-11 按用户要求新增): 酒桶王的召唤物, **替换原来的烈焰人**。
 *
 * <p>模型/贴图/动画都是本模组自制({@code tools/model3d/gen_barrel_minion.cjs} 生成):
 * 桶身 + 两道铁箍 + 眼睛 + 酒嘴 + 两腿两臂, 5 段动画(idle / walk / attack / hurt / death)。
 * <p>AI 与小姜饼人同款(近战追击 + 随机游荡 + 盯人), 也就是"小僵尸式的近身攻击"。
 */
public class SmallBarrelEntity extends Monster implements GeoAnimatable {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.barrel_minion.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.barrel_minion.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.barrel_minion.attack");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.barrel_minion.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.barrel_minion.death");

    /** 一次性动画时长(tick): attack 0.5s / hurt 0.4s */
    private static final double T_ATTACK = 10.0, T_HURT = 8.0;

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    /** 引信: -1 = 未点燃; >=0 = 剩余 tick(点燃后 1.5 秒炸) */
    private int fuse = -1;
    /** 引爆半径(格) */
    private static final double TRIGGER_RANGE = 5.0;
    /** 引信时长(tick) */
    private static final int FUSE = 30;
    /** 爆炸威力(不毁地形, 只伤实体) */
    private static final float BLAST = 1.2f;            // 2.4 → ×0.5(v77)

    public SmallBarrelEntity(EntityType<? extends SmallBarrelEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_SPEED, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);   // 酒桶是硬的
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * 2026-09-11 按用户要求: 小酒桶的攻击方式改成**自爆**。
     * 贴近玩家(5 格内)点燃 1.5 秒引信 -> 炸开; 期间玩家跑远就熄灭引信。
     * 爆炸用 {@code ExplosionInteraction.NONE} = **不破坏地形**, 只伤实体。
     */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        net.minecraft.world.entity.LivingEntity t = this.getTarget();
        double dist = (t == null || !t.isAlive()) ? Double.MAX_VALUE : this.distanceTo(t);
        if (this.fuse < 0) {
            if (dist <= TRIGGER_RANGE) {
                this.fuse = FUSE;
                this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);   // 播挥击动作当"拔栓"
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sounds.SoundEvents.TNT_PRIMED, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 1.3f);
            }
        } else {
            if (dist > TRIGGER_RANGE + 4.0) { this.fuse = -1; return; }       // 玩家跑远 -> 熄灭
            if (this.fuse % 2 == 0) {
                net.farmingtales.ftboss.util.Fx.particles(this.level(), this.getX(), this.getY() + 0.8, this.getZ(),
                        2, net.minecraft.core.particles.ParticleTypes.SMOKE, 0.2, 0.02);
            }
            if (--this.fuse <= 0) this.detonate();
        }
    }

    /** 引爆: 只伤实体, 不毁地形 */
    private void detonate() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sl)) { this.discard(); return; }
        sl.explode(this, this.getX(), this.getY() + 0.4, this.getZ(), BLAST,
                net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.8f, 1.1f);
        net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][小酒桶] 自爆 @ {}, {}, {} (威力 {}, 不毁地形)",
                String.format("%.1f", this.getX()), String.format("%.1f", this.getY()), String.format("%.1f", this.getZ()), BLAST);
        this.discard();
    }

    /** 被打死时原地泼一小片酒(纯装饰) */
    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            net.farmingtales.ftboss.util.Fx.particles(this.level(), this.getX(), this.getY() + 0.5, this.getZ(),
                    16, net.minecraft.core.particles.ParticleTypes.SPLASH, 0.5, 0.05);
        }
    }

    // ---- GeckoLib ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "barrel_ctrl", 4, this::predicate));
    }

    /** 动画状态机: 死亡 > 受击 > 挥击 > 移动 > 待机 */
    private PlayState predicate(AnimationState<SmallBarrelEntity> state) {
        if (this.isDeadOrDying()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        if (playing(state, HURT, T_HURT)) return PlayState.CONTINUE;
        if (playing(state, ATTACK, T_ATTACK)) return PlayState.CONTINUE;
        if (this.hurtTime > 0) {
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        if (this.swinging) {
            state.setAnimation(ATTACK);
            return PlayState.CONTINUE;
        }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    private static boolean playing(AnimationState<SmallBarrelEntity> state, RawAnimation anim, double ticks) {
        return state.isCurrentAnimation(anim) && state.getAnimationTick() < ticks;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }
}
