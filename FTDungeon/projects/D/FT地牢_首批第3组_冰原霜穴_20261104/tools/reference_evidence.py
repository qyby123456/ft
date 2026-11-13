import collections
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LIBRARY = Path('D:/ft工程文件/结构参考_20261103')
sys.path.insert(0, str(Path.home() / 'AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import plain, read_nbt

SOURCES = [
    ('01', 'rooms/junction_md', [1, 1, 1], 'Three-way main chamber; central natural feature replaces junction decor.'),
    ('02', 'rooms/mess', [1, 1, 1], 'Functional side furnishings with through circulation; original mess becomes frozen rest room.'),
    ('03', 'rooms/storeroom', [1, 1, 1], 'Compact dead-end storage, empty chest and wall shelving.'),
    ('04', 'portal_rooms/portal_room', [1, 1, 1], 'Long axial hall; portal and gameplay machinery removed, columns remain original design.'),
    ('05', 'hallways/hallway_2', [1, 1, 5], 'Five hallway bays in a straight run; dimensions multiplied only along Z.'),
    ('06', 'hallways/hallway_turn', [3, 1, 3], 'Three-by-three envelope of a real 5-block turn module; L footprint instead of square room.'),
    ('07', 'rooms/armoury_md', [1, 1, 1], 'Guard/armoury function adapted to entrance vestibule; no item frames or rewards.'),
    ('08', 'rooms/junction_md', [1, 1, 1], 'Junction envelope and branching; intentional skylight and snow tree replace upper level.'),
    ('09', 'rooms/treasure_room_lg', [1, 1, 1], 'Treasure dead end with central raised focal point; no loot generation.'),
]

def main():
    catalog = json.loads((LIBRARY / 'catalog/templates.json').read_text(encoding='utf-8'))
    entries = []
    for code, relative, scale, adaptation in SOURCES:
        identifier = 'betterstrongholds:' + relative
        record = next(item for item in catalog if item['id'] == identifier)
        source = LIBRARY / record['path']
        template = plain(read_nbt(source)[1])
        palette = template['palette']
        counts = collections.Counter(palette[block['state']]['Name'] for block in template['blocks'])
        connectors = [dict(position=block['pos'], state=palette[block['state']], nbt=block.get('nbt', {})) for block in template['blocks'] if palette[block['state']]['Name'] == 'minecraft:jigsaw']
        dimensions = [template['size'][axis] * scale[axis] for axis in range(3)]
        assert template['size'] == record['size']
        entries.append(dict(code=code, source_id=identifier, source_path=str(source), source_sha256=hashlib.sha256(source.read_bytes()).hexdigest(),
            details_path=str(LIBRARY / record['details']), source_size=template['size'], size=dimensions, scale=scale,
            connectors=connectors, source_material_counts=dict(counts.most_common()), adaptation=adaptation,
            interface_adaptation='Original jigsaw metadata retained here as evidence only. Replaced by FT north-centred 3x4 clear entrances at feet Y=1; not YUNG runtime compatible.',
            palette_adaptation='Retain vanilla stone_bricks/cracked/chiseled bricks and spruce support language; add snow_block/packed_ice/blue_ice for frost theme. Omit spawners, jigsaws, commands, entities, loot tables and original processors.'))
    document = dict(library=str(LIBRARY), summaries=[str(LIBRARY / name) for name in ['README.md', 'LEARNING_NOTES.md', 'SAMPLE_CATALOG.md', 'EVIDENCE.md']],
        scope='Original procedural adaptation informed by parsed real NBT; not a copied original template and not reconstructed YUNG gameplay.', entries=entries)
    target = ROOT / 'evidence/reference-evidence.json'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(document, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps([dict(code=entry['code'], source=entry['source_id'], source_size=entry['source_size'], size=entry['size'], jigsaws=len(entry['connectors'])) for entry in entries], ensure_ascii=False))

if __name__ == '__main__':
    main()
