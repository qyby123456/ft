package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 【本模组第一个网络通道】（2026-09-16）。
 *
 * <p><b>为什么非要有它</b>：{@code shadowsigil} / {@code shadow_pillar} 这两张贴图
 * 从 2026-09-15 就生成了，但**没有任何 Java 引用** —— 地面魔法阵是"世界贴花"，
 * 既不是实体也不是粒子（粒子是点精灵，画不出阵列贴图）。
 * 服务端能自动同步给客户端的只有**粒子与音效**，贴花必须有自定义包。
 * 在那之前，"暗影传送法阵未生效"不是配置错，而是**根本没接线**
 * （见 {@code docs/model-assets.md} 的 "无 Java 引用，保留待确认"）。
 *
 * <p><b>协议版本</b>：{@link #PROTOCOL} = 10。S2C/C2S 包包括
 * {@link ShadowSigilPacket}、{@link ManorIntroPacket} 与庄园召回边界同步包。因为 mod 与客户端是同一个 jar 分发，
 * 用 {@code NetworkRegistry.acceptMissingOr} 双向放行，避免在"客户端装了但服务端没装"
 * 这种开发期组合下直接握手失败。
 * （注：1.20.1 的方法名是 {@code acceptMissingOr(String)}，**不是**高版本才改名的
 *  {@code acceptVanillaOrMissing} —— 已对 Forge 47.4.10 的源码 jar 核过。）
 */
public final class FTNetwork {

    /** 通道名与协商版本。改包结构时必须同时递增，否则新旧客户端会互相误解码。 */
    private static final String PROTOCOL = "10";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new net.minecraft.resources.ResourceLocation(FTBossMod.MOD_ID, "main"),
            () -> PROTOCOL,
            NetworkRegistry.acceptMissingOr(PROTOCOL),
            NetworkRegistry.acceptMissingOr(PROTOCOL));

    private static boolean registered;

    private FTNetwork() {}

    /** 在 mod 构造里调一次即可（重复调用会被内部开关挡掉）。 */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        /* C2S skill request must be registered here during mod construction. Creating a
           separate channel only when J is first pressed is too late: Forge has locked its
           channel registry by then and throws "Registration of impl channels is locked". */
        CHANNEL.messageBuilder(NonManorWeaponPacket.class, 8, NetworkDirection.PLAY_TO_SERVER)
                .encoder(NonManorWeaponPacket::encode)
                .decoder(NonManorWeaponPacket::decode)
                .consumerMainThread(NonManorWeaponPacket::handle)
                .add();
        MinecraftForge.EVENT_BUS.register(NonManorWeaponSkills.class);

        CHANNEL.messageBuilder(ManorRecallBoundaryPacket.class, 7, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManorRecallBoundaryPacket::encode)
                .decoder(ManorRecallBoundaryPacket::decode)
                .consumerMainThread(ManorRecallBoundaryPacket::handle)
                .add();
        CHANNEL.messageBuilder(ManorTransitionPacket.class, 6, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManorTransitionPacket::encode)
                .decoder(ManorTransitionPacket::decode)
                .consumerMainThread(ManorTransitionPacket::handle)
                .add();
        CHANNEL.messageBuilder(ManorVictoryPacket.class, 5, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManorVictoryPacket::encode)
                .decoder(ManorVictoryPacket::decode)
                .consumerMainThread(ManorVictoryPacket::handle)
                .add();
        CHANNEL.messageBuilder(SpellVisualPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SpellVisualPacket::encode)
                .decoder(SpellVisualPacket::decode)
                .consumerMainThread(SpellVisualPacket::handle)
                .add();
        CHANNEL.messageBuilder(DandanScytheFxPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DandanScytheFxPacket::encode)
                .decoder(DandanScytheFxPacket::decode)
                .consumerMainThread(DandanScytheFxPacket::handle)
                .add();

        /* 包 id 0：地面魔法阵（S2C，只允许服务端往客户端发）。 */
        CHANNEL.messageBuilder(ShadowSigilPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ShadowSigilPacket::encode)
                .decoder(ShadowSigilPacket::decode)
                .consumerMainThread(ShadowSigilPacket::handle)
                .add();

        CHANNEL.messageBuilder(ManorIntroPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManorIntroPacket::encode)
                .decoder(ManorIntroPacket::decode)
                .consumerMainThread(ManorIntroPacket::handle)
                .add();

    }

    /**
     * 把"在某点画一个传送法阵"发给**附近能看到这一格的玩家**。
     *
     * <p>★ 用 {@link PacketDistributor#NEAR} 时第 4 个参数是 <b>半径的平方</b>：
     * 已用 javap 反汇编原版 {@code PlayerList.broadcast} 核对过字节码 ——
     * 算的是 {@code dx*dx + dy*dy + dz*dz}，再与原参数比较，
     * 所以传 64 表示"64 格以内"（不是 64 的平方）。见 {@link #NEAR_RADIUS}。
     *
     * <p>★ 第 1 个参数是 <b>excluded</b>（要排除的玩家）—— 详见方法体里的注释，
     * 传施法者进去会把他自己排除掉，所以要另发一份给本人。
     *
     * <p>本方法**只在服务端**有实际效果；客户端或服务端为 null 时静默返回。
     */
    public static void sendSigilNear(ServerPlayer source, double x, double y, double z, boolean pillar) {
        if (source == null || !(source.level() instanceof ServerLevel sl)) {
            return;
        }
        ShadowSigilPacket msg = new ShadowSigilPacket(x, y, z, pillar);
        try {
            /* ★★ 2026-09-16 实测「没法阵」的真凶就在这里：
               {@link PacketDistributor.TargetPoint} 的第 1 个参数叫 **excluded**（要排除的玩家），
               而原版 {@code PlayerList.broadcast} 的字节码第一件事就是
               {@code if (player == excluded) continue;} —— 我一开始把施法者传进去，
               等于**把他本人排除在外**。单人/局域网里视野内只有施法者自己 →
               一个包都发不出去，表现正是"服务端结算日志正常、客户端一条日志都没有"。
               传 {@code null} 即可：附近所有玩家（含施法者本人）都收到。 */
            CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                    null, x, y, z, NEAR_RADIUS * NEAR_RADIUS, sl.dimension())), msg);
            /* 2026-09-16 诊断：区分「没发出去」和「发出去了客户端没收/没处理」。排查完可删。 */
            FTBossMod.LOGGER.info("[FTBoss][网络] 已发暗影镇印包: ({}, {}, {}) 柱={}",
                    String.format("%.2f", x), String.format("%.2f", y), String.format("%.2f", z), pillar);
        } catch (Throwable t) {
            /* 发包失败（例如还没握手完成）不该把整次技能结算带崩 —— 表现是装饰性的。 */
            FTBossMod.LOGGER.warn("[FTBoss][网络] 传送法阵包发送失败: {}", t.toString());
        }
    }

    /** "附近"的半径（格）。 */
    public static final double NEAR_RADIUS = 64.0D;
}
