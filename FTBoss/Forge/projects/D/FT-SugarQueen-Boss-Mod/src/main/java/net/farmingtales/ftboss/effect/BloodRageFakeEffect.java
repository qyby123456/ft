package net.farmingtales.ftboss.effect;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * 【血怒·伪（削弱版）】(2026-09-14 v143 按用户要求)。
 *
 * <p>来源：玩家用【黑色切割者】命中敌人时叠 1 层（最多 5 层）。**每 10 秒衰减 1 层**，掉光即消失。
 *
 * <p>层数收益（按层线性，满 5 层 = 用户要的数值）：
 * <ul>
 *   <li>攻击力：每层 +20% → 满层 **+100%**</li>
 *   <li>移速：每层 +2% → 满层 **+10%**</li>
 *   <li>真伤：每层 0.2% → 满层 **1%**（由 {@link #trueDamageRatio(int)} 给武器加算，走魔法伤害绕护甲）</li>
 * </ul>
 * 属性用**固定 UUID** 的修饰符（项目规矩：固定 UUID 才能稳定替换/移除，避免叠加爆炸）。
 */
public class BloodRageFakeEffect extends MobEffect {

    /** v153 diagnostic: log who removes this effect (find the "only 15s" culprit) */
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onRemoved(net.minecraftforge.event.entity.living.MobEffectEvent.Remove e) {
        try {
            var eff = FTEffects.BLOOD_RAGE_FAKE.get();
            if (eff == null || e.getEffectInstance() == null) return;
            if (e.getEffectInstance().getEffect() != eff) return;
            net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][bloodrage-fake] removed: target={} left={} stacks={}",
                    e.getEntity().getName().getString(), e.getEffectInstance().getDuration(),
                    e.getEffectInstance().getAmplifier() + 1);
        } catch (Throwable ignored) {}
    }

    /** 最多 5 层（amplifier 0..4） */
    public static final int MAX_STACKS = 5;
    /** 每层衰减间隔（tick） */
    public static final int DECAY_TICKS = 10 * 20;
    /** v154 按用户要求：**只持续 15 秒**（命中会刷新回满 15 秒）；衰减仍在效果内按每 10 秒掉一层 */
    public static final int DURATION = 15 * 20;

    private static final UUID ATK_UUID = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000b001");
    private static final UUID SPD_UUID = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000b002");

    public BloodRageFakeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;      // 每秒检查一次
    }

    /** 武器命中时叠层：已有则 +1 层并刷新计时，到顶就只刷新 */
    public static void onHit(LivingEntity attacker) {
        if (!(attacker instanceof Player p)) return;
        var eff = FTEffects.BLOOD_RAGE_FAKE.get();
        if (eff == null) return;
        MobEffectInstance cur = p.getEffect(eff);
        int stacks = (cur == null) ? 1 : Math.min(MAX_STACKS, cur.getAmplifier() + 2);
        p.addEffect(new MobEffectInstance(eff, DURATION, stacks - 1, false, true, true));
    }

    /** 满层 1% 真伤，按层线性 */
    public static float trueDamageRatio(int amplifier) {
        return 0.002f * (amplifier + 1);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        /* 每 10 秒衰减一层 */
        int elapsed = DURATION - (entity.getEffect(this) == null ? 0 : entity.getEffect(this).getDuration());
        if (elapsed > 0 && elapsed % DECAY_TICKS == 0) {
            if (amplifier <= 0) {
                entity.removeEffect(this);
                FTBossMod.LOGGER.info("[FTBoss][血怒·伪] {} 层数掉光, 效果结束", entity.getName().getString());
                return;
            }
            /* v154: 衰减时**保留剩余时长**(不要用 DURATION 重置), 否则 15 秒的窗口会被无限延长 */
            int left = Math.max(1, entity.getEffect(this) == null ? DURATION : entity.getEffect(this).getDuration());
            entity.addEffect(new MobEffectInstance(this, left, amplifier - 1, false, true, true));
            FTBossMod.LOGGER.info("[FTBoss][血怒·伪] {} 衰减到 {} 层", entity.getName().getString(), amplifier);
        }
        applyAttributes(entity, amplifier + 1);
    }

    /* ★ v149 关键修复：属性必须在效果**添加/移除**时挂上/摘掉。
       原来只在 applyEffectTick 里加, 效果结束后**永远不会移除** -> 攻击力/移速加成永久残留。 */
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
        applyAttributes(entity, amplifier + 1);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
        var atk = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.removeModifier(ATK_UUID);
        var spd = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) spd.removeModifier(SPD_UUID);
    }

    /** 把层数换算成属性修饰符（先摘旧的再加新的） */
    private static void applyAttributes(LivingEntity e, int stacks) {
        var atk = e.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(ATK_UUID);
            atk.addTransientModifier(new AttributeModifier(ATK_UUID, "ftboss.blood_rage_fake_atk",
                    0.20 * stacks, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        var spd = e.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(SPD_UUID);
            spd.addTransientModifier(new AttributeModifier(SPD_UUID, "ftboss.blood_rage_fake_spd",
                    0.02 * stacks, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}