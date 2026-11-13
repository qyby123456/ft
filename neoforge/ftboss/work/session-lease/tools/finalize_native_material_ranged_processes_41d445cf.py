import hashlib
import json
import os
import subprocess
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1];hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):
    digest(path);return json.loads(path.read_text(encoding='utf-8'))
failed_suffixes={'material-ranged-paid-repair-baseline-99c65f41','material-ranged-paid-repair-e43eb814','material-ranged-bow-signed-e43eb814','material-ranged-crossbow-signed-2be7d6c1','material-ranged-crossbow-signed-3320e085','material-ranged-crossbow-control-signed-3320e085'}
suffixes=['material-ranged-paid-repair-baseline-99c65f41','material-ranged-paid-repair-e43eb814','material-ranged-paid-repair-qualified-e43eb814','material-ranged-bow-signed-e43eb814','material-ranged-bow-signed-2be7d6c1','material-ranged-crossbow-signed-2be7d6c1','material-ranged-bow-restart-qualified-3320e085','material-ranged-bow-controls-qualified-3320e085','material-ranged-crossbow-signed-3320e085','material-ranged-crossbow-control-signed-3320e085','material-ranged-crossbow-controls-qualified-3320e085','material-ranged-crossbow-diamond-diagnostic-17e0dabe','material-ranged-crossbow-owner-capacity-qualified-41d445cf','material-ranged-crossbow-signed-41d445cf','material-ranged-crossbow-restart-qualified-41d445cf']
rows=[]
for suffix in suffixes:
    path=work/'reports'/('native-repair-runtime-'+suffix+'.json');data=load(path);attempt=Path(data['attempt']);terminal=load(attempt/'terminal308.json');process=load(attempt/'live-process308.json')
    assert data['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'],suffix
    wanted=1 if suffix in failed_suffixes else 0
    assert data['probe']['failures']==terminal['probe']['failures']==wanted,suffix
    log=attempt/'runtime308.log';text=log.read_text(encoding='utf-8',errors='replace');assert 'Stopping server' in text and 'All dimensions are saved' in text;digest(log)
    rows.append({'receipt':str(path),'attempt':str(attempt),'pid':process['pid'],'observerFailures':wanted,'normalStop':True,'allDimensionsSaved':True,'javaExitCode':0,'diagnosticOrCapacityNotFullSignedMatrix':'diagnostic' in suffix or 'capacity' in suffix})
compile_only=work/'build/repair-runtime-6a45dd11/compile-terminal320.json';compile_data=load(compile_only)
assert compile_data['exitCode']==1 and not compile_data['serverStarted'];digest(compile_only.parent/'compile308.log')
current_path=str(work).replace("'","''");forward=work.as_posix().replace("'","''")
command="$rows=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') -and ($_.CommandLine -like '*"+current_path+"*' -or $_.CommandLine -like '*"+forward+"*') }); ConvertTo-Json -InputObject @($rows | Select-Object ProcessId,Name) -Compress"
result=subprocess.run(['powershell','-NoProfile','-Command',command],cwd=root,capture_output=True,text=True);assert result.returncode==0,result.stderr
processes=json.loads(result.stdout or '[]');others=[row for row in processes if int(row['ProcessId'])!=os.getpid()]
assert not others,others
assert digest(root/'work/session-chain/reports/actual-current.json')=='1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
for family,tag in [('bow','3320e085'),('crossbow','41d445cf')]:
    path=work/('reports/native-material-'+family+'-paid-signed-restart-focused-evidence-'+tag+'.json');data=load(path);assert data['failures']==0
    for name,wanted in data['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
capacity=load(work/'reports/native-shared-repair-owner-growth-focused-evidence-41d445cf.json');assert capacity['failures']==0
for name,wanted in capacity['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
for path in [Path(__file__),root/'WRITE_OWNER.claim']:digest(path)
output=work/'reports/native-material-ranged-final-process-check-41d445cf.json'
with output.open('x',encoding='utf-8') as stream:json.dump({'failures':0,'actualRuntimeCount':15,'observerPassCount':9,'preservedFailureCount':6,'runtimes':rows,'compileOnlyObserverFailurePreserved':str(compile_only),'ownedJavaOrOtherPythonProcessesRemaining':False,'adoptedReceiptUnchanged':True,'full103ItemMatrixAccepted':False,'goalStillActive':True,'installable':False,'deployed':False,'inputHashes':hashes},stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'actualRuntimes':15,'observerPass':9,'failuresPreserved':6,'allNormalSavedJavaZero':True,'ownedProcessesRemaining':False,'hashes':len(hashes)}))
