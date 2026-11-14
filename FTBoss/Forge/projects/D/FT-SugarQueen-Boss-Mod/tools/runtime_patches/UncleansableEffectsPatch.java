import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class UncleansableEffectsPatch {
    public static final String HEAL_BAN = "net/farmingtales/ftboss/effect/HealBanEffect.class";
    public static final String BLESSING = "net/farmingtales/ftboss/boss/ForgeBlessing.class";

    private static ClassNode node(byte[] content) {
        ClassNode type = new ClassNode();
        new ClassReader(content).accept(type, 0);
        return type;
    }

    private static byte[] bytes(ClassNode type) {
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] fingerprint(MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "Fingerprint", null, "java/lang/Object", null);
        method.accept(writer);
        return writer.toByteArray();
    }

    private static Map<String, byte[]> methods(ClassNode type) {
        Map<String, byte[]> methods = new HashMap<>();
        for (MethodNode method : type.methods) methods.put(method.name + method.desc, fingerprint(method));
        return methods;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static byte[] patchEffect(byte[] baseline, byte[] donorBytes) {
        ClassNode type = node(baseline);
        Map<String, byte[]> originals = methods(type);
        ClassNode donor = new ClassNode();
        node(donorBytes).accept(new ClassRemapper(donor, new Remapper() {
            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                if (name.equals("level") && descriptor.equals("()Lnet/minecraft/world/level/Level;")) return "m_9236_";
                return name;
            }
            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                return owner.equals("net/minecraft/world/level/Level") && name.equals("isClientSide") ? "f_46443_" : name;
            }
        }));
        if (type.visibleAnnotations == null) type.visibleAnnotations = new java.util.ArrayList<>();
        for (var annotation : donor.visibleAnnotations) {
            type.visibleAnnotations.removeIf(existing -> existing.desc.equals(annotation.desc));
            type.visibleAnnotations.add(annotation);
        }
        for (String name : new String[]{"getCurativeItems", "onRemove"}) {
            MethodNode replacement = donor.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
            for (MethodNode existing : type.methods) {
                if (existing.name.equals(name)) require(Arrays.equals(fingerprint(existing), fingerprint(replacement)),
                        "Existing effect hook differs; stop and re-audit: " + name);
            }
            type.methods.removeIf(method -> method.name.equals(name));
            type.methods.add(replacement);
        }
        Map<String, byte[]> result = methods(type);
        originals.forEach((name, content) -> {
            if (!name.startsWith("getCurativeItems(") && !name.startsWith("onRemove("))
                require(Arrays.equals(content, result.get(name)), "Unrelated effect method changed: " + name);
        });
        System.out.println("PASS HealBanEffect original constructor/category and healing predicate preserved; only cure/Remove guard added");
        return bytes(type);
    }

    private static byte[] patchBlessing(byte[] baseline) {
        ClassNode type = node(baseline);
        Map<String, byte[]> originals = methods(type);
        MethodNode grant = type.methods.stream().filter(method -> method.name.equals("grant")).findFirst().orElseThrow();
        int changed = 0;
        for (AbstractInsnNode instruction : grant.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || !call.name.equals("m_21195_")
                    || !call.desc.equals("(Lnet/minecraft/world/effect/MobEffect;)Z")) continue;
            AbstractInsnNode discard = call.getNext();
            while (discard.getOpcode() < 0) discard = discard.getNext();
            if (discard instanceof JumpInsnNode existing && existing.getOpcode() == Opcodes.IFEQ) {
                changed++;
                continue;
            }
            require(discard.getOpcode() == Opcodes.POP, "Unexpected blessing count layout");
            AbstractInsnNode increment = discard.getNext();
            while (increment.getOpcode() < 0) increment = increment.getNext();
            require(increment.getOpcode() == Opcodes.IINC, "Expected existing clear counter");
            AbstractInsnNode next = increment.getNext();
            while (next.getOpcode() < 0) next = next.getNext();
            require(next instanceof JumpInsnNode && next.getOpcode() == Opcodes.GOTO, "Expected existing loop target");
            grant.instructions.set(discard, new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode) next).label));
            changed++;
        }
        require(changed == 1, "Expected one blessing removal site");
        Map<String, byte[]> result = methods(type);
        originals.forEach((name, content) -> {
            if (!name.equals(grant.name + grant.desc)) require(Arrays.equals(content, result.get(name)), "Other blessing method changed");
        });
        System.out.println("PASS ForgeBlessing only counts successful removals; existing loop target and frames retained");
        return bytes(type);
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (ZipFile archive = new ZipFile(arguments[0])) {
            byte[] originalEffect = archive.getInputStream(archive.getEntry(HEAL_BAN)).readAllBytes();
            byte[] donor = Files.readAllBytes(Path.of(arguments[1]));
            byte[] effect = patchEffect(originalEffect, donor);
            require(Arrays.equals(effect, patchEffect(effect, donor)), "Effect patch not idempotent");
            byte[] blessing = patchBlessing(archive.getInputStream(archive.getEntry(BLESSING)).readAllBytes());
            require(Arrays.equals(blessing, patchBlessing(blessing)), "Blessing patch not idempotent");
            for (var entry : Map.of(HEAL_BAN, effect, BLESSING, blessing).entrySet()) {
                Path path = output.resolve(entry.getKey());
                Files.createDirectories(path.getParent());
                Files.write(path, entry.getValue());
            }
        }
    }
}
