package net.farmingtales.ftboss.boss.ai;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
public final class NativeSparrowHitEvasionInstaller implements Opcodes {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity",HELPER="net/farmingtales/ftboss/boss/ai/SparrowHitEvasion",KITING="net/farmingtales/ftboss/boss/ai/ManorKiting";
    private static final String SOURCE="Lnet/minecraft/world/damagesource/DamageSource;";
    private NativeSparrowHitEvasionInstaller() {}
    private static void require(boolean condition,String label){if(!condition)throw new IllegalStateException(label);}
    private static MethodNode method(ClassNode type,String name){List<MethodNode> found=type.methods.stream().filter(method->method.name.equals(name)).toList();require(found.size()==1,"Unique original Sparrow method "+name);return found.getFirst();}
    private static List<MethodInsnNode> calls(MethodNode method,String owner,String name){List<MethodInsnNode> found=new ArrayList<>();for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals(owner)&&call.name.equals(name))found.add(call);return found;}
    public static int install(ClassNode target) {
        require(target.name.equals(BOSS),"Sparrow actual target class");
        int existing=0;for(MethodNode method:target.methods)for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals(HELPER))existing++;
        if(existing!=0){verifyInstalled(target);return 0;}
        MethodNode feedback=method(target,"hitFeedback"),tick=method(target,"sparrowTick");
        require(calls(feedback,"net/farmingtales/ftboss/boss/ai/ManorDefense","confirmedHit").size()==1,"Existing original defense hook remains present");
        List<MethodInsnNode> support=calls(tick,BOSS,"supportedBoss"),evade=calls(tick,KITING,"evadeSupport");
        require(support.size()==1&&support.getFirst().desc.equals("()L"+BOSS+";"),"Unique ordinary support target ingress");
        require(evade.size()==1&&evade.getFirst().getOpcode()==INVOKEVIRTUAL&&evade.getFirst().desc.equals("(L"+BOSS+";)Z"),"Unique original kiting hook shape");
        InsnList hit=new InsnList();hit.add(new VarInsnNode(ALOAD,0));hit.add(new VarInsnNode(ALOAD,1));hit.add(new MethodInsnNode(INVOKESTATIC,HELPER,"confirmedHit","(L"+BOSS+";"+SOURCE+")V",false));feedback.instructions.insert(hit);
        InsnList prepare=new InsnList();prepare.add(new VarInsnNode(ALOAD,0));prepare.add(new MethodInsnNode(INVOKESTATIC,HELPER,"prepare","(L"+BOSS+";L"+BOSS+";)L"+BOSS+";",false));tick.instructions.insert(support.getFirst(),prepare);
        MethodInsnNode old=evade.getFirst();tick.instructions.set(old,new MethodInsnNode(INVOKESTATIC,HELPER,"evadeSupport","(L"+KITING+";L"+BOSS+";)Z",false));
        feedback.maxStack=Math.max(feedback.maxStack,2);tick.maxStack=Math.max(tick.maxStack,3);verifyInstalled(target);return 3;
    }
    public static void verifyInstalled(ClassNode target) {
        require(target.name.equals(BOSS),"Sparrow actual verification target");MethodNode feedback=method(target,"hitFeedback"),tick=method(target,"sparrowTick");
        require(calls(feedback,HELPER,"confirmedHit").size()==1&&calls(tick,HELPER,"prepare").size()==1&&calls(tick,HELPER,"evadeSupport").size()==1,"Exactly three original Sparrow integration hooks");
        require(calls(tick,KITING,"evadeSupport").isEmpty(),"No duplicate direct kiting execution");
        List<AbstractInsnNode> feedbackCode=new ArrayList<>();for(AbstractInsnNode instruction:feedback.instructions)if(instruction.getOpcode()>=0)feedbackCode.add(instruction);
        require(feedbackCode.get(0) instanceof VarInsnNode self&&self.var==0&&feedbackCode.get(1) instanceof VarInsnNode source&&source.var==1&&feedbackCode.get(2)==calls(feedback,HELPER,"confirmedHit").getFirst(),"Original evasion confirmed hit precedes existing defense hook");
        for(MethodInsnNode call:calls(tick,HELPER,"prepare"))require(call.getOpcode()==INVOKESTATIC&&call.desc.equals("(L"+BOSS+";L"+BOSS+";)L"+BOSS+";"),"Original prepare descriptor");
        for(MethodInsnNode call:calls(tick,HELPER,"evadeSupport"))require(call.getOpcode()==INVOKESTATIC&&call.desc.equals("(L"+KITING+";L"+BOSS+";)Z"),"Original kiting descriptor");
    }
}
