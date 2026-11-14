import importlib.util
import json
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / '_work/saya-dynamic-vision-20261026'
spec = importlib.util.spec_from_file_location('support', ROOT / '_work/material-bow-combat-20261022/build.py')
support = importlib.util.module_from_spec(spec)
spec.loader.exec_module(support)
support.WORK = WORK
BASE = 'net/farmingtales/ftboss/'
CLASSES = ['client/ThornCrownOverlay', 'util/SayaFleshVisionRules']
TEXTURES = ['saya_flesh_dynamic.png', 'saya_veins_dynamic.png']


def main():
    WORK.mkdir(exist_ok=True)
    (WORK / 'empty').mkdir(exist_ok=True)
    baseline = WORK / 'baseline.jar'
    shutil.copy2(support.TARGET, baseline)
    baseline_hash = support.sha(baseline)
    sources = [ROOT / 'src/main/java' / (BASE + name + '.java') for name in CLASSES]
    sources += [ROOT / 'src/main/resources/assets/ftboss/textures/gui/overlays' / name for name in TEXTURES]
    sources += [Path(__file__), ROOT / 'tools/model3d/gen_saya_flesh_vision.cjs',
                ROOT / 'tools/verification/SayaFleshVisionCheck.java',
                ROOT / 'tools/verification/check_saya_flesh_vision.cjs']
    source_hashes = {str(path): support.sha(path) for path in sources}
    dependencies = [Path(value) for value in json.loads(
        (ROOT / '_work/trident-ranged-parity-20261025/dependencies.json').read_text())]
    mods = sorted(support.TARGET.parent.glob('*.jar'))
    asm = [support.LIBRARIES / f'org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar'
           for name in ['asm', 'asm-tree', 'asm-util', 'asm-analysis']]
    support.run(['node', ROOT / 'tools/verification/check_saya_flesh_vision.cjs',
                 WORK / 'generated-check'], 'assets-and-wiring')
    named_baseline = WORK / 'baseline-mapped.jar'
    support.run([support.JDK / 'java.exe', '-Xmx2G', '-jar', support.FART, '--input', baseline,
                 '--output', named_baseline, '--map', support.MAPPINGS, '--lib', support.SRG_FORGE,
                 '--threads', '2'], 'map-baseline')
    classes = WORK / 'classes'
    support.compile_java(sources[:2], classes, [support.FORGE, named_baseline, *dependencies, *mods], 'compile-overlay')
    tools = WORK / 'tools'
    support.compile_java([ROOT / 'tools/verification/SayaFleshVisionCheck.java',
                          ROOT / 'tools/verification/ZipStreamCheck.java',
                          ROOT / 'tools/verification/FoodWeaponBuildLinkCheck.java'],
                         tools, [classes, *asm], 'compile-checks')
    support.run([support.JDK / 'java.exe', '-Xverify:all', '-cp', ';'.join(map(str, [tools, classes])),
                 'SayaFleshVisionCheck'], 'animation-rules')
    named = WORK / 'named.jar'
    support.run([support.JDK / 'jar.exe', '--create', '--file', named, '-C', classes, '.'], 'pack-classes')
    reobf = WORK / 'reobf.jar'
    support.run([support.JDK / 'java.exe', '-Xmx1G', '-jar', support.FART, '--input', named, '--output', reobf,
                 '--map', support.MAPPINGS, '--reverse', '--lib', support.FORGE, '--lib', named_baseline,
                 '--threads', '2'], 'reobfuscate')
    overlay = WORK / 'overlay'
    with zipfile.ZipFile(reobf) as compiled:
        for name in CLASSES:
            compiled.extract(BASE + name + '.class', overlay)
    for name in TEXTURES:
        entry = Path('assets/ftboss/textures/gui/overlays') / name
        destination = overlay / entry
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(ROOT / 'src/main/resources' / entry, destination)
    expected = {path.relative_to(overlay).as_posix(): support.sha(path)
                for path in overlay.rglob('*') if path.is_file()}
    assert len(expected) == 4, 'Unexpected overlay files'
    candidate = WORK / 'qys-bosses.jar'
    shutil.copy2(baseline, candidate)
    support.run([support.JDK / 'jar.exe', '--update', '--file', candidate, '-C', overlay, '.'], 'package')
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        previous = set(before.namelist())
        current = set(after.namelist())
        assert previous <= current and len(after.namelist()) == len(current)
        changed = {entry for entry in previous if before.read(entry) != after.read(entry)}
        added = current - previous
        assert changed | added == set(expected), 'Out-of-scope JAR change'
        assert after.testzip() is None
        for entry, digest in expected.items():
            assert support.hashlib.sha256(after.read(entry)).hexdigest() == digest
    support.run(['node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate], 'zip-rules')
    support.run([support.JDK / 'java.exe', '-cp', tools, 'ZipStreamCheck', candidate], 'zip-stream')
    mapped = WORK / 'candidate-mapped.jar'
    support.run([support.JDK / 'java.exe', '-Xmx2G', '-jar', support.FART, '--input', candidate,
                 '--output', mapped, '--map', support.MAPPINGS, '--lib', support.SRG_FORGE,
                 '--threads', '2'], 'map-candidate')
    production = WORK / 'production'
    with zipfile.ZipFile(mapped) as archive:
        for name in CLASSES:
            archive.extract(BASE + name + '.class', production)
    support.run([support.JDK / 'java.exe', '-Xverify:all', '-cp', ';'.join(map(str,
                 [tools, mapped, support.FORGE, *dependencies, *mods, *asm])),
                 'FoodWeaponBuildLinkCheck', production, mapped], 'verify-production-links')
    assert all(support.sha(Path(path)) == digest for path, digest in source_hashes.items()), 'Source changed during build'
    assert support.sha(support.TARGET) == baseline_hash, 'Runtime changed during build; rebuild on latest baseline'
    report = dict(passed=True, baselineSha256=baseline_hash, candidateSha256=support.sha(candidate),
                  changed=sorted(changed), added=sorted(added), preservedEntries=len(previous) - len(changed),
                  sources=source_hashes, frames=12, frameTicks=3, framesPerSecond=20 / 3,
                  atlasMemoryMiB=13.5, docsTouched=False, gameplayTested=False,
                  docsProposal='_work/docs-proposals/saya-dynamic-armor-vision.md')
    (WORK / 'build-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('PASS dynamic Saya armor vision candidate: ' + support.sha(candidate), flush=True)


if __name__ == '__main__':
    main()
