/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.QizaiArsenalRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QizaiArsenal {
    private final EasterBossEntity boss;
    private final List<Salvo> salvos = new ArrayList<Salvo>();
    private final List<QizaiStarEntity> missiles = new ArrayList<QizaiStarEntity>();
    private int cooldown = 60;
    private int pushCooldown;
    private int pushTicks;
    private int pushFade;
    private Vec3 pushOrigin = Vec3.f_82478_;
    private long pushStarted;

    public QizaiArsenal(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    private boolean eligible() {
        return this.boss.getVariant() == 11 && this.boss.m_6084_() && !this.boss.m_213877_() && !this.boss.isMini() && !this.boss.isGiant() && !this.boss.m_21525_() && !ManorFight.isFinaleMember((LivingEntity)this.boss);
    }

    private boolean enemy(LivingEntity livingEntity) {
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        return livingEntity.m_6084_() && !livingEntity.m_213877_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_() && !this.boss.m_7307_((Entity)livingEntity) && !livingEntity.m_7307_((Entity)this.boss);
    }

    public boolean cast(ServerLevel serverLevel, Vec3 vec3) {
        if (!this.eligible() || this.boss.m_20147_() || serverLevel != this.boss.m_9236_() || this.salvos.size() >= 4 || !Double.isFinite(vec3.f_82479_ + vec3.f_82480_ + vec3.f_82481_)) {
            return false;
        }
        Salvo salvo = new Salvo(serverLevel.m_46467_(), this.boss.m_20182_());
        Vec3 vec32 = vec3;
        for (int i = 0; i < 6; ++i) {
            double d = (double)i * Math.PI * 2.0 / 6.0;
            Vec3 vec33 = this.boss.m_20182_().m_82520_(Math.cos(d) * 2.3, 2.6 + (double)(i % 2) * 0.8, Math.sin(d) * 2.3);
            Vec3 vec34 = vec33.m_82549_(vec32.m_82546_(vec33).m_82541_().m_82490_(32.0));
            salvo.crystals.add(vec33);
            salvo.ends.add(this.clip(serverLevel, vec33, vec34));
        }
        this.salvos.add(salvo);
        this.cooldown = 180;
        this.sync();
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_144243_, SoundSource.HOSTILE, 1.2f, 0.75f);
        return true;
    }

    public void tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
        block12: {
            block11: {
                Object object;
                Object object2;
                if (!this.eligible()) {
                    this.cancel();
                    return;
                }
                if (bl || this.boss.m_20147_()) {
                    this.cancel();
                    return;
                }
                this.missiles.removeIf(qizaiStarEntity -> qizaiStarEntity.m_213877_() || !qizaiStarEntity.m_6084_());
                if (this.cooldown > 0) {
                    --this.cooldown;
                }
                if (this.pushCooldown > 0) {
                    --this.pushCooldown;
                }
                if (this.pushFade > 0) {
                    --this.pushFade;
                }
                if (!bl2 && this.cooldown == 0 && this.salvos.isEmpty() && (object2 = this.boss.m_5448_()) instanceof LivingEntity && this.enemy((LivingEntity)(object = object2)) && this.boss.m_20280_((Entity)object) <= 1024.0 && this.boss.m_142582_((Entity)object)) {
                    this.cast(serverLevel, object.m_146892_());
                }
                object = this.salvos.iterator();
                while (object.hasNext()) {
                    object2 = object.next();
                    if (++object2.age == 24) {
                        this.launch(serverLevel, (Salvo)object2);
                    }
                    for (int i = 0; i < 6; ++i) {
                        if (object2.age != 24 + i * 2) continue;
                        this.laser(serverLevel, (Salvo)object2, i);
                    }
                    if (object2.age < 42) continue;
                    object.remove();
                }
                if (this.pushTicks <= 0) break block11;
                if (--this.pushTicks != 0) break block12;
                this.repel(serverLevel);
                break block12;
            }
            if (this.pushCooldown == 0) {
                for (Object object2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
                    Vec3 vec3 = object2.m_20182_().m_82546_(this.boss.m_20182_());
                    if (!this.enemy((LivingEntity)object2) || vec3.m_165925_() > 25.0 || Math.abs(vec3.f_82480_) > 3.0 || !this.boss.m_142582_((Entity)object2)) continue;
                    this.pushOrigin = this.boss.m_20182_();
                    this.pushStarted = serverLevel.m_46467_();
                    this.pushTicks = 16;
                    this.pushCooldown = 100;
                    serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.0f, 1.6f);
                    break;
                }
            }
        }
        this.sync();
    }

    private void launch(ServerLevel serverLevel, Salvo salvo) {
        ArrayList<LivingEntity> arrayList = new ArrayList<LivingEntity>();
        for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
            if (!this.enemy(livingEntity2) || !this.boss.m_142582_((Entity)livingEntity2) || !(livingEntity2.m_20238_(salvo.center) <= 1024.0)) continue;
            arrayList.add(livingEntity2);
        }
        arrayList.sort(Comparator.comparingDouble(livingEntity -> livingEntity.m_20238_(salvo.center)));
        if (arrayList.isEmpty()) {
            return;
        }
        for (int i = 0; i < 12; ++i) {
            LivingEntity livingEntity2;
            livingEntity2 = salvo.crystals.get(i % salvo.crystals.size());
            double d = (double)i * Math.PI * 2.0 / 12.0;
            QizaiStarEntity qizaiStarEntity = new QizaiStarEntity(serverLevel, this.boss, (Vec3)livingEntity2);
            qizaiStarEntity.track((LivingEntity)arrayList.get(i % arrayList.size()), 60);
            qizaiStarEntity.m_20256_(new Vec3(Math.cos(d), 0.2, Math.sin(d)).m_82541_().m_82490_(0.58));
            serverLevel.m_7967_((Entity)qizaiStarEntity);
            this.missiles.add(qizaiStarEntity);
        }
        serverLevel.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_12417_, SoundSource.HOSTILE, 1.1f, 1.2f);
    }

    private void laser(ServerLevel serverLevel, Salvo salvo, int n) {
        Vec3 vec3 = salvo.crystals.get(n);
        Vec3 vec32 = this.clip(serverLevel, vec3, salvo.ends.get(n));
        salvo.ends.set(n, vec32);
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
            AABB aABB = livingEntity.m_20191_().m_82400_(0.45);
            if (!this.enemy(livingEntity) || salvo.hit.contains(livingEntity.m_20148_()) || !aABB.m_82390_(vec3) && aABB.m_82371_(vec3, vec32).isEmpty()) continue;
            salvo.hit.add(livingEntity.m_20148_());
            livingEntity.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), 48.0f);
        }
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, SoundEvents.f_276532_, SoundSource.HOSTILE, 0.7f, 1.7f);
    }

    private Vec3 clip(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
        return serverLevel.m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_82450_();
    }

    private void repel(ServerLevel serverLevel) {
        this.pushStarted = serverLevel.m_46467_();
        this.pushFade = 12;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
            Vec3 vec3 = livingEntity.m_20182_().m_82546_(this.pushOrigin);
            if (!this.enemy(livingEntity) || !QizaiArsenalRules.insidePush(vec3.m_165925_(), vec3.f_82480_) || serverLevel.m_45547_(new ClipContext(this.pushOrigin.m_82520_(0.0, 1.0, 0.0), livingEntity.m_146892_(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_6662_() != HitResult.Type.MISS) continue;
            Vec3 vec32 = vec3.m_82542_(1.0, 0.0, 1.0).m_82541_();
            if (vec32.m_82556_() < 0.01) {
                vec32 = new Vec3(1.0, 0.0, 0.0);
            }
            BossKnockback.push((Entity)this.boss, (Entity)livingEntity, vec32.f_82479_ * 5.2, 0.8, vec32.f_82481_ * 5.2);
            livingEntity.f_19864_ = true;
        }
        serverLevel.m_6263_(null, this.pushOrigin.f_82479_, this.pushOrigin.f_82480_, this.pushOrigin.f_82481_, SoundEvents.f_215771_, SoundSource.HOSTILE, 1.1f, 1.4f);
    }

    private void sync() {
        CompoundTag compoundTag = new CompoundTag();
        if (this.boss.isQizaiProtectedCasting()) {
            compoundTag.m_128379_("shield", true);
        }
        ListTag listTag = new ListTag();
        for (Salvo salvo : this.salvos) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128356_("start", salvo.start);
            ListTag listTag2 = new ListTag();
            for (int i = 0; i < salvo.crystals.size(); ++i) {
                CompoundTag compoundTag3 = new CompoundTag();
                QizaiArsenal.putPoint(compoundTag3, "", salvo.crystals.get(i));
                QizaiArsenal.putPoint(compoundTag3, "end", salvo.ends.get(i));
                listTag2.add((Object)compoundTag3);
            }
            compoundTag2.m_128365_("crystals", (Tag)listTag2);
            listTag.add((Object)compoundTag2);
        }
        if (!listTag.isEmpty()) {
            compoundTag.m_128365_("salvos", (Tag)listTag);
        }
        if (this.pushTicks > 0 || this.pushFade > 0) {
            CompoundTag compoundTag4 = new CompoundTag();
            QizaiArsenal.putPoint(compoundTag4, "", this.pushOrigin);
            compoundTag4.m_128356_("start", this.pushStarted);
            compoundTag4.m_128379_("released", this.pushTicks == 0);
            compoundTag.m_128365_("push", (Tag)compoundTag4);
        }
        if (!compoundTag.equals((Object)this.boss.getQizaiArsenalCue())) {
            this.boss.setQizaiArsenalCue(compoundTag);
        }
    }

    private static void putPoint(CompoundTag compoundTag, String string, Vec3 vec3) {
        compoundTag.m_128347_(string + "x", vec3.f_82479_);
        compoundTag.m_128347_(string + "y", vec3.f_82480_);
        compoundTag.m_128347_(string + "z", vec3.f_82481_);
    }

    public void cancel() {
        this.salvos.clear();
        this.missiles.forEach(Entity::m_146870_);
        this.missiles.clear();
        this.pushTicks = 0;
        this.pushFade = 0;
        if (!this.boss.getQizaiArsenalCue().m_128456_()) {
            this.boss.setQizaiArsenalCue(new CompoundTag());
        }
    }

    private static final class Salvo {
        final long start;
        final Vec3 center;
        final List<Vec3> crystals = new ArrayList<Vec3>();
        final List<Vec3> ends = new ArrayList<Vec3>();
        final Set<UUID> hit = new HashSet<UUID>();
        int age;

        Salvo(long start, Vec3 center) {
            this.start = start;
            this.center = center;
        }
    }
}

