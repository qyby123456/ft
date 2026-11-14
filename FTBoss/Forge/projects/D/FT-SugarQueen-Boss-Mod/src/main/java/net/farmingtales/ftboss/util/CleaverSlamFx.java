package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 【黑色切割者】下砸落点冲击环（规格 2026-09-16 §7「下砸特效」的可复用实现）。
 *
 * <p><b>这一版只做"与触发点无关"的部分</b>：给一个世界坐标，画出规格要求的落点表现。
 * 谁触发（玩家右键 / 命中 / 功底的下砸 / 雪黎式落地）由调用方决定 ——
 * 本类<b>不改伤害、不改攻击范围、不碰冷却与 Buff</b>，纯装饰。
 *
 * <p>规格逐条对应：
 * <ul>
 *   <li><b>命中时</b>：阵心短促闪光 + 外环快速展开 + 一次少量碎屑 + 一次冲击音效；</li>
 *   <li><b>命中后</b>：客户端按 tick 展开血色冲击环，短尾粒子自然淡出；</li>
 *   <li><b>寿命</b>：{@link #LIFE_TICKS} = 16 tick，落在规格的 12~18 区间；</li>
 *   <li><b>地面处理</b>：用<b>碰撞形状</b>找真实支撑面；<b>不加载新区块</b>
 *       （{@code isLoaded} 先判，未加载直接退化）；命中墙面/无支撑 → 退化为小型命中闪光，
 *       <b>不生成水平悬空圆阵</b>；台阶/坡面/悬崖与低天花板分别缩小或缩短；</li>
 *   <li><b>粒子</b>：一次短促爆发，大小/速度带少量变化，<b>不生成真实方块</b>；</li>
 *   <li><b>音效</b>：命中一次重击 + 一次金属余响，<b>绝不每 tick 播</b>；</li>
 *   <li><b>纯装饰</b>：环的半径与技能的伤害半径<b>不绑定</b>，避免让玩家误以为它代表额外伤害范围
 *       —— 所以本类不接受"半径"参数，统一用 {@link #RING_RADIUS}。</li>
 * </ul>
 *
 */
public final class CleaverSlamFx {

    /** 规格要求的整体寿命（tick）：12~18 之间取 16。 */
    public static final int LIFE_TICKS = 16;

    /** 装饰环半径（格）。**故意与伤害半径解耦** —— 别让玩家把它当成伤害范围。 */
    public static final double RING_RADIUS = 2.6D;

    /** 找支撑面时最多往下找几格；找不到就退化为小型闪光。 */
    private static final int GROUND_SEARCH_DOWN = 3;

    /** 能量柱需要的净空高度；不够就缩短/不画（低天花板）。 */
    private static final double COLUMN_CLEARANCE = 2.5D;

    private CleaverSlamFx() {}

    /**
     * 在 (x,y,z) 处播放下砸落点表现。**服务端调用**（用 {@code level.sendParticles}）。
     *
     * @param impactY 技能判定的落点（通常是实体/射线命中点）；本类会在它附近找真实地面
     */
    public static void play(Level level, double x, double impactY, double z) {
        if (!(level instanceof ServerLevel sl)) return;
        BlockPos base = BlockPos.containing(x, impactY, z);
        /* 不强制加载新区块（规格：不强制加载新区块） */
        if (!sl.isLoaded(base)) return;

        double groundY = findGroundY(sl, base);
        if (Double.isNaN(groundY)) {
            /* 无支撑面（悬空命中 / 命中墙面）→ 退化为小型命中闪光，**不生成悬空圆阵** */
            sl.sendParticles(ParticleTypes.FLASH, x, impactY, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            Fx.particles(sl, x, impactY, z, 10, ParticleTypes.SQUID_INK, 0.35D, 0.08D);
            Fx.sound(sl, x, impactY, z, SoundEvents.ANVIL_LAND, 0.7F, 0.7F);
            return;
        }

        boolean roomy = hasClearance(sl, x, groundY, z);
        double r = roomy ? RING_RADIUS : RING_RADIUS * 0.6D;      // 低天花板：整体缩小
        double top = groundY + 0.06D;

        /* 1) 阵心短促闪光（一次） */
        sl.sendParticles(ParticleTypes.FLASH, x, top + 0.4D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        WeaponFx.burst(sl, new Vec3(x, top, z), r, Theme.BLOOD);

        /* 3) 一次少量碎屑：数量少、速度带变化（规格：不通过生成真实方块制造碎片） */
        Fx.dust(sl, x, top + 0.2D, z, 26, 0.32F, 0.12F, 0.42F, r * 0.9D, 0.18D);
        Fx.particles(sl, x, top + 0.3D, z, 12, ParticleTypes.CRIT, 0.7D, 0.14D);

        /* 4) 能量柱：只在净空够时画，且**快速消失**（这里只给一瞬的上冲柱） */
        if (roomy) {
            for (int i = 0; i < 10; i++) {
                double yy = top + 0.25D + i * 0.22D;
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, x, yy, z, 2, 0.10D, 0.02D, 0.10D, 0.0D);
            }
        }

        /* 5) 音效：命中一次重击 + 一次金属余响（**不每 tick 播**） */
        Fx.sound(sl, x, groundY, z, SoundEvents.ANVIL_LAND, 1.0F, 0.62F);
        Fx.sound(sl, x, groundY, z, SoundEvents.NETHERITE_BLOCK_HIT, 0.6F, 1.25F);
    }

    /**
     * 用**碰撞形状**找真实支撑面（规格：使用碰撞形状寻找真实支撑面）。
     * 返回可站立的 y（该方块之上），找不到返回 {@link Double#NaN}。
     */
    private static double findGroundY(ServerLevel sl, BlockPos base) {
        for (int dy = 0; dy <= GROUND_SEARCH_DOWN; dy++) {
            BlockPos p = base.below(dy);
            if (!sl.isLoaded(p)) return Double.NaN;
            VoxelShape shape = sl.getBlockState(p).getCollisionShape(sl, p);
            if (shape.isEmpty()) continue;
            /* 该方块顶面之上的两格必须是空的，否则这里站不住/是墙里 */
            AABB top = shape.bounds().move(p);
            double y = top.maxY;
            if (sl.isEmptyBlock(p.above()) && sl.isEmptyBlock(p.above(2))) return y;
        }
        return Double.NaN;
    }

    /** 落点上方是否有足够净空（低天花板 → 缩短能量柱并整体缩小）。 */
    private static boolean hasClearance(ServerLevel sl, double x, double groundY, double z) {
        BlockPos p = BlockPos.containing(x, groundY + 0.1D, z);
        for (int i = 1; i <= (int) Math.ceil(COLUMN_CLEARANCE); i++) {
            if (!sl.isEmptyBlock(p.above(i))) return false;
        }
        return true;
    }
}
