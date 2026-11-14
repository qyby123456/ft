/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.SimplePreparableReloadListener
 *  net.minecraft.util.profiling.ProfilerFiller
 *  org.apache.logging.log4j.LogManager
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;

public final class ArcaneAssets
extends SimplePreparableReloadListener<Map<String, ArcaneRig.Effect>> {
    public static final List<String> IDS = List.of("qizai_star_volley", "qizai_starfall", "qizai_crimson_prayer", "qizai_starstep", "star_cross", "rose_starfall", "qizai_invulnerable_aegis", "qizai_resonance_emission", "qizai_star_sweep", "qizai_sky_nova", "qizai_prism_lock", "qizai_laser_barrage");
    private static volatile Map<String, ArcaneRig.Effect> loaded = Map.of();

    public static ArcaneRig.Effect get(String string) {
        return loaded.get(string);
    }

    public static ArcaneRig.Effect kind(int n) {
        return n > 0 && n <= 6 ? ArcaneAssets.get(IDS.get(n - 1)) : (n >= 7 && n <= 10 ? ArcaneAssets.get(IDS.get(n + 1)) : null);
    }

    protected Map<String, ArcaneRig.Effect> m_5944_(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        LinkedHashMap<String, ArcaneRig.Effect> linkedHashMap = new LinkedHashMap<String, ArcaneRig.Effect>();
        for (String string : IDS) {
            try {
                BufferedReader bufferedReader = resourceManager.m_215597_(new ResourceLocation("ftboss", "qizai_arcane/" + string + ".json"));
                try {
                    ArcaneRig.Effect effect = ArcaneRig.read(bufferedReader);
                    if (!effect.id().equals(string)) {
                        throw new IllegalArgumentException("Wrong effect resource");
                    }
                    linkedHashMap.put(string, effect);
                }
                finally {
                    if (bufferedReader == null) continue;
                    bufferedReader.close();
                }
            }
            catch (Exception exception) {
                LogManager.getLogger((String)"QizaiArcaneV5").error("Failed to prepare {}", (Object)string, (Object)exception);
            }
        }
        return Map.copyOf(linkedHashMap);
    }

    protected void m_5787_(Map<String, ArcaneRig.Effect> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        loaded = map;
    }
}

