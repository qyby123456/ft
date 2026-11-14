package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.farmingtales.ftboss.util.WeaponBossMaterialRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Boss 掉落的堆叠构筑原料；kind 指定来源及材质，2026-10-12 与佩戴遗物分离以免误献祭。 */
public final class WeaponBossMaterialItem extends Item {
    private final Kind kind;

    public WeaponBossMaterialItem(Kind kind) {
        super(new Properties().stacksTo(64).rarity(Rarity.RARE).fireResistant());
        this.kind = kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.boss_material",
                Component.translatable("material.ftboss.weapon." + WeaponBossMaterialRules.material(kind).id())).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.boss_material.source",
                Component.translatable("entity.ftboss." + kind.entity)).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.boss_material.consume").withStyle(ChatFormatting.GRAY));
    }
}
