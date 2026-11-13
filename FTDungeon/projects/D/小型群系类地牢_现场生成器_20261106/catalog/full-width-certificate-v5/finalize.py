import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import digest,load,save


def main():
    log=(ROOT/'tests.log').read_text('utf-8')
    if 'Ran 6 tests' not in log or '\nOK' not in log:
        raise ValueError('tests not passed')
    frozen=ROOT.parent/'s8-07-doorway-review-v5'
    for record in load(frozen/'SHA256SUMS.json')['files']:
        if digest(frozen/record['path'])!=record['sha256']:
            raise ValueError('v5 frozen resource changed')
    contract=load(ROOT/'contract.json')
    if digest(frozen/'ready.json')!=contract['frozenV5ReadySha256']:
        raise ValueError('v5 frozen ready changed')
    files=[{'path':path.relative_to(ROOT).as_posix(),'sha256':digest(path),'bytes':path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name not in {'ready.json','progress.json','SHA256SUMS.json'} and '__pycache__' not in path.parts]
    save(ROOT/'SHA256SUMS.json',{'algorithm':'SHA-256','files':files,'excludes':['ready.json','progress.json','SHA256SUMS.json','__pycache__']})
    ready={'component':'catalog-full-width-certificate-v5','apiVersion':1,'ready':True,'status':'EXECUTABLE_FINITE_INTERVAL_CONTRACT_READY_NATIVE_INPUT_PENDING','accepted':False,'productionPromotion':False,'contract':'contract.json','contractSha256':digest(ROOT/'contract.json'),'nativeInputSchema':'native-input.schema.json','schemaSha256':digest(ROOT/'native-input.schema.json'),'calculator':'certificate.py','calculatorSha256':digest(ROOT/'certificate.py'),'strips':12,'requiredNativeBlockEntries':sum(len(strip['requiredBlocks']) for strip in contract['strips']),'inputCoordinateSpace':'original source-local absolute coordinates after inverse native world transform','worldEvidence':'NOT_PROVIDED_YET','worldAcceptance':'NOT_RUN_BY_CATALOG','certificateClaimCeiling':'CONDITIONAL_FINITE_COVERAGE_PASS only; native step replay/algorithm-bound evidence still required','finiteCoverage':'all native AABB horizontal edges plus/minus body radius, exact rational open-interval and singleton-boundary atoms; no acceptance width shrink','samplingNotProof':True,'frozenV5ReadySha256':contract['frozenV5ReadySha256'],'originalV5InventoryVerified':True,'testCommand':'python -B test_certificate.py','tests':'PASS 6 synthetic algorithm tests; no fabricated native data','inventorySha256':digest(ROOT/'SHA256SUMS.json'),'consumerCommand':'python -B certificate.py --contract contract.json --native <integration-owned-native-shapes.json> --out <integration-owned-result.json>','constraints':['all original full-width/3-high strips retained','native shapes must be full collected player-context boxes, not inferred names','each support discontinuity requires both native directions, no setPos shortcut','sidegate independent narrow route; cannot close central arch','existing36cases2200checks remain mandatory']}
    save(ROOT/'ready.json',ready)
    save(ROOT/'progress.json',{'component':ready['component'],'status':ready['status'],'readySha256':digest(ROOT/'ready.json'),'nativeInput':'PENDING','tests':'6/6 synthetic PASS','v5FrozenUnchanged':True})
    print('READY contractSHA256='+ready['contractSha256']+' nativeBlockEntries='+str(ready['requiredNativeBlockEntries']))
    print('PASS frozen v5 inventory and ready unchanged')


if __name__=='__main__':
    main()
