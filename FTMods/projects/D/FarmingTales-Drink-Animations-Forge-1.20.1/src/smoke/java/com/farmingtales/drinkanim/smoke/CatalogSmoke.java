package com.farmingtales.drinkanim.smoke;

import com.farmingtales.drinkanim.client.*;
import com.farmingtales.drinkanim.math.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;

@Mod.EventBusSubscriber(modid="drink_animation_smoke",value=Dist.CLIENT)
public final class CatalogSmoke {
    private static final List<DrinkProfile> ITEMS=DrinkCatalog.all().stream().filter(p->{
        String selection=System.getProperty("farmingtales.smokeDrinks","");
        return selection.isEmpty()||selection.equals("--papercraft")&&p.id().startsWith("papercraft_magic_decoration:")
            ||selection.equals("--letsdo")&&(p.id().startsWith("brewery:")||p.id().startsWith("vinery:"))
            ||Arrays.asList(selection.split(",")).contains(p.id())||Arrays.asList(selection.split(",")).contains(p.id().split(":")[1]);
    }).toList();
    private static int index,age;
    private static int breathingFrames;
    private static boolean prepared;
    private static int preparationTicks;
    private static volatile String current="";
    private static final List<String> sounds=new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Float> gulpVolumes=new java.util.concurrent.CopyOnWriteArrayList<>();
    private record InspectionSound(String event,float volume) {}
    private static final List<InspectionSound> inspectionSounds=new java.util.concurrent.CopyOnWriteArrayList<>();
    private record AudioSample(String item,String action,String event,String resource,float volume,float pitch) {}
    private static final List<AudioSample> audioSamples=new java.util.concurrent.CopyOnWriteArrayList<>();
    private static Path result;
    private static float minCameraYaw=Float.POSITIVE_INFINITY,maxCameraYaw=Float.NEGATIVE_INFINITY;
    private static float baseCameraYaw=Float.NaN;
    private static int cameraSamples;
    private static int eatingVariant=-1,lastEatingVariant=-1,eatingCameraSamples;
    private static boolean eatingVariantStable;
    private static double eatingCameraPeak;
    private static final Set<Integer> eatingStyles=new HashSet<>();
    private static int switchingSettledAt;
    private static org.joml.Quaternionf previousRunningBottle;
    private static boolean runningReturnContinuous=true;
    private static double walkingMin,walkingMax,walkingStart;
    private static double capDecorationPeak;
    private static double ribbonPeak;
    private static double viewRibbonPeak;
    private static float ribbonViewYaw;
    private static net.minecraft.world.phys.Vec3 ribbonWalkStart;
    @SubscribeEvent public static void renderedCamera(net.minecraftforge.client.event.RenderLevelStageEvent e) {
        if(Boolean.getBoolean("farmingtales.eatingSmoke")
            &&e.getStage()==net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL
            &&DrinkController.action()==BottleMotion.Action.DRINK) {
            var mc=Minecraft.getInstance();var actual=mc.gameRenderer.getMainCamera();
            double pitch=actual.getXRot()-mc.player.getXRot();
            double yaw=net.minecraft.util.Mth.wrapDegrees(actual.getYRot()-mc.player.getYRot());
            eatingCameraPeak=Math.max(eatingCameraPeak,Math.hypot(pitch,yaw));eatingCameraSamples++;
        }
        if(e.getStage()!=net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL
            ||!current.equals("farmingtales:brandy_xo")||DrinkController.action()!=BottleMotion.Action.INSPECT)return;
        var camera=Minecraft.getInstance().gameRenderer.getMainCamera();
        if(Float.isNaN(baseCameraYaw))baseCameraYaw=camera.getYRot();
        float yaw=net.minecraft.util.Mth.wrapDegrees(camera.getYRot()-baseCameraYaw);
        minCameraYaw=Math.min(minCameraYaw,yaw);maxCameraYaw=Math.max(maxCameraYaw,yaw);cameraSamples++;
    }
    public static void tick(Minecraft mc) {
        if(index>=ITEMS.size()) {
            if(Boolean.getBoolean("farmingtales.eatingSmoke")&&ITEMS.size()>=4)
                check(eatingStyles.size()==4,"runtime selects all four eating styles");
            try {Files.writeString(Path.of("audio-balance-"+mc.getUser().getName()+".json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(audioSamples));}
            catch(Exception e) {throw new RuntimeException(e);}
            com.mojang.logging.LogUtils.getLogger().info("CATALOG_SMOKE_COMPLETE {}",ITEMS.size());mc.stop();return;
        }
        var profile=ITEMS.get(index);var item=SmokeFixture.DRINKS.get(profile.id()).get();
        if(!prepared) {
          if(preparationTicks++==0) {
            if(result==null) {
                result=Path.of("catalog-results-"+mc.getUser().getName()+".txt");
                try {Files.writeString(result,"");}catch(Exception e){throw new RuntimeException(e);}
            }
            ModelCompatSmoke.prepare(mc);
            current=profile.id();sounds.clear();mc.setScreen(null);DrinkController.cancel();
            minCameraYaw=Float.POSITIVE_INFINITY;maxCameraYaw=Float.NEGATIVE_INFINITY;cameraSamples=0;baseCameraYaw=Float.NaN;
            mc.options.mainHand().set(Boolean.getBoolean("farmingtales.smokeLeftHand")?net.minecraft.world.entity.HumanoidArm.LEFT:net.minecraft.world.entity.HumanoidArm.RIGHT);mc.player.getInventory().selected=8;
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if(index==0) {
                    var anchor=player.blockPosition();
                    // Fixed landmarks make world-camera motion visible in the isolated preview.
                    for(int x:new int[]{-7,-3,3,7})for(int y=0;y<4;y++)
                        player.serverLevel().setBlockAndUpdate(anchor.offset(x,y,-14),
                            (y==3?net.minecraft.world.level.block.Blocks.SEA_LANTERN:net.minecraft.world.level.block.Blocks.SMOOTH_STONE).defaultBlockState());
                }
                var initial=new ItemStack(item,3);
                if((profile.bodyModel().startsWith("animation/world_bottles/")||profile.bodyModel().startsWith("animation/tavern_bottles/")))initial.getOrCreateTag().putInt("BrewLevel",6);
                player.getInventory().clearContent();player.getInventory().setItem(0,initial);
                player.getFoodData().setFoodLevel(15);player.removeAllEffects();
            });
          }
          var ready=mc.player.getInventory().getItem(0);
          if(ready.is(item)&&ready.getCount()==3&&!DrinkController.active()&&!DrinkController.renderable()) {
              mc.player.getInventory().selected=0;prepared=true;age=0;
          } else if(preparationTicks>150)throw new IllegalStateException("Fixture did not settle before drawing "+profile.id());
          return;
        }
        int t=age++;
        if(CoconutAnimations.owns(profile)&&Boolean.getBoolean("farmingtales.capturePerformances")) {
            if(t>=60&&t<=164&&t%4==0)shot(mc,profile,"coconut-inspect-"+(t-60));
            if(t>=168&&t<=250&&t%2==0)shot(mc,profile,"coconut-drink-"+(t-168));
        }
        if(ServingAnimations.owns(profile)) {
            int inspectTicks=AnimationPlayback.ticks(profile,BottleMotion.Action.INSPECT,32);
            if(Boolean.getBoolean("farmingtales.capturePerformances")&&t>=60&&t<=60+inspectTicks&&t%2==0)
                shot(mc,profile,(ServingAnimations.popsicle(profile)?"popsicle-inspect-":ServingAnimations.pineapple(profile)?"pineapple-inspect-":"chocolate-inspect-")+(t-60));
            int extra=Math.max(0,inspectTicks-BottleMotion.INSPECT_TICKS);
            if(t>=168&&t<168+extra)return;
            if(t>=168+extra)t-=extra;
        }
        // View the exposed inner floor at several steep angles, where coplanar
        // shoulder faces used to flicker. This is confined to the test client.
        if(Boolean.getBoolean("farmingtales.bottleSurfaceSmoke")) {
            if(t==195)mc.player.setXRot(60);
            if(t==199)mc.player.setXRot(70);
            if(t==203)mc.player.setXRot(80);
            if(t==197||t==201||t==205)shot(mc,profile,"cavity-angle-"+t);
            if(t==208)mc.player.setXRot(0);
        }
        if(DecorationMotion.parts(profile).stream().anyMatch(DecorationMotion.Part::capAttached)) {
            if(t==50)capDecorationPeak=0;
            if(t>=60&&t<=164) {
                try {
                    var field=BottleRenderer.class.getDeclaredField("CAP_DECORATIONS");field.setAccessible(true);
                    var state=(DecorationMotion.State)field.get(null);
                    capDecorationPeak=Math.max(capDecorationPeak,Math.abs(state.x())+Math.abs(state.z()));
                } catch(ReflectiveOperationException e){throw new RuntimeException(e);}
                if(t%8==0)shot(mc,profile,"ornament-inspect-"+t);
            }
            if(t==165)check(capDecorationPeak>.005,"cap ornament responds to rendered inspection, peak="+capDecorationPeak);
            if(t>=176&&t<=216&&t%4==0)shot(mc,profile,"ornament-opening-"+t);
        }
        if(DecorationMotion.parts(profile).stream().anyMatch(part->part.ribbon()!=null)) {
            if(t==50)ribbonPeak=0;
            if(t>=60&&t<=164) {
                for(var part:DecorationMotion.parts(profile))if(part.ribbon()!=null)try {
                    var field=BottleRenderer.class.getDeclaredField(part.capAttached()?"CAP_DECORATIONS":"DECORATIONS");field.setAccessible(true);
                    var state=(DecorationMotion.State)field.get(null);
                    var baked=mc.getModelManager().getModel(BottleRenderer.model(part.model()));
                    for(var quad:baked.getQuads(null,null,net.minecraft.util.RandomSource.create(42))) {
                        var data=quad.getVertices();int stride=data.length/4;
                        for(int i=0;i<4;i++) {
                            var point=new org.joml.Vector3f(Float.intBitsToFloat(data[i*stride]),Float.intBitsToFloat(data[i*stride+1]),Float.intBitsToFloat(data[i*stride+2])).mul(16);
                            ribbonPeak=Math.max(ribbonPeak,point.distance(state.ribbonPoint(part,point)));
                        }
                    }
                } catch(ReflectiveOperationException e){throw new RuntimeException(e);}
                if(t%8==0)shot(mc,profile,"ribbon-inspect-"+t);
            }
            if(t==165)check(ribbonPeak>.05,"rigid ornament visibly swings in rendered inspection, pixels="+ribbonPeak);
        }
        // Inspect every new model; exercise complete interaction sequences for each bottle archetype.
        if(System.getProperty("farmingtales.smokeDrinks","").equals("--letsdo")&&t==51
            &&!Set.of("whiskey_highland_hearth","whiskey_jamesons_malt","whiskey_lilitusinglemalt",
                "clark_wine","creepers_crush","jellie_wine","jo_special_mixture","cristel_wine",
                "red_wine","strad_wine","beer_hops","beer_haley","lilitu_wine","apple_cider","eiswein","mellohi_wine").contains(profile.id().split(":")[1])) {
            DrinkController.cancel();mc.player.getInventory().selected=8;index++;age=0;prepared=false;preparationTicks=0;return;
        }
        if(CupAnimations.coffeePreview(profile)) {
            if(t>=60&&t<=164&&t%4==0)shot(mc,profile,"coffee-inspect-"+(t-60));
            if(t>=168&&t<=168+AnimationPlayback.ticks(profile,BottleMotion.Action.DRINK,32)&&t%2==0)
                shot(mc,profile,"coffee-meal-"+(t-168));
        }
        if(ServingAnimations.owns(profile)) {
            if(t>=168&&t<=168+AnimationPlayback.ticks(profile,BottleMotion.Action.DRINK,32)&&t%2==0)
                shot(mc,profile,"garnish-meal-"+(t-168));
            int extra=AnimationPlayback.raiseTicks(profile)-38;
            if(t>=232&&t<232+extra)return;
            if(t>=232+extra)t-=extra;
        }
        if(Boolean.getBoolean("farmingtales.eatingSmoke")) {
            if(DrinkController.action()==BottleMotion.Action.DRINK&&t>168)
                eatingVariantStable&=DrinkController.eatingVariant()==eatingVariant;
            if(t>=190&&t<=270&&t%10==0)shot(mc,profile,"eating-v52-"+t);
        }
        if(Boolean.getBoolean("farmingtales.breathingSmoke")&&index==0&&t==50&&breathingFrames<=80) {
            if(breathingFrames%10==0)shot(mc,profile,"breathing-idle-"+breathingFrames);
            breathingFrames++;age--;return;
        }
        if(Boolean.getBoolean("farmingtales.breathingSmoke")&&t>=220&&t<=290&&t%10==0)
            shot(mc,profile,"breathing-eat-"+t);
        if((profile.cup()||profile.food())&&t%10==0&&t<=330)shot(mc,profile,String.format(java.util.Locale.ROOT,"half-second-%03d",t));
        // The half-second frames land between mouthfuls, so shoot the instants the hand reaches the
        // lips and the ones where it has settled back into the waiting hold.
        if(t>=168&&StickyRiceCakeAnimations.owns(profile))
            for(int bite:StickyRiceCakeAnimations.bites(16))if(t==168+bite||t==168+bite+4)
                shot(mc,profile,"mouthful-"+(t-168));
        if(ContainerAnimations.owns(profile)&&(t==6||t==10||t==14||t==18))shot(mc,profile,"draw-"+t);
        // The whole catalogue is driven by the real hotkeys here: V eats, G inspects. Every other
        // assertion calls the controller directly, so this is what proves the keys reach each item.
        if(t==24) {
            check(DrinkController.action()==BottleMotion.Action.IDLE&&DrinkController.renderable(),
                "settled in the idle hold before the hotkey");
            press(GLFW.GLFW_KEY_V);
            check(DrinkController.action()==BottleMotion.Action.DRINK,"V starts this item's eating animation");
            DrinkController.cancel();
        }
        if(t==38) {
            check(DrinkController.action()==BottleMotion.Action.IDLE,"the eating hotkey settles back to idle");
            press(GLFW.GLFW_KEY_G);
            check(DrinkController.action()==BottleMotion.Action.INSPECT,"G starts this item's inspection animation");
            DrinkController.cancel();
        }
        ModelCompatSmoke.verify(mc,t);
        if(t==50) {
            if(index==0)checkJarFamilyResources(mc);
            if(Boolean.getBoolean("farmingtales.shaderSmoke")) {
                try {
                    var api=Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                    check((Boolean)api.getMethod("isShaderPackInUse").invoke(api.getMethod("getInstance").invoke(null)),"requested shader pack is active");
                }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
            }
            check(DrinkController.supports(mc.player.getMainHandItem())&&DrinkController.profile().id().equals(profile.id())&&!DrinkController.active(),"selected item reaches its own idle");
            if((profile.bodyModel().startsWith("animation/world_bottles/")||profile.bodyModel().startsWith("animation/tavern_bottles/"))) {
                check(DrinkController.supports(new ItemStack(item)),"bottle animation works without BrewLevel");
                for(int brewLevel:new int[]{0,6,12}) {
                    var tagged=new ItemStack(item);tagged.getOrCreateTag().putInt("BrewLevel",brewLevel);
                    check(DrinkController.supports(tagged),"bottle animation ignores BrewLevel="+brewLevel);
                }
                check(mc.player.getMainHandItem().getTag().getInt("BrewLevel")==6,"idle preserves original BrewLevel");
            }
            var missing=mc.getModelManager().getMissingModel();
            var textureModels=new ArrayList<String>(List.of(profile.bodyModel(),profile.capModel(),profile.neckModel()));
            textureModels.addAll(Arrays.asList(ServingAnimations.models(profile)));
            for(var part:DecorationMotion.parts(profile))textureModels.add(part.model());
            boolean texturesPresent=true;
            for(String path:textureModels) {
                var baked=mc.getModelManager().getModel(BottleRenderer.model(path));
                var faces=new ArrayList<net.minecraft.core.Direction>(Arrays.asList(net.minecraft.core.Direction.values()));faces.add(null);
                for(var face:faces)for(var quad:baked.getQuads(null,face,net.minecraft.util.RandomSource.create(42)))
                    texturesPresent&=!quad.getSprite().contents().name().getPath().equals("missingno");
            }
            check(texturesPresent,"body neck and decoration textures are present");
            if(BottleClosures.fixedBottle(profile)) {
                if(profile.id().equals("brewery:whiskey_highland_hearth"))
                    check(!profile.liquid().isEmpty(),"Highland Hearth uses dynamic whisky");
                else check(profile.liquid().isEmpty(),"original fixed contents have no dynamic liquid pass");
                if(BottleClosures.longCork(profile)) {
                    double minY=Double.POSITIVE_INFINITY,maxY=Double.NEGATIVE_INFINITY;
                    var baked=mc.getModelManager().getModel(BottleRenderer.model(profile.capModel()));
                    for(var quad:baked.getQuads(null,null,net.minecraft.util.RandomSource.create(42))) {
                        var vertices=quad.getVertices();int stride=vertices.length/4;
                        for(int i=0;i<4;i++) {
                            double y=Float.intBitsToFloat(vertices[i*stride+1]);
                            minY=Math.min(minY,y);maxY=Math.max(maxY,y);
                        }
                    }
                    check(Math.abs(maxY-minY-BottleClosures.corkLength(profile))<1e-5,"baked cork is three times the original length");
                }
            }
            if(profile.jam()) {
                var rim=mc.getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                    .apply(ResourceLocation.parse("farmingtales_drink_anim:jam/ancient_fruit_jam_lip"));
                check(rim.contents().name().toString().equals("farmingtales_drink_anim:jam/ancient_fruit_jam_lip"),"jam pale cup rim texture is loaded");
            }
            for(String path:List.of(profile.bodyModel(),profile.capModel()))
                check(mc.getModelManager().getModel(BottleRenderer.model(path))!=missing,"baked model "+path);
            if(profile.can())check(mc.getModelManager().getModel(BottleRenderer.model(profile.canLid().flapModel()))!=missing,"opening flap model loaded");
            for(var part:DecorationMotion.parts(profile))check(mc.getModelManager().getModel(BottleRenderer.model(part.model()))!=missing,"dynamic "+part.kind()+" model loaded");
            for(var region:profile.liquid()) {
                var sprite=mc.getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                    .apply(ResourceLocation.parse(region.texture().replace(":textures/",":").replace(".png","")));
                check(!sprite.contents().name().getPath().equals("missingno"),"liquid atlas sprite loaded "+sprite.contents().name());
            }
            if(profile.style()==DrinkProfile.Style.BEER)check(mc.getModelManager().getModel(BottleRenderer.model(profile.neckModel()))!=missing,"opaque beer neck model loaded");
            shot(mc,profile,"idle");
            if(BeerFoam.supports(profile)) {
                var r=BeerFoam.region(profile);var sprite=mc.getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                    .apply(ResourceLocation.parse(r.texture().replace(":textures/",":").replace(".png","")));
                check(!sprite.contents().name().getPath().equals("missingno"),"beer foam atlas loaded");
            }
        }
        // Exercise both authored movement-audio limits through Forge's real sound engine.
        if(index==0&&t==52)DrinkAudio.play("slosh",.10f,1);
        if(index==0&&t==55)DrinkAudio.play("slosh",.35f,1.06f);
        if(index==0&&t==59) {
            var movementSounds=audioSamples.stream().filter(s->s.event().equals("slosh")).toList();
            check(movementSounds.size()==2
                &&movementSounds.stream().allMatch(s->s.resource().contains(":balanced/"))
                &&movementSounds.stream().anyMatch(s->Math.abs(s.volume()-.092f)<.00001f)
                &&movementSounds.stream().anyMatch(s->Math.abs(s.volume()-.128f)<.00001f),
                "movement liquid sounds use half gain and retain their loudness difference");
        }
        if(t==60) {inspectionSounds.clear();DrinkController.start(BottleMotion.Action.INSPECT);}
        if(t==92)shot(mc,profile,"inspect");
        if(ContainerAnimations.owns(profile)&&(t==80||t==103||t==120||t==137||t==155))shot(mc,profile,"inspect-"+(t-60));
        if(t==168) {
            check(inspectionVolumeMatches(profile),"inspection liquid sounds use half gain while other sounds retain their volume");
            if(profile.style()==DrinkProfile.Style.BRANDY)check(cameraSamples>10&&maxCameraYaw-minCameraYaw>.10f&&maxCameraYaw-minCameraYaw<.35f,
                "rendered inspection world camera stays calm, yaw range="+(maxCameraYaw-minCameraYaw)+" degrees");
            check(mc.player.getMainHandItem().getCount()==3&&!DrinkController.active(),"inspect preserves all three items");
            DrinkController.start(BottleMotion.Action.DRINK);sounds.clear();gulpVolumes.clear();
            if(Boolean.getBoolean("farmingtales.eatingSmoke")) {
                eatingVariant=DrinkController.eatingVariant();eatingStyles.add(eatingVariant);
                check(eatingVariant!=lastEatingVariant,"consecutive eating styles differ, selected="+eatingVariant);
                lastEatingVariant=eatingVariant;eatingVariantStable=true;eatingCameraSamples=0;eatingCameraPeak=0;
            }
        }
        if(t==190)shot(mc,profile,"opening");
        if(BottleClosures.longCork(profile)&&(t==196||t==199||t==202||t==205))
            shot(mc,profile,"long-cork-"+(t-168));
        if(t==188&&profile.cup()&&!com.farmingtales.drinkanim.math.ServingAnimations.owns(profile))check(DrinkController.age(1)>=BottleMotion.RAISE_TICKS,"cup reaches sipping within one second");
        if(t==202&&profile.style()==DrinkProfile.Style.JUICE)
            check(DrinkController.age(1)>=BottleMotion.RAISE_TICKS,"bottled juice raises faster after opening");
        if(t==200) {
            shot(mc,profile,"open-neck");
            if(BottleClosures.stopper(profile))check(DrinkMotion.neckReveal(profile,DrinkController.action(),DrinkController.age(1),16,false)==0,"wooden stopper has no extra replacement neck");
            if(profile.style()==DrinkProfile.Style.BEER)check(DrinkMotion.neckReveal(profile,DrinkController.action(),DrinkController.age(1),16,false)==1,"beer cap removal exposes the full glass neck");
        }
        if(BottleClosures.waterCap(profile)&&t>=180&&t<=208&&t%4==0)shot(mc,profile,"lid-turn-"+(t-168));
        if(ContainerAnimations.owns(profile)&&(t==206||t==214||t==222||t==252||t==272||t==292||t==300))shot(mc,profile,"drink-"+(t-168));
        if(t==208&&profile.can())shot(mc,profile,"tab-throw");
        if(t==218&&profile.can()) {
            shot(mc,profile,"tab-gone");
            check(!DrinkMotion.capVisible(profile,DrinkController.action(),DrinkController.age(1),16,false),"removed tab is absent during drinking");
        }
        if(t==232) {
            shot(mc,profile,"drink");
            if(profile.jam()&&!profile.sweetJam())
                check(itemCrumbs(mc)==0,"custom jar eating has no vanilla item crumbs");
            if(profile.food())check(itemCrumbs(mc)==0,"spoon eating has no floating whole-item crumbs");
        }
        if(Boolean.getBoolean("farmingtales.letsdoLiquidSmoke")&&profile.id().equals("brewery:whiskey_highland_hearth")
            &&(t==246||t==250||t==252))shot(mc,profile,"low-fill-"+t);
        if(Boolean.getBoolean("farmingtales.breathingSmoke")&&profile.jam()&&t==220) {
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var fragment=new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,player.getMainHandItem().copy());
                player.serverLevel().sendParticles(fragment,player.getX(),player.getEyeY()-.6,player.getZ()+.6,64,.1,1.5,.1,0);
            });
        }
        if(profile.jam()&&t==232)
            check(DrinkMotion.assistance(profile,DrinkController.action(),DrinkController.age(1),DrinkController.duration())==0,"jam eating uses only the main hand");
        if(profile.jam()&&t==256) {
            check(DrinkMotion.assistance(profile,DrinkController.action(),DrinkController.age(1),DrinkController.duration())==0,"free hand remains hidden after eating");
            shot(mc,profile,"empty-jar-return");
        }
        if(t==313)shot(mc,profile,"replacement");
        if(profile.id().equals("papercraft_magic_decoration:guang_s")) {
            if(t==320)mc.player.setXRot(75);
            if(t==325)shot(mc,profile,"closed-can-shoulder");
            if(t==328)mc.player.setXRot(0);
        }
        if(t==121&&!profile.id().startsWith("farmingtales:")
            &&!PresentationOnly.active(profile)&&!profile.food())checkCocktail(mc,profile);
        if(t==332) {
            if(Boolean.getBoolean("farmingtales.eatingSmoke")) {
                check(eatingVariantStable,"selected eating style stays stable throughout action");
                check(eatingCameraSamples>10&&eatingCameraPeak>.06&&eatingCameraPeak<2.5,
                    "rendered eating world camera moves subtly, peak="+eatingCameraPeak+" degrees");
            }
            boolean presentationOnly=PresentationOnly.active(profile);
            check(mc.player.getMainHandItem().getCount()==(presentationOnly?3:2)&&!DrinkController.active(),
                presentationOnly?"presentation only keeps the whole stack":"drink consumes once and returns to idle");
            if((profile.bodyModel().startsWith("animation/world_bottles/")||profile.bodyModel().startsWith("animation/tavern_bottles/")))
                check(mc.player.getMainHandItem().hasTag()&&mc.player.getMainHandItem().getTag().getInt("BrewLevel")==6,
                    "drinking preserves remaining stack BrewLevel");
            if(StickyRiceCakeAnimations.owns(profile)) {
                check(sounds.stream().filter(s->s.equals("sticky_rice_cake_bite")).count()==3
                    &&sounds.stream().noneMatch(s->s.equals("gulp")||s.equals("slosh")),
                    "three bite sounds without drink gulps");
                check(sounds.stream().filter(s->s.equals("snack_swallow")).count()==3,
                    "every mouthful is swallowed");
            } else if(PlateFoodAnimations.owns(profile)) {
                // One bite and one swallow per portion the source mod ships a model for.
                int portions=PlateFoodAnimations.portions(profile);
                // Each dish names its own bite cue; the duration only moves the cue times,
                // so the shared timeline is asked for the name instead of hard coding one.
                String bite=PlateFoodAnimations.eatingSounds(profile,40).stream()
                    .map(c->c.event()).filter(e->e.endsWith("_bite")).findFirst().orElse("sticky_rice_cake_bite");
                check(sounds.stream().filter(s->s.equals(bite)).count()==portions
                    &&sounds.stream().noneMatch(s->s.equals("gulp")||s.equals("slosh")),
                    "one bite sound per portion without drink gulps");
                check(sounds.stream().filter(s->s.equals("snack_swallow")).count()==portions,
                    "every portion is swallowed");
            } else if(profile.food()) {
                check(sounds.stream().filter(s->s.equals("custard_eat")).count()==3
                    &&sounds.stream().noneMatch(s->s.equals("gulp")||s.equals("slosh")),
                    "three spoonful sounds without drink gulps");
                check(sounds.stream().filter(s->s.equals("inspect_turn")).count()==3,
                    "spoon contacts the custard before every mouthful");
            } else if(profile.jam()) {
                check(sounds.stream().filter(s->s.equals("jam_eat")).count()==2&&sounds.stream().noneMatch(s->s.equals("gulp")||s.equals("slosh")),"two jam mouthful sounds without water gulps");
                check(sounds.stream().noneMatch(s->s.equals("jam_pat"))&&sounds.stream().filter(s->s.equals("inspect_turn")).count()==4,"four single-hand shake cues without pat sounds");
                check(audioSamples.stream().filter(s->s.item().equals(current)&&s.event().equals("jam_eat")).allMatch(s->s.resource().contains(":jam/honey")),"jam uses local vanilla honey audio");
                if(Boolean.getBoolean("farmingtales.switchSmoke")&&profile.sweetJam()) {
                    var mouthfuls=audioSamples.stream().filter(s->s.item().equals(current)&&s.event().equals("jam_eat")).toList();
                    check(mouthfuls.size()==2&&mouthfuls.stream().anyMatch(s->Math.abs(s.volume()-.55f*1.35f*.8f*.65f)<.00001f)
                        &&mouthfuls.stream().anyMatch(s->Math.abs(s.volume()-.59f*1.35f*.8f*.65f)<.00001f),
                        "fruit jam eating sounds are 35 percent louder in the real sound engine");
                }
            } else {
                int mouthfuls=4;
                check(sounds.stream().filter(s->s.equals("gulp")).count()==mouthfuls,"drinking sounds match mouthfuls: "+mouthfuls);
                check(gulpVolumes.size()==mouthfuls&&gulpVolumes.stream().allMatch(v->v>=.23f&&v<=.29f),"drinking sounds retain the original 0.80 volume");
                if(ServingAnimations.chocolate(profile))
                    check(sounds.stream().filter(s->s.equals("chocolate_bite")).count()==2&&sounds.indexOf("chocolate_bite")<sounds.indexOf("gulp"),"chocolate eaten before drinking");
                if(ServingAnimations.pineapple(profile))check(sounds.stream().filter(s->s.equals("pineapple_bite")).count()==1
                    &&sounds.indexOf("pineapple_bite")<sounds.indexOf("gulp"),"pineapple eaten before drinking");
                if(ServingAnimations.popsicle(profile))check(sounds.stream().filter(s->s.equals("popsicle_bite")).count()==3
                    &&sounds.stream().filter(s->s.equals("stick_spit")).count()==1&&sounds.indexOf("stick_spit")<sounds.indexOf("gulp"),"three bites then spit before drinking");
            }
            var physical=audioSamples.stream().filter(s->s.item().equals(current)&&List.of("cork","cork_pull","can_open","cap_twist","cap_release").contains(s.event())).toList();
            if(profile.cup()||profile.food())check(physical.isEmpty(),"open cup or spoon food has no cap, cork or can-opening sounds");
            else check(!physical.isEmpty()&&physical.stream().allMatch(s->s.resource().contains(":balanced/")&&s.volume()>=.25f&&s.volume()<=.36f),"opening sounds use balanced samples near drinking loudness");
            check(profile.cork()||profile.jam()||profile.food()||sounds.stream().noneMatch(s->s.equals("cork")||s.equals("cork_pull")),"closure sound matches container");
            if(profile.can())check(sounds.stream().filter(s->s.equals("can_open")).count()==1,"one can opening sound");
            if(profile.style()==DrinkProfile.Style.BRANDY)check(sounds.stream().filter(s->s.equals("cap_twist")).count()==2&&sounds.contains("cap_release"),"XO unscrews and removes the cap");
            if(BottleClosures.screw(profile))check(sounds.stream().filter(s->s.equals("cap_twist")).count()==2&&sounds.contains("cap_release"),"specified screw cap twists twice and releases");
            if(profile.style()==DrinkProfile.Style.BEER)check(sounds.contains("cap_release")&&sounds.stream().noneMatch(s->s.equals("cap_twist")||s.equals("cork")),"beer lifts its short cap without a cork or screw sound");
            int bottles=mc.player.getInventory().countItem(Items.GLASS_BOTTLE);
            int expectedBottles=presentationOnly?0:(profile.wine()||profile.cup()?1:0);
            check(bottles==expectedBottles,"original container behavior wine="+profile.wine()+" cup="+profile.cup()+" bottles="+bottles);
            if(!presentationOnly) {mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
        }
        if(t==334&&!PresentationOnly.active(profile)) {
            check(DrinkController.vanillaUseActive()&&!DrinkController.active(),"ordinary right click keeps vanilla animation");
            mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);
        }
        if(t==338)mc.player.getInventory().selected=8;
        if(t==342)check(DrinkController.action()==BottleMotion.Action.STOW,"selecting empty slot starts this bottle's stow");
        if(t==342||t==346||t==350||t==354||t==358)shot(mc,profile,"stow-"+(t-338));
        if(t==366)check(!DrinkController.active()&&!DrinkController.renderable()&&mc.player.getMainHandItem().isEmpty(),"stow finishes with empty hand");
        if(profile.id().equals("kaleidoscope_world_liquor:dassai")&&t>=367) {
            checkDassaiViewMotion(mc,profile,t);
            if(t==882){index++;age=0;prepared=false;preparationTicks=0;}
            return;
        }
        if(Boolean.getBoolean("farmingtales.switchSmoke")&&t>=367) {
            checkFastSwitch(mc,profile,t);
            if(t==482){index++;age=0;prepared=false;preparationTicks=0;}
            return;
        }
        boolean water=profile.id().equals("farmingtales:purified_water");
        boolean placement=Boolean.getBoolean("farmingtales.placementSmoke")&&profile.id().equals("farmingtales:orange_juice_1");
        boolean surface=!profile.can()&&!water&&!placement;
        if(water)checkWaterRunningReturn(mc,profile,t);
        if(placement)checkBlockPlacement(mc,profile,t);
        if(surface) {
            if(t==370)mc.player.getInventory().selected=0;
            if(Boolean.getBoolean("farmingtales.letsdoLiquidSmoke")&&!profile.liquid().isEmpty()) {
                if(t==382){walkingMin=Double.POSITIVE_INFINITY;walkingMax=Double.NEGATIVE_INFINITY;walkingStart=mc.player.walkDist;mc.options.keyUp.setDown(true);}
                if(t>=386&&t<=398){double x=DrinkController.SLOSH.x(1);walkingMin=Math.min(walkingMin,x);walkingMax=Math.max(walkingMax,x);}
                if(t==390||t==398)shot(mc,profile,"walking-"+t);
                if(t==399){mc.options.keyUp.setDown(false);check(mc.player.walkDist>walkingStart+.1&&walkingMax-walkingMin>.01,"actual walking continuously moves the liquid");}
            }
            if(t>=400&&t<=464&&(t-400)%16==0) {
                float pitch=new float[]{-80,-25,0,25,80}[(t-400)/16];
                mc.player.setXRot(pitch);
            }
            if(t>=412&&t<=476&&(t-412)%16==0)shot(mc,profile,"surface-pitch-"+mc.player.getXRot());
            if(t==480){mc.player.setXRot(0);if(!profile.breweryMug()&&!Boolean.getBoolean("farmingtales.shaderSmoke"))mc.player.getInventory().selected=8;}
            if(profile.breweryMug()) {
                var reference=mc.player.blockPosition().relative(mc.player.getDirection(),2);
                if(t==480) {
                    mc.player.setXRot(35);
                    mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().overworld()
                        .setBlockAndUpdate(reference,((BlockItem)item).getBlock().defaultBlockState()));
                }
                if(t==494)shot(mc,profile,"source-comparison");
                if(t==498) {
                    mc.player.setXRot(0);mc.player.getInventory().selected=8;
                    mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().overworld()
                        .setBlockAndUpdate(reference,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()));
                }
            }
        }
        if(Boolean.getBoolean("farmingtales.shaderSmoke")&&surface) {
            String other=profile.id().equals("farmingtales:red_grape_juice_1")?"farmingtales:white_grape_juice_1":"farmingtales:red_grape_juice_1";
            if(t==480)mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).getInventory().setItem(1,new ItemStack(SmokeFixture.DRINKS.get(other).get(),3)));
            if(t==500)mc.player.getInventory().selected=1;
            if(t==504)check(DrinkController.action()==BottleMotion.Action.STOW&&DrinkController.profile().id().equals(profile.id()),"shader switch retains the old cup until it is stowed");
            if(t==526)check(!DrinkController.active()&&DrinkController.profile().id().equals(other),"shader switch draws and settles the next cup");
            if(t==530)mc.player.getInventory().selected=0;
            if(t==558)check(!DrinkController.active()&&DrinkController.profile().id().equals(profile.id()),"shader switch returns to the original cup");
            if(t>=496&&t<=566&&t%2==0)shot(mc,profile,"switch-"+t);
        }
        boolean sauceParticles=profile.jam()&&!profile.sweetJam();
        if(sauceParticles) {
            if(t==482)mc.player.getInventory().selected=0;
            if(t==516) {mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
            if(t==532) {
                check(DrinkController.vanillaUseActive()&&itemCrumbs(mc)>0,
                    "ordinary right click still produces vanilla eating particles");
                mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);
            }
            if(t==538)mc.player.getInventory().selected=8;
        }
        if(t==(sauceParticles?560:placement?580:water?490:surface?(Boolean.getBoolean("farmingtales.shaderSmoke")?580:500):370)) {index++;age=0;prepared=false;preparationTicks=0;}
        if(Boolean.getBoolean("farmingtales.capturePerformances")
            &&t%2==0&&t<=369)
            shot(mc,profile,String.format(java.util.Locale.ROOT,"motion-%03d",t));
    }
    private static void checkDassaiViewMotion(Minecraft mc,DrinkProfile profile,int t) {
        if(t==370){mc.player.getInventory().selected=0;ribbonViewYaw=mc.player.getYRot();}
        if(t<400||t>=880)return;
        String[] names={"look_left","look_right","look_up","look_down","walk_forward","walk_back","walk_left","walk_right"};
        int trial=(t-400)/60,phase=(t-400)%60;
        var keys=new net.minecraft.client.KeyMapping[]{mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight};
        if(phase==0) {
            for(var key:keys)key.setDown(false);
            mc.player.setYRot(ribbonViewYaw);mc.player.setXRot(0);
            viewRibbonPeak=0;ribbonWalkStart=mc.player.position();
        }
        if(trial<4&&phase>=16&&phase<=36) {
            float angle=(float)(35*Math.pow(Math.sin(Math.PI*(phase-16)/20),2));
            if(trial<2)mc.player.setYRot(ribbonViewYaw+(trial==0?-angle:angle));
            else mc.player.setXRot(trial==2?-angle:angle);
        }
        if(trial>=4&&phase==16)keys[trial-4].setDown(true);
        if(phase==36)for(var key:keys)key.setDown(false);
        if(phase>=16&&phase<=54) {
            try {
                var field=BottleRenderer.class.getDeclaredField("CAP_DECORATIONS");field.setAccessible(true);
                var state=(DecorationMotion.State)field.get(null);var part=DecorationMotion.parts(profile).get(0);
                var baked=mc.getModelManager().getModel(BottleRenderer.model(part.model()));
                for(var quad:baked.getQuads(null,null,net.minecraft.util.RandomSource.create(42))) {
                    var data=quad.getVertices();int stride=data.length/4;
                    for(int i=0;i<4;i++) {
                        var point=new org.joml.Vector3f(Float.intBitsToFloat(data[i*stride]),Float.intBitsToFloat(data[i*stride+1]),Float.intBitsToFloat(data[i*stride+2])).mul(16);
                        viewRibbonPeak=Math.max(viewRibbonPeak,point.distance(state.ribbonPoint(part,point)));
                    }
                }
            } catch(ReflectiveOperationException e){throw new RuntimeException(e);}
            if(phase%4==0)shot(mc,profile,"ribbon-"+names[trial]+"-"+phase);
        }
        if(phase==58) {
            check(DrinkController.action()==BottleMotion.Action.IDLE,"manual ribbon test uses idle hold "+names[trial]);
            if(trial>=4)check(mc.player.position().distanceTo(ribbonWalkStart)>.1,"actual player movement "+names[trial]);
            check(viewRibbonPeak>.05,"ribbon follows manual "+names[trial]+", pixels="+viewRibbonPeak);
        }
    }
    private static void checkFastSwitch(Minecraft mc,DrinkProfile profile,int t) {
        String other=profile.jam()?"farmingtales:orange_wine":"farmingtales:ancient_fruit_jam_1";
        if(t==370) {
            switchingSettledAt=-1;mc.player.getInventory().selected=0;
            mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())
                .getInventory().setItem(1,new ItemStack(SmokeFixture.DRINKS.get(other).get(),3)));
        }
        if(t==390) {check(!DrinkController.active(),"switch fixture begins in settled hold");mc.player.getInventory().selected=1;}
        if(t==391||t==393)check(DrinkController.action()==BottleMotion.Action.STOW&&DrinkController.profile().id().equals(profile.id()),
            "fast switch keeps the old food visible until stowed");
        if(t>390&&t<422&&!DrinkController.active()&&DrinkController.profile().id().equals(other)&&switchingSettledAt<0)
            switchingSettledAt=t;
        if(t==420) {
            int expected=19;
            check(switchingSettledAt>=390+expected&&switchingSettledAt<=391+expected,
                "fast switch reaches the new food in "+(switchingSettledAt-390)+" ticks");
        }
        if(t==422)mc.player.getInventory().selected=0;
        if(t==435)mc.player.getInventory().selected=1;
        if(t==480)check(!DrinkController.active()&&DrinkController.profile().id().equals(other),
            "rapid reselection during draw settles on the latest food");
        if(t>=390&&t<=420&&t%2==0)shot(mc,profile,"fast-switch-"+(t-390));
    }
    @SubscribeEvent public static void sound(PlaySoundSourceEvent e) {
        if(current.isEmpty()||e.getSound()==null)return;
        var key=e.getSound().getLocation();
        if(key.getNamespace().equals("farmingtales_drink_anim")) {
            audioSamples.add(new AudioSample(current,DrinkController.action().name(),key.getPath(),e.getSound().getSound().getLocation().toString(),e.getSound().getVolume(),e.getSound().getPitch()));
            sounds.add(key.getPath());
            if(key.getPath().equals("gulp"))gulpVolumes.add(e.getSound().getVolume());
            if(DrinkController.action()==BottleMotion.Action.INSPECT)
                inspectionSounds.add(new InspectionSound(key.getPath(),e.getSound().getVolume()));
        }
    }
    private static net.minecraft.core.BlockPos ground,shelf,blocked;
    private static void checkBlockPlacement(Minecraft mc,DrinkProfile profile,int t) {
        if(t==370) {
            mc.player.getInventory().selected=0;
            ground=mc.player.blockPosition().offset(-1,-1,-2);
            shelf=mc.player.blockPosition().offset(1,0,-2);
            blocked=mc.player.blockPosition().offset(0,-1,-2);
            mc.getSingleplayerServer().execute(()->{
                var level=mc.getSingleplayerServer().overworld();
                for(var pos:List.of(ground,shelf,blocked))level.setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(blocked.above(),net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState());
            });
        }
        if(t==402||t==438) {
            var pos=t==402?ground:shelf;
            var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos).add(0,.5,0),net.minecraft.core.Direction.UP,pos,false);
            var result=mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,hit);
            if(result.shouldSwing())mc.player.swing(InteractionHand.MAIN_HAND);
            check(result.consumesAction()&&!DrinkController.active(),(t==402?"ground":"table")+" successful placement uses vanilla swing without replacement draw");
        }
        if(t>=402&&t<=462&&t%2==0)shot(mc,profile,String.format(java.util.Locale.ROOT,"place-%03d",t));
        if(t==434)check(!DrinkController.active()&&mc.player.getMainHandItem().getCount()==1&&!mc.level.getBlockState(ground.above()).isAir(),"placing one cup consumes once and returns to the remaining cup");
        if(t==455) {
            check(!DrinkController.active()&&mc.player.getMainHandItem().isEmpty()&&!DrinkController.renderable()&&!mc.level.getBlockState(shelf.above()).isAir(),"placing last cup releases it and returns to empty hand");
            mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).getInventory().setItem(0,new ItemStack(SmokeFixture.DRINKS.get(profile.id()).get())));
        }
        if(t==490) {
            var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(blocked).add(0,.5,0),net.minecraft.core.Direction.UP,blocked,false);
            var result=mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,hit);
            // Edible BlockItem may fall back to vanilla drinking after placement fails.
            check(DrinkController.action()!=BottleMotion.Action.PLACE&&mc.player.getMainHandItem().getCount()==1
                &&mc.level.getBlockState(blocked.above()).is(net.minecraft.world.level.block.Blocks.BEDROCK),"failed placement does not animate or consume a cup");
            mc.gameMode.releaseUsingItem(mc.player);
        }
        if(t==508)mc.player.getInventory().selected=8;
        if(t==510)mc.getSingleplayerServer().execute(()->{
            mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).getInventory().setItem(0,new ItemStack(SmokeFixture.DRINKS.get(profile.id()).get(),3));
            for(var pos:List.of(ground,shelf))mc.getSingleplayerServer().overworld().setBlockAndUpdate(pos.above(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        });
        if(t==514)mc.player.getInventory().selected=0;
        if(t==535||t==539) {
            var pos=t==535?ground:shelf;
            var result=mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos).add(0,.5,0),net.minecraft.core.Direction.UP,pos,false));
            if(result.shouldSwing())mc.player.swing(InteractionHand.MAIN_HAND);
            if(t==539)check(!DrinkController.active()&&DrinkController.renderable(),"rapid placement keeps the remaining cup without a replacement draw");
        }
        if(t==565) {
            check(!DrinkController.active()&&mc.player.getMainHandItem().getCount()==1&&!mc.level.getBlockState(ground.above()).isAir()&&!mc.level.getBlockState(shelf.above()).isAir(),"rapid placements consume exactly two and recover once");
            mc.player.getInventory().selected=8;
        }
    }
    private static void checkWaterRunningReturn(Minecraft mc,DrinkProfile profile,int t) {
        if(t==370) {
            mc.player.getInventory().selected=0;mc.options.keyUp.setDown(true);mc.options.keySprint.setDown(true);
            previousRunningBottle=null;runningReturnContinuous=true;
        }
        if(t==400)DrinkController.start(BottleMotion.Action.INSPECT);
        if(t>=438&&t<=460) {
            var action=DrinkController.action();double at=DrinkController.age(1);
            var pose=DrinkMotion.sample(profile,action,at,16,true);
            pose=DrinkMotion.blendRunning(profile,action,pose,DrinkController.MOVEMENT.run(1)*DrinkMotion.idleBlend(profile,action,at,16,true));
            var rotation=BottleRig.rotation(pose,1);
            if(previousRunningBottle!=null&&Math.abs(previousRunningBottle.dot(rotation))<.99f)runningReturnContinuous=false;
            previousRunningBottle=rotation;
        }
        if(t>=400&&t<=480&&Boolean.getBoolean("farmingtales.capturePerformances"))
            shot(mc,profile,String.format(java.util.Locale.ROOT,"return-run-%03d",t-400));
        if(t==480) {
            check(mc.player.isSprinting()&&DrinkController.MOVEMENT.run(1)>.7,"water inspection returns during actual sprint movement");
            check(runningReturnContinuous&&previousRunningBottle!=null&&!DrinkController.active(),"water spin ends without unwinding and returns to running idle");
            mc.options.keyUp.setDown(false);mc.options.keySprint.setDown(false);
        }
    }
    private static void checkJarFamilyResources(Minecraft mc) {
        int count=0;
        var missing=mc.getModelManager().getMissingModel();
        for(var profile:DrinkCatalog.all()) {
            if(!profile.jam())continue;
            boolean present=SmokeFixture.DRINKS.get(profile.id()).get()!=net.minecraft.world.item.Items.AIR;
            for(String path:List.of(profile.bodyModel(),profile.capModel(),profile.neckModel())) {
                var baked=mc.getModelManager().getModel(BottleRenderer.model(path));
                present&=baked!=missing;
                int quads=0;
                var faces=new ArrayList<net.minecraft.core.Direction>(Arrays.asList(net.minecraft.core.Direction.values()));
                faces.add(null);
                for(var face:faces)for(var quad:baked.getQuads(null,face,net.minecraft.util.RandomSource.create(42))) {
                    quads++;
                    present&=!quad.getSprite().contents().name().getPath().equals("missingno");
                }
                present&=quads>0;
            }
            for(var region:profile.liquid()) {
                var expected=ResourceLocation.parse(region.texture().replace(":textures/",":").replace(".png",""));
                present&=mc.getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                    .apply(expected).contents().name().equals(expected);
            }
            check(present,"jar family models and original-texture liquid loaded "+profile.id());
            count++;
        }
        check(count==130,"all 130 jar resources loaded");
    }
    private static void press(int key) {
        var mapping=InputConstants.Type.KEYSYM.getOrCreate(key);
        net.minecraft.client.KeyMapping.set(mapping,true);net.minecraft.client.KeyMapping.click(mapping);
        MinecraftForge.EVENT_BUS.post(new InputEvent.Key(key,0,GLFW.GLFW_PRESS,0));
        net.minecraft.client.KeyMapping.set(mapping,false);
    }
    private static void checkCocktail(Minecraft mc,DrinkProfile profile) {
        check(mc.player.getMainHandItem().getUseDuration()==32,"cocktail fixture uses the original 32-tick consumption duration");
        var missing=mc.getModelManager().getMissingModel();
        var models=new ArrayList<String>();models.add(profile.bodyModel());
        for(var part:DecorationMotion.parts(profile))models.add(part.model());
        for(String path:models) {
            var baked=mc.getModelManager().getModel(BottleRenderer.model(path));
            check(baked!=missing,"cocktail and garnish model loaded "+path);
            var faces=new ArrayList<net.minecraft.core.Direction>(Arrays.asList(net.minecraft.core.Direction.values()));faces.add(null);
            int quads=0;boolean valid=true;
            for(var face:faces)for(var quad:baked.getQuads(null,face,net.minecraft.util.RandomSource.create(42))) {
                quads++;valid&=!quad.getSprite().contents().name().getPath().equals("missingno");
            }
            check(valid&&quads>0,"cocktail mesh has painted faces "+path);
        }
        for(var r:profile.liquid()) {
            var name=ResourceLocation.parse(r.texture().replace(":textures/",":").replace(".png",""));
            var sprite=mc.getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).apply(name);
            check(sprite.contents().name().equals(name),"cocktail liquid atlas loaded");
            if(profile.id().equals("kaleidoscope_tavern:mystery_cocktail"))
                check(sprite.contents().getUniqueFrames().count()==9,"mystery cocktail retains all nine animated color frames");
        }
        if(profile.id().equals("kaleidoscope_tavern:signature_cocktail")) {
            var a=mc.player.getMainHandItem().copy();var b=a.copy();
            a.getOrCreateTag().putInt("Color",0xE04020);b.getOrCreateTag().putInt("Color",0x20B0E0);
            check(LiquidTint.color(profile,a)==0xE04020&&LiquidTint.color(profile,b)==0x20B0E0,
                "signature liquid reads each recipe color without caching across stacks");
            a.getOrCreateTag().putInt("Color",0x40DD70);
            check(LiquidTint.color(profile,a)==0x40DD70,"signature liquid updates when the same stack color changes");
        }
    }
    private static boolean inspectionVolumeMatches(DrinkProfile profile) {
        var expected=new ArrayList<InspectionSound>();
        expected.add(new InspectionSound("cloth",.06f*.3f*.8f*1.7f));
        for(var cue:AnimationSoundTimeline.inspection(profile)) {
            // Negative sentinel: physical cues have a loudness range, fabric has exact legacy gain.
            boolean physical=cue.event().equals("inspect_liquid")||cue.event().equals("inspect_settle");
            expected.add(new InspectionSound(cue.event(),physical?-1:cue.volume()*(cue.event().equals("jam_tap")?1:.15f)*.8f*1.7f));
        }
        var actual=new ArrayList<>(inspectionSounds);
        if(actual.size()!=expected.size())return false;
        for(var want:expected) {
            int found=-1;
            for(int i=0;i<actual.size();i++) {
                var got=actual.get(i);
                float physicalGain=got.event().equals("inspect_liquid")?got.volume()*2:got.volume();
                boolean gain=want.volume()<0?physicalGain>=.239f&&physicalGain<=.329f:Math.abs(got.volume()-want.volume())<.00001f;
                if(got.event().equals(want.event())&&gain) {found=i;break;}
            }
            if(found<0)return false;
            actual.remove(found);
        }
        return actual.isEmpty();
    }
    private static void check(boolean value,String label) {
        String text=(value?"PASS ":"FAIL ")+current+" "+label;
        com.mojang.logging.LogUtils.getLogger().info("CATALOG_SMOKE {}",text);
        try {Files.writeString(result,text+"\n",StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}
    }
    private static int itemCrumbs(Minecraft mc) {
        // Ambient / block particles are unrelated to eating. Inspect active item
        // fragments specifically, including Forge's pending particle queue.
        var particles=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Object,Boolean>());
        try {
            for(var field:net.minecraft.client.particle.ParticleEngine.class.getDeclaredFields()) {
                if(java.lang.reflect.Modifier.isStatic(field.getModifiers()))continue;
                field.setAccessible(true);Object value=field.get(mc.particleEngine);
                if(value instanceof Map<?,?> map)for(Object queue:map.values()) {
                    if(queue instanceof Collection<?> collection)particles.addAll(collection);
                }
                if(value instanceof Collection<?> collection)particles.addAll(collection);
            }
        } catch(ReflectiveOperationException e) {throw new IllegalStateException(e);}
        var types=new java.util.TreeMap<String,Integer>();
        for(Object particle:particles)if(particle instanceof net.minecraft.client.particle.Particle)
            types.merge(particle.getClass().getSimpleName(),1,Integer::sum);
        com.mojang.logging.LogUtils.getLogger().info("CATALOG_PARTICLE_TYPES {} {}",current,types);
        return (int)particles.stream().filter(p->p instanceof net.minecraft.client.particle.BreakingItemParticle).count();
    }
    private static void shot(Minecraft mc,DrinkProfile p,String stage) {
        Screenshot.grab(mc.gameDirectory,(Boolean.getBoolean("farmingtales.shaderSmoke")?"shader-":"")+"v184-catalog-"+mc.getUser().getName()+"-"+p.id().split(":")[1]+"-"+stage+".png",mc.getMainRenderTarget(),c->{});
    }
}

