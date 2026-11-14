import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-exact-melee-variants-paid-repair-native-materials-qualified-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
failed_path=work/'reports/native-repair-runtime-exact-melee-variants-paid-repair-native-materials-qualified-41d445cf.json';failed=load(failed_path);terminal=Path(failed['attempt'])/'terminal308.json';end=load(terminal)
assert failed['exitCode']==0 and failed['probe']['failures']==1 and end['completeSave'] and end['stopSent']
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
source=source.replace('for (int sample = 0; sample < ids.size(); sample++) {','for (int sample = 0; sample < 1; sample++) {')
anchor="""                    if (net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(weapon))
                        throw new AssertionError("Original registered recipe/reference layout is not yet combat-ready: " + id + " / " + net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon));""";assert source.count(anchor)==1;source=source.replace(anchor,'')
anchor='                original.getInventory().setItem(0, weapon);';assert source.count(anchor)==1;source=source.replace(anchor,anchor+'\n                installOriginalMaterials(original, weapon, row);')
source=source.replace('nineteenExactOriginalMeleeVariantsPaidRepairAndSameProcessReload','oneWoodenSwordOriginalMaterialInstallationPaidRepairAndSameProcessReload')
anchor='    private static void runNativeRepair(';assert source.count(anchor)==1
method="""    private static double readinessScore(net.farmingtales.ftboss.util.SwordFunctionRules.Status status) {
        if (!status.valid()) return -1.0;
        double total = 0.0;
        for (var function : net.farmingtales.ftboss.util.SwordFunctionRules.Function.values()) {
            double minimum = status.minimum().value(function);
            total += minimum <= 0.0 ? 1.0 : Math.min(1.0, status.total().value(function) / minimum);
        }
        return total;
    }

    private static void installOriginalMaterials(ServerPlayer player, ItemStack weapon, Map<String, Object> row) throws Exception {
        if (!net.farmingtales.ftboss.item.SwordCavityBuild.applies(weapon)) return;
        Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
        var lease = guard.getDeclaredMethod("lease", Object.class); lease.setAccessible(true);
        var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
        if (lease.invoke(null, weapon) != null || generation.invoke(null, weapon) != null) throw new AssertionError("Material installation must precede any repair authority");
        var preview = net.farmingtales.ftboss.item.SwordCavityBuild.class.getDeclaredMethod("containerPreview", ItemStack.class, List.class); preview.setAccessible(true);
        List<net.minecraft.world.item.Item> materials = new ArrayList<>();
        for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            ItemStack piece = new ItemStack(item);
            if (net.farmingtales.ftboss.item.SwordCavityBuild.materialFor(piece) != net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material.ORIGINAL
                && net.farmingtales.ftboss.api.SwordMaterialExtensions.resolve(piece).isPresent()) materials.add(item);
        }
        materials.sort(java.util.Comparator.comparing(item -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString()));
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        var channel = new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(player.getServer(), connection, player,
            net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
        List<Object> installed = new ArrayList<>(); row.put("originalConsumedMaterialInstallations", installed);
        try {
            for (int step = 0; step < 27 && net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(weapon); step++) {
                double score = readinessScore(net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon));
                net.farmingtales.ftboss.util.SwordCavityRules.Slot bestSlot = null;
                net.minecraft.world.item.Item bestMaterial = null;
                for (var slot : net.farmingtales.ftboss.util.SwordCavityRules.Slot.values()) {
                    if (!net.farmingtales.ftboss.item.SwordCavityBuild.cellUnlocked(weapon, slot.ordinal())) continue;
                    for (var item : materials) {
                        ItemStack piece = new ItemStack(item);
                        var material = net.farmingtales.ftboss.item.SwordCavityBuild.materialFor(piece);
                        if (!net.farmingtales.ftboss.item.SwordCavityBuild.canReplace(weapon, slot, material)) continue;
                        List<ItemStack> contents = new ArrayList<>(net.farmingtales.ftboss.item.SwordCavityBuild.containerItems(weapon));
                        contents.set(slot.ordinal(), piece);
                        ItemStack proposed = (ItemStack)preview.invoke(null, weapon, contents);
                        if (proposed.isEmpty()) continue;
                        double candidate = readinessScore(net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(proposed));
                        if (candidate > score + 0.0000001) { score = candidate; bestSlot = slot; bestMaterial = item; }
                    }
                }
                if (bestSlot == null) throw new AssertionError("Original material rules have no improving legal installation: " + net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon));
                player.getInventory().setItem(2, new ItemStack(bestMaterial));
                int revision = net.farmingtales.ftboss.item.SwordCavityBuild.revision(weapon);
                boolean accepted = net.farmingtales.ftboss.item.SwordCavityBuild.replace(player, bestSlot, 2, revision);
                if (!accepted || !player.getInventory().getItem(2).isEmpty()) throw new AssertionError("Original material replacement did not accept/consume exact physical supply");
                installed.add(Map.of("slot", bestSlot.name(), "item", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(bestMaterial).toString(), "originalAccepted", true, "actualSupplyConsumed", true, "status", net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon).toString()));
                if (lease.invoke(null, weapon) != null || generation.invoke(null, weapon) != null) throw new AssertionError("Original material installation unexpectedly minted repair authority");
            }
            if (net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(weapon)) throw new AssertionError("Original material layout still incomplete");
            row.put("originalMaterialInstallationBeforeAnyAuthorityAndFunctionReady", true);
        } finally { channel.finishAndReleaseAll(); }
    }

"""
source=source.replace(anchor,method+anchor)
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_source=work/'tests/exact-melee-wooden-original-installed-paid-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-exact-melee-variants-paid-repair-native-materials-qualified-41d445cf.json','native-repair-runtime-exact-melee-wooden-original-installed-paid-41d445cf.json')
preparation=work/'reports/native-exact-melee-wooden-original-installed-paid-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_wooden_original_installed_paid_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,failed_path,terminal,old_source,old_launcher,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'actualCases':1,'productChanged':False,'originalRealInventoryMaterialReplacementWithActualSupplyConsumptionBeforeAuthority':True,'readOnlyOriginalDetachedPreviewUsedForPlanningOnly':True,'authorityFieldsWritten':False,'authenticatedMultiplayerAccepted':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualCases':1,'originalInventoryConsumptionRequired':True,'productUnchanged41':True}))
