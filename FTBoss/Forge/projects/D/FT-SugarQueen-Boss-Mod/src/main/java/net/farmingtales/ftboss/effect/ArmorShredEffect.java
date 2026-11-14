package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * 【护甲撕裂】(2026-09-14 v144 按用户要求「黑色切割者可以削弱敌人护甲 10 点」)。
 *
 * <p>命中敌人时挂上：**护甲 -10**（固定 UUID 的 ADDITION 修饰符，可刷新、可叠加到 2 层 = -20），
 * 默认 8 秒。用状态效果而不是"打补丁式计时器"：玩家能在敌人身上/自己 HUD 看见，且随效果结束自动还原。
 */
public class ArmorShredEffect extends MobEffect {

    /** 每层削减的护甲值 */
    public static final double SHRED_PER_STACK = 10.0;
    /** 最多 2 层（-20） */
    public static final int MAX_STACKS = 2;
    /** 持续 */
    public static final int TICKS = 8 * 20;

    private static final UUID UUID_PER_STACK_1 = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000c001");
    private static final UUID UUID_PER_STACK_2 = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000c002");

    public ArmorShredEffect() {
        super(MobEffectCategory.HARMFUL, 0x6E7B8B);
    }

    /** 命中叠层：已有则 +1（封顶 2 层）并刷新 */
    public static void onHit(LivingEntity target) {
        var eff = FTEffects.ARMOR_SHRED.get();
        if (eff == null) return;
        MobEffectInstance cur = target.getEffect(eff);
        int stacks = (cur == null) ? 1 : Math.min(MAX_STACKS, cur.getAmplifier() + 2);
        target.addEffect(new MobEffectInstance(eff, TICKS, stacks - 1, false, true, true));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        var armor = entity.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        armor.removeModifier(UUID_PER_STACK_1);
        armor.removeModifier(UUID_PER_STACK_2);
        armor.addTransientModifier(new AttributeModifier(UUID_PER_STACK_1, "ftboss.armor_shred_1",
                -SHRED_PER_STACK, AttributeModifier.Operation.ADDITION));
        if (amplifier >= 1) {
            armor.addTransientModifier(new AttributeModifier(UUID_PER_STACK_2, "ftboss.armor_shred_2",
                    -SHRED_PER_STACK, AttributeModifier.Operation.ADDITION));
        }
    }
}