import base64
import datetime
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys
from urllib.parse import urlencode

ROOT = Path(__file__).resolve().parent
specification = importlib.util.spec_from_file_location('validate', ROOT / 'validate.py')
validation = importlib.util.module_from_spec(specification)
specification.loader.exec_module(validation)


def main():
    number = int(sys.argv[1])
    folder = ROOT / 'incoming' / f'NAT-B1-G4-{number:02}'
    analysis = json.loads((folder / 'image-analysis.json').read_text(encoding='utf-8'))
    assert analysis['reference_sha256'] == validation.digest(Path(analysis['reference']))
    for previous in range(1, number):
        assert (ROOT / 'incoming' / f'NAT-B1-G4-{previous:02}' / 'serial-complete.json').is_file(), 'Previous image not completed'
    environment = dict(os.environ, SWAMP_ROOM=str(number))
    subprocess.run(['node', 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs', str(ROOT / 'recipe.mjs'), '--out-dir', str(folder), '--name', 'room', '--data-version', '3465', '--json'], env=environment, check=True, stdout=subprocess.DEVNULL)
    metadata, report = validation.inspect(folder)
    validation.native([metadata])
    validation.render([metadata])
    url = 'http://127.0.0.1:19440/?' + urlencode({'nbt': 'data:application/octet-stream;base64,' + base64.b64encode((folder / 'room.nbt').read_bytes()).decode(), 'name': metadata['id']})
    validation.save(ROOT / 'lodestone-previews.json', {'rooms': [{'id': metadata['id'], 'url': url}]})
    subprocess.run(['node', str(ROOT / 'capture-lodestone.mjs'), metadata['id']], check=True)
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    validation.save(folder / 'native-review.json', native)
    validation.save(folder / 'serial-complete.json', {'status': 'PASS', 'completed_at_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'sequence_number': number, 'image_analysis': str(folder / 'image-analysis.json'), 'sha256': metadata['sha256'], 'static': report, 'native': native['status'], 'lodestone': 'PASS', 'world_placement': 'NOT_RUN'})
    print('SERIAL COMPLETE ' + metadata['id'])


if __name__ == '__main__':
    main()
