import cn.ft.biomedungeon.core.DungeonPlanner;
import static cn.ft.biomedungeon.core.DungeonApi.*;
import java.util.HashSet;
import java.util.List;

public final class CatalogCoreSmoke {
    public static void main(String[] arguments) {
        var straight = new Template("s8_05", "s8", "biomedungeon:s8_05", "fb8302274c4f0865c721669581e2714363f4ac49f5ebe22a12d3c7dfafd19837", new Vec3i(15,12,43), List.of(new Port("p0_north",new Vec3i(7,2,0),"north",5,3,"dry"),new Port("p1_south",new Vec3i(7,2,42),"south",5,3,"dry")),"normal");
        var corner = new Template("s8_06", "s8", "biomedungeon:s8_06", "a22b7225717bc69a44e1e6400f792dd3e8b67ff11b52fd7114fb96d958fc9470", new Vec3i(27,13,27), List.of(new Port("p0_north",new Vec3i(6,2,0),"north",5,3,"dry"),new Port("p1_east",new Vec3i(26,2,20),"east",5,3,"dry")),"normal");
        var templates = List.of(straight, corner);
        var variants = new HashSet<String>();
        var selectedIds = new HashSet<String>();
        for (int seed = 0; seed < 32; seed++) {
            var request = new Request(seed,"minecraft:overworld",new Vec3i(0,96,0),0,"catalog-v1","s8",6,10000);
            var first = DungeonPlanner.plan(request, templates, List.of());
            var repeat = DungeonPlanner.plan(request, List.of(corner,straight), List.of());
            if (!first.equals(repeat) || first.rooms().size()!=6 || first.links().size()!=5) {
                throw new AssertionError("real catalog planning/reproducibility failed seed="+seed);
            }
            variants.add(first.rooms().toString());
            first.rooms().forEach(room -> selectedIds.add(room.templateId()));
        }
        if (variants.size()<2 || selectedIds.size()!=2) {
            throw new AssertionError("not genuine room-level template variation");
        }
        System.out.println("PASS real catalog + core snapshot: 32 seeds, six rooms, five links, stable reversed pool; layoutVariants="+variants.size()+" selectedTemplateIds="+selectedIds);
    }
}
