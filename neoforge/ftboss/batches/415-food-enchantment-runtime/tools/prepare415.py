import hashlib
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/412-six-weapon-runtime'
binding = LOCAL / 'batches/414-food-enchantment-binding/reports/actual-incremental414-experimental.json'
binding_hash = hashlib.file_digest(binding.open('rb'), 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('412', '415')
    if source.name == 'LoadedWeapon412.java':
        marker = '            PlayerDataStorage storage = '
        assert text.count(marker) == 1
        text = text.replace(marker, '            report.put("retiredFoodEnchantment", verifyRetiredFoodEnchantment415(event));\n' + marker)
        helper = '''
    private static Object verifyRetiredFoodEnchantment415(ServerStartedEvent event) {
        ServerPlayer player = new ServerPlayer(event.getServer(), event.getServer().overworld(), new GameProfile(new UUID(415, 1), "retired_enchant"), ClientInformation.createDefault());
        player.giveExperienceLevels(20);
        ItemStack weapon = new ItemStack(Items.IRON_SWORD);
        CompoundTag data = new CompoundTag();
        CompoundTag evolution = new CompoundTag();
        evolution.putString("Profile", "butcher"); evolution.putInt("Level", 1);
        net.minecraft.nbt.ListTag blessings = new net.minecraft.nbt.ListTag();
        for (String id : List.of("blood_feast", "cleaver_might", "butcher_hide", "vintage_crit")) {
            CompoundTag blessing = new CompoundTag(); blessing.putString("Blessing", id); blessing.putInt("Level", 3); blessings.add(blessing);
        }
        evolution.put("FoodBlessings", blessings); data.put("FTBossFoodEvolution", evolution);
        weapon.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        ItemStack food = new ItemStack(Items.BEEF, 64);
        player.getInventory().setItem(0, weapon); player.getInventory().setItem(1, food);
        ItemStack beforeWeapon = weapon.copy(); ItemStack beforeFood = food.copy();
        int beforeLevel = player.experienceLevel; int beforeExperience = player.totalExperience;
        int rejected = 0;
        for (int tint = 0; tint <= 10; tint++) {
            for (int slot : new int[]{-1, 0, 1, 35, 36, 39, 40, 41}) {
                if (net.farmingtales.ftboss.item.FoodWeaponEvolution.applyFoodBlessingFromUi(player, tint, slot)
                    || net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$applyFoodBlessingFromUi(player, tint, slot)) throw new AssertionError("Retired food enchantment request accepted");
                rejected += 2;
            }
            if (net.farmingtales.ftboss.item.FoodWeaponEvolution.isBlessingCandidate(weapon, food, tint)
                || net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$isBlessingCandidate(weapon, food, tint)) throw new AssertionError("Retired enchantment ingredient accepted");
        }
        if (!net.farmingtales.ftboss.item.FoodWeaponEvolution.foodBlessingViews(weapon).isEmpty()
            || !net.farmingtales.ftboss.item.FoodWeaponEvolution.ftboss$legacyOrdinary$foodBlessingViews(weapon).isEmpty()) throw new AssertionError("Legacy enchantment still displayed");
        if (net.farmingtales.ftboss.item.ArmorFoodBlessings.apply(player, "retired", weapon, food, 0)
            || net.farmingtales.ftboss.item.ArmorFoodBlessings.eligible(new ItemStack(Items.DIAMOND_CHESTPLATE))) throw new AssertionError("Retired armor enchantment accepted");
        net.farmingtales.ftboss.item.FoodWeaponEvolution.foodBlessingDefense(null);
        net.farmingtales.ftboss.item.FoodWeaponEvolution.foodBlessingKillFeast(null);
        net.farmingtales.ftboss.item.FoodWeaponEvolution.grindstoneBlessing(null);
        if (!ItemStack.matches(weapon, beforeWeapon) || !ItemStack.matches(food, beforeFood)
            || player.experienceLevel != beforeLevel || player.totalExperience != beforeExperience) throw new AssertionError("Rejected enchantment consumed or changed inputs");
        return Map.of("failures", 0, "rejectedRequests", rejected, "legacyViewsEmpty", true, "armorRequestsRejected", true, "noFoodWeaponOrExperienceMutation", true, "scratchPlayerNotAuthenticated", true);
    }
'''
        text = text.rstrip()[:-1] + helper + '}\n'
    (BATCH / 'tests' / source.name.replace('412', '415')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '412.py')).read_text(encoding='utf-8').replace('412', '415')
    text = text.replace('411-six-weapon-binding', '414-food-enchantment-binding').replace('actual-incremental411', 'actual-incremental414')
    text = text.replace('d3bfc4e2574f4c974bd707fcf68f8cc2b219b79da9d8da0a897b20bdb12a4fdb', binding_hash)
    if name == 'seal':
        marker = "scope = runtime['probe']['sixWeaponScope']"
        text = text.replace(marker, "retirement = runtime['probe']['retiredFoodEnchantment']\nassert retirement['failures'] == 0 and retirement['rejectedRequests'] == 176 and retirement['noFoodWeaponOrExperienceMutation']\n" + marker)
        text = text.replace("'enchantmentRemovalComplete': False", "'retiredFoodEnchantment': retirement, 'enchantmentRemovalComplete': False")
    (BATCH / 'tools' / (name + '415.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'binding': binding_hash, 'retiredEnchantmentNoConsumptionCases': 176})
