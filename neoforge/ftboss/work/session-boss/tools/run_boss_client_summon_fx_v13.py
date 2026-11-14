import hashlib
import json
import os
import shutil
import subprocess
import time
import uuid
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
JAVA = Path('C:/Program Files/Zulu/zulu-21/bin')
MC = Path('C:/Users/qyby/AppData/Roaming/.minecraft')

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['noNewWeaponRuns'] and owner['weaponValidationPaused'] and owner['weaponSnapshotInstallable'] is False
assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == owner['activeWriterThreadId']
query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell', '-NoProfile', '-Command', query], encoding='utf-8-sig').strip()
preflight = WORK / 'reports/stage13-client-fx-20261113/preflight.json'
assert load(preflight)['failures'] == 0
assert load(preflight.parent / 'authenticated-entry-blocker.json')['authenticatedSummonEntryAccepted'] is False
built_path = WORK / 'build/boss-summon-fx-client-assets-v2/candidate-build-receipt.json'
built = load(built_path)
boss_jar = Path(built['jar'])
assert sha(boss_jar) == built['jarSha256'] == '589ad968f2efe15a98a1438ee9d9b7218343c197b69d616ada7da3681bb2e3fe'
prior_report = WORK / 'reports/summon-fx-v8-candidate-b74fa0f4.json'
prior = load(prior_report)
assert prior['probe']['failures'] == 0 and prior['terminal']['completeSave']
prior_attempt = Path(prior['attempt'])
attempt = WORK / 'build' / ('client-summon-fx-v13-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
game = attempt / 'game'
game.mkdir()
for folder in ['mods', 'recipe-inputs', 'kubejs']:
    source = prior_attempt / 'server' / folder
    if source.exists():
        shutil.copytree(source, game / folder)
old_boss = game / 'mods/ftboss-boss-core-1.21.1-stage13-summon-fx-NOT-INSTALLABLE.jar'
assert sha(old_boss) == built['baseJarSha256']
assert built['resourceRestorationOnly'] and built['allOriginalEntriesByteIdentical']
old_boss.unlink()
shutil.copyfile(boss_jar, game / 'mods' / boss_jar.name)
assert sha(game / 'mods' / boss_jar.name) == built['jarSha256']
assert not (game / 'saves').exists(), 'Native client must create its own fresh isolated world'
for folder in ['observer-classes', 'observer-resources/META-INF', 'isolated-home', 'isolated-tmp', 'natives']:
    (attempt / folder).mkdir(parents=True)
context_path = WORK / 'build/boss-flight-ban-v1/compile-context.json'
context = load(context_path)
sdk = []
for entry in context['classpath'].split(';'):
    path = Path(entry)
    if path.suffix.lower() != '.jar':
        continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():
            continue
    sdk.append(str(path))
source_dir = WORK / 'tests/client-summon-fx-v4'
sources = sorted(source_dir.glob('*.java'))
compile_command = [str(JAVA / 'javac.exe'), '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', str(boss_jar) + ';' + ';'.join(sdk), '-d', str(attempt / 'observer-classes'), *[str(path) for path in sources]]
save(attempt / 'compile-command.json', compile_command)
with (attempt / 'compile.log').open('wb') as stream:
    compiled = subprocess.run(compile_command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal.json', {'exitCode': compiled.returncode, 'clientStarted': False})
assert compiled.returncode == 0, str(attempt / 'compile.log')
metadata = attempt / 'observer-resources/META-INF/neoforge.mods.toml'
metadata.write_text('modLoader="javafml"\nloaderVersion="[4,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftboss_clientfxprobe"\nversion="1.0"\ndisplayName="Boss isolated client summon FX observer"\n[[mixins]]\nconfig="ftboss.clientfxprobe.mixins.json"\n', encoding='utf-8')
mixins = attempt / 'observer-resources/ftboss.clientfxprobe.mixins.json'
save(mixins, {'required': True, 'minVersion': '0.8', 'package': 'ftboss.clientfxprobe.mixin', 'compatibilityLevel': 'JAVA_21', 'client': ['ClientSummonPacketObserver'], 'injectors': {'defaultRequire': 1}})
version_path = MC / 'versions/FarmingTales_NeoForge 机械动力/FarmingTales_NeoForge 机械动力.json'
version = load(version_path)
libraries = []
native_inputs = []
for entry in version['libraries']:
    artifact = entry.get('downloads', {}).get('artifact')
    if not artifact:
        continue
    name = entry['name']
    if ':natives-' in name and not name.endswith(':natives-windows'):
        continue
    rules = entry.get('rules', [])
    allowed = not rules
    for rule in rules:
        platform = rule.get('os', {})
        if platform.get('name', 'windows') == 'windows' and platform.get('arch', 'x86_64') in ['x86_64', 'amd64']:
            allowed = rule['action'] == 'allow'
    if not allowed:
        continue
    library = MC / 'libraries' / artifact['path']
    assert library.is_file(), library
    libraries.append(library)
    if name.endswith(':natives-windows'):
        native_inputs.append(library)
        with zipfile.ZipFile(library) as bundle:
            for member in bundle.namelist():
                if member.lower().endswith('.dll'):
                    target = attempt / 'natives' / Path(member).name
                    payload = bundle.read(member)
                    if target.exists():
                        assert target.read_bytes() == payload
                    else:
                        target.write_bytes(payload)
libraries = list(dict.fromkeys(libraries))
original_command = load(prior_attempt / 'runtime-command.json')
old_mods = next(value.removeprefix('-Dfml.modFolders=') for value in original_command if value.startswith('-Dfml.modFolders='))
minecraft_roots = [Path(value.split('%%', 1)[1]) for value in old_mods.split(';') if value.startswith('minecraft%%')]
vanilla_client = next(Path(value) for value in context['classpath'].split(';') if Path(value).name.endswith('-client-1.21.1-official.jar'))
client_classes = attempt / 'client'
client_classes.mkdir()
client_extra = attempt / 'client-extra-resources.jar'
extra_manifest = {}
with zipfile.ZipFile(vanilla_client) as original, zipfile.ZipFile(client_extra, 'x', compression=zipfile.ZIP_DEFLATED) as resources:
    for member in original.namelist():
        if member.endswith('/'):
            continue
        if member.endswith('.class'):
            if any((base / member).exists() for base in minecraft_roots):
                continue
            destination = client_classes / member
            assert destination.resolve().is_relative_to(client_classes.resolve())
            destination.parent.mkdir(parents=True, exist_ok=True)
            payload = original.read(member)
            destination.write_bytes(payload)
            extra_manifest[member] = hashlib.sha256(payload).hexdigest()
        elif not member.startswith('META-INF/'):
            resources.writestr(member, original.read(member))
save(attempt / 'client-extra-classes-manifest.json', extra_manifest)
save(attempt / 'client-view-receipt.json', {'originalOfficialClient': str(vanilla_client), 'originalOfficialClientSha256': sha(vanilla_client), 'historicalClassAndResourceRootsPreserved': [str(path) for path in minecraft_roots], 'onlyAbsentClassFilesAdded': len(extra_manifest), 'originalPatchedClassesUnmodified': True, 'separateVanillaClientResourcesJar': str(client_extra), 'clientExtraSha256': sha(client_extra)})
original_command = [value + ';minecraft%%' + str(client_classes) if value.startswith('-Dfml.modFolders=') else value + ';' + str(client_classes) + ';' + str(client_extra) if value.startswith('-DlegacyClassPath=') else value for value in original_command]
original_cp = original_command.index('-cp') + 1
original_command[original_cp] += ';' + str(client_classes) + ';' + str(client_extra)
prior_classpath = original_command[original_command.index('-cp') + 1].split(';')
legacy = next(value.removeprefix('-DlegacyClassPath=') for value in original_command if value.startswith('-DlegacyClassPath='))
existing_jars = {Path(value).name: Path(value) for value in [*prior_classpath, *legacy.split(';')] if Path(value).suffix.lower() == '.jar' and Path(value).is_file()}
additional_libraries = []
for library in libraries:
    if library.name in existing_jars:
        assert sha(library) == sha(existing_jars[library.name]), library
    else:
        additional_libraries.append(library)
response_old = prior_attempt / 'actual-official-dev198.args'
response = attempt / 'actual-client-dev.args'
response_text = response_old.read_text(encoding='mbcs').replace('--launchTarget forgeserverdev', '--launchTarget forgeclientdev').replace('-DignoreList=', '-DignoreList=client,client-extra-resources.jar,')
assert '--launchTarget forgeclientdev' in response_text
response.write_text(response_text, encoding='mbcs')
command = []
for value in original_command:
    if value == 'nogui' or value.startswith(('-Djava.awt.headless=', '-Dmixin.debug.', '-Dftboss.summonfx.mode=')):
        continue
    if value.startswith('-Dfml.modFolders='):
        minecraft = [part for part in value.removeprefix('-Dfml.modFolders=').split(';') if part.startswith('minecraft%%')]
        value = '-Dfml.modFolders=' + ';'.join(minecraft + ['ftboss_clientfxprobe%%' + str(attempt / 'observer-classes'), 'ftboss_clientfxprobe%%' + str(attempt / 'observer-resources')])
    elif value.startswith('-Duser.home='):
        value = '-Duser.home=' + str(attempt / 'isolated-home')
    elif value.startswith('-Djava.io.tmpdir='):
        value = '-Djava.io.tmpdir=' + str(attempt / 'isolated-tmp')
    elif value.startswith('-DlegacyClassPath='):
        value += ';' + ';'.join(str(path) for path in additional_libraries)
    elif value.startswith('@'):
        value = '@' + str(response)
    command.append(value)
classpath_index = command.index('-cp') + 1
command[classpath_index] += ';' + ';'.join(str(path) for path in additional_libraries)
command[1:1] = ['-Djava.library.path=' + str(attempt / 'natives'), '-Dorg.lwjgl.system.SharedLibraryExtractPath=' + str(attempt / 'natives'), '-Djna.tmpdir=' + str(attempt / 'natives'), '-Dfml.earlyWindowControl=false']
command += ['--username', 'ft_fx_probe', '--uuid', '00000000000000000000000000b05513', '--accessToken', '0', '--userType', 'legacy', '--version', 'isolated-client-fx', '--gameDir', str(game), '--assetsDir', str(MC / 'assets'), '--assetIndex', '17', '--width', '960', '--height', '640']
save(attempt / 'expanded-runtime-command.json', command)
response_index = next(index for index, value in enumerate(command) if value.startswith('@'))
def quote_argument(value):
    return '"' + value.replace('\\', '\\\\').replace('"', '\\"') + '"'
full_response = attempt / 'full-client-launch.args'
full_text = '\n'.join(quote_argument(value) for value in command[1:response_index]) + '\n' + response_text + '\n' + '\n'.join(quote_argument(value) for value in command[response_index + 1:]) + '\n'
full_response.write_text(full_text, encoding='mbcs')
command = [command[0], '@' + str(full_response)]
save(attempt / 'runtime-command.json', command)
inputs = {str(path): sha(path) for path in [Path(__file__), preflight, preflight.parent / 'authenticated-entry-blocker.json', prior_report, built_path, boss_jar, context_path, WORK / 'build/client-normal-stop-v1/client-normal-stop.jar', metadata, mixins, response_old, response, full_response, client_extra, vanilla_client, version_path, MC / 'assets/indexes/17.json', *sources, *libraries]}
save(attempt / 'inputs-before.json', inputs)
environment = os.environ.copy()
environment.pop('MOD_CLASSES', None)
game.joinpath('options.txt').write_text('pauseOnLostFocus:false\nrenderDistance:4\nsimulationDistance:5\nmaxFps:60\nguiScale:2\nfov:0.0\nonboardAccessibility:false\nparticles:0\nfullscreen:false\n', encoding='utf-8')
with (attempt / 'runtime.log').open('w', encoding='utf-8') as stream:
    process = subprocess.Popen(command, cwd=game, env=environment, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    save(attempt / 'live-process.json', {'pid': process.pid, 'attempt': str(attempt), 'isolatedGame': str(game), 'actualClient': True, 'offlineFixture': True, 'authenticated': False, 'formalInstanceUsedReadOnly': True, 'noWeaponAddon': True})
    print(json.dumps({'attempt': str(attempt), 'pid': process.pid, 'actualClient': True, 'offlineNotAuthenticated': True}), flush=True)
    deadline = time.monotonic() + 1800
    cleanup_attempted = False
    while process.poll() is None:
        observed_log = (attempt / 'runtime.log').read_text(encoding='utf-8', errors='replace')
        if not cleanup_attempted and ('Crash report saved to' in observed_log or ('Caught error loading resourcepacks' in observed_log and 'ModLoadingException' in observed_log)) and not (game / 'actual-client-summon-fx.json').exists():
            cleanup_attempted = True
            stop_jar = WORK / 'build/client-normal-stop-v1/client-normal-stop.jar'
            stop_command = [str(JAVA / 'java.exe'), '--add-modules', 'jdk.attach', '-cp', str(stop_jar), 'ClientNormalStopAttach', str(process.pid), str(stop_jar), 'normal-stop-agent-receipt.json']
            cleanup = subprocess.run(stop_command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=30)
            save(attempt / 'bootstrap-fatal-normal-stop.json', {'cleanupOnly': True, 'exitCode': cleanup.returncode, 'output': cleanup.stdout + cleanup.stderr, 'fatalClientModLoadingFailurePreserved': True, 'authenticationOrAuthorityModified': False})
        if time.monotonic() > deadline:
            print(json.dumps({'observationExtendedForSameClient': process.pid, 'noForcedRestart': True}), flush=True)
            deadline = time.monotonic() + 1800
        time.sleep(0.5)
log = (attempt / 'runtime.log').read_text(encoding='utf-8', errors='replace')
probe_path = game / 'actual-client-summon-fx.json'
probe = load(probe_path) if probe_path.exists() else {'failures': 1, 'failure': 'Actual client observer did not complete'}
unchanged = all(sha(name) == expected for name, expected in inputs.items())
terminal = {'exitCode': process.returncode, 'observerRequestedNormalClientStop': probe_path.exists(), 'integratedWorldCompleteSave': 'All dimensions are saved' in log, 'inputHashesUnchanged': unchanged, 'clientProcessReaped': True}
save(attempt / 'terminal.json', terminal)
output = WORK / 'reports' / (attempt.name + '.json')
save(output, {'attempt': str(attempt), 'terminal': terminal, 'probe': probe, 'inputHashes': inputs, 'original315ChecksRerun': False, 'authenticatedSummonEntryAccepted': False, 'authenticatedMultiplayerAccepted': False, 'fullBossMigrationComplete': False, 'weaponDevelopmentPaused': True, 'weaponRecoveryJarInstallable': False, 'installable': False, 'deployed': False})
print(json.dumps({'report': str(output), 'terminal': terminal, 'probeFailures': probe['failures'], 'actualClientParticles': probe.get('exactOriginalParticlePacketDeliveryAccepted', False), 'actualFrontRender': probe.get('actualFrontRenderAccepted', False)}), flush=True)
assert process.returncode == 0 and unchanged and probe['failures'] == 0 and terminal['integratedWorldCompleteSave']
