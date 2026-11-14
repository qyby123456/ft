package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * 【是否真的在打 Boss】(2026-09-13 v84 按用户反馈新增)。
 *
 * <p>背景: 之前"麻雀随机支援"( {@code SparrowSupport} )与"Forge 的祝福"( {@code ForgeBlessing} )
 * 都只判断"玩家附近**存在**本模组的 Boss 实体"。于是:
 * <ul>
 *   <li>**遗留/自己召唤出来的 Boss**(例如某只没人管的苹果爵士)站在旁边, 就会被当成"正在交战";</li>
 *   <li>祝福更是完全没判定, 每 60 秒无条件发。</li>
 * </ul>
 * 用户反馈"没进 Boss 战也触发了玩家 buff 和召唤麻雀"。
 *
 * <p>现在统一用这里的两条判据:
 * <ol>
 *   <li>玩家在**庄园战场**范围内( {link ManorFight#isPlayerFighting} ) —— 战场正在进行;</li>
 *   <li>{@value #ENGAGE_RANGE} 格内有本模组 Boss **正把这个玩家当目标**(它确实在打你)。</li>
 * </ol>
 */
public final class FightState {

    /** "在交战"的判定半径(格) */
    public static final double ENGAGE_RANGE = 64.0;

    private FightState() {}

    /** 玩家是否真的在打 Boss(见类注释的两条判据) */
    public static boolean inFight(ServerPlayer p) {
        if (p == null || !p.isAlive()) return false;
        if (ManorFight.isPlayerFighting(p)) return true;
        if (!(p.level() instanceof ServerLevel sl)) return false;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(ENGAGE_RANGE))) {
            if (!isBoss(le)) continue;
            if (le instanceof Mob m && m.getTarget() == p) return true;
        }
        return false;
    }

    /** 该 Boss 是否正把这个玩家当目标(供召唤判定用) */
    public static boolean isEngagedWith(LivingEntity boss, Player p) {
        return boss instanceof Mob m && m.getTarget() == p && isBoss(boss);
    }

    /** 是不是"本模组的 Boss"(按命名空间判断; 支援位麻雀与柚子分身不算) */
    public static boolean isBoss(Entity e) {
        if (e == null || !e.isAlive() || e.isRemoved()) return false;
        if (e instanceof EasterBossEntity eb
                && (eb.getVariant() == EasterBossEntity.SPARROW_VARIANT || eb.isMini())) return false;
        var key = EntityType.getKey(e.getType());
        return key != null && FTBossMod.MOD_ID.equals(key.getNamespace());
    }
}
