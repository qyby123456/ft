import argparse
import hashlib
import json
import shutil
import subprocess
import sys
from fractions import Fraction
from itertools import product
from pathlib import Path

ROOT=Path(__file__).resolve().parent
CATALOG=ROOT.parent
PROJECT=CATALOG.parent
sys.path.insert(0,str(CATALOG))
from freeze_catalog import digest,load,save

EXPECTED_JAR='bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f'


def locked_copy(source,relative):
    source=Path(source)
    before=digest(source)
    destination=ROOT/relative
    destination.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(source,destination)
    if digest(destination)!=before or digest(source)!=before:
        raise ValueError('input changed while copying: '+str(source))
    return {'source':str(source),'path':relative,'sha256':before}


def inverse_box(world_box,origin,turns):
    minimum=[None,None,None]
    maximum=[None,None,None]
    for point in product(*[(Fraction(str(world_box[axis])),Fraction(str(world_box[axis+3]))) for axis in range(3)]):
        position_x,position_y,position_z=[value-Fraction(str(origin[axis])) for axis,value in enumerate(point)]
        local=[(position_x,position_y,position_z),(position_z,position_y,-position_x),(-position_x,position_y,-position_z),(-position_z,position_y,position_x)][turns]
        for axis,value in enumerate(local):
            minimum[axis]=value if minimum[axis] is None else min(minimum[axis],value)
            maximum[axis]=value if maximum[axis] is None else max(maximum[axis],value)
    return minimum+maximum


def audit_native(contract,native):
    if len(contract['strips'])!=12 or len(native['scenes'])!=12 or sum(len(scene['blocks']) for scene in native['scenes'])!=4416:
        raise ValueError('expected complete 12/4416 collection')
    summaries=[]
    for strip in contract['strips']:
        scene=next(scene for scene in native['scenes'] if scene['stripId']==strip['stripId'])
        if scene['quarterTurns']!=strip['quarterTurns'] or scene['resourceSha256']!=strip['resourceSha256']:
            raise ValueError('scene version mismatch')
        if scene['nativeContext']!='PLAYER_STANDING_COLLISION_CONTEXT':
            raise ValueError('unexpected native context')
        if scene['actualOrigin'].keys() < {'x','y','z'}:
            raise ValueError('origin missing')
        origin=[scene['actualOrigin'][axis] for axis in ('x','y','z')]
        turns=scene['quarterTurns']
        boxes=0
        for block in scene['blocks']:
            if block['nativeState']!=block['sourceRotatedState']:
                raise ValueError('native dynamic/source-rotated state mismatch '+strip['stripId']+' '+str(block['localBlock']))
            if not block['fluidEmpty']:
                raise ValueError('fluid in full-width native collection')
            local_x,local_y,local_z=block['localBlock']
            rotated=[(local_x,local_y,local_z),(-local_z-1,local_y,local_x),(-local_x-1,local_y,-local_z-1),(local_z,local_y,-local_x-1)][turns]
            expected_world=[value+offset for value,offset in zip(rotated,origin)]
            if expected_world!=block['worldBlock']:
                raise ValueError('native block rigid transform mismatch')
            if len(block['boxes'])!=len(block['nativeWorldBoxes']):
                raise ValueError('shape count mismatch')
            for local_box,world_box in zip(block['boxes'],block['nativeWorldBoxes']):
                if inverse_box(world_box,origin,turns)!=[Fraction(str(value)) for value in local_box]:
                    raise ValueError('inverse full-corner native AABB mismatch')
                boxes+=1
        summaries.append({'stripId':strip['stripId'],'resourceSha256':scene['resourceSha256'],'quarterTurns':turns,'centerX':strip['centerX'],'centerZ':strip['centerZ'],'headroom':strip['headroom'],'bodyWidth':strip['bodyWidth'],'startFeetY':strip['startFeetY'],'endFeetY':strip['endFeetY'],'blockCount':len(scene['blocks']),'boxCount':boxes,'nativeStateVsSourceRotatedState':'EXACT_MATCH_ALL_COLLECTED_CELLS','worldBlockAndFullAABBInverse':'PASS','playerContextAtRecordedFeet':scene['contextFeet'],'contextInvarianceAlongFullPath':'NOT_PROVEN_BY_SINGLE_CONTEXT_CAPTURE','nativeStepExecution':'NOT_EXECUTED_BY_CATALOG'})
    return {'status':'PASS_EXACT_CAPTURE_AUDIT','strips':summaries,'sourceStateChecks':4416,'dynamicStateScope':'exact captured state string matches rotated original at collection time only; not an elapsed tick or all-context invariant proof','nativeContextScope':'standing FakePlayer at one recorded contextFeet per strip; varying-feet/step context still independent obligation'}


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--native',required=True)
    parser.add_argument('--snapshot',required=True)
    arguments=parser.parse_args()
    native_source=Path(arguments.native).resolve()
    snapshot=Path(arguments.snapshot).resolve()
    integration=(PROJECT/'integration/v6-final').resolve()
    if not native_source.is_relative_to(integration) or not snapshot.is_relative_to(integration):
        raise ValueError('only v6-final owned input accepted')
    if (ROOT/'inputs/native-shapes.json').exists():
        raise ValueError('existing input lock: use a new versioned review directory, no overwrite')
    save(ROOT/'progress.json',{'component':'catalog-continuous-native-review','status':'VERIFYING_REAL_NATIVE_INPUT','nativeSource':str(native_source),'snapshot':str(snapshot),'worldAcceptance':False,'expectedJarSha256':EXPECTED_JAR})
    support=CATALOG/'full-width-certificate-v6'
    support_ready=load(support/'ready.json')
    for record in load(support/'SHA256SUMS.json')['files']:
        if digest(support/record['path'])!=record['sha256']:
            raise ValueError('frozen calculator changed')
    locks=[]
    for name in ('ready.json','contract.json','certificate.py','native-input.schema.json','SHA256SUMS.json'):
        locks.append(locked_copy(support/name,'calculator-snapshot/'+name))
    lock=load(snapshot/'snapshot-lock.json')
    verified=[]
    for component,record in lock['components'].items():
        for relative,expected in record['files'].items():
            target=snapshot/component/relative
            if digest(target)!=expected:
                raise ValueError('final snapshot mismatch: '+str(target))
            verified.append({'component':component,'path':relative,'sha256':expected})
    runtime_ready=load(snapshot/'runtime/ready.json')
    if runtime_ready['artifactSha256']!=EXPECTED_JAR:
        raise ValueError('wrong final jar version')
    artifact=snapshot/'runtime'/runtime_ready['artifact']
    if digest(artifact)!=EXPECTED_JAR:
        raise ValueError('final jar bytes do not match')
    for component in ('runtime','core','catalog','continuous-certificate'):
        locks.append(locked_copy(snapshot/component/'ready.json','inputs/final-'+component+'-ready.json'))
    locks.append(locked_copy(snapshot/'snapshot-lock.json','inputs/final-snapshot-lock.json'))
    locks.append(locked_copy(artifact,'inputs/final-artifact.jar'))
    locks.append(locked_copy(native_source,'inputs/native-shapes.json'))
    collector=PROJECT/'integration/v6-final/world-tests/cn/ft/biomedungeon/runtime/NativeShapeCollection.java'
    locks.append(locked_copy(collector,'inputs/NativeShapeCollection-current-readonly.java'))
    contract=load(ROOT/'calculator-snapshot/contract.json')
    native=load(ROOT/'inputs/native-shapes.json')
    if native['contractSha256']!=digest(ROOT/'calculator-snapshot/contract.json'):
        raise ValueError('native contract hash mismatch')
    audit=audit_native(contract,native)
    save(ROOT/'capture-audit.json',audit)
    save(ROOT/'input-lock.json',{'runtimeJarSha256':EXPECTED_JAR,'nativeInputSource':str(native_source),'finalSnapshot':str(snapshot),'inputs':locks,'finalSnapshotFilesVerified':len(verified),'verifiedFiles':verified,'supportReadySha256':digest(support/'ready.json'),'noIntegrationFilesModified':True})
    save(ROOT/'progress.json',{'component':'catalog-continuous-native-review','status':'RUNNING_FROZEN_CALCULATOR_ON_NATIVE_CAPTURE','nativeSHA256':digest(ROOT/'inputs/native-shapes.json'),'capturedBlocks':4416,'strips':12,'stateAndTransforms':'PASS','nativeStepAndContextInvariance':'NOT_CLAIMED'})
    print('AUDIT PASS real4416 records12strips; exact states/native transforms; snapshot files='+str(len(verified)),flush=True)
    command=[sys.executable,'-B',str(ROOT/'calculator-snapshot/certificate.py'),'--contract',str(ROOT/'calculator-snapshot/contract.json'),'--native',str(ROOT/'inputs/native-shapes.json'),'--out',str(ROOT/'finite-certificate.json')]
    with (ROOT/'calculator.log').open('w',encoding='utf-8') as log:
        completed=subprocess.run(command,cwd=ROOT,stdout=log,stderr=subprocess.STDOUT,check=False)
    if completed.returncode!=0:
        save(ROOT/'progress.json',{'component':'catalog-continuous-native-review','status':'CALCULATOR_REJECTED_REAL_INPUT','returncode':completed.returncode,'log':'calculator.log','noNativePASS':True})
        raise RuntimeError('frozen calculator failed: see calculator.log')
    result=load(ROOT/'finite-certificate.json')
    summary={'component':'catalog-continuous-native-review','status':result['status'],'realNativeInput':True,'runtimeJarSha256':EXPECTED_JAR,'nativeSHA256':digest(ROOT/'inputs/native-shapes.json'),'contractSha256':digest(ROOT/'calculator-snapshot/contract.json'),'calculatorSha256':digest(ROOT/'calculator-snapshot/certificate.py'),'captureAudit':'capture-audit.json','certificate':'finite-certificate.json','strips':[{'stripId':strip['stripId'],'status':strip['status'],'finiteAtoms':len(strip['tiles']),'failureCount':len(strip['failures']),'nativeStepBoundariesPending':len(strip['nativeStepReplayRequired'])} for strip in result['results']],'nativeStateDynamicScope':audit['dynamicStateScope'],'nativeContextScope':audit['nativeContextScope'],'completeGeneratorReady':False,'accepted':False,'productionPromotion':False,'nativeMovementOrContextInvariantCertificate':'NOT_PROVEN_BY_THIS_STATIC_REVIEW','integrationIndependentExecutionStillRequired':True}
    save(ROOT/'review-summary.json',summary)
    for record in locks:
        if digest(ROOT/record['path'])!=record['sha256'] or digest(record['source'])!=record['sha256']:
            raise ValueError('input lock changed during computation')
    files=[{'path':path.relative_to(ROOT).as_posix(),'sha256':digest(path),'bytes':path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name not in ('ready.json','progress.json','SHA256SUMS.json') and '__pycache__' not in path.parts]
    save(ROOT/'SHA256SUMS.json',{'files':files,'algorithm':'SHA-256'})
    save(ROOT/'ready.json',{'component':summary['component'],'ready':True,'status':'REAL_NATIVE_STATIC_RECOMPUTATION_COMPLETE','resultStatus':summary['status'],'accepted':False,'runtimeJarSha256':EXPECTED_JAR,'nativeInputSha256':summary['nativeSHA256'],'summary':'review-summary.json','summarySha256':digest(ROOT/'review-summary.json'),'certificateSha256':digest(ROOT/'finite-certificate.json'),'inventorySha256':digest(ROOT/'SHA256SUMS.json'),'limitations':['static native box finite coverage only; no independent motion step execution','one-context state equality not a whole-route invariant or tick proof','not a generator release or promotion; integration owns final acceptance']})
    save(ROOT/'progress.json',{'component':summary['component'],'status':'REAL_NATIVE_STATIC_RECOMPUTATION_COMPLETE','resultStatus':summary['status'],'readySha256':digest(ROOT/'ready.json'),'realNativeBlocks':4416,'strips':12,'nativeMotionClaim':False})
    print('RESULT '+summary['status']+' failures='+str(sum(strip['failureCount'] for strip in summary['strips'])),flush=True)


if __name__=='__main__':
    main()
