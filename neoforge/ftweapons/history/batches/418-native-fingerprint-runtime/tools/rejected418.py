import sys
from pathlib import Path

ROOT = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
runtime_path = BATCH / 'reports/actual-coverage418-read.json'
runtime = load(runtime_path)
attempt = Path(runtime['attempt'])
terminal_path = attempt / 'terminal308.json'
terminal = load(terminal_path)
assert terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave']
for name, expected in runtime['inputHashes'].items():
    assert digest(name) == expected, name
case = runtime['probe']['cases'][0]
coverage = case['coverageNextRead418']['orderedReadChecks']
assert len(coverage) == 33 and coverage[-1]['index'] == 32 and 'refusal' in coverage[-1]
assert case['publications'] == [] and case['damageEvents'] == [] and case['targetHealthBefore'] == case['targetHealthAfter']
save(BATCH / 'reports/actual-runtime418-rejected-gate.json', {
    'runtimeExitCode': 0, 'probeFailures': runtime['probe']['failures'], 'gateAccepted': False,
    'orderedCoverageObserved': len(coverage), 'orderedCoverageRequired': 74, 'firstRefusal': coverage[-1],
    'publicationPresent': False, 'nativeImpactObserved': False, 'observedArrows': case['arrows'],
    'diagnosis': 'Native arrow was not removed after twelve ticks; no target damage or native impact events. These observations alone do not establish a production regression.',
    'nextAction': 'Record projectile geometry and require actual native collision in a closer positive fixture without changing production candidate or authority fields.',
    'positiveSignedSessionAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [runtime_path, terminal_path, Path(__file__)]}})
print({'gateAccepted': False, 'coverage': len(coverage), 'publicationPresent': False, 'nativeImpactObserved': False})
