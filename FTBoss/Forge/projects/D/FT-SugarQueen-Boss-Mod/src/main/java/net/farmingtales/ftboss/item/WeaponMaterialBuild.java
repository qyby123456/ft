package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 食武三部件改装；材料、损耗余量及耐久比例保存在原物品，2026-10-12 新增材料 UI 与真实属性。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class WeaponMaterialBuild {
    public static final String ROOT = "FTBossWeaponMaterials";
    private static final UUID DAMAGE_ID = UUID.fromString("894c7a17-bf58-492f-ae96-d21abeb6a811");
    private static final UUID SPEED_ID = UUID.fromString("bceec5f0-ffb7-4449-a91c-98a9a22de2e3");
    private static final UUID REACH_ID = UUID.fromString("712d0686-c0bd-4e7f-9a4f-cedd24c6bfaf");

    private WeaponMaterialBuild() {}

    public static Material materialFor(ItemStack stack) {
        return WeaponMaterialCatalog.materialFor(stack);
    }

    public static List<Material> materials(ItemStack stack) {
        if (WeaponAttachmentPolicy.excluded(stack)) return java.util.Collections.nCopies(Part.values().length, Material.ORIGINAL);
        if (FourWeaponFoundationAdapter.applies(stack)) {
            List<Material> result = new ArrayList<>(java.util.Collections.nCopies(Part.values().length, Material.ORIGINAL));
            for (Material material : FourWeaponFoundationAdapter.powerMaterials(stack)) if (!result.contains(material)) result.add(material);
            return List.copyOf(result);
        }
        if (SwordCavityBuild.applies(stack)) {
            List<Material> result = new ArrayList<>(SwordCavityBuild.legacyMaterials(stack));
            for (Material material : SwordCavityBuild.powerMaterials(stack)) if (!result.contains(material)) result.add(material);
            return List.copyOf(result);
        }
        CompoundTag data = stack.getTagElement(ROOT);
        List<Material> result = new ArrayList<>();
        for (Part part : Part.values()) {
            result.add(data == null ? Material.ORIGINAL : Material.byId(data.getString(part.name())));
        }
        return List.copyOf(result);
    }

    public static List<Material> attackMaterials(DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return List.of();
        if (source.getDirectEntity() == player && source.is(DamageTypes.PLAYER_ATTACK)) {
            return FoodWeaponLifecycle.isBroken(player.getMainHandItem()) ? List.of() : materials(player.getMainHandItem());
        }
        return FoodWeaponRanged.attackMaterials(source, player);
    }

    public static Build build(ItemStack stack) {
        return SwordCavityBuild.applies(stack) ? SwordCavityBuild.build(stack) : FoodWeaponFamilyBuild.build(stack);
    }

    public static Build preview(ItemStack stack, Part part, Material material) {
        if (SwordCavityBuild.applies(stack)) {
            for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) if (slot.legacyPart == part) {
                return SwordCavityBuild.build(SwordCavityBuild.preview(stack, slot, material));
            }
        }
        return FoodWeaponFamilyBuild.materialPreview(stack, part, material);
    }

    public static Component materialName(Material material) {
        return Component.translatable("material.ftboss.weapon." + material.id());
    }

    public static Component partName(Part part) {
        return Component.translatable("part.ftboss.weapon." + part.name().toLowerCase(java.util.Locale.ROOT));
    }

    public static Component passive(Material material) {
        return Component.translatable("passive.ftboss.material." + material.id());
    }

    public static int maxDurability(ItemStack stack, int original) {
        int base = FoodWeaponFamilyBuild.baseDurability(stack, original);
        return WeaponDurabilityBuild.maxDurability(stack, base);
    }

    public static int durabilityCost(ItemStack stack, int requested) {
        return EquipmentWeaponBuild.applies(stack) ? EquipmentWeaponBuild.durabilityCost(stack, requested) : requested;
    }

    public static int previewDurability(ItemStack stack, Build prospective) {
        return FoodWeaponBuildRules.maxDurability(FoodWeaponFamilyBuild.baseDurability(stack,
                stack.getItem().getMaxDamage(stack)), prospective);
    }

    public record CombatStats(double damage, double speed, double reach, int durability, double wear) {}

    public static CombatStats combatStats(ItemStack stack) {
        return new CombatStats(itemAttribute(stack, Attributes.ATTACK_DAMAGE, 1.0),
                itemAttribute(stack, Attributes.ATTACK_SPEED, 4.0),
                itemAttribute(stack, ForgeMod.ENTITY_REACH.get(), ForgeMod.ENTITY_REACH.get().getDefaultValue()),
                stack.getMaxDamage(), build(stack).wear());
    }

    public static CombatStats combatPreview(ItemStack stack, Part part, Material material) {
        if (SwordCavityBuild.applies(stack)) {
            for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) if (slot.legacyPart == part) {
                return combatStats(SwordCavityBuild.preview(stack, slot, material));
            }
        }
        ItemStack prospective = stack.copy();
        prospective.getOrCreateTagElement(ROOT).putString(part.name(), material.id());
        return combatStats(prospective);
    }

    private static double itemAttribute(ItemStack stack, Attribute attribute, double base) {
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute);
        double added = base;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) added += modifier.getAmount();
        }
        double result = added;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) result += added * modifier.getAmount();
        }
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) result *= 1.0 + modifier.getAmount();
        }
        return attribute.sanitizeValue(result);
    }

    public static boolean replaceFromUi(ServerPlayer player, int encoded) {
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        if (WeaponAttachmentPolicy.excluded(weapon)) return false;
        if (SwordCavityBuild.applies(weapon) || FourWeaponFoundationAdapter.applies(weapon)) return false;
        if (player.isSpectator() || !player.isAlive() || encoded < 0 || encoded >= Part.values().length * 36) return false;
        Part part = Part.byIndex(encoded / 36);
        int inventorySlot = encoded % 36;
        ItemStack offering = player.getInventory().getItem(inventorySlot);
        Material material = materialFor(offering);
        if (weapon.isEmpty() || FoodWeaponLifecycle.isBroken(weapon) || offering == weapon || material == Material.ORIGINAL
                || materials(weapon).get(part.ordinal()) == material) return false;
        int oldMax = weapon.getMaxDamage();
        int oldDamage = weapon.getDamageValue();
        if (oldMax > 0 && oldDamage >= oldMax) return false;
        ItemStack prospective = weapon.copy();
        prospective.getOrCreateTagElement(ROOT).putString(part.name(), material.id());
        int newMax = prospective.getMaxDamage();
        int newDamage = FoodWeaponBuildRules.preserveDamage(oldDamage, oldMax, newMax);
        if (oldMax > 0 && newDamage >= newMax) {
            player.displayClientMessage(Component.translatable("message.ftboss.material.repair_first"), true);
            return false;
        }
        FoodWeaponLifecycle.ensureTracked(weapon);
        CompoundTag data = weapon.getOrCreateTagElement(ROOT);
        data.putString(part.name(), material.id());
        if (oldMax > 0) FoodWeaponLifecycle.materialDamage(weapon, newDamage);
        if (!player.getAbilities().instabuild) offering.shrink(1);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.translatable("message.ftboss.material.replaced",
                FoodWeaponFamilyBuild.partName(weapon, part), materialName(material))
                .withStyle(ChatFormatting.GOLD));
        FTBossMod.LOGGER.info("[FTBoss][材料构成] {} 替换 {} 的 {} 为 {}，耐久 {}/{} -> {}/{}",
                player.getName().getString(), weapon.getHoverName().getString(), part.name(), material.id(),
                oldDamage, oldMax, weapon.getDamageValue(), weapon.getMaxDamage());
        return true;
    }

    private static double originalWeaponAttribute(ItemAttributeModifierEvent event, Attribute attribute, double base) {
        double result = base;
        for (AttributeModifier modifier : event.getOriginalModifiers().get(attribute)) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) result += modifier.getAmount();
        }
        return Math.max(0.0, result);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void attributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!EquipmentAttributeSlots.heldItem(stack)) return;
        if (EquipmentWeaponBuild.applies(stack)) return;
        if (event.getSlotType() != EquipmentSlot.MAINHAND || stack.getTagElement(ROOT) == null && !FourWeaponFoundationAdapter.applies(stack)
                || FoodWeaponFamilyBuild.kind(stack) != net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind.MELEE
                || FoodWeaponLifecycle.isBroken(stack)) return;
        Build build = build(stack);
        double damage = originalWeaponAttribute(event, Attributes.ATTACK_DAMAGE, 1.0) * (build.damage() - 1.0);
        double speed = originalWeaponAttribute(event, Attributes.ATTACK_SPEED, 4.0) * (build.speed() - 1.0);
        if (Math.abs(damage) > 0.00001) event.addModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(DAMAGE_ID, "ftboss.material.damage", damage, AttributeModifier.Operation.ADDITION));
        if (Math.abs(speed) > 0.00001) event.addModifier(Attributes.ATTACK_SPEED,
                new AttributeModifier(SPEED_ID, "ftboss.material.speed", speed, AttributeModifier.Operation.ADDITION));
        if (build.reach() > 0) event.addModifier(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(REACH_ID, "ftboss.material.reach", build.reach(), AttributeModifier.Operation.ADDITION));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void projectileLaunch(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        FoodWeaponRanged.projectileJoin(event);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void hit(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();
        if (target == player || target.isAlliedTo(player)) return;
        List<Material> materials = attackMaterials(event.getSource());
        if (materials.isEmpty()) return;
        CompoundTag snapshot = FoodWeaponRanged.materialSnapshot(event.getSource(), player);
        if (snapshot.getString("Family").equals("BOW")) {
            double stored = ArrowPassiveDamage.remainingMaterial(snapshot.getDouble("ProjectileDamage"), event);
            double multiplier = Double.isFinite(stored) && stored > 0.0 ? Math.max(0.50, Math.min(2.0, stored)) : 1.0;
            event.setAmount(event.getAmount() * (float) multiplier);
        }
        if (materials.contains(Material.NETHERITE) && target.getHealth() <= target.getMaxHealth() * 0.30F) {
            event.setAmount(event.getAmount() * 1.15F);
        }
        if (materials.contains(Material.DIAMOND)) event.setAmount(event.getAmount() * 1.05F);
        if (materials.contains(Material.BLAZE)) target.setSecondsOnFire(3);
        if (materials.contains(Material.STONE)) target.knockback(0.20, player.getX() - target.getX(), player.getZ() - target.getZ());
        if (materials.contains(Material.COPPER) || materials.contains(Material.BONE)) {
            String key = "FTBossMaterialProc";
            long now = player.serverLevel().getGameTime();
            CompoundTag timers = player.getPersistentData();
            if (!timers.contains(key) || now - timers.getLong(key) >= 100) {
                timers.putLong(key, now);
                if (materials.contains(Material.COPPER)) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 0), player);
                if (materials.contains(Material.BONE)) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WITHER, 40, 0), player);
            }
        }
        WeaponMaterialPowers.hit(event, player, materials);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void guard(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (FoodWeaponLifecycle.isBroken(player.getMainHandItem())) return;
        List<Material> materials = materials(player.getMainHandItem());
        if (materials.contains(Material.OBSIDIAN)) event.setAmount(event.getAmount() * 0.90F);
        if (materials.contains(Material.LEATHER) && player.isSprinting()
                && event.getSource().getDirectEntity() instanceof AbstractArrow) event.setAmount(event.getAmount() * 0.90F);
        if (materials.contains(Material.IRON) && player.getHealth() <= player.getMaxHealth() * 0.30F) {
            event.setAmount(event.getAmount() * 0.92F);
        }
        WeaponMaterialPowers.guard(event, player, materials);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void kill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Monster) || LemonSummonRewards.suppressed(event.getEntity())
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        List<Material> materials = attackMaterials(event.getSource());
        if (materials.isEmpty()) return;
        WeaponMaterialPowers.kill(player, materials, event.getSource());
        if (materials.contains(Material.GOLD)) player.giveExperiencePoints(1);
        long now = player.serverLevel().getGameTime();
        if ((materials.contains(Material.EMERALD) || materials.contains(Material.WOOD))
                && now - player.getPersistentData().getLong("FTBossMaterialKillHeal") >= 60) {
            player.getPersistentData().putLong("FTBossMaterialKillHeal", now);
            player.heal(materials.contains(Material.EMERALD) ? 1.0F : 0.5F);
        }
    }

    @SubscribeEvent
    public static void materialClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        WeaponMaterialPowers.clonePlayer(event);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getTagElement(ROOT) == null) return;
        if (SwordCavityBuild.applies(event.getItemStack())) {
            var cells = SwordCavityBuild.cells(event.getItemStack());
            event.getToolTip().add(Component.translatable("tooltip.ftboss.cavity.summary",
                    cells.stream().filter(cell -> cell.installed()).count(), SwordCavityBuild.links(event.getItemStack()).size())
                    .withStyle(ChatFormatting.GRAY));
            if (SwordCavityBuild.missingRequired(event.getItemStack())) event.getToolTip().add(
                    Component.translatable("tooltip.ftboss.sword.structure_missing").withStyle(ChatFormatting.RED));
            return;
        }
        Build build = build(event.getItemStack());
        if (FoodWeaponFamilyBuild.kind(event.getItemStack()) == net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind.MELEE) {
            event.getToolTip().add(Component.translatable("screen.ftboss.material.stats",
                Math.round(build.damage() * 100), Math.round(build.speed() * 100),
                Math.round(build.wear() * 100), String.format(java.util.Locale.ROOT, "%.2f", build.reach()))
                .withStyle(ChatFormatting.GOLD));
        }
        List<Material> materials = materials(event.getItemStack());
        if (!SwordCavityBuild.applies(event.getItemStack())) {
            for (Part part : Part.values()) event.getToolTip().add(Component.translatable("screen.ftboss.material.part",
                    FoodWeaponFamilyBuild.partName(event.getItemStack(), part), materialName(materials.get(part.ordinal()))).withStyle(ChatFormatting.GRAY));
        }
        EnumSet<Material> listed = EnumSet.noneOf(Material.class);
        for (Material material : materials) {
            if (material != Material.ORIGINAL && listed.add(material)) {
                event.getToolTip().add(passive(material).copy().withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        event.getToolTip().add(Component.translatable("tooltip.ftboss.material.unique").withStyle(ChatFormatting.DARK_GRAY));
    }
}
