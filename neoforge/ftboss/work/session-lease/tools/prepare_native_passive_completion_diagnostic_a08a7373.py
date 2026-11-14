import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
TAG = 'a08a7373'

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

parent_path = WORK / ('reports/native-passive-root-signed-preparation-' + TAG + '.json')
parent = json.loads(parent_path.read_text(encoding='utf-8'))
for name, expected in parent['inputHashes'].items():
    assert digest(Path(name)) == expected, Path(name).name
source = Path(parent['newObserver']).read_text(encoding='utf-8')
anchor = '        if (!exactIdentityCoverage(nativePuts.keySet(), nativeReturned)'
assert source.count(anchor) == 1
extra = '        row.put("diagnostic_nativeQuoteCount", nativePuts.size());\n        row.put("diagnostic_nativeReturnedCount", nativeReturned.size());\n        row.put("diagnostic_materializedCount", materialized.size());\n        row.put("diagnostic_targetWriteCount", targetWrites.size());\n        row.put("diagnostic_targetReturnedCount", targetReturned.size());\n        Object observationFailure = read(session, "firstObservationFailure");\n        row.put("diagnostic_firstObservationFailure", observationFailure == null ? null : observationFailure.toString());\n        List<Object> quotes = new ArrayList<>(); row.put("diagnostic_nativeQuotes", quotes);\n        for (var entry : nativePuts.entrySet()) {\n            Object quote = read(entry.getValue(), "quote");\n            Map<String,Object> item = new LinkedHashMap<>(); quotes.add(item);\n            item.put("leafClass", entry.getKey().getClass().getName());\n            item.put("key", read(quote, "key"));\n            item.put("quoteLeafMatches", read(quote, "leaf") == entry.getKey());\n            item.put("returned", nativeReturned.contains(entry.getKey()));\n            item.put("materialized", materialized.containsValue(entry.getValue()));\n        }\n        List<Object> groupsBefore = new ArrayList<>(); row.put("diagnostic_groups", groupsBefore);\n        for (Object group : ((Map<?,?>)read(book, "groups")).values()) {\n            Map<String,Object> item = new LinkedHashMap<>(); groupsBefore.add(item);\n            for (String key : List.of("profile", "done", "acquired", "rooted")) item.put(key, read(group,key));\n            item.put("timers", new ArrayList<>((java.util.Set<?>)read(group, "timers")));\n            item.put("ranged", new ArrayList<>((java.util.Set<?>)read(group, "ranged")));\n        }\n        Object issuer = read(read(session, "lineage"), "issuer");\n        row.put("diagnostic_issuerClass", issuer.getClass().getName());\n        row.put("diagnostic_issuerFieldPreparationMatches", read(issuer, "fieldPreparation") == preparation);\n        Object sequenceBinding = read(issuer, "fieldSequenceBinding");\n        row.put("diagnostic_issuerFieldSequenceBindingPresent", sequenceBinding != null);\n        row.put("diagnostic_issuerFieldSequenceMatches", sequenceBinding != null && read(sequenceBinding, "sequence") == session);\n        ThreadLocal<?> passiveRoot = (ThreadLocal<?>)readStatic(Class.forName("net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority"), "PASSIVE_ROOT");\n        row.put("diagnostic_privatePassiveRootCleared", passiveRoot.get() == null);\n'
source = source.replace(anchor, extra + anchor)
observer = WORK / ('tests/native-passive-root-completion-diagnostic-' + TAG + '/PositiveWeapon436.java')
launcher = WORK / ('tools/run_native_passive_root_completion_diagnostic_' + TAG + '.py')
preparation = WORK / ('reports/native-passive-root-completion-diagnostic-preparation-' + TAG + '.json')
text = Path(parent['newLauncher']).read_text(encoding='utf-8')
old_observer = Path(parent['newObserver']).relative_to(WORK).as_posix()
assert old_observer in text
text = text.replace(old_observer, observer.relative_to(WORK).as_posix())
text = text.replace(parent_path.relative_to(WORK).as_posix(), preparation.relative_to(WORK).as_posix())
text = text.replace('reports/native-repair-runtime-passive-root-signed-' + TAG + '.json', 'reports/native-repair-runtime-passive-root-completion-diagnostic-' + TAG + '.json')
helper = WORK / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
namespace['add'](observer.relative_to(ROOT).as_posix(), source)
namespace['add'](launcher.relative_to(ROOT).as_posix(), text)
paths = [Path(__file__), parent_path, Path(parent['newObserver']), Path(parent['newLauncher']), observer, launcher, helper]
with preparation.open('x', encoding='utf-8') as stream:
    json.dump({'newObserver':str(observer),'newLauncher':str(launcher),'readOnlyDiagnostic':True,'fullOriginalSessionAssertionsRetained':True,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'completeAssertionsRetained':True,'productClassesModified':False}))
