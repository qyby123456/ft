/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.common.ForgeMod
 *  net.minecraftforge.event.ItemAttributeModifierEvent
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
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
import net.farmingtales.ftboss.item.FoodWeaponBondHits;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeFoodWeaponWear;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.item.WeaponDurabilityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.item.WeaponMaterialPowers;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class WeaponMaterialBuild {
    public static final String ROOT = "FTBossWeaponMaterials";
    private static final UUID DAMAGE_ID = UUID.fromString("894c7a17-bf58-492f-ae96-d21abeb6a811");
    private static final UUID SPEED_ID = UUID.fromString("bceec5f0-ffb7-4449-a91c-98a9a22de2e3");
    private static final UUID REACH_ID = UUID.fromString("712d0686-c0bd-4e7f-9a4f-cedd24c6bfaf");

    private WeaponMaterialBuild() {
    }

    public static FoodWeaponBuildRules.Material materialFor(ItemStack itemStack) {
        return WeaponMaterialCatalog.materialFor(itemStack);
    }

    public static List<FoodWeaponBuildRules.Material> attackMaterials(DamageSource damageSource) {
        Entity entity = damageSource.m_7639_();
        if (!(entity instanceof ServerPlayer)) {
            return List.of();
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity;
        if (damageSource.m_7640_() == serverPlayer && damageSource.m_276093_(DamageTypes.f_268464_)) {
            return FoodWeaponLifecycle.isBroken(serverPlayer.m_21205_()) ? List.of() : WeaponMaterialBuild.materials(serverPlayer.m_21205_());
        }
        Entity entity2 = damageSource.m_7640_();
        if (entity2 instanceof AbstractArrow && (entity = (AbstractArrow)entity2).m_19749_() == serverPlayer && damageSource.m_269533_(DamageTypeTags.f_268524_)) {
            entity2 = entity.getPersistentData().m_128469_(ROOT);
            if (entity2.m_128456_()) {
                return List.of();
            }
            ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>();
            for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
                arrayList.add(FoodWeaponBuildRules.Material.byId(entity2.m_128461_(part.name())));
            }
            return List.copyOf(arrayList);
        }
        return List.of();
    }

    public static Component materialName(FoodWeaponBuildRules.Material material) {
        return Component.m_237115_((String)("material.ftboss.weapon." + material.id()));
    }

    public static Component partName(FoodWeaponBuildRules.Part part) {
        return Component.m_237115_((String)("part.ftboss.weapon." + part.name().toLowerCase(Locale.ROOT)));
    }

    public static Component passive(FoodWeaponBuildRules.Material material) {
        return Component.m_237115_((String)("passive.ftboss.material." + material.id()));
    }

    public static int previewDurability(ItemStack itemStack, FoodWeaponBuildRules.Build build) {
        return FoodWeaponBuildRules.maxDurability(itemStack.m_41720_().getMaxDamage(itemStack), build);
    }

    public static CombatStats combatStats(ItemStack itemStack) {
        return new CombatStats(WeaponMaterialBuild.itemAttribute(itemStack, Attributes.f_22281_, 1.0), WeaponMaterialBuild.itemAttribute(itemStack, Attributes.f_22283_, 4.0), WeaponMaterialBuild.itemAttribute(itemStack, (Attribute)ForgeMod.ENTITY_REACH.get(), ((Attribute)ForgeMod.ENTITY_REACH.get()).m_22082_()), itemStack.m_41776_(), WeaponMaterialBuild.build(itemStack).wear());
    }

    private static double itemAttribute(ItemStack itemStack, Attribute attribute, double d) {
        Collection collection = itemStack.m_41638_(EquipmentSlot.MAINHAND).get((Object)attribute);
        double d2 = d;
        for (AttributeModifier attributeModifier : collection) {
            if (attributeModifier.m_22217_() != AttributeModifier.Operation.ADDITION) continue;
            d2 += attributeModifier.m_22218_();
        }
        double d3 = d2;
        for (AttributeModifier attributeModifier : collection) {
            if (attributeModifier.m_22217_() != AttributeModifier.Operation.MULTIPLY_BASE) continue;
            d3 += d2 * attributeModifier.m_22218_();
        }
        for (AttributeModifier attributeModifier : collection) {
            if (attributeModifier.m_22217_() != AttributeModifier.Operation.MULTIPLY_TOTAL) continue;
            d3 *= 1.0 + attributeModifier.m_22218_();
        }
        return attribute.m_6740_(d3);
    }

    private static double originalWeaponAttribute(ItemAttributeModifierEvent itemAttributeModifierEvent, Attribute attribute, double d) {
        double d2 = d;
        for (AttributeModifier attributeModifier : itemAttributeModifierEvent.getOriginalModifiers().get((Object)attribute)) {
            if (attributeModifier.m_22217_() != AttributeModifier.Operation.ADDITION) continue;
            d2 += attributeModifier.m_22218_();
        }
        return Math.max(0.0, d2);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void attributes(ItemAttributeModifierEvent itemAttributeModifierEvent) {
        ItemStack itemStack = itemAttributeModifierEvent.getItemStack();
        if (itemAttributeModifierEvent.getSlotType() != EquipmentSlot.MAINHAND || itemStack.m_41737_(ROOT) == null || FoodWeaponLifecycle.isBroken(itemStack)) {
            return;
        }
        FoodWeaponBuildRules.Build build = WeaponMaterialBuild.build(itemStack);
        double d = WeaponMaterialBuild.originalWeaponAttribute(itemAttributeModifierEvent, Attributes.f_22281_, 1.0) * (build.damage() - 1.0);
        double d2 = WeaponMaterialBuild.originalWeaponAttribute(itemAttributeModifierEvent, Attributes.f_22283_, 4.0) * (build.speed() - 1.0);
        if (Math.abs(d) > 1.0E-5) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22281_, new AttributeModifier(DAMAGE_ID, "ftboss.material.damage", d, AttributeModifier.Operation.ADDITION));
        }
        if (Math.abs(d2) > 1.0E-5) {
            itemAttributeModifierEvent.addModifier(Attributes.f_22283_, new AttributeModifier(SPEED_ID, "ftboss.material.speed", d2, AttributeModifier.Operation.ADDITION));
        }
        if (build.reach() > 0.0) {
            itemAttributeModifierEvent.addModifier((Attribute)ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_ID, "ftboss.material.reach", build.reach(), AttributeModifier.Operation.ADDITION));
        }
    }

    @SubscribeEvent
    public static void projectileLaunch(EntityJoinLevelEvent entityJoinLevelEvent) {
        ServerPlayer serverPlayer;
        AbstractArrow abstractArrow;
        Entity entity;
        block6: {
            block5: {
                if (entityJoinLevelEvent.getLevel().f_46443_ || !((entity = entityJoinLevelEvent.getEntity()) instanceof AbstractArrow) || !((entity = (abstractArrow = (AbstractArrow)entity).m_19749_()) instanceof ServerPlayer)) break block5;
                serverPlayer = (ServerPlayer)entity;
                if (!abstractArrow.getPersistentData().m_128425_(ROOT, 10)) break block6;
            }
            return;
        }
        entity = serverPlayer.m_21211_();
        if (entity.m_41619_()) {
            entity = serverPlayer.m_21205_();
        }
        if (entity.m_41737_(ROOT) == null) {
            return;
        }
        CompoundTag compoundTag = entity.m_41737_(ROOT).m_6426_();
        compoundTag.m_128347_("ProjectileDamage", WeaponMaterialBuild.build((ItemStack)entity).damage());
        WeaponMaterialPowers.snapshot((ItemStack)entity, compoundTag);
        abstractArrow.getPersistentData().m_128365_(ROOT, (Tag)compoundTag);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void hit(LivingHurtEvent livingHurtEvent) {
        Object object;
        Entity entity;
        if (livingHurtEvent.getAmount() <= 0.0f || !((entity = livingHurtEvent.getSource().m_7639_()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity;
        entity = livingHurtEvent.getEntity();
        if (entity == serverPlayer || entity.m_7307_((Entity)serverPlayer)) {
            return;
        }
        List<FoodWeaponBuildRules.Material> list = WeaponMaterialBuild.attackMaterials(livingHurtEvent.getSource());
        if (list.isEmpty()) {
            return;
        }
        Entity entity2 = livingHurtEvent.getSource().m_7640_();
        if (entity2 instanceof AbstractArrow) {
            object = (AbstractArrow)entity2;
            double d = (entity2 = object.getPersistentData().m_128469_(ROOT)).m_128459_("ProjectileDamage");
            double d2 = Double.isFinite(d) && d > 0.0 ? Math.max(0.5, Math.min(2.0, d)) : 1.0;
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * (float)d2);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.NETHERITE) && entity.m_21223_() <= entity.m_21233_() * 0.3f) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 1.15f);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.DIAMOND)) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 1.05f);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BLAZE)) {
            entity.m_20254_(3);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.STONE)) {
            entity.m_147240_(0.2, serverPlayer.m_20185_() - entity.m_20185_(), serverPlayer.m_20189_() - entity.m_20189_());
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.COPPER) || list.contains((Object)FoodWeaponBuildRules.Material.BONE)) {
            object = "FTBossMaterialProc";
            long l = serverPlayer.m_284548_().m_46467_();
            CompoundTag compoundTag = serverPlayer.getPersistentData();
            if (!compoundTag.m_128441_((String)object) || l - compoundTag.m_128454_((String)object) >= 100L) {
                compoundTag.m_128356_((String)object, l);
                if (list.contains((Object)FoodWeaponBuildRules.Material.COPPER)) {
                    entity.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 40, 0), (Entity)serverPlayer);
                }
                if (list.contains((Object)FoodWeaponBuildRules.Material.BONE)) {
                    entity.m_147207_(new MobEffectInstance(MobEffects.f_19615_, 40, 0), (Entity)serverPlayer);
                }
            }
        }
        WeaponMaterialPowers.hit(livingHurtEvent, serverPlayer, list);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void guard(LivingHurtEvent livingHurtEvent) {
        Object object;
        if (livingHurtEvent.getAmount() <= 0.0f || !((object = livingHurtEvent.getEntity()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object;
        if (FoodWeaponLifecycle.isBroken(serverPlayer.m_21205_())) {
            return;
        }
        object = WeaponMaterialBuild.materials(serverPlayer.m_21205_());
        if (object.contains((Object)FoodWeaponBuildRules.Material.OBSIDIAN)) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 0.9f);
        }
        if (object.contains((Object)FoodWeaponBuildRules.Material.LEATHER) && serverPlayer.m_20142_() && livingHurtEvent.getSource().m_7640_() instanceof AbstractArrow) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 0.9f);
        }
        if (object.contains((Object)FoodWeaponBuildRules.Material.IRON) && serverPlayer.m_21223_() <= serverPlayer.m_21233_() * 0.3f) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 0.92f);
        }
        WeaponMaterialPowers.guard(livingHurtEvent, serverPlayer, (List<FoodWeaponBuildRules.Material>)object);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void kill(LivingDeathEvent livingDeathEvent) {
        Object object;
        if (!(livingDeathEvent.getEntity() instanceof Monster) || LemonSummonRewards.suppressed((Entity)livingDeathEvent.getEntity()) || !((object = livingDeathEvent.getSource().m_7639_()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object;
        object = WeaponMaterialBuild.attackMaterials(livingDeathEvent.getSource());
        if (object.isEmpty()) {
            return;
        }
        WeaponMaterialPowers.kill(serverPlayer, (List<FoodWeaponBuildRules.Material>)object, livingDeathEvent.getSource());
        if (object.contains((Object)FoodWeaponBuildRules.Material.GOLD)) {
            serverPlayer.m_6756_(1);
        }
        long l = serverPlayer.m_284548_().m_46467_();
        if ((object.contains((Object)FoodWeaponBuildRules.Material.EMERALD) || object.contains((Object)FoodWeaponBuildRules.Material.WOOD)) && l - serverPlayer.getPersistentData().m_128454_("FTBossMaterialKillHeal") >= 60L) {
            serverPlayer.getPersistentData().m_128356_("FTBossMaterialKillHeal", l);
            serverPlayer.m_5634_(object.contains((Object)FoodWeaponBuildRules.Material.EMERALD) ? 1.0f : 0.5f);
        }
    }

    @SubscribeEvent
    public static void materialClone(PlayerEvent.Clone clone) {
        WeaponMaterialPowers.clonePlayer(clone);
    }

    public static List<FoodWeaponBuildRules.Material> materials(ItemStack itemStack) {
        if (WeaponAttachmentPolicy.excluded(itemStack)) {
            return Collections.nCopies(FoodWeaponBuildRules.Part.values().length, FoodWeaponBuildRules.Material.ORIGINAL);
        }
        if (SwordCavityBuild.applies(itemStack)) {
            ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>(SwordCavityBuild.legacyMaterials(itemStack));
            for (FoodWeaponBuildRules.Material material : SwordCavityBuild.powerMaterials(itemStack)) {
                if (arrayList.contains((Object)material)) continue;
                arrayList.add(material);
            }
            return List.copyOf(arrayList);
        }
        CompoundTag compoundTag = itemStack.m_41737_(ROOT);
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>();
        for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
            arrayList.add(compoundTag == null ? FoodWeaponBuildRules.Material.ORIGINAL : FoodWeaponBuildRules.Material.byId(compoundTag.m_128461_(part.name())));
        }
        return List.copyOf(arrayList);
    }

    public static FoodWeaponBuildRules.Build build(ItemStack itemStack) {
        return SwordCavityBuild.applies(itemStack) ? SwordCavityBuild.build(itemStack) : FoodWeaponBuildRules.build(WeaponMaterialBuild.materials(itemStack));
    }

    public static FoodWeaponBuildRules.Build preview(ItemStack itemStack, FoodWeaponBuildRules.Part part, FoodWeaponBuildRules.Material material) {
        if (SwordCavityBuild.applies(itemStack)) {
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                if (slot.legacyPart != part) continue;
                return SwordCavityBuild.build(SwordCavityBuild.preview(itemStack, slot, material));
            }
        }
        ArrayList<FoodWeaponBuildRules.Material> arrayList = new ArrayList<FoodWeaponBuildRules.Material>(WeaponMaterialBuild.materials(itemStack));
        arrayList.set(part.ordinal(), material);
        return FoodWeaponBuildRules.build((List<FoodWeaponBuildRules.Material>)arrayList);
    }

    public static CombatStats combatPreview(ItemStack itemStack, FoodWeaponBuildRules.Part part, FoodWeaponBuildRules.Material material) {
        if (SwordCavityBuild.applies(itemStack)) {
            for (ItemStack itemStack2 : SwordCavityRules.Slot.values()) {
                if (itemStack2.legacyPart != part) continue;
                return WeaponMaterialBuild.combatStats(SwordCavityBuild.preview(itemStack, (SwordCavityRules.Slot)itemStack2, material));
            }
        }
        ItemStack itemStack3 = itemStack.m_41777_();
        itemStack3.m_41698_(ROOT).m_128359_(part.name(), material.id());
        return WeaponMaterialBuild.combatStats(itemStack3);
    }

    public static boolean replaceFromUi(ServerPlayer serverPlayer, int n) {
        ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
        if (WeaponAttachmentPolicy.excluded(itemStack)) {
            return false;
        }
        if (SwordCavityBuild.applies(itemStack) || FourWeaponFoundationAdapter.applies(itemStack)) {
            return false;
        }
        if (serverPlayer.m_5833_() || !serverPlayer.m_6084_() || n < 0 || n >= FoodWeaponBuildRules.Part.values().length * 36) {
            return false;
        }
        FoodWeaponBuildRules.Part part = FoodWeaponBuildRules.Part.byIndex(n / 36);
        int n2 = n % 36;
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n2);
        FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(itemStack2);
        if (itemStack.m_41619_() || FoodWeaponLifecycle.isBroken(itemStack) || itemStack2 == itemStack || material == FoodWeaponBuildRules.Material.ORIGINAL || WeaponMaterialBuild.materials(itemStack).get(part.ordinal()) == material) {
            return false;
        }
        int n3 = itemStack.m_41776_();
        int n4 = itemStack.m_41773_();
        if (n3 > 0 && n4 >= n3) {
            return false;
        }
        ItemStack itemStack3 = itemStack.m_41777_();
        itemStack3.m_41698_(ROOT).m_128359_(part.name(), material.id());
        int n5 = itemStack3.m_41776_();
        int n6 = FoodWeaponBuildRules.preserveDamage(n4, n3, n5);
        if (n3 > 0 && n6 >= n5) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.material.repair_first"), true);
            return false;
        }
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        compoundTag.m_128359_(part.name(), material.id());
        if (n3 > 0) {
            FoodWeaponLifecycle.materialDamage(itemStack, n6);
        }
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack2.m_41774_(1);
        }
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36096_.m_38946_();
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.material.replaced", (Object[])new Object[]{FoodWeaponFamilyBuild.partName(itemStack, part), WeaponMaterialBuild.materialName(material)}).m_130940_(ChatFormatting.GOLD));
        FTBossMod.LOGGER.info("[FTBoss][\u6750\u6599\u6784\u6210] {} \u66ff\u6362 {} \u7684 {} \u4e3a {}\uff0c\u8010\u4e45 {}/{} -> {}/{}", new Object[]{serverPlayer.m_7755_().getString(), itemStack.m_41786_().getString(), part.name(), material.id(), n4, n3, itemStack.m_41773_(), itemStack.m_41776_()});
        return true;
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        if (itemTooltipEvent.getItemStack().m_41737_(ROOT) == null) {
            return;
        }
        if (SwordCavityBuild.applies(itemTooltipEvent.getItemStack())) {
            List<SwordCavityRules.Cell> list = SwordCavityBuild.cells(itemTooltipEvent.getItemStack());
            itemTooltipEvent.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.cavity.summary", (Object[])new Object[]{list.stream().filter(cell -> cell.installed()).count(), SwordCavityBuild.links(itemTooltipEvent.getItemStack()).size()}).m_130940_(ChatFormatting.GRAY));
            if (SwordCavityBuild.missingRequired(itemTooltipEvent.getItemStack())) {
                itemTooltipEvent.getToolTip().add(Component.m_237115_((String)"tooltip.ftboss.sword.structure_missing").m_130940_(ChatFormatting.RED));
            }
            return;
        }
        FoodWeaponBuildRules.Build build = WeaponMaterialBuild.build(itemTooltipEvent.getItemStack());
        itemTooltipEvent.getToolTip().add(Component.m_237110_((String)"screen.ftboss.material.stats", (Object[])new Object[]{Math.round(build.damage() * 100.0), Math.round(build.speed() * 100.0), Math.round(build.wear() * 100.0), String.format(Locale.ROOT, "%.2f", build.reach())}).m_130940_(ChatFormatting.GOLD));
        List<FoodWeaponBuildRules.Material> list = WeaponMaterialBuild.materials(itemTooltipEvent.getItemStack());
        for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
            itemTooltipEvent.getToolTip().add(Component.m_237110_((String)"screen.ftboss.material.part", (Object[])new Object[]{WeaponMaterialBuild.partName(part), WeaponMaterialBuild.materialName(list.get(part.ordinal()))}).m_130940_(ChatFormatting.GRAY));
        }
        EnumSet<FoodWeaponBuildRules.Material> enumSet = EnumSet.noneOf(FoodWeaponBuildRules.Material.class);
        for (FoodWeaponBuildRules.Material material : list) {
            if (material == FoodWeaponBuildRules.Material.ORIGINAL || !enumSet.add(material)) continue;
            itemTooltipEvent.getToolTip().add(WeaponMaterialBuild.passive(material).m_6881_().m_130940_(ChatFormatting.DARK_AQUA));
        }
        itemTooltipEvent.getToolTip().add(Component.m_237115_((String)"tooltip.ftboss.material.unique").m_130940_(ChatFormatting.DARK_GRAY));
    }

    public static int durabilityCost(ItemStack itemStack, int n) {
        return n;
    }

    public static boolean replaceFromUi(ServerPlayer serverPlayer, ItemStack itemStack, int n) {
        if (serverPlayer.m_21205_() != itemStack && serverPlayer.m_21206_() != itemStack) {
            return false;
        }
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            return false;
        }
        if (SwordCavityBuild.applies(itemStack)) {
            return false;
        }
        if (serverPlayer.m_5833_() || !serverPlayer.m_6084_() || n < 0 || n >= FoodWeaponBuildRules.Part.values().length * 36) {
            return false;
        }
        FoodWeaponBuildRules.Part part = FoodWeaponBuildRules.Part.byIndex(n / 36);
        int n2 = n % 36;
        ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(n2);
        FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(itemStack2);
        if (itemStack.m_41619_() || FoodWeaponLifecycle.isBroken(itemStack) || itemStack2 == itemStack || material == FoodWeaponBuildRules.Material.ORIGINAL || WeaponMaterialBuild.materials(itemStack).get(part.ordinal()) == material) {
            return false;
        }
        int n3 = itemStack.m_41776_();
        int n4 = itemStack.m_41773_();
        if (n3 > 0 && n4 >= n3) {
            return false;
        }
        ItemStack itemStack3 = itemStack.m_41777_();
        itemStack3.m_41698_(ROOT).m_128359_(part.name(), material.id());
        int n5 = itemStack3.m_41776_();
        int n6 = n4;
        if (n3 > 0 && n6 >= n5) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.material.repair_first"), true);
            return false;
        }
        NativeFoodWeaponWear.manage(serverPlayer, itemStack);
        FoodWeaponLifecycle.ensureTracked(itemStack);
        CompoundTag compoundTag = itemStack.m_41698_(ROOT);
        compoundTag.m_128359_(part.name(), material.id());
        if (n3 > 0) {
            FoodWeaponLifecycle.materialDamage(itemStack, n6);
        }
        if (!serverPlayer.m_150110_().f_35937_) {
            itemStack2.m_41774_(1);
        }
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36096_.m_38946_();
        serverPlayer.m_213846_((Component)Component.m_237110_((String)"message.ftboss.material.replaced", (Object[])new Object[]{FoodWeaponFamilyBuild.partName(itemStack, part), WeaponMaterialBuild.materialName(material)}).m_130940_(ChatFormatting.GOLD));
        FTBossMod.LOGGER.info("[FTBoss][\u6750\u6599\u6784\u6210] {} \u66ff\u6362 {} \u7684 {} \u4e3a {}\uff0c\u8010\u4e45 {}/{} -> {}/{}", new Object[]{serverPlayer.m_7755_().getString(), itemStack.m_41786_().getString(), part.name(), material.id(), n4, n3, itemStack.m_41773_(), itemStack.m_41776_()});
        return true;
    }

    static void applyClosedBaseProc(ServerPlayer serverPlayer, LivingEntity livingEntity, List<FoodWeaponBuildRules.Material> list, FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        String string = "FTBossMaterialProc";
        long l = serverPlayer.m_284548_().m_46467_();
        CompoundTag compoundTag = serverPlayer.getPersistentData();
        if (compoundTag.m_128441_(string) && l - compoundTag.m_128454_(string) < 100L) {
            return;
        }
        compoundTag.m_128356_(string, l);
        if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
            return;
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.COPPER)) {
            livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 40, 0), (Entity)serverPlayer);
            if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                return;
            }
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.BONE)) {
            livingEntity.m_147207_(new MobEffectInstance(MobEffects.f_19615_, 40, 0), (Entity)serverPlayer);
            if (!FoodWeaponHitPlan.leafLive(finalEffectsCommit, childHurtBridge)) {
                return;
            }
        }
    }

    public static FoodWeaponHitPlan quoteFinalHit(LivingHurtEvent livingHurtEvent, FoodWeaponBondHits.Hit hit) {
        Entity entity;
        FoodWeaponHitPlan.Builder builder = new FoodWeaponHitPlan.Builder(livingHurtEvent.getAmount());
        if (hit == null || livingHurtEvent.isCanceled() || FoodWeaponBondHits.secondary(livingHurtEvent.getSource()) || !Float.isFinite(livingHurtEvent.getAmount()) || livingHurtEvent.getAmount() <= 0.0f || !((entity = livingHurtEvent.getSource().m_7639_()) instanceof ServerPlayer)) {
            return builder.build();
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity;
        entity = livingHurtEvent.getEntity();
        if (entity == serverPlayer || !FoodWeaponBondHits.hostile(serverPlayer, (LivingEntity)entity)) {
            return builder.build();
        }
        List<FoodWeaponBuildRules.Material> list = WeaponMaterialBuild.materials(hit.weapon());
        if (list.isEmpty()) {
            return builder.build();
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.NETHERITE) && entity.m_21223_() <= entity.m_21233_() * 0.3f) {
            builder.multiply(1.15f);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.DIAMOND)) {
            builder.multiply(1.05f);
        }
        float f = WeaponMaterialPowers.quoteHit((LivingEntity)entity, (ServerPlayer)serverPlayer, (ItemStack)hit.weapon(), list, (float)builder.amount());
        builder = new FoodWeaponHitPlan.Builder(f);
        builder.requirePrimaryPayment();
        if (list.contains((Object)FoodWeaponBuildRules.Material.BLAZE)) {
            builder.fire(null, FoodWeaponSourceLedger.TargetEffectDomain.MATERIAL_DAMAGE, (LivingEntity)entity, 3);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.STONE)) {
            builder.materialKnockback(serverPlayer, (LivingEntity)entity);
        }
        if (list.contains((Object)FoodWeaponBuildRules.Material.COPPER) || list.contains((Object)FoodWeaponBuildRules.Material.BONE)) {
            builder.materialBaseProc(serverPlayer, (LivingEntity)entity, list);
        }
        WeaponMaterialPowers.quoteFinalHitEffects((FoodWeaponHitPlan.Builder)builder, (LivingHurtEvent)livingHurtEvent, (ServerPlayer)serverPlayer, list);
        return builder.build();
    }

    public static int maxDurability(ItemStack itemStack, int n) {
        int n2 = FoodWeaponFamilyBuild.baseDurability(itemStack, n);
        return WeaponDurabilityBuild.maxDurability(itemStack, n2);
    }

    public record CombatStats(double damage, double speed, double reach, int durability, double wear) {
    }
}

