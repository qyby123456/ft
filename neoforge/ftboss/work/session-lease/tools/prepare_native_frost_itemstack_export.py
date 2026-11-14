import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
tag = 'b8eb35ff'
origin = work / ('tools/run_native_paid_frost_ingress_' + tag + '.py')
launcher = origin.read_text(encoding='utf-8')
anchor = "save(attempt / 'runtime-command308.json', command)"
assert launcher.count(anchor) == 1
launcher = launcher.replace(anchor, "command[1:1] = ['-Dmixin.debug.export=true', '-Dmixin.debug.export.filter=net.minecraft.world.item.ItemStack']\n" + anchor)
launcher = launcher.replace('reports/native-repair-runtime-paid-frost-ingress-', 'reports/native-repair-runtime-frost-itemstack-export-')
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
namespace['add']('work/session-lease/tools/run_native_frost_itemstack_export_' + tag + '.py', launcher)
print(json.dumps({'tag': tag, 'singleTransformedItemStackExport': True, 'productUnchanged': True}))
