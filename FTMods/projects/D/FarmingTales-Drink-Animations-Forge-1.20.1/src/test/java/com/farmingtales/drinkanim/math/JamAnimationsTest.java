package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;

class JamAnimationsTest {
    private static final DrinkProfile JAM=DrinkCatalog.find("farmingtales:ancient_fruit_jam_1");
    @Test void helperOnlyOpensTheJarAndNeverWipes() {
        for(int use:new int[]{16,64}) {
            assertEquals(1,JamAnimations.assistance(BottleMotion.Action.DRINK,20,use));
            for(double t=44;t<=BottleMotion.totalDrinkTicks(use);t+=.25)
                assertEquals(0,JamAnimations.assistance(BottleMotion.Action.DRINK,t,use));
            assertTrue(JamAnimations.eatingSounds(use).stream().noneMatch(c->c.event().equals("cloth")));
        }
    }
    @Test void eatingAndBothJoltsKeepThePalmAnchoredWhileTheElbowBends() {
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1})for(int use:new int[]{16,64}) {
            Vector3f reference=null;
            for(double t=46;t<=48+BottleMotion.sipTicks(use);t+=.1) {
                var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,use,true);
                var rig=BottleRig.solve(pose,BottleMotion.Action.DRINK,t,use,side,slim,JAM);
                var inverse=new Quaternionf(rig.bottle().rotation()).conjugate();
                var local=inverse.transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
                var rotation=new Quaternionf(inverse).mul(rig.hand().rotation());
                if(reference==null)reference=local;
                assertTrue(local.distance(reference)<1e-6,"Palm slides while eating or shaking");
            }
        }
    }
    @Test void singleHandInspectionKeepsTheJarSealed() {
        assertEquals(86,AnimationPlayback.ticks(JAM,BottleMotion.Action.INSPECT,16));

        Vector3f capLocal=null;
        for(int t=0;t<=86;t++) {
            assertFalse(DrinkMotion.capDetached(JAM,BottleMotion.Action.INSPECT,t));
            assertTrue(DrinkMotion.capVisible(JAM,BottleMotion.Action.INSPECT,t,16,true));
            var pose=DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,t,16,true);
            assertTrue(Math.abs(pose.yaw()-155)<12&&Math.abs(pose.pitch())<26&&Math.abs(Math.IEEEremainder(pose.roll(),360))<181,"Inspect must not scan sides or bottom");
            var rig=BottleRig.solve(pose,BottleMotion.Action.INSPECT,t,16,1,true,JAM);
            var cap=new Quaternionf(rig.bottle().rotation()).conjugate().transform(new Vector3f(rig.cork().position()).sub(rig.bottle().position()));
            if(capLocal==null)capLocal=cap;
            assertTrue(capLocal.distance(cap)<1e-6,"Lid moves relative to sealed jar");
            assertFalse(JamAnimations.open(BottleMotion.Action.INSPECT,t,16,true));
        }
        assertTrue(Math.abs(BottleRig.rotation(BottleMotion.IDLE,1).dot(BottleRig.rotation(DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,86,16,true),1)))>1-1e-6);
    }
    @Test void grippingPalmAndStopperCornerStayAttachedDuringThePull() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            Vector3f last=null,lastHand=null;
            for(double t=0;t<=BottleMotion.totalDrinkTicks(16);t+=.05) {
                var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true);
                var rig=BottleRig.solve(pose,BottleMotion.Action.DRINK,t,16,side,slim,JAM);
                Vector3f relative=new Quaternionf(rig.bottle().rotation()).conjugate()
                    .transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
                assertTrue(Float.isFinite(relative.x)&&Float.isFinite(relative.y)&&Float.isFinite(relative.z));
                if(last!=null)assertTrue(last.distance(rig.bottle().position())<.10,"Jar discontinuity at "+t);
                if(lastHand!=null)assertTrue(lastHand.distance(rig.hand().position())<.08,"Gripping hand changes contact abruptly at "+t);
                last=new Vector3f(rig.bottle().position());
                lastHand=new Vector3f(rig.hand().position());
                if(t>=15&&t<=32)assertTrue(rig.helper().position().distance(rig.cork().position())<.46,"Corner grip separated");
            }
        }
    }
    @Test void jamSticksUntilTheSingleHandJoltAndThenFlows() {
        for(int duration:new int[]{16,64}) {
            double span=BottleMotion.sipTicks(duration);
            var first=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,48+.3*span,duration,true);
            var stuck=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,48+.5*span,duration,true);
            var second=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,48+.85*span,duration,true);
            assertTrue(first.pitch()>100&&first.pitch()<113&&second.pitch()>104&&second.pitch()<122,"Tip the short jar towards the mouth with room for the inertial rebound");
            assertTrue(first.z()>-.70&&first.z()<-.55,"Short-jar eating stays close to the mouth with room for the forearm");
            assertEquals(first.fill(),stuck.fill(),1e-6,"Remaining jam sticks before shaking");
            assertTrue(second.fill()<stuck.fill());
            assertEquals(0,DrinkMotion.assistance(JAM,BottleMotion.Action.DRINK,48+.85*span,duration),1e-6);
            var cues=AnimationSoundTimeline.drinking(JAM,duration);
            assertEquals(2,cues.stream().filter(c->c.event().equals("jam_eat")).count());
            assertEquals(0,cues.stream().filter(c->c.event().equals("jam_pat")).count());
            assertEquals(4,cues.stream().filter(c->c.event().equals("inspect_turn")).count());
            double prior=.8;
            for(double t=48;t<48+span;t+=.1) {
                double fill=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,duration,true).fill();
                assertTrue(fill<=prior+1e-8,"Eaten jam must not refill");prior=fill;
            }
        }
    }
    @Test void forearmBeyondTheGrippingFingersRemainsOutsideTheJarCore() {
        for(boolean slim:new boolean[]{false,true})for(var action:new BottleMotion.Action[]{BottleMotion.Action.INSPECT,BottleMotion.Action.DRINK})
            for(double t=0;t<(action==BottleMotion.Action.INSPECT?70:88);t+=.25) {
                var pose=DrinkMotion.sample(JAM,action,t,16,true);var rig=BottleRig.solve(pose,action,t,16,1,slim,JAM);
                var inverse=new Quaternionf(rig.bottle().rotation()).conjugate();
                for(double along:new double[]{-4.5,-7,-11}) {
                    var center=new Quaternionf(rig.hand().rotation()).transform(new Vector3f(0,(float)(along*BottleRig.ARM_SCALE/16),0)).add(rig.hand().position());
                    var local=inverse.transform(center.sub(rig.bottle().position())).div(JAM.renderScale()/16).add(8,4.8f,8);
                    new Quaternionf().rotateY((float)Math.toRadians(22.5)).transform(local.sub(8,3.35f,8)).add(8,3.35f,8);
                    boolean inside=local.x>5.7&&local.x<10.3&&local.y>.3&&local.y<5.8&&local.z>5.7&&local.z<10.3;
                    assertFalse(inside,"Forearm centre penetrates the jar at "+action+" "+t);
                }
            }
    }
    @Test void lowerForearmStaysOutsideWhileThePalmUsesTheCanGrasp() {
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1})for(double t=10;t<=48+BottleMotion.sipTicks(16)+5;t+=.25) {
            var motion=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true);
            var rig=BottleRig.solve(motion,BottleMotion.Action.DRINK,t,16,side,slim,JAM);
            var inverse=new Quaternionf(rig.bottle().rotation()).rotateY((float)Math.toRadians(-22.5)).conjugate();
            var vertices=new java.util.ArrayList<Vector3f>();
            for(float x:new float[]{slim?-1.75f:-2.25f,slim?1.75f:2.25f})for(float y:new float[]{-12.25f,-6f})for(float z:new float[]{-2.25f,2.25f}) {
                var vertex=new Quaternionf(rig.hand().rotation()).transform(new Vector3f(x,y,z).mul(BottleRig.ARM_SCALE/16)).add(rig.hand().position());
                inverse.transform(vertex.sub(rig.bottle().position())).div(JAM.renderScale()/16).add(8,4.8f,8);
                vertices.add(vertex);
            }
            var basis=new Vector3f[]{new Vector3f(1,0,0),new Vector3f(0,1,0),new Vector3f(0,0,1)};
            var axes=new java.util.ArrayList<Vector3f>(java.util.List.of(basis));
            for(var axis:basis) {
                var handAxis=new Quaternionf(inverse).mul(rig.hand().rotation()).transform(new Vector3f(axis));
                axes.add(handAxis);
                for(var glassAxis:basis)axes.add(new Vector3f(handAxis).cross(glassAxis));
            }
            var core=new java.util.ArrayList<Vector3f>();
            for(float x:new float[]{5.7f,10.3f})for(float y:new float[]{.3f,5.8f})for(float z:new float[]{5.7f,10.3f})core.add(new Vector3f(x,y,z));
            boolean separate=false;
            for(var axis:axes) {
                if(axis.lengthSquared()<1e-8)continue;axis.normalize();
                double min=vertices.stream().mapToDouble(v->v.dot(axis)).min().orElseThrow(),max=vertices.stream().mapToDouble(v->v.dot(axis)).max().orElseThrow();
                double low=core.stream().mapToDouble(v->v.dot(axis)).min().orElseThrow(),high=core.stream().mapToDouble(v->v.dot(axis)).max().orElseThrow();
                separate|=max<=low||min>=high;
            }
            assertTrue(separate,"Lower forearm intersects jar core: "+slim+" side="+side+" t="+t);

        }
    }
    @Test void shakingTheJarDoesNotPushItBelowTheViewport() {
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1})for(int use:new int[]{16,64})
            for(double t=48;t<=48+BottleMotion.sipTicks(use);t+=.25) {
                var action=BottleMotion.Action.DRINK;
                var pose=DrinkMotion.sample(JAM,action,t,use,true);
                var rig=BottleRig.solve(pose,action,t,use,side,slim,JAM);
                var offset=BottleFraming.offset(rig,action,t,use,true,JAM);
                var center=new Quaternionf(rig.bottle().rotation()).transform(new Vector3f(0,(3.1f-4.8f)*JAM.renderScale()/16,0))
                    .add(rig.bottle().position()).add(offset);
                DrinkMotion.camera(JAM,action,t,use).rotation().transform(center);
                assertTrue(center.z<-.28f&&center.y>.70f*center.z+.04f,
                    "Jar must remain above the lower viewport edge while shaking: "+slim+" side="+side+" t="+t);
            }
    }
    @Test void surfaceTextureUsesItsOwnFullTileAndTossReturnsToTheWineGrip() {
        assertTrue(JAM.scale()<1.05);
        var r=JAM.liquid().get(0);
        var n=new LiquidMesh.V(0,1,0);
        var face=new LiquidMesh.Face(java.util.List.of(r.minimum(),r.maximum()),n,true);
        var a=LiquidUV.sample(r,face,r.minimum());var b=LiquidUV.sample(r,face,r.maximum());
        assertEquals(.5,Math.abs(b.u()-a.u()),1e-6);
        assertEquals(.5,a.u(),1e-6);
        assertEquals(1,Math.abs(b.v()-a.v()),1e-6);
        var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRAW,9.5,16,true);
        var rig=BottleRig.solve(pose,BottleMotion.Action.DRAW,9.5,16,1,true,JAM);
        var reference=BottleRig.solve(BottleMotion.IDLE,BottleMotion.Action.IDLE,0,16,1,true,JAM);
        var grip=new Quaternionf(rig.bottle().rotation()).conjugate().transform(new Vector3f(rig.hand().position()).sub(rig.bottle().position()));
        var restGrip=new Quaternionf(reference.bottle().rotation()).conjugate().transform(new Vector3f(reference.hand().position()).sub(reference.bottle().position()));
        assertTrue(rig.bottle().position().y-rig.hand().position().y>.14,"Short toss must still separate clearly above the receiving palm");
        var caughtPose=DrinkMotion.sample(JAM,BottleMotion.Action.DRAW,19,16,true);
        var caught=BottleRig.solve(caughtPose,BottleMotion.Action.DRAW,19,16,1,true,JAM);
        assertTrue(caught.hand().position().distance(reference.hand().position())<1e-6,"Catch must return to the established grip");
        assertTrue(reference.hand().rotation().equals(BottleRig.armRotation(ArmMotion.REST,1,false),1e-5f),"Use established wine sleeve orientation");
        assertEquals(.03/16,2.05/16-r.maximum().x(),1e-6,"Jam fits just inside the thicker glass wall");
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(JAM,BottleMotion.Action.DRAW,19,16,true));
    }
    @Test void jamUsesTheSameFlatClippedSurfaceAsWineWithinTheJar() {
        var r=JAM.liquid().get(0);
        for(double fill:new double[]{.8,.4,.055})for(double tilt:new double[]{-.18,0,.18}) {
            var n=new LiquidMesh.V(tilt,1,tilt*.5).unit();
            double level=ContainerLiquid.level(JAM.liquid(),n,fill);
            var mesh=ContainerLiquid.exposedFaces(JAM.liquid(),r,n,level);
            assertEquals(1,mesh.stream().filter(LiquidMesh.Face::surface).count());
            for(var face:mesh)for(var point:face.points()) {
                assertTrue(point.x()>=r.min()[0]-1e-8&&point.x()<=r.max()[0]+1e-8);
                assertTrue(point.y()>=r.min()[1]-1e-8&&point.y()<=r.max()[1]+1e-8);
                assertTrue(point.z()>=r.min()[2]-1e-8&&point.z()<=r.max()[2]+1e-8);
                if(face.surface())assertEquals(level,n.dot(point),1e-8,"Surface must have no spikes or crown");
            }
        }
    }
    @Test void viscousSurfaceRespondsGraduallyAndSettlesWithoutRipples() {
        var state=new JamLiquid.State();var tilted=new Quaternionf().rotateZ(1.1f);
        state.sample(0,new Quaternionf(),0,0,0);
        double first=state.sample(1.0/60,tilted,0,.2,0).x();
        LiquidMesh.V moving=null;
        for(int i=2;i<=60;i++)moving=state.sample(i/60.0,tilted,0,.2,0);
        assertTrue(Math.abs(first)<Math.abs(moving.x())*.1,"Jam reacts instantly like water");
        assertTrue(Math.abs(moving.x())>.06,"Jam cannot deform");
        for(int i=61;i<=360;i++) {
            moving=state.sample(i/60.0,new Quaternionf(),0,0,0);
            assertEquals(1,Math.sqrt(moving.dot(moving)),1e-6);
        }
        assertEquals(0,moving.x(),1e-5);assertEquals(0,moving.z(),1e-5);
    }
    @Test void jamTiltStopsAtTheAngleLimitAndTheRim() {
        var region=JAM.liquid().get(0);
        for(double fill:new double[]{.8,.4,.055})for(float pitch:new float[]{-90,0,90})for(int angle=0;angle<360;angle+=5) {
            var target=JamLiquid.target(new Quaternionf().rotateX((float)Math.toRadians(angle)),pitch,.4,-.4);
            var normal=LiquidSurface.contained(JAM,target,fill);
            assertTrue(normal.y()>=Math.cos(Math.toRadians(JamLiquid.MAX_TILT_DEGREES))-1e-6,"Liquid turns into a vertical sheet");
            var n=new Quaternionf().rotateY((float)Math.toRadians(22.5)).transform(new Vector3f((float)normal.x(),(float)normal.y(),(float)normal.z()));
            var local=new LiquidMesh.V(n.x,n.y,n.z);double level=ContainerLiquid.level(JAM.liquid(),local,fill);
            for(double x:new double[]{region.min()[0],region.max()[0]})
                for(double z:new double[]{region.min()[2],region.max()[2]})
                    assertTrue((level-local.x()*x-local.z()*z)/local.y()<=region.max()[1]+1e-5,"Surface exceeds the rim");
        }
        var left=JamLiquid.target(new Quaternionf(),0,-.4,0);var right=JamLiquid.target(new Quaternionf(),0,.4,0);
        assertTrue(right.x()-left.x()>.3,"Left/right slosh is imperceptible");
    }
    @Test void inspectionPresentsTheJarAtTheCanDistance() {
        for(double t=0;t<=86;t+=.1) {
            var pose=DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,t,16,true);
            assertTrue(pose.z()<=BottleMotion.IDLE.z()+.001,"Inspection pushes the jar into the camera");
            assertTrue(pose.x()>.30,"Jar leaves its right-front presentation");
        }
    }
    @Test void tossGraspArrivesFromTheRightAndStaysAttachedAfterCatching() {
        for(boolean slim:new boolean[]{false,true})for(int side:new int[]{-1,1}) {
            double previous=Double.POSITIVE_INFINITY;
            for(double t=8.6;t<=12;t+=.1) {
                var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRAW,t,16,true);
                var frame=BottleRig.solve(pose,BottleMotion.Action.DRAW,t,16,side,slim,JAM);
                double horizontal=side*(frame.hand().position().x-frame.bottle().position().x);
                assertTrue(horizontal<=previous+1e-5,"Receiving hand moves away during the catch");previous=horizontal;
            }
            Vector3f contact=null;
            for(double t=12;t<=19;t+=.1) {
                var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRAW,t,16,true);
                var frame=BottleRig.solve(pose,BottleMotion.Action.DRAW,t,16,side,slim,JAM);
                var local=new Quaternionf(frame.bottle().rotation()).conjugate().transform(new Vector3f(frame.hand().position()).sub(frame.bottle().position()));
                if(contact==null)contact=local;
                assertTrue(local.distance(contact)<1e-6,"Catch changes contact during the recoil");
            }
        }
        assertEquals(10,AnimationPlayback.ticks(JAM,BottleMotion.Action.DRAW,16));
        assertEquals(180,DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,24,16,true).roll()+8,1e-7);
        assertEquals(360,DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,86,16,true).roll(),1e-7);
    }
    @Test void threeTerminalShakesFinishBeforeTheJarIsLowered() {
        var rest=new BottleMotion.Pose(0,0,0,0,0,0,.8);int troughs=0;
        for(double t=27;t<39;t+=.05) {
            double a=InertialGestures.jamShake(t-.05,rest).y(),b=InertialGestures.jamShake(t,rest).y(),c=InertialGestures.jamShake(t+.05,rest).y();
            if(b<a&&b<c&&b<-.02)troughs++;
        }
        assertEquals(3,troughs);
        assertEquals(rest,InertialGestures.jamShake(40,rest));
    }
    @Test void removedStopperMovesWithTheLeftHandInsteadOfFollowingTheRisingJar() {
        Quaternionf rotation=null;
        for(double t=27.1;t<=33;t+=.1) {
            var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true);
            var rig=BottleRig.solve(pose,BottleMotion.Action.DRINK,t,16,1,true,JAM);
            if(rotation==null)rotation=new Quaternionf(rig.cork().rotation());
            assertTrue(Math.abs(rotation.dot(rig.cork().rotation()))>1-1e-6,"Removed stopper follows the jar tilt");
            assertTrue(rig.helper().position().distance(rig.cork().position())<.46,"Left hand loses the stopper");
        }
    }
    @Test void stopperStaysSeatedDuringStrainAndHandsSeparateInOppositeDirectionsOnRelease() {
        for(int side:new int[]{-1,1})for(boolean slim:new boolean[]{false,true}) {
            Vector3f seated=null;
            for(double t=15;t<=27;t+=.1) {
                var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true);
                var rig=BottleRig.solve(pose,BottleMotion.Action.DRINK,t,16,side,slim,JAM);
                var local=new Quaternionf(rig.bottle().rotation()).conjugate().transform(new Vector3f(rig.cork().position()).sub(rig.bottle().position()));
                if(seated==null)seated=local;
                assertTrue(local.distance(seated)<1e-6,"Tight stopper floats away before the pull");
            }
            var before=BottleRig.solve(DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,27,16,true),BottleMotion.Action.DRINK,27,16,side,slim,JAM);
            var after=BottleRig.solve(DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,29.5,16,true),BottleMotion.Action.DRINK,29.5,16,side,slim,JAM);
            assertTrue(after.cork().position().y-before.cork().position().y>.115,"Pull does not carry stopper upward");
            assertTrue(after.bottle().position().y<before.bottle().position().y-.01,"Braced jar must absorb the release downward");
        }
    }
    @Test void wholeStopperPullFollowsTheNeckAxisWithoutPrying() {
        for(int side:new int[]{-1,1}) {
            var initial=BottleRig.solve(DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,27,16,true),BottleMotion.Action.DRINK,27,16,side,true,JAM);
            var axis=new Quaternionf(initial.cork().rotation()).transform(new Vector3f(0,1,0));
            for(double t=27;t<=30;t+=.1) {
                var rig=BottleRig.solve(DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true),BottleMotion.Action.DRINK,t,16,side,true,JAM);
                var delta=new Vector3f(rig.cork().position()).sub(initial.cork().position());
                assertTrue(new Vector3f(delta).cross(axis).length()<1e-6,"Stopper is levered sideways during extraction");
                assertTrue(Math.abs(initial.cork().rotation().dot(rig.cork().rotation()))>1-1e-6,"Stopper twists during extraction");
            }
        }
    }
    @Test void openingHelperStaysOnTheLeftOfTheStopperAndAwayFromTheHoldingHand() {
        for(boolean slim:new boolean[]{false,true})for(double t=15;t<=27;t+=.1) {
            var rig=BottleRig.solve(DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true),BottleMotion.Action.DRINK,t,16,1,slim,JAM);
            assertTrue(rig.helper().position().x<rig.cork().position().x-.045,"Left hand reaches across the front of the jar");
            assertTrue(rig.hand().position().distance(rig.helper().position())>.16,"Opening palms overlap each other");
        }
    }
    @Test void throwingHandFollowsThroughUpwardWithoutRetractingFromTheJar() {
        for(boolean slim:new boolean[]{false,true}) {
            Vector3f release=null;
            for(double t=20;t<=24;t+=.1) {
                var pose=DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,t,16,true);
                var rig=BottleRig.solve(pose,BottleMotion.Action.INSPECT,t,16,1,slim,JAM);
                if(release==null)release=new Vector3f(rig.hand().position());
                assertTrue(rig.hand().position().y>=release.y-1e-5,"Hand snaps downward immediately after launch");
                assertTrue(Math.abs(rig.hand().position().x-release.x)<.035,"Hand darts aside after launch");
                assertTrue(rig.hand().position().z-release.z<.02,"Hand retracts toward camera after launch");
            }
        }
    }
    @Test void eatingJarIsSlightlyDiagonalAndTheMouthPointsDownToPour() {
        for(double t=48;t<=48+BottleMotion.sipTicks(16);t+=.1) {
            var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true);
            var rotation=BottleRig.rotation(pose,1);
            var mouth=new Quaternionf(rotation).transform(new Vector3f(0,1,0));
            var edge=new Quaternionf(rotation).rotateY((float)Math.toRadians(-22.5)).transform(new Vector3f(0,0,1));
            double angle=Math.toDegrees(Math.atan2(edge.y,edge.x));
            assertTrue(mouth.y<-.10&&mouth.z>.80,"Mouth must point slightly down towards the player");
            assertTrue(angle>7&&angle<20,"Keep a natural diagonal instead of a flat or diamond rim");
        }
    }
    @Test void inspectionCatchFeedsStraightIntoTheFirstShake() {
        var low=DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,30,16,true);
        var high=DrinkMotion.sample(JAM,BottleMotion.Action.INSPECT,34,16,true);
        assertTrue(high.y()-low.y()>.06,"Catch settles into an idle pause before the first shake");
        assertEquals(86,AnimationPlayback.ticks(JAM,BottleMotion.Action.INSPECT,16));
    }
    @Test void discardedStopperLeavesTheEmptyHelperHand() {
        for(int side:new int[]{-1,1}) {
            var release=BottleRig.solve(DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,33,16,true),BottleMotion.Action.DRINK,33,16,side,true,JAM);
            var thrown=BottleRig.solve(DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,39,16,true),BottleMotion.Action.DRINK,39,16,side,true,JAM);
            assertTrue(release.cork().position().z-thrown.cork().position().z>.60,"Stopper is not thrown forward");
            assertEquals(release.cork().position().x,thrown.cork().position().x,1e-6,"Forward throw drifts sideways");
            assertTrue(thrown.cork().position().distance(thrown.helper().position())>.40,"Empty hand still follows thrown stopper");
            assertTrue(Math.abs(release.cork().rotation().dot(thrown.cork().rotation()))<.9,"Discarded stopper does not tumble");
        }
    }
    @Test void emptiedJarLeavesBeforeASealedReplacementAppears() {
        int end=BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(16);
        for(int t=end+17;t<=end+22;t++) {
            var pose=DrinkMotion.sample(JAM,BottleMotion.Action.DRINK,t,16,true);
            assertTrue(pose.y()<-2,"Replacement must change out of sight");
        }
        assertFalse(DrinkMotion.capVisible(JAM,BottleMotion.Action.DRINK,58,16,true));
        assertTrue(DrinkMotion.capVisible(JAM,BottleMotion.Action.DRINK,BottleMotion.totalDrinkTicks(16),16,true));
    }
}
