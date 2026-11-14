import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import net.farmingtales.ftboss.boss.ai.NativeManorFinalDamageInstaller;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class ManorDamageContextWrapperProofV1 implements Opcodes {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String SOURCE="net/minecraft/world/damagesource/DamageSource";
    private static final String CONTAINER="net/neoforged/neoforge/common/damagesource/DamageContainer";
    private static final RuntimeException FAILURE=new IllegalStateException("owned context exception identity control");
    private static int checks,mode,depth,nestedCalls;
    private static Method reduce;
    private static void require(boolean condition,String label) {checks++;if(!condition)throw new AssertionError(label);}
    private static Stack<Object> stack(Object boss)throws Exception {return (Stack<Object>)boss.getClass().getField("damageContainers").get(boss);}
    public static float armor(Object boss,float amount)throws Exception {
        Stack<Object> containers=stack(boss);require(!containers.empty(),"Armor sees owned context");
        require(containers.peek().getClass().getName().equals(CONTAINER.replace('/','.')),"Owned context has the actual platform type name");
        require(containers.peek().getClass().getField("originalDamage").getFloat(containers.peek())==amount,"Owned context receives the invocation input");
        if(mode==1)throw FAILURE;
        return amount*0.8F;
    }
    public static float magic(Object boss,float amount)throws Exception {
        Stack<Object> containers=stack(boss);require(!containers.empty(),"Magic sees owned context");
        if(mode==2)throw FAILURE;
        if(mode==3&&depth==0){
            Object outer=containers.peek();depth++;
            float inner=(Float)reduce.invoke(boss,null,50.0F);nestedCalls++;depth--;
            require(Math.abs(inner-30.0F)<0.0001F,"Nested reduction result preserved");
            require(containers.peek()==outer,"Nested invocation restored its caller context identity");
        }
        return amount*0.75F;
    }
    private static ClassNode base(String name) {
        ClassNode type=new ClassNode();type.version=V21;type.access=ACC_PUBLIC;type.name=name;type.superName="java/lang/Object";
        return type;
    }
    private static MethodNode constructor(String descriptor) {
        MethodNode method=new MethodNode(ACC_PUBLIC,"<init>",descriptor,null,null);
        method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new MethodInsnNode(INVOKESPECIAL,"java/lang/Object","<init>","()V",false));return method;
    }
    private static byte[] bytes(ClassNode type) {ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);type.accept(writer);return writer.toByteArray();}
    private static MethodNode template()throws Exception {Method factory=NativeManorFinalDamageInstaller.class.getDeclaredMethod("contextWrapper");factory.setAccessible(true);return (MethodNode)factory.invoke(null);}
    private static boolean rejected(MethodNode method)throws Exception {
        Method verifier=NativeManorFinalDamageInstaller.class.getDeclaredMethod("verifyContextWrapper",MethodNode.class);verifier.setAccessible(true);
        try{verifier.invoke(null,method);return false;}catch(InvocationTargetException error){return error.getCause() instanceof IllegalStateException;}
    }
    public static void main(String[] arguments)throws Exception {
        Map<String,byte[]> definitions=new HashMap<>();
        ClassNode source=base(SOURCE);MethodNode sourceConstructor=constructor("()V");sourceConstructor.instructions.add(new InsnNode(RETURN));source.methods.add(sourceConstructor);definitions.put(SOURCE.replace('/','.'),bytes(source));
        ClassNode container=base(CONTAINER);container.fields.add(new FieldNode(ACC_PUBLIC,"originalDamage","F",null,null));
        MethodNode containerConstructor=constructor("(L"+SOURCE+";F)V");containerConstructor.instructions.add(new VarInsnNode(ALOAD,0));containerConstructor.instructions.add(new VarInsnNode(FLOAD,2));containerConstructor.instructions.add(new FieldInsnNode(PUTFIELD,CONTAINER,"originalDamage","F"));containerConstructor.instructions.add(new InsnNode(RETURN));container.methods.add(containerConstructor);definitions.put(CONTAINER.replace('/','.'),bytes(container));
        ClassNode boss=base(BOSS);boss.fields.add(new FieldNode(ACC_PUBLIC,"damageContainers","Ljava/util/Stack;",null,null));
        MethodNode bossConstructor=constructor("()V");bossConstructor.instructions.add(new VarInsnNode(ALOAD,0));bossConstructor.instructions.add(new TypeInsnNode(NEW,"java/util/Stack"));bossConstructor.instructions.add(new InsnNode(DUP));bossConstructor.instructions.add(new MethodInsnNode(INVOKESPECIAL,"java/util/Stack","<init>","()V",false));bossConstructor.instructions.add(new FieldInsnNode(PUTFIELD,BOSS,"damageContainers","Ljava/util/Stack;"));bossConstructor.instructions.add(new InsnNode(RETURN));boss.methods.add(bossConstructor);
        for(String name:new String[]{"getDamageAfterArmorAbsorb","getDamageAfterMagicAbsorb"}) {
            MethodNode callback=new MethodNode(ACC_PUBLIC,name,"(L"+SOURCE+";F)F",null,null);callback.instructions.add(new VarInsnNode(ALOAD,0));callback.instructions.add(new VarInsnNode(FLOAD,2));callback.instructions.add(new MethodInsnNode(INVOKESTATIC,"ManorDamageContextWrapperProofV1",name.contains("Armor")?"armor":"magic","(Ljava/lang/Object;F)F",false));callback.instructions.add(new InsnNode(FRETURN));boss.methods.add(callback);
        }
        boss.methods.add(template());definitions.put(BOSS.replace('/','.'),bytes(boss));
        ClassLoader isolated=new ClassLoader(ManorDamageContextWrapperProofV1.class.getClassLoader()) {
            protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
                if(!definitions.containsKey(name))return super.loadClass(name,resolve);
                Class<?> loaded=findLoadedClass(name);if(loaded==null){byte[] payload=definitions.get(name);loaded=defineClass(name,payload,0,payload.length);}if(resolve)resolveClass(loaded);return loaded;
            }
        };
        Class<?> bossType=isolated.loadClass(BOSS.replace('/','.'));reduce=bossType.getMethod("ftboss$reduceManorDamage",isolated.loadClass(SOURCE.replace('/','.')),float.class);
        int invocations=0;
        for(int outerDepth=0;outerDepth<=1;outerDepth++)for(int selectedMode=0;selectedMode<=3;selectedMode++) {
            Object instance=bossType.getConstructor().newInstance();Stack<Object> containers=stack(instance);Object outer=new Object();if(outerDepth==1)containers.push(outer);mode=selectedMode;
            try {float result=(Float)reduce.invoke(instance,null,100.0F);require(mode==0||mode==3,"Expected exceptional callback did throw");require(Math.abs(result-60.0F)<0.0001F,"Original armor then magic arithmetic preserved");}
            catch(InvocationTargetException error){require((mode==1||mode==2)&&error.getCause()==FAILURE,"Original callback exception identity preserved");}
            require(containers.size()==outerDepth,"Owned context removed on success and both exception paths");require(outerDepth==0||containers.peek()==outer,"Caller context identity preserved");invocations++;
        }
        require(nestedCalls==2,"Nested real generated-wrapper invocations exercised with and without outer context");
        int negatives=0;MethodNode missingFinally=template();missingFinally.tryCatchBlocks.clear();if(rejected(missingFinally))negatives++;
        MethodNode missingPush=template();for(AbstractInsnNode instruction:missingPush.instructions.toArray())if(instruction instanceof MethodInsnNode call&&call.name.equals("push")){missingPush.instructions.remove(instruction);break;}if(rejected(missingPush))negatives++;
        MethodNode wrongBoundary=template();wrongBoundary.tryCatchBlocks.getFirst().start=wrongBoundary.tryCatchBlocks.getFirst().end;if(rejected(wrongBoundary))negatives++;
        MethodNode wrongArithmetic=template();for(AbstractInsnNode instruction:wrongArithmetic.instructions)if(instruction instanceof MethodInsnNode call&&call.name.equals("getDamageAfterMagicAbsorb")){call.name="getDamageAfterArmorAbsorb";break;}if(rejected(wrongArithmetic))negatives++;
        require(negatives==4,"All context corruption negative controls rejected");
        System.out.println("failures=0;checks="+checks+";generatedWrapperInvocations="+invocations+";nestedInvocations="+nestedCalls+";exceptionIdentityPreserved=true;callerContextPreserved=true;negativeControls="+negatives+";syntheticLifecycleProofOnly=true;");
    }
}
