package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.BossHandler;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** 主从鼠王、限时奶酪装置、逐场归属与存档恢复，2026-10-12。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
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
    private static final TagKey<Item> HEALING_FOOD = TagKey.create(Registries.ITEM,
            new ResourceLocation("ftboss", "rat_swarm_healing_food"));
    private static final double BAR_RANGE = 48.0;
    private static final int MISSING_GRACE = 200;

    private RatSwarmEncounter() {}

    public static boolean isDeputy(RatBossEntity rat) {
        return rat.getPersistentData().getBoolean(DEPUTY);
    }

    public static boolean isMaster(RatBossEntity rat) {
        return !rat.isMinion();
    }

    public static boolean skipEmpower(net.minecraft.world.entity.LivingEntity entity) {
        return entity instanceof RatBossEntity rat && (isMaster(rat) || isDeputy(rat));
    }

    public static boolean skipSharedHealth(LivingHurtEvent event) {
        return event.getEntity() instanceof RatBossEntity rat && isDeputy(rat);
    }

    public static boolean handlePlayerHit(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide
                || !(event.getSource().getEntity() instanceof RatBossEntity rat)) return false;
        if (!isDeputy(rat) && rat.isMinion()) return false;
        BossMercy.noteThreat(player, rat, player.level().getGameTime());
        if (isDeputy(rat)) applyDeputyDebuff(player);
        return true;
    }

    public static boolean skipVictory(Entity entity) {
        return entity instanceof RatBossEntity rat && rat.isMinion();
    }

    public static void appendTooltip(List<Component> tooltip) {
        tooltip.add(Component.literal("§7击败副王破盾，再用奶酪或牛奶喂满限时装置"));
        tooltip.add(Component.literal("§8单次伤害达到主王最大生命可直接穿盾"));
    }

    public static void prepare(RatBossEntity rat) {
        if (rat.level().isClientSide || !isMaster(rat)) return;
        if (!rat.getPersistentData().getBoolean(READY)) initialize(rat);
        rat.setNoAi(true);
        rat.setInvulnerable(true);
    }

    public static boolean blocksDamage(RatBossEntity rat, DamageSource source, float damage) {
        if (rat.level().isClientSide) return false;
        if (!Float.isFinite(damage) || damage <= 0) return true;
        if (!isMaster(rat)) {
            if (isDeputy(rat)) rat.invulnerableTime = 0;
            return false;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (rat.getPersistentData().getInt(PHASE) == RatSwarmRules.DEVICE_PHASE) return false;
        float maximum = rat.getPersistentData().getBoolean(READY) ? rat.getMaxHealth()
                : (float) RatSwarmRules.MASTER_HEALTH;
        if (RatSwarmRules.lethalBurst(damage, maximum, source.getEntity() instanceof ServerPlayer player
                && player.isAlive() && !player.isSpectator())) {
            rat.setInvulnerable(false);
            return false;
        }
        return true;
    }

    public static boolean tick(RatBossEntity rat) {
        if (!(rat.level() instanceof ServerLevel level)) return false;
        if (isDeputy(rat)) {
            tickDeputy(level, rat);
            if (rat.isAlive() && !rat.isRemoved()) rat.tickEncounterMelee();
            return true;
        }
        if (!isMaster(rat)) return false;
        if (!rat.isAlive()) return true;
        CompoundTag data = rat.getPersistentData();
        if (!data.getBoolean(READY)) initialize(rat);
        rat.setNoAi(true);
        rat.setTarget(null);
        rat.getNavigation().stop();
        boolean exposed = data.getInt(PHASE) == RatSwarmRules.DEVICE_PHASE;
        rat.setInvulnerable(!exposed);
        if (exposed) {
            int remaining = Math.max(0, data.getInt(TIMER) - 1);
            data.putInt(TIMER, remaining);
            List<ArmorStand> active = devices(level, rat);
            if (RatSwarmRules.deviceExpired(remaining)) {
                for (ArmorStand stand : active) explodeDevice(level, rat, stand);
                active.clear();
            }
            if (active.isEmpty()) beginGuard(level, rat);
            else if (rat.tickCount % 20 == 0) {
                for (ArmorStand stand : active) updateDeviceName(stand, remaining);
            }
        } else {
            ensureSwarm(level, rat);
            if (data.getBoolean(DEFEATED) || data.getFloat(POOL) <= 0) beginDevices(level, rat);
            else maintainSwarm(level, rat);
        }
        updateBar(level, rat, true);
        if (rat.tickCount % 20 == 0) {
            net.farmingtales.ftboss.sound.BossMusic.request(level, rat);
            for (MobEffectInstance effect : new ArrayList<>(rat.getActiveEffects())) {
                if (!effect.getEffect().isBeneficial()) rat.removeEffect(effect.getEffect());
            }
        }
        return true;
    }

    private static void initialize(RatBossEntity master) {
        float ratio = master.getMaxHealth() > 0 ? master.getHealth() / master.getMaxHealth() : 1;
        double maximum = RatSwarmRules.MASTER_HEALTH * (NonManorFinaleDifficulty.isFinale(master) ? 2.0 : 1.0);
        master.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maximum);
        master.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0);
        master.getAttribute(Attributes.ARMOR).setBaseValue(0);
        master.setHealth(Math.max(1, master.getMaxHealth() * ratio));
        CompoundTag data = master.getPersistentData();
        data.putDouble("ftboss_base_hp", maximum);
        data.putDouble("ftboss_base_atk", 0);
        data.putDouble("ftboss:regular_finale_base/minecraft:generic.max_health", RatSwarmRules.MASTER_HEALTH);
        data.putBoolean(READY, true);
        data.putInt(PHASE, RatSwarmRules.GUARD_PHASE);
        data.remove(DEPUTY_ID);
        data.remove(DEFEATED);
        data.remove(SWARM);
        data.remove(WAVE);
        data.remove(POOL);
        data.remove(POOL_MAX);
        data.remove(LAST_HIT);
        data.remove(LAST_DAMAGE);
        master.setNoAi(true);
        master.setInvulnerable(true);
    }

    private static void tickDeputy(ServerLevel level, RatBossEntity rat) {
        if (!rat.isAlive()) return;
        RatBossEntity master = owner(level, rat);
        if (master == null) {
            int missing = rat.getPersistentData().getInt(MISSING_TICKS) + 1;
            rat.getPersistentData().putInt(MISSING_TICKS, missing);
            if (missing >= MISSING_GRACE) rat.discard();
            return;
        }
        if (master.getPersistentData().getInt(PHASE) == RatSwarmRules.GUARD_PHASE) ensureSwarm(level, master);
        if (!master.isAlive() || master.getPersistentData().getInt(PHASE) != RatSwarmRules.GUARD_PHASE
                || !swarmMember(master, rat)) {
            rat.discard();
            return;
        }
        rat.getPersistentData().putInt(MISSING_TICKS, 0);
        rat.setNoAi(false);
        rat.setInvulnerable(false);
        if (rat.tickCount % 20 == 0) rat.setTarget(nearestPlayer(level, rat));
        syncSwarmHealth(master, rat);
        for (MobEffectInstance effect : new ArrayList<>(rat.getActiveEffects())) {
            if (!effect.getEffect().isBeneficial()) rat.removeEffect(effect.getEffect());
        }
        if (rat.getUUID().equals(uuid(master.getPersistentData(), DEPUTY_ID))) updateBar(level, rat, false);
        else rat.hideBossBar();
    }

    private static void spawnDeputy(ServerLevel level, RatBossEntity master) {
        int count = RatSwarmRules.swarmSize(master.getPersistentData().getFloat(POOL), master.getPersistentData().getFloat(POOL_MAX));
        ListTag entries = master.getPersistentData().getList(SWARM, Tag.TAG_COMPOUND);
        int attempts = count - entries.size();
        for (int index = 0; index < attempts; index++) spawnMember(level, master, entries.size(), count);
        selectSwarmBar(level, master);
    }

    private static void spawnMember(ServerLevel level, RatBossEntity master, int index, int count) {
        RatBossEntity rat = (RatBossEntity) master.getType().create(level);
        if (rat == null) return;
        rat.setMinion(true);
        rat.getPersistentData().putBoolean(DEPUTY, true);
        rat.getPersistentData().putUUID(OWNER, master.getUUID());
        rat.getPersistentData().putUUID(WAVE, master.getPersistentData().getUUID(WAVE));
        double multiplier = NonManorFinaleDifficulty.isFinale(master) ? 2.0 : 1.0;
        rat.getPersistentData().putDouble("ftboss_base_hp", RatSwarmRules.DEPUTY_HEALTH * multiplier);
        rat.getPersistentData().putDouble("ftboss_base_atk", RatSwarmRules.DEPUTY_DAMAGE);
        rat.getPersistentData().putDouble("ftboss:regular_finale_base/minecraft:generic.max_health", RatSwarmRules.DEPUTY_HEALTH);
        rat.getAttribute(Attributes.MAX_HEALTH).setBaseValue(master.getPersistentData().getFloat(POOL_MAX));
        rat.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(RatSwarmRules.DEPUTY_DAMAGE);
        rat.getAttribute(Attributes.ARMOR).setBaseValue(0);
        rat.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.24);
        rat.setHealth(master.getPersistentData().getFloat(POOL));
        rat.setNoAi(false);
        rat.setInvulnerable(false);
        rat.setPersistenceRequired();
        rat.setCustomName(Component.translatable("entity.ftboss.rat_minion"));
        rat.setCustomNameVisible(true);
        double angle = Math.PI * 2 * index / count;
        double radius = 2.5 + master.getRandom().nextDouble() * 4;
        if (!BossCollision.place(level, rat, master.position().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius),
                BossCollision.SpawnMode.GROUND, (float) Math.toDegrees(angle))) return;
        ManorFightMembers.inherit(master, rat);
        MinionBlessing.mark(rat, master.getUUID());
        LemonSummonRewards.mark(rat);
        if (!level.addFreshEntity(rat)) return;
        ListTag entries = master.getPersistentData().getList(SWARM, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putUUID("id", rat.getUUID());
        entries.add(entry);
        master.getPersistentData().put(SWARM, entries);
        rat.setTarget(nearestPlayer(level, rat));
    }

    private static float swarmMaximum(RatBossEntity master) {
        return (float) (RatSwarmRules.DEPUTY_HEALTH * master.getMaxHealth() / RatSwarmRules.MASTER_HEALTH);
    }

    private static void ensureSwarm(ServerLevel level, RatBossEntity master) {
        CompoundTag data = master.getPersistentData();
        if (!data.hasUUID(WAVE)) data.putUUID(WAVE, UUID.randomUUID());
        if (!data.contains(SWARM)) {
            ListTag entries = new ListTag();
            UUID legacy = uuid(data, DEPUTY_ID);
            if (legacy != null) {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("id", legacy);
                entries.add(entry);
            }
            data.put(SWARM, entries);
        }
        if (!data.contains(POOL)) {
            float maximum = swarmMaximum(master);
            Entity legacy = data.hasUUID(DEPUTY_ID) ? level.getEntity(data.getUUID(DEPUTY_ID)) : null;
            float health = legacy instanceof RatBossEntity rat && isDeputy(rat) && owner(level, rat) == master
                    ? RatSwarmRules.scaleSwarm(rat.getHealth(), rat.getMaxHealth(), maximum) : maximum;
            data.putFloat(POOL_MAX, maximum);
            data.putFloat(POOL, health);
        }
    }

    private static void maintainSwarm(ServerLevel level, RatBossEntity master) {
        CompoundTag data = master.getPersistentData();
        float maximum = swarmMaximum(master);
        data.putFloat(POOL, RatSwarmRules.scaleSwarm(data.getFloat(POOL), data.getFloat(POOL_MAX), maximum));
        data.putFloat(POOL_MAX, maximum);
        ListTag entries = data.getList(SWARM, Tag.TAG_COMPOUND);
        List<RatBossEntity> loaded = deputies(level, master);
        int missing = loaded.size() < entries.size() ? data.getInt(MISSING_TICKS) + 1 : 0;
        data.putInt(MISSING_TICKS, missing);
        if (missing >= MISSING_GRACE) {
            for (int index = entries.size() - 1; index >= 0; index--) {
                UUID id = uuid(entries.getCompound(index), "id");
                if (loaded.stream().noneMatch(rat -> rat.getUUID().equals(id))) entries.remove(index);
            }
            data.putInt(MISSING_TICKS, 0);
        }
        if (entries.isEmpty() || master.tickCount % 20 == 0) spawnDeputy(level, master);
        selectSwarmBar(level, master);
        for (RatBossEntity rat : deputies(level, master)) syncSwarmHealth(master, rat);
    }

    private static void selectSwarmBar(ServerLevel level, RatBossEntity master) {
        List<RatBossEntity> loaded = deputies(level, master);
        UUID selected = uuid(master.getPersistentData(), DEPUTY_ID);
        if (loaded.stream().noneMatch(rat -> rat.getUUID().equals(selected))) {
            if (loaded.isEmpty()) master.getPersistentData().remove(DEPUTY_ID);
            else master.getPersistentData().putUUID(DEPUTY_ID, loaded.get(0).getUUID());
        }
    }

    private static void syncSwarmHealth(RatBossEntity master, RatBossEntity rat) {
        rat.getAttribute(Attributes.MAX_HEALTH).setBaseValue(master.getPersistentData().getFloat(POOL_MAX));
        rat.setHealth(master.getPersistentData().getFloat(POOL));
    }

    private static void beginDevices(ServerLevel level, RatBossEntity master) {
        CompoundTag data = master.getPersistentData();
        clearSwarm(level, master);
        data.putInt(PHASE, RatSwarmRules.DEVICE_PHASE);
        data.putInt(TIMER, RatSwarmRules.DEVICE_TICKS);
        data.remove(DEFEATED);
        data.remove(DEPUTY_ID);
        master.setInvulnerable(false);
        ListTag ids = new ListTag();
        for (int index = 0; index < RatSwarmRules.DEVICE_COUNT; index++) {
            double angle = Math.PI * 2 * index / RatSwarmRules.DEVICE_COUNT;
            ArmorStand stand = new ArmorStand(level, master.getX(), master.getY(), master.getZ());
            if (!BossCollision.place(level, stand, master.position().add(Math.cos(angle) * 4, 0, Math.sin(angle) * 4),
                    BossCollision.SpawnMode.GROUND, (float) Math.toDegrees(angle))) continue;
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setNoGravity(true);
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.BARREL));
            stand.setCustomNameVisible(true);
            stand.getPersistentData().putBoolean(DEVICE, true);
            stand.getPersistentData().putUUID(OWNER, master.getUUID());
            updateDeviceName(stand, RatSwarmRules.DEVICE_TICKS);
            if (level.addFreshEntity(stand)) {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("id", stand.getUUID());
                ids.add(entry);
            }
        }
        data.put(DEVICES, ids);
        for (ServerPlayer player : players(level, master)) {
            player.displayClientMessage(Component.literal("§e副鼠群倒下！20秒内用奶酪或牛奶右键喂满装置，每个装置需要5份。"), false);
        }
        FTBossMod.LOGGER.info("[FTBoss][鼠群循环] 破盾与装置阶段 master={} devices={}", master.getUUID(), ids.size());
    }

    private static void beginGuard(ServerLevel level, RatBossEntity master) {
        CompoundTag data = master.getPersistentData();
        data.putInt(PHASE, RatSwarmRules.GUARD_PHASE);
        data.remove(DEVICES);
        data.remove(DEPUTY_ID);
        data.remove(DEFEATED);
        data.put(SWARM, new ListTag());
        data.putUUID(WAVE, UUID.randomUUID());
        data.putFloat(POOL_MAX, swarmMaximum(master));
        data.putFloat(POOL, data.getFloat(POOL_MAX));
        data.remove(LAST_HIT);
        data.remove(LAST_DAMAGE);
        data.putInt(MISSING_TICKS, 0);
        master.setInvulnerable(true);
        spawnDeputy(level, master);
    }

    private static void updateBar(ServerLevel level, RatBossEntity rat, boolean master) {
        BossBars.track(rat, rat.getBossBar());
        boolean exposed = rat.getPersistentData().getInt(PHASE) == RatSwarmRules.DEVICE_PHASE;
        rat.getBossBar().setName(Component.literal(master ? exposed
                ? "鼠群主王 · 装置倒计时 " + ((rat.getPersistentData().getInt(TIMER) + 19) / 20) + "秒"
                : "鼠群主王 · 护盾（击败副鼠群破盾）" : "鼠群副王 · 共享血池"));
        rat.getBossBar().setColor(master ? exposed ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE
                : BossEvent.BossBarColor.YELLOW);
        rat.getBossBar().setProgress(Math.max(0, Math.min(1, rat.getHealth() / rat.getMaxHealth())));
        List<ServerPlayer> nearby = players(level, rat);
        for (ServerPlayer old : new ArrayList<>(rat.getBossBar().getPlayers())) {
            if (!nearby.contains(old)) rat.getBossBar().removePlayer(old);
        }
        for (ServerPlayer player : nearby) rat.getBossBar().addPlayer(player);
        rat.getBossBar().setVisible(!nearby.isEmpty());
    }

    private static List<ServerPlayer> players(ServerLevel level, Entity entity) {
        return level.getEntitiesOfClass(ServerPlayer.class, entity.getBoundingBox().inflate(BAR_RANGE),
                player -> player.isAlive() && !player.isSpectator() && entity.distanceToSqr(player) <= BAR_RANGE * BAR_RANGE);
    }

    private static ServerPlayer nearestPlayer(ServerLevel level, Entity entity) {
        ServerPlayer nearest = null;
        for (ServerPlayer candidate : players(level, entity)) {
            if (candidate.isCreative()) continue;
            if (nearest == null || entity.distanceToSqr(candidate) < entity.distanceToSqr(nearest)) nearest = candidate;
        }
        return nearest;
    }

    private static UUID uuid(CompoundTag data, String key) {
        return data.hasUUID(key) ? data.getUUID(key) : null;
    }

    private static RatBossEntity owner(ServerLevel level, Entity child) {
        UUID id = uuid(child.getPersistentData(), OWNER);
        Entity entity = id == null ? null : level.getEntity(id);
        return entity instanceof RatBossEntity rat && isMaster(rat) ? rat : null;
    }

    private static boolean swarmMember(RatBossEntity master, RatBossEntity rat) {
        if (!isDeputy(rat) || !master.getUUID().equals(uuid(rat.getPersistentData(), OWNER))) return false;
        ListTag entries = master.getPersistentData().getList(SWARM, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            if (!rat.getUUID().equals(uuid(entries.getCompound(index), "id"))) continue;
            UUID wave = uuid(master.getPersistentData(), WAVE);
            if (wave == null) return false;
            if (!rat.getPersistentData().hasUUID(WAVE)) rat.getPersistentData().putUUID(WAVE, wave);
            return wave.equals(uuid(rat.getPersistentData(), WAVE));
        }
        return false;
    }

    private static List<RatBossEntity> deputies(ServerLevel level, RatBossEntity master) {
        List<RatBossEntity> result = new ArrayList<>();
        ListTag entries = master.getPersistentData().getList(SWARM, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            UUID id = uuid(entries.getCompound(index), "id");
            Entity entity = id == null ? null : level.getEntity(id);
            if (entity instanceof RatBossEntity rat && rat.isAlive() && swarmMember(master, rat)) result.add(rat);
        }
        return result;
    }

    private static void clearSwarm(ServerLevel level, RatBossEntity master) {
        for (RatBossEntity rat : deputies(level, master)) rat.discard();
        UUID legacy = uuid(master.getPersistentData(), DEPUTY_ID);
        Entity entity = legacy == null ? null : level.getEntity(legacy);
        if (entity instanceof RatBossEntity rat && isDeputy(rat) && owner(level, rat) == master) rat.discard();
        master.getPersistentData().remove(SWARM);
        master.getPersistentData().remove(WAVE);
        master.getPersistentData().remove(DEPUTY_ID);
    }

    private static List<ArmorStand> devices(ServerLevel level, RatBossEntity master) {
        List<ArmorStand> result = new ArrayList<>();
        ListTag entries = master.getPersistentData().getList(DEVICES, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            UUID id = uuid(entries.getCompound(index), "id");
            Entity entity = id == null ? null : level.getEntity(id);
            if (entity instanceof ArmorStand stand && stand.isAlive() && owner(level, stand) == master
                    && stand.getPersistentData().getBoolean(DEVICE)) result.add(stand);
        }
        return result;
    }

    public static boolean healingFood(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(HEALING_FOOD) || stack.is(Items.MILK_BUCKET)) return true;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && stack.getItem().isEdible() && id.getPath().contains("cheese");
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        feed(event, event.getTarget());
    }

    @SubscribeEvent
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        feed(event, event.getTarget());
    }

    private static void feed(PlayerInteractEvent event, Entity target) {
        if (!(target instanceof ArmorStand stand) || !stand.getPersistentData().getBoolean(DEVICE)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player) || !(stand.level() instanceof ServerLevel level)) return;
        RatBossEntity master = owner(level, stand);
        if (master == null || !master.isAlive() || master.getPersistentData().getInt(PHASE) != RatSwarmRules.DEVICE_PHASE
                || RatSwarmRules.deviceExpired(master.getPersistentData().getInt(TIMER)) || !devices(level, master).contains(stand)
                || !player.isAlive() || player.isSpectator() || player.distanceToSqr(stand) > 36 || !player.hasLineOfSight(stand)) return;
        ItemStack held = player.getItemInHand(event.getHand());
        if (!healingFood(held)) {
            player.displayClientMessage(Component.literal("§e需要奶酪或牛奶：右键为装置补充20点充能。"), true);
            return;
        }
        CompoundTag data = stand.getPersistentData();
        long now = level.getGameTime();
        if (data.contains(LAST_FEED) && now - data.getLong(LAST_FEED) < RatSwarmRules.FEED_COOLDOWN) return;
        int health = RatSwarmRules.healDevice(data.getInt(DEVICE_HEALTH));
        data.putInt(DEVICE_HEALTH, health);
        data.putLong(LAST_FEED, now);
        if (!player.isCreative()) {
            ItemStack container = held.hasCraftingRemainingItem() ? held.getCraftingRemainingItem() : ItemStack.EMPTY;
            held.shrink(1);
            if (!container.isEmpty()) {
                if (held.isEmpty()) player.setItemInHand(event.getHand(), container);
                else if (!player.getInventory().add(container)) player.drop(container, false);
            }
        }
        level.sendParticles(ParticleTypes.HEART, stand.getX(), stand.getY() + 1.8, stand.getZ(), 4, 0.2, 0.2, 0.2, 0);
        updateDeviceName(stand, master.getPersistentData().getInt(TIMER));
        if (RatSwarmRules.deviceComplete(health)) {
            stand.discard();
            master.invulnerableTime = 0;
            master.hurt(master.damageSources().indirectMagic(stand, player), master.getMaxHealth() * RatSwarmRules.DEVICE_DAMAGE_FRACTION);
            level.playSound(null, master.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1, 0.7F);
            FTBossMod.LOGGER.info("[FTBoss][鼠群循环] 装置喂满 master={} health={}/{}", master.getUUID(), master.getHealth(), master.getMaxHealth());
            if (master.isAlive() && devices(level, master).isEmpty()) beginGuard(level, master);
        }
    }

    private static void updateDeviceName(ArmorStand stand, int remaining) {
        stand.setCustomName(Component.literal("§e奶酪装置 §f" + stand.getPersistentData().getInt(DEVICE_HEALTH)
                + "/100 §c" + ((remaining + 19) / 20) + "秒"));
    }

    private static void explodeDevice(ServerLevel level, RatBossEntity master, ArmorStand stand) {
        level.sendParticles(ParticleTypes.EXPLOSION, stand.getX(), stand.getY() + 1, stand.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, stand.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.8F, 1);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
                stand.getBoundingBox().inflate(RatSwarmRules.EXPLOSION_RADIUS))) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()
                    || player.distanceToSqr(stand) > RatSwarmRules.EXPLOSION_RADIUS * RatSwarmRules.EXPLOSION_RADIUS) continue;
            player.hurt(master.damageSources().mobAttack(master), RatSwarmRules.EXPLOSION_DAMAGE);
        }
        stand.discard();
        FTBossMod.LOGGER.info("[FTBoss][鼠群循环] 装置超时爆炸 master={}", master.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeputyHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof RatBossEntity rat) || !isDeputy(rat)
                || !(rat.level() instanceof ServerLevel level)) return;
        RatBossEntity master = owner(level, rat);
        if (master != null) ensureSwarm(level, master);
        if (master == null || !master.isAlive() || !swarmMember(master, rat)
                || master.getPersistentData().getInt(PHASE) != RatSwarmRules.GUARD_PHASE) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeputyDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof RatBossEntity rat) || !isDeputy(rat)
                || !(rat.level() instanceof ServerLevel level)) return;
        event.setCanceled(true);
        RatBossEntity master = owner(level, rat);
        if (master == null || !master.isAlive() || !swarmMember(master, rat)
                || master.getPersistentData().getInt(PHASE) != RatSwarmRules.GUARD_PHASE) return;
        CompoundTag data = master.getPersistentData();
        long now = level.getGameTime();
        long elapsed = data.contains(LAST_HIT) ? now - data.getLong(LAST_HIT) : 10;
        if (event.getSource().is(DamageTypeTags.BYPASSES_COOLDOWN)) elapsed = 10;
        float damage = RatSwarmRules.sharedHitDamage(event.getAmount(), data.getFloat(LAST_DAMAGE), elapsed);
        if (damage <= 0) return;
        if (elapsed < 0 || elapsed >= 10) data.putLong(LAST_HIT, now);
        data.putFloat(LAST_DAMAGE, event.getAmount());
        float health = RatSwarmRules.damageSwarm(data.getFloat(POOL), damage);
        data.putFloat(POOL, health);
        if (health <= 0) {
            data.putBoolean(DEFEATED, true);
            clearSwarm(level, master);
        } else {
            for (RatBossEntity member : deputies(level, master)) syncSwarmHealth(master, member);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeputyDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RatBossEntity rat) || !isDeputy(rat)
                || !(rat.level() instanceof ServerLevel level)) return;
        RatBossEntity master = owner(level, rat);
        if (master != null) ensureSwarm(level, master);
        if (master != null && master.isAlive() && swarmMember(master, rat)
                && master.getPersistentData().getFloat(POOL) <= 0) {
            master.getPersistentData().putBoolean(DEFEATED, true);
        }
    }

    @SubscribeEvent
    public static void onDeviceTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand) || !stand.getPersistentData().getBoolean(DEVICE)
                || !(stand.level() instanceof ServerLevel level)) return;
        RatBossEntity master = owner(level, stand);
        if (master != null) {
            stand.getPersistentData().putInt(MISSING_TICKS, 0);
            if (!master.isAlive() || master.getPersistentData().getInt(PHASE) != RatSwarmRules.DEVICE_PHASE
                    || !devices(level, master).contains(stand)) stand.discard();
        } else {
            int missing = stand.getPersistentData().getInt(MISSING_TICKS) + 1;
            stand.getPersistentData().putInt(MISSING_TICKS, missing);
            if (missing >= MISSING_GRACE) stand.discard();
        }
    }

    public static void applyDeputyDebuff(ServerPlayer player) {
        BossHandler.applyRandomRatDebuff(player);
    }

    public static void cleanup(RatBossEntity master) {
        if (!(master.level() instanceof ServerLevel level) || !isMaster(master)) return;
        clearSwarm(level, master);
        for (ArmorStand stand : devices(level, master)) stand.discard();
        master.getPersistentData().remove(DEPUTY_ID);
        master.getPersistentData().remove(DEVICES);
        net.farmingtales.ftboss.sound.BossMusic.release(level, master);
    }

    public static void cleanupRemoved(RatBossEntity master, Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.UNLOADED_TO_CHUNK || reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            if (master.level() instanceof ServerLevel level && isMaster(master)) {
                net.farmingtales.ftboss.sound.BossMusic.release(level, master);
            }
            return;
        }
        cleanup(master);
    }

    public static boolean legacyBelongsTo(Entity owner, Entity child) {
        if (child instanceof RatBossEntity rat && isDeputy(rat)) return false;
        return SummonSweep.belongsTo(owner, child);
    }
}
