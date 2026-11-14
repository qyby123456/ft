package net.farmingtales.ftboss.item;

import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_EXECUTE;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_FORTUNE;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_HUNT;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.BLESSING_UNDERTOW;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_FIRE;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_KNOCKBACK;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_POISON;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_SLOW;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain.EVOLUTION_WEAKEN;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.TargetEffectDomain;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * 十座石像新增 70 项专属附魔的战斗结算（每个新增 ID 只有这一处机制实现）。
 *
 * <p>入口约定（由主线程插在既有安全位置，本类不自行注册事件、不额外 hurt、不建 tick/换弹 hook）：
 * {@link #quote} 挂在既有 hit plan 引用阶段（只用 {@link FoodWeaponHitPlan.Builder} typed leaves，
 * attempt 一律传 null，不调用 {@code roll}、不占用/争抢原有 30 项的 attempt domain；效果支付仍走
 * primary payment 与既有 target domain）；{@link #defense} 挂在 {@link FoodBlessingCombat#defense}；
 * {@link #kill} 挂在既有 LivingDeathEvent 击杀入口；{@link #attributes} 挂在既有
 * ItemAttributeModifierEvent 属性入口。</p>
 *
 * <p>关键参数：等级 L 一律 1..3；70 项全部为「条件必触发」，每项冷却写入 hit plan 的 weapon data 快照
 * （键 {@code LastFoodBlessing.<id>}，以 {@code Last} 开头，兼容 {@code FoodWeaponRanged} 的远程玩家计时器合并），
 * 受伤/击杀冷却写入玩家 persistentData（键 {@code FTBossStatue_<id>}）；所有叠层都有上限与过期，不无限叠伤。</p>
 *
 * <p>修改历史：2026-10-25 新建；原 30 项继续由 FoodWeaponEvolution/FoodBlessingCombat 实现，
 * 本类只扫描 {@link StatueBlessingCatalog#isAdded} 为真的 70 项，武器上没有新项时一次快照后立即返回。</p>
 */
public final class StatueBlessingCombat {
    private static final String WEAPON_ROOT = "FTBossFoodEvolution";
    private static final String BLESSINGS = "FoodBlessings";
    private static final String TIMER_PREFIX = "LastFoodBlessing.";
    private static final String PERSIST_PREFIX = "FTBossStatue_";

    private StatueBlessingCombat() {}

    /** 一次命中只对武器 NBT 做一次快照；没有任何新增附魔时返回空表，热路径立即退出。 */
    private record Active(String id, int level) {}

    private static List<Active> snapshot(ItemStack weapon) {
        CompoundTag root = weapon.getTagElement(WEAPON_ROOT);
        if (root == null) return List.of();
        ListTag list = root.getList(BLESSINGS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return List.of();
        List<Active> result = null;
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            String id = entry.getString("Blessing");
            if (!StatueBlessingCatalog.isAdded(id)) continue;
            int level = Math.max(1, Math.min(3, entry.getInt("Level")));
            if (result == null) result = new ArrayList<>(3);
            result.add(new Active(id, level));
        }
        return result == null ? List.of() : result;
    }

    /** 防御按主手 + 副手合并（同 ID 取最高等级），与既有 FoodBlessingCombat.heldLevel 的主副手语义一致。 */
    private static List<Active> heldBlessings(ServerPlayer player) {
        List<Active> main = FoodBlessingCombat.eligible(player.getMainHandItem())
                ? snapshot(player.getMainHandItem()) : List.of();
        List<Active> secondary = FoodBlessingCombat.eligible(player.getOffhandItem())
                ? snapshot(player.getOffhandItem()) : List.of();
        if (secondary.isEmpty()) return main;
        if (main.isEmpty()) return secondary;
        Map<String, Integer> levels = new LinkedHashMap<>();
        for (Active blessing : main) levels.put(blessing.id(), blessing.level());
        for (Active blessing : secondary) levels.merge(blessing.id(), blessing.level(), Math::max);
        List<Active> result = new ArrayList<>(levels.size());
        for (Map.Entry<String, Integer> entry : levels.entrySet()) result.add(new Active(entry.getKey(), entry.getValue()));
        return result;
    }


    /** hit 阶段：只向既有 Builder 追加 typed leaves，结算仍由既有 hit plan 统一提交。 */
    public static void quote(FoodWeaponHitPlan.Builder builder, ServerPlayer player, LivingEntity target,
                             CompoundTag data, long now, ItemStack weapon, FoodWeaponHitPlan.RandomQuotes randomQuotes) {
        if (builder == null || player == null || target == null || data == null || weapon == null || weapon.isEmpty()) return;
        if (!FoodBlessingCombat.eligible(weapon)) return;
        List<Active> active = snapshot(weapon);
        if (active.isEmpty()) return;
        for (Active blessing : active) {
            String id = blessing.id();
            int level = blessing.level();
            switch (id) {
                case "sugar_spun_guard" -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.5F && ready(data, id, now, 140)) {
                        heal(builder, player, 0.5F + 0.25F * level);
                        self(builder, player, MobEffects.ABSORPTION, 60 + 20 * level, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "sugar_glass_edge" -> {
                    if (target.getHealth() >= target.getMaxHealth() * 0.9F) builder.add(1.0F + 0.5F * level);
                }
                case "sugar_caramel_snare" -> {
                    double offsetX = target.getX() - player.getX();
                    double offsetZ = target.getZ() - player.getZ();
                    if (ready(data, id, now, 60) && offsetX * offsetX + offsetZ * offsetZ <= 9.0D) {
                        on(builder, player, target, EVOLUTION_SLOW, MobEffects.MOVEMENT_SLOWDOWN, 40 + 10 * level, 1);
                        timer(builder, data, id, now);
                    }
                }
                case "butcher_gore_cleave" -> {
                    CompoundTag victim = target.getPersistentData();
                    String countKey = "FTBossWoundCount_" + player.getUUID();
                    String expireKey = "FTBossWoundExpire_" + player.getUUID();
                    int cap = level + 1;
                    int count = victim.getLong(expireKey) >= now ? Math.min(cap, victim.getInt(countKey)) : 0;
                    if (count > 0) builder.add(0.6F * count);
                    builder.requireTarget(BLESSING_EXECUTE, target);
                    builder.write(null, victim, countKey, IntTag.valueOf(Math.min(cap, count + 1)));
                    builder.write(null, victim, expireKey, LongTag.valueOf(now + 60L));
                }
                case "butcher_hook_pull" -> {
                    if (ready(data, id, now, 80)) {
                        builder.knockback(null, EVOLUTION_KNOCKBACK, target, 0.28D,
                                target.getX() - player.getX(), target.getZ() - player.getZ());
                        on(builder, player, target, EVOLUTION_SLOW, MobEffects.MOVEMENT_SLOWDOWN, 40, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "butcher_rend" -> {
                    if (ready(data, id, now, 80) && (target.hasEffect(MobEffects.WEAKNESS)
                            || target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || target.hasEffect(MobEffects.POISON))) {
                        on(builder, player, target, EVOLUTION_WEAKEN, MobEffects.WEAKNESS, 60 + 15 * level, 1);
                        on(builder, player, target, EVOLUTION_SLOW, MobEffects.MOVEMENT_SLOWDOWN, 60 + 15 * level, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "wine_barrel_bash" -> {
                    if (ready(data, "wine_barrel_bash_charge", now, 60)) builder.add(2.0F + 1.0F * level);
                    builder.writeAfterWorld(null, data, TIMER_PREFIX + "wine_barrel_bash_charge", LongTag.valueOf(now));
                }
                case "wine_aging_strike" -> {
                    CompoundTag own = player.getPersistentData();
                    String countKey = PERSIST_PREFIX + "wine_aging_strike_count";
                    String atKey = PERSIST_PREFIX + "wine_aging_strike_at";
                    int count = now - own.getLong(atKey) <= 60L ? Math.min(3, own.getInt(countKey) + 1) : 1;
                    builder.add(0.3F * level * count);
                    builder.write(null, own, countKey, IntTag.valueOf(count));
                    builder.write(null, own, atKey, LongTag.valueOf(now));
                }
                case "wine_cork_echo" -> {
                    if (!target.isOnFire() && ready(data, id, now, 90)) {
                        builder.fire(null, EVOLUTION_FIRE, target, 2 + level);
                        timer(builder, data, id, now);
                    }
                }
                case "saya_root_snare" -> {
                    if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || target.hasEffect(MobEffects.POISON)
                            || target.hasEffect(MobEffects.WEAKNESS)) {
                        builder.add(1.2F + 0.6F * level);
                    }
                }
                case "saya_tea_mist" -> {
                    if (target.getHealth() <= target.getMaxHealth() * 0.5F && ready(data, id, now, 100)) {
                        on(builder, player, target, EVOLUTION_POISON, MobEffects.POISON, 60 + 20 * level, 1);
                        timer(builder, data, id, now);
                    }
                }
                case "rat_plague_bite" -> {
                    if (target.getHealth() <= target.getMaxHealth() * 0.4F && ready(data, id, now, 90)) {
                        on(builder, player, target, EVOLUTION_POISON, MobEffects.POISON, 60 + 20 * level, 1);
                        builder.add(0.8F + 0.4F * level);
                        timer(builder, data, id, now);
                    }
                }
                case "rat_swarm_mark" -> {
                    String markKey = "FTBossStatueMark_rat_swarm_mark_" + player.getUUID();
                    if (target.getPersistentData().contains(markKey) && target.getPersistentData().getLong(markKey) >= now) {
                        builder.add(1.5F + 0.75F * level);
                        builder.write(null, target.getPersistentData(), markKey, null);
                    } else {
                        builder.requireTarget(BLESSING_EXECUTE, target);
                        builder.write(null, target.getPersistentData(), markKey, LongTag.valueOf(now + 100L));
                    }
                }
                case "defect_static_charge" -> {
                    CompoundTag own = player.getPersistentData();
                    String countKey = PERSIST_PREFIX + "defect_static_charge_count";
                    String atKey = PERSIST_PREFIX + "defect_static_charge_at";
                    int cap = level + 1;
                    int charge = now - own.getLong(atKey) <= 300L ? Math.min(cap, own.getInt(countKey)) : 0;
                    if (charge >= cap) {
                        builder.add(1.5F + 0.5F * level);
                        builder.write(null, own, countKey, IntTag.valueOf(0));
                    } else {
                        builder.write(null, own, countKey, IntTag.valueOf(charge + 1));
                    }
                    builder.write(null, own, atKey, LongTag.valueOf(now));
                }
                case "defect_overclock" -> {
                    if (FoodWeaponFamilyBuild.kind(weapon) != Kind.MELEE || weapon.getItem() instanceof CrossbowItem
                            || weapon.getItem() instanceof TridentItem) builder.multiply(1.0F + 0.03F * level);
                }
                case "defect_arc_brand" -> {
                    if (ready(data, id, now, 70)) {
                        if (target.hasEffect(MobEffects.GLOWING)) {
                            builder.add(1.2F + 0.6F * level);
                        } else {
                            on(builder, player, target, BLESSING_FORTUNE, MobEffects.GLOWING, 100 + 20 * level, 0);
                        }
                        timer(builder, data, id, now);
                    }
                }
                case "compost_spore_burst" -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.7F && ready(data, id, now, 100)) {
                        heal(builder, player, 0.5F + 0.25F * level);
                        on(builder, player, target, EVOLUTION_POISON, MobEffects.POISON, 40 + 10 * level, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "compost_fertile_wrath" -> {
                    if (target.hasEffect(MobEffects.POISON) || target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                        builder.add(1.5F + 0.75F * level);
                    }
                }
                case "compost_harvest_mark" -> {
                    String markKey = "FTBossStatueMark_compost_harvest_mark_" + player.getUUID();
                    String levelKey = markKey + "_level";
                    builder.requireTarget(BLESSING_HUNT, target);
                    builder.write(null, target.getPersistentData(), markKey, LongTag.valueOf(now + 120L));
                    builder.write(null, target.getPersistentData(), levelKey, IntTag.valueOf(level));
                }
                case "apple_core_blast" -> {
                    if (target.isOnFire()) builder.add(1.5F + 0.75F * level);
                }
                case "apple_cider_haste" -> {
                    if (ready(data, id, now, 90)) {
                        self(builder, player, MobEffects.DIG_SPEED, 60 + 20 * level, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "apple_orchard_step" -> {
                    if (ready(data, id, now, 80)) {
                        self(builder, player, MobEffects.MOVEMENT_SPEED, 40 + 10 * level, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "tide_undertow_pull" -> {
                    if (target.isInWaterRainOrBubble() && ready(data, id, now, 80)) {
                        builder.requireTarget(BLESSING_UNDERTOW, target);
                        builder.knockback(null, EVOLUTION_KNOCKBACK, target, 0.30D,
                                target.getX() - player.getX(), target.getZ() - player.getZ());
                        on(builder, player, target, BLESSING_UNDERTOW, MobEffects.MOVEMENT_SLOWDOWN, 40, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "tide_brine_bite" -> {
                    if (player.isInWaterRainOrBubble() && ready(data, id, now, 60)) {
                        on(builder, player, target, EVOLUTION_SLOW, MobEffects.MOVEMENT_SLOWDOWN, 60 + 15 * level, 1);
                        on(builder, player, target, EVOLUTION_WEAKEN, MobEffects.DIG_SLOWDOWN, 60 + 15 * level, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "tide_crushing_depth" -> {
                    if (target.getHealth() / Math.max(1.0F, target.getMaxHealth())
                            > player.getHealth() / Math.max(1.0F, player.getMaxHealth())) {
                        builder.add(1.2F + 0.6F * level);
                    }
                }
                case "benger_hamstring" -> {
                    if (ready(data, id, now, 60)) {
                        builder.add(0.5F * level);
                        on(builder, player, target, EVOLUTION_SLOW, MobEffects.MOVEMENT_SLOWDOWN, 30 + 10 * level, 0);
                        timer(builder, data, id, now);
                    }
                }
                case "benger_scent_trail" -> {
                    if (target.hasEffect(MobEffects.GLOWING)) builder.add(1.5F + 0.75F * level);
                }
                default -> { }
            }
        }
    }


    /** 防御阶段：只调整既有事件/既有实体状态，不额外调用 hurt，不产生递归伤害。 */
    public static void defense(LivingHurtEvent event) {
        if (event == null || event.isCanceled() || event.getAmount() <= 0.0F) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        List<Active> active = heldBlessings(player);
        if (active.isEmpty()) return;
        long now = player.serverLevel().getGameTime();
        boolean projectile = event.getSource().getDirectEntity() instanceof Projectile;
        boolean explosion = event.getSource().is(DamageTypeTags.IS_EXPLOSION);
        boolean lightning = event.getSource().is(DamageTypeTags.IS_LIGHTNING);
        boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE);
        LivingEntity attacker = event.getSource().getDirectEntity() instanceof LivingEntity direct && direct != player ? direct : null;
        for (Active blessing : active) {
            String id = blessing.id();
            int level = blessing.level();
            switch (id) {
                case "sugar_frost_lattice" -> {
                    if (projectile && ready(player, id, now, 100)) {
                        reduce(event, 0.10D + 0.04D * level);
                        stamp(player, id, now);
                    }
                }
                case "sugar_peppermint_veil" -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.4F && ready(player, id, now, 150)) {
                        reduce(event, 0.07D + 0.03D * level);
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, level - 1));
                        stamp(player, id, now);
                    }
                }
                case "butcher_blood_armor" -> {
                    if (attacker != null && ready(player, id, now, 120)) {
                        player.heal((float) Math.min(2.0D + 1.0D * level, event.getAmount() * 0.10D));
                        stamp(player, id, now);
                    }
                }
                case "butcher_bone_breaker" -> {
                    if (event.getAmount() >= 8.0F && ready(player, id, now, 120)) {
                        reduce(event, 0.08D + 0.03D * level);
                        stamp(player, id, now);
                    }
                }
                case "saya_bud_shield" -> {
                    if (hostiles(player, 8.0D).size() >= 2 && ready(player, id, now, 140)) {
                        reduce(event, 0.09D + 0.03D * level);
                        stamp(player, id, now);
                    }
                }
                case "saya_thorn_repay" -> {
                    if (attacker != null && ready(player, id, now, 60)) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 40 + 10 * level), player);
                        stamp(player, id, now);
                    }
                }
                case "saya_verdant_balance" -> {
                    if (event.getAmount() >= player.getMaxHealth() * 0.12F && ready(player, id, now, 180)) {
                        float before = event.getAmount();
                        reduce(event, 0.10D + 0.04D * level);
                        player.heal((float) (before * (0.05D + 0.02D * level)));
                        stamp(player, id, now);
                    }
                }
                case "wine_tipsy_weave" -> {
                    if ((player.hasEffect(MobEffects.MOVEMENT_SPEED) || player.hasEffect(MobEffects.DIG_SPEED))
                            && ready(player, id, now, 80)) {
                        reduce(event, 0.08D + 0.03D * level);
                        stamp(player, id, now);
                    }
                }
                case "wine_spill_guard" -> {
                    if ((fire || explosion) && ready(player, id, now, 160)) {
                        reduce(event, 0.15D + 0.05D * level);
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40 + 20 * level));
                        stamp(player, id, now);
                    }
                }
                case "rat_cheese_armor" -> {
                    if (player.getFoodData().getFoodLevel() >= 15 && ready(player, id, now, 90)) {
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80, level - 1));
                        stamp(player, id, now);
                    }
                }
                case "rat_scavenge_hide" -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.4F && ready(player, id, now, 140)) {
                        reduce(event, 0.09D + 0.03D * level);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
                        stamp(player, id, now);
                    }
                }
                case "defect_deflect_field" -> {
                    String countKey = PERSIST_PREFIX + "defect_deflect_field_hits";
                    if (projectile && ready(player, id, now, 200)) {
                        int hits = Math.min(3, player.getPersistentData().getInt(countKey)) + 1;
                        if (hits >= 3) {
                            event.setAmount(0.0F);
                            player.getPersistentData().putInt(countKey, 0);
                            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40 + 10 * level, 0));
                            stamp(player, id, now);
                        } else {
                            player.getPersistentData().putInt(countKey, hits);
                        }
                    }
                }
                case "defect_grounding" -> {
                    if ((explosion || lightning) && ready(player, id, now, 160)) {
                        reduce(event, 0.15D + 0.05D * level);
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0));
                        stamp(player, id, now);
                    }
                }
                case "defect_feedback" -> {
                    if (attacker != null && ready(player, id, now, 60)) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40 + 10 * level), player);
                        stamp(player, id, now);
                    }
                }
                case "compost_recycle_guard" -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.5F && ready(player, id, now, 140)) {
                        reduce(event, 0.06D + 0.02D * level);
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40 + 10 * level, 0));
                        stamp(player, id, now);
                    }
                }
                case "compost_mulch_armor" -> {
                    if (ready(player, id, now, 100)) {
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80, level - 1));
                        stamp(player, id, now);
                    }
                }
                case "compost_growth_spurt" -> {
                    if (ready(player, id, now, 120)) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 + 20 * level, 0));
                        stamp(player, id, now);
                    }
                }
                case "apple_blossom_ward" -> {
                    if (ready(player, id, now, 100)) {
                        reduce(event, 0.05D + 0.02D * level);
                        player.heal(0.5F + 0.25F * level);
                        stamp(player, id, now);
                    }
                }
                case "apple_fruit_skin" -> {
                    if (player.getHealth() >= player.getMaxHealth() * 0.95F) reduce(event, 0.15D + 0.05D * level);
                }
                case "apple_golden_shell" -> {
                    if (event.getAmount() >= player.getHealth() && ready(player, id, now, 1200)) {
                        event.setAmount(Math.max(0.0F, player.getHealth() - 1.0F));
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
                        stamp(player, id, now);
                    }
                }
                case "tide_current_guard" -> {
                    if (player.isInWaterRainOrBubble() && ready(player, id, now, 80)) {
                        reduce(event, 0.10D + 0.04D * level);
                        stamp(player, id, now);
                    }
                }
                case "tide_foam_veil" -> {
                    if (ready(player, id, now, 120)) {
                        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60 + 20 * level, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30 + 10 * level, 0));
                        stamp(player, id, now);
                    }
                }
                case "benger_guard_dog" -> {
                    List<TamableAnimal> guardPets = pets(player);
                    if (!guardPets.isEmpty() && ready(player, id, now, 100)) {
                        reduce(event, 0.10D + 0.03D * level);
                        for (TamableAnimal pet : guardPets) pet.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0), player);
                        stamp(player, id, now);
                    }
                }
                case "benger_territorial_growl" -> {
                    if (ready(player, id, now, 80)) {
                        for (LivingEntity hostile : hostiles(player, 3.0D)) {
                            hostile.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40 + 10 * level, 0), player);
                        }
                        stamp(player, id, now);
                    }
                }
                case "benger_loyal_heart" -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.35F && ready(player, id, now, 120)) {
                        reduce(event, 0.09D + 0.03D * level);
                        stamp(player, id, now);
                    }
                }
                default -> { }
            }
        }
    }


    /** 击杀阶段：只给玩家/宠物回馈或对有限小范围敌对生物施加效果；同一事件不重复发奖、不造成伤害。 */
    public static void kill(LivingDeathEvent event) {
        if (event == null || event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity victim = event.getEntity();
        if (victim == player || LemonSummonRewards.suppressed(victim)) return;
        FoodWeaponBondHits.Hit hit = FoodWeaponBondHits.resolve(event.getSource(), player);
        ItemStack weapon = FoodWeaponBondHits.equipped(player, hit);
        if (!FoodBlessingCombat.eligible(weapon)) return;
        List<Active> active = snapshot(weapon);
        if (active.isEmpty()) return;
        long now = player.serverLevel().getGameTime();
        for (Active blessing : active) {
            String id = blessing.id();
            int level = blessing.level();
            switch (id) {
                case "sugar_syrup_surge" -> {
                    player.heal(1.0F + 0.5F * level);
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40 + 15 * level, 0));
                }
                case "butcher_offal_feast" -> {
                    if (player.getHealth() <= player.getMaxHealth() * 0.6F) player.heal(1.5F + 0.75F * level);
                    else player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0));
                }
                case "defect_surge_kill" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60 + 20 * level, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0));
                }
                case "saya_camellia_bloom" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60 + 20 * level, 0));
                    for (LivingEntity hostile : hostiles(player, 5.0D)) {
                        hostile.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0), player);
                    }
                }
                case "wine_vintage_revive" -> {
                    if (ready(player, id, now, 200)) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80 + 30 * level, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, level - 1));
                        stamp(player, id, now);
                    }
                }
                case "rat_gnaw_heal" -> {
                    player.getFoodData().eat(1 + level, 0.15F + 0.05F * level);
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0));
                    player.heal(0.25F * level);
                }
                case "rat_plague_cloud" -> {
                    for (LivingEntity hostile : hostiles(player, 3.0D)) {
                        hostile.addEffect(new MobEffectInstance(MobEffects.POISON, 40 + 10 * level, 0), player);
                    }
                }
                case "compost_vital_rot" -> {
                    float missing = 1.0F - player.getHealth() / Math.max(1.0F, player.getMaxHealth());
                    player.heal((float) ((1.0D + 0.5D * level) * (1.0D + missing)));
                }
                case "compost_harvest_mark" -> {
                    String markKey = "FTBossStatueMark_compost_harvest_mark_" + player.getUUID();
                    if (victim.getPersistentData().contains(markKey) && victim.getPersistentData().getLong(markKey) >= now) {
                        player.heal(1.0F + 0.5F * level);
                        player.getFoodData().eat(1, 0.1F);
                    }
                }
                case "apple_harvest_feast" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60 + 20 * level, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80, level - 1));
                }
                case "tide_tidal_reward" -> {
                    float heal = 0.75F + 0.5F * level;
                    player.heal(player.isInWaterRainOrBubble() ? heal * 2.0F : heal);
                }
                case "benger_pack_call" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 + 20 * level, 0));
                    for (TamableAnimal pet : pets(player)) {
                        pet.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0), player);
                    }
                }
                case "benger_blood_hound" -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60 + 20 * level, 0));
                default -> { }
            }
        }
    }


    /** 属性阶段：只加能兑现的属性；弓弩/法杖主手不加近战攻击伤害，攻击速度只给近战武器。 */
    public static void attributes(ItemAttributeModifierEvent event, ItemStack weapon) {
        if (event == null || !EquipmentAttributeSlots.growthSlot(event.getSlotType(), weapon)) return;
        if (weapon == null || weapon.isEmpty() || !FoodBlessingCombat.eligible(weapon)) return;
        List<Active> active = snapshot(weapon);
        if (active.isEmpty()) return;
        for (Active blessing : active) {
            int level = blessing.level();
            switch (blessing.id()) {
                case "sugar_amber_heart" -> add(event, "sugar_amber_heart", Attributes.MAX_HEALTH, 2.0D * level);
                case "butcher_carving_armor" -> add(event, "butcher_carving_armor", Attributes.ARMOR, 1.0D * level);
                case "saya_seed_of_fortune" -> add(event, "saya_seed_of_fortune", Attributes.LUCK, 0.5D * level);
                case "wine_barrel_reinforce" -> add(event, "wine_barrel_reinforce", Attributes.ARMOR_TOUGHNESS, 0.75D * level);
                case "rat_whisker_luck" -> add(event, "rat_whisker_luck", net.minecraftforge.common.ForgeMod.ENTITY_REACH.get(), 0.15D * level);
                case "defect_overclock" -> {
                    if (FoodWeaponFamilyBuild.kind(weapon) == Kind.MELEE) {
                        add(event, "defect_overclock", Attributes.ATTACK_SPEED, 0.03D * level);
                    }
                }
                case "tide_abyssal_anchor" -> add(event, "tide_abyssal_anchor", Attributes.KNOCKBACK_RESISTANCE, 0.06D * level);
                default -> { }
            }
        }
    }


    private static void self(FoodWeaponHitPlan.Builder builder, ServerPlayer player,
                             MobEffect effect, int duration, int amplifier) {
        builder.effect(null, null, player, new MobEffectInstance(effect, duration, amplifier), player);
    }

    private static void on(FoodWeaponHitPlan.Builder builder, ServerPlayer player,
                           LivingEntity target, TargetEffectDomain domain, MobEffect effect, int duration, int amplifier) {
        builder.effect(null, domain, target, new MobEffectInstance(effect, duration, amplifier), player);
    }

    private static void heal(FoodWeaponHitPlan.Builder builder, ServerPlayer player, float amount) {
        builder.heal(null, player, amount);
    }

    private static void timer(FoodWeaponHitPlan.Builder builder, CompoundTag data, String id, long now) {
        builder.writeAfterWorld(null, data, TIMER_PREFIX + id, LongTag.valueOf(now));
    }

    private static boolean ready(CompoundTag data, String id, long now, int cooldown) {
        String key = TIMER_PREFIX + id;
        long last = data.getLong(key);
        return !data.contains(key) || now < last || now - last >= cooldown;
    }

    private static boolean ready(ServerPlayer player, String id, long now, int cooldown) {
        CompoundTag data = player.getPersistentData();
        String key = PERSIST_PREFIX + id;
        long last = data.getLong(key);
        return !data.contains(key) || now < last || now - last >= cooldown;
    }

    private static void stamp(ServerPlayer player, String id, long now) {
        player.getPersistentData().putLong(PERSIST_PREFIX + id, now);
    }

    private static void reduce(LivingHurtEvent event, double fraction) {
        event.setAmount((float) (event.getAmount() * Math.max(0.0D, 1.0D - fraction)));
    }

    private static List<TamableAnimal> pets(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(8.0D),
                pet -> pet.isAlive() && pet.isTame() && pet.isOwnedBy(player));
    }

    private static List<Monster> hostiles(ServerPlayer player, double radius) {
        return player.serverLevel().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius),
                monster -> monster.isAlive() && !monster.isAlliedTo(player));
    }

    private static void add(ItemAttributeModifierEvent event, String id, Attribute attribute, double amount) {
        if (attribute == null || amount <= 0.0D) return;
        UUID modifier = EquipmentAttributeSlots.modifierId(
                UUID.nameUUIDFromBytes(("ftboss.statue_blessing." + id).getBytes(StandardCharsets.UTF_8)), event.getItemStack());
        event.addModifier(attribute, new AttributeModifier(modifier, "ftboss.statue_blessing." + id, amount,
                AttributeModifier.Operation.ADDITION));
    }
}
