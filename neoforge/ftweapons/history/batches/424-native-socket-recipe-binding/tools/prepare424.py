from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/421-native-seed-isolation-binding/tools'
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
text = (prior / 'build421.py').read_text(encoding='utf-8')
text = text.replace('417-native-fingerprint-binding', '421-native-seed-isolation-binding').replace('actual-incremental417', 'actual-incremental421')
text = text.replace('420-native-private-seed-isolation', '423-native-socket-recipe-private-output')
text = text.replace('actual-seed420-compiled-not-adopted', 'actual-socket-recipe423-compiled-not-adopted')
text = text.replace('actual-seed420-sealed-not-adopted', 'actual-socket-recipe423-sealed-not-adopted')
start = text.index('targets = ')
end = text.index('additional_sources = ', start)
text = text[:start] + '''targets = {next(path for path in sources if path.name == name): proposal_root / folder / name for name, folder in [('FoodWeaponSourceLedger.java', 'item'), ('FoundationApprovedRules.java', 'transaction'), ('NativeExpectedFieldObservation.java', 'item')]}
''' + text[end:]
guards = ['FoodWeaponSourceLedger$PrivateRead423', 'transaction/FoundationApprovedRules', 'transaction/FoundationApprovedRules$PrivateRead423']
text = text.replace('new_guards = []', 'new_guards = ' + repr(guards))
old_path = 'graph[' + repr('net/farmingtales/ftboss/item/') + ' + guard + ' + repr('.class') + ']'
new_path = 'graph[(' + repr('net/farmingtales/ftboss/') + ' if guard.startswith(' + repr('transaction/') + ') else ' + repr('net/farmingtales/ftboss/item/') + ') + guard + ' + repr('.class') + ']'
assert text.count(old_path) == 1
text = text.replace(old_path, new_path)
text = text.replace('    additions = []', '    additions = []' + chr(10) + '    route_replacement = []')
marker = '    if not replacements and not additions:'
route_code = '''    if name == 'NativeCapabilityInstallationProof170.java':
        old_route = 'entry.getKey().startsWith(§mixin/§)'
        new_route = old_route + ''.join(' || entry.getKey().equals(§' + guard + '§)' for guard in new_guards if guard.startswith('transaction/'))
        assert text.count(old_route) == 1
        text = text.replace(old_route, new_route)
        route_replacement = [old_route, new_route]
'''.replace('§', chr(34))
text = text.replace(marker, route_code + marker)
text = text.replace(''' 'guardRouteReplacement': []''', ''' 'guardRouteReplacement': route_replacement''')
text = text.replace('len(pins) == 200', 'len(pins) == 203').replace('1379', '1381')
text = text.replace(''' 'guardCount': 200''', ''' 'guardCount': 203''')
text = text.replace('''name.startswith(('mixin/', 'util/'))''', '''name.startswith(('mixin/', 'util/', 'transaction/'))''')
text = text.replace('merge-binding421-', 'merge-binding424-').replace('''actual-incremental421-experimental.json', {''', '''actual-incremental424-experimental.json', {''')
text = text.replace('progress: five fingerprint output blockers passed in runtime419; reflected seed laundering reproduced and isolated with exact-class copies and subclass callback rejection', 'progress: grouped socket and recipe private outputs; 256 binary alias and 256 unordered recipe ordering cases; four existing methods changed and all others retained')
(BATCH / 'tools/build424.py').write_text(text, encoding='utf-8')
text = (prior / 'check421.py').read_text(encoding='utf-8').replace('421', '424')
text = text.replace('417-native-fingerprint-binding', '421-native-seed-isolation-binding').replace('actual-incremental417', 'actual-incremental421')
text = text.replace('len(before) == 200 and len(after) == 200', 'len(before) == 200 and len(after) == 203')
text = text.replace('assert added == []', 'assert added == ' + repr(sorted(guards)))
text = text.replace('after.size() != 200', 'after.size() != 203')
text = text.replace('400', '406').replace('1379', '1381')
text = text.replace('actual guards=200 rejected=406', 'actual guards=203 rejected=406')
text = text.replace(''' 'guardCount': 200''', ''' 'guardCount': 203''').replace(''' 'guards': 200''', ''' 'guards': 203''')
text = text.replace('name.startsWith(§util/§)'.replace('§', chr(34)), 'name.startsWith(§util/§) || name.startsWith(§transaction/§)'.replace('§', chr(34)))
(BATCH / 'tools/check424.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'classes': 1381, 'guards': 203, 'addedGuards': guards})
