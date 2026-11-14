import hashlib
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
namespace={'__file__':str(helper)}
text=helper.read_text(encoding='utf-8');exec(text[:text.index('probe=(WORK/')],namespace)
create=namespace['create']
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def digest(path):
    with path.open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
audit=WORK/'reports/native-remaining-variant-gap-audit-3320e085.json'
ids=[row['id'] for row in load(audit)['groups']['otherExactOriginalClassesStillNeedActualMatrix']]
assert len(ids)==19
original=WORK/'tests/bow-original-use-restart-529ba4c9/PositiveWeapon436.java'
proof=WORK/'reports/native-bow-original-use-signed-restart-focused-evidence-529ba4c9.json'
assert digest(original)==load(proof)['inputHashes'][str(original)]
source=original.read_text(encoding='utf-8').replace('new UUID(620, sample + 1)','new UUID(690, sample + 1)').replace('"ft_six_" + sample','"ft_exact_" + sample')
anchor='List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");'
assert source.count(anchor)==1
source=source.replace(anchor,'List<String> ids = List.of('+', '.join(json.dumps(value) for value in ids)+');')
source=source.replace('for (int sample : new int[]{1}) {','for (int sample = 0; sample < ids.size(); sample++) {')
source=source.replace('!= 3L','!= 2L').replace('!= 4L','!= 3L').replace('!= 3)','!= 2)')
source=source.replace('generation-three','generation-two').replace('three to four','two to three')
source=source.replace('sameEpochGenerationThreeToFour','sameEpochGenerationTwoToThree').replace('actualOriginalGenerationFourSaveCommitted','actualOriginalGenerationThreeSaveCommitted')
source=source.replace('paidBowPostCombatContinuationAcrossNewProcess','nineteenConfiguredOriginalMeleePostCombatContinuationAcrossNewProcess')
anchor='if (actual.getDamageValue() != 1)';assert source.count(anchor)==1
source=source.replace(anchor,'if (actual.getDamageValue() != (actual.getItem().getClass() == net.minecraft.world.item.SwordItem.class ? 1 : 2))')
new_source=WORK/'tests/material-proc-nineteen-restart-cf217105/PositiveWeapon436.java';create(new_source,source)
parent=WORK/'tools/run_native_material_proc_whole_nineteen_cf217105.py'
runner=parent.read_text(encoding='utf-8')
runner=runner.replace('tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java',new_source.relative_to(WORK).as_posix())
runner=runner.replace('reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json','reports/native-repair-runtime-material-proc-whole-nineteen-cf217105.json')
anchor="assert all(case['originalConfiguredWeaponNotBrokenBeforeAnyPaidAuthority'] for case in seed_result['probe']['cases'])";assert runner.count(anchor)==1
runner=runner.replace(anchor,"assert seed_result['probe']['nineteenConfiguredOriginalMeleeGenerationTwoFullSignedSessions']\nassert all(case['actualOriginalPostCombatSaveAuthenticatedCommitted'] for case in seed_result['probe']['cases'])")
anchor="output = WORK / 'reports/native-repair-runtime-material-proc-whole-nineteen-cf217105.json'";assert runner.count(anchor)==1
runner=runner.replace(anchor,"output = WORK / 'reports/native-repair-runtime-material-proc-nineteen-restart-cf217105.json'")
new_runner=WORK/'tools/run_native_material_proc_nineteen_restart_cf217105.py';create(new_runner,runner)
report={'original':str(original),'proof':str(proof),'newObserver':str(new_source),'newRunner':str(new_runner),'ids':ids,
        'onlyInputAdaptations':['original nineteen registry ids/UUIDs/player names','generation two to three instead of three to four','exact original class-specific sword1/axe2 wear'],
        'allOriginalRestartAssertionsPreserved':True,'acceptedCompleteWholeNineteenSeedRequiredAtLaunch':True,
        'failedWorldAllowedAsSeed':False,'nativeRestartAccepted':False,'installable':False,'deployed':False,
        'inputHashes':{str(path):digest(path) for path in [original,proof,audit,parent,new_source,new_runner,Path(__file__)]}}
with (WORK/'reports/material-proc-nineteen-restart-preparation-cf217105.json').open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'fixturePrepared':True,'runtimeNotStarted':True,'acceptedWholeNineteenSeedRequired':True}),flush=True)
