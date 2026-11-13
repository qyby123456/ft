import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class EventIsolationAudit {
    private static final String PREFIX = "cn/farmingtales/qysevent/";
    private static final String CLIENT = PREFIX + "client/";
    private static int assertions;

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
        assertions++;
    }

    private static boolean clientOnly(String name) {
        return name.startsWith(CLIENT) || name.startsWith("net/minecraft/client/")
                || name.startsWith("com/mojang/blaze3d/") || name.equals(PREFIX + "mixin/LevelRendererMixin");
    }

    private static boolean containsClient(Type type) {
        if (type.getSort() == Type.METHOD) {
            for (Type argument : type.getArgumentTypes()) if (containsClient(argument)) return true;
            return containsClient(type.getReturnType());
        }
        if (type.getSort() == Type.ARRAY) return containsClient(type.getElementType());
        return type.getSort() == Type.OBJECT && clientOnly(type.getInternalName());
    }

    private static boolean clientSubscriber(ClassNode owner) {
        List<AnnotationNode> annotations = new ArrayList<>();
        if (owner.visibleAnnotations != null) annotations.addAll(owner.visibleAnnotations);
        if (owner.invisibleAnnotations != null) annotations.addAll(owner.invisibleAnnotations);
        for (AnnotationNode annotation : annotations) {
            if (!annotation.desc.endsWith("Mod$EventBusSubscriber;") || annotation.values == null) continue;
            for (int index = 0; index < annotation.values.size(); index += 2) {
                if (!annotation.values.get(index).equals("value")) continue;
                Object values = annotation.values.get(index + 1);
                if (values instanceof List<?> list && !list.isEmpty()) {
                    return list.stream().allMatch(value -> value instanceof String[] enumeration
                            && enumeration.length == 2 && enumeration[1].equals("CLIENT"));
                }
            }
        }
        return false;
    }

    private static Set<String> lambdaHandles(MethodNode method, String owner) {
        Set<String> result = new HashSet<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                for (Object argument : dynamic.bsmArgs) {
                    if (argument instanceof Handle handle && handle.getOwner().equals(owner)) result.add(handle.getName() + handle.getDesc());
                }
            }
        }
        return result;
    }

    private static final class CommonClassLoader extends ClassLoader {
        private final Map<String, byte[]> content;

        CommonClassLoader(Map<String, byte[]> content) {
            super(EventIsolationAudit.class.getClassLoader());
            this.content = content;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (clientOnly(name.replace('.', '/'))) throw new ClassNotFoundException("Client type denied in server audit: " + name);
            synchronized (getClassLoadingLock(name)) {
                Class<?> result = findLoadedClass(name);
                if (result == null && content.containsKey(name)) {
                    byte[] bytes = content.get(name);
                    result = defineClass(name, bytes, 0, bytes.length);
                }
                if (result == null) result = super.loadClass(name, false);
                if (resolve) resolveClass(result);
                return result;
            }
        }
    }

    public static void main(String[] arguments) throws Exception {
        Map<String, ClassNode> nodes = new HashMap<>();
        Map<String, byte[]> content = new HashMap<>();
        try (JarFile archive = new JarFile(arguments[0])) {
            for (var entry : archive.stream().toList()) {
                if (!entry.getName().endsWith(".class")) continue;
                byte[] bytes = archive.getInputStream(entry).readAllBytes();
                ClassNode owner = new ClassNode();
                new ClassReader(bytes).accept(owner, 0);
                nodes.put(owner.name, owner);
                content.put(owner.name.replace('/', '.'), bytes);
            }
        }
        ClassNode packet = nodes.get(PREFIX + "WeatherSyncPacket");
        Set<String> guarded = new HashSet<>();
        for (MethodNode method : packet.methods) {
            boolean distClient = false;
            boolean distDispatch = false;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraftforge/api/distmarker/Dist")
                        && field.name.equals("CLIENT")) distClient = true;
                if (instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraftforge/fml/DistExecutor")
                        && call.name.equals("unsafeRunWhenOn")) distDispatch = true;
            }
            if (distDispatch) {
                require(distClient, "Packet dispatch is not guarded by Dist.CLIENT");
                guarded.addAll(lambdaHandles(method, packet.name));
            }
        }
        boolean changed;
        do {
            changed = false;
            for (MethodNode method : packet.methods) {
                if (guarded.contains(method.name + method.desc)) changed |= guarded.addAll(lambdaHandles(method, packet.name));
            }
        } while (changed);
        require(!guarded.isEmpty(), "Missing guarded packet callback chain");
        int commonClasses = 0;
        int clientClasses = 0;
        int guardedReferences = 0;
        for (ClassNode owner : nodes.values()) {
            if (clientOnly(owner.name)) {
                clientClasses++;
                if (owner.name.equals(CLIENT + "WeatherFilterOverlay") || owner.name.equals(CLIENT + "ClientWeatherParticles")) {
                    require(clientSubscriber(owner), "Client Forge event handler is not client-only: " + owner.name);
                }
                continue;
            }
            commonClasses++;
            require(!clientOnly(owner.superName), "Common class extends client class: " + owner.name);
            for (String implemented : owner.interfaces) require(!clientOnly(implemented), "Common interface is client-only");
            for (FieldNode field : owner.fields) require(!containsClient(Type.getType(field.desc)), "Common field has client signature");
            for (MethodNode method : owner.methods) {
                require(!containsClient(Type.getMethodType(method.desc)), "Common method has client signature: " + owner.name + "." + method.name);
                for (AbstractInsnNode instruction : method.instructions) {
                    String referenced = instruction instanceof MethodInsnNode call ? call.owner
                            : instruction instanceof FieldInsnNode field ? field.owner
                            : instruction instanceof TypeInsnNode cast ? cast.desc : null;
                    if (referenced != null && clientOnly(referenced)) {
                        require(owner.name.equals(packet.name) && guarded.contains(method.name + method.desc)
                                && (method.access & Opcodes.ACC_PRIVATE) != 0, "Unguarded client reference: " + owner.name + "." + method.name);
                        guardedReferences++;
                    }
                }
            }
        }
        CommonClassLoader loader = new CommonClassLoader(content);
        int loaded = 0;
        for (String name : content.keySet()) {
            if (clientOnly(name.replace('.', '/'))) continue;
            Class<?> type = Class.forName(name, false, loader);
            type.getDeclaredMethods();
            type.getDeclaredFields();
            type.getDeclaredConstructors();
            loaded++;
        }
        require(loaded == commonClasses, "Not all common Event classes were linked");
        try {
            loader.loadClass("net.minecraft.client.Minecraft");
            throw new IllegalStateException("Server audit did not block client classes");
        } catch (ClassNotFoundException expected) {
            assertions++;
        }
        Files.writeString(Path.of(arguments[1]), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "passed", true, "assertions", assertions, "commonClassesLoaded", loaded, "clientClassesExcluded", clientClasses,
                "guardedPacketClientReferences", guardedReferences, "classInitializationPerformed", false,
                "dedicatedServerStarted", false, "scope", "Static sided signatures, guarded callback chain and client-denying common Event classloader")));
        System.out.println("Event side isolation: " + loaded + " common classes verified, " + clientClasses + " client classes excluded");
    }
}
