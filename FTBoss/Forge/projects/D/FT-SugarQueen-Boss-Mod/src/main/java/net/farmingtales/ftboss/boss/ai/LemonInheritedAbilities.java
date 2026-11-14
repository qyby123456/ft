package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.boss.BossHandler;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public final class LemonInheritedAbilities {
    private final EasterBossEntity owner;
    private final LemonSacrifice sacrifice;
    private final TeaTactics tea;
    private final BarrelTactics barrel;
    private Vec3 rollDirection = Vec3.ZERO;
    private long rollStarted = -1;
    private long blastAt = -1;

    public LemonInheritedAbilities(EasterBossEntity owner, LemonSacrifice sacrifice) {
        this.owner = owner;
        this.sacrifice = sacrifice;
        this.tea = new TeaTactics(owner, sacrifice::inheritedChild);
        this.barrel = new BarrelTactics(owner, LemonInheritedAbilityRules.MULTIPLIER);   // v159: 继承技能 ×5
    }

    private boolean due(String name, int period) {
        long now = this.owner.level().getGameTime();
        String key = "next_" + name;
        if (!this.sacrifice.state().contains(key)) {
            this.sacrifice.state().putLong(key, now + period);
            return false;
        }
        if (now < this.sacrifice.state().getLong(key)) return false;
        this.sacrifice.state().putLong(key, now + period);
        return true;
    }

    public void tick() {
        if (!(this.owner.level() instanceof ServerLevel level)) return;
        LivingEntity target = this.owner.getTarget();
        boolean valid = target != null && target.level() == level && BossSpellEffects.canTarget(this.owner, target)
                && this.owner.distanceToSqr(target) <= 32 * 32 && this.owner.hasLineOfSight(target);
        if (due("purge", 20)) {
            for (MobEffectInstance effect : new ArrayList<>(this.owner.getActiveEffects())) {
                if (!effect.getEffect().isBeneficial()) this.owner.removeEffect(effect.getEffect());
            }
        }
        if (due("blessing", 60)) blessOwned(level);
        if (this.sacrifice.has("sugar_queen")) queen(valid ? target : null);
        if (this.sacrifice.has("tea_tree")) tea(level, valid ? target : null);
        if (this.sacrifice.has("wine_barrel")) wine(level, valid ? target : null);
        if (this.sacrifice.has("rat_boss")) rats(level, valid ? target : null);
        if (this.sacrifice.has("barrel_minion")) barrelBlast(level, valid ? target : null);
        if (this.sacrifice.has("meat_mountain") && valid && this.owner.distanceToSqr(target) <= 9
                && due("butcher", ButcherCombatRules.interval(this.owner.getHealth() < this.owner.getMaxHealth() * 0.5F))) {
            int hits = this.sacrifice.state().getInt("butcher_hits");
            float before = target.getHealth() + target.getAbsorptionAmount();
            /* v159 按用户要求「获取的 boss 技能伤害 ×5」：继承来的屠夫近战走这个倍率。 */
            if (target.hurt(this.owner.damageSources().mobAttack(this.owner),
                    LemonInheritedAbilityRules.damage(ButcherCombatRules.damage(5.5F, hits)))) {
                onMeleeHit(target, Math.max(0, before - target.getHealth() - target.getAbsorptionAmount()));
                this.owner.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
        }
    }

    private void blessOwned(ServerLevel level) {
        int count = 0;
        int duration = (int) Math.min(140, this.sacrifice.state().getLong("until") - level.getGameTime());
        for (Mob minion : level.getEntitiesOfClass(Mob.class, this.owner.getBoundingBox().inflate(40),
                entity -> entity.isAlive() && this.sacrifice.inheritedChild(entity))) {
            if (++count > 8) break;
            int effects = 0;
            for (MobEffectInstance effect : this.owner.getActiveEffects()) {
                if (!effect.getEffect().isBeneficial()) continue;
                if (minion instanceof RatBossEntity && (effect.getEffect() == MobEffects.REGENERATION
                        || effect.getEffect() == MobEffects.ABSORPTION || effect.getEffect() == MobEffects.HEALTH_BOOST
                        || effect.getEffect() == MobEffects.SATURATION || effect.getEffect() == MobEffects.HEAL)) continue;
                minion.addEffect(new MobEffectInstance(effect.getEffect(), duration,
                        Math.min(1, Math.max(0, effect.getAmplifier() - 1)), true, false, true));
                if (++effects >= 3) break;
            }
        }
    }

    private void queen(LivingEntity target) {
        if (due("sweet_aura", 10)) {
            grant("farm_and_charm:sweets", 600, 2);
            grant("brewinandchewin:sweet_heart", 600, 0);
        }
        if (target != null && !this.owner.isPerformingSkill() && this.rollStarted < 0) {
            Vec3 offset = target.position().add(0, 2.2, 0).subtract(this.owner.position());
            if (offset.lengthSqr() > 4) {
                Vec3 destination = this.owner.position().add(offset.normalize().scale(0.25));
                if (this.owner.level().noCollision(this.owner, this.owner.getBoundingBox().move(destination.subtract(this.owner.position())))) {
                    this.owner.teleportTo(destination.x, destination.y, destination.z);
                }
            }
        }
    }

    private void tea(ServerLevel level, LivingEntity target) {
        boolean lowHealth = this.owner.getHealth() < this.owner.getMaxHealth() * 0.5F;
        if (due("tea_aura", 10)) grant("herbalbrews:renewal", 600, 1);
        if (target != null) {
            this.tea.tickMinionSummon();
            this.tea.absorb();
        }
        if (due("tea_buffs", 110 + this.owner.getRandom().nextInt(60))) {
            List<MobEffect> pool = List.of(MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_BOOST, MobEffects.DIG_SPEED,
                    MobEffects.DAMAGE_RESISTANCE, MobEffects.REGENERATION, MobEffects.ABSORPTION, MobEffects.SATURATION,
                    MobEffects.FIRE_RESISTANCE, MobEffects.LUCK, MobEffects.JUMP, MobEffects.WATER_BREATHING);
            for (int count = 0; count < 2; count++) grant(pool.get(this.owner.getRandom().nextInt(pool.size())),
                    100 + this.owner.getRandom().nextInt(120), this.owner.getRandom().nextInt(3));
        }
        if (target == null) return;
        if (due("tea_growth", 1200)) {
            int rank = Math.min(10, this.sacrifice.state().getInt("tea_growth") + 1);
            this.sacrifice.state().putInt("tea_growth", rank);
            grant(MobEffects.HEALTH_BOOST, 12000, rank - 1);
            this.owner.heal(4);
        }
        double distance = this.owner.distanceTo(target);
        if (distance > 1.5 && distance < 16 && due("roots", lowHealth ? 62 : 100)) {
            Vec3 direction = target.position().subtract(this.owner.position()).multiply(1, 0, 1).normalize();
            int count = 9 + this.owner.getRandom().nextInt(3);
            for (int index = 1; index <= count; index++) {
                Vec3 position = this.owner.position().add(direction.scale(index * 1.6));
                BlockPos block = BlockPos.containing(position);
                double height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, block.getX(), block.getZ());
                EvokerFangs fangs = new EvokerFangs(level, position.x, height, position.z,
                        (float) Math.atan2(direction.z, direction.x), (index - 1) * 2, this.owner);
                ManorFightMembers.inherit(this.owner, fangs);
                level.addFreshEntity(fangs);
            }
            Fx.sound(level, this.owner.getX(), this.owner.getY(), this.owner.getZ(), SoundEvents.ROOTED_DIRT_PLACE, 1, 0.7F);
        }
        if (distance < 16 && due("poison", lowHealth ? 100 : 160)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 220, 2));
            target.hurt(this.owner.damageSources().indirectMagic(this.owner, this.owner),
                    LemonInheritedAbilityRules.damage(2.5F));   // v159: 继承技能 ×5
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY() + 1, target.getZ(), 16, 0.5, 0.5, 0.5, 0.03);
        }
    }

    private void wine(ServerLevel level, LivingEntity target) {
        int stage = this.owner.getHealth() < this.owner.getMaxHealth() * 0.34F ? 2
                : this.owner.getHealth() < this.owner.getMaxHealth() * 0.67F ? 1 : 0;
        stage = Math.max(stage, this.sacrifice.state().getInt("wine_stage"));
        this.sacrifice.state().putInt("wine_stage", stage);
        if (due("wine_aura", 10)) {
            grant("vinery:armor_effect", 600, stage);
            grant("vinery:health_effect", 600, 1 + stage);
            if (stage >= 1) grant("vinery:resistance_effect", 600, stage - 1);
            if (stage >= 2) grant("vinery:luck_effect", 600, 1);
            this.barrel.mist();
        }
        if (target == null) {
            this.rollStarted = -1;
            return;
        }
        if (due("drunken_aura", 30)) {
            for (ServerPlayer player : level.players()) {
                if (BossSpellEffects.canTarget(this.owner, player) && this.owner.distanceToSqr(player) <= 49
                        && this.owner.hasLineOfSight(player)) {
                    debuff(player, "brewery:drunk", 200, 0);
                    debuff(player, "brewinandchewin:tipsy", 240, 4);
                }
            }
        }
        if (due("cannon", stage >= 2 ? 110 : 170)) cannon(level, target);
        if (due("fireball", stage >= 2 ? 45 : 70)) {
            var fireball = new io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball(level, this.owner);
            Vec3 direction = target.getEyePosition().subtract(this.owner.getEyePosition()).normalize();
            fireball.setPos(this.owner.getEyePosition().add(direction.scale(1.8)));
            fireball.setDamage(LemonInheritedAbilityRules.damage(11 + BossRage.tierOf(this.owner) * 4));   // v159
            fireball.shoot(direction);
            ManorFightMembers.inherit(this.owner, fireball);
            level.addFreshEntity(fireball);
            Fx.sound(level, this.owner.getX(), this.owner.getY(), this.owner.getZ(), SoundEvents.BLAZE_SHOOT, 1.2F, 0.85F);
        }
        if (due("wine_summon", stage >= 1 ? 150 : 210)) {
            int alive = level.getEntitiesOfClass(Mob.class, this.owner.getBoundingBox().inflate(48),
                    entity -> entity.isAlive() && this.sacrifice.inheritedChild(entity)
                            && (entity.getType() == FTEntities.BARREL_MINION.get()
                            || ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString().equals("brewery:beer_elemental"))).size();
            int count = Math.min(2 + this.owner.getRandom().nextInt(2), Math.max(0, 8 - alive));
            for (int index = 0; index < count; index++) summon(level, FTEntities.BARREL_MINION.get().create(level), target);
            var beer = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("brewery", "beer_elemental"));
            if (alive + count < 8 && beer != null && beer.create(level) instanceof Mob minion) summon(level, minion, target);
        }
        roll(level, target);
        if (this.rollStarted < 0 && due("spray", 100)) {
            withFacing(target, this.barrel::corkSpray);
        }
    }

    private void cannon(ServerLevel level, LivingEntity target) {
        int count = 3 + this.owner.getRandom().nextInt(3);
        for (int index = 0; index < count; index++) {
            double offsetX = target.getX() + (this.owner.getRandom().nextDouble() - 0.5) * 7 - this.owner.getX();
            double offsetZ = target.getZ() + (this.owner.getRandom().nextDouble() - 0.5) * 7 - this.owner.getZ();
            double distance = Math.max(1, Math.sqrt(offsetX * offsetX + offsetZ * offsetZ));
            double flight = distance / 1.05;
            var ball = new BarrelCannonballEntity(level, this.owner);
            /* v159: 柠檬猫继承来的炮弹 —— 直击与爆炸威力都按"继承技能 ×5"放大（真身酒桶王的不变）。 */
            ball.setExplosionPower(BarrelCannonballEntity.EXPLOSION_POWER * LemonInheritedAbilityRules.MULTIPLIER);
            ball.setPos(this.owner.getX() + offsetX / distance * 2.2, this.owner.getY() + 1.9,
                    this.owner.getZ() + offsetZ / distance * 2.2);
            ball.setDeltaMovement(offsetX / distance * 1.05,
                    (target.getEyeY() - this.owner.getY() - 1.8) / flight + 0.0225 * flight, offsetZ / distance * 1.05);
            ManorFightMembers.inherit(this.owner, ball);
            level.addFreshEntity(ball);
        }
        Fx.sound(level, this.owner.getX(), this.owner.getY(), this.owner.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.3F, 0.7F);
    }

    private void roll(ServerLevel level, LivingEntity target) {
        if (this.rollStarted < 0) {
            if (this.owner.isPerformingSkill() || !due("roll", 130)) return;
            this.rollDirection = target.position().subtract(this.owner.position()).multiply(1, 0, 1).normalize();
            this.rollStarted = level.getGameTime();
        }
        long elapsed = level.getGameTime() - this.rollStarted;
        if (elapsed < 10) {
            if (elapsed % 2 == 0) this.barrel.warnCharge(this.rollDirection.x, this.rollDirection.z);
        } else if (elapsed < 32) {
            if (!this.owner.isPerformingSkill()) {
                this.owner.setDeltaMovement(this.rollDirection.x * 0.42, this.owner.getDeltaMovement().y, this.rollDirection.z * 0.42);
                this.owner.hasImpulse = true;
            }
            if (elapsed % 3 == 0) this.barrel.rollTrail();
            if (elapsed % 8 == 0) this.barrel.hitPlayers(2.9, 8, 1.1);
        } else if (elapsed < 42) {
            if (elapsed % 2 == 0) this.barrel.warnSlam();
        } else {
            this.barrel.slam();
            this.rollStarted = -1;
        }
    }

    private void rats(ServerLevel level, LivingEntity target) {
        if (target == null || !due("swarm", this.owner.getHealth() < this.owner.getMaxHealth() * 0.5F ? 65 : 100)) return;
        List<RatBossEntity> rats = level.getEntitiesOfClass(RatBossEntity.class, this.owner.getBoundingBox().inflate(48),
                rat -> rat.isMinion() && rat.isAlive() && this.sacrifice.inheritedChild(rat));
        for (RatBossEntity rat : rats) rat.setTarget(target);
        int cap = this.owner.getHealth() < this.owner.getMaxHealth() * 0.5F ? 16 : 12;
        for (int index = rats.size(); index < Math.min(cap, rats.size() + 6); index++) {
            RatBossEntity rat = FTEntities.RAT_BOSS.get().create(level);
            if (rat == null) continue;
            rat.setMinion(true);
            summon(level, rat, target);
        }
        Fx.ring(level, this.owner.getX(), this.owner.getY() + 0.1, this.owner.getZ(), 3, 20, ParticleTypes.POOF, 0.02);
    }

    private boolean summon(ServerLevel level, Mob minion, LivingEntity target) {
        if (minion == null) return false;
        double angle = this.owner.getRandom().nextDouble() * Math.PI * 2;
        double radius = 2.5 + this.owner.getRandom().nextDouble() * 3.5;
        minion.setPos(this.owner.getX() + Math.cos(angle) * radius, this.owner.getY(), this.owner.getZ() + Math.sin(angle) * radius);
        minion.setPersistenceRequired();
        minion.setTarget(target);
        MinionBlessing.mark(minion, this.owner.getUUID());
        ManorFightMembers.inherit(this.owner, minion);
        for (MobEffectInstance effect : this.owner.getActiveEffects()) {
            if (effect.getEffect().isBeneficial() && !(minion instanceof RatBossEntity)) minion.addEffect(new MobEffectInstance(effect));
        }
        return level.addFreshEntity(minion);
    }

    private void grant(String id, int duration, int amplifier) {
        grant(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id)), duration, amplifier);
    }

    private void grant(MobEffect effect, int duration, int amplifier) {
        if (effect != null) this.sacrifice.grant(new MobEffectInstance(effect, duration, amplifier));
    }

    private static void debuff(LivingEntity target, String id, int duration, int amplifier) {
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id));
        if (effect != null) target.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }

    private void withFacing(LivingEntity target, Runnable action) {
        float previousYaw = this.owner.getYRot();
        Vec3 direction = target.position().subtract(this.owner.position());
        this.owner.setYRot((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        try {
            action.run();
        } finally {
            this.owner.setYRot(previousYaw);
        }
    }

    public void onMeleeHit(LivingEntity target, float dealt) {
        if (dealt <= 0) return;
        if (this.sacrifice.has("meat_mountain")) {
            int hits = this.sacrifice.state().getInt("butcher_hits");
            this.owner.heal(ButcherCombatRules.lifesteal(dealt, this.owner.getMaxHealth() - this.owner.getHealth()));
            ButcherHitEffects.apply(this.owner, target, ButcherCombatRules.empowered(hits));
            this.sacrifice.state().putInt("butcher_hits", hits + 1);
        }
        if (this.sacrifice.has("rat_boss") || this.sacrifice.has("rat_minion")) {
            if (target instanceof ServerPlayer player) BossHandler.applyRandomRatDebuff(player);
        }
        if (this.sacrifice.has("sugar_queen")) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60));
        }
    }

    public void reset() {
        pause();
    }

    public void pause() {
        this.rollStarted = -1;
        this.blastAt = -1;
        net.farmingtales.ftboss.integ.spells.LemonSpellBarrage.cancel(this.owner);
    }

    private void barrelBlast(ServerLevel level, LivingEntity target) {
        if (target == null || this.owner.distanceToSqr(target) > 81) {
            this.blastAt = -1;
            return;
        }
        if (this.blastAt < 0 && this.owner.distanceToSqr(target) <= 25 && due("barrel_blast", 100)) {
            this.blastAt = level.getGameTime() + 30;
        }
        if (this.blastAt < 0) return;
        if (this.owner.tickCount % 2 == 0) level.sendParticles(ParticleTypes.SMOKE, this.owner.getX(),
                this.owner.getY() + 0.8, this.owner.getZ(), 2, 0.2, 0.2, 0.2, 0.02);
        if (level.getGameTime() >= this.blastAt) {
            this.blastAt = -1;
            level.explode(this.owner, this.owner.getX(), this.owner.getY() + 0.4, this.owner.getZ(),
                    1.2F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        }
    }
}
