import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-nonmanor-finale-effective-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def run(name,command,timeout=90):
    save(OUT/(name+'-command.json'),command)
    try:
        with (OUT/(name+'.log')).open('xb') as stream:result=subprocess.run(command,cwd=OUT,stdout=stream,stderr=subprocess.STDOUT,timeout=timeout,creationflags=subprocess.CREATE_NO_WINDOW)
        terminal={'exitCode':result.returncode,'timeout':False}
    except subprocess.TimeoutExpired:
        terminal={'exitCode':None,'timeout':True,'ownedSynchronousChildReapedBySubprocessRun':True}
    save(OUT/(name+'-terminal.json'),terminal);return terminal

owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
preflight=load(OUT/'preflight.json');assert preflight['sourceCommit']=='e30819034dca27e2932b2dcf4b3b0d47e77e8009'
assert not (OUT/'audit-inputs.json').exists()
original=ROOT/'reference/forge-current.jar';built_path=WORK/'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json';built=load(built_path);current=Path(built['jar'])
assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
assert sha(current)=='c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';context=load(context_path)
classpath=[]
for entry in context['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()=='.jar' and path.is_file():
        with zipfile.ZipFile(path) as bundle:
            if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    classpath.append(path)
assert all(path.exists() for path in classpath)
cp=';'.join(str(path) for path in classpath)
classes=OUT/'classes';classes.mkdir()
for folder in ['isolated-home','isolated-tmp']:(OUT/folder).mkdir()
source_paths=[WORK/'tests/NonManorFinaleCallsitesV1.java',WORK/'tests/NonManorAttributeEngineV1.java']
prior_path=WORK/'reports/zicai-curio-client-v13-candidate-6559f481.json';prior=load(prior_path)
assert prior['probe']['failures']==0 and prior['terminal']['clientProcessReaped']
runtime_mods=sorted((Path(prior['attempt'])/'game/mods').glob('*.jar'));assert runtime_mods
runtime_descriptor=OUT/'runtime-mod-archives.json';save(runtime_descriptor,[str(path) for path in runtime_mods])
mcp=Path('C:/Users/qyby/AppData/Roaming/.minecraft/libraries/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/mcp_config-1.20.1-20230612.114412.zip')
official=Path('C:/Users/qyby/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client_mappings.txt')
with zipfile.ZipFile(mcp) as bundle:mcp_lines=bundle.read('config/joined.tsrg').decode('utf-8').splitlines()
index=next(index for index,line in enumerate(mcp_lines) if ' m_22170_ ' in line)
class_line=next(mcp_lines[position] for position in range(index,-1,-1) if not mcp_lines[position].startswith('\t'))
assert class_line.startswith('bhd ') and mcp_lines[index].strip()=='b ()Ljava/util/Collection; m_22170_ 22170'
official_lines=official.read_text(encoding='utf-8').splitlines();index=next(index for index,line in enumerate(official_lines) if line.startswith('net.minecraft.world.entity.ai.attributes.AttributeMap -> bhd:'))
block=[]
for line in official_lines[index+1:]:
    if line and not line.startswith((' ','#')):break
    block.append(line)
match=next(line for line in block if 'getSyncableAttributes() -> b' in line)
save(OUT/'attribute-collection-mapping-proof.json',{'mcpConfigSha256':sha(mcp),'mcpClassLine':class_line,'mcpMethodLine':'b ()Ljava/util/Collection; m_22170_ 22170','officialMappingsSha256':sha(official),'officialClassLine':official_lines[index],'officialMethodLine':match.strip(),'resolvedOriginalMethod':'AttributeMap.getSyncableAttributes()','originalAndCurrentCollectionScopeDiffer':False,'priorNonSyncableCoverageHypothesisRejected':True,'originalStoredBaseVersusCurrentTotalMultiplierStillRequiresEffectiveBaseline':True})
inputs={str(path):sha(path) for path in [Path(__file__),built_path,context_path,prior_path,original,current,mcp,official,*source_paths,*runtime_mods,*[path for path in classpath if path.is_file()]]}
save(OUT/'audit-inputs.json',inputs)
compiled=run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),*[str(path) for path in source_paths]])
assert compiled['exitCode']==0,'Preserve compile failure and inspect compile.log before any correction'
scanner=run('callsites',[str(JAVA/'java.exe'),'-Xmx256m','-Duser.home='+str(OUT/'isolated-home'),'-Djava.io.tmpdir='+str(OUT/'isolated-tmp'),'-cp',str(classes)+';'+cp,'NonManorFinaleCallsitesV1',str(original),str(current),str(runtime_descriptor),str(OUT/'callsites-and-mixin-targets.json')])
assert scanner['exitCode']==0,'Preserve static scanner failure'
engine=run('native-attribute-engine',[str(JAVA/'java.exe'),'-Xmx256m','-Duser.home='+str(OUT/'isolated-home'),'-Djava.io.tmpdir='+str(OUT/'isolated-tmp'),'-cp',str(classes)+';'+cp,'NonManorAttributeEngineV1',str(OUT/'native-attribute-engine.json')],timeout=45)
for name,expected in inputs.items():assert sha(name)==expected,name
scan=load(OUT/'callsites-and-mixin-targets.json')
subject_mixin_targets=[row for row in scan['declaredRuntimeArchiveMixinTargets'] if any('NonManorFinale' in target or 'TeaTreeBossEntity' in target or 'AttributeMap' in target or 'AttributeInstance' in target for target in row['targets'])]
engine_report=load(OUT/'native-attribute-engine.json') if (OUT/'native-attribute-engine.json').exists() else None
receipt={'originalSha256':sha(original),'candidateSha256':sha(current),'allFrozenInputHashesUnchanged':True,'originalCollectionActuallySyncable':True,'incorrectNonSyncableScopeHypothesisRejected':True,'directAndBootstrapCallsites':len(scan['directAndBootstrapCallsites']),'subjectClasses':len(scan['subjectMethods']),'runtimeArchiveMixinDeclarations':len(scan['declaredRuntimeArchiveMixinTargets']),'unresolvedRuntimeArchiveMixinDeclarations':scan['unresolvedRuntimeArchiveMixinDeclarations'],'declaredRelevantArchiveMixinTargets':subject_mixin_targets,'nativeAttributeArithmeticProbeExitCode':engine['exitCode'],'nativeAttributeArithmeticChecks':engine_report.get('checks',0) if engine_report else 0,'nativeAttributeArithmeticProbeAccepted':engine['exitCode']==0 and engine_report is not None and engine_report.get('failures')==0,'nativeBossAdmissionOrFullRuntimeAccepted':False,'liveMixinTransformationCompleteProofClaimed':False,'difficultyUnlockOrVictoryDataChanged':False,'actualPlayerLoginClaimed':False,'productChanged':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'formalInstancesReadOnly':True,'weaponsAndSessionPaused':True,'next':'Review exact callers, archive-config targets and native arithmetic result. Require actual eligible Boss admission and scaling/reload evidence before any product change; do not synthesize unlock/victory or use raw static differences as acceptance.'}
save(OUT/'audit-receipt.json',receipt)
print(json.dumps({key:receipt[key] for key in ['originalCollectionActuallySyncable','directAndBootstrapCallsites','runtimeArchiveMixinDeclarations','nativeAttributeArithmeticProbeExitCode','nativeAttributeArithmeticChecks','nativeAttributeArithmeticProbeAccepted','nativeBossAdmissionOrFullRuntimeAccepted','productChanged']},ensure_ascii=False),flush=True)
