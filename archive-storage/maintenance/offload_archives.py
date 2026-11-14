import argparse
import datetime
import hashlib
import json
import os
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'reports/github-large-artifact-offload-v1-20261114'
REPO = ROOT / 'github-sync/repositories/ft'
RECOVERY = ROOT / 'github-sync/repositories/ft-checkpoint-recovery-20261113'
BRANCH = 'codex/neoforge-source-20261113'
PREFIX = 'archive-storage/'
TAG = 'ft-cache-archive-20261114'
API = 'https://api.github.com/repos/qyby123456/ft'
KEEP = {'work/session-boss/archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip'}

class SafeRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, connection, code, message, headers, new_url):
        redirected = super().redirect_request(request, connection, code, message, headers, new_url)
        if redirected is not None and urllib.parse.urlparse(request.full_url).hostname != urllib.parse.urlparse(new_url).hostname:
            redirected.remove_header('Authorization')
        return redirected

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def save(path, value):
    temporary = Path(path).with_suffix(Path(path).suffix + '.writing')
    temporary.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    temporary.replace(path)

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def stamp():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()

def owner():
    assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == load(ROOT / 'WRITE_OWNER.json')['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'

def git(repository, *arguments, network=False, public=False):
    command = ['git', '-C', str(repository)]
    if network:
        command += ['-c', 'http.proxy=' + os.environ['FT_TASK_PROXY'], '-c', 'http.sslBackend=openssl', '-c', 'http.sslVerify=true', '-c', 'http.version=HTTP/1.1']
        if public:
            command += ['-c', 'credential.helper=']
    result = subprocess.run(command + list(arguments), capture_output=True, env=dict(os.environ, GIT_TERMINAL_PROMPT='0', GCM_INTERACTIVE='Never'), timeout=50)
    assert result.returncode == 0, 'Git operation failed: ' + arguments[0]
    return result.stdout

def connect():
    result = subprocess.run(['git', '-C', str(REPO), 'credential', 'fill'], input='protocol=https\nhost=github.com\npath=qyby123456/ft.git\n\n', text=True, capture_output=True, env=dict(os.environ, GIT_TERMINAL_PROMPT='0', GCM_INTERACTIVE='Never'), timeout=30)
    assert result.returncode == 0, 'Existing Git credential helper failed'
    fields = dict(line.split('=', 1) for line in result.stdout.splitlines() if '=' in line)
    assert fields.get('password')
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({'https': os.environ['FT_TASK_PROXY'], 'http': os.environ['FT_TASK_PROXY']}), SafeRedirect())
    headers = {'Authorization': 'Bearer ' + fields['password'], 'Accept': 'application/vnd.github+json', 'X-GitHub-Api-Version': '2022-11-28', 'User-Agent': 'FT-archive-cache-maintenance'}
    return opener, headers

def request_json(opener, headers, url, method='GET', value=None):
    request_headers = dict(headers)
    data = None
    if value is not None:
        data = json.dumps(value).encode('utf-8')
        request_headers['Content-Type'] = 'application/json'
    request = urllib.request.Request(url, data=data, headers=request_headers, method=method)
    try:
        with opener.open(request, timeout=45) as response:
            return json.load(response)
    except urllib.error.HTTPError as failure:
        raise RuntimeError('GitHub API status ' + str(failure.code)) from None

class UploadProgress:
    def __init__(self, stream, name, total):
        self.stream, self.name, self.total = stream, name, total
        self.sent, self.updated = 0, 0.0
    def read(self, count=-1):
        data = self.stream.read(count)
        self.sent += len(data)
        if time.monotonic() - self.updated > 5:
            save(OUT / 'live-progress.json', {'phase': 'upload', 'path': self.name, 'bytes': self.sent, 'total': self.total, 'pid': os.getpid(), 'at': stamp()})
            self.updated = time.monotonic()
        return data

def assets(opener, headers, release):
    found = {}
    page = 1
    while True:
        rows = request_json(opener, headers, API + '/releases/' + str(release['id']) + '/assets?per_page=100&page=' + str(page))
        found.update({row['name']: row for row in rows})
        if len(rows) < 100:
            return found
        page += 1

def verify_asset(headers, asset, expected, path):
    opener, unused_headers = connect()
    request_headers = {**headers, 'Accept': 'application/octet-stream', 'Cache-Control': 'no-cache'}
    url = API + '/releases/assets/' + str(asset['id']) + '?verify=' + str(time.time_ns())
    request = urllib.request.Request(url, headers=request_headers)
    hasher = hashlib.sha256()
    count, updated = 0, 0.0
    with opener.open(request, timeout=45) as response:
        assert response.status == 200 and 'json' not in response.headers.get('Content-Type', '').lower(), 'Expected binary asset download'
        while True:
            data = response.read(4 * 1024 * 1024)
            if not data:
                break
            hasher.update(data)
            count += len(data)
            assert count <= expected['bytes']
            if time.monotonic() - updated > 5:
                save(OUT / 'live-progress.json', {'phase': 'independent-download-verification', 'path': path, 'bytes': count, 'total': expected['bytes'], 'pid': os.getpid(), 'at': stamp()})
                updated = time.monotonic()
    assert count == expected['bytes'] and hasher.hexdigest() == expected['sha256'], 'Independent artifact download byte mismatch'
    return {'downloadedBytes': count, 'downloadSha256': hasher.hexdigest(), 'independentRemoteDownloadVerified': True, 'verifiedAt': stamp()}

def publish_and_prune(index, baseline):
    owner()
    target = REPO / PREFIX
    target.mkdir(exist_ok=True)
    (target / '.gitattributes').write_bytes(b'* -text\n')
    coverage_target = target / 'coverage'
    coverage_target.mkdir(exist_ok=True)
    for metadata in (OUT / 'archive-coverage').iterdir():
        destination = coverage_target / metadata.name
        if not destination.exists():
            shutil.copyfile(metadata, destination)
    save(target / 'INDEX.json', index)
    note = '# FT archive storage\n\nSource and this index are stored in Git. Large immutable recovery archives are uploaded as content-addressed assets of the same repository draft release ft-cache-archive-20261114. Draft assets require repository write access and are intentionally not a public playable release. No deployment or installability is claimed. Each remoteVerified row records an independent streamed full download SHA256 and byte count.\n\nUse GitHub release asset API IDs with authenticated Accept: application/octet-stream requests. Do not print or save credentials or signed redirect URLs. Verify bytes and SHA256 before using a downloaded file. The original relative path is the key in INDEX.json. CAS increments still require their original hash-bound base archive. Native failed worlds remain failed evidence, never restart seeds.\n\nLocal archive copies are evictable caches after remote artifact verification plus independent Git index recovery verification. The active Stage28 base may remain cached while needed. Preserve the current Boss candidate, required compile/runtime inputs and formal instances.\n'
    (target / 'RESTORE.md').write_text(note, encoding='utf-8')
    source_target = target / 'maintenance/offload_archives.py'
    source_target.parent.mkdir(exist_ok=True)
    source_target.write_bytes(Path(__file__).read_bytes())
    git(REPO, 'add', '--sparse', '-f', '--', PREFIX)
    git(REPO, 'add', '--sparse', '--renormalize', '-f', '--', PREFIX)
    changed = [name for name in git(REPO, 'diff', '--cached', '--name-only', '-z').decode().split('\0') if name]
    assert changed and all(name.startswith(PREFIX) for name in changed)
    generation = index['generation']
    scan = OUT / ('source-scan-' + str(generation))
    command = [sys.executable, '-B', str(WORK / 'tools/verify_github_source_snapshot_v3.py'), '--git-dir', str(REPO / '.git'), '--work-tree', str(REPO), '--source-prefix', PREFIX.rstrip('/'), '--staged', '--output', str(scan)]
    scanned = subprocess.run(command, capture_output=True, timeout=60)
    (OUT / ('source-scan-' + str(generation) + '.log')).write_bytes(scanned.stdout + scanned.stderr)
    assert scanned.returncode == 0
    assert git(REPO, 'write-tree').decode().strip() == load(scan / 'verification.json')['indexTree']
    git(REPO, 'commit', '-m', 'Persist remotely verified recovery archives batch ' + str(generation))
    head = git(REPO, 'rev-parse', 'HEAD').decode().strip()
    save(OUT / ('git-local-commit-' + str(generation) + '.json'), {'commit': head})
    git(REPO, 'push', 'origin', 'HEAD:refs/heads/' + BRANCH, network=True)
    git(RECOVERY, 'fetch', '--no-tags', 'origin', 'refs/heads/' + BRANCH, network=True, public=True)
    assert git(RECOVERY, 'rev-parse', 'FETCH_HEAD').decode().strip() == head
    assert git(RECOVERY, 'ls-remote', 'origin', 'refs/heads/' + BRANCH, network=True, public=True).split()[0].decode() == head
    assert not (RECOVERY / '.git/objects/info/alternates').exists()
    assert git(RECOVERY, 'show', head + ':' + PREFIX + 'INDEX.json') == (target / 'INDEX.json').read_bytes()
    for subtree in ['neoforge/ftboss', 'neoforge/ftweapons']:
        assert git(RECOVERY, 'rev-parse', head + ':' + subtree) == git(RECOVERY, 'rev-parse', baseline + ':' + subtree)
    remote_proof = {'commit': head, 'generation': generation, 'independentRemoteGitFetchVerified': True, 'indexSha256': sha(target / 'INDEX.json'), 'bossAndWeaponSubtreesUnchanged': True, 'draftReleaseId': index['releaseId']}
    save(OUT / ('git-remote-proof-' + str(generation) + '.json'), remote_proof)
    for relative in ['work/session-boss/archives', 'work/session-lease/archives']:
        deletion = {name[len(relative) + 1:]: row for name, row in index['archives'].items() if name.startswith(relative + '/') and row['remoteVerified'] and name not in KEEP and (ROOT / name).exists()}
        if not deletion:
            continue
        for name, row in deletion.items():
            path = (ROOT / relative / name).resolve()
            assert path.is_relative_to((ROOT / relative).resolve()) and not path.is_symlink() and path.stat().st_size == row['bytes'] and sha(path) == row['sha256']
        plan = OUT / ('prune-' + str(generation) + '-' + Path(relative).name + '-' + Path(relative).parent.name + '.json')
        save(plan, {'targetRelativePath': relative, 'deleteFiles': deletion, 'verifiedGitCommit': head})
        checked = subprocess.run(['powershell', '-NoProfile', '-File', str(WORK / 'tools/check_github_source_cache_handles_v1.ps1'), '-PlanPath', str(plan)], capture_output=True, timeout=50)
        plan.with_suffix('.handles.log').write_bytes(checked.stdout + checked.stderr)
        assert checked.returncode == 0, 'Archive handle guard failed; local files retained'
        owner()
        for name, row in deletion.items():
            path = (ROOT / relative / name).resolve()
            assert sha(path) == row['sha256']
            path.unlink()
            index['archives'][relative + '/' + name]['localCacheRemoved'] = True
    current = git(RECOVERY, 'rev-parse', 'HEAD').decode().strip()
    git(RECOVERY, 'update-ref', 'refs/heads/' + BRANCH, head, current)
    save(OUT / 'index-state.json', index)
    print(json.dumps({'checkpoint': head, 'verifiedArchives': len(index['archives']), 'localArchivesRemoved': sum(row.get('localCacheRemoved', False) for row in index['archives'].values()), 'logicalBytesRemoved': sum(row['bytes'] for row in index['archives'].values() if row.get('localCacheRemoved')), 'freeBytes': shutil.disk_usage(ROOT).free}), flush=True)

def run(limit):
    owner()
    assert not git(REPO, 'status', '--porcelain').strip()
    baseline = 'aaff29b64aea74bf54f6c32ef35d96d752e8931a'
    opener, headers = connect()
    releases = request_json(opener, headers, API + '/releases?per_page=100')
    matching = [row for row in releases if row['tag_name'] == TAG]
    if matching:
        assert len(matching) == 1
        release = matching[0]
    else:
        release = request_json(opener, headers, API + '/releases', 'POST', {'tag_name': TAG, 'target_commitish': baseline, 'name': 'FT migration private recovery archive cache 2026-11-14', 'body': 'Recovery backups only. NOT INSTALLABLE. Draft assets require repository write access. Source/index remain on codex/neoforge-source-20261113. Never use failed native worlds as restart seeds.', 'draft': True, 'prerelease': True})
    assert release['draft']
    save(OUT / 'release.json', {'id': release['id'], 'tag': TAG, 'draft': True, 'repository': 'qyby123456/ft'})
    index = load(OUT / 'index-state.json') if (OUT / 'index-state.json').exists() else {'format': 'ft-remote-archive-cache-v1', 'repository': 'qyby123456/ft', 'releaseId': release['id'], 'releaseTag': TAG, 'draft': True, 'generation': 0, 'archives': {}}
    prior_generations = [int(path.stem.rsplit('-', 1)[1]) for path in OUT.glob('git-local-commit-*.json')]
    index['generation'] = max([index['generation'], *prior_generations])
    if any(row['remoteVerified'] and not row.get('localCacheRemoved') and name not in KEEP and (ROOT / name).exists() for name, row in index['archives'].items()):
        index['generation'] += 1
        publish_and_prune(index, baseline)
    available = assets(opener, headers, release)
    inventory = load(OUT / 'archive-inventory.json')['files']
    priority = 'work/session-boss/archives/weapons-paused-cf217105-boss-priority-v2-20261113/weapons-source-evidence-and-recovery.zip'
    ordered = sorted(inventory, key=lambda row: (0 if row['bytes'] < 1000000 else 1 if row['path'] == priority else 2, row['bytes']))
    pending, pending_bytes, processed = 0, 0, 0
    for item in ordered:
        relative = item['path']
        if relative in index['archives'] and index['archives'][relative]['remoteVerified']:
            continue
        path = (ROOT / relative).resolve()
        assert any(path.is_relative_to((ROOT / prefix).resolve()) for prefix in ['work/session-boss/archives', 'work/session-lease/archives'])
        assert path.is_file() and not path.is_symlink() and path.stat().st_size == item['bytes'] and item['bytes'] < 2000000000
        expected = {**item, 'sha256': sha(path)}
        with zipfile.ZipFile(path) as bundle:
            assert bundle.infolist()
        asset_name = 'sha256-' + expected['sha256'] + path.suffix.lower()
        available = assets(opener, headers, release)
        asset = available.get(asset_name)
        print(json.dumps({'starting': relative, 'bytes': item['bytes'], 'existingRemoteAsset': bool(asset)}), flush=True)
        if asset is None:
            url = release['upload_url'].split('{')[0] + '?name=' + urllib.parse.quote(asset_name)
            upload_headers = {**headers, 'Content-Type': 'application/octet-stream', 'Content-Length': str(item['bytes'])}
            with path.open('rb') as stream:
                request = urllib.request.Request(url, data=UploadProgress(stream, relative, item['bytes']), headers=upload_headers, method='POST')
                with opener.open(request, timeout=45) as response:
                    asset = json.load(response)
            available[asset_name] = asset
        assert asset['state'] == 'uploaded' and asset['size'] == item['bytes']
        if asset.get('digest'):
            assert asset['digest'] == 'sha256:' + expected['sha256']
        preverified_path = OUT / 'parallel-preverified-assets' / (expected['sha256'] + '.json')
        if preverified_path.exists():
            preverified = load(preverified_path)
            assert preverified['assetId'] == asset['id'] and preverified['releaseId'] == release['id']
            assert preverified['bytes'] == expected['bytes'] and preverified['sha256'] == expected['sha256']
            assert preverified['independentRemoteDownloadVerified'] and preverified['downloadSha256'] == expected['sha256'] and preverified['downloadedBytes'] == expected['bytes']
            verified = {key: preverified[key] for key in ['downloadedBytes', 'downloadSha256', 'independentRemoteDownloadVerified', 'verifiedAt']}
        else:
            verified = verify_asset(headers, asset, expected, relative)
        assert sha(path) == expected['sha256']
        row = {'bytes': item['bytes'], 'sha256': expected['sha256'], 'assetId': asset['id'], 'assetName': asset_name, 'apiUrl': API + '/releases/assets/' + str(asset['id']), 'remoteVerified': True, **verified, 'localCacheRemoved': False, 'activeCacheRetained': relative in KEEP}
        index['archives'][relative] = row
        save(OUT / 'index-state.json', index)
        save(OUT / ('asset-proof-' + expected['sha256'] + '.json'), {'originalPath': relative, **row})
        pending += 1
        pending_bytes += item['bytes']
        processed += 1
        if pending >= 12 or pending_bytes >= 128 * 1024 * 1024 or limit and processed >= limit:
            index['generation'] += 1
            publish_and_prune(index, baseline)
            pending, pending_bytes = 0, 0
        if limit and processed >= limit:
            break
    if pending:
        index['generation'] += 1
        publish_and_prune(index, baseline)
    save(OUT / 'terminal.json', {'exitCode': 0, 'processedThisInvocation': processed, 'verifiedArchives': len(index['archives']), 'localCacheRemoved': sum(row.get('localCacheRemoved', False) for row in index['archives'].values()), 'finishedAt': stamp()})

parser = argparse.ArgumentParser()
parser.add_argument('--limit', type=int, default=0)
arguments = parser.parse_args()
try:
    run(arguments.limit)
except Exception as failure:
    save(OUT / 'terminal-failure.json', {'exitCode': 1, 'exceptionType': type(failure).__name__, 'publicReason': str(failure) if isinstance(failure, AssertionError) else 'Network or tool operation failed; inspect authoritative state before retry', 'at': stamp()})
    print(json.dumps({'failed': type(failure).__name__, 'receipt': str(OUT / 'terminal-failure.json')}), flush=True)
    sys.exit(1)
