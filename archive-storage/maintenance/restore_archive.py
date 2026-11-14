import argparse
import hashlib
import json
import os
import subprocess
import time
import urllib.parse
import urllib.request
from pathlib import Path

class SafeRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, connection, code, message, headers, url):
        redirected = super().redirect_request(request, connection, code, message, headers, url)
        if redirected is not None and urllib.parse.urlparse(request.full_url).hostname != urllib.parse.urlparse(url).hostname:
            redirected.remove_header('Authorization')
        return redirected

parser = argparse.ArgumentParser(description='Fetch one verified draft-release archive into a new local cache file.')
parser.add_argument('--repository', type=Path, required=True)
parser.add_argument('--commit', default='HEAD')
parser.add_argument('--archive', required=True)
parser.add_argument('--output', type=Path, required=True)
arguments = parser.parse_args()
repository = arguments.repository.resolve()
result = subprocess.run(['git', '-C', str(repository), 'show', arguments.commit + ':archive-storage/INDEX.json'], capture_output=True, check=True)
index = json.loads(result.stdout)
row = index['archives'][arguments.archive]
assert row['remoteVerified'] and row['independentRemoteDownloadVerified']
assert row['downloadSha256'] == row['sha256'] and row['downloadedBytes'] == row['bytes']
output = arguments.output.resolve()
assert not output.exists() and output.parent.is_dir()
partial = output.with_name(output.name + '.download-partial')
assert not partial.exists()
credential = subprocess.run(['git', '-C', str(repository), 'credential', 'fill'], input='protocol=https\nhost=github.com\npath=qyby123456/ft.git\n\n', capture_output=True, text=True, env=dict(os.environ, GIT_TERMINAL_PROMPT='0', GCM_INTERACTIVE='Never'), timeout=30)
assert credential.returncode == 0, 'Existing GitHub authentication unavailable'
fields = dict(line.split('=', 1) for line in credential.stdout.splitlines() if '=' in line)
assert fields.get('password')
assert index['repository'] == 'qyby123456/ft'
url = 'https://api.github.com/repos/qyby123456/ft/releases/assets/' + str(row['assetId']) + '?cache-restore=' + str(time.time_ns())
headers = {'Authorization': 'Bearer ' + fields['password'], 'Accept': 'application/octet-stream', 'X-GitHub-Api-Version': '2022-11-28', 'User-Agent': 'FT-verified-cache-restore'}
proxy = os.environ.get('FT_TASK_PROXY')
opener = urllib.request.build_opener(urllib.request.ProxyHandler({'https': proxy, 'http': proxy}) if proxy else urllib.request.ProxyHandler(), SafeRedirect())
request = urllib.request.Request(url, headers=headers)
try:
    hasher = hashlib.sha256()
    count = 0
    with opener.open(request, timeout=45) as response, partial.open('xb') as stream:
        assert response.status == 200 and 'json' not in response.headers.get('Content-Type', '').lower()
        while True:
            data = response.read(4 * 1024 * 1024)
            if not data:
                break
            count += len(data)
            assert count <= row['bytes']
            hasher.update(data)
            stream.write(data)
    assert count == row['bytes'] and hasher.hexdigest() == row['sha256'], 'Archive recovery bytes did not match Git index'
    assert not output.exists()
    partial.replace(output)
    print(json.dumps({'archive': arguments.archive, 'cacheFile': str(output), 'bytes': count, 'sha256': hasher.hexdigest(), 'recoveryVerified': True, 'installable': False}), flush=True)
except Exception:
    if partial.exists():
        assert partial.resolve().parent == output.parent and partial.name == output.name + '.download-partial'
        partial.unlink()
    raise
