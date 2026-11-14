package ftboss.provenance;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefinitionObserver {
    private static final Set<String> TARGETS = Set.of(
        "top/theillusivec4/curios/Curios",
        "top/theillusivec4/curios/mixin/CuriosImplMixinHooks",
        "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent",
        "net/minecraft/world/level/storage/PlayerDataStorage",
        "net/minecraft/world/entity/player/Inventory",
        "net/farmingtales/ftboss/item/NativeItemCapabilityObservation",
        "net/farmingtales/ftboss/item/NativeCuriosProviderFields260",
        "net/farmingtales/ftboss/item/OwnerRepairGenerationAuthority"
    );
    private static final AtomicInteger CAPTURES = new AtomicInteger();
    private static final AtomicInteger ERRORS = new AtomicInteger();
    private static final CaptureTransformer OBSERVER = new CaptureTransformer();
    private static Instrumentation instrumentation;
    private static Path root;

    public static void premain(String arguments, Instrumentation provided) throws Exception {
        root = Path.of(arguments).toAbsolutePath().normalize();
        Files.createDirectories(root);
        instrumentation = provided;
        Properties startup = new Properties();
        startup.setProperty("preexistingTargetCount", Long.toString(java.util.Arrays.stream(provided.getAllLoadedClasses())
            .filter(type -> TARGETS.contains(type.getName().replace('.', '/'))).count()));
        provided.addTransformer(OBSERVER, false);
        inventory(startup);
        write(root.resolve("startup.xml"), startup);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Properties terminal = new Properties();
                inventory(terminal);
                terminal.setProperty("captureCount", Integer.toString(CAPTURES.get()));
                terminal.setProperty("errorCount", Integer.toString(ERRORS.get()));
                write(root.resolve("terminal.xml"), terminal);
            } catch (Throwable failure) {
                failure(failure);
            }
        }, "ftboss-readonly-definition-observer-terminal"));
    }

    private static Object field(Object instance, String name) throws Exception {
        Field declared = instance.getClass().getDeclaredField(name);
        declared.setAccessible(true);
        return declared.get(instance);
    }

    private static List<ClassFileTransformer> transformers(String fieldName) throws Exception {
        Object manager = field(instrumentation, fieldName);
        if (manager == null) return List.of();
        Object[] snapshot;
        synchronized (manager) {
            snapshot = ((Object[])field(manager, "mTransformerList")).clone();
        }
        List<ClassFileTransformer> result = new ArrayList<>();
        for (Object info : snapshot) result.add((ClassFileTransformer)field(info, "mTransformer"));
        return result;
    }

    private static void inventory(Properties result) throws Exception {
        List<ClassFileTransformer> ordinary = transformers("mTransformerManager");
        List<ClassFileTransformer> retransformation = transformers("mRetransfomableTransformerManager");
        result.setProperty("ordinaryTransformerCount", Integer.toString(ordinary.size()));
        result.setProperty("retransformableTransformerCount", Integer.toString(retransformation.size()));
        result.setProperty("soleApplicableJavaTransformer", Boolean.toString(ordinary.size() == 1 && ordinary.get(0) == OBSERVER && retransformation.isEmpty()));
        result.setProperty("ordinaryTransformers", ordinary.stream().map(value -> value.getClass().getName()).toList().toString());
        result.setProperty("retransformableTransformers", retransformation.stream().map(value -> value.getClass().getName()).toList().toString());
        result.setProperty("observerAlwaysReturnsNull", "true");
    }

    private static void write(Path path, Properties values) throws Exception {
        Files.createDirectories(path.getParent());
        try (var output = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            values.storeToXML(output, "Read-only JVM class-definition boundary observation", StandardCharsets.UTF_8);
        }
    }

    private static void failure(Throwable failure) {
        int number = ERRORS.incrementAndGet();
        try {
            Files.writeString(root.resolve("capture-error-" + number + ".txt"), failure.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (Exception ignored) {
        }
    }

    private static final class CaptureTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(Module module, ClassLoader loader, String name, Class<?> redefining, ProtectionDomain domain, byte[] bytes) {
            if (name == null || !TARGETS.contains(name)) return null;
            try {
                int number = CAPTURES.incrementAndGet();
                Path captured = root.resolve(String.format("capture-%02d", number)).resolve(name + ".class");
                if (!captured.normalize().startsWith(root)) throw new IllegalArgumentException("Definition observation path escaped capture root");
                Files.createDirectories(captured.getParent());
                Files.write(captured, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                Properties metadata = new Properties();
                inventory(metadata);
                metadata.setProperty("internalName", name);
                metadata.setProperty("classFile", captured.toString());
                metadata.setProperty("byteLength", Integer.toString(bytes.length));
                metadata.setProperty("sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
                metadata.setProperty("loaderClass", loader == null ? "bootstrap" : loader.getClass().getName());
                metadata.setProperty("loaderName", loader == null ? "bootstrap" : String.valueOf(loader.getName()));
                metadata.setProperty("loaderIdentity", loader == null ? "0" : Integer.toString(System.identityHashCode(loader)));
                metadata.setProperty("module", module == null ? "null" : String.valueOf(module.getName()));
                metadata.setProperty("redefinition", Boolean.toString(redefining != null));
                metadata.setProperty("codeSource", domain == null || domain.getCodeSource() == null ? "null" : domain.getCodeSource().getLocation().toExternalForm());
                metadata.setProperty("capturePoint", "JVM ClassFileTransformer after application-loader transformation");
                metadata.setProperty("returnedReplacementBytes", "false");
                write(root.resolve(String.format("capture-%02d.xml", number)), metadata);
            } catch (Throwable failure) {
                failure(failure);
            }
            return null;
        }
    }
}
