package net.farmingtales.teamlag;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class QueryCommands {
    private QueryCommands() {}
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("teamlag").requires(source -> source.hasPermission(2))
                .executes(context -> query(context.getSource(), null, false))
                .then(Commands.literal("here").executes(context -> query(context.getSource(), null, true)))
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("status").executes(context -> {
                    send(context.getSource(), LagQuery.status());
                    send(context.getSource(), RefinedStorageTiming.status());
                    send(context.getSource(), ObservableReports.status());
                    send(context.getSource(), TimingHooks.status());
                    send(context.getSource(), "/teamlag [团队UUID/在线玩家名/团队名] | here | list | observable [5..60秒]；所有查询均为只读");
                    return 1;
                }))
                .then(Commands.literal("observable").executes(context -> { send(context.getSource(), ObservableReports.status()); return 1; })
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(5, 60)).executes(context -> {
                            send(context.getSource(), ObservableReports.trigger(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "seconds")));
                            return 1;
                        })))
                .then(Commands.argument("team", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            try {
                                var snapshot = TeamDirectory.snapshot();
                                var names = new java.util.ArrayList<String>();
                                snapshot.teams().forEach((id, team) -> { names.add(id.toString()); names.add(StringArgumentType.escapeIfRequired(team.name())); });
                                names.addAll(java.util.List.of(context.getSource().getServer().getPlayerNames()));
                                return SharedSuggestionProvider.suggest(names, builder);
                            } catch (ReflectiveOperationException | RuntimeException exception) { return builder.buildFuture(); }
                        })
                        .executes(context -> query(context.getSource(), StringArgumentType.getString(context, "team"), false))));
    }
    private static int query(CommandSourceStack source, String target, boolean here) {
        try {
            var snapshot = TeamDirectory.snapshot();
            UUID id;
            if (target != null) id = TeamDirectory.resolve(target, snapshot);
            else if (here) {
                source.getPlayerOrException();
                id = snapshot.claims().get(ChunkKey.at(source.getLevel(), BlockPos.containing(source.getPosition())));
            } else {
                var player = source.getPlayer();
                if (player == null) { source.sendFailure(Component.literal("控制台请使用 /teamlag <团队UUID/在线玩家名/团队名>")); return 0; }
                id = TeamDirectory.playerTeam(player.getUUID());
            }
            TeamDirectory.Team team = id == null ? null : TeamDirectory.team(id);
            if (team == null) { source.sendFailure(Component.literal("未找到团队；here 查询脚下认领团队，也可指定团队 UUID、在线玩家名或认领团队名称")); return 0; }
            describe(source, team, snapshot);
            return 1;
        } catch (Exception exception) { return failed(source, exception); }
    }
    private static int list(CommandSourceStack source) {
        try {
            var snapshot = TeamDirectory.snapshot();
            var measurements = new java.util.HashMap<UUID, LagQuery.Measurement>();
            snapshot.teams().keySet().forEach(id -> measurements.put(id, LagQuery.measurement(snapshot.chunks(id))));
            var teams = snapshot.teams().values().stream().sorted(Comparator
                    .comparingDouble((TeamDirectory.Team team) -> measurements.get(team.id()).micros()).reversed()).limit(50).toList();
            send(source, "团队卡顿总值排行（当前所有维度认领区块，最多 50 队）：");
            for (var team : teams) describe(source, team, snapshot, measurements.get(team.id()));
            if (teams.isEmpty()) send(source, "当前没有团队认领区块；暂无采样不等于卡顿为零");
            return teams.size();
        } catch (Exception exception) { return failed(source, exception); }
    }
    private static void describe(CommandSourceStack source, TeamDirectory.Team team, TeamDirectory.Snapshot snapshot) {
        describe(source, team, snapshot, LagQuery.measurement(snapshot.chunks(team.id())));
    }
    private static void describe(CommandSourceStack source, TeamDirectory.Team team, TeamDirectory.Snapshot snapshot, LagQuery.Measurement measurement) {
        var chunks = snapshot.chunks(team.id());
        int divisor = LagQuery.ONLINE_ONLY.get() ? team.online() : team.members();
        String perCapita = divisor <= 0 ? "不适用（无人在线/无成员）" : String.format(Locale.ROOT, "%.2f μs/tick", measurement.micros() / divisor);
        send(source, String.format(Locale.ROOT, "团队「%s」UUID=%s；在线 %d/%d 人；认领 %d 区块（跨维度）", team.name(), team.id(), team.online(), team.members(), chunks.size()));
        if (measurement.sampled() == 0) { send(source, "  暂无实测数据（不等于 0）；先运行机器，或 /teamlag observable 10 采样；未加载区块不会被强制加载"); return; }
        send(source, String.format(Locale.ROOT, "  总值 %.2f μs/tick；人均 %s（按%s %d 人）；覆盖 %d/%d 区块；%s，%d tick。仅查询，不产生任何限制。",
                measurement.micros(), perCapita, LagQuery.ONLINE_ONLY.get() ? "在线" : "全部成员", divisor, measurement.sampled(), chunks.size(), measurement.source(), measurement.ticks()));
    }
    private static int failed(CommandSourceStack source, Exception exception) {
        source.sendFailure(Component.literal("查询失败：FTB 团队/认领数据不可用，或目标不唯一；" + exception.getMessage()));
        return 0;
    }
    private static void send(CommandSourceStack source, String text) { source.sendSuccess(() -> Component.literal(text), false); }
}
