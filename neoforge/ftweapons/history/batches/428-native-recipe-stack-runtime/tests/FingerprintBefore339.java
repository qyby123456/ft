package net.farmingtales.ftboss.item;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.TreeSet;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class FingerprintBefore339 {
    private static final int SCHEMA_VERSION = 2;
    private static final int MAX_CELLS = 4286;
    private static final int MAX_NBT_NODES = 16428;
    private static final int MAX_DEPTH = 32;
    private static final int MAX_BYTES = 262144;

    private FingerprintBefore339() {
    }

    public static String create(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            try {
                if (!FourWeaponFoundationAdapter.applies(var0)) {
                    CompoundTag var7 = composition(var0);
                    MessageDigest var8 = MessageDigest.getInstance("SHA-256");
                    append(var8, var7, 0, new int[]{0, 0});
                    return HexFormat.of().formatHex(var8.digest());
                } else {
                    CompoundTag var1 = foundationSnapshot(var0);
                    if (var1.isEmpty()) {
                        return "";
                    } else {
                        CompoundTag var2 = new CompoundTag();
                        var2.putString("Mode", "four_weapon_foundation");

                        for (String var4 : List.of("ItemId", "Profile", "Family", "Rules", "Composition")) {
                            var2.putString(var4, var1.getString(var4));
                        }

                        var2.putInt("Level", var1.getInt("Level"));
                        var2.put("Cells", var1.getList("Cells", 10));
                        MessageDigest var9 = MessageDigest.getInstance("SHA-256");
                        append(var9, var2, 0, new int[]{0, 0});
                        return HexFormat.of().formatHex(var9.digest());
                    }
                }
            } catch (IllegalArgumentException var5) {
                return "";
            } catch (NoSuchAlgorithmException var6) {
                throw new IllegalStateException(var6);
            }
        } else {
            return "";
        }
    }

    public static CompoundTag foundationSnapshot(ItemStack var0) {
        if (var0 != null && !var0.isEmpty() && FourWeaponFoundationAdapter.applies(var0)) {
            try {
                MessageDigest var1 = MessageDigest.getInstance("SHA-256");
                CompoundTag components = NativeFoundationItemData.image(var0).getCompound("components");
                if (!components.isEmpty()) {
                    append(var1, components, 0, new int[]{0, 0});
                }

                CompoundTag var2 = FourWeaponFoundationAdapter.compositionSnapshot(var0);
                if (var2.getBoolean("Valid")
                    && !var2.getBoolean("BlocksUse")
                    && var2.getString("Rules").matches("[0-9a-f]{64}")
                    && var2.getString("Composition").matches("[0-9a-f]{64}")) {
                    String var3 = SwordCavityExtensions.foundationExtensionsFingerprint(var0);
                    String var4 = SwordMaterialExtensions.foundationExtensionsFingerprint();
                    if (var3 != null && var4 != null) {
                        append(var1, var2, 0, new int[]{0, 0});
                        return var2.copy();
                    } else {
                        return new CompoundTag();
                    }
                } else {
                    return new CompoundTag();
                }
            } catch (LinkageError | IllegalArgumentException var5) {
                return new CompoundTag();
            } catch (NoSuchAlgorithmException var6) {
                throw new IllegalStateException(var6);
            }
        } else {
            return new CompoundTag();
        }
    }

    private static CompoundTag composition(ItemStack var0) {
        CompoundTag var1 = new CompoundTag();
        var1.putInt("Schema", 2);
        var1.putString("Encoding", "NEOFORGE_1_21_1_MATERIAL_COMPONENTS_V1");
        if (!SwordCavityBuild.applies(var0)) {
            var1.putString("Mode", "legacy_parts");
            CompoundTag var19 = NativeFoundationItemData.element(var0, "FTBossWeaponMaterials");

            for (Part var26 : Part.values()) {
                var1.putString(var26.name(), Material.byId(var19 == null ? "" : var19.getString(var26.name())).id());
            }

            return var1;
        } else {
            var1.putString("Mode", "logical_cavity");
            Slot[] var2 = Slot.values();
            if (var2.length > 4286) {
                throw new IllegalArgumentException("Logical cavity schema budget exceeded");
            } else {
                CompoundTag var3 = NativeFoundationItemData.element(var0, "FTBossSwordStructure");
                if (var3 != null) {
                    MessageDigest var4;
                    try {
                        var4 = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException var18) {
                        throw new IllegalStateException(var18);
                    }

                    int[] var5 = new int[]{0, 0};
                    CompoundTag var6 = var3.getCompound("Cells");

                    for (Slot var10 : var2) {
                        append(var4, var6.getCompound(var10.name()).getCompound("Item").getCompound("tag"), 0, var5);
                    }
                }

                List var21 = SwordNativeMaterials.parts(var0);
                List var23 = SwordCavityBuild.cells(var0, var21);
                if (var2.length <= 4286 && var23.size() == var2.length && var21.size() == var2.length) {
                    var1.putBoolean("GridValues", var3 != null && var3.getBoolean("GridValues"));
                    var1.putBoolean("LinksEnabled", var3 != null && var3.getBoolean("LinksEnabled"));
                    ListTag var25 = new ListTag();

                    for (Slot var30 : var2) {
                        int var11 = var30.ordinal();
                        Cell var12 = (Cell)var23.get(var11);
                        Piece var13 = (Piece)var21.get(var11);
                        CompoundTag var14 = new CompoundTag();
                        var14.putInt("LogicalIndex", var11);
                        var14.putString("Slot", var30.name());
                        var14.putString("State", var12.state().name());
                        var14.putBoolean("Present", var12.present());
                        var14.putString("Material", var12.material().id());
                        if (var12.state() == State.NATIVE) {
                            var14.putString("ItemId", var13.itemId());
                            var14.putInt("Count", var13.count());
                            var14.putString("Origin", var13.origin().name());
                            ItemStack var15 = SwordNativeMaterials.display(var0, var11, var21);
                            var14.putBoolean("ActualItemValid", !var15.isEmpty());
                            var14.put("ItemTag", semanticTag(var15));
                        } else if (var12.state() == State.INSTALLED || var12.state() == State.INVALID) {
                            ItemStack var31 = SwordCavityBuild.storedItem(var0, var30);
                            var14.putBoolean("ActualItemValid", !var31.isEmpty());
                            if (!var31.isEmpty()) {
                                ResourceLocation var16 = BuiltInRegistries.ITEM.getKey(var31.getItem());
                                var14.putString("ItemId", var16 == null ? "" : var16.toString());
                                var14.putInt("Count", var31.getCount());
                                var14.put("ItemTag", semanticTag(var31));
                            } else if (var3 != null) {
                                CompoundTag var32 = var3.getCompound("Cells").getCompound(var30.name());
                                CompoundTag var17 = var32.getCompound("Item");
                                var14.putBoolean("StoredItem", var32.contains("Item"));
                                var14.putString("ItemId", var17.getString("id"));
                                var14.putInt("Count", var17.getInt("count"));
                                var14.put("ItemTag", semanticTag(var17.getCompound("components")));
                            }
                        }

                        var25.add(var14);
                    }

                    var1.put("Cells", var25);
                    return var1;
                } else {
                    throw new IllegalArgumentException("Unbounded or inconsistent logical cavity schema");
                }
            }
        }
    }

    private static CompoundTag semanticTag(ItemStack var0) {
        return semanticTag(NativeFoundationItemData.image(var0).getCompound("components"));
    }

    private static CompoundTag semanticTag(CompoundTag var0) {
        return NativeFoundationItemData.materialComponents(var0);
    }

    private static void bytes(MessageDigest var0, String var1, int[] var2) {
        if (var1.length() > 262144) {
            throw new IllegalArgumentException("Material tag string budget exceeded");
        } else {
            ByteBuffer var3;
            try {
                var3 = StandardCharsets.UTF_8
                    .newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(var1));
            } catch (CharacterCodingException var5) {
                throw new IllegalArgumentException("Invalid material tag Unicode", var5);
            }

            var2[1] += var3.remaining();
            if (var2[1] > 262144) {
                throw new IllegalArgumentException("Material tag byte budget exceeded");
            } else {
                var0.update(var3);
            }
        }
    }

    private static void append(MessageDigest var0, Tag var1, int var2, int[] var3) {
        if (var2 <= 32 && ++var3[0] <= 16428) {
            if (var1 instanceof CompoundTag var4) {
                if (var4.size() > 16428 - var3[0]) {
                    throw new IllegalArgumentException("Material compound node budget exceeded");
                }

                bytes(var0, "compound{" + var4.size() + ":", var3);

                for (String var7 : new TreeSet<>(var4.getAllKeys())) {
                    bytes(var0, var7.length() + ":" + var7, var3);
                    append(var0, var4.get(var7), var2 + 1, var3);
                }

                bytes(var0, "}", var3);
            } else if (var1 instanceof ListTag var5) {
                if (var5.size() > 16428 - var3[0]) {
                    throw new IllegalArgumentException("Material list node budget exceeded");
                }

                bytes(var0, "list[" + var5.size() + ":", var3);

                for (Tag var13 : var5) {
                    append(var0, var13, var2 + 1, var3);
                }

                bytes(var0, "]", var3);
            } else {
                int var12 = 262144 - var3[1];
                if (var1 instanceof ByteArrayTag var14 && var14.getAsByteArray().length > var12 / 32
                    || var1 instanceof IntArrayTag var8 && var8.getAsIntArray().length > var12 / 32
                    || var1 instanceof LongArrayTag var9 && var9.getAsLongArray().length > var12 / 32
                    || var1 instanceof StringTag var10 && var10.getAsString().length() > var12 / 2) {
                    throw new IllegalArgumentException("Material scalar budget exceeded");
                }

                bytes(var0, var1.getId() + ":" + var1, var3);
            }
        } else {
            throw new IllegalArgumentException("Material tag complexity exceeded");
        }
    }
}
