package com.farmingtales.drinkanim.smoke;

import com.farmingtales.drinkanim.client.DrinkController;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import java.nio.file.*;

/** Regression scenarios exercise the real client lifecycle between distinct item IDs. */
public final class SwitchingSmoke {
    private static int age;
    private static Path result;
    public static boolean tick(Minecraft mc) {
        int t=age++;
        if(t==0) {
            result=Path.of("switch-results-"+mc.getUser().getName()+".txt");
            try {Files.writeString(result,"");}catch(Exception e){throw new RuntimeException(e);}
            mc.setScreen(null);mc.player.getInventory().selected=0;
            mc.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.RIGHT);
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                p.getInventory().clearContent();
                String[] ids={"orange_wine","red_apple_wine","mysticfruit_wine"};
                for(int i=0;i<ids.length;i++)p.getInventory().setItem(i,new ItemStack(SmokeFixture.DRINKS.get("farmingtales:"+ids[i]).get(),3));
            });
        }
        if(t==45)expect(Action.IDLE,"orange_wine","initial drawn bottle");
        if(t==46) {
            mc.player.getInventory().selected=1;
            expect(Action.IDLE,"orange_wine","render before next tick still uses displayed bottle");
        }
        if(t==49)expect(Action.STOW,"orange_wine","switch stows the old item");
        if(t==52)mc.player.getInventory().selected=2;
        if(t==61)expect(Action.DRAW,"mysticfruit_wine","latest selection draws after old bottle is hidden");
        if(t==63)mc.player.getInventory().selected=1;
        if(t==65)expect(Action.DRAW,"mysticfruit_wine","switch during draw retains the drawing model");
        if(t==75)expect(Action.STOW,"mysticfruit_wine","completed draw stows its own model after rapid switch");
        if(t==86)expect(Action.DRAW,"red_apple_wine","queued bottle gets its own draw");
        if(t==100)expect(Action.IDLE,"red_apple_wine","queued draw completes without a model swap");
        if(t==101)DrinkController.start(Action.INSPECT);
        if(t==150)mc.player.getInventory().selected=2;
        if(t==153)expect(Action.STOW,"red_apple_wine","inspection switch stows inspected bottle");
        if(t==166)expect(Action.DRAW,"mysticfruit_wine","inspection cancellation draws latest selection");
        if(t==195)DrinkController.start(Action.DRINK);
        if(t==208)mc.player.getInventory().selected=1;
        if(t==211)expect(Action.STOW,"mysticfruit_wine","drink switch stows the old bottle");
        if(t==224)expect(Action.DRAW,"red_apple_wine","drink cancellation draws selected bottle");
        if(t==258) {
            expect(Action.IDLE,"red_apple_wine","all transitions reach idle");
            boolean counts=true;
            for(int i=0;i<3;i++)counts&=mc.player.getInventory().getItem(i).getCount()==3;
            check(counts,"switching before consumption preserves all stacks");
            com.mojang.logging.LogUtils.getLogger().info("SWITCH_SMOKE_COMPLETE");
        }
        return t>=260;
    }
    private static void expect(Action action,String id,String label) {
        check(DrinkController.action()==action&&DrinkController.profile().id().equals("farmingtales:"+id),
            label+" action="+DrinkController.action()+" rendered="+DrinkController.profile().id());
    }
    private static void check(boolean ok,String message) {
        String line=(ok?"PASS ":"FAIL ")+message;
        com.mojang.logging.LogUtils.getLogger().info("SWITCH_SMOKE {}",line);
        try{Files.writeString(result,line+"\n",StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}
    }
    private SwitchingSmoke() {}
}
