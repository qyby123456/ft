package ftboss.clientfxprobe;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.butcherskills.ButcherFxPlan;
import net.farmingtales.ftboss.butcherskills.ButcherSummonFx;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import java.util.LinkedHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.lwjgl.opengl.GL11;

@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class BossClientSummonFxProbe {
    private static final Map<String,Object> report=new ConcurrentHashMap<>();
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> packets=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> captures=new CopyOnWriteArrayList<>();
    private static final List<Entity> entities=new ArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean serverPrepared;
    private static volatile boolean baselineCaptured;
    private static volatile boolean spawned;
    private static volatile boolean serverComplete;
    private static volatile int received;
    private static volatile int expectedPackets;
    private static volatile long summonAge;
    private static volatile int renderedActiveFrames;
    private static int clientTicks;
    private static boolean nativeWorldCreationRequested;
    private static int stableTicks;
    private static int matchedCrimson;
    private static int matchedBone;
    private static long baselineColored;
    private static long baselineCrimson;
    private static long baselineBone;
    private static long start;

    public BossClientSummonFxProbe() {
        NeoForge.EVENT_BUS.addListener(BossClientSummonFxProbe::clientTick);
        NeoForge.EVENT_BUS.addListener(BossClientSummonFxProbe::serverTick);
        NeoForge.EVENT_BUS.addListener(BossClientSummonFxProbe::render);
        report.put("authenticatedEntryAccepted",false);
        report.put("authenticatedMultiplayerAccepted",false);
        report.put("dedicatedServerParticleDeliveryAccepted",false);
        report.put("fullBossMigrationComplete",false);
        report.put("installable",false);
        report.put("deployed",false);
        report.put("scope","Actual offline integrated client receives original server summon dust packets and renders one fixed front view. No authenticated start, multiplayer or four-direction GL acceptance.");
    }

    private static void require(boolean condition,String name) {
        if (!condition) throw new AssertionError(name);
        checks.add(name);
    }

    public static void packetReturned(ClientboundLevelParticlesPacket packet) {
        if (finished.get() || !spawned || !(packet.getParticle() instanceof DustParticleOptions dust)) return;
        if (!RenderSystem.isOnRenderThread()) return;
        var color=dust.getColor();
        boolean crimson=Math.abs(color.x-0.85F)<0.00001F && Math.abs(color.y-0.06F)<0.00001F && Math.abs(color.z-0.12F)<0.00001F;
        boolean bone=Math.abs(color.x-1.0F)<0.00001F && Math.abs(color.y-0.72F)<0.00001F && Math.abs(color.z-0.38F)<0.00001F;
        if ((!crimson && !bone) || Math.abs(dust.getScale()-0.75F)>0.00001F) return;
        if (Math.abs(packet.getX())>8.0 || Math.abs(packet.getZ()-1.5)>8.0 || packet.getY()<69.0 || packet.getY()>80.0) return;
        require(packet.getCount()==1 && packet.getMaxSpeed()==0.0F,"OriginalSingleDustPacketShape"+received);
        require(Math.abs(packet.getXDist()-0.018F)<0.00001F && Math.abs(packet.getYDist()-0.018F)<0.00001F && Math.abs(packet.getZDist()-0.018F)<0.00001F,"OriginalDustSpread"+received);
        packets.add(Map.of("index",received,"x",packet.getX(),"y",packet.getY(),"z",packet.getZ(),"bone",bone,"scale",dust.getScale(),"afterOriginalClientPacketHandler",true));
        if (crimson) matchedCrimson++; else matchedBone++;
        received++;
    }

    private static void serverTick(ServerTickEvent.Post event) {
        if (finished.get() || !event.getServer().isSingleplayer()) return;
        try {
            var server=event.getServer();
            if (server.getPlayerList().getPlayerCount()!=1) return;
            ServerPlayer player=server.getPlayerList().getPlayers().getFirst();
            ServerLevel level=player.serverLevel();
            if (!serverPrepared) {
                require(player.getGameProfile().getName().equals("ft_fx_probe"),"ExplicitOfflineFixtureIdentity");
                require(server.isSingleplayer(),"IntegratedServerIsNotAuthenticatedDedicatedAcceptance");
                for(int horizontal=-10;horizontal<=10;horizontal++) for(int depth=-12;depth<=12;depth++) level.setBlock(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState(),3);
                level.setDayTime(6000L);
                level.setWeatherParameters(6000,0,false,false);
                player.teleportTo(level,0.5,70.0,-8.5,0.0F,10.0F);
                player.setInvulnerable(true);
                serverPrepared=true;
                report.put("actualLocalPlayerJoinedThroughVanillaIntegratedLogin",true);
                return;
            }
            if (!baselineCaptured) return;
            if (!spawned) {
                MeatMountainEntity boss=(MeatMountainEntity)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("ftboss:meat_mountain")).create(level);
                boss.setCloneButcher(true);boss.setNoAi(true);boss.setNoGravity(true);boss.setInvisible(true);boss.setPersistenceRequired();boss.moveTo(-2.0,70.0,1.5,0.0F,0.0F);
                Cow minion=EntityType.COW.create(level);
                minion.setNoAi(true);minion.setNoGravity(true);minion.setInvisible(true);minion.moveTo(2.0,70.0,1.5,0.0F,0.0F);
                entities.add(boss);entities.add(minion);
                expectedPackets=0;
                for(Entity entity:entities) {
                    boolean bossFlag=entity instanceof MeatMountainEntity;
                    int phase=Math.floorMod(entity.getUUID().hashCode(),4);
                    expectedPackets+=ButcherFxPlan.summon(bossFlag,0).size();
                    for(int elapsed=1;elapsed<=32;elapsed++) if(elapsed%4==phase) expectedPackets+=ButcherFxPlan.summon(bossFlag,elapsed).size();
                }
                spawned=true;
                for(Entity entity:entities) require(ButcherSummonFx.addWithFx(level,entity),"OriginalSummonHelperAdmits"+entity.getType());
                start=level.getGameTime();
                report.put("expectedOriginalDustPackets",expectedPackets);
                report.put("originalHelperAndRealRegisteredEntities",true);
                return;
            }
            summonAge=level.getGameTime()-start;
            if (summonAge>36 && received==expectedPackets) {
                require(matchedCrimson>0 && matchedBone>0,"BothOriginalDustColorsDelivered");
                require(renderedActiveFrames>0,"AtLeastOneActualRenderedSummonFrame");
                for(Entity entity:entities) entity.discard();
                serverComplete=true;
            }
            if (summonAge>120) throw new AssertionError("Exact native client packet count or rendered frame missing: expected="+expectedPackets+" received="+received);
        } catch(Throwable failure) {finish(failure);}
    }

    private static void clientTick(ClientTickEvent.Post event) {
        if (finished.get()) return;
        try {
            Minecraft client=Minecraft.getInstance();
            client.options.pauseOnLostFocus=false;
            if (!nativeWorldCreationRequested && client.level==null && client.getOverlay()==null && client.screen instanceof TitleScreen) {
                nativeWorldCreationRequested=true;
                GameRules rules=new GameRules();
                rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);
                rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
                LevelSettings settings=new LevelSettings("Boss actual-client FX fixture",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);
                report.put("nativePublicFreshWorldCreationRequested",true);
                report.put("noPreviousOrFailedWorldSeed",true);
                client.createWorldOpenFlows().createFreshLevel("client-fx-native",settings,new WorldOptions(130132026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);
            }

            client.options.hideGui=true;
            client.options.particles().set(ParticleStatus.ALL);
            if (++clientTicks>2400) throw new AssertionError("Client fixture tick budget exceeded");
            if(clientTicks==1 || clientTicks%100==0) Files.writeString(Path.of("client-progress.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("clientTicks",clientTicks,"screenClass",client.screen==null?"none":client.screen.getClass().getName(),"overlayClass",client.getOverlay()==null?"none":client.getOverlay().getClass().getName(),"hasPlayer",client.player!=null,"hasLevel",client.level!=null,"serverPrepared",serverPrepared,"baselineCaptured",baselineCaptured,"spawned",spawned,"receivedPackets",received)));

            if (Files.exists(Path.of("normal-stop-request.json"))) throw new AssertionError("Explicit normal stop requested");
            if (client.player!=null && client.level!=null && serverPrepared) {
                client.player.setYRot(0.0F);client.player.setXRot(10.0F);
                stableTicks++;
            }
            if (serverComplete) finish(null);
        } catch(Throwable failure) {finish(failure);}
    }

    private static Map<String,Object> capture(String name) throws Exception {
        Minecraft client=Minecraft.getInstance();
        require(RenderSystem.isOnRenderThread(),"ScreenshotOnActualRenderThread"+name);
        require(GL11.glGetError()==GL11.GL_NO_ERROR,"NoGlErrorBefore"+name);
        try(NativeImage image=Screenshot.takeScreenshot(client.getMainRenderTarget())) {
            long crimsonPixels=0;
            long bonePixels=0;
            long brightWorldPixels=0;
            int worldBottom=image.getHeight()*3/4;
            for(int vertical=32;vertical<worldBottom;vertical++) for(int horizontal=12;horizontal<image.getWidth()-12;horizontal++) {
                int value=image.getPixelRGBA(horizontal,vertical);
                int red=value&255;int green=(value>>>8)&255;int blue=(value>>>16)&255;
                if(blue>100 && green>80 && blue>=red) brightWorldPixels++;
                if(red>80 && green<red*0.5 && blue<red*0.6) crimsonPixels++;
                if(red>100 && green>red*0.45 && green<red*0.88 && blue<green*0.8 && blue>5) bonePixels++;
            }
            long colored=crimsonPixels+bonePixels;
            Path path=Path.of(name+".png");
            image.writeToFile(path);
            require(GL11.glGetError()==GL11.GL_NO_ERROR,"NoGlErrorAfter"+name);
            Map<String,Object> result=new LinkedHashMap<>();
            result.put("image",path.toAbsolutePath().toString());
            result.put("width",image.getWidth());result.put("height",image.getHeight());
            result.put("coloredPixels",colored);result.put("crimsonPixels",crimsonPixels);result.put("bonePixels",bonePixels);
            result.put("brightWorldPixels",brightWorldPixels);result.put("worldOnlyRoi",List.of(12,32,image.getWidth()-12,worldBottom));
            result.put("bottomBossHudExcluded",true);result.put("receivedPackets",received);
            result.put("nativeParticleCount",client.particleEngine.countParticles());result.put("summonAge",summonAge);
            return result;
        }
    }

    private static void render(RenderFrameEvent.Post event) {
        if (finished.get()) return;
        try {
            Minecraft client=Minecraft.getInstance();
            if(client.player==null || client.level==null || client.screen!=null || stableTicks<40) return;
            if(!baselineCaptured) {
                Map<String,Object> baseline=capture("summon-front-baseline");
                baselineColored=(Long)baseline.get("coloredPixels");captures.add(baseline);
                baselineCrimson=(Long)baseline.get("crimsonPixels");baselineBone=(Long)baseline.get("bonePixels");
                require((Long)baseline.get("brightWorldPixels")>5000,"BaselineHasVisibleSkyInsteadOfEnclosedCamera");
                report.put("renderMetricExcludesBossHud",true);
                report.put("bothOriginalDustColorsMustAddWorldPixels",true);
                report.put("glRenderer",GL11.glGetString(GL11.GL_RENDERER));
                report.put("glVersion",GL11.glGetString(GL11.GL_VERSION));
                baselineCaptured=true;return;
            }
            if(spawned && received>=44 && renderedActiveFrames==0 && Integer.parseInt(client.particleEngine.countParticles())>=20) {
                Map<String,Object> active=capture("summon-front-active-"+captures.size());
                captures.add(active);
                if((Long)active.get("coloredPixels")>baselineColored+5 && (Long)active.get("crimsonPixels")>baselineCrimson+5 && (Long)active.get("bonePixels")>baselineBone+5) {
                    renderedActiveFrames++;
                    require(true,"OriginalSummonDustAddsVisiblePixelsAgainstFixedScene");
                }
            }
        } catch(Throwable failure) {finish(failure);}
    }

    private static void finish(Throwable failure) {
        if(!finished.compareAndSet(false,true)) return;
        Minecraft.getInstance().execute(()->{
            try {
                report.put("failures",failure==null?0:1);
                if(failure!=null) {report.put("failure",failure.toString());failure.printStackTrace();}
                report.put("checks",new ArrayList<>(checks));report.put("packets",new ArrayList<>(packets));report.put("captures",new ArrayList<>(captures));
                report.put("actualDustPacketsReceived",received);report.put("crimsonPackets",matchedCrimson);report.put("bonePackets",matchedBone);
                report.put("actualFrontRenderAccepted",failure==null && renderedActiveFrames>0);
                report.put("exactOriginalParticlePacketDeliveryAccepted",failure==null && received==expectedPackets);
                report.put("clientTicks",clientTicks);report.put("renderedActiveFrames",renderedActiveFrames);
                Files.writeString(Path.of("actual-client-summon-fx.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            } catch(Exception saving) {saving.printStackTrace();}
            Minecraft.getInstance().stop();
        });
    }
}
