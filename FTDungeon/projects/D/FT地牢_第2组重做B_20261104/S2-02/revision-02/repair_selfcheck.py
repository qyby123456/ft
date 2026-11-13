from collections import deque
import json

from prepare import ROOT, TARGET, Structure, digest, save


def main():
    previous = ROOT.parent
    before_path = previous / 'incoming/G2B-02/room.nbt'
    after_path = TARGET / 'room.nbt'
    assert digest(before_path) == 'bb8885701818a29fbaf9904da9d191680511628304b97194c814d2989464ed87'
    before, after = Structure.read(before_path), Structure.read(after_path)
    assert before.size == after.size and before.block_entities == after.block_entities
    assert before.entities == after.entities
    changes = []
    for point in before.blocks.keys() | after.blocks.keys():
        old, new = before.get(point), after.get(point)
        if old == new:
            continue
        assert point in {(6, 11, 20), (15, 11, 20)}
        assert old.name == 'minecraft:air' and new.name == 'minecraft:chain'
        assert dict(new.properties) == {'axis': 'y', 'waterlogged': 'false'}
        changes.append({'position': list(point), 'before': old.name, 'after': new.name, 'properties': dict(new.properties)})
    assert len(changes) == 2
    for east in [6, 15]:
        assert after.get((east, 10, 20)).name == after.get((east, 12, 20)).name == 'minecraft:cut_sandstone'
    excluded = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:water', 'minecraft:lava'}
    solids = {point for point, state in after.blocks.items() if state.name not in excluded}
    reached = {point for point in solids if point[1] == 0}
    queue = deque(reached)
    offsets = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    while queue:
        point = queue.popleft()
        for offset in offsets:
            neighbor = tuple(value + delta for value, delta in zip(point, offset))
            if neighbor in solids and neighbor not in reached:
                reached.add(neighbor)
                queue.append(neighbor)
    disconnected = sorted(solids - reached)
    assert not disconnected, disconnected
    old_metadata = json.loads((previous / 'incoming/G2B-02/room.json').read_text(encoding='utf-8'))
    metadata = json.loads((TARGET / 'room.json').read_text(encoding='utf-8'))
    for field in ['size', 'entrance', 'interior', 'connections', 'route_targets', 'landmark']:
        assert old_metadata[field] == metadata[field], field
    old_manifest = json.loads((previous / 'FILE_MANIFEST.json').read_text(encoding='utf-8'))
    assert all(digest(previous / entry['path']) == entry['sha256'] for entry in old_manifest['files'])
    save(ROOT / 'qa/repair-selfcheck.json', {'status': 'MAKER_SELF_CHECK_PASS_PENDING_INDEPENDENT_REVIEW', 'previous_rejected_source': str(before_path), 'previous_sha256': digest(before_path), 'source': str(after_path), 'sha256': digest(after_path), 'exact_changes': sorted(changes, key=lambda item: item['position']), 'unchanged_metadata_fields': ['size', 'entrance', 'interior', 'connections', 'route_targets', 'landmark'], 'block_entities_and_entities_unchanged': True, 'prior_manifest_hashes_verified': len(old_manifest['files']), 'six_face_solid_count': len(solids), 'six_face_reached_from_y0': len(reached), 'six_face_disconnected': disconnected, 'scope': 'Static six-face adjacency and exact NBT state diff only; not Minecraft gravity/attachment/physics simulation. Roof connection verified through chain Y11 between sandstone Y10 and intact roof Y12.'})
    print(f'REPAIR PASS: exactly 2 new chains; {len(solids)} solids connected to Y0; all {len(old_manifest["files"])} prior file hashes unchanged')


if __name__ == '__main__':
    main()
