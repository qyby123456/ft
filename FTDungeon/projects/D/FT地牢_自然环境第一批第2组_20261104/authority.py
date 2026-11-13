import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
LIBRARY = Path('D:/ft工程文件/结构参考_20261103')
sys.path.insert(0, str(LIBRARY))
from analyze_structures import NbtReader

MAPPING = [
    ('rooms/junction_md', [19, 16, 19], 'Preserve junction envelope; three dry connectors, one primary traffic floor plus a supported west gallery with stair access; central obelisk, ring routes and screening wall are original desert adaptations.'),
    ('rooms/armoury_md', [13, 8, 13], 'Preserve room envelope and doorway role; replace armoury function with sealed cistern and side furnishings.'),
    ('rooms/crypt_sm', [7, 8, 7], 'Preserve small terminal room envelope; crypt becomes empty storage chamber.'),
    ('rooms/mess', [19, 8, 13], 'Rotate source 13x8x19 envelope by 90 degrees; communal room becomes column hall with opposite dry doorways.'),
    ('hallways/hallway_2', [5, 8, 17], 'Repeat four 5x8x5 traffic cells with shared boundary planes: length 4*(5-1)+1=17; reorient opposite doorway pair to north/south.'),
    ('hallways/hallway_turn', [13, 8, 13], 'Preserve 90-degree route role; enlarge to 3*(5-1)+1=13 on each horizontal axis for readable L shape; rotate route to north/east.'),
    ('rooms/armoury_md', [13, 10, 13], 'Preserve 13x13 footprint; add two vertical blocks for guardian plinths and supported lamps; add south continuation.'),
    ('rooms/junction_md', [19, 16, 19], 'Preserve junction envelope and four traffic branches; one safe level, open central roof and sealed oasis basin.'),
    ('rooms/treasure_room_lg', [19, 16, 19], 'Preserve treasure envelope and terminal role; replace raised side entry with centered ground-level north entry for FT safety; remove original loot/runtime sockets.'),
]


def prepare():
    evidence = []
    for index, (relative, size, adaptation) in enumerate(MAPPING):
        detail_path = LIBRARY / 'nbt-details/better_strongholds/betterstrongholds' / (relative + '.json')
        details = json.loads(detail_path.read_text(encoding='utf-8'))
        original_path = LIBRARY / details['path']
        reader = NbtReader(original_path.read_bytes())
        assert reader.number('>B') == 10
        reader.string()
        template = reader.value(10)
        assert template['size'] == details['size']
        assert len(template['blocks']) == details['block_records']
        evidence.append({'module': f'NAT1-G2-{index + 1:02}', 'source_nbt': str(original_path), 'source_nbt_sha256': hashlib.sha256(original_path.read_bytes()).hexdigest(), 'source_details': str(detail_path), 'source_size': template['size'], 'source_connectors': details['connectors'], 'source_palette': template['palette'], 'source_function': relative, 'output_size': size, 'retained_and_adapted': adaptation, 'connector_conversion': 'Source jigsaw semantic sockets are evidence, NOT copied literally: original offsets/orientations may describe wall/decor sockets. FT gets centered boundary feet coordinates y=1, dry 3x3 apertures and outward normals in room.json. No Jigsaw/YUNG runtime compatibility claimed.', 'palette_conversion': {'stone_bricks': 'sandstone / smooth_sandstone', 'chiseled_stone_bricks': 'chiseled_sandstone', 'stone_brick_wall': 'solid cut_sandstone framing', 'yellow_stained_glass': 'omit placeholder; do not treat as decorative material', 'cyan_concrete': 'omit terrain-safety placeholder; no processor execution', 'spawner/infested blocks/loot': 'omit; no automatic enemies or rewards'}, 'authority_notes': str(LIBRARY / 'LEARNING_NOTES.md')})
    target = ROOT / 'authority-evidence.json'
    target.write_text(json.dumps({'status': 'SOURCE_NBT_READ_AND_MATCHED_TO_CATALOG', 'sources_read_before_revised_build': True, 'original_draft': 'Prior image-derived draft superseded; not accepted or deployed', 'scope': 'Local research only; original geometry and mod code not redistributed; independently constructed adaptations.', 'modules': evidence}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return evidence


if __name__ == '__main__':
    prepare()
