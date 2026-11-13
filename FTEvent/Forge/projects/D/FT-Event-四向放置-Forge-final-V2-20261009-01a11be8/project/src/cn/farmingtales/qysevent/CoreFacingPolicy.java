package cn.farmingtales.qysevent;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class CoreFacingPolicy {
    private static final ResourceLocation CORE = new ResourceLocation("farmingtales", "wishing_well_core");

    private CoreFacingPolicy() {}

    public static boolean matches(ResourceLocation key, boolean horizontalFacing) {
        return horizontalFacing && CORE.equals(key);
    }

    public static Direction rotate(Direction facing, Rotation rotation) {
        return rotation.m_55954_(facing);
    }

    public static Direction mirror(Direction facing, Mirror mirror) {
        return rotate(facing, mirror.m_54846_(facing));
    }

    private static boolean applies(BlockState state) {
        if (!state.m_61138_(HorizontalDirectionalBlock.f_54117_)) return false;
        return matches(ForgeRegistries.BLOCKS.getKey(state.m_60734_()), true);
    }

    public static BlockState rotateCore(BlockState state, Rotation rotation) {
        if (!applies(state)) return null;
        return state.m_61124_(HorizontalDirectionalBlock.f_54117_,
                rotate(state.m_61143_(HorizontalDirectionalBlock.f_54117_), rotation));
    }

    public static BlockState mirrorCore(BlockState state, Mirror mirror) {
        if (!applies(state)) return null;
        return state.m_61124_(HorizontalDirectionalBlock.f_54117_,
                mirror(state.m_61143_(HorizontalDirectionalBlock.f_54117_), mirror));
    }
}
