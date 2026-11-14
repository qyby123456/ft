package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorRefinementRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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

/** 蛋蛋命中后的主动传送封锁；保留场地召回、Boss强制位移与重生。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class DandanTeleportSeal {
    private static final String KEY = "ftboss.dandan_teleport_seal";
    private static final Map<ServerPlayer, Long> NOTICE = new WeakHashMap<>();

    static {
        if (ModList.get().isLoaded("waystones")) Waystones.register();
    }

    private DandanTeleportSeal() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hit(LivingDamageEvent event) {
        if (event.getAmount() <= 0 || !(event.getEntity() instanceof ServerPlayer player)
                || player.isCreative() || player.isSpectator()) return;
        Entity owner = event.getSource().getEntity();
        if (owner == null) owner = event.getSource().getDirectEntity();
        for (int depth = 0; depth < 4 && owner instanceof Projectile projectile; depth++) owner = projectile.getOwner();
        if (!(owner instanceof EasterBossEntity boss) || boss.getVariant() != EasterBossEntity.DANDAN_VARIANT
                || boss.isMini() || boss.isGiant() || boss.isNoAi() || !boss.isAlive()) return;
        CompoundTag seal = new CompoundTag();
        seal.putLong("Until", player.server.overworld().getGameTime() + ManorRefinementRules.TELEPORT_SEAL_TICKS);
        seal.putUUID("Boss", boss.getUUID());
        seal.putString("Dimension", boss.level().dimension().location().toString());
        player.getPersistentData().put(KEY, seal);
        notice(player);
    }

    public static boolean sealed(ServerPlayer player) {
        if (player == null || !player.isAlive() || player.isCreative() || player.isSpectator()) return false;
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY)) return false;
        CompoundTag seal = data.getCompound(KEY);
        if (ManorRefinementRules.sealed(player.server.overworld().getGameTime(), seal.getLong("Until"))
                && seal.hasUUID("Boss")) {
            UUID source = seal.getUUID("Boss");
            for (var level : player.server.getAllLevels()) {
                if (!level.dimension().location().toString().equals(seal.getString("Dimension"))) continue;
                Entity entity = level.getEntity(source);
                if (entity instanceof EasterBossEntity boss && boss.isAlive() && !boss.isRemoved()
                        && !boss.isNoAi() && boss.getVariant() == EasterBossEntity.DANDAN_VARIANT) return true;
            }
        }
        data.remove(KEY);
        return false;
    }

    public static boolean block(ServerPlayer player) {
        if (!sealed(player) || forced()) return false;
        notice(player);
        return true;
    }

    public static boolean blockConnection(ServerPlayer player) {
        if (!sealed(player)) return false;
        boolean correction = StackWalker.getInstance().walk(frames -> frames.anyMatch(frame ->
                frame.getClassName().equals("net.minecraft.server.network.ServerGamePacketListenerImpl")
                        && (frame.getMethodName().equals("handleMovePlayer") || frame.getMethodName().equals("m_7185_")
                        || frame.getMethodName().equals("handleMoveVehicle") || frame.getMethodName().equals("m_5659_"))));
        return !correction && block(player);
    }

    private static boolean forced() {
        return StackWalker.getInstance().walk(frames -> frames.anyMatch(frame -> {
            String owner = frame.getClassName();
            return owner.startsWith("net.farmingtales.ftboss.boss.") && !owner.equals(DandanTeleportSeal.class.getName())
                    && !owner.startsWith(DandanTeleportSeal.class.getName() + "$")
                    || owner.equals("net.farmingtales.ftboss.entity.EasterBossEntity")
                    || owner.equals("net.minecraft.server.players.PlayerList");
        }));
    }

    private static void notice(ServerPlayer player) {
        long now = player.server.overworld().getGameTime();
        Long previous = NOTICE.get(player);
        if (previous != null && now - previous < 40) return;
        NOTICE.put(player, now);
        player.displayClientMessage(Component.literal("§5末影封锁 §7· 暂时无法主动传送"), true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void teleport(EntityTeleportEvent event) {
        if (event.isCancelable() && event.getEntity() instanceof ServerPlayer player && block(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void travel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && block(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pearl(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getItemStack().is(Items.ENDER_PEARL) && block(player)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { NOTICE.clear(); }

    private static final class Waystones {
        private static void register() {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false,
                    net.blay09.mods.waystones.api.WaystoneTeleportEvent.Pre.class, event -> {
                        if (event.getContext().getEntity() instanceof ServerPlayer player && block(player)) event.setCanceled(true);
                    });
        }
    }
}
