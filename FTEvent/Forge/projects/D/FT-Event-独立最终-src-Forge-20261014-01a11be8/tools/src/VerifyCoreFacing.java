import cn.farmingtales.qysevent.CoreFacingPolicy;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;

public final class VerifyCoreFacing implements Opcodes {
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

    private static boolean hasCall(MethodNode method, String name) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.name.equals(name)) return true;
        }
        return false;
    }

    private static Object value(AnnotationNode annotation, String key) {
        for (int index = 0; index < annotation.values.size(); index += 2) {
            if (annotation.values.get(index).equals(key)) return annotation.values.get(index + 1);
        }
        return null;
    }

    public static void main(String[] arguments) throws Exception {
        Direction[] facing = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        Rotation[] rotations = {Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90};
        for (int directionIndex = 0; directionIndex < facing.length; directionIndex++) {
            for (int rotationIndex = 0; rotationIndex < rotations.length; rotationIndex++) {
                require(CoreFacingPolicy.rotate(facing[directionIndex], rotations[rotationIndex]) == facing[(directionIndex + rotationIndex) % 4], "Bad core rotation");
            }
            for (Mirror mirror : Mirror.values()) {
                boolean flips = mirror == Mirror.LEFT_RIGHT && directionIndex % 2 == 0
                        || mirror == Mirror.FRONT_BACK && directionIndex % 2 == 1;
                require(CoreFacingPolicy.mirror(facing[directionIndex], mirror) == facing[(directionIndex + (flips ? 2 : 0)) % 4], "Bad core mirror");
            }
        }
        require(CoreFacingPolicy.matches(new ResourceLocation("farmingtales", "wishing_well_core"), true), "Core was excluded");
        require(!CoreFacingPolicy.matches(new ResourceLocation("farmingtales", "wishing_well_core"), false), "Missing facing was accepted");
        require(!CoreFacingPolicy.matches(null, true), "Null key was accepted");
        for (String name : List.of("minecraft:furnace", "minecraft:stone", "qys_event:oracle", "qys_event:weather_forecaster", "qys_event:wishing_well", "ftboss:oracle", "farmingtales:other")) {
            require(!CoreFacingPolicy.matches(new ResourceLocation(name), true), "Unrelated block affected: " + name);
        }
        try (JarFile runtime = new JarFile(arguments[0]); JarFile minecraft = new JarFile(arguments[1]);
             JarFile kubejs = new JarFile(arguments[2])) {
            ClassNode target = read(minecraft, "net/minecraft/world/level/block/state/BlockBehaviour");
            ClassNode mixin = read(runtime, "cn/farmingtales/qysevent/mixin/CoreFacingMixin");
            require(mixin.invisibleAnnotations.stream().anyMatch(annotation -> annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")), "Missing Mixin registration annotation");
            int injectors = 0;
            for (MethodNode handler : mixin.methods) {
                if (handler.visibleAnnotations == null) continue;
                for (AnnotationNode annotation : handler.visibleAnnotations) {
                    if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) continue;
                    List<?> selectors = (List<?>) value(annotation, "method");
                    require(selectors.size() == 1, "Ambiguous injection selector");
                    String selector = selectors.get(0).toString();
                    require(target.methods.stream().filter(method -> (method.name + method.desc).equals(selector)).count() == 1, "Exact Forge target not found: " + selector);
                    require(Boolean.TRUE.equals(value(annotation, "cancellable")), "Handler cannot return updated facing");
                    require(Boolean.FALSE.equals(value(annotation, "remap")), "Unexpected SRG remapping");
                    require(Integer.valueOf(1).equals(value(annotation, "require")), "Injector not fail-fast");
                    List<?> injectionPoints = (List<?>) value(annotation, "at");
                    require(injectionPoints.size() == 1 && "HEAD".equals(value((AnnotationNode) injectionPoints.get(0), "value")), "Injection point is not HEAD");
                    require(hasCall(handler, "setReturnValue"), "Handler does not set state");
                    injectors++;
                }
            }
            require(injectors == 2, "Expected exactly rotation and mirror injectors");
            ClassNode basic = read(kubejs, "dev/latvian/mods/kubejs/block/custom/BasicBlockJS");
            ClassNode cardinal = read(kubejs, "dev/latvian/mods/kubejs/block/custom/HorizontalDirectionalBlockBuilder$HorizontalDirectionalBlockJS");
            for (String methodName : List.of("m_6843_", "m_6943_")) {
                require(cardinal.methods.stream().noneMatch(method -> method.name.equals(methodName)), "Cardinal blocks bypass audited fallback");
                MethodNode method = basic.methods.stream().filter(candidate -> candidate.name.equals(methodName)).findFirst().orElseThrow();
                require(hasCall(method, methodName), "KubeJS fallback missing " + methodName);
            }
            for (String name : List.of("cn/farmingtales/qysevent/CoreFacingPolicy", "cn/farmingtales/qysevent/mixin/CoreFacingMixin")) {
                ClassReader reader = new ClassReader(runtime.getInputStream(runtime.getJarEntry(name + ".class")));
                StringWriter errors = new StringWriter();
                CheckClassAdapter.verify(reader, VerifyCoreFacing.class.getClassLoader(), false, new PrintWriter(errors));
                require(errors.toString().isBlank(), "ASM runtime type check failed: " + errors);
            }
        }
        Files.writeString(Path.of(arguments[3]), "{\"passed\":true,\"assertions\":" + assertions
                + ",\"rotation_mirror_cases\":28,\"scope_guard_cases\":10,\"exact_mixin_selectors_checked\":true,\"kubejs_fallback_checked\":true,\"game_mixin_application_tested\":false}\n");
        System.out.println("Core rotation/mirror, scope guards, Forge selectors and ASM: " + assertions + " assertions passed");
    }
}
