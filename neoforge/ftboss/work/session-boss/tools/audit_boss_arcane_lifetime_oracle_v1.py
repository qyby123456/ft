import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-arcane-lifetime-oracle-v1'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,data):
    with path.open('x',encoding='utf-8') as stream:json.dump(data,stream,ensure_ascii=False,indent=2)
def run(name,command):
    save(OUT/(name+'-command.json'),command);result=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout);assert result.returncode==0;return result.stdout.decode('utf-8','replace')
OUT.mkdir();source=WORK/'tests/ArcaneLifetimeOracleV1.java';original=ROOT/'reference/forge-current.jar';base=WORK/'releases/boss-core-stage24-manor-body-chain-20261113/ftboss-boss-core-1.21.1-stage24-manor-body-chain.jar';context=WORK/'build/boss-flight-ban-v1/compile-context.json'
assert sha(base)=='4faa8fcb3e0746bfed8176a2c51f0e2e3a0fd13ec7592cd199e016399c203bb0' and sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
gson=[path for path in json.loads(context.read_text('utf-8'))['classpath'].split(';') if Path(path).name.startswith('gson-') and path.endswith('.jar')];assert len(gson)==1
classpath=str(base)+';'+gson[0];run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-cp',classpath,'-d',str(OUT),str(source)])
rows={}
for label,jar in [('original',original),('baseline',base)]:
    run(label,[str(JAVA/'java.exe'),'-Xmx128m','-cp',str(OUT)+';'+str(jar)+';'+gson[0],'ArcaneLifetimeOracleV1',str(OUT/(label+'-rows.json'))]);rows[label]=json.loads((OUT/(label+'-rows.json')).read_text('utf-8'))
assert len(rows['original'])==len(rows['baseline'])
differences=[]
for before,expected in zip(rows['baseline'],rows['original']):
    changed=[key for key in before if before[key]!=expected[key]]
    if changed:differences.append({'id':before['id'],'phase':before['phase'],'changedKeys':changed,'baseline':before,'original':expected})
assert differences and all(row['changedKeys']==['lifetime'] for row in differences)
save(OUT/'audit-receipt.json',{'failures':0,'rowsEach':len(rows['original']),'differences':differences,'allNonLifetimeFieldsOriginalEquivalent':True,'inputs':{str(path):sha(path) for path in [Path(__file__),source,base,original,context,Path(gson[0])]},'scope':'Public pure enum oracle;not native runtime or client visual acceptance.'})
print(json.dumps({'rowsEach':len(rows['original']),'differences':[{'id':row['id'],'phase':row['phase'],'baseline':row['baseline']['lifetime'],'original':row['original']['lifetime']} for row in differences]}),flush=True)
