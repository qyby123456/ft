package com.farmingtales.drinkanim.math;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A gently moving foam surface, joined underneath to the more mobile beer. */
public final class BeerFoam {
    private record Texture(String texture,float[] sideUV,float[] surfaceUV) {}
    private record Definition(List<String> items,Map<String,Texture> variants) {}
    public record Surface(LiquidMesh.V normal,double level) {}
    private static final Definition DATA=load();
    private static Definition load() {
        try(var in=BeerFoam.class.getResourceAsStream("/assets/farmingtales_drink_anim/beer_foam.json")) {
            if(in==null)throw new IllegalStateException("Missing beer foam resource");
            return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Definition.class);
        } catch(java.io.IOException e) {throw new IllegalStateException(e);}
    }
    public static boolean supports(DrinkProfile p) {return DATA.items().contains(p.id());}
    public static DrinkProfile.Region region(DrinkProfile p) {
        var r=p.liquid().get(0);
        var texture=DATA.variants().get(p.id());
        return new DrinkProfile.Region(r.min(),new double[]{r.max()[0],9.75/16,r.max()[2]},texture.sideUV(),texture.surfaceUV(),texture.texture());
    }
    public static Surface surface(DrinkProfile p,LiquidMesh.V liquidNormal,double fill,double time) {
        var n=liquidNormal.unit();var r=p.liquid().get(0);
        double x=n.x()/Math.max(.001,n.y())*.18,z=n.z()/Math.max(.001,n.y())*.18;
        double length=Math.hypot(x,z),limit=Math.tan(Math.toRadians(4));
        if(length>limit){x*=limit/length;z*=limit/length;}
        var foamNormal=new LiquidMesh.V(x,1,z).unit();
        double cx=(r.min()[0]+r.max()[0])*.5,cz=(r.min()[2]+r.max()[2])*.5;
        double cy=(ContainerLiquid.level(p.liquid(),n,fill)-n.x()*cx-n.z()*cz)/Math.max(.001,n.y());
        // Nearly empty beer pools in a corner; its plane at the center can lie below the mug floor.
        cy=Math.max(r.min()[1],cy);
        double thickness=(2.25+.025*Math.sin(time*.09)+.015*Math.sin(time*.14+1.2))/16*Math.min(1,fill/.10);
        return new Surface(foamNormal,foamNormal.dot(new LiquidMesh.V(cx,cy+thickness,cz)));
    }
    public static List<LiquidMesh.Face> mesh(DrinkProfile p,LiquidMesh.V normal,double fill,double time) {
        if(!supports(p)||fill<=.001)return List.of();
        var r=region(p);var n=normal.unit();
        double bottom=ContainerLiquid.level(p.liquid(),n,fill);
        var top=surface(p,n,fill,time);
        var result=new ArrayList<LiquidMesh.Face>();
        for(var face:LiquidMesh.clip(top.normal(),top.level(),r.minimum(),r.maximum())) {
            var points=new ArrayList<LiquidMesh.V>();
            var a=face.points().get(face.points().size()-1);double da=n.dot(a)-bottom;
            for(var b:face.points()) {
                double db=n.dot(b)-bottom;
                if((da>=0)!=(db>=0))points.add(a.add(b.sub(a).mul(da/(da-db))));
                if(db>=0)points.add(b);
                a=b;da=db;
            }
            if(points.size()>=3)result.add(new LiquidMesh.Face(points,face.normal(),face.surface()));
        }
        return result;
    }
    private BeerFoam() {}
}
