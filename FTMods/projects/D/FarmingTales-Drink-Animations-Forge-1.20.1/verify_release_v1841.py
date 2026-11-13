"""Verify the foam-only release against the installed 1.8.40 baseline."""
from pathlib import Path
import hashlib
import json
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
VERSION = '1.8.41'
ASSETS = 'assets/farmingtales_drink_anim/'
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
jar = ROOT / f'build/libs/farmingtales_drink_anim-{VERSION}.jar'
baseline = ROOT / 'backups/1.8.40-before-foam-corks'
old_jar = next(baseline.glob('*.jar'))
allowed_changes = {
    'META-INF/mods.toml', ASSETS + 'beer_foam.json',
    'assets/minecraft/atlases/blocks.json',
    'com/farmingtales/drinkanim/math/BeerFoam.class',
    'com/farmingtales/drinkanim/math/BeerFoam$Definition.class',
    'com/farmingtales/drinkanim/math/BottleRig.class',
    'com/farmingtales/drinkanim/math/DrinkProfile.class',
    'com/farmingtales/drinkanim/math/DrinkProfile$Style.class',
}
allowed_additions = {
    ASSETS + f'textures/letsdo/brewery/beer_{name}_foam.png'
    for name in ('barley', 'hops')
} | {
    'com/farmingtales/drinkanim/math/BeerFoam$Surface.class',
    'com/farmingtales/drinkanim/math/BeerFoam$Texture.class',
}
with zipfile.ZipFile(old_jar) as old, zipfile.ZipFile(jar) as new:
    a, b = set(old.namelist()), set(new.namelist())
    changed = sorted(n for n in a & b if old.read(n) != new.read(n))
    recompiled_only = []
    for name in set(changed) - allowed_changes:
        assert name.endswith('.class'), name
        source = Path('src/main/java') / (name.removesuffix('.class').split('$')[0] + '.java')
        assert (ROOT / source).read_bytes() == (baseline / source).read_bytes(), name
        recompiled_only.append(name)
    assert b - a == allowed_additions
    removed = sorted(a - b)
    assert set(removed) <= {
        'com/farmingtales/drinkanim/math/ContainerAnimations$1.class',
        'com/farmingtales/drinkanim/math/DecorationMotion$2.class',
    }
    for name in removed:
        source = Path('src/main/java') / (name.split('$')[0] + '.java')
        assert (ROOT / source).read_bytes() == (baseline / source).read_bytes(), name
    foam = json.loads(new.read(ASSETS + 'beer_foam.json'))
    expected = {'brewery:beer_barley', 'brewery:beer_hops'}
    assert set(foam['items']) == set(foam['variants']) == expected
    textures = [v['texture'].split(':', 1)[1] for v in foam['variants'].values()]
    assert len(set(textures)) == 2
    assert len({new.read(ASSETS + t) for t in textures}) == 2
    previous_atlas = json.loads(old.read('assets/minecraft/atlases/blocks.json'))
    atlas = json.loads(new.read('assets/minecraft/atlases/blocks.json'))
    additions = [s for s in atlas['sources'] if s not in previous_atlas['sources']]
    assert {s['resource'] for s in additions} == {
        f'farmingtales_drink_anim:letsdo/brewery/beer_{name}_foam' for name in ('barley', 'hops')
    }
    assert [s for s in atlas['sources'] if s not in additions] == previous_atlas['sources']
    for f in (ROOT / 'src/main/resources').rglob('*'):
        if f.is_file():
            assert new.read(f.relative_to(ROOT / 'src/main/resources').as_posix()) == f.read_bytes(), f
    assert f'version="{VERSION}"' in new.read('META-INF/mods.toml').decode()

# Different local JDKs can emit different bytecode for unchanged sources.
renderer = Path('src/main/java/com/farmingtales/drinkanim/client/BottleRenderer.java')
assert (ROOT / renderer).read_bytes() == (baseline / renderer).read_bytes()
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(baseline / config.name)
tests = [ET.parse(f).getroot() for f in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 148, count
log = ROOT / 'preview/smoke-foam-only-1.8.41.log'
text = log.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 6' in text and 'BUILD SUCCESSFUL' in text
assert 'SILENT_CLIENT' in text
assert 'CATALOG_SMOKE FAIL' not in text
# The launcher also runs an older, unrelated wine timing scenario before the catalog.
# Preserve its findings without changing or claiming to validate that behavior here.
legacy_failures = [line for line in text.splitlines() if 'DRINK_SMOKE FAIL' in line]
for name in ('wheat', 'barley', 'hops', 'nettle', 'haley', 'oat'):
    assert f'CATALOG_SMOKE PASS brewery:beer_{name} drink consumes once and returns to idle' in text
report = dict(version=VERSION, sha256=sha(jar), previous_sha256=sha(old_jar),
              unit_tests=count, client_retested=True, client_profiles=6,
              client_log=str(log), client_log_sha256=sha(log),
              unrelated_legacy_wine_harness_failures=legacy_failures,
              changed_entries=changed, added_entries=sorted(allowed_additions),
              unchanged_source_recompiled=sorted(recompiled_only),
              removed_compiler_switch_helpers=removed,
              foam_items=sorted(expected), configuration_sha256=sha(config),
              scope='Foam only: barley and hops use separate textures; all other beers have no foam. '
                    'Foam slope response is 18%, capped at 4 degrees, with reduced ripples. '
                    'Existing bottle models, profiles, liquid motion, grip and sound settings retained.')
(ROOT / 'verification-1.8.41.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print(f'Verified {VERSION}: {count} tests, six client drink checks, foam-only JAR diff. SHA256 {sha(jar)}')
