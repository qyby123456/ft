import hashlib
import json
from pathlib import Path
work = Path(__file__).resolve().parents[1]
pickup_origin = work / 'tools/prepare_native_trident_complete_observation_runtime.py'
origin_receipt = json.loads((work / 'reports/native-trident-foundation-gate-preparation-29296443.json').read_text(encoding='utf-8'))
assert hashlib.sha256(pickup_origin.read_bytes()).hexdigest() == origin_receipt['inputHashes'][str(pickup_origin)]
pickup_preparation = work / 'reports/native-trident-physical-pickup-preparation-29296443.json'
frozen = json.loads(pickup_preparation.read_text(encoding='utf-8'))
pickup_observer = Path(frozen['newObserver'])
assert hashlib.sha256(pickup_observer.read_bytes()).hexdigest() == frozen['inputHashes'][str(pickup_observer)]
source = pickup_observer.read_text(encoding='utf-8')
first = source.index('                var physical =')
last = source.index('                row.put("originalThrowPickupAndPersistenceAccepted", false);', first)
block = source[first:last]
outer = {'__file__': str(pickup_origin)}
exec(compile(pickup_origin.read_text(encoding='utf-8').rsplit("exec(compile(text, str(__file__), 'exec'), execution)", 1)[0], str(pickup_origin), 'exec'), outer)
text = outer['text']
execution = outer['execution']
anchor = "namespace['add'](new_source.relative_to(root).as_posix(), source)"
assert text.count(anchor) == 1
extra = 'pickup_anchor = ' + repr('                row.put("originalThrowPickupAndPersistenceAccepted", false);') + '\nassert source.count(pickup_anchor) == 1\nsource = source.replace(pickup_anchor, ' + repr(block) + ' + pickup_anchor)\n'
text = text.replace(anchor, extra + anchor)
text = text.replace('[Path(__file__), complete_origin,', '[Path(__file__), pickup_origin, pickup_preparation, pickup_observer, complete_origin,')
execution.update({'__file__': str(Path(__file__).resolve()), 'pickup_origin': pickup_origin, 'pickup_preparation': pickup_preparation, 'pickup_observer': pickup_observer})
exec(compile(text, str(__file__), 'exec'), execution)
