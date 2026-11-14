package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 铁魔法施法侍从: 女王召唤铁魔法自带法师实体, 挂 WizardAttackGoal 令其持续施法(同东方起源鉴做法)
 * 全场至多1只; 铁魔法缺失/实体缺失时自动禁用
 */
public final class WizardSummon {

    private static final String[] WIZARD_IDS = {
            "irons_spellbooks:archevoker",
            "irons_spellbooks:cryomancer",
            "irons_spellbooks:pyromancer",
            "irons_spellbooks:alchemist"
    };
    /** 侍从/女王共用进攻法术池(女王指令, SpellRegistry 静态字段名; 不存在的字段会自动跳过)
     *  覆盖火/冰/雷/奥术/血/神圣/自然各系 AoE+单体, Boss 等级统一由 BossIronSpellLevels 约束 */
    private static final String[] SPELL_FIELDS = {
            // 火
            "FIRE_ARROW_SPELL", "FIREBOLT_SPELL", "FIREBALL_SPELL", "MAGMA_BOMB_SPELL",
            "BLAZE_STORM_SPELL", "SCORCH_SPELL", "HEAT_SURGE_SPELL", "FIRE_BREATH_SPELL",
            "WALL_OF_FIRE_SPELL", "FLAMING_BARRAGE_SPELL",
            // 冰
            "RAY_OF_FROST_SPELL", "ICICLE_SPELL", "ICE_SPIKES_SPELL", "CONE_OF_COLD_SPELL",
            "BLIZZARD_SPELL", "FROSTWAVE_SPELL", "FROSTBITE_SPELL", "ICE_TOMB_SPELL",
            // 雷
            "CHAIN_LIGHTNING_SPELL", "BALL_LIGHTNING_SPELL", "LIGHTNING_BOLT_SPELL",
            "LIGHTNING_LANCE_SPELL", "THUNDERSTORM_SPELL", "VOLT_STRIKE_SPELL", "ELECTROCUTE_SPELL",
            // 奥术/虚空
            "MAGIC_ARROW_SPELL", "MAGIC_MISSILE_SPELL", "ARROW_VOLLEY_SPELL", "ELDRITCH_BLAST_SPELL",
            "BLACK_HOLE_SPELL", "FANG_STRIKE_SPELL", "DRAGON_BREATH_SPELL", "GRAVITY_FISSURE",
            // 血
            "BLOOD_SLASH_SPELL", "BLOOD_NEEDLES_SPELL", "HEARTSTOP_SPELL", "RAY_OF_SIPHONING_SPELL", "ACUPUNCTURE_SPELL",
            // 神圣
            "GUIDING_BOLT_SPELL", "STARFALL_SPELL", "SUNBEAM_SPELL", "WISP_SPELL",
            // 自然
            "POISON_ARROW_SPELL", "ACID_ORB_SPELL", "POISON_SPLASH_SPELL", "ROOT_SPELL",
            "POISON_BREATH_SPELL", "SCULK_TENTACLES_SPELL", "BLIGHT_SPELL", "FIREFLY_SWARM_SPELL"
    };

    private static ServerLevel level;
    private static UUID wizardUUID;
    private static boolean checked = false;
    private static boolean available = false;

    /**
     * 怪物施法不安全/仅玩家可用的法术黑名单(按 spellId 过滤, 防止以后又被加回列表):
     * <ul>
     *   <li>{@code spectral_hammer} 幽灵锤: 内部 {@code private Player owner}, 怪物施放时 owner=null,
     *       tick 挖掘方块时以空玩家构造 {@code BlockEvent.BreakEvent} → NPE 直接崩服(已实测)</li>
     * </ul>
     */
    private static final java.util.Set<String> UNSAFE_FOR_MOBS = java.util.Set.of(
            "irons_spellbooks:spectral_hammer"
    );

    /**
     * Iron's Spells attacks that register {@code CameraShakeData}. FTBoss must never select or
     * directly cast these: the persistent earthquake/camera shake is intentionally disabled.
     */
    private static final java.util.Set<String> CAMERA_SHAKE_SPELLS = java.util.Set.of(
            "irons_spellbooks:earthquake",
            "irons_spellbooks:shockwave",
            "irons_spellbooks:sonic_boom",
            "irons_spellbooks:divine_smite",
            "irons_spellbooks:sacrifice",
            "irons_spellbooks:raise_hell"
    );

    private WizardSummon() {}

    private static boolean cameraShakeSpell(String spellId) {
        return spellId != null && CAMERA_SHAKE_SPELLS.contains(spellId);
    }

    private static boolean cameraShakeSpell(AbstractSpell spell) {
        try {
            return spell != null && cameraShakeSpell(spell.getSpellId());
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** 确保侍从在场(死亡/丢失则重新召唤) */
    public static boolean ensure(ServerLevel lvl, double x, double y, double z, ServerPlayer target) {
        try {
            if (!checked) {
                checked = true;
                available = checkApi() && pickWizardType(lvl) != null;
                if (!available) {
                    FTBossMod.LOGGER.info("[FTBoss][铁魔法] 未找到可用法师实体, 侍从施法已禁用");
                }
            }
            if (!available) return false;
            level = lvl;
            if (wizardUUID != null) {
                Entity old = lvl.getEntity(wizardUUID);
                if (old instanceof Mob mob && mob.isAlive()) {
                    if (target != null) mob.setTarget(target);
                    return true;
                }
            }
            // 重新召唤
            EntityType<?> type = pickWizardType(lvl);
            if (type == null) { available = false; return false; }
            Entity ent = type.create(lvl);
            if (!(ent instanceof PathfinderMob pm) || !(ent instanceof IMagicEntity)) {
                ent.discard();
                available = false;
                return false;
            }
            ent.setPos(x, y, z);
            ent.setCustomName(Component.literal("§d糖霜·魔法侍从"));
            ent.setCustomNameVisible(true);
            if (ent instanceof Mob mob) {
                mob.setNoAi(false);
                mob.setPersistenceRequired();
                mob.setTarget(target);
                mob.setAggressive(true);
                // 挂铁魔法官方施法AI
                WizardAttackGoal goal = new WizardAttackGoal((IMagicEntity) ent, 1.05, 18, 36)
                        .setSpells(spellPool(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
                        .setSpellQuality(2f, 4f);
                pm.goalSelector.addGoal(1, goal);
                // 去掉原伤害AI以外的杂项? 保留默认, 法师自身AI会施法近战
            }
            lvl.addFreshEntity(ent);
            net.farmingtales.ftboss.boss.ai.MinionBlessing.mark(ent);   // 侍从可继承女王低级增益
            wizardUUID = ent.getUUID();
            FTBossMod.LOGGER.info("[FTBoss][铁魔法] 魔法侍从已召唤: {}", net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(ent.getType()));
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][铁魔法] 侍从召唤异常, 已禁用: {}", t.toString());
            available = false;
            return false;
        }
    }

    public static void despawnAll() {
        if (level != null && wizardUUID != null) {
            Entity e = level.getEntity(wizardUUID);
            if (e != null) e.kill();
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
        } catch (Throwable t) {
            return false;
        }
    }

    private static EntityType<?> pickWizardType(ServerLevel lvl) {
        for (String id : WIZARD_IDS) {
            EntityType<?> t = ForgeRegistries.ENTITY_TYPES.getValue(new net.minecraft.resources.ResourceLocation(id));
            if (t != null) return t;
        }
        return null;
    }

    /** 从 SpellRegistry 反射取法术对象列表(女王/侍从共用) */
    public static List<AbstractSpell> spellPool() {
        try {
            return resolve(SPELL_FIELDS);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /**
     * 让任意铁魔法生物对目标施放指定法术(2026-09-11 抽出: 女王本体/沙耶/酒桶王/克隆体共用)。
     * <p>先反射绑定 {@code TargetEntityCastData}(法术才知道朝谁放), 再 {@code initiateCastSpell}。
     * 失败只记日志, 不抛异常(铁魔法缺失时整条链路自动失效)。
     */
    /**
     * **不依赖 IMagicEntity 的直接施法**(2026-09-11 修"故障机器人完全不放铁魔法")。
     * 没有实现 {@code IMagicEntity} 的生物(如 DefectCoreEntity)走这条路: 直接调
     * {@code AbstractSpell#castSpell} —— 跳过施法动画/冷却系统, 但法术本体照常生效。
     * @return 是否成功
     */
    /** 按法术 id 直接施放(2026-09-11, 供"黑球 5% 黑洞"用) */
    public static boolean castByName(net.minecraft.server.level.ServerLevel sl,
                                     net.minecraft.world.entity.LivingEntity caster,
                                     net.minecraft.world.entity.LivingEntity target,
                                     String spellId, int level) {
        if (cameraShakeSpell(spellId)) {
            FTBossMod.LOGGER.info("[FTBoss][铁魔法] 已拦截带地震/震屏效果的法术: {}", spellId);
            return false;
        }
        if (spellId == null || UNSAFE_FOR_MOBS.contains(spellId)) return false;
        try {
            AbstractSpell spell = io.redspace.ironsspellbooks.api.registry.SpellRegistry
                    .getSpell(new net.minecraft.resources.ResourceLocation(spellId));
            if (spell == null) {
                FTBossMod.LOGGER.warn("[FTBoss][铁魔法] 找不到法术 {}", spellId);
                return false;
            }
            int lvl = net.farmingtales.ftboss.integ.spells.BossIronSpellLevels.castLevel(caster, spell,
                    Math.max(spell.getMinLevel(), Math.min(level, spell.getMaxLevel())));
            if (!net.farmingtales.ftboss.integ.spells.BossSpellPacing.tryLegacyCast(caster, spell)) return false;
            spell.onCast(sl, lvl, caster,
                    io.redspace.ironsspellbooks.api.spells.CastSource.MOB,
                    io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(caster));
            FTBossMod.LOGGER.info("[FTBoss][铁魔法] {} lv{} -> {}", spellId, lvl,
                    target == null ? "-" : target.getName().getString());
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][铁魔法] 施放 {} 失败: {}", spellId, t.toString());
            return false;
        }
    }

    public static boolean castDirect(net.minecraft.world.entity.LivingEntity caster,
                                     AbstractSpell spell,
                                     int level,
                                     net.minecraft.world.entity.LivingEntity target) {
        if (caster == null || spell == null || cameraShakeSpell(spell) || UNSAFE_FOR_MOBS.contains(spell.getSpellId())) return false;
        if (!net.farmingtales.ftboss.integ.spells.BossSpellPacing.tryLegacyCast(caster, spell)) return false;
        try {
            int lvl = net.farmingtales.ftboss.integ.spells.BossIronSpellLevels.castLevel(caster, spell,
                    Math.max(spell.getMinLevel(), Math.min(level + net.farmingtales.ftboss.boss.ai.BossEmpower.spellBonusFor(caster), spell.getMaxLevel())));
            // 生物没有 ServerPlayer 重载, 必须走 onCast(Level,int,LivingEntity,CastSource,MagicData)
            spell.onCast(caster.level(), lvl, caster,
                    io.redspace.ironsspellbooks.api.spells.CastSource.MOB,
                    io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(caster));
            FTBossMod.LOGGER.info("[FTBoss][铁魔法直放] {} lv{} 目标={}", spell.getSpellId(), lvl,
                    target == null ? "-" : target.getName().getString());
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][铁魔法直放] 失败 {}: {}", spell.getSpellId(), t.toString());
            return false;
        }
    }

    public static boolean castAt(io.redspace.ironsspellbooks.api.entity.IMagicEntity mob,
                              net.minecraft.world.entity.LivingEntity target,
                              AbstractSpell spell, int level) {
        if (mob == null || spell == null || cameraShakeSpell(spell) || UNSAFE_FOR_MOBS.contains(spell.getSpellId())) return false;
        if (mob instanceof net.minecraft.world.entity.LivingEntity caster
                && !net.farmingtales.ftboss.integ.spells.BossSpellPacing.tryLegacyCast(caster, spell)) return false;
        try {
            if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
                try {
                    Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                    for (var ctor : tdc.getConstructors()) {
                        if (ctor.getParameterCount() == 1
                                && ctor.getParameterTypes()[0].isAssignableFrom(net.minecraft.world.entity.player.Player.class)) {
                            Object inst = ctor.newInstance(sp);
                            java.lang.reflect.Method set = io.redspace.ironsspellbooks.api.magic.MagicData.class
                                    .getMethod("setAdditionalCastData", io.redspace.ironsspellbooks.api.spells.ICastData.class);
                            Object md = io.redspace.ironsspellbooks.api.magic.MagicData
                                    .getPlayerMagicData((net.minecraft.world.entity.LivingEntity) mob);
                            set.invoke(md, inst);
                            break;
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
            int castLevel = net.farmingtales.ftboss.integ.spells.BossIronSpellLevels.castLevel(mob, spell,
                    level + net.farmingtales.ftboss.boss.ai.BossEmpower.spellBonusFor(mob));
            mob.initiateCastSpell(spell, castLevel);
            FTBossMod.LOGGER.info("[FTBoss][铁魔法] 施法: {} lv{} 目标={}", spell.getSpellId(), castLevel,
                    target == null ? "-" : target.getName().getString());
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][铁魔法] 施法异常: {}", t.toString());
            return false;
        }
    }

    /** 从池里随机挑一个法术并按狂暴层级算等级(克隆体用) */
    public static void castRandom(io.redspace.ironsspellbooks.api.entity.IMagicEntity mob,
                                  net.minecraft.world.entity.LivingEntity target,
                                  net.minecraft.util.RandomSource random, int baseLevel) {
        List<AbstractSpell> pool = spellPool();
        if (pool.isEmpty()) return;
        AbstractSpell s = pool.get(random.nextInt(pool.size()));
        int lvl = Math.max(s.getMinLevel(), Math.min(baseLevel, s.getMaxLevel()));
        castAt(mob, target, s, lvl);
    }

    /**
     * 按稀有度挑法术**(2026-09-10 新增, 给"狂暴时更倾向史诗/传说法术"用)**。
     *
     * @param castLevel  预计施放等级(铁魔法的稀有度是随等级变的: {@code spell.getRarity(level)})
     * @param minOrdinal 最低稀有度序号: 0=COMMON 1=UNCOMMON 2=RARE <b>3=EPIC 4=LEGENDARY</b>
     * @return 满足条件的法术; 若一个都没有(或反射失败)则**回退整池**, 避免 Boss 无术可放
     */
    public static List<AbstractSpell> spellPoolRare(int castLevel, int minOrdinal) {
        List<AbstractSpell> all = spellPool();
        List<AbstractSpell> out = new ArrayList<>();
        for (AbstractSpell s : all) {
            try {
                if (s.getRarity(net.farmingtales.ftboss.integ.spells.BossIronSpellLevels.advancedLevel(s, castLevel)).ordinal() >= minOrdinal) out.add(s);
            } catch (Throwable ignored) {
            }
        }
        return out.isEmpty() ? all : out;
    }

    /** 该法术在指定等级下的稀有度序号(取不到返回 0) */
    public static int rarityOf(AbstractSpell s, int castLevel) {
        try {
            return s.getRarity(net.farmingtales.ftboss.integ.spells.BossIronSpellLevels.advancedLevel(s, castLevel)).ordinal();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** 是否火系法术(酒桶王"火焰法术更快 + 火焰弹"用) */
    public static boolean isFire(AbstractSpell s) {
        try {
            String id = s.getSpellId();
            if (id != null) {
                String low = id.toLowerCase(java.util.Locale.ROOT);
                if (low.contains("fire") || low.contains("magma") || low.contains("blaze")
                        || low.contains("flame") || low.contains("scorch") || low.contains("heat")) return true;
            }
            var school = s.getSchoolType();
            if (school != null) {
                String sid = String.valueOf(school.getId()).toLowerCase(java.util.Locale.ROOT);
                return sid.contains("fire");
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * 取指定 SpellRegistry 静态字段名的法术(沙耶等 Boss 的专属补充池用)。
     * 字段不存在/反射失败自动跳过, 沿用同一套"怪物不安全"黑名单。
     */
    public static List<AbstractSpell> spellPoolOf(String... fieldNames) {
        try {
            return resolve(fieldNames);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private static List<AbstractSpell> resolve(String[] fields) throws Exception {
        List<AbstractSpell> out = new ArrayList<>();
        Class<?> registry = Class.forName("io.redspace.ironsspellbooks.api.registry.SpellRegistry");
        for (String name : fields) {
            try {
                Object ro = registry.getField(name).get(null);
                Object spell = ro.getClass().getMethod("get").invoke(ro);
                if (spell instanceof AbstractSpell as && as.isEnabled()) {
                    String id = as.getSpellId();
                    if (cameraShakeSpell(id)) {
                        FTBossMod.LOGGER.info("[FTBoss][铁魔法] 已排除带地震/震屏效果的法术: {}", id);
                        continue;
                    }
                    if (id != null && UNSAFE_FOR_MOBS.contains(id)) {
                        FTBossMod.LOGGER.info("[FTBoss][铁魔法] 已排除仅玩家可用法术: {}", id);
                        continue;
                    }
                    out.add(as);
                }
            } catch (Exception ignored) {
            }
        }
        return out;
    }
}
