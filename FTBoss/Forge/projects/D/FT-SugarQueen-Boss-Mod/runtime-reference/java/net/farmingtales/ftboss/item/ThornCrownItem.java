/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.model.HumanoidModel
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
import javax.annotation.Nullable;
import net.farmingtales.ftboss.client.model.ThornCrownModel;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.ThornArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
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

public final class ThornCrownItem
extends ArmorItem {
    public static final int DEFENSE = 4;
    public static final float TOUGHNESS = 3.0f;
    public static final int DURABILITY = 528;
    public static final float REFLECT_RATIO = 0.25f;
    public static final float MIN_REFLECT_DAMAGE = 2.0f;
    public static final float MAX_REFLECT_DAMAGE = 6.0f;
    public static final int REFLECT_COOLDOWN_TICKS = 10;

    public ThornCrownItem() {
        super((ArmorMaterial)Material.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties().m_41497_(Rarity.EPIC));
    }

    public static boolean isWornBy(LivingEntity wearer) {
        return wearer.m_6844_(EquipmentSlot.HEAD).m_150930_((Item)FTBossItems.THORN_CROWN.get());
    }

    public static int equippedPieces(LivingEntity wearer) {
        return ThornArmorItem.equippedPieces(wearer);
    }

    public static float reflectedDamage(float damage) {
        return ThornCrownItem.reflectedDamage(damage, false);
    }

    public static float reflectedDamage(float damage, boolean silverKey) {
        float base = Math.min(6.0f, Math.max(2.0f, damage * 0.25f));
        return silverKey ? base * 2.0f : base;
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        tooltip.add((Component)Component.m_237110_((String)"tooltip.ftboss.thorn_crown.thorns", (Object[])new Object[]{Math.round(25.0f), 2, 6, Float.valueOf(0.5f)}).m_130940_(ChatFormatting.DARK_RED));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.thorn_crown.vision").m_130940_(ChatFormatting.RED));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.thorn_crown.key").m_130940_(ChatFormatting.LIGHT_PURPLE));
        tooltip.add((Component)Component.m_237115_((String)"tooltip.ftboss.thorn_crown.repair").m_130940_(ChatFormatting.GRAY));
    }

    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "ftboss:textures/models/armor/thorn_crown.png";
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private HumanoidModel<?> model;

            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (slot != EquipmentSlot.HEAD) {
                    return original;
                }
                if (this.model == null) {
                    this.model = new ThornCrownModel(ThornCrownModel.createLayer().m_171564_());
                }
                return this.model;
            }
        });
    }

    private static enum Material implements ArmorMaterial
    {
        INSTANCE;


        public int m_266425_(ArmorItem.Type type) {
            return 528;
        }

        public int m_7366_(ArmorItem.Type type) {
            return type == ArmorItem.Type.HELMET ? 4 : 0;
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
            return "ftboss:thorn_crown";
        }

        public float m_6651_() {
            return 3.0f;
        }

        public float m_6649_() {
            return 0.1f;
        }
    }
}

