/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 */
package net.farmingtales.ftboss.client.defectzero;

import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.client.defectzero.DefectSkillSync;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class DefectSkillAnimation {
    private static final Map<String, RawAnimation> CLIPS = new HashMap<String, RawAnimation>();

    private DefectSkillAnimation() {
    }

    public static void apply(AnimationState<?> animationState, RawAnimation rawAnimation, BossActionState bossActionState) {
        String[] stringArray = DefectSkillSync.decode(bossActionState.name());
        if (stringArray.length == 0) {
            animationState.setAnimation(rawAnimation);
            return;
        }
        animationState.getController().transitionLength(0);
        animationState.getController().setAnimationSpeed((stringArray[1].equals("charge") ? 16.0 : 8.0) / (double)Integer.parseInt(stringArray[3]));
        String string2 = "animation.defect_core.skill." + stringArray[2] + "." + stringArray[1];
        animationState.setAnimation(CLIPS.computeIfAbsent(string2, string -> RawAnimation.begin().thenPlay(string)));
    }
}

