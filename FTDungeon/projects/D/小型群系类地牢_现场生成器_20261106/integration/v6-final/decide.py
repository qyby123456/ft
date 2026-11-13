import gzip
import json
import shutil
from pathlib import Path
import sys

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
import integrate
import verify_saved_world as base

GATES=('ORIGINAL_HASH_METADATA','INSTALLED_EXACT_ARTIFACT','SAVED_NATIVE_READBACK_RESTART','ALL_ORIGINAL_TARGETS_FOUR_ROTATIONS_RETURN','ACTUAL_GLOBAL_ENTRANCE_RETURN','FULL_DECLARED_WIDTH_CLEARANCE','NATIVE_STEP_DOOR_CONTAINER_INTERACTIONS','EXACT_SOURCE_FLUID_TICKS_RESTART','RUNTIME_PROTECTION_CONFLICT_RECOVERY','NO_UNRESOLVED_BLOCKERS')


def require(condition,message):
    if not condition:raise integrate.GateError(message)


def reference(path):
    return dict(path=str(path),sha256=integrate.digest(path))


def sealed(root,jar):
    ready=integrate.load(root/'ready.json');files=integrate.inventory(ready,allow_empty=True)
    if 'inventorySha256' in ready:
        inventory_path=integrate.contained(root,ready.get('inventory','SHA256SUMS.json'))
        require(integrate.digest(inventory_path)==ready['inventorySha256'],'Independent inventory changed '+str(root))
        declared=integrate.load(inventory_path)
        if isinstance(declared,dict) and declared and all(isinstance(value,dict) and 'sha256' in value for value in declared.values()):
            files|={relative:value['sha256'] for relative,value in declared.items()}
        else:
            files|=integrate.inventory(declared)
    require(bool(files),'No independent sealed inventory '+str(root))
    for relative,expected in files.items():require(integrate.digest(integrate.contained(root,relative))==expected,'Independent frozen evidence changed '+relative)
    require(ready.get('jarSha256',ready.get('runtimeJarSha256'))==jar,'Independent final JAR mismatch '+str(root))
    return dict(root=str(root),ready=reference(root/'ready.json'),filesVerified=len(files),status=ready['status'])


def main():
    frozen=integrate.load(HOME/'progress.json');jar=frozen['jarSha256'];snapshot=Path(frozen['snapshot']);lock=integrate.load(snapshot/'snapshot-lock.json')
    for name,entry in lock['components'].items():
        for relative,expected in entry['files'].items():require(integrate.digest(integrate.contained(snapshot/name,relative))==expected,'Final snapshot changed '+relative)
    require(jar=='bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f' and integrate.digest(Path(frozen['jar']))==jar,'Final exact JAR changed')
    independent={name:sealed(root,jar)for name,root in [('faults',integrate.ROOT/'core/final-runtime-faults'),('commands',integrate.ROOT/'core/final-command-gates'),('boundary',integrate.ROOT/'catalog/final-native-boundary'),('static',integrate.ROOT/'catalog/continuous-native-review')]}
    inputs=[reference(snapshot/'snapshot-lock.json'),reference(HOME/'installed-progress.json')]
    installed=integrate.load(HOME/'installed-progress.json');require(installed['jarSha256']==jar and len(installed['cases'])==4,'Four final installed cases missing')
    reader=base.load_reader(HOME.parent/'staging/20261106_132015_129205');fluid=[]
    for turn in range(4):
        entry=installed['cases'][str(turn)];require(entry['anchor']==[1000,200,1000] and entry['ordinal']==turn and entry['worldSeed']=='267611119455246637','Original anchor/seed regression changed')
        require(entry['status'].startswith('EXACT_INSTALLED_PAYLOAD') and entry['incompleteReceiptRefused'],'Installed final case did not finish')
        journal=Path(installed['root'])/('q'+str(turn))/'world/ft_biome_dungeon_candidate_v5/minecraft/overworld'/(entry['instanceId']+'.nbt');record=base.decode(reader,gzip.decompress(journal.read_bytes()[72:]));observations=json.loads(record['observationHistory'])
        require(record['observedTicks']>=2400 and record['nativeFluidEvents']==0 and record['phase']=='VERIFYING','Final installed natural tick stage mismatch')
        require(len(observations)>=25 and all(value['observation']['allTicking'] and not any(value['observation'][key] for key in ('unsupportedVines','fires','escapedFluid','foreignBlockChanges'))for value in observations),'Native force ticking or containment failed')
        baseline=HOME/'acceptance-baselines'/('q'+str(turn)+'.nbt');baseline.parent.mkdir(exist_ok=True)
        if baseline.exists():require(integrate.digest(baseline)==integrate.digest(journal),'Precommit journal baseline already changed')
        else:shutil.copyfile(journal,baseline)
        fluid.append(dict(ordinal=turn,journal=reference(baseline),originalCanonicalJournalPath=str(journal),observedTicks=record['observedTicks'],forceTickSamples=len(observations),observationHistory=observations))
    matrix_progress=integrate.load(HOME/'matrix-progress.json');require(matrix_progress['jarSha256']==jar,'Matrix final JAR mismatch');matrix_root=Path(matrix_progress['root']);matrix=integrate.load(matrix_root/'q0/world/INDEPENDENT_NATIVE_REPORT.json');shapes=matrix_root/'q0/world/INDEPENDENT_NATIVE_SHAPES.json';certificate=integrate.load(matrix_root/'finite-certificate.json')
    require(matrix['attempted']==matrix['passed']==2248 and matrix['isolatedScenes']==36 and len(matrix['bands'])==748 and all(value['forward'] and value['returned'] for value in matrix['bands']) and not matrix['failures'],'Original matrix or width coverage failed')
    require(certificate['status']=='CONDITIONAL_FINITE_COVERAGE_PASS' and len(certificate['results'])==12 and not any(value['failures']for value in certificate['results']),'Finite exact native-box certificate failed')
    boundary=integrate.load(integrate.ROOT/'catalog/final-native-boundary/native-boundary-report.json')
    require(boundary['status']=='PASS_NATIVE_REPRESENTATIVE_BOUNDARIES_CONTEXT_CHECKED' and boundary['attempted']==boundary['passed']==352 and boundary['directed']==704 and boundary['singletonAtoms']==192 and boundary['openIntervalAtoms']==160 and boundary['contextChecks']>=23784 and boundary['sourceShapeChecks']>=370364,'Native boundary/context evidence incomplete')
    combined=integrate.load(HOME/'combined-progress.json');require(combined['jarSha256']==jar,'Combined final JAR mismatch');combined_root=Path(combined['root']);global_refs=[];use_refs=[];state_refs=[]
    for turn in range(4):
        world=combined_root/('q'+str(turn))/'world';global_path=world/'INDEPENDENT_GLOBAL_REPORT.json';use_path=world/'INDEPENDENT_NATIVE_REPORT.json';state_path=world/'INDEPENDENT_NATIVE_STATE_REVIEW.json';global_result=integrate.load(global_path);uses=integrate.load(use_path);state=integrate.load(state_path)
        require(global_result['attempted']==global_result['passed']==148 and global_result['bridgesPassed']==8 and global_result['localChecksPassed']==global_result['localChecksAttempted']==562 and not global_result['localFailures'],'Global original target/corridor return failed')
        require(uses['attempted']==uses['passed']==15 and all(value['status']=='PASS' for value in uses['interactions']),'Native door/container approach use return failed')
        require(state['unapproved']==0 and state['sourceCells']==127239 and all(value['approved'] for value in state['rawDifferences']),'Unreviewed native original state differences')
        global_refs.append(reference(global_path));use_refs.append(reference(use_path));state_refs.append(reference(state_path))
    coordinates_path=integrate.ROOT/'review/takeover-generator-20261106/coordinate-review.json';coordinates=integrate.load(coordinates_path)
    require(coordinates['status']=='ROTATED_BLOCK_CENTER_CONVENTION_INDEPENDENT_ARITHMETIC_REVIEW_PASS' and coordinates['endpointCount']==4496 and coordinates['routeChecks']==2248,'Coordinate convention not independently closed')
    for entry in coordinates['references']:require(integrate.digest(Path(entry['path']))==entry['sha256'],'Coordinate review input changed')
    side=integrate.load(HOME/'sidegate-progress.json');require(side['jarSha256']==jar and len(side['cases'])==4,'Four sidegate layouts missing');side_refs=[]
    for turn in range(4):
        path=Path(side['root'])/('q'+str(turn))/'world/INDEPENDENT_SIDEGATE_REPORT.json';value=integrate.load(path);require(value['status']=='PASS_DERIVED_SIDE_BRANCH_GEOMETRY_AND_MAIN_ARCH_INDEPENDENCE','Sidegate/main arch independence failed');side_refs.append(reference(path))
    protection=integrate.load(HOME/'protection-progress.json');require(protection['jarSha256']==jar and len(protection['cases'])==6,'Six native event cases missing');protection_refs=[]
    for turn in range(6):
        path=Path(protection['root'])/('q'+str(turn))/'world/INDEPENDENT_NATIVE_REPORT.json';value=integrate.load(path);require(value['status']=='PASS_NATIVE_FORGE_EVENT_QUARANTINE_RELEASE' and value['blockAndNativePayloadUnchanged'],'Native event/ticket cleanup failed');protection_refs.append(reference(path))
    faults=integrate.load(integrate.ROOT/'core/final-runtime-faults/result.json');require(faults['status']=='PASS_ALL_SIX_INDEPENDENT_INSTALLED_FAULT_CASES' and len(faults['passedCases'])==6 and not faults['failedCases'],'Independent installed fault gate failed')
    commands=integrate.ROOT/'core/final-command-gates';pause=integrate.load(commands/'pause-io-progress.json');negative=integrate.load(commands/'receipt-negative-progress.json');restart=integrate.load(commands/'restart-progress.json')
    require(pause['status']=='PASS_NATIVE_INSTALLED_PAUSE_IO_EXCEPTION_CLEANUP','Installed pause persistence cleanup failed');require(negative['status']=='PASS_ALL_INVALID_RECEIPTS_REFUSED' and len(negative['cases'])==6,'Invalid receipt refusal failed');require(restart['status']=='PASS_ALL_FOUR_INSTALLED_SEPARATE_JVM_NATIVE_RESTARTS' and len(restart['cases'])==4,'Four installed saved native restarts failed')
    evidence=dict(inputs=inputs,independentSeals=independent,fullMatrix=reference(matrix_root/'q0/world/INDEPENDENT_NATIVE_REPORT.json'),nativeShapes=reference(shapes),finiteCertificate=reference(matrix_root/'finite-certificate.json'),nativeBoundary=reference(integrate.ROOT/'catalog/final-native-boundary/native-boundary-report.json'),coordinateDecision=reference(coordinates_path),globalReturnReports=global_refs,nativeUseReports=use_refs,nativeStateReview=state_refs,sidegateReports=side_refs,eventBusProtectionReports=protection_refs,installedFaultResult=reference(integrate.ROOT/'core/final-runtime-faults/result.json'),pauseIO=reference(commands/'pause-io-progress.json'),invalidReceipts=reference(commands/'receipt-negative-progress.json'),savedRestarts=reference(commands/'restart-progress.json'),nativeTickObservations=fluid)
    decisions={gate:dict(status='INDEPENDENT_PASS',report=str(HOME/'acceptance-reports'/(gate+'.json')))for gate in GATES}
    details={'ORIGINAL_HASH_METADATA':'Eight original resources unchanged, one explicit authorized derived entrance; all full IDs/source map preserved','INSTALLED_EXACT_ARTIFACT':'Four ordinary Forge installations of the final release JAR at original seed267611119455246637 anchor1000,200,1000 ordinals0..3','SAVED_NATIVE_READBACK_RESTART':'All source cells/default and raw native payloads, full shell/escape readback, four separate JVM restarts; raw native wall/vine evolution explicitly preserved and independently checked','ALL_ORIGINAL_TARGETS_FOUR_ROTATIONS_RETURN':'36 scenes2248 original+sidegate directed-return checks and592 original targets across four actual layouts','ACTUAL_GLOBAL_ENTRANCE_RETURN':'148 original targets and8 actual corridor bridges per layout; block-center physical convention explicitly accepted via4496 endpoint arithmetic review','FULL_DECLARED_WIDTH_CLEARANCE':'748 native lanes plus exact finite4620 native-box tiles and352/704 native boundary representatives with192singleton160open atoms and per-move context/state/shape checks; conditional static coverage combined with finite native representatives, no infinite motion enumeration claim','NATIVE_STEP_DOOR_CONTAINER_INTERACTIONS':'60 original mapped native ray/reach/menu/door uses with supported entrance walks/returns; side gate closed/open geometry and main-arch independence checked separately; no human client/network claim','EXACT_SOURCE_FLUID_TICKS_RESTART':'9 exact source lava cells per layout; >=2400 natural ticks with all force-ticking observation samples, no unsupported vines/fire/fluid escape; separate JVM saved native readback','RUNTIME_PROTECTION_CONFLICT_RECOVERY':'Independent installed corrupt journals/foreign preflight/unknown inventory/Java forcekill explicit recovery/real piston; six actual Forge event bus response tests; installed pause IOException cleanup and six invalid receipt refusals; no power-loss claim','NO_UNRESOLVED_BLOCKERS':'All scoped independent gates reviewed; earlier bounds/import/stale-report fixture failures retained and replaced by exact-artifact corrected evidence. Candidate acceptance/production release remain false. Explicit isolated commit and its admission-I/O/restart validation follow this eligible decision.'}
    for gate,entry in decisions.items():
        path=Path(entry['report']);integrate.save(path,dict(gate=gate,status='INDEPENDENT_PASS',jarSha256=jar,scope=details[gate],evidence=evidence,accepted=False,productionPromotion=False));entry['sha256']=integrate.digest(path)
    result=dict(status='ALL_INDEPENDENT_GATES_PASS_ISOLATED_COMMIT_ELIGIBLE',jarSha256=jar,snapshot=str(snapshot),gates=decisions,unresolvedBlockers=[],evidence=evidence,accepted=False,productionPromotion=False,actualCommitted=False)
    integrate.save(HOME/'acceptance-decision.json',result);print(result['status'])


if __name__=='__main__':main()
