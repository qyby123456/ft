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
            row.put("actualDamageSourceClass", event.getSource().getClass().getName());
            Object captureStorage = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "EVENT_CAPTURES");
            java.util.Deque<?> currentCaptures = (java.util.Deque<?>)((ThreadLocal<?>)captureStorage).get();
            row.put("originalEventCaptureCount", currentCaptures.size());
            Object currentCapture = currentCaptures.peekLast();
            if (currentCapture != null) {
                row.put("originalEventCaptureFailed", read(currentCapture, "failed"));
                row.put("originalEventCaptureClosed", read(currentCapture, "closed"));
                row.put("originalEventCapturedProducerCount", ((java.util.List<?>)read(currentCapture, "producers")).size());
                Object binding = read(currentCapture, "originalBinding");
                Object currentRoot = read(binding, "root");
                row.put("originalEventRootSite", String.valueOf(read(currentRoot, "site")));
                row.put("originalEventRootRetired", read(currentRoot, "retired"));
                row.put("originalEventRootClosed", read(currentRoot, "closed"));
                row.put("originalEventBindingClosed", read(binding, "closed"));
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
            for (int sample : new int[]{0, 1, 2, 3, 4, 5}) {
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
                double dispatchedMaterial = net.farmingtales.ftboss.item.WeaponMaterialBuild.build(actual).damage();
                double nativeMaterial = net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.combat(actual).damage();
                row.put("dispatchedMaterialFactor", dispatchedMaterial);
                row.put("nativeFoundationMaterialFactor", nativeMaterial);
                if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual)
                    && Double.doubleToLongBits(dispatchedMaterial) != Double.doubleToLongBits(nativeMaterial))
                    throw new AssertionError("Damage accounting ignored the authoritative native foundation for " + id);

                row.put("nativeFoundationApplies", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual));
                row.put("nativeFoundationBlocksUse", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(actual));
                row.put("nativeFoundationStatus", String.valueOf(net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.status(actual)));
                row.put("oldSwordRequiredMissing", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual));
                row.put("lifecycleBrokenBeforeUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                if (net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual))
                    throw new AssertionError("Actual paid native foundation was falsely broken for " + id);
                CompoundTag missingImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                missingImage.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossFourWeaponFoundation").put("Cells", new net.minecraft.nbt.ListTag());
                var missingWeapon = net.minecraft.world.item.ItemStack.parseOptional(server.registryAccess(), missingImage);
                boolean missingDenied = !missingWeapon.isEmpty() && net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(missingWeapon)
                    && net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(missingWeapon);
                row.put("missingNativeCellsDenied", missingDenied);
                CompoundTag invalidImage = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                invalidImage.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossFourWeaponFoundation").putInt("Schema", 0);
                var invalidWeapon = net.minecraft.world.item.ItemStack.parseOptional(server.registryAccess(), invalidImage);
                boolean invalidDenied = !invalidWeapon.isEmpty() && net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.blocksUse(invalidWeapon)
                    && net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(invalidWeapon);
                row.put("malformedNativeFoundationDenied", invalidDenied);
                row.put("sameGenuineAuthorityAfterGateControls", leaseMethod.invoke(null, actual) == actualLease
                    && generationMethod.invoke(null, actual) == generation && !Boolean.TRUE.equals(read(generation, "retired")));
                if (!missingDenied || !invalidDenied || !Boolean.TRUE.equals(row.get("sameGenuineAuthorityAfterGateControls")))
                    throw new AssertionError("Native foundation gate or negative control changed authority");

                var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
                var channel = new io.netty.channel.embedded.EmbeddedChannel(connection); channels.add(channel);
                player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player,
                    net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(player);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(owner, player);
                player.moveTo(0.0, -59.0, 0.0, 0.0F, 0.0F);
                player.setHealth(10.0F);
                var target = new net.minecraft.world.entity.monster.Zombie(server.overworld());
                target.setNoAi(true); target.moveTo(0.0, -59.0, 1.5, 0.0F, 0.0F);
                if (!server.overworld().addFreshEntity(target)) throw new AssertionError("Actual melee target was not added");
                actions.clear(); damageEvents.clear();
                float healthBefore = target.getHealth();
                int wearBefore = actual.getDamageValue();
                row.put("actualItemClass", actual.getItem().getClass().getName());
                row.put("selectedSlot", player.getInventory().selected);
                row.put("actualIsMainHandBeforeUse", player.getMainHandItem() == actual);
                row.put("actualCountBeforeUse", actual.getCount());
                row.put("actualDamageBeforeUse", actual.getDamageValue());
                row.put("playerAliveBeforeUse", player.isAlive());
                Class<?> tridentCoverageType = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                var tridentCertificate = tridentCoverageType.getDeclaredMethod("nativeTridentCertified"); tridentCertificate.setAccessible(true);
                row.put("underlyingAbstractArrowCustodyProofCertified", tridentCertificate.invoke(null));
                row.put("brokenBeforeOriginalUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                row.put("durabilityDepletedBeforeUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.durabilityDepleted(actual));
                row.put("cavityAppliesBeforeUse", net.farmingtales.ftboss.item.SwordCavityBuild.applies(actual));
                row.put("cavityMissingBeforeUse", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual));
                row.put("functionProfileBeforeUse", String.valueOf(net.farmingtales.ftboss.api.SwordCavityExtensions.functionProfile(actual)));
                row.put("functionStatusBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(actual)));
                row.put("nativePartsBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordNativeMaterials.parts(actual)));
                row.put("cavityCellsBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordCavityBuild.cells(actual)));
                row.put("generationRetiredAfterStructureReads", read(generation, "retired"));

                Map<String, Object> useControls = new LinkedHashMap<>(); row.put("actualUseRefusalControls", useControls);
                CompoundTag beforeRefusal = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object inputBeforeHelpers = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseEntered(actual, server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseReturned(actual);
                Object rangedLocal = readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_RANGED_USE");
                boolean foreignUnchanged = ((ThreadLocal<?>)rangedLocal).get() == null
                    && inputBeforeHelpers == ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput()
                    && beforeRefusal.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired"));
                useControls.put("foreignUseCallbacksDoNotMintOrModifyAuthority", foreignUnchanged);
                if (!foreignUnchanged) throw new AssertionError("Foreign original use helpers changed private authority");
                var noAmmoResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                boolean noAmmoRefused = !noAmmoResult.getResult().consumesAction() && !player.isUsingItem()
                    && beforeRefusal.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired")) && ((ThreadLocal<?>)rangedLocal).get() == null;
                useControls.put("originalNoAmmoUseResult", noAmmoResult.getResult().toString());
                useControls.put("originalNoAmmoUseRefusedWithSameWholeImageAndLiveAuthority", noAmmoRefused);
                if (!noAmmoRefused) throw new AssertionError("Original no-ammo bow use altered paid state or leaked private use scope");
                target.discard();
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
        report.put("bowEntry", "Actual original no-ammo ItemStack.use refusal and outside callbacks on genuinely paid material Bows; no projectile impact executed by this control fixture");
        report.put("foundationGateScopeOnly", true); report.put("fullLifecycleAccepted", false); report.put("installable", false);
        try { Files.writeString(Path.of("actual-storage-view308.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
