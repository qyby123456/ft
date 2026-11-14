import hashlib
import json
import os
import shutil
import stat
import sys
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=None
CACHE=WORK/'cache/closed-sparrow-runtime-jars-sha256-v1'
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
config=load(Path(sys.argv[1]).resolve());OUT=(ROOT/config['output']).resolve();assert OUT.is_relative_to(WORK/'reports') and not OUT.is_symlink()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids']
assert owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
assert not OUT.exists();OUT.mkdir();CACHE.mkdir(exist_ok=True)
attempts=config['attempts'];assert len(attempts)==len(set(attempts)) and all(Path(name).name==name for name in attempts)
rows={};reports=[]
for attempt_name in attempts:
    report=load(WORK/'reports'/(attempt_name+'.json'));reports.append(report);assert report['terminal']['clientProcessReaped'] and report['terminal']['inputHashesUnchanged']
    if report['terminal']['exitCode']!=0:
        permitted=config.get('closedCrashAttempts',{}).get(attempt_name);assert permitted is not None and report['terminal']['exitCode']==permitted['exitCode'] and not report.get('actualEarnedUnlockAndFinaleAttributeBaselineAccepted',False)
        crash=Path(permitted['crashReport']);assert crash.is_relative_to(WORK/'build'/attempt_name/'game/crash-reports') and crash.is_file() and sha(crash)==permitted['sha256']
        assert 'java.lang.IllegalStateException: Not building!' in crash.read_text(encoding='utf-8') and 'ShadowSigilVisual.renderSigil' in crash.read_text(encoding='utf-8')
    else:assert report['terminal']['exitCode']==0
    assert Path(report['attempt']).resolve()==(WORK/'build'/attempt_name).resolve()
    mods=WORK/'build'/attempt_name/'game/mods';files=sorted(mods.glob('*.jar'));assert len(files)==28
    plan={}
    for path in files:
        assert path.resolve().is_relative_to(mods.resolve()) and not path.is_symlink() and path.stat().st_nlink==1 and path.stat().st_mode&stat.S_IWRITE
        digest=sha(path);assert report['inputHashes'][str(path)]==digest
        existing=WORK/'cache/common-mods-sha256-v1'/(digest+'.jar')
        cached=existing if existing.is_file() else CACHE/(digest+'.jar')
        if cached.exists():assert not cached.is_symlink() and sha(cached)==digest and cached.stat().st_size==path.stat().st_size
        relative=path.relative_to(ROOT).as_posix();row={'sha256':digest,'bytes':path.stat().st_size,'cacheRelativePath':cached.relative_to(ROOT).as_posix(),'beforeFileId':path.stat().st_ino};rows[relative]=row;plan[path.name]={'sha256':digest,'bytes':path.stat().st_size}
    plan_path=OUT/(attempt_name+'-handles-plan.json');save(plan_path,{'targetRelativePath':mods.relative_to(ROOT).as_posix(),'deleteFiles':plan})
    result=subprocess.run(['powershell','-NoProfile','-File',str(WORK/'tools/check_github_source_cache_handles_v1.ps1'),'-PlanPath',str(plan_path)],capture_output=True,timeout=60);(OUT/(attempt_name+'-handles.log')).write_bytes(result.stdout+result.stderr);assert result.returncode==0
save(OUT/'plan.json',{'closedTerminalEvidenceJarsOnly':True,'explicitClosedCrashAttempts':config.get('closedCrashAttempts',{}),'selectedWorldsAndEveryOriginalPathRetained':True,'noRemoteResourceBackupClaimed':True,'allFileBytesPreservedLocally':True,'paths':rows})
free=shutil.disk_usage(ROOT).free;aliases=0;created=0
for relative,row in rows.items():
    path=(ROOT/relative).resolve();cached=(ROOT/row['cacheRelativePath']).resolve();assert path.is_relative_to(WORK/'build') and cached.is_relative_to(WORK/'cache') and sha(path)==row['sha256']
    if not cached.exists():os.link(path,cached);os.chmod(cached,stat.S_IREAD);created+=1
    if path.stat().st_ino==cached.stat().st_ino:continue
    assert path.stat().st_nlink==1 and path.stat().st_mode&stat.S_IWRITE
    temporary=path.with_name(path.name+'.closed-runtime-readonly-alias.tmp');assert not temporary.exists();os.link(cached,temporary);assert sha(temporary)==row['sha256'];os.replace(temporary,path);aliases+=1
    assert path.stat().st_ino==cached.stat().st_ino and sha(path)==row['sha256'] and not path.stat().st_mode&stat.S_IWRITE
    with (OUT/'journal.jsonl').open('a',encoding='utf-8') as stream:stream.write(json.dumps({'path':relative,'sha256':row['sha256'],'pathPreserved':True})+'\n')
for report in reports:
    for name,wanted in report['inputHashes'].items():assert Path(name).exists() and sha(Path(name))==wanted,name
result={'jarPathsPreserved':len(rows),'readonlyAliasesCreated':aliases,'newCanonicalLocalContents':created,'allFrozenRuntimeInputsUnchanged':True,'allSelectedEvidenceWorldsRetained':True,'sourceAndFormalInstancesUnchanged':True,'freeBytesBefore':free,'freeBytesAfter':shutil.disk_usage(ROOT).free,'newInstancesMustIndependentlyCopy':True,'modelOrMusicUploaded':False,'noRemoteRecoveryClaimed':True,'closedCrashExitDoesNotImplyAcceptance':True,'explicitClosedCrashAttempts':config.get('closedCrashAttempts',{})}
save(OUT/'receipt.json',result);print(json.dumps(result),flush=True)
