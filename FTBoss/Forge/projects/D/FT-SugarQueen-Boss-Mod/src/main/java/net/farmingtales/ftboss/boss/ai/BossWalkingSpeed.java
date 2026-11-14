package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class BossWalkingSpeed {
    public static final double BASE_SPEED = 0.1;
    public static final double BLOCKS_PER_TICK = 0.21585;

    /* ==================== ★ 2026-09-24 按用户要求「提高 boss 在水中移速」 ====================
       水里本来是"比陆地慢一截"：原版水阻尼 {@code LivingEntity.getWaterSlowDown()} 默认 **0.8**
       （只有"海豚的恩惠"才是 0.96），而且下面 {@link #speed} 的速度上限也按陆地那一档夹。
       落水的 Boss 因此变成活靶子 —— 追不上人、也绕不了后。

       三条一起提：
         · 在水里给 {@code MOVEMENT_SPEED} 挂一个 {@value #WATER_SPEED_BONUS_PERCENT}% 的
           **瞬时**修饰符（{@code addTransientModifier}，不进存档、出水立刻摘掉）；
         · {@link #speed} 的上限跟着放宽同样倍率 —— 否则非彩蛋系的 Boss 会被
           {@code min(BASE_SPEED, …)} 那个夹子原样夹回陆地速度（这一条很容易漏）；
         · 彩蛋系（{@link EasterBossEntity}）另有 {@code getWaterSlowDown()} 覆盖，
           把水阻尼抬到 {@link #WATER_SLOW_DOWN}（0.96，与海豚的恩惠同档）。

       想再调只改 {@link #WATER_SPEED_BONUS} 一个数；不想要这条功能就把 onLivingTick 摘掉。 */
    /** 水中移速加成（+45%）。 */
    public static final double WATER_SPEED_BONUS = 0.45;
    /** 文档/日志里显示用的百分比。 */
    public static final int WATER_SPEED_BONUS_PERCENT = 45;
    /** 水阻尼：原版 0.8，这里抬到与"海豚的恩惠"同档。 */
    public static final float WATER_SLOW_DOWN = 0.96F;
    /** 水中速度修饰符的固定 UUID（瞬时，不写存档）。 */
    private static final UUID WATER_SPEED_MODIFIER = UUID.fromString("2f4a1c6e-6c2b-4a1e-9d3f-1b7c5e0a9d21");

    private BossWalkingSpeed() {}

    public static boolean applies(Mob boss) {
        if (boss instanceof EasterBossEntity easter) return !easter.isMini() && !easter.isImpossibleChallenge();
        if (boss instanceof RatBossEntity rat) return !rat.isMinion();
        return boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity
                || boss instanceof AppleBossEntity || boss instanceof CompostBossEntity
                || boss instanceof BengerEntity || boss instanceof TideFishBossEntity
                || boss instanceof DefectCoreEntity;
    }

    /** 这个 Boss 此刻是不是泡在水里（加成与上限放宽共用这一个判据）。 */
    public static boolean inWater(Mob boss) {
        return boss != null && boss.isInWater();
    }

    public static float speed(Mob boss, float requested) {
        if (!applies(boss)) return requested;
        double attribute = boss.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double base = boss instanceof EasterBossEntity ? attribute : Math.min(BASE_SPEED, attribute);
        /* ★ 落水时上限同步放宽同样的倍率 —— 否则 requested 涨了也会被这里夹回陆地速度。 */
        double maximum = base * (inWater(boss) ? 1.0 + WATER_SPEED_BONUS : 1.0);
        float limited = Float.isFinite(requested) ? Math.max(0, Math.min((float) maximum, requested)) : 0;
        return boss instanceof EasterBossEntity || inWater(boss) || boss.isNoGravity()
                ? limited : limited * 2.0F;
    }

    /**
     * ★ 2026-09-24：落水时挂上/摘掉速度修饰符。
     *
     * <p>用 {@code addTransientModifier}（**瞬时**修饰符，不写存档），
     * 所以"退出存档时正泡在水里"不会把加成永久烙在 Boss 身上 —— 下次进来按当时的水况重新判定。
     */
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob boss) || !applies(boss)) return;
        var movement = boss.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        boolean wet = inWater(boss);
        boolean applied = movement.getModifier(WATER_SPEED_MODIFIER) != null;
        if (wet == applied) return;                     // 状态没变就不动它（每 tick 只做一次判断）
        try {
            if (wet) {
                movement.addTransientModifier(new AttributeModifier(WATER_SPEED_MODIFIER,
                        "ftboss_water_speed", WATER_SPEED_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else {
                movement.removeModifier(WATER_SPEED_MODIFIER);
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 水中移速修饰符切换失败(已忽略): {}", t.toString());
        }
    }

    public static void normalizeInput(Mob boss) {
        if (applies(boss)) boss.setZza(boss.getSpeed() > 0 ? 1 : 0);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof EasterBossEntity easter && !easter.isMini()) {
            easter.refreshWalkingSpeed();
            return;
        }
        if (!(event.getEntity() instanceof Mob boss) || !applies(boss)) return;
        var movement = boss.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) movement.setBaseValue(BASE_SPEED);
    }
}
