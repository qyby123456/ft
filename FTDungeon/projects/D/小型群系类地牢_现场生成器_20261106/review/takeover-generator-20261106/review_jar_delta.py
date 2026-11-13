import hashlib
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    old_base = ROOT / 'runtime/next-commit'
    new_base = ROOT / 'runtime/next-commit-iofix'
    old_ready = load(old_base / 'ready.json')
    new_ready = load(new_base / 'ready.json')
    old_path = old_base / old_ready['artifact']
    new_path = new_base / new_ready['artifact']
    assert sha(old_path.read_bytes()) == old_ready['artifactSha256']
    assert sha(new_path.read_bytes()) == new_ready['artifactSha256']
    with zipfile.ZipFile(old_path) as old, zipfile.ZipFile(new_path) as new:
        assert old.testzip() is None and new.testzip() is None
        old_names, new_names = set(old.namelist()), set(new.namelist())
        added, removed = sorted(new_names - old_names), sorted(old_names - new_names)
        changes = [name for name in sorted(old_names & new_names) if old.read(name) != new.read(name)]
        assert not added and not removed
        assert set(changes) == {'META-INF/mods.toml', 'cn/ft/biomedungeon/runtime/UnifiedEngine.class'}
        assert not any('GameTests.class' in name or 'CommandIoFailureTest' in name for name in new_names)
        resources = {}
        mapping = load(ROOT / 'catalog/s8-07-doorway-review-v5/nine-room-package-v5/source-map.json')
        for source in mapping:
            name = 'data/ftbd_candidate/' + source['candidateId'] + '.nbt'
            resources[name] = sha(new.read(name))
            assert resources[name] == source['candidateSha256']
        core_names = [name for name in new_names if name.startswith('cn/ft/biomedungeon/core/') and name.endswith('.class')]
        assert core_names and all(old.read(name) == new.read(name) for name in core_names)
    result = dict(status='INDEPENDENT_PACKAGED_DELTA_PASS_NOT_WORLD_ACCEPTANCE',
                  oldJarSha256=old_ready['artifactSha256'], newJarSha256=new_ready['artifactSha256'],
                  changedEntries=changes, addedEntries=added, removedEntries=removed,
                  unchangedCoreClasses=len(core_names), exactNineResourceHashes=resources,
                  noTestClassesAdded=True, completeGeneratorReady=False, productionPromotion=False)
    Path(__file__).with_name('jar-delta-review.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(dict(status=result['status'], changedEntries=changes, coreClasses=len(core_names), sourceRooms=len(resources))))


if __name__ == '__main__':
    main()
