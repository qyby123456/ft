import hashlib
import importlib.util
import json
import os
import subprocess
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'reports/boss-manor-damage-context-source-checkpoint-v2-20261114'
REPO = ROOT / 'github-sync/repositories/ft'
RECOVERY = ROOT / 'github-sync/repositories/ft-checkpoint-recovery-20261113'
BRANCH = 'codex/neoforge-source-20261113'
PREFIX = 'neoforge/ftboss/'
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()
def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
def git(repository, *arguments, network=False, public=False):
    command = ['git', '-C', str(repository)]
    if network:
        command += ['-c', 'http.proxy=' + os.environ['FT_TASK_PROXY'], '-c', 'http.sslBackend=openssl', '-c', 'http.sslVerify=true', '-c', 'http.version=HTTP/1.1']
    if public:
        command += ['-c', 'credential.helper=']
    return subprocess.run(command + list(arguments), capture_output=True, check=True, timeout=90, env=dict(os.environ, GIT_TERMINAL_PROMPT='0', GCM_INTERACTIVE='Never')).stdout
owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == load(ROOT / 'WRITE_OWNER.claim')['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
closeout_path = WORK / 'reports/manor-damage-context-v1-closeout-20261114.json'
closeout = load(closeout_path)
assert closeout['scopedNativeAcceptance'] and closeout['jvmExitCode'] == 0 and closeout['normalCompleteSave']
assert sha(Path(closeout['jar'])) == closeout['jarSha256']
assert not git(REPO, 'diff', '--cached', '--name-only').strip()
assert set(git(REPO, 'diff', '--name-only').decode().splitlines()).issubset({PREFIX + 'AGENTS.md', PREFIX + 'work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md'})
assert not OUT.exists()
OUT.mkdir()
previous = git(REPO, 'rev-parse', 'HEAD').decode().strip()
selected = ['AGENTS.md', 'work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md', 'work/session-boss/MANOR_DAMAGE_CONTEXT_20261114.md', 'work/session-boss/src/manor-damage-context-v1/net/farmingtales/ftboss/boss/ai/NativeManorFinalDamageInstaller.java']
selected += ['work/session-boss/tests/' + name for name in ['SparrowEvasionCallsitesV1.java', 'SparrowEvasionNativeBaselineV1.java', 'ManorDamageContextNativeV1.java', 'ManorDamageContextWrapperProofV1.java', 'ManorDamageLoadedClassAuditV1.java', 'LoadedBossClassCaptureV1.java', 'LoadedBossClassCaptureV2.java', 'LoadedBossClassCaptureV3.java']]
selected += ['work/session-boss/tools/' + name for name in ['audit_boss_sparrow_evasion_effective_v1.py', 'run_boss_sparrow_evasion_native_baseline_v1.py', 'run_boss_sparrow_evasion_native_baseline_v2.py', 'audit_boss_manor_damage_context_v1.py', 'build_boss_manor_damage_context_v1.py', 'run_boss_manor_damage_context_native_v1.py', 'verify_loaded_class_capture_utf8_v1.py', 'verify_source_only_upload_v1.py', 'publish_boss_manor_damage_context_v1.py', 'publish_boss_manor_damage_context_v2.py']]
selected += ['work/session-boss/reports/' + name for name in ['manor-damage-context-v1-closeout-20261114.json', 'sparrow-evasion-native-baseline-v2-39125bde.json', 'manor-damage-context-native-v1-820f458d.json', 'boss-agent-path-encoding-incident-20261114.json', 'boss-agent-path-encoding-recovery-plan-20261114.json']]
selected += ['work/session-boss/build/' + name for name in ['boss-manor-damage-context-v1-20261114/candidate-build-receipt.json', 'boss-manor-damage-context-audit-v1-20261114/audit-inputs.json', 'loaded-class-capture-utf8-v1-20261114/verification.json', 'boss-sparrow-evasion-effective-v1-20261114/summary.json']]
manifest = []
for relative in selected:
    source = ROOT / relative
    assert source.suffix.lower() in ('.java', '.py', '.json', '.md')
    destination_relative = relative
    if relative.startswith('work/session-boss/reports/'):
        destination_relative = 'github-sync/manor-damage-context-20261114/evidence/' + source.name
    elif relative.startswith('work/session-boss/build/'):
        destination_relative = 'github-sync/manor-damage-context-20261114/evidence/' + source.parent.name + '-' + source.name
    destination = REPO / (PREFIX + destination_relative)
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_bytes(source.read_bytes())
    manifest.append({'sourcePath': relative, 'gitPath': PREFIX + destination_relative, 'bytes': source.stat().st_size, 'sha256': sha(source)})
checkpoint_path = PREFIX + 'github-sync/checkpoint-manor-damage-context-20261114.json'
checkpoint = {'sourceOnly': True, 'noModelTextureAnimationOrMusicPayloads': True, 'rawCandidateAndWorldResourcesLocalOnly': True, 'previousCommit': previous, 'acceptedScope': closeout['acceptedScope'], 'candidateSha256': closeout['jarSha256'], 'nativeChecks': closeout['actualPlayerNativeProbeChecks'], 'loadedClassChecks': 22, 'syntheticLifecycleChecks': 72, 'sourceFiles': manifest, 'stage29ScopedAccepted': True, 'nativeLivingDamagePostContractAccepted': False, 'sparrowEvasionPortAccepted': False, 'fullBossMigrationComplete': False, 'installable': False, 'deployed': False, 'weaponsAndSessionPaused': True, 'next': 'Complete original Sparrow evasion and native damage event compatibility, then remaining Boss client/authenticated battle/reward/lifecycle requirements. No deployment or formal overwrites.'}
save(REPO / checkpoint_path, checkpoint)
save(OUT / 'source-manifest.json', checkpoint)
git(REPO, 'add', '--sparse', '-f', '--', *[row['gitPath'] for row in manifest], checkpoint_path)
changed = git(REPO, 'diff', '--cached', '--name-only').decode().splitlines()
assert changed and all(name.startswith(PREFIX) for name in changed)
tree = git(REPO, 'write-tree').decode().strip()
spec = importlib.util.spec_from_file_location('source_only_guard', WORK / 'tools/verify_source_only_upload_v1.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
audit = module.audit(REPO, tree)
save(OUT / 'source-only-staged-audit.json', audit)
assert audit['passed']
scan = OUT / 'source-scan'
result = subprocess.run([os.sys.executable, '-B', str(WORK / 'tools/verify_github_source_snapshot_v3.py'), '--git-dir', str(REPO / '.git'), '--work-tree', str(REPO), '--source-prefix', 'neoforge/ftboss', '--staged', '--output', str(scan)], capture_output=True, timeout=90)
(OUT / 'source-scan.log').write_bytes(result.stdout + result.stderr)
assert result.returncode == 0, 'Review exact source scan failures before publication'
assert git(REPO, 'write-tree').decode().strip() == load(scan / 'verification.json')['indexTree']
for row in manifest:
    assert git(REPO, 'show', ':' + row['gitPath']) == (ROOT / row['sourcePath']).read_bytes()
git(REPO, 'commit', '-m', 'Adapt original Manor damage reductions to balanced NeoForge context')
commit = git(REPO, 'rev-parse', 'HEAD').decode().strip()
save(OUT / 'local-commit.json', {'commit': commit, 'tree': tree})
git(REPO, 'push', 'origin', 'HEAD:refs/heads/' + BRANCH, network=True)
git(RECOVERY, 'fetch', '--no-tags', 'origin', 'refs/heads/' + BRANCH, network=True, public=True)
assert git(RECOVERY, 'rev-parse', 'FETCH_HEAD').decode().strip() == commit
assert git(RECOVERY, 'ls-remote', 'origin', 'refs/heads/' + BRANCH, network=True, public=True).split()[0].decode() == commit
assert not (RECOVERY / '.git/objects/info/alternates').exists()
for row in manifest:
    recovered = git(RECOVERY, 'show', commit + ':' + row['gitPath'])
    assert len(recovered) == row['bytes'] and hashlib.sha256(recovered).hexdigest() == row['sha256']
assert git(RECOVERY, 'show', commit + ':' + checkpoint_path) == (REPO / checkpoint_path).read_bytes()
for subtree in ['neoforge/ftweapons', 'archive-storage', 'README.md']:
    assert git(RECOVERY, 'rev-parse', previous + ':' + subtree) == git(RECOVERY, 'rev-parse', commit + ':' + subtree)
git(RECOVERY, 'fsck', '--full')
current = git(RECOVERY, 'rev-parse', 'HEAD').decode().strip()
git(RECOVERY, 'update-ref', 'refs/heads/' + BRANCH, commit, current)
proof = {'commit': commit, 'independentRemoteFetchVerified': True, 'allSelectedSourceAndTextEvidenceExactBytesVerified': True, 'filesVerifiedIncludingCheckpoint': len(manifest) + 1, 'wholeTreeSourceOnlyGuardPassed': True, 'wholeTreeFilesAudited': audit['filesAudited'], 'weaponAndArchiveStorageTreesUnchanged': True, 'noModelOrMusicFilesUploaded': True, 'stage29ScopedNativeAccepted': True, 'fullBossGoalComplete': False, 'installable': False, 'deployed': False}
save(OUT / 'remote-proof.json', proof)
print(json.dumps(proof), flush=True)
