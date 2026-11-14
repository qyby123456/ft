package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorPhysicalDefenseRules;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ManorPhysicalDefense {
    public static final TagKey<DamageType> PHYSICAL_DAMAGE = TagKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation("ftboss", "physical_damage"));
    public static final UUID HALF_HEALTH_ARMOR = UUID.fromString("2451b949-9d73-47e6-83a2-1b1c976a3c6e");
    private static final String STATE = "ftboss:physical_defense";

    private ManorPhysicalDefense() {}

    public static boolean applies(LivingEntity entity) {
        return entity instanceof EasterBossEntity boss && !boss.isMini() && !boss.isGiant()
                && ManorPhysicalDefenseRules.supports(boss.getVariant());
    }

    private static CompoundTag state(EasterBossEntity boss) {
        CompoundTag data = boss.getPersistentData().getCompound(STATE);
        if (!data.contains("variant") || data.getInt("variant") != boss.getVariant()) {
            data = new CompoundTag();
            data.putInt("variant", boss.getVariant());
            boss.getPersistentData().put(STATE, data);
        }
        return data;
    }

    public static float limit(LivingEntity entity, DamageSource source, float finalDamage) {
        return ManorDamageLimit.limit(entity, source, finalDamage);
    }

    public static void syncArmor(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity boss) || entity.level().isClientSide()) return;
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        if (!applies(entity) || !entity.isAlive()) {
            armor.removeModifier(HALF_HEALTH_ARMOR);
            boss.getPersistentData().remove(STATE);
            return;
        }
        CompoundTag data = state(boss);
        if (!data.getBoolean("half_health")
                && ManorPhysicalDefenseRules.halfHealth(entity.getHealth(), entity.getMaxHealth())) {
            data.putBoolean("half_health", true);
        }
        if (data.getBoolean("half_health")) {
            if (armor.getModifier(HALF_HEALTH_ARMOR) == null) {
                armor.addTransientModifier(new AttributeModifier(HALF_HEALTH_ARMOR,
                        "ftboss.manor_half_health_armor", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else {
            armor.removeModifier(HALF_HEALTH_ARMOR);
        }
    }

    public static void healthChanged(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity boss) || entity.level().isClientSide()) return;
        CompoundTag data = boss.getPersistentData().getCompound(STATE);
        if (data.contains("variant") && data.getInt("variant") == boss.getVariant()) syncArmor(entity);
    }

    public static int armorValue(LivingEntity entity, int vanillaArmor) {
        if (!applies(entity)) return vanillaArmor;
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor == null || armor.getModifier(HALF_HEALTH_ARMOR) == null) return vanillaArmor;
        double addition = armor.getBaseValue();
        for (AttributeModifier modifier : armor.getModifiers(AttributeModifier.Operation.ADDITION)) {
            addition += modifier.getAmount();
        }
        double value = addition;
        for (AttributeModifier modifier : armor.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
            value += addition * modifier.getAmount();
        }
        for (AttributeModifier modifier : armor.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            if (!modifier.getId().equals(HALF_HEALTH_ARMOR)) value *= 1.0 + modifier.getAmount();
        }
        value = armor.getAttribute().sanitizeValue(value);
        return Double.isFinite(value) ? (int) Math.min(Integer.MAX_VALUE, Math.max(0.0, value * 2.0)) : vanillaArmor;
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        syncArmor(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingHurtEvent event) {
        if (!event.isCanceled()) syncArmor(event.getEntity());
    }
}
