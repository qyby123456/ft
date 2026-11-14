const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const project = path.resolve(__dirname, '../..');
const output = path.resolve(process.argv[2] || path.join(project, 'build/manor-buff-copy-contract'));
const javaHome = process.env.JAVA_HOME;
if (!javaHome) throw new Error('Set JAVA_HOME to JDK 17');
const fixtures = {
  'net/minecraft/resources/ResourceLocation.java': `
    package net.minecraft.resources;
    public record ResourceLocation(String namespace, String path) {
      public String getNamespace() { return namespace; }
      public String getPath() { return path; }
    }
  `,
  'net/minecraft/world/entity/ai/attributes/Attributes.java': `
    package net.minecraft.world.entity.ai.attributes;
    public class Attributes {
      public static final Object MAX_HEALTH = new Object();
      public static final Object ATTACK_DAMAGE = new Object();
    }
  `,
  'net/minecraft/world/effect/MobEffect.java': `
    package net.minecraft.world.effect;
    import java.util.HashMap;
    import java.util.Map;
    public class MobEffect {
      private final boolean beneficial;
      private final Map<Object, Object> modifiers = new HashMap<>();
      public MobEffect(boolean beneficial) { this.beneficial = beneficial; }
      public boolean isBeneficial() { return beneficial; }
      public Map<Object, Object> getAttributeModifiers() { return modifiers; }
    }
  `,
  'net/minecraft/world/effect/MobEffects.java': `
    package net.minecraft.world.effect;
    public class MobEffects {
      public static final MobEffect ABSORPTION = new MobEffect(true);
      public static final MobEffect HEALTH_BOOST = new MobEffect(true);
    }
  `,
  'net/minecraft/world/effect/MobEffectInstance.java': `
    package net.minecraft.world.effect;
    public record MobEffectInstance(MobEffect effect, int duration, int amplifier,
                                    boolean ambient, boolean visible, boolean icon) {
      public MobEffect getEffect() { return effect; }
      public int getDuration() { return duration; }
      public int getAmplifier() { return amplifier; }
      public boolean isAmbient() { return ambient; }
      public boolean isVisible() { return visible; }
      public boolean showIcon() { return icon; }
    }
  `,
  'net/minecraftforge/registries/ForgeRegistries.java': `
    package net.minecraftforge.registries;
    import net.minecraft.world.effect.MobEffect;
    import net.minecraft.resources.ResourceLocation;
    import java.util.IdentityHashMap;
    public class ForgeRegistries {
      public static final Effects MOB_EFFECTS = new Effects();
      public static class Effects extends IdentityHashMap<MobEffect, ResourceLocation> {
        public ResourceLocation getKey(MobEffect effect) { return get(effect); }
      }
    }
  `,
  'ManorBuffCopyTest.java': `
    import net.farmingtales.ftboss.boss.ai.ManorBuffCopy;
    import net.minecraft.resources.ResourceLocation;
    import net.minecraft.world.effect.*;
    import net.minecraft.world.entity.ai.attributes.Attributes;
    import net.minecraftforge.registries.ForgeRegistries;
    public class ManorBuffCopyTest {
      private static int assertions;
      public static void main(String[] arguments) {
        check(ManorBuffCopy.DURATION_TICKS == 600, "30 seconds duration");
        check(ManorBuffCopy.COOLDOWN_TICKS == 300, "15 seconds cooldown");
        int previous = 0;
        int count = 0;
        for (int tick = 0; tick <= 3600; tick++) {
          if (!ManorBuffCopy.isCopyTick(tick)) continue;
          check(tick - previous == 300, "copy spacing");
          previous = tick;
          count++;
        }
        check(count == 12, "two copies per duration");
        check(!ManorBuffCopy.isCopyTick(0) && !ManorBuffCopy.isCopyTick(-300), "no premature copy");
        for (int tick : new int[]{100, 200, 299, 301, 599}) check(!ManorBuffCopy.isCopyTick(tick), "no old cadence");
        check(ManorBuffCopy.copyOf(null) == null, "null source");
        check(ManorBuffCopy.copyOf(instance(null, 100, 0)) == null, "null effect");
        check(ManorBuffCopy.copyOf(instance(new MobEffect(false), 100, 0)) == null, "non-beneficial excluded");
        check(ManorBuffCopy.copyOf(instance(MobEffects.HEALTH_BOOST, 100, 0)) == null, "opening health excluded");
        check(ManorBuffCopy.copyOf(instance(MobEffects.ABSORPTION, 100, 0)) == null, "absorption excluded");
        MobEffect customHealth = new MobEffect(true);
        customHealth.getAttributeModifiers().put(Attributes.MAX_HEALTH, new Object());
        check(ManorBuffCopy.copyOf(instance(customHealth, 100, 0)) == null, "modded health excluded");
        MobEffect renewal = new MobEffect(true);
        ForgeRegistries.MOB_EFFECTS.put(renewal, new ResourceLocation("herbalbrews", "renewal"));
        check(ManorBuffCopy.copyOf(instance(renewal, 100, 0)) == null, "renewal excluded");
        MobEffect strength = new MobEffect(true);
        strength.getAttributeModifiers().put(Attributes.ATTACK_DAMAGE, new Object());
        for (int duration : new int[]{1, 299, 600, 3600, -1}) {
          for (int amplifier : new int[]{0, 1, 4, 9}) {
            MobEffectInstance source = instance(strength, duration, amplifier);
            MobEffectInstance copied = ManorBuffCopy.copyOf(source);
            check(copied != null && copied != source, "new independent instance");
            check(copied.getDuration() == 600, "fixed copy duration");
            check(copied.getEffect() == strength && copied.getAmplifier() == amplifier, "preserve effect and level");
            check(copied.isAmbient() && !copied.isVisible() && copied.showIcon(), "preserve visual flags");
            check(source.getDuration() == duration, "player effect untouched");
          }
        }
        ForgeRegistries.MOB_EFFECTS.put(strength, new ResourceLocation("ftboss", "blood_rage_fake"));
        check(ManorBuffCopy.copyOf(instance(strength, 100, 4)) != null, "blood rage still copyable");
        ForgeRegistries.MOB_EFFECTS.put(strength, new ResourceLocation("othermod", "renewal"));
        check(ManorBuffCopy.copyOf(instance(strength, 100, 4)) != null, "exact namespace filter");
        System.out.println("ManorBuffCopyTest: " + assertions + " contract checks passed (stub effect registry)");
      }
      private static MobEffectInstance instance(MobEffect effect, int duration, int amplifier) {
        return new MobEffectInstance(effect, duration, amplifier, true, false, true);
      }
      private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
      }
    }
  `,
};

const sources = Object.entries(fixtures).map(([relative, source]) => {
  const file = path.join(output, 'fixtures', relative);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, source, 'utf8');
  return file;
});
sources.push(path.join(project, 'src/main/java/net/farmingtales/ftboss/boss/ai/ManorBuffCopy.java'));
const classes = path.join(output, 'classes');
fs.mkdirSync(classes, { recursive: true });
function run(executable, argumentsList) {
  const result = spawnSync(path.join(javaHome, 'bin', executable + (process.platform === 'win32' ? '.exe' : '')),
    argumentsList, { stdio: 'inherit', windowsHide: true });
  if (result.error) throw result.error;
  if (result.status !== 0) process.exit(result.status || 1);
}
run('javac', ['-encoding', 'UTF-8', '-d', classes, ...sources]);
run('java', ['-cp', classes, 'ManorBuffCopyTest']);
