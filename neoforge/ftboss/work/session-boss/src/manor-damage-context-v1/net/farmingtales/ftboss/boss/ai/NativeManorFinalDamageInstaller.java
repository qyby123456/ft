package net.farmingtales.ftboss.boss.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorFinalDamageInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String FINAL = "net/farmingtales/ftboss/boss/ai/ManorFinalDamage";
    private static final String ACCESS = "net/farmingtales/ftboss/boss/ai/NativeManorFinalDamageAccess";
    private static final String SOURCE = "Lnet/minecraft/world/damagesource/DamageSource;";
    private static final String SETTLE = "(L" + BOSS + ";" + SOURCE + "F)[F";
    private static final String EXPECTED = "7c822d39d56baa2e7157fc1d208bad9b7e9910422acc5f2beb8c2e0f79c311db";

    private NativeManorFinalDamageInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);
        if (isInstalled(target)) {verifyInstalled(target);return 0;}
        NativeManorDefenseInstaller.verifyInstalled(target);
        if (target.interfaces.contains(ACCESS) || target.methods.stream().anyMatch(method -> method.name.startsWith("ftboss$") && method.name.endsWith("ManorDamage") || method.name.equals("ftboss$manorSparrow"))) throw new IllegalStateException("Partial native final-damage accessor installation");
        MethodNode manor = method(target,"manorHurt","(" + SOURCE + "F)Z");
        AbstractInsnNode[] bounds = bounds(manor);
        List<AbstractInsnNode> original = new ArrayList<>();
        for (AbstractInsnNode instruction=bounds[0];instruction!=bounds[1];instruction=instruction.getNext()) original.add(instruction);
        if (!fingerprint(original,bounds[1]).equals(EXPECTED)) throw new IllegalStateException("Changed original native settlement block");
        MethodNode hurt = method(target,"hurt","(" + SOURCE + "F)Z");
        MethodInsnNode gate = unique(hurt,"net/farmingtales/ftboss/boss/ai/BossDamageGate","gate","(Lnet/minecraft/world/entity/LivingEntity;" + SOURCE + "F)F");
        method(target,"nearbySparrow","()L" + BOSS + ";");method(target,"sparrowShare","(F" + SOURCE + ")F");method(target,"youziMinionShare","(F" + SOURCE + ")F");method(target,"blocksCombatDamage","()Z");
        InsnList replacement = new InsnList();LabelNode ready = new LabelNode();
        replacement.add(new VarInsnNode(Opcodes.ALOAD,0));replacement.add(new VarInsnNode(Opcodes.ALOAD,1));replacement.add(new VarInsnNode(Opcodes.FLOAD,2));replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC,FINAL,"settle",SETTLE,false));replacement.add(new VarInsnNode(Opcodes.ASTORE,6));replacement.add(new VarInsnNode(Opcodes.ALOAD,6));replacement.add(new JumpInsnNode(Opcodes.IFNONNULL,ready));replacement.add(new InsnNode(Opcodes.ICONST_0));replacement.add(new InsnNode(Opcodes.IRETURN));replacement.add(ready);
        replacement.add(new VarInsnNode(Opcodes.ALOAD,6));replacement.add(new InsnNode(Opcodes.ICONST_0));replacement.add(new InsnNode(Opcodes.FALOAD));replacement.add(new VarInsnNode(Opcodes.FSTORE,3));replacement.add(new VarInsnNode(Opcodes.ALOAD,6));replacement.add(new InsnNode(Opcodes.ICONST_1));replacement.add(new InsnNode(Opcodes.FALOAD));replacement.add(new VarInsnNode(Opcodes.FSTORE,5));
        manor.instructions.insertBefore(bounds[0],replacement);
        for (AbstractInsnNode instruction:original) manor.instructions.remove(instruction);
        for (AbstractInsnNode instruction:manor.instructions.toArray()) if (instruction instanceof FrameNode) manor.instructions.remove(instruction);
        manor.maxLocals=Math.max(manor.maxLocals,7);manor.maxStack=Math.max(manor.maxStack,3);
        gate.owner=FINAL;gate.name="legacyGate";
        target.interfaces.add(ACCESS);
        target.methods.add(contextWrapper());
        target.methods.add(wrapper("ftboss$manorSparrow","()L"+BOSS+";",List.of(new VarInsnNode(Opcodes.ALOAD,0),invoke("nearbySparrow","()L"+BOSS+";"),new InsnNode(Opcodes.ARETURN)),1,1));
        target.methods.add(wrapper("ftboss$shareManorDamage","(F"+SOURCE+")F",List.of(new VarInsnNode(Opcodes.ALOAD,0),new VarInsnNode(Opcodes.ALOAD,0),new VarInsnNode(Opcodes.FLOAD,1),new VarInsnNode(Opcodes.ALOAD,2),invoke("sparrowShare","(F"+SOURCE+")F"),new VarInsnNode(Opcodes.ALOAD,2),invoke("youziMinionShare","(F"+SOURCE+")F"),new InsnNode(Opcodes.FRETURN)),4,3));
        target.methods.add(wrapper("ftboss$blocksManorDamage","()Z",List.of(new VarInsnNode(Opcodes.ALOAD,0),invoke("blocksCombatDamage","()Z"),new InsnNode(Opcodes.IRETURN)),1,1));
        verifyInstalled(target);NativeManorDefenseInstaller.verifyInstalled(target);NativeManorRaidInstaller.verifyInstalled(target);return 2;
    }

    public static boolean isInstalled(ClassNode target) {
        return target.methods.stream().flatMap(method -> code(method).stream()).anyMatch(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(FINAL));
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        if (target.interfaces.stream().filter(name -> name.equals(ACCESS)).count()!=1) throw new IllegalStateException("Missing native final-damage accessor interface");
        MethodNode manor=method(target,"manorHurt","("+SOURCE+"F)Z");List<AbstractInsnNode> instructions=code(manor);MethodInsnNode settle=unique(manor,FINAL,"settle",SETTLE);int position=instructions.indexOf(settle);
        if (position<3 || !variable(instructions.get(position-3),Opcodes.ALOAD,0) || !variable(instructions.get(position-2),Opcodes.ALOAD,1) || !variable(instructions.get(position-1),Opcodes.FLOAD,2) || !variable(instructions.get(position+1),Opcodes.ASTORE,6) || !variable(instructions.get(position+2),Opcodes.ALOAD,6) || !(instructions.get(position+3) instanceof JumpInsnNode jump) || jump.getOpcode()!=Opcodes.IFNONNULL || next(jump.label)!=instructions.get(position+6) || instructions.get(position+4).getOpcode()!=Opcodes.ICONST_0 || instructions.get(position+5).getOpcode()!=Opcodes.IRETURN) throw new IllegalStateException("Changed settlement arguments or null cancellation guard");
        int[] opcodes={Opcodes.ALOAD,Opcodes.ICONST_0,Opcodes.FALOAD,Opcodes.FSTORE,Opcodes.ALOAD,Opcodes.ICONST_1,Opcodes.FALOAD,Opcodes.FSTORE};
        for(int index=0;index<opcodes.length;index++) if(instructions.get(position+6+index).getOpcode()!=opcodes[index]) throw new IllegalStateException("Changed full settlement result extraction");
        if(!variable(instructions.get(position+6),Opcodes.ALOAD,6)||!variable(instructions.get(position+9),Opcodes.FSTORE,3)||!variable(instructions.get(position+10),Opcodes.ALOAD,6)||!variable(instructions.get(position+13),Opcodes.FSTORE,5)||!variable(instructions.get(position+14),Opcodes.ALOAD,0)||!(instructions.get(position+15) instanceof IntInsnNode value)||value.operand!=10||!(instructions.get(position+16) instanceof FieldInsnNode timer)||!timer.name.equals("invulnerableTime")) throw new IllegalStateException("Changed post-settlement native damage/absorption locals or timing");
        if(instructions.stream().anyMatch(instruction -> instruction instanceof MethodInsnNode call && (call.name.equals("refreshClampBounds")||call.name.equals("setAbsorptionAmount")||call.name.equals("sparrowShare")||call.name.equals("youziMinionShare")||call.owner.endsWith("/ManorAdaptiveShield")||call.owner.endsWith("/ManorDefense")))) throw new IllegalStateException("Old settlement or duplicate direct reductions remain");
        unique(method(target,"hurt","("+SOURCE+"F)Z"),FINAL,"legacyGate","(Lnet/minecraft/world/entity/LivingEntity;"+SOURCE+"F)F");
        verifyContextWrapper(method(target,"ftboss$reduceManorDamage","("+SOURCE+"F)F"));
        verifyWrapper(target,"ftboss$manorSparrow","()L"+BOSS+";",new int[]{Opcodes.ALOAD,Opcodes.INVOKEVIRTUAL,Opcodes.ARETURN},new String[]{"nearbySparrow"},new int[]{0});
        verifyWrapper(target,"ftboss$shareManorDamage","(F"+SOURCE+")F",new int[]{Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.FLOAD,Opcodes.ALOAD,Opcodes.INVOKEVIRTUAL,Opcodes.ALOAD,Opcodes.INVOKEVIRTUAL,Opcodes.FRETURN},new String[]{"sparrowShare","youziMinionShare"},new int[]{0,0,1,2,2});
        verifyWrapper(target,"ftboss$blocksManorDamage","()Z",new int[]{Opcodes.ALOAD,Opcodes.INVOKEVIRTUAL,Opcodes.IRETURN},new String[]{"blocksCombatDamage"},new int[]{0});
    }

    private static void verifyWrapper(ClassNode target,String name,String descriptor,int[] opcodes,String[] calls,int[] locals) {
        MethodNode method=method(target,name,descriptor);List<AbstractInsnNode> instructions=code(method);int callIndex=0,localIndex=0;
        if((method.access&(Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL))!=(Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL)||instructions.size()!=opcodes.length) throw new IllegalStateException("Changed native wrapper shape");
        for(int index=0;index<opcodes.length;index++) {
            AbstractInsnNode instruction=instructions.get(index);if(instruction.getOpcode()!=opcodes[index])throw new IllegalStateException("Changed native wrapper opcode");
            if(instruction instanceof VarInsnNode variable && variable.var!=locals[localIndex++])throw new IllegalStateException("Changed native wrapper local");
            if(instruction instanceof MethodInsnNode call && (!call.owner.equals(BOSS)||!call.name.equals(calls[callIndex++])||call.itf||!call.desc.equals(name.equals("ftboss$manorSparrow")?"()L"+BOSS+";":name.equals("ftboss$blocksManorDamage")?"()Z":name.equals("ftboss$shareManorDamage")?"(F"+SOURCE+")F":"("+SOURCE+"F)F")))throw new IllegalStateException("Changed genuine native wrapper target");
        }
    }

    private static AbstractInsnNode[] bounds(MethodNode method) {
        List<AbstractInsnNode> instructions=code(method);MethodInsnNode refresh=unique(method,BOSS,"refreshClampBounds","()V");int start=instructions.indexOf(refresh)-1;
        List<FieldInsnNode> timers=instructions.stream().filter(instruction -> instruction instanceof FieldInsnNode field && field.getOpcode()==Opcodes.PUTFIELD && field.name.equals("invulnerableTime")&&field.desc.equals("I")).map(instruction -> (FieldInsnNode)instruction).toList();
        if(timers.size()!=1||!variable(instructions.get(start),Opcodes.ALOAD,0))throw new IllegalStateException("Changed native settlement boundaries");int end=instructions.indexOf(timers.getFirst())-2;
        if(end<=start||!variable(instructions.get(end),Opcodes.ALOAD,0)||!(instructions.get(end+1) instanceof IntInsnNode value)||value.operand!=10)throw new IllegalStateException("Changed original native invulnerability timer");
        return new AbstractInsnNode[]{instructions.get(start),instructions.get(end)};
    }

    private static String fingerprint(List<AbstractInsnNode> nodes,AbstractInsnNode tail) {
        List<AbstractInsnNode> instructions=nodes.stream().filter(instruction->instruction.getOpcode()>=0).toList();StringBuilder result=new StringBuilder();
        for(AbstractInsnNode instruction:instructions) {
            result.append(instruction.getOpcode());
            if(instruction instanceof VarInsnNode variable)result.append(':').append(variable.var);
            else if(instruction instanceof FieldInsnNode field)result.append(':').append(field.owner).append(':').append(field.name).append(':').append(field.desc);
            else if(instruction instanceof MethodInsnNode call)result.append(':').append(call.owner).append(':').append(call.name).append(':').append(call.desc).append(':').append(call.itf);
            else if(instruction instanceof TypeInsnNode type)result.append(':').append(type.desc);
            else if(instruction instanceof IntInsnNode value)result.append(':').append(value.operand);
            else if(instruction instanceof LdcInsnNode constant)result.append(':').append(constant.cst.getClass().getName()).append(':').append(constant.cst);
            else if(instruction instanceof JumpInsnNode jump) {AbstractInsnNode destination=next(jump.label);int index=destination==tail?instructions.size():instructions.indexOf(destination);if(index<0)throw new IllegalStateException("Unexpected settlement branch exits block");result.append(':').append(index);}
            else if(!(instruction instanceof InsnNode))throw new IllegalStateException("Unsupported original native settlement instruction");
            result.append('\n');
        }
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(result.toString().getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException failure){throw new IllegalStateException(failure);}
    }

    private static MethodNode contextWrapper() {
        String container="net/neoforged/neoforge/common/damagesource/DamageContainer";
        MethodNode result=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL,"ftboss$reduceManorDamage","("+SOURCE+"F)F",null,null);
        LabelNode start=new LabelNode(),end=new LabelNode(),handler=new LabelNode();
        result.instructions.add(new TypeInsnNode(Opcodes.NEW,container));
        result.instructions.add(new InsnNode(Opcodes.DUP));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        result.instructions.add(new VarInsnNode(Opcodes.FLOAD,2));
        result.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,container,"<init>","("+SOURCE+"F)V",false));
        result.instructions.add(new VarInsnNode(Opcodes.ASTORE,3));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        result.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,BOSS,"damageContainers","Ljava/util/Stack;"));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));
        result.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"java/util/Stack","push","(Ljava/lang/Object;)Ljava/lang/Object;",false));
        result.instructions.add(new InsnNode(Opcodes.POP));
        result.instructions.add(start);
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        result.instructions.add(new VarInsnNode(Opcodes.FLOAD,2));
        result.instructions.add(invoke("getDamageAfterArmorAbsorb","("+SOURCE+"F)F"));
        result.instructions.add(new VarInsnNode(Opcodes.FSTORE,2));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        result.instructions.add(new VarInsnNode(Opcodes.FLOAD,2));
        result.instructions.add(invoke("getDamageAfterMagicAbsorb","("+SOURCE+"F)F"));
        result.instructions.add(new VarInsnNode(Opcodes.FSTORE,4));
        result.instructions.add(end);
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        result.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,BOSS,"damageContainers","Ljava/util/Stack;"));
        result.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"java/util/Stack","pop","()Ljava/lang/Object;",false));
        result.instructions.add(new InsnNode(Opcodes.POP));
        result.instructions.add(new VarInsnNode(Opcodes.FLOAD,4));
        result.instructions.add(new InsnNode(Opcodes.FRETURN));
        result.instructions.add(handler);
        result.instructions.add(new FrameNode(Opcodes.F_FULL,4,new Object[]{BOSS,"net/minecraft/world/damagesource/DamageSource",Opcodes.FLOAT,container},1,new Object[]{"java/lang/Throwable"}));
        result.instructions.add(new VarInsnNode(Opcodes.ASTORE,5));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        result.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,BOSS,"damageContainers","Ljava/util/Stack;"));
        result.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"java/util/Stack","pop","()Ljava/lang/Object;",false));
        result.instructions.add(new InsnNode(Opcodes.POP));
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,5));
        result.instructions.add(new InsnNode(Opcodes.ATHROW));
        result.tryCatchBlocks.add(new TryCatchBlockNode(start,end,handler,null));
        result.maxStack=4;result.maxLocals=6;
        return result;
    }

    private static void verifyContextWrapper(MethodNode actual) {
        MethodNode expected=contextWrapper();
        if(actual.access!=expected.access || !fingerprint(code(actual),null).equals(fingerprint(code(expected),null)) || actual.tryCatchBlocks.size()!=1) throw new IllegalStateException("Changed owned NeoForge damage context wrapper");
        TryCatchBlockNode actualHandler=actual.tryCatchBlocks.getFirst(),expectedHandler=expected.tryCatchBlocks.getFirst();
        List<AbstractInsnNode> actualCode=code(actual),expectedCode=code(expected);
        if(actualHandler.type!=null || actualCode.indexOf(next(actualHandler.start))!=expectedCode.indexOf(next(expectedHandler.start)) || actualCode.indexOf(next(actualHandler.end))!=expectedCode.indexOf(next(expectedHandler.end)) || actualCode.indexOf(next(actualHandler.handler))!=expectedCode.indexOf(next(expectedHandler.handler))) throw new IllegalStateException("Changed owned NeoForge damage context cleanup boundaries");
    }

    private static MethodNode wrapper(String name,String descriptor,List<AbstractInsnNode> instructions,int stack,int locals) {MethodNode method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL,name,descriptor,null,null);instructions.forEach(method.instructions::add);method.maxStack=stack;method.maxLocals=locals;return method;}
    private static MethodInsnNode invoke(String name,String descriptor) {return new MethodInsnNode(Opcodes.INVOKEVIRTUAL,BOSS,name,descriptor,false);}
    private static boolean variable(AbstractInsnNode instruction,int opcode,int slot) {return instruction instanceof VarInsnNode variable&&variable.getOpcode()==opcode&&variable.var==slot;}
    private static AbstractInsnNode next(AbstractInsnNode instruction) {while(instruction!=null&&instruction.getOpcode()<0)instruction=instruction.getNext();return instruction;}
    private static List<AbstractInsnNode> code(MethodNode method) {List<AbstractInsnNode> result=new ArrayList<>();for(AbstractInsnNode instruction:method.instructions)if(instruction.getOpcode()>=0)result.add(instruction);return result;}
    private static MethodNode method(ClassNode target,String name,String descriptor) {List<MethodNode> methods=target.methods.stream().filter(method -> method.name.equals(name)&&method.desc.equals(descriptor)&&(method.access&(Opcodes.ACC_STATIC|Opcodes.ACC_NATIVE|Opcodes.ACC_ABSTRACT))==0).toList();if(methods.size()!=1)throw new IllegalStateException("Missing genuine native final-damage method "+name);return methods.getFirst();}
    private static MethodInsnNode unique(MethodNode method,String owner,String name,String descriptor) {List<MethodInsnNode> calls=code(method).stream().filter(instruction->instruction instanceof MethodInsnNode call&&call.getOpcode()==((owner.equals(FINAL)||owner.endsWith("/BossDamageGate"))?Opcodes.INVOKESTATIC:Opcodes.INVOKEVIRTUAL)&&call.owner.equals(owner)&&call.name.equals(name)&&call.desc.equals(descriptor)).map(instruction->(MethodInsnNode)instruction).toList();if(calls.size()!=1)throw new IllegalStateException("Missing unique actual native final-damage call "+name);return calls.getFirst();}
    private static void requireTarget(ClassNode target) {if(target==null||!target.name.equals(BOSS))throw new IllegalStateException("Unsupported native final-damage target");}
}
