import hashlib,json,subprocess,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss';OUT=WORK/'reports/original-finale-attribute-surface-v1-20261114';JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'];prior_path=WORK/'reports/earned-finale-zhuni-closeout-20261114.json';prior=load(prior_path);assert prior['actualNormalVictoryEarnedUnlock'] and prior['actualEligibleFinaleAttributeBaselineAccepted'];candidate=Path(prior['candidate']);original=ROOT/'reference/forge-current.jar';assert sha(candidate)==prior['candidateJarSha256']=='b751ae8fbf5552342ae5e887cc5d7675b7fcdf995fcde1773dcc0dc32e691345' and sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942';decompiler=WORK/'tools/vineflower-1.10.1.jar';assert not OUT.exists();OUT.mkdir();inputs={str(path):sha(path) for path in [Path(__file__),prior_path,candidate,original,decompiler]};class_hashes={}
for label,path,names in [('original',original,['boss/NonManorFinaleDifficulty','boss/NonManorFinaleDifficulty$AttributeBase']),('current',candidate,['boss/NonManorFinale'])]:
    packed=OUT/(label+'-selected-code.jar');destination=OUT/(label+'-sources');destination.mkdir();class_hashes[label]={}
    with zipfile.ZipFile(path) as jar,zipfile.ZipFile(packed,'x') as selected:
        for name in names:
            full='net/farmingtales/ftboss/'+name+'.class';data=jar.read(full);selected.writestr(full,data);class_hashes[label][name]=hashlib.sha256(data).hexdigest()
    command=[str(JAVA/'java.exe'),'-Dfile.encoding=UTF-8','-Xmx384m','-jar',str(decompiler),'-thr=1',str(packed),str(destination)];result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);raw=result.stdout+result.stderr;(OUT/(label+'-console.raw-local.log')).write_bytes(raw)
    try:decoded=raw.decode('utf-8');encoding='utf-8'
    except UnicodeDecodeError:decoded=raw.decode('gb18030');encoding='gb18030'
    (OUT/(label+'-decompiler.txt')).write_text(decoded,encoding='utf-8');assert result.returncode==0;(OUT/(label+'-terminal.json')).write_text(json.dumps({'exitCode':result.returncode,'rawConsoleBytes':len(raw),'rawConsoleSha256':hashlib.sha256(raw).hexdigest(),'decodedAs':encoding,'rawConsoleContentUploaded':False},indent=2)+'\n',encoding='utf-8')
receipt={'candidateJarSha256':sha(candidate),'classHashes':class_hashes,'inputHashes':inputs,'allFrozenInputsUnchanged':all(sha(path)==digest for path,digest in inputs.items()),'actualEligibleBaseline':prior['actualOriginalBaseOnlyParityGaps'],'selectedCodeOnly':True,'productChanged':False,'modelOrMusicUploaded':False,'installable':False,'deployed':False};assert receipt['allFrozenInputsUnchanged'];(OUT/'receipt.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'classes':class_hashes,'productChanged':False}),flush=True)
