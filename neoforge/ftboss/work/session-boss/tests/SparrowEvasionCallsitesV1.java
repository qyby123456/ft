import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class SparrowEvasionCallsitesV1 {
    private static boolean relevant(String owner, String name) {
        return owner.contains("SparrowHitEvasion") || owner.endsWith("MeatBoss") || owner.endsWith("SummonGate");
    }

    private static String instruction(AbstractInsnNode instruction) {
        if (instruction instanceof MethodInsnNode call) return call.getOpcode()+" "+call.owner+"."+call.name+call.desc;
        if (instruction instanceof FieldInsnNode field) return field.getOpcode()+" "+field.owner+"."+field.name+" "+field.desc;
        if (instruction instanceof LdcInsnNode constant) return "LDC "+constant.cst;
        if (instruction instanceof InvokeDynamicInsnNode dynamic) return "INDY "+dynamic.name+dynamic.desc+" "+java.util.Arrays.toString(dynamic.bsmArgs);
        return instruction.getOpcode()>=0 ? Integer.toString(instruction.getOpcode()) : null;
    }

    private static void scanBoss(Path path, String label, List<Object> calls, List<Object> subjects) throws Exception {
        try (ZipFile bundle=new ZipFile(path.toFile())) {
            for (ZipEntry entry: java.util.Collections.list(bundle.entries())) {
                if (!entry.getName().startsWith("net/farmingtales/ftboss/") || !entry.getName().endsWith(".class")) continue;
                byte[] bytes=bundle.getInputStream(entry).readAllBytes();
                ClassNode type=new ClassNode();new ClassReader(bytes).accept(type,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
                boolean subject=type.name.contains("SparrowHitEvasion") || type.name.endsWith("MeatBoss") || type.name.endsWith("SummonGate") || type.name.endsWith("MeatMountainEntity");
                List<Object> bodies=new ArrayList<>();
                for (MethodNode method:type.methods) {
                    List<String> instructions=new ArrayList<>();boolean relevantCall=false;int index=0;
                    for (AbstractInsnNode instruction:method.instructions) {
                        String rendered=instruction(instruction);if (rendered!=null) instructions.add(rendered);
                        if (instruction instanceof MethodInsnNode call && relevant(call.owner,call.name)) {
                            relevantCall=true;calls.add(Map.of("label",label,"class",type.name,"method",method.name+method.desc,"instructionIndex",index,"kind","direct","target",call.owner+"."+call.name+call.desc));
                        }
                        if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                            for (Object argument:dynamic.bsmArgs) if (argument instanceof Handle handle && relevant(handle.getOwner(),handle.getName())) {
                                relevantCall=true;calls.add(Map.of("label",label,"class",type.name,"method",method.name+method.desc,"instructionIndex",index,"kind","bootstrap-handle","target",handle.getOwner()+"."+handle.getName()+handle.getDesc()));
                            }
                        }
                        index++;
                    }
                    if (subject || relevantCall) {
                        bodies.add(Map.of("method",method.name+method.desc,"access",method.access,"instructions",instructions));
                    }
                }
                if (subject || !bodies.isEmpty()) {
                    List<String> annotations=new ArrayList<>();
                    if (type.visibleAnnotations!=null) for (AnnotationNode annotation:type.visibleAnnotations) annotations.add(annotation.desc+" "+annotation.values);
                    subjects.add(Map.of("label",label,"class",type.name,"sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)),"annotations",annotations,"methods",bodies));
                }
            }
        }
    }

    private static void scanRuntime(Path path, List<Object> mixins, List<Object> unresolved) throws Exception {
        if (!Files.isRegularFile(path) || !path.toString().endsWith(".jar")) return;
        try (ZipFile bundle=new ZipFile(path.toFile())) {
            for (ZipEntry entry:java.util.Collections.list(bundle.entries())) {
                String name=entry.getName();
                if (!name.endsWith(".json") || !name.toLowerCase().contains("mixin") || name.toLowerCase().contains("refmap")) continue;
                JsonElement parsed;
                try { parsed=JsonParser.parseString(new String(bundle.getInputStream(entry).readAllBytes(),StandardCharsets.UTF_8)); }
                catch (RuntimeException failure) { unresolved.add(Map.of("jar",path.toString(),"config",name,"reason",failure.toString()));continue; }
                if (!parsed.isJsonObject()) continue;
                JsonObject configuration=parsed.getAsJsonObject();
                if (!configuration.has("package")) continue;
                String prefix=configuration.get("package").getAsString();
                for (String side:List.of("mixins","client","server")) {
                    if (!configuration.has(side) || !configuration.get(side).isJsonArray()) continue;
                    JsonArray classes=configuration.getAsJsonArray(side);
                    for (JsonElement shortName:classes) {
                        String fullName=prefix+"."+shortName.getAsString();ZipEntry classEntry=bundle.getEntry(fullName.replace('.','/')+".class");
                        if (classEntry==null) { unresolved.add(Map.of("jar",path.toString(),"config",name,"mixin",fullName,"reason","class absent from same archive;requires explicit external-resolution audit"));continue; }
                        ClassNode type=new ClassNode();new ClassReader(bundle.getInputStream(classEntry).readAllBytes()).accept(type,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
                        List<AnnotationNode> annotations=new ArrayList<>();if (type.visibleAnnotations!=null) annotations.addAll(type.visibleAnnotations);if (type.invisibleAnnotations!=null) annotations.addAll(type.invisibleAnnotations);
                        List<String> targets=new ArrayList<>();
                        for (AnnotationNode annotation:annotations) {
                            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;") || annotation.values==null) continue;
                            for (int index=0;index<annotation.values.size();index+=2) {
                                String key=annotation.values.get(index).toString();Object value=annotation.values.get(index+1);
                                if ((!key.equals("value") && !key.equals("targets")) || !(value instanceof List<?> values)) continue;
                                for (Object target:values) targets.add(target instanceof Type targetType?targetType.getClassName():target.toString());
                            }
                        }
                        mixins.add(Map.of("jar",path.toString(),"config",name,"side",side,"mixin",fullName,"targets",targets));
                    }
                }
            }
        }
    }

    public static void main(String[] arguments) throws Exception {
        List<Object> calls=new ArrayList<>();List<Object> subjects=new ArrayList<>();List<Object> mixins=new ArrayList<>();List<Object> unresolved=new ArrayList<>();
        scanBoss(Path.of(arguments[0]),"original",calls,subjects);scanBoss(Path.of(arguments[1]),"current",calls,subjects);
        JsonArray runtime=JsonParser.parseString(Files.readString(Path.of(arguments[2]))).getAsJsonArray();
        for (JsonElement entry:runtime) scanRuntime(Path.of(entry.getAsString()),mixins,unresolved);
        Map<String,Object> report=new LinkedHashMap<>();report.put("directAndBootstrapCallsites",calls);report.put("subjectMethods",subjects);report.put("declaredRuntimeArchiveMixinTargets",mixins);report.put("unresolvedRuntimeArchiveMixinDeclarations",unresolved);report.put("scope","Static primary class and declared archive configuration audit; plugins, conditionally selected configs, mod-folder resources and live transformation remain separate proof obligations.");
        Files.writeString(Path.of(arguments[3]),new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report));
        System.out.println("calls="+calls.size()+" subjects="+subjects.size()+" mixinDeclarations="+mixins.size()+" unresolved="+unresolved.size());
    }
}
