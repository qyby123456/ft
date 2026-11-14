import com.google.gson.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;
import net.farmingtales.ftboss.boss.ai.NativeSparrowHitEvasionInstaller;
public final class SparrowHitEvasionComposerV2 implements Opcodes {
    private static final String HELPER="net/farmingtales/ftboss/boss/ai/SparrowHitEvasion",RULES="net/farmingtales/ftboss/util/SparrowHitEvasionRules";
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    private static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static ClassNode read(byte[] bytes){ClassNode type=new ClassNode();new ClassReader(bytes).accept(type,0);return type;}
    private static byte[] bytes(ClassNode type){ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);type.accept(writer);return writer.toByteArray();}
    private static String graph(MethodNode method){Textifier text=new Textifier();method.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    public static void main(String[] arguments)throws Exception {
        JsonObject proof=JsonParser.parseString(Files.readString(Path.of(arguments[1]))).getAsJsonObject();JsonObject members=proof.getAsJsonObject("memberNames"),pins=proof.getAsJsonObject("originalClassHashes");
        Remapper mapper=new Remapper(){
            public String map(String name){
                if(name.equals("net/minecraft/world/level/pathfinder/BlockPathTypes"))return "net/minecraft/world/level/pathfinder/PathType";
                if(name.equals("net/minecraftforge/fml/common/Mod$EventBusSubscriber"))return "net/neoforged/fml/common/EventBusSubscriber";
                if(name.equals("net/minecraftforge/fml/common/Mod$EventBusSubscriber$Bus"))return "net/neoforged/fml/common/EventBusSubscriber$Bus";
                if(name.startsWith("net/minecraftforge/eventbus/api/"))return name.replace("net/minecraftforge/eventbus/api/","net/neoforged/bus/api/");
                if(name.startsWith("net/minecraftforge/event/"))return name.replace("net/minecraftforge/event/","net/neoforged/neoforge/event/");
                return name;
            }
            public String mapMethodName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}
            public String mapFieldName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}
        };
        int mappedMethods=0,verified=0;Path destination=Path.of(arguments[2]);
        try(ZipFile original=new ZipFile(arguments[0])) {
            for(var entry:pins.entrySet()) {
                String path=entry.getKey();byte[] input=original.getInputStream(original.getEntry(path)).readAllBytes();require(sha(input).equals(entry.getValue().getAsString()),"Pinned original evasion bytes");
                ClassNode source=read(input),target=new ClassNode();source.accept(new ClassRemapper(target,mapper));
                for(MethodNode method:target.methods){new Analyzer<BasicValue>(new BasicVerifier()).analyze(target.name,method);verified++;}
                for(int index=0;index<source.methods.size();index++){MethodNode expected=new MethodNode();source.methods.get(index).accept(new MethodRemapper(expected,mapper));require(graph(expected).equals(graph(target.methods.get(index))),"Entire mapped original method graph");mappedMethods++;}
                byte[] payload=bytes(target);if(path.equals(RULES+".class")){require(target.name.equals(RULES),"Original pure rules identity");payload=input;}
                Path output=destination.resolve(path);Files.createDirectories(output.getParent());Files.write(output,payload,StandardOpenOption.CREATE_NEW);
            }
        }
        ClassNode actual=read(Files.readAllBytes(Path.of(arguments[3])));Map<String,String> before=new LinkedHashMap<>();for(MethodNode method:actual.methods)before.put(method.name+method.desc,graph(method));
        require(NativeSparrowHitEvasionInstaller.install(actual)==3,"Three exact integration points installed");NativeSparrowHitEvasionInstaller.verifyInstalled(actual);require(NativeSparrowHitEvasionInstaller.install(actual)==0,"Hook installer idempotent");net.farmingtales.ftboss.boss.ai.NativeManorDefenseInstaller.verifyInstalled(actual);
        int changed=0;for(MethodNode method:actual.methods){new Analyzer<BasicValue>(new BasicVerifier()).analyze(actual.name,method);if(!before.get(method.name+method.desc).equals(graph(method))){require(Set.of("hitFeedback","sparrowTick").contains(method.name),"Only original hook owner methods changed");changed++;}}
        require(changed==2,"Exactly hitFeedback and sparrowTick changed");
        MethodNode tick=actual.methods.stream().filter(method->method.name.equals("sparrowTick")).findFirst().orElseThrow();for(AbstractInsnNode instruction:tick.instructions.toArray())if(instruction instanceof MethodInsnNode call&&call.owner.equals(HELPER)&&call.name.equals("prepare")){tick.instructions.remove(instruction);break;}
        boolean rejected=false;try{NativeSparrowHitEvasionInstaller.verifyInstalled(actual);}catch(IllegalStateException expected){rejected=true;}require(rejected,"Missing prepare negative control rejected");
        System.out.println("failures=0;originalClasses=3;mappedOriginalMethodGraphs="+mappedMethods+";asmVerifiedOriginalMethods="+verified+";pureRulesByteIdentical=true;integrationHooks=3;changedLoadedMethods=2;idempotent=true;negativeControls=1;");
    }
}
