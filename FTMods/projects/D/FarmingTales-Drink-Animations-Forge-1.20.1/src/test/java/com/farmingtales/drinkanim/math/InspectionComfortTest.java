package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class InspectionComfortTest {
    @Test void allInspectionCamerasKeepOnlySmallAnglesAndMildMovingFeedback() {
        for(var p:DrinkCatalog.all()) {
            var motion=new Locomotion();motion.reset(0);
            for(int tick=0;tick<=104;tick++) {
                motion.tick(tick*.18,.16,tick<50||tick>=58,true,tick==50?.42:tick==58?.65:0,0);
                var camera=DrinkMotion.camera(p,Action.INSPECT,tick,16);
                assertTrue(Math.abs(camera.pitch())<.20,p.id()+" pitch");
                assertTrue(Math.abs(camera.yaw())<.15,p.id()+" yaw");
                assertTrue(Math.abs(camera.roll())<.05,p.id()+" horizon roll");
                var moving=motion.camera(1,camera,DrinkMotion.movementCameraStrength(Action.INSPECT));
                assertTrue(Math.abs(moving.pitch()-camera.pitch())<.06,"Running / jump feedback must also remain mild");
            }
        }
    }
    private static double elapsedAt(DrinkProfile p,double authored) {
        double lo=0,hi=104;
        for(int i=0;i<50;i++) {
            double mid=(lo+hi)*.5;
            if(AnimationPlayback.authored(p,Action.INSPECT,mid,16)<authored)lo=mid;else hi=mid;
        }
        return (lo+hi)*.5;
    }
    @Test void waterReturnsFortyPercentFasterWithContinuousLaunchCatchAndSoundClock() {
        var p=DrinkCatalog.find("farmingtales:purified_water");
        double launch=elapsedAt(p,30),catchAt=elapsedAt(p,48);
        assertEquals((18*76.0/104)/1.4,catchAt-launch,1e-9);
        assertTrue(AnimationPlayback.ticks(p,Action.INSPECT,16)<=50);
        for(double t=0;t<=10;t+=.125)assertEquals(t*104/76,AnimationPlayback.authored(p,Action.INSPECT,t,16),1e-9,"Initial lift unchanged");
        for(double authored:new double[]{18,25,30,48,53,65,72.2}) {
            double at=elapsedAt(p,authored),h=.0001;
            var a=pose(p,at-h);var b=pose(p,at);var c=pose(p,at+h);
            assertEquals((b.y()-a.y())/h,(c.y()-b.y())/h,.002,"No vertical velocity jump");
            assertEquals((b.roll()-a.roll())/h,(c.roll()-b.roll())/h,.03,"No spin jump");
        }
        int catches=0;
        for(int tick=1;tick<=66;tick++) {
            double a=AnimationPlayback.authored(p,Action.INSPECT,tick-1,16),b=AnimationPlayback.authored(p,Action.INSPECT,tick,16);
            for(var cue:ContainerAnimations.inspectionSounds(p))if(cue.event().equals("inspect_support")&&AnimationPlayback.crossed(a,b,cue.tick())) {
                catches++;assertEquals(Math.ceil(catchAt),tick,1e-9);
            }
        }
        assertEquals(1,catches);
    }
    private static BottleMotion.Pose pose(DrinkProfile p,double elapsed) {
        return DrinkMotion.sample(p,Action.INSPECT,AnimationPlayback.authored(p,Action.INSPECT,elapsed,16),16,true);
    }
    @Test void waterReturnMovesOnceAndKeepsThePalmFixedToTheBottle() {
        var p=DrinkCatalog.find("farmingtales:purified_water");
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            org.joml.Vector3f grip=null;
            var previous=DrinkMotion.sample(p,Action.INSPECT,65,16,true);
            for(double t=65;t<=104;t+=.125) {
                var now=DrinkMotion.sample(p,Action.INSPECT,t,16,true);
                assertTrue(now.x()>=previous.x()-1e-8&&now.y()<=previous.y()+1e-8,"No pause followed by a late bounce");
                if(t>66&&t<71)assertTrue(now.x()>previous.x()+1e-7,"Return keeps progressing");
                var rig=BottleRig.solve(now,Action.INSPECT,t,16,side,slim,p);
                var local=new org.joml.Quaternionf(rig.bottle().rotation()).conjugate()
                    .transform(new org.joml.Vector3f(rig.hand().position()).sub(rig.bottle().position()));
                if(grip==null)grip=local;
                assertEquals(0,grip.distance(local),1e-6,"No sliding grip during return");
                previous=now;
            }
            for(double boundary:new double[]{53,72,82}) {
                var a=DrinkMotion.sample(p,Action.INSPECT,boundary-.001,16,true);
                var b=DrinkMotion.sample(p,Action.INSPECT,boundary+.001,16,true);
                assertTrue(Math.abs(a.x()-b.x())<1e-6&&Math.abs(a.y()-b.y())<1e-6,"Resting endpoints join smoothly");
            }
        }
    }
    @Test void finishedWaterSpinBlendsIntoRunningWithoutUnwinding() {
        var p=DrinkCatalog.find("farmingtales:purified_water");
        for(boolean slim:new boolean[]{true,false})for(int side:new int[]{-1,1})for(double run:new double[]{0,.35,1}) {
            BottleRig.Frame previous=null;
            for(double t=72;t<=82;t+=.125) {
                var motion=DrinkMotion.blendRunning(p,Action.INSPECT,DrinkMotion.sample(p,Action.INSPECT,t,16,true),run*DrinkMotion.idleBlend(p,Action.INSPECT,t,16,true));
                var rig=BottleRig.solve(motion,Action.INSPECT,t,16,side,slim,p);
                if(previous!=null)assertTrue(Math.abs(previous.bottle().rotation().dot(rig.bottle().rotation()))>.999,"No reverse revolution at inspection end");
                previous=rig;
            }
            var idleMotion=DrinkMotion.blendRunning(p,Action.IDLE,DrinkMotion.sample(p,Action.IDLE,0,16,true),run);
            var idle=BottleRig.solve(idleMotion,Action.IDLE,0,16,side,slim,p);
            assertEquals(0,idle.hand().position().distance(previous.hand().position()),1e-6);
            assertTrue(Math.abs(idle.bottle().rotation().dot(previous.bottle().rotation()))>1-1e-6);
        }
    }
    @Test void brandyMatchesReferencePalmsAndMaintainsContactThroughSipping() {
        var p=DrinkCatalog.find("farmingtales:brandy_xo");
        for(boolean slim:new boolean[]{true,false})for(int side:new int[]{-1,1})for(boolean helper:new boolean[]{false,true}) {
            org.joml.Vector3f contact=null;org.joml.Quaternionf rotation=null;
            for(double t=48;t<=88;t+=.25) {
                var rig=BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,16,true),Action.DRINK,t,16,side,slim,p);
                var hand=helper?rig.helper():rig.hand();
                var inverse=new org.joml.Quaternionf(rig.bottle().rotation()).conjugate();
                var local=inverse.transform(new org.joml.Vector3f(hand.position()).sub(rig.bottle().position()));
                var relative=new org.joml.Quaternionf(inverse).mul(hand.rotation());
                if(contact==null) {contact=local;rotation=relative;}
                assertEquals(0,contact.distance(local),1e-6,"Palm does not slide while tilting the bottle");
                assertTrue(Math.abs(rotation.dot(relative))>1-1e-6,"Wrist follows the same bottle tilt");
            }
        }
        for(var action:new Action[]{Action.INSPECT,Action.DRINK}) {
            double time=action==Action.INSPECT?43:AnimationPlayback.authored(p,action,2.5666666667*20,16);
            var rig=BottleRig.solve(DrinkMotion.sample(p,action,time,16,true),action,time,16,1,true,p);
            var hand=action==Action.INSPECT?rig.helper():rig.hand();
            var screen=new org.joml.Vector3f(hand.position()).add(BottleFraming.offset(rig,action,time,16,true,p)).mul(16);
            if(action==Action.INSPECT) {assertEquals(-2.77,screen.x,.06);assertEquals(-4.66,screen.y,.06);assertEquals(-12.74,screen.z,.06);}
            else {assertEquals(4.11,screen.x,.04);assertEquals(-3.03,screen.y,.04);assertEquals(-10.70,screen.z,.04);}
        }
    }
}
