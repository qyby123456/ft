package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.WeaponAccessoryLinkRules;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Wear;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Input;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.farmingtales.ftboss.util.WeaponMaterialBonusRules.Bonus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class EquipmentWeaponBuild {
    public static final double STRENGTH = 1.5;
    private static final Piece EMPTY = new Piece("minecraft:air", 0, Origin.SHARED);

    private EquipmentWeaponBuild() {
    }

    public static boolean applies(ItemStack var0) {
        return !var0.isEmpty() && var0.getCount() == 1 && var0.getItem() instanceof ArmorItem && !WeaponAttachmentPolicy.excluded(var0);
    }

    private static ItemStack reference(ItemStack var0) {
        Ingredient var1 = ((ArmorItem)var0.getItem()).getMaterial().value().repairIngredient().get();

        ItemStack[] var2;
        try {
            var2 = var1 == null ? new ItemStack[0] : var1.getItems();
        } catch (NullPointerException var7) {
            var2 = new ItemStack[0];
        }

        if (var2 != null) {
            for (ItemStack var6 : var2) {
                if (var6 != null && !var6.isEmpty()) {
                    return var6;
                }
            }
        }

        return new ItemStack(Items.LEATHER);
    }

    public static Build scale(ItemStack var0, Build var1) {
        return applies(var0) && var1.damage() != 0.0
            ? GourmetAccessoryEffects.scale(
                new Build(
                    1.0 + (var1.durability() - 1.0) * 1.5,
                    1.0 + (var1.wear() - 1.0) * 1.5,
                    1.0 + (var1.damage() - 1.0) * 1.5,
                    1.0 + (var1.speed() - 1.0) * 1.5,
                    var1.reach() * 1.5
                ),
                var0
            )
            : GourmetAccessoryEffects.scale(var1, var0);
    }

    public static Bonus scale(ItemStack var0, Bonus var1) {
        return GourmetAccessoryEffects.scale(
            !applies(var0)
                ? var1
                : new Bonus(var1.spellPower() * 1.5, var1.maxMana() * 1.5, var1.attackDamage() * 1.5, var1.armor() * 1.5, var1.maxHealth() * 1.5),
            var0
        );
    }

    public static int maxDurability(ItemStack var0, int var1) {
        double var2 = WeaponDurabilityRules.multiplier(WeaponDurabilityBuild.installedAlloys(var0));
        return FoodWeaponBuildRules.maxDurability(var1, scale(var0, new Build(var2, 1.0, 1.0, 1.0, 0.0)));
    }

    public static void attributes(ItemAttributeModifierEvent var0) {
        ItemStack var1 = var0.getItemStack();
        if (applies(var1)) {
            Bonus var2 = WeaponMaterialBonuses.bonuses(var1);
            add(var0, "irons_spellbooks:spell_power", var2.spellPower());
            add(var0, "irons_spellbooks:max_mana", var2.maxMana());
            add(var0, "minecraft:generic.attack_damage", var2.attackDamage());
            add(var0, "minecraft:generic.armor", var2.armor());
            add(var0, "minecraft:generic.max_health", var2.maxHealth());
        }
    }

    private static void add(ItemAttributeModifierEvent var0, String var1, double var2) {
        Holder<Attribute> var4 = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(var1)).orElse(null);
        if (var4 != null && var2 != 0.0) {
            UUID var5 = UUID.nameUUIDFromBytes(("ftboss.material_rarity." + ((ArmorItem)var0.getItemStack().getItem()).getEquipmentSlot().getName() + "." + var1).getBytes(StandardCharsets.UTF_8));
            var0.addModifier(var4, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss", "material_rarity/" + var5), var2, Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(((ArmorItem)var0.getItemStack().getItem()).getEquipmentSlot()));
        }
    }

    public static void ensureIdentity(ItemStack var0) {
        if (applies(var0)) {
            MinecraftServer var1 = ServerLifecycleHooks.getCurrentServer();
            if (var1 != null && var1.isSameThread()) {
                FoodWeaponData.edit(var0, "FTBossWeaponLifecycle", lifecycle -> {
                    if (!lifecycle.hasUUID("Weapon")) {
                        lifecycle.putUUID("Weapon", UUID.randomUUID());
                    }
                });

                capture(var0, var1.overworld());
            }
        }
    }

    public static String family(ItemStack var0) {
        return switch (((ArmorItem)var0.getItem()).getEquipmentSlot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> throw new IllegalArgumentException("Not an armor slot");
        };
    }

    public static int[] layout(ItemStack var0) {
        int[] var1 = new int[27];
        Arrays.setAll(var1, var0x -> var0x);
        return var1;
    }

    public static String functionKey(Function var0, boolean var1) {
        return "function.ftboss.armor." + (var1 ? "short." : "") + var0.name().toLowerCase(Locale.ROOT);
    }

    private static String itemId(ItemStack var0) {
        return BuiltInRegistries.ITEM.getKey(var0.getItem()).toString();
    }

    private static List<String> environment(ItemStack var0) {
        return WeaponCavityShapeRules.environment(family(var0), itemId(var0))
            .stream()
            .filter(var0x -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(var0x)))
            .toList();
    }

    public static List<Piece> parts(ItemStack var0) {
        List var1 = (List)WeaponTooltipReadScope.cachedValue(var0, "ftboss.armor.parts");
        return var1 != null ? var1 : (List)WeaponTooltipReadScope.rememberValue(var0, "ftboss.armor.parts", parts(var0, null));
    }

    private static int[] recipeShape(ItemStack var0) {
        String var1 = family(var0);

        return switch (var1) {
            case "helmet" -> new int[]{2, 4, 6, 11, 15};
            case "chestplate" -> new int[]{2, 6, 11, 13, 15, 20, 22, 24};
            case "leggings" -> new int[]{2, 4, 6, 11, 15, 20, 24};
            default -> new int[]{11, 15, 20, 24};
        };
    }

    private static RecipeHolder<?> recipe(ItemStack var0, Level var1) {
        ResourceLocation var2 = SwordCavityExtensions.nativeRecipe(var0);
        return var1.getRecipeManager()
            .getRecipes()
            .stream()
            .filter(var2x -> {
                try {
                    ItemStack var3 = var2x.value().getResultItem(var1.registryAccess());
                    return var3.is(var0.getItem()) && var3.getCount() == 1 && (!var2x.value().getIngredients().isEmpty() || var2x.value() instanceof SmithingRecipe);
                } catch (RuntimeException var4) {
                    return false;
                }
            })
            .min(
                Comparator.<RecipeHolder<?>>comparingInt(var2x -> var2x.id().equals(var2) ? 0 : (var2x.id().toString().equals(itemId(var0)) ? 1 : 2))
                    .thenComparing(var0x -> var0x.id().toString())
            )
            .orElse(null);
    }

    private static void place(List<Piece> var0, Piece var1, ItemStack var2) {
        for (int var6 : WeaponCavityShapeRules.outline(family(var2))) {
            if (((Piece)var0.get(var6)).count() == 0) {
                var0.set(var6, var1);
                return;
            }
        }

        for (int var7 = 0; var7 < var0.size(); var7++) {
            if (((Piece)var0.get(var7)).count() == 0) {
                var0.set(var7, var1);
                return;
            }
        }

        throw new IllegalArgumentException("Equipment recipe exceeds shared 27-cell capacity");
    }

    private static boolean fillRecipe(ItemStack var0, Level var1, List<Piece> var2, Set<String> var3) {
        if (var3.add(itemId(var0)) && var3.size() <= 8) {
            RecipeHolder<?> recipeHolder = recipe(var0, var1);
            Recipe<?> var4 = recipeHolder == null ? null : recipeHolder.value();
            if (var4 == null) {
                var3.remove(itemId(var0));
                return false;
            } else {
                List<Ingredient> var5 = var4.getIngredients();
                if (var4 instanceof SmithingRecipe var6) {
                    Stream<ItemStack> var7 = BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(var6::isBaseIngredient);
                    Stream<ItemStack> var8 = BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(var6::isAdditionIngredient);
                    var5 = List.of(Ingredient.of(var7), Ingredient.of(var8));
                }

                for (int var13 = 0; var13 < var5.size(); var13++) {
                    ItemStack var14 = Arrays.stream(((Ingredient)var5.get(var13)).getItems())
                        .filter(var0x -> !var0x.isEmpty())
                        .min(Comparator.comparing(EquipmentWeaponBuild::itemId))
                        .orElse(ItemStack.EMPTY);
                    if (!var14.isEmpty()
                        && !itemId(var14).endsWith("_smithing_template")
                        && (!(var4 instanceof SmithingRecipe) || !(var14.getItem() instanceof ArmorItem) || !fillRecipe(var14, var1, var2, var3))) {
                        Piece var15 = new Piece(itemId(var14), 1, Origin.RECIPE);
                        if (var4 instanceof ShapedRecipe) {
                            ShapedRecipe var9 = (ShapedRecipe)var4;
                            if (var9.getWidth() <= 3 && var9.getHeight() <= 3) {
                                int var10 = var13 % var9.getWidth();
                                int var11 = var13 / var9.getWidth();
                                if (family(var0).equals("boots")) {
                                    var11 += 3 - var9.getHeight();
                                }

                                int var12 = var11 * 9 + 2 + var10 * 2;
                                if (((Piece)var2.get(var12)).count() == 0) {
                                    var2.set(var12, var15);
                                } else {
                                    place(var2, var15, var0);
                                }
                                continue;
                            }
                        }

                        place(var2, var15, var0);
                    }
                }

                var3.remove(itemId(var0));
                return var2.stream().anyMatch(var0x -> var0x.origin() == Origin.RECIPE);
            }
        } else {
            return false;
        }
    }

    public static List<Link> links(ItemStack var0) {
        return WeaponAccessoryLinkRules.links(bondMaterials(var0), true);
    }

    private static List<Material> bondMaterials(ItemStack var0) {
        return bondItems(var0).stream().<Material>map(SwordMaterialExtensions::baseMaterial).toList();
    }

    public static List<SwordMaterialDefinition> bondDefinitions(ItemStack var0) {
        return bondItems(var0).stream().map(SwordMaterialExtensions::resolve).flatMap(Optional::stream).toList();
    }

    public static Build build(ItemStack var0) {
        ArrayList<Cell> var1 = new ArrayList<>(SwordCavityBuild.cells(var0));

        for (int var2 = 0; var2 < var1.size(); var2++) {
            if (!SwordCavityBuild.cellUnlocked(var0, var2)) {
                var1.set(var2, new Cell(State.EMPTY, Material.ORIGINAL));
            }
        }

        Build var4 = SwordCavityRules.gridBuild(var1, false);
        if (SwordCavityBuild.missingRequired(var0)) {
            return new Build(var4.durability(), var4.wear(), 0.0, 0.0, 0.0);
        } else {
            Build var3 = WeaponAccessoryLinkRules.apply(var4, bondMaterials(var0), true);
            return scale(var0, WeaponDurabilityBuild.combat(var0, SwordCavityExtensions.applyBuild(var0, var3)));
        }
    }

    public static int durabilityCost(ItemStack var0, int var1) {
        if (var1 <= 0) {
            return var1;
        } else {
            ensureIdentity(var0);
            CompoundTag var2 = FoodWeaponData.read(var0, "FTBossWeaponMaterials");
            if (var2 == null) {
                var2 = new CompoundTag();
            }
            Wear var3 = FoodWeaponBuildRules.wear(var1, SwordCavityBuild.build(var0).wear(), var2.getDouble("WearCarry"));
            var2.putDouble("WearCarry", var3.carry());
            FoodWeaponData.write(var0, "FTBossWeaponMaterials", var2);
            return var3.damage();
        }
    }

    public static List<Piece> parts(ItemStack var0, Level var1) {
        CompoundTag var2 = FoodWeaponData.read(var0, "FTBossSwordNativeMaterials");
        if (var2 != null && var2.getInt("EquipmentShape") == 2 && var2.getString("Weapon").equals(itemId(var0))) {
            List var3 = SwordNativeMaterials.budget(var0);
            if (var3.size() == 27) {
                return DiamondAlloyArmorCore.parts(var0, var3);
            }
        }

        ArrayList<Piece> var9 = new ArrayList<>(Collections.nCopies(27, EMPTY));
        if (var1 != null) {
            fillRecipe(var0, var1, var9, new HashSet<>());
        }

        if (var9.stream().noneMatch(var0x -> var0x.count() > 0)) {
            String var4 = itemId(reference(var0));

            for (int var8 : recipeShape(var0)) {
                var9.set(var8, new Piece(var4, 1, Origin.THEME));
            }
        }

        for (String var11 : environment(var0)) {
            place(var9, new Piece(var11, 1, Origin.THEME), var0);
        }

        return DiamondAlloyArmorCore.parts(var0, List.copyOf(var9));
    }

    public static void capture(ItemStack var0, Level var1) {
        if (applies(var0) && var1 != null && !var1.isClientSide) {
            CompoundTag var2 = FoodWeaponData.read(var0, "FTBossSwordNativeMaterials");
            if (var2 == null
                || var2.getInt("EquipmentShape") != 2
                || !var2.getString("Weapon").equals(itemId(var0))
                || DiamondAlloyArmorCore.applies(var0) && !var2.getBoolean("DiamondAlloyCore")) {
                List var3 = parts(var0, var1);
                CompoundTag var4 = var2 == null ? new CompoundTag() : var2.copy();
                var4.putInt("Version", 1);
                var4.putInt("Size", 27);
                var4.putInt("EquipmentShape", 2);
                var4.putString("Weapon", itemId(var0));
                if (DiamondAlloyArmorCore.index(var0, var3) >= 0) {
                    var4.putBoolean("DiamondAlloyCore", true);
                }

                RecipeHolder<?> var5 = recipe(var0, var1);
                if (var5 != null) {
                    var4.putString("Recipe", var5.id().toString());
                }

                CompoundTag var6 = new CompoundTag();

                for (int var7 = 0; var7 < var3.size(); var7++) {
                    Piece var8 = (Piece)var3.get(var7);
                    CompoundTag var9 = new CompoundTag();
                    var9.putString("Id", var8.itemId());
                    var9.putInt("Count", var8.count());
                    var9.putString("Origin", var8.origin().name());
                    var6.put(Integer.toString(var7), var9);
                }

                var4.put("Parts", var6);
                FoodWeaponData.write(var0, "FTBossSwordNativeMaterials", var4);
            }
        }
    }

    public static Profile profile(ItemStack var0) {
        Profile var1 = (Profile)WeaponTooltipReadScope.cachedValue(var0, "ftboss.armor.profile");
        if (var1 != null) {
            return var1;
        } else {
            List var2 = parts(var0);
            int var3 = DiamondAlloyArmorCore.index(var0, var2);
            HashSet<Integer> var4 = new HashSet<>();

            for (int var8 : WeaponCavityShapeRules.outline(family(var0))) {
                var4.add(var8);
            }

            ArrayList<Integer> var20 = new ArrayList<>();
            ArrayList<Values> var21 = new ArrayList<>();
            ArrayList<Values> var22 = new ArrayList<>();
            HashMap<String, Values> var23 = new HashMap<>(SwordFunctionRules.iron().itemValues());
            ArrayList<Input> var9 = new ArrayList<>();
            int var10 = 0;

            for (int var11 = 0; var11 < 27; var11++) {
                Piece var12 = (Piece)var2.get(var11);
                boolean var13 = var12.count() > 0 && var11 != var3;
                boolean var14 = var4.contains(var11) || var13 || var11 == var3;
                boolean var15 = !var14 && !SwordCavityBuild.storedItem(var0, Slot.byIndex(var11)).isEmpty();
                int var16 = var14 ? 0 : 1 + var10++ / 2;
                var20.add(var15 ? 0 : (var16 <= 4 ? var16 : -1));
                int var17 = var11 / 9;
                int var18 = var11 % 9;
                var21.add(new Values(var17 == 0 ? 1.0 : 0.5, var18 > 2 && var18 < 6 ? 0.6 : 1.0, var18 == 4 ? 1.0 : 0.5, 0.8, var17 == 2 ? 1.0 : 0.5));
                var22.add(new Values(0.04, 0.04, 0.04, 0.04, 0.04));
                Material var19 = SwordCompatibilityRules.material(var12.itemId());
                if (var13) {
                    var23.putIfAbsent(var12.itemId(), SwordCompatibilityRules.materialFunctions(var19 == Material.ORIGINAL ? Material.IRON : var19));
                }

                var9.add(new Input(var12.itemId(), var19, var13));
            }

            Profile var24 = new Profile(SwordFunctionRules.ZERO, var21, var20, SwordFunctionRules.iron().materials(), var22, var23);
            Values var25 = SwordFunctionRules.evaluateItems(var24, var9, 0).total();
            Values var26 = new Values(var25.handle() * 0.995, var25.guard() * 0.995, var25.core() * 0.995, var25.stability() * 0.995, var25.blade() * 0.995);
            return (Profile)WeaponTooltipReadScope.rememberValue(
                var0, "ftboss.armor.profile", DiamondAlloyArmorCore.profile(new Profile(var26, var21, var20, var24.materials(), var22, var23), var25, var3)
            );
        }
    }

    public static List<ItemStack> bondItems(ItemStack var0) {
        if (applies(var0) && !SwordCavityBuild.missingRequired(var0)) {
            List var1 = parts(var0);
            List var2 = SwordCavityBuild.cells(var0, var1);
            ArrayList<ItemStack> var3 = new ArrayList<>();

            for (int var4 = 0; var4 < var2.size(); var4++) {
                if (!DiamondAlloyArmorCore.fixed(var0, var4) && SwordCavityBuild.cellUnlocked(var0, var4)) {
                    Cell var5 = (Cell)var2.get(var4);
                    ItemStack var6 = var5.state() == State.NATIVE
                        ? SwordNativeMaterials.display(var0, var4, var1)
                        : (var5.installed() ? SwordCavityBuild.storedItem(var0, Slot.byIndex(var4)) : ItemStack.EMPTY);
                    if (!var6.isEmpty()) {
                        var3.add(var6.copy());
                    }
                }
            }

            return List.copyOf(var3);
        } else {
            return List.of();
        }
    }

    public static boolean cellUnlocked(ItemStack var0, int var1) {
        if (applies(var0) && var1 >= 0 && var1 < 27) {
            Profile var2 = (Profile)WeaponTooltipReadScope.cachedValue(var0, "ftboss.armor.profile");
            if (var2 != null) {
                return SwordFunctionRules.unlocked(var2, var1, FoodWeaponEvolution.evolutionLevel(var0));
            } else {
                int var3 = 0;

                for (int var7 : WeaponCavityShapeRules.outline(family(var0))) {
                    var3 |= 1 << var7;
                }

                if ((var3 & 1 << var1) != 0) {
                    return true;
                } else {
                    List var8 = parts(var0);
                    int var9 = DiamondAlloyArmorCore.index(var0, var8);

                    for (int var10 = 0; var10 < 27; var10++) {
                        if (((Piece)var8.get(var10)).count() > 0 || var10 == var9) {
                            var3 |= 1 << var10;
                        }
                    }

                    if ((var3 & 1 << var1) != 0) {
                        return true;
                    } else {
                        int var11 = 1 + Integer.bitCount(~var3 & (1 << var1) - 1) / 2;
                        int var12 = Math.max(0, Math.min(4, FoodWeaponEvolution.evolutionLevel(var0)));
                        return var11 <= var12 || !SwordCavityBuild.storedItem(var0, Slot.byIndex(var1)).isEmpty();
                    }
                }
            }
        } else {
            return false;
        }
    }
}
