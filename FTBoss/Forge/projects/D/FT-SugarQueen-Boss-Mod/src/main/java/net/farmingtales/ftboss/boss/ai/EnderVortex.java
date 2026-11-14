package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 【末影守卫的虚空漩涡】(2026-09-14 v124 按用户要求「蛋蛋现在可以使用末影守卫的漩涡吸引玩家」)。
 *
 * <p>末影守卫(灾变 {@code cataclysm:ender_guardian})的招牌"虚空漩涡"其实是**一个独立实体**
 * {@code cataclysm:void_vortex}(来源是「漩涡护手」右键召唤的那个), 它自带收拢/嘶吼的整套演出,
 * 所以直接**把它召唤出来用**比自己写粒子要像得多 —— 这也是"使用末影守卫的漩涡"的字面做法。
 *
 * <p>两点实现说明:
 * <ol>
 *   <li>本模组**不依赖**灾变编译(它的 jar 不在编译期 classpath 上), 所以只能走
 *       {@code ForgeRegistries.ENTITY_TYPES} 按 id 取 {@link EntityType} 再 {@code create(level)} ——
 *       拿不到就返回 null(那局就只播自绘粒子 + 我们自己的吸引力, 技能照样生效)。</li>
 *   <li>它那两个"设置"方法是模组自己的方法(不走 SRG 映射), 名字在运行时就是
 *       {@code setLifespan}/{@code setOwner}, 所以用反射调一下就能控制寿命与归属;
 *       反射失败也不影响 —— 它的默认寿命本来就是为这个技能设计的。</li>
 * </ol>
 *
 * <p><b>注意</b>: 漩涡本体对**玩家**的吸引力并不可靠(玩家位置是客户端权威的, 老问题),
 * 所以"把玩家吸过去"这件事由 {@code EasterBossEntity.dandanVortexTick} 自己每 tick 结算
 * (teleport 硬拽), 这个类只负责<b>演出</b>。
 */
public final class EnderVortex {

    /** 灾变的虚空漩涡实体 id */
    public static final ResourceLocation VOID_VORTEX = new ResourceLocation("cataclysm", "void_vortex");

    private static Boolean available = null;

    private EnderVortex() {}

    /** 这台整合包里有没有灾变的虚空漩涡 */
    public static boolean available() {
        if (available == null) available = ForgeRegistries.ENTITY_TYPES.getValue(VOID_VORTEX) != null;
        return available;
    }

    /**
     * 在指定位置撕开一个虚空漩涡。
     *
     * @param ticks 期望寿命(tick) —— 到点我们自己也会把它 discard, 这里只是别让它提前消失
     * @return 召出来的实体; 灾变缺失/创建失败时返回 null(调用方自己画粒子兜底)
     */
    public static Entity spawn(ServerLevel sl, LivingEntity owner, double x, double y, double z, int ticks) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(VOID_VORTEX);
        if (type == null) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 没有找到 {} —— 虚空漩涡只放自绘粒子(吸引力照常生效)", VOID_VORTEX);
            return null;
        }
        try {
            Entity e = type.create(sl);
            if (e == null) return null;
            e.moveTo(x, y, z, owner != null ? owner.getYRot() : 0.0f, 0.0f);
            boolean lifespanOk = false;
            boolean ownerOk = false;
            try {
                e.getClass().getMethod("setLifespan", int.class).invoke(e, ticks);
                lifespanOk = true;
            } catch (Throwable ignored) { /* 用它的默认寿命 */ }
            if (owner != null) {
                try {
                    e.getClass().getMethod("setOwner", LivingEntity.class).invoke(e, owner);
                    ownerOk = true;
                } catch (Throwable ignored) { /* 无归属也能转 */ }
            }
            sl.addFreshEntity(e);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 末影守卫的虚空漩涡已召唤 @({}, {}, {}) 寿命 {} tick (setLifespan={}, setOwner={})",
                    String.format("%.1f", x), String.format("%.1f", y), String.format("%.1f", z),
                    ticks, lifespanOk, ownerOk);
            return e;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 虚空漩涡召唤失败: {}", t.toString());
            return null;
        }
    }
}
