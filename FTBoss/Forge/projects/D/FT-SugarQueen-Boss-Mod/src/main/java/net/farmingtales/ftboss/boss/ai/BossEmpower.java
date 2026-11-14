package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * **Boss 强化**(2026-09-11 起): 强化除【沙耶(茶树)】与【故障机器人】之外的全部 Boss
 * (糖霜女皇 / 屠夫 / 鼠群 / 酒桶王)。2026-09-12 追加: 屠夫狂暴加成 + 扩展索敌、酒桶王分身。
 */
public final class BossEmpower {

    private static final UUID ID_SPEED = UUID.fromString("a1b2c3d4-0001-4a01-9001-000000000001");
    private static final UUID ID_ATTACK = UUID.fromString("a1b2c3d4-0002-4a01-9001-000000000002");
    private static final UUID ID_ARMOR = UUID.fromString("a1b2c3d4-0003-4a01-9001-000000000003");
    private static final UUID ID_KB = UUID.fromString("a1b2c3d4-0004-4a01-9001-000000000004");
    private static final UUID ID_RAGE_SPEED = UUID.fromString("a1b2c3d4-0005-4a01-9001-000000000005");
    private static final UUID ID_RAGE_ATK = UUID.fromString("a1b2c3d4-0006-4a01-9001-000000000006");

    private static final double SPEED_MUL = 0.25;
    private static final double ATTACK_MUL = 0.30;
    private static final double ARMOR_ADD = 4.0;
    /* 2026-09-19 按用户要求「允许 Boss 受到击退」: 强化包不再额外加抗击退(原 0.30，会叠到 0.8 又把击退挡住)。 */
    private static final double KB_ADD = 0.0;
    private static final int EFFECT_TICKS = 520;

    private BossEmpower() {}

    public static boolean isEmpowered(LivingEntity e) {
        if (e == null) return false;
        try {
            var t = e.getType();
            return t == FTEntities.SUGAR_QUEEN.get()
                    || t == FTEntities.MEAT_MOUNTAIN.get()
                    || t == FTEntities.RAT_BOSS.get()
                    || t == FTEntities.WINE_BARREL.get()
                    || t == FTEntities.COMPOST_BOSS.get();   // 2026-09-12: 炫彩答辩超人也吃强化(自身攻防不足)
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** 施法等级加成(由 WizardSummon 调用) */
    public static int spellBonusFor(Object caster) {
        return (caster instanceof LivingEntity le && isEmpowered(le)) ? 1 : 0;
    }

    /** 每 20t 调用一次(BossHandler)。注意: 判定一律用"区间"写法, 因为采样相位与整除不同余时会永不成立 */
    public static void tick(LivingEntity boss) {
        if (RatSwarmEncounter.skipEmpower(boss)) return;
        if (boss == null || boss.level().isClientSide || !isEmpowered(boss)) return;
        mod(boss, Attributes.MOVEMENT_SPEED, ID_SPEED, "ftboss_empower_speed", SPEED_MUL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        mod(boss, Attributes.ATTACK_DAMAGE, ID_ATTACK, "ftboss_empower_attack", ATTACK_MUL, AttributeModifier.Operation.MULTIPLY_TOTAL);
        mod(boss, Attributes.ARMOR, ID_ARMOR, "ftboss_empower_armor", ARMOR_ADD, AttributeModifier.Operation.ADDITION);
        mod(boss, Attributes.KNOCKBACK_RESISTANCE, ID_KB, "ftboss_empower_kb", KB_ADD, AttributeModifier.Operation.ADDITION);

        /* ① 屠夫: 狂暴后 移速 +50% / 伤害 +200%; 并且**不再只索敌玩家** */
        if (boss.getType() == FTEntities.MEAT_MOUNTAIN.get()) {
            boolean raging = BossRage.tierOf(boss) >= 1;
            var spd = boss.getAttribute(Attributes.MOVEMENT_SPEED);
            var atk = boss.getAttribute(Attributes.ATTACK_DAMAGE);
            if (raging) {
                if (spd != null && spd.getModifier(ID_RAGE_SPEED) == null) {
                    spd.addTransientModifier(new AttributeModifier(ID_RAGE_SPEED, "ftboss_rage_speed", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                if (atk != null && atk.getModifier(ID_RAGE_ATK) == null) {
                    atk.addTransientModifier(new AttributeModifier(ID_RAGE_ATK, "ftboss_rage_atk", 2.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                if (boss.tickCount % 40 < 20) {
                    boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false, true));
                }
            }
            var cur = (boss instanceof net.minecraft.world.entity.Mob m0) ? m0.getTarget() : null;
            if ((cur == null || !cur.isAlive()) && boss.level() instanceof net.minecraft.server.level.ServerLevel sl2) {
                LivingEntity best = null;
                double bd = 32.0 * 32.0;
                for (LivingEntity le : sl2.getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(32.0))) {
                    if (le == boss || !le.isAlive() || BossAggro.isBossSide(le)) continue;
                    double d = boss.distanceToSqr(le);
                    if (d < bd) { bd = d; best = le; }
                }
                if (best != null && boss instanceof net.minecraft.world.entity.Mob m1) m1.setTarget(best);
            }
        }

        /* ② 酒桶王: **二阶段(血量 <50%)** 才召唤分身, 用**与 Boss 一样的模型**, 场上最多 2 个 */
        if (boss.getType() == FTEntities.WINE_BARREL.get() && boss.tickCount % 600 < 20
                && boss.getHealth() < boss.getMaxHealth() * 0.5f          // 2026-09-12: 二阶段才召唤
                && boss.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            int have = sl.getEntitiesOfClass(net.farmingtales.ftboss.entity.WineBarrelDecoyEntity.class,
                    boss.getBoundingBox().inflate(24.0)).size();
            if (have < 2) {
                var decoy = FTEntities.WINE_BARREL_DECOY.get().create(sl);   // 与 Boss 同一模型
                if (decoy != null) {
                    decoy.setNoAi(true);
                    decoy.setHealth(1.0f);
                    decoy.setCustomName(net.minecraft.network.chat.Component.literal("§7酒桶王的分身"));
                    decoy.setCustomNameVisible(true);
                    decoy.setPersistenceRequired();
                    decoy.moveTo(boss.getX() + (boss.getRandom().nextDouble() - 0.5) * 4,
                            boss.getY(), boss.getZ() + (boss.getRandom().nextDouble() - 0.5) * 4, 0f, 0f);
                    net.farmingtales.ftboss.boss.ManorFightMembers.inherit(boss, decoy);
                    sl.addFreshEntity(decoy);
                    FTBossMod.LOGGER.info("[FTBoss][酒桶王] 分身出现(1 血, 不攻击)");
                }
            }
        }

        /* ③ 常驻增益: 每 400t 续一次, 持续 520t */
        if (boss.tickCount % 400 < 20) {
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_TICKS, 0, false, false, true));
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_TICKS, 0, false, false, true));
            boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, EFFECT_TICKS, 0, false, false, true));
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, EFFECT_TICKS, 1, false, false, true));
        }
    }

    private static void mod(LivingEntity e, Attribute attr, UUID id, String name, double amount, AttributeModifier.Operation op) {
        var inst = e.getAttribute(attr);
        if (inst == null) return;
        if (inst.getModifier(id) == null) inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
    }
}
