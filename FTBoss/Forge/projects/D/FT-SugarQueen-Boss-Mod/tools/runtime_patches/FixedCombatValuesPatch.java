import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.SourceInterpreter;

public final class FixedCombatValuesPatch implements Opcodes {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final Set<String> HELPERS = Set.of(
            ROOT + "util/ManorWeaponRules.skillDamage(F)F",
            ROOT + "util/ManorWeaponRules.skillBonusDamage(F)F",
            ROOT + "util/ManorWeaponRules.skillHeal(F)F",
            ROOT + "util/ManorWeaponBalanceRules.skillDamage(F)F",
            ROOT + "util/ManorWeaponBalanceRules.skillBonusDamage(F)F",
            ROOT + "util/XuanlanRules.weaponDamage(F)F");
    private final Map<String, ClassNode> types;

    public FixedCombatValuesPatch(Map<String, ClassNode> types) {
        this.types = types;
    }

    public static Number constant(AbstractInsnNode instruction) {
        if (instruction == null) return null;
        int opcode = instruction.getOpcode();
        if (opcode >= ICONST_M1 && opcode <= ICONST_5) return opcode - ICONST_0;
        if (opcode >= LCONST_0 && opcode <= LCONST_1) return (long) (opcode - LCONST_0);
        if (opcode >= FCONST_0 && opcode <= FCONST_2) return (float) (opcode - FCONST_0);
        if (opcode >= DCONST_0 && opcode <= DCONST_1) return (double) (opcode - DCONST_0);
        if (instruction instanceof IntInsnNode literal && (opcode == BIPUSH || opcode == SIPUSH)) return literal.operand;
        if (instruction instanceof LdcInsnNode literal && literal.cst instanceof Number number) return number;
        return null;
    }

    public static Number unary(int opcode, Number value) {
        if (value == null) return null;
        return switch (opcode) {
            case INEG -> -value.intValue();
            case LNEG -> -value.longValue();
            case FNEG -> -value.floatValue();
            case DNEG -> -value.doubleValue();
            case I2L, F2L, D2L -> value.longValue();
            case I2F, L2F, D2F -> value.floatValue();
            case I2D, L2D, F2D -> value.doubleValue();
            case L2I, F2I, D2I -> value.intValue();
            case I2B -> (int) value.byteValue();
            case I2C -> (int) (char) value.intValue();
            case I2S -> (int) value.shortValue();
            default -> null;
        };
    }

    public static Number binary(int opcode, Number left, Number right) {
        if (left == null || right == null) return null;
        try {
            return switch (opcode) {
                case IADD -> left.intValue() + right.intValue();
                case ISUB -> left.intValue() - right.intValue();
                case IMUL -> left.intValue() * right.intValue();
                case IDIV -> left.intValue() / right.intValue();
                case IREM -> left.intValue() % right.intValue();
                case LADD -> left.longValue() + right.longValue();
                case LSUB -> left.longValue() - right.longValue();
                case LMUL -> left.longValue() * right.longValue();
                case LDIV -> left.longValue() / right.longValue();
                case LREM -> left.longValue() % right.longValue();
                case FADD -> left.floatValue() + right.floatValue();
                case FSUB -> left.floatValue() - right.floatValue();
                case FMUL -> left.floatValue() * right.floatValue();
                case FDIV -> left.floatValue() / right.floatValue();
                case FREM -> left.floatValue() % right.floatValue();
                case DADD -> left.doubleValue() + right.doubleValue();
                case DSUB -> left.doubleValue() - right.doubleValue();
                case DMUL -> left.doubleValue() * right.doubleValue();
                case DDIV -> left.doubleValue() / right.doubleValue();
                case DREM -> left.doubleValue() % right.doubleValue();
                default -> null;
            };
        } catch (ArithmeticException ignored) {
            return null;
        }
    }

    public static boolean finite(Number value) {
        return value != null && (!(value instanceof Float) || Float.isFinite(value.floatValue()))
                && (!(value instanceof Double) || Double.isFinite(value.doubleValue()));
    }

    public Number evaluate(String owner, String name, String descriptor, Number value, int depth) {
        if (depth > 8 || !HELPERS.contains(owner + "." + name + descriptor)) return null;
        ClassNode type = types.get(owner);
        if (type == null) return null;
        MethodNode method = type.methods.stream().filter(candidate -> candidate.name.equals(name)
                && candidate.desc.equals(descriptor)).findFirst().orElse(null);
        if (method == null || (method.access & ACC_STATIC) == 0) return null;
        List<Number> stack = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) {
            int opcode = instruction.getOpcode();
            if (opcode < 0 || opcode == NOP) continue;
            Number literal = constant(instruction);
            if (literal != null) {
                stack.add(literal);
            } else if (instruction instanceof VarInsnNode variable && variable.var == 0 && opcode == FLOAD) {
                stack.add(value.floatValue());
            } else if (opcode == FRETURN) {
                return stack.size() == 1 ? stack.get(0).floatValue() : null;
            } else if (instruction instanceof MethodInsnNode call && opcode == INVOKESTATIC && !stack.isEmpty()) {
                Number result = evaluate(call.owner, call.name, call.desc, stack.remove(stack.size() - 1), depth + 1);
                if (!finite(result)) return null;
                stack.add(result);
            } else {
                if (stack.isEmpty()) return null;
                Number right = stack.remove(stack.size() - 1);
                Number result = unary(opcode, right);
                if (result == null) {
                    if (stack.isEmpty()) return null;
                    result = binary(opcode, stack.remove(stack.size() - 1), right);
                }
                if (!finite(result)) return null;
                stack.add(result);
            }
        }
        return null;
    }

    private static AbstractInsnNode previousCode(AbstractInsnNode instruction) {
        AbstractInsnNode previous = instruction.getPrevious();
        while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
        return previous;
    }

    private int foldBranches(String owner, MethodNode method) throws Exception {
        int changes = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || call.getOpcode() != INVOKESTATIC
                    || !HELPERS.contains(call.owner + "." + call.name + call.desc)) continue;
            AbstractInsnNode second = previousCode(call);
            if (second == null || !(constant(second) instanceof Float)) continue;
            AbstractInsnNode separator = previousCode(second);
            if (!(separator instanceof JumpInsnNode jump) || jump.getOpcode() != GOTO) continue;
            AbstractInsnNode first = previousCode(separator);
            if (first == null || first.getNext() != separator || !(constant(first) instanceof Float)) continue;
            boolean mergeTarget = false;
            for (AbstractInsnNode cursor = second.getNext(); cursor != call; cursor = cursor.getNext()) {
                if (cursor == null || cursor.getOpcode() >= 0) break;
                if (cursor == jump.label) mergeTarget = true;
            }
            if (!mergeTarget) continue;
            var frames = new Analyzer<>(new SourceInterpreter()).analyze(owner, method);
            var frame = frames[method.instructions.indexOf(call)];
            if (frame == null || frame.getStackSize() == 0
                    || !frame.getStack(frame.getStackSize() - 1).insns.equals(Set.of(first, second))) continue;
            Number firstValue = evaluate(call.owner, call.name, call.desc, constant(first), 0);
            Number secondValue = evaluate(call.owner, call.name, call.desc, constant(second), 0);
            if (!finite(firstValue) || !finite(secondValue)) continue;
            method.instructions.set(first, new LdcInsnNode(firstValue));
            method.instructions.set(second, new LdcInsnNode(secondValue));
            method.instructions.remove(call);
            changes++;
        }
        return changes;
    }

    public int fold(String owner, MethodNode method) throws Exception {
        int changes = 0;
        boolean changed;
        do {
            changed = false;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                AbstractInsnNode right = instruction.getPrevious();
                Number rightValue = constant(right);
                if (rightValue == null) continue;
                Number result;
                AbstractInsnNode left = null;
                if (instruction instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC) {
                    result = evaluate(call.owner, call.name, call.desc, rightValue, 0);
                } else {
                    result = unary(instruction.getOpcode(), rightValue);
                    if (result == null) {
                        left = right.getPrevious();
                        result = binary(instruction.getOpcode(), constant(left), rightValue);
                    }
                }
                if (!finite(result)) continue;
                method.instructions.set(instruction, new LdcInsnNode(result));
                method.instructions.remove(right);
                if (left != null) method.instructions.remove(left);
                changes++;
                changed = true;
            }
        } while (changed);
        return changes + foldBranches(owner, method);
    }

    public static Map<String, ClassNode> read(ZipFile jar) throws Exception {
        Map<String, ClassNode> types = new HashMap<>();
        for (var entry : jar.stream().filter(entry -> entry.getName().startsWith(ROOT)
                && entry.getName().endsWith(".class") && !entry.getName().startsWith(ROOT + "client/")).toList()) {
            ClassNode type = new ClassNode();
            new ClassReader(jar.getInputStream(entry).readAllBytes()).accept(type, 0);
            types.put(type.name, type);
        }
        return types;
    }

    public static byte[] write(ClassNode type) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (ACC_ABSTRACT | ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[1]);
        List<String> report = new ArrayList<>();
        int expressions = 0;
        try (ZipFile jar = new ZipFile(arguments[0])) {
            Map<String, ClassNode> types = read(jar);
            FixedCombatValuesPatch optimizer = new FixedCombatValuesPatch(types);
            for (ClassNode type : types.values().stream().sorted((left, right) -> left.name.compareTo(right.name)).toList()) {
                int count = 0;
                for (MethodNode method : type.methods) {
                    int folded = optimizer.fold(type.name, method);
                    if (folded > 0) report.add(type.name + "." + method.name + method.desc + "\t" + folded);
                    count += folded;
                }
                if (count == 0) continue;
                Path output = payload.resolve(type.name + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, write(type));
                expressions += count;
            }
        }
        Files.write(payload.getParent().resolve("folded-methods.tsv"), report);
        System.out.println("PASS: baked " + expressions + " constant operations in " + report.size() + " methods");
    }
}
