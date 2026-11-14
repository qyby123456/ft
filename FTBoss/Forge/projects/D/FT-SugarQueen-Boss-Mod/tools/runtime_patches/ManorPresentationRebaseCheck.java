import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

public final class ManorPresentationRebaseCheck {
    public static void main(String[] arguments) throws Exception {
        Set<String> protectedEntries = new HashSet<>(Files.readAllLines(Path.of(arguments[2])));
        try (ZipFile original = new ZipFile(arguments[0]); ZipFile latest = new ZipFile(arguments[1])) {
            for (var entry : original.stream().toList()) {
                String name = entry.getName();
                if (name.startsWith("net/farmingtales/ftboss/") && name.endsWith(".class")
                        && (name.contains("Manor") || name.contains("PresentationClock") || name.contains("HiddenBossIntro"))) {
                    protectedEntries.add(name);
                }
            }
            for (String name : protectedEntries) {
                var before = original.getEntry(name);
                var after = latest.getEntry(name);
                if ((before == null) != (after == null)) throw new AssertionError("Concurrent manor ownership change: " + name);
                if (before != null && !Arrays.equals(original.getInputStream(before).readAllBytes(), latest.getInputStream(after).readAllBytes())) {
                    throw new AssertionError("Concurrent manor bytecode change requires reconciliation: " + name);
                }
            }
            int unrelatedChanges = 0;
            Set<String> seen = new HashSet<>();
            for (var entry : latest.stream().toList()) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!seen.add(name)) throw new AssertionError("Duplicate latest-baseline entry: " + name);
                var before = original.getEntry(name);
                if (before == null || !Arrays.equals(original.getInputStream(before).readAllBytes(), latest.getInputStream(entry).readAllBytes())) {
                    if (protectedEntries.contains(name)) throw new AssertionError("Protected entry changed: " + name);
                    unrelatedChanges++;
                }
            }
            for (var entry : original.stream().toList()) {
                if (!entry.isDirectory() && !seen.contains(entry.getName())) {
                    if (protectedEntries.contains(entry.getName())) throw new AssertionError("Protected entry removed: " + entry.getName());
                    unrelatedChanges++;
                }
            }
            System.out.println("PASS baseline rebase: " + protectedEntries.size() + " presentation entries unchanged; "
                    + unrelatedChanges + " unrelated latest-baseline changes retained");
        }
    }
}
