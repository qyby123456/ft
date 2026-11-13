package net.farmingtales.s06qa;

import java.nio.file.Path;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraftforge.fml.common.Mod;

@Mod("fts06qa")
public final class Verification {
    public Verification() {
        try {
            Path destination = Path.of(System.getProperty("fts06qa.root")).resolve("qa/native-tests.xml");
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(destination.toFile()));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
