import ast
import importlib.util
import json
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
runner=(WORK/'tools/run_native_expectation_cursor_dd1ad841.py').read_text(encoding='utf-8')
runner=module.replace(runner,'reports/native-repair-ranged-use-candidate-dd1ad841.json','reports/native-repair-session-timer-expectation-candidate-73d460d2.json')
runner=module.replace(runner,'787596b981d42b24a80047bdde8ae76e3bfc6185b9f0ca6a45b3380ad7e1c6fa','e290bee4f709f7912cfadfb6f103300100f6a264cdeb13422f8df843fab3e9ac')
runner=module.replace(runner,'tests/native-publishing-binding-diagnostic-dd1ad841/PositiveWeapon436.java','tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java')
runner=runner.replace('expectation-cursor-diagnostic-20261112/ExpectationCursorDiagnostic.java','native-map-rejection-diagnostic-20261112/NativeMapRejectionDiagnostic.java').replace('ExpectationCursorDiagnostic','NativeMapRejectionDiagnostic').replace('expectation-cursor.log','native-map-rejection.log').replace('native-repair-runtime-expectation-cursor-dd1ad841.json','native-repair-runtime-map-rejection-73d460d2.json')
target=WORK/'tools/run_native_map_rejection_73d460d2.py'
ast.parse(runner)
module.create(target,runner)
output=WORK/'build/native-map-rejection-debugger-compile-20261112'
output.mkdir()
classes=output/'classes'
classes.mkdir()
source=WORK/'tests/native-map-rejection-diagnostic-20261112/NativeMapRejectionDiagnostic.java'
with (output/'compile.log').open('xb') as stream:
    result=subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe','--add-modules','jdk.jdi','-encoding','UTF-8','-d',str(classes),str(source)],stdout=stream,stderr=subprocess.STDOUT)
(output/'compile-terminal.json').write_text(json.dumps({'exitCode':result.returncode,'targetJvmStarted':False,'readOnlyDebugger':True,'targetMethodInvocations':False,'targetWrites':False},indent=2),encoding='utf-8')
assert result.returncode==0,str(output/'compile.log')
print(json.dumps({'preparedRunner':str(target),'debuggerCompiled':True,'originalWholeNineteenObserverUnchanged':True,'productSourcesOrClassesChanged':False,'formalInstancesReadOnly':True}),flush=True)
