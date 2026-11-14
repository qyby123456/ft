package net.farmingtales.ftboss.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/** Saya's three body pieces; the existing crown is the helmet of the same set. */
public final class ThornArmorItem extends ArmorItem {
    public static final int SET_SIZE = 4;
    public static final float TOUGHNESS = 3.0F;
    private static final int DURABILITY_MULTIPLIER = 48;

    public ThornArmorItem(Type type) {
        super(Material.INSTANCE, type, new Properties().rarity(Rarity.EPIC));
        if (type == Type.HELMET) throw new IllegalArgumentException("Use ThornCrownItem for the helmet");
    }

    public static boolean isThornPiece(ItemStack stack) {
        return stack.is(FTBossItems.THORN_CROWN.get())
                || stack.is(FTBossItems.THORN_CHESTPLATE.get())
                || stack.is(FTBossItems.THORN_LEGGINGS.get())
                || stack.is(FTBossItems.THORN_BOOTS.get());
    }

    /** Counts functional armor slots only, so cosmetic armor never intensifies the vision penalty. */
    public static int equippedPieces(LivingEntity wearer) {
        int pieces = 0;
        for (ItemStack stack : wearer.getArmorSlots()) if (isThornPiece(stack)) pieces++;
        return Math.min(SET_SIZE, pieces);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.thorn_armor.thorns",
                Math.round(ThornCrownItem.REFLECT_RATIO * 100), (int) ThornCrownItem.MIN_REFLECT_DAMAGE,
                (int) ThornCrownItem.MAX_REFLECT_DAMAGE,
                ThornCrownItem.REFLECT_COOLDOWN_TICKS / 20.0F).withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.ftboss.thorn_armor.vision").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.ftboss.thorn_armor.repair").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return slot == EquipmentSlot.LEGS
                ? "ftboss:textures/models/armor/thorn_armor_layer_2.png"
                : "ftboss:textures/models/armor/thorn_armor_layer_1.png";
    }

    private enum Material implements ArmorMaterial {
        INSTANCE;

        @Override
        public int getDurabilityForType(Type type) {
            return switch (type) {
                case BOOTS -> 13 * DURABILITY_MULTIPLIER;
                case LEGGINGS -> 15 * DURABILITY_MULTIPLIER;
                case CHESTPLATE -> 16 * DURABILITY_MULTIPLIER;
                case HELMET -> 11 * DURABILITY_MULTIPLIER;
            };
        }

        @Override
        public int getDefenseForType(Type type) {
            return switch (type) {
                case BOOTS -> 4;
                case LEGGINGS -> 7;
                case CHESTPLATE -> 9;
                case HELMET -> ThornCrownItem.DEFENSE;
            };
        }

        @Override public int getEnchantmentValue() { return 18; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(Items.VINE); }
        @Override public String getName() { return "ftboss:thorn_armor"; }
        @Override public float getToughness() { return TOUGHNESS; }
        @Override public float getKnockbackResistance() { return 0.1F; }
    }
}
