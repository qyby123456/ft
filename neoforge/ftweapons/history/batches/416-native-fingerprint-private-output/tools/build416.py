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
binding = LOCAL / 'batches/414-food-enchantment-binding/reports/actual-incremental414-experimental.json'
diagnostic = LOCAL / 'batches/415-food-enchantment-runtime/reports/actual-first-rejection415-diagnostic.json'
for receipt in [binding, diagnostic]:
    data = load(receipt)
    for name, expected in {**data['inputHashes'], **data.get('outputHashes', {})}.items():
        assert digest(name) == expected, name
prior = Path(load(binding)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
targets = []
originals = []
changed_methods = {}

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

for name in ['FoundationAuthorRuleSnapshots.java', 'FoundationCanonicalDigest.java', 'FourWeaponFoundationAdapter.java', 'FoodWeaponMaterialFingerprint.java', 'NativeExpectedFieldObservation.java']:
    original = next(Path(value) for value in arguments if Path(value).name == name)
    originals.append(original)
    text = original.read_text(encoding='utf-8')
    if name == 'FoundationAuthorRuleSnapshots.java':
        text = region(text, '    public static String fingerprint(', '    private static FoundationAuthorRuleSnapshots.FrozenRules capture(', [
            ('CompoundTag var4 = new CompoundTag();', 'PrivateFingerprint416 var4 = new PrivateFingerprint416();'),
            ('FoundationCanonicalDigest.canonical(var4)', 'FoundationCanonicalDigest.canonical(var4.finish())'),
            ('CompoundTag var12 = var3.copy();', 'PrivateFingerprint416 var12 = new PrivateFingerprint416(var3.copy());'),
            ('FoundationCanonicalDigest.fingerprint(§author-rules§, var12)', 'FoundationCanonicalDigest.fingerprint(§author-rules§, var12.finish())')])
        text = region(text, '    private static FoundationAuthorRuleSnapshots.FrozenRules capture(', '    private static final class FrozenRules', [
            ('CompoundTag var18 = new CompoundTag();', 'PrivateFingerprint416 var18 = new PrivateFingerprint416();'),
            ('FoundationCanonicalDigest.canonical(var18);', 'CompoundTag snapshot416 = var18.finish();' + chr(10) + '                FoundationCanonicalDigest.canonical(snapshot416);'),
            ('new FoundationAuthorRuleSnapshots.FrozenRules(var18)', 'new FoundationAuthorRuleSnapshots.FrozenRules(snapshot416)')])
        changed_methods[name] = ['fingerprint', 'capture']
    elif name == 'FoundationCanonicalDigest.java':
        text = region(text, '    public static String fingerprint(', '    public static ', [
            ('CompoundTag var2 = new CompoundTag();', 'PrivateFingerprint416 var2 = new PrivateFingerprint416();'),
            ('canonical(var2)', 'canonical(var2.finish())')])
        changed_methods[name] = ['fingerprint']
    elif name == 'FourWeaponFoundationAdapter.java':
        text = region(text, '    public static String compositionFingerprint(', '    public static CompoundTag compositionSnapshot(', [
            ('CompoundTag var3 = new CompoundTag();', 'PrivateSnapshot407 var3 = new PrivateSnapshot407();'),
            ('CompoundTag var8 = new CompoundTag();', 'PrivateSnapshot407 var8 = new PrivateSnapshot407();'),
            ('var4.add(var8);', 'var4.add(var8.finish());'),
            ('FoundationCanonicalDigest.fingerprint(§composition§, var3)', 'FoundationCanonicalDigest.fingerprint(§composition§, var3.finish())')])
        changed_methods[name] = ['compositionFingerprint']
    elif name == 'FoodWeaponMaterialFingerprint.java':
        text = region(text, '    public static String create(', '    public static CompoundTag foundationSnapshot(', [
            ('CompoundTag var2 = new CompoundTag();', 'PrivateFingerprint416 var2 = new PrivateFingerprint416();'),
            ('append(var9, var2, 0, new int[]{0, 0});', 'append(var9, var2.finish(), 0, new int[]{0, 0});')])
        changed_methods[name] = ['create']
    else:
        marker = '    private static final class ReadRegistries implements net.minecraft.core.HolderLookup.Provider {'
        helper = '''    public static CompoundTag fingerprintCompoundForRead416(CompoundTag seed, Map<String, Tag> values) {
        StackWalker.StackFrame frame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(NativeExpectedFieldObservation::foundationReadFrame404);
        Class<?> owner = frame == null ? null : frame.getDeclaringClass();
        boolean author = owner != null
            && owner.getNestHost() == net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots.class
            && owner.getName().equals(§net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots$PrivateFingerprint416§);
        boolean canonical = owner != null
            && owner.getNestHost() == net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest.class
            && owner.getName().equals(§net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest$PrivateFingerprint416§);
        boolean material = owner != null && owner.getNestHost() == FoodWeaponMaterialFingerprint.class
            && owner.getName().equals(§net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint$PrivateFingerprint416§);
        if ((!author && !canonical && !material) || frame == null || !frame.getMethodName().equals(§finish§)
            || !frame.getDescriptor().equals(§()Lnet/minecraft/nbt/CompoundTag;§)
            || values == null || values.getClass() != java.util.LinkedHashMap.class || (seed != null && !author)) {
            throw new IllegalStateException(§Private fingerprint output requires its authentic unpublished builder§);
        }
        CompoundTag output = seed == null ? newReadCompound() : seed;
        Map<String, Tag> privateFields = fields(output);
        for (Map.Entry<String, Tag> entry : values.entrySet()) privateFields.put(entry.getKey(), entry.getValue());
        return output;
    }

'''.replace('§', chr(34))
        text = once(text, marker, helper + marker)
        changed_methods[name] = []
    if name in ['FoundationAuthorRuleSnapshots.java', 'FoundationCanonicalDigest.java', 'FoodWeaponMaterialFingerprint.java']:
        seed_fields = ''
        seed_argument = 'null'
        if name == 'FoundationAuthorRuleSnapshots.java':
            seed_fields = '''        private final CompoundTag seed;
        private PrivateFingerprint416() { this.seed = null; }
        private PrivateFingerprint416(CompoundTag seed) { this.seed = seed; }
'''
            seed_argument = 'seed'
        builder = '''
    private static final class PrivateFingerprint416 {
        private final java.util.LinkedHashMap<String, net.minecraft.nbt.Tag> fields = new java.util.LinkedHashMap<>();
''' + seed_fields + '''        private void put(String key, net.minecraft.nbt.Tag value) { fields.put(key, value); }
        private void putString(String key, String value) { put(key, net.minecraft.nbt.StringTag.valueOf(value)); }
        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }
        private CompoundTag finish() {
            return net.farmingtales.ftboss.item.NativeExpectedFieldObservation.fingerprintCompoundForRead416(''' + seed_argument + ''', fields);
        }
    }
'''
        text = text.rstrip()[:-1] + builder + '}' + chr(10)
    package = text.split('package ', 1)[1].split(';', 1)[0]
    target = BATCH / 'proposal' / package.replace('.', '/') / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    targets.append(target)
attempt = BATCH / 'build' / ('fingerprint416-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), *map(str, targets)]
save(attempt / 'compile-command416.json', command)
with (attempt / 'compile416.log').open('wb') as stream:
    terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal416.json', {'exitCode': terminal.returncode})
assert terminal.returncode == 0, str(attempt / 'compile416.log')
save(BATCH / 'reports/actual-fingerprint416-compiled-not-adopted.json', {
    'attempt': str(attempt), 'failures': 0, 'changedExistingMethods': changed_methods,
    'cumulativeCandidateUpdated': False, 'actualRuntimeTested': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, diagnostic, Path(__file__), *originals, *targets]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')}})
print({'compiled': True, 'attempt': str(attempt), 'existingMethodsChanged': 5}, flush=True)
