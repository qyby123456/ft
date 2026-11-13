import hashlib
import json
from pathlib import Path
import sys
import zipfile


project = Path(__file__).resolve().parent.parent
baseline = Path(sys.argv[1])
expected = '4f9ddcfc060fe56719f5f1fd7c4d1baed62a4b174886b09e7b8329d42c948304'
if hashlib.sha256(baseline.read_bytes()).hexdigest() != expected:
    raise SystemExit('Installed baseline changed; merge latest version first')
built = project / 'build/libs/ft-dungeon-mobs-forge-1.20.1-0.24.3.jar'
release = project / 'release'
release.mkdir(exist_ok=True)
destination = release / 'FT地牢-Forge-1.20.1-0.24.3.jar'
overrides = {
    'META-INF/MANIFEST.MF',
    'META-INF/mods.toml',
    'net/farmingtales/ftdungeonmobs/generator/ChaosDungeonGenerator.class',
    'net/farmingtales/ftdungeonmobs/generator/ChaosDungeonGenerator$State.class',
    'net/farmingtales/ftdungeonmobs/generator/ChaosSpawnRoom.class',
    'net/farmingtales/ftdungeonmobs/generator/DungeonGenerator.class',
    'net/farmingtales/ftdungeonmobs/generator/DungeonGenerators.class',
    'net/farmingtales/ftdungeonmobs/DungeonGateBlockEntity.class',
    'net/farmingtales/ftdungeonmobs/DungeonGateGenerators.class',
    'net/farmingtales/ftdungeonmobs/DungeonGeneratorMenu.class',
    'data/ftmaze/chaos_reset_boxes.json',
    'data/ftmaze/functions/entry/save.mcfunction',
    'data/ftmaze/structures/chaos_spawn_room.nbt',
}
with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(built) as compiled:
    metadata = compiled.read('META-INF/mods.toml').decode('utf-8')
    if 'ftgateintegration' in metadata or 'ftdungeoncreaturetests' in metadata:
        raise SystemExit('Test metadata in release')
    if not overrides.issubset(set(compiled.namelist())):
        raise SystemExit('Compiled release is incomplete')
    with zipfile.ZipFile(destination, 'w', compression=zipfile.ZIP_DEFLATED) as output:
        for entry in original.infolist():
            if entry.filename not in overrides:
                output.writestr(entry, original.read(entry.filename))
        for name in sorted(overrides):
            output.writestr(compiled.getinfo(name), compiled.read(name))
with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(destination) as output:
    preserved = [name for name in original.namelist() if name not in overrides]
    if not all(original.read(name) == output.read(name) for name in preserved):
        raise SystemExit('Unrelated baseline entry changed')
    if any('ChaosSpawnRoomTests' in name or 'ChaosRegenerationTests' in name for name in output.namelist()):
        raise SystemExit('Test fixture leaked into release')
    changed = [name for name in output.namelist() if name not in original.namelist()
               or output.read(name) != original.read(name)]
result = {
    'version': '0.24.3',
    'baselineSha256': expected,
    'sha256': hashlib.sha256(destination.read_bytes()).hexdigest(),
    'preservedBaselineEntries': len(preserved),
    'changedEntries': changed,
    'nativeGameTest': 'gametest-regeneration-r2.log',
    'preservesInstalledBiomeFix': True,
    'inheritedArchitectureCheckExcluded': True,
    'fullModpackClientTested': False,
}
(release / 'verification.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False, indent=2))
