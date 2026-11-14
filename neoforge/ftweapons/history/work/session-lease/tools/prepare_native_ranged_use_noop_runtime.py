from pathlib import Path


work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_ranged_use_runtime.py'
helper_text = origin.read_text(encoding='utf-8')
anchor = "namespace['add'](source.relative_to(root).as_posix(), old_source.read_text(encoding='utf-8'))"
assert helper_text.count(anchor) == 1
addition = '''                Object inputBeforeEarlyRelease = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                CompoundTag beforeEarlyRelease = (CompoundTag)actual.save(player.registryAccess());
                int ammoBeforeEarlyRelease = player.getInventory().getItem(1).getCount();
                actual.releaseUsing(server.overworld(), player, actual.getUseDuration(player) - 1);
                boolean earlyReleaseUnchanged = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput() == inputBeforeEarlyRelease
                    && beforeEarlyRelease.equals(actual.save(player.registryAccess())) && !Boolean.TRUE.equals(read(generation, "retired"))
                    && player.getInventory().getItem(1).getCount() == ammoBeforeEarlyRelease;
                row.put("originalEarlyReleasePreservedInputImageAmmoAndAuthority", earlyReleaseUnchanged);
                if (!earlyReleaseUnchanged) throw new AssertionError("Original early release modified or retired unchanged genuine authority");
                player.stopUsingItem();
                var restarted = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                if (!restarted.getResult().consumesAction() || !player.isUsingItem() || Boolean.TRUE.equals(read(generation, "retired")))
                    throw new AssertionError("Original paid crossbow could not restart charge after original early release");
'''
replacement = "source_text = old_source.read_text(encoding='utf-8')\nsource_anchor = '                int ammoBefore = player.getInventory().getItem(1).getCount();'\nassert source_text.count(source_anchor) == 1\nsource_text = source_text.replace(source_anchor, " + repr(addition) + " + source_anchor)\nnamespace['add'](source.relative_to(root).as_posix(), source_text)"
helper_text = helper_text.replace(anchor, replacement)
helper_text = helper_text.replace('[Path(__file__), helper, preparation, old_source, old_launcher, frozen, source, launcher]', '[Path(__file__), origin, helper, preparation, old_source, old_launcher, frozen, source, launcher]')
exec(compile(helper_text, str(__file__), 'exec'))
