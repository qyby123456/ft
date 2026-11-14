package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 【失明/黑暗遮罩 + 客户端同步检查】(2026-09-14 v121 建, v122 换钩子)。
 *
 * <p>背景: 折耳根给玩家挂的 失明/黑暗/反胃 在**服务端确实都在**(验真日志 `失明=true 黑暗=true`)，
 * 但原版失明与黑暗的"画面变黑"都属于**雾效**({@code BlindnessFogEnvironment}/{@code DarknessFogEnvironment}),
 * 会被光影包整段接管, 于是屏幕上什么都看不出来 —— 这就是用户反复说"还是没挂上"的原因。
 * v121 想自己画一层, 但当时挂的是 {@code RenderGuiEvent} —— 那个事件在这版 Forge(47.4.10)
 * **有类但不会被发出**(NeoForge 才发), 所以日志里一条都没有。v122 改用 1.20.1 一定会发的
 * {@link RenderGuiOverlayEvent}(画在 HOTBAR 那一层: 盖住整个世界, 但之后画的 HUD 仍能看到)。
 *
 * <p>另外每 5 秒打一条**客户端同步检查**: 本地玩家身上到底有没有这几个效果 ——
 * 日志里 `客户端玩家身上: 失明=true` 就说明服务端→客户端同步没问题(纯粹是雾效被吃掉, 这层遮罩生效);
 * `=false` 才说明连客户端都没同步到, 那要往同步/被清除的方向查。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BlindnessOverlay {

    /** 失明: 全屏遮罩(约 63% 黑) */
    private static final int BLIND_FULL = 0xA0000000;
    /** 失明: 边缘更黑, 做出"视野被压窄"的感觉 */
    private static final int BLIND_EDGE = 0xC8000000;
    /** 黑暗: 稍淡一点的全屏遮罩(和原版黑暗那种"缓缓压下来"接近) */
    private static final int DARK_FULL = 0x78000000;
    /** 黑暗: 边缘 */
    private static final int DARK_EDGE = 0x98000000;

    private static long lastLog = 0L;

    private BlindnessOverlay() {}

    /**
     * 画遮罩: 挂在 HOTBAR 这一层(它一定会被画), 于是这层黑盖住世界、但血条/效果图标仍在其上。
     * 失明/黑暗/本模组的【暗影笼罩】各自独立判断(一起中就更黑), 免得光影吃掉雾效后玩家"什么都感觉不到"。
     *
     * <p>★ v125: 之所以要认第三条效果 —— 铁魔法 Spellbooks 的**「可见之戒」饰品每 tick 摘掉原版的
     * 失明/黑暗**(v124 的诊断把调用栈打出来了: {@code VisibilityRing#curioTick}), 所以戴着它的玩家
     * 身上只会有我们的 {@code ftboss:shadow_blind}, 画面全靠这一条撑。
     */
    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post e) {
        if (e.getOverlay() == null || e.getOverlay().id() == null) return;
        if (!e.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        boolean shroud = hasShroud(mc.player);
        if (shroud || ShadowDarknessOverlay.isVisible()) return;
        boolean blind = mc.player.hasEffect(MobEffects.BLINDNESS);
        boolean dark = mc.player.hasEffect(MobEffects.DARKNESS);
        if (!blind && !dark) return;
        var g = e.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int full = blind ? BLIND_FULL : DARK_FULL;
        int edge = blind ? BLIND_EDGE : DARK_EDGE;
        g.fill(0, 0, w, h, full);
        /* 上下 + 左右四条边带: 中间留一条"缝", 看起来像视野被压窄 */
        int bandV = Math.max(1, h / 6);
        int bandH = Math.max(1, w / 8);
        g.fill(0, 0, w, bandV, edge);
        g.fill(0, h - bandV, w, h, edge);
        g.fill(0, 0, bandH, h, edge);
        g.fill(w - bandH, 0, w, h, edge);
    }

    /** 身上有没有本模组的【暗影笼罩】(可见之戒挡不住的那条) */
    private static boolean hasShroud(net.minecraft.world.entity.LivingEntity e) {
        var eff = net.farmingtales.ftboss.effect.FTEffects.SHADOW_BLIND.get();
        return eff != null && e.hasEffect(eff);
    }

    /** v122 诊断: 每 5 秒报一次"本地玩家身上到底有没有这些效果" */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        boolean blind = mc.player.hasEffect(MobEffects.BLINDNESS);
        boolean dark = mc.player.hasEffect(MobEffects.DARKNESS);
        boolean nausea = mc.player.hasEffect(MobEffects.CONFUSION);
        boolean shroud = hasShroud(mc.player);
        if (!blind && !dark && !nausea && !shroud) return;
        long now = mc.level.getGameTime();
        if (now - lastLog < 100L) return;
        lastLog = now;
        FTBossMod.LOGGER.info("[FTBoss][client] 客户端玩家身上: 失明={} 黑暗={} 反胃={} 暗影笼罩={}(>=1 就会自己画遮罩)",
                blind, dark, nausea, shroud);
    }
}
