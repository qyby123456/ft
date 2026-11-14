import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'reports/ftweapons-git-source-cleanup-v1-20261114'
REPO = ROOT / 'github-sync/repositories/ft'
RECOVERY = ROOT / 'github-sync/repositories/ft-checkpoint-recovery-20261113'
PREFIX = 'neoforge/ftweapons/'
BRANCH = 'codex/neoforge-source-20261113'
PREVIOUS = 'fb3e345f81000d284375db418c6befbdb2c4b745'
ARCHIVE = WORK / 'archives/weapons-paused-cf217105-boss-priority-v2-20261113'
PYTHON = sys.executable

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(data):
    return hashlib.sha256(data).hexdigest()

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def git(repository, *arguments, timeout=50, network=False, public=False):
    command = ['git', '-C', str(repository)]
    if network:
        proxy = os.environ['FT_TASK_PROXY']
        command += ['-c', 'http.proxy=' + proxy, '-c', 'http.sslBackend=openssl', '-c', 'http.sslVerify=true', '-c', 'http.version=HTTP/1.1']
        if public:
            command += ['-c', 'credential.helper=']
    environment = dict(os.environ, GIT_TERMINAL_PROMPT='0', GCM_INTERACTIVE='Never')
    result = subprocess.run(command + list(arguments), capture_output=True, env=environment, timeout=timeout)
    assert result.returncode == 0, 'Git command failed: ' + arguments[0] + ' (inspect saved state before retry)'
    return result.stdout

def owner():
    assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == load(ROOT / 'WRITE_OWNER.json')['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'

def put(relative, data, rows, original=None):
    name = PREFIX + relative
    assert name not in rows
    target = REPO / name
    assert target.resolve().is_relative_to((REPO / PREFIX).resolve()) and not target.exists()
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
    rows[name] = {'bytes': len(data), 'sha256': digest(data), 'original': str(original) if original else None}

def prepare():
    owner()
    assert not OUT.exists()
    assert git(REPO, 'rev-parse', 'HEAD').decode().strip() == PREVIOUS
    assert not git(REPO, 'status', '--porcelain').strip()
    assert not (REPO / PREFIX).exists()
    OUT.mkdir(parents=True)
    receipt = load(ARCHIVE / 'archive-receipt.json')
    assert sha(receipt['archive']) == receipt['archiveSha256']
    assert sha(receipt['snapshotJar']) == receipt['snapshotJarSha256']
    rows = {}
    roots = ['work/session-lease', 'work/session-chain', 'work/session-continuity', 'work/session-projection', 'batches']
    excluded = {'build', 'archives', 'reports', 'runtime', 'runtimes', 'cache', '.git', 'server', 'game', '__pycache__'}
    extensions = {'.py', '.java', '.md', '.json', '.in', '.mf'}
    for relative in roots:
        for base, directories, names in os.walk(ROOT / relative):
            directories[:] = [name for name in directories if name not in excluded]
            assert 'AGENTS.md' not in names, 'Review nested instructions first'
            for name in sorted(names):
                path = Path(base) / name
                if path.suffix.lower() not in extensions or re.search(r'(?i)credentials?', path.name):
                    continue
                assert path.is_file() and not path.is_symlink()
                put('history/' + path.relative_to(ROOT).as_posix(), path.read_bytes(), rows, path)
    arguments_path = ROOT / 'work/session-lease/build/native-repair-cf217105/complete-arguments316.json'
    arguments = load(arguments_path)
    sources = [Path(value) for value in arguments if value.endswith('.java')]
    assert len(sources) == 718
    effective = []
    for path in sources:
        data = path.read_bytes()
        text = data.decode('utf-8-sig')
        package = re.search(r'(?m)^\s*package\s+([\w.]+)\s*;', text)
        assert package, path.name
        relative = 'current-effective/src/main/java/' + package.group(1).replace('.', '/') + '/' + path.name
        put(relative, data, rows, path)
        effective.append({'source': relative, 'bytes': len(data), 'sha256': digest(data), 'originWorkspace': 'C' if path.resolve().is_relative_to(ROOT) else 'historical-read-only-D'})
    with zipfile.ZipFile(receipt['snapshotJar']) as bundle:
        for name in bundle.namelist():
            if name.startswith('frozen-resources/'):
                put('recovery-resources/' + name.removeprefix('frozen-resources/'), bundle.read(name), rows)
    candidate = load(ROOT / 'work/session-lease/reports/native-repair-material-proc-integration-candidate-cf217105.json')
    for group in ['requiredProductionResources', 'additionalProductionResources']:
        for name, original in candidate[group].items():
            put('current-effective/src/main/resources/' + name, Path(original).read_bytes(), rows, Path(original))
    classpath = arguments[arguments.index('-classpath') + 1].split(';')
    dependencies = [{'name': Path(value).name, 'bytes': Path(value).stat().st_size, 'sha256': sha(value), 'uploaded': False} for value in classpath]
    manifest = {'format': 'ftweapons-paused-source-snapshot-v1', 'effectiveSourceCount': len(effective), 'sources': effective, 'compileOptions': arguments[:arguments.index('-classpath')], 'dependencies': dependencies, 'sourceArgumentFileSha256': sha(arguments_path), 'weaponDevelopmentPaused': True, 'installable': False, 'fullSignedSessionAccepted': False, 'recoverySnapshotJarSha256': receipt['snapshotJarSha256'], 'recoveryArchiveSha256': receipt['archiveSha256'], 'binaryArchivesUploaded': False, 'sharedCompileDependenciesIncluded': True}
    put('SOURCE_SNAPSHOT.json', (json.dumps(manifest, ensure_ascii=False, indent=2) + '\n').encode('utf-8'), rows)
    note = '# FT weapons paused source snapshot\n\nThis directory freezes the current weapon work without resuming development. current-effective contains all 718 Java inputs of the cf217105 compile, including shared compile dependencies; history preserves the C-workspace weapon proposals, tests, tools and notes. recovery-resources is the frozen separated recovery-container resource layer; current-effective resources are the later candidate overrides.\n\nThis is not a standalone registered NeoForge addon. The native whole19 acceptance failed, full signed Session acceptance is false, and no new runtime or deployment is claimed. The recovery JAR remains noninstallable.\n\nSOURCE_SNAPSHOT.json binds source bytes and external dependency hashes. Dependencies, worlds, binary archives and the recovery JAR are retained locally and are not recoverable from this source-only Git snapshot. Historical D inputs were read only. Original Boss Git paths and formal instances are unchanged.\n\nRestore this directory with git archive from the verified branch commit into a separate empty workspace. Read LOCAL_CLEANUP.json when present for removed original paths and their byte-identical Git replacements. Keep shared Boss helper inputs locally. Development remains paused until an explicit resume instruction.\n'
    put('README.md', note.encode('utf-8'), rows)
    put('.gitattributes', b'* -text\n', rows)
    public = {name: {key: value for key, value in row.items() if key != 'original'} for name, row in rows.items()}
    put('FILES.json', (json.dumps(public, ensure_ascii=False, indent=2) + '\n').encode('utf-8'), rows)
    save(OUT / 'source-map.json', {'previousCommit': PREVIOUS, 'branch': BRANCH, 'files': rows, 'archiveSha256': receipt['archiveSha256'], 'snapshotJarSha256': receipt['snapshotJarSha256']})
    git(REPO, 'add', '--sparse', '-f', '--', PREFIX)
    exceptions = load(ROOT / 'github-sync/reviewed-scan-exceptions.json')
    mapped = []
    for item in exceptions['exceptions']:
        mapped.append({**item, 'path': 'history/' + item['path']})
    save(OUT / 'reviewed-exceptions.json', {'exceptions': mapped, 'scope': 'Existing exact-byte, exact-line metadata reviews mapped only to unchanged historical sources'})
    print(json.dumps({'preparedFiles': len(rows), 'bytes': sum(row['bytes'] for row in rows.values()), 'effectiveSources': len(effective), 'sourceMap': str(OUT / 'source-map.json')}), flush=True)

def verify(head, label):
    git(RECOVERY, 'fetch', '--no-tags', 'origin', 'refs/heads/' + BRANCH, network=True, public=True)
    assert git(RECOVERY, 'rev-parse', 'FETCH_HEAD').decode().strip() == head
    assert git(RECOVERY, 'ls-remote', 'origin', 'refs/heads/' + BRANCH, network=True, public=True).split()[0].decode() == head
    assert not (RECOVERY / '.git/objects/info/alternates').exists()
    assert git(RECOVERY, 'remote', 'get-url', 'origin').decode().strip() == 'https://github.com/qyby123456/ft.git'
    git(RECOVERY, 'merge-base', '--is-ancestor', PREVIOUS, head)
    git(RECOVERY, 'fsck', '--full')
    assert git(RECOVERY, 'rev-parse', head + ':neoforge/ftboss').strip() == git(RECOVERY, 'rev-parse', PREVIOUS + ':neoforge/ftboss').strip()
    assert git(RECOVERY, 'rev-parse', head + ':README.md').strip() == git(RECOVERY, 'rev-parse', PREVIOUS + ':README.md').strip()
    source_map = load(OUT / 'source-map.json')['files']
    entries = git(RECOVERY, 'ls-tree', '-r', '-z', head, '--', PREFIX).split(b'\0')
    files = {}
    process = subprocess.Popen(['git', '-C', str(RECOVERY), 'cat-file', '--batch'], stdin=subprocess.PIPE, stdout=subprocess.PIPE)
    for entry in entries:
        if not entry:
            continue
        header, raw_name = entry.split(b'\t', 1)
        mode, kind, object_id = header.split()
        name = raw_name.decode('utf-8')
        assert mode == b'100644' and kind == b'blob' and name in source_map
        process.stdin.write(object_id + b'\n')
        process.stdin.flush()
        response = process.stdout.readline().split()
        size = int(response[2])
        data = process.stdout.read(size)
        assert response[:2] == [object_id, b'blob'] and process.stdout.read(1) == b'\n'
        assert len(data) == source_map[name]['bytes'] and digest(data) == source_map[name]['sha256'], name
        files[name] = {'bytes': size, 'sha256': digest(data)}
    process.stdin.close()
    assert process.wait(timeout=30) == 0 and set(files) == set(source_map)
    proof = {'commit': head, 'branch': BRANCH, 'prefix': PREFIX, 'independentRemoteFetch': True, 'allWeaponBlobsPathSizeSha256Verified': len(files), 'bossSubtreeUnchanged': True, 'rootReadmeUnchanged': True, 'gitFsckFullPassed': True, 'largeArtifactsRemoteRecoveryVerified': False, 'files': files}
    save(OUT / ('remote-proof-' + label + '.json'), proof)
    return proof

def publish():
    owner()
    scan = load(OUT / 'scan-v2/verification.json')
    assert scan['filesWithIssues'] == 0 and scan['scanCoversEveryEnumeratedGitCandidate']
    assert git(REPO, 'write-tree').decode().strip() == scan['indexTree']
    assert git(REPO, 'rev-parse', 'HEAD').decode().strip() == PREVIOUS
    changed = [name for name in git(REPO, 'diff', '--cached', '--name-only', '-z').decode().split('\0') if name]
    assert set(changed) == set(load(OUT / 'source-map.json')['files'])
    assert git(REPO, 'branch', '--show-current').decode().strip() == BRANCH
    git(REPO, 'commit', '-m', 'Archive paused FT weapons source snapshot before local cleanup')
    head = git(REPO, 'rev-parse', 'HEAD').decode().strip()
    save(OUT / 'local-commit.json', {'commit': head, 'pushed': False})
    git(REPO, 'push', 'origin', 'HEAD:refs/heads/' + BRANCH, network=True)
    proof = verify(head, 'before-cleanup')
    assert not git(REPO, 'status', '--porcelain').strip()
    print(json.dumps({key: value for key, value in proof.items() if key != 'files'}), flush=True)

parser = argparse.ArgumentParser()
parser.add_argument('operation', choices=['prepare', 'publish', 'verify'])
args = parser.parse_args()
if args.operation == 'prepare':
    prepare()
elif args.operation == 'publish':
    publish()
else:
    print(json.dumps({key: value for key, value in verify(git(REPO, 'rev-parse', 'HEAD').decode().strip(), 'final').items() if key != 'files'}), flush=True)
