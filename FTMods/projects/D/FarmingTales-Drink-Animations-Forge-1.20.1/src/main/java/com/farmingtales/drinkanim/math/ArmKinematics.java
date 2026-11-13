package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A rigid vanilla arm pivots from its elbow side; its palm remains at the bottle grip. */
public final class ArmKinematics {
    public static final float FOREARM_LENGTH=12*BottleRig.ARM_SCALE/16;
    public static Vector3f elbow(Vector3f palm,Quaternionf rotation) {
        return new Vector3f(palm).sub(rotation.transform(new Vector3f(0,FOREARM_LENGTH,0)));
    }
    private ArmKinematics() {}
}
