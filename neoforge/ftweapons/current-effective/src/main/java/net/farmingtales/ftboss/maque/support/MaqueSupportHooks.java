package net.farmingtales.ftboss.maque.support;

import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class MaqueSupportHooks {
    private MaqueSupportHooks() {
    }

    public static void arrival(Entity var0) {
        MaqueSupportPacket.send(var0, var0, 0);
    }

    public static void departure(Entity var0) {
        MaqueSupportPacket.send(var0, var0, 1);
    }

    public static void intercept(Entity var0, Entity var1, float var2) {
        if (var2 > 0.0F && Float.isFinite(var2)) {
            MaqueSupportPacket.send(var1, var0, 6);
        }
    }

    public static void heal(LivingEntity var0, float var1, Entity var2, int var3) {
        float var4 = var0.getHealth();
        var0.heal(var1);
        if (var0.getHealth() > var4) {
            MaqueSupportPacket.send((Entity)(var2 == null ? var0 : var2), var0, var3);
        }
    }

    public static boolean empower(LivingEntity var0, MobEffectInstance var1, Entity var2, int var3) {
        boolean var4 = var0.addEffect(var1);
        if (var4 && var1.getEffect().value().isBeneficial()) {
            MaqueSupportPacket.send((Entity)(var2 == null ? var0 : var2), var0, var3);
        }

        return var4;
    }

    public static boolean cleanse(LivingEntity var0, MobEffect var1, Entity var2) {
        boolean var3 = var0.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(var1));
        if (var3 && !var1.isBeneficial()) {
            MaqueSupportPacket.send((Entity)(var2 == null ? var0 : var2), var0, 4);
        }

        return var3;
    }

    public static boolean copy(LivingEntity var0, MobEffectInstance var1, Entity var2) {
        boolean var3 = var0.addEffect(var1);
        if (var3 && var0 instanceof EasterBossEntity var4 && var4.getVariant() == 9 && var1.getEffect().value().isBeneficial()) {
            MaqueSupportPacket.send(var2, var0, 5);
        }

        return var3;
    }

    public static void healLock(ServerPlayer var0, Entity var1) {
        HealBan.ban(var0);
        MaqueSupportPacket.send(var1, var0, 11);
    }
}
