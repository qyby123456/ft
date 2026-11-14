import hashlib
import json
import subprocess
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1];hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):
    digest(path);return json.loads(path.read_text(encoding='utf-8'))
paths=['material-ranged-paid-repair-baseline-99c65f41','material-ranged-paid-repair-e43eb814','material-ranged-paid-repair-qualified-e43eb814','material-ranged-bow-signed-e43eb814','material-ranged-bow-signed-2be7d6c1','material-ranged-crossbow-signed-2be7d6c1','material-ranged-bow-restart-qualified-3320e085','material-ranged-bow-controls-qualified-3320e085','material-ranged-crossbow-signed-3320e085','material-ranged-crossbow-control-signed-3320e085','material-ranged-crossbow-controls-qualified-3320e085','material-ranged-crossbow-restart-qualified-3320e085']
rows=[];pids=[]
for suffix in paths:
    path=work/'reports'/('native-repair-runtime-'+suffix+'.json');result=load(path);attempt=Path(result['attempt']);terminal=load(attempt/'terminal308.json');process=load(attempt/'live-process308.json');pids.append(process['pid'])
    assert result['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'],suffix
    expected=1 if suffix in [paths[index] for index in [0,1,3,5,9]] else 0
    assert result['probe']['failures']==terminal['probe']['failures']==expected,suffix
    log=attempt/'runtime308.log';text=log.read_text(encoding='utf-8',errors='replace');assert 'Stopping server' in text and 'All dimensions are saved' in text;digest(log)
    rows.append({'receipt':str(path),'attempt':str(attempt),'pid':process['pid'],'observerFailures':expected,'normalSavedStop':True,'javaExitCode':0,'accepted':expected==0})
command="$rows=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','python.exe','pythonw.exe') -and $_.CommandLine -like '*session-lease*' }); ConvertTo-Json -InputObject @($rows | Select-Object ProcessId,Name,CommandLine) -Compress"
result=subprocess.run(['powershell','-NoProfile','-Command',command],cwd=root,capture_output=True,text=True);assert result.returncode==0,result.stderr
processes=json.loads(result.stdout or '[]');foreign=[row for row in processes if 'finalize_native_material_ranged_processes_3320e085.py' not in (row.get('CommandLine') or '')]
assert not foreign,[(row['ProcessId'],row['Name']) for row in foreign]
assert digest(root/'work/session-chain/reports/actual-current.json')=='1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
for family in ['bow','crossbow']:
    path=work/('reports/native-material-'+family+'-paid-signed-restart-focused-evidence-3320e085.json');data=load(path);assert data['failures']==0
    for name,wanted in data['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
for path in [Path(__file__),root/'WRITE_OWNER.claim']:digest(path)
output=work/'reports/native-material-ranged-final-process-check-3320e085.json'
with output.open('x',encoding='utf-8') as stream:json.dump({'failures':0,'runtimeCount':12,'acceptedRuntimeCount':7,'excludedRuntimeCount':5,'runtimes':rows,'ownedJavaOrOtherPythonProcessesRemaining':False,'adoptedReceiptUnchanged':True,'goalStillActive':True,'installable':False,'deployed':False,'inputHashes':hashes},stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'runtimes':12,'accepted':7,'excluded':5,'allNormalSavedJavaZero':True,'ownedProcessesRemaining':False,'hashes':len(hashes)}))
