import re
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
text = (ROOT / 'batches/435-native-revocation-origin-binding/tools/build435.py').read_text(encoding='utf-8')
text = text.replace('432-native-ranged-fresh-binding', '435-native-revocation-origin-binding').replace('actual-incremental432', 'actual-incremental435')
text = text.replace('batches\\434-native-revocation-origin', 'work\\session-chain').replace('batches/434-native-revocation-origin', 'work/session-chain')
text = text.replace('actual-revocation434-compiled-not-adopted.json', 'functional-compiled.json').replace('actual-revocation434-sealed-not-adopted.json', 'functional-checked.json')
text = re.sub(r'(?m)^prototype = .+$', 'prototype = Path(' + repr((WORK / 'reports/functional-compiled.json').as_posix()) + ')', text)
text = re.sub(r'(?m)^evidence = .+$', 'evidence = Path(' + repr((WORK / 'reports/functional-checked.json').as_posix()) + ')', text)
text = re.sub(r'(?m)^proposal_root = .+$', 'proposal_root = Path(' + repr((WORK / 'proposal/net/farmingtales/ftboss').as_posix()) + ')', text)
start = text.index('targets = ')
end = text.index('additional_sources = ', start)
text = text[:start] + "targets = {next(path for path in sources if path.name == name): proposal_root / 'item' / name for name in ['OwnerFullRepairGuard.java', 'NativeExpectedFieldObservation.java']}" + chr(10) + text[end:]
text = re.sub(r'(?m)^new_guards = .+$', 'new_guards = []', text)
text = text.replace('len(old_guards) == 207', 'len(old_guards) == 208')
text = text.replace('merge-binding435-', 'current-').replace("actual-incremental435-experimental.json', {", "actual-current.json', {")
start = text.index('baseline = load(ROOT / ')
end = text.index('prior = ', start)
text = text[:start] + text[end:]
text = text.replace('progress: 433 native fresh acquisition and timer groups completed; add only first revocation origin diagnostics without changing rejection behavior or game values', 'functional fix: keep independently planned native ranged writes within player domain continuity; reject other callbacks, owners, phases and raw escapes; milestone positive runtime pending')
(WORK / 'tools/build_current.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'newGuards': 0, 'priorEvidenceReused': 435})
