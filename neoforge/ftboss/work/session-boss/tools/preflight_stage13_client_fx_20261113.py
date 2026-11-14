import datetime
import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'reports/stage13-client-fx-20261113'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

assert not OUT.exists()
owner = load(ROOT / 'WRITE_OWNER.json')
claim = load(ROOT / 'WRITE_OWNER.claim')
assert owner['activeWriterThreadId'] == claim['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['noNewWeaponRuns'] and owner['weaponValidationPaused'] and owner['weaponSnapshotInstallable'] is False
query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;parentPid=$_.ParentProcessId;name=$_.Name;created=$_.CreationDate;owned=($_.CommandLine -match 'session-boss|session-lease|continue_native_signed_acceptance|run_native_material_proc|run_native_original_nineteen')} } | ConvertTo-Json -Compress"
raw = subprocess.check_output(['powershell', '-NoProfile', '-Command', query], encoding='utf-8-sig').strip()
processes = json.loads(raw) if raw else []
processes = processes if isinstance(processes, list) else [processes]
assert not any(row['owned'] and row['pid'] != os.getpid() for row in processes)
assert not any(row['name'] in ['java.exe', 'javaw.exe'] for row in processes)
acceptance_path = WORK / 'candidates/boss-core-stage13-natural-summon-20261113/acceptance.json'
archive_path = acceptance_path.parent / 'archive-receipt.json'
accepted = load(acceptance_path)
archive = load(archive_path)
assert accepted['checks'] == 315 and accepted['naturalOwnerTicks'] == 66 and accepted['naturalEntitySchedulingAccepted']
assert sha(archive['archive']) == archive['archiveSha256']
assert archive['allFilesReadBackAndHashVerified'] and archive['files'] == 445
assert sha(accepted['report']) == accepted['reportSha256']
result = load(accepted['report'])
assert result['terminal']['exitCode'] == 0 and result['terminal']['completeSave'] and result['terminal']['stopSent']
formal = accepted['formalInstancesUnchanged']
deployment_temps = []
for row in formal.values():
    assert sha(row['path']) == row['sha256']
    target = Path(row['path'])
    for path in target.parent.iterdir():
        if path.is_file() and path.name.startswith(('qys-bosses', 'ftboss-boss-core')) and path.suffix.lower() in ['.tmp', '.part', '.pending', '.new']:
            deployment_temps.append(str(path))
assert not deployment_temps, deployment_temps
scope = [ROOT, WORK, WORK / 'candidates/boss-core-stage13-natural-summon-20261113', ROOT / 'work/session-lease/reports/native-signed-acceptance-pipeline-v2-cf217105']
half_writes = [str(path) for folder in scope for path in folder.iterdir() if path.is_file() and (path.name.endswith(('.next.json', '.tmp', '.part', '.pending')) or path.name.startswith('~$'))]
assert not half_writes, half_writes
version = Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/FarmingTales_NeoForge 机械动力.json')
version_data = load(version)
argument_tokens = [value for value in version_data['arguments']['game'] if isinstance(value, str)]
assert '${auth_access_token}' in argument_tokens and '${auth_player_name}' in argument_tokens
auth_keys = ['FTBOSS_AUTH_ACCESS_TOKEN', 'FTBOSS_AUTH_UUID', 'FTBOSS_AUTH_PLAYER_NAME', 'MC_AUTH_ACCESS_TOKEN', 'MINECRAFT_ACCESS_TOKEN']
auth_presence = {name: bool(os.environ.get(name)) for name in auth_keys}
OUT.mkdir(parents=True)
save(OUT / 'process-audit-before.json', processes)
save(OUT / 'reuse-frozen-evidence.json', {'acceptance': str(acceptance_path), 'acceptanceSha256': sha(acceptance_path), 'report': accepted['report'], 'reportSha256': accepted['reportSha256'], 'archiveReceipt': str(archive_path), 'archiveReceiptSha256': sha(archive_path), 'archive': archive['archive'], 'archiveSha256': archive['archiveSha256'], 'reusedChecks': 315, 'reusedNaturalTicks': 66, 'normalExitAndCompleteSave': True, 'originalChecksRerun': False, 'originalNativeJvmsRestarted': False})
auth = {'createdAt': datetime.datetime.now().astimezone().isoformat(), 'status': 'blocked_external_authenticated_client_not_provided', 'authenticatedSummonEntryAccepted': False, 'observedLiveMinecraftJvms': 0, 'explicitTestCredentialEnvironmentPresence': auth_presence, 'launcherVersionDescription': str(version), 'launcherVersionSha256': sha(version), 'versionContainsCredentialPlaceholdersOnly': True, 'personalLauncherAccountStoresRead': False, 'credentialsPrintedOrCopied': False, 'authenticationServiceAvailabilityClaimed': False, 'testScopeLimitation': 'No actual authenticated client connection/session was supplied for this batch. Empty live Java census and absent explicit credential interface do not claim no account exists on this computer.', 'requiredExternalInput': 'A real authenticated Minecraft client session connecting to the isolated online-mode acceptance server; preserve original summon/start path and login verification.', 'noSyntheticServerPlayerOrAuthority': True, 'fallback': 'One isolated real-client summon particle delivery/render check; offline integrated login explicitly does not satisfy authenticated or dedicated-multiplayer acceptance.'}
assert not any(auth_presence.values()), 'An explicit auth test input is present; inspect its authorized flow before selecting fallback'
save(OUT / 'authenticated-entry-blocker.json', auth)
save(OUT / 'preflight.json', {'failures': 0, 'createdAt': datetime.datetime.now().astimezone().isoformat(), 'soleWriter': claim['threadId'], 'ownedJavaPythonRemainingExcludingThisAudit': 0, 'unrelatedPythonProcessesLeftUntouched': sum(row['name'] in ['python.exe', 'pythonw.exe'] and not row['owned'] for row in processes), 'deploymentTemporaryFiles': deployment_temps, 'scopedMetadataHalfWrites': half_writes, 'scopeDirectories': [str(path) for path in scope], 'knownEarlierPausePreflightIncompleteFolderPreserved': str(WORK / 'archives/weapons-paused-cf217105-boss-priority-20261113'), 'formalInstancesUnchanged': formal, 'original315ChecksReusedWithoutRerun': True, 'authenticatedEntryBlocker': str(OUT / 'authenticated-entry-blocker.json'), 'selectedSingleRemainingAcceptance': 'Client summon particle packet delivery and actual front-view rendering only', 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'weaponRecoveryJarInstallable': False, 'stage13Installable': False, 'deploymentAllowed': False})
print(json.dumps({'preflight': str(OUT / 'preflight.json'), 'ownedResidualProcesses': 0, 'deploymentTemps': 0, 'metadataHalfWrites': 0, 'authStatus': auth['status'], 'next': 'actual isolated client summon particles only; no deployment or weapon work'}), flush=True)
