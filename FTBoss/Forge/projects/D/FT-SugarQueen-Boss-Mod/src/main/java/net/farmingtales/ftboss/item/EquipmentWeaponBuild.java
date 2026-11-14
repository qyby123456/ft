package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public final class EquipmentWeaponBuild {
    public static final double STRENGTH = 1.50;
    private static final Piece EMPTY = new Piece("minecraft:air", 0, Origin.SHARED);

    private EquipmentWeaponBuild() {}

    public static void ensureIdentity(ItemStack stack) {
        if (!applies(stack)) return;
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null || !server.isSameThread()) return;
        var data = stack.getOrCreateTagElement(FoodWeaponLifecycle.ROOT);
        if (!data.hasUUID("Weapon")) data.putUUID("Weapon", java.util.UUID.randomUUID());
        capture(stack, server.overworld());
    }

    public static boolean applies(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() == 1 && stack.getItem() instanceof ArmorItem
                && !WeaponAttachmentPolicy.excluded(stack);
    }

    private static ItemStack reference(ItemStack stack) {
        Ingredient repair = ((ArmorItem) stack.getItem()).getMaterial().getRepairIngredient();
        ItemStack[] ingredients;
        try {
            ingredients = repair == null ? new ItemStack[0] : repair.getItems();
        } catch (NullPointerException invalidRepairTag) {
            ingredients = new ItemStack[0];
        }
        if (ingredients != null) for (ItemStack ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) return ingredient;
        }
        return new ItemStack(Items.LEATHER);
    }

    public static String family(ItemStack stack) {
        return switch (((ArmorItem) stack.getItem()).getEquipmentSlot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> throw new IllegalArgumentException("Not an armor slot");
        };
    }

    public static int[] layout(ItemStack stack) {
        int[] positions = new int[27];
        Arrays.setAll(positions, index -> index);
        return positions;
    }

    public static String functionKey(SwordFunctionRules.Function function, boolean compact) {
        return "function.ftboss.armor." + (compact ? "short." : "") + function.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String itemId(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    }

    private static List<String> environment(ItemStack stack) {
        return WeaponCavityShapeRules.environment(family(stack), itemId(stack)).stream()
                .filter(identifier -> ForgeRegistries.ITEMS.containsKey(new ResourceLocation(identifier))).toList();
    }

    public static List<Piece> parts(ItemStack stack) {
        List<Piece> cached = WeaponTooltipReadScope.cachedValue(stack, "ftboss.armor.parts");
        return cached != null ? cached : WeaponTooltipReadScope.rememberValue(stack, "ftboss.armor.parts", parts(stack, null));
    }

    public static List<Piece> parts(ItemStack stack, Level level) {
        CompoundTag saved = stack.getTagElement(SwordNativeMaterials.ROOT);
        if (saved != null && saved.getInt("EquipmentShape") == 2 && saved.getString("Weapon").equals(itemId(stack))) {
            List<Piece> stored = SwordNativeMaterials.budget(stack);
            if (stored.size() == 27) return DiamondAlloyArmorCore.parts(stack, stored);
        }
        List<Piece> result = new ArrayList<>(Collections.nCopies(27, EMPTY));
        if (level != null) fillRecipe(stack, level, result, new HashSet<>());
        if (result.stream().noneMatch(piece -> piece.count() > 0)) {
            String ingredient = itemId(reference(stack));
            for (int position : recipeShape(stack)) result.set(position, new Piece(ingredient, 1, Origin.THEME));
        }
        for (String identifier : environment(stack)) place(result, new Piece(identifier, 1, Origin.THEME), stack);
        return DiamondAlloyArmorCore.parts(stack, List.copyOf(result));
    }

    private static int[] recipeShape(ItemStack stack) {
        return switch (family(stack)) {
            case "helmet" -> new int[]{2, 4, 6, 11, 15};
            case "chestplate" -> new int[]{2, 6, 11, 13, 15, 20, 22, 24};
            case "leggings" -> new int[]{2, 4, 6, 11, 15, 20, 24};
            default -> new int[]{11, 15, 20, 24};
        };
    }

    private static Recipe<?> recipe(ItemStack stack, Level level) {
        ResourceLocation preferred = net.farmingtales.ftboss.api.SwordCavityExtensions.nativeRecipe(stack);
        return level.getRecipeManager().getRecipes().stream().filter(candidate -> {
            try {
                ItemStack output = candidate.getResultItem(level.registryAccess());
                return output.is(stack.getItem()) && output.getCount() == 1
                        && (!candidate.getIngredients().isEmpty() || candidate instanceof SmithingRecipe);
            } catch (RuntimeException ignored) {
                return false;
            }
        }).min(Comparator.comparingInt((Recipe<?> candidate) -> candidate.getId().equals(preferred) ? 0
                : candidate.getId().toString().equals(itemId(stack)) ? 1 : 2).thenComparing(candidate -> candidate.getId().toString())).orElse(null);
    }

    private static boolean fillRecipe(ItemStack stack, Level level, List<Piece> result, java.util.Set<String> visiting) {
        if (!visiting.add(itemId(stack)) || visiting.size() > 8) return false;
        Recipe<?> recipe = recipe(stack, level);
        if (recipe == null) {
            visiting.remove(itemId(stack));
            return false;
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        if (recipe instanceof SmithingRecipe smithing) {
            var bases = ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(smithing::isBaseIngredient);
            var additions = ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(smithing::isAdditionIngredient);
            ingredients = List.of(Ingredient.of(bases), Ingredient.of(additions));
        }
        for (int index = 0; index < ingredients.size(); index++) {
            ItemStack ingredient = Arrays.stream(ingredients.get(index).getItems()).filter(candidate -> !candidate.isEmpty())
                    .min(Comparator.comparing(EquipmentWeaponBuild::itemId)).orElse(ItemStack.EMPTY);
            if (ingredient.isEmpty() || itemId(ingredient).endsWith("_smithing_template")) continue;
            if (recipe instanceof SmithingRecipe && ingredient.getItem() instanceof ArmorItem
                    && fillRecipe(ingredient, level, result, visiting)) continue;
            Piece piece = new Piece(itemId(ingredient), 1, Origin.RECIPE);
            if (recipe instanceof ShapedRecipe shaped && shaped.getWidth() <= 3 && shaped.getHeight() <= 3) {
                int column = index % shaped.getWidth();
                int row = index / shaped.getWidth();
                if (family(stack).equals("boots")) row += 3 - shaped.getHeight();
                int position = row * 9 + 2 + column * 2;
                if (result.get(position).count() == 0) result.set(position, piece);
                else place(result, piece, stack);
            } else place(result, piece, stack);
        }
        visiting.remove(itemId(stack));
        return result.stream().anyMatch(piece -> piece.origin() == Origin.RECIPE);
    }

    private static void place(List<Piece> result, Piece piece, ItemStack stack) {
        for (int position : WeaponCavityShapeRules.outline(family(stack))) {
            if (result.get(position).count() == 0) {
                result.set(position, piece);
                return;
            }
        }
        for (int position = 0; position < result.size(); position++) {
            if (result.get(position).count() == 0) {
                result.set(position, piece);
                return;
            }
        }
        throw new IllegalArgumentException("Equipment recipe exceeds shared 27-cell capacity");
    }

    public static void capture(ItemStack stack, Level level) {
        if (!applies(stack) || level == null || level.isClientSide) return;
        CompoundTag previous = stack.getTagElement(SwordNativeMaterials.ROOT);
        if (previous != null && previous.getInt("EquipmentShape") == 2
                && previous.getString("Weapon").equals(itemId(stack))
                && (!DiamondAlloyArmorCore.applies(stack) || previous.getBoolean("DiamondAlloyCore"))) return;
        List<Piece> pieces = parts(stack, level);
        CompoundTag snapshot = previous == null ? new CompoundTag() : previous.copy();
        snapshot.putInt("Version", 1);
        snapshot.putInt("Size", 27);
        snapshot.putInt("EquipmentShape", 2);
        snapshot.putString("Weapon", itemId(stack));
        if (DiamondAlloyArmorCore.index(stack, pieces) >= 0) snapshot.putBoolean("DiamondAlloyCore", true);
        Recipe<?> recipe = recipe(stack, level);
        if (recipe != null) snapshot.putString("Recipe", recipe.getId().toString());
        CompoundTag entries = new CompoundTag();
        for (int index = 0; index < pieces.size(); index++) {
            Piece piece = pieces.get(index);
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", piece.itemId());
            entry.putInt("Count", piece.count());
            entry.putString("Origin", piece.origin().name());
            entries.put(Integer.toString(index), entry);
        }
        snapshot.put("Parts", entries);
        stack.getOrCreateTag().put(SwordNativeMaterials.ROOT, snapshot);
    }

    public static SwordFunctionRules.Profile profile(ItemStack stack) {
        SwordFunctionRules.Profile cached = WeaponTooltipReadScope.cachedValue(stack, "ftboss.armor.profile");
        if (cached != null) return cached;
        List<Piece> parts = parts(stack);
        int core = DiamondAlloyArmorCore.index(stack, parts);
        var outline = new HashSet<Integer>();
        for (int position : WeaponCavityShapeRules.outline(family(stack))) outline.add(position);
        List<Integer> unlocks = new ArrayList<>();
        List<SwordFunctionRules.Values> weights = new ArrayList<>();
        List<SwordFunctionRules.Values> bases = new ArrayList<>();
        var overrides = new java.util.HashMap<>(SwordFunctionRules.iron().itemValues());
        List<SwordFunctionRules.Input> inputs = new ArrayList<>();
        int evolutionCells = 0;
        for (int index = 0; index < 27; index++) {
            Piece piece = parts.get(index);
            boolean present = piece.count() > 0 && index != core;
            boolean initial = outline.contains(index) || present || index == core;
            boolean paid = !initial && !SwordCavityBuild.storedItem(stack, SwordCavityRules.Slot.byIndex(index)).isEmpty();
            int unlockLevel = initial ? 0 : 1 + evolutionCells++ / 2;
            unlocks.add(paid ? 0 : unlockLevel <= SwordFunctionRules.MAX_EVOLUTION ? unlockLevel : -1);
            int row = index / 9;
            int column = index % 9;
            weights.add(new SwordFunctionRules.Values(row == 0 ? 1 : 0.5, column <= 2 || column >= 6 ? 1 : 0.6,
                    column == 4 ? 1 : 0.5, 0.8, row == 2 ? 1 : 0.5));
            bases.add(new SwordFunctionRules.Values(0.04, 0.04, 0.04, 0.04, 0.04));
            Material material = SwordCompatibilityRules.material(piece.itemId());
            if (present) overrides.putIfAbsent(piece.itemId(), SwordCompatibilityRules.materialFunctions(material == Material.ORIGINAL ? Material.IRON : material));
            inputs.add(new SwordFunctionRules.Input(piece.itemId(), material, present));
        }
        var provisional = new SwordFunctionRules.Profile(SwordFunctionRules.ZERO, weights, unlocks,
                SwordFunctionRules.iron().materials(), bases, overrides);
        var total = SwordFunctionRules.evaluateItems(provisional, inputs, 0).total();
        var minimum = new SwordFunctionRules.Values(total.handle() * 0.995, total.guard() * 0.995,
                total.core() * 0.995, total.stability() * 0.995, total.blade() * 0.995);
        return WeaponTooltipReadScope.rememberValue(stack, "ftboss.armor.profile",
                DiamondAlloyArmorCore.profile(new SwordFunctionRules.Profile(minimum, weights, unlocks, provisional.materials(), bases, overrides), total, core));
    }

    public static boolean cellUnlocked(ItemStack stack, int index) {
        if (!applies(stack) || index < 0 || index >= SwordCavityRules.CELL_COUNT) return false;
        SwordFunctionRules.Profile cached = WeaponTooltipReadScope.cachedValue(stack, "ftboss.armor.profile");
        if (cached != null) return SwordFunctionRules.unlocked(cached, index, FoodWeaponEvolution.evolutionLevel(stack));
        int initial = 0;
        for (int position : WeaponCavityShapeRules.outline(family(stack))) initial |= 1 << position;
        if ((initial & 1 << index) != 0) return true;
        List<Piece> parts = parts(stack);
        int core = DiamondAlloyArmorCore.index(stack, parts);
        for (int position = 0; position < SwordCavityRules.CELL_COUNT; position++) {
            if (parts.get(position).count() > 0 || position == core) initial |= 1 << position;
        }
        if ((initial & 1 << index) != 0) return true;
        int unlockLevel = 1 + Integer.bitCount(~initial & ((1 << index) - 1)) / 2;
        int evolution = Math.max(0, Math.min(SwordFunctionRules.MAX_EVOLUTION, FoodWeaponEvolution.evolutionLevel(stack)));
        return unlockLevel <= evolution || !SwordCavityBuild.storedItem(stack, SwordCavityRules.Slot.byIndex(index)).isEmpty();
    }

    public static Build scale(ItemStack stack, Build build) {
        if (!applies(stack) || build.damage() == 0) return GourmetAccessoryEffects.scale(build, stack);
        return new Build(1 + (build.durability() - 1) * STRENGTH, 1 + (build.wear() - 1) * STRENGTH,
                1 + (build.damage() - 1) * STRENGTH, 1 + (build.speed() - 1) * STRENGTH, build.reach() * STRENGTH);
    }

    public static List<ItemStack> bondItems(ItemStack stack) {
        if (!applies(stack) || SwordCavityBuild.missingRequired(stack)) return List.of();
        List<Piece> parts = parts(stack);
        var cells = SwordCavityBuild.cells(stack, parts);
        List<ItemStack> items = new ArrayList<>();
        for (int index = 0; index < cells.size(); index++) {
            if (DiamondAlloyArmorCore.fixed(stack, index)) continue;
            if (!SwordCavityBuild.cellUnlocked(stack, index)) continue;
            var cell = cells.get(index);
            ItemStack item = cell.state() == net.farmingtales.ftboss.util.SwordCavityRules.State.NATIVE
                    ? SwordNativeMaterials.display(stack, index, parts)
                    : cell.installed() ? SwordCavityBuild.storedItem(stack,
                            net.farmingtales.ftboss.util.SwordCavityRules.Slot.byIndex(index)) : ItemStack.EMPTY;
            if (!item.isEmpty()) items.add(item.copy());
        }
        return List.copyOf(items);
    }

    public static List<net.farmingtales.ftboss.util.SwordCavityRules.Link> links(ItemStack stack) {
        return net.farmingtales.ftboss.util.WeaponAccessoryLinkRules.links(bondMaterials(stack), true);
    }

    private static List<Material> bondMaterials(ItemStack stack) {
        return bondItems(stack).stream().map(net.farmingtales.ftboss.api.SwordMaterialExtensions::baseMaterial).toList();
    }

    public static List<net.farmingtales.ftboss.api.SwordMaterialDefinition> bondDefinitions(ItemStack stack) {
        return bondItems(stack).stream().map(net.farmingtales.ftboss.api.SwordMaterialExtensions::resolve)
                .flatMap(java.util.Optional::stream).toList();
    }

    public static Build build(ItemStack stack) {
        var cells = new ArrayList<>(SwordCavityBuild.cells(stack));
        for (int index = 0; index < cells.size(); index++) if (!SwordCavityBuild.cellUnlocked(stack, index)) {
            cells.set(index, new net.farmingtales.ftboss.util.SwordCavityRules.Cell(
                    net.farmingtales.ftboss.util.SwordCavityRules.State.EMPTY, Material.ORIGINAL));
        }
        Build base = net.farmingtales.ftboss.util.SwordCavityRules.gridBuild(cells, false);
        if (SwordCavityBuild.missingRequired(stack)) return new Build(base.durability(), base.wear(), 0, 0, 0);
        Build linked = net.farmingtales.ftboss.util.WeaponAccessoryLinkRules.apply(base, bondMaterials(stack), true);
        return scale(stack, WeaponDurabilityBuild.combat(stack,
                net.farmingtales.ftboss.api.SwordCavityExtensions.applyBuild(stack, linked)));
    }

    public static int durabilityCost(ItemStack stack, int requested) {
        if (requested <= 0) return requested;
        ensureIdentity(stack);
        CompoundTag data = stack.getOrCreateTagElement(WeaponMaterialBuild.ROOT);
        var wear = net.farmingtales.ftboss.util.FoodWeaponBuildRules.wear(requested, SwordCavityBuild.build(stack).wear(),
                data.getDouble("WearCarry"));
        data.putDouble("WearCarry", wear.carry());
        return wear.damage();
    }

    public static Bonus scale(ItemStack stack, Bonus bonus) {
        return !applies(stack) ? GourmetAccessoryEffects.scale(bonus, stack) : new Bonus(bonus.spellPower() * STRENGTH, bonus.maxMana() * STRENGTH,
                bonus.attackDamage() * STRENGTH, bonus.armor() * STRENGTH, bonus.maxHealth() * STRENGTH);
    }

    public static int maxDurability(ItemStack stack, int original) {
        double multiplier = net.farmingtales.ftboss.util.WeaponDurabilityRules.multiplier(WeaponDurabilityBuild.installedAlloys(stack));
        return net.farmingtales.ftboss.util.FoodWeaponBuildRules.maxDurability(original, scale(stack, new Build(multiplier, 1, 1, 1, 0)));
    }

    public static void attributes(net.minecraftforge.event.ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!applies(stack) || ((ArmorItem) stack.getItem()).getEquipmentSlot() != event.getSlotType()) return;
        Bonus bonus = WeaponMaterialBonuses.bonuses(stack);
        add(event, "irons_spellbooks:spell_power", bonus.spellPower());
        add(event, "irons_spellbooks:max_mana", bonus.maxMana());
        add(event, "minecraft:generic.attack_damage", bonus.attackDamage());
        add(event, "minecraft:generic.armor", bonus.armor());
        add(event, "minecraft:generic.max_health", bonus.maxHealth());
    }

    private static void add(net.minecraftforge.event.ItemAttributeModifierEvent event, String name, double amount) {
        var attribute = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation(name));
        if (attribute == null || amount == 0) return;
        var identifier = java.util.UUID.nameUUIDFromBytes(("ftboss.material_rarity." + event.getSlotType().getName() + "." + name)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        event.addModifier(attribute, new net.minecraft.world.entity.ai.attributes.AttributeModifier(identifier,
                "ftboss.material_rarity." + name, amount, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }
}
