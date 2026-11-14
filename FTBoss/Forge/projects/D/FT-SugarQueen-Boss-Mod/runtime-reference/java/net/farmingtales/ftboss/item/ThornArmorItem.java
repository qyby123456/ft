/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ArmorItem$Type
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.model.ThornArmorClientExtension;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ThornArmorItem
extends ArmorItem {
    public static final int SET_SIZE = 4;
    public static final float TOUGHNESS = 3.0f;
    private static final int DURABILITY_MULTIPLIER = 48;

    public ThornArmorItem(ArmorItem.Type type) {
        super((ArmorMaterial)Material.INSTANCE, type, new Item.Properties().m_41497_(Rarity.EPIC));
        if (type == ArmorItem.Type.HELMET) {
            throw new IllegalArgumentException("Use ThornCrownItem for the helmet");
        }
    }

    public static boolean isThornPiece(ItemStack itemStack) {
        return itemStack.m_150930_((Item)FTBossItems.THORN_CROWN.get()) || itemStack.m_150930_((Item)FTBossItems.THORN_CHESTPLATE.get()) || itemStack.m_150930_((Item)FTBossItems.THORN_LEGGINGS.get()) || itemStack.m_150930_((Item)FTBossItems.THORN_BOOTS.get());
    }

    public static int equippedPieces(LivingEntity livingEntity) {
        int n = 0;
        for (ItemStack itemStack : livingEntity.m_6168_()) {
            if (!ThornArmorItem.isThornPiece(itemStack)) continue;
            ++n;
        }
        return Math.min(4, n);
    }

    public void m_7373_(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.m_7373_(itemStack, level, list, tooltipFlag);
        list.add((Component)Component.m_237110_((String)"tooltip.ftboss.thorn_armor.thorns", (Object[])new Object[]{Math.round(25.0f), 2, 6, Float.valueOf(0.5f)}).m_130940_(ChatFormatting.DARK_RED));
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.thorn_armor.vision").m_130940_(ChatFormatting.RED));
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.thorn_armor.repair").m_130940_(ChatFormatting.GRAY));
    }

    public String getArmorTexture(ItemStack itemStack, Entity entity, EquipmentSlot equipmentSlot, String string) {
        return "ftboss:textures/models/armor/thorn_armor_3d.png";
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new ThornArmorClientExtension());
    }

    private static enum Material implements ArmorMaterial
    {
        INSTANCE;


        public int m_266425_(ArmorItem.Type type) {
            if (type == ArmorItem.Type.BOOTS) {
                return 624;
            }
            if (type == ArmorItem.Type.LEGGINGS) {
                return 720;
            }
            if (type == ArmorItem.Type.CHESTPLATE) {
                return 768;
            }
            if (type == ArmorItem.Type.HELMET) {
                return 528;
            }
            throw new IncompatibleClassChangeError();
        }

        public int m_7366_(ArmorItem.Type type) {
            if (type == ArmorItem.Type.BOOTS) {
                return 4;
            }
            if (type == ArmorItem.Type.LEGGINGS) {
                return 7;
            }
            if (type == ArmorItem.Type.CHESTPLATE) {
                return 9;
            }
            if (type == ArmorItem.Type.HELMET) {
                return 4;
            }
            throw new IncompatibleClassChangeError();
        }

        public int m_6646_() {
            return 18;
        }

        public SoundEvent m_7344_() {
            return SoundEvents.f_11678_;
        }

        public Ingredient m_6230_() {
            return Ingredient.m_43929_((ItemLike[])new ItemLike[]{Items.f_42029_});
        }

        public String m_6082_() {
            return "ftboss:thorn_armor";
        }

        public float m_6651_() {
            return 3.0f;
        }

        public float m_6649_() {
            return 0.1f;
        }
    }
}

