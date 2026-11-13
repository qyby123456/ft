import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PrivateTimerImageBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/item/";
    private static int preserved;
    private static int added;
    private static int normalizedCalls;

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
            if (normalize && instruction instanceof MethodInsnNode call && call.owner.equals(PREFIX+"FoodWeaponExpectedTrace") && call.name.equals("timerExpectationForRead")) {
                call.name="materialize";
                normalizedCalls++;
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
            var original=read(before,name);
            var changed=read(after,name);
            require(original.fields.stream().map(field->field.name+field.desc+field.access).toList().equals(changed.fields.stream().map(field->field.name+field.desc+field.access).toList()),"Existing state fields changed");
            for (var method:changed.methods) {
                new Analyzer<>(new BasicVerifier()).analyze(changed.name,method);
                var prior=original.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElse(null);
                if (prior==null) {
                    boolean expected=name.equals("FoodWeaponExpectedTrace") && (List.of("timerExpectationForRead","materializeTimerExpectation").contains(method.name) || method.name.startsWith("lambda$timerExpectationForRead$")) || name.equals("NativeExpectedFieldObservation") && method.name.equals("timerExpectationCompoundForRead");
                    require(expected,"Unexpected new method: "+name+"."+method.name);
                    added++;
                    if (method.name.equals("materializeTimerExpectation")) {
                        for (var instruction:method.instructions) {
                            require(!(instruction instanceof FieldInsnNode field) || field.getOpcode()!=Opcodes.PUTSTATIC && field.getOpcode()!=Opcodes.PUTFIELD,"Expected materializer writes object or authority state");
                            if (instruction instanceof MethodInsnNode call) require(!call.owner.equals("net/minecraft/nbt/CompoundTag") || !call.name.equals("put"),"Expected materializer re-enters observed CompoundTag.put");
                        }
                    }
                } else {
                    boolean normalize=name.equals("FoodWeaponSourceLedger") && method.name.equals("nativeTimerExpectationReturned");
                    require(body(prior,false).equals(body(method,normalize)),"Existing method changed beyond two exact planner calls: "+name+"."+method.name);
                    preserved++;
                }
            }
            for (var prior:original.methods) require(changed.methods.stream().anyMatch(method->method.name.equals(prior.name) && method.desc.equals(prior.desc)),"Original method removed");
        }
        require(added==5 && normalizedCalls==2,"Unexpected private read factory boundary");
        System.out.println("failures=0;originalMethodBodiesPreserved="+preserved+";onlyTwoPlannerCallsNormalized=2;addedPrivateReadMethodsAndLambdas=5;originalMapGuardUnchanged=true;immutablePrivateFieldsUnchanged=true");
    }
}
