import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1];hashes={}
def digest(path):
    key=str(path)
    if key not in hashes:hashes[key]=hashlib.sha256(path.read_bytes()).hexdigest()
    return hashes[key]
def load(path):digest(path);return json.loads(path.read_text(encoding='utf-8'))
audit=work/'reports/native-remaining-variant-gap-audit-3320e085.json';data=load(audit)
items=data['groups']['customSubclassesStillNeedImplementationAndProviderAudit']+data['groups']['originalNonDamageableStaffNotNativePaidRepairCandidates'];assert len(items)==66
classes=sorted({row['class'] for row in items})
command_receipt=work/'build/repair-runtime-8420d899/compile-command308.json';compile_command=load(command_receipt)
proposal=work/'build/repair-runtime-8420d899/ftboss-unpackaged-proposal'
mod_jars=sorted((proposal.parent/'server/mods').glob('*.jar'));assert mod_jars
roots=[proposal,*mod_jars,*[Path(value) for value in compile_command[compile_command.index('-cp')+1].split(';')]]
attempt=work/'build/remaining-original-implementations-static-qualified-41d445cf';attempt.mkdir()
class_rows=[]
for class_name in classes:
    relative=class_name.replace('.','/')+'.class';found=None
    for location in roots:
        if location.is_dir() and (location/relative).is_file():
            path=location/relative;content=path.read_bytes();digest(path);found={'container':str(location),'classPath':str(path)};break
        if location.is_file() and location.suffix=='.jar':
            with zipfile.ZipFile(location) as archive:
                if relative not in archive.namelist():continue
                content=archive.read(relative);digest(location);found={'container':str(location),'archiveEntry':relative};break
    assert found is not None,class_name
    class_rows.append({'class':class_name,'ids':[row['id'] for row in items if row['class']==class_name],'classSha256':hashlib.sha256(content).hexdigest(),**found})
command=['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-p','-s','-c','-classpath',';'.join(str(value) for value in roots),*classes]
command_path=attempt/'javap-command.json';command_path.write_text(json.dumps(command,ensure_ascii=False,indent=2),encoding='utf-8');digest(command_path)
log=attempt/'javap-implementations.txt'
with log.open('wb') as stream:result=subprocess.run(command,cwd=root,stdout=stream,stderr=subprocess.STDOUT)
assert result.returncode==0,result.returncode
digest(log);text=log.read_text(encoding='utf-8',errors='replace')
assert all(('class '+row['class']) in text for row in class_rows)
zero=[row['id'] for row in items if row['maxDamage']==0];assert len(zero)==11
for path in [Path(__file__),audit]:digest(path)
output=work/'reports/native-remaining-original-implementation-static-audit-41d445cf.json'
with output.open('x',encoding='utf-8') as stream:json.dump({'failures':0,'registeredItems':66,'distinctImplementations':len(classes),'currentCandidateDiskClassEvidence':class_rows,'javapAllMethodsDescriptorsAndInstructions':str(log),'originalZeroDurabilityIdsMustStayZero':zero,'runtimeMixinTransformedImplementationAcceptance':False,'actualProviderGetCapabilityOrInternalMutationAccepted':False,'nativePaidRepairOrSignedCombatAccepted':False,'arbitrarySubclassAdmissionAllowed':False,'productChanged':False,'authenticatedMultiplayerAccepted':False,'installable':False,'inputHashes':hashes},stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'items':66,'uniqueClasses':len(classes),'zeroDurabilityMustRemainZero':11,'actualRuntimeAcceptance':False,'hashes':len(hashes)}))
