package net.farmingtales.s7qa;

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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.*;

@Mod("fts7qa")
@GameTestHolder("fts7qa")
@PrefixGameTestTemplate(false)
public class FungalTests {
 static Path root(){return Path.of(System.getProperty("fts7.root"));}
 public FungalTests() throws Exception {GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(root().resolve("tests.xml").toFile()));}
 static List<Integer> xyz(BlockPos position){return List.of(position.getX(),position.getY(),position.getZ());}
 static void require(boolean valid,String message){if(!valid)throw new IllegalStateException(message);}
 @GameTest(template="empty",batch="s7",timeoutTicks=1600)
 public static void allRooms(GameTestHelper helper)throws Exception{
  ServerLevel level=helper.getLevel();
  require(level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK),"Fire tick must remain enabled");
  require(level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING)==3,"Default random ticks required");
  List<Map<String,Object>> reports=new ArrayList<>();
  List<Map<BlockPos,BlockState>> expectedRooms=new ArrayList<>();
  List<BlockPos> origins=new ArrayList<>();
  List<BlockPos> sizes=new ArrayList<>();
  int index=0;
  for(var item:JsonParser.parseString(Files.readString(root().resolve("inputs.json"))).getAsJsonArray()){
   var input=item.getAsJsonObject();Path source=Path.of(input.get("candidate").getAsString());
   String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
   require(digest.equals(input.get("sha256").getAsString()),"Candidate drift");
   CompoundTag data=NbtIo.readCompressed(source.toFile());
   ListTag dimensions=data.getList("size",Tag.TAG_INT);
   BlockPos size=new BlockPos(dimensions.getInt(0),dimensions.getInt(1),dimensions.getInt(2));
   BlockPos origin=new BlockPos(512+(index%5)*96,100,512+(index/5)*96);
   for(int east=(origin.getX()-16)>>4;east<=(origin.getX()+size.getX()+16)>>4;east++)for(int south=(origin.getZ()-16)>>4;south<=(origin.getZ()+size.getZ()+16)>>4;south++)level.setChunkForced(east,south,true);
   for(BlockPos position:BlockPos.betweenClosed(origin.offset(-2,-1,-2),origin.offset(size.getX()+1,size.getY()+1,size.getZ()+1))){
    BlockPos relative=position.subtract(origin);
    boolean shell=relative.getX()<0||relative.getX()>=size.getX()||relative.getZ()<0||relative.getZ()>=size.getZ()||relative.getY()<0||relative.getY()>=size.getY();
    level.setBlock(position,shell?Blocks.BARRIER.defaultBlockState():Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
   }
   StructureTemplate template=new StructureTemplate();template.load(BuiltInRegistries.BLOCK.asLookup(),data);
   require(template.placeInWorld(level,origin,origin,new StructurePlaceSettings().setKnownShape(false).setFinalizeEntities(false),level.random,Block.UPDATE_ALL),"Placement failed");
   Map<BlockPos,BlockState> expected=new LinkedHashMap<>();ListTag palette=data.getList("palette",Tag.TAG_COMPOUND);
   for(var block:data.getList("blocks",Tag.TAG_COMPOUND)){
    CompoundTag entry=(CompoundTag)block;ListTag position=entry.getList("pos",Tag.TAG_INT);
    BlockPos target=origin.offset(position.getInt(0),position.getInt(1),position.getInt(2));
    BlockState state=NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(entry.getInt("state")));
    expected.put(target,state);
   }
   int liveDoors=0,doorToggleChecks=0;
   for(var entry:expected.entrySet()){
    BlockPos position=entry.getKey();BlockState actual=level.getBlockState(position);
    if(actual.getBlock() instanceof DoorBlock door && actual.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER){
     liveDoors++;boolean original=actual.getValue(DoorBlock.OPEN);door.setOpen(null,level,actual,position,!original);
     require(level.getBlockState(position).getValue(DoorBlock.OPEN)!=original,"Door did not toggle");
     door.setOpen(null,level,level.getBlockState(position),position,original);doorToggleChecks++;
    }
    BlockState updated=Block.updateFromNeighbourShapes(level.getBlockState(position),level,position);
    if(!updated.equals(level.getBlockState(position)))level.setBlock(position,updated,Block.UPDATE_ALL);
    level.updateNeighborsAt(position,level.getBlockState(position).getBlock());
    if(!level.getFluidState(position).isEmpty())level.scheduleTick(position,level.getFluidState(position).getType(),1);
   }
   Map<String,Object> report=new LinkedHashMap<>();report.put("id",input.get("id").getAsString());report.put("sha256",digest);report.put("origin",xyz(origin));report.put("doors",liveDoors);report.put("door_toggle_roundtrips",doorToggleChecks);reports.add(report);
   expectedRooms.add(expected);origins.add(origin);sizes.add(size);index++;
  }
  long start=level.getGameTime();
  helper.runAfterDelay(1200,()->{
   try{
    int totalIssues=0;
    for(int room=0;room<reports.size();room++){
     BlockPos origin=origins.get(room),size=sizes.get(room);Map<BlockPos,BlockState> expected=expectedRooms.get(room);
     List<Object> failures=new ArrayList<>(),changes=new ArrayList<>(),items=new ArrayList<>();int fire=0,leaks=0;
     for(var entry:expected.entrySet()){
      BlockState actual=level.getBlockState(entry.getKey());
      if(!actual.isAir()&&!actual.canSurvive(level,entry.getKey()))failures.add(Map.of("position",xyz(entry.getKey().subtract(origin)),"state",actual.toString()));
      if(!actual.equals(entry.getValue()))changes.add(Map.of("position",xyz(entry.getKey().subtract(origin)),"before",entry.getValue().toString(),"after",actual.toString()));
      if(actual.is(Blocks.FIRE)||actual.is(Blocks.SOUL_FIRE))fire++;
      if(!actual.getFluidState().isEmpty()&&entry.getValue().getFluidState().isEmpty())leaks++;
     }
     AABB bounds=new AABB(origin.offset(-1,-1,-1),origin.offset(size.getX()+1,size.getY()+1,size.getZ()+1));
     for(ItemEntity entity:level.getEntitiesOfClass(ItemEntity.class,bounds))items.add(Map.of("item",entity.getItem().toString(),"position",List.of(entity.getX(),entity.getY(),entity.getZ())));
     var armor=level.getEntitiesOfClass(ArmorStand.class,bounds);List<Object> equipped=new ArrayList<>();
     for(ArmorStand stand:armor){List<String> slots=new ArrayList<>();stand.getArmorSlots().forEach(stack->slots.add(stack.toString()));equipped.add(Map.of("armor",slots,"mainhand",stand.getMainHandItem().toString()));}
     var report=reports.get(room);report.put("ticks",level.getGameTime()-start);report.put("support_failures",failures);report.put("state_changes",changes);report.put("dropped_items",items);report.put("fires",fire);report.put("new_fluid_cells",leaks);report.put("equipped_armor_stands",equipped);
     StructureTemplate capture=new StructureTemplate();capture.fillFromWorld(level,origin,size,true,null);NbtIo.writeCompressed(capture.save(new CompoundTag()),root().resolve(report.get("id")+"-after-1200.nbt").toFile());
     totalIssues+=failures.size()+items.size()+fire+leaks;
    }
    Map<String,Object> output=new LinkedHashMap<>();output.put("status",totalIssues==0?"EXECUTED_NO_LISTED_FAILURES":"EXECUTED_REQUIRES_FIX");output.put("randomTickSpeed",level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));output.put("doFireTick",level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK));output.put("rooms",reports);output.put("scope","Single orientation isolated world, all candidates with original hashes, vanilla block behavior in Forge test harness; ordinary ticks and explicit neighbor/fluid scheduling; door API toggles, not player interaction or combat certification");
    Files.writeString(root().resolve("world-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(output));
    require(totalIssues==0,"World update failures: "+totalIssues);helper.succeed();
   }catch(Exception failure){throw new IllegalStateException(failure);}
  });
 }
}
