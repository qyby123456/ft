import os
from pathlib import Path
import shutil
import sys

sys.dont_write_bytecode = True
HOME = Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
sys.path.insert(0,str(HOME.parent/'v4-unified'))
import integrate
import run_native

if __name__ == '__main__':
    for name in ('progress.json','installed-progress.json','installed-q3-retry-progress.json'):
        target=HOME/name
        if not target.exists():
            shutil.copyfile(HOME.parent/'v4-unified'/name,target)
    shutil.copyfile(HOME.parent/'v4-unified/overlay.gradle',HOME/'overlay.gradle')
    helper=HOME/'world-tests/cn/ft/biomedungeon/runtime/IndependentNativeTests.java'
    helper.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(HOME.parent/'v4-unified/world-tests/cn/ft/biomedungeon/runtime/IndependentNativeTests.java',helper)
    os.environ['FT_NATIVE_NAMESPACE']='ftbd_usepath'
    os.environ['FT_INSTALLED_CASE3_PROGRESS']='installed-q3-retry-progress.json'
    run_native.HOME=HOME
    run_native.PROGRESS=HOME/os.environ.get('FT_USE_PROGRESS','interaction-progress.json')
    run_native.main()
