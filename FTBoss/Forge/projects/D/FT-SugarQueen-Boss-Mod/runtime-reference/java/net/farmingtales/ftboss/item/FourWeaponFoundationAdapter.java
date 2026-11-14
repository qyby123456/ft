/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TridentItem
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.AdditionalWeaponUiEligibility;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.item.WeaponDurabilityBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.registries.ForgeRegistries;

public final class FourWeaponFoundationAdapter {
    public static final String ROOT = "FTBossFourWeaponFoundation";
    public static final String EVOLUTION = "FTBossFoodEvolution";
    private static final String LEGACY = "FTBossSwordStructure";
    private static final String MATERIALS = "FTBossWeaponMaterials";
    public static final int SCHEMA = 2;
    public static final int DIGEST_VERSION = 3;
    private static final FoodWeaponBuildRules.Build IDENTITY = new FoodWeaponBuildRules.Build(1.0, 1.0, 1.0, 1.0, 0.0);
    private static final Map<String, FourWeaponFoundationRules.Profile> PROFILES = FourWeaponFoundationProfiles.examples();

    private FourWeaponFoundationAdapter() {
    }

    public static String registryId(ItemStack itemStack) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation == null ? "" : resourceLocation.toString();
    }

    private static FourWeaponFoundationRules.Profile nativeProfile(ItemStack itemStack) {
        if (WeaponAttachmentPolicy.excluded(itemStack)) {
            return null;
        }
        if (itemStack.m_41619_() || itemStack.m_41613_() != 1) {
            return null;
        }
        FourWeaponFoundationRules.Profile profile = PROFILES.get(FourWeaponFoundationAdapter.registryId(itemStack));
        if (profile == null) {
            return null;
        }
        if (!FourWeaponFoundationAdapter.matchesType(itemStack, profile.family())) {
            return null;
        }
        try {
            FourWeaponFoundationRules.Profile profile2;
            Class<?> clazz = Class.forName("net.farmingtales.ftboss.api.SwordCavityExtensions");
            Object object = clazz.getMethod("foundationProfile", ItemStack.class, FourWeaponFoundationRules.Profile.class).invoke(null, itemStack, profile);
            return object instanceof FourWeaponFoundationRules.Profile ? (profile2 = (FourWeaponFoundationRules.Profile)object) : null;
        }
        catch (ClassNotFoundException classNotFoundException) {
            return profile;
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            return null;
        }
    }

    public static boolean matchesType(ItemStack itemStack, String string) {
        String string2;
        if (itemStack == null || itemStack.m_41619_()) {
            return false;
        }
        String string3 = FourWeaponFoundationAdapter.registryId(itemStack);
        FourWeaponFoundationRules.Profile profile = PROFILES.get(string3);
        if (profile == null || !profile.family().equals(string) || ForgeRegistries.ITEMS.getValue(ResourceLocation.m_135820_((String)string3)) != itemStack.m_41720_()) {
            return false;
        }
        switch (string3) {
            case "tide:starlight_bow": {
                String string4 = "com.li64.tide.registries.items.StarlightBowItem";
                break;
            }
            case "irons_spellbooks:autoloader_crossbow": {
                String string4 = "io.redspace.ironsspellbooks.item.weapons.AutoloaderCrossbow";
                break;
            }
            case "ftboss:barrel_crossbow": {
                String string4 = "net.farmingtales.ftboss.item.BarrelCrossbowItem";
                break;
            }
            case "armamentarium:ender_maiden_shortbow": {
                String string4 = "net.gqstavo.armamentarium.item.EnderMaidenShortbowItem";
                break;
            }
            case "cataclysm:cursed_bow": {
                String string4 = "com.github.L_Ender.cataclysm.items.Cursed_bow";
                break;
            }
            case "ftboss:qizai_prayer_staff": {
                String string4 = "net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem";
                break;
            }
            case "ftboss:frost_queen_staff": {
                String string4 = "net.farmingtales.ftboss.item.FrostQueenStaffItem";
                break;
            }
            case "armamentarium:mariana_scepter": {
                String string4 = "net.gqstavo.armamentarium.item.MarianaScepterItem";
                break;
            }
            case "irons_spellbooks:staff_of_the_nines": {
                String string4 = "io.redspace.ironsspellbooks.item.weapons.StaffOfTheNines";
                break;
            }
            case "legendary_monsters:wand_of_clouds": {
                String string4 = "net.miauczel.legendary_monsters.item.custom.WandOfCloudsItem";
                break;
            }
            case "cataclysm:wrath_of_the_desert": {
                String string4 = "com.github.L_Ender.cataclysm.items.Wrath_of_the_desert";
                break;
            }
            case "cataclysm:black_steel_axe": {
                String string4 = "net.minecraft.world.item.AxeItem";
                break;
            }
            default: {
                String string4 = string2 = "";
            }
        }
        if (!string2.isEmpty()) {
            return itemStack.m_41720_().getClass().getName().equals(string2);
        }
        return switch (string) {
            case "bow" -> itemStack.m_41720_() instanceof BowItem;
            case "crossbow" -> itemStack.m_41720_() instanceof CrossbowItem;
            case "axe" -> itemStack.m_41720_() instanceof AxeItem;
            case "trident" -> itemStack.m_41720_() instanceof TridentItem;
            case "staff" -> FourWeaponFoundationAdapter.castingItem(itemStack);
            default -> false;
        };
    }

    private static boolean castingItem(ItemStack itemStack) {
        for (Class<?> clazz = itemStack.m_41720_().getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            if (!clazz.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) continue;
            return true;
        }
        return false;
    }

    public static boolean applies(ItemStack itemStack) {
        if (AdditionalWeaponUiEligibility.keepLegacyUi(itemStack)) {
            return false;
        }
        if (WeaponAttachmentPolicy.excluded(itemStack)) {
            return false;
        }
        return !itemStack.m_41619_() && (PROFILES.containsKey(FourWeaponFoundationAdapter.registryId(itemStack)) || itemStack.m_41783_() != null && itemStack.m_41783_().m_128441_(ROOT));
    }

    public static boolean active(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack);
    }

    public static FourWeaponFoundationRules.Profile profile(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.read(itemStack).profile();
    }

    public static FourWeaponFoundationRules.Status status(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.read(itemStack).status();
    }

    public static int revision(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.read(itemStack).revision();
    }

    public static boolean blocksUse(ItemStack itemStack) {
        if (!FourWeaponFoundationAdapter.applies(itemStack)) {
            return false;
        }
        View view = FourWeaponFoundationAdapter.read(itemStack);
        return !view.valid() || !view.status().ready();
    }

    public static boolean cellUnlocked(ItemStack itemStack, int n) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        return view.valid() && FourWeaponFoundationRules.unlocked(view.profile(), n, view.level());
    }

    public static List<ItemStack> slots(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (int i = 0; i < 27; ++i) {
            Entry entry = view.entries().get(i);
            arrayList.add(view.valid() && entry != null && entry.paid() ? entry.storedItem() : ItemStack.f_41583_);
        }
        return List.copyOf(arrayList);
    }

    public static List<FoodWeaponBuildRules.Material> powerMaterials(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || !view.status().ready()) {
            return List.of();
        }
        ArrayList arrayList = new ArrayList();
        view.entries().forEach((n, entry) -> {
            if (entry.paid() && FourWeaponFoundationRules.unlocked(view.profile(), n, view.level()) && entry.value().material() != FoodWeaponBuildRules.Material.ORIGINAL) {
                arrayList.add(entry.value().material());
            }
        });
        return List.copyOf(arrayList);
    }

    public static FoodWeaponBuildRules.Build combat(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || !view.status().ready()) {
            return WeaponDurabilityBuild.combat(itemStack, IDENTITY);
        }
        FoodWeaponBuildRules.Build build = FourWeaponFoundationRules.combat(view.profile(), view.cells(), view.level());
        ArrayList arrayList = new ArrayList();
        view.entries().forEach((n, entry) -> {
            if (entry.paid() && FourWeaponFoundationRules.unlocked(view.profile(), n, view.level())) {
                arrayList.add(entry.storedItem());
            }
        });
        try {
            FoodWeaponBuildRules.Build build2;
            Class<?> clazz = Class.forName("net.farmingtales.ftboss.api.SwordCavityExtensions");
            Object object = clazz.getMethod("applyBuild", ItemStack.class, FoodWeaponBuildRules.Build.class, List.class, Boolean.TYPE).invoke(null, itemStack, build, List.copyOf(arrayList), true);
            return WeaponDurabilityBuild.combat(itemStack, object instanceof FoodWeaponBuildRules.Build ? (build2 = (FoodWeaponBuildRules.Build)object) : IDENTITY);
        }
        catch (ClassNotFoundException classNotFoundException) {
            return WeaponDurabilityBuild.combat(itemStack, build);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            return WeaponDurabilityBuild.combat(itemStack, IDENTITY);
        }
    }

    public static View read(ItemStack itemStack) {
        FourWeaponFoundationRules.Profile profile = null;
        try {
            CompoundTag compoundTag;
            boolean bl;
            profile = FourWeaponFoundationAdapter.nativeProfile(itemStack);
            FourWeaponFoundationAdapter.require(profile != null, "unapproved-registry-or-type");
            FoundationCanonicalDigest.canonical((Tag)itemStack.m_41739_(new CompoundTag()));
            FourWeaponFoundationAdapter.profileFingerprint(profile);
            FoundationAuthorRuleSnapshots.validate(itemStack);
            CompoundTag compoundTag2 = itemStack.m_41783_();
            if (compoundTag2 != null && compoundTag2.m_128441_(ROOT)) {
                FourWeaponFoundationAdapter.require(compoundTag2.m_128425_(ROOT, 10), "bad-root-type");
                return FourWeaponFoundationAdapter.parse(itemStack, profile, compoundTag2.m_128469_(ROOT));
            }
            int[] nArray = FourWeaponFoundationAdapter.progress(itemStack, true);
            HashMap<Integer, Entry> hashMap = new HashMap<Integer, Entry>();
            HashSet<Integer> hashSet = new HashSet<Integer>();
            boolean bl2 = bl = compoundTag2 != null && (compoundTag2.m_128441_(LEGACY) || compoundTag2.m_128441_(MATERIALS));
            if (bl && compoundTag2.m_128441_(MATERIALS)) {
                FourWeaponFoundationAdapter.require(compoundTag2.m_128425_(MATERIALS, 10), "bad-legacy-material-root");
            }
            if (bl && compoundTag2.m_128425_(MATERIALS, 10) && compoundTag2.m_128469_(MATERIALS).m_128441_("WearCarry")) {
                compoundTag = compoundTag2.m_128469_(MATERIALS);
                FourWeaponFoundationAdapter.require(compoundTag.m_128425_("WearCarry", 6) && Double.isFinite(compoundTag.m_128459_("WearCarry")) && compoundTag.m_128459_("WearCarry") >= 0.0 && compoundTag.m_128459_("WearCarry") < 1.0, "bad-legacy-wear-carry");
            }
            if (bl && compoundTag2.m_128441_(LEGACY)) {
                FourWeaponFoundationAdapter.migrateCells(itemStack, profile, hashMap, hashSet);
            } else {
                if (bl) {
                    FourWeaponFoundationAdapter.require(compoundTag2.m_128425_(MATERIALS, 10), "bad-legacy-material-root");
                    compoundTag = compoundTag2.m_128469_(MATERIALS);
                    for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
                        if (!compoundTag.m_128441_(part.name())) continue;
                        FourWeaponFoundationAdapter.require(compoundTag.m_128425_(part.name(), 8), "bad-legacy-material");
                        FourWeaponFoundationAdapter.require(compoundTag.m_128461_(part.name()).equals(FoodWeaponBuildRules.Material.ORIGINAL.id()), "legacy-paid-item-missing");
                    }
                }
                if (bl) {
                    hashSet.addAll(profile.nativeCells().keySet());
                } else {
                    profile.nativeCells().forEach((n, cell) -> hashMap.put((Integer)n, new Entry((FourWeaponFoundationRules.Cell)cell, null)));
                }
            }
            int n2 = 0;
            if (bl && compoundTag2.m_128425_(LEGACY, 10)) {
                n2 = FourWeaponFoundationAdapter.integer(compoundTag2.m_128469_(LEGACY), "Revision", 0, false);
            }
            FourWeaponFoundationAdapter.require(n2 >= 0 && n2 < Integer.MAX_VALUE, "bad-legacy-revision");
            return new View(profile, hashMap, hashSet, null, nArray[0], nArray[1], n2, "", bl);
        }
        catch (LinkageError | RuntimeException throwable) {
            String string = throwable.getMessage();
            return new View(profile, Map.of(), Set.of(), null, 0, 0, -2, string == null || string.isBlank() ? "invalid-foundation-input" : string, false);
        }
    }

    private static int[] progress(ItemStack itemStack, boolean bl) {
        CompoundTag compoundTag = itemStack.m_41783_();
        if (compoundTag == null || !compoundTag.m_128441_(EVOLUTION)) {
            return new int[]{0, 0};
        }
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_(EVOLUTION, 10), "bad-evolution-root");
        CompoundTag compoundTag2 = compoundTag.m_128469_(EVOLUTION);
        int n = FourWeaponFoundationAdapter.integer(compoundTag2, "Level", 0, false);
        int n2 = FourWeaponFoundationAdapter.integer(compoundTag2, "Experience", 0, false);
        int n3 = FourWeaponFoundationAdapter.integer(compoundTag2, "ProgressionVersion", 0, false);
        FourWeaponFoundationAdapter.require(n >= 0 && n <= 8 && n2 >= 0 && n3 >= 0 && n3 <= 2, "bad-or-future-progression");
        if (!bl) {
            FourWeaponFoundationAdapter.require(n <= 4 && n3 == 2, "unreconciled-progression");
        }
        if (bl && n3 < 2) {
            int n4 = n == 0 ? 0 : FoodWeaponBuildRules.evolutionCost(n - 1);
            n2 = Math.max(0, n2 - n4);
        }
        n2 = (n = Math.min(n, 4)) == 4 ? 0 : Math.min(n2, FoodWeaponBuildRules.evolutionCost(n));
        return new int[]{n, n2};
    }

    private static View parse(ItemStack itemStack, FourWeaponFoundationRules.Profile profile, CompoundTag compoundTag) {
        Object object;
        Object object22;
        FourWeaponFoundationAdapter.require(FourWeaponFoundationAdapter.integer(compoundTag, "Schema", 0, true) == 2, "bad-or-future-schema");
        FourWeaponFoundationAdapter.require(FourWeaponFoundationAdapter.integer(compoundTag, "DigestVersion", 0, true) == 3, "unsupported-digest-version");
        FourWeaponFoundationAdapter.require(FourWeaponFoundationAdapter.integer(compoundTag, "ProfileRevision", 0, true) == 1, "bad-or-future-profile");
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_("Profile", 8) && compoundTag.m_128461_("Profile").equals(profile.id()), "profile-mismatch");
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_("Rules", 8) && FoundationProfileFingerprints.matches(compoundTag.m_128461_("Rules"), profile), "profile-rules-changed");
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_("Initialized", 1) && compoundTag.m_128445_("Initialized") == 1, "uninitialized-root");
        int n = FourWeaponFoundationAdapter.integer(compoundTag, "Revision", -1, true);
        FourWeaponFoundationAdapter.require(n >= 0 && n < Integer.MAX_VALUE, "bad-revision");
        int[] nArray = FourWeaponFoundationAdapter.progress(itemStack, false);
        int n2 = FourWeaponFoundationAdapter.integer(compoundTag, "ReconciledLevel", -1, true);
        FourWeaponFoundationAdapter.require(n2 >= 0 && n2 <= 4, "bad-reconciled-level");
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_("Cells", 9), "bad-cells-type");
        ListTag listTag = (ListTag)compoundTag.m_128423_("Cells");
        FourWeaponFoundationAdapter.require(listTag.size() <= 27 && (listTag.isEmpty() || listTag.m_7264_() == 10), "bad-cells-list");
        HashMap<Integer, Entry> hashMap = new HashMap<Integer, Entry>();
        for (Object object22 : listTag) {
            Object object3;
            object = (CompoundTag)object22;
            int n3 = FourWeaponFoundationAdapter.integer((CompoundTag)object, "Slot", -1, true);
            FourWeaponFoundationAdapter.require(n3 >= 0 && n3 < 27 && !hashMap.containsKey(n3), "duplicate-or-out-of-range-slot");
            FourWeaponFoundationAdapter.require(object.m_128425_("Source", 8), "bad-source");
            String string = object.m_128461_("Source");
            if (string.equals("native-reference")) {
                FourWeaponFoundationAdapter.require(!object.m_128441_("Item"), "reference-has-refundable-payload");
                object3 = profile.nativeCells().get(n3);
                FourWeaponFoundationAdapter.require(object3 != null && object.m_128425_("Id", 8) && ((FourWeaponFoundationRules.Cell)object3).itemId().equals(object.m_128461_("Id")) && object.m_128425_("Material", 8) && ((FourWeaponFoundationRules.Cell)object3).material().id().equals(object.m_128461_("Material")), "bad-reference");
                hashMap.put(n3, new Entry((FourWeaponFoundationRules.Cell)object3, null));
                continue;
            }
            FourWeaponFoundationAdapter.require(string.equals("player-paid") && object.m_128425_("Item", 10), "bad-paid-cell");
            object3 = FourWeaponFoundationAdapter.paid(profile, object.m_128469_("Item"));
            FourWeaponFoundationAdapter.require(object.m_128425_("Id", 8) && ((Entry)object3).value().itemId().equals(object.m_128461_("Id")), "paid-id-mismatch");
            FourWeaponFoundationAdapter.require(object.m_128425_("Material", 8) && ((Entry)object3).value().material().id().equals(object.m_128461_("Material")), "material-mismatch");
            hashMap.put(n3, (Entry)object3);
        }
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_("RetiredNative", 9), "bad-retirement-type");
        Iterator iterator = (ListTag)compoundTag.m_128423_("RetiredNative");
        FourWeaponFoundationAdapter.require(iterator.size() <= 27 && (iterator.isEmpty() || iterator.m_7264_() == 3), "bad-retirement-list");
        object22 = new HashSet();
        object = iterator.iterator();
        while (object.hasNext()) {
            Tag tag = (Tag)object.next();
            int n4 = ((IntTag)tag).m_7047_();
            FourWeaponFoundationAdapter.require(n4 >= 0 && n4 < 27 && object22.add(n4) && (!hashMap.containsKey(n4) || ((Entry)hashMap.get(n4)).paid()), "bad-retirement-entry");
        }
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_("LegacyBackup", 10), "missing-full-backup");
        if (compoundTag.m_128441_("WearCarry")) {
            FourWeaponFoundationAdapter.require(compoundTag.m_128425_("WearCarry", 6) && Double.isFinite(compoundTag.m_128459_("WearCarry")) && compoundTag.m_128459_("WearCarry") >= 0.0 && compoundTag.m_128459_("WearCarry") < 1.0, "bad-wear-carry");
        }
        return new View(profile, hashMap, (Set<Integer>)object22, compoundTag, nArray[0], nArray[1], n, "", false);
    }

    private static Map<Integer, FourWeaponFoundationRules.Values> paidValues(FourWeaponFoundationRules.Profile profile, Map<Integer, Entry> map) {
        HashMap hashMap = new HashMap();
        map.forEach((n, entry) -> {
            if (!entry.paid() || profile.items().containsKey(entry.value().itemId())) {
                return;
            }
            Optional<SwordMaterialDefinition> optional = SwordMaterialExtensions.resolve(entry.storedItem());
            if (optional.isPresent() && optional.get().categories().contains((Object)SwordMaterialDefinition.Category.FOOD)) {
                SwordFunctionRules.Values values = optional.get().functions();
                hashMap.put(n, new FourWeaponFoundationRules.Values(values.handle(), values.guard(), values.core(), values.stability(), values.blade()));
            }
        });
        return Map.copyOf(hashMap);
    }

    private static Entry paid(FourWeaponFoundationRules.Profile profile, CompoundTag compoundTag) {
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_("id", 8) && (compoundTag.m_128425_("Count", 1) || compoundTag.m_128425_("Count", 3)) && compoundTag.m_128451_("Count") == 1, "paid-count-not-one");
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)compoundTag.m_128461_("id"));
        FourWeaponFoundationAdapter.require(resourceLocation != null && ForgeRegistries.ITEMS.containsKey(resourceLocation) && !resourceLocation.toString().equals("minecraft:air"), "unknown-item");
        FourWeaponFoundationAdapter.require(!compoundTag.m_128441_("tag") || compoundTag.m_128425_("tag", 10), "bad-item-tag");
        FourWeaponFoundationAdapter.require(!compoundTag.m_128441_("ForgeCaps") || compoundTag.m_128425_("ForgeCaps", 10), "bad-item-caps");
        ItemStack itemStack = ItemStack.m_41712_((CompoundTag)compoundTag.m_6426_());
        FourWeaponFoundationAdapter.require(!itemStack.m_41619_() && itemStack.m_41613_() == 1 && FourWeaponFoundationAdapter.registryId(itemStack).equals(resourceLocation.toString()) && !FourWeaponFoundationAdapter.applies(itemStack), "invalid-or-nested-weapon");
        FoodWeaponBuildRules.Material material = FourWeaponFoundationAdapter.resolveMaterial(itemStack);
        if (itemStack.m_41763_()) {
            material = FoodWeaponBuildRules.Material.ORIGINAL;
        }
        FourWeaponFoundationAdapter.require(profile.items().containsKey(resourceLocation.toString()) || material != FoodWeaponBuildRules.Material.ORIGINAL && profile.materials().containsKey((Object)material), "unsupported-paid-material");
        FourWeaponFoundationRules.Cell cell = new FourWeaponFoundationRules.Cell(resourceLocation.toString(), material, "player-paid", FourWeaponFoundationAdapter.canonical((Tag)compoundTag));
        return new Entry(cell, compoundTag);
    }

    /*
     * WARNING - void declaration
     */
    private static void migrateCells(ItemStack itemStack, FourWeaponFoundationRules.Profile profile, Map<Integer, Entry> map, Set<Integer> set) {
        String string;
        Object object;
        CompoundTag compoundTag = itemStack.m_41783_();
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_(LEGACY, 10), "bad-legacy-root");
        CompoundTag compoundTag2 = compoundTag.m_128469_(LEGACY);
        FourWeaponFoundationAdapter.require(FourWeaponFoundationAdapter.integer(compoundTag2, "Version", 0, true) == 2, "unsupported-legacy-version");
        FourWeaponFoundationAdapter.require(compoundTag2.m_128425_("Cells", 10), "bad-legacy-cells");
        CompoundTag compoundTag3 = compoundTag2.m_128469_("Cells");
        HashSet<String> hashSet = new HashSet<String>();
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            hashSet.add(slot.name());
        }
        FourWeaponFoundationAdapter.require(hashSet.containsAll(compoundTag3.m_128431_()), "unknown-legacy-slot");
        if (compoundTag.m_128425_(MATERIALS, 10)) {
            void compoundTag5;
            CompoundTag compoundTag4 = compoundTag.m_128469_(MATERIALS);
            SwordCavityRules.Slot[] slotArray = SwordCavityRules.Slot.values();
            int n = slotArray.length;
            boolean i = false;
            while (compoundTag5 < n) {
                SwordCavityRules.Slot n2 = slotArray[compoundTag5];
                if (n2.legacyPart != null && compoundTag4.m_128441_(n2.legacyPart.name())) {
                    FourWeaponFoundationAdapter.require(compoundTag4.m_128425_(n2.legacyPart.name(), 8), "bad-legacy-material-type");
                    object = compoundTag4.m_128461_(n2.legacyPart.name());
                    if (!((String)object).equals(FoodWeaponBuildRules.Material.ORIGINAL.id())) {
                        string = compoundTag3.m_128469_(n2.name());
                        FourWeaponFoundationAdapter.require(string.m_128461_("State").equals("installed") && ((String)object).equals(string.m_128461_("Material")) && string.m_128425_("Item", 10), "conflicting-legacy-ledgers");
                    }
                }
                ++compoundTag5;
            }
        }
        block13: for (CompoundTag compoundTag4 : SwordCavityRules.Slot.values()) {
            int n = compoundTag4.ordinal();
            if (!compoundTag3.m_128441_(compoundTag4.name())) {
                if (!profile.nativeCells().containsKey(n)) continue;
                set.add(n);
                continue;
            }
            FourWeaponFoundationAdapter.require(compoundTag3.m_128425_(compoundTag4.name(), 10), "bad-legacy-entry");
            object = compoundTag3.m_128469_(compoundTag4.name());
            FourWeaponFoundationAdapter.require(object.m_128425_("State", 8), "bad-legacy-state");
            switch (string = object.m_128461_("State")) {
                case "installed": {
                    FourWeaponFoundationAdapter.require(object.m_128425_("Item", 10) && object.m_128425_("Material", 8), "legacy-paid-item-missing");
                    Object object2 = FourWeaponFoundationAdapter.paid(profile, object.m_128469_("Item"));
                    FourWeaponFoundationAdapter.require(((Entry)object2).value().material().id().equals(object.m_128461_("Material")), "legacy-material-mismatch");
                    map.put(n, (Entry)object2);
                    if (!profile.nativeCells().containsKey(n)) continue block13;
                    set.add(n);
                    continue block13;
                }
                case "native": {
                    FourWeaponFoundationAdapter.require(!object.m_128441_("Item"), "legacy-native-has-item");
                    Object object2 = profile.nativeCells().get(n);
                    if (object2 == null) continue block13;
                    map.put(n, new Entry((FourWeaponFoundationRules.Cell)object2, null));
                    continue block13;
                }
                case "empty": 
                case "missing": {
                    FourWeaponFoundationAdapter.require(!object.m_128441_("Item"), "legacy-hidden-paid-item");
                    if (!profile.nativeCells().containsKey(n)) continue block13;
                    set.add(n);
                    continue block13;
                }
                default: {
                    throw new IllegalArgumentException("bad-legacy-state");
                }
            }
        }
    }

    public static Change prepareEdit(ItemStack itemStack, int n, ItemStack itemStack2, int n2) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || view.revision() != n2 || n < 0 || n >= 27 || itemStack2 == itemStack || !itemStack2.m_41619_() && !FourWeaponFoundationAdapter.cellUnlocked(itemStack, n)) {
            return null;
        }
        try {
            ItemStack itemStack3;
            HashMap<Integer, Entry> hashMap = new HashMap<Integer, Entry>(view.entries());
            HashSet<Integer> hashSet = new HashSet<Integer>(view.retired());
            Entry entry = (Entry)hashMap.remove(n);
            Entry entry2 = null;
            if (!itemStack2.m_41619_()) {
                itemStack3 = itemStack2.m_41777_();
                itemStack3.m_41764_(1);
                entry2 = FourWeaponFoundationAdapter.paid(view.profile(), itemStack3.m_41739_(new CompoundTag()));
                hashMap.put(n, entry2);
            }
            if (entry == null && entry2 == null) {
                return null;
            }
            if (entry != null && !entry.paid()) {
                hashSet.add(n);
            }
            itemStack3 = FourWeaponFoundationAdapter.persist(itemStack, view, hashMap, hashSet, view.level(), view.experience(), true);
            return new Change(FourWeaponFoundationAdapter.fingerprint(itemStack), itemStack3, entry != null && entry.paid() ? entry.storedItem() : ItemStack.f_41583_, entry2 == null ? 0 : 1);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    public static ItemStack preview(ItemStack itemStack, int n, ItemStack itemStack2) {
        Change change = FourWeaponFoundationAdapter.prepareEdit(itemStack, n, itemStack2, FourWeaponFoundationAdapter.revision(itemStack));
        return change == null ? itemStack.m_41777_() : change.after();
    }

    private static ItemStack persist(ItemStack itemStack, View view, Map<Integer, Entry> map, Set<Integer> set, int n, int n2, boolean bl) {
        CompoundTag compoundTag;
        CompoundTag compoundTag2;
        ItemStack itemStack2 = itemStack.m_41777_();
        CompoundTag compoundTag3 = view.rootCopy();
        if (compoundTag3 == null) {
            compoundTag3 = new CompoundTag();
            compoundTag3.m_128365_("LegacyBackup", (Tag)itemStack.m_41739_(new CompoundTag()).m_6426_());
            compoundTag2 = itemStack.m_41737_(MATERIALS);
            if (compoundTag2 != null && compoundTag2.m_128441_("WearCarry")) {
                FourWeaponFoundationAdapter.require(compoundTag2.m_128425_("WearCarry", 6) && Double.isFinite(compoundTag2.m_128459_("WearCarry")) && compoundTag2.m_128459_("WearCarry") >= 0.0 && compoundTag2.m_128459_("WearCarry") < 1.0, "bad-legacy-wear-carry");
                compoundTag3.m_128347_("WearCarry", compoundTag2.m_128459_("WearCarry"));
            }
        }
        compoundTag3.m_128405_("Schema", 2);
        compoundTag3.m_128405_("DigestVersion", 3);
        compoundTag3.m_128405_("ProfileRevision", 1);
        compoundTag3.m_128359_("Profile", view.profile().id());
        compoundTag3.m_128359_("Rules", FourWeaponFoundationAdapter.profileFingerprint(view.profile()));
        compoundTag3.m_128379_("Initialized", true);
        FourWeaponFoundationAdapter.require(!bl || view.revision() < Integer.MAX_VALUE, "revision-exhausted");
        compoundTag3.m_128405_("Revision", Math.max(0, view.revision()) + (bl ? 1 : 0));
        compoundTag3.m_128405_("ReconciledLevel", n);
        compoundTag2 = new ListTag();
        ListTag listTag = new TreeSet<Integer>(map.keySet()).iterator();
        while (listTag.hasNext()) {
            int n3 = listTag.next();
            Entry entry = map.get(n3);
            compoundTag = new CompoundTag();
            compoundTag.m_128405_("Slot", n3);
            compoundTag.m_128359_("Source", entry.value().provenance());
            compoundTag.m_128359_("Id", entry.value().itemId());
            compoundTag.m_128359_("Material", entry.value().material().id());
            if (entry.paid()) {
                compoundTag.m_128365_("Item", (Tag)entry.payload());
            }
            compoundTag2.add((Object)compoundTag);
        }
        compoundTag3.m_128365_("Cells", (Tag)compoundTag2);
        listTag = new ListTag();
        CompoundTag compoundTag4 = new TreeSet<Integer>(set).iterator();
        while (compoundTag4.hasNext()) {
            int n4 = compoundTag4.next();
            listTag.add((Object)IntTag.m_128679_((int)n4));
        }
        compoundTag3.m_128365_("RetiredNative", (Tag)listTag);
        itemStack2.m_41784_().m_128365_(ROOT, (Tag)compoundTag3);
        compoundTag4 = itemStack2.m_41737_(MATERIALS);
        if (compoundTag4 != null) {
            compoundTag4.m_128473_("WearCarry");
        }
        CompoundTag compoundTag5 = itemStack2.m_41698_(EVOLUTION);
        compoundTag5.m_128405_("Level", n);
        compoundTag5.m_128405_("Experience", n2);
        compoundTag5.m_128405_("ProgressionVersion", 2);
        if (view.root == null && compoundTag5.m_128425_("StageHistory", 9)) {
            compoundTag = (ListTag)compoundTag5.m_128423_("StageHistory");
            FourWeaponFoundationAdapter.require(compoundTag.isEmpty() || compoundTag.m_7264_() == 10, "bad-legacy-history");
            while (compoundTag.size() > n) {
                compoundTag.remove(compoundTag.size() - 1);
            }
            if (compoundTag5.m_128425_("SelectedStage", 3)) {
                compoundTag5.m_128405_("SelectedStage", Math.max(0, Math.min(n - 1, compoundTag5.m_128451_("SelectedStage"))));
            }
        }
        FoundationCanonicalDigest.canonical((Tag)itemStack2.m_41739_(new CompoundTag()));
        return itemStack2;
    }

    public static boolean initializeForServerEdit(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid()) {
            return false;
        }
        if (view.persisted()) {
            return true;
        }
        try {
            ItemStack itemStack2 = FourWeaponFoundationAdapter.persist(itemStack, view, view.entries(), view.retired(), view.level(), view.experience(), true);
            itemStack.m_41751_(itemStack2.m_41783_().m_6426_());
            return true;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    public static boolean reconcileLevel(ItemStack itemStack, int n, int n2) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || !view.persisted() || n < 0 || n > 4 || n2 < 0 || view.level() != n || view.experience() != n2) {
            return false;
        }
        CompoundTag compoundTag = view.rootCopy();
        if (compoundTag.m_128451_("ReconciledLevel") == n) {
            return true;
        }
        ItemStack itemStack2 = FourWeaponFoundationAdapter.persist(itemStack, view, view.entries(), view.retired(), n, n2, true);
        itemStack.m_41751_(itemStack2.m_41783_().m_6426_());
        return true;
    }

    public static boolean replace(ServerPlayer serverPlayer, ItemStack itemStack, int n, int n2, int n3) {
        int n4;
        ItemStack itemStack2;
        if (serverPlayer.m_5833_() || !serverPlayer.m_6084_() || serverPlayer.m_9236_().f_46443_ || itemStack.m_41613_() != 1 || serverPlayer.m_21205_() != itemStack && serverPlayer.m_21206_() != itemStack || n2 < -1 || n2 >= 36) {
            return false;
        }
        ItemStack itemStack3 = itemStack2 = n2 < 0 ? ItemStack.f_41583_ : serverPlayer.m_150109_().m_8020_(n2);
        if (n2 >= 0 && itemStack2.m_41619_() || itemStack2 == itemStack) {
            return false;
        }
        Change change = FourWeaponFoundationAdapter.prepareEdit(itemStack, n, itemStack2, n3);
        if (change == null) {
            return false;
        }
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        ArrayList<ItemStack> arrayList2 = new ArrayList<ItemStack>();
        int n5 = -1;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack4 = serverPlayer.m_150109_().m_8020_(i);
            if (itemStack4 == itemStack) {
                n5 = i;
            }
            arrayList.add(itemStack4.m_41777_());
            arrayList2.add(itemStack4.m_41777_());
        }
        if (change.consume() != 0) {
            ((ItemStack)arrayList2.get(n2)).m_41774_(change.consume());
        }
        if (!FourWeaponFoundationAdapter.fit(arrayList2, change.refund(), n5)) {
            return false;
        }
        ItemStack itemStack5 = change.after();
        if (itemStack.m_41763_() && itemStack.m_41776_() > 0) {
            int n6 = itemStack.m_41776_();
            n4 = itemStack5.m_41776_();
            if (n4 <= 0) {
                return false;
            }
            if (itemStack.m_41773_() >= n4) {
                return false;
            }
            itemStack5.m_41784_().m_128405_("Damage", itemStack.m_41773_());
        }
        if (!FourWeaponFoundationAdapter.fingerprint(itemStack).equals(change.before())) {
            return false;
        }
        for (int i = 0; i < 36; ++i) {
            if (FourWeaponFoundationAdapter.fingerprint(serverPlayer.m_150109_().m_8020_(i)).equals(FourWeaponFoundationAdapter.fingerprint((ItemStack)arrayList.get(i)))) continue;
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41783_() == null ? null : itemStack.m_41783_().m_6426_();
        try {
            itemStack.m_41751_(itemStack5.m_41783_().m_6426_());
            NativeFoodWeaponWear.manage(serverPlayer, itemStack);
            NativeFoodWeaponWear.componentsChanged(itemStack);
            for (n4 = 0; n4 < 36; ++n4) {
                if (n4 == n5) continue;
                serverPlayer.m_150109_().m_6836_(n4, (ItemStack)arrayList2.get(n4));
            }
            serverPlayer.m_150109_().m_6596_();
            serverPlayer.f_36096_.m_38946_();
            return true;
        }
        catch (RuntimeException runtimeException) {
            itemStack.m_41751_(compoundTag);
            for (int i = 0; i < 36; ++i) {
                if (i == n5) continue;
                serverPlayer.m_150109_().m_6836_(i, (ItemStack)arrayList.get(i));
            }
            return false;
        }
    }

    private static boolean fit(List<ItemStack> list, ItemStack itemStack, int n) {
        int n2;
        if (itemStack.m_41619_()) {
            return true;
        }
        for (n2 = 0; n2 < list.size(); ++n2) {
            if (n2 == n) continue;
            ItemStack itemStack2 = list.get(n2);
            CompoundTag compoundTag = itemStack2.m_41739_(new CompoundTag());
            CompoundTag compoundTag2 = itemStack.m_41739_(new CompoundTag());
            compoundTag.m_128473_("Count");
            compoundTag2.m_128473_("Count");
            if (itemStack2.m_41619_() || !compoundTag.equals((Object)compoundTag2) || itemStack2.m_41613_() >= itemStack2.m_41741_()) continue;
            itemStack2.m_41769_(1);
            return true;
        }
        for (n2 = 0; n2 < list.size(); ++n2) {
            if (n2 == n || !list.get(n2).m_41619_()) continue;
            list.set(n2, itemStack.m_41777_());
            return true;
        }
        return false;
    }

    public static String rulesFingerprint(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid()) {
            return null;
        }
        try {
            ArrayList<Map<String, Optional<SwordMaterialDefinition>>> arrayList = new ArrayList<Map<String, Optional<SwordMaterialDefinition>>>();
            for (int n : new TreeSet<Integer>(view.entries().keySet())) {
                Entry entry = view.entries().get(n);
                if (!entry.paid()) continue;
                arrayList.add(Map.of("Slot", n, "Definition", SwordMaterialExtensions.resolve(entry.storedItem())));
            }
            return FoundationAuthorRuleSnapshots.fingerprint(itemStack, FourWeaponFoundationAdapter.profileFingerprint(view.profile()), FoundationCanonicalDigest.object(arrayList));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    private static String profileFingerprint(FourWeaponFoundationRules.Profile profile) {
        return FoundationProfileFingerprints.fingerprint(profile);
    }

    private static FoodWeaponBuildRules.Material resolveMaterial(ItemStack itemStack) {
        try {
            Class<?> clazz = Class.forName("net.farmingtales.ftboss.api.SwordMaterialExtensions");
            Object object = clazz.getMethod("baseMaterial", ItemStack.class).invoke(null, itemStack);
            FourWeaponFoundationAdapter.require(object instanceof FoodWeaponBuildRules.Material, "bad-author-material");
            return (FoodWeaponBuildRules.Material)((Object)object);
        }
        catch (ClassNotFoundException classNotFoundException) {
            return WeaponMaterialCatalogRules.resolve(FourWeaponFoundationAdapter.registryId(itemStack), itemStack.m_204131_().map(tagKey -> tagKey.f_203868_().toString()).toList());
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            throw new IllegalArgumentException("unavailable-author-material", throwable);
        }
    }

    public static String fingerprint(ItemStack itemStack) {
        return FoundationCanonicalDigest.fingerprint("full-stack", (Tag)itemStack.m_41739_(new CompoundTag()));
    }

    public static String compositionFingerprint(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || !view.status().ready() || FourWeaponUseGuard.blocksUse(itemStack)) {
            return null;
        }
        try {
            String string = FourWeaponFoundationAdapter.rulesFingerprint(itemStack);
            if (string == null) {
                return null;
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128359_("Rules", string);
            compoundTag.m_128405_("Level", view.level());
            compoundTag.m_128365_("Retired", (Tag)FoundationCanonicalDigest.object(view.retired()));
            ListTag listTag = new ListTag();
            for (int n : new TreeSet<Integer>(view.entries().keySet())) {
                Entry entry = view.entries().get(n);
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.m_128405_("Slot", n);
                compoundTag2.m_128365_("Value", (Tag)FoundationCanonicalDigest.object(entry.value()));
                if (entry.paid()) {
                    compoundTag2.m_128365_("Item", (Tag)entry.payload());
                }
                listTag.add((Object)compoundTag2);
            }
            compoundTag.m_128365_("Cells", (Tag)listTag);
            return FoundationCanonicalDigest.fingerprint("composition", (Tag)compoundTag);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    public static CompoundTag compositionSnapshot(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("DigestVersion", 3);
        compoundTag.m_128359_("ItemId", FourWeaponFoundationAdapter.registryId(itemStack));
        compoundTag.m_128379_("Valid", view.valid());
        compoundTag.m_128379_("BlocksUse", FourWeaponUseGuard.blocksUse(itemStack));
        compoundTag.m_128359_("Failure", view.failure());
        compoundTag.m_128405_("Level", view.level());
        compoundTag.m_128405_("Revision", view.revision());
        compoundTag.m_128365_("FullStack", (Tag)itemStack.m_41739_(new CompoundTag()).m_6426_());
        if (view.persisted()) {
            compoundTag.m_128365_("Foundation", (Tag)view.rootCopy());
        }
        if (view.valid()) {
            String string;
            compoundTag.m_128359_("Profile", view.profile().id());
            compoundTag.m_128359_("Family", view.profile().family());
            String string2 = FourWeaponFoundationAdapter.rulesFingerprint(itemStack);
            if (string2 != null) {
                compoundTag.m_128359_("Rules", string2);
            }
            if ((string = FourWeaponFoundationAdapter.compositionFingerprint(itemStack)) != null) {
                compoundTag.m_128359_("Composition", string);
            }
            ListTag listTag = new ListTag();
            for (int n : new TreeSet<Integer>(view.entries().keySet())) {
                Entry entry = view.entries().get(n);
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.m_128405_("Slot", n);
                compoundTag2.m_128379_("Unlocked", FourWeaponFoundationRules.unlocked(view.profile(), n, view.level()));
                compoundTag2.m_128359_("Source", entry.value().provenance());
                compoundTag2.m_128359_("Id", entry.value().itemId());
                compoundTag2.m_128359_("Material", entry.value().material().id());
                if (entry.paid()) {
                    compoundTag2.m_128365_("Item", (Tag)entry.payload());
                }
                listTag.add((Object)compoundTag2);
            }
            compoundTag.m_128365_("Cells", (Tag)listTag);
        }
        return compoundTag;
    }

    public static boolean canEvolve(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        return view.valid() && view.level() < 4 && view.status().ready();
    }

    public static boolean containerEditable(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.read(itemStack).valid();
    }

    static ItemStack nativeWearCandidate(ItemStack itemStack, int n) {
        String string;
        View view = FourWeaponFoundationAdapter.read(itemStack);
        Entry entry = view.entries().get(n);
        if (!(view.valid() && view.persisted() && view.status().ready() && entry != null && entry.paid() && n != 13 && view.revision() >= 0 && view.revision() != Integer.MAX_VALUE)) {
            return null;
        }
        for (int i = 0; i < view.profile().axes().size(); ++i) {
            string = view.profile().axes().get(i).toLowerCase(Locale.ROOT);
            if (!string.contains("core") || !(view.profile().weights().get(n).at(i) >= 1.0)) continue;
            return null;
        }
        HashMap<Integer, Entry> hashMap = new HashMap<Integer, Entry>(view.entries());
        hashMap.remove(n);
        string = FourWeaponFoundationAdapter.persist(itemStack, view, hashMap, view.retired(), view.level(), view.experience(), true);
        View view2 = FourWeaponFoundationAdapter.read((ItemStack)string);
        return view2.valid() && view2.status().ready() ? string : null;
    }

    public static boolean losePart(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || view.entries().isEmpty()) {
            return false;
        }
        try {
            int n2 = view.entries().keySet().stream().filter(n -> view.entries().get(n).paid()).mapToInt(Integer::intValue).max().orElse(-1);
            if (n2 < 0) {
                return false;
            }
            HashMap<Integer, Entry> hashMap = new HashMap<Integer, Entry>(view.entries());
            HashSet<Integer> hashSet = new HashSet<Integer>(view.retired());
            Entry entry = (Entry)hashMap.remove(n2);
            if (!entry.paid()) {
                hashSet.add(n2);
            }
            ItemStack itemStack2 = FourWeaponFoundationAdapter.persist(itemStack, view, hashMap, hashSet, view.level(), view.experience(), true);
            itemStack.m_41751_(itemStack2.m_41783_().m_6426_());
            return true;
        }
        catch (LinkageError | RuntimeException throwable) {
            return false;
        }
    }

    public static boolean resetWearAfterRepair(ItemStack itemStack) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid()) {
            return false;
        }
        try {
            ItemStack itemStack2 = FourWeaponFoundationAdapter.persist(itemStack, view, view.entries(), view.retired(), view.level(), view.experience(), true);
            itemStack2.m_41737_(ROOT).m_128347_("WearCarry", 0.0);
            CompoundTag compoundTag = itemStack2.m_41737_(MATERIALS);
            if (compoundTag != null) {
                compoundTag.m_128473_("WearCarry");
            }
            FoundationCanonicalDigest.canonical((Tag)itemStack2.m_41739_(new CompoundTag()));
            itemStack.m_41751_(itemStack2.m_41783_().m_6426_());
            return true;
        }
        catch (LinkageError | RuntimeException throwable) {
            return false;
        }
    }

    public static int durabilityCost(ItemStack itemStack, int n) {
        if (n <= 0 || !itemStack.m_41763_()) {
            return n;
        }
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || !view.persisted()) {
            return n;
        }
        FoodWeaponBuildRules.Wear wear = FoodWeaponBuildRules.wear(n, FourWeaponFoundationAdapter.combat(itemStack).wear(), view.root.m_128459_("WearCarry"));
        itemStack.m_41737_(ROOT).m_128347_("WearCarry", wear.carry());
        return wear.damage();
    }

    public static boolean reference(ItemStack itemStack, int n) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        Entry entry = view.entries().get(n);
        return view.valid() && entry != null && !entry.paid();
    }

    public static ItemStack containerPreview(ItemStack itemStack, List<ItemStack> list) {
        View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid() || list == null || list.size() != 27) {
            return null;
        }
        try {
            HashMap<Integer, Entry> hashMap = new HashMap<Integer, Entry>(view.entries());
            HashSet<Integer> hashSet = new HashSet<Integer>(view.retired());
            boolean bl = false;
            for (int i = 0; i < 27; ++i) {
                Entry entry = (Entry)hashMap.get(i);
                ItemStack itemStack2 = entry != null && entry.paid() ? entry.storedItem() : ItemStack.f_41583_;
                ItemStack itemStack3 = list.get(i);
                if (itemStack3 == null || itemStack3 == itemStack || !itemStack3.m_41619_() && itemStack3.m_41613_() != 1) {
                    return null;
                }
                if (FourWeaponFoundationAdapter.fingerprint(itemStack2).equals(FourWeaponFoundationAdapter.fingerprint(itemStack3))) continue;
                if (!itemStack3.m_41619_() && !FourWeaponFoundationRules.unlocked(view.profile(), i, view.level())) {
                    return null;
                }
                if (entry != null && !entry.paid()) {
                    hashSet.add(i);
                }
                hashMap.remove(i);
                if (!itemStack3.m_41619_()) {
                    hashMap.put(i, FourWeaponFoundationAdapter.paid(view.profile(), itemStack3.m_41739_(new CompoundTag())));
                }
                bl = true;
            }
            return bl ? FourWeaponFoundationAdapter.persist(itemStack, view, hashMap, hashSet, view.level(), view.experience(), true) : itemStack.m_41777_();
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    public static boolean canSetContainerItems(ItemStack itemStack, List<ItemStack> list) {
        ItemStack itemStack2 = FourWeaponFoundationAdapter.containerPreview(itemStack, list);
        return itemStack2 != null && FourWeaponFoundationAdapter.preserveDamage(itemStack, itemStack2);
    }

    public static boolean setContainerItems(ServerPlayer serverPlayer, ItemStack itemStack, List<ItemStack> list, int n) {
        if (serverPlayer.m_5833_() || !serverPlayer.m_6084_() || serverPlayer.m_9236_().f_46443_ || FourWeaponFoundationAdapter.revision(itemStack) != n || itemStack != serverPlayer.m_21205_() && itemStack != serverPlayer.m_21206_()) {
            return false;
        }
        ItemStack itemStack2 = FourWeaponFoundationAdapter.containerPreview(itemStack, list);
        if (itemStack2 == null || !FourWeaponFoundationAdapter.preserveDamage(itemStack, itemStack2)) {
            return false;
        }
        if (FourWeaponFoundationAdapter.fingerprint(itemStack2).equals(FourWeaponFoundationAdapter.fingerprint(itemStack))) {
            return true;
        }
        itemStack.m_41751_(itemStack2.m_41783_().m_6426_());
        NativeFoodWeaponWear.manage(serverPlayer, itemStack);
        NativeFoodWeaponWear.componentsChanged(itemStack);
        serverPlayer.m_150109_().m_6596_();
        return true;
    }

    private static boolean preserveDamage(ItemStack itemStack, ItemStack itemStack2) {
        if (!itemStack.m_41763_() || itemStack.m_41776_() <= 0) {
            return true;
        }
        int n = itemStack.m_41773_();
        int n2 = itemStack2.m_41776_();
        if (n2 <= 0 || n >= n2) {
            return false;
        }
        itemStack2.m_41784_().m_128405_("Damage", n);
        return true;
    }

    private static CompoundTag authorRulesSnapshot(ItemStack itemStack) {
        return FoundationAuthorRuleSnapshots.snapshot(itemStack);
    }

    public static String fingerprintTag(Tag tag) {
        return FoundationCanonicalDigest.fingerprint("external-NBT", tag);
    }

    public static String canonical(Tag tag) {
        return HexFormat.of().formatHex(FoundationCanonicalDigest.canonical(tag));
    }

    private static int integer(CompoundTag compoundTag, String string, int n, boolean bl) {
        if (!compoundTag.m_128441_(string)) {
            FourWeaponFoundationAdapter.require(!bl, "missing-" + string);
            return n;
        }
        FourWeaponFoundationAdapter.require(compoundTag.m_128425_(string, 3), "bad-type-" + string);
        return compoundTag.m_128451_(string);
    }

    private static void require(boolean bl, String string) {
        if (!bl) {
            throw new IllegalArgumentException(string);
        }
    }

    public static int durabilityAlloys(ItemStack itemStack) {
        FourWeaponFoundationRules.Profile profile = FourWeaponFoundationAdapter.nativeProfile(itemStack);
        return profile == null ? 0 : WeaponDurabilityBuild.foundationAlloys(itemStack, profile);
    }

    public static final class View {
        private final FourWeaponFoundationRules.Profile profile;
        private final Map<Integer, Entry> entries;
        private final Set<Integer> retired;
        private final CompoundTag root;
        private final int level;
        private final int experience;
        private final int revision;
        private final String failure;
        private final boolean migration;

        private View(FourWeaponFoundationRules.Profile profile, Map<Integer, Entry> map, Set<Integer> set, CompoundTag compoundTag, int n, int n2, int n3, String string, boolean bl) {
            this.profile = profile;
            this.entries = Map.copyOf(map);
            this.retired = Set.copyOf(set);
            this.root = compoundTag == null ? null : compoundTag.m_6426_();
            this.level = n;
            this.experience = n2;
            this.revision = n3;
            this.failure = string;
            this.migration = bl;
        }

        public boolean valid() {
            return this.failure.isEmpty();
        }

        public String failure() {
            return this.failure;
        }

        public FourWeaponFoundationRules.Profile profile() {
            return this.profile;
        }

        public int level() {
            return this.level;
        }

        public int experience() {
            return this.experience;
        }

        public int revision() {
            return this.revision;
        }

        public boolean persisted() {
            return this.root != null;
        }

        public boolean migration() {
            return this.migration;
        }

        public Map<Integer, Entry> entries() {
            return this.entries;
        }

        public Set<Integer> retired() {
            return this.retired;
        }

        public Map<Integer, FourWeaponFoundationRules.Cell> cells() {
            HashMap hashMap = new HashMap();
            this.entries.forEach((n, entry) -> hashMap.put(n, entry.value()));
            return Map.copyOf(hashMap);
        }

        public FourWeaponFoundationRules.Status status() {
            return this.valid() ? FourWeaponFoundationRules.evaluate(this.profile, this.cells(), this.level, FourWeaponFoundationAdapter.paidValues(this.profile, this.entries)) : null;
        }

        public CompoundTag rootCopy() {
            return this.root == null ? null : this.root.m_6426_();
        }
    }

    public static final class Entry {
        private final FourWeaponFoundationRules.Cell value;
        private final CompoundTag item;

        private Entry(FourWeaponFoundationRules.Cell cell, CompoundTag compoundTag) {
            this.value = cell;
            this.item = compoundTag == null ? null : compoundTag.m_6426_();
        }

        public FourWeaponFoundationRules.Cell value() {
            return this.value;
        }

        public CompoundTag payload() {
            return this.item == null ? null : this.item.m_6426_();
        }

        public boolean paid() {
            return this.value.refundable();
        }

        public ItemStack storedItem() {
            return this.item == null ? ItemStack.f_41583_ : ItemStack.m_41712_((CompoundTag)this.item.m_6426_());
        }
    }

    public static final class Change {
        private final String before;
        private final ItemStack after;
        private final ItemStack refund;
        private final int consume;

        private Change(String string, ItemStack itemStack, ItemStack itemStack2, int n) {
            this.before = string;
            this.after = itemStack.m_41777_();
            this.refund = itemStack2.m_41777_();
            this.consume = n;
        }

        public String before() {
            return this.before;
        }

        public ItemStack after() {
            return this.after.m_41777_();
        }

        public ItemStack refund() {
            return this.refund.m_41777_();
        }

        public int consume() {
            return this.consume;
        }
    }
}

