package net.farmingtales.s8qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fts8qa")
@PrefixGameTestTemplate(false)
public class LadderTests {
    @GameTest(template="empty", batch="s8ladder", timeoutTicks=400)
    public static void ladder(GameTestHelper helper) throws Exception {
        Path root=Path.of(System.getProperty("fts8.root"));
        var input=JsonParser.parseString(Files.readString(root.resolve("ladder-input.json"))).getAsJsonObject();
        Path source=Path.of(input.get("candidate").getAsString());
        String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        var level=helper.getLevel();
        BlockPos origin=new BlockPos(2048,160,2048);
        for(int east=127;east<=130;east++)for(int south=127;south<=130;south++)level.setChunkForced(east,south,true);
        StructureTemplate template=new StructureTemplate();template.load(BuiltInRegistries.BLOCK.asLookup(),NbtIo.readCompressed(source.toFile()));
        template.placeInWorld(level,origin,origin,new StructurePlaceSettings().setKnownShape(false),level.random,Block.UPDATE_ALL);
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"S8LadderQA"));
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(origin.getX()+16.5,origin.getY()+2,origin.getZ()+7.5,180,0);
        player.setOnGround(true);
        List<Object> trace=new ArrayList<>();int[] ticks={0};boolean[] landed={false},climbed={false},descended={false};double[] highest={2};
        helper.onEachTick(()->{
            ticks[0]++;
            double relativeSouth=player.getZ()-origin.getZ(),relativeUp=player.getY()-origin.getY();
            if(relativeUp>=7.99&&relativeSouth<6&&player.onGround()&&level.noCollision(player,player.getBoundingBox()))landed[0]=true;
            if(landed[0])player.setYRot(0);
            double forward=landed[0]?Math.max(-.25,Math.min(.25,(6.55-relativeSouth)*.65)):1;
            player.travel(new Vec3(0,0,forward));
            highest[0]=Math.max(highest[0],player.getY()-origin.getY());climbed[0]|=player.onClimbable();
            descended[0]|=landed[0]&&player.onClimbable()&&player.getY()-origin.getY()<6;
            if(ticks[0]%4==0)trace.add(Map.of("tick",ticks[0],"position",List.of(player.getX()-origin.getX(),player.getY()-origin.getY(),player.getZ()-origin.getZ()),"on_climbable",player.onClimbable(),"on_ground",player.onGround()));
            if(ticks[0]==280){
                boolean returned=landed[0]&&descended[0]&&player.getY()-origin.getY()<2.1&&player.onGround();
                try{Files.writeString(root.resolve("ladder-physics.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("source_sha256",digest,"top_landed",landed[0],"climbable_observed",climbed[0],"descent_climbable_observed",descended[0],"returned_to_base",returned,"highest_y",highest[0],"trace",trace,"scope","Actual survival FakePlayer Player.travel; only initial teleport, no injected upward velocity. Automated movement, no client input or whole-build human traversal.")));}catch(Exception failure){throw new IllegalStateException(failure);}
                if(!returned)helper.fail("Ladder transition not completed; see trace");else helper.succeed();
            }
        });
    }
}
