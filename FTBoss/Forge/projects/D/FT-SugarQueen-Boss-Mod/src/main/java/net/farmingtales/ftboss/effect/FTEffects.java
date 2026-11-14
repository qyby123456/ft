package net.farmingtales.ftboss.effect;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 本模组的状态效果注册(2026-09-11: 先加【感染】) */
public final class FTEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, FTBossMod.MOD_ID);

    /** 【感染】: 玩家攻击敌人时受到"层数"点伤害 */
    public static final RegistryObject<MobEffect> INFECTION =
            EFFECTS.register("infection", InfectionEffect::new);

    /** 【屎？！！】(2026-09-12): 被答辩超人击中叠层 -> 方向键位混乱 + 持续扣法力 */
    public static final RegistryObject<MobEffect> POOP_CONFUSION =
            EFFECTS.register("poop_confusion", PoopConfusionEffect::new);

    /** 【虚空侵蚀】(2026-09-14 v118 按用户要求): 折耳根打中玩家叠层 -> 每秒按层数扣**虚空伤害**(穿甲), 可叠加 */
    public static final RegistryObject<MobEffect> SHADOW_EROSION =
            EFFECTS.register("shadow_erosion", ShadowErosionEffect::new);

    /** 【禁疗】(2026-09-14 v119 按用户要求「禁疗做成一个 buff」): 有这个效果就回不了血 */
    public static final RegistryObject<MobEffect> HEAL_BAN =
            EFFECTS.register("heal_ban", HealBanEffect::new);

    /** 【血怒】(2026-09-14 v119 按用户要求「血怒也是」): 功底的血怒层数显示(等级 = 层数 - 1) */
    public static final RegistryObject<MobEffect> BLOOD_RAGE =
            EFFECTS.register("blood_rage", BloodRageEffect::new);

    /** 【蓝火】(2026-09-14 v119 按用户要求): 柠檬猫火焰的独立 debuff —— 每秒 10 点虚空伤害 */
    public static final RegistryObject<MobEffect> BLUE_FIRE =
            EFFECTS.register("blue_fire", BlueFireEffect::new);

    /** 【充能】(2026-09-14 v122 按用户要求「青衣召唤三叉戟后获得充能…持续 30s」): 青衣投戟层数的可见显示 */
    public static final RegistryObject<MobEffect> QINGYI_CHARGE =
            EFFECTS.register("qingyi_charge", QingyiChargeEffect::new);

    /** 【流血】(2026-09-14 v124 按用户反馈「朵拉的对玩家造成流血效果未生效」): 本模组自带的流血 DoT */
    public static final RegistryObject<MobEffect> BLEED =
            EFFECTS.register("bleed", BleedEffect::new);

    /** 【暗影笼罩】(2026-09-14 v125): 折耳根的失明/黑暗 —— 铁魔法「可见之戒」会把原版那两条摘掉,
     *  所以本模组自带一条**它不认识**的效果来保证"玩家真的瞎了" */
    public static final RegistryObject<MobEffect> SHADOW_BLIND =
            EFFECTS.register("shadow_blind", ShadowBlindEffect::new);

    /** 【震颤】(2026-09-14 v133): benger 连续下砸叠层, 满 10 层眩晕 1 秒并清零 */
    public static final RegistryObject<MobEffect> TREMOR =
            EFFECTS.register("tremor", TremorEffect::new);

    /** 【血怒·伪】(2026-09-14 v143): 黑色切割者命中叠层, 满 5 层 +100% 攻击 / +10% 移速 / 1% 真伤, 每 10 秒掉一层 */
    public static final RegistryObject<MobEffect> BLOOD_RAGE_FAKE =
            EFFECTS.register("blood_rage_fake", BloodRageFakeEffect::new);

    /** 【护甲撕裂】(2026-09-14 v144): 黑色切割者命中 -10 护甲（可叠 2 层） */
    public static final RegistryObject<MobEffect> ARMOR_SHRED =
            EFFECTS.register("armor_shred", ArmorShredEffect::new);

    public static final RegistryObject<MobEffect> LETHAL_TEMPO =
            EFFECTS.register("lethal_tempo", LethalTempoEffect::new);

    /** 【绽放】: 银之钥持续赋予；16 格内敌人会受到寄生（感染）效果。 */
    public static final RegistryObject<MobEffect> BLOOM =
            EFFECTS.register("bloom", BloomEffect::new);

    private FTEffects() {}

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
