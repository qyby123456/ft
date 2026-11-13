import json
import sys

from deliver import ROOT, SHARED, ROOM, ROOM_ID, module, save


def native():
    runner = module('native_runner', SHARED / 'run_native_review.py')
    original = (SHARED / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    old_ids = '"L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3"'
    assert old_ids in original
    source = original.replace(old_ids, json.dumps(ROOM_ID))
    line = '        int reachable = checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "interior"));'
    assert line in source
    source = source.replace(line, line + '\n        for (com.google.gson.JsonElement element : metadata.getAsJsonArray("route_targets")) {\n            checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(element.getAsJsonObject(), "position"));\n        }')
    (ROOT / 'tools').mkdir(exist_ok=True)
    (ROOT / 'tools/VerifyDeliveries.java').write_text(source, encoding='utf-8')
    runner.ROOT = ROOT
    sys.argv = ['native', '--root', str(ROOT)]
    runner.main()


def render():
    from PIL import Image
    renderer = module('fourview', SHARED / 'review_four_views.py')
    renderer.REVIEW = ROOT / 'views'
    sys.argv = ['render', '--rooms', ROOM_ID, '--revision', 'single-01', '--source-root', str(ROOT / 'incoming')]
    renderer.main()
    with Image.open(ROOT / 'views/single-01' / ROOM_ID / 'interior-north.png') as image:
        image.save(ROOM / 'preview.png')


def assembly():
    save(ROOT / 'assembly.json', {'size': [23, 13, 23], 'chunk_limited': False, 'size_basis': 'single reference image proportions; not a block-exact survey', 'modules': [{'id': ROOM_ID, 'nbt': str(ROOM / 'room.nbt'), 'local_origin': [0, 0, 0], 'placement_order': 1, 'connection_anchors': json.loads((ROOM / 'room.json').read_text(encoding='utf-8'))['connections']}], 'split': False, 'split_reason': '23x13x23 fits one Java structure NBT; no 16-block chunk constraint', 'bounds_policy': 'check full 23x13x23 volume and entities before placement; reject occupied volume; explicit air can erase world blocks', 'rotation': 'rotate blocks and all anchors together; quarter turn clockwise [x,y,z] -> [22-z,y,x]; north->east->south->west', 'world_origin': 'assigned by coordinator; not reserved', 'world_placement': 'NOT_RUN'})


if __name__ == '__main__':
    {'native': native, 'render': render, 'assembly': assembly}[sys.argv[1]]()
