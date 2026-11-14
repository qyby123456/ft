package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import net.farmingtales.ftboss.item.EasterSpawnEggItem;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridge;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageScopeAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.*;
@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class SparrowActualWorldReloadNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Map<String,Object>> observations=new ArrayList<>();
    private static boolean worldRequested,clientReady;
    private static int checks,clientTicks,serverTicks,loadedRecordObservations;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static net.farmingtales.ftboss.boss.SparrowAppearanceData data;
    private static com.google.gson.JsonObject expected;
    private static UUID owner;
    private static Set<UUID> birds;
    private static long firstWorldTime=-1,lastPresentTime=-1,firstAbsentTime=-1,expectedPruneTime=-1;
    public SparrowActualWorldReloadNativeV1(){NeoForge.EVENT_BUS.addListener(SparrowActualWorldReloadNativeV1::clientTick);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,SparrowActualWorldReloadNativeV1::serverTick);}
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Map<String,Object> snapshot(net.farmingtales.ftboss.boss.SparrowAppearanceData.Entry entry,long now){return Map.of("worldTime",now,"owner",entry.owner.toString(),"stage",entry.stage,"players",entry.players,"used",entry.used,"lastAppearance",entry.lastAppearance,"birds",entry.birds.stream().map(UUID::toString).sorted().toList());}
    private static void checkEntry(net.farmingtales.ftboss.boss.SparrowAppearanceData.Entry entry){
        require(entry!=null,"Native DimensionDataStorage reloaded prior actual original record");require(entry.owner.equals(owner)&&entry.stage==expected.get("stage").getAsInt(),"Original saved owner and stage retained in new JVM");require(entry.players==expected.get("players").getAsInt()&&entry.used==expected.get("used").getAsInt(),"Original participant and quota usage retained");require(entry.lastAppearance==expected.get("lastAppearance").getAsLong()&&entry.birds.equals(birds),"Original appearance time and actual bird UUID retained");
    }
    private static void setup()throws Exception{
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual saved-world survival player");level=player.serverLevel();
        expected=com.google.gson.JsonParser.parseString(Files.readString(Path.of("expected-original-appearance.json"))).getAsJsonObject();owner=UUID.fromString(expected.get("owner").getAsString());birds=new HashSet<>();for(var item:expected.getAsJsonArray("birds"))birds.add(UUID.fromString(item.getAsString()));require(birds.size()==1,"Prior original naturally spawned support identity bound");
        data=net.farmingtales.ftboss.boss.SparrowAppearanceData.get(level.getServer());require(data==net.farmingtales.ftboss.boss.SparrowAppearanceData.get(level.getServer()),"Native DimensionDataStorage cache returns same actual loaded object");
        firstWorldTime=level.getGameTime();expectedPruneTime=(Math.floorDiv(firstWorldTime,1200)+1)*1200;require(firstWorldTime>=expected.get("actualOverworldTime").getAsLong()&&firstWorldTime<1200,"Actual persisted world time resumes before next original periodic prune");
        var entry=data.find(owner,expected.get("stage").getAsInt());checkEntry(entry);observations.add(snapshot(entry,firstWorldTime));System.out.println("SPARROW_WORLD_RELOAD_ACTUAL_RECORD "+firstWorldTime+" expectedPrune="+expectedPruneTime);
        report.put("actualOriginalRecordLoadedByNativeSavedData",true);report.put("actualLoadedPlayerUuid",player.getUUID().toString());
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{
        require(++serverTicks<3000,"Bounded actual saved world reload and natural periodic prune");if(player==null){var players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}
        long now=level.getGameTime();var entry=data.find(owner,expected.get("stage").getAsInt());
        if(now<expectedPruneTime){checkEntry(entry);loadedRecordObservations++;lastPresentTime=now;if(now==expectedPruneTime-1)observations.add(snapshot(entry,now));}
        else{
            require(entry==null,"Original native ServerTick.Post periodically prunes persisted retired appearance in resumed world");if(firstAbsentTime<0){firstAbsentTime=now;System.out.println("SPARROW_WORLD_RELOAD_NATIVE_PRUNE "+now);}
            if(now-firstAbsentTime>=40){require(loadedRecordObservations>0&&lastPresentTime==expectedPruneTime-1&&firstAbsentTime==expectedPruneTime,"Actual new JVM record lifetime crosses exact natural periodic boundary");report.put("actualNativeWorldReloadAndPeriodicPruneAccepted",true);finish(null);}
        }
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{
        Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;report.put("publicOpenWorldInvoked",true);client.createWorldOpenFlows().openWorld("sparrow-actual-world-reload-v1",()->{throw new AssertionError("Public saved world load returned without opening");});}
        require(++clientTicks<12000,"Bounded actual existing-world client loading");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{
        report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("nativeWorldRestartAccepted",failure==null);report.put("naturalServerTicks",serverTicks);report.put("loadedRecordObservations",loadedRecordObservations);report.put("recordSnapshots",observations);report.put("firstWorldTime",firstWorldTime);report.put("lastPresentTime",lastPresentTime);report.put("firstAbsentTime",firstAbsentTime);report.put("expectedPruneTime",expectedPruneTime);
        report.put("privateGameplayOrRewardCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("worldTimeOverridden",false);report.put("appearanceIdentityOrQuotaWrittenByObserver",false);report.put("observerDirectStorageLoadSaveOrPruneInvoked",false);report.put("directEntityCreationByObserver",false);report.put("fakePlayerUsed",false);report.put("actualClientAndIntegratedServer",true);report.put("copiedActualPriorNativeWorld",true);report.put("authenticated",false);report.put("actualMultiplayerGameplayAccepted",false);report.put("curiosReloadAcceptanceClaimed",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);
        Files.writeString(Path.of("actual-sparrow-world-reload.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
