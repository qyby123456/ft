import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}
checks = {}


def source(build, name):
    path = WORK / ('build/native-repair-' + build + '/sources/net/farmingtales/ftboss/' + name + '.java')
    text = path.read_text(encoding='utf-8-sig')
    hashes[str(path)] = hashlib.sha256(path.read_bytes()).hexdigest()
    return text


for name in ['item/OwnerStatueWearBoundary', 'item/NativeMapPutLateProofExtension', 'mixin/OwnerStatueWearInventoryMixin']:
    assert source('c8790d91', name) == source('2ca083af', name), name
    checks[name] = 'identical complete frozen source'
old = source('c8790d91', 'item/OwnerStatueWearAdmission')
new = source('2ca083af', 'item/OwnerStatueWearAdmission')
start = '    private static boolean storageCaller('
end = '    private static boolean originalCaller('
assert old[old.index(start):old.index(end)] == new[new.index(start):new.index(end)]
checks['originalSaveEntryEncodingReplacementAndTerminalMethods'] = 'identical frozen source region'
old = source('c8790d91', 'item/NativeFoodWeaponWear')
new = source('2ca083af', 'item/NativeFoodWeaponWear')
start = '    static CompoundTag persistenceImage('
assert old[old.index(start):old.index('    public static boolean eligible(')] == new[new.index(start):new.index('    static CompoundTag loadedPersistenceImage(')]
checks['independentLiveExpectedItemAndCanonicalStoreSaveHelpers'] = 'identical frozen source region'
for source_path in [WORK / 'reports/native-save-terminal-focused-evidence.json',
                    WORK / 'reports/native-continuation-acceptance-2ca083af.json', Path(__file__)]:
    hashes[str(source_path)] = hashlib.sha256(source_path.read_bytes()).hexdigest()
result = {
    'candidate': 'native-repair-2ca083af', 'reusedSaveFaultCandidate': 'native-repair-c8790d91',
    'originalSaveProtocolFrozenSourceChecks': checks,
    'newLoadSuccessorMethodsAreOutsideComparedSaveRegions': True,
    'realDeleteShareFaultRepeatedOn2ca083af': False,
    'fullBehavioralEquivalenceOrLoaderFinalBytesClaimed': False,
    'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-save-protocol-reuse-2ca083af.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'unchangedOriginalSaveProtocolSourceChecks': len(checks), 'faultRetestOnNewCandidate': False})
