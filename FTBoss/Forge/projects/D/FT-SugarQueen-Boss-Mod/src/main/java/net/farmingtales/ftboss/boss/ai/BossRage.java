package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * 全 Boss 通用的「受伤上限 + 狂暴阶段」(参考传奇怪物类模组的 Boss 反馈设计)。
 *
 * <h3>① 单次受伤上限 / 连击递减 / 反逃课距离</h3>
 * 见 {@link BossDamageGate}(逐条对齐传奇怪物 <i>Legendary Monsters</i> 的 {@code damageCap()} 限伤体系)。
 *
 * <h3>② 狂暴阶段</h3>
 * 血量 ≤50% 进入 <b>狂暴 I</b>(力量/抗性/迅捷)，≤25% 进入 <b>狂暴 II</b>(再叠加再生)，
 * <b>例外: 故障机器人(DefectCoreEntity)走纯进攻档</b> —— 迅捷 + 力量, 不给抗性与再生(用户要求削减其防御buff)；
 * 触发时只有咆哮 + 红色粒子, <b>不再发聊天播报</b>(阶段提醒只保留沙耶茶芽庇佑)；
 * 层级写在实体持久数据里(不会因区块重载重复触发)。
 */
public final class BossRage {

    /** 持久数据键: 已进入的狂暴层级 */
    private static final String NBT_TIER = "ftboss_rage_tier";

    /** 单次受伤上限已迁移到 {@link BossDamageGate}(传奇怪物式)，此处只保留狂暴阶段。 */
    private BossRage() {}

    /** 每 tick 调用(服务端): 检查血量阈值、维持狂暴增益与粒子 */
    public static void tickBoss(LivingEntity boss) {
        if (boss.level().isClientSide() || boss.isDeadOrDying()) return;

        int tier = boss.getPersistentData().getInt(NBT_TIER);
        float ratio = boss.getHealth() / Math.max(1.0F, boss.getMaxHealth());
        int want = ratio <= 0.25F ? 2 : (ratio <= 0.50F ? 1 : 0);

        if (want > tier) {
            boss.getPersistentData().putInt(NBT_TIER, want);
            tier = want;
            enterRage(boss, tier);
        }
        if (tier <= 0) return;

        if (boss.tickCount % 20 == 0) refreshRage(boss, tier);
        if (boss.tickCount % 5 == 0) rageParticles(boss, tier);
    }

    /** 死亡/重置时清理分层(下次召唤重新算) */
    public static void reset(LivingEntity boss) {
        boss.getPersistentData().putInt(NBT_TIER, 0);
    }

    /** 当前狂暴层级(0/1/2), 供其它系统做数值或特效分支 */
    public static int tierOf(LivingEntity boss) {
        return boss.getPersistentData().getInt(NBT_TIER);
    }

    private static void enterRage(LivingEntity boss, int tier) {
        Level level = boss.level();
        double x = boss.getX(), y = boss.getY() + 1.0, z = boss.getZ();

        Fx.sound(level, x, y, z, SoundEvents.RAVAGER_ROAR, 1.7F, tier >= 2 ? 0.7F : 0.9F);
        Fx.sound(level, x, y, z, SoundEvents.WITHER_SPAWN, 0.5F, 1.7F);

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(new DustParticleOptions(new Vector3f(0.60F, 0.03F, 0.03F), 1.3F),
                    x, y, z, tier >= 2 ? 60 : 35, 1.2, 1.0, 1.2, 0.18);
            Fx.ring(level, x, boss.getY() + 0.2, z, tier >= 2 ? 3.6 : 2.6, tier >= 2 ? 40 : 26,
                    ParticleTypes.ANGRY_VILLAGER, 0.02);
            // 按需求: 阶段切换不再发聊天播报(阶段提醒只保留沙耶茶芽庇佑), 仅保留上方音效+粒子演出
        }
    }

    private static void refreshRage(LivingEntity boss, int tier) {
        // 狂暴 I: 迅捷 I + 抗性 I; 狂暴 II: 额外 力量 I + 再生 I(整体已下调, 不再给 II/III 级)
        //
        // 2026-09-11: **故障机器人是例外** —— 用户明确要求"减少机器人的防御buff", 所以它走纯进攻档:
        // 迅捷(狂暴II 再 +1 级) + 狂暴II 的力量, **不给抗性、不给再生**(它的防御全靠冰球那点薄霜甲)。
        boolean offenseOnly = boss instanceof net.farmingtales.ftboss.entity.DefectCoreEntity;
        boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80,
                offenseOnly && tier >= 2 ? 1 : 0, false, false, true));
        if (!offenseOnly) {
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false, true));
        }
        if (tier >= 2) {
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, offenseOnly ? 1 : 0, false, false, true));
            if (!offenseOnly) {
                boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, false, false, true));
            }
        }
    }

    private static void rageParticles(LivingEntity boss, int tier) {
        Fx.dust(boss.level(), boss.getX(), boss.getY() + 1.0, boss.getZ(),
                tier >= 2 ? 6 : 3, 0.55F, 0.04F, 0.04F, 0.7, 0.02);
    }
}
