import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
owner_path = root / 'WRITE_OWNER.json'
owner = json.loads(owner_path.read_text(encoding='utf-8'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
stage = work / 'reports/native-continuation-acceptance-2ca083af-stage5.json'
receipt = json.loads(stage.read_text(encoding='utf-8'))
for name, wanted in receipt['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
adopted = root / 'work/session-chain/reports/actual-current.json'
assert hashlib.sha256(adopted.read_bytes()).hexdigest() == '1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
owner['lastRuntimePid'] = 83344
owner['lastRuntimeTerminal'] = True
owner['ownedJavaOrPythonProcessesRemaining'] = False
owner['latestNativeContinuationFocusedAcceptance'] = stage.relative_to(root).as_posix()
owner['latestNativeCorruptContinuationRecoveryEvidence'] = 'work/session-lease/reports/native-corrupt-continuation-recovery-focused-evidence.json'
owner['nativeCorruptRecoveryProtocol'] = 'work/session-lease/CORRUPT_CONTINUATION_RECOVERY_ACCEPTANCE.md'
owner['nextNativeContinuationWork'] = [
    'six-family repair/continuation object-policy gap, including axe/staff and actual subtype/provider provenance; see SIX_FAMILY_REPAIR_GAP.md',
    'remaining full lifecycle/final recovery audit',
    'recipes, retired-feature, client/authenticated-multiplayer, four GL directions and final migration acceptance'
]
owner_path.write_text(json.dumps(owner, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'sameWriterRetained': True, 'stage5VerifiedHashes': len(receipt['inputHashes']), 'adoptedUnchanged': True, 'installable': False}))
