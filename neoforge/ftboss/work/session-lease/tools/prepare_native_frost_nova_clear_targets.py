import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
tag = '2e5c6034'
origin = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(origin)}
exec(compile(origin.read_text(encoding='utf-8').split('candidate = work /')[0], str(origin), 'exec'), namespace)
old_source = work / ('tests/paid-frost-nova-' + tag + '/PositiveWeapon436.java')
source = old_source.read_text(encoding='utf-8')
assert source.count('secondTarget.moveTo(1.5, -59.0, 0.0, 0.0F, 0.0F);') == 1
source = source.replace('secondTarget.moveTo(1.5, -59.0, 0.0, 0.0F, 0.0F);', 'secondTarget.moveTo(0.0, -59.0, 3.0, 0.0F, 0.0F);')
anchor = '                player.setShiftKeyDown(true);'
assert source.count(anchor) == 1
source = source.replace(anchor, '''                for (var visible : List.of(target, secondTarget)) {
                    var clip = server.overworld().clip(new net.minecraft.world.level.ClipContext(player.getEyePosition(), visible.getBoundingBox().getCenter(),
                        net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                    if (clip.getType() != net.minecraft.world.phys.HitResult.Type.MISS) throw new AssertionError("Nova fixture target is occluded by " + clip.getBlockPos());
                    if (server.overworld().getEntity(visible.getUUID()) != visible || !visible.isAlive() || visible.isInvulnerable()
                        || visible.isAlliedTo(player) || player.isAlliedTo(visible)) throw new AssertionError("Nova fixture target is not independently eligible");
                }
                row.put("twoTargetsIndependentlyVisibleAndEligibleBeforeOriginalNova", true);
''' + anchor)
new_source = work / ('tests/paid-frost-nova-clear-' + tag + '/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(work.parents[1]).as_posix(), source)
old_launcher = work / ('tools/run_native_paid_frost_nova_' + tag + '.py')
launcher = old_launcher.read_text(encoding='utf-8')
launcher = launcher.replace('tests/paid-frost-nova-', 'tests/paid-frost-nova-clear-')
launcher = launcher.replace('reports/native-repair-runtime-paid-frost-nova-', 'reports/native-repair-runtime-paid-frost-nova-clear-')
new_launcher = work / ('tools/run_native_paid_frost_nova_clear_' + tag + '.py')
namespace['add'](new_launcher.relative_to(work.parents[1]).as_posix(), launcher)
output = work / ('reports/native-frost-nova-clear-preparation-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'sameProductCandidate': True, 'actualOriginalLineOfSightRequired': True, 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in [Path(__file__), old_source, old_launcher, new_source, new_launcher]}}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
