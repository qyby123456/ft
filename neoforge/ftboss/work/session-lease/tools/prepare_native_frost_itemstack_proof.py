import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
arguments = json.loads((work / 'build/native-repair-667b6466/complete-arguments316.json').read_text(encoding='utf-8'))
source = Path(next(value for value in arguments if Path(value).name == 'NativeItemStackInstallationProof165.java'))
target = work / 'proposal-native/net/farmingtales/ftboss/item/NativeItemStackInstallationProof165.java'
assert not target.exists()
text = source.read_text(encoding='utf-8')
anchor = '        if (kind != null) wear(actual, parent, kind);\n        return parent;'
assert text.count(anchor) == 1
text = text.replace(anchor, '        if (kind != null) wear(actual, parent, kind);\n        if (parent.name.equals("use")) frostUseObservers(actual, parent);\n        return parent;')
methods = '''    private void frostUseObservers(ClassNode actual, MethodNode parent) throws Exception {
        String family = "NativeReadCutItemStackMixin";
        ClassNode source = authentic(family);
        for (String suffix : List.of("ftboss$nativeFrostUseEntered", "ftboss$nativeFrostUseReturned")) {
            MethodNode before = source.methods.stream().filter(method -> method.name.equals(suffix)).reduce((first, second) -> { throw new IllegalStateException("Duplicate Frost use source handler"); }).orElseThrow();
            AnnotationNode inject = annotation(before, "Inject");
            boolean head = suffix.endsWith("Entered");
            require(List.of(parent.name + parent.desc).equals(value(inject, "method")), "Exact original Frost use observer selector");
            List<?> points = (List<?>)value(inject, "at");
            require(points.size() == 1 && points.getFirst() instanceof AnnotationNode && Objects.equals(value((AnnotationNode)points.getFirst(), "value"), head ? "HEAD" : "RETURN")
                && Objects.equals(value(inject, "require"), 1) && (!head || Objects.equals(value(inject, "order"), 900)), "Exact original Frost use observer injection contract");
            List<MethodNode> found = actual.methods.stream().filter(method -> method.name.endsWith("$" + suffix) && method.desc.equals(before.desc)).filter(method -> {
                try { return Objects.equals(value(annotation(method, "MixinMerged"), "mixin"), source.name.replace('/', '.')); }
                catch (RuntimeException failure) { return false; }
            }).toList();
            require(found.size() == 1, "Unique actual Frost use observer handler");
            MethodNode callback = found.getFirst();
            AnnotationNode merged = annotation(callback, "MixinMerged");
            require(merged.values.size() == 6 && Objects.equals(value(merged, "priority"), 1) && Objects.equals(value(merged, "sessionId"), SESSION)
                && callback.access == before.access && callback.maxLocals == before.maxLocals && Objects.equals(callback.signature, before.signature)
                && callback.exceptions.equals(before.exceptions) && OwnerFieldInstallationProof.nativeComponentLambdaBody(before, callback, source, actual), "Whole authentic Frost use callback and actual installation provenance");
            new Analyzer<>(new BasicVerifier()).analyze(actual.name, NativeBossLivingComposerSupportV2.copy(callback));
            long expectedCalls = head ? 1 : code(parent).stream().filter(instruction -> instruction.getOpcode() == ARETURN).count();
            long actualCalls = actual.methods.stream().flatMap(method -> code(method).stream()).filter(instruction -> instruction instanceof MethodInsnNode call
                && call.owner.equals(actual.name) && call.name.equals(callback.name) && call.desc.equals(callback.desc)).count();
            require(actualCalls == expectedCalls, "No missing, duplicated or foreign Frost use observer invocation");
            if (head) parent.instructions.insert(invokeHead(actual, callback, true, parent));
            else beforeReturns(parent, () -> invokeHead(actual, callback, true, parent));
        }
    }

'''
anchor = '    private void nativeUseRedirect('
assert text.count(anchor) == 1
text = text.replace(anchor, methods + anchor)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
prefix = helper.read_text(encoding='utf-8').split('candidate = work /')[0]
namespace = {'__file__': str(helper)}
exec(compile(prefix, str(helper), 'exec'), namespace)
namespace['add']('work/session-lease/proposal-native/net/farmingtales/ftboss/item/NativeItemStackInstallationProof165.java', text)
print(json.dumps({'wholeItemStackProofReplaysExactFrostObservers': True, 'existingProofChecksPreserved': True}))
