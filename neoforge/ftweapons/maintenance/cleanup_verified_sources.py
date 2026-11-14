import argparse
import json
import os
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
helper = ROOT / 'work/session-boss/tools/persist_ftweapons_source_v1.py'
namespace = {'__file__': str(helper), '__name__': 'persistence_helpers_only'}
exec(compile(helper.read_text(encoding='utf-8').split('parser = argparse')[0], str(helper), 'exec'), namespace)
load, save, sha, git, owner = (namespace[name] for name in ['load', 'save', 'sha', 'git', 'owner'])
OUT, REPO, PREFIX = (namespace[name] for name in ['OUT', 'REPO', 'PREFIX'])
ROOTS = ['work/session-lease', 'work/session-chain', 'work/session-continuity', 'work/session-projection', 'batches']
FORMAL = {
    'C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar': '6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0',
    'C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar': '7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
}

def guard():
    owner()
    for name, expected in FORMAL.items():
        assert sha(name) == expected
    query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^javaw?\\.exe$' -and $_.CommandLine -match 'session-boss|session-lease|FTBoss-NeoForge-1.21.1-移植-20261022' } | Select-Object ProcessId,Name | ConvertTo-Json -Compress"
    result = subprocess.run(['powershell', '-NoProfile', '-Command', query], capture_output=True, encoding='utf-8-sig', timeout=30)
    assert result.returncode == 0 and not result.stdout.strip(), 'Owned native process still present'

def plan():
    guard()
    assert not (OUT / 'canonical-cleanup-plan.json').exists()
    proof = load(OUT / 'remote-proof-before-cleanup.json')
    assert proof['independentRemoteFetch'] and proof['bossSubtreeUnchanged']
    assert git(REPO, 'rev-parse', 'HEAD').decode().strip() == proof['commit']
    source_map = load(OUT / 'source-map.json')['files']
    originals = {}
    for name, row in source_map.items():
        if not row['original']:
            continue
        path = Path(row['original']).resolve()
        if not path.is_relative_to(ROOT):
            continue
        assert any(path.is_relative_to((ROOT / relative).resolve()) for relative in ROOTS)
        assert path.is_file() and not path.is_symlink() and sha(path) == row['sha256']
        relative = path.relative_to(ROOT).as_posix()
        if relative not in originals:
            originals[relative] = {'bytes': row['bytes'], 'sha256': row['sha256'], 'remotePath': name}
        else:
            assert originals[relative]['sha256'] == row['sha256']
    code = []
    for folder in [ROOT / 'work/session-boss/tools', ROOT / 'work/session-boss/tests', ROOT / 'work/session-boss/boss-core', ROOT / 'tools']:
        for base, directories, names in os.walk(folder):
            directories[:] = [name for name in directories if name not in {'build', 'classes', '__pycache__', 'cache'}]
            for name in names:
                path = Path(base) / name
                if path.suffix.lower() in {'.py', '.java', '.ps1', '.gradle', '.toml'}:
                    code.append(path.read_text(encoding='utf-8-sig', errors='replace'))
    text = '\n'.join(code)
    removed, retained = {}, {}
    for relative, row in originals.items():
        path = ROOT / relative
        reason = None
        if path.name == 'CURRENT_INCREMENTAL_BATCH.json':
            reason = 'Paused workstream control state retained'
        elif path.name in text:
            reason = 'Basename referenced in preserved Boss or shared tools; conservatively retained'
        if reason:
            retained[relative] = {**row, 'reason': reason}
        else:
            removed[relative] = row
    assert 'work/session-lease/tools/reactivate_goal_after_boss_stage12.py' in retained
    assert all((ROOT / name).suffix.lower() in {'.py', '.java', '.md', '.json', '.in', '.mf'} for name in removed)
    result = {'targetRelativePath': '.', 'verifiedSourceCommit': proof['commit'], 'deleteFiles': removed, 'retainedSharedInputs': retained, 'explicitUserAuthorizedCanonicalWeaponSourceCleanup': True, 'binaryArtifactsWorldsArchivesFormalInstancesAndDExcluded': True}
    save(OUT / 'canonical-cleanup-plan.json', result)
    print(json.dumps({'plannedSourceFiles': len(removed), 'plannedLogicalBytes': sum(row['bytes'] for row in removed.values()), 'retainedSharedOrControlInputs': len(retained)}), flush=True)

def cleanup():
    guard()
    assert not (OUT / 'canonical-cleanup-result.json').exists()
    plan = load(OUT / 'canonical-cleanup-plan.json')
    proof = load(OUT / 'remote-proof-before-cleanup.json')
    assert proof['commit'] == plan['verifiedSourceCommit'] == git(REPO, 'rev-parse', 'HEAD').decode().strip()
    assert not git(REPO, 'status', '--porcelain').strip()
    for name, row in plan['deleteFiles'].items():
        path = (ROOT / name).resolve()
        assert any(path.is_relative_to((ROOT / relative).resolve()) for relative in ROOTS)
        assert path.is_file() and not path.is_symlink() and sha(path) == row['sha256'] and path.stat().st_size == row['bytes']
        assert proof['files'][row['remotePath']] == {'bytes': row['bytes'], 'sha256': row['sha256']}
    checker = ROOT / 'work/session-boss/tools/check_github_source_cache_handles_v1.ps1'
    for relative in ROOTS:
        entries = {name[len(relative) + 1:]: row for name, row in plan['deleteFiles'].items() if name.startswith(relative + '/')}
        if not entries:
            continue
        label = Path(relative).name
        lock_plan = OUT / ('canonical-lock-plan-' + label + '.json')
        save(lock_plan, {'targetRelativePath': relative, 'deleteFiles': entries})
        locks = subprocess.run(['powershell', '-NoProfile', '-File', str(checker), '-PlanPath', str(lock_plan)], capture_output=True, timeout=50)
        (OUT / ('canonical-lock-' + label + '.stdout')).write_bytes(locks.stdout)
        (OUT / ('canonical-lock-' + label + '.stderr')).write_bytes(locks.stderr)
        assert locks.returncode == 0, 'File handle or referencing process guard failed; no canonical files removed'
    removed = []
    for name, row in plan['deleteFiles'].items():
        path = (ROOT / name).resolve()
        assert path.is_relative_to(ROOT) and not path.is_symlink() and sha(path) == row['sha256']
        path.unlink()
        removed.append(name)
        if len(removed) % 100 == 0:
            save(OUT / 'canonical-cleanup-progress.json', {'removed': removed})
    directories = sorted({(ROOT / name).parent for name in removed}, key=lambda path: len(path.parts), reverse=True)
    for directory in directories:
        while directory != ROOT and directory.as_posix() not in {(ROOT / relative).as_posix() for relative in ROOTS}:
            assert any(directory.resolve().is_relative_to((ROOT / relative).resolve()) for relative in ROOTS)
            if not directory.exists() or any(directory.iterdir()):
                break
            directory.rmdir()
            directory = directory.parent
    for name, row in plan['retainedSharedInputs'].items():
        assert sha(ROOT / name) == row['sha256']
    guard()
    result = {'verifiedSourceCommit': proof['commit'], 'removedCanonicalWeaponSourceFiles': len(removed), 'removedCanonicalLogicalBytes': sum(row['bytes'] for row in plan['deleteFiles'].values()), 'retainedSharedOrControlSourceFiles': len(plan['retainedSharedInputs']), 'allRemovedFilesHadExactRemoteRecoveryProof': True, 'archivesJarsWorldsAndFormalInstancesRemoved': 0, 'formalInstancesUnchanged': True, 'weaponDevelopmentPaused': True, 'installable': False, 'deletedPaths': removed}
    save(OUT / 'canonical-cleanup-result.json', result)
    print(json.dumps({key: value for key, value in result.items() if key != 'deletedPaths'}), flush=True)

parser = argparse.ArgumentParser()
parser.add_argument('operation', choices=['plan', 'cleanup'])
args = parser.parse_args()
plan() if args.operation == 'plan' else cleanup()
