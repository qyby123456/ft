import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
event_origin = work / 'tools/prepare_native_trident_constructor_cleanup_runtime.py'
frozen = json.loads((work / 'reports/native-trident-foundation-gate-preparation-c0607cae.json').read_text(encoding='utf-8'))
assert hashlib.sha256(event_origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(event_origin)]
outer = {'__file__': str(event_origin)}
exec(compile(event_origin.read_text(encoding='utf-8').rsplit("exec(compile(text, str(__file__), 'exec'), namespace)", 1)[0], str(event_origin), 'exec'), outer)
text = outer['text']
execution = outer['namespace']
anchor = "namespace['add'](new_source.relative_to(root).as_posix(), source)"
assert text.count(anchor) == 1
line = '            Object waiters = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "GUARD_WAITERS");'
observation = """            row.put("actualDamageSourceClass", event.getSource().getClass().getName());
            Object captureStorage = readStatic(net.farmingtales.ftboss.item.FoodWeaponFinalDamage.class, "EVENT_CAPTURES");
            java.util.Deque<?> currentCaptures = (java.util.Deque<?>)((ThreadLocal<?>)captureStorage).get();
            row.put("originalEventCaptureCount", currentCaptures.size());
            Object currentCapture = currentCaptures.peekLast();
            if (currentCapture != null) {
                row.put("originalEventCaptureFailed", read(currentCapture, "failed"));
                row.put("originalEventCaptureClosed", read(currentCapture, "closed"));
                row.put("originalEventCapturedProducerCount", ((java.util.List<?>)read(currentCapture, "producers")).size());
                Object binding = read(currentCapture, "originalBinding");
                Object currentRoot = read(binding, "root");
                row.put("originalEventRootSite", String.valueOf(read(currentRoot, "site")));
                row.put("originalEventRootRetired", read(currentRoot, "retired"));
                row.put("originalEventRootClosed", read(currentRoot, "closed"));
                row.put("originalEventBindingClosed", read(binding, "closed"));
            }
"""
extra = 'event_anchor = ' + repr(line) + '\nassert source.count(event_anchor) == 1\nsource = source.replace(event_anchor, ' + repr(observation) + ' + event_anchor)\n'
text = text.replace(anchor, extra + anchor)
text = text.replace('[Path(__file__), lineage_origin, observation_origin, derived_origin, origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), event_origin, lineage_origin, observation_origin, derived_origin, origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]')
execution.update({'__file__': str(Path(__file__).resolve()), 'event_origin': event_origin})
exec(compile(text, str(__file__), 'exec'), execution)
