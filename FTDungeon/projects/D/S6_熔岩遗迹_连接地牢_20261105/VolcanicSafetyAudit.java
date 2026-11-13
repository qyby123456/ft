import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class VolcanicSafetyAudit {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Path source = root.resolve("volcanic-connected.nbt");
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        var metadata = JsonParser.parseString(Files.readString(root.resolve("layout.json"))).getAsJsonObject();
        if (!metadata.get("sha256").getAsString().equals(sha256)) throw new IllegalStateException("Hash mismatch");
        var data = NbtIo.readCompressed(source.toFile());
        var palette = data.getList("palette",10);
        var entries = data.getList("blocks",10);
        List<BlockState> states = new ArrayList<>();
        var fuelIndices = new HashSet<Integer>();
        List<Object> oddsEvidence = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(index)));
        for (var method : FireBlock.class.getDeclaredMethods()) {
            if (method.getParameterCount()!=1 || method.getParameterTypes()[0]!=BlockState.class || method.getReturnType()!=int.class || !method.getName().toLowerCase().contains("odds")) continue;
            method.setAccessible(true);
            for (int index = 0; index < states.size(); index++) {
                int odds = (int) method.invoke(Blocks.FIRE,states.get(index));
                if (odds>0) fuelIndices.add(index);
                oddsEvidence.add(Map.of("method",method.getName(),"state",states.get(index).toString(),"odds",odds));
            }
        }
        if (oddsEvidence.isEmpty()) throw new IllegalStateException("Native fire methods unavailable");
        Map<BlockPos,BlockState> blocks = new HashMap<>();
        List<BlockPos> lava = new ArrayList<>();
        List<BlockPos> fuels = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            var point = entry.getList("pos",3);
            BlockPos position = new BlockPos(point.getInt(0),point.getInt(1),point.getInt(2));
            int stateIndex = entry.getInt("state");
            BlockState state = states.get(stateIndex);
            if (!state.isAir()) blocks.put(position,state);
            if (state.is(Blocks.LAVA)) {
                if (!state.getFluidState().isSource()) throw new IllegalStateException("Non-source lava: "+position);
                lava.add(position);
            }
            if (fuelIndices.contains(stateIndex)) fuels.add(position);
        }
        int sealedFaces = 0;
        for (BlockPos position : lava) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = position.relative(direction);
                BlockState neighbor = blocks.getOrDefault(adjacent,Blocks.AIR.defaultBlockState());
                if (neighbor.is(Blocks.LAVA)) continue;
                if (!Block.isFaceFull(neighbor.getCollisionShape(EmptyBlockGetter.INSTANCE,adjacent),direction.getOpposite())) throw new IllegalStateException("Non-full lava boundary: "+position+" "+direction+" "+neighbor);
                sealedFaces++;
            }
            for (BlockPos fuel : fuels) {
                int rise = fuel.getY()-position.getY();
                if (rise>=0 && rise<=4 && Math.abs(fuel.getX()-position.getX())<=4 && Math.abs(fuel.getZ()-position.getZ())<=4) throw new IllegalStateException("Fuel inside conservative buffer: "+fuel+" lava="+position);
            }
        }
        List<Object> containers = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            if (!entry.contains("nbt")) continue;
            var point = entry.getList("pos",3);
            BlockPos position = new BlockPos(point.getInt(0),point.getInt(1),point.getInt(2));
            BlockState state = blocks.get(position);
            var entity = BlockEntity.loadStatic(position,state,entry.getCompound("nbt"));
            if (entity instanceof BarrelBlockEntity barrel) {
                if (!barrel.isEmpty() || barrel.getContainerSize()!=27) throw new IllegalStateException("Unexpected barrel inventory: "+position);
                containers.add(Map.of("position",position.toShortString(),"type","minecraft:barrel","empty",true,"slots",27,"saved",barrel.saveWithFullMetadata().toString(),"interaction","NOT_RUN"));
                continue;
            }
            if (!(entity instanceof ChestBlockEntity chest) || !chest.isEmpty() || chest.getContainerSize()!=27) throw new IllegalStateException("Unexpected container: "+position);
            for (int rise = 1; rise<=2; rise++) if (!blocks.getOrDefault(position.above(rise),Blocks.AIR.defaultBlockState()).isAir()) throw new IllegalStateException("Blocked chest: "+position);
            AABB sweep = new AABB(position.getX(),position.getY()+0.875,position.getZ(),position.getX()+1,position.getY()+2,position.getZ()+1);
            for (int east = -1; east<=1; east++) for (int up = -1; up<=2; up++) for (int south = -1; south<=1; south++) {
                BlockPos adjacent = position.offset(east,up,south);
                if (adjacent.equals(position)) continue;
                BlockState neighbor = blocks.getOrDefault(adjacent,Blocks.AIR.defaultBlockState());
                for (AABB box : neighbor.getCollisionShape(EmptyBlockGetter.INSTANCE,adjacent).toAabbs()) if (sweep.intersects(box.move(adjacent))) throw new IllegalStateException("Lid collision: "+position);
            }
            containers.add(Map.of("position",position.toShortString(),"type",chest.getType().toString(),"empty",true,"slots",27,"two_air_above",true,"native_lid_envelope_clear",true,"saved",chest.saveWithFullMetadata().toString()));
        }
        var report = Map.of("source_sha256",sha256,"source_lava_cells",lava.size(),"solid_full_collision_boundary_faces",sealedFaces,"native_fuel_cells",fuels.size(),"native_fire_odds",oddsEvidence,"conservative_fire_buffer_clear",true,"containers",containers,"runtime_tested",false,"scope","Native static collision face containment, FireBlock fuel odds and dx/dz<=4, dy=0..4 fuel exclusion; real chest load/save, empty inventory and lid envelope. No world/fluid/random ticks, external fuels, player movement damage or interaction certification.");
        Files.writeString(root.resolve("qa/volcanic-safety.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS lava="+lava.size()+" sealed_faces="+sealedFaces+" fuels="+fuels.size()+" containers="+containers.size());
    }
}
