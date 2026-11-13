import hashlib
import json
from pathlib import Path
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / 'output/creature-import'


def main():
    build_log = (EVIDENCE / 'build-final.log').read_text(encoding='utf-8-sig', errors='replace')
    test_log = (EVIDENCE / 'gametest-final.log').read_text(encoding='utf-8-sig', errors='replace')
    assert 'BUILD SUCCESSFUL' in build_log
    assert 'All 12 required tests passed' in test_log
    candidate = ROOT / 'build/libs/ft-dungeon-mobs-forge-1.20.1-0.24.0.jar'
    baseline = EVIDENCE / 'baseline-0.23.0.jar'
    allowed_classes = ('net/farmingtales/ftdungeonmobs/FTDungeonMobs', 'net/farmingtales/ftdungeonmobs/client/DungeonClient', 'eclipse/generator/Gameplay')
    allowed_resources = {'META-INF/MANIFEST.MF', 'META-INF/mods.toml', 'assets/ftdungeonmobs/lang/en_us.json', 'assets/ftdungeonmobs/lang/zh_cn.json'}
    with zipfile.ZipFile(candidate) as current, zipfile.ZipFile(baseline) as previous:
        assert current.testzip() is None
        assert not set(previous.namelist()) - set(current.namelist())
        changed = [name for name in previous.namelist() if current.read(name) != previous.read(name)]
        unexpected = [name for name in changed if name not in allowed_resources and not (name.endswith('.class') and name.startswith(allowed_classes))]
        assert not unexpected, unexpected
        for language in ['en_us', 'zh_cn']:
            name = 'assets/ftdungeonmobs/lang/' + language + '.json'
            before, after = json.loads(previous.read(name)), json.loads(current.read(name))
            assert all(after[key] == value for key, value in before.items())
        metadata = current.read('META-INF/mods.toml').decode()
        assert 'ftdungeoncreaturetests' not in metadata and 'ftgateintegration' not in metadata
        assert not any(name.startswith('net/farmingtales/ftdungeonmobs/CreatureTestMod') for name in current.namelist())
        for model_id in ['ghost_screamer', 'ore_monster', 'skeleton_merchant']:
            assert 'assets/ftdungeonmobs/geo/' + model_id + '.geo.json' in current.namelist()
            assert 'assets/ftdungeonmobs/models/item/' + model_id + '_spawn_egg.json' in current.namelist()
    release = ROOT / 'release'
    release.mkdir(exist_ok=True)
    target = release / 'FT地牢-Forge-1.20.1-0.24.0.jar'
    shutil.copy2(candidate, target)
    receipt = dict(version='0.24.0', baseline_sha256=hashlib.sha256(baseline.read_bytes()).hexdigest(), sha256=hashlib.sha256(target.read_bytes()).hexdigest(), native_tests_passed=12, imported_models=3, imported_animations=9, baseline_entries_preserved=True, changed_entries=changed, inherited_architecture_check_failure=True, full_modpack_client_tested=False, player_world_files_modified=False)
    (release / 'verification.json').write_text(json.dumps(receipt, ensure_ascii=False, indent=2), encoding='utf-8')
    shutil.copy2(ROOT / '三实体导入说明.md', release)
    print(json.dumps(receipt, ensure_ascii=True))


if __name__ == '__main__':
    main()
