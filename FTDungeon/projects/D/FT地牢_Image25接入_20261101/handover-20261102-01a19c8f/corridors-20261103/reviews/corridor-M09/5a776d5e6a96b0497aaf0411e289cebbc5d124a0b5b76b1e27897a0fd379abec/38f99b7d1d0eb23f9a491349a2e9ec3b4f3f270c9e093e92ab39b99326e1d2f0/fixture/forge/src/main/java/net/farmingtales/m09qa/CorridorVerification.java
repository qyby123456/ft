package net.farmingtales.m09qa;

import java.nio.file.Path;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraftforge.fml.common.Mod;

@Mod("ftm09qa")
public final class CorridorVerification {
    public CorridorVerification() {
        try {
            Path destination = Path.of(System.getProperty("ftm09qa.root")).resolve("qa/native-tests.xml");
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(destination.toFile()));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
