package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.util.FoodSocketPowerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
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

/** 食物组合提供短腾空与限时友方狼；时间单位 tick，2026-10-12 新增且不授飞行权限、不绕过庄园禁飞。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodSocketPowers {
    private static final String ROOT = "FTBossFoodSocketPowers";
    private static final String HELPER = "FTBossFoodSocketHelper";
    private static final String WEAPON_TOKEN = "FTBossFoodSocketWeaponToken";
    private static final double MAX_HELPER_DISTANCE_SQUARED = 1024.0;
    private static final double MAX_TARGET_DISTANCE_SQUARED = 576.0;
    private static final int HELPER_BREED_LOCK_TICKS = 6000;

    private FoodSocketPowers() {}

    private static int count(ItemStack stack, FoodType type) { return FoodWeaponEvolution.socketTypeCount(stack, type); }
    private static boolean flight(ItemStack stack) {
        int fish = count(stack, FoodType.FISH);
        return fish >= 2 && FoodSocketPowerRules.flight(fish, count(stack, FoodType.WINE));
    }
    private static boolean pack(ItemStack stack) { return FoodSocketPowerRules.pack(count(stack, FoodType.MEAT), count(stack, FoodType.CHEESE)); }
    private static long now(ServerPlayer player) { return player.serverLevel().getServer().overworld().getGameTime(); }

    public static int setBonus(int[] counts, FoodType type) {
        if (counts.length != FoodType.values().length) return 0;
        FoodType partner = switch (type) {
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
        int own = counts[type.ordinal()];
        if (type == FoodType.SWEET || type == FoodType.FOOD) own = counts[FoodType.SWEET.ordinal()] + counts[FoodType.FOOD.ordinal()];
        return FoodSocketPowerRules.setBonus(own, counts[partner.ordinal()]);
    }

    public static List<WeaponDetailView> details(ItemStack stack) {
        int[] counts = new int[FoodType.values().length];
        for (FoodType type : FoodType.values()) counts[type.ordinal()] = count(stack, type);
        int fish = counts[FoodType.FISH.ordinal()];
        int wine = counts[FoodType.WINE.ordinal()];
        int meat = counts[FoodType.MEAT.ordinal()];
        int cheese = counts[FoodType.CHEESE.ordinal()];
        List<WeaponDetailView> details = new ArrayList<>();
        if (FoodSocketPowerRules.flight(fish, wine)) {
            details.add(new WeaponDetailView(Component.translatable("screen.ftboss.socket.combo"),
                        Component.translatable("screen.ftboss.socket.flight", Math.min(2, fish), Math.min(1, wine)),
                        Component.translatable("screen.ftboss.socket.flight.description")));
        }
        if (FoodSocketPowerRules.pack(meat, cheese)) {
            details.add(new WeaponDetailView(Component.translatable("screen.ftboss.socket.combo"),
                        Component.translatable("screen.ftboss.socket.pack", Math.min(2, meat), Math.min(1, cheese)),
                        Component.translatable("screen.ftboss.socket.pack.description")));
        }
        for (FoodType type : FoodType.values()) {
            if (setBonus(counts, type) == 0) continue;
            details.add(new WeaponDetailView(Component.translatable("screen.ftboss.socket.combo"),
                        Component.translatable("screen.ftboss.socket.set_boost"),
                        Component.translatable("screen.ftboss.socket.set_boost.description")));
            break;
        }
        return List.copyOf(details);
    }

    @SubscribeEvent
    public static void flightTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData().getCompound(ROOT);
        ItemStack weapon = player.getMainHandItem();
        long clock = now(player);
        boolean eligible = player.isAlive() && !player.isSpectator() && !player.isPassenger()
                && !player.isFallFlying() && !player.isInWaterOrBubble() && !player.onClimbable()
                && !ManorFlyBan.active(player) && FoodWeaponEvolution.isEvolutionCandidate(weapon) && flight(weapon);
        boolean running = FoodSocketPowerRules.active(clock, data.getLong("FlightUntil"), FoodSocketPowerRules.FLIGHT_DURATION_TICKS);
        if (running && (!eligible || !data.hasUUID("FlightWeapon") || !weapon.hasTag()
                || !weapon.getTag().hasUUID(WEAPON_TOKEN) || !data.getUUID("FlightWeapon").equals(weapon.getTag().getUUID(WEAPON_TOKEN)))) {
            data.remove("FlightUntil");
            data.remove("FlightWeapon");
            running = false;
        }
        boolean sneaking = player.isShiftKeyDown();
        boolean pressed = sneaking && !data.getBoolean("WasSneaking");
        data.putBoolean("WasSneaking", sneaking);
        if (!running && eligible && !player.onGround() && pressed
                && FoodSocketPowerRules.ready(clock, data.getLong("FlightCooldown"), FoodSocketPowerRules.FLIGHT_COOLDOWN_TICKS)) {
            if (!weapon.getOrCreateTag().hasUUID(WEAPON_TOKEN)) weapon.getOrCreateTag().putUUID(WEAPON_TOKEN, UUID.randomUUID());
            data.putUUID("FlightWeapon", weapon.getTag().getUUID(WEAPON_TOKEN));
            data.putLong("FlightUntil", clock + FoodSocketPowerRules.FLIGHT_DURATION_TICKS);
            data.putLong("FlightCooldown", clock + FoodSocketPowerRules.FLIGHT_COOLDOWN_TICKS);
            running = true;
            player.displayClientMessage(Component.translatable("message.ftboss.socket.flight"), true);
        }
        if (running) {
            Vec3 motion = player.getDeltaMovement();
            double vertical = sneaking ? Math.min(0.20, Math.max(0.12, motion.y)) : Math.max(-0.08, motion.y);
            player.setDeltaMovement(motion.x, vertical, motion.z);
            player.fallDistance = 0.0F;
            player.hurtMarked = true;
        }
        if (!data.isEmpty()) player.getPersistentData().put(ROOT, data);
    }

    private static boolean validTarget(ServerPlayer owner, LivingEntity target) {
        return target instanceof Monster && target.isAlive() && !target.isRemoved() && !target.isAlliedTo(owner)
                && !owner.isAlliedTo(target) && !LemonSummonRewards.suppressed(target)
                && !(target instanceof OwnableEntity owned && owned.getOwnerUUID() != null)
                && target != owner && !(target instanceof TamableAnimal) && target.level() == owner.level()
                && target.distanceToSqr(owner) <= MAX_TARGET_DISTANCE_SQUARED;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void summon(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player || !player.isAlive() || player.isSpectator()
                || !validTarget(player, event.getEntity()) || !pack(player.getMainHandItem())) return;
        long clock = now(player);
        CompoundTag data = player.getPersistentData().getCompound(ROOT);
        if (!FoodSocketPowerRules.ready(clock, data.getLong("PackCooldown"), FoodSocketPowerRules.PACK_COOLDOWN_TICKS)) return;
        if (FoodSocketPowerRules.active(clock, data.getLong("PackUntil"), FoodSocketPowerRules.PACK_DURATION_TICKS)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!weapon.getOrCreateTag().hasUUID(WEAPON_TOKEN)) weapon.getOrCreateTag().putUUID(WEAPON_TOKEN, UUID.randomUUID());
        UUID token = UUID.randomUUID();
        data.putUUID("PackToken", token);
        data.putUUID("PackWeapon", weapon.getTag().getUUID(WEAPON_TOKEN));
        data.putLong("PackUntil", clock + FoodSocketPowerRules.PACK_DURATION_TICKS);
        player.getPersistentData().put(ROOT, data);
        int spawned = 0;
        for (int index = 0; index < FoodSocketPowerRules.PACK_SIZE; index++) {
            Wolf wolf = EntityType.WOLF.create(player.serverLevel());
            if (wolf == null) continue;
            double side = index == 0 ? -1.0 : 1.0;
            wolf.moveTo(player.getX() + side, player.getY(), player.getZ(), player.getYRot(), 0.0F);
            if (!player.serverLevel().noCollision(wolf) || !player.serverLevel().getFluidState(wolf.blockPosition()).isEmpty()) {
                wolf.discard();
                continue;
            }
            wolf.setTame(true);
            wolf.setOwnerUUID(player.getUUID());
            wolf.setAge(HELPER_BREED_LOCK_TICKS);
            wolf.resetLove();
            wolf.setOrderedToSit(false);
            wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0);
            wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(12.0);
            wolf.setHealth(12.0F);
            CompoundTag helper = new CompoundTag();
            helper.putUUID("Owner", player.getUUID());
            helper.putUUID("Token", token);
            helper.putLong("Expires", clock + FoodSocketPowerRules.PACK_DURATION_TICKS);
            wolf.getPersistentData().put(HELPER, helper);
            LemonSummonRewards.mark(wolf);
            wolf.setTarget(event.getEntity());
            if (player.serverLevel().addFreshEntity(wolf)) spawned++;
            else wolf.discard();
        }
        if (spawned > 0) {
            data.putLong("PackCooldown", clock + FoodSocketPowerRules.PACK_COOLDOWN_TICKS);
            player.displayClientMessage(Component.translatable("message.ftboss.socket.pack", spawned), true);
        } else {
            data.remove("PackUntil");
            data.remove("PackToken");
            data.remove("PackWeapon");
        }
        player.getPersistentData().put(ROOT, data);
    }

    private static ServerPlayer owner(Wolf wolf) {
        CompoundTag helper = wolf.getPersistentData().getCompound(HELPER);
        if (!helper.hasUUID("Owner") || !(wolf.level() instanceof ServerLevel level)) return null;
        Player candidate = level.getServer().getPlayerList().getPlayer(helper.getUUID("Owner"));
        return candidate instanceof ServerPlayer player && player.level() == level ? player : null;
    }

    @SubscribeEvent
    public static void helperJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof Wolf wolf)
                || !wolf.getPersistentData().contains(HELPER)) return;
        if (event.loadedFromDisk()) {
            wolf.discard();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void helperTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || wolf.level().isClientSide || !wolf.getPersistentData().contains(HELPER)) return;
        ServerPlayer player = owner(wolf);
        CompoundTag helper = wolf.getPersistentData().getCompound(HELPER);
        if (player == null || !player.isAlive() || player.isSpectator() || !helper.hasUUID("Token")) {
            wolf.discard();
            return;
        }
        CompoundTag data = player.getPersistentData().getCompound(ROOT);
        if (!data.hasUUID("PackToken") || !helper.getUUID("Token").equals(data.getUUID("PackToken"))
                || !FoodSocketPowerRules.active(now(player), helper.getLong("Expires"), FoodSocketPowerRules.PACK_DURATION_TICKS)
                || wolf.distanceToSqr(player) > MAX_HELPER_DISTANCE_SQUARED || !pack(player.getMainHandItem())
                || !data.hasUUID("PackWeapon") || !player.getMainHandItem().hasTag()
                || !player.getMainHandItem().getTag().hasUUID(WEAPON_TOKEN)
                || !data.getUUID("PackWeapon").equals(player.getMainHandItem().getTag().getUUID(WEAPON_TOKEN))) {
            wolf.discard();
            return;
        }
        wolf.setAge(HELPER_BREED_LOCK_TICKS);
        wolf.resetLove();
        wolf.setOrderedToSit(false);
        if (wolf.getTarget() != null && !validTarget(player, wolf.getTarget())) wolf.setTarget(null);
    }

    private static boolean temporary(net.minecraft.world.entity.Entity entity) {
        return entity instanceof Wolf && entity.getPersistentData().contains(HELPER);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void helperInteract(PlayerInteractEvent.EntityInteract event) {
        if (!temporary(event.getTarget())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void helperInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!temporary(event.getTarget())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void helperBreed(BabyEntitySpawnEvent event) {
        if (!temporary(event.getParentA()) && !temporary(event.getParentB())) return;
        event.setCanceled(true);
        if (event.getChild() != null) event.getChild().discard();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void target(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || !wolf.getPersistentData().contains(HELPER)) return;
        ServerPlayer player = owner(wolf);
        if (player == null || event.getNewTarget() != null && !validTarget(player, event.getNewTarget())) event.setNewTarget(null);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void safeDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Wolf wolf) || !wolf.getPersistentData().contains(HELPER)) return;
        ServerPlayer player = owner(wolf);
        if (player == null || !validTarget(player, event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().contains(HELPER)) {
            event.getDrops().clear();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void experience(LivingExperienceDropEvent event) {
        if (event.getEntity().getPersistentData().contains(HELPER)) event.setDroppedExperience(0);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        CompoundTag previous = event.getOriginal().getPersistentData().getCompound(ROOT);
        CompoundTag timers = new CompoundTag();
        if (previous.contains("FlightCooldown", Tag.TAG_LONG)) timers.putLong("FlightCooldown", previous.getLong("FlightCooldown"));
        if (previous.contains("PackCooldown", Tag.TAG_LONG)) timers.putLong("PackCooldown", previous.getLong("PackCooldown"));
        event.getEntity().getPersistentData().put(ROOT, timers);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { endAbilities(event.getEntity()); }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { endAbilities(event.getEntity()); }

    private static void endAbilities(Player player) {
        CompoundTag data = player.getPersistentData().getCompound(ROOT);
        for (String key : List.of("FlightUntil", "FlightWeapon", "PackUntil", "PackToken", "PackWeapon")) data.remove(key);
        player.getPersistentData().put(ROOT, data);
    }
}
