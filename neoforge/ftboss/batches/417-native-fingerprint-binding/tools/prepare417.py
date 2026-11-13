from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
prior = LOCAL / 'batches/414-food-enchantment-binding/tools'
text = (prior / 'build414.py').read_text(encoding='utf-8')
text = text.replace('411-six-weapon-binding', '414-food-enchantment-binding').replace('actual-incremental411', 'actual-incremental414')
text = text.replace('413-food-enchantment-removal', '416-native-fingerprint-private-output')
text = text.replace('actual-enchantment413-compiled-not-adopted', 'actual-fingerprint416-compiled-not-adopted')
text = text.replace('actual-enchantment413-checked-not-adopted', 'actual-fingerprint416-sealed-not-adopted')
start = text.index('targets = ')
end = text.index('additional_sources = ', start)
text = text[:start] + '''targets = {next(path for path in sources if path.name == name): proposal_root / folder / name for name, folder in [('FoundationAuthorRuleSnapshots.java', 'util/foundations'), ('FoundationCanonicalDigest.java', 'util/foundations'), ('FourWeaponFoundationAdapter.java', 'item'), ('FoodWeaponMaterialFingerprint.java', 'item'), ('NativeExpectedFieldObservation.java', 'item')]}
''' + text[end:]
new_guards = ['util/foundations/FoundationAuthorRuleSnapshots',
    'util/foundations/FoundationAuthorRuleSnapshots$FrozenRules',
    'util/foundations/FoundationAuthorRuleSnapshots$MaterialSnapshot',
    'util/foundations/FoundationAuthorRuleSnapshots$PrivateFingerprint416',
    'util/foundations/FoundationCanonicalDigest$PrivateFingerprint416',
    'FoodWeaponMaterialFingerprint', 'FoodWeaponMaterialFingerprint$PrivateFingerprint416']
text = text.replace('new_guards = []', 'new_guards = ' + repr(new_guards))
old_path = 'graph[' + repr('net/farmingtales/ftboss/item/') + ' + guard + ' + repr('.class') + ']'
new_path = 'graph[(' + repr('net/farmingtales/ftboss/') + ' if guard.startswith(' + repr('util/') + ') else ' + repr('net/farmingtales/ftboss/item/') + ') + guard + ' + repr('.class') + ']'
assert text.count(old_path) == 1
text = text.replace(old_path, new_path)
text = text.replace('    additions = []', '    additions = []' + chr(10) + '    route_replacement = []')
marker = '    if not replacements and not additions:'
route_code = '''    if name == 'NativeCapabilityInstallationProof170.java':
        old_route = 'entry.getKey().equals(§util/foundations/FoundationCanonicalDigest$PrivateCompound§)'
        new_route = old_route + ''.join(' || entry.getKey().equals(§' + guard + '§)' for guard in new_guards if guard.startswith('util/'))
        assert text.count(old_route) == 1
        text = text.replace(old_route, new_route)
        route_replacement = [old_route, new_route]
'''.replace('§', chr(34))
text = text.replace(marker, route_code + marker)
text = text.replace(''' 'guardRouteReplacement': []''', ''' 'guardRouteReplacement': route_replacement''')
text = text.replace('len(graph) == 1376', 'len(graph) == 1379').replace('len(pins) == 193', 'len(pins) == 200')
text = text.replace(''' 'classCount': 1376''', ''' 'classCount': 1379''').replace(''' 'guardCount': 193''', ''' 'guardCount': 200''')
text = text.replace('merge-binding414-', 'merge-binding417-').replace('''actual-incremental414-experimental.json', {''', '''actual-incremental417-experimental.json', {''')
text = text.replace('progress: food enchantment implementations and UI removed; exact synthetic lambda bijection and retained business differential passed', 'progress: grouped five private fingerprint output methods; 640 binary alias and 128 digest parity cases; all other methods retained')
(BATCH / 'tools/build417.py').write_text(text, encoding='utf-8')
text = (prior / 'check414.py').read_text(encoding='utf-8').replace('414', '417')
text = text.replace('411-six-weapon-binding', '414-food-enchantment-binding').replace('actual-incremental411', 'actual-incremental414')
text = text.replace('len(before) == 193 and len(after) == 193', 'len(before) == 193 and len(after) == 200')
text = text.replace('assert added == []', 'assert added == ' + repr(sorted(new_guards)))
text = text.replace('after.size() != 193', 'after.size() != 200')
text = text.replace('386', '400').replace('1376', '1379')
text = text.replace('actual guards=193 rejected=400', 'actual guards=200 rejected=400')
text = text.replace(''' 'guardCount': 193''', ''' 'guardCount': 200''').replace(''' 'guards': 193''', ''' 'guards': 200''')
(BATCH / 'tools/check417.py').write_text(text, encoding='utf-8')
print({'prepared': str(BATCH), 'newGuardKeys': new_guards})
