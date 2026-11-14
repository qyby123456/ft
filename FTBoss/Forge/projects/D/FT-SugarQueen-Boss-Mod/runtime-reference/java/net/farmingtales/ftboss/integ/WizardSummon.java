/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.entity.IMagicEntity
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.registry.SpellRegistry
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.api.spells.SchoolType
 *  io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossEmpower;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class WizardSummon {
    private static final String[] WIZARD_IDS = new String[]{"irons_spellbooks:archevoker", "irons_spellbooks:cryomancer", "irons_spellbooks:pyromancer", "irons_spellbooks:alchemist"};
    private static final String[] SPELL_FIELDS = new String[]{"FIRE_ARROW_SPELL", "FIREBOLT_SPELL", "FIREBALL_SPELL", "MAGMA_BOMB_SPELL", "BLAZE_STORM_SPELL", "SCORCH_SPELL", "HEAT_SURGE_SPELL", "FIRE_BREATH_SPELL", "WALL_OF_FIRE_SPELL", "FLAMING_BARRAGE_SPELL", "RAY_OF_FROST_SPELL", "ICICLE_SPELL", "ICE_SPIKES_SPELL", "CONE_OF_COLD_SPELL", "BLIZZARD_SPELL", "FROSTWAVE_SPELL", "FROSTBITE_SPELL", "ICE_TOMB_SPELL", "CHAIN_LIGHTNING_SPELL", "BALL_LIGHTNING_SPELL", "LIGHTNING_BOLT_SPELL", "LIGHTNING_LANCE_SPELL", "THUNDERSTORM_SPELL", "VOLT_STRIKE_SPELL", "ELECTROCUTE_SPELL", "MAGIC_ARROW_SPELL", "MAGIC_MISSILE_SPELL", "ARROW_VOLLEY_SPELL", "ELDRITCH_BLAST_SPELL", "BLACK_HOLE_SPELL", "FANG_STRIKE_SPELL", "BLOOD_SLASH_SPELL", "BLOOD_NEEDLES_SPELL", "HEARTSTOP_SPELL", "RAY_OF_SIPHONING_SPELL", "GUIDING_BOLT_SPELL", "STARFALL_SPELL", "SUNBEAM_SPELL", "POISON_ARROW_SPELL", "ACID_ORB_SPELL", "POISON_SPLASH_SPELL", "ROOT_SPELL", "POISON_BREATH_SPELL", "SCULK_TENTACLES_SPELL"};
    private static ServerLevel level;
    private static UUID wizardUUID;
    private static boolean checked;
    private static boolean available;
    private static final Set<String> UNSAFE_FOR_MOBS;
    private static final Set<String> CAMERA_SHAKE_SPELLS;

    private WizardSummon() {
    }

    private static boolean cameraShakeSpell(String spellId) {
        return spellId != null && CAMERA_SHAKE_SPELLS.contains(spellId);
    }

    private static boolean cameraShakeSpell(AbstractSpell spell) {
        try {
            return spell != null && WizardSummon.cameraShakeSpell(spell.getSpellId());
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean ensure(ServerLevel lvl, double x, double y, double z, ServerPlayer target) {
        try {
            PathfinderMob pm;
            Entity ent;
            block13: {
                block12: {
                    Mob mob;
                    Entity old;
                    if (!checked) {
                        checked = true;
                        boolean bl = available = WizardSummon.checkApi() && WizardSummon.pickWizardType(lvl) != null;
                        if (!available) {
                            FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5] \u672a\u627e\u5230\u53ef\u7528\u6cd5\u5e08\u5b9e\u4f53, \u4f8d\u4ece\u65bd\u6cd5\u5df2\u7981\u7528");
                        }
                    }
                    if (!available) {
                        return false;
                    }
                    level = lvl;
                    if (wizardUUID != null && (old = lvl.m_8791_(wizardUUID)) instanceof Mob && (mob = (Mob)old).m_6084_()) {
                        if (target != null) {
                            mob.m_6710_((LivingEntity)target);
                        }
                        return true;
                    }
                    EntityType<?> type = WizardSummon.pickWizardType(lvl);
                    if (type == null) {
                        available = false;
                        return false;
                    }
                    ent = type.m_20615_((Level)lvl);
                    if (!(ent instanceof PathfinderMob)) break block12;
                    pm = (PathfinderMob)ent;
                    if (ent instanceof IMagicEntity) break block13;
                }
                ent.m_146870_();
                available = false;
                return false;
            }
            ent.m_6034_(x, y, z);
            ent.m_6593_((Component)Component.m_237113_((String)"\u00a7d\u7cd6\u971c\u00b7\u9b54\u6cd5\u4f8d\u4ece"));
            ent.m_20340_(true);
            if (ent instanceof Mob) {
                Mob mob = (Mob)ent;
                mob.m_21557_(false);
                mob.m_21530_();
                mob.m_6710_((LivingEntity)target);
                mob.m_21561_(true);
                WizardAttackGoal goal = new WizardAttackGoal((IMagicEntity)ent, 1.05, 18, 36).setSpells(WizardSummon.spellPool(), new ArrayList(), new ArrayList(), new ArrayList()).setSpellQuality(2.0f, 4.0f);
                pm.f_21345_.m_25352_(1, (Goal)goal);
            }
            lvl.m_7967_(ent);
            MinionBlessing.mark(ent);
            wizardUUID = ent.m_20148_();
            FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5] \u9b54\u6cd5\u4f8d\u4ece\u5df2\u53ec\u5524: {}", (Object)ForgeRegistries.ENTITY_TYPES.getKey((Object)ent.m_6095_()));
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u94c1\u9b54\u6cd5] \u4f8d\u4ece\u53ec\u5524\u5f02\u5e38, \u5df2\u7981\u7528: {}", (Object)t.toString());
            available = false;
            return false;
        }
    }

    public static void despawnAll() {
        Entity e;
        if (level != null && wizardUUID != null && (e = level.m_8791_(wizardUUID)) != null) {
            e.m_6074_();
        }
        wizardUUID = null;
        level = null;
    }

    private static boolean checkApi() {
        try {
            Class.forName("io.redspace.ironsspellbooks.api.entity.IMagicEntity");
            Class.forName("io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal");
            Class.forName("io.redspace.ironsspellbooks.api.registry.SpellRegistry");
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }

    private static EntityType<?> pickWizardType(ServerLevel lvl) {
        for (String id : WIZARD_IDS) {
            EntityType t = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
            if (t == null) continue;
            return t;
        }
        return null;
    }

    public static List<AbstractSpell> spellPool() {
        try {
            return WizardSummon.resolve(SPELL_FIELDS);
        }
        catch (Exception ex) {
            return new ArrayList<AbstractSpell>();
        }
    }

    public static boolean castByName(ServerLevel sl, LivingEntity caster, LivingEntity target, String spellId, int level) {
        if (WizardSummon.cameraShakeSpell(spellId)) {
            FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5] \u5df2\u62e6\u622a\u5e26\u5730\u9707/\u9707\u5c4f\u6548\u679c\u7684\u6cd5\u672f: {}", (Object)spellId);
            return false;
        }
        try {
            AbstractSpell spell = SpellRegistry.getSpell((ResourceLocation)new ResourceLocation(spellId));
            if (spell == null) {
                FTBossMod.LOGGER.warn("[FTBoss][\u94c1\u9b54\u6cd5] \u627e\u4e0d\u5230\u6cd5\u672f {}", (Object)spellId);
                return false;
            }
            int lvl = Math.max(spell.getMinLevel(), Math.min(level, spell.getMaxLevel()));
            if (!BossSpellPacing.tryLegacyCast(caster, spell)) {
                return false;
            }
            spell.onCast((Level)sl, lvl, caster, CastSource.MOB, MagicData.getPlayerMagicData((LivingEntity)caster));
            FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5] {} lv{} -> {}", new Object[]{spellId, lvl, target == null ? "-" : target.m_7755_().getString()});
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u94c1\u9b54\u6cd5] \u65bd\u653e {} \u5931\u8d25: {}", (Object)spellId, (Object)t.toString());
            return false;
        }
    }

    public static boolean castDirect(LivingEntity caster, AbstractSpell spell, int level, LivingEntity target) {
        if (caster == null || spell == null || WizardSummon.cameraShakeSpell(spell)) {
            return false;
        }
        if (!BossSpellPacing.tryLegacyCast(caster, spell)) {
            return false;
        }
        try {
            int lvl = Math.max(spell.getMinLevel(), Math.min(level + BossEmpower.spellBonusFor(caster), spell.getMaxLevel()));
            spell.onCast(caster.m_9236_(), lvl, caster, CastSource.MOB, MagicData.getPlayerMagicData((LivingEntity)caster));
            FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5\u76f4\u653e] {} lv{} \u76ee\u6807={}", new Object[]{spell.getSpellId(), lvl, target == null ? "-" : target.m_7755_().getString()});
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u94c1\u9b54\u6cd5\u76f4\u653e] \u5931\u8d25 {}: {}", (Object)spell.getSpellId(), (Object)t.toString());
            return false;
        }
    }

    public static boolean castAt(IMagicEntity mob, LivingEntity target, AbstractSpell spell, int level) {
        LivingEntity caster;
        if (mob == null || spell == null || WizardSummon.cameraShakeSpell(spell)) {
            return false;
        }
        if (mob instanceof LivingEntity && !BossSpellPacing.tryLegacyCast(caster = (LivingEntity)mob, spell)) {
            return false;
        }
        try {
            if (target instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)target;
                try {
                    Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                    for (Constructor<?> ctor : tdc.getConstructors()) {
                        if (ctor.getParameterCount() != 1 || !ctor.getParameterTypes()[0].isAssignableFrom(Player.class)) continue;
                        Object inst = ctor.newInstance(sp);
                        Method set = MagicData.class.getMethod("setAdditionalCastData", ICastData.class);
                        MagicData md = MagicData.getPlayerMagicData((LivingEntity)((LivingEntity)mob));
                        set.invoke(md, inst);
                        break;
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            mob.initiateCastSpell(spell, level + BossEmpower.spellBonusFor(mob));
            FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5] \u65bd\u6cd5: {} lv{} \u76ee\u6807={}", new Object[]{spell.getSpellId(), level, target == null ? "-" : target.m_7755_().getString()});
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u94c1\u9b54\u6cd5] \u65bd\u6cd5\u5f02\u5e38: {}", (Object)t.toString());
            return false;
        }
    }

    public static void castRandom(IMagicEntity mob, LivingEntity target, RandomSource random, int baseLevel) {
        List<AbstractSpell> pool = WizardSummon.spellPool();
        if (pool.isEmpty()) {
            return;
        }
        AbstractSpell s = pool.get(random.m_188503_(pool.size()));
        int lvl = Math.max(s.getMinLevel(), Math.min(baseLevel, s.getMaxLevel()));
        WizardSummon.castAt(mob, target, s, lvl);
    }

    public static List<AbstractSpell> spellPoolRare(int castLevel, int minOrdinal) {
        List<AbstractSpell> all = WizardSummon.spellPool();
        ArrayList<AbstractSpell> out = new ArrayList<AbstractSpell>();
        for (AbstractSpell s : all) {
            try {
                if (s.getRarity(castLevel).ordinal() < minOrdinal) continue;
                out.add(s);
            }
            catch (Throwable throwable) {}
        }
        return out.isEmpty() ? all : out;
    }

    public static int rarityOf(AbstractSpell s, int castLevel) {
        try {
            return s.getRarity(castLevel).ordinal();
        }
        catch (Throwable ignored) {
            return 0;
        }
    }

    public static boolean isFire(AbstractSpell s) {
        try {
            String low;
            String id = s.getSpellId();
            if (id != null && ((low = id.toLowerCase(Locale.ROOT)).contains("fire") || low.contains("magma") || low.contains("blaze") || low.contains("flame") || low.contains("scorch") || low.contains("heat"))) {
                return true;
            }
            SchoolType school = s.getSchoolType();
            if (school != null) {
                String sid = String.valueOf(school.getId()).toLowerCase(Locale.ROOT);
                return sid.contains("fire");
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public static List<AbstractSpell> spellPoolOf(String ... fieldNames) {
        try {
            return WizardSummon.resolve(fieldNames);
        }
        catch (Exception ex) {
            return new ArrayList<AbstractSpell>();
        }
    }

    private static List<AbstractSpell> resolve(String[] fields) throws Exception {
        ArrayList<AbstractSpell> out = new ArrayList<AbstractSpell>();
        Class<?> registry = Class.forName("io.redspace.ironsspellbooks.api.registry.SpellRegistry");
        for (String name : fields) {
            try {
                Object ro = registry.getField(name).get(null);
                Object spell = ro.getClass().getMethod("get", new Class[0]).invoke(ro, new Object[0]);
                if (!(spell instanceof AbstractSpell)) continue;
                AbstractSpell as = (AbstractSpell)spell;
                String id = as.getSpellId();
                if (WizardSummon.cameraShakeSpell(id)) {
                    FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5] \u5df2\u6392\u9664\u5e26\u5730\u9707/\u9707\u5c4f\u6548\u679c\u7684\u6cd5\u672f: {}", (Object)id);
                    continue;
                }
                if (id != null && UNSAFE_FOR_MOBS.contains(id)) {
                    FTBossMod.LOGGER.info("[FTBoss][\u94c1\u9b54\u6cd5] \u5df2\u6392\u9664\u4ec5\u73a9\u5bb6\u53ef\u7528\u6cd5\u672f: {}", (Object)id);
                    continue;
                }
                out.add(as);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return out;
    }

    static {
        checked = false;
        available = false;
        UNSAFE_FOR_MOBS = Set.of("irons_spellbooks:spectral_hammer");
        CAMERA_SHAKE_SPELLS = Set.of("irons_spellbooks:earthquake", "irons_spellbooks:shockwave", "irons_spellbooks:sonic_boom", "irons_spellbooks:divine_smite", "irons_spellbooks:sacrifice", "irons_spellbooks:raise_hell");
    }
}

