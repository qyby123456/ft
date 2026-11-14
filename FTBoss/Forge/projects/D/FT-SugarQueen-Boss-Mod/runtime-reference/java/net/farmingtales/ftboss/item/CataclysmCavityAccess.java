/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.util.UUID;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class CataclysmCavityAccess {
    private CataclysmCavityAccess() {
    }

    public static boolean supported(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_() || itemStack.m_41613_() != 1 || WeaponAttachmentPolicy.excluded(itemStack)) {
            return false;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null) {
            return false;
        }
        String string = switch (resourceLocation.toString()) {
            case "cataclysm:the_incinerator" -> "com.github.L_Ender.cataclysm.items.The_Incinerator";
            case "cataclysm:the_annihilator" -> "com.github.L_Ender.cataclysm.items.The_Annihilator";
            case "cataclysm:cursed_bow" -> "com.github.L_Ender.cataclysm.items.Cursed_bow";
            default -> "";
        };
        return !string.isEmpty() && itemStack.m_41720_().getClass().getName().equals(string);
    }

    public static boolean seedFromStatue(ServerPlayer serverPlayer, ItemStack itemStack, BlockPos blockPos) {
        if (serverPlayer == null || blockPos == null || !CataclysmCavityAccess.supported(itemStack) || !serverPlayer.f_8924_.m_18695_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_() || serverPlayer.f_36096_ != serverPlayer.f_36095_ || !serverPlayer.f_36095_.m_142621_().m_41619_() || itemStack != serverPlayer.m_21205_() && itemStack != serverPlayer.m_21206_() || !serverPlayer.m_9236_().m_46805_(blockPos) || !serverPlayer.m_9236_().m_6857_().m_61937_(blockPos) || serverPlayer.m_20275_((double)blockPos.m_123341_() + 0.5, (double)blockPos.m_123342_() + 1.0, (double)blockPos.m_123343_() + 0.5) > 64.0 || !(serverPlayer.m_9236_().m_8055_(blockPos).m_60734_() instanceof CandyStatueBlock)) {
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41783_();
        if (compoundTag != null && compoundTag.m_128441_("FTBossWeaponLifecycle") && !compoundTag.m_128425_("FTBossWeaponLifecycle", 10)) {
            return false;
        }
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return FoundationEntryPoints.seedFromStatue(serverPlayer, itemStack, blockPos, FourWeaponTransactionNetwork.negotiated(serverPlayer));
        }
        if (!SwordCavityBuild.applies(itemStack)) {
            return false;
        }
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossWeaponLifecycle");
        if (compoundTag2 != null && compoundTag2.m_128441_("Weapon")) {
            return compoundTag2.m_128403_("Weapon");
        }
        itemStack.m_41698_("FTBossWeaponLifecycle").m_128362_("Weapon", UUID.randomUUID());
        return true;
    }
}

