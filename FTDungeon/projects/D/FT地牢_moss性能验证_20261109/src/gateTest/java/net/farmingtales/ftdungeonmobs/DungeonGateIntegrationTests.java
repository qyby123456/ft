package net.farmingtales.ftdungeonmobs;

import com.mojang.authlib.GameProfile;
import eclipse.generator.EclipseMod;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerator;
import net.farmingtales.ftdungeonmobs.generator.DungeonGeneratorRegistrationEvent;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftgateintegration")
@PrefixGameTestTemplate(false)
public final class DungeonGateIntegrationTests {
    private static final ResourceLocation ECLIPSE = new ResourceLocation("fteclipsegen", "clockwork");
    private static final ResourceLocation UNKNOWN = new ResourceLocation("ftgateintegration", "missing_provider");
    private static final long SEED = 20261106L;

    private static void rejected(GameTestHelper helper, Runnable operation, String message) {
        boolean failed = false;
        try {
            operation.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            failed = true;
        }
        helper.assertTrue(failed, message);
    }

    private static DungeonGateBlockEntity copy(DungeonGateBlockEntity original) {
        DungeonGateBlockEntity restored = new DungeonGateBlockEntity(original.getBlockPos(), original.getBlockState());
        restored.load(original.saveWithoutMetadata());
        return restored;
    }

    private static DungeonGateBlockEntity gate(GameTestHelper helper, ServerLevel level, BlockPos anchor, int tier) {
        for (int offsetX = -3; offsetX <= 4; offsetX++) {
            for (int offsetZ = -3; offsetZ <= 4; offsetZ++) {
                BlockPos floor = anchor.offset(offsetX, -1, offsetZ);
                level.setBlock(floor, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                for (int height = 1; height <= 5; height++) {
                    level.setBlock(floor.above(height), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
        DungeonGateBlock block = FTDungeonMobs.DUNGEON_ENTRY_GATE.get();
        for (int part = 0; part < 12; part++) {
            BlockPos position = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
            level.setBlock(position, block.defaultBlockState().setValue(DungeonGateBlock.FACING, Direction.NORTH)
                .setValue(DungeonGateBlock.PART, part), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        brazier(level, anchor, tier);
        helper.assertTrue(DungeonGateBlock.complete(level, anchor, block, Direction.NORTH), "All twelve native gate parts form an actual complete gate");
        helper.assertTrue(level.getBlockEntity(anchor) instanceof DungeonGateBlockEntity, "Actual registered gate block entity exists");
        return (DungeonGateBlockEntity) level.getBlockEntity(anchor);
    }

    private static void brazier(ServerLevel level, BlockPos anchor, int tier) {
        level.setBlock(anchor.offset(3, 0, 0), FTDungeonMobs.BLOODTHORN_BRAZIER.get().defaultBlockState()
            .setValue(BloodthornBrazierBlock.TIER, tier), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    private static void opened(ServerLevel level, BlockPos anchor) {
        for (int part = 0; part < 12; part++) {
            BlockPos position = DungeonGateBlock.partPosition(anchor, Direction.NORTH, part);
            level.setBlock(position, level.getBlockState(position).setValue(DungeonGateBlock.OPEN, true)
                .setValue(DungeonGateBlock.MOVING, false), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    private static final class TestPlayer extends ServerPlayer {
        boolean operator;

        TestPlayer(ServerLevel level, boolean operator) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "GateNativeQA"));
            this.operator = operator;
            connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), this) {
                @Override public void send(Packet<?> packet) {}
                @Override public void send(Packet<?> packet, PacketSendListener listener) {}
            };
            setGameMode(GameType.SURVIVAL);
            setInvulnerable(true);
            setNoGravity(true);
        }

        @Override public boolean hasPermissions(int permission) { return permission <= (operator ? 2 : 0); }
    }

    private static TestPlayer player(ServerLevel level, BlockPos anchor, boolean operator) {
        TestPlayer player = new TestPlayer(level, operator);
        near(player, anchor);
        level.addNewPlayer(player);
        return player;
    }

    private static void near(ServerPlayer player, BlockPos anchor) {
        player.setPos(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 2.5);
    }

    private static void remove(ServerPlayer player) {
        player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }

    public static void verifyCompletedEclipseGate(GameTestHelper helper, ServerPlayer player, eclipse.generator.Engine.Job winner) {
        ServerLevel overworld = helper.getLevel().getServer().overworld();
        BlockPos anchor = new BlockPos(503000, 100, 500000);
        DungeonGateBlockEntity gate = gate(helper, overworld, anchor, 2);
        gate.configureGenerator(ECLIPSE, winner.plan.seed);
        player.teleportTo(overworld, anchor.getX() + .5, anchor.getY(), anchor.getZ() + 2.5, 0, 0);
        player.setGameMode(GameType.CREATIVE);
        int count = eclipse.generator.Store.get(player.server.getLevel(EclipseMod.DIMENSION)).jobs.size();
        DungeonGateGenerators.prepare(player, gate);
        DungeonGateGenerators.prepare(player, gate);
        var selected = eclipse.generator.Store.get(player.server.getLevel(EclipseMod.DIMENSION)).jobs.get(gate.generatorInstance());
        helper.assertTrue(selected != null && "COMPLETE".equals(selected.stage) && selected.plan.seed == winner.plan.seed,
                "Actual Eclipse adapter reuses a COMPLETE instance with the frozen seed, excluding the failed reservation");
        helper.assertTrue(eclipse.generator.Store.get(player.server.getLevel(EclipseMod.DIMENSION)).jobs.size() == count,
                "Repeated native adapter prepare does not duplicate COMPLETE dungeon");
        opened(overworld, anchor);
        player.setGameMode(GameType.SURVIVAL);
        var origin = player.position();
        DungeonGateTravel.use(player, anchor, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false);
        helper.assertTrue(player.serverLevel().dimension().equals(EclipseMod.DIMENSION), "Native FT gate enters actual completed Eclipse instance");
        helper.assertTrue(player.blockPosition().distSqr(eclipse.generator.Catalog.pos(selected.plan.entry)) < 4,
                "Native FT gate routes to its bound instance entry rather than another same-seed anchor");
        EclipseMod.leave(player);
        helper.assertTrue(player.serverLevel() == overworld && player.position().distanceToSqr(origin) < .001,
                "Actual generator exit returns to original FT gate location");
        helper.assertTrue(!DungeonGateGenerators.automaticAllowed(player, anchor), "Returned player cannot immediately retrigger the source gate");
        player.setPos(anchor.getX() + 6, anchor.getY(), anchor.getZ());
        DungeonGateGenerators.playerTick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(
                net.minecraftforge.event.TickEvent.Phase.END, player));
        helper.assertTrue(DungeonGateGenerators.automaticAllowed(player, anchor), "Walking away rearms automatic gate entry");
        System.out.println("FT_GATE_ECLIPSE_ACTUAL_PASS: completed instance;seed reuse;native FT gate travel;return;automatic reentry guard");
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void nativeRegistrationAndGateNbt(GameTestHelper helper) {
        for (String id : List.of("ftdungeonmobs", "ftdungeonprops", "fteclipsegen")) {
            helper.assertTrue(ModList.get().getMods().stream().filter(info -> info.getModId().equals(id)).count() == 1,
                "Exactly one native loaded mod entry for " + id);
        }
        helper.assertTrue(GateTestProviders.registrationEvents == 1, "External registration event fired once at server startup after the Forge runtime bus starts");
        var builtins = DungeonGenerators.all().stream().filter(provider -> !provider.id().getNamespace().equals("ftgateintegration")).toList();
        helper.assertTrue(builtins.size() == 2 && DungeonGenerators.find(DungeonGenerators.CHAOS).isPresent()
            && DungeonGenerators.find(DungeonGenerators.LEGACY).isEmpty()
            && DungeonGenerators.find(ECLIPSE).isPresent(), "Registry contains exactly the two production defaults");
        helper.assertTrue(DungeonGenerators.find(GateTestProviders.ID).orElseThrow() == GateTestProviders.PROVIDER,
            "Public Forge registration event accepts an external provider");
        helper.assertTrue(DungeonGenerators.all().stream().map(DungeonGenerator::id).distinct().count() == DungeonGenerators.all().size(), "Provider IDs are unique");
        rejected(helper, () -> new DungeonGeneratorRegistrationEvent().register(GateTestProviders.PROVIDER), "Registration is sealed outside initialization event");
        BlockPos anchor = new BlockPos(500000, 100, 500000);
        DungeonGateBlockEntity original = gate(helper, helper.getLevel(), anchor, 2);
        original.load(new CompoundTag());
        helper.assertTrue(original.generatorId().equals(DungeonGenerators.CHAOS) && !original.hasGeneratorSeed()
            && original.generatorInstance().isEmpty() && original.generatorTier() == 0, "Old gate NBT without generator tags defaults to chaos and remains unbound");
        original.configureGenerator(DungeonGenerators.LEGACY, 12);
        original = copy(original);
        helper.assertTrue(original.generatorId().equals(DungeonGenerators.CHAOS), "Unbound classic gate migrates to chaos");
        original.configureGenerator(ECLIPSE, Long.MIN_VALUE);
        DungeonGateBlockEntity selected = copy(original);
        helper.assertTrue(selected.generatorId().equals(ECLIPSE) && selected.hasGeneratorSeed() && selected.generatorSeed() == Long.MIN_VALUE,
            "Unbound selected type, explicit seed flag and signed seed survive real BE NBT round trip");
        original.bindGenerator("persistent-native-instance", 4);
        DungeonGateBlockEntity restored = copy(original);
        helper.assertTrue(restored.generatorInstance().equals("persistent-native-instance") && restored.generatorTier() == 4
            && restored.generatorId().equals(ECLIPSE) && restored.generatorSeed() == Long.MIN_VALUE, "Bound instance, tier, generator and seed survive NBT");
        rejected(helper, () -> restored.configureGenerator(DungeonGenerators.LEGACY, 7), "Binding locks type and seed");
        rejected(helper, () -> restored.bindGenerator("different-instance", 4), "Binding cannot redirect instance");
        rejected(helper, () -> restored.bindGenerator("persistent-native-instance", 3), "Binding cannot redirect tier");
        restored.bindGenerator("persistent-native-instance", 4);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 18000)
    public static void chaosBuildAndTravel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = new BlockPos(505000, 100, 500000);
        DungeonGateBlockEntity gate = gate(helper, level, anchor, 2);
        gate.configureGenerator(DungeonGenerators.CHAOS, SEED);
        TestPlayer participant = player(level, anchor, true);
        DungeonGateGenerators.prepare(participant, gate);
        helper.assertTrue(!gate.generatorInstance().isEmpty(), "Chaos reserves a persistent gate instance");
        DungeonGateGenerators.prepare(participant, gate);
        helper.assertTrue(!DungeonGateGenerators.status(level, gate).ready(), "Chaos cannot admit before the build finishes");
        awaitChaos(helper, gate, participant, anchor);
    }

    private static void awaitChaos(GameTestHelper helper, DungeonGateBlockEntity gate, TestPlayer participant, BlockPos anchor) {
        var current = DungeonGateGenerators.status(helper.getLevel(), gate);
        helper.assertTrue(current.phase() != DungeonGenerator.Phase.FAILED
                && current.phase() != DungeonGenerator.Phase.UNAVAILABLE, "Chaos build remains valid: " + current.detail().getString());
        if (!current.ready()) {
            helper.runAfterDelay(20, () -> awaitChaos(helper, gate, participant, anchor));
            return;
        }
        var provider = DungeonGenerators.find(DungeonGenerators.CHAOS).orElseThrow();
        Vec3Snapshot expected = new Vec3Snapshot(participant.getX(), participant.getY(), participant.getZ());
        opened(helper.getLevel(), anchor);
        DungeonGateGenerators.travel(participant, anchor, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false);
        helper.assertTrue(provider.ownsDimension(participant.serverLevel()), "The real FT gate enters the generated chaos room network");
        helper.assertTrue(participant.gameMode.getGameModeForPlayer() == GameType.ADVENTURE, "Chaos uses adventure mode");
        rejected(helper, () -> provider.leave(participant), "Final boss locks early return");
        participant.server.getCommands().performPrefixedCommand(participant.server.createCommandSourceStack(),
                "scoreboard players set #run ftm_state 4");
        provider.leave(participant);
        helper.assertTrue(participant.serverLevel() == helper.getLevel()
                && participant.position().distanceToSqr(expected.x(), expected.y(), expected.z()) < 0.01,
                "Chaos restores this player's own entrance position");
        helper.assertTrue(participant.gameMode.getGameModeForPlayer() == GameType.SURVIVAL, "Chaos restores prior game mode");
        remove(participant);
        System.out.println("FT_CHAOS_NATIVE_PASS: original R5 build;ready-only gate;boss lock;personal return;mode restore");
        helper.succeed();
    }

    private record Vec3Snapshot(double x, double y, double z) {}

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void completeGatePreparationGuardsAndTravel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = new BlockPos(501000, 100, 500000);
        DungeonGateBlockEntity gate = gate(helper, level, anchor, 0);
        TestPlayer player = player(level, anchor, true);
        var provider = GateTestProviders.PROVIDER;
        int reserves = provider.reserves;
        int entries = provider.entries;
        try {
            helper.assertTrue(DungeonGateGenerators.canConfigure(player), "OP2 can configure");
            DungeonGeneratorMenu selection = new DungeonGeneratorMenu(31, player.getInventory(), level, anchor);
            int eclipseSlot = -1;
            for (int index = 0; index < DungeonGenerators.all().size(); index++) {
                if (DungeonGenerators.all().get(index).id().equals(ECLIPSE)) eclipseSlot = index;
            }
            helper.assertTrue(eclipseSlot >= 0 && eclipseSlot < 21, "Built-in Eclipse option is present on first menu page");
            selection.clicked(eclipseSlot, 0, ClickType.PICKUP, player);
            helper.assertTrue(selection.getCarried().isEmpty(), "Selecting Eclipse cannot take its menu icon");
            helper.assertTrue(gate.generatorId().equals(ECLIPSE) && gate.hasGeneratorSeed(), "Actual gate can select built-in Eclipse and freeze its seed");
            helper.assertTrue(DungeonGateGenerators.status(level, gate).phase() == DungeonGenerator.Phase.UNPLANNED, "Unbound Eclipse selection reports unplanned without reserving Engine volume");
            DungeonGateGenerators.choose(player, gate, GateTestProviders.ID);
            gate.configureGenerator(GateTestProviders.ID, SEED);
            rejected(helper, () -> DungeonGateGenerators.prepare(player, gate), "Unlit actual brazier refuses generation");
            level.setBlock(anchor.offset(3, 0, 0), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            rejected(helper, () -> DungeonGateGenerators.prepare(player, gate), "Missing actual brazier refuses generation");
            brazier(level, anchor, 3);
            player.operator = false;
            helper.assertTrue(!DungeonGateGenerators.canConfigure(player), "Normal survival player cannot configure");
            rejected(helper, () -> DungeonGateGenerators.prepare(player, gate), "Missing permission refuses actual prepare");
            player.setGameMode(GameType.CREATIVE);
            helper.assertTrue(DungeonGateGenerators.canConfigure(player), "Creative player can configure without OP");
            player.setGameMode(GameType.SURVIVAL);
            player.operator = true;
            player.setPos(anchor.getX() + 10.5, anchor.getY(), anchor.getZ() + 0.5);
            rejected(helper, () -> DungeonGateGenerators.prepare(player, gate), "Beyond eight blocks refuses generation");
            near(player, anchor);
            BlockPos missing = DungeonGateBlock.partPosition(anchor, Direction.NORTH, 11);
            BlockState part = level.getBlockState(missing);
            level.setBlock(missing, part.setValue(DungeonGateBlock.PART, 10), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            rejected(helper, () -> DungeonGateGenerators.prepare(player, gate), "Malformed actual gate part refuses generation");
            level.setBlock(missing, part, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            helper.assertTrue(provider.reserves == reserves && gate.generatorInstance().isEmpty(), "All denied operations reserve nothing and bind nothing");
            DungeonGateGenerators.prepare(player, gate);
            String instance = gate.generatorInstance();
            helper.assertTrue(!instance.isEmpty() && gate.generatorTier() == 3 && gate.generatorSeed() == SEED, "Prepare binds frozen seed and live brazier tier");
            helper.assertTrue(provider.reserves == reserves + 1 && provider.requests.get(instance).source() == level
                && provider.requests.get(instance).gate().equals(anchor), "External provider receives exact source gate request");
            DungeonGateGenerators.prepare(player, gate);
            helper.assertTrue(provider.reserves == reserves + 1 && instance.equals(gate.generatorInstance()) && gate.generatorSeed() == SEED,
                "Repeated prepare reuses frozen binding without reserving or rerolling");
            rejected(helper, () -> DungeonGateGenerators.choose(player, gate, ECLIPSE), "Bound gate rejects type changes through public selection API");
            opened(level, anchor);
            helper.assertTrue(DungeonGateGenerators.travel(player, anchor, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false), "Selected provider handles unready travel instead of legacy fallback");
            helper.assertTrue(provider.entries == entries && player.serverLevel() == level, "PLANNED instance does not admit a player");
            provider.phases.put(instance, DungeonGenerator.Phase.COMPLETE);
            player.operator = false;
            helper.assertTrue(DungeonGateGenerators.travel(player, anchor, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false), "Ready selected provider handles ordinary player travel");
            helper.assertTrue(provider.entries == entries + 1, "Only COMPLETE status admits normal survival player");
            ServerLevel generated = level.getServer().getLevel(EclipseMod.DIMENSION);
            helper.assertTrue(generated != null, "Merged Eclipse dimension is available on native server");
            DungeonGateBlockEntity nested = gate(helper, generated, new BlockPos(1000000, 100, 1000000), 2);
            nested.configureGenerator(GateTestProviders.ID, SEED);
            TestPlayer inside = player(generated, nested.getBlockPos(), true);
            try {
                rejected(helper, () -> DungeonGateGenerators.prepare(inside, nested), "Entrance inside a generated dimension refuses nested generation");
            } finally {
                remove(inside);
            }
            helper.assertTrue(provider.reserves == reserves + 1, "Generated-dimension denial cannot allocate another instance");
            System.out.println("FT_GATE_NATIVE_LIFECYCLE_PASS: real twelve-part gate;brazier;Eclipse selection;external provider;frozen binding;guards;ready-only travel");
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void menuCannotExtractAndUnknownProviderFailsClosed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = new BlockPos(502000, 100, 500000);
        DungeonGateBlockEntity gate = gate(helper, level, anchor, 2);
        gate.configureGenerator(UNKNOWN, SEED);
        gate.bindGenerator("missing-provider-instance", 2);
        TestPlayer player = player(level, anchor, false);
        try {
            DungeonGateGenerators.open(player, anchor);
            helper.assertTrue(player.containerMenu instanceof DungeonGeneratorMenu, "Public gate open API opens the actual server menu for an ordinary player");
            DungeonGeneratorMenu menu = (DungeonGeneratorMenu) player.containerMenu;
            helper.assertTrue(menu.getType() == MenuType.GENERIC_9x3 && menu.slots.size() == 63 && menu.stillValid(player), "Native generic9x3 menu is usable at complete gate");
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
            List<ItemStack> inventory = new ArrayList<>();
            for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
                inventory.add(player.getInventory().getItem(index).copy());
            }
            List<ItemStack> icons = new ArrayList<>();
            for (int slot = 0; slot < 27; slot++) {
                icons.add(menu.getSlot(slot).getItem().copy());
            }
            for (int slot = 0; slot < 27; slot++) {
                for (ClickType type : List.of(ClickType.PICKUP, ClickType.QUICK_MOVE, ClickType.SWAP,
                        ClickType.CLONE, ClickType.THROW, ClickType.PICKUP_ALL)) {
                    menu.clicked(slot, 0, type, player);
                    menu.clicked(slot, 1, type, player);
                    helper.assertTrue(menu.getCarried().isEmpty(), "No cursor item extracted by " + type + " at " + slot);
                    helper.assertTrue(menu.quickMoveStack(player, slot).isEmpty(), "Shift move returns EMPTY");
                }
                helper.assertTrue(!menu.canTakeItemForPickAll(menu.getSlot(slot).getItem(), menu.getSlot(slot)), "Double-click cannot collect menu icon");
                for (int hotbar = 0; hotbar < 9; hotbar++) {
                    menu.clicked(slot, hotbar, ClickType.SWAP, player);
                }
            }
            player.setGameMode(GameType.CREATIVE);
            menu.clicked(0, 2, ClickType.CLONE, player);
            helper.assertTrue(menu.getCarried().isEmpty(), "Creative middle-click cannot clone menu icon");
            player.setGameMode(GameType.SURVIVAL);
            menu.setCarried(new ItemStack(Items.EMERALD, 5));
            menu.clicked(-999, 0, ClickType.QUICK_CRAFT, player);
            menu.clicked(0, 1, ClickType.QUICK_CRAFT, player);
            menu.clicked(-999, 2, ClickType.QUICK_CRAFT, player);
            helper.assertTrue(menu.getCarried().is(Items.EMERALD) && menu.getCarried().getCount() == 5, "Drag cannot deposit or duplicate carried items");
            menu.setCarried(ItemStack.EMPTY);
            for (int slot = 27; slot < 63; slot++) {
                menu.clicked(slot, 0, ClickType.PICKUP, player);
                menu.clicked(slot, 0, ClickType.QUICK_MOVE, player);
                menu.clicked(slot, 0, ClickType.SWAP, player);
            }
            for (int index = 0; index < inventory.size(); index++) {
                helper.assertTrue(ItemStack.matches(inventory.get(index), player.getInventory().getItem(index)), "Menu never mutates real inventory slot " + index);
            }
            for (int slot = 0; slot < 27; slot++) {
                helper.assertTrue(ItemStack.matches(icons.get(slot), menu.getSlot(slot).getItem()), "Menu icons cannot be removed or replaced at " + slot);
            }
            helper.assertTrue(gate.generatorId().equals(UNKNOWN) && gate.generatorInstance().equals("missing-provider-instance"), "Unprivileged menu clicks cannot reconfigure bound gate");
            DungeonGateBlockEntity unknown = copy(gate);
            helper.assertTrue(unknown.generatorId().equals(UNKNOWN) && unknown.generatorInstance().equals("missing-provider-instance"), "Unknown provider binding survives load rather than reverting to legacy");
            helper.assertTrue(DungeonGateGenerators.status(level, gate).phase() == DungeonGenerator.Phase.UNAVAILABLE, "Unknown generator reports unavailable");
            opened(level, anchor);
            var before = player.position();
            int entries = GateTestProviders.PROVIDER.entries;
            helper.assertTrue(DungeonGateGenerators.travel(player, anchor, FTDungeonMobs.DUNGEON_ENTRY_GATE.get(), false), "Unknown provider consumes travel without legacy fallback");
            helper.assertTrue(player.serverLevel() == level && player.position().equals(before) && GateTestProviders.PROVIDER.entries == entries,
                "Missing provider never teleports or enters another provider");
            player.operator = true;
            rejected(helper, () -> DungeonGateGenerators.prepare(player, gate), "Unknown provider cannot prepare or redirect saved binding");
            helper.assertTrue(gate.generatorId().equals(UNKNOWN) && gate.generatorInstance().equals("missing-provider-instance"), "Failed prepare preserves unknown binding for recovery");
            System.out.println("FT_GATE_NATIVE_MENU_PASS: generic9x3;pickup;shift;number keys;drag;drop;doubleclick;unknown-provider fail-closed");
        } finally {
            remove(player);
        }
        helper.succeed();
    }
}
