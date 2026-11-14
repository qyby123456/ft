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
text=original.read_text(encoding='utf-8').replace('3320e085','41d445cf')
text=text.replace('native-material-ranged-absent-component-source-delta-41d445cf.json','native-shared-repair-owner-growth-source-delta-41d445cf.json')
anchor="assert row['ammoBefore']==2 and row['ammoAfterCharge']==1";assert text.count(anchor)==1
text=text.replace(anchor,"assert row['ammoBefore']==16 and row['ammoAfterCharge']==15")
text=text.replace('native-repair-runtime-material-ranged-crossbow-control-signed-41d445cf.json','native-repair-runtime-material-ranged-crossbow-controls-qualified-3320e085.json')
anchor="assert len({attempt,restart_attempt,control_attempt})==3";assert text.count(anchor)==1
text=text.replace(anchor,anchor+"""
for row in controls['cases']:assert row['actualOriginalTargetHitAndRemovedBeforeNextCase'] and row['healthAfter']<row['healthBefore']
capacity_path=work/'reports/native-shared-repair-owner-growth-focused-evidence-41d445cf.json'
capacity=load(capacity_path);frozen(capacity);assert capacity['failures']==0 and capacity['actualCases']==6
assert capacity['allPriorGenuineOwnersPreservedDuringGrowth'] and capacity['foreignPublicSharedNodeMutationNoticeRetiresAllSixWithoutItemMutation']
controls_proof_path=work/'reports/native-material-crossbow-foreign-controls-focused-evidence-3320e085.json'
controls_proof=load(controls_proof_path);frozen(controls_proof);assert controls_proof['failures']==0 and controls_proof['actualCases']==6
for path in [original,proof]:digest(path)
""")
text=text.replace("'positiveReuseOn3320'","'positiveRuntimeScope'").replace("'Actual3320 runtime'","'Actual41d445cf runtime'")
anchor="'scope':'Unenchanted ordinary arrow only; no arbitrary subclasses/special ammo or authenticated clients certified.'";assert text.count(anchor)==1
text=text.replace(anchor,"'scope':'Unenchanted ordinary arrow only; positive full signed impact and new JVM restart actually run on41. Foreign controls3320 reused by exact source delta: only monitored owner capacity allocation changes, original helper/caller/signing refusals unchanged; actual41 six-owner preservation and all-owner refusal fanout independently verified. No arbitrary subclasses/special ammo or authenticated clients certified.'")
namespace={'__file__':str(Path(__file__).resolve()),'original':original,'proof':proof}
exec(compile(text,str(original),'exec'),namespace)
