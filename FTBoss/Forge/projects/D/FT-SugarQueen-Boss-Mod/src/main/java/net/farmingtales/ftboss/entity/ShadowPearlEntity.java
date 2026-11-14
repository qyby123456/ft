package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.item.weapons.BlackCleaverAndShadow;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


/**
 * 【裂瞳投珠】的暗影珍珠投射物（2026-09-15 第二版：恢复原技能设计）。
 *
 * 命中效果（用户定：与原「暗影剑」设计一致）：
 *   · 直接命中实体：8 点伤害（走 damageSources().thrown，受 PvP/友军与 owner 判定）
 *   · **命中后把使用者传送到命中点**（找安全落点、清零坠落距离、保留视角）—— 传送回来了 ✓
 *   · **落点范围伤害**：半径 {@link #AOE_RADIUS} 内除使用者外每个目标 {@link #AOE_DAMAGE} 点
 *     + 致盲 {@link #BLIND_TICKS} tick（与原 AOE 设计一致）
 *   · 命中方块：同样传送 + 在表面法线外侧出范围伤害与特效；**不破坏方块**
 *   · 不穿透：命中即 remove；40 tick 未命中自动消散（高延迟兜底）
 *
 * 高速命中：直接复用原版 Projectile 的**扫掠线段**判定（方块 ClipContext / 实体扩展 AABB），
 *   不是"只测每 tick 落点"，因此不会穿过薄墙或小型移动实体 ✓
 *
 * 渲染占位：仍用原版末影珍珠物品外观（ThrownItemRenderer），等 shadow_pearl.png 到位再换。
 */
public class ShadowPearlEntity extends ThrowableItemProjectile {

    /** 直接命中伤害 8 点 = 4 颗心。 */
    public static final float DAMAGE = 8.0F;
    /** 落点范围伤害（与原 ShadowSword 的 AOE 一致）。 */
    public static final float AOE_DAMAGE = BlackCleaverAndShadow.ShadowSword.AOE_DAMAGE;
    public static final double AOE_RADIUS = BlackCleaverAndShadow.ShadowSword.AOE_RADIUS;
    public static final int BLIND_TICKS = BlackCleaverAndShadow.ShadowSword.BLIND_TICKS;
    /** 最大飞行时间 40 tick。 */
    public static final int MAX_LIFE = 40;
    private static final int TRAIL_MAX_PER_TICK = 2;

    private int life;
    private Vec3 lastPos;

    public ShadowPearlEntity(EntityType<? extends ShadowPearlEntity> type, Level level) {
        super(type, level);
    }

    public ShadowPearlEntity(Level level, LivingEntity shooter) {
        super(FTEntities.SHADOW_PEARL.get(), shooter, level);
    }

    /* 注：本类**不注册属性表**，也注册不了 —— EntityAttributeCreationEvent.put 只收
       EntityType<? extends LivingEntity>，而珍珠是 ThrowableItemProjectile。
       启动日志里那句 "Entity ftboss:shadow_pearl has no attributes"（一次启动 378 行）
       的根因不在属性，而在注册时的 MobCategory：原版 DefaultAttributes.validate()
       先 filter(type -> type.getCategory() != MobCategory.MISC) 再查属性表，
       珍珠以前套的是 MONSTER 重载，所以被当成"没属性表的怪"点了名。
       修法在 FTEntities.SHADOW_PEARL —— 改成 MobCategory.MISC 即可。 */

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;   // 占位外观
    }

    @Override
    protected float getGravity() {
        return 0.0F;                // 无重力直线
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnTrail();
        }
        lastPos = position();
        if (!level().isClientSide && ++life > MAX_LIFE) {
            impact(position(), false);
        }
    }

    private void spawnTrail() {
        if (lastPos == null) {
            return;
        }
        double dist = lastPos.distanceTo(position());
        int n = (int) Math.min(TRAIL_MAX_PER_PICK(), Math.max(1, Math.ceil(dist / 0.25D)));
        for (int i = 0; i < n; i++) {
            double f = (i + 1.0D) / (n + 1.0D);
            level().addParticle(ParticleTypes.PORTAL,
                    lastPos.x + (getX() - lastPos.x) * f,
                    lastPos.y + (getY() - lastPos.y) * f,
                    lastPos.z + (getZ() - lastPos.z) * f, 0.0D, 0.0D, 0.0D);
        }
    }

    private static int TRAIL_MAX_PER_PICK() {
        return TRAIL_MAX_PER_TICK;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = getOwner();
        if (target == owner) {
            return;
        }
        if (owner instanceof Player shooter && target instanceof Player victim && !shooter.canHarmPlayer(victim)) {
            return;
        }
        target.hurt(level().damageSources().thrown(this, owner == null ? this : owner), DAMAGE);
        impact(result.getLocation(), true);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide) {
            return;
        }
        Vec3 n = Vec3.atLowerCornerOf(result.getDirection().getNormal());
        impact(result.getLocation().add(n.scale(0.12D)), true);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide && result.getType() == HitResult.Type.ENTITY && !isRemoved()) {
            impact(position(), true);
        }
    }

    /**
     * 这颗珍珠的投掷者是否正处在蛋蛋的末影封锁里。
     *
     * <p>单独一个方法，是因为 {@link #impact} 里**必须**在
     * {@code ShadowSwordItem.teleportStrike}（它直接调 {@code ServerPlayer.teleportTo}）
     * 之前就问过 —— 那次传送不触发 {@code EntityTeleportEvent.EnderPearl}，
     * 原版那条钩子根本看不到它。
     */
    private boolean sealed() {
        return getOwner() instanceof ServerPlayer owner
                && net.farmingtales.ftboss.boss.ai.DandanPearlBan.blockLanding(owner, "裂瞳投珠 · 末影珍珠");
    }

    /**
     * 命中结算：范围伤害 + 致盲 + 特效/音效 + **把使用者传送过来** + 自身移除。
     *
     * <p>★ 2026-09-16 修掉一个**重复结算**的 bug：本方法原本先自己算一遍
     * {@code AOE_RADIUS} 内的伤害+致盲，第 170 行又调 {@code ShadowSwordItem.teleportStrike}，
     * 而后者**内部还会再算一遍**（同样的圆柱判定 + 同样的致盲/击退/冲击波）。
     * 于是落点内的生物吃 **2 倍伤害**，而且两套半径不同（3.0 vs SKY_RADIUS）会出现
     * "3.0 内吃两次、3.0~AOE_RADIUS 只吃一次"的错位。
     *
     * <p>现在：**使用者是玩家时，以 {@code teleportStrike} 为唯一结算点**（它更完整 ——
     * 圆柱判定 + 击退 + 三层冲击波 + 落脚点搜索），本方法不再重复算。
     * 使用者不是玩家（或已消失）时，退回本方法自己的范围伤害，保证珍珠仍有伤害。
     */
    private void impact(Vec3 at, boolean withSound) {
        Level lv = level();
        Entity owner = getOwner();

        /* ★ 2026-09-18 补：蛋蛋的末影封锁对这颗珍珠**完全无效** ——
           它继承 ThrowableItemProjectile（不是原版 ThrownEnderpearl），
           落地传送走的是 ShadowSwordItem.teleportStrike 里的 ServerPlayer.teleportTo，
           既不触发 EntityTeleportEvent.EnderPearl、也不经过任何"手里拿珍珠"的判定，
           所以 DandanPearlBan 那两条钩子一条都拦不到它。
           这里显式问一次：封锁内就整颗作废（不传送、不结算范围伤害），
           与"原版珍珠在封锁里被取消 -> 直接 discard"的表现一致。 */
        if (sealed()) {
            discard();
            return;
        }

        /* 1~2) 结算范围伤害 */
        boolean delegated = false;

        if (owner instanceof ServerPlayer sp && sp.isAlive()) {
            /* 【暗影传送】(v2 2026-09-16 按用户要求): 删掉"瞬移到 14 格高再砸下来"的下坠，
               改成**直接传送到落点、并在落点结算范围魔法伤害** —— 传送与结算是同一瞬间，
               没有滞空状态，那些"卡住 / 登出 / 换维度残留"的边角问题一并消失。 */
            net.farmingtales.ftboss.item.weapons.ShadowSwordItem.teleportStrike(sp, at.x, at.y, at.z);
            delegated = true;
        }

        if (!delegated) {
            /* 非玩家发射（或发射者已消失）：本方法自己结算，否则这颗珍珠就没有伤害了。
               注意这里**不带致盲之外的额外效果**，保持与旧行为一致。 */
            AABB box = new AABB(at.x - AOE_RADIUS, at.y - AOE_RADIUS, at.z - AOE_RADIUS,
                    at.x + AOE_RADIUS, at.y + AOE_RADIUS, at.z + AOE_RADIUS);
            DamageSource src = lv.damageSources().thrown(this, owner == null ? this : owner);

            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, box)) {
                if (e == owner) {
                    continue;
                }
                if (owner instanceof Player shooter && e instanceof Player victim
                        && !shooter.canHarmPlayer(victim)) {
                    continue;
                }
                e.hurt(src, AOE_DAMAGE);
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TICKS, 0));
            }
        }

        /* 3) 珍珠自身的命中特效（落点那套冲击波由结算方负责，这里只补"珍珠碎开"） */
        for (int i = 0; i < 10; i++) {
            double a = random.nextDouble() * Math.PI * 2.0D;
            double b = (random.nextDouble() - 0.5D) * Math.PI;
            double s = 0.12D + random.nextDouble() * 0.18D;
            lv.addParticle(ParticleTypes.SQUID_INK, at.x, at.y, at.z,
                    Math.cos(a) * Math.cos(b) * s, Math.sin(b) * s * 0.6D, Math.sin(a) * Math.cos(b) * s);
        }
        lv.addParticle(ParticleTypes.FLASH, at.x, at.y, at.z, 0.0D, 0.0D, 0.0D);
        if (withSound) {
            lv.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.9F, 0.7F);
        }
        discard();
    }
}
