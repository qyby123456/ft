from pathlib import Path
import sys

sys.argv = ['freeze_revision.py', '8', 'bdb3d81c6b955badcc41ed2290ff540a13f6dca820f6b61f3923d3465e897e41', 'S8-10-independent', 'FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-10', 'views/inspection-overview-front.png', 'incoming/S8R_IMG10/overview.nbt']
code = Path('freeze_revision.py').read_text(encoding='utf-8').replace("Path(record['candidate']).with_suffix('.json')", "(Path(record['candidate']).parent / 'room.json')")
exec(compile(code, 'freeze_revision.py', 'exec'))
