package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;

class LiquidSurfaceTest {
    @Test void bottleAndCupResponsesIncreaseButHeadMotionStillSaturates() {
        var rotation=new Quaternionf();
        var baseline=LiquidSurface.normal(rotation,0,.05,0,0);
        for(var p:DrinkCatalog.all()) {
            var n=LiquidSurface.normal(p,rotation,0,.05,0,0);
            double gain=(n.x()/n.y())/(baseline.x()/baseline.y());
            if(p.food())assertEquals(0,gain,1e-8,p.id()+" keeps custard surface rigid");
            else if(p.jam())assertTrue(gain>.65&&gain<.8,p.id()+" needs visible slosh with time-domain viscosity");
            else if(p.can())assertEquals(1,gain,1e-8,p.id());
            else if(p.cup())assertTrue(gain>1.15&&gain<1.25,p.id());
            else assertTrue(gain>1.4&&gain<1.5,p.id());
            float limit=p.food()?25:p.jam()?28:p.cup()?30:p.can()?25:35;
            for(int sign:new int[]{-1,1})
                assertEquals(LiquidSurface.normal(p,rotation,limit*sign,.1,.1,.01),
                    LiquidSurface.normal(p,rotation,90*sign,.1,.1,.01),p.id());
            var extreme=LiquidSurface.normal(p,rotation,0,100,-100,.1);
            assertTrue(Math.hypot(extreme.x(),extreme.z())/extreme.y()<=.270001,p.id());
        }
    }
    @Test void headPitchStopsChangingLiquidAfterTheLimit() {
        var q=new Quaternionf().rotateXYZ(.3f,.4f,.2f);
        for(float sign:new float[]{-1,1})
            assertEquals(LiquidSurface.normal(q,25*sign,.1,.1,.01),LiquidSurface.normal(q,90*sign,.1,.1,.01));
    }
    @Test void hugeSloshCannotPullTheSurfaceUnboundedly() {
        var n=LiquidSurface.normal(new Quaternionf(),0,100,100,0);
        assertEquals(LiquidSurface.MAX_SLOSH,Math.hypot(n.x(),n.z())/n.y(),1e-8);
    }
    @Test void cupsKeepTheirVolumeAndEverySurfacePointBelowTheRimEvenWhenInverted() {
        for(var p:DrinkCatalog.all())if(p.cup())
            for(double fill:new double[]{.025,.4,.8,.98})for(int angle=0;angle<360;angle+=30) {
                var raw=LiquidSurface.normal(p,new Quaternionf().rotateXYZ((float)Math.toRadians(angle),.3f,.5f),90,.8,-.8,.1);
                var n=LiquidSurface.contained(p,raw,fill);
                assertTrue(n.y()>=Math.cos(Math.toRadians(60))-1e-8,p.id());
                n=local(p,n);
                double level=ContainerLiquid.level(p.liquid(),n,fill),volume=0,total=0;
                for(var r:p.liquid()) {
                    total+=ContainerLiquid.volume(r);
                    volume+=ContainerLiquid.volume(r)*LiquidMesh.fractionBelow(n,level,r.minimum(),r.maximum());
                    for(var face:LiquidMesh.clip(n,level,r.minimum(),r.maximum()))
                        for(var v:face.points())assertTrue(v.y()<=p.mouthY()/16.0+1e-7,p.id()+" overflows at "+angle);
                }
                assertEquals(fill,volume/total,1e-7,p.id());
            }
    }
    @Test void fullCupsCanReachTheirActualInnerRimInEveryDirection() {
        for(var p:DrinkCatalog.all())if(p.cup())for(int angle=0;angle<360;angle+=15) {
            double a=Math.toRadians(angle);
            var n=local(p,LiquidSurface.contained(p,new LiquidMesh.V(Math.cos(a),-.2,Math.sin(a)),.8));
            double level=ContainerLiquid.level(p.liquid(),n,.8),peak=Double.NEGATIVE_INFINITY;
            for(var r:p.liquid())for(var face:LiquidMesh.clip(n,level,r.minimum(),r.maximum()))
                if(face.surface())for(var v:face.points())peak=Math.max(peak,v.y());
            double rim=p.liquid().stream().mapToDouble(r->r.max()[1]).max().orElseThrow();
            assertEquals(rim,peak,.001/16,p.id()+" must reach rim without a pixel of empty headroom, direction "+angle);
        }
    }
    @Test void cupSloshCanReachTheRimInsteadOfFreezingAtAnEmptyPixel() {
        for(var p:DrinkCatalog.all())if(p.cup()) {
            double maxSlope=0,maxChange=0;LiquidMesh.V first=null;
            for(int t=0;t<=20;t++) {
                var n=LiquidSurface.contained(p,LiquidSurface.normal(p,new Quaternionf(),0,t*.06,-t*.035,.02),.8);
                if(first==null)first=n;
                maxChange=Math.max(maxChange,Math.sqrt(n.sub(first).dot(n.sub(first))));
                maxSlope=Math.max(maxSlope,Math.hypot(n.x(),n.z())/n.y());
            }
            var r=p.liquid().get(0);
            double diagonal=Math.hypot(r.max()[0]-r.min()[0],r.max()[2]-r.min()[2])*.5;
            double available=(r.max()[1]-r.min()[1])*.2;
            assertTrue(maxSlope>Math.min(.18,available/diagonal*.8),p.id()+" visible tilt within this cup's headroom");
            assertTrue(maxChange>.06,p.id()+" visible inspection movement");
        }
    }
    private static LiquidMesh.V local(DrinkProfile p,LiquidMesh.V n) {
        double a=Math.toRadians(-p.cupShape().liquidYaw());
        return new LiquidMesh.V(Math.cos(a)*n.x()+Math.sin(a)*n.z(),n.y(),-Math.sin(a)*n.x()+Math.cos(a)*n.z());
    }
    @Test void cupsDrinkLowerWithoutMovingTheIdleHold() {
        for(var p:DrinkCatalog.all())if(p.cup()) {
            assertEquals(BottleMotion.IDLE.y()+.12,DrinkMotion.sample(p,BottleMotion.Action.IDLE,0,16,true).y(),1e-8);
            assertTrue(DrinkMotion.sample(p,BottleMotion.Action.DRINK,48,16,true).y()<-.35,p.id());
        }
    }
}
