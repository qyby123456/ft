import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PrivateFingerprintCompositionBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/item/";
    private static final String COMPOUND="net/minecraft/nbt/CompoundTag";
    private static final String BUILDER=PREFIX+"FoodWeaponMaterialFingerprint$PrivateFingerprint416";
    private static int finished;
    private static int normalizedTagPuts;
    private static int preserved;

    private static void require(boolean value,String reason) {
        if (!value) throw new AssertionError(reason);
    }

    private static ClassNode read(Path directory,String name) throws Exception {
        var node=new ClassNode();
        new ClassReader(Files.readAllBytes(directory.resolve(PREFIX+name+".class"))).accept(node,0);
        return node;
    }

    private static String body(MethodNode method,boolean normalize) {
        var copy=new MethodNode(method.access,method.name,method.desc,method.signature,method.exceptions.toArray(String[]::new));
        method.accept(copy);
        for (var instruction:copy.instructions.toArray()) {
            if (instruction instanceof FrameNode || instruction instanceof LineNumberNode) copy.instructions.remove(instruction);
            if (normalize && instruction instanceof TypeInsnNode type && type.desc.equals(BUILDER)) type.desc=COMPOUND;
            if (normalize && instruction instanceof MethodInsnNode call && call.owner.equals(BUILDER)) {
                if (call.name.equals("finish")) {
                    require(call.desc.equals("()Lnet/minecraft/nbt/CompoundTag;"),"Unexpected finish descriptor");
                    copy.instructions.remove(call);
                    finished++;
                } else {
                    require(List.of("<init>","put","putString","putInt","putBoolean").contains(call.name),"Unexpected private builder call");
                    call.owner=COMPOUND;
                    if (call.name.equals("put")) {
                        require(call.desc.equals("(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V"),"Unexpected private put descriptor");
                        call.desc="(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;";
                        copy.instructions.insert(call,new InsnNode(Opcodes.POP));
                        normalizedTagPuts++;
                    }
                }
            }
        }
        var text=new Textifier();
        copy.instructions.accept(new TraceMethodVisitor(text));
        return text.getText().toString();
    }

    public static void main(String[] arguments) throws Exception {
        Path before=Path.of(arguments[0]);
        Path after=Path.of(arguments[1]);
        for (String name:List.of("FoodWeaponSourceLedger","FoodWeaponExpectedTrace","NativeExpectedFieldObservation")) {
            require(java.util.Arrays.equals(Files.readAllBytes(before.resolve(PREFIX+name+".class")),Files.readAllBytes(after.resolve(PREFIX+name+".class"))),"Existing guard or timer class changed: "+name);
        }
        int added=0;
        for (String name:List.of("FoodWeaponMaterialFingerprint","FoodWeaponMaterialFingerprint$PrivateFingerprint416")) {
            var original=read(before,name);
            var changed=read(after,name);
            require(original.fields.stream().map(field->field.name+field.desc+field.access).toList().equals(changed.fields.stream().map(field->field.name+field.desc+field.access).toList()),"Existing state fields changed");
            for (var method:changed.methods) {
                new Analyzer<>(new BasicVerifier()).analyze(changed.name,method);
                var prior=original.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElse(null);
                if (prior==null) {
                    require(name.endsWith("$PrivateFingerprint416") && method.name.equals("putBoolean") && method.desc.equals("(Ljava/lang/String;Z)V") && (method.access&Opcodes.ACC_PRIVATE)!=0,"Unexpected added method");
                    for (var instruction:method.instructions) {
                        require(!(instruction instanceof FieldInsnNode field) || field.getOpcode()!=Opcodes.PUTFIELD && field.getOpcode()!=Opcodes.PUTSTATIC,"Boolean encoder writes object state");
                        if (instruction instanceof MethodInsnNode call) require(call.owner.equals("net/minecraft/nbt/ByteTag") && call.name.equals("valueOf") || call.owner.equals(BUILDER) && call.name.equals("put"),"Unexpected boolean encoder call");
                    }
                    added++;
                } else {
                    require(body(prior,false).equals(body(method,name.equals("FoodWeaponMaterialFingerprint") && method.name.equals("composition"))),"Existing method semantics changed: "+name+"."+method.name);
                    preserved++;
                }
            }
            require(changed.methods.size()==original.methods.size()+(name.endsWith("$PrivateFingerprint416")?1:0),"Existing method removed");
        }
        require(added==1 && finished==3 && normalizedTagPuts==4,"Unexpected private-output delta");
        System.out.println("failures=0;existingMethodsPreserved="+preserved+";onlyPrivateOutputFinishes="+finished+";addedBooleanEncoder="+added+";originalMapGuardByteIdentical=true;timerClassesByteIdentical=true;fieldAndValueSemanticsPreserved=true");
    }
}
