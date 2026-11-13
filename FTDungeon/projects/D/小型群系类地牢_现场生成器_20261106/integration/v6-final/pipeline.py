import datetime
from pathlib import Path
import subprocess
import sys
import time

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
import integrate


def main():
    frozen=integrate.load(HOME/'progress.json');installed=integrate.load(HOME/'installed-progress.json');expected_root=installed['root']
    result=dict(status='WAITING_EXISTING_INSTALLED_HANDLE_CHAIN_NO_DUPLICATE_WORLD',jarSha256=frozen['jarSha256'],installedRoot=expected_root,steps=[],startedAt=integrate.stamp(),accepted=False,productionPromotion=False)
    progress=HOME/'pipeline-progress.json';integrate.save(progress,result)
    while True:
        installed=integrate.load(HOME/'installed-progress.json')
        if installed['root']!=expected_root or installed['jarSha256']!=frozen['jarSha256']:raise integrate.GateError('Installed run identity changed while awaiting actual existing cases')
        if installed['status']!='RUNNING':break
        time.sleep(3)
    if installed['status']!='INSTALLED_CASES_COMPLETE_INDEPENDENT_GATES_PENDING':raise integrate.GateError('Final exact installed cases failed; no replay/approval')
    for arguments in [('native.py','combined'),('native.py','boundary'),('pause_io.py',),('commit_negatives.py',),('native.py','protection'),('restart_saved.py',)]:
        entry=dict(arguments=list(arguments),status='RUNNING');result['steps'].append(entry);result['status']='RUNNING';integrate.save(progress,result)
        log=HOME/('pipeline-'+arguments[0].replace('.py','')+('-'+arguments[1] if len(arguments)>1 else '')+'.log')
        with log.open('w',encoding='utf-8')as output:
            process=subprocess.Popen([sys.executable,'-B',str(HOME/arguments[0]),*arguments[1:]],cwd=HOME,stdout=output,stderr=subprocess.STDOUT)
            entry.update(pid=process.pid,log=str(log));integrate.save(progress,result)
            while process.poll() is None:time.sleep(3)
        entry.update(exitCode=process.returncode,logSha256=integrate.digest(log),status='EXITED_REQUIRES_REPORT_DECISION')
        integrate.save(progress,result)
        if process.returncode:result['status']='HARNESS_EXCEPTION_STOPPED_NO_COMMIT';integrate.save(progress,result);return
    result['status']='SEQUENTIAL_NATIVE_AND_NEGATIVE_TESTS_EXECUTED_REPORT_DECISION_PENDING';integrate.save(progress,result)


if __name__=='__main__':main()
