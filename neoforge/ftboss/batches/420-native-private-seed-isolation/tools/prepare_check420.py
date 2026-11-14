from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/416-native-fingerprint-private-output'
template = (prior / 'tests/Builder416.java.in').read_text(encoding='utf-8').replace('Builder416', 'Builder420')
old = '            if (seeded && actual != privateSeed) throw new AssertionError(§Private seed identity changed§);'
new = '            if (seeded && (actual == privateSeed || !Arrays.equals(privateSeedBefore, binary(privateSeed)))) throw new AssertionError(§Private seed was reused or changed§);'
assert template.count(old) == 1
template = template.replace(old, new)
marker = '            CompoundTag privateSeed = seeded ? seed.copy() : null;'
assert template.count(marker) == 1
template = template.replace(marker, marker + chr(10) + '            byte[] privateSeedBefore = seeded ? binary(privateSeed) : null;')
(BATCH / 'tests/Builder420.java.in').write_text(template, encoding='utf-8')
(BATCH / 'tests/SeedBoundary420.java').write_text((BATCH / 'tests/SeedBoundary420.java.in').read_text(encoding='utf-8').replace('§', chr(34)), encoding='utf-8')
text = (prior / 'tools/check416.py').read_text(encoding='utf-8').replace('Builder416', 'Builder420')
text = text.replace('actual-fingerprint416-compiled-not-adopted', 'actual-seed420-compiled-not-adopted')
text = text.replace('414-food-enchantment-binding', '417-native-fingerprint-binding').replace('actual-incremental414', 'actual-incremental417')
for before, after in [('checks416-', 'checks420-'), ('agent416.jar', 'agent420.jar'), ('actual-builder416', 'actual-builder420'), ('actual-asm416', 'actual-asm420'), ('actual-semantic416', 'actual-semantic420'), ('actual-fingerprint416-sealed-not-adopted', 'actual-seed420-sealed-not-adopted')]:
    text = text.replace(before, after)
start = text.index('    if name == ' + repr('NativeExpectedFieldObservation.java') + ':')
end = text.index('    if not expected', start)
text = text[:start] + '    assert row[' + repr('addedMethods') + '] == [], row' + chr(10) + text[end:]
text = text.replace('assert len(changed) == 5', 'assert len(changed) == 1')
marker = 'run(java[:1] + ['
start = text.index(marker)
extra = '''before_boundary = BATCH / 'reports/actual-seed420-before-boundary.json'
after_boundary = BATCH / 'reports/actual-seed420-after-boundary.json'
old_java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-javaagent:' + str(agent), '-Xverify:all', '-cp', str(output) + ';' + str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])]
run(old_java + ['SeedBoundary420', 'before', str(before_boundary)], 'seed-boundary-before')
run(java[:1] + ['-javaagent:' + str(agent)] + java[1:] + ['SeedBoundary420', 'after', str(after_boundary)], 'seed-boundary-after')
assert load(before_boundary)['reflectivePrivateSeedMutated'] is True
assert load(after_boundary)['reflectivePrivateSeedMutated'] is False
assert load(after_boundary)['subclassSeedRejectedBeforeCallback'] is True
'''
text = text[:start] + extra + text[start:]
text = text.replace('binding, builder, asm, semantic, agent,', 'binding, before_boundary, after_boundary, builder, asm, semantic, agent,')
(BATCH / 'tools/check420.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'beforeAndAfterSeedBoundaryProof': True})
