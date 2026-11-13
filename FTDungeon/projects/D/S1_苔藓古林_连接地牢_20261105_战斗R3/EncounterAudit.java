import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class EncounterAudit {
    public static void main(String[] arguments) throws Exception {
        Path root = Path.of(arguments[0]);
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        JsonObject plan = JsonParser.parseString(Files.readString(root.resolve("encounters.json"))).getAsJsonObject();
        var template = NbtIo.readCompressed(Path.of(plan.get("source").getAsString()).toFile());
        var rawPalette = template.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int index = 0; index < rawPalette.size(); index++) palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), rawPalette.getCompound(index)));
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        var records = template.getList("blocks", Tag.TAG_COMPOUND);
        for (int index = 0; index < records.size(); index++) {
            var entry = records.getCompound(index);
            var position = entry.getList("pos", Tag.TAG_INT);
            blocks.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), palette.get(entry.getInt("state")));
        }
        List<Object> spawnChecks = new ArrayList<>();
        for (var roomEntry : plan.getAsJsonArray("rooms")) {
            var room = roomEntry.getAsJsonObject();
            for (var unitEntry : room.getAsJsonArray("units")) {
                var unit = unitEntry.getAsJsonObject();
                var position = unit.getAsJsonArray("local");
                double east = position.get(0).getAsDouble() + 0.5;
                double up = position.get(1).getAsDouble();
                double south = position.get(2).getAsDouble() + 0.5;
                double half = unit.get("width").getAsDouble() / 2;
                AABB body = new AABB(east-half+0.00001, up+0.00001, south-half+0.00001, east+half-0.00001, up+unit.get("height").getAsDouble(), south+half-0.00001);
                boolean supported = false;
                AABB feet = new AABB(east-half+0.01, up-0.01, south-half+0.01, east+half-0.01, up+0.000001, south+half-0.01);
                for (int posX = (int)Math.floor(body.minX)-1; posX <= Math.floor(body.maxX)+1; posX++) {
                    for (int posY = (int)Math.floor(up)-1; posY <= Math.floor(body.maxY); posY++) {
                        for (int posZ = (int)Math.floor(body.minZ)-1; posZ <= Math.floor(body.maxZ)+1; posZ++) {
                            BlockPos blockPosition = new BlockPos(posX, posY, posZ);
                            BlockState state = blocks.getOrDefault(blockPosition, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                            if (posY >= up && !state.getFluidState().isEmpty()) throw new IllegalStateException("Fluid at spawn " + unit);
                            for (AABB local : state.getCollisionShape(EmptyBlockGetter.INSTANCE, blockPosition).toAabbs()) {
                                AABB collision = local.move(blockPosition);
                                if (collision.intersects(body)) throw new IllegalStateException("Spawn obstructed: " + unit + " by " + blockPosition);
                                if (collision.intersects(feet)) supported = true;
                            }
                        }
                    }
                }
                if (!supported) throw new IllegalStateException("Unsupported spawn " + unit);
                spawnChecks.add(Map.of("room",room.get("room").getAsInt(),"entity",unit.get("id").getAsString(),"local",position,"width",unit.get("width").getAsDouble(),"height",unit.get("height").getAsDouble(),"collision_clear",true,"supported",true));
            }
        }
        var access = net.minecraft.data.registries.VanillaRegistries.createLookup();
        var commands = new Commands(Commands.CommandSelection.ALL, CommandBuildContext.simple(access, FeatureFlags.VANILLA_SET));
        var source = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, 4, "audit", Component.literal("audit"), null, null);
        int commandCount = 0;
        try (var paths = Files.walk(root.resolve("datapack"))) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".mcfunction")).sorted().toList()) {
                int lineNumber = 0;
                for (String original : Files.readAllLines(path)) {
                    lineNumber++;
                    if (original.isBlank() || original.startsWith("#")) continue;
                    String command = original.replaceAll("ftdungeonmobs:skeleton_(warrior|archer|mage|king)", "minecraft:zombie");
                    var result = commands.getDispatcher().parse(command, source);
                    if (result.getReader().canRead() || result.getContext().getCommand() == null && result.getContext().getChild() == null) {
                        throw new IllegalStateException(path + ":" + lineNumber + " " + original + " => " + result.getExceptions());
                    }
                    commandCount++;
                }
            }
        }
        Files.createDirectories(root.resolve("qa"));
        var report = Map.of("passed",true,"spawn_count",spawnChecks.size(),"spawns",spawnChecks,"commands_parsed",commandCount,"command_check_scope","Vanilla 1.20.1 Brigadier grammar/SNBT with four FT entity IDs substituted by vanilla zombie only for parsing. Actual FT registry/assets and dimensions audited separately from installed JAR. No Forge execution or combat test.","spawn_shape_scope","Actual vanilla block collision AABBs against installed-mod entity dimensions, static EmptyBlockGetter; no mob pathfinding or gameplay.");
        Files.writeString(root.resolve("qa/encounter-audit.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS: " + spawnChecks.size() + " spawn boxes; " + commandCount + " command lines");
    }
}
