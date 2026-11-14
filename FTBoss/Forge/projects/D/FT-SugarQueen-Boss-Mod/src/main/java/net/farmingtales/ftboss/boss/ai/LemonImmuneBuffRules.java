package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 【柠檬猫免疫"打不死"】—— 2026-09-19 v171 按用户要求新增：
 * 「给柠檬加一个 buff 检测防止无法杀死，比如抗性五、止心术、饱腹代偿等 buff」。
 *
 * <p>用户同时澄清：「柠檬不会获得 boss 的 buff，但是会得到 boss 的技能」。
 * 所以本类只管**增益**，**不碰技能继承**（{@code LemonSacrifice.spells()} 那条线一行没动）。
 *
 * <p><b>为什么要专门做这件事</b>：柠檬猫身上原本有三条互不相干的增益来源，
 * 叠起来能把玩家卡死：
 * <ol>
 *   <li>{@code EasterBossEntity.foodBuffPool()} 的「尝了口菜」随机食物增益 ——
 *       <b>它自己就把「饱腹代偿 / 滋养 / 饱腹」这三种列在池子里</b>，
 *       是柠檬猫"打不死"的来源之一；</li>
 *   <li>{@code lemonSacrifice} 的献祭继承：会把被献祭召唤物身上的**所有有益效果**
 *       按 amplified 等级复制过来；</li>
 *   <li>{@code CHAIN_BUFF_POOL} 的连锁增益与助战强化。</li>
 * </ol>
 * 三条路各给一点抗性/吸收，柠檬猫就能稳定吃到抗性 IV~V 级别的减伤。
 *
 * <p><b>两道防线</b>：
 * <ul>
 *   <li><b>发之前不选</b>：{@link #isForbidden} 把危险效果挡在增益池外（见 EasterBossEntity 的池子构造）；</li>
 *   <li><b>已经拿到就清</b>：{@link #purge(LivingEntity)} 定期扫描并移除，兜住"别的来源塞进来"的情况。</li>
 * </ul>
 *
 * <p>判定按**注册名**比对（没装对应 mod 时自然不命中），并自带一条 registry 名兜底扫描。
 */
public final class LemonImmuneBuffRules {

    /**
     * 抗性等级上限（含）：amplifier ≥ 这个值就视为"配合其它增益会打不死"，直接清掉。
     *
     * <p>amplifier 是**从 0 开始**的：抗性 I = 0、抗性 V = 4。
     * 取 3 表示**抗性 IV 及以上**不允许（抗性 IV = 减伤 80%，再叠吸收就已经很难打死）。
     */
    public static final int MAX_RESISTANCE_AMPLIFIER = 3;

    /**
     * 一律不许有的增益（按完整注册名）。
     *
     * <ul>
     *   <li>{@code irons_spellbooks:heartstop} 止心术（用户点名）—— 心跳停止期间免疫致命伤；</li>
     *   <li>创造模式飞行/无敌类的增益；</li>
     *   <li>{@code apotheosis} 系"免疫伤害/无敌"类。</li>
     * </ul>
     */
    public static final List<String> FORBIDDEN_BUFFS = List.of(
            "kaleidoscope_cookery:satiated_shield",     // 饱腹代偿（用户点名）
            "farmersdelight:nourishment",               // 滋养
            "farm_and_charm:satiation",                 // 饱腹
            "irons_spellbooks:heartstop",               // 止心术（用户点名）
            "irons_spellbooks:oakskin",
            "irons_spellbooks:black_hole",              // 黑洞（同属庄园禁术清单）
            "apotheosis:invulnerability",               // 无敌
            "apotheosis:damage_immunity"                // 免疫伤害
    );

    /** 注册名里带这些片段的效果一律清掉（覆盖整合包里没逐个列举的同义效果） */
    public static final List<String> FORBIDDEN_NAME_FRAGMENTS = List.of(
            "invulnerab", "invincib", "damage_immunity", "heartstop"
    );

    private LemonImmuneBuffRules() {}

    /** 这个增益是不是"会让柠檬猫打不死"的（按注册名 + 名字片段判定）。 */
    public static boolean isForbidden(MobEffect effect) {
        if (effect == null) return false;
        ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (key == null) return false;
        String id = key.toString();
        if (FORBIDDEN_BUFFS.contains(id)) return true;
        String path = key.getPath();
        for (String fragment : FORBIDDEN_NAME_FRAGMENTS) {
            if (path.contains(fragment)) return true;
        }
        return false;
    }

    /** 抗性等级是否已经高到该清（amplifier 从 0 起算）。 */
    public static boolean tooMuchResistance(MobEffectInstance instance) {
        return instance != null && instance.getEffect() == MobEffects.DAMAGE_RESISTANCE
                && instance.getAmplifier() >= MAX_RESISTANCE_AMPLIFIER;
    }

    /** 这一条效果该不该从柠檬猫身上清掉。 */
    public static boolean shouldPurge(MobEffectInstance instance) {
        if (instance == null) return false;
        MobEffect effect = instance.getEffect();
        /* 只清**有益**的：减益留着（玩家还想让它中毒/减速呢）。 */
        if (!effect.isBeneficial()) return false;
        return isForbidden(effect) || tooMuchResistance(instance);
    }

    /**
     * 扫描并移除柠檬猫身上的"打不死"增益。
     *
     * @return 清掉了几条
     */
    public static int purge(LivingEntity boss) {
        if (boss == null || boss.level().isClientSide || boss.getActiveEffects().isEmpty()) return 0;
        List<MobEffectInstance> doomed = new ArrayList<>();
        for (MobEffectInstance instance : boss.getActiveEffects()) {
            if (shouldPurge(instance)) doomed.add(instance);
        }
        if (doomed.isEmpty()) return 0;
        StringBuilder names = new StringBuilder();
        for (MobEffectInstance instance : doomed) {
            if (names.length() > 0) names.append('/');
            names.append(instance.getEffect().getDisplayName().getString());
            if (instance.getAmplifier() > 0) names.append(' ').append(roman(instance.getAmplifier() + 1));
            boss.removeEffect(instance.getEffect());
        }
        FTBossMod.LOGGER.info("[FTBoss][Lemon] buff 检测: 清掉 {} 条会让柠檬猫打不死的增益 [{}]",
                doomed.size(), names);
        return doomed.size();
    }

    private static String roman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII";
            default -> String.valueOf(level);
        };
    }
}
