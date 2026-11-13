import collections
import json
import urllib.request

from PIL import Image
from build import ROOT, digest, load_structure, save


def main():
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS' and len(native['rooms']) == 9
    entries = []
    for folder in sorted((ROOT / 'incoming').iterdir()):
        hashes = {}
        for filename in ['room.nbt', 'room.json', 'materials.json', 'preview.png', 'qa.json', 'summary.json']:
            target = folder / filename
            assert target.is_file()
            hashes[filename] = digest(target)
        metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
        qa = json.loads((folder / 'qa.json').read_text(encoding='utf-8'))
        assert hashes['room.nbt'] == metadata['sha256'] == qa['sha256'] == qa['native_minecraft_1_20_1']['sha256']
        assert qa['status'] == qa['native_minecraft_1_20_1']['status'] == 'PASS'
        with Image.open(folder / 'preview.png') as image:
            image.verify()
        template = load_structure(folder / 'room.nbt')
        counts = collections.Counter(template['palette'][record['state']]['Name'] for record in template['blocks'] if template['palette'][record['state']]['Name'] != 'minecraft:air')
        assert dict(counts) == json.loads((folder / 'materials.json').read_text(encoding='utf-8'))
        review = ROOT / 'four-view-review/r2' / folder.name
        assert digest(review / 'room.nbt') == hashes['room.nbt']
        for mode in ['interior', 'exterior']:
            for direction in ['north', 'south', 'west', 'east']:
                with Image.open(review / f'{mode}-{direction}.png') as image:
                    image.verify()
        entries.append({'id': folder.name, 'status': 'PASS', 'files': hashes, 'rendered_views': 8})
    url = 'http://127.0.0.1:5388/__minecraft_builder__/build.nbt'
    import hashlib
    served = urllib.request.urlopen(url, timeout=10).read()
    assert hashlib.sha256(served).hexdigest() == digest(ROOT / 'incoming/N8_01/room.nbt')
    save(ROOT / 'qa/packaging.json', {'status': 'PASS', 'rooms': entries, 'count': len(entries), 'rendered_views': 72, 'lodestone_nbt_endpoint_hash': 'PASS', 'lodestone_browser_webgl': 'not automated', 'visual_sample_review': ['nine-room north contact sheet', 'N8_06 interior four views', 'N8_08 exterior four views'], 'forge_world_placement': 'NOT_RUN', 'gameplay': 'NOT_RUN'})
    print('PASS: 9 modules, 72 rendered views, materials/hashes/native reports and Lodestone NBT endpoint')


if __name__ == '__main__':
    main()
