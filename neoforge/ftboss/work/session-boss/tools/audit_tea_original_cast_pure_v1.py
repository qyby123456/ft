import hashlib,json,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss';OUT=WORK/'reports/tea-original-cast-pure-v1-20261114';JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
source=WORK/'tests/TeaOriginalCastPureAuditV1.java';original=ROOT/'reference/forge-current.jar';candidate=WORK/'build/boss-tea-original-cast-v2-20261114/ftboss-boss-core-1.21.1-stage45-original-tea-cast-NOT-INSTALLABLE.jar';assert sha(candidate)=='3493ee60f51c1309f127f4dfb4a2d9bbc0ec1a75fbc048bd89e6678b99ba69ba' and sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942';assert not OUT.exists();OUT.mkdir();classes=OUT/'compiled-classes';classes.mkdir();inputs={str(path):sha(path) for path in [Path(__file__),source,original,candidate]};commands=[[str(JAVA/'javac.exe'),'-encoding','UTF-8','--release','21','-d',str(classes),str(source)],[str(JAVA/'java.exe'),'-cp',str(classes),'TeaOriginalCastPureAuditV1',str(original),str(candidate)]]
for index,command in enumerate(commands):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);raw=result.stdout+result.stderr
    try:text=raw.decode('utf-8');encoding='utf-8'
    except UnicodeDecodeError:text=raw.decode('gb18030');encoding='gb18030'
    (OUT/f'step-{index}.txt').write_text(text,encoding='utf-8');(OUT/f'step-{index}-terminal.json').write_text(json.dumps({'exitCode':result.returncode,'decodedAs':encoding,'rawOutputSha256':hashlib.sha256(raw).hexdigest(),'rawOutputContentUploaded':False},indent=2)+'\n',encoding='utf-8');assert result.returncode==0,text[-1300:]
    if index:proof=json.loads(text)
assert proof['negativeControls']==2 and proof['localOnlySamplesCompared']==896 and proof['publicPureHelperCallsOnly'] and not proof['poseOrModelValuesExported'];assert all(sha(path)==digest for path,digest in inputs.items());proof.update({'inputHashes':inputs,'originalClassloaderIndependentOfCandidate':True,'readOnlyOriginalLocalDataUsed':True,'modelOrMusicUploaded':False,'actualNativePoseRenderAccepted':False});(OUT/'receipt.json').write_text(json.dumps(proof,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({key:value for key,value in proof.items() if key!='inputHashes'}),flush=True)
