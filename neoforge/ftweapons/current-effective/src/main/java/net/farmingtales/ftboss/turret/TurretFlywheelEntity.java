package net.farmingtales.ftboss.turret;

import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.server.level.ServerEntity;

public final class TurretFlywheelEntity extends Entity {
    private UUID owner;
    private UUID target;
    private int age;

    public TurretFlywheelEntity(EntityType<? extends TurretFlywheelEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void launch(EasterBossEntity boss, ServerPlayer player, Vec3 origin, int index) {
        this.owner = boss.getUUID();
        this.target = player.getUUID();
        this.setPos(origin.x, origin.y, origin.z);
        Vec3 direction = player.getBoundingBox()
            .getCenter()
            .subtract(origin)
            .normalize()
            .yRot((float)(index - 1) * 0.45F)
            .add(0.0, index == 1 ? 0.2 : 0.0, 0.0)
            .normalize();
        this.setDeltaMovement(direction.scale(0.8));
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            if (++this.age <= 60
                && this.owner != null
                && level.getEntity(this.owner) instanceof EasterBossEntity boss
                && boss.isAlive()
                && !boss.isRemoved()
                && boss.getVariant() == 6
                && this.target != null
                && level.getEntity(this.target) instanceof ServerPlayer player
                && ManorBalanceHooks.players(boss, level).contains(player)) {
                Vec3 from = this.position();
                Vec3 goal = player.getBoundingBox().getCenter();
                Vec3 velocity = this.getDeltaMovement();
                if (this.age > 4) {
                    velocity = velocity.scale(0.78).add(goal.subtract(from).normalize().scale(0.22)).normalize().scale(0.8);
                }

                Vec3 to = from.add(velocity);
                HitResult block = level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this));
                if (block.getType() != Type.MISS) {
                    to = block.getLocation();
                }

                if (player.getBoundingBox().inflate(0.2).contains(from) || player.getBoundingBox().inflate(0.2).clip(from, to).isPresent()) {
                    float base = 10.67F * (float)boss.flywheelDamageMul() / 3.0F;
                    if (player.getHealth() > 100.0F) {
                        base *= 1.25F;
                    }

                    player.invulnerableTime = 0;
                    player.hurt(level.damageSources().thrown(this, boss), base * 0.5F);
                    player.invulnerableTime = 0;
                    player.hurt(level.damageSources().indirectMagic(this, boss), base * 0.5F);
                    player.invulnerableTime = 0;
                    player.hurt(level.damageSources().indirectMagic(this, boss), 0.44333336F);
                    player.invulnerableTime = 0;
                    ManorAdaptiveDamage.hurtSkill(player, boss, level.damageSources().fellOutOfWorld(), 0.33333334F);
                    player.invulnerableTime = 0;
                    ManorAdaptiveDamage.hurtSkill(player, boss, level.damageSources().onFire(), 0.33333334F);
                    this.discard();
                } else if (block.getType() != Type.MISS) {
                    this.discard();
                } else {
                    this.setDeltaMovement(velocity);
                    this.setPos(to);
                    this.setYRot((float)Math.toDegrees(Math.atan2(-velocity.x, velocity.z)));
                }
            } else {
                this.discard();
            }
        } else {
            this.setPos(this.position().add(this.getDeltaMovement()));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag data) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag data) {
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return super.getAddEntityPacket(entity);
    }
}
