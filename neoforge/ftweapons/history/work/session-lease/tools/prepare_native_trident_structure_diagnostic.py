import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
root = work.parents[1]
preparation = work / 'reports/native-trident-use-diagnostic-distinct-preparation-9d1607e8.json'
frozen = json.loads(preparation.read_text(encoding='utf-8'))
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
for path in [old_source, old_launcher]:
    assert hashlib.sha256(path.read_bytes()).hexdigest() == frozen['inputHashes'][str(path)]
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
source = old_source.read_text(encoding='utf-8')
anchor = '                row.put("playerAliveBeforeUse", player.isAlive());'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + '''
                row.put("brokenBeforeOriginalUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));
                row.put("durabilityDepletedBeforeUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.durabilityDepleted(actual));
                row.put("cavityAppliesBeforeUse", net.farmingtales.ftboss.item.SwordCavityBuild.applies(actual));
                row.put("cavityMissingBeforeUse", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(actual));
                row.put("functionProfileBeforeUse", String.valueOf(net.farmingtales.ftboss.api.SwordCavityExtensions.functionProfile(actual)));
                row.put("functionStatusBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(actual)));
                row.put("nativePartsBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordNativeMaterials.parts(actual)));
                row.put("cavityCellsBeforeUse", String.valueOf(net.farmingtales.ftboss.item.SwordCavityBuild.cells(actual)));
                row.put("generationRetiredAfterStructureReads", read(generation, "retired"));
''')
new_source = work / 'tests/trident-structure-diagnostic-9d1607e8/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_structure_diagnostic_9d1607e8.py'
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8').replace('trident-use-diagnostic-distinct-9d1607e8', 'trident-structure-diagnostic-9d1607e8')
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
output = work / 'reports/native-trident-structure-diagnostic-preparation-9d1607e8.json'
assert not output.exists()
paths = [Path(__file__), preparation, helper, old_source, old_launcher, new_source, new_launcher]
output.write_text(json.dumps({'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'readOnlyDiagnosticNotPositiveAcceptance': True, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'prepared': True, 'diagnosticNotAcceptance': True}))
