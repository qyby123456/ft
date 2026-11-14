/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.AnimationController$State
 *  software.bernie.geckolib.core.object.PlayState
 */
package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.item.weapons.BlackCleaverItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;

public final class CleaverSkillController
extends AnimationController<BlackCleaverItem> {
    public static final String NAME = "cleaver_skill";
    private double seconds;

    public CleaverSkillController(BlackCleaverItem item) {
        super((GeoAnimatable)item, NAME, 0, state -> PlayState.STOP);
    }

    protected double adjustTick(double tick) {
        double local = super.adjustTick(tick);
        this.seconds = local / 20.0;
        return local;
    }

    public String clipName() {
        return this.getAnimationState() == AnimationController.State.STOPPED || this.getCurrentAnimation() == null ? null : this.getCurrentAnimation().animation().name();
    }

    public double seconds() {
        return this.seconds;
    }
}

