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

candidate = work / 'reports/native-repair-frost-ingress-candidate.json'
receipt = json.loads(candidate.read_text(encoding='utf-8'))
tag = Path(receipt['attempt']).name.removeprefix('native-repair-')
frozen = work / ('reports/native-repair-frost-ingress-candidate-' + tag + '.json')
assert not frozen.exists()
frozen.write_bytes(candidate.read_bytes())
source = (work / 'tests/paid-staff-session/PositiveWeapon436.java').read_text(encoding='utf-8')
source = source.replace('    private static ServerStartedEvent pending;', '    private static ServerStartedEvent pending;\n    private static net.minecraft.world.damagesource.DamageSource capturedSource;\n    private static CompoundTag capturedSigned;')
source = source.replace('                row.put("resolvedSnapshotPresent", !signed.isEmpty());', '                capturedSource = event.getSource(); capturedSigned = signed.copy();\n                row.put("resolvedSnapshotPresent", !signed.isEmpty());')
start = source.index('                Object snapshot = ((Map<?, ?>)readStatic(')
finish = source.index('                row.put("sameLeaseAndGenerationAfterAttack"', start)
source = source[:start] + source[finish:]
anchor = '                row.put("generationRetiredAfterAttack", read(generation, "retired"));'
source = source.replace(anchor, anchor + '''
                row.put("capturedNativeFrostPurpose", capturedSigned == null ? "" : capturedSigned.getString("DamagePurpose"));
                boolean outsideDenied = capturedSigned != null && capturedSource != null
                    && net.farmingtales.ftboss.item.FoodWeaponSourceLedger.resolveReadOnly(capturedSource, player).isEmpty()
                    && !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(capturedSigned, player);
                row.put("sameActualSourceAndSnapshotDeniedAfterFrostReturn", outsideDenied);
                if (!outsideDenied || actual.getDamageValue() != wearBefore + 1 || !useResult.getResult().consumesAction())
                    throw new AssertionError("Actual original Frost ray did not preserve use/wear or close its actual-hurt permission");''')
melee_source = (work / 'tests/paid-melee-ingress-eef72352/PositiveWeapon436.java').read_text(encoding='utf-8')
start = melee_source.index('                Class<?> storeType = Class.forName("net.farmingtales.ftboss.item.OwnerLineageRevocationStore");')
finish = melee_source.index('                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);', start)
save_checks = melee_source[start:finish].replace('post-melee', 'post-Frost')
source = source.replace('                row.put("actualOriginalPostCombatSaveCalled", true);', '                row.put("actualOriginalPostCombatSaveCalled", true);\n' + save_checks)
source = source.replace('Actual paid original melee', 'Actual paid original Frost ray').replace('Actual original melee', 'Actual original Frost ray')
add('work/session-lease/tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java', source)
launcher = (work / 'tools/run_native_paid_melee_ingress_eef72352.py').read_text(encoding='utf-8')
launcher = launcher.replace('native-repair-melee-ingress-candidate-eef72352.json', frozen.name)
launcher = launcher.replace('15f4f0e9a3f6fe21fffe9a969d2cf522835e6145388598b26d8db33502bf9e55', hashlib.sha256(frozen.read_bytes()).hexdigest())
launcher = launcher.replace('tests/paid-melee-ingress-eef72352/PositiveWeapon436.java', 'tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java')
launcher = launcher.replace('native-repair-runtime-paid-melee-ingress-eef72352.json', 'native-repair-runtime-paid-frost-ingress-' + tag + '.json')
add('work/session-lease/tools/run_native_paid_frost_ingress_' + tag + '.py', launcher)
print(json.dumps({'candidate': str(frozen), 'sha256': hashlib.sha256(frozen.read_bytes()).hexdigest(), 'tag': tag}))
