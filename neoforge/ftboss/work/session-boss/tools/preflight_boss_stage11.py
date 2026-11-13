import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

owner=load(ROOT/'WRITE_OWNER.json')
claim=load(ROOT/'WRITE_OWNER.claim')
thread='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['activeWriterThreadId']==claim['threadId']==thread
assert owner['weaponFunctionalDevelopmentPaused'] and not owner['weaponCandidateValidationResumed']
previous=WORK/'releases/boss-core-stage10-blood-tide-20261112'
feature=load(previous/'feature-verification.json')
deployment=load(previous/'deployment-receipt.json')
archived=load(previous/'archive-receipt.json')
terminal=load(WORK/'reports/stage10-terminal-audit-20261112.json')
assert feature['candidateCases']==59 and terminal['ownedJavaPythonRemaining']==0 and terminal['forgeUnchanged']
target=Path(deployment['target'])
assert digest(target)==deployment['deployedSha256']==archived['deployedIncrementSha256']
assert not list(target.parent.glob(target.name+'.stage*.tmp'))
assert digest(deployment['backup'])==deployment['backupSha256']
failed=WORK/'build/boss-blood-tide-v1'
assert load(failed/'compile.terminal.json')['exitCode']==0
assert load(failed/'proof-run.terminal.json')['exitCode']==1
assert not list(failed.glob('*.jar'))
recorded=[]
with zipfile.ZipFile(archived['archive']) as archive:
    manifest=json.loads(archive.read('ARCHIVE_MANIFEST.json'))
    for path in (failed/'proof-run.log',failed/'proof-run.terminal.json',WORK/'tests/BloodTideBoundaryProof.java'):
        key=path.relative_to(ROOT).as_posix()
        assert manifest[key]['sha256']==digest(path)
        recorded.append({'path':str(path),'sha256':digest(path),'retainedInStage10Archive':True})
command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease');target=($_.CommandLine -like '*FarmingTales_NeoForge 机械动力*')} } | ConvertTo-Json -Compress"
processes=json.loads(subprocess.check_output(['powershell.exe','-NoProfile','-Command',command],encoding='utf-8-sig'))
processes=processes if isinstance(processes,list) else [processes]
processes=[row for row in processes if row['pid']!=os.getpid()]
assert not any(row['owned'] or row['target'] for row in processes)
paused_path=WORK/'archives/weapons-paused-73d460d2-20261112/archive-receipt.json'
paused=load(paused_path)
assert paused['failures']==0 and not paused['installable'] and not paused['weaponStandaloneRunnableJarAccepted']
output=WORK/'build/boss-cleaver-sweep-audit-20261112'
output.mkdir()
report={'soleWriter':thread,'claimUnchanged':True,'stage10AcceptanceReusedWithoutRerun':True,'stage10CandidateCases':59,'stage10DedicatedNormalExitReused':True,'stage10RollbackAndArchiveEvidenceReused':True,'stage10Receipts':{str(path):digest(path) for path in (previous/'feature-verification.json',previous/'deployment-receipt.json',previous/'archive-receipt.json',previous/'closeout-archive-receipt.json',WORK/'reports/stage10-terminal-audit-20261112.json')},'earlyFailedBuildEvidence':recorded,'earlyFailureReason':'Proof fixture omitted unchanged compiler-generated lambda from expected method count; product compiled; no failed candidate JAR/runtime/deployment created. Corrected proof is frozen in stage10 evidence.','deploymentTemporaryFiles':[],'halfWrittenDeploymentDetected':False,'incompleteCandidatePromoted':False,'processAudit':processes,'weaponDevelopmentPaused':True,'weaponCheckpointReceipt':str(paused_path),'weaponCheckpointReceiptSha256':digest(paused_path),'weaponRecoveryJarInstallable':False,'priorScratchDisposition':'Archived retained evidence; no cleanup retry or deletion workaround','formalNeoForgeBeforeSha256':digest(target),'forgeBeforeSha256':deployment['forgeSha256Unchanged'],'fullBossAcceptance':False}
(output/'preflight.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
base=previous/'ftboss-boss-core-1.21.1-stage10.jar'
assert digest(base)==deployment['deployedSha256']
(output/'baseline-build-receipt.json').write_text(json.dumps({'jar':str(base),'jarSha256':digest(base),'proposal':str(WORK/'boss-core')},ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'preflightPassed':True,'stage10ReusedCases':59,'ownedOrTargetProcesses':0,'temporaryOrHalfWrittenDeployments':0,'earlyFailureArchived':True,'weaponDevelopmentPaused':True}),flush=True)
