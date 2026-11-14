/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.combat.xueli.XueliCombatServer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class XueliMaids {
    public static final String GUARD_TAG = "ftboss_xueli_guard";
    public static final String GUARD_KEY = "ftboss_xueli_guard";
    private static final String GUARD_OWNER = "ftboss_xueli_guard_owner";
    public static final int BATCH_MIN = 3;
    public static final int BATCH_MAX = 3;
    public static final int PER_BATCH = 2;
    public static final int MIN_TOTAL = 6;
    public static final int MAX_TOTAL = 6;
    public static final int TRIGGER_CD = 2400;
    public static final double HEAL_PERCENT_PER_MAID = 0.001;
    private static final double FOLLOW_DISTANCE = 16.0;
    private static final double PLAYER_RANGE = 48.0;
    private static final int REMIND_CD = 100;
    private static final int BLOCK_NOTIFY_CD = 40;
    private final EasterBossEntity boss;
    private final Map<UUID, TrackedMaid> tracked = new HashMap<UUID, TrackedMaid>();
    private long discoveryAt = -1L;
    private boolean shield = false;
    private int remindCd = 20;
    private int blockCd = 0;
    private int graceTicks = 0;
    private long summonTick = 0L;
    private long lastTrigger = -1L;
    private long cacheAt = -1L;

    public XueliMaids(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    public boolean shielded() {
        return this.shield;
    }

    public static boolean isGuard(Entity e) {
        if (e == null) {
            return false;
        }
        if (e.m_19880_().contains("ftboss_xueli_guard")) {
            return true;
        }
        try {
            return e.getPersistentData().m_128471_("ftboss_xueli_guard");
        }
        catch (Throwable t) {
            return false;
        }
    }

    private void invalidateCache() {
        this.cacheAt = -1L;
    }

    public int aliveMaids() {
        long now = this.boss.m_9236_().m_46467_();
        if (this.cacheAt < 0L || now - this.cacheAt >= 10L) {
            this.cacheAt = now;
            this.collect();
        }
        return this.tracked.size();
    }

    public boolean canTrigger() {
        long l = this.boss.m_9236_().m_46467_();
        return this.lastTrigger >= 0L && l >= this.lastTrigger && l - this.lastTrigger >= 2400L;
    }

    private static EntityType<?> maidType() {
        try {
            return (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("touhou_little_maid", "maid"));
        }
        catch (Throwable t) {
            return null;
        }
    }

    public void trigger() {
        if (!this.canTrigger()) {
            return;
        }
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            XueliCombatServer.maids(this.boss, this.shield);
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        EntityType<?> type = XueliMaids.maidType();
        if (type == null) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u96ea\u9ece\u5973\u4ec6\u62a4\u6cd5: \u6ca1\u627e\u5230 touhou_little_maid:maid, \u672c\u6b21\u8df3\u8fc7");
            this.lastTrigger = sl.m_46467_();
            XueliCombatServer.maids(this.boss, this.shield);
            return;
        }
        this.clear();
        int batches = 3;
        int want = 6;
        int spawned = 0;
        for (int i = 0; i < want; ++i) {
            try {
                Entity raw = type.m_20615_((Level)sl);
                if (!(raw instanceof Mob)) {
                    if (raw == null) continue;
                    raw.m_146870_();
                    continue;
                }
                Mob m = (Mob)raw;
                double angle = Math.PI * 2 / (double)want * (double)i + this.boss.m_217043_().m_188500_() * 0.5;
                double r = 4.0 + this.boss.m_217043_().m_188500_() * 3.0;
                BlockPos spot = XueliMaids.safeSpot(sl, this.boss.m_20185_() + Math.cos(angle) * r, this.boss.m_20189_() + Math.sin(angle) * r, this.boss.m_20186_());
                if (spot == null) {
                    m.m_7678_(this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), this.boss.m_217043_().m_188501_() * 360.0f, 0.0f);
                } else {
                    m.m_7678_((double)spot.m_123341_() + 0.5, (double)spot.m_123342_(), (double)spot.m_123343_() + 0.5, this.boss.m_217043_().m_188501_() * 360.0f, 0.0f);
                }
                m.m_20049_("ftboss_xueli_guard");
                m.getPersistentData().m_128379_("ftboss_xueli_guard", true);
                m.getPersistentData().m_128362_(GUARD_OWNER, this.boss.m_20148_());
                m.m_6593_((Component)Component.m_237113_((String)"\u00a7b\u96ea\u9ece\u7684\u5973\u4ec6"));
                m.m_20340_(true);
                m.m_21530_();
                LivingEntity t = this.boss.m_5448_();
                if (t != null) {
                    m.m_6710_(t);
                }
                ManorFightMembers.inherit((Entity)this.boss, (Entity)m);
                if (!sl.m_7967_((Entity)m)) continue;
                this.track(m);
                Fx.particles((Level)sl, m.m_20185_(), m.m_20186_() + 0.9, m.m_20189_(), 16, (ParticleOptions)ParticleTypes.f_123750_, 0.35, 0.04);
                Fx.particles((Level)sl, m.m_20185_(), m.m_20186_() + 0.4, m.m_20189_(), 10, (ParticleOptions)ParticleTypes.f_123810_, 0.3, 0.02);
                ++spawned;
                continue;
            }
            catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u96ea\u9ece\u53ec\u5524\u5973\u4ec6\u5931\u8d25: {}", (Object)t.toString());
            }
        }
        this.lastTrigger = sl.m_46467_();
        if (spawned <= 0) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u96ea\u9ece\u5973\u4ec6\u62a4\u6cd5: \u4e00\u4f4d\u90fd\u6ca1\u53ec\u51fa\u6765");
            XueliCombatServer.maids(this.boss, this.shield);
            return;
        }
        this.shield = true;
        this.remindCd = 20;
        this.blockCd = 0;
        this.graceTicks = 20;
        this.invalidateCache();
        this.summonTick = this.boss.m_9236_().m_46467_();
        Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11739_, 1.5f, 1.1f);
        Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11699_, 1.2f, 1.3f);
        Fx.ring((Level)sl, this.boss.m_20185_(), this.boss.m_20186_() + 0.2, this.boss.m_20189_(), 5.5, 36, (ParticleOptions)ParticleTypes.f_123750_, 0.03);
        this.broadcast("msg.ftboss.xueli_shield", spawned);
        this.boss.onXueliShieldActivated();
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u5973\u4ec6\u62a4\u6cd5\u300d\u542f\u52a8: {} \u6279\u6b21 -> {} \u4f4d\u5973\u4ec6(\u6e05\u7a7a\u524d\u5b8c\u5168\u514d\u4f24)", (Object)batches, (Object)spawned);
        XueliCombatServer.maids(this.boss, this.shield);
    }

    private static BlockPos safeSpot(ServerLevel sl, double x, double z, double y) {
        for (int dy = 0; dy <= 3; ++dy) {
            for (int sign = 0; sign <= 1; ++sign) {
                double yy;
                BlockPos pos;
                if (dy == 0 && sign == 1 || !sl.m_8055_((pos = BlockPos.m_274561_((double)x, (double)(yy = y + (double)(sign == 0 ? dy : -dy)), (double)z)).m_7495_()).m_280296_() || !sl.m_46859_(pos) || !sl.m_46859_(pos.m_7494_())) continue;
                return pos;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onGuardHurt(LivingAttackEvent e) {
        try {
            if (!XueliMaids.isGuard((Entity)e.getEntity())) {
                return;
            }
            DamageSource src = e.getSource();
            if (src.m_276093_(DamageTypes.f_286979_)) {
                return;
            }
            if (EasterBossEntity.isRealPlayerDamage(src)) {
                return;
            }
            e.setCanceled(true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void tick() {
        int alive;
        if (this.boss.m_21224_()) {
            XueliCombatServer.maids(this.boss, this.shield);
            return;
        }
        this.tickSummoning();
        if (this.boss.f_19797_ % 10 == 0) {
            this.followMaids();
        }
        if (!this.shield) {
            if (this.boss.m_9236_() instanceof ServerLevel && this.aliveMaids() > 0) {
                this.shield = true;
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u5973\u4ec6\u62a4\u6cd5\u91cd\u65b0\u63a5\u7ba1({} \u4f4d)", (Object)this.aliveMaids());
            }
            XueliCombatServer.maids(this.boss, this.shield);
            return;
        }
        if (this.graceTicks > 0) {
            --this.graceTicks;
        }
        if ((alive = this.aliveMaids()) <= 0 && this.graceTicks > 0) {
            XueliCombatServer.maids(this.boss, this.shield);
            return;
        }
        if (alive <= 0) {
            this.shield = false;
            Level level = this.boss.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11738_, 1.5f, 1.2f);
                Fx.sound((Level)sl, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11983_, 1.4f, 1.1f);
                Fx.particles((Level)sl, this.boss.m_20185_(), this.boss.m_20186_() + 1.4, this.boss.m_20189_(), 45, (ParticleOptions)ParticleTypes.f_123797_, 0.9, 0.12);
                this.broadcast("msg.ftboss.xueli_shield_break", new Object[0]);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u5973\u4ec6\u62a4\u6cd5\u5c3d\u6570\u5012\u4e0b, \u5e87\u62a4\u7834\u9664");
            long lived = this.boss.m_9236_().m_46467_() - this.summonTick;
            if (lived < 100L) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u5973\u4ec6\u62a4\u6cd5\u53ea\u6491\u4e86 {} tick \u5c31\u5168\u706d(\u5f02\u5e38!) \u2014\u2014 \u6b63\u5e38\u60c5\u51b5\u4e0b\u5b83\u4eec\u4e0d\u4f1a\u88ab\u81ea\u5bb6 Boss \u7d22\u654c(\u89c1 isValidMobTarget / XueliMaids.isGuard)", (Object)lived);
            }
            XueliCombatServer.maids(this.boss, this.shield);
            return;
        }
        if (this.boss.f_19797_ % 5 == 0) {
            Fx.dust(this.boss.m_9236_(), this.boss.m_20185_(), this.boss.m_20186_() + 1.1, this.boss.m_20189_(), 4, 0.35f, 0.7f, 0.95f, 1.0, 0.01);
        }
        if (this.boss.f_19797_ % 20 == 0) {
            double heal = (double)this.boss.m_21233_() * 0.001 * (double)alive;
            if (this.boss.m_21223_() < this.boss.m_21233_()) {
                this.boss.m_5634_((float)heal);
                Level level = this.boss.m_9236_();
                if (level instanceof ServerLevel) {
                    ServerLevel sl = (ServerLevel)level;
                    Fx.particles((Level)sl, this.boss.m_20185_(), this.boss.m_20186_() + 1.6, this.boss.m_20189_(), 6, (ParticleOptions)ParticleTypes.f_123750_, 0.4, 0.03);
                }
            }
        }
        if (this.blockCd > 0) {
            --this.blockCd;
        }
        if (--this.remindCd <= 0) {
            this.remindCd = 100;
            this.remind(alive);
        }
        XueliCombatServer.maids(this.boss, this.shield);
    }

    /*
     * WARNING - void declaration
     */
    public void onBlocked(DamageSource damageSource) {
        Entity entity;
        void source;
        XueliCombatServer.blocked(this.boss, damageSource, this.shield);
        if (this.blockCd > 0) {
            return;
        }
        this.blockCd = 40;
        if (source == null || !((entity = source.m_7639_()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer p = (ServerPlayer)entity;
        Component.m_237110_((String)"msg.ftboss.xueli_shield_hit", (Object[])new Object[]{this.aliveMaids()});
        Fx.sound(this.boss.m_9236_(), this.boss.m_20185_(), this.boss.m_20186_() + 1.0, this.boss.m_20189_(), SoundEvents.f_12346_, 1.0f, 1.4f);
    }

    private void followMaids() {
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        double sx = this.boss.m_20185_();
        double sy = this.boss.m_20186_();
        double sz = this.boss.m_20189_();
        for (Mob m : this.collect()) {
            double ang;
            Vec3 safe;
            boolean farAway;
            boolean buried = !sl.m_45786_((Entity)m);
            boolean gasping = m.m_20146_() < m.m_6062_() - 20;
            boolean bl = farAway = m.m_20280_((Entity)this.boss) > 256.0;
            if (!buried && !gasping && !farAway || (safe = ManorSafeTeleport.find(sl, (Entity)m, new Vec3(sx + Math.cos(ang = this.boss.m_217043_().m_188500_() * Math.PI * 2.0) * 2.5, sy, sz + Math.sin(ang) * 2.5), 8)) == null) continue;
            ManorSafeTeleport.move(sl, (Entity)m, safe);
            this.tracked.get((Object)m.m_20148_()).position = m.m_20183_();
            m.m_20301_(m.m_6062_());
            Fx.particles((Level)sl, m.m_20185_(), m.m_20186_() + 0.9, m.m_20189_(), 10, (ParticleOptions)ParticleTypes.f_123760_, 0.3, 0.05);
        }
        if (this.boss.f_19797_ % 20 == 0) {
            for (TrackedMaid guard : this.tracked.values()) {
                if (guard.entity != null && !guard.entity.m_213877_()) continue;
                sl.m_6325_(guard.position.m_123341_() >> 4, guard.position.m_123343_() >> 4);
            }
        }
    }

    public void reset() {
        this.shield = false;
        this.lastTrigger = -1L;
        this.clear();
        XueliCombatServer.maids(this.boss, this.shield);
    }

    private void clear() {
        for (Mob m : this.collect()) {
            m.m_146870_();
        }
        this.tracked.clear();
        this.invalidateCache();
        this.graceTicks = 0;
    }

    private void remind(int n) {
    }

    private void broadcast(String string, Object ... objectArray) {
    }

    public void save(CompoundTag tag) {
        this.collect();
        ListTag guards = new ListTag();
        this.tracked.forEach((id, guard) -> {
            CompoundTag entry = new CompoundTag();
            entry.m_128362_("Id", id);
            entry.m_128356_("Pos", guard.position.m_121878_());
            guards.add((Object)entry);
        });
        tag.m_128365_("XueliGuards", (Tag)guards);
        tag.m_128356_("XueliGuardLastTrigger", this.lastTrigger);
    }

    public void load(CompoundTag compoundTag) {
        this.tracked.clear();
        for (Tag tag : compoundTag.m_128437_("XueliGuards", 10)) {
            CompoundTag compoundTag2 = (CompoundTag)tag;
            if (!compoundTag2.m_128403_("Id")) continue;
            this.tracked.put(compoundTag2.m_128342_("Id"), new TrackedMaid(null, BlockPos.m_122022_((long)compoundTag2.m_128454_("Pos"))));
        }
        this.shield = !this.tracked.isEmpty();
        long l = this.lastTrigger = compoundTag.m_128425_("XueliGuardLastTrigger", 4) ? compoundTag.m_128454_("XueliGuardLastTrigger") : -1L;
        if (this.lastTrigger == 0L && this.tracked.isEmpty()) {
            this.lastTrigger = -1L;
        }
        this.discoveryAt = -1L;
        this.invalidateCache();
    }

    private boolean belongsToBoss(Mob maid) {
        UUID owner = maid.getPersistentData().m_128403_(GUARD_OWNER) ? maid.getPersistentData().m_128342_(GUARD_OWNER) : ManorFightMembers.ownerId((Entity)maid);
        return XueliMaids.isGuard((Entity)maid) && this.boss.m_20148_().equals(owner);
    }

    private void track(Mob maid) {
        this.tracked.put(maid.m_20148_(), new TrackedMaid(maid, maid.m_20183_()));
        this.invalidateCache();
    }

    private List<Mob> collect() {
        ArrayList<Mob> out = new ArrayList<Mob>();
        Level level = this.boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return out;
        }
        ServerLevel level2 = (ServerLevel)level;
        long now = level2.m_46467_();
        if (this.discoveryAt < 0L || now - this.discoveryAt >= 100L) {
            this.discoveryAt = now;
            for (Entity entity : level2.m_8583_()) {
                Mob maid;
                if (!(entity instanceof Mob) || !(maid = (Mob)entity).m_6084_() || !this.belongsToBoss(maid)) continue;
                this.track(maid);
            }
        }
        Iterator<Map.Entry<UUID, TrackedMaid>> entries = this.tracked.entrySet().iterator();
        while (entries.hasNext()) {
            Mob maid;
            Map.Entry<UUID, TrackedMaid> entry = entries.next();
            TrackedMaid guard = entry.getValue();
            Entity resolved = level2.m_8791_(entry.getKey());
            if (resolved instanceof Mob && this.belongsToBoss(maid = (Mob)resolved)) {
                guard.entity = maid;
            }
            if ((maid = guard.entity) == null) continue;
            if (maid.m_21223_() <= 0.0f || maid.m_213877_() && maid.m_146911_().m_146965_()) {
                entries.remove();
                continue;
            }
            if (maid.m_213877_()) continue;
            guard.position = maid.m_20183_();
            out.add(maid);
        }
        return out;
    }

    private void tickSummoning() {
        if (!(this.boss.m_9236_() instanceof ServerLevel) || this.boss.m_21525_() || this.boss.isMini()) {
            return;
        }
        LivingEntity livingEntity = this.boss.m_5448_();
        if (livingEntity == null || !livingEntity.m_6084_()) {
            return;
        }
        long l = this.boss.m_9236_().m_46467_();
        if (this.lastTrigger < 0L || l < this.lastTrigger) {
            this.lastTrigger = l;
            return;
        }
        if (this.canTrigger()) {
            this.trigger();
        }
    }

    private static final class TrackedMaid {
        private Mob entity;
        private BlockPos position;

        private TrackedMaid(Mob entity, BlockPos position) {
            this.entity = entity;
            this.position = position;
        }
    }
}

