package eclipse.generator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftdungeonmobs.BloodthornBrazierBlock;
import net.farmingtales.ftdungeonmobs.DungeonGateBlock;
import net.farmingtales.ftdungeonmobs.DungeonGateBlockEntity;
import net.farmingtales.ftdungeonmobs.DungeonGateGenerators;
import net.farmingtales.ftdungeonmobs.DungeonGeneratorMenu;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerators;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fteclipsestylesqa")
@PrefixGameTestTemplate(false)
public final class EclipseStyleGameTests {
    private static final List<String> STYLES = List.of("fteclipsegen:clockwork", "fteclipsegen:forest", "fteclipsegen:desert", "fteclipsegen:frost", "fteclipsegen:swamp", "fteclipsegen:coral", "fteclipsegen:lava", "fteclipsegen:amethyst");
    private static final long SEED = 20261109L;

    private static void rejected(GameTestHelper helper, Runnable action, String reason) {
        boolean failed = false;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { failed = true; }
        helper.assertTrue(failed, reason);
    }

    private static Engine.Job planned(Planner.Plan plan) {
        Engine.Job job = new Engine.Job();
        job.plan = plan;
        job.planHash = Catalog.hash(Catalog.GSON.toJson(plan).getBytes(StandardCharsets.UTF_8));
        return job;
    }

    private static String materials(Planner.Plan plan) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            for (String key : Catalog.get().templates.keySet().stream().sorted().toList()) {
                for (var cell : Profiles.blocks(plan, key)) digest.update((key + ":" + cell.position() + ":" + cell.state() + "\n").getBytes(StandardCharsets.UTF_8));
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static String geometry(Planner.Plan plan) {
        StringBuilder signature = new StringBuilder();
        int redesigned = 0;
        for (String key : Catalog.get().templates.keySet().stream().sorted().toList()) {
            var oldCells = Catalog.blocks(key);
            var newCells = Profiles.blocks(plan, key);
            helperGeometrySize(oldCells, newCells);
            boolean changed = false;
            for (int index = 0; index < oldCells.size(); index++) {
                var before = oldCells.get(index);
                var after = newCells.get(index);
                Catalog.require(before.position().equals(after.position()), "Themed room coordinates match canonical dense order");
                if (before.state().isAir() != after.state().isAir()) changed = true;
                signature.append(after.state().isAir() ? '0' : '1');
            }
            if (changed) redesigned++;
        }
        Catalog.require(redesigned == Catalog.get().templates.size(), "Every template geometry per style must be independently redesigned");
        return Catalog.hash(signature.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void helperGeometrySize(List<Catalog.Cell> original, List<Catalog.Cell> themed) {
        Catalog.require(original.size() == themed.size(), "Themed room dense cell count preserves original dimensions");
    }

    private static void interfaces(Planner.Plan plan) {
        for (var template : Catalog.get().templates.values()) {
            var cells = new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
            for (var cell : Profiles.blocks(plan, template.key)) cells.put(cell.position(), cell.state());
            for (var port : template.ports) for (int[] point : port.cells) {
                BlockPos feet = Catalog.pos(point);
                for (int rise = 0; rise < 5; rise++) Catalog.require(cells.get(feet.above(rise)).isAir(), "Themed port cap clearance: " + plan.style + "/" + template.key);
                Catalog.require(cells.get(feet.below()).isFaceSturdy(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, feet.below(), Direction.UP), "Supported themed port");
            }
            for (var socket : template.sockets) {
                BlockPos feet = Catalog.pos(socket.position);
                for (int rise = 0; rise < 3; rise++) Catalog.require(cells.get(feet.above(rise)).isAir(), "Themed rune must not replace architecture: " + plan.style + "/" + template.key + "/" + feet);
                Catalog.require(cells.get(Catalog.pos(socket.support)).isFaceSturdy(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, Catalog.pos(socket.support), Direction.UP), "Themed rune full support");
            }
        }
    }

    private static void balancedLayouts(Planner.Plan plan) {
        Catalog.require(RoomSets.balancedLayouts(plan.style), "Rebuilt room sets opt in to balanced layout selection");
        var uses = new java.util.HashMap<String, Integer>();
        for (var weighted : Catalog.get().pools.get("straight/room/layout")) uses.put(weighted.template, 0);
        for (var piece : plan.pieces) if (piece.child != null) {
            int minimum = uses.values().stream().mapToInt(Integer::intValue).min().orElseThrow();
            Catalog.require(uses.get(piece.child) == minimum, "Room child repeats only after every available layout has been used");
            uses.merge(piece.child, 1, Integer::sum);
        }
    }

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void distinctPlansStoreReloadAndLegacyClockwork(GameTestHelper helper) {
        Set<String> ids = new HashSet<>();
        Set<String> palettes = new HashSet<>();
        Set<String> geometries = new HashSet<>();
        Store store = new Store();
        for (int index = 0; index < STYLES.size(); index++) {
            String style = STYLES.get(index);
            Profiles.get(style);
            Planner.Plan sameAnchor = Planner.create(style, SEED, new BlockPos(0, 100, 0));
            helper.assertTrue(ids.add(sameAnchor.id), "Same seed and anchor have distinct style IDs: " + style);
            helper.assertTrue(Catalog.GSON.toJson(sameAnchor).equals(Catalog.GSON.toJson(Planner.create(style, SEED, new BlockPos(0, 100, 0)))), "Style plan is deterministic");
            helper.assertTrue(style.equals(sameAnchor.style) && Profiles.get(style).fingerprint.equals(sameAnchor.styleFingerprint), "Plan freezes actual style identity and fingerprint");
            helper.assertTrue(palettes.add(materials(sameAnchor)), "Actual decoded placement materials differ for every style");
            helper.assertTrue(sameAnchor.roomSetFingerprint != null, "New built-in plans freeze independent room set identity");
            helper.assertTrue(geometries.add(geometry(sameAnchor)), "Every themed room set has distinct solid/air geometry, not merely a palette swap");
            interfaces(sameAnchor);
            balancedLayouts(sameAnchor);
            Planner.Plan badRooms = Catalog.GSON.fromJson(Catalog.GSON.toJson(sameAnchor), Planner.Plan.class);
            badRooms.roomSetFingerprint = "changed";
            rejected(helper, () -> Profiles.blocks(badRooms, "straight/room_cells"), "Changed room set refuses fallback");
            Planner.Plan priorStyle = Planner.create(style, SEED, new BlockPos(65536 + index * 4096, 100, 0));
            priorStyle.roomSetFingerprint = null;
            priorStyle.id = Catalog.hash(("style-v1:" + style + ":" + priorStyle.styleFingerprint + ":" + SEED + ":" + Catalog.pos(priorStyle.anchor).asLong() + ":" + priorStyle.fingerprint).getBytes(StandardCharsets.UTF_8)).substring(0, 16);
            helper.assertTrue(!priorStyle.id.equals(sameAnchor.id), "Old palette-only and new themed instances have different identities");
            helper.assertTrue(Profiles.blocks(priorStyle, "straight/room_cells").size() == Catalog.blocks("straight/room_cells").size(), "Old style instance continues to use original template geometry");
            Engine.Job priorJob = planned(priorStyle);
            store.jobs.put(priorStyle.id, priorJob);
            String historyIndex = "data/fteclipsegen/roomsets-history/" + style.substring("fteclipsegen:".length()) + "/index.json";
            String previousFingerprint = Catalog.GSON.fromJson(new String(Catalog.resource(historyIndex), StandardCharsets.UTF_8), String.class);
            Planner.Plan archived = Planner.create(style, SEED, new BlockPos(131072 + index * 4096, 100, 0));
            archived.roomSetFingerprint = previousFingerprint;
            archived.id = Catalog.hash(("rooms-v1:" + style + ":" + archived.styleFingerprint + ":" + previousFingerprint + ":" + SEED + ":" + Catalog.pos(archived.anchor).asLong() + ":" + archived.fingerprint).getBytes(StandardCharsets.UTF_8)).substring(0, 16);
            helper.assertTrue(!previousFingerprint.equals(sameAnchor.roomSetFingerprint), "Entrance and Boss redesign creates a new room set version");
            helper.assertTrue(!materials(archived).equals(materials(sameAnchor)), "Frozen previous room set decodes its original architecture");
            store.jobs.put(archived.id, planned(archived));
            String recentIndex = historyIndex.replace("index.json", "v033.json");
            String recentFingerprint = Catalog.GSON.fromJson(new String(Catalog.resource(recentIndex), StandardCharsets.UTF_8), String.class);
            Planner.Plan recent = Planner.create(style, SEED, new BlockPos(196608 + index * 4096, 100, 0));
            recent.roomSetFingerprint = recentFingerprint;
            recent.id = Catalog.hash(("rooms-v1:" + style + ":" + recent.styleFingerprint + ":" + recentFingerprint + ":" + SEED + ":" + Catalog.pos(recent.anchor).asLong() + ":" + recent.fingerprint).getBytes(StandardCharsets.UTF_8)).substring(0, 16);
            helper.assertTrue(!recentFingerprint.equals(sameAnchor.roomSetFingerprint) && !materials(recent).equals(materials(sameAnchor)), "0.33 room sets preserve their frozen architecture separately from all-room rebuilds");
            store.jobs.put(recent.id, planned(recent));
            Engine.Job job = planned(Planner.create(style, SEED, new BlockPos(index * 4096, 100, 0)));
            store.jobs.put(job.plan.id, job);
        }
        CompoundTag saved = store.save(new CompoundTag());
        CompoundTag untouched = saved.copy();
        Store restored = Store.load(saved);
        helper.assertTrue(saved.equals(untouched) && Catalog.GSON.toJson(store.jobs).equals(Catalog.GSON.toJson(restored.jobs)), "Eight-style Store roundtrip preserves full jobs without source mutation");
        rejected(helper, () -> Profiles.get("fteclipsegen:missing"), "Unknown profile refuses fallback");
        rejected(helper, () -> Planner.create("fteclipsegen:missing", SEED, BlockPos.ZERO), "Unknown plan style refuses fallback");
        JsonObject changed = JsonParser.parseString(saved.getString("payload")).getAsJsonObject();
        changed.getAsJsonObject("jobs").entrySet().iterator().next().getValue().getAsJsonObject().getAsJsonObject("plan").addProperty("styleFingerprint", "changed");
        CompoundTag bad = saved.copy();
        String payload = changed.toString();
        bad.putString("payload", payload);
        bad.putString("payloadHash", Catalog.hash(payload.getBytes(StandardCharsets.UTF_8)));
        rejected(helper, () -> Store.load(bad), "Changed style fingerprint fails closed despite valid payload hash");
        Planner.Plan legacy = Planner.create(SEED, new BlockPos(0, 100, 32768));
        JsonObject originalFormat = JsonParser.parseString(Catalog.GSON.toJson(legacy)).getAsJsonObject();
        originalFormat.remove("style");
        originalFormat.remove("styleFingerprint");
        originalFormat.remove("roomSetFingerprint");
        legacy = Catalog.GSON.fromJson(originalFormat, Planner.Plan.class);
        legacy.id = Catalog.hash(("v1:" + legacy.seed + ":" + Catalog.pos(legacy.anchor).asLong() + ":" + legacy.fingerprint).getBytes(StandardCharsets.UTF_8)).substring(0, 16);
        originalFormat.addProperty("id", legacy.id);
        String originalHash = Catalog.hash(Catalog.GSON.toJson(originalFormat).getBytes(StandardCharsets.UTF_8));
        helper.assertTrue(!originalFormat.has("style") && !originalFormat.has("styleFingerprint")
            && originalHash.equals(Catalog.hash(Catalog.GSON.toJson(legacy).getBytes(StandardCharsets.UTF_8))), "Original raw JSON plan hash is unchanged when new null fields are omitted");
        Store old = new Store();
        Engine.Job oldJob = planned(legacy);
        oldJob.planHash = originalHash;
        old.jobs.put(legacy.id, oldJob);
        CompoundTag oldTag = old.save(new CompoundTag());
        var rawLegacy = JsonParser.parseString(oldTag.getString("payload")).getAsJsonObject().getAsJsonObject("jobs").getAsJsonObject(legacy.id).getAsJsonObject("plan");
        helper.assertTrue(!rawLegacy.has("style") && !rawLegacy.has("styleFingerprint")
            && originalHash.equals(Catalog.hash(Catalog.GSON.toJson(rawLegacy).getBytes(StandardCharsets.UTF_8))), "Encoded legacy fixture actually omits both fields and retains exact original frozen hash rule");
        Store oldReload = Store.load(oldTag);
        helper.assertTrue(Catalog.GSON.toJson(old.jobs).equals(Catalog.GSON.toJson(oldReload.jobs)) && Profiles.DEFAULT.equals(Profiles.style(oldReload.jobs.get(legacy.id).plan)), "Style-less old clockwork plan/hash/ID survives unchanged and resolves to clockwork");
        helper.assertTrue(originalHash.equals(oldReload.jobs.get(legacy.id).planHash)
            && legacy.id.equals(oldReload.jobs.get(legacy.id).plan.id), "Legacy reload never substitutes new style-v1 ID or rehashes original clockwork plan");
        for (var template : Catalog.get().templates.values()) {
            helper.assertTrue(Catalog.blocks(template.key).equals(Profiles.blocks(legacy, template.key)), "Legacy clockwork decoded blockstates remain exactly unchanged");
        }
        System.out.println("ECLIPSE_STYLE_STORE_PASS:eight styles;same-seed distinct IDs;actual palette differences;full Store reload;legacy clockwork;unknown/fingerprint failclosed");
        System.out.println("ECLIPSE_ROOM_HISTORY_PASS:eight archived room sets;original frozen IDs and payloads;new entrance and boss architecture;unknown history rejected");
        System.out.println("ECLIPSE_ROOM_REBUILD_PASS:216 independently rebuilt templates;balanced child selection;0.32 and 0.33 frozen room sets");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80000)
    public static void actualMenuEightBuildsStyleIsolationAndEntry(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(EclipseMod.DIMENSION);
        helper.assertTrue(level != null, "Actual shared Eclipse dimension loaded");
        int count = Store.get(level).jobs.size();
        rejected(helper, () -> Engine.plan(level, "fteclipsegen:missing", SEED, BlockPos.ZERO), "Unknown style fails before allocating live job");
        helper.assertTrue(Store.get(level).jobs.size() == count, "Rejected style does not allocate live job");
        buildNext(helper, level, new ArrayList<>(), 0);
    }

    private static void buildNext(GameTestHelper helper, ServerLevel level, List<Engine.Job> jobs, int index) {
        if (index == STYLES.size()) {
            Store reloaded = Store.load(Store.get(level).save(new CompoundTag()));
            for (var job : jobs) helper.assertTrue(Catalog.GSON.toJson(job).equals(Catalog.GSON.toJson(reloaded.jobs.get(job.plan.id))), "Completed actual style build survives full Store reload");
            System.out.println("ECLIPSE_STYLE_RUNTIME_PASS:eight real builds;native menu selection+button entry;distinct style+seed reservation;actual palette placement;Store reload;provider leave");
            helper.succeed();
            return;
        }
        Engine.Job job = Engine.plan(level, STYLES.get(index), SEED, new BlockPos(index * 4096, 100, 0));
        for (var previous : jobs) helper.assertTrue(!previous.plan.id.equals(job.plan.id), "Same-seed live jobs never reuse another style");
        jobs.add(job);
        Engine.start(level, job.plan.id);
        awaitBuild(helper, level, job, 19000, () -> {
            placed(helper, level, job);
            menu(helper, level, job, index);
            buildNext(helper, level, jobs, index + 1);
        });
    }

    private static void awaitBuild(GameTestHelper helper, ServerLevel level, Engine.Job job, int attempts, Runnable ready) {
        helper.assertTrue(!"FAILED".equals(job.stage), "Actual Eclipse style build failed: " + job.plan.style + ":" + job.error);
        if ("COMPLETE".equals(job.stage)) {
            ready.run();
        } else {
            helper.assertTrue(attempts > 0, "Actual style build bounded wait exhausted");
            Engine.tick(level, 32768);
            helper.runAfterDelay(1, () -> awaitBuild(helper, level, job, attempts - 1, ready));
        }
    }

    private static void placed(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        int checked = 0;
        for (var piece : job.plan.pieces) {
            if (piece.key.startsWith("boss/")) continue;
            var original = Catalog.blocks(piece.key);
            var styled = Profiles.blocks(job.plan, piece.key);
            for (int index = 0; index < original.size(); index++) {
                var before = original.get(index);
                var after = styled.get(index);
                if ((!Profiles.DEFAULT.equals(job.plan.style) && before.state().equals(after.state()))
                    || before.nbt() != null || after.state().isAir() || after.state().hasBlockEntity()) continue;
                BlockPos position = Planner.transform(Catalog.vec(after.position()), piece.size, piece.rotation, piece.origin);
                if (piece.sockets.stream().anyMatch(socket -> position.equals(Catalog.pos(socket.position)) || position.equals(Catalog.pos(socket.support)))) continue;
                helper.assertTrue(level.getBlockState(position).equals(after.state().rotate(Rotation.values()[piece.rotation])), "World contains exact selected style palette state at native transformed position");
                checked++;
                if (checked == 24) return;
            }
        }
        helper.assertTrue(checked > 0, "Completed style build includes verified actual material samples");
    }

    private static ServerPlayer player(ServerLevel level, BlockPos point) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "EclipseStyleQA"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        player.setGameMode(GameType.CREATIVE);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setPos(point.getX() + 0.5, point.getY(), point.getZ() + 0.5);
        level.addNewPlayer(player);
        return player;
    }

    private static void menu(GameTestHelper helper, ServerLevel dungeon, Engine.Job job, int ordinal) {
        ServerLevel source = dungeon.getServer().overworld();
        BlockPos anchor = new BlockPos(600000 + ordinal * 100, 150, 600000);
        DungeonGateBlock block = FTDungeonMobs.DUNGEON_ENTRY_GATE.get();
        for (int part = 0; part < 12; part++) {
            BlockPos point = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
            if (part < 3) source.setBlockAndUpdate(point.below(), Blocks.STONE.defaultBlockState());
            source.setBlock(point, block.defaultBlockState().setValue(DungeonGateBlock.PART, part).setValue(DungeonGateBlock.FACING, Direction.NORTH), 18);
        }
        source.setBlockAndUpdate(anchor.east(3).below(), Blocks.STONE.defaultBlockState());
        source.setBlockAndUpdate(anchor.east(3), FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState().setValue(BloodthornBrazierBlock.TIER, 2));
        ServerPlayer player = player(source, anchor.south(2));
        try {
            DungeonGateGenerators.open(player, anchor);
            helper.assertTrue(player.containerMenu instanceof DungeonGeneratorMenu, "Actual FT style selection menu opens");
            var menu = player.containerMenu;
            var gate = (DungeonGateBlockEntity) source.getBlockEntity(anchor);
            var originalId = gate.generatorId();
            int jobsBeforeNavigation = Store.get(dungeon).jobs.size();
            boolean originalSeedPresent = gate.hasGeneratorSeed();
            long originalSeed = gate.generatorSeed();
            String originalInstance = gate.generatorInstance();
            helper.assertTrue(menu.getSlot(0).getItem().getHoverName().getString().contains("小型群系")
                && menu.getSlot(1).getItem().getHoverName().getString().contains("中等地牢（月蚀）"), "Root retains small slot0 and exposes medium category slot1");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(menu.getSlot(20).getItem().getHoverName().getString().contains("返回地牢分类"), "Small category retains return slot20");
            menu.clicked(20, 0, ClickType.PICKUP, player);
            menu.clicked(1, 0, ClickType.PICKUP, player);
            menu.clicked(24, 0, ClickType.PICKUP, player);
            menu.clicked(25, 0, ClickType.PICKUP, player);
            helper.assertTrue(gate.generatorId().equals(originalId) && gate.hasGeneratorSeed() == originalSeedPresent
                && gate.generatorSeed() == originalSeed && gate.generatorInstance().equals(originalInstance)
                && player.containerMenu == menu && Store.get(dungeon).jobs.size() == jobsBeforeNavigation, "Medium navigation and unselected seed/generate buttons never mutate previous gate or enter another category");
            menu.clicked(20, 0, ClickType.PICKUP, player);
            helper.assertTrue(gate.generatorId().equals(originalId) && gate.generatorInstance().equals(originalInstance), "Returning from medium category never selects a generator");
            menu.clicked(1, 0, ClickType.PICKUP, player);
            var providers = DungeonGenerators.all().stream().filter(provider -> provider.category()
                == net.farmingtales.ftdungeonmobs.generator.DungeonGenerator.Category.MEDIUM).toList();
            helper.assertTrue(providers.size() == STYLES.size(), "Medium category exposes exactly eight expected style providers");
            int selected = -1;
            for (String style : STYLES) {
                int providerIndex = -1;
                for (int index = 0; index < providers.size(); index++) if (providers.get(index).id().toString().equals(style)) providerIndex = index;
                helper.assertTrue(providerIndex >= 0 && providerIndex < 20, "Every middle-dungeon style appears in real first menu page: " + style);
                var provider = providers.get(providerIndex);
                var icon = menu.getSlot(providerIndex).getItem();
                helper.assertTrue(!icon.isEmpty() && icon.getItem() == provider.icon().getItem() && icon.getHoverName().getString().startsWith(provider.title().getString()), "Actual native menu displays exact style provider icon/title");
                menu.clicked(providerIndex, 0, ClickType.PICKUP, player);
                helper.assertTrue(gate.generatorId().toString().equals(style) && menu.getCarried().isEmpty(), "Each middle-dungeon style is actually selectable without taking its icon");
                if (style.equals(job.plan.style)) selected = providerIndex;
            }
            menu.clicked(selected, 0, ClickType.PICKUP, player);
            gate.configureGenerator(new ResourceLocation(job.plan.style), SEED);
            int jobs = Store.get(dungeon).jobs.size();
            DungeonGateGenerators.prepare(player, gate);
            DungeonGateGenerators.prepare(player, gate);
            helper.assertTrue(job.plan.id.equals(gate.generatorInstance()) && Store.get(dungeon).jobs.size() == jobs, "Actual provider prepare reuses only exact matching COMPLETE style+seed");
            menu.clicked((selected + 1) % STYLES.size(), 0, ClickType.PICKUP, player);
            menu.clicked(24, 0, ClickType.PICKUP, player);
            helper.assertTrue(gate.generatorId().toString().equals(job.plan.style) && gate.generatorSeed() == SEED
                && job.plan.id.equals(gate.generatorInstance()), "Bound medium gate rejects style and seed replacement");
            menu.clicked(20, 0, ClickType.PICKUP, player);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            menu.clicked(24, 0, ClickType.PICKUP, player);
            menu.clicked(25, 0, ClickType.PICKUP, player);
            helper.assertTrue(player.serverLevel() == source && player.containerMenu == menu
                && gate.generatorId().toString().equals(job.plan.style) && gate.generatorSeed() == SEED
                && job.plan.id.equals(gate.generatorInstance()), "Small category never seeds or enters a bound medium instance before selecting its own style");
            menu.clicked(20, 0, ClickType.PICKUP, player);
            menu.clicked(1, 0, ClickType.PICKUP, player);
            helper.assertTrue(gate.generatorId().toString().equals(job.plan.style) && gate.generatorSeed() == SEED
                && job.plan.id.equals(gate.generatorInstance()), "Bound category roundtrip preserves exact style seed and instance");
            menu.clicked(22, 0, ClickType.PICKUP, player);
            for (int part = 0; part < 12; part++) {
                BlockPos point = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
                source.setBlock(point, source.getBlockState(point).setValue(DungeonGateBlock.OPEN, true).setValue(DungeonGateBlock.MOVING, false), 18);
            }
            player.setGameMode(GameType.SURVIVAL);
            var origin = player.position();
            menu.clicked(25, 0, ClickType.PICKUP, player);
            helper.assertTrue(player.containerMenu != menu && player.serverLevel() == dungeon && player.blockPosition().distSqr(Catalog.pos(job.plan.entry)) < 4, "Real enter-button click enters exact selected middle style instance");
            DungeonGenerators.find(new ResourceLocation(job.plan.style)).orElseThrow().leave(player);
            helper.assertTrue(player.serverLevel() == source && player.position().distanceToSqr(origin) < 0.001
                && !player.getPersistentData().contains("fteclipsegen_return"), "Style provider leaves to source and consumes original return record");
        } finally {
            player.closeContainer();
            player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }
}
