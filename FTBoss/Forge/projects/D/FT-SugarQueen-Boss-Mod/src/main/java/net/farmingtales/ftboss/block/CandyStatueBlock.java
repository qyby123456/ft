package net.farmingtales.ftboss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Boss 石像: 1x1宽 2格高, 上下半格联动(全实心顶面可放置)。
 *
 * <p><b>外观(2026-09-10 重做)</b>: 早先是"一张白膜贴图 + 客户端 BlockColor 乘色", 结果每个石像就是
 * 一整块纯色。现在改成**每个 Boss 一张专属手绘贴图**(石材斑驳 + 该 Boss 的花纹与配色, 见
 * {@code tools/model3d/gen_statue_skins.cjs}), 方块状态 {@link #TINT} 只用来**选模型**:
 * <pre>
 *   tint = 0            → 白膜(共用 {@code ftboss:block/statue_white}, 未上色的石料)
 *   tint = 1..5         → 该 Boss 的上色皮肤 {@code ftboss:block/statue_&lt;该方块&gt;}
 *                         (1 姜饼王 / 2 屠夫 / 3 沙耶 / 4 酒桶王 / 5 鼠群, 仅作档位编号, 颜色已画进贴图)
 * </pre>
 * 方块放下时**默认就是上色状态**({@link #getStateForPlacement} 用 {@link #ownTint});
 * 手持**对应 Boss 的召唤物**左键可在"上色 ↔ 白膜"之间切换(见 {@code boss.BossHandler#onLeftClickBlock})。
 */
public class CandyStatueBlock extends DoublePlantBlock {

    /** 外观档位: 0 = 白膜(未上色石料) / 1 姜饼王 / 2 屠夫 / 3 沙耶 / 4 酒桶王 / 5 鼠群 / 6 故障机器人 / 7 答辩超人。
     *  各档位的**实际颜色已经画进对应贴图**(不再是运行时乘色), 所以这里不再维护颜色表。
     *  <p>2026-09-12 修: 原来上限写死 5, 导致 6(机器人) 与 7(答辩超人) 被 clamp 成 5 ——
     *  这两座石像"手持召唤物切换外观"时会去匹配**鼠群诱饵**, 提示"召唤物不匹配"。 */
    public static final IntegerProperty TINT = IntegerProperty.create("tint", 0, 10);

    /**
     * 「白膜」开关(2026-09-10 追加): <b>默认 false = 显示该 Boss 的真实外观</b>。
     * <p>为什么不用 {@link #TINT}=0 表示白膜: 早期版本的石像放置时 tint 就是 0, 这些**已经放在存档里**的方块
     * 会一直显示白膜, 玩家看到的就是"新外观没生效"。改成新增一个属性后, 旧存档解析时缺失的属性会取默认值
     * (MC 的 BlockStateParser 是宽松的) → **所有旧石像自动变成 Boss 外观**, 白膜仍然可以用左键切出来。
     * <p>{@link #TINT} 保留只为兼容旧存档与"手持对应召唤物"的匹配逻辑, 不再参与选模型。
     */
    public static final BooleanProperty WHITE = BooleanProperty.create("white");
    /** Gold appearance unlocked together with terminal difficulty. */
    public static final BooleanProperty GOLD = BooleanProperty.create("gold");

    /** 这座石像"上色"后的档位(0 = 通用糖果石像) */
    private final int ownTint;

    public CandyStatueBlock(BlockBehaviour.Properties props) {
        this(props, 0);
    }

    public CandyStatueBlock(BlockBehaviour.Properties props, int ownTint) {
        super(props);
        this.ownTint = Math.max(0, Math.min(10, ownTint));
        // ⚠ 必须**显式写回 HALF=LOWER**(2026-09-10 修复"石像无法放置"):
        //   StateDefinition.any() 返回的是"每个属性都取第一个可能值"的那个状态, 而 DoubleBlockHalf
        //   的枚举顺序是 UPPER 在前 → any() 给的是 HALF=UPPER。父类 DoublePlantBlock 的构造里本来
        //   写了一次 LOWER, 但本类随后又 registerDefaultState(any()...) 会**把它覆盖回 UPPER**,
        //   于是 BlockItem 放置时拿到 HALF=UPPER(上半格), setPlacedBy 又在上面再放一个 UPPER,
        //   两个上半格都过不了 canSurvive → 石像永远放不下来(物品模型还同时报错, 见 models/item/*.json)。
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(TINT, this.ownTint)
                .setValue(WHITE, Boolean.FALSE)
                .setValue(GOLD, Boolean.FALSE));
    }

    /** 该石像"上色"后的档位(0 = 通用糖果石像) */
    public int ownTint() {
        return this.ownTint;
    }

    /** 是否处于白膜外观 */
    public static boolean isWhite(BlockState state) {
        try {
            return Boolean.TRUE.equals(state.getValue(WHITE));
        } catch (Throwable t) {
            return false;
        }
    }

    /** Whether the statue is using the terminal-difficulty gold appearance. */
    public static boolean isGold(BlockState state) {
        try {
            return Boolean.TRUE.equals(state.getValue(GOLD));
        } catch (Throwable t) {
            return false;
        }
    }

    /** 放置时带入上色档位(tint 只作兼容记录), 白膜默认关 → 一放出来就是该 Boss 的外观 */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState s = super.getStateForPlacement(ctx);
        return s == null ? null : s.setValue(TINT, this.ownTint)
                .setValue(WHITE, Boolean.FALSE).setValue(GOLD, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TINT);
        builder.add(WHITE);
        builder.add(GOLD);
    }

    /** 取该方块状态的档位(0 = 白膜) */
    public static int tintOf(BlockState state) {
        try {
            Integer v = state.getValue(TINT);
            return v == null ? 0 : v;
        } catch (Throwable t) {
            return 0;
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter level, BlockPos pos) {
        return !floor.isAir()
                && floor.getFluidState().isEmpty()
                && !floor.getCollisionShape(level, pos).isEmpty();
    }
}
