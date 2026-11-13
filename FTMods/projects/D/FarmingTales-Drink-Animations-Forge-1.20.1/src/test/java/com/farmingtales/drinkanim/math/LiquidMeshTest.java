package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;

class LiquidMeshTest {
    @Test void handContactStaysFixedOnBottleForBothHandsThroughoutActions() {
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1})for(var action:BottleMotion.Action.values()) {
            if(action==BottleMotion.Action.INSPECT)continue;
            org.joml.Vector3f expected=null;
            for(double tick=0;tick<=160;tick+=.25) {
                var rig=BottleRig.solve(BottleMotion.sample(action,tick,16),action,tick,16,side,slim);
                var local=new org.joml.Vector3f(rig.hand().position()).sub(rig.bottle().position());
                new org.joml.Quaternionf(rig.bottle().rotation()).conjugate().transform(local);
                if(expected==null)expected=new org.joml.Vector3f(local);
                assertTrue(local.distance(expected)<1e-6,"hand slides at "+action+" "+tick);
                var axis=rig.hand().rotation().transform(new org.joml.Vector3f(0,-.75f*BottleRig.ARM_SCALE,0));
                assertEquals(.75*BottleRig.ARM_SCALE,axis.length(),1e-6,"arm length must stay fixed");
            }
        }
    }
    @Test void drinkingRaisesForearmAndInspectionTurnsTheWholeBottleWithoutWristTwist() {
        var rest=BottleRig.solve(BottleMotion.IDLE,BottleMotion.Action.IDLE,0,16,1);
        var raised=BottleRig.solve(BottleMotion.sample(BottleMotion.Action.DRINK,60,16),BottleMotion.Action.DRINK,60,16,1);
        assertTrue(raised.hand().position().y-rest.hand().position().y>.16,"hand must rise with the bottle");
        var raisedFrame=BottleFraming.offset(raised,BottleMotion.Action.DRINK,60,16,true);
        var restFrame=BottleFraming.offset(rest,BottleMotion.Action.IDLE,0,16,false);
        assertTrue(raised.hand().position().y+raisedFrame.y-rest.hand().position().y-restFrame.y>.15,"framing must not cancel the arm lift");
        var up=new org.joml.Vector3f(0,1,0);
        assertTrue(raised.hand().rotation().transform(new org.joml.Vector3f(up)).y-rest.hand().rotation().transform(new org.joml.Vector3f(up)).y>.25);
        for(var action:BottleMotion.Action.values())for(double tick=0;tick<=160;tick+=.25)
            assertEquals(0,ArmMotion.sample(action,tick,16,false).pronation());
        assertTrue(Math.abs(BottleMotion.sample(BottleMotion.Action.INSPECT,32,16).roll())<=20);
        assertEquals(180,BottleMotion.sample(BottleMotion.Action.INSPECT,54,16).yaw()-BottleMotion.IDLE.yaw(),1e-6);
        assertEquals(1,BottleMotion.inspectionHand(52),1e-10);
        assertEquals(0,BottleMotion.inspectionHand(96),1e-10);
        assertEquals(ArmMotion.REST,ArmMotion.sample(BottleMotion.Action.INSPECT,BottleMotion.INSPECT_TICKS,16,false));
    }
    @Test void preservesVolumeUprightSidewaysAndUpsideDown() {
        for(var normal:new LiquidMesh.V[]{new LiquidMesh.V(0,1,0),new LiquidMesh.V(1,0,0),new LiquidMesh.V(0,-1,0),new LiquidMesh.V(.5,.7,-.2),new LiquidMesh.V(.00001,1,.01)}) {
            for(double fill:new double[]{.025,.15,.5,.8,.98}) {
                var faces=LiquidMesh.build(normal,fill);double volume=0;
                for(var f:faces)for(int i=1;i<f.points().size()-1;i++)volume+=f.points().get(0).dot(f.points().get(i).cross(f.points().get(i+1)))/6;
                assertEquals(fill*LiquidMesh.WIDTH*LiquidMesh.WIDTH*LiquidMesh.HEIGHT,volume,1e-7,normal+" fill "+fill);
                for(var f:faces)for(var p:f.points()) {
                    assertTrue(Double.isFinite(p.x()+p.y()+p.z()));
                    assertTrue(Math.abs(p.x())<=LiquidMesh.WIDTH/2+1e-8);
                    assertTrue(Math.abs(p.z())<=LiquidMesh.WIDTH/2+1e-8);
                    assertTrue(p.y()>=LiquidMesh.BOTTOM-1e-8&&p.y()<=LiquidMesh.BOTTOM+LiquidMesh.HEIGHT+1e-8);
                }
            }
        }
    }
    @Test void emptyBottleHasNoLiquidFaces() {assertTrue(LiquidMesh.build(new LiquidMesh.V(0,1,0),0).isEmpty());}
    @Test void movementAndJumpImpulsesSettleInsteadOfOscillatingForever() {
        var spring=new SloshSpring();spring.tick(.15,.3);
        assertTrue(spring.energy()>.1);
        for(int i=0;i<160;i++)spring.tick(0,0);
        assertTrue(spring.energy()<1e-7);
    }
    @Test void extremeMovementStaysBounded() {
        var spring=new SloshSpring();for(int i=0;i<200;i++)spring.tick(100,-100);
        assertTrue(Math.abs(spring.x(1))<=.55);assertTrue(Math.abs(spring.z(1))<=.55);
    }
    @Test void inspectionKeepsWineAndReturnsToRest() {
        for(int i=0;i<BottleMotion.INSPECT_TICKS;i++)assertEquals(.8,BottleMotion.sample(BottleMotion.Action.INSPECT,i,16).fill(),1e-9);
        var end=BottleMotion.sample(BottleMotion.Action.INSPECT,BottleMotion.INSPECT_TICKS,16);
        assertEquals(BottleMotion.IDLE.x(),end.x(),1e-6);
        assertEquals(BottleMotion.IDLE.y(),end.y(),1e-6);
        assertTrue(Math.abs(BottleRig.rotation(end,1).dot(BottleRig.rotation(BottleMotion.IDLE,1)))>1-1e-6);
    }
    @Test void drinkingDrainsOnlyDuringTheSip() {
        int start=BottleMotion.RAISE_TICKS,end=start+BottleMotion.sipTicks(16);
        assertEquals(.8,BottleMotion.sample(BottleMotion.Action.DRINK,start,16).fill(),1e-9);
        double previous=.8;
        for(int i=start;i<=end;i++){double fill=BottleMotion.sample(BottleMotion.Action.DRINK,i,16).fill();assertTrue(fill<=previous);previous=fill;}
        assertEquals(.025,previous,1e-9);
    }
    @Test void completedDrinkHidesTheEmptyBottleBeforeDrawingOnlyAnAvailableReplacement() {
        int sipEnd=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(16),end=BottleMotion.totalDrinkTicks(16);
        var noReplacement=BottleMotion.sample(BottleMotion.Action.DRINK,end,16,false);
        var replacement=BottleMotion.sample(BottleMotion.Action.DRINK,end,16,true);
        assertTrue(noReplacement.y()<-1.5);
        assertEquals(.025,noReplacement.fill(),1e-9);
        assertEquals(BottleMotion.IDLE,replacement);
        assertFalse(BottleMotion.corkVisible(BottleMotion.Action.DRINK,sipEnd+10,16,true));
        assertTrue(BottleMotion.corkVisible(BottleMotion.Action.DRINK,end,16,true));
        assertTrue(BottleMotion.mouthOpen(BottleMotion.Action.DRINK,sipEnd+10,16,true));
        assertFalse(BottleMotion.mouthOpen(BottleMotion.Action.DRINK,end,16,true));
    }
    @Test void runningFadesOutAndDoesNotKeepBobbingAtRest() {
        var motion=new Locomotion();motion.reset(0);
        for(int i=0;i<45;i++)motion.tick(i*.15,.2,true,true,0,0);
        assertTrue(motion.run(1)>.98);
        for(int i=0;i<100;i++)motion.tick(6.6,0,true,false,0,0);
        var pose=motion.apply(BottleMotion.IDLE,1,1);
        assertEquals(BottleMotion.IDLE.x(),pose.x(),1e-8);
        assertEquals(BottleMotion.IDLE.y(),pose.y(),1e-8);
        assertTrue(motion.run(1)<1e-8);
    }
    @Test void corkPullIsUpwardAndMouthOpensAtRelease() {
        double previous=0;
        for(double age=0;age<=45;age+=.25) {
            double lift=BottleMotion.corkLift(age);
            assertTrue(lift>=previous);previous=lift;
            assertTrue(Math.abs(BottleMotion.corkRock(age))<=2.01);
        }
        assertFalse(BottleMotion.mouthOpen(16));
        assertTrue(BottleMotion.mouthOpen(16.1),"glass must be revealed as soon as the cork moves");
        assertTrue(BottleMotion.mouthOpen(BottleMotion.CORK_RELEASE));
        assertTrue(BottleMotion.corkVisible(BottleMotion.Action.DRINK,BottleMotion.CORK_RELEASE,16,false));
        assertTrue(BottleMotion.mouthOpen(BottleMotion.Action.DRINK,BottleMotion.CORK_RELEASE,16,false));
        assertEquals(0,BottleMotion.openingHand(45));
    }
    @Test void drawAndStowStartOffscreenAndReturnToTheAuthoredHold() {
        var drawStart=BottleMotion.sample(BottleMotion.Action.DRAW,0,16);
        var drawEnd=BottleMotion.sample(BottleMotion.Action.DRAW,BottleMotion.DRAW_TICKS,16);
        var stowStart=BottleMotion.sample(BottleMotion.Action.STOW,0,16);
        var stowEnd=BottleMotion.sample(BottleMotion.Action.STOW,BottleMotion.STOW_TICKS,16);
        assertTrue(drawStart.y()<BottleMotion.IDLE.y()-1.8);
        assertEquals(BottleMotion.IDLE,drawEnd);
        assertEquals(BottleMotion.IDLE,stowStart);
        assertTrue(stowEnd.y()<BottleMotion.IDLE.y()-1.8);
        assertNotEquals(new BottleMotion.Camera(0,0,0),BottleMotion.camera(BottleMotion.Action.DRAW,8,16));
        assertNotEquals(new BottleMotion.Camera(0,0,0),BottleMotion.camera(BottleMotion.Action.STOW,8,16));
    }
    @Test void taczStyleCameraMovesTheWorldAndViewmodelForEveryAuthoredAction() {
        assertTrue(BottleMotion.camera(BottleMotion.Action.DRAW,8,16).magnitude()>2);
        assertTrue(BottleMotion.camera(BottleMotion.Action.STOW,8.4,16).magnitude()>1.5);
        assertTrue(BottleMotion.camera(BottleMotion.Action.INSPECT,34,16).magnitude()>.75);
        assertTrue(BottleMotion.camera(BottleMotion.Action.DRINK,60,16).magnitude()>.75);
        assertEquals(new Quaternionf(),BottleMotion.camera(BottleMotion.Action.DRAW,0,16).rotation());
        assertEquals(new Quaternionf(),BottleMotion.camera(BottleMotion.Action.INSPECT,BottleMotion.INSPECT_TICKS,16).rotation());
    }
    @Test void runtimeUsesTheBlockbenchParentScaleForBottleAndBothSlimArms() {
        assertEquals(1.25f,BottleRig.BLOCKBENCH_CALIBRATION,1e-6);
        assertEquals(.7625f,BottleRig.SCALE,1e-6);
        assertEquals(.70f,BottleRig.ARM_SCALE,1e-6);
    }
    @Test void replacementBottleNeckReturnsToTheBottleInsteadOfTheRetractedHelper() {
        for(int duration:new int[]{16,40,80,200})for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            for(double age=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration)+24;
                age<=BottleMotion.totalDrinkTicks(duration);age+=.25) {
                if(!BottleMotion.corkVisible(BottleMotion.Action.DRINK,age,duration,true))continue;
                var motion=BottleMotion.sample(BottleMotion.Action.DRINK,age,duration,true);
                var replacement=BottleRig.solve(motion,BottleMotion.Action.DRINK,age,duration,side,slim);
                var sealed=BottleRig.solve(motion,BottleMotion.Action.IDLE,0,duration,side,slim);
                assertTrue(replacement.cork().position().distance(sealed.cork().position())<1e-6,"new neck detached at "+age);
                assertTrue(replacement.cork().rotation().equals(sealed.cork().rotation(),1e-6f));
            }
        }
    }
    @Test void bothPalmProfilesSitBehindTheLabelAndSlimKeepsItsOwnContact() {
        for(int side:new int[]{-1,1}) {
            var wide=BottleRig.solve(BottleMotion.IDLE,BottleMotion.Action.IDLE,0,16,side,false);
            var slim=BottleRig.solve(BottleMotion.IDLE,BottleMotion.Action.IDLE,0,16,side,true);
            assertTrue(wide.hand().position().z<wide.bottle().position().z-.07);
            assertTrue(slim.hand().position().z<slim.bottle().position().z-.07);
            assertTrue(wide.hand().position().distance(slim.hand().position())>.015);
            assertEquals(wide.bottle(),slim.bottle());
            assertEquals(wide.cork(),slim.cork());
        }
    }
    @Test void rigidVanillaArmsPreserveLengthAndNeverFlipInward() {
        for(var action:new BottleMotion.Action[]{BottleMotion.Action.DRINK,BottleMotion.Action.INSPECT})
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            BottleRig.Frame previous=null;
            for(double age=0;age<=150;age+=.25) {
                var rig=BottleRig.solve(BottleMotion.sample(action,age,16),action,age,16,side,slim);
                for(boolean helper:new boolean[]{false,true}) {
                    var arm=helper?rig.helper():rig.hand();
                    var elbow=ArmKinematics.elbow(arm.position(),arm.rotation());
                    assertEquals(.525,arm.position().distance(elbow),1e-6,"12px arm stays rigid");
                    assertTrue(Float.isFinite(arm.rotation().lengthSquared()));
                    assertEquals(0,ArmMotion.sample(action,age,16,helper).pronation(),"no inward roll");
                    if(previous!=null) {
                        var old=helper?previous.helper():previous.hand();
                        assertTrue(Math.abs(old.rotation().dot(arm.rotation()))>.997,"forearm must not flip at "+action+" "+age);
                    }
                }
                previous=rig;
            }
        }
    }
    @Test void inspectionCompletesOneForwardRevolutionThenReturnsWithoutRewinding() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            for(double age=20;age<=86;age+=.25) {
                var pose=BottleMotion.sample(BottleMotion.Action.INSPECT,age,16);
                var rig=BottleRig.solve(pose,BottleMotion.Action.INSPECT,age,16,side,slim);
                var untwisted=new BottleMotion.Pose(pose.x(),pose.y(),pose.z(),pose.pitch(),BottleMotion.IDLE.yaw(),pose.roll(),pose.fill());
                var reference=BottleRig.solve(untwisted,BottleMotion.Action.INSPECT,age,16,side,slim);
                assertTrue(rig.hand().position().distance(reference.hand().position())<1e-6,"holding palm must not orbit bottle");
                assertTrue(rig.helper().position().distance(reference.helper().position())<1e-6,"support palm must stay under bottle");
            }
            double previousYaw=BottleMotion.IDLE.yaw();
            for(double age=0;age<=104;age+=.25) {
                var pose=BottleMotion.sample(BottleMotion.Action.INSPECT,age,16);
                assertTrue(Math.abs(pose.roll())<=20,"inspection tilt should stay restrained");
                assertTrue(pose.yaw()>=previousYaw-1e-6,"inspection must not rewind at "+age);
                assertTrue(pose.yaw()<=BottleMotion.IDLE.yaw()+360+1e-6,"one revolution only");
                previousYaw=pose.yaw();
                assertTrue(BottleMotion.camera(BottleMotion.Action.INSPECT,age,16).magnitude()<1.8,"restrained camera motion");
                if(age>=77)assertEquals(BottleMotion.IDLE.yaw()+360,pose.yaw(),1e-6,"facing label stays facing during return");
            }
            var end=BottleRig.solve(BottleMotion.sample(BottleMotion.Action.INSPECT,104,16),BottleMotion.Action.INSPECT,104,16,side,slim);
            var idle=BottleRig.solve(BottleMotion.IDLE,BottleMotion.Action.IDLE,0,16,side,slim);
            assertTrue(end.hand().position().distance(idle.hand().position())<1e-6);
            assertTrue(Math.abs(end.bottle().rotation().dot(idle.bottle().rotation()))>1-1e-6);
        }
    }
    @Test void supportPalmStaysCenteredBelowTheBottleThroughoutTheTurn() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true})for(double age=20;age<=86;age+=.25) {
            var pose=BottleMotion.sample(BottleMotion.Action.INSPECT,age,16);
            var rig=BottleRig.solve(pose,BottleMotion.Action.INSPECT,age,16,side,slim);
            var local=new org.joml.Vector3f(rig.helper().position()).sub(rig.bottle().position());
            new Quaternionf(rig.bottle().rotation()).conjugate().transform(local);
            assertEquals(0,local.x,1e-6,"support must stay on the bottle axis");
            assertEquals(0,local.z,1e-6,"support must stay on the bottle axis");
            assertEquals(-.30*BottleRig.SCALE-.008,local.y,1e-6,"support sits below the base");
        }
    }
    @Test void seatedNeckAndGlassKeepRigidSubpixelClearanceAboveTheShoulder() {
        for(var action:BottleMotion.Action.values())for(int duration:new int[]{16,40,200})
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            int length=action==BottleMotion.Action.DRINK?BottleMotion.totalDrinkTicks(duration):BottleMotion.INSPECT_TICKS;
            for(double age=0;age<=length;age+=.25) {
                var rig=BottleRig.solve(BottleMotion.sample(action,age,duration,true),action,age,duration,side,slim);
                var shoulder=rig.bottle().rotation().transform(new org.joml.Vector3f(0,(11f/16-.30f)*BottleRig.SCALE,0)).add(rig.bottle().position());
                var glass=rig.bottle().rotation().transform(new org.joml.Vector3f(0,(11.1f/16+BottleRig.NECK_JOIN_Y-.30f)*BottleRig.SCALE,0)).add(rig.bottle().position());
                var separated=rig.bottle().rotation().transform(new org.joml.Vector3f(0,BottleRig.NECK_CLEARANCE*BottleRig.SCALE,0)).add(shoulder);
                assertTrue(glass.distance(separated)<1e-6,"glass keeps a small non-coplanar clearance");
                assertTrue(glass.distance(shoulder)>.001&&glass.distance(shoulder)<.002,"no z-fighting or visible neck gap");
                if(BottleRig.corkDetached(action,age)||!BottleMotion.corkVisible(action,age,duration,true))continue;
                var neck=rig.cork().rotation().transform(new org.joml.Vector3f(0,(11.1f/16-1)*BottleRig.SCALE,0)).add(rig.cork().position());
                assertTrue(neck.distance(glass)<1e-6,"closed neck and open glass must share their seated height at "+action+" "+age);
                assertTrue(Math.abs(rig.cork().rotation().dot(rig.bottle().rotation()))>1-1e-6,"closed neck must rotate rigidly with the bottle");
            }
        }
    }
    @Test void inspectionPausesOnEachFaceWithZeroTurnSpeedAtTheStops() {
        double[] stops={29,45,61,77};
        for(int i=0;i<stops.length;i++) {
            double start=stops[i],angle=BottleMotion.IDLE.yaw()+90*(i+1);
            for(double age=start;age<=start+9;age+=.1)
                assertEquals(angle,BottleMotion.sample(BottleMotion.Action.INSPECT,age,16).yaw(),1e-6,"hold each face for 0.45 seconds");
            assertTrue(Math.abs(BottleMotion.inspectionYaw(start)-BottleMotion.inspectionYaw(start-.001))/.001<.01,"ease to a stop");
            assertTrue(Math.abs(BottleMotion.inspectionYaw(start+9+.001)-BottleMotion.inspectionYaw(start+9))/.001<.01,"ease into the next turn");
        }
    }
    @Test void replacementFinishesAtTheSameRigAsIdleWithoutAnotherDip() {
        for(int duration:new int[]{16,40,200})for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            int end=BottleMotion.totalDrinkTicks(duration);
            var before=BottleRig.solve(BottleMotion.sample(BottleMotion.Action.DRINK,end-1./3,duration,true),BottleMotion.Action.DRINK,end-1./3,duration,side,slim);
            var finish=BottleRig.solve(BottleMotion.sample(BottleMotion.Action.DRINK,end,duration,true),BottleMotion.Action.DRINK,end,duration,side,slim);
            var idle=BottleRig.solve(BottleMotion.IDLE,BottleMotion.Action.IDLE,0,duration,side,slim);
            assertEquals(idle.bottle(),finish.bottle());
            assertEquals(idle.hand(),finish.hand());
            assertEquals(idle.cork(),finish.cork());
            assertEquals(0,finish.assistance());
            assertTrue(before.hand().position().distance(idle.hand().position())<.001);
            assertTrue(BottleFraming.offset(finish,BottleMotion.Action.DRINK,end,duration,true).distance(BottleFraming.offset(idle,BottleMotion.Action.IDLE,0,duration,false))<1e-6);
        }
    }
    @Test void drinkingCameraHasSlowBoundedMotionAndNoReleaseShake() {
        double previous=0;
        for(double age=0;age<=147;age+=1./3) {
            double magnitude=BottleMotion.camera(BottleMotion.Action.DRINK,age,16).magnitude();
            assertTrue(magnitude<1.4);
            assertTrue(Math.abs(magnitude-previous)<.14);
            previous=magnitude;
        }
    }
    @Test void gaitAndIdleBreathingBlendBackBeforeTheDrinkStateEnds() {
        int end=BottleMotion.totalDrinkTicks(16);
        assertEquals(1,BottleMotion.idleBlend(BottleMotion.Action.DRINK,end,16,true));
        assertEquals(0,BottleMotion.idleBlend(BottleMotion.Action.DRINK,end-6,16,true));
        assertTrue(BottleMotion.idleBlend(BottleMotion.Action.DRINK,end-1./3,16,true)>.998);
        assertEquals(0,BottleMotion.idleBlend(BottleMotion.Action.DRINK,end,16,false));
    }
    @Test void completeGesturesKeepBothSleevesBottleAndCorkOutsideTheNearPlane() {
        for(var action:new BottleMotion.Action[]{BottleMotion.Action.DRINK,BottleMotion.Action.INSPECT})
        for(int duration:new int[]{16,40,200})for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            int length=action==BottleMotion.Action.DRINK?BottleMotion.totalDrinkTicks(duration):BottleMotion.INSPECT_TICKS;
            org.joml.Vector3f previous=null;
            for(double age=0;age<=length;age+=1./3) {
                var rig=BottleRig.solve(BottleMotion.sample(action,age,duration,true),action,age,duration,side,slim);
                var offset=BottleFraming.offset(rig,action,age,duration,true);
                float front=BottleFraming.front(rig,offset,DrinkMotion.camera(DrinkCatalog.ORANGE,action,age,duration).rotation(),action,age,duration,true);
                assertTrue(front<=-BottleFraming.MIN_ARM_DEPTH+1e-5,action+" clips at "+age);
                assertTrue(BottleFraming.bottleFront(rig,offset,DrinkMotion.camera(DrinkCatalog.ORANGE,action,age,duration).rotation(),action,age,duration,true)
                    <=-BottleFraming.MIN_DEPTH+1e-5,"bottle too close at "+age);
                if(previous!=null)assertTrue(previous.distance(offset)<.035,"camera distance pops at "+action+" "+age+" from "+previous+" to "+offset);
                previous=offset;
            }
        }
    }
}
