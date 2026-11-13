package net.farmingtales.ftdungeonmobs;

import java.util.Optional;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerator;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerators;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.state.BlockState;

public final class DungeonGateGenerators {
    private static final String RETURN_GATE = "FTGeneratorReturnGate";
    private DungeonGateGenerators() {}

    public static boolean automaticAllowed(ServerPlayer player, BlockPos anchor) {
        net.minecraft.nbt.CompoundTag saved = player.getPersistentData().getCompound(RETURN_GATE);
        return !saved.contains("Anchor") || saved.getLong("Anchor") != anchor.asLong()
                || !saved.getString("Dimension").equals(player.serverLevel().dimension().location().toString());
    }

    public static void playerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.getPersistentData().contains(RETURN_GATE)) return;
        net.minecraft.nbt.CompoundTag saved = player.getPersistentData().getCompound(RETURN_GATE);
        ServerLevel level = player.serverLevel();
        if (!saved.getString("Dimension").equals(level.dimension().location().toString())) return;
        BlockPos anchor = BlockPos.of(saved.getLong("Anchor"));
        if (!level.hasChunkAt(anchor)) {
            player.getPersistentData().remove(RETURN_GATE);
            return;
        }
        BlockState state = level.getBlockState(anchor);
        if (!(state.getBlock() instanceof DungeonGateBlock)
                || !DungeonGateBlock.passage(anchor, state.getValue(DungeonGateBlock.FACING)).inflate(.75)
                        .intersects(player.getBoundingBox())) player.getPersistentData().remove(RETURN_GATE);
    }

    public static boolean canConfigure(ServerPlayer player) {
        return player.hasPermissions(2) || player.isCreative();
    }

    public static boolean valid(ServerPlayer player, ServerLevel source, BlockPos position) {
        if (player.isSpectator() || player.serverLevel() != source || !source.hasChunkAt(position)
                || !source.mayInteract(player, position) || player.distanceToSqr(position.getX() + .5,
                        position.getY() + 1, position.getZ() + .5) > 64) return false;
        BlockState state = source.getBlockState(position);
        return state.getBlock() instanceof DungeonGateBlock gate && !gate.returning()
                && state.getValue(DungeonGateBlock.PART) == DungeonGateBlock.ANCHOR_PART
                && DungeonGateBlock.complete(source, position, gate, state.getValue(DungeonGateBlock.FACING));
    }

    private static void requireConfiguration(ServerPlayer player, DungeonGateBlockEntity entity) {
        if (!canConfigure(player)) throw new IllegalStateException("配置/生成需要管理员或创造模式；普通玩家可进入已完成地牢");
        if (!(entity.getLevel() instanceof ServerLevel source) || !valid(player, source, entity.getBlockPos()))
            throw new IllegalStateException("请靠近完整的地牢入口门操作");
        if (source.dimension().equals(DungeonGateTravel.DUNGEON)
                || DungeonGenerators.all().stream().anyMatch(provider -> provider.ownsDimension(source)))
            throw new IllegalStateException("请在地牢之外配置入口门");
        DungeonGateData round = DungeonGateData.get(source.getServer());
        if (round.roundActive() && round.matches(source.dimension(), entity.getBlockPos()))
            throw new IllegalStateException("经典地牢本轮尚未结束，不能切换入口类型");
    }

    public static void choose(ServerPlayer player, DungeonGateBlockEntity entity,
            net.minecraft.resources.ResourceLocation id) {
        requireConfiguration(player, entity);
        if (DungeonGenerators.find(id).isEmpty()) throw new IllegalStateException("生成器未注册");
        long seed = entity.hasGeneratorSeed() ? entity.generatorSeed() : player.serverLevel().random.nextLong();
        entity.configureGenerator(id, seed);
        DungeonGateInteraction.refreshControl(player.serverLevel(), entity.getBlockPos());
    }

    public static void reroll(ServerPlayer player, DungeonGateBlockEntity entity) {
        requireConfiguration(player, entity);
        entity.configureGenerator(entity.generatorId(), player.serverLevel().random.nextLong());
    }

    public static DungeonGenerator.Status status(ServerLevel source, DungeonGateBlockEntity entity) {
        Optional<DungeonGenerator> provider = DungeonGenerators.find(entity.generatorId());
        if (provider.isEmpty()) return new DungeonGenerator.Status(DungeonGenerator.Phase.UNAVAILABLE,
                Component.literal("生成器未安装：" + entity.generatorId()));
        try {
            return provider.get().status(source.getServer(), entity.generatorInstance());
        } catch (RuntimeException failure) {
            return new DungeonGenerator.Status(DungeonGenerator.Phase.UNAVAILABLE,
                    Component.literal("读取生成器状态失败：" + failure.getMessage()));
        }
    }

    public static void prepare(ServerPlayer player, DungeonGateBlockEntity entity) {
        requireConfiguration(player, entity);
        ServerLevel source = player.serverLevel();
        DungeonGenerator provider = DungeonGenerators.find(entity.generatorId())
                .orElseThrow(() -> new IllegalStateException("生成器未安装，保留绑定"));
        int tier = DungeonGateInteraction.controllerSource(source, entity.getBlockPos())
                .map(ArenaDifficultyApi.ArenaSource::tier).orElse(0);
        if (tier <= 0) throw new IllegalStateException("先点亮门旁六格内的血棘祭炉");
        if (!entity.hasGeneratorSeed()) entity.configureGenerator(entity.generatorId(), source.random.nextLong());
        if (entity.generatorInstance().isEmpty()) {
            String instance = provider.reserve(new DungeonGenerator.Request(source, entity.getBlockPos(), entity.generatorSeed(), tier));
            if (instance == null || instance.isBlank() || instance.length() > 256)
                throw new IllegalStateException("生成器返回非法实例ID");
            entity.bindGenerator(instance, tier);
            source.getChunkSource().save(false);
        }
        provider.start(source.getServer(), entity.generatorInstance());
        DungeonGateInteraction.refreshControl(source, entity.getBlockPos());
        player.sendSystemMessage(Component.literal("地牢已绑定：" + entity.generatorInstance() + "；菜单会显示施工进度，完成后通过门进入。"));
    }

    public static void open(ServerPlayer player, BlockPos position) {
        ServerLevel source = player.serverLevel();
        if (!valid(player, source, position)) {
            player.displayClientMessage(Component.literal("请靠近完整的地牢入口门"), true);
            return;
        }
        player.openMenu(new SimpleMenuProvider((containerId, inventory, ignored) ->
                new DungeonGeneratorMenu(containerId, inventory, source, position), Component.literal("FT 地牢 · 生成类型")));
    }

    public static boolean travel(ServerPlayer player, BlockPos anchor, DungeonGateBlock gate, boolean returning) {
        ServerLevel source = player.serverLevel();
        if (returning) {
            Optional<DungeonGenerator> provider = DungeonGenerators.all().stream()
                    .filter(candidate -> candidate.ownsDimension(source)).findFirst();
            if (provider.isEmpty()) return false;
            try {
                BlockState state = source.getBlockState(anchor);
                if (!state.getValue(DungeonGateBlock.OPEN) || state.getValue(DungeonGateBlock.MOVING))
                    throw new IllegalStateException("请等待门完全开启");
                provider.get().leave(player);
            } catch (RuntimeException failure) {
                player.displayClientMessage(Component.literal(failure.getMessage()), true);
            }
            return true;
        }
        if (!(source.getBlockEntity(anchor) instanceof DungeonGateBlockEntity entity)) return false;
        try {
            if (!valid(player, source, anchor)) throw new IllegalStateException("请靠近完整的地牢入口门");
            if (source.dimension().equals(DungeonGateTravel.DUNGEON)
                    || DungeonGenerators.all().stream().anyMatch(provider -> provider.ownsDimension(source)))
                throw new IllegalStateException("不能在地牢内部使用另一座入口门");
            DungeonGenerator.Status current = status(source, entity);
            if (!current.ready()) throw new IllegalStateException(current.detail().getString());
            int tier = DungeonGateInteraction.controllerSource(source, anchor)
                    .map(ArenaDifficultyApi.ArenaSource::tier).orElse(0);
            BlockState state = source.getBlockState(anchor);
            if (tier <= 0 || !state.getValue(DungeonGateBlock.OPEN) || state.getValue(DungeonGateBlock.MOVING))
                throw new IllegalStateException("请点亮祭炉并等待地牢门完全开启");
            DungeonGenerators.find(entity.generatorId()).orElseThrow().enter(player, entity.generatorInstance());
            net.minecraft.nbt.CompoundTag saved = new net.minecraft.nbt.CompoundTag();
            saved.putString("Dimension", source.dimension().location().toString());
            saved.putLong("Anchor", anchor.asLong());
            player.getPersistentData().put(RETURN_GATE, saved);
        } catch (RuntimeException failure) {
            player.displayClientMessage(Component.literal(failure.getMessage() == null ? "生成器拒绝传送" : failure.getMessage()), true);
        }
        return true;
    }
}
