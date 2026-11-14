/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.saveddata.SavedData
 *  net.minecraft.world.level.storage.LevelResource
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.item;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class NativeFoodWeaponWear {
    private static final String KEY = "NativeWear";
    private static final int VERSION = 1;
    private static final int[] REMAINING = new int[]{50, 35, 20, 10};
    private static final ThreadLocal<ItemStack> REPAIR_WRITE = new ThreadLocal();
    private static final Map<ItemStack, Claim> CLAIMS = new WeakHashMap<ItemStack, Claim>();

    private NativeFoodWeaponWear() {
    }

    public static boolean eligible(ItemStack itemStack) {
        if (!NativeFoodWeaponWear.nativeDamageable(itemStack)) {
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle");
        CompoundTag compoundTag2 = compoundTag == null ? null : compoundTag.m_128469_(KEY);
        return compoundTag2 != null && compoundTag2.m_128471_("Managed") && compoundTag2.m_128451_("Version") == 1 && compoundTag2.m_128403_("Identity") && FoodWeaponEvolution.isEvolutionCandidate(itemStack);
    }

    static boolean nativeDamageable(ItemStack itemStack) {
        return itemStack != null && !itemStack.m_41619_() && itemStack.m_41613_() == 1 && (!itemStack.m_41782_() || !itemStack.m_41783_().m_128471_("Unbreakable")) && itemStack.m_41720_().getMaxDamage(itemStack) > 0 && itemStack.m_41763_() && itemStack.m_41776_() > 0;
    }

    public static boolean statueRepairWrite(ItemStack itemStack) {
        return REPAIR_WRITE.get() == itemStack && NativeFoodWeaponWear.eligible(itemStack);
    }

    private static MinecraftServer server() {
        MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
        return minecraftServer != null && minecraftServer.m_18695_() ? minecraftServer : null;
    }

    private static Authority authority(MinecraftServer minecraftServer) {
        return (Authority)minecraftServer.m_129783_().m_8895_().m_164861_(Authority::load, Authority::new, "ftboss_native_weapon_wear");
    }

    private static void checkpoint(MinecraftServer minecraftServer, Authority authority) {
        if (!minecraftServer.m_18695_() || Thread.holdsLock(OwnerFullRepairGuard.class) || Thread.holdsLock(FoodWeaponSourceLedger.class) || Thread.holdsLock(OwnerFieldExpectedAuthority.class)) {
            throw new IllegalStateException("Native wear checkpoint outside server boundary");
        }
        authority.m_77762_();
        minecraftServer.m_129783_().m_8895_().m_78151_();
        Path path = minecraftServer.m_129843_(LevelResource.f_78182_).resolve("data").resolve("ftboss_native_weapon_wear.dat");
        try {
            if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, new LinkOption[0]) || !NbtIo.m_128937_((File)path.toFile()).m_128469_("data").equals((Object)authority.m_7176_(new CompoundTag()))) {
                throw new IllegalStateException("Native wear authority checkpoint did not persist");
            }
        }
        catch (IOException iOException) {
            throw new IllegalStateException("Native wear checkpoint unavailable", iOException);
        }
    }

    private static ServerPlayer holder(ItemStack itemStack) {
        MinecraftServer minecraftServer = NativeFoodWeaponWear.server();
        if (minecraftServer == null) {
            return null;
        }
        ServerPlayer serverPlayer = null;
        for (ServerPlayer serverPlayer2 : minecraftServer.m_6846_().m_11314_()) {
            for (int i = 0; i < serverPlayer2.m_150109_().m_6643_(); ++i) {
                if (serverPlayer2.m_150109_().m_8020_(i) != itemStack) continue;
                if (serverPlayer != null) {
                    return null;
                }
                serverPlayer = serverPlayer2;
            }
        }
        return serverPlayer;
    }

    public static void manage(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (serverPlayer == null || serverPlayer.m_20194_() == null || !serverPlayer.m_20194_().m_18695_() || serverPlayer.m_9236_().f_46443_ || !NativeFoodWeaponWear.nativeDamageable(itemStack) || !FoodWeaponEvolution.isEvolutionCandidate(itemStack) || NativeFoodWeaponWear.holder(itemStack) != serverPlayer) {
            return;
        }
        CompoundTag compoundTag = itemStack.m_41698_("FTBossWeaponLifecycle");
        CompoundTag compoundTag2 = compoundTag.m_128469_(KEY);
        if (compoundTag2.m_128456_()) {
            compoundTag2.m_128405_("Version", 1);
            compoundTag2.m_128379_("Managed", true);
            compoundTag2.m_128362_("Identity", UUID.randomUUID());
            compoundTag2.m_128405_("Mask", NativeFoodWeaponWear.reached(itemStack.m_41773_(), itemStack.m_41776_()));
            compoundTag.m_128365_(KEY, (Tag)compoundTag2);
        }
        if (!NativeFoodWeaponWear.eligible(itemStack)) {
            return;
        }
        if (!compoundTag.m_128403_("Weapon")) {
            compoundTag.m_128362_("Weapon", UUID.randomUUID());
        }
        if (compoundTag.m_128461_("RepairProfile").isEmpty()) {
            compoundTag.m_128359_("RepairProfile", FoodWeaponEvolution.repairProfileId(itemStack));
        }
        Authority authority = NativeFoodWeaponWear.authority(serverPlayer.m_20194_());
        String string = compoundTag2.m_128342_("Identity").toString();
        if (!authority.weapons.m_128441_(string)) {
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128405_("Mask", compoundTag2.m_128451_("Mask") & 0xF);
            compoundTag3.m_128405_("Damage", itemStack.m_41773_());
            compoundTag3.m_128405_("Maximum", itemStack.m_41776_());
            authority.weapons.m_128365_(string, (Tag)compoundTag3);
            NativeFoodWeaponWear.checkpoint(serverPlayer.m_20194_(), authority);
        }
    }

    static void manageBound(ItemStack itemStack) {
        ServerPlayer serverPlayer = NativeFoodWeaponWear.holder(itemStack);
        if (serverPlayer != null) {
            NativeFoodWeaponWear.manage(serverPlayer, itemStack);
        }
    }

    static void componentsChanged(ItemStack itemStack) {
        if (!NativeFoodWeaponWear.eligible(itemStack)) {
            return;
        }
        ServerPlayer serverPlayer = NativeFoodWeaponWear.holder(itemStack);
        if (serverPlayer == null) {
            return;
        }
        Authority authority = NativeFoodWeaponWear.authority(serverPlayer.m_20194_());
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle").m_128469_(KEY);
        CompoundTag compoundTag2 = authority.weapons.m_128469_(compoundTag.m_128342_("Identity").toString());
        if (compoundTag2.m_128456_()) {
            return;
        }
        compoundTag2.m_128405_("Damage", itemStack.m_41773_());
        compoundTag2.m_128405_("Maximum", itemStack.m_41776_());
        NativeFoodWeaponWear.checkpoint(serverPlayer.m_20194_(), authority);
    }

    private static int reached(int n, int n2) {
        if (n2 <= 0) {
            return 0;
        }
        long l = Math.max(0L, (long)n2 - (long)Math.max(0, n));
        int n3 = 0;
        for (int i = 0; i < REMAINING.length; ++i) {
            if (l * 100L > (long)n2 * (long)REMAINING[i]) continue;
            n3 |= 1 << i;
        }
        return n3;
    }

    public static void damageChanged(ItemStack itemStack) {
        try {
            NativeFoodWeaponWear.settleDamage(itemStack);
        }
        catch (LinkageError | RuntimeException throwable) {
            FTBossMod.LOGGER.error("Native food weapon wear skipped after authority failure", throwable);
        }
    }

    private static void settleDamage(ItemStack itemStack) {
        if (NativeFoodWeaponWear.server() == null) {
            return;
        }
        if (!NativeFoodWeaponWear.eligible(itemStack)) {
            return;
        }
        ServerPlayer serverPlayer = NativeFoodWeaponWear.holder(itemStack);
        if (serverPlayer == null) {
            return;
        }
        MinecraftServer minecraftServer = serverPlayer.m_20194_();
        Authority authority = NativeFoodWeaponWear.authority(minecraftServer);
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle").m_128469_(KEY);
        CompoundTag compoundTag2 = authority.weapons.m_128469_(compoundTag.m_128342_("Identity").toString());
        if (compoundTag2.m_128456_()) {
            NativeFoodWeaponWear.manage(serverPlayer, itemStack);
            return;
        }
        int n = itemStack.m_41773_();
        int n2 = itemStack.m_41776_();
        int n3 = (compoundTag2.m_128451_("Mask") | compoundTag.m_128451_("Mask")) & 0xF;
        int n4 = n2 == compoundTag2.m_128451_("Maximum") && n > compoundTag2.m_128451_("Damage") ? NativeFoodWeaponWear.reached(n, n2) & ~NativeFoodWeaponWear.reached(compoundTag2.m_128451_("Damage"), n2) & ~n3 : 0;
        int n5 = compoundTag2.m_128451_("Mask");
        compoundTag2.m_128405_("Mask", n3 | n4);
        compoundTag2.m_128405_("Damage", n);
        compoundTag2.m_128405_("Maximum", n2);
        authority.m_77762_();
        if (n4 != 0 || n5 != (n3 | n4)) {
            NativeFoodWeaponWear.checkpoint(minecraftServer, authority);
        }
        if (compoundTag.m_128451_("Mask") != (n3 | n4)) {
            compoundTag.m_128405_("Mask", n3 | n4);
        }
        for (int i = 0; i < REMAINING.length; ++i) {
            Candidate candidate;
            if ((n4 & 1 << i) == 0 || (candidate = (Candidate)NativeFoodWeaponWear.candidates(itemStack).stream().min(Comparator.comparingInt(Candidate::rarity).thenComparingInt(Candidate::order)).orElse(null)) == null) continue;
            itemStack.m_41751_(candidate.after().m_41783_().m_6426_());
        }
        compoundTag2.m_128405_("Maximum", itemStack.m_41776_());
        compoundTag2.m_128405_("Damage", itemStack.m_41773_());
        if (itemStack.m_41773_() >= itemStack.m_41776_()) {
            itemStack.m_41737_("FTBossWeaponLifecycle").m_128379_("Broken", true);
        }
        if (n4 != 0) {
            NativeFoodWeaponWear.checkpoint(minecraftServer, authority);
        }
    }

    private static int rarity(ItemStack itemStack) {
        return itemStack.m_41619_() ? Integer.MAX_VALUE : itemStack.m_41791_().ordinal();
    }

    private static ItemStack offering(FoodWeaponBuildRules.Material material) {
        for (String string : WeaponMaterialCatalogRules.offerings(material)) {
            ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string);
            if (resourceLocation == null || !ForgeRegistries.ITEMS.containsKey(resourceLocation)) continue;
            return new ItemStack((ItemLike)ForgeRegistries.ITEMS.getValue(resourceLocation));
        }
        return ItemStack.f_41583_;
    }

    private static List<Candidate> candidates(ItemStack itemStack) {
        ArrayList<Candidate> arrayList = new ArrayList<Candidate>();
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
            for (Map.Entry<Integer, FourWeaponFoundationAdapter.Entry> entry : view.entries().entrySet()) {
                ItemStack itemStack2 = FourWeaponFoundationAdapter.nativeWearCandidate(itemStack, entry.getKey());
                if (itemStack2 == null) continue;
                arrayList.add(new Candidate(NativeFoodWeaponWear.rarity(entry.getValue().storedItem()), entry.getKey(), itemStack2));
            }
        } else if (SwordCavityBuild.applies(itemStack)) {
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                ItemStack itemStack3 = SwordCavityBuild.nativeWearCandidate(itemStack, slot);
                if (itemStack3 == null) continue;
                arrayList.add(new Candidate(NativeFoodWeaponWear.rarity(SwordCavityBuild.storedItem(itemStack, slot)), slot.ordinal(), itemStack3));
            }
        } else {
            CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponMaterials");
            if (compoundTag == null) {
                return arrayList;
            }
            for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
                FoodWeaponBuildRules.Material material;
                if (part == FoodWeaponBuildRules.Part.CORE || (material = FoodWeaponBuildRules.Material.byId(compoundTag.m_128461_(part.name()))) == FoodWeaponBuildRules.Material.ORIGINAL) continue;
                ItemStack itemStack4 = itemStack.m_41777_();
                itemStack4.m_41737_("FTBossWeaponMaterials").m_128359_(part.name(), FoodWeaponBuildRules.Material.ORIGINAL.id());
                arrayList.add(new Candidate(NativeFoodWeaponWear.rarity(NativeFoodWeaponWear.offering(material)), part.ordinal(), itemStack4));
            }
        }
        return arrayList;
    }

    private static CompoundTag taskState(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag) {
        if (!NativeFoodWeaponWear.eligible(itemStack) || NativeFoodWeaponWear.holder(itemStack) != serverPlayer || !compoundTag.m_128403_("NativeTask")) {
            return null;
        }
        Authority authority = NativeFoodWeaponWear.authority(serverPlayer.m_20194_());
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossWeaponLifecycle").m_128469_(KEY);
        return authority.weapons.m_128469_(compoundTag2.m_128342_("Identity").toString()).m_128469_("Tasks").m_128469_(compoundTag.m_128342_("NativeTask").toString());
    }

    static boolean startTask(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag) {
        if (!NativeFoodWeaponWear.eligible(itemStack) || StatueRepairMutation.nativeRepairSource(serverPlayer, itemStack) == null || NativeFoodWeaponWear.holder(itemStack) != serverPlayer) {
            return false;
        }
        Authority authority = NativeFoodWeaponWear.authority(serverPlayer.m_20194_());
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossWeaponLifecycle").m_128469_(KEY);
        CompoundTag compoundTag3 = authority.weapons.m_128469_(compoundTag2.m_128342_("Identity").toString());
        if (compoundTag3.m_128456_()) {
            return false;
        }
        CompoundTag compoundTag4 = compoundTag3.m_128469_("Tasks");
        if (compoundTag4.m_128431_().size() >= 1024) {
            return false;
        }
        UUID uUID = UUID.randomUUID();
        if (compoundTag4.m_128441_(uUID.toString())) {
            return false;
        }
        CompoundTag compoundTag5 = new CompoundTag();
        compoundTag5.m_128385_("Paid", new int[3]);
        compoundTag4.m_128365_(uUID.toString(), (Tag)compoundTag5);
        compoundTag3.m_128365_("Tasks", (Tag)compoundTag4);
        NativeFoodWeaponWear.checkpoint(serverPlayer.m_20194_(), authority);
        compoundTag.m_128362_("NativeTask", uUID);
        return true;
    }

    static boolean taskLive(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag) {
        CompoundTag compoundTag2 = NativeFoodWeaponWear.taskState(serverPlayer, itemStack, compoundTag);
        return StatueRepairMutation.nativeRepairSource(serverPlayer, itemStack) != null && compoundTag2 != null && !compoundTag2.m_128456_() && !compoundTag2.m_128471_("Consumed") && Arrays.equals(compoundTag2.m_128465_("Paid"), compoundTag.m_128465_("Progress"));
    }

    static void recordTaskPayment(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag, int n, int n2) {
        if (!NativeFoodWeaponWear.taskLive(serverPlayer, itemStack, compoundTag) || n < 0 || n >= 3 || n2 <= 0) {
            throw new IllegalStateException("Detached native repair payment");
        }
        CompoundTag compoundTag2 = NativeFoodWeaponWear.taskState(serverPlayer, itemStack, compoundTag);
        int[] nArray = compoundTag2.m_128465_("Paid");
        if (nArray.length != 3) {
            throw new IllegalStateException("Invalid native repair accounting");
        }
        nArray[n] = Math.addExact(nArray[n], n2);
        compoundTag2.m_128385_("Paid", nArray);
        NativeFoodWeaponWear.checkpoint(serverPlayer.m_20194_(), NativeFoodWeaponWear.authority(serverPlayer.m_20194_()));
    }

    static boolean prepareStatueClaim(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag) {
        StatueRepairMutation statueRepairMutation = StatueRepairMutation.nativeRepairSource(serverPlayer, itemStack);
        if (statueRepairMutation == null || !NativeFoodWeaponWear.taskLive(serverPlayer, itemStack, compoundTag) || CLAIMS.containsKey(itemStack)) {
            return false;
        }
        CompoundTag compoundTag2 = NativeFoodWeaponWear.taskState(serverPlayer, itemStack, compoundTag);
        int[] nArray = compoundTag2.m_128465_("Paid");
        if (nArray.length != 3) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            if (nArray[i] == FoodWeaponLifecycleRules.repairRequired(compoundTag.m_128451_("Level"), i)) continue;
            return false;
        }
        compoundTag2.m_128379_("Consumed", true);
        NativeFoodWeaponWear.checkpoint(serverPlayer.m_20194_(), NativeFoodWeaponWear.authority(serverPlayer.m_20194_()));
        CompoundTag compoundTag3 = itemStack.m_41737_("FTBossWeaponLifecycle").m_128469_(KEY);
        CLAIMS.put(itemStack, new Claim(statueRepairMutation, serverPlayer, compoundTag3.m_128342_("Identity"), compoundTag.m_128342_("NativeTask")));
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void committedStatueRepair(StatueRepairMutation statueRepairMutation, ServerPlayer serverPlayer, ItemStack itemStack) {
        Claim claim = CLAIMS.remove(itemStack);
        if (claim == null || claim.outer() != statueRepairMutation || claim.player() != serverPlayer || !statueRepairMutation.nativeRepairCommitted(serverPlayer, itemStack) || !NativeFoodWeaponWear.eligible(itemStack) || NativeFoodWeaponWear.holder(itemStack) != serverPlayer) {
            return;
        }
        Authority authority = NativeFoodWeaponWear.authority(serverPlayer.m_20194_());
        CompoundTag compoundTag = authority.weapons.m_128469_(claim.weapon().toString());
        if (!compoundTag.m_128469_("Tasks").m_128469_(claim.task().toString()).m_128471_("Consumed") || !itemStack.m_41737_("FTBossWeaponLifecycle").m_128469_(KEY).m_128342_("Identity").equals(claim.weapon())) {
            return;
        }
        ItemStack itemStack2 = REPAIR_WRITE.get();
        REPAIR_WRITE.set(itemStack);
        try {
            FoodWeaponLifecycle.restoreAfterQuest(itemStack);
            compoundTag.m_128405_("Damage", 0);
            compoundTag.m_128405_("Maximum", itemStack.m_41776_());
            NativeFoodWeaponWear.checkpoint(serverPlayer.m_20194_(), authority);
        }
        finally {
            if (itemStack2 == null) {
                REPAIR_WRITE.remove();
            } else {
                REPAIR_WRITE.set(itemStack2);
            }
        }
    }

    private static final class Authority
    extends SavedData {
        private CompoundTag weapons = new CompoundTag();

        private Authority() {
        }

        private static Authority load(CompoundTag compoundTag) {
            if (compoundTag.m_128451_("Version") != 1) {
                throw new IllegalArgumentException("Unsupported native wear data");
            }
            Authority authority = new Authority();
            authority.weapons = compoundTag.m_128469_("Weapons").m_6426_();
            return authority;
        }

        public CompoundTag m_7176_(CompoundTag compoundTag) {
            compoundTag.m_128405_("Version", 1);
            compoundTag.m_128365_("Weapons", (Tag)this.weapons.m_6426_());
            return compoundTag;
        }
    }

    private record Candidate(int rarity, int order, ItemStack after) {
    }

    private record Claim(StatueRepairMutation outer, ServerPlayer player, UUID weapon, UUID task) {
    }
}

