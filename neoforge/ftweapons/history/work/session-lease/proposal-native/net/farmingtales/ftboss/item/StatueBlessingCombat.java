package net.farmingtales.ftboss.item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponBondHits.Hit;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan.Builder;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan.RandomQuotes;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain;
import net.farmingtales.ftboss.util.MeleeEnhancementRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;

public final class StatueBlessingCombat {
    private StatueBlessingCombat() {
    }

    public static void quote(Builder builder, ServerPlayer player, LivingEntity target, CompoundTag data, long time, ItemStack stack, RandomQuotes random) {
    }

    public static void defense(BeforeMitigation event) {
    }

    public static void kill(LivingDeathEvent event) {
    }

    public static void attributes(ItemAttributeModifierEvent event, ItemStack stack) {
    }

    public static void attributes(ItemAttributeModifierEvent event, ItemStack stack, EquipmentSlot slot) {
    }

    private static record Active(String id, int level) {
    }
}
