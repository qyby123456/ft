package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.LevelResource;

public final class OwnerStatueWearAdmission {
    private static final Map<ItemStack, OwnerStatueWearAdmission.Accepted> ACCEPTED = new WeakHashMap<>();
    private static final Object ACCEPTED_LOCK = new Object();
    private static final Map<ItemStack, WeakReference<OwnerStatueWearAdmission.Root>> LIVE_ROOTS = new WeakHashMap<>();
    private static final ThreadLocal<Load> SAVING = new ThreadLocal<>();

    private OwnerStatueWearAdmission() {
    }

    private static boolean storageCaller(int kind, String guard) {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream
            .filter(frame -> frame.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class).limit(2L).toList());
        return frames.size() == 2 && frames.get(0).getDeclaringClass() == PlayerDataStorage.class
            && frames.get(0).getMethodName().equals(guard) && frames.get(1).getDeclaringClass() == PlayerDataStorage.class
            && OwnerStatueWearBoundary.original(kind, frames.get(1).getMethodName(), frames.get(1).getDescriptor());
    }

    public static Load saveEntered(PlayerDataStorage storage, Player player) {
        if (!storageCaller(100, "ftboss$nativeRepairSaveEnterGuard") || storage == null || storage.getClass() != PlayerDataStorage.class
            || !(player instanceof ServerPlayer actual) || actual.getServer() == null || !actual.getServer().isSameThread()) return null;
        Load nested = SAVING.get();
        if (nested != null) {
            nested.saveRejected = true;
            SAVING.remove();
            return null;
        }
        Load context = new Load(actual.getInventory(), actual, actual.getServer());
        try {
            HashSet<UUID> weapons = new HashSet<>();
            for (int slot = 0; slot < context.inventory.getContainerSize(); slot++) {
                ItemStack stack = context.inventory.getItem(slot);
                OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(stack);
                if (lease == null) continue;
                CompoundTag expected = NativeFoodWeaponWear.persistenceImage(actual, stack);
                if (expected == null || !weapons.add(lease.weapon)) throw new IllegalStateException("Native save authority/image or unique custody changed");
                context.saveItems.put(slot, stack);
                context.saveExpected.put(slot, expected);
                context.saveLeases.put(slot, lease);
                context.saveGenerations.put(slot, OwnerFullRepairGuard.generation(stack));
            }
            SAVING.set(context);
            return context;
        } catch (RuntimeException | Error failure) {
            context.saveRejected = true;
            net.farmingtales.ftboss.FTBossMod.LOGGER.error("Native repair save capture rejected", failure);
            return null;
        }
    }

    private static void validateSave(Load context) {
        if (context == null || SAVING.get() != context || context.closed || context.saveRejected
            || context.thread != Thread.currentThread() || !context.server.isSameThread()
            || context.player.getServer() != context.server || context.player.getInventory() != context.inventory
            || !context.owner.equals(context.player.getUUID())) throw new IllegalStateException("Native save context no longer live");
        HashSet<UUID> weapons = new HashSet<>();
        for (int slot = 0; slot < context.inventory.getContainerSize(); slot++) {
            ItemStack stack = context.inventory.getItem(slot);
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(stack);
            if (lease == null && !context.saveItems.containsKey(slot)) continue;
            CompoundTag expected = NativeFoodWeaponWear.persistenceImage(context.player, stack);
            if (context.saveItems.get(slot) != stack || context.saveLeases.get(slot) != lease
                || context.saveGenerations.get(slot) != OwnerFullRepairGuard.generation(stack)
                || expected == null || !expected.equals(context.saveExpected.get(slot)) || !weapons.add(lease.weapon))
                throw new IllegalStateException("Native save physical lineage/image/custody changed");
        }
    }

    public static void saveEncoded(CompoundTag encoded) {
        if (!storageCaller(101, "ftboss$nativeRepairSaveEncodeGuard")) return;
        Load context = SAVING.get();
        if (context == null) return;
        try {
            validateSave(context);
            if (encoded == null || encoded.getClass() != CompoundTag.class || !encoded.hasUUID("UUID")
                || !context.owner.equals(encoded.getUUID("UUID")) || context.saveSerialized != null
                || encoded.contains("FTBossRepairSave")) throw new IllegalStateException("Changed original player serialization");
            OwnerLineageRevocationStore store = NativeFoodWeaponWear.persistenceStore(context.player);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + context.owner).getBytes(StandardCharsets.UTF_8));
            CompoundTag before = store.readRepairDocument(documentOwner, false);
            if (before.isEmpty() && context.saveItems.isEmpty()) {
                context.saveSkip = true;
                return;
            }
            if (!before.isEmpty() && (before.getInt("Schema") != 1 || !before.hasUUID("Owner")
                || !context.owner.equals(before.getUUID("Owner")))) throw new IllegalStateException("Native save journal owner/schema changed");
            CompoundTag next = before.copy();
            next.putInt("Schema", 1);
            next.putUUID("Owner", context.owner);
            next.putUUID("LastSave", context.identity);
            next.putLong("Sequence", Math.addExact(before.getLong("Sequence"), 1L));
            next.putString("Status", "PENDING_ORIGINAL_SAVE");
            CompoundTag records = new CompoundTag();
            HashSet<Integer> observedSlots = new HashSet<>();
            ListTag inventory = encoded.getList("Inventory", 10);
            if (inventory.size() > 256) throw new IllegalStateException("Native save inventory budget");
            for (Tag value : inventory) {
                if (!(value instanceof CompoundTag entry) || entry.contains("FTBossRepairContinuation"))
                    throw new IllegalStateException("Unknown original inventory continuation metadata");
                int encodedSlot = entry.getByte("Slot") & 255;
                int slot = encodedSlot == 150 ? 40 : encodedSlot >= 100 && encodedSlot < 104 ? encodedSlot - 64 : encodedSlot;
                if (!context.saveItems.containsKey(slot)) continue;
                if (!observedSlots.add(slot) || !context.saveExpected.get(slot).equals(NativeInventoryItemTrace.image(entry)))
                    throw new IllegalStateException("Original saved entry differs from independent expected item");
                OwnerLineageRevocationStore.Lease lease = context.saveLeases.get(slot);
                CompoundTag expected = context.saveExpected.get(slot);
                CompoundTag ticket = new CompoundTag();
                ticket.putUUID("Save", context.identity);
                ticket.putUUID("Owner", context.owner);
                ticket.putUUID("Weapon", lease.weapon);
                ticket.putUUID("NativeIdentity", expected.getCompound("components").getCompound("minecraft:custom_data")
                    .getCompound("FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity"));
                ticket.putUUID("LeaseEpoch", lease.incarnation);
                ticket.putLong("LeaseGeneration", lease.generation);
                ticket.putInt("Slot", slot);
                CompoundTag record = new CompoundTag();
                record.putString("Status", "PENDING_ORIGINAL_SAVE");
                record.put("Ticket", ticket.copy());
                record.put("Expected", expected.copy());
                records.put(lease.weapon.toString(), record);
                context.saveTickets.put(slot, ticket);
            }
            if (observedSlots.size() != context.saveItems.size()) throw new IllegalStateException("Original save omitted a live repaired item");
            next.put("Continuations", records);
            store.writeRepairDocument(documentOwner, false, before, next);
            context.saveStore = store;
            context.saveDocumentOwner = documentOwner;
            context.saveDocument = next.copy();
            for (Tag value : inventory) {
                CompoundTag entry = (CompoundTag)value;
                int encodedSlot = entry.getByte("Slot") & 255;
                int slot = encodedSlot == 150 ? 40 : encodedSlot >= 100 && encodedSlot < 104 ? encodedSlot - 64 : encodedSlot;
                CompoundTag ticket = context.saveTickets.get(slot);
                if (ticket != null) entry.put("FTBossRepairContinuation", ticket.copy());
            }
            encoded.putUUID("FTBossRepairSave", context.identity);
            context.saveSerializedSource = encoded;
            context.saveSerialized = encoded.copy();
            validateSave(context);
        } catch (RuntimeException | IOException | Error failure) {
            context.saveRejected = true;
            net.farmingtales.ftboss.FTBossMod.LOGGER.error("Native repair original-save encoding rejected", failure);
        }
    }

    public static void saveReplaced(Path target, Path temporary, Path backup) {
        if (!storageCaller(101, "ftboss$nativeRepairSaveReplaceGuard")) return;
        Load context = SAVING.get();
        if (context == null || context.saveSkip) return;
        try {
            validateSave(context);
            Path directory = context.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toAbsolutePath().normalize();
            Path expected = directory.resolve(context.owner + ".dat");
            Path actual = target == null ? null : target.toAbsolutePath().normalize();
            Path pending = temporary == null ? null : temporary.toAbsolutePath().normalize();
            Path previous = backup == null ? null : backup.toAbsolutePath().normalize();
            if (!expected.equals(actual) || pending == null || previous == null
                || !directory.equals(pending.getParent()) || !directory.equals(previous.getParent())
                || !previous.getFileName().toString().equals(context.owner + ".dat_old")
                || actual.equals(pending) || actual.equals(previous) || pending.equals(previous)
                || Files.isSymbolicLink(directory) || Files.isSymbolicLink(actual)
                || Files.isSymbolicLink(pending) || Files.isSymbolicLink(previous)
                || !directory.toRealPath().equals(directory) || !Files.isRegularFile(actual)
                || context.saveSerialized == null || !context.saveSerialized.equals(context.saveSerializedSource)
                || !context.saveSerialized.equals(NbtIo.readCompressed(actual, NbtAccounter.unlimitedHeap())))
                throw new IllegalStateException("Original player-save replacement did not install the exact planned snapshot");
            context.saveTarget = actual;
            context.saveReplaced = true;
        } catch (RuntimeException | IOException | Error failure) {
            context.saveRejected = true;
            net.farmingtales.ftboss.FTBossMod.LOGGER.error("Native repair original-save replacement rejected", failure);
        }
    }

    public static void saveExited(Load context, Throwable originalFailure) {
        if (!storageCaller(100, "ftboss$nativeRepairSaveExitGuard") || context == null || SAVING.get() != context) return;
        try {
            if (originalFailure != null || context.saveRejected || context.saveSkip) return;
            validateSave(context);
            if (!context.saveReplaced || context.saveStore == null || context.saveDocument == null
                || context.saveSerialized == null || context.saveTarget == null
                || !context.saveSerialized.equals(context.saveSerializedSource)
                || !context.saveSerialized.equals(NbtIo.readCompressed(context.saveTarget, NbtAccounter.unlimitedHeap())))
                throw new IllegalStateException("Original player-save terminal is missing or changed");
            CompoundTag committed = context.saveDocument.copy();
            if (!"PENDING_ORIGINAL_SAVE".equals(committed.getString("Status"))
                || !context.identity.equals(committed.getUUID("LastSave"))) throw new IllegalStateException("Native original-save terminal replay");
            committed.putString("Status", "COMMITTED_ORIGINAL_SAVE");
            CompoundTag records = committed.getCompound("Continuations");
            for (String key : records.getAllKeys()) {
                CompoundTag record = records.getCompound(key);
                if (!"PENDING_ORIGINAL_SAVE".equals(record.getString("Status"))) throw new IllegalStateException("Native save entry terminal replay");
                record.putString("Status", "COMMITTED_ORIGINAL_SAVE");
            }
            context.saveStore.writeRepairDocument(context.saveDocumentOwner, false, context.saveDocument, committed);
            context.saveDocument = committed;
        } catch (RuntimeException | IOException | Error failure) {
            context.saveRejected = true;
            net.farmingtales.ftboss.FTBossMod.LOGGER.error("Native repair original-save terminal rejected", failure);
        } finally {
            context.closed = true;
            SAVING.remove();
        }
    }

    private static boolean originalCaller(int var0, String var1) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var2 -> {
                    java.util.List<java.lang.StackWalker.StackFrame> var3 = var2.filter(
                            var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class
                                    && var0xx.getDeclaringClass() != OwnerNativeSemanticAuthority.class
                                    && var0xx.getDeclaringClass() != OwnerStatueTaskService.class
                        )
                        .limit(2L)
                        .toList();
                    return var3.size() == 2
                        && ((StackFrame)var3.get(0)).getDeclaringClass() == (var0 == 0 ? Inventory.class : ItemStack.class)
                        && ((StackFrame)var3.get(0)).getMethodName().equals(var1)
                        && ((StackFrame)var3.get(1)).getDeclaringClass() == ((StackFrame)var3.get(0)).getDeclaringClass()
                        && OwnerStatueWearBoundary.original(var0, ((StackFrame)var3.get(1)).getMethodName(), ((StackFrame)var3.get(1)).getDescriptor());
                }
            );
    }

    public static OwnerStatueWearAdmission.Load loadEntered(Inventory var0, ListTag var1) {
        if (originalCaller(0, "ftboss$statueWearLoadGuard")
            && var0 != null
            && var1 != null
            && var1.getClass() == ListTag.class
            && var0.player instanceof ServerPlayer var2
            && var2.getInventory() == var0
            && var2.getServer() != null
            && var2.getServer().isSameThread()
            && var1.size() <= 256) {
            boolean var8 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0x -> {
                        java.util.List<java.lang.StackWalker.StackFrame> var1x = var0x.filter(var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class).limit(8L).toList();
                        return var1x.size() == 8
                            && ((StackFrame)var1x.get(0)).getDeclaringClass() == Inventory.class
                            && ((StackFrame)var1x.get(0)).getMethodName().equals("ftboss$statueWearLoadGuard")
                            && ((StackFrame)var1x.get(1)).getDeclaringClass() == Inventory.class
                            && OwnerStatueWearBoundary.original(0, ((StackFrame)var1x.get(1)).getMethodName(), ((StackFrame)var1x.get(1)).getDescriptor())
                            && ((StackFrame)var1x.get(2)).getDeclaringClass() == Player.class
                            && ((StackFrame)var1x.get(2)).getMethodName().equals("readAdditionalSaveData")
                            && ((StackFrame)var1x.get(3)).getDeclaringClass() == ServerPlayer.class
                            && ((StackFrame)var1x.get(3)).getMethodName().equals("readAdditionalSaveData")
                            && ((StackFrame)var1x.get(4)).getDeclaringClass() == Entity.class
                            && ((StackFrame)var1x.get(4)).getMethodName().equals("load")
                            && ((StackFrame)var1x.get(5)).getDeclaringClass() == PlayerDataStorage.class
                            && ((StackFrame)var1x.get(5)).getMethodName().equals("lambda$load$1")
                            && ((StackFrame)var1x.get(5)).getDescriptor().equals("(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;")
                            && ((StackFrame)var1x.get(6)).getDeclaringClass() == Optional.class
                            && ((StackFrame)var1x.get(6)).getMethodName().equals("map")
                            && ((StackFrame)var1x.get(6)).getDescriptor().equals("(Ljava/util/function/Function;)Ljava/util/Optional;")
                            && ((StackFrame)var1x.get(7)).getDeclaringClass() == PlayerDataStorage.class
                            && ((StackFrame)var1x.get(7)).getMethodName().equals("load")
                            && ((StackFrame)var1x.get(7)).getDescriptor().equals("(Lnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;");
                    }
                );
            if (!var8) {
                return null;
            } else {
                OwnerStatueWearAdmission.Load var4 = new OwnerStatueWearAdmission.Load(var0, var2, var2.getServer());

                for (int var5 = 0; var5 < var1.size(); var5++) {
                    if (!(var1.get(var5) instanceof CompoundTag var6) || var6.getClass() != CompoundTag.class) {
                        return null;
                    }

                    var4.inputs.put(var6, NativeExpectedRawFields.observe(var6));
                }

                return var4;
            }
        } else {
            return null;
        }
    }

    public static void loadBound(OwnerStatueWearAdmission.Load var0, CompoundTag var1, ItemStack var2) {
        if (originalCaller(0, "ftboss$statueWearBindGuard")
            && var0 != null
            && !var0.closed
            && var0.thread == Thread.currentThread()
            && var0.player.getInventory() == var0.inventory
            && var0.player.getServer() == var0.server
            && var0.server.isSameThread()
            && var0.owner.equals(var0.player.getUUID())
            && var0.inputs.containsKey(var1)) {
            quoteNativeReload(var0, var1, var2);
            OwnerNativeSemanticAuthority.wearInputLoaded(var0, var1, var2);
        }
    }

    private static void quoteNativeReload(Load context, CompoundTag source, ItemStack actual) {
        Value quoted = context.inputs.get(source);
        if (FoodWeaponExpectedTrace.field(quoted, "FTBossRepairContinuation") == null) return;
        try {
            if (quoted == null) throw new IllegalStateException("Original native reload source quote missing");
            CompoundTag snapshot = (CompoundTag)FoodWeaponExpectedTrace.materialize(quoted);
            CompoundTag ticket = snapshot.getCompound("FTBossRepairContinuation");
            if (!snapshot.contains("Slot", 1) || !snapshot.contains("FTBossRepairContinuation", 10)
                || !ticket.getAllKeys().equals(Set.of("Save", "Owner", "Weapon", "NativeIdentity", "LeaseEpoch", "LeaseGeneration", "Slot"))
                || !ticket.hasUUID("Save") || !ticket.hasUUID("Owner") || !ticket.hasUUID("Weapon")
                || !ticket.hasUUID("NativeIdentity") || !ticket.hasUUID("LeaseEpoch")
                || !ticket.contains("LeaseGeneration", 4) || ticket.getLong("LeaseGeneration") < 0L
                || !ticket.contains("Slot", 3) || !context.owner.equals(ticket.getUUID("Owner")))
                throw new IllegalStateException("Malformed original native reload ticket");
            int encodedSlot = snapshot.getByte("Slot") & 255;
            int slot = encodedSlot == 150 ? 40 : encodedSlot >= 100 && encodedSlot < 104 ? encodedSlot - 64 : encodedSlot;
            CompoundTag expected = NativeFoodWeaponWear.loadedPersistenceImage(context.player, actual, source, quoted);
            if (slot < 0 || slot >= context.inventory.getContainerSize() || ticket.getInt("Slot") != slot || expected == null
                || !expected.equals(FoodWeaponExpectedTrace.materialize(NativeInventoryItemTrace.project(quoted))) || context.reloadItems.containsKey(slot))
                throw new IllegalStateException("Original native reload item/slot/unique source mismatch");
            CompoundTag lifecycle = expected.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossWeaponLifecycle");
            if (!ticket.getUUID("Weapon").equals(lifecycle.getUUID("Weapon"))
                || !ticket.getUUID("NativeIdentity").equals(lifecycle.getCompound("NativeWear").getUUID("Identity")))
                throw new IllegalStateException("Original native reload weapon identity mismatch");
            context.reloadItems.put(slot, actual);
            context.reloadSources.put(slot, source);
            context.reloadExpected.put(slot, expected);
            context.reloadTickets.put(slot, ticket.copy());
        } catch (RuntimeException | Error failure) {
            context.reloadRejected = true;
            net.farmingtales.ftboss.FTBossMod.LOGGER.error("Native repair original-load quote rejected", failure);
        }
    }

    private static ItemStack validateReloadSlot(Load context, int slot) {
        if (context == null || !context.closed || !context.reloadFinishing || context.reloadRejected
            || context.thread != Thread.currentThread() || !context.server.isSameThread()
            || context.player.getServer() != context.server || context.player.getInventory() != context.inventory
            || !context.owner.equals(context.player.getUUID())) throw new IllegalStateException("Original native reload context changed");
        ItemStack actual = context.reloadItems.get(slot);
        CompoundTag source = context.reloadSources.get(slot);
        Value quoted = context.inputs.get(source);
        CompoundTag expected = actual == null ? null : NativeFoodWeaponWear.loadedPersistenceImage(context.player, actual, source, quoted);
        if (actual == null || context.inventory.getItem(slot) != actual || source == null || quoted == null
            || expected == null || !expected.equals(context.reloadExpected.get(slot)))
            throw new IllegalStateException("Original native reload terminal item/source/custody changed");
        return actual;
    }

    private static void restoreNativeLoaded(Load context) {
        if (!originalCaller(0, "ftboss$statueWearLoadExitGuard") || context.reloadRejected || context.reloadItems.isEmpty()) return;
        context.reloadFinishing = true;
        try {
            HashSet<UUID> weapons = new HashSet<>();
            for (int slot = 0; slot < context.inventory.getContainerSize(); slot++) {
                ItemStack stack = context.inventory.getItem(slot);
                if (!NativeFoodWeaponWear.eligible(stack)) continue;
                UUID weapon = FoodWeaponLifecycle.weaponId(stack);
                if (weapon != null && !weapons.add(weapon)) throw new IllegalStateException("Duplicate original loaded weapon identity");
            }
            for (int slot : context.reloadItems.keySet()) validateReloadSlot(context, slot);
            context.reloadStore = NativeFoodWeaponWear.persistenceStore(context.player);
            UUID documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-continuation/v1/" + context.owner).getBytes(StandardCharsets.UTF_8));
            for (int slot : context.reloadItems.keySet()) {
                OwnerLineageRevocationStore.Lease issued = null;
                ItemStack actual = context.reloadItems.get(slot);
                try {
                    validateReloadSlot(context, slot);
                    CompoundTag ticket = context.reloadTickets.get(slot);
                    CompoundTag before = context.reloadStore.readRepairDocument(documentOwner, false);
                    CompoundTag record = before.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
                    if (before.getInt("Schema") != 1 || !context.owner.equals(before.getUUID("Owner"))
                        || !ticket.getUUID("Save").equals(before.getUUID("LastSave"))
                        || !"COMMITTED_ORIGINAL_SAVE".equals(before.getString("Status"))
                        || !"COMMITTED_ORIGINAL_SAVE".equals(record.getString("Status"))
                        || !ticket.equals(record.getCompound("Ticket")) || !context.reloadExpected.get(slot).equals(record.getCompound("Expected")))
                        throw new IllegalStateException("Original native reload has no exact unused committed save continuation");
                    NativeItemCapabilityObservation capabilities = NativeFoodWeaponWear.loadedPersistenceCapabilities(actual);
                    if (!capabilities.live()) throw new IllegalStateException("Original loaded native capabilities changed");
                    context.reloadCapabilities.put(slot, capabilities);
                    CompoundTag reserved = before.copy();
                    CompoundTag reservedRecord = reserved.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
                    reservedRecord.putString("Status", "RESERVED_ORIGINAL_LOAD_NO_REISSUE");
                    reservedRecord.putUUID("Load", context.identity);
                    context.reloadStore.writeRepairDocument(documentOwner, false, before, reserved);
                    context.reloadRecords.put(slot, reservedRecord.copy());
                    issued = context.reloadStore.consumeNativeReload(context, slot);
                    context.reloadIssued.put(slot, issued);
                    if (!OwnerRepairGenerationAuthority.publishNativeReload(context, slot, actual, issued, capabilities))
                        throw new IllegalStateException("Original native reload physical publication rejected");
                    CompoundTag consumed = reserved.copy();
                    CompoundTag consumedRecord = consumed.getCompound("Continuations").getCompound(ticket.getUUID("Weapon").toString());
                    consumedRecord.putString("Status", "CONSUMED_ORIGINAL_LOAD");
                    consumedRecord.putUUID("SuccessorEpoch", issued.incarnation);
                    consumedRecord.putLong("SuccessorGeneration", issued.generation);
                    context.reloadStore.writeRepairDocument(documentOwner, false, reserved, consumed);
                    context.reloadRecords.put(slot, consumedRecord.copy());
                } catch (RuntimeException | IOException | Error failure) {
                    if (issued != null) {
                        OwnerLineageRevocationStore.retireLease(issued);
                        OwnerFullRepairGuard.changed(actual, 20);
                    }
                    net.farmingtales.ftboss.FTBossMod.LOGGER.error("Native repair original-load continuation rejected", failure);
                }
            }
        } catch (RuntimeException | Error failure) {
            context.reloadRejected = true;
            net.farmingtales.ftboss.FTBossMod.LOGGER.error("Native repair original-load terminal rejected", failure);
        } finally {
            context.reloadFinishing = false;
        }
    }

    static CompoundTag nativeReloadReservation(Load context, int slot, OwnerLineageRevocationStore store) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerLineageRevocationStore.class || !caller.getMethodName().equals("consumeNativeReload")) return null;
        validateReloadSlot(context, slot);
        CompoundTag record = context.reloadRecords.get(slot);
        NativeItemCapabilityObservation capabilities = context.reloadCapabilities.get(slot);
        return context.reloadStore == store && record != null && capabilities != null && capabilities.live()
            && "RESERVED_ORIGINAL_LOAD_NO_REISSUE".equals(record.getString("Status"))
            && context.identity.equals(record.getUUID("Load")) ? record.copy() : null;
    }

    static boolean nativeReloadCandidate(Load context, int slot, ItemStack actual, OwnerLineageRevocationStore.Lease lease,
                                         NativeItemCapabilityObservation capabilities) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerRepairGenerationAuthority.class || !caller.getMethodName().equals("publishNativeReload")) return false;
        if (validateReloadSlot(context, slot) != actual || context.reloadIssued.get(slot) != lease
            || context.reloadCapabilities.get(slot) != capabilities || !capabilities.live() || !context.reloadStore.owns(lease)) return false;
        CompoundTag ticket = context.reloadTickets.get(slot);
        return context.owner.equals(lease.owner) && ticket.getUUID("Weapon").equals(lease.weapon)
            && ticket.getUUID("LeaseEpoch").equals(lease.incarnation) && ticket.getLong("LeaseGeneration") != Long.MAX_VALUE
            && lease.generation == ticket.getLong("LeaseGeneration") + 1L;
    }

    static OwnerStatueWearAdmission.Origin quotedOrigin(
        OwnerStatueWearAdmission.Load var0, CompoundTag var1, ItemStack var2, OwnerNativeSemanticAuthority.WeaponInput var3, Value var4
    ) {
        if (!StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerNativeSemanticAuthority.class && var0xx.getMethodName().equals("wearInputLoaded"))
                        .orElse(false)
            )) {
            return null;
        } else {
            Value var5 = var0.inputs.get(var1);
            Value var6 = FoodWeaponExpectedTrace.field(var5, "Slot");
            if (var6 == null || var6.kind != 1 || !FoodWeaponExpectedTrace.same(var5, NativeExpectedRawFields.observe(var1))) {
                return null;
            } else if (NativeInventoryItemTrace.wearProjection(var5) == null) {
                return null;
            } else {
                Value var7 = NativeInventoryItemTrace.wearProjection(var5);
                if (!FoodWeaponExpectedTrace.same(var7, var4)) {
                    return null;
                } else {
                    int var8 = (int)var6.bits & 0xFF;
                    if (var8 == 150) {
                        var8 = 40;
                    } else if (var8 >= 100 && var8 < 104) {
                        var8 -= 64;
                    } else if (var8 < 0 || var8 >= 36) {
                        return null;
                    }

                    OwnerStatueWearAdmission.Origin var9 = new OwnerStatueWearAdmission.Origin(var0, var2, var3, var4, var8);
                    var0.origins.add(var9);
                    return var9;
                }
            }
        }
    }

    public static void loadExited(OwnerStatueWearAdmission.Load var0, Throwable var1) {
        if (originalCaller(0, "ftboss$statueWearLoadExitGuard") && var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
            var0.closed = true;
            if (var1 == null
                && var0.player.getInventory() == var0.inventory
                && var0.player.getServer() == var0.server
                && var0.server.isSameThread()
                && var0.owner.equals(var0.player.getUUID())) {
                for (OwnerStatueWearAdmission.Origin var3 : var0.origins) {
                    ItemStack var4 = var3.actual.get();
                    var3.loaded = var4 != null && var0.inventory.getItem(var3.slot) == var4 && OwnerFullRepairGuard.permits(var4);
                }

                restoreNativeLoaded(var0);

                var0.inputs.clear();
                var0.origins.clear();
            }
        }
    }

    private static boolean physical(OwnerStatueWearAdmission.Origin var0, ServerPlayer var1, ItemStack var2) {
        Inventory var3 = var0.inventory.get();
        if (var1 != null
            && var0.player.get() == var1
            && var3 != null
            && var1.getInventory() == var3
            && var0.server == var1.getServer()
            && var0.server.isSameThread()
            && var0.thread == Thread.currentThread()
            && var0.owner.equals(var1.getUUID())
            && var3.getItem(var0.slot) == var2) {
            return var0.slot == 40 ? var1.getOffhandItem() == var2 : var3.selected == var0.slot && var1.getMainHandItem() == var2;
        } else {
            return false;
        }
    }

    public static OwnerStatueWearAdmission.Frame entered(ItemStack var0, Object var1, int var2, int var3) {
        if (NativeFoodWeaponWear.eligible(var0) && originalCaller(var2, "ftboss$statueWearEnterGuard")) {
            OwnerNativeSemanticAuthority.NativeWear nativeOperation = OwnerNativeSemanticAuthority.nativeOperationEntered(var0, var1, var2);
            if (nativeOperation != null) return new OwnerStatueWearAdmission.Frame(nativeOperation);
        }
        if (OwnerRepairContinuation.noBinding(var0) && OwnerNativeSemanticAuthority.wearOrigin(var0) == null) {
            return null;
        } else if (NativeFoodWeaponWear.eligible(var0)) {
            return null;
        } else if (!originalCaller(var2, "ftboss$statueWearEnterGuard")) {
            return null;
        } else {
            OwnerRepairContinuation.Entry var4 = OwnerRepairContinuation.entered(var0, var1, var2, var3);
            if (var4 != null) {
                return new OwnerStatueWearAdmission.Frame(var4);
            } else {
                OwnerStatueWearAdmission.Origin var5 = OwnerNativeSemanticAuthority.wearOrigin(var0);
                if (var5 != null && var5.thread == Thread.currentThread()) {
                    if (var1 instanceof UseOnContext var6) {
                        var1 = var6.getPlayer();
                    }

                    OwnerStatueWearAdmission.Frame var24 = var5.active;
                    OwnerStatueWearAdmission.Root var7;
                    if (var24 == null) {
                        if (var2 == 9
                            || var5.spent
                            || !var5.loaded
                            || !(var1 instanceof ServerPlayer var8)
                            || !physical(var5, var8, var0)
                            || !OwnerRepairGenerationAuthority.statueTaskObjectPolicy(var0)
                            || !OwnerFullRepairGuard.permits(var0)) {
                            return null;
                        }

                        var5.spent = true;
                        var7 = new OwnerStatueWearAdmission.Root(var5, var8, var0);
                    } else {
                        var7 = var24.root;
                        if (var7.phase != OwnerStatueWearAdmission.Phase.ORIGINAL
                            || var7.actual != var0
                            || var1 != null && var1 != var7.player
                            || var24.depth >= 128) {
                            var7.rejected = true;
                            return null;
                        }
                    }

                    OwnerStatueWearAdmission.Frame var25 = new OwnerStatueWearAdmission.Frame(var7, var24, var2, var3);
                    var5.active = var25;
                    boolean var9 = false;

                    try {
                        if (var24 == null) {
                            var7.outer = var25;
                            synchronized (ACCEPTED_LOCK) {
                                LIVE_ROOTS.put(var0, new WeakReference<>(var7));
                            }

                            OwnerStatueWearAdmission.Reading var26 = new OwnerStatueWearAdmission.Reading(var7, var25, true);
                            CompoundTag var11 = OwnerNativeSemanticAuthority.wearImage(var5, var0, var26);
                            if (var11 == null || !FoodWeaponExpectedTrace.same(var5.expected, NativeExpectedRawFields.observe(var11))) {
                                return null;
                            }

                            var7.before = (CompoundTag)FoodWeaponExpectedTrace.materialize(var5.expected);
                            var7.rules = OwnerStatueTaskPlan.rules(var0);
                            var7.damage = var0.getDamageValue();
                            var7.maximum = var0.getMaxDamage();
                            var7.broken = FoodWeaponLifecycle.isBroken(var0);
                            UUID var12 = FoodWeaponLifecycle.weaponId(var0);
                            if (var12 == null || !FoodWeaponLifecycle.managed(var0) || var7.maximum <= 0) {
                                return null;
                            }

                            try {
                                OwnerStatueTaskService.requireOriginalWearNotRetired(var7.player, var12);
                            } catch (IOException var22) {
                                return null;
                            }

                            int var13 = 0;

                            for (int var30 = 0; var30 < var7.player.getInventory().getContainerSize(); var30++) {
                                if (var12.equals(FoodWeaponLifecycle.weaponId(var7.player.getInventory().getItem(var30)))) {
                                    var13++;
                                }
                            }

                            if (var13 != 1 || !physical(var5, var7.player, var0) || OwnerNativeSemanticAuthority.wearImage(var5, var0, var26) == null) {
                                return null;
                            }

                            var7.plan = OwnerStatueWearPlan.original(var0, var7.before, var7.maximum);

                            try {
                                if (var7.plan != null && !OwnerStatueTaskService.originalWearPlanUnused(var7.player, var12)) {
                                    var7.plan = null;
                                }
                            } catch (IOException var20) {
                                var7.plan = null;
                            }

                            var7.phase = OwnerStatueWearAdmission.Phase.ORIGINAL;
                        }

                        if (var2 >= 7 && var2 <= 9) {
                            var7.wears++;
                        }

                        trace(var25, true, null);
                        var9 = true;
                        return var25;
                    } finally {
                        if (!var9) {
                            var7.rejected = true;
                            var5.active = var24;
                        }
                    }
                } else {
                    return null;
                }
            }
        }
    }

    private static boolean serviceCaller(String var0) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var1 -> var1.filter(var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class)
                        .findFirst()
                        .map(var1x -> var1x.getDeclaringClass() == OwnerStatueTaskService.class && var1x.getMethodName().equals(var0))
                        .orElse(false)
            );
    }

    static OwnerStatueWearAdmission.Accepted validated(OwnerStatueWearAdmission.Terminal var0) {
        if (serviceCaller("recordOriginalWear") && var0 != null && var0.handedOff) {
            OwnerStatueWearAdmission.Root var1 = var0.root;
            if (var1.phase == OwnerStatueWearAdmission.Phase.TERMINAL
                && var1.origin.active == var1.outer
                && !var1.rejected
                && var1.writing == null
                && var1.plan != null
                && var1.plan.taskStartEligible(var0.primary)
                && physical(var1.origin, var1.player, var1.actual)
                && var1.player.level() == var1.level
                && originalCaller(var1.outer.kind, "ftboss$statueWearExitGuard")) {
                CompoundTag var2 = OwnerNativeSemanticAuthority.wearImage(
                    var1.origin, var1.actual, new OwnerStatueWearAdmission.Reading(var1, var1.outer, false)
                );
                return var2 != null && var1.plan.cursor.equals(var2) && var1.rules.equals(OwnerStatueTaskPlan.rules(var1.actual))
                    ? new OwnerStatueWearAdmission.Accepted(var1, var0.primary)
                    : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static void activate(OwnerStatueWearAdmission.Accepted var0, OwnerStatueWearAdmission.Terminal var1) {
        if (serviceCaller("recordOriginalWear") && var0 != null && !var0.spent && var1 != null) {
            OwnerStatueWearAdmission.Root var2 = var1.root;
            CompoundTag var3 = var2.phase == OwnerStatueWearAdmission.Phase.TERMINAL
                ? OwnerNativeSemanticAuthority.wearImage(var2.origin, var2.actual, new OwnerStatueWearAdmission.Reading(var2, var2.outer, false))
                : null;
            if (var2.origin.active == var2.outer
                && !var2.rejected
                && var2.plan != null
                && var2.plan.taskStartEligible(var1.primary)
                && var2.writing == null
                && var0.startEligible()
                && var0.call.equals(var2.call)
                && var0.actual.get() == var2.actual
                && var0.player.get() == var2.player
                && physical(var2.origin, var2.player, var2.actual)
                && var0.expected.equals(var3)
                && originalCaller(var2.outer.kind, "ftboss$statueWearExitGuard")) {
                synchronized (ACCEPTED_LOCK) {
                    if (!var2.rejected && !var2.plan.rejected) {
                        ACCEPTED.put(var2.actual, var0);
                    } else {
                        var0.spent = true;
                    }
                }
            } else {
                var0.spent = true;
            }
        } else {
            throw new IllegalStateException("Detached validated wear publication");
        }
    }

    static boolean hasStartCandidate(ServerPlayer var0, ItemStack var1, int var2) {
        boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == StatueRepairMutation.class && var0xx.getMethodName().equals("commit"))
                        .orElse(false)
            );
        if (var3 && var0 != null && var1 != null) {
            synchronized (ACCEPTED_LOCK) {
                OwnerStatueWearAdmission.Accepted var5 = ACCEPTED.get(var1);
                return var5 != null
                    && !var5.spent
                    && var5.startEligible()
                    && var5.actual.get() == var1
                    && var5.player.get() == var0
                    && var5.inventory.get() == var0.getInventory()
                    && var5.server == var0.getServer()
                    && var5.server.isSameThread()
                    && var5.owner.equals(var0.getUUID())
                    && var5.slot == var2
                    && var2 >= 0
                    && var2 < var0.getInventory().getContainerSize()
                    && var0.getInventory().getItem(var2) == var1
                    && (var2 == 40 ? var0.getOffhandItem() == var1 : var0.getInventory().selected == var2 && var0.getMainHandItem() == var1);
            }
        } else {
            return false;
        }
    }

    static OwnerStatueWearAdmission.Accepted takeForStart(ServerPlayer var0, ItemStack var1, int var2) {
        if (!serviceCaller("prepareStart")) {
            return null;
        } else {
            OwnerStatueWearAdmission.Accepted var3;
            synchronized (ACCEPTED_LOCK) {
                var3 = ACCEPTED.remove(var1);
            }

            if (var3 != null && !var3.spent) {
                var3.spent = true;
                return var3.startEligible()
                        && var3.actual.get() == var1
                        && var3.player.get() == var0
                        && var3.inventory.get() == var0.getInventory()
                        && var3.server == var0.getServer()
                        && var3.server.isSameThread()
                        && var3.owner.equals(var0.getUUID())
                        && var3.slot == var2
                        && var0.getInventory().getItem(var2) == var1
                        && (var2 == 40 ? var0.getOffhandItem() == var1 : var0.getInventory().selected == var2 && var0.getMainHandItem() == var1)
                        && var3.expected.equals(NativeFoundationItemData.image(var1))
                        && var3.rules.equals(OwnerStatueTaskPlan.rules(var1))
                    ? var3
                    : null;
            } else {
                return null;
            }
        }
    }

    public static void custodyChanged(Object var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == NonNullList.class && var0xx.getMethodName().contains("ftboss$statueCustody"))
                        .orElse(false)
            );
        if (var1) {
            OwnerStatueTaskOrigin.custodyChanged(var0);
            OwnerRepairContinuation.custodyChanged(var0);
            synchronized (ACCEPTED_LOCK) {
                LIVE_ROOTS.entrySet().removeIf(var1x -> {
                    OwnerStatueWearAdmission.Root var2 = var1x.getValue().get();
                    if (var2 != null && var2.phase != OwnerStatueWearAdmission.Phase.CLOSED) {
                        Inventory var3 = var2.origin.inventory.get();
                        if (var3 == null || var0 == var3.items || var0 == var3.armor || var0 == var3.offhand) {
                            var2.rejected = true;
                            if (var2.plan != null) {
                                var2.plan.rejected = true;
                            }
                        }

                        return false;
                    } else {
                        return true;
                    }
                });
                ACCEPTED.entrySet().removeIf(var1x -> {
                    OwnerStatueWearAdmission.Accepted var2 = var1x.getValue();
                    Inventory var3 = var2.inventory.get();
                    boolean var4 = var3 == null || var0 == var3.items || var0 == var3.armor || var0 == var3.offhand;
                    if (var4) {
                        var2.spent = true;
                    }

                    return var4;
                });
            }
        }
    }

    private static OwnerStatueWearAdmission.Root activeRoot(ItemStack var0) {
        OwnerStatueWearAdmission.Origin var1 = OwnerNativeSemanticAuthority.wearOrigin(var0);
        return var1 != null && var1.active != null && var1.thread == Thread.currentThread() ? var1.active.root : null;
    }

    private static CompoundTag prefix(OwnerStatueWearAdmission.Root var0) {
        OwnerStatueWearAdmission.Reading var1 = new OwnerStatueWearAdmission.Reading(var0, var0.origin.active, false);
        var1.prefix = true;
        return OwnerNativeSemanticAuthority.wearImage(var0.origin, var0.actual, var1);
    }

    private static boolean writer(int var0) {
        List<StackFrame> var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.filter(
                            var0xx -> var0xx.getDeclaringClass().getNestHost() != OwnerStatueWearAdmission.class
                                    && var0xx.getDeclaringClass() != OwnerStatueWearWrites.class
                        )
                        .toList()
            );
        if (var1.isEmpty()) {
            return false;
        } else {
            StackFrame var2 = (StackFrame)var1.get(0);
            if (var0 == 3) {
                if (!OwnerStatueWearBoundary.writer(1, var2)) {
                    return false;
                } else {
                    StackFrame var6 = var1.stream().filter(var0x -> var0x.getDeclaringClass() != ItemStack.class).findFirst().orElse(null);
                    return var6 != null
                        && (
                            var6.getDeclaringClass() == FoodWeaponSourceLedger.class && var6.getMethodName().equals("nativeAppliedDamage")
                                || var6.getDeclaringClass() == FoodWeaponLifecycle.class
                                    && var6.getMethodName().equals("setInternalDamage")
                                    && var1.stream()
                                        .anyMatch(
                                            var0x -> var0x.getDeclaringClass() == FoodWeaponLifecycle.class
                                                    && var0x.getMethodName().equals("afterDamageChange")
                                        )
                        );
                }
            } else {
                String var3 = switch (var0) {
                    case 1 -> "durabilityCost";
                    case 2 -> "ensureTracked";
                    default -> "";
                    case 4 -> "loseSocket";
                    case 5 -> "loseMaterial";
                    case 6, 7, 8 -> "afterDamageChange";
                    case 9 -> "finishHurtOriginal";
                };
                if (var2.getDeclaringClass() == (var0 == 1 ? WeaponMaterialBuild.class : FoodWeaponLifecycle.class) && var2.getMethodName().equals(var3)) {
                    StackFrame var4 = var1.stream().filter(var0x -> var0x.getDeclaringClass() == ItemStack.class).findFirst().orElse(null);
                    if (var4 == null) {
                        return false;
                    } else {
                        int var5 = var0 == 1 ? 2 : (var0 == 9 ? 5 : 4);
                        return OwnerStatueWearBoundary.writer(var5, var4)
                            || var0 == 2 && (OwnerStatueWearBoundary.writer(3, var4) || OwnerStatueWearBoundary.writer(5, var4));
                    }
                } else {
                    return false;
                }
            }
        }
    }

    public static OwnerStatueWearAdmission.Write beforeWrite(ItemStack var0, int var1, Object var2, String var3, Tag var4) {
        OwnerStatueWearAdmission.Root var5 = activeRoot(var0);
        if (var5 != null && var5.plan != null) {
            try {
                if (writer(var1)
                    && var5.phase == OwnerStatueWearAdmission.Phase.ORIGINAL
                    && var5.writing == null
                    && !var5.rejected
                    && !var5.plan.rejected
                    && var5.plan.cursor.equals(prefix(var5))) {
                    OwnerStatueWearPlan.Step var6 = var5.plan.next(var1, var3, var4);
                    CompoundTag var7 = NativeSourceLedgerItemData.rawCustomDataForObservation(var0);
                    Object var8 = var1 == 8 ? var0 : (var6.path().isEmpty() ? var7 : var7.getCompound(var6.path()));
                    if (var1 == 4) {
                        var8 = ((CompoundTag)var8).getList(var6.key(), 10);
                    }

                    if (var1 == 3) {
                        var2 = var0;
                    }

                    if (var1 != 3 && var8 != var2) {
                        throw new IllegalStateException("Wear writer receiver differs from original storage");
                    } else {
                        Object var9 = var1 == 3 ? var7 : var2;
                        OwnerStatueWearAdmission.Write var10 = new OwnerStatueWearAdmission.Write(var5, var6, var2, var9);
                        var5.writing = var10;
                        return var10;
                    }
                } else {
                    throw new IllegalStateException("Detached original prefix write");
                }
            } catch (Throwable var11) {
                var5.plan.rejected = true;
                return null;
            }
        } else {
            return null;
        }
    }

    public static void afterWrite(OwnerStatueWearAdmission.Write var0, Throwable var1) {
        if (var0 != null) {
            OwnerStatueWearAdmission.Root var2 = var0.root;

            try {
                boolean var3 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(var0x -> var0x.skip(1L).findFirst().map(var0xx -> var0xx.getDeclaringClass() == OwnerStatueWearWrites.class).orElse(false));
                if (!var3 || var0.closed || var2.writing != var0 || var2.phase != OwnerStatueWearAdmission.Phase.ORIGINAL) {
                    throw new IllegalStateException("Wear write token replay");
                }

                CompoundTag var4 = prefix(var2);
                if (var0.step.after().equals(var4)) {
                    var2.plan.advanced(var0.step);
                    if (var0.step.site() == 8 && var1 == null) {
                        var2.plan.swallowedRollback = true;
                    }
                } else if (var1 == null || !var0.before.equals(var4)) {
                    throw new IllegalStateException("Unplanned partial original mutation");
                }

                if (!OwnerFullRepairGuard.watchWearPrefix(var2.actual)) {
                    throw new IllegalStateException("Original alias watch incomplete");
                }
            } catch (Throwable var8) {
                var2.plan.rejected = true;
            } finally {
                var0.closed = true;
                if (var2.writing == var0) {
                    var2.writing = null;
                }
            }
        }
    }

    static boolean watching(ItemStack var0) {
        boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == OwnerFullRepairGuard.class && var0xx.getMethodName().equals("watchWearPrefix"))
                        .orElse(false)
            );
        OwnerStatueWearAdmission.Root var2 = var1 ? activeRoot(var0) : null;
        return var2 != null && var2.phase == OwnerStatueWearAdmission.Phase.ORIGINAL && var2.writing != null && var2.plan != null && !var2.plan.rejected;
    }

    static void mutationNoticed(Object var0, Object var1, int var2) {
        OwnerStatueTaskOrigin.changed(var0, var1, var2);
        OwnerRepairContinuation.carryMutation(var0, var1, var2);
        if (var0 instanceof ItemStack var3) {
            synchronized (ACCEPTED_LOCK) {
                OwnerStatueWearAdmission.Accepted var5 = ACCEPTED.remove(var3);
                if (var5 != null) {
                    var5.spent = true;
                }
            }

            OwnerStatueWearAdmission.Root var11 = activeRoot(var3);
            if (var11 == null) {
                synchronized (ACCEPTED_LOCK) {
                    WeakReference var14 = LIVE_ROOTS.get(var3);
                    OwnerStatueWearAdmission.Root var15 = var14 == null ? null : (OwnerStatueWearAdmission.Root)var14.get();
                    if (var15 != null) {
                        var15.rejected = true;
                    }
                }
            } else if (var11.plan != null && var11.phase != OwnerStatueWearAdmission.Phase.CLOSED) {
                if (var11.phase != OwnerStatueWearAdmission.Phase.ORIGINAL) {
                    var11.plan.rejected = true;
                } else {
                    OwnerStatueWearAdmission.Write var12 = var11.writing;
                    if (var12 == null
                        || var12.closed
                        || var1 != var12.receiver && var1 != var12.storage
                        || var2 != 20 && var2 != 21
                        || !StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                            .walk(
                                var0x -> {
                                    java.util.List<java.lang.StackWalker.StackFrame> var1x = var0x.takeWhile(var0xx -> var0xx.getDeclaringClass() != OwnerStatueWearWrites.class).toList();
                                    return var1x.stream()
                                        .allMatch(
                                            var0xx -> var0xx.getDeclaringClass().getNestHost() == OwnerStatueWearAdmission.class
                                                    || var0xx.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class
                                                    || var0xx.getDeclaringClass() == ItemStack.class
                                                    || var0xx.getDeclaringClass() == Item.class
                                                    || var0xx.getDeclaringClass() == CompoundTag.class
                                                    || var0xx.getDeclaringClass() == ListTag.class
                                        );
                                }
                            )) {
                        if (var1 == var0 && (var2 == 20 || var2 == 21)) {
                            OwnerStatueWearAdmission.Frame var6 = var11.origin.active;
                            boolean var7 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                                .walk(
                                    var1x -> {
                                        java.util.List<java.lang.StackWalker.StackFrame> var2x = var1x.filter(var0xx -> var0xx.getDeclaringClass() == ItemStack.class).limit(2L).toList();
                                        return var2x.size() == 2
                                            && (
                                                OwnerStatueWearBoundary.writer(6, (StackFrame)var2x.get(0))
                                                    || OwnerStatueWearBoundary.writer(7, (StackFrame)var2x.get(0))
                                            )
                                            && OwnerStatueWearBoundary.original(
                                                var6.kind, ((StackFrame)var2x.get(1)).getMethodName(), ((StackFrame)var2x.get(1)).getDescriptor()
                                            );
                                    }
                                );
                            if (var7) {
                                return;
                            }
                        }

                        if (var1 != null || var2 != 1) {
                            var11.plan.rejected = true;
                        }
                    }
                }
            }
        }
    }

    static void cost(ItemStack var0, int var1) {
        OwnerStatueWearAdmission.Root var2 = activeRoot(var0);
        if (var2 != null && var2.plan != null) {
            try {
                if (!writer(1) || var2.origin.active.kind != 8 || var2.origin.active.requested != var1 || !var2.plan.cursor.equals(prefix(var2))) {
                    throw new IllegalStateException("Detached material cost");
                }

                var2.plan.cost(var1);
            } catch (Throwable var4) {
                var2.plan.rejected = true;
            }
        }
    }

    static void damage(ItemStack var0, int var1, int var2) {
        OwnerStatueWearAdmission.Root var3 = activeRoot(var0);
        if (var3 != null && var3.plan != null) {
            try {
                boolean var4 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0x -> var0x.skip(1L)
                                .findFirst()
                                .map(
                                    var0xx -> var0xx.getDeclaringClass() == FoodWeaponSourceLedger.class
                                            && var0xx.getMethodName().equals("nativeAppliedDamage")
                                )
                                .orElse(false)
                    );
                if (!var4 || var3.origin.active.kind != 8 || !var3.plan.cursor.equals(prefix(var3))) {
                    throw new IllegalStateException("Detached applied damage");
                }

                var3.plan.damage(var1, var2);
            } catch (Throwable var5) {
                var3.plan.rejected = true;
            }
        }
    }

    static void rollback(ItemStack var0, CompoundTag var1, Throwable var2) {
        OwnerStatueWearAdmission.Root var3 = activeRoot(var0);
        if (var3 != null && var3.plan != null) {
            try {
                if (!writer(8) || !var3.plan.cursor.equals(prefix(var3))) {
                    throw new IllegalStateException("Detached swallowed rollback");
                }

                var3.plan.rollback(var1, var2);
            } catch (Throwable var5) {
                var3.plan.rejected = true;
            }
        }
    }

    static void finish(ItemStack var0, ServerPlayer var1, boolean var2) {
        OwnerStatueWearAdmission.Root var3 = activeRoot(var0);
        if (var3 != null && var3.plan != null) {
            try {
                boolean var4 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0x -> var0x.filter(
                                    var0xx -> var0xx.getDeclaringClass() != OwnerStatueWearAdmission.class
                                            && var0xx.getDeclaringClass() != OwnerStatueWearWrites.class
                                )
                                .findFirst()
                                .map(var0xx -> var0xx.getDeclaringClass() == FoodWeaponLifecycle.class && var0xx.getMethodName().equals("finishHurt"))
                                .orElse(false)
                    );
                boolean var5 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() == ItemStack.class)
                                .findFirst()
                                .map(var0xx -> OwnerStatueWearBoundary.writer(5, var0xx))
                                .orElse(false)
                    );
                if (!var4 || !var5 || var3.origin.active.kind != 8 || var1 != var3.player || !var3.plan.cursor.equals(prefix(var3))) {
                    throw new IllegalStateException("Detached original hurt finish");
                }

                if (var2) {
                    var3.plan.finished();
                } else {
                    var3.plan.finish(true);
                }
            } catch (Throwable var6) {
                var3.plan.rejected = true;
            }
        }
    }

    private static void trace(OwnerStatueWearAdmission.Frame var0, boolean var1, Throwable var2) {
        OwnerStatueWearAdmission.Root var3 = var0.root;
        if (var3.trace.size() >= 256) {
            var3.rejected = true;
        } else {
            CompoundTag var4 = new CompoundTag();
            var4.putInt("Method", var0.kind);
            var4.putInt("Requested", var0.requested);
            var4.putInt("Depth", var0.depth);
            var4.putBoolean("Entered", var1);
            var4.putBoolean("ReturnedNormally", !var1 && var2 == null);
            if (var2 != null) {
                var4.putString("PrimaryClass", var2.getClass().getName());
            }

            var3.trace.add(var4);
        }
    }

    public static void exited(OwnerStatueWearAdmission.Frame var0, Throwable var1) {
        if (var0 != null && var0.nativeOperation != null) {
            OwnerNativeSemanticAuthority.nativeOperationReturned(var0.nativeOperation, var1);
            var0.closed = true;
            return;
        }
        if (var0 != null && var0.continuation != null) {
            OwnerRepairContinuation.exited(var0.continuation, var1);
            var0.closed = true;
        } else if (var0 != null && originalCaller(var0.kind, "ftboss$statueWearExitGuard") && !var0.closed && var0.root.origin.thread == Thread.currentThread()
            )
         {
            OwnerStatueWearAdmission.Root var2 = var0.root;
            OwnerStatueWearAdmission.Origin var3 = var2.origin;
            if (var3.active != var0) {
                var2.rejected = true;
            } else {
                try {
                    trace(var0, false, var1);
                    if (var0.previous != null) {
                        return;
                    }

                    if (var2.wears != 0) {
                        var2.phase = OwnerStatueWearAdmission.Phase.TERMINAL;
                        CompoundTag var24 = new CompoundTag();
                        var24.putInt("Schema", 1);
                        var24.putUUID("Call", var2.call);
                        var24.putUUID("OriginalLoad", var3.load);
                        var24.putUUID("Owner", var3.owner);
                        var24.putInt("Slot", var3.slot);
                        var24.putString("OriginalDimension", var2.dimension);
                        var24.putString("DimensionAtTerminalObservation", var2.player.level().dimension().location().toString());
                        var24.putString("Authority", "ORIGINAL_LOAD_CUSTODY_BOUND_WEAR_EVIDENCE_ONLY_NOT_TASK_OR_GENERATION_AUTHORITY");
                        var24.putBoolean("ReturnedNormally", var1 == null);
                        if (var1 != null) {
                            var24.putString("PrimaryClass", var1.getClass().getName());
                        }

                        var24.putBoolean("CustodyAtTerminal", physical(var3, var2.player, var2.actual) && var2.player.level() == var2.level);
                        var24.putBoolean("RejectedObservation", var2.rejected);
                        var24.putInt("WearMethodEntriesNotVerifiedDamage", var2.wears);
                        var24.putBoolean("MutationPlanVerified", false);
                        var24.putBoolean("CreatesTaskOrGeneration", false);
                        var24.put("AcceptedOriginalLoadBefore", var2.before.copy());
                        var24.putString("RulesBefore", var2.rules);
                        var24.putInt("DamageBefore", var2.damage);
                        var24.putInt("MaximumBefore", var2.maximum);
                        var24.putBoolean("BrokenBefore", var2.broken);
                        var24.put("OriginalCallTrace", var2.trace.copy());

                        try {
                            CompoundTag var5 = OwnerNativeSemanticAuthority.wearImage(
                                var3, var2.actual, new OwnerStatueWearAdmission.Reading(var2, var0, false)
                            );
                            if (var5 == null) {
                                throw new IllegalStateException("Original wear final fields unavailable");
                            }

                            var24.put("ObservedFinalImageNotExpected", var5);
                            var24.putBoolean("ObservedImageChanged", !var2.before.equals(var5));
                            var24.putInt("CountAfter", var2.actual.getCount());
                            var24.putInt("DamageAfter", var2.actual.getDamageValue());
                            var24.putInt("MaximumAfter", var2.actual.getMaxDamage());
                            var24.putBoolean("BrokenAfter", FoodWeaponLifecycle.isBroken(var2.actual));
                            var24.putString("RulesAfterObservation", OwnerStatueTaskPlan.rules(var2.actual));
                        } catch (Throwable var22) {
                            var24.putString("FinalObservationFailureClass", var22.getClass().getName());
                        }

                        try {
                            OwnerStatueTaskService.recordOriginalWear(new OwnerStatueWearAdmission.Terminal(var2, var1, var24));
                        } catch (IOException var20) {
                            OwnerStatueTaskService.unavailable(var2.player, var20);
                        } catch (LinkageError | RuntimeException var21) {
                            OwnerStatueTaskService.unavailable(var2.player, new IOException("Original wear evidence checkpoint failed", var21));
                        }

                        return;
                    }

                    var2.phase = OwnerStatueWearAdmission.Phase.CAPTURING;
                    if (var1 == null && !var2.rejected) {
                        CompoundTag var4 = OwnerNativeSemanticAuthority.wearImage(var3, var2.actual, new OwnerStatueWearAdmission.Reading(var2, var0, true));
                        if (var4 != null && var2.before.equals(var4)) {
                            var3.spent = false;
                        }
                    }
                } finally {
                    var0.closed = true;
                    var3.active = var0.previous;
                    if (var0.previous == null) {
                        var2.phase = OwnerStatueWearAdmission.Phase.CLOSED;
                        synchronized (ACCEPTED_LOCK) {
                            LIVE_ROOTS.remove(var2.actual);
                        }
                    }
                }
            }
        }
    }

    static final class Accepted {
        private final WeakReference<ItemStack> actual;
        private final WeakReference<ServerPlayer> player;
        private final WeakReference<Inventory> inventory;
        private final MinecraftServer server;
        private final UUID owner;
        private final UUID call;
        private final UUID load;
        private final UUID weapon;
        private final int slot;
        private final int level;
        private final int maximum;
        private final String profile;
        private final String rules;
        private final CompoundTag expected;
        private final boolean normal;
        private final String primaryClass;
        private final boolean rollback;
        private final boolean rollbackAttempted;
        private volatile boolean spent;
        private boolean taskOriginTransferred;

        private Accepted(OwnerStatueWearAdmission.Root var1, Throwable var2) {
            this.actual = new WeakReference<>(var1.actual);
            this.player = new WeakReference<>(var1.player);
            this.inventory = new WeakReference<>(var1.player.getInventory());
            this.server = var1.origin.server;
            this.owner = var1.origin.owner;
            this.call = var1.call;
            this.load = var1.origin.load;
            this.weapon = NativeRepairStackImage.data(var1.plan.cursor).getCompound("FTBossWeaponLifecycle").getUUID("Weapon");
            this.slot = var1.origin.slot;
            this.level = var1.plan.level;
            this.maximum = var1.plan.expectedMaximum();
            this.profile = var1.plan.profile;
            this.rules = var1.rules;
            this.expected = var1.plan.cursor.copy();
            this.normal = var2 == null;
            this.primaryClass = var2 == null ? "" : var2.getClass().getName();
            this.rollback = var1.plan.swallowedRollback;
            this.rollbackAttempted = var1.plan.rollbackAttempted;
        }

        boolean transferToTaskOrigin(ServerPlayer var1, ItemStack var2, int var3) {
            boolean var4 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.skip(1L)
                            .findFirst()
                            .map(var0x -> var0x.getDeclaringClass() == OwnerStatueTaskOrigin.class && var0x.getMethodName().equals("reserve"))
                            .orElse(false)
                );
            if (var4
                && !this.taskOriginTransferred
                && this.spent
                && this.startEligible()
                && this.actual.get() == var2
                && this.player.get() == var1
                && this.inventory.get() == var1.getInventory()
                && this.server == var1.getServer()
                && this.server.isSameThread()
                && this.owner.equals(var1.getUUID())
                && this.slot == var3
                && var1.getInventory().getItem(var3) == var2
                && this.expected.equals(NativeFoundationItemData.image(var2))
                && this.rules.equals(OwnerStatueTaskPlan.rules(var2))) {
                this.taskOriginTransferred = true;
                return true;
            } else {
                return false;
            }
        }

        private boolean startEligible() {
            return this.normal && this.primaryClass.isEmpty() && !this.rollbackAttempted && !this.rollback;
        }

        CompoundTag record() {
            if (!this.startEligible()) {
                throw new IllegalStateException("Wear evidence is not normal no-rollback START authority");
            } else {
                CompoundTag var1 = new CompoundTag();
                var1.putUUID("Call", this.call);
                var1.putUUID("OriginalLoad", this.load);
                var1.putUUID("Owner", this.owner);
                var1.putUUID("Weapon", this.weapon);
                var1.putInt("Slot", this.slot);
                var1.putInt("Level", this.level);
                var1.putInt("Maximum", this.maximum);
                var1.putString("Profile", this.profile);
                var1.putString("Rules", this.rules);
                var1.putString("Status", "AVAILABLE_LIVE_IDENTITY_ONLY");
                var1.putString("Authority", "INDEPENDENT_ORIGINAL_WRITER_PREFIX_NOT_OBSERVATION");
                var1.putBoolean("ReturnedNormally", this.normal);
                var1.putString("PrimaryClass", this.primaryClass);
                var1.putBoolean("OriginalRollbackEntered", this.rollbackAttempted);
                var1.putBoolean("SwallowedOriginalRollbackReturned", this.rollback);
                var1.put("Expected", this.expected.copy());
                return var1;
            }
        }
    }

    public static final class Frame {
        private final OwnerNativeSemanticAuthority.NativeWear nativeOperation;
        private final OwnerStatueWearAdmission.Root root;
        private final OwnerStatueWearAdmission.Frame previous;
        private final int kind;
        private final int requested;
        private final int depth;
        private final OwnerRepairContinuation.Entry continuation;
        private boolean closed;

        private Frame(OwnerRepairContinuation.Entry var1) {
            this.nativeOperation = null;
            this.root = null;
            this.previous = null;
            this.kind = 0;
            this.requested = 0;
            this.depth = 0;
            this.continuation = var1;
        }

        private Frame(OwnerStatueWearAdmission.Root var1, OwnerStatueWearAdmission.Frame var2, int var3, int var4) {
            this.nativeOperation = null;
            this.root = var1;
            this.previous = var2;
            this.kind = var3;
            this.requested = var4;
            this.continuation = null;
            this.depth = var2 == null ? 1 : var2.depth + 1;
        }

        private Frame(OwnerNativeSemanticAuthority.NativeWear nativeOperation) {
            this.nativeOperation = nativeOperation;
            this.root = null;
            this.previous = null;
            this.kind = 1;
            this.requested = 0;
            this.depth = 1;
            this.continuation = null;
        }
    }

    public static final class Load {
        private final Inventory inventory;
        private final ServerPlayer player;
        private final MinecraftServer server;
        private final UUID owner;
        private final UUID identity = UUID.randomUUID();
        private final Thread thread = Thread.currentThread();
        private final IdentityHashMap<CompoundTag, Value> inputs = new IdentityHashMap<>();
        private final List<OwnerStatueWearAdmission.Origin> origins = new ArrayList<>();
        private boolean closed;
        private final Map<Integer, ItemStack> saveItems = new LinkedHashMap<>();
        private final Map<Integer, CompoundTag> saveExpected = new LinkedHashMap<>();
        private final Map<Integer, OwnerLineageRevocationStore.Lease> saveLeases = new LinkedHashMap<>();
        private final Map<Integer, Object> saveGenerations = new LinkedHashMap<>();
        private final Map<Integer, CompoundTag> saveTickets = new LinkedHashMap<>();
        private boolean saveRejected;
        private boolean saveSkip;
        private boolean saveReplaced;
        private CompoundTag saveSerializedSource;
        private CompoundTag saveSerialized;
        private OwnerLineageRevocationStore saveStore;
        private UUID saveDocumentOwner;
        private CompoundTag saveDocument;
        private Path saveTarget;
        private final Map<Integer, ItemStack> reloadItems = new LinkedHashMap<>();
        private final Map<Integer, CompoundTag> reloadSources = new LinkedHashMap<>();
        private final Map<Integer, CompoundTag> reloadExpected = new LinkedHashMap<>();
        private final Map<Integer, CompoundTag> reloadTickets = new LinkedHashMap<>();
        private final Map<Integer, CompoundTag> reloadRecords = new LinkedHashMap<>();
        private final Map<Integer, NativeItemCapabilityObservation> reloadCapabilities = new LinkedHashMap<>();
        private final Map<Integer, OwnerLineageRevocationStore.Lease> reloadIssued = new LinkedHashMap<>();
        private OwnerLineageRevocationStore reloadStore;
        private boolean reloadRejected;
        private boolean reloadFinishing;

        private Load(Inventory var1, ServerPlayer var2, MinecraftServer var3) {
            this.inventory = var1;
            this.player = var2;
            this.server = var3;
            this.owner = var2.getUUID();
        }
    }

    static final class Origin {
        final OwnerNativeSemanticAuthority.WeaponInput input;
        final Value expected;
        private final WeakReference<ItemStack> actual;
        private final WeakReference<ServerPlayer> player;
        private final WeakReference<Inventory> inventory;
        private final MinecraftServer server;
        private final UUID owner;
        private final UUID load;
        private final int slot;
        private final Thread thread = Thread.currentThread();
        private boolean loaded;
        private boolean spent;
        private OwnerStatueWearAdmission.Frame active;

        private Origin(OwnerStatueWearAdmission.Load var1, ItemStack var2, OwnerNativeSemanticAuthority.WeaponInput var3, Value var4, int var5) {
            this.input = var3;
            this.expected = var4;
            this.actual = new WeakReference<>(var2);
            this.player = new WeakReference<>(var1.player);
            this.inventory = new WeakReference<>(var1.inventory);
            this.server = var1.server;
            this.owner = var1.owner;
            this.load = var1.identity;
            this.slot = var5;
        }
    }

    private static enum Phase {
        CAPTURING,
        ORIGINAL,
        TERMINAL,
        CLOSED;
    }

    static final class Reading {
        private final OwnerStatueWearAdmission.Root root;
        private final OwnerStatueWearAdmission.Frame frame;
        private final boolean before;
        private boolean prefix;

        private Reading(OwnerStatueWearAdmission.Root var1, OwnerStatueWearAdmission.Frame var2, boolean var3) {
            this.root = var1;
            this.frame = var2;
            this.before = var3;
        }

        boolean live(OwnerNativeSemanticAuthority.WeaponInput var1, ItemStack var2) {
            OwnerStatueWearAdmission.Origin var3 = this.root.origin;
            return var3.input == var1
                && var3.actual.get() == var2
                && this.root.actual == var2
                && var3.active == this.frame
                && !this.frame.closed
                && var3.thread == Thread.currentThread()
                && var3.server.isSameThread()
                && this.root.player.getServer() == var3.server
                && var3.owner.equals(this.root.player.getUUID())
                && (
                    this.before
                        ? this.root.phase == OwnerStatueWearAdmission.Phase.CAPTURING
                            && this.root.player.level() == this.root.level
                            && OwnerStatueWearAdmission.physical(var3, this.root.player, var2)
                            && OwnerFullRepairGuard.permits(var2)
                        : (
                            this.prefix
                                ? this.root.phase == OwnerStatueWearAdmission.Phase.ORIGINAL
                                    && this.root.player.level() == this.root.level
                                    && OwnerStatueWearAdmission.physical(var3, this.root.player, var2)
                                : this.root.phase == OwnerStatueWearAdmission.Phase.TERMINAL
                        )
                )
                && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                    .walk(
                        var1x -> var1x.anyMatch(
                                var1xx -> var1xx.getDeclaringClass() == ItemStack.class
                                        && OwnerStatueWearBoundary.original(this.root.outer.kind, var1xx.getMethodName(), var1xx.getDescriptor())
                            )
                    );
        }

        boolean before() {
            return this.before;
        }
    }

    private static final class Root {
        private final OwnerStatueWearAdmission.Origin origin;
        private final ServerPlayer player;
        private final ItemStack actual;
        private final Object level;
        private final String dimension;
        private final UUID call = UUID.randomUUID();
        private final ListTag trace = new ListTag();
        private OwnerStatueWearAdmission.Frame outer;
        private OwnerStatueWearAdmission.Phase phase = OwnerStatueWearAdmission.Phase.CAPTURING;
        private CompoundTag before;
        private String rules;
        private int damage;
        private int maximum;
        private boolean broken;
        private int wears;
        private volatile boolean rejected;
        private OwnerStatueWearPlan plan;
        private OwnerStatueWearAdmission.Write writing;

        private Root(OwnerStatueWearAdmission.Origin var1, ServerPlayer var2, ItemStack var3) {
            this.origin = var1;
            this.player = var2;
            this.actual = var3;
            this.level = var2.level();
            this.dimension = var2.level().dimension().location().toString();
        }
    }

    static final class Terminal {
        private final OwnerStatueWearAdmission.Root root;
        private final Throwable primary;
        private final CompoundTag evidence;
        private boolean handedOff;

        private Terminal(OwnerStatueWearAdmission.Root var1, Throwable var2, CompoundTag var3) {
            this.root = var1;
            this.primary = var2;
            this.evidence = var3;
        }

        ServerPlayer player() {
            return this.root.player;
        }

        UUID identity() {
            return this.root.call;
        }

        CompoundTag take() {
            boolean var1 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.skip(1L)
                            .findFirst()
                            .map(var0x -> var0x.getDeclaringClass() == OwnerStatueTaskService.class && var0x.getMethodName().equals("recordOriginalWear"))
                            .orElse(false)
                );
            if (var1
                && !this.handedOff
                && this.root.phase == OwnerStatueWearAdmission.Phase.TERMINAL
                && this.root.origin.active == this.root.outer
                && this.root.origin.thread == Thread.currentThread()
                && this.root.player.getServer() == this.root.origin.server
                && this.root.origin.owner.equals(this.root.player.getUUID())
                && this.root.origin.server.isSameThread()
                && OwnerStatueWearAdmission.originalCaller(this.root.outer.kind, "ftboss$statueWearExitGuard")) {
                this.handedOff = true;
                if (this.primary == null != this.evidence.getBoolean("ReturnedNormally")) {
                    throw new IllegalStateException("Wear primary outcome mismatch");
                } else {
                    return this.evidence.copy();
                }
            } else {
                throw new IllegalStateException("Detached original wear terminal");
            }
        }
    }

    public static final class Write {
        private final OwnerStatueWearAdmission.Root root;
        private final OwnerStatueWearPlan.Step step;
        private final Object receiver;
        private final Object storage;
        private final CompoundTag before;
        private boolean closed;

        private Write(OwnerStatueWearAdmission.Root var1, OwnerStatueWearPlan.Step var2, Object var3, Object var4) {
            this.root = var1;
            this.step = var2;
            this.receiver = var3;
            this.storage = var4;
            this.before = var1.plan.cursor.copy();
        }
    }
}
