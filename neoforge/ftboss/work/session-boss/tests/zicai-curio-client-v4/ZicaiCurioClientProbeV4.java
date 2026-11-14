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
public final class ZicaiCurioClientProbeV4 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> damageRows=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> snapshots=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean fixtureInstalled,prepared,serverComplete,clientPlayerReady,clientSawFixture,clientSawSuppression,clientSawRestore,serverSuppressionActive;
    private static volatile String slotName;
    private static int slotIndex,callbacks,clientTicks,playerPostTicks,preparedPlayerTicks;
    private static long startedAt,expectedUntil;
    private static boolean worldRequested,refreshed;
    private static volatile boolean deathTriggered,deathEventObserved,respawnEventObserved,respawnRequested,clientSawRespawnFixture,logoutCycle;
    private static long respawnAt;
    private static ServerLevel level;
    private static ServerPlayer player;
    private static EasterBossEntity boss;
    private static IDynamicStackHandler stacks;
    private static ItemStack fixture;
    public ZicaiCurioClientProbeV4(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV4::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV4::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV4::damage);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV4::playerTick);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV4::death);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV4::respawn);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV4::loggedOut);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return fixture!=null&&actual.getCount()==fixture.getCount()&&ItemStack.isSameItemSameComponents(actual,fixture);}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static void snapshot(String phase){snapshots.add(Map.of("phase",phase,"gameTime",level.getGameTime(),"slot",slotName,"index",slotIndex,"equippedFixture",same(stacks.getStackInSlot(slotIndex)),"pendingRows",pending().size(),"pendingUntil",pending().isEmpty()?-1L:pending().getCompound(0).getLong("Until"),"playerHealth",player.getHealth()));}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerPostTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(!finished.get()&&event.getEntity()==player&&event.getSource().getEntity()==boss)damageRows.add(Map.of("gameTime",level.getGameTime(),"finalDamage",event.getNewDamage(),"sourceOwnerMatchesActualEligibleBoss",true));}
    private static TriState equipQuery(boolean cosmetic,int index){CurioCanEquipEvent event=new CurioCanEquipEvent(fixture.copy(),new SlotContext(slotName,player,index,cosmetic,true));NeoForge.EVENT_BUS.post(event);return event.getEquipResult();}
    private static void pendingContract(String phase){
        require(pending().size()==1,"ExactlyOneOriginalPendingRecord:"+phase);
        var record=pending().getCompound(0);
        require(record.getString("Slot").equals(slotName)&&record.getInt("Index")==slotIndex,"OriginalPendingSlotAndIndex:"+phase);
        require(record.getLong("Until")==expectedUntil,"Original100TickExpiryOrRefresh:"+phase);
        require(same(ItemStack.parseOptional(player.registryAccess(),record.getCompound("Stack"))),"ActualRegistryAwareItemStackCodecPreservesCountAndComponents:"+phase);
        require(stacks.getStackInSlot(slotIndex).isEmpty(),"ActualNoncosmeticCuriosSlotSuppressed:"+phase);
    }
    private static void fixture(){
        require(System.getProperty("ftboss.zicaicurio.mode","baseline").equals("candidate"),"CandidateModeExplicit");
        require(player.getGameProfile().getName().equals("ft_fx_probe")&&!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLoginPlayer");
        player.setGameMode(GameType.SURVIVAL);require(!player.isCreative()&&!player.isSpectator(),"OriginalPlayerEligibility");
        level.setChunkForced(0,0,true);level.getChunk(0,0);for(int horizontal=4;horizontal<=15;horizontal++)for(int depth=4;depth<=15;depth++)level.setBlockAndUpdate(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState());
        player.teleportTo(8,70,13);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);
        ICuriosItemHandler inventory=CuriosApi.getCuriosInventory(player).orElseThrow();
        for(var entry:inventory.getCurios().entrySet()){IDynamicStackHandler candidate=entry.getValue().getStacks();for(int index=0;index<candidate.getSlots();index++)if(candidate.getStackInSlot(index).isEmpty()){slotName=entry.getKey();slotIndex=index;stacks=candidate;break;}if(stacks!=null)break;}
        require(stacks!=null&&pending().isEmpty(),"ExistingActualNoncosmeticSlotAndFreshPersistentState");
        fixture=new ItemStack(Items.GOLD_NUGGET);fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Boss baseline nonweapon Curios fixture"));stacks.setStackInSlot(slotIndex,fixture.copy());require(same(stacks.getStackInSlot(slotIndex)),"PublicFixtureItemAndComponentsInserted");fixtureInstalled=true;
    }
    private static void hit(){
        int damageBefore=damageRows.size();require(clientSawFixture,"ActualClientSawEquippedFixtureBeforeDamage");require(equipQuery(false,slotIndex)!=TriState.FALSE,"NoPrematureEquipDenial");
        boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory");boss.setVariant(8);boss.setMini(false);boss.setGiant(false);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,240,8,0,0);require(level.addFreshEntity(boss),"ActualRegisteredBossAdmission");require(boss.getVariant()==8&&!boss.isMini()&&!boss.isGiant(),"OriginalEligibleOwner");
        startedAt=level.getGameTime();expectedUntil=startedAt+100;preparedPlayerTicks=playerPostTicks;snapshot("before-hit");
        float before=player.getHealth();require(player.hurt(player.damageSources().mobAttack(boss),4.0F),"ActualNativePublicPositiveDamagePipeline");require(player.getHealth()<before&&!damageRows.isEmpty()&&((Number)damageRows.get(damageBefore).get("finalDamage")).floatValue()>0,"ActualFinalPositiveOwnerDamageAndHealthDelta");
        pendingContract("first-hit");snapshot("after-first-hit");
        require(equipQuery(false,slotIndex)==TriState.FALSE,"ActualEventBusSilencedSlotDeniesNoncosmeticEquip");require(equipQuery(true,slotIndex)!=TriState.FALSE,"OriginalCosmeticEquipException");require(equipQuery(false,slotIndex+1)!=TriState.FALSE,"OriginalDifferentSlotIndexException");
        before=player.getHealth();require(player.hurt(player.damageSources().mobAttack(boss),8.0F),"SecondSameTickFinalPositiveNativeHit");require(player.getHealth()<before&&damageRows.size()==damageBefore+2,"SecondSameTickActualHealthAndPostDamage");pendingContract("same-tick-second-hit");snapshot("after-same-tick-second-hit");
        prepared=true;serverSuppressionActive=true;
    }
    private static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event){
        if(event.getEntity()!=player||!deathTriggered||finished.get())return;
        try{require(pending().isEmpty()&&same(stacks.getStackInSlot(slotIndex)),"OriginalHighestPriorityDeathRestoresPendingBeforeNativeDeathProcessing");deathEventObserved=true;serverSuppressionActive=false;snapshot("actual-native-death-after-helper");boss.discard();}catch(Throwable failure){finish(failure);}
    }
    private static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event){
        if(!(event.getEntity() instanceof ServerPlayer actual)||!actual.getGameProfile().getName().equals("ft_fx_probe")||!deathTriggered||finished.get())return;
        try{require(actual!=player&&actual.getUUID().equals(player.getUUID()),"ActualNativeRespawnReplacedServerPlayerWithSameAccountIdentity");player=actual;level=player.serverLevel();stacks=CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get(slotName).getStacks();require(pending().isEmpty()&&same(stacks.getStackInSlot(slotIndex)),"NativeKeepInventoryRespawnPreservesRestoredCurioExactlyOnce");respawnEventObserved=true;respawnAt=level.getGameTime();serverSuppressionActive=false;snapshot("actual-native-respawn-after-helper");}catch(Throwable failure){finish(failure);}
    }
    private static void loggedOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event){
        if(event.getEntity()!=player||!logoutCycle)return;
        Throwable problem=null;int count=0;long now=level.getGameTime();
        try{
            require(now<expectedUntil,"ActualNativeLogoutOccursBefore100TickExpiry");require(pending().isEmpty(),"OriginalLogoutForceRestorationClearsPending");
            var inventory=CuriosApi.getCuriosInventory(player);if(inventory.isPresent())for(var entry:inventory.get().getCurios().values())for(int index=0;index<entry.getStacks().getSlots();index++){ItemStack stack=entry.getStacks().getStackInSlot(index);if(ItemStack.isSameItemSameComponents(stack,fixture))count+=stack.getCount();}
            for(int index=0;index<player.getInventory().getContainerSize();index++){ItemStack stack=player.getInventory().getItem(index);if(ItemStack.isSameItemSameComponents(stack,fixture))count+=stack.getCount();}
            require(count==1,"ActualLogoutRestoresExactlyOneItemWithOriginalComponentsNoLossOrDuplication");
        }catch(Throwable failure){problem=failure;}
        try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",problem==null?0:1);result.put("failure",problem==null?null:problem.toString());result.put("actualNativeLogoutObserved",true);result.put("logoutGameTime",now);result.put("pendingUntilBeforeLogout",expectedUntil);result.put("beforeNaturalExpiry",now<expectedUntil);result.put("remainingPendingRows",pending().size());result.put("matchingCurioAndInventoryItemCount",count);result.put("actualDeathObserved",deathEventObserved);result.put("actualRespawnObserved",respawnEventObserved);result.put("actualClientSawRespawnFixture",clientSawRespawnFixture);result.put("authenticated",false);result.put("fullPersistenceNewJvmLoginAccepted",false);Files.writeString(Path.of("actual-zicai-curio-logout.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception failure){failure.printStackTrace();}
    }
    private static void serverTick(ServerTickEvent.Post event){
        if(finished.get()||!event.getServer().isSingleplayer())return;
        try{
            callbacks++;require(callbacks<2000,"BoundedNaturalServerPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
            if(!prepared){player=event.getServer().getPlayerList().getPlayers().getFirst();level=player.serverLevel();if(!clientPlayerReady||playerPostTicks<100)return;if(!fixtureInstalled){fixture();return;}if(!clientSawFixture)return;hit();return;}
            long now=level.getGameTime(),elapsed=now-startedAt;
            if(!deathTriggered){pendingContract("before-actual-death-"+elapsed);if(elapsed>=20){require(clientSawSuppression,"ActualClientObservedSuppressionBeforeDeath");deathTriggered=true;require(player.hurt(player.damageSources().fellOutOfWorld(),100000.0F),"ActualNativeFatalDamagePipeline");require(player.isDeadOrDying()&&deathEventObserved,"ActualNativeDeathAndOriginalRestoreCallbackObserved");}return;}
            if(!respawnEventObserved)return;
            if(!logoutCycle){require(pending().isEmpty(),"NoStalePendingAfterActualRespawn");if(now-respawnAt<100||!clientSawRespawnFixture)return;require(player.isAlive()&&same(stacks.getStackInSlot(slotIndex)),"ActualNativeRespawnPlayerAndCurioReady");player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);player.setNoGravity(true);clientSawSuppression=false;hit();logoutCycle=true;return;}
            pendingContract("before-actual-logout-"+elapsed);
            if(elapsed>=5&&clientSawSuppression){require(deathEventObserved&&respawnEventObserved&&clientSawRespawnFixture,"ActualDeathRespawnAndClientRestoreAllObserved");snapshot("pending-before-real-client-stop-and-logout");serverComplete=true;finish(null);}
        }catch(Throwable failure){finish(failure);}
    }
    private static void clientTick(ClientTickEvent.Post event){
        if(finished.get())return;
        try{
            Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientPlayerReady=true;
            if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Boss Zicai Curios candidate fixture",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-candidate-native",settings,new WorldOptions(13282026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
            if(deathTriggered&&!respawnRequested&&client.player!=null&&client.player.isDeadOrDying()&&client.getConnection()!=null){respawnRequested=true;client.getConnection().send(new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));}if(++clientTicks>4000)throw new AssertionError("BoundedNativeClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
            if(client.player!=null&&slotName!=null&&fixture!=null){var inventory=CuriosApi.getCuriosInventory(client.player);if(inventory.isPresent()){var clientStacks=inventory.get().getCurios().get(slotName);if(clientStacks!=null&&slotIndex<clientStacks.getStacks().getSlots()){ItemStack actual=clientStacks.getStacks().getStackInSlot(slotIndex);if(fixtureInstalled&&!prepared&&same(actual))clientSawFixture=true;if(prepared&&serverSuppressionActive&&actual.isEmpty())clientSawSuppression=true;if(respawnEventObserved&&client.player.isAlive()&&same(actual))clientSawRespawnFixture=true;if(serverComplete&&same(actual))clientSawRestore=true;}}}
            
        }catch(Throwable failure){finish(failure);}
    }
    private static void finish(Throwable failure){
        if(!finished.compareAndSet(false,true))return;
        Minecraft.getInstance().execute(()->{try{
            Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("damageRows",new ArrayList<>(damageRows));result.put("snapshots",new ArrayList<>(snapshots));result.put("naturalServerPostCallbacks",callbacks);result.put("naturalPlayerPostCallbacks",playerPostTicks);result.put("clientTicks",clientTicks);result.put("clientSawFixture",clientSawFixture);result.put("clientSawSuppression",clientSawSuppression);result.put("clientSawRestore",clientSawRestore);result.put("candidateZicaiSilenceLifecyclePreLogoutAccepted",failure==null&&serverComplete&&deathEventObserved&&respawnEventObserved&&clientSawRespawnFixture&&clientSawSuppression);result.put("originalZicaiSilenceAccepted",false);result.put("baselineOriginalZicaiSilenceGapsDemonstrated",false);result.put("actualLocalPlayerNativeLoginAndCuriosSynchronization",failure==null&&clientSawFixture&&clientSawSuppression&&clientSawRespawnFixture);result.put("actualDeathObserved",deathEventObserved);result.put("actualRespawnObserved",respawnEventObserved);result.put("actualClientSawRespawnFixture",clientSawRespawnFixture);result.put("logoutSupplementRequired",true);result.put("fullLifecycleAcceptance",false);result.put("authenticated",false);result.put("fakeOrSyntheticServerPlayerConstructed",false);result.put("privateReflectionDebuggerOrInternalBridgeInvocation",false);result.put("fullBossMigrationComplete",false);result.put("scope","Actual vanilla integrated login andfresh public world;actual registered NoAI Boss directed positive damage,original pending100ticks,second same-tick positive hit,same pending selection refreshed bynatural later hit,registry-aware item component codec,noncosmetic/cosmetic/index event-bus equip queries,natural expiry andactual client equipped/suppressed/restored state synchronization. Actual native death andreal client respawn packet withkeepInventory fixture;second real positive-damage silence followed bynormal client stop andactual logout event checked inseparate post-stop supplement. No manual entity/effect ticks,private gameplay invocation,full equip-action UI,new-JVM persistence,authenticated multiplayer/full AI/battle orframebuffer claim.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
        }catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});
    }
}
