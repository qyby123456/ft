import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;

public final class Agent404 {
    public static Object watched;
    public static int writes;
    public static boolean reject;

    public static void changing(Object owner) {
        if (owner != watched) return;
        writes++;
        if (reject) throw new IllegalStateException("existing callback mutation observed");
    }

    public static void premain(String arguments, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> redefined, ProtectionDomain domain, byte[] bytes) {
                boolean compound = name.equals("net/minecraft/nbt/CompoundTag");
                if (!compound && !name.equals("net/minecraft/nbt/ListTag")) return null;
                ClassWriter output = new ClassWriter(0);
                new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9, output) {
                    @Override
                    public void visit(int version, int access, String type, String signature, String parent, String[] interfaces) {
                        String[] extended = java.util.Arrays.copyOf(interfaces, interfaces.length + 1);
                        extended[interfaces.length] = "net/farmingtales/ftboss/item/NativeExpectedFieldViews$" + (compound ? "Compound" : "Sequence");
                        super.visit(version, access, type, signature, parent, extended);
                    }

                    @Override
                    public MethodVisitor visitMethod(int access, String method, String descriptor, String signature, String[] exceptions) {
                        MethodVisitor original = super.visitMethod(access, method, descriptor, signature, exceptions);
                        if (!compound || !method.equals("put") || !descriptor.equals("(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;")) return original;
                        return new MethodVisitor(Opcodes.ASM9, original) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, "Agent404", "changing", "(Ljava/lang/Object;)V", false);
                            }
                        };
                    }

                    private void accessor(String method, String descriptor, String field, String fieldDescriptor, int returned) {
                        MethodVisitor visitor = super.visitMethod(Opcodes.ACC_PUBLIC, method, descriptor, null, null);
                        visitor.visitCode();
                        visitor.visitVarInsn(Opcodes.ALOAD, 0);
                        visitor.visitFieldInsn(Opcodes.GETFIELD, name, field, fieldDescriptor);
                        visitor.visitInsn(returned);
                        visitor.visitMaxs(1, 1);
                        visitor.visitEnd();
                    }

                    @Override
                    public void visitEnd() {
                        if (compound) accessor("ftboss$rawCompoundFields", "()Ljava/util/Map;", "tags", "Ljava/util/Map;", Opcodes.ARETURN);
                        else {
                            accessor("ftboss$rawListFields", "()Ljava/util/List;", "list", "Ljava/util/List;", Opcodes.ARETURN);
                            accessor("ftboss$rawListKind", "()B", "type", "B", Opcodes.IRETURN);
                        }
                        super.visitEnd();
                    }
                }, 0);
                return output.toByteArray();
            }
        });
    }
}
