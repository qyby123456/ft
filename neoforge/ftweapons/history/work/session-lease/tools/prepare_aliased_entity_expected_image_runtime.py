import hashlib
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
helper_namespace={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('probe=(WORK/')[0],helper_namespace)
parent_path=WORK/'reports/native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json'
parent=load(parent_path)
assert digest(parent_path)=='23f7a20eae05b48826098112dcee71f67e3c61e51f03ba9c0f897866970f49bc'
candidate_path=WORK/'reports/native-repair-aliased-entity-expected-image-candidate.json'
candidate=load(candidate_path);build=Path(candidate['attempt'])
assert build.name=='native-repair-5f283607'
assert load(build/'boundary-proof-terminal.json')['exitCode']==0 and load(build/'asm.json')['failures']==0
boundary=(build/'boundary-proof.log').read_text(encoding='utf-8')
assert 'expectedImageMethodsUpdated=8' in boundary and 'pureExpectedAliasCases=18' in boundary
preparation=load(WORK/'reports/aliased-entity-expected-image-preparation-20261112.json')
row=next(row for row in preparation['sources'] if row['proposal'].endswith('OwnerFieldExpectedAuthority.java'))
before=Path(row['beforeSource']).read_text(encoding='utf-8');after=Path(row['proposal']).read_text(encoding='utf-8')
old='new State(var11 == null ? var5 : var11, var2.expected, var3 == null ? FoodWeaponExpectedTrace.emptyCompound() : var3.expected)'
new='new State(var11 == null ? var5 : var11, var2.expected, var3 == null ? FoodWeaponExpectedTrace.emptyCompound() : var3.expected, var3 == var2)'
assert before.count(old)==1 and before.replace(old,new)==after
original=WORK/'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java'
observed=WORK/'tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java'
addition='                        Object earlyFailure = read(session, "firstObservationFailure");\n                        publicationRow.put("earlyFirstObservationFailure", earlyFailure == null ? null : earlyFailure.toString());\n'
assert observed.read_text(encoding='utf-8').replace(addition,'')==original.read_text(encoding='utf-8')
watch=(WORK/'tests/native-domain-value-diff-watch-8199dcc2/NativeDomainValueDiffWatch.java').read_text(encoding='utf-8')
watch=watch.replace('NativeDomainValueDiffWatch','NativeAliasedDomainValueDiffWatch')
watch=helper_namespace['replace'](watch,'    private static String differenceReport="null";','    private static String differenceReport="null";\n    private static String aliasReport="null";')
marker='        domain=actual;'
replacement=marker+'''
        var carrier=field(prepared,"carrier");var actualRoot=field(domain,"originalPersistent");
        var carrierRoot=carrier instanceof ObjectReference object?field(object,"originalPersistent"):null;
        var builder=(ObjectReference)field(prepared,"builder");var planned=(ObjectReference)field(builder,"expected");
        aliasReport="{\\"readOnly\\":true,\\"methodInvocations\\":false,\\"authorityWrites\\":false,\\"sameDomainBefore\\":"+domain.equals(carrier)+",\\"sameNonNullPersistentRoot\\":"+(actualRoot!=null && actualRoot.equals(carrierRoot))+",\\"plannedAliasFact\\":"+value(field(planned,"playerCarrierAliased"))+"}";
        System.out.println("EXACT_DOMAIN_ALIAS_AT_ARM "+aliasReport);System.out.flush();'''
watch=helper_namespace['replace'](watch,marker,replacement)
watch=helper_namespace['replace'](watch,'        Files.writeString(Path.of(arguments[1]).resolve("domain-value-diff.json"),differenceReport);','        Files.writeString(Path.of(arguments[1]).resolve("domain-alias-at-arm.json"),aliasReport);\n        Files.writeString(Path.of(arguments[1]).resolve("domain-value-diff.json"),differenceReport);')
watch_source=WORK/'tests/native-aliased-domain-value-diff-watch-5f283607/NativeAliasedDomainValueDiffWatch.java'
helper_namespace['create'](watch_source,watch)
for key,value in parent.items():
    if key not in candidate:candidate[key]=value
for key in ['requiredProductionResources','additionalProductionResources']:
    for path in parent[key].values():candidate['inputHashes'][path]=parent['inputHashes'][path]
for path in [Path(__file__),parent_path,watch_source,original,observed,WORK/'tests/certified-ranged-timer-frame-foreign-guard/NativeExpectedImageReadGuardProbe.java',WORK/'tests/AliasedEntityExpectedImageProof.java',WORK/'tools/verify_aliased_entity_expected_image.py',WORK/'reports/aliased-entity-expected-image-preparation-20261112.json',build/'boundary-proof.log',build/'boundary-proof-terminal.json',WORK/'reports/native-repair-runtime-domain-value-diff-watch-8199dcc2.json']:
    candidate['inputHashes'][str(path)]=digest(path)
for path,wanted in candidate['inputHashes'].items():assert digest(path)==wanted,path
candidate.update(boundaryProof=boundary,aliasedExpectedImageBoundaryVerified=True,actualPreparedDomainIdentityOnly=True,ownerAdvanceAndAllComparisonGatesUnchanged=True,originalFullNineteenAssertionsPreserved=True,positiveSignedSessionAccepted=False,fullSignedSessionAccepted=False,runtimeAccepted=False,installable=False,deployed=False)
frozen=WORK/'reports/native-repair-aliased-entity-expected-image-candidate-5f283607.json'
with frozen.open('x',encoding='utf-8') as stream:json.dump(candidate,stream,ensure_ascii=False,indent=2)
runner=(WORK/'tools/run_native_domain_value_diff_watch_8199dcc2.py').read_text(encoding='utf-8')
for old,new in [('native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json',frozen.name),('23f7a20eae05b48826098112dcee71f67e3c61e51f03ba9c0f897866970f49bc',digest(frozen)),('tests/native-domain-value-diff-watch-8199dcc2/NativeDomainValueDiffWatch.java','tests/native-aliased-domain-value-diff-watch-5f283607/NativeAliasedDomainValueDiffWatch.java'),("'NativeDomainValueDiffWatch', str(diagnostic_port)","'NativeAliasedDomainValueDiffWatch', str(diagnostic_port)"),('native-repair-runtime-domain-value-diff-watch-8199dcc2.json','native-repair-runtime-aliased-entity-expected-image-5f283607.json')]:runner=helper_namespace['replace'](runner,old,new)
runner=helper_namespace['replace'](runner,"'exactDomainValueDifference': load(attempt / 'domain-value-diff.json'),","'exactDomainValueDifference': load(attempt / 'domain-value-diff.json'), 'actualDomainAlias': load(attempt / 'domain-alias-at-arm.json'),")
runner=helper_namespace['replace'](runner,"watch_probe.get('firstExactRetirementCaptured')","watch_probe.get('armed')")
helper_namespace['create'](WORK/'tools/run_native_aliased_entity_expected_image_5f283607.py',runner)
print(json.dumps({'frozenCandidate':str(frozen),'sha256':digest(frozen),'inputCount':len(candidate['inputHashes']),'originalFullNineteenAssertionsPreserved':True,'installable':False,'deployed':False}),flush=True)
