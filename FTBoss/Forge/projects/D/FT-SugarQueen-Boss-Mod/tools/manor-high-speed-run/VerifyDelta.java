import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class VerifyDelta {
    private static byte[] bytes(ClassNode node) {
        var writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
    public static void main(String[] arguments) throws Exception {
        int checkedMethods = 0;
        int hooks = 0;
        try (ZipFile original = new ZipFile(arguments[0]); ZipFile delta = new ZipFile(arguments[1])) {
            for (String name : List.of("net/farmingtales/ftboss/client/model/EasterBossModel.class","net/farmingtales/ftboss/client/render/GongdiHumanoidRenderer.class","net/farmingtales/ftboss/client/manorrun/RunAnimator.class")) {
                if (original.getEntry(name) == null) continue;
                var before = new ClassNode();
                var after = new ClassNode();
                new ClassReader(original.getInputStream(original.getEntry(name)).readAllBytes()).accept(before,0);
                new ClassReader(delta.getInputStream(delta.getEntry(name)).readAllBytes()).accept(after,0);
                if (before.methods.size() != after.methods.size() || before.fields.size() != after.fields.size()) throw new AssertionError("Changed schema");
                for (int index = 0; index < before.methods.size(); index++) {
                    MethodNode oldMethod = before.methods.get(index), newMethod = after.methods.get(index);
                    new Analyzer<>(new BasicVerifier()).analyze(after.name,newMethod);
                    if (name.endsWith("RunAnimator.class") && (newMethod.name.equals("prepare") || newMethod.name.equals("apply"))) {
                        for (int count = 0; count < 5; count++) newMethod.instructions.remove(newMethod.instructions.getFirst());
                        AbstractInsnNode entryLabel = newMethod.instructions.getFirst();
                        AbstractInsnNode gateFrame = entryLabel.getNext();
                        while (gateFrame != null && gateFrame.getOpcode() < 0 && !(gateFrame instanceof org.objectweb.asm.tree.FrameNode)) gateFrame = gateFrame.getNext();
                        if (!(gateFrame instanceof org.objectweb.asm.tree.FrameNode)) throw new AssertionError("Missing gate frame");
                        newMethod.instructions.remove(gateFrame);
                        if (!(oldMethod.instructions.getFirst() instanceof org.objectweb.asm.tree.LabelNode)) newMethod.instructions.remove(entryLabel);
                    }
                    for (AbstractInsnNode instruction : newMethod.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && (call.owner.endsWith("/manorrun/ManorRunAnimation") || call.owner.endsWith("/manorrun/GongdiRunFrame"))) {
                            int preceding = name.contains("EasterBossModel") ? 3 : 2;
                            for (int count = 0; count < preceding; count++) newMethod.instructions.remove(instruction.getPrevious());
                            newMethod.instructions.remove(instruction);
                            hooks++;
                        }
                    }
                    newMethod.maxStack = oldMethod.maxStack;
                    checkedMethods++;
                }
                if (!Arrays.equals(bytes(before),bytes(after))) throw new AssertionError("Unrelated method or metadata changed in " + name);
            }
            var entries = delta.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    var node = new ClassNode();
                    new ClassReader(delta.getInputStream(entry).readAllBytes()).accept(node,0);
                    if (node.version != 61) throw new AssertionError("Not Java 17");
                    for (MethodNode method : node.methods) new Analyzer<>(new BasicVerifier()).analyze(node.name,method);
                }
            }
        }
        int streamed = 0;
        try (var zip = new ZipInputStream(Files.newInputStream(Path.of(arguments[1])))) {
            while (zip.getNextEntry() != null) { zip.readAllBytes(); streamed++; }
        }
        if (hooks != 2) throw new AssertionError("Wrong hook count");
        Files.writeString(Path.of(arguments[2],"method-merge-check.json"),"{\"offlineOnly\":true,\"normalizedOriginalRestoredExactly\":true,\"legacyManorGuards\":2,\"hooks\":" + hooks + ",\"methodsChecked\":" + checkedMethods + ",\"zipStreamEntries\":" + streamed + "}\n");
        System.out.println("All original methods and metadata restored exactly after removing two hooks and two legacy entry guards: PASS");
    }
}
