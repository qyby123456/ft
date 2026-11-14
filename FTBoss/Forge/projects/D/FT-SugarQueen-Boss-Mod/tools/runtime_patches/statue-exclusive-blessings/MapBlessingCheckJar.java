import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.nio.file.Files;
import net.minecraftforge.srgutils.IMappingFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public final class MapBlessingCheckJar {
    public static void main(String[] arguments) throws Exception {
        IMappingFile mappings = IMappingFile.load(Path.of(arguments[0]).toFile()).reverse();
        Map<String, ClassReader> hierarchy = new HashMap<>();
        try (JarFile input = new JarFile(arguments[1]); JarFile minecraft = new JarFile(arguments[2]);
                JarOutputStream output = new JarOutputStream(Files.newOutputStream(Path.of(arguments[3])))) {
            Remapper remapper = new Remapper() {
                private String member(String owner, String name, String descriptor, boolean method) {
                    var mapped = mappings.getClass(owner);
                    if (mapped != null) {
                        String result = method ? mapped.remapMethod(name, descriptor) : mapped.remapField(name);
                        if (!result.equals(name)) return result;
                    }
                    ClassReader type = hierarchy.get(owner);
                    if (type == null) {
                        try {
                            var entry = minecraft.getJarEntry(owner + ".class");
                            if (entry != null) type = new ClassReader(minecraft.getInputStream(entry));
                            else {
                                entry = input.getJarEntry(owner + ".class");
                                if (entry != null) type = new ClassReader(input.getInputStream(entry));
                            }
                        } catch (Exception failure) { throw new IllegalStateException(failure); }
                        if (type != null) hierarchy.put(owner, type);
                    }
                    if (type == null) return name;
                    if (type.getSuperName() != null) {
                        String result = member(type.getSuperName(), name, descriptor, method);
                        if (!result.equals(name)) return result;
                    }
                    for (String parent : type.getInterfaces()) {
                        String result = member(parent, name, descriptor, method);
                        if (!result.equals(name)) return result;
                    }
                    return name;
                }

                @Override public String mapMethodName(String owner, String name, String descriptor) {
                    return name.startsWith("<") ? name : member(owner, name, descriptor, true);
                }

                @Override public String mapFieldName(String owner, String name, String descriptor) {
                    return member(owner, name, descriptor, false);
                }
            };
            Set<String> originals = Set.of("FoodWeaponEvolution", "FoodWeaponEvolution$1", "FoodBlessingCombat",
                    "NativeMaterialCrossbowSupport", "MaterialHitPlanCompatibility", "BowGrowthCombat", "BowGrowthCombat$Bonus",
                    "RangedHybridCombat", "ArrowPassiveDamage");
            for (var entry : input.stream().toList()) {
                String name = entry.getName();
                String shortName = name.substring(name.lastIndexOf('/') + 1).replaceFirst("\\.class$", "");
                if (!name.startsWith("net/farmingtales/ftboss/item/") || !name.endsWith(".class")
                        || (!originals.contains(shortName) && !shortName.startsWith("StatueBlessing") && !shortName.startsWith("Armor"))) continue;
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(input.getInputStream(entry)).accept(new ClassRemapper(writer, remapper), 0);
                output.putNextEntry(new JarEntry(name));
                output.write(writer.toByteArray());
                output.closeEntry();
            }
        }
    }
}
