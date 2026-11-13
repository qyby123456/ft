from collections import Counter
import gzip
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
import verify_saved_world as base
import readback

HOME = Path(__file__).resolve().parent


def report(progress, turn):
    entry = progress['cases'][str(turn)]
    if entry['status'] != 'EXECUTED':
        raise integrate.GateError('Native case not executed: ' + str(entry))
    data = integrate.load(Path(entry['report']))
    if integrate.digest(Path(entry['report'])) != entry['reportSha256']:
        raise integrate.GateError('Native report changed')
    return entry, data


def main():
    snapshot = Path(integrate.load(HOME / 'progress.json')['snapshot'])
    installed = integrate.load(HOME / 'installed-progress.json')
    installed_retry = integrate.load(HOME / 'installed-q3-retry-progress.json')
    local = integrate.load(HOME / 'native-final-progress.json')
    global_cases = integrate.load(HOME / 'global-progress.json')
    global_retry = integrate.load(HOME / 'global-q3-progress.json')
    matrix = integrate.load(HOME / 'matrix-final-progress.json')
    completed_recovery = integrate.load(HOME / 'completed-recovery.json')
    semantics_progress = integrate.load(HOME / 'endpoint-semantics-progress.json')
    semantics_entry, semantics = report(semantics_progress, 0)
    if completed_recovery['status'] != 'ACTUAL_INSTALLED_COMPLETED_ORIGINAL_NATIVE_REVALIDATION_PASS':
        raise integrate.GateError('Completed native original-payload restart not verified')
    if local['status'] != 'SCOPED_NATIVE_PROBES_EXECUTED':
        raise integrate.GateError('Final independent native/interaction runs not complete')
    reader = base.load_reader(integrate.HOME / 'staging/20261106_132015_129205')
    cases = {}
    unexpected_dynamic = []
    interaction_failures = []
    for turn in range(4):
        source = installed_retry if turn == 3 else installed
        run = Path(source['root']) / ('q' + str(turn))
        evidence = source['cases'][str(turn)]
        if evidence['status'] == 'FAIL':
            raise integrate.GateError('Selected installed case failed')
        offline = readback.inspect(snapshot, run / 'world')
        integrate.save(HOME / ('final-readback-q' + str(turn) + '.json'), offline)
        native_entry, native = report(local, turn)
        global_entry, returned = report(global_retry if turn == 3 else global_cases, turn)
        changes = native['nativeChanges']
        rejected_changes = [item for item in changes if not item['approvedByIndependentNativeCheck']]
        unexpected_dynamic.extend(rejected_changes)
        interactions = native['interactions']
        bad_use = [item for item in interactions if item.get('error') or not item.get('approachCollisionFree') or ('toggled' in item and not (item['toggled'] and item['restored'])) or ('nativeMenuOpened' in item and not item['nativeMenuOpened'])]
        interaction_failures.extend(bad_use)
        journal = next((run / 'world/ft_biome_dungeon_candidate_v4/minecraft/overworld').glob('*.nbt'))
        record = base.decode(reader, gzip.decompress(journal.read_bytes()[72:]))
        observations = json.loads(record['observations'])
        if not observations or any(not item['observation']['allTicking'] or item['observation']['escapedFluid'] or item['observation']['fires'] or item['observation']['foreignBlockChanges'] for item in observations):
            raise integrate.GateError('Persisted native force-tick observation evidence failed')
        cases[str(turn)] = dict(installed=evidence, offlineReadback=str(HOME / ('final-readback-q' + str(turn) + '.json')), savedCounts=offline['counts'], rawStateDifferences=len(offline['originalStateDifferences']), rawNativeFieldDifferences=offline['originalNativeFieldDifferences'], enclosureErrors=offline['enclosureErrors'], escapedFluidOrFire=offline['escapedFluidOrFire'], independentLocal=dict(attempted=native['attempted'], passed=native['passed'], report=native_entry['report'], reportSha256=native_entry['reportSha256']), independentGlobal=dict(attempted=returned['attempted'], passed=returned['passed'], bridgesPassed=returned['bridgesPassed'], report=global_entry['report'], reportSha256=global_entry['reportSha256']), nativeChanges=dict(count=len(changes), classifications=dict(Counter('native-neighbor-shape' if item['nativeShapeNormalization'] else 'bounded-surviving-vine' if item['boundedSurvivingNativeVine'] else 'UNAPPROVED' for item in changes)), unapproved=rejected_changes), interactions=interactions, phase=record['phase'], stage=record['stage'], restartValidations=record.get('restartValidations'), nativeObservationSamples=len(observations), savedObservedTicks=record.get('observedTicks'), nativeForceTickObservations=observations)
    matrix_entry, all_routes = report(matrix, 0)
    failures = [item for item in all_routes['bands'] if not (item['forward'] and item['returned'])]
    precise = []
    for failure in failures:
        identifier = failure['caseId'][:5]
        room_case = next(item for item in integrate.load(snapshot / 'catalog/rotated-route-inputs.json')['cases'] if item['caseId'] == failure['caseId'])
        precise.append(dict(**failure, sourceOriginalStart=room_case['sourceStart'], preservedOriginalTargets=room_case['targets'], meaning='Sampled continuous declared-width straight longitudinal strip fails in original interior doorway, not a proof no alternate bent route exists. Original target routes are retained and pass. No source block/target edits allowed.'))
    issue = dict(id='S8_07_FULL_WIDTH_INTERIOR_DOORWAY_NOT_CERTIFIED', severity='ACCEPTANCE_BLOCKER_NOT_RUNTIME_CORRUPTION', sourceSha256='ad71c099fe5f87345f34d659655075a4ca0025952119b96247f88ff527a76bd7', originalStrip=dict(x=[11.3,13.7], start=[None,2,0.3], end=[None,4,5.7], bodyWidth=0.6, bodyHeight=1.8, nativeStep=0.6, requiredHeadroom=3), failedSampleCount=len(failures), failures=precise, scope='Four original rotations; 0.05 transverse lanes and 0.025 native movement subdivision; literal continuous coordinates, no half-block shift; source original doorway kept intact. This is a sampled-width result, not an analytic continuous-width certificate.')
    budget = integrate.load(HOME / 'budget-rejected-plan.json')
    issues = dict(completeGeneratorReady=False, productionPromotion=False, blockers=[issue] if failures else [], dynamicReviewUnexpected=unexpected_dynamic, interactionFailures=interaction_failures, boundedBudgetRefusal=dict(command='ftbiomedungeon_candidate plan 1000 200 1000 s8 9 3', originalFailure=installed['cases']['3'], worldSeed=integrate.load(HOME / 'budget-rejected-seed.json')['worldSeed'], computedEnvelope=budget, interpretation='Safe runtime budget rejection before journal/build; width 277 including shell exceeds interior span limit256. Core geometry success does not guarantee runtime envelope budget. Seed0 retry separately evidenced; rejected trial retained.'), coordinateSemantics=dict(frozenTargets=148, frozenMatrix=2200, deletedOrModifiedTargets=0, physicalConvention='Original block-index endpoint [x,y,z] interpreted as feet at local block-center [x+0.5,y,z+0.5], then rigidly rotated and translated; not adding positive world0.5 after rotation', reason='Voxel rotation R maps the whole unit block; its center is R(local+[0.5,0,0.5]). Literal source block-index corners are a different body placement.', verdict='EXPLICIT_CONVENTION_NATIVE_PASS_NOT_SILENT_LITERAL_COORDINATE_ACCEPTANCE', continuousBandChecks='Continuous source coordinates are used literally with no half offset'), remaining=['No real human client/network UI test; native FakePlayer use/menu opening only', 'Transverse strip sampling does not prove clearance for every real-valued center across a width', 'S8-07 full interior width remains blocked by original doorway unless acceptance contract is clarified without removing original targets or changing source', 'No power-loss claim; V3 forced-kill evidence belongs to older exact V2 runtime only', 'No production promotion; PAYLOAD_VERIFIED remains non-COMMITTED'])
    integrate.save(HOME / 'ISSUES.json', issues)
    issues['coordinateSemantics']['nativeEndpointProbe'] = dict(attempted=semantics['attempted'], blockCentersLegal=semantics['passed'], literalCoordinatesLegal=semantics['literalLegal'], report=semantics_entry['report'], reportSha256=semantics_entry['reportSha256'])
    issues['coordinateSemantics']['literalEndpointFailures'] = [item for item in semantics['endpoints'] if not item['literalOriginalCoordinates']['legalStanding']]
    integrate.save(HOME / 'ISSUES.json', issues)
    for relative, expected in integrate.load(integrate.HOME / 'v3-faults/ready.json')['files'].items():
        if integrate.digest(integrate.contained(integrate.HOME / 'v3-faults', relative)) != expected:
            raise integrate.GateError('V3 sealed evidence changed')
    v1 = integrate.load(integrate.HOME / 'v2/v1-seal.json')
    if integrate.digest(integrate.HOME / 'ready.json') != v1['readySha256']:
        raise integrate.GateError('V1 ready changed')
    for relative, expected in v1['files'].items():
        if integrate.digest(integrate.contained(integrate.HOME, relative)) != expected:
            raise integrate.GateError('V1 file changed')
    v2 = integrate.check_component(integrate.HOME / 'v2', 'integration-v2')
    result = dict(status='INDEPENDENT_SCOPED_EVIDENCE_COMPLETE_FULL_ACCEPTANCE_BLOCKED' if failures or unexpected_dynamic or interaction_failures else 'INDEPENDENT_SCOPED_PASS_NO_PRODUCTION_PROMOTION', completeGeneratorReady=False, productionPromotion=False, jarSha256=integrate.load(snapshot / 'runtime/ready.json')['jarSha256'], snapshot=str(snapshot), snapshotLockSha256=integrate.digest(snapshot / 'snapshot-lock.json'), actualInstalledCases=cases, isolatedFullMatrix=dict(attempted=all_routes['attempted'], independentlyReplayed=all_routes['passed'], originalTargetsPerRotation=148, originalTargetsRemoved=0, scenes=36, report=matrix_entry['report'], reportSha256=matrix_entry['reportSha256'], allSourceLavaForceTicking=all_routes['allSourceLavaForceTicking'], nativeElapsedTicks=all_routes['nativeElapsedTicks'], escapedFluidOrFire=all_routes['escapedFluidOrFire'], fullWidthSampledLanes=len(all_routes['bands']), fullWidthFailedLanes=len(failures)), seals=dict(v1Files=len(v1['files']),v2Files=len(v2['files']),v3Files=len(integrate.load(integrate.HOME / 'v3-faults/ready.json')['files'])), issues='ISSUES.json')
    result['completedOriginalNativeRestart'] = completed_recovery
    result['endpointSemantics'] = issues['coordinateSemantics']['nativeEndpointProbe']
    integrate.save(HOME / 'final-evidence.json', result)
    progress = integrate.load(HOME / 'progress.json')
    progress.update(status=result['status'], finalEvidence='final-evidence.json', issues='ISSUES.json', completeGeneratorReady=False)
    integrate.save(HOME / 'progress.json', progress)
    files = {}
    for path in HOME.rglob('*'):
        if not path.is_file() or path.name == 'ready.json' and path.parent == HOME:
            continue
        relative = path.relative_to(HOME)
        if any(part in ('libraries','.gradle','gradle-cache','project','__pycache__') for part in relative.parts):
            continue
        if path.suffix in ('.py','.java','.gradle','.json','.md','.log','.nbt','.mca','.jar','.ps1','.properties') or path.name == 'level.dat':
            files[str(relative)] = integrate.digest(path)
    integrate.save(HOME / 'ready.json', dict(component='integration-v4-unified',apiVersion=1,ready=True,status=result['status'],completeGeneratorReady=False,productionPromotion=False,files=files))
    print(result['status'], 'files=',len(files),'widthFailures=',len(failures),'dynamicUnexpected=',len(unexpected_dynamic))


if __name__ == '__main__':
    main()
