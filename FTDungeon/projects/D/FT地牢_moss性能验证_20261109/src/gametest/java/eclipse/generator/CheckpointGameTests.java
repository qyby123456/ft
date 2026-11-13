package eclipse.generator;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fteclipsegen")
@PrefixGameTestTemplate(false)
public final class CheckpointGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void incrementalPersistenceContract(GameTestHelper helper) {
        IncrementalCheckpointTests.main(new String[0]);
        helper.succeed();
    }
}
