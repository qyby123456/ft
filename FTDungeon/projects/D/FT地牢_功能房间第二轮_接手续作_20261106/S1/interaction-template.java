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
import net.minecraft.world.*;
import net.minecraft.world.level.ClipContext;
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
 static Vec3 feet(Node node,BlockPos origin){return new Vec3(origin.getX()+node.east/2.0,origin.getY()+node.up/2.0,origin.getZ()+node.south/2.0);}
 static List<Double> xyz(Vec3 value){return List.of(value.x,value.y,value.z);}
 static AABB body(Node node,BlockPos origin){Vec3 point=feet(node,origin);return new AABB(point.x-.299,point.y+.0001,point.z-.299,point.x+.299,point.y+1.7999,point.z+.299);}
 static boolean stand(ServerLevel level,Node node,BlockPos origin,BlockPos size){
  if(node.east<1||node.east>size.getX()*2-1||node.south<1||node.south>size.getZ()*2-1||node.up<0||node.up/2.0+1.8>size.getY())return false;
  AABB box=body(node,origin);if(!level.noCollision(box)||level.containsAnyLiquid(box))return false;
  return !level.noCollision(new AABB(box.minX+.02,box.minY-.055,box.minZ+.02,box.maxX-.02,box.minY-.0002,box.maxZ-.02));
 }
 static boolean sweep(ServerLevel level,Node start,Node end,BlockPos origin){
  AABB first=body(start,origin),last=body(end,origin);double top=Math.max(first.minY,last.minY);
  AABB lifted=new AABB(first.minX,top,first.minZ,first.maxX,top+1.7998,first.maxZ);
  if(!level.noCollision(first.minmax(lifted)))return false;
  for(int index=0;index<=4;index++){AABB along=lifted.move((last.minX-first.minX)*index/4.0,0,(last.minZ-first.minZ)*index/4.0);if(!level.noCollision(along)||level.containsAnyLiquid(along))return false;}
  return level.noCollision(last.minmax(new AABB(last.minX,top,last.minZ,last.maxX,top+1.7998,last.maxZ)));
 }
 static Map<Node,Node> flood(ServerLevel level,Node start,BlockPos origin,BlockPos size){
  Map<Node,Node> parent=new LinkedHashMap<>();Map<Node,Boolean> standing=new HashMap<>();ArrayDeque<Node> queue=new ArrayDeque<>();
  if(stand(level,start,origin,size)){queue.add(start);parent.put(start,start);}
  while(!queue.isEmpty()){
   Node current=queue.remove();
   for(int[] direction:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int rise=-1;rise<=1;rise++){
    Node next=new Node(current.east+direction[0],current.up+rise,current.south+direction[1]);
    if(parent.containsKey(next)||!standing.computeIfAbsent(next,key->stand(level,key,origin,size))||!sweep(level,current,next,origin))continue;
    parent.put(next,current);queue.add(next);
   }
  }return parent;
 }
 static BlockHitResult hit(ServerLevel level,FakePlayer player,Node node,BlockPos origin,BlockPos target){
  Vec3 position=feet(node,origin);player.setPos(position);Vec3 eye=player.getEyePosition();
  for(AABB shape:level.getBlockState(target).getShape(level,target).toAabbs()){
   Vec3 aim=shape.getCenter().add(target.getX(),target.getY(),target.getZ());
   if(eye.distanceTo(aim)>4.5)continue;
   BlockHitResult hit=level.clip(new ClipContext(eye,aim,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,player));
   if(hit.getType()==HitResult.Type.BLOCK&&hit.getBlockPos().equals(target)&&eye.distanceTo(hit.getLocation())<=4.5)return hit;
  }return null;
 }
 @GameTest(template="empty",batch="s1interactions",timeoutTicks=1800)
 public static void interactions(GameTestHelper helper)throws Exception{
  ServerLevel level=helper.getLevel();FakePlayer player=FakePlayerFactory.getMinecraft(level);List<Object> reports=new ArrayList<>();int group=0,failures=0;
  for(var record:JsonParser.parseString(Files.readString(root().resolve("inputs.json"))).getAsJsonArray()){
   var input=record.getAsJsonObject();Path source=Path.of(input.get("candidate").getAsString());String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));if(!digest.equals(input.get("sha256").getAsString()))throw new IllegalStateException("Hash drift");
   var design=JsonParser.parseString(Files.readString(Path.of(input.get("design").getAsString()))).getAsJsonObject();
   CompoundTag data=NbtIo.readCompressed(source.toFile());ListTag dim=data.getList("size",Tag.TAG_INT);BlockPos size=new BlockPos(dim.getInt(0),dim.getInt(1),dim.getInt(2)),origin=new BlockPos(512+(group%5)*112,100,512+(group/5)*224);
   for(int east=origin.getX()>>4;east<=(origin.getX()+size.getX())>>4;east++)for(int south=origin.getZ()>>4;south<=(origin.getZ()+size.getZ())>>4;south++)level.setChunkForced(east,south,true);
   for(BlockPos clear:BlockPos.betweenClosed(origin.offset(-2,-2,-2),origin.offset(size.getX()+1,size.getY()+1,size.getZ()+1)))if(!level.getBlockState(clear).isAir())level.setBlock(clear,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
   StructureTemplate template=new StructureTemplate();template.load(BuiltInRegistries.BLOCK.asLookup(),data);template.placeInWorld(level,origin,origin,new StructurePlaceSettings().setKnownShape(false),level.random,Block.UPDATE_ALL);
   var entry=design.getAsJsonArray("start");Node start=new Node((int)Math.round(entry.get(0).getAsDouble()*2)+1,(int)Math.round(entry.get(1).getAsDouble()*2),(int)Math.round(entry.get(2).getAsDouble()*2)+1);
   List<BlockPos> pending=new ArrayList<>();
   for(var block:data.getList("blocks",Tag.TAG_COMPOUND)){ListTag position=((CompoundTag)block).getList("pos",Tag.TAG_INT);BlockPos target=origin.offset(position.getInt(0),position.getInt(1),position.getInt(2));if(level.getBlockState(target).getBlock() instanceof ButtonBlock||level.getBlockEntity(target) instanceof Container)pending.add(target);}
   List<Object> interactions=new ArrayList<>();boolean progress=true;Map<Node,Node> parent=flood(level,start,origin,size);
   while(progress&&!pending.isEmpty()){
    progress=false;
    for(var iterator=pending.iterator();iterator.hasNext();){
     BlockPos target=iterator.next();Node selected=null;BlockHitResult ray=null;
     for(Node node:parent.keySet()){
      Vec3 eye=feet(node,origin).add(0,player.getEyeHeight(),0);if(eye.distanceToSqr(Vec3.atCenterOf(target))>30)continue;
      ray=hit(level,player,node,origin,target);if(ray!=null){selected=node;break;}
     }
     if(selected==null)continue;
     Vec3 eye=player.getEyePosition();BlockState before=level.getBlockState(target);InteractionResult result=before.use(level,player,InteractionHand.MAIN_HAND,ray);
     List<Object> path=new ArrayList<>();for(Node cursor=selected;;cursor=parent.get(cursor)){path.add(xyz(feet(cursor,BlockPos.ZERO)));if(cursor.equals(start))break;}Collections.reverse(path);
     boolean button=before.getBlock() instanceof ButtonBlock,blocked=before.is(Blocks.CHEST)&&ChestBlock.isChestBlockedAt(level,target);
     boolean passed=result.consumesAction()&&!blocked&&(!button||level.getBlockState(target).getValue(ButtonBlock.POWERED));if(!passed)failures++;
     Map<String,Object> evidence=new LinkedHashMap<>();evidence.put("target",List.of(target.getX()-origin.getX(),target.getY()-origin.getY(),target.getZ()-origin.getZ()));evidence.put("block",before.toString());evidence.put("kind",button?"button":"container");evidence.put("connected_path_local_feet",path);evidence.put("standing_local_feet",xyz(feet(selected,BlockPos.ZERO)));evidence.put("eye_world",xyz(eye));evidence.put("first_hit_world",xyz(ray.getLocation()));evidence.put("first_hit_block",List.of(ray.getBlockPos().getX(),ray.getBlockPos().getY(),ray.getBlockPos().getZ()));evidence.put("distance",eye.distanceTo(ray.getLocation()));evidence.put("clip","OUTLINE/NONE actual first hit");evidence.put("use_result",result.toString());evidence.put("blocked_lid",blocked);evidence.put("passed",passed);interactions.add(evidence);
     iterator.remove();progress=true;
    }
    if(progress&&!pending.isEmpty())parent=flood(level,start,origin,size);
   }
   List<Object> missing=new ArrayList<>();for(BlockPos target:pending)missing.add(Map.of("position",List.of(target.getX()-origin.getX(),target.getY()-origin.getY(),target.getZ()-origin.getZ()),"block",level.getBlockState(target).toString()));failures+=pending.size();
   reports.add(Map.of("id",input.get("id").getAsString(),"source_sha256",digest,"origin",List.of(origin.getX(),origin.getY(),origin.getZ()),"interactions",interactions,"unreachable_or_occluded",missing,"reachable_standing_nodes",parent.size()));group++;
   Files.writeString(root().resolve("interaction-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("candidates",reports,"failures",failures,"scope","Entry-connected supported collision paths; ray from actual standing player eye, OUTLINE first-hit <=4.5; actual block use. No pre-opened buttons, no geometry edits. Standing placement after path proof is not natural-travel certification.")));
  }
  if(failures>0)throw new IllegalStateException("Interaction failures: "+failures);helper.succeed();
 }
}
