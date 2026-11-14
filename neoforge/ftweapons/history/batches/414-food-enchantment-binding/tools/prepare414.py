from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/411-six-weapon-binding/tools'
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
text = (prior / 'build411.py').read_text(encoding='utf-8')
text = text.replace('408-native-composition-binding', '411-six-weapon-binding').replace('actual-incremental408', 'actual-incremental411')
text = text.replace('410-six-weapon-scope/reports/actual-scope410-compiled-not-adopted.json', '413-food-enchantment-removal/reports/actual-enchantment413-compiled-not-adopted.json')
text = text.replace('410-six-weapon-scope/reports/actual-scope410-sealed-not-adopted.json', '413-food-enchantment-removal/reports/actual-enchantment413-checked-not-adopted.json')
text = text.replace('410-six-weapon-scope\\\\reports\\\\actual-scope410-compiled-not-adopted.json', '413-food-enchantment-removal\\\\reports\\\\actual-enchantment413-compiled-not-adopted.json')
text = text.replace('410-six-weapon-scope\\\\reports\\\\actual-scope410-sealed-not-adopted.json', '413-food-enchantment-removal\\\\reports\\\\actual-enchantment413-checked-not-adopted.json')
start = text.index('prototype = ')
end = text.index('for receipt in ', start)
text = text[:start] + 'prototype = Path(' + repr(str(LOCAL / 'batches/413-food-enchantment-removal/reports/actual-enchantment413-compiled-not-adopted.json')) + ')\nevidence = Path(' + repr(str(LOCAL / 'batches/413-food-enchantment-removal/reports/actual-enchantment413-checked-not-adopted.json')) + ')\n' + text[end:]
start = text.index('proposal_root = ')
end = text.index('classpath = ', start)
text = text[:start] + 'proposal_root = Path(' + repr(str(LOCAL / 'batches/413-food-enchantment-removal/proposal/net/farmingtales/ftboss')) + ')\n' + '''targets = {next(path for path in sources if path.name == name): proposal_root / folder / name for name, folder in [('FoodWeaponEvolution.java', 'item'), ('ArmorFoodBlessings.java', 'item'), ('StatueHubScreen.java', 'client'), ('OrdinaryStatueHubScreen.java', 'client')]}
additional_sources = []
''' + text[end:]
text = text.replace("new_guards = ['WeaponSystemScope410', 'WeaponAttachmentPolicy']", 'new_guards = []')
text = text.replace('len(old_guards) == 191', 'len(old_guards) == 193')
text = text.replace('merge-binding411-', 'merge-binding414-').replace('actual-incremental411-experimental.json\', {', 'actual-incremental414-experimental.json\', {')
text = text.replace('progress: user retained six weapon families; tool admission removal compiled and method differential checked', 'progress: food enchantment implementations and UI removed; exact synthetic lambda bijection and retained business differential passed')
(BATCH / 'tools/build414.py').write_text(text, encoding='utf-8')
text = (prior / 'check411.py').read_text(encoding='utf-8').replace('411', '414')
text = text.replace('408-native-composition-binding', '411-six-weapon-binding').replace('actual-incremental408', 'actual-incremental411')
text = text.replace('len(before) == 191 and len(after) == 193', 'len(before) == 193 and len(after) == 193')
text = text.replace("assert added == ['WeaponAttachmentPolicy', 'WeaponSystemScope410']", 'assert added == []')
text = text.replace("'before.size() != 191'", "'before.size() != 193'")
text = text.replace("'oldGuardKeysPreserved': 191", "'oldGuardKeysPreserved': 193")
(BATCH / 'tools/check414.py').write_text(text, encoding='utf-8')
print({'prepared': str(BATCH), 'enchantmentRemovalBinding': True})
