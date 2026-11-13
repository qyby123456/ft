import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import load,save,digest


def main():
    verification=load(ROOT/'derived-candidate/verification.json')
    nine_log=(ROOT/'nine-package-tests.log').read_text('utf-8')
    if 'Ran 4 tests' not in nine_log or '\nOK' not in nine_log:
        raise ValueError('nine room package test log not passing')
    if 'Ran 3 tests' not in (ROOT/'tests.log').read_text('utf-8') or '\nOK' not in (ROOT/'tests.log').read_text('utf-8'):
        raise ValueError('test log not passing')
    files=[{'path':path.relative_to(ROOT).as_posix(),'sha256':digest(path),'bytes':path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name not in {'ready.json','progress.json','SHA256SUMS.json'} and '__pycache__' not in path.parts]
    save(ROOT/'SHA256SUMS.json',{'files':files,'algorithm':'SHA-256','excludes':['ready.json','progress.json','SHA256SUMS.json','__pycache__']})
    ready={'component':'catalog-s8-07-doorway-review-v5','apiVersion':1,'ready':True,'status':'DERIVED_CANDIDATE_OFFLINE_REVIEW_READY','accepted':False,'productionPromotion':False,'originalSha256':verification['originalSha256'],'derivedSha256':verification['derivedSha256'],'candidateManifest':'derived-candidate/candidate-manifest.json','candidateNBT':verification['resource'],'exactChangedCells':20,'preservedBE':6,'originalStairsAndHeightsPreserved':True,'doorFeature':'original adjacent doublegate relocated intact to functional side branch; main three-wide arch cannot be shut by that side pair','samplingFailureReproduced':'112=28x4, exact lane centers matched independently by interval algebra','routeAudit':'36cases2200checks148targetsPerRotation, zero removed','tests':'3 tests PASS plus actual derived NBT readback/geometry/BE/stair/floor/target-preservation assertions PASS','newNativeWorldTests':'NOT_RUN','runtimeHandoff':'new ID/hash/version required; no message sent or external-owner file changed; coordinator may direct isolated runtime/integration consumption','requires':verification['requiredIndependentChecks'],'inventorySha256':digest(ROOT/'SHA256SUMS.json')}
    package=ROOT/'nine-room-package-v5'
    ready.update({'status':'SEALED_EIGHT_ORIGINALS_ONE_DERIVED_ISOLATED_NINE_ROOM_PACKAGE_READY','originalSourcePath':next(record['originalSourcePath'] for record in load(package/'source-map.json') if not record['unchanged']),'preservedOriginalStairCount':12,'allOriginalTargetMapping':'nine-room-package-v5/all-original-target-mapping.json','allOriginalTargetMappingSha256':digest(package/'all-original-target-mapping.json'),'changeLedger':'derived-candidate/change-ledger.json','changeLedgerSha256':digest(ROOT/'derived-candidate/change-ledger.json'),'derivedTemplateId':'s8_07_widearch_side_doublegate_v5','nineRoomPackage':'nine-room-package-v5','nineRoomManifest':'nine-room-package-v5/data/biomedungeon/manifest.json','nineRoomManifestSha256':digest(package/'data/biomedungeon/manifest.json'),'sourceMap':'nine-room-package-v5/source-map.json','sourceMapSha256':digest(package/'source-map.json'),'nineRoomCount':9,'unchangedOriginalRoomCount':8,'originalRouteCases':36,'originalChecksPreserved':2200,'allOriginalTransformedTargets':592,'newSideGateTargets':16,'additionalSideGateChecks':48,'totalChecks':2248,'nativeConsumerContract':'nine-room-package-v5/native-test-contract.json','nativeConsumerContractSha256':digest(package/'native-test-contract.json'),'tests':'3 review tests +4 nine-package tests PASS; actual original/derived diff20, 12stairs, 6BE, allY0..3 unchanged','experimentAuthorization':'coordinator explicitly permits isolated derived design; accepted remains false; not production promotion','architecturalChangeNotice':'SIDE DOUBLE DOORS CANNOT SEAL MAIN ARCH. Eight original-byte rooms plus one derived entrance; not nine unchanged originals.','historicalProposalArchive':'history/README-before-seal.md'})
    save(ROOT/'ready.json',ready)
    save(ROOT/'progress.json',{'component':ready['component'],'status':ready['status'],'readySha256':digest(ROOT/'ready.json'),'accepted':False,'derivedSha256':ready['derivedSha256'],'tests':'PASS offline','worldTests':'NOT_RUN'})
    print('READY actual candidate unaccepted; files='+str(len(files))+' readySHA256='+digest(ROOT/'ready.json'))


if __name__=='__main__':
    main()
