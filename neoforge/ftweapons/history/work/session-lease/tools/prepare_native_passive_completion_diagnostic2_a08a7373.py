import hashlib
import json
from pathlib import Path
WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
parent_path = WORK / 'reports/native-passive-root-completion-diagnostic-preparation-a08a7373.json'
parent = json.loads(parent_path.read_text(encoding='utf-8'))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
for name, wanted in parent['inputHashes'].items():
    assert digest(Path(name)) == wanted
source = Path(parent['newObserver']).read_text(encoding='utf-8')
anchor = '    private static int verifyActualOriginalSessionReturns('
position = source.index(anchor)
helper = '    private static Map<String,Object> diagnosticState(Object value) throws Exception {\n        Map<String,Object> result = new LinkedHashMap<>();\n        if (value == null) { result.put("present", false); return result; }\n        result.put("class", value.getClass().getName());\n        for (Class<?> type = value.getClass(); type != Object.class; type = type.getSuperclass()) {\n            for (Field field : type.getDeclaredFields()) {\n                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;\n                field.setAccessible(true);\n                Object observed = field.get(value);\n                String name = type.getSimpleName() + "." + field.getName();\n                if (observed == null || observed instanceof Number || observed instanceof Boolean || observed instanceof String) result.put(name, observed);\n                else if (observed instanceof Enum<?> || observed instanceof Throwable) result.put(name, observed.toString());\n                else result.put(name + "Class", observed.getClass().getName());\n            }\n        }\n        return result;\n    }\n\n'
source = source[:position] + helper + source[position:]
anchor = '        if (!exactIdentityCoverage(nativePuts.keySet(), nativeReturned)'
assert source.count(anchor) == 1
extra = '        Object originalPublication = read(read(session, "lineage"), "publication");\n        row.put("diagnostic_finalPublication", diagnosticState(originalPublication));\n        List<Object> producers = new ArrayList<>(); row.put("diagnostic_producers", producers);\n        for (Object producer : (List<?>)read(originalPublication, "producers")) {\n            Map<String,Object> item = diagnosticState(producer); producers.add(item);\n            if (producer.getClass().getSimpleName().equals("PassiveProducer")) {\n                Object passivePrepared = read(producer, "consumerPrepared");\n                item.put("preparedState", diagnosticState(passivePrepared));\n                item.put("quotedPlanIdentityMatches", nativePuts.containsKey(read(producer, "plan")));\n                item.put("originalConsumerState", diagnosticState(read(producer, "originalConsumer")));\n                item.put("acceptedState", diagnosticState(read(passivePrepared, "originalSequence")));\n                item.put("commitState", diagnosticState(read(producer, "consumerCommit")));\n            }\n        }\n'
source = source.replace(anchor, extra + anchor)
observer = WORK / 'tests/native-passive-root-completion-diagnostic2-a08a7373/PositiveWeapon436.java'
launcher = WORK / 'tools/run_native_passive_root_completion_diagnostic2_a08a7373.py'
preparation = WORK / 'reports/native-passive-root-completion-diagnostic2-preparation-a08a7373.json'
text = Path(parent['newLauncher']).read_text(encoding='utf-8')
text = text.replace(Path(parent['newObserver']).relative_to(WORK).as_posix(), observer.relative_to(WORK).as_posix())
text = text.replace(parent_path.relative_to(WORK).as_posix(), preparation.relative_to(WORK).as_posix())
text = text.replace('reports/native-repair-runtime-passive-root-completion-diagnostic-a08a7373.json','reports/native-repair-runtime-passive-root-completion-diagnostic2-a08a7373.json')
helper_path = WORK / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper_path)}
exec(compile(helper_path.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper_path), 'exec'),namespace)
namespace['add'](observer.relative_to(ROOT).as_posix(),source)
namespace['add'](launcher.relative_to(ROOT).as_posix(),text)
paths = [Path(__file__),parent_path,Path(parent['newObserver']),Path(parent['newLauncher']),observer,launcher,helper_path]
with preparation.open('x',encoding='utf-8') as stream:
    json.dump({'newObserver':str(observer),'newLauncher':str(launcher),'readOnlyDiagnostic':True,'fullOriginalSessionAssertionsRetained':True,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'producerAndConsumerStateReadOnly':True}))
