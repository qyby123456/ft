package net.farmingtales.ftboss.entity;

/**
 * 充能球类型(2026-09-11)。故障机器人的"召唤小怪"就是召唤这些球(照鼠群的召唤物模式),
 * 每颗球存活到期(或被拆掉)时按类型结算不同效果。
 *
 * <ul>
 *   <li>{@link #GLASS} 玻璃球 —— <b>AOE</b>: 方圆 8 格全体玩家吃伤害 + 缓慢</li>
 *   <li>{@link #LIGHTNING} 电球 —— <b>单体</b>: 对最近玩家高额雷击 + 感电</li>
 *   <li>{@link #DARK} 黑球 —— <b>蓄力</b>: 存活越久伤害越高(封顶), 无视护甲</li>
 *   <li>{@link #FROST} 冰球 —— <b>保护</b>: 给 Boss 薄霜甲(吸收, **无抗性**)</li>
 *   <li>{@link #ION} 离子球 —— <b>强化</b>: 直接给核心**充能** + 速度(不给力量/吸收/生命提升)</li>
 * </ul>
 */
public enum OrbType {
    GLASS("glass", "玻璃球", "§b"),
    LIGHTNING("lightning", "电球", "§e"),
    DARK("dark", "黑球", "§5"),
    FROST("frost", "冰球", "§f"),
    ION("ion", "离子球", "§d");

    /** 资源前缀(geo/动画/贴图都用它) */
    public final String id;
    /** 中文名(动作栏提示用) */
    public final String cn;
    /** 提示颜色 */
    public final String color;

    OrbType(String id, String cn, String color) {
        this.id = id;
        this.cn = cn;
        this.color = color;
    }

    public static OrbType byId(String id) {
        for (OrbType t : values()) {
            if (t.id.equals(id)) return t;
        }
        return GLASS;
    }
}
