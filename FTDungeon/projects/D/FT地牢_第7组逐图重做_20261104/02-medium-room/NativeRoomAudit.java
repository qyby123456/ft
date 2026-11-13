import com.google.gson.*;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

public class NativeRoomAudit {
    static final Map<BlockPos,BlockState> BLOCKS = new HashMap<>();
    static final Map<BlockPos,List<AABB>> SHAPES = new HashMap<>();
    static final LevelReader VIEW = (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(),new Class<?>[]{LevelReader.class},(proxy,method,arguments)-> {
        return switch(method.getName()) {
            case "getBlockState" -> BLOCKS.getOrDefault((BlockPos)arguments[0],Blocks.AIR.defaultBlockState());
            case "getFluidState" -> BLOCKS.getOrDefault((BlockPos)arguments[0],Blocks.AIR.defaultBlockState()).getFluidState();
            case "getBlockEntity" -> null;
            case "getMinBuildHeight" -> 0;
            case "getHeight", "getMaxBuildHeight" -> 14;
            case "getRawBrightness", "getMaxLocalRawBrightness" -> 15;
            case "hasChunk", "hasChunkAt", "hasChunksAt" -> true;
            case "toString" -> "S7-02 immutable isolated view";
            default -> throw new UnsupportedOperationException(method.getName());
        };
    });
    record Node(int halfX,int halfZ) {
        List<Double> coordinates() { return List.of(halfX/2.0,2.0,halfZ/2.0); }
    }
    static void require(boolean condition,String message) { if(!condition) throw new IllegalStateException(message); }
    static List<AABB> boxes(BlockPos point) {
        return SHAPES.computeIfAbsent(point.immutable(),key->VIEW.getBlockState(key).getCollisionShape(VIEW,key).toAabbs().stream().map(box->box.move(key)).toList());
    }
    static boolean collide(AABB volume) {
        for(int blockX=(int)Math.floor(volume.minX);blockX<=(int)Math.floor(volume.maxX);blockX++) {
            for(int blockY=(int)Math.floor(volume.minY);blockY<=(int)Math.floor(volume.maxY);blockY++) {
                for(int blockZ=(int)Math.floor(volume.minZ);blockZ<=(int)Math.floor(volume.maxZ);blockZ++) {
                    for(AABB box:boxes(new BlockPos(blockX,blockY,blockZ))) if(box.intersects(volume)) return true;
                }
            }
        }
        return false;
    }
    static AABB body(Node node) { return new AABB(node.halfX/2.0-.299999,2.000001,node.halfZ/2.0-.299999,node.halfX/2.0+.299999,3.799999,node.halfZ/2.0+.299999); }
    static boolean stand(Node node) {
        if(node.halfX<1||node.halfX>45||node.halfZ<1||node.halfZ>45||collide(body(node))) return false;
        double centerX=node.halfX/2.0,centerZ=node.halfZ/2.0;
        if(!collide(new AABB(centerX-.299,1.985,centerZ-.299,centerX+.299,1.999999,centerZ+.299))) return false;
        for(int blockX=(int)Math.floor(centerX-.299);blockX<=(int)Math.floor(centerX+.299);blockX++) {
            for(int blockZ=(int)Math.floor(centerZ-.299);blockZ<=(int)Math.floor(centerZ+.299);blockZ++) {
                for(int blockY=1;blockY<=3;blockY++) if(!VIEW.getFluidState(new BlockPos(blockX,blockY,blockZ)).isEmpty()) return false;
            }
        }
        return true;
    }
    static boolean sweep(Node from,Node to) {
        return stand(from)&&stand(to)&&!collide(body(from).expandTowards((to.halfX-from.halfX)/2.0,0,(to.halfZ-from.halfZ)/2.0));
    }
    static Map<Node,Node> search(Node start) {
        Map<Node,Node> previous=new HashMap<>();
        ArrayDeque<Node> pending=new ArrayDeque<>();
        require(stand(start),"Entry not standable");
        previous.put(start,start);pending.add(start);
        while(!pending.isEmpty()) {
            Node from=pending.removeFirst();
            for(int[] delta:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                Node to=new Node(from.halfX+delta[0],from.halfZ+delta[1]);
                if(!previous.containsKey(to)&&sweep(from,to)) { previous.put(to,from);pending.addLast(to); }
            }
        }
        return previous;
    }
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        Path root=Path.of(arguments[0]);
        var metadata=JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        var tagData=JsonParser.parseString(Files.readString(root.resolve("vanilla-block-tags.json"))).getAsJsonObject();
        Map<TagKey<Block>,List<Holder<Block>>> bindings=new HashMap<>();
        for(var tag:tagData.entrySet()) {
            List<Holder<Block>> values=new ArrayList<>();
            for(var value:tag.getValue().getAsJsonArray()) values.add(BuiltInRegistries.BLOCK.getHolderOrThrow(ResourceKey.create(Registries.BLOCK,new ResourceLocation(value.getAsString()))));
            bindings.put(TagKey.create(Registries.BLOCK,new ResourceLocation(tag.getKey())),values);
        }
        BuiltInRegistries.BLOCK.bindTags(bindings);
        String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(root.resolve("room.nbt"))));
        require(digest.equals(metadata.get("sha256").getAsString()),"Metadata hash mismatch");
        CompoundTag nbt=NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        require(nbt.getInt("DataVersion")==3465,"Wrong version");
        List<BlockState> states=new ArrayList<>();
        var palette=nbt.getList("palette",Tag.TAG_COMPOUND);
        for(int index=0;index<palette.size();index++) {
            CompoundTag value=palette.getCompound(index);ResourceLocation id=new ResourceLocation(value.getString("Name"));
            require(id.getNamespace().equals("minecraft")&&BuiltInRegistries.BLOCK.containsKey(id),"Unknown block "+id);
            Block block=BuiltInRegistries.BLOCK.get(id);
            for(String key:value.getCompound("Properties").getAllKeys()) {
                var property=block.getStateDefinition().getProperty(key);
                require(property!=null&&property.getValue(value.getCompound("Properties").getString(key)).isPresent(),"Bad state "+id+" "+key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),value));
        }
        int containers=0;
        var records=nbt.getList("blocks",Tag.TAG_COMPOUND);
        for(int index=0;index<records.size();index++) {
            CompoundTag value=records.getCompound(index);var coords=value.getList("pos",Tag.TAG_INT);
            BlockPos point=new BlockPos(coords.getInt(0),coords.getInt(1),coords.getInt(2));BlockState state=states.get(value.getInt("state"));
            require(BLOCKS.put(point,state)==null,"Duplicate position");
            if(value.contains("nbt",Tag.TAG_COMPOUND)) {
                var blockData=value.getCompound("nbt");
                require(!blockData.contains("LootTable")&&blockData.getList("Items",Tag.TAG_COMPOUND).isEmpty(),"Unexpected loot");
                BlockEntity entity=BlockEntity.loadStatic(point,state,blockData);
                require(entity!=null&&entity.getType().isValid(state),"Invalid container");entity.saveWithFullMetadata();containers++;
            }
        }
        require(nbt.getList("entities",Tag.TAG_COMPOUND).isEmpty(),"Unexpected entities");
        StructureTemplate template=new StructureTemplate();template.load(BuiltInRegistries.BLOCK.asLookup(),nbt);
        require(template.save(new CompoundTag()).getList("blocks",Tag.TAG_COMPOUND).size()==records.size(),"Template roundtrip mismatch");
        require(template.getSize().equals(new BlockPos(23,14,23)),"Template size mismatch");
        List<Object> survival=new ArrayList<>();
        for(var entry:BLOCKS.entrySet()) {
            BlockState state=entry.getValue();String id=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            boolean fragile=state.getBlock() instanceof BushBlock||state.getBlock() instanceof FlowerPotBlock||id.endsWith("lantern")||id.endsWith(":vine")||id.endsWith("amethyst_cluster")||id.endsWith("glow_lichen");
            if(fragile) { boolean valid=state.canSurvive(VIEW,entry.getKey());require(valid,"Static survival failed "+id+" "+entry.getKey());survival.add(Map.of("block",id,"position",List.of(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ()),"can_survive",valid)); }
        }
        List<Object> portCells=new ArrayList<>();
        for(int blockX=10;blockX<=12;blockX++) {
            BlockPos floor=new BlockPos(blockX,1,0);require(VIEW.getBlockState(floor).isFaceSturdy(VIEW,floor,Direction.UP),"Door unsupported");
            for(int blockY=2;blockY<=5;blockY++) { require(VIEW.getBlockState(new BlockPos(blockX,blockY,0)).isAir(),"Door not 3x4 clear");portCells.add(List.of(blockX,blockY,0)); }
        }
        Node start=new Node(23,1);Map<Node,Node> previous=search(start);List<Object> paths=new ArrayList<>();
        for(var item:metadata.getAsJsonArray("test_targets")) {
            var target=item.getAsJsonArray();require(target.get(1).getAsInt()==2,"Wrong floor target");
            Node cursor=new Node(target.get(0).getAsInt()*2+1,target.get(2).getAsInt()*2+1);require(previous.containsKey(cursor),"Unreachable target "+target);
            List<Object> path=new ArrayList<>();
            while(true) { path.add(cursor.coordinates());Node parent=previous.get(cursor);if(parent.equals(cursor))break;require(sweep(cursor,parent),"Reverse sweep failed");cursor=parent; }
            Collections.reverse(path);paths.add(Map.of("target",target,"status","PASS_BIDIRECTIONAL","full_player_center_path",path));
        }
        var result=new LinkedHashMap<String,Object>();result.put("status","PASS_OFFLINE_NATIVE_ONLY");result.put("sha256",digest);result.put("registry_and_template","PASS");result.put("containers_loaded",containers);result.put("bound_vanilla_tags",bindings.size());result.put("static_survival",survival);result.put("brightness_assumption",15);result.put("door_air_cells_3x4",portCells);result.put("target_paths",paths);result.put("reachable_half_grid_nodes",previous.size());result.put("player_model",Map.of("width",.6,"height",1.8,"horizontal_step",.5,"feet_y",2,"swept_each_edge",true,"no_jump",true));result.put("world_placement","NOT_RUN");result.put("neighbor_random_fluid_ticks","NOT_RUN");result.put("scope","Real vanilla shapes/tags, immutable LevelReader, planar swept player volume; not ServerPlayer or world physics. Tags and brightness are explicit assumptions.");
        Files.writeString(root.resolve("native-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
        System.out.println("PASS S7-02; containers="+containers+"; fragile="+survival.size()+"; routes="+paths.size()+"; sampled_nodes="+previous.size());
    }
}
