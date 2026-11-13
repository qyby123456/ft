import hashlib
import json
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parent
PREVIOUS=ROOT.parent
MAIN=Path('D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1')


def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    previous_ready=PREVIOUS/'ready.json'
    lock=json.loads((ROOT/'input-lock.json').read_text(encoding='utf-8'))
    assert sha(previous_ready)==lock['previousReadySha256']
    for record in json.loads(previous_ready.read_text(encoding='utf-8'))['files']:
        assert sha(PREVIOUS/record['path'])==record['sha256'],'Original 29 assertion evidence changed'
    log=(ROOT/'gametest-ready.log').read_text(encoding='utf-8-sig',errors='replace')
    success=re.search(r'READY_FLOW_ACTUAL_ENTRY_AND_RETURN_PASS checks=(\d+)',log)
    limited='READY_FLOW_TOOL_LIMITATION' in log
    assert success or limited,'No actual READY flow test result yet'
    assert 'Game test server shutting down' in log,'GameTest JVM not finished'
    matches=[]
    for name in ('DungeonGenerators.java','DungeonGeneratorMenu.java','DungeonGateData.java','DungeonGateBlockEntity.java','DungeonGateTravel.java'):
        relative=Path('src/main/java/net/farmingtales/ftdungeonmobs')/name
        source=MAIN/relative
        tested=ROOT/'stage'/relative
        assert sha(tested)==sha(PREVIOUS/'stage'/relative)==sha(source),'Gate source changed: '+name
        matches.append(dict(path=str(source),testedSha256=sha(tested),currentMainSha256=sha(source),same=True))
    jar=MAIN/'build/libs/ft-dungeon-mobs-forge-1.20.1-0.21.0.jar'
    assert sha(jar)=='9316998125a8867b54a6576648e9ef175d4abccea78c3173c4feaeef464043c3'
    result=dict(status='PASS_ACTUAL_READY_ENTRY_COOLDOWN_NATIVE_RETURN' if success else 'TOOL_LIMITATION_READY_FLOW_NOT_FULLY_VERIFIED',
                assertions=int(success.group(1)) if success else None,previous29AssertionsPreserved=True,
                previousReadySha256=sha(previous_ready),finalMainArtifactSha256=sha(jar),artifactPath=str(jar),
                testMode='ForgeGameTestServer source snapshot, not installed reobfuscated final JAR',
                currentGateMainSourcesIdentical=matches,nativeCrossDimensionVerified=bool(success),
                onEnteredOnce=bool(success),cooldownAndNativeReturnVerified=bool(success),
                fakePlayer=True,actualNetworkUI=False,mainModified=False,backendModified=False,
                logSha256=sha(ROOT/'gametest-ready.log'))
    (ROOT/'result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    files=[dict(path=path.relative_to(ROOT).as_posix(),sha256=sha(path)) for path in sorted(ROOT.rglob('*')) if path.is_file() and path!=ROOT/'ready.json' and '.gradle' not in path.relative_to(ROOT).parts]
    (ROOT/'ready.json').write_text(json.dumps(dict(status=result['status'],assertions=result['assertions'],files=files),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(result['status']+' files='+str(len(files))+' readySHA256='+sha(ROOT/'ready.json'),flush=True)


if __name__=='__main__':main()
