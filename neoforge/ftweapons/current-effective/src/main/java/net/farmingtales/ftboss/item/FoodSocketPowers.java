package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.util.FoodSocketPowerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class FoodSocketPowers {
    private static final String ROOT = "FTBossFoodSocketPowers";
    private static final String HELPER = "FTBossFoodSocketHelper";
    private static final String WEAPON_TOKEN = "FTBossFoodSocketWeaponToken";
    private static final double MAX_HELPER_DISTANCE_SQUARED = 1024.0;
    private static final double MAX_TARGET_DISTANCE_SQUARED = 576.0;
    private static final int HELPER_BREED_LOCK_TICKS = 6000;

    private FoodSocketPowers() {
    }

    private static int count(ItemStack var0, FoodType var1) {
        return FoodWeaponEvolution.socketTypeCount(var0, var1);
    }

    private static boolean flight(ItemStack var0) {
        int var1 = count(var0, FoodType.FISH);
        return var1 < 2 ? false : FoodSocketPowerRules.flight(var1, count(var0, FoodType.WINE));
    }

    private static boolean pack(ItemStack var0) {
        return FoodSocketPowerRules.pack(count(var0, FoodType.MEAT), count(var0, FoodType.CHEESE));
    }

    private static long now(ServerPlayer var0) {
        return var0.serverLevel().getServer().overworld().getGameTime();
    }

    public static int setBonus(int[] var0, FoodType var1) {
        if (var0.length != FoodType.values().length) {
            return 0;
        } else {
            FoodType var2 = switch (var1) {
                case TEA -> FoodType.APPLE;
                case APPLE -> FoodType.TEA;
                case MEAT -> FoodType.CHEESE;
                case CHEESE -> FoodType.MEAT;
                case FISH -> FoodType.WINE;
                case WINE -> FoodType.FISH;
                case VEGETABLE -> FoodType.ENERGY;
                case ENERGY -> FoodType.VEGETABLE;
                case SWEET, FOOD -> FoodType.APPLE;
            };
            int var3 = var0[var1.ordinal()];
            if (var1 == FoodType.SWEET || var1 == FoodType.FOOD) {
                var3 = var0[FoodType.SWEET.ordinal()] + var0[FoodType.FOOD.ordinal()];
            }

            return FoodSocketPowerRules.setBonus(var3, var0[var2.ordinal()]);
        }
    }

    @SubscribeEvent
    public static void flightTick(Post var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            CompoundTag var13 = var1.getPersistentData().getCompound("FTBossFoodSocketPowers");
            ItemStack var3 = var1.getMainHandItem();
            long var4 = now(var1);
            boolean var6 = var1.isAlive()
                && !var1.isSpectator()
                && !var1.isPassenger()
                && !var1.isFallFlying()
                && !var1.isInWaterOrBubble()
                && !var1.onClimbable()
                && !ManorFlyBan.active(var1)
                && FoodWeaponEvolution.isEvolutionCandidate(var3)
                && flight(var3);
            boolean var7 = FoodSocketPowerRules.active(var4, var13.getLong("FlightUntil"), 60);
            if (var7
                && (
                    !var6
                        || !var13.hasUUID("FlightWeapon")
                        || NativeFoundationItemData.customData(var3) == null
                        || !NativeFoundationItemData.customData(var3).hasUUID("FTBossFoodSocketWeaponToken")
                        || !var13.getUUID("FlightWeapon").equals(NativeFoundationItemData.customData(var3).getUUID("FTBossFoodSocketWeaponToken"))
                )) {
                var13.remove("FlightUntil");
                var13.remove("FlightWeapon");
                var7 = false;
            }

            boolean var8 = var1.isShiftKeyDown();
            boolean var9 = var8 && !var13.getBoolean("WasSneaking");
            var13.putBoolean("WasSneaking", var8);
            if (!var7 && var6 && !var1.onGround() && var9 && FoodSocketPowerRules.ready(var4, var13.getLong("FlightCooldown"), 600)) {
                var13.putUUID("FlightWeapon", NativeSocketWeaponTokenData.ensure(var3));
                var13.putLong("FlightUntil", var4 + 60L);
                var13.putLong("FlightCooldown", var4 + 600L);
                var7 = true;
                var1.displayClientMessage(Component.translatable("message.ftboss.socket.flight"), true);
            }

            if (var7) {
                Vec3 var10 = var1.getDeltaMovement();
                double var11 = var8 ? Math.min(0.2, Math.max(0.12, var10.y)) : Math.max(-0.08, var10.y);
                var1.setDeltaMovement(var10.x, var11, var10.z);
                var1.fallDistance = 0.0F;
                var1.hurtMarked = true;
            }

            if (!var13.isEmpty()) {
                var1.getPersistentData().put("FTBossFoodSocketPowers", var13);
            }
        }
    }

    private static boolean validTarget(ServerPlayer var0, LivingEntity var1) {
        return var1 instanceof Monster
            && var1.isAlive()
            && !var1.isRemoved()
            && !var1.isAlliedTo(var0)
            && !var0.isAlliedTo(var1)
            && !LemonSummonRewards.suppressed(var1)
            && (!(var1 instanceof OwnableEntity var2) || var2.getOwnerUUID() == null)
            && var1 != var0
            && !(var1 instanceof TamableAnimal)
            && var1.level() == var0.level()
            && var1.distanceToSqr(var0) <= 576.0;
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void summon(BeforeMitigation var0) {
        if (FoodWeaponFinalDamage.nativeOriginalInteractionActive(var0.getSource(), var0.getEntity())) {
            captureNativePack202(var0);
            return;
        }
        if (!(var0.getAmount() <= 0.0F)
            && var0.getSource().getEntity() instanceof ServerPlayer var1
            && var0.getSource().getDirectEntity() == var1
            && var1.isAlive()
            && !var1.isSpectator()
            && validTarget(var1, var0.getEntity())
            && pack(var1.getMainHandItem())) {
            long var13 = now(var1);
            CompoundTag var4 = var1.getPersistentData().getCompound("FTBossFoodSocketPowers");
            if (FoodSocketPowerRules.ready(var13, var4.getLong("PackCooldown"), 400)) {
                if (!FoodSocketPowerRules.active(var13, var4.getLong("PackUntil"), 600)) {
                    ItemStack var5 = var1.getMainHandItem();
                    UUID nativeWeaponToken = NativeSocketWeaponTokenData.ensure(var5);
                    UUID var6 = UUID.randomUUID();
                    var4.putUUID("PackToken", var6);
                    var4.putUUID("PackWeapon", nativeWeaponToken);
                    var4.putLong("PackUntil", var13 + 600L);
                    var1.getPersistentData().put("FTBossFoodSocketPowers", var4);
                    int var7 = 0;

                    for (int var8 = 0; var8 < 2; var8++) {
                        Wolf var9 = EntityType.WOLF.create(var1.serverLevel());
                        if (var9 != null) {
                            double var10 = var8 == 0 ? -1.0 : 1.0;
                            var9.moveTo(var1.getX() + var10, var1.getY(), var1.getZ(), var1.getYRot(), 0.0F);
                            if (var1.serverLevel().noCollision(var9) && var1.serverLevel().getFluidState(var9.blockPosition()).isEmpty()) {
                                var9.setTame(true, true);
                                var9.setOwnerUUID(var1.getUUID());
                                var9.setAge(6000);
                                var9.resetLove();
                                var9.setOrderedToSit(false);
                                var9.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0);
                                var9.getAttribute(Attributes.MAX_HEALTH).setBaseValue(12.0);
                                var9.setHealth(12.0F);
                                CompoundTag var12 = new CompoundTag();
                                var12.putUUID("Owner", var1.getUUID());
                                var12.putUUID("Token", var6);
                                var12.putLong("Expires", var13 + 600L);
                                var9.getPersistentData().put("FTBossFoodSocketHelper", var12);
                                LemonSummonRewards.mark(var9);
                                var9.setTarget(var0.getEntity());
                                if (var1.serverLevel().addFreshEntity(var9)) {
                                    var7++;
                                } else {
                                    var9.discard();
                                }
                            } else {
                                var9.discard();
                            }
                        }
                    }

                    if (var7 > 0) {
                        var4.putLong("PackCooldown", var13 + 400L);
                        var1.displayClientMessage(Component.translatable("message.ftboss.socket.pack", var7), true);
                    } else {
                        var4.remove("PackUntil");
                        var4.remove("PackToken");
                        var4.remove("PackWeapon");
                    }

                    var1.getPersistentData().put("FTBossFoodSocketPowers", var4);
                }
            }
        }
    }

    private static ServerPlayer owner(Wolf var0) {
        CompoundTag var1 = var0.getPersistentData().getCompound("FTBossFoodSocketHelper");
        if (var1.hasUUID("Owner") && var0.level() instanceof ServerLevel var2) {
            if (var2.getServer().getPlayerList().getPlayer(var1.getUUID("Owner")) instanceof ServerPlayer var4 && var4.level() == var2) {
                return var4;
            }

            return null;
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public static void helperJoin(EntityJoinLevelEvent var0) {
        if (!var0.getLevel().isClientSide && var0.getEntity() instanceof Wolf var1 && var1.getPersistentData().contains("FTBossFoodSocketHelper")) {
            if (var0.loadedFromDisk()) {
                var1.discard();
                var0.setCanceled(true);
            }
        }
    }

    public static void helperTick(LivingEntity var0) {
        if (var0 instanceof Wolf var1 && !var1.level().isClientSide && var1.getPersistentData().contains("FTBossFoodSocketHelper")) {
            ServerPlayer var5 = owner(var1);
            CompoundTag var3 = var1.getPersistentData().getCompound("FTBossFoodSocketHelper");
            if (var5 != null && var5.isAlive() && !var5.isSpectator() && var3.hasUUID("Token")) {
                CompoundTag var4 = var5.getPersistentData().getCompound("FTBossFoodSocketPowers");
                if (var4.hasUUID("PackToken")
                    && var3.getUUID("Token").equals(var4.getUUID("PackToken"))
                    && FoodSocketPowerRules.active(now(var5), var3.getLong("Expires"), 600)
                    && !(var1.distanceToSqr(var5) > 1024.0)
                    && pack(var5.getMainHandItem())
                    && var4.hasUUID("PackWeapon")
                    && NativeFoundationItemData.customData(var5.getMainHandItem()) != null
                    && NativeFoundationItemData.customData(var5.getMainHandItem()).hasUUID("FTBossFoodSocketWeaponToken")
                    && var4.getUUID("PackWeapon").equals(NativeFoundationItemData.customData(var5.getMainHandItem()).getUUID("FTBossFoodSocketWeaponToken"))) {
                    var1.setAge(6000);
                    var1.resetLove();
                    var1.setOrderedToSit(false);
                    if (var1.getTarget() != null && !validTarget(var5, var1.getTarget())) {
                        var1.setTarget(null);
                    }

                    return;
                }

                var1.discard();
                return;
            }

            var1.discard();
            return;
        }
    }

    private static boolean temporary(Entity var0) {
        return var0 instanceof Wolf && var0.getPersistentData().contains("FTBossFoodSocketHelper");
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void helperInteract(EntityInteract var0) {
        if (temporary(var0.getTarget())) {
            var0.setCanceled(true);
            var0.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void helperInteractSpecific(EntityInteractSpecific var0) {
        if (temporary(var0.getTarget())) {
            var0.setCanceled(true);
            var0.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void helperBreed(BabyEntitySpawnEvent var0) {
        if (temporary(var0.getParentA()) || temporary(var0.getParentB())) {
            var0.setCanceled(true);
            if (var0.getChild() != null) {
                var0.getChild().discard();
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void target(LivingChangeTargetEvent var0) {
        if (var0.getEntity() instanceof Wolf var1 && var1.getPersistentData().contains("FTBossFoodSocketHelper")) {
            ServerPlayer var3 = owner(var1);
            if (var3 == null || var0.getNewAboutToBeSetTarget() != null && !validTarget(var3, var0.getNewAboutToBeSetTarget())) {
                var0.setNewAboutToBeSetTarget(null);
            }

            return;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void safeDamage(BeforeMitigation var0) {
        if (var0.getSource().getEntity() instanceof Wolf var1 && var1.getPersistentData().contains("FTBossFoodSocketHelper")) {
            ServerPlayer var3 = owner(var1);
            if (var3 == null || !validTarget(var3, var0.getEntity())) {
                var0.setCanceled(true);
            }

            return;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void drops(LivingDropsEvent var0) {
        if (var0.getEntity().getPersistentData().contains("FTBossFoodSocketHelper")) {
            var0.getDrops().clear();
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void experience(LivingExperienceDropEvent var0) {
        if (var0.getEntity().getPersistentData().contains("FTBossFoodSocketHelper")) {
            var0.setDroppedExperience(0);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(Clone var0) {
        CompoundTag var1 = var0.getOriginal().getPersistentData().getCompound("FTBossFoodSocketPowers");
        CompoundTag var2 = new CompoundTag();
        if (var1.contains("FlightCooldown", 4)) {
            var2.putLong("FlightCooldown", var1.getLong("FlightCooldown"));
        }

        if (var1.contains("PackCooldown", 4)) {
            var2.putLong("PackCooldown", var1.getLong("PackCooldown"));
        }

        var0.getEntity().getPersistentData().put("FTBossFoodSocketPowers", var2);
    }

    @SubscribeEvent
    public static void logout(PlayerLoggedOutEvent var0) {
        endAbilities(var0.getEntity());
    }

    @SubscribeEvent
    public static void dimension(PlayerChangedDimensionEvent var0) {
        endAbilities(var0.getEntity());
    }

    private static void endAbilities(Player var0) {
        CompoundTag var1 = var0.getPersistentData().getCompound("FTBossFoodSocketPowers");

        for (String var3 : List.of("FlightUntil", "FlightWeapon", "PackUntil", "PackToken", "PackWeapon")) {
            var1.remove(var3);
        }

        var0.getPersistentData().put("FTBossFoodSocketPowers", var1);
    }

    public static void applyPack(FoodSocketPowers.PackCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null
            && !var0.executed
            && Thread.currentThread() == var0.thread
            && var0.server == var0.plan.context.player().serverLevel().getServer()
            && var0.server.isSameThread()) {
            FoodWeaponConsumerSequence.Accepted var2 = var0.prepared.originalSequence;
            if (var2 == null || var2.enter(var0, var1)) {
                Throwable var3 = null;

                try {
                    var0.executed = true;
                    if (!publishedPack(var0)) {
                        return;
                    }

                    CompoundTag var4 = var0.publishedController;
                    if ((var2 == null ? !FoodWeaponBondHits.matchesContext(var0.plan.context) : !var2.ordinaryContext())
                        || !matchesPackReceipt(var0.prepared, var0.amount, var0.receipt)
                        || !var4.hasUUID("PackToken")
                        || !var4.getUUID("PackToken").equals(var0.token)) {
                        restorePack(var0, var1);
                        return;
                    }

                    if (!packLeafLive(var0, var1)) {
                        return;
                    }

                    int var5 = spawnPack(var0, var1);
                    if (!packLeafLive(var0, var1)) {
                        return;
                    }

                    if (var5 <= 0) {
                        restorePack(var0, var1);
                        return;
                    }

                    var0.plan.context.player().displayClientMessage(Component.translatable("message.ftboss.socket.pack", var5), true);
                    if (packLeafLive(var0, var1)) {
                        return;
                    }
                } catch (Error | RuntimeException var9) {
                    var3 = var9;
                    throw var9;
                } finally {
                    if (var2 != null) {
                        var2.finish(var0, var1, var3);
                    }
                }
            }
        }
    }

    static boolean bindOriginalSequence(
        FoodSocketPowers.PreparedPack var0,
        FoodWeaponOriginalSequence.Session var1,
        Object var2,
        int var3,
        FoodWeaponFinalDamage.OffenseReceipt var4,
        float var5,
        FoodWeaponFinalDamage.PublishingCleanup var6
    ) {
        if (var0 != null
            && !var0.publishAttempted
            && var0.originalSequence == null
            && FoodWeaponFinalDamage.matchesPackPublishing(var0, var4, var6)
            && matchesPackReceipt(var0, var5, var4)) {
            FoodSocketPowers.CompleteBatchBinding var7 = var0.binding;
            FoodWeaponConsumerSequence.Accepted var8 = FoodWeaponConsumerSequence.bind(
                var1,
                var2,
                var3,
                var0,
                var0.plan,
                var4,
                var6,
                var7.batch,
                var7.admission,
                var7.requests,
                var7.targets,
                var0.plan.context.player(),
                var0.plan.context.target(),
                var0.plan.context.source(),
                var0.plan.context.actualWeapon(),
                var0.plan.context.originalHand(),
                var0.plan.context.quotedAt(),
                var5,
                "WolfProducer"
            );
            if (var8 == null) {
                return false;
            } else {
                var0.originalSequence = var8;
                return true;
            }
        } else {
            return false;
        }
    }

    static FoodSocketPowers.PackCommit commitPack(
        FoodSocketPowers.PreparedPack var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2, FoodWeaponFinalDamage.PublishingCleanup var3
    ) {
        FoodWeaponConsumerSequence.Accepted var4 = var0 == null ? null : var0.originalSequence;
        int var5 = var4 == null ? 0 : var4.publicationCallEntered(var0);
        Throwable var6 = null;

        FoodSocketPowers.PackCommit var7;
        try {
            if (var0 == null || var0.publishAttempted || var0.publishingCleanup != null || !FoodWeaponFinalDamage.matchesPackPublishing(var0, var2, var3)) {
                return null;
            }

            var0.publishingCleanup = var3;
            var7 = commitPack(var0, var1, var2);
        } catch (Throwable var11) {
            var6 = var11;
            throw var11;
        } finally {
            if (var4 != null) {
                var4.publicationExited(var0, var5, var6);
            }
        }

        return var7;
    }

    static void finishPackPublication(FoodSocketPowers.PreparedPack var0, FoodWeaponFinalDamage.PublishingCleanup var1) {
        FoodWeaponConsumerSequence.Accepted var2 = var0 == null ? null : var0.originalSequence;
        Throwable var3 = null;

        try {
            if (!preparedCleanupCaller()
                || var0 == null
                || var0.publishingCleanup != var1
                || (var0.originalSequence == null || !var0.originalSequence.cleanup(var0, var1))
                    && !FoodWeaponFinalDamage.matchesPackPublicationCleanup(var0, var1)
                || !var0.publishAttempted
                || !var0.publishExited
                || var0.publicationReceipt == null) {
                return;
            }

            FoodSocketPowers.CompleteBatchBinding var4 = var0.binding;
            if (!matchesPreparedPack(var0, var0.plan, var4.batch, var4.admission, var4.requests, var4.targets)) {
                return;
            }

            var0.finalizingCleanup = var1;
            Throwable var5 = null;

            try {
                retirePackPublication(var0, FoodWeaponSourceLedger.originalInteractionBinding(var4.batch));
            } catch (Error | RuntimeException var34) {
                var5 = var34;
                throw var34;
            } finally {
                try {
                    finishPackOwnerController(var0, var1, var5);
                } finally {
                    if (var0.originalSequence != null) {
                        var0.originalSequence.retired(var0);
                    }
                }
            }
        } catch (Throwable var37) {
            var3 = var37;
            throw var37;
        } finally {
            if (var2 != null) {
                var2.publicationCleanupExited(var0, var1, var3);
            }
        }
    }

    static boolean matchesOwnerPackParameters(
        FoodSocketPowers.PreparedPack var0,
        FoodWeaponFinalDamage.PublishingCleanup var1,
        FoodWeaponFinalDamage.OffenseReceipt var2,
        CompoundTag var3,
        CompoundTag var4,
        UUID var5,
        UUID var6,
        CompoundTag var7,
        CompoundTag var8,
        long var9
    ) {
        if (var0 != null
            && var0.ownerParameters != null
            && var0.publishingCleanup == var1
            && var0.publishAttempted
            && !var0.publishExited
            && var0.publication == null
            && var0.publicationReceipt == var2
            && var0.ownerControllerBeginEntered
            && !var0.ownerControllerBeginCompleted
            && Thread.holdsLock(FoodWeaponSourceLedger.class)
            && FoodWeaponFinalDamage.matchesPackPublishing(var0, var2, var1)) {
            FoodSocketPowers.PackOwnerParameters var11 = var0.ownerParameters;
            return var3 != null
                && var3 == var11.persistent
                && var4 != null
                && var4 == var11.data
                && var5 != null
                && var5 == var11.weaponToken
                && var6 != null
                && var6 == var11.packToken
                && var7 != null
                && var7 == var11.fingerprint
                && var7 == FoodWeaponBondHits.originalContextFingerprint(var0.plan.context)
                && var8 != null
                && var8.equals(var11.snapshot)
                && var9 == var11.tick
                && var9 == var0.plan.quotedAt()
                && FoodWeaponFinalDamage.matchesPackPublishing(var0, var2, var1);
        } else {
            return false;
        }
    }

    static boolean matchesPackCommit(
        FoodSocketPowers.PackCommit var0,
        FoodSocketPowers.PreparedPack var1,
        FoodSocketPowers.PackPlan var2,
        FoodWeaponFinalDamage.OffenseReceipt var3,
        float var4,
        FoodWeaponSourceLedger.AttemptBatchQuote var5,
        FoodWeaponSourceLedger.AttemptBatchAdmission var6,
        List<FoodWeaponSourceLedger.AttemptRequest> var7,
        List<FoodWeaponSourceLedger.TargetRequest> var8
    ) {
        return var0 != null
            && var1 != null
            && var0.prepared == var1
            && var0.plan == var2
            && var2 != null
            && var2.committed
            && var3 != null
            && var0.receipt == var3
            && Float.isFinite(var4)
            && var4 > 0.0F
            && Float.floatToRawIntBits(var0.amount) == Float.floatToRawIntBits(var4)
            && matchesPreparedPack(var1, var2, var5, var6, var7, var8);
    }

    static boolean matchesPreparedPack(
        FoodSocketPowers.PreparedPack var0,
        FoodSocketPowers.PackPlan var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        List<FoodWeaponSourceLedger.AttemptRequest> var4,
        List<FoodWeaponSourceLedger.TargetRequest> var5
    ) {
        return var0 != null
            && var1 != null
            && var0.plan == var1
            && var2 != null
            && var3 != null
            && var4 != null
            && var5 != null
            && var0.binding.batch == var2
            && var0.binding.admission == var3
            && var0.binding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(var2)
            && var0.binding.originalBinding != null
            && var0.binding.requests.equals(var4)
            && var0.binding.targets.equals(var5);
    }

    private static FoodSocketPowers.PackPlayerCall[] newPackPlayerCalls() {
        FoodSocketPowers.PackPlayerCall[] var0 = new FoodSocketPowers.PackPlayerCall[10];

        for (int var1 = 0; var1 < var0.length; var1++) {
            var0[var1] = new FoodSocketPowers.PackPlayerCall();
        }

        return var0;
    }

    public static FoodSocketPowers.PreparedPack preparePack(
        FoodSocketPowers.PackPlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4
    ) {
        if (!matchesPackBatch(var0, var1, var2, var3, var4)) {
            return null;
        } else {
            FoodSocketPowers.CompleteBatchBinding var5 = new FoodSocketPowers.CompleteBatchBinding(var1, var2, var3, var4);
            return var5.originalBinding == null ? null : new FoodSocketPowers.PreparedPack(var0, var5);
        }
    }

    private static boolean publishedPack(FoodSocketPowers.PackCommit var0) {
        if (var0.plan.context.player().getPersistentData().get("FTBossFoodSocketPowers") != var0.publishedController) {
            return false;
        } else {
            for (String var2 : List.of(
                "PackToken", "PackWeapon", "PackWeaponId", "PackHand", "PackDimension", "PackFingerprint", "PackSource", "PackUntil", "PackCooldown"
            )) {
                if (!Objects.equals(var0.publishedController.get(var2), var0.publishedState.get(var2))) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean matchesPackReceipt(FoodSocketPowers.PreparedPack var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2) {
        if (var0 != null && var0.originalSequence != null) {
            return var0.originalSequence.matchesReceipt(var0, var2, var1);
        } else if (var0 != null && var2 != null && Float.isFinite(var1) && !(var1 <= 0.0F)) {
            FoodSocketPowers.CompleteBatchBinding var3 = var0.binding;
            FoodWeaponBondHits.Context var4 = var0.plan.context;
            return var2.matchesBatch(var3.batch, var3.admission, var3.requests, var3.targets)
                && var2.matches(var0.plan, var4.player(), var4.source(), var4.quotedAt(), var1)
                && var2.attempt(FoodWeaponSourceLedger.AttemptDomain.WOLF_PACK)
                    .filter(
                        var0x -> var0x.domain() == FoodWeaponSourceLedger.AttemptDomain.WOLF_PACK
                                && var0x.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                    )
                    .isPresent();
        } else {
            return false;
        }
    }

    private static void restorePack(FoodSocketPowers.PackCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (ownsPackCleanup(var0, var1)) {
            var0.cleanupCompleted = true;
            retirePackPublication(var0.prepared, FoodWeaponSourceLedger.originalInteractionBinding(var0.prepared.binding.batch));
        }
    }

    private static boolean packLeafLive(FoodSocketPowers.PackCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        return var0 != null && var0.prepared.originalSequence != null
            ? var0.executed && publishedPack(var0) && var0.prepared.originalSequence.continuing(var0, var1)
            : var0 != null
                && var0.executed
                && publishedPack(var0)
                && matchesPackReceipt(var0.prepared, var0.amount, var0.receipt)
                && FoodWeaponFinalDamage.matchesPackInvocation(var0, var1);
    }

    private static int spawnPack(FoodSocketPowers.PackCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        FoodSocketPowers.PackPlan var2 = var0.plan;
        ServerPlayer var3 = var2.context.player();
        UUID var4 = var0.token;
        int var5 = 0;

        for (int var6 = 0; var6 < 2; var6++) {
            if (!packLeafLive(var0, var1)) {
                return var5;
            }

            Wolf var7 = EntityType.WOLF.create(var3.serverLevel());
            if (!packLeafLive(var0, var1)) {
                if (var7 != null) {
                    var7.discard();
                }

                return var5;
            }

            if (var7 != null) {
                double var8 = var6 == 0 ? -1.0 : 1.0;
                if (!packLeafLive(var0, var1)) {
                    var7.discard();
                    return var5;
                }

                var7.moveTo(var3.getX() + var8, var3.getY(), var3.getZ(), var3.getYRot(), 0.0F);
                if (!packLeafLive(var0, var1)) {
                    var7.discard();
                    return var5;
                }

                boolean var10 = var3.serverLevel().noCollision(var7) && var3.serverLevel().getFluidState(var7.blockPosition()).isEmpty();
                if (!packLeafLive(var0, var1)) {
                    var7.discard();
                    return var5;
                }

                if (!var10) {
                    var7.discard();
                    if (!packLeafLive(var0, var1)) {
                        return var5;
                    }
                } else {
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.setTame(true, true);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.setOwnerUUID(var3.getUUID());
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.setAge(-24000);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.resetLove();
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.setOrderedToSit(false);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.getAttribute(Attributes.MAX_HEALTH).setBaseValue(12.0);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.setHealth(12.0F);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    CompoundTag var11 = new CompoundTag();
                    var11.putUUID("Owner", var3.getUUID());
                    var11.putString("Origin", "ftboss:food_socket_pack");
                    var11.putBoolean("PermanentBaby", true);
                    var11.putUUID("Token", var4);
                    var11.putLong("Expires", var2.quotedAt() + 600L);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.getPersistentData().put("FTBossFoodSocketHelper", var11);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    LemonSummonRewards.mark(var7);
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    var7.setTarget(var2.context.target());
                    if (!packLeafLive(var0, var1)) {
                        var7.discard();
                        return var5;
                    }

                    if (var3.serverLevel().addFreshEntity(var7)) {
                        var5++;
                    } else {
                        var7.discard();
                    }

                    if (!packLeafLive(var0, var1)) {
                        return var5;
                    }
                }
            }
        }

        return var5;
    }

    public static FoodSocketPowers.PackCommit commitPack(FoodSocketPowers.PreparedPack var0, float var1, FoodWeaponFinalDamage.OffenseReceipt var2) {
        FoodWeaponConsumerSequence.Accepted var3 = var0 == null ? null : var0.originalSequence;
        boolean var4 = false;
        int var5 = var3 == null ? 0 : var3.publicationCallEntered(var0);
        Throwable var6 = null;

        FoodSocketPowers.PackCommit var38;
        try {
            if (var0 == null
                || var0.publishAttempted
                || var0.publishingCleanup != null && !FoodWeaponFinalDamage.matchesPackPublishing(var0, var2, var0.publishingCleanup)) {
                return null;
            }

            FoodSocketPowers.PackPlan var7 = var0.plan;
            if (!packContext(var7) || !matchesPackReceipt(var0, var1, var2)) {
                return null;
            }

            var0.publishAttempted = true;
            var0.publicationReceipt = var2;
            var0.publicationAmountBits = Float.floatToRawIntBits(var1);
            var4 = true;
            ServerPlayer var8 = var7.context.player();
            ItemStack var9 = var7.context.actualWeapon();
            CompoundTag var10 = var8.getPersistentData();
            CompoundTag var11 = var10.getCompound("FTBossFoodSocketPowers").copy();
            UUID var12 = NativeFoundationItemData.customData(var9) != null && NativeFoundationItemData.customData(var9).hasUUID("FTBossFoodSocketWeaponToken")
                ? NativeFoundationItemData.customData(var9).getUUID("FTBossFoodSocketWeaponToken")
                : UUID.randomUUID();
            UUID var13 = UUID.randomUUID();
            CompoundTag var14 = var7.snapshot();
            beginPackOwnerController(var0, var2, var10, var11, var12, var13, var14);
            putPackPlayerUuid(var0, 0, var11, "PackToken", var13);
            putPackPlayerUuid(var0, 1, var11, "PackWeapon", var12);
            putPackPlayerUuid(var0, 2, var11, "PackWeaponId", var14.getUUID("WeaponId"));
            putPackPlayerString(var0, 3, var11, "PackHand", var14.getString("OriginalHand"));
            putPackPlayerString(var0, 4, var11, "PackDimension", var14.getString("Dimension"));
            putPackPlayerTag(var0, 5, var11, "PackFingerprint", FoodWeaponBondHits.fingerprint(var9));
            putPackPlayerTag(var0, 6, var11, "PackSource", var14.copy());
            putPackPlayerLong(var0, 7, var11, "PackUntil", var7.quotedAt() + 600L);
            putPackPlayerLong(var0, 8, var11, "PackCooldown", var7.quotedAt() + 400L);
            var7.committed = true;
            FoodSocketPowers.PackPublication var15 = new FoodSocketPowers.PackPublication(var0, var10, var11);
            var0.publication = var15;
            planPackOwnerToken(var0, var12);

            try {
                var15.weaponWriteEntered = true;
                var15.weaponTagLookupEntered = true;
                CompoundTag var16 = NativeSocketWeaponTokenData.entry(var9);
                var15.weaponTagReceiver = var16;
                var15.weaponTagLookupCompleted = true;
                enterPackOwnerUuid(var0, var9, var16, var12);
                var15.weaponTokenWriteEntered = true;
                NativeSocketWeaponTokenData.write(var9, var16, var12);
                var15.weaponWriteCompleted = true;
                returnPackOwnerUuid(var0, var9, var16, var12);
            } catch (Throwable var31) {
                recordPackOriginalFailure(var0, var31);
                throw var31;
            } finally {
                closePackOwnerUuid(var0);
            }

            var15.persistentLookupEntered = true;
            CompoundTag var37 = var8.getPersistentData();
            var15.persistent = var37;
            var15.persistentLookupCompleted = true;
            var15.preimageReadEntered = true;
            var15.previousRoot = var37.get("FTBossFoodSocketPowers");

            for (String var18 : var15.keys) {
                Tag var19 = var15.previousRoot instanceof CompoundTag var20 ? var20.get(var18) : null;
                if (var19 != null) {
                    var15.previous.put(var18, var19.copy());
                }
            }

            var15.preimageReadCompleted = true;
            var15.rootWriteEntered = true;
            putPackPlayerRoot(var0, var15, var37, var11);
            if (readBackPackPublication(var15)) {
                var38 = new FoodSocketPowers.PackCommit(var0, var2, var1, var13, var11);
                var0.commitConstructed = true;
                if (var0.originalSequence != null) {
                    var0.originalSequence.published(var0, var38);
                }

                return var38;
            }

            var38 = null;
        } catch (Throwable var33) {
            var6 = var33;
            if (var4) {
                var0.publishFailure = var33;
                if (var0.originalSequence != null) {
                    var0.originalSequence.originalThrown(var33);
                }

                recordPackOriginalFailure(var0, var33);
            }

            throw var33;
        } finally {
            if (var4) {
                var0.publishExited = true;
            }

            if (var3 != null) {
                var3.publicationExited(var0, var5, var6);
            }
        }

        return var38;
    }

    private static boolean preparedCleanupCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class)
            && StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodSocketPowers.class)
                            .findFirst()
                            .map(
                                var0x -> var0x.getDeclaringClass() == FoodWeaponFinalDamage.class
                                        && var0x.getMethodName().equals("finalizeFailedPublication")
                                        && var0x.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")
                            )
                            .orElse(false)
                );
    }

    private static void retirePackPublication(FoodSocketPowers.PreparedPack var0, FoodWeaponSourceLedger.OriginalInteractionBinding var1) {
        FoodSocketPowers.PackPublication var2 = var0.publication;
        if (var2 != null && !var2.cleanupStarted) {
            var2.cleanupStarted = true;

            try {
                if (!FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(var0.binding.originalBinding, var1, var2.player)) {
                    var2.displaced = true;
                } else if (!var2.rootWriteEntered) {
                    var2.cleanupCompleted = true;
                } else {
                    for (int var3 = 0; var3 < var2.keys.size(); var3++) {
                        if (!ownsPackPublication(var0, var1)) {
                            var2.displaced = true;
                            return;
                        }

                        String var4 = var2.keys.get(var3);
                        Tag var5 = var2.previous.get(var4);
                        Tag var6 = var5 == null ? null : var5.copy();
                        if (!ownsPackPublication(var0, var1)) {
                            var2.displaced = true;
                            return;
                        }

                        var2.restoreProgress[var3] = 1;
                        if (var6 == null) {
                            var2.target.remove(var4);
                        } else {
                            var2.target.put(var4, var6);
                        }

                        var2.restoreProgress[var3] = 2;
                    }

                    var2.cleanupCompleted = ownsPackPublication(var0, var1);
                    var2.displaced = !var2.cleanupCompleted;
                }
            } catch (Error | RuntimeException var7) {
                var2.cleanupFailure = var7;
                throw var7;
            }
        }
    }

    private static void finishPackOwnerController(FoodSocketPowers.PreparedPack var0, FoodWeaponFinalDamage.PublishingCleanup var1, Throwable var2) {
        if (var0.ownerController != null && !var0.ownerControllerFinishEntered) {
            var0.ownerControllerFinishEntered = true;

            try {
                FoodWeaponSourceLedger.finishOwnerPackWriter(var0.ownerController, var0, var1);
                var0.ownerControllerFinishCompleted = true;
            } catch (Error | RuntimeException var4) {
                var0.ownerControllerFinishFailure = var4;
                recordPackObserverFailure(var0, 3, var4);
            }
        }
    }

    public static boolean matchesPackBatch(
        FoodSocketPowers.PackPlan var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4
    ) {
        return packContext(var0) && FoodWeaponBondHits.matchesBatch(var0.context, FoodWeaponSourceLedger.EffectDomain.WOLF_PACK, var1, var2, var3, var4);
    }

    private static boolean ownsPackCleanup(FoodSocketPowers.PackCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1) {
        if (var0 != null && var0.prepared.originalSequence != null) {
            return var0.executed
                && !var0.cleanupCompleted
                && var0.prepared.plan == var0.plan
                && var0.plan.committed
                && publishedPack(var0)
                && var0.prepared.originalSequence.continuing(var0, var1);
        } else if (var0 != null
            && var0.executed
            && !var0.cleanupCompleted
            && var0.prepared.plan == var0.plan
            && var0.plan.committed
            && publishedPack(var0)
            && FoodWeaponFinalDamage.matchesPackCleanupInvocation(var0, var1)) {
            FoodSocketPowers.CompleteBatchBinding var2 = var0.prepared.binding;
            return var2.originalBinding != null
                && var2.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(var2.batch)
                && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(
                    var2.originalBinding, FoodWeaponSourceLedger.originalInteractionBinding(var2.batch), var0.plan.context.player()
                );
        } else {
            return false;
        }
    }

    private static void captureNativePack202(BeforeMitigation event) {
        FoodSocketPowers.PackPlan plan = null;
        if (!(event.getAmount() <= 0.0F)
            && event.getSource().getEntity() instanceof ServerPlayer player
            && event.getSource().getDirectEntity() == player
            && player.isAlive()
            && !player.isSpectator()
            && validTarget(player, event.getEntity())
            && pack(player.getMainHandItem())) {
            FoodWeaponBondHits.Context context = FoodWeaponBondHits.quoteHitContext(event.getEntity(), event.getSource());
            if (context == null || !packSourceContext(context) || !pack(context.actualWeapon())) {
                FoodWeaponFinalDamage.rejectNativeProducer(event);
                return;
            }
            CompoundTag controller = player.getPersistentData().getCompound("FTBossFoodSocketPowers");
            if (FoodSocketPowerRules.ready(context.quotedAt(), controller.getLong("PackCooldown"), 400)
                && !FoodSocketPowerRules.active(context.quotedAt(), controller.getLong("PackUntil"), 600)) {
                plan = new FoodSocketPowers.PackPlan(context, new FoodWeaponSourceLedger.AttemptRequest(FoodWeaponSourceLedger.AttemptDomain.WOLF_PACK, 1.0));
                if (!packContext(plan)) {
                    FoodWeaponFinalDamage.rejectNativeProducer(event);
                    return;
                }
            }
        }
        FoodWeaponFinalDamage.capturePack(event, plan);
    }

    private static boolean packContext(FoodSocketPowers.PackPlan var0) {
        if (var0 != null
            && !var0.committed
            && FoodWeaponBondHits.matchesContext(var0.context)
            && packSourceContext(var0.context)
            && validTarget(var0.context.player(), var0.context.target())
            && pack(var0.context.actualWeapon())
            && Objects.equals(var0.controller, var0.context.player().getPersistentData().get("FTBossFoodSocketPowers"))
            && Objects.equals(var0.weaponTag, NativeFoundationItemData.image(var0.context.actualWeapon()))) {
            CompoundTag var1 = var0.context.player().getPersistentData().getCompound("FTBossFoodSocketPowers");
            return FoodSocketPowerRules.ready(var0.quotedAt(), var1.getLong("PackCooldown"), 400)
                && !FoodSocketPowerRules.active(var0.quotedAt(), var1.getLong("PackUntil"), 600);
        } else {
            return false;
        }
    }

    private static void beginPackOwnerController(
        FoodSocketPowers.PreparedPack var0,
        FoodWeaponFinalDamage.OffenseReceipt var1,
        CompoundTag var2,
        CompoundTag var3,
        UUID var4,
        UUID var5,
        CompoundTag var6
    ) {
        if (var0.publishingCleanup != null) {
            try {
                CompoundTag var7 = FoodWeaponBondHits.originalContextFingerprint(var0.plan.context);
                var0.ownerParameters = new FoodSocketPowers.PackOwnerParameters(var2, var3, var4, var5, var7, var6, var0.plan.quotedAt());
                var0.ownerPublicationLookupEntered = true;
                FoodWeaponSourceLedger.OwnerWriterPublication var8 = FoodWeaponFinalDamage.packPublishingWriter(var0, var1, var0.publishingCleanup);
                var0.ownerPublication = var8;
                var0.ownerPublicationLookupCompleted = true;
                if (var8 == null) {
                    return;
                }

                var0.ownerControllerBeginEntered = true;
                var0.ownerController = FoodWeaponSourceLedger.beginOwnerPackController(var8, var0, var0.publishingCleanup, var2, var3, var4, var5, var7);
                var0.ownerControllerBeginCompleted = true;
            } catch (Throwable var9) {
                recordPackObserverFailure(var0, 0, var9);
            }
        }
    }

    private static void putPackPlayerUuid(FoodSocketPowers.PreparedPack var0, int var1, CompoundTag var2, String var3, UUID var4) {
        FoodSocketPowers.PackPlayerCall var5 = beginPackPlayerCall(var0, var1, var2, var3);
        var5.argument = var4;
        planPackPlayerUuid(var5, var1, var4);

        try {
            enterPackPlayerUuid(var5, var4);
            var5.originalEntered = true;
            var2.putUUID(var3, var4);
            var5.originalReturned = true;
            returnPackPlayerTag(var5);
        } catch (Throwable var10) {
            recordPackPlayerOriginalFailure(var5, var10);
            throw var10;
        } finally {
            closePackPlayerCall(var5);
        }
    }

    private static void putPackPlayerString(FoodSocketPowers.PreparedPack var0, int var1, CompoundTag var2, String var3, String var4) {
        FoodSocketPowers.PackPlayerCall var5 = beginPackPlayerCall(var0, var1, var2, var3);
        var5.argument = var4;
        planPackPlayerString(var5, var1, var4);

        try {
            enterPackPlayerString(var5, var4);
            var5.originalEntered = true;
            var2.putString(var3, var4);
            var5.originalReturned = true;
            returnPackPlayerTag(var5);
        } catch (Throwable var10) {
            recordPackPlayerOriginalFailure(var5, var10);
            throw var10;
        } finally {
            closePackPlayerCall(var5);
        }
    }

    private static Tag putPackPlayerTag(FoodSocketPowers.PreparedPack var0, int var1, CompoundTag var2, String var3, Tag var4) {
        FoodSocketPowers.PackPlayerCall var5 = beginPackPlayerCall(var0, var1, var2, var3);
        var5.argument = var4;
        planPackPlayerTag(var5, var1, var4);

        Tag var7;
        try {
            enterPackPlayerTag(var5, var4);
            var5.originalEntered = true;
            Tag var6 = var2.put(var3, var4);
            var5.returnedValue = var6;
            var5.originalReturned = true;
            returnPackPlayerTag(var5);
            var7 = var6;
        } catch (Throwable var11) {
            recordPackPlayerOriginalFailure(var5, var11);
            throw var11;
        } finally {
            closePackPlayerCall(var5);
        }

        return var7;
    }

    private static void putPackPlayerLong(FoodSocketPowers.PreparedPack var0, int var1, CompoundTag var2, String var3, long var4) {
        FoodSocketPowers.PackPlayerCall var6 = beginPackPlayerCall(var0, var1, var2, var3);
        var6.longArgument = var4;
        planPackPlayerLong(var6, var1, var4);

        try {
            enterPackPlayerLong(var6, var4);
            var6.originalEntered = true;
            var2.putLong(var3, var4);
            var6.originalReturned = true;
            returnPackPlayerTag(var6);
        } catch (Throwable var11) {
            recordPackPlayerOriginalFailure(var6, var11);
            throw var11;
        } finally {
            closePackPlayerCall(var6);
        }
    }

    private static void planPackOwnerToken(FoodSocketPowers.PreparedPack var0, UUID var1) {
        if (var0.ownerPublication != null) {
            var0.ownerTokenPlanEntered = true;

            try {
                var0.ownerTokenStep = FoodWeaponSourceLedger.planOwnerWolfTokenWriter(var0.ownerPublication, var0, var0, 0, var1);
                var0.ownerTokenPlanReturned = true;
            } catch (Throwable var3) {
                recordPackObserverFailure(var0, 0, var3);
            }
        }
    }

    private static void enterPackOwnerUuid(FoodSocketPowers.PreparedPack var0, ItemStack var1, CompoundTag var2, UUID var3) {
        if (var0.ownerTokenStep != null) {
            var0.ownerUuidEntryEntered = true;

            try {
                FoodWeaponSourceLedger.ownerWriterUuidEntered(var0.ownerTokenStep, var1, var2, "FTBossFoodSocketWeaponToken", var3);
                var0.ownerUuidEntryReturned = true;
            } catch (Throwable var5) {
                recordPackObserverFailure(var0, 0, var5);
            }
        }
    }

    private static void returnPackOwnerUuid(FoodSocketPowers.PreparedPack var0, ItemStack var1, CompoundTag var2, UUID var3) {
        if (var0.ownerTokenStep != null) {
            var0.ownerUuidReturnEntered = true;

            try {
                FoodWeaponSourceLedger.ownerWriterUuidReturned(var0.ownerTokenStep, var1, var2, "FTBossFoodSocketWeaponToken", var3);
                var0.ownerUuidReturnReturned = true;
            } catch (Throwable var5) {
                recordPackObserverFailure(var0, 1, var5);
            }
        }
    }

    private static void closePackOwnerUuid(FoodSocketPowers.PreparedPack var0) {
        if (var0.ownerTokenStep != null && !var0.ownerUuidCloseEntered) {
            var0.ownerUuidCloseEntered = true;

            try {
                FoodWeaponSourceLedger.closeOwnerWriterUuidStep(var0.ownerTokenStep);
                var0.ownerUuidCloseReturned = true;
            } catch (Throwable var2) {
                recordPackObserverFailure(var0, 3, var2);
            }
        }
    }

    private static void recordPackOriginalFailure(FoodSocketPowers.PreparedPack var0, Throwable var1) {
        if (var0.originalSequence != null) {
            var0.originalSequence.originalThrown(var1);
        }

        if (var0.ownerWriterFailures.original == null) {
            var0.ownerWriterFailures.original(var1);

            try {
                if (var0.ownerTokenStep != null) {
                    FoodWeaponSourceLedger.ownerWriterOriginalFailed(var0.ownerTokenStep, var1);
                } else if (var0.ownerController != null) {
                    FoodWeaponSourceLedger.ownerPlayerWriterOriginalFailed(var0.ownerController, var1);
                } else if (var0.ownerPublication != null) {
                    FoodWeaponSourceLedger.ownerPublicationOriginalFailed(var0.ownerPublication, var1);
                }
            } catch (Throwable var3) {
                var0.ownerWriterFailures.diagnostic(2, var3);
                recordPackObserverFailure(var0, 2, var3);
            }
        }
    }

    private static Tag putPackPlayerRoot(FoodSocketPowers.PreparedPack var0, FoodSocketPowers.PackPublication var1, CompoundTag var2, CompoundTag var3) {
        FoodSocketPowers.PackPlayerCall var4 = beginPackPlayerCall(var0, 9, var2, "FTBossFoodSocketPowers");
        var4.argument = var3;
        planPackPlayerRoot(var4, var2, var3);

        Tag var6;
        try {
            enterPackPlayerTag(var4, var3);
            var4.originalEntered = true;
            Tag var5 = var2.put("FTBossFoodSocketPowers", var3);
            var1.rootWriteCompleted = true;
            var4.returnedValue = var5;
            var4.originalReturned = true;
            returnPackPlayerTag(var4);
            var6 = var5;
        } catch (Throwable var10) {
            recordPackPlayerOriginalFailure(var4, var10);
            throw var10;
        } finally {
            closePackPlayerCall(var4);
        }

        return var6;
    }

    private static boolean readBackPackPublication(FoodSocketPowers.PackPublication var0) {
        var0.rootReadbackMatched = var0.persistent.get("FTBossFoodSocketPowers") == var0.target;
        if (!var0.rootReadbackMatched) {
            return false;
        } else {
            boolean var1 = true;

            for (int var2 = 0; var2 < var0.keys.size(); var2++) {
                String var3 = var0.keys.get(var2);
                var0.readbackProgress[var2] = 1;
                Tag var4 = var0.target.get(var3);
                if (var4 != null) {
                    var0.observed.put(var3, var4.copy());
                }

                var0.readbackProgress[var2] = 2;
                var1 &= Objects.equals(var0.observed.get(var3), var0.expected.get(var3));
            }

            var0.rootReadbackCompleted = true;
            var0.ownPostimageMatched = var1 && var0.persistent.get("FTBossFoodSocketPowers") == var0.target;
            return var0.ownPostimageMatched;
        }
    }

    private static boolean ownsPackPublication(FoodSocketPowers.PreparedPack var0, FoodWeaponSourceLedger.OriginalInteractionBinding var1) {
        FoodSocketPowers.PackPublication var2 = var0.publication;
        FoodSocketPowers.CompleteBatchBinding var3 = var0.binding;
        if (!packCleanupPhase(var0)
            || var2 == null
            || !Thread.holdsLock(FoodWeaponSourceLedger.class)
            || Thread.currentThread() != var2.thread
            || var2.server != var2.player.serverLevel().getServer()
            || !var2.server.isSameThread()
            || var3.originalBinding != FoodWeaponSourceLedger.originalInteractionBinding(var3.batch)
            || !FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(var3.originalBinding, var1, var2.player)) {
            return false;
        } else if (var2.player.getPersistentData() == var2.persistent && var2.persistent.get("FTBossFoodSocketPowers") == var2.target) {
            for (int var4 = 0; var4 < var2.keys.size(); var4++) {
                String var5 = var2.keys.get(var4);
                Tag var6 = var2.restoreProgress[var4] == 2 ? var2.previous.get(var5) : var2.expected.get(var5);
                if (!Objects.equals(var2.target.get(var5), var6)) {
                    return false;
                }
            }

            return FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(var3.originalBinding, var1, var2.player) && packCleanupPhase(var0);
        } else {
            return false;
        }
    }

    private static void recordPackObserverFailure(FoodSocketPowers.PreparedPack var0, int var1, Throwable var2) {
        if (var0.originalSequence != null) {
            var0.originalSequence.observationFailed(var1, var2);
        }

        var0.ownerWriterFailures.observer(var1, var2);

        try {
            if (var0.ownerTokenStep != null) {
                FoodWeaponSourceLedger.ownerWriterObservationFailed(var0.ownerTokenStep, var1, var2);
            } else if (var0.ownerController != null) {
                FoodWeaponSourceLedger.ownerPlayerWriterObservationFailed(var0.ownerController, var1, var2);
            } else if (var0.ownerPublication != null) {
                FoodWeaponSourceLedger.ownerPublicationObservationFailed(var0.ownerPublication, var1, var2);
            }
        } catch (Throwable var4) {
            var0.ownerWriterFailures.diagnostic(var1, var4);
        }
    }

    private static boolean packSourceContext(FoodWeaponBondHits.Context var0) {
        if (var0 == null) {
            return false;
        } else {
            ItemStack var1 = var0.actualWeapon();
            if (!FourWeaponFoundationAdapter.applies(var1)) {
                return true;
            } else {
                CompoundTag var2 = var0.snapshot();
                CompoundTag var3 = FoodWeaponMaterialFingerprint.foundationSnapshot(var1);
                return var2.contains("FoundationSnapshot", 10)
                    && var2.contains("FoundationApprovedRules", 8)
                    && !var2.getString("FoundationApprovedRules").isEmpty()
                    && !var3.isEmpty()
                    && var3.equals(var2.getCompound("FoundationSnapshot"))
                    && FoodWeaponSourceLedger.validReadOnly(var2, var0.player())
                    && FoodWeaponSourceLedger.originalHandContinuousReadOnly(var2, var0.player());
            }
        }
    }

    private static FoodSocketPowers.PackPlayerCall beginPackPlayerCall(FoodSocketPowers.PreparedPack var0, int var1, CompoundTag var2, String var3) {
        FoodSocketPowers.PackPlayerCall var4 = var0.ownerPlayerCalls[var1];
        var4.publication = var0.ownerPublication;
        var4.originalSequence = var0.originalSequence;
        var4.writer = var0.ownerController;
        var4.receiver = var2;
        var4.key = var3;
        return var4;
    }

    private static void planPackPlayerUuid(FoodSocketPowers.PackPlayerCall var0, int var1, UUID var2) {
        if (var0.writer != null) {
            var0.planEntered = true;

            try {
                var0.step = FoodWeaponSourceLedger.planOwnerPlayerUuidKey(var0.writer, var1, var0.receiver, var0.key, var2);
                var0.planReturned = true;
            } catch (Throwable var4) {
                recordPackPlayerObserverFailure(var0, 0, var4);
            }
        }
    }

    private static void enterPackPlayerUuid(FoodSocketPowers.PackPlayerCall var0, UUID var1) {
        if (var0.step != null) {
            var0.entryEntered = true;

            try {
                FoodWeaponSourceLedger.ownerPlayerUuidEntered(var0.step, var0.receiver, var0.key, var1);
                var0.entryReturned = true;
            } catch (Throwable var3) {
                recordPackPlayerObserverFailure(var0, 0, var3);
            }
        }
    }

    private static void returnPackPlayerTag(FoodSocketPowers.PackPlayerCall var0) {
        if (var0.step != null) {
            var0.returnEntered = true;

            try {
                FoodWeaponSourceLedger.ownerPlayerPutReturned(var0.step, var0.receiver);
                var0.returnReturned = true;
            } catch (Throwable var2) {
                recordPackPlayerObserverFailure(var0, 1, var2);
            }
        }
    }

    private static void closePackPlayerCall(FoodSocketPowers.PackPlayerCall var0) {
        if (var0.step != null && !var0.closeEntered) {
            var0.closeEntered = true;

            try {
                FoodWeaponSourceLedger.closeOwnerPlayerStep(var0.step);
                var0.closeReturned = true;
            } catch (Throwable var2) {
                recordPackPlayerObserverFailure(var0, 3, var2);
            }
        }
    }

    private static void recordPackPlayerOriginalFailure(FoodSocketPowers.PackPlayerCall var0, Throwable var1) {
        if (var0.originalSequence != null) {
            var0.originalSequence.originalThrown(var1);
        }

        if (var0.failures.original == null) {
            var0.failures.original(var1);

            try {
                if (var0.step != null) {
                    FoodWeaponSourceLedger.ownerPlayerOriginalFailed(var0.step, var1);
                } else if (var0.writer != null) {
                    FoodWeaponSourceLedger.ownerPlayerWriterOriginalFailed(var0.writer, var1);
                }
            } catch (Throwable var3) {
                var0.failures.diagnostic(2, var3);
                recordPackPlayerObserverFailure(var0, 2, var3);
            }
        }
    }

    private static void planPackPlayerString(FoodSocketPowers.PackPlayerCall var0, int var1, String var2) {
        if (var0.writer != null) {
            var0.planEntered = true;

            try {
                var0.step = FoodWeaponSourceLedger.planOwnerPlayerStringKey(var0.writer, var1, var0.receiver, var0.key, var2);
                var0.planReturned = true;
            } catch (Throwable var4) {
                recordPackPlayerObserverFailure(var0, 0, var4);
            }
        }
    }

    private static void enterPackPlayerString(FoodSocketPowers.PackPlayerCall var0, String var1) {
        if (var0.step != null) {
            var0.entryEntered = true;

            try {
                FoodWeaponSourceLedger.ownerPlayerStringEntered(var0.step, var0.receiver, var0.key, var1);
                var0.entryReturned = true;
            } catch (Throwable var3) {
                recordPackPlayerObserverFailure(var0, 0, var3);
            }
        }
    }

    private static void planPackPlayerTag(FoodSocketPowers.PackPlayerCall var0, int var1, Tag var2) {
        if (var0.writer != null) {
            var0.planEntered = true;

            try {
                var0.step = FoodWeaponSourceLedger.planOwnerPlayerKey(var0.writer, var1, var0.key, var2);
                var0.planReturned = true;
            } catch (Throwable var4) {
                recordPackPlayerObserverFailure(var0, 0, var4);
            }
        }
    }

    private static void enterPackPlayerTag(FoodSocketPowers.PackPlayerCall var0, Tag var1) {
        if (var0.step != null) {
            var0.entryEntered = true;

            try {
                FoodWeaponSourceLedger.ownerPlayerPutEntered(var0.step, var0.receiver, var0.key, var1);
                var0.entryReturned = true;
            } catch (Throwable var3) {
                recordPackPlayerObserverFailure(var0, 0, var3);
            }
        }
    }

    private static void planPackPlayerLong(FoodSocketPowers.PackPlayerCall var0, int var1, long var2) {
        if (var0.writer != null) {
            var0.planEntered = true;

            try {
                var0.step = FoodWeaponSourceLedger.planOwnerPlayerLongKey(var0.writer, var1, var0.receiver, var0.key, var2);
                var0.planReturned = true;
            } catch (Throwable var5) {
                recordPackPlayerObserverFailure(var0, 0, var5);
            }
        }
    }

    private static void enterPackPlayerLong(FoodSocketPowers.PackPlayerCall var0, long var1) {
        if (var0.step != null) {
            var0.entryEntered = true;

            try {
                FoodWeaponSourceLedger.ownerPlayerLongEntered(var0.step, var0.receiver, var0.key, var1);
                var0.entryReturned = true;
            } catch (Throwable var4) {
                recordPackPlayerObserverFailure(var0, 0, var4);
            }
        }
    }

    private static void planPackPlayerRoot(FoodSocketPowers.PackPlayerCall var0, CompoundTag var1, CompoundTag var2) {
        if (var0.writer != null) {
            var0.planEntered = true;

            try {
                var0.step = FoodWeaponSourceLedger.planOwnerPlayerRoot(var0.writer, 9, var1, var2);
                var0.planReturned = true;
            } catch (Throwable var4) {
                recordPackPlayerObserverFailure(var0, 0, var4);
            }
        }
    }

    private static boolean packCleanupPhase(FoodSocketPowers.PreparedPack var0) {
        return var0.originalSequence != null && var0.originalSequence.cleanup(var0, var0.publishingCleanup)
            ? true
            : var0.finalizingCleanup == null
                || var0.publishingCleanup == var0.finalizingCleanup && FoodWeaponFinalDamage.matchesPackPublicationCleanup(var0, var0.finalizingCleanup);
    }

    private static void recordPackPlayerObserverFailure(FoodSocketPowers.PackPlayerCall var0, int var1, Throwable var2) {
        if (var0.originalSequence != null) {
            var0.originalSequence.observationFailed(var1, var2);
        }

        var0.failures.observer(var1, var2);

        try {
            if (var0.step != null) {
                FoodWeaponSourceLedger.ownerPlayerObservationFailed(var0.step, var1, var2);
            } else if (var0.writer != null) {
                FoodWeaponSourceLedger.ownerPlayerWriterObservationFailed(var0.writer, var1, var2);
            }
        } catch (Throwable var4) {
            var0.failures.diagnostic(var1, var4);
        }
    }

    public static List<WeaponDetailView> details(ItemStack var0) {
        int[] var1 = new int[FoodType.values().length];

        for (FoodType var5 : FoodType.values()) {
            var1[var5.ordinal()] = count(var0, var5);
        }

        int var11 = var1[FoodType.FISH.ordinal()];
        int var12 = var1[FoodType.WINE.ordinal()];
        int var13 = var1[FoodType.MEAT.ordinal()];
        int var14 = var1[FoodType.CHEESE.ordinal()];
        ArrayList var6 = new ArrayList();
        if (FoodSocketPowerRules.flight(var11, var12)) {
            var6.add(
                new WeaponDetailView(
                    Component.translatable("screen.ftboss.socket.combo"),
                    Component.translatable("screen.ftboss.socket.flight", Math.min(2, var11), Math.min(1, var12)),
                    Component.translatable("screen.ftboss.socket.flight.description")
                )
            );
        }

        if (FoodSocketPowerRules.pack(var13, var14)) {
            var6.add(
                new WeaponDetailView(
                    Component.translatable("screen.ftboss.socket.combo"),
                    Component.translatable("screen.ftboss.socket.pack", Math.min(2, var13), Math.min(1, var14)),
                    Component.translatable("screen.ftboss.socket.pack.description")
                )
            );
        }

        for (FoodType var10 : FoodType.values()) {
            if (setBonus(var1, var10) != 0) {
                var6.add(
                    new WeaponDetailView(
                        Component.translatable("screen.ftboss.socket.combo"),
                        Component.translatable("screen.ftboss.socket.set_boost"),
                        Component.translatable("screen.ftboss.socket.set_boost.description")
                    )
                );
                break;
            }
        }

        return List.copyOf(var6);
    }

    private static final class CompleteBatchBinding {
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;

        private CompleteBatchBinding(
            FoodWeaponSourceLedger.AttemptBatchQuote var1,
            FoodWeaponSourceLedger.AttemptBatchAdmission var2,
            List<FoodWeaponSourceLedger.AttemptRequest> var3,
            List<FoodWeaponSourceLedger.TargetRequest> var4
        ) {
            this.batch = var1;
            this.admission = var2;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(var1);
            this.requests = List.copyOf(var3);
            this.targets = List.copyOf(var4);
        }
    }

    public static final class PackCommit {
        private final FoodSocketPowers.PackPlan plan;
        private final FoodSocketPowers.PreparedPack prepared;
        private final CompoundTag publishedController;
        private final CompoundTag publishedState;
        private final CompoundTag previousController;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final float amount;
        private final UUID token;
        private final Thread thread;
        private final MinecraftServer server;
        private boolean executed;
        private boolean cleanupCompleted;

        private PackCommit(FoodSocketPowers.PreparedPack var1, FoodWeaponFinalDamage.OffenseReceipt var2, float var3, UUID var4, CompoundTag var5) {
            this.plan = var1.plan;
            this.prepared = var1;
            this.publishedController = var5;
            this.publishedState = var5.copy();
            this.previousController = var1.plan.controller instanceof CompoundTag var6 ? var6.copy() : new CompoundTag();
            this.receipt = var2;
            this.amount = var3;
            this.token = var4;
            this.thread = Thread.currentThread();
            this.server = var1.plan.context.player().serverLevel().getServer();
        }
    }

    private static final class PackOwnerParameters {
        private final CompoundTag persistent;
        private final CompoundTag data;
        private final UUID weaponToken;
        private final UUID packToken;
        private final CompoundTag fingerprint;
        private final CompoundTag snapshot;
        private final long tick;

        private PackOwnerParameters(CompoundTag var1, CompoundTag var2, UUID var3, UUID var4, CompoundTag var5, CompoundTag var6, long var7) {
            this.persistent = var1;
            this.data = var2;
            this.weaponToken = var3;
            this.packToken = var4;
            this.fingerprint = var5;
            this.snapshot = var6;
            this.tick = var7;
        }
    }

    public static final class PackPlan {
        private final FoodWeaponBondHits.Context context;
        private final Tag controller;
        private final Tag weaponTag;
        private final List<FoodWeaponSourceLedger.AttemptRequest> attempts;
        private boolean committed;

        private PackPlan(FoodWeaponBondHits.Context var1, FoodWeaponSourceLedger.AttemptRequest var2) {
            this.context = var1;
            Tag var3 = var1.player().getPersistentData().get("FTBossFoodSocketPowers");
            this.controller = var3 == null ? null : var3.copy();
            this.weaponTag = NativeFoundationItemData.image(var1.actualWeapon());
            this.attempts = List.of(var2);
        }

        public FoodWeaponBondHits.Context context() {
            return this.context;
        }

        public CompoundTag snapshot() {
            return this.context.snapshot();
        }

        public long quotedAt() {
            return this.context.quotedAt();
        }

        public List<FoodWeaponSourceLedger.AttemptRequest> attempts() {
            return this.attempts;
        }

        public List<FoodWeaponSourceLedger.TargetRequest> targets() {
            return List.of();
        }
    }

    private static final class PackPlayerCall {
        private final FoodSocketPowers.PackWriterFailures failures = new FoodSocketPowers.PackWriterFailures();
        private FoodWeaponSourceLedger.OwnerWriterPublication publication;
        private FoodWeaponSourceLedger.OwnerPlayerWriter writer;
        private FoodWeaponConsumerSequence.Accepted originalSequence;
        private FoodWeaponSourceLedger.OwnerPlayerStep step;
        private CompoundTag receiver;
        private String key;
        private Object argument;
        private long longArgument;
        private Tag returnedValue;
        private boolean planEntered;
        private boolean planReturned;
        private boolean entryEntered;
        private boolean entryReturned;
        private boolean originalEntered;
        private boolean originalReturned;
        private boolean returnEntered;
        private boolean returnReturned;
        private boolean closeEntered;
        private boolean closeReturned;
    }

    private static final class PackPublication {
        private final ServerPlayer player;
        private final ItemStack weapon;
        private final CompoundTag readPersistent;
        private final Tag beforeFirstWriteRoot;
        private final CompoundTag beforeFirstWrite;
        private CompoundTag persistent;
        private final CompoundTag target;
        private final CompoundTag expected;
        private Tag previousRoot;
        private final CompoundTag previous;
        private final CompoundTag observed;
        private final List<String> keys;
        private final byte[] readbackProgress;
        private final byte[] restoreProgress;
        private final Thread thread;
        private final MinecraftServer server;
        private CompoundTag weaponTagReceiver;
        private boolean weaponWriteEntered;
        private boolean weaponTagLookupEntered;
        private boolean weaponTagLookupCompleted;
        private boolean weaponTokenWriteEntered;
        private boolean weaponWriteCompleted;
        private boolean persistentLookupEntered;
        private boolean persistentLookupCompleted;
        private boolean preimageReadEntered;
        private boolean preimageReadCompleted;
        private boolean rootReadbackMatched;
        private boolean rootReadbackCompleted;
        private boolean ownPostimageMatched;
        private boolean rootWriteEntered;
        private boolean rootWriteCompleted;
        private boolean cleanupStarted;
        private boolean cleanupCompleted;
        private boolean displaced;
        private Throwable cleanupFailure;

        private PackPublication(FoodSocketPowers.PreparedPack var1, CompoundTag var2, CompoundTag var3) {
            this.player = var1.plan.context.player();
            this.weapon = var1.plan.context.actualWeapon();
            this.readPersistent = var2;
            this.beforeFirstWriteRoot = var2.get("FTBossFoodSocketPowers");
            this.beforeFirstWrite = new CompoundTag();
            this.target = var3;
            this.expected = new CompoundTag();
            this.previous = new CompoundTag();
            this.observed = new CompoundTag();
            this.keys = List.of(
                "PackToken", "PackWeapon", "PackWeaponId", "PackHand", "PackDimension", "PackFingerprint", "PackSource", "PackUntil", "PackCooldown"
            );
            this.readbackProgress = new byte[this.keys.size()];
            this.restoreProgress = new byte[this.keys.size()];

            for (String var5 : this.keys) {
                Tag var6 = var3.get(var5);
                if (var6 != null) {
                    this.expected.put(var5, var6.copy());
                }

                Tag var7 = this.beforeFirstWriteRoot instanceof CompoundTag var8 ? var8.get(var5) : null;
                if (var7 != null) {
                    this.beforeFirstWrite.put(var5, var7.copy());
                }
            }

            this.thread = Thread.currentThread();
            this.server = this.player.serverLevel().getServer();
        }
    }

    private static final class PackWriterFailures {
        private final Throwable[] observer = new Throwable[4];
        private final Throwable[] diagnostic = new Throwable[4];
        private Throwable original;

        private void observer(int var1, Throwable var2) {
            if (this.observer[var1] == null) {
                this.observer[var1] = var2;
            }
        }

        private void diagnostic(int var1, Throwable var2) {
            if (this.diagnostic[var1] == null) {
                this.diagnostic[var1] = var2;
            }
        }

        private void original(Throwable var1) {
            if (this.original == null) {
                this.original = var1;
            }
        }
    }

    public static final class PreparedPack {
        private final FoodSocketPowers.PackPlan plan;
        private final FoodSocketPowers.CompleteBatchBinding binding;
        private FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private FoodWeaponFinalDamage.PublishingCleanup finalizingCleanup;
        private FoodSocketPowers.PackPublication publication;
        private FoodWeaponConsumerSequence.Accepted originalSequence;
        private FoodSocketPowers.PackOwnerParameters ownerParameters;
        private FoodWeaponSourceLedger.OwnerWriterPublication ownerPublication;
        private FoodWeaponSourceLedger.OwnerPlayerWriter ownerController;
        private final FoodSocketPowers.PackWriterFailures ownerWriterFailures = new FoodSocketPowers.PackWriterFailures();
        private final FoodSocketPowers.PackPlayerCall[] ownerPlayerCalls = FoodSocketPowers.newPackPlayerCalls();
        private FoodWeaponSourceLedger.OwnerWriterStep ownerTokenStep;
        private boolean ownerTokenPlanEntered;
        private boolean ownerTokenPlanReturned;
        private boolean ownerUuidEntryEntered;
        private boolean ownerUuidEntryReturned;
        private boolean ownerUuidReturnEntered;
        private boolean ownerUuidReturnReturned;
        private boolean ownerUuidCloseEntered;
        private boolean ownerUuidCloseReturned;
        private boolean ownerPublicationLookupEntered;
        private boolean ownerPublicationLookupCompleted;
        private boolean ownerControllerBeginEntered;
        private boolean ownerControllerBeginCompleted;
        private boolean ownerControllerFinishEntered;
        private boolean ownerControllerFinishCompleted;
        private Throwable ownerControllerFinishFailure;
        private FoodWeaponFinalDamage.OffenseReceipt publicationReceipt;
        private int publicationAmountBits;
        private boolean publishAttempted;
        private boolean publishExited;
        private boolean commitConstructed;
        private Throwable publishFailure;

        private PreparedPack(FoodSocketPowers.PackPlan var1, FoodSocketPowers.CompleteBatchBinding var2) {
            this.plan = var1;
            this.binding = var2;
        }
    }
}
