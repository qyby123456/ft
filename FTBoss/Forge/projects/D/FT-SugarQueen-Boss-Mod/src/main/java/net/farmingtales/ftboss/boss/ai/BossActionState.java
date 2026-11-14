package net.farmingtales.ftboss.boss.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animation.AnimationState;

public final class BossActionState {
    private final LivingEntity owner;
    private final EntityDataAccessor<CompoundTag> accessor;
    private int renderedSequence = -1;

    public BossActionState(LivingEntity owner, EntityDataAccessor<CompoundTag> accessor) {
        this.owner = owner;
        this.accessor = accessor;
    }

    public void start(String name, int duration) {
        if (this.owner.level().isClientSide) return;
        CompoundTag previous = this.owner.getEntityData().get(this.accessor);
        CompoundTag next = new CompoundTag();
        next.putString("name", name);
        next.putLong("start", this.owner.level().getGameTime());
        next.putInt("duration", Math.max(0, duration));
        next.putInt("sequence", previous.getInt("sequence") + 1);
        if (this.owner instanceof net.farmingtales.ftboss.entity.MeatMountainEntity butcher) {
            next.putInt("visual_phase", butcher.getButcherPhase());
            next.putInt("duration", Math.max(0, ButcherMotionTiming.duration(butcher.getButcherPhase(), name, duration)));
        }
        this.owner.getEntityData().set(this.accessor, next);
    }

    public int duration() {
        return this.owner.getEntityData().get(this.accessor).getInt("duration");
    }

    public float elapsed(float partialTick) {
        CompoundTag data = this.owner.getEntityData().get(this.accessor);
        return Math.max(0, this.owner.level().getGameTime() - data.getLong("start") + partialTick);
    }

    public boolean active() {
        return this.owner.isAlive() && elapsed(0) < duration();
    }

    public String name() {
        return active() ? this.owner.getEntityData().get(this.accessor).getString("name") : "";
    }

    public void prepare(AnimationState<?> state) {
        int sequence = this.owner.getEntityData().get(this.accessor).getInt("sequence");
        if (sequence != this.renderedSequence) {
            this.renderedSequence = sequence;
            state.getController().forceAnimationReset();
        }
        state.getController().setTransitionLength(active() ? 0 : 3);
    }
}
