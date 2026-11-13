import hashlib
import importlib.util
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
LIBRARY = Path('D:/ft工程文件/结构参考_20261103')
SOURCES = {
    'junction': 'better_strongholds/data/betterstrongholds/structures/rooms/junction_md.nbt',
    'armoury': 'better_strongholds/data/betterstrongholds/structures/rooms/armoury_md.nbt',
    'store': 'better_strongholds/data/betterstrongholds/structures/rooms/storeroom.nbt',
    'library': 'better_strongholds/data/betterstrongholds/structures/rooms/library_md.nbt',
    'prison': 'better_strongholds/data/betterstrongholds/structures/rooms/prison_lg.nbt',
    'straight': 'stellarity/data/stellarity/structures/stronghold/corridors/straight.nbt',
    'turn': 'stellarity/data/stellarity/structures/stronghold/corridors/turn_1.nbt',
    'mushroom': 'dungeons_arise/data/dungeons_arise/structures/giant_mushroom/red_giant_mushroom_part_2.nbt',
}
ADAPTATIONS = [
    ('NAT7-01', 'junction', 'Preserve exact 19x16x19 junction envelope and four horizontal branches; replace furnishings with supported giant-mushroom focal dais.'),
    ('NAT7-02', 'armoury', '13x8x13 to 17x10x17: +4 lateral and +2 headroom for mushroom alcoves, preserve central circulation and functional side storage.'),
    ('NAT7-03', 'store', '7x8x13 to 11x8x11: widen 4, shorten 2; retain small storage role with empty containers, add east emergency connector.'),
    ('NAT7-04', 'library', '17x8x25 rotated to 25x8x17 then width -2 and height +4; replace stacks with two supported mushroom-column rows; preserve longitudinal aisle.'),
    ('NAT7-05', 'straight', '25x6x7 rotated to 7x6x25, height +2 for fungus caps; preserve endpoint feet y=1, two opposite ports and 25-block run.'),
    ('NAT7-06', 'turn', 'Preserve exact 17x17 footprint, height 6 to 8; convert source south/east off-centre anchors to north/east centre anchors for FT, retain true L-shaped shell.'),
    ('NAT7-07', 'junction', '19x16x19 to 19x10x17: keep width, shorten depth 2 and height 6; retain opposing transition ports, use two guard niches.'),
    ('NAT7-08', 'junction', '19x16x19 to 23x12x23: widen 4, height -4, open central roof only; preserve four-branch junction and peripheral circulation.'),
    ('NAT7-09', 'prison', '19x8x19 to 21x12x19: widen 2, height +4; reuse branch-room envelope, deliberately change function from cells to terminal stepped treasury.'),
]


def collect():
    specification = importlib.util.spec_from_file_location('mod_structure_evidence', LIBRARY / 'analyze_structures.py')
    parser = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(parser)
    records = {}
    for key, relative in SOURCES.items():
        path = LIBRARY / 'unpacked' / relative
        payload = path.read_bytes()
        details = parser.inspect_template(payload)
        records[key] = {'path': str(path), 'sha256': hashlib.sha256(payload).hexdigest(),
                        **{name: details[name] for name in ('size', 'data_version', 'block_records', 'block_counts', 'connectors', 'palette_note')}}
    assert records['junction']['size'] == [19, 16, 19]
    assert records['straight']['size'] == [25, 6, 7]
    assert records['turn']['size'] == [17, 6, 17]
    assert records['mushroom']['block_counts']['minecraft:red_terracotta'] == 2103
    result = {'authority': 'Unpacked real NBT geometry, palette, connector facts; concepts are secondary style references only.',
              'sources': records, 'adaptations': [{'room': room, 'source': source, 'changes': changes} for room, source, changes in ADAPTATIONS],
              'shared_rules': ['Stone-brick structural envelope from YUNG/Stellarity; mushroom_stem, red_terracotta and white_terracotta from actual Dungeons Arise mushroom palette.',
                               'Original jigsaws translated into explicit metadata connections, not copied; no claim of compatibility with YUNG/WDA processors or pools.',
                               'No original blocks, loot, entities, executable tags or module bytes copied; original procedural recipes only.'],
              'notes': [str(LIBRARY / name) for name in ('README.md', 'LEARNING_NOTES.md', 'SAMPLE_CATALOG.md', 'EVIDENCE.md')]}
    (ROOT / 'qa/source-evidence.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return result
