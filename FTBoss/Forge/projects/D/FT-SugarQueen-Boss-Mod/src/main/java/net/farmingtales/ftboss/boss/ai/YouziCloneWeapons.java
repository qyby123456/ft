package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.effect.ArmorShredEffect;
import net.farmingtales.ftboss.effect.BloodRageFakeEffect;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.item.ManorWeaponCatalog;
import net.farmingtales.ftboss.item.weapons.DandanScytheItem;
import net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.farmingtales.ftboss.item.weapons.XueliWeaponRules;
import net.farmingtales.ftboss.util.DoraSmgRules;
import net.farmingtales.ftboss.util.DoraWeaponRules;
import net.farmingtales.ftboss.util.MaqueFeatherRules;
import net.farmingtales.ftboss.util.PufferBatRules;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.farmingtales.ftboss.util.YouziStatueRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class YouziCloneWeapons {
    private static final String READY = "ftboss:clone_weapon_ready";
    private static final int INITIAL_DELAY = 40;
    private static final int DEFAULT_COOLDOWN = 160;
    private static final java.util.List<ManorWeaponCatalog.Entry> MELEE_WEAPONS = ManorWeaponCatalog.ENTRIES.stream()
            .filter(entry -> isMeleeWeapon(entry.item())).toList();
    private static boolean resolving;

    private YouziCloneWeapons() {}

    public static String[] weaponIds() {
        return MELEE_WEAPONS.stream().map(ManorWeaponCatalog.Entry::item).toArray(String[]::new);
    }

    public static boolean isMeleeWeapon(String identifier) {
        return switch (identifier) {
            case "ftboss:dandan_scythe", "ftboss:dora_lance", "ftboss:black_cleaver", "ftboss:lemoncat_skillet",
                    "ftboss:shadow_sword", "ftboss:puffer_bat", "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin" -> true;
            default -> false;
        };
    }

    public static boolean applies(EasterBossEntity boss) {
        return boss.isMini() && boss.getVariant() == 0;
    }

    public static boolean isManorWeapon(ItemStack stack) {
        String identifier = id(stack);
        return ManorWeaponCatalog.ENTRIES.stream().anyMatch(entry -> entry.item().equals(identifier));
    }

    private static String id(ItemStack stack) {
        return stack.isEmpty() ? "" : String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    public static void equip(EasterBossEntity boss, int index) {
        var entries = MELEE_WEAPONS;
        for (int offset = 0; offset < entries.size(); offset++) {
            var entry = entries.get(Math.floorMod(index + offset, entries.size()));
            ItemStack stack = ManorWeaponCatalog.stack(entry);
            if (stack.isEmpty()) continue;
            for (var enchantment : new net.minecraft.world.item.enchantment.Enchantment[] {
                    Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.FIRE_ASPECT,
                    Enchantments.SWEEPING_EDGE, Enchantments.UNBREAKING, Enchantments.MENDING }) {
                stack.enchant(enchantment, EasterBossEntity.WEAPON_ENCH_LEVEL);
            }
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
            boss.setItemInHand(InteractionHand.MAIN_HAND, stack);
            boss.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            boss.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            boss.getPersistentData().putLong(READY, boss.level().getGameTime() + INITIAL_DELAY);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 柚子分身装备庄园武器: {} ({})", entry.item(), entry.form());
            return;
        }
    }

    public static void equipRandom(EasterBossEntity boss) {
        var available = MELEE_WEAPONS.stream().filter(entry -> !ManorWeaponCatalog.stack(entry).isEmpty()).toList();
        if (available.isEmpty()) return;
        equip(boss, MELEE_WEAPONS.indexOf(available.get(boss.getRandom().nextInt(available.size()))));
    }

    public static boolean canTarget(EasterBossEntity boss, LivingEntity target) {
        if (target == null || target == boss || !target.isAlive() || target.isSpectator() || target.isInvulnerable()
                || target instanceof EasterBossEntity || XueliMaids.isGuard(target) || boss.isAlliedTo(target)
                || target.isAlliedTo(boss) || !boss.hasLineOfSight(target)) return false;
        if (target instanceof Player player && player.isCreative()) return false;
        var fight = ManorFightMembers.fightId(boss);
        var owner = ManorFightMembers.ownerId(boss);
        return (fight == null || !fight.equals(ManorFightMembers.fightId(target)))
                && (owner == null || !owner.equals(ManorFightMembers.ownerId(target)) && !owner.equals(target.getUUID()));
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !applies(boss)
                || !(boss.level() instanceof ServerLevel level) || !boss.isAlive() || boss.isNoAi()) return;
        ItemStack stack = boss.getMainHandItem();
        if (!isMeleeWeapon(id(stack))) {
            equipRandom(boss);
            stack = boss.getMainHandItem();
        }
        if (!isMeleeWeapon(id(stack))) return;
        long now = level.getGameTime();
        var data = boss.getPersistentData();
        if (!data.contains(READY)) data.putLong(READY, now + INITIAL_DELAY);
        if (now < data.getLong(READY)) return;
        String identifier = id(stack);
        if (identifier.equals("ftboss:maque_blessing_feather")) {
            boss.heal(MaqueFeatherRules.passiveHeal());
            data.putLong(READY, now + MaqueFeatherRules.PASSIVE_INTERVAL_TICKS);
            return;
        }
        LivingEntity target = boss.getTarget();
        if (!canTarget(boss, target)) return;
        double range = range(identifier);
        if (boss.distanceToSqr(target) > range * range) return;
        boss.getLookControl().setLookAt(target, 360.0F, 360.0F);
        data.putLong(READY, now + cooldown(identifier));
        cast(level, boss, target, identifier, stack);
    }

    public static double range(String identifier) {
        return switch (identifier) {
            case "ftboss:statue_youzi" -> YouziStatueRules.ECHO_RANGE;
            case "ftboss:dandan_scythe" -> DandanScytheItem.SWEEP_RADIUS;
            case "ftboss:dora_nova_launcher" -> DoraSmgRules.RANGE;
            case "ftboss:dora_pulse_gun" -> DoraWeaponRules.GUN_RANGE;
            case "ftboss:dora_lance" -> DoraWeaponRules.THRUST_RANGE;
            case "ftboss:wenxuanlan_flywheel" -> XuanlanRules.RANGE;
            case "ftboss:shadow_sword" -> 12.0;
            case "ftboss:lemoncat_skillet" -> LemonCatWeaponItem.SKILLET_RANGE;
            case "ftboss:lemoncat_spatula" -> 16.0;
            case "ftboss:xueli_skillet" -> XueliWeaponRules.SWEEP_RADIUS;
            case "ftboss:xueli_rolling_pin" -> XueliWeaponRules.ROLL_LENGTH;
            case "ftboss:qizai_prayer_staff" -> 16.0;
            default -> 0.0;
        };
    }

    public static int cooldown(String identifier) {
        return switch (identifier) {
            case "ftboss:statue_youzi" -> YouziStatueRules.ECHO_COOLDOWN;
            case "ftboss:dandan_scythe" -> DandanScytheItem.SWEEP_COOLDOWN;
            case "ftboss:dora_nova_launcher" -> 20;
            case "ftboss:dora_pulse_gun" -> DoraWeaponRules.Mode.PULSE.cooldownTicks;
            case "ftboss:dora_lance" -> DoraWeaponRules.Mode.THRUST.cooldownTicks;
            case "ftboss:lemoncat_skillet" -> LemonCatWeaponItem.SKILLET_COOLDOWN;
            case "ftboss:lemoncat_spatula" -> LemonCatWeaponItem.SPATULA_COOLDOWN;
            case "ftboss:xueli_skillet" -> XueliWeaponRules.SKILLET_COOLDOWN;
            case "ftboss:xueli_rolling_pin" -> XueliWeaponRules.PIN_COOLDOWN;
            default -> DEFAULT_COOLDOWN;
        };
    }

    private static void cast(ServerLevel level, EasterBossEntity boss, LivingEntity target, String identifier, ItemStack stack) {
        float damage = switch (identifier) {
            case "ftboss:statue_youzi" -> YouziStatueRules.echoDamage();
            case "ftboss:dandan_scythe" -> DandanScytheItem.sweepDamage();
            case "ftboss:dora_nova_launcher" -> DoraSmgRules.damage(boss.distanceTo(target));
            case "ftboss:dora_pulse_gun" -> DoraWeaponRules.Mode.PULSE.damage();
            case "ftboss:dora_lance" -> DoraWeaponRules.Mode.THRUST.damage();
            case "ftboss:wenxuanlan_flywheel" -> stack.getOrCreateTag().getBoolean("XuanlanWhite")
                    ? XuanlanRules.bladeDamage(false, false) : XuanlanRules.shotDamage(XuanlanRules.MIN_CHARGE_TICKS, 0);
            case "ftboss:shadow_sword" -> ShadowSwordItem.skyDamage();
            case "ftboss:lemoncat_skillet" -> LemonCatWeaponItem.skilletDamage(false);
            case "ftboss:xueli_skillet" -> XueliWeaponRules.sweepDamage();
            case "ftboss:xueli_rolling_pin" -> XueliWeaponRules.rollDamage(XueliWeaponRules.FULL_CHARGE);
            default -> 0.0F;
        };
        if (identifier.equals("ftboss:lemoncat_spatula")) {
            boss.removeEffect(MobEffects.POISON);
            boss.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            boss.heal(LemonCatWeaponItem.healAmount(false));
            boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, LemonCatWeaponItem.REGEN_TICKS, 0));
        } else if (identifier.equals("ftboss:qizai_prayer_staff")) {
            WizardSummon.castByName(level, boss, target, "irons_spellbooks:magic_missile", 1);
        } else if (damage > 0.0F) {
            boolean magical = identifier.equals("ftboss:statue_youzi") || identifier.equals("ftboss:shadow_sword")
                    || identifier.equals("ftboss:wenxuanlan_flywheel");
            var source = magical ? boss.damageSources().indirectMagic(boss, boss) : boss.damageSources().mobAttack(boss);
            if (target.hurt(source, damage)) {
                if (identifier.equals("ftboss:dandan_scythe") || identifier.equals("ftboss:xueli_rolling_pin")) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, XueliWeaponRules.SLOW_DURATION, 0), boss);
                }
            }
        }
        boss.swing(InteractionHand.MAIN_HAND, true);
        level.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 0.7, target.getZ(), 12, 0.3, 0.3, 0.3, 0.02);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hit(LivingDamageEvent event) {
        if (resolving || event.getAmount() <= 0.0F || !(event.getSource().getEntity() instanceof EasterBossEntity boss)
                || !applies(boss) || boss.level().isClientSide || event.getSource().getDirectEntity() != boss
                || !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
                || !canTarget(boss, event.getEntity())) return;
        LivingEntity target = event.getEntity();
        ItemStack stack = boss.getMainHandItem();
        String identifier = id(stack);
        resolving = true;
        try {
            if (identifier.startsWith("tridents_n_stuff:") && isManorWeapon(stack)) {
                stack.getItem().hurtEnemy(stack, target, boss);
            } else if (identifier.equals("ftboss:puffer_bat")) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, PufferBatRules.POISON_TICKS, PufferBatRules.POISON_AMPLIFIER), boss);
            } else if (identifier.equals("ftboss:black_cleaver")) {
                var effect = FTEffects.BLOOD_RAGE_FAKE.get();
                var current = boss.getEffect(effect);
                int stacks = current == null ? 1 : Math.min(BloodRageFakeEffect.MAX_STACKS, current.getAmplifier() + 2);
                boss.addEffect(new MobEffectInstance(effect, BloodRageFakeEffect.DURATION, stacks - 1));
                ArmorShredEffect.onHit(target);
                float raw = (float) boss.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double armor = target.getArmorValue();
                double toughness = target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
                double reduction = Math.min(20.0, Math.max(armor / 5.0, armor - raw / (2.0 + toughness / 4.0))) / 25.0;
                float extra = (float) (raw * reduction * 0.5) + raw * BloodRageFakeEffect.trueDamageRatio(stacks - 1);
                if (extra > 0.0F) {
                    target.invulnerableTime = 0;
                    target.hurt(boss.damageSources().indirectMagic(boss, boss), extra);
                }
            }
        } finally {
            resolving = false;
        }
    }
}
