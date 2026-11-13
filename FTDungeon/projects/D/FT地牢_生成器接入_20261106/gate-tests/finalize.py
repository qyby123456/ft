import hashlib
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parent


def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    inputs=json.loads((ROOT/'execution-input-lock.json').read_text(encoding='utf-8'))
    log=(ROOT/'gametest-attempt1.log').read_text(encoding='utf-8-sig',errors='replace')
    compile_log=(ROOT/'compile.log').read_text(encoding='utf-8-sig',errors='replace')
    assert 'BUILD SUCCESSFUL' in compile_log
    assert 'INDEPENDENT_GENERATOR_GATE_CONTRACT_PASS checks=29' in log
    assert 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    drift=[]
    effective={}
    for record in inputs['inputs']:
        effective[record['destination']]=record
        source=Path(record['source'])
        current=sha(source) if source.exists() else None
        if current!=record['sha256']:
            drift.append(dict(path=str(source),testedSha256=record['sha256'],currentSha256=current))
    for relative,record in effective.items():
        if relative!='stage/build.gradle':assert sha(ROOT/relative)==record['sha256'],'Snapshot mutated: '+relative
    assert sha(ROOT/'stage/build.gradle')==inputs['stageBuildGradleSha256']
    assert sha(ROOT/'src/net/farmingtales/ftdungeonmobs/GeneratorGateGameTests.java')==inputs['fixtureSha256']
    names=('DungeonGenerators.java','DungeonGeneratorMenu.java','DungeonGateData.java','DungeonGateBlockEntity.java','DungeonGateTravel.java')
    gate_sources=[dict(path=record['source'],testedSha256=record['sha256']) for record in effective.values() if Path(record['source']).name in names]
    summary=dict(status='PASS_ACTUAL_FORGE_GAMETEST_ONE_TEST_29_ASSERTIONS',requiredTests=1,passedTests=1,assertions=29,
                 mainCompilePassed=True,fixtureCompilePassed=True,forge='1.20.1-47.4.10',java='17.0.8',
                 fixtureSha256=inputs['fixtureSha256'],testedGateSources=gate_sources,sourceDriftAfterSnapshot=drift,
                 mainSourceModified=False,backendSourceModified=False,ordinaryInstalledJarTest=False,
                 actualExecution='ForgeGameTestServer userdev on isolated source snapshot with backend library sources',
                 fakePlayer=True,actualNetworkUI=False,crossJvmDiskRestart=False,productionInstanceTouched=False,
                 maxOwnedGameServers=1,logSha256=sha(ROOT/'gametest-attempt1.log'),compileLogSha256=sha(ROOT/'compile.log'))
    (ROOT/'result.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    records=[]
    for path in sorted(ROOT.rglob('*')):
        if not path.is_file() or path==ROOT/'ready.json' or '.gradle' in path.relative_to(ROOT).parts:continue
        records.append(dict(path=path.relative_to(ROOT).as_posix(),sha256=sha(path)))
    ready=dict(status=summary['status'],tests=1,assertions=29,actualNetworkUI=False,sourceDriftAfterSnapshot=drift,files=records)
    (ROOT/'ready.json').write_text(json.dumps(ready,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('GATE_REGRESSION_READY files='+str(len(records))+' SHA256='+sha(ROOT/'ready.json'))
    print(json.dumps(gate_sources,ensure_ascii=False))
    print('sourceDriftAfterSnapshot='+str(len(drift)))


if __name__=='__main__':main()
