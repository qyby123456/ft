package net.farmingtales.ftboss.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Curios monocle that reveals food-weapon appraisal text without touching an item's native lore. */
public final class FoodAppraisalMonocleItem extends Item implements ICurioItem {
    public static final String SLOT = "monocle";

    public FoodAppraisalMonocleItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    public static boolean isEquipped(@Nullable LivingEntity entity) {
        if (entity == null) return false;
        try {
            return CuriosApi.getCuriosHelper().findCurios(entity,
                    stack -> stack.is(FTBossItems.FOOD_APPRAISAL_MONOCLE.get())).stream().anyMatch(result ->
                    SLOT.equals(result.slotContext().identifier()) && !result.slotContext().cosmetic());
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return SLOT.equals(context.identifier()) && !context.cosmetic();
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.food_appraisal_monocle.slot")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.food_appraisal_monocle.effect")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.food_appraisal_monocle.clean")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
