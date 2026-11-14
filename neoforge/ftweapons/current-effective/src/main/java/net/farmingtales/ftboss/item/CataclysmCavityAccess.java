package net.farmingtales.ftboss.item;

import java.util.UUID;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class CataclysmCavityAccess {
    private CataclysmCavityAccess() {
    }

    public static boolean supported(ItemStack var0) {
        if (var0 != null && !var0.isEmpty() && var0.getCount() == 1 && !WeaponAttachmentPolicy.excluded(var0)) {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            if (var1 == null) {
                return false;
            } else {
                String var3 = var1.toString();

                String var2 = switch (var3) {
                    case "cataclysm:the_incinerator" -> "com.github.L_Ender.cataclysm.items.The_Incinerator";
                    case "cataclysm:the_annihilator" -> "com.github.L_Ender.cataclysm.items.The_Annihilator";
                    case "cataclysm:cursed_bow" -> "com.github.L_Ender.cataclysm.items.Cursed_bow";
                    default -> "";
                };
                return !var2.isEmpty() && var0.getItem().getClass().getName().equals(var2);
            }
        } else {
            return false;
        }
    }

    public static boolean seedFromStatue(ServerPlayer var0, ItemStack var1, BlockPos var2) {
        if (var0 != null
            && var2 != null
            && supported(var1)
            && var0.server.isSameThread()
            && var0.isAlive()
            && !var0.isSpectator()
            && !var0.isSleeping()
            && !var0.isUsingItem()
            && var0.containerMenu == var0.inventoryMenu
            && var0.inventoryMenu.getCarried().isEmpty()
            && (var1 == var0.getMainHandItem() || var1 == var0.getOffhandItem())
            && var0.level().hasChunkAt(var2)
            && var0.level().getWorldBorder().isWithinBounds(var2)
            && !(var0.distanceToSqr((double)var2.getX() + 0.5, (double)var2.getY() + 1.0, (double)var2.getZ() + 0.5) > 64.0)
            && var0.level().getBlockState(var2).getBlock() instanceof CandyStatueBlock) {
            CompoundTag var3 = NativeLegacyEvolutionItemData.root(var1);
            if (var3 != null && var3.contains("FTBossWeaponLifecycle") && !var3.contains("FTBossWeaponLifecycle", 10)) {
                return false;
            } else if (FourWeaponFoundationAdapter.applies(var1)) {
                return FoundationEntryPoints.seedFromStatue(var0, var1, var2, FourWeaponTransactionNetwork.negotiated(var0));
            } else if (!SwordCavityBuild.applies(var1)) {
                return false;
            } else {
                CompoundTag var4 = NativeLegacyEvolutionItemData.element(var1, "FTBossWeaponLifecycle");
                if (var4 != null && var4.contains("Weapon")) {
                    return var4.hasUUID("Weapon");
                } else {
                    FoodWeaponData.edit(var1, "FTBossWeaponLifecycle", data -> data.putUUID("Weapon", UUID.randomUUID()));
                    return true;
                }
            }
        } else {
            return false;
        }
    }
}
