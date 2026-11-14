package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 【流血】(2026-09-14 v124 按用户反馈「朵拉的对玩家造成流血效果未生效」)。
 *
 * <p>原来 {@code ExtraDebuffs.bleed} 是去借**别的模组**的流血效果
 * ({@code legendary_monsters:bleeding} / {@code tridents_n_stuff:bleeding} / {@code armamentarium:blood_loss})。
 * 那些效果要么只在自己的武器结算里掉血、要么根本不注册, 于是"挂上了等于没挂"。
 * 现在本模组**自带**一个流血: 每秒按等级掉血, 不依赖任何外部模组。
 *
 * <p>伤害用 {@link net.minecraft.world.damageSources()#magic()} —— **穿甲**(不吃护甲值), 但会被
 * 抗性提升/保护附魔减免; 死亡提示是"被魔法杀死了"而不是难看的"掉出了世界"。
 *
 * <p>等级 = 层数 - 1: 每层每秒 {@value #DAMAGE_PER_SECOND} 点, 最高 5 层 = 15 点/秒。
 */
public class BleedEffect extends MobEffect {

    /** 每层每秒的流血伤害 */
    public static final float DAMAGE_PER_SECOND = 3.0F;
    /** 最大层数(等级 = 层数 - 1) */
    public static final int MAX_STACKS = 10;   // v133: 二阶段允许叠到 10 层
    /** 默认时长(8 秒) */
    public static final int TICKS = 8 * 20;

    public BleedEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B1A1A);   // 暗红
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }

    /** 每秒结算一次 */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        float dmg = DAMAGE_PER_SECOND * Math.min(MAX_STACKS, amplifier + 1);
        entity.invulnerableTime = 0;
        entity.hurt(entity.damageSources().magic(), dmg);
        /* v132 按用户要求: benger 造成的流血伤害, 50% 回流成它的生命 */
        if (entity instanceof net.minecraft.world.entity.player.Player pl) {
            net.farmingtales.ftboss.boss.ai.BleedLink.refund(pl, dmg);
        }
    }
}
