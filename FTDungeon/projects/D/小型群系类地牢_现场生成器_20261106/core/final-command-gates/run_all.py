import importlib.util
import json
import subprocess
import sys
import os
from pathlib import Path

ROOT=Path(__file__).resolve().parent


def main():
    for name in ('pause_io.py','commit_negatives.py','restart_saved.py'):
        print('STAGE_START '+name,flush=True)
        process=subprocess.run([sys.executable,'-B',str(ROOT/name)],cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',errors='replace',env=dict(os.environ,PYTHONIOENCODING='utf-8'))
        (ROOT/(name+'.runner.log')).write_text(process.stdout,encoding='utf-8')
        print(process.stdout,flush=True)
        assert process.returncode==0,'Harness error '+name
    print('COMMAND_GATE_SEQUENCE_COMPLETE',flush=True)


if __name__=='__main__':main()
