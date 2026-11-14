package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【折耳根：连击转虚空 + 黑暗/失明 + 诊断】(2026-09-14 v117 建, v118 扩充)。
 *
 * <p>用户要求: 「折耳根如果连续命中玩家50次则伤害的50%变成虚空伤害」、
 * 「折耳根的失明还是失效」(v118)。
 *
 * <p>为什么用事件而不是改各个技能: 折耳根的伤害来源很杂(近战武器结算 / 暗影突袭 /
 * 铁魔法法术 / 未来的新招), 逐个去改既容易漏、也没法覆盖外部模组造成的伤害。
 * 挂在 {@link LivingHurtEvent} 上就一句话: **只要这次伤害归因于折耳根、挨打的是玩家**,
 * 就顺手把三件事做了 —— 记连击、复核黑暗/失明、满了就把一半拆成虚空。
 *
 * <p>拆伤害的做法: `e.setAmount(原伤害 × 0.5)` 留下物理那一半, 再立刻用
 * `fellOutOfWorld`(虚空伤害, 自带穿甲、不吃护甲与魔法减免)补上另一半。
 * 虚空伤害**没有攻击者实体**, 所以这个监听器不会二次触发(不会无限递归)。
 *
 * <p>连击计数: 只有"打到玩家"才 +1(v118 按用户反馈改了 —— 原来打到任何非玩家生物都会断连,
 * 而庄园里满地都是召唤物/女仆/分身, 50 连根本叠不满); 30 秒没打中玩家才由 Boss 的 `tick()` 断连。
 *
 * <p>注册方式与 {@link HealBan} 一致: 在 {@code FTBossMod} 里**显式**注册(不靠类注解自动扫描)。
 */
public final class ShadowVoidSplit {

    /** 折耳根的 variant 下标 */
    public static final int SHADOW_VARIANT = 7;

    /** 黑暗/失明的复核间隔(每秒最多一次) */
    private static final long FX_CD = 20L;
    /** 「失明/黑暗到底有没有挂上」的诊断日志间隔 */
    private static final long FX_LOG_CD = 5 * 20L;
    /** 「这一刀拆成虚空」的日志间隔 */
    private static final long SPLIT_LOG_CD = 2 * 20L;

    private static final Map<UUID, Long> LAST_FX = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_FX_LOG = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_SPLIT_LOG = new ConcurrentHashMap<>();

    private ShadowVoidSplit() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent e) {
        try {
            Entity attacker = e.getSource().getEntity();
            if (!(attacker instanceof EasterBossEntity boss)) return;
            if (boss.getVariant() != SHADOW_VARIANT || boss.isMini()) return;
            if (!(e.getEntity() instanceof Player p)) return;
            long now = boss.level().getGameTime();

            boss.onShadowHitPlayer(now);
            applyDarknessAndBlindness(p, now);
            if (!boss.shadowVoidMode()) return;

            float amount = e.getAmount();
            if (amount <= 0.0f) return;
            /* ★★★ v119 按用户要求改成「获得对玩家的**额外** 50% 虚空伤害」★★★
               不再把原伤害减半(那是 v117 的"转换"), 而是**额外再补一发**等量的一半虚空伤害:
               净效果 = 满 25 连后, 折耳根对玩家的总伤害 +50%, 而且多出来的那 50% 自带穿甲。 */
            float extra = amount * 0.5f;
            p.invulnerableTime = 0;
            net.farmingtales.ftboss.boss.ManorMercy.withOwner(boss, () -> p.hurt(p.damageSources().fellOutOfWorld(), extra));
            logSplit(p, extra, now);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 折耳根「连击转虚空」结算失败: {}", t.toString());
        }
    }

    /**
     * v118: 折耳根的**黑暗 + 失明**改成"每次打中玩家就复核一遍"。
     *
     * <p>用户两次反馈「失明失效」。v118 的验真日志已经给出答案: 12 条全是
     * {@code 失明=true 黑暗=true} —— 服务端**确实挂上了**(`hasEffect` 不会骗人),
     * 玩家看不到是**客户端的光影/资源包把失明的雾吃掉了**(原版失明的画面就是一层黑雾)。
     *
     * <p>所以 v119 除了继续挂 失明 + 黑暗, 再补一层**反胃(CONFUSION)** ——
     * 它的"画面扭曲/摇晃"是客户端后期处理里最难被光影关掉的, 玩家一定能感觉到中招;
     * 失明本身仍然有游戏性效果(不能疾跑/不能暴击), 不算白挂。
     *
     * <p><b>v124 按用户要求「折耳根的失明改回去之前可以生效的版本」</b>: 10:50 那局的日志把真因
     * 摊开了 —— 服务端刚挂上是 {@code 现在 失明=true 黑暗=true}, 可**5 秒后**再查
     * {@code 上一轮残留 失明=false}(时长明明是 300t=15 秒), 客户端 30 条诊断里也是 **0 条**
     * {@code 失明=true}(只有反胃一直在) —— 说明有个东西**只针对失明/黑暗**在把它们摘掉。
     * 所以不再指望"挂一次能留 15 秒", 改成 {@link #maintainTick} 在交战中**每 5 tick 补一次**,
     * 谁摘都不影响玩家"一直瞎着"这个体感。
     */
    private static void applyDarknessAndBlindness(Player p, long now) {
        Long last = LAST_FX.get(p.getUUID());
        if (last != null && now - last < FX_CD) return;
        LAST_FX.put(p.getUUID(), now);
        /* v121 诊断: 先记"上一轮挂的还在不在" —— 若这里经常是 false, 说明有东西在**中途把它洗掉**
           (而不是我们没挂)。上一次的日志已经证明挂上那一下是 true。 */
        boolean keptBlind = p.hasEffect(MobEffects.BLINDNESS);
        boolean keptDark = p.hasEffect(MobEffects.DARKNESS);
        applyShadowBlind(p, EasterBossEntity.SHADOW_DARKNESS_ON_HIT);   // v125: 可见之戒挡不住的那一条
        Long ll = LAST_FX_LOG.get(p.getUUID());
        if (ll != null && now - ll < FX_LOG_CD) return;
        LAST_FX_LOG.put(p.getUUID(), now);
        if (LAST_FX_LOG.size() > 64) {
            LAST_FX_LOG.entrySet().removeIf(en -> now - en.getValue() >= FX_LOG_CD);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根暗影效果 -> {} (上一轮残留 失明={} 黑暗={} | 现在 失明={} 黑暗={})",
                p.getName().getString(), keptBlind, keptDark,
                p.hasEffect(MobEffects.BLINDNESS), p.hasEffect(MobEffects.DARKNESS));
    }

    /** v118: 把"这一刀拆了多少成虚空"也记一条(2 秒最多一次), 用来确认转虚空真的在生效 */
    private static void logSplit(Player p, float half, long now) {
        Long last = LAST_SPLIT_LOG.get(p.getUUID());
        if (last != null && now - last < SPLIT_LOG_CD) return;
        LAST_SPLIT_LOG.put(p.getUUID(), now);
        if (LAST_SPLIT_LOG.size() > 64) {
            LAST_SPLIT_LOG.entrySet().removeIf(en -> now - en.getValue() >= SPLIT_LOG_CD);
        }
        /* ★ 2026-09-21: 以前这里打的是**削减前**的数(实机日志出现过"这次 351.9 点改走虚空"),
           而 {@link BossOutgoingDamageNerfs} 会在 LOW 优先级把它再乘一次 —— 日志数字比玩家实际吃到的
           大一倍以上, 看起来像"虚空伤害根本没被削"。现在两个数一起打: 拆出来的原始值 + 实际结算值。 */
        float applied = BossOutgoingDamageNerfs.zheergenDamage(half);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根连击转虚空 -> {} (拆出 {} 点 -> 实际结算 {} 点虚空/穿甲)",
                p.getName().getString(), String.format("%.1f", half), String.format("%.1f", applied));
    }

    /* ==================== v124: 「失明/黑暗」自己持续维持 + 谁摘了就报出来 ==================== */

    /** 补挂间隔(每 5 tick 一次) */
    private static final long MAINTAIN_CD = 5L;
    /** 每次补挂给的时长(3 秒: 就算下一 tick 被摘掉, 5 tick 后也会补回来) */
    private static final int MAINTAIN_TICKS = 60;
    /** 折耳根周围多少格内的玩家一直吃失明 */
    private static final double MAINTAIN_RANGE = 24.0;
    /** 最近多久内被折耳根打过的玩家也算"交战中"(跑远了也先瞎一会儿) */
    private static final long MAINTAIN_HIT_WINDOW = 15L * 20L;

    private static final Map<UUID, Long> LAST_MAINTAIN_LOG = new ConcurrentHashMap<>();

    /**
     * v124 按用户要求「折耳根的失明改回去之前可以生效的版本」。
     *
     * <p>上面那套"打中才挂"依赖"挂上去能留 15 秒" —— 而 10:50 那局的日志证明它留不住
     * (服务端 5 秒后就查不到了, 客户端 30 条诊断也全是 {@code 失明=false})。
     * 所以改成**由折耳根自己每 5 tick 补一次**: 只要它在场且玩家在 24 格内(或 15 秒内刚被它打过),
     * 失明 + 黑暗就一直挂着 —— 不管是谁在摘, 玩家体感都是"从头瞎到尾"。
     */
    public static void maintainTick(EasterBossEntity boss, net.minecraft.server.level.ServerLevel sl) {
        try {
            if (boss == null || boss.getVariant() != SHADOW_VARIANT || boss.isMini() || !boss.isAlive()) return;
            long now = sl.getGameTime();
            if (now % MAINTAIN_CD != 0L) return;
            for (net.minecraft.server.level.ServerPlayer p : sl.players()) {
                if (!p.isAlive() || p.isSpectator()) continue;
                boolean near = p.distanceToSqr(boss) <= MAINTAIN_RANGE * MAINTAIN_RANGE;
                Long hit = LAST_FX.get(p.getUUID());
                boolean recentlyHit = hit != null && now - hit <= MAINTAIN_HIT_WINDOW;
                if (!near && !recentlyHit) continue;
                boolean hadBlind = p.hasEffect(MobEffects.BLINDNESS);
                boolean hadDark = p.hasEffect(MobEffects.DARKNESS);
                applyShadowBlind(p, MAINTAIN_TICKS);
                Long ll = LAST_MAINTAIN_LOG.get(p.getUUID());
                if (ll != null && now - ll < FX_LOG_CD) continue;
                LAST_MAINTAIN_LOG.put(p.getUUID(), now);
                if (LAST_MAINTAIN_LOG.size() > 64) {
                    LAST_MAINTAIN_LOG.entrySet().removeIf(en -> now - en.getValue() >= FX_LOG_CD);
                }
                FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根失明维持 -> {} (补挂前 失明={} 黑暗={} | 补挂后 失明={} 黑暗={} 暗影笼罩={})",
                        p.getName().getString(), hadBlind, hadDark,
                        p.hasEffect(MobEffects.BLINDNESS), p.hasEffect(MobEffects.DARKNESS),
                        p.hasEffect(net.farmingtales.ftboss.effect.FTEffects.SHADOW_BLIND.get()));
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 折耳根失明维持失败: {}", t.toString());
        }
    }

    private static void applyShadowBlind(Player p, int ticks) {
        net.farmingtales.ftboss.effect.ShadowBlindEffect.applyTo(p, ticks);
    }
}
