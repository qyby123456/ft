from pathlib import Path
from PIL import Image
import base64, collections, copy, hashlib, io, json, zipfile, xml.etree.ElementTree as ET
from prepare_jar_family import discover

ROOT = Path(__file__).resolve().parent
ASSET = 'assets/farmingtales_drink_anim/'
BACKUP = ROOT / 'backups/1.8.20-before-jar-family'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.21.jar'

def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

baseline = read(BACKUP / 'verification.json')
prior = BACKUP / '[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.20.jar'
assert sha(prior) == baseline['sha256']
manifest = read(ROOT / 'jar-family-sources.json')
assert {e['id'] for e in manifest['entries']} == {'farmingtales:' + name for name, _, _ in discover()}
assert collections.Counter(e['category'] for e in manifest['entries']) == {'jam': 32, 'roe_sauce': 89, 'mayonnaise': 8}
for source in manifest['sources'] + read(ROOT / 'jam-source-assets.json'):
    assert sha(Path(source['path'])) == source['sha256'], source['path']
with zipfile.ZipFile(JAR) as built, zipfile.ZipFile(prior) as old:
    profiles = json.loads(built.read(ASSET + 'drink_profiles.json'))
    previous = json.loads(old.read(ASSET + 'drink_profiles.json'))
    assert len(profiles) == 182 and profiles[:54] == previous
    by_id = {p['id']: p for p in profiles}
    assert len(by_id) == len(profiles)
    expected = {e['id'] for e in manifest['entries']}
    assert {p['id'] for p in profiles if p['style'] == 'JAM'} == expected
    prototype = by_id['farmingtales:ancient_fruit_jam_1']
    masks = json.loads(built.read(ASSET + 'hand_volumes.json'))
    atlas = json.loads(built.read('assets/minecraft/atlases/blocks.json'))
    sprites = {s.get('resource') for s in atlas['sources']}
    allowed = {ASSET + 'drink_profiles.json', ASSET + 'hand_volumes.json', 'assets/minecraft/atlases/blocks.json'}
    for name in old.namelist():
        if (name.startswith('assets/') and name not in allowed) or name.endswith('.class'):
            assert built.read(name) == old.read(name), 'Approved runtime/resource changed: ' + name
    for name in built.namelist():
        assert not name.startswith('assets/farmingtales/') and '/smoke/' not in name
    meta = built.read('META-INF/mods.toml').decode()
    assert all(s in meta for s in ['version="1.8.21"', 'license="ARR"', 'authors="紫菜"', '果酱、鱼籽酱和蛋黄酱'])
    assert 'JarEatingParticlesMixin' in built.read('farmingtales_drink_anim.mixins.json').decode()
    for source in (ROOT / 'src/main/resources').rglob('*'):
        if source.is_file():
            assert built.read(source.relative_to(ROOT / 'src/main/resources').as_posix()) == source.read_bytes()
    for entry in manifest['entries']:
        p = by_id[entry['id']]
        canonical = copy.deepcopy(p)
        for key in ['id', 'bodyModel', 'capModel', 'neckModel']:
            canonical[key] = prototype[key]
        canonical['liquid'][0]['texture'] = prototype['liquid'][0]['texture']
        assert canonical == prototype, entry['id']
        assert masks[p['id']] == masks[prototype['id']]
        for key in ['bodyModel', 'capModel', 'neckModel']:
            model = json.loads(built.read(ASSET + 'models/' + p[key] + '.json'))
            template = json.loads(old.read(ASSET + 'models/' + prototype[key] + '.json'))
            assert model['elements'] == template['elements']
            for location in model['textures'].values():
                assert location in sprites, location
                assert 'assets/' + location.replace(':', '/textures/') + '.png' in built.namelist()
        texture = Image.open(entry['texture']).convert('RGBA')
        liquid = Image.open(io.BytesIO(built.read('assets/' + p['liquid'][0]['texture'].replace(':', '/')))).convert('RGBA')
        assert liquid.crop((0, 0, 4, 4)).tobytes() == texture.crop((10, 10, 14, 14)).tobytes()
        assert liquid.crop((4, 0, 8, 4)).tobytes() == texture.crop((10, 14, 14, 18)).tobytes()
        body = json.loads(built.read(ASSET + 'models/' + p['bodyModel'] + '.json'))
        original = built.read('assets/' + body['textures']['particle'].replace(':', '/textures/') + '.png')
        assert original == Path(entry['texture']).read_bytes()

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get('failures')) == int(t.get('errors')) == int(t.get('skipped')) == 0 for t in tests)
count = sum(int(t.get('tests')) for t in tests)
assert count == 116
assert 'BUILD SUCCESSFUL' in (ROOT / 'preview/build-1.8.21.log').read_text(encoding='utf-8-sig')
assert all(f.stat().st_mtime <= JAR.stat().st_mtime + 2 for f in (ROOT / 'src/main').rglob('*') if f.is_file())
runs = {}
for shape, model, size in [('slim', 'slim', 4), ('wide', 'default', 3)]:
    path = ROOT / f'preview/smoke-jars-{shape}-1.8.21.log'
    log = path.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in log and f'CATALOG_SMOKE_COMPLETE {size}' in log
    assert 'CATALOG_SMOKE FAIL' not in log and 'player arm renderer model=' + model in log
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
    checks = [line.split('CATALOG_SMOKE PASS ', 1)[1] for line in log.splitlines() if 'CATALOG_SMOKE PASS ' in line]
    for entry in manifest['entries']:
        assert any('jar family models and original-texture liquid loaded ' + entry['id'] in line for line in checks)
    items = ['apple_jam_1', 'salmon_roe_sauce', 'egg_mayonnaise'] + (['dragon_egg_mayonnaise'] if shape == 'slim' else [])
    for item in items:
        selected = [line for line in checks if line.startswith('farmingtales:' + item + ' ')]
        required = ['drink consumes once', 'inspect preserves all three items', 'ordinary right click keeps vanilla animation',
                    'stow finishes with empty hand', 'two jam mouthful sounds without water gulps', 'four single-hand shake cues',
                    'jam eating uses only the main hand', 'free hand remains hidden after eating']
        if not item.endswith('_jam_1'):
            required += ['custom jar eating has no vanilla item crumbs', 'ordinary right click still produces vanilla eating particles']
        for text in required:
            assert any(text in line for line in selected), (shape, item, text)
    runs[shape] = dict(log=str(path), sha256=sha(path), assertions=checks)
assert 'enableShaders=false' in (ROOT / 'run-smoke/config/oculus.properties').read_text()
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)

folder = ROOT / 'blockbench/酱罐动画-v50'
bb = read(folder / '导出检查.json')
assert len(bb) == 258
assert {(b['item'], b['arm']) for b in bb} == {(e['id'], a) for e in manifest['entries'] for a in ['slim', 'wide']}
templates = {arm: read(ROOT / f'blockbench/上古水果果酱-v49/ancient_fruit_jam_1-{label}-v49.bbmodel')
             for arm, label in [('slim', '纤细手臂'), ('wide', 'Steve')]}
for entry in bb:
    data = read(Path(entry['path']))
    template = templates[entry['arm']]
    assert data['elements'] == template['elements'] and data['outliner'] == template['outliner']
    item = entry['item'].split(':')[1]
    assert len(data['animations']) == 10
    for actual, expected in zip(data['animations'], template['animations']):
        actual['name'] = actual['name'].replace(item, 'ancient_fruit_jam_1')
        assert actual == expected, (item, actual['name'])
        assert any(t.get('type') == 'camera' for t in actual['animators'].values())
    for texture in data['textures']:
        if texture['name'] == template['textures'][-1]['name']:
            assert texture == template['textures'][-1]
            continue
        embedded = Image.open(io.BytesIO(base64.b64decode(texture['source'].split(',')[1]))).convert('RGBA')
        expected = Image.open(ROOT / 'src/main/resources' / ASSET / 'textures/jam' / texture['name']).convert('RGBA')
        assert embedded.convert('RGB').tobytes() == expected.convert('RGB').tobytes()
fit = read(ROOT / 'preview/bb-fit-v50.json')
assert len(fit) == 2 and all(f['max_position_error_pixels'] <= .06001 and f['max_camera_error_degrees'] < .3 for f in fit)
archive = folder.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    assert len([n for n in z.namelist() if n.endswith('.bbmodel')]) == 258
    for path in folder.rglob('*'):
        if path.is_file():
            assert z.read(path.relative_to(folder).as_posix()) == path.read_bytes()
report = dict(version='1.8.21', sha256=sha(JAR), unit_tests=count, profiles=profiles, aged_aliases=baseline['aged_aliases'],
              jar_counts={'jam': 32, 'roe_sauce': 89, 'mayonnaise': 8}, blockbench=bb, blockbench_fit=fit,
              blockbench_archive=str(archive), blockbench_sha256=sha(archive), client_runs=runs, client_retested=True,
              soundVolume=.80, inspectionVolumeMultiplier=1.70, liquidGainMultiplier=.5, configuration_sha256=sha(config),
              scope='Extend the approved v49 jar animation to all 129 single jars (128 additions). Original per-item textures, '
                    'viscous liquid, flat lip, negative glass and arm masks. Existing animation classes/assets unchanged. '
                    'Local first-person hotkey eating suppresses vanilla crumbs; ordinary use retains them. '
                    '116 tests; both hidden isolated Forge arm clients without shaders; all 129 assets checked. '
                    'Full pack/user worlds not launched. 258 Blockbench v50 projects with animated cameras.')
(ROOT / 'verification.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('Verified 1.8.21: 129 jars, 116 tests, both arm clients and 258 Blockbench projects.')
