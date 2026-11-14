package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeAnimationEmitterInstaller {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String BOSS = ROOT + "entity/EasterBossEntity";
    private static final String SPELL = ROOT + "integ/spells/ManorSignatureSpell";
    private static final String VISUAL = ROOT + "skill/SpellVisuals";
    private static final String KIND = "L" + ROOT + "util/ManorSpellRules$Kind;";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String DATA = "Lio/redspace/ironsspellbooks/api/magic/MagicData;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String CAST = "(" + LEVEL + "I" + LIVING + DATA + ")V";
    private static final String RELEASE = "(" + LEVEL + "I" + LIVING + "Lio/redspace/ironsspellbooks/api/spells/CastSource;" + DATA + ")V";
    private static final String COMPLETE = "(" + LEVEL + "I" + LIVING + DATA + "Z)V";
    private static final String SEND = "(Lnet/minecraft/server/level/ServerLevel;L" + ROOT + "skill/SpellVisualPacket;)V";
    private static final String CLONES = "(Lnet/minecraft/server/level/ServerLevel;)V";
    private static final String PHASE = "(" + KIND + LIVING + DATA + ")V";
    private static final String IRON = ROOT + "dandaniron/IronHooks";
    private static final String LEMON = ROOT + "lemoncasting/CastingHooks";
    private static final String DORA = ROOT + "doramotion/DoraMotionSync";
    private static final Map<String,String> PREIMAGES = Map.of(
        BOSS + ":summonMiniClones" + CLONES, "f2c6876d1b247b7ecac2354bf0deff3c393ab7344504ff258e5ac4f8409a37e9",
        VISUAL + ":send" + SEND, "d4a6b229b705bc940a29200c74c4b228b7126916698929f94ee0f21c521083c7",
        SPELL + ":onServerPreCast" + CAST, "d7dd4dc6ae2f18ebc0969e1ea579650a3132dc78072c1f150e871b1b0e077348",
        SPELL + ":onServerCastTick" + CAST, "306ae379079d629a9b71a75e0f50702e1cfdae970c60ea1ea55fdb22cdce59fd",
        SPELL + ":onCast" + RELEASE, "813564f50be0d465af38f8ad7bed99e6f098ccb414861c72f3b6ba7fc1000f8c"
    );
    private NativeAnimationEmitterInstaller() { }
    public static void install(ClassNode target) {
        requireOwner(target);
        ClassNode candidate = copy(target);
        if (!nativePreimages(candidate)) {
            verifyInstalled(target);
            return;
        }
        patch(candidate);
        verifyInstalled(candidate);
        target.methods = candidate.methods;
    }
    public static void verifyInstalled(ClassNode target) {
        requireOwner(target);
        ClassNode baseline = copy(target);
        unpatch(baseline);
        require(nativePreimages(baseline), "Native emitter preimage drift: " + target.name);
        ClassNode expected = copy(baseline);
        patch(expected);
        require(NativeBossLivingComposerSupportV2.fingerprint(expected).equals(NativeBossLivingComposerSupportV2.fingerprint(target)), "Emitter placement or metadata drift: " + target.name);
    }
    private static void requireOwner(ClassNode owner) {
        require(owner.name.equals(BOSS) || owner.name.equals(SPELL) || owner.name.equals(VISUAL), "Unexpected animation emitter owner: " + owner.name);
    }
    private static boolean nativePreimages(ClassNode owner) {
        if (owner.name.equals(SPELL) && owner.methods.stream().anyMatch(method -> method.name.equals("onServerCastComplete"))) return false;
        for (Map.Entry<String,String> entry : PREIMAGES.entrySet()) {
            if (!entry.getKey().startsWith(owner.name + ":")) continue;
            String key = entry.getKey().substring(owner.name.length() + 1);
            List<MethodNode> matches = owner.methods.stream().filter(method -> (method.name + method.desc).equals(key)).toList();
            if (matches.size() != 1 || !entry.getValue().equals(NativeBossLivingComposerSupportV2.methodFingerprint(matches.getFirst()))) return false;
        }
        return true;
    }
    private static void patch(ClassNode owner) {
        if (owner.name.equals(BOSS)) {
            MethodNode method = method(owner,"summonMiniClones",CLONES);
            List<AbstractInsnNode> sites = code(method).stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKEVIRTUAL && call.owner.equals("net/minecraft/server/level/ServerLevel") && call.name.equals("addFreshEntity") && call.desc.equals("(Lnet/minecraft/world/entity/Entity;)Z")).toList();
            require(sites.size() == 1, "Native clone addFreshEntity count");
            AbstractInsnNode discarded = next(sites.getFirst());
            require(discarded.getOpcode() == Opcodes.POP, "Native clone boolean must be consumed exactly once");
            method.instructions.insertBefore(discarded,cloneHook());
            method.instructions.remove(discarded);
            method.maxStack += 2;
        } else if (owner.name.equals(VISUAL)) {
            MethodNode method = method(owner,"send",SEND);
            InsnList prefix = visualPrefix();
            method.instructions.insert(prefix);
            method.maxStack += 2;
        } else {
            MethodNode begin = method(owner,"onServerPreCast",CAST);
            begin.instructions.insert(facing(4));
            InsnList suffix = phase(LEMON,"begin",4,false);
            suffix.add(phase(DORA,"begin",4,false));
            suffix.add(iron("begin",true,false));
            begin.instructions.insertBefore(lastReturn(begin),suffix);
            MethodNode tick = method(owner,"onServerCastTick",CAST);
            InsnList prefix = facing(4); prefix.add(iron("tick",true,false));
            tick.instructions.insert(prefix);
            MethodNode release = method(owner,"onCast",RELEASE);
            prefix = facing(5); prefix.add(iron("release",false,false));
            release.instructions.insert(prefix);
            List<AbstractInsnNode> calls = code(release).stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL && call.owner.equals(owner.superName) && call.name.equals("onCast") && call.desc.equals(RELEASE)).toList();
            require(calls.size() == 1,"Native release superclass site count");
            AbstractInsnNode superCall = calls.getFirst();
            AbstractInsnNode arguments = superCall;
            for (int argument = 0; argument < 6; argument++) arguments = previous(arguments);
            require(arguments instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ALOAD && variable.var == 0,"Native release superclass arguments");
            release.instructions.insertBefore(arguments,phase(LEMON,"release",5,false));
            release.instructions.insert(superCall,phase(DORA,"release",5,false));
            begin.maxStack += 4; tick.maxStack += 4; release.maxStack += 4;
            owner.methods.add(complete(owner.superName));
        }
    }
    private static void unpatch(ClassNode owner) {
        if (owner.name.equals(BOSS)) {
            MethodNode method = method(owner,"summonMiniClones",CLONES);
            List<AbstractInsnNode> matched = find(method,cloneHook());
            method.instructions.insertBefore(matched.getFirst(),new InsnNode(Opcodes.POP));
            matched.forEach(method.instructions::remove);
        } else if (owner.name.equals(VISUAL)) {
            remove(method(owner,"send",SEND),visualPrefix());
        } else {
            MethodNode begin = method(owner,"onServerPreCast",CAST);
            remove(begin,facing(4)); remove(begin,phase(LEMON,"begin",4,false)); remove(begin,phase(DORA,"begin",4,false)); remove(begin,iron("begin",true,false));
            MethodNode tick = method(owner,"onServerCastTick",CAST);
            remove(tick,facing(4)); remove(tick,iron("tick",true,false));
            MethodNode release = method(owner,"onCast",RELEASE);
            remove(release,facing(5)); remove(release,iron("release",false,false)); remove(release,phase(LEMON,"release",5,false)); remove(release,phase(DORA,"release",5,false));
            MethodNode complete = method(owner,"onServerCastComplete",COMPLETE);
            require(NativeBossLivingComposerSupportV2.methodFingerprint(complete).equals("9b5ade3f667b7c7c4555b519589c034684e5a6d8946ec0cec7555d76d2f6b340"),"Complete original completion override drift");
            owner.methods.remove(complete);
        }
    }
    private static MethodNode complete(String parent) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC,"onServerCastComplete",COMPLETE,null,null);
        method.instructions.add(iron("complete",false,true));
        method.instructions.add(load(Opcodes.ALOAD,0)); method.instructions.add(load(Opcodes.ALOAD,1)); method.instructions.add(load(Opcodes.ILOAD,2)); method.instructions.add(load(Opcodes.ALOAD,3)); method.instructions.add(load(Opcodes.ALOAD,4)); method.instructions.add(load(Opcodes.ILOAD,5));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,parent,"onServerCastComplete",COMPLETE,false));
        method.instructions.add(phase(LEMON,"complete",4,true)); method.instructions.add(phase(DORA,"complete",4,true));
        method.instructions.add(new InsnNode(Opcodes.RETURN)); method.maxLocals = 6; method.maxStack = 6;
        return method;
    }
    private static InsnList cloneHook() {
        InsnList result = new InsnList(); result.add(load(Opcodes.ALOAD,0)); result.add(load(Opcodes.ALOAD,5)); result.add(call(ROOT + "youzi/motion/YouziBodyNetwork","summoned","(ZL" + BOSS + ";L" + BOSS + ";)V")); return result;
    }
    private static InsnList visualPrefix() {
        InsnList result = new InsnList();
        for (String owner : List.of(ROOT + "youzi/spells/YouziSpellNetwork",IRON)) { result.add(load(Opcodes.ALOAD,0)); result.add(load(Opcodes.ALOAD,1)); result.add(call(owner,owner.equals(IRON) ? "observe" : "emit",SEND)); }
        return result;
    }
    private static InsnList facing(int data) {
        InsnList result = new InsnList(); result.add(load(Opcodes.ALOAD,3)); result.add(load(Opcodes.ALOAD,0)); result.add(new FieldInsnNode(Opcodes.GETFIELD,SPELL,"kind",KIND)); result.add(load(Opcodes.ALOAD,data)); result.add(call(ROOT + "integ/spells/youzifacing/YouziCastFacing","prepare","(" + LIVING + KIND + DATA + ")V")); return result;
    }
    private static InsnList phase(String owner,String name,int data,boolean cancelled) {
        InsnList result = new InsnList(); result.add(load(Opcodes.ALOAD,0)); result.add(new FieldInsnNode(Opcodes.GETFIELD,SPELL,"kind",KIND)); result.add(load(Opcodes.ALOAD,3)); result.add(load(Opcodes.ALOAD,data)); if (cancelled) result.add(load(Opcodes.ILOAD,5)); result.add(call(owner,name,cancelled ? "(" + KIND + LIVING + DATA + "Z)V" : PHASE)); return result;
    }
    private static InsnList iron(String name,boolean level,boolean cancelled) {
        InsnList result = new InsnList(); result.add(load(Opcodes.ALOAD,0)); if (level) result.add(load(Opcodes.ILOAD,2)); result.add(load(Opcodes.ALOAD,3)); if (level) result.add(load(Opcodes.ALOAD,4)); if (cancelled) result.add(load(Opcodes.ILOAD,5)); result.add(call(IRON,name,"(L" + SPELL + ";" + (level ? "I" : "") + LIVING + (level ? DATA : "") + (cancelled ? "Z" : "") + ")V")); return result;
    }
    private static VarInsnNode load(int opcode,int local) { return new VarInsnNode(opcode,local); }
    private static MethodInsnNode call(String owner,String name,String descriptor) { return new MethodInsnNode(Opcodes.INVOKESTATIC,owner,name,descriptor,false); }
    private static MethodNode method(ClassNode owner,String name,String descriptor) {
        List<MethodNode> matches = owner.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).toList(); require(matches.size() == 1,"Missing or duplicate method: " + name); return matches.getFirst();
    }
    private static List<AbstractInsnNode> code(MethodNode method) { List<AbstractInsnNode> result = new ArrayList<>(); for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) result.add(instruction); return result; }
    private static List<AbstractInsnNode> find(MethodNode method,InsnList pattern) {
        List<AbstractInsnNode> actual = code(method); List<AbstractInsnNode> wanted = new ArrayList<>(); for (AbstractInsnNode instruction : pattern) wanted.add(instruction);
        List<List<AbstractInsnNode>> matches = new ArrayList<>();
        for (int start = 0; start + wanted.size() <= actual.size(); start++) { boolean equal = true; for (int offset = 0; offset < wanted.size(); offset++) equal &= same(actual.get(start + offset),wanted.get(offset)); if (equal) matches.add(new ArrayList<>(actual.subList(start,start + wanted.size()))); }
        require(matches.size() == 1,"Missing or duplicate exact emitter block: " + method.name); return matches.getFirst();
    }
    private static boolean same(AbstractInsnNode actual,AbstractInsnNode wanted) {
        if (actual.getOpcode() != wanted.getOpcode()) return false;
        if (wanted instanceof VarInsnNode variable) return actual instanceof VarInsnNode other && other.var == variable.var;
        if (wanted instanceof MethodInsnNode call) return actual instanceof MethodInsnNode other && other.owner.equals(call.owner) && other.name.equals(call.name) && other.desc.equals(call.desc) && other.itf == call.itf;
        if (wanted instanceof FieldInsnNode field) return actual instanceof FieldInsnNode other && other.owner.equals(field.owner) && other.name.equals(field.name) && other.desc.equals(field.desc);
        return wanted instanceof InsnNode;
    }
    private static void remove(MethodNode method,InsnList pattern) { find(method,pattern).forEach(method.instructions::remove); }
    private static AbstractInsnNode next(AbstractInsnNode instruction) { do { instruction = instruction.getNext(); } while (instruction != null && instruction.getOpcode() < 0); require(instruction != null,"Missing following instruction"); return instruction; }
    private static AbstractInsnNode previous(AbstractInsnNode instruction) { do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0); require(instruction != null,"Missing previous instruction"); return instruction; }
    private static AbstractInsnNode lastReturn(MethodNode method) { AbstractInsnNode instruction = method.instructions.getLast(); while (instruction.getOpcode() < 0) instruction = instruction.getPrevious(); require(instruction.getOpcode() == Opcodes.RETURN,"Missing final return"); return instruction; }
    private static ClassNode copy(ClassNode owner) { ClassWriter writer = new ClassWriter(0); owner.accept(writer); ClassNode result = new ClassNode(); new ClassReader(writer.toByteArray()).accept(result,0); return result; }
    private static void require(boolean condition,String message) { if (!condition) throw new IllegalStateException(message); }
}
