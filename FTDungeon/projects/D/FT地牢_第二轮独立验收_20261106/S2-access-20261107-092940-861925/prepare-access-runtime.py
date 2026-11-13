from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / (sys.argv[1] if len(sys.argv)>1 else 'runtime-access-r1')
assert not (OUTPUT / 'server').exists()
project = OUTPUT / 'native-test'
project.mkdir(parents=True, exist_ok=True)
original = ROOT / 'runtime-assembly-r1/native-test'
for name in ['build.gradle','settings.gradle','gradle.properties']:
    shutil.copyfile(original / name, project / name)
shutil.copytree(original / 'src/main/resources', project / 'src/main/resources', dirs_exist_ok=True)
shutil.copyfile(ROOT / 'runtime-assembly-r1/run.ps1', OUTPUT / 'run.ps1')
server = OUTPUT / 'server'
server.mkdir()
shutil.copyfile(ROOT / 'runtime-assembly-r1/server/server.properties', server / 'server.properties')
datapacks = server / 's2-new-isolated-qa-world/datapacks'
datapacks.mkdir(parents=True)
shutil.copyfile(ROOT / 'access-install-33ceb33f/s2_desert_access.zip', datapacks / 's2_desert_access.zip')
runner = (OUTPUT / 'run.ps1').read_text(encoding='utf-8')
runner = runner.replace("if(Test-Path -LiteralPath \"$PSScriptRoot\\server\\s2-new-isolated-qa-world\"){throw 'Use a fresh attempt directory; existing QA world must be preserved'}", "if(Test-Path -LiteralPath \"$PSScriptRoot\\server\\s2-new-isolated-qa-world\\level.dat\"){throw 'Existing QA world must be preserved'}")
(OUTPUT / 'run.ps1').write_text(runner, encoding='utf-8')
