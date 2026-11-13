import json
from pathlib import Path
import sys

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
import integrate


def main():
    actual=integrate.load(HOME/'actual-commit-progress.json');probe=integrate.load(HOME/'commit-io-progress.json');decision=integrate.load(HOME/'acceptance-decision.json')
    if actual['status']!='PASS_ACTUAL_EXPLICIT_ISOLATED_COMMITTED_AND_SAME_PATH_RESTART' or probe['status']!='PASS_INSTALLED_COMMIT_ADMISSION_IO_CLEANUP':raise integrate.GateError('Actual installed COMMITTED/restart and admission cleanup evidence required')
    if any(value['status']!='INDEPENDENT_PASS' for value in decision['gates'].values()) or decision['unresolvedBlockers']:raise integrate.GateError('Acceptance decision not complete')
    for gate,entry in decision['gates'].items():
        if integrate.digest(Path(entry['report']))!=entry['sha256']:raise integrate.GateError('Decided report changed '+gate)
    world=Path(actual['world']);receipt=world/'candidate-acceptance'/(actual['instanceId']+'.json')
    if integrate.digest(receipt)!=actual['receiptSha256']:raise integrate.GateError('Actual committed receipt changed')
    receipt_data=integrate.load(receipt)
    for entry in receipt_data['reports'].values():
        if integrate.digest(world/entry['path'])!=entry['sha256']:raise integrate.GateError('Actual committed gate report changed')
    result=dict(status='COMPLETE_INDEPENDENT_ISOLATED_ACCEPTANCE_ACTUAL_COMMITTED_RESTART_VERIFIED',jarSha256=actual['jarSha256'],instanceId=actual['instanceId'],canonicalWorld=str(world),completeIsolatedAcceptance=True,actualJobCOMMITTED=True,sameCanonicalWorldRestartVerified=True,accepted=False,productionPromotion=False,unresolvedBlockers=[],gates=decision['gates'],decision=dict(path=str(HOME/'acceptance-decision.json'),sha256=integrate.digest(HOME/'acceptance-decision.json')),commitAdmissionIO=dict(path=str(HOME/'commit-io-progress.json'),sha256=integrate.digest(HOME/'commit-io-progress.json')),actualCommit=dict(path=str(HOME/'actual-commit-progress.json'),sha256=integrate.digest(HOME/'actual-commit-progress.json')),counts=dict(actualInstalledOrientations=4,originalTargetsReturned=592,actualCorridorBridgesReturned=32,isolatedScenes=36,originalDirectedReturnChecks=2200,derivedSideGateDirectedReturnChecks=48,fullWidthNativeLanes=748,nativeShapeRecords=4416,finiteCoverageTiles=4620,stepBoundaryRepresentatives=352,nativeBoundaryDirections=704,singletonAtoms=192,openIntervalAtoms=160,nativeContextChecks=23784,nativeSourceShapeChecks=370364,nativeDoorContainerUses=60,forgeEventBusProtectionCases=6,independentInstalledFaultCases=6,invalidReceiptCases=6),scope=['Eight unchanged originals plus one authorized derived entrance; original targets/stairs/furniture/elevations preserved','Explicit rotated source block-center feet convention reviewed for4496 endpoints; no literal block-min-corner motion claim','Full declared width supported by exact finite native-box coverage plus native lane/step representative motion and context/state/shape checks; no infinite real-number motion enumeration claim','Door/container native FakePlayer ray/reach/use/menu/return, event-bus protections and real piston supplement; no human/network client claim','Abrupt exact-PID Java termination recovery and ordinary JVM save/restart; no power-loss guarantee','Runtime/candidate accepted and productionPromotion remain false; actual COMMITTED applies only to this authorized isolated job'])
    integrate.save(HOME/'final-evidence.json',result)
    excluded=[];files={}
    for path in sorted(HOME.rglob('*')):
        if not path.is_file():continue
        relative=path.relative_to(HOME);parts=relative.parts
        if relative.as_posix()=='ready.json' or any(part in ('.gradle','gradle-cache','__pycache__') for part in parts) or ('project' in parts and 'build' in parts and not any(part in ('classes','resources') for part in parts)):
            excluded.append(relative.as_posix());continue
        files[relative.as_posix()]=integrate.digest(path)
    integrate.save(HOME/'ready.json',dict(component='integration-v6-final-independent-isolated-acceptance',ready=True,status=result['status'],jarSha256=result['jarSha256'],completeIsolatedAcceptance=True,actualJobCOMMITTED=True,accepted=False,productionPromotion=False,finalEvidence='final-evidence.json',files=files,exclusionPolicy='Only ready self-reference, Python/Gradle caches and reproducible Gradle build intermediates excluded; executed class/resources and verification source/locks retained',excludedFileCount=len(excluded)))
    print(result['status'],'sealed files=',len(files),'readySha256=',integrate.digest(HOME/'ready.json'))


if __name__=='__main__':main()
