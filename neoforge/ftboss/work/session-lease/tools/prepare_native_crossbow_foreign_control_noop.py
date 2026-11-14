import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_crossbow_foreign_control.py'
frozen = json.loads((work / 'reports/native-crossbow-foreign-control-preparation-89f2b629.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
helper_text = origin.read_text(encoding='utf-8')
old = "anchor = '                player.stopUsingItem();'"
new = "anchor = '                player.stopUsingItem();\\n                var fireResult ='"
assert helper_text.count(old) == 1
helper_text = helper_text.replace(old, new)
helper_text = helper_text.replace('[Path(__file__), helper, preparation, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), origin, helper, preparation, old_source, old_launcher, new_source, new_launcher]')
exec(compile(helper_text, str(__file__), 'exec'))
