package net.farmingtales.ftboss.boss.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Map;

/**
 * 传奇怪物(Legendary Monsters)式限伤系统 —— 逐条对齐其 {@code IAnimatedBoss} 的实现：
 *
 * <ol>
 *   <li><b>放行绕过无敌的伤害</b>：{@code source.is(BYPASSES_INVULNERABILITY)} 时不吃上限
 *       （对应 LM: {@code if (source.is(tag)) return super.hurt(...)}）。</li>
 *   <li><b>单次伤害上限</b>：{@code amount = min(damageCap(), amount)}
 *       （LM 默认 21.0 / 云巨人 18.0 / 小Boss 21）。</li>
 *   <li><b>连击递减</b>：最近 {@value #ADAPT_TICKS} tick 内被打过则按"伤害时间因子"百分比缩放，
 *       下限 {@value #COMBO_FLOOR}%（LM: {@code amount *= clamp(toPercent(reducedDamageTicks), 0.5f, amount)}）。
 *       因子每挨一次打 -{@value #FACTOR_STEP}，{@value #ADAPT_TICKS} tick 无伤自动复位 100。</li>
 *   <li><b>反逃课距离</b>：攻击者/当前目标距离超过 antiCheeseDistance 时完全免伤
 *       （LM: {@code if (distanceTo(getTarget()) > antiCheeseDistance()) return false}，默认 15 格）。</li>
 * </ol>
 *
 * 用法（Boss 的 hurt()）：
 * <pre>
 * float gated = BossDamageGate.gate(this, source, amount);
 * if (gated &lt;= 0F) return false;                       // 被限伤/距离判定拦下
 * boolean hit = super.hurt(source, gated);
 * if (hit) BossDamageGate.onHit(this);                 // 结算成功才推进连击计数
 * return hit;
 * </pre>
 */
public final class BossDamageGate {

    /**
     * 限伤总开关。
     * false = 取消单次伤害上限 / 连击递减 / 反逃课距离, 玩家打多少就吃多少;
     * 只保留「完全无视真实伤害与流血」那一条(那是免伤, 不是限伤)。
     */
    public static final boolean LIMIT_ENABLED = false;

    /** 单次伤害上限(传奇怪物默认 21.0, 按其 Boss 血量档位等比缩放) */
    private static final Map<String, Double> DAMAGE_CAP = Map.of(
            "ftboss:sugar_queen", 58.0,   // 糖霜女皇 480HP(已削弱)
            "ftboss:meat_mountain", 58.0, // 屠夫 480HP(已削弱)
            "ftboss:tea_tree", 56.0,      // 沙耶 280HP(带召唤物, 单次上限放宽)
            "ftboss:wine_barrel", 58.0,   // 酒魔·酒桶王 480HP(已削弱)
            "ftboss:rat_boss", 46.0       // 鼠群大老鼠 380HP(已削弱)
    );
    /** 兜底上限(未登记的 Boss) */
    private static final double DEFAULT_CAP = 48.0;

    /** 反逃课距离(格): 超过此距离的攻击完全免伤; <=0 表示关闭 */
    private static final Map<String, Double> ANTI_CHEESE = Map.of(
            "ftboss:sugar_queen", 32.0,
            "ftboss:meat_mountain", 32.0,
            "ftboss:tea_tree", 40.0,      // 茶树本体远程施法, 放宽
            "ftboss:wine_barrel", 32.0,
            "ftboss:rat_boss", 32.0,
            "ftboss:defect_core", 36.0      // 故障机器人(第 6 个 Boss): 悬浮远程, 放宽一点
    );
    private static final double DEFAULT_ANTI_CHEESE = 32.0;

    /** 连击窗口(tick): 略短于 LM 的 30, 避免削弱手感 */
    public static final int ADAPT_TICKS = 24;
    /** 每次受击, 伤害时间因子递减量(进一步放宽到 6) */
    public static final int FACTOR_STEP = 6;
    /** 伤害时间因子下限(LM: DamageTime() = max(20, factor)) */
    public static final int FACTOR_MIN = 20;
    /** 伤害时间因子满值 */
    public static final int FACTOR_MAX = 100;
    /** 连击减伤下限(进一步放宽到 0.7) */
    public static final float COMBO_FLOOR = 0.7F;
    /** 受击无敌帧(LM: HURT_COOLDOWN 30) */
    public static final int HURT_CD = 30;

    private static final String K_ADAPT = "ftboss_adapt_ticks";
    private static final String K_FACTOR = "ftboss_time_factor";
    private static final String K_CD = "ftboss_hurt_cd";
    private static final String K_COUNT = "ftboss_hurt_count";
    private static final String K_NOTIFY = "ftboss_anticheese_notify";

    /** 完全免疫的伤害命名空间: armamentarium(幽匿系列装备/亵渎战矛)的真实伤害与流血伤害
     *  2026-09-13 v83 修「Boss 卡血条、打不掉血」: **这一条以前是"整个 armamentarium 命名空间一律免疫"**,
     *  而 armamentarium 的**普通武器伤害**也走它自家的伤害类型 → 玩家拿那套武器打 Boss **一下都打不掉**
     *  (日志/反馈: 血条纹丝不动、连 /kill 都杀不死)。现在改成:
     *  **命名空间不再单独作为免伤依据**, 只看下面的"流血/真实伤害"关键字表 ——
     *  与 v27 的本意(只挡血王子焰形剑的流血、亵渎战矛的末日真伤)一致。 */
    private static final java.util.Set<String> IMMUNE_NAMESPACES = java.util.Set.of();

    /** 其它要完全免疫的伤害类型路径/msgId 关键字(含流血与末日真实伤害)
     *  2026-09-10 深夜按用户要求补强: armamentarium「血王子」系列(焰形剑/孪生匕首)的流血走的是
     *  自定义伤害类型 {@code armamentarium:hemorrhage} —— 这里把"流血"类关键字放宽,
     *  防止别的 mod 用相似 id 绕过。
     *  2026-09-13 v83: 这里**不再加宽关键字**(曾想加 "blood" 兜底, 但那会再次误伤
     *  武器普通伤害里带 blood 的类型); 只保留精确的流血/真伤 id。
     *  真的还有漏网的, 日志里会打 `[FTBoss][免疫] ... 伤害类型 xxx` —— 照着补 id 即可。 */
    private static final java.util.Set<String> IMMUNE_IDS = java.util.Set.of(
            "hemorrhage",       // 流血(Blood Loss 出血伤害)
            "blood_loss",
            "bloodloss",
            "bleed",
            "bleeding",
            "doom",             // 末日(亵渎战矛 Impending Doom 真实伤害)
            "impending_doom",
            "executioner", "assassination", "desecration", "true_damage"
    );

    private static final String K_IMMUNE_NOTIFY = "ftboss_immune_notify";

    private BossDamageGate() {}

    /** 是否为「无视」的伤害: armamentarium 自定义伤害类型, 或流血/末日等真实伤害 */
    public static boolean isImmune(DamageSource source) {
        var key = source.typeHolder().unwrapKey();
        if (key.isPresent()) {
            var loc = key.get().location();
            if (IMMUNE_NAMESPACES.contains(loc.getNamespace())) return true;
            if (IMMUNE_IDS.contains(loc.getPath())) return true;
        }
        String msg = source.getMsgId();
        if (msg != null) {
            String low = msg.toLowerCase(java.util.Locale.ROOT);
            for (String s : IMMUNE_IDS) {
                if (low.contains(s)) return true;
            }
        }
        return false;
    }

    /** 伤害修正入口: 返回实际应结算的伤害, 返回 <=0 表示本次攻击被完全免伤 */
    public static float gate(LivingEntity boss, DamageSource source, float amount) {
        if (amount <= 0F) return amount;

        // ⓪ 完全无视: armamentarium 的真实伤害 / 流血伤害(幽匿系列装备、亵渎战矛)
        if (isImmune(source)) {
            notifyImmune(boss, source);
            return 0F;
        }

        // ⓪.5 限伤总开关: 关闭时直接放行(不做单次上限 / 连击递减 / 反逃课距离)
        boolean impossible = boss instanceof net.farmingtales.ftboss.entity.EasterBossEntity manor && manor.isImpossibleChallenge();
        if (!LIMIT_ENABLED && !impossible) return amount;

        // ① 绕过无敌的伤害(/kill、虚空等)不受上限约束
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return amount;

        // ② 单次伤害上限
        float result = (float) Math.min(capOf(boss), (double) amount);

        // ③ 连击递减: 最近挨打窗口内, 伤害按时间因子百分比缩放(最低 50%)
        float mult = Mth.clamp(factorOf(boss) / 100F, COMBO_FLOOR, 1.0F);
        result *= mult;

        // ④ 反逃课距离: 拉太远打不掉血
        double limit = antiCheeseOf(boss);
        if (limit > 0.0) {
            Entity attacker = impossible ? net.farmingtales.ftboss.entity.EasterBossEntity.realAttacker(source)
                    : boss instanceof Mob mob ? mob.getTarget() : null;
            if (attacker == null) attacker = source.getEntity();
            if (attacker != null && attacker != boss && boss.distanceTo(attacker) > limit) {
                notifyAntiCheese(boss, source, limit);
                return 0F;
            }
        }
        return result;
    }

    /**
     * Legacy compatibility entry point for bosses compiled against the old
     * global 500-point nullifier. The rule was retired on 2026-09-29, so
     * this method deliberately never blocks damage.
     */
    @Deprecated
    public static boolean nullified(LivingEntity boss, DamageSource source, float amount) {
        return false;
    }

    /** 结算成功后调用: 推进连击窗口与伤害时间因子(LM: adaptTicks=30, factor-=10, hurtCount++) */
    public static void onHit(LivingEntity boss) {
        CompoundTag tag = boss.getPersistentData();
        tag.putInt(K_ADAPT, ADAPT_TICKS);
        tag.putInt(K_FACTOR, Math.max(FACTOR_MIN, factorOf(boss) - FACTOR_STEP));
        tag.putInt(K_CD, HURT_CD);
        int count = tag.getInt(K_COUNT) + 1;
        tag.putInt(K_COUNT, count >= 2 ? 0 : count);
    }

    /** 每 tick 调用(服务端): 衰减窗口/无敌帧, 窗口过期则复位伤害时间因子(对应 LM 的 tick 衰减) */
    public static void tick(LivingEntity boss) {
        if (boss.level().isClientSide()) return;
        CompoundTag tag = boss.getPersistentData();

        int cd = tag.getInt(K_CD);
        if (cd > 0) tag.putInt(K_CD, cd - 1);

        int adapt = tag.getInt(K_ADAPT);
        if (adapt > 0) {
            adapt--;
            tag.putInt(K_ADAPT, adapt);
            if (adapt == 0) tag.putInt(K_FACTOR, FACTOR_MAX);   // LM: damageAdaptationTicks==0 → setDamageTimeFactor(100)
        } else if (tag.getInt(K_FACTOR) != FACTOR_MAX && tag.contains(K_FACTOR)) {
            tag.putInt(K_FACTOR, FACTOR_MAX);
        }
    }

    /** 当前受击无敌帧剩余(供其它系统判断) */
    public static boolean isInvulnerableByCd(LivingEntity boss) {
        return boss.getPersistentData().getInt(K_CD) > 0;
    }

    /** 当前连击因子(100 = 无递减) */
    public static int timeFactor(LivingEntity boss) {
        return factorOf(boss);
    }

    /** 单次伤害上限 */
    public static double capOf(LivingEntity boss) {
        return DAMAGE_CAP.getOrDefault(entityId(boss), DEFAULT_CAP);
    }

    /** 反逃课距离 */
    public static double antiCheeseOf(LivingEntity boss) {
        return ANTI_CHEESE.getOrDefault(entityId(boss), DEFAULT_ANTI_CHEESE);
    }

    private static int factorOf(LivingEntity boss) {
        int f = boss.getPersistentData().getInt(K_FACTOR);
        return f <= 0 ? FACTOR_MAX : f;
    }

    private static String entityId(LivingEntity boss) {
        var key = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(boss.getType());
        return key == null ? "" : key.toString();
    }

    /** 无视真实/流血伤害时给攻击者一个限频提示(并在日志里留一行, 方便日后排查"打不掉血") */
    private static void notifyImmune(LivingEntity boss, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag tag = boss.getPersistentData();
        long now = boss.level().getGameTime();
        if (now - tag.getLong(K_IMMUNE_NOTIFY) < 40L) return;
        tag.putLong(K_IMMUNE_NOTIFY, now);
        player.displayClientMessage(Component.translatable("msg.ftboss.immune", boss.getDisplayName()), true);
        net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][免疫] {} 的 {} 伤害被完全免疫(伤害类型 {})",
                boss.getDisplayName().getString(), player.getName().getString(), source.getMsgId());
    }

    private static void notifyAntiCheese(LivingEntity boss, DamageSource source, double limit) {        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag tag = boss.getPersistentData();
        long now = boss.level().getGameTime();
        if (now - tag.getLong(K_NOTIFY) < 40L) return;   // 2 秒最多提示一次
        tag.putLong(K_NOTIFY, now);
        player.displayClientMessage(Component.translatable("msg.ftboss.anticheese",
                boss.getDisplayName(), String.format("%.0f", limit)), true);
    }


}
