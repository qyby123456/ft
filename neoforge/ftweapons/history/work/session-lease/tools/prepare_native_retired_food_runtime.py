import hashlib
import json
import sys
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
mode=sys.argv[1]
assert mode in ['trident','bow']
current=json.loads((work/'reports/native-repair-ranged-use-candidate.json').read_text(encoding='utf-8'))
tag=Path(current['attempt']).name.removeprefix('native-repair-')
assert 'additionalProductionResources' in current and not current['retiredFoodEnchantmentRuntimeAccepted']
parent_name='trident-paid-signed-pickup-restart' if mode=='trident' else 'bow-original-use-signed-restart'
evidence_path=work/('reports/native-'+parent_name+'-focused-evidence-529ba4c9.json')
evidence=json.loads(evidence_path.read_text(encoding='utf-8'));assert evidence['failures']==0
old_source=work/('tests/trident-foundation-gate-529ba4c9/PositiveWeapon436.java' if mode=='trident' else 'tests/bow-original-use-529ba4c9/PositiveWeapon436.java')
old_launcher=work/('tools/run_native_trident_foundation_gate_529ba4c9.py' if mode=='trident' else 'tools/run_native_bow_original_use_529ba4c9.py')
for path in [old_source,old_launcher]:assert hashlib.sha256(path.read_bytes()).hexdigest()==evidence['inputHashes'][str(path)],path
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
legacy=work.parent/'session-chain/runtime/tests/candidate/LoadedWeapon436.java'
legacy_text=legacy.read_text(encoding='utf-8')
start=legacy_text.index('    private static Object verifyRetiredFoodEnchantment436(')
legacy_method=legacy_text[start:legacy_text.rindex('\n}')]
source=old_source.read_text(encoding='utf-8')
source=source.replace('import net.minecraft.nbt.CompoundTag;','import net.minecraft.nbt.CompoundTag;\nimport net.minecraft.world.item.ItemStack;\nimport net.minecraft.world.item.Items;\nimport net.minecraft.core.component.DataComponents;\nimport net.minecraft.world.item.component.CustomData;')
anchor='            var server = event.getServer();'
assert source.count(anchor)==1
source=source.replace(anchor,'            report.put("retiredFoodRequests", verifyRetiredFoodEnchantment436(event));\n            report.put("retiredFoodCatalogProtocolAndEffects", verifyRetiredClosure(event));\n'+anchor)
extra=r'''    private static Object verifyRetiredClosure(ServerStartedEvent event) throws Exception {
        for (String profile : List.of("sugar", "butcher", "saya", "wine", "rat", "defect", "compost", "apple", "tide", "benger", "", "unknown")) {
            if (!net.farmingtales.ftboss.item.StatueBlessingCatalog.pool(profile).isEmpty()
                || !net.farmingtales.ftboss.item.ArmorBlessingCatalog.pool(profile).isEmpty()) throw new AssertionError("Retired food catalogue still publishes entries");
        }
        if (!net.farmingtales.ftboss.item.StatueBlessingCatalog.all().isEmpty() || !net.farmingtales.ftboss.item.ArmorBlessingCatalog.all().isEmpty()
            || net.farmingtales.ftboss.item.StatueBlessingCatalog.byId("butcher_hook_pull") != null
            || net.farmingtales.ftboss.item.ArmorBlessingCatalog.byId("armor_butcher_strike") != null) throw new AssertionError("Retired food catalogue remains discoverable");
        ItemStack stored = new ItemStack(Items.IRON_SWORD);
        CompoundTag legacy = new CompoundTag();
        legacy.putString("Profile", "butcher"); legacy.putInt("Level", 3);
        var blessings = new net.minecraft.nbt.ListTag();
        for (String identity : List.of("blood_feast", "cleaver_might", "syrup_step", "butcher_hook_pull")) {
            CompoundTag row = new CompoundTag(); row.putString("Blessing", identity); row.putInt("Level", 3); blessings.add(row);
        }
        legacy.put("FoodBlessings", blessings); CompoundTag root = new CompoundTag(); root.put("FTBossFoodEvolution", legacy);
        stored.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        var sharpness = event.getServer().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
        var enchantments = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        enchantments.set(sharpness, 3); stored.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        CompoundTag before = net.farmingtales.ftboss.item.NativeFoundationItemData.image(stored);
        if (net.farmingtales.ftboss.item.FoodBlessingCombat.eligible(stored)
            || net.farmingtales.ftboss.item.FoodBlessingCombat.level(stored, "blood_feast") != 0
            || net.farmingtales.ftboss.item.FoodBlessingCombat.level(stored, "cleaver_might") != 0
            || net.farmingtales.ftboss.item.FoodBlessingCombat.crossbowChargeTicks(25, stored) != 25) throw new AssertionError("Stored retired blessing still enables gameplay");
        net.farmingtales.ftboss.item.FoodBlessingCombat.blessingAttributes(null, stored);
        net.farmingtales.ftboss.item.FoodBlessingCombat.blessingAttributes(null, stored, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        net.farmingtales.ftboss.item.FoodBlessingCombat.quoteDamage(null, null, stored);
        net.farmingtales.ftboss.item.FoodBlessingCombat.defense(null); net.farmingtales.ftboss.item.FoodBlessingCombat.killFeast(null);
        net.farmingtales.ftboss.item.StatueBlessingCombat.quote(null, null, null, root, 80L, stored, null);
        net.farmingtales.ftboss.item.StatueBlessingCombat.defense(null); net.farmingtales.ftboss.item.StatueBlessingCombat.kill(null);
        net.farmingtales.ftboss.item.StatueBlessingCombat.attributes(null, stored);
        net.farmingtales.ftboss.item.StatueBlessingCombat.attributes(null, stored, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        if (!before.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(stored))
            || stored.get(DataComponents.ENCHANTMENTS).getLevel(sharpness) != 3) throw new AssertionError("Retirement rewrote stored data or vanilla enchantment component");
        net.farmingtales.ftboss.network.StatueMenuNetwork.Action.send(net.minecraft.core.BlockPos.ZERO, 7, 1);
        net.farmingtales.ftboss.network.StatueMenuNetwork.Action.handle(new net.farmingtales.ftboss.network.StatueMenuNetwork.Action(net.minecraft.core.BlockPos.ZERO, 7, 1), null);
        net.farmingtales.ftboss.network.StatueMenuNetwork.OrdinaryAction.send(net.minecraft.core.BlockPos.ZERO, 7, 1);
        for (String language : List.of("en_us", "zh_cn")) {
            try (var stream = PositiveWeapon436.class.getResourceAsStream("/assets/ftboss/lang/" + language + ".json")) {
                if (stream == null) throw new AssertionError("Actual retired language resource not found");
                var values = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (String key : values.keySet()) if (key.startsWith("food_blessing.ftboss.") || key.startsWith("tooltip.ftboss.food_blessing.")
                    || key.startsWith("message.ftboss.food_blessing.") || key.startsWith("screen.ftboss.statue_hub.enchant.")
                    || key.equals("screen.ftboss.statue_hub.tab.enchant")) throw new AssertionError("Actual language resource still exposes retired food enchantment");
                for (String key : List.of("item.ftboss.maque_blessing_feather", "tooltip.ftboss.maque_feather.blessing", "message.ftboss.maque_feather.blessing"))
                    if (!values.has(key)) throw new AssertionError("Unrelated maque blessing translation removed");
            }
        }
        return Map.of("cataloguesEmpty", true, "storedFoodEffectsInert", true, "storedDataAndVanillaSharpnessThreePreserved", true,
            "retiredPacketSendAndHandleReturnWithoutNetworkOrContext", true, "actualLanguageResourcesRetiredAndMaqueKeysRetained", true);
    }

'''
source=source[:source.rindex('\n}')]+ '\n'+legacy_method+'\n'+extra+source[source.rindex('\n}'):]
new_source=work/('tests/retired-food-'+mode+'-'+tag+'/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8')
launcher=launcher.replace('native-repair-ranged-use-candidate-529ba4c9.json','native-repair-ranged-use-candidate-'+tag+'.json')
old_candidate=work/'reports/native-repair-ranged-use-candidate-529ba4c9.json'
launcher=launcher.replace(hashlib.sha256(old_candidate.read_bytes()).hexdigest(),hashlib.sha256((work/('reports/native-repair-ranged-use-candidate-'+tag+'.json')).read_bytes()).hexdigest())
launcher=launcher.replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
output_old='native-repair-runtime-trident-foundation-gate-529ba4c9.json' if mode=='trident' else 'native-repair-runtime-bow-original-use-529ba4c9.json'
launcher=launcher.replace(output_old,'native-repair-runtime-retired-food-'+mode+'-'+tag+'.json')
anchor="for directory in ['mods', 'recipe-inputs', 'kubejs']:"
assert launcher.count(anchor)==1
extra='''additional_resources = load(binding)['additionalProductionResources']
for resource_name, frozen_name in additional_resources.items():
    frozen_resource = Path(frozen_name)
    assert digest(frozen_resource) == load(binding)['inputHashes'][frozen_name]
    target_resource = proposal / resource_name
    assert target_resource.is_file()
    shutil.copyfile(frozen_resource, target_resource)

'''
launcher=launcher.replace(anchor,extra+anchor)
preparation=work/('reports/native-retired-food-'+mode+'-preparation-'+tag+'.json')
anchor="save(attempt / 'inputs-before308.json', inputs)"
head,tail=launcher.rsplit(anchor,1)
extra="inputs[str(WORK / "+repr(preparation.relative_to(work).as_posix())+")] = digest(WORK / "+repr(preparation.relative_to(work).as_posix())+")\nfor resource_name, frozen_name in additional_resources.items():\n    inputs[frozen_name] = digest(Path(frozen_name))\n    inputs[str(proposal / resource_name)] = digest(proposal / resource_name)\n"
launcher=head+extra+anchor+tail
new_launcher=work/('tools/run_native_retired_food_'+mode+'_'+tag+'.py')
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,evidence_path,old_source,old_launcher,legacy,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'candidate':str(work/('reports/native-repair-ranged-use-candidate-'+tag+'.json')),'mode':mode,'newObserver':str(new_source),'newLauncher':str(new_launcher),'authorityFieldsWritten':False,'storedItemDataDeleted':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'candidateTag':tag,'mode':mode}))
