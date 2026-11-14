package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.FoodAppraisalMonocleItem;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 单片镜配件羁绊预览；只读真实材料规则，区分可参与和已激活，不改写物品描述或构筑。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class WeaponAccessoryBondTooltip {
    private record Preview(Component name, Component description, ResourceLocation weapon) {}

    private WeaponAccessoryBondTooltip() {}

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (!FoodAppraisalMonocleItem.isEquipped(event.getEntity())) return;
        ItemStack stack = event.getItemStack();
        if (SwordMaterialExtensions.resolve(stack).isEmpty()) return;
        List<Preview> previews = previews(stack);
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.ftboss.accessory_bonds.title").withStyle(ChatFormatting.DARK_AQUA));
        if (previews.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ftboss.accessory_bonds.none").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (Preview preview : previews) {
            MutableComponent line = Component.literal(" • ").append(preview.name().copy());
            if (preview.weapon() != null) {
                var weapon = ForgeRegistries.ITEMS.getValue(preview.weapon());
                Component name = weapon == null ? Component.literal(preview.weapon().toString()) : weapon.getDescription();
                line.append(Component.translatable("tooltip.ftboss.accessory_bonds.weapon", name));
            }
            tooltip.add(line.withStyle(ChatFormatting.AQUA));
            if (Screen.hasShiftDown()) tooltip.add(preview.description().copy().withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.ftboss.accessory_bonds.preview").withStyle(ChatFormatting.DARK_GRAY));
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.ftboss.accessory_bonds.shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static List<Preview> previews(ItemStack stack) {
        List<Preview> result = new ArrayList<>();
        var material = SwordMaterialExtensions.resolve(stack).orElseThrow();
        for (SwordCavityRules.Link link : SwordCavityRules.participatingLinks(material.baseMaterial())) {
            String id = link.name().toLowerCase(Locale.ROOT);
            result.add(new Preview(Component.translatable("link.ftboss.sword." + id),
                    Component.translatable("link.ftboss.sword.detail." + id), null));
        }
        for (SwordCavityExtensions.BondDefinition bond : SwordCavityExtensions.participatingBonds(stack)) {
            MutableComponent requirements = Component.empty();
            bond.materials().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
                if (!requirements.getSiblings().isEmpty()) requirements.append(Component.literal(" + "));
                requirements.append(WeaponMaterialCatalog.source(entry.getKey())).append(Component.literal(" ×" + entry.getValue()));
            });
            result.add(new Preview(bond.name(), Component.translatable("tooltip.ftboss.accessory_bonds.requirements", requirements)
                    .append(Component.literal("; ")).append(bond.description().copy()), bond.weapon()));
        }
        for (SwordMaterialExtensions.BondDefinition bond : SwordMaterialExtensions.participatingBonds(stack)) {
            result.add(new Preview(bond.name(), bond.description(), bond.weapon()));
        }
        return List.copyOf(result);
    }
}
