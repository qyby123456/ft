import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;

/** 配件 UI 交付差异门禁；2026-10-20 校验资源不变、交易门禁不变及构造初始化精确增量。 */
public final class DeltaCheck {
    private static String text(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter result = new StringWriter();
        printer.print(new PrintWriter(result));
        return result.toString();
    }
    public static void main(String[] arguments) throws Exception {
        Map<String, Set<String>> allowed = Map.of("net/farmingtales/ftboss/api/SwordMaterialExtensions.class", Set.of("foundationExtensionsSnapshot", "foundationRuleSnapshot"),
                "net/farmingtales/ftboss/item/FourWeaponFoundationAdapter.class", Set.of("authorRulesSnapshot", "read", "rulesFingerprint"),
                "net/farmingtales/ftboss/client/SwordCavityContainerScreen.class", Set.of("render", "renderLabels", "m_88315_", "m_280003_"),
                "net/farmingtales/ftboss/client/SwordCavityBondPanel.class", Set.of("render"),
                "net/farmingtales/ftboss/menu/SwordCavityMenu.class", Set.of(),
                "net/farmingtales/ftboss/transaction/TransactionLifecycle.class", Set.of("tick"));
        int unchanged = 0;
        int unchangedMethods = 0;
        Set<String> changed = new TreeSet<>();
        try (var before = new JarFile(arguments[0]); var after = new JarFile(arguments[1])) {
            for (var entry : before.stream().toList()) {
                var current = after.getJarEntry(entry.getName());
                if (current == null) throw new AssertionError("Removed entry: " + entry.getName());
                byte[] oldBytes = before.getInputStream(entry).readAllBytes();
                byte[] newBytes = after.getInputStream(current).readAllBytes();
                if (Arrays.equals(oldBytes, newBytes)) { unchanged++; continue; }
                changed.add(entry.getName());
                if (!allowed.containsKey(entry.getName())) throw new AssertionError("Unexpected entry delta: " + entry.getName());
                ClassNode oldClass = new ClassNode();
                ClassNode newClass = new ClassNode();
                new ClassReader(oldBytes).accept(oldClass, ClassReader.SKIP_DEBUG);
                new ClassReader(newBytes).accept(newClass, ClassReader.SKIP_DEBUG);
                if (entry.getName().endsWith("/SwordCavityMenu.class")) {
                    MethodPatch.initializeBinding(oldClass);
                    boolean srg = newClass.methods.stream().anyMatch(method -> method.name.equals("m_6875_"));
                    if (srg) for (MethodNode method : oldClass.methods) {
                        if (!method.name.equals("<init>")) continue;
                        for (AbstractInsnNode instruction : method.instructions.toArray()) {
                            if (instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraft/world/entity/player/Inventory") && field.name.equals("player")) field.name = "f_35978_";
                            if (instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraft/world/entity/player/Player") && call.name.equals("level")) call.name = "m_9236_";
                            if (instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraft/world/level/Level") && call.name.equals("dimension")) call.name = "m_46472_";
                        }
                    }
                }
                int addedMethods = entry.getName().endsWith("/SwordMaterialExtensions.class") ? 1 : 0;
                int addedFields = entry.getName().endsWith("/SwordCavityBondPanel.class") ? 2 : 0;
                if (oldClass.methods.size() + addedMethods != newClass.methods.size() || oldClass.fields.size() + addedFields != newClass.fields.size()) throw new AssertionError("Class shape changed");
                for (int index = 0; index < oldClass.fields.size(); index++) {
                    FieldNode previous = oldClass.fields.get(index);
                    FieldNode currentField = newClass.fields.get(index);
                    if (!previous.name.equals(currentField.name) || !previous.desc.equals(currentField.desc)
                            || previous.access != currentField.access || !Objects.equals(previous.value, currentField.value)) throw new AssertionError("Existing field changed");
                }
                for (int index = 0; index < oldClass.methods.size(); index++) {
                    MethodNode oldMethod = oldClass.methods.get(index);
                    MethodNode newMethod = newClass.methods.get(index);
                    if (!oldMethod.name.equals(newMethod.name) || !oldMethod.desc.equals(newMethod.desc) || oldMethod.access != newMethod.access) throw new AssertionError("Method shape changed");
                    if (!allowed.get(entry.getName()).contains(oldMethod.name)) {
                        if (!text(oldMethod).equals(text(newMethod))) throw new AssertionError("Unexpected method change: " + oldMethod.name);
                        unchangedMethods++;
                    } else if (entry.getName().endsWith("/FourWeaponFoundationAdapter.class") && oldMethod.name.equals("read")) {
                        int replaced = 0;
                        for (AbstractInsnNode instruction : oldMethod.instructions.toArray()) {
                            if (instruction instanceof MethodInsnNode call && call.owner.equals(oldClass.name) && call.name.equals("authorRulesSnapshot")) {
                                AbstractInsnNode next = call.getNext();
                                if (next.getOpcode() != Opcodes.POP) throw new AssertionError("Unexpected read context");
                                oldMethod.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                        "net/farmingtales/ftboss/util/foundations/FoundationAuthorRuleSnapshots", "validate",
                                        "(Lnet/minecraft/world/item/ItemStack;)V", false));
                                oldMethod.instructions.remove(next);
                                replaced++;
                            }
                        }
                        if (replaced != 1 || !text(oldMethod).equals(text(newMethod))) throw new AssertionError("Weapon read guards changed outside author validation");
                    }
                }
            }
            if (!changed.equals(allowed.keySet())) throw new AssertionError("Missing expected changes");
            var added = after.stream().filter(entry -> before.getJarEntry(entry.getName()) == null).map(JarEntry::getName).toList();
            if (!new HashSet<>(added).equals(Set.of("net/farmingtales/ftboss/util/foundations/FoundationAuthorRuleSnapshots.class", "net/farmingtales/ftboss/util/foundations/FoundationAuthorRuleSnapshots$FrozenRules.class", "net/farmingtales/ftboss/util/foundations/FoundationAuthorRuleSnapshots$MaterialSnapshot.class", "net/farmingtales/ftboss/client/CavityPresentationCache.class", "net/farmingtales/ftboss/client/CavityPresentationCache$Frame.class"))) throw new AssertionError("Unexpected additions: " + added);
            System.out.println("PASS unchangedEntries=" + unchanged + " unchangedOrExactlyReconstructedMethods=" + unchangedMethods + " changedMethods=9 addedMethod=1 addedFields=2 newHelperClasses=5 modelsChanged=0");
        }
    }
}
