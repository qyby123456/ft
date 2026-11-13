package net.farmingtales.ftdungeontrading;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.farmingtales.ftdungeontrading.api.DungeonTradingApi;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class TradeCommands {
    private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType(
            reason -> Component.translatable("message.ftdungeontrading.command_error", reason));

    private TradeCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("fttrade").requires(source -> source.hasPermission(2))
                .then(Commands.literal("offers").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(String.join(", ", DungeonTradingApi.offerIds().stream().map(Object::toString).toList())), false);
                    return DungeonTradingApi.offerIds().size();
                }))
                .then(Commands.literal("bind")
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                                .then(Commands.argument("offer", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(DungeonTradingApi.offerIds(), builder))
                                        .executes(context -> {
                                            var source = context.getSource();
                                            var position = BlockPosArgument.getLoadedBlockPos(context, "position");
                                            var offer = ResourceLocationArgument.getId(context, "offer");
                                            try { DungeonTradingApi.bindTable(source.getLevel(), position, offer, -1); }
                                            catch (IllegalArgumentException exception) { throw ERROR.create(exception.getMessage()); }
                                            source.sendSuccess(() -> Component.translatable("message.ftdungeontrading.bound", position.toShortString(), offer), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("stock")
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                                .then(Commands.argument("count", IntegerArgumentType.integer(-1, 1000000)).executes(context -> {
                                    var source = context.getSource();
                                    var position = BlockPosArgument.getLoadedBlockPos(context, "position");
                                    var table = DungeonTradingApi.table(source.getLevel(), position).orElseThrow(() -> ERROR.create("No trading table"));
                                    int stock = IntegerArgumentType.getInteger(context, "count");
                                    try { DungeonTradingApi.bindTable(source.getLevel(), position, table.offerId(), stock); }
                                    catch (IllegalArgumentException exception) { throw ERROR.create(exception.getMessage()); }
                                    source.sendSuccess(() -> Component.translatable("message.ftdungeontrading.stock_set", stock), true);
                                    return 1;
                                }))))
                .then(Commands.literal("inspect").then(Commands.argument("position", BlockPosArgument.blockPos()).executes(context -> {
                    var source = context.getSource();
                    var position = BlockPosArgument.getLoadedBlockPos(context, "position");
                    var table = DungeonTradingApi.table(source.getLevel(), position).orElseThrow(() -> ERROR.create("No trading table"));
                    source.sendSuccess(() -> Component.translatable("message.ftdungeontrading.inspected", table.offerId(), table.remainingStock(), table.changeVersion()), false);
                    return 1;
                }))));
    }
}
