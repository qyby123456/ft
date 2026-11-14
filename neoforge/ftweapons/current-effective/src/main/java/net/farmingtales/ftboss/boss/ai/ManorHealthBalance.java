package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSupportRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorHealthBalance {
    private static final String SUPPORT = "ftboss_sparrow_support_role";
    private static final String APPLIED = "ftboss_manor_health_balance";
    private static final String SCALING = "ftboss_base_hp";
    private static final String IMPOSSIBLE_READY = "ftboss_impossible_sparrow_health";

    private ManorHealthBalance() {
    }

    public static float supportHealth(EasterBossEntity var0) {
        var0.getPersistentData().putBoolean("ftboss_sparrow_support_role", true);
        normalize(var0);
        return 500.0F;
    }

    public static boolean support(EasterBossEntity var0) {
        CompoundTag var1 = var0.getPersistentData();
        AttributeInstance var2 = var0.getAttribute(Attributes.MAX_HEALTH);
        return var0.getVariant() == 9
            && (
                var1.getBoolean("ftboss_sparrow_support_role")
                    || var1.getBoolean("ftboss_impossible_sparrow_health")
                    || ManorFightMembers.ownerId(var0) != null
                    || var2 != null && var2.getBaseValue() <= 250.0
            );
    }

    public static void prepareImpossible(EasterBossEntity var0) {
        var0.setChallengeDifficulty(ManorDifficulty.IMPOSSIBLE);
        supportHealth(var0);
        normalize(var0);
        var0.getPersistentData().putBoolean("ftboss_impossible_sparrow_health", true);
    }

    public static void normalize(LivingEntity var0) {
        if (var0 instanceof EasterBossEntity var1
            && !var0.level().isClientSide
            && var0.isAlive()
            && !var1.isMini()
            && !var1.isGiant()
            && (EasterBossEntity.isManorVariant(var1.getVariant()) || var1.getVariant() == 9)) {
            AttributeInstance var2 = var1.getAttribute(Attributes.MAX_HEALTH);
            if (var2 == null) {
                return;
            }

            CompoundTag var3 = var1.getPersistentData();
            boolean var4 = support(var1);
            if (var4) {
                var3.putBoolean("ftboss_sparrow_support_role", true);
            }

            double var5 = var4 ? 500.0 : 1500.0;
            if (var3.getDouble("ftboss_manor_health_balance") == var5) {
                return;
            }

            double var7 = var3.contains("ftboss_base_hp") ? var3.getDouble("ftboss_base_hp") : var2.getBaseValue();
            float var9 = var1.getHealth();
            float var10 = var1.getMaxHealth();
            var2.setBaseValue(ManorSupportRules.scaledBase(var2.getBaseValue(), var7, var5));
            if (var3.contains("ftboss_base_hp")) {
                var3.putDouble("ftboss_base_hp", var5);
            }

            var1.setHealth(ManorSupportRules.health(var9, var10, var1.getMaxHealth()));
            var3.putDouble("ftboss_manor_health_balance", var5);
            return;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onJoin(EntityJoinLevelEvent var0) {
        if (!var0.isCanceled() && var0.getEntity() instanceof EasterBossEntity var1) {
            normalize(var1);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onTick(EntityTickEvent.Pre var0) {
        if (var0.getEntity() instanceof EasterBossEntity var1) {
            normalize(var1);
        }
    }
}
