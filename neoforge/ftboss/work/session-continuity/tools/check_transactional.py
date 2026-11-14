import subprocess
import sys
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text

receipt = WORK / 'reports/candidate-transactional.json'
data = load(receipt)
attempt = Path(data['attempt'])
arguments = load(attempt / 'complete-arguments316.json')
checks = attempt / 'checks'
source = checks / 'AdvanceCallers.java'
source.write_text('''package net.farmingtales.ftboss.item;
public final class AdvanceCallers {
    public static void main(String[] args) throws Throwable {
        int rejected = 0;
        if (OwnerFieldExpectedAuthority.advance(null, null)) throw new AssertionError("Direct field grant");
        if (OwnerNativeSemanticAuthority.advance(null, null, null)) throw new AssertionError("Direct weapon grant");
        rejected += 2;
        for (var owner : new Class<?>[]{OwnerFieldExpectedAuthority.class, OwnerNativeSemanticAuthority.class}) {
            var parameterTypes = owner == OwnerFieldExpectedAuthority.class
                ? new Class<?>[]{FoodWeaponOriginalSequence.Session.class, FoodWeaponOriginalSequence.Step.class}
                : new Class<?>[]{OwnerNativeSemanticAuthority.Before.class, FoodWeaponOriginalSequence.Session.class, FoodWeaponOriginalSequence.Step.class};
            var method = owner.getDeclaredMethod("advance", parameterTypes);
            method.setAccessible(true);
            var parameters = new Object[parameterTypes.length];
            if (Boolean.TRUE.equals(method.invoke(null, parameters))) throw new AssertionError("Reflective grant");
            if (Boolean.TRUE.equals(java.lang.invoke.MethodHandles.lookup().unreflect(method).invokeWithArguments(parameters))) throw new AssertionError("Handle grant");
            rejected += 2;
        }
        for (int index = 1; index < args.length; index++) {
            Class<?> type = Class.forName(args[index], false, AdvanceCallers.class.getClassLoader());
            type.getDeclaredFields(); type.getDeclaredMethods(); type.getDeclaredConstructors();
        }
        System.out.println("rejected=" + rejected + ", linked=" + (args.length - 1));
        if (rejected != 6) throw new AssertionError("Missing rejection");
    }
}
''', encoding='utf-8')
classpath = str(checks) + ';' + str(attempt / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
commands = [
    ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(checks), str(source)],
    ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', classpath,
        'net.farmingtales.ftboss.item.AdvanceCallers', 'links', *[name.removesuffix('.class').replace('/', '.') for name in data['changedRawClasses']]],
]
for index, command in enumerate(commands):
    with (checks / ('callers-' + str(index) + '.log')).open('wb') as stream:
        result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT)
    save(checks / ('callers-' + str(index) + '-terminal.json'), {'exitCode': result.returncode})
    assert result.returncode == 0, str(checks)
save(WORK / 'reports/checked-transactional.json', {'failures': 0, 'callerRejections': 6, 'linkedClasses': len(data['changedRawClasses']),
    'asm': load(attempt / 'asm.json'), 'activeScopeNegativeCasesAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [receipt, Path(__file__), source]}})
print({'failures': 0, 'callerRejections': 6, 'linkedClasses': len(data['changedRawClasses'])}, flush=True)
