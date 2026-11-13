import argparse
import json
import math
from fractions import Fraction
from pathlib import Path


def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8'))


def overlaps(low_a,high_a,low_b,high_b):
    return low_a < high_b and low_b < high_a


def interval_cells(low,high,breaks):
    values=sorted({low,high,*[value for value in breaks if low < value < high]})
    atoms=[]
    for index,value in enumerate(values):
        atoms.append((value,value))
        if index+1<len(values):
            atoms.append((value,values[index+1]))
    return atoms


def certify(contract,native):
    if native.get('schemaVersion')!=1 or native.get('coverageComplete') is not True:
        raise ValueError('complete native collection required')
    if native.get('collectionMode')!='WORLD_NATIVE_VOXELSHAPE':
        raise ValueError('block names or samples are not native collision evidence')
    if native.get('contractSha256')!=contract['inputSha256']:
        raise ValueError('exact contract hash binding required')
    if len(native.get('scenes',[]))!=len(contract['strips']) or {scene['stripId'] for scene in native['scenes']}!={strip['stripId'] for strip in contract['strips']}:
        raise ValueError('every strip/rotation must be represented exactly once')
    results=[]
    for strip in contract['strips']:
        strip={**strip,**{key:Fraction(str(strip[key])) for key in ('bodyWidth','minFeetY','maxFeetY','headroom','nativeStepHeight','startFeetY','endFeetY')},'centerX':[Fraction(str(value)) for value in strip['centerX']],'centerZ':[Fraction(str(value)) for value in strip['centerZ']]}
        scene=next(scene for scene in native['scenes'] if scene['stripId']==strip['stripId'])
        if scene.get('quarterTurns')!=strip.get('quarterTurns'):
            raise ValueError('native rotation identity mismatch')
        if scene['resourceSha256']!=strip['resourceSha256']:
            raise ValueError('resource hash mismatch')
        if scene.get('nativeContext')!='PLAYER_STANDING_COLLISION_CONTEXT':
            raise ValueError('native player context required')
        expected={tuple(position) for position in strip['requiredBlocks']}
        actual={tuple(block['localBlock']) for block in scene['blocks']}
        if expected!=actual or len(scene['blocks'])!=len(expected):
            raise ValueError('missing/duplicate/out-of-coverage cells')
        boxes=[]
        for block in scene['blocks']:
            if block.get('fluidEmpty') is not True:
                raise ValueError('fluid cannot be silently ignored')
            if 'nativeState' not in block or 'boxes' not in block:
                raise ValueError('air/empty shapes must be explicit')
            for box in block['boxes']:
                if len(box)!=6 or not all(isinstance(value,(int,float)) and math.isfinite(value) for value in box):
                    raise ValueError('invalid native AABB')
                if not all(box[axis]<box[axis+3] for axis in range(3)):
                    raise ValueError('empty/inverted AABB')
                policies=contract.get('nativeShapeBoundsByBlockName',{})
                if policies:
                    name=block.get('blockName')
                    if name not in policies:
                        raise ValueError('unknown native shape source; bounded policy required')
                    expected_name=strip.get('sourceBlockNames',{}).get(','.join(map(str,block['localBlock'])))
                    if expected_name is not None and name!=expected_name:
                        raise ValueError('source/native block name mismatch')
                    bounds=policies[name]
                else:
                    bounds=[0,0,0,1,1,1]
                if not all(block['localBlock'][axis]+bounds[axis] <= box[axis] and box[axis+3]<=block['localBlock'][axis]+bounds[axis+3] for axis in range(3)):
                    raise ValueError('native shape exceeds approved bounded envelope; expand policy/collection, never clip')
                boxes.append(([Fraction(str(value)) for value in box],block['localBlock']))
        radius=strip['bodyWidth']/2
        supports=[(box,position) for box,position in boxes if box[4]<=strip['maxFeetY'] and box[4]>=strip['minFeetY']]
        x_cells=interval_cells(*strip['centerX'],[edge+offset for box,_ in boxes for edge in (box[0],box[3]) for offset in (-radius,radius)])
        z_cells=interval_cells(*strip['centerZ'],[edge+offset for box,_ in boxes for edge in (box[2],box[5]) for offset in (-radius,radius)])
        tiles=[]
        failures=[]
        for x_index,(x_low,x_high) in enumerate(x_cells):
            for z_index,(z_low,z_high) in enumerate(z_cells):
                center_x=(x_low+x_high)/2
                center_z=(z_low+z_high)/2
                active=[(box,position) for box,position in supports if overlaps(center_x-radius,center_x+radius,box[0],box[3]) and overlaps(center_z-radius,center_z+radius,box[2],box[5])]
                feet=max((box[4] for box,_ in active),default=None)
                blockers=[]
                if feet is not None:
                    for box,position in boxes:
                        if overlaps(center_x-radius,center_x+radius,box[0],box[3]) and overlaps(center_z-radius,center_z+radius,box[2],box[5]) and overlaps(feet,feet+strip['headroom'],box[1],box[4]):
                            blockers.append(position)
                tile={'xIndex':x_index,'zIndex':z_index,'centerXInterval':[x_low,x_high],'centerZInterval':[z_low,z_high],'feetY':feet,'supportBlocks':[position for box,position in active if box[4]==feet],'blockingBlocks':blockers}
                tiles.append(tile)
                if feet is None or blockers:
                    failures.append({'type':'NO_SUPPORT' if feet is None else 'HEADROOM_COLLISION','tile':tile})
        indexed={(tile['xIndex'],tile['zIndex']):tile for tile in tiles}
        native_step_replay=[]
        for x_index in range(len(x_cells)):
            for z_index in range(len(z_cells)-1):
                first=indexed[(x_index,z_index)]
                second=indexed[(x_index,z_index+1)]
                if first['feetY'] is not None and second['feetY'] is not None and first['feetY']!=second['feetY']:
                    native_step_replay.append({'xAtom':x_cells[x_index],'boundaryZ':z_cells[z_index][1],'firstFeet':first['feetY'],'secondFeet':second['feetY'],'requiredNativeForwardReverse':'PENDING; demonstrate native0.6step resolution, not setPos teleport'})
                if first['feetY'] is not None and second['feetY'] is not None and abs(second['feetY']-first['feetY'])>strip['nativeStepHeight']:
                    failures.append({'type':'STEP_EXCEEDS_LIMIT','xIndex':x_index,'boundaryZ':z_cells[z_index][1],'fromFeet':first['feetY'],'toFeet':second['feetY']})
        # Closed outer endpoints must match original lower/upper landing feet; internal
        # shared boundaries use the maximum adjacent floor, including instantaneous rise.
        for x_index in range(len(x_cells)):
            for z_index,expected_y in ((0,strip['startFeetY']),(len(z_cells)-1,strip['endFeetY'])):
                if indexed[(x_index,z_index)]['feetY']!=expected_y:
                    failures.append({'type':'ENDPOINT_FEET_MISMATCH','xIndex':x_index,'zIndex':z_index})
        results.append({'stripId':strip['stripId'],'status':'FAIL' if failures else 'CONDITIONAL_FINITE_COVERAGE_PASS','tiles':tiles,'failures':failures,'criticalXIntervals':x_cells,'criticalZIntervals':z_cells,'nativeStepReplayRequired':native_step_replay,'boundaryPolicy':'Exact contact permitted. Both open interval atoms and every singleton boundary atom explicitly evaluated using exact rational arithmetic. Adjacent support discontinuities bounded by stepHeight in both directions.','claim':'Finite static configuration-space coverage for native boxes and support-envelope stair profile, not executed player motion/interaction/tick acceptance.'})
    return {'schemaVersion':1,'status':'FAIL' if any(result['status']=='FAIL' for result in results) else 'CONDITIONAL_FINITE_COVERAGE_PASS','results':results,'requiredSeparateNativeEvidence':['native forward/reverse step resolution at each discontinuity, no arbitrary teleport','native geometry invariant across collision context/state during traversal','native state rotations/source hash readback','closed-set endpoint/body validity and actual player step policy match','side doublegate interaction separately; cannot seal main arch'],'independentWorldTestsExecutedByThisScript':False}


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--contract',required=True)
    parser.add_argument('--native',required=True)
    parser.add_argument('--out',required=True)
    arguments=parser.parse_args()
    contract=load(arguments.contract)
    import hashlib
    contract['inputSha256']=hashlib.sha256(Path(arguments.contract).read_bytes()).hexdigest()
    result=certify(contract,load(arguments.native))
    Path(arguments.out).write_text(json.dumps(result,ensure_ascii=False,indent=2,default=lambda value:float(value) if isinstance(value,Fraction) else str(value))+'\n',encoding='utf-8')
    print(result['status'])


if __name__=='__main__':
    main()
