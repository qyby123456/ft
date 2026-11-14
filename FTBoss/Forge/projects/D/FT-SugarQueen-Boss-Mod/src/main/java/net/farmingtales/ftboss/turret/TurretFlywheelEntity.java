package net.farmingtales.ftboss.turret;

import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** 炮台一次性飞轮：三路出膛、缓转追踪，碰墙或命中立即消失，不回旋不重复伤害。 */
public final class TurretFlywheelEntity extends Entity {
    private UUID owner;
    private UUID target;
    private int age;

    public TurretFlywheelEntity(EntityType<? extends TurretFlywheelEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public void launch(EasterBossEntity boss, ServerPlayer player, Vec3 origin, int index) {
        owner = boss.getUUID();
        target = player.getUUID();
        setPos(origin.x, origin.y, origin.z);
        Vec3 direction = player.getBoundingBox().getCenter().subtract(origin).normalize()
                .yRot((index - 1) * 0.45F).add(0, index == 1 ? 0.2 : 0, 0).normalize();
        setDeltaMovement(direction.scale(0.8));
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            setPos(position().add(getDeltaMovement()));
            return;
        }
        if (++age > 60 || owner == null || !(level.getEntity(owner) instanceof EasterBossEntity boss)
                || !boss.isAlive() || boss.isRemoved() || boss.getVariant() != 6 || target == null
                || !(level.getEntity(target) instanceof ServerPlayer player)
                || !ManorBalanceHooks.players(boss, level).contains(player)) {
            discard();
            return;
        }
        Vec3 from = position();
        Vec3 goal = player.getBoundingBox().getCenter();
        Vec3 velocity = getDeltaMovement();
        if (age > 4) velocity = velocity.scale(0.78).add(goal.subtract(from).normalize().scale(0.22)).normalize().scale(0.8);
        Vec3 to = from.add(velocity);
        HitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (block.getType() != HitResult.Type.MISS) to = block.getLocation();
        if (player.getBoundingBox().inflate(0.2).contains(from)
                || player.getBoundingBox().inflate(0.2).clip(from, to).isPresent()) {
            float base = FlywheelProjectileEntity.DAMAGE * (float) boss.flywheelDamageMul() / 3.0F;
            if (player.getHealth() > FlywheelProjectileEntity.HEALTH_BONUS_THRESHOLD) base *= FlywheelProjectileEntity.HEALTH_BONUS_MULT;
            player.invulnerableTime = 0;
            player.hurt(level.damageSources().thrown(this, boss), base * 0.5F);
            player.invulnerableTime = 0;
            player.hurt(level.damageSources().indirectMagic(this, boss), base * 0.5F);
            player.invulnerableTime = 0;
            player.hurt(level.damageSources().indirectMagic(this, boss), FlywheelProjectileEntity.EXTRA_MAGIC / 3.0F);
            player.invulnerableTime = 0;
            ManorAdaptiveDamage.hurtSkill(player, boss, level.damageSources().fellOutOfWorld(), FlywheelProjectileEntity.EXTRA_VOID / 3.0F);
            player.invulnerableTime = 0;
            ManorAdaptiveDamage.hurtSkill(player, boss, level.damageSources().onFire(), FlywheelProjectileEntity.EXTRA_FIRE / 3.0F);
            discard();
            return;
        }
        if (block.getType() != HitResult.Type.MISS) {
            discard();
            return;
        }
        setDeltaMovement(velocity);
        setPos(to);
        setYRot((float) Math.toDegrees(Math.atan2(-velocity.x, velocity.z)));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag data) {}
    @Override
    protected void readAdditionalSaveData(CompoundTag data) {}
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
