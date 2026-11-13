import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-manor-five-audit-v1-20261113'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
assert not OUT.exists();OUT.mkdir()
original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
current=WORK/'releases/boss-core-stage18-gongdi-public-contract-20261113/ftboss-boss-core-1.21.1-stage18-gongdi-public-contract.jar';assert sha(current)=='5aabc09b09e836eee820bda998077561b6fbe7b5a6d9673e80e6ca0d3f9a0cfa'
decompiler=WORK/'tools/vineflower-1.10.1.jar';assert sha(decompiler)=='b9b208e50793b64657a6b6292067526613f549de7405f9243624b02f4276e409'
inputs={str(path):sha(path) for path in [Path(__file__),original,current,decompiler]};class_hashes={}
for label,path in [('original',original),('current',current)]:
    archive=OUT/(label+'-input.jar')
    with zipfile.ZipFile(path) as bundle,zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as target:
        names=[name for name in bundle.namelist() if name.startswith('net/farmingtales/ftboss/boss/ai/ManorFiveSkills') and name.endswith('.class')]
        assert len(names)>=1
        for name in names:
            payload=bundle.read(name);target.writestr(name,payload);class_hashes[label+'/'+name]=hashlib.sha256(payload).hexdigest()
    destination=OUT/(label+'-sources');destination.mkdir()
    command=['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xmx512m','-jar',str(decompiler),'-thr=1',str(archive),str(destination)]
    save(OUT/(label+'-command.json'),command)
    with (OUT/(label+'-decompile.log')).open('wb') as log:result=subprocess.run(command,cwd=ROOT,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    save(OUT/(label+'-terminal.json'),{'exitCode':result.returncode});assert result.returncode==0
save(OUT/'source-recovery-receipt.json',{'inputs':inputs,'classHashes':class_hashes,'readOnlyPrimaryAudit':True,'sourceRecoveryOnly':True,'productChanged':False,'goalStatus':'active'})
print(json.dumps({'audit':str(OUT),'recoveredOriginalAndCurrent':True,'sourceRecoveryOnly':True}),flush=True)
