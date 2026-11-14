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
package net.farmingtales.ftboss.qingyi_basic;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.farmingtales.ftboss.qingyi_basic.BasicAccess;
import net.farmingtales.ftboss.qingyi_basic.BasicMotion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public final class BasicServer {
    private static final String[] CLIPS = new String[]{"trident_thrust", "trident_sweep", "trident_backcut"};

    private BasicServer() {
    }

    public static String weapon(EasterBossEntity easterBossEntity) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)easterBossEntity.m_21205_().m_41720_());
        return resourceLocation == null ? "" : resourceLocation.toString();
    }

    public static boolean supported(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.getVariant() == 5 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && !easterBossEntity.m_21525_() && easterBossEntity.m_6084_() && easterBossEntity instanceof BasicAccess && easterBossEntity.m_21205_().m_41720_() instanceof QingyiConfluenceTridentItem;
    }

    private static CompoundTag cue(EasterBossEntity easterBossEntity) {
        return ((BasicAccess)easterBossEntity).ftboss$getQingyiBasic();
    }

    private static void set(EasterBossEntity easterBossEntity, CompoundTag compoundTag) {
        ((BasicAccess)easterBossEntity).ftboss$setQingyiBasic(compoundTag);
    }

    private static boolean ownServer(EasterBossEntity easterBossEntity) {
        return BasicServer.supported(easterBossEntity) && !easterBossEntity.m_9236_().f_46443_;
    }

    public static void retainTail(CompoundTag compoundTag, CompoundTag compoundTag2) {
        if (compoundTag.m_128441_("tailStart")) {
            compoundTag2.m_128356_("tailStart", compoundTag.m_128454_("tailStart"));
            compoundTag2.m_128359_("tailWeapon", compoundTag.m_128461_("tailWeapon"));
            compoundTag2.m_128379_("tailLeft", compoundTag.m_128471_("tailLeft"));
        }
    }

    private static CompoundTag create(EasterBossEntity easterBossEntity, Entity entity, boolean bl, int n, int n2, int n3) {
        CompoundTag compoundTag = new CompoundTag();
        BasicServer.retainTail(BasicServer.cue(easterBossEntity), compoundTag);
        compoundTag.m_128359_("clip", CLIPS[Math.floorMod(n3, 3)]);
        compoundTag.m_128379_("preparation", bl);
        compoundTag.m_128379_("active", true);
        compoundTag.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", n);
        compoundTag.m_128405_("interval", n2);
        compoundTag.m_128405_("sequence", n3 + (bl ? 0 : 1));
        compoundTag.m_128359_("weapon", BasicServer.weapon(easterBossEntity));
        compoundTag.m_128379_("left", easterBossEntity.m_5737_() == HumanoidArm.LEFT);
        if (!bl) {
            compoundTag.m_128356_("tailStart", easterBossEntity.m_9236_().m_46467_());
            compoundTag.m_128359_("tailWeapon", BasicServer.weapon(easterBossEntity));
            compoundTag.m_128379_("tailLeft", easterBossEntity.m_5737_() == HumanoidArm.LEFT);
        }
        if (entity != null) {
            compoundTag.m_128362_("target", entity.m_20148_());
        }
        return compoundTag;
    }

    public static void prepare(EasterBossEntity easterBossEntity, LivingEntity livingEntity, double d, int n, int n2, double d2, boolean bl) {
        boolean bl2;
        if (!(easterBossEntity instanceof BasicAccess) || easterBossEntity.getVariant() != 5 || easterBossEntity.m_9236_().f_46443_) {
            return;
        }
        CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
        boolean bl3 = bl2 = compoundTag.m_128471_("active") && compoundTag.m_128471_("preparation");
        if (!BasicServer.ownServer(easterBossEntity) || bl || livingEntity == null || !livingEntity.m_6084_() || d > d2 || !easterBossEntity.m_142582_((Entity)livingEntity)) {
            if (bl2) {
                BasicServer.cancel(easterBossEntity);
            }
            return;
        }
        n2 = Math.max(1, n2);
        if (compoundTag.m_128451_("interval") != n2) {
            compoundTag = compoundTag.m_6426_();
            compoundTag.m_128405_("interval", n2);
            BasicServer.set(easterBossEntity, compoundTag);
        }
        if (n < 1 || n > BasicMotion.preparation(n2)) {
            return;
        }
        long l = easterBossEntity.m_9236_().m_46467_();
        if (bl2 && compoundTag.m_128403_("target") && compoundTag.m_128342_("target").equals(livingEntity.m_20148_()) && compoundTag.m_128461_("weapon").equals(BasicServer.weapon(easterBossEntity)) && compoundTag.m_128471_("left") == (easterBossEntity.m_5737_() == HumanoidArm.LEFT) && l - compoundTag.m_128454_("start") < (long)compoundTag.m_128451_("duration")) {
            return;
        }
        BasicServer.set(easterBossEntity, BasicServer.create(easterBossEntity, (Entity)livingEntity, true, n, n2, compoundTag.m_128451_("sequence")));
    }

    public static void strike(EasterBossEntity easterBossEntity, Entity entity) {
        if (!BasicServer.ownServer(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
        int n = Math.max(1, compoundTag.m_128451_("interval"));
        BasicServer.set(easterBossEntity, BasicServer.create(easterBossEntity, entity, false, BasicMotion.recovery(n), n, compoundTag.m_128451_("sequence")));
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        if (easterBossEntity instanceof BasicAccess && easterBossEntity.getVariant() == 5 && !easterBossEntity.m_9236_().f_46443_) {
            CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
            if (!compoundTag.m_128471_("active")) {
                return;
            }
            CompoundTag compoundTag2 = new CompoundTag();
            BasicServer.retainTail(compoundTag, compoundTag2);
            compoundTag2.m_128405_("sequence", compoundTag.m_128451_("sequence"));
            compoundTag2.m_128405_("interval", compoundTag.m_128451_("interval"));
            BasicServer.set(easterBossEntity, compoundTag2);
        }
    }

    public static void validate(EasterBossEntity easterBossEntity, boolean bl) {
        if (!(easterBossEntity instanceof BasicAccess) || easterBossEntity.getVariant() != 5 || easterBossEntity.m_9236_().f_46443_) {
            return;
        }
        CompoundTag compoundTag = BasicServer.cue(easterBossEntity);
        if (!compoundTag.m_128471_("active")) {
            return;
        }
        LivingEntity livingEntity = easterBossEntity.m_5448_();
        if (!(BasicServer.supported(easterBossEntity) && !bl && compoundTag.m_128461_("weapon").equals(BasicServer.weapon(easterBossEntity)) && compoundTag.m_128471_("left") == (easterBossEntity.m_5737_() == HumanoidArm.LEFT) && easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start") < (long)compoundTag.m_128451_("duration") && (!compoundTag.m_128471_("preparation") || livingEntity != null && livingEntity.m_6084_() && compoundTag.m_128403_("target") && compoundTag.m_128342_("target").equals(livingEntity.m_20148_())))) {
            BasicServer.cancel(easterBossEntity);
        }
    }
}

