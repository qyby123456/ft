import datetime
from pathlib import Path
import shutil
import sys

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
import integrate


def copy_inventory(source,target,files):
    for relative,expected in files.items():
        original=integrate.contained(source,relative)
        if integrate.digest(original)!=expected:
            raise integrate.GateError('Input hash mismatch: '+str(original))
        destination=integrate.contained(target,relative);destination.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(original,destination)
        if integrate.digest(destination)!=expected or integrate.digest(original)!=expected:
            raise integrate.GateError('Input changed while freezing')


def main():
    phase=sys.argv[1] if len(sys.argv)>1 else 'prepare'
    runtime=integrate.ROOT/'runtime/next-commit-iofix'
    if phase=='freeze' and not (runtime/'ready.json').is_file():
        integrate.save(HOME/'progress.json',dict(status='WAITING_EXACT_RUNTIME_READY_NO_INDEPENDENT_WORLD_LAUNCHED',runtime=str(runtime),productionPromotion=False,completeGeneratorReady=False))
        print('WAITING_EXACT_RUNTIME_READY');return
    stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    root=HOME/('staging' if phase=='freeze' else 'input-preparation')/stamp
    lock=dict(createdAt=integrate.stamp(),productionPromotion=False,inheritedWorldPass=False,components={})
    for name,source in [('core',integrate.ROOT/'core/envelope-usability-review'),('catalog',integrate.ROOT/'catalog/s8-07-doorway-review-v5')]+([('runtime',runtime)] if phase=='freeze' else []):
        ready_hash=integrate.digest(source/'ready.json');ready=integrate.load(source/'ready.json')
        if ready.get('ready') is False:
            raise integrate.GateError('Not frozen: '+name)
        files=integrate.inventory(ready,allow_empty=name=='catalog')
        if name=='catalog':
            if integrate.digest(source/'SHA256SUMS.json')!=ready['inventorySha256']:
                raise integrate.GateError('Catalog inventory mismatch')
            files|=integrate.inventory(integrate.load(source/'SHA256SUMS.json'))
            files['SHA256SUMS.json']=integrate.digest(source/'SHA256SUMS.json')
        copy_inventory(source,root/name,files)
        shutil.copyfile(source/'ready.json',root/name/'ready.json')
        if integrate.digest(source/'ready.json')!=ready_hash:
            raise integrate.GateError('Ready changed during snapshot')
        lock['components'][name]=dict(source=str(source),readySha256=ready_hash,files=files)
    certificate=integrate.ROOT/'catalog/full-width-certificate-v6'
    certificate_ready_hash=integrate.digest(certificate/'ready.json')
    certificate_ready=integrate.load(certificate/'ready.json')
    if certificate_ready.get('ready') is not True or certificate_ready.get('component')!='catalog-full-width-certificate-v6':
        raise integrate.GateError('The complete frozen v6 certificate package is required')
    if integrate.digest(certificate/'SHA256SUMS.json')!=certificate_ready['inventorySha256']:
        raise integrate.GateError('Certificate v6 inventory mismatch')
    certificate_files=integrate.inventory(integrate.load(certificate/'SHA256SUMS.json'))
    certificate_files['SHA256SUMS.json']=integrate.digest(certificate/'SHA256SUMS.json')
    certificate_files['ready.json']=certificate_ready_hash
    for field,name in [('contractSha256','contract.json'),('schemaSha256','native-input.schema.json'),('calculatorSha256','certificate.py')]:
        if certificate_files.get(name)!=certificate_ready[field]:
            raise integrate.GateError('Certificate v6 ready binding mismatch: '+name)
    copy_inventory(certificate,root/'continuous-certificate',certificate_files)
    lock['components']['continuous-certificate']=dict(source=str(certificate),readySha256=certificate_ready_hash,files=certificate_files,nativeEvidenceProvidedByOwner=False)
    catalog=root/'catalog/nine-room-package-v5'
    mapping=integrate.load(catalog/'source-map.json');identity={item['candidateId']:item['originalId'] for item in mapping}
    if identity.get('s8_07_widearch_side_doublegate_v5')!='s8_07' or len(identity)!=9:
        raise integrate.GateError('Explicit full original/candidate identity mapping missing')
    matrix=integrate.load(catalog/'path-check-matrix.json');routes=integrate.load(catalog/'rotated-route-inputs.json')
    if len(matrix['checks'])!=2248 or len(routes['cases'])!=36:
        raise integrate.GateError('Original+sidegate matrix coverage mismatch')
    contract=integrate.load(root/'continuous-certificate/contract.json')
    if len(contract['strips'])!=12 or sum(len(strip['requiredBlocks']) for strip in contract['strips'])!=4416:
        raise integrate.GateError('Certificate strip/block coverage changed')
    if not contract.get('nativeShapeBoundsByBlockName') or any(len(strip.get('sourceBlockNames',{}))!=len(strip['requiredBlocks']) for strip in contract['strips']):
        raise integrate.GateError('Certificate v6 bounded native policy/source block identity missing')
    collection=dict(status='PREPARED_NO_NATIVE_GEOMETRY_YET',contractSha256=certificate_files['contract.json'],schemaVersion=1,collectionMode='WORLD_NATIVE_VOXELSHAPE',coverageComplete=False,requiredStripCount=len(contract['strips']),requiredBlockRecords=sum(len(strip['requiredBlocks']) for strip in contract['strips']),candidateToOriginalId=identity,requirements=['Use CollisionContext.of(native standing FakePlayer), not block default collision context or analytic guessed boxes','Load actual source/rotated blocks in all36 scenes before collecting12 strips; retain air/empty shapes and fluidEmpty explicitly','Each block localBlock is original source coordinate; actual world position uses frozen room origin and voxel rotation','Native box corners rotate back by inverse rigid rotation to original local continuous coordinates before certificate input; preserve exact dyadic values','Bind every stripId/resource SHA/q/contract hash, no prefix/substring ID inference','Run original748 swept samples and native step-boundary forward/return separately from finite rational coverage script','Never certify coverageComplete until all4416 required native block records exist and source/state/rotation are verified'],scenes=[])
    collection['certificateComponent']='catalog-full-width-certificate-v6'
    collection['requirements']+=['Emit blockName from the actual native block registry for every record, including empty shapes; require equality with this strip sourceBlockNames at the original local coordinate','Preserve complete native AABBs without clipping; use only the exact named nativeShapeBoundsByBlockName bounds, including approved height1.5 where applicable','Unknown block names or bounds violations require a new expanded frozen policy/collection; never trim or omit native geometry','The nine supplied synthetic tests are calculator checks only and do not establish native world evidence']
    integrate.save(root/'collection-preparation.json',collection)
    integrate.save(root/'snapshot-lock.json',lock)
    if phase=='freeze':
        ready=integrate.load(root/'runtime/ready.json');jar_candidates=[path for path in lock['components']['runtime']['files'] if path.endswith('.jar') and 'build' in path and 'libs' in path]
        if len(jar_candidates)!=1:
            raise integrate.GateError('Exactly one frozen release JAR required')
        jar=root/'runtime'/jar_candidates[0]
        integrate.save(HOME/'progress.json',dict(status='EXACT_INPUTS_FROZEN_INDEPENDENT_WORLDS_NOT_RUN',snapshot=str(root),jar=str(jar),jarSha256=integrate.digest(jar),snapshotLockSha256=integrate.digest(root/'snapshot-lock.json'),completeGeneratorReady=False,productionPromotion=False))
    else:
        integrate.save(HOME/'prepared-inputs.json',dict(status='CATALOG_CORE_CERTIFICATE_INPUTS_PREPARED_WAIT_RUNTIME_READY',snapshot=str(root),snapshotLockSha256=integrate.digest(root/'snapshot-lock.json'),coreFilesVerified=len(lock['components']['core']['files']),catalogFilesVerified=len(lock['components']['catalog']['files']),collection=str(root/'collection-preparation.json'),noNativeCertificateClaim=True))
    print(phase,root,'strips=',collection['requiredStripCount'],'blocks=',collection['requiredBlockRecords'])


if __name__=='__main__':
    main()
