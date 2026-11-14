import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
owner_path = root / 'WRITE_OWNER.json'
owner = json.loads(owner_path.read_text(encoding='utf-8'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
evidence = work / 'reports/native-six-family-paid-repair-restart-focused-evidence.json'
receipt = json.loads(evidence.read_text(encoding='utf-8'))
for name, wanted in receipt['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
candidate = work / 'reports/native-repair-six-family-candidate-7751b0a9.json'
adopted = root / 'work/session-chain/reports/actual-current.json'
assert hashlib.sha256(adopted.read_bytes()).hexdigest() == '1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
owner['lastRuntimePid'] = 26084
owner['lastRuntimeTerminal'] = True
owner['ownedJavaOrPythonProcessesRemaining'] = False
owner['latestNativeRepairPrototype'] = candidate.relative_to(root).as_posix()
owner['latestNativeRepairPrototypeSha256'] = hashlib.sha256(candidate.read_bytes()).hexdigest()
owner['latestNativeSixFamilyPaidRepairEvidence'] = evidence.relative_to(root).as_posix()
owner['latestNativeSixFamilyCensusEvidence'] = 'work/session-lease/reports/native-six-family-registered-census-evidence.json'
owner['latestNativeSixFamilyPolicyDeltaEvidence'] = 'work/session-lease/reports/native-six-family-policy-delta-7751b0a9.json'
owner['nativeSixFamilyPaidRepairProtocol'] = 'work/session-lease/SIX_FAMILY_PAID_REPAIR_ACCEPTANCE.md'
owner['nativeContinuationStatus'] = '7751b0a9 focused six native-damageable representative paid repairs and real restart accepted; previous stage5 evidence remains scoped to 2ca083af; unpromoted NOT-INSTALLABLE; same sole writer continues'
owner['nextNativeContinuationWork'] = [
    'actual signed combat Sessions for six families, including axe melee and staff casting; no empty-lease substitute',
    'retained subclasses/external provider, fractional wear/lifecycle matrix and eight zero-original-durability staff paths',
    'new-candidate rejection/failure audit with justified immutable frozen-evidence reuse',
    'remaining full lifecycle/final recovery audit, recipes, retired-feature, real client/dedicated/authenticated multiplayer, four GL directions and final migration'
]
owner_path.write_text(json.dumps(owner, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'sameWriterRetained': True, 'verifiedHashes': len(receipt['inputHashes']), 'latestPrototype': '7751b0a9', 'adoptedUnchanged': True, 'installable': False}))
