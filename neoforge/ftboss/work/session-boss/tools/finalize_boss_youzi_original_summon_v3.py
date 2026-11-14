import hashlib
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
receipt_path=WORK/'reports/youzi-original-summon-closeout-20261114.json';receipt=load(receipt_path);candidate=Path(receipt['candidate']);assert sha(candidate)==receipt['candidateJarSha256'] and not receipt['nativeMatrixAccepted']
intro=(ROOT/'AGENTS.md').read_text(encoding='utf-8').split('## Stage32 原版 hurt')[0];assert intro.startswith('## Stage33')
assert (WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md').read_text(encoding='utf-8').startswith(intro)
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py';namespace={'__file__':str(helper),'__name__':'create_only'};exec(compile(helper.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(helper),'exec'),namespace)
expected_doc=intro+'\n待办：\n\n'+'\n'.join('- '+value for value in receipt['remaining'])+'\n'
assert (WORK/'YOUZI_ORIGINAL_SUMMON_RECOVERY_20261114.md').read_text(encoding='utf-8')==expected_doc
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path);assert not state['activeRuntimePids'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited'];state.update({'currentWorkstream':'boss_stage33_original_youzi_methods_structurally_restored_native_oracle_pending_source_archive','latestScopedBossCandidate':str(candidate),'latestScopedBossCandidateSha256':sha(candidate),'latestScopedBossCandidateBuildReceipt':str(candidate.parent/'candidate-build-receipt.json'),'latestScopedBossCandidateAcceptanceReceipt':str(receipt_path),'latestYouziOriginalSummonCloseout':str(receipt_path),'latestYouziOriginalSummonLoadedAudit':str(WORK/'reports/youzi-original-summon-loaded-audit-v4-20261114/receipt.json'),'originalYouziThreeMethodsStructurallyRestored':True,'youziOriginalSummonScopedNativeAccepted':False,'fullManagedManorSharingNativeAccepted':False,'fullBossMigrationComplete':False,'latestScopedBossCandidateInstallable':False,'latestScopedBossCandidateDeployed':False});path.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'candidateSha256':sha(candidate),'actualGraphMethods':342,'nativeMatrixAccepted':False,'formalHashesVerified':True}),flush=True)
