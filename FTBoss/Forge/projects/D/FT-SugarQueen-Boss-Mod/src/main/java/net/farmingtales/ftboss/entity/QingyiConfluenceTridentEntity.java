package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.skill.QingyiConfluenceSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class QingyiConfluenceTridentEntity extends ThrownTrident implements GeoAnimatable {
    private static final EntityDataAccessor<Boolean> FOIL = SynchedEntityData.defineId(
            QingyiConfluenceTridentEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.qingyi_confluence.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public QingyiConfluenceTridentEntity(EntityType<? extends QingyiConfluenceTridentEntity> type, Level level) {
        super(type, level);
    }

    public QingyiConfluenceTridentEntity(Level level, LivingEntity owner, ItemStack stack) {
        this(FTEntities.QINGYI_CONFLUENCE_TRIDENT.get(), level);
        CompoundTag data = new CompoundTag();
        super.addAdditionalSaveData(data);
        data.put("Trident", stack.copyWithCount(1).save(new CompoundTag()));
        readAdditionalSaveData(data);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FOIL, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag data) {
        super.readAdditionalSaveData(data);
        entityData.set(FOIL, getPickupItem().hasFoil());
    }

    @Override
    public boolean isFoil() {
        return entityData.get(FOIL);
    }

    @Override
    protected boolean canHitEntity(Entity candidate) {
        if (getOwner() instanceof LivingEntity owner && candidate instanceof LivingEntity victim
                && !QingyiConfluenceSkill.canTarget(owner, victim)) return false;
        return super.canHitEntity(candidate);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        LivingEntity victim = hit.getEntity() instanceof LivingEntity living ? living : null;
        float before = victim == null ? 0 : victim.getHealth() + victim.getAbsorptionAmount();
        super.onHitEntity(hit);
        if (!level().isClientSide && victim != null && getOwner() instanceof LivingEntity owner
                && QingyiConfluenceSkill.canTarget(owner, victim)
                && victim.getHealth() + victim.getAbsorptionAmount() < before) QingyiConfluenceSkill.mark(victim, 40);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "flight", 0, state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object entity) {
        return tickCount;
    }
}
