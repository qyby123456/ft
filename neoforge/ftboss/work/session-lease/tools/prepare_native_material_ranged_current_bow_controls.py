import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1]
original=work/'tools/prepare_native_material_ranged_bow_controls_qualified.py'
receipt=work/'reports/native-material-ranged-bow-controls-qualified-preparation-2be7d6c1.json'
frozen=json.loads(receipt.read_text(encoding='utf-8'))
assert hashlib.sha256(original.read_bytes()).hexdigest()==frozen['inputHashes'][str(original)]
product=json.loads((work/'reports/native-repair-ranged-use-candidate.json').read_text(encoding='utf-8'))
tag=Path(product['attempt']).name.removeprefix('native-repair-')
text=original.read_text(encoding='utf-8').replace('2be7d6c1',tag)
anchor='paths=[Path(__file__),helper,';assert text.count(anchor)==1
text=text.replace(anchor,'paths=[Path(__file__),original,receipt,helper,')
namespace={'__file__':str(Path(__file__).resolve()),'original':original,'receipt':receipt}
exec(compile(text,str(original),'exec'),namespace)
