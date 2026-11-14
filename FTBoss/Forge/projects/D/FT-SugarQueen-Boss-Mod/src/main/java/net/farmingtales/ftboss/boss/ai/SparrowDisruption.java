package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.SparrowDisruptionRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SparrowDisruption {
    private static final String NEXT_PULSE = "ftboss:sparrow_disruption_next";
    private static final Map<UUID, Map<Item, Long>> SILENCED = new HashMap<>();
    private static final Map<UUID, Map.Entry<ItemStack, Long>> SILENCED_STACKS = new HashMap<>();

    private SparrowDisruption() {}

    public static void tick(EasterBossEntity sparrow, EasterBossEntity boss) {
        if (!(sparrow.level() instanceof ServerLevel level) || boss == null || !sparrow.isAlive()
                || sparrow.isMini() || sparrow.getVariant() != EasterBossEntity.SPARROW_VARIANT
                || !boss.isAlive() || boss.isMini() || boss.level() != level
                || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT
                || HiddenBossIntro.isActing(boss.getUUID())) return;
        long now = level.getGameTime();
        var data = sparrow.getPersistentData();
        if (!data.contains(NEXT_PULSE) || data.getLong(NEXT_PULSE) - now > SparrowDisruptionRules.INTERVAL_TICKS) {
            data.putLong(NEXT_PULSE, now + SparrowDisruptionRules.INTERVAL_TICKS);
            return;
        }
        if (!SparrowDisruptionRules.due(now, data.getLong(NEXT_PULSE))) return;
        data.putLong(NEXT_PULSE, now + SparrowDisruptionRules.INTERVAL_TICKS);
        for (ServerPlayer player : level.players()) {
            if (!SparrowDisruptionTargets.valid(player) || player.distanceToSqr(boss)
                    > SparrowDisruptionRules.RANGE * SparrowDisruptionRules.RANGE) continue;
            disrupt(sparrow, player);
        }
    }

    private static void disrupt(EasterBossEntity sparrow, ServerPlayer player) {
        ItemStack selected = SparrowDisruptionRules.chooseHotbar(player.getInventory().items,
                stack -> !stack.isEmpty(), sparrow.getRandom()::nextInt);
        if (selected != null) {
            long until = clock(player) + SparrowDisruptionRules.SILENCE_TICKS;
            SILENCED_STACKS.put(player.getUUID(), Map.entry(selected, until));
            if (player.isUsingItem() && player.getUseItem() == selected) player.stopUsingItem();
        }
        var effects = new ArrayList<MobEffectInstance>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (SparrowDisruptionRules.removable(effect.getEffect().isBeneficial(), effect.isAmbient())) effects.add(effect);
        }
        MobEffectInstance removed = SparrowDisruptionRules.choose(effects, sparrow.getRandom()::nextInt);
        if (removed != null) player.removeEffect(removed.getEffect());
        if (selected == null && removed == null) return;
        Component message = Component.literal("§e麻雀 §7干扰：");
        if (selected != null) message = message.copy().append(selected.getHoverName()).append("§7 沉默 5 秒");
        if (removed != null) message = message.copy().append(selected == null ? "" : "；")
                .append("§7清除 ").append(removed.getEffect().getDisplayName());
        player.displayClientMessage(message, true);
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(),
                    12, 0.35, 0.45, 0.35, 0.03);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.6f, 1.6f);
        }
    }

    private static long clock(Player player) {
        return player.getServer().overworld().getGameTime();
    }

    public static void silenceItems(ServerPlayer player, java.util.List<Item> selected, int ticks) {
        if (!SparrowDisruptionTargets.valid(player) || ticks <= 0) return;
        long until = clock(player) + ticks;
        Map<Item, Long> items = SILENCED.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
        for (Item item : selected) {
            items.merge(item, until, Math::max);
            if (!player.getCooldowns().isOnCooldown(item)) player.getCooldowns().addCooldown(item, ticks);
            if (player.isUsingItem() && player.getUseItem().is(item)) player.stopUsingItem();
        }
    }

    public static boolean blocked(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer) || !SparrowDisruptionTargets.valid(player) || stack.isEmpty()) return false;
        Map.Entry<ItemStack, Long> selected = SILENCED_STACKS.get(player.getUUID());
        if (selected != null && selected.getKey() == stack
                && SparrowDisruptionRules.active(clock(player), selected.getValue())) return true;
        Map<Item, Long> items = SILENCED.get(player.getUUID());
        Long until = items == null ? null : items.get(stack.getItem());
        return until != null && SparrowDisruptionRules.active(clock(player), until);
    }

    public static void castWeaponSkill(ServerPlayer player, ResourceLocation requestedItem) {
        if (!blocked(player, player.getMainHandItem())) {
            net.farmingtales.ftboss.skill.NonManorWeaponSkills.cast(player, requestedItem);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent event) {
        if (event.isCancelable() && blocked(event.getEntity(), event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent event) {
        if (blocked(event.getEntity(), event.getEntity().getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamage(LivingAttackEvent event) {
        if (event.getSource().getDirectEntity() instanceof ServerPlayer player
                && blocked(player, player.getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (blocked(event.getPlayer(), event.getPlayer().getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && blocked(player, event.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player && blocked(player, event.getItem())) {
            if (event.isCancelable()) event.setCanceled(true);
            player.stopUsingItem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity() instanceof Player player && blocked(player, event.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.player instanceof ServerPlayer player)) return;
        Map<Item, Long> items = SILENCED.get(player.getUUID());
        long now = clock(player);
        if (items != null) {
            items.entrySet().removeIf(entry -> !SparrowDisruptionRules.active(now, entry.getValue()));
            if (!SparrowDisruptionTargets.valid(player) || items.isEmpty()) SILENCED.remove(player.getUUID());
        }
        Map.Entry<ItemStack, Long> selected = SILENCED_STACKS.get(player.getUUID());
        if (selected != null && (!SparrowDisruptionTargets.valid(player) || selected.getKey().isEmpty()
                || !SparrowDisruptionRules.active(now, selected.getValue()))) SILENCED_STACKS.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SILENCED.remove(event.getEntity().getUUID());
        SILENCED_STACKS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SILENCED.remove(player.getUUID());
            SILENCED_STACKS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SILENCED.clear();
        SILENCED_STACKS.clear();
    }
}
