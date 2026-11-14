import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
attempt = work / 'build/repair-runtime-7503378f'
probe_path = attempt / 'server/actual-storage-view308.json'
probe = json.loads(probe_path.read_text(encoding='utf-8'))
assert probe['failures'] == 1 and 'definition capture' in probe['failure']
command = json.loads((attempt / 'runtime-command308.json').read_text(encoding='utf-8'))
agent_option = next(value for value in command if value.startswith('-javaagent:'))
expected = Path(agent_option.split('=', 1)[1])
assert not list(expected.iterdir())
unexpected = []
for root in Path('C:/').iterdir():
    if root.name.startswith('ft') and root.name != 'ft工程文件C' and root.is_dir():
        for project in root.iterdir():
            failed_attempt = project / 'work/session-lease/build/repair-runtime-7503378f'
            if failed_attempt.is_dir():
                unexpected.extend(path for path in failed_attempt.iterdir()
                                  if path.is_dir() and path.name.startswith('jvm-definition-capt'))
assert len(unexpected) == 1
files = {str(path): hashlib.sha256(path.read_bytes()).hexdigest()
         for path in unexpected[0].rglob('*') if path.is_file()}
assert files
log = (attempt / 'runtime308.log').read_text(encoding='utf-8', errors='replace')
receipt = {
    'attempt': str(attempt), 'runtimePid': 78156,
    'probeFailure': probe['failure'], 'accepted': False,
    'allDimensionsSavedInLog': 'All dimensions are saved' in log,
    'exitCode': None, 'exitCodeUnavailableAfterInterruptedLauncher': True,
    'unexpectedWritesOutsideAuthorizedWorkspace': True,
    'unexpectedCaptureRoot': str(unexpected[0]), 'unexpectedFileHashes': files,
    'unexpectedFilesDeletedOrMoved': False, 'formalInstanceModified': False,
    'cause': 'Absolute non-ASCII javaagent premain argument resolved to a mojibake sibling path; ordinary JVM property retained correct path.',
    'correction': 'Separate immutable launcher passes ASCII ../jvm-definition-captures relative to the isolated server working directory.',
    'noRerunOfFrozenSignedSessionSuite': True, 'installable': False,
    'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest()
                    for path in [probe_path, attempt / 'runtime-command308.json', attempt / 'runtime308.log', Path(__file__)]}
}
output = work / 'reports/definition-capture-path-boundary-incident.json'
assert not output.exists()
output.write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'unexpectedFileCount': len(files), 'failedRunExcluded': True, 'receipt': str(output)}, ensure_ascii=True))
