import hashlib
import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
patcher = 'C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'

def add(relative, text):
    assert not (root / relative).exists(), relative
    lines = text.splitlines()
    previous = []
    while lines:
        chunk = []
        length = 0
        while lines and length + len(lines[0]) < 6000:
            line = lines.pop(0)
            chunk.append(line)
            length += len(line) + 1
        assert chunk
        if previous:
            body = '*** Update File: ' + relative + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
            suffix = '*** End of File\n'
        else:
            body = '*** Add File: ' + relative + '\n'
            suffix = ''
        patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
        result = subprocess.run([patcher, '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
        assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
        previous.extend(chunk)

candidate = work / 'reports/native-repair-semantic-noop-candidate.json'
receipt = json.loads(candidate.read_text(encoding='utf-8'))
tag = Path(receipt['attempt']).name.removeprefix('native-repair-')
frozen = work / ('reports/native-repair-semantic-noop-candidate-' + tag + '.json')
assert not frozen.exists()
frozen.write_bytes(candidate.read_bytes())
source = (work / 'tests/paid-melee-noop/PositiveWeapon436.java').read_text(encoding='utf-8')
source = source.replace('new int[]{0, 3}', 'new int[]{0, 1, 2, 3, 4, 5}')
start = source.index('                var connection = new net.minecraft.network.Connection')
finish = source.index('            }\n            report.put("failures", 0);', start)
replacement = '''                CompoundTag evolution = net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossFoodEvolution");
                if (evolution == null) throw new AssertionError("Genuine retained representative lacks original evolution root");
                var saveEvolution = net.farmingtales.ftboss.item.FoodWeaponEvolution.class.getDeclaredMethod("saveData", net.minecraft.world.item.ItemStack.class, CompoundTag.class);
                saveEvolution.setAccessible(true);
                for (int repetition = 0; repetition < 2; repetition++) saveEvolution.invoke(null, actual, evolution.copy());
                if (actual.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != componentBeforeTracking
                    || !beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation
                    || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Identical original evolution save replaced component or retired paid authority");
                row.put("repeatedOriginalEvolutionSavePreservedExactComponentIdentityWholeImageAndLiveAuthority", true);
                if (sample % 2 == 0) {
                    CompoundTag changed = evolution.copy();
                    changed.putLong("LastSocketProc", changed.getLong("LastSocketProc") + 1L);
                    saveEvolution.invoke(null, actual, changed);
                    row.put("actualChangedOriginalEvolutionDataPersisted", changed.equals(net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossFoodEvolution")));
                    saveEvolution.invoke(null, actual, evolution.copy());
                    row.put("wholeImageRestoredByOriginalEvolutionSave", beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)));
                } else {
                    CompoundTag lifecycle = net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossWeaponLifecycle");
                    CompoundTag changed = lifecycle.copy();
                    changed.putInt("WearStep", changed.getInt("WearStep") + 1);
                    net.farmingtales.ftboss.item.NativeLifecycleItemData.commit(actual, "FTBossWeaponLifecycle", lifecycle, changed);
                    row.put("actualChangedOriginalLifecycleDataPersisted", changed.equals(net.farmingtales.ftboss.item.FoodWeaponData.read(actual, "FTBossWeaponLifecycle")));
                    net.farmingtales.ftboss.item.NativeLifecycleItemData.commit(actual, "FTBossWeaponLifecycle", changed, lifecycle);
                    row.put("wholeImageRestoredByOriginalLifecycleCommit", beforeTracking.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual)));
                }
                if (!Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Real component change and restored-image ABA failed to retire original genuine generation");
                if (row.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith("actualChanged") && !Boolean.TRUE.equals(entry.getValue()))
                    || row.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith("wholeImageRestored") && !Boolean.TRUE.equals(entry.getValue())))
                    throw new AssertionError("Mutation/ABA negative control did not perform its real original data writes");
                row.put("realChangeAndRestoredWholeImageAbaNeverRevivedGeneration", true);
                row.put("negativeControlUsesOrdinaryDataMutationNotExpectedGameplayWear", true);
'''
source = source[:start] + replacement + source[finish:]
source = source.replace('report.put("allSixFamilySignedCombatAccepted", false);', 'report.put("allSixFamilySignedCombatAccepted", false); report.put("sixFamilySemanticNoopAndRealMutationAbaRegression", true);')
add('work/session-lease/tests/semantic-noop-regression/PositiveWeapon436.java', source)
launcher = (work / 'tools/run_native_paid_melee_noop.py').read_text(encoding='utf-8')
launcher = launcher.replace('native-repair-melee-noop-candidate-fd5476e2.json', frozen.name)
launcher = launcher.replace('6efebd38bade0bab0f22c70824e36588d342ad58eb69d2992881198451ae24ac', hashlib.sha256(frozen.read_bytes()).hexdigest())
launcher = launcher.replace('tests/paid-melee-noop/PositiveWeapon436.java', 'tests/semantic-noop-regression/PositiveWeapon436.java')
launcher = launcher.replace('native-repair-runtime-paid-melee-noop.json', 'native-repair-runtime-semantic-noop-regression.json')
add('work/session-lease/tools/run_native_semantic_noop_regression.py', launcher)
print(json.dumps({'candidate': str(frozen), 'sha256': hashlib.sha256(frozen.read_bytes()).hexdigest(), 'testFamilies': 6}))
