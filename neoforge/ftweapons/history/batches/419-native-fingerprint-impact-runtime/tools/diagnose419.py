import sys
from pathlib import Path

ROOT = Path('D:\\ft工程文件\\FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sealed_path = BATCH / 'reports/actual-nonempty419-sealed-experimental.json'
sealed = load(sealed_path)
for name, wanted in sealed['inputHashes'].items():
    assert digest(name) == wanted, name
session = sealed['postPublicationSession']
failure = session.get('firstObservationFailure')
trace = failure.get('trace', '') if isinstance(failure, dict) else ''
lines = trace.splitlines()
project_frames = [line.strip() for line in lines if 'net.farmingtales.ftboss.item.' in line and 'NativeMutationMap276' not in line]
save(BATCH / 'reports/actual-first-rejection419-diagnostic.json', {
    'diagnosticFailures': 0, 'runtimeClassesVerified': sealed['runtimeClassesVerified'],
    'nonemptyTimerSessionRetainedAndGroupsDone': sealed['nonemptyTimerSessionRetainedAndGroupsDone'],
    'traceRevoked': session['traceRevoked'], 'coverageInstalled': session['coverageInstalled'],
    'firstFailure': failure, 'firstProjectFrames': project_frames,
    'actualTimerGroupStatus': [{'selectedInstructions': group['selectedInstructions'], 'done': group['done']} for group in sealed['actualTimerGroups']],
    'fullSignedSessionAccepted': False, 'fullLeaseRepairPaymentAccepted': False,
    'authenticatedMultiplayerAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [sealed_path, Path(__file__)]}})
print({'nonemptyPositive': sealed['nonemptyTimerSessionRetainedAndGroupsDone'],
    'firstFailure': lines[0] if lines else failure, 'firstProjectFrames': project_frames[:12]}, flush=True)
