package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class WeaponMaterialBuild {
    public static final String ROOT = "FTBossWeaponMaterials";
    private static final UUID DAMAGE_ID = UUID.fromString("894c7a17-bf58-492f-ae96-d21abeb6a811");
    private static final UUID SPEED_ID = UUID.fromString("bceec5f0-ffb7-4449-a91c-98a9a22de2e3");
    private static final UUID REACH_ID = UUID.fromString("712d0686-c0bd-4e7f-9a4f-cedd24c6bfaf");

    private WeaponMaterialBuild() {
    }

    public static Material materialFor(ItemStack var0) {
        return WeaponMaterialCatalog.materialFor(var0);
    }

    public static List<Material> attackMaterials(DamageSource var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            if (var0.getDirectEntity() == var1 && var0.is(DamageTypes.PLAYER_ATTACK)) {
                return FoodWeaponLifecycle.isBroken(var1.getMainHandItem()) ? List.of() : materials(var1.getMainHandItem());
            } else {
                if (var0.getDirectEntity() instanceof AbstractArrow var9 && var9.getOwner() == var1 && var0.is(DamageTypeTags.IS_PROJECTILE)) {
                    CompoundTag var10 = var9.getPersistentData().getCompound("FTBossWeaponMaterials");
                    if (var10.isEmpty()) {
                        return List.of();
                    }

                    ArrayList var4 = new ArrayList();

                    for (Part var8 : Part.values()) {
                        var4.add(Material.byId(var10.getString(var8.name())));
                    }

                    return List.copyOf(var4);
                }

                return List.of();
            }
        } else {
            return List.of();
        }
    }

    public static Component materialName(Material var0) {
        return Component.translatable("material.ftboss.weapon." + var0.id());
    }

    public static Component partName(Part var0) {
        return Component.translatable("part.ftboss.weapon." + var0.name().toLowerCase(Locale.ROOT));
    }

    public static Component passive(Material var0) {
        return Component.translatable("passive.ftboss.material." + var0.id());
    }

    public static int previewDurability(ItemStack var0, Build var1) {
        return FoodWeaponBuildRules.maxDurability(var0.getItem().getMaxDamage(var0), var1);
    }

    public static WeaponMaterialBuild.CombatStats combatStats(ItemStack var0) {
        return new WeaponMaterialBuild.CombatStats(
            itemAttribute(var0, Attributes.ATTACK_DAMAGE, 1.0),
            itemAttribute(var0, Attributes.ATTACK_SPEED, 4.0),
            itemAttribute(var0, Attributes.ENTITY_INTERACTION_RANGE, Attributes.ENTITY_INTERACTION_RANGE.value().getDefaultValue()),
            var0.getMaxDamage(),
            build(var0).wear()
        );
    }

    private static double itemAttribute(ItemStack var0, Holder<Attribute> var1, double var2) {
        ArrayList<AttributeModifier> var4 = new ArrayList<>();
        var0.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(var1)) {
                var4.add(modifier);
            }
        });
        double var5 = var2;

        for (AttributeModifier var8 : var4) {
            if (var8.operation() == Operation.ADD_VALUE) {
                var5 += var8.amount();
            }
        }

        double var11 = var5;

        for (AttributeModifier var10 : var4) {
            if (var10.operation() == Operation.ADD_MULTIPLIED_BASE) {
                var11 += var5 * var10.amount();
            }
        }

        for (AttributeModifier var13 : var4) {
            if (var13.operation() == Operation.ADD_MULTIPLIED_TOTAL) {
                var11 *= 1.0 + var13.amount();
            }
        }

        return var1.value().sanitizeValue(var11);
    }

    private static double originalWeaponAttribute(ItemAttributeModifierEvent var0, Holder<Attribute> var1, double var2) {
        double var4 = var2;

        for (var entry : var0.getDefaultModifiers().modifiers()) {
            AttributeModifier var7 = entry.modifier();
            if (entry.attribute().equals(var1) && entry.slot().test(EquipmentSlot.MAINHAND) && var7.operation() == Operation.ADD_VALUE) {
                var4 += var7.amount();
            }
        }

        return Math.max(0.0, var4);
    }

    @SubscribeEvent(
        priority = EventPriority.LOW
    )
    public static void attributes(ItemAttributeModifierEvent var0) {
        ItemStack var1 = var0.getItemStack();
        if (FoodWeaponData.read(var1, "FTBossWeaponMaterials") != null && !FoodWeaponLifecycle.isBroken(var1)) {
            Build var2 = build(var1);
            double var3 = originalWeaponAttribute(var0, Attributes.ATTACK_DAMAGE, 1.0) * (var2.damage() - 1.0);
            double var5 = originalWeaponAttribute(var0, Attributes.ATTACK_SPEED, 4.0) * (var2.speed() - 1.0);
            if (Math.abs(var3) > 1.0E-5) {
                var0.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss", "material/" + DAMAGE_ID), var3, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }

            if (Math.abs(var5) > 1.0E-5) {
                var0.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss", "material/" + SPEED_ID), var5, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }

            if (var2.reach() > 0.0) {
                var0.addModifier(
                    Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss", "material/" + REACH_ID), var2.reach(), Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                );
            }
        }
    }

    @SubscribeEvent
    public static void projectileLaunch(EntityJoinLevelEvent var0) {
        if (!var0.getLevel().isClientSide
            && var0.getEntity() instanceof AbstractArrow var1
            && var1.getOwner() instanceof ServerPlayer var2
            && !var1.getPersistentData().contains("FTBossWeaponMaterials", 10)) {
            ItemStack var6 = var2.getUseItem();
            if (var6.isEmpty()) {
                var6 = var2.getMainHandItem();
            }

            if (FoodWeaponData.read(var6, "FTBossWeaponMaterials") != null) {
                CompoundTag var4 = FoodWeaponData.read(var6, "FTBossWeaponMaterials").copy();
                var4.putDouble("ProjectileDamage", build(var6).damage());
                WeaponMaterialPowers.snapshot(var6, var4);
                var1.getPersistentData().put("FTBossWeaponMaterials", var4);
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOW
    )
    public static void hit(BeforeMitigation var0) {
        if (FoodWeaponFinalDamage.nativeOriginalInteractionActive(var0.getSource(), var0.getEntity())) {
            FoodWeaponFinalDamage.captureMaterial(var0);
            return;
        }
        if (!(var0.getAmount() <= 0.0F) && var0.getSource().getEntity() instanceof ServerPlayer var1) {
            LivingEntity var10 = var0.getEntity();
            if (var10 != var1 && !var10.isAlliedTo(var1)) {
                List var3 = attackMaterials(var0.getSource());
                if (!var3.isEmpty()) {
                    if (var0.getSource().getDirectEntity() instanceof AbstractArrow var4) {
                        CompoundTag var12 = var4.getPersistentData().getCompound("FTBossWeaponMaterials");
                        double var6 = var12.getDouble("ProjectileDamage");
                        double var8 = Double.isFinite(var6) && var6 > 0.0 ? Math.max(0.5, Math.min(2.0, var6)) : 1.0;
                        var0.setAmount(var0.getAmount() * (float)var8);
                    }

                    if (var3.contains(Material.NETHERITE) && var10.getHealth() <= var10.getMaxHealth() * 0.3F) {
                        var0.setAmount(var0.getAmount() * 1.15F);
                    }

                    if (var3.contains(Material.DIAMOND)) {
                        var0.setAmount(var0.getAmount() * 1.05F);
                    }

                    if (var3.contains(Material.BLAZE)) {
                        var10.igniteForSeconds(3);
                    }

                    if (var3.contains(Material.STONE)) {
                        var10.knockback(0.2, var1.getX() - var10.getX(), var1.getZ() - var10.getZ());
                    }

                    if (var3.contains(Material.COPPER) || var3.contains(Material.BONE)) {
                        String var11 = "FTBossMaterialProc";
                        long var13 = var1.serverLevel().getGameTime();
                        CompoundTag var7 = var1.getPersistentData();
                        if (!var7.contains(var11) || var13 - var7.getLong(var11) >= 100L) {
                            var7.putLong(var11, var13);
                            if (var3.contains(Material.COPPER)) {
                                var10.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), var1);
                            }

                            if (var3.contains(Material.BONE)) {
                                var10.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0), var1);
                            }
                        }
                    }

                    WeaponMaterialPowers.hit(var0, var1, var3);
                }
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOW
    )
    public static void guard(BeforeMitigation var0) {
        if (!(var0.getAmount() <= 0.0F) && var0.getEntity() instanceof ServerPlayer var1) {
            if (!FoodWeaponLifecycle.isBroken(var1.getMainHandItem())) {
                List var3 = materials(var1.getMainHandItem());
                if (var3.contains(Material.OBSIDIAN)) {
                    var0.setAmount(var0.getAmount() * 0.9F);
                }

                if (var3.contains(Material.LEATHER) && var1.isSprinting() && var0.getSource().getDirectEntity() instanceof AbstractArrow) {
                    var0.setAmount(var0.getAmount() * 0.9F);
                }

                if (var3.contains(Material.IRON) && var1.getHealth() <= var1.getMaxHealth() * 0.3F) {
                    var0.setAmount(var0.getAmount() * 0.92F);
                }

                WeaponMaterialPowers.guard(var0, var1, var3);
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void kill(LivingDeathEvent var0) {
        if (var0.getEntity() instanceof Monster
            && !LemonSummonRewards.suppressed(var0.getEntity())
            && var0.getSource().getEntity() instanceof ServerPlayer var1) {
            List var5 = attackMaterials(var0.getSource());
            if (!var5.isEmpty()) {
                WeaponMaterialPowers.kill(var1, var5, var0.getSource());
                if (var5.contains(Material.GOLD)) {
                    var1.giveExperiencePoints(1);
                }

                long var3 = var1.serverLevel().getGameTime();
                if ((var5.contains(Material.EMERALD) || var5.contains(Material.WOOD))
                    && var3 - var1.getPersistentData().getLong("FTBossMaterialKillHeal") >= 60L) {
                    var1.getPersistentData().putLong("FTBossMaterialKillHeal", var3);
                    var1.heal(var5.contains(Material.EMERALD) ? 1.0F : 0.5F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void materialClone(Clone var0) {
        WeaponMaterialPowers.clonePlayer(var0);
    }

    public static List<Material> materials(ItemStack var0) {
        if (WeaponAttachmentPolicy.excluded(var0)) {
            return Collections.nCopies(Part.values().length, Material.ORIGINAL);
        } else if (SwordCavityBuild.applies(var0)) {
            ArrayList var7 = new ArrayList<>(SwordCavityBuild.legacyMaterials(var0));

            for (Material var9 : SwordCavityBuild.powerMaterials(var0)) {
                if (!var7.contains(var9)) {
                    var7.add(var9);
                }
            }

            return List.copyOf(var7);
        } else {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponMaterials");
            ArrayList var2 = new ArrayList();

            for (Part var6 : Part.values()) {
                var2.add(var1 == null ? Material.ORIGINAL : Material.byId(var1.getString(var6.name())));
            }

            return List.copyOf(var2);
        }
    }

    public static Build build(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0) ? FourWeaponFoundationAdapter.combat(var0)
            : SwordCavityBuild.applies(var0) ? SwordCavityBuild.build(var0) : FoodWeaponBuildRules.build(materials(var0));
    }

    public static Build preview(ItemStack var0, Part var1, Material var2) {
        if (SwordCavityBuild.applies(var0)) {
            for (Slot var6 : Slot.values()) {
                if (var6.legacyPart == var1) {
                    return SwordCavityBuild.build(SwordCavityBuild.preview(var0, var6, var2));
                }
            }
        }

        ArrayList var7 = new ArrayList<>(materials(var0));
        var7.set(var1.ordinal(), var2);
        return FoodWeaponBuildRules.build(var7);
    }

    public static WeaponMaterialBuild.CombatStats combatPreview(ItemStack var0, Part var1, Material var2) {
        if (SwordCavityBuild.applies(var0)) {
            for (Slot var6 : Slot.values()) {
                if (var6.legacyPart == var1) {
                    return combatStats(SwordCavityBuild.preview(var0, var6, var2));
                }
            }
        }

        ItemStack var7 = var0.copy();
        FoodWeaponData.edit(var7, "FTBossWeaponMaterials", data -> data.putString(var1.name(), var2.id()));
        return combatStats(var7);
    }

    public static boolean replaceFromUi(ServerPlayer var0, int var1) {
        ItemStack var2 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
        if (WeaponAttachmentPolicy.excluded(var2)) {
            return false;
        } else if (!SwordCavityBuild.applies(var2) && !FourWeaponFoundationAdapter.applies(var2)) {
            if (!var0.isSpectator() && var0.isAlive() && var1 >= 0 && var1 < Part.values().length * 36) {
                Part var3 = Part.byIndex(var1 / 36);
                int var4 = var1 % 36;
                ItemStack var5 = var0.getInventory().getItem(var4);
                Material var6 = materialFor(var5);
                if (!var2.isEmpty()
                    && !FoodWeaponLifecycle.isBroken(var2)
                    && var5 != var2
                    && var6 != Material.ORIGINAL
                    && materials(var2).get(var3.ordinal()) != var6) {
                    int var7 = var2.getMaxDamage();
                    int var8 = var2.getDamageValue();
                    if (var7 > 0 && var8 >= var7) {
                        return false;
                    } else {
                        ItemStack var9 = var2.copy();
                        FoodWeaponData.edit(var9, "FTBossWeaponMaterials", data -> data.putString(var3.name(), var6.id()));
                        int var10 = var9.getMaxDamage();
                        int var11 = FoodWeaponBuildRules.preserveDamage(var8, var7, var10);
                        if (var7 > 0 && var11 >= var10) {
                            var0.displayClientMessage(Component.translatable("message.ftboss.material.repair_first"), true);
                            return false;
                        } else {
                            FoodWeaponLifecycle.ensureTracked(var2);
                            FoodWeaponData.edit(var2, "FTBossWeaponMaterials", data -> data.putString(var3.name(), var6.id()));
                            if (var7 > 0) {
                                FoodWeaponLifecycle.materialDamage(var2, var11);
                            }

                            if (!var0.getAbilities().instabuild) {
                                var5.shrink(1);
                            }

                            var0.getInventory().setChanged();
                            var0.containerMenu.broadcastChanges();
                            var0.sendSystemMessage(
                                Component.translatable("message.ftboss.material.replaced", FoodWeaponFamilyBuild.partName(var2, var3), materialName(var6))
                                    .withStyle(ChatFormatting.GOLD)
                            );
                            FTBossMod.LOGGER
                                .info(
                                    "[FTBoss][材料构成] {} 替换 {} 的 {} 为 {}，耐久 {}/{} -> {}/{}",
                                    new Object[]{
                                        var0.getName().getString(),
                                        var2.getHoverName().getString(),
                                        var3.name(),
                                        var6.id(),
                                        var8,
                                        var7,
                                        var2.getDamageValue(),
                                        var2.getMaxDamage()
                                    }
                                );
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        if (FoodWeaponData.read(var0.getItemStack(), "FTBossWeaponMaterials") != null) {
            if (SwordCavityBuild.applies(var0.getItemStack())) {
                List<Cell> var7 = SwordCavityBuild.cells(var0.getItemStack());
                var0.getToolTip()
                    .add(
                        Component.translatable(
                                "tooltip.ftboss.cavity.summary",
                                var7.stream().filter(var0x -> var0x.installed()).count(),
                                SwordCavityBuild.links(var0.getItemStack()).size()
                            )
                            .withStyle(ChatFormatting.GRAY)
                    );
                if (SwordCavityBuild.missingRequired(var0.getItemStack())) {
                    var0.getToolTip().add(Component.translatable("tooltip.ftboss.sword.structure_missing").withStyle(ChatFormatting.RED));
                }
            } else {
                Build var1 = build(var0.getItemStack());
                var0.getToolTip()
                    .add(
                        Component.translatable(
                                "screen.ftboss.material.stats",
                                Math.round(var1.damage() * 100.0),
                                Math.round(var1.speed() * 100.0),
                                Math.round(var1.wear() * 100.0),
                                String.format(Locale.ROOT, "%.2f", var1.reach())
                            )
                            .withStyle(ChatFormatting.GOLD)
                    );
                List<Material> var2 = materials(var0.getItemStack());

                for (Part var6 : Part.values()) {
                    var0.getToolTip()
                        .add(
                            Component.translatable("screen.ftboss.material.part", partName(var6), materialName((Material)var2.get(var6.ordinal())))
                                .withStyle(ChatFormatting.GRAY)
                        );
                }

                EnumSet var8 = EnumSet.noneOf(Material.class);

                for (Material var10 : var2) {
                    if (var10 != Material.ORIGINAL && var8.add(var10)) {
                        var0.getToolTip().add(passive(var10).copy().withStyle(ChatFormatting.DARK_AQUA));
                    }
                }

                var0.getToolTip().add(Component.translatable("tooltip.ftboss.material.unique").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    public static int durabilityCost(ItemStack var0, int var1) {
        return var1;
    }

    public static boolean replaceFromUi(ServerPlayer var0, ItemStack var1, int var2) {
        if (var0.getMainHandItem() != var1 && var0.getOffhandItem() != var1) {
            return false;
        } else if (FourWeaponFoundationAdapter.applies(var1)) {
            return false;
        } else if (SwordCavityBuild.applies(var1)) {
            return false;
        } else if (!var0.isSpectator() && var0.isAlive() && var2 >= 0 && var2 < Part.values().length * 36) {
            Part var3 = Part.byIndex(var2 / 36);
            int var4 = var2 % 36;
            ItemStack var5 = var0.getInventory().getItem(var4);
            Material var6 = materialFor(var5);
            if (!var1.isEmpty()
                && !FoodWeaponLifecycle.isBroken(var1)
                && var5 != var1
                && var6 != Material.ORIGINAL
                && materials(var1).get(var3.ordinal()) != var6) {
                int var7 = var1.getMaxDamage();
                int var8 = var1.getDamageValue();
                if (var7 > 0 && var8 >= var7) {
                    return false;
                } else {
                    ItemStack var9 = var1.copy();
                    FoodWeaponData.edit(var9, "FTBossWeaponMaterials", data -> data.putString(var3.name(), var6.id()));
                    int var10 = var9.getMaxDamage();
                    if (var7 > 0 && var8 >= var10) {
                        var0.displayClientMessage(Component.translatable("message.ftboss.material.repair_first"), true);
                        return false;
                    } else {
                        NativeFoodWeaponWear.manage(var0, var1);
                        FoodWeaponLifecycle.ensureTracked(var1);
                        FoodWeaponData.edit(var1, "FTBossWeaponMaterials", data -> data.putString(var3.name(), var6.id()));
                        if (var7 > 0) {
                            FoodWeaponLifecycle.materialDamage(var1, var8);
                        }

                        if (!var0.getAbilities().instabuild) {
                            var5.shrink(1);
                        }

                        var0.getInventory().setChanged();
                        var0.containerMenu.broadcastChanges();
                        var0.sendSystemMessage(
                            Component.translatable("message.ftboss.material.replaced", FoodWeaponFamilyBuild.partName(var1, var3), materialName(var6))
                                .withStyle(ChatFormatting.GOLD)
                        );
                        FTBossMod.LOGGER
                            .info(
                                "[FTBoss][材料构成] {} 替换 {} 的 {} 为 {}，耐久 {}/{} -> {}/{}",
                                new Object[]{
                                    var0.getName().getString(),
                                    var1.getHoverName().getString(),
                                    var3.name(),
                                    var6.id(),
                                    var8,
                                    var7,
                                    var1.getDamageValue(),
                                    var1.getMaxDamage()
                                }
                            );
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    static void applyClosedBaseProc(
        ServerPlayer var0, LivingEntity var1, List<Material> var2, FoodWeaponHitPlan.FinalEffectsCommit var3, FoodWeaponFinalDamage.ChildHurtBridge var4
    ) {
        if (FoodWeaponHitPlan.leafLive(var3, var4)) {
            String var5 = "FTBossMaterialProc";
            long var6 = var0.serverLevel().getGameTime();
            CompoundTag var8 = var0.getPersistentData();
            if (!var8.contains(var5) || var6 - var8.getLong(var5) >= 100L) {
                var8.putLong(var5, var6);
                if (FoodWeaponHitPlan.leafLive(var3, var4)) {
                    if (var2.contains(Material.COPPER)) {
                        var1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), var0);
                        if (!FoodWeaponHitPlan.leafLive(var3, var4)) {
                            return;
                        }
                    }

                    if (var2.contains(Material.BONE)) {
                        var1.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0), var0);
                        if (!FoodWeaponHitPlan.leafLive(var3, var4)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    public static FoodWeaponHitPlan quoteFinalHit(BeforeMitigation var0, FoodWeaponBondHits.Hit var1) {
        FoodWeaponHitPlan.Builder var2 = new FoodWeaponHitPlan.Builder(var0.getAmount());
        if (var1 != null
            && !var0.isCanceled()
            && !FoodWeaponBondHits.secondary(var0.getSource())
            && Float.isFinite(var0.getAmount())
            && !(var0.getAmount() <= 0.0F)
            && var0.getSource().getEntity() instanceof ServerPlayer var3) {
            LivingEntity var8 = var0.getEntity();
            if (var8 != var3 && FoodWeaponBondHits.hostile(var3, var8)) {
                List var5 = materials(var1.weapon());
                if (var5.isEmpty()) {
                    return var2.build();
                } else {
                    if (var5.contains(Material.NETHERITE) && var8.getHealth() <= var8.getMaxHealth() * 0.3F) {
                        var2.multiply(1.15F);
                    }

                    if (var5.contains(Material.DIAMOND)) {
                        var2.multiply(1.05F);
                    }

                    float var6 = MaterialHitPlanCompatibility.quoteHit(var8, var3, var1.weapon(), var5, var2.amount());
                    var2 = new FoodWeaponHitPlan.Builder(var6);
                    var2.requirePrimaryPayment();
                    if (var5.contains(Material.BLAZE)) {
                        var2.fire(null, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, var8, 3);
                    }

                    if (var5.contains(Material.STONE)) {
                        var2.materialKnockback(var3, var8);
                    }

                    if (var5.contains(Material.COPPER) || var5.contains(Material.BONE)) {
                        var2.materialBaseProc(var3, var8, var5);
                    }

                    MaterialHitPlanCompatibility.quoteFinalHitEffects(var2, var0, var3, var5);
                    return var2.build();
                }
            } else {
                return var2.build();
            }
        } else {
            return var2.build();
        }
    }

    public static int maxDurability(ItemStack var0, int var1) {
        int var2 = FoodWeaponFamilyBuild.baseDurability(var0, var1);
        return WeaponDurabilityBuild.maxDurability(var0, var2);
    }

    public static record CombatStats(double damage, double speed, double reach, int durability, double wear) {
    }
}
