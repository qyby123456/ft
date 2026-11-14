package net.farmingtales.ftboss.balance;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ShadowSigilPacket;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.farmingtales.ftboss.zheergenfx.LightHooks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ManorShadowBlink {
    private static final Map<EasterBossEntity, Long> LAST_BLINK = new WeakHashMap<>();

    private ManorShadowBlink() {
    }

    public static void tick(EasterBossEntity boss) {
        if (boss.level() instanceof ServerLevel level
            && boss.getTarget() instanceof ServerPlayer target
            && ManorBalanceHooks.validPlayer(boss, target)
            && boss.canTeleportWith(target)) {
            long now = level.getGameTime();
            if (!ManorBalanceRules.blinkReady(now, LAST_BLINK.getOrDefault(boss, Long.MIN_VALUE))) {
                return;
            }

            double distance = boss.distanceToSqr(target);
            if (!(distance < 25.0) && !(distance > 1024.0)) {
                Vec3 behind = target.getLookAngle().multiply(-1.0, 0.0, -1.0).normalize().scale(2.2);
                if (behind.lengthSqr() < 0.001) {
                    behind = new Vec3(0.0, 0.0, -2.2);
                }

                if (LightHooks.teleport(boss, target.getX() + behind.x, target.getY(), target.getZ() + behind.z, true)) {
                    LAST_BLINK.put(boss, now);
                    boss.getLookControl().setLookAt(target, 60.0F, 60.0F);
                } else {
                    LAST_BLINK.put(boss, now - 30L + 10L);
                }

                return;
            }

            return;
        }
    }

    public static boolean landed(boolean moved, EasterBossEntity boss) {
        if (moved && boss.getVariant() == 7 && ManorBalanceHooks.mainBoss(boss) && boss.isAlive() && boss.level() instanceof ServerLevel level) {
            LAST_BLINK.put(boss, level.getGameTime());
            Vec3 var11 = boss.position();

            for (ServerPlayer player : level.players()) {
                if (ManorBalanceHooks.validPlayer(boss, player) && boss.canTeleportWith(player)) {
                    double deltaX = player.getX() - var11.x;
                    double deltaZ = player.getZ() - var11.z;
                    if (!(deltaX * deltaX + deltaZ * deltaZ > 9.0) && !(Math.abs(player.getY() - var11.y) > 2.5)) {
                        player.hurt(level.damageSources().indirectMagic(boss, boss), ShadowSwordItem.skyDamage());
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                        Vec3 away = new Vec3(deltaX, 0.0, deltaZ);
                        away = away.lengthSqr() < 1.0E-4 ? boss.getLookAngle().multiply(1.0, 0.0, 1.0).normalize() : away.normalize();
                        player.push(away.x * 1.4, 0.35, away.z * 1.4);
                        player.hurtMarked = true;
                    }
                }
            }

            ShadowSigilPacket packet = new ShadowSigilPacket(var11.x, var11.y, var11.z, true);

            for (ServerPlayer viewer : level.players()) {
                if (viewer.distanceToSqr(var11) <= 4096.0) {
                    PacketDistributor.sendToPlayer(viewer, packet);
                }
            }

            WeaponFx.burst(level, var11.add(0.0, 0.1, 0.0), 3.0, Theme.SHADOW);
            level.sendParticles(ParticleTypes.SQUID_INK, var11.x, var11.y + 0.5, var11.z, 10, 0.7, 0.7, 0.7, 0.08);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, var11.x, var11.y + 1.2, var11.z, 12, 0.6, 0.6, 0.6, 0.12);
            level.sendParticles(ParticleTypes.CLOUD, var11.x, var11.y + 0.2, var11.z, 6, 1.0, 1.0, 1.0, 0.06);
            level.sendParticles(ParticleTypes.FLASH, var11.x, var11.y + 0.6, var11.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, boss.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.9F);
            level.playSound(null, boss.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.1F, 0.55F);
            level.playSound(null, boss.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.7F, 1.35F);
            return moved;
        } else {
            return moved;
        }
    }

    public static void clear() {
        LAST_BLINK.clear();
    }
}
