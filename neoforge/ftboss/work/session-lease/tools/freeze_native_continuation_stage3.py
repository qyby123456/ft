import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}
receipts = {}
for name in ['native-continuation-acceptance-2ca083af-stage2', 'native-active-cas-crash-focused-evidence']:
    path = WORK / 'reports' / (name + '.json')
    receipt = json.loads(path.read_text(encoding='utf-8-sig'))
    assert receipt['installable'] is False and receipt['deployed'] is False and receipt['promoted'] is False
    for source, wanted in receipt['inputHashes'].items():
        assert hashlib.sha256(Path(source).read_bytes()).hexdigest() == wanted, source
        hashes[source] = wanted
    hashes[str(path)] = hashlib.sha256(path.read_bytes()).hexdigest()
    receipts[name] = str(path)
hashes[str(Path(__file__))] = hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
result = {
    'candidate': 'native-repair-2ca083af', 'frozenFocusedEvidence': receipts,
    'priorNormalRestartsSignedSessionsReplayPendingAndWearChecksReusedWithoutRerun': True,
    'verifiedProcessInterruptionAfterActualActiveCasBeforePhysicalPublication': True,
    'actualNewProcessNoReissueAndNoClaimReopenAfterThatCrash': True,
    'deliberateCrashIsNotRepresentedAsNormallySavedWorld': True,
    'remainingAcceptance': [
        'actual transformed Curios factory and JVM-defined loader-final byte provenance',
        'remaining corrupt-continuation and full lifecycle recovery audit',
        'six retained families including axe and staff, fractional wear and lifecycle variants',
        'ten external recipes and retired protocol/catalog/translation closure',
        'actual client, dedicated server, authenticated multiplayer, four GL directions and final migration closure',
    ],
    'authorityWrittenByReflection': False, 'testMixinUsed': False,
    'fullLifecycleAccepted': False, 'fullMigrationAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-continuation-acceptance-2ca083af-stage3.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'verifiedCrashGateAdded': True, 'inputCount': len(hashes), 'fullMigrationAccepted': False})
