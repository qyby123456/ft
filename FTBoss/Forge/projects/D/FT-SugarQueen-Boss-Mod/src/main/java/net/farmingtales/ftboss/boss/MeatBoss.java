package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.farmingtales.ftboss.boss.ai.ButcherMinionMeleeGoal;
import net.farmingtales.ftboss.boss.ai.ButcherPhaseRules;
import net.farmingtales.ftboss.boss.ai.ButcherPhases;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 屠夫: 肉类近战Boss 静态战斗大脑(模板同 SugarQueenBoss, 无飞行/无铁魔法)
 * 外观: 卫道士套皮放大 + 专属屠夫皮肤; 召唤(主世界安全系, 不僵尸化): 屠夫学徒(卫道士) / 腌肉尸壳 / (精英)屠夫队长
 * 死亡掉落: 屠夫石像(纪念品) + 烤肉; 触发: 屠夫之心(合成)
 */
public final class MeatBoss {

    private static final double MAX_HP = 760;
    private static final double DESPAWN_RANGE = 50;
    private static final int SUMMON_CD_P2 = ButcherCombatRules.ENRAGED_SUMMON_INTERVAL;
    /** 波次: {数量, 精英数} */
    private static final int[][] WAVES = { {2, 0}, {2, 1}, {3, 1}, {3, 1} };

    /** 真·无玩家时的宽限计时(tick): 60 秒后才清场(玩家死亡读秒不算"无玩家") */
    private static final int NO_PLAYER_GRACE = 20 * 60;
    private static int noPlayerTicks = 0;

    /** 是否有玩家在脱战范围内, **或者有玩家正在死亡读秒**(后者也算"还在") */
    private static boolean anyPlayerPresentOrDying(net.minecraft.world.entity.Entity boss) {
        if (level == null) return false;
        for (ServerPlayer p : level.players()) {
            if (!p.isAlive()) return true;                                  // 死亡界面/等待重生
            if (p.distanceToSqr(boss) <= DESPAWN_RANGE * DESPAWN_RANGE) return true;
        }
        return false;
    }

    private static final String[] MINION_NAMES = {"屠夫学徒", "腌肉尸壳", "屠夫队长·精英"};
    private static final ChatFormatting[] MINION_COLORS = {
            ChatFormatting.WHITE, ChatFormatting.GOLD, ChatFormatting.RED};

    private static boolean active = false;
    private static ServerLevel level;
    private static UUID bossUUID;
    private static UUID ownerUUID;
    private static boolean phase2 = false;
    private static boolean finalMode = false;
    private static boolean totemUsed = false;
    private static int tick = 0;
    private static int summonCd = ButcherCombatRules.FIRST_SUMMON_DELAY;
    private static int waveIdx = 0;
    private static final List<UUID> minions = new ArrayList<>();

    private MeatBoss() {}

    /**
     * 额外召唤的屠夫「复制体」(2026-09-11 按用户要求"允许玩家召唤同种多个boss")。
     * 与本体同款外观/双持/血量, 战斗由 {@code MeatMountainEntity.initCloneButcher()} 的原版近战 AI 驱动。
     */
    private static boolean spawnClone(ServerPlayer sp) {
        try {
            ServerLevel lvl = sp.serverLevel();
            Entity raw = FTEntities.MEAT_MOUNTAIN.get().create(lvl);
            if (!(raw instanceof MeatMountainEntity boss)) return false;
            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.position().add(look.x * 3, 0, look.z * 3);
            boss.setCloneButcher(true);
            boss.equipMeatLeg();
            boss.setCustomName(Component.literal("§c§l")
                    .append(Component.translatable("boss.ftboss.butcher"))
                    .append(Component.literal("§7·复制体")));
            boss.setCustomNameVisible(true);
            boss.setHealth((float) MAX_HP);
            if (!net.farmingtales.ftboss.util.BossCollision.place(lvl, boss,
                    new Vec3(pos.x, sp.getY(), pos.z),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, sp.getYRot() + 180.0F)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(sp, "屠夫复制体");
                return false;
            }
            boss.setPersistenceRequired();
            if (!lvl.addFreshEntity(boss)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(sp, "屠夫复制体");
                return false;
            }
            lvl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1, 0.8f);
            sp.displayClientMessage(Component.literal("§c屠夫复制体加入战斗！"), true);
            FTBossMod.LOGGER.info("[FTBoss] 屠夫复制体加入战斗 @ {} {} {}", pos.x, pos.y, pos.z);
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 屠夫复制体召唤失败: {}", t.toString());
            return false;
        }
    }

    /**
     * 右键屠夫之心触发。
     * <p><b>2026-09-10 修改</b>：
     * <ol>
     *   <li><b>取消「糖霜女皇在场就不能召唤屠夫」这条门槛</b>(按用户确认)：五个 Boss 允许同时在场；</li>
     *   <li>登记的实体若已不在场，<b>自动清场自愈</b>，不再永久卡住"已在战斗中"；</li>
     *   <li>召唤前清掉世界里所有残留屠夫实体(旧存档遗骸/退出未清)，保证场上只留刚召唤的这只；</li>
     *   <li>任何失败原因都明确提示玩家 + 写日志，不再静默返回 false。</li>
     * </ol>
     */
    public static boolean tryStart(ServerPlayer sp) {
        /* 2026-09-13 v93 按用户要求「全部 boss 都必须在主世界召唤」——
           闸放在最前面(早于"自愈清场"), 免得在下界白清一次场。克隆体那条路也一起盖住。 */
        if (!SummonGate.allow(sp.serverLevel(), sp, "屠夫")) return false;
        // 自愈: 登记的屠夫已不在场(死亡/卸载/被移除/存档残留) -> 清场后允许重新召唤
        if (active && !trackedBossAlive()) {
            FTBossMod.LOGGER.info("[FTBoss] 屠夫状态自愈: 登记的实体已不在场, 清理后允许重新召唤");
            cleanup("自愈: 登记的实体不在场");
        }
        if (active) {
            // 2026-09-11 按用户要求"允许玩家召唤同种多个boss": 已有一只在打时, 额外召唤变成**克隆体**
            // (克隆体由 MeatMountainEntity 里的原版近战 AI 自行战斗, 不占用本体的静态大脑)
            return spawnClone(sp);
        }
        try {
            ServerLevel lvl = sp.serverLevel();
            Entity raw = FTEntities.MEAT_MOUNTAIN.get().create(lvl);
            if (!(raw instanceof MeatMountainEntity boss)) {
                sp.displayClientMessage(Component.literal("§c屠夫实体创建失败"), true);
                FTBossMod.LOGGER.warn("[FTBoss] 屠夫召唤失败: create() 未返回屠夫实体");
                return false;
            }
            // 清掉世界里所有"未登记"的屠夫(旧存档遗骸/退出未清/0血残体), 避免场上出现两只或看不见的幽灵
            int strays = purgeStrays(sp.getServer());
            if (strays > 0) FTBossMod.LOGGER.info("[FTBoss] 屠夫召唤前清理残留实体 {} 只", strays);

            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.position().add(look.x * 2.5, 0, look.z * 2.5);
            if (!net.farmingtales.ftboss.util.BossCollision.place(lvl, boss,
                    new Vec3(pos.x, sp.getY(), pos.z),
                    net.farmingtales.ftboss.util.BossCollision.SpawnMode.GROUND, sp.getYRot() + 180.0F)) {
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(sp, "屠夫");
                return false;
            }
            boss.setCustomName(Component.literal("§c§l").append(Component.translatable("boss.ftboss.butcher")));
            boss.setCustomNameVisible(true);
            boss.setHealth((float) MAX_HP);
            boss.setPersistenceRequired();
            boss.equipMeatLeg();

            // 登记必须在入世界之前: EntityJoinLevelEvent 的"无主屠夫清理"用 isBoss() 判定, 否则会误删本体
            level = lvl;
            bossUUID = boss.getUUID();
            ownerUUID = sp.getUUID();
            active = true;
            phase2 = false;
            finalMode = false;
            totemUsed = false;
            tick = 0;
            summonCd = ButcherCombatRules.FIRST_SUMMON_DELAY;
            waveIdx = 0;
            noPlayerTicks = 0;
            minions.clear();

            if (!lvl.addFreshEntity(boss)) {
                cleanup("入世界失败(addFreshEntity 返回 false)");
                net.farmingtales.ftboss.util.BossCollision.tellNoRoom(sp, "屠夫");
                return false;
            }
            lvl.playSound(null, pos.x, pos.y, pos.z, SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.2f, 0.7f);
            FTBossMod.LOGGER.info("[FTBoss] 屠夫 召唤 @ {} {} {} (实体id={}, 入世界后可按UUID取回={})",
                    String.format("%.1f", pos.x), String.format("%.1f", pos.y), String.format("%.1f", pos.z),
                    boss.getId(), lvl.getEntity(bossUUID) == boss);
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] 屠夫召唤异常: ", t);
            cleanup("召唤过程抛异常");
            sp.displayClientMessage(Component.literal("§c屠夫召唤异常, 详见日志"), true);
            return false;
        }
    }

    /** 登记的屠夫实体是否还在场(已加载区块内 且 未被移除 且 活着) */
    private static boolean trackedBossAlive() {
        if (level == null || bossUUID == null) return false;
        Entity e = level.getEntity(bossUUID);
        return e instanceof MeatMountainEntity m && !m.isRemoved() && m.isAlive();
    }

    /** 清掉所有"未被本大脑登记"的屠夫实体(旧存档残留/退出未清/0血遗骸), 返回清理数量 */
    public static int purgeStrays(net.minecraft.server.MinecraftServer srv) {
        if (srv == null) return 0;
        List<MeatMountainEntity> strays = new ArrayList<>();
        for (ServerLevel sl : srv.getAllLevels()) {
            for (Entity e : sl.getAllEntities()) {
                // 2026-09-11: 玩家额外召唤的"复制体"是合法实体, 不算残留
                if (e instanceof MeatMountainEntity m && !m.getUUID().equals(bossUUID)
                        && !m.isCloneButcher()) strays.add(m);
            }
        }
        for (MeatMountainEntity m : strays) {
            m.hideBossBar();
            m.discard();
        }
        return strays.size();
    }

    public static void tick() {
        if (!active || level == null) return;
        try {
            tick++;
            Entity e = level.getEntity(bossUUID);
            if (e == null) { cleanup("实体不在已加载区块(被卸载/未同步)"); return; }
            if (!(e instanceof Mob boss) || boss.isRemoved()) { cleanup("实体已被移除"); return; }
            if (boss.getHealth() <= 0) {
                cleanup("血量归零但未触发死亡逻辑");
                return;
            }

            LivingEntity target = net.farmingtales.ftboss.boss.ai.BossTargets.combatTarget(boss, 30);
            if (target == null) {
                // 2026-09-11 修"只有屠夫会离场": 原来只要"最近玩家距离 > 50 格"就立刻清场,
                // 而玩家死亡瞬间(死亡界面/重生读秒)会被当成"没人了" -> 屠夫当场清场走人(日志实测:
                // 玩家死亡后 1.6 秒 "屠夫 清场: 附近无玩家")。现在:
                //   ① 只要有玩家**正在死亡读秒**(isAlive()==false) 就视为"还在", 计时清零;
                //   ② 真没人也要**宽限 60 秒**才清场(避免玩家临时跑远就被判脱战)。
                if (anyPlayerPresentOrDying(boss)) {
                    noPlayerTicks = 0;
                } else if (++noPlayerTicks > NO_PLAYER_GRACE) {
                    cleanup("附近长时间(" + (NO_PLAYER_GRACE / 20) + " 秒)无玩家");
                }
                return;
            }
            noPlayerTicks = 0;

            double dist = boss.distanceTo(target);

            // ===== 索敌 =====
            boss.setTarget(target);

            // ===== 波次召唤(疣猪兽/猪灵/蛮兵/石像) =====
            if (phase2) summonCd--;
            if (phase2 && summonCd <= 0) {
                summonCd = SUMMON_CD_P2;
                int[] w = WAVES[waveIdx % WAVES.length];
                waveIdx++;
                if (summonWave(w[0], w[1], target) > 0) {
                    broadcast("§c屠夫 §7嚎叫一声，屠夫学徒们围了上来…");
                }
            }

            if (tick % 5 == 0) {
                minions.removeIf(id -> {
                    Entity summon = level.getEntity(id);
                    return summon == null || !summon.isAlive() || summon.isRemoved();
                });
                for (UUID u : minions) {
                    Entity ce = level.getEntity(u);
                    if (ce instanceof Mob mini && mini.isAlive()) {
                        mini.setTarget(mini.isAlliedTo(target) ? null : target);
                    }
                }
            }

            if (tick % 10 == 0) {
                int hp = (int) Math.max(0, Math.ceil(boss.getHealth()));
                for (ServerPlayer p : level.players()) {
                    if (p.distanceToSqr(boss) < DESPAWN_RANGE * DESPAWN_RANGE) {
                        // 按需求: 不再附阶段后缀(烤肉狂暴/屠夫终局), 只显示纯血量
                        p.displayClientMessage(Component.translatable("msg.ftboss.butcher_hp", hp, (int) boss.getMaxHealth()), true);
                    }
                }
            }
            if (tick % 100 == 0) {
                FTBossMod.LOGGER.info("[FTBoss] 屠夫: 血={}/{} 距离={} 波次CD={}",
                        (int) boss.getHealth(), (int) boss.getMaxHealth(), String.format("%.1f", dist), summonCd);
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] 屠夫战斗异常: ", t);
        }
    }

    public static float onMeatHitPlayer(ServerPlayer player, float dealt) {
        return dealt;
    }

    private static int summonWave(int count, int elites, LivingEntity target) {
        count = ButcherCombatRules.summonCount(count, minions.size());
        int spawned = 0, tries = 0;
        while (spawned < count && tries < count * 5) {
            tries++;
            try {
                double a = Math.random() * Math.PI * 2;
                double r = 3 + Math.random() * 6;
                double x = bossX() + Math.cos(a) * r;
                double z = bossZ() + Math.sin(a) * r;
                double y = findSafeY(x, z);
                if (y == Double.MIN_VALUE) continue;
                boolean elite = spawned < elites;
                int kind = elite ? 2 : spawned % 2; // 精英=屠夫队长; 普通=屠夫学徒/腌肉尸壳
                Mob mini = createMinion(kind);
                if (mini == null) continue;
                mini.teleportTo(x, y, z);
                mini.setCustomName(Component.literal(MINION_NAMES[kind]).withStyle(MINION_COLORS[kind]));
                mini.setCustomNameVisible(true);
                mini.setGlowingTag(true);
                net.farmingtales.ftboss.boss.ai.MinionBlessing.mark(mini, bossUUID);
                boostMinion(mini, kind);
                if (!level.addFreshEntity(mini)) continue;
                minions.add(mini.getUUID());
                if (target != null) mini.setTarget(target);
                spawned++;
            } catch (Throwable t) {
                FTBossMod.LOGGER.error("[FTBoss] 屠夫波次召唤异常: ", t);
            }
        }
        return spawned;
    }

    private static Mob createMinion(int kind) {
        try {
            return net.minecraft.world.entity.EntityType.VINDICATOR.create(level);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void boostMinion(Mob mini, int kind) {
        mini.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ButcherCombatRules.minionHealth(kind));
        mini.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ButcherCombatRules.minionDamage(kind));
        mini.getAttribute(Attributes.ARMOR).setBaseValue(ButcherCombatRules.minionArmor(kind));
        mini.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(ButcherCombatRules.minionSpeed(kind));
        mini.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(kind == 2 ? 0.7 : 0.4);
        mini.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(ButcherCombatRules.TARGET_RANGE);
        mini.setHealth(mini.getMaxHealth());
        mini.setPersistenceRequired();
        mini.setCanPickUpLoot(false);
        if (mini instanceof PathfinderMob pathfinder) {
            mini.goalSelector.addGoal(1, new ButcherMinionMeleeGoal(pathfinder, kind));
        }
        mini.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.farmingtales.ftboss.item.FTBossItems.TURKEY_LEGS.get()));
        mini.setDropChance(EquipmentSlot.MAINHAND, 0f);
        if (kind == 1) mini.setCustomName(Component.literal("屠夫卫道士").withStyle(ChatFormatting.GOLD));
        if (kind == 2) {
            mini.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 0, false, false));
        }
    }

    private static double bossX() { Entity e = level.getEntity(bossUUID); return e != null ? e.getX() : 0; }
    private static double bossY() { Entity e = level.getEntity(bossUUID); return e != null ? e.getY() : 100; }
    private static double bossZ() { Entity e = level.getEntity(bossUUID); return e != null ? e.getZ() : 0; }

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

    // ---------- 图腾二命(终局) ----------
    public static boolean tryPopTotem(Mob boss) {
        if (net.farmingtales.ftboss.boss.ai.LemonSummonRewards.suppressed(boss)) return false;
        return boss instanceof MeatMountainEntity butcher && ButcherPhases.advance(butcher);
    }

    public static void onPhaseChanged(MeatMountainEntity boss, int phase) {
        if (!isBoss(boss)) return;
        phase2 = phase == ButcherPhaseRules.ASSAULT;
        finalMode = phase == ButcherPhaseRules.RECOVERY;
        summonCd = ButcherCombatRules.FIRST_SUMMON_DELAY;
        waveIdx = 0;
        if (phase2 && level != null) {
            summonWave(net.farmingtales.ftboss.boss.ai.ButcherBalanceRules.OPENING_MINIONS, 1, boss.getTarget());
            return;
        }
        if (level == null) return;
        for (UUID minionId : minions) {
            Entity minion = level.getEntity(minionId);
            if (minion != null) minion.discard();
        }
        minions.clear();
    }

    private static void broadcast(String msg) {
        FTBossMod.LOGGER.info("[FTBoss] {}", msg);
    }

    public static void onBossDeath(Mob boss) {
        if (net.farmingtales.ftboss.boss.ai.LemonSummonRewards.suppressed(boss)) return;
        if (!active || boss == null || !boss.getUUID().equals(bossUUID)) return;
        try {
            double x = boss.getX(), y = boss.getY(), z = boss.getZ();
            int pork = 12 + level.random.nextInt(16);
            int beef = 8 + level.random.nextInt(8);
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(level, new ItemEntity(level, x, y, z, new ItemStack(Items.COOKED_PORKCHOP, pork)), boss);
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(level, new ItemEntity(level, x, y + 0.3, z, new ItemStack(Items.COOKED_BEEF, beef)), boss);
            // 掉落: 屠夫的「白膜石像」(2026-09-10: 老的 butcher_statue 已删除, 统一改为白膜石像)
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(level, new ItemEntity(level, x, y + 0.6, z,
                    new ItemStack(net.farmingtales.ftboss.block.FTBlocks.STATUE_BUTCHER.get().asItem(), 1)), boss);
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(level, new ExperienceOrb(level, x, y, z, 150), boss);
            level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1, 1);
            broadcast("§c屠夫 §7倒下了——掉了一地烤肉和一座石像…");
            FTBossMod.LOGGER.info("[FTBoss] 屠夫 被击败");
        } catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] 屠夫死亡掉落异常: ", t);
        } finally {
            cleanup("被击败");
        }
    }

    public static void cleanup(String reason) {
        if (active) FTBossMod.LOGGER.info("[FTBoss] 屠夫 清场: {}", reason);
        active = false;
        if (level != null) {
            for (UUID u : minions) {
                Entity e = level.getEntity(u);
                if (e != null) e.discard();
            }
            minions.clear();
            if (bossUUID != null) {
                Entity b = level.getEntity(bossUUID);
                if (b != null) {
                    // 用 discard() 而不是 kill(): kill() 走 hurt(), 会被限伤/无敌帧/死亡事件拦截而清不掉,
                    // 这正是"退出游戏后存档里留下幽灵屠夫"的来源
                    if (b instanceof MeatMountainEntity mm) mm.hideBossBar();
                    b.discard();
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

    /** 兼容旧调用点(不注明原因) */
    public static void cleanup() { cleanup("未注明(外部调用)"); }

    public static boolean isActive() { return active; }

    public static boolean isBoss(Entity e) {
        return active && e != null && e.getUUID().equals(bossUUID);
    }

    public static UUID ownerIdOf() { return ownerUUID; }
}
