"""Validate the built client JAR and preserve a report alongside the source."""
import hashlib
import base64
import io
import json
import struct
import xml.etree.ElementTree as ET
import zipfile
import unittest
from pathlib import Path
from PIL import Image

root = Path(__file__).absolute().parent
from test_container_assets import ExteriorTests
from test_wine_surfaces import WineSurfaceTests
from verify_glass_release import verify as verify_glass, GLASS_BODIES
surface_tests=unittest.TextTestRunner().run(unittest.TestSuite([unittest.defaultTestLoader.loadTestsFromTestCase(ExteriorTests),unittest.defaultTestLoader.loadTestsFromTestCase(WineSurfaceTests)]))
assert surface_tests.wasSuccessful(), 'Exterior shell regression checks failed'
jar = root / 'build/libs/farmingtales_drink_anim-1.6.6.jar'
originals = json.loads((root / 'original-hashes.json').read_text(encoding='utf-8-sig'))
for original in originals:
    path=Path(original['Path'])
    if not path.exists() and path.suffix=='.jar' and Path(str(path)+'.disabled').exists():
        path=Path(str(path)+'.disabled')
    assert hashlib.sha256(path.read_bytes()).hexdigest().upper() == original['Hash'], str(path)
with zipfile.ZipFile(jar) as archive:
    names = archive.namelist()
    verify_glass(archive)
    assert 'version="1.6.6"' in archive.read('META-INF/mods.toml').decode('utf-8')
    assert not any('/smoke/' in name or 'SmokeFixture' in name or 'SmokeClient' in name for name in names)
    assert not any(name.startswith('assets/minecraft/textures/entity/player/') for name in names), 'Test skin must not ship'
    required = ['META-INF/mods.toml', 'farmingtales_drink_anim.mixins.json', 'farmingtales_drink_anim.refmap.json']
    prefix = 'assets/farmingtales_drink_anim/'
    required += [prefix + 'models/animation/' + model + '.json' for model in ('orange_bottle', 'orange_cork', 'orange_open_neck')]
    required += [prefix + 'sounds/' + sound + '.ogg' for sound in ('cork', 'cork_pull')]
    assert all(name in names for name in required)
    assert not any('BendableHands' in name for name in names), 'Keep complete vanilla arms'
    assert 'com/farmingtales/drinkanim/client/HandClipper.class' in names
    masks=json.loads(archive.read(prefix+'hand_volumes.json'))
    assert len(masks)==24 and all(masks.values())
    with zipfile.ZipFile(root/'backups/1.5.1-before-expressive-performances/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.5.1.jar') as prior:
        from container_assets import GLASS
        corrected_shells={prefix+'models/animation/drinks/'+item+'_body.json' for item in GLASS}
        opaque_beer_necks={prefix+'models/animation/drinks/'+item+'_neck.json' for item in ['light_beer','wheatbeer']}
        opaque_beer_textures={prefix+'textures/drinks/'+item+'_open.png' for item in ['light_beer','wheatbeer']}
        for name in prior.namelist():
            if name.startswith(prefix) and (name.endswith(('.ogg','.png')) or '/models/' in name or name.endswith(('drink_profiles.json','sounds.json'))):
                if name in corrected_shells|GLASS_BODIES|{prefix+'drink_profiles.json'}:
                    # Rebuild the allowed shell from protected original assets; texture/closure/liquid files remain exact.
                    assert archive.read(name)==(root/'src/main/resources'/name).read_bytes()
                elif name in opaque_beer_necks:
                    expected=json.loads(prior.read(name));expected['render_type']='minecraft:cutout'
                    assert json.loads(archive.read(name))==expected
                elif name in opaque_beer_textures:
                    before=Image.open(io.BytesIO(prior.read(name))).convert('RGBA');after=Image.open(io.BytesIO(archive.read(name))).convert('RGBA')
                    assert before.convert('RGB').tobytes()==after.convert('RGB').tobytes(), 'Preserve beer neck colors'
                    assert all(b==(255 if a==217 else a) for a,b in zip(before.getchannel('A').tobytes(),after.getchannel('A').tobytes()))
                else:assert archive.read(name)==prior.read(name), 'Keep the established bottle assets and embedded sound samples: '+name
        for name in ['ArmMotion','AnimationSoundTimeline','HandVolumes','ConvexClip']:
            path='com/farmingtales/drinkanim/math/'+name+'.class'
            assert archive.read(path)==prior.read(path), 'Preserve native arm geometry, established sounds and clipping math'
    with zipfile.ZipFile(root/'backups/1.5.1-before-expressive-performances/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.5.1.jar') as prior:
        for path in ['com/farmingtales/drinkanim/client/HandClipper.class', prefix+'hand_volumes.json']:
            assert archive.read(path)==prior.read(path), 'Keep the hand clipping fix: '+path
    events=json.loads(archive.read(prefix+'sounds.json'))
    sound_sources=json.loads(archive.read(prefix+'sound_sources.json'))['sources']
    assert len(sound_sources)==11
    assert sorted(n for n in names if n.startswith(prefix+'sounds/') and n.endswith('.ogg'))==sorted(prefix+s['packaged_path'] for s in sound_sources)
    for item in sound_sources:
        assert hashlib.sha256(archive.read(prefix+item['packaged_path'])).hexdigest()==item['sha256']
        origin=item['source']
        for part in origin.get('components',[]):
            assert hashlib.sha256(Path(part['file']).read_bytes()).hexdigest()==part['sha256']
            assert part['entry'].startswith('minecraft/sounds/')
        if 'container_sha256' in origin:
            assert hashlib.sha256(Path(origin['file']).read_bytes()).hexdigest()==origin['container_sha256'], 'Source mod changed'
            with zipfile.ZipFile(origin['file']) as source_archive: source_bytes=source_archive.read(origin['entry'])
        else: source_bytes=Path(origin['file']).read_bytes()
        assert hashlib.sha256(source_bytes).hexdigest()==item['source_sha256']
        if item['processing']=='byte-for-byte copy': assert item['source_sha256']==item['sha256']
        assert item['channels']==1
    for event,spec in events.items():
        for sound in spec['sounds']:
            reference=sound if isinstance(sound,str) else sound['name']
            assert reference.startswith('farmingtales_drink_anim:'), 'All sound files must be embedded in this mod'
            assert prefix+'sounds/'+reference.split(':',1)[1]+'.ogg' in names
            if event in ('cork','cork_pull'):
                legacy=root/'backups/1.3.6-before-elbow-audio-and-continuity/src/main/resources/assets/farmingtales_drink_anim/sounds'/(event+'.ogg')
                assert archive.read(prefix+'sounds/'+event+'.ogg')==legacy.read_bytes()
    assert events['gulp']['sounds'][0]['name']=='farmingtales_drink_anim:vanilla/drink'
    assert 'swallow' not in events, 'No extra completion gulp'
    assert not any(n.startswith('assets/minecraft/') and 'sound' in n for n in names), 'Do not replace vanilla sound resources'

    for name in names:
        if name.endswith('.json'):
            json.loads(archive.read(name))
        if name.endswith('.class'):
            assert struct.unpack('>H', archive.read(name)[6:8])[0] == 61, name
    refmap = json.loads(archive.read('farmingtales_drink_anim.refmap.json'))
    assert not any('DrinkUseSoundMixin' in name for name in names), 'Conflicting redirect must not ship'
    assert 'DrinkUseSoundMixin' not in json.loads(archive.read('farmingtales_drink_anim.mixins.json'))['client']
    assert refmap['mappings'], 'Mixin release mapping missing'
    assert any('DrinkMovementMixin' in key for key in refmap['mappings']), 'Movement mixin must be remapped for release'
    original_neck=json.loads(Path(originals[1]['Path']).read_text(encoding='utf-8-sig'))['elements'][1]
    rendered_neck=json.loads(archive.read(prefix+'models/animation/orange_open_neck.json'))['elements']
    assert rendered_neck==[original_neck], 'Only recolor the original neck; keep its exact geometry and UVs'
    assert prefix+'textures/orange_wine_open.png' in names
    mouth_texture = archive.read(prefix+'textures/orange_wine_open.png')
    pixels = Image.open(io.BytesIO(mouth_texture)).convert('RGBA')
    old_pixels = Image.open(root/'backups/1.3.1-before-slim-neck-fix/src/main/resources/assets/farmingtales_drink_anim/textures/orange_wine_open.png').convert('RGBA')
    assert pixels.convert('RGB').tobytes() == old_pixels.convert('RGB').tobytes(), 'Keep existing neck colors'
    for x0,y0,x1,y1 in [(11,3,13,8),(12,8,14,10)]:
        assert all(pixels.getpixel((x,y))[3] in (0,217) for x in range(x0,x1) for y in range(y0,y1))
    assert 217 in pixels.getchannel('A').tobytes()
    assert json.loads(archive.read(prefix+'models/animation/orange_open_neck.json'))['render_type']=='minecraft:translucent'
from verify_hollow_release import run
run()
