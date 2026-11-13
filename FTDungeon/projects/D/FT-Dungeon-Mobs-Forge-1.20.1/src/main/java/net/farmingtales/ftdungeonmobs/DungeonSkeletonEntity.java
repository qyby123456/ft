package net.farmingtales.ftdungeonmobs;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class DungeonSkeletonEntity extends Monster implements GeoEntity, RangedAttackMob {
    private static final EntityDataAccessor<String> ACTIVE_ANIMATION = SynchedEntityData.defineId(DungeonSkeletonEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ANIMATION_SEQUENCE = SynchedEntityData.defineId(DungeonSkeletonEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final SkeletonVariant variant;
    private int animationTicksRemaining;
    private int animationElapsedTicks;
    private int attackCooldown;
    private int lastAnimationSequence = -1;
    private boolean combatAnimation;
    private LivingEntity attackTarget;
    private int difficultyTier;
    private boolean automaticDifficulty = true;
    private DifficultyProfile difficultyOverrides = DifficultyProfile.IDENTITY;
    private long difficultyRevision = -1;
    private Difficulty appliedWorldDifficulty;

    public DungeonSkeletonEntity(EntityType<? extends DungeonSkeletonEntity> entityType, Level level, SkeletonVariant variant) {
        super(entityType, level);
        this.variant = variant;
        this.xpReward = variant == SkeletonVariant.KING ? 30 : 5;
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(variant.defaultWeapon()));
        setDropChance(EquipmentSlot.MAINHAND, 0);
    }

    public static AttributeSupplier.Builder createAttributes(SkeletonVariant variant) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, variant.health())
                .add(Attributes.ATTACK_DAMAGE, variant.baseAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, variant.speed())
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ARMOR, variant == SkeletonVariant.KING ? 6 : 2)
                .add(AttributeRegistry.SPELL_RESIST.get(), 1)
                .add(AttributeRegistry.SPELL_POWER.get(), 1);
    }

    public SkeletonVariant variant() { return variant; }

    int storedDifficultyTier() { return difficultyTier; }
    boolean automaticDifficulty() { return automaticDifficulty; }
    DifficultyProfile difficultyOverrides() { return difficultyOverrides; }
    long difficultyRevision() { return difficultyRevision; }
    Difficulty appliedWorldDifficulty() { return appliedWorldDifficulty; }

    void storeDifficultyState(int tier, boolean automatic, DifficultyProfile overrides, long revision, Difficulty worldDifficulty) {
        difficultyTier = tier;
        automaticDifficulty = automatic;
        difficultyOverrides = overrides;
        difficultyRevision = revision;
        appliedWorldDifficulty = worldDifficulty;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        DungeonDifficultyApi.writeState(this, tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        DungeonDifficultyApi.readState(this, tag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACTIVE_ANIMATION, "");
        entityData.define(ANIMATION_SEQUENCE, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new DungeonAttackGoal());
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public MobType getMobType() { return MobType.UNDEAD; }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.SKELETON_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.SKELETON_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.SKELETON_DEATH; }

    public String activeAnimation() { return entityData.get(ACTIVE_ANIMATION); }

    public void previewAnimation(String name) {
        if (!variant.hasAnimation(name)) {
            throw new IllegalArgumentException("Unknown animation: " + name);
        }
        startAnimation(name, false);
    }

    private void startAnimation(String name, boolean combat) {
        combatAnimation = combat;
        attackTarget = combat ? getTarget() : null;
        animationElapsedTicks = 0;
        animationTicksRemaining = variant.animationTicks(name);
        entityData.set(ACTIVE_ANIMATION, name);
        entityData.set(ANIMATION_SEQUENCE, entityData.get(ANIMATION_SEQUENCE) + 1);
        getNavigation().stop();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !isAlive()) { return; }
        if (difficultyRevision < 0 || tickCount % 20 == 0) {
            DungeonDifficultyApi.refreshIfNeeded(this);
        }
        if (attackCooldown > 0) { attackCooldown--; }
        if (animationTicksRemaining <= 0) { return; }
        getNavigation().stop();
        animationElapsedTicks++;
        if (combatAnimation && animationElapsedTicks == variant.impactTick()) {
            performAttack(attackTarget);
        }
        if (--animationTicksRemaining <= 0) {
            entityData.set(ACTIVE_ANIMATION, "");
            combatAnimation = false;
            attackTarget = null;
        }
    }

    private void performAttack(LivingEntity target) {
        if (target == null || !target.isAlive() || !(level() instanceof ServerLevel serverLevel)
                || !hasLineOfSight(target) || distanceToSqr(target) > variant.attackRange() * variant.attackRange()) {
            return;
        }
        if (variant == SkeletonVariant.ARCHER || variant == SkeletonVariant.MAGE) {
            performRangedAttack(target, 1);
        } else {
            doHurtTarget(target);
        }
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon) {
        return variant == SkeletonVariant.ARCHER && weapon instanceof BowItem;
    }

    @Override
    public ItemStack getProjectile(ItemStack weaponStack) {
        if (weaponStack.getItem() instanceof ProjectileWeaponItem weapon) {
            ItemStack heldProjectile = ProjectileWeaponItem.getHeldProjectile(this, weapon.getSupportedHeldProjectiles());
            return heldProjectile.isEmpty() ? new ItemStack(Items.ARROW) : heldProjectile;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel serverLevel) || !target.isAlive() || isAlliedTo(target)) { return; }
        if (variant == SkeletonVariant.ARCHER && getMainHandItem().getItem() instanceof BowItem bow) {
            AbstractArrow arrow = ProjectileUtil.getMobArrow(this, getProjectile(getMainHandItem()), power);
            arrow = bow.customArrow(arrow);
            arrow.setBaseDamage(arrow.getBaseDamage() * Math.max(0, getAttributeValue(Attributes.ATTACK_DAMAGE) / variant.damage()));
            double deltaX = target.getX() - getX();
            double deltaZ = target.getZ() - getZ();
            double deltaY = target.getY(0.333) - arrow.getY();
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            arrow.shoot(deltaX, deltaY + horizontalDistance * 0.2, deltaZ, 1.6F, 4);
            serverLevel.addFreshEntity(arrow);
            playSound(SoundEvents.SKELETON_SHOOT, 1, 1);
        } else if (variant == SkeletonVariant.MAGE) {
            IronSpellCombat.castMagicMissile(serverLevel, this, target);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, this::animationState));
    }

    private PlayState animationState(AnimationState<DungeonSkeletonEntity> state) {
        int sequence = entityData.get(ANIMATION_SEQUENCE);
        if (lastAnimationSequence != sequence) {
            state.getController().forceAnimationReset();
            lastAnimationSequence = sequence;
        }
        String override = activeAnimation();
        return state.setAndContinue(variant.animation(override.isEmpty() ? (state.isMoving() ? "walk" : "idle") : override));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    private final class DungeonAttackGoal extends Goal {
        private int navigationCooldown;

        private DungeonAttackGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive() && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
        }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void stop() {
            getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) { return; }
            getLookControl().setLookAt(target, 30, 30);
            if (!activeAnimation().isEmpty()) {
                getNavigation().stop();
                return;
            }
            if (distanceToSqr(target) <= variant.attackRange() * variant.attackRange() && hasLineOfSight(target)) {
                getNavigation().stop();
                if (attackCooldown <= 0) {
                    startAnimation("attack", true);
                    attackCooldown = variant.attackTicks() + 15;
                }
            } else if (--navigationCooldown <= 0) {
                getNavigation().moveTo(target, 1);
                navigationCooldown = 10;
            }
        }
    }
}
