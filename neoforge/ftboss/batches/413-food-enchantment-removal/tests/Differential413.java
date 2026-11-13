import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class Differential413 {
    private static final Set<String> WEAPON = Set.of("applyFoodBlessing", "applyFoodBlessingFromUi", "isBlessingCandidate", "ftboss$legacyOrdinary$applyFoodBlessing", "ftboss$legacyOrdinary$applyFoodBlessingFromUi", "ftboss$legacyOrdinary$isBlessingCandidate", "hasWashableBlessing", "foodBlessingList", "ftboss$legacyOrdinary$foodBlessingList", "applyFoodBlessingHits", "foodBlessingDefense", "foodBlessingKillFeast", "grindstoneBlessing", "foodBlessingViews", "ftboss$legacyOrdinary$foodBlessingViews", "addFoodBlessingAttributes", "ftboss$legacyOrdinary$addFoodBlessingAttributes", "addFoodBlessingTooltip", "quoteFoodBlessingHits");
    private static final Set<String> ARMOR = Set.of("eligible", "hasRoom", "apply", "entries", "views", "attributes", "defend", "poison", "tickStart", "tickEnd", "damaged");
    private static ClassNode read(Path path) throws Exception {
        ClassNode result = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(result, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return result;
    }

    private static String base(String name) {
        return name.startsWith("lambda$") ? name.substring(0, name.lastIndexOf('$')) : name;
    }

    private static byte[] body(MethodNode method, String owner, Map<String, String> names, boolean matching) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "MethodComparison413", null, "java/lang/Object", null);
        var visitor = writer.visitMethod(method.access, "body", method.desc, method.signature, method.exceptions.toArray(String[]::new));
        method.accept(new MethodRemapper(visitor, new Remapper() {
            @Override public String mapMethodName(String type, String name, String descriptor) {
                if (!type.equals(owner)) return name;
                return matching ? base(name) : names.getOrDefault(name + descriptor, name);
            }
        }));
        writer.visitEnd();
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        Path before = Path.of(arguments[0]);
        Path after = Path.of(arguments[1]);
        List<Object> mappings = new ArrayList<>();
        int unchanged = 0;
        int retired = 0;
        int removedLambdas = 0;
        try (var paths = Files.walk(after)) {
            for (Path path : paths.filter(value -> value.toString().endsWith(".class")).toList()) {
                Path relative = after.relativize(path);
                if (!Files.exists(before.resolve(relative))) throw new AssertionError("Unexpected new class " + relative);
                ClassNode oldOwner = read(before.resolve(relative));
                ClassNode newOwner = read(path);
                if (!oldOwner.name.equals(newOwner.name)) throw new AssertionError("Owner changed");
                Map<String, String> rename = new HashMap<>();
                Set<MethodNode> matched = new HashSet<>();
                for (MethodNode method : newOwner.methods) {
                    if (!method.name.startsWith("lambda$")) continue;
                    List<MethodNode> candidates = oldOwner.methods.stream().filter(old -> !matched.contains(old) && old.name.startsWith("lambda$") && base(old.name).equals(base(method.name)) && old.desc.equals(method.desc) && Arrays.equals(body(old, oldOwner.name, Map.of(), true), body(method, newOwner.name, Map.of(), true))).toList();
                    if (candidates.isEmpty() || !matched.add(candidates.getFirst())) throw new AssertionError("Non-bijective lambda mapping " + method.name);
                    MethodNode old = candidates.getFirst();
                    rename.put(method.name + method.desc, old.name);
                    if (!method.name.equals(old.name)) mappings.add(Map.of("owner", newOwner.name, "newName", method.name, "oldName", old.name, "descriptor", method.desc, "bodyEqual", true));
                }
                Set<String> allowed = newOwner.name.endsWith("/FoodWeaponEvolution") ? WEAPON : newOwner.name.endsWith("/ArmorFoodBlessings") ? ARMOR : newOwner.name.endsWith("$Section") ? Set.of("$values", "<clinit>") : newOwner.name.endsWith("StatueHubScreen") ? Set.of("render", "mouseClicked", "mouseScrolled") : Set.of();
                Set<String> seen = new HashSet<>();
                for (MethodNode method : newOwner.methods) {
                    String oldName = rename.getOrDefault(method.name + method.desc, method.name);
                    MethodNode old = oldOwner.methods.stream().filter(value -> value.name.equals(oldName) && value.desc.equals(method.desc)).findFirst().orElseThrow(() -> new AssertionError("Unexpected added method " + method.name));
                    seen.add(old.name + old.desc);
                    if (allowed.contains(method.name)) {
                        retired++;
                    } else {
                        if (!Arrays.equals(body(old, oldOwner.name, Map.of(), false), body(method, newOwner.name, rename, false))) throw new AssertionError("Retained business method changed: " + newOwner.name + "." + method.name);
                        unchanged++;
                    }
                }
                for (MethodNode old : oldOwner.methods) {
                    if (seen.contains(old.name + old.desc)) continue;
                    if (old.name.startsWith("lambda$") && allowed.contains(base(old.name).substring("lambda$".length()))) {
                        removedLambdas++;
                    } else if (newOwner.name.endsWith("StatueHubScreen") && Set.of("renderEnchant", "blessingSlots").contains(old.name)) {
                        retired++;
                    } else throw new AssertionError("Unexpected removed method: " + newOwner.name + "." + old.name);
                }
            }
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("failures", 0);
        report.put("exactBijectionLambdaRenames", mappings);
        report.put("unchangedMethodsAfterExactReferenceRebinding", unchanged);
        report.put("retiredOrUiMethods", retired);
        report.put("removedLambdasOnlyFromRetiredOperations", removedLambdas);
        report.put("blanketLambdaNormalizationAccepted", false);
        Files.writeString(Path.of(arguments[2]), new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }
}
