package net.farmingtales.ftboss.item.weapons;

import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;

/** 黑切技能同步控制器；2026-09-16 暴露 GeckoLib 已采样的局部秒数，使骨骼与发光共用一条时钟。 */
public final class CleaverSkillController extends AnimationController<BlackCleaverItem> {
    public static final String NAME = "cleaver_skill";
    private double seconds;

    public CleaverSkillController(BlackCleaverItem item) {
        super(item, NAME, 0, state -> PlayState.STOP);
    }

    @Override
    protected double adjustTick(double tick) {
        double local = super.adjustTick(tick);
        seconds = local / 20.0;
        return local;
    }

    public String clipName() {
        return getAnimationState() == State.STOPPED || getCurrentAnimation() == null ? null : getCurrentAnimation().animation().name();
    }

    public double seconds() {
        return seconds;
    }
}
