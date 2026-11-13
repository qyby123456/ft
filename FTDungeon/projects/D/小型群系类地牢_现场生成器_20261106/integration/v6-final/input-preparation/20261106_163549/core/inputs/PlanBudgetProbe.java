import cn.ft.biomedungeon.core.DungeonApi;
import cn.ft.biomedungeon.core.next.DungeonYPlanner;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PlanBudgetProbe {
    public static void main(String[] arguments) throws Exception {
        Path snapshot=Path.of(arguments[0]); long seed=Long.parseLong(arguments[1]);
        Gson json=new Gson(); List<DungeonApi.Template> templates=new ArrayList<>();
        for(String name:List.of("baseline.json","expansion.json")) {
            var root=JsonParser.parseString(Files.readString(snapshot.resolve("runtime/src/main/resources/data/ftbd_candidate/"+name))).getAsJsonObject();
            for(var value:root.getAsJsonArray("templates")) { templates.add(json.fromJson(value,DungeonApi.Template.class)); }
        }
        var request=new DungeonApi.Request(seed,"minecraft:overworld",new DungeonApi.Vec3i(1000,200,1000),3,"runtime-v4-unified-candidate1-planner-y-v2","s8",9,50000);
        var plan=DungeonYPlanner.plan(request,templates,List.of(),new DungeonYPlanner.Limits(164,316,64,true,3));
        int minimumX=Integer.MAX_VALUE,minimumY=Integer.MAX_VALUE,minimumZ=Integer.MAX_VALUE,maximumX=Integer.MIN_VALUE,maximumY=Integer.MIN_VALUE,maximumZ=Integer.MIN_VALUE;
        for(var room:plan.rooms()) { var bounds=room.bounds(); minimumX=Math.min(minimumX,bounds.min().x()); minimumY=Math.min(minimumY,bounds.min().y()); minimumZ=Math.min(minimumZ,bounds.min().z()); maximumX=Math.max(maximumX,bounds.maxExclusive().x()); maximumY=Math.max(maximumY,bounds.maxExclusive().y()); maximumZ=Math.max(maximumZ,bounds.maxExclusive().z()); }
        for(var link:plan.links()) { for(var point:link.waypoints()) { minimumX=Math.min(minimumX,point.x()-8); minimumY=Math.min(minimumY,point.y()-2); minimumZ=Math.min(minimumZ,point.z()-8); maximumX=Math.max(maximumX,point.x()+9); maximumY=Math.max(maximumY,point.y()+10); maximumZ=Math.max(maximumZ,point.z()+9); } }
        long volume=(long)(maximumX-minimumX+6)*(maximumY-minimumY+6)*(maximumZ-minimumZ+6);
        System.out.println(json.toJson(Map.of("request",request,"plan",plan,"envelopeMin",List.of(minimumX-3,minimumY-3,minimumZ-3),"envelopeMaxExclusive",List.of(maximumX+3,maximumY+3,maximumZ+3),"cells",volume,"fitsRuntimeBudget",maximumX-minimumX<=256 && maximumY-minimumY<=96 && maximumZ-minimumZ<=256 && volume<=1500000)));
    }
}
