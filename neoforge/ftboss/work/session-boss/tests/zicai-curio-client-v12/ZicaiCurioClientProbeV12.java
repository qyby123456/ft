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
public final class ZicaiCurioClientProbeV12 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> transitions=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> snapshots=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady,clientInitialRelic,clientNetherSuppressed,clientNetherRestored,clientOverworldRestored;
    private static volatile int phase;
    private static int callbacks,clientTicks,playerTicks,positiveDamagePosts;
    private static long startedAt,until,returnedAt;
    private static ServerPlayer player;
    private static ServerLevel overworld,nether;
    private static ItemStack fixture;
    private static java.util.UUID originalUuid;
    public ZicaiCurioClientProbeV12(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV12::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV12::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV12::playerTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV12::changedDimension);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV12::damage);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return fixture!=null&&ItemStack.isSameItemSameComponents(actual,fixture);}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static IDynamicStackHandler stacks(){return CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("boss_relic").getStacks();}
    private static int total(){int count=0;for(var handler:CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values()){for(int index=0;index<handler.getStacks().getSlots();index++){ItemStack actual=handler.getStacks().getStackInSlot(index);if(same(actual))count+=actual.getCount();}for(int index=0;index<handler.getCosmeticStacks().getSlots();index++){ItemStack actual=handler.getCosmeticStacks().getStackInSlot(index);if(same(actual))count+=actual.getCount();}}for(int index=0;index<player.getInventory().getContainerSize();index++){ItemStack actual=player.getInventory().getItem(index);if(same(actual))count+=actual.getCount();}for(int index=0;index<pending().size();index++){ItemStack actual=ItemStack.parseOptional(player.registryAccess(),pending().getCompound(index).getCompound("Stack"));if(same(actual))count+=actual.getCount();}return count;}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(event.getEntity()==player&&event.getNewDamage()>0)positiveDamagePosts++;}
    private static void changedDimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event){if(event.getEntity()==player)transitions.add(Map.of("from",event.getFrom().location().toString(),"to",event.getTo().location().toString(),"overworldGameTime",overworld.getGameTime(),"pendingRows",pending().size(),"playerUuid",player.getUUID().toString()));}
    private static void snapshot(String name){snapshots.add(Map.of("phase",name,"overworldGameTime",overworld.getGameTime(),"playerDimension",player.serverLevel().dimension().location().toString(),"dimensionGameTime",player.serverLevel().getGameTime(),"pendingRows",pending().size(),"pendingUntil",pending().isEmpty()?-1L:pending().getCompound(0).getLong("Until"),"totalRelics",total(),"equipped",same(stacks().getStackInSlot(0))));}
    private static void activeContract(){require(pending().size()==1,"DimensionTransferKeepsExactlyOnePendingRecord");var record=pending().getCompound(0);require(record.getLong("Until")==until,"DimensionTransferDoesNotRefreshOriginalOverworldExpiry");require(record.getString("Slot").equals("boss_relic")&&record.getInt("Index")==0&&same(ItemStack.parseOptional(player.registryAccess(),record.getCompound("Stack"))),"DimensionTransferPreservesOriginalSlotCountAndComponents");require(stacks().getStackInSlot(0).isEmpty()&&total()==1,"CrossDimensionPendingItemNotLostOrDuplicated");}
    private static void setup(net.minecraft.server.MinecraftServer server){
        require(System.getProperty("ftboss.zicaicurio.mode").equals("candidate"),"ExplicitUnchangedStage28DimensionMode");require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLocalPlayer");originalUuid=player.getUUID();overworld=server.overworld();nether=server.getLevel(Level.NETHER);require(nether!=null&&player.serverLevel()==overworld,"ActualNativeOverworldAndNetherLevels");
        for(ServerLevel target:List.of(overworld,nether)){target.setChunkForced(0,0,true);target.getChunk(0,0);for(int offsetX=-2;offsetX<=2;offsetX++)for(int offsetZ=-2;offsetZ<=2;offsetZ++){target.setBlockAndUpdate(new BlockPos(8+offsetX,129,8+offsetZ),Blocks.STONE.defaultBlockState());for(int height=130;height<=132;height++)target.setBlockAndUpdate(new BlockPos(8+offsetX,height,8+offsetZ),Blocks.AIR.defaultBlockState());}}
        player.setGameMode(GameType.SURVIVAL);player.teleportTo(8,130,8);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);
        require(stacks().getSlots()==1&&stacks().getStackInSlot(0).isEmpty()&&pending().isEmpty(),"FreshValidBossRelicSlotForDimensionOnlyFixture");fixture=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Original Boss dimension lifecycle relic fixture"));require(stacks().isItemValid(0,fixture),"OriginalCuriosValidatorAcceptsDimensionFixture");stacks().setStackInSlot(0,fixture.copy());phase=1;
    }
    private static void startSilence(){
        EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(overworld);require(boss!=null,"ActualRegisteredDimensionFixtureBoss");boss.setVariant(8);boss.setMini(false);boss.setGiant(false);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,245,8,0,0);require(overworld.addFreshEntity(boss),"ActualDimensionFixtureBossAdmission");startedAt=overworld.getGameTime();float before=player.getHealth();boolean accepted=player.hurt(player.damageSources().mobAttack(boss),3.0F);require(accepted&&positiveDamagePosts==1&&player.getHealth()<before,"SingleNativePositiveDamageStartsDimensionLifecycle");boss.discard();until=startedAt+100;activeContract();snapshot("before-dimension-transfer");phase=2;
    }
    private static void transfer(ServerLevel target){var moved=player.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(target,new net.minecraft.world.phys.Vec3(8,130,8),net.minecraft.world.phys.Vec3.ZERO,0,0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));require(moved==player&&player.getUUID().equals(originalUuid)&&player.serverLevel()==target,"PublicNativeDimensionTransitionKeepsActualPlayerIdentity");player.setNoGravity(true);}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{
        require(++callbacks<2200,"BoundedNaturalDimensionLifecyclePosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
        if(phase==0){player=event.getServer().getPlayerList().getPlayers().getFirst();if(!clientReady||playerTicks<100)return;setup(event.getServer());return;}
        if(phase==1){if(!clientInitialRelic)return;startSilence();return;}
        require(total()==1,"ExactlyOneOriginalRelicAcrossNaturalDimensionLifecycle");long now=overworld.getGameTime();
        if(phase==2){activeContract();if(now-startedAt<20)return;require(now<until,"TransferOccursBeforeOriginalExpiry");phase=3;transfer(nether);require(transitions.size()==1,"ActualNativeOutboundDimensionEventObserved");activeContract();snapshot("native-nether-arrival-active");return;}
        if(phase==3){require(player.serverLevel()==nether,"ActualPlayerRemainsInDestinationDimension");if(now<until){activeContract();return;}require(pending().isEmpty()&&same(stacks().getStackInSlot(0))&&stacks().getStackInSlot(0).getCount()==1,"OriginalOverworldClockExpiresPendingWhilePlayerInNether");if(!clientNetherSuppressed||!clientNetherRestored){require(now<until+100,"BoundedActualClientNetherSuppressionAndRestorationSync");return;}snapshot("native-nether-restored-at-original-expiry");phase=4;transfer(overworld);returnedAt=now;require(transitions.size()==2,"ActualNativeReturnDimensionEventObserved");snapshot("native-overworld-return");return;}
        require(player.serverLevel()==overworld&&pending().isEmpty()&&same(stacks().getStackInSlot(0))&&total()==1,"NativeReturnHasNoStalePendingOrDuplicateRelic");if(now-returnedAt<40||!clientOverworldRestored){require(now<returnedAt+150,"BoundedActualClientOverworldReturnSync");return;}require(positiveDamagePosts==1&&transitions.size()==2,"ExactlyOneHitAndTwoNativeDimensionTransitions");require(clientNetherSuppressed&&clientNetherRestored&&clientOverworldRestored,"ActualClientObservedBothDimensionItemStatesAndReturn");snapshot("stable-after-native-roundtrip");finish(null);
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null){clientReady=true;var inventory=CuriosApi.getCuriosInventory(client.player);if(inventory.isPresent()){var handler=inventory.get().getCurios().get("boss_relic");if(handler!=null&&handler.getStacks().getSlots()>0){ItemStack actual=handler.getStacks().getStackInSlot(0);if(phase==1&&same(actual)&&actual.getCount()==1)clientInitialRelic=true;if(client.level.dimension()==Level.NETHER&&phase==3){if(actual.isEmpty())clientNetherSuppressed=true;if(same(actual)&&actual.getCount()==1)clientNetherRestored=true;}if(client.level.dimension()==Level.OVERWORLD&&phase==4&&same(actual)&&actual.getCount()==1)clientOverworldRestored=true;}}}
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Original Boss dimension lifecycle",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-dimension-lifecycle-native",settings,new WorldOptions(14112028L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        if(++clientTicks>6000)throw new AssertionError("BoundedNativeDimensionLifecycleClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("transitions",new ArrayList<>(transitions));result.put("snapshots",new ArrayList<>(snapshots));result.put("positiveDamagePosts",positiveDamagePosts);result.put("naturalPlayerPostCallbacks",playerTicks);result.put("clientNetherSuppressed",clientNetherSuppressed);result.put("clientNetherRestored",clientNetherRestored);result.put("clientOverworldRestored",clientOverworldRestored);result.put("curiosDimensionLifecycleAccepted",failure==null&&transitions.size()==2&&clientNetherSuppressed&&clientNetherRestored&&clientOverworldRestored);result.put("fourFallbackAndEighteenOwnerControlsRerun",false);result.put("newJvmLoginAccepted",false);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("fullCuriosLifecycleMatrixAccepted",false);result.put("scope","Unchanged Stage28;fresh public native world andactual vanilla local player;one actual positive Boss hit,public native dimension transfer withactual event andclient level change while original pending active,natural original overworld expiry inNether,exact single original component-preserving relic,public return and40natural stableticks. No private helper,synthetic player/event,manual entity/effect tick,clock mutation,portal collision/UI,new-JVM reload,previous four fallback/18owner-control rerun,authenticated multiplayer orfull Boss acceptance.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
