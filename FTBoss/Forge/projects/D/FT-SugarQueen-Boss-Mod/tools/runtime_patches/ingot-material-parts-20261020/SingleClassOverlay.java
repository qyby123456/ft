import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** 单类覆盖并逐条验证生产包，其余条目必须保持原样。 */
public final class SingleClassOverlay {
    private static final String ENTRY = "net/farmingtales/ftboss/util/WeaponEnvironmentMaterials.class";
    private static final Set<String> ADDED = Set.of("net/farmingtales/ftboss/util/WeaponMaterialBonusRules.class",
            "net/farmingtales/ftboss/util/WeaponMaterialBonusRules$Input.class",
            "net/farmingtales/ftboss/util/WeaponMaterialBonusRules$Bonus.class",
            "net/farmingtales/ftboss/item/WeaponMaterialBonuses.class");
    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[1]), candidate = Path.of(arguments[2]);
        if (arguments[0].equals("overlay")) {
            try (JarFile original = new JarFile(baseline.toFile());
                    JarOutputStream output = new JarOutputStream(Files.newOutputStream(candidate))) {
                Enumeration<JarEntry> entries = original.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    JarEntry copy = new JarEntry(entry.getName());
                    copy.setTime(entry.getTime());
                    output.putNextEntry(copy);
                    if (!entry.isDirectory()) {
                        byte[] bytes = entry.getName().equals(ENTRY)
                                ? Files.readAllBytes(Path.of(arguments[3]).resolve(ENTRY))
                                : original.getInputStream(entry).readAllBytes();
                        output.write(bytes);
                    }
                    output.closeEntry();
                }
                for (String name : ADDED) {
                    if (original.getJarEntry(name) != null) throw new IllegalStateException("New class already exists " + name);
                    output.putNextEntry(new JarEntry(name));
                    output.write(Files.readAllBytes(Path.of(arguments[3]).resolve(name)));
                    output.closeEntry();
                }
            }
        }
        int unchanged = 0, changed = 0;
        try (JarFile original = new JarFile(baseline.toFile()); JarFile updated = new JarFile(candidate.toFile())) {
            if (original.size() + ADDED.size() != updated.size()) throw new IllegalStateException("Entry count changed");
            for (String name : ADDED) if (updated.getJarEntry(name) == null) throw new IllegalStateException("Missing bonus class " + name);
            Set<String> names = new HashSet<>();
            Enumeration<JarEntry> entries = original.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!names.add(entry.getName())) throw new IllegalStateException("Duplicate baseline entry");
                JarEntry after = updated.getJarEntry(entry.getName());
                if (after == null) throw new IllegalStateException("Missing " + entry.getName());
                boolean equal = Arrays.equals(original.getInputStream(entry).readAllBytes(), updated.getInputStream(after).readAllBytes());
                if (entry.getName().equals(ENTRY)) {
                    if (equal) throw new IllegalStateException("Material class not changed");
                    changed++;
                } else {
                    if (!equal) throw new IllegalStateException("Unrelated change " + entry.getName());
                    unchanged++;
                }
            }
            if (changed != 1) throw new IllegalStateException("Expected exactly one class change");
        }
        System.out.println("PASS_EXACT_MATERIAL_DELTA changed=" + changed + " added=" + ADDED.size() + " unchanged=" + unchanged);
    }
}
