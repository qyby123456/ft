import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
public final class ManorOriginalRoutingDifferenceV1 {
    private static int checks;
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static ClassNode read(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_DEBUG);return node;}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    public static void main(String[] args)throws Exception {
        JsonObject proof=JsonParser.parseString(Files.readString(Path.of(args[1]))).getAsJsonObject(),members=proof.getAsJsonObject("memberNames");
        Remapper mapper=new Remapper(){public String mapMethodName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}public String mapFieldName(String owner,String name,String descriptor){return members.has(name)?members.get(name).getAsString():name;}};
        ClassNode original;
        try(ZipFile jar=new ZipFile(args[0])){original=read(jar.getInputStream(jar.getEntry("net/farmingtales/ftboss/entity/EasterBossEntity.class")).readAllBytes());}
        ClassNode actual=read(Files.readAllBytes(Path.of(args[2])));JsonObject differences=new JsonObject();
        for(JsonElement item:proof.getAsJsonArray("selectedOriginalMethods")){
            String name=item.getAsString(),actualName=members.has(name)?members.get(name).getAsString():name;MethodNode before=NativeManorDamageBridgeInstaller.method(original,name),mapped=new MethodNode(before.access,actualName,before.desc,before.signature,before.exceptions.toArray(String[]::new));before.accept(new MethodRemapper(mapped,mapper));
            MethodNode after=NativeManorDamageBridgeInstaller.method(actual,actualName);JsonObject row=new JsonObject();row.addProperty("mappedOriginalGraph",graph(mapped));row.addProperty("actualGraph",graph(after));row.addProperty("exact",graph(mapped).equals(graph(after)));
            if(!graph(mapped).equals(graph(after))){for(MethodNode method:List.of(mapped,after)){for(AbstractInsnNode node:method.instructions.toArray())if(node instanceof FrameNode)method.instructions.remove(node);Textifier text=new Textifier();method.accept(new TraceMethodVisitor(text));row.addProperty(method==mapped?"originalInstructions":"actualInstructions",text.getText().toString());}}
            differences.add(name,row);
        }
        Files.writeString(Path.of(args[3]),new GsonBuilder().setPrettyPrinting().create().toJson(differences));
    }
}
