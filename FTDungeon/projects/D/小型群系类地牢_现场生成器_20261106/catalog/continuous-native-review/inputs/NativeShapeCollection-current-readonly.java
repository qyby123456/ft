package cn.ft.biomedungeon.runtime;

import cn.ft.biomedungeon.core.DungeonApi;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.FakePlayer;

final class NativeShapeCollection {
    private static BlockPos position(int localX,int localY,int localZ,DungeonApi.PlacedRoom room){int rotatedX=switch(room.quarterTurns()){case 1->-localZ-1;case 2->-localX-1;case 3->localZ;default->localX;};int rotatedZ=switch(room.quarterTurns()){case 1->localX;case 2->-localZ-1;case 3->-localX-1;default->localZ;};return new BlockPos(room.origin().x()+rotatedX,room.origin().y()+localY,room.origin().z()+rotatedZ);}

    private static List<Double> inverse(AABB box,BlockPos block,DungeonApi.PlacedRoom room){
        double minimumX=Double.POSITIVE_INFINITY,minimumY=Double.POSITIVE_INFINITY,minimumZ=Double.POSITIVE_INFINITY,maximumX=Double.NEGATIVE_INFINITY,maximumY=Double.NEGATIVE_INFINITY,maximumZ=Double.NEGATIVE_INFINITY;
        for(double coordinateX:new double[]{box.minX+block.getX()-room.origin().x(),box.maxX+block.getX()-room.origin().x()}){for(double coordinateY:new double[]{box.minY+block.getY()-room.origin().y(),box.maxY+block.getY()-room.origin().y()}){for(double coordinateZ:new double[]{box.minZ+block.getZ()-room.origin().z(),box.maxZ+block.getZ()-room.origin().z()}){
            double sourceX=switch(room.quarterTurns()){case 1->coordinateZ;case 2->-coordinateX;case 3->-coordinateZ;default->coordinateX;};double sourceZ=switch(room.quarterTurns()){case 1->-coordinateX;case 2->-coordinateZ;case 3->coordinateX;default->coordinateZ;};minimumX=Math.min(minimumX,sourceX);maximumX=Math.max(maximumX,sourceX);minimumY=Math.min(minimumY,coordinateY);maximumY=Math.max(maximumY,coordinateY);minimumZ=Math.min(minimumZ,sourceZ);maximumZ=Math.max(maximumZ,sourceZ);
        }}}
        return List.of(minimumX,minimumY,minimumZ,maximumX,maximumY,maximumZ);
    }

    static void write(net.minecraft.server.level.ServerLevel level,List<DungeonApi.PlacedRoom> rooms,TemplateCatalog catalog,Path contractPath,Path output)throws Exception{
        byte[] contractBytes=Files.readAllBytes(contractPath);var contract=JsonParser.parseString(new String(contractBytes,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        FakePlayer player=new FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("2784c45d-fbd1-419c-97c5-6ed1d611bd73"),"FTNativeShapeCollector"));player.setMaxUpStep(0.6F);
        List<Map<String,Object>> scenes=new ArrayList<>();int collected=0;
        for(var element:contract.getAsJsonArray("strips")){
            var strip=element.getAsJsonObject();String id=strip.get("templateId").getAsString();int turn=strip.get("quarterTurns").getAsInt();var room=rooms.stream().filter(value->value.templateId().equals(id)&&value.quarterTurns()==turn).findFirst().orElseThrow();var template=catalog.templates.get(id);
            if(!template.metadata().sha256().equals(strip.get("resourceSha256").getAsString())){throw new IllegalArgumentException("Native strip exact resource SHA mismatch "+id);}
            Map<BlockPos,TemplateCatalog.Cell> source=new HashMap<>();for(var cell:template.cells()){source.put(cell.local(),cell);}
            double sourceX=strip.getAsJsonArray("centerX").get(0).getAsDouble(),sourceZ=strip.getAsJsonArray("centerZ").get(0).getAsDouble();double rotatedX=switch(turn){case 1->-sourceZ;case 2->-sourceX;case 3->sourceZ;default->sourceX;};double rotatedZ=switch(turn){case 1->sourceX;case 2->-sourceZ;case 3->-sourceX;default->sourceZ;};player.setPos(room.origin().x()+rotatedX,room.origin().y()+strip.get("startFeetY").getAsDouble(),room.origin().z()+rotatedZ);
            var context=CollisionContext.of(player);List<Map<String,Object>> blocks=new ArrayList<>();
            for(var localValue:strip.getAsJsonArray("requiredBlocks")){
                var local=localValue.getAsJsonArray();int localX=local.get(0).getAsInt(),localY=local.get(1).getAsInt(),localZ=local.get(2).getAsInt();BlockPos actual=position(localX,localY,localZ,room);level.getChunkAt(actual);var state=level.getBlockState(actual);var shape=state.getCollisionShape(level,actual,context);List<List<Double>> boxes=new ArrayList<>();for(AABB box:shape.toAabbs()){boxes.add(inverse(box,actual,room));}
                String blockName=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();String sourceKey=localX+","+localY+","+localZ;var sourceName=strip.getAsJsonObject("sourceBlockNames").get(sourceKey);if(sourceName==null||!blockName.equals(sourceName.getAsString())){throw new IllegalArgumentException("Native/source block identity mismatch "+strip.get("stripId")+" "+sourceKey+" actual="+blockName);}
                var original=source.get(new BlockPos(localX,localY,localZ));String expected=original==null?"EXPLICIT_EXTERIOR_AIR":original.state().rotate(Rotation.values()[turn]).toString();Map<String,Object> block=new LinkedHashMap<>();block.put("localBlock",List.of(localX,localY,localZ));block.put("worldBlock",List.of(actual.getX(),actual.getY(),actual.getZ()));block.put("blockName",blockName);block.put("nativeState",state.toString());block.put("sourceRotatedState",expected);block.put("fluidEmpty",state.getFluidState().isEmpty());block.put("boxes",boxes);block.put("nativeWorldBoxes",shape.toAabbs().stream().map(box->List.of(box.minX+actual.getX(),box.minY+actual.getY(),box.minZ+actual.getZ(),box.maxX+actual.getX(),box.maxY+actual.getY(),box.maxZ+actual.getZ())).toList());blocks.add(block);collected++;
            }
            scenes.add(Map.of("stripId",strip.get("stripId").getAsString(),"resourceSha256",template.metadata().sha256(),"nativeContext","PLAYER_STANDING_COLLISION_CONTEXT","quarterTurns",turn,"actualOrigin",room.origin(),"contextFeet",List.of(player.getX(),player.getY(),player.getZ()),"blocks",blocks));
        }
        Map<String,Object> result=new LinkedHashMap<>();result.put("schemaVersion",1);result.put("collectionMode","WORLD_NATIVE_VOXELSHAPE");result.put("contractSha256",AtomicFiles.sha256(contractBytes));result.put("coverageComplete",true);result.put("scenes",scenes);result.put("requiredBlockRecordsCollected",collected);result.put("boxCoordinates","Original source-local continuous AABBs obtained by inverse rigid rotation of actual native world shapes; dyadic doubles retained exactly");result.put("scope","Actual standing FakePlayer collision context; static shapes only, separate native step boundary replay and shape-context invariance required");Files.writeString(output,new Gson().toJson(result));System.out.println("INDEPENDENT_NATIVE_SHAPES strips="+scenes.size()+" blocks="+collected);
    }
}
