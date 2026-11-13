import hashlib
import json
import math
from pathlib import Path

root=Path(__file__).resolve().parent
assembly=json.loads((root/'assembly/assembly.json').read_text(encoding='utf-8'))
size=assembly['size']
output=root/'assembly/clearance'
output.mkdir(exist_ok=True)
tiles=[]
commands=[]
for east in range(0,size[0],32):
    for up in range(0,size[1],32):
        for south in range(0,size[2],32):
            start=[east,up,south]
            end=[min(size[0]-1,east+31),min(size[1]-1,up+31),min(size[2]-1,south+31)]
            volume=math.prod(end[axis]-start[axis]+1 for axis in range(3))
            assert volume<=32768
            tiles.append({'from':start,'to':end,'volume':volume})
            commands.append('fill '+' '.join('~'+str(value) for value in start+end)+' minecraft:air replace')
assert sum(tile['volume'] for tile in tiles)==math.prod(size)
for index,first in enumerate(tiles):
    for second in tiles[index+1:]:
        assert any(first['to'][axis]<second['from'][axis] or second['to'][axis]<first['from'][axis] for axis in range(3))
path=output/'clear-entire-bounds.mcfunction'
path.write_text('\n'.join(commands)+'\n',encoding='utf-8')
(output/'clearance-plan.json').write_text(json.dumps({'assembly_sha256':assembly['sha256'],'size':size,'volume':math.prod(size),'covered_volume':sum(tile['volume'] for tile in tiles),'nonoverlap':True,'tiles':tiles,'commands_file':str(path),'commands_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'execution':'NOT_EXECUTED; offline relative-coordinate plan only','anchor':'Execution position must be intended final minimum X/Y/Z of full structure, zero rotation. Clear whole bounds before placing any source/part; sparse parts alone do not clear omitted cells.','scope':'Block clearance only; no entity deletion or production-world deployment authorization'},ensure_ascii=False,indent=2),encoding='utf-8')
print('Full bounds clearance',len(tiles),'commands',sum(tile['volume'] for tile in tiles),'cells; not executed')
