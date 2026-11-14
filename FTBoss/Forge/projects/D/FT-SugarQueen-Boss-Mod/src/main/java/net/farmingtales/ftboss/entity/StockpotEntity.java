package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 【雪黎的汤锅】(2026-09-13 v76 按用户要求新增)。
 *
 * <p>外观 = 森罗物语·烹饪的**汤锅**物品({@code kaleidoscope_cookery:stockpot}), 取不到就退回原版炼药锅。
 * 雪黎把它**整个扔出去**当远程攻击: 命中玩家/落地时"哐"一声, 对落点 3 格内的玩家造成
 * 一半物理 + 一半魔法的伤害(和飞轮同一套"无视 50% 护甲"的思路), 再撒一把汤水粒子。
 */
public class StockpotEntity extends ThrowableItemProjectile {

    /** 命中伤害(物+魔各一半) */
    public static final float POT_DAMAGE = net.farmingtales.ftboss.entity.EasterBossEntity.XUELI_POT_DAMAGE;
    /** 溅射半径 */
    public static final double SPLASH_RADIUS = 3.0;
    /** 寿命上限(10 秒), 防止一直飞 */
    private static final int MAX_LIFE = 200;

    public StockpotEntity(EntityType<? extends StockpotEntity> type, Level level) {
        super(type, level);
    }

    public StockpotEntity(Level level, LivingEntity shooter) {
        super(FTEntities.STOCKPOT.get(), shooter, level);
        this.setItem(new ItemStack(this.getDefaultItem()));
    }

    /** 汤锅(森罗物语·烹饪); 取不到退回原版炼药锅 */
    @Override
    protected Item getDefaultItem() {
        try {
            Item pot = ForgeRegistries.ITEMS.getValue(new ResourceLocation(
                    net.farmingtales.ftboss.entity.EasterBossEntity.XUELI_POT_ID));
            if (pot != null && pot != Items.AIR) return pot;
            Item alt = ForgeRegistries.ITEMS.getValue(new ResourceLocation("youkaisfeasts", "stockpot"));
            if (alt != null && alt != Items.AIR) return alt;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 取汤锅物品失败: {}", t.toString());
        }
        return Items.CAULDRON;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.tickCount > MAX_LIFE || this.isInWater()) this.splash(null);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (this.level().isClientSide) return;
        this.splash(hit.getEntity());
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide) return;
        if (result.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) result);
        } else {
            this.splash(null);
        }
    }

    /** 落地/命中: 半径内玩家吃"一半物理 + 一半魔法", 然后哐当一声碎掉 */
    private void splash(Entity direct) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel sl)) { this.discard(); return; }
        double x = this.getX(), y = this.getY(), z = this.getZ();
        float half = POT_DAMAGE * 0.5f;
        int hits = 0;
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(x, y, z) > SPLASH_RADIUS * SPLASH_RADIUS) continue;
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().thrown(this, this.getOwner()), half);        // 物理那半(吃护甲)
            p.invulnerableTime = 0;
            p.hurt(this.damageSources().indirectMagic(this, this.getOwner()), half); // 魔法那半(穿甲)
            EasterBossEntity.applyXueliFrost(p);                                     // v105: 汤锅也带细雪
            net.farmingtales.ftboss.boss.ai.CombatStatusHooks.xueliHit(p, "pot");
            hits++;
        }
        sl.playSound(null, x, y, z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2f, 0.6f);
        sl.playSound(null, x, y, z, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.HOSTILE, 1.0f, 0.5f);
        Fx.particles(sl, x, y + 0.2, z, 26, ParticleTypes.SPLASH, 1.0, 0.12);
        Fx.particles(sl, x, y + 0.2, z, 10, ParticleTypes.LARGE_SMOKE, 0.7, 0.05);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 雪黎汤锅落地: 命中 {} 人 ({} 伤害, 半径 {} 格)",
                hits, String.format("%.1f", POT_DAMAGE), (int) SPLASH_RADIUS);
        this.discard();
    }

    @Override
    protected float getGravity() { return 0.05f; }
}
