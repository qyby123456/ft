package ftboss.clientfxprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class ZicaiCurioClientProbeV5 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> snapshots=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady,fixtureInstalled,clientSawFixture,deathTriggered,deathObserved,respawnObserved,respawnRequested,clientRespawnAlive;
    private static int callbacks,clientTicks,playerTicks;
    private static long installedAt,respawnAt;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static IDynamicStackHandler stacks;
    private static ItemStack fixture;
    public ZicaiCurioClientProbeV5(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV5::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV5::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV5::playerTick);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV5::death);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV5::respawn);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return fixture!=null&&ItemStack.isSameItemSameComponents(actual,fixture);}
    private static int count(){int total=0;for(var handler:CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values()){for(int index=0;index<handler.getStacks().getSlots();index++){ItemStack actual=handler.getStacks().getStackInSlot(index);if(same(actual))total+=actual.getCount();}for(int index=0;index<handler.getCosmeticStacks().getSlots();index++){ItemStack actual=handler.getCosmeticStacks().getStackInSlot(index);if(same(actual))total+=actual.getCount();}}for(int index=0;index<player.getInventory().getContainerSize();index++){ItemStack actual=player.getInventory().getItem(index);if(same(actual))total+=actual.getCount();}return total;}
    private static void snapshot(String phase){snapshots.add(Map.of("phase",phase,"gameTime",level.getGameTime(),"matchingCurioAndInventoryItemCount",count(),"headSlotHasFixture",same(stacks.getStackInSlot(0)),"pendingRows",player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10).size()));}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void fixture(){
        require(System.getProperty("ftboss.zicaicurio.mode").equals("control"),"ExplicitUnchangedStage27Control");
        require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLoginPlayer");
        player.setGameMode(GameType.SURVIVAL);level.setChunkForced(0,0,true);level.getChunk(0,0);level.setBlockAndUpdate(new BlockPos(8,69,8),Blocks.STONE.defaultBlockState());player.teleportTo(8,70,8);player.setNoGravity(true);
        stacks=CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("head").getStacks();require(stacks.getSlots()>0&&stacks.getStackInSlot(0).isEmpty(),"ExistingEmptyHeadSlot");
        fixture=new ItemStack(Items.GOLD_NUGGET);fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Boss baseline nonweapon Curios fixture"));
        require(!stacks.isItemValid(0,fixture),"OriginalInvalidHeadFixtureExplicitlyDiagnosed");
        stacks.setStackInSlot(0,fixture.copy());require(count()==1,"ExactlyOneFixtureBeforeNativeDeath");installedAt=level.getGameTime();fixtureInstalled=true;snapshot("installed-one-invalid-fixture");
    }
    private static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event){if(event.getEntity()!=player||!deathTriggered||finished.get())return;try{snapshot("native-death-event");require(count()==1,"ExactlyOneFixtureAtActualDeathEvent");deathObserved=true;}catch(Throwable failure){finish(failure);}}
    private static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event){if(!(event.getEntity() instanceof ServerPlayer actual)||!actual.getGameProfile().getName().equals("ft_fx_probe")||!deathTriggered||finished.get())return;try{require(actual!=player&&actual.getUUID().equals(player.getUUID()),"NativeRespawnReplacesActualServerPlayer");player=actual;level=player.serverLevel();stacks=CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("head").getStacks();respawnObserved=true;respawnAt=level.getGameTime();snapshot("native-respawn-event");}catch(Throwable failure){finish(failure);}}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{
        require(++callbacks<2000,"BoundedNaturalServerPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
        if(!fixtureInstalled){player=event.getServer().getPlayerList().getPlayers().getFirst();level=player.serverLevel();if(!clientReady||playerTicks<100)return;fixture();return;}
        if(!deathTriggered){if(level.getGameTime()-installedAt<20||!clientSawFixture)return;snapshot("before-native-fatal-damage");require(count()==1,"ExactlyOneFixtureImmediatelyBeforeFatalDamage");deathTriggered=true;require(player.hurt(player.damageSources().fellOutOfWorld(),100000.0F),"ActualPublicNativeFatalDamage");require(deathObserved&&player.isDeadOrDying(),"ActualNativeDeathObserved");return;}
        if(!respawnObserved||level.getGameTime()-respawnAt<120||!clientRespawnAlive)return;snapshot("120-natural-ticks-after-native-respawn");require(deathObserved&&respawnObserved,"ActualDeathRespawnControlCompleted");finish(null);
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Unchanged Stage27 invalid Curios control",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-control-native",settings,new WorldOptions(13282026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        if(++clientTicks>4000)throw new AssertionError("BoundedNativeClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
        if(client.player!=null&&fixtureInstalled){var inventory=CuriosApi.getCuriosInventory(client.player);if(inventory.isPresent()){var handler=inventory.get().getCurios().get("head");if(handler!=null&&same(handler.getStacks().getStackInSlot(0)))clientSawFixture=true;}if(respawnObserved&&client.player.isAlive())clientRespawnAlive=true;}
        if(deathTriggered&&!respawnRequested&&client.player!=null&&client.player.isDeadOrDying()&&client.getConnection()!=null){respawnRequested=true;client.getConnection().send(new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));}
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("snapshots",new ArrayList<>(snapshots));result.put("naturalPlayerPostCallbacks",playerTicks);result.put("unchangedStage27InvalidSlotControlCompleted",failure==null&&deathObserved&&respawnObserved&&clientRespawnAlive);result.put("actualDeathObserved",deathObserved);result.put("actualRespawnObserved",respawnObserved);result.put("authenticated",false);result.put("originalZicaiSilenceAccepted",false);result.put("fullBossMigrationComplete",false);result.put("scope","Unchanged Stage27 native invalid gold-nugget head-slot death/respawn control;no Boss helper or silence invocation,no fixture-validity bypass claim,no failed-world reuse,no lifecycle product acceptance");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
