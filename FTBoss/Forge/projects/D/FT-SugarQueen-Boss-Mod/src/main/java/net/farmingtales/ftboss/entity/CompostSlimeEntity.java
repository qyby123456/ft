package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

/**
 * 【棕黑史莱姆】(2026-09-12 按用户要求新增, 炫彩答辩超人的召唤物)。
 *
 * <p>直接继承原版 {@link Slime}, 所以**弹跳 AI / 挤压形变 / 死亡分裂成小史莱姆**全部白送,
 * 只需要换一张**棕黑色**贴图({@code tools/model3d/gen_compost_slime_tex.cjs} 拿原版 slime.png
 * 保留形状与透明度、只把色相映射到棕黑)。45 秒后自行消散。
 */
public class CompostSlimeEntity extends Slime {

    /** 存活时间(45 秒) */
    public static final int LIFE = 900;

    public CompostSlimeEntity(EntityType<? extends CompostSlimeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > LIFE) this.discard();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
