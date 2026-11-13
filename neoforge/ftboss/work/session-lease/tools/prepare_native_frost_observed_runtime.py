import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_frost_ingress_runtime.py'
helper = origin.read_text(encoding='utf-8')
diagnostic = (work / 'tests/frost-certificate-diagnostic-d036c8bb/PositiveWeapon436.java').read_text(encoding='utf-8')
trace_anchor = '                        publicationRow.put("traceComplete", complete.invoke(session));'
start = diagnostic.index(trace_anchor) + len(trace_anchor)
finish = diagnostic.index('\n                    }\n', start)
block = diagnostic[start:finish]
anchor = "add('work/session-lease/tests/paid-frost-ingress-'"
assert helper.count(anchor) == 1
helper = helper.replace(anchor, 'source = source.replace(' + repr(trace_anchor) + ', ' + repr(trace_anchor + block) + ')\n' + anchor)
exec(compile(helper, str(__file__), 'exec'))
output = work / ('reports/native-frost-observed-preparation-' + tag + '.json')
assert not output.exists()
paths = [Path(__file__), origin, work / 'tests/frost-certificate-diagnostic-d036c8bb/PositiveWeapon436.java', work / ('tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java'), work / ('tools/run_native_paid_frost_ingress_' + tag + '.py')]
output.write_text(json.dumps({'sameOriginalRayFixtureWithReadOnlyCertificateAndSessionDiagnostics': True, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
