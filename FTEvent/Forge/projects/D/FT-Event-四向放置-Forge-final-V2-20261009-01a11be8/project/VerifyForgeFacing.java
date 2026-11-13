import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class VerifyForgeFacing implements Opcodes {
    private static final String BASE = "cn/farmingtales/qysevent/";
    private static final String ORACLE = BASE + "OracleBlock";
    private static final String WEATHER = BASE + "WeatherForecasterBlock";
    private static int assertions;

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
        assertions++;
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode owner = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class"))).accept(owner, 0);
        return owner;
    }

    private static MethodNode method(ClassNode owner, String name) {
        return owner.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static String body(MethodNode method) {
        Textifier text = new Textifier();
        method.accept(new TraceMethodVisitor(text));
        return text.getText().toString();
    }

    private static boolean calls(MethodNode method, String name) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.name.equals(name)) return true;
        }
        return false;
    }

    private static boolean callbackField(MethodNode method, String name) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode field && field.name.equals(name)) return true;
        }
        return false;
    }

    private static MethodNode mapOracle(MethodNode original) {
        MethodNode copy = new MethodNode(original.access, original.name, original.desc, original.signature,
                                        original.exceptions.toArray(String[]::new));
        original.accept(copy);
        for (AbstractInsnNode instruction : copy.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals(ORACLE)) field.owner = WEATHER;
            if (instruction instanceof MethodInsnNode call && call.owner.equals(ORACLE)) call.owner = WEATHER;
        }
        return copy;
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile jar = new JarFile(arguments[0]); JarFile baseline = new JarFile(arguments[1]);
             JarFile kube = new JarFile(arguments[2])) {
            ClassNode weather = read(jar, WEATHER);
            ClassNode oracle = read(jar, ORACLE);
            ClassNode previous = read(baseline, WEATHER);
            require(weather.superName.equals(previous.superName), "Weather superclass changed");
            require(weather.fields.stream().anyMatch(field -> field.name.equals("FACING") && (field.access & ACC_STATIC) != 0), "Missing facing field");
            for (String name : List.of("m_5573_", "m_6843_", "m_6943_", "m_7926_")) {
                require(body(method(weather, name)).equals(body(mapOracle(method(oracle, name)))), "Weather differs from Oracle facing: " + name);
            }
            require(body(method(weather, "m_6227_")).equals(body(method(previous, "m_6227_"))), "Emerald forecast interaction changed");
            for (ClassNode block : List.of(weather, oracle)) {
                require(block.methods.stream().noneMatch(candidate -> candidate.name.equals("m_5940_") || candidate.name.equals("m_5909_")), "Unexpected custom collision/shape override");
            }
            require(calls(method(weather, "<init>"), "m_49959_"), "Missing default state registration");
            require(calls(method(weather, "m_5573_"), "m_8125_") && calls(method(weather, "m_5573_"), "m_122424_"), "Placement does not face player");
            require(calls(method(weather, "m_6843_"), "m_55954_"), "Rotation missing");
            require(calls(method(weather, "m_6943_"), "m_54846_"), "Mirror missing");
            for (String name : List.of(WEATHER, ORACLE)) {
                ClassReader reader = new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")));
                StringWriter errors = new StringWriter();
                CheckClassAdapter.verify(reader, VerifyForgeFacing.class.getClassLoader(), false, new PrintWriter(errors));
                require(errors.toString().isBlank(), name + " failed runtime ASM checks: " + errors);
                Class<?> loaded = Class.forName(name.replace('/', '.'), false, VerifyForgeFacing.class.getClassLoader());
                loaded.getDeclaredMethods();
                loaded.getDeclaredFields();
                loaded.getDeclaredConstructors();
            }
            ClassNode cardinal = read(kube, "dev/latvian/mods/kubejs/block/custom/HorizontalDirectionalBlockBuilder$HorizontalDirectionalBlockJS");
            require(calls(method(cardinal, "m_5573_"), "m_8125_") && calls(method(cardinal, "m_5573_"), "m_122424_"), "KubeJS cardinal placement is not horizontal opposite");
            ClassNode basic = read(kube, "dev/latvian/mods/kubejs/block/custom/BasicBlockJS");
            require(callbackField(method(basic, "m_6843_"), "rotateStateModification"), "KubeJS rotation callback not supported");
            require(callbackField(method(basic, "m_6943_"), "mirrorStateModification"), "KubeJS mirror callback not supported");
            for (String name : List.of("BlockStateRotateCallbackJS", "BlockStateMirrorCallbackJS", "BlockStateModifyCallbackJS")) {
                Class<?> loaded = Class.forName("dev.latvian.mods.kubejs.block.callbacks." + name, false, VerifyForgeFacing.class.getClassLoader());
                require(loaded.getDeclaredMethods().length > 0, "Missing KubeJS callback API " + name);
            }
        }
        Files.writeString(Path.of(arguments[3]), "{\"passed\":true,\"assertions\":" + assertions
                + ",\"jvm_linkage\":true,\"asm_runtime_types\":true,\"oracle_weather_methods_equal\":true,\"interaction_preserved\":true,\"kubejs_api_verified\":true}\n");
        System.out.println("Forge Oracle/Weather JVM, ASM and KubeJS callback API checks: " + assertions + " assertions passed");
    }
}
