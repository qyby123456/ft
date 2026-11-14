package net.farmingtales.ftboss.item;

import java.util.UUID;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class CataclysmCavityAccess {
    private CataclysmCavityAccess() {}

    public static boolean supported(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getCount() != 1 || WeaponAttachmentPolicy.excluded(stack)) return false;
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (identifier == null) return false;
        String expected = switch (identifier.toString()) {
            case "cataclysm:the_incinerator" -> "com.github.L_Ender.cataclysm.items.The_Incinerator";
            case "cataclysm:the_annihilator" -> "com.github.L_Ender.cataclysm.items.The_Annihilator";
            case "cataclysm:cursed_bow" -> "com.github.L_Ender.cataclysm.items.Cursed_bow";
            default -> "";
        };
        return !expected.isEmpty() && stack.getItem().getClass().getName().equals(expected);
    }

    public static boolean seedFromStatue(ServerPlayer player, ItemStack stack, BlockPos pos) {
        if (player == null || pos == null || !supported(stack) || !player.server.isSameThread()
                || !player.isAlive() || player.isSpectator() || player.isSleeping() || player.isUsingItem()
                || player.containerMenu != player.inventoryMenu || !player.inventoryMenu.getCarried().isEmpty()
                || stack != player.getMainHandItem() && stack != player.getOffhandItem()
                || !player.level().hasChunkAt(pos) || !player.level().getWorldBorder().isWithinBounds(pos)
                || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5) > 64.0
                || !(player.level().getBlockState(pos).getBlock() instanceof CandyStatueBlock)) return false;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(FoodWeaponLifecycle.ROOT) && !tag.contains(FoodWeaponLifecycle.ROOT, Tag.TAG_COMPOUND)) return false;
        if (FourWeaponFoundationAdapter.applies(stack)) {
            return net.farmingtales.ftboss.transaction.FoundationEntryPoints.seedFromStatue(player, stack, pos,
                    net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork.negotiated(player));
        } else if (!SwordCavityBuild.applies(stack)) return false;
        CompoundTag state = stack.getTagElement(FoodWeaponLifecycle.ROOT);
        if (state != null && state.contains("Weapon")) return state.hasUUID("Weapon");
        stack.getOrCreateTagElement(FoodWeaponLifecycle.ROOT).putUUID("Weapon", UUID.randomUUID());
        return true;
    }
}
