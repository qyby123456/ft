import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
parent=work/'reports/native-material-ranged-bow-control-signed-preparation-2be7d6c1.json'
frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher'])
source=old_source.read_text(encoding='utf-8')
anchor='                if (!noAmmoRefused) throw new AssertionError("Original no-ammo bow use altered paid state or leaked private use scope");'
assert source.count(anchor)==1;start=source.index(anchor)+len(anchor)
end=source.index('                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);',start)
source=source[:start]+'\n                target.discard();\n'+source[end:]
source=source.replace('Actual paid original ItemStack.use -> early original release -> original ItemStack.use -> full original Bow releaseUsing -> actual Arrow.tick impact and full signed Session','Actual original no-ammo ItemStack.use refusal and outside callbacks on genuinely paid material Bows; no projectile impact executed by this control fixture')
new_source=work/'tests/material-ranged-bow-controls-qualified-2be7d6c1/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-bow-control-signed-2be7d6c1.json','native-repair-runtime-material-ranged-bow-controls-qualified-2be7d6c1.json')
preparation=work/'reports/native-material-ranged-bow-controls-qualified-preparation-2be7d6c1.json'
launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_material_ranged_bow_controls_qualified_2be7d6c1.py'
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'productChanged':False,'scope':'All6 Bow foreign callback and no-ammo same authority/image/no scope leak controls only; remove redundant positive full signed impacts covered by separate actual runtime.','inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualBowControls':6,'signedImpactClaimByThisFixture':False}))
