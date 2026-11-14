package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ManorDifficulty;

import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.farmingtales.ftboss.util.QingyiCombatRules;
import net.farmingtales.ftboss.skill.QingyiConfluenceSkill;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.util.ManorCombatRules;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.GongdiSkills;
import net.farmingtales.ftboss.boss.ai.ManorFiveSkills;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.boss.ai.ManorBuffCopy;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.farmingtales.ftboss.util.HiddenBossCastRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorSkillDamageRules;
import net.farmingtales.ftboss.util.ManorTeleportRules;
import net.farmingtales.ftboss.util.ZicaiDebuffRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
/* v115: 补上 ServerPlayer 的 import —— 本文件以前到处写全限定名 net.minecraft.server.level.ServerPlayer,
   而新加的几处(暗影虚空 applyShadowVoid 等)用了短名, 缺这个 import 会直接编译不过。 */
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;
import net.farmingtales.ftboss.util.DoraCombatTempo;
import net.farmingtales.ftboss.util.DoraSniperCycle;
import net.farmingtales.ftboss.util.FlywheelCombatRules;

/**
 * 【甜苹果庄园·彩蛋 Boss】(2026-09-12) —— 9 位人形 Boss 共用这一个类。
 *
 * <p>渲染走**原版玩家模型**({@code EasterBossModel extends PlayerModel}), 所以天然兼容玩家动作;
 * 每位只靠 {@link #DATA_VARIANT} 区分: 名字 / 皮肤 / 武器 / 附加骨骼 / 招式风格。
 *
 * <p>武器表(2026-09-12 定案):
 * <pre>
 *  0 柚子   手持缩小版自己(石像管线)     附加骨骼: 呆毛+长发
 *  1 蛋蛋   末地烛 minecraft:end_rod
 *  2 朵拉   枪械  bren:machine_gun
 *  3 功底   黑色切割者(自制 3D 模型)      附加骨骼: 披风
 *  4 柠檬猫 煎锅+锅铲 farmersdelight:skillet / kaleidoscope_cookery:kitchen_shovel
 *  5 青衣   四件 TnS 独特武器轮换
 *  6 文玄蓝 飞轮近战 + 阀门远程
 *  7 折耳根 铁魔法(施法 Boss, 用施法姿势)
 *  8 紫菜   待定                           附加骨骼: 光环
 * </pre>
 */
public class EasterBossEntity extends Monster implements ManorBalanceAccess {

    /* ---------------- 变体表 ---------------- */
    /* 2026-09-12 v48 修「麻雀进场却变成紫菜」:
       COUNT 原来还是 9, 而 setVariant() 会用 Math.min(COUNT-1, v) 夹位 —— 于是 setVariant(9)
       被夹成 8(紫菜), 麻雀皮肤/名字全丢, 入场演出也认不出"这是麻雀"(日志: 隐藏 Boss 入场: 紫菜 variant=9)。
       2026-09-13 v76 加入**雪黎**(variant 10) —— 现在 11 个变体: 0~8 庄园九人 + 9 麻雀(支援) + 10 雪黎。 */
    public static final int COUNT = 12;
    public static final int QIZAI_VARIANT = 11;
    private final net.farmingtales.ftboss.boss.ai.QizaiMagic qizaiMagic = new net.farmingtales.ftboss.boss.ai.QizaiMagic(this);
    private final net.farmingtales.ftboss.skill.QizaiArsenal qizaiArsenal = new net.farmingtales.ftboss.skill.QizaiArsenal(this);
    /**
     * 庄园轮换名单。**2026-09-13 v76 起不再是连续区间**:
     * {@code 0~8} = 甜苹果庄园九人, {@code 10} = 雪黎(新加入, 用户要求"加进庄园轮换");
     * {@code 9} = 麻雀是随机来帮忙的**支援位**, 不进轮换。
     */
    public static final int[] MANOR_VARIANTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, QIZAI_VARIANT};
    /** 庄园轮换人数(= {@link #MANOR_VARIANTS} 长度, 现在是 10) */
    public static final int MANOR_COUNT = MANOR_VARIANTS.length;
    /** 演出结束后庄园首发是否随机(否则固定第 0 位) */
    public static final boolean MANOR_FIRST_VARIANT_ALLOWED = true;
    /** 麻雀的变体下标 */
    public static final int SPARROW_VARIANT = 9;
    /** 雪黎的变体下标(2026-09-13 v76 新 Boss) */
    public static final int XUELI_VARIANT = 10;
    /** 蛋蛋的变体下标(2026-09-17: 由 private 改 public —— 末影封锁 {@code DandanPearlBan} 要按它判定) */
    public static final int DANDAN_VARIANT = 1;
    /** 紫菜的变体下标（2026-09-24：补上具名常量 —— 她的攻速与减益规则要按它判定） */
    public static final int ZICAI_VARIANT = 8;
    /** 折耳根的变体下标（2026-09-24：补上具名常量 —— 他的技能冷却与传送目标规则要按它判定） */
    public static final int ZHEERGEN_VARIANT = 7;
    private static final int DANDAN_WEAPON_MODEL_DATA = 91601;
    public static final int LEMONCAT_VARIANT = 4;
    /** Lemon Cat keeps two thirds of every incoming heal (all heal sources pass through heal()). */
    public static final float LEMON_HEAL_MULTIPLIER = 2.0F / 3.0F;
    /** 青衣的变体下标(2026-09-21: 补上具名常量 —— 她的出伤减半钩子 {@code QingyiDamageHalved} 要按它判定,
        以前满文件都是裸字面量 {@code getVariant() == 5}) */
    public static final int QINGYI_VARIANT = 5;
    public static final int LEMONCAT_WEAPON_MODEL_DATA = 160416;
    private static final String LEMONCAT_OFFHAND_ID = "kaleidoscope_cookery:kitchen_shovel";

    /* 固定技能基础值已经包含历史倍率；以下公开常量保留给旧接口兼容。
       新的固定伤害直接填写最终基础值，不再经由这些常量连乘。
       随距离变化的分身自爆仍保留原有浮点运算顺序，避免重排计算改变结果。 */
    public static final double WEAPON_DAMAGE_MULT = 5.0;
    public static final double SKILL_MULT = 3.0;
    public static final double SKILL_RADIUS_MULT = 1.5;
    /** v176: 由 4/3 减半为 2/3(= 用户「庄园 boss 伤害减半」) */
    public static final double BOSS_DAMAGE_MULT = 0.6666666666666666;

    /* ==================== v29: 拿到增益就"连锁" + 甩玩家一个随机减益 ====================
       任何一位彩蛋 Boss **获得正面效果**时(含常驻增益刷新) ->
       额外给自己再上 **2 个不同的随机增益** + 给 32 格内最近玩家挂 **1 个随机减益**。
       有 2 秒冷却, 免得常驻增益一次刷 6 个就刷屏。 */
    public static final int CHAIN_BUFF_CD = 40;
    private static final net.minecraft.world.effect.MobEffect[] CHAIN_BUFF_POOL = {
            MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE,
            MobEffects.REGENERATION, MobEffects.DIG_SPEED, MobEffects.JUMP, MobEffects.ABSORPTION,
            MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING, MobEffects.SATURATION,
            MobEffects.HEALTH_BOOST, MobEffects.LUCK, MobEffects.NIGHT_VISION,
            MobEffects.CONDUIT_POWER, MobEffects.DOLPHINS_GRACE, MobEffects.SLOW_FALLING,
    };
    /* 2026-09-12 v37 按用户要求: **对玩家的减益里不允许 失明 / 黑暗 / 漂浮**, 已从这里移除 */
    private static final net.minecraft.world.effect.MobEffect[] RANDOM_DEBUFF_POOL = {
            MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN,
            MobEffects.POISON, MobEffects.WITHER, MobEffects.HUNGER, MobEffects.UNLUCK, MobEffects.GLOWING,
    };

    /* ==================== 全员自带增益 + "想逃?"抓人(2026-09-12 v23 用户定案) ====================
       每位彩蛋 Boss 常驻 **移速提高 + 跳跃提升**(每 5 秒补一次);
       连续 {@value #NO_TARGET_GRAB_TICKS} tick 没锁定到**玩家** -> 把最近的玩家**传送到自己身边**,
       并说 "哼，想逃？"。
       2026-09-13 v94 按用户要求「boss 未索敌到玩家 10 秒则…」: 15 秒 -> **10 秒**, 而且要求锁定的
       必须是**玩家**(以前打小怪也算"有目标", 于是 Boss 追着铁傀儡跑、玩家在边上站着都不抓)。
       **庄园里这条不再生效** —— 那里由 {@code ManorFight.recallStrayPlayers} 统一处理(它能按
       "谁离场地更近"决定挪玩家还是挪 Boss, 比这里只会挪玩家更合理)。 */
    public static final int INNATE_BUFF_TICKS = 100;         // 每 5 秒补一次
    public static final int INNATE_BUFF_DURATION = 200;      // 10 秒(够覆盖到下一次)
    /** 2026-09-12 v27 用户定案: 全员常驻的六个 II 级增益 */
    public static final net.minecraft.world.effect.MobEffect[] INNATE_EFFECTS = {
            MobEffects.MOVEMENT_SPEED,      // 速度 II
            MobEffects.DAMAGE_RESISTANCE,   // 抗性提升 II
            MobEffects.REGENERATION,        // 生命恢复 II
            MobEffects.DIG_SPEED,           // 急迫 II
            MobEffects.JUMP,                // 跳跃提升 II
            MobEffects.DAMAGE_BOOST,        // 力量 II
    };
    public static final int NO_TARGET_GRAB_TICKS = 10 * 20;  // v94: 15 秒 -> 10 秒没锁定到玩家就抓人
    public static final double NO_TARGET_GRAB_RANGE = 256.0;
    public static final String NO_TARGET_TEXT = "哼，想逃？";

    /* ==================== v26 用户定案的一批机制 ====================
       · **文玄蓝飞轮攻击距离 16 格**、**朵拉机枪射程 12 格**;
       · **近战 Boss**(非远程)5 秒内没进到玩家 3 格内 -> **投末影珍珠瞬移到玩家旁边**;
       · **柠檬猫**: 每 5 秒给自己上 2 个随机"食物系"增益;
       · **青衣**: 投三叉戟 + 激流冲刺 + 扔"随机 6 种减益"的溅射药水;
       · **全员**: 自己中了减益 -> **2 秒后清除自身所有效果**。 */
    public static final double FLYWHEEL_ATTACK_RANGE = 24.0;   // 2026-09-12 v41: 16 -> 24
    public static final double BURST_RANGE = 18.0;             // 2026-09-12 v41: 朵拉机枪 12 -> 18
    public static final double PEARL_NEAR = 4.0;
    /**
     * v26 的旧口径：离目标超过 {@value #PEARL_NEAR} 格累计这么多 tick 就投珍珠。
     *
     * <p>★ 2026-09-24 按用户要求「boss 的珍珠追击没触发，减少触发」重做后，
     * <b>这个常数只作为"非庄园场合的兜底节奏"保留</b>（见 {@code pearlTick}）：
     * 庄园战里改用 {@link net.farmingtales.ftboss.util.ManorChaseRules#PEARL_IDLE_TICKS}
     * 那套「连续 15 秒没打到玩家才追」的判据。
     */
    public static final int PEARL_TRIGGER_TICKS = 20;
    /** v118 按用户要求「玩家的净化效果改成 1min 一次」: 中减益后 **60 秒**才清一次
        (原来只有 2 秒 —— 等于玩家/其它 Boss 给它挂的任何减益都留不住) */
    public static final int PURGE_DELAY_TICKS = 60 * 20;
    /** v106: 追击时每隔多少 tick 啃一次地(3 秒) —— 见 {@code digTick} */
    public static final int BREAK_INTERVAL = 3 * 20;
    public static final int FOOD_BUFF_INTERVAL = 200;
    public static final int QINGYI_POTION_DEBUFFS = 6;
    /** 谁是"远程 Boss"(不吃末影珍珠那一套): 2 朵拉(机枪) / 6 文玄蓝(飞轮+阀门) */
    public static final boolean[] RANGED = {
            false, false, true, false, false, false, true, false, false, false, false, true
    };
    public static final double RANGED_MOVEMENT_SPEED = 0.165;

    /* ==================== 功底「破甲 + 血怒」(2026-09-12 v24 用户定案) ====================
       黑色切割者命中玩家时:
         · 给**玩家**叠「护甲侵蚀」—— 每层 -5 护甲, 最多 5 层(-25), 每层 10 秒(见 {@code ArmorShred});
           破甲叠满 5 层 -> 功底额外获得 **速度 II 8 秒**;
         · 给**自己**叠「血怒」—— 每层 +25% 攻击力, 最多 5 层(+125%), 永久叠到死。 */
    public static final int BLOOD_RAGE_MAX = 5;
    /** 2026-09-12 v45 按用户要求: 血怒攻击力提高到 **225%** —— 5 层满 => 每层 45%
        2026-09-13 v72 按用户要求「功底每层血怒提高攻击力 150%」: 每层 45% -> **150%** (5 层满 = +750%)
        2026-09-13 v75 按用户要求「血怒回调到满层 250% 增幅」: 每层 **50%** (5 层满 = **+250%**) */
    public static final double BLOOD_RAGE_PER_STACK = 0.50;
    public static final int BLOOD_RAGE_TICKS = 8 * 20;
    /* 2026-09-13 v71 按用户要求: 「功底血怒后获得 25% 的吸血 + 额外 2% 的真实伤害」
       2026-09-13 v75 按用户要求「吸血回调到 15%」: 25% -> **15%** */
    /** 血怒期间: 造成伤害的 15% 回血给自己 */
    public static final double BLOOD_RAGE_LIFESTEAL = 0.15;
    /** 血怒期间: 每次命中额外附加"造成伤害的 2%"作为**真实伤害**(无视护甲/抗性) */
    public static final double BLOOD_RAGE_TRUE_DAMAGE = 0.10;
    private static final java.util.UUID BLOOD_RAGE_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000b2");
    /* 2026-09-13 v88 按用户要求「功底血怒满层后移速上升」:
       血怒叠到 {@value #BLOOD_RAGE_MAX} 层(满层)时, 额外给自己挂 **+40% 移动速度**;
       重上场/层数被重置时一并摘掉(见 {@code resetForSpawn})。 */
    public static final double BLOOD_RAGE_MAX_SPEED_BONUS = 0.40;
    private static final java.util.UUID BLOOD_RAGE_SPEED_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000b3");
    /** 名字(与皮肤文件名一致) */
    public static final String[] NAMES = {
            "柚子", "蛋蛋", "朵拉", "功底", "柠檬猫", "青衣", "文玄蓝", "折耳根", "紫菜", "麻雀", "雪黎", "祁崽"
    };
    /** 皮肤文件(不含扩展名; 位于 textures/entity/easter/) */
    public static final String[] SKINS = {
            "youzi", "dandan", "duola", "gongdi", "ningmengmao", "qingyi", "wenxuanlan", "zheergeng", "zicai", "maque",
            "xueli", "qizai"
    };
    /** 注册 id */
    public static final String[] IDS = {
            "youzi", "dandan", "duola", "gongdi", "ningmengmao", "qingyi", "wenxuanlan", "zheergeng", "zicai", "maque",
            "xueli", "qizai"
    };
    /** 主手武器(注册 id, 空串=无; 有些是别的模组的物品, 只用于渲染/掉落, 取不到就留空) */
    public static final String[] WEAPONS = {
            "ftboss:statue_youzi",          // 0 柚子: 缩小版自己(石像管线, 待生成)
            "minecraft:end_rod",            // 1 蛋蛋
            "bren:machine_gun",             // 2 朵拉
            "ftboss:black_cleaver",         // 3 功底
            "farmersdelight:skillet",       // 4 柠檬猫
            "ftboss:qingyi_confluence_trident",
            "create:flywheel",              // 6 文玄蓝
            "ftboss:shadow_sword",          // 7 折耳根: 暗影剑(饥荒) + 铁魔法
            "ftboss:puffer_bat",            // 8 紫菜: 河豚棒(魔法攻击)
            "ftboss:maque_blessing_feather",
            "farmersdelight:skillet",
            "ftboss:qizai_prayer_staff"
    };
    /**
     * 附加骨骼开关: [呆毛, 长发, 披风, 光环]
     *
     * <p><b>2026-09-12 按用户反馈全部关闭</b>: 这 4 根骨骼采样的是玩家皮肤的**帽子覆盖层**
     * (纹理 32,0~64,16), 而这 9 张皮肤那块是空的 → 部件会露出"错乱的贴图块", 看起来像头部贴图坏了。
     * 想启用某位的附加部件时, 把对应位改 true, 并给它的皮肤在帽子层画上像素(或我生成一张覆盖层贴图)。
     */
    public static final boolean[][] EXTRA = {
            {false, false, false, false}, // 柚子
            {false, false, false, false}, // 蛋蛋
            {false, false, false, false}, // 朵拉
            {false, false, false, false}, // 功底
            {false, false, false, false}, // 柠檬猫
            {false, false, false, false}, // 青衣
            {false, false, false, false}, // 文玄蓝
            {false, false, false, false}, // 折耳根
            {false, false, false, false}, // 紫菜
            {false, false, false, false}, // 麻雀
            {false, false, false, false}, // 雪黎
            {false, false, false, false},
    };
    /** 招式风格: 0=劈砍 1=突刺 2=横扫 */
    public static final int[] STYLES = {
            1, 0, 1, 0, 2, 2, 2, 2, 1, 2, 2, 1
    };
    /** 是否施法型(折耳根) */
    public static final boolean[] CASTER = {
            false, false, false, false, false, false, false, true, false, false, false, true
    };

    /* ==================== 柚子 招牌机制「自我叠影」(2026-09-12 v15) ====================
       武器 = **缩小版它自己**(ftboss:statue_youzi):
         · 近战命中给目标挂 **缓慢 II + 虚弱 II 共 10 秒**(高难档);
         · **每命中 3 次**召出 2 个 **0.55 倍大的迷你柚子分身**(各 150 血 / 15 秒后消散);
         · 场上同款分身已达 4 个时不再召, 改为给自己上 **速度 II 10 秒**(防越打越多)。 */
    public static final int MINI_LIFE_TICKS = 15 * 20;
    public static final int MINI_CAP = 6;    // v124 按用户要求「柚子的最高在场召唤数量提高到 6」(原 4)
    public static final int YOUZI_HITS_PER_SUMMON = 3;
    public static final double MINI_HEALTH = 150.0;
    /** v117 按用户要求「柚子的召唤物会分摊她 10% 的受伤」: 分身替她吃掉的伤害比例 */
    public static final float YOUZI_MINION_SHARE = 0.05f;
    public static final float MINI_SCALE = 0.55f;
    /** 两次分裂之间的最小间隔(2 秒) —— 挨打也计数, 没这个会瞬间铺满 */
    public static final int MINI_SUMMON_CD = 40;

    /* ==================== 朵拉 招牌机制「压制射击」(2026-09-12 v17) ====================
       武器 = bren:machine_gun。技能: 朝目标**连射 8 发**(每 3 tick 一发, 共约 1.2 秒),
       每发 3.5 点弹道伤害(被墙挡住就打墙, 用纯网格枪线 + 冲击刻痕表现);
       命中 >= 5 发 -> 追加 **缓慢 III 4 秒**(压制住动不了)。 */
    /* 2026-09-12 v27/v28 按用户要求: 枪(朵拉的 Bren 机枪)**弹药翻倍 + 射速翻倍**:
       8 发 -> 16 发 -> **32 发**(v28 再翻倍); 间隔 3 tick -> 实际 **1.5 tick**;
       每发子弹 **16 点"物理 + 魔法"混合伤害**(一半吃护甲, 一半直接穿甲) */
    public static final int BURST_SHOTS = 32;
    public static final int BURST_INTERVAL = 3;              // 基准间隔
    public static final double BURST_RATE_MULT = 2.0;        // 射速倍率
    /** 每发 16 点 = 8 物理(吃护甲) + 8 魔法(穿甲); v29 全部伤害再 x2 -> 每半 16, 合计 32 */
    public static final float BURST_DAMAGE_PHYS = 6.6666665F;
    public static final float BURST_DAMAGE_MAGIC = 6.6666665F;
    public static final float BURST_DAMAGE = 13.333333F;
    /* 2026-09-13 v88 按用户要求「朵拉的射击准度低一点」。
       以前: 每发都是"枪口 -> 目标身体中心"的直线, 只要中间没墙就 **100% 命中**
       (日志里永远是 `朵拉「压制射击」结束: 命中 32/32`, 一轮固定 682 点, 太死板)。
       现在: 每发在**水平方向**加一个随机偏差(垂直不偏 —— 目标是 1.8 格高、瞄的是身体中心,
       偏一点也不脱靶), 偏差半径随距离线性放大(近处稳、贴射程边缘飘),
       并且要以"这一发到底有没有真的打中碰撞箱"为准才算命中(AABB.clip)。
       命中的弹丸仍然飞过目标(能看到擦身而过), 只是不结算伤害。

       命中率 ≈ min(1, 0.3 / 偏差半径) —— 玩家碰撞箱半宽 0.3 格:
         · 4 格:  ±0.42 -> 约 71%
         · 10 格: ±0.60 -> 约 50%
         · 18 格: ±0.78 -> 约 38% */
    public static final double BURST_SPREAD_NEAR = 0.42;     // 近距离(<=4 格)的水平偏差半径(格)
    public static final double BURST_SPREAD_FAR = 0.78;      // 贴射程上限({@link #BURST_RANGE} 格)时的偏差半径

    /* ==================== 功底 招牌机制「断罪斩」(2026-09-12 v18) ====================
       武器 = 黑色切割者。技能分两段: **蓄力 1.5 秒**(举刀 + 凋零音) -> 半径 5 格横扫,
       22 点伤害 + 击飞 0.9 + **凋零 III 6 秒**(流血) + 缓慢 II 5 秒;
       一刀砍中 >= 2 人时自己拿 **力量 II 8 秒**。 */
    public static final int CLEAVE_WINDUP = HiddenBossCastRules.quicker(30);
    public static final double CLEAVE_RADIUS = 16.875;

    /* ==================== 柠檬猫 招牌机制「猛火翻锅」(2026-09-12 v19) ====================
       武器 = 煎锅 + 锅铲。技能: 先起油花(蓄力 0.75 秒) -> 半径 4 格**翻锅**:
       18 点伤害 + 把玩家**抛上天**(0.1.4 上推) + 点燃 6 秒 + 饥饿 II 8 秒(被煎熟)。 */
    public static final int PAN_WINDUP = HiddenBossCastRules.quicker(15);
    public static final double PAN_RADIUS =6;
    public static final float PAN_DAMAGE = 100.8f;

    /* ==================== 柠檬猫 招牌机制「喊帮手」(2026-09-12 v33 用户定案) ====================
       随机召唤 **2 位"食物系"Boss** 助战(本模组现成的那几位: 糖霜女皇/屠夫/茶树/酒桶王/鼠群),
       持续 90 秒, 柠檬猫退场/死亡时一起收回。
       2026-09-13 v75 按用户要求: **不允许召唤苹果爵士** —— 玩家同时持有「血腥契约 + 甜苹果庄园头衔」时,
       被召唤出来的苹果爵士会立刻触发"头衔共鸣 → 蜕变", 把庄园**重新激活刷新**一位 Boss
       (日志证据: 柠檬猫召唤: 苹果爵士·蜜脆 -> [Apple] 触发蜕变: 头衔+血腥契约 均满足)。 */
    public static final int SUMMON_LIFE_TICKS = 90 * 20;
    /* 2026-09-13 v88 按用户要求「柠檬召唤的 boss 会被加强: 增加随机 5 个 buff, 血量加 100」。
       强化只作用于**召唤出来的这几位助战**(招出来时一次性给), 不影响玩家自己召唤的那只。 */
    /** 助战额外血量(直接加在 MAX_HEALTH 基础值上, 之后仍会吃 {@code BossScaling} 的人数倍率) */
    public static final double SUMMON_EXTRA_HEALTH = 100.0;
    /** 助战额外随机增益个数(从 {@link #CHAIN_BUFF_POOL} 里抽, 尽量不重复) */
    public static final int SUMMON_EXTRA_BUFFS = 5;
    public static final String[] FOOD_BOSS_IDS = {
            "ftboss:sugar_queen", "ftboss:meat_mountain", "ftboss:tea_tree",
            "ftboss:wine_barrel", "ftboss:rat_boss",
    };

    /* ==================== 雪黎 招牌机制(2026-09-13 v76 用户定案, 庄园第 10 位) ====================
       外观: 皮肤 {@code textures/entity/easter/xueli.png}(用户提供)。
       装备: **平底锅**(主手 farmersdelight:skillet) + **擀面杖**(副手 bakery:rolling_pin)。
       远程: 把整个**汤锅**(kaleidoscope_cookery:stockpot)扔出去砸人。
       近战: 擀面杖命中后, 给玩家挂上"**机械动力粉碎轮**"式的持续伤害 ——
             每秒 {@value #XUELI_CRUSH_DAMAGE} 点, 共 {@value #XUELI_CRUSH_TICKS} tick(合计 15),
             使用原版虚空伤害，不依赖机械动力。 */
    public static final int XUELI_CRUSH_TICKS = 60;
    /** 粉碎轮每秒结算一次固定虚空伤害，不受雪黎通用出伤倍率放大。 */
    public static final float XUELI_CRUSH_DAMAGE = 5.0f;
    /** 汤锅砸中的伤害(一半物一半魔法, 见 StockpotEntity) */
    public static final float XUELI_POT_DAMAGE = (float)40;
    /** 擀面杖(副手) */
    public static final String XUELI_MELEE_ID = "bakery:rolling_pin";
    public static final float XUELI_ROLLING_PIN_QUOTE_CHANCE = 0.20f;
    public static final int XUELI_ROLLING_PIN_QUOTE_COOLDOWN = 5 * 20;
    private long xueliRollingPinQuoteReadyAt = 0L;
    private final net.farmingtales.ftboss.util.XueliDialogueState xueliDialogue =
            new net.farmingtales.ftboss.util.XueliDialogueState();
    /** 汤锅(远程投掷物) */
    public static final String XUELI_POT_ID = "kaleidoscope_cookery:stockpot";
    /** 玩家 UUID -> 还剩几 tick 的"粉碎轮"持续伤害 */
    private final java.util.Map<java.util.UUID, Integer> crushTicks = new java.util.HashMap<>();
    /* 2026-09-13 v78 按用户要求"给雪黎更多技能": 三招轮换(汤锅投掷 / 平底锅横扫 / 擀面杖压路冲锋) */
    /** 下一招用哪一招(0=汤锅 1=平底锅 2=擀面杖 3=禁疗飞刀 4=女仆护法 5=自爆分身, 轮换)
        ★ 2026-09-14 v106: 按用户反馈「雪黎的禁疗未生效」—— 以前从 0 起轮, 而庄园里雪黎往往
        两三个技能就被打死了, 轮不到第 4 个技能(日志里汤锅/擀面杖/平底锅都出现过, 飞刀一次都没有)。
        现在从 **3** 起轮: 每次上场第一招就是禁疗飞刀, 前 6 招里飞刀出现两次。 */
    private int xueliNext = 3;
    /** v103 雪黎自爆分身: 引信剩余 tick(0 = 不是炸弹) / 是否已经炸过 */
    private int bombFuse = 0;
    private boolean bombBlown = false;
    /** v104 雪黎「女仆护法」(照沙耶的茶芽庇佑做的免伤护盾); 见 {@link net.farmingtales.ftboss.boss.ai.XueliMaids} */
    private final net.farmingtales.ftboss.boss.ai.XueliMaids xueliMaids =
            new net.farmingtales.ftboss.boss.ai.XueliMaids(this);
    /** 平底锅横扫: 半径 4.5 格、30 伤害(数字已含全局修正) + 点燃 4 秒 */
    public static final double XUELI_PAN_RADIUS = 4.5;
    public static final float XUELI_PAN_DAMAGE = 30.0f;
    /** 擀面杖压路冲锋: 持续 tick / 撞到的伤害(数字已含全局修正) / 命中间隔 */
    public static final int XUELI_ROLL_TICKS = 30;
    public static final float XUELI_ROLL_DAMAGE = 12.0f;
    public static final int XUELI_ROLL_HIT_CD = 6;
    private int xueliRollTicks = 0;
    private int xueliRollHitCd = 0;

    /* ==================== 雪黎 ⑦「天降瞬砸」(2026-09-13 v111 新增; 2026-09-14 v125 改五连砸) ====================
       用户原话:「雪黎瞬移到玩家上空, 3 秒后下砸造成眩晕和 40 点伤害(粒子特效提醒)」。
       流程: 选一名玩家 -> **落点定死在她当时站的位置**(要躲就得在预警时间内跑出雪圈) ->
             瞬移到落点正上方 {@value #XUELI_SLAM_HEIGHT} 格, NoGravity 悬停
             {@value #XUELI_SLAM_WARN_TICKS} tick: 每 tick 在落点画**雪花警戒环** + 她身上寒气/云柱,
             音调递增的提示音 + action bar 播报 -> 到点**砸落**: 半径 {@value #XUELI_SLAM_RADIUS} 格内
             玩家吃 {@value #XUELI_SLAM_DAMAGE} 伤害 + **眩晕** {@value #XUELI_SLAM_STUN_TICKS} tick + 细雪(v105 那套)。

       ★ v125 按用户要求「允许雪黎进行连续 5 次快速下砸, 每次下砸时间变成 1s, 间隔 1s」:
         - 一次施放 = **{@value #XUELI_SLAM_COMBO} 连砸**; 每下的蓄势(预警)缩到 {@value #XUELI_SLAM_WARN_TICKS} tick,
           两下之间在地面停 {@value #XUELI_SLAM_GAP_TICKS} tick(间隔) 再窜上去砸下一发。
         - **眩晕同步压到 {@value #XUELI_SLAM_STUN_TICKS} tick(= 间隔时长)**: 否则 3 秒定身会把玩家锁到
           第 3 下之后, "间隔 1 秒"形同虚设、5 下必吃(用户 2026-09-14 明确选了这一方案)。
         - 半径仍按上一版 {@value #XUELI_SLAM_RADIUS} 格不变(用户明确要求保留)。 */
    public static final double XUELI_SLAM_HEIGHT = 12.0;
    /** 每一发的蓄势(悬停预警)时长 —— v125: 3 秒 → **1 秒** */
    public static final int XUELI_SLAM_WARN_TICKS = HiddenBossCastRules.quicker(20);
    public static final double XUELI_SLAM_RADIUS = 8.0;   // v124 按用户要求「下砸范围提高到半径 8 格」(原 4.5); v125 用户要求不改
    public static final float XUELI_SLAM_DAMAGE = 40.0f;
    /** v125: 眩晕 3 秒 → **1 秒**(= 两下之间的间隔, 中了还能在间隔里跑出下一发的雪圈) */
    public static final int XUELI_SLAM_STUN_TICKS = 20;
    /** v125: 一次施放连砸几下 */
    public static final int XUELI_SLAM_COMBO = 5;
    /** v125: 两下之间的间隔(落地后在地面停这么久, 再窜上去砸下一发) */
    public static final int XUELI_SLAM_GAP_TICKS = 20;
    /** 悬停/蓄势剩余 tick(>0 = 正在放 ⑦, 期间不接别的招、被钉在半空) */
    private int xueliSlamWarn = 0;
    /** v125: 这一轮连砸还剩几发(>0 = 连砸进行中, 间隔期也算, 期间不接别的招) */
    private int xueliSlamLeft = 0;
    /** v125: 距离下一发还有几 tick(落地后开始倒数; >0 = 间隔期) */
    private int xueliSlamGap = 0;
    /** ⑦ 的落点(起手时定死, 不跟着玩家跑 —— 预警时间才有意义) */
    private double xueliSlamX, xueliSlamY, xueliSlamZ;
    /** 被砸晕的玩家: UUID -> 还剩几 tick */
    private final java.util.Map<java.util.UUID, Integer> stunTicks = new java.util.HashMap<>();
    /** v121: 被眩晕玩家的**定身锚点**(UUID -> x,y,z) —— 玩家位置客户端说了算, 只能每 tick 拉回来 */
    private final java.util.Map<java.util.UUID, double[]> stunAnchor = new java.util.HashMap<>();
    /**
     * 2026-09-20: 眩晕**结束时**才甩出去的那一下击退(UUID -> dx,dy,dz)。
     * <p>为什么非要拖到最后: 眩晕期间每 tick 都会被拉回锚点 + 水平速度清零,
     * 当场推的那一下下一 tick 就被抹掉了(玩家完全没感觉)。攒到解除的那一 tick 再推,
     * 才是"被按在原地挨完, 然后被击飞出去"的手感 —— 文玄蓝「超能光束」用这一条。
     */
    private final java.util.Map<java.util.UUID, double[]> stunLaunch = new java.util.HashMap<>();
    /* ==================== 青衣 招牌机制「四象轮转」(2026-09-12 v20) ====================
       武器 = 四件 TnS 独特武器**每 8 秒轮换一件**(主手真的换物品, 玩家看得见),
       每件自带主题增益 + 命中减益各不同(见 {@link #onWeaponHit}):
         0 风暴追寻者 stormseeker          -> 自身迅捷 II / 命中: 缓慢 II + 虚弱 II 8 秒
         1 亵渎者     the_desecrator       -> 自身力量 II / 命中: 凋零 II 8 秒
         2 深渊回响   echo_of_the_deep     -> 自身抗性 II / 命中: 失明 II + 反胃 8 秒
         3 恒星       stellaris            -> 自身再生 I  / 命中: 飘浮 I 3 秒 + 缓慢 III 8 秒 */
    public static final String[] QINGYI_WEAPONS = {
            "tridents_n_stuff:stormseeker",
            "tridents_n_stuff:the_desecrator",
            "tridents_n_stuff:echo_of_the_deep",
            "tridents_n_stuff:stellaris",
    };

    /* ==================== 青衣「铁魔法闪电」(2026-09-13 v82 用户要求) ====================
       用户: "青衣现在会使用伤害更高并提高施法频率的闪电和铁魔法闪电"。
       ① 三叉戟的真实落雷: 见 {@code QingyiTridentEntity}(脉冲 4s->3s, 全局冷却 6s->3.5s, 伤害 5.33->16);
       ② **铁魔法闪电**(这一块): 每 {@value #QINGYI_SPELL_CD} tick 施放一次**闪电系法术**, 等级
          {@value #QINGYI_SPELL_LVL_MIN}~{@value #QINGYI_SPELL_LVL_MAX}(比紫菜的 3~5 更高)。
       没装铁魔法时退回"目标头顶真闪电 + 魔法伤害", 保证效果不落空。 */
    public static final String[] QINGYI_LIGHTNING_SPELLS = {
            "irons_spellbooks:chain_lightning",     // 连锁闪电
            "irons_spellbooks:lightning_lance",     // 闪电长矛
            "irons_spellbooks:thunderstorm",        // 雷暴
            "irons_spellbooks:lightning_bolt",      // 落雷
    };
    public static final int QINGYI_SPELL_CD = 4 * 20;
    public static final int QINGYI_SPELL_LVL_MIN = 5;
    public static final int QINGYI_SPELL_LVL_MAX = 7;
    /** 退回实现时的伤害(数字已含全局修正) */
    public static final float QINGYI_SPELL_FALLBACK_DAMAGE = 64.8f;
    private int qingyiSpellCd = 4 * 20;
    public static final int QINGYI_SWAP_TICKS = 160;   // 8 秒一件
    /** 2026-09-12 v29: 三叉戟连续命中多少次要给 4 个随机减益 */
    public static final int QINGYI_TRIDENT_STREAK = 5;
    /** 2026-09-12 v29: 恒星三叉戟雨一次多少把
     *  2026-09-12 v67 按用户反馈「青衣的闪电打击有点太多了」: **16 把 -> 10 把**(次数减少;
     *  频率那边见 {@code QingyiTridentEntity}: 脉冲 4 秒 + 闪电全局 6 秒冷却) */
    public static final int QINGYI_RAIN_COUNT = 12;
    public static final double QINGYI_RIPTIDE_RADIUS = 3.75;
    /** v117 按用户要求「召唤三叉戟的欲望提高」: 她的技能冷却再乘这个系数(约 2.4~4.7 秒一招) */
    public static final double QINGYI_CD_MULT = 0.30;
    /** v117 按用户要求「召唤的三叉戟改成全部都是恒星三叉戟」: `QINGYI_WEAPONS` 里恒星的下标 */
    public static final int QINGYI_STELLARIS_INDEX = 3;

    /* ==================== 青衣「攻防强化」(2026-09-13 v102 用户要求「加强青衣的攻击力和防御力」) ====================
       要点: 青衣的伤害大头是**写死的技能常数**, 只加 ATTACK_DAMAGE 几乎没感觉, 所以两头都补:
         · 攻击: ATTACK_DAMAGE +{@value #QINGYI_ATK_BONUS}(MULTIPLY_TOTAL, 近战/属性派生)
                 + 青衣全部技能伤害 ×{@value #QINGYI_DAMAGE_MULT}
                 + 三叉戟**本体命中**的伤害是原版 ThrownTrident 按"武器基础攻击力"结算的(跟投掷者属性无关),
                   所以在 {@link #onTridentHit} 里补一层"漏掉的那部分"魔法伤害;
         · 防御: ARMOR +{@value #QINGYI_ARMOR_BONUS}(修饰符, 12 -> 18)
                 + 庄园内**承伤 ×{@value #QINGYI_DAMAGE_TAKEN_MULT}** ——
                   庄园主阶段(manorHurt)是**绕过原版护甲**直接扣血的, 不在那里减等于完全没效果。
       ★ 修饰符用新 UUID, 别和 v95「三叉戟层数」那三个(ATK/ARMOR/HEALTH 各 +1/层)混在一起。 */
    /** 青衣历史技能倍率；固定技能已折入各自的基础数值。 */
    public static final double QINGYI_DAMAGE_MULT = 2.25;
    /** 青衣近战攻击力加成(MULTIPLY_TOTAL) */
    public static final double QINGYI_ATK_BONUS = 1.00;
    /** 青衣护甲加成(12 -> 18, 仅影响非庄园路径) */
    public static final double QINGYI_ARMOR_BONUS = 8.0;
    /** 青衣在庄园战场里的承伤倍率(0.75 = 减伤 25%) */
    public static final double QINGYI_DAMAGE_TAKEN_MULT = 0.70;
    private static final java.util.UUID QINGYI_FLAT_ATK_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000d1");
    private static final java.util.UUID QINGYI_FLAT_ARMOR_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000d2");

    /* ==================== 青衣「每招一把三叉戟就变强一点」(2026-09-13 v95 用户要求) ====================
       用户原话:「青衣现在每召唤一把三叉戟则他的全部伤害提高一点, 护甲加一点, 血量加一点」。
       "一把" = 一次 {@code QingyiTridentEntity} 的生成: 「投戟」1 把 / 「恒星三叉戟雨」各 1 把(一次技能 10 把)。
       三种加成都是**每把 +1**, 且"全部伤害"是真的全部 ——
       近战走 ATTACK_DAMAGE 修饰符, 青衣那几处**写死的技能伤害**(激流冲刺 / 投戟必触发 / 铁魔法闪电 /
       三叉戟落雷 / 三叉戟命中)逐个加上 {@link #qingyiDamageBonus()}。
       ★ 为什么用**修饰符**而不是直接改基础值: {@code BossScaling} 每 20 tick 会把 MAX_HEALTH / ATTACK_DAMAGE
       的**基础值**按场上人数重算(setBaseValue), 直接改基础值会被冲掉; 修饰符不受影响。
       层数存进 NBT(区块重载后 setVariant 会清零, 靠 readAdditionalSaveData 还原并重新挂上修饰符)。 */
    /** 每把三叉戟: 全部伤害 +N */
    public static final double QINGYI_STACK_DAMAGE = 1.0;
    /** 每把三叉戟: 护甲 +N */
    public static final double QINGYI_STACK_ARMOR = 1.0;
    /** 每把三叉戟: 血量上限 +N(同时补 N 点当前血量, 免得血条塌一截) */
    public static final double QINGYI_STACK_HEALTH = 1.0;
    /** 伤害层数上限(兜底, 防止一场打太久无限膨胀): 200 把 = 全部伤害 +200 */
    public static final int QINGYI_MAX_STACKS = 200;
    /**
     * ★★★ 2026-09-24 按用户反馈「青衣的 buff 叠加有问题：血量护甲异常上升」★★★
     *
     * <p>实测日志({@code 2026-09-23 23:26~23:32} 那一场)：她 3 分钟就把层数顶到
     * {@code 200/200}——因为「星戟雨」一次落 10 把三叉戟 = 一次技能 +10 层。
     * 于是护甲 {@code 6 → 206}、血量上限 {@code 1000 → 1200}，玩家看到的就是"血条和护甲一路涨"。
     * 护甲在庄园主阶段虽然被 {@code manorHurt} 绕过，但非庄园场合(小怪/召唤物视角)是真减伤，
     * 而且"数值看着失控"本身就是问题。
     *
     * <p>所以**护甲与血量的层数单独封顶**：伤害照旧叠到 {@link #QINGYI_MAX_STACKS}(200)，
     * 护甲最多 +{@value #QINGYI_MAX_ARMOR_STACKS}、血量上限最多 +{@value #QINGYI_MAX_HEALTH_STACKS}。
     * 想彻底关掉某一项就把它改成 0。
     */
    public static final int QINGYI_MAX_ARMOR_STACKS = 20;
    /** 血量上限的层数上限，见 {@link #QINGYI_MAX_ARMOR_STACKS}。 */
    public static final int QINGYI_MAX_HEALTH_STACKS = 20;
    /* ---- v122 按用户要求「青衣召唤三叉戟后获得充能和 6 个随机 buff 持续 30s」 ---- */
    /** 每召唤一把(投戟 1 把 / 星戟雨每把 1 次)额外抽的随机增益个数(**不重复**) */
    public static final int QINGYI_ONSUMMON_BUFFS = 6;
    /** 【充能】与这 6 个随机增益的持续时长 */
    public static final int QINGYI_ONSUMMON_TICKS = 30 * 20;
    /** 【充能】等级显示上限: 层数最高 200, 但等级数字在客户端包里是 byte, 显示封顶到 V */
    public static final int QINGYI_CHARGE_MAX_AMP = 4;
    private static final java.util.UUID QINGYI_ATK_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c1");
    private static final java.util.UUID QINGYI_ARMOR_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c2");
    private static final java.util.UUID QINGYI_HEALTH_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c3");

    /* ==================== 文玄蓝 招牌机制「飞轮绞杀 + 蒸汽泄压」(2026-09-12 v21) ====================
       武器 = Create 飞轮(近战) + 阀门(远程)。
       · 被动绞杀: 每 1 秒, 3.5 格内的玩家各吃 6 点(旋转飞轮磨), 命中时自身迅捷 II 2 秒;
       · 主动泄压: 朝正面喷 **6 格蒸汽锥**(±35 度): 20 伤害 + 击退 + 缓慢 II 6 秒。 */
    public static final double GRIND_RADIUS = 6.25;
    public static final float GRIND_DAMAGE = (float)24;
    public static final double VENT_RANGE = 10.75;
    public static final float VENT_DAMAGE = (float)80;
    /* 2026-09-12 v22 按用户要求: 阀门远程改成**多发追踪回旋飞轮**(参考"光之女王的飞弹") */
    public static final int FLYWHEEL_SHOTS = 6;             // 2026-09-12 v45: 4 -> 6(更快叠满)
    public static final int FLYWHEEL_INTERVAL = 8;
    /** 2026-09-12 v41 按用户要求: 等一会儿让**所有环绕中的飞轮一起齐射** */
    public static final int FLYWHEEL_VOLLEY_TICKS = 20 * 20;
    private int flywheelVolleyCd = FLYWHEEL_VOLLEY_TICKS;
    private int openingFlywheelsGranted;
    private int openingFlywheelsRetry;
    /* ==================== 文玄蓝 招牌连招(v71 用户定案) ====================
       「16~8 格内: 高伤害光束打击 -> 全部飞轮一起扑向玩家 -> 飞轮攻击力 +10%(可叠加)」。
       距离在这个区间内才走这套; 太近/太远仍走原来的"投飞轮/叠满喷蒸汽"。 */
    /** 连招触发距离区间(格) */
    public static final double BEAM_COMBO_MIN = 8.0;
    public static final double BEAM_COMBO_MAX = 16.0;
    /** 光束打击伤害(**高伤害**) —— 2026-09-17 按用户要求「光束打击连招伤害减半」: 120 -> 60
        (结算时仍会乘 {@link #XUANLAN_DAMAGE_MULT}, 所以实际数值也正好减半)。 */
    public static final float BEAM_STRIKE_DAMAGE = (float)60;
    /** 光束命中判定半径(格, 到光束直线的垂直距离) */
    public static final double BEAM_STRIKE_RADIUS = 2.6;
    /** 每次连招后飞轮攻击力 **+10%**(加法叠加: 1.0 -> 1.1 -> 1.2 ...) */
    public static final double FLYWHEEL_DAMAGE_STEP = 0.10;
    /** 飞轮攻击力倍率上限(防止长时间战斗无限叠加) */
    public static final double FLYWHEEL_DAMAGE_CAP = 5.0;
    /* 2026-09-13 v72 按用户要求「玄蓝的击退减少，移速上升，攻击伤害增加」 */
    /**
     * 玄蓝的伤害倍率。注意: 玄蓝是远程 Boss, 它的伤害**不来自 ATTACK_DAMAGE 属性**,
     * 而是飞轮 / 飞轮绞杀 / 蒸汽泄压 / 光束打击这几处写死的数字 —— 所以这里统一乘一下,
     * 同时仍然给 ATTACK_DAMAGE 挂 +30% 修饰符(万一有近战路径也吃得到)。
     *
     * <p>数值沿革: v72 起为 **3.64**(= 2.8 × 1.3, 含当时那次 +30%);
     * ★ v177 按用户要求「提高一点玄蓝的击退和伤害(15%)」→ **4.19**(3.64 × 1.15)。
     * 这一处会同时抬高 {@code GRIND_DAMAGE}(飞轮绞杀)、{@code VENT_DAMAGE}(蒸汽泄压)
     * 与飞轮的 {@code flywheelDamageMul} —— 也就是玄蓝除了"招牌光束"以外的全部伤害。
     * (2026-09-20 起「超能光束」**不再造成伤害**, 改成眩晕 + 清空玩家增益, 见 {@link #XUANLAN_BEAM_STUN}。)
     */
    public static final double XUANLAN_DAMAGE_MULT = 4.19;
    /** 攻击伤害 +30%(用修饰符叠加, 不会被 BossScaling 的基础值覆盖) */
    public static final double XUANLAN_ATK_BONUS = 0.30;
    private static final java.util.UUID XUANLAN_ATK_MOD =
            java.util.UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c6");
    /* ==================== v114 按用户要求「全部庄园 boss 基础攻击力上调（功底除外）」 ====================
       基础值是 createAttributes() 里的 9.0 × WEAPON_DAMAGE_MULT(5) × BOSS_DAMAGE_MULT(4/3) = 60,
       这里再叠一个 **+25%** 的 MULTIPLY_TOTAL 修饰符(和上面玄蓝那套同一写法 —— BossScaling 只改基础值,
       不会把修饰符冲掉)。范围: **庄园轮换名单 MANOR_VARIANTS**, 但**排除功底(variant 3)**;
       麻雀(9)本来就不在名单里, 不受影响。
       想再调: 只改 MANOR_ATK_BONUS 一个数(0.25 = +25%)。 */
    public static final double MANOR_ATK_BONUS = 0.25;
    /** 功底的变体号(排除在本次上调之外) */
    public static final int GONGDI_VARIANT = 3;
    private static final java.util.UUID MANOR_ATK_MOD =
            java.util.UUID.fromString("6f1a2d3c-3333-4a2b-9c3d-0000000000d7");

    /** 这个变体是不是"庄园轮换名单"里的(麻雀=9 是支援位, 不在名单) */
    public static boolean isManorVariant(int v) {
        for (int m : MANOR_VARIANTS) {
            if (m == v) return true;
        }
        return false;
    }
    /**
     * 玄蓝「蒸汽泄压」的击退力度。
     * 沿革: 1.3 -> 0.5 -> 0.18(注释记的 v81 几轮下调) -> 实际曾落在 0.85;
     * ★ v177 按用户要求「提高一点玄蓝的击退(+15%)」→ **0.98**(0.85 × 1.15)。
     * (顺带修掉原来那两行注释: 它们写的 0.18 / 0.05 与代码里的实际值早已不符。)
     */
    public static final double VENT_KNOCKBACK = 0.98;
    /**
     * 玄蓝「光束连招」的击退力度。v177 同样 +15%: 0.55 -> **0.63**。
     */
    public static final double BEAM_KNOCKBACK = 0.63;
    /**
     * ★ v177 新增: 「超能光束」(招牌大招)的击退力度。
     * 原本是写在施放处的**魔法数字 0.45**, 提成常量是为了让玄蓝的三处击退
     * ({@link #VENT_KNOCKBACK} / {@link #BEAM_KNOCKBACK} / 本项)能一起调、一眼看全。
     * v177 按用户要求同幅 +15%: 0.45 -> 0.5175。
     *
     * <p>★ 2026-09-20 (v186) 按用户要求「加强超能光束击退」: 0.5175 -> **1.5**(约 ×2.9)。
     * 同时改了三点, 否则加数字也白加:
     * ① 改走**带施法者**的重载 {@code BossKnockback.push(this, p, …)}, 吃玄蓝自己的
     *    {@code XUANLAN_MULTIPLIER} = 1.5(和它的蒸汽泄压 / 光束连招一致);
     * ② 改成**眩晕解除的那一 tick** 才推({@code stunPlayerWithLaunch} → {@code applyStunLaunch}),
     *    因为眩晕期间每 tick 都被拉回锚点、速度清零, 当场推等于没推;
     * ③ 上抛也从写死的 0.15 提成 {@link #XUANLAN_BEAM_LIFT} 并一起加强。
     * 折算到玩家身上的实际值 = 1.5 × 1.5(玄蓝) × 0.25(对玩家水平折扣) ≈ **0.56**,
     * 上抛 = 0.35 × 1.5 × 0.5 ≈ 0.26。
     */
    public static final double XUANLAN_BEAM_KNOCKBACK = 1.5;
    /** 「超能光束」命中后（眩晕解除时）的上抛力度, 与 {@link #XUANLAN_BEAM_KNOCKBACK} 一起调。 */
    public static final double XUANLAN_BEAM_LIFT = 0.35;

    /* ==================== 文玄蓝「超能光束」(2026-09-13 v79 用户要求) ====================
       "类似故障机器人的超能光束, 但**没有虚弱效果**" ——
       ★ 2026-09-20 按用户要求「删除玄蓝超能光束伤害，改成眩晕和清空玩家buff效果，不要锁头，
         可以锁定一会后放一个前摇施放」:
         ① **不再造成任何伤害** —— 原来的「每脉冲 460 点、四系均分、4 脉冲」整块删除;
         ② 被照到的玩家改为 **眩晕 {@value #XUANLAN_BEAM_STUN} tick + 清空全部增益效果**
            ({@code util/PlayerCrowdControl}); 每次施法每人只结算一次;
         ③ **不再锁头** —— 只有开头 {@value #XUANLAN_BEAM_LOCK} tick 的"锁定"阶段会跟头,
            锁完立刻冻结方向, 接着 {@value #XUANLAN_BEAM_CHARGE} tick 前摇, 再引导
            {@value #XUANLAN_BEAM_FIRE} tick; 前摇与引导期间方向**一动不动 ⇒ 侧移就能躲开**;
         ④ 放完仍然**不进入落地衰弱**(用户明确要求去掉虚弱)。
       流程: 冷却到 -> 锁定 LOCK tick(预警线跟着你) -> 前摇 CHARGE tick(方向已冻结)
             -> 引导 FIRE tick, 每 PULSE tick 判定一次光束内的玩家。 */
    public static final int XUANLAN_BEAM_CD = 16 * 20;
    /** 锁定阶段(秒): 这段时间会一直跟头, 预警线跟着玩家走 */
    public static final int XUANLAN_BEAM_LOCK = 20;
    /** 前摇(秒): 方向已冻结, 红色收缩环 + 预警线 + 音调递增的警示音 —— 真正的躲避窗口 */
    public static final int XUANLAN_BEAM_CHARGE = 20;
    public static final int XUANLAN_BEAM_FIRE = 40;
    public static final int XUANLAN_BEAM_PULSE = 10;
    /** 命中后的眩晕时长: 3 秒 */
    public static final int XUANLAN_BEAM_STUN = 3 * 20;
    public static final double XUANLAN_BEAM_LEN = 40.0;
    public static final double XUANLAN_BEAM_RADIUS = 4.0;
    private int xuanlanBeamCd = 8 * 20;      // 上场后 8 秒放第一次
    private final net.farmingtales.ftboss.boss.ai.XuanlanSkyStrike xuanlanSky =
            new net.farmingtales.ftboss.boss.ai.XuanlanSkyStrike(this);
    private int xuanlanBeamLock = 0;
    private int xuanlanBeamCharge = 0;
    private int xuanlanBeamFire = 0;
    private int xuanlanBeamPulse = 0;
    private double xlBeamDx = 0.0, xlBeamDy = 0.0, xlBeamDz = 1.0;
    /** 本次施法已经结算过的玩家(uuid): 眩晕 + 清增益每次施法每人只吃一次 */
    private final java.util.Set<java.util.UUID> xlBeamPunished = new java.util.HashSet<>();

    public static final int DORA_VARIANT = 2;
    public static final String DORA_SMG_ID = "ftboss:dora_nova_launcher";
    public static final String DORA_FOCUS_ID = "bren:machine_gun";
    public static final int RELOAD_TICKS = 3 * 20;
    public static final String DUOLA_MELEE_ID = "ftboss:dora_lance";
    public static final float SPEAR_CHARGE_DAMAGE = 8.333333F;
    public static final double SPEAR_KNOCKBACK = 1.25;
    public static final double SPEAR_KNOCKBACK_UP = 0.30;
    public static final double BURST_KNOCKBACK = 0.55;
    public static final double BURST_KNOCKBACK_UP = 0.10;

    /* ==================== 折耳根 招牌机制「暗影突袭」(2026-09-12 v21) ====================
       武器 = 铁魔法 + 暗影剑(饥荒风)。技能: 闪到目标**背后** 2.2 格, 一刀 20 点 +
       **凋零 III 6 秒 + 失明 II 5 秒**, 原地留一团墨影(暗影粒子)。 */
    public static final float SHADOW_DAMAGE = 112.0f;
    /** 2026-09-13 v72 按用户要求: 折耳根命中再补 **10 点虚空伤害**(虚空伤害类型自带穿甲, 不吃护甲) */
    public static final float SHADOW_VOID_DAMAGE = 10.0f;
    /* 2026-09-13 v75 按用户要求「折耳根对玩家使用黑暗后一半伤害变成虚空伤害，且伤害降低30%」 */
    /** 总伤害整体 **-30%** */
    public static final double SHADOW_DAMAGE_CUT = 0.70;
    /** 其中**一半**走虚空伤害(fellOutOfWorld, 自带穿甲), 另一半走普通物理 */
    public static final double SHADOW_VOID_HALF = 0.5;
    /** 黑暗时长(5 秒 × 技能倍率) */
    public static final int SHADOW_DARKNESS_TICKS = (int)300;

    /* 2026-09-14 v116 按用户要求「折耳根现在会对玩家造成黑暗效果，同时会对玩家持续造成虚空伤害」:
       · **黑暗**: 打中玩家就挂(v115 把暗影突袭换成了失明 -> 现在**黑暗与失明并存**, 两个都要);
       · **持续虚空伤害**(「暗影侵蚀」): v118 按用户要求「允许虚空持续伤害叠加，可以做成 debuff」——
         已经做成真正的状态效果 {@link net.farmingtales.ftboss.effect.ShadowErosionEffect}:
         每次命中叠 1 层(最多 5 层), 每秒按层数扣虚空伤害, HUD 上有图标与剩余时间;
         不再用 Boss 内部那张计时表。 */
    /** 近战命中挂的黑暗时长 */
    public static final int SHADOW_DARKNESS_ON_HIT = (int)300;

    /* ==================== 紫菜 招牌机制「毒雾爆发」(2026-09-12 v21) ====================
       武器 = 河豚棒(魔法)。技能: 4.5 格毒雾: 14 伤害 + **中毒 III 10 秒 + 缓慢 II 6 秒**。 */
    public static final double POISON_RADIUS =6.75f;
    public static final float POISON_DAMAGE = (float)56;

    /** 蛋蛋「烛影置换」的伤害 与 柚子「自我叠影」满层爆发 (v29 再 x2) */
    public static final float ENDER_SWAP_DAMAGE = (float)64;
    /** ★ 2026-09-14 蛋蛋「连续瞬移」: 每下 20 点**魔法**伤害, 共 6 下, 每 5 tick 一下 */
    public static final float DANDAN_RUSH_DAMAGE = 28.0f;
    public static final int DANDAN_RUSH_HITS = 6;
    public static final int DANDAN_RUSH_INTERVAL = 5;
    private int dandanRush = 0;
    private int dandanRushTick = 0;
    public static final float YOUZI_BURST_DAMAGE = (float)72;

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MINI =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IMPOSSIBLE =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.BOOLEAN);
    /** 保留旧实体数据槽，避免其他同步字段的编号变化。 */
    private static final EntityDataAccessor<Boolean> DATA_GIANT =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> DATA_COMBAT_ANIMATION =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<CompoundTag> DATA_SKILL_CUE =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<CompoundTag> DATA_QIZAI_ARSENAL =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Boolean> DATA_DORA_RELOADING =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_XUANLAN_SKY_HIDDEN =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DORA_SPEAR_TICKS =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MANOR_INTRO_HIDDEN =
            SynchedEntityData.defineId(EasterBossEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int POISON_WINDUP = HiddenBossCastRules.quicker(20);
    public static final int XUELI_PAN_WINDUP = HiddenBossCastRules.quicker(12);
    public static final int SHADOW_STRIKE_WINDUP = HiddenBossCastRules.quicker(10);
    public static final int CUE_RELEASE_TICKS = 12;
    private int poisonWind;
    private int xueliPanWind;
    private int shadowStrikeWind;
    private java.util.UUID shadowStrikeTarget;

    /* ==================== ★ 2026-09-24 折耳根「虚影」(按用户要求) ====================
       用户原话：「给折耳根加一个传送，施法后 10% 留下虚影，然后过一会虚影再使用一次技能的效果，
       然后虚影消失」；随后明确「独立做一个折耳根人形紫色残影的模型来达成效果，
       虚影本身无碰撞体积和攻击性」。

       <p>所以残影是**独立实体** {@link ShadowPhantomEntity}（人形紫色残影，无碰撞/无 AI/无攻击/
       不可被选中/不会移动/不受攻击伤害），它自己**不结算任何伤害**；"复现一次技能"由**本体**在到点时结算
       （{@link #firePhantomEcho}）—— 伤害来源仍是折耳根，因此照旧吃
       {@code BossOutgoingDamageNerfs} 的折耳根倍率，不会因为"换了个实体打"而突然变疼。
       观感上沿用 benger 那套虚影的**半透明渲染**做法，但不沿用它的免伤语义。

       <p>★ 用户第二轮补充：「保持它会记录当时折耳根触发残影的技能并在 5~0s 内被残影重新施放，
       伤害会变成原伤害 25% 的真伤」——
       所以残影**记下触发它的那一招**（{@link #phantomEchoSkill}），延迟取
       {@link #PHANTOM_ECHO_MIN_DELAY}~{@link #PHANTOM_ECHO_MAX_DELAY} tick（即 0~5 秒）之间的随机值，
       复现的伤害 = 原伤害 × {@link #PHANTOM_ECHO_DAMAGE_MULT}(25%)，且**整份走真伤/穿甲**。

       <p>★ 用户第三轮：「检测一下残影有没有触发，然后触发概率提高到35%」——
       检测结果：**一次都没触发过**，因为折耳根真正会放的招是扩展技能（影刃十字 / 噬影牢笼），
       原生「暗影突袭」是死代码（见 {@link #onManorFiveCastStart} 的说明）。
       所以留影改挂在他真正会放的技能上，概率提到 {@value #SHADOW_PHANTOM_CHANCE_PERCENT}%。 */
    /** 留残影的概率。 */
    public static final double SHADOW_PHANTOM_CHANCE = 0.65;
    /** 日志/文档里显示用的百分比。 */
    public static final int SHADOW_PHANTOM_CHANCE_PERCENT = 65;
    /** 残影复现延迟的下界（tick）：用户口径「5~0s 内」，所以下界是 0。 */
    public static final int PHANTOM_ECHO_MIN_DELAY = 0;
    /** 残影复现延迟的上界（tick）：5 秒。 */
    public static final int PHANTOM_ECHO_MAX_DELAY = 5 * 20;
    /** 复现之后再活多久消散（tick）。2026-09-24 从 10 提到 20（0.5 → 1 秒）：残影更容易被看见。 */
    public static final int PHANTOM_LINGER = 20;
    /** 残影复现的伤害倍率：原伤害的 25%（用户点名），且整份**按真伤**结算。 */
    public static final double PHANTOM_ECHO_DAMAGE_MULT = 0.25;
    /** 残影复现时允许的最大距离（格）—— 以**残影**的位置量，不按本体算。 */
    public static final double PHANTOM_ECHO_RANGE = 16.0;
    /** 残影记录的技能 id：折耳根「暗影突袭」。以后别的技能也能留影时再加新值。 */
    public static final String PHANTOM_SKILL_SHADOW_STRIKE = "shadow_strike";
    /** 仅本体：距离复现还剩多少 tick（<=0 表示没有待结算的残影）。 */
    private int phantomEchoTicks = -1;
    /** 仅本体：**残影记录下来的那一招**（触发残影时折耳根正在放什么）。 */
    private String phantomEchoSkill;
    /** 仅本体：复现时要打的目标（留影那一刻锁定的那个）。 */
    private java.util.UUID phantomEchoTarget;
    /** 仅本体：残影所在位置（复现的距离/LOS 以它为准）。 */
    private double phantomEchoX, phantomEchoY, phantomEchoZ;

    /* ==================== ★ 2026-10-10 折耳根被动「往日之影」+ 传送追击提速 ====================
       用户原话：「折耳根的传送追击效果较慢，然后新增被动：往日之影 —— 在折耳根给与玩家
       **满层虚空侵蚀**后每次技能都会触发一次残影，残影全部伤害变成虚空伤害」。 */

    /** 被动名（日志与玩家提示用）。 */
    public static final String PASSIVE_PAST_SHADOW = "往日之影";
    /**
     * 「往日之影」留下的残影复现倍率。
     *
     * <p>★ 2026-10-10 用户确认口径：「残影**全部伤害**变成虚空伤害」= 残影打出**原伤害 100% 的虚空伤害**
     * （不是原残影的 25%）。所以这里是 {@code 1.0} —— 与普通残影的
     * {@link #PHANTOM_ECHO_DAMAGE_MULT}(25%) 区分开。
     *
     * <p>注意：100% 指的是"技能原伤害的 100%"，仍然照旧过
     * {@code BossOutgoingDamageNerfs.ZHEERGEN_MULTIPLIER} 那道折耳根出伤倍率与
     * {@code ManorAdaptiveDamage} 的自适应加成，不会因为"换了个来源"而绕开平衡。
     */
    public static final double PHANTOM_PAST_SHADOW_MULT = 1.0;
    /** 日志/文案里显示用的百分比。 */
    public static final int PHANTOM_PAST_SHADOW_PERCENT = 100;
    /** 仅本体：本次待复现的残影是否由「往日之影」留下（决定日志口径；两条路的伤害都走虚空/穿甲）。 */
    private boolean phantomEchoPastShadow;
    /** 仅本体：本场是否已播报过「往日之影」唤醒（只在第一次满层时提示一次）。 */
    private boolean pastShadowAnnounced;

    /**
     * 闪现追击：玩家拉开到这个距离以上就闪到他背后（2026-10-10 用户「传送追击效果较慢」）。
     *
     * <p>以前折耳根**只在技能起手那一瞬**闪一次，玩家一跑就只能靠两条腿追，观感很慢。
     */
    public static final double ZHEERGEN_BLINK_DIST = 8.0;
    /** 闪现追击的冷却（tick）：3 秒。 */
    public static final int ZHEERGEN_BLINK_COOLDOWN = 3 * 20;
    /** 闪现追击的距离上限（格）：超过这个距离交给庄园的召回/传送机制，不自己跨半张地图闪。 */
    public static final double ZHEERGEN_BLINK_MAX = 48.0;
    /** 仅本体：闪现追击的冷却倒计时。 */

    private final ServerBossEvent bossBar;
    private int actionTicks = 0;
    private int castTicks = 0;
    private int skillCd = 60;
    private long sharedSkillClockTick;
    private boolean sharedSkillClockInitialized;
    private int miniLife = -1;      // 仅迷你分身: 剩余存活 tick
    private int youziHits = 0;      // 仅柚子本体: 累计命中次数(每 3 次召分身)
    private int burstShots = 0;     // 仅朵拉: 连射剩余发数
    /* v112: 删掉 burstTimer —— 朵拉的连射早就改用 burstAcc(半 tick 精度的射速累加器),
       这个字段只在 skillSuppress 里被赋 0、从没被读过。死字段。 */
    private int burstHits = 0;      // 仅朵拉: 本轮连射命中数
    private double burstAcc = 0;
    private int reloadTicks = 0;
    private int spearHitCd = 0;
    private int doraSpearLeft;
    private int doraSpearGap;
    private java.util.UUID doraSpearTarget;
    private int burstTotalShots = 0;
    private double doraSkillProgress = 0;
    private double doraReloadProgress = 0;
    private final DoraCombatTempo doraTempo = new DoraCombatTempo();
    private final DoraSniperCycle doraSniper = new DoraSniperCycle();
    private Vec3 doraSniperDirection = Vec3.ZERO;
    private boolean doraSniperNext;
    private final GongdiSkills gongdiSkills = new GongdiSkills(this);
    private final ManorFiveSkills manorFiveSkills = new ManorFiveSkills(this);
    private int cleaveWind = 0;
    /** ★ v128: 断罪斩用完后攻击力 +20%(用户要求), 持续 5 秒 */
    public static final float CLEAVE_ATK_BONUS_PERCENT = 0.20f;
    public static final int CLEAVE_ATK_BOOST_TICKS = 5 * 20;
    /** ★ v128 按用户要求: 断罪斩用完后**攻击力 +20% 持续 5 秒**的剩余 tick */
    private int cleaveAtkT = 0;
    private static final java.util.UUID CLEAVE_ATK_MOD =
            java.util.UUID.fromString("6f1a2d3c-3333-4a2b-9c3d-0000000000e1");

    /** ★ v128: 断罪斩命中后给功底挂 5 秒攻击力 +20%(走属性修饰符, 到点自己摘) */
    private void applyCleaveAtkBoost() {
        var atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk == null) return;
        atk.removeModifier(CLEAVE_ATK_MOD);
        atk.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                CLEAVE_ATK_MOD, "ftboss.cleave_boost",
                atk.getBaseValue() * CLEAVE_ATK_BONUS_PERCENT,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        this.cleaveAtkT = CLEAVE_ATK_BOOST_TICKS;
        FTBossMod.LOGGER.info("[FTBoss][Easter] 功底「断罪斩」攻击力 +{}% 持续 {} 秒",
                (int) (CLEAVE_ATK_BONUS_PERCENT * 100), CLEAVE_ATK_BOOST_TICKS / 20);
    }
    private int panWind = 0;        // 仅柠檬猫: 猛火翻锅蓄力剩余 tick
    private int qingyiIndex = 0;    // 仅青衣: 当前拿第几件(0~3)
    private int qingyiTimer = QINGYI_SWAP_TICKS;
    private final QingyiConfluenceSkill qingyiConfluence = new QingyiConfluenceSkill(this);
    private boolean qingyiConfluenceNext = true;
    private int lastMiniSummon = -10000;   // 仅柚子: 上次分裂的 tickCount
    private int lastYouziLog = -10000;     // 仅柚子: 上次"叠影计数"日志的 tickCount(v53 限频用)
    private int flywheelShots = 0;         // 仅文玄蓝: 待投出的飞轮数
    private int flywheelTimer = 0;
    private int noTargetTicks = 0;         // 全员: 连续没锁定到玩家的 tick 数(满 15 秒抓人)
    private int bloodRage = 0;             // 仅功底: 血怒层数(每层 +25% 攻击力)
    /** v71 仅文玄蓝: 飞轮攻击力倍率(每次"光束连招" +10%, 上限 {@link #FLYWHEEL_DAMAGE_CAP}) */
    private double flywheelDamageMul = 1.0;
    private int meleeLockTicks = 0;
    /**
     * ★ 2026-09-24: 「距上一次**真的打到玩家**过了多少 tick」—— 珍珠追击与场地禁飞都看它
     * （清零点见 {@code ManorChaseTracker}，常量见 {@link net.farmingtales.ftboss.util.ManorChaseRules}）。
     */
    private int playerHitIdleTicks = 0;
    /** ★ 2026-09-24: 珍珠追击的冷却倒计时（"减少触发"的那一半）。 */
    private int pearlRushCd = 0;
    /** ★ 2026-09-24: 是否因为「爱意滑翔」(brewery:haley) 而处于飞行状态。 */
    private boolean haleyFlying = false;
    private int purgeTimer = 0;            // 全员: 中减益后倒计时, 到 0 清空自身所有效果
    /** v106: 追击啃地的倒计时(见 {@code digTick}); 0/负 -> 下一次追到人就啃一层 */
    private int digT = 0;
    private int riptideTicks = 0;          // 仅青衣: 激流冲刺剩余 tick
    private int riptideHitCd = 0;          // 仅青衣: 冲刺伤害的间隔
    private int potionDelay = 0;           // 仅青衣: 冲刺结束后扔药水的倒计时
    private int qingyiTridentHits = 0;     // 仅青衣: 三叉戟连续命中玩家的次数(满 5 给 4 个减益)
    private int qingyiRain = 0;            // 仅青衣: 恒星三叉戟雨剩余把数
    private int qingyiRainTimer = 0;       // 仅青衣: 雨点间隔
    private boolean qingyiAlt = false;     // 仅青衣: 两个技能交替(投戟/星戟雨)
    /** 仅青衣 v95: 已经召唤过多少把三叉戟 = 三种加成各 +这么多 */
    private int qingyiTridentStacks = 0;
    private int qingyiFallingTridentCooldown = 0;
    /** v122: 「青衣召唤三叉戟」日志节流(星戟雨一次 10 把, 每秒最多写一条) */
    private int qingyiOnSummonLogCd = 0;
    private boolean chainGuard = false;    // 仅内部: 防止"连锁增益"递归触发
    private int lastChainBuff = -10000;    // 上次连锁增益的 tick
    /** 2026-09-12 v52: 安静上buff(麻雀的支援用它) —— 不触发"连锁增益"提示、不给玩家挂减益 */
    private boolean quietBuffs = false;
    private boolean flywheelMaxed = false; // 仅文玄蓝: 飞轮是否叠满(叠满后飞轮攻速 4/秒 -> 10/秒)
    public boolean isFlywheelMaxed() { return this.flywheelMaxed; }
    /** v71: 飞轮攻击力倍率(供 {@code FlywheelProjectileEntity} 结算时取用); 非文玄蓝/未叠加时为 1.0 */
    public double flywheelDamageMul() { return this.flywheelDamageMul; }
    private int sparrowLife = 0;           // 仅麻雀: 已经来了多久
    private final java.util.List<java.util.UUID> summons = new java.util.ArrayList<>();   // 仅柠檬猫: 召唤的帮手
    /** v119 按用户要求「柠檬会使用食物boss的随机技能」: 三招轮换的步进(0 喊帮手 / 1 翻锅 / 2 借用技能) */
    private int lemonStep = 1;
    private final net.farmingtales.ftboss.boss.ai.LemonSacrifice lemonSacrifice =
            new net.farmingtales.ftboss.boss.ai.LemonSacrifice(this);

    public net.farmingtales.ftboss.boss.ai.LemonSacrifice lemonSacrifice() {
        return this.lemonSacrifice;
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, Entity source) {
        if (this.lemonSacrifice != null && this.lemonSacrifice.capturesEffects() && effect.getEffect().isBeneficial()) {
            return this.lemonSacrifice.grant(effect);
        }
        return super.addEffect(effect, source);
    }
    private int summonLife = 0;            // 仅柠檬猫: 帮手剩余存活 tick
    private int stuckTicks = 0;            // 卡死看门狗: 连续处于 NoAi 的 tick
    private net.minecraft.world.phys.Vec3 riptideDir = net.minecraft.world.phys.Vec3.ZERO;
    private final boolean[] extraBones = new boolean[4];
    private final net.farmingtales.ftboss.boss.ai.ManorKiting impossibleKiting = new net.farmingtales.ftboss.boss.ai.ManorKiting();

    public EasterBossEntity(EntityType<? extends EasterBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 180;
        this.bossBar = new ServerBossEvent(this.getDisplayName(),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
    }

    @Override
    public void setSpeed(float speed) {
        if (this.isImpossibleChallenge()) {
            super.setSpeed(speed * this.getChallengeDifficulty().movementMultiplier(this.getVariant()));
            this.setZza(speed);
            return;
        }
        super.setSpeed(BossWalkingSpeed.speed(this, speed)
                * (this.isMini() ? 1.0F : this.getChallengeDifficulty().movementMultiplier(this.getVariant())));
        BossWalkingSpeed.normalizeInput(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, net.farmingtales.ftboss.util.ManorSupportRules.BASE_HEALTH)
                /* 2026-09-12 v46: 护甲值翻倍(6 -> 12)。
                   ★ v176 按用户要求「boss 护甲值减半」: 12 -> **6**(正好回到 v46 之前)。 */
                .add(Attributes.ARMOR, 6.0)
                /* 2026-09-12 v23/v29 按用户要求: 武器(近战)伤害 x5 再 x2 -> 90 */
                .add(Attributes.ATTACK_DAMAGE,60)
                /* 2026-09-12 v23: 移速提高(另有常驻速度 I)。
                   ★ v176 按用户要求「移动速度减半」: 0.33 -> **0.165**。
                   (常驻的「速度 I」增益没动 —— 那是 buff, 与基础移速是两回事;
                    若之后觉得仍偏快, 再调 SPEED_EMPOWER 那一层。) */
                .add(Attributes.MOVEMENT_SPEED, BossWalkingSpeed.BASE_SPEED
                        * net.farmingtales.ftboss.util.ManorBaseMovementSpeed.MULTIPLIER)
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19: 允许被击退(功底穿亚当甲时仍会被抬到 0.9)
                /* 2026-09-12 v53 按用户要求: **允许 Boss 登上 3 格高的方块** —— 原版步高只有 0.6 格,
                   1 格高的台阶就得跳; Forge 这个"额外抬腿高度"给 2.4, 合计 3.0 格(寻路也会跟着认) */
                .add(net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get(), 2.4)
                /* 2026-09-13 v81: 42 -> 64(配合"离玩家太远主动寻找玩家") */
                .add(Attributes.FOLLOW_RANGE, 64.0)
                /*
                 * ★★★ 2026-09-24 实机崩溃修（08:18）★★★
                 * 「爱意滑翔」允许飞行时，`haleyFlightTick` 会把走控换成
                 * {@link net.minecraft.world.entity.ai.control.FlyingMoveControl}，
                 * 而它**每 tick** 都要读 `Attributes.FLYING_SPEED`：
                 *     FlyingMoveControl.tick → LivingEntity.getAttributeValue(FLYING_SPEED)
                 *       → AttributeSupplier.getValue → IllegalArgumentException:
                 *         Can't find attribute minecraft:generic.flying_speed
                 * 属性表里没注册这个属性 → **服务端当场崩**（崩报 Suspected Mod = ftboss）。
                 * `Mob.createMobAttributes()` 不含飞行速度，必须显式加；0.4 与鹦鹉同级。
                 * 只影响"真的在飞"的那几秒，其余行为完全不变。
                 */
                .add(Attributes.FLYING_SPEED, 0.4);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PursueGoal(1.35));
        this.goalSelector.addGoal(2, new PlayerReachMeleeGoal(this, 1.2, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        /* 2026-09-13 v78 按用户要求: **允许索敌玩家以外的生物**(铁傀儡/中立怪/其它 mod 的怪) ——
           以前只认 Player, 玩家召来的铁傀儡把 Boss 打了它也只能靠"被打还手"偶尔反击。
           排除项: 本模组自己的实体(Boss/召唤物/投射物)与彩蛋同伴, 免得庄园自己打起来。 */
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this,
                net.minecraft.world.entity.Mob.class, 10, true, false, this::isValidMobTarget));
    }

    /** v78: 除了玩家以外的合法目标(自家实体与彩蛋同伴除外) */
    private boolean isValidMobTarget(LivingEntity e) {
        if (e == null || e == this || !e.isAlive()) return false;
        if (e instanceof Player) return false;                     // 玩家由上面那条目标负责
        if (e instanceof EasterBossEntity) return false;           // 彩蛋同伴(含分身/麻雀/雪黎)
        /* ★ 2026-09-14 v119 修「女仆没死护盾就失效」的真凶:
           雪黎召来的车万女仆是**外来实体**(不是 ftboss 命名空间), 于是通过了下面那两道过滤,
           被她自己当成目标一刀砍死(日志: `雪黎的女仆被雪黎杀死了`) —— 女仆 20 血、她一刀 60+。
           现在带"护卫标记"的女仆一律**不可被本模组 Boss 索敌**(和沙耶的茶芽庇佑同理)。 */
        if (net.farmingtales.ftboss.boss.ai.XueliMaids.isGuard(e)) return false;
        var key = net.minecraft.world.entity.EntityType.getKey(e.getType());
        if (key != null && FTBossMod.MOD_ID.equals(key.getNamespace())) return false;   // 自家召唤物/投射物
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(DATA_MINI, false);
        this.entityData.define(DATA_IMPOSSIBLE, false);
        this.entityData.define(DATA_GIANT, false);
        this.entityData.define(DATA_COMBAT_ANIMATION, new CompoundTag());
        this.entityData.define(DATA_SKILL_CUE, new CompoundTag());
        this.entityData.define(DATA_QIZAI_ARSENAL, new CompoundTag());
        this.entityData.define(DATA_DORA_RELOADING, false);
        this.entityData.define(DATA_XUANLAN_SKY_HIDDEN, false);
        this.entityData.define(DATA_MANOR_INTRO_HIDDEN, false);
        this.entityData.define(DATA_DORA_SPEAR_TICKS, 0);
    }

    /** 血条(供入场演出做"凋零式填充") */
    public ServerBossEvent getBossBar() { return this.bossBar; }

    /** 显示名 = 变体名(9 位共用同一实体类型, 靠这个区分名牌/血条) */
    @Override
    public net.minecraft.network.chat.Component getName() {
        return net.minecraft.network.chat.Component.literal(NAMES[this.getVariant()]);
    }

    /* ---------------- 变体 ---------------- */
    public int getVariant() { return Math.max(0, Math.min(COUNT - 1, this.entityData.get(DATA_VARIANT))); }

    private java.util.UUID manorSummonerId;

    public java.util.UUID getManorSummonerId() {
        return this.manorSummonerId;
    }

    public void setManorSummonerId(java.util.UUID summonerId) {
        this.manorSummonerId = summonerId;
    }

    public boolean canTeleportWith(LivingEntity target) {
        return target instanceof ServerPlayer player && player.level() == this.level()
                && player.isAlive() && !player.isSpectator()
                && ManorTeleportRules.isSummoner(this.manorSummonerId, player.getUUID());
    }

    /**
     * 2026-09-19 防逃课扩容（用户口径 A）：Boss 可以被送到**任意参战者**身边，不再只认召唤者。
     * 基础条件与 {@link #canTeleportWith} 一致（同维度、存活、非旁观）；
     * "是不是本场参战者"由调用方用持久化的参战名单判定 —— 仍然不猜最近玩家。
     */
    public boolean canTeleportToFightMember(ServerPlayer player) {
        return player != null && player.level() == this.level()
                && player.isAlive() && !player.isSpectator();
    }

    private ServerPlayer manorSummoner() {
        if (this.manorSummonerId == null || !(this.level() instanceof ServerLevel serverLevel)) return null;
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(this.manorSummonerId);
        return this.canTeleportWith(player) ? player : null;
    }

    /** 是否「自我叠影」召出的迷你分身(区别: 渲染 0.55 倍 + 限时消散 + 不刷屏血条) */
    public boolean isMini() { return this.entityData.get(DATA_MINI); }

    public boolean isImpossibleChallenge() { return this.entityData.get(DATA_IMPOSSIBLE); }

    public ManorDifficulty getChallengeDifficulty() {
        return this.isImpossibleChallenge() ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL;
    }

    public void setChallengeDifficulty(ManorDifficulty difficulty) {
        boolean wasImpossible = this.isImpossibleChallenge();
        this.entityData.set(DATA_IMPOSSIBLE, difficulty == ManorDifficulty.IMPOSSIBLE);
        if (!wasImpossible && this.isImpossibleChallenge()) {
            this.flywheelVolleyCd = difficulty.acceleratedTicks(this.flywheelVolleyCd);
        }
        this.refreshWalkingSpeed();
    }

    public void refreshWalkingSpeed() {
        var movement = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) movement.setBaseValue(net.farmingtales.ftboss.util.ManorBaseMovementSpeed.scale(
                net.farmingtales.ftboss.util.YouziMiniRules.speed(
                this.isImpossibleChallenge() ? ManorDifficulty.LEGACY_SPEED : this.isMini()
                        ? (RANGED[this.getVariant()] ? RANGED_MOVEMENT_SPEED : 0.33) : BossWalkingSpeed.BASE_SPEED,
                this.getVariant(), this.isMini()), this.isMini()));
    }

    public void setMini(boolean mini) {
        this.entityData.set(DATA_MINI, mini);
        this.refreshWalkingSpeed();
        this.manorFiveSkills.refreshAttackBonus();
        if (!mini) return;
        this.miniLife = MINI_LIFE_TICKS;
        this.setPersistenceRequired();
        this.xpReward = 0;
        var hp = this.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(net.farmingtales.ftboss.util.YouziMiniRules.health(MINI_HEALTH, this.getVariant()));
        this.setHealth((float) net.farmingtales.ftboss.util.YouziMiniRules.health(MINI_HEALTH, this.getVariant()));
        net.farmingtales.ftboss.youzi.YouziMiniBalance.restore(this);
    }

    /** 旧终章的兼容标记，不提供巨人属性、体型或攻击。 */
    public boolean isGiant() { return this.entityData.get(DATA_GIANT); }

    public void setGiant(boolean giant) {
        this.entityData.set(DATA_GIANT, giant);
    }

    public void setVariant(int v) {
        this.qizaiMagic.forceCancel();
        this.qizaiArsenal.cancel();
        this.xuanlanSky.cancel();
        this.qingyiConfluence.cancel();
        this.qingyiConfluenceNext = true;
        this.xueliDialogue.restore(0, this.level().getGameTime());
        this.xueliRollingPinQuoteReadyAt = 0L;
        this.gongdiSkills.reset();
        this.manorFiveSkills.reset();
        this.qingyiRain = 0;
        this.qingyiFallingTridentCooldown = 0;
        this.riptideTicks = 0;
        this.potionDelay = 0;
        this.entityData.set(DATA_VARIANT, Math.max(0, Math.min(COUNT - 1, v)));
        this.manorFiveSkills.refreshAttackBonus();
        this.clearDoraCombat();
        this.doraSniperNext = false;
        /* 血条标题跟随变体名字 */
        this.bossBar.setName(Component.literal("§c" + NAMES[this.getVariant()]));
        this.bossBar.setColor(BossEvent.BossBarColor.RED);
        this.refreshExtraBones();
        this.appleStage = 0;                    // v51: 新上场/新变体 -> 附魔金苹果阶段重新计
        this.equipWeapon();
        this.equipArmor();
        if (this.getVariant() == 5) { this.qingyiIndex = 0; this.qingyiTimer = QINGYI_SWAP_TICKS; }
        /* v95: 青衣的三叉戟层数也从 0 起(区块重载时 setVariant 会走到这, 稍后由 readAdditionalSaveData 还原) */
        this.qingyiTridentStacks = 0;
        this.syncQingyiTridentBuffs();
        this.applyQingyiFlatBuffs();       // v102: 青衣固定攻防强化
        this.refreshWalkingSpeed();
        /* v72 按用户要求「玄蓝攻击伤害增加」: +30% 走**修饰符**(BossScaling 只改基础值, 不会把它冲掉) */
        var atkForXl = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkForXl != null) {
            atkForXl.removeModifier(XUANLAN_ATK_MOD);
            if (this.getVariant() == 6) {
                atkForXl.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        XUANLAN_ATK_MOD, "ftboss.xuanlan_atk", XUANLAN_ATK_BONUS,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        /* ★ v114 按用户要求「全部庄园 boss 基础攻击力上调（功底除外）」: +25% 修饰符 */
        var atkManor = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkManor != null) {
            atkManor.removeModifier(MANOR_ATK_MOD);
            if (isManorVariant(this.getVariant()) && this.getVariant() != GONGDI_VARIANT) {
                atkManor.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        MANOR_ATK_MOD, "ftboss.manor_atk", MANOR_ATK_BONUS,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        /* 2026-09-12 v24: 血怒一律从 0 层开始 */
        this.bloodRage = 0;
        var atkAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkAttr != null) atkAttr.removeModifier(BLOOD_RAGE_MOD);
        /* v88: 满层才有的移速加成也要一起摘掉(不然重上场会带着上一场的速度) */
        var spdAttrReset = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spdAttrReset != null) spdAttrReset.removeModifier(BLOOD_RAGE_SPEED_MOD);
        /* v71: 飞轮攻击力倍率也重置(每次上场从 1.0 起); v72: 玄蓝从 1.3 起(他的伤害 +30%) */
        this.flywheelDamageMul = this.getVariant() == 6 ? XUANLAN_DAMAGE_MULT : 1.0;
        this.flywheelMaxed = false;
        this.openingFlywheelsGranted = 0;
        this.openingFlywheelsRetry = 0;
        /* v78: 雪黎的冲锋状态重置。
           ★ v115 按审计报告 G: **不要再重置 xueliNext** —— setVariant 在"每位上场"和"区块重载"时都会走到这,
           以前每次都打回 3, 一场只放得出 4 招, 于是 0/1/2(汤锅/平底锅/擀面杖)永远轮不到(日志里各 0 次)。
           现在轮换进度存 NBT(见 addAdditionalSaveData), 跨场次接着转; 全新实体仍从字段初值 3 起。 */
        this.xueliRollTicks = 0;
        this.xueliRollHitCd = 0;
        this.crushTicks.clear();
        this.shadowStreak = 0;                 // v117: 连击/转虚空状态也跟着清
        this.shadowVoidMode = false;
        this.beaconBanNotice.clear();          // v117: 蛋蛋的信标压制提示限流表
        this.xueliCloneCd = 6 * 20;            // v118: 雪黎自爆分身的独立冷却
        this.dandanHits = 0;                   // v119: 蛋蛋的攻击成长重置
        var atkReset = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkReset != null) atkReset.removeModifier(DANDAN_ATK_MOD);
        this.lemonStep = 1;                    // v119: 柠檬的三招轮换步进
        /* v111: ⑦ 天降瞬砸 + 眩晕状态一起清(万一是在悬停半空被重置的, 别把她永久留在天上)
           v125: 连砸的"剩余发数/间隔倒数"也要清, 否则重置后会在原地补砸 */
        if (this.xueliSlamWarn > 0) this.setNoGravity(false);
        this.xueliSlamWarn = 0;
        this.xueliSlamLeft = 0;
        this.xueliSlamGap = 0;
        this.stunTicks.clear();
        this.stunAnchor.clear();               // v121: 定身锚点也一起清
        this.stunLaunch.clear();               // 2026-09-20: 攒着的击退也一起清
        /* v79: 玄蓝的超能光束状态重置 */
        this.xuanlanBeamLock = 0;
        this.xuanlanBeamCharge = 0;
        this.xuanlanBeamFire = 0;
        this.xuanlanBeamPulse = 0;
        this.xuanlanBeamCd = 8 * 20;
        this.xlBeamPunished.clear();
        /* v124: 蛋蛋的虚空漩涡一起收掉(实体丢出去的就 discard, 免得重置后还留个漩涡在场上) */
        this.dandanVortexTicks = 0;
        this.dandanVortexCd = 6 * 20;
        this.dandanVortexPulls = 0;
        if (this.dandanVortexEntity != null) {
            this.dandanVortexEntity.discard();
            this.dandanVortexEntity = null;
        }
    }

    /** 武器附魔等级(与护甲一致: 满附魔 8 级) */
    public static final int WEAPON_ENCH_LEVEL = 8;
    /** 武器附魔 id(2026-09-12 v50 按用户要求"检查 Boss 装备有没有附魔" —— 以前**只有护甲有, 武器没有**)。
     *  运行时才用 {@link #buildEnchants} 取注册对象: 静态初始化时附魔注册表还没填好。 */
    private static final String[] WEAPON_ENCH_IDS = {
            "sharpness", "smite", "fire_aspect", "sweeping", "unbreaking", "mending"
    };

    public static final String[] MINI_WEAPONS = net.farmingtales.ftboss.boss.ai.YouziCloneWeapons.weaponIds();

    /** ★ 2026-09-14 按用户要求: "提取 buff"时**不提取「焕新之触」**(herbalbrews:renewal) */
    private static boolean isNoStealBuff(MobEffectInstance mi) {
        if (mi == null || mi.getEffect() == null) return true;
        var key = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(mi.getEffect());
        return key != null && "herbalbrews".equals(key.getNamespace()) && "renewal".equals(key.getPath());
    }

    /**
     * ★ 2026-09-16 按用户要求: **「血怒」不能被抽走**（"不允许 boss 抽走血怒"）。
     *
     * <p>为什么单独开一个方法而不是加进 {@link #isNoStealBuff}：那个白名单**同时**管两条路 ——
     * ①「复制玩家增益」（{@code syncBuffs...}，Boss 只是 {@code addEffect} 到自己身上）；
     * ②「抽掉玩家一个增益」（{@link #stripOneBuff}，蛋蛋 / 紫菜专用）。
     * 用户要的是"两边都该有血怒、Boss 只是复制"，所以**只有抽取那条路要排除血怒**，
     * 复制那条路照旧（Boss 仍然能拿到血怒）。加进 isNoStealBuff 会把复制也一起挡掉 ✗。
     */
    private static boolean isNoStripBuff(MobEffectInstance mi) {
        if (isNoStealBuff(mi)) return true;
        var key = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(mi.getEffect());
        if (key == null || !"ftboss".equals(key.getNamespace())) return false;
        String path = key.getPath();
        return "blood_rage".equals(path) || "blood_rage_fake".equals(path);
    }

    /** ★ 2026-09-14 按用户要求: 柚子分身**共享本体增益** */
    public void copyBuffsFrom(EasterBossEntity src) {
        if (src == null || src == this) return;
        for (MobEffectInstance mi : src.getActiveEffects()) {
            this.addEffect(new MobEffectInstance(mi.getEffect(), mi.getDuration(), mi.getAmplifier(),
                    mi.isAmbient(), false, false));
        }
    }

    /** 本体每 3 秒把自身增益同步给在场分身(48 格内, 同变体) */
    private void shareBuffsToMinis() {
        if (!(this.level() instanceof ServerLevel slShare)) return;
        for (EasterBossEntity m : slShare.getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(48))) {
            if (m != this && m.isMini() && m.getVariant() == this.getVariant()) m.copyBuffsFrom(this);
        }
    }

    /** 给迷你分身装武器(取不到就保持空手, 不会崩) */
    public void equipMiniWeapon(int index) {
        net.farmingtales.ftboss.boss.ai.YouziCloneWeapons.equipRandom(this);
    }

    /** 按变体把武器装到**主手**(渲染由 ItemInHandLayer 负责, 会跟着手臂挥砍动作走) */
    public void equipWeapon() {
        try {
            ItemStack previousOffhand = this.getOffhandItem();
            if (new ResourceLocation(DUOLA_MELEE_ID).equals(ForgeRegistries.ITEMS.getKey(previousOffhand.getItem()))) {
                this.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
            if (previousOffhand.hasTag()
                    && previousOffhand.getTag().getInt("CustomModelData") == LEMONCAT_WEAPON_MODEL_DATA
                    && new net.minecraft.resources.ResourceLocation(LEMONCAT_OFFHAND_ID).equals(
                            net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(previousOffhand.getItem()))) {
                this.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
            String id = WEAPONS[this.getVariant()];
            if (this.getVariant() == DORA_VARIANT) id = DORA_SMG_ID;
            if (this.getVariant() == 6) {
                id = "ftboss:wenxuanlan_flywheel";
            }
            if (id == null || id.isEmpty()) {
                this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                return;
            }
            var item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(id));
            ItemStack st = item == null ? ItemStack.EMPTY : new ItemStack(item);
            /* 2026-09-12 v50: 武器也满附魔 8 级 + 无法破坏(护甲早就有了, 武器一直裸着) */
            if (!st.isEmpty()) {
                for (Enchantment e : buildEnchants(WEAPON_ENCH_IDS)) {
                    if (e != null) st.enchant(e, WEAPON_ENCH_LEVEL);
                }
                st.getOrCreateTag().putBoolean("Unbreakable", true);
                if (this.getVariant() == DANDAN_VARIANT && st.is(Items.END_ROD)) {
                    st.getOrCreateTag().putInt("CustomModelData", DANDAN_WEAPON_MODEL_DATA);
                }
                if (this.getVariant() == LEMONCAT_VARIANT) {
                    st.getOrCreateTag().putInt("CustomModelData", LEMONCAT_WEAPON_MODEL_DATA);
                }
            }
            this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, st);
            this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0f);
            /* 2026-09-13 v76 雪黎: **副手擀面杖**(近战那套"粉碎轮"伤害的来头) */
            if (this.getVariant() == XUELI_VARIANT || this.getVariant() == LEMONCAT_VARIANT
                    || this.getVariant() == DORA_VARIANT) {
                String offhandId = this.getVariant() == DORA_VARIANT ? DUOLA_MELEE_ID
                        : this.getVariant() == LEMONCAT_VARIANT ? LEMONCAT_OFFHAND_ID : XUELI_MELEE_ID;
                var pin = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        new net.minecraft.resources.ResourceLocation(offhandId));
                ItemStack off = pin == null ? ItemStack.EMPTY : new ItemStack(pin);
                if (!off.isEmpty()) {
                    for (Enchantment e : buildEnchants(WEAPON_ENCH_IDS)) {
                        if (e != null) off.enchant(e, WEAPON_ENCH_LEVEL);
                    }
                    off.getOrCreateTag().putBoolean("Unbreakable", true);
                    if (this.getVariant() == LEMONCAT_VARIANT) {
                        off.getOrCreateTag().putInt("CustomModelData", LEMONCAT_WEAPON_MODEL_DATA);
                    }
                }
                this.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, off);
                this.setDropChance(net.minecraft.world.entity.EquipmentSlot.OFFHAND, 0.0f);
            }
            if (this.getVariant() == LEMONCAT_VARIANT) {
                FTBossMod.LOGGER.info("[FTBoss][LemonCat] weapon models CMD={} main={} off={}",
                        LEMONCAT_WEAPON_MODEL_DATA, this.getMainHandItem(), this.getOffhandItem());
            }
            /* 诊断(2026-09-12): 把"到底装上了什么"打进日志 —— 若客户端仍看不到武器, 一眼就能分清是
               "服务端没装上" 还是 "客户端渲染层没生效" */
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} 装备武器: 目标={} 实际主手={} 附魔 lv{} 共 {} 条, 无法破坏=true",
                    NAMES[this.getVariant()], id, this.getMainHandItem(),
                    WEAPON_ENCH_LEVEL, this.getMainHandItem().getEnchantmentTags().size());
        } catch (Throwable t) {
            this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 装载武器失败: {}", t.toString());
        }
    }

    /* ==================== 护甲(2026-09-12 v13 用户定案) ====================
       「焰魔」= 灾变(Cataclysm)的 Ignis; 它的护甲就是**腾炎套**(ignitium)。
       要求: 整套 4 件 + 每件**满附魔 8 级** + **无法破坏(Unbreakable)**。
       外观: EasterBossRenderer 里挂了原版 HumanoidArmorLayer, Forge 会通过
       IClientItemExtensions 取到灾变自己的护甲模型与贴图 → 显示的就是腾炎甲 ✓ */
    public static final String[] ARMOR_IDS = {
            "cataclysm:ignitium_helmet",
            /* 2026-09-12 v31 按用户说明: **焰魔甲(腾炎套)本身就能飞**, 所以不用换成"鞘翅胸甲",
               整套腾炎甲原样穿(8 级满附魔 + 无法破坏), 飞行交给 NoGravity 实现。 */
            "cataclysm:ignitium_chestplate",
            "cataclysm:ignitium_leggings",
            "cataclysm:ignitium_boots",
    };
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    /** 2026-09-12 v35 按用户要求: **功底(variant 3)改穿亚当甲**(armamentarium Adamantine), 附魔/无法破坏完全一样 */
    public static final String[] ARMOR_IDS_ADAMANTINE = {
            "armamentarium:adamantine_helmet",
            "armamentarium:adamantine_chestplate",
            "armamentarium:adamantine_leggings",
            "armamentarium:adamantine_boots",
    };

    /** 该变体穿哪一套甲 */
    private String[] armorFor() {
        return this.getVariant() == 3 ? ARMOR_IDS_ADAMANTINE : ARMOR_IDS;
    }
    /** 满附魔等级: 用户要求 8 级(原版上限 4, 这里直接写 8) */
    public static final int ARMOR_ENCH_LEVEL = 8;

    /** 每件护甲适用的全部附魔(不含诅咒)。用**注册 id** 取, 不依赖映射字段名(Enchantments.X 在这套
     *  official 映射下取不到, 而且将来换版本也不用改) */
    private static Enchantment[] enchantsFor(EquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return buildEnchants("protection", "fire_protection", "blast_protection",
                        "projectile_protection", "thorns", "respiration", "aqua_affinity",
                        "unbreaking", "mending");
            case CHEST:
                return buildEnchants("protection", "fire_protection", "blast_protection",
                        "projectile_protection", "thorns", "unbreaking", "mending");
            case LEGS:
                return buildEnchants("protection", "fire_protection", "blast_protection",
                        "projectile_protection", "thorns", "swift_sneak", "unbreaking", "mending");
            case FEET:
                return buildEnchants("protection", "fire_protection", "blast_protection",
                        "projectile_protection", "thorns", "feather_falling", "depth_strider",
                        "frost_walker", "soul_speed", "unbreaking", "mending");
            default:
                return new Enchantment[0];
        }
    }

    private static Enchantment[] buildEnchants(String... ids) {
        java.util.List<Enchantment> list = new java.util.ArrayList<>();
        for (String id : ids) {
            Enchantment e = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(id));
            if (e != null) list.add(e);
            else FTBossMod.LOGGER.warn("[FTBoss][Easter] 找不到附魔 {} (跳过)", id);
        }
        return list.toArray(new Enchantment[0]);
    }

    /** 穿上整套腾炎甲: 每件满附魔 8 级 + Unbreakable; 掉落率设 0(不给玩家白嫖这套神装) */
    public void equipArmor() {
        this.equipArmor(true);
    }

    /** @param log 是否打日志 —— 每 10 秒的兜底重穿用 false, 免得刷屏(v53) */
    private void equipArmor(boolean log) {
        int ok = 0;
        String[] ids = this.armorFor();
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            EquipmentSlot slot = ARMOR_SLOTS[i];
            ItemStack st = ItemStack.EMPTY;
            try {
                var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ids[i]));
                if (item != null && item != Items.AIR) {
                    st = new ItemStack(item);
                    for (Enchantment e : enchantsFor(slot)) {
                        if (e != null) st.enchant(e, ARMOR_ENCH_LEVEL);
                    }
                    st.getOrCreateTag().putBoolean("Unbreakable", true);
                }
            } catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] 护甲装载失败 {}: {}", ids[i], t.toString());
            }
            this.setItemSlot(slot, st);
            this.setDropChance(slot, 0.0f);
            if (!st.isEmpty()) ok++;
        }
        if (log) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} 穿{}甲: {}/4 件, 附魔 lv{}, 无法破坏={}",
                    NAMES[this.getVariant()], this.getVariant() == 3 ? "亚当" : "腾炎",
                    ok, ARMOR_ENCH_LEVEL, ok > 0);
        }
    }

    /* ==================== 防御阶段「附魔金苹果」(2026-09-12 v50 用户定案) ====================
       用户原话:「提高boss的防御, 可以参考沙耶的防御, 比如每减少25%使用附魔金苹果和给予自己6个buff(二级)」。
       由 {@code ManorFight} 在共享血条跨过 75% / 50% / 25% 时调用。 */
    /** 6 个二级增益的持续时间 */
    public static final int APPLE_BUFF_TICKS = 60 * 20;
    /** v51 独立血条: 自己每掉到这些比例就吃一次附魔金苹果(每个阶段只发一次) */
    public static final double[] APPLE_STAGES = {0.75, 0.50, 0.25};
    /** v51 保底伤害: 单次至少打掉自身最大血量的 1.5%(1000 血 -> 15) */
    public static final double MIN_DAMAGE_PERCENT = 0.015;
    /** v52 单次伤害上限: 最多打掉自身最大血量的 10%(1000 血 -> 100), 防一刀秒 */
    public static final double MAX_DAMAGE_PERCENT = 0.10;
    /** 已经发过几次附魔金苹果(0~3) */
    private int appleStage = 0;

    /** v51: 按**自己的血量比例**发附魔金苹果(独立血条模式下由 hurt() 调用; 每个阶段一次) */
    private void tickAppleStages() {
        if (this.isMini()) return;
        /* 2026-09-13 v72: 麻雀**不吃金苹果** —— 她走的是同一套 manorHurt(为了"一定能打死"),
           但金苹果的伤害吸收 IV + 再生会让一个支援位变得打不动, 与用户要求相反。 */
        if (this.getVariant() == SPARROW_VARIANT) return;
        double ratio = this.getHealth() / Math.max(1.0f, this.getMaxHealth());
        while (this.appleStage < APPLE_STAGES.length && ratio <= APPLE_STAGES[this.appleStage]) {
            this.appleStage++;
            this.eatEnchantedGoldenApple();
        }
    }

    /** 吃一个附魔金苹果(伤害吸收 IV 2 分钟 + 生命恢复 II 5 秒) + 6 个二级增益 */
    public void eatEnchantedGoldenApple() {
        if (this.level().isClientSide) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        /* 原版附魔金苹果的效果 */
        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2 * 60 * 20, 3, false, true, true));
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 5 * 20, 1, false, true, true));
        /* 6 个二级增益(速度/抗性提升/生命恢复/急迫/跳跃提升/力量 —— 和常驻那套一致, 二级) */
        StringBuilder sb = new StringBuilder();
        for (var eff : INNATE_EFFECTS) {
            this.addEffect(new MobEffectInstance(eff, APPLE_BUFF_TICKS, 1, false, true, true));
            sb.append(eff.getDisplayName().getString()).append(' ');
        }
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.4, this.getZ(),
                30, 0.5, 0.7, 0.5, 0.08);
        sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.8, this.getZ(),
                8, 0.4, 0.4, 0.4, 0.03);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.4f, 0.9f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_BURP, SoundSource.HOSTILE, 1.2f, 1.0f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} 吃下附魔金苹果: 伤害吸收 IV 2 分钟 + 生命恢复 II 5 秒 + 6 个二级增益: {}",
                NAMES[this.getVariant()], sb.toString().trim());
    }

    private void refreshExtraBones() {
        boolean[] e = EXTRA[this.getVariant()];
        for (int i = 0; i < 4; i++) this.extraBones[i] = e[i];
    }

    /** 模型用的附加骨骼开关 */
    public boolean[] getExtraBones() {
        if (this.tickCount % 20 == 0) this.refreshExtraBones();
        return this.extraBones;
    }

    /* ---------------- 模型挂钩(玩家动作兼容层) ---------------- */
    public int getActionTicks() {
        CompoundTag animation = this.entityData.get(DATA_COMBAT_ANIMATION);
        return animation.getBoolean("cast") ? 0 : Math.max(0,
                animation.getInt("duration") - (int) this.getCombatAnimationAge(0.0f));
    }

    public float getActionProgress() {
        return this.getActionProgress(0.0f);
    }

    public float getActionProgress(float partialTick) {
        CompoundTag animation = this.entityData.get(DATA_COMBAT_ANIMATION);
        return net.farmingtales.ftboss.util.EasterCombatMath.progress(this.getCombatAnimationAge(partialTick),
                animation.getInt("duration"), animation.getBoolean("impact"));
    }

    public float getCombatAnimationAge(float partialTick) {
        return (float) (this.level().getGameTime() - this.entityData.get(DATA_COMBAT_ANIMATION).getLong("start")) + partialTick;
    }

    public float getCombatAnimationWeight(float partialTick) {
        if (!this.isAlive()) return 0.0f;
        CompoundTag animation = this.entityData.get(DATA_COMBAT_ANIMATION);
        if (animation.getBoolean("cast")) return net.farmingtales.ftboss.util.EasterCombatMath.castWeight(
                this.getCombatAnimationAge(partialTick), animation.getInt("duration"));
        return net.farmingtales.ftboss.util.EasterCombatMath.weight(this.getCombatAnimationAge(partialTick),
                animation.getInt("duration"), animation.getBoolean("impact"));
    }

    public int getWeaponStyle() { return STYLES[this.getVariant()]; }

    public boolean isDoraReloading() { return this.entityData.get(DATA_DORA_RELOADING); }

    public int getDoraSpearTicks() { return this.entityData.get(DATA_DORA_SPEAR_TICKS); }

    public boolean isCastingLike() {
        CompoundTag animation = this.entityData.get(DATA_COMBAT_ANIMATION);
        return animation.getBoolean("cast") && this.getCombatAnimationAge(0.0f) < animation.getInt("duration");
    }

    public int getCastStyle() { return this.entityData.get(DATA_COMBAT_ANIMATION).getInt("style"); }

    private void syncCombatAnimation(int ticks, boolean casting, int style, boolean impact) {
        if (this.level().isClientSide) return;
        CompoundTag animation = new CompoundTag();
        animation.putLong("start", this.level().getGameTime());
        animation.putInt("duration", Math.max(1, ticks));
        animation.putBoolean("cast", casting);
        animation.putInt("style", style);
        animation.putBoolean("impact", impact);
        this.entityData.set(DATA_COMBAT_ANIMATION, animation);
    }

    /** 播一次武器动作(帧数 20 ≈ 1 秒) */
    public void playAction(int ticks) {
        this.actionTicks = Math.max(1, ticks);
        this.castTicks = 0;
        this.syncCombatAnimation(ticks, false, this.getWeaponStyle(), false);
    }

    private void playImpactAction(int ticks) {
        this.playAction(ticks);
        this.syncCombatAnimation(ticks, false, this.getWeaponStyle(), true);
    }

    public int getMeleeAttackInterval() {
        int override = net.farmingtales.ftboss.util.ManorBossCombatRules.meleeInterval(this.getVariant());
        int interval = this.allowsParallelMelee() ? QingyiCombatRules.MELEE_INTERVAL
                : override > 0 ? override : net.farmingtales.ftboss.util.ManorMeleeTiming.DEFAULT_INTERVAL;
        return net.farmingtales.ftboss.boss.ai.XueliPhases.attackInterval(
                Math.max(1, this.getChallengeDifficulty().acceleratedTicks(interval)), this);
    }

    private void playMeleeImpactAction() {
        CompoundTag animation = this.entityData.get(DATA_COMBAT_ANIMATION);
        if (!net.farmingtales.ftboss.util.ManorMeleeTiming.canRestart(this.actionTicks, this.castTicks,
                this.isCastingLike(), animation.getBoolean("melee"))) return;
        this.playImpactAction(net.farmingtales.ftboss.util.ManorMeleeTiming.recoveryTicks(
                this.getMeleeAttackInterval(), this.isImpossibleChallenge()));
        animation = this.entityData.get(DATA_COMBAT_ANIMATION).copy();
        animation.putBoolean("melee", true);
        this.entityData.set(DATA_COMBAT_ANIMATION, animation);
    }

    /** 播一次施法姿势 */
    public void playCast(int ticks, int style) {
        this.castTicks = Math.max(1, ticks);
        this.actionTicks = 0;
        this.syncCombatAnimation(ticks, true, style, false);
    }

    public CompoundTag getSkillCue() {
        return this.entityData.get(DATA_SKILL_CUE);
    }

    public void setQizaiCue(CompoundTag cue) {
        if (this.getVariant() == QIZAI_VARIANT) this.entityData.set(DATA_SKILL_CUE, cue);
    }
    public CompoundTag getQizaiArsenalCue() { return this.entityData.get(DATA_QIZAI_ARSENAL); }
    public void setQizaiArsenalCue(CompoundTag cue) { this.entityData.set(DATA_QIZAI_ARSENAL, cue); }
    public boolean castQizaiArsenal(Vec3 point) {
        return this.level() instanceof ServerLevel level && this.qizaiArsenal.cast(level, point);
    }
    public boolean isQizaiProtectedCasting() { return net.farmingtales.ftboss.balance.ManorBalanceHooks.crossProtected(this.qizaiMagic != null && this.qizaiMagic.protectedCast(), this); }
    public boolean isQizaiUninterruptibleCasting() { return this.qizaiMagic != null && this.qizaiMagic.uninterruptible(); }

    public void playParallelSpell(int duration) {
        if (this.skillCommitted() || this.actionTicks > 0 || this.castTicks > 0) return;
        this.syncCombatAnimation(duration, true, this.getWeaponStyle(), false);
        CompoundTag animation = this.entityData.get(DATA_COMBAT_ANIMATION).copy();
        animation.putBoolean("parallelSpell", true);
        this.entityData.set(DATA_COMBAT_ANIMATION, animation);
    }

    public void finishParallelSpell() {
        if (this.entityData.get(DATA_COMBAT_ANIMATION).getBoolean("parallelSpell")) {
            this.entityData.set(DATA_COMBAT_ANIMATION, new CompoundTag());
        }
    }

    public boolean isGongdiBloodRageFull() {
        return this.bloodRage >= BLOOD_RAGE_MAX;
    }

    public void onGongdiAbilityHit(ServerLevel level, Player player, float dealt) {
        this.bloodRageOnHit(player, dealt);
        if (!player.isAlive()) return;
        net.farmingtales.ftboss.boss.ArmorShred.addStack(player, level.getGameTime());
        this.addBloodRage(level, player);
    }

    public float gongdiMeleeDamage(LivingEntity target) {
        return GongdiCombatRules.meleeDamage(this.getAttributeValue(Attributes.ATTACK_DAMAGE),
                EnchantmentHelper.getDamageBonus(this.getMainHandItem(), target.getMobType()));
    }

    public void finishSignatureCleave(int outerHits) {
        this.applyCleaveAtkBoost();
        this.heal(GongdiCombatRules.outerHeal(this.getMaxHealth() - this.getHealth(), outerHits));
        if (outerHits >= 2) this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 480, 1));
    }

    public void onSignatureCleaveHit(ServerLevel level, Player player, float dealt) {
        this.gongdiSkills.onAbilityHit(level, player, dealt);
    }

    public float manorSkillDamage(LivingEntity target, float baseDamage, double multiplier) {
        if (this.isMini() || !ManorFiveCombatRules.hasExpandedSkills(this.getVariant()) || target == null) return baseDamage;
        float dealt = ManorSkillDamageRules.damage(this.getAttributeValue(Attributes.ATTACK_DAMAGE),
                EnchantmentHelper.getDamageBonus(this.getMainHandItem(), target.getMobType()), baseDamage, multiplier);
        /* v159 按用户要求「柠檬获取到 boss 技能后，原 boss 技能伤害 ×5」：
           柠檬猫献祭继承期间**召来的助战 Boss**本身就是"继承来的能力"，
           它们自己的技能伤害也要放大；别的场合（真身自己当场上 Boss）保持原样。 */
        return net.farmingtales.ftboss.boss.ai.LemonInheritedAbilityRules.allySkillDamage(this, dealt);
    }

    public void beginGongdiCast(String skill, int ticks, double radius, Vec3 direction) {
        this.playCast(ticks, 3);
        this.beginSkillCue(this.position(), radius, ticks, true);
        CompoundTag cue = this.getSkillCue().copy();
        cue.putString("gongdiSkill", skill);
        cue.putDouble("directionX", direction.x);
        cue.putDouble("directionZ", direction.z);
        this.entityData.set(DATA_SKILL_CUE, cue);
    }

    public void finishGongdiCast() {
        this.playImpactAction(18);
        this.releaseSkillCue(this.position());
    }

    public void resetGongdiComboCooldown() {
        this.skillCd = Math.min(this.skillCd, GongdiCombatRules.EXECUTE_RESET);
    }

    public void beginManorFiveCast(int duration, int windup, Vec3 origin, double radius, boolean circleCue) {
        this.playCast(duration, this.getWeaponStyle());
        this.entityData.set(DATA_SKILL_CUE, new CompoundTag());
        if (circleCue) this.beginSkillCue(origin, radius, windup, false);
        if (!this.castsWithoutStopping()) this.getNavigation().stop();
    }

    public void finishManorFiveCast(Vec3 origin) {
        this.playImpactAction(12);
        this.releaseSkillCue(origin);
    }

    public void cancelManorFiveCast() {
        this.actionTicks = 0;
        this.castTicks = 0;
        this.entityData.set(DATA_SKILL_CUE, new CompoundTag());
        this.entityData.set(DATA_COMBAT_ANIMATION, new CompoundTag());
    }

    private void beginSkillCue(Vec3 origin, double radius, int ticks, boolean followsBoss) {
        CompoundTag cue = new CompoundTag();
        cue.putLong("start", this.level().getGameTime());
        cue.putInt("duration", ticks);
        cue.putDouble("x", origin.x);
        cue.putDouble("y", origin.y);
        cue.putDouble("z", origin.z);
        cue.putDouble("radius", radius);
        cue.putBoolean("follow", followsBoss);
        this.entityData.set(DATA_SKILL_CUE, cue);
    }

    private void releaseSkillCue(Vec3 origin) {
        CompoundTag cue = this.getSkillCue().copy();
        if (cue.isEmpty()) return;
        cue.putLong("start", this.level().getGameTime());
        cue.putInt("duration", cue.contains("gongdiSkill") ? 18 : CUE_RELEASE_TICKS);
        cue.putDouble("x", origin.x);
        cue.putDouble("y", origin.y);
        cue.putDouble("z", origin.z);
        cue.putBoolean("follow", false);
        cue.putBoolean("released", true);
        this.entityData.set(DATA_SKILL_CUE, cue);
    }

    private void syncBeamCue(Vec3 origin, Vec3 end, double radius, int duration, boolean firing, boolean released) {
        CompoundTag previous = this.getSkillCue();
        boolean changedPhase = !previous.getBoolean("beam") || previous.getBoolean("firing") != firing
                || previous.getBoolean("released") != released;
        if (!changedPhase && !released && this.tickCount % 2 != 0) return;
        CompoundTag cue = new CompoundTag();
        cue.putLong("start", changedPhase || released ? this.level().getGameTime() : previous.getLong("start"));
        cue.putInt("duration", duration);
        cue.putDouble("x", origin.x);
        cue.putDouble("y", origin.y);
        cue.putDouble("z", origin.z);
        cue.putDouble("endX", end.x);
        cue.putDouble("endY", end.y);
        cue.putDouble("endZ", end.z);
        cue.putDouble("radius", radius);
        cue.putBoolean("beam", true);
        cue.putBoolean("firing", firing);
        cue.putBoolean("released", released);
        this.entityData.set(DATA_SKILL_CUE, cue);
    }

    private boolean skillCommitted() {
        return this.qizaiMagic.busy() || this.isXuanlanSkyCasting() || this.doraSniper.busy() || this.manorFiveSkills.isCasting() || this.dandanRush > 0
                || this.qingyiConfluence.busy()
                || this.getVariant() == 5 && (this.riptideTicks > 0 || this.potionDelay > 0 || this.qingyiRain > 0)
                || this.gongdiSkills.isCasting() || this.cleaveWind > 0 || this.panWind > 0 || this.poisonWind > 0 || this.xueliPanWind > 0
                || this.shadowStrikeWind > 0 || this.xuanlanBeamBusy() || this.xueliSlamWarn > 0;
    }

    private boolean combatPlayer(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private Vec3 clipSkillRay(Vec3 from, Vec3 to) {
        return this.level().clip(new net.minecraft.world.level.ClipContext(from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getLocation();
    }

    private void tickSkillWindups(ServerLevel level) {
        this.qizaiMagic.tick(level);
        if (this.skillCommitted() && !this.castsWithoutStopping()) this.getNavigation().stop();
        this.qingyiConfluence.tick(level);
        this.manorFiveSkills.tick(level);
        if (this.getVariant() == 3) this.gongdiSkills.tick(level);
        if (this.cleaveWind > 0 && --this.cleaveWind == 0) this.executeCleave();
        if (this.panWind > 0 && --this.panWind == 0) this.executePanFlip();
        if (this.poisonWind > 0 && --this.poisonWind == 0) this.executePoisonBurst(level);
        if (this.xueliPanWind > 0 && --this.xueliPanWind == 0) this.executeXueliPanSlap(level);
        if (this.shadowStrikeWind > 0 && --this.shadowStrikeWind == 0) this.executeShadowStrike(level);
        CompoundTag cue = this.getSkillCue();
        if (!cue.isEmpty() && cue.getBoolean("released")
                && level.getGameTime() - cue.getLong("start") > cue.getInt("duration")) {
            this.entityData.set(DATA_SKILL_CUE, new CompoundTag());
        }
    }

    private void cancelSkillWindups() {
        /* 软清理可以撤掉其它技能，但不能把承诺施放仍在运行的祁崽 cue/动作先抹掉。
           真正的死亡/换形态/硬转场都会先 forceCancel，因此不会被这里错误保留。 */
        boolean preserveQizaiCast = this.qizaiMagic.uninterruptible();
        this.qizaiMagic.cancel();
        this.qizaiArsenal.cancel();
        this.xuanlanSky.cancel();
        this.doraSpearLeft = 0;
        this.doraSpearGap = 0;
        this.doraSpearTarget = null;
        this.entityData.set(DATA_DORA_SPEAR_TICKS, 0);
        this.qingyiConfluence.cancel();
        this.cancelDoraSniper();
        this.manorFiveSkills.cancel();
        this.qingyiRain = 0;
        this.riptideTicks = 0;
        this.potionDelay = 0;
        this.gongdiSkills.reset();
        this.cleaveWind = 0;
        this.panWind = 0;
        this.poisonWind = 0;
        this.xueliPanWind = 0;
        this.shadowStrikeWind = 0;
        this.shadowStrikeTarget = null;
        this.xuanlanBeamLock = 0;
        this.xuanlanBeamCharge = 0;
        this.xuanlanBeamFire = 0;
        this.xlBeamPunished.clear();
        if (this.xueliSlamWarn > 0 || this.xueliSlamLeft > 0) this.setNoGravity(false);
        this.xueliSlamWarn = 0;
        this.xueliSlamLeft = 0;
        this.xueliSlamGap = 0;
        if (!preserveQizaiCast && !this.getSkillCue().isEmpty()) this.entityData.set(DATA_SKILL_CUE, new CompoundTag());
        if (!preserveQizaiCast && !this.entityData.get(DATA_COMBAT_ANIMATION).isEmpty()) {
            this.entityData.set(DATA_COMBAT_ANIMATION, new CompoundTag());
            this.actionTicks = 0;
            this.castTicks = 0;
        }
    }

    public void prepareArenaTransfer() {
        this.qizaiMagic.forceCancel();
        this.cancelSkillWindups();
        this.dandanRush = 0;
        this.xueliRollTicks = 0;
        this.stunTicks.clear();
        this.stunAnchor.clear();
        this.stunLaunch.clear();
        this.crushTicks.clear();
        this.setNoGravity(false);
    }

    public boolean isPerformingSkill() {
        return this.skillCommitted() || this.isNoAi();
    }

    public boolean allowsParallelMelee() {
        return this.getVariant() == 5 && !this.isMini() && !this.isGiant();
    }

    /**
     * ★ 2026-09-24 按用户要求「提高 boss 在水中移速」。
     *
     * <p>原版水阻尼是 {@code 0.8}（只有"海豚的恩惠"才给 {@code 0.96}），所以 Boss 一落水就明显变慢、
     * 追不上人。这里把它抬到 {@link net.farmingtales.ftboss.boss.ai.BossWalkingSpeed#WATER_SLOW_DOWN}
     * （0.96，与海豚的恩惠同档）；配合那边的水中速度修饰符（+45%）与上限放宽，
     * 落水后的节奏基本跟陆地一致。想关掉就改那一个常量。
     */
    @Override
    protected float getWaterSlowDown() {
        return net.farmingtales.ftboss.boss.ai.BossWalkingSpeed.WATER_SLOW_DOWN;
    }

    public boolean castsWithoutStopping() {
        return !this.movementLocked();
    }

    private boolean movementLocked() {
        return this.qizaiMagic.busy() || this.isXuanlanSkyCasting() || this.doraSniper.busy() || this.xuanlanBeamBusy()
                || this.dandanRush > 0 || this.riptideTicks > 0 || this.reloadTicks > 0
                || this.xueliRollTicks > 0 || this.xueliSlamWarn > 0 || this.xueliSlamLeft > 0;
    }

    /**
     * v176 受击反馈的**水平击退力度**(格/tick 量级)。
     * 0.45 是"看得见、但不会把 Boss 推离战场"的量级 —— 配合
     * {@link net.farmingtales.ftboss.util.BossKnockback#BOSS_RESISTANCE}(0.5) 实际位移更小。
     * 想更"重"就往上调, 想只保留粒子/音效就设 0。
     */
    public static final double HIT_FEEDBACK_KNOCKBACK = 0.45;

    private boolean blocksMelee() {
        if (this.getVariant() == ZHEERGEN_VARIANT && !this.isMini() && !this.isGiant()) return false;
        return !this.allowsParallelMelee() && (this.movementLocked()
                || this.gongdiSkills.isCasting() || this.cleaveWind > 0 || this.panWind > 0
                || this.xueliPanWind > 0 || this.shadowStrikeWind > 0);
    }

    private void tickSharedSkillCooldown() {
        if (this.level().isClientSide || this.isMini() || !this.isAlive() || this.getVariant() == DORA_VARIANT) return;
        long now = this.level().getGameTime();
        if (this.sharedSkillClockInitialized && this.sharedSkillClockTick == now) return;
        this.sharedSkillClockTick = now;
        this.sharedSkillClockInitialized = true;
        this.skillCd = net.farmingtales.ftboss.boss.ai.XueliPhases.remainingSkillCooldown(Math.max(0, this.skillCd - 1), this);
    }

    private void expireCombatPoseTimers() {
        CompoundTag animation = this.entityData.get(DATA_COMBAT_ANIMATION);
        if (!net.farmingtales.ftboss.util.ManorCombatClock.poseExpired(animation.contains("start"),
                animation.getLong("start"), animation.getInt("duration"), this.level().getGameTime())) return;
        this.actionTicks = 0;
        this.castTicks = 0;
    }

    private boolean canScheduleSharedSkill() {
        return !this.skillCommitted() && !this.movementLocked()
                && (this.getVariant() != DORA_VARIANT || this.castTicks <= 0);
    }

    /* ---- 2026-09-12 v32: "柚子在場"门槛 ---- */

    /** 柚子(非分身)最近一次 tick 的游戏刻 */
    private static long lastYouziTick = -10000L;

    /** 柚子是否在場(最近 1 秒内 tick 过就算在场) —— 滑翔/破方块/连锁增益/麻雀支援都要它 */
    public boolean youziPresent() {
        if (!(this.level() instanceof ServerLevel sl)) return false;
        return sl.getGameTime() - lastYouziTick <= 20L;
    }

    /** 给事件总线侧(麻雀出没判定)用的静态版本 */
    public static boolean youziPresent(net.minecraft.server.MinecraftServer server) {
        if (server == null) return false;
        return server.overworld().getGameTime() - lastYouziTick <= 20L;
    }

    /** 2026-09-12 v35: 48 格内有没有麻雀(连锁增益的触发条件) */
    private boolean sparrowNearby() {
        for (EasterBossEntity e : this.level().getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(48.0))) {
            if (e != this && e.isAlive() && e.getVariant() == SPARROW_VARIANT) return true;
        }
        return false;
    }

    /**
     * 2026-09-12 v54 按用户要求: 「麻雀在场时全部 Boss 的 buff 等级提高一层」。
     * 用法: 所有给 Boss 自己/同伴上的增益, 等级都用 {@code buffAmp(基准等级)} 算。
     * (常驻增益每 5 秒重挂一次, 所以麻雀来了/走了最多 5 秒就体现出来。)
     */
    /** ★ 2026-09-14: sparrowNearby() 是**实体扫描**, 而 buffAmp() 有 10 处调用 ——
        加 10 tick 缓存(麻雀来了/走了最多 0.5 秒体现, 反正常驻增益本来就 5 秒重挂一次)。 */
    private int sparrowCacheAt = -9999;
    private boolean sparrowCacheVal = false;
    private boolean sparrowCached() {
        if (this.tickCount - this.sparrowCacheAt >= 10) {
            this.sparrowCacheVal = this.sparrowNearby();
            this.sparrowCacheAt = this.tickCount;
        }
        return this.sparrowCacheVal;
    }

    /* ★ 2026-09-14: 限伤上下限只在**最大血量变化**时重算(原来每次伤害两遍乘法) */
    private float clampHpFor = -1.0f, clampLo = 0.0f, clampHi = 0.0f;
    private void refreshClampBounds() {
        float hp = this.getMaxHealth();
        if (hp != this.clampHpFor) {
            this.clampHpFor = hp;
            this.clampLo = (float) (hp * MIN_DAMAGE_PERCENT);
            this.clampHi = (float) (hp * MAX_DAMAGE_PERCENT);
        }
    }

    public int buffAmp(int base) {
        return base + (this.sparrowCached() ? 1 : 0);
    }

    /** 2026-09-12 v44: 把入场演出那只麻雀正式转成"场地支援位"(登记 + 关掉演出用的无敌/站定) */
    public static void supportSparrow(ServerLevel sl, EasterBossEntity sparrow) {
        if (sparrow == null) return;
        sparrow.setInvulnerable(false);
        sparrow.setNoAi(false);
        sparrow.setCustomName(Component.literal("§e麻雀"));
        sparrow.setCustomNameVisible(true);
        sparrow.setPersistenceRequired();
        var hp = sparrow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        float health = net.farmingtales.ftboss.boss.ImpossibleSparrowSupport.health(sparrow);
        if (hp != null) hp.setBaseValue(health);
        sparrow.setHealth(health);
        net.farmingtales.ftboss.boss.SparrowSupport.noteSparrow(sparrow);
        sl.sendParticles(ParticleTypes.CLOUD, sparrow.getX(), sparrow.getY() + 1.0, sparrow.getZ(),
                30, 0.5, 0.6, 0.5, 0.06);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀转为支援位: variant={} 名字={} 血量={}",
                sparrow.getVariant(), sparrow.getName().getString(),
                (int) health);
    }

    /* ---------------- 战斗 ---------------- */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.blocksMelee()) return false;
        if (this.getVariant() == 5 && !this.qingyiConfluence.busy() && !this.isCastingLike()) {
            QingyiConfluenceTridentItem.animate(this, "attack");
        }
        /* 2026-09-12 v60 按用户要求:
           ① **去掉玩家的无敌帧**(先清零再走原版结算) —— 否则 Boss 的连击只算第一下;
           ② 近战命中把玩家**击退**, 然后**立刻加速追上去**;
           ③ 蛋蛋(variant 1)命中还会**抽掉玩家一个增益**。 */
        if (!this.level().isClientSide && target instanceof Player pPre) pPre.invulnerableTime = 0;
        /* v71: 记下命中前的血量, 用来算"这一次实际造成多少伤害"(血怒吸血/真实伤害按实伤算) */
        float healthBefore = (target instanceof LivingEntity leB) ? leB.getHealth() : 0.0f;
        boolean hit = super.doHurtTarget(target);
        if (hit && !this.level().isClientSide && target instanceof LivingEntity victim) {
            this.lemonSacrifice.onMeleeHit(victim, Math.max(0, healthBefore - victim.getHealth()));
        }
        /* v71 功底血怒: 按"实际造成的伤害"结算吸血 + 2% 真实伤害(见 bloodRageOnHit) */
        if (hit && !this.level().isClientSide && this.getVariant() == 3 && target instanceof LivingEntity leHit) {
            this.bloodRageOnHit(leHit, Math.max(0.0f, healthBefore - leHit.getHealth()));
        }
        if (hit && !this.level().isClientSide && target instanceof Player p) {
            if (this.getVariant() == DORA_VARIANT) {
                this.doraKnockback(p, SPEAR_KNOCKBACK, SPEAR_KNOCKBACK_UP);
                this.doraHit(p);
            } else this.knockbackPlayer(p);
            this.chaseAfter(p);
            if (this.getVariant() == 1) this.stripOneBuff(p);
            if (this.getVariant() == 1) this.dandanRampAttack();   // v119: 蛋蛋物理攻击逐渐提高(上限 200)
            /* 2026-09-13 v76 雪黎: 擀面杖命中 -> 挂上"机械动力粉碎轮"持续伤害(3/ tick, 共 60 tick) */
            if (this.getVariant() == XUELI_VARIANT) this.startCrush(p);
            /* v105: 雪黎**近战自带细雪**(挂霜 + 叠满转冻结伤害) */
            if (this.getVariant() == XUELI_VARIANT) applyXueliFrost(p);
        }
        if (hit) this.playMeleeImpactAction();
        if (hit && !this.level().isClientSide && target instanceof LivingEntity le) this.onWeaponHit(le);
        return hit;
    }

    /** v60 按用户要求: 近战命中把玩家**击退**(水平推开 + 一点上抛) */
    private void knockbackPlayer(Player p) {
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
        boolean empowered = !this.isMini() && ManorFiveCombatRules.hasExpandedSkills(this.getVariant());
        double strength = empowered ? ManorFiveCombatRules.MELEE_PUSH : 1.0;
        double lift = empowered ? ManorFiveCombatRules.MELEE_LIFT : 0.42;
        net.farmingtales.ftboss.util.BossKnockback.push(this, p, dx / len * strength, lift, dz / len * strength);
        p.hurtMarked = true;                      // 让客户端接受这次位移
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.HOSTILE, 1.0f, 0.9f);
        }
    }

    /** v60 按用户要求: 击退之后**立刻加速追击**(速度 III 1.5 秒 + 立刻朝玩家迈步) */
    private void chaseAfter(Player p) {
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 2, false, false, false));
        this.setTarget(p);
        this.getLookControl().setLookAt(p, 60.0f, 60.0f);
    }

    /** v60 蛋蛋: 命中玩家时**抽掉他一个随机增益**(只抽正面效果)。
     *  ★ 2026-09-16: 抽的是 {@link #isNoStripBuff} —— 它比复制用的白名单多排除一个「血怒」，
     *  所以蛋蛋/紫菜抽不走黑切给玩家的血怒（但 Boss 依旧能"复制"到血怒）。 */
    private void stripOneBuff(Player p) {
        java.util.List<MobEffectInstance> buffs = new java.util.ArrayList<>();
        for (MobEffectInstance mi : p.getActiveEffects()) {
            if (mi.getEffect() != null && mi.getEffect().isBeneficial() && !isNoStripBuff(mi)) buffs.add(mi);
        }
        if (buffs.isEmpty()) return;
        MobEffectInstance pick = buffs.get(this.random.nextInt(buffs.size()));
        p.removeEffect(pick.getEffect());
        /* v182: 原文案较长且每次抽增益都发 —— 长文进日志, 行动栏只留一句短的 */
        net.farmingtales.ftboss.boss.BossText.flavorWithHint(p, "末影扰动",
                "你被抽掉了一个增益：" + pick.getEffect().getDisplayName().getString(),
                "§5【末影扰动】§7失去 §c" + pick.getEffect().getDisplayName().getString());
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1.2, p.getZ(),
                    18, 0.35, 0.5, 0.35, 0.05);
            sl.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.4f);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋 抽掉玩家 {} 的增益: {}",
                p.getName().getString(), pick.getEffect().getDisplayName().getString());
    }

    /**
     * 【武器命中效果】每位 Boss 一条招牌(2026-09-12 起逐位填; 高难档: 减益 II 级 / 10 秒起)。
     *
     * <p>0 柚子(武器=缩小版自己): 缓慢 II + 虚弱 II 10 秒, 且每 3 次命中召 2 个迷你分身。
     * <p>1 蛋蛋(武器=末地烛): 末影扰动 —— 飘浮 I 4 秒 + 失明 II 8 秒。
     * <p>2 朵拉(武器=Bren 机枪): 破甲压制 —— 虚弱 II + 挖掘疲劳 II 各 10 秒。
     * <p>3 功底(武器=黑色切割者): 裂伤 —— 凋零 II + 虚弱 II 各 8 秒。
     * <p>4 柠檬猫(武器=煎锅/锅铲): 热油 —— 点燃 5 秒 + 缓慢 II 8 秒。
     * <p>5 青衣(四件 TnS 武器轮换): 按当前那一件给不同减益(见 {@link #QINGYI_WEAPONS})。
     * <p>6 文玄蓝(飞轮+阀门): 齿轮绞伤 —— 缓慢 III 5 秒 + 反胃 5 秒。
     * <p>7 折耳根(暗影剑+铁魔法): 暗影侵蚀 —— 凋零 II 8 秒 + 失明 II 5 秒。
     * <p>8 紫菜(河豚棒): 河豚毒 —— 中毒 II 8 秒 + 缓慢 II 6 秒。
     */
    private void onWeaponHit(LivingEntity target) {
        switch (this.getVariant()) {
            case 6: {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 2));
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.GRINDSTONE_USE, SoundSource.HOSTILE, 1.0f, 0.9f);
                break;
            }
            case 7: {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 8 * 20, 1));
                /* 2026-09-12 v54 按用户要求「允许折耳根对玩家造成失明」: 这里从反胃改回**失明 II 5 秒**
                   v115: 时长补上 SKILL_MULT(和同类技能一致; 原来只有 5 秒, 比文档短 2/3)
                   v116 按用户要求「折耳根现在会对玩家造成黑暗效果」: 再**并存**一层黑暗(不再二选一) */
                net.farmingtales.ftboss.effect.ShadowBlindEffect.applyTo(target, SHADOW_DARKNESS_ON_HIT);
                /* v116 按用户要求「同时会对玩家持续造成虚空伤害」: 打中玩家就开「暗影侵蚀」 */
                if (target instanceof ServerPlayer spVoid) {
                    this.applyShadowVoid(spVoid, target.level() instanceof ServerLevel slVoid ? slVoid : null);
                }
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 0.9f, 1.4f);
                if (target.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1.0, target.getZ(),
                            16, 0.3, 0.4, 0.3, 0.04);
                }
                break;
            }
            case 8: {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 8 * 20, 1));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6 * 20, 1));
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.HOSTILE, 1.0f, 0.8f);
                if (target.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 1.0, target.getZ(),
                            14, 0.3, 0.4, 0.3, 0.06);
                }
                /* ★★★ 2026-09-24 按用户要求「减少紫菜单次攻击给予的 debuff，改成一次攻击 2 个」★★★
                   这里原来还会调 {@code applyPufferDebuffs}(从减益池再随机抽 2 种、同种还会 +1 级),
                   于是一次平砍最多挂 4 条 —— 现在**只保留河豚毒那两条固定减益**
                   (中毒 II 8 秒 + 缓慢 II 6 秒), 也就是"一次攻击正好 2 个 debuff"。
                   减益池那套仍留给招牌技能「毒雾爆发」(它自带长前摇与范围预警)。 */
                break;
            }
            case 5: {
                QingyiConfluenceSkill.mark(target, 8 * 20);
                switch (this.qingyiIndex) {
                    case 0:
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8 * 20, 1));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 8 * 20, 1));
                        break;
                    case 1:
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 8 * 20, 1));
                        break;
                    case 2:
                        /* v37: 原失明 -> 挖掘疲劳(不允许失明) */
                        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 8 * 20, 1));
                        /* v119 按用户要求「朵拉命中玩家增加流血效果」 */
                        net.farmingtales.ftboss.boss.ai.ExtraDebuffs.bleed(target, 8 * 20, 1);
                        break;
                    case 3:
                        /* v37: 原漂浮 -> 发光(不允许漂浮) */
                        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 8 * 20, 0));
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8 * 20, 2));
                        break;
                    default:
                        break;
                }
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 1.0f, 0.8f + this.qingyiIndex * 0.15f);
                if (target.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(),
                            12, 0.3, 0.4, 0.3, 0.15);
                }
                break;
            }
            case 4: {
                target.setSecondsOnFire(5);
                /* v119: 柠檬的火焰同时挂"蓝火"(独立结算的虚空伤害) */
                net.farmingtales.ftboss.boss.ai.ExtraDebuffs.blueFire(target, 5 * 20);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8 * 20, 1));
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.1f, 1.2f);
                if (target.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(),
                            14, 0.3, 0.4, 0.3, 0.03);
                }
                break;
            }
            case 3: {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 8 * 20, 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 8 * 20, 1));
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 0.9f, 0.7f);
                /* 2026-09-12 v24 按用户要求: 破甲(玩家) + 血怒(自己) */
                if (this.level() instanceof ServerLevel sl3 && target instanceof Player pl) {
                    int st = net.farmingtales.ftboss.boss.ArmorShred.addStack(pl, sl3.getGameTime());
                    sl3.sendParticles(ParticleTypes.DAMAGE_INDICATOR, pl.getX(), pl.getY() + 1.0, pl.getZ(),
                            6, 0.25, 0.3, 0.25, 0.02);
                    pl.displayClientMessage(Component.literal(String.format(
                            "§c护甲侵蚀 %d/%d §7(%d 护甲)§8 · 10 秒",
                            st, net.farmingtales.ftboss.boss.ArmorShred.MAX_STACKS,
                            (int) (net.farmingtales.ftboss.boss.ArmorShred.PER_STACK * st))), true);
                    if (st >= net.farmingtales.ftboss.boss.ArmorShred.MAX_STACKS) {
                        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BLOOD_RAGE_TICKS, 1));
                        sl3.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.1f, 1.3f);
                        FTBossMod.LOGGER.info("[FTBoss][Easter] 功底 破甲叠满 {} 层 -> 自身速度 II 8 秒", st);
                    }
                    this.addBloodRage(sl3, pl);
                    this.gongdiSkills.onHit(pl);
                }
                break;
            }
            case 2: {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 20, 1));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10 * 20, 1));
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 0.8f, 1.3f);
                break;
            }
            case 1: {
                /* v37 按用户要求: 去掉漂浮/失明 -> 改成反胃 + 饥饿(和它的换位技能一致) */
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 8 * 20, 1));
                target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.9f, 1.45f);
                if (target.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0, target.getZ(),
                            22, 0.4, 0.6, 0.4, 0.25);
                }
                break;
            }
            case 0: {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 20, 1));
                target.level().playSound(null, target.getX(), target.getY() + 1.0, target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 0.7f, 0.6f);
                this.youziAddHit(target, "武器命中");
                break;
            }
            default:
                break;
        }
    }

    private void summonMiniClones(ServerLevel sl) {
        int players = net.farmingtales.ftboss.youzi.YouziSummonScaling.playerCount(sl, this);
        int summonCap = net.farmingtales.ftboss.util.YouziSummonRules.cap(players);
        int summonCount = net.farmingtales.ftboss.util.YouziSummonRules.count(players);
        int alive = 0;
        for (EasterBossEntity e : sl.getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(40.0))) {
            if (e.isMini() && e.isAlive() && e.getVariant() == this.getVariant()) alive++;
        }
        if (alive >= summonCap) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10 * 20, 1));
            FTBossMod.LOGGER.info("[FTBoss][Easter] 柚子分身已达上限 {} 个 -> 改为自身速度 II", alive);
            return;
        }
        int made = 0;
        for (int i = 0; i < summonCount && alive + made < summonCap; i++) {
            EasterBossEntity mini = FTEntities.EASTER_BOSS.get().create(sl);
            if (mini == null) break;
            double ang = this.random.nextDouble() * Math.PI * 2.0;
            double x = this.getX() + Math.cos(ang) * 2.6;
            double z = this.getZ() + Math.sin(ang) * 2.6;
            mini.moveTo(x, this.getY(), z, this.random.nextFloat() * 360.0f, 0.0f);
            mini.setVariant(this.getVariant());
            mini.setMini(true);
            /* 2026-09-12 v55 按用户要求: 分身不再拿"缩小版自己(石像)", 换成这三把武器轮着来
               —— 斩龙剑 / 冰斧 / 荣耀金斧(★ 2026-09-24 去掉血王子焰型剑, 见 MINI_WEAPONS) */
            mini.equipMiniWeapon(this.random.nextInt(MINI_WEAPONS.length));
            mini.copyBuffsFrom(this);          // v128: 分身一出生就带上本体的增益
            mini.setTarget(this.getTarget());
            mini.setManorSummonerId(this.manorSummonerId);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, mini);
            sl.addFreshEntity(mini);
            // 出生特效 + 音效: 一眼看清"分裂出来了"
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, x, this.getY() + 0.9, z,
                    20, 0.35, 0.6, 0.35, 0.05);
            made++;
        }
        if (made > 0) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.HOSTILE, 1.1f, 1.5f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 柚子「自我叠影」召出 {} 个迷你分身(第 {} 次命中触发)",
                    made, this.youziHits);
        }
    }

    /**
     * 2026-09-12: 若已被「甜苹果庄园」战场托管(HiddenBossIntro 之后), 伤害**转交共享血条**,
     * 自身不掉血也不会被打死 —— 直到共享血条清空由战场统一结算。
     */
    /** 魔法伤害减免比例(2026-09-12 v46 按用户要求: **40%**) */
    public static final float MAGIC_DAMAGE_REDUCTION = 0.40f;

    /** 判断这是不是"魔法类"伤害: ①原版 witch_resistant_to(魔法/间接魔法/凋零/音爆);
     *  ②伤害类型 path 里含 magic / spell / sonic; ③msgId 含 magic / spell(铁魔法等 mod 的伤害) */
    private static boolean isMagicDamage(DamageSource src) {
        try {
            if (src.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)) return true;
            var key = src.typeHolder().unwrapKey();
            if (key.isPresent()) {
                String p = key.get().location().getPath().toLowerCase(java.util.Locale.ROOT);
                if (p.contains("magic") || p.contains("spell") || p.contains("sonic")) return true;
            }
            String msg = src.getMsgId();
            if (msg != null) {
                String m = msg.toLowerCase(java.util.Locale.ROOT);
                if (m.contains("magic") || m.contains("spell")) return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /** 是不是"掉出世界/虚空"伤害 */
    private static boolean isVoidDamage(DamageSource source) {
        try {
            return source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)
                    || "outOfWorld".equals(source.getMsgId());
        } catch (Throwable t) {
            return false;
        }
    }

    /** v148: 连续掉入虚空的次数(连续 = 间隔不超过 30 秒; 满 2 次就搬迁战场) */
    private int voidFalls = 0;
    private int voidSafeTicks = 0;
    private static final int VOID_STREAK_WINDOW = 30 * 20;

    /** 这次伤害是不是**真实玩家**打出来的(机械手/部署器的假人不算) */
    public static boolean isRealPlayerDamage(DamageSource source) {
        return realAttacker(source) != null;
    }

    public static ServerPlayer realAttacker(DamageSource source) {
        net.minecraft.world.entity.Entity attacker = source.getEntity();
        if (attacker == null) attacker = source.getDirectEntity();
        for (int depth = 0; attacker instanceof net.minecraft.world.entity.projectile.Projectile projectile && depth < 8; depth++) {
            net.minecraft.world.entity.Entity owner = projectile.getOwner();
            if (owner == null || owner == attacker) return null;
            attacker = owner;
        }
        return attacker instanceof ServerPlayer player && !(player instanceof net.minecraftforge.common.util.FakePlayer) ? player : null;
    }

    /** 掉入虚空 -> 立刻传送到玩家位置(优先正在交战的玩家; 没有玩家就抬到世界底部之上) */
    private void rescueFromVoid() {
        ManorFight.rescue(this);
    }

    private boolean blocksCombatDamage() {
        return net.farmingtales.ftboss.boss.HiddenBossIntro.isActing(this.getUUID())
                || this.isInvulnerable() && !ManorFight.isFinaleMember(this);
    }

    public boolean isXuanlanSkyCasting() { return this.xuanlanSky != null && this.xuanlanSky.active(); }

    public boolean isManorMaidShielded() { return this.xueliMaids.shielded(); }

    public boolean isXuanlanSkyHidden() { return this.entityData.get(DATA_XUANLAN_SKY_HIDDEN); }

    public void setXuanlanSkyHidden(boolean hidden) { this.entityData.set(DATA_XUANLAN_SKY_HIDDEN, hidden); }

    public boolean isManorIntroHidden() { return this.entityData.get(DATA_MANOR_INTRO_HIDDEN); }

    public void setManorIntroHidden(boolean hidden) { this.entityData.set(DATA_MANOR_INTRO_HIDDEN, hidden); }

    @Override
    public void heal(float amount) {
        if (this.isXuanlanSkyCasting()) return;
        if (this.getVariant() == LEMONCAT_VARIANT) amount *= LEMON_HEAL_MULTIPLIER;
        amount = net.farmingtales.ftboss.boss.ai.LemonCombatBalance.limitHealing(this, amount);
        float before = this.getHealth();
        super.heal(amount);
        net.farmingtales.ftboss.boss.ai.LemonCombatBalance.recordHealing(this, before);
    }

    @Override
    public void setHealth(float health) {
        if (this.isXuanlanSkyCasting() && health > this.getHealth()) return;
        super.setHealth(health);
        net.farmingtales.ftboss.boss.ai.ManorPhysicalDefense.healthChanged(this);
    }

    @Override
    public int getArmorValue() {
        int armor = net.farmingtales.ftboss.boss.ai.ManorPhysicalDefense.armorValue(this, super.getArmorValue());
        return net.farmingtales.ftboss.util.ManorArmorBalanceRules.armor(this.getVariant(), this.isMini(), this.isGiant(), armor);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean administrativeKill = source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL);
        if (!administrativeKill && this.isQizaiProtectedCasting()) return false;
        if (!administrativeKill && this.isXuanlanSkyCasting()) return false;
        if (!administrativeKill && this.blocksCombatDamage()) return false;
        if (!this.level().isClientSide) {
            if (!ManorCombatRules.allowsDamage(isVoidDamage(source), administrativeKill, isRealPlayerDamage(source))) return false;
            if (!administrativeKill && (!Float.isFinite(amount) || amount <= 0.0f)) return false;
        }
        /* ★ v114 按用户要求「boss 无视荆棘反伤」(按澄清: **只管庄园十人**, 其它 Boss 保持现状):
           荆棘反伤一律不算。**必须显式挡**的原因: 庄园主阶段走 manorHurt 的单次伤害
           clamp[MIN~MAX_DAMAGE_PERCENT](1000 血 -> 15~100 点), 而荆棘单次只有 3~9 点 ——
           会被"抬"到 15 点; Boss 一秒挥好几次, 玩家站着不动光靠荆棘就能把庄园 Boss 磨死。
           挡在这里 = 连伤害反馈都不给(和"流血/末日完全免疫"同一层)。 */
        if (!this.level().isClientSide && isThornsDamage(source)) return false;
        /* 2026-09-13 v104 按用户要求「雪黎召唤 1~3 批次女仆…未击杀全部女仆则无法对雪黎造成伤害」:
           护法还在 -> **完全免伤** (和沙耶的茶芽庇佑同一套)。
           ★ 绕过无敌的伤害(/kill、掉出世界)**不受护盾阻挡** —— 否则又会回到 v83 那个
             "卡血条、kill 不死"的老毛病。 */
        if (!this.level().isClientSide && this.getVariant() == XUELI_VARIANT && !this.isMini()
                && !administrativeKill
                && this.xueliMaids.shielded()) {
            this.xueliMaids.onBlocked(source);
            return false;
        }
        /* 2026-09-12 v46 按用户要求: **魔法伤害减免 40%** */
        if (!this.level().isClientSide && amount > 0.0f && isMagicDamage(source)) {
            amount *= (1.0f - MAGIC_DAMAGE_REDUCTION);
        }
        /* 2026-09-12 v27 按用户要求: **无视血王子焰形剑的流血**等真实伤害/流血类伤害。
           复用工程里已有的 BossDamageGate(它认得 armamentarium:hemorrhage 与各类 bleed/doom 伤害id)。 */
        if (!this.level().isClientSide && net.farmingtales.ftboss.boss.ai.BossDamageGate.isImmune(source)) {
            return false;
        }
        if (!this.level().isClientSide && this.isImpossibleChallenge() && !administrativeKill) {
            amount = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
            if (amount <= 0) return false;
        }
        /* 2026-09-12 v22: **挨打也累计「自我叠影」** —— 原来只有"柚子打中玩家"才计数,
           玩家自己去打它时永远看不到分身(用户反馈"分身似乎没实现")。 */
        if (!this.level().isClientSide) {
            ServerPlayer attacker = realAttacker(source);
            if (attacker != null) ManorFight.attackedBy(this, attacker);
        }
        if (!this.level().isClientSide && amount > 0.5f) this.youziAddHit(null, "被击中");
        /* 2026-09-12 v52 修「Boss 锁血」: v51 把庄园主阶段的伤害交给原版管线后出现锁血 ——
           满附魔 8 级护甲 + 抗性提升 + 伤害吸收把单次伤害压到 1 以下, 原版永远打不出"最后一刀",
           血量卡在 0.5 不动(日志证据: 蛋蛋连吃 3 个附魔金苹果 = 血量到过 25%, 但九位一个都没死)。
           现在主阶段**自己算伤害**(manorHurt), 一定打得死; 伤害始终由当前实体处理。 */
        if (!this.level().isClientSide && this.level() instanceof ServerLevel
                && (net.farmingtales.ftboss.boss.ManorFight.isManaged(this) || this.getVariant() == SPARROW_VARIANT)) {
            boolean manorHit = this.manorHurt(source, amount);
            if (manorHit && this.isImpossibleChallenge()) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
            if (manorHit) this.hitFeedback(source);
            return manorHit;
        }
        /* v96: 不在庄园里的彩蛋 Boss 也吃"麻雀分摊" */
        boolean hit = super.hurt(source, net.farmingtales.ftboss.boss.ai.ManorDefense.reduce(this, source, this.sparrowShare(amount, source)));
        if (hit && this.isImpossibleChallenge()) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
        if (hit) this.hitFeedback(source);
        return hit;
    }

    private void hitFeedback(DamageSource source) {
        net.farmingtales.ftboss.boss.ai.RangedBossTeleport.confirmedHit(this, source);
        net.farmingtales.ftboss.boss.ai.SparrowHitEvasion.confirmedHit(this, source);
        net.farmingtales.ftboss.boss.ai.ManorDefense.confirmedHit(this, source);
        if (this.level().isClientSide) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        /* ② 击退: 沿攻击者 -> 本体方向推开 */
        Entity attacker = source == null ? null : source.getEntity();
        if (attacker != null && attacker != this) {
            Vec3 away = this.position().subtract(attacker.position());
            if (away.lengthSqr() > 1.0E-4) {
                Vec3 dir = away.normalize();
                net.farmingtales.ftboss.util.BossKnockback.push(this, dir.x * HIT_FEEDBACK_KNOCKBACK, 0.12,
                        dir.z * HIT_FEEDBACK_KNOCKBACK);
                this.hurtMarked = true;
            }
        }
        /* ③ 红色伤害效果(原版受伤红闪) */
        this.animateHurt(1.0f);
    }

    /**
     * v52: 庄园主阶段的伤害结算(**不走原版护甲/抗性管线**)。
     *
     * <p>口径: 单次伤害 = clamp(原始伤害, 最大血量的 {@value #MIN_DAMAGE_PERCENT} ~ {@value #MAX_DAMAGE_PERCENT});
     * 先扣伤害吸收(附魔金苹果那个护盾照样有用), 再扣血; 扣到 0 就正常死亡。
     * 这样玩家一定能把血打空(不会锁血), 大威力武器也照样吃上限、不会一刀秒。
     */
    /** 荆棘反伤判定(v114): 原版荆棘的 msgId 就是 {@code thorns}; 别的 mod 的荆棘类反伤按关键字兜底 */
    private static boolean isThornsDamage(DamageSource source) {
        if (source == null) return false;
        String msg = source.getMsgId();
        return msg != null && msg.contains("thorns");
    }

    private boolean manorHurt(DamageSource source, float amount) {
        /* 2026-09-13 v83 修「卡血条 + kill 不死」:
           以前 `/kill`(genericKill, 带 BYPASSES_INVULNERABILITY) 也会被下面的
           "单次上限 10% + 无敌帧"吞掉 —— 连管理员手段都杀不死庄园 Boss,
           玩家看到的就是"血条卡住、kill 不死"。
           现在: **无视无敌的伤害(如 /kill、掉出世界)直接秒杀并正常走死亡流程**。 */
        if (source != null && source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
            this.setHealth(0.0f);
            this.die(source);
            this.discard();
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} 被无视无敌的伤害终结({}) -> 立即移除",
                    NAMES[this.getVariant()], source.getMsgId());
            return true;
        }
        /* 2026-09-12 v58: **已经死了就别再结算** —— 以前尸体还能被打, 每次都重跑一遍 die(),
           日志里出现"13 次/75 毫秒 的随主消散/延长"就是因为这个 */
        if (this.isDeadOrDying()) return false;
        if (this.invulnerableTime > 0) return false;                       // 原版无敌帧: 0.5 秒最多一刀
        net.farmingtales.ftboss.boss.ai.ManorPhysicalDefense.syncArmor(this);
        this.refreshClampBounds();
        float dmg = this.isImpossibleChallenge() ? Math.min(this.clampHi, amount)
                : Math.max(this.clampLo, Math.min(this.clampHi, amount));
        dmg = this.getDamageAfterArmorAbsorb(source, dmg);
        /* v96: 麻雀分摊 50%(在她活着且在附近时) —— 必须在下面扣血之前算 */
        dmg = this.sparrowShare(dmg, source);
        /* v117 按用户要求「柚子的召唤物会分摊她 10% 的受伤」 */
        dmg = this.youziMinionShare(dmg, source);
        /* v102 按用户要求「加强青衣的防御力」: 庄园主阶段绕过原版护甲直接扣血,
           所以青衣的减伤必须在这里做(护甲值在庄园里是无效的)。 */
        if (this.getVariant() == 5 && !this.isMini()) {
            dmg = (float) (dmg * QINGYI_DAMAGE_TAKEN_MULT);
        }
        /* 伤害吸收先顶(附魔金苹果 IV = 16 点) */
        dmg = net.farmingtales.ftboss.boss.ai.ManorAdaptiveShield.reduce(this, source, dmg);
        dmg = net.farmingtales.ftboss.boss.ai.ManorDefense.reduce(this, source, dmg);
        float abs = this.getAbsorptionAmount();
        float absorbed = Math.min(Math.max(0, abs), dmg);
        if (abs > 0.0f) {
            float use = Math.min(abs, dmg);
            this.setAbsorptionAmount(abs - use);
            dmg -= use;
        }
        this.invulnerableTime = 10;
        this.hurtTime = 10;
        this.hurtDuration = 10;
        dmg = net.farmingtales.ftboss.boss.ai.ManorPhysicalDefense.limit(this, source, dmg);
        if (dmg > 0.0f) {
            net.farmingtales.ftboss.boss.ai.ManorRaidCombat.beforeHealthWrite(this, this.getHealth() - dmg, source);
            this.setHealth(this.getHealth() - dmg);
        }
        net.farmingtales.ftboss.boss.ai.BossHitFeedback.confirmed(this, source, dmg + absorbed, dmg <= 0 && absorbed > 0);
        this.tickAppleStages();
        if (this.getHealth() <= 0.0f) {
            this.setHealth(0.0f);
            this.die(source);
            /* 2026-09-12 v59 修「Boss 假死 + 又进入无敌 + 提前召唤」:
               原版死亡是"血量<=0 起算 20 tick 死亡动画, 到点才移除"; 这 1 秒里只要有人把它奶回 >0
               (麻雀每 2 秒奶 1% 正好会撞上), 原版的 tickDeath() 就**不再被调用** → 死亡计时冻结、
               **永不移除**; 而 dead=true 已经让 hurt() 拒伤(无敌), 技能却继续放(假死)。
               所以这里**立刻移除** —— 不给"被奶回来"的窗口。 */
            this.discard();
        }
        return true;
    }

    /** 流血类**效果**也一律免疫(传奇怪物 / TnS 的 bleeding, 以及任何 id 里带 bleed/hemorrhage 的) */
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (net.farmingtales.ftboss.boss.ai.ManorStatusImmunity.blocks(this, effect.getEffect())) return false;
        if (this.lemonSacrifice != null && this.lemonSacrifice.active() && !effect.getEffect().isBeneficial()) return false;
        var eff = effect.getEffect();
        if (eff != null) {
            var key = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(eff);
            if (key != null) {
                String path = key.getPath().toLowerCase(java.util.Locale.ROOT);
                if (path.contains("bleed") || path.contains("hemorrhage") || path.contains("blood_loss")) {
                    return false;
                }
            }
        }
        return super.canBeAffected(effect);
    }

    /**
     * 2026-09-12 v29 按用户要求: **自身获得增益时** -> 额外获得 2 个不同的随机增益,
     * 并给 32 格内最近玩家挂 1 个随机减益。
     *
     * <p>由 {@code EasterBuffChain}(监听 {@code MobEffectEvent.Added})调用 —— 原版 addEffect 是 final, 覆盖不了。
     * 有 {@value #CHAIN_BUFF_CD} tick 冷却 + 递归保护(常驻增益每 5 秒刷一次, 正好触发一次连锁)。
     */
    public void onBuffGainedHook(MobEffectInstance effect) {
        if (effect == null || effect.getEffect() == null) return;
        /* 2026-09-12 v52 按用户要求「麻雀的增益不需要提示」: 麻雀喂的增益一律安静处理 */
        if (this.quietBuffs) return;
        if (this.chainGuard || this.level().isClientSide) return;
        if (!effect.getEffect().isBeneficial()) return;
        /* 2026-09-12 v35 按用户要求: "拿到增益再甩玩家减益"这套是**麻雀**的功能 —— 48 格内有麻雀才触发 */
        if (!this.sparrowNearby()) return;
        if (this.tickCount - this.lastChainBuff < CHAIN_BUFF_CD) return;
        this.lastChainBuff = this.tickCount;
        this.onBuffGained();
    }

    private void onBuffGained() {
        this.chainGuard = true;
        try {
            int a = this.random.nextInt(CHAIN_BUFF_POOL.length);
            int b = a;
            for (int i = 0; i < 8 && b == a; i++) b = this.random.nextInt(CHAIN_BUFF_POOL.length);
            /* v54: 连锁增益这两个也吃"麻雀在场 +1 级" */
            this.addEffect(new MobEffectInstance(CHAIN_BUFF_POOL[a], 15 * 20, this.buffAmp(1), true, false, false));
            this.addEffect(new MobEffectInstance(CHAIN_BUFF_POOL[b], 15 * 20, this.buffAmp(1), true, false, false));
            Player p = this.nearestPlayerForBuff();
            String dbg = "(附近无玩家)";
            if (p != null) {
                var de = RANDOM_DEBUFF_POOL[this.random.nextInt(RANDOM_DEBUFF_POOL.length)];
                p.addEffect(new MobEffectInstance(de, 10 * 20, 1));
                dbg = de.getDisplayName().getString();
                /* 2026-09-12 v53 按用户要求「麻雀的增益不需要提示」: 这条 action bar 提示删掉
                   (麻雀在场时每几秒就弹一次, 太吵), 减益本身照旧挂在玩家身上, 日志仍保留 */
            }
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.2, this.getZ(),
                        12, 0.4, 0.5, 0.4, 0.15);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.HOSTILE, 0.8f, 1.5f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} 连锁增益: 额外 +{} / +{} ; 给玩家 {} 挂上 {}",
                    NAMES[this.getVariant()], CHAIN_BUFF_POOL[a].getDisplayName().getString(),
                    CHAIN_BUFF_POOL[b].getDisplayName().getString(),
                    p == null ? "(无)" : p.getName().getString(), dbg);
        } finally {
            this.chainGuard = false;
        }
    }

    /** 2026-09-12 v52: 在**该实体**身上安静地上增益(麻雀支援用) —— 期间不触发"连锁增益"提示与玩家减益 */
    public void runQuietBuffs(Runnable action) {
        this.quietBuffs = true;
        try {
            action.run();
        } finally {
            this.quietBuffs = false;
        }
    }

    private Player nearestPlayerForBuff() {
        Player best = null;
        double bd = 32.0 * 32.0;
        for (Player p : this.level().players()) {
            if (!p.isAlive()) continue;
            double d = p.distanceToSqr(this);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }


    /* ==================== v30: 麻雀(支援位) ==================== */

    /* 2026-09-12 v52 按用户反馈"Boss 锁血"调低: 每 3 秒 2% -> 每 4 秒 1%。
       2026-09-12 v54 按用户要求「麻雀对 boss 的回血速度翻倍」: 4 秒 -> 2 秒(1%/2 秒 = 5 HP/s)。
       2026-09-13 v87 按用户反馈降频(庄园日志里她每 1~2 秒刷一条, 且把队友奶得太硬): 2 秒 -> **3 秒**。
       注意只改**节奏**: 每次仍是 1% 最大生命 + 3 个随机增益, 数值没动。 */
    public static final int SPARROW_HEAL_TICKS = 3 * 20;        // 每 3 秒
    public static final int SPARROW_CLEANSE_TICKS = 5 * 20;     // 每 5 秒
    /* ==================== 麻雀 v96 三项新增(2026-09-13 用户要求) ====================
       用户原话:「麻雀会共享 boss 的 buff 和分摊 50% 的伤害, 同时每次获取玩家 buff 时
       10% 概率禁止玩家 30s 内回复血量」。 */
    /** 麻雀替被支援的 Boss 吃掉的伤害比例(50%) */
    public static final float SPARROW_DAMAGE_SHARE = 0.50f;
    /** 麻雀"每次获取玩家增益"时，禁止那名玩家回血的概率(10%) */
    public static final float SPARROW_NOHEAL_CHANCE = 0.10f;
    /** 麻雀与 Boss 共享增益——找 Boss / 给 Boss 的频率(tick) */
    public static final int SPARROW_SHARE_TICKS = 20;
    /** 2026-10-18：取消对庄园 Boss 的直接百分比回复，仅保留原增益支援周期。 */
    public static final double SPARROW_HEAL_PERCENT = 0.0;
    public static final int SPARROW_BUFFS = 3;                  // 每次 3 个随机增益
    public static final int SPARROW_LIFE_TICKS = 90 * 20;       // 待 90 秒就走
    /* 2026-09-13 v72 按用户要求「麻雀会优先传送自己到友军旁边」:
       以前她只会**把玩家抓过来**(15 秒抓人)和**末影珍珠追玩家**, 现在改成:
       只要离被支援的友军超过 {@value #SPARROW_TELEPORT_RANGE} 格, 就**把自己传送**到友军身边
       (每 {@value #SPARROW_TELEPORT_CD} tick 最多一次), 不再去抓玩家。 */
    public static final double SPARROW_TELEPORT_RANGE = 10.0;
    public static final int SPARROW_TELEPORT_CD = 3 * 20;
    private long lastSparrowTeleport = -10000L;

    /** 麻雀：每 3 秒给予 3 个随机增益，每 5 秒净化另一位 Boss 的减益，不直接百分比回血。 */
    private void sparrowTick() {
        if (net.farmingtales.ftboss.boss.ai.XuanlanEncounterLimits.stopSupport(this)) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.bossBar.setVisible(false);
        /* 2026-09-12 v50 按用户要求「麻雀不要飞」: 以前 NoGravity + 自己推 delta, 悬停在被支援者头顶 4 格;
           现在取消无重力/滑翔, 老老实实用寻路在**地面**跟着走 */
        this.setNoGravity(false);
        if (this.isFallFlying()) this.setSharedFlag(7, false);
        if (this.getTarget() != null) this.setTarget(null);      // 2026-09-12 v35: 麻雀**不攻击玩家**
        if (!this.isImpossibleChallenge() && ++this.sparrowLife > SPARROW_LIFE_TICKS) {
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PARROT_AMBIENT, SoundSource.HOSTILE, 1.0f, 1.6f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀支援结束, 飞走了");
            this.discard();
            return;
        }
        EasterBossEntity boss = net.farmingtales.ftboss.boss.ai.SparrowHitEvasion.prepare(this.supportedBoss(), this);
        boss = net.farmingtales.ftboss.boss.ai.XuanlanEncounterLimits.supportTarget(boss);
        net.farmingtales.ftboss.boss.ai.SparrowDisruption.tick(this, boss);
        if (boss == null) return;
        net.farmingtales.ftboss.boss.ai.SparrowDisruption.tick(this, boss);
        net.farmingtales.ftboss.integ.spells.SparrowBorrowedSpells.tick(this, boss);
        double dist = this.distanceTo(boss);
        if (net.farmingtales.ftboss.boss.ai.SparrowHitEvasion.evadeSupport(this.impossibleKiting, this)) {
            this.getLookControl().setLookAt(boss, 30.0f, 30.0f);
        } else if (dist > 5.0) {
            this.getNavigation().moveTo(boss, 1.15);              // 地面跟过去
        } else {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(boss, 30.0f, 30.0f);
        }
        if (this.tickCount % SPARROW_HEAL_TICKS == 0) {
            if (!this.isCastingLike()) this.playCast(HiddenBossCastRules.quicker(24), 4);
            this.beginSkillCue(boss.position(), 2.0, CUE_RELEASE_TICKS, false);
            this.releaseSkillCue(boss.position());
            StringBuilder sb = new StringBuilder();
            /* 2026-09-12 v52 按用户要求「麻雀的增益不需要提示」: 3 个增益走"安静模式",
               不会再触发"连锁增益"那条 action bar 提示, 也不会顺手给玩家挂减益 */
            boss.runQuietBuffs(() -> {
                for (int i = 0; i < SPARROW_BUFFS; i++) {
                    var eff = CHAIN_BUFF_POOL[this.random.nextInt(CHAIN_BUFF_POOL.length)];
                    /* v54: 麻雀在场(就是它自己在场) -> 等级 +1 */
                    boss.addEffect(new MobEffectInstance(eff, 15 * 20, boss.buffAmp(1), true, false, false));
                    sb.append(eff.getDisplayName().getString()).append(' ');
                }
            });
            sl.sendParticles(ParticleTypes.HEART, boss.getX(), boss.getY() + 2.0, boss.getZ(), 8, 0.4, 0.4, 0.4, 0.03);
            sl.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.PARROT_AMBIENT, SoundSource.HOSTILE, 1.0f, 1.4f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀支援: {} 获得 {} 个随机增益（不再给予百分比治疗）: {}",
                    NAMES[boss.getVariant()], SPARROW_BUFFS, sb.toString().trim());
        }
        if (this.tickCount % SPARROW_CLEANSE_TICKS == 0) this.cleanseOtherBoss(sl, boss);
        /* v96 按用户要求「麻雀会共享 boss 的 buff」: 与它支援的那位**双向同步正面效果** */
        if (this.tickCount % SPARROW_SHARE_TICKS == 0) this.shareBuffsWith(boss);
    }

    /**
     * v96 按用户要求「麻雀会共享 boss 的 buff」: 双向共享**正面效果**。
     *
     * <p>两边都只在"对方那层更高、或者自己快到期了(剩余 ≤ 3 秒)"时才补, 所以是**幂等**的 ——
     * 每 20 tick 走一次也不会来回刷屏刷效果。给 Boss 的那一半走 {@code runQuietBuffs}
     * (否则每补一个增益都会触发"连锁增益"提示 + 顺手给玩家挂减益)。
     */
    private void shareBuffsWith(EasterBossEntity boss) {
        if (boss == null || !boss.isAlive() || boss.isDeadOrDying()) return;
        int toMe = 0;
        for (MobEffectInstance mi : new java.util.ArrayList<>(boss.getActiveEffects())) {
            if (mi.getEffect() == null || !mi.getEffect().isBeneficial()) continue;
            MobEffectInstance cur = this.getEffect(mi.getEffect());
            if (cur != null && cur.getAmplifier() >= mi.getAmplifier() && cur.getDuration() > 60) continue;
            this.addEffect(new MobEffectInstance(mi.getEffect(), 15 * 20, mi.getAmplifier(), true, false, false));
            toMe++;
        }
        final int[] toBoss = {0};
        boss.runQuietBuffs(() -> {
            for (MobEffectInstance mi : new java.util.ArrayList<>(this.getActiveEffects())) {
                if (mi.getEffect() == null || !mi.getEffect().isBeneficial()) continue;
                MobEffectInstance cur = boss.getEffect(mi.getEffect());
                if (cur != null && cur.getAmplifier() >= mi.getAmplifier() && cur.getDuration() > 60) continue;
                boss.addEffect(new MobEffectInstance(mi.getEffect(), 15 * 20, mi.getAmplifier(), true, false, false));
                toBoss[0]++;
            }
        });
        if (toMe > 0 || toBoss[0] > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀与 {} 共享增益: 麻雀得到 {} 个, {} 得到 {} 个",
                    NAMES[boss.getVariant()], toMe, NAMES[boss.getVariant()], toBoss[0]);
        }
    }

    /** 48 格内最近的、活着的麻雀(没有就返回 null) */
    private EasterBossEntity nearbySparrow() {
        EasterBossEntity best = null;
        double bd = 48.0 * 48.0;
        for (EasterBossEntity e : this.level().getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(48.0))) {
            if (e == this || e.getVariant() != SPARROW_VARIANT || !e.isAlive() || e.isDeadOrDying()) continue;
            double d = e.distanceToSqr(this);
            if (d < bd) { bd = d; best = e; }
        }
        return best;
    }

    /**
     * v96 按用户要求「麻雀分摊 50% 的伤害」: 麻雀在场时, 她替 Boss 吃掉一半。
     *
     * <p>直接扣她的血 —— **不走她自己的 hurt 管线**: 否则这笔伤害又会被庄园
     * "单次最多 10% 最大血量" 缩放一遍(500 血就是最多 50), 那就不叫分摊了。
     * 扣到 0 就正常走阵亡流程(她本来就有自己的 500 血和退场逻辑)。
     *
     * @return 分摊之后 Boss 实际该吃下的伤害
     */
    private float sparrowShare(float dmg, DamageSource source) {
        if (dmg <= 0.0f || this.getVariant() == SPARROW_VARIANT || this.isMini()) return dmg;
        if (!(this.level() instanceof ServerLevel sl)) return dmg;
        EasterBossEntity sp = this.nearbySparrow();
        if (sp == null || sp.blocksCombatDamage()) return dmg;
        float share = dmg * SPARROW_DAMAGE_SHARE;
        float rest = net.farmingtales.ftboss.boss.ai.ManorDamageLimit.sharedHealth(sp.getHealth(), share, sp, source);
        sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, sp.getX(), sp.getY() + 1.0, sp.getZ(),
                6, 0.3, 0.4, 0.3, 0.03);
        if (rest <= 0.0f) {
            /* ★ v112 修复: 先看**无敌标志**。入场演出(HiddenBossIntro)与终章(ManorFight)里的麻雀都是
               {@code setInvulnerable(true)} 的, 以前这里直接 setHealth(0)+die()+discard(),
               能把演出中/终章里的那只麻雀打死, 并把演出静默收掉。现在无敌时就留一口气、不死。 */
            if (sp.isInvulnerable()) {
                sp.setHealth(1.0f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀在无敌期(演出/终章)替 {} 分摊了 {} 点, 不死",
                        NAMES[this.getVariant()], String.format("%.1f", share));
                return dmg - share;
            }
            sp.setHealth(0.0f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀替 {} 分摊伤害时阵亡(这一下分摊了 {} 点)",
                    NAMES[this.getVariant()], String.format("%.1f", share));
            sp.die(source == null ? this.damageSources().generic() : source);
            sp.discard();
        } else {
            sp.setHealth(rest);
        }
        return dmg - share;
    }

    /* ==================== v117 柚子「分身分摊」(按用户要求) ==================== */

    /**
     * 柚子: 她的迷你分身替她分摊 {@value #YOUZI_MINION_SHARE} 的受伤。
     *
     * <p>分摊量**平分给所有活着的分身**(和麻雀那套一样: 直接扣血、不走它们的受伤管线,
     * 免得再吃一次庄园的 10% 缩放); 谁血不够谁就地阵亡。一个分身都没有时不生效。
     * 分身合计替本体承担 5% 伤害，不随分身数量叠加；清掉分身即可取消分摊。
     *
     * @return 分摊之后柚子本体实际该吃下的伤害
     */
    private float youziMinionShare(float dmg, DamageSource source) {
        if (dmg <= 0.0f || this.getVariant() != 0 || this.isMini()) return dmg;
        if (!(this.level() instanceof ServerLevel sl)) return dmg;
        java.util.List<EasterBossEntity> minis = new java.util.ArrayList<>();
        for (EasterBossEntity e : sl.getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(64.0))) {
            if (e == this || !e.isMini() || e.getVariant() != 0) continue;
            if (!e.isAlive() || e.isDeadOrDying() || e.blocksCombatDamage()) continue;
            minis.add(e);
        }
        if (minis.isEmpty()) return dmg;
        float share = dmg * YOUZI_MINION_SHARE;
        float per = share / minis.size();
        for (EasterBossEntity m : minis) {
            float rest = m.getHealth() - per;
            sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, m.getX(), m.getY() + 1.0, m.getZ(),
                    4, 0.3, 0.4, 0.3, 0.03);
            if (rest <= 0.0f) {
                if (m.isInvulnerable()) {
                    /* 演出/终章里的分身是无敌的: 留一口气, 别把演出静默收掉(和麻雀那套同理) */
                    m.setHealth(1.0f);
                } else {
                    m.setHealth(0.0f);
                    m.die(source == null ? this.damageSources().generic() : source);
                    m.discard();
                }
            } else {
                m.setHealth(rest);
            }
        }
        return dmg - share;
    }

    /** 被支援的 Boss = 32 格内最近的其它彩蛋 Boss(v50: **跳过迷你分身** —— 以前麻雀会去奶 150 血的小柚子) */
    private EasterBossEntity supportedBoss() {
        EasterBossEntity best = null;
        double bd = 32.0 * 32.0;
        for (EasterBossEntity e : this.level().getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(32.0))) {
            /* v59: 死/濒死的不奶 —— 以前会把"已经死了但还没移除"的 Boss 奶回 >0, 导致假死+无敌 */
            if (e == this || !e.isAlive() || e.isDeadOrDying() || e.isMini() || e.getVariant() == SPARROW_VARIANT) continue;
            double d = e.distanceToSqr(this);
            if (d < bd) { bd = d; best = e; }
        }
        return best;
    }

    /** 每 5 秒清掉"另一位" Boss 身上的减益(优先挑真的中了减益的那位) */
    private void cleanseOtherBoss(ServerLevel sl, EasterBossEntity primary) {
        EasterBossEntity target = null;
        for (EasterBossEntity e : sl.getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(32.0))) {
            if (e == this || !e.isAlive() || e.getVariant() == SPARROW_VARIANT) continue;
            if (e != primary && hasDebuff(e)) { target = e; break; }
        }
        if (target == null) target = primary;
        int n = 0;
        for (MobEffectInstance mi : new java.util.ArrayList<>(target.getActiveEffects())) {
            if (mi.getEffect() != null && !mi.getEffect().isBeneficial()) {
                target.removeEffect(mi.getEffect());
                n++;
            }
        }
        if (n > 0) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.5, target.getZ(),
                    20, 0.5, 0.6, 0.5, 0.05);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PARROT_AMBIENT, SoundSource.HOSTILE, 1.0f, 1.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀净化: 清掉 {} 的 {} 个减益", NAMES[target.getVariant()], n);
        }
    }

    private static boolean hasDebuff(LivingEntity e) {
        for (MobEffectInstance mi : e.getActiveEffects()) {
            if (mi.getEffect() != null && !mi.getEffect().isBeneficial()) return true;
        }
        return false;
    }

    /**
     * 柚子「自我叠影」计数(2026-09-12 v22): 命中玩家 / 自己被击中都会累计,
     * 满 {@link #YOUZI_HITS_PER_SUMMON} 次且过了 {@link #MINI_SUMMON_CD} 冷却就分裂 2 个。
     */
    private void youziAddHit(LivingEntity victim, String why) {
        if (this.getVariant() != 0 || this.isMini()) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.youziHits++;
        int rem = this.youziHits % YOUZI_HITS_PER_SUMMON;
        /* 2026-09-12 v53: 平时**不再打日志** —— v48 每轮 2 行, 一局仍是几百行; 现在只在
           "满了但冷却中"时每 5 秒记一行, 其余只看召唤那一行。 */
        if (rem != 0) return;
        int since = this.tickCount - this.lastMiniSummon;
        if (since < MINI_SUMMON_CD) {
            if (this.tickCount - this.lastYouziLog >= 100) {
                this.lastYouziLog = this.tickCount;
                FTBossMod.LOGGER.info("[FTBoss][Easter] 柚子叠影已满但冷却中({}/{} tick), 累计命中 {}",
                        since, MINI_SUMMON_CD, this.youziHits);
            }
            return;
        }
        this.lastMiniSummon = this.tickCount;
        if (victim != null) victim.hurt(this.damageSources().mobAttack(this), YOUZI_BURST_DAMAGE);
        this.summonMiniClones(sl);
    }

    @Override
    public void tick() {
        if (net.farmingtales.ftboss.turret.ZhuniStage.hold(this)) return;
        super.tick();
        if (this.level().isClientSide) return;
        this.tickSharedSkillCooldown();
        this.expireCombatPoseTimers();
        if (this.getVariant() == DORA_VARIANT) this.doraCombatTick();

        /* 2026-09-12 v59: **死/濒死就不再干活** —— 以前尸体还能被奶回血继续放技能(假死),
           这里直接停掉常驻增益/技能/召唤/净化, 只等死亡动画走完 or 被立刻移除。 */
        if (this.isDeadOrDying()) {
            this.qizaiMagic.forceCancel();
            this.cancelSkillWindups();
            return;
        }
        if (this.level() instanceof ServerLevel skyLevel && this.getVariant() == 6 && this.xuanlanSky.tick(skyLevel)) {
            this.bossBar.setProgress(Math.max(0f, Math.min(1f, this.getHealth() / this.getMaxHealth())));
            net.farmingtales.ftboss.sound.BossMusic.request(skyLevel, this);
            return;
        }
        if (this.isNoAi()) {
            if (this.level() instanceof ServerLevel combatLevel && this.qizaiMagic.uninterruptible()) {
                this.qizaiMagic.tick(combatLevel);
            } else {
                this.cancelSkillWindups();
            }
        } else if (this.level() instanceof ServerLevel combatLevel) this.tickSkillWindups(combatLevel);
        if (this.level() instanceof ServerLevel arsenalLevel) {
            this.qizaiArsenal.tick(arsenalLevel, this.qizaiMagic.skyCasting(), this.qizaiMagic.busy());
        }
        if (this.level() instanceof ServerLevel tridentLevel) this.tickQingyiFallingTrident(tridentLevel);

        /* 2026-09-12 v32: 记录"柚子在場"的时间戳(滑翔/破方块/连锁增益/麻雀支援的门槛) */
        if (this.getVariant() == 0 && !this.isMini() && this.isAlive()
                && this.level() instanceof ServerLevel slZ) {
            lastYouziTick = slZ.getGameTime();
        }

        /* 2026-09-12 v39 修"重进存档后 Boss 打不死":
           入场演出/终章火柴人都是**静态状态**, 退出存档再进来就丢了 —— 但 Boss 身上的
           "无敌 + 站定(setNoAi)" 是随存档保存的, 于是永远解不开。
           这里做看门狗: 处于 NoAi 且既不在演出、也不在战场托管里, 满 10 秒就自动解除, 恢复可击杀。 */
        boolean acting = net.farmingtales.ftboss.boss.HiddenBossIntro.isActing(this.getUUID());
        boolean finale = ManorFight.isFinaleMember(this);
        if (!this.isQizaiUninterruptibleCasting()
                && ManorCombatRules.shouldThaw(this.isNoAi(), this.isInvulnerable(), acting, finale)) {
            if (++this.stuckTicks > 200) {
                this.stuckTicks = 0;
                this.setInvulnerable(false);
                this.setNoAi(false);
                FTBossMod.LOGGER.info("[FTBoss][Easter] {} {} 清除过期冻结状态", NAMES[this.getVariant()], this.getUUID());
            }
        } else {
            this.stuckTicks = 0;
        }

        /* ---- 迷你分身(柚子自我叠影): 只保留限时消散, 不刷屏、不放技能、不驱动演出 ---- */
        if (this.isMini()) {
            if (this.bombFuse > 0 && this.level() instanceof ServerLevel bombLevel) this.bombFuseTick(bombLevel);
            if (this.isRemoved() || this.isDeadOrDying()) return;
            this.bossBar.setVisible(false);
            if (--this.miniLife <= 0) {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                            this.getX(), this.getY() + 0.6, this.getZ(), 18, 0.3, 0.5, 0.3, 0.08);
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            this.getX(), this.getY() + 0.8, this.getZ(), 8, 0.25, 0.4, 0.25, 0.05);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.HOSTILE, 0.8f, 1.7f);
                    FTBossMod.LOGGER.info("[FTBoss][Easter] {} 迷你分身 {} 消散 @{},{},{}",
                            NAMES[this.getVariant()], this.getUUID(), (int) this.getX(), (int) this.getY(), (int) this.getZ());
                }
                this.discard();
                return;
            }
            if (this.actionTicks > 0) this.actionTicks--;
            if (this.castTicks > 0) this.castTicks--;
            return;
        }

        if (this.tickCount == 1) { this.equipWeapon(); this.equipArmor(); }   // NBT 先写 variant 的情况兜底
        /* ★ 2026-09-24 折耳根残影：到点由**本体**复现一次「暗影突袭」的命中效果
           （残影实体自己不结算伤害；见 spawnShadowPhantom / firePhantomEcho）。 */
        if (this.phantomEchoTicks > 0 && --this.phantomEchoTicks == 0
                && this.level() instanceof ServerLevel echoLevel) {
            this.firePhantomEcho(echoLevel);
        }
        if (this.tickCount % 200 == 0) this.equipArmor(false);   // 每 10 秒兜底(不打日志, v53)
        /* 2026-09-13 v76 雪黎: 粉碎轮持续伤害每 tick 结算(即使她自己被打断/被冻住也照跑) */
        this.xueliCrushTick();
        /* 2026-09-14 v117: 蛋蛋「信标压制」(每 10 tick 摘一次玩家的信标加成) + 折耳根连击超时断连 */
        if (this.tickCount % DANDAN_BEACON_PERIOD == 0) this.dandanBeaconBan();
        this.shadowStreakTick();
        /* v124: 蛋蛋「虚空漩涡」(末影守卫那套) + 折耳根「失明/黑暗维持」 */
        if (this.level() instanceof ServerLevel slV124) {
            this.dandanVortexTick(slV124);
            net.farmingtales.ftboss.boss.ai.ShadowVoidSplit.maintainTick(this, slV124);
        }
        /* 2026-09-13 v85 修「庄园开打整场没音乐」: **彩蛋 Boss 现在也会请求 BGM** ——
           第一个请求的 Boss 决定曲目组(彩蛋系 -> {@code Group.MANOR_EPIC} = 新写的 20 分钟史诗战斗曲)。
           迷你分身不参与(否则柚子的分身会去抢音乐控制权)。 */
        if (!this.isMini() && this.level() instanceof ServerLevel slBgm) {
            net.farmingtales.ftboss.sound.BossMusic.request(slBgm, this);
        }
        /* 2026-09-13 v81 按用户要求: 离玩家太远就**主动寻找玩家**(追过去/闪过去);
           麻雀不追(她是支援位), 终章那九位站位由 ManorFight 管。
           注意: 这里 `frozen` 还没声明, 直接用 isNoAi() 判断(同一含义) */
        if (!this.isNoAi() && !this.skillCommitted() && this.getVariant() != SPARROW_VARIANT
                && this.xueliSlamWarn <= 0                       // v111: ⑦ 悬停蓄势期间别把她拽回地面
                && !net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)
                && this.level() instanceof ServerLevel slHunt) {
            this.huntStrayPlayer(slHunt);
        }
        /* ★ 2026-10-10 按用户要求「折耳根的传送追击效果较慢」：折耳根的**庄园内闪现追击** ——
           以前他只在技能起手那一瞬闪一次，玩家一跑就只能靠两条腿追，观感很慢。
           现在只要目标拉开 {@value #ZHEERGEN_BLINK_DIST} 格以上，
           每隔 {@value #ZHEERGEN_BLINK_COOLDOWN} tick 就闪到对方背后一次。 */
        if (!this.isNoAi() && this.getVariant() == ZHEERGEN_VARIANT && !this.isMini()
                && !net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)   // 与 huntStrayPlayer 同口径：终章站位由 ManorFight 管
                && this.level() instanceof ServerLevel slBlink) {
            this.zheergenBlinkChase(slBlink);
        }
        this.tickXueliDialogue();
        /* 2026-09-13 v78 雪黎: 擀面杖压路冲锋的推进 */
        if (this.xueliRollTicks > 0) this.xueliRollTick();
        /* 2026-09-13 v103 雪黎: 自爆分身的引信 */
        if (this.bombFuse > 0 && this.level() instanceof ServerLevel slBomb) this.bombFuseTick(slBomb);
        /* 2026-09-13 v104 雪黎: 女仆护法的护盾维持 + 缓慢回血(和沙耶 tick 茶芽庇佑同一位置) */
        if (this.getVariant() == XUELI_VARIANT && !this.isMini()) this.xueliMaids.tick();
        /* 2026-09-13 v111 雪黎 ⑦「天降瞬砸」: 悬停警戒 -> 砸落; 以及被砸晕玩家的每 tick 定身 */
        if (this.getVariant() == XUELI_VARIANT && this.level() instanceof ServerLevel slSlam) {
            this.xueliSlamTick(slSlam);
            this.xueliStunTick(slSlam);
            this.xueliCloneTick(slSlam);      // v118: 自爆分身独立冷却兜底
        }
        /* 2026-09-20: 眩晕的每 tick 硬定身不再只给雪黎跑 —— 文玄蓝「超能光束」用同一套
           (表为空时这个方法立刻返回, 其余变体没有额外开销) */
        if (this.level() instanceof ServerLevel slStun) this.xueliStunTick(slStun);
        /* 2026-09-12 v60: 「最后的 Boss」那九位**只站位** —— 不再同步玩家增益/净化自身/放技能,
           免得玩家看到"九位还活着", 误以为最终阶段进早了(位置由 ManorFight.finaleUpkeep 每 tick 对齐) */
        if (net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)) {
            if (!this.isNoAi()) this.setNoAi(true);
            return;
        }
        /* 2026-09-12 v23 按用户要求: 全员常驻增益(每 5 秒补一次, 相当于永久);
           2026-09-12 v27 按用户要求: **全部 II 级** —— 速度 II / 抗性 II / 生命恢复 II / 急迫 II / 跳跃提升 II / 力量 II */
        if (this.tickCount % INNATE_BUFF_TICKS == 0) {
            for (var eff : INNATE_EFFECTS) {
                /* 2026-09-12 v39: 文玄蓝不挂速度 II(移动过快会抽搐) */
                if (eff == MobEffects.MOVEMENT_SPEED && this.getVariant() == 6) continue;
                /* v54: 麻雀在场 -> 全部增益等级 +1 */
                this.addEffect(new MobEffectInstance(eff, INNATE_BUFF_DURATION, this.buffAmp(1), true, false, false));
            }
            /* 2026-09-12 v44 按用户反馈"功底的护甲套装 buff 还是没有":
               Armamentarium 的套装效果是 **armamentarium:adamantine_effect**(The Adamantine),
               由 MCreator 的 FullSetBonusTickProcedure **只对玩家**触发, 怪物永远吃不到 ——
               所以这里直接把这个效果挂到功底身上(每 5 秒续一次)。 */
            if (this.getVariant() == 3) {
                var adam = ForgeRegistries.MOB_EFFECTS.getValue(
                        new ResourceLocation("armamentarium", "adamantine_effect"));
                if (adam != null) {
                    this.addEffect(new MobEffectInstance(adam, INNATE_BUFF_DURATION, 3, true, false, false));   // v128: 等级 IV(用户要求)
                } else {
                    /* 万一这个 mod 不在: 退回"硬得离谱"的手动等效 */
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, INNATE_BUFF_DURATION, this.buffAmp(3), true, false, false));
                    this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, INNATE_BUFF_DURATION, this.buffAmp(2), true, false, false));
                }
                var kbAttr = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                if (kbAttr != null && kbAttr.getBaseValue() < 0.9) kbAttr.setBaseValue(0.9);
                if (this.tickCount % 600 == 0) {
                    FTBossMod.LOGGER.info("[FTBoss][Easter] 功底 亚当甲套装效果: {}",
                            adam == null ? "手动等效(抗性III+吸收II)" : "armamentarium:adamantine_effect");
                }
            }
        }
        if (ManorBuffCopy.isCopyTick(this.tickCount)) this.syncPlayerBuffs();
        /* ★ v128: 柚子分身每 3 秒与本体同步一次增益 */
        if (this.getVariant() == 0 && !this.isMini() && this.tickCount % 60 == 0) this.shareBuffsToMinis();
        /* 2026-09-12: 由 Boss 自己驱动"入场演出"和"庄园战场"(不再单靠事件总线 —— 那次演出卡住就是这个原因) */
        net.farmingtales.ftboss.boss.HiddenBossIntro.pumpAll();
        net.farmingtales.ftboss.boss.ManorFight.pumpAll();
        if (this.actionTicks > 0) this.actionTicks--;
        if (this.castTicks > 0) this.castTicks--;
        /* ★ v128: 断罪斩的攻击力加成到点就摘掉 */
        if (this.cleaveAtkT > 0 && --this.cleaveAtkT == 0) {
            var atkMod = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atkMod != null) atkMod.removeModifier(CLEAVE_ATK_MOD);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 功底「断罪斩」攻击力加成结束");
        }

        this.bossBar.setProgress(Math.max(0f, Math.min(1f, this.getHealth() / this.getMaxHealth())));
        /* ★ v147 掉入虚空立刻传送回来(用户要求) */
        if (!this.level().isClientSide && this.getY() < this.level().getMinBuildHeight() - 8.0) {
            this.rescueFromVoid();
            if (this.isRemoved()) return;
        } else if (this.voidFalls > 0 && ++this.voidSafeTicks > VOID_STREAK_WINDOW) {
            this.voidFalls = 0;      // 30 秒没再掉下去 -> 不再算"连续"
        }
        this.bossBar.setVisible(this.isAlive() && !(this.isInvulnerable() && this.isNoAi()));   // 演出期间(无敌+站定)隐藏自带血条

        /* ---- 2026-09-12 v26 新机制 ---- */
        this.purgeTick();                                     // 中减益 -> 2 秒后清掉自身减益(增益保留)
        this.dandanRushTick();      // ★ 2026-09-14 蛋蛋连瞬结算


        /* ★ v106 按用户反馈「boss 破坏方块的效率不高」: 追着玩家时每 3 秒啃掉身边一层 5x5 */
        if (!this.isMini() && this.level() instanceof ServerLevel slDig) {
            LivingEntity digTarget = this.getTarget();
            if (digTarget != null && digTarget.isAlive()) {
                if (this.digT > 0) this.digT--;
                else { this.digT = BREAK_INTERVAL; this.digTick(slDig); }
            } else {
                this.digT = 0;                                // 没目标就重置, 一追上来立刻啃第一口
            }
        }
        if (this.getVariant() == 4 && this.tickCount % FOOD_BUFF_INTERVAL == 0) this.foodBuffTick();   // 柠檬猫: 食物系增益
        /* ★ v171 按用户要求「给柠檬加一个 buff 检测防止无法杀死」:
           每 2 秒扫一次柠檬猫身上的增益, 清掉"抗性 IV 及以上 / 止心术 / 饱腹代偿 …"这类
           会让它打不死的。放在这里而不是只在发增益时拦, 是因为献祭继承、连锁增益、
           助战强化、以及整合包别的来源都可能塞进来 —— 只有定期清扫才兜得住。
           只清**有益**效果, 玩家挂在它身上的减益照旧保留。 */
        if (this.getVariant() == LEMONCAT_VARIANT && !this.isMini() && this.tickCount % 40 == 0) {
            net.farmingtales.ftboss.boss.ai.LemonImmuneBuffRules.purge(this);
        }
        if (this.riptideTicks > 0 || this.potionDelay > 0) this.qingyiTick();   // 青衣: 激流冲刺 + 扔药水
        /* 2026-09-12 v34: 朵拉的装弹/长枪冲刺 */
        if (this.reloadTicks > 0) this.reloadTick();
        /* 2026-09-12 v29: 青衣的"恒星三叉戟雨"推进(每 2 tick 落一把, 共 16 把) */
        if (this.qingyiRain > 0 && --this.qingyiRainTimer <= 0) {
            this.qingyiRainTimer = net.farmingtales.ftboss.balance.ManorBalanceHooks.tridentInterval(2, this, "rain");
            this.qingyiRain--;
            if (this.level() instanceof ServerLevel slR) {
                LivingEntity rt = this.getTarget();
                if (rt != null && rt.isAlive()) this.dropStarTrident(slR, rt);
                else this.qingyiRain = 0;
            }
        }

        /* 冻结中(入场演出的占位者 / 终章拼成巨大人形的 9 位)**不放技能、不换武器、不抓人**,
           只保留已经开始的连射/蓄力结算; 位置由 ManorFight 每 tick 对齐人形站位 */
        boolean frozen = this.isNoAi();
        if (this.getVariant() == LEMONCAT_VARIANT) {
            if (this.level() instanceof ServerLevel lemonLevel) this.tryHalfHealthLemonSacrifice(lemonLevel);
            this.lemonSacrifice.tick();
        }
        if (!frozen && this.getVariant() == 6 && this.level() instanceof ServerLevel openingLevel) {
            this.grantOpeningFlywheels(openingLevel);
        }
        /* 2026-09-12 v33 按用户要求: **去除飞行/滑翔** —— Boss 一律地面行动。
           破方块改成"抓人(15 秒没锁定到玩家)时砸开周边 5x5"。 */
        /* 2026-09-12 v48 修"麻雀进了场却什么都不做": 以前这里要求"柚子在場", 可麻雀是**随机支援位**,
           柚子不一定在 —— 于是它只是站着。现在只要它自己在场就跑支援(治疗/上增益/净化)。 */
        if (!frozen && this.getVariant() == SPARROW_VARIANT) this.sparrowTick();
        /* 2026-09-12 v42 按用户反馈"麻雀未生效": 去掉这里的柚子在場门槛 —— 只要有任何一位彩蛋 Boss
           在 tick, 麻雀判定就跑(另外 BossHandler 的玩家 tick 也会驱动一次, 双保险) */
        try {
            var srv = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (srv != null) net.farmingtales.ftboss.boss.SparrowSupport.tick(srv);
        } catch (Throwable ignored) {
        }
        /* 2026-09-12 v33: 柠檬猫召唤的食物系 Boss 有寿命, 到点收回 */
        if (this.getVariant() == 4 && this.summonLife > 0 && --this.summonLife <= 0) {
            this.clearSummons(this.level() instanceof ServerLevel slS ? slS : null);
        }
        LivingEntity t = this.getTarget();
        /* ★ v115 按审计报告 G 修「技能轮换被冷却门控饿死」:
           原来 `--this.skillCd` 挂在 && 链最后 —— 短路求值下冷却**只在"完全空闲"的 tick 才递减**,
           而近战每次命中都会 playAction(20)、青衣每 6 秒 playCast(40) ⇒ 冷却推进被拖慢约 6 倍
           (日志对照: 写在函数首句的 qingyiSpellCd 12 次 vs 被门控的技能 2 次)。
           现在冷却无条件递减, 只看"到点 + 当前能出手"。 */
        if (this.skillCd > 0) {
            if (this.getVariant() == DORA_VARIANT) {
                this.doraSkillProgress += 1.0 / this.doraTempo.cooldownMultiplier();
                int elapsed = (int) (this.doraSkillProgress + 1.0E-9);
                this.doraSkillProgress -= elapsed;
                this.skillCd = Math.max(0, this.skillCd - elapsed);
            } else this.tickSharedSkillCooldown();
        } else this.doraSkillProgress = 0;
        if (this.getVariant() == GONGDI_VARIANT && !frozen && !this.isInvulnerable() && !this.isGiant() && !finale
                && !this.skillCommitted() && this.skillCd <= 0 && t instanceof Player player
                && this.gongdiSkills.validTarget(player, CLEAVE_RADIUS)) {
            /* ★ v178 按用户要求「功底减少铁魔法施法, 多用技能」:
               以前技能冷却挂在施法间隔上(spellInterval(3) / SPELLS_PER_CAST = 160 tick),
               「少放法术」会把技能一起拖慢; 现在技能自带冷却 120 tick(6 秒),
               与铁魔法节奏解耦, 三招轮转因此更密。 */
            this.skillCd = Math.max(1, net.farmingtales.ftboss.util.ManorBossCombatRules.gongdiSkillCooldown());
            this.castSkill(t);
        }
        if (this.getVariant() != 3 && this.getVariant() != QIZAI_VARIANT && !frozen && !this.skillCommitted() && t != null && t.isAlive()
                && (this.getVariant() != DORA_VARIANT || this.burstShots <= 0 && this.reloadTicks <= 0)
                && this.canScheduleSharedSkill() && this.skillCd <= 0) {
            /* 2026-09-12 v33/v45: 朵拉与文玄蓝的技能冷却减半(装弹/投掷更频繁)
               2026-09-12 v54 按用户要求: **蛋蛋(1)的技能频率也提上来**
               2026-09-12 v60 按用户要求「蛋蛋的传送频率加快」: 蛋蛋再快一档(冷却 ÷3 -> 3~5 秒一次)
               2026-09-13 v79 按用户要求「蛋蛋的瞬移和换位会增加使用率」: 蛋蛋再快一档(÷4 -> 约 2~3.5 秒一次) */
            int cd = 160 + this.random.nextInt(120);
            if (this.getVariant() == 1) cd /= 4;
            else if (this.getVariant() == 2 || this.getVariant() == 6) cd /= 2;
            /* ★ 2026-09-24 折耳根(7) 从这里挪走：他的技能冷却改由
               ManorBossCombatRules.ZHEERGEN_SKILL_COOLDOWN 单独给值（用户要求「更多使用暗影剑传送」），
               不再吃"近战位 ×2"那一档，所以这里不需要给他预留 cd。 */
            else if (this.getVariant() == 0 || this.getVariant() == 4) cd = 80 + this.random.nextInt(40);
            /* v78 按用户要求「给雪黎更多技能」: 冷却再缩到 ~60%(约 2.4~4.7 秒一招, 三招轮着放看得见) */
            else if (this.getVariant() == XUELI_VARIANT) cd = (int) (cd * 0.6);
            /* v117 按用户要求「青衣召唤三叉戟的欲望提高」: 她也缩到 ~60%, 投戟/星戟雨放得更勤 */
            else if (this.getVariant() == 5) cd = (int) (cd * QINGYI_CD_MULT);
            /* v79 玄蓝在充能/引导超能光束期间不插别的技能(光束优先, 但**不 return**——后面的
               连射/蓄力/传送结算还得继续跑) */
            if (this.xuanlanBeamBusy()) {
                this.skillCd = 10;
            } else {
                this.skillCd = net.farmingtales.ftboss.boss.ai.XueliPhases.skillInterval(
                        net.farmingtales.ftboss.util.ManorBossCombatRules.skillCooldown(this.getVariant(), cd), this);
                this.castSkill(t);
            }
        }
        /* 2026-09-12 v17/v27: 朵拉的连射推进 —— 射速翻倍(基准 3 tick -> 实际 1.5 tick 一发) */
        if (this.burstShots > 0) {
            this.burstAcc += BURST_RATE_MULT * 2.0 * this.doraTempo.attackSpeedMultiplier()
                    * this.getChallengeDifficulty().attackRate();
            int need = BURST_INTERVAL * 2;
            while (this.burstShots > 0 && this.burstAcc >= need) {
                this.burstAcc -= need;
                this.pumpBurst();
            }
        }
        /* 2026-09-12 v18: 功底的断罪斩 —— 蓄力结束就砍 */
        /* 2026-09-12 v19: 柠檬猫的猛火翻锅 —— 蓄力结束就翻 */
        /* 2026-09-12 v20: 青衣的四象轮转 —— 每 8 秒换一件 TnS 独特武器 */
        if (!frozen && this.getVariant() == 5 && --this.qingyiTimer <= 0) {
            this.qingyiTimer = QINGYI_SWAP_TICKS;
            if (this.level() instanceof ServerLevel sl5) this.rotateQingyiWeapon(sl5);
        }
        /* 2026-09-13 v82 按用户要求: 青衣定期放**铁魔法闪电**(等级 5~7, 每 6 秒一次) */
        if (!frozen && this.getVariant() == 5 && this.level() instanceof ServerLevel slQL) {
            this.qingyiLightningTick(slQL);
        }
        /* 2026-09-12 v21: 文玄蓝的飞轮绞杀 —— 每 1 秒磨一次身边的玩家 */
        if (!frozen && this.getVariant() == 6 && this.tickCount % 20 == 0 && this.level() instanceof ServerLevel sl6) {
            this.flywheelGrind(sl6);
            /* 2026-09-12 v45: 记录"飞轮是否叠满" —— 叠满后飞轮攻速 4/秒 -> 10/秒 */
            boolean maxed = this.countFlywheels(sl6) >= FlywheelProjectileEntity.MAX_FLYWHEELS;
            if (maxed != this.flywheelMaxed) {
                this.flywheelMaxed = maxed;
                FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝飞轮{} -> 攻速 {}",
                        maxed ? "已叠满 " + FlywheelProjectileEntity.MAX_FLYWHEELS + " 个" : "未满",
                        maxed ? FlywheelProjectileEntity.HIT_COOLDOWN_MAXED + " tick/次(约每秒 10 个)" 
                              : FlywheelProjectileEntity.HIT_COOLDOWN + " tick/次(约每秒 4 个)");
            }
        }
        /* 2026-09-13 v79 文玄蓝「超能光束」状态机(每 tick; 充能/引导期间专心引导) */
        if (!frozen && this.getVariant() == 6 && this.level() instanceof ServerLevel slBeam) {
            this.xuanlanBeamTick(slBeam);
        }
        /* 2026-09-12 v41 按用户要求: 环绕中的飞轮等 20 秒后**全部齐射一轮**
           (v79: 引导光束时不齐射, 专心引导) */
        if (!frozen && this.getVariant() == 6 && !this.xuanlanBeamBusy() && --this.flywheelVolleyCd <= 0
                && this.level() instanceof ServerLevel slV) {
            this.flywheelVolleyCd = this.getChallengeDifficulty().acceleratedTicks(FLYWHEEL_VOLLEY_TICKS);
            this.flywheelVolley(slV);
        }
        /* 2026-09-12 v22: 文玄蓝的多发飞轮 —— 每 8 tick 投一个(引导光束时暂停投掷) */
        if (this.flywheelShots > 0 && !this.xuanlanBeamBusy() && --this.flywheelTimer <= 0
                && this.level() instanceof ServerLevel sl7) {
            this.throwFlywheel(sl7);
        }
        /* 2026-09-12 v23 按用户要求: 连续没锁定到玩家 -> 把玩家抓回身边 + "哼，想逃？"
           2026-09-13 v72: **麻雀例外** —— 她不去抓玩家, 而是把自己传送到友军身边(见 teleportToAlly)
           2026-09-13 v94: 阈值 15 秒 -> 10 秒, 且只认**玩家**目标; 庄园里交给 ManorFight 统一处理 */
        if (!frozen) {
            LivingEntity lock = this.getTarget();
            if (lock instanceof Player lp && lp.isAlive()) {
                this.noTargetTicks = 0;
            } else if (++this.noTargetTicks >= NO_TARGET_GRAB_TICKS) {
                this.noTargetTicks = 0;
                if (this.getVariant() == SPARROW_VARIANT) {
                    if (this.level() instanceof ServerLevel slS) this.teleportToAlly(slS);
                } else if (this.level() instanceof ServerLevel slGrab) {
                    /* v94: 庄园战场里不自己抓 —— 那边会按"谁离场地更近"决定挪玩家还是挪 Boss */
                    if (!net.farmingtales.ftboss.boss.ManorFight.isManaged(this)) this.grabLaggard(slGrab);
                }
            }
            /* v72 麻雀: 每 tick 检查一次"离友军太远就传过去"(内部自带 10 格阈值 + 3 秒冷却) */
            if (this.getVariant() == SPARROW_VARIANT) {
                if (this.level() instanceof ServerLevel slS2) this.teleportToAlly(slS2);
            }
            if (!this.isMini() && !this.isGiant() && !RANGED[this.getVariant()]
                    && this.getVariant() != SPARROW_VARIANT) this.pearlTick();
        }
        /* ★★★ 2026-09-24 新增（三条都挂在这里，全是服务端）★★★
           ① 「多久没打到玩家」计时：本 tick +1，被 {@code ManorChaseTracker} 在真的造成伤害时清零；
           ② 连续 120 秒没打到玩家 -> 开 60 秒场地禁飞，并把计时器清零重新倒计时；
           ③ 拥有「爱意滑翔」(brewery:haley) 时允许本 Boss 飞行追击。 */
        if (!this.level().isClientSide && !frozen) {
            if (this.playerHitIdleTicks < Integer.MAX_VALUE) this.playerHitIdleTicks++;
            this.manorFlyBanTick();
            this.haleyFlightTick();
        }
    }

    /**
     * v72 按用户要求「麻雀会优先传送自己到友军旁边」。
     *
     * <p>友军 = {@link #supportedBoss()}(32 格内最近的其它彩蛋 Boss, 麻雀一直在奶的那位)。
     * 离他超过 {@value #SPARROW_TELEPORT_RANGE} 格就带传送门粒子闪过去, 每 {@value #SPARROW_TELEPORT_CD} tick 最多一次。
     * 这样她既能跟上被支援的 Boss, 又不会去抓玩家。
     */
    private void teleportToAlly(ServerLevel sl) {
        EasterBossEntity ally = this.supportedBoss();
        if (ally == null) return;
        double dist = Math.sqrt(this.distanceToSqr(ally));
        if (dist <= SPARROW_TELEPORT_RANGE) return;
        if (sl.getGameTime() - this.lastSparrowTeleport < SPARROW_TELEPORT_CD) return;
        this.lastSparrowTeleport = sl.getGameTime();
        double ang = this.random.nextDouble() * Math.PI * 2.0;
        double tx = ally.getX() + Math.cos(ang) * 3.0;
        double tz = ally.getZ() + Math.sin(ang) * 3.0;
        double ox = this.getX(), oy = this.getY(), oz = this.getZ();
        sl.sendParticles(ParticleTypes.PORTAL, ox, oy + 1.0, oz, 26, 0.4, 0.7, 0.4, 0.28);
        Vec3 safe = ManorSafeTeleport.find(sl, this, new Vec3(tx, ally.getY(), tz), 6);
        if (safe == null) return;
        ManorSafeTeleport.move(sl, this, safe);
        sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 26, 0.4, 0.7, 0.4, 0.28);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.1f, 1.4f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀 传送到友军 {} 身边 (传送前 {} 格)",
                NAMES[ally.getVariant()], (int) dist);
    }

    /** 抓回逃跑的玩家(2026-09-12 v23 用户定案): 把 {@value #NO_TARGET_GRAB_RANGE} 格内**最近的玩家**
     * 传送到自己身边 3 格, 给本人播报 "哼，想逃？"(64 格内其他玩家也能看到是谁说的), 两端各放传送门粒子。
     * 2026-09-12 v33 按用户要求: 抓人时**自动砸开周边 5×5 的方块**(硬度 ≤5, 无方块实体)。 */
    private void grabLaggard(ServerLevel sl) {
        LivingEntity best = net.farmingtales.ftboss.boss.ai.BossAggro.highest(this);
        if (best == null) best = this.manorSummoner();
        if (best == null || best.distanceToSqr(this) >= NO_TARGET_GRAB_RANGE * NO_TARGET_GRAB_RANGE) return;
        double ang = this.random.nextDouble() * Math.PI * 2.0;
        double tx = this.getX() + Math.cos(ang) * 3.0;
        double tz = this.getZ() + Math.sin(ang) * 3.0;
        double ox = best.getX(), oy = best.getY(), oz = best.getZ();
        sl.sendParticles(ParticleTypes.PORTAL, ox, oy + 1.0, oz, 30, 0.4, 0.7, 0.4, 0.3);
        Vec3 safe = ManorSafeTeleport.find(sl, best, new Vec3(tx, this.getY(), tz), 8);
        if (safe == null) return;
        ManorSafeTeleport.move(sl, best, safe);
        sl.sendParticles(ParticleTypes.PORTAL, tx, this.getY() + 1.0, tz, 30, 0.4, 0.7, 0.4, 0.3);
        sl.playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.3f, 1.2f);
        sl.playSound(null, tx, this.getY(), tz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.3f, 0.8f);
        best.sendSystemMessage(Component.literal("§c§l" + NO_TARGET_TEXT));
        for (Player p : sl.players()) {
            if (p != best && p.distanceToSqr(this) <= 64.0 * 64.0) {
                p.sendSystemMessage(Component.literal("§c" + NAMES[this.getVariant()] + "§7：§c§l" + NO_TARGET_TEXT));
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} 15 秒未锁定到玩家 -> 把 {} 抓回身边 @{},{},{}",
                NAMES[this.getVariant()], best.getName().getString(), (int) tx, (int) this.getY(), (int) tz);
        /* 2026-09-12 v52 按用户要求: 抓人同时砸开周边 **7x7、从脚底往上的 3 层**(脚下那块地不砸) */
        this.breakAround(sl, 3, 3);
    }

    /**
     * 破坏以自己为中心的 (2r+1)×(2r+1) 方块, **只砸自己脚下那一层以及往上的 up 层**(2026-09-12 v52 按用户要求)。
     * <p>改动前是"y 方向 ±ry", 会把**自己脚下那块地**也挖掉, Boss 动不动就掉进自己挖的坑里;
     * 现在 dy 从 0 开始(脚下那块地 dy=-1 一律不动), 范围也按用户要求放大到 7×7。
     * 仍然只砸**硬度 ≤5 且没有方块实体**的方块(石头/泥土/木头/玻璃/树叶…), 箱子之类不动。
     * ★ 2026-09-24 按用户要求「允许 boss 破坏同水平基岩」：**与 Boss 身体同一水平段**的
     * 不可破坏方块（基岩/屏障…硬度 -1）现在也砸得掉，世界底层/上层的基岩不在此列。
     * <p>2026-09-14 v106: 实际砸方块的循环拆到 {@link #breakBlocks} —— 追击时每 3 秒也会啃一次。
     */
    private void breakAround(ServerLevel sl, int r, int up) {
        int broken = this.breakBlocks(sl, r, up);
        if (broken > 0) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.2f, 0.9f);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} 抓人时砸开周边 {}x{} (脚下那层不砸, 往上 {} 层) 方块: {} 个",
                NAMES[this.getVariant()], r * 2 + 1, r * 2 + 1, up, broken);
    }

    /**
     * ★★★ 2026-09-14 v106 按用户反馈「boss 破坏方块的效率不高」★★★
     *
     * <p>以前全模组**只有一处**会砸方块: 15 秒没锁定到玩家时"把玩家抓回身边"那一击(7×7×3)。
     * 一场仗打下来 Boss 基本不碰地形 —— 玩家的体感就是"它不会拆方块"。
     * 现在只要它**正在追人**, 每 {@value #BREAK_INTERVAL} tick 就啃掉身边 5×5 的一层(脚下那层不动),
     * 追着玩家一路犁过去; 抓人那一击仍然额外砸一次更大的 7×7×3。
     */
    private void digTick(ServerLevel sl) {
        int broken = this.breakBlocks(sl, 2, 1);
        if (broken <= 0) return;
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 0.9f, 1.1f);
        /* 每 5 次才打一条日志(3 秒一次 × 十位 Boss, 全打会把日志刷爆) */
        if (this.tickCount % (BREAK_INTERVAL * 5) < BREAK_INTERVAL) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} 追击啃地 5x5: {} 个方块",
                    NAMES[this.getVariant()], broken);
        }
    }

    /** 真正砸方块的那段循环: 返回砸掉的数量(不含音效/日志, 两个调用点共用) */
    private int breakBlocks(ServerLevel sl, int r, int up) {
        if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(sl, this)) return 0;
        int cx = (int) Math.floor(this.getX());
        int cy = (int) Math.floor(this.getY());
        int cz = (int) Math.floor(this.getZ());
        /* ★★★ 2026-09-24 按用户要求「允许 boss 破坏同水平基岩」★★★
           以前这里用 `hardness < 0.0f` 把所有"不可破坏"方块（基岩/屏障/传送门框架…）一律跳过 ——
           玩家只要在与 Boss **同一水平段**的基岩后面或缝里一躲，Boss 就永远碰不到他。
           现在只放行**与 Boss 身体同一水平段**的不可破坏方块：
           `pos.getY()` 落在 [脚底层, 身体最高层] 之内 —— 也就是"它自己这一层高的墙/块"。
           世界底层与上层的基岩（远在脚底之下 / 头顶之上）仍然不碰，避免把地图啃穿。
           注意 `BlockState.canEntityDestroy` 对 wither_immune 标签（基岩在内）恒为 false，
           所以这一类不再走那道判定，改由 destroyBlock（必要时兜底清空气）决定成败。 */
        int bodyTop = (int) Math.floor(this.getBoundingBox().maxY);
        int broken = 0;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = 0; dy <= up; dy++) {          // v52: 从脚底这层往上, 脚下(y-1)不砸
                    var pos = new net.minecraft.core.BlockPos(cx + dx, cy + dy, cz + dz);
                    var state = sl.getBlockState(pos);
                    if (state.isAir()) continue;
                    float hardness = state.getDestroySpeed(sl, pos);
                    if (hardness > 5.0f || state.hasBlockEntity()) continue;
                    boolean indestructible = hardness < 0.0f;
                    boolean sameLevel = pos.getY() >= cy && pos.getY() <= bodyTop;
                    if (indestructible && !sameLevel) continue;
                    if (!indestructible && !state.canEntityDestroy(sl, pos, this)) continue;
                    if (!net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(this, pos, state)) continue;
                    if (pos.getY() < this.getY() && pos.getY() == cy) continue;
                    boolean removed = sl.destroyBlock(pos, false, this, 512);
                    if (!removed && indestructible) {
                        /* 兜底：`destroyBlock` 在个别版本/模组下会拒绝"硬度 -1"的方块，直接清成空气 */
                        removed = sl.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                    if (removed) broken++;
                }
            }
        }
        return broken;
    }

    public void prepareManorEntryBuffs() {
        this.syncPlayerBuffs();
    }

    private void syncPlayerBuffs() {
        Player best = null;
        double bd = 48.0 * 48.0;
        for (Player p : this.level().players()) {
            if (!p.isAlive()) continue;
            double d = p.distanceToSqr(this);
            if (d < bd) { bd = d; best = p; }
        }
        if (best == null) return;
        int n = 0;
        for (net.minecraft.world.effect.MobEffectInstance inst : java.util.List.copyOf(best.getActiveEffects())) {
            MobEffectInstance copied = ManorBuffCopy.copyOf(inst);
            if (copied != null && this.addEffect(copied)) n++;
        }
        if (n > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} 同步了 {} 名玩家的 {} 个增益效果（持续 {} 秒，冷却 {} 秒）",
                    NAMES[this.getVariant()], 1, n, ManorBuffCopy.DURATION_TICKS / 20, ManorBuffCopy.COOLDOWN_TICKS / 20);
            /* v96 按用户要求「每次获取玩家 buff 时 10% 概率禁止玩家 30s 内回复血量」——
               只有麻雀会这么干(其它彩蛋 Boss 拿到增益不附带禁疗)。 */
            if (this.getVariant() == SPARROW_VARIANT && this.random.nextFloat() < SPARROW_NOHEAL_CHANCE
                    && best instanceof net.minecraft.server.level.ServerPlayer sp) {
                net.farmingtales.ftboss.boss.ai.HealBan.ban(sp);
            }
        }
    }

    /** 每位 Boss 的看家技能: 施法型用施法姿势 + 范围冲击, 其余用横扫重击; 已有专属机制的在下面分流 */
    private void castSkill(LivingEntity target) {
        if (!net.farmingtales.ftboss.boss.ai.XueliPhases.shouldCast(this)) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (this.getVariant() == LEMONCAT_VARIANT && this.lemonSacrifice.trySacrifice(this.summons)) {
            this.summons.clear();
            this.summonLife = 0;
            return;
        }
        if (this.getVariant() == 5) {
            if (this.qingyiConfluenceNext) {
                this.qingyiConfluence.start(sl, target);
                if (this.qingyiConfluence.busy()) {
                    this.qingyiConfluenceNext = false;
                    return;
                }
            } else this.qingyiConfluenceNext = true;
        }
        if (this.manorFiveSkills.tryStart(sl, target)) return;
        /* 2026-09-12 v16: 蛋蛋专属 —— 末影换位 */
        if (this.getVariant() == 1) { this.skillEnderSwap(sl, target); return; }
        /* 2026-09-12 v17: 朵拉专属 —— 压制射击 */
        if (this.getVariant() == DORA_VARIANT) { this.skillDoraGun(sl, target); return; }
        /* 2026-09-12 v18: 功底专属 —— 三招轮转(无情铁手 / 致残打击 / 诺克萨斯断头台)
           ★ v178 按用户要求「功底减少铁魔法施法, 多用技能」:
           以前 tryStart 里的 rotation%3 空转位会**回退到「断罪斩(大杀四方)」**,
           等于三招里总有 1/3 被自己的横扫占掉; 现在去掉该回退 ——
           空转位由 GongdiSkills 顺延到下一个可用的三招技能(取决于单招冷却: 致残 100 / 铁手 160 / 断头台 360),
           因此功底打出来的一定是诺手三招。若要恢复大杀四方, 把下面的
           `this.gongdiSkills.tryStart(sl, target);` 改回
           `if (!this.gongdiSkills.tryStart(sl, target)) this.skillCleave(sl, target);` 即可。 */
        if (this.getVariant() == GONGDI_VARIANT) {
            this.gongdiSkills.tryStart(sl, target);
            return;
        }
        /* 2026-09-12 v19/v33/v42: 柠檬猫专属 —— 猛火翻锅 / 喊 2 位食物系 Boss 助战
           (v39 曾按"不要把其他 Boss 拉进场地"关掉; v42 按用户反馈"召唤未生效"恢复)
           2026-09-12 v62 按用户反馈「柠檬的 boss 助战没触发」: 以前是纯 50/50 随机, 短战斗常常一次都摇不到召唤;
           现在 —— ① 场上没有帮手存活时**必定召唤**; ② 否则两个技能交替(先召唤, 后翻锅)。 */
        if (this.getVariant() == LEMONCAT_VARIANT) {
            /* 献祭后的助战 Boss 真正被击败三位后，召唤位永久退役；后续在翻锅/借招之间交替，
               不空放“喊帮手”，也不把超时或主动收回算作击败。 */
            boolean canSummon = this.lemonSacrifice.canSummonHelpers();
            if (this.aliveSummons() == 0 && canSummon) {
                this.skillSummonFoodBosses(sl, target);
            } else if (!canSummon) {
                if ((this.lemonStep++ & 1) == 0) this.skillPanFlip(sl, target);
                else this.skillLemonBorrowed(sl, target);
            } else {
                switch (this.lemonStep % 3) {
                    case 0 -> this.skillSummonFoodBosses(sl, target);
                    case 1 -> this.skillPanFlip(sl, target);
                    default -> this.skillLemonBorrowed(sl, target);
                }
                this.lemonStep++;
            }
            return;
        }
        /* 2026-09-12 v20: 青衣专属 —— 两个技能交替: ①投戟+激流+混毒药剂 ②空中召唤 16 把恒星三叉戟 */
        if (this.getVariant() == 5) {
            if (this.qingyiAlt) { this.qingyiAlt = false; this.skillQingyi(sl, target); }
            else { this.qingyiAlt = true; this.skillStarTridentRain(sl, target); }
            return;
        }
        /* 2026-09-12 v21: 文玄蓝 / 折耳根 / 紫菜 专属 */
        if (this.getVariant() == 6) {
            /* 2026-09-12 v24 按用户要求: 飞轮没满就一直投(会越打越多并环绕在身边, 最多 25 个);
               满了以后改喷蒸汽(相当于"技能换挡"), 不再白投。
               2026-09-13 v71 按用户要求: **16~8 格内**改走连招 ——
               「高伤害光束打击 -> 全部飞轮一起扑向玩家 -> 飞轮攻击力 +10%」。 */
            double dist = Math.sqrt(this.distanceToSqr(target));
            if (dist >= BEAM_COMBO_MIN && dist <= BEAM_COMBO_MAX) {
                this.skillBeamCombo(sl, target);
            } else if (this.countFlywheels(sl) >= FlywheelProjectileEntity.MAX_FLYWHEELS) {
                this.skillSteamVent(sl, target);
            } else {
                this.skillFlywheelVolley(sl, target);
            }
            return;
        }
        if (this.getVariant() == 7) { this.skillShadowStrike(sl, target); return; }
        /* 2026-09-13 v76 雪黎: 招牌技能 = 把**汤锅**整个扔出去砸人(近战另有擀面杖的粉碎轮)
           2026-09-13 v78 按用户要求"给雪黎更多技能": 三招轮换 —— ①汤锅投掷 ②平底锅横扫 ③擀面杖压路冲锋 */
        if (this.getVariant() == XUELI_VARIANT && !this.isMini()) {
            /* 2026-09-13 v103 按用户要求加了 ④禁疗飞刀 / ⑤自爆分身 —— 五招轮换。
               2026-09-13 v111 按用户要求再加 ⑦「天降瞬砸」—— 七招轮换:
               0 汤锅 / 1 平底锅 / 2 压路冲锋 / 3 禁疗飞刀 / 4 天降瞬砸 / 5 女仆护法 / 6 自爆分身。
               ★ **迷你分身(自爆分身)不进这个分支**: 它们要是也能放 ⑥, 就会无限召分身(滚雪球);
               分身走下面的通用近战, 靠近就自爆, 这才是"自爆分身"该有的样子。 */
            /* v111: ⑦ 蓄势期不接别的招, 否则警告打到一半她自己先跑了。
               v125: 五连砸的**间隔期**(xueliSlamLeft > 0)同样不接 —— 否则间隔那 1 秒会被别的招插进来 */
            if (this.xueliSlamWarn > 0 || this.xueliSlamLeft > 0 || this.xueliSlamGap > 0) return;
            switch (this.xueliNext) {
                case 0 -> this.skillXueli(sl, target);
                case 1 -> this.skillXueliPanSlap(sl, target);
                case 2 -> this.skillXueliRoll(sl, target);
                /* v106: 从 3 起轮 = 每次上场第一招就是禁疗飞刀(以前轮不到第 4 招就被打死了) */
                case 3 -> this.skillXueliKnives(sl, target);
                /* v111 ⑦「天降瞬砸」**排在第 2 招**: 庄园里雪黎常常两三个技能就被打死, 排太后根本看不到;
                   场上没玩家时(理论上不会有)退回平底锅, 不浪费这一回合 */
                case 4 -> {
                    if (!this.skillXueliSlam(sl, target)) this.skillXueliPanSlap(sl, target);
                }
                case 5 -> {
                    /* v104 ⑥「女仆护法」: 有冷却/上一批没清完就先不放, 这一轮改掷飞刀(不浪费回合) */
                    if (this.xueliMaids.canTrigger()) this.xueliMaids.trigger();
                    else this.skillXueliKnives(sl, target);
                }
                default -> this.skillXueliClones(sl, target);
            }
            this.xueliNext = (this.xueliNext + 1) % 7;
            return;
        }
        if (this.getVariant() == 8) {
            /* 2026-09-12 v28: 紫菜"积极使用高级魔法" —— 毒雾与铁魔法高级法术各半。
               2026-09-13 v89 按用户要求「去除紫菜的铁魔法」: 铁魔法那半去掉, 招牌技能只剩自家的
               「毒雾爆发」(14 伤害 + 中毒 III + 缓慢 II)。见 {@link #zicaiAllowed} 里同步去掉的
               铁魔法减益(河豚减益池以前会把 irons_spellbooks 的负面效果一起收进来)。 */
            this.skillPoisonBurst(sl, target);
            return;
        }
        if (CASTER[this.getVariant()]) {
            playCast(HiddenBossCastRules.quicker(50), 0);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.4f, 0.9f);
            for (Player p : sl.players()) {
                if (p.distanceToSqr(this) <= 49.0 && p.isAlive()) {
                    /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
                    p.invulnerableTime = 0;
                    p.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(p,
                            this.getVariant() == 0 ? 9.333333F : 6.6666665F, 1.5));
                    net.farmingtales.ftboss.util.BossKnockback.push(p, 0, this.getVariant() == 0 ? 0.70 : 0.5, 0);
                    p.hurtMarked = true;
                }
            }
        } else {
            playAction(26);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.4f, 0.8f);
            double dx = target.getX() - this.getX(), dz = target.getZ() - this.getZ();
            for (Player p : sl.players()) {
                if (p.distanceToSqr(this) <= 25.0 && p.isAlive()) {
                    /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
                    p.invulnerableTime = 0;
                    p.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(p,
                            this.getVariant() == 0 ? 22.4f : 16.0f, 1.5));
                    net.farmingtales.ftboss.util.BossKnockback.knockback(p, this.getVariant() == 0 ? 1.20 : 0.8, -dx, -dz);
                    p.hurtMarked = true;
                }
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} 使用了看家技能(variant={})",
                NAMES[this.getVariant()], this.getVariant());
    }

    /**
     * 蛋蛋 招牌机制「烛影置换」(2026-09-12 v16, 每 8~14 秒一次):
     * 末地烛一照 -> 本体瞬移到目标身侧 2.5 格并转身盯住, 目标吃
     * **飘浮 II 3 秒 + 失明 II 5 秒 + 16 点末影伤害**, 自己拿 **速度 II 6 秒 + 抗性提升 I 3 秒**;
     * 起点/终点/目标三处各放传送门粒子 + 末影人传送音(玩家能听出"它换位了")。
     */
    /** ★ 2026-09-14 蛋蛋连瞬(每 tick): 传送到目标身侧 2.5 格 -> 20 点魔法伤害, 共 6 下 */
    private void dandanRushTick() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (this.dandanRush <= 0) return;
        if (--this.dandanRushTick > 0) return;
        this.dandanRushTick = DANDAN_RUSH_INTERVAL;
        this.dandanRush--;
        LivingEntity t = this.getTarget();
        if (!this.canTeleportWith(t)) { this.dandanRush = 0; return; }
        var dir = t.position().subtract(this.position());
        double len = Math.max(0.001, dir.length());
        double tx = t.getX() - dir.x / len * 2.5, tz = t.getZ() - dir.z / len * 2.5, ty = t.getY();
        Vec3 safe = ManorSafeTeleport.find(sl, this, new Vec3(tx, ty, tz), 4);
        if (safe == null) { this.dandanRush = 0; return; }
        ManorSafeTeleport.move(sl, this, safe);
        sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.6, 0.3, 0.2);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0f, 1.25f);
        t.invulnerableTime = 0;
        if (t.hurt(sl.damageSources().indirectMagic(this, this), this.manorSkillDamage(t, DANDAN_RUSH_DAMAGE, 1.5))
                && this.dandanRush == 0 && t instanceof Player player) {
            this.knockbackPlayer(player);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋连瞬: {} 吃 {} 点魔法伤害(剩 {} 下)",
                t.getName().getString(), (int) DANDAN_RUSH_DAMAGE, this.dandanRush);
    }

    private void skillEnderSwap(ServerLevel sl, LivingEntity target) {
        if (!this.canTeleportWith(target)) return;
        playCast(30, 2);
        double ox = this.getX(), oy = this.getY(), oz = this.getZ();
        double px = target.getX(), py = target.getY(), pz = target.getZ();
        /* 2026-09-12 v28 按用户要求: **随机换位** —— Boss 去玩家那儿, 玩家被丢到 Boss 原来的位置 */
        sl.sendParticles(ParticleTypes.PORTAL, ox, oy + 1.0, oz, 30, 0.4, 0.7, 0.4, 0.3);
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, px, py + 1.0, pz, 20, 0.3, 0.6, 0.3, 0.15);
        Vec3 bossSpot = ManorSafeTeleport.find(sl, this, new Vec3(px, py, pz), 4);
        Vec3 playerSpot = ManorSafeTeleport.find(sl, target, new Vec3(ox, oy, oz), 4);
        if (bossSpot == null || playerSpot == null) return;
        ManorSafeTeleport.move(sl, this, bossSpot);
        ManorSafeTeleport.move(sl, target, playerSpot);
        boolean moved = true;
        /* 2026-09-12 v28 按用户要求: 对玩家造成 **反胃 + 饥饿** */
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, (int)300, 1));
        /* ★ 2026-09-14 按用户要求: 单次伤害 → **连续瞬移到玩家, 20 点魔法伤害 × 6 次** */
        this.dandanRush = DANDAN_RUSH_HITS;
        this.dandanRushTick = 0;
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int)360, 1));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int)180, 0));
        this.getLookControl().setLookAt(target, 40.0f, 40.0f);
        double[][] spots = {
                {ox, oy + 1.0, oz},
                {this.getX(), this.getY() + 1.0, this.getZ()},
                {target.getX(), target.getY() + 1.0, target.getZ()},
        };
        for (double[] p : spots) {
            sl.sendParticles(ParticleTypes.PORTAL, p[0], p[1], p[2], 30, 0.4, 0.7, 0.4, 0.3);
            sl.sendParticles(ParticleTypes.REVERSE_PORTAL, p[0], p[1], p[2], 16, 0.3, 0.5, 0.3, 0.15);
        }
        sl.playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2f, 1.25f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2f, 0.8f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋「烛影置换(换位)」: 换位成功={} 目标={} 现位置 {},{},{}",
                moved, target.getName().getString(), (int) this.getX(), (int) this.getY(), (int) this.getZ());
    }

    /**
     * 朵拉 招牌机制「压制射击」(2026-09-12 v17): 开火 —— 之后由 {@link #pumpBurst()} 每 3 tick 打一发。
     */
    private void skillSuppress(ServerLevel sl, LivingEntity target) {
        if (this.doraSniper.busy() || this.reloadTicks > 0 || this.burstShots > 0 || !this.equipDoraGun(false)) return;
        this.doraSniperNext = true;
        this.burstTotalShots = this.doraTempo.burstShots(BURST_SHOTS);
        playAction((int) Math.ceil(this.burstTotalShots * BURST_INTERVAL
                / (BURST_RATE_MULT * this.doraTempo.attackSpeedMultiplier() * this.getChallengeDifficulty().attackRate())) + 6);
        this.burstShots = this.burstTotalShots;
        this.burstAcc = 0;
        this.burstHits = 0;
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.CROSSBOW_QUICK_CHARGE_3, SoundSource.HOSTILE, 1.1f, 0.9f);
        FTBossMod.LOGGER.info("[FTBoss][Dora] 冲锋枪「压制射击」开火: {} 发 x {} 伤害, 目标={}",
                this.burstTotalShots, BURST_DAMAGE, target.getName().getString());
    }

    /**
     * 功底 招牌机制「断罪斩」(2026-09-12 v18) 第一段: 举刀蓄力 {@link #CLEAVE_WINDUP} tick。
     * 蓄力结束由 {@link #executeCleave()} 结算, 给玩家留出躲开的窗口。
     */
    private void skillCleave(ServerLevel sl, LivingEntity target) {
        this.cleaveWind = this.getChallengeDifficulty().acceleratedTicks(CLEAVE_WINDUP);
        playCast(this.cleaveWind, 1);
        this.beginSkillCue(this.position(), CLEAVE_RADIUS, this.cleaveWind, true);
        CompoundTag cue = this.getSkillCue().copy();
        cue.putString("gongdiSkill", "decimate");
        cue.putDouble("innerRadius", GongdiCombatRules.INNER_RADIUS);
        this.entityData.set(DATA_SKILL_CUE, cue);
        if (target instanceof Player player) {
            player.displayClientMessage(Component.literal("§c功底：大杀四方 §7· 贴近内圈或退出红圈，阻止外刃回血"), true);
        }
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0f, 0.7f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.0f, 0.8f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 功底「断罪斩」开始蓄力 {} tick(半径 {} 格)",
                this.cleaveWind, (int) CLEAVE_RADIUS);
    }

    private void executeCleave() {
        if (!(this.level() instanceof ServerLevel level)) return;
        double attackDamage = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        playImpactAction(18);
        this.releaseSkillCue(this.position());
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.7f, 0.6f);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0, this.getZ(), 12, 2.0, 0.4, 2.0, 0.0);
        int outerHits = 0;
        for (Player player : level.players()) {
            if (!this.gongdiSkills.validTarget(player, CLEAVE_RADIUS)) continue;
            Vec3 offset = player.position().subtract(this.position());
            boolean outer = offset.horizontalDistanceSqr() >= GongdiCombatRules.INNER_RADIUS * GongdiCombatRules.INNER_RADIUS;
            float meleeDamage = GongdiCombatRules.meleeDamage(attackDamage,
                    EnchantmentHelper.getDamageBonus(this.getMainHandItem(), player.getMobType()));
            float damage = GongdiCombatRules.cleaveDamage(meleeDamage, outer);
            float before = player.getHealth();
            player.invulnerableTime = 0;
            if (!player.hurt(this.damageSources().mobAttack(this), damage)) continue;
            if (outer) {
                this.gongdiSkills.onAbilityHit(level, player, Math.max(0.0f, before - player.getHealth()));
                outerHits++;
            }
        }
        this.applyCleaveAtkBoost();
        float healing = GongdiCombatRules.outerHeal(this.getMaxHealth() - this.getHealth(), outerHits);
        if (healing > 0.0f) {
            this.heal(healing);
            level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.8, this.getZ(), 6, 0.4, 0.4, 0.4, 0.0);
        }
        if (outerHits >= 2) this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 480, 1));
        FTBossMod.LOGGER.info("[FTBoss][Gongdi] decimate outerHits={} healing={}", outerHits, healing);
    }

    /** 柠檬猫 招牌机制「猛火翻锅」第一段(2026-09-12 v19): 起油花 0.75 秒 */
    /* ==================== v119 柠檬猫「借用食物系 Boss 的随机技能」 ====================
       用户: 「柠檬会使用食物boss的随机技能」。
       实现: 从四位食物系 Boss 的招牌里各借一招(女皇的甜品爆裂 / 屠夫的剁砍 / 茶树的茶雾 / 酒桶王的酒桶),
       每次施放**随机抽一招**; 伤害用同一套 SKILL_MULT / BOSS_DAMAGE_MULT 口径, 方便日后调。 */
    public static final float LEMON_BORROW_DAMAGE = 78.4f;

    /** 借用食物系 Boss 的随机技能 */
    private void skillLemonBorrowed(ServerLevel sl, LivingEntity target) {
        playAction(30);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        switch (this.random.nextInt(4)) {
            case 0 -> {   // 女皇: 甜品爆裂 —— 自身周围一圈
                double x = this.getX(), y = this.getY(), z = this.getZ();
                sl.sendParticles(ParticleTypes.HEART, x, y + 0.8, z, 24, 1.6, 0.5, 1.6, 0.05);
                int hits = 0;
                for (Player p : sl.players()) {
                    if (!p.isAlive() || p.distanceToSqr(x, y, z) > 25.0) continue;
                    p.invulnerableTime = 0;
                    p.hurt(this.damageSources().indirectMagic(this, this), this.manorSkillDamage(p, LEMON_BORROW_DAMAGE, 1.5));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                    hits++;
                }
                FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫借招「甜品爆裂」命中 {} 人", hits);
            }
            case 1 -> {   // 屠夫: 剁砍 —— 单体重击 + 流血
                target.invulnerableTime = 0;
                target.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(target, LEMON_BORROW_DAMAGE * 1.5f, 2.0));
                net.farmingtales.ftboss.boss.ai.ExtraDebuffs.bleed(target, 8 * 20, 1);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(),
                        18, 0.4, 0.5, 0.4, 0.12);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫借招「剁砍」-> {}", target.getName().getString());
            }
            case 2 -> {   // 茶树: 茶雾 —— 大范围减益
                double x = this.getX(), y = this.getY(), z = this.getZ();
                sl.sendParticles(ParticleTypes.SPLASH, x, y + 0.8, z, 40, 2.4, 0.6, 2.4, 0.08);
                int hits = 0;
                for (Player p : sl.players()) {
                    if (!p.isAlive() || p.distanceToSqr(x, y, z) > 36.0) continue;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                    p.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                    hits++;
                }
                FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫借招「茶雾」命中 {} 人", hits);
            }
            default -> {  // 酒桶王: 酒桶 —— 单体击飞 + 反胃
                target.invulnerableTime = 0;
                target.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(target, LEMON_BORROW_DAMAGE, 1.5));
                net.farmingtales.ftboss.util.BossKnockback.push(target, (target.getX() - this.getX()) * 0.6, 0.5, (target.getZ() - this.getZ()) * 0.6);
                sl.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + 1.0, target.getZ(),
                        16, 0.4, 0.5, 0.4, 0.06);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫借招「酒桶」-> {}", target.getName().getString());
            }
        }
    }

    private void skillPanFlip(ServerLevel sl, LivingEntity target) {
        if (net.farmingtales.ftboss.integ.ManorSpellBridge.replace(this, target, "searing_pan")) return;
        this.panWind = this.getChallengeDifficulty().acceleratedTicks(PAN_WINDUP);
        playCast(this.panWind, 3);
        this.beginSkillCue(this.position(), PAN_RADIUS, this.panWind, true);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.CAMPFIRE_CRACKLE, SoundSource.HOSTILE, 1.2f, 1.3f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.9f, 1.1f);
        sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.8, this.getZ(),
                30, 1.6, 0.3, 1.6, 0.02);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫「猛火翻锅」起油花, {} tick 后翻锅", this.panWind);
    }

    /** 翻锅第二段: 半径 {@link #PAN_RADIUS} 格 -> 18 伤害 + 抛上天 + 点燃 6 秒 + 饥饿 II 8 秒 */
    private void executePanFlip() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        playImpactAction(16);
        this.releaseSkillCue(this.position());
        double x = this.getX(), y = this.getY(), z = this.getZ();
        sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.9f, 1.3f);
        sl.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.4f, 0.8f);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.5, z, 24, 2.0, 0.2, 2.0, 0.06);
        sl.sendParticles(ParticleTypes.LAVA, x, y + 0.4, z, 16, 1.2, 0.2, 1.2, 0.0);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!this.combatPlayer(p) || p.distanceToSqr(x, y, z) > PAN_RADIUS * PAN_RADIUS) continue;
            /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(p, PAN_DAMAGE, 2.0));
            Vec3 outward = p.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize();
            net.farmingtales.ftboss.util.BossKnockback.push(p, outward.x * 0.9, 1.65, outward.z * 0.9);
            p.hurtMarked = true;
            p.setSecondsOnFire((int)18);
            /* v119 按用户要求「柠檬现在的火焰伤害会独立计算成新的 debuff: 蓝火, 每秒 20 点虚空伤害」 */
            net.farmingtales.ftboss.boss.ai.ExtraDebuffs.blueFire(p,
                    net.farmingtales.ftboss.effect.BlueFireEffect.TICKS);
            p.addEffect(new MobEffectInstance(MobEffects.HUNGER, (int)480, 1));
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫「猛火翻锅」命中 {} 人 ({} 伤害 + 抛飞 + 点燃)", hits, PAN_DAMAGE);
    }

    /* ==================== 朵拉「近战长枪」(2026-09-12 v34 用户定案) ==================== */

    private void clearDoraCombat() {
        this.cancelDoraSniper();
        this.clearDoraTempo();
        if (this.burstShots > 0 || this.reloadTicks > 0) {
            this.actionTicks = 0;
            this.entityData.set(DATA_COMBAT_ANIMATION, new CompoundTag());
        }
        this.burstShots = 0;
        this.burstAcc = 0;
        this.reloadTicks = 0;
        this.spearHitCd = 0;
        this.doraSpearLeft = 0;
        this.doraSpearGap = 0;
        this.doraSpearTarget = null;
        this.doraSkillProgress = 0;
        this.doraReloadProgress = 0;
        this.entityData.set(DATA_DORA_RELOADING, false);
        this.entityData.set(DATA_DORA_SPEAR_TICKS, 0);
    }

    private boolean equipDoraGun(boolean focus) {
        if (this.getVariant() != DORA_VARIANT || this.level().isClientSide) return false;
        String identifier = focus ? DORA_FOCUS_ID : DORA_SMG_ID;
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(identifier));
        if (item == null || item == Items.AIR) return false;
        if (this.getMainHandItem().is(item)) return true;
        ItemStack weapon = new ItemStack(item);
        for (Enchantment enchantment : buildEnchants(WEAPON_ENCH_IDS)) {
            if (enchantment != null) weapon.enchant(enchantment, WEAPON_ENCH_LEVEL);
        }
        weapon.getOrCreateTag().putBoolean("Unbreakable", true);
        this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, weapon);
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
        return true;
    }

    private void skillDoraGun(ServerLevel level, LivingEntity target) {
        if (!(target instanceof Player player) || !this.combatPlayer(player) || this.isMini() || this.isNoAi()
                || this.doraSniper.busy() || this.burstShots > 0 || this.reloadTicks > 0) return;
        double distanceSquared = this.distanceToSqr(target);
        if (!DoraSniperCycle.inRange(distanceSquared) || !this.hasLineOfSight(target)) {
            this.skillCd = 10;
            return;
        }
        if (this.doraSniperNext || distanceSquared > BURST_RANGE * BURST_RANGE) {
            this.startDoraSniper(level, player);
        } else this.skillSuppress(level, target);
    }

    private Vec3 doraMuzzle() {
        return new Vec3(this.getX(), this.getEyeY() - 0.15, this.getZ());
    }

    private void aimDoraSniper(Player target) {
        Vec3 offset = target.getBoundingBox().getCenter().subtract(this.doraMuzzle());
        this.doraSniperDirection = offset.lengthSqr() > 1.0E-6 ? offset.normalize() : this.getLookAngle();
    }

    private void startDoraSniper(ServerLevel level, Player target) {
        if (!this.equipDoraGun(true) || !this.doraSniper.begin(target.getUUID(), this.isImpossibleChallenge())) return;
        this.doraSniperNext = false;
        this.aimDoraSniper(target);
        this.entityData.set(DATA_SKILL_CUE, new CompoundTag());
        this.playCast(DoraSniperCycle.CHARGE_TICKS, 1);
        this.getNavigation().stop();
        this.syncDoraSniperCue(false);
        level.playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.0F, 1.2F);
        target.displayClientMessage(Component.translatable("message.ftboss.dora_sniper.charge"), true);
        FTBossMod.LOGGER.info("[FTBoss][Dora] 聚能枪蓄力狙击: {} tick, 最后 {} tick 锁定方向, 目标={}",
                DoraSniperCycle.CHARGE_TICKS,
                DoraSniperCycle.CHARGE_TICKS - DoraSniperCycle.TRACK_TICKS,
                target.getName().getString());
    }

    private void syncDoraSniperCue(boolean fired) {
        Vec3 start = this.doraMuzzle();
        Vec3 end = this.clipSkillRay(start, start.add(this.doraSniperDirection.scale(DoraSniperCycle.RANGE)));
        int duration = DoraSniperCycle.cueTicks(fired, this.isImpossibleChallenge());
        this.syncBeamCue(start, end, DoraSniperCycle.RADIUS, duration, fired, fired);
        CompoundTag cue = this.getSkillCue().copy();
        cue.putBoolean("doraSniper", true);
        cue.putBoolean("doraLocked", this.doraSniper.locked());
        this.entityData.set(DATA_SKILL_CUE, cue);
    }

    private void tickDoraSniper(ServerLevel level, Player target) {
        var step = this.doraSniper.tick(target.getUUID(), target.level() == this.level()
                && DoraSniperCycle.inRange(this.distanceToSqr(target)));
        if (step == DoraSniperCycle.Step.CANCELLED
                || step == DoraSniperCycle.Step.FINISHED) {
            this.cancelDoraSniper();
            return;
        }
        this.getNavigation().stop();
        if (this.getDoraSpearTicks() <= 0) this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        if (step == DoraSniperCycle.Step.TRACKING
                || step == DoraSniperCycle.Step.LOCK) this.aimDoraSniper(target);
        Vec3 sight = this.doraMuzzle().add(this.doraSniperDirection.scale(DoraSniperCycle.RANGE));
        this.getLookControl().setLookAt(sight.x, sight.y, sight.z, 90.0F, 90.0F);
        if (step == DoraSniperCycle.Step.LOCK) {
            level.playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.HOSTILE, 1.2F, 1.8F);
            target.displayClientMessage(Component.translatable("message.ftboss.dora_sniper.lock"), true);
        }
        if (step == DoraSniperCycle.Step.FIRE) this.fireDoraSniper(level);
        else if (this.doraSniper.charging()) this.syncDoraSniperCue(false);
    }

    private void fireDoraSniper(ServerLevel level) {
        Vec3 start = this.doraMuzzle();
        Vec3 end = this.clipSkillRay(start, start.add(this.doraSniperDirection.scale(DoraSniperCycle.RANGE)));
        LivingEntity victim = null;
        double nearest = start.distanceToSqr(end);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(start, end).inflate(DoraSniperCycle.RADIUS),
                entity -> entity != this && entity.isAlive() && !entity.isSpectator())) {
            var bounds = candidate.getBoundingBox().inflate(DoraSniperCycle.RADIUS);
            Vec3 intersection = bounds.contains(start) ? start : bounds.clip(start, end).orElse(null);
            if (intersection != null && start.distanceToSqr(intersection) < nearest) {
                nearest = start.distanceToSqr(intersection);
                end = intersection;
                victim = candidate;
            }
        }
        boolean hit = false;
        if (victim instanceof Player player && this.combatPlayer(player) && !this.isAlliedTo(player) && this.hasLineOfSight(player)) {
            hit = net.farmingtales.ftboss.skill.DoraSniperDamage.hurt(player, this);
            if (hit) {
                this.doraKnockback(player, 1.1, 0.16);
                this.doraHit(player);
            }
        }
        if (!hit) this.doraTempo.miss();
        this.syncDoraSniperCue(true);
        CompoundTag cue = this.getSkillCue().copy();
        cue.putDouble("endX", end.x);
        cue.putDouble("endY", end.y);
        cue.putDouble("endZ", end.z);
        this.entityData.set(DATA_SKILL_CUE, cue);
        this.playAction(this.getChallengeDifficulty().acceleratedTicks(DoraSniperCycle.RECOVERY_TICKS));
        WeaponFx.send(level, Kind.DORA_IMPACT, Theme.ARC, end, end, 0.85, this.getYRot());
        net.farmingtales.ftboss.balance.ManorExpansionHooks.bullet(this, level, this.getTarget());
        level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 0.9F, 1.6F);
        FTBossMod.LOGGER.info("[FTBoss][Dora] 聚能枪狙击释放: 命中={}, 射程={}", hit,
                DoraSniperCycle.RANGE);
    }

    private void cancelDoraSniper() {
        boolean active = this.doraSniper.busy() || this.getSkillCue().getBoolean("doraSniper");
        this.doraSniper.reset();
        this.doraSniperDirection = Vec3.ZERO;
        if (!active) return;
        this.entityData.set(DATA_SKILL_CUE, new CompoundTag());
        this.entityData.set(DATA_COMBAT_ANIMATION, new CompoundTag());
        this.actionTicks = 0;
        this.castTicks = 0;
        if (!this.level().isClientSide && this.getVariant() == DORA_VARIANT && this.isAlive()) this.equipDoraGun(false);
    }

    private void clearDoraTempo() {
        boolean active = this.doraTempo.stacks() > 0;
        this.doraTempo.reset();
        this.removeEffect(net.farmingtales.ftboss.effect.FTEffects.LETHAL_TEMPO.get());
        if (active && this.getVariant() == DORA_VARIANT) {
            this.bossBar.setName(Component.literal("§c" + NAMES[DORA_VARIANT]));
        }
    }

    private void doraHit(Player player) {
        if (this.getVariant() != DORA_VARIANT || !this.combatPlayer(player) || this.isNoAi()
                || this.isMini() || !(this.level() instanceof ServerLevel serverLevel)) return;
        int previous = this.doraTempo.stacks();
        int stacks = this.doraTempo.hit(player.getUUID(), serverLevel.getGameTime());
        if (stacks <= 0) {
            this.removeEffect(net.farmingtales.ftboss.effect.FTEffects.LETHAL_TEMPO.get());
            this.bossBar.setName(Component.literal("§c" + NAMES[DORA_VARIANT]));
            return;
        }
        this.addEffect(new MobEffectInstance(net.farmingtales.ftboss.effect.FTEffects.LETHAL_TEMPO.get(),
                DoraCombatTempo.DURATION, stacks - 1, false, true, true));
        this.bossBar.setName(Component.literal("§c" + NAMES[DORA_VARIANT] + " §6[致命节奏 " + stacks + "/6]"));
        if (previous != stacks) {
            Vec3 tempoFlash = this.position().add(0, 1.0, 0);
            WeaponFx.send(serverLevel, Kind.DORA_IMPACT, Theme.GOLD, tempoFlash, tempoFlash, 0.52, this.getYRot());
            FTBossMod.LOGGER.info("[FTBoss][Dora] 致命节奏 {}/6: 攻速x{}, 冷却x{}, 连射{}发",
                    stacks, this.doraTempo.attackSpeedMultiplier(), this.doraTempo.cooldownMultiplier(),
                    this.doraTempo.burstShots(BURST_SHOTS));
        }
    }

    private void doraKnockback(Player player, double strength, double lift) {
        Vec3 direction = new Vec3(player.getX() - this.getX(), 0, player.getZ() - this.getZ());
        if (direction.lengthSqr() < 1.0E-6) {
            Vec3 look = this.getLookAngle();
            direction = new Vec3(look.x, 0, look.z);
            if (direction.lengthSqr() < 1.0E-6) direction = new Vec3(0, 0, 1);
        }
        direction = direction.normalize();
        net.farmingtales.ftboss.util.BossKnockback.push(player, direction.x * strength, lift, direction.z * strength);
        player.hurtMarked = true;
    }

    private void doraCombatTick() {
        LivingEntity target = this.getTarget();
        if (this.isDeadOrDying() || this.isNoAi() || this.isMini()
                || !(target instanceof Player player) || !this.combatPlayer(player)) {
            this.clearDoraCombat();
            return;
        }
        int previous = this.doraTempo.stacks();
        this.doraTempo.tick(this.level().getGameTime());
        if (previous > 0 && (this.doraTempo.stacks() == 0
                || !this.hasEffect(net.farmingtales.ftboss.effect.FTEffects.LETHAL_TEMPO.get()))) {
            this.clearDoraTempo();
        }
        int spearTicks = this.getDoraSpearTicks();
        if (spearTicks > 0) this.entityData.set(DATA_DORA_SPEAR_TICKS, spearTicks - 1);
        if (this.spearHitCd > 0) this.spearHitCd--;
        this.tickDoraSpear((ServerLevel) this.level(), player);
        if (this.doraSniper.busy()) {
            this.tickDoraSniper((ServerLevel) this.level(), player);
            return;
        }
        if (this.burstShots > 0 && this.actionTicks < 4) this.playAction(8);
    }

    private void tickDoraSpear(ServerLevel level, Player player) {
        if (!new ResourceLocation(DUOLA_MELEE_ID).equals(ForgeRegistries.ITEMS.getKey(this.getOffhandItem().getItem()))
                || !this.hasLineOfSight(player) || this.distanceToSqr(player) > 100
                || this.doraSpearTarget != null && !this.doraSpearTarget.equals(player.getUUID())) {
            this.doraSpearLeft = 0;
            this.doraSpearGap = 0;
            this.doraSpearTarget = null;
            return;
        }
        if (this.doraSpearLeft <= 0) {
            if (this.spearHitCd > 0) return;
            this.doraSpearLeft = net.farmingtales.ftboss.util.ManorBossCombatRules.DORA_SPEAR_HITS;
            this.doraSpearTarget = player.getUUID();
            this.doraSpearGap = 0;
        }
        if (this.doraSpearGap-- > 0) return;
        Vec3 toward = player.getBoundingBox().getCenter().subtract(this.getEyePosition());
        Vec3 impulse = toward.multiply(1, 0, 1).normalize().scale(1.125);
        this.setDeltaMovement(impulse.x, this.getDeltaMovement().y, impulse.z);
        this.hasImpulse = true;
        this.entityData.set(DATA_DORA_SPEAR_TICKS, 4);
        this.swing(net.minecraft.world.InteractionHand.OFF_HAND);
        this.spearChargeHit(level, toward);
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.8F, 1.2F);
        this.doraSpearLeft--;
        this.doraSpearGap = this.getChallengeDifficulty().acceleratedTicks(
                net.farmingtales.ftboss.util.ManorBossCombatRules.DORA_SPEAR_INTERVAL) - 1;
        if (this.doraSpearLeft <= 0) {
            this.spearHitCd = this.getChallengeDifficulty().acceleratedTicks((int) Math.ceil(
                    net.farmingtales.ftboss.util.ManorBossCombatRules.DORA_SPEAR_COOLDOWN * this.doraTempo.cooldownMultiplier()));
            this.doraSpearTarget = null;
        }
    }

    private void startReload(ServerLevel sl) {
        this.cancelDoraSniper();
        this.reloadTicks = RELOAD_TICKS;
        this.doraReloadProgress = 0;
        this.burstAcc = 0;
        this.actionTicks = 0;
        this.entityData.set(DATA_COMBAT_ANIMATION, new CompoundTag());
        this.entityData.set(DATA_DORA_RELOADING, true);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.CROSSBOW_LOADING_END, SoundSource.HOSTILE, 1.2f, 0.9f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.HOSTILE, 1.2f, 1.0f);
        FTBossMod.LOGGER.info("[FTBoss][Dora] 双持装弹冲刺: 基础{}tick, 冷却x{}",
                RELOAD_TICKS, this.doraTempo.cooldownMultiplier());
    }

    private void reloadTick() {
        if (this.reloadTicks <= 0) return;
        if (!(this.level() instanceof ServerLevel sl)) { this.reloadTicks = 0; return; }
        this.doraReloadProgress += this.getChallengeDifficulty().attackRate() / this.doraTempo.cooldownMultiplier();
        int elapsed = (int) (this.doraReloadProgress + 1.0E-9);
        this.doraReloadProgress -= elapsed;
        this.reloadTicks = Math.max(0, this.reloadTicks - elapsed);
        LivingEntity t = this.getTarget();
        if (t != null && t.isAlive()) {
            Vec3 d = t.position().subtract(this.position());
            if (d.lengthSqr() > 1.0) {
                Vec3 dir = d.normalize();
                if (this.getDoraSpearTicks() <= 0) {
                    this.setDeltaMovement(dir.x * 1.05, Math.max(0.05, dir.y * 0.30), dir.z * 1.05);
                }
                this.hasImpulse = true;
                this.setYRot((float) Math.toDegrees(Math.atan2(-dir.x, dir.z)));
                this.yRotO = this.getYRot();
            }
        }
        if (this.reloadTicks <= 0) this.finishReload(sl);
    }

    private void spearChargeHit(ServerLevel sl, Vec3 toward) {
        Vec3 dir = toward.lengthSqr() < 1.0E-6 ? this.getLookAngle() : toward.normalize();
        Vec3 start = this.getEyePosition();
        Vec3 end = this.clipSkillRay(start, start.add(dir.scale(net.farmingtales.ftboss.util.ManorBossCombatRules.DORA_SPEAR_REACH)));
        float spearDamage = net.farmingtales.ftboss.util.DoraSpearDamageRules.damage(
                SPEAR_CHARGE_DAMAGE, this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        int hits = 0;
        for (Player p : sl.players()) {
            if (!this.combatPlayer(p) || this.isAlliedTo(p) || !this.hasLineOfSight(p)
                    || !p.getBoundingBox().inflate(0.45).contains(start)
                    && p.getBoundingBox().inflate(0.45).clip(start, end).isEmpty()) continue;
            /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
            p.invulnerableTime = 0;
            if (!p.hurt(this.damageSources().mobAttack(this), spearDamage)) continue;
            net.farmingtales.ftboss.util.BossKnockback.push(p, dir.x * SPEAR_KNOCKBACK, SPEAR_KNOCKBACK_UP, dir.z * SPEAR_KNOCKBACK);
            p.hurtMarked = true;
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 1));
            this.doraHit(p);
            hits++;
        }
        if (hits <= 0) return;
        this.entityData.set(DATA_DORA_SPEAR_TICKS, 4);
        FTBossMod.LOGGER.info("[FTBoss][Dora] 六格三连长枪突刺: 命中 {} 人 x {} 伤害",
                hits, spearDamage);
    }

    private void finishReload(ServerLevel sl) {
        this.reloadTicks = 0;
        this.doraReloadProgress = 0;
        this.entityData.set(DATA_DORA_RELOADING, false);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundSource.HOSTILE, 1.2f, 1.2f);
        FTBossMod.LOGGER.info("[FTBoss][Dora] 冲锋枪装弹完成，保持长枪副手，下一轮可切换聚能枪狙击");
    }

    /** v62: 柠檬猫当前还活着几个帮手(用来判断"必须召唤"还是"可以翻锅") */
    private int aliveSummons() {
        return aliveSummonBosses();
    }

    public boolean isLemonDirectSummon(Entity entity) {
        return this.getVariant() == LEMONCAT_VARIANT && this.summons.contains(entity.getUUID());
    }

    /**
     * 柠檬猫当前还活着几个**助战 Boss**（没召唤物时返回 0）。
     *
     * <p>★ v171：由 {@code LemonSacrifice} 的献祭闸门使用（用户要求
     * 「献祭技能只会在场上召唤的 boss 有四个才会献祭」）。判定与 {@code aliveSummons()}
     * **完全同一套实现** —— 原来两处各算一遍，很容易将来改一处漏一处。
     */
    public int aliveSummonBosses() {
        if (!(this.level() instanceof ServerLevel sl)) return 0;
        int count = 0;
        for (java.util.UUID id : new java.util.HashSet<>(this.summons)) {
            if (this.lemonSacrifice.countsAsSummonedBoss(sl.getEntity(id))) count++;
        }
        return count;
    }

    /**
     * 柠檬猫降到半血时仍未完成献祭的保底：条件尚未凑齐就立刻换出完整的两位助战，
     * 并在同一 tick 献祭，不再等待下一次原生技能冷却。若生成失败导致不足两位，
     * 本次不消耗唯一献祭机会，后续 tick 会继续重试。
     */
    private boolean tryHalfHealthLemonSacrifice(ServerLevel sl) {
        if (!this.lemonSacrifice.halfHealthFallbackReady()) return false;
        int before = this.aliveSummonBosses();
        if (before < this.lemonSacrifice.requiredBosses()) {
            this.skillSummonFoodBosses(sl, this.getTarget());
        }
        if (!this.lemonSacrifice.trySacrifice(this.summons)) return false;
        this.summons.clear();
        this.summonLife = 0;
        FTBossMod.LOGGER.info("[FTBoss][Lemon] 半血保底献祭已触发: 原有助战={}，已按参战人数召齐并完成献祭", before);
        return true;
    }

    private void skillSummonFoodBosses(ServerLevel sl, LivingEntity target) {
        if (!this.lemonSacrifice.canSummonHelpers()) return;
        playCast(HiddenBossCastRules.quicker(45), 3);
        this.clearSummons(sl);
        /* 2026-09-12 v42: 直接引用注册对象(以前按字符串 id 查, 一旦查不到就静默失败) */
        java.util.List<net.minecraft.world.entity.EntityType<?>> types = new java.util.ArrayList<>();
        types.add(FTEntities.SUGAR_QUEEN.get());
        types.add(FTEntities.MEAT_MOUNTAIN.get());
        types.add(FTEntities.TEA_TREE.get());
        types.add(FTEntities.WINE_BARREL.get());
        types.add(FTEntities.RAT_BOSS.get());
        /* 2026-09-13 v75: 苹果爵士从助战名单里**移除**(会触发头衔共鸣/血腥契约把庄园刷回来) */        int made = 0;
        for (int i = 0; i < this.lemonSacrifice.summonCount(); i++) {
            try {
                net.farmingtales.ftboss.boss.ai.LemonCombatBalance.refillBossPool(types);
                if (types.isEmpty()) break;
                var type = types.remove(this.random.nextInt(types.size()));
                var e = type.create(sl);
                if (e == null) {
                    FTBossMod.LOGGER.warn("[FTBoss][Easter] 召唤食物系 Boss 失败(create 返回 null): {}", type);
                    continue;
                }
                double ang = this.random.nextDouble() * Math.PI * 2.0;
                double x = this.getX() + Math.cos(ang) * 3.5;
                double z = this.getZ() + Math.sin(ang) * 3.5;
                e.moveTo(x, this.getY(), z, this.random.nextFloat() * 360.0f, 0.0f);
                /* 2026-09-12 v50 按用户反馈「柠檬猫的召唤物不是 boss 而是假实体」:
                   以前这里只是 type.create() 出来的**裸实体** ——
                     · 屠夫没有名字(显示成未翻译的 entity.ftboss.meat_mountain)、也没有 AI 目标注册,
                       而 BossHandler 的"无主屠夫清理"会把**没有 cloneButcher 标记**的屠夫立刻 discard 掉
                       (日志证据: create 出来的 meat_mountain 随后就不见了) -> 玩家眼里就是"假实体";
                     · 女皇没有 cloneQueen 标记 -> 简化 AI 不开, 站着不动。
                   现在: 走**克隆体**那条合法路径 + 正确译名 + 满血 + 强制保留 + 锁定玩家。 */
                if (e instanceof Mob m) {
                    m.setCustomName(Component.literal("§c§l").append(summonBossName(type))
                            .append(Component.literal("§7·助战")));
                    m.setCustomNameVisible(true);
                    /* 2026-09-13 v88 按用户要求「柠檬召唤的 boss 会被加强 ... 血量加 100」:
                       改的是 MAX_HEALTH **基础值** -> 之后 BossScaling 的人数倍率照样作用在它上面,
                       而且必须在 addFreshEntity 之前改, 否则 BossScaling 第一次 tick 会把旧血量记成基准。 */
                    var hp = m.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                    if (hp != null) {
                        hp.setBaseValue(hp.getBaseValue() + SUMMON_EXTRA_HEALTH);
                        m.setHealth((float) hp.getValue());
                    }
                    if (m instanceof SugarQueenEntity q) q.setCloneQueen(true);        // 女皇: 开简化 AI
                    if (m instanceof MeatMountainEntity b) b.setCloneButcher(true);    // 屠夫: 开克隆 AI, 且不会被当残留清掉
                    m.setPersistenceRequired();
                    if (target != null) m.setTarget(target);
                    /* 2026-09-13 v88 按用户要求「... 增加随机 5 个 buff」:
                       从 {@link #CHAIN_BUFF_POOL} 里**不重复**抽 5 个, 等级走 {@code buffAmp}(麻雀在场再 +1),
                       寿命与助战一致(到点随助战一起收回, 不会留着 buff 的鬼影)。 */
                    String extraBuffs = applySummonBuffs(m);
                    FTBossMod.LOGGER.info("[FTBoss][Easter] 助战强化: {} 血量 +{} -> {}, {} 个增益: {}",
                            type.getDescription().getString(), (int) SUMMON_EXTRA_HEALTH,
                            hp != null ? (int) hp.getValue() : -1, SUMMON_EXTRA_BUFFS, extraBuffs);
                }
                /* ★ 2026-09-13 晚按用户要求「柠檬猫召唤的 boss 不能放音乐, 防止音乐冲突」:
                   这五位都是**完整的食物系 Boss**, 各自带专属曲目, 一进场就会跟庄园战的
                   双轨套曲抢 BGM 调度权。打上标记后 BossMusic 会统一拒绝它们(见 TAG_NO_BGM)。 */
                net.farmingtales.ftboss.sound.BossMusic.markNoBgm(e);
                net.farmingtales.ftboss.boss.ai.LemonSummonRewards.mark(e);
                net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, e);
                if (this.lemonSacrifice.used()) {
                    /* ★ 2026-09-21: 连"召唤者是谁"一起烙上（只打标记的话，死亡回调那条主人链路可能指向上层，
                       三只的计数就永远加不上）。 */
                    net.farmingtales.ftboss.boss.ai.LemonSummonRewards.markPostSacrificeHelper(e, this.getUUID());
                }
                if (!sl.addFreshEntity(e)) continue;
                this.summons.add(e.getUUID());
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, this.getY() + 1.0, z,
                        25, 0.5, 0.7, 0.5, 0.08);
                sl.playSound(null, x, this.getY(), z, SoundEvents.EVOKER_CAST_SPELL,
                        SoundSource.HOSTILE, 1.3f, 0.9f);
                made++;
                FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫召唤: {} @{},{},{}",
                        type.getDescription().getString(), (int) x, (int) this.getY(), (int) z);
            } catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] 召唤食物系 Boss 异常: {}", t.toString());
            }
        }
        this.summonLife = made > 0 ? SUMMON_LIFE_TICKS : 0;
        FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫喊来 {} 位食物系 Boss 助战(持续 {} 秒)",
                made, SUMMON_LIFE_TICKS / 20);
    }

    /**
     * 2026-09-13 v88 按用户要求「柠檬召唤的 boss 会被加强 ... 增加随机 5 个 buff」:
     * 给刚召唤出来的助战**不重复**地抽 {@value #SUMMON_EXTRA_BUFFS} 个增益, 等级走 {@link #buffAmp}
     * (麻雀在场再 +1 层), 寿命 = 助战寿命 {@link #SUMMON_LIFE_TICKS}。
     *
     * @return 喂了哪几个增益(空格分隔, 写日志用)
     */
    private String applySummonBuffs(Mob m) {
        /* Fisher-Yates 洗牌(用实体自己的 random, 不额外 new Random) */
        int[] idx = new int[CHAIN_BUFF_POOL.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        for (int i = idx.length - 1; i > 0; i--) {
            int j = this.random.nextInt(i + 1);
            int tmp = idx[i]; idx[i] = idx[j]; idx[j] = tmp;
        }
        StringBuilder sb = new StringBuilder();
        int n = Math.min(SUMMON_EXTRA_BUFFS, idx.length);
        for (int k = 0; k < n; k++) {
            var eff = CHAIN_BUFF_POOL[idx[k]];
            /* 不安静模式: 让玩家一眼看出"这只助战被加强了"(有粒子 + 图标) */
            m.addEffect(new MobEffectInstance(eff, SUMMON_LIFE_TICKS, this.buffAmp(1), true, true, true));
            sb.append(eff.getDisplayName().getString()).append(' ');
        }
        return sb.toString().trim();
    }

    /** 召唤用的正式译名(2026-09-12 v50): 用已有的 boss.* 键, 免得显示成未翻译的实体 id */
    private static Component summonBossName(net.minecraft.world.entity.EntityType<?> type) {
        if (type == FTEntities.SUGAR_QUEEN.get()) return Component.translatable("boss.ftboss.sugar_queen");
        if (type == FTEntities.MEAT_MOUNTAIN.get()) return Component.translatable("boss.ftboss.butcher");
        if (type == FTEntities.TEA_TREE.get()) return Component.translatable("boss.ftboss.saya");
        if (type == FTEntities.WINE_BARREL.get()) return Component.translatable("boss.ftboss.wine_barrel");
        if (type == FTEntities.RAT_BOSS.get()) return Component.translatable("boss.ftboss.rat_swarm");
        return Component.translatable("entity.ftboss.apple_boss");
    }

    /**
     * 柠檬猫献祭后直接召出的助战 Boss 真正死亡时由 {@code LemonSummonRewards} 回调。
     *
     * <p>★ 2026-09-21：不再要求"UUID 必须还在 {@code summons} 清单里"才计数 ——
     * 那份清单是**清理用**的（超时/换批/主死），会被 {@code clearSummons} 与存档重载动过；
     * 而"玩家是否真的打死了三只助战"应当只由死亡事件 + 烙在助战身上的召唤者 UUID 决定
     * （见 {@code LemonSummonRewards.POST_SACRIFICE_OWNER_TAG}）。这里仍然顺手把清单里的它摘掉。
     */
    public boolean onPostSacrificeHelperDefeated(Entity helper) {
        if (helper == null || this.getVariant() != LEMONCAT_VARIANT || !this.lemonSacrifice.used()) return false;
        this.summons.remove(helper.getUUID());
        if (this.aliveSummons() == 0) this.summonLife = 0;
        return this.lemonSacrifice.recordPostSacrificeHelperDefeat();
    }

    /** 收回柠檬猫喊来的帮手(超时/退场/死亡都走这里) */
    private void clearSummons(ServerLevel sl) {
        if (sl != null && !this.summons.isEmpty()) {
            for (java.util.UUID id : new java.util.ArrayList<>(this.summons)) {
                var e = sl.getEntity(id);
                if (e instanceof LivingEntity le && le.isAlive()) {
                    sl.sendParticles(ParticleTypes.CLOUD, le.getX(), le.getY() + 1.0, le.getZ(),
                            20, 0.4, 0.6, 0.4, 0.05);
                    le.discard();
                }
            }
        }
        this.summons.clear();
        this.summonLife = 0;
    }

    /**
     * 青衣 招牌机制「四象轮转」(2026-09-12 v20): 换下一件 TnS 独特武器 + 主题自身增益 + 换装特效。
     * 主手真的是换物品, 所以玩家能看到武器在变(颜色/模型都不同)。
     */
    private void rotateQingyiWeapon(ServerLevel sl) {
        this.qingyiIndex = (this.qingyiIndex + 1) % QINGYI_WEAPONS.length;
        String id = QINGYI_WEAPONS[this.qingyiIndex];
        if (!(this.getMainHandItem().getItem() instanceof QingyiConfluenceTridentItem)) this.equipWeapon();
        switch (this.qingyiIndex) {
            case 0:
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 8 * 20, 1));
                break;
            case 1:
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 8 * 20, 1));
                break;
            case 2:
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 8 * 20, 1));
                break;
            case 3:
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 8 * 20, 0));
                break;
            default:
                break;
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.HOSTILE, 1.0f, 1.2f);
        sl.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 1.2, this.getZ(),
                40, 0.6, 0.7, 0.6, 0.4);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣「四象轮转」第 {} 件 -> {} (实际主手={})",
                this.qingyiIndex + 1, id, this.getMainHandItem());
    }

    /** 文玄蓝 被动「飞轮绞杀」(2026-09-12 v21): 每 1 秒对 {@link #GRIND_RADIUS} 格内玩家各 6 点 */
    private void flywheelGrind(ServerLevel sl) {
        double x = this.getX(), y = this.getY(), z = this.getZ();
        int hits = 0;
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.distanceToSqr(x, y, z) > GRIND_RADIUS * GRIND_RADIUS) continue;
            /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), 100.56F);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2 * 20, 1));
            hits++;
        }
        sl.sendParticles(ParticleTypes.CRIT, x, y + 0.9, z, 10, 0.9, 0.5, 0.9, 0.12);
        if (hits > 0) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2 * 20, 1));
            sl.playSound(null, x, y, z, SoundEvents.GRINDSTONE_USE, SoundSource.HOSTILE, 1.0f, 1.5f);
        }
    }

    /** 文玄蓝 招牌机制「蒸汽泄压」(2026-09-12 v21): 正面 6 格蒸汽锥 —— 20 伤害 + 击退 + 缓慢 II 6 秒 */
    private void skillSteamVent(ServerLevel sl, LivingEntity target) {
        if (net.farmingtales.ftboss.integ.ManorSpellBridge.replace(this, target, "pressure_lance")) return;
        playCast(HiddenBossCastRules.quicker(28), 2);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        Vec3 look = this.getLookAngle();
        Vec3 from = this.position().add(0.0, 1.0, 0.0);
        for (double d = 1.0; d <= VENT_RANGE; d += 0.5) {
            Vec3 c = from.add(look.scale(d));
            sl.sendParticles(ParticleTypes.CLOUD, c.x, c.y, c.z, 4, 0.2, 0.2, 0.2, 0.05);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y, c.z, 1, 0.15, 0.15, 0.15, 0.02);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.4f, 0.7f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 0.9f, 0.8f);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!p.isAlive()) continue;
            Vec3 to = p.position().add(0.0, p.getBbHeight() * 0.5, 0.0).subtract(from);
            double dist = to.length();
            if (dist > VENT_RANGE || dist < 0.2) continue;
            if (to.normalize().dot(look) < 0.82) continue;      // ±35 度锥
            /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(p, 335.2F, 1.5));
            net.farmingtales.ftboss.util.BossKnockback.push(this, p, look.x * VENT_KNOCKBACK, 0.22, look.z * VENT_KNOCKBACK);
            p.hurtMarked = true;
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int)360, 1));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int)360, 1));
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝「蒸汽泄压」命中 {} 人 ({} 伤害 + 击退)", hits, VENT_DAMAGE);
    }

    /**
     * 文玄蓝 招牌机制「飞轮回旋」(2026-09-12 v22, 按用户要求参考"光之女王的飞弹"):
     * 开火后由 {@link #throwFlywheel(ServerLevel)} 每 {@link #FLYWHEEL_INTERVAL} tick 投出 1 个,
     * 共 {@link #FLYWHEEL_SHOTS} 个 **追踪最近玩家、再返航回自己**的 Create 飞轮。
     */
    private void skillFlywheelVolley(ServerLevel sl, LivingEntity target) {
        playAction(34);
        this.flywheelShots = FLYWHEEL_SHOTS;
        this.flywheelTimer = 0;
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.HOSTILE, 1.0f, 0.7f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝「飞轮回旋」: 即将投出 {} 个追踪飞轮", FLYWHEEL_SHOTS);
    }

    private void grantOpeningFlywheels(ServerLevel level) {
        if (this.getVariant() != 6 || this.isMini() || this.isGiant() || this.isNoAi() || !this.isAlive()
                || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)
                || this.openingFlywheelsGranted >= FlywheelCombatRules.OPENING_COUNT) return;
        if (this.openingFlywheelsRetry > 0) {
            this.openingFlywheelsRetry--;
            return;
        }
        this.openingFlywheelsGranted = FlywheelCombatRules.openingProgress(
                this.openingFlywheelsGranted, this.countFlywheels(level));
        while (this.openingFlywheelsGranted < FlywheelCombatRules.OPENING_COUNT) {
            FlywheelProjectileEntity flywheel = new FlywheelProjectileEntity(level, this);
            flywheel.beginOrbit(Math.PI * 2.0 * this.openingFlywheelsGranted / FlywheelCombatRules.OPENING_COUNT);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, flywheel);
            if (!level.addFreshEntity(flywheel)) {
                this.openingFlywheelsRetry = 20;
                return;
            }
            this.openingFlywheelsGranted++;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝开局飞轮就绪: {} 个", FlywheelCombatRules.OPENING_COUNT);
    }

    /** 投出 1 个飞轮(带一点随机散布, 三个不会叠在一条线上) */
    private void throwFlywheel(ServerLevel sl) {
        this.flywheelTimer = this.getChallengeDifficulty().acceleratedTicks(FLYWHEEL_INTERVAL);
        this.flywheelShots--;
        if (this.countFlywheels(sl) >= net.farmingtales.ftboss.boss.ai.ZhuniPairCombat.flywheelLimit(this)) {
            this.flywheelShots = 0;
            FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝飞轮已达上限 {} 个 -> 停止投掷",
                    net.farmingtales.ftboss.boss.ai.ZhuniPairCombat.flywheelLimit(this));
            return;
        }
        try {
            FlywheelProjectileEntity fw = new FlywheelProjectileEntity(sl, this);
            double yaw = Math.toRadians(this.getYRot() + (this.random.nextDouble() - 0.5) * 50.0);
            double dx = -Math.sin(yaw), dz = Math.cos(yaw);
            fw.setPos(this.getX() + dx * 0.8, this.getEyeY() - 0.15, this.getZ() + dz * 0.8);
            fw.shoot(dx * 0.55, 0.05, dz * 0.55, 1.1f, 0.0f);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, fw);
            sl.addFreshEntity(fw);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1.0f, 1.4f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝投出飞轮 (还剩 {} 个)", this.flywheelShots);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 投掷飞轮失败: {}", t.toString());
            this.flywheelShots = 0;
        }
    }

    /**
     * 文玄蓝: 让**所有环绕中的飞轮一起再扑一轮**(2026-09-12 v41 按用户要求, 每 20 秒一次)。
     * 齐射只是重置它们的状态机(重新追最近的玩家), 40 tick 后自然回到环绕; 命中照旧 16 点、不击退。
     */
    private void flywheelVolley(ServerLevel sl) {
        int n = 0;
        try {
            for (FlywheelProjectileEntity f : sl.getEntitiesOfClass(FlywheelProjectileEntity.class,
                    this.getBoundingBox().inflate(48.0), f -> f.getOwner() == this && f.isOrbiting())) {
                f.launchAgain();
                n++;
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 飞轮齐射异常: {}", t.toString());
        }
        if (n > 0) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_RIPTIDE_2, SoundSource.HOSTILE, 1.2f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝: {} 个飞轮齐射!", n);
        }
    }

    /**
     * 文玄蓝 招牌连招(v71 按用户要求, 触发距离 {@value #BEAM_COMBO_MIN}~{@value #BEAM_COMBO_MAX} 格):
     * <ol>
     *   <li>① **高伤害光束打击** —— 沿直线结算 {@link #BEAM_STRIKE_DAMAGE} 点;</li>
     *   <li>② 把**所有环绕中的飞轮一起扑向玩家**({@link #flywheelVolley});</li>
     *   <li>③ 飞轮攻击力 **+10%**(加法叠加, 上限 {@link #FLYWHEEL_DAMAGE_CAP} 倍, 由飞轮结算时读取)。</li>
     * </ol>
     */
    private void skillBeamCombo(ServerLevel sl, LivingEntity target) {
        playImpactAction(20);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        this.beamStrike(sl, target);
        this.flywheelVolley(sl);
        double before = this.flywheelDamageMul;
        this.flywheelDamageMul = Math.min(FLYWHEEL_DAMAGE_CAP, this.flywheelDamageMul + FLYWHEEL_DAMAGE_STEP);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝 光束连招完成: 飞轮攻击力 x{} -> x{}",
                String.format("%.2f", before), String.format("%.2f", this.flywheelDamageMul));
    }

    /** 高伤害光束打击: 从眼前沿直线打到目标; 到直线 {@link #BEAM_STRIKE_RADIUS} 格内的玩家都吃伤害 */
    private void beamStrike(ServerLevel sl, LivingEntity target) {
        net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(
                this.getX(), this.getEyeY() - 0.15, this.getZ());
        net.minecraft.world.phys.Vec3 to = new net.minecraft.world.phys.Vec3(
                target.getX(), target.getEyeY(), target.getZ());
        to = this.clipSkillRay(from, to);
        net.minecraft.world.phys.Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 0.2) return;
        net.minecraft.world.phys.Vec3 nd = d.normalize();
        /* 光束视觉: 末地烛 + 电火花(每 0.25 格采样一次), 落点再补一把灵魂火 */
        this.syncBeamCue(from, to, BEAM_STRIKE_RADIUS, CUE_RELEASE_TICKS, true, true);
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, to.x, to.y, to.z, 14, 0.3, 0.3, 0.3, 0.05);
        sl.playSound(null, from.x, from.y, from.z, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 1.5f);
        sl.playSound(null, from.x, from.y, from.z, SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 1.1f, 1.6f);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!this.combatPlayer(p) || !this.hasLineOfSight(p)) continue;
            net.minecraft.world.phys.Vec3 rel = p.position().add(0, p.getBbHeight() * 0.5, 0).subtract(from);
            double along = rel.dot(nd);
            if (along < 0.0 || along > len) continue;
            if (rel.subtract(nd.scale(along)).length() > BEAM_STRIKE_RADIUS + p.getBbWidth() * 0.5) continue;
            p.invulnerableTime = 0;                            // v60: 去掉玩家无敌帧
            p.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(p, 251.4F, 2.0));
            net.farmingtales.ftboss.util.BossKnockback.push(this, p, nd.x * BEAM_KNOCKBACK, 0.18, nd.z * BEAM_KNOCKBACK);
            p.hurtMarked = true;
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝「光束打击」命中 {} 人 ({} 伤害)",
                hits, String.format("%.1f", BEAM_STRIKE_DAMAGE));
    }

    /** 自己名下还活着几个飞轮(含飞行中与环绕中的) —— 用于 25 个上限判定 */
    private int countFlywheels(ServerLevel sl) {        try {
            return sl.getEntitiesOfClass(FlywheelProjectileEntity.class,
                    this.getBoundingBox().inflate(48.0), f -> f.getOwner() == this).size();
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * 功底「血怒」(2026-09-12 v24 用户定案): 命中玩家 +1 层(最多 {@value #BLOOD_RAGE_MAX} 层),
     * 每层 {@value #BLOOD_RAGE_PER_STACK} 攻击力(MULTIPLY_TOTAL, 5 层满 = +250%), 永久叠到死;
     * **满层时额外 +{@value #BLOOD_RAGE_MAX_SPEED_BONUS} 移动速度**(2026-09-13 v88 用户要求「满层后移速上升」)。
     */
    private void addBloodRage(ServerLevel sl, Player victim) {
        if (this.bloodRage < BLOOD_RAGE_MAX) this.bloodRage++;
        var attr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            attr.removeModifier(BLOOD_RAGE_MOD);
            attr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    BLOOD_RAGE_MOD, "ftboss.blood_rage",
                    BLOOD_RAGE_PER_STACK * this.bloodRage,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        /* 2026-09-13 v88 按用户要求「功底血怒满层后移速上升」: 满层才挂, 没满就保持摘掉的状态 */
        var spdAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        boolean maxed = this.bloodRage >= BLOOD_RAGE_MAX;
        if (spdAttr != null) {
            spdAttr.removeModifier(BLOOD_RAGE_SPEED_MOD);
            if (maxed) {
                spdAttr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        BLOOD_RAGE_SPEED_MOD, "ftboss.blood_rage_speed",
                        BLOOD_RAGE_MAX_SPEED_BONUS,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        /* v119 按用户要求「血怒也是[做成 buff]」: 把层数显示成可见增益(等级 = 层数 - 1),
           攻击力那份仍然是上面的 AttributeModifier —— 这个效果只负责"看得见"。 */
        var rageEff = net.farmingtales.ftboss.effect.FTEffects.BLOOD_RAGE.get();
        if (rageEff != null) {
            this.addEffect(new MobEffectInstance(rageEff,
                    net.farmingtales.ftboss.effect.BloodRageEffect.TICKS,
                    Math.max(0, this.bloodRage - 1), false, true, true));
        }
        double pct = BLOOD_RAGE_PER_STACK * this.bloodRage * 100.0;
        String spdTxt = maxed ? String.format(" · 移速 +%.0f%%", BLOOD_RAGE_MAX_SPEED_BONUS * 100.0) : "";
        sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, this.getX(), this.getY() + 1.2, this.getZ(),
                8, 0.3, 0.4, 0.3, 0.05);
        if (victim != null) {
            victim.displayClientMessage(Component.literal(String.format(
                    "§c功底 血怒 %d/%d §7(+%.0f%% 攻击力 · 吸血 %.0f%% · 真实伤害 %.0f%%%s)",
                    this.bloodRage, BLOOD_RAGE_MAX, pct,
                    BLOOD_RAGE_LIFESTEAL * 100.0, BLOOD_RAGE_TRUE_DAMAGE * 100.0, spdTxt)), true);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 功底 血怒 {}/{} (+{}% 攻击, 吸血 {}%, 真实伤害 {}%{})",
                this.bloodRage, BLOOD_RAGE_MAX, (int) pct,
                (int) (BLOOD_RAGE_LIFESTEAL * 100.0), (int) (BLOOD_RAGE_TRUE_DAMAGE * 100.0), spdTxt);
    }

    /**
     * v71 按用户要求「功底血怒后获得 25% 的吸血和额外 2% 的真实伤害」。
     *
     * <p>在**每一次对玩家造成伤害**后调用(近战 {@link #doHurtTarget}、断罪斩 {@link #executeCleave}),
     * 参数是这一次**实际打掉的血量**:
     * <ul>
     *   <li>吸血: 回复 {@code 实伤 × }{@value #BLOOD_RAGE_LIFESTEAL};</li>
     *   <li>真实伤害: 额外造成 {@code 实伤 × }{@value #BLOOD_RAGE_TRUE_DAMAGE}, 走"无视护甲/抗性"的伤害类型。</li>
     * </ul>
     * 血怒层数为 0(还没叠起来)时什么都不做。
     */
    private void bloodRageOnHit(LivingEntity victim, float dealt) {
        if (this.bloodRage <= 0 || victim == null || dealt <= 0.0f) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        float heal = (float) (dealt * BLOOD_RAGE_LIFESTEAL);
        if (heal > 0.0f && this.getHealth() < this.getMaxHealth()) {
            this.heal(heal);
            sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.7, this.getZ(), 2, 0.3, 0.3, 0.3, 0.0);
        }
        float trueDmg = (float) (dealt * BLOOD_RAGE_TRUE_DAMAGE);
        if (trueDmg > 0.0f) {
            victim.invulnerableTime = 0;
            victim.hurt(net.farmingtales.ftboss.boss.ai.BossDamageTuning.trueDamage(sl, this), trueDmg);
        }
    }

    /* ==================== 雪黎: 粉碎轮持续伤害 + 投汤锅(v76) ==================== */

    /* 2026-09-13 v105 按用户要求「雪黎攻击玩家自带细雪效果」: 每次命中叠**细雪冻结**。
       用的是原版细雪(Powder Snow)那一套 —— 命中就往玩家身上加冻结进度:
         · 有进度就有**结霜覆盖层**(客户端按 Entity.getPercentFrozen() 画, 一眼看得出);
         · 叠到 getTicksRequiredToFreeze()(原版 140 tick)就转**冻结伤害**(Player.tick 里按
           freezeDamage 游戏规则结算);
         · 不在细雪里每 tick 掉 2 点(Entity.baseTick), 所以挨一下约留 2 秒霜、连击才冻实。
       另附 3 秒**缓慢** —— 细雪的减速本来是无形的, 加个缓慢玩家才知道自己中招。
       注意: **不能**写 setIsInPowderSnow(true)(Entity.baseTick 每 tick 会把它清回 false), 只能写 ticksFrozen。 */
    /** 每次命中叠多少冻结进度(略低于 140: 单次只挂霜, 连击两下就冻实) */
    public static final int XUELI_FROST_PER_HIT = 90;
    /** 冻结进度上限 = 原版阈值 + 这个余量 */
    public static final int XUELI_FROST_OVER = 60;

    /** v105: 雪黎的攻击自带细雪 —— 命中即挂霜(叠满转冻结伤害) + 3 秒缓慢 */
    public static void applyXueliFrost(LivingEntity victim) {
        if (victim == null || victim.level().isClientSide) return;
        String who = victim.getName().getString();
        if (!victim.canFreeze()) {                          // 烈焰人这类免疫冻结的不吃(数据包也可能把玩家加进 tag)
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎细雪跳过 -> {} (canFreeze=false)", who);
            return;
        }
        int need = victim.getTicksRequiredToFreeze();
        int before = victim.getTicksFrozen();
        victim.setTicksFrozen(Math.min(before + XUELI_FROST_PER_HIT, need + XUELI_FROST_OVER));
        int after = victim.getTicksFrozen();
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true, true));
        /* v111 诊断: v105 只在"刚好叠满那一下"打日志, 结果整场日志里**一条都没有** —— 分不清是根本没触发,
           还是 before 一直 >= 阈值(玩家身上有别的东西在维持冻结进度)。现在每次都打, 阈值与前后进度都写出来。 */
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎细雪 -> {} (进度 {} -> {} / 阈值 {}{})",
                who, before, after, need, after >= need ? ", 已冻实" : "");
    }

    /** 擀面杖命中: 给玩家挂上 {@value #XUELI_CRUSH_TICKS} tick 的"粉碎轮"(每秒 {@value #XUELI_CRUSH_DAMAGE} 点, 重复命中原地刷新) */
    private void startCrush(Player p) {
        this.crushTicks.put(p.getUUID(), XUELI_CRUSH_TICKS);
        p.displayClientMessage(Component.literal("§6雪黎 §7的擀面杖压住了你: §c粉碎轮 §7("
                + XUELI_CRUSH_TICKS + " tick, 每秒 " + (int) XUELI_CRUSH_DAMAGE + " 点)"), true);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.GRINDSTONE_USE, SoundSource.HOSTILE, 1.2f, 0.6f);
            if (sl.getGameTime() >= this.xueliRollingPinQuoteReadyAt
                    && this.random.nextFloat() < XUELI_ROLLING_PIN_QUOTE_CHANCE) {
                this.xueliRollingPinQuoteReadyAt = sl.getGameTime() + XUELI_ROLLING_PIN_QUOTE_COOLDOWN;
                this.sayXueliDialogue("摩托罗拉哒！");
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎 擀面杖 -> {} 吃到粉碎轮 {} tick, 每秒 {} 点",
                p.getName().getString(), XUELI_CRUSH_TICKS, (int) XUELI_CRUSH_DAMAGE);
    }

    /**
     * 每 tick 推进粉碎轮，每 20 tick 结算一次原版虚空伤害。
     * 仅在结算伤害时清无敌帧，60 tick 内共结算三次。
     */
    private void xueliCrushTick() {
        if (this.crushTicks.isEmpty()) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        var it = this.crushTicks.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            var p = sl.getPlayerByUUID(en.getKey());
            int left = en.getValue() - 1;
            if (p == null || !p.isAlive() || p.isSpectator() || left < 0) {
                it.remove();
                continue;
            }
            en.setValue(left);
            if (left % 20 == 0) {
                p.invulnerableTime = 0;
                net.farmingtales.ftboss.boss.ai.XueliCrushDamage.hurt(p, this.crushDamageSource(sl), XUELI_CRUSH_DAMAGE);
            }
            sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 0.6, p.getZ(), 2, 0.25, 0.2, 0.25, 0.02);
            if (left % 10 == 0) {
                sl.playSound(null, p.getX(), p.getY(), p.getZ(),
                        SoundEvents.GRINDSTONE_USE, SoundSource.HOSTILE, 0.7f, 0.5f);
            }
        }
    }

    private net.minecraft.world.damagesource.DamageSource crushDamageSource(ServerLevel sl) {
        return new net.minecraft.world.damagesource.DamageSource(
                sl.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD), this, this);
    }

    /** 雪黎招牌技能: 把整个**汤锅**扔向目标 */
    private void skillXueli(ServerLevel sl, LivingEntity target) {
        playImpactAction(20);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        try {
            var pot = new net.farmingtales.ftboss.entity.StockpotEntity(sl, this);
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - (this.getEyeY() - 0.2);
            double dz = target.getZ() - this.getZ();
            pot.setPos(this.getX(), this.getEyeY() - 0.2, this.getZ());
            pot.shoot(dx, dy + 2.0, dz, 1.05f, 1.0f);          // 抛物线
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, pot);
            sl.addFreshEntity(pot);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1.1f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎 扔出汤锅 -> {}", target.getName().getString());
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 雪黎投汤锅失败: {}", t.toString());
        }
    }

    /* ==================== 雪黎: 另外两招(v78) ==================== */

    /* ==================== 雪黎 ④⑤「禁疗飞刀 / 自爆分身」(2026-09-13 v103 用户要求) ====================
       用户: 「雪黎现在可以投掷禁疗飞刀, 和召唤类似于酒桶王的自爆分身的分身」。
       · ④ 禁疗飞刀: 一次掷 {@value #XUELI_KNIFE_COUNT} 把, 命中玩家 = 少量伤害 + **禁疗 30 秒**
                     (复用 v96 那套 HealBan: relics:anti_heal 标记 + 拦 LivingHealEvent);
       · ⑤ 自爆分身: 召 {@value #XUELI_CLONE_COUNT} 个**小号雪黎**(走现有 mini 体系: 0.55 倍体型,
                     有自己的血量, 会追人), 头顶引信 {@value #XUELI_CLONE_FUSE} tick 后**自爆**;
                     被玩家提前打死也会炸(弱化版), 所以别贴着它打。 */
    public static final int XUELI_KNIFE_COUNT = 3;
    /** 自爆分身数量 / 引信(tick) / 血量 */
    public static final int XUELI_CLONE_COUNT = 2;
    /** v118 按用户反馈「雪黎不会召唤小雪黎」: 自爆分身原本排在轮换最后一格(default),
        她常常两三个技能就被打死 -> 永远轮不到。现在给它**独立冷却**兜底:
        场上没有活着的分身、且冷却到了就召一批(和 ⑦ 天降瞬砸那种"排第几"无关)。 */
    public static final int XUELI_CLONE_CD = 30 * 20;
    private int xueliCloneCd = 6 * 20;
    public static final int XUELI_CLONE_FUSE = 5 * 20;
    public static final double XUELI_CLONE_HEALTH = 80.0;
    /** 自爆: 半径与基准伤害(按距离衰减到 25%) */
    public static final double XUELI_BOMB_RADIUS = 4.5;
    public static final float XUELI_BOMB_DAMAGE = 26.0f;
    public static final float XUELI_BOMB_DAMAGE_ON_DEATH = 14.0f;

    /** 雪黎 ④「禁疗飞刀」: 朝目标一次掷三把(带散射) */
    private void skillXueliKnives(ServerLevel sl, LivingEntity target) {
        playImpactAction(16);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        int made = 0;
        try {
            for (int i = 0; i < XUELI_KNIFE_COUNT; i++) {
                var knife = new net.farmingtales.ftboss.entity.XueliKnifeEntity(sl, this);
                knife.setPos(this.getX(), this.getEyeY() - 0.2, this.getZ());
                /* 三把之间给一点散射(±0.09 弧度), 免得三把叠成一把 */
                double spread = (i - (XUELI_KNIFE_COUNT - 1) / 2.0) * 0.09;
                double dx = target.getX() - this.getX();
                double dy = target.getEyeY() - (this.getEyeY() - 0.2);
                double dz = target.getZ() - this.getZ();
                double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                double nx = dx / len, nz = dz / len;
                knife.shoot(nx * Math.cos(spread) - nz * Math.sin(spread),
                        dy / len + 0.06,
                        nx * Math.sin(spread) + nz * Math.cos(spread),
                        2.4f, 0.0f);
                net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, knife);
                sl.addFreshEntity(knife);
                made++;
            }
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1.0f, 1.9f);
            sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getEyeY(), this.getZ(),
                    10, 0.3, 0.3, 0.3, 0.12);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「禁疗飞刀」掷出 {} 把 -> {}",
                    made, target.getName().getString());
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 雪黎掷飞刀失败: {}", t.toString());
        }
    }

    /* ==================== v118 雪黎「自爆分身」独立冷却 ==================== */

    /** 每 tick(仅雪黎本体): 场上没有活着的自爆分身且冷却到了 -> 召一批(保证"小雪黎"一定看得到) */
    private void xueliCloneTick(ServerLevel sl) {
        this.xueliCloneCd = Math.min(this.xueliCloneCd, net.farmingtales.ftboss.boss.ai.XueliPhases.cooldown(XUELI_CLONE_CD, this));
        if (this.xueliCloneCd > 0) this.xueliCloneCd--;
        if (this.xueliCloneCd > 0) return;
        if (this.isNoAi() || this.skillCommitted() || this.xueliSlamLeft > 0 || this.xueliSlamGap > 0
                || this.castTicks > 0 || this.actionTicks > 0) return;
        if (this.aliveXueliClones(sl) > 0) return;
        this.xueliCloneCd = net.farmingtales.ftboss.boss.ai.XueliPhases.cooldown(XUELI_CLONE_CD, this);
        this.skillXueliClones(sl, this.getTarget());
    }

    /** 场上还活着几个雪黎的自爆分身(不含自己) */
    private int aliveXueliClones(ServerLevel sl) {
        int n = 0;
        for (EasterBossEntity e : sl.getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(64.0))) {
            if (e == this || !e.isMini() || e.getVariant() != XUELI_VARIANT) continue;
            if (!e.isAlive() || e.isDeadOrDying()) continue;
            n++;
        }
        return n;
    }

    /** 雪黎 ⑤「自爆分身」: 召 {@value #XUELI_CLONE_COUNT} 个小号雪黎, 引信到点自爆 */
    private void skillXueliClones(ServerLevel sl, LivingEntity target) {
        net.farmingtales.ftboss.boss.ai.CombatStatusHooks.grantValor(this);
        playCast(HiddenBossCastRules.quicker(30), 0);
        int made = 0;
        for (int i = 0; i < XUELI_CLONE_COUNT; i++) {
            try {
                EasterBossEntity clone = FTEntities.EASTER_BOSS.get().create(sl);
                if (clone == null) continue;
                double ang = this.random.nextDouble() * Math.PI * 2.0;
                double x = this.getX() + Math.cos(ang) * 3.0;
                double z = this.getZ() + Math.sin(ang) * 3.0;
                clone.moveTo(x, this.getY(), z, this.random.nextFloat() * 360.0f, 0.0f);
                clone.setVariant(XUELI_VARIANT);
                clone.setManorSummonerId(this.manorSummonerId);
                clone.setMini(true);
                clone.setCustomName(Component.literal("§b雪黎的分身"));
                clone.setCustomNameVisible(true);
                var hp = clone.getAttribute(Attributes.MAX_HEALTH);
                if (hp != null) hp.setBaseValue(XUELI_CLONE_HEALTH);
                clone.setHealth((float) XUELI_CLONE_HEALTH);
                clone.setNoAi(false);                  // 迷你分身照样自己走(会追玩家)
                clone.bombFuse = XUELI_CLONE_FUSE;      // 引信
                if (target != null) clone.setTarget(target);
                /* 血条: 分身不该占屏幕上那条 Boss 血条 */
                clone.getBossBar().setVisible(false);
                net.farmingtales.ftboss.boss.BossBars.untrack(clone, clone.getBossBar());
                net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, clone);
                if (!sl.addFreshEntity(clone)) continue;
                sl.sendParticles(ParticleTypes.CLOUD, x, this.getY() + 0.6, z, 18, 0.4, 0.4, 0.4, 0.06);
                made++;
            } catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] 雪黎召分身失败: {}", t.toString());
            }
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.3f, 1.1f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「自爆分身」召出 {} 个(引信 {} 秒)",
                made, XUELI_CLONE_FUSE / 20);
        if (made > 0) this.sayXueliDialogue(net.farmingtales.ftboss.util.XueliDialogueState.CLONES);
    }

    /** 自爆分身的引信: 每 tick 递减, 到点爆炸; 被提前打死走 onDeath 版(弱化) */
    private void bombFuseTick(ServerLevel sl) {
        if (this.bombFuse <= 0) return;
        if (--this.bombFuse > 0) return;
        this.detonate(sl, false);
    }

    /** 自爆: 纯视觉爆炸(不炸方块/不殉爆) + 半径内玩家按距离衰减吃伤害 */
    private void detonate(ServerLevel sl, boolean onDeath) {
        if (this.bombBlown) return;
        this.bombBlown = true;
        double R = onDeath ? XUELI_BOMB_RADIUS * 0.75 : XUELI_BOMB_RADIUS;
        float base = onDeath ? XUELI_BOMB_DAMAGE_ON_DEATH : XUELI_BOMB_DAMAGE;
        double x = this.getX(), y = this.getY() + 0.7, z = this.getZ();
        if (sl != null) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5f, 0.85f);
            net.farmingtales.ftboss.util.Fx.particles(sl, x, y, z, 36, ParticleTypes.EXPLOSION, 2.0, 0.12);
            net.farmingtales.ftboss.util.Fx.particles(sl, x, y, z, 18, ParticleTypes.LARGE_SMOKE, 1.5, 0.06);
            int hits = 0;
            for (Player p : sl.players()) {
                if (!p.isAlive() || p.isSpectator()) continue;
                double d = Math.sqrt(p.distanceToSqr(x, y, z));
                if (d > R) continue;
                p.invulnerableTime = 0;
                p.hurt(this.damageSources().explosion(this, this),
                        (float) (base * Math.max(0.25, 1.0 - d / R)) * (float) BOSS_DAMAGE_MULT);
                applyXueliFrost(p);                                       // v105: 分身自爆也带细雪
                net.farmingtales.ftboss.boss.ai.CombatStatusHooks.xueliHit(p, "bomb");
                net.farmingtales.ftboss.util.BossKnockback.knockback(p, 1.1, x - p.getX(), z - p.getZ());
                hits++;
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎分身自爆({}) 命中 {} 人 (基准 {} 伤害, 半径 {} 格)",
                    onDeath ? "被打死·弱化" : "引信到点", hits,
                    String.format("%.1f", base * BOSS_DAMAGE_MULT), (int) R);
        }
        this.discard();
    }

    /** 雪黎 ②「平底锅横扫」: 半径 4.5 格内玩家吃 {@value #XUELI_PAN_DAMAGE} 伤害 + 点燃 4 秒 + 击退 */
    private void skillXueliPanSlap(ServerLevel sl, LivingEntity target) {
        this.xueliPanWind = this.getChallengeDifficulty().acceleratedTicks(XUELI_PAN_WINDUP);
        playCast(this.xueliPanWind, 1);
        this.beginSkillCue(this.position(), XUELI_PAN_RADIUS, this.xueliPanWind, true);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 0.7f, 1.4f);
    }

    private void executeXueliPanSlap(ServerLevel sl) {
        playImpactAction(16);
        this.releaseSkillCue(this.position());
        double x = this.getX(), y = this.getY(), z = this.getZ();
        sl.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.6f, 0.7f);
        sl.playSound(null, x, y, z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.1f, 1.4f);
        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y + 1.0, z, 14, 2.2, 0.4, 2.2, 0.0);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 0.6, z, 24, 1.8, 0.3, 1.8, 0.06);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!this.combatPlayer(p) || p.distanceToSqr(x, y, z) > XUELI_PAN_RADIUS * XUELI_PAN_RADIUS) continue;
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), XUELI_PAN_DAMAGE);
            applyXueliFrost(p);                                    // v105: 平底锅也带细雪
            double dx = p.getX() - x, dz = p.getZ() - z;
            double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            net.farmingtales.ftboss.util.BossKnockback.push(p, dx / len * 0.8, 0.3, dz / len * 0.8);
            net.farmingtales.ftboss.boss.ai.CombatStatusHooks.xueliHit(p, "pan");
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「平底锅横扫」命中 {} 人 ({} 伤害 + 点燃 4 秒)",
                hits, (int) XUELI_PAN_DAMAGE);
    }

    /** 雪黎 ③「擀面杖压路冲锋」: 朝目标冲 {@value #XUELI_ROLL_TICKS} tick, 撞到的玩家吃伤害 + 挂粉碎轮 */
    private void skillXueliRoll(ServerLevel sl, LivingEntity target) {
        playAction(22);
        this.xueliRollTicks = XUELI_ROLL_TICKS;
        this.xueliRollHitCd = 0;
        this.sayXueliDialogue(net.farmingtales.ftboss.util.XueliDialogueState.ROLLER);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.1f, 1.3f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.MINECART_RIDING, SoundSource.HOSTILE, 0.9f, 1.6f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「擀面杖压路冲锋」开始({} tick)", XUELI_ROLL_TICKS);
    }

    /** 冲锋推进(每 tick): 沿目标方向猛冲, 3 格内玩家吃伤害并被挂上粉碎轮 */
    private void xueliRollTick() {
        if (this.xueliRollTicks <= 0) return;
        if (!(this.level() instanceof ServerLevel sl)) { this.xueliRollTicks = 0; return; }
        this.xueliRollTicks--;
        LivingEntity t = this.getTarget();
        if (t != null && t.isAlive()) {
            net.minecraft.world.phys.Vec3 d = t.position().subtract(this.position());
            if (d.lengthSqr() > 1.0) {
                net.minecraft.world.phys.Vec3 dir = d.normalize();
                this.setDeltaMovement(dir.x * 0.95, Math.max(0.05, dir.y * 0.28), dir.z * 0.95);
                this.hasImpulse = true;
                this.setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
                this.yRotO = this.getYRot();
            }
        }
        if (this.xueliRollHitCd > 0) this.xueliRollHitCd--;
        if (this.xueliRollHitCd <= 0) {
            int hits = 0;
            for (Player p : sl.players()) {
                if (!p.isAlive() || p.distanceToSqr(this) > 9.0) continue;      // 3 格
                p.invulnerableTime = 0;
                p.hurt(this.damageSources().mobAttack(this), XUELI_ROLL_DAMAGE);
                net.farmingtales.ftboss.util.BossKnockback.push(p, (p.getX() - this.getX()) * 0.35, 0.25, (p.getZ() - this.getZ()) * 0.35);
                applyXueliFrost(p);                                            // v105: 压路冲锋也带细雪
                net.farmingtales.ftboss.boss.ai.CombatStatusHooks.xueliHit(p, "roller");
                this.startCrush(p);                                            // 压路机: 直接上粉碎轮
                hits++;
            }
            if (hits > 0) this.xueliRollHitCd = XUELI_ROLL_HIT_CD;
        }
        sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.4, this.getZ(), 4, 0.3, 0.2, 0.3, 0.06);
        if (this.xueliRollTicks <= 0) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 1.0f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎 压路冲锋结束");
        }
    }

    /* ==================== 雪黎 ⑦「天降瞬砸」实现(v111) ==================== */

    /** ⑦「天降瞬砸」起手: 返回 false = 场上一个玩家都没有(调用方改放别的招)。
     *  <p>v125: 一次施放 = {@value #XUELI_SLAM_COMBO} 连砸, 这里只负责"开始这一轮"并放出第 1 发,
     *  后续每一发由 {@link #xueliSlamDown} 倒数 {@value #XUELI_SLAM_GAP_TICKS} tick 后调
     *  {@link #launchXueliSlam} 续上。 */
    private boolean skillXueliSlam(ServerLevel sl, LivingEntity target) {
        Player victim = this.pickSlamVictim(target);
        if (victim == null || !this.canTeleportWith(victim)) return false;
        this.xueliSlamLeft = XUELI_SLAM_COMBO;
        this.xueliSlamGap = 0;
        this.launchXueliSlam(sl, victim);
        return true;
    }

    /** 选一发连砸的目标(目标玩家优先, 否则最近的可打玩家); 没有就返回 null */
    private Player pickSlamVictim(LivingEntity target) {
        return this.manorSummoner();
    }

    /** v125: 放出**一发**下砸 —— 定落点、窜上半空、开始蓄势({@value #XUELI_SLAM_WARN_TICKS} tick)。 */
    private void launchXueliSlam(ServerLevel sl, Player victim) {
        if (!this.canTeleportWith(victim)) {
            this.cancelXueliSlam();
            return;
        }
        int idx = XUELI_SLAM_COMBO - this.xueliSlamLeft + 1;      // 第几下(1..5)
        this.xueliSlamX = victim.getX();
        this.xueliSlamY = victim.getY();
        this.xueliSlamZ = victim.getZ();
        this.xueliSlamWarn = XUELI_SLAM_WARN_TICKS;
        playCast(XUELI_SLAM_WARN_TICKS, 3);
        this.beginSkillCue(new Vec3(this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ),
                XUELI_SLAM_RADIUS, XUELI_SLAM_WARN_TICKS, false);
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setDeltaMovement(0, 0, 0);
        this.hasImpulse = true;
        this.teleportTo(this.xueliSlamX, this.xueliSlamY + XUELI_SLAM_HEIGHT, this.xueliSlamZ);
        this.setXRot(90.0f);                                    // 头朝下盯着落点
        this.fallDistance = 0.0f;
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5f, 0.6f);
        sl.playSound(null, this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ,
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.8f, 1.7f);
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(),
                40, 0.5, 0.5, 0.5, 0.25);
        this.tellNearby(sl, "§b【天降瞬砸 " + idx + "/" + XUELI_SLAM_COMBO + "】§7雪黎窜上半空 —— §c"
                + (XUELI_SLAM_WARN_TICKS / 20.0) + " 秒后砸落, 快离开雪圈!");
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「天降瞬砸」第 {}/{} 发起手: 悬停 {} tick, 落点 ({}, {}, {})",
                idx, XUELI_SLAM_COMBO, XUELI_SLAM_WARN_TICKS,
                (int) this.xueliSlamX, (int) this.xueliSlamY, (int) this.xueliSlamZ);
    }

    private void cancelXueliSlam() {
        this.xueliSlamWarn = 0;
        this.xueliSlamLeft = 0;
        this.xueliSlamGap = 0;
        this.setNoGravity(false);
        this.setXRot(0.0F);
        this.fallDistance = 0.0F;
    }

    private void repairXueliSlam() {
        if (this.xueliSlamLeft > 0 && this.xueliSlamWarn <= 0 && this.xueliSlamGap <= 0) {
            this.cancelXueliSlam();
        }
    }

    /** ⑦ 悬停(每 tick): 钉在半空 + 落点雪花警戒环 + 提示音; 归零就砸下去。
     *  <p>v125: 悬停结束后若还在连砸序列里, 走的是 {@link #xueliSlamGapTick} 的间隔倒数。 */
    private void xueliSlamTick(ServerLevel sl) {
        this.repairXueliSlam();
        if (this.xueliSlamWarn <= 0) { this.xueliSlamGapTick(sl); return; }
        /* 钉住: 关重力 + 清速度 + 每 tick 拉回悬停点(免得寻路/追击把她拽走) */
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setDeltaMovement(0, 0, 0);
        this.hasImpulse = true;
        this.teleportTo(this.xueliSlamX, this.xueliSlamY + XUELI_SLAM_HEIGHT, this.xueliSlamZ);
        this.setXRot(90.0f);
        this.fallDistance = 0.0f;
        int left = this.xueliSlamWarn;
        float prog = 1.0f - (float) left / XUELI_SLAM_WARN_TICKS;        // 0 -> 1
        /* ① 地面警戒圈: 一圈雪花(随进度转起来) + 中心一枚末地烛标记 */
        /* ② 她在半空的位置也要看得见(身上寒气 + 脚下云) */
        if (left % 4 == 0) {
            sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.6, this.getZ(),
                    6, 0.5, 0.3, 0.5, 0.02);
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() - 1.2, this.getZ(),
                    2, 0.3, 0.4, 0.3, 0.02);
        }
        /* ③ 提示音, 音调随进度抬高(催命感)。v125 蓄势只有 1 秒, 所以只在起手那 tick 响一次 */
        if (left % 20 == 0 || left <= 5 && left % 2 == 0) {
            sl.playSound(null, this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ,
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.HOSTILE, 1.4f, 0.7f + prog * 0.7f);
        }
        if (--this.xueliSlamWarn <= 0) this.xueliSlamDown(sl);
    }

    /** v125: 五连砸的**间隔期**(每 tick): 她在地面站 {@value #XUELI_SLAM_GAP_TICKS} tick, 倒数到点就窜上去砸下一发。
     *  <p>间隔期脚下一个淡雪圈提示"还没完"; 目标在这 1 秒里跑了就重新锁他(落点按**新位置**算)。 */
    private void xueliSlamGapTick(ServerLevel sl) {
        if (this.xueliSlamGap <= 0) return;
        if (--this.xueliSlamGap > 0) {
            /* 站桩提示: 脚下几片雪花, 让玩家知道还有下一发 */
            sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.2, this.getZ(),
                    4, 1.2, 0.15, 1.2, 0.01);
            return;
        }
        if (this.xueliSlamLeft <= 0) return;                       // 已经打完了(理论上不会走到)
        Player victim = this.pickSlamVictim(this.getTarget());
        if (victim == null) {                                      // 场上没人了 -> 收招
            this.xueliSlamLeft = 0;
            this.xueliSlamGap = 0;
            this.setNoGravity(false);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「天降瞬砸」连砸中断(场上没有目标)");
            return;
        }
        this.launchXueliSlam(sl, victim);
    }

    /** ⑦ 砸落: 落地 AOE —— {@value #XUELI_SLAM_DAMAGE} 伤害 + 眩晕 + 细雪。
     *  <p>v125: 落地后若连砸还没打完, 进入 {@value #XUELI_SLAM_GAP_TICKS} tick 间隔, 然后再砸下一发。 */
    private void xueliSlamDown(ServerLevel sl) {
        this.playImpactAction(14);
        this.releaseSkillCue(new Vec3(this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ));
        this.xueliSlamWarn = 0;
        this.setNoGravity(false);
        this.fallDistance = 0.0f;
        this.teleportTo(this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ);
        this.setXRot(0.0f);
        double x = this.xueliSlamX, y = this.xueliSlamY, z = this.xueliSlamZ;
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 0.4, z, 1, 0.0, 0.0, 0.0, 0.0);
        net.farmingtales.ftboss.util.Fx.particles(sl, x, y + 0.4, z, 60, ParticleTypes.SNOWFLAKE, XUELI_SLAM_RADIUS, 0.35);
        net.farmingtales.ftboss.util.Fx.particles(sl, x, y + 0.4, z, 28, ParticleTypes.CLOUD, 3.2, 0.12);
        net.farmingtales.ftboss.util.Fx.particles(sl, x, y + 0.4, z, 22, ParticleTypes.CRIT, 2.6, 0.25);
        sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.8f, 0.55f);
        sl.playSound(null, x, y, z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.6f, 0.5f);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!this.combatPlayer(p)) continue;
            if (p.distanceToSqr(x, y, z) > XUELI_SLAM_RADIUS * XUELI_SLAM_RADIUS) continue;
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), XUELI_SLAM_DAMAGE);
            applyXueliFrost(p);                                     // v105: 她的招一律带细雪
            net.farmingtales.ftboss.boss.ai.CombatStatusHooks.xueliHit(p, "slam");
            hits++;
        }
        int done = XUELI_SLAM_COMBO - this.xueliSlamLeft + 1;       // 刚砸完的是第几下
        if (this.xueliSlamLeft > 0) this.xueliSlamLeft--;
        boolean more = this.xueliSlamLeft > 0;
        this.tellNearby(sl, hits > 0
                ? ("§b【天降瞬砸 " + done + "/" + XUELI_SLAM_COMBO + "】§7砸中 " + hits + " 人 —— §c眩晕 "
                    + (XUELI_SLAM_STUN_TICKS / 20) + " 秒!" + (more ? " §7还有 " + this.xueliSlamLeft + " 发!" : ""))
                : ("§b【天降瞬砸 " + done + "/" + XUELI_SLAM_COMBO + "】§7砸空了"
                    + (more ? " §7还有 " + this.xueliSlamLeft + " 发!" : "")));
        if (more) {
            this.xueliSlamGap = XUELI_SLAM_GAP_TICKS;               // 落地站 1 秒, 再砸下一发
        } else {
            this.xueliSlamGap = 0;
            /* v182: 「五连砸结束」是纯战报(玩家刚看到砸完), 不发给玩家 */
        }
        if (done == 1) this.sayXueliDialogue(net.farmingtales.ftboss.util.XueliDialogueState.SLAM);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「天降瞬砸」第 {}/{} 发砸落: 命中 {} 人 ({} 伤害 + 眩晕 {} 秒, 半径 {} 格), 剩余 {} 发",
                done, XUELI_SLAM_COMBO, hits, (int) XUELI_SLAM_DAMAGE, XUELI_SLAM_STUN_TICKS / 20,
                (int) XUELI_SLAM_RADIUS, this.xueliSlamLeft);
    }

    /** v111 眩晕: 记一笔(同一玩家取更长的那个) + 记下**定身锚点** + action bar 提示。
     *  <p>★ v121 按用户「下砸的眩晕未生效」: 玩家移动是**客户端权威**的 ——
     *  只清零服务端速度根本拦不住(服务端会照收客户端上报的位置), 所以必须记住锚点、
     *  每 tick 把他**硬拉回**原地(见 {@link #xueliStunTick})。
     *  <p>2026-09-20: 只对**召唤者**生效(雪黎的瞬砸是跟着他走的位移技能);
     *  文玄蓝「超能光束」要对任意参战者生效, 走 {@link #stunPlayerAny}。 */
    private void stunPlayer(Player p, int ticks) {
        if (!this.canTeleportWith(p)) return;
        this.stunPlayerAny(p, ticks, "雪黎的砸击");
    }

    /**
     * 2026-09-20 新增: 对**任意参战玩家**上同一套硬定身眩晕(文玄蓝「超能光束」用)。
     *
     * <p>和 {@link #stunPlayer} 共用 {@code stunTicks} / {@code stunAnchor} 两张表,
     * 所以结算仍然是 {@link #xueliStunTick}(已经不再只给雪黎跑, 见 tick() 里的调用)。
     *
     * @param source 写进 action bar 的来源名
     */
    private void stunPlayerAny(Player p, int ticks, String source) {
        if (p == null || !p.isAlive() || p.isSpectator()) return;
        this.stunTicks.merge(p.getUUID(), ticks, Math::max);
        this.stunAnchor.put(p.getUUID(), new double[]{p.getX(), p.getY(), p.getZ()});
        p.displayClientMessage(
                Component.literal("§b" + source + "§c让你眼冒金星 —— §c眩晕 " + (ticks / 20) + " 秒"), true);
    }

    /**
     * 2026-09-20 按用户要求「加强超能光束击退」: 眩晕 + **解除时甩出去**的合体入口。
     *
     * <p>眩晕期间玩家每 tick 被拉回锚点、水平速度清零, 所以当场推的那一下等于白推 ——
     * 这里把 (dx, dy, dz) 先存进 {@code stunLaunch}, 由 {@link #applyStunLaunch} 在
     * **眩晕解除的那一 tick** 用 {@link net.farmingtales.ftboss.util.BossKnockback} 推出去
     * (会带上玄蓝自己的 {@code XUANLAN_MULTIPLIER}, 和对玩家的水平/竖直折扣)。
     */
    private void stunPlayerWithLaunch(Player p, int ticks, String source, double dx, double dy, double dz) {
        if (p == null || !p.isAlive() || p.isSpectator()) return;
        this.stunPlayerAny(p, ticks, source);
        this.stunLaunch.put(p.getUUID(), new double[]{dx, dy, dz});
    }

    /** 眩晕解除的那一 tick: 把攒着的击退真正推出去(玩家已经没了就丢弃)。 */
    private void applyStunLaunch(Player p) {
        if (p == null) return;
        double[] launch = this.stunLaunch.remove(p.getUUID());
        if (launch == null || !p.isAlive() || p.isSpectator()) return;
        net.farmingtales.ftboss.util.BossKnockback.push(this, p, launch[0], launch[1], launch[2]);
        p.hurtMarked = true;
        p.displayClientMessage(Component.literal("§c眩晕结束 —— §7你被【超能光束】击飞！"), true);
        if (p.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.2, p.getZ(), 14, 0.4, 0.15, 0.4, 0.08);
        }
    }

    /**
     * v111 眩晕结算(每 tick): ① 横向定身(清速度 + 客户端同步) ② 缓慢 VI 短时刷新 ③ 头顶金星粒子。
     * <p>不写成事件监听是有意的: 眩晕跟着**雪黎这个实例**走 —— 她被打死/离场了, 眩晕自然立刻结束。
     */
    private void xueliStunTick(ServerLevel sl) {
        if (this.stunTicks.isEmpty()) return;
        var it = this.stunTicks.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            Player p = sl.getPlayerByUUID(en.getKey());
            int left = en.getValue() - 1;
            /* 2026-09-20: 有效性判定放宽到"任意存活参战玩家" —— 文玄蓝的光束眩晕不再只认召唤者 */
            if (p == null || !p.isAlive() || p.isSpectator() || left <= 0) {
                this.stunAnchor.remove(en.getKey());     // v121: 眩晕结束/玩家没了 -> 锚点一起清
                this.applyStunLaunch(p);                 // 2026-09-20: 眩晕解除的那一 tick 才把攒着的击退甩出去
                it.remove();
                continue;
            }
            en.setValue(left);
            /* ① ★ v121 定身(真定身): 玩家位置是客户端说了算, 所以每 tick **硬拉回锚点**
               (水平 + 竖直都拉着, 免得他一边跳一边走); 3 秒的眩晕就是原地挨打 */
            double[] anchor = this.stunAnchor.get(p.getUUID());
            if (anchor != null && p instanceof ServerPlayer spTp && spTp.connection != null) {
                spTp.connection.teleport(anchor[0], anchor[1], anchor[2], p.getYRot(), p.getXRot());
            }
            /* ② 速度也清掉 + 缓慢顶到 VI(客户端自己也算得动不了) + 停疾跑 + 打断吃东西/拉弓 */
            net.minecraft.world.phys.Vec3 mv = p.getDeltaMovement();
            p.setDeltaMovement(0.0, Math.min(0.0, mv.y), 0.0);
            p.hurtMarked = true;
            p.setSprinting(false);
            p.stopUsingItem();
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 6, false, false, true));
            /* ③ 头顶金星: 绕着脑袋转 */
            double ang = p.tickCount * 0.35;
            for (int i = 0; i < 3; i++) {
                double a = ang + i * (Math.PI * 2.0 / 3.0);
                sl.sendParticles(ParticleTypes.CRIT,
                        p.getX() + Math.cos(a) * 0.55,
                        p.getY() + p.getBbHeight() + 0.3,
                        p.getZ() + Math.sin(a) * 0.55,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    /* ==================== 文玄蓝「超能光束」实现(v79, 2026-09-20 改版) ==================== */

    /**
     * 把光束方向重新指向当前目标。
     *
     * <p>★ 2026-09-20 按用户要求「不要锁头」: 这个"跟头"只允许在**锁定阶段**调用,
     * 前摇与引导期间方向必须冻结 —— 玩家侧移才有意义。v119 那套「每 tick 重新锁定」
     * 已经取消(它让光束变成了打不掉的追踪激光)。
     */
    private void xuanlanAimAtTarget() {
        LivingEntity t = this.getTarget();
        if (t == null || !t.isAlive()) return;
        double x = this.getX(), y = this.getEyeY(), z = this.getZ();
        double ty = t.getY() + t.getBbHeight() * 0.5;
        double dx = t.getX() - x, dy = ty - y, dz = t.getZ() - z;
        double len = Math.max(0.001, Math.sqrt(dx * dx + dy * dy + dz * dz));
        this.xlBeamDx = dx / len;
        this.xlBeamDy = dy / len;
        this.xlBeamDz = dz / len;
    }

    /** 正在锁定/充能/引导光束(此期间不放别的技能、不投飞轮) */
    public boolean xuanlanBeamBusy() {
        return this.xuanlanBeamLock > 0 || this.xuanlanBeamCharge > 0 || this.xuanlanBeamFire > 0;
    }

    /** 锁定 + 前摇的总时长: 预警收缩环走完这条时间正好在发射那一 tick 收满 */
    private int xuanlanBeamPreFireTicks() {
        return this.getChallengeDifficulty().acceleratedTicks(XUANLAN_BEAM_LOCK)
                + this.getChallengeDifficulty().acceleratedTicks(XUANLAN_BEAM_CHARGE);
    }

    /**
     * 光束状态机(每 tick 由 {@code tick()} 调用)。
     *
     * <p>2026-09-20 改版后的三段: **锁定 {@value #XUANLAN_BEAM_LOCK}**(跟头)
     * → **前摇 {@value #XUANLAN_BEAM_CHARGE}**(方向冻结, 躲避窗口) → **引导 {@value #XUANLAN_BEAM_FIRE}**(判定)。
     *
     * @return true = 正在锁定/充能/引导(调用方应让本体"专心引导": 停导航、不投飞轮)
     */
    private boolean xuanlanBeamTick(ServerLevel sl) {
        LivingEntity beamTarget = this.getTarget();
        if (this.xuanlanBeamBusy() && (beamTarget == null || !beamTarget.isAlive()
                || beamTarget instanceof Player player && !this.combatPlayer(player))) {
            this.cancelSkillWindups();
            this.xuanlanBeamCd = 20;
            return false;
        }
        double x = this.getX(), y = this.getEyeY(), z = this.getZ();
        if (this.xuanlanBeamFire > 0) {
            this.getNavigation().stop();
            /* ★ 2026-09-20: 引导期间**不再跟头** —— 方向在前摇开始那一刻就冻结了(「不要锁头」)。 */
            this.xuanlanBeamFx(sl, x, y, z);
            if (--this.xuanlanBeamPulse <= 0) {
                this.xuanlanBeamPulse = XUANLAN_BEAM_PULSE;
                this.xuanlanBeamPunish(sl, x, y, z);
            }
            if (--this.xuanlanBeamFire <= 0) {
                this.releaseSkillCue(new Vec3(x, y, z));
                sl.playSound(null, x, y, z, SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.6f, 0.6f);
                this.xuanlanBeamCd = XUANLAN_BEAM_CD;
                    }
            return true;
        }
        if (this.xuanlanBeamCharge > 0) {
            this.getNavigation().stop();
            /* ★ 2026-09-20: 前摇期间**不跟头**, 只播预警(收缩环已经随锁定一起在收, 收满即发射)。 */
            this.xuanlanBeamFx(sl, x, y, z);
            if (this.xuanlanBeamCharge % 5 == 0) {
                double prog = 1.0 - (double) this.xuanlanBeamCharge
                        / Math.max(1, this.getChallengeDifficulty().acceleratedTicks(XUANLAN_BEAM_CHARGE));
                sl.playSound(null, x, y, z, SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE,
                        1.1f, 0.7f + (float) prog * 0.7f);
            }
            if (--this.xuanlanBeamCharge <= 0) {
                this.xuanlanBeamFire = XUANLAN_BEAM_FIRE;
                this.xuanlanBeamPulse = XUANLAN_BEAM_PULSE;
                sl.playSound(null, x, y, z, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.6f, 1.4f);
                this.tellNearby(sl, "§b§l【超能光束】§r§c发射！§7(被照到会眩晕 + 清空增益)");
            }
            return true;
        }
        if (this.xuanlanBeamLock > 0) {
            /* 锁定阶段(用户要求「可以锁定一会」): 这段时间一直跟头, 预警线跟着玩家走, 让他知道被锁了。 */
            this.getNavigation().stop();
            this.xuanlanAimAtTarget();
            this.xuanlanBeamFx(sl, x, y, z);
            if (this.xuanlanBeamLock % 6 == 0) {
                sl.playSound(null, x, y, z, SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.7f, 0.6f);
            }
            if (--this.xuanlanBeamLock <= 0) {
                this.xuanlanBeamCharge = this.getChallengeDifficulty().acceleratedTicks(XUANLAN_BEAM_CHARGE);
                this.tellNearby(sl, "§b【超能光束】§7锁定完成 —— §c方向固定, 侧移躲开！§7("
                        + this.xuanlanBeamCharge / 20.0 + " 秒后发射)");
                sl.playSound(null, x, y, z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.4f, 0.8f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝「超能光束」锁定 {} tick 完成, 前摇 {} tick(方向已冻结, 不再跟头)",
                        this.getChallengeDifficulty().acceleratedTicks(XUANLAN_BEAM_LOCK), this.xuanlanBeamCharge);
            }
            return true;
        }
        if (this.xuanlanBeamCd > 0) this.xuanlanBeamCd--;
        if (this.xuanlanBeamCd <= 0 && !this.skillCommitted() && this.actionTicks <= 0 && this.castTicks <= 0) {
            LivingEntity t = this.getTarget();
            if (t != null && t.isAlive()) {
                this.xuanlanAimAtTarget();
                this.xuanlanBeamLock = this.getChallengeDifficulty().acceleratedTicks(XUANLAN_BEAM_LOCK);
                this.xlBeamPunished.clear();
                this.playCast(this.xuanlanBeamPreFireTicks() + XUANLAN_BEAM_FIRE, 1);
                this.xuanlanBeamFx(sl, x, y, z);
                this.tellNearby(sl, "§b【超能光束】§7文玄蓝开始锁定你 —— §c先别急, 锁定结束后还有前摇, 那时侧移！");
                sl.playSound(null, x, y, z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.0f, 0.6f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝「超能光束」开始锁定 {} tick -> 前摇 {} tick -> 引导 {} tick"
                                + "(无伤害; 命中 {} 秒眩晕 + 清空增益; 前摇与引导期间不跟头)",
                        this.xuanlanBeamLock, this.getChallengeDifficulty().acceleratedTicks(XUANLAN_BEAM_CHARGE),
                        XUANLAN_BEAM_FIRE, XUANLAN_BEAM_STUN / 20);
                return true;
            }
            this.xuanlanBeamCd = 20;      // 没目标, 1 秒后再试
        }
        return false;
    }

    /** 光束视觉: 锁定/前摇期 = 红色预警线(跟着走 -> 冻结), 引导期 = 能量柱 */
    private void xuanlanBeamFx(ServerLevel sl, double x, double y, double z) {
        Vec3 origin = new Vec3(x, y, z);
        Vec3 end = this.clipSkillRay(origin, origin.add(this.xlBeamDx * XUANLAN_BEAM_LEN,
                this.xlBeamDy * XUANLAN_BEAM_LEN, this.xlBeamDz * XUANLAN_BEAM_LEN));
        boolean firing = this.xuanlanBeamFire > 0;
        /* 预警期给"锁定 + 前摇"的总时长, 收缩环正好在发射那一 tick 收满 */
        int duration = firing ? XUANLAN_BEAM_FIRE : this.xuanlanBeamPreFireTicks();
        this.syncBeamCue(origin, end, XUANLAN_BEAM_RADIUS, duration, firing, false);
    }

    /**
     * 光束结算(2026-09-20 用户要求: **不再造成伤害**, 改成【眩晕 + 清空增益】)。
     *
     * <p>判定与老版本一致(到光束直线 {@value #XUANLAN_BEAM_RADIUS} 格内 + 有视线),
     * 但打到人只做两件事:{@link #stunPlayerAny 眩晕}(硬定身, 与雪黎瞬砸同一套)
     * {@value #XUANLAN_BEAM_STUN} tick, 以及
     * {@link net.farmingtales.ftboss.util.PlayerBuffRules#clearBuffs 清空全部增益}。
     * 每次施法每人只结算一次(第一次被照到就吃满), 之后继续站在光束里也不会反复晕。
     */
    private void xuanlanBeamPunish(ServerLevel sl, double x, double y, double z) {
        Vec3 origin = new Vec3(x, y, z);
        double len = origin.distanceTo(this.clipSkillRay(origin, origin.add(this.xlBeamDx * XUANLAN_BEAM_LEN,
                this.xlBeamDy * XUANLAN_BEAM_LEN, this.xlBeamDz * XUANLAN_BEAM_LEN)));
        int hits = 0;
        for (Player p : sl.players()) {
            if (!this.combatPlayer(p) || this.xlBeamPunished.contains(p.getUUID())) continue;
            if (!this.hasLineOfSight(p)) continue;
            net.minecraft.world.phys.Vec3 rel = p.position().add(0, p.getBbHeight() * 0.5, 0).subtract(x, y, z);
            double along = rel.x * this.xlBeamDx + rel.y * this.xlBeamDy + rel.z * this.xlBeamDz;
            if (along < 0.0 || along > len) continue;
            double perp = rel.subtract(this.xlBeamDx * along, this.xlBeamDy * along, this.xlBeamDz * along).length();
            if (perp > XUANLAN_BEAM_RADIUS + p.getBbWidth() * 0.5) continue;
            this.xlBeamPunished.add(p.getUUID());
            /* 2026-09-20 (v186): 眩晕 + 攒一发击退 —— 击退在眩晕解除那一 tick 才甩出去
               (眩晕期间会被定身锚点抹掉, 见 stunPlayerWithLaunch 的说明)。 */
            this.stunPlayerWithLaunch(p, XUANLAN_BEAM_STUN, "超能光束",
                    this.xlBeamDx * XUANLAN_BEAM_KNOCKBACK, XUANLAN_BEAM_LIFT,
                    this.xlBeamDz * XUANLAN_BEAM_KNOCKBACK);
            int cleared = net.farmingtales.ftboss.util.PlayerBuffRules.clearBuffs(p);
            p.displayClientMessage(Component.literal("§7你的增益被【超能光束】清空 " + cleared
                    + " 个 —— §c眩晕结束后会被击飞"), true);
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    p.getX(), p.getY() + 1.2, p.getZ(), 18, 0.35, 0.6, 0.35, 0.06);
            hits++;
        }
        sl.playSound(null, x, y, z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2f, 1.3f);
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,
                x + this.xlBeamDx * 3.0, y + this.xlBeamDy * 3.0, z + this.xlBeamDz * 3.0, 1, 0, 0, 0, 0);
        if (hits > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] 文玄蓝「超能光束」脉冲命中 {} 人: 眩晕 {} 秒 + 清空增益(本招已无伤害)",
                    hits, XUANLAN_BEAM_STUN / 20);
        }
    }

    private boolean canSpeakXueliDialogue() {
        return this.getVariant() == XUELI_VARIANT && !this.isMini() && !this.isGiant()
                && this.isAlive() && !this.isNoAi()
                && !net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this);
    }

    private void tickXueliDialogue() {
        if (!(this.level() instanceof ServerLevel level) || !this.canSpeakXueliDialogue()) return;
        if (!(this.getTarget() instanceof Player player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()
                || player.level() != level || this.distanceToSqr(player) > 64.0 * 64.0) return;
        long now = level.getGameTime();
        String line = this.xueliDialogue.nextStage(now, this.getHealth() / Math.max(1.0f, this.getMaxHealth()));
        if (line == null && this.xueliDialogue.randomReady(now)
                && this.actionTicks <= 0 && this.castTicks <= 0 && !this.skillCommitted()
                && this.xueliRollTicks <= 0 && this.xueliSlamLeft <= 0) {
            line = this.xueliDialogue.randomLine(this.random.nextInt(this.xueliDialogue.randomChoiceCount()));
            int minimum = net.farmingtales.ftboss.util.XueliDialogueState.RANDOM_MIN_DELAY;
            int maximum = net.farmingtales.ftboss.util.XueliDialogueState.RANDOM_MAX_DELAY;
            this.xueliDialogue.scheduleRandom(now, minimum + this.random.nextInt(maximum - minimum + 1));
        }
        if (line != null) this.sayXueliDialogue(line);
    }

    private void sayXueliDialogue(String line) {
        if (!(this.level() instanceof ServerLevel level) || !this.canSpeakXueliDialogue()) return;
        this.tellNearby(level, "§b雪黎：§f" + line);
        this.xueliDialogue.deferRandom(level.getGameTime());
    }

    public void onXueliShieldActivated() {
        this.sayXueliDialogue(net.farmingtales.ftboss.util.XueliDialogueState.SHIELD);
    }

    /**
     * 给附近玩家发一条**必需机制提示**（技能预警/倒计时这类）。
     *
     * <p>★ v182 按用户要求「优化 boss 文本，非必要减少文字输出」改了两点：
     * <ol>
     *   <li><b>不再双发</b>：原来同一句话既进行动栏又进聊天栏，等于一次动作刷两条。
     *       现在只走**行动栏**（聊天栏留给真正需要留痕的内容）；</li>
     *   <li>调用方已经把"纯战报"类的调用删掉了 —— 这个方法现在只承载
     *       玩家**必须立刻反应**的信息（如"快躲开这条线"）。</li>
     * </ol>
     * 文案本身全部保留（不缩短、不改措辞），只是渠道收敛。
     */
    private void tellNearby(ServerLevel sl, String msg) {
        Component c = Component.literal(msg);
        for (Player p : sl.players()) {
            if (p.distanceToSqr(this) > 64.0 * 64.0) continue;
            p.displayClientMessage(c, true);
        }
    }

    /* ==================== v81 按用户要求: 「Boss 离玩家太远会主动寻找玩家」 ====================
       以前 FOLLOW_RANGE 只有 42 格: 玩家跑远一点 Boss 就**丢目标、原地发呆**(只靠"15 秒抓人"
       那种被动手段)。现在多一层主动追击:
         · 超过 {@value #HUNT_START} 格 -> 强制锁定最近的玩家 + 加速寻路追过去;
         · 超过 {@value #HUNT_TELEPORT} 格 -> 直接**闪现**到玩家身边(带传送门粒子/音效, 有冷却),
           免得在复杂地形里永远追不上。
       麻雀(支援位)不参与 —— 她的行为是"传送到友军身边"(v72), 不追玩家。 */
    public static final double HUNT_RANGE = 160.0;
    public static final double HUNT_START = 20.0;
    public static final double HUNT_TELEPORT = 72.0;
    public static final int HUNT_TELEPORT_CD = 6 * 20;
    private int huntTeleportCd = 0;

    private void huntStrayPlayer(ServerLevel sl) {
        if (this.huntTeleportCd > 0) this.huntTeleportCd--;
        ServerPlayer best = this.manorSummoner();
        if (best == null) return;
        double bd = best.distanceToSqr(this);
        if (bd >= HUNT_RANGE * HUNT_RANGE) return;
        double dist = Math.sqrt(bd);
        if (dist <= HUNT_START) return;                     // 够近, 交给正常战斗 AI
        /* 目标丢了(玩家跑到 FOLLOW_RANGE 之外就会丢) -> 重新锁定 */
        LivingEntity cur = this.getTarget();
        if (cur == null || !cur.isAlive() || cur != best) this.setTarget(best);
        this.getLookControl().setLookAt(best, 30.0f, 30.0f);
        this.getNavigation().moveTo(best, 1.6);             // 加速追
        if (dist > HUNT_TELEPORT && this.huntTeleportCd <= 0) {
            /* 2026-09-13 v94: **庄园里不自己闪现** —— 那里由 ManorFight 按"谁离场地更近"决定
               挪玩家还是挪 Boss; 否则玩家一跑出场地, Boss 就自己追出去, 和拉回机制拔河。 */
            if (net.farmingtales.ftboss.boss.ManorFight.isManaged(this)) return;
            this.huntTeleportCd = HUNT_TELEPORT_CD;
            double ang = this.random.nextDouble() * Math.PI * 2.0;
            double tx = best.getX() + Math.cos(ang) * 3.0;
            double tz = best.getZ() + Math.sin(ang) * 3.0;
            double ox = this.getX(), oy = this.getY(), oz = this.getZ();
            sl.sendParticles(ParticleTypes.PORTAL, ox, oy + 1.0, oz, 30, 0.4, 0.7, 0.4, 0.3);
            Vec3 safe = ManorSafeTeleport.find(sl, this, new Vec3(tx, best.getY(), tz), 8);
            if (safe == null) return;
            ManorSafeTeleport.move(sl, this, safe);
            sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.4, 0.7, 0.4, 0.3);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2f, 0.9f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} 离玩家 {} 格 -> 主动闪现追击",
                    NAMES[this.getVariant()], (int) dist);
        }
    }

    /* ==================== 青衣: 三叉戟层数(v95) ==================== */

    /** 青衣「三叉戟层数」带来的**全部伤害 +N**(其它变体/迷你分身恒为 0)。供 QingyiTridentEntity 结算落雷用 */
    public float qingyiDamageBonus() {
        return (this.getVariant() == 5 && !this.isMini())
                ? (float) (this.qingyiTridentStacks * QINGYI_STACK_DAMAGE) : 0.0f;
    }

    /** 当前层数(日志/调试) */
    public int qingyiTridentStacks() { return this.qingyiTridentStacks; }

    /** v102: 把「青衣攻防强化」的两个修饰符挂上(非青衣一律摘掉); 幂等, 可随时重算 */
    private void applyQingyiFlatBuffs() {
        boolean isQingyi = this.getVariant() == 5 && !this.isMini();
        var atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(QINGYI_FLAT_ATK_MOD);
            if (isQingyi) atk.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    QINGYI_FLAT_ATK_MOD, "ftboss.qingyi_atk", QINGYI_ATK_BONUS,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        var arm = this.getAttribute(Attributes.ARMOR);
        if (arm != null) {
            arm.removeModifier(QINGYI_FLAT_ARMOR_MOD);
            if (isQingyi) arm.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    QINGYI_FLAT_ARMOR_MOD, "ftboss.qingyi_armor", QINGYI_ARMOR_BONUS,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
    }

    /**
     * 把层数同步到三个属性上(**幂等**: 先摘再加, 所以随时可以重算)。
     * 用修饰符而不是改基础值 —— {@code BossScaling} 每 20 tick 会按人数重设基础值, 会把它冲掉。
     */
    private void syncQingyiTridentBuffs() {
        int n = (this.getVariant() == 5 && !this.isMini()) ? this.qingyiTridentStacks : 0;
        /* 血量上限层数单独封顶；护甲只清除旧修饰符，不再随召唤增长。 */
        int healthStacks = Math.min(n, QINGYI_MAX_HEALTH_STACKS);
        var atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(QINGYI_ATK_MOD);
            if (n > 0) atk.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    QINGYI_ATK_MOD, "ftboss.qingyi_trident_atk", n * QINGYI_STACK_DAMAGE,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
        var arm = this.getAttribute(Attributes.ARMOR);
        if (arm != null) {
            arm.removeModifier(QINGYI_ARMOR_MOD);
        }
        var hp = this.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.removeModifier(QINGYI_HEALTH_MOD);
            if (healthStacks > 0) hp.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    QINGYI_HEALTH_MOD, "ftboss.qingyi_trident_health", healthStacks * QINGYI_STACK_HEALTH,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
    }

    /** 青衣召唤三叉戟后保留伤害/血量上限成长，不再叠甲；每五把回复十点生命，成长封顶后仍可回复。
        v122 按用户要求: 另外获得【充能】(等级显示层数) + {@value #QINGYI_ONSUMMON_BUFFS} 个
        **不重复**的随机增益, 全部持续 {@value #QINGYI_ONSUMMON_TICKS} tick(30 秒)。 */
    private void onQingyiTridentSummoned() {
        if (this.getVariant() != 5 || this.isMini()) return;
        if (this.qingyiTridentStacks < QINGYI_MAX_STACKS) {
            this.qingyiTridentStacks++;
            this.syncQingyiTridentBuffs();
        }
        net.farmingtales.ftboss.boss.ai.QingyiTridentRecovery.onSummoned(this);
        /* v122 【充能】: 等级 = 层数 - 1(显示封顶到 V; 真正的数值还是靠上面那三个属性修饰符) */
        int chargeAmp = Math.min(QINGYI_CHARGE_MAX_AMP, Math.max(0, this.qingyiTridentStacks - 1));
        var charge = net.farmingtales.ftboss.effect.FTEffects.QINGYI_CHARGE.get();
        if (charge != null) {
            this.addEffect(new MobEffectInstance(charge, QINGYI_ONSUMMON_TICKS, chargeAmp, false, true, true));
        }
        /* v122 6 个不重复的随机增益(Fisher-Yates, 用实体自己的 random) */
        int[] idx = new int[CHAIN_BUFF_POOL.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        for (int i = idx.length - 1; i > 0; i--) {
            int j = this.random.nextInt(i + 1);
            int tmp = idx[i]; idx[i] = idx[j]; idx[j] = tmp;
        }
        StringBuilder sb = new StringBuilder();
        int n = Math.min(QINGYI_ONSUMMON_BUFFS, idx.length);
        for (int k = 0; k < n; k++) {
            var eff = CHAIN_BUFF_POOL[idx[k]];
            this.addEffect(new MobEffectInstance(eff, QINGYI_ONSUMMON_TICKS, this.buffAmp(1), true, true, true));
            sb.append(eff.getDisplayName().getString()).append(' ');
        }
        /* 星戟雨一次落 10 把 -> 日志会刷屏, 所以每秒最多写一条 */
        if (--this.qingyiOnSummonLogCd <= 0) {
            this.qingyiOnSummonLogCd = 20;
            FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣召唤三叉戟 -> 层数 {}/{} (全部伤害 +{}, 护甲 +{}, 血量上限 +{}; 每5把回复10点生命)"
                            + " | 充能 {} 级 + {} 个增益 {} 秒: {}",
                    this.qingyiTridentStacks, QINGYI_MAX_STACKS,
                    (int) (this.qingyiTridentStacks * QINGYI_STACK_DAMAGE),
                    0,
                    (int) (Math.min(this.qingyiTridentStacks, QINGYI_MAX_HEALTH_STACKS) * QINGYI_STACK_HEALTH),
                    chargeAmp + 1, n, QINGYI_ONSUMMON_TICKS / 20, sb.toString().trim());
        }
    }

    /* ==================== 青衣: 铁魔法闪电(v82) ==================== */

    /** 青衣每 {@value #QINGYI_SPELL_CD} tick 放一次闪电法术 */
    private void qingyiLightningTick(ServerLevel sl) {
        if (this.qingyiSpellCd > 0) this.qingyiSpellCd--;
        if (this.qingyiSpellCd > 0) return;
        LivingEntity t = this.getTarget();
        /* v114 按用户反馈「青衣的闪电也有类似问题」: 创造/旁观玩家不当目标 ——
           全局兜底清扫是每 10t 一次, 施法前这里再即时判一道, 免得这一发闪电打在无敌玩家身上。 */
        if (t instanceof Player pt && !net.farmingtales.ftboss.boss.ai.BossTargets.valid(pt)) {
            this.setTarget(null);
            return;
        }
        if (!this.allowsParallelMelee() || this.isNoAi() || !this.isAlive() || this.isInvulnerable()
                || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)
                || t == null || !t.isAlive() || t.level() != sl || this.isAlliedTo(t) || t.isAlliedTo(this)
                || this.distanceToSqr(t) > 32.0 * 32.0 || !this.hasLineOfSight(t)) return;
        this.qingyiSpellCd = QINGYI_SPELL_CD;
        String id = QINGYI_LIGHTNING_SPELLS[this.random.nextInt(QINGYI_LIGHTNING_SPELLS.length)];
        int lvl = QINGYI_SPELL_LVL_MIN + this.random.nextInt(QINGYI_SPELL_LVL_MAX - QINGYI_SPELL_LVL_MIN + 1);
        this.playParallelSpell(HiddenBossCastRules.quicker(40));
        this.getLookControl().setLookAt(t, 60.0f, 60.0f);
        boolean ok = net.farmingtales.ftboss.boss.IronSpellBridge.cast(sl, this, id, lvl);
        if (!ok) {
            /* 没装铁魔法 -> 等效实现: 目标头顶真闪电 + 魔法伤害 + 感电(缓慢/虚弱) */
            try {
                var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(t.getX(), t.getY(), t.getZ());
                    bolt.setVisualOnly(false);
                    net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, bolt);
                    sl.addFreshEntity(bolt);
                }
            } catch (Throwable ignored) {
            }
            if (t instanceof Player p) {
                p.invulnerableTime = 0;
                p.hurt(this.damageSources().indirectMagic(this, this), this.manorSkillDamage(p,
                        QINGYI_SPELL_FALLBACK_DAMAGE + this.qingyiDamageBonus(), 1.75));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3 * 20, 2));
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 3 * 20, 1));
            } else {
                t.hurt(this.damageSources().indirectMagic(this, this), this.manorSkillDamage(t,
                        QINGYI_SPELL_FALLBACK_DAMAGE + this.qingyiDamageBonus(), 1.75));
            }
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.6f, 1.2f);
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.6, this.getZ(),
                30, 0.6, 0.6, 0.6, 0.25);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣「铁魔法闪电」{} lv{} 目标={} 真施法={}",
                id, lvl, t.getName().getString(), ok);
    }

    /* ==================== v26 新增机制(用户定案) ==================== */

    /**
     * 追击用的末影珍珠（2026-09-24 按用户要求重做）。
     *
     * <p>用户原话：「boss 的珍珠追击没触发（减少触发，除非长时间未攻击到玩家 15s，
     * 超过 120s 则禁飞场地 60s，结束后允许飞行并重新倒计时禁飞）」、「蛋蛋会更多使用珍珠」。
     *
     * <p><b>为什么以前几乎不触发</b>：旧判据是「离目标超过 {@link #PEARL_NEAR}(4 格) 累计
     * {@link #PEARL_TRIGGER_TICKS}(20) tick」，而 Boss 一直在贴身近战 —— 一进 4 格计数器就被清零，
     * 真正"追不上"的时候又因为 {@code skillCommitted()} / {@code movementLocked()} 被return 掉。
     *
     * <p><b>现在的口径</b>（只作用于被 {@code ManorFight} 托管的庄园 Boss；别处保持 v26 老节奏）：
     * <ol>
     *   <li>{@link ManorChaseRules#PEARL_IDLE_TICKS}：连续 15 秒没打到玩家才允许追
     *       （蛋蛋 {@link ManorChaseRules#DANDAN_PEARL_IDLE_TICKS} = 10 秒，她更爱用珍珠）；</li>
     *   <li>每次追完还有 {@link ManorChaseRules#PEARL_COOLDOWN_TICKS} 冷却（蛋蛋
     *       {@link ManorChaseRules#DANDAN_PEARL_COOLDOWN_TICKS}）—— 这是"减少触发"的那一半；</li>
     *   <li>距离仍要超过 {@link #PEARL_NEAR} 格（贴脸时不投珍珠，免得糊在自己脚下）。</li>
     * </ol>
     */
    private void pearlTick() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (this.pearlRushCd > 0) this.pearlRushCd--;
        LivingEntity t = this.getTarget();
        if (!this.canTeleportWith(t) || t instanceof Player player && player.isCreative()) { this.meleeLockTicks = 0; return; }
        if (this.distanceToSqr(t) <= PEARL_NEAR * PEARL_NEAR) { this.meleeLockTicks = 0; return; }
        if (net.farmingtales.ftboss.boss.ManorFight.isManaged(this)) {
            int idleNeed = this.getVariant() == DANDAN_VARIANT
                    ? net.farmingtales.ftboss.util.ManorChaseRules.DANDAN_PEARL_IDLE_TICKS
                    : net.farmingtales.ftboss.util.ManorChaseRules.PEARL_IDLE_TICKS;
            if (this.playerHitIdleTicks < idleNeed) { this.meleeLockTicks = 0; return; }
            if (this.pearlRushCd > 0) return;
        } else {
            /* 非庄园场合（柠檬猫召来的助战、指令召出的 Boss…）保持 v26 的老节奏，避免改坏那些场景 */
            int trigger = this.getVariant() == DANDAN_VARIANT ? PEARL_TRIGGER_TICKS / 2 : PEARL_TRIGGER_TICKS;
            this.meleeLockTicks = Math.min(trigger, this.meleeLockTicks + 1);
            if (this.meleeLockTicks < trigger) return;
        }
        if (this.skillCommitted() || this.movementLocked()) return;
        this.meleeLockTicks = 0;
        this.pearlRushCd = this.getVariant() == DANDAN_VARIANT
                ? net.farmingtales.ftboss.util.ManorChaseRules.DANDAN_PEARL_COOLDOWN_TICKS
                : net.farmingtales.ftboss.util.ManorChaseRules.PEARL_COOLDOWN_TICKS;
        this.pearlRush(sl, t);
    }

    /** 被 {@code ManorChaseTracker} 调用：这一次伤害**真的打在玩家身上**了（含分身/投射物打的）。 */
    public void onPlayerHitLanded() {
        this.playerHitIdleTicks = 0;
    }

    /** 供回归/日志读取：当前"多久没打到玩家"的 tick 数。 */
    public int playerHitIdleTicks() {
        return this.playerHitIdleTicks;
    }

    private void manorFlyBanTick() {
    }

    private void haleyFlightTick() {
        if (!this.haleyFlying) return;
        this.haleyFlying = false;
        this.setNoGravity(false);
        this.moveControl = new net.minecraft.world.entity.ai.control.MoveControl(this);
        var movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, Math.min(0.0, movement.y), movement.z);
        this.hurtMarked = true;
    }

    private boolean hasHaleyGlide() {
        return false;
    }

    /** 投一颗末影珍珠(落地会对周围玩家造成 45 点魔法伤害) + 自己瞬移到玩家旁边(保证一定贴上去) */
    private void pearlRush(ServerLevel sl, LivingEntity target) {
        if (!this.canTeleportWith(target)) return;
        try {
            /* 2026-09-12 v54 按用户要求: 换成 EasterPearlEntity(落点 4 格内玩家吃 45 点魔法伤害) */
            var pearl = new net.farmingtales.ftboss.entity.EasterPearlEntity(sl, this);
            pearl.setPos(this.getX(), this.getEyeY() - 0.2, this.getZ());
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            pearl.shoot(dx / len, dy / len + 0.12, dz / len, 1.5f, 0.0f);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, pearl);
            sl.addFreshEntity(pearl);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 投末影珍珠失败: {}", t.toString());
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.HOSTILE, 1.2f, 0.8f);
        double ang = this.random.nextDouble() * Math.PI * 2.0;
        double tx = target.getX() + Math.cos(ang) * 2.2;
        double tz = target.getZ() + Math.sin(ang) * 2.2;
        sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.4, 0.7, 0.4, 0.3);
        this.randomTeleport(tx, target.getY(), tz, true);
        sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.4, 0.7, 0.4, 0.3);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.3f, 1.0f);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} 追击距离超过 4 格 -> 末影珍珠瞬移到身边",
                NAMES[this.getVariant()]);
    }

    /** 中减益 -> {@value #PURGE_DELAY_TICKS} tick(2 秒)后清除自身**全部减益**(增益保留) */
    private void purgeTick() {
        boolean debuffed = false;
        for (MobEffectInstance mi : this.getActiveEffects()) {
            if (mi.getEffect() != null && !mi.getEffect().isBeneficial()) { debuffed = true; break; }
        }
        if (!debuffed) { this.purgeTimer = 0; return; }
        if (this.purgeTimer <= 0) { this.purgeTimer = PURGE_DELAY_TICKS; return; }
        if (--this.purgeTimer > 0) return;
        /* ★★★ 2026-09-14 v106 按用户反馈「boss 的净化疑似会清除自己的 buff」★★★
           以前这里是 this.removeAllEffects() —— 把自己刚同步来的玩家增益一起洗掉了(日志里就发生在
           "同步了 1 名玩家的 19 个增益效果" 之后一行)。现在**只清负面**, 增益照留。 */
        int removed = 0;
        for (MobEffectInstance mi : new java.util.ArrayList<>(this.getActiveEffects())) {
            if (mi.getEffect() == null || mi.getEffect().isBeneficial()) continue;
            this.removeEffect(mi.getEffect());
            removed++;
        }
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.0, this.getZ(),
                    40, 0.6, 0.8, 0.6, 0.25);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0f, 1.4f);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} 中减益 2 秒 -> 净化自身 {} 个减益(增益保留)",
                NAMES[this.getVariant()], removed);
    }

    /** 柠檬猫: 锅铲一直"做菜", 每 5 秒随机来 2 个食物系增益 */
    private void foodBuffTick() {
        net.farmingtales.ftboss.balance.ManorBalanceHooks.food(this);
    }

    /** 柠檬猫的"食物增益池"(v124 按用户要求「柠檬获取的buff类型可以多一点」扩充)。
     *  原来只有 10 种纯原版效果; 现在补上视野/水下/海豚/缓降/幸运这些"吃了好料"的感觉,
     *  再把整合包里确实存在的几种**酒/食物系**增益(名称对得上就拿、拿不到就跳过)也并进来。 */
    private static net.minecraft.world.effect.MobEffect[] foodBuffPoolCache;
    private static boolean foodBuffPoolLogged = false;

    static net.minecraft.world.effect.MobEffect[] foodBuffPool() {
        if (foodBuffPoolCache != null) return foodBuffPoolCache;
        java.util.List<net.minecraft.world.effect.MobEffect> list = new java.util.ArrayList<>(java.util.List.of(
                MobEffects.SATURATION, MobEffects.REGENERATION, MobEffects.ABSORPTION,
                MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_RESISTANCE,
                MobEffects.DIG_SPEED, MobEffects.JUMP, MobEffects.HEALTH_BOOST, MobEffects.FIRE_RESISTANCE,
                MobEffects.NIGHT_VISION, MobEffects.WATER_BREATHING, MobEffects.CONDUIT_POWER,
                MobEffects.DOLPHINS_GRACE, MobEffects.SLOW_FALLING, MobEffects.LUCK));
        /* 整合包里的"酒/食物"增益(有就并进来, 没有就跳过) */
        String[][] extra = {
                {"vinery", "armor_effect"},                 // 葡萄酒: 护甲
                {"vinery", "health_effect"},                // 葡萄酒: 生命
                {"farmersdelight", "comfort"},              // 农夫乐事: 舒适
                {"farmersdelight", "nourishment"},          // 农夫乐事: 滋养
                {"farm_and_charm", "satiation"},            // 农场与魅力: 饱腹
                {"kaleidoscope_cookery", "satiated_shield"},// 森罗物语: 饱腹代偿
        };
        int foreign = 0;
        for (String[] id : extra) {
            var eff = ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation(id[0], id[1]));
            if (eff != null && eff.isBeneficial() && !list.contains(eff)) {
                list.add(eff);
                foreign++;
            }
        }
        int removed = 0;
        for (var iterator = list.iterator(); iterator.hasNext();) {
            if (net.farmingtales.ftboss.boss.ai.LemonImmuneBuffRules.isForbidden(iterator.next())) {
                iterator.remove();
                removed++;
            }
        }
        foodBuffPoolCache = list.toArray(new net.minecraft.world.effect.MobEffect[0]);
        if (!foodBuffPoolLogged) {
            foodBuffPoolLogged = true;
            FTBossMod.LOGGER.info("[FTBoss][Easter] 柠檬猫食物增益池就绪: {} 种(其中整合包酒/食物系 {} 种, 已剔除打不死类 {} 种)",
                    foodBuffPoolCache.length, foreign, removed);
        }
        return foodBuffPoolCache;
    }

    /**
     * 青衣 招牌技能「投戟 + 激流 + 混毒药剂」(2026-09-12 v26 用户定案):
     * ①把三叉戟**投掷**出去造成伤害; ②用**激流**朝玩家冲刺(沿途撞人); ③冲刺结束扔一瓶**随机 6 种减益**的溅射药水。
     */
    private void skillQingyi(ServerLevel sl, LivingEntity target) {
        playImpactAction(20);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        this.throwQingyiTrident(sl, target);
        /* 2026-09-12 v28 按用户要求: 三叉戟**必定触发**对应的技能效果 */
        this.applyQingyiTridentEffect(sl, target);
        Vec3 d = target.position().subtract(this.position());
        d = new Vec3(d.x, 0.0, d.z);
        this.riptideDir = d.lengthSqr() < 1.0E-4 ? Vec3.ZERO : d.normalize();
        this.riptideTicks = 12;
        this.riptideHitCd = 0;
        this.potionDelay = 0;
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.HOSTILE, 1.4f, 1.0f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣「投戟 + 激流冲刺」开始(当前武器={})", this.getMainHandItem());
    }

    /** 青衣: 冲刺每 tick 推进 + 撞人结算; 冲刺结束后扔混毒药剂 */
    private void qingyiTick() {
        if (!(this.level() instanceof ServerLevel sl)) { this.riptideTicks = 0; this.potionDelay = 0; return; }
        if (this.riptideTicks > 0) {
            this.riptideTicks--;
            Vec3 d = this.riptideDir;
            this.setDeltaMovement(d.x * 1.15, 0.10, d.z * 1.15);
            this.hasImpulse = true;
            sl.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.6, this.getZ(), 12, 0.4, 0.3, 0.4, 0.2);
            sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            if (this.riptideHitCd > 0) this.riptideHitCd--;
            /* 2026-09-12 v28 按用户要求: 冲刺期间**连续瞬移到玩家身边**(每 6 tick 一次) */
            if (this.riptideTicks % 6 == 0) {
                LivingEntity tp = this.getTarget();
                if (this.canTeleportWith(tp)) {
                    double ang = this.random.nextDouble() * Math.PI * 2.0;
                    double tx = tp.getX() + Math.cos(ang) * 2.2;
                    double tz = tp.getZ() + Math.sin(ang) * 2.2;
                    sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.5, 0.3, 0.25);
                    this.randomTeleport(tx, tp.getY(), tz, true);
                    sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.5, 0.3, 0.25);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.9f, 1.3f);
                }
            }
            if (this.riptideHitCd <= 0) {
                int hits = 0;
                for (Player p : sl.players()) {
                    if (!this.combatPlayer(p) || p.distanceToSqr(this) > QINGYI_RIPTIDE_RADIUS * QINGYI_RIPTIDE_RADIUS) continue;
                    /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
                    p.invulnerableTime = 0;
                    if (!p.hurt(this.damageSources().mobAttack(this), this.manorSkillDamage(p,
                            18.0F + this.qingyiDamageBonus(), 1.5))) continue;
                    /* 2026-09-12 v47 按用户要求: 学朵拉长枪的"**连续击飞**" —— 冲刺期间每次撞人都把玩家挑起来 */
                    net.farmingtales.ftboss.util.BossKnockback.push(p, d.x * 1.45, 0.95, d.z * 1.45);
                    p.hurtMarked = true;
                    hits++;
                }
                if (hits > 0) this.riptideHitCd = 5;
            }
            if (this.riptideTicks == 0) this.potionDelay = 6;
            return;
        }
        if (this.potionDelay > 0 && --this.potionDelay <= 0) {
            LivingEntity t = this.getTarget();
            if (t != null && t.isAlive()) this.throwDebuffPotion(sl, t);
        }
    }

    /** 投出当前主手的三叉戟(TnS 独特武器都是三叉戟), 命中造成伤害; 禁止玩家捡走 */
    private void throwQingyiTrident(ServerLevel sl, LivingEntity target) {
        try {
            /* v117 按用户要求「召唤的三叉戟改成全部都是恒星三叉戟」:
               投出去的那把一律用 `tridents_n_stuff:stellaris`, 不再跟着当前手持那件走;
               手持仍然四件轮换(决定近战减益与换装增益), 效果也照旧按 `qingyiIndex` 触发。 */
            ItemStack stack = this.qingyiStellarisStack();
            if (stack.isEmpty()) stack = new ItemStack(Items.TRIDENT);
            var tri = new net.farmingtales.ftboss.entity.QingyiTridentEntity(sl, this, stack, this.qingyiIndex);
            tri.setPos(this.getX(), this.getEyeY() - 0.15, this.getZ());
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            tri.shoot(dx / len, dy / len + 0.05, dz / len, 3.0f, 0.05f);   // 2026-09-12 v28: 投掷速度大幅加快
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, tri);
            if (!QingyiTridentEntity.spawnOwned(sl, this, tri)) return;
            this.onQingyiTridentSummoned();          // v95: 每投出一把 -> 伤害/护甲/血量各 +1
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1.3f, 0.9f);
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getEyeY(), this.getZ(),
                    14, 0.3, 0.3, 0.3, 0.1);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 青衣投掷三叉戟失败: {}", t.toString());
        }
    }

    /**
     * v117: 恒星三叉戟(`tridents_n_stuff:stellaris`)的物品栈。
     * 取不到这个物品(没装 TnS)就退回当前手持那件, 保证"永远投得出东西"。
     */
    private ItemStack qingyiStellarisStack() {
        try {
            net.minecraft.world.item.Item it = net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getValue(new net.minecraft.resources.ResourceLocation(QINGYI_WEAPONS[QINGYI_STELLARIS_INDEX]));
            if (it != null) return new ItemStack(it);
        } catch (Throwable ignored) {
        }
        return this.getMainHandItem().copy();
    }

    /**
     * 青衣: 三叉戟**必定触发**的技能效果(按当前那一件 TnS 独特武器决定) —— 2026-09-12 v28 用户定案。
     * 0 风暴追寻者: 引雷 + 缓慢 II / 1 亵渎者: 凋零 III / 2 深渊回响: 失明 II + 反胃 + 击飞 / 3 恒星: 飘浮 II + 缓慢 III
     */
    private void applyQingyiTridentEffect(ServerLevel sl, LivingEntity target) {
        try {
            switch (this.qingyiIndex) {
                case 0: {
                    var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.moveTo(target.getX(), target.getY(), target.getZ());
                        bolt.setVisualOnly(true);       // 只做视觉, 不烧地/不额外伤害
                        net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, bolt);
                        sl.addFreshEntity(bolt);
                    }
                    target.hurt(this.damageSources().indirectMagic(this, this), this.manorSkillDamage(target,
                            45.0F + this.qingyiDamageBonus(), 1.5));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15 * 20, 1));
                    break;
                }
                case 1: {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 15 * 20, 2));
                    target.hurt(this.damageSources().indirectMagic(this, this), this.manorSkillDamage(target,
                            36.0F + this.qingyiDamageBonus(), 1.5));
                    break;
                }
                case 2: {
                    /* v37: 原失明 -> 挖掘疲劳(不允许失明) */
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 15 * 20, 1));
                    net.farmingtales.ftboss.util.BossKnockback.push(target, 0.0, 0.9, 0.0);
                    break;
                }
                case 3: {
                    /* v37: 原漂浮 -> 发光(不允许漂浮) */
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15 * 20, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15 * 20, 2));
                    break;
                }
                default:
                    break;
            }
            sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 1.3f, 1.0f);
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(),
                    24, 0.4, 0.6, 0.4, 0.2);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣三叉戟必触发效果: 第 {} 件 ({})",
                    this.qingyiIndex + 1, QINGYI_WEAPONS[this.qingyiIndex]);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 三叉戟效果异常: {}", t.toString());
        }
    }

    /**
     * 青衣的**自定义三叉戟命中玩家**时的回调(2026-09-12 v29 用户定案):
     * ① 用的是**风暴追寻者**(第 0 件)时 -> **必定召唤真实闪电**;
     * ② 累计**连续命中 5 次** -> 玩家获得 **4 个随机减益**。
     */
    public void onTridentHit(Player p, int weaponIndex) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        /* v95 按用户要求「每召唤一把三叉戟则全部伤害 +1」: 三叉戟**本体命中**的伤害是原版
           ThrownTrident 结算的(拿的是武器基础攻击力), 改不动, 所以在这里**补一层等量的魔法伤害**。
           先清无敌帧, 否则刚吃下原版那一击、这一下会被无敌帧吞掉。 */
        float bonus = this.qingyiDamageBonus();
        /* v102: 三叉戟本体命中的伤害是原版 ThrownTrident 按**武器基础攻击力**结算的(与投掷者属性无关),
           所以这里把"攻击力 +35% 应该多出来的那部分"按本体攻击力估算, 一并补成魔法伤害。 */
        bonus += (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25);
        bonus = this.manorSkillDamage(p, bonus, 1.5);
        if (bonus > 0.0f) {
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().indirectMagic(this, this), bonus);
        }
        if (weaponIndex == 0) {
            try {
                var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(p.getX(), p.getY(), p.getZ());
                    bolt.setVisualOnly(false);       // 真闪电: 会麻痹一下 + 少量伤害
                    net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, bolt);
                    sl.addFreshEntity(bolt);
                }
                /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
                p.invulnerableTime = 0;
                p.hurt(this.damageSources().indirectMagic(this, this), (float)32);
            } catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] 召唤闪电失败: {}", t.toString());
            }
        }
        this.qingyiTridentHits++;
        FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣三叉戟命中玩家(第 {} 件): 连击 {}/{}",
                weaponIndex + 1, this.qingyiTridentHits, QINGYI_TRIDENT_STREAK);
        if (this.qingyiTridentHits >= QINGYI_TRIDENT_STREAK) {
            this.qingyiTridentHits = 0;
            java.util.List<net.minecraft.world.effect.MobEffect> pool =
                    new java.util.ArrayList<>(java.util.Arrays.asList(RANDOM_DEBUFF_POOL));
            java.util.Collections.shuffle(pool, new java.util.Random(this.random.nextLong()));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4 && i < pool.size(); i++) {
                p.addEffect(new MobEffectInstance(pool.get(i), 15 * 20, 1));
                sb.append(pool.get(i).getDisplayName().getString()).append(' ');
            }
            p.displayClientMessage(Component.literal("§5【星戟连击】§7你被挂上 4 个减益: " + sb), true);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1.0, p.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
            sl.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 1.3f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣三叉戟 {} 连击达成 -> {} 获得 4 个减益: {}",
                    QINGYI_TRIDENT_STREAK, p.getName().getString(), sb.toString().trim());
        }
    }

    /**
     * 青衣 技能「恒星三叉戟雨」(2026-09-12 v29 用户定案): 在空中召唤 **16 把恒星三叉戟**投掷向玩家。
     */
    private void skillStarTridentRain(ServerLevel sl, LivingEntity target) {
        playCast(36, 2);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        this.qingyiRain = QINGYI_RAIN_COUNT;
        this.qingyiRainTimer = 0;
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5f, 0.8f);
        sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 1.2f, 1.4f);
        sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 10.0, target.getZ(),
                60, 3.0, 1.0, 3.0, 0.15);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣「恒星三叉戟雨」: 空中召唤 {} 把恒星三叉戟 -> {}",
                QINGYI_RAIN_COUNT, target.getName().getString());
    }

    /** 从玩家头顶 ~12 格处丢下一把恒星三叉戟(带随机落点) */
    private void dropStarTrident(ServerLevel sl, LivingEntity target) {
        try {
            ItemStack stack = ItemStack.EMPTY;
            var stellaris = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tridents_n_stuff", "stellaris"));
            if (stellaris != null) stack = new ItemStack(stellaris);
            if (stack.isEmpty()) stack = new ItemStack(Items.TRIDENT);
            var tri = new net.farmingtales.ftboss.entity.QingyiTridentEntity(sl, this, stack, QINGYI_STELLARIS_INDEX);
            double ang = this.random.nextDouble() * Math.PI * 2.0;
            double r = 1.0 + this.random.nextDouble() * 4.5;
            double x = target.getX() + Math.cos(ang) * r;
            double z = target.getZ() + Math.sin(ang) * r;
            double y = target.getY() + 12.0;
            tri.setPos(x, y, z);
            tri.shoot(target.getX() - x, (target.getY() + 0.6) - y, target.getZ() - z, 1.8f, 0.12f);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, tri);
            if (!QingyiTridentEntity.spawnOwned(sl, this, tri)) return;
            this.onQingyiTridentSummoned();          // v95: 星戟雨每落一把也一样 +1
            sl.sendParticles(ParticleTypes.END_ROD, x, y, z, 10, 0.2, 0.4, 0.2, 0.02);
            sl.playSound(null, x, y, z, SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1.0f, 1.5f);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 恒星三叉戟投放失败: {}", t.toString());
            this.qingyiRain = 0;
        }
    }

    private void tickQingyiFallingTrident(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (!this.allowsParallelMelee() || this.isNoAi() || !this.isAlive() || this.isInvulnerable()
                || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)
                || !net.farmingtales.ftboss.boss.ai.BossTargets.valid(target)
                || target.level() != level || this.isAlliedTo(target) || target.isAlliedTo(this)
                || this.distanceToSqr(target) > QingyiCombatRules.FALLING_TRIDENT_RANGE * QingyiCombatRules.FALLING_TRIDENT_RANGE
                || !this.hasLineOfSight(target)) {
            this.qingyiFallingTridentCooldown = 0;
            return;
        }
        if (this.qingyiFallingTridentCooldown > 0) this.qingyiFallingTridentCooldown--;
        if (this.qingyiFallingTridentCooldown > 0) return;
        this.dropStarTrident(level, target);
        this.qingyiFallingTridentCooldown = net.farmingtales.ftboss.balance.ManorBalanceHooks.tridentInterval(QingyiCombatRules.FALLING_TRIDENT_INTERVAL, this, "passive");
    }

    /** 扔一瓶含**随机 6 种减益**的溅射药水 */
    private void throwDebuffPotion(ServerLevel sl, LivingEntity target) {
        try {
            net.minecraft.world.effect.MobEffect[] pool = {
                    MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN,
                    MobEffects.POISON, MobEffects.WITHER, MobEffects.HUNGER, MobEffects.UNLUCK, MobEffects.GLOWING,
            };
            java.util.List<net.minecraft.world.effect.MobEffect> shuffled =
                    new java.util.ArrayList<>(java.util.Arrays.asList(pool));
            java.util.Collections.shuffle(shuffled, new java.util.Random(this.random.nextLong()));
            java.util.List<MobEffectInstance> effs = new java.util.ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < QINGYI_POTION_DEBUFFS && i < shuffled.size(); i++) {
                var eff = shuffled.get(i);
                effs.add(new MobEffectInstance(eff, 6 * 20, 0));   // 6 秒 x 技能倍率见下
                sb.append(eff.getDisplayName().getString()).append(' ');
            }
            ItemStack stack = new ItemStack(Items.SPLASH_POTION);
            net.minecraft.world.item.alchemy.PotionUtils.setCustomEffects(stack, effs);
            stack.setHoverName(Component.literal("§5青衣的混毒药剂"));
            var potion = new net.minecraft.world.entity.projectile.ThrownPotion(sl, this);
            potion.setItem(stack);
            potion.setPos(this.getX(), this.getEyeY() - 0.15, this.getZ());
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            potion.shoot(dx / len, dy / len + 0.12, dz / len, 0.9f, 1.0f);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, potion);
            sl.addFreshEntity(potion);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SPLASH_POTION_THROW, SoundSource.HOSTILE, 1.2f, 1.0f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 青衣扔出混毒药剂: {}", sb.toString().trim());
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 青衣扔药水失败: {}", t.toString());
        }
    }

    /* ==================== 紫菜: 河豚一整套减益(2026-09-12 v28 用户定案) ====================
       2026-09-13 v89 按用户要求「去除紫菜的铁魔法」: ① 招牌技能不再"毒雾/铁魔法高级法术各半",
       只剩毒雾爆发; ② 河豚减益池排除 {@code irons_spellbooks} 命名空间。 */

    /** 先按 id 精确找(纯净毒液 = armamentarium:pure_venom) */
    private static final String[][] ZICAI_DEBUFF_IDS = {
            {"armamentarium", "pure_venom"},        // 纯净毒液
            {"minecraft", "wither"},                // 凋零
            {"minecraft", "poison"},                // 中毒
            {"tridents_n_stuff", "bleeding"},       // 流血(TnS)
            {"legendary_monsters", "bleeding"},     // 流血(传奇怪物)
    };
    private static java.util.List<net.minecraft.world.effect.MobEffect> ZICAI_POOL;

    /** v73 按用户要求: 紫菜**每次只挂 3 种不同的减益**(以前是把整池 125 种全挂)
        2026-09-13 v75 按用户要求「每次攻击只能叠加两个」: 3 -> **2** 种 */
    public static final int ZICAI_PER_APPLY = 2;
    /** 挂上去的减益时长(15 秒) */
    public static final int ZICAI_DEBUFF_TICKS = 15 * 20;
    /**
     * v74 按用户要求「debuff 如果施加了同种则提高一层」: 同一种减益再中一次 -> **等级 +1**。
     * <p>上限 {@value #ZICAI_DEBUFF_MAX_AMP} 级(即 V 级) —— 不设上限的话, 反复抽到凋零/中毒会无限叠,
     * 几百秒后一下就秒人。要更狠/不封顶改这个常量即可。
     */
    public static final int ZICAI_DEBUFF_MAX_AMP = 4;

    /**
     * ★★★ 2026-09-14 v106 按用户反馈「紫菜 debuff 会导致崩溃」★★★
     *
     * <p>减益池是**扫全注册表**收来的(实测 62~125 种, 含各种模组效果)。里面只要有一个效果
     * 的 {@code addAttributeModifiers} 用了**固定 UUID**、又没处理"同一个修饰符已经挂在属性上"
     * 的情况, 第二次施加就会抛
     * {@code IllegalArgumentException: Modifier is already applied on this attribute!} ——
     * 那不是玩家掉线, 是整个服务端 tick 崩掉。
     *
     * <p>双保险: ① 施加前先把同种效果**移除**(连带把它挂的属性修饰符摘干净), 再干净地加一次;
     * ② 单次施加整个包在 try/catch 里, 出事就记一条带**效果注册 id**的日志并把该效果
     * **拉黑**(本场不再抽它)。这样即使还有漏网的坏效果, 最坏也只是少一种 debuff, 不会崩。
     */
    private static final java.util.Set<net.minecraft.world.effect.MobEffect> ZICAI_BLOCKED =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** 在目标身上**安全地**挂一个减益(见 {@link #ZICAI_BLOCKED}); 返回是否成功 */
    private boolean safeAddDebuff(LivingEntity target, net.minecraft.world.effect.MobEffect eff, int amp) {
        if (net.farmingtales.ftboss.boss.ai.CombatStatusHooks.sharedHarmful(eff)) {
            return net.farmingtales.ftboss.boss.ai.CombatStatusHooks.apply(target, eff, ZICAI_DEBUFF_TICKS);
        }
        if (eff == null || ZICAI_BLOCKED.contains(eff)) return false;
        try {
            int safeAmplifier = ZicaiDebuffRules.safeAmplifier(eff, Math.min(ZICAI_DEBUFF_MAX_AMP, Math.max(0, amp)));
            if (safeAmplifier < 0) return false;
            /* ① 先摘掉旧实例: 把可能残留的固定 UUID 属性修饰符一起移除, 避免"已施加"异常 */
            if (target.hasEffect(eff)) target.removeEffect(eff);
            return target.addEffect(new MobEffectInstance(eff, ZICAI_DEBUFF_TICKS, safeAmplifier));
        } catch (Throwable t) {
            ZICAI_BLOCKED.add(eff);
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 紫菜减益 {} 施加失败({}) —— 已拉黑, 本场不再使用",
                    net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(eff), t.toString());
            return false;
        }
    }

    /**
     * 这次该给几级: 目标身上**已经有同种**就 +1 层, 没有就用基础等级
     * (凋零/中毒基础 III, 其余基础 II)。
     */
    private static int zicaiAmpFor(LivingEntity target, net.minecraft.world.effect.MobEffect effect) {
        int base = (effect == MobEffects.WITHER || effect == MobEffects.POISON) ? 2 : 1;
        MobEffectInstance cur = target.getEffect(effect);
        if (cur == null) return base;
        return Math.min(ZICAI_DEBUFF_MAX_AMP, Math.max(base, cur.getAmplifier() + 1));
    }

    /**
     * v72: 这条减益到底该不该挂给玩家。
     *
     * <p>v62 的"收全部负面效果"实测收到 **125 种**, 日志里混进了三类不该出现的东西:
     * <ul>
     *   <li>**玩家禁用效果**: 失明 / 黑暗 / 飘浮(用户早就定过"玩家不许有这三种"), 还有反重力类;</li>
     *   <li>**HUD 破坏类**: Xaero 小地图的"小地图不可用 / 实体雷达不可用 / 路径点不可用 / No World Map"
     *       —— 中了就等于把玩家的地图功能关掉, 明显不是"战斗减益";</li>
     *   <li>**假减益**: 发光 / 不祥之兆(中性, isBeneficial() 为 false 但根本不痛不痒) 和 **瞬间伤害**
     *       (瞬间类效果用 addEffect 挂时长没有意义)。</li>
     * </ul>
     * 所以只保留 **HARMFUL 类别**、且不带下面这些关键字的效果。
     */
    private static final String[] ZICAI_BLOCK_KEYWORDS = {
            "blind", "darkness", "levitation", "gravity",          // 失明/黑暗/漂浮/反重力
            "minimap", "world_map", "worldmap", "waypoint", "radar", "cave_maps", "wm_",   // Xaero 等地图类
            "地图", "雷达", "路径点",
    };

    /**
     * 2026-09-13 v89 按用户要求「去除紫菜的铁魔法」。
     *
     * <p>第 ③ 步"剩下的负面效果全收(不限 mod)"会把 **Iron's Spells 'n Spellbooks** 的 HARMFUL 效果
     * 一起捞进来(实测该 mod 有 心停/灵魂燃烧/霜咬/枯萎/寒冷/减速/流血斩 等), 于是紫菜会隔空给玩家挂
     * "止心术"这种本来只属于铁魔法法术的东西。既然要去掉铁魔法, 就整个命名空间一起排除。
     */
    /**
     * ★★★ 2026-09-14 v107 按用户反馈「紫菜 debuff 会导致崩溃」—— **真凶抓到了** ★★★
     *
     * <p>用户给的客户端日志里两次崩在同一个地方(<b>22:34:36 / 22:35:25</b>, 正是紫菜上场那几分钟):
     * <pre>
     * net.minecraft.ReportedException: Ticking entity            (LocalPlayer)
     * Caused by: java.lang.NullPointerException: Cannot invoke "MinecraftServer.m_6846_()"
     *            because the return value of "PacketDistributor.getServer()" is null
     *   at net.theholyraj.rajswordmod.network.ModMessages.sendToClients(ModMessages.java:70)
     *   at net.theholyraj.rajswordmod.world.mobeffects.custom.HolyFireEffect.applyEffectTick(HolyFireEffect.java:46)
     * Suspected Mod: The Eight Renowned Blades (rajswordmod)
     * crash-2026-09-13_22.34.36-client.txt / 22.35.25-client.txt
     * </pre>
     * 崩溃报告里的 Entity NBT 显示玩家身上挂着
     * {@code rajswordmod:holy_fire  Duration=300, Amplifier=1} ——
     * **300 tick / 等级 1 正好等于我们紫菜减益的 15 秒 + 叠一层**, 也就是这只效果是**紫菜河豚给挂上去的**。
     * 那个效果在自己的 {@code applyEffectTick} 里**无条件调用发包**, 客户端没有 server →
     * NPE 当场把**客户端**崩掉(裸客户端=闪退, 装 NotEnoughCrashes 才只是弹个报告)。
     *
     * <p>所以"施加时 try/catch"救不了它(崩的是之后**客户端 tick**那一下, 不在我们的调用栈里) ——
     * 唯一解法就是**别给玩家挂**。整个 {@code rajswordmod}(八把名刀)命名空间一起排除:
     * 同一个 mod 的其它效果同样可疑, 而减益池本来就是"扫全注册表"收来的, 少一个 mod 毫无损失。
     */
    private static final String[] ZICAI_BLOCK_NAMESPACES = {
            "irons_spellbooks",     // v89 按用户要求去掉铁魔法
            "rajswordmod",          // v107: 客户端 tick 里发包 -> 崩客户端(见上)
    };

    /** 再按 id 精确兜一道(万一有人把上面的命名空间条目删了, 这条还能挡住已知必崩的那个) */
    private static final String[] ZICAI_BLOCK_IDS = {
            "rajswordmod:holy_fire",
    };

    private static boolean zicaiAllowed(net.minecraft.world.effect.MobEffect eff) {
        if (net.farmingtales.ftboss.boss.ai.CombatStatusHooks.sharedHarmful(eff)) return true;
        if (eff == null) return false;
        if (eff.isBeneficial()) return false;
        if (eff.getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) return false;   // 剔掉中性(发光/不祥之兆)
        if (eff.isInstantenous()) return false;                                                        // 瞬间伤害挂时长没意义
        var key = ForgeRegistries.MOB_EFFECTS.getKey(eff);
        if (key == null) return false;
        String id = key.toString();
        for (String badId : ZICAI_BLOCK_IDS) {
            if (id.equals(badId)) return false;                                                        // v107: 已知会崩客户端的
        }
        for (String ns : ZICAI_BLOCK_NAMESPACES) {
            if (key.getNamespace().equals(ns)) return false;                                            // v89: 铁魔法整个排除
        }
        String path = key.getPath().toLowerCase(java.util.Locale.ROOT);
        String desc = eff.getDisplayName().getString();
        for (String bad : ZICAI_BLOCK_KEYWORDS) {
            if (path.contains(bad) || desc.toLowerCase(java.util.Locale.ROOT).contains(bad)) return false;
        }
        return true;
    }

    private static synchronized java.util.List<net.minecraft.world.effect.MobEffect> zicaiPool() {
        if (ZICAI_POOL != null) return ZICAI_POOL;
        java.util.List<net.minecraft.world.effect.MobEffect> list = new java.util.ArrayList<>();
        int armamentarium = 0;
        /* ① 2026-09-12 v62 按用户要求: 先收 **armamentarium 的全套负面效果**(用户点名"尤其是 armamentarium 的") */
        for (var entry : ForgeRegistries.MOB_EFFECTS.getEntries()) {
            var eff = entry.getValue();
            if (list.contains(eff)) continue;
            if (!ForgeRegistries.MOB_EFFECTS.getKey(eff).getNamespace().equals("armamentarium")) continue;
            if (!zicaiAllowed(eff)) continue;
            list.add(eff);
            armamentarium++;
        }
        /* ② 再收手工点名的 id(纯净毒液/凋零/中毒/流血 等) */
        for (String[] id : ZICAI_DEBUFF_IDS) {
            var eff = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id[0], id[1]));
            if (eff != null && !list.contains(eff) && zicaiAllowed(eff)) list.add(eff);
        }
        /* ③ 2026-09-12 v62 按用户要求: 剩下的**负面效果**全收(不限 mod) —— v72 起按 zicaiAllowed 过滤 */
        for (var entry : ForgeRegistries.MOB_EFFECTS.getEntries()) {
            var eff = entry.getValue();
            if (list.contains(eff) || !zicaiAllowed(eff)) continue;
            list.add(eff);
        }
        ZICAI_POOL = list;
        StringBuilder sb = new StringBuilder();
        for (var e : list) sb.append(e.getDisplayName().getString()).append('/');
        FTBossMod.LOGGER.info("[FTBoss][Easter] 紫菜河豚减益池 {} 种(其中 armamentarium {} 种, 已剔除失明/黑暗/飘浮/地图禁用/中性/瞬间类/铁魔法): {}",
                list.size(), armamentarium, sb.toString());
        return list;
    }

    /**
     * 河豚/毒雾命中: 从减益池里**随机抽 {@value #ZICAI_PER_APPLY} 种不同的**挂上。
     *
     * <p>2026-09-13 v73 按用户要求「紫菜一次挂 3 种不同的 debuff 而不是 100 个」——
     * v62 起这里是**把整池(实测 125 种)全挂**, 玩家身上一次性堆满、HUD 上只看得到前几个,
     * 实际体感是"糊了一脸但说不清中了什么"。现在每次只抽 3 种, 每次都不一样。
     */
    private void applyPufferDebuffs(ServerLevel sl, LivingEntity target) {
        net.farmingtales.ftboss.balance.ManorBalanceHooks.zicai(this, sl, target);
    }

    @Override
    public void ftboss$applyPufferSingle(ServerLevel sl, LivingEntity target) {
        var pool = zicaiPool();
        if (pool.isEmpty()) return;
        java.util.List<net.minecraft.world.effect.MobEffect> pick = new java.util.ArrayList<>(ZICAI_PER_APPLY);
        int guard = 0;
        int want = Math.min(ZICAI_PER_APPLY, pool.size());
        while (pick.size() < want && guard++ < 128) {
            var eff = pool.get(this.random.nextInt(pool.size()));
            if (!pick.contains(eff)) pick.add(eff);
        }
        StringBuilder sb = new StringBuilder();
        int ok = 0;
        for (var eff : pick) {
            /* v74: 同种已中就 +1 层(上限 ZICAI_DEBUFF_MAX_AMP 级) */
            int amp = zicaiAmpFor(target, eff);
            /* ★ v106: 走 safeAddDebuff(先摘旧实例 + try/catch + 坏效果拉黑), 坏效果不再能崩服务端 */
            if (!this.safeAddDebuff(target, eff, amp)) continue;
            ok++;
            /* 文案带等级, 例如"凋零 IV" —— 1.20.1 的 MobEffectInstance 没有 getDisplayName(),
               用原版 potion.withAmplifier + enchantment.level.N 拼 */
            Component shown = Component.translatable("potion.withAmplifier", eff.getDisplayName(),
                    Component.translatable("enchantment.level." + (target.getEffect(eff).getAmplifier() + 1)));
            sb.append(shown.getString()).append(' ');
        }
        if (ok <= 0) return;
        sl.sendParticles(ParticleTypes.SNEEZE, target.getX(), target.getY() + 1.0, target.getZ(),
                20, 0.4, 0.5, 0.4, 0.03);
        /* 让玩家看得见自己中了哪 3 种(以前 125 种根本看不出) */
        if (target instanceof Player pDebuff) {
            pDebuff.displayClientMessage(Component.literal("§2紫菜 §7给你挂了: §c" + sb.toString().trim()), true);
            /* v75 按用户要求: 每次攻击**同时去除玩家一个增益**(复用蛋蛋那套只抽正面效果的实现) */
            this.stripOneBuff(pDebuff);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 紫菜河豚给 {} 挂了 {} 种减益: {}",
                target.getName().getString(), ok, sb.toString().trim());
    }

    /* 2026-09-13 v89 按用户要求「去除紫菜的铁魔法」: 整个「施放铁魔法高级法术」的技能被删掉
       (以前是 {@code ZICAI_SPELLS} 里 6 个 irons_spellbooks 法术之一, 和毒雾各 50%),
       相关常量与 {@code skillZicaiMagic} 一并移除, 避免留下死代码。 */

    /**
     * ★ 2026-09-24 按用户要求「折耳根会更多使用暗影剑进行传送」。
     *
     * <p>「暗影突袭」的可传送目标 = **召唤者**，或者**本场庄园战的参战者**。
     *
     * <p>修的是什么：这一招原来直接用 {@link #canTeleportWith}，而那个判据是
     * {@code ManorTeleportRules.isSummoner(...)} —— **只认"开战那个人"**。
     * 多人局里 Boss 常常盯着非召唤者打（2026-09-24 晚那场：三人参战，
     * 折耳根全程追着 BlackEgret / gdollg，召唤者是 qyby123456），于是
     * {@code skillShadowStrike} 每次都在这行静默 return —— **整场日志里一次
     * 「折耳根『暗影突袭』」都没有**（用户反馈的就是这个现象）。
     *
     * <p>落地那一半（{@code executeShadowStrike}）本来就是宽松的
     * {@code combatPlayer}（存活 + 非创造 + 非旁观），所以只需要放开这道起手闸；
     * 参战者口径沿用召回/传送那套的 {@code ManorFight.isPlayerFighting}
     * （同维度 + 战场 96 格内），不猜"最近玩家"。
     *
     * <p>刷新点开外的"彩蛋水晶野生召唤"仍保持原口径（只认召唤者）——
     * 那一侧没有参战名单，放开等于让 Boss 对着路人乱闪。
     */
    private boolean canShadowStrike(LivingEntity target) {
        if (this.canTeleportWith(target)) return true;
        if (!(target instanceof ServerPlayer player)) return false;
        if (!this.combatPlayer(player) || player.level() != this.level()) return false;
        return net.farmingtales.ftboss.boss.ManorFight.isPlayerFighting(player);
    }

    /** 折耳根 招牌机制「暗影突袭」(2026-09-12 v21): 闪到目标背后 -> 20 伤害 + 凋零 III 6 秒 + 失明 II 5 秒 */
    private void skillShadowStrike(ServerLevel sl, LivingEntity target) {
        if (net.farmingtales.ftboss.integ.ManorSpellBridge.replace(this, target, "shadow_brand")) return;
        if (!this.canShadowStrike(target)) return;
        playCast(this.getChallengeDifficulty().acceleratedTicks(SHADOW_STRIKE_WINDUP), 1);
        double ox = this.getX(), oy = this.getY(), oz = this.getZ();
        Vec3 back = target.getLookAngle().scale(-1.0);          // 目标背后
        double tx = target.getX() + back.x * 2.2;
        double tz = target.getZ() + back.z * 2.2;
        boolean moved = this.randomTeleport(tx, target.getY(), tz, true);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.7f);
        /* 墨影 */
        for (double[] p : new double[][]{{ox, oy + 1.0, oz}, {this.getX(), this.getY() + 1.0, this.getZ()}}) {
            sl.sendParticles(ParticleTypes.SQUID_INK, p[0], p[1], p[2], 30, 0.6, 0.8, 0.6, 0.05);
            sl.sendParticles(ParticleTypes.SMOKE, p[0], p[1], p[2], 20, 0.4, 0.6, 0.4, 0.03);
        }
        /* ★ 2026-09-24 按用户要求「施法后 10% 留下虚影」：
           在**起跳原点**(ox,oy,oz)留一个残像，它过一会儿重放一次**这一招**、然后消散。 */
        if (this.random.nextDouble() < SHADOW_PHANTOM_CHANCE) {
            this.spawnShadowPhantom(sl, ox, oy, oz, target, PHANTOM_SKILL_SHADOW_STRIKE);
        }
        this.shadowStrikeTarget = target.getUUID();
        this.shadowStrikeWind = this.getChallengeDifficulty().acceleratedTicks(SHADOW_STRIKE_WINDUP);
        this.beginSkillCue(this.position(), 4.0, this.shadowStrikeWind, true);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 0.7f, 1.5f);
    }

    private void executeShadowStrike(ServerLevel sl) {
        Entity resolved = this.shadowStrikeTarget == null ? null : sl.getEntity(this.shadowStrikeTarget);
        this.shadowStrikeTarget = null;
        if (!(resolved instanceof LivingEntity target) || !target.isAlive()
                || target instanceof Player player && !this.combatPlayer(player)) {
            this.entityData.set(DATA_SKILL_CUE, new CompoundTag());
            return;
        }
        playImpactAction(14);
        this.releaseSkillCue(this.position());
        boolean hit = target.distanceToSqr(this) <= 16.0 && this.hasLineOfSight(target);
        if (hit) this.applyShadowStrikeEffect(sl, target);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.1f, 1.3f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根「暗影突袭」: 命中={} ({} 伤害)", hit, SHADOW_DAMAGE);
    }

    /**
     * 「暗影突袭」的**命中效果**结算（本体与残影重放共用这一份，别再抄第二遍）。
     *
     * <p>内容：失明 II（{@link #SHADOW_DARKNESS_TICKS}）+ 伤害 + 击退 + 凋零 III 6 秒 +
     * 叠一层「暗影侵蚀」。本体走 {@link #applyShadowStrikeEffect(ServerLevel, LivingEntity)}
     * 的默认参数（全伤害、一半转虚空）；残影重放走 25% + **整份真伤**。
     */
    private void applyShadowStrikeEffect(ServerLevel sl, LivingEntity target) {
        this.applyShadowStrikeEffect(sl, target, 1.0, false);
    }

    /**
     * @param damageMultiplier 伤害倍率（本体 1.0；残影重放 = {@link #PHANTOM_ECHO_DAMAGE_MULT} = 25%）
     * @param asTrueDamage     true = 这一份伤害**整份走真伤/穿甲**（{@code fellOutOfWorld}，与本体那半
     *                         "虚空伤害"同一套机制）；false = 原样"一半虚空 + 一半物理"
     */
    private void applyShadowStrikeEffect(ServerLevel sl, LivingEntity target, double damageMultiplier,
                                         boolean asTrueDamage) {
        /* v75 按用户要求: ① 给玩家**黑暗**; ② 总伤害 **-30%**; ③ 其中**一半变成虚空伤害**
           ★ v115 按用户反馈「折耳根的失明效果疑似失效」: 这里原来挂的是原版 **DARKNESS(黑暗)**,
           和技能文档写的"失明 II 5 秒"对不上(玩家看到的只是屏幕变暗, 也不吃失明的"看不见"效果)。
           现在改成文档承诺的 **失明 II**(时长仍按 SKILL_MULT 放大 = 15 秒)。 */
        net.farmingtales.ftboss.effect.ShadowBlindEffect.applyTo(target, SHADOW_DARKNESS_TICKS);
        float total = (float) (this.manorSkillDamage(target, (float) (SHADOW_DAMAGE * SHADOW_DAMAGE_CUT), 2.0)
                * damageMultiplier);
        if (asTrueDamage) {
            /* 残影重放：整份真伤/穿甲（不吃护甲），走的是与"虚空伤害"同一条路。 */
            target.invulnerableTime = 0;
            ManorAdaptiveDamage.hurtSkill(target, this, this.damageSources().fellOutOfWorld(), total);
        } else {
            /* 虚空伤害没有攻击者实体 -> 旧版全局钩子**不会**给它打折, 所以这里补回 1.5 倍,
               保持和"钩子时代"完全一样的实际数值 */
            float voidPart = (float) (total * SHADOW_VOID_HALF * 1.5);
            float physPart = total - voidPart;
            target.invulnerableTime = 0;
            target.hurt(this.damageSources().mobAttack(this), physPart);
            target.invulnerableTime = 0;
            ManorAdaptiveDamage.hurtSkill(target, this, this.damageSources().fellOutOfWorld(), voidPart);
        }
        if (target instanceof Player player) this.knockbackPlayer(player);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, (int)360, 2));
        /* v116 按用户要求: 突袭命中同样开「暗影侵蚀」(持续虚空伤害) */
        if (target instanceof ServerPlayer spVoid) this.applyShadowVoid(spVoid, sl);
        /* 2026-09-12 v54: 原来这里是"失明 II 5 秒";
           v75 按用户要求改成**黑暗**(两者都是视野干扰, 同时上没必要, 所以是替换不是叠加) */
        sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(),
                20, 0.3, 0.5, 0.3, 0.05);
    }

    /* ==================== ★ 2026-09-24 折耳根「虚影」====================
       见 {@link #SHADOW_PHANTOM_CHANCE} 的说明：暗影突袭的起跳原点按 10% 概率放一个
       **独立的人形紫色残影**（{@link ShadowPhantomEntity}，无碰撞/无 AI/无攻击），
       它延迟 {@link #PHANTOM_DELAY} tick 之后由本体复现一次「暗影突袭」的命中效果，
       再过 {@link #PHANTOM_LINGER} tick 消散。 */

    /**
     * ★ 2026-09-24 按用户要求「给折耳根加一个传送，施法后留下虚影」。
     *
     * <p><b>为什么挂在这里</b>：实战日志查下来，折耳根**从来没放过**原生的「暗影突袭」
     * （历史 12 份归档 + 当场日志里 0 次）—— 原因是 {@code castSkill} 里
     * {@code if (this.manorFiveSkills.tryStart(...)) return;} 排在折耳根分支**之前**，
     * 而 {@code ManorFiveCombatRules.supports(7)} 为真，所以他的技能槽早就被"扩展技能"
     * （影刃十字 / 噬影牢笼）占了，原生那一招是**死代码**。
     * 所以传送与留影改挂在他真正会放的技能上：{@link ManorFiveSkills#tryStart} 锁定技能后回调本方法。
     *
     * <p>两件事：
     * <ol>
     *   <li><b>传送</b>：先用暗影剑闪到目标背后 2.2 格（沿用原生突袭的手法与那道 canShadowStrike 闸）；</li>
     *   <li><b>留影</b>：{@value #SHADOW_PHANTOM_CHANCE_PERCENT}% 概率在**起跳原点**留下紫色残影，
     *       残影记下**这一招的 id**（{@code shadow_cross} / {@code shadow_cage}），
     *       0~5 秒后在原地重放一次（伤害 = 原伤害 25% 的真伤，见 {@link #PHANTOM_ECHO_DAMAGE_MULT}）。</li>
     * </ol>
     */
    public void onManorFiveCastStart(net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill skill) {
        if (this.getVariant() != ZHEERGEN_VARIANT || this.isMini() || skill == null) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        LivingEntity target = this.getTarget();
        try {
            double ox = this.getX(), oy = this.getY(), oz = this.getZ();
            if (target != null && this.canShadowStrike(target)) {
                Vec3 back = target.getLookAngle().scale(-1.0);
                boolean moved = this.randomTeleport(target.getX() + back.x * 2.2, target.getY(),
                        target.getZ() + back.z * 2.2, true);
                this.getLookControl().setLookAt(target, 60.0f, 60.0f);
                if (moved) {
                    sl.playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.7f);
                    for (double[] p : new double[][]{{ox, oy + 1.0, oz}, {this.getX(), this.getY() + 1.0, this.getZ()}}) {
                        sl.sendParticles(ParticleTypes.SQUID_INK, p[0], p[1], p[2], 30, 0.6, 0.8, 0.6, 0.05);
                        sl.sendParticles(ParticleTypes.SMOKE, p[0], p[1], p[2], 20, 0.4, 0.6, 0.4, 0.03);
                    }
                }
            }
            /* ★ 2026-10-10 用户「新增被动：往日之影」——
               目标身上**虚空侵蚀满层**时：① 每一次技能都必留残影（不再是 35% 概率）；
               ② 这份残影的伤害**整份按虚空伤害**结算（{@code fellOutOfWorld} 那条路，
               与「虚空侵蚀」每跳同源：穿甲、不吃护甲与魔法减免）。 */
            boolean pastShadow = target instanceof ServerPlayer spErosion
                    && net.farmingtales.ftboss.effect.ShadowErosionEffect.atFullStacks(spErosion);
            if (pastShadow) this.announcePastShadow(target);
            if (pastShadow || this.random.nextDouble() < SHADOW_PHANTOM_CHANCE) {
                this.spawnShadowPhantom(sl, ox, oy, oz, target, skill.id(), pastShadow);
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 折耳根传送/留影失败(已忽略, 不崩服): {}", t.toString());
        }
    }

    /**
     * ★ 2026-10-10 按用户要求「折耳根的传送追击效果较慢」新增：**庄园内的闪现追击**。
     *
     * <p>以前折耳根的传送只发生在两处：技能起手那一瞬（{@link #onManorFiveCastStart}）与
     * 庄园外的珍珠/闪现（{@link #huntStrayPlayer}，但庄园里被 {@code ManorFight.isManaged} 主动关掉）。
     * 所以战斗里玩家一拉开距离，他就只能一步步走过来 —— 观感很慢。
     *
     * <p>现在只要目标拉开 {@value #ZHEERGEN_BLINK_DIST} 格以上，每隔
     * {@value #ZHEERGEN_BLINK_COOLDOWN} tick(3 秒) 就闪到对方背后 2.2 格（沿用技能起手那套
     * {@code randomTeleport} + 传送门粒子 + 末影人音效），并立刻看向目标。
     * 距离超过 {@value #ZHEERGEN_BLINK_MAX} 格时**不自己闪** —— 那种情况交给庄园的召回/传送机制。
     */
    private void zheergenBlinkChase(ServerLevel sl) {
        net.farmingtales.ftboss.balance.ManorShadowBlink.tick(this);
    }

    /** 在指定位置放一个折耳根的紫色残影，并排好本体侧的"复现"结算（记录触发它的那一招）。 */
    private void spawnShadowPhantom(ServerLevel sl, double x, double y, double z, LivingEntity target,
                                    String skill) {
        this.spawnShadowPhantom(sl, x, y, z, target, skill, false);
    }

    /**
     * @param pastShadow 是否是「往日之影」（虚空侵蚀满层）留下的残影 ——
     *                   影响复现倍率（{@link #PHANTOM_PAST_SHADOW_MULT}）与日志口径
     */
    private void spawnShadowPhantom(ServerLevel sl, double x, double y, double z, LivingEntity target,
                                    String skill, boolean pastShadow) {
        try {
            ShadowPhantomEntity phantom = FTEntities.SHADOW_PHANTOM.get().create(sl);
            if (phantom == null) return;
            /* 复现延迟：用户口径「5~0s 内重新施放」—— 0~5 秒之间取随机值。 */
            int delay = PHANTOM_ECHO_MIN_DELAY
                    + this.random.nextInt(PHANTOM_ECHO_MAX_DELAY - PHANTOM_ECHO_MIN_DELAY + 1);
            /* ★ 2026-09-24 第二次改动：残影**侧移 0.9 格**再落位 —— 直接落在本体脚下时，
               一旦这次传送没成功（落点被挡等）本体就站在残影身上，模型完全被盖住，
               玩家眼里就是"没触发"。错开一点既能看见，又仍是"他刚才站的位置"。
               侧移方向取"本体→目标"的垂线（没目标就用随机朝向）。 */
            double dx = 1.0, dz = 0.0;
            if (target != null) {
                double fx = target.getX() - x, fz = target.getZ() - z;
                double len = Math.sqrt(fx * fx + fz * fz);
                if (len > 1.0E-4) { dx = -fz / len; dz = fx / len; } else { dx = 0.0; dz = 1.0; }
            } else {
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                dx = Math.cos(angle);
                dz = Math.sin(angle);
            }
            double px = x + dx * 0.9, pz = z + dz * 0.9;
            phantom.moveTo(px, y, pz, this.getYRot(), 0.0F);
            phantom.setLife(delay + PHANTOM_LINGER);   // 复现之后再活 LINGER tick 才消散
            if (!sl.addFreshEntity(phantom)) return;
            /* 复现由**本体**结算（伤害归属仍是折耳根），位置按残影所在处算。 */
            this.phantomEchoTicks = delay;
            this.phantomEchoSkill = skill;
            this.phantomEchoPastShadow = pastShadow;
            this.phantomEchoTarget = target == null ? null : target.getUUID();
            this.phantomEchoX = px;
            this.phantomEchoY = y;
            this.phantomEchoZ = pz;
            sl.playSound(null, px, y, pz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 0.5f);
            sl.sendParticles(ParticleTypes.REVERSE_PORTAL, px, y + 1.0, pz, 30, 0.5, 0.9, 0.5, 0.08);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根{}留下紫色残影 @{},{},{} "
                            + "(记录技能={}, {} tick 后复现一次, 伤害={}% {})",
                    pastShadow ? "「" + PASSIVE_PAST_SHADOW + "」" : "「暗影突袭」",
                    (int) px, (int) y, (int) pz, skill, delay, (int) (this.echoMultiplier() * 100),
                    pastShadow ? "虚空伤害" : "真伤");
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 折耳根残影生成失败(已忽略, 不崩服): {}", t.toString());
        }
    }

    /**
     * 残影到点：以**残影的位置**判定，把它**记录下来的那一招**重放一次。
     *
     * <p>两条分流：
     * <ul>
     *   <li>{@link #PHANTOM_SKILL_SHADOW_STRIKE}（原生「暗影突袭」）→ {@link #applyShadowStrikeEffect}
     *       的 25% 真伤分支；</li>
     *   <li>扩展技能的 id（{@code shadow_cross} / {@code shadow_cage}，也就是折耳根实际会放的招）
     *       → {@link ManorFiveSkills#echoCast}：以残影位置为中心、按**同一套形状判定**
     *       重放第 0 段（减益/击退/粒子都一致），伤害同样是 25% 真伤。</li>
     * </ul>
     */
    private void firePhantomEcho(ServerLevel sl) {
        try {
            double x = this.phantomEchoX, y = this.phantomEchoY, z = this.phantomEchoZ;
            String skill = this.phantomEchoSkill;
            LivingEntity target = null;
            if (this.phantomEchoTarget != null && sl.getEntity(this.phantomEchoTarget) instanceof LivingEntity locked
                    && locked.isAlive()) {
                target = locked;
            }
            if (target == null) {
                double best = 24.0 * 24.0;
                for (ServerPlayer p : sl.players()) {
                    if (!this.combatPlayer(p)) continue;
                    double d = p.distanceToSqr(x, y, z);
                    if (d < best) { best = d; target = p; }
                }
            }
            boolean hit = target != null
                    && target.distanceToSqr(x, y, z) <= PHANTOM_ECHO_RANGE * PHANTOM_ECHO_RANGE
                    && this.hasLineOfSight(target);
            /* ★ 2026-10-10「往日之影」：满层虚空侵蚀下的残影倍率见 {@link #PHANTOM_PAST_SHADOW_MULT}，
               两条分支都整份走**虚空伤害**（{@code fellOutOfWorld}：穿甲、不吃护甲与魔法减免）。
               trueDamage=true 就是这条虚空路 —— 与「虚空侵蚀」每跳同一个机制。 */
            double mult = this.echoMultiplier();
            String kind = this.phantomEchoPastShadow ? "虚空伤害·" + PASSIVE_PAST_SHADOW : "真伤";
            if (PHANTOM_SKILL_SHADOW_STRIKE.equals(skill)) {
                if (hit) this.applyShadowStrikeEffect(sl, target, mult, true);
                sl.playSound(null, x, y, z, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 0.8f, 1.5f);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 1.0, z, 24, 0.4, 0.6, 0.4, 0.05);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根残影重放「{}」: 命中={} ({}% {})",
                        skill, hit, (int) (mult * 100), kind);
            } else {
                var recorded = net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill.byId(skill);
                int hits = recorded == null ? 0
                        : this.manorFiveSkills.echoCast(sl, recorded, new Vec3(x, y, z), target, mult, true);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根残影重放「{}」: 成功命中 {} 人 ({}% {})",
                        skill, hits, (int) (mult * 100), kind);
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 折耳根残影复现失败(已忽略, 不崩服): {}", t.toString());
        } finally {
            this.phantomEchoTicks = -1;
            this.phantomEchoSkill = null;
            this.phantomEchoTarget = null;
            this.phantomEchoPastShadow = false;
        }
    }

    /** 本次残影复现的倍率：「往日之影」用 {@link #PHANTOM_PAST_SHADOW_MULT}，其余用 25%。 */
    private double echoMultiplier() {
        return this.phantomEchoPastShadow ? PHANTOM_PAST_SHADOW_MULT : PHANTOM_ECHO_DAMAGE_MULT;
    }

    /**
     * 「往日之影」第一次被唤醒时给玩家一句提示（只播一次/每场）。
     *
     * <p>2026-10-10 按用户要求新增的被动没有 GUI 图标，所以用**行动栏 + 聊天栏**留痕，
     * 与本模组「虚空侵蚀」「禁术拦截」那几处的做法一致。
     */
    private void announcePastShadow(LivingEntity target) {
        if (this.pastShadowAnnounced) return;
        this.pastShadowAnnounced = true;
        try {
            if (target instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("§5折耳根 §7的被动 §d§l" + PASSIVE_PAST_SHADOW
                        + " §7已唤醒：§7你的 §d虚空侵蚀 §7已满层 —— 他**每一次技能**都会留下残影"), true);
            }
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.0f, 0.8f);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 1.4, this.getZ(),
                        40, 0.6, 0.8, 0.6, 0.06);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根被动「{}」唤醒: {} 的虚空侵蚀已满层({} 层)",
                    PASSIVE_PAST_SHADOW,
                    target == null ? "?" : target.getName().getString(),
                    net.farmingtales.ftboss.effect.ShadowErosionEffect.MAX_STACKS);
        } catch (Throwable ignored) {
        }
    }

    /* ==================== v116 折耳根「暗影侵蚀」(持续虚空伤害 + 黑暗) ==================== */

    /** 开/续「暗影侵蚀」—— 折耳根每次打中玩家都从这里**叠一层**虚空 DoT(见 ShadowErosionEffect) */
    private void applyShadowVoid(ServerPlayer p, ServerLevel sl) {
        if (p == null || p.isSpectator() || p.isCreative()) return;   // 创造/旁观不挂(和 Boss 不索敌创造玩家一致)
        var eff = net.farmingtales.ftboss.effect.FTEffects.SHADOW_EROSION.get();
        if (eff == null) return;
        MobEffectInstance cur = p.getEffect(eff);
        int oldStacks = cur == null ? 0 : cur.getAmplifier() + 1;
        int stacks = Math.min(net.farmingtales.ftboss.effect.ShadowErosionEffect.MAX_STACKS, oldStacks + 1);
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(eff,
                net.farmingtales.ftboss.effect.ShadowErosionEffect.TICKS, stacks - 1, false, true, true));
        if (stacks > oldStacks) {          // 只在"层数真的涨了"那一下提示/记日志, 不刷屏
            int dps = (int) net.farmingtales.ftboss.boss.ai.BossOutgoingDamageNerfs.zheergenDamage(
                    net.farmingtales.ftboss.effect.ShadowErosionEffect.DAMAGE_PER_STACK * stacks);
            p.displayClientMessage(Component.literal("§5折耳根 §7的暗影缠住了你: §d虚空侵蚀 §7" + stacks
                    + " 层 §7(每秒 " + dps + " 点虚空伤害, 穿甲)"), true);
            FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根「虚空侵蚀」-> {} 叠到 {} 层(每秒 {} 点虚空伤害)",
                    p.getName().getString(), stacks, dps);
        }
    }

    /* ==================== v117 蛋蛋「信标压制」(按用户要求) ====================
       用户: 「蛋蛋现在会禁用玩家的信标加成，同时自身保留加成」。
       原版信标/潮涌核心给的效果都是 **ambient(环境)效果**(`MobEffectInstance.isAmbient() == true`),
       所以这里每 {@value #DANDAN_BEACON_PERIOD} tick 把 {@value #DANDAN_BEACON_RANGE} 格内玩家
       身上的 ambient 效果**全摘掉** —— 信标补一次我们摘一次, 效果上就等于"禁用"。
       **只动玩家**: 蛋蛋自己(以及它从玩家那儿同步来的增益)完全不受影响。 */
    public static final int DANDAN_BEACON_PERIOD = 10;
    /** v118 按用户要求「信标压制应该开局使用」: 范围 48 -> 96(整个庄园战场), 且**正在打庄园战的玩家无视距离** */
    public static final double DANDAN_BEACON_RANGE = 96.0;
    /** v119 按用户要求「蛋蛋的物理攻击伤害会逐渐提高，最高 200 点」:
        每次命中玩家 +{@value #DANDAN_ATK_PER_HIT} 攻击力(ADDITION 修饰符, 可叠加),
        直到"实吃伤害"达到 {@value #DANDAN_ATK_MAX} 为止(实吃 = 攻击力 × BOSS_DAMAGE_MULT)。 */
    public static final double DANDAN_ATK_MAX = 200.0;
    public static final double DANDAN_ATK_PER_HIT = 5.0;
    private static final java.util.UUID DANDAN_ATK_MOD =
            java.util.UUID.fromString("6f1c9d2a-3b47-4c58-9e21-7a0d5b8c4e13");
    /** 已经叠了多少次(重置时清零) */
    private int dandanHits = 0;

    /* ==================== v124 蛋蛋「虚空漩涡」(按用户要求) ====================
       用户: 「蛋蛋现在可以使用末影守卫的漩涡吸引玩家」。
       末影守卫(灾变)的漩涡是个独立实体 {@code cataclysm:void_vortex}(见
       {@link net.farmingtales.ftboss.boss.ai.EnderVortex}), 这里负责**把它开出来**并
       **自己把玩家往里吸** —— 玩家位置是客户端权威的, 光给它加 dp 拉不动人,
       所以用 {@code connection.teleport} 每 tick 硬拽(和雪黎那个"硬定身"同一套路)。
       吸力 {@value #DANDAN_VORTEX_PULL} 格/tick: 走路(4.3 格/秒 = 0.215/tick)必被吸进去,
       冲刺(5.6 格/秒 = 0.28/tick)勉强能挣脱 —— 想跑就得一直冲刺。 */
    /** 技能冷却 */
    public static final int DANDAN_VORTEX_CD = 20 * 20;
    /** 漩涡持续时长 */
    public static final int DANDAN_VORTEX_TICKS = 80;
    /** 吸引半径(格) */
    public static final double DANDAN_VORTEX_RADIUS = 21.0;
    /** 每 tick 往中心拽多少格 */
    public static final double DANDAN_VORTEX_PULL = 0.26;
    /** 每 tick 往上提多少(做出"被卷起来"的感觉) */
    public static final double DANDAN_VORTEX_LIFT = 0.12;

    private int dandanVortexCd = 6 * 20;      // 上场 6 秒后放第一次
    private int dandanVortexTicks = 0;
    private int dandanVortexLogCd = 0;
    private int dandanVortexPulls = 0;
    private double dandanVortexX, dandanVortexY, dandanVortexZ;
    private net.minecraft.world.entity.Entity dandanVortexEntity = null;

    /** 蛋蛋: 每次打中玩家就把攻击力往上顶一档(上限见 {@value #DANDAN_ATK_MAX}) */
    private void dandanRampAttack() {
        this.dandanHits++;
        var attr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr == null) return;
        double base = attr.getBaseValue();
        double capBonus = Math.max(0.0, 300.0 - base);
        double bonus = Math.min(capBonus, this.dandanHits * DANDAN_ATK_PER_HIT);
        attr.removeModifier(DANDAN_ATK_MOD);
        attr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                DANDAN_ATK_MOD, "ftboss.dandan_ramp", bonus,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }
    /** 提示限流: 玩家 UUID -> 上次提示的游戏刻 */
    private final java.util.Map<java.util.UUID, Long> beaconBanNotice = new java.util.HashMap<>();

    /** 蛋蛋: 摘掉附近玩家身上的"信标/潮涌"类加成(ambient 效果) */
    private void dandanBeaconBan() {
        if (this.getVariant() != 1 || this.isMini()) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        long now = sl.getGameTime();
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(this) > DANDAN_BEACON_RANGE * DANDAN_BEACON_RANGE
                    && !net.farmingtales.ftboss.boss.ManorFight.isPlayerFighting(p)) continue;   // v118: 庄园战内无视距离
            int stripped = 0;
            for (MobEffectInstance eff : new java.util.ArrayList<>(p.getActiveEffects())) {
                if (!eff.isAmbient()) continue;              // 只摘信标/潮涌这类环境效果
                p.removeEffect(eff.getEffect());
                stripped++;
            }
            if (stripped <= 0) continue;
            Long last = this.beaconBanNotice.get(p.getUUID());
            if (last == null || now - last >= 100L) {        // 每 5 秒最多提示一次
                if (this.beaconBanNotice.size() > 64) {
                    this.beaconBanNotice.entrySet().removeIf(en -> now - en.getValue() >= 100L);
                }
                this.beaconBanNotice.put(p.getUUID(), now);
                p.displayClientMessage(Component.literal("§e蛋蛋 §7压制了你的 §b信标加成 §7(" + stripped + " 项)"), true);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋信标压制 -> {} (摘掉 {} 项 ambient 效果)",
                        p.getName().getString(), stripped);
            }
        }
    }

    /** v124 蛋蛋「虚空漩涡」: 每 tick 结算(冷却 -> 起手 -> 吸引 -> 收场) */
    private void dandanVortexTick(ServerLevel sl) {
        if (this.getVariant() != 1 || this.isMini()) return;

        /* ① 进行中: 把半径内的玩家往中心硬拽 */
        if (this.dandanVortexTicks > 0) {
            int pulled = 0;
            double nearest = Double.MAX_VALUE;
            for (net.minecraft.server.level.ServerPlayer p : sl.players()) {
                if (!this.canTeleportWith(p)) continue;
                if (!p.isAlive() || p.isSpectator()) continue;
                if (!net.farmingtales.ftboss.boss.ai.BossTargets.valid(p)) continue;
                double dx = this.dandanVortexX - p.getX();
                double dz = this.dandanVortexZ - p.getZ();
                double dy = this.dandanVortexY - p.getY();
                double d2 = dx * dx + dz * dz + dy * dy;
                if (d2 > DANDAN_VORTEX_RADIUS * DANDAN_VORTEX_RADIUS) continue;
                double flat = Math.sqrt(dx * dx + dz * dz);
                double step = Math.min(DANDAN_VORTEX_PULL, Math.max(0.0, flat - 0.2));
                double nx = p.getX(), nz = p.getZ();
                if (flat > 1.0E-4 && step > 0.0) {
                    nx += dx / flat * step;
                    nz += dz / flat * step;
                }
                double ny = p.getY();
                double up = this.dandanVortexY + 1.0 - p.getY();
                if (up > 0.0) ny += Math.min(DANDAN_VORTEX_LIFT, up);
                p.connection.teleport(nx, ny, nz, p.getYRot(), p.getXRot());
                p.hurtMarked = true;
                p.fallDistance = 0.0f;
                pulled++;
                nearest = Math.min(nearest, Math.sqrt(d2));
            }
            this.dandanVortexPulls += pulled;
            /* 漩涡自己的视觉: 一圈往内收的传送门粒子 + 中心烟雾 */
            double ang0 = this.tickCount * 0.35;
            for (int i = 0; i < 10; i++) {
                double a = ang0 + i * (Math.PI * 2.0 / 10.0);
                double r = DANDAN_VORTEX_RADIUS * (1.0 - (this.tickCount % 40) / 40.0);
                sl.sendParticles(ParticleTypes.PORTAL,
                        this.dandanVortexX + Math.cos(a) * r, this.dandanVortexY + 0.4 + Math.sin(a * 2) * 0.4,
                        this.dandanVortexZ + Math.sin(a) * r, 2, 0.0, 0.0, 0.0, 0.05);
            }
            sl.sendParticles(ParticleTypes.REVERSE_PORTAL, this.dandanVortexX, this.dandanVortexY + 1.0,
                    this.dandanVortexZ, 6, 0.5, 0.6, 0.5, 0.05);
            if (--this.dandanVortexLogCd <= 0) {
                this.dandanVortexLogCd = 20;
                FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋虚空漩涡 吸引中: {} 名玩家(最近 {} 格), 剩 {} tick",
                        pulled, nearest == Double.MAX_VALUE ? "-" : String.format("%.1f", nearest),
                        this.dandanVortexTicks);
            }
            if (--this.dandanVortexTicks <= 0) {
                if (this.dandanVortexEntity != null) {
                    this.dandanVortexEntity.discard();
                    this.dandanVortexEntity = null;
                }
                sl.playSound(null, this.dandanVortexX, this.dandanVortexY, this.dandanVortexZ,
                        SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, 1.0f, 0.7f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋虚空漩涡 结束: 累计吸引 {} 人次", this.dandanVortexPulls);
            }
            return;
        }

        /* ② 冷却 */
        if (this.dandanVortexCd > 0) {
            this.dandanVortexCd--;
            return;
        }

        /* ③ 起手: 优先当前目标, 其次最近的有效玩家 */
        LivingEntity t = this.getTarget();
        if (t == null || !t.isAlive() || !net.farmingtales.ftboss.boss.ai.BossTargets.valid(t)) {
            t = null;
            double best = 24.0 * 24.0;
            for (net.minecraft.server.level.ServerPlayer p : sl.players()) {
                if (!net.farmingtales.ftboss.boss.ai.BossTargets.valid(p)) continue;
                double d = p.distanceToSqr(this);
                if (d < best) { best = d; t = p; }
            }
        }
        if (t == null) {
            this.dandanVortexCd = 20;      // 场上没人: 1 秒后再看
            return;
        }
        this.dandanVortexX = this.getX();
        this.dandanVortexY = this.getY() + 0.2;
        this.dandanVortexZ = this.getZ();
        this.dandanVortexTicks = DANDAN_VORTEX_TICKS;
        this.dandanVortexCd = DANDAN_VORTEX_CD;
        this.dandanVortexLogCd = 20;
        this.dandanVortexPulls = 0;
        this.dandanVortexEntity = net.farmingtales.ftboss.boss.ai.EnderVortex.spawn(
                sl, this, this.dandanVortexX, this.dandanVortexY, this.dandanVortexZ, DANDAN_VORTEX_TICKS + 20);
        sl.playSound(null, this.dandanVortexX, this.dandanVortexY, this.dandanVortexZ,
                SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.2f, 0.8f);
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.dandanVortexX, this.dandanVortexY + 2.0,
                this.dandanVortexZ, 1, 0.0, 0.0, 0.0, 0.0);
        /* v125 按用户要求「不需要提示玩家蛋蛋的末影漩涡」: 去掉 actionbar/聊天提示, 只留日志 */
        FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋「虚空漩涡」起手 @({}, {}, {}) 吸引半径 {} 格 / {} tick, 末影守卫实体={}",
                String.format("%.1f", this.dandanVortexX), String.format("%.1f", this.dandanVortexY),
                String.format("%.1f", this.dandanVortexZ), (int) DANDAN_VORTEX_RADIUS,
                DANDAN_VORTEX_TICKS, this.dandanVortexEntity != null);
    }

    /* ==================== v117 折耳根「连击转虚空」(按用户要求) ====================
       用户: 「折耳根如果连续命中玩家50次则伤害的50%变成虚空伤害」。
       计数与拆分都在 {@link net.farmingtales.ftboss.boss.ai.ShadowVoidSplit} 里做
       (每次打到玩家 +1, 打到非玩家立刻断连, 超过 {@value #SHADOW_STREAK_TIMEOUT} tick 没打中也会断)。
       满 {@value #SHADOW_STREAK_HITS} 次后, 它对玩家造成的**任意**伤害都有一半改走虚空(自带穿甲)。 */
    /** v119 按用户要求「25 次攻击后获得对玩家的**额外**50% 虚空伤害」(原来是 50 连击 + 把一半改成虚空) */
    public static final int SHADOW_STREAK_HITS = 25;
    /** v118: 断连超时 5 秒 -> 30 秒(原来一转眼就断, 50 连几乎叠不满); 打到非玩家也不再断连 */
    public static final int SHADOW_STREAK_TIMEOUT = 30 * 20;
    private int shadowStreak = 0;
    private boolean shadowVoidMode = false;
    private long shadowLastHitTick = 0L;

    /** 连击是否已满(满后 50% 伤害转虚空); 由 {@link net.farmingtales.ftboss.boss.ai.ShadowVoidSplit} 读 */
    public boolean shadowVoidMode() { return this.shadowVoidMode; }

    /** 供 {@link net.farmingtales.ftboss.boss.ai.ShadowVoidSplit} 调用: 记一次"打到玩家" */
    public void onShadowHitPlayer(long now) {
        this.shadowStreak++;
        this.shadowLastHitTick = now;
        if (!this.shadowVoidMode && this.shadowStreak >= SHADOW_STREAK_HITS) {
            this.shadowVoidMode = true;
            FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根 连续命中玩家 {} 次 -> 此后它对玩家的伤害 50% 转为虚空",
                    this.shadowStreak);
        } else if (this.shadowStreak % 10 == 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] 折耳根 连续命中玩家 {} 次{}", this.shadowStreak,
                    this.shadowVoidMode ? "(已转虚空)" : "");
        }
    }

    /** 连击超时断连(每 tick 查一次) */
    private void shadowStreakTick() {
        if (this.shadowStreak <= 0) return;
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (sl.getGameTime() - this.shadowLastHitTick > SHADOW_STREAK_TIMEOUT) {
            this.shadowStreak = 0;
        }
    }

    /** 紫菜 招牌机制「毒雾爆发」(2026-09-12 v21): 4.5 格毒雾 —— 14 伤害 + 中毒 III 10 秒 + 缓慢 II 6 秒 */
    private void skillPoisonBurst(ServerLevel sl, LivingEntity target) {
        this.poisonWind = this.getChallengeDifficulty().acceleratedTicks(POISON_WINDUP);
        playCast(this.poisonWind, 0);
        this.beginSkillCue(this.position(), POISON_RADIUS, this.poisonWind, true);
        this.getLookControl().setLookAt(target, 60.0f, 60.0f);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    private void executePoisonBurst(ServerLevel sl) {
        net.farmingtales.ftboss.boss.ai.CombatStatusHooks.grantValor(this);
        this.playImpactAction(16);
        this.releaseSkillCue(this.position());
        double x = this.getX(), y = this.getY(), z = this.getZ();
        sl.sendParticles(ParticleTypes.SNEEZE, x, y + 0.6, z, 28, 2.3, 0.25, 2.3, 0.04);
        sl.sendParticles(ParticleTypes.ITEM_SLIME, x, y + 0.4, z, 16, 1.8, 0.2, 1.8, 0.04);
        sl.sendParticles(ParticleTypes.POOF, x, y + 1.0, z, 30, 1.4, 0.6, 1.4, 0.06);
        sl.playSound(null, x, y, z, SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.HOSTILE, 1.4f, 0.7f);
        sl.playSound(null, x, y, z, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.HOSTILE, 1.2f, 0.9f);
        int hits = 0;
        for (Player p : sl.players()) {
            if (!this.combatPlayer(p) || p.distanceToSqr(x, y, z) > POISON_RADIUS * POISON_RADIUS) continue;
            /* v60 按用户要求: 去掉玩家的无敌帧 —— 每一下都实打实 */
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().mobAttack(this), POISON_DAMAGE);
            p.addEffect(new MobEffectInstance(MobEffects.POISON, (int)600, 2));
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int)360, 1));
            this.applyPufferDebuffs(sl, p);     // 2026-09-12 v28: 毒雾里同样挂满那套减益
            hits++;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] 紫菜「毒雾爆发」命中 {} 人 ({} 伤害 + 中毒 III 10 秒)", hits, POISON_DAMAGE);
    }

    /** 打一发: 弹道打到墙就停(不穿墙), 命中目标才结算伤害; 一轮打完统计压制 */
    private void pumpBurst() {
        if (!(this.level() instanceof ServerLevel sl)) { this.burstShots = 0; return; }
        this.burstShots--;
        LivingEntity t = this.getTarget();
        if (t == null || !t.isAlive()) { this.burstShots = 0; return; }
        /* 2026-09-12 v26 按用户要求: 朵拉的机枪射程 = 12 格, 超出就停火 */
        if (this.distanceToSqr(t) > BURST_RANGE * BURST_RANGE) {
            this.burstShots = 0;
            this.doraTempo.miss();
            FTBossMod.LOGGER.info("[FTBoss][Easter] 朵拉目标超出射程 {} 格 -> 停火", (int) BURST_RANGE);
            return;
        }

        Vec3 from = new Vec3(this.getX(), this.getEyeY() - 0.15, this.getZ());
        Vec3 to = new Vec3(t.getX(), t.getY() + t.getBbHeight() * 0.5, t.getZ());

        /* 2026-09-13 v88 按用户要求「朵拉的射击准度低一点」:
           以前 to 就是目标身体中心 -> 中间没墙就 100% 命中(日志里永远 32/32)。
           现在每发在**水平方向**加一个随机偏差(取"枪口->目标"水平连线的垂线方向),
           偏差半径按距离在 NEAR..FAR 之间线性放大; 命中与否以**真的打中碰撞箱**为准。 */
        Vec3 flat = new Vec3(to.x - from.x, 0.0, to.z - from.z);
        Vec3 side = flat.lengthSqr() < 1.0E-6
                ? new Vec3(1.0, 0.0, 0.0)
                : new Vec3(-flat.z, 0.0, flat.x).normalize();
        double dist = Math.sqrt(this.distanceToSqr(t));
        double t01 = net.minecraft.util.Mth.clamp((dist - 4.0) / (BURST_RANGE - 4.0), 0.0, 1.0);
        double spread = BURST_SPREAD_NEAR + (BURST_SPREAD_FAR - BURST_SPREAD_NEAR) * t01;
        Vec3 aim = to.add(side.scale((this.random.nextDouble() * 2.0 - 1.0) * spread));

        var hit = this.level().clip(new net.minecraft.world.level.ClipContext(from, aim,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        boolean clear = hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
        Vec3 end = clear ? aim : hit.getLocation();
        /* clear 只说明"没打到墙"; 还要看这一发到底有没有穿过目标的碰撞箱(v88) */
        boolean onTarget = clear && t.getBoundingBox().inflate(0.06).clip(from, end).isPresent();

        /* 枪线由客户端纯网格绘制，不进入粒子引擎。 */
        WeaponFx.send(sl, Kind.DORA_TRACER, Theme.ARC, from, end, 0.12, this.getYRot());
        net.farmingtales.ftboss.balance.ManorExpansionHooks.bullet(this, sl, t);
        sl.playSound(null, this.getX(), this.getY() + 1.2, this.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.HOSTILE, 0.65f, 1.7f);

        if (onTarget) {
            /* 2026-09-12 v28: 16 点"物理 + 魔法"混合伤害 —— 先物理(吃护甲), 再清无敌帧打魔法(穿甲) */
            t.invulnerableTime = 0;
            boolean physicalHit = t.hurt(this.damageSources().mobAttack(this), BURST_DAMAGE_PHYS);
            t.invulnerableTime = 0;
            boolean magicHit = t.hurt(this.damageSources().indirectMagic(this, this), BURST_DAMAGE_MAGIC);
            if (physicalHit || magicHit) {
                this.burstHits++;
                if (t instanceof Player player) {
                    this.doraKnockback(player, BURST_KNOCKBACK, BURST_KNOCKBACK_UP);
                    this.doraHit(player);
                }
            } else this.doraTempo.miss();
            WeaponFx.send(sl, Kind.DORA_IMPACT, Theme.GOLD, end, end, 0.34, this.getYRot());
        } else {
            /* 打墙或擦身而过：网格曳光仍会明确标出落点，但不结算伤害。 */
            this.doraTempo.miss();
        }

        if (this.burstShots <= 0) {
            if (this.burstHits >= 5) {   // 压制: 命中过半 -> 缓慢 III (4 秒 x3 = 12 秒)
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int)240, 2));
                /* v124 按用户反馈「朵拉的对玩家造成流血效果未生效」: 她的招牌是这把机枪,
                   光靠"近战武器命中"挂流血基本挂不到 —— 压制射击打满也挂流血。 */
                net.farmingtales.ftboss.boss.ai.ExtraDebuffs.bleed(t, 8 * 20, 1);
                FTBossMod.LOGGER.info("[FTBoss][Easter] 朵拉压制生效: 命中 {} 发 -> 目标缓慢 III 4 秒 + 流血 8 秒", this.burstHits);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] 朵拉「压制射击」结束: 命中 {}/{}", this.burstHits, this.burstTotalShots);
            /* 2026-09-12 v34: 子弹打空 -> 装弹(这 3 秒持长枪冲刺) */
            if (this.getVariant() == 2) this.startReload(sl);
        }
    }

    /**
     * ★ v180 按用户要求「庄园 boss 单个击杀不要掉落掉落物, 得全部击杀才获取」:
     * 本开关控制**单只庄园 Boss 死亡时的自行掉落**。
     *
     * <p>关掉后, {@link #dropCustomDeathLoot} 里的三处掉落
     * (蛋蛋镰刀 / 柠檬猫锅铲 / 各自的招牌武器) 一律不发生;
     * 与之配套的 {@code ManorSpellDrops}(每只掉一对法术卷轴)也一并关掉。
     * 于是庄园的收益只剩**通关时那个共享胜利宝箱**
     * ({@code ManorVictoryLoot} —— 里面本来就有全部庄园武器 + 满阶卷轴,
     * 已逐项核对: 镰刀/锅铲/飞轮/雪黎厨具/麻雀羽毛/四把三叉戟一个不缺,
     * 所以关掉单体掉落**不会让任何武器变得拿不到**)。
     *
     * <p>设回 true 即恢复旧的"单只也掉"行为。
     */
    public static final boolean PER_BOSS_DROPS = false;

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        /* v180: 单只庄园 Boss 不掉东西 —— 收益统一走通关宝箱。 */
        if (!PER_BOSS_DROPS) return;
        boolean playerKill = recentlyHit || source.getEntity() instanceof Player
                || source.getEntity() instanceof net.minecraft.world.entity.OwnableEntity pet && pet.getOwnerUUID() != null;
        if (this.level() instanceof ServerLevel && net.farmingtales.ftboss.util.DandanScytheRules.shouldDrop(
                this.getVariant(), this.isMini(), this.isGiant(), this.isNoAi(),
                net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this), playerKill,
                this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))) {
            this.spawnAtLocation(new ItemStack(net.farmingtales.ftboss.item.FTBossItems.DANDAN_SCYTHE.get()));
        }
        if (this.level() instanceof ServerLevel && net.farmingtales.ftboss.util.LemonCatWeaponRules.shouldDrop(
                this.getVariant(), this.isMini(), net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this), recentlyHit,
                this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))) {
            var weapon = this.random.nextBoolean()
                    ? net.farmingtales.ftboss.item.FTBossItems.LEMONCAT_SKILLET.get()
                    : net.farmingtales.ftboss.item.FTBossItems.LEMONCAT_SPATULA.get();
            this.spawnAtLocation(new ItemStack(weapon));
            FTBossMod.LOGGER.info("[FTBoss][LemonCat] player weapon dropped: {}", ForgeRegistries.ITEMS.getKey(weapon));
        }
        if (!this.level().isClientSide && !net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(this)
                && net.farmingtales.ftboss.util.MaqueFeatherRules.shouldDrop(
                this.getVariant(), this.isMini(), this.isGiant(), recentlyHit)) {
            this.dropWeapon();
        }
    }

    @Override
    public void die(DamageSource source) {
        if (net.farmingtales.ftboss.boss.ai.ZhuniPairCombat.deferDeath(this, source)) return;
        this.lemonSacrifice.onRemoval(RemovalReason.KILLED);
        this.cancelDoraSniper();
        this.manorFiveSkills.cancel();
        this.gongdiSkills.reset();
        /* v103: 雪黎的自爆分身**被提前打死也会炸**(弱化版) —— 必须在 super.die 之前, 
           否则 die() 里可能已经把它移出世界了 */
        if (!this.level().isClientSide && this.bombFuse > 0 && !this.bombBlown
                && this.level() instanceof ServerLevel slX) {
            this.detonate(slX, true);
        }
        super.die(source);
        BossBars.untrack(this, this.bossBar);
        this.bossBar.removeAllPlayers();
        /* 2026-09-13 v85 修「庄园开打整场没有音乐」: 以前**彩蛋 Boss 从不 release BGM**
           (只有食物系 Boss 调 BossMusic), 于是音乐只能靠"兜底停止"收尾、真死时也奏不出落幕钟声。 */
        if (!this.level().isClientSide && this.level() instanceof ServerLevel slR) {
            net.farmingtales.ftboss.sound.BossMusic.release(slR, this);
        }
        /* 2026-09-13 v103: 雪黎也会召分身(自爆分身) —— 她死时一起收掉 */
        if (!this.level().isClientSide && this.level() instanceof ServerLevel slD) {
            if (this.getVariant() == XUELI_VARIANT && !this.isMini()) {
                this.xueliMaids.reset();          // v104: 女仆护法一起收掉
            }
            if ((this.getVariant() == 0 || this.getVariant() == XUELI_VARIANT) && !this.isMini()) {
                this.clearMinis(slD);
            }
        }
    }

    /** v54: 迷你分身寿命的**兜底上限**(5 分钟) —— 正常情况下主人一死就随主消散, 见 {@link #clearMinis} */
    public static final int MINI_GRACE_TICKS = 5 * 60 * 20;

    /** v58: 柚子本体阵亡/退场 -> 她的迷你分身**立刻**随主消散(不再留 5 分钟继续打人) */
    private void clearMinis(ServerLevel sl) {
        int n = 0;
        for (EasterBossEntity e : sl.getEntitiesOfClass(EasterBossEntity.class, this.getBoundingBox().inflate(128.0))) {
            if (e == this || !e.isMini() || e.getVariant() != this.getVariant()) continue;
            sl.sendParticles(ParticleTypes.CLOUD, e.getX(), e.getY() + 0.6, e.getZ(), 18, 0.3, 0.5, 0.3, 0.08);
            if (!this.getUUID().equals(ManorFightMembers.ownerId(e))) continue;
            sl.sendParticles(ParticleTypes.SMOKE, e.getX(), e.getY() + 0.4, e.getZ(), 8, 0.25, 0.3, 0.25, 0.03);
            e.discard();
            n++;
        }
        if (n > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] 柚子本体阵亡/退场 -> {} 个迷你分身随主消散", n);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.qizaiMagic.forceCancel();
        this.qizaiArsenal.cancel();
        if (reason.shouldDestroy()) this.xuanlanSky.cancel();
        this.lemonSacrifice.onRemoval(reason);
        this.cancelDoraSniper();
        this.manorFiveSkills.cancel();
        this.gongdiSkills.reset();
        /* 2026-09-12 v33: 被轮换/终章清场时, 一起把召唤的帮手收回
           2026-09-12 v58: 柚子的迷你分身也一起收回(以前只在 die() 里处理, 清场 discard 时会漏) */
        if (!this.level().isClientSide && reason.shouldDestroy() && this.level() instanceof ServerLevel slR) {
            this.clearSummons(slR);
            /* v103: 雪黎的自爆分身也一起收回(和柚子同理); v104: 她的女仆护法也清掉 */
            if (this.getVariant() == XUELI_VARIANT && !this.isMini()) this.xueliMaids.reset();
            if ((this.getVariant() == 0 || this.getVariant() == XUELI_VARIANT) && !this.isMini()) this.clearMinis(slR);
        }
        BossBars.untrack(this, this.bossBar);
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ManorDifficulty", this.getChallengeDifficulty().id());
        tag.putInt("variant", this.getVariant());
        tag.putBoolean("ManorMini", this.isMini());
        tag.putInt("ManorMiniLife", this.miniLife);
        tag.putInt("ManorBombFuse", this.bombFuse);
        tag.putBoolean("ManorBombBlown", this.bombBlown);
        if (this.manorSummonerId != null) tag.putUUID("ManorSummoner", this.manorSummonerId);
        else tag.remove("ManorSummoner");
        tag.putInt("manorFiveRotation", this.manorFiveSkills.rotation());
        /* v95: 青衣的三叉戟层数要存 —— setVariant 在区块重载时会把它清零(见 readAdditionalSaveData 的还原) */
        tag.putInt("qingyiTridentStacks", this.qingyiTridentStacks);
        /* ★ v115 按用户反馈「雪黎技能失效 / 青衣技能失效」: 技能轮换的"下一招"也必须存 ——
           以前它只活在实例里, 区块重载(setVariant)就把雪黎打回第 3 招、青衣打回"星戟雨",
           于是雪黎前三招(汤锅/平底锅/擀面杖)与青衣的投戟永远轮不到(审计报告 G 的日志证据:
           汤锅/平底锅/擀面杖 各 0 次、投戟 0 次)。 */
        tag.putInt("xueliNext", this.xueliNext);
        tag.putInt("xueliDialogueProgress", this.xueliDialogue.progress());
        if (this.getVariant() == XUELI_VARIANT && !this.isMini()) this.xueliMaids.save(tag);
        tag.putBoolean("qingyiAlt", this.qingyiAlt);
        tag.putBoolean("qingyiConfluenceNext", this.qingyiConfluenceNext);
        tag.putBoolean("doraSniperNext", this.doraSniperNext);
        tag.putInt("lemonStep", this.lemonStep);
        tag.putInt("lemonSummonLife", this.summonLife);
        net.minecraft.nbt.ListTag lemonSummons = new net.minecraft.nbt.ListTag();
        for (java.util.UUID summon : this.summons) lemonSummons.add(net.minecraft.nbt.NbtUtils.createUUID(summon));
        tag.put("lemonSummons", lemonSummons);
        tag.putInt("openingFlywheelsGranted", this.openingFlywheelsGranted);
        this.xuanlanSky.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setChallengeDifficulty(ManorDifficulty.fromId(tag.getString("ManorDifficulty")));
        if (tag.contains("variant")) this.setVariant(tag.getInt("variant"));
        this.manorSummonerId = tag.hasUUID("ManorSummoner") ? tag.getUUID("ManorSummoner") : null;
        if (tag.getBoolean("ManorMini")) {
            this.entityData.set(DATA_MINI, true);
            this.miniLife = Math.max(1, Math.min(MINI_LIFE_TICKS, tag.getInt("ManorMiniLife")));
            this.bombFuse = Math.max(0, tag.getInt("ManorBombFuse"));
            this.bombBlown = tag.getBoolean("ManorBombBlown");
        }
        net.farmingtales.ftboss.youzi.YouziMiniBalance.restore(this);
        this.refreshWalkingSpeed();
        this.manorFiveSkills.restoreRotation(tag.getInt("manorFiveRotation"));
        /* v95: setVariant 已经把层数清零+摘下修饰符, 这里按存档还原并重新挂上 */
        if (tag.contains("qingyiTridentStacks")) {
            this.qingyiTridentStacks = Math.max(0, Math.min(QINGYI_MAX_STACKS, tag.getInt("qingyiTridentStacks")));
            this.syncQingyiTridentBuffs();
        }
        /* v115: 技能轮换进度还原(必须在 setVariant 之后 —— setVariant 不再动它, 见那边的注释) */
        if (tag.contains("xueliNext")) this.xueliNext = Math.floorMod(tag.getInt("xueliNext"), 7);
        this.doraSniperNext = tag.getBoolean("doraSniperNext");
        this.lemonStep = tag.contains("lemonStep") ? Math.floorMod(tag.getInt("lemonStep"), 3) : 1;
        this.summonLife = Math.max(0, Math.min(SUMMON_LIFE_TICKS, tag.getInt("lemonSummonLife")));
        this.summons.clear();
        for (net.minecraft.nbt.Tag summon : tag.getList("lemonSummons", 11)) {
            if (summon instanceof net.minecraft.nbt.IntArrayTag uuid && uuid.getAsIntArray().length == 4) {
                this.summons.add(net.minecraft.nbt.NbtUtils.loadUUID(uuid));
            }
        }
        this.openingFlywheelsGranted = tag.contains("openingFlywheelsGranted")
                ? FlywheelCombatRules.openingProgress(tag.getInt("openingFlywheelsGranted"), 0)
                : FlywheelCombatRules.OPENING_COUNT;
        this.xueliDialogue.restore(tag.getInt("xueliDialogueProgress"), this.level().getGameTime());
        this.xueliMaids.load(tag);
        if (tag.contains("qingyiAlt")) this.qingyiAlt = tag.getBoolean("qingyiAlt");
        if (tag.contains("qingyiConfluenceNext")) this.qingyiConfluenceNext = tag.getBoolean("qingyiConfluenceNext");
        this.applyQingyiFlatBuffs();       // v102: 区块重载后把青衣的攻防强化补回去(transient 修饰符不会存盘)
        this.xuanlanSky.load(tag);
        /* ★ 2026-09-24: 「爱意滑翔」飞行会把 NoGravity 写进 NBT，而 {@code haleyFlying} 是瞬态字段 ——
           存档时正好在飞、读档时身上已经没有这个效果的话，Boss 会**永久悬空**。
           这里把重力还回来（雪黎天降瞬砸的悬停有自己的 xueliSlam* 状态，别去碰它）。 */
        if (!this.haleyFlying && this.xueliSlamWarn <= 0 && this.xueliSlamLeft <= 0) this.setNoGravity(false);
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    /**
     * 近战目标(2026-09-12 v33 用户定案): **攻击距离与玩家持平 = 3 格**。
     * 原版 {@code MeleeAttackGoal} 对玩家尺寸的 Boss 只有约 1.4 格(BB 宽 ×2 的平方), 所以这里覆写。
     * 远程 Boss(朵拉/文玄蓝) 不近战, 直接 canUse=false。
     */
    private static class PlayerReachMeleeGoal extends MeleeAttackGoal {
        private final EasterBossEntity boss;
        private int qingyiMeleeCooldown;
        private long meleeClockTick;
        private boolean meleeClockInitialized;

        PlayerReachMeleeGoal(EasterBossEntity boss, double speed, boolean followTarget) {
            super(boss, speed, followTarget);
            this.boss = boss;
            this.setFlags(EnumSet.noneOf(Goal.Flag.class));
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return boss.allowsParallelMelee() ? QingyiCombatRules.MELEE_REACH_SQUARED : 9.0;
        }

        @Override
        protected int getAttackInterval() {
            return this.boss.getMeleeAttackInterval();
        }

        private void refreshMeleeCooldown() {
            long now = this.boss.level().getGameTime();
            this.qingyiMeleeCooldown = net.farmingtales.ftboss.boss.ai.XueliPhases.remainingMeleeCooldown(
                    net.farmingtales.ftboss.util.ManorCombatClock.remainingCooldown(
                            this.qingyiMeleeCooldown, this.meleeClockTick, now, this.meleeClockInitialized), this.boss);
            this.meleeClockTick = now;
            this.meleeClockInitialized = true;
        }

        @Override
        public void tick() {
            this.refreshMeleeCooldown();
            LivingEntity target = this.boss.getTarget();
            if (target != null && !this.boss.blocksMelee()) {
                this.checkAndPerformAttack(target, this.boss.distanceToSqr(target));
            }
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target, double distanceSquared) {
            this.refreshMeleeCooldown();
            if (this.qingyiMeleeCooldown > 0 || distanceSquared > this.getAttackReachSqr(target)
                    || !boss.hasLineOfSight(target)) return;
            this.qingyiMeleeCooldown = this.getAttackInterval();
            boss.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            if (boss.doHurtTarget(target) && boss.isImpossibleChallenge()) boss.impossibleKiting.afterMelee(boss);
        }

        @Override
        public boolean canUse() {
            if (RANGED[boss.getVariant()]) return false;
            if (boss.getVariant() == SPARROW_VARIANT) return false;   // 2026-09-12 v35: 麻雀不攻击玩家
            return !boss.isNoAi() && net.farmingtales.ftboss.boss.ai.BossTargets.valid(boss.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() { this.refreshMeleeCooldown(); }

        @Override
        public void stop() {}
    }

    /** 追击: 走向目标并始终看向它(和苹果爵士同一套写法) */
    private class PursueGoal extends Goal {
        private final double speed;
        private final net.farmingtales.ftboss.boss.ai.BossApproach approach =
                new net.farmingtales.ftboss.boss.ai.BossApproach();

        PursueGoal(double speed) {
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            /* ★ 2026-09-24: 爱意滑翔期间飞行追击由 haleyFlightTick 的飞控接管, 地面追击让位 */
            if (EasterBossEntity.this.haleyFlying) return false;
            if (EasterBossEntity.this.isNoAi() || EasterBossEntity.this.movementLocked()
                    || EasterBossEntity.this.getVariant() == SPARROW_VARIANT) return false;
            LivingEntity t = EasterBossEntity.this.getTarget();
            return net.farmingtales.ftboss.boss.ai.BossTargets.valid(t);
        }

        @Override
        public boolean canContinueToUse() { return this.canUse(); }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void stop() {
            this.approach.reset(EasterBossEntity.this);
            EasterBossEntity.this.impossibleKiting.reset(EasterBossEntity.this);
        }

        @Override
        public void tick() {
            LivingEntity t = EasterBossEntity.this.getTarget();
            if (t == null) return;
            EasterBossEntity.this.getLookControl().setLookAt(t, 30.0F, 30.0F);
            if (net.farmingtales.ftboss.boss.ai.RangedBossTeleport.tick(EasterBossEntity.this, t)) return;
            if (EasterBossEntity.this.isImpossibleChallenge()) {
                if (EasterBossEntity.this.isMini() || EasterBossEntity.this.isGiant()) {
                    if (EasterBossEntity.this.distanceToSqr(t) > 9.0) EasterBossEntity.this.getNavigation().moveTo(t, this.speed);
                    else EasterBossEntity.this.getNavigation().stop();
                    return;
                }
                EasterBossEntity.this.impossibleKiting.tick(EasterBossEntity.this, t,
                        EasterBossEntity.this.allowsParallelMelee() ? QingyiCombatRules.CHASE_SPEED : this.speed,
                        RANGED[EasterBossEntity.this.getVariant()]);
                return;
            }
            boolean ranged = RANGED[EasterBossEntity.this.getVariant()];
            boolean dora = EasterBossEntity.this.getVariant() == DORA_VARIANT;
            double stop = ranged ? (dora ? 6.0 : 10.0) : 2.3;
            double resume = ranged ? (dora ? 9.0 : 14.0) : 2.8;
            double retreat = ranged && EasterBossEntity.this.doraSpearLeft <= 0 ? (dora ? 2.5 : 6.0) : 0;
            double navSpeed = EasterBossEntity.this.allowsParallelMelee() ? QingyiCombatRules.CHASE_SPEED
                    : EasterBossEntity.this.getVariant() == 6 ? 0.9 : ranged ? this.speed : 1.2;
            this.approach.tick(EasterBossEntity.this, t, navSpeed, stop, resume, retreat, dora ? 4.0 : 8.0);
        }
    }

    /** 供召唤道具/指令使用: 生成指定变体的 Boss */
    public static boolean spawn(ServerLevel sl, int variant, double x, double y, double z, float yaw) {
        if (!net.farmingtales.ftboss.boss.SummonGate.isAllowedDim(sl)) return false;
        var type = FTEntities.EASTER_BOSS.get();
        EasterBossEntity e = type.create(sl);
        if (e == null) return false;
        e.moveTo(x, y, z, yaw, 0f);
        e.setVariant(variant);
        sl.addFreshEntity(e);
        return true;
    }

    /** 掉落: 目前掉 1 个自己的武器(能取到才掉) + 记录日志 */
    public void dropWeapon() {
        String id = WEAPONS[this.getVariant()];
        if (id == null || id.isEmpty()) return;
        try {
            var item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(id));
            if (item != null) this.spawnAtLocation(new ItemStack(item));
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 掉落武器失败 {}: {}", id, t.toString());
        }
    }
}
