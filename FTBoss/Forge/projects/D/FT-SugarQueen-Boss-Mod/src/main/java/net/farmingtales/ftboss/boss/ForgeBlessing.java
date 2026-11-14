package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * 【Forge 的祝福】(2026-09-13 v80 按用户要求)。
 *
 * <p>玩家**每 60 秒**自动获得一次祝福:
 * <ol>
 *   <li>**清除自身可净化减益**(只清 {@link MobEffectCategory#HARMFUL} 类别, 不会误删祝福自己给的
 *       套装效果 —— 那些是 NEUTRAL 类别);</li>
 *   <li>获得三件套装的**buff 效果 25 秒**(2026-09-14 v106 按用户要求: 等级 **IV**,
 *       即 amplifier {@value #BUFF_AMPLIFIER}):
 *     <ul>
 *       <li>{@code armamentarium:adamantine_effect} —— 金刚不坏(亚当套装效果);</li>
 *       <li>{@code armamentarium:damon_effect} —— 死亡王子套装效果;</li>
 *       <li>{@code armamentarium:lone_graveseeker_effect} —— 寻墓孤人(装备描述即"真正的幽匿共生体", 即幽匿套装)。</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p>id 是通过整合包里的汉化资源包({@code kubejs/assets/汉化材质.zip})逐条对出来的:
 * 只有 {@code adamantine_effect}(金刚不坏)与 {@code lone_graveseeker_effect}(幽匿共生体套装)能一一对上;
 * 整合包里**没有**叫"血王子"的套装效果(血王子只有一把焰形剑), 所以中间那件按同系的
 * "死亡王子套装效果"(damon_effect) 实现 —— 要换别的效果改 {@link #EFFECT_IDS} 即可。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ForgeBlessing {

    /** 每隔多少 tick 发一次祝福(60 秒) */
    public static final int PERIOD_TICKS = 60 * 20;
    /** 获得的套装效果持续多久(25 秒) */
    public static final int BUFF_TICKS = 25 * 20;
    /**
     * 三件套装效果的等级 —— **amplifier 3 = IV 级**(2026-09-14 v106 按用户要求,
     * 用户原话「玩家获得的三种护甲效果应该是 4 级」)。想改等级改这一个数。
     */
    public static final int BUFF_AMPLIFIER = 3;
    /** 三件套装效果(顺序即提示里的显示顺序) */
    public static final String[][] EFFECT_IDS = {
            {"armamentarium", "adamantine_effect"},          // 金刚不坏(亚当套)
            {"armamentarium", "damon_effect"},               // 死亡王子套
            {"armamentarium", "lone_graveseeker_effect"},    // 寻墓孤人 = 幽匿共生体套装
    };
    /**
     * 是否只在**真正交战中**才发祝福。
     * 2026-09-13 v84 按用户反馈「没进 Boss 战也触发了玩家 buff」: 默认改成 **true** ——
     * 判据见 {@link net.farmingtales.ftboss.boss.ai.FightState#inFight}: 玩家在庄园战场内,
     * 或者 64 格内有本模组 Boss 正把他当目标。想恢复"一直发"把它改回 false 即可。
     */
    public static final boolean ONLY_WHEN_BOSS_NEARBY = true;

    private static final List<MobEffect> CACHE = new ArrayList<>();
    private static boolean cacheTried = false;

    private ForgeBlessing() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!(e.player instanceof ServerPlayer p) || p.level().isClientSide) return;
        if (p.tickCount % PERIOD_TICKS != 0 || p.tickCount == 0) return;
        /* v84: "在打 Boss"才发 —— 不是"附近有 Boss 实体"(遗留/自己召唤的 Boss 不算) */
        if (ONLY_WHEN_BOSS_NEARBY
                && !net.farmingtales.ftboss.boss.ManorFight.isPlayerFighting(p)) return;   // v129: 只在**庄园战场**里发(打 benger 等场地 Boss 不再触发净化/加成)
        grant(p);
    }

    /** 发一次祝福: 清减益 + 三件套装效果 25 秒 */
    public static void grant(ServerPlayer p) {
        int cleared = 0;
        for (MobEffectInstance inst : new ArrayList<>(p.getActiveEffects())) {
            if (inst.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
            if (p.removeEffect(inst.getEffect())) cleared++;
        }
        StringBuilder sb = new StringBuilder();
        for (MobEffect eff : effects()) {
            if (eff == null) continue;
            /* ★★★ 2026-09-14 v106 按用户要求「玩家获得的三种护甲(套装)效果应该是 4 级」★★★
               原来是 level 0 = I 级, 现在给 amplifier 3 = **IV 级**。 */
            p.addEffect(new MobEffectInstance(eff, BUFF_TICKS, BUFF_AMPLIFIER, false, true, true));
            sb.append(eff.getDisplayName().getString()).append(' ');
        }
        if (p.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1.0, p.getZ(),
                    30, 0.6, 0.9, 0.6, 0.25);
            sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(),
                    14, 0.5, 0.8, 0.5, 0.12);
            sl.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2f, 1.3f);
        }
        p.displayClientMessage(Component.literal("§6§l【Forge 的祝福】§r§7清除了 §c" + cleared
                + " §7个减益 · 获得 §d" + sb.toString().trim() + " §7IV 级(" + (BUFF_TICKS / 20) + " 秒)"), false);
        FTBossMod.LOGGER.info("[FTBoss][祝福] {} 获得 Forge 的祝福: 清除 {} 个减益, 套装效果 {} 级 {} 秒 [{}]",
                p.getName().getString(), cleared, BUFF_AMPLIFIER + 1, BUFF_TICKS / 20, sb.toString().trim());
    }

    /** 三件套装效果(找不到的会记一次日志并跳过) */
    private static List<MobEffect> effects() {
        if (!cacheTried) {
            cacheTried = true;
            for (String[] id : EFFECT_IDS) {
                MobEffect eff = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id[0], id[1]));
                if (eff == null) {
                    FTBossMod.LOGGER.warn("[FTBoss][祝福] 找不到效果 {}:{} —— 已跳过", id[0], id[1]);
                    continue;
                }
                CACHE.add(eff);
            }
            FTBossMod.LOGGER.info("[FTBoss][祝福] 祝福套装效果就绪 {} 件: {}", CACHE.size(),
                    CACHE.stream().map(m -> m.getDisplayName().getString()).toList());
        }
        return CACHE;
    }
}
