package ftboss.storage308;

import java.lang.reflect.Method;
import java.util.List;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class InfusionEvidenceBefore433 {
    private static final String TYPE = "com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe";
    private static final String RECIPE = "cataclysm:weapon_infusion/wrath_of_the_desert";

    private InfusionEvidenceBefore433() {
    }

    public static CompoundTag read(RecipeHolder<?> holder, RegistryAccess var1) {
        Recipe<?> var0 = holder == null ? null : holder.value();
        if (var0 != null
            && var0.getClass().getName().equals("com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe")
            && holder.id().toString().equals("cataclysm:weapon_infusion/wrath_of_the_desert")) {
            try {
                if (!var0.getSerializer().getClass().getName().equals("com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe$Serializer")) {
                    return null;
                } else {
                    ItemStack var2 = var0.getResultItem(var1);
                    if (var2 != null
                        && !var2.isEmpty()
                        && var2.getCount() == 1
                        && FourWeaponFoundationAdapter.registryId(var2).equals("cataclysm:wrath_of_the_desert")) {
                        CompoundTag var3 = new CompoundTag();
                        var3.putString("Id", holder.id().toString());
                        var3.putString("Type", "com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe");
                        var3.putString("Encoding", RecipeDataBefore433.ENCODING);
                        var3.putString("Serializer", var0.getSerializer().getClass().getName());
                        var3.put("Result", RecipeDataBefore433.stack(var2, var1));
                        var3.putString("ResultMode", "STATIC_RESULT_BASE_TAG_OVERRIDE_1_20_1");
                        var3.putString("IngredientMode", "CATACLYSM_WEAPON_FUSION_ORDERED_ROLES");
                        var3.putInt("SlotCount", 2);
                        ListTag var4 = new ListTag();
                        List var5 = List.of("getbaseIngredient", "getAdditionIngredient");
                        List var6 = List.of("base", "addition");
                        int var7 = 0;

                        while (var7 < var5.size()) {
                            Method var8 = var0.getClass().getDeclaredMethod((String)var5.get(var7));
                            if (var8.getReturnType() != Ingredient.class) {
                                return null;
                            }

                            Ingredient var9 = (Ingredient)var8.invoke(var0);
                            if (var9 != null && var9.getClass() == Ingredient.class && !var9.isCustom() && !var9.isEmpty()) {
                                ItemStack[] var10 = var9.getItems();
                                if (var10 != null && var10.length != 0 && var10.length <= 65536) {
                                    CompoundTag var11 = new CompoundTag();
                                    var11.putInt("Index", var7);
                                    var11.putString("Role", (String)var6.get(var7));
                                    var11.putString("State", "NON_EMPTY");
                                    var11.putString("Type", var9.getClass().getName());
                                    var11.put("Definition", FoundationCanonicalDigest.object(RecipeDataBefore433.definition(var9, var1)));
                                    var11.putInt("AlternativeCount", var10.length);
                                    ListTag var12 = new ListTag();

                                    for (int var13 = 0; var13 < var10.length; var13++) {
                                        ItemStack var14 = var10[var13];
                                        if (var14 == null || var14.isEmpty()) {
                                            return null;
                                        }

                                        CompoundTag var15 = RecipeDataBefore433.stack(var14, var1);
                                        FourWeaponFoundationAdapter.canonical(var15);
                                        CompoundTag var16 = new CompoundTag();
                                        var16.putInt("Index", var13);
                                        var16.put("Item", var15);
                                        var12.add(var16);
                                    }

                                    var11.put("Alternatives", var12);
                                    var4.add(var11);
                                    var7++;
                                    continue;
                                }

                                return null;
                            }

                            return null;
                        }

                        var3.put("Ingredients", var4);
                        FourWeaponFoundationAdapter.canonical(var3);
                        return var3;
                    } else {
                        return null;
                    }
                }
            } catch (RuntimeException | LinkageError | ReflectiveOperationException var17) {
                return null;
            }
        } else {
            return null;
        }
    }
}
