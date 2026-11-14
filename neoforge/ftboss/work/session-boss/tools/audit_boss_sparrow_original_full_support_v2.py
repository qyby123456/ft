import hashlib
import json
import re
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/sparrow-original-support-full-surface-audit-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command,expected=0):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==expected,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
candidate=Path(owner['latestScopedBossCandidate']);assert sha(candidate)=='d691bdb1a2b5f540d9bcd8149e6d04baa5c2e1df1a6c01578b4b32b75cfdb69c';original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
mapping_path=WORK/'build/boss-manor-original-helpers-platform-audit-v1-20261114/mapping-proof.json';mapping=load(mapping_path);target='net/farmingtales/ftboss/boss/SparrowSupport';lines=mapping['originalSharingMethodInstructions'][target+'#__ALL__'];names=[]
for line in lines:
    if line.startswith('  ') and not line.startswith('    ') and '(' in line:
        name=line.strip().split('(')[0].split()[-1]
        if name in ['onServerTick','onServerStopped'] or name.endswith('SparrowSupport'):continue
        names.append(name)
assert len(names)==11 and len(set(names))==10
mapping['methodSelectionsByClass']={target:sorted(set(names))}
source=WORK/'tests/SparrowOriginalSupportMappedAuditV1.java';prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1];inputs={str(path):sha(path) for path in [Path(__file__),candidate,original,mapping_path,source]}
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();save('selected-mapping.json',mapping)
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)]);proof=run('strict-original-contrast',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'SparrowOriginalSupportMappedAuditV1',str(original),str(candidate),str(OUT/'selected-mapping.json'),str(OUT/'method-comparison.json')],expected=1)
comparison=load(OUT/'method-comparison.json');assert not comparison['allSelectedMethodsExact'] and comparison['selectedCompleteMethods']==11 and comparison['negativeControls']==2
missing=[]
with zipfile.ZipFile(original) as before,zipfile.ZipFile(candidate) as after:
    for name in ['net/farmingtales/ftboss/boss/SparrowMultiplayerSupport','net/farmingtales/ftboss/boss/SparrowAppearanceData','net/farmingtales/ftboss/boss/SparrowAppearanceData$Entry','net/farmingtales/ftboss/util/SparrowMultiplayerRules']:
        assert before.getinfo(name+'.class') and name+'.class' not in after.namelist();missing.append(name)
assert inputs=={path:sha(path) for path in inputs};save('receipt.json',{'proof':proof,'selectedCompleteGameplayMethods':11,'exactAfterOfficialMappingsAndHolderApi':comparison['exactCompleteMethods'],'differingCompleteMethods':[{'owner':row['owner'],'method':row['method'],'descriptor':row['descriptor']} for row in comparison['differences']],'missingOriginalHelperClasses':missing,'nativeLifecycleEntrypointsExplicitlyExcludedFromOriginalGraphComparison':['onServerTick','onServerStopped'],'nativeLifecycleEntrypointsNotClaimedBroken':True,'strictExpectedDifferencePreserved':True,'fullSparrowMultiplayerRestored':False,'productChanged':False,'inputHashes':inputs,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False});print(json.dumps({'methods':11,'exact':comparison['exactCompleteMethods'],'different':[row['method'] for row in comparison['differences']],'missingOriginalHelpers':len(missing),'fullSparrowMultiplayerRestored':False}),flush=True)
