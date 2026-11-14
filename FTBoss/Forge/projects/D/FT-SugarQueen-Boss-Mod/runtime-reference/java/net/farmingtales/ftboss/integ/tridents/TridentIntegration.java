/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.common.loot.IGlobalLootModifier
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 */
package net.farmingtales.ftboss.integ.tridents;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.integ.tridents.SanguineLifestealGuard;
import net.farmingtales.ftboss.integ.tridents.TridentIconLootModifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TridentIntegration {
    private static final Set<String> DISABLED_ICONS = Set.of("icon_of_storms", "icon_of_evil", "icon_of_depths", "icon_of_stars", "icon_of_blood");
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create((ResourceKey)ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (String)"ftboss");

    private TridentIntegration() {
    }

    public static void register(IEventBus modBus) {
        LOOT_MODIFIERS.register(modBus);
        modBus.addListener(EventPriority.LOWEST, TridentIntegration::filterCreativeContents);
        SanguineLifestealGuard.register();
    }

    public static boolean isDisabledIcon(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_());
        return id != null && id.m_135827_().equals("tridents_n_stuff") && DISABLED_ICONS.contains(id.m_135815_());
    }

    private static void filterCreativeContents(BuildCreativeModeTabContentsEvent event) {
        Iterator entries = event.getEntries().iterator();
        while (entries.hasNext()) {
            if (!TridentIntegration.isDisabledIcon((ItemStack)((Map.Entry)entries.next()).getKey())) continue;
            entries.remove();
        }
    }

    static {
        LOOT_MODIFIERS.register("remove_trident_icons", () -> TridentIconLootModifier.CODEC);
    }
}

