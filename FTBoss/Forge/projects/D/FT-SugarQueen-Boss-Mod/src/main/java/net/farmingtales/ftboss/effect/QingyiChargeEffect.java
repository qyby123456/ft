package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 【充能】(2026-09-14 v122 按用户要求「青衣召唤三叉戟后获得充能…持续 30s」)。
 *
 * <p>青衣每投出一把三叉戟(「投戟」1 把 / 「恒星三叉戟雨」每把 1 次)就会叠一层
 * {@code qingyiTridentStacks}(伤害/护甲/血量上限各 +1, 上限 200)。这个效果负责**把层数显示出来**:
 * 等级 = 层数 - 1(最高显示到 5 级, 免得等级数字溢出客户端包里的 byte), 每次投出刷新到 30 秒。
 *
 * <p>纯显示/记录: 真正的数值由 {@code EasterBossEntity.applyQingyiFlatBuffs()} 的 AttributeModifier 提供。
 */
public class QingyiChargeEffect extends MobEffect {

    public QingyiChargeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x2FD8E6);   // 电光青
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }
}
