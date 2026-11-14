package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;

/**
 * 【铁魔法桥】(2026-09-12 v28 用户定案, v48 修正): 让自建 Boss 也能**真的施放 Iron's Spells 法术**。
 *
 * <p>v48 修正(用户日志里的 {@code NoSuchMethodException}):
 * 以前按 {@code onCast(Level, int, LivingEntity, CastSource, ItemStack)} 找方法 —— 但实际签名是
 * {@code onCast(Level, int, LivingEntity, CastSource, MagicData)}: 第 5 个参数是施法者的
 * **魔法数据 MagicData**, 不是物品栏里的法术书。所以 init 每次都抛 NoSuchMethodException,
 * 桥从来没生效过(日志: 铁魔法桥不可用 / 真施法=false), 紫菜那套"预告的法术"一直是脚本等效魔法。
 *
 * <p>v48 起改成**结构匹配**: 在 {@code AbstractSpell} 里找"名字 onCast / 5 个参数 /
 * 第 1 个是 Level / 第 3 个能接受 LivingEntity"的那个方法, 第 5 个参数用
 * {@code MagicData.getPlayerMagicData(LivingEntity)} 现场取 —— 以后铁魔法小版本换参数类型也不会断。
 *
 * <p>全程**反射**调用: 铁魔法是可选依赖, 没装或签名再变时所有方法直接返回 false(自动退回脚本魔法), 绝不崩。
 */
public final class IronSpellBridge {

    private static boolean ready = false;
    private static boolean failed = false;
    private static Method getSpell;         // SpellRegistry.getSpell(String) -> AbstractSpell
    private static Method onCast;           // AbstractSpell.onCast(Level, int, LivingEntity, CastSource, MagicData)
    private static Method getMagicData;     // MagicData.getPlayerMagicData(LivingEntity) -> MagicData
    private static Object castSourceMob;    // CastSource.MOB

    private IronSpellBridge() {}

    /** 按结构找一个方法: 名字匹配 + 5 个参数 + 第 1 个吃 Level + 第 3 个吃 LivingEntity */
    private static Method findOnCast(Class<?> abs) {
        for (Class<?> c = abs; c != null && c != Object.class; c = c.getSuperclass()) {
            Method[] ms;
            try {
                ms = c.getDeclaredMethods();
            } catch (Throwable t) {
                continue;
            }
            for (Method m : ms) {
                if (!"onCast".equals(m.getName())) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 5) continue;
                if (!p[0].isAssignableFrom(Level.class)) continue;         // 第 1 个: Level
                if (!p[2].isAssignableFrom(LivingEntity.class)) continue;  // 第 3 个: 施法者
                try { m.setAccessible(true); } catch (Throwable ignored) { /* 公开方法也能用 */ }
                return m;
            }
        }
        return null;
    }

    private static synchronized boolean init() {
        if (ready) return true;
        if (failed) return false;
        try {
            Class<?> reg = Class.forName("io.redspace.ironsspellbooks.api.registry.SpellRegistry");
            Class<?> abs = Class.forName("io.redspace.ironsspellbooks.api.spells.AbstractSpell");
            Class<?> src = Class.forName("io.redspace.ironsspellbooks.api.spells.CastSource");
            getSpell = reg.getMethod("getSpell", String.class);

            onCast = findOnCast(abs);
            if (onCast == null) throw new IllegalStateException("AbstractSpell.onCast 找不到(签名又变了)");

            /* v48: 第 5 个参数(MagicData)现场取 —— 铁魔法给每个生物挂的那份魔法数据 */
            Class<?> magic = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            try {
                getMagicData = magic.getMethod("getPlayerMagicData", LivingEntity.class);
            } catch (NoSuchMethodException nsm) {
                getMagicData = magic.getMethod("getMagicData", LivingEntity.class);
            }

            for (Object c : src.getEnumConstants()) {
                if (c instanceof Enum<?> en && "MOB".equals(en.name())) { castSourceMob = c; break; }
            }
            if (castSourceMob == null) throw new IllegalStateException("CastSource.MOB 不存在");
            ready = true;
            FTBossMod.LOGGER.info("[FTBoss] 铁魔法桥就绪 —— onCast{} 可用, 自建 Boss 可以真施法",
                    java.util.Arrays.toString(onCast.getParameterTypes()));
            return true;
        } catch (Throwable t) {
            failed = true;
            FTBossMod.LOGGER.info("[FTBoss] 铁魔法桥不可用(将使用等效魔法): {}", t.toString());
            return false;
        }
    }

    /** 让 caster 真放一个法术(法术 id 形如 irons_spellbooks:blizzard); 返回是否成功 */
    public static boolean cast(ServerLevel sl, LivingEntity caster, String spellId, int level) {
        if (!init()) return false;
        try {
            Object spell = getSpell.invoke(null, spellId);
            if (spell == null) {
                /* 有些版本只认不带命名空间的 path */
                String path = spellId.contains(":") ? spellId.substring(spellId.indexOf(':') + 1) : spellId;
                spell = getSpell.invoke(null, path);
            }
            if (spell == null) {
                FTBossMod.LOGGER.warn("[FTBoss] 找不到法术 {}", spellId);
                return false;
            }
            Object magicData = getMagicData.invoke(null, caster);
            if (magicData == null) {
                FTBossMod.LOGGER.warn("[FTBoss] {} 没有魔法数据, 无法施放 {}",
                        caster.getName().getString(), spellId);
                return false;
            }
            int castLevel = net.farmingtales.ftboss.integ.spells.BossIronSpellLevels.castLevel(caster,
                    (io.redspace.ironsspellbooks.api.spells.AbstractSpell) spell, level);
            onCast.invoke(spell, sl, castLevel, caster, castSourceMob, magicData);
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 施放 {} 失败: {}", spellId, t.toString());
            return false;
        }
    }
}
