import hashlib
import json
import sys
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
mode=sys.argv[1];assert mode in ['restart','controls']
audit=work/'reports/native-remaining-variant-gap-audit-3320e085.json';ids=[row['id'] for row in load(audit)['groups']['otherExactOriginalClassesStillNeedActualMatrix']];assert len(ids)==19
base='signed-qualified' if mode=='restart' else 'paid-repair-pair-qualified'
parent=work/('reports/native-exact-melee-variants-'+base+'-preparation-41d445cf.json');frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
seed=work/('reports/native-repair-runtime-exact-melee-variants-'+base+'-41d445cf.json');result=load(seed);terminal=Path(result['attempt'])/'terminal308.json';end=load(terminal)
assert result['exitCode']==0 and result['probe']['failures']==0 and end['completeSave'] and end['stopSent']
assert [row['id'] for row in result['probe']['cases']]==ids
if mode=='restart':
    original=work/'tests/bow-original-use-restart-529ba4c9/PositiveWeapon436.java';proof=work/'reports/native-bow-original-use-signed-restart-focused-evidence-529ba4c9.json'
else:
    original=work/'tests/melee-foreign-item-eef72352/PositiveWeapon436.java';proof=work/'reports/native-melee-paid-signed-combat-controls-focused-evidence-eef72352.json'
assert digest(original)==load(proof)['inputHashes'][str(original)]
source=original.read_text(encoding='utf-8').replace('new UUID(620, sample + 1)','new UUID(690, sample + 1)').replace('"ft_six_" + sample','"ft_exact_" + sample')
if mode=='restart':
    anchor='List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");';assert source.count(anchor)==1
    source=source.replace(anchor,'List<String> ids = List.of('+', '.join(json.dumps(value) for value in ids)+');')
    source=source.replace('for (int sample : new int[]{1}) {','for (int sample = 0; sample < ids.size(); sample++) {')
    source=source.replace('!= 3L','!= 2L').replace('!= 4L','!= 3L').replace('!= 3)','!= 2)')
    source=source.replace('generation-three','generation-two').replace('three to four','two to three')
    source=source.replace('sameEpochGenerationThreeToFour','sameEpochGenerationTwoToThree').replace('actualOriginalGenerationFourSaveCommitted','actualOriginalGenerationThreeSaveCommitted')
    source=source.replace('paidBowPostCombatContinuationAcrossNewProcess','nineteenExactMeleeVariantsPostCombatContinuationAcrossNewProcess')
    anchor='if (actual.getDamageValue() != 1)';assert source.count(anchor)==1
    source=source.replace(anchor,'if (actual.getDamageValue() != (actual.getItem().getClass() == net.minecraft.world.item.SwordItem.class ? 1 : 2))')
else:
    source=source.replace('for (int sample : new int[]{0, 3}) {','for (int sample = 0; sample < 19; sample++) {')
    source=source.replace('!= 5L','!= 2L').replace('generation five','generation two').replace('genuineGenerationFiveBeforeControl','genuineGenerationTwoBeforeControl')
    source=source.replace('sample == 0','actual.getItem().getClass() == net.minecraft.world.item.SwordItem.class')
    anchor='                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(actual.getItem()).toString();';assert source.count(anchor)==1
    source=source.replace(anchor,anchor+'\n                if (!List.of('+', '.join(json.dumps(value) for value in ids)+').get(sample).equals(id)) throw new AssertionError("Original exact control registry identity drift");')
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_source=work/('tests/exact-melee-variants-'+mode+'-41d445cf/PositiveWeapon436.java');ns['add'](new_source.relative_to(root).as_posix(),source)
old_launcher=Path(frozen['newLauncher']);launcher=old_launcher.read_text(encoding='utf-8')
launcher=launcher.replace(Path(frozen['newObserver']).relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
if mode=='restart':
    previous_seed='reports/native-repair-runtime-exact-melee-variants-paid-repair-pair-qualified-41d445cf.json'
    assert launcher.count(previous_seed)==1;launcher=launcher.replace(previous_seed,seed.relative_to(work).as_posix())
else:
    previous_seed="assert seed_result['probe']['sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess']\nassert len(seed_result['probe']['cases']) == 6\nassert all(case['actualOriginalGenerationTwoSaveCommitted'] for case in seed_result['probe']['cases'])"
    assert launcher.count(previous_seed)==1
    launcher=launcher.replace(previous_seed,"assert len(seed_result['probe']['cases']) == 19\nassert all(case['actualSuccessorOriginalSaveCommitted'] for case in seed_result['probe']['cases'])")
    launcher=launcher.replace('reports/native-repair-runtime-six-family-paid-restart.json',seed.relative_to(work).as_posix())
output=work/('reports/native-repair-runtime-exact-melee-variants-'+mode+'-41d445cf.json')
launcher=launcher.replace("output = WORK / "+repr(seed.relative_to(work).as_posix()),"output = WORK / "+repr(output.relative_to(work).as_posix()))
preparation=work/('reports/native-exact-melee-variants-'+mode+'-preparation-41d445cf.json');launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/('tools/run_native_exact_melee_variants_'+mode+'_41d445cf.py');ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),audit,parent,seed,terminal,original,proof,old_launcher,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':ids,'mode':mode,'seed':str(seed),'newObserver':str(new_source),'newLauncher':str(new_launcher),'sameProduct':'41d445cf','authorityFieldsWritten':False,'authenticatedMultiplayerAccepted':False,'installable':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'mode':mode,'cases':19}))
