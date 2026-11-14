import hashlib
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/409-native-composition-runtime'
binding = LOCAL / 'batches/411-six-weapon-binding/reports/actual-incremental411-experimental.json'
binding_hash = hashlib.file_digest(binding.open('rb'), 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('409', '412')
    if source.name == 'LoadedWeapon409.java':
        marker = '            PlayerDataStorage storage = '
        assert text.count(marker) == 1
        text = text.replace(marker, '            report.put("sixWeaponScope", verifySixWeaponScope412());\n' + marker)
        helper = '''
    private static Object verifySixWeaponScope412() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> retained = new ArrayList<>();
        for (String id : List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "irons_spellbooks:staff_of_the_nines")) {
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.parse(id)).orElseThrow(() -> new AssertionError("Retained family fixture missing: " + id));
            ItemStack stack = new ItemStack(item);
            if (!net.farmingtales.ftboss.item.WeaponSystemScope410.supports(stack)) throw new AssertionError("Retained family rejected: " + id);
            retained.add(id);
        }
        List<String> rejected = new ArrayList<>();
        for (net.minecraft.world.item.Item item : List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.SHEARS, Items.SHIELD, Items.DIAMOND_CHESTPLATE, Items.STICK)) {
            ItemStack stack = new ItemStack(item);
            CompoundTag data = new CompoundTag();
            CompoundTag evolution = new CompoundTag();
            evolution.putString("Profile", "butcher"); evolution.putInt("Level", 2);
            data.put("FTBossFoodEvolution", evolution);
            CompoundTag materials = new CompoundTag();
            for (net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part part : net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part.values()) materials.putString(part.name(), "diamond_alloy");
            data.put("FTBossWeaponMaterials", materials);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            if (net.farmingtales.ftboss.item.WeaponSystemScope410.supports(stack)
                || !net.farmingtales.ftboss.item.WeaponAttachmentPolicy.excluded(stack)
                || net.farmingtales.ftboss.item.FoodWeaponEvolution.isEvolutionCandidate(stack)
                || net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$isEvolutionCandidate(stack)
                || net.farmingtales.ftboss.item.WeaponDurabilityBuild.installedAlloys(stack) != 0
                || net.farmingtales.ftboss.item.WeaponMaterialBuild.materials(stack).stream().anyMatch(material -> material != net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material.ORIGINAL)) throw new AssertionError("Retired tool admitted with legacy data: " + item);
            rejected.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());
        }
        result.put("retainedSixFamilies", retained);
        result.put("rejectedLegacyDataItems", rejected);
        result.put("failures", 0);
        return result;
    }
'''
        text = text.rstrip()[:-1] + helper + '}\n'
    (BATCH / 'tests' / source.name.replace('409', '412')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '409.py')).read_text(encoding='utf-8').replace('409', '412')
    text = text.replace('408-native-composition-binding', '411-six-weapon-binding').replace('actual-incremental408', 'actual-incremental411')
    text = text.replace('358c801472a73858cd0fa7df6b470d00b82b49b9b4203820e4446999dda6e197', binding_hash)
    text = text.replace('== 1374', '== 1376')
    if name == 'seal':
        marker = "case = runtime['probe']['cases'][0]"
        text = text.replace(marker, "scope = runtime['probe']['sixWeaponScope']\nassert scope['failures'] == 0 and len(scope['retainedSixFamilies']) == 6 and len(scope['rejectedLegacyDataItems']) == 12\n" + marker)
        text = text.replace("'productionChanged': True", "'sixWeaponScope': scope, 'enchantmentRemovalComplete': False, 'productionChanged': True")
    (BATCH / 'tools' / (name + '412.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'binding': binding_hash, 'sixFamiliesAndTwelveLegacyItems': True})
