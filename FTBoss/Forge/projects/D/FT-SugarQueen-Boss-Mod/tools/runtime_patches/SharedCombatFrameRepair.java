import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

public final class SharedCombatFrameRepair {
    private record TypeInfo(String parent, List<String> interfaces, boolean isInterface) {}

    private static final class Hierarchy {
        private final Map<String, TypeInfo> types = new HashMap<>();

        private TypeInfo type(String name) {
            return types.computeIfAbsent(name, key -> {
                try (InputStream input = SharedCombatFrameRepair.class.getClassLoader()
                        .getResourceAsStream(key + ".class")) {
                    if (input == null) throw new IllegalStateException("Missing hierarchy class: " + key);
                    ClassReader reader = new ClassReader(input);
                    return new TypeInfo(reader.getSuperName(), List.of(reader.getInterfaces()),
                            (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0);
                } catch (java.io.IOException failure) {
                    throw new IllegalStateException("Cannot read hierarchy: " + key, failure);
                }
            });
        }

        private static String component(String array) {
            String descriptor = array.substring(1);
            return descriptor.startsWith("L") ? descriptor.substring(1, descriptor.length() - 1) : descriptor;
        }

        private static boolean primitive(String name) {
            return name.length() == 1 && "ZCBSIFJD".contains(name);
        }

        private boolean assignable(String target, String source) {
            if (target.equals(source) || target.equals("java/lang/Object")) return true;
            if (source.startsWith("[")) {
                if (target.equals("java/lang/Cloneable") || target.equals("java/io/Serializable")) return true;
                if (!target.startsWith("[")) return false;
                String targetComponent = component(target), sourceComponent = component(source);
                if (primitive(targetComponent) || primitive(sourceComponent)) return targetComponent.equals(sourceComponent);
                return assignable(targetComponent, sourceComponent);
            }
            if (target.startsWith("[")) return false;
            ArrayDeque<String> pending = new ArrayDeque<>();
            Set<String> visited = new HashSet<>();
            pending.add(source);
            while (!pending.isEmpty()) {
                String current = pending.removeFirst();
                if (!visited.add(current)) continue;
                if (target.equals(current)) return true;
                TypeInfo info = type(current);
                if (info.parent() != null) pending.add(info.parent());
                pending.addAll(info.interfaces());
            }
            return false;
        }

        private String common(String first, String second) {
            if (assignable(first, second)) return first;
            if (assignable(second, first)) return second;
            if (first.startsWith("[") && second.startsWith("[")) {
                String firstComponent = component(first), secondComponent = component(second);
                if (!primitive(firstComponent) && !primitive(secondComponent)) {
                    String merged = common(firstComponent, secondComponent);
                    return "[" + (merged.startsWith("[") ? merged : "L" + merged + ";");
                }
                return "java/lang/Object";
            }
            if (first.startsWith("[") || second.startsWith("[")) return "java/lang/Object";
            if (type(first).isInterface() || type(second).isInterface()) return "java/lang/Object";
            String current = type(first).parent();
            while (current != null && !assignable(current, second)) current = type(current).parent();
            return current == null ? "java/lang/Object" : current;
        }
    }

    private static MethodNode copy(MethodNode method) {
        MethodNode result = new MethodNode(method.access, method.name, method.desc,
                method.signature, method.exceptions.toArray(String[]::new));
        method.accept(result);
        return result;
    }

    public static byte[] instructions(MethodNode method) {
        MethodNode normalized = copy(method);
        for (AbstractInsnNode instruction : normalized.instructions.toArray()) {
            if (instruction instanceof FrameNode) normalized.instructions.remove(instruction);
        }
        normalized.maxLocals = 0;
        normalized.maxStack = 0;
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "SharedCadenceVerification", null, "java/lang/Object", null);
        normalized.accept(writer.visitMethod(normalized.access, normalized.name, normalized.desc,
                normalized.signature, normalized.exceptions.toArray(String[]::new)));
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static MethodNode repair(ClassNode owner, MethodNode method, Hierarchy hierarchy) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override protected String getCommonSuperClass(String first, String second) {
                return hierarchy.common(first, second);
            }
        };
        writer.visit(owner.version, owner.access, owner.name, owner.signature,
                owner.superName, owner.interfaces.toArray(String[]::new));
        method.accept(writer.visitMethod(method.access, method.name, method.desc,
                method.signature, method.exceptions.toArray(String[]::new)));
        writer.visitEnd();
        ClassNode rebuilt = new ClassNode();
        new ClassReader(writer.toByteArray()).accept(rebuilt, 0);
        MethodNode updated = rebuilt.methods.get(0);
        if (!Arrays.equals(instructions(method), instructions(updated))) {
            throw new IllegalStateException("Frame repair changed instructions: " + owner.name + "." + method.name);
        }
        return updated;
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[0]);
        Hierarchy hierarchy = new Hierarchy();
        List<String> changes = new ArrayList<>();
        for (String name : arguments.length > 2 ? List.of(arguments[2]) : List.of(SharedCombatCadencePatch.BOSS, SharedCombatCadencePatch.GOAL)) {
            Path file = payload.resolve(name + ".class");
            ClassNode type = new ClassNode();
            new ClassReader(Files.readAllBytes(file)).accept(type, 0);
            for (int index = 0; index < type.methods.size(); index++) {
                MethodNode method = type.methods.get(index);
                if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) != 0) continue;
                boolean branches = false, frames = false;
                for (AbstractInsnNode instruction : method.instructions) {
                    branches |= instruction instanceof JumpInsnNode || instruction instanceof TableSwitchInsnNode
                            || instruction instanceof LookupSwitchInsnNode;
                    frames |= instruction instanceof FrameNode;
                }
                boolean modified = name.equals(SharedCombatCadencePatch.BOSS) ? method.name.equals("m_8119_")
                        : Set.of("m_8037_", "m_6739_", "m_8056_").contains(method.name);
                if (arguments.length > 3) modified |= Set.of(arguments[3].split(",")).contains(method.name);
                if ((branches && !frames) || modified) {
                    type.methods.set(index, repair(type, method, hierarchy));
                    changes.add(name + "." + method.name + method.desc);
                }
            }
            ClassWriter writer = new ClassWriter(0);
            type.accept(writer);
            Files.write(file, writer.toByteArray());
        }
        Files.write(Path.of(arguments[1]), changes);
        System.out.println("PASS StackMap repair: " + changes.size() + " methods; executable instructions unchanged");
    }
}
