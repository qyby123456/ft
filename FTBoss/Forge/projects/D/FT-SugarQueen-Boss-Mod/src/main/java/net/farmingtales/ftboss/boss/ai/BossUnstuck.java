package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 「防卡瞬移」(2026-09-11 按用户要求新增): Boss 每隔一段时间自查**有没有位移**,
 * 若原地不动(被地形/方块卡住、被围住、寻路失败)就**瞬移到玩家附近的安全点**。
 *
 * <p>判定: 每 {@value #CHECK} tick 记一次坐标, 下一次检查时若水平位移 &lt; {@value #MIN_MOVE} 格,
 * 且离目标 &gt; {@value #MIN_TARGET_DIST} 格 —— 就算卡住(正在咏唱/正在冲刺的不算, 免得打断演出)。
 * 瞬移用 {@link LivingEntity#randomTeleport} (末影人同款, 自带落点安全检查), 失败则退化为"挪到目标上方"。
 *
 * <p><b>只给会移动的 Boss 挂</b>: 沙耶是扎根的(移速 0, 本来就不该动), 因此不调用本类。
 * 状态放在 persistentData 里, 不需要额外字段。
 */
public final class BossUnstuck {

    /** 检查周期(tick) */
    private static final int CHECK = 80;
    /** 一个周期内水平位移小于这个值 => 判定卡住 */
    private static final double MIN_MOVE = 2.0;
    /** 离目标太近(在贴身打)就不判卡 */
    private static final double MIN_TARGET_DIST = 4.0;

    private static final String K_T = "ftboss_unstuck_t";
    private static final String K_X = "ftboss_unstuck_x";
    private static final String K_Z = "ftboss_unstuck_z";

    private BossUnstuck() {}

    /** 每 tick 调用(服务端, 由会移动的 Boss 转发) */
    public static void tick(LivingEntity boss) {
        if (boss.level().isClientSide() || boss.isDeadOrDying()) return;
        if (!(boss.level() instanceof ServerLevel sl)) return;

        var tag = boss.getPersistentData();
        int t = tag.getInt(K_T);
        if (t <= 0) {
            tag.putInt(K_T, CHECK);
            tag.putDouble(K_X, boss.getX());
            tag.putDouble(K_Z, boss.getZ());
            return;
        }
        tag.putInt(K_T, t - 1);
        if (t > 1) return;

        double moved = Math.sqrt(
                Math.pow(boss.getX() - tag.getDouble(K_X), 2)
              + Math.pow(boss.getZ() - tag.getDouble(K_Z), 2));
        tag.putDouble(K_X, boss.getX());
        tag.putDouble(K_Z, boss.getZ());
        if (moved >= MIN_MOVE) return;

        // 没有目标 / 目标就在身边 => 不动是正常的
        LivingEntity target = (boss instanceof Mob m) ? m.getTarget() : null;
        if (target == null || boss.distanceTo(target) <= MIN_TARGET_DIST) return;
        // 正在咏唱(铁魔法)时不动是正常的, 别打断
        /* ★ v112 修复: 这里原来直接写 `boss instanceof io.redspace.ironsspellbooks...IMagicEntity`,
           而铁魔法是**可选依赖** —— 缺它的时候方法体的类型解析会抛 NoClassDefFoundError,
           本类又被 4 只 Boss 的 tick 每 80t 转发一次(SugarQueenEntity:133 / MeatMountainEntity:140 /
           RatBossEntity:196 / WineBarrelBossEntity:176) -> 直接崩服。改成反射探测,
           和 IronSpellBridge / ManorSpellBan 的兜底同一套路。 */
        if (castingByReflection(boss)) return;

        // 卡住了: 瞬移到目标附近的安全点
        for (int i = 0; i < 10; i++) {
            double a = boss.getRandom().nextDouble() * Math.PI * 2;
            double r = 2.0 + boss.getRandom().nextDouble() * 3.5;
            double x = target.getX() + Math.cos(a) * r;
            double z = target.getZ() + Math.sin(a) * r;
            int y = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(x), (int) Math.floor(z));
            if (boss.randomTeleport(x, y + 0.1, z, false)) {
                Fx.particles(sl, x, y + 1.0, z, 24, ParticleTypes.REVERSE_PORTAL, 0.4, 0.04);
                Fx.particles(sl, boss.getX(), boss.getY() + 1.0, boss.getZ(), 16, ParticleTypes.PORTAL, 0.3, 0.05);
                sl.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.9f);
                FTBossMod.LOGGER.info("[FTBoss] 防卡瞬移: {} -> ({}, {}, {}) ({}t 内只挪了 {} 格)",
                        boss.getName().getString(), String.format("%.1f", x), y, String.format("%.1f", z),
                        CHECK, String.format("%.1f", moved));
                return;
            }
        }
        // 兜底: 直接落到目标脚边
        boss.teleportTo(target.getX(), target.getY() + 0.5, target.getZ());
        FTBossMod.LOGGER.info("[FTBoss] 防卡瞬移(兜底): {} -> 目标脚边", boss.getName().getString());
    }

    /* ==================== 可选依赖(铁魔法)的反射探测 ==================== */
    /** 铁魔法"正在咏唱"的接口/方法(查一次就缓存; 缺 mod 时全为 null) */
    private static Class<?> MAGIC_IFACE;
    private static java.lang.reflect.Method IS_CASTING;
    private static boolean magicLookedUp = false;

    /** 反射判"这个 Boss 是不是正在咏唱"(铁魔法缺失时安静返回 false, 绝不抛异常) */
    private static boolean castingByReflection(LivingEntity boss) {
        try {
            if (!magicLookedUp) {
                magicLookedUp = true;
                MAGIC_IFACE = Class.forName("io.redspace.ironsspellbooks.api.entity.IMagicEntity");
                IS_CASTING = MAGIC_IFACE.getMethod("isCasting");
            }
            if (MAGIC_IFACE == null || IS_CASTING == null || !MAGIC_IFACE.isInstance(boss)) return false;
            Object r = IS_CASTING.invoke(boss);
            return r instanceof Boolean b && b;
        } catch (Throwable t) {
            MAGIC_IFACE = null;
            IS_CASTING = null;
            return false;
        }
    }
}
