from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/414-food-enchantment-binding/tools'
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
text = (prior / 'build414.py').read_text(encoding='utf-8')
text = text.replace('411-six-weapon-binding', '417-native-fingerprint-binding').replace('actual-incremental411', 'actual-incremental417')
text = text.replace('413-food-enchantment-removal', '420-native-private-seed-isolation')
text = text.replace('actual-enchantment413-compiled-not-adopted', 'actual-seed420-compiled-not-adopted')
text = text.replace('actual-enchantment413-checked-not-adopted', 'actual-seed420-sealed-not-adopted')
start = text.index('targets = ')
end = text.index('additional_sources = ', start)
text = text[:start] + '''targets = {next(path for path in sources if path.name == 'NativeExpectedFieldObservation.java'): proposal_root / 'item/NativeExpectedFieldObservation.java'}
''' + text[end:]
text = text.replace('len(old_guards) == 193', 'len(old_guards) == 200').replace('len(pins) == 193', 'len(pins) == 200')
text = text.replace('1376', '1379').replace('merge-binding414-', 'merge-binding421-')
text = text.replace('''actual-incremental414-experimental.json', {''', '''actual-incremental421-experimental.json', {''')
text = text.replace(''' 'guardCount': 193''', ''' 'guardCount': 200''')
text = text.replace('progress: food enchantment implementations and UI removed; exact synthetic lambda bijection and retained business differential passed', 'progress: five fingerprint output blockers passed in runtime419; reflected seed laundering reproduced and isolated with exact-class copies and subclass callback rejection')
(BATCH / 'tools/build421.py').write_text(text, encoding='utf-8')
text = (prior / 'check414.py').read_text(encoding='utf-8').replace('414', '421')
text = text.replace('411-six-weapon-binding', '417-native-fingerprint-binding').replace('actual-incremental411', 'actual-incremental417')
text = text.replace('193', '200').replace('386', '400').replace('1376', '1379')
(BATCH / 'tools/check421.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'classes': 1379, 'guardCount': 200})
