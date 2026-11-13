"""Verify the bitten chocolate shape and preserve all installed animation code."""
from pathlib import Path
import hashlib
import io
from PIL import Image
import json
import re
import math
import itertools
import tomllib
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
BACKUP = ROOT / 'backups/1.8.69-before-chocolate-bite-shape'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.70.jar'
MATH = 'com/farmingtales/drinkanim/math/'


def native(path):
    return Path('\\\\?\\' + str(path.resolve()))


def sha(path):
    return hashlib.sha256(native(path).read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


baseline = read(BACKUP / 'baseline.json')
previous = BACKUP / Path(baseline['installed_path']).name
assert sha(previous) == baseline['previous_sha256']
with zipfile.ZipFile(native(previous)) as old, zipfile.ZipFile(JAR) as new:
    added = set(new.namelist()) - set(old.namelist())
    assert not added, added
    assert not set(old.namelist()) - set(new.namelist())
    changed = {name for name in old.namelist() if old.read(name) != new.read(name)}
    assert changed == {'META-INF/mods.toml','assets/farmingtales_drink_anim/models/animation/papercraft/diplomatico_coffee_serving_bite1.json'}, changed
    bite_name='assets/farmingtales_drink_anim/models/animation/papercraft/diplomatico_coffee_serving_bite1.json'
    before=json.loads(old.read(bite_name));after=json.loads(new.read(bite_name))
    assert after == dict(before,elements=[e for e in before['elements'] if e['name']!='original_7'])
    all_classes=[name for name in old.namelist() if name.endswith('.class')]
    assert all(old.read(name)==new.read(name) for name in all_classes)
    def slab_bounds(elements):
        values=[]
        for e in elements:
            rotation=e['rotation']
            assert rotation['axis']=='y' and rotation['origin']==[1,0,-2]
            a=math.radians(rotation['angle']-22.5)
            for x,z in itertools.product([e['from'][0],e['to'][0]],[e['from'][2],e['to'][2]]):
                values.append(1+(x-1)*math.cos(a)+(z+2)*math.sin(a))
        return [min(values),max(values)]
    assert slab_bounds(before['elements'])[0]<-4
    assert slab_bounds(after['elements'])==[0,4]
    core=next(e for e in after['elements'] if e['name']=='chocolate_bite_interior')
    assert set(core['faces'])=={'north','east','south','west','up','down'}
    atlas=Image.open(io.BytesIO(new.read('assets/farmingtales_drink_anim/textures/papercraft/diplomatico_coffee_0.png'))).convert('RGBA')
    for face in core['faces'].values():
        u,v,w,q=face['uv']
        rect=(int(min(u,w)*atlas.width/16),int(min(v,q)*atlas.height/16),math.ceil(max(u,w)*atlas.width/16),math.ceil(max(v,q)*atlas.height/16))
        assert atlas.crop(rect).getchannel('A').getextrema()[0]>=250
    scope=dict(animation_class_files_unchanged=len(all_classes),only_bitten_model_changed=True,
               retained_slab_x=slab_bounds(after['elements']),opaque_bite_faces=6)
    old_meta = tomllib.loads(baseline['user_metadata'])
    new_meta = tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert new_meta['mods'][0]['version'] == '1.8.70'
    new_meta['mods'][0]['version'] = old_meta['mods'][0]['version']
    assert new_meta == old_meta
    for name, digest in baseline['preserve_hashes'].items():
        assert hashlib.sha256(new.read(name)).hexdigest() == digest, name
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == native(path).read_bytes(), path

# All production resources, including the accepted blue popsicle texture, are preserved.
tests = [ET.parse(path).getroot() for path in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(k)) == 0 for test in tests for k in ('errors','failures','skipped'))
count = sum(int(test.get('tests')) for test in tests)
assert count == 194, count
client_logs = []
for variant,username,names in (
    ('slim','SlimSteve29',('diplomatico_coffee',)),):
    logpath = ROOT / f'preview/verify-chocolate-{variant}-1.8.70.log'
    raw = logpath.read_bytes()
    log = raw.decode('utf-16' if raw.startswith((b'\xff\xfe',b'\xfe\xff')) else 'utf-8-sig',errors='replace')
    assert 'BUILD SUCCESSFUL' in log and f'CATALOG_SMOKE_COMPLETE {len(names)}' in log
    assert not re.search(r'(?:AGED|DRINK|CATALOG)_SMOKE FAIL',log)
    for name in names:
        for label in ('body neck and decoration textures are present','inspect preserves all three items',
                      'drink consumes once and returns to idle','stow finishes with empty hand'):
            assert 'CATALOG_SMOKE PASS papercraft_magic_decoration:' + name + ' ' + label in log, (name,label)
    captures = []
    for phase in ('idle','inspect','drink','replacement','garnish-meal-8','garnish-meal-16',
                  'garnish-meal-26','garnish-meal-42','garnish-meal-58','garnish-meal-64',
                  'garnish-meal-72','garnish-meal-84','garnish-meal-96',
                  'chocolate-inspect-46','chocolate-inspect-58','chocolate-inspect-86','chocolate-inspect-108','chocolate-inspect-156'):
        path = ROOT / f'run-smoke/screenshots/v184-catalog-{username}-diplomatico_coffee-{phase}.png'
        assert path.stat().st_mtime > JAR.stat().st_mtime, path
        captures.append(str(path))
    audio = read(ROOT / f'run-smoke/audio-balance-{username}.json')
    for event,expected in (('chocolate_bite',2),('snack_swallow',2)):
        cues = [a for a in audio if a['item']=='papercraft_magic_decoration:diplomatico_coffee' and a['event']==event]
        assert len(cues) == expected and all(a['volume'] > 0 for a in cues), event
    frame_report = read(ROOT / f'preview/chocolate-frame-report-{variant}-1.8.70.json')
    assert all(v['clock_monotonic'] and v['rendered_frames'] > 100 and v['subtick_advances'] > 50 for v in frame_report.values())
    assert frame_report['INSPECT']['bite_bursts']==0 and frame_report['DRINK']['bite_bursts']==2
    assert frame_report['DRINK']['max_crumbs']==8 and frame_report['DRINK']['crumb_frames']>20
    client_logs.append(dict(path=str(logpath),sha256=sha(logpath),profiles=len(names),captures=captures,frame_report=frame_report))
config = PACK / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256'] == sha(BACKUP / config.name)
report = dict(version='1.8.70',sha256=sha(JAR),previous_sha256=sha(previous),
              configuration_sha256=sha(config),unit_tests=count,changed_jar_entries=sorted(changed),
              added_jar_entries=sorted(added),
              motion_comparison=scope,client_logs=client_logs,
              scope='Only the post-first-bite chocolate model changes: remove the rotated detail that extended into the eaten half as a long protruding strip. Preserve every surviving face, its source UV and the opaque six-face chocolate interior. Transformed geometry stays inside the retained half. All Java classes, intact chocolate model, eating and inspection animations, palm inset, bite particles, other drink assets, metadata, tooltip, logo and configuration match 1.8.69. All 194 existing tests and the isolated Forge client replay pass; two crumb bursts remain visible. Full modpack shader replay was not performed.')
(ROOT / 'verification-1.8.70.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.70: {count} tests, {scope["animation_class_files_unchanged"]} unchanged animation class files, client replay passes; SHA256 {sha(JAR)}')
