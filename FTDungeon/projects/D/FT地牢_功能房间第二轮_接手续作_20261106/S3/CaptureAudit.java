import com.google.gson.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.state.BlockState;

public class CaptureAudit {
 static List<BlockState> palette(CompoundTag data){List<BlockState> result=new ArrayList<>();for(var entry:data.getList("palette",Tag.TAG_COMPOUND))result.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),(CompoundTag)entry));return result;}
 static BlockPos point(CompoundTag entry){ListTag pos=entry.getList("pos",Tag.TAG_INT);return new BlockPos(pos.getInt(0),pos.getInt(1),pos.getInt(2));}
 static String hash(Path path)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));}
 public static void main(String[] arguments)throws Exception{
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();Path root=Path.of(arguments[0]);List<Object> results=new ArrayList<>();
  for(var value:JsonParser.parseString(Files.readString(root.resolve("inputs.json"))).getAsJsonArray()){
   var input=value.getAsJsonObject();String id=input.get("id").getAsString();Path source=Path.of(input.get("candidate").getAsString()),capture=root.resolve(id+"-after-1200.nbt");
   CompoundTag original=NbtIo.readCompressed(source.toFile()),actual=NbtIo.readCompressed(capture.toFile());List<BlockState> before=palette(original),after=palette(actual);
   ListTag dimensions=original.getList("size",Tag.TAG_INT);long volume=(long)dimensions.getInt(0)*dimensions.getInt(1)*dimensions.getInt(2);
   Map<BlockPos,BlockState> expected=new HashMap<>();Map<BlockPos,String> keys=new HashMap<>();
   int randomTickCells=0;
   for(var block:original.getList("blocks",Tag.TAG_COMPOUND)){CompoundTag entry=(CompoundTag)block;BlockState state=before.get(entry.getInt("state"));expected.put(point(entry),state);if(state.isRandomlyTicking())randomTickCells++;if(entry.contains("nbt")&&!entry.getCompound("nbt").getString("Lock").isEmpty())keys.put(point(entry),entry.getCompound("nbt").getString("Lock"));}
   int typeChanges=0,addedSolid=0,nonempty=0,lockChanges=0,outOfBounds=0;List<Object> differences=new ArrayList<>();
   for(var block:actual.getList("blocks",Tag.TAG_COMPOUND)){
    CompoundTag entry=(CompoundTag)block;BlockPos position=point(entry);BlockState state=after.get(entry.getInt("state")),old=expected.get(position);
    if(position.getX()<0||position.getX()>=dimensions.getInt(0)||position.getY()<0||position.getY()>=dimensions.getInt(1)||position.getZ()<0||position.getZ()>=dimensions.getInt(2))outOfBounds++;
    if(old==null&&!state.isAir())addedSolid++;
    if(old!=null&&old.getBlock()!=state.getBlock())typeChanges++;
    if(old!=null&&!old.equals(state))differences.add(Map.of("pos",List.of(position.getX(),position.getY(),position.getZ()),"before",old.toString(),"after",state.toString()));
    if(entry.contains("nbt")){CompoundTag entity=entry.getCompound("nbt");if(!entity.getList("Items",Tag.TAG_COMPOUND).isEmpty()||entity.contains("LootTable"))nonempty++;if(keys.containsKey(position)&&!keys.get(position).equals(entity.getString("Lock")))lockChanges++;}
   }
   results.add(Map.of("id",id,"source_sha256",hash(source),"capture_sha256",hash(capture),"full_volume",volume,"captured_records",actual.getList("blocks",Tag.TAG_COMPOUND).size(),"source_random_ticking_cells",randomTickCells,"issues",Map.of("block_type_changes",typeChanges,"added_solids",addedSolid,"nonempty_or_loot_containers",nonempty,"lock_changes",lockChanges,"out_of_bounds",outOfBounds),"state_changes",differences));
  }
  Files.writeString(root.resolve("capture-audit.json"),new GsonBuilder().setPrettyPrinting().create().toJson(results));
 }
}
