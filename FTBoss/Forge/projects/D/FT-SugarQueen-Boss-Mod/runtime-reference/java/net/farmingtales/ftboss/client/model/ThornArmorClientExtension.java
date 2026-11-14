/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.client.model.ThornArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ThornArmorClientExtension
implements IClientItemExtensions {
    private HumanoidModel<?> chestModel;
    private HumanoidModel<?> leggingsModel;
    private HumanoidModel<?> bootsModel;

    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> humanoidModel) {
        if (equipmentSlot == EquipmentSlot.CHEST) {
            if (this.chestModel == null) {
                this.chestModel = ThornArmorClientExtension.bake(EquipmentSlot.CHEST);
            }
            return this.chestModel;
        }
        if (equipmentSlot == EquipmentSlot.LEGS) {
            if (this.leggingsModel == null) {
                this.leggingsModel = ThornArmorClientExtension.bake(EquipmentSlot.LEGS);
            }
            return this.leggingsModel;
        }
        if (equipmentSlot == EquipmentSlot.FEET) {
            if (this.bootsModel == null) {
                this.bootsModel = ThornArmorClientExtension.bake(EquipmentSlot.FEET);
            }
            return this.bootsModel;
        }
        return humanoidModel;
    }

    private static ThornArmorModel bake(EquipmentSlot equipmentSlot) {
        return new ThornArmorModel(ThornArmorModel.createLayer(equipmentSlot).m_171564_());
    }
}

