package cn.farmingtales.qysevent.worldevent;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import cn.farmingtales.qysevent.QysEventMod;
import cn.farmingtales.qysevent.WeatherManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

/** Operator test/list command for every daily event. */
public final class DailyWorldEventCommand {
    private DailyWorldEventCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("qysevent")
                .then(buildEventNode("event"))
                .then(Commands.literal("status").executes(context -> status(context.getSource()))));
        event.getDispatcher().register(buildEventNode("ftevent"));
        // Compatibility with the old event command; qys-bosses no longer owns this branch.
        event.getDispatcher().register(Commands.literal("ftboss").then(buildEventNode("event")));
        QysEventMod.LOGGER.info("[QysEvent] registered /qysevent event, /ftevent and /ftboss event");
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildEventNode(String name) {
        return Commands.literal(name)
                .requires(DailyWorldEventCommand::canUse)
                .executes(DailyWorldEventCommand::list)
                .then(Commands.literal("list").executes(DailyWorldEventCommand::list))
                .then(Commands.literal("trigger")
                        .then(Commands.argument("event", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        java.util.Arrays.stream(DailyWorldEvent.values()).map(DailyWorldEvent::id), builder))
                                .executes(DailyWorldEventCommand::trigger)))
                .then(Commands.literal("clear").executes(DailyWorldEventCommand::clear));
    }


    private static int status(CommandSourceStack source) {
        DailyWorldEvent current = DailyWorldEventManager.active(source.getServer());
        Component daily = current == null
                ? Component.translatable("message.qys_event.state.not_triggered")
                : Component.translatable("message.qys_event.state.triggered", current.title());
        source.sendSuccess(() -> Component.translatable("message.qys_event.status.daily", daily), false);
        source.sendSuccess(() -> Component.literal(WeatherManager.statusLine(source.getLevel())), false);
        return 1;
    }

    private static boolean canUse(CommandSourceStack source) {
        return source.hasPermission(2);
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        DailyWorldEvent current = DailyWorldEventManager.active(server);
        Component currentName = current == null
                ? Component.translatable("message.qys_event.state.not_triggered")
                : Component.translatable("message.qys_event.state.triggered", current.title());
        source.sendSuccess(() -> Component.translatable("message.qys_event.event_test.header",
                DailyWorldEvent.values().length, currentName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        for (DailyWorldEvent worldEvent : DailyWorldEvent.values()) {
            MutableComponent button = Component.translatable("message.qys_event.event_test.trigger_button")
                    .withStyle(style -> style.withColor(ChatFormatting.GREEN).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/ftevent trigger " + worldEvent.id()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("message.qys_event.event_test.trigger_hover",
                                            worldEvent.title()))));
            MutableComponent line = Component.literal("  ").append(button).append(Component.literal(" "))
                    .append(worldEvent.title().copy().withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("  [" + worldEvent.id() + "]").withStyle(ChatFormatting.DARK_GRAY));
            source.sendSuccess(() -> line, false);
        }
        MutableComponent clearButton = Component.translatable("message.qys_event.event_test.clear_button")
                .withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftevent clear"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("message.qys_event.event_test.clear_hover"))));
        source.sendSuccess(() -> Component.literal("  ").append(clearButton), false);
        return DailyWorldEvent.values().length;
    }

    private static int trigger(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "event");
        DailyWorldEvent selected = DailyWorldEvent.byId(id);
        if (selected == null) {
            source.sendFailure(Component.translatable("message.qys_event.event_test.unknown", id));
            return 0;
        }
        DailyWorldEventManager.setActiveForTesting(source.getServer(), selected);
        source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                "message.qys_event.event_test.forced", selected.title(), selected.id())
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        DailyWorldEventManager.setActiveForTesting(context.getSource().getServer(), null);
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                "message.qys_event.event_test.cleared").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }
}


