import hashlib
import json
from pathlib import Path
import sys
import zipfile


project = Path(__file__).resolve().parent.parent
baseline = Path(sys.argv[1])
expected = 'adfedf3d980cc2f25e6d040178e3f921f4de414aa8609ceef68c588d96bb6ec6'
if hashlib.sha256(baseline.read_bytes()).hexdigest() != expected:
    raise SystemExit('Installed baseline changed; merge latest version first')
built = project / 'build/libs/ft-dungeon-mobs-forge-1.20.1-0.24.1.jar'
release = project / 'release'
release.mkdir(exist_ok=True)
destination = release / 'FT地牢-Forge-1.20.1-0.24.1.jar'
overrides = {
    'META-INF/MANIFEST.MF',
    'META-INF/mods.toml',
    'net/farmingtales/ftdungeonmobs/generator/ChaosDungeonGenerator.class',
    'net/farmingtales/ftdungeonmobs/generator/ChaosDungeonGenerator$State.class',
    'net/farmingtales/ftdungeonmobs/generator/ChaosSpawnRoom.class',
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
    if any('ChaosSpawnRoomTests' in name for name in output.namelist()):
        raise SystemExit('Test fixture leaked into release')
    changed = [name for name in output.namelist() if name not in original.namelist()
               or output.read(name) != original.read(name)]
result = {
    'version': '0.24.1',
    'baselineSha256': expected,
    'sha256': hashlib.sha256(destination.read_bytes()).hexdigest(),
    'preservedBaselineEntries': len(preserved),
    'changedEntries': changed,
    'nativeGameTest': 'gametest-spawn-attempt2.log: all 1 required tests passed',
    'layoutsWithWalkableConnection': 4,
    'nativeEntryReturnAndUnsafeFloorRejection': True,
    'inheritedArchitectureCheckExcluded': True,
    'fullModpackClientTested': False,
}
(release / 'verification.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False, indent=2))
