package net.farmingtales.ftboss.item;

import java.util.List;
import javax.annotation.Nullable;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Terminal-difficulty Saya trophy: seals fleshsight and turns it into a parasitic bloom aura. */
public final class SilverKeyItem extends Item implements ICurioItem {
    public static final int BLOOM_REFRESH_TICKS = 60;

    public SilverKeyItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public static boolean isEquipped(LivingEntity entity) {
        try {
            return CuriosApi.getCuriosHelper().findCurios(entity,
                    stack -> stack.is(FTBossItems.SILVER_KEY.get())).stream().anyMatch(result ->
                    BossRelicRules.SLOT.equals(result.slotContext().identifier())
                            && !result.slotContext().cosmetic());
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        LivingEntity wearer = context.entity();
        if (context.cosmetic() || !BossRelicRules.SLOT.equals(context.identifier())
                || wearer.level().isClientSide || wearer.tickCount % 20 != 0) return;
        MobEffectInstance bloom = wearer.getEffect(FTEffects.BLOOM.get());
        if (bloom == null || bloom.getDuration() <= 30) {
            wearer.addEffect(new MobEffectInstance(FTEffects.BLOOM.get(), BLOOM_REFRESH_TICKS,
                    0, true, false, true));
        }
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (!context.entity().level().isClientSide) context.entity().removeEffect(FTEffects.BLOOM.get());
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return BossRelicRules.SLOT.equals(context.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.silver_key.slot").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.silver_key.vision").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.silver_key.thorns").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.ftboss.silver_key.bloom").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.ftboss.silver_key.source").withStyle(ChatFormatting.DARK_GRAY));
    }
}
