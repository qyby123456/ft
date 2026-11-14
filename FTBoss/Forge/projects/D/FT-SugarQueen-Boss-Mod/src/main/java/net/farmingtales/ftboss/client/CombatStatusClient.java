package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.boss.ai.CombatStatusHooks;
import net.farmingtales.ftboss.boss.ai.CombatStatusRules;
import net.farmingtales.ftboss.effect.CombatStatusEffects;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 共享控制状态的客户端输入与闪烁黑暗遮罩。2026-10-26：仅客户端加载。 */
@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class CombatStatusClient {
    private CombatStatusClient() {}

    @SubscribeEvent
    public static void input(MovementInputUpdateEvent event) {
        if (!CombatStatusHooks.immobilized(event.getEntity())) return;
        var input = event.getInput();
        input.forwardImpulse = 0;
        input.leftImpulse = 0;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        var player = Minecraft.getInstance().player;
        if (player != null && CombatStatusHooks.stunned(player)) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Overlay {
        private Overlay() {}

        @SubscribeEvent
        public static void register(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "combat_flickering_darkness", (gui, graphics, partial, width, height) -> {
                var player = Minecraft.getInstance().player;
                if (player == null || !player.isAlive() || player.isSpectator()
                        || !player.hasEffect(CombatStatusEffects.FLICKERING_DARKNESS)) return;
                int alpha = Math.round(255 * CombatStatusRules.darkness(player.tickCount + partial));
                graphics.fill(0, 0, width, height, alpha << 24 | 0x080411);
            });
        }
    }
}
