package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class SparrowEvasionNativeBaselineV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Object> samples=new ArrayList<>();
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity sparrow;
    private static boolean worldRequested;
    private static volatile boolean clientReady;
    private static int checks,clientTicks,callbacks,playerTicks,entityTicks,phase,positivePosts;
    private static long spawned,hitAt;
    public SparrowEvasionNativeBaselineV1() {
        NeoForge.EVENT_BUS.addListener(SparrowEvasionNativeBaselineV1::clientTick);
        NeoForge.EVENT_BUS.addListener(SparrowEvasionNativeBaselineV1::serverTick);
        NeoForge.EVENT_BUS.addListener(SparrowEvasionNativeBaselineV1::playerTick);
        NeoForge.EVENT_BUS.addListener(SparrowEvasionNativeBaselineV1::entityTick);
        NeoForge.EVENT_BUS.addListener(SparrowEvasionNativeBaselineV1::damage);
    }
    private static void require(boolean condition,String label) { checks++;if (!condition) throw new AssertionError(label); }
    private static boolean eligible() {
        return sparrow.isAlive() && !sparrow.isRemoved() && sparrow.getVariant()==9 && !sparrow.isMini() && !sparrow.isGiant()
            && !sparrow.isNoAi() && !sparrow.isInvulnerable() && !sparrow.isInvisible() && !sparrow.isManorIntroHidden()
            && !sparrow.isXuanlanSkyHidden() && !HiddenBossIntro.isActing(sparrow.getUUID());
    }
    private static Map<String,Object> snapshot(String label) {
        Map<String,Object> row=new LinkedHashMap<>();row.put("label",label);row.put("time",level.getGameTime());row.put("uuid",sparrow.getUUID().toString());
        row.put("eligibleByMappedOriginalGuards",eligible());row.put("health",sparrow.getHealth());row.put("position",List.of(sparrow.getX(),sparrow.getY(),sparrow.getZ()));
        row.put("playerDistanceSquared",sparrow.distanceToSqr(player));row.put("performingSkill",sparrow.isPerformingSkill());row.put("castingLike",sparrow.isCastingLike());row.put("castsWithoutStopping",sparrow.castsWithoutStopping());
        var path=sparrow.getNavigation().getPath();row.put("hasNavigationPath",path!=null);row.put("navigationDone",sparrow.getNavigation().isDone());
        row.put("pathNodes",path==null?0:path.getNodeCount());row.put("naturalEntityPostTicks",entityTicks);row.put("alive",sparrow.isAlive());row.put("removed",sparrow.isRemoved());return row;
    }
    private static void playerTick(PlayerTickEvent.Post event) { if (event.getEntity() instanceof ServerPlayer) playerTicks++; }
    private static void entityTick(EntityTickEvent.Post event) { if (event.getEntity()==sparrow && !event.getEntity().level().isClientSide()) entityTicks++; }
    private static void damage(LivingDamageEvent.Post event) {
        if (event.getEntity()!=sparrow || event.getNewDamage()<=0.0F) return;
        try {
            require(event.getSource().getEntity()==player,"actual positive native hit attributed to the real integrated player");
            require(eligible(),"mapped original eligibility guards hold at actual confirmed positive damage");
            positivePosts++;report.put("actualPositiveDamage",event.getNewDamage());report.put("actualDamageSourcePlayerIdentityMatches",true);samples.add(snapshot("native-positive-damage-post"));
        } catch (Throwable failure) { finish(failure); }
    }
    private static void setup() {
        require(!(player instanceof FakePlayer) && !player.isCreative() && !player.isSpectator(),"actual native survival player, no FakePlayer or identity fabrication");
        level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);
        for (int blockX=-16;blockX<=24;blockX++) for (int blockZ=-16;blockZ<=16;blockZ++) level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());
        level.setChunkForced(-1,0,true);level.setChunkForced(0,0,true);level.setChunkForced(0,-1,true);level.setChunkForced(-1,-1,true);
        player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);
        sparrow=FTEntities.EASTER_BOSS.get().create(level);require(sparrow!=null,"actual registered Easter Boss created");sparrow.setVariant(9);sparrow.moveTo(2.5,ground+0.1,0.5,180.0F,0.0F);
        require(level.addFreshEntity(sparrow),"native addFreshEntity admitted ordinary variant nine");sparrow.setTarget(player);spawned=level.getGameTime();phase=1;
        report.put("actualRegisteredSparrowUuid",sparrow.getUUID().toString());report.put("actualServerPlayerUuid",player.getUUID().toString());report.put("noAllyFixture",true);samples.add(snapshot("native-admission"));
    }
    private static void serverTick(ServerTickEvent.Post event) {
        if (finished.get() || !event.getServer().isSingleplayer()) return;
        try {
            require(++callbacks<1400,"bounded native server observation");
            if (event.getServer().getPlayerList().getPlayerCount()!=1 || !clientReady || playerTicks<100) return;
            player=event.getServer().getPlayerList().getPlayers().getFirst();
            if (phase==0) { setup();return; }
            long now=level.getGameTime();require(sparrow.isAlive() && !sparrow.isRemoved(),"actual admitted ordinary Sparrow remains alive");
            if (phase==1) {
                require(now-spawned<240,"bounded natural eligibility without bypassing original intro or guards");
                if (now-spawned<40 || !eligible() || sparrow.isPerformingSkill() || sparrow.isCastingLike() && !sparrow.castsWithoutStopping()) return;
                require(entityTicks>=20,"actual Sparrow natural callbacks before hit");require(sparrow.distanceToSqr(player)<=4096.0,"original hit-owner distance bound");
                require(player.getMainHandItem().isEmpty(),"vanilla empty hand only, paused weapon system untouched");samples.add(snapshot("before-native-player-attack"));
                float before=sparrow.getHealth();hitAt=now;player.attack(sparrow);
                require(positivePosts==1 && sparrow.getHealth()<before,"one actual native ServerPlayer.attack gives positive confirmed damage");report.put("attackHealthBefore",before);report.put("attackHealthAfter",sparrow.getHealth());phase=2;return;
            }
            if (now-hitAt==1 || now-hitAt==20 || now-hitAt==40 || now-hitAt==60 || now-hitAt==80) samples.add(snapshot("natural-after-hit-"+(now-hitAt)));
            if (now-hitAt<80) return;
            require(positivePosts==1 && entityTicks>=90,"positive hit and eighty real natural server ticks observed");
            var activePath=sparrow.getNavigation().getPath();Vec3 away=sparrow.position().subtract(player.position()).multiply(1.0,0.0,1.0);
            if (away.lengthSqr()<0.000001) away=new Vec3(1.0,0.0,0.0);
            Vec3 destination=sparrow.position().add(away.normalize().scale(6.0));var planned=sparrow.getNavigation().createPath(BlockPos.containing(destination.x,sparrow.getY(),destination.z),0);
            report.put("readOnlyPublicPathPlanningAfterObservation",Map.of("pathFound",planned!=null,"canReach",planned!=null&&planned.canReach(),"nodes",planned==null?0:planned.getNodeCount()));
            require(sparrow.getNavigation().getPath()==activePath,"post-observation public planning did not install a navigation path");
            report.put("actualConfirmedHitAndNaturalBaselineObserved",true);finish(null);
        } catch (Throwable failure) { finish(failure); }
    }
    private static void clientTick(ClientTickEvent.Post event) {
        if (finished.get()) return;
        try {
            Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;
            if (client.player!=null && client.level!=null) clientReady=true;
            if (!worldRequested && client.level==null && client.getOverlay()==null && client.screen instanceof TitleScreen) {
                worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
                LevelSettings settings=new LevelSettings("Original Sparrow hit-evasion native baseline",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("sparrow-evasion-native-baseline",settings,new WorldOptions(14112040L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);
            }
            require(++clientTicks<6000,"bounded client observer");if (Files.exists(Path.of("normal-stop-request.json"))) throw new AssertionError("Explicit normal stop requested");
        } catch (Throwable failure) { finish(failure); }
    }
    private static void finish(Throwable failure) {
        if (!finished.compareAndSet(false,true)) return;
        if (failure!=null) { StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace); }
        Minecraft.getInstance().execute(()-> {
            try {
                report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("samples",samples);report.put("naturalPlayerPostTicks",playerTicks);report.put("naturalEntityPostTicks",entityTicks);report.put("positiveDamagePosts",positivePosts);
                report.put("actualClientAndIntegratedServer",true);report.put("explicitOfflineFixture",true);report.put("authenticated",false);report.put("fakePlayerUsed",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);
                report.put("newJvmPlayerLoginAccepted",false);report.put("sparrowEvasionPortAccepted",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);
                Files.writeString(Path.of("actual-sparrow-evasion-baseline.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            } catch (Exception problem) { problem.printStackTrace(); }
            Minecraft.getInstance().stop();
        });
    }
}
