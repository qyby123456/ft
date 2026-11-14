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
public final class ZicaiCurioClientProbeV11 {
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> cases=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> posts=new CopyOnWriteArrayList<>();
    private static final List<String> names=List.of("wrong-variant","mini-owner","giant-owner","ownerless","causing-precedes-direct","direct-owner-fallback","projectile-causing-depth1","projectile-causing-depth8","projectile-causing-depth9","projectile-self-owner","ManorMercy-owner-fallback","causing-precedes-ManorMercy","creative-final-positive","spectator-exemption","zero-final-damage","cancelled-incoming","eligible-causing-precedes-projectile","projectile-ineligible-owner");
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean worldRequested,clientReady;
    private static int callbacks,clientTicks,playerTicks,scenario,phase,cancelledIncoming,admittedProjectiles;
    private static boolean cancelThisHit,expectedSilence;
    private static long startedAt;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static IDynamicStackHandler stacks;
    private static ItemStack fixture;
    public ZicaiCurioClientProbeV11(){NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV11::clientTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV11::serverTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV11::playerTick);NeoForge.EVENT_BUS.addListener(ZicaiCurioClientProbeV11::damage);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,ZicaiCurioClientProbeV11::incoming);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static boolean same(ItemStack actual){return fixture!=null&&ItemStack.isSameItemSameComponents(actual,fixture);}
    private static ListTag pending(){return player.getPersistentData().getCompound("PlayerPersisted").getList("ftboss:ZicaiCurioSilence",10);}
    private static int total(){int count=0;var inventory=CuriosApi.getCuriosInventory(player).orElseThrow();for(var handler:inventory.getCurios().values()){for(int index=0;index<handler.getSlots();index++){ItemStack actual=handler.getStacks().getStackInSlot(index);if(same(actual))count+=actual.getCount();}for(int index=0;index<handler.getCosmeticStacks().getSlots();index++){ItemStack actual=handler.getCosmeticStacks().getStackInSlot(index);if(same(actual))count+=actual.getCount();}}for(int index=0;index<player.getInventory().getContainerSize();index++){ItemStack actual=player.getInventory().getItem(index);if(same(actual))count+=actual.getCount();}for(int index=0;index<pending().size();index++){ItemStack actual=ItemStack.parseOptional(player.registryAccess(),pending().getCompound(index).getCompound("Stack"));if(same(actual))count+=actual.getCount();}return count;}
    private static void playerTick(PlayerTickEvent.Post event){if(event.getEntity()==player)playerTicks++;}
    private static void damage(LivingDamageEvent.Post event){if(event.getEntity()==player)posts.add(Map.of("scenario",scenario,"finalDamage",event.getNewDamage(),"gameTime",level.getGameTime()));}
    private static void incoming(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event){if(event.getEntity()==player&&cancelThisHit){event.setCanceled(true);cancelledIncoming++;}}
    private static void setup(){
        require(System.getProperty("ftboss.zicaicurio.mode").equals("candidate"),"ExplicitUnchangedStage28OwnerControlMode");require(!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLocalPlayer");player.setGameMode(GameType.SURVIVAL);level.setChunkForced(0,0,true);level.getChunk(0,0);level.setBlockAndUpdate(new BlockPos(8,69,8),Blocks.STONE.defaultBlockState());player.teleportTo(8,70,8);player.setNoGravity(true);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);player.setHealth(1000);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);
        stacks=CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().get("boss_relic").getStacks();require(stacks.getSlots()==1&&stacks.getStackInSlot(0).isEmpty()&&pending().isEmpty(),"FreshOriginalActualBossRelicSlot");fixture=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","frost_crown_charm")));fixture.set(DataComponents.CUSTOM_NAME,Component.literal("Original Boss owner-control relic fixture"));require(stacks.isItemValid(0,fixture),"OriginalSlotValidatorAcceptsOwnerControlRelic");stacks.setStackInSlot(0,fixture.copy());phase=1;
    }
    private static net.minecraft.world.entity.projectile.Projectile projectile(net.minecraft.world.entity.Entity owner,List<net.minecraft.world.entity.Entity> admitted){var actual=net.minecraft.world.entity.EntityType.SNOWBALL.create(level);require(actual!=null,"ActualRegisteredSnowballFactory");actual.setOwner(owner);actual.setNoGravity(true);actual.moveTo(8,210+admitted.size()*2,8,0,0);require(level.addFreshEntity(actual)&&level.getEntity(actual.getUUID())==actual,"ActualNativeProjectileAdmission");require(actual.getOwner()==owner,"ActualPublicProjectileOwnerIdentity");admitted.add(actual);admittedProjectiles++;return actual;}
    private static void startCase(){
        String label=names.get(scenario);require(total()==1&&pending().isEmpty()&&same(stacks.getStackInSlot(0)),"OriginalSingleRelicReady:"+label);player.setGameMode(scenario==12?GameType.CREATIVE:scenario==13?GameType.SPECTATOR:GameType.SURVIVAL);player.setNoGravity(true);require(net.farmingtales.ftboss.boss.ManorMercy.damageOwner()==null,"NoPreexistingThreadLocalDamageOwner:"+label);
        List<net.minecraft.world.entity.Entity> admitted=new ArrayList<>();EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory:"+label);boss.setVariant(scenario==0||scenario==17?7:8);boss.setMini(scenario==1);boss.setGiant(scenario==2);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8,245,8,0,0);require(level.addFreshEntity(boss)&&level.getEntity(boss.getUUID())==boss,"ActualSourceBossAdmission:"+label);admitted.add(boss);
        net.minecraft.world.entity.Entity direct=boss,causing=boss;
        if(scenario==3||scenario==10){direct=null;causing=null;}
        if(scenario==4||scenario==11){var cow=net.minecraft.world.entity.EntityType.COW.create(level);require(cow!=null,"ActualRegisteredOtherOwnerFactory");cow.setNoAi(true);cow.setNoGravity(true);cow.moveTo(8,235,8,0,0);require(level.addFreshEntity(cow),"ActualOtherOwnerAdmission");admitted.add(cow);causing=cow;direct=scenario==4?boss:null;}
        if(scenario==5){direct=boss;causing=null;}
        if(scenario==6||scenario==7||scenario==8||scenario==17){int depth=scenario==7?8:scenario==8?9:1;net.minecraft.world.entity.Entity owner=boss;for(int index=0;index<depth;index++)owner=projectile(owner,admitted);direct=owner;causing=owner;}
        if(scenario==9){var actual=projectile(boss,admitted);actual.setOwner(actual);require(actual.getOwner()==actual,"ActualSelfOwnerNegativeControl");direct=actual;causing=actual;}
        if(scenario==16){direct=projectile(boss,admitted);causing=boss;}
        var type=(scenario==12||scenario==13?player.damageSources().fellOutOfWorld():player.damageSources().mobAttack(boss)).typeHolder();var source=new net.minecraft.world.damagesource.DamageSource(type,direct,causing);require(source.getEntity()==causing&&source.getDirectEntity()==direct,"ActualDamageSourceOwnerPrecedenceFields:"+label);
        expectedSilence=scenario==5||scenario==6||scenario==7||scenario==10||scenario==16;float amount=scenario==14?0.0F:3.0F;float health=player.getHealth();int beforePosts=posts.size(),beforeCancelled=cancelledIncoming;startedAt=level.getGameTime();boolean accepted;
        cancelThisHit=scenario==15;try{accepted=scenario==10||scenario==11?net.farmingtales.ftboss.boss.ManorMercy.withOwner(boss,()->player.hurt(source,amount)):player.hurt(source,amount);}finally{cancelThisHit=false;}
        require(net.farmingtales.ftboss.boss.ManorMercy.damageOwner()==null,"PublicManorMercyScopeRestoresThreadLocal:"+label);int postCount=posts.size()-beforePosts;boolean positive=postCount>0&&((Number)posts.getLast().get("finalDamage")).floatValue()>0;
        if(scenario!=13&&scenario!=14&&scenario!=15)require(accepted&&positive&&player.getHealth()<health,"ActualFinalPositiveNativePipeline:"+label);
        if(scenario==14)require(!positive,"ZeroDamageNeverCreatesFinalPositivePost");
        if(scenario==15)require(!accepted&&postCount==0&&cancelledIncoming==beforeCancelled+1,"NativeIncomingCancellationPreventsPost");
        if(scenario==12)require(player.isCreative()&&positive,"CreativeExemptionDirectlyTestedAfterFinalPositiveDamage");
        if(scenario==13)require(player.isSpectator(),"ActualSpectatorEligibilityControl");
        if(expectedSilence){require(pending().size()==1&&pending().getCompound(0).getLong("Until")==startedAt+100&&stacks.getStackInSlot(0).isEmpty(),"OriginalSilenceForResolvedEligibleOwner:"+label);require(same(ItemStack.parseOptional(player.registryAccess(),pending().getCompound(0).getCompound("Stack"))),"OriginalComponentPreservingPending:"+label);}
        else require(pending().isEmpty()&&same(stacks.getStackInSlot(0)),"NoSilenceForOriginalOwnerOrEligibilityExemption:"+label);
        require(total()==1,"ExactlyOneRelicAfterOwnerControl:"+label);cases.add(Map.of("scenario",scenario,"name",label,"nativeDamageAccepted",accepted,"finalPositivePost",positive,"nativePostCount",postCount,"healthBefore",health,"healthAfter",player.getHealth(),"expectedSilence",expectedSilence,"pendingRows",pending().size(),"gameTime",startedAt));for(var entity:admitted)entity.discard();phase=2;
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{
        require(++callbacks<2400,"BoundedNaturalOwnerControlPosts");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
        if(phase==0){player=event.getServer().getPlayerList().getPlayers().getFirst();level=player.serverLevel();if(!clientReady||playerTicks<100)return;setup();return;}
        if(phase==1){startCase();return;}
        require(total()==1,"OriginalRelicConservedAcrossNaturalOwnerControlTicks");long elapsed=level.getGameTime()-startedAt;if(elapsed<(expectedSilence?102:22))return;require(pending().isEmpty()&&same(stacks.getStackInSlot(0)),"ActualNaturalReadyStateBeforeNextIndependentControl:"+names.get(scenario));scenario++;if(scenario==names.size()){require(cases.size()==18,"ExactlyEighteenDistinctOwnerProjectileEligibilityControls");finish(null);}else phase=1;
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Original Boss owner projectile eligibility controls",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("zicai-curio-owner-controls-native",settings,new WorldOptions(14112027L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        if(++clientTicks>6000)throw new AssertionError("BoundedNativeOwnerControlClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("cases",new ArrayList<>(cases));result.put("nativeDamagePosts",new ArrayList<>(posts));result.put("actualProjectilesAdmitted",admittedProjectiles);result.put("actualIncomingEventsCancelled",cancelledIncoming);result.put("naturalPlayerPostCallbacks",playerTicks);result.put("curiosOwnerProjectileEligibilityControlsAccepted",failure==null&&scenario==18);result.put("authenticated",false);result.put("fullBossMigrationComplete",false);result.put("fullCuriosLifecycleMatrixAccepted",false);result.put("nativeProjectileCollisionOrFlightAcceptanceClaimed",false);result.put("scope","Unchanged Stage28,new public fresh native world,actual local vanilla player,registered admitted Boss/Cow/Snowball sources andpublic native positive damage;18 original owner/variant/mini/giant,source precedence,projectile depth1/8/9/self owner,public ManorMercy scope,creative final-positive,spectator,zero/cancelled damage controls. Actual source objects aredirected public fixtures;no private gameplay method,manual entity/effect tick,synthetic ServerPlayer,natural projectile collision,authenticated multiplayer orfull lifecycle acceptance.");Files.writeString(Path.of("actual-zicai-curio-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
