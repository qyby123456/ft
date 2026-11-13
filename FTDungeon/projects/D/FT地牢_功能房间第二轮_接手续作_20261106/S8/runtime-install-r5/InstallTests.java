package net.farmingtales.s8qa;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("fts8qa")
@GameTestHolder("fts8qa")
@PrefixGameTestTemplate(false)
public class InstallTests {
    static Path root(){return Path.of(System.getProperty("fts8.root"));}
    static void write(String name,Object value)throws Exception{Files.writeString(root().resolve(name),new GsonBuilder().setPrettyPrinting().create().toJson(value));}
    static boolean neighborOnly(BlockState wanted,BlockState actual){
        if(wanted.getBlock()!=actual.getBlock())return false;
        java.util.Set<String> allowed;
        if(wanted.getBlock() instanceof net.minecraft.world.level.block.StairBlock)allowed=java.util.Set.of("shape");
        else if(wanted.getBlock() instanceof net.minecraft.world.level.block.FenceBlock)allowed=java.util.Set.of("north","east","south","west");
        else if(wanted.getBlock() instanceof net.minecraft.world.level.block.WallBlock)allowed=java.util.Set.of("north","east","south","west","up");
        else return false;
        for(var entry:wanted.getValues().entrySet())if(!allowed.contains(entry.getKey().getName())&&!entry.getValue().equals(actual.getValues().get(entry.getKey())))return false;
        return true;
    }
    @GameTest(template="empty",batch="installation",timeoutTicks=2000)
    public static void install(GameTestHelper helper)throws Exception{
        var input=JsonParser.parseString(Files.readString(root().resolve("install-input.json"))).getAsJsonObject();
        Path candidate=Path.of(input.get("candidate").getAsString());
        String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(candidate)));
        if(!digest.equals(input.get("sha256").getAsString()))throw new IllegalStateException("Candidate drift");
        for(var element:input.getAsJsonArray("frozen_pack_files")){
            var file=element.getAsJsonObject();Path source=root().resolve(file.get("path").getAsString());
            String actual=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
            if(!actual.equals(file.get("sha256").getAsString()))throw new IllegalStateException("Pack drift "+source);
        }
        var level=helper.getLevel();var server=level.getServer();BlockPos origin=new BlockPos(1024,128,1024);
        for(int east=62;east<=79;east++)for(int south=62;south<=73;south++)level.setChunkForced(east,south,true);
        GameProfile profile=new GameProfile(UUID.randomUUID(),"S8InstallHost");
        var player=new net.minecraft.server.level.ServerPlayer(server,level,profile);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),player);
        server.getPlayerList().getOps().add(new net.minecraft.server.players.ServerOpListEntry(profile,4,false));player.setGameMode(GameType.CREATIVE);
        player.moveTo(1000.5,129,1000.5,0,0);
        var source=player.createCommandSourceStack();
        if(source.getEntity()!=player||!source.hasPermission(2))throw new IllegalStateException("Not an actual administrator player command source");
        List<Object> commands=new ArrayList<>();
        java.util.function.Function<String,Integer> execute=command->{int result=server.getCommands().performPrefixedCommand(player.createCommandSourceStack(),command);commands.add(Map.of("command",command,"result",result,"source_entity_uuid",player.getUUID().toString(),"source_player",player.getGameProfile().getName(),"permission_2",player.createCommandSourceStack().hasPermission(2)));System.out.println("S8_INSTALL_COMMAND "+result+" "+command);return result;};
        CompoundTag data=NbtIo.readCompressed(candidate.toFile());
        Map<BlockPos,BlockState> expected=new LinkedHashMap<>();Map<BlockPos,CompoundTag> payloads=new LinkedHashMap<>();
        var palette=data.getList("palette",Tag.TAG_COMPOUND);var blocks=data.getList("blocks",Tag.TAG_COMPOUND);
        for(int index=0;index<blocks.size();index++){var entry=blocks.getCompound(index);var position=entry.getList("pos",Tag.TAG_INT);BlockPos absolute=origin.offset(position.getInt(0),position.getInt(1),position.getInt(2));expected.put(absolute,NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(entry.getInt("state"))));if(entry.contains("nbt"))payloads.put(absolute,entry.getCompound("nbt"));}
        var plan=JsonParser.parseString(Files.readString(root().resolve("server/world/datapacks/s8_frozen_install/plan.json"))).getAsJsonObject();
        for(var element:plan.getAsJsonArray("regions")){var region=element.getAsJsonObject();var from=region.getAsJsonArray("from");var to=region.getAsJsonArray("to");String command="fill "+(1024+from.get(0).getAsInt())+" "+(128+from.get(1).getAsInt())+" "+(1024+from.get(2).getAsInt())+" "+(1024+to.get(0).getAsInt())+" "+(128+to.get(1).getAsInt())+" "+(1024+to.get(2).getAsInt())+" minecraft:stone replace";if(execute.apply(command)<=0)throw new IllegalStateException("Seed failed");}
        long seeded=0;for(BlockPos position:BlockPos.betweenClosed(origin,origin.offset(239,28,144)))if(level.getBlockState(position).is(Blocks.STONE))seeded++;
        if(seeded!=1009200)throw new IllegalStateException("Dirty volume not fully seeded "+seeded);
        if(execute.apply("execute positioned 1024 128 1024 run function s8_takeover:clear")<=0)throw new IllegalStateException("Clear function unavailable");
        long uncleared=0;for(BlockPos position:BlockPos.betweenClosed(origin,origin.offset(239,28,144)))if(!level.getBlockState(position).isAir())uncleared++;
        write("clear-result.json",Map.of("seeded_stone_cells",seeded,"checked_cells",1009200,"remaining_non_air",uncleared,"commands",commands));
        if(uncleared!=0)throw new IllegalStateException("Incomplete clear "+uncleared);
        if(execute.apply("function s8_admin:install")<=0)throw new IllegalStateException("Full install function unavailable");
        List<Object> failures=new ArrayList<>(),shapeChanges=new ArrayList<>();long barriers=0,implicitAir=0;int payloadPass=0;
        for(BlockPos position:BlockPos.betweenClosed(origin,origin.offset(239,28,144))){
            BlockState wanted=expected.getOrDefault(position,Blocks.AIR.defaultBlockState()),actual=level.getBlockState(position);
            if(wanted.is(Blocks.BARRIER)&&actual.is(Blocks.BARRIER))barriers++;
            if(!expected.containsKey(position)&&actual.isAir())implicitAir++;
            if(!wanted.equals(actual)){var finding=Map.of("position",List.of(position.getX(),position.getY(),position.getZ()),"expected",wanted.toString(),"actual",actual.toString());if(neighborOnly(wanted,actual))shapeChanges.add(finding);else failures.add(finding);}
        }
        List<Object> bedNormalization=new ArrayList<>();
        for(var entry:payloads.entrySet()){
            var entity=level.getBlockEntity(entry.getKey());var expectedPayload=entry.getValue().copy();
            if(entity instanceof net.minecraft.world.level.block.entity.BedBlockEntity bed&&expectedPayload.contains("color")&&bed.getColor().getId()==expectedPayload.getInt("color")){
                bedNormalization.add(Map.of("position",entry.getKey().toShortString(),"source_legacy_color",expectedPayload.getInt("color"),"runtime_bed_color",bed.getColor().getId(),"reason","Vanilla 1.20.1 derives bed color from purple_bed state and omits legacy color on save; original source and part payloads unchanged"));expectedPayload.remove("color");
            }
            if(entity!=null&&NbtUtils.compareNbt(expectedPayload,entity.saveWithFullMetadata(),true))payloadPass++;else failures.add(Map.of("payload_position",entry.getKey().toShortString(),"expected",entry.getValue().toString(),"actual",entity==null?"null":entity.saveWithFullMetadata().toString()));
        }
        write("bed-normalization.json",bedNormalization);
        write("installation-result.json",Map.of("source_sha256",digest,"full_volume_checked",1009200,"source_records",expected.size(),"implicit_air_verified",implicitAir,"barriers",barriers,"block_entity_payloads_verified",payloadPass,"block_entity_payloads_expected",payloads.size(),"neighbor_shape_changes",shapeChanges,"failures",failures));
        if(!failures.isEmpty()||barriers!=90282||payloadPass!=payloads.size())throw new IllegalStateException("Installed package mismatch; inspect installation-result.json");
        List<Object> access=new ArrayList<>();
        for(String function:List.of("s8_admin:enter","s8_admin:return")){
            List<Double> before=List.of(player.getX(),player.getY(),player.getZ());int result=execute.apply("function "+function);List<Double> after=List.of(player.getX(),player.getY(),player.getZ());
            boolean enter=function.endsWith("enter");boolean destination=enter?Math.abs(player.getX()-1030.5)<.01&&Math.abs(player.getY()-132)<.01&&Math.abs(player.getZ()-1079.5)<.01:Math.abs(player.getX()-1000.5)<.01&&Math.abs(player.getY()-129)<.01&&Math.abs(player.getZ()-1000.5)<.01;
            boolean clear=level.noCollision(player,player.getBoundingBox()),floor=level.getBlockState(player.blockPosition().below()).isCollisionShapeFullBlock(level,player.blockPosition().below());
            access.add(Map.of("function",function,"before",before,"after",after,"command_result",result,"destination_correct",destination,"body_collision_free",clear,"supported_floor",floor,"source_is_player",player.createCommandSourceStack().getEntity()==player,"operator_permission_2",player.createCommandSourceStack().hasPermission(2)));
            if(result<=0||!destination||!clear||!floor)throw new IllegalStateException("Access failed "+function);
        }
        GameProfile guestProfile=new GameProfile(UUID.randomUUID(),"S8InstallGuest");
        var guest=new net.minecraft.server.level.ServerPlayer(server,level,guestProfile);
        guest.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),guest);
        guest.setGameMode(GameType.SURVIVAL);guest.moveTo(1000.5,129,1000.5,0,0);
        var onlineField=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");onlineField.setAccessible(true);
        @SuppressWarnings("unchecked") var onlinePlayers=(List<net.minecraft.server.level.ServerPlayer>)onlineField.get(server.getPlayerList());onlinePlayers.add(guest);
        if(guest.createCommandSourceStack().hasPermission(2))throw new IllegalStateException("Guest unexpectedly OP");
        for(String function:List.of("s8_admin:enter","s8_admin:return")){
            List<Double> before=List.of(guest.getX(),guest.getY(),guest.getZ());String command="execute as S8InstallGuest run function "+function;int result=execute.apply(command);
            boolean enter=function.endsWith("enter");boolean destination=enter?Math.abs(guest.getX()-1030.5)<.01&&Math.abs(guest.getY()-132)<.01&&Math.abs(guest.getZ()-1079.5)<.01:Math.abs(guest.getX()-1000.5)<.01&&Math.abs(guest.getY()-129)<.01&&Math.abs(guest.getZ()-1000.5)<.01;
            boolean clear=level.noCollision(guest,guest.getBoundingBox()),floor=level.getBlockState(guest.blockPosition().below()).isCollisionShapeFullBlock(level,guest.blockPosition().below());
            access.add(Map.of("function",function,"mode","Administrator command executed as non-OP guest","before",before,"after",List.of(guest.getX(),guest.getY(),guest.getZ()),"command_result",result,"destination_correct",destination,"body_collision_free",clear,"supported_floor",floor,"guest_has_permission_2",guest.createCommandSourceStack().hasPermission(2)));
            if(result<=0||!destination||!clear||!floor)throw new IllegalStateException("Hosted guest access failed");
        }
        onlinePlayers.remove(guest);
        long finalBarriers=0;for(var entry:expected.entrySet())if(entry.getValue().is(Blocks.BARRIER)&&level.getBlockState(entry.getKey()).is(Blocks.BARRIER))finalBarriers++;
        write("access-result.json",Map.of("access",access,"barriers_before",barriers,"barriers_after",finalBarriers,"player_class",player.getClass().getName(),"scope","Administrator hosted functions executed with actual vanilla ServerPlayer command source and real ServerGamePacketListenerImpl on an unconnected test transport; no network client, human input or multiplayer system claim"));
        write("commands.json",commands);
        if(finalBarriers!=90282)throw new IllegalStateException("Barrier changed");
        level.save(null,true,false);
        write("execution-result.json",Map.of("pass",true,"source_sha256",digest,"installed_parts",20,"clear_cells",1009200,"barriers",finalBarriers,"external_enter_return",true));
        System.out.println("S8_INSTALL_PASS full_clear=1009200 parts=20 barriers="+finalBarriers+" payloads="+payloadPass);
        helper.succeed();
    }
}
