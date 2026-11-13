import collections
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
REFERENCE = Path('D:/ft工程文件/结构参考_20261103')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import read_nbt, plain

SELECTIONS = [
    ('rooms/junction_md', 'junction footprint and multi-exit circulation', 'same', 'Preserve 19x16x19 envelope; normalize offset interfaces to central N/S/E; cap W; replace junction centerpiece with rooted wetland tree.'),
    ('rooms/armoury_md', 'single-entry functional side room', 'same', 'Preserve 13x8x13 and single north entrance; center the off-axis connector; replace racks with dry stone worktable and empty storage.'),
    ('rooms/crypt_sm', 'small single-entry terminal chamber', 'same', 'Preserve 7x8x7 envelope and north-only topology; move connector from x4 to x3, feet y1 to y2; remove tomb gameplay.'),
    ('rooms/prison_lg', 'axial north/south circulation and side bays', 'same', 'Preserve 19x8x19 envelope and N/S topology; replace cells with paired supported column rows; align both portals to x9.'),
    ('hallways/hallway_2', 'repeated straight circulation module', 'straight', 'Source 5x8x5; rotate its active W/E hall semantics to N/S, repeat three segments and add two-block wetland margins: 9x8x19.'),
    ('hallways/hallway_turn', 'turn module separated from wall/doorway sockets', 'turn', 'Source 5x8x5; widen each axis to 2*5+3=13 for dry 3-wide bend plus wall/lamps; select N/E as external connection faces. Source raw faces are NOT claimed unchanged.'),
    ('rooms/armoury_md', 'threshold chamber plus separate doorway semantics', 'same', 'Preserve 13x8x13 envelope; replace armoury furnishings with guard pedestals; add declared south exit for entrance vestibule use.'),
    ('rooms/junction_md', 'multi-exit central open gathering space', 'same', 'Preserve 19x16x19 envelope; remove roof interior only for courtyard; normalize to N/S/E, cap W and separate dry routes from pools.'),
    ('rooms/treasure_room_lg', 'terminal treasure chamber plus detail sockets', 'same', 'Preserve 19x16x19 envelope and treasure function; omit all internal jigsaw detail sockets; replace elevated east entry with safe floor-level north entry; empty chest only, no loot or traps.'),
]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    modules = []
    for index, (relative, role, transform, changes) in enumerate(SELECTIONS):
        detail_path = REFERENCE / 'nbt-details/better_strongholds/betterstrongholds' / (relative + '.json')
        detail = json.loads(detail_path.read_text(encoding='utf-8'))
        source_path = REFERENCE / detail['path']
        raw = plain(read_nbt(source_path)[1])
        if raw['size'] != detail['size']:
            raise ValueError('Raw NBT and published details differ: ' + relative)
        counts = collections.Counter(raw['palette'][entry['state']]['Name'] for entry in raw['blocks'])
        connectors = [{'pos': entry['pos'], 'state': raw['palette'][entry['state']], 'nbt': entry['nbt']} for entry in raw['blocks'] if raw['palette'][entry['state']]['Name'] == 'minecraft:jigsaw']
        dimensions = list(raw['size'])
        if transform == 'straight':
            dimensions = [dimensions[0] + 4, dimensions[1], dimensions[2] * 3 + 4]
        elif transform == 'turn':
            dimensions = [dimensions[0] * 2 + 3, dimensions[1], dimensions[2] * 2 + 3]
        modules.append({'id': f'NAT-B1-G4-{index + 1:02}', 'source_nbt': str(source_path), 'source_sha256': digest(source_path), 'source_details': str(detail_path), 'source_details_sha256': digest(detail_path), 'source_size': raw['size'], 'output_size': dimensions, 'source_palette_names': sorted(counts), 'source_top_blocks': counts.most_common(12), 'source_connectors_raw': connectors, 'inherited_role': role, 'declared_changes': changes})
    summaries = [{'path': str(REFERENCE / name), 'sha256': digest(REFERENCE / name)} for name in ('README.md', 'LEARNING_NOTES.md', 'SAMPLE_CATALOG.md', 'EVIDENCE.md')]
    result = {'authority': 'Raw extracted Mod NBT plus local extraction summaries; images are secondary mood references only.', 'method': 'New procedural geometry derived from verified envelopes and documented topology, NOT raw asset copy or runtime port.', 'summaries': summaries, 'modules': modules, 'runtime_boundary': 'Original YUNG jigsaw/pool/processor behavior is not reproduced. External explicit ports in room.json replace jigsaws. No processor placeholders are copied as visual material.', 'license': 'Local private study; no redistribution authorization inferred.'}
    (ROOT / 'source-evidence.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('Verified raw NBT evidence: ' + str(len(modules)) + ' mappings')


if __name__ == '__main__':
    main()
