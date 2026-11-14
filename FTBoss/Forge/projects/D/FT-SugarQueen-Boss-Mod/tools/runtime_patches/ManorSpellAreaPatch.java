import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

/** 庄园范围增量补丁：只替换指定方法及其闭包，保留当前运行包的其他内容；2026-10-27。 */
public final class ManorSpellAreaPatch implements Opcodes {
    private static final String ROOT = "net/farmingtales/ftboss/";
    public static final String[][] METHODS = {
        {"util/ManorSpellCastRules", "castTicks"},
        {"integ/spells/BossSpellTuning", "radius", "radiusScale"},
        {"skill/SpellVisuals", "radiusScale", "playerRadiusScale"},
        {"integ/spells/ZicaiVoidSpells", "cast", "strike"},
        {"skill/QizaiConstellation", "marks", "valid", "impact"},
        {"integ/spells/QizaiSpell", "onCast", "prepare"},
        {"integ/spells/QizaiInfernoEffects$Cast", "targets", "beams"},
        {"integ/spells/QizaiBarrageEffects$Cast", "spawn", "sync"},
        {"client/render/ZicaiVoidVisual", "render"},
        {"client/render/QizaiSigilRenderer", "shouldRender", "render"},
        {"client/render/QizaiLaserBarrageVisual", "render"},
        {"client/render/QizaiLaserBarrageModel", "frame"},
        {"skill/SpellVisualPacket", "valid"},
        {"client/render/SpellVisualRenderer", "renderBeamModels"},
        {"util/SpellBeamModels", "draw"},
        {"util/SpellVisualGeometry", "draw"}
    };

    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[1]), output = Path.of(arguments[3]);
        ZicaiVoidPatch.Names names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            for (String[] selection : METHODS) {
                String name = ROOT + selection[0];
                ClassNode target = read(baseline.getInputStream(baseline.getEntry(name + ".class")).readAllBytes());
                ClassNode donor = new ClassNode();
                ClassNode original = read(Files.readAllBytes(classes.resolve(name + ".class")));
                Set<String> selectedNames = new HashSet<>();
                for (int index = 1; index < selection.length; index++) {
                    for (MethodNode method : original.methods) if (method.name.equals(selection[index]) && (method.access & ACC_BRIDGE) == 0)
                        selectedNames.add(names.mapMethodName(name, method.name, method.desc));
                }
                original.accept(new ClassRemapper(donor, names));
                ArrayDeque<MethodNode> pending = new ArrayDeque<>();
                for (String selected : selectedNames) {
                    int found = 0;
                    for (MethodNode method : donor.methods) {
                        if (method.name.equals(selected) && (method.access & ACC_BRIDGE) == 0) { pending.add(method); found++; }
                    }
                    if (found == 0) throw new IllegalStateException("Missing donor " + name + "." + selected);
                }
                Set<String> copied = new HashSet<>();
                while (!pending.isEmpty()) {
                    MethodNode method = pending.remove();
                    if (!copied.add(method.name + method.desc)) continue;
                    target.methods.removeIf(old -> old.name.equals(method.name) && old.desc.equals(method.desc));
                    target.methods.add(method);
                    for (AbstractInsnNode instruction : method.instructions) {
                        if (!(instruction instanceof InvokeDynamicInsnNode dynamic)) continue;
                        for (Object argument : dynamic.bsmArgs) {
                            if (!(argument instanceof Handle handle) || !handle.getOwner().equals(name)
                                    || !handle.getName().startsWith("lambda$")) continue;
                            pending.add(donor.methods.stream().filter(candidate -> candidate.name.equals(handle.getName())
                                    && candidate.desc.equals(handle.getDesc())).findFirst().orElseThrow());
                        }
                    }
                }
                save(target, output);
                System.out.println("Patched " + name + ": " + copied);
            }
            ClassNode rules = read(Files.readAllBytes(classes.resolve(ROOT + "util/ManorSpellAreaRules.class")));
            save(rules, output);
        }
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static void save(ClassNode node, Path output) throws Exception {
        for (MethodNode method : node.methods) {
            if ((method.access & (ACC_ABSTRACT | ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path file = output.resolve(node.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }
}
