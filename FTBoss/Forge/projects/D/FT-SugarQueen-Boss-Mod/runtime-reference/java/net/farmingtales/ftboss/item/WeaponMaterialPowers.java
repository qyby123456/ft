/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobType
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 */
package net.farmingtales.ftboss.item;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponMaterialPowerRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

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

    private WeaponMaterialPowers() {
    }

    public static void snapshot(ItemStack itemStack, CompoundTag compoundTag) {
        CompoundTag compoundTag2 = new CompoundTag();
        for (FoodWeaponEvolution.FoodType foodType : FoodWeaponEvolution.FoodType.values()) {
            compoundTag2.m_128405_(foodType.name(), WeaponMaterialPowerRules.foodCount(FoodWeaponEvolution.socketTypeCount(itemStack, foodType)));
        }
        compoundTag.m_128365_(FOODS, (Tag)compoundTag2);
    }

    private static int foodCount(ServerPlayer serverPlayer, DamageSource damageSource, FoodWeaponBuildRules.Material material) {
        Entity entity;
        String string = WeaponMaterialPowerRules.foodFamily(material);
        if (string.isEmpty()) {
            return 0;
        }
        if (damageSource != null && (entity = damageSource.m_7640_()) instanceof AbstractArrow) {
            AbstractArrow abstractArrow = (AbstractArrow)entity;
            return WeaponMaterialPowerRules.foodCount(abstractArrow.getPersistentData().m_128469_("FTBossWeaponMaterials").m_128469_(FOODS).m_128451_(string));
        }
        return WeaponMaterialPowerRules.foodCount(FoodWeaponEvolution.socketTypeCount(serverPlayer.m_21205_(), FoodWeaponEvolution.FoodType.valueOf(string)));
    }

    private static boolean proc(ServerPlayer serverPlayer, FoodWeaponBuildRules.Material material, int n) {
        int n2;
        long l = serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
        CompoundTag compoundTag = serverPlayer.getPersistentData().m_128469_(TIMERS);
        CompoundTag compoundTag2 = compoundTag.m_128469_(material.id());
        int n3 = WeaponMaterialPowerRules.baseCooldown(material);
        int n4 = n2 = compoundTag2.m_128425_(DELAY, 3) ? Math.max(n3 * 4 / 5, Math.min(n3, compoundTag2.m_128451_(DELAY))) : n3;
        if (!WeaponMaterialPowerRules.ready(l, compoundTag2.m_128425_(STAMP, 4), compoundTag2.m_128454_(STAMP), n2)) {
            return false;
        }
        compoundTag2.m_128356_(STAMP, l);
        compoundTag2.m_128405_(DELAY, WeaponMaterialPowerRules.cooldown(material, n));
        compoundTag.m_128365_(material.id(), (Tag)compoundTag2);
        serverPlayer.getPersistentData().m_128365_(TIMERS, (Tag)compoundTag);
        return true;
    }

    private static void effect(LivingEntity livingEntity, MobEffect mobEffect, int n, int n2, ServerPlayer serverPlayer) {
        livingEntity.m_147207_(new MobEffectInstance(mobEffect, WeaponMaterialPowerRules.duration(n, n2), 0), (Entity)serverPlayer);
    }

    private static TamableAnimal pet(ServerPlayer serverPlayer, boolean bl) {
        double d = 8.0;
        return serverPlayer.m_284548_().m_6443_(TamableAnimal.class, serverPlayer.m_20191_().m_82400_(d), tamableAnimal -> tamableAnimal.m_6084_() && tamableAnimal.m_21824_() && tamableAnimal.m_21830_((LivingEntity)serverPlayer) && tamableAnimal.m_20280_((Entity)serverPlayer) <= d * d && (!bl || tamableAnimal.m_21223_() < tamableAnimal.m_21233_())).stream().min(Comparator.comparingDouble(tamableAnimal -> tamableAnimal.m_20280_((Entity)serverPlayer))).orElse(null);
    }

    public static void hit(LivingHurtEvent livingHurtEvent, ServerPlayer serverPlayer, List<FoodWeaponBuildRules.Material> list) {
        int n;
        OwnableEntity ownableEntity;
        LivingEntity livingEntity = livingHurtEvent.getEntity();
        if (!(livingEntity instanceof Monster) || livingEntity.m_7307_((Entity)serverPlayer) || serverPlayer.m_7307_((Entity)livingEntity) || livingEntity instanceof OwnableEntity && (ownableEntity = (OwnableEntity)livingEntity).m_21805_() != null || LemonSummonRewards.suppressed((Entity)livingEntity)) {
            return;
        }
        ownableEntity = livingHurtEvent.getSource();
        if (list.contains((Object)FoodWeaponBuildRules.Material.COAL) && livingEntity.m_6060_()) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.COAL)) ? 1.06f : 1.04f));
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.LAPIS) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.LAPIS, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.LAPIS))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19598_, 40, n, serverPlayer);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.REDSTONE) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.REDSTONE, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.REDSTONE))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19596_, 40, n, serverPlayer);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.QUARTZ) && livingEntity.m_21023_(MobEffects.f_19619_)) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.QUARTZ)) ? 1.1f : 1.06f));
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.AMETHYST) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.AMETHYST, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.AMETHYST))) {
            WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19619_, 60, n, serverPlayer);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.WITHERITE) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.WITHERITE, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.WITHERITE))) {
            WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19613_, 60, n, serverPlayer);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.IGNITIUM)) {
            n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.IGNITIUM);
            if (livingEntity.m_6060_()) {
                livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(n) ? 1.12f : 1.08f));
            }
            if (WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.IGNITIUM, n)) {
                livingEntity.m_20254_(WeaponMaterialPowerRules.duration(80, n) / 20);
            }
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.CURSIUM)) {
            n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.CURSIUM);
            if (livingEntity.m_6336_() == MobType.f_21641_) {
                livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(n) ? 1.12f : 1.08f));
            }
            if (WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.CURSIUM, n)) {
                WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19611_, 240, n, serverPlayer);
            }
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.ANCIENT_METAL) && livingEntity.m_21230_() > 0) {
            n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.ANCIENT_METAL);
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(n) ? 1.08f : 1.06f));
            if (WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.ANCIENT_METAL, n)) {
                WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19597_, 60, n, serverPlayer);
            }
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_FROST) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_FROST, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_FROST))) {
            WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19597_, 60, n, serverPlayer);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_BUTCHER) && serverPlayer.m_21223_() <= serverPlayer.m_21233_() * 0.5f && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_BUTCHER, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_BUTCHER))) {
            serverPlayer.m_5634_(1.0f);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_TEA) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_TEA, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_TEA))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19605_, 60, n, serverPlayer);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_WINE) && serverPlayer.m_20142_() && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_WINE, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_WINE))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19609_, 60, n, serverPlayer);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_CHEESE) && WeaponMaterialPowers.pet(serverPlayer, false) != null) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_CHEESE)) ? 1.12f : 1.08f));
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_SHEPHERD)) {
            TamableAnimal tamableAnimal = WeaponMaterialPowers.pet(serverPlayer, true);
            int n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_SHEPHERD);
            if (tamableAnimal != null && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_SHEPHERD, n2)) {
                tamableAnimal.m_5634_(2.0f);
            }
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_TIDE) && serverPlayer.m_20070_()) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 1.08f);
            int n3 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_TIDE);
            if (WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_TIDE, n3)) {
                WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19608_, 100, n3, serverPlayer);
                WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19593_, 100, n3, serverPlayer);
            }
        }
    }

    public static void guard(LivingHurtEvent livingHurtEvent, ServerPlayer serverPlayer, List<FoodWeaponBuildRules.Material> list) {
        int n;
        int n2;
        OwnableEntity ownableEntity;
        Monster monster;
        Entity entity;
        if (livingHurtEvent.getSource().m_269533_(DamageTypeTags.f_268738_) || livingHurtEvent.getSource().m_269533_(DamageTypeTags.f_268630_)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.ENDERITE) && livingHurtEvent.getSource().m_269533_(DamageTypeTags.f_268549_)) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(WeaponMaterialPowers.foodCount(serverPlayer, null, FoodWeaponBuildRules.Material.ENDERITE)) ? 0.7f : 0.8f));
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BLACK_STEEL) && livingHurtEvent.getSource().m_269533_(DamageTypeTags.f_268524_)) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(WeaponMaterialPowers.foodCount(serverPlayer, null, FoodWeaponBuildRules.Material.BLACK_STEEL)) ? 0.88f : 0.92f));
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.MITHRIL) && serverPlayer.m_6047_()) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (WeaponMaterialPowerRules.synergy(WeaponMaterialPowers.foodCount(serverPlayer, null, FoodWeaponBuildRules.Material.MITHRIL)) ? 0.88f : 0.92f));
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_CAPACITOR) && (entity = livingHurtEvent.getSource().m_7639_()) instanceof Monster && (monster = (Monster)entity).m_6084_() && !monster.m_7307_((Entity)serverPlayer) && !serverPlayer.m_7307_((Entity)monster) && (!(monster instanceof OwnableEntity) || (ownableEntity = (OwnableEntity)monster).m_21805_() == null) && !LemonSummonRewards.suppressed((Entity)monster) && livingHurtEvent.getSource().m_7640_() == monster && monster.m_20280_((Entity)serverPlayer) <= 9.0 && serverPlayer.m_142582_((Entity)monster) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_CAPACITOR, n2 = WeaponMaterialPowers.foodCount(serverPlayer, null, FoodWeaponBuildRules.Material.BOSS_CAPACITOR))) {
            monster.m_147240_(WeaponMaterialPowerRules.repelStrength(n2), serverPlayer.m_20185_() - monster.m_20185_(), serverPlayer.m_20189_() - monster.m_20189_());
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_APPLE) && serverPlayer.m_21223_() <= serverPlayer.m_21233_() * 0.5f && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_APPLE, n = WeaponMaterialPowers.foodCount(serverPlayer, null, FoodWeaponBuildRules.Material.BOSS_APPLE))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19606_, 60, n, serverPlayer);
        }
    }

    public static void kill(ServerPlayer serverPlayer, List<FoodWeaponBuildRules.Material> list, DamageSource damageSource) {
        int n;
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_SPORE) && WeaponMaterialPowers.proc(serverPlayer, FoodWeaponBuildRules.Material.BOSS_SPORE, n = WeaponMaterialPowers.foodCount(serverPlayer, damageSource, FoodWeaponBuildRules.Material.BOSS_SPORE))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19617_, 80, n, serverPlayer);
        }
    }

    public static void clonePlayer(PlayerEvent.Clone clone) {
        if (clone.getOriginal().getPersistentData().m_128425_(TIMERS, 10)) {
            clone.getEntity().getPersistentData().m_128365_(TIMERS, (Tag)clone.getOriginal().getPersistentData().m_128469_(TIMERS).m_6426_());
        }
    }

    static void applyClosedHitSteps(LivingHurtEvent livingHurtEvent, ServerPlayer serverPlayer, List<FoodWeaponBuildRules.Material> list, List<TamableAnimal> list2, FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        int n;
        int n2;
        OwnableEntity ownableEntity;
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (FoodWeaponBondHits.secondary(livingHurtEvent.getSource()) || livingHurtEvent.isCanceled() || !Float.isFinite(livingHurtEvent.getAmount()) || livingHurtEvent.getAmount() <= 0.0f || FoodWeaponBondHits.resolve(livingHurtEvent.getSource(), serverPlayer) == null) {
            return;
        }
        LivingEntity livingEntity = livingHurtEvent.getEntity();
        if (!(livingEntity instanceof Monster) || livingEntity.m_7307_((Entity)serverPlayer) || serverPlayer.m_7307_((Entity)livingEntity) || livingEntity instanceof OwnableEntity && (ownableEntity = (OwnableEntity)livingEntity).m_21805_() != null || LemonSummonRewards.suppressed((Entity)livingEntity)) {
            return;
        }
        ownableEntity = livingHurtEvent.getSource();
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.LAPIS) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.LAPIS, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.LAPIS))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19598_, 40, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.REDSTONE) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.REDSTONE, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.REDSTONE))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19596_, 40, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.AMETHYST) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.AMETHYST, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.AMETHYST))) {
            WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19619_, 60, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.WITHERITE) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.WITHERITE, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.WITHERITE))) {
            WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19613_, 60, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.IGNITIUM) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.IGNITIUM, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.IGNITIUM))) {
            livingEntity.m_20254_(WeaponMaterialPowerRules.duration(80, n2) / 20);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.CURSIUM) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.CURSIUM, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.CURSIUM))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19611_, 240, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.ANCIENT_METAL) && livingEntity.m_21230_() > 0 && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.ANCIENT_METAL, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.ANCIENT_METAL))) {
            WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19597_, 60, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_FROST) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.BOSS_FROST, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_FROST))) {
            WeaponMaterialPowers.effect(livingEntity, MobEffects.f_19597_, 60, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_BUTCHER) && serverPlayer.m_21223_() <= serverPlayer.m_21233_() * 0.5f && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.BOSS_BUTCHER, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_BUTCHER))) {
            serverPlayer.m_5634_(1.0f);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_TEA) && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.BOSS_TEA, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_TEA))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19605_, 60, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_WINE) && serverPlayer.m_20142_() && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.BOSS_WINE, n2 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_WINE))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19609_, 60, n2, serverPlayer);
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_SHEPHERD)) {
            TamableAnimal tamableAnimal = WeaponMaterialPowers.pet(serverPlayer, true);
            if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                return;
            }
            if (!(tamableAnimal == null || list2.stream().anyMatch(tamableAnimal2 -> tamableAnimal2 == tamableAnimal) && FoodWeaponHitPlan.matchesBoundRecipient(finalEffectsCommit, (LivingEntity)tamableAnimal, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE))) {
                FoodWeaponHitPlan.stopFinalEffects(finalEffectsCommit);
                return;
            }
            int n3 = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_SHEPHERD);
            if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                return;
            }
            if (tamableAnimal != null && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.BOSS_SHEPHERD, n3)) {
                if (WeaponMaterialPowers.pet(serverPlayer, true) != tamableAnimal) {
                    FoodWeaponHitPlan.stopFinalEffects(finalEffectsCommit);
                    return;
                }
                if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                    return;
                }
                if (!FoodWeaponHitPlan.matchesBoundRecipient(finalEffectsCommit, (LivingEntity)tamableAnimal, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE)) {
                    FoodWeaponHitPlan.stopFinalEffects(finalEffectsCommit);
                    return;
                }
                tamableAnimal.m_5634_(2.0f);
            }
        }
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BOSS_TIDE) && serverPlayer.m_20070_() && WeaponMaterialPowers.closedProc(finalEffectsCommit, childHurtBridge, serverPlayer, FoodWeaponBuildRules.Material.BOSS_TIDE, n = WeaponMaterialPowers.foodCount(serverPlayer, (DamageSource)ownableEntity, FoodWeaponBuildRules.Material.BOSS_TIDE))) {
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19608_, 100, n, serverPlayer);
            if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                return;
            }
            WeaponMaterialPowers.effect((LivingEntity)serverPlayer, MobEffects.f_19593_, 100, n, serverPlayer);
        }
    }

    static boolean matchesPetRecipientClosure(ServerPlayer serverPlayer, Set<TamableAnimal> set) {
        if (serverPlayer == null || set == null || !serverPlayer.m_284548_().m_7654_().m_18695_()) {
            return false;
        }
        for (Entity entity : serverPlayer.m_284548_().m_8583_()) {
            TamableAnimal tamableAnimal;
            if (!(entity instanceof TamableAnimal) || !(tamableAnimal = (TamableAnimal)entity).m_6084_() || tamableAnimal.m_213877_() || set.contains(tamableAnimal)) continue;
            return false;
        }
        return true;
    }

    private static boolean closedProc(FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge, ServerPlayer serverPlayer, FoodWeaponBuildRules.Material material, int n) {
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return false;
        }
        boolean bl = WeaponMaterialPowers.proc(serverPlayer, material, n);
        return bl && FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge);
    }
}

