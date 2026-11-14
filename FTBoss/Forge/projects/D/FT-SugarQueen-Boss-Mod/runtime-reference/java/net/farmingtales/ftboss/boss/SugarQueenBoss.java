/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.entity.IMagicEntity
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.monster.Slime
 *  net.minecraft.world.entity.monster.Zombie
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.farmingtales.ftboss.util.BossCollision;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SugarQueenBoss {
    private static final double MAX_HP = 360.0;
    private static final int SUMMON_CD = 170;
    private static final int SUMMON_CD_P2 = 120;
    private static final int[][] WAVES = new int[][]{{2, 0}, {2, 0}, {3, 1}, {3, 1}};
    private static final int MINION_CAP = 10;
    private static final Item[] SWEETS = new Item[]{Items.f_42572_, Items.f_42687_, Items.f_42787_, Items.f_42780_, Items.f_42410_, Items.f_42575_, Items.f_42502_};
    private static final int PINK = 15775487;
    private static boolean active = false;
    private static ServerLevel level;
    private static UUID bossUUID;
    private static UUID ownerUUID;
    private static boolean phase2;
    private static boolean finalMode;
    private static boolean totemUsed;
    private static int tick;
    private static int summonCd;
    private static int castCd;
    private static double flyA;
    private static int waveIdx;
    private static int dashTicks;
    private static int dashCd;
    private static final List<UUID> crystals;
    private static final String[] MINION_NAMES;
    private static final ChatFormatting[] MINION_COLORS;

    private SugarQueenBoss() {
    }

    private static boolean spawnClone(ServerPlayer sp) {
        try {
            ServerLevel lvl = sp.m_284548_();
            Entity raw = ((EntityType)FTEntities.SUGAR_QUEEN.get()).m_20615_((Level)lvl);
            if (!(raw instanceof SugarQueenEntity)) {
                return false;
            }
            SugarQueenEntity queen = (SugarQueenEntity)raw;
            Vec3 look = sp.m_20154_();
            Vec3 pos = sp.m_20182_().m_82520_(look.f_82479_ * 3.0, 0.0, look.f_82481_ * 3.0);
            queen.setCloneQueen(true);
            queen.m_6593_((Component)Component.m_237115_((String)"boss.ftboss.sugar_queen").m_7220_((Component)Component.m_237113_((String)"\u00a77\u00b7\u590d\u5236\u4f53")).m_130940_(ChatFormatting.LIGHT_PURPLE));
            queen.m_20340_(true);
            queen.m_21153_(360.0f);
            queen.m_20242_(true);
            if (!BossCollision.place(lvl, (Entity)queen, new Vec3(pos.f_82479_, sp.m_20186_() + 2.0, pos.f_82481_), BossCollision.SpawnMode.AIR, sp.m_146908_() + 180.0f)) {
                BossCollision.tellNoRoom((Player)sp, "\u7cd6\u971c\u5973\u7687\u590d\u5236\u4f53");
                return false;
            }
            queen.m_21530_();
            if (!lvl.m_7967_((Entity)queen)) {
                BossCollision.tellNoRoom((Player)sp, "\u7cd6\u971c\u5973\u7687\u590d\u5236\u4f53");
                return false;
            }
            lvl.m_6263_(null, pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11868_, SoundSource.HOSTILE, 1.0f, 1.4f);
            SugarQueenBoss.broadcast("\u00a7d\u7cd6\u971c\u5973\u7687 \u00a77\u53c8\u53ec\u5524\u4e86\u4e00\u4e2a\u590d\u5236\u4f53\uff01");
            FTBossMod.LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687\u590d\u5236\u4f53\u52a0\u5165\u6218\u6597 @ {} {} {}", new Object[]{pos.f_82479_, pos.f_82480_, pos.f_82481_});
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u5973\u7687\u590d\u5236\u4f53\u53ec\u5524\u5931\u8d25: {}", (Object)t.toString());
            return false;
        }
    }

    public static boolean tryStart(ServerPlayer sp) {
        if (!SummonGate.allow((Level)sp.m_284548_(), (Player)sp, "\u7cd6\u971c\u5973\u7687")) {
            return false;
        }
        if (active) {
            return SugarQueenBoss.spawnClone(sp);
        }
        try {
            ServerLevel lvl = sp.m_284548_();
            Entity raw = ((EntityType)FTEntities.SUGAR_QUEEN.get()).m_20615_((Level)lvl);
            if (!(raw instanceof SugarQueenEntity)) {
                return false;
            }
            SugarQueenEntity queen = (SugarQueenEntity)raw;
            Vec3 look = sp.m_20154_();
            Vec3 pos = sp.m_20182_().m_82520_(look.f_82479_ * 3.0, 0.0, look.f_82481_ * 3.0);
            queen.m_20225_(true);
            queen.m_6593_((Component)Component.m_237115_((String)"boss.ftboss.sugar_queen").m_130944_(new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD}));
            queen.m_20340_(true);
            queen.m_21153_(360.0f);
            queen.m_20242_(true);
            if (!BossCollision.place(lvl, (Entity)queen, new Vec3(pos.f_82479_, sp.m_20186_() + 2.0, pos.f_82481_), BossCollision.SpawnMode.AIR, sp.m_146908_() + 180.0f) || !lvl.m_7967_((Entity)queen)) {
                BossCollision.tellNoRoom((Player)sp, "\u7cd6\u971c\u5973\u7687");
                return false;
            }
            lvl.m_6263_(null, pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11868_, SoundSource.HOSTILE, 1.0f, 1.1f);
            level = lvl;
            bossUUID = queen.m_20148_();
            ownerUUID = sp.m_20148_();
            active = true;
            phase2 = false;
            finalMode = false;
            totemUsed = false;
            tick = 0;
            summonCd = 170;
            waveIdx = 0;
            dashTicks = 0;
            dashCd = 120;
            crystals.clear();
            FTBossMod.LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687(\u672c\u4f53\u65bd\u6cd5) \u53ec\u5524 @ {} {} {}", new Object[]{pos.f_82479_, pos.f_82480_, pos.f_82481_});
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] \u53ec\u5524\u5f02\u5e38: ", t);
            return false;
        }
    }

    public static void tick() {
        if (!active || level == null) {
            return;
        }
        try {
            LivingEntity target;
            Mob boss;
            ++tick;
            Entity e = level.m_8791_(bossUUID);
            if (!(e instanceof Mob) || (boss = (Mob)e).m_213877_()) {
                SugarQueenBoss.cleanup();
                return;
            }
            if (boss.m_21223_() <= 0.0f) {
                if (totemUsed && finalMode) {
                    boss.m_21153_(108.0f);
                } else {
                    return;
                }
            }
            if ((target = BossTargets.combatTarget(boss, 30.0)) == null) {
                return;
            }
            double dist = boss.m_20270_((Entity)target);
            if (!phase2 && (double)(boss.m_21223_() / boss.m_21233_()) < 0.5) {
                phase2 = true;
                summonCd = Math.min(summonCd, 120);
            }
            boss.m_6710_(target);
            SugarQueenBoss.facePlayer((Entity)boss, target);
            if (dashTicks > 0) {
                --dashTicks;
                double tdx = target.m_20185_() - boss.m_20185_();
                double tdy = target.m_20188_() - 0.6 - boss.m_20186_();
                double tdz = target.m_20189_() - boss.m_20189_();
                double tdl = Math.max(0.001, Math.sqrt(tdx * tdx + tdy * tdy + tdz * tdz));
                double speed = 0.95;
                BossCollision.move((Entity)boss, new Vec3(tdx / tdl * speed, tdy / tdl * speed, tdz / tdl * speed));
                Fx.particles((Level)level, boss.m_20185_(), boss.m_20186_() + 1.2, boss.m_20189_(), 8, (ParticleOptions)ParticleTypes.f_123797_, 0.3, 0.03);
                if (tdl < 2.2 || dashTicks == 0) {
                    SugarQueenBoss.dashSlam((Entity)boss, level, target);
                    dashTicks = 0;
                    dashCd = finalMode || phase2 ? 90 : 140;
                }
            } else {
                double dz;
                double dy;
                double distToTarget = boss.m_20270_((Entity)target);
                if (--dashCd <= 0 && distToTarget > 6.0 && distToTarget < 26.0) {
                    dashTicks = 16;
                    dashCd = 140;
                    SugarQueenBoss.broadcast("\u00a7d\u7cd6\u971c\u5973\u7687 \u00a77\u84c4\u529b\u51b2\u523a\uff01");
                    level.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_12363_, SoundSource.HOSTILE, 1.0f, 1.4f);
                }
                double radius = 6.5;
                double hx = target.m_20185_() + Math.cos(flyA += 0.02) * radius;
                double hz = target.m_20189_() + Math.sin(flyA) * radius;
                double hy = target.m_20186_() + 2.0 + Math.sin((double)tick * 0.05) * 0.35;
                double dx = hx - boss.m_20185_();
                double dl = Math.sqrt(dx * dx + (dy = hy - boss.m_20186_()) * dy + (dz = hz - boss.m_20189_()) * dz);
                if (dl > 0.8) {
                    double step = Math.min(0.28, dl);
                    BossCollision.move((Entity)boss, new Vec3(dx / dl * step, dy / dl * step, dz / dl * step));
                }
            }
            if (--summonCd <= 0) {
                summonCd = finalMode || phase2 ? 120 : 170;
                int[] w = WAVES[waveIdx % WAVES.length];
                ++waveIdx;
                int alive = SugarQueenBoss.aliveMinions();
                int count = Math.min(w[0], Math.max(0, 10 - alive));
                if (count > 0) {
                    SugarQueenBoss.summonWave(count, Math.min(w[1], count), target);
                    SugarQueenBoss.broadcast("\u00a7d\u7cd6\u971c\u5973\u7687 \u00a77\u53ec\u5524\u4e86\u4e00\u961f\u59dc\u997c\u58eb\u5175\u2026");
                } else if (tick % 100 == 0) {
                    FTBossMod.LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687 \u53ec\u5524\u5df2\u8fbe\u4e0a\u9650({}/{}), \u672c\u6ce2\u8df3\u8fc7", (Object)alive, (Object)10);
                }
            }
            if (boss instanceof IMagicEntity) {
                IMagicEntity m = (IMagicEntity)boss;
                if (--castCd <= 0) {
                    int n = finalMode ? 45 : (castCd = phase2 ? 62 : 85);
                    if (!m.isCasting()) {
                        boss.m_6710_(target);
                        SugarQueenBoss.castAt(m, target);
                    }
                }
            }
            if (tick % 20 == 0) {
                for (UUID c : crystals) {
                    Mob mini;
                    Entity ce = level.m_8791_(c);
                    if (!(ce instanceof Mob) || !(mini = (Mob)ce).m_6084_()) continue;
                    mini.m_6710_(target);
                    if (!(mini.m_20280_((Entity)target) < 4.840000000000001)) continue;
                    try {
                        mini.m_7327_((Entity)target);
                    }
                    catch (Throwable t) {
                        FTBossMod.LOGGER.warn("[FTBoss] \u5c0f\u59dc\u997c\u4eba\u4ee3\u6253\u5931\u8d25(\u5df2\u5ffd\u7565, \u4e0d\u5d29\u670d): {}", (Object)t.toString());
                    }
                }
            }
            if ((phase2 || finalMode) && tick % 20 == 0) {
                if (boss.m_21223_() > 0.0f && boss.m_21223_() < boss.m_21233_()) {
                    boss.m_5634_(0.5f);
                }
                for (ServerPlayer p : level.m_6907_()) {
                    if (!p.m_6084_() || !(p.m_20280_((Entity)boss) < 64.0)) continue;
                    p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 140, 1));
                    p.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 140, 0));
                    p.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 140, 0));
                }
            }
            if (tick % 100 == 0) {
                IMagicEntity m;
                FTBossMod.LOGGER.info("[FTBoss] \u8840={}/{} \u8ddd\u79bb={} \u65bd\u6cd5\u4e2d={} \u6ce2\u6b21CD={} \u53ec\u5524\u7269={}/{}", new Object[]{(int)boss.m_21223_(), 360, String.format("%.1f", dist), boss instanceof IMagicEntity && (m = (IMagicEntity)boss).isCasting() ? "\u662f" : "\u5426", summonCd, SugarQueenBoss.aliveMinions(), 10});
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] \u6218\u6597\u5f02\u5e38: ", t);
        }
    }

    private static void castAt(IMagicEntity m, LivingEntity target) {
        try {
            LivingEntity caster;
            int n;
            if (m instanceof LivingEntity) {
                LivingEntity le = (LivingEntity)m;
                n = BossRage.tierOf(le);
            } else {
                n = 0;
            }
            int rage = n;
            List<AbstractSpell> pool = WizardSummon.spellPool();
            if (pool.isEmpty()) {
                return;
            }
            if (rage >= 1 && SugarQueenBoss.level.f_46441_.m_188501_() < 0.7f) {
                pool = WizardSummon.spellPoolRare(6, 3);
            }
            AbstractSpell s = pool.get(SugarQueenBoss.level.f_46441_.m_188503_(pool.size()));
            if (m instanceof LivingEntity && !BossSpellPacing.tryLegacyCast(caster = (LivingEntity)m, s)) {
                return;
            }
            int lvl = Math.max(s.getMinLevel(), Math.min(4 + rage, s.getMaxLevel()));
            try {
                Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                for (Constructor<?> ctor : tdc.getConstructors()) {
                    if (ctor.getParameterCount() != 1 || !ctor.getParameterTypes()[0].isInstance(target)) continue;
                    Object inst = ctor.newInstance(target);
                    Method set = MagicData.class.getMethod("setAdditionalCastData", ICastData.class);
                    MagicData md = MagicData.getPlayerMagicData((LivingEntity)((LivingEntity)m));
                    set.invoke(md, inst);
                    break;
                }
            }
            catch (Throwable tdc) {
                // empty catch block
            }
            if (m instanceof LivingEntity) {
                LivingEntity le = (LivingEntity)m;
                le.m_5618_(le.m_146908_());
                le.m_5616_(le.m_146908_());
            }
            m.initiateCastSpell(s, lvl);
            FTBossMod.LOGGER.info("[FTBoss] \u5973\u738b\u65bd\u6cd5: {} lv{} \u76ee\u6807={}", new Object[]{s.getSpellId(), lvl, target.m_7755_().getString()});
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u65bd\u6cd5\u5f02\u5e38: {}", (Object)t.toString());
        }
    }

    private static void dashSlam(Entity boss, ServerLevel level, LivingEntity target) {
        LivingEntity le;
        LivingEntity src;
        double range = 4.5;
        float dmg = 8.0f;
        LivingEntity livingEntity = src = boss instanceof LivingEntity ? (le = (LivingEntity)boss) : null;
        if (target != null && !(target instanceof Player) && target.m_6084_() && (double)target.m_20270_(boss) <= 4.5) {
            target.m_6469_(level.m_269111_().m_269333_(src), 8.0f);
        }
        if (target != null && target.m_6084_() && (double)target.m_20270_(boss) > 4.5) {
            target.m_6469_(level.m_269111_().m_269333_(src), 4.0f);
        }
        for (ServerPlayer p : level.m_45976_(ServerPlayer.class, boss.m_20191_().m_82400_(4.5))) {
            if (!p.m_6084_()) continue;
            p.m_6469_(level.m_269111_().m_269333_(src), 8.0f);
            Vec3 push = new Vec3(p.m_20185_() - boss.m_20185_(), 0.6, p.m_20189_() - boss.m_20189_()).m_82541_().m_82490_(1.8);
            BossKnockback.push((Entity)p, push.f_82479_, push.f_82480_, push.f_82481_);
            p.f_19864_ = true;
            p.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 80, 1, false, false, true));
        }
        level.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 1.6f, 1.25f);
        level.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_11668_, SoundSource.HOSTILE, 1.2f, 0.8f);
        Fx.particles((Level)level, boss.m_20185_(), boss.m_20186_() + 0.8, boss.m_20189_(), 40, (ParticleOptions)ParticleTypes.f_123797_, 1.2, 0.15);
        Fx.particles((Level)level, boss.m_20185_(), boss.m_20186_() + 0.4, boss.m_20189_(), 30, (ParticleOptions)ParticleTypes.f_123796_, 1.0, 0.05);
        SugarQueenBoss.broadcast("\u00a7d\u7cd6\u971c\u5973\u7687 \u00a77\u51b2\u523a\u649e\u51fb\uff01");
    }

    private static void facePlayer(Entity boss, LivingEntity target) {
        double dz;
        double dx = target.m_20185_() - boss.m_20185_();
        if (dx * dx + (dz = target.m_20189_() - boss.m_20189_()) * dz < 0.001) {
            return;
        }
        boss.m_146922_((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        double dy = target.m_20186_() + 1.2 - (boss.m_20186_() + 1.0);
        double h = Math.sqrt(dx * dx + dz * dz);
        boss.m_146926_((float)Math.toDegrees(Math.atan2(dy, Math.max(h, 0.01))));
    }

    private static int aliveMinions() {
        if (level == null) {
            return 0;
        }
        int alive = 0;
        for (UUID id : crystals) {
            LivingEntity le;
            Entity e = level.m_8791_(id);
            if (!(e instanceof LivingEntity) || !(le = (LivingEntity)e).m_6084_() || le.m_213877_()) continue;
            ++alive;
        }
        return alive;
    }

    private static void summonWave(int count, int elites, LivingEntity target) {
        int spawned = 0;
        int tries = 0;
        while (spawned < count && tries < count * 5) {
            ++tries;
            try {
                boolean elite;
                double z;
                double a = Math.random() * Math.PI * 2.0;
                double r = 3.0 + Math.random() * 5.0;
                double x = SugarQueenBoss.bossX() + Math.cos(a) * r;
                double y = SugarQueenBoss.findSafeY(x, z = SugarQueenBoss.bossZ() + Math.sin(a) * r);
                if (y == Double.MIN_VALUE) continue;
                boolean bl = elite = spawned < elites;
                int kind = elite ? (waveIdx + spawned) % 2 : (spawned % 4 == 3 ? 1 + (waveIdx + spawned) % 3 : 0);
                Mob mini = SugarQueenBoss.createMinion(kind);
                if (mini == null) continue;
                mini.m_6021_(x, y, z);
                level.m_7967_((Entity)mini);
                mini.m_6593_((Component)Component.m_237113_((String)(MINION_NAMES[kind] + (elite ? "\u00b7\u7cbe\u82f1" : ""))).m_130940_(elite ? ChatFormatting.GOLD : MINION_COLORS[kind]));
                mini.m_20340_(true);
                mini.m_146915_(true);
                MinionBlessing.mark((Entity)mini, bossUUID);
                if (elite) {
                    mini.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 999999, 2, false, false));
                    mini.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 999999, 1, false, false));
                    mini.m_7292_(new MobEffectInstance(MobEffects.f_19616_, 999999, 1, false, false));
                    if (mini instanceof Zombie) {
                        Zombie zb = (Zombie)mini;
                        SugarQueenBoss.applyEliteGear(zb);
                        zb.m_21153_(zb.m_21233_());
                    }
                } else {
                    mini.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 999999, kind == 2 ? 2 : 1, false, false));
                    mini.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 999999, kind == 3 ? 1 : (kind == 0 ? 1 : 0), false, false));
                    if (kind == 0) {
                        mini.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 999999, 0, false, false));
                    }
                    if (mini instanceof Zombie) {
                        mini.m_8061_(EquipmentSlot.MAINHAND, SugarQueenBoss.sweetItem((ItemLike)SWEETS[SugarQueenBoss.level.f_46441_.m_188503_(SWEETS.length)], 2 + SugarQueenBoss.level.f_46441_.m_188503_(2), null));
                        mini.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
                    }
                }
                crystals.add(mini.m_20148_());
                if (target != null) {
                    mini.m_6710_(target);
                }
                ++spawned;
            }
            catch (Throwable t) {
                FTBossMod.LOGGER.error("[FTBoss] \u6ce2\u6b21\u53ec\u5524\u5f02\u5e38: ", t);
            }
        }
    }

    private static Mob createMinion(int kind) {
        try {
            Mob m;
            switch (kind) {
                case 1: {
                    m = (Mob)EntityType.f_20458_.m_20615_((Level)level);
                    break;
                }
                case 2: {
                    m = (Mob)EntityType.f_20479_.m_20615_((Level)level);
                    break;
                }
                case 3: {
                    m = (Mob)EntityType.f_20526_.m_20615_((Level)level);
                    if (!(m instanceof Slime)) break;
                    Slime sl = (Slime)m;
                    sl.m_7839_(3, false);
                    break;
                }
                default: {
                    m = (Mob)((EntityType)FTEntities.GINGERBREAD_MINION.get()).m_20615_((Level)level);
                }
            }
            return m;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static void applyEliteGear(Zombie zombie) {
        zombie.m_8061_(EquipmentSlot.HEAD, SugarQueenBoss.coloredLeather(Items.f_42407_));
        zombie.m_8061_(EquipmentSlot.CHEST, SugarQueenBoss.coloredLeather(Items.f_42408_));
        zombie.m_8061_(EquipmentSlot.LEGS, SugarQueenBoss.coloredLeather(Items.f_42462_));
        zombie.m_8061_(EquipmentSlot.FEET, SugarQueenBoss.coloredLeather(Items.f_42463_));
        zombie.m_8061_(EquipmentSlot.MAINHAND, SugarQueenBoss.sweetItem((ItemLike)SWEETS[SugarQueenBoss.level.f_46441_.m_188503_(SWEETS.length)], 4 + SugarQueenBoss.level.f_46441_.m_188503_(2), null));
        for (EquipmentSlot s : EquipmentSlot.values()) {
            zombie.m_21409_(s, 0.0f);
        }
    }

    private static ItemStack coloredLeather(Item item) {
        ItemStack s = new ItemStack((ItemLike)item);
        s.m_41698_("display").m_128405_("color", 15775487);
        return s;
    }

    private static ItemStack sweetItem(ItemLike it, int glintLevel, String name) {
        ItemStack s = new ItemStack(it);
        if (glintLevel > 0) {
            s.m_41663_(Enchantments.f_44977_, glintLevel);
        }
        if (name != null && !name.isEmpty()) {
            s.m_41714_((Component)Component.m_237113_((String)name).m_130940_(ChatFormatting.LIGHT_PURPLE));
        }
        return s;
    }

    private static double findSafeY(double x, double z) {
        int y;
        int dy;
        int base = (int)Math.floor(SugarQueenBoss.bossY());
        for (dy = 0; dy >= -14; --dy) {
            y = base + dy;
            if (!SugarQueenBoss.isSafe(x, y, z)) continue;
            return (double)y + 0.6;
        }
        for (dy = 1; dy <= 14; ++dy) {
            y = base + dy;
            if (!SugarQueenBoss.isSafe(x, y, z)) continue;
            return (double)y + 0.6;
        }
        return Double.MIN_VALUE;
    }

    private static boolean isSafe(double x, int y, double z) {
        BlockPos here = BlockPos.m_274561_((double)x, (double)y, (double)z);
        return level.m_46859_(here) && level.m_46859_(here.m_7494_()) && !level.m_46859_(here.m_7495_());
    }

    public static boolean tryPopTotem(Mob boss) {
        if (!active || boss == null || !boss.m_20148_().equals(bossUUID)) {
            return false;
        }
        if (totemUsed) {
            return false;
        }
        try {
            totemUsed = true;
            finalMode = true;
            summonCd = Math.min(summonCd, 120);
            boss.m_21153_(108.0f);
            boss.m_21219_();
            if (boss instanceof IMagicEntity) {
                IMagicEntity m = (IMagicEntity)boss;
                try {
                    m.cancelCast();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            level.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_12513_, SoundSource.PLAYERS, 1.0f, 1.0f);
            FTBossMod.LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687 \u89e6\u53d1\u7ec8\u5c40(\u56fe\u817e)");
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] \u7ec8\u5c40\u89e6\u53d1\u5f02\u5e38: ", t);
            return false;
        }
    }

    private static double bossX() {
        Entity e = level.m_8791_(bossUUID);
        return e != null ? e.m_20185_() : 0.0;
    }

    private static double bossY() {
        Entity e = level.m_8791_(bossUUID);
        return e != null ? e.m_20186_() : 100.0;
    }

    private static double bossZ() {
        Entity e = level.m_8791_(bossUUID);
        return e != null ? e.m_20189_() : 0.0;
    }

    private static void broadcast(String msg) {
        FTBossMod.LOGGER.info("[FTBoss] {}", (Object)msg);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void onBossDeath(Mob boss) {
        if (LemonSummonRewards.suppressed((Entity)boss)) {
            return;
        }
        if (!active || boss == null || !boss.m_20148_().equals(bossUUID)) {
            return;
        }
        try {
            double x = boss.m_20185_();
            double y = boss.m_20186_();
            double z = boss.m_20189_();
            int sugar = 24 + SugarQueenBoss.level.f_46441_.m_188503_(25);
            NonManorBossLootRouter.spawn((Level)level, (Entity)new ItemEntity((Level)level, x, y, z, new ItemStack((ItemLike)Items.f_42501_, sugar)), (LivingEntity)boss);
            NonManorBossLootRouter.spawn((Level)level, (Entity)new ItemEntity((Level)level, x, y + 0.3, z, new ItemStack((ItemLike)FTBlocks.STATUE_SUGAR_QUEEN.get(), 1)), (LivingEntity)boss);
            if (SugarQueenBoss.level.f_46441_.m_188501_() < 0.3f) {
                ItemStack book = new ItemStack((ItemLike)Items.f_42516_);
                book.m_41714_((Component)Component.m_237113_((String)"\u00a76\u79d8\u5236\u7126\u7cd6\u914d\u65b9"));
                NonManorBossLootRouter.spawn((Level)level, (Entity)new ItemEntity((Level)level, x, y + 0.5, z, book), (LivingEntity)boss);
            }
            NonManorBossLootRouter.spawn((Level)level, (Entity)new ExperienceOrb((Level)level, x, y, z, 200), (LivingEntity)boss);
            level.m_6263_(null, x, y, z, SoundEvents.f_12513_, SoundSource.PLAYERS, 1.0f, 1.0f);
            SugarQueenBoss.broadcast("\u00a7d\u7cd6\u971c\u5973\u7687 \u00a77\u88ab\u6253\u8d25\u4e86\u2014\u2014\u7559\u4e0b\u4e00\u5730\u7cd6\u971c\u4e0e\u77f3\u50cf\u2026");
            FTBossMod.LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687 \u88ab\u51fb\u8d25");
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] \u6b7b\u4ea1\u6389\u843d\u5f02\u5e38: ", t);
        }
        finally {
            SugarQueenBoss.cleanup("\u88ab\u51fb\u8d25");
        }
    }

    public static void cleanup() {
        SugarQueenBoss.cleanup("\u672a\u6ce8\u660e(\u5916\u90e8\u8c03\u7528)");
    }

    public static void cleanup(String reason) {
        if (active) {
            FTBossMod.LOGGER.info("[FTBoss] \u7cd6\u971c\u5973\u7687 \u6e05\u573a: {}", (Object)reason);
        }
        active = false;
        if (level != null) {
            for (UUID c : crystals) {
                Entity e = level.m_8791_(c);
                if (e == null) continue;
                e.m_146870_();
            }
            crystals.clear();
            if (bossUUID != null) {
                Entity b = level.m_8791_(bossUUID);
                if (b instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity)b;
                    SummonSweep.clearMarked(le, 80.0);
                }
                if (b != null) {
                    b.m_146870_();
                }
            }
        }
        bossUUID = null;
        ownerUUID = null;
        level = null;
        tick = 0;
        finalMode = false;
        totemUsed = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isBoss(Entity e) {
        return active && e != null && e.m_20148_().equals(bossUUID);
    }

    public static UUID ownerIdOf() {
        return ownerUUID;
    }

    static {
        phase2 = false;
        finalMode = false;
        totemUsed = false;
        tick = 0;
        summonCd = 170;
        castCd = 100;
        flyA = 0.0;
        waveIdx = 0;
        dashTicks = 0;
        dashCd = 120;
        crystals = new ArrayList<UUID>();
        MINION_NAMES = new String[]{"\u5c0f\u59dc\u997c\u4eba", "\u7126\u7cd6\u7ed3\u6676", "\u7cd6\u971c\u8718\u86db", "\u8f6f\u7cd6\u53f2\u83b1\u59c6"};
        MINION_COLORS = new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GOLD, ChatFormatting.WHITE, ChatFormatting.LIGHT_PURPLE};
    }
}

