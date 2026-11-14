package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    public PositiveWeapon436() { NeoForge.EVENT_BUS.addListener(PositiveWeapon436::started); }

    private static Object read(Object instance, String name) throws Exception {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(instance); }
            catch (NoSuchFieldException missing) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object readStatic(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>();
        List<ServerPlayer> scratches = new ArrayList<>();
        report.put("cases", cases);
        try {
            var server = event.getServer();
            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
            var generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
            Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");
            var open = storeType.getDeclaredMethod("open", net.minecraft.server.MinecraftServer.class); open.setAccessible(true);
            Object store = open.invoke(null, server);
            var readDocument = storeType.getDeclaredMethod("readRepairDocument", UUID.class, boolean.class); readDocument.setAccessible(true);
            var recordPath = storeType.getDeclaredMethod("recordPath", UUID.class, UUID.class); recordPath.setAccessible(true);
            var readRecord = storeType.getDeclaredMethod("read", Path.class, UUID.class, UUID.class); readRecord.setAccessible(true);
            List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");
            for (int sample = 0; sample < ids.size(); sample++) {
                String id = ids.get(sample);
                UUID owner = new UUID(620, sample + 1);
                GameProfile profile = new GameProfile(owner, "ft_six_" + sample);
                ServerPlayer original = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(original);
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.parse(id)).orElseThrow();
                ItemStack weapon = new ItemStack(item);
                Map<String, Object> row = new LinkedHashMap<>(); row.put("id", id); row.put("owner", owner.toString()); row.put("itemClass", item.getClass().getName()); cases.add(row);
                CompoundTag data = new CompoundTag();
                CompoundTag evolution = new CompoundTag();
                evolution.putString("Profile", "butcher"); evolution.putString("EvolutionSkill", "blood_feast");
                evolution.putInt("Level", 1); evolution.putInt("Experience", 0); evolution.putInt("ProgressionVersion", 2);
                data.put("FTBossFoodEvolution", evolution);
                weapon.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                row.put("originalFoundationApplicable", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(weapon));
                if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(weapon)
                    && !net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.initializeForServerEdit(weapon))
                    throw new AssertionError("Original native foundation initialization failed for " + id);
                original.getInventory().setItem(0, weapon);
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(original);
                net.farmingtales.ftboss.item.NativeFoodWeaponWear.manage(original, weapon);
                net.farmingtales.ftboss.item.FoodWeaponLifecycle.ensureTracked(weapon);
                weapon.setDamageValue(10);
                storage.save(original);
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(original);
                ServerPlayer repairedPlayer = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(repairedPlayer);
                if (storage.load(repairedPlayer).isEmpty()) throw new AssertionError("Original pre-repair player load did not run for " + id);
                ItemStack actual = repairedPlayer.getInventory().getItem(0);
                if (!actual.is(item) || !net.farmingtales.ftboss.item.NativeFoodWeaponWear.eligible(actual) || actual.getDamageValue() != 10)
                    throw new AssertionError("Original native wear fixture is not eligible/coherent for " + id);
                runNativeRepair(event, repairedPlayer, actual, row);
                Object predecessorLease = leaseMethod.invoke(null, actual);
                Object predecessorGeneration = generationMethod.invoke(null, actual);
                if (predecessorLease == null || predecessorGeneration == null) throw new AssertionError("Actual paid CLAIM did not publish physical authority for " + id);
                Object predecessorEpoch = read(predecessorLease, "incarnation");
                if (((Number)read(predecessorLease, "generation")).longValue() != 0L) throw new AssertionError("Actual original paid claim did not start at generation zero for " + id);
                CompoundTag expected = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                storage.save(repairedPlayer);
                UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + owner).getBytes(StandardCharsets.UTF_8));
                CompoundTag committed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                if (!committed.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")) throw new AssertionError("Actual paid save did not commit for " + id);
                ServerPlayer successorPlayer = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(successorPlayer);
                if (storage.load(successorPlayer).isEmpty()) throw new AssertionError("Actual original paid reload did not run for " + id);
                ItemStack successor = successorPlayer.getInventory().getItem(0);
                Object successorLease = leaseMethod.invoke(null, successor);
                Object successorGeneration = generationMethod.invoke(null, successor);
                if (successorLease == null || successorGeneration == null || successorLease == predecessorLease || successorGeneration == predecessorGeneration
                    || !expected.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(successor)) || !Boolean.TRUE.equals(read(predecessorGeneration, "retired")))
                    throw new AssertionError("Actual same-process paid reload failed to advance/revoke predecessor for " + id);
                if (!predecessorEpoch.equals(read(successorLease, "incarnation")) || ((Number)read(successorLease, "generation")).longValue() != 1L)
                    throw new AssertionError("Actual paid successor changed epoch or did not advance exactly once for " + id);
                UUID weaponId = net.farmingtales.ftboss.item.FoodWeaponLifecycle.weaponId(successor);
                Path lineagePath = (Path)recordPath.invoke(store, owner, weaponId);
                Object lineage = readRecord.invoke(store, lineagePath, owner, weaponId);
                if (!Boolean.TRUE.equals(read(lineage, "active")) || ((Number)read(lineage, "generation")).longValue() != 1L)
                    throw new AssertionError("Original lineage did not reach generation one for " + id);
                byte[] lineageBytes = Files.readAllBytes(lineagePath);
                CompoundTag consumed = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                ServerPlayer replay = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                scratches.add(replay);
                if (storage.load(replay).isEmpty()) throw new AssertionError("Original consumed replay did not run for " + id);
                if (leaseMethod.invoke(null, replay.getInventory().getItem(0)) != null || generationMethod.invoke(null, replay.getInventory().getItem(0)) != null
                    || leaseMethod.invoke(null, successor) != successorLease || generationMethod.invoke(null, successor) != successorGeneration
                    || !Arrays.equals(lineageBytes, Files.readAllBytes(lineagePath)) || !consumed.equals(readDocument.invoke(store, documentOwner, false)))
                    throw new AssertionError("Consumed save replay reissued or mutated genuine successor for " + id);
                storage.save(successorPlayer);
                CompoundTag saved = NbtIo.readCompressed(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(owner + ".dat"), NbtAccounter.unlimitedHeap());
                CompoundTag ticket = saved.getList("Inventory", 10).getCompound(0).getCompound("FTBossRepairContinuation");
                CompoundTag finalDocument = (CompoundTag)readDocument.invoke(store, documentOwner, false);
                if (ticket.getLong("LeaseGeneration") != 1L || !finalDocument.getString("Status").equals("COMMITTED_ORIGINAL_SAVE")
                    || !ticket.equals(finalDocument.getCompound("Continuations").getCompound(weaponId.toString()).getCompound("Ticket")))
                    throw new AssertionError("Original successor save lacks exact committed generation-one ticket for " + id);
                row.put("actualPaidClaimAndThreeSupplyConsumptions", true); row.put("actualOriginalSaveCommitted", true);
                row.put("actualOriginalLoadRestoredNonNullGenuineSuccessor", true); row.put("sameEpochGenerationZeroToOne", true);
                row.put("oldPhysicalGenerationRetired", true); row.put("consumedReplayDeniedAndFirstSuccessorUnchanged", true);
                row.put("actualSuccessorOriginalSaveCommitted", true); row.put("successorWholeItemImage", expected.toString());
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(repairedPlayer);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).remove(owner, repairedPlayer);
            }
            report.put("failures", 0); report.put("sixNativeDamageableFamilyRepresentativesPaidRepairAndSameProcessReload", true);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            try {
                var players = (List<ServerPlayer>)read(event.getServer().getPlayerList(), "players");
                var byId = (Map<UUID, ServerPlayer>)read(event.getServer().getPlayerList(), "playersByUUID");
                for (ServerPlayer scratch : scratches) { players.remove(scratch); byId.remove(scratch.getUUID(), scratch); }
                report.put("scratchPlayersRemoved", scratches.stream().noneMatch(players::contains));
            } catch (Exception failure) { report.put("failures", 1); report.put("cleanupFailure", failure.toString()); }
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false);
        report.put("fullSixFamilyRuntimeMatrixAccepted", false); report.put("fullSignedCombatSessionRunByThisFixture", false);
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }

    private static void runNativeRepair(ServerStartedEvent event, ServerPlayer player, ItemStack weapon, Map<String, Object> row) throws Exception {
        var server = event.getServer();
        player.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server,
            new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), player,
            net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
        ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(player);
        ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(player.getUUID(), player);
        row.put("scratchRegistrationNotAuthenticated", true);
        var statuePos = new net.minecraft.core.BlockPos(1, -59, 0);
        net.farmingtales.ftboss.block.CandyStatueBlock statue = null;
        for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            if (block instanceof net.farmingtales.ftboss.block.CandyStatueBlock candidate && candidate.ownTint() == 2) { statue = candidate; break; }
        }
        if (statue == null) throw new AssertionError("Native repair statue missing");
        var half = net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;
        server.overworld().setBlock(statuePos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        server.overworld().setBlock(statuePos, statue.defaultBlockState().setValue(half, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER), 2);
        server.overworld().setBlock(statuePos.above(), statue.defaultBlockState().setValue(half, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 2);
        var authority = (net.farmingtales.ftboss.transaction.FourWeaponTransactions)readStatic(net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime.class, "authority");
        if (authority == null) throw new AssertionError("Original runtime transaction authority absent");
        var boundary = new net.farmingtales.ftboss.transaction.ExistingTransactionBoundary(java.util.Set.of(), new net.farmingtales.ftboss.transaction.FoundationApprovedRules());
        row.put("statueLower", server.overworld().getBlockState(statuePos).toString());
        row.put("statueUpper", server.overworld().getBlockState(statuePos.above()).toString());
        if (boundary.statueIdentity(player, statuePos) == null) throw new AssertionError("Scratch statue pair invalid: " + row.get("statueLower") + "/" + row.get("statueUpper"));
        var binding = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding(player.getUUID(), net.farmingtales.ftboss.item.FoodWeaponLifecycle.weaponId(weapon),
            net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.registryId(weapon), 0, 0, player.level().dimension().location().toString(), statuePos.asLong(), boundary.statueIdentity(player, statuePos));
        List<Object> transactions = new ArrayList<>();
        row.put("repairTransactions", transactions);
        row.put("nativeWearEligibleBeforeStart", net.farmingtales.ftboss.item.NativeFoodWeaponWear.eligible(weapon));
        row.put("repairDamageBeforeStart", weapon.getDamageValue());
        row.put("repairProfileBeforeStart", net.farmingtales.ftboss.item.FoodWeaponLifecycle.repairProfile(weapon));
        row.put("repairViewBeforeStart", net.farmingtales.ftboss.item.FoodWeaponRepair.view(weapon, 2).toString());
        row.put("repairImageBeforeStart", net.farmingtales.ftboss.item.NativeFoundationItemData.image(weapon).toString());
        row.put("repairStackClass", weapon.getClass().getName());
        row.put("repairItemClass", weapon.getItem().getClass().getName());
        row.put("repairCapabilityFields", (Object)weapon instanceof net.farmingtales.ftboss.item.OwnerRepairCapabilityFields);
        List<Object> nativeCapabilities = new ArrayList<>();
        row.put("nativeCapabilities", nativeCapabilities);
        for (var capability : net.neoforged.neoforge.capabilities.ItemCapability.getAll()) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("capability", capability.toString());
            detail.put("physicalFields", (Object)capability instanceof net.farmingtales.ftboss.item.NativeItemCapabilityFields);
            Object providers = read(capability, "providers");
            Object values = providers instanceof Map<?, ?> providerMap ? providerMap.get(weapon.getItem()) : null;
            detail.put("providers", values instanceof List<?> list ? list.stream().map(value -> value.getClass().getName()).toList() : String.valueOf(values));
            nativeCapabilities.add(detail);
        }
        for (int action = 0; action < 5; action++) {
            var operation = action == 0 ? net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.START_REPAIR
                : action == 4 ? net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.CLAIM_REPAIR
                : net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.SUBMIT_REPAIR;
            int index = action - 1;
            int expectedPayment = 0;
            int supplySlot = 2;
            if (action > 0 && action < 4) {
                expectedPayment = net.farmingtales.ftboss.util.FoodWeaponLifecycleRules.repairRequired(net.farmingtales.ftboss.item.FoodWeaponEvolution.evolutionLevel(weapon), index);
                net.minecraft.world.item.Item supply = index == 1 ? Items.IRON_INGOT : Items.LAPIS_LAZULI;
                if (index == 0) {
                    supply = null;
                    for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) if (net.farmingtales.ftboss.item.FoodWeaponEvolution.matchesStatueFood(new ItemStack(item), 2)) { supply = item; break; }
                }
                if (supply == null) throw new AssertionError("Native task food supply missing");
                player.getInventory().setItem(supplySlot, new ItemStack(supply, expectedPayment));
            }
            ((net.minecraft.world.level.storage.ServerLevelData)server.overworld().getLevelData()).setGameTime(server.overworld().getGameTime() + 5L);
            var opened = authority.open(player.getUUID(), new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Open(UUID.randomUUID(), binding));
            if (opened.code() != net.farmingtales.ftboss.transaction.FourWeaponTransactions.Code.OPENED || opened.snapshot() == null || !opened.snapshot().allowed().contains(operation)) throw new AssertionError("Original transaction open denied: " + opened);
            var snapshot = opened.snapshot();
            var request = new net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request(UUID.randomUUID(), snapshot.session(), snapshot.credential(), binding,
                snapshot.version(), operation, -1, action > 0 && action < 4 ? index : 0, "");
            var reply = authority.submit(player.getUUID(), request);
            Map<String, Object> transaction = new LinkedHashMap<>();
            transaction.put("operation", operation.name()); transaction.put("accepted", reply.accepted()); transaction.put("code", reply.code().name());
            transaction.put("expectedPayment", expectedPayment); transaction.put("remainingSupply", player.getInventory().getItem(supplySlot).getCount());
            transactions.add(transaction);
            if (!reply.accepted()) throw new AssertionError("Original native repair transaction failed: " + transaction);
            if (action > 0 && action < 4 && !player.getInventory().getItem(supplySlot).isEmpty()) throw new AssertionError("Actual native payment did not consume supplies");
            authority.close(player.getUUID(), snapshot.session());
        }
        Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
        java.lang.reflect.Method leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
        java.lang.reflect.Method generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
        row.put("nonNullLease", leaseMethod.invoke(null, weapon) != null);
        row.put("repairGenerationPresent", generationMethod.invoke(null, weapon) != null);
        row.put("damageAfterRepair", weapon.getDamageValue());
        if (!Boolean.TRUE.equals(row.get("nonNullLease")) || !Boolean.TRUE.equals(row.get("repairGenerationPresent")) || weapon.getDamageValue() != 0) throw new AssertionError("Native claim did not publish genuine lease/generation");
    }

}
