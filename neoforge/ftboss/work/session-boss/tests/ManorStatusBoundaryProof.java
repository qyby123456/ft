import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class ManorStatusBoundaryProof {
    private static final String OWNER = "net/farmingtales/ftboss/boss/ai/ManorStatusImmunity";
    private static final String DESC = "(ZLnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;)Z";

    private static ClassNode read(byte[] bytes, int options) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, options);
        return node;
    }

    private static List<String> instructions(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        return printer.getText().stream().map(Object::toString).toList();
    }

    public static void main(String[] arguments) throws Exception {
        Path input = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        ClassNode node = read(Files.readAllBytes(input), 0);
        MethodNode affected = node.methods.stream().filter(method -> method.name.equals("canBeAffected") && method.desc.equals("(Lnet/minecraft/world/effect/MobEffectInstance;)Z")).findFirst().orElseThrow();
        int inserted = 0;
        for (AbstractInsnNode instruction : affected.instructions.toArray()) {
            if (instruction.getOpcode() != Opcodes.IRETURN) continue;
            InsnList guard = new InsnList();
            guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
            guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
            guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, "allowed", DESC, false));
            affected.instructions.insertBefore(instruction, guard);
            inserted++;
        }
        if (inserted != 3) throw new AssertionError("Unexpected original NeoForge effect predicate return sites: " + inserted);
        affected.maxStack += 2;
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        Files.write(output, writer.toByteArray());
        ClassNode previous = read(Files.readAllBytes(input), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        ClassNode current = read(Files.readAllBytes(output), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        int verified = 0;
        int removed = 0;
        for (MethodNode method : current.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                new Analyzer<>(new BasicVerifier()).analyze(current.name, method);
                verified++;
            }
            MethodNode before = previous.methods.stream().filter(entry -> entry.name.equals(method.name) && entry.desc.equals(method.desc)).findFirst().orElseThrow();
            if (method.name.equals("canBeAffected") && method.desc.equals(affected.desc)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(OWNER)) continue;
                    if (call.getOpcode() != Opcodes.INVOKESTATIC || !call.name.equals("allowed") || !call.desc.equals(DESC) || call.getNext().getOpcode() != Opcodes.IRETURN) throw new AssertionError("Unexpected guard structure");
                    AbstractInsnNode entity = call.getPrevious().getPrevious();
                    AbstractInsnNode effect = call.getPrevious();
                    if (!(entity instanceof VarInsnNode entityLoad) || entityLoad.getOpcode() != Opcodes.ALOAD || entityLoad.var != 0 || !(effect instanceof VarInsnNode effectLoad) || effectLoad.getOpcode() != Opcodes.ALOAD || effectLoad.var != 1) throw new AssertionError("Wrong original receiver/argument guard");
                    method.instructions.remove(entity);
                    method.instructions.remove(effect);
                    method.instructions.remove(call);
                    removed++;
                }
                method.maxStack = before.maxStack;
            }
            if (!instructions(before).equals(instructions(method))) throw new AssertionError("Unrelated original method changed: " + method.name);
        }
        if (removed != inserted || previous.methods.size() != current.methods.size()) throw new AssertionError("Method coverage mismatch");
        ClassNode guard = read(Files.readAllBytes(Path.of(arguments[2])), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        for (MethodNode method : guard.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(guard.name, method);
            verified++;
        }
        int originalForgeGuards = 0;
        try (ZipFile archive = new ZipFile(arguments[3])) {
            ClassNode originalForge = read(archive.getInputStream(archive.getEntry("net/farmingtales/ftboss/entity/EasterBossEntity.class")).readAllBytes(), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            for (MethodNode method : originalForge.methods) {
                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode call && call.owner.equals(OWNER) && call.name.equals("allowed") && call.desc.equals(DESC)) originalForgeGuards++;
                }
            }
        }
        if (originalForgeGuards != 4) throw new AssertionError("Frozen Forge admission guard evidence drift");
        System.out.println("failures=0;guardedNativeReturnSites=" + inserted + ";frozenForgeGuardSites=" + originalForgeGuards + ";originalEntityMethodsPreserved=" + current.methods.size() + ";basicVerifiedMethods=" + verified + ";onlyCanBeAffectedReturnGuardAdded=true;noOtherBossNumbersOrInstructionsChanged=true");
    }
}
