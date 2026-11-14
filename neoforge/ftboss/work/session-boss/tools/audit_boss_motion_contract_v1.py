import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-motion-contract-audit-v1-20261113'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
owner=json.loads((ROOT/'WRITE_OWNER.json').read_text(encoding='utf-8'));assert owner['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns'];assert not OUT.exists();OUT.mkdir()
original=ROOT/'reference/forge-current.jar';current=WORK/'releases/boss-core-stage21-manor-original-timing-20261113/ftboss-boss-core-1.21.1-stage21-manor-original-timing.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942' and sha(current)=='21af23af5246b284bd5939b9b46735c1a61fc69627a9b86d6811c8c91d0d329d'
texts={};class_hashes={};entries={}
for label,path in [('original',original),('current',current)]:
    with zipfile.ZipFile(path) as bundle:
        names=[name for name in bundle.namelist() if name.startswith('net/farmingtales/ftboss/dandanmotion/') and name.endswith('.class')];entries[label]=names
        names=['net/farmingtales/ftboss/entity/EasterBossEntity.class']+names
        for name in names:
            class_hashes[label+'/'+name]=hashlib.sha256(bundle.read(name)).hexdigest();classname=name.removesuffix('.class').replace('/','.');command=['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-p','-c','-s','-classpath',str(path),classname];output=OUT/(label+'-'+classname.removeprefix('net.farmingtales.ftboss.')+'.javap.txt');save(output.with_suffix('.command.json'),command);result=subprocess.run(command,cwd=ROOT,capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW);output.write_bytes(result.stdout+result.stderr);save(output.with_suffix('.terminal.json'),{'exitCode':result.returncode});assert result.returncode==0
            if name.endswith('/EasterBossEntity.class'):texts[label]=result.stdout.decode('gb18030',errors='replace')
def signatures(text):return {line.strip() for line in text.splitlines() if line.strip().startswith('public ') and line.rstrip().endswith(';') and '(' in line and not line.split('(')[0].split()[-1].startswith('m_')}
missing=sorted(signatures(texts['original'])-signatures(texts['current']));save(OUT/'primary-audit-receipt.json',{'inputs':{str(path):sha(path) for path in [Path(__file__),original,current]},'classHashes':class_hashes,'dandanMotionEntries':entries,'originalPublicEntityContractsAbsentFromStage21':missing,'productChanged':False,'scope':'Read-only current actual Stage21 entity and original motion data/interface contracts;older preStage18 entity audit is not current authority. No weapon development or native gameplay launched.','fullBossGoalStatus':'active'});print(json.dumps({'audit':str(OUT),'originalMotionClasses':len(entries['original']),'currentMotionClasses':len(entries['current']),'originalPublicEntityContractsMissingFromActualStage21':len(missing),'missingMotionMethods':[name for name in missing if 'dandanMotionData' in name],'currentHeader':next(line for line in texts['current'].splitlines() if line.startswith('public class '))},ensure_ascii=False),flush=True)
