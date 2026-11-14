import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/earned-finale-death-cancel-provider-static-v1-20261114'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
owner=json.loads((ROOT/'WRITE_OWNER.json').read_text(encoding='utf-8-sig'));assert owner['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['noFurtherFormalWritesOrDeployment'] and owner['weaponValidationPaused']
assert not OUT.exists();OUT.mkdir();mods=WORK/'build/earned-finale-attribute-baseline-native-v2-4e7fd7f1/game/mods';findings=[]
for jar in sorted(mods.glob('*.jar')):
    with zipfile.ZipFile(jar) as z:
        for name in z.namelist():
            if name.endswith('.class'):
                data=z.read(name)
                if b'LivingDeathEvent' in data and b'setCanceled' in data and b'setHealth' in data:findings.append({'jar':str(jar),'jarSha256':sha(jar),'class':name,'classSha256':hashlib.sha256(data).hexdigest(),'containsFiveHealthFloatConstant':b'\x04\x40\xa0\x00\x00' in data})
selected=[row for row in findings if row['class']=='com/github/L_Ender/cataclysm/event/ServerEventHandler.class'];assert len(selected)==1
row=selected[0];command=['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-p','-c','-classpath',row['jar'],row['class'].removesuffix('.class').replace('/','.')];result=subprocess.run(command,capture_output=True,timeout=90,creationflags=subprocess.CREATE_NO_WINDOW);assert result.returncode==0;text=result.stdout.decode('gb18030',errors='replace');(OUT/'third-party-local-only.javap.txt').write_text(text,encoding='utf-8')
receipt={'actualFailedAttempt':'earned-finale-attribute-baseline-native-v2-4e7fd7f1','nativeEventCanceledWithHealthRestoredToFive':True,'candidateHandlersMetadata':findings,'bytecodeReadOnly':True,'thirdPartyDisassemblyLocalOnly':True,'exactCancelProviderNotYetProven':True,'gameplayStateModified':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False};(OUT/'receipt.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'candidateHandlers':len(findings),'fiveHealthCandidates':sum(row['containsFiveHealthFloatConstant'] for row in findings),'disassemblyLocalOnly':True}))
