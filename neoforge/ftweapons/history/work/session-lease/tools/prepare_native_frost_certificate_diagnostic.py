import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_frost_session_diagnostic.py'
frozen = json.loads((work / 'reports/native-frost-session-diagnostic-preparation-667b6466.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
helper = origin.read_text(encoding='utf-8')
block = '''
                        Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                        for (java.lang.reflect.Field field : registry.getDeclaredFields()) {
                            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) || !field.getName().toLowerCase(java.util.Locale.ROOT).contains("failure")) continue;
                            field.setAccessible(true);
                            Object failure = field.get(null);
                            publicationRow.put("registry_" + field.getName(), failure == null ? null : failure.toString());
                        }
'''
anchor = "new_source = work / ('tests/frost-session-diagnostic-'"
assert helper.count(anchor) == 1
helper = helper.replace(anchor, 'source = source.replace(anchor, anchor + ' + repr(block) + ')\n' + anchor)
helper = helper.replace('frost-session-diagnostic', 'frost-certificate-diagnostic')
helper = helper.replace('frost_session_diagnostic', 'frost_certificate_diagnostic')
exec(compile(helper, str(__file__), 'exec'))
