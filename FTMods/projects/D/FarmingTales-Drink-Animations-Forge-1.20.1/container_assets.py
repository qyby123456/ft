"""Preserve exterior UVs while removing covered faces and cutting real openings."""
from copy import deepcopy

FACE_AXIS = {'west': (0, 0), 'east': (0, 1), 'down': (1, 0), 'up': (1, 1), 'north': (2, 0), 'south': (2, 1)}
GLASS = dict(rum=[3,4,5], coconut_wine=[4,5,6,7], brandy_xo=[4,6,7,8,9],
             purified_water=[6,7,8,9,10], vitamin_drink=list(range(9,17)), sweet_orange_juice=[5,6,7,8])
FACE_UV = {
    'up':(0,False,2,False),'down':(0,False,2,True),
    'north':(0,True,1,True),'south':(0,False,1,True),
    'west':(2,False,1,True),'east':(2,True,1,True)}

def exterior_box(e):
    """Canonical bounds and outward faces, preserving the source's physical UV mapping."""
    lo,hi=bounds(e)
    if e['from']==lo and e['to']==hi:return e
    result=deepcopy(e);result['from']=lo;result['to']=hi;result['faces']={}
    for face,spec in e['faces'].items():
        axis,end=FACE_AXIS[face]
        new_end=end if e['from'][axis]<e['to'][axis] else 1-end
        target=next(f for f,ae in FACE_AXIS.items() if ae==(axis,new_end))
        ua,ur,va,vr=FACE_UV[target]
        old_ua,old_ur,old_va,old_vr=FACE_UV[face]
        def sample(u,v):
            point=lo.copy()
            point[ua]=lo[ua]+(hi[ua]-lo[ua])*(1-u if ur else u)
            point[va]=lo[va]+(hi[va]-lo[va])*(1-v if vr else v)
            x=(point[old_ua]-e['from'][old_ua])/(e['to'][old_ua]-e['from'][old_ua])
            y=(point[old_va]-e['from'][old_va])/(e['to'][old_va]-e['from'][old_va])
            if old_ur:x=1-x
            if old_vr:y=1-y
            for _ in range(spec.get('rotation',0)//90):x,y=y,1-x
            a,b,c,d=spec['uv']
            return a+(c-a)*x,b+(d-b)*y
        q00,q10,q01=sample(0,0),sample(1,0),sample(0,1)
        converted=deepcopy(spec);converted.pop('rotation',None);converted.pop('cullface',None)
        if abs(q10[1]-q00[1])<1e-8 and abs(q01[0]-q00[0])<1e-8:
            converted['uv']=[*q00,q10[0],q01[1]]
        else:
            converted['rotation']=90
            converted['uv']=[q00[0],q10[1],q01[0],q00[1]]
        result['faces'][target]=converted
    return result

def bounds(e):
    return [min(a,b) for a,b in zip(e['from'],e['to'])], [max(a,b) for a,b in zip(e['from'],e['to'])]


def split_beer_cap(element):
    """Only the upper three source pixels leave the bottle; preserve side UV scale."""
    lower, cap = deepcopy(element), deepcopy(element)
    lo, hi = bounds(element)
    split = hi[1] - 3
    assert lo[1] < split < hi[1]
    cap['from'][1] = lower['to'][1] = split
    lower['from'][1] = lo[1] - .075
    for face in ('north', 'east', 'south', 'west'):
        a,b,c,d = element['faces'][face]['uv']
        seam = b + (d-b)*3/(hi[1]-lo[1])
        cap['faces'][face]['uv'] = [a,b,c,seam]
        lower['faces'][face]['uv'] = [a,seam,c,d]
    # The new cut face uses the remaining neck's own last pixel row.
    uv = lower['faces']['north']['uv']
    lower['faces']['up'] = deepcopy(lower['faces']['north'])
    lower['faces']['up']['uv'] = [uv[0],uv[3]-1,uv[2],uv[3]]
    lower['name'], cap['name'] = 'fixed_beer_neck', 'three_pixel_beer_cap'
    return lower, cap

def subtract(rect, cut):
    x0,z0,x1,z1=rect; a,b,c,d=cut
    a,b,c,d=max(x0,a),max(z0,b),min(x1,c),min(z1,d)
    if c<=a+1e-8 or d<=b+1e-8:return [rect]
    return [r for r in [(x0,z0,a,z1),(c,z0,x1,z1),(a,z0,c,b),(a,d,c,z1)] if r[2]-r[0]>1e-8 and r[3]-r[1]>1e-8]

def face_piece(e, face, rect, suffix=''):
    axis, end=FACE_AXIS[face]; others=[i for i in range(3) if i!=axis]
    p=deepcopy(e);p['name']=e.get('name','shell')+'_'+face+suffix
    plane=e['to' if end else 'from'][axis]
    p['from'][axis]=p['to'][axis]=plane
    for j,i in enumerate(others):
        a,b=rect[j],rect[j+2]
        p['from'][i],p['to'][i]=(a,b) if e['from'][i]<=e['to'][i] else (b,a)
    def interval(i, reverse=False):
        a,b=e['from'][i],e['to'][i]
        f=(p['from'][i]-a)/(b-a);t=(p['to'][i]-a)/(b-a)
        return (1-t,1-f) if reverse else (f,t)
    uaxis,ureverse,vaxis,vreverse={
        'up':(0,False,2,False),'down':(0,False,2,True),
        'north':(0,True,1,True),'south':(0,False,1,True),
        'west':(2,False,1,True),'east':(2,True,1,True)}[face]
    u0,u1=interval(uaxis,ureverse);v0,v1=interval(vaxis,vreverse)
    spec=deepcopy(e['faces'][face]);angle=spec.get('rotation',0)
    if angle==90:u0,v0,u1,v1=v0,1-u1,v1,1-u0
    elif angle==180:u0,v0,u1,v1=1-u1,1-v1,1-u0,1-v0
    elif angle==270:u0,v0,u1,v1=1-v1,u0,1-v0,u1
    a,b,c,d=spec['uv'];spec['uv']=[a+(c-a)*u0,b+(d-b)*v0,a+(c-a)*u1,b+(d-b)*v1]
    p['faces']={face:spec}
    return p

def hollow_shell(name, source, body):
    if name not in GLASS:return body
    shells=[(i,*bounds(source['elements'][i])) for i in GLASS[name]]
    overlays=[e for e in body['elements'] if not any(e is source['elements'][i] for i in GLASS[name])
              and not e.get('rotation',{}).get('angle',0)
              and all(abs(a-b)>1e-8 for a,b in zip(e['from'],e['to']))]
    highest=max(b[1] for i,a,b in shells)
    output=[]
    for e in body['elements']:
        if e.get('rotation',{}).get('angle',0)!=0 or any(abs(a-b)<1e-8 for a,b in zip(e['from'],e['to'])):
            output.append(e);continue
        lo,hi=bounds(e)
        for face in e['faces']:
            axis,end=FACE_AXIS[face];other=[i for i in range(3) if i!=axis]
            plane=e['to' if end else 'from'][axis];sign=1 if abs(plane-hi[axis])<1e-8 else -1
            rects=[(lo[other[0]],lo[other[1]],hi[other[0]],hi[other[1]])]
            for i,a,b in shells:
                if source['elements'][i] is e:continue
                probe=plane+sign*.012
                tolerance=.12 if axis==1 else 0
                if a[axis]-tolerance<probe<b[axis]+tolerance:
                    cut=(a[other[0]],a[other[1]],b[other[0]],b[other[1]])
                    rects=[piece for r in rects for piece in subtract(r,cut)]
            if any(e is source['elements'][i] for i in GLASS[name]):
                for overlay in overlays:
                    a,b=bounds(overlay);probe=plane+sign*.012
                    # Keep the label surface where it lies over or exactly on the glass.
                    # Otherwise double-sided shell rendering exposes coplanar label flicker.
                    same_plane=abs(plane-(b[axis] if sign>0 else a[axis]))<1e-7
                    if a[axis]<probe<b[axis] or same_plane:
                        cut=(a[other[0]],a[other[1]],b[other[0]],b[other[1]])
                        rects=[piece for r in rects for piece in subtract(r,cut)]
            if axis==1 and sign>0 and abs(plane-highest)<.001:
                hole=(lo[0]+.14,lo[2]+.14,hi[0]-.14,hi[2]-.14)
                rects=[piece for r in rects for piece in subtract(r,hole)]
            canonical=exterior_box(e)
            outer=next(f for f,ae in FACE_AXIS.items() if ae==(axis,1 if sign>0 else 0))
            output.extend(face_piece(canonical,outer,r,'_'+str(i)) for i,r in enumerate(rects))
    body['elements']=output
    return body

def can_parts(template, source):
    lid=deepcopy(source['elements'][2]);rot=lid['rotation'];origin=rot['origin']
    hole=[6.95,8.10,8.45,9.90]
    body=deepcopy(template);body['elements']=deepcopy(source['elements'][:2])
    body['elements'][0]['faces'].pop('up',None)
    lo,hi=bounds(lid)
    for face in lid['faces']:
        if face in ('up','down'):
            pieces=subtract((lo[0],lo[2],hi[0],hi[2]),hole)
            body['elements'].extend(face_piece(lid,face,p,'_rim'+str(i)) for i,p in enumerate(pieces))
        else:
            piece=deepcopy(lid);piece['faces']={face:piece['faces'][face]};body['elements'].append(piece)
    # The hole has a metal thickness; the inner wall never seals its center.
    for face,axis,plane in [('east',0,hole[0]),('west',0,hole[2]),('south',2,hole[1]),('north',2,hole[3])]:
        wall=deepcopy(lid);wall['from']=[hole[0],lo[1],hole[1]];wall['to']=[hole[2],hi[1],hole[3]]
        wall['from'][axis]=wall['to'][axis]=plane
        wall['faces']={face:deepcopy(lid['faces']['north'])};wall['name']='opening_inner_wall';body['elements'].append(wall)
    flap=deepcopy(template);flap['elements']=[]
    for face in ('up','down'):
        p=face_piece(lid,face,hole,'_opening_flap');p.pop('rotation',None)
        p['from'][1]=p['to'][1]=hi[1]-(.055 if face=='down' else 0)
        flap['elements'].append(p)
    return body,flap,dict(origin=[origin[0],0,origin[2]],yaw=rot['angle'],hinge=[(hole[0]+hole[2])/2,hi[1],hole[3]],hole=hole)
