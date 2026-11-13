package net.farmingtales.s1qa;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.*;

@Mod("fts1qa")
@GameTestHolder("fts1qa")
@PrefixGameTestTemplate(false)
public class ForestTests {
 static Path root(){return Path.of(System.getProperty("fts1.root"));}
 public ForestTests()throws Exception{GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(root().resolve("tests.xml").toFile()));}
 record Waypoint(String name,double east,double up,double south){}
 static List<Double> xyz(Vec3 value){return List.of(value.x,value.y,value.z);}
 static void require(boolean passed,String message){if(!passed)throw new IllegalStateException(message);}
 static int siteForced(ServerLevel level){int count=0;for(long packed:level.getForcedChunks()){var chunk=new net.minecraft.world.level.ChunkPos(packed);if(chunk.x>=624&&chunk.x<=655&&chunk.z>=624&&chunk.z<=633)count++;}return count;}
 static final List<Waypoint> route=List.of(
  new Waypoint("entry-west-corridor",5.5,6,94.5),
  new Waypoint("library-west-port-to-aisle",25.5,6,94.5),
  new Waypoint("library-reading-aisle",25.5,6,84.5),
  new Waypoint("library-ladder-approach",33.5,6,84.5),
  new Waypoint("library-ladder-base",33.5,6,82.5),
  new Waypoint("library-climb-natural",33.5,13,81.3),
  new Waypoint("library-ladder-descend-natural",33.5,6,81.5),
  new Waypoint("library-release-ladder",33.5,6,83.0),
  new Waypoint("library-return-east-aisle",33.5,6,84.5),
  new Waypoint("library-return-center-aisle",25.5,6,84.5),
  new Waypoint("library-return-west-port",25.5,6,94.5),
  new Waypoint("library-return-corridor",5.5,6,94.5),
  new Waypoint("library-return-entry",5.5,6,129.5),
  new Waypoint("watch-south-branch",423.5,6,129.5),
  new Waypoint("watch-south-entrance",423.5,6,108.5),
  new Waypoint("watch-natural-stair-up",423.5,14,89.5),
  new Waypoint("watch-upper-platform",408.5,14,89.5),
  new Waypoint("watch-original-high-exit",408.5,14,80.5),
  new Waypoint("watch-high-return",408.5,14,89.5),
  new Waypoint("watch-stair-return-top",423.5,14,89.5),
  new Waypoint("watch-natural-stair-down",423.5,6,108.5),
  new Waypoint("watch-return-corridor",423.5,6,129.5),
  new Waypoint("watch-return-entry",5.5,6,129.5)
 );
 @GameTest(template="empty",batch="s1natural",timeoutTicks=12000)
 public static void installEnterTravelReturn(GameTestHelper helper)throws Exception{
  try{
  ServerLevel level=helper.getLevel();var server=level.getServer();List<Object> trace=new ArrayList<>(),completed=new ArrayList<>(),events=new ArrayList<>();Map<String,Object> report=new LinkedHashMap<>();
  int installed=server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"function s1:install");require(installed>0,"Datapack install failed");
  FakePlayer player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("257a9ced-7da2-4adf-8ed0-a9dbd2291111"),"S1Visitor"));
  player.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player){@Override public void send(Packet<?> packet){} @Override public void send(Packet<?> packet,PacketSendListener listener){}};
  player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);player.getAbilities().flying=false;player.getAbilities().mayfly=false;player.getAbilities().invulnerable=false;player.setNoGravity(false);player.setSpeed(.1f);player.setMaxUpStep(.6f);
  var playersField=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");playersField.setAccessible(true);((List<net.minecraft.server.level.ServerPlayer>)playersField.get(server.getPlayerList())).add(player);level.addNewPlayer(player);
  player.teleportTo(level,9995.5,62,10129.5,Set.of(),-90,0);
  level.getChunkSource().move(player);require(player.position().distanceTo(new Vec3(9995.5,62,10129.5))<.01,"Fixture ground reception spawn failed");
  ItemEntity gravityProbe=new ItemEntity(level,9996.5,74,10130.5,new ItemStack(Items.STICK));gravityProbe.setPickUpDelay(32767);level.addFreshEntity(gravityProbe);
  long began=level.getGameTime();int[] phase={0},waypoint={0},waypointTicks={0};long[] phaseStart={began};boolean[] done={false};
  report.put("scope","Actual installed vanilla datapack load/tick; ordinary-permission trigger enter/return; survival FakePlayer horizontal travel input once per ordinary server tick. No Entity.move calls, no injected vertical velocity or elevation, no teleport between entry and return. FakePlayer is not a human client.");
  report.put("candidate_sha256","b960960f6cec50c986c7360821408573c0cac7f0c36828073ddeb4d8b9f38553");report.put("events",events);report.put("completed",completed);report.put("trace",trace);report.put("status","RUNNING");
  helper.onEachTick(()->{
   if(done[0])return;
   try{
    long elapsed=level.getGameTime()-began;
    if(phase[0]==0&&elapsed>=600){
     require(siteForced(level)==0,"Installed package leaked site forced chunks");require(gravityProbe.getY()<70.3&&gravityProbe.getY()>69.8,"Natural tick gravity probe did not settle: "+gravityProbe.getY());
     boolean watchLoaded=level.getChunkSource().getChunkNow(651,631)!=null;require(!watchLoaded,"Far watch chunk did not unload before natural entry");report.put("watch_chunk_loaded_before_entry",watchLoaded);report.put("natural_loading_hold_ticks",elapsed);
     int result=server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(0),"trigger s1gate set 1");require(result>0,"Permission-zero enter trigger failed");events.add(Map.of("event","ordinary_enter_trigger","game_time",level.getGameTime(),"permission",0,"command_result",result,"forced_chunks",level.getForcedChunks().size(),"gravity_probe_y",gravityProbe.getY()));phase[0]=1;phaseStart[0]=level.getGameTime();
    }else if(phase[0]==1&&level.getGameTime()-phaseStart[0]>=10){
     require(player.position().distanceTo(new Vec3(10005.5,70,10129.5))<.2,"Enter did not land at interior start");require(!player.getAbilities().flying&&!player.isNoGravity(),"Flight/gravity state invalid");phase[0]=2;
    }else if(phase[0]==2){
     Waypoint target=route.get(waypoint[0]);double east=10000+target.east-player.getX(),south=10000+target.south-player.getZ(),distance=Math.hypot(east,south);boolean climb=target.name.equals("library-climb-natural"),descend=target.name.equals("library-ladder-descend-natural");
     boolean reached=climb?player.getY()>=64+target.up-.08:descend?player.getY()<=64+target.up+.1&&player.onGround():distance<.19&&Math.abs(player.getY()-(64+target.up))<.22&&player.onGround();
     if(reached){completed.add(Map.of("waypoint",target.name,"game_time",level.getGameTime(),"feet",xyz(player.position()),"on_ground",player.onGround(),"on_climbable",player.onClimbable(),"ticks",waypointTicks[0]));waypoint[0]++;waypointTicks[0]=0;if(waypoint[0]==route.size()){phase[0]=3;phaseStart[0]=level.getGameTime();}}
     else{
      require(++waypointTicks[0]<3600,"Natural travel stuck at "+target.name+" feet="+player.position());
      player.setYRot((float)(Math.toDegrees(Math.atan2(-east,south))));player.setXRot(0);
      double forward=climb?1:descend?.03:Math.min(1,distance*3);player.travel(new Vec3(0,0,forward));level.getChunkSource().move(player);
      trace.add(Map.of("game_time",level.getGameTime(),"waypoint",target.name,"feet",xyz(player.position()),"velocity",xyz(player.getDeltaMovement()),"on_ground",player.onGround(),"on_climbable",player.onClimbable(),"horizontal_collision",player.horizontalCollision,"input_forward",forward));
     }
    }else if(phase[0]==3){
     int result=server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(0),"trigger s1gate set 2");require(result>0,"Permission-zero return trigger failed");events.add(Map.of("event","ordinary_return_trigger","game_time",level.getGameTime(),"permission",0,"command_result",result));phase[0]=4;phaseStart[0]=level.getGameTime();
    }else if(phase[0]==4&&level.getGameTime()-phaseStart[0]>=40){
     require(player.position().distanceTo(new Vec3(9995.5,70,10129.5))<.2,"Return did not reach external pad");require(siteForced(level)==0,"Player route required site forced chunks");
     report.put("status","PASS");report.put("elapsed_server_ticks",elapsed);report.put("final_position",xyz(player.position()));report.put("forced_chunks_at_end",siteForced(level));report.put("gametest_spawn_forced_chunks",level.getForcedChunks().size());report.put("health",player.getHealth());report.put("max_up_step",player.maxUpStep());done[0]=true;Files.writeString(root().resolve("natural-travel-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));helper.succeed();
    }
    if(elapsed%100==0)Files.writeString(root().resolve("natural-travel-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
   }catch(Exception problem){done[0]=true;report.put("status","FAIL");report.put("error",problem.toString());try{Files.writeString(root().resolve("natural-travel-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception ignored){}throw new IllegalStateException(problem);}
  });
  }catch(Exception problem){var traceText=new java.io.StringWriter();problem.printStackTrace(new java.io.PrintWriter(traceText));Files.writeString(root().resolve("initialization-error.txt"),traceText.toString());throw problem;}
 }
}
