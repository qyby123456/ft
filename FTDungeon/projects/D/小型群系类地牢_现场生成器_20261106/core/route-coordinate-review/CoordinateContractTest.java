import java.nio.file.Files;
import java.nio.file.Path;
import static cn.ft.biomedungeon.core.DungeonApi.*;
import static cn.ft.biomedungeon.core.DungeonGeometry.*;

public final class CoordinateContractTest {
    private static int checks;
    private CoordinateContractTest() {}
    private static void same(double actual, double expected, String message) {
        checks++;
        if (Math.abs(actual - expected) > 1e-9) { throw new AssertionError(message + ": " + actual + " != " + expected); }
    }
    private static double[] rotatePoint(double axisX, double axisY, double axisZ, int rotation) {
        return switch (Math.floorMod(rotation, 4)) {
            case 0 -> new double[] {axisX, axisY, axisZ};
            case 1 -> new double[] {-axisZ, axisY, axisX};
            case 2 -> new double[] {-axisX, axisY, -axisZ};
            default -> new double[] {axisZ, axisY, -axisX};
        };
    }
    public static void main(String[] arguments) throws Exception {
        int rows = 0;
        for (String line : Files.readAllLines(Path.of("evidence/real-endpoints.tsv"))) {
            String[] fields = line.split("\t");
            int quarter = Integer.parseInt(fields[0]);
            Vec3i origin = new Vec3i(Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
            double sourceX = Double.parseDouble(fields[4]);
            double sourceY = Double.parseDouble(fields[5]);
            double sourceZ = Double.parseDouble(fields[6]);
            double[] originalCenter = rotatePoint(Math.round(sourceX * 2) / 2.0 + 0.5,
                    Math.round(sourceY * 2) / 2.0, Math.round(sourceZ * 2) / 2.0 + 0.5, quarter);
            double offsetX = quarter == 1 || quarter == 2 ? -0.5 : 0.5;
            double offsetZ = quarter == 2 || quarter == 3 ? -0.5 : 0.5;
            double actualX = Double.parseDouble(fields[7]) + origin.x() + offsetX;
            double actualY = Double.parseDouble(fields[8]) - 96 + origin.y();
            double actualZ = Double.parseDouble(fields[9]) + origin.z() + offsetZ;
            same(actualX, origin.x() + originalCenter[0], "old X center semantic");
            same(actualY, origin.y() + originalCenter[1], "old Y semantic");
            same(actualZ, origin.z() + originalCenter[2], "old Z center semantic");
            Vec3i cell = blockPosition(new Vec3i((int) sourceX, (int) sourceY, (int) sourceZ), origin, quarter);
            same(actualX, cell.x() + 0.5, "frozen blockPosition X center");
            same(actualY, cell.y(), "frozen blockPosition feet Y");
            same(actualZ, cell.z() + 0.5, "frozen blockPosition Z center");
            double[] inverse = rotatePoint(actualX - origin.x(), actualY - origin.y(), actualZ - origin.z(), -quarter);
            same(inverse[0] - 0.5, sourceX, "original raw target X retained");
            same(inverse[1], sourceY, "original raw target Y retained");
            same(inverse[2] - 0.5, sourceZ, "original raw target Z retained");
            same(actualX, Double.parseDouble(fields[10]), "Python X agrees");
            same(actualY, Double.parseDouble(fields[11]), "Python Y agrees");
            same(actualZ, Double.parseDouble(fields[12]), "Python Z agrees");
            rows++;
        }
        for (int quarter = 0; quarter < 4; quarter++) {
            double[] offset = rotatePoint(0.5, 0, 0.5, quarter);
            Vec3i corrected = blockPlacementOrigin(new Vec3i(0, 0, 0), quarter);
            same(offset[0], corrected.x() + 0.5, "volume/cell origin compensation identity X");
            same(offset[2], corrected.z() + 0.5, "volume/cell origin compensation identity Z");
        }
        if (rows != 4400) { throw new AssertionError("Expected all4400 selected original endpoints"); }
        System.out.println("PASS " + checks + " Java assertions: all4400 true endpoints preserve old center semantics and frozen blockPosition; Java=" + System.getProperty("java.version"));
        System.out.println("SCOPE formula harness links exact frozen core JAR; not execution of NativeRouteAudit/MC collision; runtime exact bytecode retained via javap");
    }
}
