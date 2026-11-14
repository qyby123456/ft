import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
owner_path = root / 'WRITE_OWNER.json'
owner = json.loads(owner_path.read_text(encoding='utf-8'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
stage = work / 'reports/native-continuation-acceptance-2ca083af-stage4.json'
receipt = json.loads(stage.read_text(encoding='utf-8'))
for name, wanted in receipt['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == wanted, name
owner['lastRuntimePid'] = 56708
owner['lastRuntimeTerminal'] = True
owner['ownedJavaOrPythonProcessesRemaining'] = False
owner['latestNativeContinuationFocusedAcceptance'] = stage.relative_to(root).as_posix()
owner['latestNativeDefinedFactoryEvidence'] = 'work/session-lease/reports/native-curios-defined-factory-focused-evidence.json'
owner['latestDefinitionCapturePathBoundaryIncident'] = 'work/session-lease/reports/definition-capture-path-boundary-incident.json'
owner['nativeDefinedFactoryProtocol'] = 'work/session-lease/JVM_DEFINED_FACTORY_ACCEPTANCE.md'
owner['nextNativeContinuationWork'] = [
    'remaining corrupt-continuation and full lifecycle recovery audit',
    'remaining six-family, recipes, retired-feature, client/authenticated-multiplayer and final migration acceptance'
]
owner_path.write_text(json.dumps(owner, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'sameWriterRetained': True, 'stage4VerifiedHashes': len(receipt['inputHashes']), 'installable': False}))
