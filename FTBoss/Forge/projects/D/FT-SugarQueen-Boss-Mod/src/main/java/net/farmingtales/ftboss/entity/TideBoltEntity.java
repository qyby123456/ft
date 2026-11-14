package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class TideBoltEntity extends ThrowableItemProjectile {
    private float damage = 8;

    public TideBoltEntity(EntityType<? extends TideBoltEntity> type, Level level) { super(type, level); }
    public void setDamage(float damage) { this.damage = damage; }

    @Override
    protected Item getDefaultItem() { return Items.PRISMARINE_SHARD; }

    @Override
    protected float getGravity() { return 0; }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || !(entity instanceof LivingEntity living) || !living.isAlive()) return false;
        if (getOwner() instanceof Player player) {
            return !player.isAlliedTo(entity) && (!(entity instanceof Player other) || (BossTargets.valid(other) && player.canHarmPlayer(other)));
        }
        return entity instanceof Player player && BossTargets.valid(player);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel) || !(result.getEntity() instanceof LivingEntity target)) return;
        Entity owner = getOwner();
        if (target.hurt(damageSources().thrown(this, owner), this.damage)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.1);
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            if (tickCount > 60 || getOwner() == null || !getOwner().isAlive()) { discard(); return; }
            if (tickCount % 2 == 0) server.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 2, 0.05, 0.05, 0.05, 0);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("TideDamage", this.damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.contains("TideDamage") ? tag.getFloat("TideDamage") : 8;
    }
}
