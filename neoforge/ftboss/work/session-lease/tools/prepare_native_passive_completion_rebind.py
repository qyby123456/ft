import hashlib
import json
import sys
from pathlib import Path
WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
old_tag,new_tag = sys.argv[1:]
assert len(old_tag) == len(new_tag) == 8
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
parent_path = WORK / ('reports/native-passive-root-completion-diagnostic2-preparation-' + old_tag + '.json')
parent = json.loads(parent_path.read_text(encoding='utf-8'))
for name,wanted in parent['inputHashes'].items():
    assert digest(Path(name)) == wanted
old_binding = WORK / ('reports/native-repair-ranged-use-candidate-' + old_tag + '.json')
new_binding = WORK / ('reports/native-repair-ranged-use-candidate-' + new_tag + '.json')
candidate = json.loads(new_binding.read_text(encoding='utf-8'))
for name,wanted in candidate['inputHashes'].items():
    assert digest(Path(name)) == wanted
observer = Path(str(parent['newObserver']).replace(old_tag,new_tag))
launcher = Path(str(parent['newLauncher']).replace(old_tag,new_tag))
preparation = Path(str(parent_path).replace(old_tag,new_tag))
source = Path(parent['newObserver']).read_text(encoding='utf-8')
text = Path(parent['newLauncher']).read_text(encoding='utf-8').replace(old_tag,new_tag)
assert digest(old_binding) in text
text = text.replace(digest(old_binding),digest(new_binding))
helper = WORK / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
namespace['add'](observer.relative_to(ROOT).as_posix(),source)
namespace['add'](launcher.relative_to(ROOT).as_posix(),text)
paths=[Path(__file__),parent_path,Path(parent['newObserver']),Path(parent['newLauncher']),new_binding,observer,launcher,helper]
with preparation.open('x',encoding='utf-8') as stream:
    json.dump({'newObserver':str(observer),'newLauncher':str(launcher),'readOnlyDiagnostic':True,'fullOriginalSessionAssertionsRetained':True,'authorityFieldsWritten':False,'productClassesModified':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'sameReadOnlyProducerDiagnosticOnChangedCandidate':True}))
