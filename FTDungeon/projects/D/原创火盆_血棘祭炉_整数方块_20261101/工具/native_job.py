import argparse
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-modeling-item\scripts')
from bb_client import Client, structured


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['render', 'eval'])
    parser.add_argument('--script')
    parser.add_argument('--kind', choices=['grey', 'final', 'runtime'], default='grey')
    parser.add_argument('--prefix', default='grey')
    parser.add_argument('--frames', default='0')
    parser.add_argument('--views', default='front,back,left,right,top,underside,hero')
    parser.add_argument('--native-save')
    parser.add_argument('--runtime-save')
    parser.add_argument('--size', type=int, default=640)
    parser.add_argument('--background', default='#252630')
    arguments = parser.parse_args()
    if arguments.action == 'render':
        job = {
            'kind': arguments.kind, 'prefix': arguments.prefix,
            'frames': [int(value) for value in arguments.frames.split(',')],
            'views': arguments.views.split(','), 'size': arguments.size,
            'background': arguments.background,
            'native_save': arguments.native_save, 'runtime_save': arguments.runtime_save,
        }
        (ROOT / 'work/render_job.json').write_text(json.dumps(job, ensure_ascii=False), encoding='utf-8')
        script = ROOT / '工具/render_owned.js'
    else:
        script = Path(arguments.script)
    client = Client(timeout=180)
    try:
        result = structured(client.tool('risky_eval', {'code': script.read_text(encoding='utf-8')}))
        print(json.dumps(result, ensure_ascii=False, indent=2))
    finally:
        client.close()


if __name__ == '__main__':
    main()
