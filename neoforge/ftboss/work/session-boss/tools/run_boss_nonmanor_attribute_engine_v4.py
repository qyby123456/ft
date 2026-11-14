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
PARENT=WORK/'build/boss-nonmanor-finale-effective-v1-20261114'
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
inputs=load(PARENT/'native-engine-v2/inputs-before.json')
for name,expected in inputs.items():assert sha(name)==expected,name
failed=load(PARENT/'native-engine-v2/native-attribute-engine.json');assert failed['checks']==0 and 'LoadingModList' in failed['failure']
prior_v3=load(WORK/'reports/nonmanor-attribute-native-v3-293b7885.json');assert not prior_v3['nativeArithmeticAccepted'] and prior_v3['terminal']['clientOrServerProcessReaped']
assert 'StringIndexOutOfBoundsException' in (Path(prior_v3['attempt'])/'runtime.log').read_text(encoding='utf-8',errors='replace')
assert shutil.disk_usage(ROOT).free>110*1024*1024,'Fail closed before a new runtime if C free space is insufficient'
attempt=WORK/'build'/('nonmanor-attribute-native-v4-'+uuid.uuid4().hex[:8]);attempt.mkdir()
server=attempt/'server';server.mkdir()
for folder in ['observer-classes','observer-resources/META-INF','isolated-home','isolated-tmp']:(attempt/folder).mkdir(parents=True)
(server/'mods').mkdir()
(server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=true\nmax-tick-time=-1\nlevel-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\nsync-chunk-writes=true\n',encoding='ascii')
source=WORK/'tests/NonManorAttributeEngineV4.java';prior_compile=load(PARENT/'compile-command.json');classpath=prior_compile[prior_compile.index('-cp')+1]
compile_command=[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(attempt/'observer-classes'),str(source)]
save(attempt/'compile-command.json',compile_command)
with (attempt/'compile.log').open('xb') as stream:compiled=subprocess.run(compile_command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT,timeout=60)
save(attempt/'compile-terminal.json',{'exitCode':compiled.returncode});assert compiled.returncode==0,str(attempt/'compile.log')
metadata=attempt/'observer-resources/META-INF/neoforge.mods.toml'
metadata.write_text('modLoader="javafml"\nloaderVersion="[4,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftboss_attrprobe"\nversion="1.0"\ndisplayName="Scoped native attribute arithmetic observer"\n',encoding='utf-8')
prior_path=WORK/'reports/summon-fx-v8-candidate-b74fa0f4.json';prior=load(prior_path);assert prior['terminal']['exitCode']==0 and prior['terminal']['completeSave']
prior_attempt=Path(prior['attempt']);old_command_path=prior_attempt/'runtime-command.json';command=load(old_command_path)
old_response=prior_attempt/'actual-official-dev198.args';response=attempt/old_response.name;shutil.copyfile(old_response,response)
command=[value for value in command if not value.startswith(('-Dftboss.','-Dmixin.debug.export'))]
for index,value in enumerate(command):
    if value.startswith('-Dfml.modFolders='):
        minecraft=[part for part in value.removeprefix('-Dfml.modFolders=').split(';') if part.startswith('minecraft%%')];assert len(minecraft)==2
        command[index]='-Dfml.modFolders='+';'.join(minecraft+['ftboss_attrprobe%%'+str(attempt/'observer-classes'),'ftboss_attrprobe%%'+str(attempt/'observer-resources')])
    elif value.startswith('-Duser.home='):command[index]='-Duser.home='+str(attempt/'isolated-home')
    elif value.startswith('-Djava.io.tmpdir='):command[index]='-Djava.io.tmpdir='+str(attempt/'isolated-tmp')
    elif value.startswith('@'):command[index]='@'+str(response)
    elif value.startswith('-Xmx'):command[index]='-Xmx2G'
assert not list((server/'mods').iterdir()) and not (server/'world').exists()
inputs.update({str(path):sha(path) for path in [Path(__file__),source,metadata,prior_path,old_command_path,old_response,response,server/'server.properties']})
save(attempt/'inputs-before.json',inputs);save(attempt/'runtime-command.json',command)
stop_sent=False;stop_reason=None;environment=os.environ.copy();environment.pop('MOD_CLASSES',None)
with (attempt/'runtime.log').open('x',encoding='utf-8') as stream:
    process=subprocess.Popen(command,cwd=server,env=environment,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT,text=True,encoding='utf-8')
    save(attempt/'live-process.json',{'pid':process.pid,'attempt':str(attempt),'newFreshWorld':True,'noBossOrWeaponModLoaded':True,'purpose':'Normal FML bootstrap of unchanged native attribute arithmetic;notBoss admission'})
    print(json.dumps({'pid':process.pid,'attempt':str(attempt),'nativeDedicatedLoader':True}),flush=True)
    deadline=time.monotonic()+240
    while process.poll() is None:
        reason='native arithmetic report written' if (server/'actual-native-attribute-engine.json').exists() else 'native observer failure written' if (server/'native-loader-failure.txt').exists() else 'low disk guard' if shutil.disk_usage(ROOT).free<64*1024*1024 else 'bounded startup observation exceeded' if time.monotonic()>deadline else None
        if reason and not stop_sent:
            process.stdin.write('stop\n');process.stdin.flush();stop_sent=True;stop_reason=reason
            save(attempt/'normal-stop-request.json',{'pid':process.pid,'reason':reason,'nativeInput':'stop'})
        time.sleep(0.25)
    code=process.wait();process.stdin.close()
text=(attempt/'runtime.log').read_text(encoding='utf-8',errors='replace')
terminal={'exitCode':code,'stopSent':stop_sent,'stopReason':stop_reason,'completeSave':'All dimensions are saved' in text,'clientOrServerProcessReaped':True,'inputHashesUnchanged':all(sha(name)==expected for name,expected in inputs.items())}
save(attempt/'terminal.json',terminal)
probe=load(server/'actual-native-attribute-engine.json') if (server/'actual-native-attribute-engine.json').exists() else None
context=load(server/'native-loader-context.json') if (server/'native-loader-context.json').exists() else None
report={'attempt':str(attempt),'probe':probe,'nativeLoaderContext':context,'terminal':terminal,'inputHashes':inputs,'nativeArithmeticAccepted':code==0 and terminal['completeSave'] and terminal['inputHashesUnchanged'] and probe is not None and probe['failures']==0 and context is not None and context['dedicatedServer'],'actualBossAdmissionAccepted':False,'difficultyOrUnlockStateModified':False,'priorStandaloneFailuresRetained':True,'noPriorAcceptedBossControlsRerun':True,'productChanged':False,'installable':False,'deployed':False}
report_path=WORK/'reports'/('nonmanor-attribute-native-v4-'+attempt.name.rsplit('-',1)[1]+'.json');save(report_path,report)
print(json.dumps({'report':str(report_path),'nativeArithmeticAccepted':report['nativeArithmeticAccepted'],'checks':probe['checks'] if probe else 0,'terminal':terminal}),flush=True)
