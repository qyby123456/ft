package ftboss.storage308;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_storage308")
public final class PositiveWeapon436 {
    private static final List<Object> actions = new ArrayList<>();
    private static final List<Map<String, Object>> damageEvents = new ArrayList<>();
    private static ServerStartedEvent pending;
    private static net.minecraft.world.damagesource.DamageSource capturedSource;
    private static CompoundTag capturedSigned;

    public PositiveWeapon436() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> pending = event);
        NeoForge.EVENT_BUS.addListener(PositiveWeapon436::tick);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, PositiveWeapon436::beforeMitigation);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, PositiveWeapon436::beforeHealth);
    }

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

    private static void observe(String phase, NativeWeaponDamageEvent event) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("phase", phase); row.put("amount", event.getAmount());
        try {
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                CompoundTag signed = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(event.getSource(), player);
                capturedSource = event.getSource(); capturedSigned = signed.copy();
                row.put("resolvedSnapshotPresent", !signed.isEmpty());
                row.put("resolvedSnapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(signed, player));
                row.put("repairEpochPresent", signed.hasUUID("OwnerRepairAuthorityEpoch"));
                row.put("serverSignaturePresent", signed.getString("ServerSignature").length() == 64);
            }
            Object waiters = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "GUARD_WAITERS");
            java.util.Deque<?> queued = (java.util.Deque<?>)((ThreadLocal<?>)waiters).get();
            row.put("guardWaiters", queued.size());
            for (Object action : queued) if (!actions.contains(action)) actions.add(action);
        } catch (Throwable failure) { row.put("observationFailure", failure.toString()); }
        damageEvents.add(row);
    }

    private static void beforeMitigation(NativeWeaponDamageEvent.BeforeMitigation event) { observe("beforeMitigation", event); }
    private static void beforeHealth(NativeWeaponDamageEvent.BeforeHealth event) { observe("beforeHealth", event); }

    private static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if (pending == null) return;
        ServerStartedEvent started = pending; pending = null; run(started);
    }

    private static void run(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>(); report.put("cases", cases);
        List<ServerPlayer> scratches = new ArrayList<>();
        List<io.netty.channel.embedded.EmbeddedChannel> channels = new ArrayList<>();
        try {
            var server = event.getServer();
            Class<?> boundedPolicy = Class.forName("net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority");
            var boundedMethod = boundedPolicy.getDeclaredMethod("nativeMaterialRangedPolicy", net.minecraft.world.item.ItemStack.class); boundedMethod.setAccessible(true);
            java.util.Set<String> actualBounded = new java.util.TreeSet<>();
            for (var registered : net.minecraft.core.registries.BuiltInRegistries.ITEM)
                if (Boolean.TRUE.equals(boundedMethod.invoke(null, new net.minecraft.world.item.ItemStack(registered))))
                    actualBounded.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(registered).toString());
            java.util.Set<String> expectedBounded = new java.util.TreeSet<>();
            for (String kind : java.util.List.of("bow", "crossbow")) for (String tier : java.util.List.of("wooden", "stone", "iron", "golden", "diamond", "netherite"))
                expectedBounded.add("ftboss:" + tier + "_" + kind);
            report.put("actualRegisteredMaterialPolicyAcceptedIds", actualBounded);
            if (!actualBounded.equals(expectedBounded)) throw new AssertionError("Material policy registry boundary drift: " + actualBounded);

            PlayerDataStorage storage = (PlayerDataStorage)read(server.getPlayerList(), "playerIo");
            Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
            var leaseMethod = guard.getDeclaredMethod("lease", Object.class); leaseMethod.setAccessible(true);
            var generationMethod = guard.getDeclaredMethod("generation", Object.class); generationMethod.setAccessible(true);
            List<net.minecraft.world.item.ItemStack> liveOriginals = new ArrayList<>();
            var nodeMethod = guard.getDeclaredMethod("node", Object.class, boolean.class); nodeMethod.setAccessible(true);
            var recordMethod = guard.getDeclaredMethod("record", Object.class, boolean.class); recordMethod.setAccessible(true);
            for (int sample : new int[]{6, 7, 8, 9, 10, 11}) {
                UUID owner = new UUID(651, sample + 1);
                ServerPlayer player = new ServerPlayer(server, server.overworld(), new GameProfile(owner, "ft_material_" + sample), ClientInformation.createDefault());
                scratches.add(player);
                if (storage.load(player).isEmpty()) throw new AssertionError("Original paid native melee weapon reload did not run");
                var actual = player.getInventory().getItem(0);
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(actual.getItem()).toString();
                Map<String, Object> row = new LinkedHashMap<>(); row.put("id", id); cases.add(row);
                Object actualLease = leaseMethod.invoke(null, actual);
                Object generation = generationMethod.invoke(null, actual);
                if (actualLease == null || generation == null || ((Number)read(actualLease, "generation")).longValue() != 2L)
                    throw new AssertionError("Melee test requires genuine paid generation-two original reload, not empty authority");
                row.put("genuineNonNullLeaseAndGenerationBeforeAttack", true);
                var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
                var channel = new io.netty.channel.embedded.EmbeddedChannel(connection); channels.add(channel);
                player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player,
                    net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(owner, player);
                player.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
                player.setHealth(10.0F);
                player.getInventory().setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW, 16));
                var target = new net.minecraft.world.entity.monster.Zombie(server.overworld());
                target.setNoAi(true); target.moveTo(0.0, -59.0, 1.5, 0.0F, 0.0F);
                if (!server.overworld().addFreshEntity(target)) throw new AssertionError("Actual melee target was not added");
                actions.clear(); damageEvents.clear();
                float healthBefore = target.getHealth();
                int wearBefore = actual.getDamageValue();
                var useResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualOriginalUseResult", useResult.getResult().toString());
                row.put("actualUseStarted", player.isUsingItem());
                row.put("generationRetiredAfterUse", read(generation, "retired"));
                if (!useResult.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Crossbow ItemStack.use did not start with live authority");
                Object inputBeforeEarlyRelease = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                CompoundTag beforeEarlyRelease = (CompoundTag)actual.save(player.registryAccess());
                int ammoBeforeEarlyRelease = player.getInventory().getItem(1).getCount();
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 1);
                boolean earlyReleaseUnchanged = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput() == inputBeforeEarlyRelease
                    && beforeEarlyRelease.equals(actual.save(player.registryAccess())) && !Boolean.TRUE.equals(read(generation, "retired"))
                    && player.getInventory().getItem(1).getCount() == ammoBeforeEarlyRelease;
                row.put("originalEarlyReleasePreservedInputImageAmmoAndAuthority", earlyReleaseUnchanged);
                if (!earlyReleaseUnchanged) throw new AssertionError("Original early release modified or retired unchanged genuine authority");
                player.stopUsingItem();
                var restarted = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                if (!restarted.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid crossbow could not restart charge after original early release");
                int ammoBefore = player.getInventory().getItem(1).getCount();
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 40);
                var charged = actual.get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES);
                row.put("ammoBefore", ammoBefore); row.put("ammoAfterCharge", player.getInventory().getItem(1).getCount());
                row.put("chargedProjectilesPresent", charged != null && !charged.isEmpty());
                row.put("generationRetiredAfterCharge", read(generation, "retired"));
                if (charged == null || charged.isEmpty() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Crossbow charge lost projectiles or genuine authority");
                player.stopUsingItem();
                var fireResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualOriginalFireUseResult", fireResult.getResult().toString());
                row.put("generationRetiredAfterFire", read(generation, "retired"));
                List<Map<String, Object>> flights = new ArrayList<>(); row.put("actualProjectileFlights", flights);
                List<net.minecraft.world.entity.projectile.AbstractArrow> arrows = new ArrayList<>();
                for (var entity : server.overworld().getAllEntities()) {
                    if (entity instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow && arrow.getOwner() == player) arrows.add(arrow);
                }
                if (arrows.size() != 1 || Boolean.TRUE.equals(read(generation, "retired"))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation)
                    throw new AssertionError("Original sequential charge/fire lost genuine authority for " + id);
                CompoundTag signed = net.farmingtales.ftboss.item.NativeVanillaImpact.snapshot(arrows.get(0));
                if (signed.isEmpty() || !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(signed, player)
                    || !signed.hasUUID("OwnerRepairAuthorityEpoch") || signed.getString("ServerSignature").length() != 64)
                    throw new AssertionError("Original sequential Crossbow factory lacks paid signed carrier for " + id);
                liveOriginals.add(actual);
                Object sharedNode = nodeMethod.invoke(null, net.minecraft.world.item.component.ChargedProjectiles.EMPTY, false);
                if (sharedNode == null) throw new AssertionError("Actual shared EMPTY revocation node absent");
                Object[] owners = (Object[])read(sharedNode, "owners");
                int liveOwners = 0;
                for (Object reference : owners) {
                    Object record = reference == null ? null : ((java.lang.ref.Reference<?>)reference).get();
                    if (record == null) continue;
                    Object ownerActual = ((java.lang.ref.Reference<?>)read(record, "actual")).get();
                    if (ownerActual != null && recordMethod.invoke(null, ownerActual, false) == record) liveOwners++;
                }
                row.put("actualSharedEmptyOwnerSlotCapacity", owners.length);
                row.put("actualSharedEmptyCurrentOwnerCount", liveOwners);
                row.put("actualOriginalFactoryCarrierPaidAndValid", true);
                row.put("genuineGenerationLiveAfterOriginalSequentialFire", !Boolean.TRUE.equals(read(generation, "retired")));
                row.put("actualDamageAfterOriginalFire", actual.getDamageValue());
                if (liveOwners < liveOriginals.size() || (liveOriginals.size() > 4 && owners.length <= 4))
                    throw new AssertionError("Shared original EMPTY did not retain every actual owner beyond four slots");
                for (var original : liveOriginals)
                    if (leaseMethod.invoke(null, original) == null || Boolean.TRUE.equals(read(generationMethod.invoke(null, original), "retired")))
                        throw new AssertionError("Earlier genuine owner was lost during capacity growth");
                storage.save(player);
                row.put("actualOriginalPostFactorySaveReturned", true);
                for (var arrow : arrows) arrow.discard();
                target.discard();
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).remove(owner, player);
            }
            List<CompoundTag> imagesBeforeNotice = new ArrayList<>();
            for (var original : liveOriginals) imagesBeforeNotice.add(net.farmingtales.ftboss.item.NativeFoundationItemData.image(original));
            net.farmingtales.ftboss.item.OwnerFullRepairGuard.changed(net.minecraft.world.item.component.ChargedProjectiles.EMPTY, 21);
            for (int index = 0; index < liveOriginals.size(); index++) {
                var original = liveOriginals.get(index);
                if (!Boolean.TRUE.equals(read(generationMethod.invoke(null, original), "retired"))
                    || !imagesBeforeNotice.get(index).equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(original)))
                    throw new AssertionError("Foreign public shared-node mutation notice did not retire every owner without changing items");
            }
            report.put("allSixActualOriginalCrossbowFactoriesBeyondFourSharedOwners", true);
            report.put("foreignPublicSharedNodeMutationNoticeRetiredAllSixWithoutItemMutation", true);
            report.put("originalPhysicalMutationActuallyPerformedByNoticeControl", false);
            report.put("fullSignedImpactPerformedByCapacityFixture", false);
            report.put("failures", 0);
        } catch (Throwable failure) {
            report.put("failures", 1); report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(trace)); report.put("trace", trace.toString());
        } finally {
            try {
                var players = (List<ServerPlayer>)read(event.getServer().getPlayerList(), "players");
                var byId = (Map<UUID, ServerPlayer>)read(event.getServer().getPlayerList(), "playersByUUID");
                for (ServerPlayer scratch : scratches) { players.remove(scratch); byId.remove(scratch.getUUID(), scratch); }
                for (var channel : channels) channel.finishAndReleaseAll();
                report.put("scratchPlayersRemoved", scratches.stream().noneMatch(players::contains));
            } catch (Exception failure) { report.put("failures", 1); report.put("cleanupFailure", failure.toString()); }
        }
        report.put("authorityWrittenByReflection", false); report.put("authenticatedMultiplayerAccepted", false);
        report.put("allSixFamilySignedCombatAccepted", false);
        report.put("crossbowEntry", "Actual ItemStack.use -> original Crossbow charge/releaseUsing -> actual charged ItemStack.use -> native arrow.tick impact");
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
