import hashlib
import json
import shutil
from pathlib import Path
work=Path(__file__).resolve().parents[1]
base=work/'tools/build_native_ranged_use.py'
frozen=json.loads((work/'reports/native-repair-ranged-use-candidate-9d1607e8.json').read_text(encoding='utf-8'))
assert hashlib.sha256(base.read_bytes()).hexdigest()==frozen['inputHashes'][str(base)]
text=base.read_text(encoding='utf-8')
anchor="    text = original.read_text(encoding='utf-8-sig')"
assert text.count(anchor)==1
text=text.replace(anchor,"    text = replacements.get(original, original).read_text(encoding='utf-8-sig')")
namespace={'__file__':str(base)}
exec(compile(text,str(base),'exec'),namespace)
candidate=work/'reports/native-repair-ranged-use-candidate.json'
receipt=json.loads(candidate.read_text(encoding='utf-8'))
attempt=Path(receipt['attempt'])
resource=work/'proposal-native/ftboss.native-trident-lease.mixins.json'
configuration=json.loads(resource.read_text(encoding='utf-8'))
assert configuration['mixins']==['NativeTridentFactoryBoundaryMixin','NativeVanillaTridentImpactMixin']
for name in configuration['mixins']:
    assert (attempt/'complete-classes/net/farmingtales/ftboss/mixin'/ (name+'.class')).is_file()
target=attempt/'resources'/resource.name
target.parent.mkdir()
shutil.copyfile(resource,target)
receipt['requiredProductionResources']={resource.name:str(target)}
receipt['tridentProductionHooksConfigured']=True
receipt['bindingSourcesPreserveProposalOverrides']=True
for path in [Path(__file__),resource,target]:
    receipt['inputHashes'][str(path)]=hashlib.sha256(path.read_bytes()).hexdigest()
candidate.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'tridentResourceFrozen':target.name,'bindingSourcesPreserveProposalOverrides':True,'newProductClasses':0}))
