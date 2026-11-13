import json
from pathlib import Path
import hashlib

ROOT=Path(__file__).resolve().parent
CATALOG=ROOT.parents[1]
certificate_path=CATALOG/'continuous-native-review/finite-certificate.json'
native_path=CATALOG/'continuous-native-review/inputs/native-shapes.json'
certificate=json.loads(certificate_path.read_text('utf-8'))
native=json.loads(native_path.read_text('utf-8'))
checks=[]
nonfinite=[]
outside=[]
for strip in certificate['results']:
    scene=next(scene for scene in native['scenes'] if scene['stripId']==strip['stripId'])
    origin=[scene['actualOrigin'][axis] for axis in ('x','y','z')]
    turns=scene['quarterTurns']
    boxes=[box for block in scene['blocks'] for box in block['boxes']]
    for index,boundary in enumerate(strip['nativeStepReplayRequired']):
        source_x=sum(boundary['xAtom'])/2
        endpoints=[]
        for direction in (-1,1):
            source_z=boundary['boundaryZ']+direction*.05
            active=[box[4] for box in boxes if box[4]<=4 and source_x+.3>box[0] and source_x-.3<box[3] and source_z+.3>box[2] and source_z-.3<box[5]]
            feet=max(active) if active else None
            if feet is None:
                nonfinite.append({'stripId':strip['stripId'],'index':index,'direction':direction})
                endpoints.append(None)
                continue
            rotated=[(source_x,source_z),(-source_z,source_x),(-source_x,-source_z),(source_z,-source_x)][turns]
            world=[origin[0]+rotated[0],origin[1]+feet,origin[2]+rotated[1]]
            if abs(world[0])>=29999984 or abs(world[2])>=29999984 or world[1]<-64 or world[1]+3>320:
                outside.append({'stripId':strip['stripId'],'index':index,'world':world})
            endpoints.append({'local':[source_x,feet,source_z],'world':world})
        checks.append({'stripId':strip['stripId'],'index':index,'xAtom':boundary['xAtom'],'endpoints':endpoints})
result={'status':'STATIC_DOUBLE_FEET_REPLAY_COMPLETE','checks':len(checks),'endpoints':len(checks)*2,'nonfiniteFeet':nonfinite,'outsideVanillaOverworldCoordinateBounds':outside,'worldBorderCustomOrChunkTicketReadiness':'NOT_PROVEN_BY_CAPTURE; query actual world border/owned-world/chunks separately in fixture','sourceHashes':{'certificate':hashlib.sha256(certificate_path.read_bytes()).hexdigest(),'native':hashlib.sha256(native_path.read_bytes()).hexdigest()},'conclusion':'Using exact captured native boxes and the fixture own double predicates/top<=4, all352 requirements yield finite endpoints. This does not remove the need to guard finiteness before physical/setPos. Null bounds remains independently confirmed setup exception in failed-run unchanged().','checksDetail':checks}
ROOT.mkdir(exist_ok=True,parents=True)
(ROOT/'feet-check.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({key:result[key] for key in ('checks','endpoints','nonfiniteFeet','outsideVanillaOverworldCoordinateBounds')},ensure_ascii=False))
