import base64
import hashlib
import json
import os
import shutil
import subprocess
import time
import uuid
import zipfile
import sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
assert shutil.disk_usage(ROOT).free>300*1024*1024
expectation=sys.argv[1];assert expectation in ['baseline','fixed']
built_path=WORK/('build/boss-manor-native-events-v3-20261114/candidate-build-receipt.json' if expectation=='baseline' else 'build/boss-manor-original-entry-v2-20261114/candidate-build-receipt.json');built=load(built_path);boss=Path(built['jar']);assert sha(boss)==('e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c' if expectation=='baseline' else 'a49f1772a60d72a4240eb3f293b29fb38629a2112ad326120e81377eeeeca1ec')
prior_path=WORK/'reports/zicai-curio-client-v13-candidate-6559f481.json';prior=load(prior_path);assert prior['probe']['failures']==0 and prior['terminal']['clientProcessReaped'];old=Path(prior['attempt']);expanded_path=old/'expanded-runtime-command.json';expanded=load(expanded_path)
assert expanded[expanded.index('--accessToken')+1]=='0' and expanded[expanded.index('--username')+1]=='ft_fx_probe'
mods=sorted((old/'game/mods').glob('*.jar'));assert len(mods)==28
attempt=WORK/'build'/('manor-raw-guard-boundary-'+expectation+'-v1-'+uuid.uuid4().hex[:8]);attempt.mkdir();game=attempt/'game';game.mkdir();expectation_path=game/'raw-guard-expectation.json';save(expectation_path,{'expectation':expectation,'noRuntimeProductFlags':True})
for folder in ['observer-classes','observer-resources/META-INF','agent-classes','captured-defined-classes','isolated-home','isolated-tmp']:(attempt/folder).mkdir(parents=True)
for folder in ['mods','recipe-inputs','kubejs']:
    if (old/'game'/folder).exists():shutil.copytree(old/'game'/folder,game/folder,copy_function=shutil.copyfile)
for path in mods:
    copied=game/'mods'/path.name
    with zipfile.ZipFile(path) as bundle:is_boss='net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist()
    if is_boss:shutil.copyfile(boss,copied)
    assert sha(copied)==sha(boss if is_boss else path) and copied.stat().st_ino!=path.stat().st_ino and copied.stat().st_nlink==1
shutil.copytree(old/'natives',attempt/'natives',copy_function=shutil.copyfile,ignore=shutil.ignore_patterns('jna*.dll','jna*.dll.x'))
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';sdk=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()=='.jar' and path.is_file():
        with zipfile.ZipFile(path) as jar:
            if 'net/farmingtales/ftboss/FTBossMod.class' in jar.namelist():continue
    sdk.append(entry)
agent_source=WORK/'tests/LoadedBossClassCaptureV5.java';source=WORK/'tests/ManorRawGuardBoundaryNativeV1.java'
for label,command in [('agent',[str(JAVA/'javac.exe'),'--release','21','-encoding','UTF-8','-d',str(attempt/'agent-classes'),str(agent_source)]),('observer',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',str(boss)+';'+';'.join(sdk),'-d',str(attempt/'observer-classes'),str(source)])]:
    save(attempt/(label+'-compile-command.json'),command)
    with (attempt/(label+'-compile.log')).open('xb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT,timeout=90)
    save(attempt/(label+'-compile-terminal.json'),{'exitCode':result.returncode});assert result.returncode==0,label
agent=attempt/'loaded-boss-capture.jar'
with zipfile.ZipFile(agent,'x',compression=zipfile.ZIP_DEFLATED) as jar:
    jar.writestr('META-INF/MANIFEST.MF','Manifest-Version: 1.0\nPremain-Class: ftboss.nativeprobe.LoadedBossClassCaptureV5\nCan-Redefine-Classes: false\nCan-Retransform-Classes: false\n\n')
    for path in sorted((attempt/'agent-classes').rglob('*.class')):jar.write(path,path.relative_to(attempt/'agent-classes').as_posix())
metadata=attempt/'observer-resources/META-INF/neoforge.mods.toml';metadata.write_text('modLoader="javafml"\nloaderVersion="[4,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftboss_clientfxprobe"\nversion="1.0"\ndisplayName="Actual original Boss raw guard boundary observer"\n[[dependencies.ftboss_clientfxprobe]]\nmodId="ftboss"\ntype="required"\nversionRange="[0,)"\nordering="AFTER"\nside="CLIENT"\n',encoding='utf-8')
response_index=next(index for index,value in enumerate(expanded) if value.startswith('@'));old_response=Path(expanded[response_index][1:]);response_text=old_response.read_text(encoding='mbcs');assert '--launchTarget forgeclientdev' in response_text
command=[]
for value in expanded:
    if value.startswith(('-Dftboss.zicaicurio.','-Dftboss.validation.')):continue
    if value.startswith('-Dfml.modFolders='):
        roots=[part for part in value.removeprefix('-Dfml.modFolders=').split(';') if part.startswith('minecraft%%')];value='-Dfml.modFolders='+';'.join(roots+['ftboss_clientfxprobe%%'+str(attempt/'observer-classes'),'ftboss_clientfxprobe%%'+str(attempt/'observer-resources')])
    elif value.startswith('-Duser.home='):value='-Duser.home='+str(attempt/'isolated-home')
    elif value.startswith('-Djava.io.tmpdir='):value='-Djava.io.tmpdir='+str(attempt/'isolated-tmp')
    elif value.startswith(('-Djava.library.path=','-Dorg.lwjgl.system.SharedLibraryExtractPath=','-Djna.tmpdir=')):value=value.split('=',1)[0]+'='+str(attempt/'natives')
    elif value==str(old/'game'):value=str(game)
    command.append(value)
command.insert(1,'-javaagent:'+str(agent)+'='+base64.urlsafe_b64encode(str(attempt/'captured-defined-classes').encode('utf-8')).decode('ascii'));assert not any(value.startswith('-Dftboss.') for value in command)
response_index=next(index for index,value in enumerate(command) if value.startswith('@'))
def quote(value):return '"'+value.replace(chr(92),chr(92)*2).replace('"',chr(92)+'"')+'"'
full=attempt/'full-client-launch.args';full.write_text('\n'.join(quote(value) for value in command[1:response_index])+'\n'+response_text+'\n'+'\n'.join(quote(value) for value in command[response_index+1:])+'\n',encoding='mbcs');command=[command[0],'@'+str(full)];save(attempt/'runtime-command.json',command)
options_template=attempt/'options-template.txt';options_template.write_text('pauseOnLostFocus:false\nrenderDistance:4\nsimulationDistance:5\nmaxFps:60\nguiScale:2\nfov:0.0\nonboardAccessibility:false\nparticles:0\nfullscreen:false\n',encoding='utf-8');shutil.copyfile(options_template,game/'options.txt')
inputs={str(path):sha(path) for path in [Path(__file__),source,agent_source,expectation_path,built_path,boss,prior_path,expanded_path,old_response,context_path,agent,metadata,full,options_template,*mods,*sorted((game/'mods').glob('*.jar'))]}
for folder in ['observer-classes','agent-classes','natives']:
    for path in (attempt/folder).rglob('*'):
        if path.is_file():inputs[str(path)]=sha(path)
save(attempt/'inputs-before.json',inputs);assert shutil.disk_usage(ROOT).free>80*1024*1024
process_environment=os.environ.copy();process_environment.pop('MOD_CLASSES',None)
with (attempt/'runtime.log').open('x',encoding='utf-8') as stream:
    process=subprocess.Popen(command,cwd=game,env=process_environment,stdout=stream,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    save(attempt/'live-process.json',{'pid':process.pid,'attempt':str(attempt),'actualClient':True,'freshWorldOnly':True,'explicitOfflineFixture':True,'authenticated':False,'classCaptureTransformerReturnsNull':True})
    for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
        state=load(path);state.update({'activeRuntimePids':[process.pid],'activeBossNativePid':process.pid,'activeRuntimeAttempt':str(attempt),'lastBossNativeTerminal':False,'currentWorkstream':'boss_original_raw_guard_boundary_'+expectation+'trial'});path.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({'pid':process.pid,'attempt':str(attempt),'actualClient':True,'explicitOfflineFixture':True,'authenticated':False}),flush=True)
    deadline=time.monotonic()+480;stop_written=False;agent_stop=False
    while process.poll() is None:
        observed=(attempt/'runtime.log').read_text(encoding='utf-8',errors='replace');fatal='Crash report saved to' in observed
        reason='low disk guard' if shutil.disk_usage(ROOT).free<64*1024*1024 else 'bounded startup observation expired' if time.monotonic()>deadline else 'fatal bootstrap preserved' if fatal else None
        if reason and not stop_written:save(game/'normal-stop-request.json',{'reason':reason,'nativeStopOnly':True});stop_written=True;grace=time.monotonic()+30
        if stop_written and not agent_stop and time.monotonic()>grace:
            agent_stop=True;stop_jar=WORK/'build/client-normal-stop-v1/client-normal-stop.jar';stop_command=[str(JAVA/'java.exe'),'--add-modules','jdk.attach','-cp',str(stop_jar),'ClientNormalStopAttach',str(process.pid),str(stop_jar),'normal-stop-agent-receipt.json'];stopped=subprocess.run(stop_command,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=30);save(attempt/'cleanup-normal-stop-attach.json',{'exitCode':stopped.returncode,'output':stopped.stdout+stopped.stderr,'cleanupOnly':True})
        time.sleep(0.5)
    code=process.wait();save(attempt/'raw-jvm-terminal.json',{'exitCode':code,'processReaped':True})
text=(attempt/'runtime.log').read_text(encoding='utf-8',errors='replace');probe_path=game/'actual-manor-raw-guard-boundary.json';probe=load(probe_path) if probe_path.exists() else None
terminal={'exitCode':code,'clientProcessReaped':True,'integratedWorldCompleteSave':'All dimensions are saved' in text,'inputHashesUnchanged':all(Path(path).exists() and sha(path)==expected for path,expected in inputs.items()),'missingInputs':[path for path in inputs if not Path(path).exists()],'finalDefinedClassesCaptured':all((attempt/'captured-defined-classes'/(name+'.class')).exists() for name in ['net_farmingtales_ftboss_entity_EasterBossEntity','net_minecraft_world_entity_LivingEntity','net_farmingtales_ftboss_boss_ai_ManorFinalDamage']),'observerRequestedNormalStop':probe_path.exists()};save(attempt/'terminal.json',terminal)
accepted=bool(probe and probe['failures']==0 and probe.get('actualRawGuardBoundaryCasesAccepted') and probe.get('originalNegativeProtectionControlsAccepted') and probe.get('completedModes')==15 and probe.get('expectation')==expectation and terminal['exitCode']==0 and terminal['integratedWorldCompleteSave'] and terminal['inputHashesUnchanged'] and terminal['finalDefinedClassesCaptured'])
output=WORK/'reports'/(attempt.name+'.json');save(output,{'attempt':str(attempt),'probe':probe,'terminal':terminal,'inputHashes':inputs,'actualRawGuardBoundaryCasesAccepted':accepted,'expectation':expectation,'authenticated':False,'rawGuardBoundaryCasesAccepted':accepted,'productChanged':expectation=='fixed','fullBossMigrationComplete':False,'installable':False,'deployed':False})
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path);state.update({'activeRuntimePids':[],'activeBossNativePid':None,'activeRuntimeAttempt':None,'lastBossNativeTerminal':True,'currentWorkstream':'boss_original_raw_guard_boundary_terminal_audit','latestRawGuardBoundaryReceipt':str(output),'rawGuardBoundaryCasesAccepted':accepted});path.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'report':str(output),'actualRawGuardBoundaryCasesAccepted':accepted,'expectation':expectation,'checks':probe.get('checks') if probe else None,'failure':probe.get('failure','').splitlines()[:1] if probe else None,'terminal':terminal}),flush=True)
