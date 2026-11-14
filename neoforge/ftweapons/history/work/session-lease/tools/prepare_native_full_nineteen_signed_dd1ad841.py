import hashlib
import json
from pathlib import Path
WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
TAG = 'dd1ad841'
parent_path = WORK / ('reports/native-passive-root-signed-preparation-' + TAG + '.json')
parent = json.loads(parent_path.read_text(encoding='utf-8'))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
for name, wanted in parent['inputHashes'].items():
    assert digest(Path(name)) == wanted
source = Path(parent['newObserver']).read_text(encoding='utf-8')
anchor = 'for (int sample : new int[]{13}) {'
assert source.count(anchor) == 1
source = source.replace(anchor,'for (int sample = 0; sample < 19; sample++) {')
observer = WORK / ('tests/native-full-nineteen-signed-' + TAG + '/PositiveWeapon436.java')
launcher = WORK / ('tools/run_native_full_nineteen_signed_' + TAG + '.py')
preparation = WORK / ('reports/native-full-nineteen-signed-preparation-' + TAG + '.json')
text = Path(parent['newLauncher']).read_text(encoding='utf-8')
text = text.replace(Path(parent['newObserver']).relative_to(WORK).as_posix(),observer.relative_to(WORK).as_posix())
text = text.replace(parent_path.relative_to(WORK).as_posix(),preparation.relative_to(WORK).as_posix())
text = text.replace('reports/native-repair-runtime-passive-root-signed-' + TAG + '.json','reports/native-repair-runtime-full-nineteen-signed-' + TAG + '.json')
helper = WORK / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
namespace['add'](observer.relative_to(ROOT).as_posix(),source)
namespace['add'](launcher.relative_to(ROOT).as_posix(),text)
paths = [Path(__file__),parent_path,Path(parent['newObserver']),Path(parent['newLauncher']),observer,launcher,helper]
with preparation.open('x',encoding='utf-8') as stream:
    json.dump({'newObserver':str(observer),'newLauncher':str(launcher),'originalConfiguredWeapons':19,'completeOriginalStaticAndDynamicSessionAssertionsRetained':True,'runtimeAcceptance':False,'restartAccepted':False,'installable':False,'deployed':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'allNineteenOriginalCases':True,'assertionsWeakened':False}))
