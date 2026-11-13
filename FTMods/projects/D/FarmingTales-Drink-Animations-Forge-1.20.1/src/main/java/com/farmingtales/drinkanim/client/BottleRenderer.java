package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.DrinkAnimations;
import com.farmingtales.drinkanim.DrinkConfig;
import com.farmingtales.drinkanim.math.BottleMotion;
import com.farmingtales.drinkanim.math.BottleRig;
import com.farmingtales.drinkanim.math.BottleFraming;
import com.farmingtales.drinkanim.math.LiquidMesh;
import com.farmingtales.drinkanim.math.DrinkProfile;
import com.farmingtales.drinkanim.math.DrinkCatalog;
import com.farmingtales.drinkanim.math.DrinkMotion;
import com.farmingtales.drinkanim.math.ContainerLiquid;
import com.farmingtales.drinkanim.math.DecorationMotion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid=DrinkAnimations.ID,value=Dist.CLIENT)
public final class BottleRenderer {
    private static final com.farmingtales.drinkanim.math.JamLiquid.State JAM_LIQUID=new com.farmingtales.drinkanim.math.JamLiquid.State();
    public static final ResourceLocation MODEL=ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,"animation/orange_bottle");
    public static final ResourceLocation CORK=ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,"animation/orange_cork");
    public static final ResourceLocation NECK=ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,"animation/orange_open_neck");
    private static final float SCALE=BottleRig.SCALE;
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,"textures/orange_wine.png");
    private static final DecorationMotion.State DECORATIONS=new DecorationMotion.State();
    private static final DecorationMotion.State CAP_DECORATIONS=new DecorationMotion.State();
    private static DrinkProfile decorationProfile;
    private static final RenderType LIQUID_LAYER=RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
    private static final MultiBufferSource.BufferSource TRANSLUCENT=MultiBufferSource.immediate(
        new com.mojang.blaze3d.vertex.BufferBuilder(65536));
    private static final int LIQUID_ALPHA=184;
    private static MultiBufferSource liquidBuffers(MultiBufferSource supplied) {
        // Oculus owns an unflushable hand batch. Submit translucency to that batch
        // so its opaque depth/material pass completes before the liquid is blended.
        String type=supplied.getClass().getName();
        return type.startsWith("net.irisshaders.batchedentityrendering.")
            ||type.startsWith("net.coderbot.batchedentityrendering.")?supplied:TRANSLUCENT;
    }
    private static net.minecraft.client.renderer.texture.TextureAtlasSprite liquidSprite(String texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
            .apply(ResourceLocation.parse(texture.replace(":textures/",":").replace(".png","")));
    }
    public static ResourceLocation model(String path) {return ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,path);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void hand(RenderHandEvent event) {
        var mc=Minecraft.getInstance();
        if(mc.player==null||!DrinkConfig.ENABLED.get()||!mc.options.getCameraType().isFirstPerson())return;
        if(DrinkController.vanillaUseActive())return;
        double age=DrinkController.age(event.getPartialTick());
        BottleMotion.Action rigAction=DrinkController.action();
        boolean replacement=DrinkController.replacementAvailable();
        DrinkProfile profile=DrinkController.profile();
        double assistance=DrinkMotion.assistance(profile,rigAction,age,DrinkController.duration());
        if(event.getHand()!=InteractionHand.MAIN_HAND) {
            if(assistance>0&&DrinkController.supports(DrinkController.renderStack(mc.player.getMainHandItem())))event.setCanceled(true);
            return;
        }
        ItemStack stack=DrinkController.renderStack(event.getItemStack());
        if(!DrinkController.supports(stack))return;
        event.setCanceled(true);
        // The complete rig (including its sorted translucency) is submitted once.
        // A transparent BlockItem would otherwise replay it in HAND_TRANSLUCENT.
        if(!ShaderHandPass.renderNow())return;
        if(!DrinkController.renderable())return;
        double time=mc.player.tickCount+event.getPartialTick();
        BottleMotion.Pose motion=DrinkMotion.sample(profile,rigAction,age,DrinkController.duration(),replacement);
        double idleBlend=DrinkMotion.idleBlend(profile,rigAction,age,DrinkController.duration(),replacement);
        motion=DrinkMotion.blendRunning(profile,rigAction,motion,DrinkController.MOVEMENT.run(event.getPartialTick())*idleBlend);
        motion=DrinkController.MOVEMENT.apply(motion,event.getPartialTick(),.25+.75*idleBlend);
        int side=mc.player.getMainArm()==HumanoidArm.RIGHT?1:-1;
        PoseStack pose=event.getPoseStack();pose.pushPose();
        ChocolateCrumbRenderer.draw(pose,event.getMultiBufferSource(),event.getPackedLight(),profile,rigAction,
            age,DrinkController.duration(),side,mc.player.getModelName().equals("slim"),DrinkController.eatingVariant());
        var camera=DrinkController.renderCamera(event.getPartialTick()).rotation();
        pose.mulPose(camera);
        if(!DrinkController.active())motion=com.farmingtales.drinkanim.math.PlacementMotion.swing(motion,event.getSwingProgress());
        var rig=BottleRig.solve(motion,rigAction,age,DrinkController.duration(),side,mc.player.getModelName().equals("slim"),profile);
        rig=com.farmingtales.drinkanim.math.EatingMotion.apply(rig,profile,rigAction,age,DrinkController.duration(),DrinkController.eatingVariant(),side);
        rig=com.farmingtales.drinkanim.math.BreathingMotion.apply(rig,time,side);
        if(decorationProfile!=profile){DECORATIONS.reset();CAP_DECORATIONS.reset();JAM_LIQUID.reset();decorationProfile=profile;}
        if(!mc.isPaused())DECORATIONS.advance(time/20,rig.bottle().position(),rig.bottle().rotation(),DrinkController.SLOSH.x(event.getPartialTick()),DrinkController.SLOSH.z(event.getPartialTick()));
        if(!mc.isPaused()&&DecorationMotion.parts(profile).stream().anyMatch(DecorationMotion.Part::capAttached)) {
            if(profile.id().equals("kaleidoscope_world_liquor:dassai")) {
                var view=mc.gameRenderer.getMainCamera();var eye=view.getPosition();
                CAP_DECORATIONS.advanceWithView(time/20,rig.cork().position(),rig.cork().rotation(),
                    new org.joml.Vector3d(eye.x,eye.y,eye.z),new Quaternionf(view.rotation()).mul(camera));
            } else CAP_DECORATIONS.advance(time/20,rig.cork().position(),rig.cork().rotation(),0,0);
        }
        rig=com.farmingtales.drinkanim.math.ServingAnimations.sway(profile,rigAction,age,rig,DECORATIONS.x(),DECORATIONS.z());
        var framing=BottleFraming.offset(rig,rigAction,age,DrinkController.duration(),replacement,profile,camera);
        float scale=profile.renderScale();
        pose.translate(framing.x,framing.y,framing.z);
        Quaternionf rotation=rig.bottle().rotation();
        Matrix4f bottleToView=new Matrix4f(pose.last().pose()).translate(rig.bottle().position()).scale(scale)
            .rotate(rotation).translate(-.5f,-.30f,-.5f).scale(1f/16);
        var handMask=HandClipper.Mask.create(bottleToView,profile);
        if(rigAction==BottleMotion.Action.PLACE&&!com.farmingtales.drinkanim.math.PlacementMotion.bottleVisible(age,replacement))
            handMask=new HandClipper.Mask(new Matrix4f(),java.util.List.of());
        if(!mc.player.isInvisible()) {
            renderGrip(pose,event.getMultiBufferSource(),event.getPackedLight(),side,rig.hand(),handMask);
        }
        if(rigAction==BottleMotion.Action.PLACE&&!com.farmingtales.drinkanim.math.PlacementMotion.bottleVisible(age,replacement)) {
            pose.popPose();return;
        }
        Vector3f cork=rig.cork().position();
        if(assistance>0) {
            if(!mc.player.isInvisible())renderGrip(pose,event.getMultiBufferSource(),event.getPackedLight(),-side,rig.helper(),handMask);
        }
        boolean corkVisible=DrinkMotion.capVisible(profile,rigAction,age,DrinkController.duration(),replacement);
        boolean corkDetached=DrinkMotion.capDetached(profile,rigAction,age);
        if(com.farmingtales.drinkanim.math.ServingAnimations.owns(profile)) {
            corkVisible=com.farmingtales.drinkanim.math.ServingAnimations.propVisible(profile,rigAction,age,DrinkController.duration(),replacement);
            corkDetached=true;
        }
        if(corkVisible&&corkDetached) {
            pose.pushPose();pose.translate(cork.x,cork.y,cork.z);pose.scale(scale,scale,scale);
            pose.mulPose(rig.cork().rotation());
            pose.translate(-profile.capPivot()[0]/16,-profile.capPivot()[1]/16,-profile.capPivot()[2]/16);
            String servingModel=com.farmingtales.drinkanim.math.ServingAnimations.owns(profile)
                ?com.farmingtales.drinkanim.math.ServingAnimations.propModel(profile,rigAction,age):profile.capModel();
            if(com.farmingtales.drinkanim.math.BottleClosures.longCork(profile))
                NeckRenderer.cork(pose,event.getMultiBufferSource(),event.getPackedLight(),profile,DrinkMotion.capLift(profile,age));
            else mc.getItemRenderer().renderModelLists(mc.getModelManager().getModel(model(servingModel)),stack,event.getPackedLight(),OverlayTexture.NO_OVERLAY,pose,event.getMultiBufferSource().getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)));
            drawDecorations(pose,event.getMultiBufferSource(),event.getPackedLight(),stack,profile,CAP_DECORATIONS,true,new LiquidMesh.V(0,1,0),motion.fill());
            pose.popPose();
        }
        Vector3f pivot=rig.bottle().position();
        pose.translate(pivot.x,pivot.y,pivot.z);pose.scale(scale,scale,scale);
        pose.mulPose(rotation);pose.translate(0,-.30,0);
        double sx=DrinkController.SLOSH.x(event.getPartialTick()),sz=DrinkController.SLOSH.z(event.getPartialTick());
        double ripple=(.012+Math.min(.06,DrinkController.SLOSH.energy()*.08))*Math.sin(time*.48);
        LiquidMesh.V normal=com.farmingtales.drinkanim.math.LiquidSurface.normal(profile,rotation,mc.player.getXRot(),sx,sz,ripple);
        if(profile.jam()) {
            if(decorationProfile!=profile)JAM_LIQUID.reset();
            normal=JAM_LIQUID.sample(time/20,rotation,mc.player.getXRot(),sx,sz);
        }
        normal=com.farmingtales.drinkanim.math.LiquidSurface.contained(profile,normal,motion.fill());
        drawBottle(pose,event.getMultiBufferSource(),event.getPackedLight(),stack,normal,motion.fill(),time,profile,
            bodyModel(profile,rigAction,age,DrinkController.duration(),replacement));
        pose.pushPose();pose.translate(-.5,0,-.5);
        drawDecorations(pose,event.getMultiBufferSource(),event.getPackedLight(),stack,profile,DECORATIONS,false,normal,motion.fill());
        pose.popPose();
        if(profile.can())drawCanFlap(pose,event.getMultiBufferSource(),event.getPackedLight(),stack,profile,
            DrinkMotion.canOpening(rigAction,age,DrinkController.duration(),replacement));
        if(corkVisible&&!corkDetached) {
            pose.pushPose();pose.translate(-.5,profile.neckJoin(),-.5);
            if(profile.can()) {
                float[] hinge=profile.capPivot();
                pose.translate(hinge[0]/16,hinge[1]/16,hinge[2]/16);
                pose.mulPose(new Quaternionf().rotateX((float)Math.toRadians(DrinkMotion.tabAngle(rigAction,age,DrinkController.duration()))));
                pose.translate(-hinge[0]/16,-hinge[1]/16,-hinge[2]/16);
            }
            if(com.farmingtales.drinkanim.math.BottleClosures.longCork(profile))
                NeckRenderer.cork(pose,event.getMultiBufferSource(),event.getPackedLight(),profile,0);
            else mc.getItemRenderer().renderModelLists(mc.getModelManager().getModel(model(profile.capModel())),stack,event.getPackedLight(),OverlayTexture.NO_OVERLAY,pose,event.getMultiBufferSource().getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)));
            drawDecorations(pose,event.getMultiBufferSource(),event.getPackedLight(),stack,profile,CAP_DECORATIONS,true,normal,motion.fill());
            pose.popPose();
        }
        if(DrinkMotion.neckReveal(profile,rigAction,age,DrinkController.duration(),replacement)>0) {
            pose.pushPose();pose.translate(-.5,profile.neckJoin(),-.5);
            NeckRenderer.render(pose,event.getMultiBufferSource(),event.getPackedLight(),stack,DrinkMotion.neckReveal(profile,rigAction,age,DrinkController.duration(),replacement),profile);
            pose.popPose();
        }
        // Write opaque depth first. All liquid and ice use one atlas and one sorted
        // translucent batch, so an earlier liquid pass cannot hide submerged ice.
        if(liquidBuffers(event.getMultiBufferSource())==TRANSLUCENT) {
            if(event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource source)source.endBatch();
            TRANSLUCENT.endBatch();
        }
        pose.popPose();
    }
    /** Pose uses the owning body/cap's original model coordinates, including during removal. */
    private static void drawDecorations(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,
            DrinkProfile profile,DecorationMotion.State state,boolean capAttached,LiquidMesh.V normal,double fill) {
        var mc=Minecraft.getInstance();
        for(var part:DecorationMotion.parts(profile)) {
            if(part.capAttached()!=capAttached)continue;
            if(part.ribbon()!=null&&!part.ribbon().rigid()) {
                RibbonRenderer.render(mc.getModelManager().getModel(model(part.model())),part,state,pose,
                    buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)),light);
                continue;
            }
            var transform=state.transform(part,profile,normal,fill,DrinkController.action(),
                DrinkController.age(mc.getFrameTime()),DrinkController.duration());
            float[] root=part.pivot();var offset=transform.offset();
            pose.pushPose();pose.translate((root[0]+offset.x)/16,(root[1]+offset.y)/16,(root[2]+offset.z)/16);
            pose.mulPose(transform.rotation());pose.translate(-root[0]/16,-root[1]/16,-root[2]/16);
            mc.getItemRenderer().renderModelLists(mc.getModelManager().getModel(model(part.model())),stack,light,OverlayTexture.NO_OVERLAY,
                pose,part.kind()==DecorationMotion.Kind.ICE?new AlphaConsumer(liquidBuffers(buffers).getBuffer(LIQUID_LAYER),.68f)
                    :buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)));
            pose.popPose();
        }
    }
    public static void drawBottle(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,LiquidMesh.V normal,double fill,double time) {
        drawBottle(pose,buffers,light,stack,normal,fill,time,DrinkCatalog.ORANGE);
    }
    public static void drawBottle(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,LiquidMesh.V normal,double fill,double time,DrinkProfile profile) {
        drawBottle(pose,buffers,light,stack,normal,fill,time,profile,profile.bodyModel());
    }
    /** The held geometry is the closed body, the opened neck, or the current bite stage of a handheld food. */
    private static String bodyModel(DrinkProfile profile,BottleMotion.Action action,double age,int duration,boolean replacement) {
        if(com.farmingtales.drinkanim.math.StickyRiceCakeAnimations.owns(profile))
            return com.farmingtales.drinkanim.math.StickyRiceCakeAnimations.bodyModel(action,age,duration,replacement);
        if(com.farmingtales.drinkanim.math.PlateFoodAnimations.owns(profile))
            return com.farmingtales.drinkanim.math.PlateFoodAnimations.bodyModel(profile,action,age,duration,replacement);
        return profile.jam()&&com.farmingtales.drinkanim.math.JamAnimations.open(action,age,duration,replacement)
            ?profile.neckModel():profile.bodyModel();
    }
    private static void drawBottle(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,LiquidMesh.V normal,double fill,double time,DrinkProfile profile,String bodyModel) {
        if(profile==DrinkCatalog.ORANGE)drawLiquid(pose,buffers,light,normal,fill,time);
        else drawContainerLiquid(pose,buffers,light,normal,fill,time,profile,LiquidTint.color(profile,stack));
        pose.pushPose();pose.translate(-.5,0,-.5);
        var mc=Minecraft.getInstance();
        // Inward glass exposes the liquid; explicit rim faces keep the mouth visible.
        var bodyLayer=RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
        mc.getItemRenderer().renderModelLists(mc.getModelManager().getModel(model(bodyModel)),stack,light,OverlayTexture.NO_OVERLAY,pose,buffers.getBuffer(bodyLayer));
        pose.popPose();
    }
    private static void drawContainerLiquid(PoseStack pose,MultiBufferSource buffers,int light,LiquidMesh.V normal,double fill,double time,DrinkProfile profile,int color) {
        if(fill<=0||profile.liquid().isEmpty())return;
        if(com.farmingtales.drinkanim.math.CoconutAnimations.owns(profile)&&fill<=.025001)return;
        pose.pushPose();
        if(profile.can()) {
            var lid=profile.canLid();float[] origin=lid.origin();
            pose.translate((origin[0]-8)/16,0,(origin[2]-8)/16);
            pose.mulPose(new Quaternionf().rotateY((float)Math.toRadians(lid.yaw())));
            pose.translate(-(origin[0]-8)/16,0,-(origin[2]-8)/16);
            Vector3f local=new Quaternionf().rotateY((float)Math.toRadians(-lid.yaw())).transform(new Vector3f((float)normal.x(),(float)normal.y(),(float)normal.z()));
            normal=new LiquidMesh.V(local.x,local.y,local.z).unit();
        }
        // Optional liquid orientation also fits the Tavern bottles' rotated glass.
        if(profile.cupShape()!=null) {
            float yaw=(float)Math.toRadians(profile.cupShape().liquidYaw());
            pose.mulPose(new Quaternionf().rotateY(yaw));
            Vector3f local=new Quaternionf().rotateY(-yaw).transform(new Vector3f((float)normal.x(),(float)normal.y(),(float)normal.z()));
            normal=new LiquidMesh.V(local.x,local.y,local.z).unit();
        }
        double level=ContainerLiquid.level(profile.liquid(),normal,fill);
        // Brewery mugs use their opaque source pixels, including the moving head.
        var contentBuffers=profile.breweryMug()?buffers:liquidBuffers(buffers);
        var contentLayer=profile.breweryMug()?RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS):LIQUID_LAYER;
        for(var region:profile.liquid()) {
            var buffer=contentBuffers.getBuffer(contentLayer);
            var sprite=liquidSprite(region.texture());
            var faces=ContainerLiquid.exposedFaces(profile.liquid(),region,normal,level);
            for(var face:faces) {
                var points=face.points();
                for(int i=1;i<points.size()-1;i++)for(var point:java.util.List.of(points.get(0),points.get(i),points.get(i+1),points.get(i+1))) {
                    var uv=com.farmingtales.drinkanim.math.LiquidUV.sample(region,face,point);
                    var n=face.normal();
                    buffer.vertex(pose.last().pose(),(float)point.x(),(float)point.y(),(float)point.z()).color((color>>16)&255,(color>>8)&255,color&255,profile.jam()?(face.surface()?216:238):profile.liquidAlpha())
                        .uv(sprite.getU(uv.u()*16),sprite.getV(uv.v()*16))
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.last().normal(),(float)n.x(),(float)n.y(),(float)n.z()).endVertex();
                }
            }
        }
        if(com.farmingtales.drinkanim.math.BeerFoam.supports(profile)) {
            var region=com.farmingtales.drinkanim.math.BeerFoam.region(profile);
            var sprite=liquidSprite(region.texture());
            var buffer=contentBuffers.getBuffer(contentLayer);
            for(var face:com.farmingtales.drinkanim.math.BeerFoam.mesh(profile,normal,fill,time)) {
                var points=face.points();var n=face.normal();
                for(int i=1;i<points.size()-1;i++)for(var point:java.util.List.of(points.get(0),points.get(i),points.get(i+1),points.get(i+1))) {
                    var uv=com.farmingtales.drinkanim.math.LiquidUV.sample(region,face,point);
                    buffer.vertex(pose.last().pose(),(float)point.x(),(float)point.y(),(float)point.z()).color(255,255,255,255)
                        .uv(sprite.getU(uv.u()*16),sprite.getV(uv.v()*16)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                        .normal(pose.last().normal(),(float)n.x(),(float)n.y(),(float)n.z()).endVertex();
                }
            }
        }
        pose.popPose();
    }
    private static void drawCanFlap(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,DrinkProfile profile,double opening) {
        var lid=profile.canLid();float[] origin=lid.origin(),hinge=lid.hinge();
        pose.pushPose();pose.translate(-.5,0,-.5);
        pose.translate(origin[0]/16,0,origin[2]/16);
        pose.mulPose(new Quaternionf().rotateY((float)Math.toRadians(lid.yaw())));
        pose.translate((hinge[0]-origin[0])/16,hinge[1]/16,(hinge[2]-origin[2])/16);
        pose.mulPose(new Quaternionf().rotateX((float)Math.toRadians(-80*opening)));
        pose.translate(-hinge[0]/16,-hinge[1]/16,-hinge[2]/16);
        Minecraft.getInstance().getItemRenderer().renderModelLists(Minecraft.getInstance().getModelManager().getModel(model(lid.flapModel())),stack,
            light,OverlayTexture.NO_OVERLAY,pose,buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)));
        pose.popPose();
    }
    private static void renderGrip(PoseStack pose,MultiBufferSource buffers,int light,int side,BottleRig.Transform transform,HandClipper.Mask mask) {
        var mc=Minecraft.getInstance();
        pose.pushPose();
        Vector3f grip=transform.position();
        pose.translate(grip.x,grip.y,grip.z);
        pose.mulPose(transform.rotation());
        float armScale=BottleRig.ARM_SCALE;
        pose.scale(armScale,armScale,armScale);
        boolean slim=mc.player.getModelName().equals("slim");
        if(ModelAnimationCompat.renderModelArm(pose,buffers,light,side,slim,mask)) {
            pose.popPose();return;
        }
        var arm=VanillaHands.part(slim,side,false);
        var sleeve=VanillaHands.part(slim,side,true);
        var armPose=arm.storePose();var sleevePose=sleeve.storePose();
        boolean armVisible=arm.visible,sleeveVisible=sleeve.visible;
        try {
            // Work in the palm's own space. PlayerRenderer.setupAnim would add vanilla
            // shoulder offsets and arm sway on top of the authored forearm transform.
            arm.loadPose(PartPose.ZERO);sleeve.loadPose(PartPose.ZERO);
            arm.visible=true;sleeve.visible=true;
            VanillaHands.toPalmSpace(pose,slim,side);
            var skin=mc.player.getSkinTextureLocation();
            arm.render(pose,new HandClipper(buffers.getBuffer(RenderType.entitySolid(skin)),mask),light,OverlayTexture.NO_OVERLAY);
            if(mc.player.isModelPartShown(side>0?PlayerModelPart.RIGHT_SLEEVE:PlayerModelPart.LEFT_SLEEVE))
                sleeve.render(pose,new HandClipper(buffers.getBuffer(RenderType.entityTranslucent(skin)),mask),light,OverlayTexture.NO_OVERLAY);
        } finally {
            arm.loadPose(armPose);sleeve.loadPose(sleevePose);
            arm.visible=armVisible;sleeve.visible=sleeveVisible;
            pose.popPose();
        }
    }
    private static void drawLiquid(PoseStack pose,MultiBufferSource buffers,int light,LiquidMesh.V normal,double fill,double time) {
        VertexConsumer buffer=liquidBuffers(buffers).getBuffer(LIQUID_LAYER);
        for(var face:LiquidMesh.build(normal,fill)) {
            var points=face.points();
            for(int i=1;i<points.size()-1;i++) {
                vertex(pose,buffer,points.get(0),face,light,time);
                vertex(pose,buffer,points.get(i),face,light,time);
                vertex(pose,buffer,points.get(i+1),face,light,time);
                vertex(pose,buffer,points.get(i+1),face,light,time);
            }
        }
        // A narrow, pulsing column of wine travels through the neck while the bottle is inverted.
        if(fill>.025&&fill<.7&&normal.y()<.35) {
            double radius=.017+.003*Math.sin(time*1.8);
            LiquidMesh.V n=new LiquidMesh.V(0,0,-1);
            var stream=new LiquidMesh.Face(java.util.List.of(),n,false);
            for(int i=0;i<4;i++) {
                double a=i*Math.PI/2,b=(i+1)*Math.PI/2;
                var p=new LiquidMesh.V(Math.cos(a)*radius,.66,Math.sin(a)*radius);
                var q=new LiquidMesh.V(Math.cos(b)*radius,.66,Math.sin(b)*radius);
                vertex(pose,buffer,p,stream,light,time);vertex(pose,buffer,q,stream,light,time);
                vertex(pose,buffer,new LiquidMesh.V(q.x(),.98,q.z()),stream,light,time);
                vertex(pose,buffer,new LiquidMesh.V(p.x(),.98,p.z()),stream,light,time);
            }
        }
    }
    private static void vertex(PoseStack pose,VertexConsumer buffer,LiquidMesh.V p,LiquidMesh.Face face,int light,double time) {
        double across=Math.abs(face.normal().x())>.5?p.z():p.x();
        double u=4+(across/LiquidMesh.WIDTH+.5)*3;
        double v=8-(p.y()-LiquidMesh.BOTTOM)/LiquidMesh.HEIGHT*8;
        if(face.surface()) {
            u=7.3+(p.x()/LiquidMesh.WIDTH+.5)*2.4+Math.sin(time*.32)*.18;
            v=12.3+(p.z()/LiquidMesh.WIDTH+.5)*2.4+Math.cos(time*.38)*.18;
        }
        var n=face.normal();
        var sprite=liquidSprite(TEXTURE.toString());
        buffer.vertex(pose.last().pose(),(float)p.x(),(float)p.y(),(float)p.z())
            .color(255,255,255,LIQUID_ALPHA).uv(sprite.getU(u),sprite.getV(v))
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(pose.last().normal(),(float)n.x(),(float)n.y(),(float)n.z()).endVertex();
    }
}
