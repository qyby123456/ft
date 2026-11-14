package net.farmingtales.ftboss.client.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.WeaponFxPacket;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponFxVisual {
    private static final List<Effect> ACTIVE = new ArrayList<>();
    private static final EnumMap<Theme, ParticleOptions> COLORS = new EnumMap<>(Theme.class);
    private static ClientLevel activeLevel;
    private static int remainingBudget;
    private static int rotation;

    private static final class Effect {
        final WeaponFxPacket packet;
        int age;

        Effect(WeaponFxPacket packet) {
            this.packet = packet;
        }
    }

    static {
        for (Theme theme : Theme.values()) {
            COLORS.put(theme, new DustColorTransitionOptions(color(theme.highlight), color(theme.color), 0.9F));
        }
    }

    private WeaponFxVisual() {}

    private static Vector3f color(int rgb) {
        return new Vector3f((rgb >> 16 & 255) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F);
    }

    public static void receive(WeaponFxPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !packet.valid() || packet.kind().doraGun() || packet.kind().pufferBat()
                || !level.dimension().location().equals(packet.dimension())) return;
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
        if (packet.kind() == Kind.MIST || packet.kind() == Kind.RING) {
            ACTIVE.removeIf(effect -> effect.packet.kind() == packet.kind() && effect.packet.theme() == packet.theme()
                    && effect.packet.start().distanceToSqr(packet.start()) < 0.01);
        }
        if (packet.kind() == Kind.GOURMET_HIT) {
            level.playLocalSound(packet.start().x, packet.start().y, packet.start().z,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.38F, 1.16F, false);
        }
        if (ACTIVE.size() >= WeaponFxRules.MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(new Effect(packet));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) {
            ACTIVE.clear();
            activeLevel = minecraft.level;
            return;
        }
        if (minecraft.isPaused() || minecraft.player == null || ACTIVE.isEmpty()) return;
        int setting = minecraft.options.particles().get().ordinal();
        remainingBudget = WeaponFxRules.budget(setting);
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        int size = ACTIVE.size();
        int first = Math.floorMod(rotation++, size);
        for (int index = 0; index < size; index++) {
            Effect effect = ACTIVE.get((first + index) % size);
            WeaponFxPacket packet = effect.packet;
            Vec3 segment = packet.end().subtract(packet.start());
            double along = segment.lengthSqr() < 1.0E-8 ? 0
                    : Math.max(0, Math.min(1, camera.subtract(packet.start()).dot(segment) / segment.lengthSqr()));
            double distance = Math.max(0, camera.distanceTo(packet.start().add(segment.scale(along))) - packet.radius());
            int requested = packet.kind() == Kind.GOURMET_HIT ? 18
                    : packet.kind() == Kind.HIT || packet.kind() == Kind.TRACER ? 12 : 28;
            int samples = WeaponFxRules.samples(requested, setting, distance * distance);
            if (samples > 0 && remainingBudget > 0) emit(minecraft, effect, samples);
            effect.age++;
        }
        ACTIVE.removeIf(effect -> effect.age >= effect.packet.kind().ticks);
    }

    private static void emit(Minecraft minecraft, Effect effect, int samples) {
        WeaponFxPacket packet = effect.packet;
        Vec3 center = packet.start();
        int age = effect.age;
        Theme theme = packet.theme();
        ParticleOptions dust = COLORS.get(theme);
        if (packet.kind() == Kind.BEAM || packet.kind() == Kind.TRACER) {
            if (age != 0 && age != 3) return;
            Vec3 direction = packet.end().subtract(center).normalize();
            Vec3 side = direction.cross(Math.abs(direction.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize();
            Vec3 up = side.cross(direction).normalize();
            for (int index = 1; index <= samples; index++) {
                double progress = index / (double) samples;
                double angle = progress * Math.PI * 6;
                Vec3 point = center.lerp(packet.end(), progress);
                if (age == 3) point = point.add(side.scale(Math.cos(angle) * 0.075)).add(up.scale(Math.sin(angle) * 0.075));
                particle(minecraft, dust, point, Vec3.ZERO, packet.kind() == Kind.TRACER ? 3 : age == 0 ? 7 : 4);
                if (age == 0 && index % 7 == 0) particle(minecraft, accent(theme), point, Vec3.ZERO, 5);
            }
            if (age == 0 && packet.kind() != Kind.TRACER) spark(minecraft, packet.end(), theme, 4);
            return;
        }
        if (packet.kind() == Kind.GOURMET_HIT) {
            emitGourmetHit(minecraft, packet, age, samples, dust);
            return;
        }
        if (packet.kind() == Kind.HIT) {
            if (age != 0 && age != 2) return;
            Vec3 side = new Vec3(1, 0, 0).yRot((float) -Math.toRadians(packet.yaw()));
            for (int index = 0; index < samples; index++) {
                double progress = index / (double) (samples - 1);
                double offset = (progress - 0.5) * packet.radius() * 2;
                Vec3 point = center.add(side.scale(offset)).add(0, offset * (age == 0 ? 0.65 : -0.65), 0);
                particle(minecraft, dust, point, new Vec3(0, 0.006, 0), 6);
            }
            if (age == 0) spark(minecraft, center, theme, 3);
            return;
        }
        if (packet.kind() == Kind.MIST) {
            if (age % 2 != 0) return;
            if (age == 0) {
                for (int index = 0; index < samples; index++) {
                    double angle = index * Math.PI * 2 / samples;
                    Vec3 point = center.add(Math.cos(angle) * packet.radius(), 0.08, Math.sin(angle) * packet.radius());
                    particle(minecraft, dust, point, Vec3.ZERO, 10);
                }
            }
            int count = Math.max(3, samples / 4);
            for (int index = 0; index < count; index++) {
                double angle = index * Math.PI * 2 / count + age * 0.31;
                double radius = packet.radius() * (0.25 + 0.65 * minecraft.level.random.nextDouble());
                Vec3 point = center.add(Math.cos(angle) * radius, 0.12 + (age % 4) * 0.12, Math.sin(angle) * radius);
                particle(minecraft, dust, point, new Vec3(0, 0.012, 0), 10);
                if (index == 0) particle(minecraft, ParticleTypes.BUBBLE_POP, point, new Vec3(0, 0.02, 0), 8);
            }
            return;
        }
        if (age % 3 != 0) return;
        double progress = WeaponFxRules.ease(Math.min(1, age / (double) (packet.kind().ticks - 3)));
        double radius = packet.radius() * (packet.kind() == Kind.BLAST ? 0.2 + 0.8 * progress : 1);
        for (int index = 0; index < samples; index++) {
            double angle;
            if (packet.kind() == Kind.SWEEP) {
                angle = WeaponFxRules.sweepAngle(packet.yaw(), progress, index / (samples - 1.0));
            } else {
                angle = Math.PI * 2 * index / samples + age * 0.055;
            }
            Vec3 point = center.add(Math.cos(angle) * radius, 0.06 + Math.sin(angle * 3) * 0.035, Math.sin(angle) * radius);
            if (packet.kind() == Kind.SWEEP) {
                point = minecraft.level.clip(new ClipContext(center, point, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, minecraft.player)).getLocation();
            }
            particle(minecraft, dust, point, new Vec3(0, 0.005, 0), 7);
            if (index % 8 == 0 && age == 0) particle(minecraft, accent(theme), point, new Vec3(0, 0.025, 0), 8);
        }
        if (packet.kind() == Kind.BLAST && age == 0) spark(minecraft, center.add(0, 0.2, 0), theme, 6);
    }

    /** Two curved, opposing cuts followed by a compact royal-purple/antique-gold seal. */
    private static void emitGourmetHit(Minecraft minecraft, WeaponFxPacket packet, int age,
                                       int samples, ParticleOptions dust) {
        Vec3 center = packet.start();
        Vec3 side = new Vec3(1, 0, 0).yRot((float) -Math.toRadians(packet.yaw()));
        Vec3 depth = new Vec3(-side.z, 0, side.x);
        if (age == 4) {
            int count = Math.max(6, samples / 2);
            double radius = packet.radius() * 0.38;
            for (int index = 0; index < count; index++) {
                double angle = Math.PI * 2 * index / count;
                Vec3 point = center.add(side.scale(Math.cos(angle) * radius))
                        .add(0, Math.sin(angle) * radius * 0.72, 0);
                particle(minecraft, dust, point, depth.scale(0.004), 5);
                if (index % 3 == 0) particle(minecraft, ParticleTypes.END_ROD, point,
                        new Vec3(0, 0.012, 0), 5);
            }
            spark(minecraft, center, Theme.GOURMET, 5);
            return;
        }
        if (age > 3) return;

        boolean rising = age < 2;
        double echo = (age & 1) == 0 ? 0 : 0.055;
        for (int index = 0; index < samples; index++) {
            double progress = index / (double) Math.max(1, samples - 1);
            double offset = (progress - 0.5) * packet.radius() * 2;
            double arch = Math.sin(progress * Math.PI) * packet.radius() * 0.18;
            double height = offset * (rising ? 0.72 : -0.72) + arch - echo;
            Vec3 point = center.add(side.scale(offset)).add(0, height, 0)
                    .add(depth.scale((progress - 0.5) * echo));
            particle(minecraft, dust, point, new Vec3(0, 0.007, 0), (age & 1) == 0 ? 7 : 5);
        }
        if ((age & 1) == 0) {
            spark(minecraft, center.add(0, packet.radius() * 0.08, 0), Theme.GOURMET, 4);
        }
    }

    private static ParticleOptions accent(Theme theme) {
        return switch (theme) {
            case FROST -> ParticleTypes.SNOWFLAKE;
            case BLOOD -> ParticleTypes.CRIT;
            case NATURE, BLESSING -> ParticleTypes.HAPPY_VILLAGER;
            case WINE, TIDE, COMPOST -> ParticleTypes.BUBBLE_POP;
            case ARC, GOLD -> ParticleTypes.ELECTRIC_SPARK;
            case GOURMET -> ParticleTypes.END_ROD;
            case SHADOW -> ParticleTypes.REVERSE_PORTAL;
            default -> ParticleTypes.END_ROD;
        };
    }

    private static void spark(Minecraft minecraft, Vec3 center, Theme theme, int count) {
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2 * index / count;
            Vec3 velocity = new Vec3(Math.cos(angle) * 0.07, 0.035 + index * 0.006, Math.sin(angle) * 0.07);
            particle(minecraft, accent(theme), center, velocity, 9);
        }
    }

    private static void particle(Minecraft minecraft, ParticleOptions options, Vec3 point, Vec3 velocity, int lifetime) {
        if (remainingBudget <= 0 || !minecraft.level.hasChunkAt(BlockPos.containing(point))
                || minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(point) < 0.16) return;
        remainingBudget--;
        Particle particle = minecraft.particleEngine.createParticle(options, point.x, point.y, point.z, velocity.x, velocity.y, velocity.z);
        if (particle != null) particle.setLifetime(Math.min(lifetime, WeaponFxRules.MAX_PARTICLE_LIFETIME));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == activeLevel) {
            ACTIVE.clear();
            activeLevel = null;
        }
    }
}
