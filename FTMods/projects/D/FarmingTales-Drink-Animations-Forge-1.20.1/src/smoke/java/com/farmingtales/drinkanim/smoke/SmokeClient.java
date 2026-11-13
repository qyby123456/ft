package com.farmingtales.drinkanim.smoke;

import com.farmingtales.drinkanim.client.DrinkController;
import com.farmingtales.drinkanim.client.DrinkKeys;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid="drink_animation_smoke",value=Dist.CLIENT)
public class SmokeClient {
    private static boolean started;
    private static int tick;
    private static String screenshot;
    private static String sequenceScreenshot;
    private static int boot;
    private static boolean switchesDone;
    private static volatile int hotkeyVanillaSounds,hotkeyCustomSounds,ordinaryUseSounds;
    private static final java.util.List<Integer> sipSoundTicks=new java.util.concurrent.CopyOnWriteArrayList<>();
    private static volatile boolean restoredSoundResources=true,playerSoundCategory=true,embeddedInspectionSounds=true;
    private static final java.util.Map<String,java.util.List<Integer>> inspectionSoundTicks=new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile com.mojang.blaze3d.audio.Channel cancellableChannel;
    private static volatile java.util.concurrent.Executor audioExecutor;
    private static volatile boolean sipPlayingBeforeCancel;
    private static boolean replacementContinuity=true;
    private static boolean sprintObservedDuringUse;
    private static final java.util.List<String> sprintWindow=new java.util.ArrayList<>();
    private static org.joml.Vector3f lastReplacementPosition;
    private static float replacementMaxStep;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        if(!started&&mc.getOverlay()==null&&++boot>30&&mc.player==null) {
            LogUtils.getLogger().info("DRINK_SMOKE bootstrap screen {}", mc.screen);
            GLFW.glfwHideWindow(mc.getWindow().getWindow());
            mc.options.onboardAccessibility=false;
            mc.setScreen(new TitleScreen());
        }
        if(!started&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null) {
            GLFW.glfwHideWindow(mc.getWindow().getWindow());
            started=true;mc.options.pauseOnLostFocus=false;
            mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(60);
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            mc.createWorldOpenFlows().createFreshLevel("drink-test-"+System.currentTimeMillis(),
                new LevelSettings("Animation test",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                new WorldOptions(41892,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
        }
        if(mc.player==null||mc.getSingleplayerServer()==null)return;
        if(mc.mouseHandler.isMouseGrabbed())mc.mouseHandler.releaseMouse();
        tick++;
        if(tick>65&&Boolean.getBoolean("farmingtales.agedSmoke")) {
            AgedSmoke.tick(mc);return;
        }
        if(tick>65&&Boolean.getBoolean("farmingtales.catalogOnlySmoke")) {
            CatalogSmoke.tick(mc);return;
        }
        if(tick>850&&Boolean.getBoolean("farmingtales.catalogSmoke")) {
            if(!switchesDone)switchesDone=SwitchingSmoke.tick(mc);
            else CatalogSmoke.tick(mc);
            return;
        }
        if(tick==5) {
            try {
                Files.writeString(Path.of("sound-cues.jsonl"),"");
                Files.writeString(Path.of("smoke-results.txt"),"");
            }catch(Exception e){throw new RuntimeException(e);}
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(SmokeFixture.WINE.get(),3));
                p.teleportTo(0,-60,0);p.serverLevel().setDayTime(6000);
                p.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,mc.getSingleplayerServer());
            });
            mc.player.setYRot(180);mc.player.setXRot(0);mc.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.RIGHT);
            net.minecraft.client.KeyMapping.releaseAll();
        }
        if(tick==40) {mc.setScreen(null);mc.player.getInventory().selected=0;}
        if(tick==41) {
            String expected=net.minecraft.client.resources.DefaultPlayerSkin.getSkinModelName(mc.player.getUUID());
            record((mc.player.getModelName().equals(expected)?"PASS ":"FAIL ")+"player arm renderer model="+mc.player.getModelName());
            if(Boolean.getBoolean("farmingtales.grillingSmoke")) {
                boolean loaded=java.util.List.of("kaleidoscope_grilling","kaleidoscope_cookery","food","geckolib").stream().allMatch(net.minecraftforge.fml.ModList.get()::isLoaded);
                record((loaded?"PASS ":"FAIL ")+"installed Grilling Cookery ImmersiveEating and GeckoLib load together and reach a world");
            }
        }
        if(tick==65)screenshot="01-idle.png";
        if(tick==68)mc.options.keyUp.setDown(true);
        if(tick==70) {
            mc.options.keyUse.setDown(true);
            var result=mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);
            record((result.consumesAction()&&mc.player.isUsingItem()&&DrinkController.vanillaUseActive()&&!DrinkController.active()?"PASS ":"FAIL ")+"ordinary right click keeps vanilla use animation");
        }
        if(tick==71)checkInput("ordinary right click retains vanilla movement",.2f,0,false);
        if(tick==72) {
            mc.options.keyUse.setDown(false);mc.options.keyUp.setDown(false);
            mc.gameMode.releaseUsingItem(mc.player);
        }
        if(tick==80)press(GLFW.GLFW_KEY_G);
        if(tick==114)screenshot="02-inspect.png";
        if(tick==115) {
            double camera=com.farmingtales.drinkanim.math.BottleMotion.camera(DrinkController.action(),DrinkController.age(1),DrinkController.duration()).magnitude();
            record((camera>.5&&camera<1.8?"PASS ":"FAIL ")+"restrained inspection camera drives world and viewmodel magnitude="+camera);
        }
        if(tick==150)screenshot="10-inspect-tilt.png";
        if(tick==195)check("inspect keeps inventory",3,0,false);
        if(tick==205)press(GLFW.GLFW_KEY_V);
        if(tick==223)screenshot="09-opening.png";
        if(tick==221) {
            double reveal=com.farmingtales.drinkanim.math.BottleMotion.mouthReveal(DrinkController.action(),DrinkController.age(1),DrinkController.duration(),DrinkController.replacementAvailable());
            record((reveal>0&&reveal<1?"PASS ":"FAIL ")+"glass reveals progressively before cork release fraction="+reveal);
        }
        if(tick==228) {
            double age=DrinkController.age(1);
            boolean visible=age>=com.farmingtales.drinkanim.math.BottleMotion.CORK_RELEASE&&age<41
                &&com.farmingtales.drinkanim.math.BottleMotion.corkVisible(DrinkController.action(),age,DrinkController.duration(),DrinkController.replacementAvailable())
                &&com.farmingtales.drinkanim.math.BottleMotion.mouthOpen(DrinkController.action(),age,DrinkController.duration(),DrinkController.replacementAvailable());
            record((visible?"PASS ":"FAIL ")+"recolored mouth appears while pulled cork remains visible age="+age);
        }
        if(tick==236)screenshot="13-open-glass-mouth.png";
        if(tick==247)screenshot="14-open-mouth-clear.png";
        if(tick==245)screenshot="03-raise.png";
        if(tick==270)screenshot="04-drink.png";
        if(tick==268) {mc.options.keyUp.setDown(true);mc.options.keyLeft.setDown(true);}
        if(tick==273)checkInput("hotkey drink keeps full forward and strafe speed",1,1,true);
        if(tick==274)mc.options.keyShift.setDown(true);
        if(tick==276)checkInput("hotkey drink preserves intentional crouch speed",.3f,.3f,true);
        if(tick==277) {
            mc.options.keyShift.setDown(false);mc.options.keyLeft.setDown(false);
            mc.options.keySprint.setDown(true);
        }
        if(tick>=278&&tick<=281) {
            boolean using=mc.player.isUsingItem()&&DrinkController.shouldHoldUse();
            sprintObservedDuringUse|=using&&mc.player.isSprinting()&&mc.player.input.forwardImpulse>.99f;
            sprintWindow.add("tick="+tick+" age="+DrinkController.age(1)+" using="+using+" sprint="+mc.player.isSprinting()+" forward="+mc.player.input.forwardImpulse);
        }
        if(tick==281)record((sprintObservedDuringUse?"PASS ":"FAIL ")+"sprint can start during hotkey drink "+sprintWindow);
        if(tick==284) {mc.options.keyUp.setDown(false);mc.options.keySprint.setDown(false);}
        if(tick==315)check("drink consumes once and gives container and effect",2,1,true);
        if(tick==315)screenshot="15-empty-bottle-hidden.png";
        if(tick==322) {
            screenshot="16-new-bottle-draw.png";
            double age=DrinkController.age(1);
            var pose=com.farmingtales.drinkanim.math.BottleMotion.sample(DrinkController.action(),age,DrinkController.duration(),DrinkController.replacementAvailable());
            var rig=com.farmingtales.drinkanim.math.BottleRig.solve(pose,DrinkController.action(),age,DrinkController.duration(),1);
            var seated=com.farmingtales.drinkanim.math.BottleRig.solve(pose,com.farmingtales.drinkanim.math.BottleMotion.Action.IDLE,0,16,1);
            boolean visible=com.farmingtales.drinkanim.math.BottleMotion.corkVisible(DrinkController.action(),age,DrinkController.duration(),DrinkController.replacementAvailable());
            record((visible&&rig.cork().position().distance(seated.cork().position())<1e-6?"PASS ":"FAIL ")+"replacement neck is visible and attached to new bottle age="+age);
        }
        if(tick==370)mc.options.keyUp.setDown(true);
        if(tick>=323&&tick<=345) {
            var action=DrinkController.action();double age=DrinkController.age(1);
            var pose=com.farmingtales.drinkanim.math.BottleMotion.sample(action,age,DrinkController.duration(),DrinkController.replacementAvailable());
            var rig=com.farmingtales.drinkanim.math.BottleRig.solve(pose,action,age,DrinkController.duration(),1);
            var position=new org.joml.Vector3f(rig.bottle().position()).add(com.farmingtales.drinkanim.math.BottleFraming.offset(rig,action,age,DrinkController.duration(),DrinkController.replacementAvailable()));
            if(lastReplacementPosition!=null)replacementMaxStep=Math.max(replacementMaxStep,position.distance(lastReplacementPosition));
            lastReplacementPosition=position;
            replacementContinuity&=DrinkController.renderable()&&(action==com.farmingtales.drinkanim.math.BottleMotion.Action.DRINK||action==com.farmingtales.drinkanim.math.BottleMotion.Action.IDLE);
            sequenceScreenshot=String.format("replacement-continuity-%03d.png",tick-323);
        }
        if(tick==365) {
            record((replacementContinuity&&!DrinkController.active()&&replacementMaxStep<.02?"PASS ":"FAIL ")+"replacement reaches idle without a second draw or dip maxStep="+replacementMaxStep);
            record((hotkeyCustomSounds>=5&&hotkeyVanillaSounds==0?"PASS ":"FAIL ")+"hotkey sounds follow animation without duplicate vanilla playback cues="+hotkeyCustomSounds+" duplicates="+hotkeyVanillaSounds);
            record((restoredSoundResources&&playerSoundCategory?"PASS ":"FAIL ")+"drinking uses embedded vanilla sample and player volume category");
            boolean timing=sipSoundTicks.size()==4;
            if(timing)for(int i=0;i<4;i++)timing&=sipSoundTicks.get(i)>=243+i*10&&sipSoundTicks.get(i)<=245+i*10;
            record((timing?"PASS ":"FAIL ")+"four complete sip sounds after bottle raise and before lowering ticks="+sipSoundTicks);
            boolean inspection=embeddedInspectionSounds&&inspectionSoundTicks.values().stream().mapToInt(java.util.List::size).sum()==15;
            for(var cue:com.farmingtales.drinkanim.math.AnimationSoundTimeline.inspection())
                inspection&=inspectionSoundTicks.getOrDefault(cue.event(),java.util.List.of()).stream()
                    .anyMatch(t->{
                        double before=com.farmingtales.drinkanim.math.AnimationPlayback.authored(com.farmingtales.drinkanim.math.DrinkCatalog.ORANGE,com.farmingtales.drinkanim.math.BottleMotion.Action.INSPECT,Math.max(0,t-81),16);
                        double after=com.farmingtales.drinkanim.math.AnimationPlayback.authored(com.farmingtales.drinkanim.math.DrinkCatalog.ORANGE,com.farmingtales.drinkanim.math.BottleMotion.Action.INSPECT,t-79,16);
                        return before<=cue.tick()&&after>=cue.tick();
                    });
            record((inspection?"PASS ":"FAIL ")+"embedded inspection foley follows support four turns settling and return cues="+inspectionSoundTicks);
        }
        if(tick==389) {screenshot="05-walk.png";checkMotion("walking",.001);}
        if(tick==390)mc.options.keyJump.setDown(true);
        if(tick==392)mc.options.keyJump.setDown(false);
        if(tick==396) {screenshot="06-jump.png";checkMotion("jump",.015);}
        if(tick==411) {screenshot="07-land.png";checkMotion("landing",.005);}
        if(tick==415)mc.options.keyUp.setDown(false);
        if(tick==425) {mc.options.keyUp.setDown(true);mc.options.keySprint.setDown(true);}
        if(tick==450) {
            screenshot="11-sprint.png";
            String result=(mc.player.isSprinting()&&DrinkController.MOVEMENT.run(1)>.7?"PASS ":"FAIL ")+"sprint movement blends in";
            record(result);
        }
        if(tick==474) {mc.options.keyUp.setDown(false);mc.options.keySprint.setDown(false);}
        if(tick==505)screenshot="12-after-sprint.png";
        if(tick==520)press(GLFW.GLFW_KEY_V);
        if(tick==592&&audioExecutor!=null)audioExecutor.execute(()->sipPlayingBeforeCancel=cancellableChannel.playing());
        if(tick==593)DrinkController.cancel();
        if(tick==594) {
            if(audioExecutor==null)record("FAIL cancel sound channel was not captured");
            else audioExecutor.execute(()->record((sipPlayingBeforeCancel&&!cancellableChannel.playing()?"PASS ":"FAIL ")+"cancel immediately stops the playing audio channel before the sample ends"));
        }
        if(tick==598) {
            mc.options.keyUp.setDown(true);mc.options.keyUse.setDown(true);
            mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);
        }
        if(tick==600)checkInput("cancel clears movement exception before ordinary use",.2f,0,false);
        if(tick==611) {
            mc.options.keyUse.setDown(false);mc.options.keyUp.setDown(false);
            mc.gameMode.releaseUsingItem(mc.player);
        }
        if(tick==612)record((ordinaryUseSounds>0?"PASS ":"FAIL ")+"ordinary right click still plays vanilla use sound count="+ordinaryUseSounds);
        if(tick==640)check("cancel during sip does not consume",2,1,true);
        if(tick==650)press(GLFW.GLFW_KEY_V);
        if(tick==726)mc.player.getInventory().selected=1;
        if(tick==765)check("switch slot cancels without consumption",2,1,true);
        if(tick==775) {
            mc.player.getInventory().selected=0;mc.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.LEFT);
        }
        if(tick==778)record((DrinkController.action()==com.farmingtales.drinkanim.math.BottleMotion.Action.DRAW?"PASS ":"FAIL ")+"draw transition is not a zero-frame swap");
        if(tick==784)screenshot="17-slim-draw.png";
        if(tick==805)screenshot="08-left-hand.png";
        if(tick==810)mc.player.getInventory().selected=1;
        if(tick==812)record((DrinkController.action()==com.farmingtales.drinkanim.math.BottleMotion.Action.STOW?"PASS ":"FAIL ")+"stow transition is not a zero-frame swap");
        if(tick==817)screenshot="18-slim-stow.png";
        if(tick==835)record((!DrinkController.renderable()?"PASS ":"FAIL ")+"stow finishes with bottle and arm fully hidden");
        if(tick>=80&&tick<=80+com.farmingtales.drinkanim.math.BottleMotion.INSPECT_TICKS&&tick%5==0)sequenceScreenshot=String.format("inspect-sequence-%03d.png",tick-80);
        if(tick>=205&&tick<=365&&tick%5==0)sequenceScreenshot=String.format("drink-sequence-%03d.png",tick-205);
        if(tick==850) {
            try {
                Files.copy(Path.of("smoke-results.txt"),Path.of("smoke-results-"+mc.getUser().getName()+".txt"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Files.copy(Path.of("sound-cues.jsonl"),Path.of("sound-cues-"+mc.getUser().getName()+".jsonl"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
            LogUtils.getLogger().info("DRINK_SMOKE_COMPLETE");if(!Boolean.getBoolean("farmingtales.catalogSmoke"))mc.stop();
        }
    }
    private static void press(int key) {
        var k=InputConstants.Type.KEYSYM.getOrCreate(key);
        net.minecraft.client.KeyMapping.set(k,true);net.minecraft.client.KeyMapping.click(k);
        MinecraftForge.EVENT_BUS.post(new InputEvent.Key(key,0,GLFW.GLFW_PRESS,0));
        net.minecraft.client.KeyMapping.set(k,false);
    }
    private static void checkInput(String label,float forward,float strafe,boolean custom) {
        var p=Minecraft.getInstance().player;
        boolean valid=p.isUsingItem()&&DrinkController.shouldHoldUse()==custom
            &&Math.abs(p.input.forwardImpulse-forward)<.001f&&Math.abs(p.input.leftImpulse-strafe)<.001f;
        record((valid?"PASS ":"FAIL ")+label+" forward="+p.input.forwardImpulse+" strafe="+p.input.leftImpulse+" velocity="+p.getDeltaMovement().horizontalDistance());
    }
    @SubscribeEvent public static void sound(PlaySoundSourceEvent event) {
        var instance=event.getSound();
        if(instance==null||tick<65)return;
        String location=instance.getLocation().toString();
        boolean vanillaUse=location.equals("minecraft:entity.generic.drink")||location.equals("minecraft:entity.generic.eat")||location.equals("minecraft:entity.player.burp");
        if(tick>=205&&tick<=352) {
            if(vanillaUse)hotkeyVanillaSounds++;
            if(instance.getLocation().getNamespace().equals("farmingtales_drink_anim"))hotkeyCustomSounds++;
        }
        if(tick>=598&&tick<=612&&vanillaUse)ordinaryUseSounds++;
        if(!instance.getLocation().getNamespace().equals("farmingtales_drink_anim"))return;
        var sound=instance.getSound();
        if(tick>=80&&tick<=184&&instance.getLocation().getPath().startsWith("inspect_")) {
            inspectionSoundTicks.computeIfAbsent(instance.getLocation().getPath(),k->new java.util.concurrent.CopyOnWriteArrayList<>()).add(tick);
            embeddedInspectionSounds&=sound.getLocation().getNamespace().equals("farmingtales_drink_anim");
        }
        if(tick>=205&&tick<=352) {
            playerSoundCategory&=instance.getSource()==net.minecraft.sounds.SoundSource.PLAYERS;
            if(location.equals("farmingtales_drink_anim:gulp")) {
                sipSoundTicks.add(tick);
                restoredSoundResources&=sound.getLocation().toString().equals("farmingtales_drink_anim:vanilla/drink");
            }
            if(location.equals("farmingtales_drink_anim:swallow"))restoredSoundResources=false;
        }
        if(tick>=587&&tick<=590&&location.equals("farmingtales_drink_anim:gulp")) {
            cancellableChannel=event.getChannel();
            try {
                for(var field:net.minecraft.client.sounds.SoundEngine.class.getDeclaredFields())
                    if(java.util.concurrent.Executor.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);audioExecutor=(java.util.concurrent.Executor)field.get(event.getEngine());break;
                    }
            } catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}
        }
        String json=String.format(java.util.Locale.ROOT,"{\"tick\":%d,\"event\":\"%s\",\"resource\":\"%s\",\"volume\":%.5f,\"pitch\":%.5f}\n",tick,location,sound.getLocation(),instance.getVolume(),instance.getPitch());
        try {Files.writeString(Path.of("sound-cues.jsonl"),json,java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}
    }
    private static void checkMotion(String label,double minimum) {
        double energy=DrinkController.SLOSH.energy();
        String result=(energy>minimum?"PASS ":"FAIL ")+label+" liquid movement energy="+energy;
        record(result);
    }
    private static void check(String label,int wine,int bottles,boolean effect) {
        var mc=Minecraft.getInstance();
        mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            int actual=p.getInventory().countItem(SmokeFixture.WINE.get()),glass=p.getInventory().countItem(Items.GLASS_BOTTLE);
            boolean actualEffect=p.hasEffect(MobEffects.MOVEMENT_SPEED);
            String result=(actual==wine&&glass==bottles&&actualEffect==effect?"PASS ":"FAIL ")+label+" wine="+actual+" bottles="+glass+" effect="+actualEffect;
            record(result);
        });
    }
    private static void record(String result) {
        LogUtils.getLogger().info("DRINK_SMOKE {}",result);
        try {Files.writeString(Path.of("smoke-results.txt"),result+"\n",java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e) {
        if(e.phase==TickEvent.Phase.END&&screenshot!=null) {
            var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,mc.getUser().getName()+"-"+screenshot,mc.getMainRenderTarget(),c->{});screenshot=null;
        }
        if(e.phase==TickEvent.Phase.END&&sequenceScreenshot!=null) {
            var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,mc.getUser().getName()+"-"+sequenceScreenshot,mc.getMainRenderTarget(),c->{});sequenceScreenshot=null;
        }
    }
}
