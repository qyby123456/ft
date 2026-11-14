package net.farmingtales.ftboss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * 【庄园召唤物 · 尸壳】2026-10-10 按用户要求新增。
 *
 * <p>用户原话：「庄园boss无视效果：斩首，摸金校尉。不作为亡灵生物受到部分武器增伤」。
 *
 * <p>此前庄园波次（屠夫的「腌肉尸壳」、糖霜女皇的「焦糖结晶」）**直接生成原版
 * {@code minecraft:husk}**，于是三件事同时中招：
 * <ol>
 *   <li><b>斩首</b>（{@code kaleidoscope_world_liquor:beheading}，整合包文案：攻击时有概率造成双倍伤害、
 *       若该次击杀则必掉头颅）按 {@code #kaleidoscope_world_liquor:bosses} 判定"是不是 boss"——
 *       原版类型不可能被我们加进那张表；</li>
 *   <li><b>摸金校尉</b>（{@code kaleidoscope_tavern:tomb_raider}）会把
 *       {@code #kaleidoscope_tavern:tomb_raider_disarmable} 里的实体**主手武器缴械丢在地上**——
 *       该表白名单里写着 {@code husk}/{@code zombie}/{@code vindicator}，而这些召唤物本来
 *       {@code setDropChance(MAINHAND, 0f)} 是不掉东西的；</li>
 *   <li><b>亡灵增伤</b>：原版尸壳的 {@code getMobType()} 是 {@link MobType#UNDEAD}，
 *       亡灵杀手与第三方亡灵增伤武器据此加伤。</li>
 * </ol>
 *
 * <p>换成自有类型后：类型 id 不在任何第三方白名单里（②自然免疫），可以被加进"boss"名单（①见本模组
 * {@code data/kaleidoscope_world_liquor/tags/entity_types/bosses.json}），
 * {@link #getMobType()} 覆写成 {@code UNDEFINED}（③）。
 * 外观 / 属性 / AI / 掉落策略与原版尸壳**完全一致**（渲染仍复用原版 {@code HuskRenderer}）。
 *
 * <p>⚠️ 注意：本类**不参与**"亡灵迁徙"每日事件（那个事件只改带 {@code DAILY_SPAWN} 标记的自然刷怪），
 * 也不改变 {@code isInvertedHealAndHarm()}（尸壳仍会被治疗药水伤害，与原版一致）。
 */
public class ManorHuskEntity extends Husk {

    public ManorHuskEntity(EntityType<? extends ManorHuskEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    /** 与原版尸壳同一张属性表（{@link Zombie#createAttributes()}）。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes();
    }

    /** 不作为亡灵生物（用户 2026-10-10）——亡灵增伤武器不再对庄园召唤物生效。 */
    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
