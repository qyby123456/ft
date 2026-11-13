package com.farmingtales.drinkanim.math;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuangCanTest {
    @Test void liquidAndInnerWallsStayInsideTheSteppedCanAtEveryTilt() throws Exception {
        var profile=DrinkCatalog.find("papercraft_magic_decoration:guang_s");
        assertNotNull(profile);
        for(double fill:new double[]{.025,.3,.8,1})for(int pitch=-170;pitch<=170;pitch+=17)
        for(int yaw=0;yaw<360;yaw+=30) {
            double p=Math.toRadians(pitch),a=Math.toRadians(yaw);
            var n=new LiquidMesh.V(Math.sin(p)*Math.cos(a),Math.cos(p),Math.sin(p)*Math.sin(a));
            double level=ContainerLiquid.level(profile.liquid(),n,fill);
            for(var region:profile.liquid())for(var face:ContainerLiquid.exposedFaces(profile.liquid(),region,n,level))
            for(var v:face.points()) {
                double radius=v.y()>10.00001/16?2.4/16:2.9/16;
                assertTrue(Math.abs(v.x())<radius&&Math.abs(v.z())<radius,"liquid crosses the narrowing can shoulder");
                assertTrue(v.y()<10.95/16,"liquid crosses the lid");
            }
        }
        try(var in=getClass().getResourceAsStream("/assets/farmingtales_drink_anim/models/animation/papercraft/guang_s.json")) {
            var model=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            int walls=0;
            for(var entry:model.getAsJsonArray("elements")) {
                var e=entry.getAsJsonObject();
                if(!e.get("name").getAsString().startsWith("can_inner"))continue;
                walls++;
                for(String field:new String[]{"from","to"}) {
                    var point=e.getAsJsonArray(field);double y=point.get(1).getAsDouble();
                    double radius=y>10?2.5:3;
                    assertTrue(Math.abs(point.get(0).getAsDouble()-8)<radius);
                    assertTrue(Math.abs(point.get(2).getAsDouble()-8)<radius);
                    assertTrue(y<11);
                }
            }
            assertEquals(3,walls);
        }
    }
}
