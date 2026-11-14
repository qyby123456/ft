/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.FoodAppraisalMonocleItem;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class WeaponAccessoryBondTooltip {
    private WeaponAccessoryBondTooltip() {
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        if (!FoodAppraisalMonocleItem.isEquipped((LivingEntity)itemTooltipEvent.getEntity())) {
            return;
        }
        ItemStack itemStack = itemTooltipEvent.getItemStack();
        if (SwordMaterialExtensions.resolve(itemStack).isEmpty()) {
            return;
        }
        List<Preview> list = WeaponAccessoryBondTooltip.previews(itemStack);
        List list2 = itemTooltipEvent.getToolTip();
        list2.add(Component.m_237119_());
        list2.add(Component.m_237115_((String)"tooltip.ftboss.accessory_bonds.title").m_130940_(ChatFormatting.DARK_AQUA));
        if (list.isEmpty()) {
            list2.add(Component.m_237115_((String)"tooltip.ftboss.accessory_bonds.none").m_130940_(ChatFormatting.GRAY));
            return;
        }
        for (Preview preview : list) {
            MutableComponent mutableComponent = Component.m_237113_((String)" \u2022 ").m_7220_((Component)preview.name().m_6881_());
            if (preview.weapon() != null) {
                Item item = (Item)ForgeRegistries.ITEMS.getValue(preview.weapon());
                Component component = item == null ? Component.m_237113_((String)preview.weapon().toString()) : item.m_41466_();
                mutableComponent.m_7220_((Component)Component.m_237110_((String)"tooltip.ftboss.accessory_bonds.weapon", (Object[])new Object[]{component}));
            }
            list2.add(mutableComponent.m_130940_(ChatFormatting.AQUA));
            if (!Screen.m_96638_()) continue;
            list2.add(preview.description().m_6881_().m_130940_(ChatFormatting.GRAY));
        }
        list2.add(Component.m_237115_((String)"tooltip.ftboss.accessory_bonds.preview").m_130940_(ChatFormatting.DARK_GRAY));
        if (!Screen.m_96638_()) {
            list2.add(Component.m_237115_((String)"tooltip.ftboss.accessory_bonds.shift").m_130940_(ChatFormatting.DARK_GRAY));
        }
    }

    private static List<Preview> previews(ItemStack itemStack) {
        String string;
        ArrayList<Preview> arrayList = new ArrayList<Preview>();
        SwordMaterialDefinition swordMaterialDefinition = SwordMaterialExtensions.resolve(itemStack).orElseThrow();
        for (SwordCavityRules.Link object : SwordCavityRules.participatingLinks(swordMaterialDefinition.baseMaterial())) {
            string = object.name().toLowerCase(Locale.ROOT);
            arrayList.add(new Preview((Component)Component.m_237115_((String)("link.ftboss.sword." + string)), (Component)Component.m_237115_((String)("link.ftboss.sword.detail." + string)), null));
        }
        for (SwordCavityExtensions.BondDefinition bondDefinition : SwordCavityExtensions.participatingBonds(itemStack)) {
            string = Component.m_237119_();
            bondDefinition.materials().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(arg_0 -> WeaponAccessoryBondTooltip.lambda$previews$0((MutableComponent)string, arg_0));
            arrayList.add(new Preview(bondDefinition.name(), (Component)Component.m_237110_((String)"tooltip.ftboss.accessory_bonds.requirements", (Object[])new Object[]{string}).m_7220_((Component)Component.m_237113_((String)"; ")).m_7220_((Component)bondDefinition.description().m_6881_()), bondDefinition.weapon()));
        }
        for (SwordMaterialExtensions.BondDefinition bondDefinition : SwordMaterialExtensions.participatingBonds(itemStack)) {
            arrayList.add(new Preview(bondDefinition.name(), bondDefinition.description(), bondDefinition.weapon()));
        }
        return List.copyOf(arrayList);
    }

    private static /* synthetic */ void lambda$previews$0(MutableComponent mutableComponent, Map.Entry entry) {
        if (!mutableComponent.m_7360_().isEmpty()) {
            mutableComponent.m_7220_((Component)Component.m_237113_((String)" + "));
        }
        mutableComponent.m_7220_(WeaponMaterialCatalog.source((FoodWeaponBuildRules.Material)((Object)entry.getKey()))).m_7220_((Component)Component.m_237113_((String)(" \u00d7" + entry.getValue())));
    }

    private record Preview(Component name, Component description, ResourceLocation weapon) {
    }
}

