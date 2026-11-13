package net.farmingtales.p03qa;

import net.minecraftforge.fml.common.Mod;

@Mod("ftp03qa")
public final class Verification {
    public Verification() { try { net.minecraft.gametest.framework.GlobalTestReporter.replaceWith(new net.minecraft.gametest.framework.JUnitLikeTestReporter(java.nio.file.Path.of(System.getProperty("ftp03qa.root")).resolve("route-tests.xml").toFile())); } catch (Exception exception) { throw new IllegalStateException(exception); } }
}
