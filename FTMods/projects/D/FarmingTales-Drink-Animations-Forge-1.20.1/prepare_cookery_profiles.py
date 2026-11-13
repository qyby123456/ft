"""Senluo Cookery's handheld sticky rice cake.

The source mod ships no held geometry for `sticky_rice_cake`: the item is a flat
16x16 sprite. The only three-dimensional ciba in the JAR lives on the sharing
plate, so one piece of `sticky_rice_cake_plate_5` is lifted out of that sheet and
rebuilt as a small box whose top face reads the plate's amber oval. Four stages
shrink the box away from the bitten corner and crop the oval in step, so the cake
loses a bite without adding art.

The plate's oval is a rounded shape on a transparent sheet, so the corners of its
bounding box are empty. `cutout` would punch those four corners straight through
the cake, so the generated texture keeps every source pixel and only completes the
unpainted texels inside the oval with their nearest painted neighbour - the same
repair `liquid_textures.py` performs for liquid sheets. Every face rectangle is
asserted fully opaque before the script writes anything.

The source JAR is never modified.
"""
from pathlib import Path
import hashlib
import json
import struct
import zipfile
import zlib

ROOT = Path(__file__).absolute().parent
RESOURCES = ROOT / 'src/main/resources'
ASSETS = RESOURCES / 'assets/farmingtales_drink_anim'
FIXTURE = ROOT / 'src/smoke/resources/assets'
JAR = ROOT / 'compat-libs/kaleidoscope-cookery-1.4.1.jar'

ITEM = 'kaleidoscope_cookery:sticky_rice_cake'
SOURCE_TEXTURE = 'assets/kaleidoscope_cookery/textures/block/plate/sticky_rice_cake_plate.png'
# Model texture references, the atlas source and the sprite name are one and the same:
# `<ns>:<path under textures/>` with no `textures/` prefix and no `.png`. The liquid
# region tables in `drink_profiles.json` use the other spelling; do not mix the two.
SPRITE = 'farmingtales_drink_anim:snacks/sticky_rice_cake'
TEXTURE_FILE = 'textures/snacks/sticky_rice_cake.png'
FOLDER = 'snacks/sticky_rice_cake'
STAGES = ('whole', 'bite1', 'bite2', 'bite3')

# Measured from the 32x32 plate sheet; UV space is 0..16 regardless of sheet size.
OVAL = [1.0, 9.0, 8.0, 15.0]        # the amber top face of one ciba (14 x 12 px)
RIM = [8.5, 1.5, 10.0, 5.5]         # one amber rim strip (3 x 8 px)
WHOLE = [4.0, 3.0, 4.0, 12.0, 6.0, 12.0]   # model pixels, centred on the (8, 4.8, 8) pivot
BITES = (0.0, 0.65, 1.45, 2.45)     # bite depth removed from the near corner


def READ(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))


def dump(path, data):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def dump_like(path, data):
    """Rewrite a shared table in whatever layout it already uses, so the diff stays one entry."""
    raw = Path(path).read_text(encoding='utf-8-sig')
    indent = 2 if raw.startswith('{\n') or raw.startswith('[\n') else None
    separators = None if indent else (',', ':')
    Path(path).write_text(json.dumps(data, ensure_ascii=False, indent=indent, separators=separators) + '\n',
                          encoding='utf-8')


def read_png(path):
    """RGBA8 PNG -> (width, height, bytearray). No external image library."""
    raw = Path(path).read_bytes()
    assert raw[:8] == b'\x89PNG\r\n\x1a\n', path
    offset, header, idat = 8, None, bytearray()
    while offset < len(raw):
        length = struct.unpack('>I', raw[offset:offset + 4])[0]
        kind = raw[offset + 4:offset + 8]
        body = raw[offset + 8:offset + 8 + length]
        offset += 12 + length
        if kind == b'IHDR':
            header = struct.unpack('>IIBB', body[:10])
        elif kind == b'IDAT':
            idat += body
    width, height, depth, colour = header
    assert depth == 8 and colour == 6, (depth, colour)
    data = zlib.decompress(bytes(idat))
    stride = width * 4
    pixels = bytearray(stride * height)
    cursor = 0
    for y in range(height):
        kind = data[cursor]
        cursor += 1
        line = bytearray(data[cursor:cursor + stride])
        cursor += stride
        base = y * stride
        prior = pixels[base - stride:base] if y else bytearray(stride)
        for x in range(stride):
            left = line[x - 4] if x >= 4 else 0
            up = prior[x]
            corner = prior[x - 4] if x >= 4 else 0
            if kind == 1:
                line[x] = (line[x] + left) & 255
            elif kind == 2:
                line[x] = (line[x] + up) & 255
            elif kind == 3:
                line[x] = (line[x] + ((left + up) >> 1)) & 255
            elif kind == 4:
                estimate = left + up - corner
                da, db, dc = abs(estimate - left), abs(estimate - up), abs(estimate - corner)
                near = left if da <= db and da <= dc else up if db <= dc else corner
                line[x] = (line[x] + near) & 255
        pixels[base:base + stride] = line
    return width, height, pixels


def write_png(path, width, height, pixels):
    stride = width * 4
    body = bytearray()
    for y in range(height):
        body.append(0)
        body += pixels[y * stride:(y + 1) * stride]

    def chunk(kind, payload):
        return (struct.pack('>I', len(payload)) + kind + payload
                + struct.pack('>I', zlib.crc32(kind + payload) & 0xffffffff))

    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_bytes(b'\x89PNG\r\n\x1a\n'
                           + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
                           + chunk(b'IDAT', zlib.compress(bytes(body), 9))
                           + chunk(b'IEND', b''))


def rect_pixels(rect, width, height):
    """UV rectangle (0..16) -> inclusive integer pixel box."""
    u0, v0, u1, v1 = rect
    return (max(0, int(u0 / 16 * width)), max(0, int(v0 / 16 * height)),
            min(width - 1, int(u1 / 16 * width) - 1), min(height - 1, int(v1 / 16 * height) - 1))


def complete_alpha(pixels, width, height, box):
    """Fill unpainted texels from their nearest painted neighbour inside `box`."""
    x0, y0, x1, y1 = box
    painted = [(x, y) for y in range(y0, y1 + 1) for x in range(x0, x1 + 1)
               if pixels[(y * width + x) * 4 + 3] == 255]
    assert painted, box
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            at = (y * width + x) * 4
            if pixels[at + 3] == 255:
                continue
            source = min(painted, key=lambda p: (p[0] - x) ** 2 + (p[1] - y) ** 2)
            from_at = (source[1] * width + source[0]) * 4
            pixels[at:at + 3] = pixels[from_at:from_at + 3]
            pixels[at + 3] = 255


def assert_opaque(pixels, width, height, rect, label):
    x0, y0, x1, y1 = rect_pixels(rect, width, height)
    holes = [(x, y) for y in range(y0, y1 + 1) for x in range(x0, x1 + 1)
             if pixels[(y * width + x) * 4 + 3] != 255]
    assert not holes, f'{label} samples transparent texels: {holes[:8]}'


def rim(width, height):
    """Map a face of `width` x `height` pixels onto the rim strip, never tiling."""
    u0, v0, u1, v1 = RIM
    return [u0, v0, u0 + (u1 - u0) * min(1.0, width / 8.0), v0 + (v1 - v0) * min(1.0, height / 3.0)]


def model(face_rects, inset):
    """One bite stage: the box shrinks toward the far corner, the oval crops with it."""
    x0, y0, z0, x1, y1, z1 = WHOLE
    span_x, span_z, height = x1 - x0, z1 - z0, y1 - y0
    width, depth = span_x - inset, span_z - inset
    u0, v0, u1, v1 = OVAL
    top = [u0 + (u1 - u0) * inset / span_x, v0 + (v1 - v0) * inset / span_z, u1, v1]
    side_x, side_z = rim(width, height), rim(depth, height)
    origin = [x0 + inset, y0, z0 + inset]
    faces = {
        'north': {'uv': side_x, 'texture': '#0'},
        'south': {'uv': side_x, 'texture': '#0'},
        'east': {'uv': side_z, 'texture': '#0'},
        'west': {'uv': side_z, 'texture': '#0'},
        'up': {'uv': top, 'texture': '#0'},
        'down': {'uv': top, 'texture': '#0'},
    }
    face_rects.setdefault('rims', set()).update((tuple(side_x), tuple(side_z)))
    face_rects['top'] = top
    return {
        'render_type': 'minecraft:cutout',
        'textures': {'0': SPRITE, 'particle': SPRITE},
        'elements': [{
            'from': origin,
            'to': [x1, y1, z1],
            'rotation': {'angle': 0, 'axis': 'y', 'origin': origin},
            'faces': faces,
        }],
    }


def main():
    with zipfile.ZipFile(JAR) as archive:
        raw = archive.read(SOURCE_TEXTURE)

    scratch = ROOT / 'build/cookery/sticky_rice_cake_plate.png'
    scratch.parent.mkdir(parents=True, exist_ok=True)
    scratch.write_bytes(raw)
    width, height, pixels = read_png(scratch)

    # Only the oval's own bounding box is completed; every other source pixel is untouched.
    complete_alpha(pixels, width, height, rect_pixels(OVAL, width, height))
    texture_path = ASSETS / TEXTURE_FILE
    write_png(texture_path, width, height, pixels)

    face_rects = {}
    for name, inset in zip(STAGES, BITES):
        dump(ASSETS / f'models/animation/{FOLDER}/{name}.json', model(face_rects, inset))

    # Every authored face must survive `cutout`; a transparent texel would be a hole.
    width, height, pixels = read_png(texture_path)
    for label, rects in (('rim strip', face_rects['rims']), ('ciba oval', {tuple(face_rects['top'])})):
        for rect in rects:
            assert_opaque(pixels, width, height, rect, label)

    # The upstream inventory sprite is deliberately left alone: no item model is written, so the
    # original `item/generated` icon stays in the inventory, the GUI and on the ground. The
    # first-person view does not read the item model at all - BottleRenderer cancels the vanilla
    # hand pass and draws these stage models itself.

    profile = dict(
        id=ITEM, style='FOOD', scale=1.0, width=8.0, depth=8.0,
        capPivot=[8.0, 6.0, 8.0], neckJoin=0.0, neckMin=0.0, neckMax=0.0, mouthY=6.0,
        bodyModel='animation/' + FOLDER + '/whole',
        capModel='animation/cups/empty', neckModel='animation/cups/empty',
        boundsMin=[4.0, 3.0, 4.0], boundsMax=[12.0, 6.0, 12.0],
        liquid=[], canLid=None, cupShape=None)
    profiles = [p for p in READ(ASSETS / 'drink_profiles.json') if p['id'] != ITEM]
    profiles.append(profile)
    dump(ASSETS / 'drink_profiles.json', profiles)

    # Every profile must carry a hand mask: `HandVolumesTest` asserts it, and the palm would
    # otherwise show through the cake. The cake's own volume is the occluder.
    masks = READ(ASSETS / 'hand_volumes.json')
    low, high = WHOLE[:3], WHOLE[3:]
    masks[ITEM] = [dict(min=list(low), max=list(high), origin=list(low),
                        axis='y', angle=0, rescale=False, shiftY=0)]
    dump_like(ASSETS / 'hand_volumes.json', masks)

    atlas_path = RESOURCES / 'assets/minecraft/atlases/blocks.json'
    atlas = READ(atlas_path)
    if SPRITE not in {s.get('resource') for s in atlas['sources']}:
        atlas['sources'].append(dict(type='minecraft:single', resource=SPRITE))
    dump(atlas_path, atlas)

    # Same-ID fixture so the isolated smoke client can hold the item.
    dump(FIXTURE / 'kaleidoscope_cookery/blockstates/sticky_rice_cake.json',
         {'variants': {'': {'model': 'farmingtales_drink_anim:animation/' + FOLDER + '/whole'}}})
    dump(FIXTURE / 'kaleidoscope_cookery/models/item/sticky_rice_cake.json',
         {'parent': 'farmingtales_drink_anim:animation/' + FOLDER + '/whole'})

    dump(ROOT / 'cookery-sources.json', dict(
        jars=[dict(path=str(JAR), sha256=hashlib.sha256(JAR.read_bytes()).hexdigest())],
        items=[dict(id=ITEM, source=SOURCE_TEXTURE, sha256=hashlib.sha256(raw).hexdigest(),
                    texture=SPRITE, stages=list(STAGES), bites=list(BITES))]))
    print(f'Prepared {ITEM}: {len(STAGES)} stages, {len(profiles)} profiles.')


if __name__ == '__main__':
    main()
