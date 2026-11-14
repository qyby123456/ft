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
 * 小姜饼人: 姜饼王(糖霜女皇)的召唤物(2026-09-10 新增, 取代原先套名字的原版僵尸)。
 * <p>GeckoLib 模型 {@code gingerbread_minion}(7 骨骼), 5 段动画: idle / walk / attack / hurt / death。
 * <p>与茶芽小妖同一套写法: 一次性动画用 {@code getAnimationTick()} 卡住播完再交还状态机
 * (原版 {@code swing()} 只持续 6t, 直接切状态会把 0.5s 的攻击动作腰斩)。
 */
public class GingerbreadMinionEntity extends Monster implements GeoAnimatable {
    /** 低级铁魔法冷却(2026-09-12): 较长 */
    private int spellCd = 160;
    /** 低级法术池(取不到会自动跳过并打 WARN) */
    private static final String[] LOW_SPELLS = {
            "irons_spellbooks:fireball", "irons_spellbooks:ice_block",
            "irons_spellbooks:lightning_bolt", "irons_spellbooks:magic_missile" };

    /** 2026-09-12 按用户要求: 姜饼人的随从会用**低级铁魔法**打玩家(CD 较久) */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (--this.spellCd > 0) return;
        this.spellCd = 160 + this.getRandom().nextInt(120);
        if (!(this.getTarget() instanceof net.minecraft.world.entity.player.Player p) || !p.isAlive()) return;
        if (this.distanceToSqr(p) > 24.0 * 24.0) return;
        String id = LOW_SPELLS[this.getRandom().nextInt(LOW_SPELLS.length)];
        net.farmingtales.ftboss.integ.WizardSummon.castByName(
                (net.minecraft.server.level.ServerLevel) this.level(), this, p, id, 1);
    }


    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.gingerbread_minion.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.gingerbread_minion.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.gingerbread_minion.attack");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.gingerbread_minion.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.gingerbread_minion.death");

    /** 一次性动画时长(tick): attack 0.5s / hurt 0.4s / death 1.0s(与原版 20t 尸体移除对齐) */
    private static final double T_ATTACK = 10.0, T_HURT = 8.0;

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);

    public GingerbreadMinionEntity(EntityType<? extends GingerbreadMinionEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // 2026-09-10 深夜按用户要求"小姜饼人加强一点": 24血/4攻/0.30速 → 36血/6攻/0.34速, 抗击退 0.2→0.4
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_SPEED, 1.2)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---- GeckoLib ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "gingerbread_ctrl", 4, this::predicate));
    }

    /** 动画状态机: 死亡 > 受击 > 挥击 > 移动 > 待机 */
    private PlayState predicate(AnimationState<GingerbreadMinionEntity> state) {
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

    /** 当前正在播这条一次性动画且还没播完 -> 保持它 */
    private static boolean playing(AnimationState<GingerbreadMinionEntity> state, RawAnimation anim, double ticks) {
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
