import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-manor-body-lifecycle-audit-v1-20261113'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
assert not OUT.exists();OUT.mkdir()
original=ROOT/'reference/forge-current.jar';current=WORK/'releases/boss-core-stage23-manor-light-hooks-20261113/ftboss-boss-core-1.21.1-stage23-manor-light-hooks.jar';decompiler=WORK/'tools/vineflower-1.10.1.jar'
assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942' and sha(current)=='97d564e42b907f13a25382e31dff798d6e8899d90d11a9de74c5694504a97c43' and sha(decompiler)=='b9b208e50793b64657a6b6292067526613f549de7405f9243624b02f4276e409'
inputs={str(path):sha(path) for path in [Path(__file__),original,current,decompiler]};classhashes={};inventory={}
exact={'net/farmingtales/ftboss/entity/EasterBossEntity.class','net/farmingtales/ftboss/boss/ai/NativeManorMixinPlugin.class','net/farmingtales/ftboss/boss/ai/NativeManorCastStateInstaller.class','net/farmingtales/ftboss/boss/ai/NativeManorCancelInstaller.class','net/farmingtales/ftboss/boss/ai/NativePhantomEchoSkillMembers161.class'}
prefixes=['net/farmingtales/ftboss/boss/ai/ManorFiveSkills','net/farmingtales/ftboss/qingyilight/','net/farmingtales/ftboss/zheergenfx/LightHooks','net/farmingtales/ftboss/dandannativelights/','net/farmingtales/ftboss/zheergenremainingbody/','net/farmingtales/ftboss/body/','net/farmingtales/ftboss/animation/qingyicast/','net/farmingtales/ftboss/skill/xuanlannative/','net/farmingtales/ftboss/boss/ai/NativeManorCastStateInstaller']
for label,path in [('original',original),('current',current)]:
    packed=OUT/(label+'-input.jar')
    with zipfile.ZipFile(path) as bundle,zipfile.ZipFile(packed,'x',compression=zipfile.ZIP_DEFLATED) as target:
        names=[name for name in bundle.namelist() if name.endswith('.class') and (name in exact or any(name.startswith(prefix) for prefix in prefixes))];inventory[label]={name:len(bundle.read(name)) for name in names}
        for name in names:
            payload=bundle.read(name);target.writestr(name,payload);classhashes[label+'/'+name]=hashlib.sha256(payload).hexdigest()
    destination=OUT/(label+'-sources');destination.mkdir();command=['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xmx512m','-jar',str(decompiler),'-thr=1',str(packed),str(destination)];save(OUT/(label+'-command.json'),command)
    with (OUT/(label+'-decompile.log')).open('wb') as log:result=subprocess.run(command,cwd=ROOT,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    save(OUT/(label+'-terminal.json'),{'exitCode':result.returncode});assert result.returncode==0
save(OUT/'audit-receipt.json',{'inputs':inputs,'classHashes':classhashes,'classInventory':inventory,'originalEntriesAbsentFromRawCurrentJar':sorted(set(inventory['original'])-set(inventory['current'])),'scope':'Read-only exact original and Stage23 body lifecycle,full original/current Boss and actual installer helper sources;raw class/member absence is not effective Mixin runtime absence. Actual target/order/state baselines required.','productChanged':False,'weaponsPaused':True,'fullBossGoalStatus':'active'})
print(json.dumps({'audit':str(OUT),'originalClasses':len(inventory['original']),'currentClasses':len(inventory['current']),'rawMissing':sorted(set(inventory['original'])-set(inventory['current']))}),flush=True)
