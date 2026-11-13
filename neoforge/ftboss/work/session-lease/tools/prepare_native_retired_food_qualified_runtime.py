import hashlib
import json
import sys
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
mode=sys.argv[1];assert mode in ['trident','bow']
parent=work/('reports/native-retired-food-'+mode+'-preparation-99c65f41.json')
frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,name
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher'])
source=old_source.read_text(encoding='utf-8')
anchor='            try (var stream = PositiveWeapon436.class.getResourceAsStream("/assets/ftboss/lang/" + language + ".json")) {'
assert source.count(anchor)==1
replacement='            var actualResource = net.neoforged.fml.ModList.get().getModFileById("ftboss").getFile().findResource("assets", "ftboss", "lang", language + ".json");\n            try (var stream = Files.newInputStream(actualResource)) {'
source=source.replace(anchor,replacement)
new_source=work/('tests/retired-food-'+mode+'-qualified-99c65f41/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-retired-food-'+mode+'-99c65f41.json','native-repair-runtime-retired-food-'+mode+'-qualified-99c65f41.json')
preparation=work/('reports/native-retired-food-'+mode+'-qualified-preparation-99c65f41.json')
anchor="save(attempt / 'inputs-before308.json', inputs)";head,tail=launcher.rsplit(anchor,1)
launcher=head+"inputs[str(WORK / "+repr(preparation.relative_to(work).as_posix())+")] = digest(WORK / "+repr(preparation.relative_to(work).as_posix())+")\n"+anchor+tail
new_launcher=work/('tools/run_native_retired_food_'+mode+'_qualified_99c65f41.py')
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'mode':mode,'newObserver':str(new_source),'newLauncher':str(new_launcher),'actualResourceObservedThroughLoadedModFile':True,'productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'sameProductCandidate':'99c65f41'}))
