import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class LemonRatInheritancePatch {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String BALANCE = PREFIX + "boss/ai/LemonCombatBalance";
    private static final String ABILITIES = PREFIX + "boss/ai/LemonInheritedAbilities";
    private static final Set<String> CHANGED = Set.of(BALANCE + ".class", ABILITIES + ".class");

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static byte[] entry(ZipFile archive, String name) throws Exception {
        var entry = archive.getEntry(name);
        check(entry != null, "Missing entry: " + name);
        return archive.getInputStream(entry).readAllBytes();
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static boolean replaced(String owner, String name) {
        return owner.equals(BALANCE) ? name.equals("absorbMaximumHealth") || name.startsWith("lambda$absorbMaximumHealth$")
                : name.equals("rats") || name.equals("onMeleeHit") || name.startsWith("lambda$rats$");
    }

    private static byte[] merged(ZipFile baseline, Path compiled, ZicaiVoidPatch.Names names, String owner) throws Exception {
        ClassNode original = read(entry(baseline, owner + ".class"));
        ClassNode donor = new ClassNode();
        new ClassReader(Files.readAllBytes(compiled.resolve(owner + ".class"))).accept(new ClassRemapper(donor, names), 0);
        for (String method : owner.equals(BALANCE) ? Set.of("absorbMaximumHealth") : Set.of("rats", "onMeleeHit")) {
            check(original.methods.stream().filter(candidate -> candidate.name.equals(method)).count() == 1, "Baseline method missing or ambiguous: " + method);
            check(donor.methods.stream().filter(candidate -> candidate.name.equals(method)).count() == 1, "Donor method missing or ambiguous: " + method);
        }
        original.methods.removeIf(method -> replaced(owner, method.name));
        donor.methods.stream().filter(method -> replaced(owner, method.name)).forEach(original.methods::add);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        original.accept(writer);
        byte[] result = writer.toByteArray();
        for (MethodNode method : read(result).methods) {
            if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) {
                new Analyzer<>(new BasicVerifier()).analyze(owner, method);
            }
        }
        return result;
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static int calls(MethodNode method, String name) {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.name.equals(name)) count++;
        }
        return count;
    }

    public static void main(String[] arguments) throws Exception {
        String mode = arguments[0];
        Path compiled = Path.of(arguments[2]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[3]));
        try (ZipFile baseline = new ZipFile(arguments[1])) {
            if (mode.equals("patch")) {
                Path payload = Path.of(arguments[4]);
                for (String owner : Set.of(BALANCE, ABILITIES)) {
                    Path target = payload.resolve(owner + ".class");
                    Files.createDirectories(target.getParent());
                    Files.write(target, merged(baseline, compiled, names, owner));
                }
                System.out.println("PASS targeted maximum-health and rat inheritance methods patched");
                return;
            }
            check(mode.equals("verify"), "Expected patch or verify");
            try (ZipFile candidate = new ZipFile(arguments[4])) {
                Set<String> before = baseline.stream().map(java.util.zip.ZipEntry::getName).collect(java.util.stream.Collectors.toSet());
                Set<String> after = candidate.stream().map(java.util.zip.ZipEntry::getName).collect(java.util.stream.Collectors.toSet());
                check(before.equals(after), "No archive entries may be added or removed");
                for (String name : before) {
                    if (CHANGED.contains(name)) {
                        check(Arrays.equals(entry(candidate, name), merged(baseline, compiled, names, name.substring(0, name.length() - 6))),
                                "Targeted class differs from verified donor method transplant: " + name);
                    } else {
                        check(Arrays.equals(entry(baseline, name), entry(candidate, name)), "Unrelated runtime entry changed: " + name);
                    }
                }
                ClassNode balance = read(entry(candidate, BALANCE + ".class"));
                boolean ratFilter = balance.methods.stream().filter(method -> method.name.startsWith("lambda$absorbMaximumHealth$"))
                        .anyMatch(method -> Arrays.stream(method.instructions.toArray()).anyMatch(instruction -> instruction instanceof TypeInsnNode type
                                && type.getOpcode() == Opcodes.INSTANCEOF && type.desc.equals(PREFIX + "entity/RatBossEntity")));
                check(ratFilter, "Maximum-health absorption must exclude RatBossEntity by type");
                ClassNode abilities = read(entry(candidate, ABILITIES + ".class"));
                MethodNode rats = method(abilities, "rats");
                check(calls(rats, "grant") == 0 && calls(rats, "m_5634_") == 0, "Rat inheritance cannot grant buffs or healing");
                check(calls(rats, "setMinion") == 1 && calls(rats, "summon") == 1, "Small rat spawning remains wired");
                check(abilities.methods.stream().filter(method -> replaced(ABILITIES, method.name)).noneMatch(method ->
                        Arrays.stream(method.instructions.toArray()).anyMatch(instruction -> instruction instanceof LdcInsnNode literal && "cheese".equals(literal.cst))),
                        "No cheese buff timer or registry scan remains");
                MethodNode melee = method(abilities, "onMeleeHit");
                check(calls(melee, "applyRandomRatDebuff") == 1, "Rat negative-effect hit path retained");
                check(calls(melee, "m_5634_") == 1 && calls(melee, "lifesteal") == 1, "Only independent butcher lifesteal is retained");
                check(calls(method(read(entry(candidate, PREFIX + "boss/ai/LemonSacrifice.class")), "trySacrifice"), "grant") == 0,
                        "Sacrifice does not copy victim potion effects");
                System.out.println("PASS archive scope: " + (before.size() - CHANGED.size()) + " unchanged entries; only requested methods and lambdas replaced");
                System.out.println("PASS rat profile/debuff-damage routes, old raging fix, all assets and unrelated healing preserved");
            }
        }
    }
}
