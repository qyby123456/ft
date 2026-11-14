package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.TideFishCombatRules;
import net.farmingtales.ftboss.util.TideFishCombatRules.Skill;
import net.farmingtales.ftboss.util.WaterBlessing;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TideFishBossEntity extends Monster implements GeoAnimatable, net.farmingtales.ftboss.integ.spells.RegularBossCastHost {
    public static final double MAX_HEALTH = 960.0;
    private static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(TideFishBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> ACTION =
            SynchedEntityData.defineId(TideFishBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final BossActionState action = new BossActionState(this, ACTION);
    private static final EntityDataAccessor<CompoundTag> SIGNATURE_CAST_ACTION =
            SynchedEntityData.defineId(TideFishBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private final BossActionState signatureCast = new BossActionState(this, SIGNATURE_CAST_ACTION);
    private static final RawAnimation SIGNATURE_CAST = RawAnimation.begin().thenPlay("animation.tide_fish.signature_cast");
    private static final RawAnimation SIGNATURE_SELF = RawAnimation.begin().thenPlay("animation.tide_fish.signature_self_cast");
    private final ServerBossEvent bar;
    private final Set<UUID> hitTargets = new HashSet<>();
    private Vec3 lockedDirection = Vec3.ZERO;
    private Vec3 lockedCenter = Vec3.ZERO;
    private String skill = "";
    private int skillTick;
    private int skillLength;
    private int cooldown = TideFishCombatRules.openingCooldown();
    private int rotation;
    private Skill activeSkill;
    private Skill pendingSkill;
    private Vec3[] hazardCenters = new Vec3[0];
    private Vec3 waveCenter = Vec3.ZERO;
    private boolean dashBlocked;

    public TideFishBossEntity(EntityType<? extends TideFishBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 160;
        this.setPersistenceRequired();
        this.bar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(BossWalkingSpeed.speed(this, speed));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ATTACK_SPEED, 5.6)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE);   // 2026-09-19: 允许被击退
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
        this.entityData.define(ACTION, new CompoundTag());
        this.entityData.define(SIGNATURE_CAST_ACTION, new CompoundTag());
    }

    public int getPhase() { return this.entityData.get(PHASE); }

    @Override
    public void startSignatureCast(net.farmingtales.ftboss.util.BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(net.farmingtales.ftboss.integ.spells.RegularBossCastHost.action(kind),
                net.farmingtales.ftboss.integ.spells.RegularBossCastHost.visualTicks(castTicks));
    }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect().isBeneficial() && super.canBeAffected(effect);
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof TideFishBossEntity || source.getEntity() instanceof ReefMinionEntity) return false;
        float damage = BossDamageGate.gate(this, source, amount);
        if (damage <= 0) return false;
        boolean hit = super.hurt(source, damage);
        if (hit) BossDamageGate.onHit(this);
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        BossBars.track(this, this.bar);
        BossDamageGate.tick(this);
        BossRage.tickBoss(this);
        TrapBreaker.tick(this);
        WaterBlessing.tick(this);
        this.bar.setName(getDisplayName());
        this.bar.setProgress(Math.max(0, Math.min(1, getHealth() / getMaxHealth())));
        if (tickCount % 10 == 0) {
            for (ServerPlayer viewer : Set.copyOf(this.bar.getPlayers())) {
                if (viewer.level() != level() || !viewer.isAlive() || distanceToSqr(viewer) > 2304) this.bar.removePlayer(viewer);
            }
            for (ServerPlayer viewer : server.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(48))) {
                if (viewer.isAlive() && distanceToSqr(viewer) <= 2304) this.bar.addPlayer(viewer);
            }
        }
        LivingEntity target = getTarget();
        if (target == null || target.level() != level() || !target.isAlive() || !BossTargets.valid(target) || distanceToSqr(target) > 1600) {
            target = BossTargets.nearest(server, this, 40);
            setTarget(target);
        }
        if (target != null) BossMusic.request(server, this);
        else BossMusic.release(server, this);
        if (getPhase() == 1 && getHealth() <= getMaxHealth() * 0.5f) {
            this.entityData.set(PHASE, 2);
            this.rotation = 0;
            this.activeSkill = null;
            this.pendingSkill = null;
            begin("phase", TideFishCombatRules.PHASE_TICKS, target);
            this.bar.setColor(BossEvent.BossBarColor.PURPLE);
        }
        if (!this.skill.isEmpty()) {
            getNavigation().stop();
            tickSkill(server);
            return;
        }
        if (target == null) { getNavigation().stop(); return; }
        getLookControl().setLookAt(target, 30, 30);
        if (distanceToSqr(target) > 10) {
            if (tickCount % 10 == 0) getNavigation().moveTo(target, getPhase() == 2 ? 1.5625 : 1.3125);
        } else getNavigation().stop();
        if (--this.cooldown > 0 || !hasLineOfSight(target)) return;
        // 一次出手连放两招：第一招立即起手，第二招排队，在本招结束的同一 tick 无缝接上（不等出招间隔）。
        this.activeSkill = TideFishCombatRules.next(this.rotation);
        this.pendingSkill = TideFishCombatRules.next(this.rotation + 1);
        this.rotation = Math.floorMod(this.rotation + 2, Skill.values().length);
        begin(this.activeSkill.id, this.activeSkill.duration(getPhase() == 2), target);
    }

    private void begin(String name, int length, LivingEntity target) {
        this.skill = name;
        this.skillTick = 0;
        this.skillLength = length;
        this.hitTargets.clear();
        this.lockedDirection = target == null ? getLookAngle() : target.position().subtract(position()).multiply(1, 0, 1).normalize();
        if (this.lockedDirection.horizontalDistanceSqr() < 0.001) this.lockedDirection = new Vec3(0, 0, 1);
        this.lockedCenter = target == null ? position() : target.position();
        this.waveCenter = position();
        this.dashBlocked = false;
        int hazards = name.equals("geyser") ? TideFishCombatRules.geyserCount(getPhase() == 2)
                : name.equals("maelstrom") ? TideFishCombatRules.maelstromCount(getPhase() == 2) : 0;
        this.hazardCenters = new Vec3[hazards];
        Vec3 sideways = new Vec3(this.lockedDirection.z, 0, -this.lockedDirection.x);
        for (int index = 0; index < hazards; index++) {
            double offset = index == 0 ? 0 : (index % 2 == 1 ? 1 : -1) * ((index + 1) / 2) * 4.5;
            this.hazardCenters[index] = this.lockedCenter.add(sideways.scale(offset));
        }
        faceLockedDirection();
        this.action.start(name + (getPhase() == 2 && !name.equals("phase") ? "_p2" : ""), length);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        getNavigation().stop();
    }

    private void faceLockedDirection() {
        float facing = (float) Math.toDegrees(Math.atan2(-this.lockedDirection.x, this.lockedDirection.z));
        setYRot(facing);
        this.yBodyRot = facing;
        this.yHeadRot = facing;
    }

    /* ★ 2026-09-19 按用户要求「把鱼王的技能预警去掉」：
       原来这里有个 announce(name) —— 每次起手把技能名 + 躲避提示（如
       「摆尾分潮！退开或绕到身后！」）推到玩家 actionbar。已整段删除，
       连同下面 tickSkill 里所有 END_ROD 地面预警圈。两处 lang 键
       （message.ftboss.tide_*）因此不再被引用，留着无害。
       注意：**命中特效与阶段特效（SPLASH 环）全部保留** —— 去掉的只是"提前告诉玩家"。 */

    private void tickSkill(ServerLevel server) {
        int elapsed = ++this.skillTick;
        boolean enraged = getPhase() == 2;
        float damage = this.activeSkill == null ? 0 : this.activeSkill.damage(enraged);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        faceLockedDirection();
        switch (this.skill) {
            case "sweep" -> {
                if (TideFishCombatRules.sweepAt(elapsed, enraged)) {
                    this.hitTargets.clear();
                    strikeArea(server, position(), enraged ? 5.5 : 4.5, damage, elapsed == 4, true);
                    sound(SoundEvents.PLAYER_ATTACK_SWEEP, elapsed == 4 ? 0.7f : 1.0f);
                }
            }
            case "volley" -> {
                if (elapsed < 5 && elapsed % 2 == 0) server.sendParticles(ParticleTypes.BUBBLE_POP,
                        getX(), getEyeY(), getZ(), 8, 0.3, 0.3, 0.3, 0.02);
                if (TideFishCombatRules.volleyAt(elapsed, enraged)) {
                    Vec3 aim = this.lockedCenter.add(0, 1, 0).subtract(getEyePosition()).normalize();
                    int half = TideFishCombatRules.volleyBolts(enraged) / 2;
                    for (int index = -half; index <= half; index++) {
                        TideBoltEntity bolt = new TideBoltEntity(FTEntities.TIDE_BOLT.get(), server);
                        bolt.setOwner(this);
                        bolt.setDamage(BossScaling.dmg(this, damage));
                        bolt.setPos(getX(), getEyeY() - 0.2, getZ());
                        Vec3 direction = aim.yRot(index * 0.14f);
                        bolt.shoot(direction.x, direction.y, direction.z, 1.3f, 0);
                        server.addFreshEntity(bolt);
                    }
                    sound(SoundEvents.DROWNED_SHOOT, 0.8f);
                }
            }
            case "dash" -> {
                if (enraged && elapsed == 19) {
                    this.lockedDirection = this.lockedDirection.scale(-1);
                    faceLockedDirection();
                    this.hitTargets.clear();
                    this.dashBlocked = false;
                    sound(SoundEvents.TRIDENT_RETURN, 0.7f);
                }
                if (TideFishCombatRules.dashActive(elapsed, enraged) && !this.dashBlocked) {
                    if (elapsed == 5 || elapsed == 31) sound(SoundEvents.TRIDENT_RIPTIDE_1, 0.85f);
                    Vec3 motion = this.lockedDirection.scale(enraged ? 1.05 : 0.92);
                    if (!server.noCollision(this, getBoundingBox().expandTowards(motion))) {
                        this.dashBlocked = true;
                        setDeltaMovement(0, getDeltaMovement().y, 0);
                    } else {
                        setDeltaMovement(motion.x, getDeltaMovement().y, motion.z);
                        strikeArea(server, position(), 2.6, damage, false, true);
                        ring(server, position(), 1.4);
                    }
                }
            }
            case "summon" -> {
                if (elapsed == 6) summon(server);
            }
            case "wave" -> {
                int age = TideFishCombatRules.waveAge(elapsed, enraged);
                if (age == 0) {
                    this.hitTargets.clear();
                    sound(SoundEvents.GENERIC_SPLASH, 0.7f);
                }
                if (age >= 0) {
                    double radius = 1 + age * 0.6;
                    if (elapsed % 2 == 0) ring(server, this.waveCenter, radius);
                    for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class,
                            new net.minecraft.world.phys.AABB(this.waveCenter, this.waveCenter).inflate(12, 3, 12))) {
                        double distance = player.position().subtract(this.waveCenter).horizontalDistance();
                        if (Math.abs(distance - radius) < 0.8 && Math.abs(player.getY() - this.waveCenter.y) < 0.75) hit(player, damage, true);
                    }
                }
            }
            case "maelstrom" -> {
                for (int index = 0; index < this.hazardCenters.length; index++) {
                    int release = 8 + index * 7;
                    Vec3 center = this.hazardCenters[index];
                    if (elapsed >= release && elapsed < release + 12 && elapsed % 3 == 0) {
                        ring(server, center.add(0, (elapsed - release) * 0.16, 0), 3.5 - (elapsed - release) * 0.2);
                    }
                }
                int pulse = TideFishCombatRules.maelstromAt(elapsed, enraged);
                if (pulse >= 0) {
                    this.hitTargets.clear();
                    strikeArea(server, this.hazardCenters[pulse], 3.5, damage, false, true);
                    sound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 0.7f);
                }
            }
            case "geyser" -> {
                int pulse = TideFishCombatRules.geyserAt(elapsed, enraged);
                if (pulse >= 0) {
                    Vec3 center = this.hazardCenters[pulse];
                    this.hitTargets.clear();
                    strikeArea(server, center, 1.8, damage, false, true);
                    for (int height = 0; height < 6; height++) {
                        server.sendParticles(ParticleTypes.SPLASH, center.x, center.y + height * 0.7, center.z,
                                14, 0.65, 0.25, 0.65, 0.12);
                    }
                    sound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, enraged ? 0.8f : 1.0f);
                }
            }
            case "phase" -> {
                if (elapsed % 5 == 0) ring(server, position().add(0, 1.2, 0), 3);
                if (elapsed == 20) sound(SoundEvents.CONDUIT_ACTIVATE, 0.75f);
            }
            default -> { }
        }
        if (this.skillTick >= this.skillLength) {
            this.skill = "";
            this.activeSkill = null;
            setDeltaMovement(0, getDeltaMovement().y, 0);
            if (this.pendingSkill != null) {
                Skill queued = this.pendingSkill;
                this.pendingSkill = null;
                this.activeSkill = queued;
                begin(queued.id, queued.duration(enraged), getTarget());
                this.cooldown = 0;
            } else {
                this.cooldown = TideFishCombatRules.cooldown(enraged);
            }
        }
    }

    private void strikeArea(ServerLevel server, Vec3 center, double radius, float damage, boolean cone, boolean once) {
        for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class,
                new net.minecraft.world.phys.AABB(center, center).inflate(radius, 2.5, radius))) {
            Vec3 offset = player.position().subtract(center);
            if (offset.horizontalDistanceSqr() > radius * radius) continue;
            if (cone && offset.multiply(1, 0, 1).normalize().dot(this.lockedDirection) < -0.1) continue;
            hit(player, damage, once);
        }
        if (!once) sound(SoundEvents.GENERIC_SPLASH, 0.65f);
    }

    private void hit(ServerPlayer target, float damage, boolean once) {
        if (!BossTargets.valid(target) || !hasLineOfSight(target)) return;
        if (once && !this.hitTargets.add(target.getUUID())) return;
        if (target.hurt(damageSources().mobAttack(this), BossScaling.dmg(this, damage))) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            Vec3 push = target.position().subtract(position()).multiply(1, 0, 1).normalize().scale(0.45);
            net.farmingtales.ftboss.util.BossKnockback.push(target, push.x, 0.15, push.z);
            target.hurtMarked = true;
        }
    }

    private void summon(ServerLevel server) {
        int owned = server.getEntitiesOfClass(ReefMinionEntity.class, getBoundingBox().inflate(64),
                minion -> getUUID().equals(minion.ownerId())).size();
        int count = TideFishCombatRules.summonCount(getPhase() == 2, owned);
        for (int index = 0; index < count; index++) {
            ReefMinionEntity minion = (index % 2 == 0 ? FTEntities.REEF_FISH : FTEntities.BRINE_PUFFER).get().create(server);
            if (minion == null) continue;
            long nearby = server.getEntitiesOfClass(ReefMinionEntity.class, getBoundingBox().inflate(64),
                    existing -> existing.isAlive() && existing.getType() == minion.getType()).size();
            if (nearby >= net.farmingtales.ftboss.boss.ai.SummonLimit.MAX_PER_TYPE) continue;
            double angle = index * Math.PI * 2 / Math.max(1, count);
            minion.moveTo(getX() + Math.cos(angle) * 3, getY() + 0.3, getZ() + Math.sin(angle) * 3, getYRot(), 0);
            minion.setOwnerId(getUUID());
            minion.setTarget(getTarget());
            if (getPhase() == 2) {
                minion.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 0));
                minion.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 0));
            }
            if (server.noCollision(minion)) server.addFreshEntity(minion);
        }
        sound(SoundEvents.PUFFER_FISH_BLOW_UP, 0.7f);
    }

    /**
     * 画一圈 SPLASH 水花 —— 现在**只用于命中/推进特效**。
     *
     * <p>★ 2026-09-19：原来它带一个 {@code warning} 开关，置 true 时改用
     * {@link ParticleTypes#END_ROD}（亮白粒子）画"即将打到这里"的地面预警圈。
     * 用户要求去掉鱼王的技能预警后，所有 true 调用都已删除，
     * 这个开关随之取消 —— 留着它就是留一条"预警复活"的路。
     */
    private static void ring(ServerLevel server, Vec3 center, double radius) {
        for (int index = 0; index < 40; index++) {
            double angle = index * Math.PI * 2 / 40;
            server.sendParticles(ParticleTypes.SPLASH,
                    center.x + Math.cos(angle) * radius, center.y + 0.2,
                    center.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
    }

    private void sound(net.minecraft.sounds.SoundEvent sound, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.HOSTILE, 1.2f, pitch);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        cleanup();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        cleanup();
        super.remove(reason);
    }

    private void cleanup() {
        if (!(level() instanceof ServerLevel server)) return;
        BossBars.untrack(this, this.bar);
        BossMusic.release(server, this);
        for (ReefMinionEntity minion : server.getEntitiesOfClass(ReefMinionEntity.class, getBoundingBox().inflate(96))) {
            if (getUUID().equals(minion.ownerId())) minion.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TidePhase", getPhase());
        tag.putInt("TideRotation", this.rotation);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(PHASE, tag.getInt("TidePhase") == 2 ? 2 : 1);
        this.rotation = Math.max(0, tag.getInt("TideRotation"));
        this.bar.setColor(getPhase() == 2 ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.BLUE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "tide", 3, state -> {
            this.action.prepare(state);
            this.signatureCast.prepare(state);
            if (isAlive() && this.signatureCast.active()) {
                return state.setAndContinue("signature_self_cast".equals(this.signatureCast.name())
                        ? SIGNATURE_SELF : SIGNATURE_CAST);
            }
            String name = !isAlive() ? "death" : this.action.active() ? this.action.name()
                    : state.isMoving() ? "walk" : "idle";
            return state.setAndContinue(name.equals("idle") || name.equals("walk")
                    ? RawAnimation.begin().thenLoop("animation.tide_fish." + name)
                    : RawAnimation.begin().thenPlay("animation.tide_fish." + name));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object object) { return this.tickCount; }
}
