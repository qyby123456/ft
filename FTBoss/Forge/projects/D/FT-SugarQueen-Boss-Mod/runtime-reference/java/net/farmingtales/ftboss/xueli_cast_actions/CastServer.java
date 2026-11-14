/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.xueli_cast_actions;

import java.util.Set;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.xueli_cast_actions.CastAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.registries.ForgeRegistries;

public final class CastServer {
    private static final Set<String> WIRED = Set.of("frost_needles", "whiteout_edict", "winter_clock", "pan_cast", "stockpot_throw", "knife_throw", "clone_call", "maid_call", "roll_cast", "slam_cast");

    private CastServer() {
    }

    public static boolean wired(String string) {
        return string != null && WIRED.contains(string);
    }

    public static boolean validCue(String string, String string2, int n) {
        return CastServer.wired(string) && string2 != null && n >= 1 && n <= 240 && (string2.equals("charge") || string2.equals("release") || string2.equals("instant") || string2.equals("hold"));
    }

    public static boolean supported(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.getVariant() == 10 && !easterBossEntity.isMini() && !easterBossEntity.m_21525_() && easterBossEntity.m_6084_() && easterBossEntity instanceof CastAccess;
    }

    public static String weapon(EasterBossEntity easterBossEntity) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)easterBossEntity.m_21205_().m_41720_());
        return resourceLocation == null ? "" : resourceLocation.toString();
    }

    private static CompoundTag root(EasterBossEntity easterBossEntity) {
        return easterBossEntity.ftboss$getXueliCastActions();
    }

    private static void write(EasterBossEntity easterBossEntity, String string, CompoundTag compoundTag) {
        CompoundTag compoundTag2 = CastServer.root(easterBossEntity).m_6426_();
        compoundTag2.m_128365_(string, (Tag)compoundTag);
        easterBossEntity.ftboss$setXueliCastActions(compoundTag2);
    }

    private static CompoundTag make(EasterBossEntity easterBossEntity, String string, String string2, String string3, int n) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("id", string);
        compoundTag.m_128359_("secondary", string2);
        compoundTag.m_128359_("stage", string3);
        compoundTag.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", Math.max(1, Math.min(240, n)));
        compoundTag.m_128359_("weapon", CastServer.weapon(easterBossEntity));
        compoundTag.m_128379_("left", easterBossEntity.m_5737_() == HumanoidArm.LEFT);
        return compoundTag;
    }

    public static void iron(EasterBossEntity easterBossEntity, ManorSpellRules.Kind kind, ManorSpellRules.Kind kind2, int n) {
        if (!(CastServer.supported(easterBossEntity) && easterBossEntity.m_9236_() instanceof ServerLevel && kind != null && kind.variant == 10 && CastServer.wired(kind.id) && n >= 1)) {
            return;
        }
        CastServer.write(easterBossEntity, "iron", CastServer.make(easterBossEntity, kind.id, kind2 != null && kind2.variant == 10 && CastServer.wired(kind2.id) ? kind2.id : "", "charge", n));
    }

    public static void finishIron(EasterBossEntity easterBossEntity, boolean bl) {
        if (!(easterBossEntity instanceof CastAccess) || !(easterBossEntity.m_9236_() instanceof ServerLevel) || easterBossEntity.getVariant() != 10) {
            return;
        }
        CompoundTag compoundTag = CastServer.root(easterBossEntity).m_128469_("iron");
        if (compoundTag.m_128456_()) {
            return;
        }
        if (bl || !CastServer.wired(compoundTag.m_128461_("id"))) {
            CastServer.write(easterBossEntity, "iron", new CompoundTag());
            return;
        }
        CastServer.write(easterBossEntity, "iron", CastServer.make(easterBossEntity, compoundTag.m_128461_("id"), compoundTag.m_128461_("secondary"), "release", 10));
    }

    public static void nativeAction(EasterBossEntity easterBossEntity, String string, String string2, int n) {
        if (CastServer.validCue(string, string2, n) && CastServer.supported(easterBossEntity) && easterBossEntity.m_9236_() instanceof ServerLevel) {
            CastServer.write(easterBossEntity, "native", CastServer.make(easterBossEntity, string, "", string2, n));
        }
    }

    public static void spawned(boolean bl, EasterBossEntity easterBossEntity, String string) {
        CompoundTag compoundTag;
        if (bl && CastServer.supported(easterBossEntity) && (!(compoundTag = CastServer.root(easterBossEntity).m_128469_("native")).m_128461_("id").equals(string) || compoundTag.m_128454_("start") != easterBossEntity.m_9236_().m_46467_())) {
            CastServer.nativeAction(easterBossEntity, string, "instant", string.equals("knife_throw") ? 16 : (string.equals("stockpot_throw") ? 20 : 12));
        }
    }

    public static void reset(EasterBossEntity easterBossEntity) {
        if (easterBossEntity instanceof CastAccess && !easterBossEntity.m_9236_().f_46443_) {
            easterBossEntity.ftboss$setXueliCastActions(new CompoundTag());
        }
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        if (easterBossEntity instanceof CastAccess && easterBossEntity.getVariant() == 10 && !easterBossEntity.m_9236_().f_46443_) {
            CastServer.write(easterBossEntity, "native", new CompoundTag());
        }
    }
}

