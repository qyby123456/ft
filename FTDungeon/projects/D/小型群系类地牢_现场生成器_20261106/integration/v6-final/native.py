import datetime
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
import integrate


def main():
    mode=sys.argv[1] if len(sys.argv)>1 else 'compile'
    frozen=integrate.load(HOME/'progress.json');snapshot=Path(frozen['snapshot'])
    stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    root=HOME/'native-run'/(stamp+'-'+mode);project=root/'project'
    lock=integrate.load(snapshot/'snapshot-lock.json')
    for name,entry in lock['components'].items():
        for relative,expected in entry['files'].items():
            if integrate.digest(integrate.contained(snapshot/name,relative))!=expected:
                raise integrate.GateError('Native frozen input mismatch '+relative)
    shutil.copytree(snapshot/'runtime',project,ignore=lambda directory,names:{name for name in names if Path(directory)==snapshot/'runtime' and name in {'run','history','build'}})
    verification=root/'verification';shutil.copytree(HOME/'world-tests',verification/'world-tests')
    shutil.copyfile(HOME/'overlay.gradle',verification/'overlay.gradle')
    namespace={'compile':'ftbd_matrix','matrix':'ftbd_matrix','global':'ftbd_global','interaction':'ftbd_usepath','protection':'ftbd_protection','combined':'ftbd_combined','boundary':'ftbd_boundary','sidegate':'ftbd_sidegate'}[mode]
    fixture=verification/('test-resources/data/'+namespace+'/structures/s8_05.nbt');fixture.parent.mkdir(parents=True)
    shutil.copyfile(snapshot/'catalog/nine-room-package-v5/data/biomedungeon/structures/s8_05.nbt',fixture)
    integrate.save(root/'verification-lock.json',{str(path.relative_to(verification)):integrate.digest(path) for path in verification.rglob('*') if path.is_file()})
    environment=os.environ.copy();environment['JAVA_HOME']=str(Path(os.environ['APPDATA'])/'.minecraft/runtime/java-runtime-gamma-snapshot')
    gradle=Path(os.environ['USERPROFILE'])/'.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat'
    result=dict(status='RUNNING',mode=mode,root=str(root),snapshot=str(snapshot),jarSha256=frozen['jarSha256'],sourceCompilationOnly=mode=='compile',finalWorldEvidence=False,cases={})
    progress=HOME/(mode+'-progress.json');integrate.save(progress,result)
    turns=[0] if mode in ('compile','matrix','boundary') else list(map(int,sys.argv[2:])) or list(range(4))
    if mode=='protection':turns=list(range(6))
    for turn in turns:
        folder=root/('q'+str(turn));folder.mkdir()
        if mode in ('global','interaction','protection','combined','sidegate'):
            installed=integrate.load(HOME/'installed-progress.json')
            if installed['jarSha256']!=frozen['jarSha256']:
                raise integrate.GateError('Installed/native exact artifact mismatch')
            source=Path(installed['root'])/('q'+str(0 if mode=='protection' else turn))
            completed=integrate.load(source/'result.json')
            if completed['status']=='FAIL':raise integrate.GateError('Cannot replay failed installed case')
            shutil.copytree(source/'world',folder/'world')
        command=[os.environ['COMSPEC'],'/d','/c',str(gradle),'--no-daemon','--offline','-Dnet.minecraftforge.gradle.check.certs=false','--console=plain','--project-cache-dir',str(root/'gradle-cache'),'-I',str(verification/'overlay.gradle'),'-PauditSource='+str(verification),'-PauditWorld='+str(folder),'-PauditMatrix='+str(snapshot/'catalog/nine-room-package-v5/path-check-matrix.json'),'-PauditContract='+str(snapshot/'continuous-certificate/contract.json'),'-PauditSourceMap='+str(snapshot/'catalog/nine-room-package-v5/source-map.json'),'-PauditNamespace='+namespace,'gameTestClasses' if mode=='compile' else 'runGameTestServer']
        if mode=='boundary':
            matrix=integrate.load(HOME/'matrix-progress.json')
            if matrix['jarSha256']!=frozen['jarSha256']:raise integrate.GateError('Boundary/matrix artifact mismatch')
            matrix_root=Path(matrix['root']);shutil.copytree(matrix_root/'q0/world',folder/'world')
            command.insert(-1,'-PauditFiniteCertificate='+str(matrix_root/'finite-certificate.json'))
        if mode=='protection':command.insert(-1,'-PauditEvent='+('break','place','bucket','piston','fluid','explosion')[turn])
        for old_report in (folder/'world').glob('INDEPENDENT_*REPORT.json'):
            old_report.unlink()
        log=folder/'native.log'
        with log.open('w',encoding='utf-8') as stream:
            process=subprocess.Popen(command,cwd=project,stdout=stream,stderr=subprocess.STDOUT,env=environment)
            entry=dict(status='RUNNING',launcherPid=process.pid,parentPid=os.getpid(),command=command,cwd=str(project),log=str(log));result['cases'][str(turn)]=entry;integrate.save(progress,result)
            while process.poll() is None:time.sleep(2)
        text=log.read_text(encoding='utf-8',errors='replace')
        entry.update(status='PASS_COMPILE_ONLY' if mode=='compile' and process.returncode==0 else 'EXECUTED' if process.returncode==0 and 'All 1 required tests passed' in text else 'FAIL',exitCode=process.returncode,logSha256=integrate.digest(log))
        required_report=folder/'world'/('INDEPENDENT_SIDEGATE_REPORT.json' if mode=='sidegate' else 'INDEPENDENT_NATIVE_REPORT.json')
        if mode!='compile' and not required_report.is_file():entry['status']='FAIL_MISSING_FRESH_REPORT'
        for name in ('INDEPENDENT_NATIVE_REPORT.json','INDEPENDENT_NATIVE_SHAPES.json','INDEPENDENT_SIDEGATE_REPORT.json','INDEPENDENT_GLOBAL_REPORT.json','INDEPENDENT_NATIVE_STATE_REVIEW.json'):
            report=folder/'world'/name
            if report.is_file():entry[name]=dict(path=str(report),sha256=integrate.digest(report))
        integrate.save(progress,result)
        if entry['status'].startswith('FAIL'):break
    result['status']='COMPILE_ONLY_PASS_NO_WORLD_EVIDENCE' if mode=='compile' and all(entry['status']=='PASS_COMPILE_ONLY' for entry in result['cases'].values()) else 'EXECUTED_REPORTS_REQUIRE_INDEPENDENT_DECISION' if all(entry['status']=='EXECUTED' for entry in result['cases'].values()) else 'FAILED'
    integrate.save(progress,result);integrate.save(root/'result.json',result);print(result['status'])
    if result['status']=='FAILED':raise SystemExit(1)


if __name__=='__main__':main()
