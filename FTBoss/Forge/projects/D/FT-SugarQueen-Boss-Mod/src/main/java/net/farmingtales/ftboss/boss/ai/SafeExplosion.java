package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 【爆炸不炸掉落物】(2026-09-14 v106 按用户反馈「爵士的爆炸会炸毁凋落物」)。
 *
 * <p>为什么原版爆炸会连**掉落物**一起炸没: {@code Level.explode(..., ExplosionInteraction.NONE)}
 * 只是"不破坏方块", 它对**实体的伤害照样结算** —— {@link ItemEntity#hurt} 是有的, 所以地上
 * 一堆战利品被爆炸扫过就直接消失了。我们的 Boss 里有一堆 {@code EXPLOSION} 伤害来源的爆炸
 * (苹果爵士的烂苹果堆自爆、酒桶王的炮、故障机器人的超载、肥料王的爆炸…), 每次开打都在
 * 悄悄吃掉玩家没来得及捡的东西。
 *
 * <p>修法用 Forge 给的正路: 监听 {@link ExplosionEvent.Detonate}, 把**掉落物从受影响实体列表里
 * 摘掉**({@code getAffectedEntities().removeIf(...)})。这件事发生在伤害结算之前, 所以
 * 掉落物毫发无损, 而玩家/Boss 该吃的伤害一点不少。
 *
 * <p>范围刻意收窄: **只有爆炸的直接来源是我们这个模组的实体时才动手** —— 原版 TNT、苦力怕、
 * 玩家自己的爆炸物行为完全不变(用户要的是"Boss 的爆炸别炸我的战利品", 不是改原版规则)。
 */
public final class SafeExplosion {

    private SafeExplosion() {}

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate e) {
        try {
            Entity src = e.getExplosion().getDirectSourceEntity();
            if (src == null) src = e.getExplosion().getIndirectSourceEntity();
            if (src == null) return;
            /* 只保护"我们模组的实体造成的爆炸"(ftboss 包下的实体/投射物) */
            String pkg = src.getClass().getPackageName();
            if (pkg == null || !pkg.startsWith("net.farmingtales.ftboss")) return;

            List<ItemEntity> items = new ArrayList<>();
            for (Entity en : e.getAffectedEntities()) {
                if (en instanceof ItemEntity ie) items.add(ie);
            }
            if (items.isEmpty()) return;
            e.getAffectedEntities().removeAll(items);
            FTBossMod.LOGGER.info("[FTBoss][爆炸] 保住了 {} 件掉落物({} 的爆炸)",
                    items.size(), src.getName().getString());
        } catch (Throwable ignored) {
            /* 保护失败也绝不能让爆炸本身崩掉 */
        }
    }
}
