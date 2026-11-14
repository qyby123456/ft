import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class PatchCataclysmPanels {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String HELPER = PREFIX + "item/CataclysmCavityAccess";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String PLAYER = "Lnet/minecraft/server/level/ServerPlayer;";
    private static final String POSITION = "Lnet/minecraft/core/BlockPos;";

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (ZipFile baseline = new ZipFile(arguments[0]); ZipFile donor = new ZipFile(arguments[1])) {
            String name = PREFIX + "network/StatueMenuNetwork.class";
            ClassNode network = new ClassNode();
            new ClassReader(baseline.getInputStream(baseline.getEntry(name))).accept(network, ClassReader.EXPAND_FRAMES);
            MethodNode open = network.methods.stream().filter(method -> method.name.equals("open")
                    && method.desc.equals("(" + PLAYER + POSITION + ")V")).findFirst().orElseThrow();
            if (Arrays.stream(open.instructions.toArray()).anyMatch(instruction -> instruction instanceof MethodInsnNode call
                    && call.owner.equals(HELPER))) throw new IllegalStateException("Panel entry already installed");
            var anchors = Arrays.stream(open.instructions.toArray()).filter(instruction -> instruction instanceof MethodInsnNode call
                    && call.owner.equals("net/minecraft/server/level/ServerPlayer") && call.name.equals("m_150109_")).toList();
            if (anchors.size() != 1) throw new IllegalStateException("Expected one inventory synchronization anchor");
            AbstractInsnNode anchor = anchors.get(0).getPrevious();
            if (!(anchor instanceof VarInsnNode load) || load.getOpcode() != Opcodes.ALOAD || load.var != 0) {
                throw new IllegalStateException("Unexpected inventory synchronization shape");
            }
            InsnList seeds = new InsnList();
            for (String hand : new String[]{"m_21205_", "m_21206_"}) {
                seeds.add(new VarInsnNode(Opcodes.ALOAD, 0));
                seeds.add(new VarInsnNode(Opcodes.ALOAD, 0));
                seeds.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/level/ServerPlayer", hand, "()" + STACK, false));
                seeds.add(new VarInsnNode(Opcodes.ALOAD, 2));
                seeds.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "seedFromStatue", "(" + PLAYER + STACK + POSITION + ")Z", false));
                seeds.add(new InsnNode(Opcodes.POP));
            }
            open.instructions.insertBefore(anchor, seeds);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            network.accept(writer);
            Path networkPath = output.resolve(name);
            Files.createDirectories(networkPath.getParent());
            Files.write(networkPath, writer.toByteArray());
            Path helperPath = output.resolve(HELPER + ".class");
            Files.createDirectories(helperPath.getParent());
            Files.write(helperPath, donor.getInputStream(donor.getEntry(HELPER + ".class")).readAllBytes());
            System.out.println("PASS: two guarded held-weapon seeds before inventory synchronization; existing network methods retained");
        }
    }
}
