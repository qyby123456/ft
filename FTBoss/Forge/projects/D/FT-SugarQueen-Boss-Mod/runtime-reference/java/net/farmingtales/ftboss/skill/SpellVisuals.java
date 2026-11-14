/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.dandaniron.IronHooks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.youzi.facing.YouziFacing;
import net.farmingtales.ftboss.youzi.spells.YouziSpellNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;

public final class SpellVisuals {
    private SpellVisuals() {
    }

    public static double radiusScale(Entity owner, String spellId) {
        EasterBossEntity boss;
        if (spellId.equals("star_cross") || spellId.equals("rose_starfall")) {
            return 1.0;
        }
        if (!(owner instanceof EasterBossEntity) || (boss = (EasterBossEntity)owner).isMini() || boss.isGiant() || ManorSpellRules.pair(boss.getVariant()).stream().noneMatch(kind -> kind.id.equals(spellId))) {
            return 1.0;
        }
        double baseRadius = switch (spellId) {
            case "orbital_gears" -> 3.0;
            case "pressure_lance" -> 0.6;
            case "magnetic_storm" -> 4.0;
            default -> 0.0;
        };
        return baseRadius > 0.0 ? ManorBossCombatRules.spellRadius(boss.getVariant(), baseRadius) / baseRadius : ManorBossCombatRules.radiusScale(boss.getVariant());
    }

    public static void show(ServerLevel level, LivingEntity caster, String id, SpellVisualRules.Phase phase, Vec3 point) {
        SpellVisualRules.Spell spell = SpellVisualRules.find(id);
        if (spell == null) {
            return;
        }
        Vec3 anchor = spell.attached ? caster.m_20182_() : SpellVisuals.ground(level, caster, point);
        Vec3 direction = point.m_82546_(caster.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        if (direction.m_82556_() < 0.01) {
            direction = caster.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_();
        }
        if (spell == SpellVisualRules.Spell.STAR_CROSS || spell == SpellVisualRules.Spell.ROSE_STARFALL) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        SpellVisuals.send(level, new SpellVisualPacket(level.m_46472_().m_135782_(), caster.m_19879_(), caster.m_20148_(), spell, phase, anchor, anchor.m_82549_(YouziFacing.direction(direction, caster, id))));
    }

    public static void beam(ServerLevel level, LivingEntity caster, String id, Vec3 start, Vec3 end) {
        SpellVisualRules.Spell spell = SpellVisualRules.find(id);
        if (spell != null) {
            SpellVisuals.send(level, new SpellVisualPacket(level.m_46472_().m_135782_(), caster.m_19879_(), caster.m_20148_(), spell, SpellVisualRules.Phase.BEAM, start, end));
        }
    }

    private static Vec3 ground(ServerLevel level, LivingEntity caster, Vec3 point) {
        if (!(SpellVisualRules.validPoint(point.f_82479_) && SpellVisualRules.validPoint(point.f_82480_) && SpellVisualRules.validPoint(point.f_82481_))) {
            return point;
        }
        BlockHitResult hit = level.m_45547_(new ClipContext(point.m_82520_(0.0, 0.1, 0.0), point.m_82520_(0.0, -6.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster));
        return hit.m_6662_() == HitResult.Type.MISS ? point : hit.m_82450_();
    }

    /*
     * WARNING - void declaration
     */
    public static void send(ServerLevel serverLevel, SpellVisualPacket spellVisualPacket) {
        ServerLevel level;
        void packet;
        YouziSpellNetwork.emit(serverLevel, spellVisualPacket);
        IronHooks.observe(serverLevel, spellVisualPacket);
        if (!packet.valid()) {
            return;
        }
        Vec3 segment = packet.end().m_82546_(packet.start());
        for (ServerPlayer player : level.m_6907_()) {
            double along;
            if (player instanceof FakePlayer || player.f_8906_ == null) continue;
            double d = along = segment.m_82556_() < 0.001 ? 0.0 : Math.max(0.0, Math.min(1.0, player.m_20182_().m_82546_(packet.start()).m_82526_(segment) / segment.m_82556_()));
            if (player.m_20182_().m_82557_(packet.start().m_82549_(segment.m_82490_(along))) > 4096.0 || !FTNetwork.CHANNEL.isRemotePresent(player.f_8906_.f_9742_)) continue;
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
        }
    }
}

