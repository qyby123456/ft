import hashlib
import json
import os
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}
def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding='utf-8'))

def frozen(data):
    for name, wanted in data.get('inputHashes', {}).items():
        assert digest(name) == wanted, Path(name).name

prior_path = work / 'reports/native-material-ranged-final-process-check-41d445cf.json'
prior = load(prior_path)
frozen(prior)
assert prior['failures'] == 0 and prior['actualRuntimeCount'] == 15
suffixes = [
    ('variants-paid-repair-41d445cf', 1), ('variants-paid-repair-qualified-41d445cf', 1),
    ('variants-paid-repair-profile-qualified-41d445cf', 1), ('variants-paid-repair-pair-qualified-41d445cf', 0),
    ('variants-signed-qualified-41d445cf', 1), ('wooden-sword-diagnostic-41d445cf', 1),
    ('wooden-sword-diagnostic-2699881a', 1), ('wooden-sword-diagnostic-7069385d', 1),
    ('variants-paid-repair-native-materials-qualified-41d445cf', 1), ('wooden-original-installed-paid-41d445cf', 0),
    ('wooden-configured-signed-41d445cf', 1), ('wooden-configured-signed-plan-qualified-41d445cf', 1),
    ('wooden-configured-signed-book-qualified-41d445cf', 0), ('wooden-configured-restart-41d445cf', 0),
    ('wooden-configured-controls-41d445cf', 0)
]
rows = []
for suffix, wanted in suffixes:
    path = work / ('reports/native-repair-runtime-exact-melee-' + suffix + '.json')
    data = load(path)
    attempt = Path(data['attempt'])
    end = load(attempt / 'terminal308.json')
    process = load(attempt / 'live-process308.json')
    assert data['exitCode'] == end['exitCode'] == 0
    assert data['probe']['failures'] == end['probe']['failures'] == wanted
    assert end['stopSent'] and end['completeSave']
    log = attempt / 'runtime308.log'
    text = log.read_text(encoding='utf-8', errors='replace')
    assert 'Stopping server' in text and 'All dimensions are saved' in text
    digest(log)
    rows.append({'receipt': str(path), 'attempt': str(attempt), 'pid': process['pid'], 'observerFailures': wanted,
                 'normalStop': True, 'allDimensionsSaved': True, 'javaExitCode': 0,
                 'diagnosticOrExcludedFixtureNotAcceptedCombat': wanted == 1})
preflight = work / 'build/repair-runtime-7ee071c3'
assert preflight.is_dir() and not (preflight / 'live-process308.json').exists() and not (preflight / 'compile-command308.json').exists()
compile_failed = work / 'build/exact-melee-wooden-original-rule-planned-paid-precompile-qualified-41d445cf/terminal.json'
compile_passed = work / 'build/exact-melee-wooden-original-rule-planned-paid-import-qualified-precompile-41d445cf/terminal.json'
assert load(compile_failed)['exitCode'] == 1 and not load(compile_failed)['serverJvmStarted']
assert load(compile_passed)['exitCode'] == 0 and not load(compile_passed)['serverJvmStarted']
assert (work / 'build/exact-melee-wooden-original-rule-planned-paid-precompile-41d445cf').is_dir()
for relative in [
    'reports/native-exact-melee-nineteen-paid-repair-focused-evidence-41d445cf.json',
    'reports/native-exact-melee-wooden-configured-paid-signed-restart-focused-evidence-41d445cf.json',
    'reports/native-remaining-original-superclass-closure-static-audit-41d445cf.json'
]:
    data = load(work / relative)
    assert data['failures'] == 0
    frozen(data)
current = str(work).replace("'", "''")
forward = work.as_posix().replace("'", "''")
command = "$rows=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') -and ($_.CommandLine -like '*" + current + "*' -or $_.CommandLine -like '*" + forward + "*') }); ConvertTo-Json -InputObject @($rows | Select-Object ProcessId,Name) -Compress"
result = subprocess.run(['powershell', '-NoProfile', '-Command', command], cwd=root, capture_output=True, text=True)
assert result.returncode == 0, result.stderr
processes = json.loads(result.stdout or '[]')
others = [row for row in processes if int(row['ProcessId']) != os.getpid()]
assert not others, others
assert digest(root / 'work/session-chain/reports/actual-current.json') == '1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
for path in [Path(__file__), root / 'WRITE_OWNER.claim']:
    digest(path)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
digest(helper)
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
output = work / 'reports/native-exact-melee-and-material-final-process-check-41d445cf.json'
namespace['add'](output.relative_to(root).as_posix(), json.dumps({
    'failures': 0, 'reusedPriorMaterialProcessAudit': str(prior_path),
    'actualRuntimeCount': prior['actualRuntimeCount'] + len(rows),
    'observerPassCount': prior['observerPassCount'] + sum(row['observerFailures'] == 0 for row in rows),
    'preservedFailureCount': prior['preservedFailureCount'] + sum(row['observerFailures'] == 1 for row in rows),
    'newExactMeleeRuntimes': rows, 'allActualJavaRuntimesNormalSavedExitZero': True,
    'excludedPreflightWithoutCompileOrJvmPreserved': str(preflight),
    'unusedRulePlannedCompileFailureAndSuccessPreserved': [str(compile_failed), str(compile_passed)],
    'ownedJavaOrOtherPythonProcessesRemaining': False, 'adoptedReceiptUnchanged': True,
    'oneConfiguredWoodenSwordFourPhaseScopeAccepted': True,
    'configuredNineteenMatrixAccepted': False, 'full103ItemMatrixAccepted': False,
    'authenticatedMultiplayerAccepted': False, 'goalStillActive': True, 'installable': False, 'deployed': False,
    'inputHashes': hashes
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'report': output.name, 'actualRuntimes': prior['actualRuntimeCount'] + len(rows),
                  'newRuntimes': len(rows), 'allNormalSavedJavaZero': True, 'ownedProcessesRemaining': False, 'hashes': len(hashes)}))
