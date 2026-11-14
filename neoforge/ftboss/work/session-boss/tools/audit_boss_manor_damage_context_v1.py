import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-manor-damage-context-audit-v1-20261114'
JAVA = Path('C:/Program Files/Zulu/zulu-21/bin')
assert not OUT.exists()
OUT.mkdir()
def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()
context = json.loads((WORK / 'build/boss-flight-ban-v1/compile-context.json').read_text(encoding='utf-8-sig'))
built = json.loads((WORK / 'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json').read_text(encoding='utf-8'))
selected = {'original': (ROOT / 'reference/forge-current.jar', ['net/farmingtales/ftboss/entity/EasterBossEntity.class', 'net/farmingtales/ftboss/boss/ai/ManorFinalDamage.class']), 'current': (Path(built['jar']), ['net/farmingtales/ftboss/entity/EasterBossEntity.class', 'net/farmingtales/ftboss/boss/ai/ManorFinalDamage.class', 'net/farmingtales/ftboss/event/NativeManorDamageEvents.class', 'net/farmingtales/ftboss/boss/ai/NativeManorFinalDamageAccess.class'])}
for entry in context['classpath'].split(';'):
    path = Path(entry)
    if path.name.endswith('neoforge-21.1.233-client.jar'):
        selected['neoforge-living'] = (path, ['net/minecraft/world/entity/LivingEntity.class'])
    if path.name.endswith('neoforge-21.1.233-universal.jar'):
        selected['neoforge-api'] = (path, ['net/neoforged/neoforge/common/damagesource/DamageContainer.class', 'net/neoforged/neoforge/event/entity/living/LivingDamageEvent.class', 'net/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre.class', 'net/neoforged/neoforge/event/entity/living/LivingDamageEvent$Post.class', 'net/neoforged/neoforge/common/CommonHooks.class'])
assert len(selected) == 4
inputs = {}
for label, (path, names) in selected.items():
    packed = OUT / (label + '-classes.jar')
    with zipfile.ZipFile(path) as source, zipfile.ZipFile(packed, 'x', compression=zipfile.ZIP_DEFLATED) as target:
        for name in names:
            payload = source.read(name)
            target.writestr(name, payload)
            inputs[label + '/' + name] = hashlib.sha256(payload).hexdigest()
    destination = OUT / (label + '-sources')
    destination.mkdir()
    command = [str(JAVA / 'java.exe'), '-Xmx384m', '-jar', str(WORK / 'tools/vineflower-1.10.1.jar'), '-thr=1', str(packed), str(destination)]
    with (OUT / (label + '-decompile.log')).open('xb') as stream:
        result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, timeout=90, creationflags=subprocess.CREATE_NO_WINDOW)
    assert result.returncode == 0
(OUT / 'audit-inputs.json').write_text(json.dumps({'selectedClassHashes': inputs, 'inputArchiveHashes': {label: sha(path) for label, (path, names) in selected.items()}, 'actualFailedRuntimeReceipt': str(WORK / 'reports/sparrow-evasion-native-baseline-v2-39125bde.json')}, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'audit': str(OUT), 'decompiledGroups': len(selected), 'selectedClasses': len(inputs)}), flush=True)
