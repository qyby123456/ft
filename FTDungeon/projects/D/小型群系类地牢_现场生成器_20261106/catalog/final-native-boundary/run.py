import json
import os
import subprocess
import sys
import time
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import save,load,digest


def main():
    environment=os.environ.copy()
    environment['JAVA_HOME']=r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    gradle=r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
    command=[os.environ['COMSPEC'],'/d','/c',gradle,'--no-daemon','--offline','-Dnet.minecraftforge.gradle.check.certs=false','--console=plain','--project-cache-dir',str(ROOT/'gradle-cache'),'-I',str(ROOT/'verification/overlay.gradle'),'-PauditSource='+str(ROOT/'verification'),'-PauditWorld='+str(ROOT/'run'),'runGameTestServer']
    progress={'component':'catalog-final-native-boundary','status':'ONE_OWNED_GAMETEST_BUILD_AND_RUN_HANDLE','cwd':str(ROOT/'project'),'command':command,'serverConcurrency':'one catalog server only; standard Gradle launcher may have separate build daemon'}
    save(ROOT/'progress.json',progress)
    with (ROOT/'build.log').open('w',encoding='utf-8') as stream:
        process=subprocess.Popen(command,cwd=ROOT/'project',env=environment,stdout=stream,stderr=subprocess.STDOUT)
        progress['buildProcessId']=process.pid; save(ROOT/'progress.json',progress)
        while process.poll() is None:
            time.sleep(1)
    progress.update({'status':'OWNED_BUILD_OR_WORLD_RUN_EXITED','exitCode':process.returncode,'nativeLog':'build.log','nativeLogSha256':digest(ROOT/'build.log')})
    if (ROOT/'native-boundary-report.json').exists():
        report=load(ROOT/'native-boundary-report.json');progress.update({'reportStatus':report['status'],'attempted':report['attempted'],'passed':report['passed'],'directed':report['directed']})
    save(ROOT/'progress.json',progress)
    print(json.dumps(progress,ensure_ascii=False))


if __name__=='__main__':
    main()
