package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 【雪黎的禁疗飞刀】(2026-09-13 v103 按用户要求「雪黎现在可以投掷禁疗飞刀」)。
 *
 * <p>外观 = 农夫乐事的刀({@code farmersdelight:diamond_knife} → 铁刀 → 燧石刀 → 铁剑 兜底)。
 * 命中玩家: 少量伤害 + **{@link net.farmingtales.ftboss.boss.ai.HealBan} 禁疗 30 秒**
 * (复用 v96 麻雀那套: {@code relics:anti_heal} 当可见标记 + 拦 {@code LivingHealEvent})。
 * 打空(落地/超时)只留下一把插在地上的刀和火花, 不给任何人上禁疗。
 */
public class XueliKnifeEntity extends ThrowableItemProjectile {

    /** 命中伤害(单次, 已经含全局修正) —— v116 按用户要求「每次伤害 12 点」(原来是 6 × 4/3 = 8) */
    public static final float KNIFE_DAMAGE = 12.0f;
    /** v116 按用户要求「飞刀现在会造成禁疗和流血」: 流血持续 8 秒 */
    public static final int BLEED_TICKS = 8 * 20;
    /** 擦身判定半径(直接命中实体之外, 落点这么近的玩家也算挨刀) */
    public static final double NEAR_RADIUS = 1.6;
    /** 寿命上限(6 秒), 防止一直飞 */
    private static final int MAX_LIFE = 120;

    public XueliKnifeEntity(EntityType<? extends XueliKnifeEntity> type, Level level) {
        super(type, level);
    }

    public XueliKnifeEntity(Level level, LivingEntity shooter) {
        super(FTEntities.XUELI_KNIFE.get(), shooter, level);
        this.setItem(new ItemStack(this.getDefaultItem()));
    }

    /** 农夫乐事的刀, 取不到就退回铁剑 */
    @Override
    protected Item getDefaultItem() {
        try {
            for (String id : new String[]{
                    "farmersdelight:diamond_knife", "farmersdelight:iron_knife",
                    "farmersdelight:flint_knife", "farmersdelight:netherite_knife"}) {
                Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                if (it != null && it != Items.AIR) return it;
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 取飞刀物品失败: {}", t.toString());
        }
        return Items.IRON_SWORD;
    }

    @Override
    public void tick() {
        Vec3 start = this.position();
        super.tick();
        if (this.level().isClientSide || this.isRemoved()) return;
        this.trail(start, this.position());
        if (this.tickCount > MAX_LIFE || this.isInWater()) this.strike(null);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof Player player && this.combatPlayer(player) && super.canHitEntity(entity);
    }

    private boolean combatPlayer(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private void trail(Vec3 start, Vec3 end) {
        if (!(this.level() instanceof ServerLevel level)) return;
        int steps = Math.min(8, (int) Math.ceil(start.distanceTo(end) / 0.35));
        for (int step = 1; step <= steps; step++) {
            Vec3 point = start.lerp(end, (double) step / steps);
            level.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (this.level().isClientSide) return;
        this.strike(hit.getEntity());
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide || this.isRemoved()) return;
        this.trail(this.position(), result.getLocation());
        this.setPos(result.getLocation());
        if (result.getType() == HitResult.Type.ENTITY) this.onHitEntity((EntityHitResult) result);
        else this.strike(null);
    }

    /** 结算: direct 是直接命中的实体(direct==null 表示打空 -> 只做视觉) */
    private void strike(Entity direct) {
        if (this.isRemoved()) return;
        if (!(this.level() instanceof ServerLevel sl)) { this.discard(); return; }
        double x = this.getX(), y = this.getY(), z = this.getZ();
        int hits = 0;
        if (direct instanceof Player p && this.combatPlayer(p)) {
            this.cut(p, false);
            hits++;
        } else if (direct == null) {
            /* 擦身: 落点 1.6 格内的玩家也算挨刀(飞刀细, 直接命中判定本来就窄) */
            for (Player p : sl.players()) {
                if (!this.combatPlayer(p)) continue;
                if (p.distanceToSqr(x, y, z) > NEAR_RADIUS * NEAR_RADIUS) continue;
                this.cut(p, true);
                hits++;
            }
        }
        sl.playSound(null, x, y, z, SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 0.9f, 1.7f);
        sl.playSound(null, x, y, z, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.HOSTILE, 0.7f, 1.4f);
        Fx.particles(sl, x, y + 0.2, z, 8, ParticleTypes.CRIT, 0.6, 0.06);
        Fx.particles(sl, x, y + 0.2, z, 6, ParticleTypes.ENCHANTED_HIT, 0.5, 0.04);
        if (hits > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎禁疗飞刀命中 {} 人 ({} 伤害 + 禁疗 {} 秒)",
                    hits, String.format("%.1f", KNIFE_DAMAGE),
                    net.farmingtales.ftboss.boss.ai.HealBan.BAN_TICKS / 20);
        }
        this.discard();
    }

    /** 割一刀: 伤害 + 禁疗 + 流血 */
    private void cut(Player p, boolean glancing) {
        p.invulnerableTime = 0;
        p.hurt(this.damageSources().thrown(this, this.getOwner()), glancing ? KNIFE_DAMAGE * 0.6f : KNIFE_DAMAGE);
        EasterBossEntity.applyXueliFrost(p);                     // v105: 细雪飞刀也带细雪
        net.farmingtales.ftboss.boss.ai.CombatStatusHooks.xueliHit(p, "knife");
        if (p instanceof ServerPlayer sp) {
            net.farmingtales.ftboss.boss.ai.HealBan.ban(sp, "雪黎的飞刀");
            /* v116: 追加流血; v119: 改用共享的 ExtraDebuffs(朵拉的命中流血也走它) */
            net.farmingtales.ftboss.boss.ai.ExtraDebuffs.bleed(p, BLEED_TICKS, 1);
        }
    }

    @Override
    protected float getGravity() { return 0.03f; }
}
