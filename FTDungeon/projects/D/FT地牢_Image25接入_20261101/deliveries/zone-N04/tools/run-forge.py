import hashlib
import json
import os
import subprocess
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA_HOME = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot'
LAUNCHER = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/lib/gradle-launcher-8.8.jar'))
stamp = datetime.now().strftime('%Y%m%d-%H%M%S')
run_directory = ROOT / 'forge/run' / stamp
assert not run_directory.exists()
run_directory.mkdir(parents=True)
(run_directory / "server.properties").write_text(
    'level-type=minecraft:flat\ngenerate-structures=false\nview-distance=2\nsimulation-distance=2\n'
    + 'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains","features":false,"lakes":false}\n',
    encoding="utf-8")
environment = os.environ.copy()
environment['JAVA_HOME'] = str(JAVA_HOME)
environment['PATH'] = str(JAVA_HOME / 'bin') + os.pathsep + environment.get('PATH', '')
command = [str(JAVA_HOME / 'bin/java.exe'), '-XX:ActiveProcessorCount=2', '-classpath', str(LAUNCHER),
           'org.gradle.launcher.GradleMain', 'runGameTestServer', '--offline', '--no-daemon', '--max-workers=1',
           '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', '-PqaRunDirectory=run/' + stamp]
sha256 = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
invocation = {'id': 'zone-N04', 'sha256': sha256, 'command': command, 'cwd': str(ROOT / 'forge'),
              'fresh_world': str(run_directory), 'player_world_used': False, 'deployed': False}
(ROOT / 'qa/forge-invocation.json').write_text(json.dumps(invocation, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('FORGE_START', run_directory, flush=True)
with (ROOT / 'qa/forge-run.log').open('wb') as log:
    execution = subprocess.run(command, cwd=ROOT / 'forge', env=environment, stdout=log,
                               stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW, timeout=900)
text = (ROOT / 'qa/forge-run.log').read_text(encoding='utf-8', errors='replace')
native = json.loads((ROOT / 'qa/forge-native.json').read_text(encoding='utf-8')) if (ROOT / 'qa/forge-native.json').exists() else {}
passed = execution.returncode == 0 and 'BUILD SUCCESSFUL' in text and native.get('status') == 'PASS' and native.get('sha256') == sha256
result = {'id': 'zone-N04', 'status': 'PASS' if passed else 'FAIL', 'sha256': sha256,
          'exit_code': execution.returncode, 'fresh_world': str(run_directory), 'native_report': str(ROOT / 'qa/forge-native.json')}
(ROOT / 'qa/forge-result.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(result, ensure_ascii=False, indent=2))
if not passed:
    print(text[-12000:])
    raise SystemExit(1)
