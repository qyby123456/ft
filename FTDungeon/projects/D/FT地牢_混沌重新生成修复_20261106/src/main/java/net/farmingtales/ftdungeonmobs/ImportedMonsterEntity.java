package net.farmingtales.ftdungeonmobs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class ImportedMonsterEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public ImportedMonsterEntity(EntityType<? extends ImportedMonsterEntity> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    public String modelId() {
        return getType() == ImportedCreatures.GHOST_SCREAMER.get() ? "ghost_screamer" : "ore_monster";
    }

    public static AttributeSupplier.Builder createAttributes(boolean ore) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, ore ? 40 : 24)
                .add(Attributes.ATTACK_DAMAGE, ore ? 6 : 4).add(Attributes.MOVEMENT_SPEED, ore ? 0.23 : 0.28)
                .add(Attributes.ARMOR, ore ? 8 : 0).add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        triggerAnim("main", "attack");
        return super.doHurtTarget(target);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ImportedMonsterEntity>(this, "main", 3,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation." + modelId()
                        + (state.isMoving() ? ".walk" : ".idle"))))
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation." + modelId() + ".attack")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
