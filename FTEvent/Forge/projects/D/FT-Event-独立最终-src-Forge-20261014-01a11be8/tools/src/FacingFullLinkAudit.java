import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class FacingFullLinkAudit {
    private record Source(ZipFile zip,String entry,byte[] embedded) {
        byte[] bytes() throws Exception { return embedded!=null?embedded:zip.getInputStream(zip.getEntry(entry)).readAllBytes(); }
    }
    private record Member(String owner,String name,String descriptor,boolean method) { }
    private static final Map<String,Source> SOURCES=new LinkedHashMap<>();
    private static final Map<String,ClassNode> CLASSES=new HashMap<>();
    private static final Map<String,List<ClassNode>> MIXINS=new HashMap<>();
    private static final List<ZipFile> OPENED=new ArrayList<>();
    private static final Set<String> MISSING_CLASSES=new TreeSet<>();
    private static final Map<Member,Boolean> RESOLVED=new HashMap<>();
    private static final Map<String,Object> MISSING_MEMBERS=new TreeMap<>();
    private static long assertions,memberReferences,typeReferences;

    private static void index(Path path,boolean symbols) throws Exception {
        var zip=new ZipFile(path.toFile());OPENED.add(zip);
        for(var entries=zip.entries();entries.hasMoreElements();) {
            var entry=entries.nextElement();String name=entry.getName();
            if(symbols && name.endsWith(".sig")) {
                var tokens=name.split("/",3);
                if(tokens.length==3 && tokens[0].contains("H")) SOURCES.putIfAbsent(tokens[2].substring(0,tokens[2].length()-4),new Source(zip,name,null));
            } else if(!symbols && name.endsWith(".class") && !name.startsWith("META-INF/")) {
                SOURCES.putIfAbsent(name.substring(0,name.length()-6),new Source(zip,name,null));
            } else if(!symbols && name.startsWith("META-INF/jarjar/") && name.endsWith(".jar")) {
                try(var inner=new ZipInputStream(zip.getInputStream(entry))) {
                    for(var nested=inner.getNextEntry();nested!=null;nested=inner.getNextEntry()) {
                        if(nested.getName().endsWith(".class") && !nested.getName().startsWith("META-INF/")) SOURCES.putIfAbsent(nested.getName().substring(0,nested.getName().length()-6),new Source(null,null,inner.readAllBytes()));
                    }
                }
            }
        }
    }

    private static ClassNode load(String owner) {
        if(owner.startsWith("[")) return load("java/lang/Object");
        if(CLASSES.containsKey(owner)) return CLASSES.get(owner);
        try {
            byte[] bytes;
            Source source=SOURCES.get(owner);
            if(source!=null) bytes=source.bytes();
            else {
                try(var stream=ClassLoader.getPlatformClassLoader().getResourceAsStream(owner+".class")) {
                    if(stream==null) {CLASSES.put(owner,null);MISSING_CLASSES.add(owner);return null;}
                    bytes=stream.readAllBytes();
                }
            }
            var node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);CLASSES.put(owner,node);return node;
        } catch(Exception exception) {throw new IllegalStateException(owner,exception);}
    }

    private static boolean resolve(Member member,Set<String> visited) {
        if(!visited.add(member.owner)) return false;
        if(member.owner.startsWith("[") && member.name.equals("clone")) return true;
        if((member.owner.equals("java/lang/invoke/MethodHandle") && Set.of("invoke","invokeExact").contains(member.name)) || member.owner.equals("java/lang/invoke/VarHandle")) return true;
        ClassNode node=load(member.owner);if(node==null)return false;
        if(member.method) {
            for(var method:node.methods) if(method.name.equals(member.name) && method.desc.equals(member.descriptor)) return true;
        } else for(var field:node.fields) if(field.name.equals(member.name) && field.desc.equals(member.descriptor)) return true;
        for(var mixin:MIXINS.getOrDefault(member.owner,List.of())) {
            if(member.method) for(var method:mixin.methods) if(method.name.equals(member.name) && method.desc.equals(member.descriptor)) return true;
            if(!member.method) for(var field:mixin.fields) if(field.name.equals(member.name) && field.desc.equals(member.descriptor)) return true;
        }
        if(member.name.equals("<init>")) return false;
        if(node.superName!=null && resolve(new Member(node.superName,member.name,member.descriptor,member.method),visited)) return true;
        for(String implemented:node.interfaces) if(resolve(new Member(implemented,member.name,member.descriptor,member.method),visited)) return true;
        return false;
    }

    private static void member(String from,String owner,String name,String descriptor,boolean method) {
        memberReferences++;assertions++;
        var key=new Member(owner,name,descriptor,method);
        boolean exists=RESOLVED.computeIfAbsent(key,reference->resolve(reference,new HashSet<>()));
        if(!exists) MISSING_MEMBERS.putIfAbsent(owner+"."+name+descriptor,Map.of("owner",owner,"name",name,"descriptor",descriptor,"kind",method?"method":"field","firstCaller",from));
        descriptor(descriptor);
    }

    private static void type(Type value) {
        if(value.getSort()==Type.METHOD) {for(var argument:value.getArgumentTypes()) type(argument);type(value.getReturnType());}
        else if(value.getSort()==Type.ARRAY) type(value.getElementType());
        else if(value.getSort()==Type.OBJECT) {typeReferences++;assertions++;load(value.getInternalName());}
    }
    private static void descriptor(String value) {type(value.startsWith("(")?Type.getMethodType(value):Type.getType(value));}
    private static void handle(String from,Handle value) {member(from,value.getOwner(),value.getName(),value.getDesc(),value.getTag()>Opcodes.H_PUTSTATIC);}

    private static void registerMixin(ClassNode node) {
        var annotations=new ArrayList<AnnotationNode>();
        if(node.visibleAnnotations!=null)annotations.addAll(node.visibleAnnotations);if(node.invisibleAnnotations!=null)annotations.addAll(node.invisibleAnnotations);
        for(var annotation:annotations) if(annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;") && annotation.values!=null) {
            for(int index=0;index<annotation.values.size();index+=2) {
                if(!Set.of("value","targets").contains(annotation.values.get(index))) continue;
                Object values=annotation.values.get(index+1);
                if(values instanceof List<?> list) for(var value:list) {
                    String target=value instanceof Type reference?reference.getInternalName():value.toString().replace('.','/');
                    MIXINS.computeIfAbsent(target,ignored->new ArrayList<>()).add(node);
                }
            }
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path root=Path.of(arguments[0]);
        try {
            index(root.resolve("target.jar"),false);
            index(Path.of(System.getProperty("java.home"),"lib","ct.sym"),true);
            for(String path:Files.readAllLines(root.resolve("build/link-classpath.txt"))) if(!path.isBlank()) index(Path.of(path.strip()),false);
            for(String name:new ArrayList<>(SOURCES.keySet())) if((name.startsWith("net/farmingtales/ftboss/") || name.startsWith("cn/farmingtales/qysevent/")) && name.toLowerCase(Locale.ROOT).contains("mixin")) registerMixin(load(name));
            var errors=new ArrayList<Object>();var versions=new TreeMap<Integer,Integer>();
            int classes=0,methods=0;var newGuardReferences=new TreeSet<String>();
            try(var jar=new ZipFile(root.resolve("target.jar").toFile())) {
                for(var entries=jar.entries();entries.hasMoreElements();) {
                    var entry=entries.nextElement();if(!entry.getName().endsWith(".class"))continue;
                    var node=new ClassNode();new ClassReader(jar.getInputStream(entry).readAllBytes()).accept(node,0);classes++;versions.merge(node.version,1,Integer::sum);assertions++;
                    if(node.version>61) errors.add(Map.of("class",node.name,"error","Class version exceeds Java 17: "+node.version));
                    if(node.superName!=null){type(Type.getObjectType(node.superName));}for(String implemented:node.interfaces)type(Type.getObjectType(implemented));
                    for(var field:node.fields)descriptor(field.desc);
                    for(var method:node.methods) {
                        methods++;descriptor(method.desc);
                        if(method.exceptions!=null)for(String exception:method.exceptions)type(Type.getObjectType(exception));
                        if(method.instructions.size()>0) {
                            assertions++;
                            try {new Analyzer<>(new BasicVerifier()).analyze(node.name,method);}
                            catch(Exception failure){errors.add(Map.of("class",node.name,"method",method.name+method.desc,"error",failure.toString()));}
                        }
                        for(var instruction:method.instructions.toArray()) {
                            String caller=node.name+"."+method.name;
                            if(instruction instanceof MethodInsnNode call){member(caller,call.owner,call.name,call.desc,true);if(node.name.startsWith("net/farmingtales/ftboss/youzi/facing/") || node.name.startsWith("net/farmingtales/ftboss/integ/spells/youzifacing/"))newGuardReferences.add(call.owner);}
                            if(instruction instanceof FieldInsnNode field)member(caller,field.owner,field.name,field.desc,false);
                            if(instruction instanceof TypeInsnNode cast){type(cast.desc.startsWith("[")?Type.getType(cast.desc):Type.getObjectType(cast.desc));}
                            if(instruction instanceof LdcInsnNode constant){if(constant.cst instanceof Type reference)type(reference);if(constant.cst instanceof Handle reference)handle(caller,reference);}
                            if(instruction instanceof InvokeDynamicInsnNode dynamic){descriptor(dynamic.desc);handle(caller,dynamic.bsm);for(var argument:dynamic.bsmArgs){if(argument instanceof Type reference)type(reference);if(argument instanceof Handle reference)handle(caller,reference);}}
                            if(instruction instanceof MultiANewArrayInsnNode array)descriptor(array.desc);
                        }
                    }
                }
            }
            boolean guardClientFree=newGuardReferences.stream().noneMatch(name->name.startsWith("net/minecraft/client/") || name.startsWith("com/mojang/blaze3d/"));assertions++;
            var report=new LinkedHashMap<String,Object>();
            report.put("classes",classes);report.put("methods",methods);report.put("classVersions",versions);report.put("assertions",assertions);report.put("memberReferences",memberReferences);report.put("typeReferences",typeReferences);
            report.put("indexedClasspathClasses",SOURCES.size());report.put("mixinTargetOverlays",MIXINS.keySet());report.put("platformAPI","Java 17 ct.sym where available; internal platform classes fall back to current JDK metadata");
            report.put("bytecodeFailures",errors);report.put("unresolvedClasses",MISSING_CLASSES);report.put("unresolvedMembers",MISSING_MEMBERS);report.put("newFacingHelpersHaveNoClientOnlyTypeDependencies",guardClientFree);report.put("newFacingHelperReferencedOwners",newGuardReferences);
            report.put("passed",errors.isEmpty() && MISSING_CLASSES.isEmpty() && MISSING_MEMBERS.isEmpty() && guardClientFree);
            report.put("scope","Static class/member/SRG resolution, Java-17 public API metadata and bytecode stack analysis; not Forge class initialization or real dedicated-server startup");
            Files.writeString(root.resolve("verification/full-link-audit.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            System.out.println("Full linkage audit: "+classes+" classes, "+methods+" methods, "+assertions+" assertions; missing classes="+MISSING_CLASSES.size()+", members="+MISSING_MEMBERS.size()+", bytecode failures="+errors.size());
        } finally {for(var zip:OPENED)zip.close();}
    }
}
