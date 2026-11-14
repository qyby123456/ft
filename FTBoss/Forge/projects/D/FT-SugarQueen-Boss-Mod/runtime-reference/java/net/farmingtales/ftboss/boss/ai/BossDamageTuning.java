/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public final class BossDamageTuning {
    private static ResourceKey<DamageType> TRUE_DMG_KEY = null;
    private static boolean TRUE_DMG_TRIED = false;

    private BossDamageTuning() {
    }

    public static DamageSource trueDamage(ServerLevel sl, Entity src) {
        if (!TRUE_DMG_TRIED) {
            TRUE_DMG_TRIED = true;
            try {
                Registry reg = sl.m_9598_().m_175515_(Registries.f_268580_);
                for (String p : new String[]{"electrocution", "assassination", "hemorrhage", "frenzied_burn", "venom", "cacophony", "doom"}) {
                    ResourceKey key = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("armamentarium", p));
                    if (!reg.m_142003_(key)) continue;
                    TRUE_DMG_KEY = key;
                    FTBossMod.LOGGER.info("[FTBoss][\u4f24\u5bb3] \u771f\u5b9e\u4f24\u5bb3\u7c7b\u578b = armamentarium:{}", (Object)p);
                    break;
                }
            }
            catch (Throwable reg) {
                // empty catch block
            }
            if (TRUE_DMG_KEY == null) {
                FTBossMod.LOGGER.info("[FTBoss][\u4f24\u5bb3] \u672a\u627e\u5230 armamentarium \u771f\u5b9e\u4f24\u5bb3\u7c7b\u578b -> \u9000\u56de\u9b54\u6cd5\u4f24\u5bb3");
            }
        }
        if (TRUE_DMG_KEY == null) {
            return sl.m_269111_().m_269425_();
        }
        try {
            Holder.Reference holder = sl.m_9598_().m_175515_(Registries.f_268580_).m_246971_(TRUE_DMG_KEY);
            return new DamageSource((Holder)holder, src, src);
        }
        catch (Throwable t) {
            return sl.m_269111_().m_269425_();
        }
    }
}

