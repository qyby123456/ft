package cn.ft.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@Mod("ftintegration_smoke")
@GameTestHolder("ftintegration_smoke")
@PrefixGameTestTemplate(false)
public final class EnvironmentSmoke {
    public EnvironmentSmoke() {}

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void isolatedWorldReadback(GameTestHelper helper) {
        BlockPos position = new BlockPos(2, 1, 2);
        helper.setBlock(position, Blocks.GOLD_BLOCK);
        helper.assertBlockPresent(Blocks.GOLD_BLOCK, position);
        helper.succeed();
    }
}
