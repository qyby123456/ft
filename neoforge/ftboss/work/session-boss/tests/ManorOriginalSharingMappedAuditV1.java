import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
public final class ManorOriginalSharingMappedAuditV1 {
    private static int checks;
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static ClassNode read(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_DEBUG);return node;}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    public static void main(String[] args)throws Exception {
        JsonObject proof=JsonParser.parseString(Files.readString(Path.of(args[1]))).getAsJsonObject(),members=proof.getAsJsonObject("memberNames");
        Remapper mapper=new Remapper(){public String mapMethodName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}public String mapFieldName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}};
        ClassNode original;
        try(ZipFile jar=new ZipFile(args[0])){original=read(jar.getInputStream(jar.getEntry("net/farmingtales/ftboss/entity/EasterBossEntity.class")).readAllBytes());}
        ClassNode actual=read(Files.readAllBytes(Path.of(args[2])));Map<String,MethodNode> expected=new LinkedHashMap<>();
        for(JsonElement item:proof.getAsJsonArray("selectedOriginalMethods")){
            String name=item.getAsString();MethodNode before=NativeManorDamageBridgeInstaller.method(original,name),mapped=new MethodNode(before.access,before.name,before.desc,before.signature,before.exceptions.toArray(String[]::new));before.accept(new MethodRemapper(mapped,mapper));expected.put(name,mapped);
            MethodNode after=NativeManorDamageBridgeInstaller.method(actual,name);require(graph(mapped).equals(graph(after)),"Exact entire mapped original sharing method "+name);new Analyzer<BasicValue>(new BasicVerifier()).analyze(actual.name,after);
            System.out.println("method="+name+";mappedOriginalAndActualGraph="+graph(after));
        }
        MethodNode wrapper=NativeManorDamageBridgeInstaller.method(actual,"ftboss$shareManorDamage");List<String> order=Arrays.stream(wrapper.instructions.toArray()).filter(node->node instanceof MethodInsnNode).map(node->((MethodInsnNode)node).name).toList();require(order.equals(List.of("sparrowShare","youziMinionShare")),"Original sequential Sparrow then Youzi sharing");
        MethodNode damaged=NativeManorDamageBridgeInstaller.method(actual,"sparrowShare");LdcInsnNode ratio=Arrays.stream(damaged.instructions.toArray()).filter(node->node instanceof LdcInsnNode constant&&Float.valueOf(0.5F).equals(constant.cst)).map(node->(LdcInsnNode)node).findFirst().orElseThrow();ratio.cst=0.4F;require(!graph(damaged).equals(graph(expected.get("sparrowShare"))),"Negative control: changing original share ratio is detected");
        System.out.println("failures=0;checks="+checks+";completeMappedOriginalMethods="+expected.size()+";actualDefinedAsmMethods="+expected.size()+";originalShareOrderVerified=true;ratioMutationNegativeControl=1;");
    }
}
