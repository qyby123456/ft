package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class NativeFoodWeaponWear {
    private static final String KEY = "NativeWear";
    private static final int VERSION = 1;
    private static final int[] REMAINING = new int[]{50, 35, 20, 10};
    private static final ThreadLocal<ItemStack> REPAIR_WRITE = new ThreadLocal<>();
    private static final Map<ItemStack, NativeFoodWeaponWear.Claim> CLAIMS = new WeakHashMap<>();

    private NativeFoodWeaponWear() {
    }

    static CompoundTag persistenceImage(ServerPlayer player, ItemStack stack) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerStatueWearAdmission.class)
            throw new SecurityException("Original player-save observation required");
        OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(stack);
        if (player == null || player.getServer() == null || !player.getServer().isSameThread()
            || !OwnerRepairGenerationAuthority.nativeTaskObjectPolicy(stack) || lease == null
            || !lease.owner.equals(player.getUUID()) || !lease.weapon.equals(FoodWeaponLifecycle.weaponId(stack))
            || !OwnerRepairGenerationAuthority.repaired(stack, lease) || !OwnerLineageRevocationStore.liveLease(lease)
            || !OwnerFullRepairGuard.permits(stack)) return null;
        OwnerNativeSemanticAuthority.WeaponInput input = OwnerNativeSemanticAuthority.nativeRepairInput(stack);
        CompoundTag expected = OwnerNativeSemanticAuthority.nativeRepairImage(input, stack);
        if (expected == null) return null;
        UUID identity = FoodWeaponData.read(stack, "FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
        CompoundTag state = authority(player.getServer()).weapons.getCompound(identity.toString());
        return state.getInt("Damage") == stack.getDamageValue() && state.getInt("Maximum") == stack.getMaxDamage() ? expected : null;
    }

    static OwnerLineageRevocationStore persistenceStore(ServerPlayer player) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerStatueWearAdmission.class)
            throw new SecurityException("Original player persistence required");
        return FoodWeaponSourceLedger.nativeRepairStore(player);
    }

    static CompoundTag loadedPersistenceImage(ServerPlayer player, ItemStack stack, CompoundTag source, FoodWeaponExpectedTrace.Value quoted) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerStatueWearAdmission.class)
            throw new SecurityException("Original inventory load required");
        if (player == null || player.getServer() == null || !player.getServer().isSameThread()
            || !OwnerRepairGenerationAuthority.nativeTaskObjectPolicy(stack)) return null;
        OwnerNativeSemanticAuthority.WeaponInput input = OwnerNativeSemanticAuthority.nativeRepairInput(stack);
        if (!OwnerNativeSemanticAuthority.nativeReloadSource(input, stack, source, quoted)) return null;
        CompoundTag expected = OwnerNativeSemanticAuthority.nativeRepairImage(input, stack);
        if (expected == null) return null;
        CompoundTag lifecycle = expected.getCompound("components").getCompound("minecraft:custom_data").getCompound("FTBossWeaponLifecycle");
        CompoundTag nativeWear = lifecycle.getCompound("NativeWear");
        if (!lifecycle.hasUUID("Weapon") || !nativeWear.hasUUID("Identity")) return null;
        CompoundTag state = authority(player.getServer()).weapons.getCompound(nativeWear.getUUID("Identity").toString());
        return state.getInt("Damage") == stack.getDamageValue() && state.getInt("Maximum") == stack.getMaxDamage() ? expected : null;
    }

    static NativeItemCapabilityObservation loadedPersistenceCapabilities(ItemStack stack) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerStatueWearAdmission.class)
            throw new SecurityException("Original inventory load capability observation required");
        return NativeItemCapabilityObservation.nativeRepairCapture(stack);
    }

    public static boolean eligible(ItemStack var0) {
        if (!nativeDamageable(var0)) {
            return false;
        } else {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
            CompoundTag var2 = var1 == null ? null : var1.getCompound("NativeWear");
            return var2 != null
                && var2.getBoolean("Managed")
                && var2.getInt("Version") == 1
                && var2.hasUUID("Identity")
                && FoodWeaponEvolution.isEvolutionCandidate(var0);
        }
    }

    static boolean nativeDamageable(ItemStack var0) {
        return NativeLifecycleItemData.nativeDamageable(var0);
    }

    public static boolean statueRepairWrite(ItemStack var0) {
        return REPAIR_WRITE.get() == var0 && eligible(var0);
    }

    private static MinecraftServer server() {
        MinecraftServer var0 = ServerLifecycleHooks.getCurrentServer();
        return var0 != null && var0.isSameThread() ? var0 : null;
    }

    private static NativeFoodWeaponWear.Authority authority(MinecraftServer var0) {
        return (NativeFoodWeaponWear.Authority)var0.overworld()
            .getDataStorage()
            .computeIfAbsent(new SavedData.Factory<>(NativeFoodWeaponWear.Authority::new, (tag, registries) -> NativeFoodWeaponWear.Authority.load(tag), null), "ftboss_native_weapon_wear");
    }

    private static void checkpoint(MinecraftServer var0, NativeFoodWeaponWear.Authority var1) {
        if (var0.isSameThread()
            && !Thread.holdsLock(OwnerFullRepairGuard.class)
            && !Thread.holdsLock(FoodWeaponSourceLedger.class)
            && !Thread.holdsLock(OwnerFieldExpectedAuthority.class)) {
            var1.setDirty();
            var0.overworld().getDataStorage().save();
            net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
            Path var2 = var0.getWorldPath(LevelResource.ROOT).resolve("data").resolve("ftboss_native_weapon_wear.dat");

            try {
                if (Files.isSymbolicLink(var2)
                    || !Files.isRegularFile(var2)
                    || !NbtIo.readCompressed(var2, NbtAccounter.unlimitedHeap()).getCompound("data").equals(var1.save(new CompoundTag(), var0.registryAccess()))) {
                    throw new IllegalStateException("Native wear authority checkpoint did not persist");
                }
            } catch (IOException var4) {
                throw new IllegalStateException("Native wear checkpoint unavailable", var4);
            }
        } else {
            throw new IllegalStateException("Native wear checkpoint outside server boundary");
        }
    }

    private static ServerPlayer holder(ItemStack var0) {
        MinecraftServer var1 = server();
        if (var1 == null) {
            return null;
        } else {
            ServerPlayer var2 = null;

            for (ServerPlayer var4 : var1.getPlayerList().getPlayers()) {
                for (int var5 = 0; var5 < var4.getInventory().getContainerSize(); var5++) {
                    if (var4.getInventory().getItem(var5) == var0) {
                        if (var2 != null) {
                            return null;
                        }

                        var2 = var4;
                    }
                }
            }

            return var2;
        }
    }

    public static void manage(ServerPlayer var0, ItemStack var1) {
        if (var0 != null
            && var0.getServer() != null
            && var0.getServer().isSameThread()
            && !var0.level().isClientSide
            && nativeDamageable(var1)
            && FoodWeaponEvolution.isEvolutionCandidate(var1)
            && holder(var1) == var0) {
            CompoundTag var2 = FoodWeaponData.read(var1, "FTBossWeaponLifecycle");
            CompoundTag nativeBefore = var2 == null ? null : var2.copy();
            if (var2 == null) {
                var2 = new CompoundTag();
            }
            CompoundTag var3 = var2.getCompound("NativeWear");
            if (var3.isEmpty()) {
                var3.putInt("Version", 1);
                var3.putBoolean("Managed", true);
                var3.putUUID("Identity", UUID.randomUUID());
                var3.putInt("Mask", reached(var1.getDamageValue(), var1.getMaxDamage()));
                var2.put("NativeWear", var3);
            }

            NativeLifecycleItemData.commit(var1, "FTBossWeaponLifecycle", nativeBefore, var2);
            nativeBefore = var2.copy();
            if (eligible(var1)) {
                if (!var2.hasUUID("Weapon")) {
                    var2.putUUID("Weapon", UUID.randomUUID());
                }

                if (var2.getString("RepairProfile").isEmpty()) {
                    var2.putString("RepairProfile", NativeFoodWeaponEvolutionRead.repairProfileId(var1));
                }

                NativeLifecycleItemData.commit(var1, "FTBossWeaponLifecycle", nativeBefore, var2);
                NativeFoodWeaponWear.Authority var4 = authority(var0.getServer());
                String var5 = var3.getUUID("Identity").toString();
                if (!var4.weapons.contains(var5)) {
                    CompoundTag var6 = new CompoundTag();
                    var6.putInt("Mask", var3.getInt("Mask") & 15);
                    var6.putInt("Damage", var1.getDamageValue());
                    var6.putInt("Maximum", var1.getMaxDamage());
                    var4.weapons.put(var5, var6);
                    checkpoint(var0.getServer(), var4);
                }
            }
        }
    }

    static void manageBound(ItemStack var0) {
        ServerPlayer var1 = holder(var0);
        if (var1 != null) {
            manage(var1, var0);
        }
    }

    static void componentsChanged(ItemStack var0) {
        if (eligible(var0)) {
            ServerPlayer var1 = holder(var0);
            if (var1 != null) {
                NativeFoodWeaponWear.Authority var2 = authority(var1.getServer());
                CompoundTag var3 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle").getCompound("NativeWear");
                CompoundTag var4 = var2.weapons.getCompound(var3.getUUID("Identity").toString());
                if (!var4.isEmpty()) {
                    var4.putInt("Damage", var0.getDamageValue());
                    var4.putInt("Maximum", var0.getMaxDamage());
                    checkpoint(var1.getServer(), var2);
                }
            }
        }
    }

    private static int reached(int var0, int var1) {
        if (var1 <= 0) {
            return 0;
        } else {
            long var2 = Math.max(0L, (long)var1 - (long)Math.max(0, var0));
            int var4 = 0;

            for (int var5 = 0; var5 < REMAINING.length; var5++) {
                if (var2 * 100L <= (long)var1 * (long)REMAINING[var5]) {
                    var4 |= 1 << var5;
                }
            }

            return var4;
        }
    }

    public static void damageChanged(ItemStack var0) {
        try {
            settleDamage(var0);
        } catch (LinkageError | RuntimeException var2) {
            FTBossMod.LOGGER.error("Native food weapon wear skipped after authority failure", var2);
        }
    }

    private static void settleDamage(ItemStack var0) {
        if (server() != null) {
            if (eligible(var0)) {
                ServerPlayer var1 = holder(var0);
                if (var1 != null) {
                    MinecraftServer var2 = var1.getServer();
                    NativeFoodWeaponWear.Authority var3 = authority(var2);
                    CompoundTag var4 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle").getCompound("NativeWear");
                    CompoundTag var5 = var3.weapons.getCompound(var4.getUUID("Identity").toString());
                    if (var5.isEmpty()) {
                        manage(var1, var0);
                    } else {
                        int var6 = var0.getDamageValue();
                        int var7 = var0.getMaxDamage();
                        int var8 = (var5.getInt("Mask") | var4.getInt("Mask")) & 15;
                        int var9 = var7 == var5.getInt("Maximum") && var6 > var5.getInt("Damage")
                            ? reached(var6, var7) & ~reached(var5.getInt("Damage"), var7) & ~var8
                            : 0;
                        int var10 = var5.getInt("Mask");
                        var5.putInt("Mask", var8 | var9);
                        var5.putInt("Damage", var6);
                        var5.putInt("Maximum", var7);
                        var3.setDirty();
                        if (var9 != 0 || var10 != (var8 | var9)) {
                            checkpoint(var2, var3);
                        }

                        if (var4.getInt("Mask") != (var8 | var9)) {
                            NativeLifecycleItemData.updateWearMask(var0, var4.getUUID("Identity"), var8 | var9);
                            var4.putInt("Mask", var8 | var9);
                        }

                        for (int var11 = 0; var11 < REMAINING.length; var11++) {
                            if ((var9 & 1 << var11) != 0) {
                                NativeFoodWeaponWear.Candidate var12 = candidates(var0)
                                    .stream()
                                    .min(
                                        Comparator.comparingInt(NativeFoodWeaponWear.Candidate::rarity).thenComparingInt(NativeFoodWeaponWear.Candidate::order)
                                    )
                                    .orElse(null);
                                if (var12 != null) {
                                    NativeFoundationItemData.copyComponents(var0, var12.after());
                                }
                            }
                        }

                        var5.putInt("Maximum", var0.getMaxDamage());
                        var5.putInt("Damage", var0.getDamageValue());
                        if (var0.getDamageValue() >= var0.getMaxDamage()) {
                            FoodWeaponData.edit(var0, "FTBossWeaponLifecycle", current -> current.putBoolean("Broken", true));
                        }

                        if (var9 != 0) {
                            checkpoint(var2, var3);
                        }
                    }
                }
            }
        }
    }

    private static int rarity(ItemStack var0) {
        return var0.isEmpty() ? Integer.MAX_VALUE : var0.getRarity().ordinal();
    }

    private static ItemStack offering(Material var0) {
        for (String var2 : WeaponMaterialCatalogRules.offerings(var0)) {
            ResourceLocation var3 = ResourceLocation.tryParse(var2);
            if (var3 != null && BuiltInRegistries.ITEM.containsKey(var3)) {
                return new ItemStack((ItemLike)BuiltInRegistries.ITEM.getOptional(var3).orElse(null));
            }
        }

        return ItemStack.EMPTY;
    }

    private static List<NativeFoodWeaponWear.Candidate> candidates(ItemStack var0) {
        ArrayList var1 = new ArrayList();
        if (FourWeaponFoundationAdapter.applies(var0)) {
            FourWeaponFoundationAdapter.View var2 = FourWeaponFoundationAdapter.read(var0);

            for (Entry var4 : var2.entries().entrySet()) {
                ItemStack var5 = FourWeaponFoundationAdapter.nativeWearCandidate(var0, (Integer)var4.getKey());
                if (var5 != null) {
                    var1.add(
                        new NativeFoodWeaponWear.Candidate(
                            rarity(((FourWeaponFoundationAdapter.Entry)var4.getValue()).storedItem()), (Integer)var4.getKey(), var5
                        )
                    );
                }
            }
        } else if (SwordCavityBuild.applies(var0)) {
            for (Slot var15 : Slot.values()) {
                ItemStack var6 = SwordCavityBuild.nativeWearCandidate(var0, var15);
                if (var6 != null) {
                    var1.add(new NativeFoodWeaponWear.Candidate(rarity(SwordCavityBuild.storedItem(var0, var15)), var15.ordinal(), var6));
                }
            }
        } else {
            CompoundTag var10 = FoodWeaponData.read(var0, "FTBossWeaponMaterials");
            if (var10 == null) {
                return var1;
            }

            for (Part var17 : Part.values()) {
                if (var17 != Part.CORE) {
                    Material var7 = Material.byId(var10.getString(var17.name()));
                    if (var7 != Material.ORIGINAL) {
                        ItemStack var8 = var0.copy();
                        FoodWeaponData.edit(var8, "FTBossWeaponMaterials", current -> current.putString(var17.name(), Material.ORIGINAL.id()));
                        var1.add(new NativeFoodWeaponWear.Candidate(rarity(offering(var7)), var17.ordinal(), var8));
                    }
                }
            }
        }

        return var1;
    }

    private static CompoundTag taskState(ServerPlayer var0, ItemStack var1, CompoundTag var2) {
        if (eligible(var1) && holder(var1) == var0 && var2.hasUUID("NativeTask")) {
            NativeFoodWeaponWear.Authority var3 = authority(var0.getServer());
            CompoundTag var4 = FoodWeaponData.read(var1, "FTBossWeaponLifecycle").getCompound("NativeWear");
            return var3.weapons.getCompound(var4.getUUID("Identity").toString()).getCompound("Tasks").getCompound(var2.getUUID("NativeTask").toString());
        } else {
            return null;
        }
    }

    static boolean startTask(ServerPlayer var0, ItemStack var1, CompoundTag var2) {
        if (eligible(var1) && StatueRepairMutation.nativeRepairSource(var0, var1) != null && holder(var1) == var0) {
            NativeFoodWeaponWear.Authority var3 = authority(var0.getServer());
            CompoundTag var4 = FoodWeaponData.read(var1, "FTBossWeaponLifecycle").getCompound("NativeWear");
            CompoundTag var5 = var3.weapons.getCompound(var4.getUUID("Identity").toString());
            if (var5.isEmpty()) {
                return false;
            } else {
                CompoundTag var6 = var5.getCompound("Tasks");
                if (var6.getAllKeys().size() >= 1024) {
                    return false;
                } else {
                    UUID var7 = UUID.randomUUID();
                    if (var6.contains(var7.toString())) {
                        return false;
                    } else {
                        CompoundTag var8 = new CompoundTag();
                        Claim prepared = prepareJournal(var0, var1, var7, Operation.START_REPAIR, -1, 0);
                        var8.putIntArray("Paid", new int[3]);
                        var6.put(var7.toString(), var8);
                        var5.put("Tasks", var6);
                        checkpoint(var0.getServer(), var3);
                        var2.putUUID("NativeTask", var7);
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
    }

    static boolean taskLive(ServerPlayer var0, ItemStack var1, CompoundTag var2) {
        CompoundTag var3 = taskState(var0, var1, var2);
        return StatueRepairMutation.nativeRepairSource(var0, var1) != null
            && var3 != null
            && !var3.isEmpty()
            && !var3.getBoolean("Consumed")
            && Arrays.equals(var3.getIntArray("Paid"), NativeRepairTaskData.progressCopy(var1, var2));
    }

    static void recordTaskPayment(ServerPlayer var0, ItemStack var1, CompoundTag var2, int var3, int var4) {
        if (taskLive(var0, var1, var2) && var3 >= 0 && var3 < 3 && var4 > 0) {
            CompoundTag var5 = taskState(var0, var1, var2);
            int[] var6 = var5.getIntArray("Paid");
            if (var6.length != 3) {
                throw new IllegalStateException("Invalid native repair accounting");
            } else {
                StatueRepairMutation outer = StatueRepairMutation.nativeRepairSource(var0, var1);
            if (outer == null || outer.nativeRepairPayment(var0, var1, var3) != var4) throw new IllegalStateException("Native repair lacks actual payment");
            prepareJournal(var0, var1, var2.getUUID("NativeTask"), Operation.SUBMIT_REPAIR, var3, var4);
            var6[var3] = Math.addExact(var6[var3], var4);
                var5.putIntArray("Paid", var6);
                checkpoint(var0.getServer(), authority(var0.getServer()));
            }
        } else {
            throw new IllegalStateException("Detached native repair payment");
        }
    }

    static boolean prepareStatueClaim(ServerPlayer var0, ItemStack var1, CompoundTag var2) {
        StatueRepairMutation var3 = StatueRepairMutation.nativeRepairSource(var0, var1);
        if (var3 != null && taskLive(var0, var1, var2) && !CLAIMS.containsKey(var1)) {
            CompoundTag var4 = taskState(var0, var1, var2);
            int[] var5 = var4.getIntArray("Paid");
            if (var5.length != 3) {
                return false;
            } else {
                for (int var6 = 0; var6 < 3; var6++) {
                    if (var5[var6] != FoodWeaponLifecycleRules.repairRequired(var2.getInt("Level"), var6)) {
                        return false;
                    }
                }

                prepareJournal(var0, var1, var2.getUUID("NativeTask"), Operation.CLAIM_REPAIR, -1, 0);
                var4.putBoolean("Consumed", true);
                checkpoint(var0.getServer(), authority(var0.getServer()));
                CompoundTag var7 = FoodWeaponData.read(var1, "FTBossWeaponLifecycle").getCompound("NativeWear");

                return true;
            }
        } else {
            return false;
        }
    }

    static void committedStatueRepair(StatueRepairMutation outer, ServerPlayer player, ItemStack weapon) {
        Claim claim = CLAIMS.get(weapon);
        if (claim == null) return;
        OwnerLineageRevocationStore.Lease issued = null;
        boolean completed = false;
        try {
            if (claim.outer != outer || !claim.terminal(weapon)) throw new IllegalStateException("Native repair terminal ancestry changed");
            Authority authority = authority(player.getServer());
            CompoundTag state = authority.weapons.getCompound(claim.nativeIdentity.toString());
            if (claim.operation == Operation.CLAIM_REPAIR) {
                if (!state.getCompound("Tasks").getCompound(claim.task.toString()).getBoolean("Consumed")) throw new IllegalStateException("Native repair claim not consumed");
                ItemStack previous = REPAIR_WRITE.get();
                REPAIR_WRITE.set(weapon);
                try {
                    FoodWeaponLifecycle.restoreAfterQuest(weapon);
                    state.putInt("Damage", 0);
                    state.putInt("Maximum", weapon.getMaxDamage());
                    checkpoint(player.getServer(), authority);
                } finally {
                    if (previous == null) REPAIR_WRITE.remove(); else REPAIR_WRITE.set(previous);
                }
            }
            claim.commitJournal(weapon);
            OwnerNativeSemanticAuthority.nativeTransitionReturned(claim, weapon);
            if (claim.operation == Operation.CLAIM_REPAIR) {
                issued = claim.store.consumeNativeRepair(claim);
                if (!OwnerRepairGenerationAuthority.publishNativeRepair(claim, weapon, issued)) throw new IllegalStateException("Native signed repair generation publication failed");
            }
            completed = true;
        } catch (RuntimeException | Error failure) {
            FTBossMod.LOGGER.error("Original native repair terminal {} failed", claim.operation, failure);
            throw failure;
        } catch (IOException failure) {
            throw new IllegalStateException("Native repair lease store failed", failure);
        } finally {
            CLAIMS.remove(weapon);
            if (!completed && issued != null) OwnerLineageRevocationStore.retireLease(issued);
        }
    }


    static void beforeNativeRepairWrite(ItemStack actual, Object receiver, String key, net.minecraft.nbt.Tag value) {
        Claim claim = CLAIMS.get(actual);
        if (claim != null) claim.beforeWrite(receiver, key, value);
    }

    static void afterNativeRepairWrite(ItemStack actual, Throwable failure) {
        Claim claim = CLAIMS.get(actual);
        if (claim != null) claim.afterWrite(failure);
    }

    static void nativeRepairChanged(Object actual, Object receiver, int kind) {
        Claim claim = actual instanceof ItemStack stack ? CLAIMS.get(stack) : null;
        if (claim == null) return;
        if (claim.thread != Thread.currentThread() || claim.writing == null || claim.receiver != receiver || (kind != 20 && kind != 21)) {
            claim.reject("mutation kind " + kind + " on " + (receiver == null ? "null" : receiver.getClass().getName()) + " during " + (claim.writing == null ? "no write" : claim.writing.key()));
            return;
        }
        boolean allowed = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> {
            java.util.List<StackWalker.StackFrame> path = frames.takeWhile(frame -> frame.getDeclaringClass() != OwnerStatueTaskWrites.class
                && !(frame.getDeclaringClass() == FoodWeaponLifecycle.class && frame.getMethodName().equals("restoreAfterQuest"))).toList();
            return path.stream().allMatch(frame -> frame.getDeclaringClass().getNestHost() == NativeFoodWeaponWear.class
                || frame.getDeclaringClass().getNestHost() == OwnerFullRepairGuard.class
                || frame.getDeclaringClass() == net.minecraft.nbt.CompoundTag.class || frame.getDeclaringClass() == ItemStack.class
                || frame.getDeclaringClass().getName().startsWith("net.minecraft.core.component."));
        });
        if (!allowed) claim.reject("mutation caller path outside original task write");
    }

    static void abortedStatueRepair(StatueRepairMutation outer, ServerPlayer player, ItemStack weapon) {
        Claim claim = CLAIMS.get(weapon);
        if (claim != null && claim.outer == outer && claim.player == player) CLAIMS.remove(weapon);
    }

    private static Claim prepareJournal(ServerPlayer player, ItemStack weapon, UUID task, Operation operation, int index, int payment) {
        if (CLAIMS.containsKey(weapon)) throw new IllegalStateException("Native repair already pending");
        StatueRepairMutation outer = StatueRepairMutation.nativeRepairSource(player, weapon);
        OwnerNativeSemanticAuthority.WeaponInput input = OwnerNativeSemanticAuthority.nativeRepairInput(weapon);
        CompoundTag before = OwnerNativeSemanticAuthority.nativeRepairImage(input, weapon);
        if (outer == null) throw new IllegalStateException("Native repair original transaction missing");
        if (before == null) throw new IllegalStateException("Native repair independent input missing or retired");
        if (holder(weapon) != player) throw new IllegalStateException("Native repair original holder changed");
        if (!OwnerRepairGenerationAuthority.nativeTaskObjectPolicy(weapon)) throw new IllegalStateException("Native repair stack policy denied");
        Claim claim = new Claim(outer, player, weapon, task, operation, index, payment, input, before);
        claim.reserveJournal();
        CLAIMS.put(weapon, claim);
        return claim;
    }

    private static final class Authority extends SavedData {
        private CompoundTag weapons = new CompoundTag();

        private static NativeFoodWeaponWear.Authority load(CompoundTag var0) {
            if (var0.getInt("Version") != 1) {
                throw new IllegalArgumentException("Unsupported native wear data");
            } else {
                NativeFoodWeaponWear.Authority var1 = new NativeFoodWeaponWear.Authority();
                var1.weapons = var0.getCompound("Weapons").copy();
                return var1;
            }
        }

        @Override
        public CompoundTag save(CompoundTag var1, HolderLookup.Provider registries) {
            var1.putInt("Version", 1);
            var1.put("Weapons", this.weapons.copy());
            return var1;
        }
    }

    private static record Candidate(int rarity, int order, ItemStack after) {
    }

    static final class Claim {
        private final StatueRepairMutation outer;
        private final ServerPlayer player;
        private final ItemStack weapon;
        private final UUID nativeIdentity;
        private final UUID weaponId;
        private final UUID task;
        private final Operation operation;
        private final int index;
        private final int payment;
        private final OwnerNativeSemanticAuthority.WeaponInput input;
        private final CompoundTag before;
        private final CompoundTag expected;
        private final OwnerLineageRevocationStore store;
        private final UUID documentOwner;
        private final Thread thread = Thread.currentThread();
        private CompoundTag document;
        private final OwnerStatueTaskWritePlan plan;
        private final NativeItemCapabilityObservation capabilities;
        private OwnerStatueTaskWritePlan.Step writing;
        private Object receiver;
        private boolean rejected;
        private String rejection;
        private boolean committed;
        private boolean taken;
        private OwnerLineageRevocationStore.Lease issued;

        private Claim(StatueRepairMutation outer, ServerPlayer player, ItemStack weapon, UUID task, Operation operation, int index, int payment,
                      OwnerNativeSemanticAuthority.WeaponInput input, CompoundTag before) {
            this.outer = outer;
            this.player = player;
            this.weapon = weapon;
            this.nativeIdentity = FoodWeaponData.read(weapon, "FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity");
            this.weaponId = FoodWeaponLifecycle.weaponId(weapon);
            this.task = task;
            this.operation = operation;
            this.index = index;
            this.payment = payment;
            this.input = input;
            this.capabilities = NativeItemCapabilityObservation.nativeRepairCapture(weapon);
            this.before = before.copy();
            this.expected = OwnerStatueTaskPlan.nativeRepair(before, operation, task, index, payment, player.registryAccess());
            this.plan = new OwnerStatueTaskWritePlan(before, operation, task, index, payment, player.registryAccess());
            this.documentOwner = UUID.nameUUIDFromBytes(("ftboss/native-repair-journal/v1/" + player.getUUID()).getBytes(StandardCharsets.UTF_8));
            try {
                this.store = FoodWeaponSourceLedger.nativeRepairStore(player);
                this.document = this.store.readRepairDocument(this.documentOwner, false);
            } catch (IOException failure) { throw new IllegalStateException("Native repair signed journal unavailable", failure); }
        }


        private void beforeWrite(Object receiver, String key, net.minecraft.nbt.Tag value) {
            boolean caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2L).findFirst()
                .map(frame -> frame.getDeclaringClass() == OwnerStatueTaskWrites.class || frame.getDeclaringClass() == FoodWeaponLifecycle.class).orElse(false));
            if (!caller || this.rejected || this.writing != null || this.thread != Thread.currentThread() || !this.capabilities.live()
                || !this.plan.cursor.equals(NativeFoundationItemData.image(this.weapon, this.player.registryAccess()))) throw new IllegalStateException("Native task original prefix mismatch");
            OwnerStatueTaskWritePlan.Step step = this.plan.expected(key, value);
            Object expectedReceiver = this.weapon;
            if (!step.path().isEmpty()) {
                CompoundTag compound = NativeSourceLedgerItemData.rawCustomDataForObservation(this.weapon);
                for (String part : step.path().split("/")) compound = compound.getCompound(part);
                expectedReceiver = compound;
            }
            if (receiver != expectedReceiver) throw new IllegalStateException("Native task original receiver mismatch");
            this.writing = step;
            this.receiver = receiver;
        }

        private void afterWrite(Throwable failure) {
            try {
                if (failure != null || this.rejected || this.writing == null
                    || !this.writing.after().equals(NativeFoundationItemData.image(this.weapon, this.player.registryAccess()))) {
                    this.rejected = true;
                    if (failure == null) throw new IllegalStateException("Native task write did not match independent prefix");
                    return;
                }
                this.plan.advance(this.writing);
            } finally {
                this.writing = null;
                this.receiver = null;
            }
        }

        private void persist(CompoundTag next) {
            try {
                this.store.writeRepairDocument(this.documentOwner, false, this.document, next);
                this.document = next.copy();
            } catch (IOException failure) { throw new IllegalStateException("Native repair signed journal checkpoint failed", failure); }
        }

        private void reserveJournal() {
            CompoundTag next = this.document.copy();
            if (next.isEmpty()) {
                next.putInt("Schema", 1);
                next.putUUID("Owner", this.player.getUUID());
                next.put("Tasks", new CompoundTag());
            }
            if (next.getInt("Schema") != 1 || !this.player.getUUID().equals(next.getUUID("Owner"))) throw new IllegalStateException("Native repair journal owner mismatch");
            CompoundTag tasks = next.getCompound("Tasks");
            CompoundTag record = tasks.getCompound(this.task.toString());
            if (this.operation == Operation.START_REPAIR) {
                if (!record.isEmpty() || tasks.getAllKeys().size() >= 1024) throw new IllegalStateException("Native repair START replay or budget");
                record.putUUID("Weapon", this.weaponId);
                record.putUUID("NativeIdentity", this.nativeIdentity);
                record.putString("Rules", OwnerStatueTaskPlan.rules(this.weapon));
                record.putIntArray("Paid", new int[3]);
                record.putLong("Sequence", 0L);
            } else {
                if (!"COMMITTED".equals(record.getString("Status")) || !this.weaponId.equals(record.getUUID("Weapon"))
                    || !this.nativeIdentity.equals(record.getUUID("NativeIdentity")) || !this.before.equals(record.getCompound("Expected"))
                    || !OwnerStatueTaskPlan.rules(this.weapon).equals(record.getString("Rules"))) throw new IllegalStateException("Native repair signed predecessor missing or changed");
                int[] paid = record.getIntArray("Paid");
                if (paid.length != 3 || !Arrays.equals(paid, NativeRepairStackImage.data(this.before).getCompound("FTBossWeaponLifecycle").getCompound("RepairQuest").getIntArray("Progress"))) throw new IllegalStateException("Native repair signed payment ancestry");
                if (this.operation == Operation.SUBMIT_REPAIR) paid[this.index] = Math.addExact(paid[this.index], this.payment);
                record.putIntArray("Paid", paid);
            }
            record.putString("Status", "PENDING_ORIGINAL_TERMINAL");
            record.putString("Operation", this.operation.name());
            record.putLong("Sequence", Math.addExact(record.getLong("Sequence"), 1L));
            record.put("Before", this.before.copy());
            record.put("Expected", this.expected.copy());
            tasks.put(this.task.toString(), record);
            this.persist(next);
        }

        private boolean terminal(ItemStack actual) {
            return actual == this.weapon && this.thread == Thread.currentThread() && this.player.getServer().isSameThread()
                && holder(actual) == this.player && eligible(actual) && this.weaponId.equals(FoodWeaponLifecycle.weaponId(actual))
                && this.nativeIdentity.equals(FoodWeaponData.read(actual, "FTBossWeaponLifecycle").getCompound("NativeWear").getUUID("Identity"))
                && this.outer.nativeRepairTerminal(this.player, actual, this.operation);
        }

        private void commitJournal(ItemStack actual) {
            if (this.rejected) throw new IllegalStateException("Native repair mutation rejected: " + this.rejection);
            if (this.writing != null || !this.plan.complete()) throw new IllegalStateException("Native repair original writes incomplete");
            if (!this.plan.cursor.equals(this.expected)) throw new IllegalStateException("Native repair plan cursor differs from terminal plan");
            if (!this.terminal(actual)) throw new IllegalStateException("Native repair terminal ancestry no longer live");
            if (!this.expected.equals(NativeFoundationItemData.image(actual, this.player.registryAccess()))) throw new IllegalStateException("Native repair original image differs from terminal plan");
            CompoundTag next = this.document.copy();
            CompoundTag record = next.getCompound("Tasks").getCompound(this.task.toString());
            if (!"PENDING_ORIGINAL_TERMINAL".equals(record.getString("Status"))) throw new IllegalStateException("Native repair terminal replay");
            record.putString("Status", this.operation == Operation.CLAIM_REPAIR ? "CONSUMED" : "COMMITTED");
            this.persist(next);
            this.committed = true;
        }

        private void reject(String reason) {
            if (!this.rejected) this.rejection = reason;
            this.rejected = true;
        }

        OwnerNativeSemanticAuthority.WeaponInput input() { return this.input; }
        CompoundTag expected() { return this.expected.copy(); }

        boolean authorizesInput(ItemStack actual) {
            return !this.rejected && this.writing == null && this.plan.complete() && CLAIMS.get(actual) == this && this.committed && this.capabilities.live() && this.terminal(actual) && this.expected.equals(NativeFoundationItemData.image(actual, this.player.registryAccess()));
        }

        UUID owner() { return this.player.getUUID(); }
        NativeItemCapabilityObservation capabilities() { return this.capabilities; }
        UUID weaponId() { return this.weaponId; }

        boolean takeForStore(OwnerLineageRevocationStore store) {
            if (this.store != store || this.taken || this.operation != Operation.CLAIM_REPAIR || !this.authorizesInput(this.weapon)) return false;
            CompoundTag next = this.document.copy();
            CompoundTag record = next.getCompound("Tasks").getCompound(this.task.toString());
            if (!"CONSUMED".equals(record.getString("Status"))) return false;
            record.putString("Status", "LEASE_RESERVED_NO_REISSUE");
            this.persist(next);
            this.taken = true;
            return true;
        }

        void issued(OwnerLineageRevocationStore.Lease lease) {
            if (!this.taken || this.issued != null || !this.owner().equals(lease.owner) || !this.weaponId.equals(lease.weapon)) throw new IllegalStateException("Native repair lease identity");
            CompoundTag next = this.document.copy();
            CompoundTag record = next.getCompound("Tasks").getCompound(this.task.toString());
            record.putString("Status", "LEASE_ISSUED");
            record.putUUID("LeaseEpoch", lease.incarnation);
            record.putLong("LeaseGeneration", lease.generation);
            this.persist(next);
            this.issued = lease;
        }

        boolean owns(ItemStack actual, OwnerLineageRevocationStore.Lease lease) {
            return this.issued == lease && lease != null && this.authorizesInput(actual) && OwnerLineageRevocationStore.liveLease(lease);
        }
    }
}
