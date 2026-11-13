import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import digest,load,save


def main():
    if 'Ran 9 tests' not in (ROOT/'tests.log').read_text('utf-8') or '\nOK' not in (ROOT/'tests.log').read_text('utf-8'):
        raise ValueError('tests required')
    old=ROOT.parent/'full-width-certificate-v5'
    for record in load(old/'SHA256SUMS.json')['files']:
        if digest(old/record['path'])!=record['sha256']:
            raise ValueError('old support changed')
    audit=load(ROOT/'collection-audit.json')
    if digest(old/'ready.json')!=audit['previousSupportReadySha256']:
        raise ValueError('old ready changed')
    files=[{'path':path.relative_to(ROOT).as_posix(),'sha256':digest(path),'bytes':path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name not in {'ready.json','progress.json','SHA256SUMS.json'} and '__pycache__' not in path.parts]
    save(ROOT/'SHA256SUMS.json',{'algorithm':'SHA-256','files':files,'excludes':['ready.json','progress.json','SHA256SUMS.json','__pycache__']})
    ready={'component':'catalog-full-width-certificate-v6','apiVersion':1,'ready':True,'status':'BOUNDED_NATIVE_OVERHANG_CONTRACT_READY_INPUT_PENDING','accepted':False,'productionPromotion':False,'contract':'contract.json','contractSha256':digest(ROOT/'contract.json'),'nativeInputSchema':'native-input.schema.json','schemaSha256':digest(ROOT/'native-input.schema.json'),'calculator':'certificate.py','calculatorSha256':digest(ROOT/'certificate.py'),'requiredNativeBlockEntries':4416,'sourceWallFenceGateHits':0,'newCollectionCellsNeededForCurrentDomain':0,'overhangPolicy':'exact named native wall/fence/gate box bounds through1.5high; no clipping; unknown/out-of-bound requires extended source coverage','certificateClaim':'CONDITIONAL_FINITE_COVERAGE_PASS only, native step evidence still required','tests':'9/9 synthetic tests PASS; native shape input not provided','oldSupportFrozenVerified':True,'inventorySha256':digest(ROOT/'SHA256SUMS.json')}
    save(ROOT/'ready.json',ready)
    save(ROOT/'progress.json',{'component':ready['component'],'status':ready['status'],'readySha256':digest(ROOT/'ready.json'),'nativeInput':'PENDING','tests':'9/9 synthetic PASS'})
    print('READY v6 contractSHA256='+ready['contractSha256']+' old support unchanged')


if __name__=='__main__':
    main()
