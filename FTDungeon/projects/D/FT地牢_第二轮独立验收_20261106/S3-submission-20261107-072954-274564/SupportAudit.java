import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
public final class SupportAudit {
  public static void main(String[] arguments) throws Exception {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    Path folder=Path.of(arguments[0]);
    var design=JsonParser.parseString(Files.readString(folder.resolve("design.json"))).getAsJsonObject();
    String id=design.get("id").getAsString();
    Path candidate=folder.resolve(id+".nbt");
    var data=NbtIo.readCompressed(candidate.toFile());
    var palette=data.getList("palette",Tag.TAG_COMPOUND);
    List<BlockState> states=new ArrayList<>();
    for(int index=0;index<palette.size();index++)states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(index)));
    Map<BlockPos,BlockState> blocks=new HashMap<>();
    Map<String,Integer> inventory=new LinkedHashMap<>();
    List<Object> entities=new ArrayList<>();
    List<Object> failures=new ArrayList<>();
    var entries=data.getList("blocks",Tag.TAG_COMPOUND);
    for(int index=0;index<entries.size();index++){
      var entry=entries.getCompound(index);
      var coordinate=entry.getList("pos",Tag.TAG_INT);
      BlockPos position=new BlockPos(coordinate.getInt(0),coordinate.getInt(1),coordinate.getInt(2));
      BlockState state=states.get(entry.getInt("state"));
      blocks.put(position,state);
      String name=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
      inventory.merge(name,1,Integer::sum);
      if(entry.contains("nbt")){
        var entity=entry.getCompound("nbt");
        entities.add(Map.of("position",List.of(position.getX(),position.getY(),position.getZ()),"block",name,"snbt",entity.toString()));
        if(!entity.getList("Items",Tag.TAG_COMPOUND).isEmpty()||entity.contains("LootTable"))failures.add("Nonempty/loot container "+position);
      }
    }
    LevelReader view=(LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(),new Class<?>[]{LevelReader.class},(proxy,method,parameters)->{
      String name=method.getName();
      if(name.equals("getBlockState"))return blocks.getOrDefault((BlockPos)parameters[0],Blocks.AIR.defaultBlockState());
      if(name.equals("isEmptyBlock"))return blocks.getOrDefault((BlockPos)parameters[0],Blocks.AIR.defaultBlockState()).isAir();
      if(name.equals("getFluidState"))return blocks.getOrDefault((BlockPos)parameters[0],Blocks.AIR.defaultBlockState()).getFluidState();
      if(name.equals("getMinBuildHeight"))return 0;
      if(name.equals("getHeight")||name.equals("getMaxBuildHeight"))return design.getAsJsonArray("size").get(1).getAsInt();
      if(name.equals("getRawBrightness")||name.equals("getMaxLocalRawBrightness"))return 15;
      if(name.startsWith("hasChunk"))return true;
      throw new UnsupportedOperationException(name);
    });
    int checked=0;
    for(var entry:blocks.entrySet()){
      try {if(!entry.getValue().canSurvive(view,entry.getKey()))failures.add(Map.of("position",entry.getKey().toShortString(),"state",entry.getValue().toString()));checked++;}
      catch(Exception failure){failures.add("UNSUPPORTED "+entry.getKey()+" "+failure);}
    }
    List<Object> ports=new ArrayList<>();
    for(var element:design.getAsJsonArray("ports")){
      var port=element.getAsJsonObject();var foot=port.getAsJsonArray("foot");var normal=port.getAsJsonArray("normal");
      int east=foot.get(0).getAsInt(),up=foot.get(1).getAsInt(),south=foot.get(2).getAsInt();
      boolean clear=true,supported=true;
      int width=port.get("width").getAsInt(),height=port.get("height").getAsInt();
      for(int lateral=-(width/2);lateral<=width/2;lateral++){
        int targetEast=east+(normal.get(2).getAsInt()!=0?lateral:0),targetSouth=south+(normal.get(0).getAsInt()!=0?lateral:0);
        if(blocks.getOrDefault(new BlockPos(targetEast,up-1,targetSouth),Blocks.AIR.defaultBlockState()).isAir())supported=false;
        for(int above=0;above<height;above++)if(!blocks.getOrDefault(new BlockPos(targetEast,up+above,targetSouth),Blocks.AIR.defaultBlockState()).isAir())clear=false;
      }
      ports.add(Map.of("name",port.get("name").getAsString(),"clear",clear,"supported",supported,"width",width,"height",height));
      if(!clear||!supported)failures.add("Port "+port.get("name")+" invalid");
    }
    var report=Map.of("sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(candidate))),"all_blocks_canSurvive_checked",checked,"failures",failures,"ports",ports,"inventory",inventory,"actual_nbt_entities",entities,"scope","Offline all-block canSurvive; native registry, no world ticks or dynamic simulation; ports require declared full air volume and floor blocks");
    Files.writeString(folder.resolve("qa/support.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
    if(!failures.isEmpty())throw new IllegalStateException(failures.toString());
  }
}
