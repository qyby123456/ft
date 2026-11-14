import hashlib
import json
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/github-ft-source-ready-author-pending-20261113'
CHECKOUT=ROOT/'github-sync/repositories/ft'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';functions={'__file__':str(helper)};exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],functions)
load=functions['load'];sha=functions['sha'];save=functions['save'];archive=functions['archive'];processes=functions['processes']
assert not OUT.exists();processes();OUT.mkdir();owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['latestUserDeploymentProhibited'] and owner['weaponValidationPaused']
synced=load(WORK/'reports/github-ft-source-sync-v1/source-sync-receipt.json');verified=load(WORK/'reports/github-ft-staged-source-v3-final-20261113/verification.json');assert synced['allStagedBlobSourceSha256Verified'] and verified['candidateFiles']==synced['stagedFiles'] and verified['filesWithIssues']==0 and verified['indexTreeStableBeforeAfter'] and verified['indexTree']==synced['indexTree'] and verified['exactByteAndLineReviewedMetadataMatches']==3
staged_manifest=load(WORK/'reports/github-ft-staged-source-v3-final-20261113/source-manifest.json');current_source=load(Path(synced['sourceManifest']));expected={row['path']:row for row in current_source['files']}
for row in staged_manifest['files']:
    path=CHECKOUT/row['path'];assert sha(path)==row['sha256']
    relative=row['path'].removeprefix('neoforge/ftboss/')
    if relative in expected:assert sha(ROOT/relative)==row['sha256']==expected[relative]['sha256']
assert load(WORK/'reports/github-ft-remote-review-v1/receive-pack-known-account-ipv4-dry-run.json')['exitCode']==0
base='27d20d5e1d2eb960f4a64cde9bd21230efae766c'
for ref in ['HEAD','refs/heads/main','refs/remotes/origin/main']:
    result=subprocess.run(['git','-C',str(CHECKOUT),'rev-parse',ref],capture_output=True);assert result.returncode==0 and result.stdout.decode().strip()==base
identity=subprocess.run(['git','-C',str(CHECKOUT),'-c','user.useConfigOnly=true','var','GIT_AUTHOR_IDENT'],capture_output=True);assert identity.returncode!=0,'Author identity appeared;continue authorized commit/push rather than declaring an identity blocker'
deployed=load(WORK/'releases/boss-core-stage27-erosion-damage-20261113/deployment-receipt.json');assert sha(deployed['target'])==deployed['deployedSha256']=='6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0';forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');assert sha(forge)=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
summary={'repository':'https://github.com/qyby123456/ft.git','visibility':'public','defaultBranch':'main','baseCommit':base,'localBranch':'codex/neoforge-source-20261113','importPrefix':'neoforge/ftboss','sourceFiles':synced['sourceFiles'],'stagedFiles':synced['stagedFiles'],'everySourceAndStagedBlobSha256Verified':True,'allStagedFilesSecretPatternAndPrivacyPathChecked':True,'detectorControls':13,'exactReviewNegativeControls':2,'exactByteMetadataReviews':3,'unreviewedFindings':0,'knownStoredAccountReceivePackIpv4DryRunAccepted':True,'commitAuthorNameEmailMissing':True,'commitsCreated':0,'pushesPerformed':0,'remoteRecoveryVerified':False,'localOriginalsDeleted':0,'formalInstancesUntouched':True,'forgeSha256Unchanged':sha(forge),'deployedStage27Sha256Unchanged':sha(deployed['target']),'noTokensOrPrivateAccountConfigurationCopied':True,'fullBossGoalStatus':'active','weaponsPaused':True,'nextRequiredInput':'Explicit Git commit author name andemail;then verified authorized bounded commits/pushes andfresh remote retrieval hashes.'};save(OUT/'source-ready-receipt.json',summary)
files={Path(__file__),helper,ROOT/'.gitignore',ROOT/'.gitattributes',ROOT/'github-sync/GITHUB_RESTORE_AND_SYNC.md',ROOT/'github-sync/reviewed-scan-exceptions.json',ROOT/'github-sync/current-target-ft.json',OUT/'source-ready-receipt.json',CHECKOUT/'neoforge/ftboss/github-sync/import-manifest-ft-20261113.json'}
for relative,row in expected.items():assert sha(ROOT/relative)==row['sha256'];files.add(ROOT/relative)
for pattern in ['reports/github-ft-*','reports/github-native-ignore-*','reports/github-source-snapshot-*','build/github-native-ignore-coverage-v1']:
    for directory in WORK.glob(pattern):
        if directory.is_dir():
            for path in directory.rglob('*'):
                if path.is_file() and '.git' not in path.parts:files.add(path)
result=archive(WORK/'archives/github-ft-source-ready-author-pending-20261113.zip',files);save(OUT/'archive-receipt.json',result);assert sha(deployed['target'])==deployed['deployedSha256'];processes();print(json.dumps({'sourceReadyAuthorPending':True,'summary':summary,'archive':result}),flush=True)
