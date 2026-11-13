"""Checks specific to the multi-container release; invoked by verify_release.py."""
import base64
import hashlib
import json
import struct
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).absolute().parent
VERSION = '1.4.0'
PREFIX = 'assets/farmingtales_drink_anim/'
EXPECTED = set('orange_wine mysticfruit_wine sweet_grape_wine pumpkin_wine ancient_fruit_wine sweet_berry_wine watermelon_wine sakura_wine wheatbeer cherry_liqueur light_beer red_apple_wine starfruit_wine blueberry_wine pomegranate_wine strawberry_wine rum coconut_wine brandy_xo mozhao_p mozhao purified_water vitamin_drink sweet_orange_juice'.split())
BEVERAGES = dict(mozhao='CAN', mozhao_p='PINK_CAN', purified_water='WATER', vitamin_drink='SPORTS', sweet_orange_juice='JUICE')

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def run():
    jar = ROOT / f'build/libs/farmingtales_drink_anim-{VERSION}.jar'
    with zipfile.ZipFile(jar) as archive:
        names = set(archive.namelist())
        profiles = json.loads(archive.read(PREFIX + 'drink_profiles.json'))
        assert len(profiles) == 24
        assert {p['id'].split(':')[1] for p in profiles} == EXPECTED
        assert len([n for n in names if n.startswith(PREFIX + 'models/animation/drinks/') and n.endswith('.json')]) == 69
        atlas = json.loads(archive.read('assets/minecraft/atlases/blocks.json'))['sources']
        sprites = {s['resource'] for s in atlas}
        for p in profiles:
            item = p['id'].split(':')[1]
            assert p['style'] == BEVERAGES.get(item, 'WINE')
            for field in ('bodyModel', 'capModel', 'neckModel'):
                model = json.loads(archive.read(PREFIX + 'models/' + p[field] + '.json'))
                for texture in model['textures'].values():
                    assert texture in sprites, texture
                    assert PREFIX + 'textures/' + texture.split(':')[1] + '.png' in names, texture
            for region in p['liquid']:
                assert region['texture'].startswith('farmingtales_drink_anim:textures/') and region['texture'].endswith('.png')
                assert 'assets/' + region['texture'].replace(':', '/') in names
            assert p['scale'] > 0 and p['width'] > 0
        events = json.loads(archive.read(PREFIX + 'sounds.json'))
        with zipfile.ZipFile(ROOT / 'backups/1.3.11-before-multiple-drinks/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.3.11.jar') as previous:
            old_events = json.loads(previous.read(PREFIX + 'sounds.json'))
            for event, spec in old_events.items():
                assert events[event] == spec, 'Keep existing sound gains: ' + event
            for name in previous.namelist():
                if name.startswith(PREFIX + 'models/animation/orange_') or name in (PREFIX + 'textures/orange_wine.png', PREFIX + 'textures/orange_wine_open.png'):
                    assert archive.read(name) == previous.read(name), 'Keep orange geometry and texture'

    sources = json.loads((ROOT / 'drink-profile-sources.json').read_text(encoding='utf-8-sig'))
    for source in sources:
        assert digest(source['path']) == source['sha256'], source['path']
    dependencies = json.loads((ROOT / 'compat-libs/grilling-test-hashes.json').read_text(encoding='utf-8-sig'))
    for dependency in dependencies:
        assert digest(dependency['source']) == dependency['sha256']
        assert digest(dependency['test_copy']) == dependency['sha256']
    tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
    assert tests and all(t.attrib['failures'] == '0' and t.attrib['errors'] == '0' for t in tests)
    assert any(t.attrib['name'].endswith('DrinkProfilesTest') for t in tests)

    smoke = {}
    for arm, user in [('slim', 'SlimSteve29'), ('wide', 'WideSteve10')]:
        lines = (ROOT / f'run-smoke/smoke-results-{user}.txt').read_text(encoding='utf-8-sig').splitlines()
        assert len(lines) == 30 and all(line.startswith('PASS ') for line in lines), lines
        log = (ROOT / f'preview/smoke-{arm}-{VERSION}.log').read_text(encoding='utf-8-sig')
        assert 'DRINK_SMOKE_COMPLETE' in log and 'BUILD SUCCESSFUL' in log
        assert 'Using shaderpack' in log and 'Redirect conflict' not in log
        assert 'FileNotFoundException: farmingtales_drink_anim:' not in log
        assert 'Unable to load model: \'farmingtales_drink_anim:' not in log
        assert 'CATALOG_SMOKE FAIL' not in log
        smoke[arm] = lines
    catalog = (ROOT / 'run-smoke/catalog-all-SlimSteve29.txt').read_text(encoding='utf-8-sig').splitlines()
    assert len(catalog) == 24 * 9 and all(line.startswith('PASS ') for line in catalog), catalog
    assert {line.split()[1].split(':')[1] for line in catalog} == EXPECTED
    assert 'CATALOG_SMOKE_COMPLETE 24' in (ROOT / f'preview/smoke-slim-{VERSION}.log').read_text(encoding='utf-8-sig')
    followups = {}
    for label, user, expected, logfile in [
        ('shaped_necks_and_xo_framing', 'WideSteve10', {'rum', 'coconut_wine', 'brandy_xo'}, 'smoke-wide-1.4.0.log'),
        ('can_helper_hand', 'SlimSteve29', {'mozhao', 'mozhao_p'}, 'smoke-cans-1.4.0.log')]:
        lines = (ROOT / f'run-smoke/catalog-results-{user}.txt').read_text(encoding='utf-8-sig').splitlines()
        log = (ROOT / 'preview' / logfile).read_text(encoding='utf-8-sig')
        assert len(lines) == len(expected) * 9 and all(line.startswith('PASS ') for line in lines)
        assert {line.split()[1].split(':')[1] for line in lines} == expected
        assert f'CATALOG_SMOKE_COMPLETE {len(expected)}' in log and 'BUILD SUCCESSFUL' in log
        assert 'FileNotFoundException: farmingtales_drink_anim:' not in log and 'CATALOG_SMOKE FAIL' not in log
        followups[label] = lines

    projects = []
    bbdir = ROOT / 'blockbench/酒水扩展-v15'
    for profile in profiles:
        item = profile['id'].split(':')[1]
        runtime = json.loads((ROOT / f'build/blockbench/drinks/{item}.json').read_text(encoding='utf-8-sig'))
        for arm, label in [('slim', '纤细手臂'), ('wide', 'Steve')]:
            path = bbdir / f'{item}-{label}-v15.bbmodel'
            bb = json.loads(path.read_text(encoding='utf-8'))
            assert len(bb['animations']) == 9
            assert {a['name'].split('.')[-1] for a in bb['animations']} == {'idle', 'draw', 'put_away', 'inspect', 'drink', 'walk', 'sprint', 'jump', 'preview_complete'}
            camera = next(e for e in bb['elements'] if e.get('type') == 'camera')
            assert camera['position'] == [0, 16, 15.5]
            for texture in bb['textures']:
                assert base64.b64decode(texture['source'].split(',')[1]).startswith(b'\x89PNG')
            for arm_cube in [e for e in bb['elements'] if e['name'].endswith('_arm_skin')]:
                assert abs((arm_cube['to'][0] - arm_cube['from'][0]) / .7 - (3 if arm == 'slim' else 4)) < 1e-5
                assert abs((arm_cube['to'][1] - arm_cube['from'][1]) / .7 - 12) < 1e-5
            for action in ('draw', 'put_away', 'inspect', 'drink'):
                frames = runtime['clips'][arm + '/' + action]['frames']
                assert any(sum(v * v for v in f['camera'][:3]) > .000001 for f in frames)
                animation = next(a for a in bb['animations'] if a['name'].endswith('.' + action))
                assert camera['uuid'] in animation['animators']
            projects.append(dict(item=profile['id'], arm=arm, path=str(path), sha256=digest(path)))
    assert len(list(bbdir.glob('*.bbmodel'))) == len(projects) == 48
    report = dict(version=VERSION, jar=str(jar), sha256=digest(jar), profiles=profiles,
        unit_tests=sum(int(t.attrib['tests']) for t in tests), smoke_assertions=smoke,
        catalog_assertions=catalog, followup_assertions=followups, blockbench=projects, source_resources_unchanged=len(sources),
        compatibility_dependencies_unchanged=dependencies,
        audio=dict(sips=4, inspection_cloth_gain_relative_to_1_3_10=.15, embedded_only=True),
        scope='Forge 47.4.10 development client with same-ID fixture items, Grilling 1.1.1, Cookery 1.4.1, ImmersiveEating 1.4.3, GeckoLib 4.8.4, Embeddium 0.3.31, Mekalus 1.8.0.1 and Complementary/Euphoria shaders. All 24 catalog entries tested with slim arms; base interaction suite tested with both arm shapes. After visual refinements, three shaped bottles retested with wide arms and two cans retested with slim arms. Full modpack not tested.')
    (ROOT / 'verification.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(f"Verified {VERSION}: {len(profiles)} drinks, {len(projects)} Blockbench files, {report['unit_tests']} unit tests, {len(catalog)} catalog assertions, 60 base client assertions. SHA256 {report['sha256']}")
