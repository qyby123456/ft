package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.client.model.ThornCrownModel;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public final class ThornCrownItem extends ArmorItem {
    public static final int DEFENSE = 4;
    public static final float TOUGHNESS = 3.0F;
    public static final int DURABILITY = 528;
    public static final float REFLECT_RATIO = 0.25F;
    public static final float MIN_REFLECT_DAMAGE = 2.0F;
    public static final float MAX_REFLECT_DAMAGE = 6.0F;
    public static final int REFLECT_COOLDOWN_TICKS = 10;

    public ThornCrownItem() {
        super(Material.INSTANCE, Type.HELMET, new Properties().rarity(Rarity.EPIC));
    }

    public static boolean isWornBy(LivingEntity wearer) {
        return wearer.getItemBySlot(EquipmentSlot.HEAD).is(FTBossItems.THORN_CROWN.get());
    }

    public static int equippedPieces(LivingEntity wearer) {
        return ThornArmorItem.equippedPieces(wearer);
    }

    public static float reflectedDamage(float damage) {
        return reflectedDamage(damage, false);
    }

    /** The Silver Key doubles the final retaliation, including both floor and cap. */
    public static float reflectedDamage(float damage, boolean silverKey) {
        float base = Math.min(MAX_REFLECT_DAMAGE, Math.max(MIN_REFLECT_DAMAGE, damage * REFLECT_RATIO));
        return silverKey ? base * 2.0F : base;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.thorn_crown.thorns",
                Math.round(REFLECT_RATIO * 100), (int) MIN_REFLECT_DAMAGE, (int) MAX_REFLECT_DAMAGE,
                REFLECT_COOLDOWN_TICKS / 20.0F).withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.ftboss.thorn_crown.vision").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.ftboss.thorn_crown.key").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.thorn_crown.repair").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "ftboss:textures/models/armor/thorn_crown.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HumanoidModel<?> model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (slot != EquipmentSlot.HEAD) return original;
                if (model == null) model = new ThornCrownModel(ThornCrownModel.createLayer().bakeRoot());
                return model;
            }
        });
    }

    private enum Material implements ArmorMaterial {
        INSTANCE;

        @Override
        public int getDurabilityForType(Type type) {
            return DURABILITY;
        }

        @Override
        public int getDefenseForType(Type type) {
            return type == Type.HELMET ? DEFENSE : 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 18;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.VINE);
        }

        @Override
        public String getName() {
            return "ftboss:thorn_crown";
        }

        @Override
        public float getToughness() {
            return TOUGHNESS;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.1F;
        }
    }
}
