package net.farmingtales.ftboss.skill;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 玩家白刃三连射：逐刻发射，并在换手持物、换维度或离线时清理余射（2026-11-04）。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class XuanlanBladeBurst {
    private record Burst(ServerLevel level, ItemStack stack, InteractionHand hand, long nextShot, int remaining) {}
    private static final Map<ServerPlayer, Burst> BURSTS = new WeakHashMap<>();

    private XuanlanBladeBurst() {}

    public static boolean isFiring(ServerPlayer owner) {
        return BURSTS.containsKey(owner);
    }

    public static boolean start(ServerPlayer owner, InteractionHand hand, ItemStack stack) {
        if (isFiring(owner) || !owner.isAlive() || owner.isSpectator() || !XuanlanCombat.canLaunchBlade(owner)
                || owner.getItemInHand(hand) != stack || !XuanlanMoonWeaponItem.isWhite(stack)) return false;
        if (!launch(owner, hand)) return false;
        BURSTS.put(owner, new Burst(owner.serverLevel(), stack, hand,
                owner.serverLevel().getGameTime() + XuanlanRules.BLADE_SHOT_INTERVAL_TICKS,
                XuanlanRules.BLADES_PER_VOLLEY - 1));
        return true;
    }

    private static boolean launch(ServerPlayer owner, InteractionHand hand) {
        if (XuanlanBladeRoster.count(owner) >= XuanlanRules.MAX_ACTIVE_BLADES) return false;
        ServerLevel level = owner.serverLevel();
        PlayerMoonChakramEntity blade = new PlayerMoonChakramEntity(FTEntities.PLAYER_MOON_CHAKRAM.get(), level);
        blade.setOwner(owner);
        blade.setLaunchHand(hand);
        blade.setPos(owner.getEyePosition().add(0, -0.15, 0));
        blade.setDeltaMovement(owner.getLookAngle().scale(1.25));
        if (!level.addFreshEntity(blade)) return false;
        int slot = XuanlanCombat.trackBlade(owner, blade);
        if (slot < 0) {
            blade.discard();
            return false;
        }
        XuanlanFxPacket.send(level, XuanlanMotionRules.throwKind(slot, hand == InteractionHand.OFF_HAND),
                owner.getId(), owner.getEyePosition(), blade.position());
        level.playSound(null, owner.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8F, 1.25F);
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer owner)) return;
        Burst burst = BURSTS.get(owner);
        if (burst == null) return;
        if (!owner.isAlive() || owner.isSpectator() || owner.serverLevel() != burst.level
                || owner.getItemInHand(burst.hand) != burst.stack || burst.stack.isEmpty()
                || !XuanlanMoonWeaponItem.isWhite(burst.stack)) {
            BURSTS.remove(owner);
            return;
        }
        long now = owner.serverLevel().getGameTime();
        if (now < burst.nextShot) return;
        BURSTS.remove(owner);
        if (launch(owner, burst.hand) && burst.remaining > 1) {
            BURSTS.put(owner, new Burst(burst.level, burst.stack, burst.hand,
                    now + XuanlanRules.BLADE_SHOT_INTERVAL_TICKS, burst.remaining - 1));
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        BURSTS.remove(event.getEntity());
    }
}
