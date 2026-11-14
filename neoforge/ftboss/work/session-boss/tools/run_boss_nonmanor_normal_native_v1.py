import hashlib
import json
import os
import shutil
import subprocess
import time
import uuid
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
assert shutil.disk_usage(ROOT).free>300*1024*1024,'Fail closed before copying the complete28modnativeenvironment'
build_path=WORK/'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json';built=load(build_path);boss=Path(built['jar']);assert sha(boss)=='c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
prior_path=WORK/'reports/zicai-curio-client-v13-candidate-6559f481.json';prior=load(prior_path);assert prior['probe']['failures']==0 and prior['terminal']['clientProcessReaped']
prior_game=Path(prior['attempt'])/'game';prior_mods=sorted((prior_game/'mods').glob('*.jar'));assert len(prior_mods)==28
seed_server_report_path=WORK/'reports/nonmanor-attribute-native-v5-33773afe.json';seed_server=load(seed_server_report_path);assert seed_server['nativeArithmeticAccepted'] and seed_server['terminal']['completeSave']
seed_attempt=Path(seed_server['attempt']);old_command_path=seed_attempt/'runtime-command.json';old_response=seed_attempt/'actual-official-dev198.args'
context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';context=load(context_path)
sdk=[]
import zipfile
for entry in context['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()=='.jar' and path.is_file():
        with zipfile.ZipFile(path) as bundle:
            if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(entry)
attempt=WORK/'build'/('nonmanor-normal-native-v1-'+uuid.uuid4().hex[:8]);attempt.mkdir();server=attempt/'server';server.mkdir()
for folder in ['observer-classes','observer-resources/META-INF','isolated-home','isolated-tmp']:(attempt/folder).mkdir(parents=True)
shutil.copytree(prior_game/'mods',server/'mods',copy_function=shutil.copyfile)
for source in prior_mods:
    target=server/'mods'/source.name;assert sha(source)==sha(target) and source.stat().st_ino!=target.stat().st_ino and target.stat().st_nlink==1
for folder in ['recipe-inputs','kubejs']:
    if (prior_game/folder).exists():shutil.copytree(prior_game/folder,server/folder,copy_function=shutil.copyfile)
assert not (server/'world').exists()
properties_template=attempt/'server-properties-template.txt';properties_template.write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=true\nmax-tick-time=-1\nlevel-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\nsync-chunk-writes=true\n',encoding='ascii');shutil.copyfile(properties_template,server/'server.properties')
source=WORK/'tests/NonManorNormalBossProbeV1.java'
compile_command=[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',str(boss)+';'+';'.join(sdk),'-d',str(attempt/'observer-classes'),str(source)]
save(attempt/'compile-command.json',compile_command)
with (attempt/'compile.log').open('xb') as stream:compiled=subprocess.run(compile_command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT,timeout=90)
save(attempt/'compile-terminal.json',{'exitCode':compiled.returncode});assert compiled.returncode==0,str(attempt/'compile.log')
metadata=attempt/'observer-resources/META-INF/neoforge.mods.toml';metadata.write_text('modLoader="javafml"\nloaderVersion="[4,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftboss_nonmanorprobe"\nversion="1.0"\ndisplayName="Actual ordinary Boss admission observer"\n[[dependencies.ftboss_nonmanorprobe]]\nmodId="ftboss"\ntype="required"\nversionRange="[0,)"\nordering="AFTER"\nside="SERVER"\n',encoding='utf-8')
response=attempt/old_response.name;shutil.copyfile(old_response,response)
command=load(old_command_path)
for index,value in enumerate(command):
    if value.startswith('-Dfml.modFolders='):
        minecraft=[part for part in value.removeprefix('-Dfml.modFolders=').split(';') if part.startswith('minecraft%%')];assert len(minecraft)==2
        command[index]='-Dfml.modFolders='+';'.join(minecraft+['ftboss_nonmanorprobe%%'+str(attempt/'observer-classes'),'ftboss_nonmanorprobe%%'+str(attempt/'observer-resources')])
    elif value.startswith('-Duser.home='):command[index]='-Duser.home='+str(attempt/'isolated-home')
    elif value.startswith('-Djava.io.tmpdir='):command[index]='-Djava.io.tmpdir='+str(attempt/'isolated-tmp')
    elif value.startswith('@'):command[index]='@'+str(response)
    elif value.startswith('-Xmx'):command[index]='-Xmx3G'
assert not any(value.startswith('-Dftboss.validation.') for value in command)
input_paths=[Path(__file__),source,build_path,boss,prior_path,seed_server_report_path,old_command_path,old_response,context_path,metadata,response,properties_template,*prior_mods,*sorted((server/'mods').glob('*.jar'))]
input_paths.extend(path for path in (attempt/'observer-classes').rglob('*') if path.is_file())
inputs={str(path):sha(path) for path in input_paths};save(attempt/'inputs-before.json',inputs);save(attempt/'runtime-command.json',command)
assert shutil.disk_usage(ROOT).free>80*1024*1024,'Complete native environment copied; preserve it and stop before JVM if reserve insufficient'
environment=os.environ.copy();environment.pop('MOD_CLASSES',None);stop_sent=False;stop_reason=None
with (attempt/'runtime.log').open('x',encoding='utf-8') as stream:
    process=subprocess.Popen(command,cwd=server,env=environment,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT,text=True,encoding='utf-8')
    save(attempt/'live-process.json',{'pid':process.pid,'attempt':str(attempt),'newFreshWorld':True,'completeTwentyEightModEnvironmentIndependentlyCopied':True,'purpose':'Actual normal Boss nativejoin,20fixtures,naturalcallbacksandnormalsave;notimpossibleadmission'})
    print(json.dumps({'pid':process.pid,'attempt':str(attempt),'normalBossFixturesPlanned':20}),flush=True)
    deadline=time.monotonic()+360
    while process.poll() is None:
        reason='native normal Boss report written' if (server/'actual-nonmanor-normal-boss.json').exists() else 'low disk guard' if shutil.disk_usage(ROOT).free<64*1024*1024 else 'bounded startup observation exceeded' if time.monotonic()>deadline else None
        if reason and not stop_sent:
            process.stdin.write('stop\n');process.stdin.flush();stop_sent=True;stop_reason=reason;save(attempt/'normal-stop-request.json',{'pid':process.pid,'reason':reason,'nativeInput':'stop'})
        time.sleep(0.25)
    code=process.wait();process.stdin.close()
text=(attempt/'runtime.log').read_text(encoding='utf-8',errors='replace');probe=load(server/'actual-nonmanor-normal-boss.json') if (server/'actual-nonmanor-normal-boss.json').exists() else None
expected=properties(properties_template);actual=properties(server/'server.properties');controls={key:actual.get(key)==value for key,value in expected.items()};save(attempt/'server-properties-readback.json',{'criticalProperties':controls,'allCriticalPropertiesPreserved':all(controls.values())})
terminal={'exitCode':code,'stopSent':stop_sent,'stopReason':stop_reason,'completeSave':'All dimensions are saved' in text,'processReaped':True,'inputHashesUnchanged':all(sha(name)==digest for name,digest in inputs.items()),'criticalServerConfigurationPreserved':all(controls.values())};save(attempt/'terminal.json',terminal)
accepted=bool(probe and probe['failures']==0 and probe.get('nativeOrdinaryAdmissionAndNaturalTicksAccepted') and len(probe['cases'])==20 and len(probe.get('serverStoppingSnapshots',[]))==20 and terminal['exitCode']==0 and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['criticalServerConfigurationPreserved'])
report={'attempt':str(attempt),'probe':probe,'terminal':terminal,'inputHashes':inputs,'actualNormalBossAdmissionBaselineAccepted':accepted,'actualImpossibleAdmissionAccepted':False,'playerCountScalingChangeAccepted':False,'newJvmReloadAccepted':False,'difficultyUnlockVictoryOrAuthenticationModified':False,'fullBossMigrationComplete':False,'productChanged':False,'installable':False,'deployed':False}
report_path=WORK/'reports'/('nonmanor-normal-native-v1-'+attempt.name.rsplit('-',1)[1]+'.json');save(report_path,report)
print(json.dumps({'report':str(report_path),'actualNormalBossAdmissionBaselineAccepted':accepted,'checks':probe.get('checks',0) if probe else 0,'terminal':terminal}),flush=True)
