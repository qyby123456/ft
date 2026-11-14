import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,data):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(data,stream,ensure_ascii=False,indent=2)

def execute(command,path):
    with path.open('xb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
    save(path.with_suffix('.terminal.json'),{'exitCode':result.returncode})
    assert result.returncode==0,str(path)

base=WORK/'releases/boss-core-stage12-bone-crush-20261112/ftboss-boss-core-1.21.1-stage12.jar'
assert sha(base)=='58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'
context=WORK/'build/boss-flight-ban-v1/compile-context.json'
sdk=[]
for name in load(context)['classpath'].split(';'):
    path=Path(name)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' not in bundle.namelist():sdk.append(name)
classpath=str(base)+';'+';'.join(sdk)
proposal=WORK/'proposal-stage13-summon-fx/net/farmingtales/ftboss'
sources=[proposal/'boss/MeatBoss.java',proposal/'butcherskills/ButcherBloodBrand.java',proposal/'butcherskills/ButcherSummonFx.java']
original=WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java'
text=original.read_text(encoding='utf-8');start=text.index('   public static boolean addWithFx(');end=text.index('   private static boolean eligible(',start);expected=text[start:end]
for before,after in [('ButcherSkills','ButcherSummonFx'),('m_7967_','addFreshEntity'),('m_20148_','getUUID'),('m_20182_','position'),('m_46467_','getGameTime'),('private static void tickSummons','public static void tickSummons')]:expected=expected.replace(before,after)
assert expected in sources[2].read_text(encoding='utf-8')
proof=WORK/'tests/SummonFxBoundaryProof.java'
inputs={str(path):sha(path) for path in [base,context,original,proof,*sources,Path(__file__)]}
output=WORK/'build/boss-summon-fx-v1';output.mkdir();compiled=output/'classes';compiled.mkdir();checks=output/'proof-classes';checks.mkdir()
save(output/'inputs-before.json',inputs)
execute([str(JAVA/'javac.exe'),'-proc:none','-implicit:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(compiled),*[str(path) for path in sources]],output/'compile.log')
execute([str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(checks),str(proof)],output/'proof-compile.log')
execute([str(JAVA/'java.exe'),'-Xmx512m','-cp',str(checks)+';'+classpath,'SummonFxBoundaryProof',str(base),str(compiled)],output/'proof-run.log')
overlay={path.relative_to(compiled).as_posix():path.read_bytes() for path in compiled.rglob('*.class')}
assert len(overlay)==6
candidate=output/'ftboss-boss-core-1.21.1-stage13-summon-fx-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for entry in before.infolist():after.writestr(entry,overlay.pop(entry.filename,before.read(entry.filename)))
    for name,payload in overlay.items():after.writestr(name,payload)
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    assert after.testzip() is None
    changed=[name for name in before.namelist() if before.read(name)!=after.read(name)]
    added=sorted(set(after.namelist())-set(before.namelist()))
    assert sorted(changed)==['net/farmingtales/ftboss/boss/MeatBoss.class','net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.class']
    assert len(added)==3 and all('ButcherSummonFx' in name for name in added)
assert all(sha(name)==wanted for name,wanted in inputs.items())
receipt={'jar':str(candidate),'jarSha256':sha(candidate),'baseJar':str(base),'baseJarSha256':sha(base),'proposal':str(WORK/'boss-core'),'scopedProposal':str(proposal),'changedEntries':changed,'addedEntries':added,'allOtherClassAndResourceBytesPreserved':True,'inputHashes':inputs,'proof':(output/'proof-run.log').read_text(encoding='utf-8'),'originalSummonMethodsPreservedUnderOfficialNameMapping':True,'fullBossInstallable':False,'featureRuntimeAccepted':False,'deployed':False,'weaponFunctionalDevelopmentPaused':True,'stage03MaterialChangesIncluded':False}
save(output/'candidate-build-receipt.json',receipt);save(output/'compile-context.json',{'classpath':classpath,'inputHashes':inputs})
print(json.dumps({'jarSha256':receipt['jarSha256'],'changed':changed,'added':added,'proof':receipt['proof']}),flush=True)
