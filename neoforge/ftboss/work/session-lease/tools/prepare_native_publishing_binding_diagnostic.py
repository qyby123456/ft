import hashlib
import json
import sys
from pathlib import Path
WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
tag = sys.argv[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
parent_path = WORK / ('reports/native-passive-root-completion-diagnostic2-preparation-' + tag + '.json')
parent = json.loads(parent_path.read_text(encoding='utf-8'))
for name,wanted in parent['inputHashes'].items():
    assert digest(Path(name)) == wanted
source = Path(parent['newObserver']).read_text(encoding='utf-8')
anchor = '    private static int verifyActualOriginalSessionReturns('
position = source.index(anchor)
helper = '    private static Object diagnosticOptional(Object value, String name) throws Exception {\n        if (value == null) return null;\n        try { return read(value, name); } catch (NoSuchFieldException missing) { return null; }\n    }\n\n'
source = source[:position] + helper + source[position:]
anchor = '            Map<String,Object> item = diagnosticState(producer); producers.add(item);'
assert source.count(anchor) == 1
extra = '\n            item.put("originalConsumerState", diagnosticState(diagnosticOptional(producer, "originalConsumer")));\n            Object consumerPrepared = diagnosticOptional(producer, "consumerPrepared");\n            item.put("allPreparedState", diagnosticState(consumerPrepared));\n            item.put("allAcceptedState", diagnosticState(diagnosticOptional(consumerPrepared, "originalSequence")));\n            item.put("allCommitState", diagnosticState(diagnosticOptional(producer, "consumerCommit")));\n            item.put("planState", diagnosticState(diagnosticOptional(producer, "plan")));\n'
source = source.replace(anchor,anchor+extra)
observer = WORK / ('tests/native-publishing-binding-diagnostic-' + tag + '/PositiveWeapon436.java')
launcher = WORK / ('tools/run_native_publishing_binding_diagnostic_' + tag + '.py')
preparation = WORK / ('reports/native-publishing-binding-diagnostic-preparation-' + tag + '.json')
text = Path(parent['newLauncher']).read_text(encoding='utf-8')
text = text.replace(Path(parent['newObserver']).relative_to(WORK).as_posix(),observer.relative_to(WORK).as_posix())
text = text.replace(parent_path.relative_to(WORK).as_posix(),preparation.relative_to(WORK).as_posix())
text = text.replace('reports/native-repair-runtime-passive-root-completion-diagnostic2-' + tag + '.json','reports/native-repair-runtime-publishing-binding-diagnostic-' + tag + '.json')
helper_path = WORK / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__':str(helper_path)}
exec(compile(helper_path.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper_path),'exec'),namespace)
namespace['add'](observer.relative_to(ROOT).as_posix(),source)
namespace['add'](launcher.relative_to(ROOT).as_posix(),text)
paths=[Path(__file__),parent_path,Path(parent['newObserver']),Path(parent['newLauncher']),observer,launcher,helper_path]
with preparation.open('x',encoding='utf-8') as stream:
    json.dump({'newObserver':str(observer),'newLauncher':str(launcher),'readOnlyDiagnostic':True,'completeOriginalSessionAssertionsRetained':True,'authorityFieldsWritten':False,'productClassesModified':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'allProducerConsumerFlagsReadOnly':True}))
