package net.farmingtales.ftdungeonmobs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class DungeonGateBlockEntity extends BlockEntity {
    private final DungeonGateMotion motion = new DungeonGateMotion(0, false, 0);
    private final Map<UUID, Long> travelAttempts = new HashMap<>();

    public DungeonGateBlockEntity(BlockPos position, BlockState state) {
        super(FTDungeonMobs.DUNGEON_GATE_ENTITY.get(), position, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            int progress = motion.storedProgress();
            if (!getBlockState().getValue(DungeonGateBlock.MOVING)) {
                progress = getBlockState().getValue(DungeonGateBlock.OPEN) ? DungeonGateMotion.DURATION : 0;
                motion.reset(progress, progress > 0, level.getGameTime());
            } else {
                motion.reset(progress, motion.targetOpen(), level.getGameTime());
            }
        }
    }

    public float progress(float partialTick) {
        return level == null ? 0 : motion.progress(level.getGameTime() + (double) partialTick) / DungeonGateMotion.DURATION;
    }

    public boolean targetOpen() {
        return motion.targetOpen();
    }

    public void applyControl(boolean opening, int tier) {
        if (!(level instanceof ServerLevel) || !(getBlockState().getBlock() instanceof DungeonGateBlock)) {
            return;
        }
        BlockState state = getBlockState();
        if (state.getValue(DungeonGateBlock.PORTAL_TIER) != tier) {
            updateParts(state.getValue(DungeonGateBlock.OPEN), state.getValue(DungeonGateBlock.MOVING), tier);
            synchronize();
        }
        boolean target = state.getValue(DungeonGateBlock.MOVING) ? motion.targetOpen()
                : state.getValue(DungeonGateBlock.OPEN);
        if (target == opening || !opening && obstructed((ServerLevel) level)) {
            return;
        }
        long now = level.getGameTime();
        if (!state.getValue(DungeonGateBlock.MOVING)) {
            motion.reset(state.getValue(DungeonGateBlock.OPEN) ? DungeonGateMotion.DURATION : 0, opening, now);
        } else {
            motion.retarget(opening, now);
        }
        updateParts(state.getValue(DungeonGateBlock.OPEN), true, tier);
        synchronize();
        level.playSound(null, worldPosition, opening ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                SoundSource.BLOCKS, 1.0F, opening ? 0.6F : 0.45F);
    }

    public static void serverTick(ServerLevel level, BlockPos position, BlockState state, DungeonGateBlockEntity gateEntity) {
        if (level.getGameTime() % 20 == 0) {
            DungeonGateInteraction.refreshControl(level, position);
            state = gateEntity.getBlockState();
        }
        if (!state.getValue(DungeonGateBlock.MOVING)) {
            return;
        }
        long now = level.getGameTime();
        if (!gateEntity.motion.targetOpen() && gateEntity.obstructed(level)) {
            gateEntity.motion.retarget(true, now);
            gateEntity.synchronize();
        }
        if (!gateEntity.motion.finished(now)) {
            if (now % 6 == 0) {
                level.playSound(null, position, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.55F, 0.65F);
            }
            return;
        }
        boolean opened = gateEntity.motion.targetOpen();
        gateEntity.motion.reset(opened ? DungeonGateMotion.DURATION : 0, opened, now);
        gateEntity.updateParts(opened, false);
        gateEntity.synchronize();
        level.playSound(null, position, opened ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                SoundSource.BLOCKS, 0.9F, 0.55F);
    }

    public void tryTravel(ServerPlayer player) {
        if (!(level instanceof ServerLevel) || !getBlockState().getValue(DungeonGateBlock.OPEN)
                || getBlockState().getValue(DungeonGateBlock.MOVING)
                || !DungeonGateBlock.passage(worldPosition, facing()).intersects(player.getBoundingBox())) {
            return;
        }
        if (((DungeonGateBlock) getBlockState().getBlock()).boss()) {
            return;
        }
        long now = level.getGameTime();
        Long previous = travelAttempts.get(player.getUUID());
        if (previous != null && now >= previous && now - previous < 40) {
            return;
        }
        travelAttempts.entrySet().removeIf(entry -> now < entry.getValue() || now - entry.getValue() >= 40);
        travelAttempts.put(player.getUUID(), now);
        DungeonGateBlock gate = (DungeonGateBlock) getBlockState().getBlock();
        DungeonGateTravel.use(player, worldPosition, gate, gate.returning());
    }

    private boolean obstructed(ServerLevel level) {
        return !level.getEntitiesOfClass(LivingEntity.class, DungeonGateBlock.passage(worldPosition, facing()).inflate(0.1),
                entity -> entity.isAlive() && !entity.isSpectator()).isEmpty();
    }

    private Direction facing() {
        return getBlockState().getValue(DungeonGateBlock.FACING);
    }

    private void updateParts(boolean opened, boolean moving) {
        updateParts(opened, moving, getBlockState().getValue(DungeonGateBlock.PORTAL_TIER));
    }

    private void updateParts(boolean opened, boolean moving, int tier) {
        if (level == null) {
            return;
        }
        BlockState root = getBlockState();
        for (int part = 0; part < 12; part++) {
            BlockPos target = DungeonGateBlock.partPosition(worldPosition, root.getValue(DungeonGateBlock.FACING), part);
            BlockState other = level.getBlockState(target);
            if (other.is(root.getBlock()) && other.getValue(DungeonGateBlock.PART) == part
                    && other.getValue(DungeonGateBlock.FACING) == root.getValue(DungeonGateBlock.FACING)) {
                level.setBlock(target, other.setValue(DungeonGateBlock.OPEN, opened).setValue(DungeonGateBlock.MOVING, moving)
                                .setValue(DungeonGateBlock.PORTAL_TIER, tier),
                        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    private void synchronize() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        long time = level == null ? 0 : level.getGameTime();
        tag.putInt("GateProgress", motion.progress(time));
        tag.putBoolean("GateTargetOpen", motion.targetOpen());
        tag.putLong("GateAnimationTime", time);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        motion.reset(tag.getInt("GateProgress"), tag.getBoolean("GateTargetOpen"), tag.getLong("GateAnimationTime"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 4, 2));
    }
}
