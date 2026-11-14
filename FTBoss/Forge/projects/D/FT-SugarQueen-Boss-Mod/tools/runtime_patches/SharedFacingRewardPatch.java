import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class SharedFacingRewardPatch {
    public static final String RENDERER = "net/farmingtales/ftboss/client/render/SpellVisualRenderer";
    public static final String FACING = "net/farmingtales/ftboss/client/render/SpellModelFacing";
    public static final String DROPS = "net/farmingtales/ftboss/boss/ai/NonManorLootCrateDrops";
    public static final String REWARDS = "net/farmingtales/ftboss/boss/ai/FoodBossInventoryRewards";
    public static final String CLAIMS = "net/farmingtales/ftboss/boss/ai/FoodRewardClaims";
    public static final String CHEST = "net/farmingtales/ftboss/block/ManorLootChestBlock";
    private static final String PACKET = "net/farmingtales/ftboss/skill/SpellVisualPacket";
    private static final String VEC = "net/minecraft/world/phys/Vec3";
    private static final String DEATH = "(Lnet/minecraftforge/event/entity/living/LivingDeathEvent;)V";

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (ZipFile archive = new ZipFile(arguments[0]); ZipFile donor = new ZipFile(arguments[1])) {
            ClassNode renderer = read(archive, RENDERER);
            MethodNode models = method(renderer, "renderModels", null);
            if (calls(models, FACING, "direction") != 0) throw new IllegalStateException("Facing already patched; do not overwrite blindly");
            int packetSlot = -1, ownerSlot = -1, directionSlot = -1;
            AbstractInsnNode flatten = null;
            for (AbstractInsnNode instruction : models.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                if (call.owner.equals(RENDERER) && call.name.equals("owner")) {
                    ownerSlot = ((VarInsnNode) next(call)).var;
                }
                if (call.owner.equals(PACKET) && call.name.equals("end") && packetSlot < 0) {
                    packetSlot = ((VarInsnNode) previous(call)).var;
                }
                if (call.owner.equals(VEC) && call.name.equals("m_82546_")) {
                    directionSlot = ((VarInsnNode) next(call)).var;
                }
                if (call.owner.equals(VEC) && call.name.equals("m_82542_") && call.desc.equals("(DDD)L" + VEC + ";") && flatten == null) {
                    AbstractInsnNode cursor = previous(call);
                    for (int count = 0; count < 5; count++) cursor = previous(cursor);
                    flatten = cursor;
                }
            }
            if (packetSlot < 0 || ownerSlot < 0 || directionSlot < 0 || flatten == null || flatten.getOpcode() != Opcodes.ILOAD) {
                throw new IllegalStateException("Unknown renderModels layout; must inspect before rebasing");
            }
            InsnList facing = new InsnList();
            facing.add(new VarInsnNode(Opcodes.ALOAD, packetSlot));
            facing.add(new VarInsnNode(Opcodes.ALOAD, ownerSlot));
            facing.add(new VarInsnNode(Opcodes.FLOAD, 4));
            facing.add(new MethodInsnNode(Opcodes.INVOKESTATIC, FACING, "direction", "(L" + PACKET
                    + ";Lnet/minecraft/world/entity/Entity;F)L" + VEC + ";", false));
            facing.add(new VarInsnNode(Opcodes.ASTORE, directionSlot));
            renderer.methods.forEach(candidate -> {
                if (candidate.name.equals("renderModels") && candidate != models) throw new IllegalStateException("Ambiguous renderModels");
            });
            models.instructions.insertBefore(flatten, facing);
            ClassNode drops = read(archive, DROPS);
            MethodNode death = method(drops, "onBossDeath", DEATH);
            death.instructions.clear();
            death.tryCatchBlocks.clear();
            if (death.localVariables != null) death.localVariables.clear();
            death.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            death.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REWARDS, "onBossDeath", DEATH, false));
            death.instructions.add(new InsnNode(Opcodes.RETURN));
            death.maxLocals = 1;
            ClassNode chest = read(archive, CHEST);
            MethodNode use = chest.methods.stream().filter(candidate -> candidate.name.equals("m_6227_")).findFirst().orElseThrow();
            if (calls(use, REWARDS, "claimChest") != 0) throw new IllegalStateException("Chest already patched");
            int insertions = 0;
            for (AbstractInsnNode instruction : use.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraft/world/entity/player/Player")
                        && call.name.equals("m_5893_")) {
                    AbstractInsnNode chestLoad = previous(call), playerLoad = previous(chestLoad);
                    if (!(chestLoad instanceof VarInsnNode chestVariable) || chestLoad.getOpcode() != Opcodes.ALOAD
                            || !(playerLoad instanceof VarInsnNode playerVariable) || playerLoad.getOpcode() != Opcodes.ALOAD) {
                        throw new IllegalStateException("Unknown chest opening layout");
                    }
                    InsnList claim = new InsnList();
                    claim.add(new VarInsnNode(Opcodes.ALOAD, chestVariable.var));
                    claim.add(new VarInsnNode(Opcodes.ALOAD, playerVariable.var));
                    claim.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REWARDS, "claimChest",
                            "(Lnet/farmingtales/ftboss/block/ManorLootChestBlockEntity;Lnet/minecraft/world/entity/player/Player;)V", false));
                    use.instructions.insertBefore(playerLoad, claim);
                    insertions++;
                }
            }
            if (insertions != 1) throw new IllegalStateException("Expected exactly one existing chest open call");
            write(renderer, output);
            write(drops, output);
            write(chest, output);
            for (String name : List.of(FACING, REWARDS, CLAIMS)) {
                write(read(donor, name), output);
            }
            System.out.println("PASS three method-level merges and three new helper classes; packet/owner/direction slots="
                    + packetSlot + "/" + ownerSlot + "/" + directionSlot);
        }
    }

    private static long calls(MethodNode method, String owner, String name) {
        return java.util.Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction instanceof MethodInsnNode call
                && call.owner.equals(owner) && call.name.equals(name)).count();
    }

    private static AbstractInsnNode previous(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    private static AbstractInsnNode next(AbstractInsnNode instruction) {
        do { instruction = instruction.getNext(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    private static MethodNode method(ClassNode type, String name, String descriptor) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name) && (descriptor == null || candidate.desc.equals(descriptor)))
                .findFirst().orElseThrow(() -> new IllegalStateException("Missing method " + type.name + "." + name));
    }

    private static ClassNode read(ZipFile archive, String name) throws Exception {
        var entry = archive.getEntry(name + ".class");
        if (entry == null) throw new IllegalStateException("Missing class " + name);
        ClassNode type = new ClassNode();
        try (var input = archive.getInputStream(entry)) { new ClassReader(input.readAllBytes()).accept(type, 0); }
        return type;
    }

    private static void write(ClassNode type, Path output) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path target = output.resolve(type.name + ".class");
        Files.createDirectories(target.getParent());
        Files.write(target, writer.toByteArray());
    }
}
