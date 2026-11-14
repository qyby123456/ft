package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.List;
import java.util.Random;

public final class ManorBan {

    /**
     * 被禁的**法术**（只比对命名空间后的 path）。
     *
     * <p>★ 2026-09-24 用户要求「禁用部分铁魔法」并点名「止心术」——
     * {@code heartstop} 从「只有终结难度禁」改成**两档难度都禁**，与 {@code black_hole} 同级。
     * {@code oakskin}（橡肤）用户没点名，保持原样（仍只在终结难度禁，见
     * {@link ManorImpossibleRules#BANNED_BUFF_SPELLS}）。
     *
     * <p>★ 2026-10-10 用户要求「庄园 boss 禁用玩家法术：深渊庇佑」——
     * {@code abyssal_shroud}（{@code irons_spellbooks:abyssal_shroud}，"短时间内躲避任何攻击、不受伤害"）
     * 与止心术同属免伤/无敌类，**两档难度都禁**，与 {@code black_hole}/{@code heartstop} 同级。
     * 用户本次**未**要求：清掉进场前已挂的该效果、更新启动日志文案——故两者保持原样。
     *
     * <p>历史：2026-09-20 曾按用户要求放行普通难度的止心术/橡肤（{@code docs/MANOR_PLAYER_BUFFS_20260920.md}），
     * 2026-09-24 是**只把止心术收回来**的部分回滚，橡肤仍然放行。
     */
    public static final List<String> BANNED_SPELLS = List.of("black_hole", "heartstop", "abyssal_shroud");

    private static final String[] LINES = {
            "雕虫小技还敢班门弄斧？",
            "呵，旁门左道",
    };

    private static final Random RNG = new Random();

    private ManorBan() {}

    public static void scold(ServerPlayer p, String what) {
        String line = LINES[RNG.nextInt(LINES.length)];
        p.displayClientMessage(Component.literal("§c§l" + line), true);       // 屏幕中上方
        p.sendSystemMessage(Component.literal("§c§l" + line));                // 聊天栏(留痕)
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8f, 0.6f);
        FTBossMod.LOGGER.info("[FTBoss][Manor] 禁术拦截: {} {} -> 已取消",
                p.getName().getString(), what);
    }

}
