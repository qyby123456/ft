import hashlib
import json
import math
from pathlib import Path

root=Path(__file__).resolve().parent
digest=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
candidate=root/'merged/merged.nbt'
sha256=digest(candidate)
design=json.loads((root/'merged/design.json').read_text(encoding='utf-8'))
reports={}
for name in ['natural-movement','natural-detours']:
    qa=root/'qa'/name
    result=json.loads((qa/'motion-results.json').read_text(encoding='utf-8'))
    assert result['sha256']==sha256 and result['passed']
    assert json.loads((qa/'exit.json').read_text(encoding='utf-8-sig'))['exit_code']==0
    frames=result['frames']
    assert [frame['tick'] for frame in frames]==list(range(1,len(frames)+1))
    spawn=result['events'][0]['feet']
    positions=[spawn]+[frame['feet'] for frame in frames]
    displacements=[math.dist(first,second) for first,second in zip(positions,positions[1:])]
    assert max(displacements)<0.9
    assert math.dist(positions[-1],[10.5,9,23.5])<0.3
    summary={'sha256':sha256,'passed':True,'physics_frames':len(frames),'max_single_tick_displacement':max(displacements),'distance_travelled':sum(displacements),'min_feet_y':min(position[1] for position in positions),'max_feet_y':max(position[1] for position in positions),'ladder_ticks':sum(frame['ladder'] for frame in frames),'water_ticks':sum(frame['water'] for frame in frames),'jump_intent_ticks':sum(frame['jump_intent'] for frame in frames),'initial_feet':spawn,'final_feet':positions[-1],'results_sha256':digest(qa/'motion-results.json'),'source_sha256':digest(qa/'native-test/src/main/java/net/farmingtales/s5waterqa/WaterTests.java')}
    if name=='natural-movement':
        turn=next(event['tick'] for event in result['events'] if event['event']=='finish_turnaround')
        visits={}
        for label,subset in [('outbound',[frame for frame in frames if frame['tick']<turn]),('return',[frame for frame in frames if frame['tick']>=turn])]:
            visits[label]={}
            for module in design['modules']:
                origin=module['origin'];size=module['size']
                hits=[frame for frame in subset if all(origin[axis]<=frame['feet'][axis]<origin[axis]+size[axis] for axis in range(3))]
                assert hits,module['id']
                visits[label][module['id']]={'frames':len(hits),'first_tick':hits[0]['tick'],'last_tick':hits[-1]['tick']}
        assert summary['max_feet_y']>=9.49 and summary['min_feet_y']<=5.01
        summary['room_visits']=visits;summary['finish_turnaround_tick']=turn
    else:
        assert summary['ladder_ticks']>10 and summary['water_ticks']>10 and summary['max_feet_y']>=14
        summary['events']=result['events']
    reports[name]=summary
report={'sha256':sha256,'passed':True,'runs':reports,'scope':'Producer recorded actual native physics every normal game tick; both runs one initial spawn and no controller setPos/velocity/gravity changes after it. Room witnesses measured from actual positions. Trace continuity checked without substituting route-graph reachability. Separate same-candidate actors for main round trip and optional ladder/water detours. No connected client or oxygen survival certification.'}
(root/'qa/natural-motion-summary.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False))
