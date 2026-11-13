import json
import shutil
import sys
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parent
CATALOG=ROOT.parent
PROJECT=CATALOG.parent
sys.path.insert(0,str(CATALOG))
from freeze_catalog import digest,load,save


def snapshot(path,relative):
    expected=digest(path)
    destination=ROOT/relative
    destination.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(path,destination)
    if digest(path)!=expected or digest(destination)!=expected:
        raise ValueError('read input changed during copy')
    return {'source':str(path),'path':relative,'sha256':expected}


def main():
    integration=PROJECT/'integration/v6-final'
    failed_run=integration/'native-run/20261106_172659-boundary'
    # The failed run verification snapshot identifies the code actually run.
    candidates=list((failed_run/'verification').rglob('BoundaryTests.java'))
    if not candidates:
        candidates=[integration/'world-tests/cn/ft/biomedungeon/runtime/BoundaryTests.java']
    actual=candidates[0]
    source_text=actual.read_text('utf-8')
    core=PROJECT/'core/src/main/java/cn/ft/biomedungeon/core/DungeonApi.java'
    certificate=CATALOG/'continuous-native-review/finite-certificate.json'
    report=load(certificate)
    inputs=[snapshot(actual,'inputs/BoundaryTests-failed-run.java'),snapshot(integration/'world-tests/cn/ft/biomedungeon/runtime/BoundaryTests.java','inputs/BoundaryTests-current.java'),snapshot(core,'inputs/DungeonApi.java'),snapshot(failed_run/'q0/native.log','inputs/failed-native.log'),snapshot(certificate,'inputs/finite-certificate.json'),snapshot(integration/'world-tests/cn/ft/biomedungeon/runtime/NativeShapeCollection.java','inputs/NativeShapeCollection-current.java')]
    requirements=[]
    atom_counts=Counter()
    for strip in report['results']:
        seen=set()
        for index,boundary in enumerate(strip['nativeStepReplayRequired']):
            atom=boundary['xAtom']
            if not isinstance(atom,list) or len(atom)!=2 or not all(isinstance(value,(int,float)) for value in atom) or atom[0]>atom[1]:
                raise ValueError('unexpected nonflat xAtom')
            key=(tuple(atom),boundary['boundaryZ'],boundary['firstFeet'],boundary['secondFeet'])
            if key in seen:
                raise ValueError('duplicate boundary requirement')
            seen.add(key)
            kind='singleton' if atom[0]==atom[1] else 'open_interval'
            atom_counts[kind]+=1
            requirements.append({'id':strip['stripId']+':'+str(index),'stripId':strip['stripId'],'xAtom':atom,'atomKind':kind,'representativeX':sum(atom)/2,'boundaryZ':boundary['boundaryZ'],'firstFeet':boundary['firstFeet'],'secondFeet':boundary['secondFeet'],'requiredDirections':['forward','returned'],'requiredHeadroom':3,'bodyWidth':0.6,'nativeStepHeight':0.6,'proofStatus':'native representative replay required; representative alone not proof for all real X inside open atom'})
    if len(requirements)!=352:
        raise ValueError('boundary count differs')
    core_text=core.read_text('utf-8')
    bad_constructor='turn,null)' in source_text
    throws_bounds='Objects.requireNonNull(bounds, "bounds")' in core_text
    if not bad_constructor or not throws_bounds:
        raise ValueError('expected failed-run null-bounds code differs; inspect snapshots')
    save(ROOT/'inputs-lock.json',{'files':inputs,'failedRunSourceSelection':str(actual),'fallbackCurrentOnly':not bool(list((failed_run/'verification').rglob('BoundaryTests.java'))),'noJVMStarted':True,'noIntegrationFilesModified':True})
    save(ROOT/'boundary-requirements.json',{'sourceCertificateSha256':digest(certificate),'requirements':requirements,'count':352,'nativeDirectionChecks':704,'atomKinds':dict(atom_counts),'keepSingletons':True,'doNotMergeOrDropAtoms':True})
    save(ROOT/'findings.json',{'rootCause':{'status':'STATICALLY_CONFIRMED_MATCHES_LOG','exceptionMessage':'bounds','cause':'BoundaryTests.unchanged constructs DungeonApi.PlacedRoom with bounds=null; its compact constructor requires nonnull bounds. Failure occurs before shape comparison/movement.','xAtomSchemaIsRootCause':False,'nativeJVMReproducedByCatalog':False},'schema':{'xAtom':'flat [low,high], exact singleton when low==high','requirements':352,'directions':704,'atomKinds':dict(atom_counts)},'requiredCorrections':[{'id':'B1','priority':'blocking','change':'Remove fake PlacedRoom construction. Add fixture-local inverse(AABB, BlockPos, origin,quarterTurns) overload, or compute equivalent eight-corner inverse directly from scene origin/turn. Do not weaken core non-null bounds contract or fabricate an unchecked dummy model.'},{'id':'B2','priority':'acceptance','change':'Assert12scene exact identity and resourceSHA/quarterTurns/contractSHA/certificateSHA/JAR binding before any replay; exactly352 requirements and704 directed results. Four flat south profiles have0steps; still retain complete4416 static collection and full36/2248 routes.'},{'id':'B3','priority':'acceptance','change':'Per requirement log original xAtom [low,high], singleton flag, stripId/index, before/at/after native feet, real world endpoints and both outcomes; assert attempted352 and passed352, directed704. Current helper.succeed unconditional after report must fail if any forward/returned=false.'},{'id':'B4','priority':'acceptance','change':'unchanged() currently checks context/state/shape only at sweep start/end. Recheck contributing native source states, fluid and complete untrimmed shape boxes before/after every move and at support boundary, with current CollisionContext.of(player). Record nativeState==sourceRotatedState and capture boxes. Explicitly justify any radius-based source-cell subset from bounded shape envelopes.'},{'id':'B5','priority':'acceptance','change':'Replace hardcoded top<=4 support selection with strip minFeetY/maxFeetY from frozen contract; boundary replay uses actual left/right native support and includes exact boundary singleton semantics. Preserve native movement, step0.6 and true3headroom/body0.6, no interior setPos/teleport.'},{'id':'B6','priority':'precision','change':'Current AABB.deflate(0.00001) shrinks declared body/headroom. Do not use it to waive actual penetration; report exact AABB/native result separately. Quantify numeric error if needed and retain exact rational static coverage, not a changed width/headroom acceptance.'},{'id':'B7','priority':'proof-scope','change':'Midpoint replay proves a representative of an open xAtom only. Keep every singleton and open atom; combine with exact native finite box coverage and a justified native step-branch/context invariant for each atom before calling full continuous native proof. No denser finite sampling can replace an unstated invariant.'}], 'coordinateReview':{'continuousPointRotation':'q0(x,z), q1(-z,x), q2(-x,-z), q3(z,-x), then origin; BoundaryTests.physical matches','discreteCellRotation':'q1(-z-1,x),q2(-x-1,-z-1),q3(z,-x-1); preserve -1 for block min corners only','inverseBox':'inverse all8worldAABB corners after subtract origin and recompute min/max; do not apply discrete -1 to continuous vertices','contextHelperFix':'prefer raw origin/turn inverse overload to avoid irrelevant PlacedRoom bounds entirely'},'notice':'A failed fixture setup is not evidence that352 native step boundaries failed. Static certificate remains conditional; independent integration rerun required.'})
    print('PASS static root cause=null PlacedRoom bounds; xAtom flat schema valid')
    print('PASS352 requirements/704 directions; atoms='+str(dict(atom_counts)))


if __name__=='__main__':
    main()
