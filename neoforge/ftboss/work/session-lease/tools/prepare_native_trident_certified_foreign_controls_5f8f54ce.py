import hashlib
from pathlib import Path
work=Path(__file__).resolve().parents[1]
origin=work/'tools/prepare_native_trident_foreign_controls_runtime.py'
assert hashlib.sha256(origin.read_bytes()).hexdigest()=='6d5d4861f1a772dd3d017b6e1e701605e65fb47eec0811c5e38ccc0187af9d76'
text=origin.read_text(encoding='utf-8')
anchor="    namespace['add'](new_source.relative_to(root).as_posix(), source)"
assert text.count(anchor)==1
text=text.replace(anchor,'    source = source.replace(\'                Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");\', \'                Class<?> foreignGuard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");\').replace(\'var refresh = guard.getDeclaredMethod(\', \'var refresh = foreignGuard.getDeclaredMethod(\')\n    anchor = "save(attempt / \'inputs-before308.json\', inputs)"\n    head, tail = code.rsplit(anchor, 1)\n    code = head + "inputs[str(WORK / \'reports/native-trident-foreign-controls-preparation-5f8f54ce.json\')] = digest(WORK / \'reports/native-trident-foreign-controls-preparation-5f8f54ce.json\')\\n" + anchor + tail\n'+anchor)
text=text.replace('paths = [Path(__file__), helper,', 'paths = [Path(__file__), origin, helper,')
namespace={'__file__':__file__,'origin':origin}
exec(compile(text,str(origin),'exec'),namespace)
