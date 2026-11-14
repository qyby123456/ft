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
public final class ZicaiCurioClientProbeV14 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> actions=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady,openRequested,clientMenuObserved,clientInitialFixture,clientFinalState;
    private static volatile int phase;
    private static int callbacks,clientTicks,playerTicks,lastClientAction=-1,deniedNativeEquipEvents,positivePosts;
    private static long until,startedAt,stableAt;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static ItemStack original,replacement;
    public ZicaiCurioClientProbeV14(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV14::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV14::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV14::playerTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV14::damage);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,ZicaiCurioClientProbeV14::equip);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual,ItemStack expected){return expected!=null&&ItemStack.isSameItemSameComponents(actual,expected);}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static IDynamicStackHandler stacks(){return CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("boss_relic").getStacks();}
    private static int total(ItemStack expected){int count=0;for(var handler:CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().values()){for(int index=0;index<handler.getStacks().getSlots();index++){var actual=handler.getStacks().getStackInSlot(index);if(same(actual,expected))count+=actual.getCount();}for(int index=0;index<handler.getCosmeticStacks().getSlots();index++){var actual=handler.getCosmeticStacks().getStackInSlot(index);if(same(actual,expected))count+=actual.getCount();}}for(int index=0;index<player.getInventory().getContainerSize();index++){var actual=player.getInventory().getItem(index);if(same(actual,expected))count+=actual.getCount();}if(same(player.containerMenu.getCarried(),expected))count+=player.containerMenu.getCarried().getCount();for(int index=0;index<pending().size();index++){var actual=ItemStack.parseOptional(player.registryAccess(),pending().getCompound(index).getCompound("Stack"));if(same(actual,expected))count+=actual.getCount();}return count;}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(event.getEntity()==player&&event.getNewDamage()>0)positivePosts++;}
    private static void equip(CurioCanEquipEvent event){if(event.getEntity()==player&&event.getSlotContext().identifier().equals("boss_relic")&&!event.getSlotContext().cosmetic()&&phase==4&&event.getEquipResult()==TriState.FALSE)deniedNativeEquipEvents++;}
    private static void record(String name){actions.add(Map.of("phase",name,"gameTime",level.getGameTime(),"pending",pending().size(),"originalCount",total(original),"replacementCount",total(replacement),"cursorEmpty",player.containerMenu.getCarried().isEmpty(),"deniedEvents",deniedNativeEquipEvents));}
    private static void setup(){level=player.serverLevel();require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaContainerActionPlayer");level.setChunkForced(0,0,true);level.getChunk(0,0);level.setBlockAndUpdate(new BlockPos(8,69,8),Blocks.STONE.defaultBlockState());player.setGameMode(GameType.SURVIVAL);player.teleportTo(8,70,8);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);require(stacks().getSlots()==1&&stacks().getStackInSlot(0).isEmpty()&&pending().isEmpty()&&player.getInventory().getItem(9).isEmpty()&&player.getInventory().getItem(10).isEmpty(),"FreshActualCuriosAndInventoryActionSlots");original=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));original.set(DataComponents.CUSTOM_NAME,Component.literal("Original Boss native-menu suppressed relic"));replacement=original.copy();replacement.set(DataComponents.CUSTOM_NAME,Component.literal("Original Boss native-menu replacement relic"));require(stacks().isItemValid(0,original)&&stacks().isItemValid(0,replacement),"BothOriginalRelicsPassActualSlotValidatorBeforeSilence");stacks().setStackInSlot(0,original.copy());player.getInventory().setItem(9,replacement.copy());player.inventoryMenu.broadcastChanges();phase=1;}
    private static void hit(){var boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualMenuFixtureBoss");boss.setVariant(8);boss.setMini(false);boss.setGiant(false);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,245,8,0,0);require(level.addFreshEntity(boss),"ActualMenuBossAdmission");startedAt=level.getGameTime();until=startedAt+100;require(player.hurt(player.damageSources().mobAttack(boss),3)&&positivePosts==1,"SingleNativeMenuSilenceDamage");boss.discard();require(pending().size()==1&&stacks().getStackInSlot(0).isEmpty(),"NativeMenuActionPendingFixtureReady");phase=2;}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{require(++callbacks<2200,"BoundedNativeContainerActionPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;if(phase==0){player=event.getServer().getPlayerList().getPlayers().getFirst();if(!clientReady||playerTicks<100)return;setup();return;}if(phase==1){if(clientMenuObserved&&clientInitialFixture&&player.containerMenu instanceof top.theillusivec4.curios.common.inventory.container.CuriosContainer)hit();return;}long now=level.getGameTime();require(total(original)==1&&total(replacement)==1,"ActualContainerCursorInventoryPendingConservesEachOriginalRelic");if(phase>=3&&phase<=5)require(now<until,"NativeDeniedEquipAndCursorReturnOccurBeforeExpiry");
        if(phase==2){if(now-startedAt<15)return;phase=3;return;}
        if(phase==3){if(!same(player.containerMenu.getCarried(),replacement)||!player.getInventory().getItem(9).isEmpty())return;record("actual-network-pickup-replacement");phase=4;return;}
        if(phase==4){if(deniedNativeEquipEvents==0)return;require(same(player.containerMenu.getCarried(),replacement)&&stacks().getStackInSlot(0).isEmpty()&&pending().size()==1,"ActualServerContainerClickDeniedDuringSilenceWithoutConsumingCursor");record("actual-network-equip-denied");phase=5;return;}
        if(phase==5){if(!player.containerMenu.getCarried().isEmpty()||!same(player.getInventory().getItem(9),replacement))return;record("actual-network-return-denied-item");phase=6;return;}
        if(phase==6){if(now<until+2)return;require(pending().isEmpty()&&same(stacks().getStackInSlot(0),original),"ActualMenuSlotOriginalRestoredBeforePostExpiryActions");phase=7;return;}
        if(phase==7){if(!same(player.containerMenu.getCarried(),original)||!stacks().getStackInSlot(0).isEmpty())return;record("actual-network-unequip-restored-original");phase=8;return;}
        if(phase==8){if(!player.containerMenu.getCarried().isEmpty()||!same(player.getInventory().getItem(10),original))return;phase=9;return;}
        if(phase==9){if(!same(player.containerMenu.getCarried(),replacement)||!player.getInventory().getItem(9).isEmpty())return;phase=10;return;}
        if(phase==10){if(!player.containerMenu.getCarried().isEmpty()||!same(stacks().getStackInSlot(0),replacement))return;require(same(player.getInventory().getItem(10),original)&&pending().isEmpty(),"ActualPostExpiryServerContainerClickEquipsReplacementAndPreservesOriginal");record("actual-network-post-expiry-equip-accepted");stableAt=now;phase=11;return;}
        require(pending().isEmpty()&&same(stacks().getStackInSlot(0),replacement)&&same(player.getInventory().getItem(10),original)&&player.containerMenu.getCarried().isEmpty(),"StableActualContainerActionFinalState");if(now-stableAt<40||!clientFinalState){require(now-stableAt<140,"BoundedActualNativeMenuFinalClientSync");return;}require(positivePosts==1&&deniedNativeEquipEvents>0&&clientMenuObserved&&clientFinalState,"RealMenuOpenNetworkDeniedAndAllowedActionsAccepted");finish(null);
    }catch(Throwable failure){finish(failure);}}
    private static int slotFor(net.minecraft.client.Minecraft client,int inventoryIndex){for(int index=0;index<client.player.containerMenu.slots.size();index++){var slot=client.player.containerMenu.slots.get(index);if(inventoryIndex>=0&&slot.container==client.player.getInventory()&&slot.getContainerSlot()==inventoryIndex)return index;if(inventoryIndex<0&&slot instanceof top.theillusivec4.curios.common.inventory.CurioSlot curio&&curio.getIdentifier().equals("boss_relic")&&!curio.isCosmetic()&&curio.getSlotContext().index()==0)return index;}throw new AssertionError("ActualNativeMenuSlotNotFound:"+inventoryIndex);}
    private static void action(Minecraft client,int inventoryIndex){int index=slotFor(client,inventoryIndex);client.gameMode.handleInventoryMouseClick(client.player.containerMenu.containerId,index,0,net.minecraft.world.inventory.ClickType.PICKUP,client.player);lastClientAction=phase;}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{var client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null){clientReady=true;if(phase==1&&!openRequested){openRequested=true;net.neoforged.neoforge.network.PacketDistributor.sendToServer(new top.theillusivec4.curios.common.network.client.CPacketOpenCurios(ItemStack.EMPTY));}if(client.player.containerMenu instanceof top.theillusivec4.curios.common.inventory.container.CuriosContainer&&client.screen!=null&&client.screen.getClass().getName().contains("Curios")){clientMenuObserved=true;var actual=CuriosApi.getCuriosInventory(client.player).orElseThrow().getCurios().get("boss_relic").getStacks().getStackInSlot(0);if(phase==1&&same(actual,original)&&same(client.player.getInventory().getItem(9),replacement))clientInitialFixture=true;if(phase==11&&same(actual,replacement)&&same(client.player.getInventory().getItem(10),original)&&client.player.containerMenu.getCarried().isEmpty())clientFinalState=true;if(lastClientAction!=phase){if(phase==3||phase==9)action(client,9);if(phase==4&&same(client.player.containerMenu.getCarried(),replacement))action(client,-1);if(phase==5&&same(client.player.containerMenu.getCarried(),replacement))action(client,9);if(phase==7&&same(actual,original))action(client,-1);if(phase==8&&same(client.player.containerMenu.getCarried(),original))action(client,10);if(phase==10&&same(client.player.containerMenu.getCarried(),replacement))action(client,-1);}}}
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);var settings=new LevelSettings("Original Boss native Curios menu actions",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-menu-actions-native",settings,new WorldOptions(14112030L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}if(++clientTicks>6000)throw new AssertionError("BoundedNativeMenuActionClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("actions",new ArrayList<>(actions));result.put("nativeMenuActionLifecycleAccepted",failure==null&&clientMenuObserved&&deniedNativeEquipEvents>0&&clientFinalState);result.put("nativeDeniedEquipEvents",deniedNativeEquipEvents);result.put("naturalPlayerPostCallbacks",playerTicks);result.put("clientMenuObserved",clientMenuObserved);result.put("clientFinalState",clientFinalState);result.put("osPointerInputAutomated",false);result.put("humanMouseInteractionClaimed",false);result.put("priorAcceptedControlsRerun",false);result.put("newJvmLoginAccepted",false);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("scope","Actual Curios screen opened withoriginal public client payload andservernative menu;public client gameMode containerclick generates original network handler,real cursor/item state andserver CurioCanEquipEvent denied duringactivepending;afterexpiry realunequip/place/pickup/equip accepted andactualclient synchronized. No direct eventpost orserver-slot mutation duringtestedactions,no OS input automation/privatehelper/manual tick/newJVM gate bypass/priorcontrol rerun orhuman pointer claim.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
