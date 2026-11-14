package net.farmingtales.ftboss.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class QizaiSigilEntity extends Projectile {
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(QizaiSigilEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(QizaiSigilEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(QizaiSigilEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RELEASED = SynchedEntityData.defineId(QizaiSigilEntity.class, EntityDataSerializers.BOOLEAN);
    private int remaining = 1;

    public QizaiSigilEntity(EntityType<? extends QizaiSigilEntity> type, Level level) { super(type, level); }

    public QizaiSigilEntity(ServerLevel level, LivingEntity owner, Vec3 position, int kind, int duration) {
        this(FTEntities.QIZAI_SIGIL.get(), level);
        setOwner(owner);
        setPos(position);
        entityData.set(KIND, kind);
        entityData.set(START, level.getGameTime());
        entityData.set(DURATION, Math.max(1, Math.min(1200, duration)));
        remaining = duration() + 20;
        net.farmingtales.ftboss.boss.ManorFightMembers.inherit(owner, this);
    }

    @Override protected void defineSynchedData() {
        entityData.define(KIND, 0);
        entityData.define(START, 0L);
        entityData.define(DURATION, 1);
        entityData.define(RELEASED, false);
    }

    public int kind() { return entityData.get(KIND); }
    public int duration() { return entityData.get(DURATION); }
    public boolean released() { return entityData.get(RELEASED); }
    public double progress(float partial) {
        return Math.max(0, Math.min(1, (level().getGameTime() - entityData.get(START) + partial) / duration()));
    }

    public void release() {
        entityData.set(RELEASED, true);
        entityData.set(START, level().getGameTime());
        entityData.set(DURATION, 12);
        remaining = 12;
    }

    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive()
                || owner.isRemoved() || owner.isSpectator() || owner.level() != level()
                || owner instanceof Mob mob && mob.isNoAi() || --remaining <= 0)) discard();
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); remaining = 1; }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
