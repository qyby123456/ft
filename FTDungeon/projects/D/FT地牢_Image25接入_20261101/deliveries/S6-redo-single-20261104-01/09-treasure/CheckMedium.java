import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CheckMedium {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        var metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        Object room = inspect.invoke(null, root);
        var data = NbtIo.readCompressed(root.resolve("room.nbt").toFile());
        var palette = data.getList("palette", 10);
        var entries = data.getList("blocks", 10);
        var states = new ArrayList<BlockState>();
        var fuelStates = new ArrayList<Integer>();
        var fuelChecks = new ArrayList<Object>();
        for (int stateIndex = 0; stateIndex < palette.size(); stateIndex++) {
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(stateIndex)));
        }
        for (Method method : FireBlock.class.getDeclaredMethods()) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == BlockState.class && method.getReturnType() == int.class && method.getName().toLowerCase().contains("odds")) {
                method.setAccessible(true);
                for (int stateIndex = 0; stateIndex < states.size(); stateIndex++) {
                    int odds = (int) method.invoke(Blocks.FIRE, states.get(stateIndex));
                    if (odds > 0 && !fuelStates.contains(stateIndex)) fuelStates.add(stateIndex);
                    fuelChecks.add(Map.of("method", method.getName(), "state", states.get(stateIndex).toString(), "odds", odds));
                }
            }
        }
        if (fuelChecks.isEmpty()) throw new IllegalStateException("No native FireBlock odds methods");
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        var lava = new ArrayList<BlockPos>();
        var fuels = new ArrayList<BlockPos>();
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            var position = entry.getList("pos", 3);
            var point = new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2));
            int stateIndex = entry.getInt("state");
            blocks.put(point, states.get(stateIndex));
            if (states.get(stateIndex).is(Blocks.LAVA)) lava.add(point);
            if (fuelStates.contains(stateIndex)) fuels.add(point);
        }
        var buffers = new ArrayList<Object>();
        for (var fuel : fuels) {
            int minimumChebyshev = Integer.MAX_VALUE;
            for (var source : lava) {
                int deltaX = Math.abs(source.getX() - fuel.getX());
                int deltaZ = Math.abs(source.getZ() - fuel.getZ());
                int rise = fuel.getY() - source.getY();
                if (rise >= 0 && rise <= 4) {
                    minimumChebyshev = Math.min(minimumChebyshev, Math.max(deltaX, deltaZ));
                    if (deltaX <= 4 && deltaZ <= 4) throw new IllegalStateException("Fuel too close to lava: " + fuel + " / " + source);
                }
            }
            buffers.add(Map.of("fuel", fuel.toShortString(), "state", blocks.get(fuel).toString(), "minimum_horizontal_chebyshev_for_vertical_rise_0_to_4", minimumChebyshev));
        }
        LevelReader view = (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class<?>[]{LevelReader.class}, (proxy, method, parameters) -> {
            String name = method.getName();
            if (name.equals("getBlockState")) return blocks.getOrDefault((BlockPos) parameters[0], Blocks.AIR.defaultBlockState());
            if (name.equals("getFluidState")) return blocks.getOrDefault((BlockPos) parameters[0], Blocks.AIR.defaultBlockState()).getFluidState();
            if (name.equals("getMinBuildHeight")) return 0;
            if (name.equals("getHeight") || name.equals("getMaxBuildHeight")) return 18;
            if (name.equals("getRawBrightness") || name.equals("getMaxLocalRawBrightness")) return 15;
            if (name.equals("hasChunk") || name.equals("hasChunkAt") || name.equals("hasChunksAt")) return true;
            if (name.equals("toString")) return "Immutable S6-09 survival view";
            throw new UnsupportedOperationException(name);
        });
        var survival = new ArrayList<Object>();
        for (var block : blocks.entrySet()) {
            String identifier = BuiltInRegistries.BLOCK.getKey(block.getValue().getBlock()).toString();
            if (identifier.endsWith("lantern")) {
                boolean survives = block.getValue().canSurvive(view, block.getKey());
                survival.add(Map.of("block", identifier, "position", block.getKey().toShortString(), "canSurvive", survives));
                if (!survives) throw new IllegalStateException("Native fragile support failure " + block);
            }
        }
        var containers = new ArrayList<Object>();
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.getCompound(index);
            if (!entry.contains("nbt")) continue;
            var coordinates = entry.getList("pos", 3);
            var position = new BlockPos(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
            var state = blocks.get(position);
            var entity = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(position, state, entry.getCompound("nbt"));
            if (!(entity instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest)) throw new IllegalStateException("Unexpected container " + position);
            if (chest.getContainerSize() != 27 || !chest.isEmpty()) throw new IllegalStateException("Container inventory mismatch");
            if (state.getValue(net.minecraft.world.level.block.ChestBlock.TYPE) != net.minecraft.world.level.block.state.properties.ChestType.SINGLE) throw new IllegalStateException("Joined chest");
            for (int rise = 1; rise <= 2; rise++) if (!blocks.getOrDefault(position.above(rise), Blocks.AIR.defaultBlockState()).isAir()) throw new IllegalStateException("Chest lid clearance blocked " + position);
            var lidSweep = new net.minecraft.world.phys.AABB(position.getX(), position.getY() + 0.875, position.getZ(), position.getX() + 1, position.getY() + 2, position.getZ() + 1);
            for (var neighbor : blocks.entrySet()) {
                if (neighbor.getKey().equals(position)) continue;
                var neighborPos = neighbor.getKey();
                if (Math.abs(neighborPos.getX() - position.getX()) > 1 || Math.abs(neighborPos.getZ() - position.getZ()) > 1 || Math.abs(neighborPos.getY() - position.getY()) > 2) continue;
                for (var shape : neighbor.getValue().getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, neighborPos).toAabbs()) if (lidSweep.intersects(shape.move(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ()))) throw new IllegalStateException("Lid envelope intersection");
            }
            containers.add(Map.of("position", position.toShortString(), "type", "minecraft:chest", "inventory_size", chest.getContainerSize(), "empty", chest.isEmpty(), "two_air_blocks_above", true, "conservative_lid_envelope", "full block XZ, y+0.875..y+2, native neighbor collision boxes", "saved_nbt", chest.saveWithFullMetadata().toString(), "interaction", "NOT_RUN"));
        }
        if (containers.size() != 3) throw new IllegalStateException("Expected three real chest entities");
        Files.writeString(root.resolve("qa/containers.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status", "PASS", "sha256", metadata.get("sha256").getAsString(), "containers", containers, "scope", "Actual vanilla ChestBlockEntity load/save and 27-slot empty inventory; static native collision lid envelope and air-above check; front approach targets are validated separately by BodyRoutes. No lid animation, menu, player interaction or cat/world obstruction ticks.")));
        var results = new ArrayList<Object>();
        var targets = metadata.getAsJsonArray("validation_targets");
        for (int index = 0; index < targets.size(); index++) {
            Path scratch = root.resolve("qa/collision-targets/target-" + index);
            Files.createDirectories(scratch);
            Files.copy(root.resolve("room.nbt"), scratch.resolve("room.nbt"), StandardCopyOption.REPLACE_EXISTING);
            var test = metadata.deepCopy();
            test.add("interior", targets.get(index));
            Files.writeString(scratch.resolve("room.json"), test.toString());
            inspect.invoke(null, scratch);
            results.add(Map.of("point", targets.get(index), "status", "PASS"));
        }
        var report = Map.of("status", "PASS", "sha256", metadata.get("sha256").getAsString(), "room", room, "native_collision_targets", results, "fragile_survival", survival, "native_fire_odds", fuelChecks, "fuel_buffers", buffers, "fire_buffer_rule", "reject fuel with horizontal |dx|<=4 AND |dz|<=4 and fuelY-lavaY in [0,4]; conservative static boundary only", "scope", "Actual vanilla 1.20.1 registry, StructureTemplate load/save and collision-shape routes; native canSurvive and FireBlock odds. No world placement, fluid/random ticks, rotations, gameplay or independent acceptance.");
        Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS native load, " + targets.size() + " route targets, " + survival.size() + " survival checks, " + fuels.size() + " native fuel positions (static only)");
    }
}
