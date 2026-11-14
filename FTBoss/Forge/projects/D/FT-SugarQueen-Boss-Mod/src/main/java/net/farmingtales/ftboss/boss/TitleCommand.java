package net.farmingtales.ftboss.boss;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 「头衔」补发命令 —— 2026-09-19 v175 按用户要求「触发成就来发放 / 参考其他头衔触发」。
 *
 * <p>Titles 模组的头衔是**靠进度触发**的：给某个进度打上 criterion，
 * 原版发出 {@code AdvancementEarnEvent}，该模组再拿进度 id 去查同名头衔。
 * 它自己给 {@code captain} / {@code melon_lord} 这类特殊头衔也是这么做的 ——
 * 用一条 {@code code_triggered}（{@code minecraft:impossible}）的进度，
 * 由代码在合适的时机打勾。
 *
 * <p>本模组照这个套路，为两个头衔各建了一条**隐形功能进度**
 * （{@code ftboss:title_memories_end} / {@code ftboss:title_food_boss_slayer_all}，
 * 故意没有 display，所以不会出现在进度界面里）：
 * <pre>
 *   /ftboss title                      # 补发自己已达成门槛的头衔
 *   /ftboss title &lt;玩家&gt;               # 补发给指定玩家
 *   /ftboss title &lt;玩家&gt; &lt;头衔进度id&gt;   # 只补发指定那一个
 * </pre>
 * 执行时会**先收回再打勾**，强制重放一次"未完成 → 完成"，
 * 因此即使玩家早就拿过成就（或成就来自装头衔文件之前的旧存档），也能补上头衔。
 *
 * <p>门槛：只有**已经拿到对应成就**的玩家才会被补发 ——
 * 头衔不会凭空发给没打过的人；没达成时命令会明确告诉你还差哪一个。
 *
 * <p>权限等级 2（OP）。没装 Titles 模组时命令照样能用（只是打完勾没人接），
 * 不会报错。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class TitleCommand {

    private TitleCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("ftboss")
                .then(Commands.literal("title")
                        .requires(src -> src.hasPermission(2))
                        /* 无参数 = 给自己补发全部已达成门槛的头衔 */
                        .executes(ctx -> grant(ctx.getSource().getPlayerOrException(), null))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> grant(EntityArgument.getPlayer(ctx, "player"), null))
                                .then(Commands.literal("memories_end").executes(ctx -> grant(
                                        EntityArgument.getPlayer(ctx, "player"), "title_memories_end")))
                                .then(Commands.literal("food_boss_slayer_all").executes(ctx -> grant(
                                        EntityArgument.getPlayer(ctx, "player"), "title_food_boss_slayer_all")))
                                /* v183: 终结难度庄园 -> 「12号位」 */
                                .then(Commands.literal("manor_impossible").executes(ctx -> grant(
                                        EntityArgument.getPlayer(ctx, "player"), "title_manor_impossible"))))));
    }

    /**
     * @param titleId 只补发这一个头衔进度；为 null 表示"全部已达成门槛的"
     */
    private static int grant(ServerPlayer target, String titleId) {
        List<String> ids = titleId != null
                ? List.of(titleId)
                : List.copyOf(BossAdvancements.titleAdvancementIds());

        int granted = 0;
        int notEligible = 0;
        for (String id : ids) {
            String gate = BossAdvancements.titleGate(id);
            if (gate == null) continue;                 // 这张表里没登记
            /* ★ v183: **空串门槛 = 没有可查的成就**（如「12号位」——
               终结难度通关本身不发成就）。这种情况**跳过校验、允许补发**：
               管理员用它补发就是显式授权；普通玩家没有 OP 也执行不了这条命令。 */
            if (!gate.isEmpty() && !BossAdvancements.hasEarned(target, gate)) {
                notEligible++;
                target.sendSystemMessage(Component.literal(
                        "§7【头衔】§c还差成就§f " + gateName(gate) + "§c，暂不补发。"));
                continue;
            }
            boolean ok = BossAdvancements.replayTitle(target, id);
            if (ok) granted++;
        }

        if (granted > 0) {
            target.sendSystemMessage(Component.literal(
                    "§d【头衔】§f已补发 §e" + granted + " §f个头衔 —— "
                    + "§7用头衔界面（默认快捷键 §fT§7）选择要显示的称号。"));
            FTBossMod.LOGGER.info("[FTBoss][头衔] 为 {} 补发 {} 个头衔", target.getName().getString(), granted);
        } else if (notEligible == 0) {
            target.sendSystemMessage(Component.literal("§7【头衔】没有可补发的头衔。"));
        }
        return granted;
    }

    /** 门槛进度的中文说法（给玩家看的提示用）。 */
    private static String gateName(String gateId) {
        return switch (gateId) {
            case "memories_end" -> "回忆的终结（战胜庄园）";
            case "food_boss_slayer_all" -> "美食的俘虏（战胜十位非庄园 Boss）";
            default -> gateId;
        };
    }
}
