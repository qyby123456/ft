package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.ArmorShredEffect;
import net.farmingtales.ftboss.effect.BloodRageFakeEffect;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 【武器命中特效：事件驱动】(2026-09-15 v151)。
 *
 * <p>为什么不再写在 {@code Item#hurtEnemy} 里：整合包装了 {@code l2damagetracker} 之类战斗追踪模组后，
 * 近战不一定会走 {@code hurtEnemy}（用户实测"黑切/血怒/暗影剑没实现"）。
 * 这里挂在 {@link LivingHurtEvent} 上 —— **只要伤害真的结算了就一定触发**，与用哪条攻击管线无关。
 *
 * <p>处理两件武器：
 * <ul>
 *   <li><b>黑色切割者</b>：叠【血怒·伪】+ 给目标挂【护甲撕裂】+ 补"无视 50% 护甲"的魔法伤害；</li>
 *   <li><b>暗影剑</b>：命中本身无额外效果（右键投掷珍珠是 {@code use()}，不受影响）。</li>
 * </ul>
 * 每次触发都会打一行日志，方便进游戏直接确认是否生效。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class WeaponHitEffects {

    /** 防止我们补的魔法伤害再次触发本处理器（否则会无限递归） */
    private static boolean resolving = false;

    /** 命中诊断去重：每种武器 id 只打一行（见 {@link #onHurt}）。 */
    private static final java.util.Set<String> HIT_DIAG = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private WeaponHitEffects() {}

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        if (resolving || e.getAmount() <= 0 || !e.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                || e.getSource().getDirectEntity() != e.getSource().getEntity()) return;
        try {
            Entity src = e.getSource().getEntity();
            if (src == null) src = e.getSource().getDirectEntity();
            if (src instanceof Projectile pr && pr.getOwner() != null) src = pr.getOwner();
            if (!(src instanceof Player p)) return;
            LivingEntity target = e.getEntity();
            if (target == p) return;
            if (p.level().isClientSide) return;

            var main = p.getMainHandItem();
            if (main.isEmpty()) return;
            var id = ForgeRegistries.ITEMS.getKey(main.getItem());
            if (id == null) return;

            /* ★ 一次性命中诊断（2026-09-16）：每种武器只打一行。
               用途：用户报"黑切命中不给血怒"时，一眼分清是
                 · 压根没拿黑切打（这行会显示别的武器 id / 或主手是空的）
                 · 还是拿黑切打了但下面那条 `命中 ... 血怒 N 层` 没出（那才是真回归）。
               ⚠ 本处理器只看**主手**（`getMainHandItem()`），副手持黑切不触发是设计如此。 */
            if (HIT_DIAG.add(String.valueOf(id))) {
                FTBossMod.LOGGER.info("[FTBoss][命中诊断] 玩家 {} 用主手 {} 命中 {}（黑切={}）",
                        p.getName().getString(), id, target.getName().getString(),
                        FTBossItems.BLACK_CLEAVER.getId().equals(id));
            }

            if (FTBossItems.BLACK_CLEAVER.getId().equals(id)) {
                resolving = true;
                try {
                    BloodRageFakeEffect.onHit(p);
                    ArmorShredEffect.onHit(target);
                    var eff = FTEffects.BLOOD_RAGE_FAKE.get();
                    var inst = (eff == null) ? null : p.getEffect(eff);
                    int stacks = (inst == null) ? 1 : inst.getAmplifier() + 1;
                    /* 无视 50% 护甲：把"被护甲吃掉那部分"的一半用魔法伤害补回来 */
                    float raw = (float) p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    double armor = target.getArmorValue();
                    double tough = target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
                    double reduction = Math.min(20.0, Math.max(armor / 5.0, armor - raw / (2.0 + tough / 4.0))) / 25.0;
                    float pierced = (float) (raw * reduction * 0.5);
                    float trueDmg = raw * BloodRageFakeEffect.trueDamageRatio(stacks - 1);
                    float extra = pierced + trueDmg;
                    if (extra > 0.01f) {
                        target.invulnerableTime = 0;
                        /* ★★ 2026-09-21 修「庄园 Boss 不受伤（柠檬猫血条纹丝不动）」：
                           这里以前用的是 {@code p.damageSources().magic()} —— 那个伤害源**不带任何实体**
                           （连 direct entity 都没有），而庄园 Boss 的受伤闸门是
                           `ManorCombatRules.allowsDamage(..., EasterBossEntity.isRealPlayerDamage(source))`，
                           `isRealPlayerDamage` 靠 `source.getEntity()/getDirectEntity()` 回溯玩家，拿不到就
                           **整下拒绝**。结果：这一刀"穿甲+真伤"的补伤对**庄园 Boss 本体**被静默丢弃
                           （打召唤物/小怪却照进 —— 它们不是 EasterBossEntity，没有那道闸门），
                           玩家眼里就是"黑切打庄园 Boss 不掉血"。
                           换成 {@code indirectMagic(p, p)}：**伤害类型仍是 MAGIC（穿甲语义不变）**，
                           只是把"这一下是玩家打的"记进伤害源，闸门就放行了。
                           ⚠ 不要换成 PLAYER_ATTACK —— 那会让本处理器（只认 PLAYER_ATTACK）递归。 */
                        target.hurt(p.damageSources().indirectMagic(p, p), extra);
                    }
                    FTBossMod.LOGGER.info("[FTBoss][黑切] {} 命中 {} -> 血怒 {} 层 / 破甲 / 穿甲+真伤 {} 点 (目标护甲 {})",
                            p.getName().getString(), target.getName().getString(), stacks,
                            String.format("%.2f", extra), (int) armor);
                } finally {
                    resolving = false;
                }
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][黑切] 命中特效异常(已忽略): {}", t.toString());
        }
    }
}
