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
    private static final List<String> STYLES = List.of("fteclipsegen:clockwork", "fteclipsegen:forest", "fteclipsegen:desert", "fteclipsegen:frost");
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

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void distinctPlansStoreReloadAndLegacyClockwork(GameTestHelper helper) {
        Set<String> ids = new HashSet<>();
        Set<String> palettes = new HashSet<>();
        Store store = new Store();
        for (int index = 0; index < STYLES.size(); index++) {
            String style = STYLES.get(index);
            Profiles.get(style);
            Planner.Plan sameAnchor = Planner.create(style, SEED, new BlockPos(0, 100, 0));
            helper.assertTrue(ids.add(sameAnchor.id), "Same seed and anchor have distinct style IDs: " + style);
            helper.assertTrue(Catalog.GSON.toJson(sameAnchor).equals(Catalog.GSON.toJson(Planner.create(style, SEED, new BlockPos(0, 100, 0)))), "Style plan is deterministic");
            helper.assertTrue(style.equals(sameAnchor.style) && Profiles.get(style).fingerprint.equals(sameAnchor.styleFingerprint), "Plan freezes actual style identity and fingerprint");
            helper.assertTrue(palettes.add(materials(sameAnchor)), "Actual decoded placement materials differ for every style");
            Engine.Job job = planned(Planner.create(style, SEED, new BlockPos(index * 4096, 100, 0)));
            store.jobs.put(job.plan.id, job);
        }
        CompoundTag saved = store.save(new CompoundTag());
        CompoundTag untouched = saved.copy();
        Store restored = Store.load(saved);
        helper.assertTrue(saved.equals(untouched) && Catalog.GSON.toJson(store.jobs).equals(Catalog.GSON.toJson(restored.jobs)), "Four-style Store roundtrip preserves full jobs without source mutation");
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
        System.out.println("ECLIPSE_STYLE_STORE_PASS:four styles;same-seed distinct IDs;actual palette differences;full Store reload;legacy clockwork;unknown/fingerprint failclosed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80000)
    public static void actualMenuFourBuildsStyleIsolationAndEntry(GameTestHelper helper) {
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
            System.out.println("ECLIPSE_STYLE_RUNTIME_PASS:four real builds;native menu selection+button entry;distinct style+seed reservation;actual palette placement;Store reload;provider leave");
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
            var providers = DungeonGenerators.all();
            int selected = -1;
            for (String style : STYLES) {
                int providerIndex = -1;
                for (int index = 0; index < providers.size(); index++) if (providers.get(index).id().toString().equals(style)) providerIndex = index;
                helper.assertTrue(providerIndex >= 0 && providerIndex < 20, "Every middle-dungeon style appears in real first menu page: " + style);
                var provider = providers.get(providerIndex);
                var icon = menu.getSlot(providerIndex).getItem();
                helper.assertTrue(!icon.isEmpty() && icon.getItem() == provider.icon().getItem() && icon.getHoverName().getString().startsWith(provider.title().getString()), "Actual native menu displays exact style provider icon/title");
                menu.clicked(providerIndex, 0, ClickType.PICKUP, player);
                var gate = (DungeonGateBlockEntity) source.getBlockEntity(anchor);
                helper.assertTrue(gate.generatorId().toString().equals(style) && menu.getCarried().isEmpty(), "Each middle-dungeon style is actually selectable without taking its icon");
                if (style.equals(job.plan.style)) selected = providerIndex;
            }
            menu.clicked(selected, 0, ClickType.PICKUP, player);
            var gate = (DungeonGateBlockEntity) source.getBlockEntity(anchor);
            gate.configureGenerator(new ResourceLocation(job.plan.style), SEED);
            int jobs = Store.get(dungeon).jobs.size();
            DungeonGateGenerators.prepare(player, gate);
            DungeonGateGenerators.prepare(player, gate);
            helper.assertTrue(job.plan.id.equals(gate.generatorInstance()) && Store.get(dungeon).jobs.size() == jobs, "Actual provider prepare reuses only exact matching COMPLETE style+seed");
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
