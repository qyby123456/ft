"""Interior surfaces for hand-held mugs and a closed, UV-cropped chocolate bite."""
from copy import deepcopy
from container_assets import face_piece, FACE_AXIS


def coconut_interior():
    # Sample the opaque off-white flesh already present on the original cut rim.
    flesh = {'texture': '#0', 'uv': [5, 0, 6, .5]}
    pieces = []
    for name, lo, hi, face in [
        ('west', [4.5, .18, 5.01], [5.01, 6.47, 10.99], 'east'),
        ('east', [10.99, .18, 5.01], [11.5, 6.47, 10.99], 'west'),
        ('north', [5.01, .18, 4.5], [10.99, 6.47, 5.01], 'south'),
        ('south', [5.01, .18, 10.99], [10.99, 6.47, 11.5], 'north'),
        ('floor', [5.01, .12, 5.01], [10.99, .18, 10.99], 'up'),
    ]:
        pieces.append(dict(name='coconut_flesh_'+name, **{'from': lo, 'to': hi},
                           faces={face: deepcopy(flesh)}))
    return pieces


def coffee_interior():
    # The original rim atlas has transparent pixels inside its border. Use the
    # opaque ceramic side, and leave the source exterior/negative-size pieces alone.
    ceramic = {'texture': '#0', 'uv': [0, 5, 3.5, 7.5]}
    pieces = []
    for name, lo, hi, face in [
        ('west', [4.5, .15, 5.5], [5.5, 5, 10.5], 'east'),
        ('east', [10.5, .15, 5.5], [11.5, 5, 10.5], 'west'),
        ('north', [5.5, .15, 4.5], [10.5, 5, 5.5], 'south'),
        ('south', [5.5, .15, 10.5], [10.5, 5, 11.5], 'north'),
    ]:
        pieces.append(dict(name='ceramic_inner_'+name, **{'from': lo, 'to': hi},
                           faces={face: deepcopy(ceramic)}))
    pieces.append(dict(name='ceramic_floor', **{'from': [4.5, 0, 4.5], 'to': [11.5, .15, 11.5]},
                       faces={'up': deepcopy(ceramic), 'down': deepcopy(ceramic)}))
    return pieces


def chocolate_bite(prop):
    pieces = []
    # Keep source UV coordinates on the surviving half instead of stretching
    # the entire atlas (including its transparent silhouette) over that half.
    for original in prop:
        # This diagonal detail belongs to the bitten end. Its -45 degree
        # rotation differs from the slab's 22.5 degrees, so testing its raw
        # positive X bounds retained a long strip across the removed half.
        # Remove the detail with that end; the opaque core closes the remainder.
        if original['name'] == 'original_7':
            continue
        lo = [min(a, b) for a, b in zip(original['from'], original['to'])]
        hi = [max(a, b) for a, b in zip(original['from'], original['to'])]
        if hi[0] <= 0:
            continue
        if lo[0] >= 0:
            pieces.append(deepcopy(original))
            continue
        lo[0] = 0
        for face in original['faces']:
            axis, end = FACE_AXIS[face]
            if axis == 0:
                e = deepcopy(original)
                for field in ['from', 'to']:
                    e[field][0] = max(0, e[field][0])
                e['faces'] = {face: deepcopy(original['faces'][face])}
            else:
                axes = [j for j in range(3) if j != axis]
                e = face_piece(original, face, [lo[j] for j in axes]+[hi[j] for j in axes])
            e['name'] = original['name']+'_'+face
            pieces.append(e)
    # The placed chocolate uses alpha-cutout outer details. Close the actual
    # bitten volume behind them with chocolate sampled from an opaque source patch.
    core = deepcopy(next(e for e in prop if e['name'] == 'original_8'))
    core['name'] = 'chocolate_bite_interior'
    core['from'] = [.005, -.995, -1.995]
    core['to'] = [3.995, .995, 3.995]
    core['faces'] = {f: {'texture': '#0', 'uv': [4.5, 6.5, 7.5, 7.5]}
                     for f in ['west', 'east', 'north', 'south', 'up', 'down']}
    pieces.append(core)
    return pieces
