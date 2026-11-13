package net.farmingtales.s1qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.*;
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
 record Node(int east,int up,int south){}
 static AABB body(Node node,BlockPos origin){double east=origin.getX()+node.east/2.0,up=origin.getY()+node.up/2.0,south=origin.getZ()+node.south/2.0;return new AABB(east-.299,up+.0001,south-.299,east+.299,up+1.7999,south+.299);}
 static boolean stand(ServerLevel level,Node node,BlockPos origin,BlockPos size){
  if(node.east<1||node.east>size.getX()*2-1||node.south<1||node.south>size.getZ()*2-1||node.up<0||node.up/2.0+1.8>size.getY())return false;
  AABB box=body(node,origin);if(!level.noCollision(box)||level.containsAnyLiquid(box))return false;
  AABB support=new AABB(box.minX+.02,box.minY-.055,box.minZ+.02,box.maxX-.02,box.minY-.0002,box.maxZ-.02);
  return !level.noCollision(support);
 }
 static Node node(com.google.gson.JsonArray value){return new Node((int)Math.round(value.get(0).getAsDouble()*2)+1,(int)Math.round(value.get(1).getAsDouble()*2),(int)Math.round(value.get(2).getAsDouble()*2)+1);}
 static boolean sweep(ServerLevel level,Node start,Node end,BlockPos origin){
  AABB first=body(start,origin),last=body(end,origin);double top=Math.max(first.minY,last.minY);
  AABB lifted=new AABB(first.minX,top,first.minZ,first.maxX,top+1.7998,first.maxZ);
  if(!level.noCollision(first.minmax(lifted)))return false;
  for(int index=0;index<=4;index++){
   double fraction=index/4.0;AABB along=lifted.move((last.minX-first.minX)*fraction,0,(last.minZ-first.minZ)*fraction);
   if(!level.noCollision(along)||level.containsAnyLiquid(along))return false;
  }
  AABB aboveLast=new AABB(last.minX,top,last.minZ,last.maxX,top+1.7998,last.maxZ);
  return level.noCollision(last.minmax(aboveLast));
 }
 static void move(FakePlayer player,Node point,BlockPos origin){
  double east=origin.getX()+point.east/2.0,up=origin.getY()+point.up/2.0,south=origin.getZ()+point.south/2.0;
  if(up>player.getY())player.move(MoverType.SELF,new Vec3(0,up-player.getY(),0));
  player.move(MoverType.SELF,new Vec3(east-player.getX(),0,south-player.getZ()));
  if(up<player.getY())player.move(MoverType.SELF,new Vec3(0,up-player.getY(),0));
  if(Math.abs(player.getX()-east)>.01||Math.abs(player.getY()-up)>.01||Math.abs(player.getZ()-south)>.01)throw new IllegalStateException("Collision move diverged at "+point+" actual "+player.position());
 }
 @GameTest(template="empty",batch="s1walk",timeoutTicks=1800)
 public static void allCandidates(GameTestHelper helper)throws Exception{
  ServerLevel level=helper.getLevel();FakePlayer player=FakePlayerFactory.getMinecraft(level);List<Object> results=new ArrayList<>();int group=0,failures=0;
  for(var record:JsonParser.parseString(Files.readString(root().resolve("inputs.json"))).getAsJsonArray()){
   var input=record.getAsJsonObject();Path source=Path.of(input.get("candidate").getAsString());String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
   if(!digest.equals(input.get("sha256").getAsString()))throw new IllegalStateException("Candidate drift");
   var design=JsonParser.parseString(Files.readString(Path.of(input.get("design").getAsString()))).getAsJsonObject();
   CompoundTag data=NbtIo.readCompressed(source.toFile());ListTag dim=data.getList("size",Tag.TAG_INT);BlockPos size=new BlockPos(dim.getInt(0),dim.getInt(1),dim.getInt(2));BlockPos origin=new BlockPos(512+(group%5)*112,100,512+(group/5)*224);
   for(int east=origin.getX()>>4;east<=(origin.getX()+size.getX())>>4;east++)for(int south=origin.getZ()>>4;south<=(origin.getZ()+size.getZ())>>4;south++)level.setChunkForced(east,south,true);
   for(BlockPos clear:BlockPos.betweenClosed(origin,origin.offset(size.getX()-1,size.getY()-1,size.getZ()-1)))if(!level.getBlockState(clear).isAir())level.setBlock(clear,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
   StructureTemplate template=new StructureTemplate();template.load(BuiltInRegistries.BLOCK.asLookup(),data);template.placeInWorld(level,origin,origin,new StructurePlaceSettings().setKnownShape(false),level.random,Block.UPDATE_ALL);
   for(var block:data.getList("blocks",Tag.TAG_COMPOUND)){
    ListTag coordinates=((CompoundTag)block).getList("pos",Tag.TAG_INT);BlockPos position=origin.offset(coordinates.getInt(0),coordinates.getInt(1),coordinates.getInt(2));BlockState state=level.getBlockState(position);
    if(state.getBlock() instanceof ButtonBlock button)button.use(state,level,position,player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(position),Direction.UP,position,false));
   }
   Node start=node(design.getAsJsonArray("start"));Map<Node,Node> parent=new HashMap<>();ArrayDeque<Node> queue=new ArrayDeque<>();Map<Node,Boolean> standing=new HashMap<>();Set<Node> wanted=new HashSet<>();
   for(var target:design.getAsJsonArray("targets"))wanted.add(node(target.getAsJsonObject().getAsJsonArray("position")));
   if(stand(level,start,origin,size)){parent.put(start,start);queue.add(start);}
   while(!queue.isEmpty()&&!wanted.isEmpty()){
    Node current=queue.remove();wanted.remove(current);
    for(int[] direction:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int rise=-1;rise<=1;rise++){
     Node next=new Node(current.east+direction[0],current.up+rise,current.south+direction[1]);
     if(parent.containsKey(next))continue;
     if(!standing.computeIfAbsent(next,key->stand(level,key,origin,size))||!sweep(level,current,next,origin))continue;
     parent.put(next,current);queue.add(next);
    }
   }
   List<Object> targets=new ArrayList<>();
   for(var target:design.getAsJsonArray("targets")){
    var value=target.getAsJsonObject();Node destination=node(value.getAsJsonArray("position"));boolean reachable=parent.containsKey(destination);int moves=0;String error="";
    if(reachable){
     List<Node> path=new ArrayList<>();for(Node cursor=destination;!cursor.equals(start);cursor=parent.get(cursor))path.add(cursor);Collections.reverse(path);
     player.setPos(origin.getX()+start.east/2.0,origin.getY()+start.up/2.0,origin.getZ()+start.south/2.0);
     try{for(Node point:path){move(player,point,origin);moves++;}Collections.reverse(path);if(!path.isEmpty())path.remove(0);path.add(start);for(Node point:path){move(player,point,origin);moves++;}}catch(Exception problem){error=problem.toString();}
    }
    boolean passed=reachable&&error.isEmpty();if(!passed)failures++;
    targets.add(Map.of("label",value.get("label").getAsString(),"reachable_in_real_level",reachable,"collision_move_roundtrip",passed,"moves",moves,"error",error));
   }
   results.add(Map.of("id",input.get("id").getAsString(),"source_sha256",digest,"targets",targets,"visited_nodes",parent.size()));group++;
  }
  Files.writeString(root().resolve("walk-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("candidates",results,"failures",failures,"scope","Actual ServerLevel shapes and FakePlayer Entity.move forward/return over half-step BFS paths after actual button use. No elapsed player ticks, gravity integration, client keyboard, climbing, jumping or combat certification.")));
  if(failures>0)throw new IllegalStateException("World collision-move failures: "+failures);helper.succeed();
 }
}
