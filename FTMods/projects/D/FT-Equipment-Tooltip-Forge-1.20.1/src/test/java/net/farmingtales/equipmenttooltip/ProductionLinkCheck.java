package net.farmingtales.equipmenttooltip;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class ProductionLinkCheck {
    private static final Set<String> VERIFIED = new HashSet<>();

    public static void main(String[] arguments) throws Exception {
        try (ZipFile artifact = new ZipFile(arguments[0])) {
            for (var entry : artifact.stream().filter(file -> file.getName().endsWith(".class")).toList()) {
                ClassReader reader = new ClassReader(artifact.getInputStream(entry));
                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitMethodInsn(int opcode, String owner, String member, String desc, boolean isInterface) {
                                verify(owner, member, desc, true);
                            }

                            @Override
                            public void visitFieldInsn(int opcode, String owner, String member, String desc) {
                                verify(owner, member, desc, false);
                            }
                        };
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        }
        System.out.println("PASS " + VERIFIED.size() + " production Minecraft/Forge member references");
    }

    private static void verify(String owner, String name, String descriptor, boolean method) {
        if (!owner.startsWith("net/minecraft/") && !owner.startsWith("net/minecraftforge/")) {
            return;
        }
        String key = owner + "." + name + descriptor;
        if (VERIFIED.contains(key)) {
            return;
        }
        try {
            Class<?> type = Class.forName(owner.replace('/', '.'), false, ProductionLinkCheck.class.getClassLoader());
            if (!exists(type, name, descriptor, method, new HashSet<>())) {
                throw new AssertionError("Unresolved production reference: " + key);
            }
            VERIFIED.add(key);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Cannot link " + key, exception);
        }
    }

    private static boolean exists(Class<?> type, String name, String descriptor, boolean method, Set<Class<?>> visited) {
        if (type == null || !visited.add(type)) {
            return false;
        }
        if (method) {
            if (name.equals("<init>")) {
                for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                    if (Type.getConstructorDescriptor(constructor).equals(descriptor)) return true;
                }
                return false;
            }
            for (Method candidate : type.getDeclaredMethods()) {
                if (candidate.getName().equals(name) && Type.getMethodDescriptor(candidate).equals(descriptor)) return true;
            }
        } else {
            for (Field candidate : type.getDeclaredFields()) {
                if (candidate.getName().equals(name) && Type.getDescriptor(candidate.getType()).equals(descriptor)) return true;
            }
        }
        if (exists(type.getSuperclass(), name, descriptor, method, visited)) return true;
        for (Class<?> parent : type.getInterfaces()) {
            if (exists(parent, name, descriptor, method, visited)) return true;
        }
        return false;
    }
}
