import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class PatchCavityReads {
    static final String TYPE = "net/farmingtales/ftboss/item/SwordCavityBuild";
    static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    static final String LIST = "Ljava/util/List;";
    public static void main(String[] arguments) throws Exception {
        byte[] original;
        try (ZipFile archive = new ZipFile(arguments[0])) {
            original = archive.getInputStream(archive.getEntry(TYPE + ".class")).readAllBytes();
        }
        ClassReader reader = new ClassReader(original);
        ClassNode target = new ClassNode();
        reader.accept(target, 0);
        ClassNode template = new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(TYPE + ".class"))).accept(template, 0);
        MethodNode effective = find(target, "effectiveCells", "(" + STACK + ")" + LIST);
        long checks = Arrays.stream(effective.instructions.toArray()).filter(instruction -> instruction instanceof MethodInsnNode call
                && call.owner.equals(TYPE) && call.name.equals("cellUnlocked")).count();
        require(checks == 1, "Unexpected effectiveCells implementation");
        require(target.methods.stream().noneMatch(method -> method.name.equals("legacyMaterialValues")), "Helper already exists");
        target.methods.set(target.methods.indexOf(effective), find(template, "effectiveCells", effective.desc));
        target.methods.add(find(template, "legacyMaterialValues", "(" + LIST + ")" + LIST));
        MethodNode legacy = find(target, "legacyBuild", "(" + STACK + ")Lnet/farmingtales/ftboss/util/FoodWeaponBuildRules$Build;");
        int replaced = 0;
        for (AbstractInsnNode instruction : legacy.instructions) {
            if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(TYPE) || !call.name.equals("legacyMaterials")) continue;
            require(call.desc.equals("(" + STACK + ")" + LIST), "Unexpected material descriptor");
            AbstractInsnNode load = call.getPrevious();
            while (load != null && load.getOpcode() < 0) load = load.getPrevious();
            require(load instanceof VarInsnNode local && local.getOpcode() == Opcodes.ALOAD && local.var == 0, "Unexpected stack load");
            ((VarInsnNode) load).var = 1;
            call.name = "legacyMaterialValues";
            call.desc = "(" + LIST + ")" + LIST;
            replaced++;
        }
        require(replaced == 1, "Expected exactly one legacy read replacement");
        ClassWriter writer = new ClassWriter(reader, 0);
        target.accept(writer);
        Path output = Path.of(arguments[2]).resolve(TYPE + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("PATCHED effectiveCells + legacyBuild; private pure legacyMaterialValues helper added");
    }
    static MethodNode find(ClassNode type, String name, String descriptor) {
        return type.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).findFirst().orElseThrow();
    }
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
