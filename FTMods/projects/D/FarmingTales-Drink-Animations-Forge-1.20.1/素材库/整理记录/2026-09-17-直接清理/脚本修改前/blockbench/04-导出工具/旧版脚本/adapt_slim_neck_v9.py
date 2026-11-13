"""Preserve the authored v8 motion and add a separate, attached 3px palm profile."""
import base64
import copy
import json
import math
from pathlib import Path
import uuid
import zipfile

import numpy as np

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent
SOURCE = ROOT / 'TACZ动态视角-v8/农场物语-橙子果酒-TACZ动态视角-v8.bbmodel'
OUT = ROOT / '纤细手臂与玻璃瓶颈-v9'
model = json.loads(SOURCE.read_text(encoding='utf-8'))
uid = lambda name: str(uuid.uuid5(uuid.NAMESPACE_URL, 'farmingtales/slim-v9/' + name))


def rot(angles):
    x, y, z = np.radians(angles)
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    return np.array([[cz*cy, cz*sy*sx-sz*cx, cz*sy*cx+sz*sx],
                     [sz*cy, sz*sy*sx+cz*cx, sz*sy*cx-cz*sx], [-sy, cy*sx, cy*cx]])


def channels(animator):
    result = {}
    for key in animator.get('keyframes', []):
        result.setdefault(key['channel'], []).append(key)
    for keys in result.values():
        keys.sort(key=lambda k: k['time'])
    return result


def value(track, channel, time, default):
    keys = track.get(channel, [])
    vec = lambda k: np.array([float(k['data_points'][0].get(a, 0)) for a in 'xyz'])
    if not keys:
        return np.array(default, dtype=float)
    if time <= keys[0]['time']:
        return vec(keys[0])
    for a, b in zip(keys, keys[1:]):
        if time <= b['time']:
            t = (time-a['time'])/(b['time']-a['time'])
            x, y = vec(a), vec(b)
            mode = a.get('interpolation', 'linear')
            if mode == 'step':
                return x
            if mode == 'bezier':
                return (1-t)**3*x+3*(1-t)**2*t*(x+np.array(a.get('bezier_right_value', [0]*3)))+3*(1-t)*t*t*(y+np.array(b.get('bezier_left_value', [0]*3)))+t**3*y
            return x+(y-x)*t
    return vec(keys[-1])


groups = {g['uuid']: g for g in model['groups']}
names = {g['name']: g for g in model['groups']}
parents = {}
def hierarchy(nodes, parent=None):
    for node in nodes:
        if isinstance(node, dict):
            parents[node['uuid']] = parent
            hierarchy(node.get('children', []), node['uuid'])
hierarchy(model['outliner'])
right = names['right_forearm']
bottle = names['bottle']
# Same width correction and offset as BottleRig.idleGripOffset; expressed in BB pixels.
slim_delta = rot(right['rotation']) @ np.array([-.35, 0, 0]) + np.array([0, -.10, -.20])
bottle_rest_rotation = rot(bottle['rotation'])


def world_linear(identifier, tracks, time):
    if identifier is None:
        return np.eye(3)
    group = groups[identifier]
    ch = tracks.get(identifier, {})
    local = rot(np.array(group.get('rotation', [0]*3)) + value(ch, 'rotation', time, [0]*3))
    local = local @ np.diag(value(ch, 'scale', time, [1]*3))
    return world_linear(parents[identifier], tracks, time) @ local


def adapt_positions(animation):
    tracks = {key: channels(a) for key, a in animation['animators'].items()}
    old = tracks.get(right['uuid'], {})
    def target(t):
        baseline = value(old, 'position', t, [0]*3)
        parent = world_linear(parents[right['uuid']], tracks, t)
        world_bottle = world_linear(bottle['uuid'], tracks, t)
        # Bottle's inherited 1.25 size is already present in the authored palm position.
        delta = np.linalg.solve(parent, world_bottle @ np.linalg.solve(bottle_rest_rotation*1.25, slim_delta))
        return baseline+delta
    length = animation['length']
    times = {0., float(length)} | {float(k['time']) for k in old.get('position', [])}
    def make_keys():
        ordered = sorted(times)
        result = []
        for i, t in enumerate(ordered):
            p = target(t)
            h = .0001
            left = (p-target(max(0,t-h)))/h if t else np.zeros(3)
            right_slope = (target(min(length,t+h))-p)/h if t < length else np.zeros(3)
            dl = (t-ordered[i-1])/3 if i else 0
            dr = (ordered[i+1]-t)/3 if i+1 < len(ordered) else 0
            result.append(dict(uuid=uid(animation['name']+str(t)), channel='position', time=round(t,6),
                interpolation='bezier', data_points=[dict(zip('xyz',p.tolist()))], bezier_linked=False,
                bezier_left_time=[-dl]*3, bezier_right_time=[dr]*3,
                bezier_left_value=(-left*dl).tolist(), bezier_right_value=(right_slope*dr).tolist()))
        return result
    # Keep the authored knots; add only a knot needed to hold the palm within 0.04px of its path.
    probes = np.linspace(0,length,max(2,math.ceil(length*60)+1))
    expected = [target(t) for t in probes]
    for iteration in range(100):
        keys = make_keys()
        errors = [np.max(np.abs(value({'position':keys},'position',t,[0]*3)-p)) for t,p in zip(probes,expected)]
        worst = int(np.argmax(errors))
        if errors[worst] <= .04:
            break
        times.add(float(probes[worst]))
    else:
        raise RuntimeError('Palm trajectory did not converge: '+animation['name'])
    animator = animation['animators'].setdefault(right['uuid'], dict(name=right['name'], type='bone', keyframes=[]))
    animator['keyframes'] = [k for k in animator['keyframes'] if k['channel'] != 'position'] + keys
    return dict(clip=animation['name'], keys=len(keys), added=len(keys)-max(2,len(old.get('position',[]))), max_error=float(max(errors)))


def skin_geometry(profile):
    slim = profile == 'slim'
    for element in model['elements']:
        if element.get('name') not in ('right_arm_skin','left_arm_skin'):
            continue
        left = element['name'] == 'left_arm_skin'
        origin = np.array(element['origin'])
        factor = 1 if left else .7
        width = 3 if slim else 4
        element['from'] = (origin + np.array([-width/2,-12,-2])*factor).tolist()
        element['to'] = (origin + np.array([width/2,0,2])*factor).tolist()
        u,v = (32,48) if left else (40,16)
        # This rig's palm is at local Y=0 and shoulder at Y=-12; vanilla is Y-down.
        faces = {'east':[u,v+16,u+4,v+4], 'north':[u+4,v+16,u+4+width,v+4],
                 'west':[u+4+width,v+16,u+8+width,v+4], 'south':[u+8+width,v+16,u+8+2*width,v+4],
                 'up':[u+4+width,v+4,u+4+2*width,v], 'down':[u+4,v,u+4+width,v+4]}
        for face, uv in faces.items():
            element['faces'][face]['uv'] = uv
    with zipfile.ZipFile(PROJECT.parents[2]/'.gradle-user/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar') as jar:
        texture = model['textures'][2]
        texture['source'] = 'data:image/png;base64,'+base64.b64encode(jar.read(f'assets/minecraft/textures/entity/player/{profile}/steve.png')).decode()
        texture['name'] = 'steve_'+profile+'_preview.png'


def main():
    global model
    OUT.mkdir(exist_ok=True)
    open_texture = (PROJECT/'src/main/resources/assets/farmingtales_drink_anim/textures/orange_wine_open.png').read_bytes()
    model['textures'][1]['source'] = 'data:image/png;base64,'+base64.b64encode(open_texture).decode()
    camera = next(e for e in model['elements'] if e.get('type') == 'camera')
    camera['position'] = [0,16,13.5]
    model['editor_state']['previews']['main']['position'] = [0,16,13.5]
    # The helper's unscaled cube is 12px long; the runtime uses the same 0.70 forearm length for both arms.
    for animation in model['animations']:
        animator = animation['animators'].get(names['left_cork_hand']['uuid'], {})
        for key in animator.get('keyframes', []):
            if key['channel'] == 'scale':
                for point in key['data_points']:
                    for axis in 'xyz':
                        if float(point.get(axis,0)) > 0:
                            point[axis] = float(point[axis])*.7
                for tangent in ('bezier_left_value','bezier_right_value'):
                    if tangent in key:
                        key[tangent] = [float(v)*.7 for v in key[tangent]]
    base = copy.deepcopy(model)
    report = {}
    for profile, label in [('wide','Steve'),('slim','纤细手臂')]:
        model = copy.deepcopy(base)
        skin_geometry(profile)
        model['name'] = f'农场物语-橙子果酒-{label}-v9'
        if profile == 'slim':
            report['slim_tracks'] = [adapt_positions(a) for a in model['animations']]
        path = OUT/(model['name']+'.bbmodel')
        path.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
        report[profile] = str(path)
    report['slim_idle_delta_pixels'] = slim_delta.tolist()
    report['neck_alpha'] = 217
    report['camera_position'] = [0,16,13.5]
    (OUT/'适配检查.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'profiles':2,'clips':len(model['animations']),'slim_delta':slim_delta.tolist()},ensure_ascii=True))


if __name__ == '__main__':
    main()
