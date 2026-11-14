package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;

public final class SpellVisuals {
    private SpellVisuals() {}

    public static double playerRadiusScale(net.minecraft.world.entity.Entity owner) {
        return net.farmingtales.ftboss.util.ManorSpellAreaRules.scale(owner instanceof net.minecraft.world.entity.player.Player);
    }

    public static double radiusScale(net.minecraft.world.entity.Entity owner, String spellId) {
        if (owner instanceof net.minecraft.world.entity.player.Player) {
            for (net.farmingtales.ftboss.util.ManorSpellRules.Kind kind : net.farmingtales.ftboss.util.ManorSpellRules.Kind.values())
                if (kind.id.equals(spellId)) return playerRadiusScale(owner);
        }
        if (spellId.equals("star_cross") || spellId.equals("rose_starfall")) return 1;
        if (!(owner instanceof net.farmingtales.ftboss.entity.EasterBossEntity boss) || boss.isMini() || boss.isGiant()
                || net.farmingtales.ftboss.util.ManorSpellRules.pair(boss.getVariant()).stream()
                .noneMatch(kind -> kind.id.equals(spellId))) return 1.0;
        double baseRadius = switch (spellId) {
            case "orbital_gears" -> 3.0;
            case "pressure_lance" -> 0.6;
            case "magnetic_storm" -> 4.0;
            default -> 0.0;
        };
        return baseRadius > 0
                ? net.farmingtales.ftboss.util.ManorBossCombatRules.spellRadius(boss.getVariant(), baseRadius) / baseRadius
                : net.farmingtales.ftboss.util.ManorBossCombatRules.radiusScale(boss.getVariant());
    }

    public static void show(ServerLevel level, LivingEntity caster, String id, Phase phase, Vec3 point) {
        Spell spell = SpellVisualRules.find(id);
        if (spell == null) return;
        Vec3 anchor = spell.attached ? caster.position() : ground(level, caster, point);
        Vec3 direction = point.subtract(caster.position()).multiply(1, 0, 1).normalize();
        if (direction.lengthSqr() < 0.01) direction = caster.getLookAngle().multiply(1, 0, 1).normalize();
        if (spell == Spell.STAR_CROSS || spell == Spell.ROSE_STARFALL) direction = new Vec3(0, 0, 1);
        send(level, new SpellVisualPacket(level.dimension().location(), caster.getId(), caster.getUUID(),
                spell, phase, anchor, anchor.add(direction)));
    }

    public static void beam(ServerLevel level, LivingEntity caster, String id, Vec3 start, Vec3 end) {
        Spell spell = SpellVisualRules.find(id);
        if (spell != null) send(level, new SpellVisualPacket(level.dimension().location(), caster.getId(),
                caster.getUUID(), spell, Phase.BEAM, start, end));
    }

    private static Vec3 ground(ServerLevel level, LivingEntity caster, Vec3 point) {
        if (!SpellVisualRules.validPoint(point.x) || !SpellVisualRules.validPoint(point.y)
                || !SpellVisualRules.validPoint(point.z)) return point;
        HitResult hit = level.clip(new ClipContext(point.add(0, 0.1, 0), point.add(0, -6, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.MISS ? point : hit.getLocation();
    }

    public static void send(ServerLevel level, SpellVisualPacket packet) {
        if (!packet.valid()) return;
        Vec3 segment = packet.end().subtract(packet.start());
        for (ServerPlayer player : level.players()) {
            if (player instanceof FakePlayer || player.connection == null) continue;
            double along = segment.lengthSqr() < 0.001 ? 0 : Math.max(0, Math.min(1,
                    player.position().subtract(packet.start()).dot(segment) / segment.lengthSqr()));
            if (player.position().distanceToSqr(packet.start().add(segment.scale(along))) > SpellVisualRules.VIEW_RANGE * SpellVisualRules.VIEW_RANGE
                    || !FTNetwork.CHANNEL.isRemotePresent(player.connection.connection)) continue;
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
