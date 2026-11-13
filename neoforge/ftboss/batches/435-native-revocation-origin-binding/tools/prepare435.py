import re
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
prior = LOCAL / 'batches/432-native-ranged-fresh-binding/tools'
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
guards = ['FoodWeaponOriginalSequence$Session']
text = (prior / 'build432.py').read_text(encoding='utf-8')
text = text.replace('427-native-recipe-stack-binding', '432-native-ranged-fresh-binding').replace('actual-incremental427', 'actual-incremental432')
text = text.replace('431-native-ranged-fresh-acquisition', '434-native-revocation-origin')
text = text.replace('actual-fresh431-compiled-not-adopted', 'actual-revocation434-compiled-not-adopted').replace('actual-fresh431-sealed-not-adopted', 'actual-revocation434-sealed-not-adopted')
start = text.index('targets = ')
end = text.index('additional_sources = ', start)
text = text[:start] + 'targets = {next(path for path in sources if path.name == ' + repr('FoodWeaponOriginalSequence.java') + '): proposal_root / ' + repr('item/FoodWeaponOriginalSequence.java') + '}' + chr(10) + text[end:]
text = re.sub(r'(?m)^new_guards = .+$', 'new_guards = ' + repr(guards), text)
text = text.replace('len(old_guards) == 204', 'len(old_guards) == 207').replace('len(pins) == 207', 'len(pins) == 208')
text = text.replace("'guardCount': 207", "'guardCount': 208")
text = text.replace('merge-binding432-', 'merge-binding435-').replace("actual-incremental432-experimental.json', {", "actual-incremental435-experimental.json', {")
text = text.replace('progress: independently planned detached ranged buffer semantics with exact native getter allocation provenance; twelve detached plans and four foreign caller denials passed in isolated prototype; real runtime pending', 'progress: 433 native fresh acquisition and timer groups completed; add only first revocation origin diagnostics without changing rejection behavior or game values')
(BATCH / 'tools/build435.py').write_text(text, encoding='utf-8')
text = (prior / 'check432.py').read_text(encoding='utf-8').replace('432', '435')
text = text.replace('427-native-recipe-stack-binding', '432-native-ranged-fresh-binding').replace('actual-incremental427', 'actual-incremental432')
text = text.replace('len(before) == 204 and len(after) == 207', 'len(before) == 207 and len(after) == 208')
text = re.sub(r'(?m)^assert added == .+$', 'assert added == ' + repr(guards), text)
text = text.replace('before.size() != 204', 'before.size() != 207').replace('after.size() != 207', 'after.size() != 208')
text = text.replace('rejected.size() != 414', 'rejected.size() != 416').replace('actual guards=207 rejected=414', 'actual guards=208 rejected=416')
text = text.replace('== 414', '== 416').replace("'guardCount': 207", "'guardCount': 208").replace("'guardMissingOrCorruptRejected': 414", "'guardMissingOrCorruptRejected': 416").replace("'oldGuardKeysPreserved': 204", "'oldGuardKeysPreserved': 207")
text = text.replace("'guards': 207", "'guards': 208").replace("'counterexamplesRejected': 414", "'counterexamplesRejected': 416")
(BATCH / 'tools/check435.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'guards': 208})
