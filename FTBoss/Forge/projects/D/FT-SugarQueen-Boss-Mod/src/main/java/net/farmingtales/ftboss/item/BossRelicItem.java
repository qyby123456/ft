package net.farmingtales.ftboss.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class BossRelicItem extends Item implements ICurioItem {
    private final Kind kind;

    public BossRelicItem(Kind kind) {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        this.kind = kind;
    }

    public static ItemStack roll(Item item, RandomSource random) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(BossRelicRules.AFFIX_TAG, random.nextInt(BossRelicRules.AFFIXES.size()));
        return stack;
    }

    public static int affix(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(BossRelicRules.AFFIX_TAG, Tag.TAG_INT)
                ? BossRelicRules.validAffix(stack.getTag().getInt(BossRelicRules.AFFIX_TAG)) : -1;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext context, UUID slotId, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (context.cosmetic() || !BossRelicRules.SLOT.equals(context.identifier())) return modifiers;
        switch (kind) {
            case FROST -> add(modifiers, slotId, Attributes.MAX_HEALTH, 6, Operation.ADDITION, "base");
            case BUTCHER -> add(modifiers, slotId, Attributes.ATTACK_DAMAGE, 3, Operation.ADDITION, "base");
            case TEA -> add(modifiers, slotId, Attributes.ARMOR, 4, Operation.ADDITION, "base");
            case WINE -> add(modifiers, slotId, Attributes.KNOCKBACK_RESISTANCE, 0.3, Operation.ADDITION, "base");
            case RAT -> add(modifiers, slotId, Attributes.MOVEMENT_SPEED, 0.12, Operation.MULTIPLY_BASE, "base");
            case DEFECT -> add(modifiers, slotId, Attributes.ATTACK_SPEED, 0.15, Operation.MULTIPLY_BASE, "base");
            case COMPOST -> add(modifiers, slotId, Attributes.ARMOR_TOUGHNESS, 3, Operation.ADDITION, "base");
            case APPLE -> add(modifiers, slotId, Attributes.LUCK, 2, Operation.ADDITION, "base");
            case BENGER -> {
                add(modifiers, slotId, Attributes.MOVEMENT_SPEED, 0.08, Operation.MULTIPLY_BASE, "base");
                add(modifiers, slotId, Attributes.ATTACK_DAMAGE, 1, Operation.ADDITION, "base");
            }
            case TIDE -> add(modifiers, slotId, ForgeMod.SWIM_SPEED.get(), 0.35, Operation.MULTIPLY_BASE, "base");
        }
        switch (affix(stack)) {
            case 0 -> add(modifiers, slotId, Attributes.MAX_HEALTH, 4, Operation.ADDITION, "affix");
            case 1 -> add(modifiers, slotId, Attributes.ARMOR, 2, Operation.ADDITION, "affix");
            case 2 -> add(modifiers, slotId, Attributes.MOVEMENT_SPEED, 0.05, Operation.MULTIPLY_BASE, "affix");
            case 3 -> add(modifiers, slotId, Attributes.LUCK, 1, Operation.ADDITION, "affix");
            default -> { }
        }
        return modifiers;
    }

    private static void add(Multimap<Attribute, AttributeModifier> modifiers, UUID slotId, Attribute attribute,
            double amount, Operation operation, String part) {
        UUID modifierId = UUID.nameUUIDFromBytes(("ftboss:relic/" + slotId + "/" + part)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        modifiers.put(attribute, new AttributeModifier(modifierId, "ftboss.relic." + part, amount, operation));
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.boss_relic.slot").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.boss_relic." + kind.item).withStyle(ChatFormatting.AQUA));
        int affix = affix(stack);
        tooltip.add(Component.translatable(affix < 0 ? "tooltip.ftboss.boss_relic.unrolled"
                : "tooltip.ftboss.boss_relic.affix." + BossRelicRules.AFFIXES.get(affix)).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.boss_relic.source",
                Component.translatable("entity.ftboss." + kind.entity)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
