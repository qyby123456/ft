import ast
import hashlib
import json
import os
import shutil
import subprocess
import time
import uuid
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def properties(path):
    values={}
    for line in Path(path).read_text(encoding='ascii').splitlines():
        if not line or line.startswith(('#','!')) or '=' not in line:continue
        key,value=line.split('=',1);values[key]=value.replace(chr(92)+':',':').replace(chr(92)+'=','=')
    return values
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
archive_path=WORK/'reports/nonmanor-normal-admission-archive-v1-20261114/archive-receipt.json';archived=load(archive_path)
assert archived['allReferencedPayloadHashesVerified'] and sha(ROOT/archived['archiveRelativePath'])==archived['archiveSha256']
manifest_path=ROOT/archived['seedWorldManifestRelativePath'];assert sha(manifest_path)==archived['seedWorldManifestSha256'];manifest=load(manifest_path)
assert manifest['allSourceNormalSaveAndExitChecksAccepted'] and not manifest['failedWorldAllowedAsSeed']
prior_path=ROOT/manifest['seedReportRelativePath'];assert sha(prior_path)==manifest['seedReportSha256'];prior=load(prior_path)
assert prior['actualNormalBossAdmissionBaselineAccepted'] and prior['probe']['failures']==0 and prior['terminal']['completeSave'] and prior['terminal']['exitCode']==0
for name,expected in prior['inputHashes'].items():assert sha(name)==expected,name
seed_world=ROOT/manifest['seedWorldRelativePath'];assert {p.relative_to(seed_world).as_posix() for p in seed_world.rglob('*') if p.is_file()}==set(manifest['files'])
for relative,entry in manifest['files'].items():assert (seed_world/relative).stat().st_size==entry['bytes'] and sha(seed_world/relative)==entry['sha256'],relative
failed_path=WORK/'reports/nonmanor-normal-reload-v1-a4cad831.json';failed=load(failed_path);assert failed['probe']['failures']==1 and failed['terminal']['exitCode']==0 and failed['terminal']['completeSave'] and failed['terminal']['processReaped']
for name,expected in failed['inputHashes'].items():assert sha(name)==expected,name
second_failed_path=WORK/'reports/nonmanor-normal-reload-v2-892eed0c.json';second_failed=load(second_failed_path);assert second_failed['probe']['failures']==1 and 'actual entity callbacks' in second_failed['probe']['failure'] and second_failed['terminal']['exitCode']==0 and second_failed['terminal']['completeSave'] and second_failed['terminal']['processReaped']
for name,expected in second_failed['inputHashes'].items():assert sha(name)==expected,name
assert shutil.disk_usage(ROOT).free>300*1024*1024,'Fail closed before copying complete28mods and immutable accepted world'
build_path=WORK/'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json';built=load(build_path);boss=Path(built['jar']);assert sha(boss)=='c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
prior_server=Path(prior['attempt'])/'server';prior_mods=sorted((prior_server/'mods').glob('*.jar'));assert len(prior_mods)==28
old_command_path=Path(prior['attempt'])/'runtime-command.json';old_response=Path(prior['attempt'])/'actual-official-dev198.args'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';context=load(context_path);sdk=[]
for entry in context['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()=='.jar' and path.is_file():
        with zipfile.ZipFile(path) as bundle:
            if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(entry)
attempt=WORK/'build'/('nonmanor-normal-reload-v3-'+uuid.uuid4().hex[:8]);attempt.mkdir();server=attempt/'server';server.mkdir()
for folder in ['observer-classes','observer-resources/META-INF','isolated-home','isolated-tmp']:(attempt/folder).mkdir(parents=True)
shutil.copytree(prior_server/'mods',server/'mods',copy_function=shutil.copyfile)
for source in prior_mods:
    target=server/'mods'/source.name;assert sha(source)==sha(target) and source.stat().st_ino!=target.stat().st_ino and target.stat().st_nlink==1
for folder in ['recipe-inputs','kubejs']:
    if (prior_server/folder).exists():shutil.copytree(prior_server/folder,server/folder,copy_function=shutil.copyfile)
shutil.copytree(seed_world,server/'world',copy_function=shutil.copyfile)
for relative,entry in manifest['files'].items():
    source=seed_world/relative;target=server/'world'/relative;assert sha(target)==entry['sha256'] and source.stat().st_ino!=target.stat().st_ino and target.stat().st_nlink==1
expected_rows=[];stopping={row['uuid']:row for row in prior['probe']['serverStoppingSnapshots']};assert len(stopping)==20
for row in prior['probe']['cases']:
    snapshot=stopping[row['afterCompleteJoinSnapshot']['uuid']]
    expected_rows.append({'savedSnapshot':snapshot,'withPermanentAddedModifier':row['withPermanentAddedModifier'],'publicButcherCloneInitializationUsed':row['publicButcherCloneInitializationUsed']})
expected_path=attempt/'immutable-expected-seed.json';save(expected_path,{'acceptedNormalSaveAndExitSeed':True,'sourceReportSha256':sha(prior_path),'sourceWorldManifestSha256':sha(manifest_path),'cases':expected_rows})
properties_template=attempt/'server-properties-template.txt';shutil.copyfile(Path(prior['attempt'])/'server-properties-template.txt',properties_template);shutil.copyfile(properties_template,server/'server.properties')
source=WORK/'tests/NonManorNormalReloadProbeV3.java';compile_command=[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',str(boss)+';'+';'.join(sdk),'-d',str(attempt/'observer-classes'),str(source)]
save(attempt/'compile-command.json',compile_command)
with (attempt/'compile.log').open('xb') as stream:compiled=subprocess.run(compile_command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT,timeout=90)
save(attempt/'compile-terminal.json',{'exitCode':compiled.returncode});assert compiled.returncode==0,str(attempt/'compile.log')
metadata=attempt/'observer-resources/META-INF/neoforge.mods.toml';shutil.copyfile(Path(prior['attempt'])/'observer-resources/META-INF/neoforge.mods.toml',metadata)
response=attempt/old_response.name;shutil.copyfile(old_response,response);command=load(old_command_path)
for index,value in enumerate(command):
    if value.startswith('-Dfml.modFolders='):
        minecraft=[part for part in value.removeprefix('-Dfml.modFolders=').split(';') if part.startswith('minecraft%%')];assert len(minecraft)==2
        command[index]='-Dfml.modFolders='+';'.join(minecraft+['ftboss_nonmanorprobe%%'+str(attempt/'observer-classes'),'ftboss_nonmanorprobe%%'+str(attempt/'observer-resources')])
    elif value.startswith('-Duser.home='):command[index]='-Duser.home='+str(attempt/'isolated-home')
    elif value.startswith('-Djava.io.tmpdir='):command[index]='-Djava.io.tmpdir='+str(attempt/'isolated-tmp')
    elif value.startswith('@'):command[index]='@'+str(response)
    elif value.startswith('-Xmx'):command[index]='-Xmx3G'
command.insert(1,'-Dftboss.nonmanor.seed='+str(expected_path));assert not any(value.startswith('-Dftboss.validation.') for value in command)
input_paths=[Path(__file__),source,failed_path,second_failed_path,WORK/'build/nonmanor-normal-reload-clone-audit-v1-20261114/clone-persistence-declared-contract.json',archive_path,ROOT/archived['archiveRelativePath'],manifest_path,prior_path,expected_path,build_path,boss,old_command_path,old_response,context_path,metadata,response,properties_template,*prior_mods,*sorted((server/'mods').glob('*.jar'))]
input_paths.extend(seed_world/relative for relative in manifest['files']);input_paths.extend(path for path in (attempt/'observer-classes').rglob('*') if path.is_file())
inputs={str(path):sha(path) for path in input_paths};save(attempt/'inputs-before.json',inputs);save(attempt/'runtime-command.json',command)
assert shutil.disk_usage(ROOT).free>80*1024*1024,'Complete environment copied; preserve attempt if native reserve insufficient'
environment=os.environ.copy();environment.pop('MOD_CLASSES',None);stop_sent=False;stop_reason=None
with (attempt/'runtime.log').open('x',encoding='utf-8') as stream:
    process=subprocess.Popen(command,cwd=server,env=environment,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT,text=True,encoding='utf-8')
    save(attempt/'live-process.json',{'pid':process.pid,'attempt':str(attempt),'worldByteIdenticalToAcceptedNormalSaveSeedBeforeLaunch':True,'completeTwentyEightModEnvironmentIndependentlyCopied':True,'actualPlayerLoginAttempted':False})
    for private in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
        state=load(private);state.update({'activeRuntimePids':[process.pid],'activeBossNativePid':process.pid,'activeRuntimeAttempt':str(attempt),'lastBossNativeTerminal':False,'currentWorkstream':'boss_nonmanor_ordinary_new_jvm_native_entity_reload'});private.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({'pid':process.pid,'attempt':str(attempt),'originalSeedUuidsExpected':20}),flush=True)
    deadline=time.monotonic()+400
    while process.poll() is None:
        reason='native normal reload report written' if (server/'actual-nonmanor-normal-reload-v3.json').exists() else 'low disk guard' if shutil.disk_usage(ROOT).free<64*1024*1024 else 'bounded startup observation exceeded' if time.monotonic()>deadline else None
        if reason and not stop_sent:
            process.stdin.write('stop\n');process.stdin.flush();stop_sent=True;stop_reason=reason;save(attempt/'normal-stop-request.json',{'pid':process.pid,'reason':reason,'nativeInput':'stop'})
        time.sleep(0.25)
    code=process.wait();process.stdin.close()
text=(attempt/'runtime.log').read_text(encoding='utf-8',errors='replace');probe=load(server/'actual-nonmanor-normal-reload-v3.json') if (server/'actual-nonmanor-normal-reload-v3.json').exists() else None
expected=properties(properties_template);actual=properties(server/'server.properties');controls={key:actual.get(key)==value for key,value in expected.items()};save(attempt/'server-properties-readback.json',{'criticalProperties':controls,'allCriticalPropertiesPreserved':all(controls.values())})
terminal={'exitCode':code,'stopSent':stop_sent,'stopReason':stop_reason,'completeSave':'All dimensions are saved' in text,'processReaped':True,'inputHashesUnchanged':all(sha(name)==digest for name,digest in inputs.items()),'criticalServerConfigurationPreserved':all(controls.values()),'originalAcceptedSeedWorldHashesUnchanged':all(sha(seed_world/relative)==entry['sha256'] for relative,entry in manifest['files'].items())};save(attempt/'terminal.json',terminal)
accepted=bool(probe and probe['failures']==0 and probe.get('ordinaryNoPlayerEighteenEntityReloadAccepted') and len(probe['cases'])==20 and len(probe.get('serverStoppingSnapshots',[]))==18 and terminal['exitCode']==0 and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['criticalServerConfigurationPreserved'] and terminal['originalAcceptedSeedWorldHashesUnchanged'])
report={'attempt':str(attempt),'probe':probe,'terminal':terminal,'inputHashes':inputs,'ordinaryNoPlayerEighteenEntityReloadAccepted':accepted,'originalTwentyEntityStrictReloadAccepted':False,'publicCloneFixturePersistenceAccepted':False,'curiosActualPlayerNewJvmLoginAccepted':False,'authenticatedSummonAccepted':False,'actualImpossibleAdmissionAccepted':False,'playerCountScalingChangeAccepted':False,'difficultyUnlockVictoryOrAuthenticationModified':False,'fullBossMigrationComplete':False,'productChanged':False,'installable':False,'deployed':False}
report_path=WORK/'reports'/('nonmanor-normal-reload-v3-'+attempt.name.rsplit('-',1)[1]+'.json');save(report_path,report)
for private in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(private);state.update({'activeRuntimePids':[],'activeBossNativePid':None,'activeRuntimeAttempt':None,'lastBossNativeTerminal':True,'bossNonManorOrdinaryNoPlayerEighteenEntityReloadAccepted':accepted,'bossNonManorOriginalTwentyEntityReloadAccepted':False,'bossNonManorOrdinaryReloadReceipt':str(report_path),'currentWorkstream':'boss_nonmanor_ordinary_reload_terminal_review_archive_source_publication'});private.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'report':str(report_path),'ordinaryNoPlayerEighteenEntityReloadAccepted':accepted,'checks':probe.get('checks',0) if probe else 0,'terminal':terminal}),flush=True)
