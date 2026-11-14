import hashlib
import json
import sys
from pathlib import Path
assert sys.argv[1]=='crossbow'
work=Path(__file__).resolve().parents[1]
original=work/'tools/prove_native_material_ranged_family_3320e085.py'
proof=work/'reports/native-material-bow-paid-signed-restart-focused-evidence-3320e085.json'
frozen=json.loads(proof.read_text(encoding='utf-8'))
assert hashlib.sha256(original.read_bytes()).hexdigest()==frozen['inputHashes'][str(original)]
text=original.read_text(encoding='utf-8')
anchor="assert row['ammoBefore']==2 and row['ammoAfterCharge']==1";assert text.count(anchor)==1
text=text.replace(anchor,"assert row['ammoBefore']==16 and row['ammoAfterCharge']==15")
text=text.replace('native-repair-runtime-material-ranged-crossbow-control-signed-3320e085.json','native-repair-runtime-material-ranged-crossbow-controls-qualified-3320e085.json')
anchor="assert len({attempt,restart_attempt,control_attempt})==3";assert text.count(anchor)==1
text=text.replace(anchor,anchor+"""
for row in controls['cases']:assert row['actualOriginalTargetHitAndRemovedBeforeNextCase'] and row['healthAfter']<row['healthBefore']
excluded_path=work/'reports/native-repair-runtime-material-ranged-crossbow-control-signed-3320e085.json'
excluded=load(excluded_path);frozen(excluded);excluded_terminal=load(Path(excluded['attempt'])/'terminal308.json')
assert excluded['exitCode']==0 and excluded_terminal['stopSent'] and excluded_terminal['completeSave'] and excluded['probe']['failures']==1
assert len(excluded['probe']['cases'])==5 and excluded['probe']['cases'][-1]['foreignNativeUseNoSignedCarrier'] and not excluded['probe']['cases'][-1]['damageEvents']
for path in [original,proof]:digest(path)
""")
namespace={'__file__':str(Path(__file__).resolve()),'original':original,'proof':proof}
exec(compile(text,str(original),'exec'),namespace)
