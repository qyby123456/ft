package net.farmingtales.ftboss.item;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class MaterialProcOriginalBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/item/";
    private static final Map<String,String> hashes=new LinkedHashMap<>();
    private static int cases;
    private static int preserved;
    private static int changed;
    private static int added;

    private static void require(boolean accepted,String name) {
        if(!accepted) throw new AssertionError(name);
    }

    private static String sha(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static ClassNode parse(byte[] bytes) {
        var node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;
    }

    private static String body(MethodNode source) {
        var copy=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(copy);
        Set<LabelNode> used=Collections.newSetFromMap(new IdentityHashMap<>());
        for(var instruction:copy.instructions) {
            if(instruction instanceof JumpInsnNode branch) used.add(branch.label);
            if(instruction instanceof LookupSwitchInsnNode selection) {used.add(selection.dflt);used.addAll(selection.labels);}
            if(instruction instanceof TableSwitchInsnNode selection) {used.add(selection.dflt);used.addAll(selection.labels);}
        }
        for(var block:copy.tryCatchBlocks) {used.add(block.start);used.add(block.end);used.add(block.handler);}
        for(var instruction:copy.instructions.toArray()) if(instruction instanceof FrameNode || instruction instanceof LineNumberNode || instruction instanceof LabelNode label && !used.contains(label)) copy.instructions.remove(instruction);
        var printer=new Textifier();copy.instructions.accept(new TraceMethodVisitor(printer));String text=printer.getText().toString();
        for(var entry:hashes.entrySet()) text=text.replace(entry.getKey(),entry.getValue());return text.replaceAll("lambda\\$([^\\s$]+)\\$[0-9]+", "lambda\\$$1\\$INDEX");
    }

    private static String normalized(String name) {
        return name.replaceAll("lambda\\$([^$]+)\\$[0-9]+", "lambda\\$$1\\$INDEX");
    }

    private static boolean intended(String name, String method) {
        return name.equals("FoodWeaponExpectedTrace$Kind") && Set.of("<clinit>","$values").contains(method)
            || name.equals("FoodWeaponHitPlan") && normalized(method).equals("lambda$ownerReserveLeafSelected$INDEX")
            || name.equals("FoodWeaponHitPlan$MaterialPowersLeaf") && Set.of("<init>","apply").contains(method)
            || name.equals("OwnerFieldExpectedAuthority") && method.equals("quoteTimers")
            || name.equals("OwnerTimerSemanticAuthority") && Set.of("savedLeaf","enter").contains(method)
            || name.equals("OwnerTimerSemanticAuthority$Book") && Set.of("<init>","quote","resolve","writerStep","returned","complete").contains(method)
            || name.equals("OwnerFullRepairGuard") && method.equals("changed")
            || name.equals("WeaponMaterialPowers") && Set.of("proc","closedProc").contains(method);
    }

    private static boolean added(String name, String method) {
        return name.equals("FoodWeaponFinalDamage") && (method.equals("acceptsOwnerReserveMaterialLeaf") || method.startsWith("lambda$acceptsOwnerReserveMaterialLeaf$"))
            || name.equals("FoodWeaponHitPlan") && Set.of("materialPowersLeaf","materialProcProgram").contains(method)
            || name.equals("FoodWeaponSourceLedger") && Set.of("ownerMaterialLeafAccepted","ownerMaterialLeafEntered").contains(method)
            || name.equals("OwnerTimerSemanticAuthority$Book") && method.equals("quoteMaterial")
            || name.equals("FoodWeaponExpectedTrace$Builder") && method.equals("materialProcWrite")
            || name.equals("FoodWeaponOriginalSequence$Session") && Set.of("materialProcCursor","materialProcReturned").contains(method)
            || name.equals("NativeExpectedRawFields") && Set.of("materialPersistent","materialChild").contains(method)
            || name.equals("WeaponMaterialPowers") && method.equals("quoteClosedMaterialFood");
    }

    public static void main(String[] arguments) throws Exception {
        Path before=Path.of(arguments[0]),after=Path.of(arguments[1]);
        List<Path> files;try(var paths=Files.walk(before)) {files=paths.filter(path->path.toString().endsWith(".class")).toList();}
        List<Path> changes=new ArrayList<>();
        for(Path file:files) {
            Path relative=before.relativize(file);byte[] previous=Files.readAllBytes(file),current=Files.readAllBytes(after.resolve(relative));
            if(Arrays.equals(previous,current))continue;
            changes.add(relative);String name=relative.toString().replace('\\','/');
            hashes.put(sha(previous),"<PIN:"+name+">");hashes.put(sha(current),"<PIN:"+name+">");
        }
        for(Path relative:changes) {
            ClassNode previous=parse(Files.readAllBytes(before.resolve(relative))),current=parse(Files.readAllBytes(after.resolve(relative)));
            String name=previous.name.substring(PREFIX.length());
            for(FieldNode field:previous.fields) require(current.fields.stream().anyMatch(value->value.name.equals(field.name)
                && value.desc.equals(field.desc) && value.access==field.access),"Existing field contract changed: "+name+"."+field.name);
            for(MethodNode method:previous.methods) {
                List<MethodNode> candidates=current.methods.stream().filter(value->normalized(value.name).equals(normalized(method.name)) && value.desc.equals(method.desc)).toList();
                require(!candidates.isEmpty(),"Missing original method: "+name+"."+method.name);
                MethodNode match=null;
                for(MethodNode candidate:candidates) if(body(method).equals(body(candidate))) {match=candidate;break;}
                if(match==null) {
                    require(intended(name,method.name),"Unexpected original method change: "+name+"."+method.name+method.desc);
                    match=candidates.getFirst();changed++;
                } else preserved++;
                require(method.access==match.access && Objects.equals(method.signature,match.signature) && method.exceptions.equals(match.exceptions),"Original method contract changed");
                new Analyzer<>(new BasicVerifier()).analyze(current.name,match);
            }
            for(MethodNode method:current.methods) if(previous.methods.stream().noneMatch(value->normalized(value.name).equals(normalized(method.name)) && value.desc.equals(method.desc))) {
                require(added(name,method.name),"Unexpected added original-class method: "+name+"."+method.name);added++;
            }
        }
        require(preserved>500 && changed>0 && added>0,"Boundary proof coverage unexpectedly empty");
        var kind=Class.forName("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace$Kind");
        Object[] constants=kind.getEnumConstants();String[] expected={"TIMER","RANGED_TIMER","RANGED_ROOT","NATIVE_PUT","NATIVE_CAPTURE","RANGED_ACQUIRE","MATERIAL_PROC_WRITE"};
        require(constants.length==expected.length,"Material operation kind count");
        for(int index=0;index<expected.length;index++)require(((Enum<?>)constants[index]).name().equals(expected[index]),"Original operation ordinal changed");
        System.out.println("{\"failures\":0,\"changedRawClassesCompared\":"+changes.size()+",\"originalMethodsPreserved\":"+preserved+",\"declaredOriginalMethodsChanged\":"+changed+",\"declaredMethodsAdded\":"+added+",\"originalKindOrdinalsPreserved\":true}");
    }
}
