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

public final class AliasedEntityExpectedImageProof {
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
        for(var entry:hashes.entrySet()) text=text.replace(entry.getKey(),entry.getValue());return text;
    }

    private static boolean intended(String name,String method) {
        return name.equals("FoodWeaponExpectedTrace$Builder") && Set.of("rangedAcquire","rangedAcquireDetached431","rangedRoot","append").contains(method)
            || name.equals("FoodWeaponExpectedTrace$State") && Set.of("<init>","replace","replacePlayer").contains(method)
            || name.equals("OwnerFieldExpectedAuthority$Prepared") && method.equals("<init>");
    }

    private static void classBoundary(Path before,Path after) throws Exception {
        var allowed=Set.of("FoodWeaponExpectedTrace$Builder","FoodWeaponExpectedTrace$Domain","FoodWeaponExpectedTrace$Kind","FoodWeaponExpectedTrace$State","FoodWeaponExpectedTrace","NativeCapabilityInstallationProof170","OwnerFieldExpectedAuthority$Prepared");
        List<Path> files;try(var paths=Files.walk(before)) {files=paths.filter(path->path.toString().endsWith(".class")).toList();}
        long afterCount;try(var paths=Files.walk(after)) {afterCount=paths.filter(path->path.toString().endsWith(".class")).count();}
        require(files.size()==1381 && afterCount==1381,"Complete unchanged class graph size");
        var changedFiles=new ArrayList<Path>();
        for(var path:files) {
            var relative=before.relativize(path);var prior=Files.readAllBytes(path);var next=Files.readAllBytes(after.resolve(relative));
            if(Arrays.equals(prior,next)) continue;
            String name=relative.toString().replace('\\','/');require(name.startsWith(PREFIX),"Changed external class");name=name.substring(PREFIX.length(),name.length()-6);
            require(allowed.contains(name),"Unexpected changed class: "+name);changedFiles.add(relative);
            hashes.put(sha(prior),"<PIN:"+name+">");hashes.put(sha(next),"<PIN:"+name+">");
        }
        require(changedFiles.size()==7,"Expected exactly seven raw changes");
        for(var relative:changedFiles) {
            var prior=parse(Files.readAllBytes(before.resolve(relative)));var next=parse(Files.readAllBytes(after.resolve(relative)));String name=prior.name.substring(PREFIX.length());
            var nextFields=next.fields.stream().filter(field->!name.equals("FoodWeaponExpectedTrace$State") || !field.name.equals("playerCarrierAliased")).map(field->field.name+field.desc+field.access).toList();
            require(prior.fields.stream().map(field->field.name+field.desc+field.access).toList().equals(nextFields),"Existing fields changed");
            if(name.equals("FoodWeaponExpectedTrace$State")) require(next.fields.stream().anyMatch(field->field.name.equals("playerCarrierAliased") && field.desc.equals("Z") && (field.access&Opcodes.ACC_FINAL)!=0),"Alias fact must be immutable");
            for(var method:prior.methods) {
                var current=next.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElseThrow();
                require(method.access==current.access && Objects.equals(method.signature,current.signature) && method.exceptions.equals(current.exceptions),"Method contract changed");
                new Analyzer<>(new BasicVerifier()).analyze(next.name,current);
                if(body(method).equals(body(current))) preserved++;
                else {require(intended(name,current.name),"Changed guard or unrelated method: "+name+"."+current.name+current.desc);changed++;}
            }
            for(var method:next.methods) if(prior.methods.stream().noneMatch(value->value.name.equals(method.name) && value.desc.equals(method.desc))) {
                require(name.equals("FoodWeaponExpectedTrace$State") && (method.name.equals("<init>") || method.name.equals("replaceRangedBuffer")),"Unexpected helper method");
                new Analyzer<>(new BasicVerifier()).analyze(next.name,method);added++;
            }
        }
        require(changed==8 && added==3,"Expected eight implementation updates and three State helpers");
    }

    private static void same(FoodWeaponExpectedTrace.Value expected,FoodWeaponExpectedTrace.Value actual,String reason) {
        require(FoodWeaponExpectedTrace.same(expected,actual),reason);cases++;
    }

    private static void pureExpectedStates() {
        var empty=FoodWeaponExpectedTrace.emptyCompound();var one=FoodWeaponExpectedTrace.number(4,1);var nine=FoodWeaponExpectedTrace.number(4,9);
        var buffer=FoodWeaponExpectedTrace.put(empty,"LastAlpha",one);var root=FoodWeaponExpectedTrace.put(empty,"FTBossRangedProcTimers",buffer);
        var defaultState=new FoodWeaponExpectedTrace.State(empty,root,root);
        require(!defaultState.playerCarrierAliased,"Equal or identical values must not infer entity identity");cases++;
        var independent=new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(empty,empty,empty,false));
        independent.rangedAcquireDetached431(new Object(),new Object());independent.rangedLong("LastAlpha",9);var separateRoot=independent.rangedRoot().after;
        same(empty,separateRoot.carrier,"Separate carrier must not receive a player write");
        require(!FoodWeaponExpectedTrace.same(separateRoot.player,separateRoot.carrier),"Separate roots remain separate");cases++;
        var detached=new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(empty,empty,empty,true));
        detached.rangedAcquireDetached431(new Object(),new Object());var detachedLong=detached.rangedLong("LastAlpha",9).after;
        same(empty,detachedLong.player,"Detached buffer does not yet mutate player");same(empty,detachedLong.carrier,"Detached buffer does not yet mutate aliased carrier");
        var attached=detached.rangedRoot().after;require(attached.playerCarrierAliased && attached.player==attached.carrier,"Root commit uses one shared immutable image");cases++;
        same(nine,FoodWeaponExpectedTrace.field(FoodWeaponExpectedTrace.field(attached.carrier,"FTBossRangedProcTimers"),"LastAlpha"),"Aliased carrier receives planned root contents");
        var live=new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(empty,root,root,true));
        live.rangedAcquire(new Object(),new Object());var liveTimer=live.rangedLong("LastAlpha",9).after;
        require(liveTimer.playerCarrierAliased && liveTimer.player==liveTimer.carrier,"Attached timer preserves alias identity");cases++;
        same(nine,FoodWeaponExpectedTrace.field(FoodWeaponExpectedTrace.field(liveTimer.player,"FTBossRangedProcTimers"),"LastAlpha"),"Attached timer updates expected player");
        var carrierWrite=new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(empty,root,root,true)).nativePut(FoodWeaponExpectedTrace.Domain.CARRIER,new String[]{"FTBossRangedProcTimers"},"LastAlpha",nine).after;
        require(carrierWrite.playerCarrierAliased && carrierWrite.player==carrierWrite.carrier,"Carrier write uses shared image");cases++;
        same(nine,FoodWeaponExpectedTrace.field(carrierWrite.rangedBuffer,"LastAlpha"),"Aliased nested carrier write updates attached buffer");
        var playerWrite=new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(empty,root,root,true)).nativePut(FoodWeaponExpectedTrace.Domain.PLAYER,new String[0],"Other",one).after;
        require(playerWrite.player==playerWrite.carrier && playerWrite.rangedAttached,"Unrelated shared player write retains attachment");cases++;
        same(buffer,playerWrite.rangedBuffer,"Unrelated write preserves buffer");
        var replacedRoot=new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(empty,root,root,true)).nativePut(FoodWeaponExpectedTrace.Domain.CARRIER,new String[0],"FTBossRangedProcTimers",empty).after;
        require(replacedRoot.player==replacedRoot.carrier && !replacedRoot.rangedAttached,"Root replacement detaches the previous exact buffer");cases++;
        var separateCarrier=new FoodWeaponExpectedTrace.Builder(defaultState).nativePut(FoodWeaponExpectedTrace.Domain.CARRIER,new String[0],"Other",one).after;
        same(root,separateCarrier.player,"Distinct carrier write leaves player unchanged");
        var weaponWrite=new FoodWeaponExpectedTrace.Builder(new FoodWeaponExpectedTrace.State(empty,root,root,true)).nativePut(FoodWeaponExpectedTrace.Domain.WEAPON,new String[0],"Other",one).after;
        require(weaponWrite.playerCarrierAliased && weaponWrite.player==weaponWrite.carrier,"Weapon write retains proven alias fact");cases++;
        boolean denied=false;try {new FoodWeaponExpectedTrace.State(empty,root,empty,true);}catch(IllegalArgumentException expected) {denied=true;}
        require(denied,"Incoherent aliased before-images rejected");cases++;
        same(buffer,FoodWeaponExpectedTrace.field(root,"FTBossRangedProcTimers"),"Original immutable image unchanged");
    }

    public static void main(String[] arguments) throws Exception {
        classBoundary(Path.of(arguments[0]),Path.of(arguments[1]));pureExpectedStates();
        System.out.println("failures=0;existingMethodsPreserved="+preserved+";expectedImageMethodsUpdated="+changed+";stateHelpersAdded="+added+";pureExpectedAliasCases="+cases+";rawChangedClasses=7;allOtherClassesIdentical=true;observationAndComparisonGatesUnchanged=true;authorityWrites=false");
    }
}
