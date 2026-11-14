/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
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
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class FoodWeaponMaterialFingerprint {
    private static final int SCHEMA_VERSION = 2;
    private static final int MAX_CELLS = 4096;
    private static final int MAX_NBT_NODES = 16384;
    private static final int MAX_DEPTH = 32;
    private static final int MAX_BYTES = 262144;

    private FoodWeaponMaterialFingerprint() {
    }

    public static String create(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_()) {
            return "";
        }
        try {
            if (FourWeaponFoundationAdapter.applies(itemStack)) {
                CompoundTag compoundTag = FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack);
                if (compoundTag.m_128456_()) {
                    return "";
                }
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.m_128359_("Mode", "four_weapon_foundation");
                for (String string : List.of("ItemId", "Profile", "Family", "Rules", "Composition")) {
                    compoundTag2.m_128359_(string, compoundTag.m_128461_(string));
                }
                compoundTag2.m_128405_("Level", compoundTag.m_128451_("Level"));
                compoundTag2.m_128365_("Cells", (Tag)compoundTag.m_128437_("Cells", 10));
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                FoodWeaponMaterialFingerprint.append(messageDigest, (Tag)compoundTag2, 0, new int[]{0, 0});
                return HexFormat.of().formatHex(messageDigest.digest());
            }
            CompoundTag compoundTag = FoodWeaponMaterialFingerprint.composition(itemStack);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            FoodWeaponMaterialFingerprint.append(messageDigest, (Tag)compoundTag, 0, new int[]{0, 0});
            return HexFormat.of().formatHex(messageDigest.digest());
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return "";
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new IllegalStateException(noSuchAlgorithmException);
        }
    }

    public static CompoundTag foundationSnapshot(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_() || !FourWeaponFoundationAdapter.applies(itemStack)) {
            return new CompoundTag();
        }
        try {
            CompoundTag compoundTag;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            if (itemStack.m_41783_() != null) {
                FoodWeaponMaterialFingerprint.append(messageDigest, (Tag)itemStack.m_41783_(), 0, new int[]{0, 0});
            }
            if (!(compoundTag = FourWeaponFoundationAdapter.compositionSnapshot(itemStack)).m_128471_("Valid") || compoundTag.m_128471_("BlocksUse") || !compoundTag.m_128461_("Rules").matches("[0-9a-f]{64}") || !compoundTag.m_128461_("Composition").matches("[0-9a-f]{64}")) {
                return new CompoundTag();
            }
            String string = SwordCavityExtensions.foundationExtensionsFingerprint(itemStack);
            String string2 = SwordMaterialExtensions.foundationExtensionsFingerprint();
            if (string == null || string2 == null) {
                return new CompoundTag();
            }
            FoodWeaponMaterialFingerprint.append(messageDigest, (Tag)compoundTag, 0, new int[]{0, 0});
            return compoundTag.m_6426_();
        }
        catch (IllegalArgumentException | LinkageError throwable) {
            return new CompoundTag();
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new IllegalStateException(noSuchAlgorithmException);
        }
    }

    private static CompoundTag composition(ItemStack itemStack) {
        CompoundTag compoundTag;
        Object object;
        Object object2;
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128405_("Schema", 2);
        if (!SwordCavityBuild.applies(itemStack)) {
            compoundTag2.m_128359_("Mode", "legacy_parts");
            CompoundTag compoundTag3 = itemStack.m_41737_("FTBossWeaponMaterials");
            for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
                compoundTag2.m_128359_(part.name(), FoodWeaponBuildRules.Material.byId(compoundTag3 == null ? "" : compoundTag3.m_128461_(part.name())).id());
            }
            return compoundTag2;
        }
        compoundTag2.m_128359_("Mode", "logical_cavity");
        SwordCavityRules.Slot[] slotArray = SwordCavityRules.Slot.values();
        if (slotArray.length > 4096) {
            throw new IllegalArgumentException("Logical cavity schema budget exceeded");
        }
        CompoundTag compoundTag4 = itemStack.m_41737_("FTBossSwordStructure");
        if (compoundTag4 != null) {
            try {
                object2 = MessageDigest.getInstance("SHA-256");
            }
            catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                throw new IllegalStateException(noSuchAlgorithmException);
            }
            object = new int[]{0, 0};
            compoundTag = compoundTag4.m_128469_("Cells");
            for (SwordCavityRules.Slot slot : slotArray) {
                FoodWeaponMaterialFingerprint.append((MessageDigest)object2, (Tag)compoundTag.m_128469_(slot.name()).m_128469_("Item").m_128469_("tag"), 0, (int[])object);
            }
        }
        object2 = SwordNativeMaterials.parts(itemStack);
        object = SwordCavityBuild.cells(itemStack, (List<SwordNativeMaterialRules.Piece>)object2);
        if (slotArray.length > 4096 || object.size() != slotArray.length || object2.size() != slotArray.length) {
            throw new IllegalArgumentException("Unbounded or inconsistent logical cavity schema");
        }
        compoundTag2.m_128379_("GridValues", compoundTag4 != null && compoundTag4.m_128471_("GridValues"));
        compoundTag2.m_128379_("LinksEnabled", compoundTag4 != null && compoundTag4.m_128471_("LinksEnabled"));
        compoundTag = new ListTag();
        for (SwordCavityRules.Slot slot : slotArray) {
            int n = slot.ordinal();
            SwordCavityRules.Cell cell = (SwordCavityRules.Cell)object.get(n);
            SwordNativeMaterialRules.Piece piece = (SwordNativeMaterialRules.Piece)object2.get(n);
            CompoundTag compoundTag5 = new CompoundTag();
            compoundTag5.m_128405_("LogicalIndex", n);
            compoundTag5.m_128359_("Slot", slot.name());
            compoundTag5.m_128359_("State", cell.state().name());
            compoundTag5.m_128379_("Present", cell.present());
            compoundTag5.m_128359_("Material", cell.material().id());
            if (cell.state() == SwordCavityRules.State.NATIVE) {
                compoundTag5.m_128359_("ItemId", piece.itemId());
                compoundTag5.m_128405_("Count", piece.count());
                compoundTag5.m_128359_("Origin", piece.origin().name());
                var15_21 = SwordNativeMaterials.display(itemStack, n, (List<SwordNativeMaterialRules.Piece>)object2);
                compoundTag5.m_128379_("ActualItemValid", !var15_21.m_41619_());
                compoundTag5.m_128365_("ItemTag", (Tag)FoodWeaponMaterialFingerprint.semanticTag(var15_21));
            } else if (cell.state() == SwordCavityRules.State.INSTALLED || cell.state() == SwordCavityRules.State.INVALID) {
                var15_21 = SwordCavityBuild.storedItem(itemStack, slot);
                compoundTag5.m_128379_("ActualItemValid", !var15_21.m_41619_());
                if (!var15_21.m_41619_()) {
                    var16_22 = ForgeRegistries.ITEMS.getKey((Object)var15_21.m_41720_());
                    compoundTag5.m_128359_("ItemId", var16_22 == null ? "" : var16_22.toString());
                    compoundTag5.m_128405_("Count", var15_21.m_41613_());
                    compoundTag5.m_128365_("ItemTag", (Tag)FoodWeaponMaterialFingerprint.semanticTag(var15_21));
                } else if (compoundTag4 != null) {
                    var16_22 = compoundTag4.m_128469_("Cells").m_128469_(slot.name());
                    CompoundTag compoundTag6 = var16_22.m_128469_("Item");
                    compoundTag5.m_128379_("StoredItem", var16_22.m_128441_("Item"));
                    compoundTag5.m_128359_("ItemId", compoundTag6.m_128461_("id"));
                    compoundTag5.m_128405_("Count", compoundTag6.m_128451_("Count"));
                    compoundTag5.m_128365_("ItemTag", (Tag)FoodWeaponMaterialFingerprint.semanticTag(compoundTag6.m_128469_("tag")));
                }
            }
            compoundTag.add((Object)compoundTag5);
        }
        compoundTag2.m_128365_("Cells", (Tag)compoundTag);
        return compoundTag2;
    }

    private static CompoundTag semanticTag(ItemStack itemStack) {
        return FoodWeaponMaterialFingerprint.semanticTag(itemStack.m_41783_() == null ? new CompoundTag() : itemStack.m_41783_());
    }

    private static CompoundTag semanticTag(CompoundTag compoundTag) {
        if (compoundTag.m_128440_() > 16384) {
            throw new IllegalArgumentException("Material compound node budget exceeded");
        }
        CompoundTag compoundTag2 = new CompoundTag();
        for (String string : compoundTag.m_128431_()) {
            if (string.equals("Damage")) continue;
            compoundTag2.m_128365_(string, compoundTag.m_128423_(string));
        }
        return compoundTag2;
    }

    private static void bytes(MessageDigest messageDigest, String string, int[] nArray) {
        ByteBuffer byteBuffer;
        if (string.length() > 262144) {
            throw new IllegalArgumentException("Material tag string budget exceeded");
        }
        try {
            byteBuffer = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(string));
        }
        catch (CharacterCodingException characterCodingException) {
            throw new IllegalArgumentException("Invalid material tag Unicode", characterCodingException);
        }
        nArray[1] = nArray[1] + byteBuffer.remaining();
        if (nArray[1] > 262144) {
            throw new IllegalArgumentException("Material tag byte budget exceeded");
        }
        messageDigest.update(byteBuffer);
    }

    private static void append(MessageDigest messageDigest, Tag tag, int n, int[] nArray) {
        if (n > 32 || (nArray[0] = nArray[0] + 1) > 16384) {
            throw new IllegalArgumentException("Material tag complexity exceeded");
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            if (compoundTag.m_128440_() > 16384 - nArray[0]) {
                throw new IllegalArgumentException("Material compound node budget exceeded");
            }
            FoodWeaponMaterialFingerprint.bytes(messageDigest, "compound{" + compoundTag.m_128440_() + ":", nArray);
            for (String string : new TreeSet(compoundTag.m_128431_())) {
                FoodWeaponMaterialFingerprint.bytes(messageDigest, string.length() + ":" + string, nArray);
                FoodWeaponMaterialFingerprint.append(messageDigest, compoundTag.m_128423_(string), n + 1, nArray);
            }
            FoodWeaponMaterialFingerprint.bytes(messageDigest, "}", nArray);
        } else if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            if (listTag.size() > 16384 - nArray[0]) {
                throw new IllegalArgumentException("Material list node budget exceeded");
            }
            FoodWeaponMaterialFingerprint.bytes(messageDigest, "list[" + listTag.size() + ":", nArray);
            for (Tag tag2 : listTag) {
                FoodWeaponMaterialFingerprint.append(messageDigest, tag2, n + 1, nArray);
            }
            FoodWeaponMaterialFingerprint.bytes(messageDigest, "]", nArray);
        } else {
            StringTag stringTag;
            LongArrayTag longArrayTag;
            IntArrayTag intArrayTag;
            ByteArrayTag byteArrayTag;
            int n2 = 262144 - nArray[1];
            if (tag instanceof ByteArrayTag && (byteArrayTag = (ByteArrayTag)tag).m_128227_().length > n2 / 32 || tag instanceof IntArrayTag && (intArrayTag = (IntArrayTag)tag).m_128648_().length > n2 / 32 || tag instanceof LongArrayTag && (longArrayTag = (LongArrayTag)tag).m_128851_().length > n2 / 32 || tag instanceof StringTag && (stringTag = (StringTag)tag).m_7916_().length() > n2 / 2) {
                throw new IllegalArgumentException("Material scalar budget exceeded");
            }
            FoodWeaponMaterialFingerprint.bytes(messageDigest, tag.m_7060_() + ":" + tag, nArray);
        }
    }
}

