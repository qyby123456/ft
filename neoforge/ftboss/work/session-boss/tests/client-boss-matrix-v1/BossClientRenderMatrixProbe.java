package ftboss.clientfxprobe;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.lwjgl.opengl.GL11;

@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class BossClientRenderMatrixProbe {
    private static final Map<String,Object> report=new ConcurrentHashMap<>();
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> views=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final String[] DIRECTIONS={"front","right","back","left"};
    private static final int SUBJECTS=34;
    private static final int EXPECTED_VIEWS=SUBJECTS*4;
    private static volatile int caseIndex=-1;
    private static volatile boolean baselineCaptured;
    private static volatile boolean actorSpawned;
    private static volatile boolean activeCaptured;
    private static volatile UUID actorUuid;
    private static volatile double cameraX;
    private static volatile double cameraZ;
    private static volatile float cameraYaw;
    private static volatile float cameraPitch;
    private static volatile long caseStarted;
    private static volatile int actorRenderReturns;
    private static LivingEntity actor;
    private static int clientTicks;
    private static int stableTicks;
    private static int observedCase=-1;
    private static boolean worldRequested;
    private static boolean serverPrepared;
    private static NativeImage baselineImage;

    public BossClientRenderMatrixProbe() {
        NeoForge.EVENT_BUS.addListener(BossClientRenderMatrixProbe::clientTick);
        NeoForge.EVENT_BUS.addListener(BossClientRenderMatrixProbe::serverTick);
        NeoForge.EVENT_BUS.addListener(BossClientRenderMatrixProbe::render);
        NeoForge.EVENT_BUS.addListener(BossClientRenderMatrixProbe::renderReturned);
        report.put("scope","Actual idle body/model/texture rendering for 12 Manor variants in normal/impossible difficulty and 10 non-Manor Boss types, four real camera directions. No authenticated entry, multiplayer, actual attacks or full battle acceptance.");
        report.put("expectedViews",EXPECTED_VIEWS);
        report.put("authenticatedEntryAccepted",false);report.put("authenticatedMultiplayerAccepted",false);
        report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);
    }

    private static void require(boolean condition,String name) {
        if(!condition) throw new AssertionError(name);
        checks.add(name);
    }

    private static EntityType<?> subjectType(int subject) {
        if(subject<24) return FTEntities.EASTER_BOSS.get();
        return switch(subject-24) {
            case 0 -> FTEntities.TIDE_FISH_BOSS.get();
            case 1 -> FTEntities.SUGAR_QUEEN.get();
            case 2 -> FTEntities.MEAT_MOUNTAIN.get();
            case 3 -> FTEntities.WINE_BARREL.get();
            case 4 -> FTEntities.RAT_BOSS.get();
            case 5 -> FTEntities.DEFECT_CORE.get();
            case 6 -> FTEntities.APPLE_BOSS.get();
            case 7 -> FTEntities.COMPOST_BOSS.get();
            case 8 -> FTEntities.TEA_TREE.get();
            case 9 -> FTEntities.BENGER.get();
            default -> throw new AssertionError("Unexpected original Boss subject "+subject);
        };
    }

    private static void configureCase(ServerLevel level,ServerPlayer player,int index) {
        if(actor!=null) {actor.discard();actor=null;}
        caseIndex=index;baselineCaptured=false;actorSpawned=false;activeCaptured=false;actorUuid=null;actorRenderReturns=0;
        LivingEntity next=(LivingEntity)subjectType(index/4).create(level);
        require(next!=null,"ActualRegisteredBossConstruction"+index);
        if(next instanceof EasterBossEntity manor) {
            manor.setVariant((index/4)%12);
            manor.setChallengeDifficulty(index/4<12?ManorDifficulty.NORMAL:ManorDifficulty.IMPOSSIBLE);
            manor.setMini(false);manor.setGiant(false);
        }
        if(next instanceof Mob mob) {mob.setNoAi(true);mob.setPersistenceRequired();}
        next.setNoGravity(true);next.setInvulnerable(true);next.setCustomNameVisible(false);
        next.moveTo(0.5,70.0,0.5,0.0F,0.0F);
        actor=next;
        double distance=12.0+next.getBbHeight()*0.7;
        int direction=index%4;
        cameraX=0.5+(direction==1?distance:direction==3?-distance:0.0);
        cameraZ=0.5+(direction==2?distance:direction==0?-distance:0.0);
        cameraYaw=direction*90.0F;
        cameraPitch=(float)Math.toDegrees(Math.atan2(1.62-next.getBbHeight()*0.5,distance));
        player.teleportTo(level,cameraX,70.0,cameraZ,cameraYaw,cameraPitch);
        player.setNoGravity(true);player.setInvulnerable(true);
        caseStarted=level.getGameTime();
    }

    private static void serverTick(ServerTickEvent.Post event) {
        if(finished.get() || !event.getServer().isSingleplayer()) return;
        try {
            if(event.getServer().getPlayerList().getPlayerCount()!=1) return;
            ServerPlayer player=event.getServer().getPlayerList().getPlayers().getFirst();
            ServerLevel level=player.serverLevel();
            if(!serverPrepared) {
                require(player.getGameProfile().getName().equals("ft_fx_probe"),"ExplicitOfflineLocalFixture");
                for(int horizontal=-36;horizontal<=36;horizontal++) for(int depth=-36;depth<=36;depth++) level.setBlock(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState(),3);
                level.setDayTime(6000);level.setWeatherParameters(24000,0,false,false);
                serverPrepared=true;configureCase(level,player,0);return;
            }
            if(activeCaptured) {
                if(caseIndex+1==EXPECTED_VIEWS) {actor.discard();finish(null);return;}
                configureCase(level,player,caseIndex+1);return;
            }
            if(baselineCaptured && !actorSpawned) {
                actorUuid=actor.getUUID();actorSpawned=true;
                require(level.addFreshEntity(actor),"ActualOriginalBossAdmitted"+caseIndex);
            }
            if(level.getGameTime()-caseStarted>260) throw new AssertionError("Actual body framebuffer missing for case "+caseIndex+" uuid="+actorUuid+" renderReturns="+actorRenderReturns);
        } catch(Throwable failure) {finish(failure);}
    }

    private static void clientTick(ClientTickEvent.Post event) {
        if(finished.get()) return;
        try {
            Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;client.options.hideGui=true;
            if(++clientTicks>12000) throw new AssertionError("Native Boss matrix tick budget exceeded");
            if(Files.exists(Path.of("normal-stop-request.json"))) throw new AssertionError("Explicit normal stop requested");
            if(!worldRequested && client.level==null && client.getOverlay()==null && client.screen instanceof TitleScreen) {
                worldRequested=true;
                GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
                LevelSettings settings=new LevelSettings("Boss actual GL matrix",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("boss-gl-native",settings,new WorldOptions(130142026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);
            }
            if(observedCase!=caseIndex) {observedCase=caseIndex;stableTicks=0;if(baselineImage!=null){baselineImage.close();baselineImage=null;}}
            if(client.player!=null && client.level!=null && caseIndex>=0) {
                client.player.setYRot(cameraYaw);client.player.setXRot(cameraPitch);
                if(Math.abs(client.player.getX()-cameraX)<0.2 && Math.abs(client.player.getZ()-cameraZ)<0.2) stableTicks++;
                else stableTicks=0;
            }
            if(clientTicks%100==0) Files.writeString(Path.of("client-progress.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("clientTicks",clientTicks,"caseIndex",caseIndex,"completedViews",views.size(),"baselineCaptured",baselineCaptured,"actorSpawned",actorSpawned,"screenClass",client.screen==null?"none":client.screen.getClass().getName(),"hasPlayer",client.player!=null,"hasLevel",client.level!=null)));
        } catch(Throwable failure) {finish(failure);}
    }

    private static void renderReturned(RenderLivingEvent.Post event) {
        if(actorUuid!=null && event.getEntity().getUUID().equals(actorUuid)) actorRenderReturns++;
    }

    private static void render(RenderFrameEvent.Post event) {
        if(finished.get()) return;
        try {
            Minecraft client=Minecraft.getInstance();
            if(client.player==null || client.level==null || client.screen!=null || caseIndex<0 || stableTicks<25 || activeCaptured) return;
            require(RenderSystem.isOnRenderThread(),"ActualGlThread"+caseIndex);
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"NoGlErrorBeforeCase"+caseIndex);
            String prefix=String.format("boss-%03d-%s",caseIndex,DIRECTIONS[caseIndex%4]);
            if(!baselineCaptured) {
                baselineImage=Screenshot.takeScreenshot(client.getMainRenderTarget());
                baselineImage.writeToFile(Path.of(prefix+"-baseline.png"));
                baselineCaptured=true;return;
            }
            if(!actorSpawned || actorRenderReturns<3) return;
            Entity visible=null;
            for(Entity candidate:client.level.entitiesForRendering()) if(candidate.getUUID().equals(actorUuid)) {visible=candidate;break;}
            if(!(visible instanceof LivingEntity living) || visible.tickCount<8) return;
            EntityRenderer renderer=client.getEntityRenderDispatcher().getRenderer(living);
            ResourceLocation texture=renderer.getTextureLocation(living);
            boolean resourcePresent=client.getResourceManager().getResource(texture).isPresent();
            require(resourcePresent || !texture.getNamespace().equals("ftboss"),"ActualBossTextureResourcePresent"+caseIndex+":"+texture);
            try(NativeImage image=Screenshot.takeScreenshot(client.getMainRenderTarget())) {
                require(image.getWidth()==baselineImage.getWidth() && image.getHeight()==baselineImage.getHeight(),"ExactFramebufferSize"+caseIndex);
                long changed=0;
                int left=image.getWidth()/5;int right=image.getWidth()*4/5;int top=40;int bottom=image.getHeight()*3/4;
                for(int vertical=top;vertical<bottom;vertical++) for(int horizontal=left;horizontal<right;horizontal++) {
                    int before=baselineImage.getPixelRGBA(horizontal,vertical);int after=image.getPixelRGBA(horizontal,vertical);
                    int red=Math.abs((before&255)-(after&255));int green=Math.abs(((before>>>8)&255)-((after>>>8)&255));int blue=Math.abs(((before>>>16)&255)-((after>>>16)&255));
                    if(red+green+blue>90) changed++;
                }
                image.writeToFile(Path.of(prefix+"-active.png"));
                require(changed>100,"ActualBossBodyAddsWorldPixels"+caseIndex+":"+changed);
                require(GL11.glGetError()==GL11.GL_NO_ERROR,"NoGlErrorAfterCase"+caseIndex);
                Map<String,Object> row=new LinkedHashMap<>();
                row.put("caseIndex",caseIndex);row.put("subject",caseIndex/4);row.put("direction",DIRECTIONS[caseIndex%4]);
                row.put("type",BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).toString());row.put("uuid",living.getUUID().toString());
                row.put("variant",living instanceof EasterBossEntity manor?manor.getVariant():-1);row.put("difficulty",caseIndex/4<12?"normal":caseIndex/4<24?"impossible":"non-Manor");
                row.put("rendererClass",renderer.getClass().getName());row.put("texture",texture.toString());row.put("textureResourcePresent",resourcePresent);
                row.put("changedWorldPixels",changed);row.put("worldOnlyRoi",List.of(left,top,right,bottom));row.put("actualLivingRenderReturns",actorRenderReturns);
                row.put("baseline",Path.of(prefix+"-baseline.png").toAbsolutePath().toString());row.put("active",Path.of(prefix+"-active.png").toAbsolutePath().toString());
                row.put("glRenderer",GL11.glGetString(GL11.GL_RENDERER));row.put("glVersion",GL11.glGetString(GL11.GL_VERSION));
                views.add(row);activeCaptured=true;
                Files.writeString(Path.of("completed-boss-views.json"),new GsonBuilder().setPrettyPrinting().create().toJson(views));
            }
        } catch(Throwable failure) {finish(failure);}
    }

    private static void finish(Throwable failure) {
        if(!finished.compareAndSet(false,true)) return;
        Minecraft.getInstance().execute(()->{
            try {
                if(failure!=null) failure.printStackTrace();
                if(baselineImage!=null) {baselineImage.close();baselineImage=null;}
                report.put("failures",failure==null?0:1);if(failure!=null)report.put("failure",failure.toString());
                report.put("checks",new ArrayList<>(checks));report.put("views",new ArrayList<>(views));report.put("completedViewCount",views.size());
                report.put("actualBossMatrixAccepted",failure==null && views.size()==EXPECTED_VIEWS);report.put("clientTicks",clientTicks);
                Files.writeString(Path.of("actual-client-boss-matrix.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            } catch(Exception saving) {saving.printStackTrace();}
            Minecraft.getInstance().stop();
        });
    }
}
