package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 「Boss 多久没打到玩家」的计时器（2026-09-24 按用户要求新增，配合
 * {@link net.farmingtales.ftboss.util.ManorChaseRules} 与 {@link ManorFlyBan}）。
 *
 * <p>用户原话：「boss 的珍珠追击没触发（减少触发，除非长时间未攻击到玩家 15s，
 * 超过 120s 则禁飞场地 60s，结束后允许飞行并重新倒计时禁飞）」。
 *
 * <p>计时器**挂在被追击的那个 Boss 实体上**（{@code EasterBossEntity.playerHitIdleTicks}）：
 * <ul>
 *   <li>每 tick 由实体自己 +1（见 {@code EasterBossEntity.tick}）；</li>
 *   <li>本类只在"**真的造成了伤害**"时把它清零 —— 判据是 {@link LivingHurtEvent} 里
 *       伤害来源能顺着投射物主人 / 召唤物主人链回溯到 {@code EasterBossEntity}。
 *       这和 v26 那套"离目标超过 4 格累计 20 tick"完全不同：以前 Boss 一贴身就把计数清零，
 *       所以珍珠追击实战里几乎从不触发；现在只要它一直能摸到人，计时器就一直归零。</li>
 *   <li>柚子迷你分身打的也算本体打到（{@link ManorFightMembers#owner(Entity)} 回溯到根主人）。</li>
 * </ul>
 *
 * <p><b>为什么在 {@code BossHandler} 的构造函数里显式注册</b>：本工程踩过
 * {@code @Mod.EventBusSubscriber} "注解看着在、实际没注册"的坑（见 {@code FTBossMod} 里的注释），
 * 所以新事件类一律显式注册；挂在 {@code BossHandler} 上是为了**不动 {@code FTBossMod.java}**
 * （那个文件当前部署版里还有别的线程加的一行 SanguineBond 注册，见构建合并说明）。
 */
public final class ManorChaseTracker {

    public ManorChaseTracker() {}

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        try {
            if (!(event.getEntity() instanceof Player)) return;
            if (event.getAmount() <= 0.0f) return;
            Entity source = event.getSource().getEntity();
            if (source == null) source = event.getSource().getDirectEntity();
            EasterBossEntity boss = resolveBoss(source);
            if (boss != null) boss.onPlayerHitLanded();
        } catch (Throwable ignored) {
            /* 计时器出问题绝不能让伤害结算崩掉 */
        }
    }

    /** 顺投射物主人 / 召唤物主人链找负责的彩蛋 Boss。 */
    private static EasterBossEntity resolveBoss(Entity source) {
        for (int depth = 0; source != null && depth < 8; depth++) {
            if (source instanceof EasterBossEntity boss) return boss;
            if (source instanceof Projectile projectile && projectile.getOwner() != null) {
                source = projectile.getOwner();
                continue;
            }
            Entity owner = ManorFightMembers.owner(source);
            if (owner == null || owner == source) return null;
            source = owner;
        }
        return null;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ManorFlyBan.clear((ServerLevel) null);
        FTBossMod.LOGGER.info("[FTBoss][Manor] 服务器停机: 清空场地禁飞窗口");
    }
}
