"""Fixed source bottles with a source-sized opening and a three-length stopper."""
from copy import deepcopy
from math import floor
from container_assets import bounds, face_piece, subtract


def cut_y(element, low, high):
    result = deepcopy(element)
    a, b = bounds(element)
    result['from'][1], result['to'][1] = low, high
    for direction in ('north', 'east', 'south', 'west'):
        if direction not in result['faces']:
            continue
        u, v, s, t = element['faces'][direction]['uv']
        height = b[1] - a[1]
        result['faces'][direction]['uv'] = [u, v + (t-v)*(b[1]-high)/height,
                                           s, v + (t-v)*(b[1]-low)/height]
    return result


def fixed_bottle(elements, cap_index, cut, screw, images):
    original = elements[cap_index]
    a, b = bounds(original)
    head = cut_y(original, cut, b[1])
    head['name'] = 'removable_cork'
    head['faces']['down'] = deepcopy(head['faces']['up'])
    body = [deepcopy(e) for i, e in enumerate(elements) if i != cap_index]
    if cut > a[1]:
        lower = cut_y(original, a[1], cut)
        lower['faces'].pop('up', None)
        body.append(lower)

    # Repeat the original side texels, without scaling their physical pixel size.
    # The renderer clips the buried part until it emerges above the bottle mouth.
    height = b[1] - cut
    cap = []
    for segment in range(1 if screw else 3):
        piece = deepcopy(head)
        piece['name'] = 'removable_cork' if segment == 0 else f'cork_shank_{segment}'
        piece['from'][1] -= segment * height
        piece['to'][1] -= segment * height
        if segment:
            piece['faces'].pop('up', None)
        if not screw and segment < 2:
            piece['faces'].pop('down', None)
        cap.append(piece)

    # Use the source cork's complete footprint. The old 0.22-unit inset left a
    # thin brown outline from the source's painted cork on top of the green rim.
    support = [e for e in body if bounds(e)[0][0] <= (a[0]+b[0])/2 <= bounds(e)[1][0]
               and bounds(e)[0][2] <= (a[2]+b[2])/2 <= bounds(e)[1][2]
               and bounds(e)[1][1] <= cut + .001 and bounds(e)[1][1] > bounds(e)[0][1]
               and 'north' in e['faces']]
    neck = max(support, key=lambda e: bounds(e)[1][1])
    na, nb = bounds(neck)
    opening = (max(a[0], na[0]), max(a[2], na[2]), min(b[0], nb[0]), min(b[2], nb[2]))
    depth = cut - (height if screw else 2 * height)
    rings = []
    for element in body:
        lo, hi = bounds(element)
        if depth < hi[1] <= cut + .001 and 'up' in element['faces']:
            source = deepcopy(element)
            element['faces'].pop('up')
            for i, rect in enumerate(subtract((lo[0], lo[2], hi[0], hi[2]), opening)):
                rings.append(face_piece(source, 'up', rect, f'_mouth_{i}'))
    body.extend(rings)

    # Tile the glass immediately below the stopper into the cavity. Each band
    # keeps the original side UV density; no procedural colors or miniature rim.
    band_height = min(height, nb[1] - na[1])
    band = cut_y(neck, nb[1] - band_height, nb[1])
    opposite = dict(north='south', south='north', east='west', west='east')
    top = cut
    while top > depth + 1e-7:
        h = min(band_height, top - depth)
        for direction in opposite:
            if direction not in band['faces']:
                continue
            along_z = direction in ('east', 'west')
            start, end = (opening[1], opening[3]) if along_z else (opening[0], opening[2])
            tile = face_piece(band, direction, (nb[1]-h, start, nb[1], end) if along_z
                              else (start, nb[1]-h, end, nb[1]))
            tile['name'] = f'mouth_inside_{direction}_{top:g}'
            tile['from'][1], tile['to'][1] = top - h, top
            axis = 0 if along_z else 2
            plane = opening[0 if direction == 'west' else 2] if along_z else opening[1 if direction == 'north' else 3]
            tile['from'][axis] = tile['to'][axis] = plane
            tile['faces'] = {opposite[direction]: tile['faces'][direction]}
            body.append(tile)
        top -= h

    # A dark source-glass texel closes the cavity below the fully seated shank.
    face = deepcopy(band['faces']['north'])
    u, v, s, t = face['uv']
    width, height = images[face['texture'][1:]].size
    # A constant coordinate on a texel edge can alternate adjacent colors as
    # the quad moves. Sample the center of the source pixel instead.
    u = (min(width-1, floor((u+s)*width/32))+.5)*16/width
    v = (min(height-1, floor((v+t)*height/32))+.5)*16/height
    face['uv'] = [u, v, u, v]
    face.pop('rotation', None)
    body.append({'name': 'mouth_interior_floor', 'from': [opening[0], depth, opening[1]],
                 'to': [opening[2], depth, opening[3]], 'faces': {'up': face}})
    return body, cap
