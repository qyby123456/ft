/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Start
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Stop
 *  net.minecraftforge.event.entity.living.LivingEntityUseItemEvent$Tick
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ai.SparrowDisruptionTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.NonManorWeaponSkills;
import net.farmingtales.ftboss.util.SparrowDisruptionRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class SparrowDisruption {
    private static final String NEXT_PULSE = "ftboss:sparrow_disruption_next";
    private static final Map<UUID, Map<Item, Long>> SILENCED = new HashMap<UUID, Map<Item, Long>>();

    private SparrowDisruption() {
    }

    public static void tick(EasterBossEntity easterBossEntity, EasterBossEntity easterBossEntity2) {
        ServerLevel serverLevel;
        block7: {
            block6: {
                Level level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block6;
                serverLevel = (ServerLevel)level;
                if (easterBossEntity2 != null && easterBossEntity.m_6084_() && !easterBossEntity.isMini() && easterBossEntity.getVariant() == 9 && easterBossEntity2.m_6084_() && !easterBossEntity2.isMini() && easterBossEntity2.m_9236_() == serverLevel && easterBossEntity2.getVariant() != 9 && !HiddenBossIntro.isActing(easterBossEntity2.m_20148_())) break block7;
            }
            return;
        }
        long l = serverLevel.m_46467_();
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        if (!compoundTag.m_128441_(NEXT_PULSE) || compoundTag.m_128454_(NEXT_PULSE) - l > 200L) {
            compoundTag.m_128356_(NEXT_PULSE, l + 200L);
            return;
        }
        if (!SparrowDisruptionRules.due(l, compoundTag.m_128454_(NEXT_PULSE))) {
            return;
        }
        compoundTag.m_128356_(NEXT_PULSE, l + 200L);
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (!SparrowDisruptionTargets.valid((Player)serverPlayer) || serverPlayer.m_20280_((Entity)easterBossEntity2) > 2304.0) continue;
            SparrowDisruption.disrupt(easterBossEntity, serverPlayer);
        }
    }

    private static void disrupt(EasterBossEntity easterBossEntity, ServerPlayer serverPlayer) {
        MobEffectInstance mobEffectInstance2;
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (ItemStack itemStack : serverPlayer.m_150109_().f_35974_) {
            if (itemStack.m_41619_()) continue;
            arrayList.add(itemStack);
        }
        if (!serverPlayer.m_21206_().m_41619_()) {
            arrayList.add(serverPlayer.m_21206_());
        }
        Iterator iterator = (ItemStack)SparrowDisruptionRules.choose(arrayList, arg_0 -> ((RandomSource)easterBossEntity.m_217043_()).m_188503_(arg_0));
        if (iterator != null) {
            long l = SparrowDisruption.clock((Player)serverPlayer) + 20L;
            SILENCED.computeIfAbsent(serverPlayer.m_20148_(), uUID -> new HashMap()).put(iterator.m_41720_(), l);
            if (!serverPlayer.m_36335_().m_41519_(iterator.m_41720_())) {
                serverPlayer.m_36335_().m_41524_(iterator.m_41720_(), 20);
            }
            if (serverPlayer.m_6117_() && serverPlayer.m_21211_().m_150930_(iterator.m_41720_())) {
                serverPlayer.m_5810_();
            }
        }
        ArrayList<MobEffectInstance> arrayList2 = new ArrayList<MobEffectInstance>();
        for (MobEffectInstance mobEffectInstance2 : serverPlayer.m_21220_()) {
            if (!SparrowDisruptionRules.removable(mobEffectInstance2.m_19544_().m_19486_(), mobEffectInstance2.m_19571_())) continue;
            arrayList2.add(mobEffectInstance2);
        }
        MobEffectInstance mobEffectInstance3 = (MobEffectInstance)SparrowDisruptionRules.choose(arrayList2, arg_0 -> ((RandomSource)easterBossEntity.m_217043_()).m_188503_(arg_0));
        if (mobEffectInstance3 != null) {
            serverPlayer.m_21195_(mobEffectInstance3.m_19544_());
        }
        if (iterator == null && mobEffectInstance3 == null) {
            return;
        }
        mobEffectInstance2 = Component.m_237113_((String)"\u00a7e\u9ebb\u96c0 \u00a77\u5e72\u6270\uff1a");
        if (iterator != null) {
            mobEffectInstance2 = mobEffectInstance2.m_6881_().m_7220_(iterator.m_41786_()).m_130946_("\u00a77 \u6c89\u9ed8 1 \u79d2");
        }
        if (mobEffectInstance3 != null) {
            mobEffectInstance2 = mobEffectInstance2.m_6881_().m_130946_(iterator == null ? "" : "\uff1b").m_130946_("\u00a77\u6e05\u9664 ").m_7220_(mobEffectInstance3.m_19544_().m_19482_());
        }
        serverPlayer.m_5661_((Component)mobEffectInstance2, true);
        Level level = serverPlayer.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123809_, serverPlayer.m_20185_(), serverPlayer.m_20186_() + 1.0, serverPlayer.m_20189_(), 12, 0.35, 0.45, 0.35, 0.03);
            serverLevel.m_5594_(null, serverPlayer.m_20183_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 0.6f, 1.6f);
        }
    }

    private static long clock(Player player) {
        return player.m_20194_().m_129783_().m_46467_();
    }

    public static boolean blocked(Player player, ItemStack itemStack) {
        if (!(player instanceof ServerPlayer) || !SparrowDisruptionTargets.valid(player) || itemStack.m_41619_()) {
            return false;
        }
        Map<Item, Long> map = SILENCED.get(player.m_20148_());
        Long l = map == null ? null : map.get(itemStack.m_41720_());
        return l != null && SparrowDisruptionRules.active(SparrowDisruption.clock(player), l);
    }

    public static void castWeaponSkill(ServerPlayer serverPlayer, ResourceLocation resourceLocation) {
        if (!SparrowDisruption.blocked((Player)serverPlayer, serverPlayer.m_21205_())) {
            NonManorWeaponSkills.cast(serverPlayer, resourceLocation);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.isCancelable() && SparrowDisruption.blocked(playerInteractEvent.getEntity(), playerInteractEvent.getItemStack())) {
            playerInteractEvent.setCancellationResult(InteractionResult.FAIL);
            playerInteractEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttack(AttackEntityEvent attackEntityEvent) {
        if (SparrowDisruption.blocked(attackEntityEvent.getEntity(), attackEntityEvent.getEntity().m_21205_())) {
            attackEntityEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onDamage(LivingAttackEvent livingAttackEvent) {
        ServerPlayer serverPlayer;
        Entity entity = livingAttackEvent.getSource().m_7640_();
        if (entity instanceof ServerPlayer && SparrowDisruption.blocked((Player)(serverPlayer = (ServerPlayer)entity), serverPlayer.m_21205_())) {
            livingAttackEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent breakEvent) {
        if (SparrowDisruption.blocked(breakEvent.getPlayer(), breakEvent.getPlayer().m_21205_())) {
            breakEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onUseStart(LivingEntityUseItemEvent.Start start) {
        Player player;
        LivingEntity livingEntity = start.getEntity();
        if (livingEntity instanceof Player && SparrowDisruption.blocked(player = (Player)livingEntity, start.getItem())) {
            start.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onUseTick(LivingEntityUseItemEvent.Tick tick) {
        Player player;
        LivingEntity livingEntity = tick.getEntity();
        if (livingEntity instanceof Player && SparrowDisruption.blocked(player = (Player)livingEntity, tick.getItem())) {
            if (tick.isCancelable()) {
                tick.setCanceled(true);
            }
            player.m_5810_();
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onUseStop(LivingEntityUseItemEvent.Stop stop) {
        Player player;
        LivingEntity livingEntity = stop.getEntity();
        if (livingEntity instanceof Player && SparrowDisruption.blocked(player = (Player)livingEntity, stop.getItem())) {
            stop.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent playerTickEvent) {
        Object object;
        if (playerTickEvent.phase != TickEvent.Phase.START || !((object = playerTickEvent.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object;
        object = SILENCED.get(serverPlayer.m_20148_());
        if (object == null) {
            return;
        }
        long l = SparrowDisruption.clock((Player)serverPlayer);
        object.entrySet().removeIf(entry -> !SparrowDisruptionRules.active(l, (Long)entry.getValue()));
        if (!SparrowDisruptionTargets.valid((Player)serverPlayer) || object.isEmpty()) {
            SILENCED.remove(serverPlayer.m_20148_());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        SILENCED.remove(playerLoggedOutEvent.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            SILENCED.remove(serverPlayer.m_20148_());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent serverStoppedEvent) {
        SILENCED.clear();
    }

    public static void silenceItems(ServerPlayer serverPlayer, List<Item> list, int n) {
        if (!SparrowDisruptionTargets.valid((Player)serverPlayer) || n <= 0) {
            return;
        }
        long l = SparrowDisruption.clock((Player)serverPlayer) + (long)n;
        Map map = SILENCED.computeIfAbsent(serverPlayer.m_20148_(), uUID -> new HashMap());
        for (Item item : list) {
            map.merge(item, l, Math::max);
            if (!serverPlayer.m_36335_().m_41519_(item)) {
                serverPlayer.m_36335_().m_41524_(item, n);
            }
            if (!serverPlayer.m_6117_() || !serverPlayer.m_21211_().m_150930_(item)) continue;
            serverPlayer.m_5810_();
        }
    }
}

