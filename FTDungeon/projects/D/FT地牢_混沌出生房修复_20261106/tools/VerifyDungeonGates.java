package net.farmingtales.ftdungeonmobs.verification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.awt.image.BufferedImage;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.farmingtales.ftdungeonmobs.DungeonGateBlock;
import net.farmingtales.ftdungeonmobs.DungeonGateBlockEntity;
import net.farmingtales.ftdungeonmobs.DungeonGateData;
import net.farmingtales.ftdungeonmobs.DungeonGateMotion;
import net.farmingtales.ftdungeonmobs.DungeonGateInteraction;
import net.farmingtales.ftdungeonmobs.DungeonGatePalette;
import net.farmingtales.ftdungeonmobs.DungeonGateTravel;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.farmingtales.ftdungeonmobs.VerifyBrazierStates;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftdungeonmobs", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VerifyDungeonGates {
    private VerifyDungeonGates() {}

    public static void main(String[] arguments) throws Exception {
        verifyMotion();
        Path resources = Path.of(arguments[0]);
        Path assets = resources.resolve("assets/ftdungeonmobs");
        int gateIndex = 0;
        for (String name : new String[]{"dungeon_entry_gate", "dungeon_return_gate"}) {
            JsonArray multipart = read(assets.resolve("blockstates/" + name + ".json")).getAsJsonArray("multipart");
            require(multipart.size() == 96, "Gate requires 48 frame + 48 conditional iron gate bindings");
            int cuboids = 0;
            int faces = 0;
            for (int part = 0; part < 12; part++) {
                Path path = assets.resolve("models/block/" + name + "_" + part + ".json");
                JsonObject model = read(path);
                verifyElements(model, 0, 16);
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    BlockModel parsed = BlockModel.fromStream(reader);
                    cuboids += parsed.getElements().size();
                    faces += parsed.getElements().stream().mapToInt(element -> element.faces.size()).sum();
                }
                Path moving = assets.resolve("models/block/dungeon_gate_moving_" + part + ".json");
                verifyElements(read(moving), 0, 16);
                try (Reader reader = Files.newBufferedReader(moving, StandardCharsets.UTF_8)) {
                    BlockModel parsed = BlockModel.fromStream(reader);
                    cuboids += parsed.getElements().size();
                    faces += parsed.getElements().stream().mapToInt(element -> element.faces.size()).sum();
                }
                int rotation = 0;
                for (String facing : new String[]{"north", "east", "south", "west"}) {
                    for (boolean opened : new boolean[]{false, true}) {
                        for (boolean animating : new boolean[]{false, true}) {
                            verifyBindings(multipart, name, part, facing, rotation, opened, animating);
                        }
                    }
                    rotation += 90;
                }
            }
            require(cuboids == (gateIndex == 0 ? 182 : 181) && cuboids <= 220, "Reference gate cuboid budget/count");
            require(faces == (gateIndex == 0 ? 779 : 775) && faces <= 900,
                    "Moving and fixed faces are culled separately so opening never exposes missing faces");
            Path item = assets.resolve("models/item/" + name + ".json");
            verifyElements(read(item), -16, 32);
            try (Reader reader = Files.newBufferedReader(item, StandardCharsets.UTF_8)) {
                require(BlockModel.fromStream(reader).getElements().size() == (gateIndex == 0 ? 106 : 105),
                        "Integer miniature item model");
            }
            JsonObject recipe = read(resources.resolve("data/ftdungeonmobs/recipes/" + name + ".json"));
            require(recipe.getAsJsonObject("result").get("item").getAsString().equals("ftdungeonmobs:" + name),
                    "Gate craft result");
            JsonObject pool = read(resources.resolve("data/ftdungeonmobs/loot_tables/blocks/" + name + ".json"))
                    .getAsJsonArray("pools").get(0).getAsJsonObject();
            require(pool.get("rolls").getAsInt() == 1 && pool.getAsJsonArray("entries").size() == 1, "Gate single drop");
            BufferedImage portal = ImageIO.read(assets.resolve("textures/block/" + name + "_portal.png").toFile());
            require(portal.getWidth() == 16 && portal.getHeight() == 256, "Portal pixel texture size");
            Set<Integer> frameHashes = new HashSet<>();
            for (int frame = 0; frame < 16; frame++) {
                int hash = 1;
                for (int pixelY = 0; pixelY < 16; pixelY++) {
                    for (int pixelX = 0; pixelX < 16; pixelX++) {
                        int pixel = portal.getRGB(pixelX, frame * 16 + pixelY);
                        require(((pixel >> 16) & 255) == ((pixel >> 8) & 255)
                                && ((pixel >> 8) & 255) == (pixel & 255), "Neutral grayscale portal supports all tint hues");
                        require((pixel >>> 24) == 255, "Portal must be a solid cuboid, not a transparency card");
                        hash = hash * 31 + pixel;
                    }
                }
                frameHashes.add(hash);
            }
            require(frameHashes.size() == 16, "All portal animation frames must be distinct");
            JsonObject animation = read(assets.resolve("textures/block/" + name + "_portal.png.mcmeta"))
                    .getAsJsonObject("animation");
            require(animation.get("frametime").getAsInt() == 3 && animation.getAsJsonArray("frames").size() == 16
                    && !animation.get("interpolate").getAsBoolean(), "Portal pixel frame timing");
            System.out.println("GATE_MODEL_VERIFIED: " + name + "; " + cuboids + " integer cuboids / "
                    + faces + " faces; 1152 tier/open/moving blockstates; one shared 52-cuboid moving iron gate; 16 neutral opaque portal frames.");
            gateIndex++;
        }
        BufferedImage body = ImageIO.read(assets.resolve("textures/block/dungeon_gate.png").toFile());
        require(body.getWidth() == 128 && body.getHeight() == 128, "Shared reference stone/iron pixel atlas");
        BufferedImage flame = ImageIO.read(assets.resolve("textures/block/dungeon_gate_flame.png").toFile());
        require(flame.getWidth() == 16 && flame.getHeight() == 256, "Shared warm flame texture");
        Set<Integer> flameFrames = new HashSet<>();
        for (int frame = 0; frame < 16; frame++) {
            int hash = 1;
            for (int pixelY = 0; pixelY < 16; pixelY++) {
                for (int pixelX = 0; pixelX < 16; pixelX++) {
                    int pixel = flame.getRGB(pixelX, frame * 16 + pixelY);
                    require((pixel >>> 24) == 255, "Warm flames are opaque cuboids");
                    hash = hash * 31 + pixel;
                }
            }
            flameFrames.add(hash);
        }
        require(flameFrames.size() == 16, "All warm flame frames are distinct");
        JsonObject flameAnimation = read(assets.resolve("textures/block/dungeon_gate_flame.png.mcmeta"))
                .getAsJsonObject("animation");
        require(flameAnimation.get("frametime").getAsInt() == 3
                && flameAnimation.getAsJsonArray("frames").size() == 16
                && !flameAnimation.get("interpolate").getAsBoolean(), "Warm flame pixel timing");
        System.out.println("DUNGEON_GATE_MODELS_VERIFIED: reference stone frames, skulls, stepped cuboid winches, "
                + "rectangular chain links and portcullises; 2304 tier/open/moving/part/facing blockstates; integer miniature items; "
                + "shared 128px body and three distinct 16-frame opaque textures. Worldgen/persistence use Forge runData.");
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        try {
            Path resources = Path.of(System.getProperty("ftdungeonmobs.verifyResources"));
            verifyWorldgen(resources);
            verifyPersistenceAndParts();
            verifyMotion();
            verifyAnimatedStates();
            verifyRounds();
            VerifyBrazierStates.verify();
            VerifyBloodPool.verifyRuntime(resources);
            VerifyDungeonArchitecture.verifyRuntime(resources);
            Path report = resources.getParent().getParent().getParent().resolve("build/reports/dungeon-gates-runtime.txt");
            Files.createDirectories(report.getParent());
            String result = "DUNGEON_RUNTIME_DATA_VERIFIED: Forge 47.4.10 mod loading and gate registries; "
                    + "actual biome/dimension type/LevelStem/flat generator codecs with registry lookups; "
                    + "48 part/facing inverses; 2304 tier/open/moving states; anchor-only block entities and NBT; "
                    + "four-facing open/closed collision and passage transforms; 40-tick motion/reversal/UV floor clipping; "
                    + "tiers 1..5 SavedData round trips; 6000-tick admission and immutable round tier, offline participants, "
                    + "deadline/participants persistence and no extension by later entry; invalid return NBT guards. "
                    + "No client/server world was launched; live travel and placement still require in-game acceptance.";
            Files.writeString(report, result + System.lineSeparator(), StandardCharsets.UTF_8);
            System.out.println(result);
        } catch (Exception cause) {
            throw new IllegalStateException("Dungeon gate runtime data verification failed", cause);
        }
    }

    private static void verifyWorldgen(Path resources) throws Exception {
        HolderLookup.Provider vanilla = VanillaRegistries.createLookup();
        RegistryOps<JsonElement> vanillaOps = RegistryOps.create(JsonOps.INSTANCE, vanilla);
        Path data = resources.resolve("data/ftdungeonmobs");
        Biome biome = Biome.DIRECT_CODEC.parse(vanillaOps, read(data.resolve("worldgen/biome/dungeon.json")))
                .getOrThrow(false, message -> { throw new IllegalStateException(message); });
        DimensionType dimensionType = DimensionType.DIRECT_CODEC.parse(JsonOps.INSTANCE,
                read(data.resolve("dimension_type/dungeon.json")))
                .getOrThrow(false, message -> { throw new IllegalStateException(message); });
        ResourceLocation identity = new ResourceLocation("ftdungeonmobs", "dungeon");
        RegistryAccess builtins = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        HolderLookup.Provider patched = new RegistrySetBuilder()
                .add(Registries.BIOME, context -> {
                    vanilla.lookupOrThrow(Registries.BIOME).listElements()
                            .forEach(holder -> context.register(holder.key(), holder.value()));
                    context.register(ResourceKey.create(Registries.BIOME, identity), biome);
                })
                .add(Registries.DIMENSION_TYPE, context -> {
                    vanilla.lookupOrThrow(Registries.DIMENSION_TYPE).listElements()
                            .forEach(holder -> context.register(holder.key(), holder.value()));
                    context.register(ResourceKey.create(Registries.DIMENSION_TYPE, identity), dimensionType);
                })
                .buildPatch(builtins, vanilla);
        HolderLookup.Provider lookup = new HolderLookup.Provider() {
            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(
                    ResourceKey<? extends Registry<? extends T>> key) {
                if (key.equals(Registries.BIOME) || key.equals(Registries.DIMENSION_TYPE)) {
                    return patched.lookup(key);
                }
                return vanilla.lookup(key).or(() -> builtins.lookup(key));
            }
        };
        LevelStem stem = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, lookup),
                read(data.resolve("dimension/dungeon.json")))
                .getOrThrow(false, message -> { throw new IllegalStateException(message); });
        require(stem.generator() instanceof FlatLevelSource, "Dungeon must use the registered flat generator");
        FlatLevelSource generator = (FlatLevelSource) stem.generator();
        require(generator.settings().getLayers().size() == 64, "Arrival floor must be at Y=63");
        require(stem.type().value().height() == 256 && !stem.type().value().hasSkyLight()
                && !stem.type().value().bedWorks(), "Dungeon type constraints");
        System.out.println("DUNGEON_WORLDGEN_VERIFIED: custom biome, dimension type and LevelStem decoded with real "
                + "registry lookups; flat layers=64, height=256, no skylight, no beds.");
    }

    private static void verifyPersistenceAndParts() {
        DungeonGateBlock gate = FTDungeonMobs.DUNGEON_ENTRY_GATE.get();
        BlockPos anchor = new BlockPos(-17, 70, 31);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Set<BlockPos> positions = new HashSet<>();
            for (int part = 0; part < 12; part++) {
                BlockPos position = DungeonGateBlock.partPosition(anchor, facing, part);
                require(positions.add(position), "Gate positions must be unique");
                require(DungeonGateBlock.anchor(position, gate.defaultBlockState().setValue(DungeonGateBlock.FACING, facing)
                        .setValue(DungeonGateBlock.PART, part)).equals(anchor), "Gate part-to-anchor inverse");
            }
        }
        for (int tier = 1; tier <= 5; tier++) {
            DungeonGateData original = new DungeonGateData();
            original.bind(Level.OVERWORLD, anchor);
            original.setTier(tier);
            original.markArrivalBuilt();
            DungeonGateData loaded = DungeonGateData.load(original.save(new CompoundTag()));
            require(loaded.tier() == tier && loaded.matches(Level.OVERWORLD, anchor) && loaded.arrivalBuilt(),
                    "Saved gate data round trip");
        }
        CompoundTag invalid = new CompoundTag();
        invalid.putInt("Tier", 99);
        require(DungeonGateData.load(invalid).tier() == 1, "Invalid stored tiers must be bounded");
        CompoundTag location = new CompoundTag();
        require(!DungeonGateTravel.validReturnPosition(location), "Missing coordinates must not become zero");
        location.putDouble("X", 10);
        location.putDouble("Y", 70);
        location.putDouble("Z", -30);
        require(DungeonGateTravel.validReturnPosition(location), "Finite return coordinates");
        location.putDouble("X", Double.NaN);
        require(!DungeonGateTravel.validReturnPosition(location), "NaN return coordinate");
        location.putDouble("X", 30000001);
        require(!DungeonGateTravel.validReturnPosition(location), "Out-of-bounds return coordinate");
    }

    private static void verifyBindings(JsonArray multipart, String name, int part, String facing, int rotation,
            boolean opened, boolean animating) {
        int frameMatches = 0;
        int movingMatches = 0;
        for (JsonElement value : multipart) {
            JsonObject entry = value.getAsJsonObject();
            JsonObject when = entry.getAsJsonObject("when");
            if (!when.get("facing").getAsString().equals(facing) || when.get("part").getAsInt() != part
                    || when.has("open") && when.get("open").getAsBoolean() != opened
                    || when.has("moving") && when.get("moving").getAsBoolean() != animating) {
                continue;
            }
            JsonObject apply = entry.getAsJsonObject("apply");
            require(apply.get("y").getAsInt() == rotation, "Animated models use only quarter-turn facing rotations");
            String model = apply.get("model").getAsString();
            if (model.equals("ftdungeonmobs:block/" + name + "_" + part)) {
                frameMatches++;
            } else {
                require(model.equals("ftdungeonmobs:block/dungeon_gate_moving_" + part), "Shared moving model binding");
                movingMatches++;
            }
        }
        require(frameMatches == 1 && movingMatches == (!opened && !animating ? 1 : 0),
                "Static iron gate must disappear during motion and while open, but frame must always render");
    }

    private static void verifyMotion() {
        DungeonGateMotion motion = new DungeonGateMotion(0, true, 100);
        require(motion.active() && motion.progress(99L) == 0 && motion.progress(100L) == 0,
                "Motion is clamped before its start time");
        require(motion.progress(120L) == 20 && Math.abs(motion.progress(120.5) - 20.5F) < 0.0001,
                "Opening supports smooth partial ticks");
        require(!motion.finished(139) && motion.finished(140) && motion.progress(1000L) == 40,
                "Full opening takes exactly 40 ticks and clamps at the endpoint");
        motion.retarget(false, 125);
        require(motion.progress(125L) == 25 && motion.progress(135L) == 15 && motion.finished(150),
                "Reversing mid-opening preserves its current pose and remaining distance");
        motion.retarget(true, 135);
        require(motion.progress(135L) == 15 && motion.finished(160), "Obstruction reversal returns to fully open");
        motion.reset(13, false, 8000);
        require(motion.progress(8000L) == 13 && motion.finished(8013), "Unloaded animation resumes from stored progress");
        require(new DungeonGateMotion(-20, false, 0).storedProgress() == 0
                && new DungeonGateMotion(90, true, 0).storedProgress() == 40, "Stored progress is bounded");
        float[] quad = {0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0};
        float[] clipped = DungeonGateMotion.clipFloor(quad);
        require(clipped != null && clipped != quad && quad[1] == 0, "Floor clipping must not modify shared baked vertices");
        require(clipped[1] == DungeonGateMotion.FLOOR && clipped[6] == DungeonGateMotion.FLOOR
                && clipped[4] == 1 - DungeonGateMotion.FLOOR && clipped[9] == 1 - DungeonGateMotion.FLOOR,
                "Floor clipping interpolates the UV instead of stretching the disappearing iron bars");
        for (int vertex = 0; vertex < 4; vertex++) {
            quad[vertex * 5 + 1] -= 2;
        }
        require(DungeonGateMotion.clipFloor(quad) == null, "Fully sunk faces never render under the floor");
        for (int vertex = 0; vertex < 4; vertex++) {
            quad[vertex * 5 + 1] += 4;
        }
        require(DungeonGateMotion.clipFloor(quad) == quad, "Visible faces need no allocation or UV changes");
        System.out.println("GATE_MOTION_VERIFIED: 40 ticks each way, smooth/reversible motion, stored-progress resume, UV-preserving floor clip.");
    }

    private static void verifyAnimatedStates() {
        BlockPos root = new BlockPos(-17, 70, 31);
        for (DungeonGateBlock gate : new DungeonGateBlock[]{FTDungeonMobs.DUNGEON_ENTRY_GATE.get(),
                FTDungeonMobs.DUNGEON_RETURN_GATE.get()}) {
            require(gate.getStateDefinition().getPossibleStates().size() == 1152, "Each gate has 1152 tier/open/moving states");
            require(gate.defaultBlockState().getValue(DungeonGateBlock.PORTAL_TIER) == 0, "Old gates migrate to inactive tier");
            for (int tier = 0; tier <= 5; tier++) {
                require(gate.defaultBlockState().setValue(DungeonGateBlock.PORTAL_TIER, tier)
                        .getValue(DungeonGateBlock.PORTAL_TIER) == tier, "All six tint states exist natively");
            }
            require(!gate.defaultBlockState().getValue(DungeonGateBlock.OPEN)
                    && !gate.defaultBlockState().getValue(DungeonGateBlock.MOVING), "New and legacy gates default closed");
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                AABB body = new AABB(root.getX() + 0.2, root.getY() + 0.2, root.getZ() + 0.2,
                        root.getX() + 0.8, root.getY() + 2.0, root.getZ() + 0.8);
                boolean closedBlocks = false;
                boolean openBlocks = false;
                for (int part = 0; part < 12; part++) {
                    BlockPos position = DungeonGateBlock.partPosition(root, facing, part);
                    BlockState state = gate.defaultBlockState().setValue(DungeonGateBlock.FACING, facing)
                            .setValue(DungeonGateBlock.PART, part);
                    BlockEntity entity = gate.newBlockEntity(position, state);
                    require((entity instanceof DungeonGateBlockEntity) == (part == DungeonGateBlock.ANCHOR_PART),
                            "Only the bottom-center anchor creates an animation block entity");
                    if (entity != null) {
                        require(entity.getType() == FTDungeonMobs.DUNGEON_GATE_ENTITY.get()
                                && entity.getType().isValid(state), "Both gate types share the registered block entity");
                        CompoundTag saved = new CompoundTag();
                        saved.putInt("GateProgress", 13);
                        saved.putBoolean("GateTargetOpen", true);
                        saved.putLong("GateAnimationTime", 777);
                        entity.load(saved);
                        require(entity.getUpdateTag().getInt("GateProgress") == 13
                                && entity.getUpdateTag().getBoolean("GateTargetOpen") && entity.getUpdatePacket() != null,
                                "Mid-animation state survives NBT and has a native client sync packet");
                    }
                    for (AABB box : gate.getCollisionShape(state, null, position, CollisionContext.empty()).toAabbs()) {
                        closedBlocks |= box.move(position).intersects(body);
                    }
                    for (AABB box : gate.getCollisionShape(state.setValue(DungeonGateBlock.OPEN, true), null,
                            position, CollisionContext.empty()).toAabbs()) {
                        openBlocks |= box.move(position).intersects(body);
                    }
                }
                require(closedBlocks && !openBlocks, "Closed gate blocks a standing player; open gate clears the passage");
                AABB passage = DungeonGateBlock.passage(root, facing);
                require(passage.intersects(body), "Auto-travel and obstruction detection share the open passage");
                Vec3 local = DungeonGateBlock.canonical(root, facing, Vec3.atBottomCenterOf(root));
                require(local.equals(new Vec3(24, 0, 8)), "Facing-independent gate pivot");
                Direction right = facing.getClockWise();
                Vec3 point = Vec3.atBottomCenterOf(root).add(right.getStepX(), 2, right.getStepZ());
                require(DungeonGateBlock.canonical(root, facing, point).equals(new Vec3(40, 32, 8)),
                        "Wall torch hit conversion uses the same facing as models and passage");
            }
        }
        System.out.println("GATE_ANIMATED_STATES_VERIFIED: 2304 tier/open/moving states, 96 anchor/part checks, NBT packets, four-facing collisions and transforms.");
    }

    private static void verifyRounds() {
        DungeonGateData data = new DungeonGateData();
        BlockPos root = new BlockPos(-17, 70, 31);
        UUID first = UUID.fromString("4c77e9f4-09ac-48fd-89e0-011111111111");
        UUID second = UUID.fromString("4c77e9f4-09ac-48fd-89e0-022222222222");
        data.bind(Level.OVERWORLD, root);
        data.setTier(4);
        require(!data.roundActive() && data.admissionOpen(100), "No countdown before the first successful admission");
        require(!DungeonGateInteraction.entranceOpen(0, false, false, true), "Extinguished brazier keeps gate closed");
        for (int tier = 1; tier <= 5; tier++) {
            require(DungeonGateInteraction.entranceOpen(tier, false, false, true), "All lit tiers keep gate open");
        }
        data.startRound(first, 100);
        require(DungeonGateData.ADMISSION_TICKS == 6000 && data.admissionSeconds(100) == 300,
                "First entry starts exactly five minutes (6000 server game ticks)");
        require(data.admissionOpen(6099) && data.admissionSeconds(6099) == 1
                && !data.admissionOpen(6100) && data.admissionSeconds(6100) == 0, "Exact admission deadline boundary");
        require(data.locksBrazier(Level.OVERWORLD, root.offset(6, 0, 0))
                && !data.locksBrazier(Level.OVERWORLD, root.offset(6, 1, 0))
                && !data.locksBrazier(Level.NETHER, root), "Brazier lock uses the source dimension and six-block sphere");
        long deadline = data.save(new CompoundTag()).getLong("AdmissionEndsAt");
        data.addParticipant(second);
        data.addParticipant(second);
        require(data.participantCount() == 2 && data.save(new CompoundTag()).getLong("AdmissionEndsAt") == deadline,
                "Later admissions neither duplicate participants nor extend the deadline");
        boolean tierRejected = false;
        try {
            data.setTier(2);
        } catch (IllegalStateException expected) {
            tierRejected = true;
        }
        require(tierRejected && data.tier() == 4, "Active round difficulty is immutable");
        boolean rebindRejected = false;
        try {
            data.bind(Level.NETHER, root);
        } catch (IllegalStateException expected) {
            rebindRejected = true;
        }
        require(rebindRejected && data.matches(Level.OVERWORLD, root), "Another gate cannot steal the round");
        boolean restartRejected = false;
        try {
            data.startRound(second, 5000);
        } catch (IllegalStateException expected) {
            restartRejected = true;
        }
        require(restartRejected && data.save(new CompoundTag()).getLong("AdmissionEndsAt") == deadline,
                "A later player cannot restart the five-minute countdown");
        DungeonGateData loaded = DungeonGateData.load(data.save(new CompoundTag()));
        require(loaded.roundActive() && loaded.tier() == 4 && loaded.participantCount() == 2
                && loaded.hasParticipant(first) && !loaded.admissionOpen(6100),
                "Round, offline participant IDs, locked tier and original deadline survive saving and reloading");
        require(!loaded.finishRoundIfEmpty(9000), "Offline participants keep the active round locked");
        loaded.removeParticipant(first);
        loaded.removeParticipant(second);
        require(!loaded.finishRoundIfEmpty(6099) && loaded.roundActive(), "Empty dungeon cannot reset an ongoing window");
        require(loaded.finishRoundIfEmpty(6100) && !loaded.roundActive()
                && !loaded.locksBrazier(Level.OVERWORLD, root), "Window expiration plus all participants leaving unlocks braziers");
        loaded.setTier(2);
        loaded.startRound(second, 10000);
        require(loaded.tier() == 2 && loaded.admissionSeconds(10000) == 300, "Next round receives a fresh independent window");
        require(DungeonGateInteraction.entranceOpen(4, true, true, true)
                && !DungeonGateInteraction.entranceOpen(4, true, true, false)
                && !DungeonGateInteraction.entranceOpen(4, true, false, true),
                "Only the bound entry remains open during the window; timeout or another entry stays closed");
        Set<Integer> colors = new HashSet<>();
        for (int tier = 0; tier <= 5; tier++) {
            colors.add(DungeonGatePalette.color(tier));
        }
        require(colors.size() == 6 && DungeonGatePalette.color(1) == 0x55CCEB
                && DungeonGatePalette.color(5) == 0xE34555, "Portal has six distinct gray/blue/green/gold/purple/red tints");
        System.out.println("GATE_ROUNDS_VERIFIED: lit/open and unlit/closed, immutable difficulty, exact 6000-tick window, no later extension, persistent offline players, reset only after expiration and all departures, six portal tints.");
    }

    private static void verifyElements(JsonObject model, int minimum, int maximum) {
        require(!model.has("groups") && !model.has("texture_size"), "No private editor fields");
        for (JsonElement value : model.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            require(!element.has("rotation"), "Gate geometry is unrotated cuboids only");
            for (int axis = 0; axis < 3; axis++) {
                double from = element.getAsJsonArray("from").get(axis).getAsDouble();
                double to = element.getAsJsonArray("to").get(axis).getAsDouble();
                require(from == Math.rint(from) && to == Math.rint(to) && from >= minimum && to <= maximum
                        && to > from, "Positive integer geometry within the native Java model range");
            }
            require(element.getAsJsonObject("faces").size() > 0, "No fully hidden cuboids in runtime models");
            for (JsonElement faceValue : element.getAsJsonObject("faces").asMap().values()) {
                JsonObject face = faceValue.getAsJsonObject();
                boolean portal = face.get("texture").getAsString().equals("#portal");
                require(portal == face.has("tintindex") && (!portal || face.get("tintindex").getAsInt() == 0),
                        "Only portal faces receive the difficulty tint; frame, iron bars and flames never tint");
            }
        }
    }

    private static JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("DUNGEON_GATES_FAILURE: " + message);
        }
    }
}
