package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.BengerPupEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【流血回流给 benger】(2026-09-14 v132 按用户要求「benger对玩家造成的流血伤害会50%回复自己生命」)。
 *
 * <p>难点是**归因**：本模组自己的【流血】({@code ftboss:bleed})每 tick 结算是用
 * {@code entity.hurt(damageSources().magic())} 打的 —— 这个伤害源**没有任何实体**，
 * 在 {@code LivingHurtEvent} 里根本查不到"是谁咬的"。
 *
 * <p>所以由**施加方主动登记**：benger（以及它的小狗）给玩家挂流血时顺手调
 * {@link #link(Player, LivingEntity)} 记一条"这个玩家身上的流血算谁的"，带 5 秒有效期；
 * {@link net.farmingtales.ftboss.effect.BleedEffect} 每秒结算后按登记把 **50% 伤害**回给那只 benger。
 * 小狗咬的也算在**它的主人 benger**头上（用户要的是"benger 回复自己"）。
 *
 * <p>存活期短 + 定时清理：玩家跑出战斗后这场"回流"自然失效，不需要额外的解除逻辑。
 */
public final class BleedLink {

    /** 登记有效期（tick）—— 比流血本身的时长略短即可 */
    private static final long TTL = 5 * 20L;
    /** 回流比例 */
    public static final float HEAL_RATIO = 0.5f;

    /** player -> (施加方, 过期刻) */
    private record Rec(UUID source, long expiry) {}

    private static final Map<UUID, Rec> LINK = new ConcurrentHashMap<>();

    private BleedLink() {}

    /** 记下"这个玩家身上的流血是谁咬的"（小狗会自动归到它的主人 benger） */
    public static void link(Player player, LivingEntity source) {
        if (player == null || source == null) return;
        LivingEntity credit = source;
        if (source instanceof BengerPupEntity pup) {
            /* 小狗 → 就近找它的 benger 主人（找不到就记小狗自己, 至少不会白打） */
            BengerEntity boss = null;
            double best = 64.0 * 64.0;
            for (BengerEntity b : pup.level().getEntitiesOfClass(BengerEntity.class, pup.getBoundingBox().inflate(64.0))) {
                double d = b.distanceToSqr(pup);
                if (d < best) { best = d; boss = b; }
            }
            if (boss != null) credit = boss;
        }
        LINK.put(player.getUUID(), new Rec(credit.getUUID(), (long) player.tickCount + TTL));
    }

    /** 取"这次流血该算谁的"；过期或查不到返回 null */
    public static LivingEntity creditFor(Player player) {
        if (player == null) return null;
        Rec rec = LINK.get(player.getUUID());
        if (rec == null) return null;
        if (player.tickCount > rec.expiry()) { LINK.remove(player.getUUID()); return null; }
        var e = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(64.0));
        for (LivingEntity le : e) if (le.getUUID().equals(rec.source()) && le.isAlive()) return le;
        return null;
    }

    /** 把流血伤害的 50% 回给施加方 */
    public static void refund(Player player, float damage) {
        LivingEntity src = creditFor(player);
        if (src == null) return;
        float heal = damage * HEAL_RATIO;
        if (src.getHealth() < src.getMaxHealth()) {
            src.heal(heal);
        }
    }
}
