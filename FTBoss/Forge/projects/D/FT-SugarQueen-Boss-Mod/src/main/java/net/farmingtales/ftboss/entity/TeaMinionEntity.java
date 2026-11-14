package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
 * 茶芽小妖(茶树古树召唤物): GeckoLib 山茶花芽模型(camellia_sprout)
 * 玩法: 贴身骚扰玩家, 不显示Boss条, 免疫负面效果
 *
 * <p><b>2026-09-10 动画完善</b>: 此前只接了 idle / hurt 两条,
 * 动画文件里的 attack / pollen_burst / death 是**死资源**(定义了却从没被调用)。
 * 现在改成完整状态机: 死亡 &gt; 受击 &gt; 挥击 &gt; 环境花粉 &gt; 移动 &gt; 待机,
 * 其中一次性动画用 {@code getAnimationTick()} 卡住播完再交还 ——
 * 原版 {@code swing()} 只维持 6t, 直接切状态会把 0.55s 的攻击动作腰斩。
 */
public class TeaMinionEntity extends Monster implements GeoAnimatable {

    // ===== 动画(名字必须与 assets/ftboss/animations/camellia_sprout.animation.json 一致) =====
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.camellia.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.camellia.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.camellia.attack");
    private static final RawAnimation POLLEN = RawAnimation.begin().thenPlay("animation.camellia.pollen_burst");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.camellia.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.camellia.death");

    /** 一次性动画时长(tick), 与 animation_length 对齐: attack 0.55s / pollen 1.0s / hurt 0.4s / death 1.0s */
    private static final double T_ATTACK = 11.0, T_POLLEN = 20.0, T_HURT = 8.0;
    /** 环境花粉间隔(tick): 静止时每 ~7 秒抖一次花粉 */
    private static final int POLLEN_PERIOD = 140;

    /** 「守护茶芽」标记: 沙耶二阶段召唤, 全部清除前沙耶免伤 */
    private static final String TAG_GUARD = "ftboss_guard_bud";
    private static final java.util.UUID GUARD_HP_ID = java.util.UUID.fromString("8f2b1c44-9a13-4a55-9c6e-71c3a0f4d512");

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    public TeaMinionEntity(EntityType<? extends TeaMinionEntity> type, Level level) {
        super(type, level);
        this.xpReward = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.ATTACK_SPEED, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    /** 是否为「沙耶茶芽护盾」的守护茶芽 */
    public boolean isGuardBud() {
        return this.getPersistentData().getBoolean(TAG_GUARD);
    }

    /** 标记为守护茶芽: 最大生命 +100%(更硬), 持久化, 掉落经验更多 */
    public void makeGuardBud() {
        this.getPersistentData().putBoolean(TAG_GUARD, true);
        var hp = this.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.removeModifier(GUARD_HP_ID);
            hp.addTransientModifier(new AttributeModifier(GUARD_HP_ID, "ftboss_guard_bud_hp", 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            this.setHealth(this.getMaxHealth());
        }
        this.setPersistenceRequired();
        this.setCustomNameVisible(true);
        this.xpReward = 12;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---- GeckoLib ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "camellia_ctrl", 4, this::predicate));
    }

    /** 动画状态机: 死亡 > 受击 > 挥击 > 环境花粉 > 移动 > 待机 */
    private PlayState predicate(AnimationState<TeaMinionEntity> state) {
        // 1) 死亡(1.0s, 与原版 20t 尸体移除对齐, 不会被腰斩)
        if (this.isDeadOrDying()) {
            state.setAnimation(DEATH);
            return PlayState.CONTINUE;
        }
        // 2) 已开始的一次性动画播完再交还, 避免动作被截断
        if (playing(state, HURT, T_HURT)) return PlayState.CONTINUE;
        if (playing(state, ATTACK, T_ATTACK)) return PlayState.CONTINUE;
        if (playing(state, POLLEN, T_POLLEN)) return PlayState.CONTINUE;
        // 3) 受击 / 挥击(MeleeAttackGoal 会调 swing(), 客户端由原版动画包同步)
        if (this.hurtTime > 0) {
            state.setAnimation(HURT);
            return PlayState.CONTINUE;
        }
        if (this.swinging) {
            state.setAnimation(ATTACK);
            return PlayState.CONTINUE;
        }
        // 4) 环境表演: 静止时按 tickCount 相位错开抖一次花粉(同一群小妖不会整齐划一)
        if (!state.isMoving() && Math.floorMod(this.tickCount + this.getId() * 13, POLLEN_PERIOD) == 0) {
            state.setAnimation(POLLEN);
            return PlayState.CONTINUE;
        }
        // 5) 移动 / 待机
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    /** 当前正在播这条一次性动画且还没播完 -> 保持它 */
    private static boolean playing(AnimationState<TeaMinionEntity> state, RawAnimation anim, double ticks) {
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
