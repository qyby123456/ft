package cn.ft.biomedungeon.runtime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftbd_boundary")
@PrefixGameTestTemplate(false)
public final class BoundaryTests {
    private static JsonObject activeScene;
    private static boolean unchanged(net.minecraft.server.level.ServerLevel level,FakePlayer player,JsonObject scene){int turn=scene.get("quarterTurns").getAsInt();var origin=scene.getAsJsonObject("actualOrigin");var room=new cn.ft.biomedungeon.core.DungeonApi.PlacedRoom("context-verification","context-verification",new cn.ft.biomedungeon.core.DungeonApi.Vec3i(origin.get("x").getAsInt(),origin.get("y").getAsInt(),origin.get("z").getAsInt()),turn,null);var context=net.minecraft.world.phys.shapes.CollisionContext.of(player);for(var item:scene.getAsJsonArray("blocks")){var block=item.getAsJsonObject();var coordinate=block.getAsJsonArray("worldBlock");BlockPos position=new BlockPos(coordinate.get(0).getAsInt(),coordinate.get(1).getAsInt(),coordinate.get(2).getAsInt());if(Math.abs(position.getX()-player.getX())>2||Math.abs(position.getZ()-player.getZ())>2){continue;}var state=level.getBlockState(position);if(!state.toString().equals(block.get("nativeState").getAsString())||!state.getFluidState().isEmpty()){return false;}var boxes=state.getCollisionShape(level,position,context).toAabbs().stream().map(box->NativeShapeCollection.inverse(box,position,room)).toList();if(!new Gson().toJsonTree(boxes).equals(block.getAsJsonArray("boxes"))){return false;}}return true;}
    private static Vec3 physical(JsonObject scene,double sourceX,double sourceY,double sourceZ){activeScene=scene;int turn=scene.get("quarterTurns").getAsInt();var origin=scene.getAsJsonObject("actualOrigin");double rotatedX=switch(turn){case 1->-sourceZ;case 2->-sourceX;case 3->sourceZ;default->sourceX;};double rotatedZ=switch(turn){case 1->sourceX;case 2->-sourceZ;case 3->-sourceX;default->sourceZ;};return new Vec3(origin.get("x").getAsDouble()+rotatedX,origin.get("y").getAsDouble()+sourceY,origin.get("z").getAsDouble()+rotatedZ);}
    private static double feet(JsonObject scene,double sourceX,double sourceZ){double result=Double.NEGATIVE_INFINITY;for(var item:scene.getAsJsonArray("blocks")){for(var shape:item.getAsJsonObject().getAsJsonArray("boxes")){var box=shape.getAsJsonArray();double top=box.get(4).getAsDouble();if(top<=4 && sourceX+0.3>box.get(0).getAsDouble()&&sourceX-0.3<box.get(3).getAsDouble()&&sourceZ+0.3>box.get(2).getAsDouble()&&sourceZ-0.3<box.get(5).getAsDouble()){result=Math.max(result,top);}}}return result;}
    private static boolean sweep(net.minecraft.server.level.ServerLevel level,FakePlayer player,Vec3 from,Vec3 to){player.setPos(from.x,from.y,from.z);player.setOnGround(true);if(!unchanged(level,player,activeScene)){return false;}int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(to.x-from.x),Math.abs(to.z-from.z))/0.005));for(int step=0;step<steps;step++){player.move(MoverType.SELF,new Vec3((to.x-from.x)/steps,-0.08,(to.z-from.z)/steps));var body=player.getBoundingBox();if(!level.noCollision(player,new AABB(body.minX,body.minY,body.minZ,body.maxX,body.minY+3,body.maxZ).deflate(0.00001))||level.containsAnyLiquid(body)){return false;}}for(int settle=0;settle<12;settle++){player.move(MoverType.SELF,new Vec3(0,-0.08,0));}return player.position().distanceTo(to)<0.04&&unchanged(level,player,activeScene);}
    @GameTest(template="s8_05",timeoutTicks=30000)
    public static void allFiniteCertificateSupportBoundariesBothNativeDirections(GameTestHelper helper)throws Exception {
        var level=helper.getLevel();Path world=level.getServer().getWorldPath(LevelResource.ROOT).toRealPath();helper.assertTrue(world.startsWith(Path.of(System.getProperty("ftbd.auditWorld")).toRealPath()),"Not owned matrix boundary copy");var shapes=JsonParser.parseString(Files.readString(world.resolve("INDEPENDENT_NATIVE_SHAPES.json"))).getAsJsonObject();var certificate=JsonParser.parseString(Files.readString(Path.of(System.getProperty("ftbd.auditFiniteCertificate")))).getAsJsonObject();helper.assertTrue(certificate.get("status").getAsString().equals("CONDITIONAL_FINITE_COVERAGE_PASS"),"Static native coverage must first pass");FakePlayer player=new FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("2af80b71-8d61-4525-bb0f-633ad11bd2d7"),"FTBoundaryProbe"));player.setMaxUpStep(0.6F);List<Map<String,Object>> results=new ArrayList<>();int passed=0;
        for(var item:certificate.getAsJsonArray("results")){var strip=item.getAsJsonObject();JsonObject scene=null;for(var value:shapes.getAsJsonArray("scenes")){if(value.getAsJsonObject().get("stripId").equals(strip.get("stripId"))){scene=value.getAsJsonObject();break;}}if(scene==null){throw new IllegalArgumentException("Missing bound native strip");}for(var value:strip.getAsJsonArray("nativeStepReplayRequired")){var boundary=value.getAsJsonObject();var atom=boundary.getAsJsonArray("xAtom");double sourceX=(atom.get(0).getAsDouble()+atom.get(1).getAsDouble())/2,sourceZ=boundary.get("boundaryZ").getAsDouble();double beforeFeet=feet(scene,sourceX,sourceZ-0.05),afterFeet=feet(scene,sourceX,sourceZ+0.05);Vec3 from=physical(scene,sourceX,beforeFeet,sourceZ-0.05),to=physical(scene,sourceX,afterFeet,sourceZ+0.05);boolean forward=Double.isFinite(beforeFeet)&&Double.isFinite(afterFeet)&&sweep(level,player,from,to);boolean returned=Double.isFinite(beforeFeet)&&Double.isFinite(afterFeet)&&sweep(level,player,to,from);if(forward&&returned){passed++;}Map<String,Object> result=new LinkedHashMap<>();result.put("stripId",strip.get("stripId"));result.put("requirement",boundary);result.put("actualStart",List.of(from.x,from.y,from.z));result.put("actualEnd",List.of(to.x,to.y,to.z));result.put("forward",forward);result.put("returned",returned);result.put("movementSubdivision",0.005);result.put("contextStateNativeShapeValidation","Pending full-scene native/source readback; player standing traversal context");results.add(result);}}
        Map<String,Object> report=new LinkedHashMap<>();report.put("status",passed==results.size()?"PASS_ALL_NATIVE_SUPPORT_DISCONTINUITIES":"FAIL_NATIVE_SUPPORT_DISCONTINUITIES");report.put("attempted",results.size());report.put("passed",passed);report.put("checks",results);report.put("interactions",List.of());report.put("method","Native Entity.move forward/reverse at every finite-certificate support discontinuity and x atom; initial per-direction endpoint positioning only; no interior teleport; body0.6x1.8 step0.6 plus declared headroom3");Files.writeString(world.resolve("INDEPENDENT_NATIVE_REPORT.json"),new Gson().toJson(report));helper.succeed();
    }
}
