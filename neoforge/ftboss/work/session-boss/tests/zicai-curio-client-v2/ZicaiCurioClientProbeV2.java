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
public final class ZicaiCurioClientProbeV2 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> damageRows=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> snapshots=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean prepared,serverComplete,clientSawFixture,clientPlayerReady;
    private static volatile String slotName;
    private static int slotIndex,callbacks,clientTicks,playerPostTicks,preparedPlayerTicks,gaps;
    private static long startedAt;
    private static boolean worldRequested;
    private static ServerLevel level;
    private static ServerPlayer player;
    private static EasterBossEntity boss;
    private static IDynamicStackHandler stacks;
    private static ItemStack fixture;

    public ZicaiCurioClientProbeV2(){
        NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV2::clientTick);
        NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV2::serverTick);
        NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV2::damage);
        NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV2::playerTick);
    }
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return fixture!=null&&actual.getCount()==fixture.getCount()&&ItemStack.isSameItemSameComponents(actual,fixture);}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static void snapshot(String phase){snapshots.add(Map.of("phase",phase,"gameTime",level.getGameTime(),"slot",slotName,"index",slotIndex,"equippedFixture",same(stacks.getStackInSlot(slotIndex)),"pendingRows",pending().size(),"playerHealth",player.getHealth()));}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerPostTicks++;}
    private static void damage(LivingDamageEvent.Post event){
        if(finished.get()||event.getEntity()!=player||event.getSource().getEntity()!=boss)return;
        damageRows.add(Map.of("gameTime",level.getGameTime(),"finalDamage",event.getNewDamage(),"sourceOwnerMatchesActualEligibleBoss",true));
    }
    private static void prepare(){
        require(System.getProperty("ftboss.zicaicurio.mode","baseline").equals("baseline"),"ThisV2OnlyEstablishesBaselineNeverCandidateAcceptance");
        require(player.getGameProfile().getName().equals("ft_fx_probe")&&!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLoginPlayer");
        require(player.setGameMode(GameType.SURVIVAL)||!player.isCreative(),"ActualSurvivalPlayer");
        require(!player.isCreative()&&!player.isSpectator(),"OriginalPlayerEligibility");
        level.setChunkForced(0,0,true);level.getChunk(0,0);
        for(int horizontal=4;horizontal<=15;horizontal++)for(int depth=4;depth<=15;depth++)level.setBlockAndUpdate(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState());
        player.teleportTo(8,70,13);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);
        ICuriosItemHandler inventory=CuriosApi.getCuriosInventory(player).orElseThrow();
        for(var entry:inventory.getCurios().entrySet()){
            IDynamicStackHandler candidate=entry.getValue().getStacks();
            for(int index=0;index<candidate.getSlots();index++)if(candidate.getStackInSlot(index).isEmpty()){slotName=entry.getKey();slotIndex=index;stacks=candidate;break;}
            if(stacks!=null)break;
        }
        require(stacks!=null,"ExistingActualNoncosmeticCuriosSlotAvailable");
        require(pending().isEmpty(),"FreshPlayerNoSilenceState");
        fixture=new ItemStack(Items.GOLD_NUGGET);fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Boss baseline nonweapon Curios fixture"));
        stacks.setStackInSlot(slotIndex,fixture.copy());require(same(stacks.getStackInSlot(slotIndex)),"PublicCuriosFixtureInsertionPreservesItemAndComponents");
        boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory");boss.setVariant(8);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(13,70,8,0,0);require(level.addFreshEntity(boss),"ActualRegisteredBossAdmission");
        require(boss.getVariant()==8&&!boss.isMini()&&!boss.isGiant(),"OriginalZicaiOwnerEligibility");
        startedAt=level.getGameTime();preparedPlayerTicks=playerPostTicks;snapshot("before-hit");
        float before=player.getHealth();require(player.hurt(player.damageSources().mobAttack(boss),4.0F),"ActualNativePublicPositiveDamagePipeline");
        require(player.getHealth()<before,"ActualNativePlayerHealthDecreased");
        require(!damageRows.isEmpty()&&damageRows.stream().allMatch(row->((Number)row.get("finalDamage")).floatValue()>0),"ActualFinalPositiveOwnerDamageEventObserved");
        snapshot("after-hit");require(same(stacks.getStackInSlot(slotIndex))&&pending().isEmpty(),"BaselineEffectiveSilenceRemovalAndPendingGap");gaps++;
        CurioCanEquipEvent equip=new CurioCanEquipEvent(fixture.copy(),new SlotContext(slotName,player,slotIndex,false,true));NeoForge.EVENT_BUS.post(equip);
        require(equip.getEquipResult()!=TriState.FALSE,"BaselineActualEventBusNoncosmeticEquipDenialGap");gaps++;
        boss.discard();prepared=true;
    }
    private static void serverTick(ServerTickEvent.Post event){
        if(finished.get()||!event.getServer().isSingleplayer())return;
        try{
            callbacks++;require(callbacks<2000,"BoundedNaturalServerPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
            if(!prepared){player=event.getServer().getPlayerList().getPlayers().getFirst();level=player.serverLevel();if(!clientPlayerReady||playerPostTicks<100)return;require(playerPostTicks>=100,"Warmup100ActualNaturalPlayerPostsBeforePositiveHit");prepare();return;}
            long elapsed=level.getGameTime()-startedAt;
            require(same(stacks.getStackInSlot(slotIndex))&&pending().isEmpty(),"BaselineFixtureRemainsEquippedOnNaturalTick:"+elapsed);
            if(elapsed==99||elapsed==100||elapsed==101)snapshot("natural-tick-"+elapsed);
            if(elapsed>=120){require(playerPostTicks-preparedPlayerTicks>=100,"AtLeast100ActualNaturalPlayerPostCallbacks");require(gaps==2,"ExactlyTwoEffectiveOriginalContractGaps");serverComplete=true;}
        }catch(Throwable failure){finish(failure);}
    }
    private static void clientTick(ClientTickEvent.Post event){
        if(finished.get())return;
        try{
            Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientPlayerReady=true;
            if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){
                worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
                LevelSettings settings=new LevelSettings("Boss Zicai Curios baseline fixture",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("zicai-curio-baseline-native",settings,new WorldOptions(13282026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);
            }
            if(++clientTicks>4000)throw new AssertionError("BoundedNativeClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
            if(client.player!=null&&slotName!=null&&fixture!=null){var clientInventory=CuriosApi.getCuriosInventory(client.player);if(clientInventory.isPresent()){var clientStacks=clientInventory.get().getCurios().get(slotName);if(clientStacks!=null&&slotIndex<clientStacks.getStacks().getSlots()&&same(clientStacks.getStacks().getStackInSlot(slotIndex)))clientSawFixture=true;}}
            if(serverComplete&&clientSawFixture){require(true,"ActualLocalClientCuriosFixtureSynchronizationObserved");finish(null);}
        }catch(Throwable failure){finish(failure);}
    }
    private static void finish(Throwable failure){
        if(!finished.compareAndSet(false,true))return;
        Minecraft.getInstance().execute(()->{
            try{
                Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("damageRows",new ArrayList<>(damageRows));result.put("snapshots",new ArrayList<>(snapshots));result.put("originalContractGaps",gaps);result.put("naturalServerPostCallbacks",callbacks);result.put("naturalPlayerPostCallbacks",playerPostTicks);result.put("clientTicks",clientTicks);result.put("clientSawFixture",clientSawFixture);result.put("originalZicaiSilenceAccepted",false);result.put("baselineOriginalZicaiSilenceGapsDemonstrated",failure==null&&gaps==2);result.put("actualLocalPlayerNativeLoginAndCuriosSynchronization",failure==null&&clientSawFixture);result.put("authenticated",false);result.put("fakeOrSyntheticServerPlayerConstructed",false);result.put("privateReflectionDebuggerOrInternalBridgeInvocation",false);result.put("initialImmediateLoginDamageFailureRetained",true);result.put("warmupBeforeDamageNaturalPlayerTicks",100);result.put("candidateImplementedOrAccepted",false);result.put("fullBossMigrationComplete",false);result.put("scope","Actual client public fresh world andvanilla integrated login;registered NoAI eligible variant8Boss public positive owner damage,actual noncosmetic Curios slot andvanilla nonweapon item/component fixture,actual event-bus equip query,100natural player ticks andclient slot synchronization. Baseline only;no natural Boss AI,full battle,authenticated login,complete lifecycle/restore/equip action orcandidate acceptance.");
                Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
            }catch(Exception problem){problem.printStackTrace();}
            Minecraft.getInstance().stop();
        });
    }
}
