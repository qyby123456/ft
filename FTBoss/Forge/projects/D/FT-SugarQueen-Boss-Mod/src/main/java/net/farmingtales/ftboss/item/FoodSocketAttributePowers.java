package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Ingredient;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Power;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Resolution;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 食材属性事件组件：四类配方与玩家级冷却；2026-10-12，不改冻结插槽核心、飞行权限或Boss伤害规则。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FoodSocketAttributePowers {
    private static final String COOLDOWNS = "FTBossFoodSocketAttributeCooldowns";
    private static final float ECHO_DAMAGE = 2.0F;
    private static final float BURST_DAMAGE = 4.0F;
    private static final double ECHO_RANGE_SQUARED = 36.0D;
    private static final double SPLASH_RADIUS = 3.0D;
    private static final double REPEL_STRENGTH = 0.6D;
    private static final int DELAY_GRACE_TICKS = 20;
    private static final Map<UUID, CombatState> STATES = new HashMap<>();
    private static final ThreadLocal<Boolean> EXTRA_HIT = ThreadLocal.withInitial(() -> false);

    private static final class CombatState {
        private final ItemStack weapon;
        private final ResourceKey<Level> dimension;
        private final Resolution recipe;
        private final int socketCount;
        private float guard;
        private long guardUntil;
        private UUID echoTarget;
        private long echoAt;
        private int hits;
        private long lastHit;

        private CombatState(ServerPlayer player, ItemStack weapon, Resolution recipe) {
            this.weapon = weapon;
            this.dimension = player.level().dimension();
            this.recipe = recipe;
            this.socketCount = FoodWeaponEvolution.socketCount(weapon);
        }
    }

    private FoodSocketAttributePowers() {}

    public static Resolution inspect(ItemStack stack) {
        List<Ingredient> installed = new ArrayList<>();
        if (stack != null && !stack.isEmpty() && FoodWeaponEvolution.isEvolutionCandidate(stack)) {
            for (FoodWeaponEvolution.SocketView socket : FoodWeaponEvolution.socketViews(stack)) {
                Ingredient ingredient = FoodSocketAttributeCatalog.ingredient(socket.stack());
                if (ingredient != null) installed.add(ingredient);
            }
        }
        return FoodSocketAttributeRules.resolve(installed);
    }

    private static String id(Power power) { return power.name().toLowerCase(Locale.ROOT); }

    private static Component title(Power power, Resolution recipe) {
        return Component.translatable("tooltip.ftboss.socket_attribute." + id(power) + ".title",
                Component.translatable(recipe.has(power) ? "tooltip.ftboss.socket_attribute.active" : "tooltip.ftboss.socket_attribute.missing"));
    }

    private static Component description(Power power, Resolution recipe) {
        String key = "tooltip.ftboss.socket_attribute." + id(power) + ".description";
        return switch (power) {
            case PRICE_GUARD -> Component.translatable(key, recipe.priceProgress(), 3, recipe.guardHealth());
            case SERIES_ECHO -> Component.translatable(key, recipe.seriesProgress(), recipe.series().isEmpty() ? "—" : recipe.series());
            case COLOR_REPEL -> Component.translatable(key, recipe.colorProgress());
            case PROCESS_BURST -> Component.translatable(key, recipe.processProgress());
        };
    }

    public static List<WeaponDetailView> details(ItemStack stack) {
        Resolution recipe = inspect(stack);
        List<WeaponDetailView> details = new ArrayList<>();
        for (Power power : Power.values()) {
            if (!recipe.has(power)) continue;
            details.add(new WeaponDetailView(Component.translatable("tooltip.ftboss.socket_attribute.category"),
                    title(power, recipe), description(power, recipe)));
        }
        return List.copyOf(details);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (!FoodWeaponEvolution.isEvolutionCandidate(event.getItemStack())) return;
        Resolution recipe = inspect(event.getItemStack());
        for (Power power : Power.values()) {
            if (!recipe.has(power)) continue;
            int progress = switch (power) {
                case PRICE_GUARD -> recipe.priceProgress();
                case SERIES_ECHO -> recipe.seriesProgress();
                case COLOR_REPEL -> recipe.colorProgress();
                case PROCESS_BURST -> recipe.processProgress();
            };
            event.getToolTip().add(Component.translatable("tooltip.ftboss.socket_attribute.progress", title(power, recipe), Math.min(3, progress)));
        }
    }

    private static long clock(ServerPlayer player) { return player.serverLevel().getServer().overworld().getGameTime(); }

    private static int cooldown(Power power) {
        return switch (power) {
            case PRICE_GUARD -> FoodSocketAttributeRules.GUARD_COOLDOWN_TICKS;
            case COLOR_REPEL -> FoodSocketAttributeRules.REPEL_COOLDOWN_TICKS;
            case SERIES_ECHO, PROCESS_BURST -> FoodSocketAttributeRules.OFFENSE_COOLDOWN_TICKS;
        };
    }

    private static boolean ready(ServerPlayer player, Power power, long now) {
        if (player.getPersistentData().contains(COOLDOWNS) && !player.getPersistentData().contains(COOLDOWNS, Tag.TAG_COMPOUND)) {
            CompoundTag repaired = new CompoundTag();
            for (Power kind : Power.values()) repaired.putLong(kind.name(), FoodSocketAttributeRules.deadline(now, cooldown(kind)));
            player.getPersistentData().put(COOLDOWNS, repaired);
            return false;
        }
        CompoundTag data = player.getPersistentData().getCompound(COOLDOWNS);
        String key = power.name();
        if (!data.contains(key)) return true;
        long stored = data.contains(key, Tag.TAG_LONG) ? data.getLong(key) : -1L;
        long bounded = FoodSocketAttributeRules.boundedCooldown(now, stored, cooldown(power));
        if (bounded != stored || !data.contains(key, Tag.TAG_LONG)) {
            data.putLong(key, bounded);
            player.getPersistentData().put(COOLDOWNS, data);
        }
        return now >= bounded;
    }

    private static void consume(ServerPlayer player, Power power, long now) {
        CompoundTag data = player.getPersistentData().getCompound(COOLDOWNS);
        data.putLong(power.name(), FoodSocketAttributeRules.deadline(now, cooldown(power)));
        player.getPersistentData().put(COOLDOWNS, data);
    }

    private static CombatState state(ServerPlayer player) {
        UUID owner = player.getUUID();
        ItemStack weapon = player.getMainHandItem();
        if (!player.isAlive() || player.isSpectator() || !FoodWeaponEvolution.isEvolutionCandidate(weapon)) {
            STATES.remove(owner);
            return null;
        }
        Resolution recipe = inspect(weapon);
        if (recipe.active().isEmpty()) {
            STATES.remove(owner);
            return null;
        }
        CombatState state = STATES.get(owner);
        if (state == null || state.weapon != weapon || state.dimension != player.level().dimension()
                || state.socketCount != FoodWeaponEvolution.socketCount(weapon)
                || !state.recipe.ingredients().equals(recipe.ingredients())) {
            state = new CombatState(player, weapon, recipe);
            STATES.put(owner, state);
        }
        return state;
    }

    private static boolean target(ServerPlayer player, LivingEntity target) {
        return target instanceof Monster && target.isAlive() && !target.isRemoved() && target.level() == player.level()
                && !target.isAlliedTo(player) && !player.isAlliedTo(target)
                && !(target instanceof TamableAnimal animal && animal.isTame())
                && !(target instanceof OwnableEntity owned && owned.getOwnerUUID() != null);
    }

    private static void cue(ServerPlayer player, Power power) {
        player.displayClientMessage(Component.translatable("message.ftboss.socket_attribute." + id(power)), true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void guard(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0F
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        CombatState state = state(player);
        if (state == null || !state.recipe.has(Power.PRICE_GUARD)) return;
        long now = clock(player);
        if (!FoodSocketAttributeRules.active(now, state.guardUntil, FoodSocketAttributeRules.GUARD_TICKS)) state.guard = 0.0F;
        if (state.guard <= 0.0F && ready(player, Power.PRICE_GUARD, now)) {
            state.guard = state.recipe.guardHealth();
            state.guardUntil = FoodSocketAttributeRules.deadline(now, FoodSocketAttributeRules.GUARD_TICKS);
            consume(player, Power.PRICE_GUARD, now);
            cue(player, Power.PRICE_GUARD);
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.4, 0.4, 0.4, 0.02);
        }
        float blocked = FoodSocketAttributeRules.absorb(event.getAmount(), state.guard);
        if (blocked > 0.0F) {
            state.guard -= blocked;
            event.setAmount(event.getAmount() - blocked);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void melee(LivingDamageEvent event) {
        if (EXTRA_HIT.get() || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !target(player, event.getEntity())) return;
        CombatState state = state(player);
        if (state == null) return;
        long now = clock(player);
        if (state.recipe.has(Power.SERIES_ECHO) && state.echoTarget == null && ready(player, Power.SERIES_ECHO, now)) {
            state.echoTarget = event.getEntity().getUUID();
            state.echoAt = FoodSocketAttributeRules.deadline(now, FoodSocketAttributeRules.ECHO_DELAY_TICKS);
            consume(player, Power.SERIES_ECHO, now);
            cue(player, Power.SERIES_ECHO);
        }
        if (state.recipe.has(Power.COLOR_REPEL) && ready(player, Power.COLOR_REPEL, now)) {
            List<Monster> enemies = player.serverLevel().getEntitiesOfClass(Monster.class,
                    player.getBoundingBox().inflate(SPLASH_RADIUS), enemy -> target(player, enemy)
                            && enemy.distanceToSqr(player) <= SPLASH_RADIUS * SPLASH_RADIUS);
            enemies.sort(Comparator.comparingDouble(enemy -> enemy.distanceToSqr(player)));
            for (Monster enemy : enemies.stream().limit(FoodSocketAttributeRules.MAX_SPLASH_TARGETS).toList()) {
                enemy.knockback(REPEL_STRENGTH, player.getX() - enemy.getX(), player.getZ() - enemy.getZ());
            }
            consume(player, Power.COLOR_REPEL, now);
            cue(player, Power.COLOR_REPEL);
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 0.5D, player.getZ(), 12, 1.0, 0.3, 1.0, 0.01);
        }
        if (state.recipe.has(Power.PROCESS_BURST) && ready(player, Power.PROCESS_BURST, now)) {
            state.hits = FoodSocketAttributeRules.advance(state.hits, state.lastHit, now);
            state.lastHit = now;
            if (state.hits >= FoodSocketAttributeRules.CHAIN_HITS) {
                state.hits = 0;
                consume(player, Power.PROCESS_BURST, now);
                burst(player, event.getEntity());
                cue(player, Power.PROCESS_BURST);
            }
        }
    }

    private static void damage(ServerPlayer player, LivingEntity victim, float amount) {
        if (!target(player, victim)) return;
        boolean previous = EXTRA_HIT.get();
        EXTRA_HIT.set(true);
        try {
            victim.hurt(player.damageSources().playerAttack(player), amount);
        } finally {
            EXTRA_HIT.set(previous);
        }
    }

    private static void burst(ServerPlayer player, LivingEntity center) {
        List<Monster> enemies = player.serverLevel().getEntitiesOfClass(Monster.class,
                center.getBoundingBox().inflate(SPLASH_RADIUS), enemy -> enemy != center && target(player, enemy)
                        && enemy.distanceToSqr(center) <= SPLASH_RADIUS * SPLASH_RADIUS);
        enemies.sort(Comparator.comparingDouble(enemy -> enemy.distanceToSqr(center)));
        for (Monster enemy : enemies.stream().limit(FoodSocketAttributeRules.MAX_SPLASH_TARGETS).toList()) damage(player, enemy, BURST_DAMAGE);
        player.serverLevel().sendParticles(ParticleTypes.CRIT, center.getX(), center.getY() + 0.5D, center.getZ(), 16, 0.8, 0.3, 0.8, 0.01);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || !STATES.containsKey(player.getUUID())) return;
        CombatState state = state(player);
        if (state == null) return;
        long now = clock(player);
        if (!FoodSocketAttributeRules.active(now, state.guardUntil, FoodSocketAttributeRules.GUARD_TICKS)) state.guard = 0.0F;
        if (state.echoTarget != null && (state.echoAt > FoodSocketAttributeRules.deadline(now, FoodSocketAttributeRules.ECHO_DELAY_TICKS)
                || now >= state.echoAt)) {
            UUID victimId = state.echoTarget;
            state.echoTarget = null;
            if (now >= state.echoAt && now - state.echoAt <= DELAY_GRACE_TICKS) {
                Entity victim = player.serverLevel().getEntity(victimId);
                if (victim instanceof LivingEntity living && target(player, living) && living.distanceToSqr(player) <= ECHO_RANGE_SQUARED) {
                    damage(player, living, ECHO_DAMAGE);
                    player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, living.getX(), living.getY() + 0.5D, living.getZ(), 6, 0.3, 0.3, 0.3, 0.01);
                }
            }
        }
    }

    private static void clear(Player player) { STATES.remove(player.getUUID()); }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) { clear(event.getEntity()); }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { clear(event.getEntity()); }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { clear(event.getEntity()); }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(COOLDOWNS)) {
            event.getEntity().getPersistentData().put(COOLDOWNS, event.getOriginal().getPersistentData().get(COOLDOWNS).copy());
        }
        clear(event.getOriginal());
        clear(event.getEntity());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        STATES.clear();
        EXTRA_HIT.remove();
    }
}
