import os
import shutil
import subprocess
import sys
import time
import uuid
import zipfile
import xml.etree.ElementTree as xml
from pathlib import Path

ROOT = Path('D:\\ft工程文件\\FTBoss-NeoForge-1.21.1-移植-20261022')
WORK = Path(__file__).resolve().parents[1]
BATCH = WORK.parent / 'session-chain/runtime'
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, extended, load, save
sys.path.insert(0, str(ROOT / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = WORK / 'reports/native-repair-candidate-2ca083af.json'
assert digest(binding) == '842ff9f8dbc7d746fa9979595e4172ff16c0279fc92bb91fa0c9b92c7652aa74'
bound = Path(load(binding)['attempt'])
prior_receipt = ROOT / 'batches/299-native-fresh-factory-runtime/reports/actual-native-map-version299-not-promoted.json'
assert digest(prior_receipt) == '1047eb067fb84070d2fa285bcb861980ec5ad808f5e3e5e8565057c2f7bcc18d'
prior = Path(load(prior_receipt)['actualRuntime'])
attempt = WORK / 'build' / ('repair-runtime-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
server = attempt / 'server'; server.mkdir()
proposal = attempt / 'ftboss-unpackaged-proposal'
shutil.copytree(extended(prior / 'ftboss-unpackaged-proposal'), extended(proposal))
graph = list((bound / 'complete-classes').rglob('*.class'))
assert len(graph) == 1381
for source in graph:
    target = proposal / source.relative_to(bound / 'complete-classes')
    target.parent.mkdir(parents=True, exist_ok=True); shutil.copyfile(source, target)
phase_resources = list((ROOT / 'batches/325-native-publication-session/proposal').glob('*.mixins.json'))
assert len(phase_resources) == 3
metadata = proposal / 'META-INF/neoforge.mods.toml'
with metadata.open('a', encoding='utf-8') as stream:
    for resource in phase_resources:
        shutil.copyfile(resource, proposal / resource.name)
        stream.write('\n[[mixins]]\nconfig = "' + resource.name + '"\n')
for directory in ['mods', 'recipe-inputs', 'kubejs']:
    if (prior / 'server' / directory).exists(): shutil.copytree(extended(prior / 'server' / directory), extended(server / directory))
seed_receipt = WORK / 'reports/native-repair-runtime-repair-save-terminal-final-route.json'
seed_result = load(seed_receipt)
seed_attempt = Path(seed_result['attempt'])
seed_terminal = load(seed_attempt / 'terminal308.json')
assert seed_result['exitCode'] == 0 and seed_result['probe']['failures'] == 0
assert seed_terminal['completeSave'] and seed_terminal['stopSent']
seed_world = seed_attempt / 'server/world'
assert seed_world.is_dir() and not any(path.is_symlink() for path in seed_world.rglob('*'))
seed_files = [seed_receipt, seed_attempt / 'terminal308.json', seed_world / 'level.dat',
    *list((seed_world / 'ftboss-authority').rglob('*.bin')), *list((seed_world / 'playerdata').glob('*.dat')),
    seed_world / 'data/ftboss_native_weapon_wear.dat']
seed_hashes = {str(path): digest(path) for path in seed_files}
shutil.copytree(extended(seed_world), extended(server / 'world'))
seed_case = seed_result['probe']['cases'][0]
assert seed_case['claimedWeaponSavedForRestart'] and seed_case['attackReturned']
assert seed_case['fullSignedSessionRan'] is False
(server / 'claimed-repair-seed-image.snbt').write_text(seed_case['claimedWeaponSavedImage'], encoding='utf-8')
(server / 'claimed-repair-seed-task.txt').write_text(seed_case['claimedWeaponSavedTaskIdentity'], encoding='utf-8')
for source_path in seed_files[2:]:
    assert digest(server / 'world' / source_path.relative_to(seed_world)) == seed_hashes[str(source_path)]
save(attempt / 'restart-world-seed.json', {'seedAttempt': str(seed_attempt), 'inputHashes': seed_hashes,
    'originalWorldModified': False, 'actualNewServerProcess': True, 'installable': False})
(server / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=true\nmax-tick-time=-1\nlevel-type=minecraft:flat\n', encoding='ascii')
for directory in ['isolated-home', 'isolated-tmp', 'observer-classes', 'observer-resources/META-INF']: (attempt / directory).mkdir(parents=True)
source = WORK / 'tests/curios-defined-factory/PositiveWeapon436.java'
args = load(bound / 'complete-arguments316.json')
classpath = str(bound / 'complete-classes') + ';' + relocate_text(args[args.index('-classpath') + 1])
agent_source = WORK / 'tests/definition-observer/DefinitionObserver.java'
agent_classes = attempt / 'definition-observer-classes'
agent_classes.mkdir()
agent_compile = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-d', str(agent_classes), str(agent_source)]
save(attempt / 'agent-compile-command.json', agent_compile)
with (attempt / 'agent-compile.log').open('wb') as stream:
    agent_compiled = subprocess.run(agent_compile, stdout=stream, stderr=subprocess.STDOUT)
assert agent_compiled.returncode == 0
agent_jar = attempt / 'readonly-definition-observer.jar'
with zipfile.ZipFile(agent_jar, 'x', compression=zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nPremain-Class: ftboss.provenance.DefinitionObserver\nCan-Redefine-Classes: false\nCan-Retransform-Classes: false\nCan-Set-Native-Method-Prefix: false\n\n')
    for compiled_agent in agent_classes.rglob('*.class'):
        archive.write(compiled_agent, compiled_agent.relative_to(agent_classes).as_posix())
capture_directory = attempt / 'jvm-definition-captures'
capture_directory.mkdir()
compile_command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'observer-classes'), str(source), str(BATCH / 'tests/Coverage436.java'), str(BATCH / 'tests/ItemStackProof361.java'), str(BATCH / 'tests/FingerprintBefore339.java'), str(BATCH / 'tests/FingerprintChecks339.java'), str(BATCH / 'tests/WearRoundTrip348.java'), str(BATCH / 'tests/RecipeSocketParity436.java'), str(BATCH / 'tests/ApprovedRulesBefore436.java'), str(BATCH / 'tests/SocketBefore436.java'), str(BATCH / 'tests/StackParity436.java'), str(BATCH / 'tests/RecipeDataBefore436.java'), str(BATCH / 'tests/InfusionEvidenceBefore436.java')]
save(attempt / 'compile-command308.json', compile_command)
with (attempt / 'compile308.log').open('wb') as stream: compiled = subprocess.run(compile_command, cwd=BATCH, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal320.json', {'exitCode': compiled.returncode, 'serverStarted': False})
assert compiled.returncode == 0
old_class = attempt / 'observer-classes/net/farmingtales/ftboss/item/FingerprintBefore339.class'
shutil.copyfile(old_class, proposal / 'net/farmingtales/ftboss/item/FingerprintBefore339.class')
old_class.unlink()
check_class = attempt / 'observer-classes/net/farmingtales/ftboss/item/FingerprintChecks339.class'
shutil.copyfile(check_class, proposal / 'net/farmingtales/ftboss/item/FingerprintChecks339.class')
check_class.unlink()


(attempt / 'observer-resources/META-INF/neoforge.mods.toml').write_text('modLoader="javafml"\nloaderVersion="[4,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftboss_storage308"\nversion="1.0"\ndisplayName="StorageView focused behavior"\n', encoding='utf-8')
command = load(prior / 'actual-runtime-arguments201.json')
response_old = prior / 'actual-official-dev198.args'
response = attempt / response_old.name
library_root = (prior / 'server/libraries').as_posix()
response_text = response_old.read_text(encoding='utf-8').replace('libraries/', library_root + '/').replace('-DlibraryDirectory=libraries', '-DlibraryDirectory="' + library_root + '"')
response_text = '\n'.join('"' + line.split(' ', 1)[0] + '" "' + line.split(' ', 1)[1] + '"' if line.startswith('-p ') else line for line in response_text.splitlines()) + '\n'
response.write_text(response_text, encoding='mbcs')
for index, value in enumerate(command):
    if value.startswith('-Dfml.modFolders='):
        parts = value.removeprefix('-Dfml.modFolders=').split(';')
        parts = [part for part in parts if part.startswith('minecraft%%')]
        command[index] = '-Dfml.modFolders=' + ';'.join(parts + ['ftboss%%' + str(proposal), 'ftboss_storage308%%' + str(attempt / 'observer-classes'), 'ftboss_storage308%%' + str(attempt / 'observer-resources')])
    elif value.startswith('-Duser.home='): command[index] = '-Duser.home=' + str(attempt / 'isolated-home')
    elif value.startswith('-Djava.io.tmpdir='): command[index] = '-Djava.io.tmpdir=' + str(attempt / 'isolated-tmp')
    elif value.startswith('-DlegacyClassPath='): command[index] = value.replace('libraries/', library_root + '/')
    elif value.startswith('@'): command[index] = '@' + str(response)
assert not any(value.startswith(('-javaagent:', '-agentlib:', '-agentpath:', '-Xrun')) for value in command)
assert not any(os.environ.get(name) for name in ['JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS'])
command[1:1] = ['--add-opens=java.instrument/sun.instrument=ALL-UNNAMED', '-XX:+DisableAttachMechanism',
    '-javaagent:' + str(agent_jar) + '=' + str(capture_directory), '-Dftboss.observe.definition.dir=' + str(capture_directory)]
save(attempt / 'runtime-command308.json', command)
inputs = {str(path): digest(path) for path in [binding, prior_receipt, source, BATCH / 'tests/StackParity436.java', BATCH / 'tests/RecipeDataBefore436.java', BATCH / 'tests/InfusionEvidenceBefore436.java', BATCH / 'tests/RecipeSocketParity436.java', BATCH / 'tests/ApprovedRulesBefore436.java', BATCH / 'tests/SocketBefore436.java', BATCH / 'tests/Coverage436.java', BATCH / 'tests/ItemStackProof361.java', BATCH / 'tests/FingerprintBefore339.java', BATCH / 'tests/FingerprintChecks339.java', response_old, response, Path(__file__), BATCH / 'tests/WearRoundTrip348.java', *phase_resources, metadata]}
save(attempt / 'inputs-before308.json', inputs)
inputs.update(seed_hashes)
for agent_input in [agent_source, agent_jar, attempt / 'agent-compile-command.json', attempt / 'agent-compile.log',
                    attempt / 'runtime-command308.json', *agent_classes.rglob('*.class')]:
    inputs[str(agent_input)] = digest(agent_input)
save(attempt / 'inputs-before308.json', inputs)
environment = os.environ.copy(); environment.pop('MOD_CLASSES', None)
with (attempt / 'runtime308.log').open('w', encoding='utf-8') as stream:
    process = subprocess.Popen(command, cwd=server, env=environment, stdin=subprocess.PIPE, stdout=stream, stderr=subprocess.STDOUT, text=True, encoding='utf-8')
    save(attempt / 'live-process308.json', {'pid': process.pid, 'attempt': str(attempt)})
    print({'attempt': str(attempt), 'pid': process.pid, 'focusedStorageViewOnly': True}, flush=True)
    deadline = time.monotonic() + 1800
    stop_sent = False
    while process.poll() is None:
        if (server / 'actual-storage-view308.json').exists() and not stop_sent:
            process.stdin.write('stop\n'); process.stdin.flush(); stop_sent = True
        if time.monotonic() > deadline:
            save(attempt / 'observation-deadline308.json', {'pid': process.pid, 'stillRunning': True})
            raise RuntimeError('Focused observer deadline reached; inspect live process, do not restart automatically')
        time.sleep(0.25)
    exit_code = process.returncode
log = (attempt / 'runtime308.log').read_text(encoding='utf-8', errors='replace')
probe = load(server / 'actual-storage-view308.json') if (server / 'actual-storage-view308.json').exists() else {'failures': 1, 'failure': 'Focused observer did not run'}
def properties(path):
    return {entry.attrib['key']: entry.text or '' for entry in xml.parse(path).getroot().findall('entry')}
startup = properties(capture_directory / 'startup.xml')
observer_terminal = properties(capture_directory / 'terminal.xml')
assert startup['preexistingTargetCount'] == '0'
assert startup['soleApplicableJavaTransformer'] == observer_terminal['soleApplicableJavaTransformer'] == 'true'
assert startup['ordinaryTransformerCount'] == observer_terminal['ordinaryTransformerCount'] == '1'
assert startup['retransformableTransformerCount'] == observer_terminal['retransformableTransformerCount'] == '0'
assert observer_terminal['errorCount'] == '0'
metadata_files = list(capture_directory.glob('capture-*.xml'))
assert int(observer_terminal['captureCount']) == len(metadata_files)
for metadata in metadata_files:
    captured = properties(metadata)
    assert captured['soleApplicableJavaTransformer'] == 'true' and captured['redefinition'] == 'false'
    assert captured['returnedReplacementBytes'] == 'false' and digest(captured['classFile']) == captured['sha256']
assert len(probe.get('jvmDefinitionCaptures', {})) == 8 and probe['actualOriginalLoadHasNonNullPaidLeaseAndGeneration']
capture_hashes = {str(path): digest(path) for path in capture_directory.rglob('*') if path.is_file()}
for name, wanted in inputs.items(): assert digest(name) == wanted
save(attempt / 'terminal308.json', {'exitCode': exit_code, 'probe': probe, 'stopSent': stop_sent, 'completeSave': 'All dimensions are saved' in log, 'fullRuntimeAcceptance': False})
output = WORK / 'reports/native-repair-runtime-curios-defined-factory.json'
output.parent.mkdir(exist_ok=True)
save(output, {'attempt': str(attempt), 'exitCode': exit_code, 'probe': probe, 'jvmDefinitionBoundaryCaptureHashes': capture_hashes,
    'soleNullReturningJavaTransformerInventoryObserved': True, 'otherStartupAgentsOrInheritedAgentOptions': False,
    'dynamicAttachDisabledForThisCaptureRun': True, 'curiosFactoryBytecodeProofAccepted': False,
    'reusedDependencyCrcAndLinkageReceipt299': str(prior_receipt), 'repeatedFull56OrRecipes': False,
    'fullLifecycleAccepted': False, 'installable': False, 'inputHashes': inputs})
print({'report': str(output), 'failures': probe['failures']}, flush=True)
assert exit_code == 0 and probe['failures'] == 0
