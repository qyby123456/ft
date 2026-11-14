import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
parent=work/'reports/native-material-ranged-crossbow-control-signed-preparation-3320e085.json'
frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);';assert source.count(anchor)==1
source=source.replace(anchor,"""                if (target.getHealth() >= healthBefore) throw new AssertionError("Foreign original arrow did not hit this case's actual target");
                target.discard();
                row.put("actualOriginalTargetHitAndRemovedBeforeNextCase", true);
"""+anchor)
new_source=work/'tests/material-ranged-crossbow-controls-qualified-3320e085/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-crossbow-control-signed-3320e085.json','native-repair-runtime-material-ranged-crossbow-controls-qualified-3320e085.json')
preparation=work/'reports/native-material-ranged-crossbow-controls-qualified-preparation-3320e085.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_material_ranged_crossbow_controls_qualified_3320e085.py'
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
excluded=work/'reports/native-repair-runtime-material-ranged-crossbow-control-signed-3320e085.json'
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher,excluded]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'productChanged':False,'observerCorrection':'Remove each actual hit target before next case; old fixture accumulated same-position targets and fifth had no damage event. Preserve all original negative authority assertions and additionally require current actual target health loss.','inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'sameProduct':'3320e085','negativeAssertionsNotRemoved':True}))
