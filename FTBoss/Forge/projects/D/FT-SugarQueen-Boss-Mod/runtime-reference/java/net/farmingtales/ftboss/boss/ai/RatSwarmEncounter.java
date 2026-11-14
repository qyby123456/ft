/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.BossHandler;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.boss.ai.BossMercy;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.RatSwarmRules;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class RatSwarmEncounter {
    private static final String READY = "ftboss:rat_cycle_ready";
    private static final String DEPUTY = "ftboss:rat_deputy";
    private static final String OWNER = "ftboss:rat_cycle_owner";
    private static final String DEPUTY_ID = "ftboss:rat_deputy_id";
    private static final String SWARM = "ftboss:rat_deputies";
    private static final String WAVE = "ftboss:rat_swarm_wave";
    private static final String POOL = "ftboss:rat_swarm_health";
    private static final String POOL_MAX = "ftboss:rat_swarm_max_health";
    private static final String LAST_HIT = "ftboss:rat_swarm_last_hit";
    private static final String LAST_DAMAGE = "ftboss:rat_swarm_last_damage";
    private static final String DEFEATED = "ftboss:rat_deputy_defeated";
    private static final String PHASE = "ftboss:rat_cycle_phase";
    private static final String TIMER = "ftboss:rat_device_ticks";
    private static final String DEVICES = "ftboss:rat_devices";
    private static final String DEVICE = "ftboss:rat_cheese_device";
    private static final String DEVICE_HEALTH = "ftboss:rat_device_health";
    private static final String LAST_FEED = "ftboss:rat_last_feed";
    private static final String MISSING_TICKS = "ftboss:rat_missing_ticks";
    private static final TagKey<Item> HEALING_FOOD = TagKey.m_203882_((ResourceKey)Registries.f_256913_, (ResourceLocation)new ResourceLocation("ftboss", "rat_swarm_healing_food"));
    private static final double BAR_RANGE = 48.0;
    private static final int MISSING_GRACE = 200;

    private RatSwarmEncounter() {
    }

    public static boolean isDeputy(RatBossEntity ratBossEntity) {
        return ratBossEntity.getPersistentData().m_128471_(DEPUTY);
    }

    public static boolean isMaster(RatBossEntity ratBossEntity) {
        return !ratBossEntity.isMinion();
    }

    public static boolean skipEmpower(LivingEntity livingEntity) {
        RatBossEntity ratBossEntity;
        return livingEntity instanceof RatBossEntity && (RatSwarmEncounter.isMaster(ratBossEntity = (RatBossEntity)livingEntity) || RatSwarmEncounter.isDeputy(ratBossEntity));
    }

    public static boolean skipSharedHealth(LivingHurtEvent livingHurtEvent) {
        RatBossEntity ratBossEntity;
        LivingEntity livingEntity = livingHurtEvent.getEntity();
        return livingEntity instanceof RatBossEntity && RatSwarmEncounter.isDeputy(ratBossEntity = (RatBossEntity)livingEntity);
    }

    public static boolean handlePlayerHit(LivingHurtEvent livingHurtEvent) {
        ServerPlayer serverPlayer;
        LivingEntity livingEntity;
        block6: {
            block5: {
                livingEntity = livingHurtEvent.getEntity();
                if (!(livingEntity instanceof ServerPlayer)) break block5;
                serverPlayer = (ServerPlayer)livingEntity;
                if (!serverPlayer.m_9236_().f_46443_ && (livingEntity = livingHurtEvent.getSource().m_7639_()) instanceof RatBossEntity) break block6;
            }
            return false;
        }
        RatBossEntity ratBossEntity = (RatBossEntity)livingEntity;
        if (!RatSwarmEncounter.isDeputy(ratBossEntity) && ratBossEntity.isMinion()) {
            return false;
        }
        BossMercy.noteThreat(serverPlayer, (Entity)ratBossEntity, serverPlayer.m_9236_().m_46467_());
        if (RatSwarmEncounter.isDeputy(ratBossEntity)) {
            RatSwarmEncounter.applyDeputyDebuff(serverPlayer);
        }
        return true;
    }

    public static boolean skipVictory(Entity entity) {
        RatBossEntity ratBossEntity;
        return entity instanceof RatBossEntity && (ratBossEntity = (RatBossEntity)entity).isMinion();
    }

    public static void appendTooltip(List<Component> list) {
        list.add((Component)Component.m_237113_((String)"\u00a77\u51fb\u8d25\u526f\u738b\u7834\u76fe\uff0c\u518d\u7528\u5976\u916a\u6216\u725b\u5976\u5582\u6ee1\u9650\u65f6\u88c5\u7f6e"));
        list.add((Component)Component.m_237113_((String)"\u00a78\u5355\u6b21\u4f24\u5bb3\u8fbe\u5230\u4e3b\u738b\u6700\u5927\u751f\u547d\u53ef\u76f4\u63a5\u7a7f\u76fe"));
    }

    public static void prepare(RatBossEntity ratBossEntity) {
        if (ratBossEntity.m_9236_().f_46443_ || !RatSwarmEncounter.isMaster(ratBossEntity)) {
            return;
        }
        if (!ratBossEntity.getPersistentData().m_128471_(READY)) {
            RatSwarmEncounter.initialize(ratBossEntity);
        }
        ratBossEntity.m_21557_(true);
        ratBossEntity.m_20331_(true);
    }

    public static boolean blocksDamage(RatBossEntity ratBossEntity, DamageSource damageSource, float f) {
        ServerPlayer serverPlayer;
        Entity entity;
        if (ratBossEntity.m_9236_().f_46443_) {
            return false;
        }
        if (!Float.isFinite(f) || f <= 0.0f) {
            return true;
        }
        if (!RatSwarmEncounter.isMaster(ratBossEntity)) {
            if (RatSwarmEncounter.isDeputy(ratBossEntity)) {
                ratBossEntity.f_19802_ = 0;
            }
            return false;
        }
        if (damageSource.m_269533_(DamageTypeTags.f_268738_)) {
            return false;
        }
        if (ratBossEntity.getPersistentData().m_128451_(PHASE) == 1) {
            return false;
        }
        float f2 = ratBossEntity.getPersistentData().m_128471_(READY) ? ratBossEntity.m_21233_() : 20000.0f;
        if (RatSwarmRules.lethalBurst(f, f2, (entity = damageSource.m_7639_()) instanceof ServerPlayer && (serverPlayer = (ServerPlayer)entity).m_6084_() && !serverPlayer.m_5833_())) {
            ratBossEntity.m_20331_(false);
            return false;
        }
        return true;
    }

    public static boolean tick(RatBossEntity ratBossEntity) {
        Level level = ratBossEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (RatSwarmEncounter.isDeputy(ratBossEntity)) {
            RatSwarmEncounter.tickDeputy(serverLevel, ratBossEntity);
            if (ratBossEntity.m_6084_() && !ratBossEntity.m_213877_()) {
                ratBossEntity.tickEncounterMelee();
            }
            return true;
        }
        if (!RatSwarmEncounter.isMaster(ratBossEntity)) {
            return false;
        }
        if (!ratBossEntity.m_6084_()) {
            return true;
        }
        level = ratBossEntity.getPersistentData();
        if (!level.m_128471_(READY)) {
            RatSwarmEncounter.initialize(ratBossEntity);
        }
        ratBossEntity.m_21557_(true);
        ratBossEntity.m_6710_(null);
        ratBossEntity.m_21573_().m_26573_();
        boolean bl = level.m_128451_(PHASE) == 1;
        ratBossEntity.m_20331_(!bl);
        if (bl) {
            int n = Math.max(0, level.m_128451_(TIMER) - 1);
            level.m_128405_(TIMER, n);
            MobEffectInstance mobEffectInstance = RatSwarmEncounter.devices(serverLevel, ratBossEntity);
            if (RatSwarmRules.deviceExpired(n)) {
                for (ArmorStand armorStand : mobEffectInstance) {
                    RatSwarmEncounter.explodeDevice(serverLevel, ratBossEntity, armorStand);
                }
                mobEffectInstance.clear();
            }
            if (mobEffectInstance.isEmpty()) {
                RatSwarmEncounter.beginGuard(serverLevel, ratBossEntity);
            } else if (ratBossEntity.f_19797_ % 20 == 0) {
                for (ArmorStand armorStand : mobEffectInstance) {
                    RatSwarmEncounter.updateDeviceName(armorStand, n);
                }
            }
        } else {
            RatSwarmEncounter.ensureSwarm(serverLevel, ratBossEntity);
            if (level.m_128471_(DEFEATED) || level.m_128457_(POOL) <= 0.0f) {
                RatSwarmEncounter.beginDevices(serverLevel, ratBossEntity);
            } else {
                RatSwarmEncounter.maintainSwarm(serverLevel, ratBossEntity);
            }
        }
        RatSwarmEncounter.updateBar(serverLevel, ratBossEntity, true);
        if (ratBossEntity.f_19797_ % 20 == 0) {
            BossMusic.request(serverLevel, (LivingEntity)ratBossEntity);
            for (MobEffectInstance mobEffectInstance : new ArrayList(ratBossEntity.m_21220_())) {
                if (mobEffectInstance.m_19544_().m_19486_()) continue;
                ratBossEntity.m_21195_(mobEffectInstance.m_19544_());
            }
        }
        return true;
    }

    private static void initialize(RatBossEntity ratBossEntity) {
        float f = ratBossEntity.m_21233_() > 0.0f ? ratBossEntity.m_21223_() / ratBossEntity.m_21233_() : 1.0f;
        double d = 20000.0 * (NonManorFinaleDifficulty.isFinale((LivingEntity)ratBossEntity) ? 2.0 : 1.0);
        ratBossEntity.m_21051_(Attributes.f_22276_).m_22100_(d);
        ratBossEntity.m_21051_(Attributes.f_22281_).m_22100_(0.0);
        ratBossEntity.m_21051_(Attributes.f_22284_).m_22100_(0.0);
        ratBossEntity.m_21153_(Math.max(1.0f, ratBossEntity.m_21233_() * f));
        CompoundTag compoundTag = ratBossEntity.getPersistentData();
        compoundTag.m_128347_("ftboss_base_hp", d);
        compoundTag.m_128347_("ftboss_base_atk", 0.0);
        compoundTag.m_128347_("ftboss:regular_finale_base/minecraft:generic.max_health", 20000.0);
        compoundTag.m_128379_(READY, true);
        compoundTag.m_128405_(PHASE, 0);
        compoundTag.m_128473_(DEPUTY_ID);
        compoundTag.m_128473_(DEFEATED);
        compoundTag.m_128473_(SWARM);
        compoundTag.m_128473_(WAVE);
        compoundTag.m_128473_(POOL);
        compoundTag.m_128473_(POOL_MAX);
        compoundTag.m_128473_(LAST_HIT);
        compoundTag.m_128473_(LAST_DAMAGE);
        ratBossEntity.m_21557_(true);
        ratBossEntity.m_20331_(true);
    }

    private static void tickDeputy(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        if (!ratBossEntity.m_6084_()) {
            return;
        }
        RatBossEntity ratBossEntity2 = RatSwarmEncounter.owner(serverLevel, (Entity)ratBossEntity);
        if (ratBossEntity2 == null) {
            int n = ratBossEntity.getPersistentData().m_128451_(MISSING_TICKS) + 1;
            ratBossEntity.getPersistentData().m_128405_(MISSING_TICKS, n);
            if (n >= 200) {
                ratBossEntity.m_146870_();
            }
            return;
        }
        if (ratBossEntity2.getPersistentData().m_128451_(PHASE) == 0) {
            RatSwarmEncounter.ensureSwarm(serverLevel, ratBossEntity2);
        }
        if (!ratBossEntity2.m_6084_() || ratBossEntity2.getPersistentData().m_128451_(PHASE) != 0 || !RatSwarmEncounter.swarmMember(ratBossEntity2, ratBossEntity)) {
            ratBossEntity.m_146870_();
            return;
        }
        ratBossEntity.getPersistentData().m_128405_(MISSING_TICKS, 0);
        ratBossEntity.m_21557_(false);
        ratBossEntity.m_20331_(false);
        if (ratBossEntity.f_19797_ % 20 == 0) {
            ratBossEntity.m_6710_((LivingEntity)RatSwarmEncounter.nearestPlayer(serverLevel, (Entity)ratBossEntity));
        }
        RatSwarmEncounter.syncSwarmHealth(ratBossEntity2, ratBossEntity);
        for (MobEffectInstance mobEffectInstance : new ArrayList(ratBossEntity.m_21220_())) {
            if (mobEffectInstance.m_19544_().m_19486_()) continue;
            ratBossEntity.m_21195_(mobEffectInstance.m_19544_());
        }
        if (ratBossEntity.m_20148_().equals(RatSwarmEncounter.uuid(ratBossEntity2.getPersistentData(), DEPUTY_ID))) {
            RatSwarmEncounter.updateBar(serverLevel, ratBossEntity, false);
        } else {
            ratBossEntity.hideBossBar();
        }
    }

    private static void spawnDeputy(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        int n = RatSwarmRules.swarmSize(ratBossEntity.getPersistentData().m_128457_(POOL), ratBossEntity.getPersistentData().m_128457_(POOL_MAX));
        ListTag listTag = ratBossEntity.getPersistentData().m_128437_(SWARM, 10);
        int n2 = n - listTag.size();
        for (int i = 0; i < n2; ++i) {
            RatSwarmEncounter.spawnMember(serverLevel, ratBossEntity, listTag.size(), n);
        }
        RatSwarmEncounter.selectSwarmBar(serverLevel, ratBossEntity);
    }

    private static void spawnMember(ServerLevel serverLevel, RatBossEntity ratBossEntity, int n, int n2) {
        RatBossEntity ratBossEntity2 = (RatBossEntity)ratBossEntity.m_6095_().m_20615_((Level)serverLevel);
        if (ratBossEntity2 == null) {
            return;
        }
        ratBossEntity2.setMinion(true);
        ratBossEntity2.getPersistentData().m_128379_(DEPUTY, true);
        ratBossEntity2.getPersistentData().m_128362_(OWNER, ratBossEntity.m_20148_());
        ratBossEntity2.getPersistentData().m_128362_(WAVE, ratBossEntity.getPersistentData().m_128342_(WAVE));
        double d = NonManorFinaleDifficulty.isFinale((LivingEntity)ratBossEntity) ? 2.0 : 1.0;
        ratBossEntity2.getPersistentData().m_128347_("ftboss_base_hp", 400.0 * d);
        ratBossEntity2.getPersistentData().m_128347_("ftboss_base_atk", 2.0);
        ratBossEntity2.getPersistentData().m_128347_("ftboss:regular_finale_base/minecraft:generic.max_health", 400.0);
        ratBossEntity2.m_21051_(Attributes.f_22276_).m_22100_((double)ratBossEntity.getPersistentData().m_128457_(POOL_MAX));
        ratBossEntity2.m_21051_(Attributes.f_22281_).m_22100_(2.0);
        ratBossEntity2.m_21051_(Attributes.f_22284_).m_22100_(0.0);
        ratBossEntity2.m_21051_(Attributes.f_22279_).m_22100_(0.24);
        ratBossEntity2.m_21153_(ratBossEntity.getPersistentData().m_128457_(POOL));
        ratBossEntity2.m_21557_(false);
        ratBossEntity2.m_20331_(false);
        ratBossEntity2.m_21530_();
        ratBossEntity2.m_6593_((Component)Component.m_237115_((String)"entity.ftboss.rat_minion"));
        ratBossEntity2.m_20340_(true);
        double d2 = Math.PI * 2 * (double)n / (double)n2;
        double d3 = 2.5 + ratBossEntity.m_217043_().m_188500_() * 4.0;
        if (!BossCollision.place(serverLevel, (Entity)ratBossEntity2, ratBossEntity.m_20182_().m_82520_(Math.cos(d2) * d3, 0.0, Math.sin(d2) * d3), BossCollision.SpawnMode.GROUND, (float)Math.toDegrees(d2))) {
            return;
        }
        ManorFightMembers.inherit((Entity)ratBossEntity, (Entity)ratBossEntity2);
        MinionBlessing.mark((Entity)ratBossEntity2, ratBossEntity.m_20148_());
        LemonSummonRewards.mark((Entity)ratBossEntity2);
        if (!serverLevel.m_7967_((Entity)ratBossEntity2)) {
            return;
        }
        ListTag listTag = ratBossEntity.getPersistentData().m_128437_(SWARM, 10);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128362_("id", ratBossEntity2.m_20148_());
        listTag.add((Object)compoundTag);
        ratBossEntity.getPersistentData().m_128365_(SWARM, (Tag)listTag);
        ratBossEntity2.m_6710_((LivingEntity)RatSwarmEncounter.nearestPlayer(serverLevel, (Entity)ratBossEntity2));
    }

    private static float swarmMaximum(RatBossEntity ratBossEntity) {
        return (float)(400.0 * (double)ratBossEntity.m_21233_() / 20000.0);
    }

    private static void ensureSwarm(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        UUID uUID;
        CompoundTag compoundTag = ratBossEntity.getPersistentData();
        if (!compoundTag.m_128403_(WAVE)) {
            compoundTag.m_128362_(WAVE, UUID.randomUUID());
        }
        if (!compoundTag.m_128441_(SWARM)) {
            ListTag listTag = new ListTag();
            uUID = RatSwarmEncounter.uuid(compoundTag, DEPUTY_ID);
            if (uUID != null) {
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.m_128362_("id", uUID);
                listTag.add((Object)compoundTag2);
            }
            compoundTag.m_128365_(SWARM, (Tag)listTag);
        }
        if (!compoundTag.m_128441_(POOL)) {
            RatBossEntity ratBossEntity2;
            float f = RatSwarmEncounter.swarmMaximum(ratBossEntity);
            uUID = compoundTag.m_128403_(DEPUTY_ID) ? serverLevel.m_8791_(compoundTag.m_128342_(DEPUTY_ID)) : null;
            float f2 = uUID instanceof RatBossEntity && RatSwarmEncounter.isDeputy(ratBossEntity2 = (RatBossEntity)((Object)uUID)) && RatSwarmEncounter.owner(serverLevel, (Entity)ratBossEntity2) == ratBossEntity ? RatSwarmRules.scaleSwarm(ratBossEntity2.m_21223_(), ratBossEntity2.m_21233_(), f) : f;
            compoundTag.m_128350_(POOL_MAX, f);
            compoundTag.m_128350_(POOL, f2);
        }
    }

    private static void maintainSwarm(ServerLevel serverLevel, RatBossEntity ratBossEntity2) {
        CompoundTag compoundTag = ratBossEntity2.getPersistentData();
        float f = RatSwarmEncounter.swarmMaximum(ratBossEntity2);
        compoundTag.m_128350_(POOL, RatSwarmRules.scaleSwarm(compoundTag.m_128457_(POOL), compoundTag.m_128457_(POOL_MAX), f));
        compoundTag.m_128350_(POOL_MAX, f);
        ListTag listTag = compoundTag.m_128437_(SWARM, 10);
        List<RatBossEntity> list = RatSwarmEncounter.deputies(serverLevel, ratBossEntity2);
        int n = list.size() < listTag.size() ? compoundTag.m_128451_(MISSING_TICKS) + 1 : 0;
        compoundTag.m_128405_(MISSING_TICKS, n);
        if (n >= 200) {
            for (int i = listTag.size() - 1; i >= 0; --i) {
                UUID object = RatSwarmEncounter.uuid(listTag.m_128728_(i), "id");
                if (!list.stream().noneMatch(ratBossEntity -> ratBossEntity.m_20148_().equals(object))) continue;
                listTag.remove(i);
            }
            compoundTag.m_128405_(MISSING_TICKS, 0);
        }
        if (listTag.isEmpty() || ratBossEntity2.f_19797_ % 20 == 0) {
            RatSwarmEncounter.spawnDeputy(serverLevel, ratBossEntity2);
        }
        RatSwarmEncounter.selectSwarmBar(serverLevel, ratBossEntity2);
        for (RatBossEntity ratBossEntity3 : RatSwarmEncounter.deputies(serverLevel, ratBossEntity2)) {
            RatSwarmEncounter.syncSwarmHealth(ratBossEntity2, ratBossEntity3);
        }
    }

    private static void selectSwarmBar(ServerLevel serverLevel, RatBossEntity ratBossEntity2) {
        List<RatBossEntity> list = RatSwarmEncounter.deputies(serverLevel, ratBossEntity2);
        UUID uUID = RatSwarmEncounter.uuid(ratBossEntity2.getPersistentData(), DEPUTY_ID);
        if (list.stream().noneMatch(ratBossEntity -> ratBossEntity.m_20148_().equals(uUID))) {
            if (list.isEmpty()) {
                ratBossEntity2.getPersistentData().m_128473_(DEPUTY_ID);
            } else {
                ratBossEntity2.getPersistentData().m_128362_(DEPUTY_ID, list.get(0).m_20148_());
            }
        }
    }

    private static void syncSwarmHealth(RatBossEntity ratBossEntity, RatBossEntity ratBossEntity2) {
        ratBossEntity2.m_21051_(Attributes.f_22276_).m_22100_((double)ratBossEntity.getPersistentData().m_128457_(POOL_MAX));
        ratBossEntity2.m_21153_(ratBossEntity.getPersistentData().m_128457_(POOL));
    }

    private static void beginDevices(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        CompoundTag compoundTag = ratBossEntity.getPersistentData();
        RatSwarmEncounter.clearSwarm(serverLevel, ratBossEntity);
        compoundTag.m_128405_(PHASE, 1);
        compoundTag.m_128405_(TIMER, 400);
        compoundTag.m_128473_(DEFEATED);
        compoundTag.m_128473_(DEPUTY_ID);
        ratBossEntity.m_20331_(false);
        ListTag listTag = new ListTag();
        for (int i = 0; i < 3; ++i) {
            double d = Math.PI * 2 * (double)i / 3.0;
            ArmorStand armorStand = new ArmorStand((Level)serverLevel, ratBossEntity.m_20185_(), ratBossEntity.m_20186_(), ratBossEntity.m_20189_());
            if (!BossCollision.place(serverLevel, (Entity)armorStand, ratBossEntity.m_20182_().m_82520_(Math.cos(d) * 4.0, 0.0, Math.sin(d) * 4.0), BossCollision.SpawnMode.GROUND, (float)Math.toDegrees(d))) continue;
            armorStand.m_6842_(true);
            armorStand.m_20331_(true);
            armorStand.m_20242_(true);
            armorStand.m_8061_(EquipmentSlot.HEAD, new ItemStack((ItemLike)Items.f_42768_));
            armorStand.m_20340_(true);
            armorStand.getPersistentData().m_128379_(DEVICE, true);
            armorStand.getPersistentData().m_128362_(OWNER, ratBossEntity.m_20148_());
            RatSwarmEncounter.updateDeviceName(armorStand, 400);
            if (!serverLevel.m_7967_((Entity)armorStand)) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128362_("id", armorStand.m_20148_());
            listTag.add((Object)compoundTag2);
        }
        compoundTag.m_128365_(DEVICES, (Tag)listTag);
        for (ServerPlayer serverPlayer : RatSwarmEncounter.players(serverLevel, (Entity)ratBossEntity)) {
            serverPlayer.m_5661_((Component)Component.m_237113_((String)"\u00a7e\u526f\u9f20\u7fa4\u5012\u4e0b\uff0120\u79d2\u5185\u7528\u5976\u916a\u6216\u725b\u5976\u53f3\u952e\u5582\u6ee1\u88c5\u7f6e\uff0c\u6bcf\u4e2a\u88c5\u7f6e\u9700\u89815\u4efd\u3002"), false);
        }
        FTBossMod.LOGGER.info("[FTBoss][\u9f20\u7fa4\u5faa\u73af] \u7834\u76fe\u4e0e\u88c5\u7f6e\u9636\u6bb5 master={} devices={}", (Object)ratBossEntity.m_20148_(), (Object)listTag.size());
    }

    private static void beginGuard(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        CompoundTag compoundTag = ratBossEntity.getPersistentData();
        compoundTag.m_128405_(PHASE, 0);
        compoundTag.m_128473_(DEVICES);
        compoundTag.m_128473_(DEPUTY_ID);
        compoundTag.m_128473_(DEFEATED);
        compoundTag.m_128365_(SWARM, (Tag)new ListTag());
        compoundTag.m_128362_(WAVE, UUID.randomUUID());
        compoundTag.m_128350_(POOL_MAX, RatSwarmEncounter.swarmMaximum(ratBossEntity));
        compoundTag.m_128350_(POOL, compoundTag.m_128457_(POOL_MAX));
        compoundTag.m_128473_(LAST_HIT);
        compoundTag.m_128473_(LAST_DAMAGE);
        compoundTag.m_128405_(MISSING_TICKS, 0);
        ratBossEntity.m_20331_(true);
        RatSwarmEncounter.spawnDeputy(serverLevel, ratBossEntity);
    }

    private static void updateBar(ServerLevel serverLevel, RatBossEntity ratBossEntity, boolean bl) {
        BossBars.track((Entity)ratBossEntity, ratBossEntity.getBossBar());
        boolean bl2 = ratBossEntity.getPersistentData().m_128451_(PHASE) == 1;
        ratBossEntity.getBossBar().m_6456_((Component)Component.m_237113_((String)(bl ? (bl2 ? "\u9f20\u7fa4\u4e3b\u738b \u00b7 \u88c5\u7f6e\u5012\u8ba1\u65f6 " + (ratBossEntity.getPersistentData().m_128451_(TIMER) + 19) / 20 + "\u79d2" : "\u9f20\u7fa4\u4e3b\u738b \u00b7 \u62a4\u76fe\uff08\u51fb\u8d25\u526f\u9f20\u7fa4\u7834\u76fe\uff09") : "\u9f20\u7fa4\u526f\u738b \u00b7 \u5171\u4eab\u8840\u6c60")));
        ratBossEntity.getBossBar().m_6451_(bl ? (bl2 ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE) : BossEvent.BossBarColor.YELLOW);
        ratBossEntity.getBossBar().m_142711_(Math.max(0.0f, Math.min(1.0f, ratBossEntity.m_21223_() / ratBossEntity.m_21233_())));
        List<ServerPlayer> list = RatSwarmEncounter.players(serverLevel, (Entity)ratBossEntity);
        for (ServerPlayer serverPlayer : new ArrayList(ratBossEntity.getBossBar().m_8324_())) {
            if (list.contains(serverPlayer)) continue;
            ratBossEntity.getBossBar().m_6539_(serverPlayer);
        }
        for (ServerPlayer serverPlayer : list) {
            ratBossEntity.getBossBar().m_6543_(serverPlayer);
        }
        ratBossEntity.getBossBar().m_8321_(!list.isEmpty());
    }

    private static List<ServerPlayer> players(ServerLevel serverLevel, Entity entity) {
        return serverLevel.m_6443_(ServerPlayer.class, entity.m_20191_().m_82400_(48.0), serverPlayer -> serverPlayer.m_6084_() && !serverPlayer.m_5833_() && entity.m_20280_((Entity)serverPlayer) <= 2304.0);
    }

    private static ServerPlayer nearestPlayer(ServerLevel serverLevel, Entity entity) {
        ServerPlayer serverPlayer = null;
        for (ServerPlayer serverPlayer2 : RatSwarmEncounter.players(serverLevel, entity)) {
            if (serverPlayer2.m_7500_() || serverPlayer != null && !(entity.m_20280_((Entity)serverPlayer2) < entity.m_20280_((Entity)serverPlayer))) continue;
            serverPlayer = serverPlayer2;
        }
        return serverPlayer;
    }

    private static UUID uuid(CompoundTag compoundTag, String string) {
        return compoundTag.m_128403_(string) ? compoundTag.m_128342_(string) : null;
    }

    private static RatBossEntity owner(ServerLevel serverLevel, Entity entity) {
        RatBossEntity ratBossEntity;
        UUID uUID = RatSwarmEncounter.uuid(entity.getPersistentData(), OWNER);
        Entity entity2 = uUID == null ? null : serverLevel.m_8791_(uUID);
        return entity2 instanceof RatBossEntity && RatSwarmEncounter.isMaster(ratBossEntity = (RatBossEntity)entity2) ? ratBossEntity : null;
    }

    private static boolean swarmMember(RatBossEntity ratBossEntity, RatBossEntity ratBossEntity2) {
        if (!RatSwarmEncounter.isDeputy(ratBossEntity2) || !ratBossEntity.m_20148_().equals(RatSwarmEncounter.uuid(ratBossEntity2.getPersistentData(), OWNER))) {
            return false;
        }
        ListTag listTag = ratBossEntity.getPersistentData().m_128437_(SWARM, 10);
        for (int i = 0; i < listTag.size(); ++i) {
            if (!ratBossEntity2.m_20148_().equals(RatSwarmEncounter.uuid(listTag.m_128728_(i), "id"))) continue;
            UUID uUID = RatSwarmEncounter.uuid(ratBossEntity.getPersistentData(), WAVE);
            if (uUID == null) {
                return false;
            }
            if (!ratBossEntity2.getPersistentData().m_128403_(WAVE)) {
                ratBossEntity2.getPersistentData().m_128362_(WAVE, uUID);
            }
            return uUID.equals(RatSwarmEncounter.uuid(ratBossEntity2.getPersistentData(), WAVE));
        }
        return false;
    }

    private static List<RatBossEntity> deputies(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        ArrayList<RatBossEntity> arrayList = new ArrayList<RatBossEntity>();
        ListTag listTag = ratBossEntity.getPersistentData().m_128437_(SWARM, 10);
        for (int i = 0; i < listTag.size(); ++i) {
            RatBossEntity ratBossEntity2;
            Entity entity;
            UUID uUID = RatSwarmEncounter.uuid(listTag.m_128728_(i), "id");
            Entity entity2 = entity = uUID == null ? null : serverLevel.m_8791_(uUID);
            if (!(entity instanceof RatBossEntity) || !(ratBossEntity2 = (RatBossEntity)entity).m_6084_() || !RatSwarmEncounter.swarmMember(ratBossEntity, ratBossEntity2)) continue;
            arrayList.add(ratBossEntity2);
        }
        return arrayList;
    }

    private static void clearSwarm(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        RatBossEntity ratBossEntity2;
        RatBossEntity ratBossEntity32;
        for (RatBossEntity ratBossEntity32 : RatSwarmEncounter.deputies(serverLevel, ratBossEntity)) {
            ratBossEntity32.m_146870_();
        }
        UUID uUID = RatSwarmEncounter.uuid(ratBossEntity.getPersistentData(), DEPUTY_ID);
        Object object = ratBossEntity32 = uUID == null ? null : serverLevel.m_8791_(uUID);
        if (ratBossEntity32 instanceof RatBossEntity && RatSwarmEncounter.isDeputy(ratBossEntity2 = ratBossEntity32) && RatSwarmEncounter.owner(serverLevel, (Entity)ratBossEntity2) == ratBossEntity) {
            ratBossEntity2.m_146870_();
        }
        ratBossEntity.getPersistentData().m_128473_(SWARM);
        ratBossEntity.getPersistentData().m_128473_(WAVE);
        ratBossEntity.getPersistentData().m_128473_(DEPUTY_ID);
    }

    private static List<ArmorStand> devices(ServerLevel serverLevel, RatBossEntity ratBossEntity) {
        ArrayList<ArmorStand> arrayList = new ArrayList<ArmorStand>();
        ListTag listTag = ratBossEntity.getPersistentData().m_128437_(DEVICES, 10);
        for (int i = 0; i < listTag.size(); ++i) {
            ArmorStand armorStand;
            Entity entity;
            UUID uUID = RatSwarmEncounter.uuid(listTag.m_128728_(i), "id");
            Entity entity2 = entity = uUID == null ? null : serverLevel.m_8791_(uUID);
            if (!(entity instanceof ArmorStand) || !(armorStand = (ArmorStand)entity).m_6084_() || RatSwarmEncounter.owner(serverLevel, (Entity)armorStand) != ratBossEntity || !armorStand.getPersistentData().m_128471_(DEVICE)) continue;
            arrayList.add(armorStand);
        }
        return arrayList;
    }

    public static boolean healingFood(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return false;
        }
        if (itemStack.m_204117_(HEALING_FOOD) || itemStack.m_150930_(Items.f_42455_)) {
            return true;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation != null && itemStack.m_41720_().m_41472_() && resourceLocation.m_135815_().contains("cheese");
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract entityInteract) {
        RatSwarmEncounter.feed((PlayerInteractEvent)entityInteract, entityInteract.getTarget());
    }

    @SubscribeEvent
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific entityInteractSpecific) {
        RatSwarmEncounter.feed((PlayerInteractEvent)entityInteractSpecific, entityInteractSpecific.getTarget());
    }

    private static void feed(PlayerInteractEvent playerInteractEvent, Entity entity) {
        ServerPlayer serverPlayer;
        Object object;
        ArmorStand armorStand;
        block15: {
            block14: {
                if (!(entity instanceof ArmorStand) || !(armorStand = (ArmorStand)entity).getPersistentData().m_128471_(DEVICE)) {
                    return;
                }
                playerInteractEvent.setCanceled(true);
                playerInteractEvent.setCancellationResult(InteractionResult.SUCCESS);
                object = playerInteractEvent.getEntity();
                if (!(object instanceof ServerPlayer)) break block14;
                serverPlayer = (ServerPlayer)object;
                object = armorStand.m_9236_();
                if (object instanceof ServerLevel) break block15;
            }
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = RatSwarmEncounter.owner(serverLevel, (Entity)armorStand);
        if (object == null || !object.m_6084_() || object.getPersistentData().m_128451_(PHASE) != 1 || RatSwarmRules.deviceExpired(object.getPersistentData().m_128451_(TIMER)) || !RatSwarmEncounter.devices(serverLevel, (RatBossEntity)((Object)object)).contains(armorStand) || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_20280_((Entity)armorStand) > 36.0 || !serverPlayer.m_142582_((Entity)armorStand)) {
            return;
        }
        ItemStack itemStack = serverPlayer.m_21120_(playerInteractEvent.getHand());
        if (!RatSwarmEncounter.healingFood(itemStack)) {
            serverPlayer.m_5661_((Component)Component.m_237113_((String)"\u00a7e\u9700\u8981\u5976\u916a\u6216\u725b\u5976\uff1a\u53f3\u952e\u4e3a\u88c5\u7f6e\u8865\u514520\u70b9\u5145\u80fd\u3002"), true);
            return;
        }
        CompoundTag compoundTag = armorStand.getPersistentData();
        long l = serverLevel.m_46467_();
        if (compoundTag.m_128441_(LAST_FEED) && l - compoundTag.m_128454_(LAST_FEED) < 4L) {
            return;
        }
        int n = RatSwarmRules.healDevice(compoundTag.m_128451_(DEVICE_HEALTH));
        compoundTag.m_128405_(DEVICE_HEALTH, n);
        compoundTag.m_128356_(LAST_FEED, l);
        if (!serverPlayer.m_7500_()) {
            ItemStack itemStack2 = itemStack.hasCraftingRemainingItem() ? itemStack.getCraftingRemainingItem() : ItemStack.f_41583_;
            itemStack.m_41774_(1);
            if (!itemStack2.m_41619_()) {
                if (itemStack.m_41619_()) {
                    serverPlayer.m_21008_(playerInteractEvent.getHand(), itemStack2);
                } else if (!serverPlayer.m_150109_().m_36054_(itemStack2)) {
                    serverPlayer.m_36176_(itemStack2, false);
                }
            }
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, armorStand.m_20185_(), armorStand.m_20186_() + 1.8, armorStand.m_20189_(), 4, 0.2, 0.2, 0.2, 0.0);
        RatSwarmEncounter.updateDeviceName(armorStand, object.getPersistentData().m_128451_(TIMER));
        if (RatSwarmRules.deviceComplete(n)) {
            armorStand.m_146870_();
            ((RatBossEntity)((Object)object)).f_19802_ = 0;
            ((RatBossEntity)((Object)object)).m_6469_(object.m_269291_().m_269104_((Entity)armorStand, (Entity)serverPlayer), object.m_21233_() * 0.125f);
            serverLevel.m_5594_(null, object.m_20183_(), SoundEvents.f_11912_, SoundSource.HOSTILE, 1.0f, 0.7f);
            FTBossMod.LOGGER.info("[FTBoss][\u9f20\u7fa4\u5faa\u73af] \u88c5\u7f6e\u5582\u6ee1 master={} health={}/{}", new Object[]{object.m_20148_(), Float.valueOf(object.m_21223_()), Float.valueOf(object.m_21233_())});
            if (object.m_6084_() && RatSwarmEncounter.devices(serverLevel, (RatBossEntity)((Object)object)).isEmpty()) {
                RatSwarmEncounter.beginGuard(serverLevel, (RatBossEntity)((Object)object));
            }
        }
    }

    private static void updateDeviceName(ArmorStand armorStand, int n) {
        armorStand.m_6593_((Component)Component.m_237113_((String)("\u00a7e\u5976\u916a\u88c5\u7f6e \u00a7f" + armorStand.getPersistentData().m_128451_(DEVICE_HEALTH) + "/100 \u00a7c" + (n + 19) / 20 + "\u79d2")));
    }

    private static void explodeDevice(ServerLevel serverLevel, RatBossEntity ratBossEntity, ArmorStand armorStand) {
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123813_, armorStand.m_20185_(), armorStand.m_20186_() + 1.0, armorStand.m_20189_(), 1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.m_5594_(null, armorStand.m_20183_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 0.8f, 1.0f);
        for (ServerPlayer serverPlayer : serverLevel.m_45976_(ServerPlayer.class, armorStand.m_20191_().m_82400_(4.0))) {
            if (!serverPlayer.m_6084_() || serverPlayer.m_7500_() || serverPlayer.m_5833_() || serverPlayer.m_20280_((Entity)armorStand) > 16.0) continue;
            serverPlayer.m_6469_(ratBossEntity.m_269291_().m_269333_((LivingEntity)ratBossEntity), 8.0f);
        }
        armorStand.m_146870_();
        FTBossMod.LOGGER.info("[FTBoss][\u9f20\u7fa4\u5faa\u73af] \u88c5\u7f6e\u8d85\u65f6\u7206\u70b8 master={}", (Object)ratBossEntity.m_20148_());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDeputyHurt(LivingHurtEvent livingHurtEvent) {
        RatBossEntity ratBossEntity;
        Object object = livingHurtEvent.getEntity();
        if (!(object instanceof RatBossEntity && RatSwarmEncounter.isDeputy(ratBossEntity = (RatBossEntity)((Object)object)) && (object = ratBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = RatSwarmEncounter.owner(serverLevel, (Entity)ratBossEntity);
        if (object != null) {
            RatSwarmEncounter.ensureSwarm(serverLevel, (RatBossEntity)((Object)object));
        }
        if (object == null || !object.m_6084_() || !RatSwarmEncounter.swarmMember((RatBossEntity)((Object)object), ratBossEntity) || object.getPersistentData().m_128451_(PHASE) != 0) {
            livingHurtEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDeputyDamage(LivingDamageEvent livingDamageEvent) {
        float f;
        long l;
        RatBossEntity ratBossEntity;
        Object object = livingDamageEvent.getEntity();
        if (!(object instanceof RatBossEntity && RatSwarmEncounter.isDeputy(ratBossEntity = (RatBossEntity)((Object)object)) && (object = ratBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        livingDamageEvent.setCanceled(true);
        object = RatSwarmEncounter.owner(serverLevel, (Entity)ratBossEntity);
        if (object == null || !object.m_6084_() || !RatSwarmEncounter.swarmMember((RatBossEntity)((Object)object), ratBossEntity) || object.getPersistentData().m_128451_(PHASE) != 0) {
            return;
        }
        CompoundTag compoundTag = object.getPersistentData();
        long l2 = serverLevel.m_46467_();
        long l3 = l = compoundTag.m_128441_(LAST_HIT) ? l2 - compoundTag.m_128454_(LAST_HIT) : 10L;
        if (livingDamageEvent.getSource().m_269533_(DamageTypeTags.f_273918_)) {
            l = 10L;
        }
        if ((f = RatSwarmRules.sharedHitDamage(livingDamageEvent.getAmount(), compoundTag.m_128457_(LAST_DAMAGE), l)) <= 0.0f) {
            return;
        }
        if (l < 0L || l >= 10L) {
            compoundTag.m_128356_(LAST_HIT, l2);
        }
        compoundTag.m_128350_(LAST_DAMAGE, livingDamageEvent.getAmount());
        float f2 = RatSwarmRules.damageSwarm(compoundTag.m_128457_(POOL), f);
        compoundTag.m_128350_(POOL, f2);
        if (f2 <= 0.0f) {
            compoundTag.m_128379_(DEFEATED, true);
            RatSwarmEncounter.clearSwarm(serverLevel, (RatBossEntity)((Object)object));
        } else {
            for (RatBossEntity ratBossEntity2 : RatSwarmEncounter.deputies(serverLevel, (RatBossEntity)((Object)object))) {
                RatSwarmEncounter.syncSwarmHealth((RatBossEntity)((Object)object), ratBossEntity2);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDeputyDeath(LivingDeathEvent livingDeathEvent) {
        RatBossEntity ratBossEntity;
        Object object = livingDeathEvent.getEntity();
        if (!(object instanceof RatBossEntity && RatSwarmEncounter.isDeputy(ratBossEntity = (RatBossEntity)((Object)object)) && (object = ratBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = RatSwarmEncounter.owner(serverLevel, (Entity)ratBossEntity);
        if (object != null) {
            RatSwarmEncounter.ensureSwarm(serverLevel, (RatBossEntity)((Object)object));
        }
        if (object != null && object.m_6084_() && RatSwarmEncounter.swarmMember((RatBossEntity)((Object)object), ratBossEntity) && object.getPersistentData().m_128457_(POOL) <= 0.0f) {
            object.getPersistentData().m_128379_(DEFEATED, true);
        }
    }

    @SubscribeEvent
    public static void onDeviceTick(LivingEvent.LivingTickEvent livingTickEvent) {
        ArmorStand armorStand;
        Object object = livingTickEvent.getEntity();
        if (!(object instanceof ArmorStand && (armorStand = (ArmorStand)object).getPersistentData().m_128471_(DEVICE) && (object = armorStand.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = RatSwarmEncounter.owner(serverLevel, (Entity)armorStand);
        if (object != null) {
            armorStand.getPersistentData().m_128405_(MISSING_TICKS, 0);
            if (!object.m_6084_() || object.getPersistentData().m_128451_(PHASE) != 1 || !RatSwarmEncounter.devices(serverLevel, (RatBossEntity)((Object)object)).contains(armorStand)) {
                armorStand.m_146870_();
            }
        } else {
            int n = armorStand.getPersistentData().m_128451_(MISSING_TICKS) + 1;
            armorStand.getPersistentData().m_128405_(MISSING_TICKS, n);
            if (n >= 200) {
                armorStand.m_146870_();
            }
        }
    }

    public static void applyDeputyDebuff(ServerPlayer serverPlayer) {
        BossHandler.applyRandomRatDebuff(serverPlayer);
    }

    public static void cleanup(RatBossEntity ratBossEntity) {
        ServerLevel serverLevel;
        block4: {
            block3: {
                Level level = ratBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block3;
                serverLevel = (ServerLevel)level;
                if (RatSwarmEncounter.isMaster(ratBossEntity)) break block4;
            }
            return;
        }
        RatSwarmEncounter.clearSwarm(serverLevel, ratBossEntity);
        for (ArmorStand armorStand : RatSwarmEncounter.devices(serverLevel, ratBossEntity)) {
            armorStand.m_146870_();
        }
        ratBossEntity.getPersistentData().m_128473_(DEPUTY_ID);
        ratBossEntity.getPersistentData().m_128473_(DEVICES);
        BossMusic.release(serverLevel, (LivingEntity)ratBossEntity);
    }

    public static void cleanupRemoved(RatBossEntity ratBossEntity, Entity.RemovalReason removalReason) {
        if (removalReason == Entity.RemovalReason.UNLOADED_TO_CHUNK || removalReason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            Level level = ratBossEntity.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                if (RatSwarmEncounter.isMaster(ratBossEntity)) {
                    BossMusic.release(serverLevel, (LivingEntity)ratBossEntity);
                }
            }
            return;
        }
        RatSwarmEncounter.cleanup(ratBossEntity);
    }

    public static boolean legacyBelongsTo(Entity entity, Entity entity2) {
        RatBossEntity ratBossEntity;
        if (entity2 instanceof RatBossEntity && RatSwarmEncounter.isDeputy(ratBossEntity = (RatBossEntity)entity2)) {
            return false;
        }
        return SummonSweep.belongsTo(entity, entity2);
    }
}

