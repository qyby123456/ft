import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=ROOT/'github-sync/checkpoint-20261113-final'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):
    with (OUT/name).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def command(*arguments):
    result=subprocess.run(arguments,capture_output=True);return result.returncode,result.stdout.decode('utf-8',errors='replace').strip()
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';namespace={'__file__':str(helper)};exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],namespace)
load=namespace['load'];processes=namespace['processes'];process_audit=processes()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
release=WORK/'releases/boss-core-stage27-erosion-damage-20261113';deployed=load(release/'deployment-receipt.json');expected='6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0';assert sha(deployed['target'])==deployed['deployedSha256']==expected
archives=[]
for stage in ['boss-core-stage26-shadow-retirement-20261113','boss-core-stage27-erosion-damage-20261113']:
    for name in ['archive-receipt.json','closeout-archive-receipt.json']:
        receipt_path=WORK/'releases'/stage/name;receipt=load(receipt_path);assert receipt['failures']==0 and receipt['allFilesReadBackAndHashVerified'] and sha(receipt['archive'])==receipt['archiveSha256'];archives.append({'archiveRelativePath':Path(receipt['archive']).relative_to(ROOT).as_posix(),'sha256':receipt['archiveSha256'],'files':receipt['files'],'perFileReadbackVerified':True,'uploaded':False,'remoteRecoveryVerified':False})
prune={'.git','build','cache','.gradle','.idea','.vscode','.venv','venv','__pycache__','node_modules','classes','bin','logs','log','tmp','temp','server','game','saves','world','runtimes','runtime','isolated-home','isolated-tmp','natives','libraries','archives','releases','reports','reference'}
extensions={'.java','.py','.ps1','.js','.mjs','.cjs','.ts','.tsx','.jsx','.gradle','.kts','.toml','.properties','.xml','.yml','.yaml','.md','.txt','.bat','.cmd','.sh'}
patterns=[('private-key',re.compile(r'-----BEGIN (?:RSA |EC |DSA |OPENSSH |ENCRYPTED )?PRIVATE KEY-----')),('provider-token',re.compile(r'(?:sk-[A-Za-z0-9_-]{20,}|gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|AKIA[0-9A-Z]{16})')),('inline-url-credential',re.compile(r'https?://[^\s/]+:[^\s/@]+@')),('jwt-shaped-value',re.compile(r'eyJ[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}')),('quoted-sensitive-assignment',re.compile(r'(?i)(?:api[_-]?key|access[_-]?token|refresh[_-]?token|client[_-]?secret|password|private[_-]?key)\s*[=:]\s*["\']([^"\'\r\n]{8,})["\']'))]
excluded_names={'accounts.json','launcher_accounts.json','launcher_profiles.json','usercache.json','usernamecache.json','ops.json','whitelist.json','WRITE_OWNER.json','WRITE_OWNER.claim','baseline.json'}
source=[];findings=[];large=[];skipped=0
for directory,folders,names in os.walk(ROOT):
    folders[:]=[name for name in folders if name not in prune and not name.startswith('world_') and not name.startswith('.env')]
    for name in names:
        path=Path(directory)/name;relative=path.relative_to(ROOT).as_posix()
        if name in excluded_names or name.startswith('.env') or re.search(r'(?i)credentials?|(?:^|[-_.])secrets?(?:[-_.]|$)',name):skipped+=1;continue
        eligible=path.suffix.lower() in extensions or name in {'.gitignore','.gitattributes','gradlew','Dockerfile'}
        if not eligible:continue
        size=path.stat().st_size
        if size>20*1024*1024:large.append({'path':relative,'bytes':size,'ordinaryGitCandidate':False});continue
        text=path.read_text(encoding='utf-8-sig',errors='replace');local_findings=[]
        for line_number,line in enumerate(text.splitlines(),1):
            for category,pattern in patterns:
                if pattern.search(line):local_findings.append({'line':line_number,'category':category})
        row={'path':relative,'bytes':size,'sha256':sha(path),'secretScanFindings':len(local_findings),'reviewedSafeForUpload':False};source.append(row)
        if local_findings:findings.append({'path':relative,'sha256':row['sha256'],'findings':local_findings})
source.sort(key=lambda row:row['path']);findings.sort(key=lambda row:row['path'])
code,_=command('git','rev-parse','--show-toplevel');assert code!=0,'A repository appeared;inspect it rather than changing its state'
identity={label:command('git','config','--get',key)[0]==0 for label,key in [('nameConfigured','user.name'),('emailConfigured','user.email')]}
leftovers=[]
for folder in [WORK/'reports/boss-common-mod-cache-dedup-stage26-20261113',WORK/'reports/boss-common-mod-cache-dedup-stage27-20261113',WORK/'reports/boss-archived-jar-cache-stage27-20261113']:
    record=load(folder/'preflight.json')
    for name in record['paths']:
        path=Path(name)
        for suffix in ['.stage26-cache-link.tmp','.stage27-cache-link.tmp','.archived-cache-link.tmp']:
            if path.with_name(path.name+suffix).exists():leftovers.append(path.with_name(path.name+suffix).relative_to(ROOT).as_posix())
formal=Path(deployed['target'])
for suffix in ['.stage26-shadow.tmp','.stage27-erosion.tmp']:assert not formal.with_name(formal.name+suffix).exists()
assert not leftovers;save('source-inventory.json',{'files':source,'count':len(source),'allPathsRelativeToCurrentWorkspace':True,'filesWithFindings':len(findings),'largerSourceCandidates':large,'sensitiveNamesExcluded':skipped,'scanScope':'Text source/config/scripts/docs outside generated/runtime/raw-evidence directories;JSON requires separate reviewed resource/fixture/sanitized export. This initial scan is not a complete publish-safety guarantee.','noFilesUploaded':True});save('secret-scan-findings.json',{'matchedSecretValuesIncluded':False,'findings':findings,'allFindingFilesRequireReviewBeforeAnyStaging':True,'initialScanOnly':True})
readiness={'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'gitInstalled':shutil.which('git') is not None,'githubCliOnPath':shutil.which('gh') is not None,'currentWorkspaceHasGitRepository':False,'configuredRemote':None,'branchWorktreeAndUntrackedGitState':'not applicable until intended repository is provided;no .git created','configuredCommitIdentityPresenceOnly':identity,'githubAuthorizationVerified':False,'remoteRepositoryVisibilityVerified':False,'blocker':'Existing target GitHub repository URL not supplied;remote and permissions cannot be verified. No public repository may be created.','commitsCreated':0,'pushesPerformed':0,'releaseOrLfsUploadsPerformed':0,'remoteRecoveryVerified':False,'sourceCandidates':len(source),'filesWithInitialSecretFindings':len(findings),'allInitialFindingValuesOmitted':True,'allLocalArchivesRetained':True,'filesRemovedBasedOnGitHubStorage':0,'furtherDeploymentProhibited':True,'formalInstanceWritesPerformedForThisRequirement':False,'existingDeployedStage27Sha256Unchanged':expected,'halfWrittenOwnedTemporaryFiles':leftovers,'ownedRuntimeProcessesRemaining':0,'processAudit':process_audit,'archives':archives,'weaponDevelopmentPaused':True,'weaponRecoveryJarInstallable':False,'fullBossMigrationComplete':False,'fullBossGoalStatus':'active'}
save('sync-readiness.json',readiness);assert sha(deployed['target'])==expected;processes();print(json.dumps({'sourceCandidates':len(source),'filesWithInitialSecretFindings':len(findings),'blocker':readiness['blocker'],'deployedSha256Unchanged':expected,'uploads':0,'remoteRecoveryVerified':False}),flush=True)
