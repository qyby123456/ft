const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const project = path.resolve(__dirname, '../..');
const output = path.resolve(process.argv[2] || path.join(project, 'build/manor-adaptive-contract'));
const javaHome = process.env.JAVA_HOME;
if (!javaHome) throw new Error('Set JAVA_HOME to JDK 17');
const fixtures = {
  'net/minecraft/world/level/Level.java': `
    package net.minecraft.world.level;
    public class Level { public boolean isClientSide; }
  `,
  'net/minecraft/world/entity/Entity.java': `
    package net.minecraft.world.entity;
    public class Entity { }
  `,
  'net/minecraft/world/entity/LivingEntity.java': `
    package net.minecraft.world.entity;
    import net.minecraft.world.damagesource.DamageSource;
    import net.minecraftforge.event.entity.living.LivingHurtEvent;
    import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
    public class LivingEntity extends Entity {
      public float lastDamage;
      public boolean hurt(DamageSource source, float amount) {
        LivingHurtEvent event = new LivingHurtEvent(this, source, amount);
        ManorAdaptiveDamage.onHurt(event);
        lastDamage = event.getAmount();
        return true;
      }
    }
  `,
  'net/minecraft/world/entity/player/Player.java': `
    package net.minecraft.world.entity.player;
    import net.minecraft.world.entity.LivingEntity;
    import net.minecraft.world.level.Level;
    import net.minecraft.world.effect.MobEffectInstance;
    import java.util.ArrayList;
    import java.util.List;
    public class Player extends LivingEntity {
      public final Level world = new Level();
      public final List<MobEffectInstance> effects = new ArrayList<>();
      public boolean creative, spectator;
      public boolean alive = true;
      public float maxHealth = 20;
      public Level level() { return world; }
      public boolean isAlive() { return alive; }
      public boolean isCreative() { return creative; }
      public boolean isSpectator() { return spectator; }
      public float getMaxHealth() { return maxHealth; }
      public List<MobEffectInstance> getActiveEffects() { return effects; }
    }
  `,
  'net/minecraft/world/entity/projectile/Projectile.java': `
    package net.minecraft.world.entity.projectile;
    import net.minecraft.world.entity.Entity;
    public class Projectile extends Entity {
      public Entity owner;
      public Projectile(Entity owner) { this.owner = owner; }
      public Entity getOwner() { return owner; }
    }
  `,
  'net/farmingtales/ftboss/entity/EasterBossEntity.java': `
    package net.farmingtales.ftboss.entity;
    import net.minecraft.world.entity.LivingEntity;
    public class EasterBossEntity extends LivingEntity {
      public int variant;
      public boolean mini;
      public EasterBossEntity(int variant) { this.variant = variant; }
      public int getVariant() { return variant; }
      public boolean isMini() { return mini; }
    }
  `,
  'net/minecraft/world/effect/MobEffect.java': `
    package net.minecraft.world.effect;
    public record MobEffect(boolean beneficial) {
      public boolean isBeneficial() { return beneficial; }
    }
  `,
  'net/minecraft/world/effect/MobEffectInstance.java': `
    package net.minecraft.world.effect;
    public record MobEffectInstance(MobEffect effect) {
      public MobEffect getEffect() { return effect; }
    }
  `,
  'net/minecraft/world/damagesource/DamageSource.java': `
    package net.minecraft.world.damagesource;
    import net.minecraft.world.entity.Entity;
    public record DamageSource(Entity owner, Entity direct, String type) {
      public DamageSource(Entity owner, Entity direct) { this(owner, direct, "mob_attack"); }
      public Entity getEntity() { return owner; }
      public Entity getDirectEntity() { return direct; }
    }
  `,
  'net/farmingtales/ftboss/skill/DoraSniperDamage.java': `
    package net.farmingtales.ftboss.skill;
    import net.farmingtales.ftboss.util.DoraSniperDamageRules;
    import net.minecraft.world.damagesource.DamageSource;
    public final class DoraSniperDamage {
      public static boolean isIndependent(DamageSource source) {
        return DoraSniperDamageRules.TABLE.stream().anyMatch(hit -> hit.type().equals(source.type()));
      }
    }
  `,
  'net/farmingtales/ftboss/boss/ManorMercy.java': `
    package net.farmingtales.ftboss.boss;
    import java.util.function.BooleanSupplier;
    import net.minecraft.world.entity.Entity;
    public final class ManorMercy {
      public static boolean withOwner(Entity owner, BooleanSupplier action) { return action.getAsBoolean(); }
    }
  `,
  'net/minecraftforge/event/entity/living/LivingHurtEvent.java': `
    package net.minecraftforge.event.entity.living;
    import net.minecraft.world.entity.LivingEntity;
    import net.minecraft.world.damagesource.DamageSource;
    public class LivingHurtEvent {
      private final LivingEntity entity;
      private final DamageSource source;
      private float amount;
      public boolean canceled;
      public LivingHurtEvent(LivingEntity entity, DamageSource source, float amount) {
        this.entity = entity; this.source = source; this.amount = amount;
      }
      public LivingEntity getEntity() { return entity; }
      public DamageSource getSource() { return source; }
      public float getAmount() { return amount; }
      public void setAmount(float amount) { this.amount = amount; }
      public boolean isCanceled() { return canceled; }
    }
  `,
  'net/minecraftforge/eventbus/api/EventPriority.java': `
    package net.minecraftforge.eventbus.api;
    public enum EventPriority { HIGH, NORMAL }
  `,
  'net/minecraftforge/eventbus/api/SubscribeEvent.java': `
    package net.minecraftforge.eventbus.api;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubscribeEvent { EventPriority priority() default EventPriority.NORMAL; }
  `,
};

const sources = Object.entries(fixtures).map(([relative, source]) => {
  const file = path.join(output, 'fixtures', relative);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, source, 'utf8');
  return file;
});
sources.push(path.join(project, 'src/main/java/net/farmingtales/ftboss/util/ManorFiveCombatRules.java'));
sources.push(path.join(project, 'src/main/java/net/farmingtales/ftboss/util/HiddenBossCastRules.java'));
sources.push(path.join(project, 'src/main/java/net/farmingtales/ftboss/util/ManorSkillDamageRules.java'));
sources.push(path.join(project, 'src/main/java/net/farmingtales/ftboss/util/DoraSniperDamageRules.java'));
sources.push(path.join(project, 'src/main/java/net/farmingtales/ftboss/boss/ai/ManorAdaptiveDamage.java'));
sources.push(path.join(__dirname, 'ManorAdaptiveDamageTest.java'));
const classes = path.join(output, 'classes');
fs.mkdirSync(classes, { recursive: true });
function run(executable, argumentsList) {
  const result = spawnSync(path.join(javaHome, 'bin', executable + (process.platform === 'win32' ? '.exe' : '')),
    argumentsList, { stdio: 'inherit', windowsHide: true });
  if (result.error) throw result.error;
  if (result.status !== 0) process.exit(result.status || 1);
}
run('javac', ['-encoding', 'UTF-8', '-d', classes, ...sources]);
run('java', ['-cp', classes, 'ManorAdaptiveDamageTest']);
