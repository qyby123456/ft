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
public final class ZicaiCurioClientProbeV17 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> trackerObservations=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady,clientInitial,stopPrepared,nativeStopping;
    private static int callbacks,clientTicks,playerTicks,phase,positivePosts;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static ItemStack fixture;
    private static java.lang.reflect.Field tracker;
    private static java.util.UUID bossUuid;
    public ZicaiCurioClientProbeV17(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV17::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV17::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV17::playerTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV17::damage);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV17::stopping);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV17::stopped);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return fixture!=null&&ItemStack.isSameItemSameComponents(actual,fixture);}
    private static IDynamicStackHandler stacks(){return CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("boss_relic").getStacks();}
    private static Map<?,?> readTracker(String name)throws Exception{if(tracker==null){tracker=net.farmingtales.ftboss.boss.ai.ZicaiCurioSilence.class.getDeclaredField("LAST_HITS");require(java.lang.reflect.Modifier.isPrivate(tracker.getModifiers())&&java.lang.reflect.Modifier.isStatic(tracker.getModifiers()),"OriginalTrackerReadOnlyFieldIdentity");require(tracker.trySetAccessible(),"ReadOnlyTrackerObservationAvailable");}var actual=(Map<?,?>)tracker.get(null);int nested=0;for(var value:actual.values())nested+=((Map<?,?>)value).size();trackerObservations.add(Map.of("phase",name,"playerMaps",actual.size(),"ownerEntries",nested,"thread",Thread.currentThread().getName()));return actual;}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(event.getEntity()==player&&event.getNewDamage()>0)positivePosts++;}
    private static void setup()throws Exception{level=player.serverLevel();require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaNativeStopFixturePlayer");require(readTracker("fresh-native-world").isEmpty(),"NoTrackerEntriesBeforeActualStopFixtureDamage");level.setChunkForced(0,0,true);level.getChunk(0,0);level.setBlockAndUpdate(new BlockPos(8,69,8),Blocks.STONE.defaultBlockState());player.setGameMode(GameType.SURVIVAL);player.teleportTo(8,70,8);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);require(stacks().getStackInSlot(0).isEmpty(),"FreshOriginalCurioForStopFixture");fixture=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Original Boss server-stop observer relic"));require(stacks().isItemValid(0,fixture),"ValidOriginalStopObserverRelic");stacks().setStackInSlot(0,fixture.copy());phase=1;}
    private static void hitAndStop()throws Exception{var boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualNativeStopBossFactory");boss.setVariant(8);boss.setMini(false);boss.setGiant(false);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,245,8,0,0);require(level.addFreshEntity(boss),"ActualNativeStopBossAdmission");bossUuid=boss.getUUID();long now=level.getGameTime();float before=player.getHealth();require(player.hurt(player.damageSources().mobAttack(boss),3)&&positivePosts==1&&player.getHealth()<before,"ActualPositiveDamageProducesRealTrackerEntry");var actual=readTracker("immediately-after-real-positive-hit");require(actual.size()==1&&actual.containsKey(player.getUUID()),"ActualPrivateDedupPlayerMapObservedNonemptyWithoutMutation");var owners=(Map<?,?>)actual.get(player.getUUID());require(owners.size()==1&&Long.valueOf(now).equals(owners.get(bossUuid)),"ActualPrivateDedupOwnerAndTickMatchNativeDamage");boss.discard();require(player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10).size()==1,"ActualSilencePendingBeforeNaturalShutdown");stopPrepared=true;finish(null);}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{require(++callbacks<1800,"BoundedNativeStopObserverPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;if(phase==0){player=event.getServer().getPlayerList().getPlayers().getFirst();if(!clientReady||playerTicks<100)return;setup();return;}if(clientInitial)hitAndStop();}catch(Throwable failure){finish(failure);}}
    private static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event){if(!stopPrepared)return;try{readTracker("actual-native-server-stopping");nativeStopping=true;}catch(Exception failure){failure.printStackTrace();}}
    private static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event){if(!stopPrepared)return;Throwable problem=null;try{require(nativeStopping,"ActualNativeServerStoppingEventObserved");require(readTracker("actual-native-server-stopped-lowest").isEmpty(),"ActualNativePostStopDedupTrackerEmpty");require(event.getServer().getPlayerList().getPlayerCount()==0,"ActualNativeStoppedServerHasNoPlayers");}catch(Throwable failure){problem=failure;}try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",problem==null?0:1);result.put("failure",problem==null?null:problem.toString());result.put("actualNativeServerStoppingObserved",nativeStopping);result.put("actualNativeServerStoppedObserved",true);result.put("nonemptyRealTrackerBeforeStopObserved",true);result.put("postStopTrackerEmpty",problem==null);result.put("trackerObservations",new ArrayList<>(trackerObservations));result.put("checks",new ArrayList<>(checks));result.put("readOnlyPrivateFieldObservation",true);result.put("privateFieldOrMapMutated",false);result.put("privateGameplayMethodsInvoked",false);result.put("isolatedOnStoppedClearCausalityClaimed",false);result.put("causalityLimit","Original logout orlastnaturalPlayerTick mayalreadyclear entries. This directlyaccepts realshutdown finaltrackerstate withnonempty nativepositive control;isolated onStopped.clear causality remains staticoriginalgraph evidence,notmanufacturedstaleentries.");Files.writeString(Path.of("actual-zicai-curio-stop.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception failure){failure.printStackTrace();}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{var client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null){clientReady=true;if(phase==1&&fixture!=null){var inventory=CuriosApi.getCuriosInventory(client.player);if(inventory.isPresent()){var handler=inventory.get().getCurios().get("boss_relic");if(handler!=null&&same(handler.getStacks().getStackInSlot(0)))clientInitial=true;}}}if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);var settings=new LevelSettings("Original Boss native server-stop observer",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-server-stop-native",settings,new WorldOptions(14112033L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}if(++clientTicks>5000)throw new AssertionError("BoundedNativeStopObserverClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("trackerObservations",new ArrayList<>(trackerObservations));result.put("nativeStopObservationPrepared",failure==null&&stopPrepared);result.put("actualStoppedSupplementRequired",true);result.put("serverStopLifecycleAccepted",false);result.put("naturalPlayerPostCallbacks",playerTicks);result.put("readOnlyPrivateFieldObservation",true);result.put("privateFieldOrMapMutated",false);result.put("privateGameplayMethodsInvoked",false);result.put("priorAcceptedControlsRerun",false);result.put("newJvmLoginAccepted",false);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("scope","Actual fresh nativeplayer andregisteredBoss positivehit creates realnonempty LAST_HITS observed withread-onlyField.get;normalclientstop thenactual ServerStopping/ServerStopped supplementsverifyemptyprivate tracker andzero serverplayers. No field/Map writes,privategameplayinvoke,manualticks,instrumentation orcandidatechange. Doesnotattribute isolated onStopped.clear iflogout/lasttick alreadycleared;originalhandlergraph evidencereused. No previouscontrolrerun ornewJVM gate bypass.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
