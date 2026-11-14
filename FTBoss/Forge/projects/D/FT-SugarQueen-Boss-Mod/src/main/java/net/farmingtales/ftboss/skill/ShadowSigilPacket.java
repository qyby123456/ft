package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.client.render.ShadowSigilVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C：在指定世界坐标画一次【暗影镇印】（地面魔法阵，可选能量柱）。
 *
 * <p>字段刻意只有 4 个（三个 double + 一个 boolean）—— 这是本模组的第一个自定义包，
 * 保持最小可验证：能收到就说明整条链路（注册 → 发送 → 解码 → 主线程消费）通了。
 *
 * <p><b>客户端处理</b>：{@link #handle} 里再判一次 {@link FMLEnvironment#dist}，
 * 因为本类会被**两侧**同时加载（编码/解码在服务端也要用），
 * 而 {@link ShadowSigilVisual} 是纯客户端类 —— 专服上不能碰它，否则 NoClassDefFoundError。
 * 判完 dist 之后的引用都在客户端分支里，专服不会解析到。
 */
public final class ShadowSigilPacket {

    private final double x;
    private final double y;
    private final double z;
    private final boolean pillar;

    public ShadowSigilPacket(double x, double y, double z, boolean pillar) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pillar = pillar;
    }

    public static void encode(ShadowSigilPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeBoolean(msg.pillar);
    }

    public static ShadowSigilPacket decode(FriendlyByteBuf buf) {
        return new ShadowSigilPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }

    /** 已在 {@code consumerMainThread} 上注册，这里跑在主线程，可以安全碰客户端状态。 */
    public static void handle(ShadowSigilPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        /* 2026-09-16 诊断：能打这行 = 包真的到了客户端（排查完可删）。 */
        net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][镇印] 客户端收到包 ({}, {}, {}) 柱={}",
                String.format("%.2f", msg.x), String.format("%.2f", msg.y),
                String.format("%.2f", msg.z), msg.pillar);
        ShadowSigilVisual.spawn(msg.x, msg.y, msg.z, msg.pillar);
    }
}
