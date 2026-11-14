package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 【主世界限定】(2026-09-13 v93 用户要求「做一个检测, 全部 boss 都必须在主世界召唤」)。
 *
 * <p>所有**玩家可见的召唤入口**都要先过这一关：召唤物右键、{@code /ftboss challenge} 开启隐藏 Boss。
 * 不在主世界 -> **中止召唤**、不消耗召唤物、给玩家一句提示, 并留一行日志。
 *
 * <p>为什么放在召唤入口而不是"每 tick 扫场上 Boss 的维度"：
 * Boss 自己召唤出来的东西(柠檬猫喊帮手、庄园轮换、麻雀支援、各种小怪)都继承召唤者的维度,
 * 只要**源头**在主世界, 后面全都是主世界; 反过来在别的维度把它们清掉, 会误伤"玩家把 Boss 引进传送门"
 * 这类正常行为。要改成"任何 ftboss Boss 出现在非主世界就清掉"只需在 {@link #allow} 之外再加一条扫描。
 *
 * <p>白名单只有 {@code minecraft:overworld}；其它维度(下界/末地/模组维度)一律拒绝。
 */
public final class SummonGate {

    /** 总开关: 关掉就退回"哪儿都能召唤" */
    public static final boolean OVERWORLD_ONLY = true;

    private SummonGate() {}

    /** 这个维度允许召唤 Boss 吗 */
    public static boolean isAllowedDim(Level level) {
        if (!OVERWORLD_ONLY) return true;
        return level != null && Level.OVERWORLD.equals(level.dimension());
    }

    /** 维度的中文说法(提示用) */
    public static String dimName(Level level) {
        if (level == null) return "?";
        ResourceKey<Level> k = level.dimension();
        if (Level.OVERWORLD.equals(k)) return "主世界";
        if (Level.NETHER.equals(k)) return "下界";
        if (Level.END.equals(k)) return "末地";
        return k.location().toString();
    }

    /**
     * 允许就在主世界召唤：返回 true。
     * 否则给玩家一句提示（player 可为 null）+ 一行日志，返回 **false —— 调用方必须中止召唤**。
     *
     * @param bossName 被召唤 Boss 的中文名（写进提示与日志）
     */
    public static boolean allow(Level level, Player player, String bossName) {
        if (isAllowedDim(level)) return true;
        String dim = dimName(level);
        if (player != null) {
            player.displayClientMessage(Component.translatable("msg.ftboss.overworld_only", bossName, dim), true);
        }
        FTBossMod.LOGGER.info("[FTBoss][召唤] 阻止在「{}」召唤 {} —— 全部 Boss 只能在主世界召唤",
                dim, bossName);
        return false;
    }
}
