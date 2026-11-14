import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
OUT=Path(sys.argv[1]).resolve();assert OUT.is_relative_to((WORK/'build').resolve());OUT.mkdir()
built=json.loads((WORK/'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json').read_text(encoding='utf-8'));rows={}
for label,path,expected in [('original',ROOT/'reference/forge-current.jar','c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'),('current',Path(built['jar']),'c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e')]:
    assert sha(path)==expected
    name='net/farmingtales/ftboss/entity/MeatMountainEntity.class'
    with zipfile.ZipFile(path) as bundle:class_hash=hashlib.sha256(bundle.read(name)).hexdigest()
    result=subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-p','-c','-s','-classpath',str(path),name[:-6].replace('/','.')],capture_output=True,check=True)
    output=OUT/(label+'-MeatMountainEntity.javap.txt');output.write_bytes(result.stdout);text=result.stdout.decode('utf-8',errors='replace')
    methods=[line.strip() for line in text.splitlines() if re.match(r'^  (public|protected|private).*\(',line)]
    nbt=[method for method in methods if any(token in method for token in ['CompoundTag','m_7378_','m_7380_'])]
    accesses=[line.strip() for line in text.splitlines() if '// Field cloneButcher:Z' in line]
    assert 'extends net.minecraft.world.entity.monster.Vindicator' in text and 'private boolean cloneButcher;' in text
    assert not nbt and len(accesses)==4 and sum('putfield' in line for line in accesses)==2
    assert 'public void setCloneButcher(boolean);' in text and 'public boolean isCloneButcher();' in text
    rows[label]={'jarSha256':expected,'classSha256':class_hash,'javapSha256':sha(output),'declaredNbtMethods':nbt,'cloneFieldAccessInstructions':accesses,'noDedicatedCloneNbtSerializationDeclared':True}
report={'classes':rows,'bothDeclarePlainPrivateCloneFieldAndNoNbtOverride':True,'originalRuntimeNewJvmTestClaimed':False,'dynamicThirdPartyReflectionOrMixinClosureClaimed':False,'productDefectProven':False,'productChanged':False}
(OUT/'declared-contract.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(report),flush=True)
