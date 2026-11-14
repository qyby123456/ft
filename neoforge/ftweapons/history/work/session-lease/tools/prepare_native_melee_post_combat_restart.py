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


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


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
        process = subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
        assert process.returncode == 0, process.stderr.decode('utf-8', errors='replace')
        previous.extend(chunk)


receipt = work / ('reports/native-repair-runtime-paid-melee-ingress-' + tag + '.json')
positive = load(receipt)
terminal = load(Path(positive['attempt']) / 'terminal308.json')
assert positive['exitCode'] == positive['probe']['failures'] == 0
assert terminal['stopSent'] and terminal['completeSave']
cases = positive['probe']['cases']
assert [case['id'] for case in cases] == ['minecraft:iron_sword', 'minecraft:iron_axe']
assert all(case['actualOriginalPostCombatSaveAuthenticatedCommitted'] for case in cases)
old_source = work / 'tests/six-family-paid-restart/PositiveWeapon436.java'
source = old_source.read_text(encoding='utf-8')
source = source.replace('for (int sample = 0; sample < ids.size(); sample++) {', 'for (int sample : new int[]{0, 3}) {')
source = source.replace('ticket.getLong("LeaseGeneration") != 1)', 'ticket.getLong("LeaseGeneration") != 3)')
source = source.replace('longValue() != 1L', 'longValue() != 3L')
source = source.replace('longValue() != 2L', 'longValue() != 4L')
source = source.replace('newTicket.getLong("LeaseGeneration") != 2L', 'newTicket.getLong("LeaseGeneration") != 4L')
source = source.replace('generation-one save', 'generation-three post-combat save')
source = source.replace('same epoch one to two', 'same epoch three to four')
source = source.replace('"sameEpochGenerationOneToTwo"', '"sameEpochGenerationThreeToFour"')
source = source.replace('"actualOriginalGenerationTwoSaveCommitted"', '"actualOriginalGenerationFourSaveCommitted"')
source = source.replace('"sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess"', '"paidSwordAndAxePostCombatContinuationAcrossNewProcess"')
marker = '                Object successor = readRecord.invoke(store, lineagePath, owner, ticket.getUUID("Weapon"));'
assert source.count(marker) == 1
source = source.replace(marker, '                if (actual.getDamageValue() != (sample == 0 ? 1 : 2)) throw new AssertionError("Original post-combat wear was not restored exactly for " + id);\n' + marker)
new_source = work / ('tests/melee-post-combat-restart-' + tag + '/PositiveWeapon436.java')
add(new_source, source)
old_launcher = work / ('tools/run_native_paid_melee_ingress_' + tag + '.py')
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace("seed_receipt = WORK / 'reports/native-repair-runtime-six-family-paid-restart.json'", "seed_receipt = WORK / 'reports/native-repair-runtime-paid-melee-ingress-" + tag + ".json'")
launcher = launcher.replace("assert seed_result['probe']['sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess']\nassert len(seed_result['probe']['cases']) == 6\nassert all(case['actualOriginalGenerationTwoSaveCommitted'] for case in seed_result['probe']['cases'])", "assert [case['id'] for case in seed_result['probe']['cases']] == ['minecraft:iron_sword', 'minecraft:iron_axe']\nassert all(case['actualOriginalPostCombatSaveAuthenticatedCommitted'] for case in seed_result['probe']['cases'])")
launcher = launcher.replace('tests/paid-melee-ingress-' + tag + '/PositiveWeapon436.java', 'tests/melee-post-combat-restart-' + tag + '/PositiveWeapon436.java')
launcher = launcher.replace("output = WORK / 'reports/native-repair-runtime-paid-melee-ingress-" + tag + ".json'", "output = WORK / 'reports/native-repair-runtime-melee-post-combat-restart-" + tag + ".json'")
assert 'sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess' not in launcher
assert 'sameProcessStillObserved' in launcher
new_launcher = work / ('tools/run_native_melee_post_combat_restart_' + tag + '.py')
add(new_launcher, launcher)
output = work / ('reports/native-melee-post-combat-preparation-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'positiveRuntimeReceipt': str(receipt), 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'originalWorldModified': False, 'authorityFieldsWritten': False, 'installable': False, 'inputHashes': {str(path): digest(path) for path in [Path(__file__), receipt, old_source, old_launcher, new_source, new_launcher]}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'tag': tag, 'launcher': str(new_launcher), 'requiresNewProcessObservation': True}))
