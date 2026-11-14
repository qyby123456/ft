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
public final class ZicaiCurioClientProbeV18 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientSawPersistedRelic;
    private static int callbacks,clientTicks,playerTicks;
    private static ServerPlayer player;
    private static ItemStack expected;
    private static long firstGameTime,lastGameTime;
    private static final List<Map<String,Object>> screenObservations=new CopyOnWriteArrayList<>();
    private static String lastScreen="";
    private static int ticksAfterOpen;
    private static long confirmationStartedNanos;
    private static boolean experimentalScreenObserved,confirmationFlowContinued,liveStatusWritten;
    public ZicaiCurioClientProbeV18(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV18::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV18::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV18::playerTick);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return expected!=null&&actual.getCount()==1&&ItemStack.isSameItemSameComponents(actual,expected);}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{
        require(++callbacks<2000,"BoundedNaturalReloadServerPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
        if(player==null){player=event.getServer().getPlayerList().getPlayers().getFirst();require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaNewJvmIntegratedLogin");require(experimentalScreenObserved&&confirmationFlowContinued,"ActualExperimentalConfirmationScreenThenNativeFlowAndNewJvmLogin");expected=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));expected.set(DataComponents.CUSTOM_NAME,Component.literal("Boss lifecycle valid relic fixture"));firstGameTime=player.serverLevel().getGameTime();}
        if(playerTicks<5)return;
        var inventory=CuriosApi.getCuriosInventory(player).orElseThrow();var handler=inventory.getCurios().get("boss_relic");require(handler!=null&&handler.getStacks().getSlots()>0,"OriginalBossRelicSlotReloaded");require(same(handler.getStacks().getStackInSlot(0)),"ExactOriginalItemCountAndComponentsRestoredByNativeSaveLoad");
        ListTag pending=player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);require(pending.isEmpty(),"NoStaleSilenceRecordAfterActualLogoutAndNewJvmLogin");
        int total=0;for(var entry:inventory.getCurios().values()){for(int index=0;index<entry.getStacks().getSlots();index++){ItemStack actual=entry.getStacks().getStackInSlot(index);if(ItemStack.isSameItemSameComponents(actual,expected))total+=actual.getCount();}for(int index=0;index<entry.getCosmeticStacks().getSlots();index++){ItemStack actual=entry.getCosmeticStacks().getStackInSlot(index);if(ItemStack.isSameItemSameComponents(actual,expected))total+=actual.getCount();}}for(int index=0;index<player.getInventory().getContainerSize();index++){ItemStack actual=player.getInventory().getItem(index);if(ItemStack.isSameItemSameComponents(actual,expected))total+=actual.getCount();}
        require(total==1,"ExactlyOnePersistedRelicAcrossActualCosmeticNoncosmeticAndPlayerInventory");lastGameTime=player.serverLevel().getGameTime();if(playerTicks>=120&&clientSawPersistedRelic){require(lastGameTime-firstGameTime>=120,"120NaturalTicksAcrossPersistedExpiryWindow");finish(null);}
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;client.createWorldOpenFlows().openWorld("zicai-curio-valid-lifecycle-native",()->{});}
        String screenName=client.screen==null?"none":client.screen.getClass().getName();
        if(screenName.equals("net.minecraft.client.gui.screens.BackupConfirmScreen")){experimentalScreenObserved=true;if(confirmationStartedNanos==0)confirmationStartedNanos=System.nanoTime();if(!liveStatusWritten){liveStatusWritten=true;Map<String,Object> status=new LinkedHashMap<>();status.put("screenClass",screenName);status.put("title",client.screen.getTitle().getString());status.put("requiresActualUserClick",true);status.put("button","Create Backup and Load");status.put("waitSeconds",180);status.put("deadlineUtc",java.time.Instant.now().plusSeconds(180).toString());status.put("formalInstanceWorld",false);status.put("automaticConfirmationInvoked",false);Files.writeString(Path.of("attended-reload-status.json"),new GsonBuilder().setPrettyPrinting().create().toJson(status));System.out.println("[FTBoss attended reload] Experimental confirmation is now visible. User must click Create Backup and Load within180seconds;no automatic acceptance.");}if(System.nanoTime()-confirmationStartedNanos>=180_000_000_000L){finish(new AssertionError("ActualUserExperimentalConfirmationNotReceivedWithin180Seconds"));return;}}
        else if(experimentalScreenObserved&&!(client.screen instanceof TitleScreen))confirmationFlowContinued=true;

        if(!screenName.equals(lastScreen)){lastScreen=screenName;List<String> messages=new ArrayList<>();if(client.screen!=null)for(var child:client.screen.children())if(child instanceof net.minecraft.client.gui.components.AbstractWidget widget)messages.add(widget.getMessage().getString());screenObservations.add(Map.of("clientTick",clientTicks,"screenClass",screenName,"title",client.screen==null?"":client.screen.getTitle().getString(),"widgetMessages",messages,"overlayClass",client.getOverlay()==null?"none":client.getOverlay().getClass().getName(),"actualIntegratedServerPresent",client.getSingleplayerServer()!=null));}
        if(worldRequested&&client.getOverlay()==null&&client.level==null&&client.getSingleplayerServer()==null&&!screenName.equals("net.minecraft.client.gui.screens.BackupConfirmScreen")&&++ticksAfterOpen>=6000){finish(new AssertionError("BoundedPublicWorldOpenFlowAfterAttendedConfirmation:"+screenName));return;}
        if(++clientTicks>24000)throw new AssertionError("BoundedNativeReloadClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
        if(client.player!=null&&expected!=null){var inventory=CuriosApi.getCuriosInventory(client.player);if(inventory.isPresent()){var handler=inventory.get().getCurios().get("boss_relic");if(handler!=null&&same(handler.getStacks().getStackInSlot(0)))clientSawPersistedRelic=true;}}
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("naturalPlayerPostCallbacks",playerTicks);result.put("clientSawPersistedRelic",clientSawPersistedRelic);result.put("sourceWorldWasAcceptedNativeV6Only",true);result.put("newJvmLogoutLoginPersistenceAccepted",failure==null&&playerTicks>=120&&clientSawPersistedRelic);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("originalZicaiSilenceAccepted",false);result.put("screenObservations",new ArrayList<>(screenObservations));result.put("publicScreenStateReadOnly",true);result.put("uiButtonsOrWarningsAutomaticallyAccepted",false);result.put("attendedConfirmationWaitSeconds",180);result.put("experimentalScreenObserved",experimentalScreenObserved);result.put("confirmationFlowContinued",confirmationFlowContinued);result.put("actualUserInputSourceInferredNotInstrumented",true);result.put("priorAcceptedControlsRerun",false);result.put("scope","New JVM opens byte-identical copy of accepted V6 normal-saved world through public WorldOpenFlows.openWorld;native real local player login,exact one valid original Boss relic with original components,no stale pending120natural ticks andactual client inventory sync. No failed world reuse,manual data repair,silence fixture injection,automatic experimental confirmation,world flag changes,authenticated multiplayer,prior accepted controls rerun orfull lifecycle matrix acceptance. Confirmation-page state is read-only andreal native continuation isobserved;input source itself isnotinstrumented.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
