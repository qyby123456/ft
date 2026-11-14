import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class PatchDiamondAlloy {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String MATERIAL = PREFIX + "util/FoodWeaponBuildRules$Material";
    private static final String BUILD = "L" + PREFIX + "util/FoodWeaponBuildRules$Build;";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String POLICY = PREFIX + "item/WeaponDurabilityBuild";
    private static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getEntry(PREFIX + name + ".class"))).accept(type, ClassReader.EXPAND_FRAMES);
        return type;
    }
    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }
    private static void transplant(ClassNode target, ClassNode donor, String name) {
        MethodNode replacement = method(donor, name);
        target.methods.removeIf(candidate -> candidate.name.equals(name) && candidate.desc.equals(replacement.desc));
        target.methods.add(replacement);
    }
    private static void write(ClassNode type, Path directory) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path output = directory.resolve(type.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }
    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (ZipFile original = new ZipFile(arguments[0]); ZipFile donor = new ZipFile(arguments[1])) {
            ClassNode material = read(original, "util/FoodWeaponBuildRules$Material");
            if (material.fields.stream().anyMatch(field -> field.name.equals("DIAMOND_ALLOY"))) throw new IllegalStateException("Alloy already present");
            int ordinal = (int) material.fields.stream().filter(field -> (field.access & Opcodes.ACC_ENUM) != 0).count();
            material.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                    "DIAMOND_ALLOY", "L" + MATERIAL + ";", null, null));
            MethodNode init = method(material, "<clinit>");
            AbstractInsnNode end = Arrays.stream(init.instructions.toArray()).filter(instruction -> instruction.getOpcode() == Opcodes.RETURN).findFirst().orElseThrow();
            InsnList addition = new InsnList();
            addition.add(new TypeInsnNode(Opcodes.NEW, MATERIAL));
            addition.add(new InsnNode(Opcodes.DUP));
            addition.add(new LdcInsnNode("DIAMOND_ALLOY"));
            addition.add(new LdcInsnNode(ordinal));
            for (int index = 0; index < 4; index++) addition.add(new InsnNode(Opcodes.DCONST_1));
            addition.add(new InsnNode(Opcodes.DCONST_0));
            addition.add(new LdcInsnNode(0x79D5CC));
            addition.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, MATERIAL, "<init>", "(Ljava/lang/String;IDDDDDI)V", false));
            addition.add(new FieldInsnNode(Opcodes.PUTSTATIC, MATERIAL, "DIAMOND_ALLOY", "L" + MATERIAL + ";"));
            addition.add(new FieldInsnNode(Opcodes.GETSTATIC, MATERIAL, "$VALUES", "[L" + MATERIAL + ";"));
            addition.add(new LdcInsnNode(ordinal + 1));
            addition.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "copyOf", "([Ljava/lang/Object;I)[Ljava/lang/Object;", false));
            addition.add(new TypeInsnNode(Opcodes.CHECKCAST, "[L" + MATERIAL + ";"));
            addition.add(new InsnNode(Opcodes.DUP));
            addition.add(new LdcInsnNode(ordinal));
            addition.add(new FieldInsnNode(Opcodes.GETSTATIC, MATERIAL, "DIAMOND_ALLOY", "L" + MATERIAL + ";"));
            addition.add(new InsnNode(Opcodes.AASTORE));
            addition.add(new FieldInsnNode(Opcodes.PUTSTATIC, MATERIAL, "$VALUES", "[L" + MATERIAL + ";"));
            init.instructions.insertBefore(end, addition);
            write(material, output);
            ClassNode catalog = read(original, "util/WeaponMaterialCatalogRules");
            MethodNode catalogueInit = method(catalog, "<clinit>");
            AbstractInsnNode catalogueEnd = Arrays.stream(catalogueInit.instructions.toArray()).filter(instruction -> instruction.getOpcode() == Opcodes.RETURN).findFirst().orElseThrow();
            InsnList offering = new InsnList();
            offering.add(new TypeInsnNode(Opcodes.NEW, "java/util/LinkedHashMap"));
            offering.add(new InsnNode(Opcodes.DUP));
            offering.add(new FieldInsnNode(Opcodes.GETSTATIC, catalog.name, "OFFERINGS", "Ljava/util/Map;"));
            offering.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/LinkedHashMap", "<init>", "(Ljava/util/Map;)V", false));
            offering.add(new InsnNode(Opcodes.DUP));
            offering.add(new FieldInsnNode(Opcodes.GETSTATIC, MATERIAL, "DIAMOND_ALLOY", "L" + MATERIAL + ";"));
            offering.add(new LdcInsnNode("kubejs:ne_ingot"));
            offering.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/List", "of", "(Ljava/lang/Object;)Ljava/util/List;", true));
            offering.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true));
            offering.add(new InsnNode(Opcodes.POP));
            offering.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Map", "copyOf", "(Ljava/util/Map;)Ljava/util/Map;", true));
            offering.add(new FieldInsnNode(Opcodes.PUTSTATIC, catalog.name, "OFFERINGS", "Ljava/util/Map;"));
            catalogueInit.instructions.insertBefore(catalogueEnd, offering);
            InsnList group = new InsnList();
            LabelNode normal = new LabelNode();
            group.add(new VarInsnNode(Opcodes.ALOAD, 0));
            group.add(new FieldInsnNode(Opcodes.GETSTATIC, MATERIAL, "DIAMOND_ALLOY", "L" + MATERIAL + ";"));
            group.add(new JumpInsnNode(Opcodes.IF_ACMPNE, normal));
            group.add(new FieldInsnNode(Opcodes.GETSTATIC, catalog.name + "$Group", "INGOT", "L" + catalog.name + "$Group;"));
            group.add(new InsnNode(Opcodes.ARETURN));
            group.add(normal);
            group.add(new FrameNode(Opcodes.F_NEW, 1, new Object[]{MATERIAL}, 0, null));
            method(catalog, "group").instructions.insert(group);
            write(catalog, output);
            for (String[] selection : List.of(new String[]{"util/FoodWeaponBuildRules", "build"},
                    new String[]{"util/FoodWeaponFamilyRules", "build"}, new String[]{"util/SwordCavityRules", "applyMaterials"},
                    new String[]{"item/SwordCavityBuild", "build"}, new String[]{"item/WeaponMaterialBuild", "maxDurability"},
                    new String[]{"mixin/WeaponMaterialDurabilityMixin", "ftboss$materialMaxDamage"})) {
                ClassNode target = read(original, selection[0]);
                transplant(target, read(donor, selection[0]), selection[1]);
                write(target, output);
            }
            ClassNode adapter = read(original, "item/FourWeaponFoundationAdapter");
            transplant(adapter, read(donor, "item/FourWeaponFoundationAdapter"), "durabilityAlloys");
            int fingerprints = 0;
            MethodNode parse = method(adapter, "parse");
            for (var instruction : parse.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call) || !call.name.equals("profileFingerprint")) continue;
                AbstractInsnNode next = call.getNext();
                if (!(next instanceof MethodInsnNode equal) || !equal.owner.equals("java/lang/String") || !equal.name.equals("equals")) {
                    throw new IllegalStateException("Unexpected fingerprint comparison");
                }
                call.owner = PREFIX + "util/foundations/FoundationProfileFingerprints";
                call.name = "matches";
                call.desc = "(Ljava/lang/String;Lnet/farmingtales/ftboss/util/foundations/FourWeaponFoundationRules$Profile;)Z";
                parse.instructions.remove(next);
                fingerprints++;
            }
            if (fingerprints != 1) throw new IllegalStateException("Expected exactly one rules comparison");
            for (var instruction : method(adapter, "combat").instructions.toArray()) {
                if (instruction.getOpcode() != Opcodes.ARETURN) continue;
                InsnList durability = new InsnList();
                durability.add(new VarInsnNode(Opcodes.ALOAD, 0));
                durability.add(new InsnNode(Opcodes.SWAP));
                durability.add(new MethodInsnNode(Opcodes.INVOKESTATIC, POLICY, "combat", "(" + STACK + BUILD + ")" + BUILD, false));
                method(adapter, "combat").instructions.insertBefore(instruction, durability);
            }
            write(adapter, output);
            for (String name : List.of("util/WeaponDurabilityRules", "item/WeaponDurabilityBuild", "util/foundations/FoundationProfileFingerprints")) {
                write(read(donor, name), output);
            }
            System.out.println("PASS: appended one material after " + ordinal + " existing ordinals; precise catalog entry; durability hooks and legacy fingerprint comparison updated");
        }
    }
}
