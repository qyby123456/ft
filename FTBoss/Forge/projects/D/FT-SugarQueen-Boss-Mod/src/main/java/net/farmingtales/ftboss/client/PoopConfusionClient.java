package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.client.player.Input;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 【屎？！！】的"方向键位混乱"(2026-09-12)。
 *
 * <p>挂的是 {@link MovementInputUpdateEvent} —— 它在 {@code LocalPlayer.tick()} 里、
 * 移动结算**之前**触发, 所以直接改写 {@link Input} 就能真实改变这一 tick 的走向
 * (比改 KeyMapping 可靠: {@code KeyMapping.MAP} 是构造时登记的, 换 key 不会重新映射)。
 *
 * <p>层数与混乱程度:
 * <ul>
 *   <li>1 层: A/D 反向(左右颠倒)</li>
 *   <li>2 层: 前后 + 左右全部反向(整个摇杆翻过来)</li>
 *   <li>3 层以上: 整体转 90°(W→左, A→后, S→右, D→前)</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PoopConfusionClient {

    private PoopConfusionClient() {}

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent e) {
        Player player = e.getEntity();            // 事件本身是 PlayerEvent, 已经是 Player
        if (player == null) return;
        MobEffectInstance inst = player.getEffect(FTEffects.POOP_CONFUSION.get());
        if (inst == null) return;

        Input in = e.getInput();
        // 先取原始值, 再统一改写(否则会读到已经改过的量)
        float fwd = in.forwardImpulse;
        float side = in.leftImpulse;
        boolean up = in.up, down = in.down, left = in.left, right = in.right;

        int amp = inst.getAmplifier();
        if (amp <= 0) {
            // 1 层: A/D 反向
            in.leftImpulse = -side;
            in.left = right;
            in.right = left;
        } else if (amp == 1) {
            // 2 层: 前后左右全反
            in.forwardImpulse = -fwd;
            in.leftImpulse = -side;
            in.up = down;
            in.down = up;
            in.left = right;
            in.right = left;
        } else {
            // 3 层以上: 整体转 90°(W→左, A→后, S→右, D→前)
            in.forwardImpulse = -side;
            in.leftImpulse = fwd;
            in.up = right;
            in.right = down;
            in.down = left;
            in.left = up;
        }
    }
}
