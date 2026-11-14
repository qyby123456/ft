import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;
public final class GongdiPublicContractProof implements Opcodes {
    private static final String ENTITY="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String RULES="net/farmingtales/ftboss/util/GongdiCombatRules";
    private static final String PLAYER="net/minecraft/world/entity/player/Player";
    private static final String LIVING="net/minecraft/world/entity/LivingEntity";
    private static ClassNode read(ZipFile jar,String name)throws Exception{ClassNode node=new ClassNode();new ClassReader(jar.getInputStream(jar.getEntry(name+".class")).readAllBytes()).accept(node,0);return node;}
    private static void require(boolean value,String text){if(!value)throw new AssertionError(text);}
    private static String body(MethodNode source){MethodNode copy=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(copy);for(AbstractInsnNode instruction:copy.instructions.toArray())if(instruction instanceof FrameNode||instruction instanceof LineNumberNode)copy.instructions.remove(instruction);Set<LabelNode> targets=new HashSet<>();for(AbstractInsnNode instruction:copy.instructions.toArray())if(instruction instanceof JumpInsnNode branch)targets.add(branch.label);for(AbstractInsnNode instruction:copy.instructions.toArray())if(instruction instanceof LabelNode label&&!targets.contains(label))copy.instructions.remove(label);Textifier text=new Textifier();copy.instructions.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    public static void main(String[] arguments)throws Exception{Path output=Path.of(arguments[2]);int constants=0,unchanged=0,verified=0;try(ZipFile base=new ZipFile(arguments[0]);ZipFile forge=new ZipFile(arguments[1])){
        ClassNode current=read(base,ENTITY),old=read(base,ENTITY),reference=read(forge,ENTITY);String name="onSignatureCleaveHit";MethodNode player=current.methods.stream().filter(method->method.name.equals(name)&&method.desc.contains(PLAYER)).findFirst().orElseThrow();String descriptor=player.desc.replace(PLAYER,LIVING);
        require(current.methods.stream().noneMatch(method->method.name.equals(name)&&method.desc.equals(descriptor)),"Living public hook already present");MethodNode authority=reference.methods.stream().filter(method->method.name.equals(name)&&method.desc.equals(descriptor)).findFirst().orElseThrow();MethodNode added=new MethodNode(player.access,name,descriptor,null,player.exceptions.toArray(String[]::new));player.accept(added);
        if(added.localVariables!=null)for(LocalVariableNode local:added.localVariables)local.desc=local.desc.replace(PLAYER,LIVING);
        for(AbstractInsnNode instruction:added.instructions.toArray())if(instruction instanceof MethodInsnNode call&&call.owner.equals("net/farmingtales/ftboss/boss/ai/GongdiSkills")&&call.name.equals("onAbilityHit"))call.desc=call.desc.replace(PLAYER,LIVING);
        require(body(added).equals(body(authority)),"Original public Living hook instructions differ");current.methods.add(added);
        for(int index=0;index<old.methods.size();index++){require(body(current.methods.get(index)).equals(body(old.methods.get(index))),"Previous entity method changed");unchanged++;}
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);current.accept(writer);ClassNode written=new ClassNode();new ClassReader(writer.toByteArray()).accept(written,0);for(MethodNode method:written.methods)if((method.access&(ACC_ABSTRACT|ACC_NATIVE))==0){new Analyzer<>(new BasicVerifier()).analyze(ENTITY,method);verified++;}Path path=output.resolve(ENTITY+".class");Files.createDirectories(path.getParent());Files.write(path,writer.toByteArray());
        current=read(base,RULES);old=read(base,RULES);reference=read(forge,RULES);Set<String> expected=Set.of("INNER_RADIUS","STRIKE_RANGE","PULL_RANGE","EXECUTE_RANGE","EXECUTE_COOLDOWN","EXECUTE_RESET");
        for(FieldNode field:current.fields){FieldNode original=reference.fields.stream().filter(value->value.name.equals(field.name)&&value.desc.equals(field.desc)).findFirst().orElseThrow();if(!Objects.equals(field.value,original.value)){require(expected.contains(field.name),"Unexpected constant difference:"+field.name);field.value=original.value;constants++;}}
        require(constants==6,"Expected six original constant corrections");for(int index=0;index<old.methods.size();index++)require(body(current.methods.get(index)).equals(body(old.methods.get(index))),"Pure rule method changed");writer=new ClassWriter(0);current.accept(writer);path=output.resolve(RULES+".class");Files.createDirectories(path.getParent());Files.write(path,writer.toByteArray());
        System.out.println("failures=0;originalLivingHookAdded=1;allExistingEntityMethodsPreserved="+unchanged+";methodsBasicVerified="+verified+";originalSharedConstantsRestored="+constants+";allRuleExecutableMethodsPreserved="+old.methods.size()+";existingInlinedConsumersNotRewritten=true");
    }}
}
