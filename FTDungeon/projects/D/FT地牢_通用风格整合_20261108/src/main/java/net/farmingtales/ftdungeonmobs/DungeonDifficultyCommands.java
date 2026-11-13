package net.farmingtales.ftdungeonmobs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class DungeonDifficultyCommands {
    private DungeonDifficultyCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ftdungeon").requires(source -> source.hasPermission(2))
                .then(Commands.literal("difficulty")
                        .then(Commands.literal("reload")
                                .executes(context -> {
                                    try {
                                        int count = DungeonDifficultyApi.reloadConfig(context.getSource().getServer());
                                        context.getSource().sendSuccess(() -> Component.literal("FT地牢怪物：已从文件重新加载配置，并刷新 " + count + " 个已加载实体。"), true);
                                        return count;
                                    } catch (RuntimeException exception) {
                                        throw new SimpleCommandExceptionType(Component.literal("难度配置重载失败，继续保留已应用的属性；请检查 "
                                                + DungeonDifficultyConfig.FILE_NAME + " 的语法和数值范围。")).create();
                                    }
                                }))
                        .then(Commands.literal("get")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> inspect(context.getSource(), EntityArgument.getEntities(context, "targets")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(DungeonDifficultyConfig.MIN_TIER, DungeonDifficultyConfig.MAX_TIER))
                                                .executes(context -> change(context.getSource(), EntityArgument.getEntities(context, "targets"),
                                                        entity -> DungeonDifficultyApi.setTier(entity, IntegerArgumentType.getInteger(context, "tier")), "已设置固定难度档位")))))
                        .then(Commands.literal("auto")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> change(context.getSource(), EntityArgument.getEntities(context, "targets"),
                                                DungeonDifficultyApi::useAutomaticTier, "已恢复自动难度，保留单体属性加成"))))
                        .then(Commands.literal("bonus")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("stat", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(DifficultyProfile.STAT_NAMES, builder))
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(context -> setBonus(context.getSource(), EntityArgument.getEntities(context, "targets"),
                                                                StringArgumentType.getString(context, "stat"), DoubleArgumentType.getDouble(context, "value")))))))
                        .then(Commands.literal("clear_bonus")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> change(context.getSource(), EntityArgument.getEntities(context, "targets"),
                                                DungeonDifficultyApi::clearOverrides, "已清除单体加成，保留档位和配置加成"))))
                        .then(Commands.literal("refresh")
                                .executes(context -> {
                                    int count = DungeonDifficultyApi.refreshAll(context.getSource().getServer());
                                    context.getSource().sendSuccess(() -> Component.literal("FT地牢怪物：已刷新所有已加载维度中的 " + count + " 个实体；未强制加载区块。"), true);
                                    return count;
                                })
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> change(context.getSource(), EntityArgument.getEntities(context, "targets"),
                                                DungeonDifficultyApi::refresh, "已按当前配置重新计算属性"))))));
    }

    private static int setBonus(CommandSourceStack source, Collection<? extends Entity> targets, String stat, double value)
            throws CommandSyntaxException {
        try {
            boolean inRange = switch (stat) {
                case "health" -> value >= 0.05 && value <= 100;
                case "attack", "spell_power" -> value >= 0 && value <= 100;
                case "armor" -> value >= -30 && value <= 30;
                case "magic_armor" -> value >= -0.9 && value <= 99;
                default -> false;
            };
            if (!inRange) {
                throw new IllegalArgumentException("Value outside the supported command range");
            }
            DifficultyProfile.IDENTITY.with(stat, value);
            return change(source, targets, entity -> DungeonDifficultyApi.setOverrides(entity,
                    DungeonDifficultyApi.getOverrides(entity).with(stat, value)), "已设置单体 " + stat + " = " + value);
        } catch (IllegalArgumentException exception) {
            throw new SimpleCommandExceptionType(Component.literal("属性无效：health 0.05~100，attack / spell_power 0~100，armor -30~30，magic_armor -0.9~99；属性名为 "
                    + String.join(", ", DifficultyProfile.STAT_NAMES))).create();
        }
    }

    private static int change(CommandSourceStack source, Collection<? extends Entity> targets, Consumer<DungeonSkeletonEntity> action,
                              String message) throws CommandSyntaxException {
        List<DungeonSkeletonEntity> monsters = monsters(targets);
        monsters.forEach(action);
        source.sendSuccess(() -> Component.literal("FT地牢怪物：" + message + "，共 " + monsters.size() + " 个实体（保留剩余血量比例）。"), true);
        return monsters.size();
    }

    private static int inspect(CommandSourceStack source, Collection<? extends Entity> targets) throws CommandSyntaxException {
        List<DungeonSkeletonEntity> monsters = monsters(targets);
        for (DungeonSkeletonEntity entity : monsters) {
            DungeonDifficultyApi.Snapshot stats = DungeonDifficultyApi.inspect(entity);
            String text = String.format(Locale.ROOT,
                    " [T%d %s] 生命 %.2f/%.2f，攻击 %.2f，物理护甲 %.2f，铁魔法魔抗属性 %.2f，法强 %.2f 倍",
                    stats.tier(), stats.automatic() ? "自动" : "固定", stats.health(), stats.maxHealth(), stats.attackDamage(),
                    stats.physicalArmor(), stats.magicResistance(), stats.spellPower());
            source.sendSuccess(() -> Component.empty().append(entity.getDisplayName()).append(text), false);
        }
        return monsters.size();
    }

    private static List<DungeonSkeletonEntity> monsters(Collection<? extends Entity> targets) throws CommandSyntaxException {
        List<DungeonSkeletonEntity> monsters = targets.stream().filter(DungeonSkeletonEntity.class::isInstance)
                .map(DungeonSkeletonEntity.class::cast).toList();
        if (monsters.isEmpty()) {
            throw new SimpleCommandExceptionType(Component.literal("没有选中 FT 地牢怪物；不会修改原版怪物、玩家或其他模组实体。")).create();
        }
        return monsters;
    }
}
