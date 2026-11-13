package net.farmingtales.chunkdrops;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

final class ChunkDropCommands {
    static final int PAGE_SIZE = 10;
    private static final Logger LOGGER = LogUtils.getLogger();

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("chunkdrops")
                .requires(source -> source.hasPermission(2))
                .executes(context -> top(context.getSource(), 10))
                .then(Commands.literal("help").executes(context -> help(context.getSource())))
                .then(Commands.literal("top").executes(context -> top(context.getSource(), 10))
                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                .executes(context -> top(context.getSource(), IntegerArgumentType.getInteger(context, "limit")))))
                .then(Commands.literal("here").executes(context -> here(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> here(context.getSource(), IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("chunk")
                        .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                                .then(Commands.argument("chunkZ", IntegerArgumentType.integer())
                                        .executes(context -> chunk(context.getSource(), IntegerArgumentType.getInteger(context, "chunkX"),
                                                IntegerArgumentType.getInteger(context, "chunkZ"), 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(context -> chunk(context.getSource(), IntegerArgumentType.getInteger(context, "chunkX"),
                                                        IntegerArgumentType.getInteger(context, "chunkZ"), IntegerArgumentType.getInteger(context, "page")))))))
                .then(Commands.literal("report").executes(context -> report(context.getSource()))));
    }

    private static int top(CommandSourceStack source, int limit) {
        DropScanner.Snapshot snapshot = DropScanner.scan(source.getServer());
        reply(source, "[掉落物] 全服 " + snapshot.dimensions() + " 个维度：掉落物实体 " + snapshot.entityCount()
                + "，物品总数 " + snapshot.itemCount() + "，有掉落物的区块 " + snapshot.chunks().size()
                + "，扫描 " + String.format(Locale.ROOT, "%.2f ms", snapshot.elapsedNanos() / 1_000_000.0));
        reply(source, "仅统计已加载且可访问的掉落物，按实体数量降序；64 个堆叠物品算 1 个实体 / 64 个物品。");
        for (int index = 0; index < Math.min(limit, snapshot.chunks().size()); index++) {
            DropScanner.ChunkDrops chunk = snapshot.chunks().get(index);
            DropScanner.ChunkKey key = chunk.key();
            DropScanner.Drop sample = chunk.drops().get(0);
            MutableComponent line = Component.literal("#" + (index + 1) + " " + key.dimension() + " 区块 [" + key.x() + ", "
                    + key.z() + "] 实体 " + chunk.entityCount() + " / 物品 " + chunk.itemCount()
                    + "，其中一处 " + coordinates(sample) + " ");
            line.append(link("[查看]", queryCommand(key, 1), "填入此区块的掉落物明细指令"));
            source.sendSuccess(() -> line, false);
        }
        return (int) Math.min(Integer.MAX_VALUE, snapshot.entityCount());
    }

    private static int here(CommandSourceStack source, int page) {
        int chunkX = Mth.floor(source.getPosition().x) >> 4;
        int chunkZ = Mth.floor(source.getPosition().z) >> 4;
        return chunk(source, chunkX, chunkZ, page);
    }

    private static int chunk(CommandSourceStack source, int chunkX, int chunkZ, int page) {
        if (!source.getLevel().hasChunk(chunkX, chunkZ)) {
            source.sendFailure(Component.literal("[掉落物] 区块 [" + chunkX + ", " + chunkZ + "] 未加载；不会强制加载。"));
            return 0;
        }
        DropScanner.ChunkDrops chunk = DropScanner.scanChunk(source.getLevel(), chunkX, chunkZ);
        int pages = Math.max(1, (int) ((chunk.entityCount() + (long) PAGE_SIZE - 1) / PAGE_SIZE));
        if (page > pages) {
            source.sendFailure(Component.literal("[掉落物] 页码超出范围，共 " + pages + " 页。"));
            return 0;
        }
        reply(source, "[掉落物] " + chunk.key().dimension() + " 区块 [" + chunkX + ", " + chunkZ + "]：实体 "
                + chunk.entityCount() + " / 物品总数 " + chunk.itemCount() + "，第 " + page + "/" + pages + " 页");
        long minX = chunkX * 16L;
        long minZ = chunkZ * 16L;
        reply(source, "方块范围 X " + minX + ".." + (minX + 15) + "，Z " + minZ + ".." + (minZ + 15) + "；覆盖全部高度。");
        int start = (page - 1) * PAGE_SIZE;
        int end = (int) Math.min(start + (long) PAGE_SIZE, chunk.entityCount());
        for (int index = start; index < end; index++) {
            DropScanner.Drop drop = chunk.drops().get(index);
            String teleport = "/execute in " + chunk.key().dimension() + " run tp @s "
                    + drop.x() + " " + drop.y() + " " + drop.z();
            MutableComponent line = Component.literal("#" + (index + 1) + " " + drop.name() + " (" + drop.itemId()
                    + ") ×" + drop.count() + " ");
            line.append(link(coordinates(drop), teleport, "填入传送指令，不会自动传送\n实体 UUID：" + drop.uuid()));
            source.sendSuccess(() -> line, false);
        }
        if (page < pages) {
            source.sendSuccess(() -> link("[下一页]", queryCommand(chunk.key(), page + 1), "填入下一页查询指令"), false);
        }
        return chunk.entityCount();
    }

    private static int report(CommandSourceStack source) {
        DropScanner.Snapshot snapshot = DropScanner.scan(source.getServer());
        Path directory = source.getServer().getServerDirectory().toPath().resolve("reports").resolve("chunkdrops");
        try {
            Path file = DropReport.write(snapshot, directory);
            reply(source, "[掉落物] 已导出全维度已加载掉落物：实体 " + snapshot.entityCount()
                    + " / 物品总数 " + snapshot.itemCount() + "；文件 " + file.toAbsolutePath());
            return 1;
        } catch (IOException exception) {
            LOGGER.error("Cannot export chunk drop report to {}", directory, exception);
            source.sendFailure(Component.literal("[掉落物] 报告导出失败，请查看服务器日志：" + exception.getMessage()));
            return 0;
        }
    }

    private static int help(CommandSourceStack source) {
        reply(source, "/chunkdrops [top [1-50]]：全维度掉落物区块排行，默认前 10。");
        reply(source, "/chunkdrops here [页码]：当前执行位置所在区块的掉落物与坐标。");
        reply(source, "/chunkdrops chunk <区块X> <区块Z> [页码]：当前执行维度的指定区块明细，每页 10 条。");
        reply(source, "/execute in <维度ID> run chunkdrops chunk <区块X> <区块Z>：跨维度查询。");
        reply(source, "/chunkdrops report：导出全维度 CSV，包含区块、物品 ID、数量、精确坐标及实体 UUID。");
        reply(source, "需要 OP 2 级或控制台权限；只读查询，不加载区块、不清理实体；未加载区块不在统计内。");
        return 1;
    }

    private static void reply(CommandSourceStack source, String text) {
        source.sendSuccess(() -> Component.literal(text), false);
    }

    private static String coordinates(DropScanner.Drop drop) {
        return String.format(Locale.ROOT, "(%.3f, %.3f, %.3f)", drop.x(), drop.y(), drop.z());
    }

    private static String queryCommand(DropScanner.ChunkKey key, int page) {
        return "/execute in " + key.dimension() + " run chunkdrops chunk " + key.x() + " " + key.z() + " " + page;
    }

    private static MutableComponent link(String text, String command, String tooltip) {
        return Component.literal(text).withStyle(style -> style.withColor(ChatFormatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tooltip))));
    }
}
