package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;

import net.farmingtales.ftboss.effect.ArmorShredEffect;
import net.farmingtales.ftboss.effect.BloodRageFakeEffect;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.skill.XuanlanCombat;
import net.farmingtales.ftboss.util.CleaverSlamFx;
import net.farmingtales.ftboss.util.ManorWeaponRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CleaverCombat {
    private record Cast(ServerLevel level, ItemStack weapon, Vec3 origin, Vec3 direction, long impact, int rage) {}
    private static final Map<UUID, Cast> CASTS = new HashMap<>();

    private CleaverCombat() {}

    public static InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator()
                || player.getCooldowns().isOnCooldown(weapon.getItem())) return InteractionResultHolder.fail(weapon);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.success(weapon);
        if (CASTS.containsKey(player.getUUID())) return InteractionResultHolder.fail(weapon);
        var rage = player.getEffect(FTEffects.BLOOD_RAGE_FAKE.get());
        int stacks = rage == null ? 0 : rage.getAmplifier() + 1;
        Vec3 direction = Vec3.directionFromRotation(0, player.getYRot());
        CASTS.put(player.getUUID(), new Cast(serverPlayer.serverLevel(), weapon, player.position(), direction,
                level.getGameTime() + ManorWeaponRules.CLEAVER_IMPACT_TICKS, stacks));
        player.getCooldowns().addCooldown(weapon.getItem(), ManorWeaponRules.CLEAVER_COOLDOWN_TICKS);
        ((BlackCleaverItem) weapon.getItem()).triggerCleaverAnim(player, weapon, "slam");
        level.playSound(null, player.blockPosition(), SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.PLAYERS, 0.7F, 0.55F);
        return InteractionResultHolder.consume(weapon);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Cast cast = CASTS.get(player.getUUID());
        if (cast == null) return;
        if (!player.isAlive() || player.isSpectator() || player.serverLevel() != cast.level()
                || player.getMainHandItem() != cast.weapon() || !cast.weapon().is(FTBossItems.BLACK_CLEAVER.get())
                || player.position().distanceToSqr(cast.origin()) > 16) {
            CASTS.remove(player.getUUID());
            return;
        }
        long remaining = cast.impact() - cast.level().getGameTime();
        if (remaining > 0) {
            if (remaining % 2 == 0) cast.level().sendParticles(ParticleTypes.CRIMSON_SPORE,
                    player.getX() + cast.direction().x, player.getY() + 1.2, player.getZ() + cast.direction().z,
                    3, 0.15, 0.2, 0.15, 0.01);
            return;
        }
        CASTS.remove(player.getUUID());
        impact(player, cast);
    }

    private static void impact(ServerPlayer player, Cast cast) {
        Vec3 start = player.position().add(0, 0.6, 0);
        Vec3 center = XuanlanCombat.clip(cast.level(), player, start, start.add(cast.direction().scale(1.8)));
        double radius = ManorWeaponRules.cleaverRadius(cast.rage());
        int hits = 0;
        int stacksBefore = cast.rage();
        var targets = cast.level().getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius, 2.5, radius),
                target -> XuanlanCombat.canHit(player, target));
        targets.sort(Comparator.comparingDouble(target -> target.distanceToSqr(center)));
        for (LivingEntity target : targets) {
            Vec3 delta = target.position().subtract(center);
            if (!ManorWeaponRules.insideSlam(delta.x, delta.y, delta.z, cast.rage()) || !player.hasLineOfSight(target)) continue;
            /* ★★★ 2026-09-24 修「黑切右键攻击挂不上伪血怒」★★★
               裂地斩过去是直接 `target.hurt(...)`。而玩家平砍（左键）刚刚把目标的
               `invulnerableTime` 顶到 20 tick —— 落点撞进无敌帧时，{@code LivingEntity.hurt}
               只结算"超出上一次伤害的那部分"，并且**返回 false**：
               于是 `hits` 不加，下面 `if (hits > 0)` 里"叠/消耗【血怒·伪】+ 破甲 + 减速 + 击退"
               **整块被跳过**。玩家看到的就是"右键打中了，但血怒·伪不涨"（左键平砍却正常涨）。
               这里与 {@code WeaponHitEffects}（v60/v151 那条口径）保持一致：**先清零无敌帧**，
               让这一下必中；再用"hurt 返回 true 或血量真的掉了"来判定命中，
               免得被某些模组的免疫逻辑再次吞掉。 */
            target.invulnerableTime = 0;
            float hpBefore = target.getHealth();
            boolean dealt = target.hurt(cast.level().damageSources().indirectMagic(player, player),
                    ManorWeaponRules.cleaverDamage(cast.rage()));
            float real = hpBefore - target.getHealth();
            if (!dealt && real <= 0.0F) continue;
            hits++;
            ArmorShredEffect.onHit(target);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, cast.rage() >= 5 ? 2 : 0));
            target.knockback(cast.rage() >= 5 ? 0.9 : 0.6, -delta.x, -delta.z);
            if (hits >= 16) break;
        }
        if (hits > 0) {
            if (cast.rage() >= 5) player.removeEffect(FTEffects.BLOOD_RAGE_FAKE.get());
            else BloodRageFakeEffect.onHit(player);
            /* 每次裂地斩一行日志（用户报"右键挂不上血怒"时，一眼就能看出是没命中、
               还是命中了但层数没变）——与 WeaponHitEffects 的命中日志同一个口径。 */
            var inst = player.getEffect(FTEffects.BLOOD_RAGE_FAKE.get());
            int stacksAfter = (inst == null) ? 0 : inst.getAmplifier() + 1;
            FTBossMod.LOGGER.info("[FTBoss][黑切] {} 裂地斩命中 {} 个目标 -> 血怒·伪 {} 层 -> {} 层{}",
                    player.getName().getString(), hits, stacksBefore, stacksAfter,
                    stacksBefore >= 5 ? "（满层已消耗）" : "");
        }
        CleaverSlamFx.play(cast.level(), center.x, player.getY(), center.z);
        WeaponFx.ring(cast.level(), center, radius, Theme.BLOOD);
        cast.weapon().hurtAndBreak(2, player, holder -> holder.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        CASTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        CASTS.clear();
    }
}
