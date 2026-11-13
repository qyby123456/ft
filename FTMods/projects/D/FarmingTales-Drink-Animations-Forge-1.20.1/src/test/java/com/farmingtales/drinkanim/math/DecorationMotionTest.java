package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;

class DecorationMotionTest {
    @Test void attachmentsStayPinnedWhileFreeEndsRespondAndSettle() {
        for(var p:DrinkCatalog.all())for(var part:DecorationMotion.parts(p)) {
            var state=new DecorationMotion.State();double max=0,after=0;
            for(int f=0;f<720;f++) {
                double t=f/120.0,move=t<.4?.09*Math.sin(Math.PI*t/.4):0;
                state.advance(t,new Vector3f((float)move,0,0),new Quaternionf(),0,0);
                var transform=state.transform(part,p,new LiquidMesh.V(0,1,0),.8);
                assertTrue(transform.offset().isFinite()&&transform.rotation().isFinite());
                max=Math.max(max,Math.abs(state.x())+Math.abs(state.z()));
                if(f==60)after=Math.abs(state.x())+Math.abs(state.z());
                if(part.kind()!=DecorationMotion.Kind.ICE&&part.kind()!=DecorationMotion.Kind.FLOATING_FRUIT)assertEquals(0,transform.offset().length(),1e-8,"Attached roots never translate");
            }
            assertTrue(max>.015,"Acceleration must drive visible secondary motion");
            assertTrue(after>.001,"It must continue after the driving motion stops");
            assertTrue(Math.abs(state.x())+Math.abs(state.z())<1e-8,"Unforced motion decays rather than looping");
        }
    }
    @Test void iceStaysInTheCupThroughEveryGestureAndFillLevel() {
        for(var p:DrinkCatalog.all())for(var part:DecorationMotion.parts(p))if(part.kind()==DecorationMotion.Kind.ICE||part.kind()==DecorationMotion.Kind.FLOATING_FRUIT) {
            for(var action:new Action[]{Action.DRINK,Action.INSPECT,Action.PLACE}) {
                var state=new DecorationMotion.State();var region=p.liquid().get(0);
                var local=new Quaternionf().rotateY((float)Math.toRadians(-p.cupShape().liquidYaw()));
                for(int f=0;f<500;f++) {
                    double age=AnimationPlayback.authored(p,action,f/3.,16);
                    var rig=BottleRig.solve(DrinkMotion.sample(p,action,age,16,true),action,age,16,1,true,p);
                    var normal=new Quaternionf(rig.bottle().rotation()).conjugate().transform(new Vector3f(0,1,0));
                    state.advance(f/60.,rig.bottle().position(),rig.bottle().rotation(),.1,-.1);
                    for(double fill:new double[]{.025,.4,.8}) {
                        var tr=state.transform(part,p,new LiquidMesh.V(normal.x,normal.y,normal.z),fill);
                        for(int i=-1;i<=1;i+=2)for(int j=-1;j<=1;j+=2)for(int k=-1;k<=1;k+=2) {
                            var e=part.extent();var pivot=part.pivot();
                            var corner=tr.rotation().transform(new Vector3f(i*e[0],j*e[1],k*e[2]))
                                .add(tr.offset()).add(pivot[0]-8,pivot[1],pivot[2]-8);
                            local.transform(corner);
                            assertTrue(corner.x>=region.min()[0]*16-.001&&corner.x<=region.max()[0]*16+.001,p.id()+" X wall");
                            assertTrue(corner.z>=region.min()[2]*16-.001&&corner.z<=region.max()[2]*16+.001,p.id()+" Z wall");
                            assertTrue(corner.y>=region.min()[1]*16-.001&&corner.y<=p.mouthY()+.001,p.id()+" bottom/rim");
                        }
                    }
                }
            }
        }
    }
    @Test void discontinuousWorldOrTimeChangesResetTheSpring() {
        var state=new DecorationMotion.State();
        state.advance(0,new Vector3f(),new Quaternionf(),0,0);
        state.advance(.01,new Vector3f(.01f,0,0),new Quaternionf(),1,1);
        state.advance(10,new Vector3f(100,0,0),new Quaternionf(),1,1);
        assertEquals(0,state.x());assertEquals(0,state.z());
        state.advance(1,new Vector3f(),new Quaternionf(),0,0);
        assertEquals(0,state.x());assertEquals(0,state.z());
    }
}
