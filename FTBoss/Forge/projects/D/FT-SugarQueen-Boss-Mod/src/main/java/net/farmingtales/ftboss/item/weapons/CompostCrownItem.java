package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.item.FTWeaponTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class CompostCrownItem extends SwordItem {
    public static final int POISON_TICKS = 80;

    public CompostCrownItem() {
        super(FTWeaponTier.INSTANCE, 10, -3.2F, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && !target.level().isClientSide && target.isAlive()) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 0), attacker);
        }
        return hit;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftboss.compost_crown.origin").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.compost_crown.poison", POISON_TICKS / 20)
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.ftboss.compost_crown.immunity").withStyle(ChatFormatting.GRAY));
    }
}
