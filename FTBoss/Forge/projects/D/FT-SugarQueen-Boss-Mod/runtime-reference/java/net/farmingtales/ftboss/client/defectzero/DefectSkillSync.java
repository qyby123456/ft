/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.defectzero;

import java.util.Set;
import net.farmingtales.ftboss.client.defectzero.DefectSkillHost;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class DefectSkillSync {
    public static final String PREFIX = "defectskill:";
    private static final Set<String> SPELLS = Set.of("capacitor_overload", "repulsor_discharge", "ion_rail", "static_lattice", "emergency_reboot");

    private DefectSkillSync() {
    }

    public static boolean owns(String string) {
        return SPELLS.contains(string);
    }

    public static String action(BossSpellRules.Kind kind, int n) {
        return DefectSkillSync.owns(kind.spell) ? "defectskill:charge:" + kind.spell + ":" + Math.max(1, n) : RegularBossCastHost.action(kind);
    }

    public static void released(LivingEntity livingEntity, String string) {
        if (!livingEntity.m_9236_().f_46443_ && livingEntity instanceof DefectSkillHost) {
            DefectSkillHost defectSkillHost = (DefectSkillHost)livingEntity;
            if (DefectSkillSync.owns(string)) {
                defectSkillHost.ftDefectSkillState().start("defectskill:release:" + string + ":8", 8);
            }
        }
    }

    @SubscribeEvent
    public static void cancel(LivingEvent.LivingTickEvent livingTickEvent) {
        LivingEntity livingEntity = livingTickEvent.getEntity();
        if (livingEntity.m_9236_().f_46443_ || !(livingEntity instanceof DefectSkillHost)) {
            return;
        }
        DefectSkillHost defectSkillHost = (DefectSkillHost)livingEntity;
        if (defectSkillHost.ftDefectSkillState().name().startsWith("defectskill:charge:") && !livingEntity.getPersistentData().m_128441_("ftboss:signature_cast")) {
            defectSkillHost.ftDefectSkillState().start("", 0);
        }
    }

    public static String[] decode(String string) {
        String[] stringArray;
        String[] stringArray2 = stringArray = string == null ? new String[]{} : string.split(":");
        if (stringArray.length != 4 || !stringArray[0].equals("defectskill") || !DefectSkillSync.owns(stringArray[2]) || !stringArray[1].equals("charge") && !stringArray[1].equals("release")) {
            return new String[0];
        }
        try {
            if (Integer.parseInt(stringArray[3]) <= 0) {
                return new String[0];
            }
        }
        catch (NumberFormatException numberFormatException) {
            return new String[0];
        }
        return stringArray;
    }

    public static float progress(String string, float f) {
        return DefectSkillSync.progress(DefectSkillSync.decode(string), f);
    }

    public static boolean matchesCharge(String string, String string2) {
        String[] stringArray = DefectSkillSync.decode(string);
        return stringArray.length == 4 && stringArray[1].equals("charge") && stringArray[2].equals(string2);
    }

    public static float progress(String[] stringArray, float f) {
        if (stringArray.length == 0 || !Float.isFinite(f)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, f / (float)Integer.parseInt(stringArray[3])));
    }
}

