import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
path = WORK / 'reports/native-repair-runtime-repair-reload-logout-storage.json'
result = json.loads(path.read_text(encoding='utf-8-sig'))
terminal_path = Path(result['attempt']) / 'terminal308.json'
terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
assert result['exitCode'] == terminal['exitCode'] == result['probe']['failures'] == 0
assert terminal['stopSent'] and terminal['completeSave']
probe = result['probe']
for key in ['originalPlayerListLogoutRan', 'originalLogoutSaveThenSameProcessOriginalLoadRestoredGenuineSuccessor',
            'sameEpochAndWholeItemPreserved', 'oldPhysicalLeaseAndInputRetiredAfterSuccessor', 'scratchPlayerNotAuthenticated',
            'worldJoinAndNegotiatedClientLoginNotExercised']:
    assert probe[key]
assert probe['sameProcessSuccessorGeneration'] == probe['originalLogoutSavedGeneration'] + 1
hashes = {}
for source, wanted in result['inputHashes'].items():
    assert hashlib.sha256(Path(source).read_bytes()).hexdigest() == wanted
    hashes[source] = wanted
for source in [path, terminal_path, Path(__file__)]:
    hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
failed_path = WORK / 'reports/native-repair-runtime-repair-reload-logout-login.json'
failed = json.loads(failed_path.read_text(encoding='utf-8-sig'))
assert failed['exitCode'] == 0 and failed['probe']['failures'] == 1
assert 'Connection.channel()' in failed['probe']['failure']
for source in [failed_path, Path(failed['attempt']) / 'terminal308.json']:
    hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
evidence = {
    'candidate': 'native-repair-2ca083af',
    'actualOriginalPlayerListRemoveAndItsOriginalPlayerSaveFocusedAccepted': True,
    'sameProcessOriginalPlayerStorageLoadRestoresGenuineSuccessor': True,
    'sameEpochAndWholeItemPreserved': True,
    'oldPhysicalLeaseAndInputRetiredAfterSuccessor': True,
    'generationBeforeAndAfter': [probe['originalLogoutSavedGeneration'], probe['sameProcessSuccessorGeneration']],
    'scratchPlayerNotAuthenticated': True,
    'authenticatedLoginProtocolAccepted': False,
    'worldJoinAndNegotiatedClientLoginExercised': False,
    'priorIncompleteNetworkFixtureFailurePreserved': True,
    'fullLifecycleAccepted': False,
    'authorityWrittenByReflection': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-logout-original-reload-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(evidence, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'originalLogoutAndSameProcessReloadPassed': True, 'inputCount': len(hashes)})
