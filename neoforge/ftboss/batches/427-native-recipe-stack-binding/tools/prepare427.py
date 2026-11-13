import re
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/424-native-socket-recipe-binding/tools'
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
text = (prior / 'build424.py').read_text(encoding='utf-8')
text = text.replace('421-native-seed-isolation-binding', '424-native-socket-recipe-binding').replace('actual-incremental421', 'actual-incremental424')
text = text.replace('423-native-socket-recipe-private-output', '426-native-recipe-stack-private-ops')
text = text.replace('actual-socket-recipe423-compiled-not-adopted', 'actual-recipe-stack426-compiled-not-adopted')
text = text.replace('actual-socket-recipe423-sealed-not-adopted', 'actual-recipe-stack426-sealed-not-adopted')
start = text.index('targets = ')
end = text.index('additional_sources = ', start)
text = text[:start] + '''targets = {next(path for path in sources if path.name == name): proposal_root / folder / name for name, folder in [('NativeRecipeEvidenceData.java', 'transaction'), ('NativeExpectedFieldObservation.java', 'item')]}
''' + text[end:]
guards = ['transaction/NativeRecipeEvidenceData']
text = re.sub(r'(?m)^new_guards = .+$', 'new_guards = ' + repr(guards), text)
text = text.replace('len(old_guards) == 200', 'len(old_guards) == 203').replace('len(pins) == 203', 'len(pins) == 204')
text = text.replace(''' 'guardCount': 203''', ''' 'guardCount': 204''')
text = text.replace('merge-binding424-', 'merge-binding427-').replace('''actual-incremental424-experimental.json', {''', '''actual-incremental427-experimental.json', {''')
text = text.replace('progress: grouped socket and recipe private outputs; 256 binary alias and 256 unordered recipe ordering cases; four existing methods changed and all others retained', 'progress: 6104 live recipe results and 160 socket fingerprints retained; exact recipe stack serializer call now uses private read ops with original validation and source restoration proof')
(BATCH / 'tools/build427.py').write_text(text, encoding='utf-8')
text = (prior / 'check424.py').read_text(encoding='utf-8').replace('424', '427')
text = text.replace('421-native-seed-isolation-binding', '424-native-socket-recipe-binding').replace('actual-incremental421', 'actual-incremental424')
text = text.replace('len(before) == 200 and len(after) == 203', 'len(before) == 203 and len(after) == 204')
text = re.sub(r'(?m)^assert added == .+$', 'assert added == ' + repr(guards), text)
text = text.replace('before.size() != 200', 'before.size() != 203').replace('after.size() != 203', 'after.size() != 204')
text = text.replace('406', '408').replace('actual guards=203 rejected=408', 'actual guards=204 rejected=408')
text = text.replace(''' 'guardCount': 203''', ''' 'guardCount': 204''').replace(''' 'guards': 203''', ''' 'guards': 204''').replace(''' 'oldGuardKeysPreserved': 200''', ''' 'oldGuardKeysPreserved': 203''')
(BATCH / 'tools/check427.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'classes': 1381, 'guards': 204, 'addedGuards': guards})
