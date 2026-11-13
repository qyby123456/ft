package net.farmingtales.ftdungeonmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector3f;

public final class BloodOfferingAltarEntity extends BlockEntity {
    public static final TagKey<Item> OFFERINGS_1 = offeringTag(1);
    public static final TagKey<Item> OFFERINGS_2 = offeringTag(2);
    public static final TagKey<Item> OFFERINGS_4 = offeringTag(4);
    public static final TagKey<Item> OFFERINGS_8 = offeringTag(8);
    private final BloodPoolRitual ritual = new BloodPoolRitual();

    public BloodOfferingAltarEntity(BlockPos position, BlockState state) {
        super(BloodPoolContent.ALTAR_ENTITY.get(), position, state);
    }

    private static TagKey<Item> offeringTag(int points) {
        return TagKey.create(Registries.ITEM, FTDungeonMobs.resource("blood_offerings/points_" + points));
    }

    public static int points(ItemStack stack) {
        return stack.is(OFFERINGS_8) ? 8 : stack.is(OFFERINGS_4) ? 4 : stack.is(OFFERINGS_2) ? 2 : stack.is(OFFERINGS_1) ? 1 : 0;
    }

    public int charge() {
        return ritual.charge();
    }

    public void interact(ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long now = level.getGameTime();
        ItemStack held = player.getMainHandItem();
        if (ritual.phase(now) >= 2) {
            message(player, "busy");
            return;
        }
        BloodPoolStructure.Result structure = BloodPoolStructure.validate(level, worldPosition, facing());
        if (!structure.valid()) {
            message(player, "missing_" + structure.reason());
            return;
        }
        if (!held.isEmpty()) {
            if (ritual.offer(points(held), now)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                changed();
                level.playSound(null, worldPosition, SoundEvents.SOUL_SAND_HIT, SoundSource.BLOCKS, 0.8F, 0.7F);
                player.displayClientMessage(Component.translatable("message.ftdungeonmobs.blood_pool.charge", ritual.charge(), BloodPoolRitual.REQUIRED), true);
            } else {
                message(player, ritual.charge() >= BloodPoolRitual.REQUIRED ? "ready" : "invalid_offering");
            }
            return;
        }
        if (ritual.charge() < BloodPoolRitual.REQUIRED) {
            player.displayClientMessage(Component.translatable("message.ftdungeonmobs.blood_pool.charge", ritual.charge(), BloodPoolRitual.REQUIRED), true);
            return;
        }
        if (MinecraftForge.EVENT_BUS.post(new BloodPoolActivateEvent(player, worldPosition))) {
            message(player, "cancelled");
            return;
        }
        if (ritual.activate(now)) {
            changed();
            level.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.2F, 0.55F);
            BlockPos center = worldPosition.relative(facing().getOpposite(), 4);
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.8F, 0.03F, 0.05F), 1.2F),
                    center.getX() + 0.5, center.getY() + 1.1, center.getZ() + 0.5, 24, 2.5, 0.4, 2.5, 0);
            message(player, "activated");
        }
    }

    public static void serverTick(ServerLevel level, BlockPos position, BlockState state, BloodOfferingAltarEntity altar) {
        long now = level.getGameTime();
        int phase = altar.ritual.phase(now);
        if (state.getValue(BloodOfferingAltarBlock.PHASE) != phase) {
            altar.changed();
        }
        if ((now + position.asLong()) % 20 != 0) {
            return;
        }
        if (phase == 2) {
            if (!BloodPoolStructure.validate(level, position, altar.facing()).valid()) {
                altar.ritual.interrupt(now);
                altar.changed();
            }
            return;
        }
        if (phase >= 2 || altar.ritual.charge() >= BloodPoolRitual.REQUIRED) {
            return;
        }
        AABB collection = new AABB(position).move(0, 0.8, 0).inflate(0.1, 0, 0.1);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, collection, entity -> entity.isAlive() && points(entity.getItem()) > 0)) {
            Entity owner = item.getOwner();
            if (owner instanceof ServerPlayer player && (!DungeonAdventureRules.canOffer(player) || !level.mayInteract(player, position))) {
                continue;
            }
            if (BloodPoolStructure.validate(level, position, altar.facing()).valid() && altar.ritual.offer(points(item.getItem()), now)) {
                ItemStack remaining = item.getItem().copy();
                remaining.shrink(1);
                if (remaining.isEmpty()) {
                    item.discard();
                } else {
                    item.setItem(remaining);
                }
                altar.changed();
                break;
            }
        }
    }

    private Direction facing() {
        return getBlockState().getValue(BloodOfferingAltarBlock.FACING);
    }

    private void changed() {
        setChanged();
        if (level == null || level.isClientSide) {
            return;
        }
        int phase = ritual.phase(level.getGameTime());
        BlockState current = getBlockState();
        if (current.getValue(BloodOfferingAltarBlock.PHASE) != phase) {
            level.setBlock(worldPosition, current.setValue(BloodOfferingAltarBlock.PHASE, phase), Block.UPDATE_ALL);
            BloodPoolStructure.setLit(level, worldPosition, facing(), phase == 1 || phase == 2);
        }
        level.updateNeighbourForOutputSignal(worldPosition, current.getBlock());
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message.ftdungeonmobs.blood_pool." + key), true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Charge", ritual.charge());
        tag.putLong("ActiveUntil", ritual.activeUntil());
        tag.putLong("CooldownUntil", ritual.cooldownUntil());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ritual.restore(tag.getInt("Charge"), tag.getLong("ActiveUntil"), tag.getLong("CooldownUntil"));
    }
}
