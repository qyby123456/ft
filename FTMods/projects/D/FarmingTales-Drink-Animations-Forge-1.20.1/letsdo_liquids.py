"""Separate painted contents from source glass and labels in the requested bottles."""
from copy import deepcopy
from PIL import Image
from container_assets import bounds
from liquid_textures import patch, liquid_atlas

# Source solids making up the connected cavity; decorative collars are excluded.
VOLUMES = {
    'eiswein': [1, 3], 'strad_wine': [1, 2], 'mellohi_wine': [1, 3],
    'magnetic_wine': [1, 2], 'whiskey_highland_hearth': [0],
    'chenet_wine': [1, 2], 'stal_wine': [1, 2], 'kelp_cider': [1],
    'solaris_wine': [1], 'creepers_crush': [3, 1, 4, 2],
    'aegis_wine': [0, 4], 'chorus_wine': [2, 0],
    'whiskey_jojannik': [1, 2], 'whiskey_jamesons_malt': [0, 1],
    'whiskey_smokey_reverie': [1, 2], 'glowing_wine': [0, 2],
    'jellie_wine': [0, 1], 'apple_cider': [2, 3, 4],
}
SHELLS = {name: set(indices) for name, indices in VOLUMES.items()}
SHELLS['eiswein'].add(2)
SHELLS['mellohi_wine'].add(2)

# Highland Hearth's fixed shell is blue glass, including its near-white blue highlight.
HIGHLAND_GLASS = {(93,143,194), (179,207,236), (57,92,121), (249,253,253)}

def is_contents(name, color):
    r, g, b, a = color
    if a < 128:
        return False
    if name in ('eiswein', 'strad_wine'):
        return b > r * 1.25 and g > r * 1.2 or name == 'eiswein' and min(r, g, b) > 230
    if name == 'mellohi_wine':
        return r > 180 and g > 190 and b > 110
    if name == 'magnetic_wine':
        return r > g * 2 and b > g * 1.1 and r != 132
    if name == 'chenet_wine':
        return r > g * 2.5 and b > g * 1.2
    if name == 'stal_wine':
        return r > 90 and r > g * 2.5 and abs(g - b) < 8
    if name == 'creepers_crush':
        return g > 135 and b < g * .85
    if name == 'solaris_wine':
        return r > 180 and g > 165 and b < g * .9 or g > 125 and r < 100
    if name == 'kelp_cider':
        return g > r * 1.8 and g > b * 1.15 or g > 170 and b > 170
    if name == 'chorus_wine':
        return b > r * 1.3 and b > g * 2 or r > 150 and b > 150 and g < 100
    if name == 'jellie_wine':
        return r > g * 1.8 and b > g * 1.3 and b > r * .6
    if name == 'glowing_wine':
        return r > 190 and g > 150 and r > b * 1.3
    if name == 'whiskey_highland_hearth':
        return (r, g, b) not in HIGHLAND_GLASS
    if name == 'whiskey_jamesons_malt':
        return (r, g, b) in {
            (252, 188, 76), (254, 228, 141), (239, 133, 62), (232, 137, 43),
            (195, 76, 30), (255, 241, 206), (119, 57, 36), (250, 157, 0),
        }
    if name in ('aegis_wine', 'whiskey_jojannik'):
        return (r, g, b) in {
            (162, 59, 34), (187, 112, 72), (181, 71, 59), (212, 155, 90),
            (238, 214, 132), (193, 76, 63), (173, 63, 36), (200, 119, 77),
            (254, 228, 141), (226, 165, 96),
        }
    if name == 'whiskey_smokey_reverie':
        return r > 100 and r > g * 1.4 and g > b * 2
    return False

def connected_regions(elements, indices, neck, mouth):
    boxes = [bounds(elements[i]) for i in indices]
    lo, hi = bounds(elements[neck])
    if lo[1] < mouth:
        hi[1] = mouth
        boxes.append((lo, hi))
    # Partition the union into Y bands rather than overlapping full boxes.
    levels = sorted({v[1] for box in boxes for v in box})
    result = []
    for y0, y1 in zip(levels, levels[1:]):
        active = [(a, b) for a, b in boxes if a[1] <= (y0 + y1) / 2 <= b[1]]
        if not active:
            continue
        lo = [min(a[0] for a, b in active) + .18, y0, min(a[2] for a, b in active) + .18]
        hi = [max(b[0] for a, b in active) - .18, y1, max(b[2] for a, b in active) - .18]
        if result and result[-1][0][::2] == lo[::2] and result[-1][1][::2] == hi[::2]:
            result[-1][1][1] = y1
        else:
            result.append((lo, hi))
    result[0][0][1] += .18
    result[-1][1][1] = min(result[-1][1][1], mouth - .08)
    return result

def make_dynamic(name, elements, body, neck_index, mouth, images, textures, folder, assets):
    boxes = connected_regions(elements, VOLUMES[name], neck_index, mouth)
    colors = []
    windows = 0
    for e in body:
        if not e.get('name', '').startswith('source_'):
            continue
        index = int(e['name'].split('_')[1])
        if index not in SHELLS[name] or name == 'apple_cider':
            continue
        for face, f in e['faces'].items():
            tile = patch(images[f['texture'][1:]], f['uv'])
            if not tile.width or not tile.height:
                continue
            if name == 'whiskey_highland_hearth':
                # The side label occupies source atlas rows 11..13 (rows 5..7 of this
                # ten-pixel face). Its cream ink shares a color with whisky highlights,
                # so classify it by its physical region instead of color alone.
                for y in range(tile.height):
                    label = face in ('north', 'east', 'south', 'west') and 5 * tile.height <= y * 10 < 8 * tile.height
                    for x in range(tile.width):
                        color = tile.getpixel((x, y))
                        if not label and is_contents(name, color):
                            colors.append(color)
                            tile.putpixel((x, y), color[:3] + (0,))
                            windows += 16
                # The blue source outline already supplies the rim. Do not leave a
                # fixed brown quarter-pixel border around the moving whisky.
                tile = tile.resize((tile.width * 4, tile.height * 4), Image.Resampling.NEAREST)
                key = f'glass_{index}_{face}'
                tile.save(assets / f'textures/{folder}_{key}.png')
                textures[key] = 'farmingtales_drink_anim:' + folder + '_' + key
                f.update(texture='#' + key, uv=[0, 0, 16, 16])
                continue
            colors.extend(c for c in tile.getdata() if is_contents(name, c))
            # Keep the source outline and every label color. A quarter-pixel rim
            # also preserves the silhouette of solids whose edge uses liquid color.
            tile = tile.resize((tile.width * 4, tile.height * 4), Image.Resampling.NEAREST)
            removed = 0
            for y in range(1, tile.height - 1):
                for x in range(1, tile.width - 1):
                    color = tile.getpixel((x, y))
                    if is_contents(name, color):
                        tile.putpixel((x, y), color[:3] + (0,))
                        removed += 1
            if not removed:
                continue
            key = f'glass_{index}_{face}'
            tile.save(assets / f'textures/{folder}_{key}.png')
            textures[key] = 'farmingtales_drink_anim:' + folder + '_' + key
            f.update(texture='#' + key, uv=[0, 0, 16, 16])
            windows += removed
    if name == 'apple_cider':
        colors = [(195, 119, 35, 255), (224, 153, 52, 255), (173, 91, 25, 255), (235, 174, 76, 255)]
    assert colors, f'{name}: no source liquid palette identified'
    assert windows or name == 'apple_cider', f'{name}: no visible liquid window'
    # Use representative source liquor colors, never the label or bottle outline.
    palette = sorted(set(colors), key=lambda c: sum(c[:3]))
    if name != 'whiskey_highland_hearth':
        palette = palette[max(0, len(palette) // 5):]
    liquid = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            liquid.putpixel((x, y), palette[((x // 3) * 3 + y // 3) % len(palette)])
    atlas, side, top = liquid_atlas(liquid, [0, 0, 16, 16], liquid, [0, 0, 16, 16])
    return boxes, atlas, side, top, windows
