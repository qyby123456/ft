import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class PatchAttachmentEligibility {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String POLICY = PREFIX + "item/WeaponAttachmentPolicy";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";

    private static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getEntry(PREFIX + name + ".class"))).accept(type, ClassReader.EXPAND_FRAMES);
        return type;
    }

    private static MethodNode method(ClassNode type, String name, String descriptor) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)
                && (descriptor == null || candidate.desc.equals(descriptor))).findFirst().orElseThrow();
    }

    private static void transplant(ClassNode original, ClassNode donor, String name, String descriptor) {
        MethodNode replacement = method(donor, name, descriptor);
        original.methods.removeIf(candidate -> candidate.name.equals(name) && candidate.desc.equals(replacement.desc));
        original.methods.add(replacement);
    }

    private static InsnList guard(int local, int returnOpcode, Object... locals) {
        LabelNode proceed = new LabelNode();
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, local));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, POLICY, "excluded", "(" + STACK + ")Z", false));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
        instructions.add(new InsnNode(returnOpcode == Opcodes.ARETURN ? Opcodes.ACONST_NULL : Opcodes.ICONST_0));
        instructions.add(new InsnNode(returnOpcode));
        instructions.add(proceed);
        instructions.add(new FrameNode(Opcodes.F_NEW, locals.length, locals, 0, null));
        return instructions;
    }

    private static void write(ClassNode type, Path output) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = output.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (ZipFile original = new ZipFile(arguments[0]); ZipFile donor = new ZipFile(arguments[1])) {
            ClassNode rules = read(original, "util/SwordCompatibilityRules");
            ClassNode donorRules = read(donor, "util/SwordCompatibilityRules");
            for (String name : List.of("attachmentsExcluded", "supports", "profile")) {
                transplant(rules, donorRules, name, "(Ljava/lang/String;)" + (name.equals("profile")
                        ? "Lnet/farmingtales/ftboss/util/SwordFunctionRules$Profile;" : "Z"));
            }
            write(rules, output);
            ClassNode api = read(original, "api/SwordCavityExtensions");
            transplant(api, read(donor, "api/SwordCavityExtensions"), "supports", "(" + STACK + ")Z");
            write(api, output);
            ClassNode adapter = read(original, "item/FourWeaponFoundationAdapter");
            method(adapter, "applies", null).instructions.insert(guard(0, Opcodes.IRETURN, "net/minecraft/world/item/ItemStack"));
            method(adapter, "nativeProfile", null).instructions.insert(guard(0, Opcodes.ARETURN, "net/minecraft/world/item/ItemStack"));
            write(adapter, output);
            ClassNode material = read(original, "item/WeaponMaterialBuild");
            MethodNode materials = method(material, "materials", null);
            LabelNode proceed = new LabelNode();
            InsnList exemption = new InsnList();
            exemption.add(new VarInsnNode(Opcodes.ALOAD, 0));
            exemption.add(new MethodInsnNode(Opcodes.INVOKESTATIC, POLICY, "excluded", "(" + STACK + ")Z", false));
            exemption.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
            exemption.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PREFIX + "util/FoodWeaponBuildRules$Part", "values",
                    "()[Lnet/farmingtales/ftboss/util/FoodWeaponBuildRules$Part;", false));
            exemption.add(new InsnNode(Opcodes.ARRAYLENGTH));
            exemption.add(new FieldInsnNode(Opcodes.GETSTATIC, PREFIX + "util/FoodWeaponBuildRules$Material", "ORIGINAL",
                    "Lnet/farmingtales/ftboss/util/FoodWeaponBuildRules$Material;"));
            exemption.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Collections", "nCopies", "(ILjava/lang/Object;)Ljava/util/List;", false));
            exemption.add(new InsnNode(Opcodes.ARETURN));
            exemption.add(proceed);
            exemption.add(new FrameNode(Opcodes.F_NEW, 1, new Object[]{"net/minecraft/world/item/ItemStack"}, 0, null));
            materials.instructions.insert(exemption);
            MethodNode replace = method(material, "replaceFromUi", null);
            AbstractInsnNode weapon = null;
            for (var instruction : replace.instructions) {
                if (instruction instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ASTORE && variable.var == 2) {
                    weapon = instruction;
                    break;
                }
            }
            if (weapon == null) throw new IllegalStateException("Held weapon local missing");
            InsnList editGuard = guard(2, Opcodes.IRETURN, "net/minecraft/server/level/ServerPlayer",
                    Opcodes.INTEGER, "net/minecraft/world/item/ItemStack");
            replace.instructions.insert(weapon, editGuard);
            write(material, output);
            ClassNode hub = read(original, "client/StatueHubScreen");
            transplant(hub, read(donor, "client/StatueHubScreen"), "renderAttachmentExemption", null);
            MethodNode render = method(hub, "renderMaterials", null);
            InsnList renderGuard = new InsnList();
            LabelNode renderNormal = new LabelNode();
            renderGuard.add(new VarInsnNode(Opcodes.ALOAD, 0));
            renderGuard.add(new VarInsnNode(Opcodes.ALOAD, 1));
            for (int local = 2; local <= 4; local++) renderGuard.add(new VarInsnNode(Opcodes.ILOAD, local));
            renderGuard.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, hub.name, "renderAttachmentExemption",
                    "(Lnet/minecraft/client/gui/GuiGraphics;III)Z", false));
            renderGuard.add(new JumpInsnNode(Opcodes.IFEQ, renderNormal));
            renderGuard.add(new InsnNode(Opcodes.RETURN));
            renderGuard.add(renderNormal);
            renderGuard.add(new FrameNode(Opcodes.F_NEW, 8, new Object[]{hub.name, "net/minecraft/client/gui/GuiGraphics",
                    Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER}, 0, null));
            render.instructions.insert(renderGuard);
            method(hub, "canReplaceMaterial", null).instructions.insert(guard(1, Opcodes.IRETURN, hub.name,
                    "net/minecraft/world/item/ItemStack", PREFIX + "util/FoodWeaponBuildRules$Build"));
            write(hub, output);
            String policy = POLICY + ".class";
            Path policyFile = output.resolve(policy);
            Files.createDirectories(policyFile.getParent());
            Files.write(policyFile, donor.getInputStream(donor.getEntry(policy)).readAllBytes());
            System.out.println("PASS: restored supports ABI; guarded melee, foundation, legacy edits, stale materials and UI; other runtime methods retained");
        }
    }
}
