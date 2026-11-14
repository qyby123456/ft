package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 沙耶(茶树古树)的两项召唤系战术:
 * <ul>
 *   <li>召唤/维护茶芽小妖(满编 3 只, 半血 5 只)</li>
 *   <li>「茶心汲取」: 吸收自己的茶芽小妖 → 回血 + 叠层强化(上限 4 层)</li>
 * </ul>
 * 计时器与层数状态都收在这里, 实体只保留动作状态。
 */
public final class TeaTactics {

    private static final int MINION_RANGE = 48;
    private static final int ABSORB_RANGE = 24;
    /** 2026-09-10 加强沙耶: 单次汲取 2 → 3 株, 共鸣上限 3 → 4 层 */
    private static final int ABSORB_MAX_PER_CAST = 3;
    private static final int ABSORB_STACK_CAP = 4;

    private final net.minecraft.world.entity.Mob e;
    private final java.util.function.Predicate<net.minecraft.world.entity.Entity> owned;

    private int minionCd = 150;
    private int absorbCd = 320;
    private int absorbStacks = 0;

    public TeaTactics(TeaTreeBossEntity entity) {
        this(entity, summon -> SummonSweep.belongsTo(entity, summon));
    }

    public TeaTactics(net.minecraft.world.entity.Mob entity, java.util.function.Predicate<net.minecraft.world.entity.Entity> owned) {
        this.e = entity;
        this.owned = owned;
    }

    /** 当前「茶心共鸣」层数 */
    public int stacks() {
        return absorbStacks;
    }

    private void grant(MobEffectInstance effect) {
        if (this.e instanceof net.farmingtales.ftboss.entity.EasterBossEntity lemon) lemon.lemonSacrifice().grant(effect);
        else this.e.addEffect(effect);
    }

    /** 召唤并维护茶芽小妖: 回满血、同步目标、防止消失 */
    public void tickMinionSummon() {
        if (e.getHealth() <= 0) return;
        if (--this.minionCd > 0) return;
        boolean lowHp = e.getHealth() < e.getMaxHealth() * 0.5f;
        this.minionCd = lowHp ? 90 : 150;   // 加强沙耶: 120/200 → 90/150

        int alive = 0;
        for (TeaMinionEntity t : e.level().getEntitiesOfClass(TeaMinionEntity.class,
                e.getBoundingBox().inflate(MINION_RANGE))) {
            if (!t.isAlive() || !this.owned.test(t)) continue;
            if (t.isGuardBud()) continue;   // 「守护茶芽」不参与护盾编队统计/满血补给
            alive++;
            t.setHealth(t.getMaxHealth());
            t.setTarget(e.getTarget());
            t.setPersistenceRequired();
        }

        // 加强沙耶: 满编 2/3 → 3/5, 每轮补 2 → 3 只
        int cap = lowHp ? 5 : 3;
        for (int i = alive; i < cap && i < alive + 3; i++) {
            TeaMinionEntity baby = FTEntities.TEA_MINION.get().create(e.level());
            if (baby == null) continue;
            double a = e.getRandom().nextDouble() * Math.PI * 2;
            double r = 2.5 + e.getRandom().nextDouble() * 4;
            baby.setPos(e.getX() + Math.cos(a) * r, e.getY(), e.getZ() + Math.sin(a) * r);
            baby.setCustomName(Component.translatable("entity.ftboss.tea_minion"));
            baby.setCustomNameVisible(true);
            baby.setPersistenceRequired();
            baby.setHealth(baby.getMaxHealth());
            if (e.getTarget() != null) baby.setTarget(e.getTarget());
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this.e, baby);
            e.level().addFreshEntity(baby);
        }
    }

    /** 每 12s 尝试: 24 格内最多吸收 3 株小妖(满血时不吸), 每株回 6.5% 最大生命 + 1 层共鸣 */
    public void absorb() {
        if (e instanceof TeaTreeBossEntity tea && tea.isTeaMinion() || e.isDeadOrDying()) return;
        if (--this.absorbCd > 0) return;
        this.absorbCd = 240;   // 加强沙耶: 320 → 240
        if (e.getHealth() >= e.getMaxHealth() - 0.5f) return;

        var minions = e.level().getEntitiesOfClass(TeaMinionEntity.class, e.getBoundingBox().inflate(ABSORB_RANGE));
        if (minions.isEmpty()) return;

        int n = 0;
        for (TeaMinionEntity m : minions) {
            if (n >= ABSORB_MAX_PER_CAST) break;
            if (!m.isAlive() || !this.owned.test(m)) continue;
            if (m.isGuardBud()) continue;   // 守护茶芽不可被汲取(必须先击败才能破盾)
            n++;
            Fx.particles(e.level(), m.getX(), m.getY() + 0.7, m.getZ(), 14, ParticleTypes.HAPPY_VILLAGER, 0.3, 0.05);
            Fx.particles(e.level(), m.getX(), m.getY() + 0.7, m.getZ(), 8, ParticleTypes.SPORE_BLOSSOM_AIR, 0.25, 0.02);
            Fx.sound(e.level(), m.getX(), m.getY(), m.getZ(), SoundEvents.HONEY_BLOCK_SLIDE, 1.0f, 0.7f);
            m.discard(); // 被汲取: 静默移除(不产生掉落)
        }

        float heal = e.getMaxHealth() * 0.075f * n;   // 2026-09-10 深夜加强: 5.5% → 6.5% → 7.5%/株
        e.heal(heal);
        if (n == 0) return;
        this.absorbStacks = Math.min(ABSORB_STACK_CAP, this.absorbStacks + n);
        int st = this.absorbStacks;
        grant(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, Math.min(1, st - 1), false, false, true));
        grant(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, st >= 3 ? 1 : 0, false, false, true));
        grant(new MobEffectInstance(MobEffects.ABSORPTION, 400, Math.min(1, st / 2), false, false, true));

        if (e.level() instanceof ServerLevel sl) {
            Fx.particles(sl, e.getX(), e.getY() + 1.2, e.getZ(), 30, ParticleTypes.HAPPY_VILLAGER, 0.6, 0.08);
            Fx.particles(sl, e.getX(), e.getY() + 1.4, e.getZ(), 20, ParticleTypes.SPORE_BLOSSOM_AIR, 0.5, 0.02);
            Fx.sound(sl, e.getX(), e.getY(), e.getZ(), SoundEvents.BEACON_POWER_SELECT, 1.0f, 0.8f);
            sl.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.HOSTILE, 0.5f, 1.4f);
            for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, e.getBoundingBox().inflate(32))) {
                p.displayClientMessage(Component.translatable("msg.ftboss.saya_absorb", n, st), false);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] 沙耶 吸取 {} 株茶芽小妖: 回血 {} 强化层数 {}", n, (int) heal, st);
    }
}
