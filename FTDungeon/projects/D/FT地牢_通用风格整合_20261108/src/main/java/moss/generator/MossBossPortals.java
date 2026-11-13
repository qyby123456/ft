package moss.generator;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftdungeonmobs.DungeonGateBlock;
import net.farmingtales.ftdungeonmobs.DungeonGateBlockEntity;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;

public final class MossBossPortals {
    public static final String DATA_KEY = "ftmossgen.portal";
    public static final ResourceLocation ID = new ResourceLocation("ftmossgen", "forest");
    private static final String COOLDOWN = "ftmossgen.portalCooldown";

    public record Gate(BlockPos anchor, Direction facing, boolean returning, int piece) {
        public DungeonGateBlock block() {
            return returning ? FTDungeonMobs.DUNGEON_RETURN_GATE.get() : FTDungeonMobs.DUNGEON_ENTRY_GATE.get();
        }

        public BlockState state(int part) {
            return block().defaultBlockState().setValue(DungeonGateBlock.FACING, facing)
                    .setValue(DungeonGateBlock.PART, part);
        }
    }

    private MossBossPortals() {}

    public static List<Gate> gates(Planner.Plan plan) {
        if (plan.bossPortal == null || plan.bossPortal.length != 3
                || plan.bossPortalFacing < 0 || plan.bossPortalFacing > 3) {
            throw new IllegalStateException("缺少古林深层门规划");
        }
        Planner.Piece boss = plan.pieces.get(plan.pieces.size() - 1);
        Direction backFacing = Direction.NORTH;
        for (int turn = 0; turn < boss.rotation; turn++) backFacing = backFacing.getClockWise();
        return List.of(new Gate(Catalog.pos(plan.bossPortal), Direction.from2DDataValue(plan.bossPortalFacing),
                        false, plan.bossPortalPiece),
                new Gate(Planner.transform(new int[] {16, 2, 25}, boss.size, boss.rotation, boss.origin),
                        backFacing, true, boss.index));
    }

    public static List<BlockPos> clearance(Gate gate) {
        List<BlockPos> cells = new ArrayList<>();
        for (int column = -1; column <= 1; column++) {
            for (int depth : new int[] {-1, 1}) {
                for (int height = 0; height < 4; height++) {
                    cells.add(gate.anchor.relative(gate.facing.getClockWise(), column)
                            .relative(gate.facing, depth).above(height));
                }
            }
        }
        return cells;
    }

    public static void initialize(ServerLevel level, Engine.Job job) {
        for (Gate gate : gates(job.plan)) {
            checkGeometry(level, job.plan, gate);
            if (!(level.getBlockEntity(gate.anchor) instanceof DungeonGateBlockEntity entity)) {
                throw new IllegalStateException("古林门缺少原生方块实体");
            }
            CompoundTag expected = metadata(job, gate);
            CompoundTag data = entity.getPersistentData();
            upgradeLegacyMetadata(entity, job, gate);
            if (data.contains(DATA_KEY) && !data.getCompound(DATA_KEY).equals(expected)) {
                throw new IllegalStateException("古林门绑定冲突");
            }
            if (!entity.generatorInstance().isEmpty()
                    && (!new ResourceLocation(Profiles.get(job.plan.style).id).equals(entity.generatorId()) || !job.plan.id.equals(entity.generatorInstance()))) {
                throw new IllegalStateException("拒绝覆盖其他实例的地牢门");
            }
            if (entity.generatorInstance().isEmpty()) {
                entity.configureGenerator(new ResourceLocation(Profiles.get(job.plan.style).id), job.plan.seed);
                entity.bindGenerator(job.plan.id, 1);
            }
            data.put(DATA_KEY, expected);
            entity.setChanged();
        }
        Store.flush(level);
        refresh(level, job);
    }

    public static void refresh(ServerLevel level, Engine.Job job) {
        for (Gate gate : gates(job.plan)) refreshControl(level, gate.anchor);
    }

    public static boolean refreshControl(ServerLevel level, BlockPos anchor) {
        Engine.Job job = owner(level, anchor);
        if (job == null) return marked(level, anchor);
        Gate gate = gateAt(job.plan, anchor);
        if (!level.hasChunksAt(anchor.offset(-3, -1, -3), anchor.offset(3, 4, 3))) return true;
        if (!"COMPLETE".equals(job.stage)) return true;
        if (!(level.getBlockEntity(anchor) instanceof DungeonGateBlockEntity entity)) return true;
        boolean permitted = "COMPLETE".equals(job.stage) && job.gameplay != null
                && (!gate.returning || "VICTORY".equals(job.gameplay.bossState));
        try {
            checkGeometry(level, job.plan, gate);
            if (!validMetadata(entity, job, gate)) permitted = false;
        } catch (RuntimeException exception) {
            permitted = false;
        }
        entity.applyControl(permitted, 1);
        return true;
    }

    public static boolean handleUse(ServerPlayer player, BlockPos anchor) {
        ServerLevel level = player.serverLevel();
        Engine.Job job = owner(level, anchor);
        if (job == null) return marked(level, anchor);
        refreshControl(level, anchor);
        if (gateAt(job.plan, anchor).returning && (job.gameplay == null
                || !"VICTORY".equals(job.gameplay.bossState))) {
            player.displayClientMessage(Component.literal("本实例首领尚未被旅者击败，归途封印仍在。"), true);
        } else {
            player.displayClientMessage(Component.literal("等待石门完全开启后，穿过门框前往本实例目的地。"), true);
        }
        return true;
    }

    public static boolean handleTravel(ServerPlayer player, BlockPos anchor) {
        ServerLevel level = player.serverLevel();
        Engine.Job job = owner(level, anchor);
        if (job == null) return marked(level, anchor);
        if (player instanceof FakePlayer || player.isSpectator() || !player.isAlive()
                || !"COMPLETE".equals(job.stage) || job.gameplay == null) return true;
        Gate gate = gateAt(job.plan, anchor);
        if (gate.returning && !"VICTORY".equals(job.gameplay.bossState)) return true;
        if (!(level.getBlockEntity(anchor) instanceof DungeonGateBlockEntity entity)
                || !validMetadata(entity, job, gate)) return true;
        BlockState state = level.getBlockState(anchor);
        if (!state.getValue(DungeonGateBlock.OPEN) || state.getValue(DungeonGateBlock.MOVING)
                || !DungeonGateBlock.passage(anchor, gate.facing).intersects(player.getBoundingBox())) return true;
        CompoundTag cooldown = player.getPersistentData().getCompound(COOLDOWN);
        long now = level.getGameTime();
        if (now < cooldown.getLong("until")) return true;
        try {
            checkGeometry(level, job.plan, gate);
            BlockPos destination = gate.returning ? Catalog.pos(job.plan.entry) : Catalog.pos(job.gameplay.bossEntry);
            if (!Gameplay.safeFeet(level, destination)) {
                throw new IllegalStateException("目的地落点被阻挡");
            }
            AABB landing = new AABB(destination.getX() + 0.15, destination.getY(), destination.getZ() + 0.15,
                    destination.getX() + 0.85, destination.getY() + 1.95, destination.getZ() + 0.85);
            if (!level.getEntities(player, landing, other -> other.isAlive() && !other.isSpectator()).isEmpty()) {
                throw new IllegalStateException("目的地有生物占位，请稍候");
            }
            if (!gate.returning && "READY".equals(job.gameplay.bossState) && !Gameplay.startBoss(level, job)) {
                throw new IllegalStateException("守望者战场暂不可用");
            }
            cooldown.putString("instance", job.plan.id);
            cooldown.putString("role", gate.returning ? "return" : "boss");
            cooldown.putLong("until", now + 60);
            player.getPersistentData().put(COOLDOWN, cooldown);
            player.teleportTo(level, destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, 180, 0);
            player.setDeltaMovement(0, 0, 0);
            player.fallDistance = 0;
        } catch (RuntimeException exception) {
            player.displayClientMessage(Component.literal("地牢门：" + exception.getMessage()), true);
        }
        return true;
    }

    public static void checkGeometry(ServerLevel level, Planner.Plan plan, Gate gate) {
        Planner.Piece piece = plan.pieces.stream().filter(value -> value.index == gate.piece).findFirst()
                .orElseThrow(() -> new IllegalStateException("门不属于已规划房间"));
        for (int part = 0; part < 12; part++) {
            BlockPos position = DungeonGateBlock.partPosition(gate.anchor, gate.facing, part);
            level.getChunk(position);
            if (!inside(piece, position) || !level.getWorldBorder().isWithinBounds(position)
                    || !level.getBlockState(position).is(gate.block())
                    || level.getBlockState(position).getValue(DungeonGateBlock.PART) != part
                    || level.getBlockState(position).getValue(DungeonGateBlock.FACING) != gate.facing) {
                throw new IllegalStateException("真实3x4门框不完整：" + position);
            }
        }
        for (int column = -1; column <= 1; column++) {
            for (int depth = -1; depth <= 1; depth++) {
                BlockPos floor = gate.anchor.relative(gate.facing.getClockWise(), column)
                        .relative(gate.facing, depth).below();
                if (!inside(piece, floor) || !level.getFluidState(floor).isEmpty()
                        || !level.getBlockState(floor).isCollisionShapeFullBlock(level, floor)) {
                    throw new IllegalStateException("门框或通路缺少完整地板：" + floor);
                }
            }
        }
        for (BlockPos position : clearance(gate)) {
            if (!inside(piece, position) || !level.getBlockState(position).isAir()
                    || !level.getFluidState(position).isEmpty() || level.getBlockEntity(position) != null) {
                throw new IllegalStateException("门前后净空被占用：" + position);
            }
        }
    }

    private static boolean inside(Planner.Piece piece, BlockPos position) {
        return Planner.bounds(piece).contains(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
    }

    private static CompoundTag metadata(Engine.Job job, Gate gate) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", 1);
        tag.putString("instance", job.plan.id);
        tag.putString("role", gate.returning ? "return" : "boss");
        tag.putString("dimension", MossMod.DIMENSION.location().toString());
        tag.putString("style", Profiles.get(job.plan.style).id);
        tag.putIntArray("anchor", Catalog.vec(gate.anchor));
        tag.putInt("facing", gate.facing.get2DDataValue());
        tag.putIntArray("target", gate.returning ? job.plan.entry : job.gameplay.bossEntry);
        return tag;
    }

    private static boolean validMetadata(DungeonGateBlockEntity entity, Engine.Job job, Gate gate) {
        upgradeLegacyMetadata(entity, job, gate);
        return new ResourceLocation(Profiles.get(job.plan.style).id).equals(entity.generatorId()) && job.plan.id.equals(entity.generatorInstance())
                && entity.getPersistentData().getCompound(DATA_KEY).equals(metadata(job, gate));
    }

    private static void upgradeLegacyMetadata(DungeonGateBlockEntity entity, Engine.Job job, Gate gate) {
        if (!job.legacy025 || !Profiles.DEFAULT.equals(job.plan.style)
                || !ID.equals(entity.generatorId()) || !job.plan.id.equals(entity.generatorInstance())) return;
        CompoundTag expected = metadata(job, gate);
        CompoundTag previous = expected.copy();
        previous.remove("style");
        CompoundTag data = entity.getPersistentData();
        if (data.contains(DATA_KEY) && data.getCompound(DATA_KEY).equals(previous)) {
            data.put(DATA_KEY, expected);
            entity.setChanged();
        }
    }

    private static Gate gateAt(Planner.Plan plan, BlockPos anchor) {
        return gates(plan).stream().filter(value -> value.anchor.equals(anchor)).findFirst().orElseThrow();
    }

    private static Engine.Job owner(ServerLevel level, BlockPos anchor) {
        if (!level.dimension().equals(MossMod.DIMENSION)) return null;
        for (Engine.Job job : Store.get(level).jobs.values()) {
            if (job.plan.bossPortal != null && gates(job.plan).stream().anyMatch(value -> value.anchor.equals(anchor))) {
                return job;
            }
        }
        return null;
    }

    private static boolean marked(ServerLevel level, BlockPos anchor) {
        return level.getBlockEntity(anchor) instanceof DungeonGateBlockEntity entity
                && entity.getPersistentData().contains(DATA_KEY);
    }
}
