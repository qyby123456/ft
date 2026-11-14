import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** 远程及双通道战斗定点构建；2026-10-24 保留运行基线其余方法及所有原生来源认证。 */
public final class PatchRangedHybrid implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/item/";

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(BASE + name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static String trace(MethodNode method) {
        Textifier text = new Textifier();
        method.accept(new TraceMethodVisitor(text));
        StringWriter output = new StringWriter();
        text.print(new PrintWriter(output));
        return output.toString();
    }

    public static void main(String[] args) throws Exception {
        Map<String, List<String>> replacements = Map.of(
                "BowGrowthCombat", List.of("attributes"),
                "FoodBlessingCombat", List.of("eligible", "crossbowChargeTicks", "quoteDamage"),
                "WeaponMaterialBonuses", List.of("attributes", "rangedDamage", "materialTooltip"),
                "FoodWeaponFamilyBuild", List.of("chargeTicks", "staffAttributes"),
                "WeaponMaterialBuild", List.of("materials", "build", "attributes"),
                "FoodWeaponRanged", List.of("launch"));
        try (JarFile baseline = new JarFile(args[0]); JarFile donors = new JarFile(args[1])) {
            for (var entry : replacements.entrySet()) {
                ClassNode original = read(baseline, entry.getKey());
                ClassNode node = read(baseline, entry.getKey());
                if (entry.getKey().equals("FoodWeaponRanged")) {
                    MethodNode launch = node.methods.stream().filter(method -> method.name.equals("launch")).findFirst().orElseThrow();
                    int count = 0;
                    for (AbstractInsnNode instruction : launch.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.name.equals("nativeMaterialPutReturned")) {
                            InsnList addition = new InsnList();
                            addition.add(new VarInsnNode(ALOAD, 0));
                            addition.add(new VarInsnNode(ALOAD, 1));
                            addition.add(new MethodInsnNode(INVOKESTATIC, BASE + "RangedHybridCombat", "launch",
                                    "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;)V", false));
                            launch.instructions.insert(call, addition);
                            count++;
                        }
                    }
                    if (count != 1) throw new AssertionError("Expected one authenticated material publication");
                } else {
                    ClassNode donor = read(donors, entry.getKey());
                    for (String name : entry.getValue()) {
                        MethodNode prior = node.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
                        MethodNode method = donor.methods.stream().filter(candidate -> candidate.name.equals(name) && candidate.desc.equals(prior.desc))
                                .findFirst().orElseThrow();
                        method.visibleAnnotations = prior.visibleAnnotations;
                        method.invisibleAnnotations = prior.invisibleAnnotations;
                        node.methods.set(node.methods.indexOf(prior), method);
                    }
                }
                for (MethodNode prior : original.methods) {
                    MethodNode current = node.methods.stream().filter(method -> method.name.equals(prior.name) && method.desc.equals(prior.desc))
                            .findFirst().orElseThrow();
                    if (!entry.getValue().contains(prior.name) && !trace(prior).equals(trace(current))) {
                        throw new AssertionError("Unrelated method changed: " + entry.getKey() + "#" + prior.name);
                    }
                }
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                Path output = Path.of(args[2]).resolve(BASE + entry.getKey() + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
                System.out.println("PASS selected-method patch and sibling preservation: " + entry.getKey());
            }
        }
    }
}
