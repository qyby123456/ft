import re
import shutil
import subprocess
import sys
import uuid
from pathlib import Path

ROOT = Path('D:\\ft工程文件\\FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(ROOT / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = Path('C:\\ft工程文件C\\FTBoss-NeoForge-1.21.1-移植-20261022\\batches\\435-native-revocation-origin-binding\\reports\\actual-incremental435-experimental.json')
prototype = Path('C:/ft工程文件C/FTBoss-NeoForge-1.21.1-移植-20261022/work/session-chain/reports/functional-compiled.json')
evidence = Path('C:/ft工程文件C/FTBoss-NeoForge-1.21.1-移植-20261022/work/session-chain/reports/functional-checked.json')
for receipt in [binding, prototype, evidence]:
    data = load(receipt)
    for name, expected in {**data['inputHashes'], **data.get('outputHashes', {})}.items():
        assert digest(name) == expected, name
prior = Path(load(binding)['attempt'])
proto = Path(load(prototype)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
sources = [Path(value) for value in arguments if value.endswith('.java')]
attempt = BATCH / 'build' / ('current-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classes = attempt / 'complete-classes'
shutil.copytree(prior / 'complete-classes', classes)
before = {path.relative_to(prior / 'complete-classes').as_posix(): digest(path) for path in (prior / 'complete-classes').rglob('*.class')}
graph = dict(before)
for path in (proto / 'prototype-classes').rglob('*.class'):
    relative = path.relative_to(proto / 'prototype-classes')
    assert digest(path) == load(prototype)['outputHashes'][str(path)]
    shutil.copyfile(path, classes / relative)
    graph[relative.as_posix()] = digest(path)
proposal_root = Path('C:/ft工程文件C/FTBoss-NeoForge-1.21.1-移植-20261022/work/session-chain/proposal/net/farmingtales/ftboss')
targets = {next(path for path in sources if path.name == name): proposal_root / 'item' / name for name in ['OwnerFullRepairGuard.java', 'NativeExpectedFieldObservation.java']}
additional_sources = []
classpath = str(classes) + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
updates = []
new_guards = []
for name in ['NativeStackFieldRequirementProof172.java', 'OwnerFieldInstallationProof.java', 'NativeCapabilityInstallationProof170.java']:
    old = next(path for path in sources if path.name == name)
    text = old.read_text(encoding='utf-8')
    replacements = {}
    for relative, old_hash in before.items():
        if graph[relative] != old_hash and old_hash in text:
            replacements[old_hash] = graph[relative]
            text = text.replace(old_hash, graph[relative])
    additions = []
    route_replacement = []
    if name == 'NativeCapabilityInstallationProof170.java':
        old_guards = dict(re.findall(r'Map.entry\("([^"\n]+)",\s*"([0-9a-f]{64})"\)', text))
        assert len(old_guards) == 208
        marker = '    private static final Map<String, String> GUARDS = Map.ofEntries('
        for guard in new_guards:
            assert guard not in old_guards
            additions.append('\n        Map.entry("' + guard + '", "' + graph[('net/farmingtales/ftboss/' if guard.startswith('transaction/') else 'net/farmingtales/ftboss/item/') + guard + '.class'] + '"),')
        assert text.count(marker) == 1
        text = text.replace(marker, marker + ''.join(additions))
    if name == 'NativeCapabilityInstallationProof170.java':
        old_route = 'entry.getKey().startsWith("mixin/")'
        new_route = old_route + ''.join(' || entry.getKey().equals("' + guard + '")' for guard in new_guards if guard.startswith('transaction/'))
        assert text.count(old_route) == 1
        text = text.replace(old_route, new_route)
        route_replacement = [old_route, new_route]
    if not replacements and not additions:
        continue
    target = BATCH / 'proposal/net/farmingtales/ftboss/item' / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8',
        '-cp', classpath, '-d', str(classes), str(target)]
    save(attempt / (name + '-command.json'), command)
    with (attempt / (name + '-compile.log')).open('wb') as stream:
        terminal = subprocess.run(command, cwd=BATCH, stdout=stream, stderr=subprocess.STDOUT)
    save(attempt / (name + '-terminal.json'), {'exitCode': terminal.returncode})
    assert terminal.returncode == 0, name
    relative = 'net/farmingtales/ftboss/item/' + name.replace('.java', '.class')
    graph[relative] = digest(classes / relative)
    targets[old] = target
    updates.append({'source': str(target), 'beforeSource': str(old), 'hashReplacements': replacements, 'addedGuardSourceLines': additions, 'guardRouteReplacement': route_replacement})
actual_graph = {path.relative_to(classes).as_posix(): digest(path) for path in classes.rglob('*.class')}
assert graph == actual_graph and len(graph) == 1381 and set(before).issubset(graph)
effective = [targets.get(path, path) for path in sources] + additional_sources
changed = {old_hash: graph[name] for name, old_hash in before.items() if graph[name] != old_hash}
stale = []
for source in effective:
    text = source.read_text(encoding='utf-8')
    stale.extend({'source': str(source), 'oldHash': old_hash} for old_hash in changed if old_hash in text)
assert not stale, stale
args_path = attempt / 'complete-arguments316.json'
save(args_path, arguments[:arguments.index('-d')] + ['-d', str(classes)] + list(map(str, effective)))
proof_source = next(value for value in targets.values() if value.name == 'NativeCapabilityInstallationProof170.java')
pins = dict(re.findall(r'Map.entry\("([^"\n]+)",\s*"([0-9a-f]{64})"\)', proof_source.read_text(encoding='utf-8')))
assert len(pins) == 208
for name, value in pins.items():
    prefix = 'net/farmingtales/ftboss/' if name.startswith(('mixin/', 'util/', 'transaction/')) else 'net/farmingtales/ftboss/item/'
    assert digest(classes / (prefix + name + '.class')) == value, name
paths = [binding, prototype, evidence, ROOT / 'baseline.json', Path(__file__), args_path, *additional_sources, *targets.values()]
hashes = {str(path): digest(path) for path in paths}
hashes.update({str(classes / name): value for name, value in graph.items()})
save(BATCH / 'reports/actual-current.json', {
    'attempt': str(attempt), 'failures': 0, 'classCount': 1381, 'effectiveSourceCount': len(effective), 'guardCount': 208,
    'previousGoalTurnClassification': 'functional fix: keep independently planned native ranged writes within player domain continuity; reject other callbacks, owners, phases and raw escapes; milestone positive runtime pending',
    'changedRawClasses': sorted(name for name in before if graph[name] != before[name]),
    'newClasses': sorted(set(graph) - set(before)), 'authorityBindingUpdates': updates,
    'staleEffectiveSourceHashReferences': stale, 'runtimeAccepted': False, 'positiveSignedSessionAccepted': False,
    'installable': False, 'inputHashes': hashes})
print({'attempt': str(attempt), 'classes': len(graph), 'effectiveSources': len(effective), 'guards': len(pins), 'stale': len(stale)}, flush=True)
