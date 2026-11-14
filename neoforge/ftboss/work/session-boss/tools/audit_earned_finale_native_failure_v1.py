import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/earned-finale-native-failure-audit-v1-20261114'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['noFurtherFormalWritesOrDeployment'] and owner['weaponValidationPaused']
assert not OUT.exists();OUT.mkdir()
failed=load(WORK/'reports/earned-finale-attribute-baseline-native-v2-4e7fd7f1.json');assert not failed['actualEarnedUnlockAndFinaleAttributeBaselineAccepted'] and failed['terminal']['clientProcessReaped'] and failed['terminal']['exitCode']==0 and failed['terminal']['inputHashesUnchanged']
classes=['boss/ManorFight','boss/ManorFight$Fight','boss/BossMercy','entity/EasterBossEntity','client/render/ShadowSigilVisual']
original=ROOT/'reference/forge-current.jar';current=WORK/'build/boss-shadow-sigil-buffer-v2-20261114/ftboss-boss-core-1.21.1-stage40-shadow-sigil-buffer-NOT-INSTALLABLE.jar';decompiler=WORK/'tools/vineflower-1.10.1.jar'
inputs={str(path):sha(path) for path in [Path(__file__),original,current,decompiler]}
for label,source in [('original',original),('current',current)]:
    packed=OUT/(label+'-selected-classes.jar');destination=OUT/(label+'-sources');destination.mkdir()
    with zipfile.ZipFile(source) as jar,zipfile.ZipFile(packed,'x') as small:
        for name in classes:
            entry='net/farmingtales/ftboss/'+name+'.class'
            if entry in jar.namelist():small.writestr(entry,jar.read(entry))
    command=['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xmx384m','-jar',str(decompiler),'-thr=1',str(packed),str(destination)]
    with (OUT/(label+'-decompiler.txt')).open('xb') as stream:result=subprocess.run(command,stdout=stream,stderr=subprocess.STDOUT,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW)
    assert result.returncode==0
probe=failed['probe'];summary={'failedAttempt':failed['attempt'],'failure':probe['failure'].splitlines()[0],'terminal':failed['terminal'],'stageVariants':[row['variant'] for row in probe['observedPrimaryStages']],'lastAction':probe['lastAction'],'deathEvents':probe['events'],'visiblePillarObservedFrames':probe['visiblePillarObservedFrames'],'visiblePillarReturnedFrames':probe['visiblePillarReturnedFrames'],'inputs':inputs,'inputsUnchanged':all(sha(Path(path))==digest for path,digest in inputs.items()),'accepted':False,'productChanged':False,'formalWrites':False}
(OUT/'receipt.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'audit':str(OUT),'variants':summary['stageVariants'],'failure':summary['failure'],'events':len(probe['events'])},ensure_ascii=True))
