package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 【非庄园 Boss 伤害翻倍】(2026-09-14 v130 按用户要求「把非庄园boss的boss全部伤害翻倍」)。
 *
 * <p>甜苹果庄园那十位（{@link EasterBossEntity}）**不动**；其余本模组的 Boss
 * （姜饼王 / 屠夫 / 沙耶 / 酒桶王 / 鼠群 / 故障机器人 / 答辩超人 / 苹果爵士 / **benger**）
 * 造成的**所有伤害 ×2** —— 在 {@link LivingHurtEvent} 这一处统一乘，比逐个去改各技能的伤害常量可靠得多
 * （它们的伤害来源很杂：近战 / 投射物 / 爆炸 / 铁魔法 / 召唤物，逐个改必漏）。
 *
 * <p>三个细节：
 * <ol>
 *   <li>投射物/爆炸的 {@code source.getEntity()} 往往是**弹体**，所以要顺着 {@link Projectile#getOwner()} 找回 Boss；</li>
 *   <li>Boss 打 Boss / 打自己的召唤物也照乘（本模组 Boss 之间本来就不会互相攻击，无副作用）；</li>
 *   <li>只在服务端乘一次（事件本身只在服务端发）。</li>
 * </ol>
 */
public final class NonManorDamageX2 {

    private NonManorDamageX2() {}

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        try {
            Entity src = e.getSource().getEntity();
            if (src == null) src = e.getSource().getDirectEntity();
            if (src instanceof Projectile pr && pr.getOwner() != null) src = pr.getOwner();
            if (src == null) return;
            /* 只认"本模组的 Boss"，且**排除**庄园彩蛋十人 */
            var key = net.minecraft.world.entity.EntityType.getKey(src.getType());
            if (key == null || !FTBossMod.MOD_ID.equals(key.getNamespace())) return;
            if (src instanceof EasterBossEntity) return;
            if (!(src instanceof net.minecraft.world.entity.Mob)) return;   // 物品/投射物本身不算
            float amt = e.getAmount();
            if (amt <= 0.0f) return;
            e.setAmount(amt * 2.0f);
        } catch (Throwable ignored) {
            /* 兜底：倍率出问题绝不能让伤害结算崩掉 */
        }
    }
}
