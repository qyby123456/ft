import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, AIR, plain


def main():
    source = ROOT / 'incoming/S4-10/room.nbt'
    original = Structure.read(source)
    assert original.size == (73, 22, 73) and len(original.blocks) == 117238 and not original.entities
    rebuilt = Structure(original.size, data_version=3465)
    entries = []
    for row_index, south in enumerate(range(0, original.size[2], 48)):
        for column_index, east in enumerate(range(0, original.size[0], 48)):
            order = len(entries) + 1
            origin = (east, 0, south)
            size = (min(48, original.size[0] - east), original.size[1], min(48, original.size[2] - south))
            assert max(size) <= 48
            fragment = Structure(size, data_version=original.data_version)
            for column in range(size[0]):
                for up in range(size[1]):
                    for row in range(size[2]):
                        position = (column + east, up, row + south)
                        fragment.set((column, up, row), original.get(position), original.block_entities.get(position))
            destination = ROOT / f'incoming/S4-10/parts/part-{order:02d}.nbt'
            fragment.write(destination)
            readback = Structure.read(destination)
            assert len(readback.blocks) == size[0] * size[1] * size[2]
            assert readback.size == size and readback.data_version == 3465
            for local, state in readback.blocks.items():
                position = tuple(local[axis] + origin[axis] for axis in range(3))
                assert position not in rebuilt.blocks
                rebuilt.set(position, state, readback.block_entities.get(local))
            entries.append({'order': order, 'path': str(destination.relative_to(ROOT)), 'origin': list(origin), 'size': list(size), 'sha256': hashlib.sha256(destination.read_bytes()).hexdigest(), 'explicit_records': len(readback.blocks), 'explicit_air_records': sum(state == AIR for state in readback.blocks.values()), 'block_entities': [{'local_position': list(position), 'world_relative_position': [position[axis] + origin[axis] for axis in range(3)], 'nbt': plain(payload)} for position, payload in readback.block_entities.items()]})
    assert rebuilt.blocks == original.blocks
    assert rebuilt.block_entities == original.block_entities and rebuilt.entities == original.entities
    assert rebuilt.size == original.size and rebuilt.data_version == original.data_version
    report = {'status': 'PASS_EXACT_DENSE_REASSEMBLY', 'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(), 'source_size': list(original.size), 'source_explicit_records': len(original.blocks), 'source_block_entities': len(original.block_entities), 'parts': entries, 'placement': {'world_anchor': 'User-chosen [X,Y,Z]; not placed by this task.', 'formula': 'Place each part at world_anchor + origin, zero rotation, no mirror, integrity1.0.', 'order': [entry['order'] for entry in entries], 'nonoverlapping_volumes': True, 'air_strategy': 'All source voxels including outside footprints and roof air are explicit. Place air as air (do not ignore air/use structure_void); every part fully overwrites its own box including terrain. No terrain is changed here.', 'temporary_boundary_effects': 'Install complete set before judging fluid/plant boundary behavior; no game scheduler, placement or update ticks tested.'}, 'all_voxels_including_air_state_properties_and_entity_payloads_equal_source': True, 'world_placement_ticks': 'NOT_RUN'}
    (ROOT / 'split-check.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report, ensure_ascii=True, indent=2))


if __name__ == '__main__':
    main()
