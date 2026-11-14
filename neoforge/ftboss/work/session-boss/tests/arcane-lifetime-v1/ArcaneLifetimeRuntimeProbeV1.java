package ftboss.bossprobe;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.farmingtales.ftboss.entity.*;
import net.farmingtales.ftboss.skill.*;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@Mod("ftboss_bossprobe")
public final class ArcaneLifetimeRuntimeProbeV1 {
    private static ServerLevel level;
    private static EasterBossEntity boss;
    private static JsonArray original;
    private static int callbacks,index,gaps;
    private static boolean finished;
    private static final boolean candidate=System.getProperty("ftboss.manorbody.mode","baseline").equals("candidate");
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    public ArcaneLifetimeRuntimeProbeV1(){NeoForge.EVENT_BUS.addListener(ArcaneLifetimeRuntimeProbeV1::start);NeoForge.EVENT_BUS.addListener(ArcaneLifetimeRuntimeProbeV1::tick);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static void start(ServerStartedEvent event){try{level=event.getServer().overworld();level.setChunkForced(0,0,true);level.getChunk(0,0);require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedOrFakePlayer");original=JsonParser.parseString(Files.readString(Path.of(System.getProperty("ftboss.arcane.oracle")))).getAsJsonArray();require(original.size()==288&&Spell.values().length==72&&Phase.values().length==4,"Entire72SpellFourPhaseOriginalMatrix");}catch(Throwable failure){finish(failure);}}
    private static void tick(ServerTickEvent.Post event){if(finished||level==null)return;try{
        callbacks++;require(callbacks<1000,"BoundedNativePostCallbacks");if(!level.isPositionEntityTicking(new BlockPos(8,240,8)))return;
        if(boss==null){boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory");boss.setVariant(7);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,240,8,0,0);require(level.addFreshEntity(boss),"ActualRegisteredBossAdmission");return;}
        require(level.getEntity(boss.getUUID())==boss&&boss.tickCount>0,"ActualNaturallyTickedBossOwner");
        Spell spell=Spell.values()[index];
        for(Phase phase:Phase.values()){
            JsonObject expected=original.get(index*4+phase.ordinal()).getAsJsonObject();
            require(expected.get("id").getAsString().equals(spell.id)&&expected.get("ordinal").getAsInt()==spell.ordinal()&&expected.get("phase").getAsString().equals(phase.name()),"OriginalSpellPhaseAndOrdinalIdentity");
            require(expected.get("form").getAsString().equals(spell.form.name())&&expected.get("duration").getAsInt()==spell.duration&&expected.get("color").getAsInt()==spell.color&&expected.get("radius").getAsDouble()==spell.radius&&expected.get("attached").getAsBoolean()==spell.attached&&expected.get("followsCaster").getAsBoolean()==spell.followsCaster(phase),"AllOriginalNonLifetimeSpellFieldsAndAttachmentPreserved");
            require(SpellVisualRules.find(spell.id)==spell,"OriginalPublicSpellIdResolution");
            var packet=new SpellVisualPacket(level.dimension().location(),boss.getId(),boss.getUUID(),spell,phase,boss.position(),boss.position().add(4,0,0));
            require(packet.valid(),"NativeVisualPayloadValid");
            RegistryFriendlyByteBuf buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
            SpellVisualPacket decoded;
            try{SpellVisualPacket.STREAM_CODEC.encode(buffer,packet);decoded=SpellVisualPacket.STREAM_CODEC.decode(buffer);require(decoded.equals(packet)&&buffer.readableBytes()==0&&decoded.valid(),"ActualRegistryNativeVisualPacketCodecRoundtrip");}finally{buffer.release();}
            require(decoded.spell()==spell&&decoded.phase()==phase&&decoded.owner().equals(boss.getUUID())&&decoded.dimension().equals(level.dimension().location()),"NativeDecodedEnumOwnerDimensionIdentity");
            int duration=decoded.spell().lifetime(decoded.phase()),wanted=expected.get("lifetime").getAsInt();
            if(duration!=wanted){gaps++;require(!candidate,"ActualOriginalLifetimeNotRestored:"+spell.id+":"+phase+":"+duration+":"+wanted);}
            else require(duration==wanted,"OriginalLifetimeMatchesAtNativeDecodedEnumBoundary");
            SpellVisuals.show(level,boss,spell.id,phase,boss.position());
            Map<String,Object> row=new LinkedHashMap<>();row.put("id",spell.id);row.put("ordinal",spell.ordinal());row.put("phase",phase.name());row.put("actualLifetime",duration);row.put("originalLifetime",wanted);row.put("codecAccepted",true);rows.add(row);
        }
        index++;if(index==Spell.values().length){require(gaps==(candidate?0:5),"ExactFiveBaselineGapsOrCompleteOriginalRestoration");finish(null);}
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(finished)return;finished=true;Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",checks);result.put("rows",rows);result.put("completedSpells",index);result.put("gaps",gaps);result.put("naturalPostCallbacks",callbacks);result.put("actualBossTickCount",boss==null?0:boss.tickCount);result.put("actualNativeEnumCodecMatrixAccepted",failure==null&&rows.size()==288);result.put("originalArcaneLifetimeAccepted",candidate&&failure==null&&rows.size()==288);result.put("baselineLifetimeGapDemonstrated",!candidate&&failure==null&&gaps==5);result.put("scope","All72spells/fourphases native registry packet codec and decoded enum lifetime,all original metadata,public SpellVisuals.show invocation with actual admitted naturally ticking NoAI Boss. Public payload/controller fixture;no recipient/channel/authentication fabrication,no client delivery/render/expiry or full AI/battle claim.");result.put("clientDeliveryRenderingOrExpiryAccepted",false);result.put("authenticatedMultiplayerAccepted",false);result.put("fullBossMigrationComplete",false);try{Files.writeString(Path.of("actual-arcane-lifetime-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}}
}
