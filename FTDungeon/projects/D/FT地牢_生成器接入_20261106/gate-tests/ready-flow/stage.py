import hashlib
import json
from pathlib import Path
import shutil

ROOT=Path(__file__).resolve().parent
PREVIOUS=ROOT.parent


def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    ready=json.loads((PREVIOUS/'ready.json').read_text(encoding='utf-8'))
    for record in ready['files']:assert sha(PREVIOUS/record['path'])==record['sha256']
    stage=ROOT/'stage'
    stage.mkdir()
    for relative in ('src','gradle','libs','build.gradle','settings.gradle','gradle.properties'):
        source=PREVIOUS/'stage'/relative
        if source.is_dir():shutil.copytree(source,stage/relative)
        else:shutil.copyfile(source,stage/relative)
    (ROOT/'input-lock.json').write_text(json.dumps(dict(previousReadySha256=sha(PREVIOUS/'ready.json'),fixtureSha256=sha(ROOT/'src/net/farmingtales/ftdungeonmobs/ReadyGateFlowGameTests.java'),mainSnapshot='Exact prior successful 29-assertion gate source snapshot; only new test fixture'),indent=2)+'\n',encoding='utf-8')
    print('READY_FLOW_STAGE_CREATED_PRIOR_29_ASSERTIONS_UNCHANGED')


if __name__=='__main__':main()
