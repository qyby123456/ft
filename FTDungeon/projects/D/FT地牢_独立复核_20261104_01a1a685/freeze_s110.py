import json
from pathlib import Path

code = Path('freeze_s1_01.py').read_text(encoding='utf-8').replace('nature-redo-b01-s1-01-20261104', 'nature-redo-b10-s1-10-20261105').replace('nature-redo-b01-s1-01', 'nature-redo-b10-s1-10').replace("folder=ROOT/'S1-01-independent'", "folder=ROOT/'S1-10-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c', '6d77ca4a012b88665952f096e641e23d7167ffd84179726cdf5f8c2531d421f9')
exec(compile(code, __file__, 'exec'))
specification = json.loads((folder / 'route-input.json').read_text(encoding='utf-8'))
assert len(metadata['body_route']) == 539
specification[0]['targets'].extend({'label': f'body-{index}', 'position': position} for index, position in enumerate(metadata['body_route']))
(folder / 'route-input.json').write_text(json.dumps(specification, indent=2), encoding='utf-8')
