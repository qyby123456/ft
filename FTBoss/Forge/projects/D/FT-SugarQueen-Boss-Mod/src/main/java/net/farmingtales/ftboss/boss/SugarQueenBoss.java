package net.farmingtales.ftboss.boss;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 糖霜女皇(本体施法版)
 * 女王继承铁魔法施法基类, 自身挂 WizardAttackGoal(同东方起源鉴做法): 她会自己走位/施法!
 * 女王行为由铁魔法AI驱动(不再空中绕圈), 另保留: 波次召唤糖结晶 + 图腾二阶段 + 血条/掉落
 */
public final class SugarQueenBoss {

    private static final double MAX_HP = 720;

    // 2026-09-10: 按用户要求"加强攻击性"——召唤更频繁、施法冷却更短、法术等级更高(2->4)
    // 2026-09-10 晚: 按用户要求"姜饼士兵召唤增加, 其他召唤物减少"——每波数量 2/2/3/3 → 4/4/5/5, 间隔 230/160 → 200/140
    // 2026-09-10 深夜: 全面加强 —— 召唤间隔 200/140 → 170/120
    private static final int SUMMON_CD = 170;
    private static final int SUMMON_CD_P2 = 120;

    /** 每波 {总数, 精英数}; 具体种类见 {@link #pickKind}(以「小姜饼人」为主) */
    private static final int[][] WAVES = { {2, 0}, {2, 0}, {3, 1}, {3, 1} };

    /**
     * ★ 2026-09-24 按用户要求「糖霜女王召唤数量减少，提供召唤上限」。
     *
     * <p>两条一起上：
     * <ol>
     *   <li>{@link #WAVES} 每波数量 4/4/5/5 → <b>2/2/3/3</b>（减半，精英配置不变）；</li>
     *   <li>同时在场的糖系召唤物**硬上限** {@link #MINION_CAP}：到顶之后这一波直接不刷
     *       （不再"攒着"补刷），场上数量回落到上限以下才恢复。</li>
     * </ol>
     *
     * <p>起因是 2026-09-24 晚上的实战（日志实证）：21:00~21:49 一场打下来刷了 <b>230 波</b>、
     * 小姜饼人累计死亡 <b>599 只</b>，玩家被小姜饼人连杀 5 次触发「连死怜悯」、
     * 清场时一次性移除 <b>117 个实体</b>；另外还有 200 次「遗物跳过」是因为召唤物抢了击杀归属。
     * 每波数量和并发上限各自只有一个旋钮，要再调只改 {@link #WAVES} 与 {@link #MINION_CAP}。
     */
    private static final int MINION_CAP = 10;

    private static final Item[] SWEETS = {
            Items.COOKIE, Items.PUMPKIN_PIE, Items.HONEY_BOTTLE, Items.SWEET_BERRIES,
            Items.APPLE, Items.MELON_SLICE, Items.CAKE
    };
    private static final int PINK = 0xF0B6FF;

    private static boolean active = false;
    private static ServerLevel level;
    private static UUID bossUUID;
    private static UUID ownerUUID;
    private static boolean phase2 = false;
    private static boolean finalMode = false;
    private static boolean totemUsed = false;
    private static int tick = 0;
    private static int summonCd = SUMMON_CD;
    private static int castCd = 100; // 手动施法冷却
    private static double flyA = 0.0; // 环绕相位角
    private static int waveIdx = 0;
    /** 「冲刺撞击」状态(2026-09-10 深夜新增): 剩余冲刺 tick 与冷却 */
    private static int dashTicks = 0;
    private static int dashCd = 120;
    private static final List<UUID> crystals = new ArrayList<>();

    private SugarQueenBoss() {}

    /**
     * 额外召唤的女皇「克隆体」(2026-09-11 按用户要求"允许玩家召唤同种多个boss")。
     * 与本体共用外观/血量/免疫, 但战斗由实体自身的简化 AI 驱动, 不占用本体的静态大脑。
     */
    private static boolean spawnClone(ServerPlayer sp) {
        try {
            ServerLevel lvl = sp.serverLevel();
            Entity raw = FTEntities.SUGAR_QUEEN.get().create(lvl);
            if (!(raw instanceof SugarQueenEntity queen)) return false;
            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.position().add(look.x * 3, 0, look.z * 3);
            queen.setCloneQueen(true);
            queen.setCustomName(Component.translatable("boss.ftboss.sugar_queen")
                    .append(Component.literal("§7·复制体"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            queen.setCustomNameVisible(true);
            queen.setHealth((float) MAX_HP);
            queen.setNoGravity(true);
            if (!net.farmingtales.ftboss.util.BossCollision.place(lvl, queen,
                    new Vec3(pos.x, sp.getY() + 2.0, pos.z),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.AIR, sp.getYRot() + 180.0F)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(sp, "糖霜女皇复制体");
                return false;
            }
            queen.setPersistenceRequired();
            if (!lvl.addFreshEntity(queen)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(sp, "糖霜女皇复制体");
                return false;
            }
            lvl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1, 1.4f);
            broadcast("§d糖霜女皇 §7又召唤了一个复制体！");
            FTBossMod.LOGGER.info("[FTBoss] 糖霜女皇复制体加入战斗 @ {} {} {}", pos.x, pos.y, pos.z);
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 女皇复制体召唤失败: {}", t.toString());
            return false;
        }
    }

    public static boolean tryStart(ServerPlayer sp) {
        /* 2026-09-13 v93 按用户要求「全部 boss 都必须在主世界召唤」——
           闸放在这里(而不是召唤物里), 克隆体那条路也一起盖住。 */
        if (!SummonGate.allow(sp.serverLevel(), sp, "糖霜女皇")) return false;
        // 2026-09-11 按用户要求"允许玩家召唤同种多个boss": 已有一只在打时, 额外召唤变成**克隆体**
        // (克隆体走 SugarQueenEntity 里的简化 AI: 悬停接近 + 随机施法 + 贴身挥杖, 有自己的血条)
        if (active) return spawnClone(sp);
        try {
            ServerLevel lvl = sp.serverLevel();
            Entity raw = FTEntities.SUGAR_QUEEN.get().create(lvl);
            if (!(raw instanceof SugarQueenEntity queen)) return false;
            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.position().add(look.x * 3, 0, look.z * 3);
            queen.setSilent(true);
            queen.setCustomName(Component.translatable("boss.ftboss.sugar_queen").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            queen.setCustomNameVisible(true);
            queen.setHealth((float) MAX_HP);

            // 女王=飞行法师: 物理升空(noGravity), 飞行轨迹由tick逐帧控制(不再走地面寻路AI)
            queen.setNoGravity(true);
            if (!net.farmingtales.ftboss.util.BossCollision.place(lvl, queen,
                    new Vec3(pos.x, sp.getY() + 2.0, pos.z),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.AIR, sp.getYRot() + 180.0F)
                    || !lvl.addFreshEntity(queen)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(sp, "糖霜女皇");
                return false;
            }
            lvl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1, 1.1f);

            level = lvl;
            bossUUID = queen.getUUID();
            ownerUUID = sp.getUUID();
            active = true;
            phase2 = false;
            finalMode = false;
            totemUsed = false;
            tick = 0;
            summonCd = SUMMON_CD;
            waveIdx = 0;
            dashTicks = 0;
            dashCd = 120;
            crystals.clear();
            FTBossMod.LOGGER.info("[FTBoss] 糖霜女皇(本体施法) 召唤 @ {} {} {}", pos.x, pos.y, pos.z);
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] 召唤异常: ", t);
            return false;
        }
    }

    public static void tick() {
        if (!active || level == null) return;
        try {
            tick++;
            Entity e = level.getEntity(bossUUID);
            if (!(e instanceof Mob boss) || boss.isRemoved()) { cleanup(); return; }
            if (boss.getHealth() <= 0) {
                if (totemUsed && finalMode) {
                    boss.setHealth((float) (MAX_HP * 0.3));
                } else {
                    return;
                }
            }

            LivingEntity target = net.farmingtales.ftboss.boss.ai.BossTargets.combatTarget(boss, 30);
            if (target == null) return;
            double dist = boss.distanceTo(target);

            if (!phase2 && boss.getHealth() / boss.getMaxHealth() < 0.5) {
                phase2 = true;
                summonCd = Math.min(summonCd, SUMMON_CD_P2);
                // 按需求: 阶段切换不再播报(阶段提醒只保留沙耶茶芽庇佑)
            }

            boss.setTarget(target);
            facePlayer(boss, target);
            // ===== 「冲刺撞击」(2026-09-10 深夜按用户要求新增, 仿光女的冲刺撞击) =====
            if (dashTicks > 0) {
                dashTicks--;
                double tdx = target.getX() - boss.getX();
                double tdy = target.getEyeY() - 0.6 - boss.getY();
                double tdz = target.getZ() - boss.getZ();
                double tdl = Math.max(0.001, Math.sqrt(tdx * tdx + tdy * tdy + tdz * tdz));
                double speed = 0.95;
                net.farmingtales.ftboss.util.BossCollision.move(boss,
                        new Vec3(tdx / tdl * speed, tdy / tdl * speed, tdz / tdl * speed));
                net.farmingtales.ftboss.util.Fx.particles(level, boss.getX(), boss.getY() + 1.2, boss.getZ(),
                        8, net.minecraft.core.particles.ParticleTypes.CRIT, 0.3, 0.03);
                if (tdl < 2.2 || dashTicks == 0) {
                    dashSlam(boss, level, target);   // 撞上了/冲完 -> 撞击结算
                    dashTicks = 0;
                    dashCd = (finalMode || phase2) ? 90 : 140;
                }
            } else {
                dashCd--;
                double distToTarget = boss.distanceTo(target);
                if (dashCd <= 0 && distToTarget > 6.0 && distToTarget < 26.0) {
                    dashTicks = 16;
                    dashCd = 140;
                    broadcast("§d糖霜女皇 §7蓄力冲刺！");
                    level.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                            net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.4f);
                }
                flyA += 0.02;
                double radius = 6.5;
                double hx = target.getX() + Math.cos(flyA) * radius;
                double hz = target.getZ() + Math.sin(flyA) * radius;
                double hy = target.getY() + 2.0 + Math.sin(tick * 0.05) * 0.35;
                double dx = hx - boss.getX(), dy = hy - boss.getY(), dz = hz - boss.getZ();
                double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dl > 0.8) {
                    double step = Math.min(0.28, dl);
                    net.farmingtales.ftboss.util.BossCollision.move(boss,
                            new Vec3(dx / dl * step, dy / dl * step, dz / dl * step));
                }
            }

            // 周期性波次召唤(辅助; 输出主力=女王自身铁魔法)
            summonCd--;
            if (summonCd <= 0) {
                summonCd = (finalMode || phase2) ? SUMMON_CD_P2 : SUMMON_CD;
                int[] w = WAVES[waveIdx % WAVES.length];
                waveIdx++;
                /* ★ 2026-09-24：并发上限 —— 到顶这一波就不刷了(见 MINION_CAP 的说明)。 */
                int alive = aliveMinions();
                int count = Math.min(w[0], Math.max(0, MINION_CAP - alive));
                if (count > 0) {
                    summonWave(count, Math.min(w[1], count), target);
                    broadcast("§d糖霜女皇 §7召唤了一队姜饼士兵…");
                } else if (tick % 100 == 0) {
                    FTBossMod.LOGGER.info("[FTBoss] 糖霜女皇 召唤已达上限({}/{}), 本波跳过", alive, MINION_CAP);
                }
            }

            // 手动施法: 冷却到且未在咏唱 -> 绑定目标施放(频率提高)
            if (boss instanceof IMagicEntity m) {
                castCd--;
                if (castCd <= 0) {
                    castCd = finalMode ? 45 : (phase2 ? 62 : 85);
                    if (!m.isCasting()) {
                        boss.setTarget(target);
                        castAt(m, target);
                    }
                }
            }

            if (tick % 20 == 0) {
                for (UUID c : crystals) {
                    Entity ce = level.getEntity(c);
                    if (ce instanceof Mob mini && mini.isAlive()) {
                        mini.setTarget(target);
                        if (mini.distanceToSqr(target) < 2.2 * 2.2) {
                try { mini.doHurtTarget(target); } catch (Throwable t) { FTBossMod.LOGGER.warn("[FTBoss] 小姜饼人代打失败(已忽略, 不崩服): {}", t.toString()); }
            }
                    }
                }
            }

            // ===== 狂暴(phase2/终局): 每秒自回血1点 + 反向甜蜜光环(缓慢III+虚弱II+饥饿II) =====
            if ((phase2 || finalMode) && tick % 20 == 0) {
                if (boss.getHealth() > 0 && boss.getHealth() < boss.getMaxHealth()) {
                    boss.heal(0.5f); // 1 HP/s
                }
                for (ServerPlayer p : level.players()) {
                    if (p.isAlive() && p.distanceToSqr(boss) < 8.0 * 8.0) {
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1)); // 缓慢III
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));          // 虚弱II
                        p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 140, 0));            // 饥饿II
                    }
                }
            }

            /* ★ v182 按用户要求「优化 boss 文本, 非必要减少文字输出」:
               这里原本**每 10 tick(0.5 秒)一次**把"糖霜女皇 X/Y"刷到所有玩家的行动栏上 ——
               而女皇本来就有 {@link net.minecraft.server.level.ServerBossEvent} 血条
               (见 {@code SugarQueenEntity.bossBar}), 屏幕顶部一直在显示同一个数字。
               两者重复, 且每半秒覆盖一次行动栏会把真正的机制提示(预警/禁疗等)挤掉,
               所以整段移除。血量仍由血条与每 100 tick 的日志保留, 信息不丢。 */
            if (tick % 100 == 0) {
                FTBossMod.LOGGER.info("[FTBoss] 血={}/{} 距离={} 施法中={} 波次CD={} 召唤物={}/{}",
                        (int) boss.getHealth(), (int) MAX_HP, String.format("%.1f", dist),
                        (boss instanceof IMagicEntity m && m.isCasting()) ? "是" : "否", summonCd,
                        aliveMinions(), MINION_CAP);
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] 战斗异常: ", t);
        }
    }

    /** 手动施法: 先绑定目标实体(法术才能锁定方向), 再开施法 */
    private static void castAt(IMagicEntity m, LivingEntity target) {
        try {
            // 狂暴等级(0/1/2)越高, 施放的法术等级越高: 4 -> 5 -> 6 (2026-09-10 按用户要求)
            int rage = (m instanceof net.minecraft.world.entity.LivingEntity le)
                    ? net.farmingtales.ftboss.boss.ai.BossRage.tierOf(le) : 0;
            // 2026-09-10 深夜按用户要求: **狂暴时更倾向史诗/传说法术**(70% 概率从 EPIC+ 里挑)
            List<AbstractSpell> pool = net.farmingtales.ftboss.integ.WizardSummon.spellPool();
            if (pool.isEmpty()) return;
            if (rage >= 1 && level.random.nextFloat() < 0.7f) {
                pool = net.farmingtales.ftboss.integ.WizardSummon.spellPoolRare(6, 3);   // 3=EPIC 4=LEGENDARY
            }
            AbstractSpell s = pool.get(level.random.nextInt(pool.size()));
            if (m instanceof LivingEntity caster
                    && !net.farmingtales.ftboss.integ.spells.BossSpellPacing.tryLegacyCast(caster, s)) return;
            int lvl = Math.max(s.getMinLevel(), Math.min(4 + rage, s.getMaxLevel()));
            try {
                Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                for (var ctor : tdc.getConstructors()) {
                    if (ctor.getParameterCount() == 1
                            && ctor.getParameterTypes()[0].isInstance(target)) {
                        Object inst = ctor.newInstance(target);
                        Method set = io.redspace.ironsspellbooks.api.magic.MagicData.class
                                .getMethod("setAdditionalCastData", io.redspace.ironsspellbooks.api.spells.ICastData.class);
                        Object md = io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData((net.minecraft.world.entity.LivingEntity) m);
                        set.invoke(md, inst);
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
            if (m instanceof net.minecraft.world.entity.LivingEntity le) {
                le.setYBodyRot(le.getYRot());
                le.setYHeadRot(le.getYRot());
            }
            m.initiateCastSpell(s, lvl);
            FTBossMod.LOGGER.info("[FTBoss] 女王施法: {} lv{} 目标={}", s.getSpellId(), lvl,
                    target.getName().getString());
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 施法异常: {}", t.toString());
        }
    }

    /**
     * 「冲刺撞击」结算(2026-09-10 深夜按用户要求新增, 仿光女的冲刺撞击):
     * 撞击点 4.5 格内的玩家吃 16 点伤害 + 强击退 + 缓慢, 并放冲击波粒子与两段音效。
     */
    private static void dashSlam(net.minecraft.world.entity.Entity boss, ServerLevel level, LivingEntity target) {
        final double range = 4.5;
        final float dmg = 8f;      // 16 → ×0.5(已含全局修正, v77)
        net.minecraft.world.entity.LivingEntity src =
                (boss instanceof net.minecraft.world.entity.LivingEntity le) ? le : null;
        if (target != null && !(target instanceof Player) && target.isAlive() && target.distanceTo(boss) <= range) {
            target.hurt(level.damageSources().mobAttack(src), dmg);
        }
        if (target != null && target.isAlive() && target.distanceTo(boss) > range) {
            // 目标在撞击边缘外: 至少让它吃一次"擦身"伤害, 免得冲过去却完全没打到
            target.hurt(level.damageSources().mobAttack(src), dmg * 0.5f);
        }
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().inflate(range))) {
            if (!p.isAlive()) continue;
            p.hurt(level.damageSources().mobAttack(src), dmg);
            net.minecraft.world.phys.Vec3 push = new net.minecraft.world.phys.Vec3(
                    p.getX() - boss.getX(), 0.6, p.getZ() - boss.getZ()).normalize().scale(1.8);
            net.farmingtales.ftboss.util.BossKnockback.push(p, push.x, push.y, push.z);
            p.hurtMarked = true;
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, false, true));
        }
        level.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 1.25f);
        level.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                net.minecraft.sounds.SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 0.8f);
        net.farmingtales.ftboss.util.Fx.particles(level, boss.getX(), boss.getY() + 0.8, boss.getZ(),
                40, net.minecraft.core.particles.ParticleTypes.CRIT, 1.2, 0.15);
        net.farmingtales.ftboss.util.Fx.particles(level, boss.getX(), boss.getY() + 0.4, boss.getZ(),
                30, net.minecraft.core.particles.ParticleTypes.CLOUD, 1.0, 0.05);
        broadcast("§d糖霜女皇 §7冲刺撞击！");
    }

    /** 面向玩家(含俯仰), 让铁魔法法术有正确朝向 */
    private static void facePlayer(Entity boss, LivingEntity target) {
        double dx = target.getX() - boss.getX();
        double dz = target.getZ() - boss.getZ();
        if (dx * dx + dz * dz < 0.001) return;
        boss.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90));
        double dy = (target.getY() + 1.2) - (boss.getY() + 1.0);
        double h = Math.sqrt(dx * dx + dz * dz);
        boss.setXRot((float) Math.toDegrees(Math.atan2(dy, Math.max(h, 0.01))));
    }

    /**
     * ★ 2026-09-24：还在场上的糖系召唤物数量 —— {@link #MINION_CAP} 的判据。
     *
     * <p>只数**活着且在世界上**的：死掉的、被清场的、所在区块卸载查不到的都不算
     * （查不到就不占额度，宁可少刷也不误判成"满员"卡死）。这里**不**顺手清理
     * {@link #crystals} —— 那个列表同时被"每隔 20 tick 指挥召唤物打人"和清场逻辑用着，
     * 区块卸载时实体只是暂时查不到，删条目会让它回来之后没人指挥。
     */
    private static int aliveMinions() {
        if (level == null) return 0;
        int alive = 0;
        for (UUID id : crystals) {
            Entity e = level.getEntity(id);
            if (e instanceof LivingEntity le && le.isAlive() && !le.isRemoved()) alive++;
        }
        return alive;
    }

    /** 糖系召唤物命名表: 0小姜饼人 1焦糖结晶 2糖霜蜘蛛 3软糖史莱姆 */
    private static final String[] MINION_NAMES = {"小姜饼人", "焦糖结晶", "糖霜蜘蛛", "软糖史莱姆"};
    private static final ChatFormatting[] MINION_COLORS = {
            ChatFormatting.WHITE, ChatFormatting.GOLD, ChatFormatting.WHITE, ChatFormatting.LIGHT_PURPLE};

    private static void summonWave(int count, int elites, LivingEntity target) {
        int spawned = 0, tries = 0;
        while (spawned < count && tries < count * 5) {
            tries++;
            try {
                double a = Math.random() * Math.PI * 2;
                double r = 3 + Math.random() * 5;
                double x = bossX() + Math.cos(a) * r;
                double z = bossZ() + Math.sin(a) * r;
                double y = findSafeY(x, z);
                if (y == Double.MIN_VALUE) continue;
                boolean elite = spawned < elites;
                // 2026-09-10 晚 按用户要求「姜饼士兵召唤增加, 其他召唤物减少」:
                // 非精英每 4 只里 3 只是**小姜饼人**(kind 0), 第 4 只才轮到焦糖结晶/糖霜蜘蛛/软糖史莱姆;
                // 精英仍走 小姜饼人 / 焦糖结晶 两类(僵尸精英才会穿粉甲拿粉剑, 见 applyEliteGear)。
                int kind;
                if (elite) {
                    kind = (waveIdx + spawned) % 2;
                } else {
                    kind = (spawned % 4 == 3) ? 1 + ((waveIdx + spawned) % 3) : 0;
                }
                Mob mini = createMinion(kind);
                if (mini == null) continue;
                mini.teleportTo(x, y, z);
                level.addFreshEntity(mini);
                mini.setCustomName(Component.literal(MINION_NAMES[kind] + (elite ? "·精英" : ""))
                        .withStyle(elite ? ChatFormatting.GOLD : MINION_COLORS[kind]));
                mini.setCustomNameVisible(true);
                mini.setGlowingTag(true);
                net.farmingtales.ftboss.boss.ai.MinionBlessing.mark(mini, bossUUID);   // v112: 带主人(清场只清自己的)
                if (elite) {
                    mini.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 999999, 2, false, false));
                    mini.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 999999, 1, false, false));
                    mini.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 999999, 1, false, false)); // 更肉
                    if (mini instanceof Zombie zb) {
                        applyEliteGear(zb);
                        zb.setHealth(zb.getMaxHealth());
                    }
                } else {
                    // 按种类差异化: 蜘蛛高速; 尸壳/史莱姆高攻; 僵尸均衡
                    // 2026-09-10 深夜按用户要求"小姜饼人加强一点": kind 0(姜饼士兵) 额外 力量II + 抗性I
                    mini.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 999999, kind == 2 ? 2 : 1, false, false));
                    mini.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 999999, kind == 3 ? 1 : (kind == 0 ? 1 : 0), false, false));
                    if (kind == 0) {
                        mini.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 0, false, false));
                    }
                    if (mini instanceof Zombie) {
                        mini.setItemSlot(EquipmentSlot.MAINHAND,
                                sweetItem(SWEETS[level.random.nextInt(SWEETS.length)], 2 + level.random.nextInt(2), null));
                        mini.setDropChance(EquipmentSlot.MAINHAND, 0f);
                    }
                }
                crystals.add(mini.getUUID());
                if (target != null) mini.setTarget(target);
                spawned++;
            } catch (Throwable t) {
                FTBossMod.LOGGER.error("[FTBoss] 波次召唤异常: ", t);
            }
        }
    }

    /** 按种类创建糖系召唤物 */
    private static Mob createMinion(int kind) {
        try {
            Mob m;
            switch (kind) {
                /* 2026-10-10 按用户要求: 糖霜的「焦糖结晶」原为原版 minecraft:husk,
                   会被第三方缴械白名单/亡灵增伤/斩首命中, 改用自有类型 ftboss:manor_husk。
                   注: husk 是 Zombie 子类, 下面 `mini instanceof Zombie` 的精英装备逻辑照旧生效。 */
                case 1 -> m = net.farmingtales.ftboss.entity.FTEntities.MANOR_HUSK.get().create(level);
                case 2 -> m = EntityType.SPIDER.create(level);
                case 3 -> {
                    m = EntityType.SLIME.create(level);
                    if (m instanceof Slime sl) sl.setSize(3, false); // 大号软糖史莱姆
                }
                // 0 = 小姜饼人(2026-09-10 新增的自绘召唤物, 取代原版僵尸)
                default -> m = FTEntities.GINGERBREAD_MINION.get().create(level);
            }
            return m;
        } catch (Throwable t) {
            return null;
        }
    }

    private static void applyEliteGear(Zombie zombie) {
        zombie.setItemSlot(EquipmentSlot.HEAD, coloredLeather(Items.LEATHER_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, coloredLeather(Items.LEATHER_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.LEGS, coloredLeather(Items.LEATHER_LEGGINGS));
        zombie.setItemSlot(EquipmentSlot.FEET, coloredLeather(Items.LEATHER_BOOTS));
        zombie.setItemSlot(EquipmentSlot.MAINHAND,
                sweetItem(SWEETS[level.random.nextInt(SWEETS.length)], 4 + level.random.nextInt(2), null));
        for (EquipmentSlot s : EquipmentSlot.values()) zombie.setDropChance(s, 0f);
    }

    private static ItemStack coloredLeather(Item item) {
        ItemStack s = new ItemStack(item);
        s.getOrCreateTagElement("display").putInt("color", PINK);
        return s;
    }

    private static ItemStack sweetItem(ItemLike it, int glintLevel, String name) {
        ItemStack s = new ItemStack(it);
        if (glintLevel > 0) s.enchant(Enchantments.SHARPNESS, glintLevel);
        if (name != null && !name.isEmpty()) {
            s.setHoverName(Component.literal(name).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        return s;
    }

    private static double findSafeY(double x, double z) {
        int base = (int) Math.floor(bossY());
        for (int dy = 0; dy >= -14; dy--) {
            int y = base + dy;
            if (isSafe(x, y, z)) return y + 0.6;
        }
        for (int dy = 1; dy <= 14; dy++) {
            int y = base + dy;
            if (isSafe(x, y, z)) return y + 0.6;
        }
        return Double.MIN_VALUE;
    }

    private static boolean isSafe(double x, int y, double z) {
        BlockPos here = BlockPos.containing(x, y, z);
        return level.isEmptyBlock(here) && level.isEmptyBlock(here.above()) && !level.isEmptyBlock(here.below());
    }

    // ---------- 不死图腾终局 ----------
    public static boolean tryPopTotem(Mob boss) {
        if (!active || boss == null || !boss.getUUID().equals(bossUUID)) return false;
        if (totemUsed) return false;
        try {
            totemUsed = true;
            finalMode = true;
            summonCd = Math.min(summonCd, SUMMON_CD_P2);
            boss.setHealth((float) (MAX_HP * 0.3));
            boss.removeAllEffects();
            if (boss instanceof IMagicEntity m) {
                try { m.cancelCast(); } catch (Throwable ignored) {}
            }
            // 按需求: 阶段切换不再播报(阶段提醒只保留沙耶茶芽庇佑)
            level.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1, 1);
            FTBossMod.LOGGER.info("[FTBoss] 糖霜女皇 触发终局(图腾)");
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] 终局触发异常: ", t);
            return false;
        }
    }

    private static double bossX() { Entity e = level.getEntity(bossUUID); return e != null ? e.getX() : 0; }
    private static double bossY() { Entity e = level.getEntity(bossUUID); return e != null ? e.getY() : 100; }
    private static double bossZ() { Entity e = level.getEntity(bossUUID); return e != null ? e.getZ() : 0; }

    /** 聊天播报已按需移除: 仅写日志, 不再向玩家发技能/阶段文字 */
    private static void broadcast(String msg) {
        FTBossMod.LOGGER.info("[FTBoss] {}", msg);
    }

    public static void onBossDeath(Mob boss) {
        if (net.farmingtales.ftboss.boss.ai.LemonSummonRewards.suppressed(boss)) return;
        if (!active || boss == null || !boss.getUUID().equals(bossUUID)) return;
        try {
            double x = boss.getX(), y = boss.getY(), z = boss.getZ();
            int sugar = 24 + level.random.nextInt(25);
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(level, new ItemEntity(level, x, y, z, new ItemStack(Items.SUGAR, sugar)), boss);
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(level, new ItemEntity(level, x, y + 0.3, z,
                    new ItemStack(net.farmingtales.ftboss.block.FTBlocks.STATUE_SUGAR_QUEEN.get(), 1)), boss);
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(level, new ExperienceOrb(level, x, y, z, 200), boss);
            level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1, 1);
            broadcast("§d糖霜女皇 §7被打败了——留下一地糖霜与石像…");
            FTBossMod.LOGGER.info("[FTBoss] 糖霜女皇 被击败");
        } catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] 死亡掉落异常: ", t);
        } finally {
            cleanup("被击败");
        }
    }

    public static void cleanup() {
        cleanup("未注明(外部调用)");
    }

    /**
     * 清场。与 MeatBoss 保持一致: 用 {@code discard()} 而不是 {@code kill()} ——
     * kill() 走 hurt(), 会被限伤/无敌帧/死亡事件拦掉而清不干净, 退出游戏后会在存档里留下"幽灵Boss"。
     */
    public static void cleanup(String reason) {
        if (active) FTBossMod.LOGGER.info("[FTBoss] 糖霜女皇 清场: {}", reason);
        active = false;
        if (level != null) {
            for (UUID c : crystals) {
                Entity e = level.getEntity(c);
                if (e != null) e.discard();
            }
            crystals.clear();
            if (bossUUID != null) {
                Entity b = level.getEntity(bossUUID);
                /* 2026-09-12 v58 按用户反馈「召唤物死不干净」: 女皇的小怪(小姜饼人/焦糖结晶/糖霜蜘蛛/软糖史莱姆)
                   是带 ftboss_summon 标记召出来的, 以前清场只收"结晶", 小怪会留在场上打人 */
                if (b instanceof net.minecraft.world.entity.LivingEntity le) {
                    net.farmingtales.ftboss.boss.ai.SummonSweep.clearMarked(le,
                            net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE);
                }
                if (b != null) b.discard();
            }
        }
        bossUUID = null;
        ownerUUID = null;
        level = null;
        tick = 0;
        finalMode = false;
        totemUsed = false;
    }

    public static boolean isActive() { return active; }

    public static boolean isBoss(Entity e) {
        return active && e != null && e.getUUID().equals(bossUUID);
    }

    public static UUID ownerIdOf() { return ownerUUID; }
}
