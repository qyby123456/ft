package com.farmingtales.drinkanim.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class EatingMotionTest {
    private static Vector3f relative(BottleRig.Transform root,BottleRig.Transform child) {
        return new Quaternionf(root.rotation()).conjugate().transform(new Vector3f(child.position()).sub(root.position()));
    }
    private static BottleRig.Frame rig(DrinkProfile p,double t,boolean slim,int side) {
        return BottleRig.solve(DrinkMotion.sample(p,Action.DRINK,t,16,true),Action.DRINK,t,16,side,slim,p);
    }
    @Test void allContactsSurviveEveryStyleOnBothArmShapesAndHands() {
        for(var p:DrinkCatalog.all())for(boolean slim:new boolean[]{true,false})for(int side:new int[]{-1,1})
            for(int variant=0;variant<EatingMotion.VARIANTS;variant++)for(double t:new double[]{0,24,34,40,49,62,80,91,100}) {
                var base=rig(p,t,slim,side);var out=EatingMotion.apply(base,p,Action.DRINK,t,16,variant,side);
                var before=new BottleRig.Transform[]{base.hand(),base.helper(),base.cork()};
                var after=new BottleRig.Transform[]{out.hand(),out.helper(),out.cork()};
                for(int i=0;i<before.length;i++) {
                    assertTrue(relative(base.bottle(),before[i]).distance(relative(out.bottle(),after[i]))<.00002,p.id());
                    var q=new Quaternionf(base.bottle().rotation()).conjugate().mul(before[i].rotation());
                    var r=new Quaternionf(out.bottle().rotation()).conjugate().mul(after[i].rotation());
                    assertTrue(Math.abs(q.dot(r))>.99999,p.id());
                }
                assertEquals(base.assistance(),out.assistance());
            }
    }
    @Test void variantsAreDistinctContinuousAndStayWithinComfortLimits() {
        for(String id:new String[]{"orange_wine","brandy_xo","mozhao","orange_juice_1","ancient_fruit_jam_1"}) {
            var p=DrinkCatalog.find("farmingtales:"+id);var base=rig(p,57,true,1);
            for(int v=0;v<4;v++) {
                for(int other=v+1;other<4;other++)
                    assertTrue(EatingMotion.apply(base,p,Action.DRINK,57,16,v,1).hand().position()
                        .distance(EatingMotion.apply(base,p,Action.DRINK,57,16,other,1).hand().position())>.006,id);
                for(int duration:new int[]{1,16,40,200})for(double t=0;t<=60+BottleMotion.sipTicks(duration);t+=.25) {
                    var now=EatingMotion.apply(base,p,Action.DRINK,t,duration,v,1);
                    var next=EatingMotion.apply(base,p,Action.DRINK,t+.25,duration,v,1);
                    assertTrue(now.hand().position().distance(base.hand().position())<.034,id);
                    assertTrue(now.hand().position().distance(next.hand().position())<.003,id);
                    var camera=EatingMotion.camera(p,Action.DRINK,t,duration,v);
                    var following=EatingMotion.camera(p,Action.DRINK,t+.25,duration,v);
                    assertTrue(Math.abs(camera.pitch())<=.32&&Math.abs(camera.yaw())<=.20&&Math.abs(camera.roll())<=.045,id);
                    assertTrue(Math.abs(camera.pitch()-following.pitch())<.035,id);
                    assertEquals(camera,EatingMotion.camera(p,Action.DRINK,t,duration,v));
                }
            }
        }
    }
    @Test void openingsOtherActionsAndReplacementRemainUnchanged() {
        var zero=new BottleMotion.Camera(0,0,0);
        for(var p:DrinkCatalog.all())for(int v=0;v<4;v++) {
            var base=rig(p,20,true,1);
            for(var action:Action.values())if(action!=Action.DRINK) {
                assertSame(base,EatingMotion.apply(base,p,action,45,16,v,1));
                assertEquals(zero,EatingMotion.camera(p,action,45,16,v));
            }
            for(double t:new double[]{0,p.cup()?0:33,100,110,140}) {
                var out=EatingMotion.apply(base,p,Action.DRINK,t,16,v,1);
                assertEquals(base.hand().position(),out.hand().position());
                var camera=EatingMotion.camera(p,Action.DRINK,t,16,v);
                assertEquals(0,camera.pitch(),1e-12);assertEquals(0,camera.yaw(),1e-12);assertEquals(0,camera.roll(),1e-12);
            }
        }
    }
    @Test void framingUsesTheSelectedCameraAndKeepsShouldersOffscreen() {
        for(String id:new String[]{"orange_wine","brandy_xo","mozhao","orange_juice_1","ancient_fruit_jam_1"}) {
            var p=DrinkCatalog.find("farmingtales:"+id);
            for(boolean slim:new boolean[]{true,false})for(int v=0;v<4;v++)for(double t=34;t<100;t+=.5) {
                var frame=BreathingMotion.apply(EatingMotion.apply(rig(p,t,slim,1),p,Action.DRINK,t,16,v,1),t,1);
                var camera=DrinkMotion.camera(p,Action.DRINK,t,16,v).rotation();
                var offset=BottleFraming.offset(frame,Action.DRINK,t,16,true,p,camera);
                assertTrue(BottleFraming.bottleFront(frame,offset,camera,Action.DRINK,t,16,true,p)<=-BottleFraming.MIN_DEPTH+.0001,id);
                assertTrue(BottleFraming.shoulderExposure(frame.hand(),offset,camera)<0,id);
                if(frame.assistance()>0)assertTrue(BottleFraming.shoulderExposure(frame.helper(),offset,camera)<0,id);
            }
        }
    }
    @Test void everyBagVisitsAllFourStylesWithoutBoundaryRepeats() {
        for(long seed=0;seed<50;seed++) {
            var selector=new EatingVariants(new Random(seed));int last=-1;
            for(int bag=0;bag<50;bag++) {
                Set<Integer> found=new HashSet<>();
                for(int i=0;i<4;i++) {int next=selector.next();assertNotEquals(last,next);assertTrue(next>=0&&next<4);found.add(next);last=next;}
                assertEquals(4,found.size());
            }
        }
    }
}
