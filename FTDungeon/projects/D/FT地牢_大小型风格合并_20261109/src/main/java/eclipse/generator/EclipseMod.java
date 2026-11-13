package eclipse.generator;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.function.IntSupplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(EclipseMod.ID)
public final class EclipseMod {
    public static final String ID = "fteclipsegen";
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(ID, "clockwork"));
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    public static final RegistryObject<Block> RUNE = BLOCKS.register("encounter_rune", () -> new Block(BlockBehaviour.Properties.of().strength(-1, 3600000).sound(SoundType.DEEPSLATE).noOcclusion()) {
        private final VoxelShape shape = Block.box(0, 0, 0, 16, 2, 16);
        @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) { return shape; }
    });

    public EclipseMod() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static ServerLevel target(CommandSourceStack source) {
        ServerLevel level = source.getServer().getLevel(DIMENSION);
        Catalog.require(level != null, "地牢维度尚未加载；确认完整安装模组后重启世界");
        return level;
    }

    private static int execute(CommandSourceStack source, IntSupplier action) {
        try { return action.getAsInt(); }
        catch (RuntimeException exception) {
            source.sendFailure(Component.literal("月蚀生成器：" + exception.getMessage()));
            return 0;
        }
    }

    private static String identifier(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) { return StringArgumentType.getString(context, "id"); }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("eclipse")
            .then(Commands.literal("generate").requires(source -> source.hasPermission(2))
                .then(Commands.argument("seed", LongArgumentType.longArg()).executes(context -> execute(context.getSource(), () -> {
                    ServerLevel level = target(context.getSource());
                    long seed = LongArgumentType.getLong(context, "seed");
                    Engine.Job job = Store.get(level).jobs.values().stream().filter(value -> value.plan.seed == seed
                        && Profiles.DEFAULT.equals(Profiles.style(value.plan))).findFirst().orElse(null);
                    if (job == null) job = Engine.plan(level, seed, new BlockPos(Store.get(level).jobs.size() * 2048, 100, 0));
                    Engine.start(level, job.plan.id);
                    String message = "实例 " + job.plan.id + " · " + job.plan.pieces.size() + " 模块 · " + job.stage + "；完成后 /eclipse enter " + job.plan.id;
                    context.getSource().sendSuccess(() -> Component.literal(message), false);
                    return 1;
                }))))
            .then(Commands.literal("plan").requires(source -> source.hasPermission(2))
                .then(Commands.argument("position", BlockPosArgument.blockPos()).then(Commands.argument("seed", LongArgumentType.longArg()).executes(context -> execute(context.getSource(), () -> {
                    Engine.Job job = Engine.plan(target(context.getSource()), LongArgumentType.getLong(context, "seed"), BlockPosArgument.getBlockPos(context, "position"));
                    context.getSource().sendSuccess(() -> Component.literal("已保存计划 " + job.plan.id + "，尚未写入方块；/eclipse build " + job.plan.id), false);
                    return 1;
                })))))
            .then(Commands.literal("build").requires(source -> source.hasPermission(2)).then(Commands.argument("id", StringArgumentType.word()).executes(context -> execute(context.getSource(), () -> {
                Engine.start(target(context.getSource()), identifier(context));
                context.getSource().sendSuccess(() -> Component.literal("已提交预检与分批建造"), false); return 1;
            }))))
            .then(Commands.literal("pause").requires(source -> source.hasPermission(2)).then(Commands.argument("id", StringArgumentType.word()).executes(context -> execute(context.getSource(), () -> {
                Engine.pause(target(context.getSource()), identifier(context));
                context.getSource().sendSuccess(() -> Component.literal("已暂停并保存进度"), false); return 1;
            }))))
            .then(Commands.literal("status").executes(context -> execute(context.getSource(), () -> {
                for (Engine.Job job : Store.get(target(context.getSource())).jobs.values()) context.getSource().sendSuccess(() -> Component.literal(job.plan.id + " " + job.stage + " " + job.pieceCursor + "/" + job.plan.pieces.size() + (job.error == null ? "" : " " + job.error)), false);
                return 1;
            })))
            .then(Commands.literal("enter").then(Commands.argument("id", StringArgumentType.word()).executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                return execute(context.getSource(), () -> { Engine.enter(player, identifier(context)); return 1; });
            })))
            .then(Commands.literal("leave").executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                return execute(context.getSource(), () -> { leave(player); return 1; });
            })));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> styleArgument() {
        return Commands.argument("style", ResourceLocationArgument.id()).suggests((context, builder) ->
            SharedSuggestionProvider.suggest(Profiles.all().stream().map(style -> style.id), builder));
    }

    private static int styledGenerate(CommandSourceStack source, String styleId, long seed) {
        Profiles.get(styleId);
        ServerLevel level = target(source);
        Engine.Job job = Store.get(level).jobs.values().stream().filter(value -> value.plan.seed == seed
            && styleId.equals(Profiles.style(value.plan))).findFirst().orElse(null);
        if (job == null) job = Engine.plan(level, styleId, seed, new BlockPos(Store.get(level).jobs.size() * 2048, 100, 0));
        Engine.start(level, job.plan.id);
        String message = Profiles.get(styleId).title + " · " + job.plan.id + " · " + job.stage + "；完成后 /eclipse enter " + job.plan.id;
        source.sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    @SubscribeEvent
    public void styleCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("eclipse")
            .then(Commands.literal("styles").executes(context -> execute(context.getSource(), () -> {
                for (Profiles.Style style : Profiles.all()) context.getSource().sendSuccess(() -> Component.literal(style.id + " · " + style.title), false);
                return 1;
            })))
            .then(Commands.literal("generate").requires(source -> source.hasPermission(2))
                .then(styleArgument().then(Commands.argument("seed", LongArgumentType.longArg()).executes(context -> execute(context.getSource(), () ->
                    styledGenerate(context.getSource(), ResourceLocationArgument.getId(context, "style").toString(), LongArgumentType.getLong(context, "seed")))))))
            .then(Commands.literal("plan").requires(source -> source.hasPermission(2))
                .then(Commands.argument("position", BlockPosArgument.blockPos()).then(styleArgument()
                    .then(Commands.argument("seed", LongArgumentType.longArg()).executes(context -> execute(context.getSource(), () -> {
                        Engine.Job job = Engine.plan(target(context.getSource()), ResourceLocationArgument.getId(context, "style").toString(),
                            LongArgumentType.getLong(context, "seed"), BlockPosArgument.getBlockPos(context, "position"));
                        context.getSource().sendSuccess(() -> Component.literal("已保存 " + Profiles.style(job.plan) + " 计划 " + job.plan.id + "；/eclipse build " + job.plan.id), false);
                        return 1;
                    })))))));
    }

    public static void enter(ServerPlayer player, String id) {
        ServerLevel level = player.server.getLevel(DIMENSION);
        Catalog.require(level != null, "地牢维度不可用");
        Engine.Job job = Store.get(level).jobs.get(id);
        Catalog.require(job != null && job.stage.equals("COMPLETE"), "仅可进入建造完成的实例");
        CompoundTag data = player.getPersistentData();
        if (!player.level().dimension().equals(DIMENSION)) {
            CompoundTag back = new CompoundTag();
            back.putString("dimension", player.level().dimension().location().toString());
            back.putDouble("x", player.getX()); back.putDouble("y", player.getY()); back.putDouble("z", player.getZ());
            back.putFloat("yaw", player.getYRot()); back.putFloat("pitch", player.getXRot());
            data.put("fteclipsegen_return", back);
        }
        BlockPos entry = Catalog.pos(job.plan.entry);
        player.teleportTo(level, entry.getX() + .5, entry.getY(), entry.getZ() + .5, 180, 0);
        player.fallDistance = 0;
    }

    public static void leave(ServerPlayer player) {
        Catalog.require(player.level().dimension().equals(DIMENSION), "你不在月蚀地牢维度");
        CompoundTag data = player.getPersistentData();
        Catalog.require(data.contains("fteclipsegen_return"), "没有安全返回记录，请管理员处理");
        CompoundTag back = data.getCompound("fteclipsegen_return");
        ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(back.getString("dimension"))));
        Catalog.require(level != null, "原维度不存在，保留返回记录");
        player.teleportTo(level, back.getDouble("x"), back.getDouble("y"), back.getDouble("z"), back.getFloat("yaw"), back.getFloat("pitch"));
        player.fallDistance = 0; data.remove("fteclipsegen_return");
    }

    @SubscribeEvent public void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level && level.dimension().equals(DIMENSION)) {
            Engine.tick(level, 8192);
            if (level.getGameTime() % 20 == 0) Gameplay.tick(level);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public void death(LivingDeathEvent event) { Gameplay.death(event); }
    @SubscribeEvent public void breakBlock(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(DIMENSION) && Engine.protectedAt(level, event.getPos())) event.setCanceled(true);
    }
    @SubscribeEvent public void placeBlock(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(DIMENSION) && Engine.protectedAt(level, event.getPos())) event.setCanceled(true);
    }
    @SubscribeEvent public void explosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(DIMENSION)) event.getAffectedBlocks().removeIf(position -> Engine.protectedAt(level, position));
    }
}
