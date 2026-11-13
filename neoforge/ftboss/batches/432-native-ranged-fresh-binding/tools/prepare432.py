import re
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
prior = LOCAL / 'batches/427-native-recipe-stack-binding/tools'
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
guards = ['FoodWeaponExpectedTrace$Builder', 'NativeFreshFactoryTrust298', 'OwnerTimerSemanticAuthority$Book']
text = (prior / 'build427.py').read_text(encoding='utf-8')
text = text.replace('424-native-socket-recipe-binding', '427-native-recipe-stack-binding').replace('actual-incremental424', 'actual-incremental427')
text = text.replace('426-native-recipe-stack-private-ops', '431-native-ranged-fresh-acquisition')
text = text.replace('actual-recipe-stack426-compiled-not-adopted', 'actual-fresh431-compiled-not-adopted').replace('actual-recipe-stack426-sealed-not-adopted', 'actual-fresh431-sealed-not-adopted')
start = text.index('targets = ')
end = text.index('additional_sources = ', start)
names = ['FoodWeaponExpectedTrace.java', 'OwnerTimerSemanticAuthority.java', 'NativeFreshFactoryTrust298.java', 'NativeExpectedFieldObservation.java']
text = text[:start] + 'targets = {next(path for path in sources if path.name == name): proposal_root / ' + repr('item') + ' / name for name in ' + repr(names) + '}' + chr(10) + text[end:]
text = re.sub(r'(?m)^new_guards = .+$', 'new_guards = ' + repr(guards), text)
text = text.replace('len(old_guards) == 203', 'len(old_guards) == 204').replace('len(pins) == 204', 'len(pins) == 207')
text = text.replace("'guardCount': 204", "'guardCount': 207")
text = text.replace('merge-binding427-', 'merge-binding432-').replace("actual-incremental427-experimental.json', {", "actual-incremental432-experimental.json', {")
text = text.replace('progress: 6104 live recipe results and 160 socket fingerprints retained; exact recipe stack serializer call now uses private read ops with original validation and source restoration proof', 'progress: independently planned detached ranged buffer semantics with exact native getter allocation provenance; twelve detached plans and four foreign caller denials passed in isolated prototype; real runtime pending')
(BATCH / 'tools/build432.py').write_text(text, encoding='utf-8')
text = (prior / 'check427.py').read_text(encoding='utf-8').replace('427', '432')
text = text.replace('424-native-socket-recipe-binding', '427-native-recipe-stack-binding').replace('actual-incremental424', 'actual-incremental427')
text = text.replace('len(before) == 203 and len(after) == 204', 'len(before) == 204 and len(after) == 207')
text = re.sub(r'(?m)^assert added == .+$', 'assert added == ' + repr(guards), text)
text = text.replace('before.size() != 203', 'before.size() != 204').replace('after.size() != 204', 'after.size() != 207')
text = text.replace('rejected.size() != 408', 'rejected.size() != 414').replace('actual guards=204 rejected=408', 'actual guards=207 rejected=414')
text = text.replace('== 408', '== 414').replace("'guardCount': 204", "'guardCount': 207").replace("'guardMissingOrCorruptRejected': 408", "'guardMissingOrCorruptRejected': 414").replace("'oldGuardKeysPreserved': 203", "'oldGuardKeysPreserved': 204")
text = text.replace("'guards': 204", "'guards': 207").replace("'counterexamplesRejected': 408", "'counterexamplesRejected': 414")
(BATCH / 'tools/check432.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'guards': 207, 'newGuards': guards})
