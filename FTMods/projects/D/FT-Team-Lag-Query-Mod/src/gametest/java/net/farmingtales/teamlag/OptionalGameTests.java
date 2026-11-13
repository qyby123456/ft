package net.farmingtales.teamlag;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(LagQuery.ID)
@PrefixGameTestTemplate(false)
public final class OptionalGameTests {
    @GameTest(template = "arena", timeoutTicks = 100, batch = "optional")
    public static void optionalModsAndReadOnlyIdentity(GameTestHelper helper) {
        var mods = ModList.get();
        helper.assertTrue(mods.isLoaded("ftteamlag") && !mods.isLoaded("ftecon") && !mods.isLoaded("ftlagcontrol"), "only new read-only mod ID provided");
        helper.assertTrue(mods.isLoaded("observable") != Boolean.getBoolean("ftteamlag.withoutObservable"), "OBS optional profile correct");
        helper.assertTrue(mods.isLoaded("refinedstorage") != Boolean.getBoolean("ftteamlag.withoutRefinedStorage"), "RS optional profile correct");
        helper.assertTrue(LagQuery.server() == helper.getLevel().getServer(), "server started with selected optional dependencies");
        helper.assertTrue(TimingHooks.missing().isEmpty(), "all installed timing hooks applied: " + TimingHooks.status());
        helper.assertTrue(RefinedStorageTiming.status().contains(mods.isLoaded("refinedstorage") ? "控制器" : "未安装"), "RS diagnostics correct");
        helper.succeed();
    }
}
