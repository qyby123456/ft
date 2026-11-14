import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
public final class ManorOriginalSharingMappedAuditV2 {
    private static int checks;
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static ClassNode read(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_DEBUG);return node;}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    private static MethodNode copy(MethodNode source){MethodNode target=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(target);return target;}
    private static int normalizeEquivalentShareStack(MethodNode method){
        int sites=0;
        for(AbstractInsnNode node:method.instructions.toArray())if(node instanceof MethodInsnNode call&&call.owner.equals("net/farmingtales/ftboss/youzi/YouziCombatEvents")&&call.name.equals("sharedDamage")){
            AbstractInsnNode recipient=node.getPrevious(),owner=recipient.getPrevious(),amount=owner.getPrevious(),receiver=amount.getPrevious();
            require(recipient instanceof VarInsnNode load&&load.getOpcode()==Opcodes.ALOAD&&load.var==8,"Exact existing recipient reload");
            require(owner instanceof VarInsnNode load&&load.getOpcode()==Opcodes.ALOAD&&load.var==0,"Exact unchanged owner load");
            require(receiver instanceof VarInsnNode load&&load.getOpcode()==Opcodes.ALOAD&&load.var==8,"Recipient on original receiver stack is same local 8");
            require(amount.getOpcode()==Opcodes.FCONST_1||amount.getOpcode()==Opcodes.FCONST_0||(amount instanceof VarInsnNode load&&load.getOpcode()==Opcodes.FLOAD&&load.var==9),"Exact original category-one health amount");
            require(call.desc.equals("(Lnet/farmingtales/ftboss/entity/EasterBossEntity;Lnet/farmingtales/ftboss/entity/EasterBossEntity;)V")&&call.getNext() instanceof MethodInsnNode health&&health.name.equals("setHealth")&&health.desc.equals("(F)V"),"Exact original shared marker and following health write");
            InsnList originalStack=new InsnList();originalStack.add(new InsnNode(Opcodes.DUP2));originalStack.add(new InsnNode(Opcodes.POP));originalStack.add(new VarInsnNode(Opcodes.ALOAD,0));originalStack.add(new InsnNode(Opcodes.SWAP));method.instructions.insertBefore(owner,originalStack);method.instructions.remove(owner);method.instructions.remove(recipient);sites++;
        }
        return sites;
    }
    private static List<String> stack(boolean original){
        List<String> values=new ArrayList<>(List.of("recipient8","floatHealth"));
        if(original){values.addAll(List.copyOf(values));values.removeLast();values.add("owner0");Collections.swap(values,values.size()-1,values.size()-2);}
        else{values.add("owner0");values.add("recipient8");}return values;
    }
    public static void main(String[] args)throws Exception {
        JsonObject proof=JsonParser.parseString(Files.readString(Path.of(args[1]))).getAsJsonObject(),members=proof.getAsJsonObject("memberNames");
        Remapper mapper=new Remapper(){public String mapMethodName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}public String mapFieldName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}};
        ClassNode original;
        try(ZipFile jar=new ZipFile(args[0])){original=read(jar.getInputStream(jar.getEntry("net/farmingtales/ftboss/entity/EasterBossEntity.class")).readAllBytes());}
        ClassNode actual=read(Files.readAllBytes(Path.of(args[2])));Map<String,MethodNode> expected=new LinkedHashMap<>();require(stack(true).equals(stack(false)),"Symbolic category-one stack proof preserves receiver/value/owner/recipient");int normalizedSites=0;
        for(JsonElement item:proof.getAsJsonArray("selectedOriginalMethods")){
            String name=item.getAsString();MethodNode before=NativeManorDamageBridgeInstaller.method(original,name),mapped=new MethodNode(before.access,before.name,before.desc,before.signature,before.exceptions.toArray(String[]::new));before.accept(new MethodRemapper(mapped,mapper));expected.put(name,mapped);
            MethodNode after=NativeManorDamageBridgeInstaller.method(actual,name),normalized=copy(after);int sites=normalizeEquivalentShareStack(normalized);require(sites==(name.equals("youziMinionShare")?3:0),"Only three already-present exact equivalent stack forms");normalizedSites+=sites;require(graph(mapped).equals(graph(normalized)),"Entire mapped original graph after proven three stack-equivalent rewrites "+name);new Analyzer<BasicValue>(new BasicVerifier()).analyze(actual.name,after);
            System.out.println("method="+name+";mappedOriginalAndActualGraph="+graph(after));
        }
        MethodNode invalid=copy(NativeManorDamageBridgeInstaller.method(actual,"youziMinionShare"));for(AbstractInsnNode node:invalid.instructions)if(node instanceof MethodInsnNode call&&call.name.equals("sharedDamage")){((VarInsnNode)node.getPrevious()).var=7;break;}boolean rejected=false;try{normalizeEquivalentShareStack(invalid);}catch(AssertionError expectedFailure){rejected=true;}require(rejected,"Negative control rejects foreign recipient local instead of normalizing it");
        MethodNode wrapper=NativeManorDamageBridgeInstaller.method(actual,"ftboss$shareManorDamage");List<String> order=Arrays.stream(wrapper.instructions.toArray()).filter(node->node instanceof MethodInsnNode).map(node->((MethodInsnNode)node).name).toList();require(order.equals(List.of("sparrowShare","youziMinionShare")),"Original sequential Sparrow then Youzi sharing");
        MethodNode damaged=NativeManorDamageBridgeInstaller.method(actual,"sparrowShare");LdcInsnNode ratio=Arrays.stream(damaged.instructions.toArray()).filter(node->node instanceof LdcInsnNode constant&&Float.valueOf(0.5F).equals(constant.cst)).map(node->(LdcInsnNode)node).findFirst().orElseThrow();ratio.cst=0.4F;require(!graph(damaged).equals(graph(expected.get("sparrowShare"))),"Negative control: changing original share ratio is detected");
        System.out.println("failures=0;checks="+checks+";completeMappedOriginalMethods="+expected.size()+";actualDefinedAsmMethods="+expected.size()+";originalShareOrderVerified=true;provenExistingEquivalentStackSites="+normalizedSites+";negativeControls=2;");
    }
}
