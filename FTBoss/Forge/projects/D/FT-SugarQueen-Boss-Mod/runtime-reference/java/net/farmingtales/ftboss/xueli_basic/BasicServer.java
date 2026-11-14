/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.xueli_basic;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_basic.BasicAccess;
import net.farmingtales.ftboss.xueli_basic.BasicMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public final class BasicServer {
    private static final String[] PIN = new String[]{"pin_diagonal", "pin_cross", "pin_press"};
    private static final String[] PAN = new String[]{"pan_forehand", "pan_backhand", "pan_rise"};
    private static final Map<EasterBossEntity, Integer> INTERVALS = new WeakHashMap<EasterBossEntity, Integer>();

    private BasicServer() {
    }

    public static String weapon(EasterBossEntity easterBossEntity) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)easterBossEntity.m_21205_().m_41720_());
        if (resourceLocation == null) {
            return "";
        }
        String string = resourceLocation.toString();
        return string.equals("bakery:rolling_pin") || string.equals("ftboss:xueli_rolling_pin") ? "pin" : (string.equals("farmersdelight:skillet") || string.equals("ftboss:xueli_skillet") ? "pan" : "");
    }

    public static boolean supported(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.getVariant() == 10 && easterBossEntity instanceof BasicAccess && easterBossEntity.m_6084_() && !easterBossEntity.m_21525_() && !easterBossEntity.isCastingLike() && !BasicServer.weapon(easterBossEntity).isEmpty();
    }

    private static String attack(String string, int n) {
        return (string.equals("pin") ? PIN : PAN)[Math.floorMod(n, 3)];
    }

    private static CompoundTag cue(EasterBossEntity easterBossEntity) {
        return ((BasicAccess)easterBossEntity).ftboss$getXueliBasic();
    }

    private static void set(EasterBossEntity easterBossEntity, CompoundTag compoundTag) {
        ((BasicAccess)easterBossEntity).ftboss$setXueliBasic(compoundTag);
    }

    private static CompoundTag create(EasterBossEntity easterBossEntity, String string, int n, int n2, int n3, Entity entity) {
        CompoundTag compoundTag = new CompoundTag();
        String string2 = BasicServer.weapon(easterBossEntity);
        compoundTag.m_128359_("phase", string);
        compoundTag.m_128359_("weapon", string2);
        compoundTag.m_128359_("attack", BasicServer.attack(string2, n3));
        compoundTag.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", Math.max(1, n));
        compoundTag.m_128405_("interval", Math.max(1, n2));
        compoundTag.m_128405_("sequence", n3);
        compoundTag.m_128379_("left", easterBossEntity.m_5737_() == HumanoidArm.LEFT);
        if (entity != null) {
            compoundTag.m_128362_("target", entity.m_20148_());
        }
        return compoundTag;
    }

    public static void prepare(EasterBossEntity easterBossEntity, LivingEntity livingEntity, double d, int n, int n2, double d2) {
        if (easterBossEntity.m_9236_().f_46443_ || !(easterBossEntity instanceof BasicAccess)) {
            return;
        }
        if (easterBossEntity.getVariant() == 10) {
            INTERVALS.put(easterBossEntity, Math.max(1, n2));
        }
        CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
        boolean bl = compoundTag.m_128461_("phase").equals("wind");
        if (!BasicServer.supported(easterBossEntity) || livingEntity == null || !livingEntity.m_6084_() || d > d2 || !easterBossEntity.m_142582_((Entity)livingEntity)) {
            if (bl) {
                BasicServer.set(easterBossEntity, new CompoundTag());
            }
            return;
        }
        if (n <= 0 || n > BasicMath.wind(n2)) {
            return;
        }
        long l = easterBossEntity.m_9236_().m_46467_();
        if (bl && compoundTag.m_128461_("weapon").equals(BasicServer.weapon(easterBossEntity)) && compoundTag.m_128403_("target") && compoundTag.m_128342_("target").equals(livingEntity.m_20148_()) && l - compoundTag.m_128454_("start") < (long)compoundTag.m_128451_("duration")) {
            return;
        }
        int n3 = compoundTag.m_128451_("sequence") + (compoundTag.m_128461_("phase").equals("release") ? 1 : 0);
        BasicServer.set(easterBossEntity, BasicServer.create(easterBossEntity, "wind", n, n2, n3, (Entity)livingEntity));
    }

    public static void strike(EasterBossEntity easterBossEntity, Entity entity) {
        if (easterBossEntity.m_9236_().f_46443_ || !BasicServer.supported(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
        int n = INTERVALS.getOrDefault(easterBossEntity, compoundTag.m_128456_() ? 20 : Math.max(1, compoundTag.m_128451_("interval")));
        int n2 = compoundTag.m_128451_("sequence") + (compoundTag.m_128461_("phase").equals("release") ? 1 : 0);
        BasicServer.set(easterBossEntity, BasicServer.create(easterBossEntity, "release", BasicMath.recovery(n), n, n2, entity));
    }

    public static void validate(EasterBossEntity easterBossEntity, boolean bl) {
        if (easterBossEntity.m_9236_().f_46443_ || easterBossEntity.getVariant() != 10 || !(easterBossEntity instanceof BasicAccess)) {
            return;
        }
        CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
        if (!compoundTag.m_128461_("phase").equals("wind")) {
            return;
        }
        LivingEntity livingEntity = easterBossEntity.m_5448_();
        if (!(BasicServer.supported(easterBossEntity) && !bl && livingEntity != null && livingEntity.m_6084_() && compoundTag.m_128403_("target") && compoundTag.m_128342_("target").equals(livingEntity.m_20148_()))) {
            BasicServer.set(easterBossEntity, new CompoundTag());
        }
    }

    public static void cancel(EasterBossEntity easterBossEntity, boolean bl) {
        if (easterBossEntity.m_9236_().f_46443_ || easterBossEntity.getVariant() != 10 || !(easterBossEntity instanceof BasicAccess)) {
            return;
        }
        CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
        if (!bl && compoundTag.m_128461_("phase").equals("release") && compoundTag.m_128454_("start") == easterBossEntity.m_9236_().m_46467_()) {
            return;
        }
        if (!compoundTag.m_128456_()) {
            BasicServer.set(easterBossEntity, new CompoundTag());
        }
    }
}

