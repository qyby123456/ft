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
                Object inputBeforeForeign = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                CompoundTag imageBeforeForeign = (CompoundTag)actual.save(player.registryAccess());
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseEntered(actual, server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority.nativeCrossbowComponentReturned(actual, net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,
                    net.minecraft.world.item.component.ChargedProjectiles.EMPTY, charged);
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseReturned(actual);
                boolean foreignCallbacksUnchanged = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput() == inputBeforeForeign
                    && imageBeforeForeign.equals(actual.save(player.registryAccess())) && !Boolean.TRUE.equals(read(generation, "retired"));
                row.put("outsideOriginalCallbacksPreservedCurrentInputImageAndAuthority", foreignCallbacksUnchanged);
                if (!foreignCallbacksUnchanged) throw new AssertionError("Foreign callback modified original charge authority");
                player.stopUsingItem();
                var fireResult = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeUse(actual.getItem(), actual, server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                row.put("actualOriginalFireUseResult", fireResult.getResult().toString());
                row.put("generationRetiredAfterFire", read(generation, "retired"));
                List<Map<String, Object>> flights = new ArrayList<>(); row.put("actualProjectileFlights", flights);
                List<net.minecraft.world.entity.projectile.AbstractArrow> arrows = new ArrayList<>();
                for (var entity : server.overworld().getAllEntities()) {
                    if (entity instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow && arrow.getOwner() == player) arrows.add(arrow);
                }
                for (var arrow : arrows) {
                    Map<String, Object> flight = new LinkedHashMap<>(); flights.add(flight);
                    CompoundTag image = arrow.getPersistentData().getCompound("FTBossWeaponMaterials");
                    flight.put("snapshotPresent", !image.isEmpty());
                    flight.put("snapshotValid", net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(image, player));
                    flight.put("repairEpochPresent", image.hasUUID("OwnerRepairAuthorityEpoch"));
                    flight.put("signaturePresent", image.getString("ServerSignature").length() == 64);
                    flight.put("purpose", image.getString("DamagePurpose"));
                    flight.put("nativeEntry", image.getString("NativeEntry"));
                    flight.put("paidCarrierPresent", arrow instanceof net.farmingtales.ftboss.item.NativePaidWeaponCarrier carrier && carrier.ftboss$originalOwnerPaidWeapon() != null);
                    for (int ticks = 0; ticks < 12 && !arrow.isRemoved() && target.getHealth() == healthBefore; ticks++) arrow.tick();
                    flight.put("targetHealthAfterFlight", target.getHealth());
                    arrow.discard();
                }
                row.put("originalCrossbowChargeAndFireReturned", true);
                row.put("healthBefore", healthBefore); row.put("healthAfter", target.getHealth());
                row.put("wearBefore", wearBefore); row.put("wearAfter", actual.getDamageValue());
                row.put("damageEvents", new ArrayList<>(damageEvents));
                row.put("foreignNativeUseOriginalReturnedOnce", fireResult.getResult().consumesAction() && arrows.size() == 1 && actual.getDamageValue() == wearBefore + 1);
                row.put("foreignNativeUseAuthorityRetired", read(generation, "retired"));
                row.put("foreignNativeUseNoSignedCarrier", flights.size() == 1 && flights.stream().noneMatch(value -> Boolean.TRUE.equals(value.get("snapshotPresent"))
                    || Boolean.TRUE.equals(value.get("snapshotValid")) || Boolean.TRUE.equals(value.get("paidCarrierPresent"))));
                boolean scopesClosed = true;
                for (String field : List.of("NATIVE_OPERATION", "PRIVATE_RANGED_USE", "PRIVATE_NATIVE_HURT", "PRIVATE_INGRESS"))
                    scopesClosed &= ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, field)).get() == null;
                for (String field : List.of("NATIVE_REPAIR_OPERATION", "NATIVE_WEAR"))
                    scopesClosed &= ((ThreadLocal<?>)readStatic(Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority"), field)).get() == null;
                row.put("privateScopesClosedWithoutWrites", scopesClosed);
                if (!Boolean.TRUE.equals(row.get("foreignNativeUseOriginalReturnedOnce")) || !Boolean.TRUE.equals(read(generation, "retired"))
                    || !Boolean.TRUE.equals(row.get("foreignNativeUseNoSignedCarrier")) || !scopesClosed || !actions.isEmpty()
                    || damageEvents.isEmpty() || damageEvents.stream().anyMatch(value -> Boolean.TRUE.equals(value.get("resolvedSnapshotValid"))))
                    throw new AssertionError("Foreign nativeUse minted signed paid combat authority or did not preserve original operation");
                if (target.getHealth() >= healthBefore) throw new AssertionError("Foreign original arrow did not hit this case's actual target");
                target.discard();
                row.put("actualOriginalTargetHitAndRemovedBeforeNextCase", true);
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).remove(owner, player);
            }
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
        report.put("crossbowEntry", "Actual original paid charge, then outside callback no-ops and foreign public nativeUse delegation with original arrow.tick but no signed paid authority");
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
