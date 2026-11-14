import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
parent = work / 'reports/native-trident-foreign-controls-preparation-29296443.json'
frozen = json.loads(parent.read_text(encoding='utf-8'))
for name, wanted in frozen['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
preparation = work / 'reports/native-trident-qualified-controls-preparation-29296443.json'
paths = [Path(__file__), parent, helper]
controls = []
for control in frozen['controls']:
    mode = control['mode']
    old_source = Path(control['observer'])
    old_launcher = Path(control['launcher'])
    new_source = work / ('tests/trident-' + mode + '-qualified-29296443/PositiveWeapon436.java')
    new_launcher = work / ('tools/run_native_trident_' + mode.replace('-', '_') + '_qualified_29296443.py')
    source = old_source.read_text(encoding='utf-8')
    anchor = '                Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");'
    assert source.count(anchor) == 1 and source.count('var refresh = guard.getDeclaredMethod(') == 1
    source = source.replace(anchor, '                Class<?> foreignGuard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");').replace('var refresh = guard.getDeclaredMethod(', 'var refresh = foreignGuard.getDeclaredMethod(')
    namespace['add'](new_source.relative_to(root).as_posix(), source)
    launcher = old_launcher.read_text(encoding='utf-8')
    anchor = old_source.relative_to(work).as_posix()
    assert launcher.count(anchor) == 1
    launcher = launcher.replace(anchor, new_source.relative_to(work).as_posix())
    anchor = "save(attempt / 'inputs-before308.json', inputs)"
    head, tail = launcher.rsplit(anchor, 1)
    launcher = head + "inputs[str(WORK / 'reports/native-trident-qualified-controls-preparation-29296443.json')] = digest(WORK / 'reports/native-trident-qualified-controls-preparation-29296443.json')\n" + anchor + tail
    namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
    paths.extend([old_source, old_launcher, new_source, new_launcher])
    controls.append({'mode': mode, 'observer': str(new_source), 'launcher': str(new_launcher)})
with preparation.open('x', encoding='utf-8') as stream:
    json.dump({'candidate': frozen['candidate'], 'controls': controls, 'authorityFieldsWritten': False, 'priorCompileOnlyFailures': ['repair-runtime-9870d835', 'repair-runtime-7e0fd600'], 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({'prepared': preparation.name, 'controls': len(controls)}))
