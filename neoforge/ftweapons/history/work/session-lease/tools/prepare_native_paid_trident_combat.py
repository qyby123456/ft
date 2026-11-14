import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag = '9d1607e8'
frozen_path = work / ('reports/native-crossbow-paid-signed-combat-focused-evidence-' + tag + '.json')
assert hashlib.sha256(frozen_path.read_bytes()).hexdigest() == 'e4951e8dc4144e142e97e225f79f7dec31c8c125d9aa1563ebef6eee8b67e256'
frozen = json.loads(frozen_path.read_text(encoding='utf-8'))
old_source = work / 'tests/paid-crossbow-combat-89f2b629/PositiveWeapon436.java'
old_launcher = work / ('tools/run_native_paid_crossbow_combat_' + tag + '.py')
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
candidate = work / ('reports/native-repair-ranged-use-candidate-' + tag + '.json')
for path in [old_source, old_launcher, helper, candidate]:
    assert hashlib.sha256(path.read_bytes()).hexdigest() == frozen['inputHashes'][str(path)]
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
source = old_source.read_text(encoding='utf-8')
source = source.replace('for (int sample : new int[]{2})', 'for (int sample : new int[]{4})')
source = source.replace('                player.getInventory().setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW, 16));\n', '')
start = source.index('                int ammoBefore = player.getInventory().getItem(1).getCount();')
finish = source.index('                List<Map<String, Object>> flights = new ArrayList<>();', start)
source = source[:start] + '''                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 20);
                row.put("actualOriginalReleaseReturned", true);
                row.put("generationRetiredAfterRelease", read(generation, "retired"));
                row.put("originalMainHandEmptyAfterThrow", player.getMainHandItem().isEmpty());
                player.stopUsingItem();
''' + source[finish:]
source = source.replace('entity instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow && arrow.getOwner() == player', 'entity instanceof net.minecraft.world.entity.projectile.ThrownTrident arrow && arrow.getOwner() == player')
source = source.replace('                    arrow.discard();\n', '')
source = source.replace('                row.put("originalCrossbowChargeAndFireReturned", true);', '                row.put("originalTridentUseAndReleaseReturned", true);')
start = source.index('                storage.save(player);')
finish = source.index('                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);', start)
source = source[:start] + '''                row.put("originalThrowPickupAndPersistenceAccepted", false);
                for (var projectile : arrows) projectile.discard();
                target.discard();
''' + source[finish:]
source = source.replace('CROSSBOW', 'TRIDENT').replace('Crossbow', 'Trident')
source = source.replace('report.put("crossbowEntry", "Actual ItemStack.use -> original Trident charge/releaseUsing -> actual charged ItemStack.use -> native arrow.tick impact");', 'report.put("tridentEntry", "Actual paid ItemStack.use -> original Trident releaseUsing/constructor/add/remove inventory -> actual ThrownTrident.tick impact; pickup and persistence still unaccepted");')
new_source = work / ('tests/paid-trident-combat-' + tag + '/PositiveWeapon436.java')
new_launcher = work / ('tools/run_native_paid_trident_combat_' + tag + '.py')
namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-crossbow-combat-' + tag, 'tests/paid-trident-combat-' + tag)
launcher = launcher.replace('reports/native-repair-runtime-paid-crossbow-combat-' + tag, 'reports/native-repair-runtime-paid-trident-combat-' + tag)
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
output = work / ('reports/native-trident-combat-preparation-' + tag + '.json')
assert not output.exists()
paths = [Path(__file__), frozen_path, old_source, old_launcher, helper, candidate, new_source, new_launcher]
output.write_text(json.dumps({'sameFrozenCrossbowCandidate': str(candidate), 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualGenuinePaidSeedSample': 4, 'authorityFieldsWritten': False, 'pickupAndPersistenceAccepted': False, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'tag': tag, 'launcher': str(new_launcher), 'originalThrowAndImpactRequired': True}))
