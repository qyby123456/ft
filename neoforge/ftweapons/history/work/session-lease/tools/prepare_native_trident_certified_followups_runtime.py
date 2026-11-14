import hashlib
import json
import sys
from pathlib import Path
work=Path(__file__).resolve().parents[1]
current=json.loads((work/'reports/native-repair-ranged-use-candidate.json').read_text(encoding='utf-8'))
tag=Path(current['attempt']).name.removeprefix('native-repair-')
mode=sys.argv[1]
origins={'refusal': ('tools\\prepare_native_trident_certified_pickup_refusal_5f8f54ce.py', '462e4b84b023136457e582568bba1ba61d6de1c70c59404665e7b3d9ef284657'), 'restart': ('tools\\prepare_native_trident_certified_pickup_restart_5f8f54ce.py', '50690af9dbb18f3381af2026533739bd5a4f6eb8e60550fb9c6cdf6c7c3c6eab'), 'foreign': ('tools\\prepare_native_trident_certified_foreign_controls_5f8f54ce.py', '7c86919d1ac92851fa4f1b94c4d0b59fc330ef67e4ea33e415f884e04e292789')}
relative,wanted=origins[mode]
origin=work/relative
assert hashlib.sha256(origin.read_bytes()).hexdigest()==wanted
text=origin.read_text(encoding='utf-8').replace('5f8f54ce',tag)
namespace={'__file__':__file__}
exec(compile(text,str(origin),'exec'),namespace)
