import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_frost_session_diagnostic.py'
frozen = json.loads((work / 'reports/native-frost-session-diagnostic-preparation-667b6466.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
helper = origin.read_text(encoding='utf-8')
previous_observer = (work / 'tests/paid-melee-coverage-diagnostic/PositiveWeapon436.java').read_text(encoding='utf-8')
start = previous_observer.index('                        Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");')
finish = previous_observer.index('                    }', previous_observer.index('                        publicationRow.put("registryLateStages"', start))
block = previous_observer[start:finish]
anchor = "new_source = work / ('tests/frost-session-diagnostic-'"
assert helper.count(anchor) == 1
helper = helper.replace(anchor, 'source = source.replace(anchor, anchor + ' + repr('\n' + block) + ')\n' + anchor)
helper = helper.replace('frost-session-diagnostic', 'frost-coverage-diagnostic')
helper = helper.replace('frost_session_diagnostic', 'frost_coverage_diagnostic')
helper = helper.replace('    assert not path.exists(), path', '    if path.exists():\n        assert path.read_text(encoding="utf-8") == text.rstrip("\\n") + "\\n", path\n        return')
exec(compile(helper, str(__file__), 'exec'))
