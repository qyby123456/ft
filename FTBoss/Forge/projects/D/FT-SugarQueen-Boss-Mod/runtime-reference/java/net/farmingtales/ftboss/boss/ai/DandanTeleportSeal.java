/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.blay09.mods.waystones.api.WaystoneTeleportEvent$Pre
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.Items
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.entity.EntityTeleportEvent
 *  net.minecraftforge.event.entity.EntityTravelToDimensionEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.blay09.mods.waystones.api.WaystoneTeleportEvent;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorRefinementRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class DandanTeleportSeal {
    private static final String KEY = "ftboss.dandan_teleport_seal";
    private static final Map<ServerPlayer, Long> NOTICE = new WeakHashMap<ServerPlayer, Long>();

    private DandanTeleportSeal() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void hit(LivingDamageEvent livingDamageEvent) {
        Object object;
        ServerPlayer serverPlayer;
        LivingEntity livingEntity;
        if (livingDamageEvent.getAmount() <= 0.0f || !((livingEntity = livingDamageEvent.getEntity()) instanceof ServerPlayer) || (serverPlayer = (ServerPlayer)livingEntity).m_7500_() || serverPlayer.m_5833_()) {
            return;
        }
        livingEntity = livingDamageEvent.getSource().m_7639_();
        if (livingEntity == null) {
            livingEntity = livingDamageEvent.getSource().m_7640_();
        }
        for (int i = 0; i < 4 && livingEntity instanceof Projectile; ++i) {
            object = (Projectile)livingEntity;
            livingEntity = object.m_19749_();
        }
        if (!(livingEntity instanceof EasterBossEntity) || ((EasterBossEntity)(object = (EasterBossEntity)livingEntity)).getVariant() != 1 || ((EasterBossEntity)object).isMini() || ((EasterBossEntity)object).isGiant() || object.m_21525_() || !object.m_6084_()) {
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128356_("Until", serverPlayer.f_8924_.m_129783_().m_46467_() + 200L);
        compoundTag.m_128362_("Boss", object.m_20148_());
        compoundTag.m_128359_("Dimension", object.m_9236_().m_46472_().m_135782_().toString());
        serverPlayer.getPersistentData().m_128365_(KEY, (Tag)compoundTag);
        DandanTeleportSeal.notice(serverPlayer);
    }

    public static boolean sealed(ServerPlayer serverPlayer) {
        if (serverPlayer == null || !serverPlayer.m_6084_() || serverPlayer.m_7500_() || serverPlayer.m_5833_()) {
            return false;
        }
        CompoundTag compoundTag = serverPlayer.getPersistentData();
        if (!compoundTag.m_128441_(KEY)) {
            return false;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_(KEY);
        if (ManorRefinementRules.sealed(serverPlayer.f_8924_.m_129783_().m_46467_(), compoundTag2.m_128454_("Until")) && compoundTag2.m_128403_("Boss")) {
            UUID uUID = compoundTag2.m_128342_("Boss");
            for (ServerLevel serverLevel : serverPlayer.f_8924_.m_129785_()) {
                EasterBossEntity easterBossEntity;
                Entity entity;
                if (!serverLevel.m_46472_().m_135782_().toString().equals(compoundTag2.m_128461_("Dimension")) || !((entity = serverLevel.m_8791_(uUID)) instanceof EasterBossEntity) || !(easterBossEntity = (EasterBossEntity)entity).m_6084_() || easterBossEntity.m_213877_() || easterBossEntity.m_21525_() || easterBossEntity.getVariant() != 1) continue;
                return true;
            }
        }
        compoundTag.m_128473_(KEY);
        return false;
    }

    public static boolean block(ServerPlayer serverPlayer) {
        if (!DandanTeleportSeal.sealed(serverPlayer) || DandanTeleportSeal.forced()) {
            return false;
        }
        DandanTeleportSeal.notice(serverPlayer);
        return true;
    }

    public static boolean blockConnection(ServerPlayer serverPlayer) {
        if (!DandanTeleportSeal.sealed(serverPlayer)) {
            return false;
        }
        boolean bl = StackWalker.getInstance().walk(stream -> stream.anyMatch(stackFrame -> stackFrame.getClassName().equals("net.minecraft.server.network.ServerGamePacketListenerImpl") && (stackFrame.getMethodName().equals("handleMovePlayer") || stackFrame.getMethodName().equals("m_7185_") || stackFrame.getMethodName().equals("handleMoveVehicle") || stackFrame.getMethodName().equals("m_5659_"))));
        return !bl && DandanTeleportSeal.block(serverPlayer);
    }

    private static boolean forced() {
        return StackWalker.getInstance().walk(stream -> stream.anyMatch(stackFrame -> {
            String string = stackFrame.getClassName();
            return string.startsWith("net.farmingtales.ftboss.boss.") && !string.equals(DandanTeleportSeal.class.getName()) && !string.startsWith(DandanTeleportSeal.class.getName() + "$") || string.equals("net.farmingtales.ftboss.entity.EasterBossEntity") || string.equals("net.minecraft.server.players.PlayerList");
        }));
    }

    private static void notice(ServerPlayer serverPlayer) {
        long l = serverPlayer.f_8924_.m_129783_().m_46467_();
        Long l2 = NOTICE.get(serverPlayer);
        if (l2 != null && l - l2 < 40L) {
            return;
        }
        NOTICE.put(serverPlayer, l);
        serverPlayer.m_5661_((Component)Component.m_237113_((String)"\u00a75\u672b\u5f71\u5c01\u9501 \u00a77\u00b7 \u6682\u65f6\u65e0\u6cd5\u4e3b\u52a8\u4f20\u9001"), true);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void teleport(EntityTeleportEvent entityTeleportEvent) {
        ServerPlayer serverPlayer;
        Entity entity;
        if (entityTeleportEvent.isCancelable() && (entity = entityTeleportEvent.getEntity()) instanceof ServerPlayer && DandanTeleportSeal.block(serverPlayer = (ServerPlayer)entity)) {
            entityTeleportEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void travel(EntityTravelToDimensionEvent entityTravelToDimensionEvent) {
        ServerPlayer serverPlayer;
        Entity entity = entityTravelToDimensionEvent.getEntity();
        if (entity instanceof ServerPlayer && DandanTeleportSeal.block(serverPlayer = (ServerPlayer)entity)) {
            entityTravelToDimensionEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void pearl(PlayerInteractEvent.RightClickItem rightClickItem) {
        Player player = rightClickItem.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (rightClickItem.getItemStack().m_150930_(Items.f_42584_) && DandanTeleportSeal.block(serverPlayer)) {
                rightClickItem.setCanceled(true);
                rightClickItem.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        NOTICE.clear();
    }

    static {
        if (ModList.get().isLoaded("waystones")) {
            Waystones.register();
        }
    }

    private static final class Waystones {
        private Waystones() {
        }

        private static void register() {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, WaystoneTeleportEvent.Pre.class, pre -> {
                ServerPlayer serverPlayer;
                Entity entity = pre.getContext().getEntity();
                if (entity instanceof ServerPlayer && DandanTeleportSeal.block(serverPlayer = (ServerPlayer)entity)) {
                    pre.setCanceled(true);
                }
            });
        }
    }
}

