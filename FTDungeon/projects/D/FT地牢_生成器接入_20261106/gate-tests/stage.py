import hashlib
import json
from pathlib import Path
import shutil

ROOT=Path(__file__).resolve().parent
MAIN=Path('D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1')
BACKEND=ROOT.parent/'backend'


def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    stage=ROOT/'stage'
    stage.mkdir(exist_ok=False)
    records=[]
    def copy(source,target):
        if source.is_dir():
            for path in sorted(source.rglob('*')):
                if path.is_file():copy(path,target/path.relative_to(source))
        else:
            target.parent.mkdir(parents=True,exist_ok=True)
            data=source.read_bytes()
            target.write_bytes(data)
            records.append(dict(source=str(source),destination=target.relative_to(ROOT).as_posix(),sha256=hashlib.sha256(data).hexdigest()))
    for relative in ('src/main','src/test/resources','gradle','libs','build.gradle','settings.gradle','gradle.properties'):
        copy(MAIN/relative,stage/relative)
    copy(BACKEND/'src/main/java',stage/'src/main/java')
    copy(BACKEND/'core-snapshot',stage/'src/main/java')
    copy(BACKEND/'src/main/resources/data/ftbd_candidate',stage/'src/main/resources/data/ftbd_candidate')
    with (stage/'build.gradle').open('a',encoding='utf-8') as stream:
        stream.write("\napply from: '"+str(ROOT/'overlay.gradle').replace('\\','/')+"'\n")
    result=dict(status='EXACT_SOURCE_SNAPSHOT_READY_FOR_COMPILE',stage=str(stage),mainReadOnly=True,backendReadOnly=True,inputs=records,
                stageBuildGradleSha256=sha(stage/'build.gradle'),fixtureSha256=sha(ROOT/'src/net/farmingtales/ftdungeonmobs/GeneratorGateGameTests.java'))
    (ROOT/'execution-input-lock.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('STAGED files='+str(len(records)),flush=True)


if __name__=='__main__':main()
