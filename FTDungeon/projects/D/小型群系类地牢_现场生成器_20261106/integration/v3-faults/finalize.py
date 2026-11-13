import gzip
import hashlib
import importlib.util
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
import verify_saved_world as base

HOME = Path(__file__).resolve().parent


def main():
    initial = integrate.load(HOME / 'command-faults-initial.json')
    retried = integrate.load(HOME / 'command-faults.json')
    commands = initial['cases'] | retried['cases']
    crashes = integrate.load(HOME / 'progress.json')
    if crashes['status'] == 'RUNNING':
        raise integrate.GateError('Fault processes have not completed')
    spec = importlib.util.spec_from_file_location('frozen_native_reader', integrate.HOME / 'v2/read_native_be.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    reader = base.load_reader(integrate.HOME / 'staging/20261106_132015_129205')
    native_results = {}
    for scenario, entry in crashes['scenarios'].items():
        folder = Path(crashes['root']) / scenario
        preserved = folder / 'after-kill-evidence'
        marker = integrate.load(preserved / 'marker.json')
        position = tuple(marker[key] for key in ('x', 'y', 'z'))
        before = module.NativeWorld(reader, preserved)
        after = module.NativeWorld(reader, folder / 'world-process/world')
        before_tag, after_tag = before.entity(position), after.entity(position)
        if before_tag != after_tag:
            raise integrate.GateError('Offline native inventory changed during quarantine')
        items = before_tag.get('Items', [])
        if scenario == 'unknown_items' and not any(item.get('id') == 'minecraft:emerald' and item.get('Count') == 7 for item in items):
            raise integrate.GateError('Saved unknown emerald inventory was not independently observed')
        if scenario == 'stale_default' and items:
            raise integrate.GateError('Saved stale default inventory was not empty')
        saved = int(re.search(r'cursor=(\d+)', marker['savedStatus']).group(1))
        ahead = int(re.search(r'cursor=(\d+)', marker['armedStatus']).group(1))
        journals = list((preserved / 'journal').rglob('ftd-*.nbt'))
        if len(journals) != 1:
            raise integrate.GateError('Unexpected journal count')
        framed = journals[0].read_bytes()
        if framed[:4] != b'FTD2' or hashlib.sha256(framed[72:]).hexdigest().encode() != framed[8:72]:
            raise integrate.GateError('Crashed journal frame/checksum mismatch')
        journal = base.decode(reader, gzip.decompress(framed[72:]))
        if journal['cursor'] != ahead or ahead <= saved:
            raise integrate.GateError('Journal/save high-water skew not evidenced')
        rich = []
        for region in (preserved / 'region').glob('r.*.*.mca'):
            region_x, region_z = map(int, region.stem.split('.')[1:])
            header = region.read_bytes()[:4096]
            for index in range(1024):
                if int.from_bytes(header[index * 4:index * 4 + 3], 'big'):
                    before.chunk(region_x * 32 + index % 32, region_z * 32 + index // 32)
        for entities in before.native.values():
            for entity in entities.values():
                if entity.get('Items') or entity.get('Patterns'):
                    rich.append(entity)
        if not any(entity.get('Patterns') for entity in rich) or not any(any(item.get('id') == 'minecraft:diamond' for item in entity.get('Items', [])) for entity in rich):
            raise integrate.GateError('Synthetic nonempty Items/Patterns missing in saved native chunks')
        native_results[scenario] = dict(status='PASS_OFFLINE_NATIVE_PRESERVED', position=position, before=before_tag, after=after_tag, savedCursor=saved, journalCursor=ahead, journalAheadBy=ahead-saved, phase=journal['phase'], nativeNonemptySynthetic=rich, scope='Synthetic fixtures only; original Items/Patterns are empty. Force process termination is not power loss.')
    seal = integrate.load(integrate.HOME / 'v2/v1-seal.json')
    if integrate.digest(integrate.HOME / 'ready.json') != seal['readySha256']:
        raise integrate.GateError('V1 ready changed')
    for relative, checksum in seal['files'].items():
        if integrate.digest(integrate.contained(integrate.HOME, relative)) != checksum:
            raise integrate.GateError('V1 sealed file changed: ' + relative)
    v2 = integrate.check_component(integrate.HOME / 'v2', 'integration-v2')
    passed = len(commands) == 4 and all(case['status'] == 'PASS_REJECTED_ZERO_WRITES' for case in commands.values()) and crashes['status'] == 'PASS_SCOPED' and len(native_results) == 2
    result = dict(status='SCOPED_FAULT_PROTECTION_PASS' if passed else 'FAULT_EVIDENCE_CLOSED_WITH_FAILURES', completeGeneratorReady=False, productionPromotion=False, powerlossProven=False, commandCases=commands, crashCases=crashes, offlineNative=native_results, seals=dict(v1FilesVerified=len(seal['files']), v2FilesVerified=len(v2['files'])), limitations=['No power-loss or disk-flush guarantee', 'Synthetic nonempty inventories/patterns are not original-rich-payload evidence', 'Command zero writes scoped to 565794 saved target cells per case, not unrelated vanilla generation', 'No nine-room unified runtime, full route or interaction acceptance inherited from V2'])
    integrate.save(HOME / 'final-evidence.json', result)
    integrate.save(HOME / 'ISSUES.json', dict(completeGeneratorReady=False, runtimeDefectsObserved=[] if passed else ['See failed case evidence'], harnessCorrections=['Initial zero-test namespace mismatch preserved, not counted as pass', 'SHA256 refusal wording corrected; only affected command case rerun', 'Windows PowerShell serialized ancestor array as {value,Count}; termination refused safely, same live experiment then identity-reverified and continued'], remaining=['Unified nine-room/four-rotation route, interactions, native fluid/enclosure and installed JAR restart require independent new-version evidence', 'Power-loss durability NOT_PROVEN']))
    files = {}
    roots = [Path(crashes['root']), Path(initial['root']), Path(retried['root'])]
    for path in HOME.glob('*'):
        if path.is_file() and path.name != 'ready.json':
            files[str(path.relative_to(HOME))] = integrate.digest(path)
    for root in roots:
        for path in root.rglob('*'):
            if not path.is_file():
                continue
            relative = path.relative_to(root)
            if any(part in ('gradle-cache', 'libraries', '.gradle', 'project') for part in relative.parts):
                continue
            if path.suffix in ('.log', '.json', '.nbt', '.mca', '.bin', '.java', '.gradle') or path.name == 'candidate.jar':
                files[str(path.relative_to(HOME))] = integrate.digest(path)
    for path in (HOME / 'world-tests').rglob('*.java'):
        files[str(path.relative_to(HOME))] = integrate.digest(path)
    integrate.save(HOME / 'ready.json', dict(component='integration-v3-faults', apiVersion=1, ready=True, status=result['status'], completeGeneratorReady=False, productionPromotion=False, powerlossProven=False, files=files))
    print(result['status'], 'files=', len(files))


if __name__ == '__main__':
    main()
