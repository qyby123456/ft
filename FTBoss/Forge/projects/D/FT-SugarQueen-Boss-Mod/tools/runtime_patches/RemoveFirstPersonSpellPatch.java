import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class RemoveFirstPersonSpellPatch {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String RENDER = ROOT + "client/render/";
    private static final String HELPER = RENDER + "FirstPersonSpellVisibility";
    private final Map<String, ClassNode> changed = new LinkedHashMap<>();
    private final ZipFile archive;

    private RemoveFirstPersonSpellPatch(ZipFile archive) {
        this.archive = archive;
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[1]);
        try (ZipFile archive = new ZipFile(arguments[0])) {
            RemoveFirstPersonSpellPatch patch = new RemoveFirstPersonSpellPatch(archive);
            patch.apply();
            ClassNode helper = new ClassNode();
            new ClassReader(Files.readAllBytes(output.resolve(HELPER + ".class"))).accept(helper, 0);
            for (MethodNode method : helper.methods) {
                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode call) {
                        if (call.owner.equals("net/minecraft/client/Minecraft") && call.name.equals("getInstance")) call.name = "m_91087_";
                        if (call.owner.equals("net/minecraft/client/renderer/GameRenderer") && call.name.equals("getMainCamera")) call.name = "m_109153_";
                        if (call.owner.equals("net/minecraft/client/Camera") && call.name.equals("isDetached")) call.name = "m_90594_";
                    }
                    if (instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraft/client/Minecraft")
                            && field.name.equals("gameRenderer")) field.name = "f_91063_";
                }
            }
            patch.changed.put(HELPER, helper);
            for (ClassNode type : patch.changed.values()) {
                for (MethodNode method : type.methods) {
                    if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                        new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
                    }
                }
                ClassWriter writer = new ClassWriter(0);
                type.accept(writer);
                Path file = output.resolve(type.name + ".class");
                Files.createDirectories(file.getParent());
                Files.write(file, writer.toByteArray());
                System.out.println("PATCH " + type.name);
            }
        }
    }

    private void apply() throws Exception {
        guard(RENDER + "OptimizedSpellSafety", "cameraOpacity", "([[D[DZ)F", 2, true);
        guard(RENDER + "OptimizedSpellRenderer$Transform", "hullOpacity", null, -1, false);
        guard(RENDER + "dandan/DandanMotion", "cameraFade", "(D)D", -1, false);
        guard(RENDER + "dandan/DandanMotion", "blocksSight", "([D[D[D)Z", -1, false);
        guard(RENDER + "reapflat/FlatReapMotion", "visibleAlpha", "([D[D[DZ)D", 3, false);
        guard(RENDER + "XuanlanSpellMotion", "cameraFade", "(D)F", -1, false);
        guard(ROOT + "client/xuanlanarcane/ArcaneRhythm", "nearFade", "(D)F", -1, false);
        guard(ROOT + "client/xuanlanarcane/ArcaneRhythm", "viewWeight", "(Ljava/lang/String;ZZ)F", -1, false);
        for (String name : new String[]{RENDER + "ZheergenSpellOcclusion", RENDER + "ZheergenArcaneSafety", ROOT + "zheergenfx/client/LightSafety"}) {
            guard(name, "camera", "([[D[D)F", -1, false);
            guard(name, "sight", "([[D[D[D)F", -1, false);
            if (!name.endsWith("ZheergenSpellOcclusion")) guard(name, "sightVertex", "([D[D[D)F", -1, false);
        }
        guard(ROOT + "client/maque/MaqueMotion", "cameraVisibility", "([D[D[DZ)D", 3, false);
        guard(ROOT + "client/maque/MaqueMotion", "intersectsViewRay", "([[D[D[D)Z", -1, false);
        guard(ROOT + "client/maque/MaqueArcanePose", "sightFade", "([D[D[DZ)D", 3, false);
        guard(ROOT + "client/maque/MaqueArcanePose", "rayTouches", "([[D[D[D)Z", -1, false);
        guard(ROOT + "client/xueli/XueliGuard", "opacity", "([D[D[DZZ)D", 3, false);
        guard(ROOT + "client/saya/SayaMotion", "viewAlpha", "(DDDDDDZ)F", 12, false);
        guard(ROOT + "client/wineking/WineSpellMotion", "viewAlpha", "(DDZZ)D", 5, false);
        constantFalse(RENDER + "ManorSpellMergeRules", "hideBulkyInFirstPerson");
        constantFalse(ROOT + "util/SpellVisualRules", "hiddenForCamera");
        constantFalse(ROOT + "util/SpellVisualRules", "modelHiddenForCamera");
        factor(ROOT + "client/lemoncat/LemoncatArtGuard", "alpha", "clamp", 3, 0);
        factor(ROOT + "client/zicaiarcane/ArcaneRig", "visibility", "ease", 5, 1);
        MethodNode ritual = method(ROOT + "lemonritual/client/RitualMath", "guard", null);
        int distances = 0;
        for (AbstractInsnNode instruction : ritual.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals("java/lang/Math") && call.name.equals("sqrt")) {
                InsnList bridge = new InsnList();
                bridge.add(new VarInsnNode(Opcodes.ILOAD, 3));
                bridge.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "cameraDistance", "(DZ)D", false));
                ritual.instructions.insert(call, bridge);
                distances++;
            }
        }
        require(distances == 1, "Ritual camera distance changed");
        ritual.maxStack += 1;
        MethodNode renderer = method(RENDER + "OptimizedSpellRenderer", "render", null);
        int attached = 0;
        for (AbstractInsnNode instruction : renderer.instructions.toArray()) {
            if (!(instruction instanceof FieldInsnNode field) || !field.name.equals("attached")) continue;
            AbstractInsnNode condition = field;
            while (condition != null && !(condition instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ILOAD && variable.var == 6)) {
                condition = condition.getPrevious();
            }
            require(condition != null, "Attached spell view condition missing");
            renderer.instructions.set(condition, new InsnNode(Opcodes.ICONST_0));
            attached++;
        }
        require(attached == 1, "Attached spell condition changed");
        MethodNode defect = method(ROOT + "client/defectzero/DefectZeroRenderer", "render", null);
        int self = 0;
        for (AbstractInsnNode instruction : defect.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals("net/minecraft/client/Camera") || !call.name.equals("m_90593_")) continue;
            AbstractInsnNode store = call.getNext();
            while (store != null && store.getOpcode() != Opcodes.ISTORE) store = store.getNext();
            require(store != null, "Defect camera flag missing");
            defect.instructions.insertBefore(store, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "preserveThirdPerson", "(Z)Z", false));
            self++;
        }
        require(self == 1, "Defect camera flag changed");
    }

    private void guard(String owner, String name, String descriptor, int booleanSlot, boolean geometry) throws Exception {
        MethodNode method = method(owner, name, descriptor);
        LabelNode original = new LabelNode();
        InsnList prefix = new InsnList();
        if (booleanSlot >= 0) prefix.add(new VarInsnNode(Opcodes.ILOAD, booleanSlot));
        else prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "firstPerson", "()Z", false));
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        int result = Type.getReturnType(method.desc).getSort();
        if (geometry) {
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "geometryOpacity", "([[D)F", false));
        } else prefix.add(new InsnNode(result == Type.BOOLEAN ? Opcodes.ICONST_0 : result == Type.FLOAT ? Opcodes.FCONST_1 : Opcodes.DCONST_1));
        prefix.add(new InsnNode(result == Type.BOOLEAN ? Opcodes.IRETURN : result == Type.FLOAT ? Opcodes.FRETURN : Opcodes.DRETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
        method.maxStack = Math.max(method.maxStack, 2);
    }

    private void constantFalse(String owner, String name) throws Exception {
        MethodNode method = method(owner, name, null);
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) method.localVariables.clear();
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
        method.instructions.add(new InsnNode(Opcodes.ICONST_0));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        method.maxStack = 1;
    }

    private void factor(String owner, String name, String function, int booleanSlot, int occurrence) throws Exception {
        MethodNode method = method(owner, name, null);
        int count = 0, changed = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.name.equals(function) && call.desc.equals("(D)D")) {
                if (count++ == occurrence) {
                    InsnList bridge = new InsnList();
                    bridge.add(new VarInsnNode(Opcodes.ILOAD, booleanSlot));
                    bridge.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "cameraFactor", "(DZ)D", false));
                    method.instructions.insert(call, bridge);
                    changed++;
                }
            }
            if (instruction instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ILOAD && variable.var == booleanSlot) {
                method.instructions.set(variable, new InsnNode(Opcodes.ICONST_0));
            }
        }
        require(changed == 1, owner + " camera factor changed");
        method.maxStack += 1;
    }

    private MethodNode method(String owner, String name, String descriptor) throws Exception {
        ClassNode type = changed.get(owner);
        if (type == null) {
            var entry = archive.getEntry(owner + ".class");
            require(entry != null, "Missing runtime class: " + owner);
            type = new ClassNode();
            try (var stream = archive.getInputStream(entry)) { new ClassReader(stream.readAllBytes()).accept(type, 0); }
            changed.put(owner, type);
        }
        var matches = type.methods.stream().filter(value -> value.name.equals(name) && (descriptor == null || value.desc.equals(descriptor))).toList();
        require(matches.size() == 1, "Ambiguous runtime method: " + owner + "." + name);
        return matches.get(0);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
