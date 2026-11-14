/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.item;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.NativeWeaponTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class NativeActionObservation {
    private static final ThreadLocal<Frame> CURRENT = new ThreadLocal();
    private static final Map<LivingEntity, Outcome> ACTIONS = new WeakHashMap<LivingEntity, Outcome>();
    private static final Map<Entity, Spawn> SPAWNS = new WeakHashMap<Entity, Spawn>();
    private static final Map<LivingEntity, Damage> DAMAGES = new WeakHashMap<LivingEntity, Damage>();
    private static final Map<Entity, Impact> IMPACTS = new WeakHashMap<Entity, Impact>();
    private static final Map<LivingEntity, Heal> HEALS = new WeakHashMap<LivingEntity, Heal>();
    private static final Map<Object, Frame> DELAYED = new WeakHashMap<Object, Frame>();
    private static final Map<LivingEntity, Wear> WEARS = new WeakHashMap<LivingEntity, Wear>();

    private NativeActionObservation() {
    }

    public static Invocation current() {
        Frame frame = CURRENT.get();
        return frame == null ? null : frame.invocation;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void remember(Object object) {
        Frame frame = CURRENT.get();
        if (object != null && frame != null && !frame.invalid) {
            Map<Object, Frame> map = DELAYED;
            synchronized (map) {
                DELAYED.put(object, frame);
            }
        }
    }

    public static void remember(Entity entity, LivingEntity livingEntity) {
        Frame frame = CURRENT.get();
        if (frame != null && frame.caster == livingEntity && entity.m_9236_() == livingEntity.m_9236_()) {
            NativeActionObservation.remember(entity);
        }
    }

    public static void remember(Entity entity, UUID uUID) {
        Frame frame = CURRENT.get();
        if (frame != null && frame.caster.m_20148_().equals(uUID) && entity.m_9236_() == frame.caster.m_9236_()) {
            NativeActionObservation.remember(entity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void inherit(Entity entity, Entity entity2, UUID uUID) {
        Map<Object, Frame> map = DELAYED;
        synchronized (map) {
            Frame frame = DELAYED.get(entity2);
            if (frame != null && !frame.invalid && frame.caster.m_20148_().equals(uUID) && entity.m_9236_() == entity2.m_9236_()) {
                DELAYED.put(entity, frame);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static <Result> Result delayed(Object object, LivingEntity livingEntity, Supplier<Result> supplier) {
        Frame frame;
        Frame frame2 = CURRENT.get();
        Map<Object, Frame> map = DELAYED;
        synchronized (map) {
            frame = DELAYED.get(object);
        }
        if (frame == null || frame.invalid || frame.caster != livingEntity) {
            CURRENT.remove();
        } else {
            CURRENT.set(frame);
        }
        try {
            map = supplier.get();
            return (Result)map;
        }
        finally {
            if (frame2 == null || frame2.invalid) {
                CURRENT.remove();
            } else {
                CURRENT.set(frame2);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void forget(Object object) {
        Map<Object, Frame> map = DELAYED;
        synchronized (map) {
            DELAYED.remove(object);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Invocation origin(Object object) {
        Map<Object, Frame> map = DELAYED;
        synchronized (map) {
            Frame frame = DELAYED.get(object);
            return frame == null || frame.invalid ? null : frame.invocation;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void nativeWear(ItemStack itemStack, int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer) {
        Frame frame = CURRENT.get();
        CompoundTag compoundTag = itemStack.m_41739_(new CompoundTag());
        boolean bl = false;
        Throwable throwable = null;
        try {
            itemStack.m_41622_(n, livingEntity, consumer);
            bl = true;
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            block21: {
                try {
                    if (frame == null || frame.invalid || frame.caster != livingEntity) break block21;
                    Map<LivingEntity, Wear> map = WEARS;
                    synchronized (map) {
                        WEARS.put(livingEntity, new Wear(frame.invocation, compoundTag, itemStack.m_41739_(new CompoundTag()), n, bl));
                    }
                }
                catch (Throwable throwable3) {
                    if (throwable == null) {
                        throw throwable3;
                    }
                    if (throwable3 == throwable) break block21;
                    try {
                        throwable.addSuppressed(throwable3);
                    }
                    catch (Throwable throwable4) {}
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Wear wear(LivingEntity livingEntity) {
        Map<LivingEntity, Wear> map = WEARS;
        synchronized (map) {
            return WEARS.get(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clear(Entity entity) {
        Object object = DELAYED;
        synchronized (object) {
            DELAYED.entrySet().removeIf(entry -> {
                boolean bl;
                boolean bl2 = bl = entry.getKey() == entity || ((Frame)entry.getValue()).caster == entity;
                if (bl && ((Frame)entry.getValue()).caster == entity) {
                    ((Frame)entry.getValue()).invalid = true;
                }
                return bl;
            });
        }
        object = SPAWNS;
        synchronized (object) {
            SPAWNS.remove(entity);
        }
        object = IMPACTS;
        synchronized (object) {
            IMPACTS.remove(entity);
        }
        if (entity instanceof LivingEntity) {
            object = (LivingEntity)entity;
            Map<LivingEntity, Record> map = ACTIONS;
            synchronized (map) {
                ACTIONS.remove(object);
            }
            map = WEARS;
            synchronized (map) {
                WEARS.remove(object);
            }
            map = DAMAGES;
            synchronized (map) {
                DAMAGES.remove(object);
            }
            map = HEALS;
            synchronized (map) {
                HEALS.remove(object);
            }
        }
        if ((object = CURRENT.get()) != null && object.caster == entity) {
            object.invalid = true;
            CURRENT.remove();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearServer(MinecraftServer minecraftServer) {
        HashSet<LivingEntity> hashSet = new HashSet<LivingEntity>();
        Object object = DELAYED;
        synchronized (object) {
            for (Frame frame : DELAYED.values()) {
                if (frame.caster.m_20194_() != minecraftServer) continue;
                hashSet.add(frame.caster);
            }
        }
        object = ACTIONS;
        synchronized (object) {
            for (LivingEntity livingEntity : ACTIONS.keySet()) {
                if (livingEntity.m_20194_() != minecraftServer) continue;
                hashSet.add(livingEntity);
            }
        }
        for (LivingEntity livingEntity : hashSet) {
            NativeActionObservation.clear((Entity)livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static <Result> Result invoke(Action action, ItemStack itemStack, LivingEntity livingEntity, InteractionHand interactionHand, Supplier<Result> supplier) {
        Frame frame = CURRENT.get();
        Frame frame2 = null;
        try {
            if (itemStack != null && livingEntity != null && !livingEntity.m_9236_().f_46443_) {
                frame2 = new Frame(new Invocation(action, livingEntity.m_20148_(), interactionHand, NativeWeaponTypes.id(itemStack), itemStack.m_41720_().getClass().getName(), itemStack.m_41739_(new CompoundTag())), livingEntity);
            }
        }
        catch (RuntimeException runtimeException) {
            frame2 = null;
        }
        if (frame2 == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(frame2);
        }
        boolean bl = false;
        try {
            Result Result2 = supplier.get();
            bl = true;
            Result Result3 = Result2;
            return Result3;
        }
        finally {
            block20: {
                try {
                    if (frame2 == null || frame2.invalid) break block20;
                    Map<LivingEntity, Outcome> map = ACTIONS;
                    synchronized (map) {
                        ACTIONS.put(livingEntity, new Outcome(frame2.invocation, itemStack.m_41739_(new CompoundTag()), bl, frame2.spawned));
                    }
                }
                catch (RuntimeException runtimeException) {}
            }
            if (frame == null || frame.invalid) {
                CURRENT.remove();
            } else {
                CURRENT.set(frame);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void added(Entity entity, boolean bl) {
        Object object;
        if (!bl || entity == null || entity.m_9236_().f_46443_) {
            return;
        }
        Frame frame = CURRENT.get();
        boolean bl2 = frame != null && entity instanceof Projectile && (object = (Projectile)entity).m_19749_() == frame.caster;
        try {
            Double d;
            Object object2;
            String string = entity.getClass().getName();
            String string2 = frame == null ? "" : frame.invocation.itemId();
            InteractionHand interactionHand = frame == null ? null : frame.invocation.hand();
            Vec3 vec3 = entity.m_20184_();
            if (entity instanceof AbstractArrow) {
                object2 = (AbstractArrow)entity;
                d = object2.m_36789_();
            } else {
                d = null;
            }
            object = new Spawn(string, string2, interactionHand, bl2, vec3, d, "OBSERVATION_ONLY_OWNER_RECEIPT_REQUIRED");
            object2 = SPAWNS;
            synchronized (object2) {
                SPAWNS.put(entity, (Spawn)object);
            }
            if (bl2) {
                ++frame.spawned;
                NativeActionObservation.remember(entity);
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void finalDamage(LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (livingEntity == null || damageSource == null || livingEntity.m_9236_().f_46443_) {
            return;
        }
        Entity entity = DAMAGES;
        synchronized (entity) {
            DAMAGES.remove(livingEntity);
        }
        if (!Float.isFinite(f) || f <= 0.0f) {
            return;
        }
        try {
            entity = damageSource.m_7640_();
            Damage damage = new Damage(f, damageSource.m_269415_().f_268677_(), entity == null ? "" : entity.getClass().getName(), "OWNER_VALIDATION_NOT_RUN", "NO_PROC_NO_MULTIPLIER_NO_CLAIM");
            Map<LivingEntity, Damage> map = DAMAGES;
            synchronized (map) {
                DAMAGES.put(livingEntity, damage);
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void impact(Entity entity, Entity entity2, Runnable runnable) {
        float f;
        if (entity2 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            f = livingEntity.m_21223_();
        } else {
            f = Float.NaN;
        }
        float f2 = f;
        runnable.run();
        try {
            float f3;
            Object object;
            if (entity2 instanceof LivingEntity) {
                object = (LivingEntity)entity2;
                f3 = object.m_21223_();
            } else {
                f3 = Float.NaN;
            }
            float f4 = f3;
            object = IMPACTS;
            synchronized (object) {
                IMPACTS.put(entity, new Impact(entity.getClass().getName(), entity2.getClass().getName(), f2 - f4, "HEALTH_OBSERVATION_NOT_SOURCE_AUTHORIZATION"));
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Outcome outcome(LivingEntity livingEntity) {
        Map<LivingEntity, Outcome> map = ACTIONS;
        synchronized (map) {
            return ACTIONS.get(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void heal(LivingEntity livingEntity, float f) {
        if (livingEntity == null || livingEntity.m_9236_().f_46443_) {
            return;
        }
        Object object = HEALS;
        synchronized (object) {
            HEALS.remove(livingEntity);
        }
        if (!Float.isFinite(f) || f <= 0.0f) {
            return;
        }
        try {
            object = NativeActionObservation.current();
            Map<LivingEntity, Heal> map = HEALS;
            synchronized (map) {
                HEALS.put(livingEntity, new Heal(f, object == null ? "" : ((Invocation)object).itemId(), "EVENT_AMOUNT_NOT_HEALTH_GAIN_NOT_AUTHORITY"));
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Heal heal(LivingEntity livingEntity) {
        Map<LivingEntity, Heal> map = HEALS;
        synchronized (map) {
            return HEALS.get(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Impact impact(Entity entity) {
        Map<Entity, Impact> map = IMPACTS;
        synchronized (map) {
            return IMPACTS.get(entity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Spawn spawn(Entity entity) {
        Map<Entity, Spawn> map = SPAWNS;
        synchronized (map) {
            return SPAWNS.get(entity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Damage damage(LivingEntity livingEntity) {
        Map<LivingEntity, Damage> map = DAMAGES;
        synchronized (map) {
            return DAMAGES.get(livingEntity);
        }
    }

    private static final class Frame {
        private final Invocation invocation;
        private final LivingEntity caster;
        private int spawned;
        private boolean invalid;

        private Frame(Invocation invocation, LivingEntity livingEntity) {
            this.invocation = invocation;
            this.caster = livingEntity;
        }
    }

    public record Invocation(Action action, UUID casterId, InteractionHand hand, String itemId, String itemClass, CompoundTag before) {
        private final CompoundTag before;

        public Invocation {
            compoundTag = compoundTag.m_6426_();
        }

        public CompoundTag before() {
            return this.before.m_6426_();
        }
    }

    public record Wear(Invocation invocation, CompoundTag before, CompoundTag after, int requested, boolean returnedNormally) {
        private final CompoundTag before;
        private final CompoundTag after;

        public Wear {
            compoundTag = compoundTag.m_6426_();
            compoundTag2 = compoundTag2.m_6426_();
        }

        public CompoundTag before() {
            return this.before.m_6426_();
        }

        public CompoundTag after() {
            return this.after.m_6426_();
        }
    }

    public static enum Action {
        USE,
        RELEASE,
        USE_TICK,
        FINISH,
        CAST,
        CALLBACK,
        LEFT_CLICK,
        MELEE_HIT;

    }

    public record Outcome(Invocation invocation, CompoundTag after, boolean returnedNormally, int successfulSpawns) {
        private final CompoundTag after;

        public Outcome {
            compoundTag = compoundTag.m_6426_();
        }

        public CompoundTag after() {
            return this.after.m_6426_();
        }
    }

    public record Spawn(String entityClass, String itemId, InteractionHand hand, boolean explicitOwnerMatches, Vec3 nativeVelocity, Double nativeArrowBase, String authorization) {
    }

    public record Damage(float finalPositiveAmount, String damageType, String directClass, String sourceStatus, String authorization) {
    }

    public record Impact(String projectileClass, String targetClass, float observedHealthDelta, String authorization) {
    }

    public record Heal(float acceptedEventAmount, String observedItemId, String authorization) {
    }
}

