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
public final class ZicaiCurioClientProbeV9 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> cases=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> damageRows=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady,clientEquipped,clientSuppressed,clientFallback,awaitingFallback;
    private static int callbacks,clientTicks,playerTicks,scenario,phase,freeInventorySlot=-1;
    private static long hitAt,until,completedAt;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity boss;
    private static ICuriosItemHandler inventory;
    private static top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler removedHandler;
    private static ItemStack fixture,blocker;
    public ZicaiCurioClientProbeV9(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV9::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV9::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV9::playerTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV9::damage);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual,ItemStack expected){return expected!=null&&ItemStack.isSameItemSameComponents(actual,expected);}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static IDynamicStackHandler stacks(){var handler=inventory.getCurios().get("boss_relic");return handler==null?null:handler.getStacks();}
    private static int slotCount(){var handler=inventory.getCurios().get("boss_relic");return handler==null?-1:handler.getSlots();}
    private static int curioCount(ItemStack expected){int total=0;for(var handler:inventory.getCurios().values()){for(int index=0;index<handler.getSlots();index++){ItemStack actual=handler.getStacks().getStackInSlot(index);if(same(actual,expected))total+=actual.getCount();}for(int index=0;index<handler.getCosmeticStacks().getSlots();index++){ItemStack actual=handler.getCosmeticStacks().getStackInSlot(index);if(same(actual,expected))total+=actual.getCount();}}return total;}
    private static int mainCount(){int total=0;for(int index=0;index<player.getInventory().getContainerSize();index++){ItemStack actual=player.getInventory().getItem(index);if(same(actual,fixture))total+=actual.getCount();}return total;}
    private static int pendingCount(){int total=0;for(int index=0;index<pending().size();index++){ItemStack actual=ItemStack.parseOptional(player.registryAccess(),pending().getCompound(index).getCompound("Stack"));if(same(actual,fixture))total+=actual.getCount();}return total;}
    private static int drops(){return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(64),entity->same(entity.getItem(),fixture)).stream().mapToInt(entity->entity.getItem().getCount()).sum();}
    private static void conserved(String label){require(curioCount(fixture)+mainCount()+pendingCount()==1&&drops()==0,"ExactlyOneOriginalItemAcrossAllSlotsInventoryPendingAndNoDrop:"+label);}
    private static void record(String label){cases.add(Map.of("scenario",scenario,"phase",label,"gameTime",level.getGameTime(),"until",until,"slotCount",slotCount(),"curioFixtureCount",curioCount(fixture),"inventoryFixtureCount",mainCount(),"pendingFixtureCount",pendingCount(),"blockerCount",curioCount(blocker),"worldDropCount",drops()));}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(event.getEntity()==player&&event.getSource().getEntity()==boss)damageRows.add(Map.of("scenario",scenario,"gameTime",level.getGameTime(),"finalDamage",event.getNewDamage()));}
    private static void setup(){
        require(System.getProperty("ftboss.zicaicurio.mode").equals("candidate"),"ExplicitUnchangedStage28Candidate");require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLoginPlayer");
        player.setGameMode(GameType.SURVIVAL);level.setChunkForced(0,0,true);level.getChunk(0,0);for(int horizontal=5;horizontal<12;horizontal++)for(int depth=5;depth<12;depth++)level.setBlockAndUpdate(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState());player.teleportTo(8,70,8);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);
        inventory=CuriosApi.getCuriosInventory(player).orElseThrow();require(slotCount()==1&&stacks().getStackInSlot(0).isEmpty()&&pending().isEmpty(),"FreshOriginalBossRelicSlotAndPersistentState");
        fixture=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Boss original Curios fallback fixture"));blocker=fixture.copy();blocker.set(DataComponents.CUSTOM_NAME,Component.literal("Boss public slot-mutation blocker fixture"));require(stacks().isItemValid(0,fixture)&&stacks().isItemValid(0,blocker),"ActualOriginalSlotValidatorAcceptsBothFixturesBeforeSilence");stacks().setStackInSlot(0,fixture.copy());conserved("initial-equipment");
        boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory");boss.setVariant(8);boss.setMini(false);boss.setGiant(false);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,240,8,0,0);require(level.addFreshEntity(boss),"ActualEligibleBossAdmitted");phase=1;
    }
    private static void startCase(){
        require(pending().isEmpty()&&curioCount(fixture)==1&&mainCount()==0,"ExactlyOneEquippedFixtureBeforeCase:"+scenario);clientSuppressed=false;clientFallback=false;awaitingFallback=false;hitAt=level.getGameTime();until=hitAt+100;int beforePosts=damageRows.size();float health=player.getHealth();require(player.hurt(player.damageSources().mobAttack(boss),4.0F),"ActualPublicPositiveDamage:"+scenario);require(player.getHealth()<health&&damageRows.size()==beforePosts+1&&((Number)damageRows.getLast().get("finalDamage")).floatValue()>0,"ActualFinalPositiveNativePost:"+scenario);require(pending().size()==1&&pending().getCompound(0).getLong("Until")==until&&stacks().getStackInSlot(0).isEmpty(),"OriginalPendingRecordFromActualDamage:"+scenario);
        if(scenario==0){inventory.lockSlotType("boss_relic");require(slotCount()==0,"PublicCuriosLockActuallyRemovesSlotIndex");}
        else if(scenario==1){removedHandler=inventory.getCurios().get("boss_relic");var changed=new LinkedHashMap<>(inventory.getCurios());changed.remove("boss_relic");inventory.setCurios(changed);require(slotCount()==-1,"PublicCuriosSetRemovesActualSlotIdentifier");}
        else{stacks().setStackInSlot(0,blocker.copy());require(curioCount(blocker)==1,"ExternalPublicHandlerMutationOccupiesOriginalSlot:"+scenario);}
        if(scenario==3){freeInventorySlot=-1;for(int index=0;index<36;index++)if(player.getInventory().getItem(index).isEmpty()){ItemStack filler=new ItemStack(Items.STONE,64);filler.set(DataComponents.CUSTOM_NAME,Component.literal("Owned fallback capacity fixture"));player.getInventory().setItem(index,filler);if(freeInventorySlot<0)freeInventorySlot=index;}require(freeInventorySlot>=0,"OwnedCapacityFixtureCanLaterReleaseExactlyOneSlot");for(int index=0;index<36;index++)require(!player.getInventory().getItem(index).isEmpty(),"MainInventoryActuallyFull:"+index);}
        conserved("post-mutation-"+scenario);record("post-mutation");phase=2;
    }
    private static void prepareNext(){
        if(scenario==0){inventory.growSlotType("boss_relic",1);require(slotCount()==1,"PublicCuriosSlotCountRestoredAfterAcceptedCase");}
        if(scenario==1){var changed=new LinkedHashMap<>(inventory.getCurios());changed.put("boss_relic",removedHandler);inventory.setCurios(changed);require(slotCount()==1,"PublicCuriosIdentifierRestoredAfterAcceptedCase");}
        require(pending().isEmpty()&&mainCount()==1,"AcceptedCaseFixtureReadyForPublicInventoryTransfer");ItemStack moved=ItemStack.EMPTY;for(int index=0;index<player.getInventory().getContainerSize();index++)if(same(player.getInventory().getItem(index),fixture)){moved=player.getInventory().removeItem(index,1);break;}require(same(moved,fixture)&&moved.getCount()==1,"ExistingFixtureReusedWithoutRecreation");stacks().setStackInSlot(0,moved);scenario++;clientEquipped=false;clientSuppressed=false;clientFallback=false;awaitingFallback=false;phase=1;conserved("next-case-equipment");
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{
        require(++callbacks<2200,"BoundedNaturalServerPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
        if(phase==0){player=event.getServer().getPlayerList().getPlayers().getFirst();level=player.serverLevel();if(!clientReady||playerTicks<100)return;setup();return;}
        if(phase==1){if(clientEquipped)startCase();return;}
        long now=level.getGameTime();conserved("natural-"+scenario+"-"+(now-hitAt));
        if(phase==2){
            if(now<until){require(pendingCount()==1&&mainCount()==0,"NoEarlyFallbackBeforeOriginalExpiry:"+scenario);if(now==until-1)record("until-minus-one");return;}
            if(scenario==3&&now<=until+5){require(pending().size()==1&&pendingCount()==1&&mainCount()==0&&curioCount(blocker)==1,"ExpiredFullInventoryRetainsExactPendingItem:"+(now-until));if(now==until)record("full-inventory-expiry");if(now==until+5){player.getInventory().setItem(freeInventorySlot,ItemStack.EMPTY);record("capacity-publicly-released");}return;}
            require(pending().isEmpty()&&mainCount()==1&&curioCount(fixture)==0,"OriginalFallbackRestoresOnlyToPlayerInventory:"+scenario);if(scenario>=2)require(curioCount(blocker)==1,"OccupiedCurioNotOverwritten:"+scenario);if(scenario==0)require(slotCount()==0,"MissingIndexStillMissingAtFallback");if(scenario==1)require(slotCount()==-1,"MissingIdentifierStillMissingAtFallback");record("restored-to-inventory");completedAt=now;awaitingFallback=true;phase=3;return;
        }
        if(phase==3){require(now-completedAt<100,"BoundedActualClientFallbackSync");if(now<=completedAt||!clientSuppressed||!clientFallback)return;record("actual-client-inventory-fallback-seen");if(scenario==3){require(damageRows.size()==4,"ExactlyFourDistinctFallbackNativeHits");finish(null);}else prepareNext();}
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Original Boss Curios fallback native fixture",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-fallback-native",settings,new WorldOptions(14112026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        if(++clientTicks>5000)throw new AssertionError("BoundedNaturalClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
        if(client.player!=null&&fixture!=null){int main=0;for(int index=0;index<client.player.getInventory().getContainerSize();index++){ItemStack actual=client.player.getInventory().getItem(index);if(same(actual,fixture))main+=actual.getCount();}var optional=CuriosApi.getCuriosInventory(client.player);int equipped=0;if(optional.isPresent()){var handler=optional.get().getCurios().get("boss_relic");if(handler!=null)for(int index=0;index<handler.getSlots();index++){ItemStack actual=handler.getStacks().getStackInSlot(index);if(same(actual,fixture))equipped+=actual.getCount();}}if(phase==1&&equipped==1&&main==0)clientEquipped=true;if(phase>=2&&main==0&&equipped==0)clientSuppressed=true;if(awaitingFallback&&clientSuppressed&&main==1&&equipped==0)clientFallback=true;}
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("cases",new ArrayList<>(cases));result.put("damageRows",new ArrayList<>(damageRows));result.put("naturalPlayerPostCallbacks",playerTicks);result.put("naturalServerPostCallbacks",callbacks);result.put("curiosFallbackFourCasesAccepted",failure==null&&scenario==3&&clientFallback);result.put("scopedActualClientFallbackSyncAccepted",failure==null&&clientFallback);result.put("fixtureRecreatedBetweenCases",false);result.put("nativeWorldOpenWarningInteractedWith",false);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("installable",false);result.put("deployed",false);result.put("scope","Unchanged Stage28,fresh public world andactual local integrated player;four actual positive Boss hits,natural expiry andpublic Curios lock/removal/occupied-slot mutations;full-inventory pending retention until public capacity release;exactly one original item,no target loss/duplication/drop,blocker not overwritten,andactual client main inventory synchronization. External public slot mutation fixture isnot player equip UI. No failed seed,private gameplay call,manual entity ticks,authenticated multiplayer orfull lifecycle claim.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
