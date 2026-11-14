import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];W=ROOT/'work/session-boss';OUT=W/'build/boss-manor-body-candidate-closure-v2';JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text('utf-8'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
assert not OUT.exists();OUT.mkdir();classes=OUT/'classes';classes.mkdir();receipt=W/'build/boss-manor-body-chain-v5/candidate-build-receipt.json';built=load(receipt);jar=Path(built['jar']);base=Path(built['baseJar']);assert sha(jar)==built['jarSha256'] and sha(base)==built['baseJarSha256'];context=W/'build/boss-flight-ban-v1/compile-context.json';sdk=[]
for entry in load(context)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as archive:
        if 'net/farmingtales/ftboss/FTBossMod.class' in archive.namelist():continue
    sdk.append(str(path))
cp=str(jar)+';'+str(W/'build/boss-manor-body-chain-v5/proof-classes')+';'+';'.join(sdk);source=W/'tests/ManorBodyCandidateClosureProofV2.java';output=''
for name,command in [('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)]),('proof',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'ManorBodyCandidateClosureProofV2',str(base),str(jar),str(W/'build/boss-manor-effective-lifecycle-audit-v1-20261113')])] :
    (OUT/(name+'-command.json')).write_text(json.dumps(command,ensure_ascii=False,indent=2),'utf-8');result=subprocess.run(command,cwd=ROOT,capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout+result.stderr);(OUT/(name+'-terminal.json')).write_text(json.dumps({'exitCode':result.returncode}),'utf-8');assert result.returncode==0,str(OUT/(name+'.log'));output=result.stdout.decode('utf-8',errors='replace')
(OUT/'proof-receipt.json').write_text(json.dumps({'inputs':{str(path):sha(path) for path in [Path(__file__),source,context,receipt,jar,base,W/'tests/ManorBodyChainComposerV5.java',W/'build/boss-manor-effective-lifecycle-audit-v1-20261113/EasterBossEntity-effective.class',W/'build/boss-manor-effective-lifecycle-audit-v1-20261113/ManorFiveSkills-effective.class']},'proof':output,'productChanged':False},ensure_ascii=False,indent=2),'utf-8');print(output,flush=True)
