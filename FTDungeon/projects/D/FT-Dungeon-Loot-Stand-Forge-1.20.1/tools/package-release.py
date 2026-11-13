import hashlib
import json
import re
import shutil
import zipfile
from pathlib import Path


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read_log(path):
    payload = path.read_bytes()
    return payload.decode('utf-16' if payload[:2] in (b'\xff\xfe', b'\xfe\xff') else 'utf-8', errors='replace')


def archive(output, entries):
    with zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as bundle:
        for path, name in sorted(entries, key=lambda entry: entry[1]):
            bundle.write(path, name)
    with zipfile.ZipFile(output) as bundle:
        assert bundle.testzip() is None


def main():
    root = Path(__file__).resolve().parents[1]
    destination = root.parent / '地牢随机战利品展示台_独立候选_20261103'
    output = destination.with_suffix('.zip')
    if destination.exists() or output.exists():
        raise FileExistsError('Release destination already exists; inspect rather than overwrite')
    build = read_log(root / 'verification/build-final.log')
    tests = read_log(root / 'verification/gametest-final.log')
    assert 'BUILD SUCCESSFUL' in build and 'All 3 required tests passed' in tests
    counts = [int(value) for value in re.findall(r'LOOT_[A-Z_]+_PASS assertions=(\d+)', tests)]
    assert sum(counts) == 78
    for name in ['native-roundtrip.json', 'resources.json', 'minecraft-model-parser.json']:
        assert json.loads((root / 'verification' / name).read_text(encoding='utf-8'))['passed']
    jar = root / 'build/libs/ft-dungeon-loot-stand-forge-1.20.1-0.1.0.jar'
    with zipfile.ZipFile(jar) as bundle:
        names = bundle.namelist()
        assert bundle.testzip() is None
        assert not any('LootRuntimeTest' in name or 'test_fixed' in name or 'test_overflow' in name or name.endswith('/test.json') for name in names)
        assert 'assets/ftdungeonloot/models/block/loot_stand.json' in names
        assert 'net/farmingtales/ftdungeonloot/DungeonRounds.class' in names
        assert 'modId="kubejs"' in bundle.read('META-INF/mods.toml').decode('utf-8')
    destination.mkdir()
    def copy(source, relative):
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
    copy(jar, '模组/' + jar.name)
    copy(root / 'README.md', 'README.md')
    copy(root / 'docs/DATAPACK.md', 'docs/DATAPACK.md')
    copy(root / 'model_sources/地牢随机战利品展示台_动态版.bbmodel', '模型源文件/地牢随机战利品展示台_动态版.bbmodel')
    copy(root / 'src/main/resources/assets/ftdungeonloot/textures/block/loot_stand.png', '贴图/loot_stand.png')
    for name in ['动态底座_成品.png', 'model-contact-sheet.png', 'display-contact-sheet.png', 'Blockbench_Display_GUI.png', 'Blockbench_Display_Ground.png']:
        copy(root / 'preview' / name, '预览/' + name)
    for name in ['VERIFICATION.md', 'build-final.log', 'gametest-final.log', 'native-roundtrip.json', 'resources.json',
                 'minecraft-model-parser.json', 'java-model-audit.json', 'bbmodel-audit.json', 'cost-budget.json', 'cost-audit.json']:
        copy(root / 'verification' / name, 'verification/' + name)
    source_entries = []
    for directory in ['src', 'gradle', 'tools', 'model_sources', 'docs']:
        source_entries.extend((path, path.relative_to(root).as_posix()) for path in (root / directory).rglob('*') if path.is_file())
    for name in ['README.md', 'BUILD.ps1', 'build.gradle', 'settings.gradle', 'gradle.properties', '.gitignore']:
        source_entries.append((root / name, name))
    for path in (destination / 'verification').iterdir():
        source_entries.append((path, 'verification/' + path.name))
    for path in (destination / '预览').iterdir():
        source_entries.append((path, 'preview/' + path.name))
    archive(destination / '可构建源码.zip', source_entries)
    datapack_entries = [(root / 'src/main/resources/pack.mcmeta', 'pack.mcmeta')]
    for relative in ['data/ftdungeonloot/loot_stands/ordinary.json', 'data/ftdungeonloot/loot_tables/chests/ordinary.json']:
        datapack_entries.append((root / 'src/main/resources' / relative, relative))
    archive(destination / '配置数据包示例.zip', datapack_entries)
    entries = [{'path': path.relative_to(destination).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)}
               for path in sorted(destination.rglob('*')) if path.is_file()]
    report = {'version': '0.1.0', 'status': 'independent candidate, not deployed', 'gametests': 3, 'assertions': 78,
              'minecraft_client_tested': False, 'ft_round_and_kubejs_integration_tested': False, 'jar_sha256': digest(jar), 'files': entries}
    (destination / '交付清单.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    archive(output, [(path, path.relative_to(destination).as_posix()) for path in destination.rglob('*') if path.is_file()])
    print(json.dumps({'folder': str(destination), 'bundle': str(output), 'bytes': output.stat().st_size,
                      'sha256': digest(output), 'jar_sha256': digest(jar), 'files': len(entries) + 1}, ensure_ascii=True))


if __name__ == '__main__':
    main()
