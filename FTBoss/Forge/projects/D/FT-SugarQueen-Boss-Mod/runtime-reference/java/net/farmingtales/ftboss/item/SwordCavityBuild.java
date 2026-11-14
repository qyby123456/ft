/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.TieredItem
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.AdditionalWeaponUiEligibility;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponDurabilityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class SwordCavityBuild {
    public static final String ROOT = "FTBossSwordStructure";

    private SwordCavityBuild() {
    }

    public static boolean applies(ItemStack itemStack) {
        if (AdditionalWeaponUiEligibility.keepLegacyUi(itemStack)) {
            return false;
        }
        return SwordCavityExtensions.supports(itemStack);
    }

    public static boolean active(ItemStack itemStack) {
        return SwordCavityBuild.applies(itemStack) && (itemStack.m_41737_(ROOT) != null || itemStack.m_41737_("FTBossWeaponMaterials") != null || FoodWeaponEvolution.evolutionLevel(itemStack) > 0);
    }

    public static int revision(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        return compoundTag == null ? 0 : compoundTag.m_128451_("Revision");
    }

    private static boolean linksEnabled(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        return compoundTag != null && compoundTag.m_128471_("LinksEnabled");
    }

    public static Component name(SwordCavityRules.Slot slot) {
        return Component.m_237115_((String)("part.ftboss.sword." + slot.id()));
    }

    public static Component stateName(SwordCavityRules.Cell cell) {
        return Component.m_237115_((String)("state.ftboss.sword." + cell.state().name().toLowerCase(Locale.ROOT)));
    }

    public static List<SwordCavityRules.Cell> cells(ItemStack itemStack) {
        return SwordCavityBuild.cells(itemStack, SwordNativeMaterials.parts(itemStack));
    }

    public static ItemStack displayItem(ItemStack itemStack, SwordCavityRules.Slot slot) {
        return SwordCavityBuild.displayItem(itemStack, slot, SwordNativeMaterials.parts(itemStack));
    }

    public static ItemStack displayItem(ItemStack itemStack, SwordCavityRules.Slot slot, List<SwordNativeMaterialRules.Piece> list) {
        ItemStack itemStack2;
        if (!SwordCavityBuild.applies(itemStack) || slot == null) {
            return ItemStack.f_41583_;
        }
        SwordCavityRules.Cell cell = SwordCavityBuild.cells(itemStack, list).get(slot.ordinal());
        if (!cell.present()) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack3 = SwordCavityBuild.storedItem(itemStack, slot);
        if (!itemStack3.m_41619_()) {
            return itemStack3;
        }
        if (cell.state() == SwordCavityRules.State.NATIVE && !(itemStack2 = SwordNativeMaterials.display(itemStack, slot.ordinal(), list)).m_41619_()) {
            return itemStack2;
        }
        for (String string : WeaponMaterialCatalogRules.offerings(cell.material())) {
            ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)string);
            if (resourceLocation == null || !ForgeRegistries.ITEMS.containsKey(resourceLocation)) continue;
            return new ItemStack((ItemLike)ForgeRegistries.ITEMS.getValue(resourceLocation));
        }
        if (cell.state() == SwordCavityRules.State.NATIVE) {
            for (ItemStack itemStack4 : ((SwordItem)itemStack.m_41720_()).m_43314_().m_6282_().m_43908_()) {
                if (itemStack4.m_41619_()) continue;
                ItemStack itemStack5 = itemStack4.m_41777_();
                itemStack5.m_41764_(1);
                return itemStack5;
            }
            itemStack2 = itemStack.m_41777_();
            itemStack2.m_41764_(1);
            return itemStack2;
        }
        return ItemStack.f_41583_;
    }

    public static List<Component> materialEffects(FoodWeaponBuildRules.Material material) {
        return List.of(Component.m_237115_((String)("effect.ftboss.sword.material." + material.id())));
    }

    private static void initialize(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag != null && compoundTag.m_128451_("Version") == 2) {
            return;
        }
        List<SwordCavityRules.Cell> list = SwordCavityBuild.cells(itemStack);
        CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.m_128469_("Cells").m_6426_();
        int n = SwordCavityBuild.revision(itemStack);
        CompoundTag compoundTag3 = itemStack.m_41698_(ROOT);
        compoundTag3.m_128405_("Version", 2);
        compoundTag3.m_128405_("Revision", n);
        if (compoundTag3.m_128461_("NativeItem").isEmpty()) {
            compoundTag3.m_128359_("NativeItem", ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_()).toString());
        }
        compoundTag3.m_128365_("Cells", (Tag)compoundTag2.m_6426_());
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            SwordCavityBuild.writeCell(itemStack, slot, list.get(slot.ordinal()), null);
        }
    }

    private static void writeCell(ItemStack itemStack, SwordCavityRules.Slot slot, SwordCavityRules.Cell cell, CompoundTag compoundTag) {
        CompoundTag compoundTag2 = itemStack.m_41698_(ROOT);
        CompoundTag compoundTag3 = compoundTag2.m_128469_("Cells");
        CompoundTag compoundTag4 = new CompoundTag();
        compoundTag4.m_128359_("State", cell.state().name().toLowerCase(Locale.ROOT));
        compoundTag4.m_128359_("Material", cell.material().id());
        compoundTag4.m_128379_("EverPresent", cell.present() || compoundTag3.m_128469_(slot.name()).m_128471_("EverPresent"));
        if (cell.installed() && compoundTag != null) {
            compoundTag4.m_128365_("Item", (Tag)compoundTag.m_6426_());
        }
        compoundTag3.m_128365_(slot.name(), (Tag)compoundTag4);
        compoundTag2.m_128365_("Cells", (Tag)compoundTag3);
        if (slot.legacyPart != null) {
            itemStack.m_41698_("FTBossWeaponMaterials").m_128359_(slot.legacyPart.name(), cell.installed() ? cell.material().id() : FoodWeaponBuildRules.Material.ORIGINAL.id());
        } else {
            itemStack.m_41698_("FTBossWeaponMaterials");
        }
    }

    private static SwordCavityRules.Cell vacancy(SwordCavityRules.Slot slot) {
        return new SwordCavityRules.Cell(slot.required ? SwordCavityRules.State.MISSING : SwordCavityRules.State.EMPTY, FoodWeaponBuildRules.Material.ORIGINAL);
    }

    private static int returnDestination(ServerPlayer serverPlayer, ItemStack itemStack, ItemStack itemStack2, int n) {
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack3 = serverPlayer.m_150109_().m_8020_(i);
            if (itemStack3 == itemStack || !itemStack3.m_41619_() && (i != n || itemStack3.m_41613_() != 1 || serverPlayer.m_150110_().f_35937_) && (!ItemStack.m_150942_((ItemStack)itemStack3, (ItemStack)itemStack2) || itemStack3.m_41613_() >= itemStack3.m_41741_())) continue;
            return i;
        }
        return -1;
    }

    private static void returnItem(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n);
        if (itemStack2.m_41619_()) {
            serverPlayer.m_150109_().m_6836_(n, itemStack);
        } else {
            itemStack2.m_41769_(1);
        }
    }

    public static boolean canRemove(ItemStack itemStack, SwordCavityRules.Slot slot) {
        if (!SwordCavityBuild.editable(itemStack) || slot == null || SwordCavityBuild.storedItem(itemStack, slot).m_41619_()) {
            return false;
        }
        ItemStack itemStack2 = itemStack.m_41777_();
        SwordCavityBuild.initialize(itemStack2);
        itemStack2.m_41698_(ROOT).m_128379_("GridValues", true);
        itemStack2.m_41698_(ROOT).m_128379_("LinksEnabled", true);
        SwordCavityBuild.writeCell(itemStack2, slot, SwordCavityBuild.vacancy(slot), null);
        return SwordCavityBuild.preservesLife(itemStack, itemStack2);
    }

    public static boolean remove(ServerPlayer serverPlayer, SwordCavityRules.Slot slot, int n) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
            return false;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (SwordCavityBuild.revision(itemStack) != n || !SwordCavityBuild.canRemove(itemStack, slot)) {
            return false;
        }
        ItemStack itemStack2 = SwordCavityBuild.storedItem(itemStack, slot);
        int n2 = SwordCavityBuild.returnDestination(serverPlayer, itemStack, itemStack2, -1);
        if (n2 < 0) {
            serverPlayer.m_213846_((Component)Component.m_237115_((String)"screen.ftboss.sword.inventory_full"));
            return false;
        }
        int n3 = itemStack.m_41776_();
        int n4 = itemStack.m_41773_();
        FoodWeaponLifecycle.ensureTracked(itemStack);
        SwordNativeMaterials.capture(itemStack, (Level)serverPlayer.m_284548_());
        SwordCavityBuild.initialize(itemStack);
        SwordCavityBuild.writeCell(itemStack, slot, SwordCavityBuild.vacancy(slot), null);
        SwordCavityBuild.returnItem(serverPlayer, itemStack2, n2);
        SwordCavityBuild.finishChange(serverPlayer, itemStack, n, n4, n3);
        return true;
    }

    private static ItemStack movedPreview(ItemStack itemStack, SwordCavityRules.Slot slot, SwordCavityRules.Slot slot2) {
        ItemStack itemStack2 = itemStack.m_41777_();
        SwordCavityBuild.initialize(itemStack2);
        itemStack2.m_41698_(ROOT).m_128379_("GridValues", true);
        itemStack2.m_41698_(ROOT).m_128379_("LinksEnabled", true);
        List<SwordCavityRules.Cell> list = SwordCavityBuild.cells(itemStack2);
        CompoundTag compoundTag = itemStack2.m_41737_(ROOT).m_128469_("Cells").m_6426_();
        SwordCavityRules.Cell cell = list.get(slot2.ordinal());
        SwordCavityBuild.writeCell(itemStack2, slot2, list.get(slot.ordinal()), compoundTag.m_128469_(slot.name()).m_128469_("Item"));
        SwordCavityBuild.writeCell(itemStack2, slot, cell.installed() ? cell : SwordCavityBuild.vacancy(slot), cell.installed() ? compoundTag.m_128469_(slot2.name()).m_128469_("Item") : null);
        return itemStack2;
    }

    public static boolean move(ServerPlayer serverPlayer, SwordCavityRules.Slot slot, SwordCavityRules.Slot slot2, int n) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
            return false;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (SwordCavityBuild.revision(itemStack) != n || !SwordCavityBuild.canMove(itemStack, slot, slot2)) {
            return false;
        }
        int n2 = itemStack.m_41776_();
        int n3 = itemStack.m_41773_();
        FoodWeaponLifecycle.ensureTracked(itemStack);
        SwordNativeMaterials.capture(itemStack, (Level)serverPlayer.m_284548_());
        ItemStack itemStack2 = SwordCavityBuild.movedPreview(itemStack, slot, slot2);
        itemStack.m_41784_().m_128365_(ROOT, (Tag)itemStack2.m_41737_(ROOT).m_6426_());
        itemStack.m_41784_().m_128365_("FTBossWeaponMaterials", (Tag)itemStack2.m_41737_("FTBossWeaponMaterials").m_6426_());
        SwordCavityBuild.finishChange(serverPlayer, itemStack, n, n3, n2);
        return true;
    }

    public static boolean losePart(ItemStack itemStack) {
        if (!SwordCavityBuild.active(itemStack) || SwordCavityBuild.revision(itemStack) == Integer.MAX_VALUE || SwordCavityBuild.revision(itemStack) < 0) {
            return false;
        }
        SwordCavityRules.Slot slot = SwordCavityRules.lossSlot(SwordCavityBuild.cells(itemStack));
        if (slot == null) {
            return false;
        }
        SwordCavityBuild.initialize(itemStack);
        SwordCavityBuild.writeCell(itemStack, slot, new SwordCavityRules.Cell(SwordCavityRules.State.MISSING, FoodWeaponBuildRules.Material.ORIGINAL), null);
        itemStack.m_41698_(ROOT).m_128405_("Revision", SwordCavityBuild.revision(itemStack) + 1);
        return true;
    }

    public static Component stateName(ItemStack itemStack, SwordCavityRules.Slot slot) {
        return SwordCavityBuild.stateName(SwordCavityBuild.cells(itemStack).get(slot.ordinal()));
    }

    public static FoodWeaponBuildRules.Material materialFor(ItemStack itemStack) {
        return SwordMaterialExtensions.baseMaterial(itemStack);
    }

    private static FoodWeaponBuildRules.Material nativeMaterial(ItemStack itemStack, SwordCavityRules.Slot slot) {
        if (slot == SwordCavityRules.Slot.GRIP || slot == SwordCavityRules.Slot.BINDING) {
            return FoodWeaponBuildRules.Material.WOOD;
        }
        if (slot.ordinal() >= 4) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        Item item = itemStack.m_41720_();
        if (!(item instanceof TieredItem)) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        TieredItem tieredItem = (TieredItem)item;
        item = tieredItem.m_43314_();
        if (item == Tiers.WOOD) {
            return FoodWeaponBuildRules.Material.WOOD;
        }
        if (item == Tiers.STONE) {
            return FoodWeaponBuildRules.Material.STONE;
        }
        if (item == Tiers.IRON) {
            return FoodWeaponBuildRules.Material.IRON;
        }
        if (item == Tiers.GOLD) {
            return FoodWeaponBuildRules.Material.GOLD;
        }
        if (item == Tiers.DIAMOND) {
            return FoodWeaponBuildRules.Material.DIAMOND;
        }
        if (item == Tiers.NETHERITE) {
            return FoodWeaponBuildRules.Material.NETHERITE;
        }
        for (ItemStack itemStack2 : item.m_6282_().m_43908_()) {
            FoodWeaponBuildRules.Material material = SwordCavityBuild.materialFor(itemStack2);
            if (material == FoodWeaponBuildRules.Material.ORIGINAL) continue;
            return material;
        }
        return FoodWeaponBuildRules.Material.ORIGINAL;
    }

    public static List<SwordCavityRules.Cell> cells(ItemStack itemStack, List<SwordNativeMaterialRules.Piece> list) {
        if (!SwordCavityBuild.applies(itemStack)) {
            return List.of();
        }
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossWeaponMaterials");
        ArrayList<SwordCavityRules.Cell> arrayList = new ArrayList<SwordCavityRules.Cell>();
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            FoodWeaponBuildRules.Material material;
            if (compoundTag != null) {
                Object object;
                SwordCavityRules.State state;
                material = compoundTag.m_128469_("Cells").m_128469_(slot.name());
                int n = compoundTag.m_128451_("Version");
                switch (material.m_128461_("State")) {
                    case "native": {
                        SwordCavityRules.State state2 = SwordCavityRules.State.NATIVE;
                        break;
                    }
                    case "installed": {
                        SwordCavityRules.State state2 = SwordCavityRules.State.INSTALLED;
                        break;
                    }
                    case "missing": {
                        SwordCavityRules.State state2 = SwordCavityRules.State.MISSING;
                        break;
                    }
                    case "empty": 
                    case "": {
                        SwordCavityRules.State state2 = SwordCavityRules.State.EMPTY;
                        break;
                    }
                    default: {
                        SwordCavityRules.State state2 = state = SwordCavityRules.State.INVALID;
                    }
                }
                if (n == 1 && slot.ordinal() >= 4 && state == SwordCavityRules.State.NATIVE) {
                    state = SwordCavityRules.State.EMPTY;
                }
                if (n == 1 && slot.ordinal() >= 4 && state == SwordCavityRules.State.MISSING && !material.m_128471_("EverPresent")) {
                    state = SwordCavityRules.State.EMPTY;
                }
                if (state == SwordCavityRules.State.NATIVE && !SwordNativeMaterialRules.hasPiece(list, slot.ordinal())) {
                    SwordCavityRules.State state3 = state = slot.required ? SwordCavityRules.State.MISSING : SwordCavityRules.State.EMPTY;
                }
                if (n == 2 && state == SwordCavityRules.State.EMPTY && !material.m_128471_("EverPresent") && SwordNativeMaterialRules.hasPiece(list, slot.ordinal()) && itemStack.m_41737_("FTBossSwordNativeMaterials") != null) {
                    state = SwordCavityRules.State.NATIVE;
                }
                if (n == 2 && state == SwordCavityRules.State.INSTALLED && material.m_128441_("Item") && (!material.m_128425_("Item", 10) || SwordCavityBuild.storedItem(itemStack, slot).m_41619_())) {
                    state = SwordCavityRules.State.INVALID;
                }
                Object object2 = object = state == SwordCavityRules.State.NATIVE ? SwordCavityBuild.nativeMaterial(itemStack, slot) : FoodWeaponBuildRules.Material.byId(material.m_128461_("Material"));
                if (n != 1 && n != 2 || state == SwordCavityRules.State.INSTALLED && !SwordCavityRules.accepts(slot, (FoodWeaponBuildRules.Material)((Object)object))) {
                    state = SwordCavityRules.State.INVALID;
                }
                if (state == SwordCavityRules.State.EMPTY || state == SwordCavityRules.State.MISSING || state == SwordCavityRules.State.INVALID) {
                    object = FoodWeaponBuildRules.Material.ORIGINAL;
                }
                arrayList.add(new SwordCavityRules.Cell(state, (FoodWeaponBuildRules.Material)((Object)object)));
                continue;
            }
            FoodWeaponBuildRules.Material material2 = material = compoundTag2 == null || slot.legacyPart == null ? FoodWeaponBuildRules.Material.ORIGINAL : FoodWeaponBuildRules.Material.byId(compoundTag2.m_128461_(slot.legacyPart.name()));
            arrayList.add(material == FoodWeaponBuildRules.Material.ORIGINAL ? (SwordNativeMaterialRules.hasPiece(list, slot.ordinal()) ? new SwordCavityRules.Cell(SwordCavityRules.State.NATIVE, SwordCavityBuild.nativeMaterial(itemStack, slot)) : SwordCavityRules.nativeCell(slot, SwordCavityBuild.nativeMaterial(itemStack, slot))) : new SwordCavityRules.Cell(SwordCavityRules.State.INSTALLED, material));
        }
        return List.copyOf(arrayList);
    }

    public static boolean cellUnlocked(ItemStack itemStack, int n) {
        return SwordCavityBuild.applies(itemStack) && SwordFunctionRules.unlocked(SwordCavityExtensions.functionProfile(itemStack), n, FoodWeaponEvolution.evolutionLevel(itemStack));
    }

    public static SwordFunctionRules.Status functionStatus(ItemStack itemStack) {
        if (!SwordCompatibilityRules.upgradeReferenceBudget(FourWeaponFoundationAdapter.registryId(itemStack), List.of()).isEmpty()) {
            return SwordCavityBuild.moddedFunctionStatus(itemStack);
        }
        List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterials.parts(itemStack);
        List<SwordCavityRules.Cell> list2 = SwordCavityBuild.cells(itemStack, list);
        ArrayList<SwordFunctionRules.Input> arrayList = new ArrayList<SwordFunctionRules.Input>();
        HashMap<String, SwordFunctionRules.Values> hashMap = new HashMap<String, SwordFunctionRules.Values>();
        SwordFunctionRules.Profile profile = SwordCavityExtensions.functionProfile(itemStack);
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (list2.size() != 27) break;
            SwordCavityRules.Cell cell = list2.get(slot.ordinal());
            ItemStack itemStack2 = cell.state() == SwordCavityRules.State.NATIVE && list.get(slot.ordinal()).count() == 1 ? SwordNativeMaterials.display(itemStack, slot.ordinal(), list) : SwordCavityBuild.storedItem(itemStack, slot);
            Optional<SwordMaterialDefinition> optional = SwordMaterialExtensions.resolve(itemStack2);
            String string = optional.map(swordMaterialDefinition -> swordMaterialDefinition.itemId().toString()).orElse((String)(cell.installed() ? "ftboss:legacy_" + cell.material().id() : ""));
            optional.ifPresent(swordMaterialDefinition -> {
                if (SwordMaterialExtensions.registered(swordMaterialDefinition.itemId()) || swordMaterialDefinition.categories().contains((Object)SwordMaterialDefinition.Category.FOOD)) {
                    hashMap.put(string, swordMaterialDefinition.functions());
                }
            });
            arrayList.add(new SwordFunctionRules.Input(string, optional.map(swordMaterialDefinition -> swordMaterialDefinition.baseMaterial()).orElse(cell.material()), cell.present() && (cell.state() != SwordCavityRules.State.NATIVE || !itemStack2.m_41619_())));
        }
        if (profile != null) {
            profile = profile.withItemValues(hashMap);
        }
        SwordFunctionRules.Status status = SwordFunctionRules.evaluateItems(profile, arrayList, FoodWeaponEvolution.evolutionLevel(itemStack));
        return SwordCavityRules.usable(list2) ? status : new SwordFunctionRules.Status(false, status.total(), status.minimum(), List.of(SwordFunctionRules.Function.values()), status.unlockedCells());
    }

    public static boolean missingRequired(ItemStack itemStack) {
        if (!SwordCavityBuild.applies(itemStack)) {
            return false;
        }
        if (SwordCavityExtensions.functionProfile(itemStack) != null) {
            return !SwordCavityBuild.functionStatus(itemStack).ready();
        }
        List<SwordCavityRules.Cell> list = SwordCavityBuild.cells(itemStack);
        if (!SwordCavityRules.usable(list)) {
            return true;
        }
        for (int i = 0; i < 4; ++i) {
            if (list.get(i).present()) continue;
            return true;
        }
        return false;
    }

    private static List<SwordCavityRules.Cell> effectiveCells(ItemStack itemStack) {
        ArrayList<SwordCavityRules.Cell> arrayList = new ArrayList<SwordCavityRules.Cell>(SwordCavityBuild.cells(itemStack));
        boolean bl = SwordCavityBuild.applies(itemStack);
        SwordFunctionRules.Profile profile = bl ? SwordCavityExtensions.functionProfile(itemStack) : null;
        int n = bl ? FoodWeaponEvolution.evolutionLevel(itemStack) : 0;
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (bl && SwordFunctionRules.unlocked(profile, slot.ordinal(), n)) continue;
            arrayList.set(slot.ordinal(), new SwordCavityRules.Cell(SwordCavityRules.State.EMPTY, FoodWeaponBuildRules.Material.ORIGINAL));
        }
        return arrayList;
    }

    public static List<FoodWeaponBuildRules.Material> legacyMaterials(ItemStack itemStack) {
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>(List.of(FoodWeaponBuildRules.Material.ORIGINAL, FoodWeaponBuildRules.Material.ORIGINAL, FoodWeaponBuildRules.Material.ORIGINAL));
        List<SwordCavityRules.Cell> list = SwordCavityBuild.effectiveCells(itemStack);
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (slot.legacyPart == null) continue;
            arrayList.set(slot.legacyPart.ordinal(), SwordCavityRules.installed(list, slot));
        }
        return List.copyOf(arrayList);
    }

    public static List<FoodWeaponBuildRules.Material> powerMaterials(ItemStack itemStack) {
        List<SwordCavityRules.Cell> list = SwordCavityBuild.effectiveCells(itemStack);
        if (!SwordCavityRules.usable(list) || SwordCavityBuild.missingRequired(itemStack)) {
            return List.of();
        }
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>();
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            FoodWeaponBuildRules.Material material = SwordCavityRules.installed(list, slot);
            if (material == FoodWeaponBuildRules.Material.ORIGINAL || arrayList.contains((Object)material)) continue;
            arrayList.add(material);
        }
        return List.copyOf(arrayList);
    }

    private static FoodWeaponBuildRules.Build legacyBuild(ItemStack itemStack) {
        List<SwordCavityRules.Cell> list = SwordCavityBuild.effectiveCells(itemStack);
        FoodWeaponBuildRules.Build build = FoodWeaponBuildRules.build(SwordCavityBuild.legacyMaterialValues(list));
        if (!SwordCavityRules.usable(list)) {
            return new FoodWeaponBuildRules.Build(build.durability(), build.wear(), 0.0, 0.0, 0.0);
        }
        double d = build.durability();
        double d2 = build.wear();
        double d3 = build.damage();
        double d4 = build.speed();
        double d5 = build.reach();
        double[][] dArrayArray = new double[][]{{0.0, 0.0, 0.0, 0.0}, {0.25, 0.1, 0.0, 0.15}, {0.0, 0.0, 0.0, 0.0}, {0.1, 0.0, 0.12, 0.0}, {0.0, 0.0, 0.0, 0.0}, {0.12, 0.0, 0.04, 0.0}, {0.08, 0.0, 0.12, 0.0}, {0.0, 0.08, 0.04, 0.0}};
        for (int i = 0; i < dArrayArray.length; ++i) {
            SwordCavityRules.Cell cell = list.get(i);
            if (!cell.installed() || SwordCavityRules.Slot.byIndex((int)i).legacyPart != null) continue;
            FoodWeaponBuildRules.Material material = cell.material();
            d *= 1.0 + (material.durability - 1.0) * dArrayArray[i][0];
            d2 *= 1.0 + (material.wear - 1.0) * dArrayArray[i][0];
            d3 *= 1.0 + (material.damage - 1.0) * dArrayArray[i][1];
            d4 *= 1.0 + (material.speed - 1.0) * dArrayArray[i][2];
            d5 += material.reach * dArrayArray[i][3];
        }
        List<SwordCavityRules.Link> list2 = SwordCavityBuild.links(itemStack);
        if (list2.contains((Object)SwordCavityRules.Link.FROST_EDGE)) {
            d *= 1.08;
            d2 *= 0.94;
        }
        if (list2.contains((Object)SwordCavityRules.Link.COPPER_CIRCUIT)) {
            d4 *= 1.04;
            d5 += 0.1;
        }
        return new FoodWeaponBuildRules.Build(d, Math.max(0.1, d2), d3, d4, Math.min(0.75, d5));
    }

    public static List<SwordCavityRules.Link> links(ItemStack itemStack) {
        if (!SwordCavityBuild.linksEnabled(itemStack) || SwordCavityBuild.missingRequired(itemStack)) {
            return List.of();
        }
        List<SwordCavityRules.Cell> list = SwordCavityBuild.effectiveCells(itemStack);
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag != null && compoundTag.m_128471_("GridValues")) {
            return SwordCavityRules.links(list);
        }
        if (!SwordCavityRules.usable(list)) {
            return List.of();
        }
        FoodWeaponBuildRules.Material material = SwordCavityRules.installed(list, SwordCavityRules.Slot.CORE);
        FoodWeaponBuildRules.Material material2 = SwordCavityRules.installed(list, SwordCavityRules.Slot.BLADE);
        if (material == FoodWeaponBuildRules.Material.BOSS_FROST && (material2 == FoodWeaponBuildRules.Material.BOSS_FROST || material2 == FoodWeaponBuildRules.Material.DIAMOND)) {
            return List.of(SwordCavityRules.Link.FROST_EDGE);
        }
        if (material == FoodWeaponBuildRules.Material.BOSS_CAPACITOR && (material2 == FoodWeaponBuildRules.Material.COPPER || SwordCavityRules.installed(list, SwordCavityRules.Slot.INLAY) == FoodWeaponBuildRules.Material.COPPER)) {
            return List.of(SwordCavityRules.Link.COPPER_CIRCUIT);
        }
        return List.of();
    }

    public static ItemStack storedItem(ItemStack itemStack, SwordCavityRules.Slot slot) {
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag == null || compoundTag.m_128451_("Version") != 2) {
            return ItemStack.f_41583_;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("Cells").m_128469_(slot.name());
        if (!compoundTag2.m_128461_("State").equals("installed") || !compoundTag2.m_128425_("Item", 10)) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack2 = ItemStack.m_41712_((CompoundTag)compoundTag2.m_128469_("Item"));
        if (itemStack2.m_41613_() != 1 || itemStack2.m_41619_() || SwordCavityBuild.materialFor(itemStack2) != FoodWeaponBuildRules.Material.byId(compoundTag2.m_128461_("Material"))) {
            return ItemStack.f_41583_;
        }
        return itemStack2;
    }

    public static ItemStack preview(ItemStack itemStack, SwordCavityRules.Slot slot, FoodWeaponBuildRules.Material material) {
        ItemStack itemStack2 = itemStack.m_41777_();
        if (!(SwordCavityBuild.applies(itemStack2) && SwordCavityRules.accepts(slot, material) && SwordCavityBuild.cellUnlocked(itemStack2, slot.ordinal()))) {
            return itemStack2;
        }
        SwordCavityBuild.initialize(itemStack2);
        itemStack2.m_41698_(ROOT).m_128379_("LinksEnabled", true);
        itemStack2.m_41698_(ROOT).m_128379_("GridValues", true);
        SwordCavityBuild.writeCell(itemStack2, slot, new SwordCavityRules.Cell(SwordCavityRules.State.INSTALLED, material), null);
        return itemStack2;
    }

    private static boolean editable(ItemStack itemStack) {
        if (!SwordCavityBuild.applies(itemStack) || SwordCavityBuild.revision(itemStack) < 0 || SwordCavityBuild.revision(itemStack) == Integer.MAX_VALUE || itemStack.m_41763_() && itemStack.m_41776_() > 0 && itemStack.m_41773_() >= itemStack.m_41776_()) {
            return false;
        }
        if (!SwordCavityRules.usable(SwordCavityBuild.cells(itemStack))) {
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag != null && compoundTag.m_128451_("Version") != 1 && compoundTag.m_128451_("Version") != 2) {
            return false;
        }
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossWeaponLifecycle");
        return compoundTag2 == null || !compoundTag2.m_128471_("Broken");
    }

    public static boolean containerEditable(ItemStack itemStack) {
        return SwordCavityBuild.editable(itemStack);
    }

    public static List<ItemStack> containerItems(ItemStack itemStack) {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            arrayList.add(SwordCavityBuild.storedItem(itemStack, slot));
        }
        return arrayList;
    }

    private static boolean samePiece(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.m_41619_() && itemStack2.m_41619_() || itemStack.m_41613_() == itemStack2.m_41613_() && ItemStack.m_150942_((ItemStack)itemStack, (ItemStack)itemStack2);
    }

    private static ItemStack containerPreview(ItemStack itemStack, List<ItemStack> list) {
        if (!SwordCavityBuild.editable(itemStack) || list == null || list.size() != 27) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack2 = itemStack.m_41777_();
        SwordCavityBuild.initialize(itemStack2);
        itemStack2.m_41698_(ROOT).m_128379_("GridValues", true);
        itemStack2.m_41698_(ROOT).m_128379_("LinksEnabled", true);
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            ItemStack itemStack3 = list.get(slot.ordinal());
            if (itemStack3 == null || !itemStack3.m_41619_() && (itemStack3.m_41613_() != 1 || !SwordCavityRules.accepts(slot, SwordCavityBuild.materialFor(itemStack3)))) {
                return ItemStack.f_41583_;
            }
            ItemStack itemStack4 = SwordCavityBuild.storedItem(itemStack, slot);
            if (SwordCavityBuild.samePiece(itemStack4, itemStack3)) continue;
            if (!itemStack3.m_41619_() && !SwordCavityBuild.cellUnlocked(itemStack, slot.ordinal())) {
                return ItemStack.f_41583_;
            }
            SwordCavityBuild.writeCell(itemStack2, slot, itemStack3.m_41619_() ? SwordCavityBuild.vacancy(slot) : new SwordCavityRules.Cell(SwordCavityRules.State.INSTALLED, SwordCavityBuild.materialFor(itemStack3)), itemStack3.m_41619_() ? null : itemStack3.m_41739_(new CompoundTag()));
        }
        return itemStack2;
    }

    public static boolean canSetContainerItems(ItemStack itemStack, List<ItemStack> list) {
        ItemStack itemStack2 = SwordCavityBuild.containerPreview(itemStack, list);
        return !itemStack2.m_41619_() && SwordCavityBuild.preservesLife(itemStack, itemStack2);
    }

    public static boolean setContainerItems(ServerPlayer serverPlayer, ItemStack itemStack, List<ItemStack> list, int n) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || SwordCavityBuild.revision(itemStack) != n) {
            return false;
        }
        ItemStack itemStack2 = SwordCavityBuild.containerPreview(itemStack, list);
        if (itemStack2.m_41619_() || !SwordCavityBuild.preservesLife(itemStack, itemStack2)) {
            return false;
        }
        boolean bl = false;
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (SwordCavityBuild.samePiece(SwordCavityBuild.storedItem(itemStack, slot), list.get(slot.ordinal()))) continue;
            bl = true;
        }
        if (!bl) {
            return true;
        }
        return SwordCavityBuild.applyContainerChange(serverPlayer, itemStack, itemStack2, n);
    }

    private static boolean applyContainerChange(ServerPlayer serverPlayer, ItemStack itemStack, ItemStack itemStack2, int n) {
        int n2 = itemStack.m_41773_();
        int n3 = itemStack.m_41776_();
        CompoundTag compoundTag = itemStack.m_41783_() == null ? null : itemStack.m_41783_().m_6426_();
        try {
            FoodWeaponLifecycle.ensureTracked(itemStack);
            SwordNativeMaterials.capture(itemStack, (Level)serverPlayer.m_284548_());
            itemStack.m_41784_().m_128365_(ROOT, (Tag)itemStack2.m_41737_(ROOT).m_6426_());
            itemStack.m_41784_().m_128365_("FTBossWeaponMaterials", (Tag)itemStack2.m_41737_("FTBossWeaponMaterials").m_6426_());
            SwordCavityBuild.finishChange(serverPlayer, itemStack, n, n2, n3);
            return true;
        }
        catch (RuntimeException runtimeException) {
            itemStack.m_41751_(compoundTag);
            throw runtimeException;
        }
    }

    public static boolean canClearReference(ItemStack itemStack, SwordCavityRules.Slot slot) {
        if (!(SwordCavityBuild.editable(itemStack) && slot != null && SwordCavityBuild.storedItem(itemStack, slot).m_41619_() && SwordCavityBuild.cells(itemStack).get(slot.ordinal()).present())) {
            return false;
        }
        ItemStack itemStack2 = itemStack.m_41777_();
        SwordCavityBuild.initialize(itemStack2);
        itemStack2.m_41698_(ROOT).m_128379_("GridValues", true);
        itemStack2.m_41698_(ROOT).m_128379_("LinksEnabled", true);
        SwordCavityBuild.writeCell(itemStack2, slot, SwordCavityBuild.vacancy(slot), null);
        return SwordCavityBuild.preservesLife(itemStack, itemStack2);
    }

    public static boolean clearReference(ServerPlayer serverPlayer, ItemStack itemStack, SwordCavityRules.Slot slot, int n) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || SwordCavityBuild.revision(itemStack) != n || !SwordCavityBuild.canClearReference(itemStack, slot)) {
            return false;
        }
        ItemStack itemStack2 = itemStack.m_41777_();
        SwordCavityBuild.initialize(itemStack2);
        SwordCavityBuild.writeCell(itemStack2, slot, SwordCavityBuild.vacancy(slot), null);
        return SwordCavityBuild.applyContainerChange(serverPlayer, itemStack, itemStack2, n);
    }

    public static boolean canReplace(ItemStack itemStack, SwordCavityRules.Slot slot, FoodWeaponBuildRules.Material material) {
        if (!(SwordCavityBuild.editable(itemStack) && SwordCavityRules.accepts(slot, material) && SwordCavityBuild.cellUnlocked(itemStack, slot.ordinal()))) {
            return false;
        }
        SwordCavityRules.Cell cell = SwordCavityBuild.cells(itemStack).get(slot.ordinal());
        return (!cell.installed() || cell.material() != material) && SwordCavityBuild.preservesLife(itemStack, SwordCavityBuild.preview(itemStack, slot, material));
    }

    public static String replacementFailure(ItemStack itemStack, SwordCavityRules.Slot slot, FoodWeaponBuildRules.Material material) {
        if (!SwordCavityBuild.applies(itemStack) || slot == null || !SwordCavityRules.accepts(slot, material)) {
            return "screen.ftboss.sword.invalid_material";
        }
        if (!SwordCavityBuild.cellUnlocked(itemStack, slot.ordinal())) {
            return "screen.ftboss.cavity.cell_locked";
        }
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        if (compoundTag != null && compoundTag.m_128451_("Version") != 1 && compoundTag.m_128451_("Version") != 2 || SwordCavityBuild.revision(itemStack) < 0 || SwordCavityBuild.revision(itemStack) == Integer.MAX_VALUE) {
            return "screen.ftboss.sword.version_locked";
        }
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossWeaponLifecycle");
        if (itemStack.m_41763_() && itemStack.m_41776_() > 0 && itemStack.m_41773_() >= itemStack.m_41776_() || compoundTag2 != null && compoundTag2.m_128471_("Broken")) {
            return "screen.ftboss.sword.depleted";
        }
        SwordCavityRules.Cell cell = SwordCavityBuild.cells(itemStack).get(slot.ordinal());
        if (cell.installed() && cell.material() == material) {
            return "screen.ftboss.sword.same_material";
        }
        return SwordCavityBuild.canReplace(itemStack, slot, material) ? "" : "message.ftboss.material.repair_first";
    }

    public static boolean replace(ServerPlayer serverPlayer, SwordCavityRules.Slot slot, int n, int n2) {
        int n3;
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || n < 0 || n >= 36) {
            return false;
        }
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n);
        FoodWeaponBuildRules.Material material = SwordCavityBuild.materialFor(itemStack2);
        if (itemStack2 == itemStack || itemStack2.m_41619_() || SwordCavityBuild.revision(itemStack) != n2 || !SwordCavityBuild.canReplace(itemStack, slot, material)) {
            return false;
        }
        ItemStack itemStack3 = SwordCavityBuild.storedItem(itemStack, slot);
        int n4 = n3 = itemStack3.m_41619_() ? -1 : SwordCavityBuild.returnDestination(serverPlayer, itemStack, itemStack3, n);
        if (!itemStack3.m_41619_() && n3 < 0) {
            serverPlayer.m_213846_((Component)Component.m_237115_((String)"screen.ftboss.sword.inventory_full"));
            return false;
        }
        int n5 = itemStack.m_41776_();
        int n6 = itemStack.m_41773_();
        ItemStack itemStack4 = itemStack2.m_41777_();
        itemStack4.m_41764_(1);
        FoodWeaponLifecycle.ensureTracked(itemStack);
        SwordNativeMaterials.capture(itemStack, (Level)serverPlayer.m_284548_());
        SwordCavityBuild.initialize(itemStack);
        SwordCavityBuild.writeCell(itemStack, slot, new SwordCavityRules.Cell(SwordCavityRules.State.INSTALLED, material), serverPlayer.m_150110_().f_35937_ ? null : itemStack4.m_41739_(new CompoundTag()));
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack2.m_41774_(1);
        }
        if (!itemStack3.m_41619_()) {
            SwordCavityBuild.returnItem(serverPlayer, itemStack3, n3);
        }
        SwordCavityBuild.finishChange(serverPlayer, itemStack, n2, n6, n5);
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.sword.inserted", (Object[])new Object[]{WeaponMaterialBuild.materialName(material)}).m_130940_(ChatFormatting.GOLD));
        return true;
    }

    public static boolean canMove(ItemStack itemStack, SwordCavityRules.Slot slot, SwordCavityRules.Slot slot2) {
        return SwordCavityBuild.editable(itemStack) && slot != null && slot2 != null && slot != slot2 && SwordCavityBuild.cellUnlocked(itemStack, slot.ordinal()) && SwordCavityBuild.cellUnlocked(itemStack, slot2.ordinal()) && SwordCavityBuild.cells(itemStack).get(slot.ordinal()).installed() && SwordCavityBuild.preservesLife(itemStack, SwordCavityBuild.movedPreview(itemStack, slot, slot2));
    }

    private static boolean preservesLife(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.m_41619_()) {
            return false;
        }
        if (!itemStack2.m_41763_()) {
            return true;
        }
        int n = itemStack2.m_41776_();
        return n > 0 && itemStack.m_41773_() < n;
    }

    private static void finishChange(ServerPlayer serverPlayer, ItemStack itemStack, int n, int n2, int n3) {
        itemStack.m_41698_(ROOT).m_128379_("LinksEnabled", true);
        itemStack.m_41698_(ROOT).m_128379_("GridValues", true);
        itemStack.m_41698_(ROOT).m_128405_("Revision", n + 1);
        NativeFoodWeaponWear.manage(serverPlayer, itemStack);
        FoodWeaponLifecycle.materialDamage(itemStack, n2);
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36096_.m_38946_();
    }

    static ItemStack nativeWearCandidate(ItemStack itemStack, SwordCavityRules.Slot slot) {
        if (slot == null || slot == SwordCavityRules.Slot.CORE || slot.required || !SwordCavityBuild.active(itemStack) || SwordCavityBuild.missingRequired(itemStack) || SwordCavityBuild.revision(itemStack) < 0 || SwordCavityBuild.revision(itemStack) == Integer.MAX_VALUE) {
            return null;
        }
        List<SwordCavityRules.Cell> list = SwordCavityBuild.cells(itemStack);
        if (list.size() != 27 || list.get(slot.ordinal()).state() != SwordCavityRules.State.INSTALLED) {
            return null;
        }
        ItemStack itemStack2 = itemStack.m_41777_();
        SwordCavityBuild.initialize(itemStack2);
        SwordCavityBuild.writeCell(itemStack2, slot, new SwordCavityRules.Cell(SwordCavityRules.State.EMPTY, FoodWeaponBuildRules.Material.ORIGINAL), null);
        itemStack2.m_41698_(ROOT).m_128405_("Revision", SwordCavityBuild.revision(itemStack) + 1);
        if (!SwordCavityRules.usable(SwordCavityBuild.cells(itemStack2)) || SwordCavityBuild.missingRequired(itemStack2) || !SwordCavityBuild.functionStatus(itemStack2).ready()) {
            return null;
        }
        return itemStack2;
    }

    private static List<FoodWeaponBuildRules.Material> legacyMaterialValues(List<SwordCavityRules.Cell> list) {
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>(List.of(FoodWeaponBuildRules.Material.ORIGINAL, FoodWeaponBuildRules.Material.ORIGINAL, FoodWeaponBuildRules.Material.ORIGINAL));
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (slot.legacyPart == null) continue;
            arrayList.set(slot.legacyPart.ordinal(), SwordCavityRules.installed(list, slot));
        }
        return arrayList;
    }

    private static SwordFunctionRules.Status moddedFunctionStatus(ItemStack itemStack) {
        List<SwordNativeMaterialRules.Piece> list = SwordNativeMaterials.parts(itemStack);
        List<SwordCavityRules.Cell> list2 = SwordCavityBuild.cells(itemStack, list);
        ArrayList<SwordFunctionRules.Input> arrayList = new ArrayList<SwordFunctionRules.Input>();
        HashMap<String, SwordFunctionRules.Values> hashMap = new HashMap<String, SwordFunctionRules.Values>();
        SwordFunctionRules.Profile profile = SwordCavityExtensions.functionProfile(itemStack);
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        boolean bl = resourceLocation != null && !SwordCompatibilityRules.upgradeReferenceBudget(resourceLocation.toString(), List.of()).isEmpty();
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (list2.size() != 27) break;
            SwordCavityRules.Cell cell = list2.get(slot.ordinal());
            ItemStack itemStack2 = cell.state() == SwordCavityRules.State.NATIVE && list.get(slot.ordinal()).count() == 1 ? SwordNativeMaterials.display(itemStack, slot.ordinal(), list) : SwordCavityBuild.storedItem(itemStack, slot);
            Optional<SwordMaterialDefinition> optional = SwordMaterialExtensions.resolve(itemStack2);
            ResourceLocation resourceLocation2 = itemStack2.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)itemStack2.m_41720_());
            String string = optional.map(swordMaterialDefinition -> swordMaterialDefinition.itemId().toString()).orElse((String)(resourceLocation2 != null ? resourceLocation2.toString() : (cell.installed() ? "ftboss:legacy_" + cell.material().id() : "")));
            Object object = string;
            if (bl && cell.state() == SwordCavityRules.State.NATIVE && profile != null) {
                object = "ftboss:native_reference/" + slot.ordinal();
                SwordFunctionRules.Values values = profile.itemValues().get(string);
                if (values == null) {
                    values = SwordCompatibilityRules.materialFunctions(SwordCompatibilityRules.material(string));
                }
                hashMap.put((String)object, values);
            } else {
                optional.ifPresent(swordMaterialDefinition -> {
                    if (SwordMaterialExtensions.registered(swordMaterialDefinition.itemId()) || swordMaterialDefinition.categories().contains((Object)SwordMaterialDefinition.Category.FOOD)) {
                        hashMap.put(string, swordMaterialDefinition.functions());
                    }
                });
            }
            arrayList.add(new SwordFunctionRules.Input((String)object, optional.map(swordMaterialDefinition -> swordMaterialDefinition.baseMaterial()).orElse(cell.material()), cell.present() && (cell.state() != SwordCavityRules.State.NATIVE || !itemStack2.m_41619_())));
        }
        if (profile != null) {
            profile = profile.withItemValues(hashMap);
        }
        SwordFunctionRules.Status status = SwordFunctionRules.evaluateItems(profile, arrayList, FoodWeaponEvolution.evolutionLevel(itemStack));
        return SwordCavityRules.usable(list2) ? status : new SwordFunctionRules.Status(false, status.total(), status.minimum(), List.of(SwordFunctionRules.Function.values()), status.unlockedCells());
    }

    public static FoodWeaponBuildRules.Build build(ItemStack itemStack) {
        FoodWeaponBuildRules.Build build;
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        FoodWeaponBuildRules.Build build2 = build = compoundTag != null && compoundTag.m_128471_("GridValues") ? SwordCavityRules.gridBuild(SwordCavityBuild.effectiveCells(itemStack), SwordCavityBuild.linksEnabled(itemStack)) : SwordCavityBuild.legacyBuild(itemStack);
        if (SwordCavityBuild.missingRequired(itemStack)) {
            return new FoodWeaponBuildRules.Build(build.durability(), build.wear(), 0.0, 0.0, 0.0);
        }
        return WeaponDurabilityBuild.combat(itemStack, SwordCavityExtensions.applyBuild(itemStack, build));
    }
}

