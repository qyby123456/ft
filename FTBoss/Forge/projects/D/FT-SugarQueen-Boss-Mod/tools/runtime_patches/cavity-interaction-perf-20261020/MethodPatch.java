import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** 配件 UI 最小方法移植；2026-10-20 保留完整生产类，只追加明确审计过的字段初始化。 */
public final class MethodPatch {
    private static final Map<String, Set<String>> METHODS = Map.of(
            "net/farmingtales/ftboss/api/SwordMaterialExtensions.class", Set.of("foundationExtensionsSnapshot", "foundationRuleSnapshot"),
            "net/farmingtales/ftboss/item/FourWeaponFoundationAdapter.class", Set.of("authorRulesSnapshot", "rulesFingerprint"),
            "net/farmingtales/ftboss/client/SwordCavityContainerScreen.class", Set.of("render", "renderLabels"),
            "net/farmingtales/ftboss/client/SwordCavityBondPanel.class", Set.of("render"),
            "net/farmingtales/ftboss/menu/SwordCavityMenu.class", Set.of(),
            "net/farmingtales/ftboss/transaction/TransactionLifecycle.class", Set.of("tick"));

    public static void main(String[] arguments) throws Exception {
        Map<String, byte[]> replacements = new LinkedHashMap<>();
        Path directory = Path.of(arguments[1]);
        try (var files = Files.walk(directory); var baseline = new JarFile(arguments[0])) {
            for (Path path : files.filter(file -> file.toString().endsWith(".class")).toList()) {
                String name = directory.relativize(path).toString().replace('\\', '/');
                if (name.equals("net/farmingtales/ftboss/client/SwordCavityBondPanel$Entry.class")) continue;
                if (name.startsWith("net/farmingtales/ftboss/menu/SwordCavityMenu$")) continue;
                if (name.startsWith("net/farmingtales/ftboss/item/FourWeaponFoundationAdapter$")) continue;
                byte[] bytes = Files.readAllBytes(path);
                Set<String> methodNames = METHODS.get(name);
                if (arguments.length == 3 && methodNames != null) {
                    ClassNode target = new ClassNode();
                    new ClassReader(baseline.getInputStream(baseline.getJarEntry(name)).readAllBytes()).accept(target, 0);
                    ClassNode source = new ClassNode();
                    new ClassReader(bytes).accept(source, 0);
                    if (name.endsWith("/SwordCavityBondPanel.class")) {
                        for (String fieldName : List.of("displayedWeapon", "displayedEntries")) {
                            if (target.fields.stream().anyMatch(field -> field.name.equals(fieldName))) throw new IllegalStateException("Cache field already exists");
                            target.fields.add(source.fields.stream().filter(field -> field.name.equals(fieldName)).findFirst().orElseThrow());
                        }
                    }
                    for (String methodName : methodNames) {
                        MethodNode replacement = source.methods.stream().filter(method -> method.name.equals(methodName)).findFirst().orElseThrow();
                        int index = -1;
                        for (int position = 0; position < target.methods.size(); position++) {
                            MethodNode method = target.methods.get(position);
                            if (method.name.equals(methodName) && method.desc.equals(replacement.desc)) index = position;
                        }
                        if (index < 0) {
                            if (!methodName.equals("foundationRuleSnapshot")) throw new IllegalStateException("Missing method: " + name);
                            target.methods.add(replacement);
                        } else target.methods.set(index, replacement);
                    }
                    if (name.endsWith("/FourWeaponFoundationAdapter.class")) {
                        int replaced = 0;
                        MethodNode read = target.methods.stream().filter(method -> method.name.equals("read")).findFirst().orElseThrow();
                        for (AbstractInsnNode instruction : read.instructions.toArray()) {
                            if (instruction instanceof MethodInsnNode call && call.owner.equals(target.name) && call.name.equals("authorRulesSnapshot")) {
                                AbstractInsnNode next = instruction.getNext();
                                if (next.getOpcode() != Opcodes.POP) throw new IllegalStateException("Read no longer discards author snapshot");
                                read.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                        "net/farmingtales/ftboss/util/foundations/FoundationAuthorRuleSnapshots", "validate",
                                        "(Lnet/minecraft/world/item/ItemStack;)V", false));
                                read.instructions.remove(next);
                                replaced++;
                            }
                        }
                        if (replaced != 1) throw new IllegalStateException("Expected exactly one read author validation");
                    }
                    if (name.endsWith("/SwordCavityMenu.class")) initializeBinding(target);
                    ClassWriter writer = new ClassWriter(0);
                    target.accept(writer);
                    bytes = writer.toByteArray();
                }
                replacements.put(name, bytes);
            }
            try (var output = new JarOutputStream(Files.newOutputStream(Path.of(arguments[2])))) {
                for (var entry : baseline.stream().toList()) {
                    output.putNextEntry(new JarEntry(entry.getName()));
                    byte[] replacement = replacements.remove(entry.getName());
                    output.write(replacement == null ? baseline.getInputStream(entry).readAllBytes() : replacement);
                    output.closeEntry();
                }
                for (var entry : replacements.entrySet()) {
                    output.putNextEntry(new JarEntry(entry.getKey()));
                    output.write(entry.getValue());
                    output.closeEntry();
                }
            }
        }
    }

    public static void initializeBinding(ClassNode target) {
        MethodNode constructor = target.methods.stream().filter(method -> method.name.equals("<init>")
                && method.desc.equals("(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/core/BlockPos;ILjava/util/UUID;[I)V")).findFirst().orElseThrow();
        for (AbstractInsnNode instruction : constructor.instructions.toArray()) {
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.PUTFIELD
                    && Set.of("boundSource", "expectedFingerprint", "boundDimension").contains(field.name)) throw new IllegalStateException("Binding already initialized; re-audit required");
        }
        int inserted = 0;
        for (AbstractInsnNode instruction : constructor.instructions.toArray()) {
            if (instruction.getOpcode() != Opcodes.RETURN) continue;
            InsnList initialize = new InsnList();
            initialize.add(new VarInsnNode(Opcodes.ALOAD, 0));
            initialize.add(new VarInsnNode(Opcodes.ALOAD, 0));
            initialize.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, target.name, "weapon", "()Lnet/minecraft/world/item/ItemStack;", false));
            initialize.add(new FieldInsnNode(Opcodes.PUTFIELD, target.name, "boundSource", "Lnet/minecraft/world/item/ItemStack;"));
            initialize.add(new VarInsnNode(Opcodes.ALOAD, 0));
            initialize.add(new VarInsnNode(Opcodes.ALOAD, 0));
            initialize.add(new FieldInsnNode(Opcodes.GETFIELD, target.name, "boundSource", "Lnet/minecraft/world/item/ItemStack;"));
            initialize.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/farmingtales/ftboss/transaction/ForgeTransactionHost", "fingerprint", "(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;", false));
            initialize.add(new FieldInsnNode(Opcodes.PUTFIELD, target.name, "expectedFingerprint", "Ljava/lang/String;"));
            initialize.add(new VarInsnNode(Opcodes.ALOAD, 0));
            initialize.add(new VarInsnNode(Opcodes.ALOAD, 0));
            initialize.add(new FieldInsnNode(Opcodes.GETFIELD, target.name, "inventory", "Lnet/minecraft/world/entity/player/Inventory;"));
            initialize.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/entity/player/Inventory", "player", "Lnet/minecraft/world/entity/player/Player;"));
            initialize.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/player/Player", "level", "()Lnet/minecraft/world/level/Level;", false));
            initialize.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/level/Level", "dimension", "()Lnet/minecraft/resources/ResourceKey;", false));
            initialize.add(new FieldInsnNode(Opcodes.PUTFIELD, target.name, "boundDimension", "Lnet/minecraft/resources/ResourceKey;"));
            constructor.instructions.insertBefore(instruction, initialize);
            inserted++;
        }
        if (inserted != 1) throw new IllegalStateException("Unexpected constructor exits");
    }
}
