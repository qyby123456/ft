import argparse
import ctypes
import json
import os
import shutil
import subprocess
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'reports/github-large-artifact-offload-v1-20261114'
helper = WORK / 'tools/offload_archives_to_github_v1.py'
namespace = {'__file__': str(helper), '__name__': 'archive_helpers_only'}
exec(compile(helper.read_text(encoding='utf-8').rsplit('parser = argparse.ArgumentParser()', 1)[0], str(helper), 'exec'), namespace)
load, save, sha, git, owner = (namespace[name] for name in ['load', 'save', 'sha', 'git', 'owner'])
REPO, RECOVERY, BRANCH = (namespace[name] for name in ['REPO', 'RECOVERY', 'BRANCH'])

def guard():
    owner()
    query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^javaw?\\.exe$' -and $_.CommandLine -match 'session-boss|session-lease' } | Select-Object ProcessId | ConvertTo-Json -Compress"
    result = subprocess.run(['powershell', '-NoProfile', '-Command', query], capture_output=True, timeout=30)
    assert result.returncode == 0 and not result.stdout.strip(), 'Project native process active'

def publish():
    guard()
    plan = load(OUT / 'jar-cache-cleanup-plan.json')
    index = load(OUT / 'index-state.json')
    assert not git(REPO, 'status', '--porcelain').strip()
    previous = git(REPO, 'rev-parse', 'HEAD').decode().strip()
    current_index = json.loads(git(REPO, 'show', 'HEAD:archive-storage/INDEX.json'))
    for name, row in plan['deleteFiles'].items():
        asset = index['archives'][row['archive']]
        if row['archive'] in current_index['archives']:
            assert current_index['archives'][row['archive']]['assetId'] == asset['assetId'] and current_index['archives'][row['archive']]['sha256'] == asset['sha256']
        else:
            parallel = load(OUT / 'parallel-preverified-assets' / (asset['sha256'] + '.json'))
            assert parallel['assetId'] == asset['assetId'] and parallel['independentRemoteDownloadVerified'] and parallel['downloadSha256'] == asset['sha256']
        assert asset['remoteVerified'] and asset['independentRemoteDownloadVerified'] and asset['sha256'] == row['archiveSha256']
        proof = load(OUT / 'payload-readback' / row['payloadReadbackProof'])
        assert proof['archiveSha256'] == row['archiveSha256'] and proof['allOwnPayloadsReadBackAndVerified']
        path = (ROOT / name).resolve()
        assert any(path.is_relative_to((ROOT / relative).resolve()) for relative in plan['roots'])
        assert path.is_file() and not path.is_symlink() and path.stat().st_size == row['bytes'] and sha(path) == row['sha256']
    target = REPO / 'archive-storage'
    save(target / 'JAR_CACHE_CLEANUP.json', plan)
    save(target / 'INDEX.json', index)
    maintenance = target / 'maintenance'
    maintenance.mkdir(exist_ok=True)
    for source, name in [('restore_remote_archive_v1.py', 'restore_archive.py'), ('plan_archive_backed_jar_cache_eviction_v1.py', 'plan_jar_cache_cleanup.py'), ('evict_remote_backed_jar_cache_v1.py', 'evict_jar_cache.py'), ('verify_archive_payloads_before_eviction_v1.py', 'verify_archive_payloads.py')]:
        (maintenance / name).write_bytes((WORK / 'tools' / source).read_bytes())
    proof_target = target / 'payload-readback'
    proof_target.mkdir(exist_ok=True)
    for path in (OUT / 'payload-readback').glob('*.json'):
        (proof_target / path.name).write_bytes(path.read_bytes())
    (target / 'restore-smoke-verification.json').write_bytes((OUT / 'restore-smoke-verification.json').read_bytes())
    git(REPO, 'add', '--sparse', '-f', '--', 'archive-storage/')
    assert all(name.startswith('archive-storage/') for name in git(REPO, 'diff', '--cached', '--name-only').decode().splitlines())
    scan = OUT / 'jar-cache-source-scan'
    result = subprocess.run([os.sys.executable, '-B', str(WORK / 'tools/verify_github_source_snapshot_v3.py'), '--git-dir', str(REPO / '.git'), '--work-tree', str(REPO), '--source-prefix', 'archive-storage', '--staged', '--output', str(scan)], capture_output=True, timeout=90)
    (OUT / 'jar-cache-source-scan.log').write_bytes(result.stdout + result.stderr)
    assert result.returncode == 0 and git(REPO, 'write-tree').decode().strip() == load(scan / 'verification.json')['indexTree']
    git(REPO, 'commit', '-m', 'Persist recovery mapping before evicting redundant runtime JAR caches')
    head = git(REPO, 'rev-parse', 'HEAD').decode().strip()
    save(OUT / 'jar-cache-local-commit.json', {'commit': head})
    git(REPO, 'push', 'origin', 'HEAD:refs/heads/' + BRANCH, network=True)
    git(RECOVERY, 'fetch', '--no-tags', 'origin', 'refs/heads/' + BRANCH, network=True, public=True)
    assert git(RECOVERY, 'rev-parse', 'FETCH_HEAD').decode().strip() == head
    assert git(RECOVERY, 'ls-remote', 'origin', 'refs/heads/' + BRANCH, network=True, public=True).split()[0].decode() == head
    assert not (RECOVERY / '.git/objects/info/alternates').exists()
    for entry in git(REPO, 'ls-tree', '-r', '-z', head, '--', 'archive-storage').split(b'\0'):
        if not entry:
            continue
        metadata, name = entry.split(b'\t', 1)
        path = name.decode('utf-8')
        assert git(RECOVERY, 'show', head + ':' + path) == (REPO / path).read_bytes(), path
    for subtree in ['neoforge/ftboss', 'neoforge/ftweapons']:
        assert git(RECOVERY, 'rev-parse', previous + ':' + subtree) == git(RECOVERY, 'rev-parse', head + ':' + subtree)
    git(RECOVERY, 'fsck', '--full')
    current = git(RECOVERY, 'rev-parse', 'HEAD').decode().strip()
    git(RECOVERY, 'update-ref', 'refs/heads/' + BRANCH, head, current)
    proof = {'commit': head, 'planSha256': sha(target / 'JAR_CACHE_CLEANUP.json'), 'independentRemoteGitFetchVerified': True, 'allArchiveStorageFilesExactBytesVerified': True, 'everyPlannedJarHasFullyReadbackVerifiedRemoteArchive': True, 'bossAndWeaponSubtreesUnchanged': True, 'plannedFiles': len(plan['deleteFiles']), 'logicalBytes': plan['logicalBytes']}
    save(OUT / 'jar-cache-remote-proof.json', proof)
    print(json.dumps(proof), flush=True)

def unlink_readonly_without_mutating_other_links(path):
    kernel = ctypes.WinDLL('kernel32', use_last_error=True)
    kernel.CreateFileW.argtypes = [ctypes.c_wchar_p, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p]
    kernel.CreateFileW.restype = ctypes.c_void_p
    kernel.SetFileInformationByHandle.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_void_p, ctypes.c_uint32]
    kernel.SetFileInformationByHandle.restype = ctypes.c_int
    kernel.CloseHandle.argtypes = [ctypes.c_void_p]
    handle = kernel.CreateFileW(str(path), 0x10000, 7, None, 3, 0x00200000, None)
    if handle == ctypes.c_void_p(-1).value:
        raise ctypes.WinError(ctypes.get_last_error())
    try:
        flags = ctypes.c_uint32(0x13)
        if not kernel.SetFileInformationByHandle(handle, 21, ctypes.byref(flags), ctypes.sizeof(flags)):
            raise ctypes.WinError(ctypes.get_last_error())
    finally:
        kernel.CloseHandle(handle)

def cleanup():
    guard()
    assert not (OUT / 'jar-cache-cleanup-result.json').exists()
    free_before = shutil.disk_usage(ROOT).free
    plan = load(OUT / 'jar-cache-cleanup-plan.json')
    proof = load(OUT / 'jar-cache-remote-proof.json')
    assert proof['independentRemoteGitFetchVerified'] and proof['planSha256'] == sha(REPO / 'archive-storage/JAR_CACHE_CLEANUP.json')
    assert git(REPO, 'rev-parse', 'HEAD').decode().strip() == proof['commit']
    completed = load(OUT / 'jar-cache-cleanup-progress.json')['removed'] if (OUT / 'jar-cache-cleanup-progress.json').exists() else []
    assert len(completed) == len(set(completed)) and set(completed).issubset(plan['deleteFiles'])
    for name in completed:
        assert not (ROOT / name).exists()
    for relative in plan['roots']:
        rows = {name[len(relative) + 1:]: row for name, row in plan['deleteFiles'].items() if name.startswith(relative + '/') and name not in completed}
        if not rows:
            continue
        intent_path = OUT / ('jar-cache-intent-' + relative.replace('/', '-') + '.json')
        prior_intent = load(intent_path) if intent_path.exists() else None
        if prior_intent is not None:
            assert prior_intent['verifiedGitCommit'] == proof['commit']
        for name, row in rows.items():
            path = (ROOT / relative / name).resolve()
            if not path.exists():
                assert prior_intent is not None
                continue
            assert path.is_relative_to(ROOT) and path.is_relative_to((ROOT / relative).resolve()) and not path.is_symlink() and path.is_file()
            assert path.stat().st_size == row['bytes'] and sha(path) == row['sha256']
        lock_plan = OUT / ('jar-cache-handles-' + relative.replace('/', '-') + '.json')
        save(lock_plan, {'targetRelativePath': relative, 'deleteFiles': {name:row for name,row in rows.items() if (ROOT / relative / name).exists()}})
        result = subprocess.run(['powershell', '-NoProfile', '-File', str(WORK / 'tools/check_github_source_cache_handles_v1.ps1'), '-PlanPath', str(lock_plan)], capture_output=True, timeout=60)
        lock_plan.with_suffix('.log').write_bytes(result.stdout + result.stderr)
        assert result.returncode == 0, 'Process or file handle guard failed'
        guard()
        save(intent_path, {'verifiedGitCommit': proof['commit'], 'planSha256': proof['planSha256'], 'allPresentPathsHashAndHandleChecked': True})
        for name, row in rows.items():
            path = (ROOT / relative / name).resolve()
            if not path.exists():
                assert prior_intent is not None
                completed.append(relative + '/' + name)
                continue
            assert path.is_relative_to(ROOT) and path.is_relative_to((ROOT / relative).resolve()) and not path.is_symlink() and sha(path) == row['sha256']
            if path.stat().st_file_attributes & 1:
                unlink_readonly_without_mutating_other_links(path)
            else:
                path.unlink()
            assert not path.exists()
            completed.append(relative + '/' + name)
            if len(completed) % 25 == 0:
                save(OUT / 'jar-cache-cleanup-progress.json', {'removed': completed, 'logicalBytes': sum(plan['deleteFiles'][item]['bytes'] for item in completed)})
        save(OUT / 'jar-cache-cleanup-progress.json', {'removed': completed, 'logicalBytes': sum(plan['deleteFiles'][item]['bytes'] for item in completed)})
        print(json.dumps({'cleanedRoot': relative, 'removedFiles': len(completed), 'logicalBytes': sum(plan['deleteFiles'][item]['bytes'] for item in completed)}), flush=True)
    assert set(completed) == set(plan['deleteFiles'])
    guard()
    result = {'commit': proof['commit'], 'removedFiles': len(completed), 'logicalBytesRemoved': plan['logicalBytes'], 'allPathsHaveExactRemoteRecoveryMapping': True, 'worldAndSourceFilesRemoved': 0, 'sharedReadonlyHardlinkAttributesMutated': False, 'formalInstancesModified': False, 'weaponDevelopmentResumed': False, 'freeBytesBefore': free_before, 'freeBytesAfter': shutil.disk_usage(ROOT).free}
    save(OUT / 'jar-cache-cleanup-result.json', result)
    print(json.dumps(result), flush=True)

parser = argparse.ArgumentParser()
parser.add_argument('operation', choices=['publish', 'cleanup'])
arguments = parser.parse_args()
publish() if arguments.operation == 'publish' else cleanup()
