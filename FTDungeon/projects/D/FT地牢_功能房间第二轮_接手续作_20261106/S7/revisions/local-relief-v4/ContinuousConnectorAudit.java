import com.google.gson.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ContinuousConnectorAudit {
    private static final Map<BlockPos,List<AABB>> cells=new HashMap<>();
    private static final Set<BlockPos> fluids=new HashSet<>();
    private static final double EPS=0.00001;

    private static List<AABB> collisions(AABB region){
        List<AABB> result=new ArrayList<>();
        for(int east=(int)Math.floor(region.minX);east<=Math.floor(region.maxX);east++)
            for(int up=(int)Math.floor(region.minY);up<=Math.floor(region.maxY);up++)
                for(int south=(int)Math.floor(region.minZ);south<=Math.floor(region.maxZ);south++)
                    for(AABB box:cells.getOrDefault(new BlockPos(east,up,south),List.of()))
                        if(box.intersects(region))result.add(box);
        return result;
    }

    private static boolean covered(AABB region,List<AABB> support){
        TreeSet<Double> eastEdges=new TreeSet<>(List.of(region.minX,region.maxX));
        TreeSet<Double> southEdges=new TreeSet<>(List.of(region.minZ,region.maxZ));
        for(AABB box:support){
            eastEdges.add(Math.max(region.minX,Math.min(region.maxX,box.minX)));
            eastEdges.add(Math.max(region.minX,Math.min(region.maxX,box.maxX)));
            southEdges.add(Math.max(region.minZ,Math.min(region.maxZ,box.minZ)));
            southEdges.add(Math.max(region.minZ,Math.min(region.maxZ,box.maxZ)));
        }
        List<Double> eastValues=new ArrayList<>(eastEdges),southValues=new ArrayList<>(southEdges);
        for(int east=1;east<eastValues.size();east++)for(int south=1;south<southValues.size();south++){
            double eastMiddle=(eastValues.get(east-1)+eastValues.get(east))/2;
            double southMiddle=(southValues.get(south-1)+southValues.get(south))/2;
            boolean found=false;
            for(AABB box:support)if(eastMiddle>=box.minX-EPS&&eastMiddle<=box.maxX+EPS&&southMiddle>=box.minZ-EPS&&southMiddle<=box.maxZ+EPS){found=true;break;}
            if(!found)return false;
        }
        return true;
    }

    public static void main(String[] arguments)throws Exception{
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        Path root=Path.of(arguments[0]);JsonObject assembly=JsonParser.parseString(Files.readString(root.resolve("assembly/assembly.json"))).getAsJsonObject();
        Path source=Path.of(assembly.get("candidate").getAsString());
        String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        if(!digest.equals(assembly.get("sha256").getAsString()))throw new IllegalStateException("Hash drift");
        CompoundTag data=NbtIo.readCompressed(source.toFile());ListTag palette=data.getList("palette",Tag.TAG_COMPOUND);
        List<BlockState> states=new ArrayList<>();for(int stateIndex=0;stateIndex<palette.size();stateIndex++)states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(stateIndex)));
        for(Tag item:data.getList("blocks",Tag.TAG_COMPOUND)){
            CompoundTag entry=(CompoundTag)item;ListTag coordinates=entry.getList("pos",Tag.TAG_INT);
            BlockPos position=new BlockPos(coordinates.getInt(0),coordinates.getInt(1),coordinates.getInt(2));BlockState state=states.get(entry.getInt("state"));
            List<AABB> boxes=state.getCollisionShape(EmptyBlockGetter.INSTANCE,position).toAabbs().stream().map(box->box.move(position)).toList();
            if(!boxes.isEmpty())cells.put(position,boxes);
            if(!state.getFluidState().isEmpty())fluids.add(position);
        }
        List<Object> failures=new ArrayList<>(),links=new ArrayList<>();int checked=0;
        for(JsonElement item:assembly.getAsJsonArray("links")){
            JsonObject link=item.getAsJsonObject();JsonArray points=link.getAsJsonArray("centerline_xz");int segments=0;
            double firstFoot=link.getAsJsonArray("start").get(1).getAsDouble(),lastFoot=link.getAsJsonArray("end").get(1).getAsDouble();
            for(int step=0;step<points.size()-1;step++){
                JsonArray first=points.get(step).getAsJsonArray(),last=points.get(step+1).getAsJsonArray();
                double firstEast=first.get(0).getAsDouble()+0.5,firstSouth=first.get(1).getAsDouble()+0.5,lastEast=last.get(0).getAsDouble()+0.5,lastSouth=last.get(1).getAsDouble()+0.5;
                double firstHeight=firstFoot+Math.rint((lastFoot-firstFoot)*2*step/(points.size()-1))/2;
                double lastHeight=firstFoot+Math.rint((lastFoot-firstFoot)*2*(step+1)/(points.size()-1))/2;
                AABB floorRegion=new AABB(Math.min(firstEast,lastEast)-1.5+EPS,Math.min(firstHeight,lastHeight)-1,Math.min(firstSouth,lastSouth)-1.5+EPS,Math.max(firstEast,lastEast)+1.5-EPS,Math.max(firstHeight,lastHeight)+0.5+EPS,Math.max(firstSouth,lastSouth)+1.5-EPS);
                List<AABB> support=collisions(floorRegion).stream().filter(box->box.maxY>=Math.min(firstHeight,lastHeight)-0.5-EPS&&box.maxY<=Math.max(firstHeight,lastHeight)+0.5+EPS).toList();
                List<AABB> visibleSupport=support.stream().filter(box->support.stream().noneMatch(other->other.maxY>box.maxY+EPS&&other.minX<=box.minX+EPS&&other.maxX>=box.maxX-EPS&&other.minZ<=box.minZ+EPS&&other.maxZ>=box.maxZ-EPS)).toList();
                double minTop=visibleSupport.stream().mapToDouble(box->box.maxY).min().orElse(-999),maxTop=visibleSupport.stream().mapToDouble(box->box.maxY).max().orElse(999);
                boolean floor=covered(floorRegion,support),height=maxTop-minTop<=0.5+EPS;
                AABB body=new AABB(floorRegion.minX,maxTop+EPS,floorRegion.minZ,floorRegion.maxX,maxTop+1.8-EPS,floorRegion.maxZ);
                boolean clear=collisions(body).isEmpty();boolean dry=true;
                for(int east=(int)Math.floor(body.minX);east<=Math.floor(body.maxX);east++)for(int up=(int)Math.floor(body.minY);up<=Math.floor(body.maxY);up++)for(int south=(int)Math.floor(body.minZ);south<=Math.floor(body.maxZ);south++)if(fluids.contains(new BlockPos(east,up,south)))dry=false;
                if(!floor||!height||!clear||!dry)failures.add(Map.of("from",link.get("from").getAsString(),"to",link.get("to").getAsString(),"segment",step,"full_floor_coverage",floor,"half_step_max",height,"body_clear",clear,"dry",dry,"floor_top_min",minTop,"floor_top_max",maxTop));
                checked++;segments++;
            }
            links.add(Map.of("from",link.get("from").getAsString(),"to",link.get("to").getAsString(),"continuous_segments",segments));
        }
        Map<String,Object> report=new LinkedHashMap<>();report.put("status",failures.isEmpty()?"AUTHOR_CONTINUOUS_CONNECTOR_PASS":"NEEDS_FIX");report.put("sha256",digest);report.put("segments",checked);report.put("links",links);report.put("failures",failures);report.put("scope","Native collision shapes, exact swept horizontal union of 3x3 squares along all connector centerline segments including turns. Floor rectangle union coverage is partitioned at every collision-box edge; each swept region has <=0.5 block floor-height spread and 1.8 block headroom above its highest support. This proves the declared connector strip geometry, not client walking, all room interiors, rotation compatibility or independent acceptance.");
        Path output=root.resolve("assembly/qa/continuous-width.json");Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println(checked+" continuous regions; "+failures.size()+" failures");if(!failures.isEmpty())System.exit(2);
    }
}
