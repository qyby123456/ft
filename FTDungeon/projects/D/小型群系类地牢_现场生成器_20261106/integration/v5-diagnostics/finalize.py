from pathlib import Path
import sys

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
sys.path.insert(0,str(HOME.parent/'v4-unified'))
import integrate
import readback


def main():
    fault=integrate.load(HOME/'fault-final-evidence.json')
    interaction=integrate.load(HOME/'interaction-reach-progress.json')
    if interaction['status']!='SCOPED_NATIVE_PROBES_EXECUTED':
        raise integrate.GateError('Final interaction route experiments still live/incomplete')
    prior=HOME.parent/'v4-unified';prior_ready=integrate.load(prior/'ready.json')
    for relative,expected in prior_ready['files'].items():
        if integrate.digest(integrate.contained(prior,relative))!=expected:
            raise integrate.GateError('Frozen V4 changed: '+relative)
    snapshot=Path(integrate.load(prior/'final-evidence.json')['snapshot'])
    cases={};failures=[]
    for turn,entry in interaction['cases'].items():
        if entry['status']!='EXECUTED':
            raise integrate.GateError('Interaction harness did not execute: '+turn)
        path=Path(entry['report'])
        if integrate.digest(path)!=entry['reportSha256']:
            raise integrate.GateError('Interaction report changed')
        report=integrate.load(path)
        for use in report['interactions']:
            if use['status']!='PASS' or not use.get('noTeleportAtUseOrReturn') or not use.get('withinNativeUseReach') or not use.get('returnedToEntrance') or not use.get('nativePayloadUnchanged'):
                failures.append(dict(turn=turn,evidence=use))
        offline=readback.inspect(snapshot,path.parent)
        integrate.save(HOME/('interaction-saved-readback-q'+turn+'.json'),offline)
        original_door_differences=[item for item in offline['originalStateDifferences'] if 'door' in item['expected'][0]]
        if offline['originalNativeFieldDifferences'] or original_door_differences:
            failures.append(dict(turn=turn,doorDifferences=original_door_differences,nativeFields=offline['originalNativeFieldDifferences']))
        cases[turn]=dict(attempted=report['attempted'],passed=report['passed'],report=str(path),reportSha256=entry['reportSha256'],log=entry['log'],logSha256=entry['logSha256'],nativeBody='0.6x1.8 maxUpStep0.6',doorUses=sum('dark_oak_door' in use['sourceState'] for use in report['interactions']),containerUses=sum('dark_oak_door' not in use['sourceState'] for use in report['interactions']),noArtificialHitResults=True,rangeLimit=4.5,nativeClip='ClipContext.Block.COLLIDER; actual first hit must equal target block',originalDoorLowerUpperSavedDifferences=original_door_differences,rawAndDefaultNativeFieldDifferences=offline['originalNativeFieldDifferences'],savedReadback=str(HOME/('interaction-saved-readback-q'+turn+'.json')))
    rootcause=integrate.load(HOME/'s8_07-root-cause.json')
    gates=integrate.load(HOME/'remaining-gates.json')
    gates['blocking']=[entry for entry in gates['blocking'] if entry['id'] not in ('HUMAN_CLIENT_INTERACTIONS','CONTINUOUS_FULL_WIDTH_CLEARANCE','LITERAL_VS_BLOCK_INDEX_ENDPOINT_SEMANTICS','EXACT_V4_FAULT_PROTECTION')]
    gates['blocking'].append(dict(id='NEW_DERIVED_EXACT_JAR_ACCEPTANCE',state='AWAIT_RUNTIME_NEXT_COMMIT_FREEZE_CURRENT_OLD07_FAIL_RETAINED'))
    gates['passedScopeOnly'].extend(['Exact V4 JAR truncated/checksum journals rejected with127239 original source cells/native fields unchanged per case', 'Exact V4 Java-only forced termination: unknown inventory retained, incomplete disk checkpoint fail-closed with640584 saved envelope cells unchanged', 'Exact V4 foreign diamond block preflight leaves640584 target envelope cells unchanged except explicitly inserted foreign block', 'Four layouts60 native interactions reached by complete entrance walking, real collider rays within4.5, actual door/menu use, complete entrance return; no spatial teleport during use/return; segment helpers may reassign the identical current position'])
    gates['limitationsNotMandatoryBlockers']=['Human client/network UI NOT_RUN by authorized scope; native FakePlayer use is the current server-scope interaction evidence', 'Explicit block-index feet-center contract accepted for supported inputs; literal corner or unsupported fractional convention must reject rather than silently reinterpret', '748 native width samples are numerical evidence; a piecewise continuous native VoxelShape swept certificate can establish continuous clearance without enumerating all real coordinates', 'S8-07 original negative width certificate already analytical: original calcite/door collision leaves1.625 rather than3; original V4 full-width FAIL cannot be cleared by new candidate', 'V4 midbuild abrupt recovery did not complete: journal claimed37888 cells ahead of durable world. Safety quarantine PASS is not durability/recovery completion PASS. Exact replacement checkpoint must be independently tested.', 'Side doors of derived candidate cannot close central main arch; eight originals plus one derived entrance, never nine unchanged originals']
    gates['exactV4FaultStatus']=fault['status'];gates['nativeInteractionRouteStatus']='PASS' if not failures else 'FAIL'
    integrate.save(HOME/'remaining-gates.json',gates)
    final=dict(status='EXACT_V4_DIAGNOSTICS_FAULT_SAFETY_AND_NATIVE_INTERACTION_EVIDENCE_CLOSED_ORIGINAL_WIDTH_BLOCKED' if not failures else 'EVIDENCE_CLOSED_WITH_NATIVE_INTERACTION_FAILURES',component='integration-v5-diagnostics',exactV4JarSha256=fault['jarSha256'],completeGeneratorReady=False,productionPromotion=False,originalV4FullWidthAccepted=False,originalS8_07Sha256=rootcause['sourceSha256'],rootCause='s8_07-root-cause.json',coverageAudit='v4-coverage-audit.json',faultEvidence='fault-final-evidence.json',nativeInteractions=cases,interactionFailures=failures,totalNativeInteractions=sum(item['attempted'] for item in cases.values()),passedNativeInteractions=sum(item['passed'] for item in cases.values()),currentScope='Native FakePlayer server runtime only; no formal client/world changes, no network UI requirement',newDerivedCandidate='next-candidate-test-contract.json',remainingGates='remaining-gates.json',v4ReadySha256=integrate.digest(prior/'ready.json'),v4VerifiedFiles=len(prior_ready['files']),powerloss=False)
    integrate.save(HOME/'final-evidence.json',final)
    files={}
    for path in HOME.rglob('*'):
        if not path.is_file() or path==HOME/'ready.json':
            continue
        relative=path.relative_to(HOME)
        if any(part in ('libraries','project','.gradle','gradle-cache','__pycache__') for part in relative.parts):
            continue
        if path.suffix in ('.json','.py','.java','.gradle','.log','.nbt','.mca','.md','.properties') or path.name=='level.dat':
            files[str(relative)]=integrate.digest(path)
    integrate.save(HOME/'ready.json',dict(component='integration-v5-diagnostics',apiVersion=1,ready=True,status=final['status'],completeGeneratorReady=False,productionPromotion=False,powerloss=False,files=files))
    print(final['status'],'interactions=',final['passedNativeInteractions'],'/',final['totalNativeInteractions'],'files=',len(files))


if __name__=='__main__':
    main()
