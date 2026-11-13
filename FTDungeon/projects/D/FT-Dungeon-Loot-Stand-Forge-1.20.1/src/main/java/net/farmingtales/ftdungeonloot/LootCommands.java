package net.farmingtales.ftdungeonloot;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class LootCommands {
    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("ftloot").requires(source -> source.hasPermission(2));
        root.then(Commands.literal("profiles").executes(context -> {
            context.getSource().sendSuccess(() -> Component.literal(LootProfiles.ids().toString()), false);
            return LootProfiles.ids().size();
        }));
        for (String operation : new String[]{"stand", "chest"}) {
            root.then(Commands.literal(operation).then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .then(Commands.argument("profile", ResourceLocationArgument.id())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(LootProfiles.ids(), builder))
                            .executes(context -> bind(context, operation)))));
        }
        root.then(Commands.literal("nextrun").executes(context -> {
            LootRunData data = LootRunData.get(context.getSource().getLevel());
            data.next();
            context.getSource().sendSuccess(() -> Component.translatable("message.ftdungeonloot.new_run", data.run().toString()), true);
            return 1;
        }));
        root.then(Commands.literal("inspect").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(context -> {
            var position = BlockPosArgument.getLoadedBlockPos(context, "pos");
            var entity = context.getSource().getLevel().getBlockEntity(position);
            if (entity instanceof LootStandEntity stand) {
                stand.ensureOffer();
                context.getSource().sendSuccess(() -> Component.literal("profile=" + stand.profileId() + " price=" + stand.price()
                        + " currency=" + stand.currencyId() + " sold=" + stand.sold() + " run=" + stand.run() + " reward=" + stand.reward()), false);
            } else {
                context.getSource().sendSuccess(() -> Component.literal(entity == null ? "No block entity" : entity.getPersistentData().toString()), false);
            }
            return 1;
        })));
        event.getDispatcher().register(root);
    }

    private static int bind(CommandContext<CommandSourceStack> context, String operation) throws CommandSyntaxException {
        var position = BlockPosArgument.getLoadedBlockPos(context, "pos");
        var profileId = ResourceLocationArgument.getId(context, "profile");
        LootProfile profile = LootProfiles.find(profileId).orElseThrow(() -> failure("Unknown normal chest profile: " + profileId));
        var level = context.getSource().getLevel();
        if (!profile.dimension().equals(level.dimension().location())) throw failure("Wrong dimension for profile");
        if (profile.currencyItem() == null) throw failure("Missing currency item: " + profile.currency());
        try {
            if (operation.equals("stand")) {
                if (!(level.getBlockEntity(position) instanceof LootStandEntity stand)) throw new IllegalArgumentException("Not a loot stand");
                stand.configure(profileId);
            } else {
                DungeonChests.bind(level, position, profile);
            }
        } catch (IllegalArgumentException exception) { throw failure(exception.getMessage()); }
        context.getSource().sendSuccess(() -> Component.translatable("message.ftdungeonloot.bound", operation, position.toShortString(), profileId.toString()), true);
        return 1;
    }

    private static CommandSyntaxException failure(String message) { return new SimpleCommandExceptionType(Component.literal(message)).create(); }
}
