/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss;

import java.lang.reflect.Method;
import java.util.Arrays;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class IronSpellBridge {
    private static boolean ready = false;
    private static boolean failed = false;
    private static Method getSpell;
    private static Method onCast;
    private static Method getMagicData;
    private static Object castSourceMob;

    private IronSpellBridge() {
    }

    private static Method findOnCast(Class<?> abs) {
        for (Class<?> c = abs; c != null && c != Object.class; c = c.getSuperclass()) {
            Method[] ms;
            try {
                ms = c.getDeclaredMethods();
            }
            catch (Throwable t) {
                continue;
            }
            for (Method m : ms) {
                Class<?>[] p;
                if (!"onCast".equals(m.getName()) || (p = m.getParameterTypes()).length != 5 || !p[0].isAssignableFrom(Level.class) || !p[2].isAssignableFrom(LivingEntity.class)) continue;
                try {
                    m.setAccessible(true);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                return m;
            }
        }
        return null;
    }

    private static synchronized boolean init() {
        if (ready) {
            return true;
        }
        if (failed) {
            return false;
        }
        try {
            Class<?> reg = Class.forName("io.redspace.ironsspellbooks.api.registry.SpellRegistry");
            Class<?> abs = Class.forName("io.redspace.ironsspellbooks.api.spells.AbstractSpell");
            Class<?> src = Class.forName("io.redspace.ironsspellbooks.api.spells.CastSource");
            getSpell = reg.getMethod("getSpell", String.class);
            onCast = IronSpellBridge.findOnCast(abs);
            if (onCast == null) {
                throw new IllegalStateException("AbstractSpell.onCast \u627e\u4e0d\u5230(\u7b7e\u540d\u53c8\u53d8\u4e86)");
            }
            Class<?> magic = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            try {
                getMagicData = magic.getMethod("getPlayerMagicData", LivingEntity.class);
            }
            catch (NoSuchMethodException nsm) {
                getMagicData = magic.getMethod("getMagicData", LivingEntity.class);
            }
            for (Object c : src.getEnumConstants()) {
                Enum en;
                if (!(c instanceof Enum) || !"MOB".equals((en = (Enum)c).name())) continue;
                castSourceMob = c;
                break;
            }
            if (castSourceMob == null) {
                throw new IllegalStateException("CastSource.MOB \u4e0d\u5b58\u5728");
            }
            ready = true;
            FTBossMod.LOGGER.info("[FTBoss] \u94c1\u9b54\u6cd5\u6865\u5c31\u7eea \u2014\u2014 onCast{} \u53ef\u7528, \u81ea\u5efa Boss \u53ef\u4ee5\u771f\u65bd\u6cd5", (Object)Arrays.toString(onCast.getParameterTypes()));
            return true;
        }
        catch (Throwable t) {
            failed = true;
            FTBossMod.LOGGER.info("[FTBoss] \u94c1\u9b54\u6cd5\u6865\u4e0d\u53ef\u7528(\u5c06\u4f7f\u7528\u7b49\u6548\u9b54\u6cd5): {}", (Object)t.toString());
            return false;
        }
    }

    public static boolean cast(ServerLevel sl, LivingEntity caster, String spellId, int level) {
        if (!IronSpellBridge.init()) {
            return false;
        }
        try {
            Object spell = getSpell.invoke(null, spellId);
            if (spell == null) {
                String path = spellId.contains(":") ? spellId.substring(spellId.indexOf(58) + 1) : spellId;
                spell = getSpell.invoke(null, path);
            }
            if (spell == null) {
                FTBossMod.LOGGER.warn("[FTBoss] \u627e\u4e0d\u5230\u6cd5\u672f {}", (Object)spellId);
                return false;
            }
            Object magicData = getMagicData.invoke(null, caster);
            if (magicData == null) {
                FTBossMod.LOGGER.warn("[FTBoss] {} \u6ca1\u6709\u9b54\u6cd5\u6570\u636e, \u65e0\u6cd5\u65bd\u653e {}", (Object)caster.m_7755_().getString(), (Object)spellId);
                return false;
            }
            onCast.invoke(spell, sl, level, caster, castSourceMob, magicData);
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u65bd\u653e {} \u5931\u8d25: {}", (Object)spellId, (Object)t.toString());
            return false;
        }
    }
}

