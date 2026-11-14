import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** 定向移植材料弓修复；2026-10-22 保留运行基线的凭据链、扩展 API 与所有无关方法。 */
public final class PatchMaterialBowCombat implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String BUILD = "L" + BASE + "util/FoodWeaponBuildRules$Build;";
    private static final String NATIVE_ARGS = "(Lnet/minecraft/world/item/Item;" + STACK
            + "Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V";

    private static ClassNode read(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }

    private static void write(Path root, ClassNode type) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path target = root.resolve(type.name + ".class");
        Files.createDirectories(target.getParent());
        Files.write(target, writer.toByteArray());
    }

    private static MethodNode method(ClassNode type, String name, String descriptor) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name) && candidate.desc.equals(descriptor))
                .findFirst().orElseThrow(() -> new AssertionError(type.name + "." + name + descriptor));
    }

    private static void branch(ClassNode type, String method, String helper) {
        MethodNode target = method(type, method, "(" + STACK + ")Ljava/util/List;");
        LabelNode original = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(ALOAD, 0));
        prefix.add(new MethodInsnNode(INVOKESTATIC, BASE + "item/FourWeaponFoundationAdapter", "applies", "(" + STACK + ")Z", false));
        prefix.add(new JumpInsnNode(IFEQ, original));
        prefix.add(new VarInsnNode(ALOAD, 0));
        prefix.add(new MethodInsnNode(INVOKESTATIC, BASE + "item/FoundationAccessoryBonds", helper, "(" + STACK + ")Ljava/util/List;", false));
        prefix.add(new InsnNode(ARETURN));
        prefix.add(original);
        prefix.add(new FrameNode(F_SAME, 0, null, 0, null));
        target.instructions.insert(prefix);
    }

    private static void replace(ClassNode target, ClassNode replacement, Set<String> names) {
        for (String name : names) {
            List<MethodNode> selected = replacement.methods.stream().filter(candidate -> candidate.name.equals(name)).toList();
            if (selected.size() != 1) throw new AssertionError("Ambiguous replacement " + name);
            MethodNode next = selected.get(0);
            target.methods.removeIf(previous -> previous.name.equals(next.name) && previous.desc.equals(next.desc));
            target.methods.add(next);
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path overlay = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0]); JarFile compiled = new JarFile(arguments[1])) {
            for (String name : List.of("item/WeaponMaterialBonuses", "item/FoodWeaponRanged", "client/SwordCavityBondPanel")) {
                String entry = BASE + name + ".class";
                ClassNode target = read(baseline.getInputStream(baseline.getJarEntry(entry)).readAllBytes());
                ClassNode next = read(compiled.getInputStream(compiled.getJarEntry(entry)).readAllBytes());
                replace(target, next, switch (name) {
                    case "item/WeaponMaterialBonuses" -> Set.of("attributes", "materialTooltip", "rangedDamage");
                    case "item/FoodWeaponRanged" -> Set.of("charge");
                    default -> Set.of("entries");
                });
                write(overlay, target);
            }
            for (String name : List.of("api/SwordMaterialExtensions", "api/SwordCavityExtensions", "item/FourWeaponFoundationAdapter",
                    "item/FoodWeaponSourceLedger", "item/FoodWeaponUseBridge")) {
                String entry = BASE + name + ".class";
                ClassNode target = read(baseline.getInputStream(baseline.getJarEntry(entry)).readAllBytes());
                if (name.startsWith("api/")) {
                    branch(target, "activeBonds", name.contains("SwordMaterial") ? "materialBonds" : "cavityBonds");
                    if (name.contains("SwordMaterial")) branch(target, "installed", "installed");
                } else if (name.endsWith("FoundationAdapter")) {
                    MethodNode combat = method(target, "combat", "(" + STACK + ")" + BUILD);
                    int hooks = 0;
                    for (var instruction : combat.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals(BASE + "util/foundations/FourWeaponFoundationRules")
                                && call.name.equals("combat")) {
                            InsnList hook = new InsnList();
                            hook.add(new VarInsnNode(ALOAD, 0));
                            hook.add(new InsnNode(SWAP));
                            hook.add(new MethodInsnNode(INVOKESTATIC, BASE + "item/FoundationAccessoryBonds", "apply", "(" + STACK + BUILD + ")" + BUILD, false));
                            combat.instructions.insert(call, hook);
                            hooks++;
                        }
                    }
                    if (hooks != 1) throw new AssertionError("Foundation link hook count " + hooks);
                } else {
                    boolean ledger = name.endsWith("SourceLedger");
                    MethodNode entrypoint = method(target, ledger ? "nativeKind" : "release", ledger
                            ? "(Lnet/minecraft/world/item/Item;" + STACK + ")L" + BASE + "item/FoodWeaponSourceLedger$NativeKind;" : NATIVE_ARGS);
                    LabelNode original = new LabelNode();
                    InsnList prefix = new InsnList();
                    prefix.add(new VarInsnNode(ALOAD, 0));
                    prefix.add(new VarInsnNode(ALOAD, 1));
                    prefix.add(new MethodInsnNode(INVOKESTATIC, BASE + "item/NativeMaterialBowSupport", "registered", "(Lnet/minecraft/world/item/Item;" + STACK + ")Z", false));
                    prefix.add(new JumpInsnNode(IFEQ, original));
                    if (ledger) {
                        prefix.add(new FieldInsnNode(GETSTATIC, BASE + "item/FoodWeaponSourceLedger$NativeKind", "BOW", "L" + BASE + "item/FoodWeaponSourceLedger$NativeKind;"));
                        prefix.add(new InsnNode(ARETURN));
                    } else {
                        for (int local = 0; local < 4; local++) prefix.add(new VarInsnNode(ALOAD, local));
                        prefix.add(new VarInsnNode(ILOAD, 4));
                        prefix.add(new MethodInsnNode(INVOKESTATIC, BASE + "item/NativeMaterialBowSupport", "release", NATIVE_ARGS, false));
                        prefix.add(new InsnNode(RETURN));
                    }
                    prefix.add(original);
                    prefix.add(new FrameNode(F_SAME, 0, null, 0, null));
                    entrypoint.instructions.insert(prefix);
                }
                write(overlay, target);
            }
        }
        System.out.println("PASS surgical native bow admission, paid foundation links and ranged accessory hooks");
    }
}
