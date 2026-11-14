package net.farmingtales.ftboss.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * **彩色大便投掷物**(2026-09-12 按用户要求):
 * 一阶段用棕色外观, 二阶段用彩色外观。
 * 零新增模型/贴图: 直接复用原版物品外观 + 原版 ThrownItemRenderer。
 * (2026-09-12 晚: **肥水坑技能已按用户要求整体移除**)
 */
public class RainbowPoopEntity extends ThrowableItemProjectile {

    /** 命中伤害 */
    private float dmg = 3.0f;      // 6 → ×0.5(已含全局修正, v77)
    /** 二阶段(彩色)外观 */
    private boolean rainbow = false;

    public RainbowPoopEntity(EntityType<? extends RainbowPoopEntity> type, Level level) {
        super(type, level);
    }

    public RainbowPoopEntity(Level level, LivingEntity shooter) {
        super(FTEntities.RAINBOW_POOP.get(), shooter, level);
    }

    /** v77: 调用方传的是"设计值", 这里统一 ×0.5 得到玩家实际吃到的伤害(原来由全局钩子做) */
    public void setDamage(float v) { this.dmg = v * 0.5f; }
    public void setRainbow(boolean v) {
        this.rainbow = v;
        this.setItem(new ItemStack(itemFor(level().getRandom().nextInt(3))));
    }

    private static Item itemFor(int i) {
        return switch (i) {
            case 0 -> Items.MAGENTA_DYE;
            case 1 -> Items.CYAN_DYE;
            default -> Items.YELLOW_DYE;
        };
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BROWN_DYE;      // 一阶段: 棕色
    }

    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult hit) {
        super.onHitEntity(hit);
        if (this.level().isClientSide) return;
        Entity e = hit.getEntity();
        Entity owner = this.getOwner();
        if (e instanceof LivingEntity le) {
            le.hurt(this.damageSources().thrown(this, owner), this.dmg);
            /* 2026-09-12: 直接命中也要给玩家 debuff(原来只有水坑给) —— 随机 2 种 */
            if (le instanceof net.minecraft.world.entity.player.Player) {
                /* 2026-09-12 按用户要求: 命中固定附带 **缓慢 + 反胃 + 饥饿** */
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 160, 0));      // 饥饿
            }
            if (le instanceof net.minecraft.world.entity.player.Player && this.getOwner() instanceof CompostBossEntity cb) {
                cb.noteHitOnPlayer();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) return;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            var pos = result.getLocation();
            /* 落地飞溅(棕色为主, 彩色弹追加彩虹点) */
            net.farmingtales.ftboss.util.Fx.particles(sl, pos.x, pos.y + 0.2, pos.z, 14,
                    ParticleTypes.ITEM_SLIME, 0.6, 0.08);
            if (this.rainbow) {
                net.farmingtales.ftboss.util.Fx.particles(sl, pos.x, pos.y + 0.3, pos.z, 10,
                        ParticleTypes.HAPPY_VILLAGER, 0.7, 0.06);
            }
            /* 2026-09-12 晚: 肥水坑技能已整体移除(PoopPuddle 一并删除), 落地只留飞溅 */
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.tickCount % 3 == 0) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                net.farmingtales.ftboss.util.Fx.particles(sl, this.getX(), this.getY(), this.getZ(), 1,
                        this.rainbow ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ITEM_SLIME, 0.08, 0.01);
            }
        }
    }
}
