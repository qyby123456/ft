import base64
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/loaded-class-capture-utf8-v1-20261114'
assert not OUT.exists()
OUT.mkdir()
classes = OUT / 'classes'
classes.mkdir()
source = WORK / 'tests/LoadedBossClassCaptureV3.java'
result = subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '--release', '21', '-encoding', 'UTF-8', '-d', str(classes), str(source)], capture_output=True, timeout=60)
(OUT / 'compile.log').write_bytes(result.stdout + result.stderr)
assert result.returncode == 0
agent = OUT / 'capture-agent.jar'
with zipfile.ZipFile(agent, 'x', compression=zipfile.ZIP_DEFLATED) as bundle:
    bundle.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nPremain-Class: ftboss.nativeprobe.LoadedBossClassCaptureV3\nCan-Redefine-Classes: false\nCan-Retransform-Classes: false\n\n')
    for path in classes.rglob('*.class'):
        bundle.write(path, path.relative_to(classes).as_posix())
build = WORK / 'build/boss-manor-damage-context-v1-20261114'
previous = json.loads((build / 'compile-proof-command.json').read_text(encoding='utf-8'))
classpath = str(build / 'proof-classes') + ';' + previous[previous.index('-cp') + 1]
output = OUT / '中文路径采集'
encoded = base64.urlsafe_b64encode(str(output).encode('utf-8')).decode('ascii')
command = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-javaagent:' + str(agent) + '=' + encoded, '-Xmx256m', '-cp', classpath, 'ManorDamageContextWrapperProofV1']
result = subprocess.run(command, capture_output=True, timeout=60)
(OUT / 'agent-control.log').write_bytes(result.stdout + result.stderr)
assert result.returncode == 0 and b'failures=0;' in result.stdout
captured = output / 'net_farmingtales_ftboss_entity_EasterBossEntity.class'
assert captured.is_file() and (output / 'agent-started.txt').is_file()
receipt = {'utf8ChineseOutputPathRoundtripVerified': True, 'optionEncoding': 'base64url of UTF-8 full directory path', 'encodedOptionAsciiOnly': encoded.isascii(), 'syntheticDefinitionCaptureVerified': True, 'actualGameRerunPerformed': False, 'capturedBytes': captured.stat().st_size, 'capturedSha256': hashlib.sha256(captured.read_bytes()).hexdigest(), 'agentAndWrapperProofExitCode': result.returncode, 'transformerReturnsNull': True, 'noRetransformationOrRedefinition': True}
(OUT / 'verification.json').write_text(json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
print(json.dumps(receipt), flush=True)
