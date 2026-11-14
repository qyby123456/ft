import hashlib
import json
import re
import sys
from pathlib import Path
family=sys.argv[1];assert family in ['bow','crossbow']
work=Path(__file__).resolve().parents[1];root=work.parents[1]
product=json.loads((work/'reports/native-repair-ranged-use-candidate.json').read_text(encoding='utf-8'))
tag=Path(product['attempt']).name.removeprefix('native-repair-')
seed_tag='2be7d6c1' if family=='bow' else tag
binding=work/('reports/native-repair-ranged-use-candidate-'+tag+'.json')
assert json.loads(binding.read_text(encoding='utf-8'))==product
parent=work/('reports/native-material-ranged-'+family+'-signed-preparation-'+seed_tag+'.json')
frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
seed=work/('reports/native-repair-runtime-material-ranged-'+family+'-signed-'+seed_tag+'.json')
result=json.loads(seed.read_text(encoding='utf-8'));terminal=Path(result['attempt'])/'terminal308.json';end=json.loads(terminal.read_text(encoding='utf-8'))
assert result['exitCode']==0 and result['probe']['failures']==0 and end['stopSent'] and end['completeSave']
assert len(result['probe']['cases'])==6
original=work/'tests/bow-original-use-restart-529ba4c9/PositiveWeapon436.java'
original_receipt=work/'reports/native-bow-original-use-signed-restart-focused-evidence-529ba4c9.json'
assert hashlib.sha256(original.read_bytes()).hexdigest()==json.loads(original_receipt.read_text(encoding='utf-8'))['inputHashes'][str(original)]
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
source=original.read_text(encoding='utf-8')
ids=['ftboss:'+tier+'_'+kind for kind in ['bow','crossbow'] for tier in ['wooden','stone','iron','golden','diamond','netherite']]
anchor='List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");';assert source.count(anchor)==1
source=source.replace(anchor,'List<String> ids = List.of('+', '.join(json.dumps(value) for value in ids)+');')
samples=list(range(6)) if family=='bow' else list(range(6,12))
source,count=re.subn(r'for \(int sample : new int\[\]\{[^}]+\}\)', 'for (int sample : new int[]{'+', '.join(map(str,samples))+'})',source);assert count==1
source=source.replace('new UUID(620, sample + 1)','new UUID(651, sample + 1)').replace('"ft_six_" + sample','"ft_material_" + sample')
source=source.replace('!= 3L','!= 2L').replace('!= 4L','!= 3L').replace('!= 3)','!= 2)')
source=source.replace('generation-three','generation-two').replace('three to four','two to three')
source=source.replace('sameEpochGenerationThreeToFour','sameEpochGenerationTwoToThree').replace('actualOriginalGenerationFourSaveCommitted','actualOriginalGenerationThreeSaveCommitted')
source=source.replace('paidBowPostCombatContinuationAcrossNewProcess','paidMaterialRangedPostCombatContinuationAcrossNewProcess')
new_source=work/('tests/material-ranged-'+family+'-restart-qualified-'+tag+'/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(),source)
old_launcher=Path(frozen['newLauncher']);launcher=old_launcher.read_text(encoding='utf-8')
launcher=launcher.replace('reports/native-repair-ranged-use-candidate-'+seed_tag+'.json',binding.relative_to(work).as_posix())
launcher,count=re.subn(r"assert digest\(binding\) == '[0-9a-f]{64}'",'assert digest(binding) == '+repr(hashlib.sha256(binding.read_bytes()).hexdigest()),launcher);assert count==1
launcher=launcher.replace(Path(frozen['newObserver']).relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('reports/native-repair-runtime-material-ranged-paid-repair-qualified-e43eb814.json',seed.relative_to(work).as_posix())
assert launcher.count("assert len(seed_result['probe']['cases']) == 12")==1
launcher=launcher.replace("assert len(seed_result['probe']['cases']) == 12","assert len(seed_result['probe']['cases']) == 6")
output=work/('reports/native-repair-runtime-material-ranged-'+family+'-restart-qualified-'+tag+'.json')
launcher=launcher.replace("output = WORK / "+repr(seed.relative_to(work).as_posix()),"output = WORK / "+repr(output.relative_to(work).as_posix()))
preparation=work/('reports/native-material-ranged-'+family+'-restart-qualified-preparation-'+tag+'.json')
launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/('tools/run_native_material_ranged_'+family+'_restart_qualified_'+tag+'.py')
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,parent,seed,terminal,original,original_receipt,old_launcher,new_source,new_launcher,binding]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'family':family,'seed':str(seed),'newObserver':str(new_source),'newLauncher':str(new_launcher),'generationBefore':2,'generationAfter':3,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'family':family,'actualCases':6,'sameEpoch':'2->3'}))
