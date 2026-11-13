import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss';OUT=WORK/'build/boss-manor-effective-lifecycle-audit-v1-20261113';JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text('utf-8'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,data):
    with path.open('x',encoding='utf-8') as stream:json.dump(data,stream,ensure_ascii=False,indent=2)
assert not OUT.exists();OUT.mkdir();classes=OUT/'classes';classes.mkdir();source=WORK/'tests/ManorEffectiveLifecycleAuditV1.java';context=WORK/'build/boss-flight-ban-v1/compile-context.json';receipt=WORK/'releases/boss-core-stage23-manor-light-hooks-20261113/deployment-receipt.json';deployed=load(receipt);current=Path(deployed['releasedJar']);original=ROOT/'reference/forge-current.jar';assert sha(current)==sha(deployed['target'])==deployed['deployedSha256']=='97d564e42b907f13a25382e31dff798d6e8899d90d11a9de74c5694504a97c43';sdk=[]
for entry in load(context)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as jar:
        if 'net/farmingtales/ftboss/FTBossMod.class' in jar.namelist():continue
    sdk.append(str(path))
cp=str(current)+';'+';'.join(sdk)
for name,command in [('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)]),('audit',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'ManorEffectiveLifecycleAuditV1',str(current),str(original),str(OUT)])]:
    save(OUT/(name+'-command.json'),command);result=subprocess.run(command,cwd=ROOT,capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout+result.stderr);save(OUT/(name+'-terminal.json'),{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'))
save(OUT/'audit-receipt.json',{'inputs':{str(path):sha(path) for path in [Path(__file__),source,context,receipt,current,original]},'scope':'Exact Raid postApply ClassNode only;native runtime state baseline still required.','productChanged':False,'fullBossGoalStatus':'active','weaponsPaused':True})
data=load(OUT/'effective-members.json');print(json.dumps({name:{'verifiedMethods':entry['asmVerifiedMethods'],'missingOriginalSignatures':entry['originalSignaturesAbsentFromEffective']} for name,entry in data.items() if isinstance(entry,dict)}),flush=True)
