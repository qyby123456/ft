import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
tag = sys.argv[1]
assert re.fullmatch('[0-9a-f]{8}', tag)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def add(path, text):
    assert not path.exists(), path
    pending = text.splitlines()
    previous = []
    while pending:
        chunk = []
        length = 0
        while pending and length + len(pending[0]) < 6000:
            line = pending.pop(0)
            chunk.append(line)
            length += len(line) + 1
        assert chunk
        if previous:
            body = '*** Update File: ' + path.as_posix() + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
            suffix = '*** End of File\n'
        else:
            body = '*** Add File: ' + path.as_posix() + '\n'
            suffix = ''
        patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
        result = subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
        assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
        previous.extend(chunk)


receipt = work / ('reports/native-repair-runtime-paid-frost-ingress-' + tag + '.json')
positive = json.loads(receipt.read_text(encoding='utf-8'))
terminal = json.loads((Path(positive['attempt']) / 'terminal308.json').read_text(encoding='utf-8'))
assert positive['exitCode'] == positive['probe']['failures'] == 0
assert terminal['stopSent'] and terminal['completeSave']
assert len(positive['probe']['cases']) == 1
assert positive['probe']['cases'][0]['actualOriginalPostCombatSaveAuthenticatedCommitted']
old_source = work / ('tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java')
source = old_source.read_text(encoding='utf-8')
anchor = '                row.put("serverSignaturePresent", signed.getString("ServerSignature").length() == 64);'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + '\n                row.put("nativeHurtId", signed.getUUID("NativeHurtId").toString());\n                row.put("nativeTargetId", signed.getUUID("NativeTargetId").toString());\n                row.put("nativePurpose", signed.getString("DamagePurpose"));')
anchor = '                actions.clear(); damageEvents.clear();'
assert source.count(anchor) == 1
source = source.replace(anchor, '''                CompoundTag foreignBefore = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                var foreignSource = player.damageSources().indirectMagic(player, player);
                int[] foreignCalls = {0};
                boolean foreignResult = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeFrostActualHurt(target, foreignSource, 14.0F, arguments -> {
                    foreignCalls[0]++;
                    if (arguments[0] != target || arguments[1] != foreignSource || !arguments[2].equals(14.0F)) throw new AssertionError("Foreign Frost callback arguments changed");
                    return true;
                });
                RuntimeException originalFailure = new RuntimeException("original foreign Frost callback");
                boolean sameFailure = false;
                try {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeFrostActualHurt(target, foreignSource, 14.0F, arguments -> { foreignCalls[0]++; throw originalFailure; });
                } catch (RuntimeException failure) { sameFailure = failure == originalFailure; }
                if (!foreignResult || !sameFailure || foreignCalls[0] != 2 || !foreignBefore.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Foreign Frost direct entry changed authority, original callback count, result or throwable identity");
                row.put("foreignDirectFrostHurtDelegatesOnceAndPreservesArgumentsResultAndExceptionIdentity", true);
                var secondTarget = new net.minecraft.world.entity.monster.Zombie(server.overworld());
                secondTarget.setNoAi(true); secondTarget.moveTo(1.5, -59.0, 0.0, 0.0F, 0.0F);
                if (!server.overworld().addFreshEntity(secondTarget)) throw new AssertionError("Second actual nova target was not added");
                player.setShiftKeyDown(true);
                float secondHealthBefore = secondTarget.getHealth();
''' + anchor)
assert source.count('wearBefore + 1') == 1
source = source.replace('wearBefore + 1', 'wearBefore + 4')
anchor = '                target.discard();'
assert source.count(anchor) == 1
source = source.replace(anchor, '''                row.put("secondHealthBefore", secondHealthBefore);
                row.put("secondHealthAfter", secondTarget.getHealth());
                row.put("originalCooldownActive", player.getCooldowns().isOnCooldown(actual.getItem()));
                var hurtIds = damageEvents.stream().map(value -> value.get("nativeHurtId")).filter(java.util.Objects::nonNull).distinct().toList();
                var targetIds = damageEvents.stream().map(value -> value.get("nativeTargetId")).filter(java.util.Objects::nonNull).distinct().toList();
                row.put("distinctActualSignedHurtIds", hurtIds);
                row.put("distinctActualSignedTargetIds", targetIds);
                if (secondTarget.getHealth() >= secondHealthBefore || hurtIds.size() != 2 || targetIds.size() != 2 || publications.size() != 2
                    || !player.getCooldowns().isOnCooldown(actual.getItem())
                    || damageEvents.stream().anyMatch(value -> !"NATIVE_FROST_ACTUAL_HURT".equals(value.get("nativePurpose"))))
                    throw new AssertionError("Original nova did not independently sign and close two actual target Sessions");
                secondTarget.discard();
''' + anchor)
source = source.replace('Frost ray', 'Frost nova')
new_source = work / ('tests/paid-frost-nova-' + tag + '/PositiveWeapon436.java')
add(new_source, source)
old_launcher = work / ('tools/run_native_paid_frost_ingress_' + tag + '.py')
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-frost-ingress-' + tag + '/PositiveWeapon436.java', 'tests/paid-frost-nova-' + tag + '/PositiveWeapon436.java')
launcher = launcher.replace('reports/native-repair-runtime-paid-frost-ingress-' + tag + '.json', 'reports/native-repair-runtime-paid-frost-nova-' + tag + '.json')
new_launcher = work / ('tools/run_native_paid_frost_nova_' + tag + '.py')
add(new_launcher, launcher)
output = work / ('reports/native-frost-nova-preparation-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'rayReceipt': str(receipt), 'sameProductCandidate': True, 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'originalPaidSeedUnmodified': True, 'authorityFieldsWritten': False, 'installable': False, 'inputHashes': {str(path): digest(path) for path in [Path(__file__), receipt, old_source, old_launcher, new_source, new_launcher]}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'tag': tag, 'novaObserverPrepared': True}))
