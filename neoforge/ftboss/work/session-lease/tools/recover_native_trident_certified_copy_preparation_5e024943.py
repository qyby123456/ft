import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
recovery_origin = work / 'tools/prepare_native_trident_certified_copy_runtime.py'
namespace = {'__file__': str(recovery_origin)}
exec(compile(recovery_origin.read_text(encoding='utf-8').split("exec(compile(text, str(__file__), 'exec'))")[0], str(recovery_origin), 'exec'), namespace)
text = namespace['text']
text = text.replace('assert not binding.exists()\nbinding.write_bytes(candidate.read_bytes())', 'assert binding.read_bytes() == candidate.read_bytes()')
text = text.replace("namespace['add'](new_source.relative_to(root).as_posix(), source)", "assert new_source.read_text(encoding='utf-8') == source")
text = text.replace('[Path(__file__), origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), recovery_origin, origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]')
exec(compile(text, str(__file__), 'exec'))
