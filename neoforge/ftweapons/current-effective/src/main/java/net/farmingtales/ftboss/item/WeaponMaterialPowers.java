package net.farmingtales.ftboss.item;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.util.WeaponMaterialPowerRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;

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

    public static void snapshot(ItemStack var0, CompoundTag var1) {
        CompoundTag var2 = new CompoundTag();

        for (FoodType var6 : FoodType.values()) {
            var2.putInt(var6.name(), WeaponMaterialPowerRules.foodCount(FoodWeaponEvolution.socketTypeCount(var0, var6)));
        }

        var1.put("FoodSocketCounts", var2);
    }

    private static int foodCount(ServerPlayer var0, DamageSource var1, Material var2) {
        String var3 = WeaponMaterialPowerRules.foodFamily(var2);
        if (var3.isEmpty()) {
            return 0;
        } else {
            return var1 != null && var1.getDirectEntity() instanceof AbstractArrow var4
                ? WeaponMaterialPowerRules.foodCount(var4.getPersistentData().getCompound("FTBossWeaponMaterials").getCompound("FoodSocketCounts").getInt(var3))
                : WeaponMaterialPowerRules.foodCount(FoodWeaponEvolution.socketTypeCount(var0.getMainHandItem(), FoodType.valueOf(var3)));
        }
    }

    static int quoteClosedMaterialFood(ServerPlayer player, DamageSource source, Material material) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getNestHost() != OwnerMaterialProcAuthority.class)
            throw new SecurityException("Original closed material quote required");
        return foodCount(player, source, material);
    }

    private static boolean proc(ServerPlayer var0, Material var1, int var2) {
        long var3 = var0.serverLevel().getServer().overworld().getGameTime();
        CompoundTag var5 = var0.getPersistentData().getCompound("FTBossMaterialPowers");
        CompoundTag var6 = var5.getCompound(var1.id());
        OwnerMaterialProcAuthority.acquired(var0, var1, var2, var3, var5, var6);
        int var7 = WeaponMaterialPowerRules.baseCooldown(var1);
        int var8 = var6.contains("Delay", 3) ? Math.max(var7 * 4 / 5, Math.min(var7, var6.getInt("Delay"))) : var7;
        if (!WeaponMaterialPowerRules.ready(var3, var6.contains("Stamp", 4), var6.getLong("Stamp"), var8)) {
            return false;
        } else {
            OwnerMaterialProcAuthority.putStamp(var6, "Stamp", var3);
            OwnerMaterialProcAuthority.putDelay(var6, "Delay", WeaponMaterialPowerRules.cooldown(var1, var2));
            OwnerMaterialProcAuthority.putCompound(var5, var1.id(), var6);
            OwnerMaterialProcAuthority.putCompound(var0.getPersistentData(), "FTBossMaterialPowers", var5);
            return true;
        }
    }

    private static void effect(LivingEntity var0, Holder<MobEffect> var1, int var2, int var3, ServerPlayer var4) {
        var0.addEffect(new MobEffectInstance(var1, WeaponMaterialPowerRules.duration(var2, var3), 0), var4);
    }

    private static TamableAnimal pet(ServerPlayer var0, boolean var1) {
        double var2 = 8.0;
        return var0.serverLevel()
            .getEntitiesOfClass(
                TamableAnimal.class,
                var0.getBoundingBox().inflate(var2),
                var4 -> var4.isAlive()
                        && var4.isTame()
                        && var4.isOwnedBy(var0)
                        && var4.distanceToSqr(var0) <= var2 * var2
                        && (!var1 || var4.getHealth() < var4.getMaxHealth())
            )
            .stream()
            .min(Comparator.comparingDouble(var1x -> var1x.distanceToSqr(var0)))
            .orElse(null);
    }

    public static void hit(BeforeMitigation var0, ServerPlayer var1, List<Material> var2) {
        LivingEntity var3 = var0.getEntity();
        if (var3 instanceof Monster
            && !var3.isAlliedTo(var1)
            && !var1.isAlliedTo(var3)
            && (!(var3 instanceof OwnableEntity var4) || var4.getOwnerUUID() == null)
            && !LemonSummonRewards.suppressed(var3)) {
            DamageSource var7 = var0.getSource();
            if (var2.contains(Material.COAL) && var3.isOnFire()) {
                var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(var1, var7, Material.COAL)) ? 1.06F : 1.04F));
            }

            if (var2.contains(Material.LAPIS)) {
                int var5 = foodCount(var1, var7, Material.LAPIS);
                if (proc(var1, Material.LAPIS, var5)) {
                    effect(var1, MobEffects.DIG_SPEED, 40, var5, var1);
                }
            }

            if (var2.contains(Material.REDSTONE)) {
                int var8 = foodCount(var1, var7, Material.REDSTONE);
                if (proc(var1, Material.REDSTONE, var8)) {
                    effect(var1, MobEffects.MOVEMENT_SPEED, 40, var8, var1);
                }
            }

            if (var2.contains(Material.QUARTZ) && var3.hasEffect(MobEffects.GLOWING)) {
                var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(var1, var7, Material.QUARTZ)) ? 1.1F : 1.06F));
            }

            if (var2.contains(Material.AMETHYST)) {
                int var9 = foodCount(var1, var7, Material.AMETHYST);
                if (proc(var1, Material.AMETHYST, var9)) {
                    effect(var3, MobEffects.GLOWING, 60, var9, var1);
                }
            }

            if (var2.contains(Material.WITHERITE)) {
                int var10 = foodCount(var1, var7, Material.WITHERITE);
                if (proc(var1, Material.WITHERITE, var10)) {
                    effect(var3, MobEffects.WEAKNESS, 60, var10, var1);
                }
            }

            if (var2.contains(Material.IGNITIUM)) {
                int var11 = foodCount(var1, var7, Material.IGNITIUM);
                if (var3.isOnFire()) {
                    var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(var11) ? 1.12F : 1.08F));
                }

                if (proc(var1, Material.IGNITIUM, var11)) {
                    var3.igniteForSeconds(WeaponMaterialPowerRules.duration(80, var11) / 20);
                }
            }

            if (var2.contains(Material.CURSIUM)) {
                int var12 = foodCount(var1, var7, Material.CURSIUM);
                if (var3.getType().is(EntityTypeTags.UNDEAD)) {
                    var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(var12) ? 1.12F : 1.08F));
                }

                if (proc(var1, Material.CURSIUM, var12)) {
                    effect(var1, MobEffects.NIGHT_VISION, 240, var12, var1);
                }
            }

            if (var2.contains(Material.ANCIENT_METAL) && var3.getArmorValue() > 0) {
                int var13 = foodCount(var1, var7, Material.ANCIENT_METAL);
                var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(var13) ? 1.08F : 1.06F));
                if (proc(var1, Material.ANCIENT_METAL, var13)) {
                    effect(var3, MobEffects.MOVEMENT_SLOWDOWN, 60, var13, var1);
                }
            }

            if (var2.contains(Material.BOSS_FROST)) {
                int var14 = foodCount(var1, var7, Material.BOSS_FROST);
                if (proc(var1, Material.BOSS_FROST, var14)) {
                    effect(var3, MobEffects.MOVEMENT_SLOWDOWN, 60, var14, var1);
                }
            }

            if (var2.contains(Material.BOSS_BUTCHER) && var1.getHealth() <= var1.getMaxHealth() * 0.5F) {
                int var15 = foodCount(var1, var7, Material.BOSS_BUTCHER);
                if (proc(var1, Material.BOSS_BUTCHER, var15)) {
                    var1.heal(1.0F);
                }
            }

            if (var2.contains(Material.BOSS_TEA)) {
                int var16 = foodCount(var1, var7, Material.BOSS_TEA);
                if (proc(var1, Material.BOSS_TEA, var16)) {
                    effect(var1, MobEffects.REGENERATION, 60, var16, var1);
                }
            }

            if (var2.contains(Material.BOSS_WINE) && var1.isSprinting()) {
                int var17 = foodCount(var1, var7, Material.BOSS_WINE);
                if (proc(var1, Material.BOSS_WINE, var17)) {
                    effect(var1, MobEffects.INVISIBILITY, 60, var17, var1);
                }
            }

            if (var2.contains(Material.BOSS_CHEESE) && pet(var1, false) != null) {
                var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(var1, var7, Material.BOSS_CHEESE)) ? 1.12F : 1.08F));
            }

            if (var2.contains(Material.BOSS_SHEPHERD)) {
                TamableAnimal var18 = pet(var1, true);
                int var6 = foodCount(var1, var7, Material.BOSS_SHEPHERD);
                if (var18 != null && proc(var1, Material.BOSS_SHEPHERD, var6)) {
                    var18.heal(2.0F);
                }
            }

            if (var2.contains(Material.BOSS_TIDE) && var1.isInWaterOrRain()) {
                var0.setAmount(var0.getAmount() * 1.08F);
                int var19 = foodCount(var1, var7, Material.BOSS_TIDE);
                if (proc(var1, Material.BOSS_TIDE, var19)) {
                    effect(var1, MobEffects.WATER_BREATHING, 100, var19, var1);
                    effect(var1, MobEffects.DOLPHINS_GRACE, 100, var19, var1);
                }
            }
        }
    }

    public static void guard(BeforeMitigation var0, ServerPlayer var1, List<Material> var2) {
        if (!var0.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !var0.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            if (var2.contains(Material.ENDERITE) && var0.getSource().is(DamageTypeTags.IS_FALL)) {
                var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(var1, null, Material.ENDERITE)) ? 0.7F : 0.8F));
            }

            if (var2.contains(Material.BLACK_STEEL) && var0.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(var1, null, Material.BLACK_STEEL)) ? 0.88F : 0.92F));
            }

            if (var2.contains(Material.MITHRIL) && var1.isCrouching()) {
                var0.setAmount(var0.getAmount() * (WeaponMaterialPowerRules.synergy(foodCount(var1, null, Material.MITHRIL)) ? 0.88F : 0.92F));
            }

            if (var2.contains(Material.BOSS_CAPACITOR)
                && var0.getSource().getEntity() instanceof Monster var3
                && var3.isAlive()
                && !var3.isAlliedTo(var1)
                && !var1.isAlliedTo(var3)
                && (!(var3 instanceof OwnableEntity var4) || var4.getOwnerUUID() == null)
                && !LemonSummonRewards.suppressed(var3)
                && var0.getSource().getDirectEntity() == var3
                && var3.distanceToSqr(var1) <= 9.0
                && var1.hasLineOfSight(var3)) {
                int var7 = foodCount(var1, null, Material.BOSS_CAPACITOR);
                if (proc(var1, Material.BOSS_CAPACITOR, var7)) {
                    var3.knockback(WeaponMaterialPowerRules.repelStrength(var7), var1.getX() - var3.getX(), var1.getZ() - var3.getZ());
                }
            }

            if (var2.contains(Material.BOSS_APPLE) && var1.getHealth() <= var1.getMaxHealth() * 0.5F) {
                int var6 = foodCount(var1, null, Material.BOSS_APPLE);
                if (proc(var1, Material.BOSS_APPLE, var6)) {
                    effect(var1, MobEffects.DAMAGE_RESISTANCE, 60, var6, var1);
                }
            }
        }
    }

    public static void kill(ServerPlayer var0, List<Material> var1, DamageSource var2) {
        if (var1.contains(Material.BOSS_SPORE)) {
            int var3 = foodCount(var0, var2, Material.BOSS_SPORE);
            if (proc(var0, Material.BOSS_SPORE, var3)) {
                effect(var0, MobEffects.ABSORPTION, 80, var3, var0);
            }
        }
    }

    public static void clonePlayer(Clone var0) {
        if (var0.getOriginal().getPersistentData().contains("FTBossMaterialPowers", 10)) {
            var0.getEntity().getPersistentData().put("FTBossMaterialPowers", var0.getOriginal().getPersistentData().getCompound("FTBossMaterialPowers").copy());
        }
    }

    static void applyClosedHitSteps(
        BeforeMitigation var0,
        ServerPlayer var1,
        List<Material> var2,
        List<TamableAnimal> var3,
        FoodWeaponHitPlan.FinalEffectsCommit var4,
        FoodWeaponFinalDamage.ChildHurtBridge var5
    ) {
        if (FoodWeaponHitPlan.leafLive(var4, var5)) {
            if (!FoodWeaponBondHits.secondary(var0.getSource())
                && !var0.isCanceled()
                && Float.isFinite(var0.getAmount())
                && !(var0.getAmount() <= 0.0F)
                && FoodWeaponBondHits.resolve(var0.getSource(), var1) != null) {
                LivingEntity var6 = var0.getEntity();
                if (var6 instanceof Monster
                    && !var6.isAlliedTo(var1)
                    && !var1.isAlliedTo(var6)
                    && (!(var6 instanceof OwnableEntity var7) || var7.getOwnerUUID() == null)
                    && !LemonSummonRewards.suppressed(var6)) {
                    DamageSource var10 = var0.getSource();
                    if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                        if (var2.contains(Material.LAPIS)) {
                            int var8 = foodCount(var1, var10, Material.LAPIS);
                            if (closedProc(var4, var5, var1, Material.LAPIS, var8)) {
                                effect(var1, MobEffects.DIG_SPEED, 40, var8, var1);
                            }
                        }

                        if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                            if (var2.contains(Material.REDSTONE)) {
                                int var11 = foodCount(var1, var10, Material.REDSTONE);
                                if (closedProc(var4, var5, var1, Material.REDSTONE, var11)) {
                                    effect(var1, MobEffects.MOVEMENT_SPEED, 40, var11, var1);
                                }
                            }

                            if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                if (var2.contains(Material.AMETHYST)) {
                                    int var12 = foodCount(var1, var10, Material.AMETHYST);
                                    if (closedProc(var4, var5, var1, Material.AMETHYST, var12)) {
                                        effect(var6, MobEffects.GLOWING, 60, var12, var1);
                                    }
                                }

                                if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                    if (var2.contains(Material.WITHERITE)) {
                                        int var13 = foodCount(var1, var10, Material.WITHERITE);
                                        if (closedProc(var4, var5, var1, Material.WITHERITE, var13)) {
                                            effect(var6, MobEffects.WEAKNESS, 60, var13, var1);
                                        }
                                    }

                                    if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                        if (var2.contains(Material.IGNITIUM)) {
                                            int var14 = foodCount(var1, var10, Material.IGNITIUM);
                                            if (closedProc(var4, var5, var1, Material.IGNITIUM, var14)) {
                                                var6.igniteForSeconds(WeaponMaterialPowerRules.duration(80, var14) / 20);
                                            }
                                        }

                                        if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                            if (var2.contains(Material.CURSIUM)) {
                                                int var15 = foodCount(var1, var10, Material.CURSIUM);
                                                if (closedProc(var4, var5, var1, Material.CURSIUM, var15)) {
                                                    effect(var1, MobEffects.NIGHT_VISION, 240, var15, var1);
                                                }
                                            }

                                            if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                if (var2.contains(Material.ANCIENT_METAL) && var6.getArmorValue() > 0) {
                                                    int var16 = foodCount(var1, var10, Material.ANCIENT_METAL);
                                                    if (closedProc(var4, var5, var1, Material.ANCIENT_METAL, var16)) {
                                                        effect(var6, MobEffects.MOVEMENT_SLOWDOWN, 60, var16, var1);
                                                    }
                                                }

                                                if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                    if (var2.contains(Material.BOSS_FROST)) {
                                                        int var17 = foodCount(var1, var10, Material.BOSS_FROST);
                                                        if (closedProc(var4, var5, var1, Material.BOSS_FROST, var17)) {
                                                            effect(var6, MobEffects.MOVEMENT_SLOWDOWN, 60, var17, var1);
                                                        }
                                                    }

                                                    if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                        if (var2.contains(Material.BOSS_BUTCHER) && var1.getHealth() <= var1.getMaxHealth() * 0.5F) {
                                                            int var18 = foodCount(var1, var10, Material.BOSS_BUTCHER);
                                                            if (closedProc(var4, var5, var1, Material.BOSS_BUTCHER, var18)) {
                                                                var1.heal(1.0F);
                                                            }
                                                        }

                                                        if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                            if (var2.contains(Material.BOSS_TEA)) {
                                                                int var19 = foodCount(var1, var10, Material.BOSS_TEA);
                                                                if (closedProc(var4, var5, var1, Material.BOSS_TEA, var19)) {
                                                                    effect(var1, MobEffects.REGENERATION, 60, var19, var1);
                                                                }
                                                            }

                                                            if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                                if (var2.contains(Material.BOSS_WINE) && var1.isSprinting()) {
                                                                    int var20 = foodCount(var1, var10, Material.BOSS_WINE);
                                                                    if (closedProc(var4, var5, var1, Material.BOSS_WINE, var20)) {
                                                                        effect(var1, MobEffects.INVISIBILITY, 60, var20, var1);
                                                                    }
                                                                }

                                                                if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                                    if (var2.contains(Material.BOSS_SHEPHERD)) {
                                                                        TamableAnimal var21 = pet(var1, true);
                                                                        if (!FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                                            return;
                                                                        }

                                                                        if (var21 != null
                                                                            && (
                                                                                !var3.stream().anyMatch(var1x -> var1x == var21)
                                                                                    || !FoodWeaponHitPlan.matchesBoundRecipient(
                                                                                        var4, var21, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE
                                                                                    )
                                                                            )) {
                                                                            FoodWeaponHitPlan.stopFinalEffects(var4);
                                                                            return;
                                                                        }

                                                                        int var9 = foodCount(var1, var10, Material.BOSS_SHEPHERD);
                                                                        if (!FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                                            return;
                                                                        }

                                                                        if (var21 != null && closedProc(var4, var5, var1, Material.BOSS_SHEPHERD, var9)) {
                                                                            if (pet(var1, true) != var21) {
                                                                                FoodWeaponHitPlan.stopFinalEffects(var4);
                                                                                return;
                                                                            }

                                                                            if (!FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                                                return;
                                                                            }

                                                                            if (!FoodWeaponHitPlan.matchesBoundRecipient(
                                                                                var4, var21, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE
                                                                            )) {
                                                                                FoodWeaponHitPlan.stopFinalEffects(var4);
                                                                                return;
                                                                            }

                                                                            var21.heal(2.0F);
                                                                        }
                                                                    }

                                                                    if (FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                                        if (var2.contains(Material.BOSS_TIDE) && var1.isInWaterOrRain()) {
                                                                            int var22 = foodCount(var1, var10, Material.BOSS_TIDE);
                                                                            if (closedProc(var4, var5, var1, Material.BOSS_TIDE, var22)) {
                                                                                effect(var1, MobEffects.WATER_BREATHING, 100, var22, var1);
                                                                                if (!FoodWeaponHitPlan.leafLive(var4, var5)) {
                                                                                    return;
                                                                                }

                                                                                effect(var1, MobEffects.DOLPHINS_GRACE, 100, var22, var1);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static boolean matchesPetRecipientClosure(ServerPlayer var0, Set<TamableAnimal> var1) {
        if (var0 != null && var1 != null && var0.serverLevel().getServer().isSameThread()) {
            for (Entity var3 : var0.serverLevel().getAllEntities()) {
                if (var3 instanceof TamableAnimal var4 && var4.isAlive() && !var4.isRemoved() && !var1.contains(var4)) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private static boolean closedProc(
        FoodWeaponHitPlan.FinalEffectsCommit var0, FoodWeaponFinalDamage.ChildHurtBridge var1, ServerPlayer var2, Material var3, int var4
    ) {
        if (!FoodWeaponHitPlan.leafLive(var0, var1)) {
            return false;
        } else {
            OwnerMaterialProcAuthority.Proc observation = OwnerMaterialProcAuthority.begin(var0, var1, var2, var3, var4);
            boolean var5;
            try {
                var5 = proc(var2, var3, var4);
            } catch (Throwable failure) {
                OwnerMaterialProcAuthority.finished(observation, false, failure);
                throw failure;
            }
            OwnerMaterialProcAuthority.finished(observation, var5, null);
            return var5 && FoodWeaponHitPlan.leafLive(var0, var1);
        }
    }
}
