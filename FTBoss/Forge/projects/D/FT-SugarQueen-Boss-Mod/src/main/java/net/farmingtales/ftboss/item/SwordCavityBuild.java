package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.ForgeRegistries;

/** 剑腔材料实物与迁移；2026-10-13 分离可编辑与可使用判定，铁剑按五项功能值和进化格子计算，不退还原生参考。 */
public final class SwordCavityBuild {
    public static final String ROOT = "FTBossSwordStructure";

    private SwordCavityBuild() {}

    public static boolean applies(ItemStack stack) {
        if (WeaponAttachmentPolicy.excluded(stack)) return false;
        if (ManorWeaponCavity.applies(stack)) return true;
        if (AdditionalWeaponUiEligibility.keepLegacyUi(stack)) return false;
        return SwordCavityExtensions.supports(stack);
    }

    public static Material materialFor(ItemStack stack) { return SwordMaterialExtensions.baseMaterial(stack); }

    public static boolean active(ItemStack stack) {
        return applies(stack) && (stack.getTagElement(ROOT) != null || stack.getTagElement(WeaponMaterialBuild.ROOT) != null
                || FoodWeaponEvolution.evolutionLevel(stack) > 0);
    }

    public static int revision(ItemStack stack) {
        CompoundTag data = stack.getTagElement(ROOT);
        return data == null ? 0 : data.getInt("Revision");
    }

    private static Material nativeMaterial(ItemStack stack, Slot slot) {
        if (slot == Slot.GRIP || slot == Slot.BINDING) return Material.WOOD;
        if (slot.ordinal() >= 4) return Material.ORIGINAL;
        if (!(stack.getItem() instanceof TieredItem tiered)) return Material.ORIGINAL;
        var tier = tiered.getTier();
        if (tier == Tiers.WOOD) return Material.WOOD;
        if (tier == Tiers.STONE) return Material.STONE;
        if (tier == Tiers.IRON) return Material.IRON;
        if (tier == Tiers.GOLD) return Material.GOLD;
        if (tier == Tiers.DIAMOND) return Material.DIAMOND;
        if (tier == Tiers.NETHERITE) return Material.NETHERITE;
        for (ItemStack ingredient : tier.getRepairIngredient().getItems()) {
            Material material = materialFor(ingredient);
            if (material != Material.ORIGINAL) return material;
        }
        return Material.ORIGINAL;
    }

    public static List<Cell> cells(ItemStack stack) {
        List<Cell> cached = WeaponTooltipReadScope.cachedCells(stack);
        if (cached != null) return cached;
        return WeaponTooltipReadScope.rememberCells(stack, cells(stack, SwordNativeMaterials.parts(stack)));
    }

    public static List<Cell> cells(ItemStack stack, List<Piece> nativeParts) {
        if (!applies(stack)) return List.of();
        CompoundTag structure = stack.getTagElement(ROOT);
        CompoundTag legacy = stack.getTagElement(WeaponMaterialBuild.ROOT);
        List<Cell> result = new ArrayList<>();
        for (Slot slot : Slot.values()) {
            if (structure != null) {
                CompoundTag stored = structure.getCompound("Cells").getCompound(slot.name());
                int version = structure.getInt("Version");
                State state = switch (stored.getString("State")) {
                    case "native" -> State.NATIVE;
                    case "installed" -> State.INSTALLED;
                    case "missing" -> State.MISSING;
                    case "empty", "" -> State.EMPTY;
                    default -> State.INVALID;
                };
                if (version == 1 && slot.ordinal() >= 4 && state == State.NATIVE) state = State.EMPTY;
                if (version == 1 && slot.ordinal() >= 4 && state == State.MISSING && !stored.getBoolean("EverPresent")) state = State.EMPTY;
                if (state == State.NATIVE && !SwordNativeMaterialRules.hasPiece(nativeParts, slot.ordinal())) {
                    state = slot.required ? State.MISSING : State.EMPTY;
                }
                if (version == SwordCavityRules.VERSION && state == State.EMPTY && !stored.getBoolean("EverPresent")
                        && SwordNativeMaterialRules.hasPiece(nativeParts, slot.ordinal())
                        && stack.getTagElement(SwordNativeMaterials.ROOT) != null) state = State.NATIVE;
                if (version == SwordCavityRules.VERSION && state == State.INSTALLED
                        && stored.contains("Item") && (!stored.contains("Item", Tag.TAG_COMPOUND) || storedItem(stack, slot).isEmpty())) state = State.INVALID;
                Material material = state == State.NATIVE ? nativeMaterial(stack, slot) : Material.byId(stored.getString("Material"));
                if (version != 1 && version != SwordCavityRules.VERSION
                        || state == State.INSTALLED && !SwordCavityRules.accepts(slot, material)) state = State.INVALID;
                if (state == State.EMPTY || state == State.MISSING || state == State.INVALID) material = Material.ORIGINAL;
                result.add(new Cell(state, material));
            } else {
                Material material = legacy == null || slot.legacyPart == null ? Material.ORIGINAL
                        : Material.byId(legacy.getString(slot.legacyPart.name()));
                result.add(material == Material.ORIGINAL ? SwordNativeMaterialRules.hasPiece(nativeParts, slot.ordinal())
                        ? new Cell(State.NATIVE, nativeMaterial(stack, slot)) : SwordCavityRules.nativeCell(slot, nativeMaterial(stack, slot))
                        : new Cell(State.INSTALLED, material));
            }
        }
        return GourmetBladeCavity.cells(stack, List.copyOf(result));
    }

    public static boolean cellUnlocked(ItemStack stack, int index) {
        if (GourmetBladeCavity.applies(stack)) return GourmetBladeCavity.unlocked(stack, index);
        if (EquipmentWeaponBuild.applies(stack) && applies(stack)) return EquipmentWeaponBuild.cellUnlocked(stack, index);
        return applies(stack) && SwordFunctionRules.unlocked(SwordCavityExtensions.functionProfile(stack), index,
                FoodWeaponEvolution.evolutionLevel(stack));
    }

    public static SwordFunctionRules.Status functionStatus(ItemStack stack) {
        if (GourmetBladeCavity.applies(stack)) return GourmetBladeCavity.functionStatus(stack);
        List<Piece> parts = SwordNativeMaterials.parts(stack);
        List<Cell> source = cells(stack, parts);
        List<SwordFunctionRules.Input> functional = new ArrayList<>();
        java.util.Map<String, SwordFunctionRules.Values> overrides = new java.util.HashMap<>();
        SwordFunctionRules.Profile profile = SwordCavityExtensions.functionProfile(stack);
        var weaponId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        boolean referenceProfile = EquipmentWeaponBuild.applies(stack) || weaponId != null
                && !SwordCompatibilityRules.upgradeReferenceBudget(weaponId.toString(), List.of()).isEmpty();
        for (Slot slot : Slot.values()) {
            if (source.size() != SwordCavityRules.CELL_COUNT) break;
            Cell cell = source.get(slot.ordinal());
            ItemStack item = cell.state() == State.NATIVE && parts.get(slot.ordinal()).count() == 1
                    ? SwordNativeMaterials.display(stack, slot.ordinal(), parts) : storedItem(stack, slot);
            var definition = SwordMaterialExtensions.resolve(item);
            var itemId = item.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(item.getItem());
            String identifier = definition.map(value -> value.itemId().toString()).orElse(itemId != null ? itemId.toString()
                    : cell.installed() ? "ftboss:legacy_" + cell.material().id() : "");
            String functionId = identifier;
            if (referenceProfile && cell.state() == State.NATIVE && profile != null) {
                functionId = "ftboss:native_reference/" + slot.ordinal();
                SwordFunctionRules.Values functions = profile.itemValues().get(identifier);
                if (functions == null) functions = SwordCompatibilityRules.materialFunctions(SwordCompatibilityRules.material(identifier));
                overrides.put(functionId, functions);
            } else definition.ifPresent(value -> {
                if (SwordMaterialExtensions.registered(value.itemId()) || value.categories().contains(
                        net.farmingtales.ftboss.api.SwordMaterialDefinition.Category.FOOD)) overrides.put(identifier, value.functions());
            });
            functional.add(new SwordFunctionRules.Input(functionId, definition.map(value -> value.baseMaterial()).orElse(cell.material()),
                    cell.present() && (cell.state() != State.NATIVE || !item.isEmpty())));
        }
        if (profile != null) profile = profile.withItemValues(overrides);
        SwordFunctionRules.Status status = SwordFunctionRules.evaluateItems(profile, functional, FoodWeaponEvolution.evolutionLevel(stack));
        return SwordCavityRules.usable(source) ? status : new SwordFunctionRules.Status(false, status.total(), status.minimum(),
                List.of(SwordFunctionRules.Function.values()), status.unlockedCells());
    }

    public static boolean missingRequired(ItemStack stack) {
        if (GourmetBladeCavity.applies(stack)) return !GourmetBladeCavity.functionStatus(stack).ready();
        if (FourWeaponFoundationAdapter.applies(stack)) return FourWeaponFoundationAdapter.blocksUse(stack);
        if (!applies(stack)) return false;
        if (SwordCavityExtensions.functionProfile(stack) != null) return !functionStatus(stack).ready();
        List<Cell> source = cells(stack);
        if (!SwordCavityRules.usable(source)) return true;
        for (int index = 0; index < 4; index++) if (!source.get(index).present()) return true;
        return false;
    }

    private static List<Cell> effectiveCells(ItemStack stack) {
        List<Cell> source = new ArrayList<>(cells(stack));
        boolean applicable = applies(stack);
        for (Slot slot : Slot.values()) if (!applicable || !cellUnlocked(stack, slot.ordinal())) {
            source.set(slot.ordinal(), new Cell(State.EMPTY, Material.ORIGINAL));
        }
        return source;
    }

    public static List<Material> legacyMaterials(ItemStack stack) {
        List<Material> result = new ArrayList<>(List.of(Material.ORIGINAL, Material.ORIGINAL, Material.ORIGINAL));
        List<Cell> cells = effectiveCells(stack);
        for (Slot slot : Slot.values()) if (slot.legacyPart != null) {
            result.set(slot.legacyPart.ordinal(), SwordCavityRules.installed(cells, slot));
        }
        return List.copyOf(result);
    }

    public static List<Material> powerMaterials(ItemStack stack) {
        List<Cell> cells = effectiveCells(stack);
        if (!SwordCavityRules.usable(cells) || missingRequired(stack)) return List.of();
        List<Material> result = new ArrayList<>();
        for (Slot slot : Slot.values()) {
            Material material = SwordCavityRules.installed(cells, slot);
            if (material != Material.ORIGINAL && !result.contains(material)) result.add(material);
        }
        return List.copyOf(result);
    }

    public static Build build(ItemStack stack) {
        if (EquipmentWeaponBuild.applies(stack)) return EquipmentWeaponBuild.build(stack);
        CompoundTag data = stack.getTagElement(ROOT);
        Build base = data != null && data.getBoolean("GridValues") ? SwordCavityRules.gridBuild(effectiveCells(stack), linksEnabled(stack)) : legacyBuild(stack);
        if (missingRequired(stack)) return new Build(base.durability(), base.wear(), 0.0, 0.0, 0.0);
        return EquipmentWeaponBuild.scale(stack, WeaponDurabilityBuild.combat(stack, SwordCavityExtensions.applyBuild(stack, base)));
    }

    private static Build legacyBuild(ItemStack stack) {
        List<Cell> cells = effectiveCells(stack);
        List<Material> materials = new ArrayList<>(List.of(Material.ORIGINAL, Material.ORIGINAL, Material.ORIGINAL));
        for (Slot slot : Slot.values()) if (slot.legacyPart != null) {
            materials.set(slot.legacyPart.ordinal(), SwordCavityRules.installed(cells, slot));
        }
        Build base = FoodWeaponBuildRules.build(materials);
        if (!SwordCavityRules.usable(cells)) return new Build(base.durability(), base.wear(), 0.0, 0.0, 0.0);
        double durability = base.durability();
        double wear = base.wear();
        double damage = base.damage();
        double speed = base.speed();
        double reach = base.reach();
        double[][] weights = {{0, 0, 0, 0}, {0.25, 0.10, 0, 0.15}, {0, 0, 0, 0}, {0.10, 0, 0.12, 0},
                {0, 0, 0, 0}, {0.12, 0, 0.04, 0}, {0.08, 0, 0.12, 0}, {0, 0.08, 0.04, 0}};
        for (int index = 0; index < weights.length; index++) {
            Cell cell = cells.get(index);
            if (!cell.installed() || Slot.byIndex(index).legacyPart != null) continue;
            Material material = cell.material();
            durability *= 1.0 + (material.durability - 1.0) * weights[index][0];
            wear *= 1.0 + (material.wear - 1.0) * weights[index][0];
            damage *= 1.0 + (material.damage - 1.0) * weights[index][1];
            speed *= 1.0 + (material.speed - 1.0) * weights[index][2];
            reach += material.reach * weights[index][3];
        }
        List<SwordCavityRules.Link> links = links(stack);
        if (links.contains(SwordCavityRules.Link.FROST_EDGE)) { durability *= 1.08; wear *= 0.94; }
        if (links.contains(SwordCavityRules.Link.COPPER_CIRCUIT)) { speed *= 1.04; reach += 0.10; }
        return new Build(durability, Math.max(0.1, wear), damage, speed, Math.min(0.75, reach));
    }

    private static boolean linksEnabled(ItemStack stack) {
        CompoundTag data = stack.getTagElement(ROOT);
        return data != null && data.getBoolean("LinksEnabled");
    }

    public static List<SwordCavityRules.Link> links(ItemStack stack) {
        if (WeaponAttachmentPolicy.excluded(stack)) return List.of();
        if (EquipmentWeaponBuild.applies(stack)) return EquipmentWeaponBuild.links(stack);
        if (!linksEnabled(stack) || missingRequired(stack)) return List.of();
        List<Cell> cells = effectiveCells(stack);
        CompoundTag data = stack.getTagElement(ROOT);
        if (data != null && data.getBoolean("GridValues")) return SwordCavityRules.links(cells);
        if (!SwordCavityRules.usable(cells)) return List.of();
        Material core = SwordCavityRules.installed(cells, Slot.CORE);
        Material blade = SwordCavityRules.installed(cells, Slot.BLADE);
        if (core == Material.BOSS_FROST && (blade == Material.BOSS_FROST || blade == Material.DIAMOND)) {
            return List.of(SwordCavityRules.Link.FROST_EDGE);
        }
        if (core == Material.BOSS_CAPACITOR && (blade == Material.COPPER || SwordCavityRules.installed(cells, Slot.INLAY) == Material.COPPER)) {
            return List.of(SwordCavityRules.Link.COPPER_CIRCUIT);
        }
        return List.of();
    }

    public static ItemStack displayItem(ItemStack weapon, Slot slot) {
        return displayItem(weapon, slot, SwordNativeMaterials.parts(weapon));
    }

    public static ItemStack displayItem(ItemStack weapon, Slot slot, List<Piece> nativeParts) {
        if (slot != null && GourmetBladeCavity.fixed(weapon, slot.ordinal())) return GourmetBladeCavity.coreDisplay(weapon);
        if (!applies(weapon) || slot == null) return ItemStack.EMPTY;
        Cell cell = cells(weapon, nativeParts).get(slot.ordinal());
        if (!cell.present()) return ItemStack.EMPTY;
        ItemStack stored = storedItem(weapon, slot);
        if (!stored.isEmpty()) return stored;
        if (cell.state() == State.NATIVE) {
            ItemStack nativeItem = SwordNativeMaterials.display(weapon, slot.ordinal(), nativeParts);
            if (!nativeItem.isEmpty()) return nativeItem;
        }
        for (String identifier : WeaponMaterialCatalogRules.offerings(cell.material())) {
            ResourceLocation id = ResourceLocation.tryParse(identifier);
            if (id != null && ForgeRegistries.ITEMS.containsKey(id)) return new ItemStack(ForgeRegistries.ITEMS.getValue(id));
        }
        if (cell.state() == State.NATIVE) {
            if (weapon.getItem() instanceof TieredItem tiered) for (ItemStack ingredient : tiered.getTier().getRepairIngredient().getItems()) {
                if (!ingredient.isEmpty()) {
                    ItemStack icon = ingredient.copy();
                    icon.setCount(1);
                    return icon;
                }
            }
            ItemStack icon = weapon.copy();
            icon.setCount(1);
            return icon;
        }
        return ItemStack.EMPTY;
    }

    public static List<Component> materialEffects(Material material) {
        return List.of(Component.translatable("effect.ftboss.sword.material." + material.id()));
    }

    public static ItemStack storedItem(ItemStack weapon, Slot slot) {
        CompoundTag data = weapon.getTagElement(ROOT);
        if (data == null || data.getInt("Version") != SwordCavityRules.VERSION) return ItemStack.EMPTY;
        CompoundTag entry = data.getCompound("Cells").getCompound(slot.name());
        if (!entry.getString("State").equals("installed") || !entry.contains("Item", Tag.TAG_COMPOUND)) return ItemStack.EMPTY;
        ItemStack item = ItemStack.of(entry.getCompound("Item"));
        if (item.getCount() != 1 || item.isEmpty() || materialFor(item) != Material.byId(entry.getString("Material"))) {
            return ItemStack.EMPTY;
        }
        return item;
    }

    public static ItemStack preview(ItemStack stack, Slot slot, Material material) {
        ItemStack copy = stack.copy();
        if (slot != null && GourmetBladeCavity.fixed(stack, slot.ordinal())) return copy;
        if (!applies(copy) || !SwordCavityRules.accepts(slot, material) || !cellUnlocked(copy, slot.ordinal())) return copy;
        initialize(copy);
        copy.getOrCreateTagElement(ROOT).putBoolean("LinksEnabled", true);
        copy.getOrCreateTagElement(ROOT).putBoolean("GridValues", true);
        writeCell(copy, slot, new Cell(State.INSTALLED, material), null);
        return copy;
    }

    private static void initialize(ItemStack stack) {
        CompoundTag existing = stack.getTagElement(ROOT);
        if (existing != null && existing.getInt("Version") == SwordCavityRules.VERSION) return;
        List<Cell> old = cells(stack);
        CompoundTag oldCells = existing == null ? new CompoundTag() : existing.getCompound("Cells").copy();
        int revision = revision(stack);
        CompoundTag data = stack.getOrCreateTagElement(ROOT);
        data.putInt("Version", SwordCavityRules.VERSION);
        data.putInt("Revision", revision);
        if (data.getString("NativeItem").isEmpty()) data.putString("NativeItem", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        data.put("Cells", oldCells.copy());
        for (Slot slot : Slot.values()) writeCell(stack, slot, old.get(slot.ordinal()), null);
    }

    private static void writeCell(ItemStack stack, Slot slot, Cell cell, CompoundTag item) {
        CompoundTag data = stack.getOrCreateTagElement(ROOT);
        CompoundTag cells = data.getCompound("Cells");
        CompoundTag entry = new CompoundTag();
        entry.putString("State", cell.state().name().toLowerCase(java.util.Locale.ROOT));
        entry.putString("Material", cell.material().id());
        entry.putBoolean("EverPresent", cell.present() || cells.getCompound(slot.name()).getBoolean("EverPresent"));
        if (cell.installed() && item != null) entry.put("Item", item.copy());
        cells.put(slot.name(), entry);
        data.put("Cells", cells);
        if (slot.legacyPart != null) stack.getOrCreateTagElement(WeaponMaterialBuild.ROOT)
                .putString(slot.legacyPart.name(), cell.installed() ? cell.material().id() : Material.ORIGINAL.id());
        else stack.getOrCreateTagElement(WeaponMaterialBuild.ROOT);
    }

    private static boolean editable(ItemStack weapon) {
        if (ManorWeaponCavity.pendingMigration(weapon)) return false;
        if (!applies(weapon) || revision(weapon) < 0 || revision(weapon) == Integer.MAX_VALUE
                || weapon.isDamageableItem() && weapon.getMaxDamage() > 0 && weapon.getDamageValue() >= weapon.getMaxDamage()) return false;
        if (!SwordCavityRules.usable(cells(weapon))) return false;
        CompoundTag data = weapon.getTagElement(ROOT);
        if (data != null && data.getInt("Version") != 1 && data.getInt("Version") != SwordCavityRules.VERSION) return false;
        CompoundTag life = weapon.getTagElement(FoodWeaponLifecycle.ROOT);
        return life == null || !life.getBoolean("Broken");
    }

    private static boolean preservesLife(ItemStack weapon, ItemStack prospective) {
        if (prospective.isEmpty()) return false;
        if (!prospective.isDamageableItem()) return true;
        int maximum = prospective.getMaxDamage();
        return maximum > 0 && FoodWeaponBuildRules.preserveDamage(weapon.getDamageValue(), weapon.getMaxDamage(), maximum) < maximum;
    }

    public static boolean containerEditable(ItemStack weapon) { return editable(weapon); }

    public static List<ItemStack> containerItems(ItemStack weapon) {
        List<ItemStack> items = new ArrayList<>();
        for (Slot slot : Slot.values()) items.add(storedItem(weapon, slot));
        return items;
    }

    private static boolean samePiece(ItemStack first, ItemStack second) {
        return first.isEmpty() && second.isEmpty() || first.getCount() == second.getCount() && ItemStack.isSameItemSameTags(first, second);
    }

    private static ItemStack containerPreview(ItemStack weapon, List<ItemStack> items) {
        if (!editable(weapon) || items == null || items.size() != SwordCavityRules.CELL_COUNT) return ItemStack.EMPTY;
        ItemStack prospective = weapon.copy();
        initialize(prospective);
        prospective.getOrCreateTagElement(ROOT).putBoolean("GridValues", true);
        prospective.getOrCreateTagElement(ROOT).putBoolean("LinksEnabled", true);
        for (Slot slot : Slot.values()) {
            ItemStack item = items.get(slot.ordinal());
            if (GourmetBladeCavity.fixed(weapon, slot.ordinal()) && (item == null || !item.isEmpty())) return ItemStack.EMPTY;
            if (item == null || !item.isEmpty() && (item.getCount() != 1 || !SwordCavityRules.accepts(slot, materialFor(item)))) {
                return ItemStack.EMPTY;
            }
            ItemStack previous = storedItem(weapon, slot);
            if (samePiece(previous, item)) continue;
            if (!item.isEmpty() && !cellUnlocked(weapon, slot.ordinal())) return ItemStack.EMPTY;
            writeCell(prospective, slot, item.isEmpty() ? vacancy(slot) : new Cell(State.INSTALLED, materialFor(item)),
                    item.isEmpty() ? null : item.save(new CompoundTag()));
        }
        return prospective;
    }

    public static boolean canSetContainerItems(ItemStack weapon, List<ItemStack> items) {
        ItemStack prospective = containerPreview(weapon, items);
        return !prospective.isEmpty() && preservesLife(weapon, prospective);
    }

    public static boolean setContainerItems(ServerPlayer player, ItemStack weapon, List<ItemStack> items, int expectedRevision) {
        if (!player.isAlive() || player.isSpectator() || revision(weapon) != expectedRevision) return false;
        ItemStack prospective = containerPreview(weapon, items);
        if (prospective.isEmpty() || !preservesLife(weapon, prospective)) return false;
        boolean changed = false;
        for (Slot slot : Slot.values()) if (!samePiece(storedItem(weapon, slot), items.get(slot.ordinal()))) changed = true;
        if (!changed) return true;
        return applyContainerChange(player, weapon, prospective, expectedRevision);
    }

    private static boolean applyContainerChange(ServerPlayer player, ItemStack weapon, ItemStack prospective, int expectedRevision) {
        int oldDamage = weapon.getDamageValue();
        int oldMaximum = weapon.getMaxDamage();
        CompoundTag before = weapon.getTag() == null ? null : weapon.getTag().copy();
        try {
            FoodWeaponLifecycle.ensureTracked(weapon);
            SwordNativeMaterials.capture(weapon, player.serverLevel());
            weapon.getOrCreateTag().put(ROOT, prospective.getTagElement(ROOT).copy());
            weapon.getOrCreateTag().put(WeaponMaterialBuild.ROOT, prospective.getTagElement(WeaponMaterialBuild.ROOT).copy());
            finishChange(player, weapon, expectedRevision, oldDamage, oldMaximum);
            return true;
        } catch (RuntimeException failure) {
            weapon.setTag(before);
            throw failure;
        }
    }

    public static boolean canClearReference(ItemStack weapon, Slot slot) {
        if (slot != null && GourmetBladeCavity.fixed(weapon, slot.ordinal())) return false;
        if (!editable(weapon) || slot == null || !storedItem(weapon, slot).isEmpty() || !cells(weapon).get(slot.ordinal()).present()) return false;
        ItemStack prospective = weapon.copy();
        initialize(prospective);
        prospective.getOrCreateTagElement(ROOT).putBoolean("GridValues", true);
        prospective.getOrCreateTagElement(ROOT).putBoolean("LinksEnabled", true);
        writeCell(prospective, slot, vacancy(slot), null);
        return preservesLife(weapon, prospective);
    }

    public static boolean clearReference(ServerPlayer player, ItemStack weapon, Slot slot, int expectedRevision) {
        if (!player.isAlive() || player.isSpectator() || revision(weapon) != expectedRevision || !canClearReference(weapon, slot)) return false;
        ItemStack prospective = weapon.copy();
        initialize(prospective);
        writeCell(prospective, slot, vacancy(slot), null);
        return applyContainerChange(player, weapon, prospective, expectedRevision);
    }

    public static boolean canReplace(ItemStack weapon, Slot slot, Material material) {
        if (slot != null && GourmetBladeCavity.fixed(weapon, slot.ordinal())) return false;
        if (!editable(weapon) || !SwordCavityRules.accepts(slot, material) || !cellUnlocked(weapon, slot.ordinal())) return false;
        Cell current = cells(weapon).get(slot.ordinal());
        return !(current.installed() && current.material() == material) && preservesLife(weapon, preview(weapon, slot, material));
    }

    public static String replacementFailure(ItemStack weapon, Slot slot, Material material) {
        if (!applies(weapon) || slot == null || !SwordCavityRules.accepts(slot, material)) return "screen.ftboss.sword.invalid_material";
        if (!cellUnlocked(weapon, slot.ordinal())) return "screen.ftboss.cavity.cell_locked";
        CompoundTag data = weapon.getTagElement(ROOT);
        if (data != null && data.getInt("Version") != 1 && data.getInt("Version") != SwordCavityRules.VERSION
                || revision(weapon) < 0 || revision(weapon) == Integer.MAX_VALUE) return "screen.ftboss.sword.version_locked";
        CompoundTag life = weapon.getTagElement(FoodWeaponLifecycle.ROOT);
        if (weapon.isDamageableItem() && weapon.getMaxDamage() > 0 && weapon.getDamageValue() >= weapon.getMaxDamage()
                || life != null && life.getBoolean("Broken")) {
            return "screen.ftboss.sword.depleted";
        }
        Cell current = cells(weapon).get(slot.ordinal());
        if (current.installed() && current.material() == material) return "screen.ftboss.sword.same_material";
        return canReplace(weapon, slot, material) ? "" : "message.ftboss.material.repair_first";
    }

    private static void finishChange(ServerPlayer player, ItemStack weapon, int revision, int oldDamage, int oldMaximum) {
        weapon.getOrCreateTagElement(ROOT).putBoolean("LinksEnabled", true);
        weapon.getOrCreateTagElement(ROOT).putBoolean("GridValues", true);
        weapon.getOrCreateTagElement(ROOT).putInt("Revision", revision + 1);
        FoodWeaponLifecycle.materialDamage(weapon, FoodWeaponBuildRules.preserveDamage(oldDamage, oldMaximum, weapon.getMaxDamage()));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    public static boolean replace(ServerPlayer player, Slot slot, int inventorySlot, int expectedRevision) {
        if (!player.isAlive() || player.isSpectator() || inventorySlot < 0 || inventorySlot >= SwordCavityRules.INVENTORY_SIZE) return false;
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        ItemStack offering = player.getInventory().getItem(inventorySlot);
        Material material = materialFor(offering);
        if (offering == weapon || offering.isEmpty() || revision(weapon) != expectedRevision || !canReplace(weapon, slot, material)) return false;
        ItemStack returning = storedItem(weapon, slot);
        int destination = returning.isEmpty() ? -1 : returnDestination(player, weapon, returning, inventorySlot);
        if (!returning.isEmpty() && destination < 0) {
            player.sendSystemMessage(Component.translatable("screen.ftboss.sword.inventory_full"));
            return false;
        }
        int oldMaximum = weapon.getMaxDamage();
        int oldDamage = weapon.getDamageValue();
        ItemStack piece = offering.copy();
        piece.setCount(1);
        FoodWeaponLifecycle.ensureTracked(weapon);
        SwordNativeMaterials.capture(weapon, player.serverLevel());
        initialize(weapon);
        writeCell(weapon, slot, new Cell(State.INSTALLED, material), player.getAbilities().instabuild ? null : piece.save(new CompoundTag()));
        if (!player.getAbilities().instabuild) offering.shrink(1);
        if (!returning.isEmpty()) returnItem(player, returning, destination);
        finishChange(player, weapon, expectedRevision, oldDamage, oldMaximum);
        player.sendSystemMessage(Component.translatable("message.ftboss.sword.inserted", WeaponMaterialBuild.materialName(material))
                .withStyle(ChatFormatting.GOLD));
        return true;
    }

    private static Cell vacancy(Slot slot) { return new Cell(slot.required ? State.MISSING : State.EMPTY, Material.ORIGINAL); }

    private static int returnDestination(ServerPlayer player, ItemStack weapon, ItemStack item, int consumedSlot) {
        for (int index = 0; index < SwordCavityRules.INVENTORY_SIZE; index++) {
            ItemStack existing = player.getInventory().getItem(index);
            if (existing == weapon) continue;
            if (existing.isEmpty() || index == consumedSlot && existing.getCount() == 1 && !player.getAbilities().instabuild
                    || ItemStack.isSameItemSameTags(existing, item) && existing.getCount() < existing.getMaxStackSize()) return index;
        }
        return -1;
    }

    private static void returnItem(ServerPlayer player, ItemStack item, int destination) {
        ItemStack existing = player.getInventory().getItem(destination);
        if (existing.isEmpty()) player.getInventory().setItem(destination, item);
        else existing.grow(1);
    }

    public static boolean canRemove(ItemStack weapon, Slot slot) {
        if (slot != null && GourmetBladeCavity.fixed(weapon, slot.ordinal())) return false;
        if (!editable(weapon) || slot == null || storedItem(weapon, slot).isEmpty()) return false;
        ItemStack prospective = weapon.copy();
        initialize(prospective);
        prospective.getOrCreateTagElement(ROOT).putBoolean("GridValues", true);
        prospective.getOrCreateTagElement(ROOT).putBoolean("LinksEnabled", true);
        writeCell(prospective, slot, vacancy(slot), null);
        return preservesLife(weapon, prospective);
    }

    public static boolean remove(ServerPlayer player, Slot slot, int expectedRevision) {
        if (!player.isAlive() || player.isSpectator()) return false;
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        if (revision(weapon) != expectedRevision || !canRemove(weapon, slot)) return false;
        ItemStack item = storedItem(weapon, slot);
        int destination = returnDestination(player, weapon, item, -1);
        if (destination < 0) {
            player.sendSystemMessage(Component.translatable("screen.ftboss.sword.inventory_full"));
            return false;
        }
        int oldMaximum = weapon.getMaxDamage();
        int oldDamage = weapon.getDamageValue();
        FoodWeaponLifecycle.ensureTracked(weapon);
        SwordNativeMaterials.capture(weapon, player.serverLevel());
        initialize(weapon);
        writeCell(weapon, slot, vacancy(slot), null);
        returnItem(player, item, destination);
        finishChange(player, weapon, expectedRevision, oldDamage, oldMaximum);
        return true;
    }

    private static ItemStack movedPreview(ItemStack weapon, Slot from, Slot to) {
        ItemStack prospective = weapon.copy();
        initialize(prospective);
        prospective.getOrCreateTagElement(ROOT).putBoolean("GridValues", true);
        prospective.getOrCreateTagElement(ROOT).putBoolean("LinksEnabled", true);
        List<Cell> cells = cells(prospective);
        CompoundTag entries = prospective.getTagElement(ROOT).getCompound("Cells").copy();
        Cell target = cells.get(to.ordinal());
        writeCell(prospective, to, cells.get(from.ordinal()), entries.getCompound(from.name()).getCompound("Item"));
        writeCell(prospective, from, target.installed() ? target : vacancy(from),
                target.installed() ? entries.getCompound(to.name()).getCompound("Item") : null);
        return prospective;
    }

    public static boolean canMove(ItemStack weapon, Slot from, Slot to) {
        if (from != null && GourmetBladeCavity.fixed(weapon, from.ordinal())
                || to != null && GourmetBladeCavity.fixed(weapon, to.ordinal())) return false;
        return editable(weapon) && from != null && to != null && from != to && cellUnlocked(weapon, from.ordinal())
                && cellUnlocked(weapon, to.ordinal()) && cells(weapon).get(from.ordinal()).installed()
                && preservesLife(weapon, movedPreview(weapon, from, to));
    }

    public static boolean move(ServerPlayer player, Slot from, Slot to, int expectedRevision) {
        if (!player.isAlive() || player.isSpectator()) return false;
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        if (revision(weapon) != expectedRevision || !canMove(weapon, from, to)) return false;
        int oldMaximum = weapon.getMaxDamage();
        int oldDamage = weapon.getDamageValue();
        FoodWeaponLifecycle.ensureTracked(weapon);
        SwordNativeMaterials.capture(weapon, player.serverLevel());
        ItemStack prospective = movedPreview(weapon, from, to);
        weapon.getOrCreateTag().put(ROOT, prospective.getTagElement(ROOT).copy());
        weapon.getOrCreateTag().put(WeaponMaterialBuild.ROOT, prospective.getTagElement(WeaponMaterialBuild.ROOT).copy());
        finishChange(player, weapon, expectedRevision, oldDamage, oldMaximum);
        return true;
    }

    public static boolean losePart(ItemStack stack) {
        if (!active(stack) || revision(stack) == Integer.MAX_VALUE || revision(stack) < 0) return false;
        Slot slot = SwordCavityRules.lossSlot(cells(stack));
        if (slot != null && GourmetBladeCavity.fixed(stack, slot.ordinal())) return false;
        if (slot == null) return false;
        initialize(stack);
        writeCell(stack, slot, new Cell(State.MISSING, Material.ORIGINAL), null);
        stack.getOrCreateTagElement(ROOT).putInt("Revision", revision(stack) + 1);
        return true;
    }

    public static Component name(Slot slot) { return Component.translatable("part.ftboss.sword." + slot.id()); }

    public static Component stateName(ItemStack stack, Slot slot) { return stateName(cells(stack).get(slot.ordinal())); }

    public static Component stateName(Cell cell) {
        return Component.translatable("state.ftboss.sword." + cell.state().name().toLowerCase(java.util.Locale.ROOT));
    }
}
