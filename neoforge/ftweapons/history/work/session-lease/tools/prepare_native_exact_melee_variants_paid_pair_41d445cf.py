import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-exact-melee-variants-paid-repair-profile-qualified-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
excluded=work/'reports/native-repair-runtime-exact-melee-variants-paid-repair-profile-qualified-41d445cf.json';failed=load(excluded);terminal=Path(failed['attempt'])/'terminal308.json';end=load(terminal)
assert failed['probe']['failures']==1 and failed['exitCode']==0 and end['stopSent'] and end['completeSave']
assert 'Scratch statue pair invalid' in failed['probe']['failure'] and len(failed['probe']['cases'])==14
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='        server.overworld().setBlock(statuePos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);';assert source.count(anchor)==1
source=source.replace(anchor,"""        server.overworld().setBlock(statuePos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        server.overworld().setBlock(statuePos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        if (!server.overworld().getBlockState(statuePos).isAir() || !server.overworld().getBlockState(statuePos.above()).isAir())
            throw new AssertionError("Scratch original statue pair not cleared before rebuilding");
        row.put("originalDoubleBlockPairClearedBeforeProfileReplacement", true);
"""+anchor)
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_source=work/'tests/exact-melee-variants-paid-repair-pair-qualified-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-exact-melee-variants-paid-repair-profile-qualified-41d445cf.json','native-repair-runtime-exact-melee-variants-paid-repair-pair-qualified-41d445cf.json')
preparation=work/'reports/native-exact-melee-variants-paid-repair-pair-qualified-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_variants_paid_repair_pair_qualified_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,excluded,terminal,old_source,old_launcher,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':frozen['ids'],'newObserver':str(new_source),'newLauncher':str(new_launcher),'observerCorrection':'Clear previous actual double-block statue before placing new original profile/tint lower+upper pair; keep original statue identity validation and gameplay rules.','productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'cases':19,'worldFixtureCorrectionOnly':True}))
