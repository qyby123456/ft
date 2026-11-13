package com.farmingtales.drinkanim.smoke;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod("drink_animation_smoke")
public class SmokeFixture {
    private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,"farmingtales");
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,"farmingtales");
    private static final java.util.Map<String,DeferredRegister<net.minecraft.world.level.block.Block>> BLOCK_NAMESPACES=new java.util.LinkedHashMap<>();
    private static final java.util.Map<String,DeferredRegister<Item>> ITEM_NAMESPACES=new java.util.LinkedHashMap<>();
    public static final RegistryObject<Item> WINE=ITEMS.register("orange_wine",()->new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().fast().nutrition(6).saturationMod(1).effect(()->new MobEffectInstance(MobEffects.MOVEMENT_SPEED,600),1).build())) {
        @Override public UseAnim getUseAnimation(ItemStack stack) {return UseAnim.DRINK;}
        @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
            var result=super.finishUsingItem(stack,level,entity);
            if(!level.isClientSide&&entity instanceof Player p)p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            return result;
        }
    });
    public static final java.util.Map<String,RegistryObject<Item>> DRINKS=new java.util.LinkedHashMap<>();
    public static final java.util.Map<String,Integer> AGED_CONSUMPTIONS=new java.util.concurrent.ConcurrentHashMap<>();
    static {
        BLOCK_NAMESPACES.put("farmingtales",BLOCKS);ITEM_NAMESPACES.put("farmingtales",ITEMS);
        DRINKS.put("farmingtales:orange_wine",WINE);
        for(var profile:com.farmingtales.drinkanim.math.DrinkCatalog.all()) {
            if(profile.id().equals("farmingtales:orange_wine"))continue;
            String namespace=profile.id().split(":")[0];
            var blocks=BLOCK_NAMESPACES.computeIfAbsent(namespace,n->DeferredRegister.create(ForgeRegistries.BLOCKS,n));
            var items=ITEM_NAMESPACES.computeIfAbsent(namespace,n->DeferredRegister.create(ForgeRegistries.ITEMS,n));
            var block=blocks.register(profile.id().split(":")[1],()->new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noOcclusion().strength(.3f)));
            DRINKS.put(profile.id(),items.register(profile.id().split(":")[1],()->new net.minecraft.world.item.BlockItem(block.get(),fixtureProperties(profile)) {
                @Override public UseAnim getUseAnimation(ItemStack stack){
                    return presentationOnly(profile)?UseAnim.NONE:sauce(profile)||profile.food()?UseAnim.EAT:UseAnim.DRINK;
                }
                @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
                    var result=super.finishUsingItem(stack,level,entity);
                    if(!presentationOnly(profile)&&(profile.wine()||profile.cup())&&!level.isClientSide&&entity instanceof Player p)p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                    return result;
                }
            }));
        }
        if(Boolean.getBoolean("farmingtales.agedSmoke")) {
            for(var entry:com.farmingtales.drinkanim.math.DrinkCatalog.aged().entrySet()) {
                String id=entry.getKey();var profile=entry.getValue();
                int tier=id.endsWith("_i")?1:id.endsWith("_g")?2:3;
                var food=new FoodProperties.Builder().alwaysEat().nutrition(6*tier).saturationMod(1)
                    .effect(()->new MobEffectInstance(MobEffects.MOVEMENT_SPEED,1200,tier-1),1);
                // The pack's native BrewLevel aging uses 32 ticks; its other aged items use fast food (16).
                if(!profile.id().startsWith("kaleidoscope_")&&!profile.id().startsWith("smc:"))food.fast();
                DRINKS.put(id,ITEMS.register(id.split(":")[1],()->new Item(new Item.Properties().food(food.build())) {
                    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.DRINK;}
                    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
                        var result=super.finishUsingItem(stack,level,entity);
                        if(!level.isClientSide&&entity instanceof Player p) {
                            AGED_CONSUMPTIONS.merge(id,1,Integer::sum);
                            p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                        }
                        return result;
                    }
                }));
            }
        }
    }
    private static boolean presentationOnly(com.farmingtales.drinkanim.math.DrinkProfile profile) {
        return com.farmingtales.drinkanim.math.PresentationOnly.active(profile);
    }
    private static Item.Properties fixtureProperties(com.farmingtales.drinkanim.math.DrinkProfile profile) {
        var properties=new Item.Properties();
        return presentationOnly(profile)?properties:properties.food(food(profile));
    }
    private static boolean sauce(com.farmingtales.drinkanim.math.DrinkProfile profile) {
        return profile.jam()&&!profile.sweetJam();
    }
    private static FoodProperties food(com.farmingtales.drinkanim.math.DrinkProfile profile) {
        var builder=new FoodProperties.Builder().alwaysEat().nutrition(profile.wine()?6:2).saturationMod(.2f);
        // Actual roe/mayonnaise registrations use the normal 32-tick eating duration.
        if(!sauce(profile)&&profile.id().startsWith("farmingtales:"))builder.fast();
        return builder.build();
    }
    public SmokeFixture(){
        var bus=FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_NAMESPACES.values().forEach(r->r.register(bus));ITEM_NAMESPACES.values().forEach(r->r.register(bus));
        bus.addListener(this::colors);
    }
    private void colors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item e) {
        // Same Color NBT and tint index as Tavern's SignatureCocktailColor.Item.
        var signature=DRINKS.get("kaleidoscope_tavern:signature_cocktail");
        if(signature!=null)e.register((stack,index)->index!=1?0xFFFFFF:
            stack.hasTag()&&stack.getTag().contains("Color")?stack.getTag().getInt("Color"):0x5555FF,signature.get());
    }
}
