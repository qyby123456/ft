package net.farmingtales.frostl2verification;

import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("frostl2verification")
public final class VerifyL2 {
    public VerifyL2() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::verify);
    }

    private void verify(GatherDataEvent event) {
        try {
            run(Path.of(System.getProperty("ftfg.l2project")));
        } catch (Exception exception) {
            throw new IllegalStateException("L2 structure verification failed", exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void run(Path root) throws Exception {
        Path input = root.resolve("out/room.nbt");
        CompoundTag tag = NbtIo.readCompressed(input.toFile());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> states = new ArrayList<>();
        for (int index = 0; index < palette.size(); index++) {
            CompoundTag stateTag = palette.getCompound(index);
            var identifier = new net.minecraft.resources.ResourceLocation(stateTag.getString("Name"));
            require(identifier.getNamespace().equals("minecraft") && BuiltInRegistries.BLOCK.containsKey(identifier), "Non-vanilla/unknown block " + identifier);
            BlockState state = BuiltInRegistries.BLOCK.get(identifier).defaultBlockState();
            CompoundTag properties = stateTag.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                Property<?> property = state.getBlock().getStateDefinition().getProperty(key);
                require(property != null && property.getValue(properties.getString(key)).isPresent(), "Invalid property " + identifier + " " + key);
            }
            BlockState decoded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateTag);
            decoded.initCache();
            states.add(decoded);
        }
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ListTag entries = tag.getList("blocks", Tag.TAG_COMPOUND);
        int[] size = {21, 12, 21};
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            ListTag positionTag = entry.getList("pos", Tag.TAG_INT);
            BlockPos position = new BlockPos(positionTag.getInt(0), positionTag.getInt(1), positionTag.getInt(2));
            require(position.getX() >= 0 && position.getX() < size[0] && position.getY() >= 0 && position.getY() < size[1] && position.getZ() >= 0 && position.getZ() < size[2], "Out-of-bounds " + position);
            require(blocks.put(position, states.get(entry.getInt("state"))) == null, "Duplicate position " + position);
            require(!entry.contains("nbt"), "Unexpected block-entity NBT " + position);
        }
        require(entries.size() == 5292 && tag.getList("entities", Tag.TAG_COMPOUND).isEmpty(), "Unexpected volume/entity count");
        int water = 0;
        int snow = 0;
        int plants = 0;
        int lanterns = 0;
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos position = entry.getKey();
            BlockState state = entry.getValue();
            if (state.is(Blocks.WATER)) {
                water++;
                require(state.getFluidState().isSource(), "Flowing water " + position);
                for (Direction direction : Direction.values()) {
                    BlockPos adjacent = position.relative(direction);
                    BlockState neighbor = blocks.getOrDefault(adjacent, Blocks.AIR.defaultBlockState());
                    require(neighbor.is(Blocks.WATER) || neighbor.isFaceSturdy(EmptyBlockGetter.INSTANCE, adjacent, direction.getOpposite()), "Unsealed water face " + position + " -> " + direction);
                }
            }
            if (state.is(Blocks.SNOW)) {
                snow++;
                BlockPos below = position.below();
                BlockState support = blocks.getOrDefault(below, Blocks.AIR.defaultBlockState());
                require(!support.is(Blocks.ICE) && !support.is(Blocks.PACKED_ICE) && !support.isAir(), "Unsupported snow " + position);
                require(support.is(Blocks.SPRUCE_LEAVES) || support.isFaceSturdy(EmptyBlockGetter.INSTANCE, below, Direction.UP), "Non-full snow support " + position);
            }
            if (state.is(Blocks.BLUE_ORCHID) || state.is(Blocks.AZURE_BLUET) || state.is(Blocks.WHITE_TULIP) || state.is(Blocks.CORNFLOWER)) {
                plants++;
                require(blocks.get(position.below()).is(Blocks.GRASS_BLOCK), "Unsupported flower " + position);
                require(blocks.get(position.above()).is(Blocks.LIGHT), "Missing low-light flower illumination " + position);
            }
            if (state.is(Blocks.SOUL_LANTERN)) {
                lanterns++;
                Direction attachment = state.getValue(LanternBlock.HANGING) ? Direction.UP : Direction.DOWN;
                BlockPos supportPosition = position.relative(attachment);
                BlockState support = blocks.getOrDefault(supportPosition, Blocks.AIR.defaultBlockState());
                require(support.isFaceSturdy(EmptyBlockGetter.INSTANCE, supportPosition, attachment.getOpposite(), SupportType.CENTER), "Unsupported lantern " + position);
            }
        }
        require(water == 17 && plants == 8, "Unexpected water/flower count " + water + "/" + plants);
        for (int blockX = 9; blockX <= 11; blockX++) {
            for (int blockY = 1; blockY <= 3; blockY++) {
                require(blocks.get(new BlockPos(blockX, blockY, 0)).isAir(), "Blocked north opening");
            }
        }
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
        require(template.getSize().equals(new BlockPos(21, 12, 21)), "Unexpected structure size");
        CompoundTag roundtrip = template.save(new CompoundTag());
        require(roundtrip.getList("blocks", Tag.TAG_COMPOUND).size() == entries.size(), "Structure round-trip lost blocks");
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input)));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "PASS");
        result.put("minecraft", "1.20.1");
        result.put("sha256", digest);
        result.put("nbt_roundtrip", "PASS");
        result.put("palette_states", palette.size());
        result.put("volume_blocks", entries.size());
        result.put("sealed_source_water_cells", water);
        result.put("supported_snow_layers", snow);
        result.put("supported_lit_flowers", plants);
        result.put("supported_lanterns", lanterns);
        result.put("scope", "Actual Forge registry, StructureTemplate round-trip, full-face fluid containment and snow/plant/lantern support; not a client screenshot or installed world test.");
        Files.writeString(root.resolve("out/qa-minecraft.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n", StandardCharsets.UTF_8);
        System.out.println(new GsonBuilder().create().toJson(result));
    }
}
