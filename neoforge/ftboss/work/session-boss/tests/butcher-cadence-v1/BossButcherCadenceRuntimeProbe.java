package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class BossButcherCadenceRuntimeProbe {
    private static final List<String> checks = new ArrayList<>();
    private static final List<Map<String,Object>> rows = new ArrayList<>();
    private static final Map<String,Object> report = new LinkedHashMap<>();
    private static final int[] phases = {1,1,2,2,3,3,2,2,2};
    private static MinecraftServer server;
    private static ServerLevel level;
    private static ServerPlayer player;
    private static MeatMountainEntity owner;
    private static Object combat;
    private static Field combatField;
    private static Field swingsField;
    private static Field nextAttackField;
    private static int scenario;
    private static int scenarioTicks;
    private static int serverTicks;
    private static int priorSwings;
    private static boolean candidate;
    private static boolean finished;
    private static List<Long> starts;
    private static List<Long> scheduled;

    public BossButcherCadenceRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(BossButcherCadenceRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(BossButcherCadenceRuntimeProbe::tick);
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks.add(label);
    }

    private static void started(ServerStartedEvent event) {
        report.put("cases",checks);
        report.put("rows",rows);
        report.put("failures",0);
        report.put("authenticatedMultiplayerAccepted",false);
        report.put("completeBattleAccepted",false);
        report.put("clientRenderingAccepted",false);
        report.put("authorityWrittenByReflection",false);
        report.put("privateCombatFieldsReadOnly",true);
        try {
            candidate = "candidate".equals(System.getProperty("ftboss.cadence.mode"));
            server = event.getServer();
            level = server.overworld();
            var profile = new GameProfile(UUID.fromString("b0550000-0000-0000-0000-000000000008"),"boss_cadence_probe");
            player = new ServerPlayer(server,level,profile,ClientInformation.createDefault());
            player.connection = new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(profile,false));
            player.setGameMode(GameType.SURVIVAL);
            player.setNoGravity(true);
            require(server.getPlayerList().getPlayer(profile.getId())==null,"ScratchTargetIsNotAuthenticatedPlayer");
            combatField=MeatMountainEntity.class.getDeclaredField("combat");
            combatField.setAccessible(true);
            var combatType=Class.forName("net.farmingtales.ftboss.boss.ai.ButcherCombat");
            swingsField=combatType.getDeclaredField("swings");
            nextAttackField=combatType.getDeclaredField("nextAttack");
            swingsField.setAccessible(true);
            nextAttackField.setAccessible(true);
            try { Class.forName("net.farmingtales.ftboss.item.FoodWeaponFinalDamage"); throw new AssertionError("Paused weapon implementation loaded"); } catch (ClassNotFoundException absent) { checks.add("PausedWeaponImplementationAbsent"); }
            prepare();
        } catch (Throwable failure) { fail(failure); }
    }

    private static void prepare() throws Exception {
        if (owner!=null) owner.discard();
        owner=(MeatMountainEntity)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("ftboss:meat_mountain")).create(level);
        owner.setNoAi(true);
        owner.setNoGravity(true);
        owner.moveTo(0.5,70.0,0.5,0.0F,0.0F);
        owner.tick();
        for (int phase=1;phase<phases[scenario];phase++) {
            owner.setHealth(1.0F);
            owner.invulnerableTime=0;
            owner.hurt(level.damageSources().magic(),30.0F);
        }
        require(owner.isAlive() && owner.getButcherPhase()==phases[scenario],"Scenario"+scenario+"NaturalPhaseSetup");
        require(level.getEntity(owner.getUUID())==null,"Scenario"+scenario+"UnaddedScratchEntity");
        owner.setNoAi(scenario==8);
        owner.resetButcherCombat();
        combat=combatField.get(owner);
        priorSwings=swingsField.getInt(combat);
        starts=new ArrayList<>();
        scheduled=new ArrayList<>();
        scenarioTicks=0;
    }

    private static void tick(ServerTickEvent.Post event) {
        if (finished || server==null || event.getServer()!=server) return;
        try {
            serverTicks++;
            scenarioTicks++;
            require(scenarioTicks<=200,"Scenario"+scenario+"WithinTickBudget");
            owner.moveTo(0.5,70.0,0.5,0.0F,0.0F);
            owner.setDeltaMovement(0.0,0.0,0.0);
            player.moveTo(scenario==7?12.5:1.3,70.0,0.5,0.0F,0.0F);
            owner.setHealth(owner.getMaxHealth()*(scenario<6 && scenario%2==1?0.4F:1.0F));
            owner.setTarget(scenario==6?null:player);
            long now=level.getGameTime();
            owner.tick();
            int swings=swingsField.getInt(combat);
            if (swings!=priorSwings) {
                require(swings==priorSwings+1,"Scenario"+scenario+"SingleNativeSwingPerTick");
                String expectedAction=(starts.size()%2==0)?"chop":"backhand";
                require(expectedAction.equals(owner.getButcherAction()),"Scenario"+scenario+"NativeActionAlternation");
                starts.add(now);
                scheduled.add(nextAttackField.getLong(combat)-now);
                priorSwings=swings;
            }
            if ((scenario<6 && starts.size()==3) || (scenario>=6 && scenarioTicks==45)) completeScenario();
        } catch (Throwable failure) { fail(failure); }
    }

    private static void completeScenario() throws Exception {
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("scenario",scenario);
        row.put("phase",phases[scenario]);
        row.put("healthFractionControl",scenario<6 && scenario%2==1?0.4:1.0);
        row.put("nativeActionStartGameTimes",starts);
        row.put("scheduledNextAttackDeltas",scheduled);
        List<Long> gaps=new ArrayList<>();
        for (int index=1;index<starts.size();index++) gaps.add(starts.get(index)-starts.get(index-1));
        row.put("actualStartIntervals",gaps);
        rows.add(row);
        if (scenario<6) {
            int original=phases[scenario]==1?20:(phases[scenario]==2?10:14);
            int expected=candidate?original:(scenario%2==0?6:4);
            require(scheduled.stream().allMatch(delta->delta==expected),"Scenario"+scenario+"ScheduledIntervalMatchesMode");
            require(gaps.size()==2 && gaps.stream().allMatch(gap->candidate?gap==original:gap<original),"Scenario"+scenario+"ActualServerTickSwingIntervalsMatchMode");
            row.put("originalPhaseInterval",original);
        } else {
            require(starts.isEmpty(),scenario==6?"NoTargetCannotAttack":(scenario==7?"OutOfReachCannotAttack":"DisabledAiCannotAttack"));
        }
        scenario++;
        if (scenario==phases.length) finish(); else prepare();
    }

    private static void fail(Throwable failure) {
        report.put("failures",1);
        report.put("failure",failure.toString());
        failure.printStackTrace();
        finish();
    }

    private static void finish() {
        if (finished) return;
        finished=true;
        report.put("nativeServerTickCallbacks",serverTicks);
        report.put("completedScenarios",scenario);
        report.put("originalPhaseCadenceMissingDemonstrated",!candidate && scenario==9);
        report.put("attackCadenceFeatureAccepted",candidate && scenario==9);
        report.put("naturalAuthenticatedBossEncounterAccepted",false);
        report.put("hitDamageAndSkillsAccepted",false);
        if (owner!=null) owner.discard();
        if (player!=null) player.discard();
        try { Files.writeString(Path.of("actual-butcher-cadence-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)); } catch (Exception failure) { throw new RuntimeException(failure); }
    }
}
