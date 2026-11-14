package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.blay09.mods.waystones.api.event.WaystoneTeleportEvent.Pre;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorRefinementRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class DandanTeleportSeal {
    private static final String KEY = "ftboss.dandan_teleport_seal";
    private static final Map<ServerPlayer, Long> NOTICE = new WeakHashMap<>();

    private DandanTeleportSeal() {
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void hit(LivingDamageEvent.Pre var0) {
        if (!(var0.getNewDamage() <= 0.0F) && var0.getEntity() instanceof ServerPlayer var1 && !var1.isCreative() && !var1.isSpectator()) {
            Entity var5 = var0.getSource().getEntity();
            if (var5 == null) {
                var5 = var0.getSource().getDirectEntity();
            }

            for (int var4 = 0; var4 < 4 && var5 instanceof Projectile; var4++) {
                Projectile var3 = (Projectile)var5;
                var5 = var3.getOwner();
            }

            if (var5 instanceof EasterBossEntity var6 && var6.getVariant() == 1 && !var6.isMini() && !var6.isGiant() && !var6.isNoAi() && var6.isAlive()) {
                CompoundTag var7 = new CompoundTag();
                var7.putLong("Until", var1.server.overworld().getGameTime() + 200L);
                var7.putUUID("Boss", var6.getUUID());
                var7.putString("Dimension", var6.level().dimension().location().toString());
                var1.getPersistentData().put("ftboss.dandan_teleport_seal", var7);
                notice(var1);
                return;
            }
        }
    }

    public static boolean sealed(ServerPlayer var0) {
        if (var0 != null && var0.isAlive() && !var0.isCreative() && !var0.isSpectator()) {
            CompoundTag var1 = var0.getPersistentData();
            if (!var1.contains("ftboss.dandan_teleport_seal")) {
                return false;
            } else {
                CompoundTag var2 = var1.getCompound("ftboss.dandan_teleport_seal");
                if (ManorRefinementRules.sealed(var0.server.overworld().getGameTime(), var2.getLong("Until")) && var2.hasUUID("Boss")) {
                    UUID var3 = var2.getUUID("Boss");

                    for (ServerLevel var5 : var0.server.getAllLevels()) {
                        if (var5.dimension().location().toString().equals(var2.getString("Dimension"))
                            && var5.getEntity(var3) instanceof EasterBossEntity var7
                            && var7.isAlive()
                            && !var7.isRemoved()
                            && !var7.isNoAi()
                            && var7.getVariant() == 1) {
                            return true;
                        }
                    }
                }

                var1.remove("ftboss.dandan_teleport_seal");
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean block(ServerPlayer var0) {
        if (sealed(var0) && !forced()) {
            notice(var0);
            return true;
        } else {
            return false;
        }
    }

    public static boolean blockConnection(ServerPlayer var0) {
        if (!sealed(var0)) {
            return false;
        } else {
            boolean var1 = StackWalker.getInstance()
                .walk(
                    var0x -> var0x.anyMatch(
                            var0xx -> var0xx.getClassName().equals("net.minecraft.server.network.ServerGamePacketListenerImpl")
                                    && (
                                        var0xx.getMethodName().equals("handleMovePlayer")
                                            || var0xx.getMethodName().equals("handleMovePlayer")
                                            || var0xx.getMethodName().equals("handleMoveVehicle")
                                            || var0xx.getMethodName().equals("handleMoveVehicle")
                                    )
                        )
                );
            return !var1 && block(var0);
        }
    }

    private static boolean forced() {
        return StackWalker.getInstance()
            .walk(
                var0 -> var0.anyMatch(
                        var0x -> {
                            String var1 = var0x.getClassName();
                            return var1.startsWith("net.farmingtales.ftboss.boss.")
                                    && !var1.equals(DandanTeleportSeal.class.getName())
                                    && !var1.startsWith(DandanTeleportSeal.class.getName() + "$")
                                || var1.equals("net.farmingtales.ftboss.entity.EasterBossEntity")
                                || var1.equals("net.minecraft.server.players.PlayerList");
                        }
                    )
            );
    }

    private static void notice(ServerPlayer var0) {
        long var1 = var0.server.overworld().getGameTime();
        Long var3 = NOTICE.get(var0);
        if (var3 == null || var1 - var3 >= 40L) {
            NOTICE.put(var0, var1);
            var0.displayClientMessage(Component.literal("§5末影封锁 §7· 暂时无法主动传送"), true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void teleport(EntityTeleportEvent var0) {
        if (var0 instanceof net.neoforged.bus.api.ICancellableEvent && var0.getEntity() instanceof ServerPlayer var1 && block(var1)) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void travel(EntityTravelToDimensionEvent var0) {
        if (var0.getEntity() instanceof ServerPlayer var1 && block(var1)) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void pearl(RightClickItem var0) {
        if (var0.getEntity() instanceof ServerPlayer var1 && var0.getItemStack().is(Items.ENDER_PEARL) && block(var1)) {
            var0.setCanceled(true);
            var0.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent var0) {
        NOTICE.clear();
    }

    static {
        if (ModList.get().isLoaded("waystones")) {
            DandanTeleportSeal.Waystones.register();
        }
    }

    private static final class Waystones {
        private static void register() {
            net.blay09.mods.balm.api.Balm.getEvents().onEvent(Pre.class, var0 -> {
                if (!var0.isCanceled() && var0.getContext().getEntity() instanceof ServerPlayer var1 && DandanTeleportSeal.block(var1)) {
                    var0.setCanceled(true);
                }
            }, net.blay09.mods.balm.api.event.EventPriority.Highest);
        }
    }
}
