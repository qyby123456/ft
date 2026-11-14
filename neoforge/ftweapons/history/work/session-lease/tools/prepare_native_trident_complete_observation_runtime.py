import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
complete_origin = work / 'tools/prepare_native_trident_strict_session_runtime.py'
frozen = json.loads((work / 'reports/native-trident-foundation-gate-preparation-338f0feb.json').read_text(encoding='utf-8'))
assert hashlib.sha256(complete_origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(complete_origin)]
revocation_preparation = work / 'reports/native-trident-sequence-revocation-preparation-338f0feb.json'
revocation = json.loads(revocation_preparation.read_text(encoding='utf-8'))
revocation_observer = Path(revocation['newObserver'])
assert hashlib.sha256(revocation_observer.read_bytes()).hexdigest() == revocation['inputHashes'][str(revocation_observer)]
diagnostic = revocation_observer.read_text(encoding='utf-8')
first = diagnostic.index('                        Object revocationSite =')
last = diagnostic.index('                        Object observationFailure =', first)
observation = diagnostic[first:last]
outer = {'__file__': str(complete_origin)}
exec(compile(complete_origin.read_text(encoding='utf-8').rsplit("exec(compile(text, str(__file__), 'exec'), execution)", 1)[0], str(complete_origin), 'exec'), outer)
text = outer['text']
execution = outer['execution']
anchor = "namespace['add'](new_source.relative_to(root).as_posix(), source)"
assert text.count(anchor) == 1
extra = "source = source.replace('ItemStack copiedPickup', 'net.minecraft.world.item.ItemStack copiedPickup').replace('((ItemStack)foreignCustody', '((net.minecraft.world.item.ItemStack)foreignCustody')\n"
extra += 'revocation_anchor = ' + repr('                        Object observationFailure = read(session, "firstObservationFailure");') + '\nassert source.count(revocation_anchor) == 1\nsource = source.replace(revocation_anchor, ' + repr(observation) + ' + revocation_anchor)\n'
text = text.replace(anchor, extra + anchor)
text = text.replace('[Path(__file__), strict_origin,', '[Path(__file__), complete_origin, revocation_preparation, revocation_observer, strict_origin,')
execution.update({'__file__': str(Path(__file__).resolve()), 'complete_origin': complete_origin, 'revocation_preparation': revocation_preparation, 'revocation_observer': revocation_observer})
exec(compile(text, str(__file__), 'exec'), execution)
