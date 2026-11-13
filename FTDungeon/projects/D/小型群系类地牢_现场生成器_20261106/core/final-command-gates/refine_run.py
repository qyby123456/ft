import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time

ROOT=Path(__file__).resolve().parent


def main():
    deadline=time.monotonic()+180
    while time.monotonic()<deadline:
        path=ROOT/'restart-progress.json'
        if path.exists() and json.loads(path.read_text(encoding='utf-8'))['status']!='RUNNING':break
        time.sleep(1)
    else:raise RuntimeError('Initial server sequence still active; no overlapping launch')
    history=ROOT/'initial-before-refined-assertions'
    history.mkdir()
    for name in ('receipt-negative-progress.json','restart-progress.json'):
        shutil.copyfile(ROOT/name,history/name)
    for path in (ROOT/'installed').rglob('independent-*'):
        if path.is_file():
            target=history/path.relative_to(ROOT)
            target.parent.mkdir(parents=True,exist_ok=True)
            shutil.copyfile(path,target)
    for name in ('commit_negatives.py','restart_saved.py'):
        print('REFINED_STAGE_START '+name,flush=True)
        process=subprocess.run([sys.executable,'-B',str(ROOT/name)],cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',env=dict(os.environ,PYTHONIOENCODING='utf-8'))
        (ROOT/(name+'.refined-runner.log')).write_text(process.stdout,encoding='utf-8')
        print(process.stdout,flush=True)
        assert process.returncode==0,name


if __name__=='__main__':main()
