package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorDamageRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.farmingtales.ftboss.event.NativeManorDamageEvents;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeHealth;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ManorFinalDamage {
    private ManorFinalDamage() {
    }

    public static float legacyGate(LivingEntity var0, DamageSource var1, float var2) {
        if (var0 instanceof EasterBossEntity var3 && !var3.isMini() && (ManorFight.isManaged(var3) || var3.getVariant() == 9)) {
            return var2;
        }

        return BossDamageGate.gate(var0, var1, var2);
    }

    public static float[] settle(EasterBossEntity var0, DamageSource var1, float var2) {
        float var3 = NativeManorDamageEvents.onLivingHurt(var0, var1, var2);
        if (Float.isFinite(var3) && !(var3 <= 0.0F)) {
            float var4 = ((NativeManorFinalDamageAccess)var0).ftboss$reduceManorDamage(var1, var3);
            var4 = ManorSpellRules.cap(var0, var1, var4);
            if (Float.isFinite(var4) && !(var4 <= 0.0F)) {
                EasterBossEntity var5 = ((NativeManorFinalDamageAccess)var0).ftboss$manorSparrow();
                boolean var6 = !var0.isMini() && var0.getVariant() != 9 && var5 != null && !((NativeManorFinalDamageAccess)var5).ftboss$blocksManorDamage();
                boolean var7 = false;
                if (!var0.isMini() && var0.getVariant() == 0 && var0.level() instanceof ServerLevel var8) {
                    for (EasterBossEntity var10 : var8.getEntitiesOfClass(EasterBossEntity.class, var0.getBoundingBox().inflate(64.0))) {
                        if (var10 != var0
                            && var10.isMini()
                            && var10.getVariant() == 0
                            && var10.isAlive()
                            && !var10.isDeadOrDying()
                            && !((NativeManorFinalDamageAccess)var10).ftboss$blocksManorDamage()) {
                            var7 = true;
                            break;
                        }
                    }
                }

                float var15 = ManorDamageRules.remainingDamage(var4, var6, var7);
                if (var0.getVariant() == 5 && !var0.isMini()) {
                    var15 = (float)((double)var15 * 0.7);
                }

                var15 = ManorAdaptiveShield.reduce(var0, var1, var15);
                var15 = ManorDefense.reduce(var0, var1, var15);
                float var19 = Math.max(0.0F, var0.getAbsorptionAmount());
                float var20 = Math.min(var19, var15);
                float var11 = Math.max(0.0F, var15 - var20);
                BeforeHealth var12 = NativeManorDamageEvents.damageEvent(var0, var1, var11);
                if (NativeManorDamageEvents.post(var12)) {
                    return null;
                } else {
                    float var13 = var12.getAmount();
                    if (!ManorDamageRules.ignored(var13) && (!(var11 > 0.0F) || !(var13 <= 0.0F))) {
                        ((NativeManorFinalDamageAccess)var0).ftboss$shareManorDamage(var4, var1);
                        if (var20 > 0.0F) {
                            var0.setAbsorptionAmount(var19 - var20);
                        }

                        return new float[]{Math.max(0.0F, var13), var20};
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onFinalDamage(BeforeHealth var0) {
    }
}
