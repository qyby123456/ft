package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_bossprobe")
public final class IndependentBossRuntimeProbe {
    public IndependentBossRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(IndependentBossRuntimeProbe::started);
    }

    private static void require(boolean condition, String label, List<String> cases) {
        if (!condition) throw new AssertionError(label);
        cases.add(label);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<String> cases = new ArrayList<>();
        report.put("cases", cases);
        report.put("independentBossJarAccepted", false);
        report.put("actualEntityConstructionOnly", true);
        report.put("actualCombatOrAuthenticatedPlayers", false);
        report.put("failures", 0);
        try {
            ServerLevel level = event.getServer().overworld();
            Entity owner = EntityType.ZOMBIE.create(level);
            Entity child = EntityType.ZOMBIE.create(level);
            require(owner != null && child != null, "actualVanillaEntityConstruction", cases);
            ManorFightMembers.inherit(owner, child);
            require(owner.getUUID().equals(ManorFightMembers.ownerId(child)), "nativeOwnerUuidInherited", cases);
            require(ManorFightMembers.fightId(child) == null, "missingFightNotInvented", cases);
            Entity grandchild = EntityType.ZOMBIE.create(level);
            ManorFightMembers.inherit(child, grandchild);
            require(owner.getUUID().equals(ManorFightMembers.ownerId(grandchild)), "originalRootOwnerPreservedAcrossGeneration", cases);
            UUID fight = UUID.randomUUID();
            ManorFightMembers.bind(owner, fight);
            ManorFightMembers.inherit(owner, child);
            require(fight.equals(ManorFightMembers.fightId(child)), "publicBossFightBindingInherited", cases);
            var selfBefore = owner.getPersistentData().copy();
            ManorFightMembers.inherit(owner, owner);
            require(selfBefore.equals(owner.getPersistentData()), "selfInheritanceNoMutation", cases);
            var childBefore = child.getPersistentData().copy();
            ManorFightMembers.inherit(null, child);
            ManorFightMembers.inherit(owner, null);
            require(childBefore.equals(child.getPersistentData()), "nullInheritanceNoMutation", cases);
            EntityType<?> actualBossType = null;
            EasterBossEntity parentBoss = null;
            for (var key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                if (!key.getNamespace().equals("ftboss")) continue;
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
                Entity candidate = type.create(level);
                if (candidate instanceof EasterBossEntity boss) {
                    actualBossType = type;
                    parentBoss = boss;
                    report.put("actualBossRegistryId", key.toString());
                    break;
                }
            }
            require(parentBoss != null && actualBossType != null, "actualRegisteredBossEntityConstruction", cases);
            EasterBossEntity childBoss = (EasterBossEntity) actualBossType.create(level);
            ManorDifficulty difficulty = ManorDifficulty.values()[ManorDifficulty.values().length - 1];
            UUID summoner = UUID.randomUUID();
            parentBoss.setChallengeDifficulty(difficulty);
            parentBoss.setManorSummonerId(summoner);
            ManorFightMembers.bind(parentBoss, fight);
            ManorFightMembers.inherit(parentBoss, childBoss);
            require(parentBoss.getUUID().equals(ManorFightMembers.ownerId(childBoss)), "actualBossOwnerInherited", cases);
            require(fight.equals(ManorFightMembers.fightId(childBoss)), "actualBossFightInherited", cases);
            require(childBoss.getChallengeDifficulty() == difficulty, "actualBossDifficultyPreserved", cases);
            require(summoner.equals(childBoss.getManorSummonerId()), "actualBossSummonerPreserved", cases);
            ClassLoader loader = ManorFightMembers.class.getClassLoader();
            for (String absent : List.of("net.farmingtales.ftboss.item.FoodWeaponEvolution", "net.farmingtales.ftboss.item.FoodWeaponSourceLedger", "net.farmingtales.ftboss.menu.SwordCavityMenu", "net.farmingtales.ftboss.network.StatueMenuNetwork")) {
                boolean missing = false;
                try { Class.forName(absent, false, loader); }
                catch (ClassNotFoundException expected) { missing = true; }
                require(missing, "optionalWeaponClassAbsent:" + absent, cases);
            }
            require(BuiltInRegistries.MENU.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss", "statue_blank")).isPresent(), "bossStatueMenuRegistered", cases);
            require(BuiltInRegistries.MENU.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss", "sword_cavity")).isEmpty(), "weaponCavityMenuNotRegisteredByBossCore", cases);
            require(net.farmingtales.ftboss.api.OptionalWeaponModule.skillCooldown(net.minecraft.world.item.ItemStack.EMPTY, 120) == 120, "baseCooldownUnchangedWithoutWeaponAddon", cases);
            require(net.farmingtales.ftboss.api.OptionalWeaponModule.activeSkillMultiplier(net.minecraft.world.item.ItemStack.EMPTY) == 1.0F, "baseDamageMultiplierUnchangedWithoutWeaponAddon", cases);
            require(net.farmingtales.ftboss.item.FTBossItems.SUGAR_QUEEN_CRYSTAL.get() != net.minecraft.world.item.Items.AIR, "originalBossSummonItemRegistered", cases);
            report.put("ftbossEntityRegistryCount", BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(key -> key.getNamespace().equals("ftboss")).count());
            report.put("ftbossItemRegistryCount", BuiltInRegistries.ITEM.keySet().stream().filter(key -> key.getNamespace().equals("ftboss")).count());
            report.put("actualBossClassResource", ManorFightMembers.class.getResource("ManorFightMembers.class").toString());
            report.put("actualOwnerCodeSource", ManorFightMembers.class.getProtectionDomain().getCodeSource().getLocation().toString());
        } catch (Throwable failure) {
            report.put("failures", 1);
            report.put("failure", failure.toString());
            java.io.StringWriter trace = new java.io.StringWriter();
            failure.printStackTrace(new java.io.PrintWriter(trace));
            report.put("trace", trace.toString());
        }
        try {
            Files.writeString(Path.of("actual-independent-boss-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception failure) {
            throw new IllegalStateException(failure);
        }
    }
}
