import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-material-ranged-crossbow-signed-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
melee=work/'reports/native-melee-paid-signed-combat-focused-evidence-eef72352.json';accepted=load(melee)
old_source=work/'tests/paid-melee-ingress-eef72352/PositiveWeapon436.java';assert digest(old_source)==accepted['inputHashes'][str(old_source)]
paid=work/'reports/native-repair-runtime-exact-melee-variants-paid-repair-pair-qualified-41d445cf.json';result=load(paid);terminal=Path(result['attempt'])/'terminal308.json';end=load(terminal)
assert result['exitCode']==0 and result['probe']['failures']==0 and end['stopSent'] and end['completeSave']
audit=work/'reports/native-remaining-variant-gap-audit-3320e085.json';rows=load(audit)['groups']['otherExactOriginalClassesStillNeedActualMatrix'];ids=[row['id'] for row in rows]
assert len(ids)==19 and [row['id'] for row in result['probe']['cases']]==ids
assert all(row['sameEpochGenerationZeroToOne'] and row['actualSuccessorOriginalSaveCommitted'] for row in result['probe']['cases'])
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
source=old_source.read_text(encoding='utf-8').replace('for (int sample : new int[]{0, 3}) {','for (int sample = 0; sample < 19; sample++) {')
source=source.replace('new UUID(620, sample + 1)','new UUID(690, sample + 1)').replace('"ft_six_" + sample','"ft_exact_" + sample')
source=source.replace('!= 3L','!= 2L').replace('generation-three','generation-two')
anchor='                Map<String, Object> row = new LinkedHashMap<>(); row.put("id", id); cases.add(row);';assert source.count(anchor)==1
source=source.replace(anchor,anchor+'\n                if (!List.of('+', '.join(json.dumps(value) for value in ids)+').get(sample).equals(id)) throw new AssertionError("Exact registered variant changed on original reload");')
anchor='                        publicationRow.put("traceComplete", complete.invoke(session));';assert source.count(anchor)==1
source=source.replace(anchor,anchor+"""
                        for (String flag : List.of("traceRevoked", "coverageInstalled", "next")) publicationRow.put("session_" + flag, read(session, flag));
                        Object failure = read(session, "firstObservationFailure");
                        publicationRow.put("firstObservationFailure", failure == null ? null : failure.toString());
                        if (!Boolean.TRUE.equals(publicationRow.get("traceComplete"))
                            || !Boolean.TRUE.equals(publicationRow.get("session_coverageInstalled"))
                            || Boolean.TRUE.equals(publicationRow.get("session_traceRevoked"))
                            || ((Number)publicationRow.get("session_next")).intValue() != 4 || failure != null)
                            throw new AssertionError("Original paid melee Session incomplete or revoked for " + id);
""")
source=source.replace('            report.put("failures", 0);','            report.put("nineteenExactOriginalMeleeVariantsGenerationTwoSignedSessions", true);\n            report.put("failures", 0);')
new_source=work/'tests/exact-melee-variants-signed-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
old_launcher=Path(frozen['newLauncher']);launcher=old_launcher.read_text(encoding='utf-8')
launcher=launcher.replace(Path(frozen['newObserver']).relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('reports/native-repair-runtime-material-ranged-paid-repair-pair-qualified-e43eb814.json',paid.relative_to(work).as_posix())
assert launcher.count("assert len(seed_result['probe']['cases']) == 12")==1
launcher=launcher.replace("assert len(seed_result['probe']['cases']) == 12","assert len(seed_result['probe']['cases']) == 19")
launcher=launcher.replace('native-repair-runtime-material-ranged-crossbow-signed-41d445cf.json','native-repair-runtime-exact-melee-variants-signed-41d445cf.json')
preparation=work/'reports/native-exact-melee-variants-signed-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_variants_signed_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,old_launcher,old_source,melee,paid,terminal,audit,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':ids,'newObserver':str(new_source),'newLauncher':str(new_launcher),'sameProduct':'41d445cf','genuineGeneration':2,'noProductOrGameplayValueChanges':True,'authorityFieldsWritten':False,'authenticatedMultiplayerAccepted':False,'installable':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualSignedMeleeCases':19,'requiresActualPaidGeneration':2}))
