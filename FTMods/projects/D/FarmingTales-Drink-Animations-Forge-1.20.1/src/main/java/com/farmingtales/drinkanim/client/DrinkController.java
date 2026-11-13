package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.DrinkConfig;
import com.farmingtales.drinkanim.math.BottleMotion;
import com.farmingtales.drinkanim.math.AnimationPlayback;
import com.farmingtales.drinkanim.math.AnimationSoundTimeline;
import com.farmingtales.drinkanim.math.ContainerAnimations;
import com.farmingtales.drinkanim.math.DrinkCatalog;
import com.farmingtales.drinkanim.math.DrinkProfile;
import com.farmingtales.drinkanim.math.DrinkMotion;
import com.farmingtales.drinkanim.math.BottleMotion.Action;
import com.farmingtales.drinkanim.math.SloshSpring;
import com.farmingtales.drinkanim.math.Locomotion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class DrinkController {
    public static final SloshSpring SLOSH=new SloshSpring();
    public static final Locomotion MOVEMENT=new Locomotion();
    private static Action action=Action.IDLE;
    private static int age, slot, duration=16, soundCooldown;
    private static boolean ownsUse, holding, wasGround, replacementAvailable, presented;
    private static ItemStack snapshot=ItemStack.EMPTY;
    private static ItemStack lastBottle=ItemStack.EMPTY;
    private static Vec3 previousVelocity=Vec3.ZERO;
    private static float previousYaw, previousPitch;
    private static final com.farmingtales.drinkanim.math.EatingVariants EATING_VARIANTS=
        new com.farmingtales.drinkanim.math.EatingVariants(new java.util.Random());
    private static int eatingVariant;
    public static int eatingVariant() {return eatingVariant;}

    public static boolean supports(ItemStack stack) {return !stack.isEmpty()&&DrinkCatalog.find(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())!=null;}
    public static DrinkProfile profile(ItemStack stack) {
        var profile=DrinkCatalog.find(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        return profile==null?DrinkCatalog.ORANGE:profile;
    }
    public static DrinkProfile profile() {
        var player=Minecraft.getInstance().player;
        return profile(renderStack(player==null?ItemStack.EMPTY:player.getMainHandItem()));
    }
    public static boolean active() {return action!=Action.IDLE;}
    public static boolean renderable() {return active()||presented;}
    public static boolean vanillaUseActive() {
        var mc=Minecraft.getInstance();
        return !active()&&mc.player!=null&&mc.player.isUsingItem()
            &&mc.player.getUsedItemHand()==InteractionHand.MAIN_HAND&&supports(mc.player.getUseItem());
    }
    public static Action action() {return action;}
    public static double age(float partial) {return AnimationPlayback.authored(profile(),action,Math.max(0,age-1+partial),duration);}
    public static int duration() {return duration;}
    public static boolean replacementAvailable() {return replacementAvailable;}
    public static ItemStack renderStack(ItemStack current) {return active()?snapshot:presented?lastBottle:current;}
    public static boolean shouldHoldUse() {
        var mc=Minecraft.getInstance();
        return ownsUse&&action==Action.DRINK&&mc.screen==null&&mc.player!=null
            &&mc.player.getInventory().selected==slot&&supports(mc.player.getMainHandItem())
            &&mc.player.getUsedItemHand()==InteractionHand.MAIN_HAND&&age<=AnimationPlayback.sipEnd(profile(),duration)+5;
    }
    public static void start(Action requested) {
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.gameMode==null||mc.screen!=null||!DrinkConfig.ENABLED.get()
            ||!mc.options.getCameraType().isFirstPerson()||mc.player.isSpectator()||!supports(mc.player.getMainHandItem()))return;
        if(action==Action.DRAW||action==Action.STOW)return;
        else if(active()) {cancel();return;}
        if(presented&&!ItemStack.isSameItemSameTags(lastBottle,mc.player.getMainHandItem()))return;
        if(mc.player.isUsingItem())return;
        snapshot=mc.player.getMainHandItem().copy();
        duration=com.farmingtales.drinkanim.math.PresentationOnly.active(profile(snapshot))
            ?32:Math.max(1,snapshot.getUseDuration());
        if(duration>200) return;
        slot=mc.player.getInventory().selected;
        DrinkAudio.stopAll();
        action=requested;age=0;ownsUse=false;replacementAvailable=false;presented=true;
        if(requested==Action.DRINK)eatingVariant=EATING_VARIANTS.next();
        lastBottle=snapshot.copy();
        if(requested==Action.INSPECT)DrinkAudio.playInspection("cloth",.06f,1);
        else DrinkAudio.play("cloth",.4f,1);
    }
    public static void placed(ItemStack used) {
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.screen!=null||!DrinkConfig.ENABLED.get()||!supports(used)
            ||!mc.options.getCameraType().isFirstPerson())return;
        DrinkAudio.stopAll();
        action=Action.IDLE;age=0;ownsUse=false;replacementAvailable=false;snapshot=ItemStack.EMPTY;
        presented=supports(mc.player.getMainHandItem());
        lastBottle=presented?mc.player.getMainHandItem().copy():ItemStack.EMPTY;
        // Minecraft's successful-use swing drives the shared hand/container rig.
    }
    public static void tick() {
        var mc=Minecraft.getInstance();
        if(mc.player==null||!mc.player.isAlive()||!DrinkConfig.ENABLED.get()) {
            resetAll();holding=false;SLOSH.reset();MOVEMENT.reset(0);FirstPersonCompat.update(false);return;
        }
        if(mc.isPaused())return;
        boolean supported=supports(mc.player.getMainHandItem());
        if(action==Action.IDLE&&presented&&supported&&!lastBottle.isEmpty()
            &&!ItemStack.isSameItemSameTags(lastBottle,mc.player.getMainHandItem())) {
            beginPresentation(Action.STOW,lastBottle);return;
        }
        FirstPersonCompat.update((supported||active()||presented)&&mc.options.getCameraType().isFirstPerson());
        if(supported||active()||presented) updateMotion(mc); else {holding=false;SLOSH.reset();MOVEMENT.reset(mc.player.walkDist);}
        if(action==Action.PLACE) {
            age++;
            if(age==5)DrinkAudio.play("inspect_settle",.12f,1.08f);
            if(age>=(replacementAvailable?com.farmingtales.drinkanim.math.PlacementMotion.TICKS:com.farmingtales.drinkanim.math.PlacementMotion.RECOVER_TICK)
                ||mc.screen!=null||mc.player.getInventory().selected!=slot
                ||!mc.options.getCameraType().isFirstPerson()) {
                presented=supported&&ItemStack.isSameItemSameTags(snapshot,mc.player.getMainHandItem());
                lastBottle=presented?mc.player.getMainHandItem().copy():ItemStack.EMPTY;
                snapshot=ItemStack.EMPTY;action=Action.IDLE;age=0;replacementAvailable=false;
            }
            return;
        }
        if(action==Action.IDLE) {
            if(supported&&!presented)beginPresentation(Action.DRAW,mc.player.getMainHandItem());
            else if(!supported&&presented&&!lastBottle.isEmpty())beginPresentation(Action.STOW,lastBottle);
            return;
        }
        if(action==Action.DRAW||action==Action.STOW) {
            age++;
            int end=AnimationPlayback.ticks(profile(),action,duration);
            if(age>=end)completePresentation(supported);
            return;
        }
        if(mc.screen!=null||!mc.options.getCameraType().isFirstPerson()||mc.player.getInventory().selected!=slot) {cancel();return;}
        if(mc.isPaused())return;
        int sipEnd=AnimationPlayback.sipEnd(profile(),duration);
        int useStart=sipEnd-duration;
        if(action==Action.DRINK&&age<useStart&&!ItemStack.isSameItemSameTags(snapshot,mc.player.getMainHandItem())) {cancel();return;}
        if(action==Action.INSPECT&&!ItemStack.isSameItemSameTags(snapshot,mc.player.getMainHandItem())) {cancel();return;}
        double previous=AnimationPlayback.authored(profile(),action,age,duration);
        age++;
        double current=AnimationPlayback.authored(profile(),action,age,duration);
        if(action==Action.INSPECT) {
            for(var cue:AnimationSoundTimeline.inspection(profile()))
                if(AnimationPlayback.crossed(previous,current,cue.tick()))DrinkAudio.playInspection(cue.event(),cue.volume(),cue.pitch());
            if(age>=AnimationPlayback.ticks(profile(),action,duration))finishGesture();
            return;
        }
        if(profile().cork()||profile().jam()) {
            if(AnimationPlayback.crossed(previous,current,18))DrinkAudio.play("cork_pull",.30f,.96f);
            if(AnimationPlayback.crossed(previous,current,BottleMotion.CORK_RELEASE))DrinkAudio.play("cork",.65f,.94f);
        } else if(profile().style()==DrinkProfile.Style.BEER) {
            if(AnimationPlayback.crossed(previous,current,BottleMotion.CORK_RELEASE))DrinkAudio.play("cap_release",.30f,1.20f);
        } else if(profile().can()) {
            if(AnimationPlayback.crossed(previous,current,24))DrinkAudio.play("can_open",.48f,1.0f);
        } else if(!profile().cup()&&!profile().food()) {
            if(AnimationPlayback.crossed(previous,current,16)||AnimationPlayback.crossed(previous,current,25))DrinkAudio.play("cap_twist",.30f,current<24?1.06f:1.12f);
            if(AnimationPlayback.crossed(previous,current,33))DrinkAudio.play("cap_release",.24f,1.12f);
        }
        if(age==useStart&&!com.farmingtales.drinkanim.math.PresentationOnly.active(profile())) {
            // Normal useItem sends the vanilla packet. The original item owns consumption, effects and containers.
            var result=mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);
            if(!result.consumesAction()||!mc.player.isUsingItem()) {cancel();return;}
            ownsUse=true;
        }
        if(ownsUse) {
            boolean using=mc.player.isUsingItem()&&mc.player.getUsedItemHand()==InteractionHand.MAIN_HAND;
            if(!using) {
                ownsUse=false;
                if(age<sipEnd-2) {cancel();return;}
                age=Math.max(age,sipEnd);
                replacementAvailable=supports(mc.player.getMainHandItem());
            } else if(!supports(mc.player.getUseItem())) {cancel();return;}
            else if(age>sipEnd+5) {cancel();return;}
        }
        if(!ownsUse&&age>=sipEnd&&!replacementAvailable&&supports(mc.player.getMainHandItem()))replacementAvailable=true;
        if(AnimationPlayback.crossed(previous,current,BottleMotion.RAISE_TICKS))DrinkAudio.play("cloth",.12f,1);
        for(var cue:AnimationSoundTimeline.drinking(profile(),duration))
            if(AnimationPlayback.crossed(previous,current,cue.tick()))DrinkAudio.play(cue.event(),cue.volume(),cue.pitch());
        if(AnimationPlayback.crossed(previous,current,BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration)+(profile().jam()?6:18))||AnimationPlayback.crossed(previous,current,BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration)+(profile().jam()?22:41))&&replacementAvailable)DrinkAudio.play("cloth",.22f,1);
        if(age>=AnimationPlayback.ticks(profile(),action,duration))finishGesture();
    }
    private static void updateMotion(Minecraft mc) {
        if(mc.isPaused())return;
        var p=mc.player;Vec3 velocity=p.getDeltaMovement();
        if(!holding) {previousVelocity=velocity;previousYaw=p.getYRot();previousPitch=p.getXRot();wasGround=p.onGround();holding=true;MOVEMENT.reset(p.walkDist);}
        Vec3 acceleration=velocity.subtract(previousVelocity);
        double yaw=Math.toRadians(p.getYRot()),turn=Mth.wrapDegrees(p.getYRot()-previousYaw),pitch=com.farmingtales.drinkanim.math.LiquidSurface.viewPitch(profile(),p.getXRot())-com.farmingtales.drinkanim.math.LiquidSurface.viewPitch(profile(),previousPitch);
        double side=acceleration.x*Math.cos(yaw)+acceleration.z*Math.sin(yaw);
        double forward=-acceleration.x*Math.sin(yaw)+acceleration.z*Math.cos(yaw);
        double speed=Math.min(.4,velocity.horizontalDistance());
        MOVEMENT.tick(p.walkDist,speed,p.onGround(),p.isSprinting(),acceleration.y,turn);
        double gait=com.farmingtales.drinkanim.math.LiquidSurface.walkingImpulse(p.walkDist,speed,p.onGround());
        double landing=!wasGround&&p.onGround()?Math.min(.3,Math.abs(previousVelocity.y)*.45):0;
        double strength=DrinkConfig.STRENGTH.get();
        double gesture=action==Action.INSPECT&&(age==22||age==38||age==54||age==70)?.05:action==Action.DRINK&&age==BottleMotion.CORK_RELEASE?.10:0;
        if(com.farmingtales.drinkanim.math.BambooEggAnimations.owns(profile()))
            gesture=com.farmingtales.drinkanim.math.BambooEggAnimations.gestureImpulse(action,
                (int)Math.floor(AnimationPlayback.authored(profile(),action,age,duration)));
        else if(com.farmingtales.drinkanim.math.StickyRiceCakeAnimations.owns(profile()))
            gesture=com.farmingtales.drinkanim.math.StickyRiceCakeAnimations.gestureImpulse(action,
                (int)Math.floor(AnimationPlayback.authored(profile(),action,age,duration)));
        else if(com.farmingtales.drinkanim.math.PlateFoodAnimations.owns(profile()))
            gesture=com.farmingtales.drinkanim.math.PlateFoodAnimations.gestureImpulse(profile(),action,
                (int)Math.floor(AnimationPlayback.authored(profile(),action,age,duration)));
        else if(com.farmingtales.drinkanim.math.LandNo1BottleAnimations.owns(profile())&&action==Action.INSPECT)
            gesture=com.farmingtales.drinkanim.math.LandNo1BottleAnimations.gestureImpulse(
                AnimationPlayback.authored(profile(),action,age,duration));
        if(ContainerAnimations.owns(profile())) {
            gesture=0;
            int before=(int)Math.floor(AnimationPlayback.authored(profile(),action,Math.max(0,age-1),duration));
            int now=(int)Math.floor(AnimationPlayback.authored(profile(),action,age,duration));
            for(int tick=before+1;tick<=now;tick++)gesture+=ContainerAnimations.gestureImpulse(profile(),action,tick);
        }
        double gestureX=gesture,gestureZ=gesture*.6;
        if(action==Action.INSPECT) {
            var family=profile().jam()?ContainerAnimations.Family.CAN:ContainerAnimations.family(profile());
            double authored=AnimationPlayback.authored(profile(),action,age,duration)-(profile().jam()?com.farmingtales.drinkanim.math.JamAnimations.INSPECT_SHAKE_OFFSET:0);
            gestureX+=com.farmingtales.drinkanim.math.InertialGestures.liquid(family,authored,0);
            gestureZ+=com.farmingtales.drinkanim.math.InertialGestures.liquid(family,authored,2);
        }
        SLOSH.tick((-side*1.2-turn*.007+gait+landing*.6+gestureX)*strength,
            (-forward*1.2-pitch*.006-acceleration.y*.16-gait*.7+landing+gestureZ)*strength);
        if(soundCooldown>0)soundCooldown--;
        if(mc.screen==null&&!profile().jam()&&!profile().food()&&!active()&&!vanillaUseActive()&&soundCooldown==0&&(landing>.07||SLOSH.energy()>.35)) {
            DrinkAudio.play("slosh",(float)Math.min(.35,.1+SLOSH.energy()*.2),1+(p.tickCount%3)*.03f);soundCooldown=24;
        }
        previousVelocity=velocity;previousYaw=p.getYRot();previousPitch=p.getXRot();wasGround=p.onGround();
    }
    public static void cancel() {
        var mc=Minecraft.getInstance();
        DrinkAudio.stopAll();
        if(action==Action.PLACE) {
            presented=false;lastBottle=ItemStack.EMPTY;snapshot=ItemStack.EMPTY;
            action=Action.IDLE;age=0;ownsUse=false;replacementAvailable=false;return;
        }
        if(action==Action.DRAW||action==Action.STOW) {
            // Selection may change again while an old bottle is moving. Keep its snapshot
            // until the pocket transition ends, then draw the latest selected stack.
            return;
        }
        boolean release=ownsUse;
        ItemStack old=snapshot.copy();
        boolean current=mc.player!=null&&supports(mc.player.getMainHandItem());
        if(release&&mc.player!=null&&mc.gameMode!=null)mc.gameMode.releaseUsingItem(mc.player);
        if(supports(old)&&(!current||!ItemStack.isSameItemSameTags(old,mc.player.getMainHandItem())))
            beginPresentation(Action.STOW,old);
        else finishGesture();
    }
    private static void beginPresentation(Action next,ItemStack stack) {
        DrinkAudio.stopAll();
        action=next;age=0;ownsUse=false;replacementAvailable=false;snapshot=stack.copy();
        if(next==Action.DRAW)presented=false;
        DrinkAudio.play("cloth",.22f,next==Action.DRAW?1.04f:.96f);
    }
    private static void completePresentation(boolean currentSupported) {
        boolean drew=action==Action.DRAW;
        ItemStack shown=snapshot.copy();
        var player=Minecraft.getInstance().player;
        if(drew&&(!currentSupported||!ItemStack.isSameItemSameTags(shown,player.getMainHandItem()))) {
            beginPresentation(Action.STOW,shown);return;
        }
        if(!drew&&currentSupported) {
            beginPresentation(Action.DRAW,player.getMainHandItem());return;
        }
        presented=drew&&currentSupported;
        lastBottle=presented?shown:ItemStack.EMPTY;
        action=Action.IDLE;ownsUse=false;replacementAvailable=false;age=0;snapshot=ItemStack.EMPTY;
    }
    private static void finishGesture() {
        DrinkAudio.stopAll();
        var player=Minecraft.getInstance().player;
        if(player!=null&&supports(player.getMainHandItem())&&!ItemStack.isSameItemSameTags(snapshot,player.getMainHandItem())) {
            beginPresentation(Action.STOW,snapshot);return;
        }
        presented=player!=null&&supports(player.getMainHandItem());
        if(presented)lastBottle=player.getMainHandItem().copy();
        else lastBottle=ItemStack.EMPTY;
        action=Action.IDLE;ownsUse=false;replacementAvailable=false;age=0;snapshot=ItemStack.EMPTY;
    }
    public static BottleMotion.Camera renderCamera(float partial) {
        return MOVEMENT.camera(partial,DrinkMotion.camera(profile(),action(),age(partial),duration(),eatingVariant),DrinkMotion.movementCameraStrength(action()));
    }
    private static void resetAll() {
        DrinkAudio.stopAll();
        action=Action.IDLE;ownsUse=false;replacementAvailable=false;presented=false;age=0;
        snapshot=ItemStack.EMPTY;lastBottle=ItemStack.EMPTY;
    }
}
