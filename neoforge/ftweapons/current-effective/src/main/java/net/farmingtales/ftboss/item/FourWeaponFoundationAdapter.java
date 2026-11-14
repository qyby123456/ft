package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition.Category;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Wear;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Cell;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Status;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values;
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
import net.minecraft.core.registries.BuiltInRegistries;

public final class FourWeaponFoundationAdapter {
    public static final String ROOT = "FTBossFourWeaponFoundation";
    public static final String EVOLUTION = "FTBossFoodEvolution";
    private static final String LEGACY = "FTBossSwordStructure";
    private static final String MATERIALS = "FTBossWeaponMaterials";
    public static final int SCHEMA = 2;
    public static final int DIGEST_VERSION = 3;
    private static final Build IDENTITY = new Build(1.0, 1.0, 1.0, 1.0, 0.0);
    private static final Map<String, Profile> PROFILES = FourWeaponFoundationProfiles.examples();

    private FourWeaponFoundationAdapter() {
    }

    public static String registryId(ItemStack var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
        return var1 == null ? "" : var1.toString();
    }

    private static Profile nativeProfile(ItemStack var0) {
        if (WeaponAttachmentPolicy.excluded(var0)) {
            return null;
        } else if (!var0.isEmpty() && var0.getCount() == 1) {
            Profile var1 = PROFILES.get(registryId(var0));
            if (var1 == null) {
                return null;
            } else if (!matchesType(var0, var1.family())) {
                return null;
            } else {
                try {
                    Class var2 = Class.forName("net.farmingtales.ftboss.api.SwordCavityExtensions");
                    return var2.getMethod("foundationProfile", ItemStack.class, Profile.class).invoke(null, var0, var1) instanceof Profile var4 ? var4 : null;
                } catch (ClassNotFoundException var5) {
                    return var1;
                } catch (LinkageError | ReflectiveOperationException var6) {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static boolean matchesType(ItemStack var0, String var1) {
        if (var0 != null && !var0.isEmpty()) {
            String var2 = registryId(var0);
            Profile var3 = PROFILES.get(var2);
            if (var3 != null && var3.family().equals(var1) && BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(var2)).orElse(null) == var0.getItem()) {
                String var4 = switch (var2) {
                    case "tide:starlight_bow" -> "com.li64.tide.registries.items.StarlightBowItem";
                    case "irons_spellbooks:autoloader_crossbow" -> "io.redspace.ironsspellbooks.item.weapons.AutoloaderCrossbow";
                    case "ftboss:barrel_crossbow" -> "net.farmingtales.ftboss.item.BarrelCrossbowItem";
                    case "armamentarium:ender_maiden_shortbow" -> "net.gqstavo.armamentarium.item.EnderMaidenShortbowItem";
                    case "cataclysm:cursed_bow" -> "com.github.L_Ender.cataclysm.items.Cursed_bow";
                    case "ftboss:qizai_prayer_staff" -> "net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem";
                    case "ftboss:frost_queen_staff" -> "net.farmingtales.ftboss.item.FrostQueenStaffItem";
                    case "armamentarium:mariana_scepter" -> "net.gqstavo.armamentarium.item.MarianaScepterItem";
                    case "irons_spellbooks:staff_of_the_nines" -> "io.redspace.ironsspellbooks.item.weapons.StaffOfTheNines";
                    case "legendary_monsters:wand_of_clouds" -> "net.miauczel.legendary_monsters.item.custom.WandOfCloudsItem";
                    case "cataclysm:wrath_of_the_desert" -> "com.github.L_Ender.cataclysm.items.Wrath_of_the_desert";
                    case "cataclysm:black_steel_axe" -> "net.minecraft.world.item.AxeItem";
                    default -> "";
                };
                if (!var4.isEmpty()) {
                    return var0.getItem().getClass().getName().equals(var4);
                } else {
                    return switch (var1) {
                        case "bow" -> var0.getItem() instanceof BowItem;
                        case "crossbow" -> var0.getItem() instanceof CrossbowItem;
                        case "axe" -> var0.getItem() instanceof AxeItem;
                        case "trident" -> var0.getItem() instanceof TridentItem;
                        case "staff" -> castingItem(var0);
                        default -> false;
                    };
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean castingItem(ItemStack var0) {
        for (Class var1 = var0.getItem().getClass(); var1 != null; var1 = var1.getSuperclass()) {
            if (var1.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) {
                return true;
            }
        }

        return false;
    }

    public static boolean applies(ItemStack var0) {
        if (AdditionalWeaponUiEligibility.keepLegacyUi(var0)) {
            return false;
        } else {
            return WeaponAttachmentPolicy.excluded(var0)
                ? false
                : !var0.isEmpty() && (PROFILES.containsKey(registryId(var0)) || NativeFoundationItemData.contains(var0, "FTBossFourWeaponFoundation"));
        }
    }

    public static boolean active(ItemStack var0) {
        return applies(var0);
    }

    public static Profile profile(ItemStack var0) {
        return read(var0).profile();
    }

    public static Status status(ItemStack var0) {
        return read(var0).status();
    }

    public static int revision(ItemStack var0) {
        return read(var0).revision();
    }

    public static boolean blocksUse(ItemStack var0) {
        if (!applies(var0)) {
            return false;
        } else {
            FourWeaponFoundationAdapter.View var1 = read(var0);
            return !var1.valid() || !var1.status().ready();
        }
    }

    public static boolean cellUnlocked(ItemStack var0, int var1) {
        FourWeaponFoundationAdapter.View var2 = read(var0);
        return var2.valid() && FourWeaponFoundationRules.unlocked(var2.profile(), var1, var2.level());
    }

    public static List<ItemStack> slots(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        ArrayList var2 = new ArrayList();

        for (int var3 = 0; var3 < 27; var3++) {
            FourWeaponFoundationAdapter.Entry var4 = var1.entries().get(var3);
            var2.add(var1.valid() && var4 != null && var4.paid() ? var4.storedItem() : ItemStack.EMPTY);
        }

        return List.copyOf(var2);
    }

    public static List<Material> powerMaterials(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        if (var1.valid() && var1.status().ready()) {
            ArrayList var2 = new ArrayList();
            var1.entries().forEach((var2x, var3) -> {
                if (var3.paid() && FourWeaponFoundationRules.unlocked(var1.profile(), var2x, var1.level()) && var3.value().material() != Material.ORIGINAL) {
                    var2.add(var3.value().material());
                }
            });
            return List.copyOf(var2);
        } else {
            return List.of();
        }
    }

    public static Build combat(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        if (var1.valid() && var1.status().ready()) {
            Build var2 = FourWeaponFoundationRules.combat(var1.profile(), var1.cells(), var1.level());
            ArrayList var3 = new ArrayList();
            var1.entries().forEach((var2x, var3x) -> {
                if (var3x.paid() && FourWeaponFoundationRules.unlocked(var1.profile(), var2x, var1.level())) {
                    var3.add(var3x.storedItem());
                }
            });

            Build var10000;
            try {
                Class var4 = Class.forName("net.farmingtales.ftboss.api.SwordCavityExtensions");
                var10000 = var4.getMethod("applyBuild", ItemStack.class, Build.class, List.class, boolean.class)
                        .invoke(null, var0, var2, List.copyOf(var3), true) instanceof Build var6
                    ? var6
                    : IDENTITY;
            } catch (ClassNotFoundException var7) {
                return WeaponDurabilityBuild.combat(var0, var2);
            } catch (LinkageError | ReflectiveOperationException var8) {
                return WeaponDurabilityBuild.combat(var0, IDENTITY);
            }

            return WeaponDurabilityBuild.combat(var0, var10000);
        } else {
            return WeaponDurabilityBuild.combat(var0, IDENTITY);
        }
    }

    public static FourWeaponFoundationAdapter.View read(ItemStack var0) {
        Profile var1 = null;

        try {
            var1 = nativeProfile(var0);
            require(var1 != null, "unapproved-registry-or-type");
            FoundationCanonicalDigest.canonical(NativeFoundationItemData.image(var0));
            profileFingerprint(var1);
            FoundationAuthorRuleSnapshots.validate(var0);
            CompoundTag var2 = NativeFoundationItemData.customData(var0);
            if (var2 != null && var2.contains("FTBossFourWeaponFoundation")) {
                require(var2.contains("FTBossFourWeaponFoundation", 10), "bad-root-type");
                return parse(var0, var1, var2.getCompound("FTBossFourWeaponFoundation"));
            } else {
                int[] var13 = progress(var0, true);
                HashMap var4 = new HashMap();
                HashSet var5 = new HashSet();
                boolean var6 = var2 != null && (var2.contains("FTBossSwordStructure") || var2.contains("FTBossWeaponMaterials"));
                if (var6 && var2.contains("FTBossWeaponMaterials")) {
                    require(var2.contains("FTBossWeaponMaterials", 10), "bad-legacy-material-root");
                }

                if (var6 && var2.contains("FTBossWeaponMaterials", 10) && var2.getCompound("FTBossWeaponMaterials").contains("WearCarry")) {
                    CompoundTag var7 = var2.getCompound("FTBossWeaponMaterials");
                    require(
                        var7.contains("WearCarry", 6)
                            && Double.isFinite(var7.getDouble("WearCarry"))
                            && var7.getDouble("WearCarry") >= 0.0
                            && var7.getDouble("WearCarry") < 1.0,
                        "bad-legacy-wear-carry"
                    );
                }

                if (var6 && var2.contains("FTBossSwordStructure")) {
                    migrateCells(var0, var1, var4, var5);
                } else {
                    if (var6) {
                        require(var2.contains("FTBossWeaponMaterials", 10), "bad-legacy-material-root");
                        CompoundTag var14 = var2.getCompound("FTBossWeaponMaterials");

                        for (Part var11 : Part.values()) {
                            if (var14.contains(var11.name())) {
                                require(var14.contains(var11.name(), 8), "bad-legacy-material");
                                require(var14.getString(var11.name()).equals(Material.ORIGINAL.id()), "legacy-paid-item-missing");
                            }
                        }
                    }

                    if (var6) {
                        var5.addAll(var1.nativeCells().keySet());
                    } else {
                        var1.nativeCells().forEach((var1x, var2x) -> var4.put(var1x, new FourWeaponFoundationAdapter.Entry(var2x, null)));
                    }
                }

                int var15 = 0;
                if (var6 && var2.contains("FTBossSwordStructure", 10)) {
                    var15 = integer(var2.getCompound("FTBossSwordStructure"), "Revision", 0, false);
                }

                require(var15 >= 0 && var15 < Integer.MAX_VALUE, "bad-legacy-revision");
                return new FourWeaponFoundationAdapter.View(var1, var4, var5, null, var13[0], var13[1], var15, "", var6);
            }
        } catch (LinkageError | RuntimeException var12) {
            String var3 = var12.getMessage();
            return new FourWeaponFoundationAdapter.View(
                var1, Map.of(), Set.of(), null, 0, 0, -2, var3 != null && !var3.isBlank() ? var3 : "invalid-foundation-input", false
            );
        }
    }

    private static int[] progress(ItemStack var0, boolean var1) {
        CompoundTag var2 = NativeFoundationItemData.customData(var0);
        if (var2 != null && var2.contains("FTBossFoodEvolution")) {
            require(var2.contains("FTBossFoodEvolution", 10), "bad-evolution-root");
            CompoundTag var3 = var2.getCompound("FTBossFoodEvolution");
            int var4 = integer(var3, "Level", 0, false);
            int var5 = integer(var3, "Experience", 0, false);
            int var6 = integer(var3, "ProgressionVersion", 0, false);
            require(var4 >= 0 && var4 <= 8 && var5 >= 0 && var6 >= 0 && var6 <= 2, "bad-or-future-progression");
            if (!var1) {
                require(var4 <= 4 && var6 == 2, "unreconciled-progression");
            }

            if (var1 && var6 < 2) {
                int var7 = var4 == 0 ? 0 : FoodWeaponBuildRules.evolutionCost(var4 - 1);
                var5 = Math.max(0, var5 - var7);
            }

            var4 = Math.min(var4, 4);
            var5 = var4 == 4 ? 0 : Math.min(var5, FoodWeaponBuildRules.evolutionCost(var4));
            return new int[]{var4, var5};
        } else {
            return new int[]{0, 0};
        }
    }

    private static FourWeaponFoundationAdapter.View parse(ItemStack var0, Profile var1, CompoundTag var2) {
        require(integer(var2, "Schema", 0, true) == 2, "bad-or-future-schema");
        require(integer(var2, "DigestVersion", 0, true) == 3, "unsupported-digest-version");
        require(integer(var2, "ProfileRevision", 0, true) == 1, "bad-or-future-profile");
        require(var2.contains("Profile", 8) && var2.getString("Profile").equals(var1.id()), "profile-mismatch");
        require(var2.contains("Rules", 8) && FoundationProfileFingerprints.matches(var2.getString("Rules"), var1), "profile-rules-changed");
        require(var2.contains("Initialized", 1) && var2.getByte("Initialized") == 1, "uninitialized-root");
        int var3 = integer(var2, "Revision", -1, true);
        require(var3 >= 0 && var3 < Integer.MAX_VALUE, "bad-revision");
        int[] var4 = progress(var0, false);
        int var5 = integer(var2, "ReconciledLevel", -1, true);
        require(var5 >= 0 && var5 <= 4, "bad-reconciled-level");
        require(var2.contains("Cells", 9), "bad-cells-type");
        ListTag var6 = (ListTag)var2.get("Cells");
        require(var6.size() <= 27 && (var6.isEmpty() || var6.getElementType() == 10), "bad-cells-list");
        HashMap var7 = new HashMap();

        for (Tag var9 : var6) {
            CompoundTag var10 = (CompoundTag)var9;
            int var11 = integer(var10, "Slot", -1, true);
            require(var11 >= 0 && var11 < 27 && !var7.containsKey(var11), "duplicate-or-out-of-range-slot");
            require(var10.contains("Source", 8), "bad-source");
            String var12 = var10.getString("Source");
            if (var12.equals("native-reference")) {
                require(!var10.contains("Item"), "reference-has-refundable-payload");
                Cell var19 = var1.nativeCells().get(var11);
                require(
                    var19 != null
                        && var10.contains("Id", 8)
                        && var19.itemId().equals(var10.getString("Id"))
                        && var10.contains("Material", 8)
                        && var19.material().id().equals(var10.getString("Material")),
                    "bad-reference"
                );
                var7.put(var11, new FourWeaponFoundationAdapter.Entry(var19, null));
            } else {
                require(var12.equals("player-paid") && var10.contains("Item", 10), "bad-paid-cell");
                FourWeaponFoundationAdapter.Entry var13 = paid(var1, var10.getCompound("Item"));
                require(var10.contains("Id", 8) && var13.value().itemId().equals(var10.getString("Id")), "paid-id-mismatch");
                require(var10.contains("Material", 8) && var13.value().material().id().equals(var10.getString("Material")), "material-mismatch");
                var7.put(var11, var13);
            }
        }

        require(var2.contains("RetiredNative", 9), "bad-retirement-type");
        ListTag var14 = (ListTag)var2.get("RetiredNative");
        require(var14.size() <= 27 && (var14.isEmpty() || var14.getElementType() == 3), "bad-retirement-list");
        HashSet var15 = new HashSet();

        for (Tag var17 : var14) {
            int var18 = ((IntTag)var17).getAsInt();
            require(
                var18 >= 0 && var18 < 27 && var15.add(var18) && (!var7.containsKey(var18) || ((FourWeaponFoundationAdapter.Entry)var7.get(var18)).paid()),
                "bad-retirement-entry"
            );
        }

        require(var2.contains("LegacyBackup", 10), "missing-full-backup");
        if (var2.contains("WearCarry")) {
            require(
                var2.contains("WearCarry", 6)
                    && Double.isFinite(var2.getDouble("WearCarry"))
                    && var2.getDouble("WearCarry") >= 0.0
                    && var2.getDouble("WearCarry") < 1.0,
                "bad-wear-carry"
            );
        }

        return new FourWeaponFoundationAdapter.View(var1, var7, var15, var2, var4[0], var4[1], var3, "", false);
    }

    private static Map<Integer, Values> paidValues(Profile var0, Map<Integer, FourWeaponFoundationAdapter.Entry> var1) {
        HashMap var2 = new HashMap();
        var1.forEach((var2x, var3) -> {
            if (var3.paid() && !var0.items().containsKey(var3.value().itemId())) {
                Optional var4 = SwordMaterialExtensions.resolve(var3.storedItem());
                if (var4.isPresent() && ((SwordMaterialDefinition)var4.get()).categories().contains(Category.FOOD)) {
                    net.farmingtales.ftboss.util.SwordFunctionRules.Values var5 = ((SwordMaterialDefinition)var4.get()).functions();
                    var2.put(var2x, new Values(var5.handle(), var5.guard(), var5.core(), var5.stability(), var5.blade()));
                }
            }
        });
        return Map.copyOf(var2);
    }

    private static FourWeaponFoundationAdapter.Entry paid(Profile var0, CompoundTag var1) {
        require(var1.contains("id", 8) && var1.contains("count", 3) && var1.getInt("count") == 1, "paid-count-not-one");
        ResourceLocation var2 = ResourceLocation.tryParse(var1.getString("id"));
        require(var2 != null && BuiltInRegistries.ITEM.containsKey(var2) && !var2.toString().equals("minecraft:air"), "unknown-item");
        require(!var1.contains("tag") && !var1.contains("Count") && !var1.contains("ForgeCaps"), "legacy-item-image-requires-explicit-migration");
        require(!var1.contains("components") || var1.contains("components", 10), "bad-item-components");
        ItemStack var3 = NativeFoundationItemData.decode(var1.copy());
        require(!var3.isEmpty() && var3.getCount() == 1 && registryId(var3).equals(var2.toString()) && !applies(var3), "invalid-or-nested-weapon");
        Material var4 = resolveMaterial(var3);
        if (var3.isDamageableItem()) {
            var4 = Material.ORIGINAL;
        }

        require(var0.items().containsKey(var2.toString()) || var4 != Material.ORIGINAL && var0.materials().containsKey(var4), "unsupported-paid-material");
        Cell var5 = new Cell(var2.toString(), var4, "player-paid", canonical(var1));
        return new FourWeaponFoundationAdapter.Entry(var5, var1);
    }

    private static void migrateCells(ItemStack var0, Profile var1, Map<Integer, FourWeaponFoundationAdapter.Entry> var2, Set<Integer> var3) {
        CompoundTag var4 = NativeFoundationItemData.customData(var0);
        require(var4.contains("FTBossSwordStructure", 10), "bad-legacy-root");
        CompoundTag var5 = var4.getCompound("FTBossSwordStructure");
        require(integer(var5, "Version", 0, true) == 2, "unsupported-legacy-version");
        require(var5.contains("Cells", 10), "bad-legacy-cells");
        CompoundTag var6 = var5.getCompound("Cells");
        HashSet var7 = new HashSet();

        for (Slot var11 : Slot.values()) {
            var7.add(var11.name());
        }

        require(var7.containsAll(var6.getAllKeys()), "unknown-legacy-slot");
        if (var4.contains("FTBossWeaponMaterials", 10)) {
            CompoundTag var18 = var4.getCompound("FTBossWeaponMaterials");

            for (Slot var12 : Slot.values()) {
                if (var12.legacyPart != null && var18.contains(var12.legacyPart.name())) {
                    require(var18.contains(var12.legacyPart.name(), 8), "bad-legacy-material-type");
                    String var13 = var18.getString(var12.legacyPart.name());
                    if (!var13.equals(Material.ORIGINAL.id())) {
                        CompoundTag var14 = var6.getCompound(var12.name());
                        require(
                            var14.getString("State").equals("installed") && var13.equals(var14.getString("Material")) && var14.contains("Item", 10),
                            "conflicting-legacy-ledgers"
                        );
                    }
                }
            }
        }

        for (Slot var25 : Slot.values()) {
            int var26 = var25.ordinal();
            if (!var6.contains(var25.name())) {
                if (var1.nativeCells().containsKey(var26)) {
                    var3.add(var26);
                }
            } else {
                require(var6.contains(var25.name(), 10), "bad-legacy-entry");
                CompoundTag var27 = var6.getCompound(var25.name());
                require(var27.contains("State", 8), "bad-legacy-state");
                String var28 = var27.getString("State");
                switch (var28) {
                    case "installed":
                        require(var27.contains("Item", 10) && var27.contains("Material", 8), "legacy-paid-item-missing");
                        FourWeaponFoundationAdapter.Entry var29 = paid(var1, var27.getCompound("Item"));
                        require(var29.value().material().id().equals(var27.getString("Material")), "legacy-material-mismatch");
                        var2.put(var26, var29);
                        if (var1.nativeCells().containsKey(var26)) {
                            var3.add(var26);
                        }
                        break;
                    case "native":
                        require(!var27.contains("Item"), "legacy-native-has-item");
                        Cell var17 = var1.nativeCells().get(var26);
                        if (var17 != null) {
                            var2.put(var26, new FourWeaponFoundationAdapter.Entry(var17, null));
                        }
                        break;
                    case "empty":
                    case "missing":
                        require(!var27.contains("Item"), "legacy-hidden-paid-item");
                        if (var1.nativeCells().containsKey(var26)) {
                            var3.add(var26);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("bad-legacy-state");
                }
            }
        }
    }

    public static FourWeaponFoundationAdapter.Change prepareEdit(ItemStack var0, int var1, ItemStack var2, int var3) {
        FourWeaponFoundationAdapter.View var4 = read(var0);
        if (var4.valid() && var4.revision() == var3 && var1 >= 0 && var1 < 27 && var2 != var0 && (var2.isEmpty() || cellUnlocked(var0, var1))) {
            try {
                HashMap var5 = new HashMap<>(var4.entries());
                HashSet var6 = new HashSet<>(var4.retired());
                FourWeaponFoundationAdapter.Entry var7 = (FourWeaponFoundationAdapter.Entry)var5.remove(var1);
                FourWeaponFoundationAdapter.Entry var8 = null;
                if (!var2.isEmpty()) {
                    ItemStack var9 = var2.copy();
                    var9.setCount(1);
                    var8 = paid(var4.profile(), NativeFoundationItemData.image(var9));
                    var5.put(var1, var8);
                }

                if (var7 == null && var8 == null) {
                    return null;
                } else {
                    if (var7 != null && !var7.paid()) {
                        var6.add(var1);
                    }

                    ItemStack var11 = persist(var0, var4, var5, var6, var4.level(), var4.experience(), true);
                    return new FourWeaponFoundationAdapter.Change(
                        fingerprint(var0), var11, var7 != null && var7.paid() ? var7.storedItem() : ItemStack.EMPTY, var8 == null ? 0 : 1
                    );
                }
            } catch (IllegalArgumentException var10) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static ItemStack preview(ItemStack var0, int var1, ItemStack var2) {
        FourWeaponFoundationAdapter.Change var3 = prepareEdit(var0, var1, var2, revision(var0));
        return var3 == null ? var0.copy() : var3.after();
    }

    private static ItemStack persist(
        ItemStack var0,
        FourWeaponFoundationAdapter.View var1,
        Map<Integer, FourWeaponFoundationAdapter.Entry> var2,
        Set<Integer> var3,
        int var4,
        int var5,
        boolean var6
    ) {
        ItemStack var7 = var0.copy();
        CompoundTag var8 = var1.rootCopy();
        if (var8 == null) {
            var8 = new CompoundTag();
            var8.put("LegacyBackup", NativeFoundationItemData.image(var0).copy());
            CompoundTag var9 = NativeFoundationItemData.element(var0, "FTBossWeaponMaterials");
            if (var9 != null && var9.contains("WearCarry")) {
                require(
                    var9.contains("WearCarry", 6)
                        && Double.isFinite(var9.getDouble("WearCarry"))
                        && var9.getDouble("WearCarry") >= 0.0
                        && var9.getDouble("WearCarry") < 1.0,
                    "bad-legacy-wear-carry"
                );
                var8.putDouble("WearCarry", var9.getDouble("WearCarry"));
            }
        }

        var8.putInt("Schema", 2);
        var8.putInt("DigestVersion", 3);
        var8.putInt("ProfileRevision", 1);
        var8.putString("Profile", var1.profile().id());
        var8.putString("Rules", profileFingerprint(var1.profile()));
        var8.putBoolean("Initialized", true);
        require(!var6 || var1.revision() < Integer.MAX_VALUE, "revision-exhausted");
        var8.putInt("Revision", Math.max(0, var1.revision()) + (var6 ? 1 : 0));
        var8.putInt("ReconciledLevel", var4);
        ListTag var14 = new ListTag();

        for (int var11 : new TreeSet<>(var2.keySet())) {
            FourWeaponFoundationAdapter.Entry var12 = (FourWeaponFoundationAdapter.Entry)var2.get(var11);
            CompoundTag var13 = new CompoundTag();
            var13.putInt("Slot", var11);
            var13.putString("Source", var12.value().provenance());
            var13.putString("Id", var12.value().itemId());
            var13.putString("Material", var12.value().material().id());
            if (var12.paid()) {
                var13.put("Item", var12.payload());
            }

            var14.add(var13);
        }

        var8.put("Cells", var14);
        ListTag var15 = new ListTag();

        for (int var18 : new TreeSet<>(var3)) {
            var15.add(IntTag.valueOf(var18));
        }

        var8.put("RetiredNative", var15);
        CompoundTag persistedData = NativeFoundationItemData.customData(var7);
        if (persistedData == null) {
            persistedData = new CompoundTag();
        }
        persistedData.put("FTBossFourWeaponFoundation", var8);
        CompoundTag var17 = persistedData.contains("FTBossWeaponMaterials", 10) ? persistedData.getCompound("FTBossWeaponMaterials") : null;
        if (var17 != null) {
            var17.remove("WearCarry");
        }

        CompoundTag var19 = persistedData.getCompound("FTBossFoodEvolution");
        persistedData.put("FTBossFoodEvolution", var19);
        var19.putInt("Level", var4);
        var19.putInt("Experience", var5);
        var19.putInt("ProgressionVersion", 2);
        if (var1.root == null && var19.contains("StageHistory", 9)) {
            ListTag var20 = (ListTag)var19.get("StageHistory");
            require(var20.isEmpty() || var20.getElementType() == 10, "bad-legacy-history");

            while (var20.size() > var4) {
                var20.remove(var20.size() - 1);
            }

            if (var19.contains("SelectedStage", 3)) {
                var19.putInt("SelectedStage", Math.max(0, Math.min(var4 - 1, var19.getInt("SelectedStage"))));
            }
        }

        NativeFoundationItemData.setCustomData(var7, persistedData);
        FoundationCanonicalDigest.canonical(NativeFoundationItemData.image(var7));
        return var7;
    }

    public static boolean initializeForServerEdit(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        if (!var1.valid()) {
            return false;
        } else if (var1.persisted()) {
            return true;
        } else {
            try {
                ItemStack var2 = persist(var0, var1, var1.entries(), var1.retired(), var1.level(), var1.experience(), true);
                NativeFoundationItemData.copyComponents(var0, var2);
                return true;
            } catch (IllegalArgumentException var3) {
                return false;
            }
        }
    }

    public static boolean reconcileLevel(ItemStack var0, int var1, int var2) {
        FourWeaponFoundationAdapter.View var3 = read(var0);
        if (var3.valid() && var3.persisted() && var1 >= 0 && var1 <= 4 && var2 >= 0 && var3.level() == var1 && var3.experience() == var2) {
            CompoundTag var4 = var3.rootCopy();
            if (var4.getInt("ReconciledLevel") == var1) {
                return true;
            } else {
                ItemStack var5 = persist(var0, var3, var3.entries(), var3.retired(), var1, var2, true);
                NativeFoundationItemData.copyComponents(var0, var5);
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean replace(ServerPlayer var0, ItemStack var1, int var2, int var3, int var4) {
        if (!var0.isSpectator()
            && var0.isAlive()
            && !var0.level().isClientSide
            && var1.getCount() == 1
            && (var0.getMainHandItem() == var1 || var0.getOffhandItem() == var1)
            && var3 >= -1
            && var3 < 36) {
            ItemStack var5 = var3 < 0 ? ItemStack.EMPTY : var0.getInventory().getItem(var3);
            if ((var3 < 0 || !var5.isEmpty()) && var5 != var1) {
                FourWeaponFoundationAdapter.Change var6 = prepareEdit(var1, var2, var5, var4);
                if (var6 == null) {
                    return false;
                } else {
                    ArrayList var7 = new ArrayList();
                    ArrayList var8 = new ArrayList();
                    int var9 = -1;

                    for (int var10 = 0; var10 < 36; var10++) {
                        ItemStack var11 = var0.getInventory().getItem(var10);
                        if (var11 == var1) {
                            var9 = var10;
                        }

                        var7.add(var11.copy());
                        var8.add(var11.copy());
                    }

                    if (var6.consume() != 0) {
                        ((ItemStack)var8.get(var3)).shrink(var6.consume());
                    }

                    if (!fit(var8, var6.refund(), var9)) {
                        return false;
                    } else {
                        ItemStack var15 = var6.after();
                        if (var1.isDamageableItem() && var1.getMaxDamage() > 0) {
                            int var16 = var1.getMaxDamage();
                            int var12 = var15.getMaxDamage();
                            if (var12 <= 0) {
                                return false;
                            }

                            if (var1.getDamageValue() >= var12) {
                                return false;
                            }

                            var15.setDamageValue(var1.getDamageValue());
                        }

                        if (!fingerprint(var1).equals(var6.before())) {
                            return false;
                        } else {
                            for (int var17 = 0; var17 < 36; var17++) {
                                if (!fingerprint(var0.getInventory().getItem(var17)).equals(fingerprint((ItemStack)var7.get(var17)))) {
                                    return false;
                                }
                            }

                            NativeStackComponents.Snapshot var18 = NativeStackComponents.capture(var1);

                            try {
                                NativeFoundationItemData.copyComponents(var1, var15);
                                NativeFoodWeaponWear.manage(var0, var1);
                                NativeFoodWeaponWear.componentsChanged(var1);

                                for (int var19 = 0; var19 < 36; var19++) {
                                    if (var19 != var9) {
                                        var0.getInventory().setItem(var19, (ItemStack)var8.get(var19));
                                    }
                                }

                                var0.getInventory().setChanged();
                                var0.containerMenu.broadcastChanges();
                                return true;
                            } catch (RuntimeException var14) {
                                var18.restore(var1);

                                for (int var13 = 0; var13 < 36; var13++) {
                                    if (var13 != var9) {
                                        var0.getInventory().setItem(var13, (ItemStack)var7.get(var13));
                                    }
                                }

                                return false;
                            }
                        }
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean fit(List<ItemStack> var0, ItemStack var1, int var2) {
        if (var1.isEmpty()) {
            return true;
        } else {
            for (int var3 = 0; var3 < var0.size(); var3++) {
                if (var3 != var2) {
                    ItemStack var4 = (ItemStack)var0.get(var3);
                    CompoundTag var5 = NativeFoundationItemData.image(var4);
                    CompoundTag var6 = NativeFoundationItemData.image(var1);
                    var5.remove("count");
                    var6.remove("count");
                    if (!var4.isEmpty() && var5.equals(var6) && var4.getCount() < var4.getMaxStackSize()) {
                        var4.grow(1);
                        return true;
                    }
                }
            }

            for (int var7 = 0; var7 < var0.size(); var7++) {
                if (var7 != var2 && ((ItemStack)var0.get(var7)).isEmpty()) {
                    var0.set(var7, var1.copy());
                    return true;
                }
            }

            return false;
        }
    }

    public static String rulesFingerprint(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        if (!var1.valid()) {
            return null;
        } else {
            try {
                ArrayList var2 = new ArrayList();

                for (int var4 : new TreeSet<>(var1.entries().keySet())) {
                    FourWeaponFoundationAdapter.Entry var5 = var1.entries().get(var4);
                    if (var5.paid()) {
                        var2.add(Map.of("Slot", var4, "Definition", SwordMaterialExtensions.resolve(var5.storedItem())));
                    }
                }

                return FoundationAuthorRuleSnapshots.fingerprint(var0, profileFingerprint(var1.profile()), FoundationCanonicalDigest.object(var2));
            } catch (IllegalArgumentException var6) {
                return null;
            }
        }
    }

    private static String profileFingerprint(Profile var0) {
        return FoundationProfileFingerprints.fingerprint(var0);
    }

    private static Material resolveMaterial(ItemStack var0) {
        try {
            Class var1 = Class.forName("net.farmingtales.ftboss.api.SwordMaterialExtensions");
            Object var2 = var1.getMethod("baseMaterial", ItemStack.class).invoke(null, var0);
            require(var2 instanceof Material, "bad-author-material");
            return (Material)var2;
        } catch (ClassNotFoundException var3) {
            return WeaponMaterialCatalogRules.resolve(registryId(var0), var0.getTags().map(var0x -> var0x.location().toString()).toList());
        } catch (LinkageError | ReflectiveOperationException var4) {
            throw new IllegalArgumentException("unavailable-author-material", var4);
        }
    }

    public static String fingerprint(ItemStack var0) {
        return FoundationCanonicalDigest.fingerprint("full-stack", NativeFoundationItemData.image(var0));
    }

    public static String compositionFingerprint(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        if (var1.valid() && var1.status().ready() && !FourWeaponUseGuard.blocksUse(var0)) {
            try {
                String var2 = rulesFingerprint(var0);
                if (var2 == null) {
                    return null;
                } else {
                    PrivateSnapshot407 var3 = new PrivateSnapshot407();
                    var3.putString("Rules", var2);
                    var3.putInt("Level", var1.level());
                    var3.put("Retired", FoundationCanonicalDigest.object(var1.retired()));
                    ListTag var4 = new ListTag();

                    for (int var6 : new TreeSet<>(var1.entries().keySet())) {
                        FourWeaponFoundationAdapter.Entry var7 = var1.entries().get(var6);
                        PrivateSnapshot407 var8 = new PrivateSnapshot407();
                        var8.putInt("Slot", var6);
                        var8.put("Value", FoundationCanonicalDigest.object(var7.value()));
                        if (var7.paid()) {
                            var8.put("Item", var7.payload());
                        }

                        var4.add(var8.finish());
                    }

                    var3.put("Cells", var4);
                    return FoundationCanonicalDigest.fingerprint("composition", var3.finish());
                }
            } catch (IllegalArgumentException var9) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static CompoundTag compositionSnapshot(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        PrivateSnapshot407 var2 = new PrivateSnapshot407();
        var2.putInt("DigestVersion", 3);
        var2.putString("ItemId", registryId(var0));
        var2.putBoolean("Valid", var1.valid());
        var2.putBoolean("BlocksUse", FourWeaponUseGuard.blocksUse(var0));
        var2.putString("Failure", var1.failure());
        var2.putInt("Level", var1.level());
        var2.putInt("Revision", var1.revision());
        var2.put("FullStack", NativeFoundationItemData.image(var0).copy());
        if (var1.persisted()) {
            var2.put("Foundation", var1.rootCopy());
        }

        if (var1.valid()) {
            var2.putString("Profile", var1.profile().id());
            var2.putString("Family", var1.profile().family());
            String var3 = rulesFingerprint(var0);
            if (var3 != null) {
                var2.putString("Rules", var3);
            }

            String var4 = compositionFingerprint(var0);
            if (var4 != null) {
                var2.putString("Composition", var4);
            }

            ListTag var5 = new ListTag();

            for (int var7 : new TreeSet<>(var1.entries().keySet())) {
                FourWeaponFoundationAdapter.Entry var8 = var1.entries().get(var7);
                PrivateSnapshot407 var9 = new PrivateSnapshot407();
                var9.putInt("Slot", var7);
                var9.putBoolean("Unlocked", FourWeaponFoundationRules.unlocked(var1.profile(), var7, var1.level()));
                var9.putString("Source", var8.value().provenance());
                var9.putString("Id", var8.value().itemId());
                var9.putString("Material", var8.value().material().id());
                if (var8.paid()) {
                    var9.put("Item", var8.payload());
                }

                var5.add(var9.finish());
            }

            var2.put("Cells", var5);
        }

        return var2.finish();
    }

    public static boolean canEvolve(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        return var1.valid() && var1.level() < 4 && var1.status().ready();
    }

    public static boolean containerEditable(ItemStack var0) {
        return read(var0).valid();
    }

    static ItemStack nativeWearCandidate(ItemStack var0, int var1) {
        FourWeaponFoundationAdapter.View var2 = read(var0);
        FourWeaponFoundationAdapter.Entry var3 = var2.entries().get(var1);
        if (var2.valid()
            && var2.persisted()
            && var2.status().ready()
            && var3 != null
            && var3.paid()
            && var1 != 13
            && var2.revision() >= 0
            && var2.revision() != Integer.MAX_VALUE) {
            for (int var4 = 0; var4 < var2.profile().axes().size(); var4++) {
                String var5 = var2.profile().axes().get(var4).toLowerCase(Locale.ROOT);
                if (var5.contains("core") && var2.profile().weights().get(var1).at(var4) >= 1.0) {
                    return null;
                }
            }

            HashMap var7 = new HashMap<>(var2.entries());
            var7.remove(var1);
            ItemStack var8 = persist(var0, var2, var7, var2.retired(), var2.level(), var2.experience(), true);
            FourWeaponFoundationAdapter.View var6 = read(var8);
            return var6.valid() && var6.status().ready() ? var8 : null;
        } else {
            return null;
        }
    }

    public static boolean losePart(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        if (var1.valid() && !var1.entries().isEmpty()) {
            try {
                int var2 = var1.entries().keySet().stream().filter(var1x -> var1.entries().get(var1x).paid()).mapToInt(Integer::intValue).max().orElse(-1);
                if (var2 < 0) {
                    return false;
                } else {
                    HashMap var3 = new HashMap<>(var1.entries());
                    HashSet var4 = new HashSet<>(var1.retired());
                    FourWeaponFoundationAdapter.Entry var5 = (FourWeaponFoundationAdapter.Entry)var3.remove(var2);
                    if (!var5.paid()) {
                        var4.add(var2);
                    }

                    ItemStack var6 = persist(var0, var1, var3, var4, var1.level(), var1.experience(), true);
                    NativeFoundationItemData.copyComponents(var0, var6);
                    return true;
                }
            } catch (LinkageError | RuntimeException var7) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean resetWearAfterRepair(ItemStack var0) {
        FourWeaponFoundationAdapter.View var1 = read(var0);
        if (!var1.valid()) {
            return false;
        } else {
            try {
                ItemStack var2 = persist(var0, var1, var1.entries(), var1.retired(), var1.level(), var1.experience(), true);
                CompoundTag repairData = NativeFoundationItemData.customData(var2);
                repairData.getCompound("FTBossFourWeaponFoundation").putDouble("WearCarry", 0.0);
                CompoundTag var3 = repairData.contains("FTBossWeaponMaterials", 10) ? repairData.getCompound("FTBossWeaponMaterials") : null;
                if (var3 != null) {
                    var3.remove("WearCarry");
                }

                NativeFoundationItemData.setCustomData(var2, repairData);
                FoundationCanonicalDigest.canonical(NativeFoundationItemData.image(var2));
                NativeFoundationItemData.copyComponents(var0, var2);
                return true;
            } catch (LinkageError | RuntimeException var4) {
                return false;
            }
        }
    }

    public static int durabilityCost(ItemStack var0, int var1) {
        if (var1 > 0 && var0.isDamageableItem()) {
            FourWeaponFoundationAdapter.View var2 = read(var0);
            if (var2.valid() && var2.persisted()) {
                Wear var3 = FoodWeaponBuildRules.wear(var1, combat(var0).wear(), var2.root.getDouble("WearCarry"));
                FoodWeaponData.edit(var0, "FTBossFourWeaponFoundation", data -> data.putDouble("WearCarry", var3.carry()));
                return var3.damage();
            } else {
                return var1;
            }
        } else {
            return var1;
        }
    }

    public static boolean reference(ItemStack var0, int var1) {
        FourWeaponFoundationAdapter.View var2 = read(var0);
        FourWeaponFoundationAdapter.Entry var3 = var2.entries().get(var1);
        return var2.valid() && var3 != null && !var3.paid();
    }

    public static ItemStack containerPreview(ItemStack var0, List<ItemStack> var1) {
        FourWeaponFoundationAdapter.View var2 = read(var0);
        if (var2.valid() && var1 != null && var1.size() == 27) {
            try {
                HashMap var3 = new HashMap<>(var2.entries());
                HashSet var4 = new HashSet<>(var2.retired());
                boolean var5 = false;

                for (int var6 = 0; var6 < 27; var6++) {
                    FourWeaponFoundationAdapter.Entry var7 = (FourWeaponFoundationAdapter.Entry)var3.get(var6);
                    ItemStack var8 = var7 != null && var7.paid() ? var7.storedItem() : ItemStack.EMPTY;
                    ItemStack var9 = (ItemStack)var1.get(var6);
                    if (var9 == null || var9 == var0 || !var9.isEmpty() && var9.getCount() != 1) {
                        return null;
                    }

                    if (!fingerprint(var8).equals(fingerprint(var9))) {
                        if (!var9.isEmpty() && !FourWeaponFoundationRules.unlocked(var2.profile(), var6, var2.level())) {
                            return null;
                        }

                        if (var7 != null && !var7.paid()) {
                            var4.add(var6);
                        }

                        var3.remove(var6);
                        if (!var9.isEmpty()) {
                            var3.put(var6, paid(var2.profile(), NativeFoundationItemData.image(var9)));
                        }

                        var5 = true;
                    }
                }

                return var5 ? persist(var0, var2, var3, var4, var2.level(), var2.experience(), true) : var0.copy();
            } catch (IllegalArgumentException var10) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean canSetContainerItems(ItemStack var0, List<ItemStack> var1) {
        ItemStack var2 = containerPreview(var0, var1);
        return var2 != null && preserveDamage(var0, var2);
    }

    public static boolean setContainerItems(ServerPlayer var0, ItemStack var1, List<ItemStack> var2, int var3) {
        if (!var0.isSpectator()
            && var0.isAlive()
            && !var0.level().isClientSide
            && revision(var1) == var3
            && (var1 == var0.getMainHandItem() || var1 == var0.getOffhandItem())) {
            ItemStack var4 = containerPreview(var1, var2);
            if (var4 == null || !preserveDamage(var1, var4)) {
                return false;
            } else if (fingerprint(var4).equals(fingerprint(var1))) {
                return true;
            } else {
                NativeFoundationItemData.copyComponents(var1, var4);
                NativeFoodWeaponWear.manage(var0, var1);
                NativeFoodWeaponWear.componentsChanged(var1);
                var0.getInventory().setChanged();
                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean preserveDamage(ItemStack var0, ItemStack var1) {
        if (var0.isDamageableItem() && var0.getMaxDamage() > 0) {
            int var2 = var0.getDamageValue();
            int var3 = var1.getMaxDamage();
            if (var3 > 0 && var2 < var3) {
                var1.setDamageValue(var2);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private static CompoundTag authorRulesSnapshot(ItemStack var0) {
        return FoundationAuthorRuleSnapshots.snapshot(var0);
    }

    public static String fingerprintTag(Tag var0) {
        return FoundationCanonicalDigest.fingerprint("external-NBT", var0);
    }

    public static String canonical(Tag var0) {
        return HexFormat.of().formatHex(FoundationCanonicalDigest.canonical(var0));
    }

    private static int integer(CompoundTag var0, String var1, int var2, boolean var3) {
        if (!var0.contains(var1)) {
            require(!var3, "missing-" + var1);
            return var2;
        } else {
            require(var0.contains(var1, 3), "bad-type-" + var1);
            return var0.getInt(var1);
        }
    }

    private static void require(boolean var0, String var1) {
        if (!var0) {
            throw new IllegalArgumentException(var1);
        }
    }

    public static int durabilityAlloys(ItemStack var0) {
        Profile var1 = nativeProfile(var0);
        return var1 == null ? 0 : WeaponDurabilityBuild.foundationAlloys(var0, var1);
    }

    public static final class Change {
        private final String before;
        private final ItemStack after;
        private final ItemStack refund;
        private final int consume;

        private Change(String var1, ItemStack var2, ItemStack var3, int var4) {
            this.before = var1;
            this.after = var2.copy();
            this.refund = var3.copy();
            this.consume = var4;
        }

        public String before() {
            return this.before;
        }

        public ItemStack after() {
            return this.after.copy();
        }

        public ItemStack refund() {
            return this.refund.copy();
        }

        public int consume() {
            return this.consume;
        }
    }

    public static final class Entry {
        private final Cell value;
        private final CompoundTag item;

        private Entry(Cell var1, CompoundTag var2) {
            this.value = var1;
            this.item = var2 == null ? null : var2.copy();
        }

        public Cell value() {
            return this.value;
        }

        public CompoundTag payload() {
            return this.item == null ? null : this.item.copy();
        }

        public boolean paid() {
            return this.value.refundable();
        }

        public ItemStack storedItem() {
            return this.item == null ? ItemStack.EMPTY : NativeFoundationItemData.decode(this.item.copy());
        }
    }

    public static final class View {
        private final Profile profile;
        private final Map<Integer, FourWeaponFoundationAdapter.Entry> entries;
        private final Set<Integer> retired;
        private final CompoundTag root;
        private final int level;
        private final int experience;
        private final int revision;
        private final String failure;
        private final boolean migration;

        private View(
            Profile var1,
            Map<Integer, FourWeaponFoundationAdapter.Entry> var2,
            Set<Integer> var3,
            CompoundTag var4,
            int var5,
            int var6,
            int var7,
            String var8,
            boolean var9
        ) {
            this.profile = var1;
            this.entries = Map.copyOf(var2);
            this.retired = Set.copyOf(var3);
            this.root = var4 == null ? null : var4.copy();
            this.level = var5;
            this.experience = var6;
            this.revision = var7;
            this.failure = var8;
            this.migration = var9;
        }

        public boolean valid() {
            return this.failure.isEmpty();
        }

        public String failure() {
            return this.failure;
        }

        public Profile profile() {
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

        public Map<Integer, FourWeaponFoundationAdapter.Entry> entries() {
            return this.entries;
        }

        public Set<Integer> retired() {
            return this.retired;
        }

        public Map<Integer, Cell> cells() {
            HashMap var1 = new HashMap();
            this.entries.forEach((var1x, var2) -> var1.put(var1x, var2.value()));
            return Map.copyOf(var1);
        }

        public Status status() {
            return this.valid()
                ? FourWeaponFoundationRules.evaluate(this.profile, this.cells(), this.level, FourWeaponFoundationAdapter.paidValues(this.profile, this.entries))
                : null;
        }

        public CompoundTag rootCopy() {
            return this.root == null ? null : this.root.copy();
        }
    }

    private static final class PrivateSnapshot407 {
        private final java.util.LinkedHashMap<String, net.minecraft.nbt.Tag> fields = new java.util.LinkedHashMap<>();
        private void put(String key, net.minecraft.nbt.Tag value) { fields.put(key, value); }
        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }
        private void putBoolean(String key, boolean value) { put(key, net.minecraft.nbt.ByteTag.valueOf(value)); }
        private void putString(String key, String value) { put(key, net.minecraft.nbt.StringTag.valueOf(value)); }
        private CompoundTag finish() {
            return NativeExpectedFieldObservation.compositionCompoundForRead407(fields);
        }
    }
}
