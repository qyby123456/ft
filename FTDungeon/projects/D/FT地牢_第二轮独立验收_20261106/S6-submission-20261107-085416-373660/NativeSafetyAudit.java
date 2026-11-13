import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
public class NativeSafetyAudit {
  public static void main(String[] arguments) throws Exception {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    var fireMethod=FireBlock.class.getDeclaredMethod("getIgniteOdds",BlockState.class);
    fireMethod.setAccessible(true);
    Path root=Path.of(arguments[0]);
    var reports=new ArrayList<Object>();
    for(var input:JsonParser.parseString(Files.readString(root.resolve("route-input.json"))).getAsJsonArray()) {
      var spec=input.getAsJsonObject();
      byte[] bytes=Files.readAllBytes(Path.of(spec.get("candidate").getAsString()));
      String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
      if(!digest.equals(spec.get("sha256").getAsString())) throw new IllegalStateException("Hash mismatch");
      var nbt=NbtIo.readCompressed(Path.of(spec.get("candidate").getAsString()).toFile());
      var palette=new ArrayList<BlockState>();
      for(var entry:nbt.getList("palette",Tag.TAG_COMPOUND)) palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),(net.minecraft.nbt.CompoundTag)entry));
      var states=new HashMap<BlockPos,BlockState>();
      int blockEntities=0;
      var failures=new ArrayList<Object>();
      for(var entry:nbt.getList("blocks",Tag.TAG_COMPOUND)) {
        var block=(net.minecraft.nbt.CompoundTag)entry;
        var position=block.getList("pos",Tag.TAG_INT);
        var pos=new BlockPos(position.getInt(0),position.getInt(1),position.getInt(2));
        states.put(pos,palette.get(block.getInt("state")));
        if(block.contains("nbt")) {
          blockEntities++;
          var payload=block.getCompound("nbt");
          if(payload.contains("Items")&&!payload.getList("Items",Tag.TAG_COMPOUND).isEmpty()) failures.add("Nonempty inventory at "+pos);
        }
      }
      int lavaCells=0,faces=0,waterCells=0;
      for(var cell:states.entrySet()) {
        boolean lava=cell.getValue().is(Blocks.LAVA);
        boolean water=cell.getValue().is(Blocks.WATER);
        if(!lava&&!water) continue;
        if(lava) lavaCells++; else waterCells++;
        for(var direction:Direction.values()) {
          if(water&&direction==Direction.UP) continue;
          var neighborPos=cell.getKey().relative(direction);
          var neighbor=states.getOrDefault(neighborPos,Blocks.AIR.defaultBlockState());
          if(neighbor.is(lava?Blocks.LAVA:Blocks.WATER)) continue;
          if(!Block.isFaceFull(neighbor.getCollisionShape(EmptyBlockGetter.INSTANCE,neighborPos),direction.getOpposite())) failures.add("Incomplete fluid collision face: "+cell.getKey()+" -> "+neighborPos+" "+neighbor);
          faces++;
        }
        if(lava) for(int east=-4;east<=4;east++) for(int south=-4;south<=4;south++) for(int up=-1;up<=5;up++) {
          var neighborPos=cell.getKey().offset(east,up,south);
          var neighbor=states.getOrDefault(neighborPos,Blocks.AIR.defaultBlockState());
          if((Integer)fireMethod.invoke(Blocks.FIRE,neighbor)>0) failures.add("Native flammable block near lava: "+neighborPos+" "+neighbor);
        }
      }
      var report=new LinkedHashMap<String,Object>();
      report.put("sha256",digest);
      report.put("lava_cells",lavaCells);
      report.put("water_cells",waterCells);
      report.put("native_collision_faces_checked",faces);
      report.put("block_entities",blockEntities);
      report.put("structure_entities",nbt.getList("entities",Tag.TAG_COMPOUND).size());
      report.put("failures",failures);
      report.put("scope","Vanilla 1.20.1 collision shapes/full boundary faces, FireBlock.getIgniteOdds in +/-4 XZ,-1..+5Y exclusion box; static only. Water open top permitted. No fluid/fire ticks or live interactions.");
      reports.add(report);
    }
    Files.writeString(root.resolve("native-fluid-fire.json"),new GsonBuilder().setPrettyPrinting().create().toJson(reports));
  }
}
