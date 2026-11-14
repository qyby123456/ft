import hashlib
import json
import os
import stat
import sys
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
assert len(sys.argv)==2 and sys.argv[1] in ['manor-reduced-budget-sharing-native-v2-9e9f840f','manor-reduced-budget-sharing-native-v5-30aa114e']
ATTEMPT=WORK/'build'/sys.argv[1]
OUT=WORK/'reports'/('manor-reduced-budget-prelaunch-cache-dedup-'+sys.argv[1]+'-20261114')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['noFurtherFormalWritesOrDeployment'] and owner['weaponValidationPaused'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId";assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
assert ATTEMPT.resolve().is_relative_to((WORK/'build').resolve()) and not ATTEMPT.is_symlink() and load(ATTEMPT/'observer-compile-terminal.json')['exitCode']==1 and load(ATTEMPT/'agent-compile-terminal.json')['exitCode']==0
assert all(not (ATTEMPT/name).exists() for name in ['live-process.json','runtime.log','inputs-before.json','raw-jvm-terminal.json','terminal.json']) and not (ATTEMPT/'game/saves').exists()
prior=load(WORK/'reports/zicai-curio-client-v13-candidate-6559f481.json');mods=ATTEMPT/'game/mods';expected={Path(path).name:digest for path,digest in prior['inputHashes'].items() if Path(path).parent==Path(prior['attempt'])/'game/mods'};assert len(expected)==28
candidate=load(WORK/'build/boss-youzi-original-summon-v4-20261114/candidate-build-receipt.json');candidate_path=Path(candidate['jar']);candidate_sha=sha(candidate_path);assert candidate_sha=='d691bdb1a2b5f540d9bcd8149e6d04baa5c2e1df1a6c01578b4b32b75cfdb69c'
files=sorted(mods.glob('*.jar'));assert len(files)==28 and set(path.name for path in files)==set(expected)
import zipfile
rows={};plan={}
for path in files:
    assert path.resolve().is_relative_to(mods.resolve()) and not path.is_symlink() and path.stat().st_nlink==1 and path.stat().st_mode&stat.S_IWRITE
    with zipfile.ZipFile(path) as jar:boss='net/farmingtales/ftboss/FTBossMod.class' in jar.namelist()
    digest=sha(path);assert digest==(candidate_sha if boss else expected[path.name]);cached=next((base/(digest+'.jar') for base in [WORK/'cache/common-mods-sha256-v1',WORK/'cache/closed-sparrow-runtime-jars-sha256-v1'] if (base/(digest+'.jar')).is_file()),None);assert cached is not None and cached.resolve().is_relative_to((WORK/'cache').resolve()) and not cached.is_symlink() and sha(cached)==digest and not cached.stat().st_mode&stat.S_IWRITE
    row={'sha256':digest,'bytes':path.stat().st_size,'cacheRelativePath':cached.relative_to(ROOT).as_posix()};rows[path.relative_to(ROOT).as_posix()]=row;plan[path.name]={'sha256':digest,'bytes':path.stat().st_size}
assert not OUT.exists();OUT.mkdir();plan_path=OUT/'handles-plan.json';save(plan_path,{'targetRelativePath':mods.relative_to(ROOT).as_posix(),'deleteFiles':plan})
result=subprocess.run(['powershell','-NoProfile','-File',str(WORK/'tools/check_github_source_cache_handles_v1.ps1'),'-PlanPath',str(plan_path)],capture_output=True,timeout=60);(OUT/'handles.log').write_bytes(result.stdout+result.stderr);assert result.returncode==0
save(OUT/'plan.json',{'attempt':str(ATTEMPT),'gameNeverLaunched':True,'observerCompilationExitCode':1,'worldNeverCreated':True,'resourcesRemainLocalOnly':True,'allPathsRetained':True,'paths':rows})
for name,row in rows.items():
    path=(ROOT/name).resolve();cached=(ROOT/row['cacheRelativePath']).resolve();assert path.is_relative_to(mods.resolve()) and cached.is_relative_to((WORK/'cache').resolve()) and sha(path)==row['sha256'] and path.stat().st_nlink==1
    temporary=path.with_name(path.name+'.prelaunch-readonly-alias.tmp');assert not temporary.exists();os.link(cached,temporary);assert sha(temporary)==row['sha256'];os.replace(temporary,path);assert path.stat().st_ino==cached.stat().st_ino and sha(path)==row['sha256'] and not path.stat().st_mode&stat.S_IWRITE
save(OUT/'receipt.json',{'jarPathsPreserved':28,'readonlyAliasesCreated':28,'sameLocalCanonicalHashesVerified':True,'gameNeverLaunched':True,'observerCompilationExitCode':1,'worldNeverCreated':True,'allResourceBytesPreservedLocally':True,'formalInstancesUntouched':True,'modelOrMusicUploaded':False,'remoteResourceRecoveryClaimed':False});print(json.dumps({'jarPathsPreserved':28,'readonlyAliasesCreated':28,'gameNeverLaunched':True,'observerCompilationExitCode':1,'modelOrMusicUploaded':False}),flush=True)
