import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class RemoveLegacyStatueTradePatch {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String TRADE = ROOT + "trade/TradeHandler";
    private static final Set<String> REMOVED = Set.of(
            ROOT + "trade/", TRADE + ".class", TRADE + "$Trade.class",
            "data/ftboss/loot_tables/blocks/statue.json",
            "data/ftboss/loot_tables/blocks/butcher_statue.json");

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static AbstractInsnNode previous(AbstractInsnNode instruction) {
        AbstractInsnNode cursor = instruction.getPrevious();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        require(cursor != null, "Missing preceding instruction");
        return cursor;
    }

    private static void removeStartup(MethodNode method) {
        List<MethodInsnNode> calls = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(TRADE)) calls.add(call);
        }
        require(calls.size() == 1, "Expected exactly one legacy trade loader");
        MethodInsnNode call = calls.get(0);
        AbstractInsnNode get = previous(call);
        AbstractInsnNode config = previous(get);
        require(call.getOpcode() == Opcodes.INVOKESTATIC && call.name.equals("load")
                && call.desc.equals("(Ljava/nio/file/Path;)V"), "Unexpected trade startup call");
        require(get instanceof MethodInsnNode path && path.owner.equals("net/minecraftforge/fml/loading/FMLPaths")
                && path.name.equals("get") && path.desc.equals("()Ljava/nio/file/Path;"), "Unexpected config getter");
        require(config instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC
                && field.owner.equals("net/minecraftforge/fml/loading/FMLPaths")
                && field.name.equals("CONFIGDIR"), "Unexpected config field");
        method.instructions.remove(config);
        method.instructions.remove(get);
        method.instructions.remove(call);
    }

    private static void removePaper(String owner, MethodNode method, String note) {
        LdcInsnNode namedPaper = null;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof LdcInsnNode literal && note.equals(literal.cst)) {
                require(namedPaper == null, "Duplicate recipe note");
                namedPaper = literal;
            }
        }
        require(namedPaper != null, "Missing original recipe note: " + note);
        AbstractInsnNode cursor = namedPaper;
        while (cursor != null && !(cursor instanceof JumpInsnNode)) cursor = cursor.getPrevious();
        require(cursor instanceof JumpInsnNode, "Missing recipe chance branch");
        JumpInsnNode branch = (JumpInsnNode) cursor;
        require(branch.getOpcode() == Opcodes.IFGE, "Unexpected recipe branch");
        AbstractInsnNode compare = previous(branch);
        AbstractInsnNode chance = previous(compare);
        AbstractInsnNode randomCall = previous(chance);
        AbstractInsnNode randomField = previous(randomCall);
        AbstractInsnNode level = previous(randomField);
        require(compare.getOpcode() == Opcodes.FCMPG && chance instanceof LdcInsnNode literal
                && Float.valueOf(0.3f).equals(literal.cst), "Unexpected recipe probability");
        require(randomCall instanceof MethodInsnNode call && call.owner.equals("net/minecraft/util/RandomSource")
                && call.desc.equals("()F"), "Unexpected recipe RNG");
        require(randomField instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETFIELD
                && field.owner.equals("net/minecraft/server/level/ServerLevel")
                && field.desc.equals("Lnet/minecraft/util/RandomSource;"), "Unexpected random source field");
        require(level instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC
                && field.owner.equals(owner) && field.name.equals("level"), "Unexpected Boss level field");
        int rewardCalls = 0;
        int paperFields = 0;
        boolean foundNote = false;
        List<AbstractInsnNode> removed = new ArrayList<>();
        for (cursor = level; cursor != branch.label; cursor = cursor.getNext()) {
            require(cursor != null, "Recipe branch target not found after reward");
            require(cursor == branch || !(cursor instanceof JumpInsnNode), "Unexpected nested reward branch");
            if (cursor instanceof MethodInsnNode call && call.owner.equals(ROOT + "boss/ai/NonManorBossLootRouter")
                    && call.name.equals("spawn")) rewardCalls++;
            if (cursor instanceof FieldInsnNode field && field.owner.equals("net/minecraft/world/item/Items")
                    && field.name.equals("f_42516_")) paperFields++;
            if (cursor == namedPaper) foundNote = true;
            if (cursor.getOpcode() >= 0) removed.add(cursor);
        }
        require(foundNote && rewardCalls == 1 && paperFields == 1, "Recipe block must contain only one named-paper reward");
        for (AbstractInsnNode instruction : removed) method.instructions.remove(instruction);
        if (method.localVariables != null) method.localVariables.removeIf(variable -> variable.name.equals("book"));
    }

    private static byte[] methodBytes(String owner, MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, owner, null, "java/lang/Object", null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] patchClass(byte[] original, String targetMethod, String note) throws Exception {
        ClassNode node = read(original);
        Map<String, byte[]> unchanged = new LinkedHashMap<>();
        int targets = 0;
        for (MethodNode method : node.methods) {
            if (method.name.equals(targetMethod)) {
                targets++;
                if (note == null) removeStartup(method); else removePaper(node.name, method, note);
            } else unchanged.put(method.name + method.desc, methodBytes(node.name, method));
        }
        require(targets == 1, "Unexpected target method count: " + node.name);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        byte[] result = writer.toByteArray();
        ClassNode output = read(result);
        for (MethodNode method : output.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                new Analyzer<>(new BasicVerifier()).analyze(output.name, method);
            }
            if (!method.name.equals(targetMethod)) require(Arrays.equals(unchanged.get(method.name + method.desc),
                    methodBytes(output.name, method)), "Unrelated method changed: " + method.name);
        }
        String constantPool = new String(result, StandardCharsets.ISO_8859_1);
        require(!constantPool.contains(TRADE) && !constantPool.contains("ftboss_trades"), "Retired trade reference remains");
        if (note != null) require(!new String(result, StandardCharsets.UTF_8).contains(note), "Recipe name remains");
        return result;
    }

    public static void main(String[] args) throws Exception {
        require(args.length == 4, "Usage: baseline.jar candidate.jar selectedClasses report.txt");
        Path selected = Path.of(args[2]);
        Files.createDirectories(selected);
        Map<String, byte[]> changed = new LinkedHashMap<>();
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (JarFile baseline = new JarFile(args[0])) {
            var enumeration = baseline.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();
                require(!entries.containsKey(entry.getName()), "Duplicate baseline entry: " + entry.getName());
                entries.put(entry.getName(), baseline.getInputStream(entry).readAllBytes());
            }
            for (String entry : REMOVED) require(entries.containsKey(entry), "Missing retired baseline entry: " + entry);
            changed.put(ROOT + "FTBossMod.class", patchClass(entries.get(ROOT + "FTBossMod.class"), "<init>", null));
            changed.put(ROOT + "boss/SugarQueenBoss.class", patchClass(entries.get(ROOT + "boss/SugarQueenBoss.class"),
                    "onBossDeath", "§6秘制焦糖配方"));
            changed.put(ROOT + "boss/MeatBoss.class", patchClass(entries.get(ROOT + "boss/MeatBoss.class"),
                    "onBossDeath", "§6秘制烤肉配方"));
            try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(Path.of(args[1])))) {
                enumeration = baseline.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry original = enumeration.nextElement();
                    String name = original.getName();
                    if (REMOVED.contains(name)) continue;
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(original.getTime());
                    output.putNextEntry(entry);
                    output.write(changed.getOrDefault(name, entries.get(name)));
                    output.closeEntry();
                }
            }
        }
        for (var entry : changed.entrySet()) {
            Path destination = selected.resolve(entry.getKey());
            Files.createDirectories(destination.getParent());
            Files.write(destination, entry.getValue());
        }
        int preserved = 0;
        try (JarFile candidate = new JarFile(args[1]); PrintWriter report = new PrintWriter(args[3], StandardCharsets.UTF_8)) {
            require(candidate.size() == entries.size() - REMOVED.size(), "Unexpected candidate entry count");
            for (var entry : entries.entrySet()) {
                String name = entry.getKey();
                if (REMOVED.contains(name)) {
                    require(candidate.getJarEntry(name) == null, "Retired entry remains: " + name);
                    continue;
                }
                byte[] actual = candidate.getInputStream(candidate.getJarEntry(name)).readAllBytes();
                require(Arrays.equals(actual, changed.getOrDefault(name, entry.getValue())), "Unexpected entry change: " + name);
                if (!changed.containsKey(name)) preserved++;
                if (name.endsWith(".class")) {
                    String constants = new String(actual, StandardCharsets.ISO_8859_1);
                    require(!constants.contains(TRADE) && !constants.contains("ftboss_trades.json"), "Dangling trade reference: " + name);
                }
            }
            report.println("PASS bytecode analysis: all methods in three modified classes");
            report.println("PASS legacy startup loader removed; trade classes and two obsolete loot tables absent");
            report.println("PASS two recipe chance/reward blocks removed; other methods unchanged");
            report.println("PASS no dangling legacy trade references in candidate classes");
            report.println("PASS unchanged class/resource payloads: " + preserved);
            report.println("Changed entries: " + changed.keySet());
            report.println("Removed entries: " + REMOVED);
            System.out.println("PASS runtime retirement patch; preserved entries=" + preserved);
        }
    }
}
