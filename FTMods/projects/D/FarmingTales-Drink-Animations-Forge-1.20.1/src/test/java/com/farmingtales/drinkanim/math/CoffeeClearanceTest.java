package com.farmingtales.drinkanim.math;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import static org.junit.jupiter.api.Assertions.*;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
class CoffeeClearanceTest {
    @Test void neitherSleeveCrossesTheOpenMugAboveItsRim() {
        for(var p:DrinkCatalog.all())if(CupAnimations.coffeePreview(p))
        for(boolean slim:new boolean[]{true,false})for(int side:new int[]{1,-1})
        for(var action:new Action[]{Action.INSPECT,Action.DRINK})for(double t=0;t<=90;t+=.5) {
            var r=BottleRig.solve(DrinkMotion.sample(p,action,t,32,true),action,t,32,side,slim,p);
            var inverse=new Quaternionf(r.bottle().rotation()).conjugate();
            for(var hand:new BottleRig.Transform[]{r.hand(),r.helper()}) {
                if(hand==r.helper()&&r.assistance()<1)continue;
                for(float x:new float[]{slim?-1.75f:-2.25f,0,slim?1.75f:2.25f})
                for(float y:new float[]{-2.25f,-1,0,.25f})for(float z:new float[]{-2.25f,0,2.25f}) {
                    var q=hand.rotation().transform(new Vector3f(x,y,z).mul(BottleRig.ARM_SCALE/16)).add(hand.position());
                    inverse.transform(q.sub(r.bottle().position())).mul(16/p.renderScale()).add(8,4.8f,8);
                    assertFalse(q.y>5.02&&q.x>4.65&&q.x<11.35&&q.z>4.65&&q.z<11.35,
                        p.id()+" "+action+" "+t+" sleeve enters open rim at "+q);
                }
            }
        }
    }
}
