import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
parent=work/'reports/native-retired-food-closure-focused-evidence-99c65f41.json'
frozen=json.loads(parent.read_text(encoding='utf-8'));assert frozen['failures']==0
origin_receipt=work/'reports/native-repair-runtime-six-family-paid-repair-local-channels.json'
original=json.loads(origin_receipt.read_text(encoding='utf-8'))
old_source=work/'tests/six-family-paid-repair-local-channels/PositiveWeapon436.java'
assert hashlib.sha256(old_source.read_bytes()).hexdigest()==original['inputHashes'][str(old_source)]
old_launcher=work/'tools/run_native_retired_food_trident_qualified_99c65f41.py'
assert hashlib.sha256(old_launcher.read_bytes()).hexdigest()==frozen['inputHashes'][str(old_launcher)]
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
source=old_source.read_text(encoding='utf-8')
anchor='            List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");'
assert source.count(anchor)==1
ids=['ftboss:'+tier+'_'+family for family in ['bow','crossbow'] for tier in ['wooden','stone','iron','golden','diamond','netherite']]
source=source.replace(anchor,'            List<String> ids = List.of('+', '.join(json.dumps(identity) for identity in ids)+');')
source=source.replace('new UUID(620, sample + 1)','new UUID(651, sample + 1)').replace('"ft_six_" + sample','"ft_material_" + sample')
source=source.replace('sixNativeDamageableFamilyRepresentativesPaidRepairAndSameProcessReload','twelveMaterialRangedPaidRepairAndSameProcessReload')
new_source=work/'tests/material-ranged-paid-repair-baseline-99c65f41/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8')
origin_path='tests/retired-food-trident-qualified-99c65f41/PositiveWeapon436.java';assert launcher.count(origin_path)==1
launcher=launcher.replace(origin_path,new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-retired-food-trident-qualified-99c65f41.json','native-repair-runtime-material-ranged-paid-repair-baseline-99c65f41.json')
preparation=work/'reports/native-material-ranged-paid-repair-baseline-preparation-99c65f41.json'
anchor="save(attempt / 'inputs-before308.json', inputs)";head,tail=launcher.rsplit(anchor,1)
launcher=head+"inputs[str(WORK / 'reports/native-material-ranged-paid-repair-baseline-preparation-99c65f41.json')] = digest(WORK / 'reports/native-material-ranged-paid-repair-baseline-preparation-99c65f41.json')\n"+anchor+tail
new_launcher=work/'tools/run_native_material_ranged_paid_repair_baseline_99c65f41.py'
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,parent,origin_receipt,old_source,old_launcher,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':ids,'newObserver':str(new_source),'newLauncher':str(new_launcher),'productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualPaidRepairCases':len(ids),'baselineRejectionExpectedFromPolicy':True}))
