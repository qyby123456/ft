import difflib
import hashlib
import json
import subprocess
import tempfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TARGET = Path('src/main/java/net/farmingtales/ftdungeonmobs')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    folder = ROOT / 'integration'
    candidates = {
        'DungeonRoomCatalog.java': ROOT / 'src/compat/java/net/farmingtales/ftdungeonmobs/DungeonRoomCatalog.java',
        'DungeonRoomLayout.java': ROOT / 'src/compat/java/net/farmingtales/ftdungeonmobs/DungeonRoomLayout.java',
        'DungeonSpawnPoints.java': folder / 'source-after/DungeonSpawnPoints.java',
    }
    changes = []
    manifest = []
    for name, candidate in candidates.items():
        baseline = folder / 'source-before' / name
        before = baseline.read_text(encoding='utf-8-sig')
        after = candidate.read_text(encoding='utf-8-sig')
        if before == after:
            raise ValueError('Missing candidate change: ' + name)
        target = (TARGET / name).as_posix()
        changes.extend(difflib.unified_diff(before.splitlines(keepends=True), after.splitlines(keepends=True), fromfile='a/' + target, tofile='b/' + target))
        snapshot = folder / 'source-after' / name
        if snapshot != candidate:
            snapshot.write_bytes(candidate.read_bytes())
        manifest.append({'target': target, 'before_sha256': digest(baseline), 'after_sha256': digest(snapshot), 'baseline': 'integration/source-before/' + name, 'candidate': 'integration/source-after/' + name})
    patch = folder / 'shop-import.patch'
    patch.write_text(''.join(changes), encoding='utf-8', newline='\n')
    with tempfile.TemporaryDirectory(prefix='ftshop-patch-', dir=ROOT / 'qa') as temporary:
        sandbox = Path(temporary).resolve()
        if not sandbox.is_relative_to((ROOT / 'qa').resolve()):
            raise ValueError('Patch sandbox must remain inside the independent QA directory')
        (sandbox / TARGET).mkdir(parents=True)
        for name in candidates:
            (sandbox / TARGET / name).write_text((folder / 'source-before' / name).read_text(encoding='utf-8-sig'), encoding='utf-8', newline='\n')
        for arguments in (['--check'], []):
            subprocess.run(['git', 'apply', *arguments, str(patch)], cwd=sandbox, check=True, capture_output=True)
        for name, candidate in candidates.items():
            if (sandbox / TARGET / name).read_text(encoding='utf-8') != candidate.read_text(encoding='utf-8-sig'):
                raise ValueError('Patch roundtrip differs: ' + name)
    result = {
        'status': 'CANDIDATE_NOT_APPLIED_TO_SHARED_CORE',
        'patch_sha256': digest(patch),
        'patch_roundtrip_on_snapshot': 'PASS',
        'files': manifest,
        'policy': {'shop_always_excluded': True, 'explicit_spawn_points_false_excluded': True, 'legacy_missing_flag_default_enabled': True, 'unapproved_mod_blocks_still_rejected': True, 'only_mod_block_allowlisted': 'ftdungeontrading:trading_table'},
        'limitations': ['Snapshot-based patch; receiver must rebase against current shared core and run its full regression tests.', 'Catalog and layout candidates are compiled and exercised by isolated Forge QA. The full DungeonSpawnPoints handler is not compiled or executed here.', 'Spawn catalog is cached for the current round. Restart or begin a new round after importing metadata; this patch does not provide hot-reload invalidation.', 'This excludes FT encounter points, not vanilla natural spawning, external mob systems or previously existing entities.'],
    }
    (folder / 'patch-manifest.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': result['status'], 'snapshot_apply_check': 'PASS', 'files': len(manifest), 'patch_sha256': result['patch_sha256']}))


if __name__ == '__main__':
    main()
