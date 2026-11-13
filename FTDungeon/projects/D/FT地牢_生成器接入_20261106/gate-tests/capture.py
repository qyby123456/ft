import hashlib
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parent
SOURCE=Path('D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    records=[]
    for name in ('DungeonGenerators.java','DungeonGeneratorMenu.java','DungeonGateData.java','DungeonGateBlockEntity.java','DungeonGateTravel.java','DungeonGateBlock.java','DungeonGateInteraction.java','FTDungeonMobs.java'):
        path=SOURCE/'src/main/java/net/farmingtales/ftdungeonmobs'/name
        target=ROOT/'source-review'/name
        target.parent.mkdir(parents=True,exist_ok=True)
        target.write_bytes(path.read_bytes())
        records.append(dict(path=str(path),snapshot=target.relative_to(ROOT).as_posix(),sha256=sha(path)))
    fixture=ROOT/'src/net/farmingtales/ftdungeonmobs/GeneratorGateGameTests.java'
    result=dict(status='FIXTURE_PREPARED_NOT_COMPILED_NOT_RUN',mainSourceModified=False,worldRun=False,
                awaiting='Main compile confirmation and exact runnable isolated classpath/artifact before execution',
                fixture=dict(path=str(fixture),sha256=sha(fixture)),reviewedSources=records)
    (ROOT/'prepared.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('FIXTURE_PREPARED_NOT_COMPILED_NOT_RUN SHA256='+sha(fixture),flush=True)


if __name__=='__main__':main()
