import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
derived_origin = work / 'tools/prepare_native_trident_certified_copy_runtime.py'
frozen = json.loads((work / 'reports/native-trident-foundation-gate-preparation-d8648609.json').read_text(encoding='utf-8'))
assert hashlib.sha256(derived_origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(derived_origin)]
namespace = {'__file__': str(derived_origin)}
exec(compile(derived_origin.read_text(encoding='utf-8').split("exec(compile(text, str(__file__), 'exec'))")[0], str(derived_origin), 'exec'), namespace)
text = namespace['text']
factor_code = '''
factor_anchor = '                row.put("genuineNonNullLeaseAndGenerationBeforeAttack", true);'
assert source.count(factor_anchor) == 1
source = source.replace(factor_anchor, factor_anchor + ''' + repr('''
                double dispatchedMaterial = net.farmingtales.ftboss.item.WeaponMaterialBuild.build(actual).damage();
                double nativeMaterial = net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.combat(actual).damage();
                row.put("dispatchedMaterialFactor", dispatchedMaterial);
                row.put("nativeFoundationMaterialFactor", nativeMaterial);
                if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual)
                    && Double.doubleToLongBits(dispatchedMaterial) != Double.doubleToLongBits(nativeMaterial))
                    throw new AssertionError("Damage accounting ignored the authoritative native foundation for " + id);
''') + ''')
early_anchor = '                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 20);'
assert source.count(early_anchor) == 1
source = source.replace(early_anchor, ''' + repr('''                CompoundTag beforeEarlyRelease = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object beforeEarlyInput = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 1);
                boolean earlyUnchanged = beforeEarlyRelease.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && beforeEarlyInput == ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput()
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired")) && player.getMainHandItem() == actual;
                row.put("actualEarlyReleasePreservesInputImageAndAuthority", earlyUnchanged);
                if (!earlyUnchanged) throw new AssertionError("Original early Trident release changed paid authority or image");
                player.stopUsingItem();
                var secondUse = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                if (!secondUse.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid Trident use after early release did not restart");
''') + ''' + early_anchor)
'''
anchor = "namespace['add'](new_source.relative_to(root).as_posix(), source)"
assert text.count(anchor) == 1
text = text.replace(anchor, factor_code + '\n' + anchor)
text = text.replace('[Path(__file__), origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), derived_origin, origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]')
namespace['derived_origin'] = derived_origin
namespace['__file__'] = str(Path(__file__).resolve())
exec(compile(text, str(__file__), 'exec'), namespace)
