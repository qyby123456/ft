package net.farmingtales.zoneqa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftzonen05qa")
@PrefixGameTestTemplate(false)
public final class LiftHubTests {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final BlockPos SIZE = new BlockPos(29, 24, 29);
    private static final int RECORDS = 29 * 24 * 29;
    private static final List<BlockPos> TARGETS = List.of(new BlockPos(14,7,0), new BlockPos(28,7,14), new BlockPos(14,7,28), new BlockPos(0,7,14), new BlockPos(14,7,4), new BlockPos(24,7,14), new BlockPos(14,7,24), new BlockPos(4,7,14), new BlockPos(5,1,9), new BlockPos(6,1,9), new BlockPos(5,7,9), new BlockPos(5,18,9), new BlockPos(6,18,9), new BlockPos(14,1,14));
    private record Placement(BlockPos origin, Rotation rotation, Map<BlockPos, BlockState> expected) {}
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static BlockPos transform(BlockPos local, Placement placement) {
        return StructureTemplate.transform(local, Mirror.NONE, placement.rotation(), BlockPos.ZERO).offset(placement.origin());
    }
    private static AABB bodyBounds(BlockPos feet) {
        return new AABB(feet.getX()+0.2, feet.getY(), feet.getZ()+0.2, feet.getX()+0.8, feet.getY()+1.8, feet.getZ()+0.8);
    }
    private static boolean safe(ServerLevel level, BlockPos feet) {
        BlockState state = level.getBlockState(feet);
        BlockState head = level.getBlockState(feet.above());
        if ((!state.isAir() && !state.is(Blocks.LADDER)) || (!head.isAir() && !head.is(Blocks.LADDER))) return false;
        if (!state.getFluidState().isEmpty() || !head.getFluidState().isEmpty() || !level.noCollision(bodyBounds(feet))) return false;
        return state.is(Blocks.LADDER) || level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP);
    }
    private static int walk(ServerLevel level, Placement placement) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos entry = TARGETS.get(0);
        require(safe(level, transform(entry, placement)), "Unsafe entrance");
        visited.add(entry); pending.add(entry);
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            List<BlockPos> candidates = new ArrayList<>();
            for (Direction direction : Direction.Plane.HORIZONTAL) candidates.add(current.relative(direction));
            if (level.getBlockState(transform(current, placement)).is(Blocks.LADDER)) {
                candidates.add(current.above()); candidates.add(current.below());
            }
            for (BlockPos next : candidates) {
                if (next.getX()<0 || next.getX()>=29 || next.getY()<1 || next.getY()>21 || next.getZ()<0 || next.getZ()>=29 || visited.contains(next)) continue;
                if (safe(level, transform(next, placement))) { visited.add(next); pending.addLast(next); }
            }
        }
        for (BlockPos target : TARGETS) require(visited.contains(target), "Disconnected landing/port " + target + " " + placement.rotation());
        return visited.size();
    }
    private static Map<String,Object> inspect(ServerLevel level, Placement placement, JsonArray routeCells) {
        int ladders=0, lanterns=0, chains=0, bars=0, lights=0;
        for (Map.Entry<BlockPos,BlockState> entry : placement.expected().entrySet()) {
            BlockPos position=entry.getKey(); BlockState actual=level.getBlockState(position);
            require(actual.equals(entry.getValue()), "State drift at " + position + ": expected " + entry.getValue() + ", got " + actual);
            require(actual.canSurvive(level, position), "Unsupported block at " + position + ": " + actual);
            require(actual.getFluidState().isEmpty(), "Fluid at " + position);
            require(level.getBlockEntity(position)==null, "Unexpected block entity");
            if (actual.is(Blocks.LADDER)) ladders++;
            if (actual.is(Blocks.LANTERN)) lanterns++;
            if (actual.is(Blocks.CHAIN)) chains++;
            if (actual.is(Blocks.IRON_BARS)) bars++;
            if (actual.getLightEmission()>0) lights++;
        }
        require(ladders==18, "Incomplete emergency ladder");
        for (var record : routeCells) {
            JsonArray coordinates=record.getAsJsonArray();
            BlockPos local=new BlockPos(coordinates.get(0).getAsInt(),coordinates.get(1).getAsInt(),coordinates.get(2).getAsInt());
            BlockPos world=transform(local,placement);
            require(safe(level,world) && level.getBlockState(world.above(2)).isAir(), "Three-wide/headroom route failed: " + local);
        }
        for (int floorY : new int[]{6,17}) {
            for (int along=9;along<=19;along++) for (int border : new int[]{9,19}) {
                for (BlockPos local : List.of(new BlockPos(along,floorY+1,border),new BlockPos(border,floorY+1,along))) {
                    BlockPos world=transform(local,placement);
                    require(level.getBlockState(world).is(Blocks.POLISHED_BLACKSTONE_BRICKS), "Lower guard missing");
                    require(level.getBlockState(world.above()).is(Blocks.IRON_BARS), "Upper guard missing");
                    require(!level.noCollision(bodyBounds(world)) && !level.noCollision(bodyBounds(world.above())), "Guard has no collision");
                }
            }
        }
        for (int blockX=0;blockX<29;blockX++) for(int blockZ=0;blockZ<29;blockZ++) {
            for(int capY : new int[]{0,23}) {
                BlockPos world=transform(new BlockPos(blockX,capY,blockZ),placement);
                require(level.getBlockState(world).isFaceSturdy(level,world,Direction.UP), "Bottom/roof cap missing");
            }
        }
        BlockPos lower=transform(BlockPos.ZERO,placement), upper=transform(new BlockPos(28,23,28),placement);
        AABB bounds=new AABB(Math.min(lower.getX(),upper.getX())-1,lower.getY()-1,Math.min(lower.getZ(),upper.getZ())-1,Math.max(lower.getX(),upper.getX())+2,upper.getY()+2,Math.max(lower.getZ(),upper.getZ())+2);
        List<ItemEntity> dropped=level.getEntitiesOfClass(ItemEntity.class,bounds);
        require(dropped.isEmpty(), "Dropped attachment/item: " + dropped.stream().map(item->item.getItem()+" at "+item.position()).toList());
        Map<String,Object> report=new LinkedHashMap<>();
        report.put("rotation",placement.rotation().name()); report.put("status","PASS");
        report.put("origin",List.of(placement.origin().getX(),placement.origin().getY(),placement.origin().getZ()));
        report.put("exact_state_records",placement.expected().size()); report.put("reachable_landing_graph_cells",walk(level,placement));
        report.put("three_wide_three_high_main_route_cells",routeCells.size()); report.put("ladder_segments",ladders);
        report.put("supported_lanterns",lanterns); report.put("chain_blocks",chains); report.put("collidable_bar_blocks",bars); report.put("light_blocks",lights);
        report.put("four_horizontal_and_two_internal_vertical_ports_connected",true); report.put("two_high_guard_collision_both_levels",true);
        report.put("state_drift",0); report.put("dropped_items",0); report.put("block_entities",0);
        return report;
    }
    @GameTest(template="empty",batch="n05_only",timeoutTicks=800)
    public static void liftHubFourRotationsStable(GameTestHelper helper) throws Exception {
        ServerLevel level=helper.getLevel();
        require(level.getServer() instanceof GameTestServer,"Only isolated GameTestServer allowed");
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,level.getServer());
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0,level.getServer());
        Path root=Path.of(System.getProperty("ftzonen05qa.root")).toAbsolutePath().normalize();
        require(root.getFileName().toString().equals("zone-N05") && Files.exists(root.resolve("forge/build.gradle")),"Wrong independent workspace");
        Path source=root.resolve("room.nbt");
        String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        JsonObject metadata=JSON.fromJson(Files.readString(root.resolve("room.json")),JsonObject.class);
        require(metadata.get("id").getAsString().equals("N05") && metadata.get("sha256").getAsString().equals(digest),"Identity/hash mismatch");
        JsonArray routeCells=JSON.fromJson(Files.readString(root.resolve("qa/route-cells.json")),JsonObject.class).getAsJsonArray("three_wide_main");
        CompoundTag tag=NbtIo.readCompressed(source.toFile());
        require(tag.getInt("DataVersion")==3465 && !tag.contains("palettes") && tag.getList("entities",Tag.TAG_COMPOUND).isEmpty(),"Version/palette/entities mismatch");
        ListTag palette=tag.getList("palette",Tag.TAG_COMPOUND); List<BlockState> states=new ArrayList<>();
        for(int index=0;index<palette.size();index++) {
            CompoundTag entry=palette.getCompound(index); ResourceLocation identifier=new ResourceLocation(entry.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier),"Unknown block " + identifier);
            Block block=BuiltInRegistries.BLOCK.get(identifier); CompoundTag properties=entry.getCompound("Properties");
            require(properties.size()==block.getStateDefinition().getProperties().size(),"Incomplete properties " + identifier);
            for(String key:properties.getAllKeys()) {
                Property<?> property=block.getStateDefinition().getProperty(key);
                require(property!=null && property.getValue(properties.getString(key)).isPresent(),"Invalid property " + identifier + " " + key);
            }
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),entry));
        }
        StructureTemplate template=new StructureTemplate(); template.load(BuiltInRegistries.BLOCK.asLookup(),tag);
        require(template.getSize().equals(SIZE),"Native size mismatch");
        CompoundTag roundtrip=template.save(new CompoundTag());
        require(roundtrip.getList("blocks",Tag.TAG_COMPOUND).size()==RECORDS,"Native roundtrip count mismatch");
        roundtrip.putInt("DataVersion",3465); NbtIo.writeCompressed(roundtrip,root.resolve("qa/native-roundtrip.nbt").toFile());
        ListTag records=tag.getList("blocks",Tag.TAG_COMPOUND); Map<BlockPos,BlockState> local=new LinkedHashMap<>();
        for(int index=0;index<records.size();index++) {
            CompoundTag record=records.getCompound(index); ListTag position=record.getList("pos",Tag.TAG_INT);
            BlockPos coordinates=new BlockPos(position.getInt(0),position.getInt(1),position.getInt(2));
            require(coordinates.getX()>=0 && coordinates.getX()<29 && coordinates.getY()>=0 && coordinates.getY()<24 && coordinates.getZ()>=0 && coordinates.getZ()<29,"Out of bounds");
            require(!record.contains("nbt") && local.put(coordinates,states.get(record.getInt("state")))==null,"Payload or duplicate");
        }
        require(local.size()==RECORDS,"Incomplete volume");
        List<Placement> placements=new ArrayList<>(); Set<BlockPos> occupied=new HashSet<>();
        BlockPos[] origins={new BlockPos(64,80,64),new BlockPos(160,80,64),new BlockPos(256,80,64),new BlockPos(352,80,64)};
        for(int index=0;index<Rotation.values().length;index++) {
            Rotation rotation=Rotation.values()[index]; Placement placement=new Placement(origins[index],rotation,new LinkedHashMap<>());
            local.forEach((position,state)->placement.expected().put(transform(position,placement),state.rotate(rotation)));
            require(placement.expected().keySet().stream().allMatch(occupied::add),"Placements overlap");
            Set<Long> chunks=new HashSet<>();
            for(BlockPos position:placement.expected().keySet()) {
                long chunkKey=net.minecraft.world.level.ChunkPos.asLong(position.getX()>>4,position.getZ()>>4);
                if(chunks.add(chunkKey)) level.setChunkForced(position.getX()>>4,position.getZ()>>4,true);
            }
            BlockPos corner=transform(BlockPos.ZERO,placement),opposite=transform(new BlockPos(28,23,28),placement);
            int minX=Math.min(corner.getX(),opposite.getX())-3,maxX=Math.max(corner.getX(),opposite.getX())+3;
            int minZ=Math.min(corner.getZ(),opposite.getZ())-3,maxZ=Math.max(corner.getZ(),opposite.getZ())+3;
            AABB clearance=new AABB(minX,77,minZ,maxX+1,108,maxZ+1);
            for(BlockPos position:BlockPos.betweenClosed(minX,77,minZ,maxX,107,maxZ)) level.setBlock(position,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,clearance).forEach(net.minecraft.world.entity.Entity::discard);
            StructurePlaceSettings settings=new StructurePlaceSettings().setRotation(rotation).setKnownShape(true);
            require(template.placeInWorld(level,placement.origin(),placement.origin(),settings,level.random,Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE),"Placement rejected");
            inspect(level,placement,routeCells);
            placement.expected().forEach((position,state)->{state.updateNeighbourShapes(level,position,Block.UPDATE_ALL);level.updateNeighborsAt(position,state.getBlock());});
            placements.add(placement);
        }
        System.out.println("N05_NATIVE_INITIAL_PASS: four placements, 80736 exact states; neighbours updated; test chunks forced");
        helper.runAfterDelay(240,()->{
            try {
                List<Map<String,Object>> rotations=new ArrayList<>();
                for(Placement placement:placements) rotations.add(inspect(level,placement,routeCells));
                Map<String,Object> report=new LinkedHashMap<>();
                report.put("id","zone-N05");report.put("status","PASS");report.put("sha256",digest);
                report.put("minecraft","1.20.1");report.put("forge","47.4.10");report.put("ticks_observed",240);
                report.put("test","Actual StructureTemplate.placeInWorld in isolated Forge GameTestServer");
                report.put("registry_complete_properties","PASS");report.put("native_structure_roundtrip","PASS");
                report.put("forced_neighbor_updates",true);report.put("forced_chunk_ticking",true);report.put("rotations",rotations);
                report.put("isolated_test_environment",Map.of("terrain_clearance_margin_blocks",3,"ambient_entities_removed_before_placement",true,"mob_spawning",false,"random_ticks",0));
                report.put("functional_elevator_tested",false);report.put("client_visual_tested",false);report.put("deployed",false);report.put("player_save_modified",false);
                report.put("limits",List.of("Static cage only. Ladder geometry/collision/support tested; no player-controlled climbing or moving elevator test.","Vertical docks are internal with sealed top/bottom, not open stacked-module ports.","No full-modpack client, multiplayer, FPS, external adapters or production integration tested."));
                Files.writeString(root.resolve("qa/forge-native.json"),JSON.toJson(report)+System.lineSeparator(),StandardCharsets.UTF_8);
                System.out.println("N05_FORGE_PLACEMENT_PASS: four rotations stable after 240 ticks; sha256="+digest);
                helper.succeed();
            } catch(Exception failure) {helper.fail(failure.toString());}
        });
    }
}
