package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;

/**
 * 【庄园召唤物 · 卫道士】2026-10-10 按用户要求新增。
 *
 * <p>用户原话：「庄园boss无视效果：斩首，摸金校尉。不作为亡灵生物受到部分武器增伤」。
 *
 * <p>此前屠夫波次的「屠夫学徒」(kind 0) 与「屠夫队长」(kind 2) **直接生成原版
 * {@code minecraft:vindicator}**（手里拿铁斧/钻石斧，锋锐 3/4，且 {@code setDropChance(MAINHAND, 0f)}），
 * 于是「斩首」按 {@code #kaleidoscope_world_liquor:bosses} 判定它们不是 boss（原版类型没法加进那张表）、
 * 「摸金校尉」按 {@code #kaleidoscope_tavern:tomb_raider_disarmable}（白名单里就有 {@code vindicator}）
 * **把斧子缴械丢到地上**。
 *
 * <p>换成自有类型后：类型 id 不在缴械白名单里（摸金校尉自然免疫），并可被加进"boss"名单
 * （见本模组 {@code data/kaleidoscope_world_liquor/tags/entity_types/bosses.json}）从而免疫斩首。
 * 外观 / 属性 / AI 与原版卫道士完全一致（渲染复用原版 {@code VindicatorRenderer}）。
 *
 * <p>说明：卫道士本身**不是亡灵**（{@code getMobType()} 已是 {@code UNDEFINED}），
 * 因此这里**不覆写** {@code getMobType()}；亡灵增伤那条从来只影响尸壳类召唤物。
 * 学徒与队长仍靠调用处的 {@code boostMinion(..., kind)} 区分属性与装备，本类不做区分。
 */
public class ManorVindicatorEntity extends Vindicator {

    public ManorVindicatorEntity(EntityType<? extends ManorVindicatorEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    /** 与原版卫道士同一张属性表（{@link Vindicator#createAttributes()}）。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Vindicator.createAttributes();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
