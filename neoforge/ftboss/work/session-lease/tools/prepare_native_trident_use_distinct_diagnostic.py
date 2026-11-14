import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_trident_use_diagnostic.py'
frozen = json.loads((work / 'reports/native-trident-use-diagnostic-preparation-9d1607e8.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
helper_text = origin.read_text(encoding='utf-8').replace('registry', 'entryRegistry')
helper_text = helper_text.replace('trident-use-diagnostic-9d1607e8', 'trident-use-diagnostic-distinct-9d1607e8')
helper_text = helper_text.replace('run_native_trident_use_diagnostic_9d1607e8', 'run_native_trident_use_diagnostic_distinct_9d1607e8')
helper_text = helper_text.replace('native-trident-use-diagnostic-preparation-9d1607e8', 'native-trident-use-diagnostic-distinct-preparation-9d1607e8')
helper_text = helper_text.replace('[Path(__file__), preparation, helper, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), origin, preparation, helper, old_source, old_launcher, new_source, new_launcher]')
exec(compile(helper_text, str(__file__), 'exec'))
