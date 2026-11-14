/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.DoublePlantBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DoubleBlockHalf
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package net.farmingtales.ftboss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class CandyStatueBlock
extends DoublePlantBlock {
    public static final IntegerProperty TINT = IntegerProperty.m_61631_((String)"tint", (int)0, (int)10);
    public static final BooleanProperty WHITE = BooleanProperty.m_61465_((String)"white");
    public static final BooleanProperty GOLD = BooleanProperty.m_61465_((String)"gold");
    private final int ownTint;

    public CandyStatueBlock(BlockBehaviour.Properties props) {
        this(props, 0);
    }

    public CandyStatueBlock(BlockBehaviour.Properties props, int ownTint) {
        super(props);
        this.ownTint = Math.max(0, Math.min(10, ownTint));
        this.m_49959_((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_((Property)f_52858_, (Comparable)DoubleBlockHalf.LOWER)).m_61124_((Property)TINT, (Comparable)Integer.valueOf(this.ownTint))).m_61124_((Property)WHITE, (Comparable)Boolean.FALSE)).m_61124_((Property)GOLD, (Comparable)Boolean.FALSE));
    }

    public int ownTint() {
        return this.ownTint;
    }

    public static boolean isWhite(BlockState state) {
        try {
            return Boolean.TRUE.equals(state.m_61143_((Property)WHITE));
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static boolean isGold(BlockState state) {
        try {
            return Boolean.TRUE.equals(state.m_61143_((Property)GOLD));
        }
        catch (Throwable t) {
            return false;
        }
    }

    public BlockState m_5573_(BlockPlaceContext ctx) {
        BlockState s = super.m_5573_(ctx);
        return s == null ? null : (BlockState)((BlockState)((BlockState)s.m_61124_((Property)TINT, (Comparable)Integer.valueOf(this.ownTint))).m_61124_((Property)WHITE, (Comparable)Boolean.FALSE)).m_61124_((Property)GOLD, (Comparable)Boolean.FALSE);
    }

    protected void m_7926_(StateDefinition.Builder<Block, BlockState> builder) {
        super.m_7926_(builder);
        builder.m_61104_(new Property[]{TINT});
        builder.m_61104_(new Property[]{WHITE});
        builder.m_61104_(new Property[]{GOLD});
    }

    public static int tintOf(BlockState state) {
        try {
            Integer v = (Integer)state.m_61143_((Property)TINT);
            return v == null ? 0 : v;
        }
        catch (Throwable t) {
            return 0;
        }
    }

    protected boolean m_6266_(BlockState floor, BlockGetter level, BlockPos pos) {
        return !floor.m_60795_() && floor.m_60819_().m_76178_() && !floor.m_60812_(level, pos).m_83281_();
    }
}

