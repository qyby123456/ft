import subprocess
import sys
import uuid
from pathlib import Path

ROOT = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(ROOT / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = LOCAL / 'batches/421-native-seed-isolation-binding/reports/actual-incremental421-experimental.json'
diagnostic = LOCAL / 'batches/422-native-seed-isolation-runtime/reports/actual-first-rejection422-diagnostic.json'
for receipt in [binding, diagnostic]:
    for name, expected in load(receipt)['inputHashes'].items():
        assert digest(name) == expected, name
prior = Path(load(binding)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
targets = []
originals = []
changed = {}

def once(text, before, after):
    before = before.replace('§', chr(34))
    after = after.replace('§', chr(34))
    assert text.count(before) == 1, before
    return text.replace(before, after)

def region(text, start, end, replacements):
    first = text.index(start)
    last = text.index(end, first + len(start))
    body = text[first:last]
    for before, after in replacements:
        body = once(body, before, after)
    return text[:first] + body + text[last:]

for name in ['FoodWeaponSourceLedger.java', 'FoundationApprovedRules.java', 'NativeExpectedFieldObservation.java']:
    original = next(Path(value) for value in arguments if Path(value).name == name)
    originals.append(original)
    text = original.read_text(encoding='utf-8')
    if name == 'FoodWeaponSourceLedger.java':
        text = region(text, '    public static String socketFingerprint(', '    private static String fingerprint(', [
            ('CompoundTag var2 = new CompoundTag();', 'PrivateRead423 var2 = new PrivateRead423();'),
            ('CompoundTag var8 = new CompoundTag();', 'PrivateRead423 var8 = new PrivateRead423();'),
            ('var4.add(var8);', 'var4.add(var8.finish());'),
            ('return fingerprint(var2);', 'return fingerprint(var2.finish());')])
        changed[name] = ['socketFingerprint']
    elif name == 'FoundationApprovedRules.java':
        text = region(text, '    public String fingerprint(', '    @Override', [
            ('CompoundTag var5 = new CompoundTag();', 'PrivateRead423 var5 = new PrivateRead423();'),
            ('FourWeaponFoundationAdapter.fingerprintTag(var5)', 'FourWeaponFoundationAdapter.fingerprintTag(var5.finish())')])
        text = region(text, '    public static CompoundTag recipeEvidence(', '    private static Ingredient smithingIngredient(', [
            ('CompoundTag var7 = new CompoundTag();', 'PrivateRead423 var7 = new PrivateRead423();'),
            ('CompoundTag var15 = new CompoundTag();', 'PrivateRead423 var15 = new PrivateRead423();'),
            ('CompoundTag var21 = new CompoundTag();', 'PrivateRead423 var21 = new PrivateRead423();'),
            ('var17.add(var21);', 'var17.add(var21.finish());'),
            ('var24.sort(Comparator.comparing(FourWeaponFoundationAdapter::canonical));', 'var24.sort(Comparator.comparing(FoundationApprovedRules::draftCanonical423));'),
            ('CompoundTag var27 = (CompoundTag)var24.get(var26);', 'PrivateRead423 var27 = (PrivateRead423)var24.get(var26);'),
            ('var25.add(var27);', 'var25.add(var27.finish());'),
            ('FourWeaponFoundationAdapter.canonical(var7);', 'CompoundTag evidence423 = var7.finish();' + chr(10) + '                    FourWeaponFoundationAdapter.canonical(evidence423);'),
            ('return var7;', 'return evidence423;')])
        text = region(text, '    private String nativeRecipeFingerprint(', '    }' + chr(10) + '}', [
            ('CompoundTag var6 = new CompoundTag();', 'PrivateRead423 var6 = new PrivateRead423();'),
            ('FourWeaponFoundationAdapter.fingerprintTag(var6)', 'FourWeaponFoundationAdapter.fingerprintTag(var6.finish())')])
        text = text.rstrip()[:-1] + '''
    private static String draftCanonical423(PrivateRead423 draft) {
        return FourWeaponFoundationAdapter.canonical(draft.finish());
    }
}
'''
        changed[name] = ['fingerprint', 'recipeEvidence', 'nativeRecipeFingerprint']
    else:
        marker = '    private static final class ReadRegistries implements net.minecraft.core.HolderLookup.Provider {'
        helper = '''    public static CompoundTag socketRecipeCompoundForRead423(Map<String, Tag> values) {
        StackWalker.StackFrame frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::foundationReadFrame404);
        Class<?> owner = frame == null ? null : frame.getDeclaringClass();
        boolean socket = owner != null && owner.getNestHost() == FoodWeaponSourceLedger.class
            && owner.getName().equals(§net.farmingtales.ftboss.item.FoodWeaponSourceLedger$PrivateRead423§);
        boolean recipe = owner != null
            && owner.getNestHost() == net.farmingtales.ftboss.transaction.FoundationApprovedRules.class
            && owner.getName().equals(§net.farmingtales.ftboss.transaction.FoundationApprovedRules$PrivateRead423§);
        if ((!socket && !recipe) || frame == null || !frame.getMethodName().equals(§finish§)
            || !frame.getDescriptor().equals(§()Lnet/minecraft/nbt/CompoundTag;§)
            || values == null || values.getClass() != java.util.LinkedHashMap.class) {
            throw new IllegalStateException(§Private socket or recipe output requires its authentic unpublished builder§);
        }
        CompoundTag output = newReadCompound();
        Map<String, Tag> privateFields = fields(output);
        for (Map.Entry<String, Tag> entry : values.entrySet()) privateFields.put(entry.getKey(), entry.getValue());
        return output;
    }

'''.replace('§', chr(34))
        text = once(text, marker, helper + marker)
        changed[name] = []
    if name != 'NativeExpectedFieldObservation.java':
        builder = '''
    private static final class PrivateRead423 {
        private final java.util.LinkedHashMap<String, net.minecraft.nbt.Tag> fields = new java.util.LinkedHashMap<>();
        private void put(String key, net.minecraft.nbt.Tag value) { fields.put(key, value); }
        private void putString(String key, String value) { put(key, net.minecraft.nbt.StringTag.valueOf(value)); }
        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }
        private CompoundTag finish() {
            return net.farmingtales.ftboss.item.NativeExpectedFieldObservation.socketRecipeCompoundForRead423(fields);
        }
    }
'''
        text = text.rstrip()[:-1] + builder + '}' + chr(10)
    package = text.split('package ', 1)[1].split(';', 1)[0]
    target = BATCH / 'proposal' / package.replace('.', '/') / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    targets.append(target)
attempt = BATCH / 'build' / ('socket-recipe423-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), *map(str, targets)]
save(attempt / 'compile-command423.json', command)
with (attempt / 'compile423.log').open('wb') as stream:
    terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal423.json', {'exitCode': terminal.returncode})
assert terminal.returncode == 0, str(attempt / 'compile423.log')
save(BATCH / 'reports/actual-socket-recipe423-compiled-not-adopted.json', {
    'attempt': str(attempt), 'failures': 0, 'changedExistingMethods': changed,
    'cumulativeCandidateUpdated': False, 'actualRuntimeTested': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, diagnostic, Path(__file__), *originals, *targets]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')}})
print({'compiled': True, 'attempt': str(attempt), 'changedExistingMethods': 4}, flush=True)
