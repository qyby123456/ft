package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 【召唤物随主消散】(2026-09-12 v58 按用户反馈「召唤物死不干净」)
 *
 * <p>问题: 有的 Boss 召唤物**没有寿命**, 主人死了它们还在场上追着玩家打:
 * <ul>
 *   <li>沙耶的茶芽小妖({@code TeaMinionEntity}) —— {@code setPersistenceRequired()}, 以前谁都不清;</li>
 *   <li>糖霜女皇的一波波小怪 —— 只有"结晶"被登记, 召唤物没登记;</li>
 *   <li>柚子(彩蛋)的迷你分身 —— 主人阵亡后还会留 5 分钟。</li>
 * </ul>
 *
 * <p>做法: Boss 在 {@code die()} / {@code remove()} / {@code cleanup()} 里调一次 {@link #clear},
 * 按**类型白名单**(以及带 {@code ftboss_summon} 标记的)清掉自己周围一定半径内的召唤物 ——
 * 用类型白名单而不是"清光附近所有小怪", 是为了**不误伤别人的小弟**
 * (比如庄园里柠檬猫喊来的食物系 Boss 自己也有召唤物)。
 */
public final class SummonSweep {

    /** 默认清理半径(格): 覆盖 Boss 的召唤范围, 又不至于扫到别的战场 */
    public static final double RANGE = 80.0;

    private SummonSweep() {}

    public static boolean belongsTo(Entity owner, Entity summon) {
        java.util.UUID actual = net.farmingtales.ftboss.boss.ManorFightMembers.ownerId(summon);
        if (LemonSacrifice.isInheritedSummon(summon) && !owner.getUUID().equals(actual)) return false;
        java.util.UUID root = net.farmingtales.ftboss.boss.ManorFightMembers.ownerId(owner);
        if (root == null) root = owner.getUUID();
        if (actual == null) actual = MinionBlessing.ownerId(summon);
        return actual == null || actual.equals(root) || actual.equals(owner.getUUID());
    }

    /**
     * 清掉 owner 附近指定类型的召唤物。
     *
     * @param types 属于这位 Boss 的召唤物类型(白名单)
     * @return 清掉的数量
     */
    public static int clear(LivingEntity owner, double range, Class<?>... types) {
        if (owner == null || owner.level().isClientSide) return 0;
        if (!(owner.level() instanceof ServerLevel sl)) return 0;
        int n = 0;
        for (Entity e : sl.getEntitiesOfClass(Entity.class, owner.getBoundingBox().inflate(range))) {
            if (e == owner || e.isRemoved()) continue;
            if (!belongsTo(owner, e)) continue;
            boolean match = false;
            for (Class<?> t : types) {
                if (t.isInstance(e)) { match = true; break; }
            }
            if (!match) continue;
            burst(sl, e);
            e.discard();
            n++;
        }
        report(owner, n, "召唤物");
        return n;
    }

    /** 清掉 owner 附近**带召唤物标记**的实体({@link MinionBlessing#mark} 打过标记的, 糖霜女皇的小怪走这条)。
     *
     *  <p>★★ v112 修复(用户/审计报"女皇清场误杀")：以前只判 {@code isSummon(e)}, 而它对**任何**
     *  {@link net.farmingtales.ftboss.entity.TeaMinionEntity} 都返回 true ——
     *  于是糖霜女皇死亡/主人下线/脱战清场时, 80 格内**沙耶的守护茶芽**(护盾核心, 一旦被清
     *  {@code SayaBuds} 就 shield=false)、鼠群小老鼠、屠夫学徒全被 discard 掉。
     *  现在**必须认主人**：{@code mark(e, ownerUuid)} 记过主人、且与本次清场者一致才清;
     *  没记过主人的(纯"继承增益"标记)一律不动 —— 宁可不杀, 也不误杀。
     */
    public static int clearMarked(LivingEntity owner, double range) {
        if (owner == null || owner.level().isClientSide) return 0;
        if (!(owner.level() instanceof ServerLevel sl)) return 0;
        int n = 0;
        for (Entity e : sl.getEntitiesOfClass(Entity.class, owner.getBoundingBox().inflate(range))) {
            if (e == owner || e.isRemoved()) continue;
            if (!MinionBlessing.isSummon(e)) continue;
            java.util.UUID oid = MinionBlessing.ownerId(e);                 // v112: 认主人
            if (oid == null || !oid.equals(owner.getUUID())) continue;
            burst(sl, e);
            e.discard();
            n++;
        }
        report(owner, n, "标记召唤物");
        return n;
    }

    /** v112: 清掉 owner 附近**所有由它自己召唤的**实体(按 {@link MinionBlessing#ownerId} 认主人, 不限类型)。
     *  比 {@link #clear} 的类型白名单更准 —— 答辩超人与故障机器人**共用同一个蛆类型**
     *  ({@code MaggotMinionEntity}), 按类型清会互相误杀。 */
    public static int clearOwned(LivingEntity owner, double range) {
        if (owner == null || owner.level().isClientSide) return 0;
        if (!(owner.level() instanceof ServerLevel sl)) return 0;
        int n = 0;
        for (Entity e : sl.getEntitiesOfClass(Entity.class, owner.getBoundingBox().inflate(range))) {
            if (e == owner || e.isRemoved()) continue;
            java.util.UUID oid = MinionBlessing.ownerId(e);
            if (oid == null || !oid.equals(owner.getUUID())) continue;
            burst(sl, e);
            e.discard();
            n++;
        }
        report(owner, n, "自有召唤物");
        return n;
    }

    private static void burst(ServerLevel sl, Entity e) {        sl.sendParticles(ParticleTypes.CLOUD, e.getX(), e.getY() + 0.6, e.getZ(), 14, 0.3, 0.4, 0.3, 0.05);
        sl.sendParticles(ParticleTypes.SMOKE, e.getX(), e.getY() + 0.4, e.getZ(), 8, 0.25, 0.3, 0.25, 0.03);
        sl.playSound(null, e.getX(), e.getY(), e.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.8f, 1.5f);
    }

    private static void report(LivingEntity owner, int n, String kind) {
        if (n <= 0) return;
        var key = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(owner.getType());
        FTBossMod.LOGGER.info("[FTBoss] {} 死亡/退场 -> 清掉 {} 个{}(随主消散)", key == null ? "Boss" : key, n, kind);
    }
}
