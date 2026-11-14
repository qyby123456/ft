package net.farmingtales.ftboss.item;

import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.WeaponMaterialPowerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** 锭/Boss 构筑被动；共享玩家冷却、箭矢食物快照、友军治疗与力场斥反，2026-10-12 接入既有材料事件。 */
public final class WeaponMaterialPowers {
    private static final String TIMERS = "FTBossMaterialPowers";
    private static final String FOODS = "FoodSocketCounts";
    private static final String STAMP = "Stamp";
    private static final String DELAY = "Delay";
    private static final int REDSTONE_DURATION_TICKS = 40;
    private static final int LAPIS_DURATION_TICKS = 40;
    private static final int AMETHYST_DURATION_TICKS = 60;
    private static final int WITHERITE_DURATION_TICKS = 60;
    private static final int IGNITIUM_DURATION_TICKS = 80;
    private static final int CURSIUM_DURATION_TICKS = 240;
    private static final int ANCIENT_DURATION_TICKS = 60;
    private static final int FROST_DURATION_TICKS = 60;
    private static final int TEA_DURATION_TICKS = 60;
    private static final int WINE_DURATION_TICKS = 60;
    private static final int SPORE_DURATION_TICKS = 80;
    private static final int APPLE_DURATION_TICKS = 60;
    private static final int TIDE_DURATION_TICKS = 100;

    private WeaponMaterialPowers() {}

    public static void snapshot(ItemStack weapon, CompoundTag snapshot) {
        CompoundTag counts = new CompoundTag();
        for (FoodType type : FoodType.values()) {
            counts.putInt(type.name(), WeaponMaterialPowerRules.foodCount(FoodWeaponEvolution.socketTypeCount(weapon, type)));
        }
        snapshot.put(FOODS, counts);
    }

    private static int foodCount(ServerPlayer player, DamageSource source, Material material) {
        String family = WeaponMaterialPowerRules.foodFamily(material);
        if (family.isEmpty()) return 0;
        if (source != null) {
            CompoundTag snapshot = FoodWeaponRanged.materialSnapshot(source, player);
            if (!snapshot.isEmpty()) return WeaponMaterialPowerRules.foodCount(snapshot.getCompound(FOODS).getInt(family));
        }
        return WeaponMaterialPowerRules.foodCount(FoodWeaponEvolution.socketTypeCount(player.getMainHandItem(), FoodType.valueOf(family)));
    }

    private static boolean proc(ServerPlayer player, Material material, int count) {
        long now = player.serverLevel().getServer().overworld().getGameTime();
        CompoundTag timers = player.getPersistentData().getCompound(TIMERS);
        CompoundTag timer = timers.getCompound(material.id());
        int baseDelay = WeaponMaterialPowerRules.baseCooldown(material);
        int previousDelay = timer.contains(DELAY, Tag.TAG_INT) ? Math.max(baseDelay * 4 / 5, Math.min(baseDelay, timer.getInt(DELAY))) : baseDelay;
        if (!WeaponMaterialPowerRules.ready(now, timer.contains(STAMP, Tag.TAG_LONG), timer.getLong(STAMP), previousDelay)) return false;
        timer.putLong(STAMP, now);
        timer.putInt(DELAY, WeaponMaterialPowerRules.cooldown(material, count));
        timers.put(material.id(), timer);
        player.getPersistentData().put(TIMERS, timers);
        return true;
    }

    private static void effect(LivingEntity entity, MobEffect effect, int duration, int count, ServerPlayer player) {
        entity.addEffect(new MobEffectInstance(effect, WeaponMaterialPowerRules.duration(duration, count), 0), player);
    }

    private static TamableAnimal pet(ServerPlayer player, boolean wounded) {
        double range = WeaponMaterialPowerRules.PET_RANGE;
        return player.serverLevel().getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(range), animal ->
                animal.isAlive() && animal.isTame() && animal.isOwnedBy(player) && animal.distanceToSqr(player) <= range * range
                        && (!wounded || animal.getHealth() < animal.getMaxHealth()))
                .stream().min(Comparator.comparingDouble(animal -> animal.distanceToSqr(player))).orElse(null);
    }

    public static void hit(LivingHurtEvent event, ServerPlayer player, List<Material> materials) {
        LivingEntity target = event.getEntity();
        if (!(target instanceof Monster) || target.isAlliedTo(player) || player.isAlliedTo(target)
                || target instanceof OwnableEntity owned && owned.getOwnerUUID() != null || LemonSummonRewards.suppressed(target)) return;
        DamageSource source = event.getSource();
        if (materials.contains(Material.COAL) && target.isOnFire()) {
            event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(player, source, Material.COAL)) ? 1.06F : 1.04F));
        }
        if (materials.contains(Material.LAPIS)) {
            int count = foodCount(player, source, Material.LAPIS);
            if (proc(player, Material.LAPIS, count)) effect(player, MobEffects.DIG_SPEED, LAPIS_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.REDSTONE)) {
            int count = foodCount(player, source, Material.REDSTONE);
            if (proc(player, Material.REDSTONE, count)) effect(player, MobEffects.MOVEMENT_SPEED, REDSTONE_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.QUARTZ) && target.hasEffect(MobEffects.GLOWING)) {
            event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(player, source, Material.QUARTZ)) ? 1.10F : 1.06F));
        }
        if (materials.contains(Material.AMETHYST)) {
            int count = foodCount(player, source, Material.AMETHYST);
            if (proc(player, Material.AMETHYST, count)) effect(target, MobEffects.GLOWING, AMETHYST_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.WITHERITE)) {
            int count = foodCount(player, source, Material.WITHERITE);
            if (proc(player, Material.WITHERITE, count)) effect(target, MobEffects.WEAKNESS, WITHERITE_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.IGNITIUM)) {
            int count = foodCount(player, source, Material.IGNITIUM);
            if (target.isOnFire()) event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(count) ? 1.12F : 1.08F));
            if (proc(player, Material.IGNITIUM, count)) target.setSecondsOnFire(WeaponMaterialPowerRules.duration(IGNITIUM_DURATION_TICKS, count) / 20);
        }
        if (materials.contains(Material.CURSIUM)) {
            int count = foodCount(player, source, Material.CURSIUM);
            if (target.getMobType() == MobType.UNDEAD) event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(count) ? 1.12F : 1.08F));
            if (proc(player, Material.CURSIUM, count)) effect(player, MobEffects.NIGHT_VISION, CURSIUM_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.ANCIENT_METAL) && target.getArmorValue() > 0) {
            int count = foodCount(player, source, Material.ANCIENT_METAL);
            event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(count) ? 1.08F : 1.06F));
            if (proc(player, Material.ANCIENT_METAL, count)) effect(target, MobEffects.MOVEMENT_SLOWDOWN, ANCIENT_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.BOSS_FROST)) {
            int count = foodCount(player, source, Material.BOSS_FROST);
            if (proc(player, Material.BOSS_FROST, count)) effect(target, MobEffects.MOVEMENT_SLOWDOWN, FROST_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.BOSS_BUTCHER) && player.getHealth() <= player.getMaxHealth() * 0.50F) {
            int count = foodCount(player, source, Material.BOSS_BUTCHER);
            if (proc(player, Material.BOSS_BUTCHER, count)) player.heal(1.0F);
        }
        if (materials.contains(Material.BOSS_TEA)) {
            int count = foodCount(player, source, Material.BOSS_TEA);
            if (proc(player, Material.BOSS_TEA, count)) effect(player, MobEffects.REGENERATION, TEA_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.BOSS_WINE) && player.isSprinting()) {
            int count = foodCount(player, source, Material.BOSS_WINE);
            if (proc(player, Material.BOSS_WINE, count)) effect(player, MobEffects.INVISIBILITY, WINE_DURATION_TICKS, count, player);
        }
        if (materials.contains(Material.BOSS_CHEESE) && pet(player, false) != null) {
            event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(player, source, Material.BOSS_CHEESE)) ? 1.12F : 1.08F));
        }
        if (materials.contains(Material.BOSS_SHEPHERD)) {
            TamableAnimal animal = pet(player, true);
            int count = foodCount(player, source, Material.BOSS_SHEPHERD);
            if (animal != null && proc(player, Material.BOSS_SHEPHERD, count)) animal.heal(2.0F);
        }
        if (materials.contains(Material.BOSS_TIDE) && player.isInWaterOrRain()) {
            event.setAmount(event.getAmount() * 1.08F);
            int count = foodCount(player, source, Material.BOSS_TIDE);
            if (proc(player, Material.BOSS_TIDE, count)) {
                effect(player, MobEffects.WATER_BREATHING, TIDE_DURATION_TICKS, count, player);
                effect(player, MobEffects.DOLPHINS_GRACE, TIDE_DURATION_TICKS, count, player);
            }
        }
    }

    public static void guard(LivingHurtEvent event, ServerPlayer player, List<Material> materials) {
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) || event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) return;
        if (materials.contains(Material.ENDERITE) && event.getSource().is(DamageTypeTags.IS_FALL)) {
            event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(player, null, Material.ENDERITE)) ? 0.70F : 0.80F));
        }
        if (materials.contains(Material.BLACK_STEEL) && event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(player, null, Material.BLACK_STEEL)) ? 0.88F : 0.92F));
        }
        if (materials.contains(Material.MITHRIL) && player.isCrouching()) {
            event.setAmount(event.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(player, null, Material.MITHRIL)) ? 0.88F : 0.92F));
        }
        if (materials.contains(Material.BOSS_CAPACITOR) && event.getSource().getEntity() instanceof Monster attacker
                && attacker.isAlive() && !attacker.isAlliedTo(player) && !player.isAlliedTo(attacker)
                && !(attacker instanceof OwnableEntity owned && owned.getOwnerUUID() != null)
                && !LemonSummonRewards.suppressed(attacker)
                && event.getSource().getDirectEntity() == attacker
                && attacker.distanceToSqr(player) <= WeaponMaterialPowerRules.REPEL_RANGE * WeaponMaterialPowerRules.REPEL_RANGE
                && player.hasLineOfSight(attacker)) {
            int count = foodCount(player, null, Material.BOSS_CAPACITOR);
            if (proc(player, Material.BOSS_CAPACITOR, count)) {
                attacker.knockback(WeaponMaterialPowerRules.repelStrength(count), player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
            }
        }
        if (materials.contains(Material.BOSS_APPLE) && player.getHealth() <= player.getMaxHealth() * 0.50F) {
            int count = foodCount(player, null, Material.BOSS_APPLE);
            if (proc(player, Material.BOSS_APPLE, count)) effect(player, MobEffects.DAMAGE_RESISTANCE, APPLE_DURATION_TICKS, count, player);
        }
    }

    public static void kill(ServerPlayer player, List<Material> materials, DamageSource source) {
        if (materials.contains(Material.BOSS_SPORE)) {
            int count = foodCount(player, source, Material.BOSS_SPORE);
            if (proc(player, Material.BOSS_SPORE, count)) effect(player, MobEffects.ABSORPTION, SPORE_DURATION_TICKS, count, player);
        }
    }

    public static void clonePlayer(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(TIMERS, Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put(TIMERS, event.getOriginal().getPersistentData().getCompound(TIMERS).copy());
        }
    }
}
