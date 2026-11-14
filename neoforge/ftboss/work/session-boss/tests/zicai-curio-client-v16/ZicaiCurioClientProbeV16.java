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
public final class ZicaiCurioClientProbeV16 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> hits=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady,clientInitial,clientBothSuppressed,clientFinal;
    private static int callbacks,clientTicks,playerTicks,phase,strikes,positivePosts,refreshSelections;
    private static long lastHit,maxUntil,stableAt;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity boss;
    private static final List<ItemStack> fixtures=new ArrayList<>();
    private static final java.util.Set<Integer> selected=new java.util.HashSet<>();
    public ZicaiCurioClientProbeV16(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV16::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV16::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV16::playerTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV16::damage);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual,int index){return fixtures.size()==3&&ItemStack.isSameItemSameComponents(actual,fixtures.get(index));}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler handler(){return CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("boss_relic");}
    private static int total(int fixture){int count=0;for(var actualHandler:CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values()){for(int index=0;index<actualHandler.getStacks().getSlots();index++)if(same(actualHandler.getStacks().getStackInSlot(index),fixture))count+=actualHandler.getStacks().getStackInSlot(index).getCount();for(int index=0;index<actualHandler.getCosmeticStacks().getSlots();index++)if(same(actualHandler.getCosmeticStacks().getStackInSlot(index),fixture))count+=actualHandler.getCosmeticStacks().getStackInSlot(index).getCount();}for(int index=0;index<player.getInventory().getContainerSize();index++)if(same(player.getInventory().getItem(index),fixture))count+=player.getInventory().getItem(index).getCount();for(int index=0;index<pending().size();index++){var actual=ItemStack.parseOptional(player.registryAccess(),pending().getCompound(index).getCompound("Stack"));if(same(actual,fixture))count+=actual.getCount();}return count;}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(event.getEntity()==player&&event.getNewDamage()>0)positivePosts++;}
    private static Map<Integer,net.minecraft.nbt.CompoundTag> records(){Map<Integer,net.minecraft.nbt.CompoundTag> result=new java.util.HashMap<>();for(int index=0;index<pending().size();index++){var row=pending().getCompound(index);int slot=row.getInt("Index");require(row.getString("Slot").equals("boss_relic")&&slot>=0&&slot<2&&!result.containsKey(slot),"OriginalMultiItemPendingSlotIdentityUnique");var actual=ItemStack.parseOptional(player.registryAccess(),row.getCompound("Stack"));require(same(actual,slot)&&actual.getCount()==1,"OriginalMultiItemPendingCountAndComponents");result.put(slot,row.copy());}return result;}
    private static void setup(){level=player.serverLevel();require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaMultiItemPlayer");level.setChunkForced(0,0,true);level.getChunk(0,0);level.setBlockAndUpdate(new BlockPos(8,69,8),Blocks.STONE.defaultBlockState());player.setGameMode(GameType.SURVIVAL);player.teleportTo(8,70,8);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);require(pending().isEmpty()&&handler().getStacks().getStackInSlot(0).isEmpty(),"FreshNativeMultiItemState");CuriosApi.getCuriosInventory(player).orElseThrow().growSlotType("boss_relic",1);require(handler().getStacks().getSlots()==2&&handler().getCosmeticStacks().getSlots()==2,"PublicCuriosGrowCreatesTwoActualAndCosmeticSlots");for(int index=0;index<3;index++){var actual=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));actual.set(DataComponents.CUSTOM_NAME,Component.literal("Original Boss multi-item relic "+index));fixtures.add(actual);}for(int index=0;index<2;index++){require(handler().getStacks().isItemValid(index,fixtures.get(index)),"OriginalValidMultiItemSlot:"+index);handler().getStacks().setStackInSlot(index,fixtures.get(index).copy());}require(handler().getCosmeticStacks().isItemValid(0,fixtures.get(2)),"OriginalValidCosmeticControlRelic");handler().getCosmeticStacks().setStackInSlot(0,fixtures.get(2).copy());boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualMultiItemBossFactory");boss.setVariant(8);boss.setMini(false);boss.setGiant(false);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,245,8,0,0);require(level.addFreshEntity(boss),"ActualMultiItemBossAdmission");phase=1;}
    private static void hit(){var before=records();long now=level.getGameTime();int beforePosts=positivePosts;float health=player.getHealth();require(player.hurt(player.damageSources().mobAttack(boss),3)&&positivePosts==beforePosts+1&&player.getHealth()<health,"ActualIndependentPositiveMultiItemDamage");var after=records();List<Integer> changed=new ArrayList<>();for(var entry:after.entrySet()){var previous=before.get(entry.getKey());if(previous==null||!previous.equals(entry.getValue()))changed.add(entry.getKey());}require(changed.size()==1,"EveryActualMultiItemHitSelectsExactlyOneChoice");int choice=changed.getFirst();var row=after.get(choice);require(row.getLong("Until")==now+100&&handler().getStacks().getStackInSlot(choice).isEmpty(),"ChosenOriginalSlotHasExact100NaturalTickExpiry");if(before.containsKey(choice)){refreshSelections++;require(after.size()==before.size(),"MultiItemRefreshDoesNotDuplicatePendingRows");}else require(after.size()==before.size()+1,"NewMultiItemChoiceAddsExactlyOnePendingRow");for(var entry:before.entrySet())if(entry.getKey()!=choice)require(entry.getValue().equals(after.get(entry.getKey())),"NonselectedPendingRecordUnchanged");selected.add(choice);strikes++;lastHit=now;maxUntil=Math.max(maxUntil,now+100);hits.add(Map.of("hit",strikes,"gameTime",now,"choice",choice,"beforeRows",before.size(),"afterRows",after.size(),"refresh",before.containsKey(choice),"until",now+100));require(strikes<=64,"BoundedObservedMultiItemChoicesWithoutRngReset");if(selected.size()==2&&refreshSelections>0&&after.size()==2){phase=2;boss.discard();}}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{require(++callbacks<2600,"BoundedNaturalMultiItemPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;if(phase==0){player=event.getServer().getPlayerList().getPlayers().getFirst();if(!clientReady||playerTicks<100)return;setup();return;}for(int index=0;index<3;index++)require(total(index)==1,"GlobalExactlyOneEachOriginalMultiItemRelic:"+index);require(same(handler().getCosmeticStacks().getStackInSlot(0),2)&&handler().getCosmeticStacks().getStackInSlot(0).getCount()==1,"OriginalCosmeticRelicNeverSelectedOrRemoved");long now=level.getGameTime();if(phase==1){if(!clientInitial)return;if(strikes==0||now-lastHit>=22)hit();return;}if(phase==2){records();if(now<maxUntil)return;require(pending().isEmpty()&&same(handler().getStacks().getStackInSlot(0),0)&&same(handler().getStacks().getStackInSlot(1),1),"EveryMultiItemRecordRestoredToOriginalSlotAtOwnExpiry");require(clientBothSuppressed,"ActualClientSawBothNoncosmeticSlotsSuppressed");stableAt=now;phase=3;return;}require(pending().isEmpty()&&same(handler().getStacks().getStackInSlot(0),0)&&same(handler().getStacks().getStackInSlot(1),1),"MultiItemStableRestorationNoLateDuplication");if(now-stableAt<40||!clientFinal){require(now-stableAt<150,"BoundedActualClientMultiItemFinalSync");return;}require(selected.size()==2&&refreshSelections>0&&positivePosts==strikes&&clientFinal,"BothOriginalChoiceBranchesCosmeticExclusionAndNativeClientAccepted");finish(null);}catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{var client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null){clientReady=true;var inventory=CuriosApi.getCuriosInventory(client.player);if(inventory.isPresent()&&fixtures.size()==3){var actual=inventory.get().getCurios().get("boss_relic");if(actual!=null&&actual.getStacks().getSlots()==2&&actual.getCosmeticStacks().getSlots()>0){boolean cosmetic=same(actual.getCosmeticStacks().getStackInSlot(0),2);if(phase==1&&same(actual.getStacks().getStackInSlot(0),0)&&same(actual.getStacks().getStackInSlot(1),1)&&cosmetic)clientInitial=true;if(phase==2&&actual.getStacks().getStackInSlot(0).isEmpty()&&actual.getStacks().getStackInSlot(1).isEmpty()&&cosmetic)clientBothSuppressed=true;if(phase==3&&same(actual.getStacks().getStackInSlot(0),0)&&same(actual.getStacks().getStackInSlot(1),1)&&cosmetic)clientFinal=true;}}}
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);var settings=new LevelSettings("Original Boss multi-item selection",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-multi-item-native",settings,new WorldOptions(14112032L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}if(++clientTicks>6000)throw new AssertionError("BoundedMultiItemClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("hits",new ArrayList<>(hits));result.put("multiItemLifecycleAccepted",failure==null&&selected.size()==2&&refreshSelections>0&&clientFinal);result.put("positiveDamagePosts",positivePosts);result.put("refreshSelections",refreshSelections);result.put("observedSelectedIndices",new ArrayList<>(selected));result.put("clientBothSuppressed",clientBothSuppressed);result.put("clientFinal",clientFinal);result.put("naturalPlayerPostCallbacks",playerTicks);result.put("priorAcceptedControlsRerun",false);result.put("newJvmLoginAccepted",false);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("scope","Actual public Curios slotgrowth to2validatednoncosmetic originalrelics plus1validatedcosmetic;one unchanged nativeBoss withnaturallyspaced realpositivehits,originalrandomchoiceobservedwithout RNG reset ordistributionclaim,onechangedrecordperhit,new/refreshbranches,cosmeticexcluded,eachoriginalrecordexpiresnaturally,allthreeexactlyonewithcomponentsandactualclientstates. No directhelper,eventposts,manualticks,privatefieldmutation/newJVM bypass/priorcontrolrerun/fullmultiplayerclaim.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
