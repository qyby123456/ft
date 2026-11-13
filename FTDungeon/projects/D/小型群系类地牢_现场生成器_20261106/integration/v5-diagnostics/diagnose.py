from collections import Counter
from pathlib import Path
import sys

sys.dont_write_bytecode = True
HOME = Path(__file__).resolve().parent
sys.path.insert(0, str(HOME.parent))
import integrate
import verify_saved_world as base


def main():
    prior = HOME.parent / 'v4-unified'
    ready = integrate.load(prior / 'ready.json')
    for relative, expected in ready['files'].items():
        if integrate.digest(integrate.contained(prior, relative)) != expected:
            raise integrate.GateError('V4 frozen evidence changed: ' + relative)
    final = integrate.load(prior / 'final-evidence.json')
    snapshot = Path(final['snapshot'])
    structure_path = snapshot / 'catalog/candidate-resources/data/biomedungeon/structures/s8_07.nbt'
    reader = base.load_reader(HOME.parent / 'staging/20261106_132015_129205')
    structure = reader.Structure.read(structure_path)
    source_hash = integrate.digest(structure_path)
    matrix = integrate.load(Path(final['isolatedFullMatrix']['report']))
    failed = [entry for entry in matrix['bands'] if not (entry['forward'] and entry['returned'])]
    source_cells = []
    for coordinate_z in range(8):
        for coordinate_y in range(1,9):
            for coordinate_x in range(10,15):
                state = structure.blocks[(coordinate_x, coordinate_y, coordinate_z)]
                source_cells.append(dict(local=[coordinate_x, coordinate_y, coordinate_z], name=state.name, properties=dict(state.properties)))
    bands = integrate.load(snapshot / 'runtime/src/main/resources/data/ftbd_candidate/s8_07-band.json')
    metadata = integrate.load(snapshot / 'runtime/src/main/resources/data/ftbd_candidate/expansion.json')
    room = next(item for item in metadata['templates'] if item['id'] == 's8_07')
    north = next(port for port in room['ports'] if port['facing'] == 'north')
    sample = next(entry for entry in failed if entry['caseId'] == 's8_07_r0')
    original_start = sample['originalContinuousEndpoints'][0]
    actual_start = sample['actualStart']
    origin = [round(value-offset, 6) for value, offset in zip(actual_start, original_start)]
    native_shapes = [dict(**entry, sourceLocal=[round(value-offset,6) for value,offset in zip(entry['position'],origin)]) for entry in sample['blockingCells']]
    diagnosis = dict(status='SOURCE_INTERIOR_NARROWING_CONFIRMED_NOT_EXTERNAL_CONNECTOR_DEFECT', exactV4JarSha256=final['jarSha256'], sourceSha256=source_hash, v4ReadySha256=integrate.digest(prior/'ready.json'), v4FilesVerified=len(ready['files']), originalNorthPort=north, originalBandScope=bands['scope'], originalBandNorthDepths=sorted(set(column['depth'] for column in bands['columns'] if column['facing']=='north')), failedSampleCount=len(failed), perRotation=dict(Counter(entry['caseId'] for entry in failed)), sampledWidth=dict(originalBlockSpanX=[11,14], requiredBodyCentersX=[11.3,13.7], sourceFeetYAtBoundary=2, sourceFeetYAtInnerLanding=4, sampledZ=[0.3,5.7], requiredHeadroom=3, lateralSpacing=0.05, movementSubdivision=0.025), actualOriginalDoorway=dict(sourceZ=4, sourceDoorBlocksX=[11,12], sourceCalciteJambX=[10,13], lowerY=4, upperY=5, airAboveY=6, calciteLintelY=7, rectangularOpeningWithoutDoorThickness=dict(x=[11,13], y=[4,7], width=2, height=3), openedNativeDoorSlabIntervalsX=[[11,11.1875],[12.8125,13]], physicalClearGapX=[11.1875,12.8125], physicalClearWidth=1.625, legalWidth06BodyCenterX=[11.4875,12.5125], fullDeclaredWidth3Clearance=False, threeBlockVerticalClearanceInSurvivingGap=True), exactCause='At original z4 the three-block declared north strip x11..13 narrows to a two-block interior doorway x11..12, then to1.625 blocks between original already-open door collision slabs. Original calcite x13 blocks the right part outright. Lintel y7 leaves exactly3 blocks above landing y4 where opening exists; height alone is not the principal failure. Full width3 AND height3 cannot both hold across the original declared band.', withinRequiredBand=dict(northCapturedDepths=list(range(8)), testedDepths=[0.3,5.7], doorDepth=4, verdict=True, note='The failure is after the two original stair rows z2/z3 but within the source-bound north transition band captured at depths0..7, not an unrelated distant hall.'), exteriorOnlyRepair=dict(possibleForFullWidthRequirement=False, reason='An exterior connector lies outside source z0 plane and cannot alter interior x13 calcite or opened door slabs at z4. No exterior-only widening, Y alignment, seam cap removal or route detour makes the original interior continuously3-wide. A detour may preserve point-to-point reachability but cannot satisfy this full-width band contract.', prohibitedWorkarounds=['Do not narrow declared band, lower width/headroom or omit failing lanes to mark PASS', 'Do not delete source targets, shift their meaning silently or remove doors/stairs/calcite', 'Do not flatten the original stair transition or carve interior source air']), nativeBlockingEvidence=native_shapes, failingLanes=failed, sourceBandCells=source_cells, requestedOwnerInputs=dict(catalog=['Explicit source-hash-bound interpretation of north width3: boundary-plane only or guaranteed entire depth0..7 transition-band width; any contract revision must stay visibly NOT full-width3, preserve original targets and receive upstream authorization', 'Retain all148 original targets and2200 directed/return checks; specify block-index feet anchors versus literal continuous coordinates explicitly', 'Keep S8-07 sourceSha256 and native open-door collision evidence; accepted flags remain false while full-width guarantee unresolved'], runtime=['Exact frozen replacement ready/JAR/source inventory if changing policy; preserve original room bytes and all target IDs', 'Boundary-only contract, if authorized, must encode source-bound variable-width native transition profile and distinguish it from full-width3 passage; fail closed on source/profile/operation mismatch', 'Keep exterior corridor seams outside original room; do not solve acceptance by carving original doorway', 'Provide budget-aware plan search or explicit safe-budget refusal diagnostics, retaining seed and plan for width277 rejection']), independentRetest=['Full original target matrix and four actual nine-room layouts on exact replacement JAR', 'Native swept body0.6x1.8 step0.6 with three-block headroom and documented width profile; retain this V4 full-width failure', 'Ordinary installed partial/completed native recovery and new-version corruption/forcekill/foreign-state protections'])
    integrate.save(HOME / 's8_07-root-cause.json', diagnosis)
    coverage = []
    for turn, entry in final['actualInstalledCases'].items():
        interactions = entry['interactions']
        coverage.append(dict(rootQuarterTurns=int(turn), actualRoomRotations=integrate.load(Path(entry['offlineReadback']))['rooms'], installedProcesses=entry['installed']['processes'], local=entry['independentLocal'], globalReturn=entry['independentGlobal'], nativeBE=entry['savedCounts']['nativeBlockEntities'], preservedRawNativeFields=entry['savedCounts']['rawNativeFieldsCompared'], preservedDefaultNativeFields=entry['savedCounts']['defaultNativeFieldsCompared'], doorUses=sum('toggled' in item for item in interactions), containerMenus=sum('nativeMenuOpened' in item for item in interactions), legalApproachFailures=[item for item in interactions if not item.get('approachCollisionFree')], noHumanNetworkUiClaim=True))
    audit = dict(status='V4_EXACT_EVIDENCE_COVERAGE_VERIFIED_WITH_SCOPE_LIMITS', v4ReadySha256=integrate.digest(prior/'ready.json'), verifiedFiles=len(ready['files']), exactJarSha256=final['jarSha256'], installedFourRootRotations=coverage, fullIsolated36Scenes=final['isolatedFullMatrix'], completedNativeRestart=final['completedOriginalNativeRestart'], coordinateSemantics=final['endpointSemantics'], cautions=['Four actual root orientations do not themselves cover every template rotation; separate36-scenes matrix supplies that coverage', 'Global148 target roundtrips per layout compose original local paths and8 actual corridor bridges and independently move through full concatenated routes; body only set initially, not at internal seams', '2200 matrix replays use owner-proposed native paths but independently execute them with smaller subdivisions and frozen endpoint binding; not an independent search completeness proof', '148 source targets per rotation are preserved;179 unique endpoint IDs includes ports/start/targets in one actual layout', 'Physical block-center convention is explicitly reviewed;9 literal-coordinate endpoints in q0 are illegal; no literal-coordinate pass', '16 door toggles/restores and44 native container menus across four actual layouts; banners not interactive containers; no human client/network UI or inventory transfer certification', 'S8-01 sampled49 lanes per rotation passed; S8-07 north28/49 lanes fail per rotation, south89 lanes pass; continuous real-valued full-width clearance not established', 'All V4 world jobs are PAYLOAD_VERIFIED_ROUTE_INTERACTION_ACCEPTANCE_PENDING, non-COMMITTED; no production promotion', 'V3 fault evidence not inherited by this JAR'])
    integrate.save(HOME / 'v4-coverage-audit.json', audit)
    integrate.save(HOME / 'remaining-gates.json', dict(completeGeneratorReady=False, productionPromotion=False, blocking=[dict(id='S8_07_FULL_WIDTH_CONTRACT_UNSATISFIED', state='FAIL_SOURCE_ORIGINAL_DOORWAY', evidence='s8_07-root-cause.json'),dict(id='LITERAL_VS_BLOCK_INDEX_ENDPOINT_SEMANTICS',state='EXPLICIT_CONTRACT_REQUIRED_NOT_LITERAL_PASS',measured=final['endpointSemantics']),dict(id='HUMAN_CLIENT_INTERACTIONS',state='NOT_RUN_NATIVE_FAKEPLAYER_ONLY'),dict(id='EXACT_V4_FAULT_PROTECTION',state='RUNNING_SEE_FAULT_PROGRESS_NOT_V3'),dict(id='CONTINUOUS_FULL_WIDTH_CLEARANCE',state='SAMPLED_ONLY_S8_07_FAILURES_RETAINED'),dict(id='PRODUCTION_RELEASE',state='NOT_AUTHORIZED_ACCEPTED_FALSE_NON_COMMITTED')], passedScopeOnly=['Ordinary installed nine-original command jobs at four root orientations', '2200 frozen directed/return matrix checks over36 original scenes under explicit rotated block-center convention', '148 original target global roundtrips with8 actual corridor bridges per installed layout', 'Native source/default BE fields, force-ticked fluid observations and enclosure snapshots', 'Partial and completed ordinary-JVM restart with explicit revalidation']))
    print('V4 seal1484 PASS; original S8-07 interior width2 / native gap1.625, required3;112 failing lanes retained')


if __name__ == '__main__':
    main()
