/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 *  net.minecraftforge.registries.RegisterEvent
 */
package net.farmingtales.ftboss.item;

import java.util.Objects;
import net.farmingtales.ftboss.item.weapons.XueliWeaponItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class XueliWeaponItems {
    public static final ResourceLocation SKILLET = new ResourceLocation("ftboss", "xueli_skillet");
    public static final ResourceLocation ROLLING_PIN = new ResourceLocation("ftboss", "xueli_rolling_pin");

    private XueliWeaponItems() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, SKILLET, () -> new XueliWeaponItem(true));
        event.register(ForgeRegistries.Keys.ITEMS, ROLLING_PIN, () -> new XueliWeaponItem(false));
    }

    public static Item skillet() {
        return Objects.requireNonNull((Item)ForgeRegistries.ITEMS.getValue(SKILLET));
    }

    public static Item rollingPin() {
        return Objects.requireNonNull((Item)ForgeRegistries.ITEMS.getValue(ROLLING_PIN));
    }
}

