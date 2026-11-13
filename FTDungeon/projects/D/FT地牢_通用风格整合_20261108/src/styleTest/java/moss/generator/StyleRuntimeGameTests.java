package moss.generator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftstylesqa")
@PrefixGameTestTemplate(false)
public final class StyleRuntimeGameTests {
    private static void rejected(GameTestHelper helper, Runnable action, String message) {
        boolean failed = false;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { failed = true; }
        helper.assertTrue(failed, message);
    }

    private static void finish(GameTestHelper helper, ServerLevel level, Engine.Job job, Runnable ready) {
        helper.assertTrue(!"FAILED".equals(job.stage), "Actual style build failed: " + job.plan.style + ":" + job.error);
        if ("COMPLETE".equals(job.stage)) {
            helper.assertTrue(Engine.temporaryTicketCount(level) == 0, "Style COMPLETE releases temporary tickets");
            ready.run();
            return;
        }
        Engine.tick(level, 32768);
        helper.runAfterDelay(1, () -> finish(helper, level, job, ready));
    }

    private static void force(ServerLevel level, Planner.Plan plan, boolean value) {
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (var piece : plan.pieces) {
            var box = Planner.bounds(piece);
            for (int east = (int) box.minX >> 4; east <= ((int) box.maxX - 1) >> 4; east++) {
                for (int south = (int) box.minZ >> 4; south <= ((int) box.maxZ - 1) >> 4; south++) {
                    if (seen.add(net.minecraft.world.level.ChunkPos.asLong(east, south))) level.setChunkForced(east, south, value);
                }
            }
        }
    }

    private static void gateBinding(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        for (var planned : MossBossPortals.gates(job.plan)) {
            var state = level.getBlockState(planned.anchor());
            helper.assertTrue(state.getBlock() instanceof DungeonGateBlock && state.getValue(DungeonGateBlock.PART) == 1,
                "Every style uses actual native bottom-center FT gate");
            helper.assertTrue(DungeonGateBlock.complete(level, planned.anchor(), planned.block(), planned.facing()), "Actual style gate has twelve complete native parts");
            var entity = (DungeonGateBlockEntity) level.getBlockEntity(planned.anchor());
            helper.assertTrue(entity != null && entity.generatorId().toString().equals(job.plan.style)
                && entity.generatorInstance().equals(job.plan.id), "Actual FT boss gate owns exact profile and instance");
            var metadata = entity.getPersistentData().getCompound(MossBossPortals.DATA_KEY);
            helper.assertTrue(metadata.getString("style").equals(job.plan.style) && metadata.getString("instance").equals(job.plan.id), "Portal NBT freezes style and instance");
            for (int part = 0; part < 3; part++) {
                BlockPos floor = DungeonGateBlock.partPosition(planned.anchor(), planned.facing(), part).below();
                helper.assertTrue(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP), "Profile geometry preserves all FT gate supports");
            }
        }
        helper.assertTrue(job.gameplay != null && job.plan.style.equals(job.gameplay.style), "Gameplay state preserves profile");
        var profile = Profiles.get(job.plan.style);
        for (int[] cap : job.plan.caps) {
            BlockPos bottom = Catalog.pos(cap);
            for (int rise = 0; rise < 5; rise++) {
                var expected = Profiles.state(rise == 0 || rise == 4 ? profile.capBlock : profile.barrierBlock);
                helper.assertTrue(level.getBlockState(bottom.above(rise)).equals(expected), "Actual cap geometry uses owning profile cap/barrier state");
            }
        }
        for (BlockPos cell : Gameplay.gateCells(job.plan)) helper.assertTrue(level.getBlockState(cell).equals(Profiles.state(profile.barrierBlock)), "Actual locked Boss barrier uses selected profile");
    }

    @GameTest(template = "empty", timeoutTicks = 40000)
    public static void desertFrostRealBuildStoreMenuAndStats(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(MossMod.DIMENSION);
        helper.assertTrue(level != null, "Styles share the actual forest engine dimension");
        rejected(helper, () -> Engine.plan(level, "ftmossgen:missing", 20261107, new BlockPos(0, 100, 0)), "Unknown profile cannot allocate a build");
        Engine.Job desert = Engine.plan(level, "ftmossgen:desert", 20261107, new BlockPos(0, 100, 0));
        Engine.start(level, desert.plan.id);
        finish(helper, level, desert, () -> {
            gateBinding(helper, level, desert);
            force(level, desert.plan, true);
            Engine.Job frost = Engine.plan(level, "ftmossgen:frost", 20261107, new BlockPos(8192, 100, 0));
            Engine.start(level, frost.plan.id);
            finish(helper, level, frost, () -> {
                gateBinding(helper, level, frost);
                force(level, frost.plan, true);
                crossStyleStore(helper, level, desert, frost);
                externalMenu(helper, level, desert);
                externalMenu(helper, level, frost);
                waveRules(helper, level, desert);
                waveRules(helper, level, frost);
                helper.assertTrue(Gameplay.startBoss(level, desert) && Gameplay.startBoss(level, frost), "Distinct profiles start independent actual Boss encounters");
                stats(helper, level, List.of(desert, frost), 160, () -> {
                    bossDeathRules(helper, level, desert, frost);
                });
            });
        });
    }

    private static void bossDeathRules(GameTestHelper helper, ServerLevel level, Engine.Job winner, Engine.Job frost) {
        Engine.Job nonplayer = Engine.plan(level, winner.plan.style, winner.plan.seed, new BlockPos(16384, 100, 0));
        List<Engine.Job> jobs = List.of(winner, frost, nonplayer);
        ServerPlayer killer = player(level, Catalog.pos(winner.gameplay.bossEntry));
        Runnable cleanup = () -> {
            killer.closeContainer();
            killer.serverLevel().removePlayerImmediately(killer, Entity.RemovalReason.DISCARDED);
            for (var job : jobs) {
                if (job.gameplay != null) {
                    for (var intent : job.gameplay.intents.values()) {
                        Entity entity = level.getEntity(UUID.fromString(intent.uuid));
                        if (entity != null && job.plan.id.equals(entity.getPersistentData().getString("ftmossgen.instance"))) entity.discard();
                    }
                }
                force(level, job.plan, false);
            }
        };
        guarded(cleanup, () -> {
            killer.setGameMode(GameType.SURVIVAL);
            Engine.start(level, nonplayer.plan.id);
            finishDeathBuild(helper, level, nonplayer, 20000, cleanup, () -> {
                force(level, nonplayer.plan, true);
                helper.assertTrue(Gameplay.startBoss(level, nonplayer), "Independent actual desert job starts nonplayer death encounter");
                awaitDeathBoss(helper, level, nonplayer, 160, cleanup, victim -> {
                    returnState(helper, level, winner, false);
                    returnState(helper, level, nonplayer, false);
                    helper.assertTrue(victim.hurt(level.damageSources().genericKill(), Float.MAX_VALUE) && !victim.isAlive(), "Actual desert Boss dies through nonplayer Forge death event");
                    helper.runAfterDelay(2, () -> guarded(cleanup, () -> {
                        Gameplay.tick(level);
                        helper.assertTrue("LOCKED".equals(nonplayer.gameplay.bossState) && !nonplayer.gameplay.rewardGranted
                            && "DEAD".equals(nonplayer.gameplay.intents.get(nonplayer.gameplay.bossUuid).status), "Nonplayer death records DEAD but never clears encounter or grants reward");
                        returnState(helper, level, nonplayer, false);
                        helper.assertTrue("FIGHTING".equals(winner.gameplay.bossState), "Other desert instance stays fighting after nonplayer death");
                        awaitDeathBoss(helper, level, winner, 160, cleanup, boss -> {
                            killer.teleportTo(level, boss.getX() + 2, boss.getY(), boss.getZ(), 0, 0);
                            helper.assertTrue(boss.hurt(level.damageSources().playerAttack(killer), Float.MAX_VALUE) && !boss.isAlive(), "Real ServerPlayer kills exact configured desert Boss through Forge death event");
                            helper.runAfterDelay(70, () -> guarded(cleanup, () -> {
                                Gameplay.tick(level);
                                helper.assertTrue("VICTORY".equals(winner.gameplay.bossState) && winner.gameplay.rewardGranted
                                    && killer.getUUID().toString().equals(winner.gameplay.victorUuid)
                                    && "DEAD".equals(winner.gameplay.intents.get(winner.gameplay.bossUuid).status), "Only matching Boss killed by real player clears desert encounter (VICTORY)");
                                returnState(helper, level, winner, true);
                                returnState(helper, level, nonplayer, false);
                                returnState(helper, level, frost, false);
                                helper.assertTrue("LOCKED".equals(nonplayer.gameplay.bossState) && "FIGHTING".equals(frost.gameplay.bossState), "Victory cannot unlock nonplayer or other-profile instance");
                                Store detached = Store.load(Store.get(level).save(new CompoundTag()));
                                helper.assertTrue("VICTORY".equals(detached.jobs.get(winner.plan.id).gameplay.bossState)
                                    && "LOCKED".equals(detached.jobs.get(nonplayer.plan.id).gameplay.bossState), "Cross-instance player victory and nonplayer lock survive Store reload");
                                cleanup.run();
                                System.out.println("STYLE_RUNTIME_PASS:desert+frost real builds;cross-style Store;FT profile bindings;production menu+provider entry/leave;profile Boss stats;desert real nonplayer death locked;real player Boss victory;12-part return unlock;death instance isolation");
                                helper.succeed();
                            }));
                        });
                    }));
                });
            });
        });
    }

    private static void guarded(Runnable cleanup, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException | AssertionError failure) {
            cleanup.run();
            throw failure;
        }
    }

    private static void finishDeathBuild(GameTestHelper helper, ServerLevel level, Engine.Job job, int attempts, Runnable cleanup, Runnable ready) {
        guarded(cleanup, () -> {
            helper.assertTrue(!"FAILED".equals(job.stage), "Desert death fixture build failed: " + job.error);
            if ("COMPLETE".equals(job.stage)) {
                helper.assertTrue(Engine.temporaryTicketCount(level) == 0, "Death fixture COMPLETE releases temporary tickets");
                ready.run();
            } else {
                helper.assertTrue(attempts > 0, "Desert death fixture build exceeded bounded wait");
                Engine.tick(level, 32768);
                helper.runAfterDelay(1, () -> finishDeathBuild(helper, level, job, attempts - 1, cleanup, ready));
            }
        });
    }

    private static void awaitDeathBoss(GameTestHelper helper, ServerLevel level, Engine.Job job, int attempts, Runnable cleanup,
            java.util.function.Consumer<net.minecraft.world.entity.Mob> ready) {
        guarded(cleanup, () -> {
            var intent = job.gameplay.intents.get(job.gameplay.bossUuid);
            helper.assertTrue(intent != null && "boss".equals(intent.role) && intent.uuid.equals(job.gameplay.bossUuid)
                && intent.type.equals(Profiles.get(job.plan.style).bossType) && job.plan.id.equals(job.gameplay.instanceId)
                && job.plan.style.equals(job.gameplay.style), "Boss intent freezes configured type, role, UUID, profile and instance");
            Entity entity = level.getEntity(UUID.fromString(intent.uuid));
            if (!(entity instanceof net.minecraft.world.entity.Mob boss)) {
                helper.assertTrue(attempts > 0, "Actual configured desert Mob Boss did not materialize");
                Gameplay.tick(level);
                helper.runAfterDelay(1, () -> awaitDeathBoss(helper, level, job, attempts - 1, cleanup, ready));
                return;
            }
            helper.assertTrue(boss.isAlive() && boss.getUUID().toString().equals(intent.uuid)
                && BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).toString().equals(intent.type)
                && "boss".equals(boss.getPersistentData().getString("ftmossgen.role"))
                && job.plan.id.equals(boss.getPersistentData().getString("ftmossgen.instance")), "Actual Mob Boss matches intent UUID/role/type/instance without concrete entity cast");
            boss.setNoAi(true);
            ready.accept(boss);
        });
    }

    private static void returnState(GameTestHelper helper, ServerLevel level, Engine.Job job, boolean open) {
        var gate = MossBossPortals.gates(job.plan).stream().filter(MossBossPortals.Gate::returning).findFirst().orElseThrow();
        var entity = (DungeonGateBlockEntity) level.getBlockEntity(gate.anchor());
        helper.assertTrue(entity != null && entity.generatorId().toString().equals(job.plan.style)
            && entity.generatorInstance().equals(job.plan.id), "Death-controlled return gate retains exact profile/instance binding");
        for (int part = 0; part < 12; part++) {
            var state = level.getBlockState(DungeonGateBlock.partPosition(gate.anchor(), gate.facing(), part));
            helper.assertTrue(state.getBlock() instanceof DungeonGateBlock && state.getValue(DungeonGateBlock.OPEN) == open
                && !state.getValue(DungeonGateBlock.MOVING), "All twelve return gate parts obey settled victory lock: " + job.plan.style);
        }
        for (BlockPos cell : Gameplay.gateCells(job.plan)) {
            helper.assertTrue(open ? level.getBlockState(cell).isAir()
                : level.getBlockState(cell).equals(Profiles.state(Profiles.get(job.plan.style).barrierBlock)), "Return barrier obeys instance victory and selected style material");
        }
    }

    private static void crossStyleStore(GameTestHelper helper, ServerLevel level, Engine.Job desert, Engine.Job frost) {
        CompoundTag tag = Store.get(level).save(new CompoundTag());
        Store detached = Store.load(tag.copy());
        Gson gson = new Gson();
        for (Engine.Job job : List.of(desert, frost)) {
            var loaded = detached.jobs.get(job.plan.id);
            helper.assertTrue(loaded != null && job.plan.style.equals(loaded.plan.style) && gson.toJson(job).equals(gson.toJson(loaded)), "Cross-profile Store reload preserves complete plans, style and gameplay");
        }
        JsonObject payload = JsonParser.parseString(tag.getString("payload")).getAsJsonObject();
        JsonObject plan = payload.getAsJsonObject("jobs").getAsJsonObject(desert.plan.id).getAsJsonObject("plan");
        plan.addProperty("fingerprint", "changed-profile-fingerprint");
        String changed = payload.toString();
        tag.putString("payload", changed);
        tag.putString("payloadHash", Catalog.hash(changed.getBytes(StandardCharsets.UTF_8)));
        rejected(helper, () -> Store.load(tag), "Changed profile fingerprint refuses recovery rather than resetting or rebuilding");
        helper.assertTrue(Store.get(level).jobs.get(desert.plan.id) == desert, "Rejected detached load leaves actual shared Store intact");
    }

    private static void stats(GameTestHelper helper, ServerLevel level, List<Engine.Job> jobs, int attempts, Runnable ready) {
        for (var job : jobs) {
            for (var intent : job.gameplay.intents.values()) {
                if (!"room".equals(intent.role) && !"boss_add".equals(intent.role)) continue;
                Entity spawned = level.getEntity(UUID.fromString(intent.uuid));
                if (!(spawned instanceof LivingEntity)) {
                    helper.assertTrue(attempts > 0, "Actual profile room/add entity did not materialize: " + intent.type);
                    Gameplay.tick(level);
                    helper.runAfterDelay(1, () -> stats(helper, level, jobs, attempts - 1, ready));
                    return;
                }
                helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(spawned.getType()).toString().equals(intent.type), "Actual room/add mob matches persisted profile-specific entity factory intent");
                if (spawned instanceof net.minecraft.world.entity.Mob mob) mob.setNoAi(true);
            }
            Entity entity = job.gameplay.bossUuid == null ? null : level.getEntity(UUID.fromString(job.gameplay.bossUuid));
            if (!(entity instanceof LivingEntity boss)) {
                helper.assertTrue(attempts > 0, "Profile boss did not materialize: " + job.plan.style);
                Gameplay.tick(level);
                helper.runAfterDelay(1, () -> stats(helper, level, jobs, attempts - 1, ready));
                return;
            }
            var profile = Profiles.get(job.plan.style);
            if (boss instanceof net.minecraft.world.entity.Mob mob) mob.setNoAi(true);
            helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).toString().equals(profile.bossType), "Actual Boss entity type comes from selected profile");
            helper.assertTrue(Math.abs(boss.getMaxHealth() - profile.bossHealth) < 0.001
                && Math.abs(boss.getAttributeValue(Attributes.ATTACK_DAMAGE) - profile.bossDamage) < 0.001, "Actual Boss health/damage match selected style rules");
            helper.assertTrue(boss.getName().getString().equals(profile.bossName), "Actual Boss display name comes from profile");
        }
        var desert = Profiles.get(jobs.get(0).plan.style);
        var frost = Profiles.get(jobs.get(1).plan.style);
        helper.assertTrue(!desert.bossType.equals(frost.bossType) || desert.bossHealth != frost.bossHealth || desert.bossDamage != frost.bossDamage,
            "Built-in desert/frost differ in actual combat rules, not just display text");
        ready.run();
    }

    private static void waveRules(GameTestHelper helper, ServerLevel level, Engine.Job job) {
        var profile = Profiles.get(job.plan.style);
        var room = job.gameplay.rooms.stream().filter(candidate -> !candidate.points.isEmpty()
            && candidate.points.size() <= profile.instanceCap - 5).findFirst().orElseThrow();
        helper.assertTrue(Gameplay.startWave(level, job, room), "Selected style starts an actual room wave");
        helper.assertTrue(room.nextWaveAt == level.getGameTime() + profile.roomCooldownTicks, "Room cooldown comes from selected profile");
        helper.assertTrue(!Gameplay.startWave(level, job, room), "Style room cannot bypass its shared cooldown");
        helper.assertTrue(room.active.size() == room.points.size() && Gameplay.livingReservations(job.gameplay) <= profile.instanceCap,
            "Wave respects profile per-instance reservations and covers legal room points");
        for (int index = 0; index < room.active.size(); index++) {
            var intent = job.gameplay.intents.get(room.active.get(index));
            String expected = "ranged".equals(room.points.get(index).kind) ? profile.ranged : profile.melee;
            helper.assertTrue(intent != null && intent.type.equals(expected), "Prepared actual room entity type is profile-specific");
        }
    }

    private static ServerPlayer player(ServerLevel level, BlockPos anchor) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "StyleNativeQA"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        player.setGameMode(GameType.CREATIVE);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setPos(anchor.getX() + 0.5, anchor.getY() + 0.25, anchor.getZ() + 0.5);
        level.addNewPlayer(player);
        return player;
    }

    private static void externalMenu(GameTestHelper helper, ServerLevel forest, Engine.Job job) {
        ServerLevel overworld = forest.getServer().overworld();
        BlockPos anchor = new BlockPos(job.plan.style.endsWith("desert") ? 500000 : 500100, 150, 500000);
        DungeonGateBlock block = FTDungeonMobs.DUNGEON_ENTRY_GATE.get();
        for (int part = 0; part < 12; part++) {
            BlockPos point = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
            if (part < 3) overworld.setBlockAndUpdate(point.below(), Blocks.STONE.defaultBlockState());
            overworld.setBlock(point, block.defaultBlockState().setValue(DungeonGateBlock.PART, part).setValue(DungeonGateBlock.FACING, Direction.NORTH), 18);
        }
        overworld.setBlockAndUpdate(anchor.east(3).below(), Blocks.STONE.defaultBlockState());
        overworld.setBlockAndUpdate(anchor.east(3), FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState().setValue(BloodthornBrazierBlock.TIER, 2));
        ServerPlayer player = player(overworld, anchor.south(2));
        try {
            DungeonGateGenerators.open(player, anchor);
            helper.assertTrue(player.containerMenu instanceof DungeonGeneratorMenu, "Production external FT menu actually opens");
            var menu = player.containerMenu;
            var providers = DungeonGenerators.all();
            int selected = -1;
            for (String style : List.of("ftmossgen:forest", "ftmossgen:desert", "ftmossgen:frost", "ftmossgen:obsidian")) {
                int index = -1;
                for (int current = 0; current < providers.size(); current++) if (providers.get(current).id().toString().equals(style)) index = current;
                helper.assertTrue(index >= 0 && index < 20 && !menu.getSlot(index).getItem().isEmpty(), "Automatic production menu exposes builtin/config JSON profile: " + style);
                helper.assertTrue(!providers.get(index).supportsRegeneration(), "Style provider never advertises legacy dungeon regeneration: " + style);
                var profile = Profiles.get(style);
                var icon = menu.getSlot(index).getItem();
                helper.assertTrue(BuiltInRegistries.ITEM.getKey(icon.getItem()).toString().equals(profile.icon)
                    && icon.getHoverName().getString().startsWith(profile.title), "Native menu renders actual JSON profile icon and title: " + style);
                if (style.equals("ftmossgen:obsidian")) {
                    menu.clicked(index, 0, ClickType.PICKUP, player);
                    var externalGate = (DungeonGateBlockEntity) overworld.getBlockEntity(anchor);
                    helper.assertTrue(externalGate.generatorId().toString().equals(style) && menu.getCarried().isEmpty(), "Config-only obsidian style is selectable through actual native menu");
                }
                if (style.equals(job.plan.style)) selected = index;
            }
            helper.assertTrue(selected >= 0, "Built profile appears in menu");
            menu.clicked(selected, 0, ClickType.PICKUP, player);
            var gate = (DungeonGateBlockEntity) overworld.getBlockEntity(anchor);
            helper.assertTrue(gate.generatorId().toString().equals(job.plan.style) && menu.getCarried().isEmpty(), "Actual menu selects requested profile without taking icon");
            gate.configureGenerator(new ResourceLocation(job.plan.style), job.plan.seed);
            int count = Store.get(forest).jobs.size();
            DungeonGateGenerators.prepare(player, gate);
            DungeonGateGenerators.prepare(player, gate);
            helper.assertTrue(gate.generatorInstance().equals(job.plan.id) && Store.get(forest).jobs.size() == count, "Provider reuses COMPLETE matching style+seed rather than another style with same seed");
            menu.clicked(22, 0, ClickType.PICKUP, player);
            menu.broadcastChanges();
            helper.assertTrue(menu.getSlot(20).getItem().isEmpty(), "Refreshed bound Moss menu leaves legacy regeneration slot empty");
            for (int part = 0; part < 12; part++) {
                BlockPos point = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
                overworld.setBlock(point, overworld.getBlockState(point).setValue(DungeonGateBlock.OPEN, true).setValue(DungeonGateBlock.MOVING, false), 18);
            }
            player.setGameMode(GameType.SURVIVAL);
            var origin = player.position();
            helper.assertTrue(player.containerMenu == menu && menu.stillValid(player), "Bound production menu remains open until actual enter-button click");
            menu.clicked(25, 0, ClickType.PICKUP, player);
            helper.assertTrue(player.containerMenu != menu && player.serverLevel() == forest
                && player.blockPosition().distSqr(Catalog.pos(job.plan.entry)) < 4
                && gate.generatorId().toString().equals(job.plan.style) && gate.generatorInstance().equals(job.plan.id), "Actual menu enter button closes itself and enters exact original style instance through production travel");
            DungeonGenerators.find(new ResourceLocation(job.plan.style)).orElseThrow().leave(player);
            helper.assertTrue(player.serverLevel() == overworld && player.position().distanceToSqr(origin) < 0.001 && !DungeonGateGenerators.automaticAllowed(player, anchor), "Style provider returns to source and prevents immediate automatic reentry");
        } finally {
            player.closeContainer();
            player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }
}
