import importlib.util
import json
from pathlib import Path
import sys

from build import ROOT, SHARED, REFERENCE, digest, save


def module(name, target):
    specification = importlib.util.spec_from_file_location(name, target)
    result = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(result)
    return result


def native():
    runner = module('native_runner', SHARED / 'run_native_review.py')
    original = (SHARED / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    old_ids = '"L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3"'
    new_ids = ', '.join(json.dumps(f'N8_{number:02}') for number in range(1, 10))
    assert old_ids in original
    source = ROOT / 'tools/VerifyDeliveries.java'
    source.parent.mkdir(exist_ok=True)
    source.write_text(original.replace(old_ids, new_ids), encoding='utf-8')
    runner.ROOT = ROOT
    sys.argv = ['native', '--root', str(ROOT)]
    runner.main()


def render():
    renderer = module('fourview', SHARED / 'review_four_views.py')
    renderer.REVIEW = ROOT / 'four-view-review'
    sys.argv = ['review', '--rooms', *[f'N8_{number:02}' for number in range(1, 10)], '--revision', 'r1', '--source-root', str(ROOT / 'incoming')]
    renderer.main()
    from PIL import Image
    contact = Image.new('RGB', (1800, 1350), '#101a20')
    for number in range(1, 10):
        room_id = f'N8_{number:02}'
        target = ROOT / 'four-view-review/r1' / room_id / 'interior-north.png'
        image = Image.open(target)
        image.save(ROOT / 'incoming' / room_id / 'preview.png')
        image.thumbnail((600, 450))
        contact.paste(image, (((number - 1) % 3) * 600, ((number - 1) // 3) * 450))
    contact.save(ROOT / 'preview-contact.png')


def manifest():
    import hashlib
    references = [{'path': str(target), 'sha256': digest(target)} for target in sorted(REFERENCE.glob('S8-*.png')) if int(target.name[3:5]) <= 10]
    fingerprint = hashlib.sha256(json.dumps(references, sort_keys=True).encode()).hexdigest()
    rooms = [json.loads(target.read_text(encoding='utf-8')) for target in sorted((ROOT / 'incoming').glob('*/room.json'))]
    save(ROOT / 'manifest.json', {'group': 'S8-crystal-geode', 'batch': 'first ten references; nine room modules', 'thread': '01a1a5df-ead1-7fa3-ba71-2cf0013e1e97', 'group_fingerprint': fingerprint, 'references': references, 'rooms': rooms, 'overview': 'S8-10-overview.png is layout reference only', 'image_generation': 'NOT_CALLED; no partial image files created', 'deployment': 'NOT_DEPLOYED', 'native_world_placement': 'NOT_RUN', 'formal_ids': 'proposed N8_01..09; coordinator assigns slots', 'source_authority': str(ROOT / 'source/metadata/authority.json')})
    native_result = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    for report in native_result['rooms']:
        target = ROOT / 'incoming' / report['id'] / 'qa.json'
        qa = json.loads(target.read_text(encoding='utf-8'))
        qa['native_minecraft_1_20_1'] = report
        save(target, qa)


if __name__ == '__main__':
    mode = sys.argv[1]
    {'native': native, 'render': render, 'manifest': manifest}[mode]()
