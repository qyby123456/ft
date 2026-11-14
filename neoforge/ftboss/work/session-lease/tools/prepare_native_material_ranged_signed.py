import hashlib
import json
import re
import sys
from pathlib import Path
mode=sys.argv[1]
assert mode in ['bow','crossbow','bow-restart','crossbow-restart','bow-control','crossbow-control']
restart=mode.endswith('-restart')
family=mode.split('-')[0]
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
tag='e43eb814'
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
base_preparation=work/('reports/native-material-ranged-paid-repair-qualified-preparation-'+tag+'.json')
base=json.loads(base_preparation.read_text(encoding='utf-8'))
for name,wanted in base['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
old_launcher=Path(base['newLauncher'])
launcher=old_launcher.read_text(encoding='utf-8')
if restart:
    original=work/'tests/bow-original-use-restart-529ba4c9/PositiveWeapon436.java'
    original_receipt=work/'reports/native-bow-original-use-signed-restart-focused-evidence-529ba4c9.json'
    seed=work/('reports/native-repair-runtime-material-ranged-'+family+'-signed-'+tag+'.json')
else:
    original=work/('tests/bow-original-use-529ba4c9/PositiveWeapon436.java' if family=='bow' else 'tests/paid-crossbow-combat-9d1607e8/PositiveWeapon436.java')
    original_receipt=work/('reports/native-bow-original-use-signed-restart-focused-evidence-529ba4c9.json' if family=='bow' else 'reports/native-crossbow-paid-signed-combat-focused-evidence-9d1607e8.json')
    seed=work/('reports/native-repair-runtime-material-ranged-paid-repair-qualified-'+tag+'.json')
    if mode.endswith('-control'):
        original=work/('tests/bow-original-use-controls-529ba4c9/PositiveWeapon436.java' if family=='bow' else 'tests/crossbow-foreign-control-9d1607e8/PositiveWeapon436.java')
old=json.loads(original_receipt.read_text(encoding='utf-8'))
assert hashlib.sha256(original.read_bytes()).hexdigest()==old['inputHashes'][str(original)]
seed_result=json.loads(seed.read_text(encoding='utf-8'))
seed_terminal=Path(seed_result['attempt'])/'terminal308.json'
terminal=json.loads(seed_terminal.read_text(encoding='utf-8'))
assert seed_result['exitCode']==0 and seed_result['probe']['failures']==0 and terminal['completeSave'] and terminal['stopSent']
source=original.read_text(encoding='utf-8')
samples=list(range(6)) if family=='bow' else list(range(6,12))
source,count=re.subn(r'for \(int sample : new int\[\]\{[^}]+\}\)', 'for (int sample : new int[]{'+', '.join(map(str,samples))+'})',source);assert count==1
source=source.replace('new UUID(620, sample + 1)','new UUID(651, sample + 1)').replace('"ft_six_" + sample','"ft_material_" + sample')
if restart:
    ids=['ftboss:'+tier+'_'+kind for kind in ['bow','crossbow'] for tier in ['wooden','stone','iron','golden','diamond','netherite']]
    anchor='List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");'
    assert source.count(anchor)==1
    source=source.replace(anchor,'List<String> ids = List.of('+', '.join(json.dumps(value) for value in ids)+');')
    source=source.replace('!= 3L','!= 2L').replace('!= 4L','!= 3L').replace('!= 3)','!= 2)')
    if family=='crossbow':
        source=source.replace('if (actual.getDamageValue() != 1)', 'if (actual.getDamageValue() != 1)')
else:
    source=source.replace('!= 3L','!= 2L').replace('                if (sample != 1) continue;\n','')
source=source.replace('generation-three','generation-two').replace('three to four','two to three')
if not restart:
    anchor='            var server = event.getServer();'
    assert source.count(anchor)==1
    source=source.replace(anchor,anchor+'''
            Class<?> boundedPolicy = Class.forName("net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority");
            var boundedMethod = boundedPolicy.getDeclaredMethod("nativeMaterialRangedPolicy", net.minecraft.world.item.ItemStack.class); boundedMethod.setAccessible(true);
            java.util.Set<String> actualBounded = new java.util.TreeSet<>();
            for (var registered : net.minecraft.core.registries.BuiltInRegistries.ITEM)
                if (Boolean.TRUE.equals(boundedMethod.invoke(null, new net.minecraft.world.item.ItemStack(registered))))
                    actualBounded.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(registered).toString());
            java.util.Set<String> expectedBounded = new java.util.TreeSet<>();
            for (String kind : java.util.List.of("bow", "crossbow")) for (String tier : java.util.List.of("wooden", "stone", "iron", "golden", "diamond", "netherite"))
                expectedBounded.add("ftboss:" + tier + "_" + kind);
            report.put("actualRegisteredMaterialPolicyAcceptedIds", actualBounded);
            if (!actualBounded.equals(expectedBounded)) throw new AssertionError("Material policy registry boundary drift: " + actualBounded);
''')
new_source=work/('tests/material-ranged-'+mode+'-signed-'+tag+'/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=launcher.replace(Path(base['newObserver']).relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('reports/native-repair-runtime-six-family-paid-restart.json',seed.relative_to(work).as_posix())
anchor="assert seed_result['probe']['sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess']\nassert len(seed_result['probe']['cases']) == 6\nassert all(case['actualOriginalGenerationTwoSaveCommitted'] for case in seed_result['probe']['cases'])"
assert launcher.count(anchor)==1
expected_cases=6 if restart else 12
launcher=launcher.replace(anchor,"assert len(seed_result['probe']['cases']) == "+str(expected_cases))
launcher=launcher.replace('native-repair-runtime-material-ranged-paid-repair-qualified-'+tag+'.json','native-repair-runtime-material-ranged-'+mode+'-signed-'+tag+'.json')
launcher=launcher.replace("seed_receipt = WORK / 'reports/native-repair-runtime-material-ranged-"+mode+'-signed-'+tag+".json'", "seed_receipt = WORK / "+repr(seed.relative_to(work).as_posix()))
preparation=work/('reports/native-material-ranged-'+mode+'-signed-preparation-'+tag+'.json')
launcher=launcher.replace(base_preparation.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/('tools/run_native_material_ranged_'+mode.replace('-','_')+'_signed_'+tag+'.py')
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,base_preparation,original,original_receipt,old_launcher,new_source,new_launcher,seed,seed_terminal]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'mode':mode,'seed':str(seed),'newObserver':str(new_source),'newLauncher':str(new_launcher),'actualServerInstances':1,'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'mode':mode,'actualCases':6,'newProcess':True}))
