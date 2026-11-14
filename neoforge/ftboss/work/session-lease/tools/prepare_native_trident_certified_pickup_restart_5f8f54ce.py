import hashlib
from pathlib import Path
work=Path(__file__).resolve().parents[1]
origin=work/'tools/prepare_native_trident_post_pickup_restart_e07f785e.py'
assert hashlib.sha256(origin.read_bytes()).hexdigest()=='fe8e01a170a632dad458d5fd1b3484392045cfee803c67263afc149e67569f95'
text=origin.read_text(encoding='utf-8').replace('e07f785e','5f8f54ce')
text=text.replace('paths = [Path(__file__), helper,', 'paths = [Path(__file__), origin, helper,')
namespace={'__file__':__file__,'origin':origin}
exec(compile(text,str(origin),'exec'),namespace)
