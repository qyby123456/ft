/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class WeaponAttachmentPolicy {
    private WeaponAttachmentPolicy() {
    }

    public static boolean excluded(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return false;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation != null && SwordCompatibilityRules.attachmentsExcluded(resourceLocation.toString());
    }
}

