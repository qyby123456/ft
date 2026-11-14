package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class NonManorFinaleAttributes {
    private static final String PREPARED = "ftboss:regular_finale_prepared";
    private static final String BASE_PREFIX = "ftboss:regular_finale_base/";
    private static final ResourceLocation LEGACY = ResourceLocation.fromNamespaceAndPath("ftboss", "non_manor_finale_double");

    private NonManorFinaleAttributes() {}

    public static void apply(LivingEntity boss, boolean fullHeal) {
        if (boss == null || boss.level().isClientSide || !NonManorFinale.isFinale(boss)) return;
        CompoundTag data = boss.getPersistentData();
        if (data.getBoolean(PREPARED)) {
            if (boss instanceof TeaTreeBossEntity saya && !saya.isFinaleChallenge()) data.putBoolean("ftboss:saya_finale", true);
            ensureMaximumHealth(boss, fullHeal);
            return;
        }
        float oldMaximum = boss.getMaxHealth();
        float ratio = oldMaximum > 0.0F ? boss.getHealth() / oldMaximum : 1.0F;
        List<AttributeBase> bases = new ArrayList<>();
        AttributeInstance maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        rememberBase(data, bases, maximumHealth);
        for (AttributeInstance instance : boss.getAttributes().getSyncableAttributes()) {
            if (instance != maximumHealth) rememberBase(data, bases, instance);
        }
        data.putBoolean(PREPARED, true);
        data.putBoolean("ftboss:regular_finale", true);
        if (boss instanceof TeaTreeBossEntity saya) saya.setFinaleChallenge(true);
        for (AttributeBase entry : bases) {
            entry.instance().removeModifier(LEGACY);
            entry.instance().setBaseValue(entry.instance().getAttribute().value().sanitizeValue(entry.base() * 2.0D));
        }
        boss.setHealth(fullHeal ? boss.getMaxHealth() : Math.max(1.0F, Math.min(boss.getMaxHealth(), ratio * boss.getMaxHealth())));
    }

    private static void rememberBase(CompoundTag data, List<AttributeBase> bases, AttributeInstance instance) {
        if (instance == null) return;
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(instance.getAttribute().value());
        if (id == null) return;
        String key = BASE_PREFIX + id;
        if (!data.contains(key)) data.putDouble(key, instance.getBaseValue());
        bases.add(new AttributeBase(instance, data.getDouble(key)));
    }

    private static void ensureMaximumHealth(LivingEntity boss, boolean fullHeal) {
        AttributeInstance maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximumHealth == null) return;
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(maximumHealth.getAttribute().value());
        if (id == null) return;
        CompoundTag data = boss.getPersistentData();
        String key = BASE_PREFIX + id;
        if (!data.contains(key)) data.putDouble(key, data.contains("ftboss_base_hp") ? data.getDouble("ftboss_base_hp") : maximumHealth.getBaseValue());
        double desired = maximumHealth.getAttribute().value().sanitizeValue(data.getDouble(key) * 2.0D);
        float oldMaximum = boss.getMaxHealth();
        float ratio = oldMaximum > 0.0F ? boss.getHealth() / oldMaximum : 1.0F;
        if (Math.abs(maximumHealth.getBaseValue() - desired) > 0.01D) {
            maximumHealth.setBaseValue(desired);
            boss.setHealth(fullHeal ? boss.getMaxHealth() : Math.max(1.0F, Math.min(boss.getMaxHealth(), ratio * boss.getMaxHealth())));
        }
    }

    private record AttributeBase(AttributeInstance instance, double base) {}
}
