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
public final class ZicaiCurioClientProbeV13 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> snapshots=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady,clientFixture,clientSuppressed,deathTriggered,deathObserved,respawnObserved,respawnRequested,clientRecovered;
    private static volatile int phase;
    private static int callbacks,clientTicks,playerTicks,positivePosts;
    private static long startedAt,until,respawnAt,pickupAt;
    private static ServerPlayer player,deadPlayer;
    private static ServerLevel level;
    private static ItemStack fixture;
    private static java.util.UUID identity;
    public ZicaiCurioClientProbeV13(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV13::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV13::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV13::playerTick);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV13::death);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV13::respawn);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV13::damage);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return fixture!=null&&ItemStack.isSameItemSameComponents(actual,fixture);}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static IDynamicStackHandler stacks(){return CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("boss_relic").getStacks();}
    private static List<net.minecraft.world.entity.item.ItemEntity> drops(){return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(-512,-64,-512,512,512,512),actual->same(actual.getItem()));}
    private static int held(){int count=0;for(var handler:CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values()){for(int index=0;index<handler.getStacks().getSlots();index++){ItemStack actual=handler.getStacks().getStackInSlot(index);if(same(actual))count+=actual.getCount();}for(int index=0;index<handler.getCosmeticStacks().getSlots();index++){ItemStack actual=handler.getCosmeticStacks().getStackInSlot(index);if(same(actual))count+=actual.getCount();}}for(int index=0;index<player.getInventory().getContainerSize();index++){ItemStack actual=player.getInventory().getItem(index);if(same(actual))count+=actual.getCount();}return count;}
    private static int total(){int count=held();for(var actual:drops())count+=actual.getItem().getCount();for(int index=0;index<pending().size();index++)if(same(ItemStack.parseOptional(player.registryAccess(),pending().getCompound(index).getCompound("Stack"))))count++;return count;}
    private static void snapshot(String name){snapshots.add(Map.of("phase",name,"gameTime",level.getGameTime(),"held",held(),"dropCount",drops().stream().mapToInt(actual->actual.getItem().getCount()).sum(),"pendingRows",pending().size(),"total",total(),"alive",player.isAlive()));}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(event.getEntity()==player&&event.getNewDamage()>0)positivePosts++;}
    private static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event){if(event.getEntity()!=player||!deathTriggered)return;try{require(!level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY),"ActualDefaultKeepInventoryFalseAtDeath");require(level.getGameTime()<until,"DefaultPolicyDeathBeforeSilenceExpiry");require(pending().isEmpty()&&same(stacks().getStackInSlot(0))&&held()==1,"OriginalHighestPriorityRestoreBeforeDefaultDeathDropPolicy");deathObserved=true;snapshot("death-event-after-original-restoration-before-drops");}catch(Throwable failure){finish(failure);}}
    private static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event){if(event.getEntity().getUUID().equals(identity)&&deathTriggered)try{var actual=(ServerPlayer)event.getEntity();require(actual!=deadPlayer&&actual.getUUID().equals(identity),"RealNativeRespawnReplacesPlayerSameIdentity");player=actual;level=player.serverLevel();require(!event.isEndConquered(),"ActualDeathRespawnNotEndReturn");require(pending().isEmpty()&&held()==0&&drops().stream().mapToInt(item->item.getItem().getCount()).sum()==1,"DefaultDeathDropStaysInWorldNotClonedIntoRespawn");respawnObserved=true;respawnAt=level.getGameTime();snapshot("actual-default-policy-respawn");}catch(Throwable failure){finish(failure);}}
    private static void setup(){require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaDefaultDeathFixturePlayer");identity=player.getUUID();level=player.serverLevel();require(!level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY),"DefaultFreshWorldKeepInventoryFalse");level.setChunkForced(0,0,true);level.getChunk(0,0);for(int offsetX=-5;offsetX<=5;offsetX++)for(int offsetZ=-5;offsetZ<=5;offsetZ++)level.setBlockAndUpdate(new BlockPos(8+offsetX,69,8+offsetZ),Blocks.STONE.defaultBlockState());player.setGameMode(GameType.SURVIVAL);player.teleportTo(8,70,8);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);require(stacks().getSlots()==1&&stacks().getStackInSlot(0).isEmpty()&&pending().isEmpty(),"FreshValidOriginalRelicSlot");fixture=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Original Boss default-death lifecycle relic fixture"));require(stacks().isItemValid(0,fixture),"OriginalSlotValidatorAcceptsDefaultDeathRelic");stacks().setStackInSlot(0,fixture.copy());phase=1;}
    private static void hit(){var boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredDeathFixtureBoss");boss.setVariant(8);boss.setMini(false);boss.setGiant(false);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,245,8,0,0);require(level.addFreshEntity(boss),"ActualDeathFixtureBossAdmission");startedAt=level.getGameTime();until=startedAt+100;require(player.hurt(player.damageSources().mobAttack(boss),3)&&positivePosts==1,"SingleActualPositiveSilenceHitForDefaultDeath");boss.discard();require(pending().size()==1&&pending().getCompound(0).getLong("Until")==until&&total()==1,"ActualOriginalPendingBeforeDefaultDeath");phase=2;}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{require(++callbacks<2200,"BoundedNaturalDefaultDeathPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;if(phase==0){player=event.getServer().getPlayerList().getPlayers().getFirst();if(!clientReady||playerTicks<100)return;setup();return;}if(phase==1){if(clientFixture)hit();return;}long now=level.getGameTime();require(total()==1,"GlobalExactlyOneRelicAcrossPendingDefaultDeathDropsAndRespawn");if(phase==2){if(now-startedAt<20)return;require(clientSuppressed,"ActualClientSuppressionBeforeDefaultDeath");deathTriggered=true;deadPlayer=player;phase=3;require(player.hurt(player.damageSources().fellOutOfWorld(),100000)&&player.isDeadOrDying()&&deathObserved,"ActualFatalDamageAndOriginalDeathRestoration");require(held()==0&&pending().isEmpty()&&drops().size()==1&&drops().getFirst().getItem().getCount()==1,"NativeDefaultCuriosDropProducesExactlyOneRealItemEntity");snapshot("actual-default-native-drop");return;}if(!respawnObserved)return;if(phase==3){require(pending().isEmpty(),"RespawnHasNoStaleSilenceRecord");if(now-respawnAt<30)return;require(held()==0&&drops().size()==1,"DefaultDropPreservedUntilRealPlayerPickup");var dropped=drops().getFirst();player.teleportTo(dropped.getX(),dropped.getY(),dropped.getZ());player.setNoGravity(false);pickupAt=now;phase=4;return;}if(phase==4){if(held()!=1){require(now-pickupAt<120,"BoundedNaturalItemEntityPickup");return;}require(drops().isEmpty()&&pending().isEmpty()&&stacks().getStackInSlot(0).isEmpty(),"NativePickupMovesDefaultDeathDropIntoInventoryExactlyOnce");snapshot("natural-player-item-entity-pickup");pickupAt=now;phase=5;return;}require(held()==1&&drops().isEmpty()&&pending().isEmpty(),"NoLateDefaultDeathRespawnDuplication");if(now-pickupAt<120||!clientRecovered){require(now-pickupAt<220,"BoundedClientDefaultDeathPickupSync");return;}require(deathObserved&&respawnObserved&&clientRecovered,"DefaultDeathRespawnPickupAndClientInventoryAccepted");snapshot("stable-beyond-original-expiry");finish(null);}catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{var client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null){clientReady=true;var inventory=CuriosApi.getCuriosInventory(client.player);if(inventory.isPresent()){var handler=inventory.get().getCurios().get("boss_relic");if(handler!=null&&handler.getStacks().getSlots()>0){var actual=handler.getStacks().getStackInSlot(0);if(phase==1&&same(actual))clientFixture=true;if(phase==2&&actual.isEmpty())clientSuppressed=true;}}if(respawnObserved&&client.player.isAlive())for(int index=0;index<client.player.getInventory().getContainerSize();index++)if(same(client.player.getInventory().getItem(index))&&client.player.getInventory().getItem(index).getCount()==1)clientRecovered=true;}
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);var settings=new LevelSettings("Original Boss default-death lifecycle",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-default-death-native",settings,new WorldOptions(14112029L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        if(deathTriggered&&!respawnRequested&&client.player!=null&&client.player.isDeadOrDying()&&client.getConnection()!=null){respawnRequested=true;client.getConnection().send(new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));}if(++clientTicks>6000)throw new AssertionError("BoundedDefaultDeathClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("snapshots",new ArrayList<>(snapshots));result.put("defaultDeathLifecycleAccepted",failure==null&&deathObserved&&respawnObserved&&clientRecovered);result.put("naturalPlayerPostCallbacks",playerTicks);result.put("deathObserved",deathObserved);result.put("respawnObserved",respawnObserved);result.put("clientRecovered",clientRecovered);result.put("priorAcceptedControlsRerun",false);result.put("newJvmLoginAccepted",false);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("scope","Unchanged Stage28,fresh vanilla integrated player;default keepInventory=false actual native death beforeexpiry,original highestpriority restoration,real Curios ItemEntity drop,actual client respawnpacket,newnativeplayer hasno copiedrelic,natural pickup andactualclient inventory sync,120natural stableticks. No manual ticks,privatehelper,synthetic events,failedworld seed,newJVM persistence,prior accepted keepInventory=true/fallback/18controls/dimension rerun orfulllifecycleclaim.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
