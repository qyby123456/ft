package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.NonManorWeaponSkills;
import net.farmingtales.ftboss.util.SparrowDisruptionRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Stop;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Tick;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.GAME
)
public final class SparrowDisruption {
    private static final String NEXT_PULSE = "ftboss:sparrow_disruption_next";
    private static final Map<UUID, Map<Item, Long>> SILENCED = new HashMap<>();

    private SparrowDisruption() {
    }

    public static void tick(EasterBossEntity var0, EasterBossEntity var1) {
        if (var0.level() instanceof ServerLevel var2
            && var1 != null
            && var0.isAlive()
            && !var0.isMini()
            && var0.getVariant() == 9
            && var1.isAlive()
            && !var1.isMini()
            && var1.level() == var2
            && var1.getVariant() != 9
            && !HiddenBossIntro.isActing(var1.getUUID())) {
            long var8 = var2.getGameTime();
            CompoundTag var5 = var0.getPersistentData();
            if (var5.contains("ftboss:sparrow_disruption_next") && var5.getLong("ftboss:sparrow_disruption_next") - var8 <= 200L) {
                if (!SparrowDisruptionRules.due(var8, var5.getLong("ftboss:sparrow_disruption_next"))) {
                    return;
                }

                var5.putLong("ftboss:sparrow_disruption_next", var8 + 200L);

                for (ServerPlayer var7 : var2.players()) {
                    if (SparrowDisruptionTargets.valid(var7) && !(var7.distanceToSqr(var1) > 2304.0)) {
                        disrupt(var0, var7);
                    }
                }

                return;
            }

            var5.putLong("ftboss:sparrow_disruption_next", var8 + 200L);
            return;
        }
    }

    private static void disrupt(EasterBossEntity var0, ServerPlayer var1) {
        ArrayList var2 = new ArrayList();

        for (ItemStack var4 : var1.getInventory().items) {
            if (!var4.isEmpty()) {
                var2.add(var4);
            }
        }

        if (!var1.getOffhandItem().isEmpty()) {
            var2.add(var1.getOffhandItem());
        }

        ItemStack var9 = (ItemStack)SparrowDisruptionRules.choose(var2, var0.getRandom()::nextInt);
        if (var9 != null) {
            long var10 = clock(var1) + 20L;
            SILENCED.computeIfAbsent(var1.getUUID(), var0x -> new HashMap<>()).put(var9.getItem(), var10);
            if (!var1.getCooldowns().isOnCooldown(var9.getItem())) {
                var1.getCooldowns().addCooldown(var9.getItem(), 20);
            }

            if (var1.isUsingItem() && var1.getUseItem().is(var9.getItem())) {
                var1.stopUsingItem();
            }
        }

        ArrayList var11 = new ArrayList();

        for (MobEffectInstance var6 : var1.getActiveEffects()) {
            if (SparrowDisruptionRules.removable(var6.getEffect().value().isBeneficial(), var6.isAmbient())) {
                var11.add(var6);
            }
        }

        MobEffectInstance var12 = (MobEffectInstance)SparrowDisruptionRules.choose(var11, var0.getRandom()::nextInt);
        if (var12 != null) {
            var1.removeEffect(var12.getEffect());
        }

        if (var9 != null || var12 != null) {
            MutableComponent var13 = Component.literal("§e麻雀 §7干扰：");
            if (var9 != null) {
                var13 = var13.copy().append(var9.getHoverName()).append("§7 沉默 1 秒");
            }

            if (var12 != null) {
                var13 = var13.copy().append(var9 == null ? "" : "；").append("§7清除 ").append(var12.getEffect().value().getDisplayName());
            }

            var1.displayClientMessage(var13, true);
            if (var1.level() instanceof ServerLevel var7) {
                var7.sendParticles(ParticleTypes.ENCHANT, var1.getX(), var1.getY() + 1.0, var1.getZ(), 12, 0.35, 0.45, 0.35, 0.03);
                var7.playSound(null, var1.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.6F, 1.6F);
            }
        }
    }

    private static long clock(Player var0) {
        return var0.getServer().overworld().getGameTime();
    }

    public static boolean blocked(Player var0, ItemStack var1) {
        if (var0 instanceof ServerPlayer && SparrowDisruptionTargets.valid(var0) && !var1.isEmpty()) {
            Map var2 = SILENCED.get(var0.getUUID());
            Long var3 = var2 == null ? null : (Long)var2.get(var1.getItem());
            return var3 != null && SparrowDisruptionRules.active(clock(var0), var3);
        } else {
            return false;
        }
    }

    public static void castWeaponSkill(ServerPlayer var0, ResourceLocation var1) {
        if (!blocked(var0, var0.getMainHandItem())) {
            NonManorWeaponSkills.cast(var0, var1);
        }
    }

    public static void onInteract(PlayerInteractEvent var0) {
        if (var0 instanceof net.neoforged.bus.api.ICancellableEvent cancellable && blocked(var0.getEntity(), var0.getItemStack())) {
            if (var0 instanceof PlayerInteractEvent.RightClickBlock interaction) interaction.setCancellationResult(InteractionResult.FAIL);
            if (var0 instanceof PlayerInteractEvent.RightClickItem interaction) interaction.setCancellationResult(InteractionResult.FAIL);
            if (var0 instanceof PlayerInteractEvent.EntityInteract interaction) interaction.setCancellationResult(InteractionResult.FAIL);
            if (var0 instanceof PlayerInteractEvent.EntityInteractSpecific interaction) interaction.setCancellationResult(InteractionResult.FAIL);
            cancellable.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        onInteract(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        onInteract(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        onInteract(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        onInteract(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        onInteract(event);
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onAttack(AttackEntityEvent var0) {
        if (blocked(var0.getEntity(), var0.getEntity().getMainHandItem())) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onDamage(LivingIncomingDamageEvent var0) {
        if (var0.getSource().getDirectEntity() instanceof ServerPlayer var1 && blocked(var1, var1.getMainHandItem())) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onBreak(BreakEvent var0) {
        if (blocked(var0.getPlayer(), var0.getPlayer().getMainHandItem())) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onUseStart(Start var0) {
        if (var0.getEntity() instanceof Player var1 && blocked(var1, var0.getItem())) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onUseTick(Tick var0) {
        if (var0.getEntity() instanceof Player var1 && blocked(var1, var0.getItem())) {
            var0.setCanceled(true);

            var1.stopUsingItem();
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onUseStop(Stop var0) {
        if (var0.getEntity() instanceof Player var1 && blocked(var1, var0.getItem())) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            Map<Item, Long> var5 = SILENCED.get(var1.getUUID());
            if (var5 != null) {
                long var3 = clock(var1);
                var5.entrySet().removeIf(var2 -> !SparrowDisruptionRules.active(var3, (Long)var2.getValue()));
                if (!SparrowDisruptionTargets.valid(var1) || var5.isEmpty()) {
                    SILENCED.remove(var1.getUUID());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerLoggedOutEvent var0) {
        SILENCED.remove(var0.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            SILENCED.remove(var1.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent var0) {
        SILENCED.clear();
    }

    public static void silenceItems(ServerPlayer var0, List<Item> var1, int var2) {
        if (SparrowDisruptionTargets.valid(var0) && var2 > 0) {
            long var3 = clock(var0) + (long)var2;
            Map<Item, Long> var5 = SILENCED.computeIfAbsent(var0.getUUID(), var0x -> new HashMap<>());

            for (Item var7 : var1) {
                var5.merge(var7, var3, Math::max);
                if (!var0.getCooldowns().isOnCooldown(var7)) {
                    var0.getCooldowns().addCooldown(var7, var2);
                }

                if (var0.isUsingItem() && var0.getUseItem().is(var7)) {
                    var0.stopUsingItem();
                }
            }
        }
    }
}
