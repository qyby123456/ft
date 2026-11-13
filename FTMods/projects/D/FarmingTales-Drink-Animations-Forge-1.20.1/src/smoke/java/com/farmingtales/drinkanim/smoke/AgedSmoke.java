package com.farmingtales.drinkanim.smoke;

import com.farmingtales.drinkanim.client.*;
import com.farmingtales.drinkanim.math.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffects;
import java.util.List;

/** Registered aliases are exercised with real client rendering and vanilla server consumption. */
public final class AgedSmoke {
    private static final List<String> CASES=List.of(
        "farmingtales:orange_wine_i", "farmingtales:tavern_carignan_g", "farmingtales:tavern_brandy_d",
        "farmingtales:world_liquor_dassai_d", "farmingtales:world_liquor_maotai_i",
        "farmingtales:world_liquor_bamboo_leaf_green_liquor_g", "farmingtales:world_liquor_strongbow_d",
        "farmingtales:vinery_chenet_wine_g", "farmingtales:brewery_whiskey_highland_hearth_i", "farmingtales:smc_ice_tea_d");
    private static int index,age,waitTicks,useTicks;
    private static boolean checked,prepared;
    private static CompoundTag originalTag;
    private static String current="registry";

    public static void tick(Minecraft mc) {
        if(!checked) {
            for(var entry:DrinkCatalog.aged().entrySet()) {
                var item=SmokeFixture.DRINKS.get(entry.getKey()).get();
                for(int level:new int[]{0,1,6,12}) {
                    var stack=new ItemStack(item);if(level>0)stack.getOrCreateTag().putInt("BrewLevel",level);
                    require(DrinkController.supports(stack)&&DrinkController.profile(stack)==entry.getValue(),"registered alias "+entry.getKey()+" BrewLevel="+level);
                }
            }
            com.mojang.logging.LogUtils.getLogger().info("AGED_SMOKE_REGISTRY_COMPLETE {}",DrinkCatalog.aged().size());checked=true;
        }
        if(index>=CASES.size()) {
            com.mojang.logging.LogUtils.getLogger().info("AGED_SMOKE_COMPLETE {}",CASES.size());mc.stop();return;
        }
        current=CASES.get(index);var profile=DrinkCatalog.find(current);var item=SmokeFixture.DRINKS.get(current).get();
        if(!prepared) {
            if(waitTicks++==0) {
                mc.setScreen(null);DrinkController.cancel();mc.player.getInventory().selected=8;
                originalTag=new CompoundTag();originalTag.putInt("BrewLevel",6);
                originalTag.putString("AgingSmokeMarker",current);
                originalTag.putUUID("Batch",java.util.UUID.nameUUIDFromBytes(current.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                var initial=new ItemStack(item,3);initial.setTag(originalTag.copy());
                mc.getSingleplayerServer().execute(()->{
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    player.getInventory().clearContent();player.getInventory().setItem(0,initial);
                    player.getFoodData().setFoodLevel(15);player.removeAllEffects();
                });
            }
            var ready=mc.player.getInventory().getItem(0);
            if(ready.is(item)&&ready.getCount()==3&&!DrinkController.active()&&!DrinkController.renderable()) {
                mc.player.getInventory().selected=0;prepared=true;age=0;useTicks=ready.getUseDuration();
            } else if(waitTicks>150)throw new IllegalStateException("Aged fixture failed to settle: "+current);
            return;
        }
        int t=age++,duration=useTicks;
        int drinkStart=30+AnimationPlayback.ticks(profile,BottleMotion.Action.INSPECT,duration)+5;
        int drinkEnd=drinkStart+AnimationPlayback.ticks(profile,BottleMotion.Action.DRINK,duration)+8;
        if(t==20) {
            require(!DrinkController.active()&&DrinkController.renderable()&&DrinkController.profile()==profile,"draw reaches canonical idle");
            require(originalTag.equals(mc.player.getMainHandItem().getTag()),"draw preserves aged ID and NBT");
            var paths=new java.util.ArrayList<String>(List.of(profile.bodyModel(),profile.capModel(),profile.neckModel()));
            for(var part:DecorationMotion.parts(profile))paths.add(part.model());
            boolean complete=true;
            for(String path:paths) {
                var model=mc.getModelManager().getModel(BottleRenderer.model(path));
                complete&=model!=mc.getModelManager().getMissingModel();
                for(var face:java.util.Arrays.asList(null,net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.SOUTH,
                        net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST,net.minecraft.core.Direction.UP,net.minecraft.core.Direction.DOWN))
                    for(var quad:model.getQuads(null,face,net.minecraft.util.RandomSource.create(42)))
                        complete&=!quad.getSprite().contents().name().getPath().equals("missingno");
            }
            require(complete,"canonical bottle cap and ornament resources loaded");shot(mc,"idle");
        }
        if(t==30) {
            DrinkController.start(BottleMotion.Action.INSPECT);
            require(DrinkController.action()==BottleMotion.Action.INSPECT,"inspection starts");
        }
        if(t==60)shot(mc,"inspect");
        if(t==drinkStart) {
            var stack=mc.player.getMainHandItem();
            require(!DrinkController.active()&&stack.is(item)&&stack.getCount()==3&&originalTag.equals(stack.getTag()),"inspection preserves aged item and full NBT");
            DrinkController.start(BottleMotion.Action.DRINK);
            require(DrinkController.action()==BottleMotion.Action.DRINK&&DrinkController.profile()==profile,"drinking starts with canonical animation");
        }
        if(t==drinkStart+42)shot(mc,"drink");
        if(t==drinkEnd) {
            var stack=mc.player.getMainHandItem();
            require(!DrinkController.active()&&stack.is(item)&&stack.getCount()==2&&originalTag.equals(stack.getTag()),"drink consumes one aged item and preserves remaining ID and full NBT");
            require(SmokeFixture.AGED_CONSUMPTIONS.getOrDefault(current,0)==1,"vanilla aged item consumption callback runs once");
            int bottles=mc.player.getInventory().items.stream().filter(s->s.is(Items.GLASS_BOTTLE)).mapToInt(ItemStack::getCount).sum();
            require(bottles==1,"original container return runs once");
            int tier=current.endsWith("_i")?1:current.endsWith("_g")?2:3;
            var effect=mc.player.getEffect(MobEffects.MOVEMENT_SPEED);
            require(effect!=null&&effect.getAmplifier()==tier-1,"original aged item effect is applied");
            shot(mc,"replacement");mc.player.getInventory().selected=8;
        }
        if(t==drinkEnd+18) {
            require(!DrinkController.active()&&!DrinkController.renderable(),"stow finishes with empty hand");
            com.mojang.logging.LogUtils.getLogger().info("AGED_SMOKE_SEQUENCE_COMPLETE {}",current);
            index++;prepared=false;waitTicks=0;
        }
    }
    private static void require(boolean condition,String label) {
        if(!condition)throw new IllegalStateException("AGED_SMOKE FAIL "+current+" "+label);
        com.mojang.logging.LogUtils.getLogger().info("AGED_SMOKE PASS {} {}",current,label);
    }
    private static void shot(Minecraft mc,String stage) {
        Screenshot.grab(mc.gameDirectory,"v1858-aged-"+current.split(":")[1]+"-"+stage+".png",mc.getMainRenderTarget(),message->{});
    }
    private AgedSmoke() {}
}
