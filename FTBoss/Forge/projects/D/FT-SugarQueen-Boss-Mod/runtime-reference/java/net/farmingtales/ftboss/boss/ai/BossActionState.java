/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.world.entity.LivingEntity
 *  software.bernie.geckolib.core.animation.AnimationState
 */
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
        if (this.owner.m_9236_().f_46443_) {
            return;
        }
        CompoundTag previous = (CompoundTag)this.owner.m_20088_().m_135370_(this.accessor);
        CompoundTag next = new CompoundTag();
        next.m_128359_("name", name);
        next.m_128356_("start", this.owner.m_9236_().m_46467_());
        next.m_128405_("duration", Math.max(0, duration));
        next.m_128405_("sequence", previous.m_128451_("sequence") + 1);
        this.owner.m_20088_().m_135381_(this.accessor, (Object)next);
    }

    public int duration() {
        return ((CompoundTag)this.owner.m_20088_().m_135370_(this.accessor)).m_128451_("duration");
    }

    public float elapsed(float partialTick) {
        CompoundTag data = (CompoundTag)this.owner.m_20088_().m_135370_(this.accessor);
        return Math.max(0.0f, (float)(this.owner.m_9236_().m_46467_() - data.m_128454_("start")) + partialTick);
    }

    public boolean active() {
        return this.owner.m_6084_() && this.elapsed(0.0f) < (float)this.duration();
    }

    public String name() {
        return this.active() ? ((CompoundTag)this.owner.m_20088_().m_135370_(this.accessor)).m_128461_("name") : "";
    }

    public void prepare(AnimationState<?> state) {
        int sequence = ((CompoundTag)this.owner.m_20088_().m_135370_(this.accessor)).m_128451_("sequence");
        if (sequence != this.renderedSequence) {
            this.renderedSequence = sequence;
            state.getController().forceAnimationReset();
        }
        state.getController().setTransitionLength(this.active() ? 0 : 3);
    }
}

