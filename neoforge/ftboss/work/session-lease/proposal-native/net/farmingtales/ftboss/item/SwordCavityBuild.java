package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition.Category;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Input;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Status;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.BuiltInRegistries;

public final class SwordCavityBuild {
    public static final String ROOT = "FTBossSwordStructure";

    private SwordCavityBuild() {
    }

    public static boolean applies(ItemStack var0) {
        return AdditionalWeaponUiEligibility.keepLegacyUi(var0) ? false : SwordCavityExtensions.supports(var0);
    }

    public static boolean active(ItemStack var0) {
        return applies(var0)
            && (
                NativeFoundationItemData.element(var0, "FTBossSwordStructure") != null
                    || NativeFoundationItemData.element(var0, "FTBossWeaponMaterials") != null
                    || FoodWeaponEvolution.evolutionLevel(var0) > 0
            );
    }

    public static int revision(ItemStack var0) {
        CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
        return var1 == null ? 0 : var1.getInt("Revision");
    }

    private static boolean linksEnabled(ItemStack var0) {
        CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
        return var1 != null && var1.getBoolean("LinksEnabled");
    }

    public static Component name(Slot var0) {
        return Component.translatable("part.ftboss.sword." + var0.id());
    }

    public static Component stateName(Cell var0) {
        return Component.translatable("state.ftboss.sword." + var0.state().name().toLowerCase(Locale.ROOT));
    }

    public static List<Cell> cells(ItemStack var0) {
        return cells(var0, SwordNativeMaterials.parts(var0));
    }

    public static ItemStack displayItem(ItemStack var0, Slot var1) {
        return displayItem(var0, var1, SwordNativeMaterials.parts(var0));
    }

    public static ItemStack displayItem(ItemStack var0, Slot var1, List<Piece> var2) {
        if (applies(var0) && var1 != null) {
            Cell var3 = cells(var0, var2).get(var1.ordinal());
            if (!var3.present()) {
                return ItemStack.EMPTY;
            } else {
                ItemStack var4 = storedItem(var0, var1);
                if (!var4.isEmpty()) {
                    return var4;
                } else {
                    if (var3.state() == State.NATIVE) {
                        ItemStack var5 = SwordNativeMaterials.display(var0, var1.ordinal(), var2);
                        if (!var5.isEmpty()) {
                            return var5;
                        }
                    }

                    for (String var6 : WeaponMaterialCatalogRules.offerings(var3.material())) {
                        ResourceLocation var7 = ResourceLocation.tryParse(var6);
                        if (var7 != null && BuiltInRegistries.ITEM.containsKey(var7)) {
                            return new ItemStack((ItemLike)BuiltInRegistries.ITEM.getOptional(var7).orElse(null));
                        }
                    }

                    if (var3.state() == State.NATIVE) {
                        for (ItemStack var8 : ((SwordItem)var0.getItem()).getTier().getRepairIngredient().getItems()) {
                            if (!var8.isEmpty()) {
                                ItemStack var9 = var8.copy();
                                var9.setCount(1);
                                return var9;
                            }
                        }

                        ItemStack var12 = var0.copy();
                        var12.setCount(1);
                        return var12;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static List<Component> materialEffects(Material var0) {
        return List.of(Component.translatable("effect.ftboss.sword.material." + var0.id()));
    }

    private static void initialize(ItemStack var0) {
        CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
        if (var1 == null || var1.getInt("Version") != 2) {
            List var2 = cells(var0);
            CompoundTag var3 = var1 == null ? new CompoundTag() : var1.getCompound("Cells").copy();
            int var4 = revision(var0);
            NativeCavityItemData.initialize(var0, var3, var4);

            for (Slot var9 : Slot.values()) {
                writeCell(var0, var9, (Cell)var2.get(var9.ordinal()), null);
            }
        }
    }

    private static void writeCell(ItemStack var0, Slot var1, Cell var2, CompoundTag var3) {
        NativeCavityItemData.writeCell(var0, var1, var2, var3);
    }

    private static Cell vacancy(Slot var0) {
        return new Cell(var0.required ? State.MISSING : State.EMPTY, Material.ORIGINAL);
    }

    private static int returnDestination(ServerPlayer var0, ItemStack var1, ItemStack var2, int var3) {
        for (int var4 = 0; var4 < 36; var4++) {
            ItemStack var5 = var0.getInventory().getItem(var4);
            if (var5 != var1
                && (
                    var5.isEmpty()
                        || var4 == var3 && var5.getCount() == 1 && !var0.getAbilities().instabuild
                        || ItemStack.isSameItemSameComponents(var5, var2) && var5.getCount() < var5.getMaxStackSize()
                )) {
                return var4;
            }
        }

        return -1;
    }

    private static void returnItem(ServerPlayer var0, ItemStack var1, int var2) {
        ItemStack var3 = var0.getInventory().getItem(var2);
        if (var3.isEmpty()) {
            var0.getInventory().setItem(var2, var1);
        } else {
            var3.grow(1);
        }
    }

    public static boolean canRemove(ItemStack var0, Slot var1) {
        if (editable(var0) && var1 != null && !storedItem(var0, var1).isEmpty()) {
            ItemStack var2 = var0.copy();
            initialize(var2);
            NativeCavityItemData.enableGrid(var2);
            writeCell(var2, var1, vacancy(var1), null);
            return preservesLife(var0, var2);
        } else {
            return false;
        }
    }

    public static boolean remove(ServerPlayer var0, Slot var1, int var2) {
        if (var0.isAlive() && !var0.isSpectator()) {
            ItemStack var3 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
            if (revision(var3) == var2 && canRemove(var3, var1)) {
                ItemStack var4 = storedItem(var3, var1);
                int var5 = returnDestination(var0, var3, var4, -1);
                if (var5 < 0) {
                    var0.sendSystemMessage(Component.translatable("screen.ftboss.sword.inventory_full"));
                    return false;
                } else {
                    int var6 = var3.getMaxDamage();
                    int var7 = var3.getDamageValue();
                    FoodWeaponLifecycle.ensureTracked(var3);
                    SwordNativeMaterials.capture(var3, var0.serverLevel());
                    initialize(var3);
                    writeCell(var3, var1, vacancy(var1), null);
                    returnItem(var0, var4, var5);
                    finishChange(var0, var3, var2, var7, var6);
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static ItemStack movedPreview(ItemStack var0, Slot var1, Slot var2) {
        ItemStack var3 = var0.copy();
        initialize(var3);
        NativeCavityItemData.enableGrid(
            var3);
        List var4 = cells(var3);
        CompoundTag var5 = NativeFoundationItemData.element(var3, "FTBossSwordStructure").getCompound("Cells").copy();
        Cell var6 = (Cell)var4.get(var2.ordinal());
        writeCell(var3, var2, (Cell)var4.get(var1.ordinal()), var5.getCompound(var1.name()).getCompound("Item"));
        writeCell(var3, var1, var6.installed() ? var6 : vacancy(var1), var6.installed() ? var5.getCompound(var2.name()).getCompound("Item") : null);
        return var3;
    }

    public static boolean move(ServerPlayer var0, Slot var1, Slot var2, int var3) {
        if (var0.isAlive() && !var0.isSpectator()) {
            ItemStack var4 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
            if (revision(var4) == var3 && canMove(var4, var1, var2)) {
                int var5 = var4.getMaxDamage();
                int var6 = var4.getDamageValue();
                FoodWeaponLifecycle.ensureTracked(var4);
                SwordNativeMaterials.capture(var4, var0.serverLevel());
                ItemStack var7 = movedPreview(var4, var1, var2);
                NativeCavityItemData.copyBuildData(var4, var7);
                finishChange(var0, var4, var3, var6, var5);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean losePart(ItemStack var0) {
        if (active(var0) && revision(var0) != Integer.MAX_VALUE && revision(var0) >= 0) {
            Slot var1 = SwordCavityRules.lossSlot(cells(var0));
            if (var1 == null) {
                return false;
            } else {
                initialize(var0);
                writeCell(var0, var1, new Cell(State.MISSING, Material.ORIGINAL), null);
                FoodWeaponData.edit(var0, "FTBossSwordStructure", structure -> structure.putInt("Revision", revision(var0) + 1));
                return true;
            }
        } else {
            return false;
        }
    }

    public static Component stateName(ItemStack var0, Slot var1) {
        return stateName(cells(var0).get(var1.ordinal()));
    }

    public static Material materialFor(ItemStack var0) {
        return SwordMaterialExtensions.baseMaterial(var0);
    }

    private static Material nativeMaterial(ItemStack var0, Slot var1) {
        if (var1 != Slot.GRIP && var1 != Slot.BINDING) {
            if (var1.ordinal() >= 4) {
                return Material.ORIGINAL;
            } else if (var0.getItem() instanceof TieredItem var2) {
                Tier var9 = var2.getTier();
                if (var9 == Tiers.WOOD) {
                    return Material.WOOD;
                } else if (var9 == Tiers.STONE) {
                    return Material.STONE;
                } else if (var9 == Tiers.IRON) {
                    return Material.IRON;
                } else if (var9 == Tiers.GOLD) {
                    return Material.GOLD;
                } else if (var9 == Tiers.DIAMOND) {
                    return Material.DIAMOND;
                } else if (var9 == Tiers.NETHERITE) {
                    return Material.NETHERITE;
                } else {
                    for (ItemStack var7 : var9.getRepairIngredient().getItems()) {
                        Material var8 = materialFor(var7);
                        if (var8 != Material.ORIGINAL) {
                            return var8;
                        }
                    }

                    return Material.ORIGINAL;
                }
            } else {
                return Material.ORIGINAL;
            }
        } else {
            return Material.WOOD;
        }
    }

    public static List<Cell> cells(ItemStack var0, List<Piece> var1) {
        if (!applies(var0)) {
            return List.of();
        } else {
            CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
            CompoundTag var3 = NativeFoundationItemData.element(var0, "FTBossWeaponMaterials");
            ArrayList var4 = new ArrayList();

            for (Slot var8 : Slot.values()) {
                if (var2 != null) {
                    CompoundTag var9 = var2.getCompound("Cells").getCompound(var8.name());
                    int var10 = var2.getInt("Version");
                    String var12 = var9.getString("State");

                    State var11 = switch (var12) {
                        case "native" -> State.NATIVE;
                        case "installed" -> State.INSTALLED;
                        case "missing" -> State.MISSING;
                        case "empty", "" -> State.EMPTY;
                        default -> State.INVALID;
                    };
                    if (var10 == 1 && var8.ordinal() >= 4 && var11 == State.NATIVE) {
                        var11 = State.EMPTY;
                    }

                    if (var10 == 1 && var8.ordinal() >= 4 && var11 == State.MISSING && !var9.getBoolean("EverPresent")) {
                        var11 = State.EMPTY;
                    }

                    if (var11 == State.NATIVE && !SwordNativeMaterialRules.hasPiece(var1, var8.ordinal())) {
                        var11 = var8.required ? State.MISSING : State.EMPTY;
                    }

                    if (var10 == 2
                        && var11 == State.EMPTY
                        && !var9.getBoolean("EverPresent")
                        && SwordNativeMaterialRules.hasPiece(var1, var8.ordinal())
                        && NativeFoundationItemData.element(var0, "FTBossSwordNativeMaterials") != null) {
                        var11 = State.NATIVE;
                    }

                    if (var10 == 2 && var11 == State.INSTALLED && var9.contains("Item") && (!var9.contains("Item", 10) || storedItem(var0, var8).isEmpty())) {
                        var11 = State.INVALID;
                    }

                    Material var15 = var11 == State.NATIVE ? nativeMaterial(var0, var8) : Material.byId(var9.getString("Material"));
                    if (var10 != 1 && var10 != 2 || var11 == State.INSTALLED && !SwordCavityRules.accepts(var8, var15)) {
                        var11 = State.INVALID;
                    }

                    if (var11 == State.EMPTY || var11 == State.MISSING || var11 == State.INVALID) {
                        var15 = Material.ORIGINAL;
                    }

                    var4.add(new Cell(var11, var15));
                } else {
                    Material var14 = var3 != null && var8.legacyPart != null ? Material.byId(var3.getString(var8.legacyPart.name())) : Material.ORIGINAL;
                    var4.add(
                        var14 == Material.ORIGINAL
                            ? (
                                SwordNativeMaterialRules.hasPiece(var1, var8.ordinal())
                                    ? new Cell(State.NATIVE, nativeMaterial(var0, var8))
                                    : SwordCavityRules.nativeCell(var8, nativeMaterial(var0, var8))
                            )
                            : new Cell(State.INSTALLED, var14)
                    );
                }
            }

            return List.copyOf(var4);
        }
    }

    public static boolean cellUnlocked(ItemStack var0, int var1) {
        return applies(var0) && SwordFunctionRules.unlocked(SwordCavityExtensions.functionProfile(var0), var1, FoodWeaponEvolution.evolutionLevel(var0));
    }

    public static Status functionStatus(ItemStack var0) {
        if (!SwordCompatibilityRules.upgradeReferenceBudget(FourWeaponFoundationAdapter.registryId(var0), List.of()).isEmpty()) {
            return moddedFunctionStatus(var0);
        } else {
            List var1 = SwordNativeMaterials.parts(var0);
            List var2 = cells(var0, var1);
            ArrayList var3 = new ArrayList();
            HashMap var4 = new HashMap();
            Profile var5 = SwordCavityExtensions.functionProfile(var0);

            for (Slot var9 : Slot.values()) {
                if (var2.size() != 27) {
                    break;
                }

                Cell var10 = (Cell)var2.get(var9.ordinal());
                ItemStack var11 = var10.state() == State.NATIVE && ((Piece)var1.get(var9.ordinal())).count() == 1
                    ? SwordNativeMaterials.display(var0, var9.ordinal(), var1)
                    : storedItem(var0, var9);
                Optional<net.farmingtales.ftboss.api.SwordMaterialDefinition> var12 = SwordMaterialExtensions.resolve(var11);
                ResourceLocation nativeReferenceId = var10.state() == State.NATIVE
                    && ((Piece)var1.get(var9.ordinal())).count() == 1 && !var11.isEmpty()
                        ? BuiltInRegistries.ITEM.getKey(var11.getItem()) : null;
                String var13 = var12.<String>map(var0x -> var0x.itemId().toString())
                    .orElse(nativeReferenceId != null ? nativeReferenceId.toString() : (var10.installed() ? "ftboss:legacy_" + var10.material().id() : ""));
                var12.ifPresent(var2x -> {
                    if (SwordMaterialExtensions.registered(var2x.itemId()) || var2x.categories().contains(Category.FOOD)) {
                        var4.put(var13, var2x.functions());
                    }
                });
                var3.add(
                    new Input(
                        var13,
                        var12.<Material>map(var0x -> var0x.baseMaterial()).orElse(var10.material()),
                        var10.present() && (var10.state() != State.NATIVE || !var11.isEmpty())
                    )
                );
            }

            if (var5 != null) {
                var5 = var5.withItemValues(var4);
            }

            Status var14 = SwordFunctionRules.evaluateItems(var5, var3, FoodWeaponEvolution.evolutionLevel(var0));
            return SwordCavityRules.usable(var2) ? var14 : new Status(false, var14.total(), var14.minimum(), List.of(Function.values()), var14.unlockedCells());
        }
    }

    public static boolean missingRequired(ItemStack var0) {
        if (!applies(var0)) {
            return false;
        } else if (SwordCavityExtensions.functionProfile(var0) != null) {
            return !functionStatus(var0).ready();
        } else {
            List var1 = cells(var0);
            if (!SwordCavityRules.usable(var1)) {
                return true;
            } else {
                for (int var2 = 0; var2 < 4; var2++) {
                    if (!((Cell)var1.get(var2)).present()) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    private static List<Cell> effectiveCells(ItemStack var0) {
        ArrayList var1 = new ArrayList<>(cells(var0));
        boolean var2 = applies(var0);
        Profile var3 = var2 ? SwordCavityExtensions.functionProfile(var0) : null;
        int var4 = var2 ? FoodWeaponEvolution.evolutionLevel(var0) : 0;

        for (Slot var8 : Slot.values()) {
            if (!var2 || !SwordFunctionRules.unlocked(var3, var8.ordinal(), var4)) {
                var1.set(var8.ordinal(), new Cell(State.EMPTY, Material.ORIGINAL));
            }
        }

        return var1;
    }

    public static List<Material> legacyMaterials(ItemStack var0) {
        ArrayList var1 = new ArrayList<>(List.of(Material.ORIGINAL, Material.ORIGINAL, Material.ORIGINAL));
        List var2 = effectiveCells(var0);

        for (Slot var6 : Slot.values()) {
            if (var6.legacyPart != null) {
                var1.set(var6.legacyPart.ordinal(), SwordCavityRules.installed(var2, var6));
            }
        }

        return List.copyOf(var1);
    }

    public static List<Material> powerMaterials(ItemStack var0) {
        List var1 = effectiveCells(var0);
        if (SwordCavityRules.usable(var1) && !missingRequired(var0)) {
            ArrayList var2 = new ArrayList();

            for (Slot var6 : Slot.values()) {
                Material var7 = SwordCavityRules.installed(var1, var6);
                if (var7 != Material.ORIGINAL && !var2.contains(var7)) {
                    var2.add(var7);
                }
            }

            return List.copyOf(var2);
        } else {
            return List.of();
        }
    }

    private static Build legacyBuild(ItemStack var0) {
        List var1 = effectiveCells(var0);
        Build var2 = FoodWeaponBuildRules.build(legacyMaterialValues(var1));
        if (!SwordCavityRules.usable(var1)) {
            return new Build(var2.durability(), var2.wear(), 0.0, 0.0, 0.0);
        } else {
            double var3 = var2.durability();
            double var5 = var2.wear();
            double var7 = var2.damage();
            double var9 = var2.speed();
            double var11 = var2.reach();
            double[][] var13 = new double[][]{
                {0.0, 0.0, 0.0, 0.0},
                {0.25, 0.1, 0.0, 0.15},
                {0.0, 0.0, 0.0, 0.0},
                {0.1, 0.0, 0.12, 0.0},
                {0.0, 0.0, 0.0, 0.0},
                {0.12, 0.0, 0.04, 0.0},
                {0.08, 0.0, 0.12, 0.0},
                {0.0, 0.08, 0.04, 0.0}
            };

            for (int var14 = 0; var14 < var13.length; var14++) {
                Cell var15 = (Cell)var1.get(var14);
                if (var15.installed() && Slot.byIndex(var14).legacyPart == null) {
                    Material var16 = var15.material();
                    var3 *= 1.0 + (var16.durability - 1.0) * var13[var14][0];
                    var5 *= 1.0 + (var16.wear - 1.0) * var13[var14][0];
                    var7 *= 1.0 + (var16.damage - 1.0) * var13[var14][1];
                    var9 *= 1.0 + (var16.speed - 1.0) * var13[var14][2];
                    var11 += var16.reach * var13[var14][3];
                }
            }

            List var17 = links(var0);
            if (var17.contains(Link.FROST_EDGE)) {
                var3 *= 1.08;
                var5 *= 0.94;
            }

            if (var17.contains(Link.COPPER_CIRCUIT)) {
                var9 *= 1.04;
                var11 += 0.1;
            }

            return new Build(var3, Math.max(0.1, var5), var7, var9, Math.min(0.75, var11));
        }
    }

    public static List<Link> links(ItemStack var0) {
        if (linksEnabled(var0) && !missingRequired(var0)) {
            List var1 = effectiveCells(var0);
            CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
            if (var2 != null && var2.getBoolean("GridValues")) {
                return SwordCavityRules.links(var1);
            } else if (!SwordCavityRules.usable(var1)) {
                return List.of();
            } else {
                Material var3 = SwordCavityRules.installed(var1, Slot.CORE);
                Material var4 = SwordCavityRules.installed(var1, Slot.BLADE);
                if (var3 != Material.BOSS_FROST || var4 != Material.BOSS_FROST && var4 != Material.DIAMOND) {
                    return var3 != Material.BOSS_CAPACITOR || var4 != Material.COPPER && SwordCavityRules.installed(var1, Slot.INLAY) != Material.COPPER
                        ? List.of()
                        : List.of(Link.COPPER_CIRCUIT);
                } else {
                    return List.of(Link.FROST_EDGE);
                }
            }
        } else {
            return List.of();
        }
    }

    public static ItemStack storedItem(ItemStack var0, Slot var1) {
        CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
        if (var2 != null && var2.getInt("Version") == 2) {
            CompoundTag var3 = var2.getCompound("Cells").getCompound(var1.name());
            if (var3.getString("State").equals("installed") && var3.contains("Item", 10)) {
                ItemStack var4 = NativeCavityItemData.decode(NativeFoundationItemData.registries(), var3.getCompound("Item"));
                return var4.getCount() == 1 && !var4.isEmpty() && materialFor(var4) == Material.byId(var3.getString("Material")) ? var4 : ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack preview(ItemStack var0, Slot var1, Material var2) {
        ItemStack var3 = var0.copy();
        if (applies(var3) && SwordCavityRules.accepts(var1, var2) && cellUnlocked(var3, var1.ordinal())) {
            initialize(var3);
            NativeCavityItemData.enableGrid(var3);
            writeCell(var3, var1, new Cell(State.INSTALLED, var2), null);
            return var3;
        } else {
            return var3;
        }
    }

    private static boolean editable(ItemStack var0) {
        if (applies(var0)
            && revision(var0) >= 0
            && revision(var0) != Integer.MAX_VALUE
            && (!var0.isDamageableItem() || var0.getMaxDamage() <= 0 || var0.getDamageValue() < var0.getMaxDamage())) {
            if (!SwordCavityRules.usable(cells(var0))) {
                return false;
            } else {
                CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
                if (var1 != null && var1.getInt("Version") != 1 && var1.getInt("Version") != 2) {
                    return false;
                } else {
                    CompoundTag var2 = NativeFoundationItemData.element(var0, "FTBossWeaponLifecycle");
                    return var2 == null || !var2.getBoolean("Broken");
                }
            }
        } else {
            return false;
        }
    }

    public static boolean containerEditable(ItemStack var0) {
        return editable(var0);
    }

    public static List<ItemStack> containerItems(ItemStack var0) {
        ArrayList var1 = new ArrayList();

        for (Slot var5 : Slot.values()) {
            var1.add(storedItem(var0, var5));
        }

        return var1;
    }

    private static boolean samePiece(ItemStack var0, ItemStack var1) {
        return var0.isEmpty() && var1.isEmpty() || var0.getCount() == var1.getCount() && ItemStack.isSameItemSameComponents(var0, var1);
    }

    private static ItemStack containerPreview(ItemStack var0, List<ItemStack> var1) {
        if (editable(var0) && var1 != null && var1.size() == 27) {
            ItemStack var2 = var0.copy();
            initialize(var2);
            NativeCavityItemData.enableGrid(var2);

            for (Slot var6 : Slot.values()) {
                ItemStack var7 = (ItemStack)var1.get(var6.ordinal());
                if (var7 == null || !var7.isEmpty() && (var7.getCount() != 1 || !SwordCavityRules.accepts(var6, materialFor(var7)))) {
                    return ItemStack.EMPTY;
                }

                ItemStack var8 = storedItem(var0, var6);
                if (!samePiece(var8, var7)) {
                    if (!var7.isEmpty() && !cellUnlocked(var0, var6.ordinal())) {
                        return ItemStack.EMPTY;
                    }

                    writeCell(
                        var2,
                        var6,
                        var7.isEmpty() ? vacancy(var6) : new Cell(State.INSTALLED, materialFor(var7)),
                        var7.isEmpty() ? null : NativeFoundationItemData.image(var7)
                    );
                }
            }

            return var2;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static boolean canSetContainerItems(ItemStack var0, List<ItemStack> var1) {
        ItemStack var2 = containerPreview(var0, var1);
        return !var2.isEmpty() && preservesLife(var0, var2);
    }

    public static boolean setContainerItems(ServerPlayer var0, ItemStack var1, List<ItemStack> var2, int var3) {
        if (var0.isAlive() && !var0.isSpectator() && revision(var1) == var3) {
            ItemStack var4 = containerPreview(var1, var2);
            if (!var4.isEmpty() && preservesLife(var1, var4)) {
                boolean var5 = false;

                for (Slot var9 : Slot.values()) {
                    if (!samePiece(storedItem(var1, var9), (ItemStack)var2.get(var9.ordinal()))) {
                        var5 = true;
                    }
                }

                return !var5 ? true : applyContainerChange(var0, var1, var4, var3);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean applyContainerChange(ServerPlayer var0, ItemStack var1, ItemStack var2, int var3) {
        int var4 = var1.getDamageValue();
        int var5 = var1.getMaxDamage();
        NativeStackComponents.Snapshot var6 = NativeStackComponents.capture(var1);

        try {
            FoodWeaponLifecycle.ensureTracked(var1);
            SwordNativeMaterials.capture(var1, var0.serverLevel());
            NativeCavityItemData.copyBuildData(var1, var2);
            finishChange(var0, var1, var3, var4, var5);
            return true;
        } catch (RuntimeException var8) {
            var6.restore(var1);
            throw var8;
        }
    }

    public static boolean canClearReference(ItemStack var0, Slot var1) {
        if (editable(var0) && var1 != null && storedItem(var0, var1).isEmpty() && cells(var0).get(var1.ordinal()).present()) {
            ItemStack var2 = var0.copy();
            initialize(var2);
            NativeCavityItemData.enableGrid(var2);
            writeCell(var2, var1, vacancy(var1), null);
            return preservesLife(var0, var2);
        } else {
            return false;
        }
    }

    public static boolean clearReference(ServerPlayer var0, ItemStack var1, Slot var2, int var3) {
        if (var0.isAlive() && !var0.isSpectator() && revision(var1) == var3 && canClearReference(var1, var2)) {
            ItemStack var4 = var1.copy();
            initialize(var4);
            writeCell(var4, var2, vacancy(var2), null);
            return applyContainerChange(var0, var1, var4, var3);
        } else {
            return false;
        }
    }

    public static boolean canReplace(ItemStack var0, Slot var1, Material var2) {
        if (editable(var0) && SwordCavityRules.accepts(var1, var2) && cellUnlocked(var0, var1.ordinal())) {
            Cell var3 = cells(var0).get(var1.ordinal());
            return (!var3.installed() || var3.material() != var2) && preservesLife(var0, preview(var0, var1, var2));
        } else {
            return false;
        }
    }

    public static String replacementFailure(ItemStack var0, Slot var1, Material var2) {
        if (!applies(var0) || var1 == null || !SwordCavityRules.accepts(var1, var2)) {
            return "screen.ftboss.sword.invalid_material";
        } else if (!cellUnlocked(var0, var1.ordinal())) {
            return "screen.ftboss.cavity.cell_locked";
        } else {
            CompoundTag var3 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
            if ((var3 == null || var3.getInt("Version") == 1 || var3.getInt("Version") == 2) && revision(var0) >= 0 && revision(var0) != Integer.MAX_VALUE) {
                CompoundTag var4 = NativeFoundationItemData.element(var0, "FTBossWeaponLifecycle");
                if ((!var0.isDamageableItem() || var0.getMaxDamage() <= 0 || var0.getDamageValue() < var0.getMaxDamage())
                    && (var4 == null || !var4.getBoolean("Broken"))) {
                    Cell var5 = cells(var0).get(var1.ordinal());
                    if (var5.installed() && var5.material() == var2) {
                        return "screen.ftboss.sword.same_material";
                    } else {
                        return canReplace(var0, var1, var2) ? "" : "message.ftboss.material.repair_first";
                    }
                } else {
                    return "screen.ftboss.sword.depleted";
                }
            } else {
                return "screen.ftboss.sword.version_locked";
            }
        }
    }

    public static boolean replace(ServerPlayer var0, Slot var1, int var2, int var3) {
        if (var0.isAlive() && !var0.isSpectator() && var2 >= 0 && var2 < 36) {
            ItemStack var4 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
            ItemStack var5 = var0.getInventory().getItem(var2);
            Material var6 = materialFor(var5);
            if (var5 != var4 && !var5.isEmpty() && revision(var4) == var3 && canReplace(var4, var1, var6)) {
                ItemStack var7 = storedItem(var4, var1);
                int var8 = var7.isEmpty() ? -1 : returnDestination(var0, var4, var7, var2);
                if (!var7.isEmpty() && var8 < 0) {
                    var0.sendSystemMessage(Component.translatable("screen.ftboss.sword.inventory_full"));
                    return false;
                } else {
                    int var9 = var4.getMaxDamage();
                    int var10 = var4.getDamageValue();
                    ItemStack var11 = var5.copy();
                    var11.setCount(1);
                    FoodWeaponLifecycle.ensureTracked(var4);
                    SwordNativeMaterials.capture(var4, var0.serverLevel());
                    initialize(var4);
                    writeCell(var4, var1, new Cell(State.INSTALLED, var6), var0.getAbilities().instabuild ? null : NativeFoundationItemData.image(var11, var0.serverLevel().registryAccess()));
                    if (!var0.getAbilities().instabuild) {
                        var5.shrink(1);
                    }

                    if (!var7.isEmpty()) {
                        returnItem(var0, var7, var8);
                    }

                    finishChange(var0, var4, var3, var10, var9);
                    var0.sendSystemMessage(
                        Component.translatable("message.ftboss.sword.inserted", WeaponMaterialBuild.materialName(var6)).withStyle(ChatFormatting.GOLD)
                    );
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean canMove(ItemStack var0, Slot var1, Slot var2) {
        return editable(var0)
            && var1 != null
            && var2 != null
            && var1 != var2
            && cellUnlocked(var0, var1.ordinal())
            && cellUnlocked(var0, var2.ordinal())
            && cells(var0).get(var1.ordinal()).installed()
            && preservesLife(var0, movedPreview(var0, var1, var2));
    }

    private static boolean preservesLife(ItemStack var0, ItemStack var1) {
        if (var1.isEmpty()) {
            return false;
        } else if (!var1.isDamageableItem()) {
            return true;
        } else {
            int var2 = var1.getMaxDamage();
            return var2 > 0 && var0.getDamageValue() < var2;
        }
    }

    private static void finishChange(ServerPlayer var0, ItemStack var1, int var2, int var3, int var4) {
        NativeCavityItemData.enableGrid(var1);
        FoodWeaponData.edit(var1, "FTBossSwordStructure", structure -> structure.putInt("Revision", var2 + 1));
        NativeFoodWeaponWear.manage(var0, var1);
        FoodWeaponLifecycle.materialDamage(var1, var3);
        var0.getInventory().setChanged();
        var0.containerMenu.broadcastChanges();
    }

    static ItemStack nativeWearCandidate(ItemStack var0, Slot var1) {
        if (var1 != null
            && var1 != Slot.CORE
            && !var1.required
            && active(var0)
            && !missingRequired(var0)
            && revision(var0) >= 0
            && revision(var0) != Integer.MAX_VALUE) {
            List var2 = cells(var0);
            if (var2.size() == 27 && ((Cell)var2.get(var1.ordinal())).state() == State.INSTALLED) {
                ItemStack var3 = var0.copy();
                initialize(var3);
                writeCell(var3, var1, new Cell(State.EMPTY, Material.ORIGINAL), null);
                FoodWeaponData.edit(var3, "FTBossSwordStructure", structure -> structure.putInt("Revision", revision(var0) + 1));
                return SwordCavityRules.usable(cells(var3)) && !missingRequired(var3) && functionStatus(var3).ready() ? var3 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static List<Material> legacyMaterialValues(List<Cell> var0) {
        ArrayList var1 = new ArrayList<>(List.of(Material.ORIGINAL, Material.ORIGINAL, Material.ORIGINAL));

        for (Slot var5 : Slot.values()) {
            if (var5.legacyPart != null) {
                var1.set(var5.legacyPart.ordinal(), SwordCavityRules.installed(var0, var5));
            }
        }

        return var1;
    }

    private static Status moddedFunctionStatus(ItemStack var0) {
        List var1 = SwordNativeMaterials.parts(var0);
        List var2 = cells(var0, var1);
        ArrayList var3 = new ArrayList();
        HashMap var4 = new HashMap();
        Profile var5 = SwordCavityExtensions.functionProfile(var0);
        ResourceLocation var6 = BuiltInRegistries.ITEM.getKey(var0.getItem());
        boolean var7 = var6 != null && !SwordCompatibilityRules.upgradeReferenceBudget(var6.toString(), List.of()).isEmpty();

        for (Slot var11 : Slot.values()) {
            if (var2.size() != 27) {
                break;
            }

            Cell var12 = (Cell)var2.get(var11.ordinal());
            ItemStack var13 = var12.state() == State.NATIVE && ((Piece)var1.get(var11.ordinal())).count() == 1
                ? SwordNativeMaterials.display(var0, var11.ordinal(), var1)
                : storedItem(var0, var11);
            Optional<net.farmingtales.ftboss.api.SwordMaterialDefinition> var14 = SwordMaterialExtensions.resolve(var13);
            ResourceLocation var15 = var13.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(var13.getItem());
            String var16 = var14.<String>map(var0x -> var0x.itemId().toString())
                .orElse(var15 != null ? var15.toString() : (var12.installed() ? "ftboss:legacy_" + var12.material().id() : ""));
            String var17 = var16;
            if (var7 && var12.state() == State.NATIVE && var5 != null) {
                var17 = "ftboss:native_reference/" + var11.ordinal();
                Values var18 = var5.itemValues().get(var16);
                if (var18 == null) {
                    var18 = SwordCompatibilityRules.materialFunctions(SwordCompatibilityRules.material(var16));
                }

                var4.put(var17, var18);
            } else {
                var14.ifPresent(var2x -> {
                    if (SwordMaterialExtensions.registered(var2x.itemId()) || var2x.categories().contains(Category.FOOD)) {
                        var4.put(var16, var2x.functions());
                    }
                });
            }

            var3.add(
                new Input(
                    var17,
                    var14.<Material>map(var0x -> var0x.baseMaterial()).orElse(var12.material()),
                    var12.present() && (var12.state() != State.NATIVE || !var13.isEmpty())
                )
            );
        }

        if (var5 != null) {
            var5 = var5.withItemValues(var4);
        }

        Status var19 = SwordFunctionRules.evaluateItems(var5, var3, FoodWeaponEvolution.evolutionLevel(var0));
        return SwordCavityRules.usable(var2) ? var19 : new Status(false, var19.total(), var19.minimum(), List.of(Function.values()), var19.unlockedCells());
    }

    public static Build build(ItemStack var0) {
        CompoundTag var1 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
        Build var2 = var1 != null && var1.getBoolean("GridValues") ? SwordCavityRules.gridBuild(effectiveCells(var0), linksEnabled(var0)) : legacyBuild(var0);
        return missingRequired(var0)
            ? new Build(var2.durability(), var2.wear(), 0.0, 0.0, 0.0)
            : WeaponDurabilityBuild.combat(var0, SwordCavityExtensions.applyBuild(var0, var2));
    }
}
