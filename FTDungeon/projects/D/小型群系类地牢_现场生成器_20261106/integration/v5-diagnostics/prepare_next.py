import datetime
from pathlib import Path
import shutil
import sys

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
import integrate


def main():
    source=integrate.ROOT/'catalog/s8-07-doorway-review-v5'
    ready_hash=integrate.digest(source/'ready.json');ready=integrate.load(source/'ready.json')
    if ready['accepted'] or ready['derivedTemplateId']!='s8_07_widearch_side_doublegate_v5':
        raise integrate.GateError('Unexpected derived ID or promotion')
    if integrate.digest(source/'SHA256SUMS.json')!=ready['inventorySha256']:
        raise integrate.GateError('Derived catalog inventory changed')
    files=integrate.inventory(integrate.load(source/'SHA256SUMS.json'))
    root=HOME/'candidate-inputs'/datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    for relative,expected in files.items():
        original=integrate.contained(source,relative)
        if integrate.digest(original)!=expected:
            raise integrate.GateError('Derived input changed: '+relative)
        destination=integrate.contained(root,relative);destination.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(original,destination)
        if integrate.digest(destination)!=expected:
            raise integrate.GateError('Derived snapshot copy changed')
    shutil.copyfile(source/'ready.json',root/'ready.json');shutil.copyfile(source/'SHA256SUMS.json',root/'SHA256SUMS.json')
    if integrate.digest(source/'ready.json')!=ready_hash:
        raise integrate.GateError('Derived ready changed during freeze')
    mapping=integrate.load(root/'nine-room-package-v5/source-map.json')
    explicit={item['candidateId']:item['originalId'] for item in mapping}
    if len(explicit)!=9 or explicit['s8_07_widearch_side_doublegate_v5']!='s8_07':
        raise integrate.GateError('Explicit original ID mapping missing')
    package=root/'nine-room-package-v5'
    matrix=integrate.load(package/'path-check-matrix.json')
    routes=integrate.load(package/'rotated-route-inputs.json')
    plan=dict(status='CATALOG_FROZEN_AWAIT_EXACT_RUNTIME_NEXT_COMMIT_READY_NO_WORLD_PASS',catalogSnapshot=str(root),catalogReadySha256=ready_hash,files=files,explicitCandidateToOriginalId=explicit,longIdHandling='Use explicit source-map by full candidateId; never substring or s8_07 equality to infer role/case/source binding',derivedSha256=ready['derivedSha256'],originalChecks=2200,additionalSideGateChecks=48,totalChecks=len(matrix['checks']),rotatedScenes=len(routes['cases']),widthSamplesUnchanged=748,originalFailedSamplesToRetest=112,nativeContractSha256=integrate.digest(package/'native-test-contract.json'),designTradeoff='Side double doors cannot close central main arch. Eight original-byte rooms plus one derived entrance candidate; original V4 room still FAIL full width3.',required=['Exact ready/source/JAR/manifest/planner/resource hashes from runtime/next-commit, accepted=false and new operation/version binding', 'No V4 or V5-fault pass inherited by new JAR', 'All2200 original mapped checks retained plus48 new sidegate checks; all36 original rotations', '748 unchanged width/headroom sample checks including112 old failures must pass; report native sampling separately from piecewise native VoxelShape continuous sweep certificate', 'Native side doors canSurvive, lower/upper coherent open-close from entrance-walk reachable ray-visible in-range point, payload and return preserved', 'Actual nine-room four-root command layouts, native fluid/enclosure/BE readback, ordinary installed restart, exact-new-version fault and foreign-state protection', 'Visual review remains distinct from native acceptance'],currentScopeExcluded=['Formal instance/client/world changes', 'Human/network UI as mandatory blocker', 'Silent target deletion, reduced widths/headroom, flattened original stairs or candidate called unchanged original'])
    integrate.save(HOME/'next-candidate-test-contract.json',plan)
    print(plan['status'],'scenes=',len(routes['cases']),'checks=',len(matrix['checks']))


if __name__=='__main__':
    main()
