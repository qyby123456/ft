import datetime
import hashlib
import json
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-gongdi-lifecycle-audit-v1-20261113'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['activeRuntimePid'] is None
query="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease')} } | ConvertTo-Json -Compress"
import os
raw=subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip();rows=json.loads(raw) if raw else [];rows=rows if isinstance(rows,list) else [rows]
assert not any(row['owned'] and row['pid']!=os.getpid() for row in rows)
release=WORK/'releases/boss-core-stage15-xuanlan-limits-20261113';deployment=load(release/'deployment-receipt.json');jar=release/'ftboss-boss-core-1.21.1-stage15-xuanlan.jar'
assert sha(jar)==sha(deployment['target'])==deployment['deployedSha256']=='92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4'
assert sha(deployment['backup'])==deployment['backupSha256']
archives=[]
for receipt_path in [release/'archive-receipt.json',release/'closeout-archive-receipt.json',WORK/'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json']:
    receipt=load(receipt_path);assert receipt['failures']==0 and sha(receipt['archive'])==receipt['archiveSha256'];archives.append({'receipt':str(receipt_path),'receiptSha256':sha(receipt_path),'archive':receipt['archive'],'archiveSha256':receipt['archiveSha256']})
paused=load(WORK/'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json');assert sha(paused['snapshotJar'])==paused['snapshotJarSha256']
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');assert sha(forge)=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
assert not OUT.exists();OUT.mkdir()
save(OUT/'resume-preflight.json',{'createdAt':datetime.datetime.now().astimezone().isoformat(),'previousGoalTurnClassification':'progress:stage14 and15 native verified,deployed and archived;full Boss goal remains active','currentJar':str(jar),'currentSha256':sha(jar),'formalTarget':deployment['target'],'formalSha256':sha(deployment['target']),'forgeSha256':sha(forge),'archivesReverified':archives,'ownedProcesses':rows,'weaponValidationPaused':True,'stage13Undeployed':True,'fullBossMigrationComplete':False})
classes=['boss.ai.GongdiSkills','boss.ai.ManorFiveSkills','boss.ai.ManorLivingTargets','entity.EasterBossEntity']
inputs={str(Path(__file__)):sha(__file__),str(jar):sha(jar),str(original):sha(original)}
for label,bundle in [('current',jar),('original',original)]:
    for short in classes+(['boss.ai.GongdiCastLifecycle'] if label=='original' else []):
        name='net.farmingtales.ftboss.'+short
        command=['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-p','-c','-classpath',str(bundle),name]
        result=subprocess.run(command,capture_output=True)
        save(OUT/(label+'-'+short.rsplit('.',1)[1]+'.terminal.json'),{'exitCode':result.returncode,'command':command})
        assert result.returncode==0,name
        (OUT/(label+'-'+short.rsplit('.',1)[1]+'.javap.txt')).write_bytes(result.stdout)
save(OUT/'audit-inputs.json',inputs)
print(json.dumps({'audit':str(OUT),'frozenHashesVerified':True,'previousTurn':'progress','primaryJavapClasses':9,'productChanged':False,'fullBossGoal':'active'}),flush=True)
