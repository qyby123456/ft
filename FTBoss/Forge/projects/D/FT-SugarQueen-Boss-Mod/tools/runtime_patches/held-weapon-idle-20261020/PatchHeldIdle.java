/** 手持武器空路径性能补丁的移植或回归工具，2026-10-20。 */
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class PatchHeldIdle {
    static final String ITEM = "net/farmingtales/ftboss/item/";
    static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    static final Map<String, String> EXPECTED = Map.of(
            "OwnerStatueWearAdmission", "6af00499b92dc6ed67ba4020aac7220c32e191fbeba704fb19c3c61b93d1227b",
            "NativeFoodWeaponWear", "6a8f89140a52be02ed930a8a1e36fafa97dbf8610e97d86f6de91deecbc8fda7",
            "FoodWeaponEvolution", "e8b18750926da084eefb7553f7b83d5af59bd83d43899e6c1db21409ed224734",
            "FoodSocketPowers", "a77c2fc26353e02c219dd962c29f3f0d5f9ac47eb554d6c0e97e2ee8199345ef",
            "OwnerRepairContinuation", "d610b0ebf6e7161c1947fab5ce48339adf91e1d92ee7b860d889bd242f9b8bd5",
            "FoodWeaponLifecycle", "fd2fbac312a2048a2b5f56da4266be6ee4c5a01fd2634bdd718a197980eabf6f");
    static final Map<String, Set<String>> CHANGED = Map.of(
            "OwnerStatueWearAdmission", Set.of("entered"),
            "NativeFoodWeaponWear", Set.of("settleDamage"),
            "FoodWeaponEvolution", Set.of("socketTypeCount"),
            "FoodSocketPowers", Set.of("flight"),
            "OwnerRepairContinuation", Set.of(),
            "FoodWeaponLifecycle", Set.of("beforeDamageChange"));

    public static void main(String[] arguments) throws Exception {
        try (ZipFile archive = new ZipFile(arguments[0])) {
            for (String name : CHANGED.keySet()) {
                byte[] bytes = archive.getInputStream(archive.getEntry(ITEM + name + ".class")).readAllBytes();
                require(HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)).equals(EXPECTED.get(name)),
                        "Baseline class changed; inspect and revalidate rather than overwrite: " + name);
                ClassReader reader = new ClassReader(bytes);
                ClassNode type = new ClassNode();
                reader.accept(type, 0);
                switch (name) {
                    case "OwnerRepairContinuation" -> {
                        require(type.methods.stream().noneMatch(method -> method.name.equals("noBinding")), "Helper already present");
                        ClassNode template = new ClassNode();
                        new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(ITEM + name + ".class"))).accept(template, 0);
                        type.methods.add(find(template, "noBinding"));
                    }
                    case "OwnerStatueWearAdmission" -> admission(type);
                    case "NativeFoodWeaponWear" -> settle(type);
                    case "FoodWeaponEvolution" -> sockets(type);
                    case "FoodSocketPowers" -> flight(type);
                    case "FoodWeaponLifecycle" -> unchangedDamage(type);
                    default -> throw new AssertionError();
                }
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                type.accept(writer);
                Path output = Path.of(arguments[2]).resolve(type.name + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
                System.out.println("PATCHED " + name);
            }
        }
    }

    static void unchangedDamage(ClassNode type) {
        MethodNode method = find(type, "beforeDamageChange");
        List<AbstractInsnNode> ops = operations(method);
        require(ops.get(3).getOpcode() == Opcodes.IF_ACMPEQ && ops.get(4).getOpcode() == Opcodes.ALOAD
                && call(ops.get(5), type.name, "managed"), "Damage prewrite gate drift");
        InsnList prefix = new InsnList(); LabelNode original = new LabelNode();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new JumpInsnNode(Opcodes.IFNULL, original));
        prefix.add(new VarInsnNode(Opcodes.ILOAD, 1));
        prefix.add(new JumpInsnNode(Opcodes.IFLT, original));
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/item/ItemStack", "m_41773_", "()I", false));
        prefix.add(new VarInsnNode(Opcodes.ILOAD, 1));
        prefix.add(new JumpInsnNode(Opcodes.IF_ICMPNE, original));
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ITEM + "NativeFoodWeaponWear", "statueRepairWrite", "(" + STACK + ")Z", false));
        prefix.add(new JumpInsnNode(Opcodes.IFNE, original));
        prefix.add(new VarInsnNode(Opcodes.ILOAD, 1)); prefix.add(new InsnNode(Opcodes.IRETURN));
        prefix.add(original); prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insertBefore(ops.get(4), prefix);
    }

    static void admission(ClassNode type) {
        MethodNode method = find(type, "entered");
        List<AbstractInsnNode> ops = operations(method);
        require(ops.get(0).getOpcode() == Opcodes.ALOAD && call(ops.get(1), ITEM + "NativeFoodWeaponWear", "eligible"), "Admission prefix drift");
        InsnList prefix = new InsnList();
        LabelNode original = new LabelNode();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ITEM + "OwnerRepairContinuation", "noBinding", "(" + STACK + ")Z", false));
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ITEM + "OwnerNativeSemanticAuthority", "wearOrigin",
                "(" + STACK + ")L" + ITEM + "OwnerStatueWearAdmission$Origin;", false));
        prefix.add(new JumpInsnNode(Opcodes.IFNONNULL, original));
        prefix.add(new InsnNode(Opcodes.ACONST_NULL));
        prefix.add(new InsnNode(Opcodes.ARETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    static void settle(ClassNode type) {
        MethodNode method = find(type, "settleDamage");
        List<AbstractInsnNode> ops = operations(method);
        require(ops.get(0).getOpcode() == Opcodes.ALOAD && call(ops.get(1), type.name, "eligible")
                && ops.get(2).getOpcode() == Opcodes.IFNE && ops.get(3).getOpcode() == Opcodes.RETURN, "Settle prefix drift");
        InsnList prefix = new InsnList();
        LabelNode original = new LabelNode();
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, type.name, "server", "()Lnet/minecraft/server/MinecraftServer;", false));
        prefix.add(new JumpInsnNode(Opcodes.IFNONNULL, original));
        prefix.add(new InsnNode(Opcodes.RETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    static void sockets(ClassNode type) {
        MethodNode method = find(type, "socketTypeCount");
        List<AbstractInsnNode> ops = operations(method);
        MethodInsnNode status = uniqueCall(method, ITEM + "FourWeaponFoundationAdapter$View", "status");
        AbstractInsnNode gateStart = status.getPrevious();
        while (gateStart.getOpcode() < 0) gateStart = gateStart.getPrevious();
        require(gateStart instanceof VarInsnNode load && load.getOpcode() == Opcodes.ALOAD && load.var == 2, "Socket status local drift");
        AbstractInsnNode preceding = gateStart.getPrevious();
        while (preceding.getOpcode() < 0) preceding = preceding.getPrevious();
        require(preceding instanceof JumpInsnNode prior && prior.getOpcode() == Opcodes.IF_ICMPGT, "Socket level gate drift");
        MethodInsnNode fingerprint = uniqueCall(method, ITEM + "FoodWeaponMaterialFingerprint", "foundationSnapshot");
        AbstractInsnNode empty = fingerprint.getNext();
        while (empty.getOpcode() < 0) empty = empty.getNext();
        AbstractInsnNode branch = empty.getNext();
        while (branch.getOpcode() < 0) branch = branch.getNext();
        require(branch instanceof JumpInsnNode jump && jump.getOpcode() == Opcodes.IFEQ, "Socket fingerprint gate drift");
        LabelNode countStart = ((JumpInsnNode) branch).label;
        require(((JumpInsnNode) preceding).label != countStart, "Socket level rejection target drift");
        method.instructions.insert(preceding, new JumpInsnNode(Opcodes.GOTO, countStart));
        InsnList validation = new InsnList();
        AbstractInsnNode cursor = gateStart;
        while (true) {
            AbstractInsnNode next = cursor.getNext();
            method.instructions.remove(cursor);
            validation.add(cursor);
            if (cursor == branch) break;
            cursor = next;
        }
        LabelNode accepted = new LabelNode();
        ((JumpInsnNode) branch).label = accepted;
        AbstractInsnNode returnCount = null;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof VarInsnNode local && local.getOpcode() == Opcodes.ILOAD && local.var == 3) {
                AbstractInsnNode next = instruction.getNext();
                while (next != null && next.getOpcode() < 0) next = next.getNext();
                if (next != null && next.getOpcode() == Opcodes.IRETURN) { require(returnCount == null, "Multiple socket return sites"); returnCount = instruction; }
            }
        }
        require(returnCount != null, "Socket count return missing");
        InsnList tail = new InsnList();
        tail.add(new VarInsnNode(Opcodes.ILOAD, 3));
        tail.add(new JumpInsnNode(Opcodes.IFEQ, accepted));
        tail.add(validation);
        tail.add(new InsnNode(Opcodes.ICONST_0));
        tail.add(new InsnNode(Opcodes.IRETURN));
        tail.add(accepted);
        tail.add(new FrameNode(Opcodes.F_FULL, 4, new Object[]{"net/minecraft/world/item/ItemStack", ITEM + "FoodWeaponEvolution$FoodType",
                ITEM + "FourWeaponFoundationAdapter$View", Opcodes.INTEGER}, 0, null));
        method.instructions.insertBefore(returnCount, tail);
    }

    static void flight(ClassNode type) {
        MethodNode method = find(type, "flight");
        List<AbstractInsnNode> ops = operations(method);
        require(ops.size() == 8 && call(ops.get(2), type.name, "count") && call(ops.get(5), type.name, "count")
                && ops.get(6) instanceof MethodInsnNode rule && rule.name.equals("flight"), "Flight method drift");
        MethodInsnNode rule = (MethodInsnNode) ops.get(6);
        require(rule.desc.equals("(II)Z"), "Flight rule signature drift");
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        InsnList body = method.instructions;
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(ops.get(1));
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, type.name, "count", "(" + STACK + "L" + ITEM + "FoodWeaponEvolution$FoodType;)I", false));
        body.add(new VarInsnNode(Opcodes.ISTORE, 1));
        body.add(new VarInsnNode(Opcodes.ILOAD, 1));
        body.add(new InsnNode(Opcodes.ICONST_2));
        LabelNode possible = new LabelNode();
        body.add(new JumpInsnNode(Opcodes.IF_ICMPGE, possible));
        body.add(new InsnNode(Opcodes.ICONST_0));
        body.add(new InsnNode(Opcodes.IRETURN));
        body.add(possible);
        body.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{Opcodes.INTEGER}, 0, null));
        body.add(new VarInsnNode(Opcodes.ILOAD, 1));
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(ops.get(4));
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, type.name, "count", "(" + STACK + "L" + ITEM + "FoodWeaponEvolution$FoodType;)I", false));
        body.add(rule);
        body.add(new InsnNode(Opcodes.IRETURN));
    }

    static MethodNode find(ClassNode type, String name) {
        List<MethodNode> found = type.methods.stream().filter(method -> method.name.equals(name)).toList();
        require(found.size() == 1, "Ambiguous method " + name);
        return found.get(0);
    }
    static List<AbstractInsnNode> operations(MethodNode method) {
        return Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList();
    }
    static boolean call(AbstractInsnNode instruction, String owner, String name) {
        return instruction instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name);
    }
    static MethodInsnNode uniqueCall(MethodNode method, String owner, String name) {
        List<MethodInsnNode> found = Arrays.stream(method.instructions.toArray()).filter(instruction -> call(instruction, owner, name)).map(instruction -> (MethodInsnNode) instruction).toList();
        require(found.size() == 1, "Ambiguous call " + name);
        return found.get(0);
    }
    static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
