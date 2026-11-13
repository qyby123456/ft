package com.farmingtales.drinkanim.math;

import java.util.EnumMap;
import java.util.List;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.BottleMotion.Pose;
import com.farmingtales.drinkanim.math.BottleMotion.Camera;

/** Seven authored container performances; MC forearms retain their full, fixed length. */
public final class ContainerAnimations {
    public enum Family { BRANDY, RUM, CAN, WATER, JUICE, BEER, SPORTS }
    private record Key(double time,Pose pose) {}
    private record Design(Curve draw,Curve inspect,Curve open,Curve drawCamera,Curve inspectCamera,Curve openCamera,Pose empty) {}
    private static final EnumMap<Family,Curve> STOW=new EnumMap<>(Family.class);
    private static final EnumMap<Family,Curve> STOW_CAMERA=new EnumMap<>(Family.class);
    private static final EnumMap<Family,Design> DESIGNS=new EnumMap<>(Family.class);
    private static final Pose REST=BottleMotion.IDLE;
    private static final Pose XO_REST=REST.offset(0,.12,0,0,0);
    private static final Curve WATER_RETURN=curve(
        k(53,.36,-.67,-1.16,-9,155,355),k(59,.39,-.60,-1.12,-4,155,358),
        k(72,REST.offset(0,0,0,0,360)),
        k(104,REST.offset(0,0,0,0,360)));
    private static final Curve XO_PUSH=curve(k(0,0,0,0,0,0,0),k(31,0,0,0,0,0,0),
        k(39,.043,0,0,0,0,0),k(50,-.040,0,0,0,0,0),k(61,.034,0,0,0,0,0),
        k(71,-.018,0,0,0,0,0),k(82,0,0,0,0,0,0),k(104,0,0,0,0,0,0));
    private static final double ROCK_STEP=.05;
    private static final double[] XO_ROLL=brandyInertia();

    private static double[] brandyInertia() {
        double[] samples=new double[2081];double angle=0,velocity=0;
        for(int i=1;i<samples.length;i++) {
            double target=-145*XO_PUSH.sample(i*ROCK_STEP).x();
            velocity+=((target-angle)*.34-velocity*.66)*ROCK_STEP;
            angle+=velocity*ROCK_STEP;samples[i]=angle;
        }
        return samples;
    }
    private static double brandyRoll(double t) {
        double index=clamp(t,0,104)/ROCK_STEP;int a=(int)index,b=Math.min(a+1,XO_ROLL.length-1);
        return (XO_ROLL[a]+(XO_ROLL[b]-XO_ROLL[a])*(index-a))*(1-BottleMotion.smooth((t-90)/14));
    }

    public static Family family(DrinkProfile p) {
        if(p.cup())return Family.JUICE;
        if(p.id().equals("papercraft_magic_decoration:guang_s"))return Family.CAN;
        return switch(p.id()) {
            case "farmingtales:brandy_xo", "kaleidoscope_tavern:brandy", "kaleidoscope_tavern:carignan" -> Family.BRANDY;
            case "farmingtales:rum" -> Family.RUM;
            case "farmingtales:mozhao", "farmingtales:mozhao_p" -> Family.CAN;
            case "farmingtales:purified_water" -> Family.WATER;
            case "farmingtales:sweet_orange_juice" -> Family.JUICE;
            case "farmingtales:light_beer", "farmingtales:wheatbeer" -> Family.BEER;
            case "farmingtales:vitamin_drink" -> Family.SPORTS;
            default -> null;
        };
    }
    public static boolean owns(DrinkProfile p) {return family(p)!=null;}
    public static Pose rest(DrinkProfile p) {return family(p)==Family.BRANDY?XO_REST:REST;}

    // The throwing palm follows its own track. The bottle spins freely from tick 30 to 48.
    private static final Curve WATER_HAND=curve(
        k(0,REST),k(16,.35,-.57,-1.14,-8,155,-5),k(25,.34,-.79,-1.18,-12,155,-9),
        k(30,.34,-.53,-1.18,-3,155,0),k(37,.36,-.64,-1.17,-3,155,0),
        k(43,.358,-.41,-1.16,-3,155,0),k(48,.358,-.53,-1.16,-3,155,0),
        k(53,.36,-.67,-1.16,-9,155,-5),k(65,.39,-.60,-1.12,-4,155,-2),
        k(74,.39,-.60,-1.12,-4,155,-2),k(104,REST));

    static {
        // Brandy: close both palms on the broad sides before lifting to a low centered hold.
        DESIGNS.put(Family.BRANDY,new Design(
            curve(k(0,.26,-2.60,-.93,16,145,-9),k(3,.26,-2.60,-.93,16,145,-9),
                k(7,.24,-1.32,-1.20,9,149,-8),k(11,.31,-.69,-1.20,1,152,-5),
                k(15,.43,-.53,-1.12,-4,155,-2),k(19,XO_REST)),
            curve(k(0,XO_REST),k(12,.18,-.57,-1.15,-2,173,0),k(25,0,-.42,-1.10,0,180,0),
                k(35,0,-.415,-1.10,0,180,0),k(76,0,-.425,-1.10,0,180,0),
                k(87,.08,-.48,-1.13,-1,176,0),k(104,XO_REST)),
            curve(k(0,XO_REST),k(8,.33,-.58,-1.17,-8,155,-6),k(14,.22,-.55,-1.20,-5,158,-5),
                k(22,.22,-.56,-1.20,-5,151,-7),k(29,.23,-.54,-1.19,-3,159,-4),
                k(34,.23,-.55,-1.18,-5,155,-3),k(39,.13,-.53,-1.15,-4,170,0),
                k(43,0,-.49,-1.12,10,180,0),k(48,0,-.35,-1.01,71,180,0)),
            cam(c(0,0,0,0),c(5,.55,-.20,-.10),c(10,1.45,-.32,.16),c(15,-.32,.10,-.06),c(19,0,0,0)),
            cam(c(0,0,0,0),c(12,.20,0,0),c(26,-.30,0,0),c(76,-.24,0,0),c(91,.12,0,0),c(104,0,0,0)),
            cam(c(0,0,0,0),c(14,.30,-.14,.06),c(24,.15,.08,-.06),c(34,.26,-.10,.04),c(42,.65,-.12,.02),c(48,.90,-.08,.05)),
            new Pose(0,-.30,-.975,91,180,0,.025)));
        stows(Family.BRANDY,
            curve(k(0,XO_REST),k(4,.35,-.58,-1.15,-6,155,-5),k(8,.29,-.82,-1.17,0,151,-8),
                k(14,.26,-2.60,-.93,16,145,-9),k(20,.26,-2.60,-.93,16,145,-9)),
            cam(c(0,0,0,0),c(5,.26,-.14,-.08),c(11,1.12,-.26,.14),c(20,0,0,0)));

        // Rum: the broad angular bottle is carried across the hip; each facet has a weighted stop.
        DESIGNS.put(Family.RUM,new Design(
            curve(k(0,1.05,-2.57,-.84,18,133,24),k(2,1.05,-2.57,-.84,18,133,24),
                k(6,.81,-1.40,-.97,11,140,20),k(10,.44,-.79,-1.13,1,151,-9),
                k(14,.455,-.65,-1.11,-6,156,-3),k(19,REST)),
            curve(k(0,REST),k(10,.39,-.64,-1.13,-7,155,-10),k(21,.22,-.49,-1.23,-12,155,-28),
                k(23,.22,-.49,-1.23,-12,155,-28),k(30,.25,-.48,-1.22,-7,245,-33),
                k(39,.25,-.48,-1.22,-7,245,-33),k(46,.27,-.50,-1.24,-3,335,-28),
                k(55,.27,-.50,-1.24,-3,335,-28),k(62,.24,-.51,-1.23,-8,425,-31),
                k(71,.24,-.51,-1.23,-8,425,-31),k(78,.23,-.50,-1.22,-10,515,-25),
                k(87,.23,-.50,-1.22,-10,515,-25),k(104,.48,-.69,-1.08,-3,515,0)),
            curve(k(0,REST),k(7,.34,-.66,-1.13,-11,152,-5),k(14,.25,-.61,-1.17,-9,154,-8),
                k(23,.23,-.57,-1.17,-4,155,-6),k(26,.225,-.56,-1.17,-2,155,-5),
                k(29,.27,-.65,-1.19,-16,153,-1),k(34,.25,-.60,-1.16,-8,155,-4),
                k(40,.20,-.49,-1.10,25,162,2),k(48,.12,-.345,-1.00,75,169,6)),
            cam(c(0,0,0,0),c(4,.40,-.22,.18),c(8,1.55,-.62,.48),c(13,-.40,.19,-.13),c(19,0,0,0)),
            cam(c(0,0,0,0),c(21,.42,-.54,-.34),c(30,.30,.12,-.44),c(39,.30,.12,-.44),
                c(46,.45,.31,-.27),c(55,.45,.31,-.27),c(62,.55,-.13,-.40),c(71,.55,-.13,-.40),
                c(78,.36,-.25,-.25),c(87,.36,-.25,-.25),c(104,0,0,0)),
            cam(c(0,0,0,0),c(15,.28,-.15,.07),c(26,.40,-.20,.12),c(29,-.30,.15,-.09),c(35,.12,-.05,.03),c(48,1.02,-.12,.07)),
            new Pose(.095,-.29,-.965,97,169,7,.025)));
        stows(Family.RUM,
            curve(k(0,REST),k(3,.46,-.66,-1.10,-5,155,-3),k(7,.70,-.96,-1.01,5,145,14),
                k(13,1.05,-2.57,-.84,18,133,24),k(20,1.05,-2.57,-.84,18,133,24)),
            cam(c(0,0,0,0),c(5,.25,-.17,.13),c(10,1.12,-.60,.40),c(20,0,0,0)));

        // Cans: the background pose holds steady while the elbow supplies three fast strokes.
        DESIGNS.put(Family.CAN,new Design(
            curve(k(0,.77,-2.48,-.82,8,142,13),k(2,.77,-2.48,-.82,8,142,13),
                k(5.5,.59,-1.29,-1.00,1,150,9),k(9,.35,-.59,-1.13,-10,159,-12),
                k(12,.44,-.66,-1.11,-5,156,-5),k(15,.49,-.71,-1.065,-1,154,1),k(19,REST)),
            curve(k(0,REST),k(12,.35,-.57,-1.13,-12,155,-9),k(22,.34,-.56,-1.13,18,166,-12),
                k(25,.34,-.56,-1.13,18,166,-12),k(30,.34,-.56,-1.13,-5,158,-8),
                k(65,.34,-.56,-1.13,-5,158,-8),k(78,.35,-.56,-1.13,25,165,-5),
                k(87,.35,-.56,-1.13,25,165,-5),k(104,REST)),
            curve(k(0,REST),k(7,.36,-.62,-1.16,6,155,-8),k(14,.24,-.55,-1.21,24,155,-10),
                k(20,.235,-.545,-1.21,28,155,-10),k(23,.235,-.56,-1.21,25,155,-11),
                k(26,.25,-.545,-1.21,32,155,-8),k(30,.26,-.55,-1.20,27,155,-6),
                k(35,.29,-.57,-1.16,18,156,-3),k(40,.25,-.48,-1.09,34,160,0),
                k(48,.15,-.315,-1.00,66,164,2)),
            cam(c(0,0,0,0),c(3,.24,-.12,.05),c(7,1.20,-.32,.21),c(10,-.45,.16,-.13),c(15,.12,-.03,.02),c(19,0,0,0)),
            cam(c(0,0,0,0),c(20,.48,-.25,-.15),c(26,.48,-.25,-.15),
                c(32,.26,-.08,-.04),c(64,.26,-.08,-.04),c(81,.62,-.14,.07),c(88,.62,-.14,.07),c(104,0,0,0)),
            cam(c(0,0,0,0),c(15,.48,-.15,-.08),c(23,.55,-.18,-.10),c(26,.15,.08,.05),c(35,.27,-.12,.03),c(48,.73,-.07,.03)),
            new Pose(.13,-.265,-.965,90,164,3,.025)));
        stows(Family.CAN,
            curve(k(0,REST),k(3,.44,-.65,-1.105,-8,156,-6),k(6,.58,-.77,-1.03,0,148,12),
                k(11,.77,-2.48,-.82,8,142,13),k(20,.77,-2.48,-.82,8,142,13)),
            cam(c(0,0,0,0),c(4,-.16,.09,-.10),c(8,1.10,-.34,.24),c(15,.12,-.04,.02),c(20,0,0,0)));

        // Water: spring out of the pocket; dip, release, one free spin, absorb the catch.
        DESIGNS.put(Family.WATER,new Design(
            curve(k(0,.57,-2.62,-1.09,-9,166,-8),k(2,.57,-2.62,-1.09,-9,166,-8),
                k(6,.44,-1.38,-1.17,-8,160,-8),k(11,.47,-.65,-1.105,-5,155,-2),
                k(17,REST),k(19,REST)),
            curve(k(0,REST),k(16,.35,-.57,-1.14,-8,155,-5),k(25,.34,-.79,-1.18,-12,155,-9),
                k(30,.34,-.53,-1.18,-3,155,0),k(39,.349,-.13,-1.17,-3,155,180),
                k(48,.358,-.53,-1.16,-3,155,360),k(53,.36,-.67,-1.16,-9,155,355),
                k(65,.39,-.60,-1.12,-4,155,358),k(74,.39,-.60,-1.12,-4,155,358),k(104,.48,-.69,-1.08,-3,155,360)),
            curve(k(0,REST),k(7,.35,-.635,-1.125,-9,155,-5),k(13,.25,-.585,-1.16,-7,158,-6),
                k(21,.25,-.58,-1.16,-6,152,-7),k(29,.26,-.575,-1.15,-5,158,-5),
                k(35,.29,-.59,-1.13,-5,155,-2),k(41,.23,-.45,-1.07,32,161,2),
                k(48,.125,-.34,-.985,74,164,3)),
            cam(c(0,0,0,0),c(4,.27,-.06,-.05),c(9,1.15,-.17,-.12),c(12,-.34,.08,.08),c(16,.10,-.02,-.02),c(19,0,0,0)),
            cam(c(0,0,0,0),c(17,.35,-.14,.06),c(25,.78,-.12,-.10),c(32,-.38,-.12,.06),
                c(39,-.75,-.08,.10),c(48,.03,-.08,.04),
                c(53,.55,-.13,-.10),c(65,.18,-.09,.04),c(75,.18,-.09,.04),c(104,0,0,0)),
            cam(c(0,0,0,0),c(14,.25,-.09,.02),c(24,.15,.06,-.03),c(34,.22,-.04,.03),c(48,.66,-.04,.02)),
            new Pose(.105,-.29,-.95,95,164,3,.025)));
        stows(Family.WATER,
            curve(k(0,REST),k(4,.44,-.63,-1.13,-8,158,-5),k(9,.48,-1.06,-1.18,-7,162,-9),
                k(15,.57,-2.62,-1.09,-9,166,-8),k(20,.57,-2.62,-1.09,-9,166,-8)),
            cam(c(0,0,0,0),c(4,-.15,.05,-.04),c(11,.86,-.13,-.10),c(20,0,0,0)));

        // Juice: a one-handed swirl in a small horizontal orbit, with a trailing bottle lean.
        DESIGNS.put(Family.JUICE,new Design(
            curve(k(0,.30,-2.58,-.95,15,139,-16),k(3,.30,-2.58,-.95,15,139,-16),
                k(7,.28,-1.35,-1.17,5,148,-18),k(11,.39,-.70,-1.19,-5,155,-12),
                k(15,.47,-.65,-1.115,-4,156,-3),k(19,REST)),
            curve(k(0,REST),k(18,.33,-.53,-1.17,-7,155,-6),k(85,.33,-.53,-1.17,-7,155,-6),k(104,REST)),
            curve(k(0,REST),k(5,.36,-.57,-1.17,-3,155,-14),k(10,.37,-.66,-1.13,-12,155,1),
                k(14,.27,-.61,-1.18,-8,158,-6),k(22,.26,-.60,-1.18,-6,152,-7),
                k(29,.28,-.595,-1.17,-4,159,-5),k(35,.30,-.61,-1.14,-5,155,-2),
                k(41,.255,-.48,-1.09,24,160,1),k(48,.145,-.355,-1.005,70,163,4)),
            cam(c(0,0,0,0),c(5,.46,-.23,-.14),c(10,1.22,-.29,-.22),c(15,-.28,.09,.06),c(19,0,0,0)),
            cam(c(0,0,0,0),c(18,.40,-.16,-.07),c(89,.40,-.16,-.07),c(104,0,0,0)),
            cam(c(0,0,0,0),c(6,-.08,-.20,-.14),c(11,.42,.07,.05),c(16,.27,-.10,.03),c(26,.16,.05,-.03),c(35,.25,-.06,.03),c(48,.81,-.07,.04)),
            new Pose(.125,-.295,-.975,93,163,5,.025)));
        stows(Family.JUICE,
            curve(k(0,REST),k(5,.36,-.62,-1.16,-7,155,-10),k(9,.29,-1.01,-1.20,2,146,-16),
                k(15,.30,-2.58,-.95,15,139,-16),k(20,.30,-2.58,-.95,15,139,-16)),
            cam(c(0,0,0,0),c(5,.10,-.18,-.11),c(11,1.05,-.30,-.23),c(20,0,0,0)));

        // Beer: a small toast out in front, then four 90-degree label views with 0.45-second holds.
        DESIGNS.put(Family.BEER,new Design(
            curve(k(0,.92,-2.61,-1.04,11,146,13),k(2.5,.92,-2.61,-1.04,11,146,13),
                k(6,.75,-1.52,-1.16,4,150,10),k(10,.53,-.73,-1.21,-6,157,-5),
                k(14,.46,-.64,-1.14,-7,157,-3),k(19,REST)),
            curve(k(0,REST),k(10,.39,-.56,-1.19,-8,155,-5),k(20,.26,-.40,-1.40,-13,155,-8),
                k(22,.26,-.40,-1.40,-13,155,-8),k(29,.31,-.45,-1.29,-7,245,-12),
                k(38,.31,-.45,-1.29,-7,245,-12),k(45,.33,-.465,-1.27,-3,335,-10),
                k(54,.33,-.465,-1.27,-3,335,-10),k(61,.31,-.48,-1.26,-7,425,-9),
                k(70,.31,-.48,-1.26,-7,425,-9),k(77,.29,-.47,-1.27,-10,515,-7),
                k(86,.29,-.47,-1.27,-10,515,-7),k(104,.48,-.69,-1.08,-3,515,0)),
            curve(k(0,REST),k(8,.32,-.61,-1.16,8,158,0),k(17,.12,-.44,-1.02,48,168,1),
                k(24,.085,-.37,-.95,68,173,1),k(27,.085,-.37,-.95,68,173,1),
                k(32,.14,-.51,-1.15,28,165,0),k(36,.14,-.51,-1.15,28,165,0),
                k(42,.13,-.41,-1.04,55,167,2),k(48,.12,-.34,-.995,74,167,5)),
            cam(c(0,0,0,0),c(4,.27,-.15,.08),c(9,1.38,-.40,.26),c(14,-.33,.14,-.09),c(19,0,0,0)),
            cam(c(0,0,0,0),c(20,-.30,-.28,.08),c(29,.13,.12,.06),c(38,.13,.12,.06),
                c(45,.30,.24,-.16),c(54,.30,.24,-.16),c(61,.42,-.07,-.08),c(70,.42,-.07,-.08),
                c(77,.24,-.21,.06),c(86,.24,-.21,.06),c(104,0,0,0)),
            cam(c(0,0,0,0),c(17,.65,-.10,.03),c(24,.80,-.06,.02),c(27,.60,-.06,.02),c(29,.20,-.03,.02),c(33,.38,-.45,.06),c(38,.36,0,0),c(48,.88,-.08,.05)),
            new Pose(.10,-.29,-.965,96,167,6,.025)));
        stows(Family.BEER,
            curve(k(0,REST),k(4,.52,-.64,-1.16,-8,156,-3),k(8,.69,-.96,-1.17,1,150,8),
                k(14,.92,-2.61,-1.04,11,146,13),k(20,.92,-2.61,-1.04,11,146,13)),
            cam(c(0,0,0,0),c(5,-.08,-.15,-.03),c(10,.98,-.42,.25),c(20,0,0,0)));

        // Sports bottle: firm pickup, two palm pats at the base, check the grip, resistant screw cap.
        DESIGNS.put(Family.SPORTS,new Design(
            curve(k(0,.83,-2.59,-1.14,17,159,-2),k(2,.83,-2.59,-1.14,17,159,-2),
                k(6,.63,-1.40,-1.27,10,157,-9),k(10,.38,-.64,-1.22,-7,152,-12),
                k(13,.43,-.72,-1.12,1,156,2),k(16,.48,-.68,-1.09,-5,155,-1),k(19,REST)),
            curve(k(0,REST),k(18,.29,-.53,-1.22,-9,155,-18),k(26,.29,-.53,-1.22,-9,155,-18),
                k(32,.29,-.48,-1.22,-6,155,-15),k(36,.30,-.55,-1.22,-11,155,-19),
                k(42,.29,-.48,-1.22,-6,155,-15),k(47,.30,-.55,-1.22,-11,155,-19),
                k(62,.36,-.51,-1.24,-2,245,-24),k(73,.36,-.51,-1.24,-2,245,-24),
                k(86,.31,-.55,-1.18,-10,155,-9),k(104,REST)),
            curve(k(0,REST),k(7,.34,-.65,-1.16,-10,155,-6),k(14,.23,-.60,-1.20,-8,163,-8),
                k(21,.22,-.62,-1.21,-10,147,-13),k(24,.235,-.585,-1.19,-4,158,-5),
                k(30,.245,-.595,-1.20,-5,165,-8),k(35,.27,-.61,-1.16,-6,155,-3),
                k(41,.22,-.45,-1.075,36,164,1),k(48,.115,-.33,-.995,79,168,4)),
            cam(c(0,0,0,0),c(4,.39,-.14,-.04),c(9,1.54,-.27,-.17),c(13,-.43,.14,.12),c(17,.06,-.02,-.01),c(19,0,0,0)),
            cam(c(0,0,0,0),c(19,.45,-.28,-.18),c(27,.45,-.28,-.18),c(33,.04,-.25,-.13),
                c(37,.57,-.23,-.21),c(43,.04,-.25,-.13),c(48,.57,-.23,-.21),
                c(63,.22,.30,-.26),c(74,.22,.30,-.26),c(88,.44,-.14,-.06),c(104,0,0,0)),
            cam(c(0,0,0,0),c(15,.36,-.12,.07),c(22,.49,.10,-.11),c(25,.08,-.05,.04),c(32,.28,.11,-.06),c(36,.23,-.06,.03),c(48,.97,-.09,.05)),
            new Pose(.095,-.275,-.96,100,168,5,.025)));
        stows(Family.SPORTS,
            curve(k(0,REST),k(3,.43,-.64,-1.16,-8,155,-6),k(7,.57,-.94,-1.25,4,158,-8),
                k(12,.83,-2.59,-1.14,17,159,-2),k(20,.83,-2.59,-1.14,17,159,-2)),
            cam(c(0,0,0,0),c(4,-.15,-.09,-.08),c(9,1.20,-.27,-.16),c(16,.10,-.03,-.02),c(20,0,0,0)));
    }
    private static void stows(Family f,Curve motion,Curve camera) {STOW.put(f,motion);STOW_CAMERA.put(f,camera);}

    public static Pose sample(DrinkProfile p,Action action,double t,int duration,boolean replacement) {
        var d=DESIGNS.get(family(p));
        if(action==Action.IDLE)return rest(p);
        if(action==Action.DRAW)return d.draw.sample(t);
        if(action==Action.STOW)return stow(p,t);
        if(action==Action.INSPECT) {
            if(family(p)==Family.WATER&&t>=53)return WATER_RETURN.sample(t);
            Pose pose=d.inspect.sample(t);
            if(family(p)==Family.RUM||family(p)==Family.BEER)
                return MotionDetail.facet(pose,t,family(p)==Family.RUM?23:22);
            if(family(p)==Family.CAN||family(p)==Family.JUICE)return InertialGestures.apply(family(p),t,pose);
            if(family(p)==Family.BRANDY) {
                double push=XO_PUSH.sample(t).x(),roll=brandyRoll(t);
                // Hands supply the lateral impulse; the bottle leans later and settles under damping.
                return pose.offset(push,-.00011*roll*roll,.0008*roll,.14*roll,roll);
            }
            if(family(p)==Family.WATER&&t>=25&&t<=53) {
                if(t<30)return waterJoin(p,t,true);
                if(t>48)return waterJoin(p,t,false);
                return waterFlight(p,t);
            }
            return pose;
        }
        if(t<=48)return d.open.sample(t);
        double end=48+BottleMotion.sipTicks(duration);
        if(t<end) {
            double fraction=BottleMotion.smooth((t-48)/(end-48));
            Pose pose=d.open.sample(48).mix(d.empty,fraction);
            double pulse=sipPulse(t,duration),amplitude=switch(family(p)) {
                case BRANDY -> .65; case RUM -> 1.0; case CAN -> .45;
                case WATER -> .35; case JUICE -> .55; case BEER -> .75; case SPORTS -> .85;
            };
            return pose.offset(0,.006*amplitude*pulse,.003*amplitude*pulse,1.2*amplitude*pulse,0);
        }
        double tail=t-end;
        Pose hidden=d.draw.sample(0).wine(.025);
        Pose lowered=rest(p).offset(-.09,-.035,0,-2,4).wine(.025);
        if(tail<=12)return d.empty.mix(lowered,BottleMotion.smooth(tail/12));
        if(tail<=24) {
            Curve stow=STOW.get(family(p));
            double hiddenAt=stow.keys[stow.keys.length-2].time;
            double stowTime=(tail-12)*hiddenAt/12,settle=1-BottleMotion.smooth(stowTime/5);
            return stow(p,stowTime).offset(-.09*settle,-.035*settle,0,-2*settle,4*settle).wine(.025);
        }
        if(tail<=37||!replacement)return hidden;
        if(tail<=41)return hidden.wine(.8);
        return d.draw.sample((tail-41)*19/BottleMotion.REPLACEMENT_DRAW_TICKS);
    }
    private static Pose waterFlight(DrinkProfile p,double t) {
                double u=(t-30)/18;
                // Free flight has a constant downward acceleration, not eased hovering at the apex.
                Pose flight=new Pose(.34+.018*u,-.53+1.60*u*(1-u),-1.18+.02*u,-3,155,360*u,.8);
                // Rotate about the container's centre of mass, so the visible bottle follows the arc.
                double centerY=((p.boundsMin()[1]+p.boundsMax()[1])*.5-4.8)*p.renderScale()/16;
                double lever=centerY*Math.cos(Math.toRadians(-3))/.75,roll=Math.toRadians(360*u);
                return flight.offset(lever*Math.sin(roll),lever*(1-Math.cos(roll)),0,0,0);
    }
    private static Pose waterJoin(DrinkProfile p,double t,boolean launch) {
        double a=launch?25:48,b=launch?30:53;
        Curve track=launch?DESIGNS.get(Family.WATER).inspect:WATER_RETURN;
        Pose start=launch?track.sample(a):waterFlight(p,a),end=launch?waterFlight(p,b):track.sample(b);
        double[] s=values(start),e=values(end),v=new double[7];
        double edge=launch?b:a,h=.01;
        double[] before=values(waterFlight(p,edge-h)),at=values(waterFlight(p,edge)),after=values(waterFlight(p,edge+h));
        for(int j=0;j<7;j++) {
            double velocity=(after[j]-before[j])/(2*h),acceleration=(after[j]-2*at[j]+before[j])/(h*h);
            v[j]=Curve.segment(s[j],e[j],launch?track.velocity(a,j):velocity,launch?velocity:track.velocity(b,j),
                launch?track.acceleration(a,j):acceleration,launch?acceleration:track.acceleration(b,j),(t-a)/(b-a),b-a);
        }
        return new Pose(v[0],v[1],v[2],v[3],v[4],v[5],v[6]);
    }
    private static Pose stow(DrinkProfile p,double t) {
        return STOW.get(family(p)).sample(t);
    }
    public static double assistance(DrinkProfile p,Action action,double t) {
        return assistance(p,action,t,16);
    }
    public static double assistance(DrinkProfile p,Action action,double t,int duration) {
        if(p.cup())return 0;
        Family f=family(p);
        if(action==Action.DRAW)return switch(f) {
            case BRANDY -> window(t,3,7,12,18);
            case JUICE -> window(t,5,9,12,17);
            default -> 0;
        };
        if(action==Action.STOW)return switch(f) {
            case BRANDY -> window(t,0,4,9,15);
            case JUICE -> window(t,1,5,9,14);
            default -> 0;
        };
        if(action==Action.INSPECT)return switch(f) {
            case BRANDY -> window(t,3,12,85,102);
            case RUM, BEER -> window(t,12,21,85,98);
            case CAN, WATER -> 0;
            case JUICE -> 0;
            case SPORTS -> window(t,8,18,73,91);
        };
        if(action==Action.DRINK) {
            double end=48+BottleMotion.sipTicks(duration);
            if(t>=end+41)return assistance(p,Action.DRAW,(t-end-41)*19/BottleMotion.REPLACEMENT_DRAW_TICKS,duration);
            if(f==Family.BEER)return 0;
            double opening=window(t,f==Family.CAN?5:6,14,f==Family.CAN?34:32,f==Family.CAN?46:42);
            if(f==Family.BRANDY)opening=Math.max(opening,window(t,42,48,end+7,end+18));
            return opening;
        }
        return 0;
    }
    public static double support(DrinkProfile p,Action action,double t,int duration) {
        if(action==Action.DRAW||action==Action.STOW||action==Action.INSPECT)return 1;
        double end=48+BottleMotion.sipTicks(duration);
        if(action==Action.DRINK&&t>=end+41)return 1;
        return action==Action.DRINK&&family(p)==Family.BRANDY?BottleMotion.smooth((t-42)/3):0;
    }
    /** Separate translation and rotation for a palm which has released the thrown water bottle. */
    public static Pose handAnchor(DrinkProfile p,Action action,double t,int duration,Pose rendered) {
        if(family(p)!=Family.WATER||action!=Action.INSPECT)return rendered;
        // After the catch, keep the palm rigidly attached throughout the return.
        if(t>=53)return rendered.offset(0,0,0,0,-360);
        Pose authored=sample(p,action,t,duration,true),hand=WATER_HAND.sample(t);
        // The palm is attached while imparting spin and absorbing the catch, then relaxes.
        if(t<30||t>48) {
            double blend=t<30?BottleMotion.smooth((t-25)/5):1-BottleMotion.smooth((t-48)/5);
            Pose catchPose=t>48?authored.offset(0,0,0,0,-360):authored;
            hand=hand.mix(catchPose,blend);
        }
        // Preserve locomotion applied by the caller without attaching the palm to the airborne bottle.
        return hand.offset(rendered.x()-authored.x(),rendered.y()-authored.y(),rendered.z()-authored.z(),
            rendered.pitch()-authored.pitch(),rendered.roll()-authored.roll());
    }
    public static double supportPat(DrinkProfile p,Action action,double t) {
        if(family(p)!=Family.SPORTS||action!=Action.INSPECT)return 0;
        return .04*(BottleMotion.gesturePulse(t,27,7)+BottleMotion.gesturePulse(t,37,7));
    }
    public static double symmetricHold(DrinkProfile p,Action action,double t) {
        return family(p)==Family.BRANDY&&action==Action.INSPECT?window(t,3,12,85,102):0;
    }
    public static ArmMotion.Pose symmetricArm(double t) {
        return new ArmMotion.Pose(23+3*window(t,16,25,78,94),0,0);
    }
    public static ArmMotion.Pose arm(DrinkProfile p,Action action,double t,int duration,boolean helper) {
        if(action==Action.IDLE)return ArmMotion.REST;
        double end=48+BottleMotion.sipTicks(duration);
        if(action==Action.DRINK&&t>=end+41)return arm(p,Action.DRAW,(t-end-41)*19/BottleMotion.REPLACEMENT_DRAW_TICKS,duration,helper);
        Pose pose=handAnchor(p,action,t,duration,sample(p,action,t,duration,true));Pose r=rest(p);
        if(!helper) {
            double lift=clamp((pose.pitch()-r.pitch())*.48+(pose.y()-r.y())*24,-18,50);
            double sweep=clamp((pose.x()-r.x())*22,-10,10);
            if(action==Action.INSPECT) {
                lift+=20*(family(p)==Family.WATER?window(t,5,19,59,72):window(t,5,19,85,104))+1.3*inspectionEffort(p,t);
                if(family(p)==Family.BRANDY)lift+=20*window(t,13,25,77,94);
                if(family(p)==Family.WATER)lift+=7*window(t,25,30,48,62);
                sweep-=2.2*inspectionEffort(p,t);
            }
            return new ArmMotion.Pose(lift,sweep,0);
        }
        double a=assistance(p,action,t,duration),support=support(p,action,t,duration);
        double lift=action==Action.INSPECT?18:action==Action.DRAW||action==Action.STOW?8:14;
        if(action==Action.INSPECT&&family(p)==Family.BRANDY)lift+=6*window(t,13,25,77,94);
        if(action==Action.DRINK&&family(p)==Family.BRANDY)lift+=20*support;
        if(action==Action.DRINK&&p.can())lift+=-6*window(t,14,20,22,25)+10*window(t,22,28,32,42);
        double twist=p.pullCap()||p.can()?0:16*(BottleMotion.gesturePulse(t,15,9)+BottleMotion.gesturePulse(t,24,8));
        double effort=action==Action.INSPECT?inspectionEffort(p,t):0;
        return new ArmMotion.Pose(lift*a+1.5*effort,-4*a+1.8*effort,twist*a);
    }
    public static double inspectionEffort(DrinkProfile p,double t) {
        double[] starts=switch(family(p)) {
            case RUM -> new double[]{23,39,55,71}; case BEER -> new double[]{22,38,54,70};
            case BRANDY -> new double[]{34,43,52,61};
            case CAN -> new double[]{29,37,45};
            case WATER -> new double[]{25,48}; case JUICE -> new double[]{};
            case SPORTS -> new double[]{27,37,48};
        };
        double effort=0;
        for(double start:starts)effort+=BottleMotion.gesturePulse(t,start+1,6);
        return effort;
    }
    public static Camera camera(DrinkProfile p,Action action,double t,int duration) {
        var d=DESIGNS.get(family(p));
        if(action==Action.IDLE)return new Camera(0,0,0);
        if(action==Action.DRAW)return asCamera(d.drawCamera.sample(t));
        if(action==Action.INSPECT) {
            if(family(p)==Family.WATER&&t>=65) {
                double returnWeight=1-BottleMotion.smooth((t-65)/7);
                Camera settled=asCamera(d.inspectCamera.sample(65));
                return new Camera(settled.pitch()*returnWeight,settled.yaw()*returnWeight,settled.roll()*returnWeight);
            }
            Camera camera=asCamera(d.inspectCamera.sample(t));
            if(family(p)==Family.CAN||family(p)==Family.JUICE) {
                Camera response=InertialGestures.camera(family(p),t);
                return new Camera(camera.pitch()+response.pitch(),camera.yaw()+response.yaw(),camera.roll()+response.roll());
            }
            if(family(p)==Family.BRANDY) {
                double delayed=XO_PUSH.sample(Math.max(0,t-2.25)).x()/.043;
                double lean=brandyRoll(Math.max(0,t-1.25));
                return new Camera(camera.pitch()-.10*delayed*delayed,1.65*delayed,-.105*lean);
            }
            return camera;
        }
        if(action==Action.STOW)return asCamera(STOW_CAMERA.get(family(p)).sample(t));
        if(t<=48)return asCamera(d.openCamera.sample(t));
        double end=48+BottleMotion.sipTicks(duration);
        if(t<end) {
            Camera raised=asCamera(d.openCamera.sample(48));
            double pulse=sipPulse(t,duration);
            return new Camera(raised.pitch()+.08*pulse,raised.yaw(),raised.roll());
        }
        double tail=t-end;
        Camera raised=asCamera(d.openCamera.sample(48));
        if(tail<=15) {
            double a=1-BottleMotion.smooth(tail/15);
            return new Camera(raised.pitch()*a,raised.yaw()*a,raised.roll()*a);
        }
        if(tail<=41)return new Camera(0,0,0);
        return asCamera(d.drawCamera.sample((tail-41)*19/BottleMotion.REPLACEMENT_DRAW_TICKS));
    }
    public static List<AnimationSoundTimeline.Cue> inspectionSounds(DrinkProfile p) {
        if(family(p)==Family.BEER)return AnimationSoundTimeline.inspection();
        int[] turns=switch(family(p)) {
            case BRANDY -> new int[]{20,35,44,53,62}; case CAN -> new int[]{29,37,45};
            case WATER -> new int[]{26}; case JUICE -> new int[]{30,52,74};
            case SPORTS -> new int[]{27,37,50}; case RUM -> new int[]{23,39,55,71};
            default -> throw new IllegalStateException();
        };
        var cues=new java.util.ArrayList<AnimationSoundTimeline.Cue>();
        cues.add(new AnimationSoundTimeline.Cue(6,"inspect_lift",.36f,1));
        if(family(p)!=Family.CAN&&family(p)!=Family.WATER&&family(p)!=Family.JUICE)cues.add(new AnimationSoundTimeline.Cue(14,"inspect_support",.26f,1.02f));
        for(int tick:turns) {
            cues.add(new AnimationSoundTimeline.Cue(tick,"inspect_turn",.25f,1.04f));
            if(family(p)!=Family.WATER)cues.add(new AnimationSoundTimeline.Cue(tick+3,"inspect_liquid",.15f,1.03f));
            if(family(p)==Family.RUM)cues.add(new AnimationSoundTimeline.Cue(tick+7,"inspect_settle",.09f,1));
        }
        if(family(p)==Family.WATER) {
            cues.add(new AnimationSoundTimeline.Cue(48,"inspect_support",.38f,.95f));
            cues.add(new AnimationSoundTimeline.Cue(51,"inspect_liquid",.22f,1.02f));
            cues.add(new AnimationSoundTimeline.Cue(56,"inspect_settle",.10f,1));
        }
        cues.add(new AnimationSoundTimeline.Cue(family(p)==Family.WATER?58:90,"inspect_return",.28f,.98f));
        return cues.stream().sorted(java.util.Comparator.comparingInt(AnimationSoundTimeline.Cue::tick)).toList();
    }
    /** Liquid inertia is kicked by the authored direction changes and by the water catch. */
    public static double gestureImpulse(DrinkProfile p,Action action,int t) {
        if(action==Action.DRINK)return t==27?.10:0;
        if(action!=Action.INSPECT)return 0;
        int[] beats=switch(family(p)) {
            case BRANDY -> new int[]{42,51,60,69}; case CAN -> new int[]{};
            case WATER -> new int[]{30,48}; case JUICE -> new int[]{};
            case SPORTS -> new int[]{32,42,62}; case RUM -> new int[]{30,46,62,78};
            case BEER -> new int[]{20,29,45,61,77};
        };
        for(int i=0;i<beats.length;i++)if(t==beats[i]) {
            double force=family(p)==Family.WATER?.14:family(p)==Family.CAN?.095:.065;
            return i%2==0?force:-force;
        }
        return 0;
    }
    private static double sipPulse(double t,int duration) {return MotionDetail.swallow(t,duration);}
    private static double window(double t,double start,double in,double out,double end) {return BottleMotion.smooth((t-start)/(in-start))*(1-BottleMotion.smooth((t-out)/(end-out)));}
    private static double clamp(double v,double a,double b) {return Math.max(a,Math.min(b,v));}
    private static Key k(double t,Pose pose) {return new Key(t,pose);}
    private static Key k(double t,double x,double y,double z,double pitch,double yaw,double roll) {return k(t,new Pose(x,y,z,pitch,yaw,roll,.8));}
    private static Key c(double t,double pitch,double yaw,double roll) {return k(t,pitch,yaw,roll,0,0,0);}
    private static Curve curve(Key... keys) {return new Curve(keys);}
    private static Curve cam(Key... keys) {return curve(keys);}
    private static Camera asCamera(Pose p) {return new Camera(p.x(),p.y(),p.z());}

    private static double[] values(Pose p) {return new double[]{p.x(),p.y(),p.z(),p.pitch(),p.yaw(),p.roll(),p.fill()};}

    /** Shared velocity AND acceleration. Reversals keep a restoring force instead of hanging motionless.
     * Only explicit holds and endpoints come to rest with zero acceleration. */
    private static final class Curve {
        final Key[] keys;
        final double[][] values,tangents,accelerations;
        Curve(Key... keys) {
            this.keys=keys;values=new double[keys.length][7];tangents=new double[keys.length][7];accelerations=new double[keys.length][7];
            for(int i=0;i<keys.length;i++) {
                Pose p=keys[i].pose;values[i]=new double[]{p.x(),p.y(),p.z(),p.pitch(),p.yaw(),p.roll(),p.fill()};
                if(i>0&&keys[i].time<=keys[i-1].time)throw new IllegalArgumentException("Animation key times must increase");
            }
            for(int i=1;i<keys.length-1;i++)for(int j=0;j<7;j++) {
                double h0=keys[i].time-keys[i-1].time,h1=keys[i+1].time-keys[i].time;
                double d0=(values[i][j]-values[i-1][j])/h0,d1=(values[i+1][j]-values[i][j])/h1;
                if(d0*d1>0) {
                    double w0=2*h1+h0,w1=h1+2*h0;
                    tangents[i][j]=(w0+w1)/(w0/d0+w1/d1);
                }
                if(d0!=0&&d1!=0)accelerations[i][j]=2*(d1-d0)/(h0+h1);
            }
        }
        double velocity(double t,int j) {double h=.01;return (values(sample(t+h))[j]-values(sample(t-h))[j])/(2*h);}
        double acceleration(double t,int j) {double h=.01;return (values(sample(t+h))[j]-2*values(sample(t))[j]+values(sample(t-h))[j])/(h*h);}
        static double segment(double a,double b,double va,double vb,double aa,double ab,double t,double h) {
            double t2=t*t,t3=t2*t,t4=t3*t,t5=t4*t;
            return (1-10*t3+15*t4-6*t5)*a+(t-6*t3+8*t4-3*t5)*h*va
                +(10*t3-15*t4+6*t5)*b+(-4*t3+7*t4-3*t5)*h*vb
                +.5*(t2-3*t3+3*t4-t5)*h*h*aa+.5*(t3-2*t4+t5)*h*h*ab;
        }
        Pose sample(double time) {
            if(time<=keys[0].time)return keys[0].pose;
            int b=1;while(b<keys.length&&time>=keys[b].time)b++;
            if(b==keys.length)return keys[b-1].pose;
            int a=b-1;double h=keys[b].time-keys[a].time,t=(time-keys[a].time)/h;
            double[] v=new double[7];
            for(int j=0;j<7;j++)v[j]=segment(values[a][j],values[b][j],tangents[a][j],tangents[b][j],
                accelerations[a][j],accelerations[b][j],t,h);
            return new Pose(v[0],v[1],v[2],v[3],v[4],v[5],v[6]);
        }
    }
    private ContainerAnimations() {}
}
