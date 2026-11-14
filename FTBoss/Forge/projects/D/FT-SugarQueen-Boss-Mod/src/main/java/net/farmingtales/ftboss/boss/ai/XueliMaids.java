package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 雪黎女仆护法：战斗中每 120 秒召唤 6 位车万女仆，存活护法维持免伤与缓慢回血。
 * 2026-10-21：召唤改为独立计时，同一时间保留一组，下一次召唤替换上一组。
 */
public final class XueliMaids {

    /** 计分板标签: 只有带这个标签的女仆才算"雪黎的护法" */
    public static final String GUARD_TAG = "ftboss_xueli_guard";
    /** ★ v119: 同时写一份**持久化数据**标记 —— 计分板标签可能被别的模组/指令清掉, 持久化数据跟着实体存档走 */
    public static final String GUARD_KEY = "ftboss_xueli_guard";
    private static final String GUARD_OWNER = "ftboss_xueli_guard_owner";
    /** 批次数量范围与每批人数 */
    public static final int BATCH_MIN = 3;
    public static final int BATCH_MAX = 3;
    public static final int PER_BATCH = 2;
    public static final int MIN_TOTAL = BATCH_MIN * PER_BATCH;
    public static final int MAX_TOTAL = BATCH_MAX * PER_BATCH;
    /** 两次召唤之间的冷却(tick) */
    public static final int TRIGGER_CD = 120 * 20;
    /** 每位女仆每秒回复的最大生命百分比(0.1% —— "缓慢") */
    public static final double HEAL_PERCENT_PER_MAID = 0.001;
    /** 护法单跳伤害上限 = 最大生命 × 此比例（2026-10-10，见 {@link #onGuardHurtAmount}）。 */
    public static final float GUARD_HIT_CAP = 0.25F;
    /** 同上，仅用于日志文案。 */
    public static final int GUARD_HIT_CAP_PERCENT = 25;
    private static final double FOLLOW_DISTANCE = 16.0;
    private static final double PLAYER_RANGE = 48.0;
    /** 动作栏提醒间隔(tick) */
    private static final int REMIND_CD = 100;
    /** 被打时提示的间隔(tick) */
    private static final int BLOCK_NOTIFY_CD = 40;

    private final EasterBossEntity boss;
    private final Map<UUID, TrackedMaid> tracked = new HashMap<>();
    private long discoveryAt = -1L;

    private static final class TrackedMaid {
        private Mob entity;
        private BlockPos position;

        private TrackedMaid(Mob entity, BlockPos position) {
            this.entity = entity;
            this.position = position;
        }
    }

    private boolean shield = false;
    private int remindCd = 20;
    private int blockCd = 0;
    /** v118: 刚召唤后的宽限 tick —— 这一秒内不判"女仆全灭"(缓存/区块加载都可能慢半拍) */
    private int graceTicks = 0;
    /** v119: 上次召唤的游戏刻 —— 用来发现"刚召出来就全灭"这种异常并打警告 */
    private long summonTick = 0L;
    /** 本轮计时起点的游戏刻(-1 = 战斗计时尚未开始) */
    private long lastTrigger = -1L;

    public XueliMaids(EasterBossEntity boss) {
        this.boss = boss;
    }

    /** 是否处于护盾状态(免伤中) */
    public boolean shielded() {
        return this.shield;
    }

    /**
     * 这只实体是不是"雪黎的护法"(计分板标签 **或** 持久化标记)。
     *
     * <p>v119 起 {@code EasterBossEntity.isValidMobTarget} 会用它把护法排除在索敌之外 ——
     * 这正是「女仆没死护盾就失效」的真凶: 女仆是**外来实体**(车万女仆), 以前会被雪黎自己
     * 当成目标一刀砍死(日志 `雪黎的女仆被雪黎杀死了`)。沙耶的茶芽没这个问题, 因为茶芽是
     * 本模组自己的实体、天然被排除。
     */
    public static boolean isGuard(Entity e) {
        if (e == null) return false;
        if (e.getTags().contains(GUARD_TAG)) return true;
        try {
            return e.getPersistentData().getBoolean(GUARD_KEY);
        } catch (Throwable t) {
            return false;
        }
    }

    private long cacheAt = -1L;      // v121: -1 = "还没扫过"(不能用 Long.MIN_VALUE, now-cacheAt 会溢出)

    /** 让存活缓存失效(下一次 {@link #aliveMaids()} 一定重扫)。**不要**写 Long.MIN_VALUE, 会溢出 */
    private void invalidateCache() {
        this.cacheAt = -1L;
    }

    public int aliveMaids() {
        long now = this.boss.level().getGameTime();
        if (this.cacheAt < 0L || now - this.cacheAt >= 10L) {     // v121: cacheAt<0 时无条件重扫(而不是靠溢出)
            this.cacheAt = now;
            collect();
        }
        return this.tracked.size();
    }

    /** 独立计时到期后才可召唤，技能轮换不会提前触发。 */
    public boolean canTrigger() {
        long now = this.boss.level().getGameTime();
        return this.lastTrigger >= 0L && now >= this.lastTrigger
                && now - this.lastTrigger >= XueliPhases.cooldown(TRIGGER_CD, this.boss);
    }

    private void tickSummoning() {
        if (!(this.boss.level() instanceof ServerLevel) || this.boss.isNoAi() || this.boss.isMini()) return;
        LivingEntity target = this.boss.getTarget();
        if (target == null || !target.isAlive()) return;
        long now = this.boss.level().getGameTime();
        if (this.lastTrigger < 0L || now < this.lastTrigger) {
            this.lastTrigger = now;
            return;
        }
        if (this.canTrigger()) this.trigger();
    }

    /** 车万女仆的实体类型(没装那个模组就返回 null) */
    private static EntityType<?> maidType() {
        try {
            return ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("touhou_little_maid", "maid"));
        } catch (Throwable t) {
            return null;
        }
    }

    /** 召唤护法并开启免伤 */
    public void trigger() {
        if (!(this.boss.level() instanceof ServerLevel sl) || !this.canTrigger()) return;
        EntityType<?> type = maidType();
        if (type == null) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 雪黎女仆护法: 没找到 touhou_little_maid:maid, 本次跳过");
            this.lastTrigger = sl.getGameTime();      // 记冷却, 免得每一轮都试
            return;
        }
        clear();                                       // 同一时间只留一批
        int batches = BATCH_MIN;
        int want = MIN_TOTAL;
        int spawned = 0;
        for (int i = 0; i < want; i++) {
            try {
                Entity raw = type.create(sl);
                if (!(raw instanceof Mob m)) {
                    if (raw != null) raw.discard();
                    continue;
                }
                double angle = (Math.PI * 2.0 / want) * i + this.boss.getRandom().nextDouble() * 0.5;
                double r = 4.0 + this.boss.getRandom().nextDouble() * 3.0;
                /* v120 按用户「女仆防御还是有问题」: 先找一个**能站人**的落点 —— 以前直接按 Boss 的 Y 摆,
                   庄园在地底(y≈-62), 女仆经常被摆进岩石里, 20 tick 后 `在墙里窒息而亡`。 */
                net.minecraft.core.BlockPos spot = safeSpot(sl, this.boss.getX() + Math.cos(angle) * r,
                        this.boss.getZ() + Math.sin(angle) * r, this.boss.getY());
                if (spot == null) {                        // 周围都塞满了 -> 就站 Boss 脚下(那儿一定站得住)
                    m.moveTo(this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                            this.boss.getRandom().nextFloat() * 360.0f, 0.0f);
                } else {
                    m.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5,
                            this.boss.getRandom().nextFloat() * 360.0f, 0.0f);
                }
                m.addTag(GUARD_TAG);                   // ★ 只有带这个标签的才算护法
                m.getPersistentData().putBoolean(GUARD_KEY, true);   // v119: 再写一份持久化标记
                m.getPersistentData().putUUID(GUARD_OWNER, this.boss.getUUID());
                m.setCustomName(Component.literal("§b雪黎的女仆"));
                m.setCustomNameVisible(true);
                m.setPersistenceRequired();
                LivingEntity t = this.boss.getTarget();
                if (t != null) m.setTarget(t);
                net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this.boss, m);
                if (!sl.addFreshEntity(m)) continue;
                this.track(m);
                Fx.particles(sl, m.getX(), m.getY() + 0.9, m.getZ(), 16, ParticleTypes.HEART, 0.35, 0.04);
                Fx.particles(sl, m.getX(), m.getY() + 0.4, m.getZ(), 10, ParticleTypes.END_ROD, 0.3, 0.02);
                spawned++;
            } catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] 雪黎召唤女仆失败: {}", t.toString());
            }
        }
        this.lastTrigger = sl.getGameTime();
        if (spawned <= 0) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 雪黎女仆护法: 一位都没召出来");
            return;
        }
        this.shield = true;
        this.remindCd = 20;
        this.blockCd = 0;
        /* ★★★ v118 修「女仆没死护盾就失效了」★★★
           {@link #aliveMaids()} 带 10 tick 缓存, 而 trigger() 以前**没有让缓存失效** ——
           下一 tick 读到的还是"召唤之前"那份空列表 -> 立刻走了"尽数倒下, 庇护破除"分支。
           现在: ① 召唤完立刻让缓存失效; ② 再给 1 秒宽限, 这一秒内就算扫不到也不判全灭。 */
        this.graceTicks = 20;
        this.invalidateCache();
        this.summonTick = this.boss.level().getGameTime();
        Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BEACON_POWER_SELECT, 1.5f, 1.1f);
        Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BELL_BLOCK, 1.2f, 1.3f);
        Fx.ring(sl, this.boss.getX(), this.boss.getY() + 0.2, this.boss.getZ(), 5.5, 36, ParticleTypes.HEART, 0.03);
        broadcast("msg.ftboss.xueli_shield", spawned);
        this.boss.onXueliShieldActivated();
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎「女仆护法」启动: {} 批次 -> {} 位女仆(清空前完全免伤)",
                batches, spawned);
    }

    /**
     * v120: 给女仆找一个**能站人**的落点(脚下实心 + 身上两格空)。
     * 找不到就返回 null, 调用方退回"站 Boss 脚下"。
     */
    private static net.minecraft.core.BlockPos safeSpot(ServerLevel sl, double x, double z, double y) {
        for (int dy = 0; dy <= 3; dy++) {
            for (int sign = 0; sign <= 1; sign++) {
                if (dy == 0 && sign == 1) continue;
                double yy = y + (sign == 0 ? dy : -dy);
                var pos = net.minecraft.core.BlockPos.containing(x, yy, z);
                if (!sl.getBlockState(pos.below()).isSolid()) continue;
                if (!sl.isEmptyBlock(pos) || !sl.isEmptyBlock(pos.above())) continue;
                return pos;
            }
        }
        return null;
    }

    /**
     * v120 按用户「女仆防御还是有问题」: **护法只吃玩家的伤害**。
     *
     * <p>日志已经把两种死法都点名了: `雪黎的女仆在墙里窒息而亡`(地形) 与 `雪黎的女仆被杀死了`
     * (柠檬召来的助战食物 Boss 不走 {@code EasterBossEntity.isValidMobTarget}, 照样能砍她们)。
     * 现在除了玩家亲手打, 其它任何来源(窒息/岩浆/摔落/别的 Boss 与其召唤物)都取消 ——
     * 想破盾只能像设计的那样**把女仆清掉**。{@code /kill} 这类无视无敌的伤害仍然照旧生效。
     */
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onGuardHurt(net.minecraftforge.event.entity.living.LivingAttackEvent e) {
        try {
            if (!isGuard(e.getEntity())) return;
            var src = e.getSource();
            if (src.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) return;
            if (net.farmingtales.ftboss.entity.EasterBossEntity.isRealPlayerDamage(src)) return;
            e.setCanceled(true);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 护法**单次伤害上限**（2026-10-10 按用户要求「庄园boss无视效果：斩首」新增）。
     *
     * <p>女仆是第三方类型（{@code touhou_little_maid:maid}），没法像庄园召唤物那样换成自有类型、
     * 也就没法被加进 {@code #kaleidoscope_world_liquor:bosses} 免疫「斩首」
     * （该效果：攻击时有概率造成**双倍伤害**，若这次击杀则必掉头颅）。
     * 上面那道钩子只让护法"只吃玩家伤害"，玩家自己带的斩首双倍照样能一击抹掉她们 ——
     * 所以这里再加一道**单跳上限**：一次最多打掉 {@value #GUARD_HIT_CAP_PERCENT}% 最大生命。
     * 斩首/其它爆发武器因此无法一击带走护法，但正常输出节奏几乎不受影响。
     */
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onGuardHurtAmount(net.minecraftforge.event.entity.living.LivingHurtEvent e) {
        try {
            if (!isGuard(e.getEntity())) return;
            float cap = e.getEntity().getMaxHealth() * GUARD_HIT_CAP;
            if (e.getAmount() > cap) {
                FTBossMod.LOGGER.info("[FTBoss][Easter] 女仆护法单跳上限生效: {} -> {} (上限 {}%)",
                        e.getAmount(), cap, GUARD_HIT_CAP_PERCENT);
                e.setAmount(cap);
            }
        } catch (Throwable ignored) {
        }
    }

    /** 每 tick 调用(仅雪黎) */
    public void tick() {
        if (this.boss.isDeadOrDying()) return;
        this.tickSummoning();
        if (this.boss.tickCount % 10 == 0) this.followMaids();

        if (!this.shield) {
            /* 区块重载后重新接管: 场上还有护法 -> 恢复免伤 */
            if (this.boss.level() instanceof ServerLevel && aliveMaids() > 0) {
                this.shield = true;
                FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎女仆护法重新接管({} 位)", aliveMaids());
            }
            return;
        }

        if (this.graceTicks > 0) this.graceTicks--;
        int alive = aliveMaids();
        if (alive <= 0 && this.graceTicks > 0) return;      // v118: 刚召出来先给 1 秒宽限
        if (alive <= 0) {
            this.shield = false;
            if (this.boss.level() instanceof ServerLevel sl) {
                Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.BEACON_DEACTIVATE, 1.5f, 1.2f);
                Fx.sound(sl, this.boss.getX(), this.boss.getY(), this.boss.getZ(), SoundEvents.GLASS_BREAK, 1.4f, 1.1f);
                Fx.particles(sl, this.boss.getX(), this.boss.getY() + 1.4, this.boss.getZ(), 45, ParticleTypes.CRIT, 0.9, 0.12);
                broadcast("msg.ftboss.xueli_shield_break");
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎女仆护法尽数倒下, 庇护破除");
            long lived = this.boss.level().getGameTime() - this.summonTick;
            if (lived < 100L) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] 女仆护法只撑了 {} tick 就全灭(异常!) —— "
                        + "正常情况下它们不会被自家 Boss 索敌(见 isValidMobTarget / XueliMaids.isGuard)",
                        lived);
            }
            return;
        }

        /* 护盾能量粒子 */
        if (this.boss.tickCount % 5 == 0) {
            Fx.dust(this.boss.level(), this.boss.getX(), this.boss.getY() + 1.1, this.boss.getZ(),
                    4, 0.35F, 0.70F, 0.95F, 1.0, 0.01);
        }
        /* 缓慢回血: 每位女仆每秒 HEAL_PERCENT_PER_MAID 最大生命 */
        if (this.boss.tickCount % 20 == 0) {
            double heal = this.boss.getMaxHealth() * HEAL_PERCENT_PER_MAID * alive;
            if (this.boss.getHealth() < this.boss.getMaxHealth()) {
                this.boss.heal((float) heal);
                if (this.boss.level() instanceof ServerLevel sl) {
                    Fx.particles(sl, this.boss.getX(), this.boss.getY() + 1.6, this.boss.getZ(),
                            6, ParticleTypes.HEART, 0.4, 0.03);
                }
            }
        }
        if (this.blockCd > 0) this.blockCd--;
        if (--this.remindCd <= 0) {
            this.remindCd = REMIND_CD;
            remind(alive);
        }
    }

    /** 被攻击但处于护盾状态: 给攻击者一个动作栏反馈(限频) */
    public void onBlocked(DamageSource source) {
        if (this.blockCd > 0) return;
        this.blockCd = BLOCK_NOTIFY_CD;
        if (source == null || !(source.getEntity() instanceof ServerPlayer p)) return;
        p.displayClientMessage(Component.translatable("msg.ftboss.xueli_shield_hit", aliveMaids()), true);
        Fx.sound(this.boss.level(), this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(),
                SoundEvents.SHIELD_BLOCK, 1.0f, 1.4f);
    }

    private void followMaids() {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        double sx = this.boss.getX(), sy = this.boss.getY(), sz = this.boss.getZ();
        for (Mob m : collect()) {
            boolean buried = !sl.noCollision(m);
            boolean gasping = m.getAirSupply() < m.getMaxAirSupply() - 20;
            boolean farAway = m.distanceToSqr(this.boss) > FOLLOW_DISTANCE * FOLLOW_DISTANCE;
            if (!buried && !gasping && !farAway) continue;
            double ang = this.boss.getRandom().nextDouble() * Math.PI * 2.0;
            var safe = net.farmingtales.ftboss.boss.ManorSafeTeleport.find(sl, m,
                    new net.minecraft.world.phys.Vec3(sx + Math.cos(ang) * 2.5, sy, sz + Math.sin(ang) * 2.5), 8);
            if (safe == null) continue;
            net.farmingtales.ftboss.boss.ManorSafeTeleport.move(sl, m, safe);
            this.tracked.get(m.getUUID()).position = m.blockPosition();
            m.setAirSupply(m.getMaxAirSupply());
            Fx.particles(sl, m.getX(), m.getY() + 0.9, m.getZ(), 10, ParticleTypes.PORTAL, 0.3, 0.05);
        }
        if (this.boss.tickCount % 20 == 0) {
            for (TrackedMaid guard : this.tracked.values()) {
                if (guard.entity == null || guard.entity.isRemoved()) {
                    sl.getChunk(guard.position.getX() >> 4, guard.position.getZ() >> 4);
                }
            }
        }
    }

    /** 死亡/退场清理: 收掉护法并解除护盾 */
    public void reset() {
        this.shield = false;
        this.lastTrigger = -1L;
        clear();
    }

    private void clear() {
        for (Mob m : collect()) m.discard();
        this.tracked.clear();
        this.invalidateCache();      // v121: 清完立刻让存活缓存失效(用 invalidateCache, 别再写 MIN_VALUE)
        this.graceTicks = 0;
    }

    private void remind(int alive) {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(PLAYER_RANGE))) {
            p.displayClientMessage(Component.translatable("msg.ftboss.xueli_shield_remind", alive), true);
        }
    }

    private void broadcast(String key, Object... args) {
        if (!(this.boss.level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, this.boss.getBoundingBox().inflate(PLAYER_RANGE))) {
            p.displayClientMessage(Component.translatable(key, args), false);
        }
    }

    public void save(CompoundTag tag) {
        collect();
        ListTag guards = new ListTag();
        this.tracked.forEach((id, guard) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", id);
            entry.putLong("Pos", guard.position.asLong());
            guards.add(entry);
        });
        tag.put("XueliGuards", guards);
        tag.putLong("XueliGuardLastTrigger", this.lastTrigger);
    }

    public void load(CompoundTag tag) {
        this.tracked.clear();
        for (Tag value : tag.getList("XueliGuards", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            if (entry.hasUUID("Id")) {
                this.tracked.put(entry.getUUID("Id"), new TrackedMaid(null, BlockPos.of(entry.getLong("Pos"))));
            }
        }
        this.shield = !this.tracked.isEmpty();
        this.lastTrigger = tag.contains("XueliGuardLastTrigger", Tag.TAG_LONG)
                ? tag.getLong("XueliGuardLastTrigger") : -1L;
        if (this.lastTrigger == 0L && this.tracked.isEmpty()) this.lastTrigger = -1L;
        this.discoveryAt = -1L;
        this.invalidateCache();
    }

    private boolean belongsToBoss(Mob maid) {
        UUID owner = maid.getPersistentData().hasUUID(GUARD_OWNER)
                ? maid.getPersistentData().getUUID(GUARD_OWNER) : ManorFightMembers.ownerId(maid);
        return isGuard(maid) && this.boss.getUUID().equals(owner);
    }

    private void track(Mob maid) {
        this.tracked.put(maid.getUUID(), new TrackedMaid(maid, maid.blockPosition()));
        this.invalidateCache();
    }

    private List<Mob> collect() {
        List<Mob> out = new ArrayList<>();
        if (!(this.boss.level() instanceof ServerLevel level)) return out;
        long now = level.getGameTime();
        if (this.discoveryAt < 0L || now - this.discoveryAt >= 100L) {
            this.discoveryAt = now;
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob maid && maid.isAlive() && belongsToBoss(maid)) {
                    this.track(maid);
                }
            }
        }
        var entries = this.tracked.entrySet().iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            TrackedMaid guard = entry.getValue();
            Entity resolved = level.getEntity(entry.getKey());
            if (resolved instanceof Mob maid && belongsToBoss(maid)) guard.entity = maid;
            Mob maid = guard.entity;
            if (maid == null) continue;
            if (maid.getHealth() <= 0.0f
                    || (maid.isRemoved() && maid.getRemovalReason().shouldDestroy())) {
                entries.remove();
                continue;
            }
            if (maid.isRemoved()) continue;
            guard.position = maid.blockPosition();
            out.add(maid);
        }
        return out;
    }
}
