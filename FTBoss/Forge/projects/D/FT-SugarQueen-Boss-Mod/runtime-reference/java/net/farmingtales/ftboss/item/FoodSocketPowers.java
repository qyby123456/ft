/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.animal.Wolf
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.BabyEntitySpawnEvent
 *  net.minecraftforge.event.entity.living.LivingChangeTargetEvent
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.LivingExperienceDropEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponConsumerSequence;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodSocketPowers {
    private static final String ROOT = "FTBossFoodSocketPowers";
    private static final String HELPER = "FTBossFoodSocketHelper";
    private static final String WEAPON_TOKEN = "FTBossFoodSocketWeaponToken";
    private static final double MAX_HELPER_DISTANCE_SQUARED = 1024.0;
    private static final double MAX_TARGET_DISTANCE_SQUARED = 576.0;
    private static final int HELPER_BREED_LOCK_TICKS = 6000;

    private FoodSocketPowers() {
    }

    private static int count(ItemStack itemStack, FoodWeaponEvolution.FoodType foodType) {
        return FoodWeaponEvolution.socketTypeCount(itemStack, foodType);
    }

    private static boolean flight(ItemStack itemStack) {
        int n = FoodSocketPowers.count(itemStack, FoodWeaponEvolution.FoodType.FISH);
        if (n < 2) {
            return false;
        }
        return FoodSocketPowerRules.flight(n, FoodSocketPowers.count(itemStack, FoodWeaponEvolution.FoodType.WINE));
    }

    private static boolean pack(ItemStack itemStack) {
        return FoodSocketPowerRules.pack(FoodSocketPowers.count(itemStack, FoodWeaponEvolution.FoodType.MEAT), FoodSocketPowers.count(itemStack, FoodWeaponEvolution.FoodType.CHEESE));
    }

    private static long now(ServerPlayer serverPlayer) {
        return serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
    }

    public static int setBonus(int[] nArray, FoodWeaponEvolution.FoodType foodType) {
        if (nArray.length != FoodWeaponEvolution.FoodType.values().length) {
            return 0;
        }
        FoodWeaponEvolution.FoodType foodType2 = switch (foodType) {
            default -> throw new IncompatibleClassChangeError();
            case FoodWeaponEvolution.FoodType.TEA -> FoodWeaponEvolution.FoodType.APPLE;
            case FoodWeaponEvolution.FoodType.APPLE -> FoodWeaponEvolution.FoodType.TEA;
            case FoodWeaponEvolution.FoodType.MEAT -> FoodWeaponEvolution.FoodType.CHEESE;
            case FoodWeaponEvolution.FoodType.CHEESE -> FoodWeaponEvolution.FoodType.MEAT;
            case FoodWeaponEvolution.FoodType.FISH -> FoodWeaponEvolution.FoodType.WINE;
            case FoodWeaponEvolution.FoodType.WINE -> FoodWeaponEvolution.FoodType.FISH;
            case FoodWeaponEvolution.FoodType.VEGETABLE -> FoodWeaponEvolution.FoodType.ENERGY;
            case FoodWeaponEvolution.FoodType.ENERGY -> FoodWeaponEvolution.FoodType.VEGETABLE;
            case FoodWeaponEvolution.FoodType.SWEET, FoodWeaponEvolution.FoodType.FOOD -> FoodWeaponEvolution.FoodType.APPLE;
        };
        int n = nArray[foodType.ordinal()];
        if (foodType == FoodWeaponEvolution.FoodType.SWEET || foodType == FoodWeaponEvolution.FoodType.FOOD) {
            n = nArray[FoodWeaponEvolution.FoodType.SWEET.ordinal()] + nArray[FoodWeaponEvolution.FoodType.FOOD.ordinal()];
        }
        return FoodSocketPowerRules.setBonus(n, nArray[foodType2.ordinal()]);
    }

    @SubscribeEvent
    public static void flightTick(TickEvent.PlayerTickEvent playerTickEvent) {
        boolean bl;
        Player player;
        if (playerTickEvent.phase != TickEvent.Phase.END || !((player = playerTickEvent.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        player = serverPlayer.getPersistentData().m_128469_(ROOT);
        ItemStack itemStack = serverPlayer.m_21205_();
        long l = FoodSocketPowers.now(serverPlayer);
        boolean bl2 = serverPlayer.m_6084_() && !serverPlayer.m_5833_() && !serverPlayer.m_20159_() && !serverPlayer.m_21255_() && !serverPlayer.m_20072_() && !serverPlayer.m_6147_() && !ManorFlyBan.active(serverPlayer) && FoodWeaponEvolution.isEvolutionCandidate(itemStack) && FoodSocketPowers.flight(itemStack);
        boolean bl3 = FoodSocketPowerRules.active(l, player.m_128454_("FlightUntil"), 60);
        if (!(!bl3 || bl2 && player.m_128403_("FlightWeapon") && itemStack.m_41782_() && itemStack.m_41783_().m_128403_(WEAPON_TOKEN) && player.m_128342_("FlightWeapon").equals(itemStack.m_41783_().m_128342_(WEAPON_TOKEN)))) {
            player.m_128473_("FlightUntil");
            player.m_128473_("FlightWeapon");
            bl3 = false;
        }
        boolean bl4 = (bl = serverPlayer.m_6144_()) && !player.m_128471_("WasSneaking");
        player.m_128379_("WasSneaking", bl);
        if (!bl3 && bl2 && !serverPlayer.m_20096_() && bl4 && FoodSocketPowerRules.ready(l, player.m_128454_("FlightCooldown"), 600)) {
            if (!itemStack.m_41784_().m_128403_(WEAPON_TOKEN)) {
                itemStack.m_41784_().m_128362_(WEAPON_TOKEN, UUID.randomUUID());
            }
            player.m_128362_("FlightWeapon", itemStack.m_41783_().m_128342_(WEAPON_TOKEN));
            player.m_128356_("FlightUntil", l + 60L);
            player.m_128356_("FlightCooldown", l + 600L);
            bl3 = true;
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.socket.flight"), true);
        }
        if (bl3) {
            Vec3 vec3 = serverPlayer.m_20184_();
            double d = bl ? Math.min(0.2, Math.max(0.12, vec3.f_82480_)) : Math.max(-0.08, vec3.f_82480_);
            serverPlayer.m_20334_(vec3.f_82479_, d, vec3.f_82481_);
            serverPlayer.f_19789_ = 0.0f;
            serverPlayer.f_19864_ = true;
        }
        if (!player.m_128456_()) {
            serverPlayer.getPersistentData().m_128365_(ROOT, (Tag)player);
        }
    }

    private static boolean validTarget(ServerPlayer serverPlayer, LivingEntity livingEntity) {
        OwnableEntity ownableEntity;
        return livingEntity instanceof Monster && livingEntity.m_6084_() && !livingEntity.m_213877_() && !livingEntity.m_7307_((Entity)serverPlayer) && !serverPlayer.m_7307_((Entity)livingEntity) && !LemonSummonRewards.suppressed((Entity)livingEntity) && (!(livingEntity instanceof OwnableEntity) || (ownableEntity = (OwnableEntity)livingEntity).m_21805_() == null) && livingEntity != serverPlayer && !(livingEntity instanceof TamableAnimal) && livingEntity.m_9236_() == serverPlayer.m_9236_() && livingEntity.m_20280_((Entity)serverPlayer) <= 576.0;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void summon(LivingHurtEvent livingHurtEvent) {
        CompoundTag compoundTag;
        ServerPlayer serverPlayer;
        block12: {
            block11: {
                Entity entity;
                if (livingHurtEvent.getAmount() <= 0.0f || !((entity = livingHurtEvent.getSource().m_7639_()) instanceof ServerPlayer)) break block11;
                serverPlayer = (ServerPlayer)entity;
                if (livingHurtEvent.getSource().m_7640_() == serverPlayer && serverPlayer.m_6084_() && !serverPlayer.m_5833_() && FoodSocketPowers.validTarget(serverPlayer, livingHurtEvent.getEntity()) && FoodSocketPowers.pack(serverPlayer.m_21205_())) break block12;
            }
            return;
        }
        long l = FoodSocketPowers.now(serverPlayer);
        if (!FoodSocketPowerRules.ready(l, (compoundTag = serverPlayer.getPersistentData().m_128469_(ROOT)).m_128454_("PackCooldown"), 400)) {
            return;
        }
        if (FoodSocketPowerRules.active(l, compoundTag.m_128454_("PackUntil"), 600)) {
            return;
        }
        ItemStack itemStack = serverPlayer.m_21205_();
        if (!itemStack.m_41784_().m_128403_(WEAPON_TOKEN)) {
            itemStack.m_41784_().m_128362_(WEAPON_TOKEN, UUID.randomUUID());
        }
        UUID uUID = UUID.randomUUID();
        compoundTag.m_128362_("PackToken", uUID);
        compoundTag.m_128362_("PackWeapon", itemStack.m_41783_().m_128342_(WEAPON_TOKEN));
        compoundTag.m_128356_("PackUntil", l + 600L);
        serverPlayer.getPersistentData().m_128365_(ROOT, (Tag)compoundTag);
        int n = 0;
        for (int i = 0; i < 2; ++i) {
            Wolf wolf = (Wolf)EntityType.f_20499_.m_20615_((Level)serverPlayer.m_284548_());
            if (wolf == null) continue;
            double d = i == 0 ? -1.0 : 1.0;
            wolf.m_7678_(serverPlayer.m_20185_() + d, serverPlayer.m_20186_(), serverPlayer.m_20189_(), serverPlayer.m_146908_(), 0.0f);
            if (!serverPlayer.m_284548_().m_45786_((Entity)wolf) || !serverPlayer.m_284548_().m_6425_(wolf.m_20183_()).m_76178_()) {
                wolf.m_146870_();
                continue;
            }
            wolf.m_7105_(true);
            wolf.m_21816_(serverPlayer.m_20148_());
            wolf.m_146762_(6000);
            wolf.m_27594_();
            wolf.m_21839_(false);
            wolf.m_21051_(Attributes.f_22281_).m_22100_(3.0);
            wolf.m_21051_(Attributes.f_22276_).m_22100_(12.0);
            wolf.m_21153_(12.0f);
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128362_("Owner", serverPlayer.m_20148_());
            compoundTag2.m_128362_("Token", uUID);
            compoundTag2.m_128356_("Expires", l + 600L);
            wolf.getPersistentData().m_128365_(HELPER, (Tag)compoundTag2);
            LemonSummonRewards.mark((Entity)wolf);
            wolf.m_6710_(livingHurtEvent.getEntity());
            if (serverPlayer.m_284548_().m_7967_((Entity)wolf)) {
                ++n;
                continue;
            }
            wolf.m_146870_();
        }
        if (n > 0) {
            compoundTag.m_128356_("PackCooldown", l + 400L);
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.socket.pack", (Object[])new Object[]{n}), true);
        } else {
            compoundTag.m_128473_("PackUntil");
            compoundTag.m_128473_("PackToken");
            compoundTag.m_128473_("PackWeapon");
        }
        serverPlayer.getPersistentData().m_128365_(ROOT, (Tag)compoundTag);
    }

    private static ServerPlayer owner(Wolf wolf) {
        ServerPlayer serverPlayer;
        Level level;
        CompoundTag compoundTag = wolf.getPersistentData().m_128469_(HELPER);
        if (!compoundTag.m_128403_("Owner") || !((level = wolf.m_9236_()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = serverLevel.m_7654_().m_6846_().m_11259_(compoundTag.m_128342_("Owner"));
        return level instanceof ServerPlayer && (serverPlayer = (ServerPlayer)level).m_9236_() == serverLevel ? serverPlayer : null;
    }

    @SubscribeEvent
    public static void helperJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        Wolf wolf;
        Entity entity;
        if (entityJoinLevelEvent.getLevel().f_46443_ || !((entity = entityJoinLevelEvent.getEntity()) instanceof Wolf) || !(wolf = (Wolf)entity).getPersistentData().m_128441_(HELPER)) {
            return;
        }
        if (entityJoinLevelEvent.loadedFromDisk()) {
            wolf.m_146870_();
            entityJoinLevelEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void helperTick(LivingEvent.LivingTickEvent livingTickEvent) {
        Wolf wolf;
        LivingEntity livingEntity;
        block7: {
            block6: {
                livingEntity = livingTickEvent.getEntity();
                if (!(livingEntity instanceof Wolf)) break block6;
                wolf = (Wolf)livingEntity;
                if (!wolf.m_9236_().f_46443_ && wolf.getPersistentData().m_128441_(HELPER)) break block7;
            }
            return;
        }
        livingEntity = FoodSocketPowers.owner(wolf);
        CompoundTag compoundTag = wolf.getPersistentData().m_128469_(HELPER);
        if (livingEntity == null || !livingEntity.m_6084_() || livingEntity.m_5833_() || !compoundTag.m_128403_("Token")) {
            wolf.m_146870_();
            return;
        }
        CompoundTag compoundTag2 = livingEntity.getPersistentData().m_128469_(ROOT);
        if (!(compoundTag2.m_128403_("PackToken") && compoundTag.m_128342_("Token").equals(compoundTag2.m_128342_("PackToken")) && FoodSocketPowerRules.active(FoodSocketPowers.now((ServerPlayer)livingEntity), compoundTag.m_128454_("Expires"), 600) && !(wolf.m_20280_((Entity)livingEntity) > 1024.0) && FoodSocketPowers.pack(livingEntity.m_21205_()) && compoundTag2.m_128403_("PackWeapon") && livingEntity.m_21205_().m_41782_() && livingEntity.m_21205_().m_41783_().m_128403_(WEAPON_TOKEN) && compoundTag2.m_128342_("PackWeapon").equals(livingEntity.m_21205_().m_41783_().m_128342_(WEAPON_TOKEN)))) {
            wolf.m_146870_();
            return;
        }
        wolf.m_146762_(6000);
        wolf.m_27594_();
        wolf.m_21839_(false);
        if (wolf.m_5448_() != null && !FoodSocketPowers.validTarget((ServerPlayer)livingEntity, wolf.m_5448_())) {
            wolf.m_6710_(null);
        }
    }

    private static boolean temporary(Entity entity) {
        return entity instanceof Wolf && entity.getPersistentData().m_128441_(HELPER);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void helperInteract(PlayerInteractEvent.EntityInteract entityInteract) {
        if (!FoodSocketPowers.temporary(entityInteract.getTarget())) {
            return;
        }
        entityInteract.setCanceled(true);
        entityInteract.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void helperInteractSpecific(PlayerInteractEvent.EntityInteractSpecific entityInteractSpecific) {
        if (!FoodSocketPowers.temporary(entityInteractSpecific.getTarget())) {
            return;
        }
        entityInteractSpecific.setCanceled(true);
        entityInteractSpecific.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void helperBreed(BabyEntitySpawnEvent babyEntitySpawnEvent) {
        if (!FoodSocketPowers.temporary((Entity)babyEntitySpawnEvent.getParentA()) && !FoodSocketPowers.temporary((Entity)babyEntitySpawnEvent.getParentB())) {
            return;
        }
        babyEntitySpawnEvent.setCanceled(true);
        if (babyEntitySpawnEvent.getChild() != null) {
            babyEntitySpawnEvent.getChild().m_146870_();
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void target(LivingChangeTargetEvent livingChangeTargetEvent) {
        Wolf wolf;
        LivingEntity livingEntity = livingChangeTargetEvent.getEntity();
        if (!(livingEntity instanceof Wolf) || !(wolf = (Wolf)livingEntity).getPersistentData().m_128441_(HELPER)) {
            return;
        }
        livingEntity = FoodSocketPowers.owner(wolf);
        if (livingEntity == null || livingChangeTargetEvent.getNewTarget() != null && !FoodSocketPowers.validTarget((ServerPlayer)livingEntity, livingChangeTargetEvent.getNewTarget())) {
            livingChangeTargetEvent.setNewTarget(null);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void safeDamage(LivingHurtEvent livingHurtEvent) {
        Wolf wolf;
        Entity entity = livingHurtEvent.getSource().m_7639_();
        if (!(entity instanceof Wolf) || !(wolf = (Wolf)entity).getPersistentData().m_128441_(HELPER)) {
            return;
        }
        entity = FoodSocketPowers.owner(wolf);
        if (entity == null || !FoodSocketPowers.validTarget((ServerPlayer)entity, livingHurtEvent.getEntity())) {
            livingHurtEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent livingDropsEvent) {
        if (livingDropsEvent.getEntity().getPersistentData().m_128441_(HELPER)) {
            livingDropsEvent.getDrops().clear();
            livingDropsEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void experience(LivingExperienceDropEvent livingExperienceDropEvent) {
        if (livingExperienceDropEvent.getEntity().getPersistentData().m_128441_(HELPER)) {
            livingExperienceDropEvent.setDroppedExperience(0);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone clone) {
        CompoundTag compoundTag = clone.getOriginal().getPersistentData().m_128469_(ROOT);
        CompoundTag compoundTag2 = new CompoundTag();
        if (compoundTag.m_128425_("FlightCooldown", 4)) {
            compoundTag2.m_128356_("FlightCooldown", compoundTag.m_128454_("FlightCooldown"));
        }
        if (compoundTag.m_128425_("PackCooldown", 4)) {
            compoundTag2.m_128356_("PackCooldown", compoundTag.m_128454_("PackCooldown"));
        }
        clone.getEntity().getPersistentData().m_128365_(ROOT, (Tag)compoundTag2);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        FoodSocketPowers.endAbilities(playerLoggedOutEvent.getEntity());
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent playerChangedDimensionEvent) {
        FoodSocketPowers.endAbilities(playerChangedDimensionEvent.getEntity());
    }

    private static void endAbilities(Player player) {
        CompoundTag compoundTag = player.getPersistentData().m_128469_(ROOT);
        for (String string : List.of("FlightUntil", "FlightWeapon", "PackUntil", "PackToken", "PackWeapon")) {
            compoundTag.m_128473_(string);
        }
        player.getPersistentData().m_128365_(ROOT, (Tag)compoundTag);
    }

    public static void applyPack(PackCommit packCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (packCommit == null || packCommit.executed || Thread.currentThread() != packCommit.thread || packCommit.server != packCommit.plan.context.player().m_284548_().m_7654_() || !packCommit.server.m_18695_()) {
            return;
        }
        FoodWeaponConsumerSequence.Accepted accepted = packCommit.prepared.originalSequence;
        if (accepted != null && !accepted.enter(packCommit, childHurtBridge)) {
            return;
        }
        Throwable throwable = null;
        try {
            packCommit.executed = true;
            if (!FoodSocketPowers.publishedPack(packCommit)) {
                return;
            }
            CompoundTag compoundTag = packCommit.publishedController;
            if (!((accepted == null ? FoodWeaponBondHits.matchesContext(packCommit.plan.context) : accepted.ordinaryContext()) && FoodSocketPowers.matchesPackReceipt(packCommit.prepared, packCommit.amount, packCommit.receipt) && compoundTag.m_128403_("PackToken") && compoundTag.m_128342_("PackToken").equals(packCommit.token))) {
                FoodSocketPowers.restorePack(packCommit, childHurtBridge);
                return;
            }
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                return;
            }
            int n = FoodSocketPowers.spawnPack(packCommit, childHurtBridge);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                return;
            }
            if (n > 0) {
                packCommit.plan.context.player().m_5661_((Component)Component.m_237110_((String)"message.ftboss.socket.pack", (Object[])new Object[]{n}), true);
                if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                    return;
                }
            } else {
                FoodSocketPowers.restorePack(packCommit, childHurtBridge);
            }
        }
        catch (Error | RuntimeException throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.finish(packCommit, childHurtBridge, throwable);
            }
        }
    }

    static boolean bindOriginalSequence(PreparedPack preparedPack, FoodWeaponOriginalSequence.Session session, Object object, int n, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        if (preparedPack == null || preparedPack.publishAttempted || preparedPack.originalSequence != null || !FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, offenseReceipt, publishingCleanup) || !FoodSocketPowers.matchesPackReceipt(preparedPack, f, offenseReceipt)) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = preparedPack.binding;
        FoodWeaponConsumerSequence.Accepted accepted = FoodWeaponConsumerSequence.bind(session, object, n, preparedPack, preparedPack.plan, offenseReceipt, publishingCleanup, completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets, preparedPack.plan.context.player(), preparedPack.plan.context.target(), preparedPack.plan.context.source(), preparedPack.plan.context.actualWeapon(), preparedPack.plan.context.originalHand(), preparedPack.plan.context.quotedAt(), f, "WolfProducer");
        if (accepted == null) {
            return false;
        }
        preparedPack.originalSequence = accepted;
        return true;
    }

    static PackCommit commitPack(PreparedPack preparedPack, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedPack == null ? null : preparedPack.originalSequence;
        int n = accepted == null ? 0 : accepted.publicationCallEntered(preparedPack);
        Throwable throwable = null;
        try {
            if (preparedPack == null || preparedPack.publishAttempted || preparedPack.publishingCleanup != null || !FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, offenseReceipt, publishingCleanup)) {
                PackCommit packCommit = null;
                return packCommit;
            }
            preparedPack.publishingCleanup = publishingCleanup;
            PackCommit packCommit = FoodSocketPowers.commitPack(preparedPack, f, offenseReceipt);
            return packCommit;
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            if (accepted != null) {
                accepted.publicationExited(preparedPack, n, throwable);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void finishPackPublication(PreparedPack preparedPack, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedPack == null ? null : preparedPack.originalSequence;
        Throwable throwable = null;
        try {
            if (!(FoodSocketPowers.preparedCleanupCaller() && preparedPack != null && preparedPack.publishingCleanup == publishingCleanup && (preparedPack.originalSequence != null && preparedPack.originalSequence.cleanup(preparedPack, publishingCleanup) || FoodWeaponFinalDamage.matchesPackPublicationCleanup(preparedPack, publishingCleanup)) && preparedPack.publishAttempted && preparedPack.publishExited && preparedPack.publicationReceipt != null)) {
                return;
            }
            CompleteBatchBinding completeBatchBinding = preparedPack.binding;
            if (!FoodSocketPowers.matchesPreparedPack(preparedPack, preparedPack.plan, completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets)) {
                return;
            }
            preparedPack.finalizingCleanup = publishingCleanup;
            Throwable throwable2 = null;
            try {
                FoodSocketPowers.retirePackPublication(preparedPack, FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch));
            }
            catch (Error | RuntimeException throwable3) {
                throwable2 = throwable3;
                throw throwable3;
            }
            finally {
                try {
                    FoodSocketPowers.finishPackOwnerController(preparedPack, publishingCleanup, throwable2);
                }
                finally {
                    if (preparedPack.originalSequence != null) {
                        preparedPack.originalSequence.retired(preparedPack);
                    }
                }
            }
        }
        catch (Throwable throwable4) {
            throwable = throwable4;
            throw throwable4;
        }
        finally {
            if (accepted != null) {
                accepted.publicationCleanupExited(preparedPack, publishingCleanup, throwable);
            }
        }
    }

    static boolean matchesOwnerPackParameters(PreparedPack preparedPack, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, CompoundTag compoundTag, CompoundTag compoundTag2, UUID uUID, UUID uUID2, CompoundTag compoundTag3, CompoundTag compoundTag4, long l) {
        if (preparedPack == null || preparedPack.ownerParameters == null || preparedPack.publishingCleanup != publishingCleanup || !preparedPack.publishAttempted || preparedPack.publishExited || preparedPack.publication != null || preparedPack.publicationReceipt != offenseReceipt || !preparedPack.ownerControllerBeginEntered || preparedPack.ownerControllerBeginCompleted || !Thread.holdsLock(FoodWeaponSourceLedger.class) || !FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, offenseReceipt, publishingCleanup)) {
            return false;
        }
        PackOwnerParameters packOwnerParameters = preparedPack.ownerParameters;
        return compoundTag != null && compoundTag == packOwnerParameters.persistent && compoundTag2 != null && compoundTag2 == packOwnerParameters.data && uUID != null && uUID == packOwnerParameters.weaponToken && uUID2 != null && uUID2 == packOwnerParameters.packToken && compoundTag3 != null && compoundTag3 == packOwnerParameters.fingerprint && compoundTag3 == FoodWeaponBondHits.originalContextFingerprint(preparedPack.plan.context) && compoundTag4 != null && compoundTag4.equals((Object)packOwnerParameters.snapshot) && l == packOwnerParameters.tick && l == preparedPack.plan.quotedAt() && FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, offenseReceipt, publishingCleanup);
    }

    static boolean matchesPackCommit(PackCommit packCommit, PreparedPack preparedPack, PackPlan packPlan, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return packCommit != null && preparedPack != null && packCommit.prepared == preparedPack && packCommit.plan == packPlan && packPlan != null && packPlan.committed && offenseReceipt != null && packCommit.receipt == offenseReceipt && Float.isFinite(f) && f > 0.0f && Float.floatToRawIntBits(packCommit.amount) == Float.floatToRawIntBits(f) && FoodSocketPowers.matchesPreparedPack(preparedPack, packPlan, attemptBatchQuote, attemptBatchAdmission, list, list2);
    }

    static boolean matchesPreparedPack(PreparedPack preparedPack, PackPlan packPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return preparedPack != null && packPlan != null && preparedPack.plan == packPlan && attemptBatchQuote != null && attemptBatchAdmission != null && list != null && list2 != null && preparedPack.binding.batch == attemptBatchQuote && preparedPack.binding.admission == attemptBatchAdmission && preparedPack.binding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote) && preparedPack.binding.originalBinding != null && preparedPack.binding.requests.equals(list) && preparedPack.binding.targets.equals(list2);
    }

    private static PackPlayerCall[] newPackPlayerCalls() {
        PackPlayerCall[] packPlayerCallArray = new PackPlayerCall[10];
        for (int i = 0; i < packPlayerCallArray.length; ++i) {
            packPlayerCallArray[i] = new PackPlayerCall();
        }
        return packPlayerCallArray;
    }

    public static PreparedPack preparePack(PackPlan packPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        if (!FoodSocketPowers.matchesPackBatch(packPlan, attemptBatchQuote, attemptBatchAdmission, list, list2)) {
            return null;
        }
        CompleteBatchBinding completeBatchBinding = new CompleteBatchBinding(attemptBatchQuote, attemptBatchAdmission, list, list2);
        return completeBatchBinding.originalBinding == null ? null : new PreparedPack(packPlan, completeBatchBinding);
    }

    private static boolean publishedPack(PackCommit packCommit) {
        if (packCommit.plan.context.player().getPersistentData().m_128423_(ROOT) != packCommit.publishedController) {
            return false;
        }
        for (String string : List.of("PackToken", "PackWeapon", "PackWeaponId", "PackHand", "PackDimension", "PackFingerprint", "PackSource", "PackUntil", "PackCooldown")) {
            if (Objects.equals(packCommit.publishedController.m_128423_(string), packCommit.publishedState.m_128423_(string))) continue;
            return false;
        }
        return true;
    }

    private static boolean matchesPackReceipt(PreparedPack preparedPack, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        if (preparedPack != null && preparedPack.originalSequence != null) {
            return preparedPack.originalSequence.matchesReceipt(preparedPack, offenseReceipt, f);
        }
        if (preparedPack == null || offenseReceipt == null || !Float.isFinite(f) || f <= 0.0f) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = preparedPack.binding;
        FoodWeaponBondHits.Context context = preparedPack.plan.context;
        return offenseReceipt.matchesBatch(completeBatchBinding.batch, completeBatchBinding.admission, completeBatchBinding.requests, completeBatchBinding.targets) && offenseReceipt.matches(preparedPack.plan, context.player(), context.source(), context.quotedAt(), f) && offenseReceipt.attempt(FoodWeaponSourceLedger.AttemptDomain.WOLF_PACK).filter(attemptResult -> attemptResult.domain() == FoodWeaponSourceLedger.AttemptDomain.WOLF_PACK && attemptResult.commit() == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS).isPresent();
    }

    private static void restorePack(PackCommit packCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (!FoodSocketPowers.ownsPackCleanup(packCommit, childHurtBridge)) {
            return;
        }
        packCommit.cleanupCompleted = true;
        FoodSocketPowers.retirePackPublication(packCommit.prepared, FoodWeaponSourceLedger.originalInteractionBinding(packCommit.prepared.binding.batch));
    }

    private static boolean packLeafLive(PackCommit packCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (packCommit != null && packCommit.prepared.originalSequence != null) {
            return packCommit.executed && FoodSocketPowers.publishedPack(packCommit) && packCommit.prepared.originalSequence.continuing(packCommit, childHurtBridge);
        }
        return packCommit != null && packCommit.executed && FoodSocketPowers.publishedPack(packCommit) && FoodSocketPowers.matchesPackReceipt(packCommit.prepared, packCommit.amount, packCommit.receipt) && FoodWeaponFinalDamage.matchesPackInvocation(packCommit, childHurtBridge);
    }

    private static int spawnPack(PackCommit packCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        PackPlan packPlan = packCommit.plan;
        ServerPlayer serverPlayer = packPlan.context.player();
        UUID uUID = packCommit.token;
        int n = 0;
        for (int i = 0; i < 2; ++i) {
            boolean bl;
            double d;
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                return n;
            }
            Wolf wolf = (Wolf)EntityType.f_20499_.m_20615_((Level)serverPlayer.m_284548_());
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                if (wolf != null) {
                    wolf.m_146870_();
                }
                return n;
            }
            if (wolf == null) continue;
            double d2 = d = i == 0 ? -1.0 : 1.0;
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_7678_(serverPlayer.m_20185_() + d, serverPlayer.m_20186_(), serverPlayer.m_20189_(), serverPlayer.m_146908_(), 0.0f);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            boolean bl2 = bl = serverPlayer.m_284548_().m_45786_((Entity)wolf) && serverPlayer.m_284548_().m_6425_(wolf.m_20183_()).m_76178_();
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            if (!bl) {
                wolf.m_146870_();
                if (FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) continue;
                return n;
            }
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_7105_(true);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_21816_(serverPlayer.m_20148_());
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_146762_(-24000);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_27594_();
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_21839_(false);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_21051_(Attributes.f_22281_).m_22100_(3.0);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_21051_(Attributes.f_22276_).m_22100_(12.0);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_21153_(12.0f);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128362_("Owner", serverPlayer.m_20148_());
            compoundTag.m_128359_("Origin", "ftboss:food_socket_pack");
            compoundTag.m_128379_("PermanentBaby", true);
            compoundTag.m_128362_("Token", uUID);
            compoundTag.m_128356_("Expires", packPlan.quotedAt() + 600L);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.getPersistentData().m_128365_(HELPER, (Tag)compoundTag);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            LemonSummonRewards.mark((Entity)wolf);
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            wolf.m_6710_(packPlan.context.target());
            if (!FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) {
                wolf.m_146870_();
                return n;
            }
            if (serverPlayer.m_284548_().m_7967_((Entity)wolf)) {
                ++n;
            } else {
                wolf.m_146870_();
            }
            if (FoodSocketPowers.packLeafLive(packCommit, childHurtBridge)) continue;
            return n;
        }
        return n;
    }

    public static PackCommit commitPack(PreparedPack preparedPack, float f, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        FoodWeaponConsumerSequence.Accepted accepted = preparedPack == null ? null : preparedPack.originalSequence;
        boolean bl = false;
        int n = accepted == null ? 0 : accepted.publicationCallEntered(preparedPack);
        Throwable throwable = null;
        try {
            Object object;
            CompoundTag compoundTag;
            PackPublication packPublication;
            if (preparedPack == null || preparedPack.publishAttempted || preparedPack.publishingCleanup != null && !FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, offenseReceipt, preparedPack.publishingCleanup)) {
                PackCommit packCommit = null;
                return packCommit;
            }
            PackPlan packPlan = preparedPack.plan;
            if (!FoodSocketPowers.packContext(packPlan) || !FoodSocketPowers.matchesPackReceipt(preparedPack, f, offenseReceipt)) {
                PackCommit packCommit = null;
                return packCommit;
            }
            preparedPack.publishAttempted = true;
            preparedPack.publicationReceipt = offenseReceipt;
            preparedPack.publicationAmountBits = Float.floatToRawIntBits(f);
            bl = true;
            ServerPlayer serverPlayer = packPlan.context.player();
            ItemStack itemStack = packPlan.context.actualWeapon();
            CompoundTag compoundTag2 = serverPlayer.getPersistentData();
            CompoundTag compoundTag3 = compoundTag2.m_128469_(ROOT).m_6426_();
            UUID uUID = itemStack.m_41783_() != null && itemStack.m_41783_().m_128403_(WEAPON_TOKEN) ? itemStack.m_41783_().m_128342_(WEAPON_TOKEN) : UUID.randomUUID();
            UUID uUID2 = UUID.randomUUID();
            CompoundTag compoundTag4 = packPlan.snapshot();
            FoodSocketPowers.beginPackOwnerController(preparedPack, offenseReceipt, compoundTag2, compoundTag3, uUID, uUID2, compoundTag4);
            FoodSocketPowers.putPackPlayerUuid(preparedPack, 0, compoundTag3, "PackToken", uUID2);
            FoodSocketPowers.putPackPlayerUuid(preparedPack, 1, compoundTag3, "PackWeapon", uUID);
            FoodSocketPowers.putPackPlayerUuid(preparedPack, 2, compoundTag3, "PackWeaponId", compoundTag4.m_128342_("WeaponId"));
            FoodSocketPowers.putPackPlayerString(preparedPack, 3, compoundTag3, "PackHand", compoundTag4.m_128461_("OriginalHand"));
            FoodSocketPowers.putPackPlayerString(preparedPack, 4, compoundTag3, "PackDimension", compoundTag4.m_128461_("Dimension"));
            FoodSocketPowers.putPackPlayerTag(preparedPack, 5, compoundTag3, "PackFingerprint", (Tag)FoodWeaponBondHits.fingerprint(itemStack));
            FoodSocketPowers.putPackPlayerTag(preparedPack, 6, compoundTag3, "PackSource", (Tag)compoundTag4.m_6426_());
            FoodSocketPowers.putPackPlayerLong(preparedPack, 7, compoundTag3, "PackUntil", packPlan.quotedAt() + 600L);
            FoodSocketPowers.putPackPlayerLong(preparedPack, 8, compoundTag3, "PackCooldown", packPlan.quotedAt() + 400L);
            packPlan.committed = true;
            preparedPack.publication = packPublication = new PackPublication(preparedPack, compoundTag2, compoundTag3);
            FoodSocketPowers.planPackOwnerToken(preparedPack, uUID);
            try {
                packPublication.weaponWriteEntered = true;
                packPublication.weaponTagLookupEntered = true;
                packPublication.weaponTagReceiver = compoundTag = itemStack.m_41784_();
                packPublication.weaponTagLookupCompleted = true;
                FoodSocketPowers.enterPackOwnerUuid(preparedPack, itemStack, compoundTag, uUID);
                packPublication.weaponTokenWriteEntered = true;
                compoundTag.m_128362_(WEAPON_TOKEN, uUID);
                packPublication.weaponWriteCompleted = true;
                FoodSocketPowers.returnPackOwnerUuid(preparedPack, itemStack, compoundTag, uUID);
            }
            catch (Throwable throwable2) {
                FoodSocketPowers.recordPackOriginalFailure(preparedPack, throwable2);
                throw throwable2;
            }
            finally {
                FoodSocketPowers.closePackOwnerUuid(preparedPack);
            }
            packPublication.persistentLookupEntered = true;
            packPublication.persistent = compoundTag = serverPlayer.getPersistentData();
            packPublication.persistentLookupCompleted = true;
            packPublication.preimageReadEntered = true;
            packPublication.previousRoot = compoundTag.m_128423_(ROOT);
            for (String object22 : packPublication.keys) {
                Tag tag;
                Tag tag2;
                Tag tag3 = packPublication.previousRoot;
                if (tag3 instanceof CompoundTag) {
                    CompoundTag compoundTag5 = (CompoundTag)tag3;
                    tag2 = compoundTag5.m_128423_(object22);
                } else {
                    tag2 = null;
                }
                if ((tag = tag2) == null) continue;
                packPublication.previous.m_128365_(object22, tag.m_6426_());
            }
            packPublication.preimageReadCompleted = true;
            packPublication.rootWriteEntered = true;
            FoodSocketPowers.putPackPlayerRoot(preparedPack, packPublication, compoundTag, compoundTag3);
            if (!FoodSocketPowers.readBackPackPublication(packPublication)) {
                object = null;
                return object;
            }
            object = new PackCommit(preparedPack, offenseReceipt, f, uUID2, compoundTag3);
            preparedPack.commitConstructed = true;
            if (preparedPack.originalSequence != null) {
                preparedPack.originalSequence.published(preparedPack, object);
            }
            Object object2 = object;
            return object2;
        }
        catch (Throwable throwable3) {
            throwable = throwable3;
            if (bl) {
                preparedPack.publishFailure = throwable3;
                if (preparedPack.originalSequence != null) {
                    preparedPack.originalSequence.originalThrown(throwable3);
                }
                FoodSocketPowers.recordPackOriginalFailure(preparedPack, throwable3);
            }
            throw throwable3;
        }
        finally {
            if (bl) {
                preparedPack.publishExited = true;
            }
            if (accepted != null) {
                accepted.publicationExited(preparedPack, n, throwable);
            }
        }
    }

    private static boolean preparedCleanupCaller() {
        return Thread.holdsLock(FoodWeaponSourceLedger.class) && StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodSocketPowers.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponFinalDamage.class && stackFrame.getMethodName().equals("finalizeFailedPublication") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)V")).orElse(false)) != false;
    }

    private static void retirePackPublication(PreparedPack preparedPack, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding) {
        PackPublication packPublication = preparedPack.publication;
        if (packPublication == null || packPublication.cleanupStarted) {
            return;
        }
        packPublication.cleanupStarted = true;
        try {
            if (!FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(preparedPack.binding.originalBinding, originalInteractionBinding, packPublication.player)) {
                packPublication.displaced = true;
                return;
            }
            if (!packPublication.rootWriteEntered) {
                packPublication.cleanupCompleted = true;
                return;
            }
            for (int i = 0; i < packPublication.keys.size(); ++i) {
                Tag tag;
                if (!FoodSocketPowers.ownsPackPublication(preparedPack, originalInteractionBinding)) {
                    packPublication.displaced = true;
                    return;
                }
                String string = packPublication.keys.get(i);
                Tag tag2 = packPublication.previous.m_128423_(string);
                Tag tag3 = tag = tag2 == null ? null : tag2.m_6426_();
                if (!FoodSocketPowers.ownsPackPublication(preparedPack, originalInteractionBinding)) {
                    packPublication.displaced = true;
                    return;
                }
                packPublication.restoreProgress[i] = 1;
                if (tag == null) {
                    packPublication.target.m_128473_(string);
                } else {
                    packPublication.target.m_128365_(string, tag);
                }
                packPublication.restoreProgress[i] = 2;
            }
            packPublication.cleanupCompleted = FoodSocketPowers.ownsPackPublication(preparedPack, originalInteractionBinding);
            packPublication.displaced = !packPublication.cleanupCompleted;
        }
        catch (Error | RuntimeException throwable) {
            packPublication.cleanupFailure = throwable;
            throw throwable;
        }
    }

    private static void finishPackOwnerController(PreparedPack preparedPack, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup, Throwable throwable) {
        if (preparedPack.ownerController == null || preparedPack.ownerControllerFinishEntered) {
            return;
        }
        preparedPack.ownerControllerFinishEntered = true;
        try {
            FoodWeaponSourceLedger.finishOwnerPackWriter(preparedPack.ownerController, preparedPack, publishingCleanup);
            preparedPack.ownerControllerFinishCompleted = true;
        }
        catch (Error | RuntimeException throwable2) {
            preparedPack.ownerControllerFinishFailure = throwable2;
            FoodSocketPowers.recordPackObserverFailure(preparedPack, 3, throwable2);
        }
    }

    public static boolean matchesPackBatch(PackPlan packPlan, FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
        return FoodSocketPowers.packContext(packPlan) && FoodWeaponBondHits.matchesBatch(packPlan.context, FoodWeaponSourceLedger.EffectDomain.WOLF_PACK, attemptBatchQuote, attemptBatchAdmission, list, list2);
    }

    private static boolean ownsPackCleanup(PackCommit packCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (packCommit != null && packCommit.prepared.originalSequence != null) {
            return packCommit.executed && !packCommit.cleanupCompleted && packCommit.prepared.plan == packCommit.plan && packCommit.plan.committed && FoodSocketPowers.publishedPack(packCommit) && packCommit.prepared.originalSequence.continuing(packCommit, childHurtBridge);
        }
        if (!(packCommit != null && packCommit.executed && !packCommit.cleanupCompleted && packCommit.prepared.plan == packCommit.plan && packCommit.plan.committed && FoodSocketPowers.publishedPack(packCommit) && FoodWeaponFinalDamage.matchesPackCleanupInvocation(packCommit, childHurtBridge))) {
            return false;
        }
        CompleteBatchBinding completeBatchBinding = packCommit.prepared.binding;
        return completeBatchBinding.originalBinding != null && completeBatchBinding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch) && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(completeBatchBinding.originalBinding, FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch), packCommit.plan.context.player());
    }

    private static boolean packContext(PackPlan packPlan) {
        if (!(packPlan != null && !packPlan.committed && FoodWeaponBondHits.matchesContext(packPlan.context) && FoodSocketPowers.packSourceContext(packPlan.context) && FoodSocketPowers.validTarget(packPlan.context.player(), packPlan.context.target()) && FoodSocketPowers.pack(packPlan.context.actualWeapon()) && Objects.equals(packPlan.controller, packPlan.context.player().getPersistentData().m_128423_(ROOT)) && Objects.equals(packPlan.weaponTag, packPlan.context.actualWeapon().m_41783_()))) {
            return false;
        }
        CompoundTag compoundTag = packPlan.context.player().getPersistentData().m_128469_(ROOT);
        return FoodSocketPowerRules.ready(packPlan.quotedAt(), compoundTag.m_128454_("PackCooldown"), 400) && !FoodSocketPowerRules.active(packPlan.quotedAt(), compoundTag.m_128454_("PackUntil"), 600);
    }

    private static void beginPackOwnerController(PreparedPack preparedPack, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, CompoundTag compoundTag, CompoundTag compoundTag2, UUID uUID, UUID uUID2, CompoundTag compoundTag3) {
        if (preparedPack.publishingCleanup == null) {
            return;
        }
        try {
            FoodWeaponSourceLedger.OwnerWriterPublication ownerWriterPublication;
            CompoundTag compoundTag4 = FoodWeaponBondHits.originalContextFingerprint(preparedPack.plan.context);
            preparedPack.ownerParameters = new PackOwnerParameters(compoundTag, compoundTag2, uUID, uUID2, compoundTag4, compoundTag3, preparedPack.plan.quotedAt());
            preparedPack.ownerPublicationLookupEntered = true;
            preparedPack.ownerPublication = ownerWriterPublication = FoodWeaponFinalDamage.packPublishingWriter(preparedPack, offenseReceipt, preparedPack.publishingCleanup);
            preparedPack.ownerPublicationLookupCompleted = true;
            if (ownerWriterPublication == null) {
                return;
            }
            preparedPack.ownerControllerBeginEntered = true;
            preparedPack.ownerController = FoodWeaponSourceLedger.beginOwnerPackController(ownerWriterPublication, preparedPack, preparedPack.publishingCleanup, compoundTag, compoundTag2, uUID, uUID2, compoundTag4);
            preparedPack.ownerControllerBeginCompleted = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackObserverFailure(preparedPack, 0, throwable);
        }
    }

    private static void putPackPlayerUuid(PreparedPack preparedPack, int n, CompoundTag compoundTag, String string, UUID uUID) {
        PackPlayerCall packPlayerCall = FoodSocketPowers.beginPackPlayerCall(preparedPack, n, compoundTag, string);
        packPlayerCall.argument = uUID;
        FoodSocketPowers.planPackPlayerUuid(packPlayerCall, n, uUID);
        try {
            FoodSocketPowers.enterPackPlayerUuid(packPlayerCall, uUID);
            packPlayerCall.originalEntered = true;
            compoundTag.m_128362_(string, uUID);
            packPlayerCall.originalReturned = true;
            FoodSocketPowers.returnPackPlayerTag(packPlayerCall);
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerOriginalFailure(packPlayerCall, throwable);
            throw throwable;
        }
        finally {
            FoodSocketPowers.closePackPlayerCall(packPlayerCall);
        }
    }

    private static void putPackPlayerString(PreparedPack preparedPack, int n, CompoundTag compoundTag, String string, String string2) {
        PackPlayerCall packPlayerCall = FoodSocketPowers.beginPackPlayerCall(preparedPack, n, compoundTag, string);
        packPlayerCall.argument = string2;
        FoodSocketPowers.planPackPlayerString(packPlayerCall, n, string2);
        try {
            FoodSocketPowers.enterPackPlayerString(packPlayerCall, string2);
            packPlayerCall.originalEntered = true;
            compoundTag.m_128359_(string, string2);
            packPlayerCall.originalReturned = true;
            FoodSocketPowers.returnPackPlayerTag(packPlayerCall);
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerOriginalFailure(packPlayerCall, throwable);
            throw throwable;
        }
        finally {
            FoodSocketPowers.closePackPlayerCall(packPlayerCall);
        }
    }

    private static Tag putPackPlayerTag(PreparedPack preparedPack, int n, CompoundTag compoundTag, String string, Tag tag) {
        PackPlayerCall packPlayerCall = FoodSocketPowers.beginPackPlayerCall(preparedPack, n, compoundTag, string);
        packPlayerCall.argument = tag;
        FoodSocketPowers.planPackPlayerTag(packPlayerCall, n, tag);
        try {
            Tag tag2;
            FoodSocketPowers.enterPackPlayerTag(packPlayerCall, tag);
            packPlayerCall.originalEntered = true;
            packPlayerCall.returnedValue = tag2 = compoundTag.m_128365_(string, tag);
            packPlayerCall.originalReturned = true;
            FoodSocketPowers.returnPackPlayerTag(packPlayerCall);
            Tag tag3 = tag2;
            return tag3;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerOriginalFailure(packPlayerCall, throwable);
            throw throwable;
        }
        finally {
            FoodSocketPowers.closePackPlayerCall(packPlayerCall);
        }
    }

    private static void putPackPlayerLong(PreparedPack preparedPack, int n, CompoundTag compoundTag, String string, long l) {
        PackPlayerCall packPlayerCall = FoodSocketPowers.beginPackPlayerCall(preparedPack, n, compoundTag, string);
        packPlayerCall.longArgument = l;
        FoodSocketPowers.planPackPlayerLong(packPlayerCall, n, l);
        try {
            FoodSocketPowers.enterPackPlayerLong(packPlayerCall, l);
            packPlayerCall.originalEntered = true;
            compoundTag.m_128356_(string, l);
            packPlayerCall.originalReturned = true;
            FoodSocketPowers.returnPackPlayerTag(packPlayerCall);
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerOriginalFailure(packPlayerCall, throwable);
            throw throwable;
        }
        finally {
            FoodSocketPowers.closePackPlayerCall(packPlayerCall);
        }
    }

    private static void planPackOwnerToken(PreparedPack preparedPack, UUID uUID) {
        if (preparedPack.ownerPublication == null) {
            return;
        }
        preparedPack.ownerTokenPlanEntered = true;
        try {
            preparedPack.ownerTokenStep = FoodWeaponSourceLedger.planOwnerWolfTokenWriter(preparedPack.ownerPublication, preparedPack, preparedPack, 0, uUID);
            preparedPack.ownerTokenPlanReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackObserverFailure(preparedPack, 0, throwable);
        }
    }

    private static void enterPackOwnerUuid(PreparedPack preparedPack, ItemStack itemStack, CompoundTag compoundTag, UUID uUID) {
        if (preparedPack.ownerTokenStep == null) {
            return;
        }
        preparedPack.ownerUuidEntryEntered = true;
        try {
            FoodWeaponSourceLedger.ownerWriterUuidEntered(preparedPack.ownerTokenStep, itemStack, compoundTag, WEAPON_TOKEN, uUID);
            preparedPack.ownerUuidEntryReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackObserverFailure(preparedPack, 0, throwable);
        }
    }

    private static void returnPackOwnerUuid(PreparedPack preparedPack, ItemStack itemStack, CompoundTag compoundTag, UUID uUID) {
        if (preparedPack.ownerTokenStep == null) {
            return;
        }
        preparedPack.ownerUuidReturnEntered = true;
        try {
            FoodWeaponSourceLedger.ownerWriterUuidReturned(preparedPack.ownerTokenStep, itemStack, compoundTag, WEAPON_TOKEN, uUID);
            preparedPack.ownerUuidReturnReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackObserverFailure(preparedPack, 1, throwable);
        }
    }

    private static void closePackOwnerUuid(PreparedPack preparedPack) {
        if (preparedPack.ownerTokenStep == null || preparedPack.ownerUuidCloseEntered) {
            return;
        }
        preparedPack.ownerUuidCloseEntered = true;
        try {
            FoodWeaponSourceLedger.closeOwnerWriterUuidStep(preparedPack.ownerTokenStep);
            preparedPack.ownerUuidCloseReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackObserverFailure(preparedPack, 3, throwable);
        }
    }

    private static void recordPackOriginalFailure(PreparedPack preparedPack, Throwable throwable) {
        if (preparedPack.originalSequence != null) {
            preparedPack.originalSequence.originalThrown(throwable);
        }
        if (preparedPack.ownerWriterFailures.original != null) {
            return;
        }
        preparedPack.ownerWriterFailures.original(throwable);
        try {
            if (preparedPack.ownerTokenStep != null) {
                FoodWeaponSourceLedger.ownerWriterOriginalFailed(preparedPack.ownerTokenStep, throwable);
            } else if (preparedPack.ownerController != null) {
                FoodWeaponSourceLedger.ownerPlayerWriterOriginalFailed(preparedPack.ownerController, throwable);
            } else if (preparedPack.ownerPublication != null) {
                FoodWeaponSourceLedger.ownerPublicationOriginalFailed(preparedPack.ownerPublication, throwable);
            }
        }
        catch (Throwable throwable2) {
            preparedPack.ownerWriterFailures.diagnostic(2, throwable2);
            FoodSocketPowers.recordPackObserverFailure(preparedPack, 2, throwable2);
        }
    }

    private static Tag putPackPlayerRoot(PreparedPack preparedPack, PackPublication packPublication, CompoundTag compoundTag, CompoundTag compoundTag2) {
        PackPlayerCall packPlayerCall = FoodSocketPowers.beginPackPlayerCall(preparedPack, 9, compoundTag, ROOT);
        packPlayerCall.argument = compoundTag2;
        FoodSocketPowers.planPackPlayerRoot(packPlayerCall, compoundTag, compoundTag2);
        try {
            FoodSocketPowers.enterPackPlayerTag(packPlayerCall, (Tag)compoundTag2);
            packPlayerCall.originalEntered = true;
            Tag tag = compoundTag.m_128365_(ROOT, (Tag)compoundTag2);
            packPublication.rootWriteCompleted = true;
            packPlayerCall.returnedValue = tag;
            packPlayerCall.originalReturned = true;
            FoodSocketPowers.returnPackPlayerTag(packPlayerCall);
            Tag tag2 = tag;
            return tag2;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerOriginalFailure(packPlayerCall, throwable);
            throw throwable;
        }
        finally {
            FoodSocketPowers.closePackPlayerCall(packPlayerCall);
        }
    }

    private static boolean readBackPackPublication(PackPublication packPublication) {
        boolean bl = packPublication.rootReadbackMatched = packPublication.persistent.m_128423_(ROOT) == packPublication.target;
        if (!packPublication.rootReadbackMatched) {
            return false;
        }
        boolean bl2 = true;
        for (int i = 0; i < packPublication.keys.size(); ++i) {
            String string = packPublication.keys.get(i);
            packPublication.readbackProgress[i] = 1;
            Tag tag = packPublication.target.m_128423_(string);
            if (tag != null) {
                packPublication.observed.m_128365_(string, tag.m_6426_());
            }
            packPublication.readbackProgress[i] = 2;
            bl2 &= Objects.equals(packPublication.observed.m_128423_(string), packPublication.expected.m_128423_(string));
        }
        packPublication.rootReadbackCompleted = true;
        packPublication.ownPostimageMatched = bl2 && packPublication.persistent.m_128423_(ROOT) == packPublication.target;
        return packPublication.ownPostimageMatched;
    }

    private static boolean ownsPackPublication(PreparedPack preparedPack, FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding) {
        PackPublication packPublication = preparedPack.publication;
        CompleteBatchBinding completeBatchBinding = preparedPack.binding;
        if (!(FoodSocketPowers.packCleanupPhase(preparedPack) && packPublication != null && Thread.holdsLock(FoodWeaponSourceLedger.class) && Thread.currentThread() == packPublication.thread && packPublication.server == packPublication.player.m_284548_().m_7654_() && packPublication.server.m_18695_() && completeBatchBinding.originalBinding == FoodWeaponSourceLedger.originalInteractionBinding(completeBatchBinding.batch) && FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(completeBatchBinding.originalBinding, originalInteractionBinding, packPublication.player))) {
            return false;
        }
        if (packPublication.player.getPersistentData() != packPublication.persistent || packPublication.persistent.m_128423_(ROOT) != packPublication.target) {
            return false;
        }
        for (int i = 0; i < packPublication.keys.size(); ++i) {
            Tag tag;
            String string = packPublication.keys.get(i);
            Tag tag2 = tag = packPublication.restoreProgress[i] == 2 ? packPublication.previous.m_128423_(string) : packPublication.expected.m_128423_(string);
            if (Objects.equals(packPublication.target.m_128423_(string), tag)) continue;
            return false;
        }
        return FoodWeaponSourceLedger.matchesCleanupInteractionIdentity(completeBatchBinding.originalBinding, originalInteractionBinding, packPublication.player) && FoodSocketPowers.packCleanupPhase(preparedPack);
    }

    private static void recordPackObserverFailure(PreparedPack preparedPack, int n, Throwable throwable) {
        if (preparedPack.originalSequence != null) {
            preparedPack.originalSequence.observationFailed(n, throwable);
        }
        preparedPack.ownerWriterFailures.observer(n, throwable);
        try {
            if (preparedPack.ownerTokenStep != null) {
                FoodWeaponSourceLedger.ownerWriterObservationFailed(preparedPack.ownerTokenStep, n, throwable);
            } else if (preparedPack.ownerController != null) {
                FoodWeaponSourceLedger.ownerPlayerWriterObservationFailed(preparedPack.ownerController, n, throwable);
            } else if (preparedPack.ownerPublication != null) {
                FoodWeaponSourceLedger.ownerPublicationObservationFailed(preparedPack.ownerPublication, n, throwable);
            }
        }
        catch (Throwable throwable2) {
            preparedPack.ownerWriterFailures.diagnostic(n, throwable2);
        }
    }

    private static boolean packSourceContext(FoodWeaponBondHits.Context context) {
        if (context == null) {
            return false;
        }
        ItemStack itemStack = context.actualWeapon();
        if (!FourWeaponFoundationAdapter.applies(itemStack)) {
            return true;
        }
        CompoundTag compoundTag = context.snapshot();
        CompoundTag compoundTag2 = FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack);
        return compoundTag.m_128425_("FoundationSnapshot", 10) && compoundTag.m_128425_("FoundationApprovedRules", 8) && !compoundTag.m_128461_("FoundationApprovedRules").isEmpty() && !compoundTag2.m_128456_() && compoundTag2.equals((Object)compoundTag.m_128469_("FoundationSnapshot")) && FoodWeaponSourceLedger.validReadOnly(compoundTag, context.player()) && FoodWeaponSourceLedger.originalHandContinuousReadOnly(compoundTag, context.player());
    }

    private static PackPlayerCall beginPackPlayerCall(PreparedPack preparedPack, int n, CompoundTag compoundTag, String string) {
        PackPlayerCall packPlayerCall = preparedPack.ownerPlayerCalls[n];
        packPlayerCall.publication = preparedPack.ownerPublication;
        packPlayerCall.originalSequence = preparedPack.originalSequence;
        packPlayerCall.writer = preparedPack.ownerController;
        packPlayerCall.receiver = compoundTag;
        packPlayerCall.key = string;
        return packPlayerCall;
    }

    private static void planPackPlayerUuid(PackPlayerCall packPlayerCall, int n, UUID uUID) {
        if (packPlayerCall.writer == null) {
            return;
        }
        packPlayerCall.planEntered = true;
        try {
            packPlayerCall.step = FoodWeaponSourceLedger.planOwnerPlayerUuidKey(packPlayerCall.writer, n, packPlayerCall.receiver, packPlayerCall.key, uUID);
            packPlayerCall.planReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void enterPackPlayerUuid(PackPlayerCall packPlayerCall, UUID uUID) {
        if (packPlayerCall.step == null) {
            return;
        }
        packPlayerCall.entryEntered = true;
        try {
            FoodWeaponSourceLedger.ownerPlayerUuidEntered(packPlayerCall.step, packPlayerCall.receiver, packPlayerCall.key, uUID);
            packPlayerCall.entryReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void returnPackPlayerTag(PackPlayerCall packPlayerCall) {
        if (packPlayerCall.step == null) {
            return;
        }
        packPlayerCall.returnEntered = true;
        try {
            FoodWeaponSourceLedger.ownerPlayerPutReturned(packPlayerCall.step, packPlayerCall.receiver);
            packPlayerCall.returnReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 1, throwable);
        }
    }

    private static void closePackPlayerCall(PackPlayerCall packPlayerCall) {
        if (packPlayerCall.step == null || packPlayerCall.closeEntered) {
            return;
        }
        packPlayerCall.closeEntered = true;
        try {
            FoodWeaponSourceLedger.closeOwnerPlayerStep(packPlayerCall.step);
            packPlayerCall.closeReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 3, throwable);
        }
    }

    private static void recordPackPlayerOriginalFailure(PackPlayerCall packPlayerCall, Throwable throwable) {
        if (packPlayerCall.originalSequence != null) {
            packPlayerCall.originalSequence.originalThrown(throwable);
        }
        if (packPlayerCall.failures.original != null) {
            return;
        }
        packPlayerCall.failures.original(throwable);
        try {
            if (packPlayerCall.step != null) {
                FoodWeaponSourceLedger.ownerPlayerOriginalFailed(packPlayerCall.step, throwable);
            } else if (packPlayerCall.writer != null) {
                FoodWeaponSourceLedger.ownerPlayerWriterOriginalFailed(packPlayerCall.writer, throwable);
            }
        }
        catch (Throwable throwable2) {
            packPlayerCall.failures.diagnostic(2, throwable2);
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 2, throwable2);
        }
    }

    private static void planPackPlayerString(PackPlayerCall packPlayerCall, int n, String string) {
        if (packPlayerCall.writer == null) {
            return;
        }
        packPlayerCall.planEntered = true;
        try {
            packPlayerCall.step = FoodWeaponSourceLedger.planOwnerPlayerStringKey(packPlayerCall.writer, n, packPlayerCall.receiver, packPlayerCall.key, string);
            packPlayerCall.planReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void enterPackPlayerString(PackPlayerCall packPlayerCall, String string) {
        if (packPlayerCall.step == null) {
            return;
        }
        packPlayerCall.entryEntered = true;
        try {
            FoodWeaponSourceLedger.ownerPlayerStringEntered(packPlayerCall.step, packPlayerCall.receiver, packPlayerCall.key, string);
            packPlayerCall.entryReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void planPackPlayerTag(PackPlayerCall packPlayerCall, int n, Tag tag) {
        if (packPlayerCall.writer == null) {
            return;
        }
        packPlayerCall.planEntered = true;
        try {
            packPlayerCall.step = FoodWeaponSourceLedger.planOwnerPlayerKey(packPlayerCall.writer, n, packPlayerCall.key, tag);
            packPlayerCall.planReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void enterPackPlayerTag(PackPlayerCall packPlayerCall, Tag tag) {
        if (packPlayerCall.step == null) {
            return;
        }
        packPlayerCall.entryEntered = true;
        try {
            FoodWeaponSourceLedger.ownerPlayerPutEntered(packPlayerCall.step, packPlayerCall.receiver, packPlayerCall.key, tag);
            packPlayerCall.entryReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void planPackPlayerLong(PackPlayerCall packPlayerCall, int n, long l) {
        if (packPlayerCall.writer == null) {
            return;
        }
        packPlayerCall.planEntered = true;
        try {
            packPlayerCall.step = FoodWeaponSourceLedger.planOwnerPlayerLongKey(packPlayerCall.writer, n, packPlayerCall.receiver, packPlayerCall.key, l);
            packPlayerCall.planReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void enterPackPlayerLong(PackPlayerCall packPlayerCall, long l) {
        if (packPlayerCall.step == null) {
            return;
        }
        packPlayerCall.entryEntered = true;
        try {
            FoodWeaponSourceLedger.ownerPlayerLongEntered(packPlayerCall.step, packPlayerCall.receiver, packPlayerCall.key, l);
            packPlayerCall.entryReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static void planPackPlayerRoot(PackPlayerCall packPlayerCall, CompoundTag compoundTag, CompoundTag compoundTag2) {
        if (packPlayerCall.writer == null) {
            return;
        }
        packPlayerCall.planEntered = true;
        try {
            packPlayerCall.step = FoodWeaponSourceLedger.planOwnerPlayerRoot(packPlayerCall.writer, 9, compoundTag, compoundTag2);
            packPlayerCall.planReturned = true;
        }
        catch (Throwable throwable) {
            FoodSocketPowers.recordPackPlayerObserverFailure(packPlayerCall, 0, throwable);
        }
    }

    private static boolean packCleanupPhase(PreparedPack preparedPack) {
        if (preparedPack.originalSequence != null && preparedPack.originalSequence.cleanup(preparedPack, preparedPack.publishingCleanup)) {
            return true;
        }
        return preparedPack.finalizingCleanup == null || preparedPack.publishingCleanup == preparedPack.finalizingCleanup && FoodWeaponFinalDamage.matchesPackPublicationCleanup(preparedPack, preparedPack.finalizingCleanup);
    }

    private static void recordPackPlayerObserverFailure(PackPlayerCall packPlayerCall, int n, Throwable throwable) {
        if (packPlayerCall.originalSequence != null) {
            packPlayerCall.originalSequence.observationFailed(n, throwable);
        }
        packPlayerCall.failures.observer(n, throwable);
        try {
            if (packPlayerCall.step != null) {
                FoodWeaponSourceLedger.ownerPlayerObservationFailed(packPlayerCall.step, n, throwable);
            } else if (packPlayerCall.writer != null) {
                FoodWeaponSourceLedger.ownerPlayerWriterObservationFailed(packPlayerCall.writer, n, throwable);
            }
        }
        catch (Throwable throwable2) {
            packPlayerCall.failures.diagnostic(n, throwable2);
        }
    }

    public static List<FoodWeaponEvolution.WeaponDetailView> details(ItemStack itemStack) {
        int[] nArray = new int[FoodWeaponEvolution.FoodType.values().length];
        for (FoodWeaponEvolution.FoodType foodType : FoodWeaponEvolution.FoodType.values()) {
            nArray[foodType.ordinal()] = FoodSocketPowers.count(itemStack, foodType);
        }
        int n = nArray[FoodWeaponEvolution.FoodType.FISH.ordinal()];
        int n2 = nArray[FoodWeaponEvolution.FoodType.WINE.ordinal()];
        int n3 = nArray[FoodWeaponEvolution.FoodType.MEAT.ordinal()];
        int n4 = nArray[FoodWeaponEvolution.FoodType.CHEESE.ordinal()];
        ArrayList<FoodWeaponEvolution.WeaponDetailView> arrayList = new ArrayList<FoodWeaponEvolution.WeaponDetailView>();
        if (FoodSocketPowerRules.flight(n, n2)) {
            arrayList.add(new FoodWeaponEvolution.WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.socket.combo"), (Component)Component.m_237110_((String)"screen.ftboss.socket.flight", (Object[])new Object[]{Math.min(2, n), Math.min(1, n2)}), (Component)Component.m_237115_((String)"screen.ftboss.socket.flight.description")));
        }
        if (FoodSocketPowerRules.pack(n3, n4)) {
            arrayList.add(new FoodWeaponEvolution.WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.socket.combo"), (Component)Component.m_237110_((String)"screen.ftboss.socket.pack", (Object[])new Object[]{Math.min(2, n3), Math.min(1, n4)}), (Component)Component.m_237115_((String)"screen.ftboss.socket.pack.description")));
        }
        for (FoodWeaponEvolution.FoodType foodType : FoodWeaponEvolution.FoodType.values()) {
            if (FoodSocketPowers.setBonus(nArray, foodType) == 0) continue;
            arrayList.add(new FoodWeaponEvolution.WeaponDetailView((Component)Component.m_237115_((String)"screen.ftboss.socket.combo"), (Component)Component.m_237115_((String)"screen.ftboss.socket.set_boost"), (Component)Component.m_237115_((String)"screen.ftboss.socket.set_boost.description")));
            break;
        }
        return List.copyOf(arrayList);
    }

    private static final class PackPlayerCall {
        private final PackWriterFailures failures = new PackWriterFailures();
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

        private PackPlayerCall() {
        }
    }

    public static final class PreparedPack {
        private final PackPlan plan;
        private final CompleteBatchBinding binding;
        private FoodWeaponFinalDamage.PublishingCleanup publishingCleanup;
        private FoodWeaponFinalDamage.PublishingCleanup finalizingCleanup;
        private PackPublication publication;
        private FoodWeaponConsumerSequence.Accepted originalSequence;
        private PackOwnerParameters ownerParameters;
        private FoodWeaponSourceLedger.OwnerWriterPublication ownerPublication;
        private FoodWeaponSourceLedger.OwnerPlayerWriter ownerController;
        private final PackWriterFailures ownerWriterFailures = new PackWriterFailures();
        private final PackPlayerCall[] ownerPlayerCalls = FoodSocketPowers.newPackPlayerCalls();
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

        private PreparedPack(PackPlan packPlan, CompleteBatchBinding completeBatchBinding) {
            this.plan = packPlan;
            this.binding = completeBatchBinding;
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

        private PackOwnerParameters(CompoundTag compoundTag, CompoundTag compoundTag2, UUID uUID, UUID uUID2, CompoundTag compoundTag3, CompoundTag compoundTag4, long l) {
            this.persistent = compoundTag;
            this.data = compoundTag2;
            this.weaponToken = uUID;
            this.packToken = uUID2;
            this.fingerprint = compoundTag3;
            this.snapshot = compoundTag4;
            this.tick = l;
        }
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

        private PackPublication(PreparedPack preparedPack, CompoundTag compoundTag, CompoundTag compoundTag2) {
            this.player = preparedPack.plan.context.player();
            this.weapon = preparedPack.plan.context.actualWeapon();
            this.readPersistent = compoundTag;
            this.beforeFirstWriteRoot = compoundTag.m_128423_(FoodSocketPowers.ROOT);
            this.beforeFirstWrite = new CompoundTag();
            this.target = compoundTag2;
            this.expected = new CompoundTag();
            this.previous = new CompoundTag();
            this.observed = new CompoundTag();
            this.keys = List.of("PackToken", "PackWeapon", "PackWeaponId", "PackHand", "PackDimension", "PackFingerprint", "PackSource", "PackUntil", "PackCooldown");
            this.readbackProgress = new byte[this.keys.size()];
            this.restoreProgress = new byte[this.keys.size()];
            for (String string : this.keys) {
                Tag tag;
                Tag tag2;
                Tag tag3;
                Tag tag4 = compoundTag2.m_128423_(string);
                if (tag4 != null) {
                    this.expected.m_128365_(string, tag4.m_6426_());
                }
                if ((tag3 = this.beforeFirstWriteRoot) instanceof CompoundTag) {
                    CompoundTag compoundTag3 = (CompoundTag)tag3;
                    tag2 = compoundTag3.m_128423_(string);
                } else {
                    tag2 = null;
                }
                if ((tag = tag2) == null) continue;
                this.beforeFirstWrite.m_128365_(string, tag.m_6426_());
            }
            this.thread = Thread.currentThread();
            this.server = this.player.m_284548_().m_7654_();
        }
    }

    public static final class PackPlan {
        private final FoodWeaponBondHits.Context context;
        private final Tag controller;
        private final Tag weaponTag;
        private final List<FoodWeaponSourceLedger.AttemptRequest> attempts;
        private boolean committed;

        private PackPlan(FoodWeaponBondHits.Context context, FoodWeaponSourceLedger.AttemptRequest attemptRequest) {
            this.context = context;
            Tag tag = context.player().getPersistentData().m_128423_(FoodSocketPowers.ROOT);
            this.controller = tag == null ? null : tag.m_6426_();
            this.weaponTag = context.actualWeapon().m_41783_() == null ? null : context.actualWeapon().m_41783_().m_6426_();
            this.attempts = List.of(attemptRequest);
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

    public static final class PackCommit {
        private final PackPlan plan;
        private final PreparedPack prepared;
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

        private PackCommit(PreparedPack preparedPack, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, UUID uUID, CompoundTag compoundTag) {
            CompoundTag compoundTag2;
            this.plan = preparedPack.plan;
            this.prepared = preparedPack;
            this.publishedController = compoundTag;
            this.publishedState = compoundTag.m_6426_();
            Tag tag = preparedPack.plan.controller;
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag3 = (CompoundTag)tag;
                compoundTag2 = compoundTag3.m_6426_();
            } else {
                compoundTag2 = new CompoundTag();
            }
            this.previousController = compoundTag2;
            this.receipt = offenseReceipt;
            this.amount = f;
            this.token = uUID;
            this.thread = Thread.currentThread();
            this.server = preparedPack.plan.context.player().m_284548_().m_7654_();
        }
    }

    private static final class CompleteBatchBinding {
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding originalBinding;
        private final List<FoodWeaponSourceLedger.AttemptRequest> requests;
        private final List<FoodWeaponSourceLedger.TargetRequest> targets;

        private CompleteBatchBinding(FoodWeaponSourceLedger.AttemptBatchQuote attemptBatchQuote, FoodWeaponSourceLedger.AttemptBatchAdmission attemptBatchAdmission, List<FoodWeaponSourceLedger.AttemptRequest> list, List<FoodWeaponSourceLedger.TargetRequest> list2) {
            this.batch = attemptBatchQuote;
            this.admission = attemptBatchAdmission;
            this.originalBinding = FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote);
            this.requests = List.copyOf(list);
            this.targets = List.copyOf(list2);
        }
    }

    private static final class PackWriterFailures {
        private final Throwable[] observer = new Throwable[4];
        private final Throwable[] diagnostic = new Throwable[4];
        private Throwable original;

        private PackWriterFailures() {
        }

        private void observer(int n, Throwable throwable) {
            if (this.observer[n] == null) {
                this.observer[n] = throwable;
            }
        }

        private void diagnostic(int n, Throwable throwable) {
            if (this.diagnostic[n] == null) {
                this.diagnostic[n] = throwable;
            }
        }

        private void original(Throwable throwable) {
            if (this.original == null) {
                this.original = throwable;
            }
        }
    }
}

