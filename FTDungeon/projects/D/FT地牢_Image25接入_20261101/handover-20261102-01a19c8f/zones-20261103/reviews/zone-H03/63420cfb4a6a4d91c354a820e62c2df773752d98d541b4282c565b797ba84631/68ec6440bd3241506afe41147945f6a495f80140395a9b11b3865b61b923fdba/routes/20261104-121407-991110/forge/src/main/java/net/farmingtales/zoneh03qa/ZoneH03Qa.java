package net.farmingtales.zoneh03qa;

import net.minecraftforge.fml.common.Mod;

@Mod("ftzoneh03qa")
public final class ZoneH03Qa {
    public ZoneH03Qa() { try { net.minecraft.gametest.framework.GlobalTestReporter.replaceWith(new net.minecraft.gametest.framework.JUnitLikeTestReporter(java.nio.file.Path.of(System.getProperty("ftzoneh03qa.root")).resolve("route-tests.xml").toFile())); } catch (Exception exception) { throw new IllegalStateException(exception); } }
}
