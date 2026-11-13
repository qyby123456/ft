package com.farmingtales.drinkanim.math;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Shared attachment transforms for the game renderer and the editable animation export. */
public final class BottleRig {
    public static final float BLOCKBENCH_CALIBRATION=1.25f;
    public static final float SCALE=.61f*BLOCKBENCH_CALIBRATION;
    public static final float ARM_SCALE=.70f;
    public static final float NECK_CLEARANCE=.025f/16;
    public static final float NECK_JOIN_Y=-.1f/16+NECK_CLEARANCE;
    public static final float CORK_SEATED_HEIGHT=.70f+NECK_JOIN_Y;
    public static final float CAMERA_Z=15.5f;
    // Convert the original runtime anchor to the user's BB pivot, then use its camera.
    public static final float VIEW_Y=(10.648f-11.38f)/16;
    public static final float VIEW_Z=(16-CAMERA_Z)/16;
    public record Transform(Vector3f position,Quaternionf rotation) {}
    public record Frame(Transform bottle,Transform hand,Transform helper,Transform cork,double assistance) {}

    public static Frame solve(BottleMotion.Pose motion,BottleMotion.Action action,double age,int duration,int side) {
        return solve(motion,action,age,duration,side,false);
    }
    public static Vector3f idleGripOffset(int side,boolean slim) {
        // The user's authored palm position, relative to the bottle pivot (Blockbench pixels).
        Vector3f offset=new Vector3f(side*1.58f,.1229328f,-1.410216f);
        if(slim) {
            // A 3px palm keeps its inner edge at the wide palm's contact surface.
            Vector3f inset=armRotation(ArmMotion.REST,side,false).transform(new Vector3f(-side*.35f,0,0));
            offset.add(inset).add(0,-.10f,-.20f);
        }
        return offset.div(16);
    }
    public static Frame solve(BottleMotion.Pose motion,BottleMotion.Action action,double age,int duration,int side,boolean slim) {
        return solve(motion,action,age,duration,side,slim,DrinkCatalog.ORANGE);
    }
    public static Frame solve(BottleMotion.Pose motion,BottleMotion.Action action,double age,int duration,int side,boolean slim,DrinkProfile profile) {
        if(BambooEggAnimations.owns(profile))return BambooEggAnimations.rig(profile,motion,action,age,duration,side,slim);
        if(StickyRiceCakeAnimations.owns(profile))return StickyRiceCakeAnimations.rig(profile,motion,action,age,duration,side,slim);
        if(PlateFoodAnimations.owns(profile))return PlateFoodAnimations.rig(profile,motion,action,age,duration,side,slim);
        if(profile.jam())return JamAnimations.rig(profile,motion,action,age,duration,side,slim);
        float scale=profile.renderScale();
        Quaternionf rotation=rotation(motion,side);
        var anchor=ContainerAnimations.handAnchor(profile,action,age,duration,motion);
        // Axial inspection turns the bottle between the palms; forearms do not orbit its axis.
        boolean waterReturn=action==BottleMotion.Action.INSPECT&&ContainerAnimations.family(profile)==ContainerAnimations.Family.WATER&&age>=53;
        Quaternionf gripRotation=action==BottleMotion.Action.INSPECT&&!waterReturn
            ?rotation(new BottleMotion.Pose(anchor.x(),anchor.y(),anchor.z(),anchor.pitch(),BottleMotion.IDLE.yaw(),anchor.roll(),anchor.fill()),side)
            :rotation;
        Vector3f pivot=new Vector3f((float)(side*motion.x()*.75),(float)(motion.y()*.75+.30*scale),(float)(motion.z()*.75));
        if(profile!=DrinkCatalog.ORANGE&&!profile.cup()&&action==BottleMotion.Action.DRINK) {
            float follow=(float)(BottleMotion.smooth((age-35)/13)*(1-BottleMotion.smooth((age-sipEnd(duration))/12)));
            float mouthDifference=(16.1f/16-.30f)*SCALE-(profile.mouthY()/16-.30f)*scale;
            pivot.add(rotation.transform(new Vector3f(0,mouthDifference*follow,0)));
        }
        Vector3f contact=idleGripOffset(side,slim);
        rotation(BottleMotion.IDLE,side).conjugate().transform(contact);
        contact.mul(profile.width()/4*profile.scale(),1,profile.depth()/4*profile.scale());
        Vector3f handPivot=new Vector3f(pivot).add((float)(side*(anchor.x()-motion.x())*.75),
            (float)((anchor.y()-motion.y())*.75),(float)((anchor.z()-motion.z())*.75));
        gripRotation.transform(contact).add(handPivot);
        if(action==BottleMotion.Action.INSPECT&&!ContainerAnimations.owns(profile)) {
            double regrip=BottleMotion.inspectionRegrip(age),push=BottleMotion.inspectionPush(age);
            contact.add((float)(side*(.013*regrip-.007*push)),(float)(.004*push),(float)(.006*regrip));
        }
        ArmMotion.Pose armPose=DrinkMotion.arm(profile,action,age,duration,false);
        if(action==BottleMotion.Action.DRINK) {
            double end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);
            double follow=BottleMotion.smooth((age-34)/9)*(1-BottleMotion.smooth((age-end)/16));
            double coupledLift=(motion.pitch()-BottleMotion.IDLE.pitch())*.62;
            armPose=new ArmMotion.Pose(armPose.lift()+(coupledLift-armPose.lift())*follow,armPose.sweep(),0);
        }
        Quaternionf handRotation=armRotation(armPose,side,false);
        Transform hand=new Transform(contact,handRotation);
        double assistance=DrinkMotion.assistance(profile,action,age,duration);
        boolean pulling=action==BottleMotion.Action.DRINK&&profile.style()!=DrinkProfile.Style.BEER&&age<(profile.pullCap()?41:42);
        float[] capPivot=profile.capPivot();
        Vector3f cork=new Vector3f((capPivot[0]-8)/16*scale,
            (float)(((capPivot[1]-4.8f)/16+profile.neckJoin()+(pulling?DrinkMotion.capLift(profile,age):0))*scale),(capPivot[2]-8)/16*scale);
        rotation.transform(cork).add(pivot);
        boolean removeTab=profile.can()&&action==BottleMotion.Action.DRINK&&age>28&&age<47;
        if(removeTab) {
            float lift=(float)BottleMotion.smooth((age-28)/5);
            cork.add(-side*.055f*lift,.065f*lift,-.015f*lift);
        }
        Vector3f helperOffset=profile.can()?new Vector3f(-side*.08f,-.012f,.085f):new Vector3f(-side*.025f,-.10f,.025f);
        if(profile.can()&&action==BottleMotion.Action.DRINK) {
            float hook=(float)BottleMotion.smooth((age-14)/7),pull=(float)BottleMotion.smooth((age-22)/6);
            helperOffset.set(-side*.075f,-.028f+.025f*hook+.032f*pull,.095f-.04f*hook);
        }
        Vector3f helperTarget;
        if(action==BottleMotion.Action.INSPECT) {
            helperTarget=new Vector3f(0,-.30f*scale-.008f,0);
            gripRotation.transform(helperTarget).add(pivot);
        } else {
            helperTarget=new Vector3f(cork).add(helperOffset);
        }
        if(ContainerAnimations.owns(profile)) {
            float support=(float)ContainerAnimations.support(profile,action,age,duration);
            if(support>0) {
                // Support the near left edge so the palm is visible below the container, including XO's wide base.
                Vector3f base=gripRotation.transform(new Vector3f(0,-.30f*scale-.008f,0)).add(pivot)
                    .add(-side*profile.width()*scale/64,0,.018f);
                helperTarget.lerp(base,support);
                helperTarget.y+=(float)ContainerAnimations.supportPat(profile,action,age);
            }
        }
        if(LandNo1BottleAnimations.owns(profile)&&action==BottleMotion.Action.DRINK) {
            float support=(float)LandNo1BottleAnimations.bodySupport(age,duration);
            if(support>0) {
                // The four-serving bottle transfers from the cap hand to a load-bearing palm.
                Vector3f base=rotation.transform(new Vector3f(-side*profile.width()*scale/64,
                    -.30f*scale-.008f,.018f)).add(pivot);
                helperTarget.lerp(base,support);
            }
        }
        // Retract outside the camera first; keep the original grip target and authored orientation.
        Vector3f helper=new Vector3f(helperTarget).lerp(new Vector3f(-side*.75f,-.83f,-.90f),(float)(1-assistance));
        if(removeTab&&age>35) {
            float flight=(float)(age-35);
            cork.add(-side*.09f*flight,.024f*flight-.006f*flight*flight,-.035f*flight);
        }
        if(pulling&&!profile.can()&&age>=(profile.pullCap()?BottleMotion.CORK_RELEASE:33))cork.set(helper).sub(helperOffset);
        Quaternionf corkRotation=new Quaternionf(rotation).rotateZ((float)Math.toRadians(pulling?BottleMotion.corkRock(age):0));
        if(!profile.pullCap()) {
            corkRotation=new Quaternionf(rotation);
            if(profile.can()) {
                corkRotation.rotateX((float)Math.toRadians(DrinkMotion.tabAngle(action,age,duration)));
                if(removeTab&&age>35)corkRotation.rotateXYZ(radians((age-35)*17),0,radians(side*(age-35)*11));
            } else if(pulling)corkRotation.rotateY((float)Math.toRadians(-side*DrinkMotion.twist(age)));
        }
        Transform helperTransform=new Transform(helper,armRotation(DrinkMotion.arm(profile,action,age,duration,true),-side,true));
        float symmetric=(float)ContainerAnimations.symmetricHold(profile,action,age);
        if(symmetric>0) {
            Quaternionf rock=new Quaternionf(rotation).rotateY(radians(-side*180));
            var matched=new ArmMotion.Pose(26,0,0);
            Quaternionf rightRotation=new Quaternionf(rock).mul(armRotation(matched,side,false));
            Quaternionf leftRotation=new Quaternionf(rock).mul(armRotation(matched,-side,false));
            Transform left=brandySideGrip(pivot,rock,leftRotation,profile,-side,slim);
            helperTransform=new Transform(new Vector3f(helperTransform.position()).lerp(left.position(),symmetric),
                new Quaternionf(helperTransform.rotation()).slerp(leftRotation,symmetric));
        }
        if(profile.style()==DrinkProfile.Style.BRANDY||ContainerAnimations.family(profile)==ContainerAnimations.Family.BRANDY) {
            Quaternionf facing=new Quaternionf(rotation).rotateY(radians(-side*180));
            if(action==BottleMotion.Action.INSPECT) {
                // Transfer from the ordinary side grip while the other palm supports the base.
                // Restore the carry grip before the final movement back to idle.
                float transfer=(float)(BottleMotion.smooth((age-3)/9)*(1-BottleMotion.smooth((age-76)/9)));
                Quaternionf carryRotation=new Quaternionf(rotation).mul(rotation(restMotion(profile),side).conjugate())
                    .mul(armRotation(ArmMotion.REST,side,false));
                Vector3f carryContact=idleGripOffset(side,slim);
                rotation(BottleMotion.IDLE,side).conjugate().transform(carryContact);
                carryContact.mul(profile.width()/4*profile.scale(),1,profile.depth()/4*profile.scale());
                rotation.transform(carryContact).add(pivot);
                Transform carry=new Transform(carryContact,carryRotation);
                Quaternionf supported=new Quaternionf(facing).mul(armRotation(new ArmMotion.Pose(26,0,0),side,false));
                hand=blend(carry,brandySideGrip(pivot,facing,supported,profile,side,slim),transfer);
            } else if(action==BottleMotion.Action.DRINK) {
                float support=(float)(BottleMotion.smooth((age-41)/7)*(1-BottleMotion.smooth((age-sipEnd(duration)-3)/12)));
                // Calibrate the grip to the user's drinking pose, then keep it in bottle space.
                // Both wrists follow every tilt instead of sliding as the liquid empties.
                double referencePitch=75.65,lift=16+referencePitch*.30;
                Quaternionf gripFrame=new Quaternionf(facing).rotateX(radians(-referencePitch));
                Quaternionf right=new Quaternionf(gripFrame).mul(armRotation(new ArmMotion.Pose(lift,0,0),side,false));
                Quaternionf left=new Quaternionf(gripFrame).mul(armRotation(new ArmMotion.Pose(lift,0,0),-side,false));
                hand=blend(hand,brandySideGrip(pivot,facing,right,profile,side,slim,5),support);
                helperTransform=blend(helperTransform,brandySideGrip(pivot,facing,left,profile,-side,slim,5),support);
            }
        }

        if(profile.style()==DrinkProfile.Style.BEER&&action==BottleMotion.Action.DRINK&&age>=27&&age<47) {
            // The cap stays at the bite point while the bottle pulls away, then flies out sideways.
            var bite=ContainerAnimations.sample(profile,action,27,duration,true);
            Quaternionf biteRotation=rotation(bite,side);
            Vector3f bitePivot=new Vector3f((float)(side*bite.x()*.75),(float)(bite.y()*.75+.30*scale),(float)(bite.z()*.75));
            cork=biteRotation.transform(new Vector3f((capPivot[0]-8)/16*scale,
                ((capPivot[1]-4.8f)/16+profile.neckJoin())*scale,(capPivot[2]-8)/16*scale)).add(bitePivot);
            double flight=Math.max(0,age-32);
            cork.add((float)(side*.11*flight),(float)(.012*flight-.0045*flight*flight),(float)(-.075*flight));
            corkRotation=new Quaternionf(biteRotation).rotateXYZ(radians(flight*29),radians(side*flight*17),radians(flight*41));
        }
        if(profile.cup()) {
            // Keep the palm fixed to the stem or the low side of the cup throughout the swirl and sip.
            float unit=scale/16;
            Vector3f local=idleGripOffset(side,slim);
            rotation(BottleMotion.IDLE,side).conjugate().transform(local);
            local.y=0;local.normalize().mul((profile.cupShape().radius()+.55f)*unit);
            local.y=(profile.cupShape().gripY()-4.8f)*unit;
            if(profile.breweryMug()) {
                // These mugs use the same side-of-container contact as a can, ignoring the handle.
                local=idleGripOffset(side,slim);
                rotation(BottleMotion.IDLE,side).conjugate().transform(local);
                local.mul(profile.width()/4*profile.scale(),1,profile.depth()/4*profile.scale());
            }
            Vector3f palm=rotation.transform(local).add(pivot);
            double sipping=action==BottleMotion.Action.DRINK?BottleMotion.smooth((age-24)/24)*(1-BottleMotion.smooth((age-sipEnd(duration))/12)):0;
            double lift=18+24*sipping;
            if(CoconutAnimations.owns(profile))lift+=.9*Math.max(0,motion.pitch()-48)*sipping;
            Quaternionf coupled=new Quaternionf(rotation).mul(rotation(BottleMotion.IDLE,side).conjugate())
                .mul(armRotation(new ArmMotion.Pose(lift,0,0),side,false));
            // Bend at the elbow while the wrist follows the rim. Rotating the entire
            // forearm by the cup tilt points the shoulder into view and lowers the rig.
            coupled.slerp(armRotation(new ArmMotion.Pose(lift,0,0),side,false),(float)sipping);
            hand=new Transform(palm,coupled);
            if(CupAnimations.coffeePreview(profile))hand=coffeeGrip(pivot,rotation,coupled,profile,side,slim);
        }
        if(profile.id().equals("vinery:jo_special_mixture")&&action==BottleMotion.Action.DRINK&&age<42) {
            // Jo's short, broad lid needs the opening palm above its shoulder.
            // Apply only to the helper after the original cap and carry-hand solve.
            float reach=age<14?(float)assistance:1;
            Vector3f clearance=new Vector3f(-side*.13f,.15f,.06f).mul(reach);
            // Approach around the shoulder; keep the same grip offset while
            // the detached cap follows the original withdrawing hand path.
            if(age<14)clearance.add(new Vector3f(-side*.10f,.06f,.24f).mul(4*reach*(1-reach)));
            var openingArm=DrinkMotion.arm(profile,action,age,duration,true);
            Quaternionf openingRotation=armRotation(new ArmMotion.Pose(openingArm.lift()-34*assistance,
                openingArm.sweep(),openingArm.pronation()),-side,true);
            helperTransform=new Transform(new Vector3f(helperTransform.position()).add(clearance),openingRotation);
        }
        return ServingAnimations.rig(profile,action,age,duration,side,slim,
            CupAnimations.coffeeRig(profile,action,age,side,slim,new Frame(new Transform(pivot,rotation),hand,helperTransform,new Transform(cork,corkRotation),assistance)));
    }
    /** Keep the closed fist around the outer upright of the original cup handle. */
    public static Transform coffeeGrip(Vector3f pivot,Quaternionf cup,Quaternionf arm,DrinkProfile p,int side,boolean slim) {
        float unit=p.renderScale()/16;
        // The handle is rotated 22.5 degrees around (4.25, 5); the rabbit's
        // entire handle is a quarter pixel farther toward the mug.
        float handleX=p.id().endsWith(":white_rabbit_mocha")?4.5f:4.25f;
        float angle=radians(22.5);
        float x=handleX-2.5f*(float)Math.cos(angle)+2*(float)Math.sin(angle);
        float y=5-2.5f*(float)Math.sin(angle)-2*(float)Math.cos(angle);
        Vector3f local=new Vector3f(side*(x-8)*unit,(y-4.8f)*unit,0);
        // The user's grip marker is the centre of the exposed hand end face.
        // Its origin is shared by both arm widths; do not attach to the sleeve side.
        return new Transform(cup.transform(local).add(pivot),new Quaternionf(arm));
    }
    private static Transform brandySideGrip(Vector3f pivot,Quaternionf facing,Quaternionf arm,DrinkProfile profile,int side,boolean slim) {
        return brandySideGrip(pivot,facing,arm,profile,side,slim,4);
    }
    private static Transform brandySideGrip(Vector3f pivot,Quaternionf facing,Quaternionf arm,DrinkProfile profile,int side,boolean slim,float radius) {
        Vector3f normal=facing.transform(new Vector3f(side,0,0));
        float unit=profile.renderScale()/16;
        Vector3f surface=facing.transform(new Vector3f(side*radius*unit,(1.20f-4.8f)*unit,.030f)).add(pivot);
        Vector3f palmX=arm.transform(new Vector3f(1,0,0)),palmY=arm.transform(new Vector3f(0,1,0)),palmZ=arm.transform(new Vector3f(0,0,1));
        // Start from the sleeve extent, then reproduce the closer grip in the reference.
        // The existing interior mask hides only the small part of the palm inside the glass.
        float halfWidth=(slim?1.75f:2.25f)*ARM_SCALE/16,halfDepth=2.25f*ARM_SCALE/16;
        float extent=Math.abs(normal.dot(palmX))*halfWidth+Math.abs(normal.dot(palmZ))*halfDepth;
        float axial=normal.dot(palmY);
        extent+=Math.max(12.25f*axial,-.25f*axial)*ARM_SCALE/16;
        Vector3f inset=new Vector3f(-side*(radius==4?.80f:.50f),radius==4?.08f:0,radius==4?-.50f:0).div(16);
        return new Transform(surface.fma(extent-.002f,normal).add(facing.transform(inset)),new Quaternionf(arm));
    }
    private static double sipEnd(int duration) {return BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration);}
    private static BottleMotion.Pose restMotion(DrinkProfile profile) {return ContainerAnimations.rest(profile);}
    private static Transform blend(Transform a,Transform b,float weight) {
        return new Transform(new Vector3f(a.position()).lerp(b.position(),weight),new Quaternionf(a.rotation()).slerp(b.rotation(),weight));
    }
    public static boolean corkDetached(BottleMotion.Action action,double age) {
        return action==BottleMotion.Action.DRINK&&age>16&&age<41;
    }
    public static Quaternionf rotation(BottleMotion.Pose motion,int side) {
        return new Quaternionf().rotateZ(radians(side*motion.roll())).rotateX(radians(motion.pitch())).rotateY(radians(side*motion.yaw()));
    }
    public static Quaternionf armRotation(ArmMotion.Pose motion,int side,boolean helper) {
        Vector3f axis=helper?new Vector3f(-side*.45f,.50f,-.74f):new Vector3f(-side*.40f,.20f,-.894f);
        new Quaternionf().rotateY(radians(side*motion.sweep())).rotateX(radians(motion.lift())).transform(axis).normalize();
        Vector3f forward=new Vector3f(1,0,0).cross(axis).normalize();
        Vector3f right=new Vector3f(axis).cross(forward).normalize();
        return new Quaternionf().setFromNormalized(new Matrix3f().setColumn(0,right).setColumn(1,axis).setColumn(2,forward))
            .rotateY(radians(side*motion.pronation()));
    }
    private static float radians(double degrees) {return (float)Math.toRadians(degrees);}
}
