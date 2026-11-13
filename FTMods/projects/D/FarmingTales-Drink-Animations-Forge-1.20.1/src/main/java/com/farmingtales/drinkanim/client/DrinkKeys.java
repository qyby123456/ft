package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.DrinkAnimations;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=DrinkAnimations.ID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public final class DrinkKeys {
    public static final KeyMapping DRINK = new KeyMapping("key.farmingtales_drink_anim.drink", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.farmingtales_drink_anim");
    public static final KeyMapping INSPECT = new KeyMapping("key.farmingtales_drink_anim.inspect", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.farmingtales_drink_anim");
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) {
        event.register(DRINK);
        event.register(INSPECT);
    }
    public static KeyMapping drinkKey() {return DRINK;}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event) {
        for(var profile:com.farmingtales.drinkanim.math.DrinkCatalog.all())
            for(String model:com.farmingtales.drinkanim.math.ServingAnimations.models(profile))event.register(BottleRenderer.model(model));
        for(var profile:com.farmingtales.drinkanim.math.DrinkCatalog.all())
            if(com.farmingtales.drinkanim.math.StickyRiceCakeAnimations.owns(profile))
                for(String model:com.farmingtales.drinkanim.math.StickyRiceCakeAnimations.models())event.register(BottleRenderer.model(model));
        for(var profile:com.farmingtales.drinkanim.math.DrinkCatalog.all())
            if(com.farmingtales.drinkanim.math.PlateFoodAnimations.owns(profile))
                for(String model:com.farmingtales.drinkanim.math.PlateFoodAnimations.models(profile))event.register(BottleRenderer.model(model));
        for(var profile:com.farmingtales.drinkanim.math.DrinkCatalog.all()) {
            for(String model:java.util.List.of(profile.bodyModel(),profile.capModel(),profile.neckModel()))
                event.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,model));
            if(profile.can())event.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,profile.canLid().flapModel()));
            for(var part:com.farmingtales.drinkanim.math.DecorationMotion.parts(profile))
                event.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,part.model()));
        }
    }
}
