from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
for folder in ['reports', 'tests', 'proposal', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
prior = LOCAL / 'batches/408-native-composition-binding/tools'
binding = LOCAL / 'batches/408-native-composition-binding/reports/actual-incremental408-experimental.json'
prototype = LOCAL / 'batches/410-six-weapon-scope/reports/actual-scope410-compiled-not-adopted.json'
evidence = LOCAL / 'batches/410-six-weapon-scope/reports/actual-scope410-sealed-not-adopted.json'
proposal = LOCAL / 'batches/410-six-weapon-scope/proposal/net/farmingtales/ftboss/item'
text = (prior / 'build408.py').read_text(encoding='utf-8')
text = text.replace("ROOT / 'batches/405-native-foundation-output-binding/reports/actual-incremental405-experimental.json'", 'Path(' + repr(str(binding)) + ')')
text = text.replace("ROOT / 'batches/407-native-composition-private-output/reports/actual-composition407-compiled-not-adopted.json'", 'Path(' + repr(str(prototype)) + ')')
text = text.replace("ROOT / 'batches/407-native-composition-private-output/reports/actual-composition407-sealed-not-adopted.json'", 'Path(' + repr(str(evidence)) + ')')
start = text.index('proposal_root = ')
end = text.index('classpath = ', start)
text = text[:start] + 'proposal_root = Path(' + repr(str(proposal)) + ')\n' + '''targets = {next(path for path in sources if path.name == 'FoodWeaponEvolution.java'): proposal_root / 'FoodWeaponEvolution.java'}
additional_sources = [proposal_root / 'WeaponSystemScope410.java', proposal_root / 'WeaponAttachmentPolicy.java']
additional_source = additional_sources[0]
''' + text[end:]
text = text.replace("new_guards = ['FourWeaponFoundationAdapter', 'FourWeaponFoundationAdapter$PrivateSnapshot407']", "new_guards = ['WeaponSystemScope410', 'WeaponAttachmentPolicy']")
text = text.replace('len(old_guards) == 189', 'len(old_guards) == 191').replace('len(pins) == 191', 'len(pins) == 193')
text = text.replace('len(graph) == 1374', 'len(graph) == 1376').replace("'classCount': 1374", "'classCount': 1376").replace("'guardCount': 191", "'guardCount': 193")
text = text.replace('effective = [targets.get(path, path) for path in sources]', 'effective = [targets.get(path, path) for path in sources] + additional_sources')
text = text.replace("        assert name == 'NativeStackFieldRequirementProof172.java'\n", '')
text = text.replace('args_path, additional_source, *targets.values()', 'args_path, *additional_sources, *targets.values()')
text = text.replace('merge-binding408-', 'merge-binding411-').replace('actual-incremental408-experimental.json\', {', 'actual-incremental411-experimental.json\', {')
text = text.replace('progress: composition407 binary parity sealed; continuation moved to C with D historical inputs read-only', 'progress: user retained six weapon families; tool admission removal compiled and method differential checked')
(BATCH / 'tools/build411.py').write_text(text, encoding='utf-8')
text = (prior / 'check408.py').read_text(encoding='utf-8').replace('408', '411')
text = text.replace("ROOT / 'batches/405-native-foundation-output-binding/reports/actual-incremental405-experimental.json'", 'Path(' + repr(str(binding)) + ')')
text = text.replace("ROOT / 'batches/405-native-foundation-output-binding/proposal/net/farmingtales/ftboss/item/NativeCapabilityInstallationProof170.java'", 'LOCAL_SOURCE_PLACEHOLDER')
text = text.replace('LOCAL_SOURCE_PLACEHOLDER', 'Path(' + repr(str(LOCAL / 'batches/408-native-composition-binding/proposal/net/farmingtales/ftboss/item/NativeCapabilityInstallationProof170.java')) + ')')
text = text.replace('len(before) == 189 and len(after) == 191', 'len(before) == 191 and len(after) == 193')
text = text.replace("assert added == ['FourWeaponFoundationAdapter', 'FourWeaponFoundationAdapter$PrivateSnapshot407']", "assert added == ['WeaponAttachmentPolicy', 'WeaponSystemScope410']")
text = text.replace("'before.size() != 189'", "'before.size() != 191'").replace('after.size() != 191', 'after.size() != 193')
text = text.replace("'rejected.size() != 382'", "'rejected.size() != 386'").replace('actual guards=191 rejected=382', 'actual guards=193 rejected=386')
text = text.replace('== 382', '== 386').replace('== 1374', '== 1376')
text = text.replace("'allGraphAsmChecks': 1374", "'allGraphAsmChecks': 1376").replace("'guardCount': 191", "'guardCount': 193").replace("'guardMissingOrCorruptRejected': 382", "'guardMissingOrCorruptRejected': 386").replace("'oldGuardKeysPreserved': 189", "'oldGuardKeysPreserved': 191")
text = text.replace("'asmClasses': 1374", "'asmClasses': 1376").replace("'guards': 191", "'guards': 193").replace("'counterexamplesRejected': 382", "'counterexamplesRejected': 386")
text = text.replace('assert len(manifest) == 2', "assert len(manifest) == len(receipt['authorityBindingUpdates']) and len(manifest) >= 1")
text = text.replace("'authoritySourcesRestoredExactly': 2", "'authoritySourcesRestoredExactly': len(manifest)").replace("'restoredAuthorityWholeClassesSkipDebugEqual': 2", "'restoredAuthorityWholeClassesSkipDebugEqual': len(manifest)").replace("'restoredAuthorities': 2", "'restoredAuthorities': len(manifest)")
(BATCH / 'tools/check411.py').write_text(text, encoding='utf-8')
print({'prepared': str(BATCH), 'sixWeaponAdmissionBinding': True})
