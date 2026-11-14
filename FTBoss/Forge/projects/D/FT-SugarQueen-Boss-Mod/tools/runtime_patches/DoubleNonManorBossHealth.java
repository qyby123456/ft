import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

public final class DoubleNonManorBossHealth {
    private static final String BASE = "net/farmingtales/ftboss/";
    private record Change(String member, Number value, int count, boolean field) {}
    private record Target(String owner, List<Change> changes) {}
    private static Change value(String method, Number value, int count) { return new Change(method, value, count, false); }
    private static Change field(String name, Number value) { return new Change(name, value, 1, true); }
    private static Target target(String owner, Change... changes) { return new Target(BASE + owner, List.of(changes)); }
    private static final List<Target> TARGETS = List.of(
            target("entity/SugarQueenEntity", value("createAttributes", 360.0D, 1)),
            target("entity/MeatMountainEntity", value("m_34104_", 380.0D, 1)),
            target("entity/TeaTreeBossEntity", field("MAX_HP", 340.0D),
                    value("createAttributes", 340.0D, 1), value("setFinaleChallenge", 340.0D, 1)),
            target("entity/WineBarrelBossEntity", value("createAttributes", 400.0D, 1)),
            target("entity/DefectCoreEntity", value("createAttributes", 404.0D, 1)),
            target("entity/CompostBossEntity", value("createAttributes", 340.0D, 1)),
            target("entity/AppleBossEntity", value("createAttributes", 320.0D, 1)),
            target("entity/BengerEntity", field("MAX_HP", 330.0F), value("createAttributes", 330.0D, 1)),
            target("entity/TideFishBossEntity", field("MAX_HEALTH", 480.0D), value("createAttributes", 480.0D, 1)),
            target("boss/SugarQueenBoss", field("MAX_HP", 360.0D), value("spawnClone", 360.0F, 1),
                    value("tryStart", 360.0F, 1), value("tick", 108.0F, 1), value("tick", 360, 1),
                    value("tryPopTotem", 108.0F, 1)),
            target("boss/MeatBoss", field("MAX_HP", 380.0D), value("spawnClone", 380.0F, 1), value("tryStart", 380.0F, 1)),
            target("boss/ai/ButcherPhaseRules", field("BASE_HEALTH", 380.0D)),
            target("boss/ai/RatSwarmRules", field("MASTER_HEALTH", 20000.0D)),
            target("boss/ai/RatSwarmEncounter", value("blocksDamage", 20000.0F, 1),
                    value("initialize", 20000.0D, 2), value("swarmMaximum", 20000.0D, 1)));

    private static Number twice(Number value) {
        if (value instanceof Double) return value.doubleValue() * 2;
        if (value instanceof Float) return value.floatValue() * 2;
        if (value instanceof Integer) return value.intValue() * 2;
        throw new IllegalArgumentException("Unsupported numeric type: " + value);
    }

    private static ClassNode read(JarFile archive, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getJarEntry(name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static String trace(ClassNode node) {
        StringWriter text = new StringWriter();
        node.accept(new TraceClassVisitor(new PrintWriter(text)));
        return text.toString();
    }

    private static void patch(ClassNode node, Target target) {
        for (Change change : target.changes()) {
            int count = 0;
            if (change.field()) {
                for (var member : node.fields) if (member.name.equals(change.member()) && change.value().equals(member.value)) {
                    member.value = twice(change.value());
                    count++;
                }
            } else {
                for (var method : node.methods) if (method.name.equals(change.member())) {
                    for (var instruction : method.instructions) {
                        if (instruction instanceof LdcInsnNode constant && change.value().equals(constant.cst)) {
                            constant.cst = twice(change.value());
                            count++;
                        } else if (instruction instanceof IntInsnNode constant && change.value() instanceof Integer
                                && constant.operand == change.value().intValue()) {
                            constant.operand *= 2;
                            count++;
                        }
                    }
                }
            }
            if (count != change.count()) throw new IllegalStateException(node.name + "." + change.member()
                    + " expected " + change.count() + " health constants " + change.value() + ", found " + count);
        }
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0]);
             JarFile candidate = arguments.length > 2 ? new JarFile(arguments[2]) : null) {
            List<String> changed = new ArrayList<>();
            for (Target target : TARGETS) {
                ClassNode node = read(baseline, target.owner());
                patch(node, target);
                if (candidate == null) {
                    ClassWriter writer = new ClassWriter(0);
                    node.accept(writer);
                    Path destination = Path.of(arguments[1]).resolve(node.name + ".class");
                    Files.createDirectories(destination.getParent());
                    Files.write(destination, writer.toByteArray());
                } else if (!trace(node).equals(trace(read(candidate, target.owner())))) {
                    throw new AssertionError("Unexpected bytecode difference beyond health constants: " + target.owner());
                }
                changed.add(node.name);
            }
            System.out.println("PASS " + (candidate == null ? "patch" : "full bytecode audit")
                    + ": " + changed.size() + " classes; only approved health constants doubled");
        }
    }
}
