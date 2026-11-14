/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraftforge.event.entity.EntityAttributeCreationEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 *  net.minecraftforge.registries.RegisterEvent
 */
package net.farmingtales.ftboss.turret;

import net.farmingtales.ftboss.turret.TurretFlywheelEntity;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.ZhuniEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class XuanlanTurrets {
    public static final ResourceLocation ID = new ResourceLocation("ftboss", "xuanlan_moon_turret");
    public static EntityType<XuanlanTurretEntity> TYPE;
    public static EntityType<ZhuniEntity> BIRD;
    public static EntityType<TurretFlywheelEntity> FLYWHEEL;

    private XuanlanTurrets() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, ID, () -> {
            TYPE = EntityType.Builder.m_20704_(XuanlanTurretEntity::new, (MobCategory)MobCategory.MISC).m_20699_(1.1f, 1.8f).m_20702_(8).m_20717_(2).m_20716_().m_20712_(ID.toString());
            return TYPE;
        });
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, new ResourceLocation("ftboss", "zhuni"), () -> {
            BIRD = EntityType.Builder.m_20704_(ZhuniEntity::new, (MobCategory)MobCategory.MISC).m_20699_(0.65f, 1.15f).m_20702_(8).m_20717_(2).m_20716_().m_20712_("ftboss:zhuni");
            return BIRD;
        });
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, new ResourceLocation("ftboss", "turret_flywheel"), () -> {
            FLYWHEEL = EntityType.Builder.m_20704_(TurretFlywheelEntity::new, (MobCategory)MobCategory.MISC).m_20699_(0.4f, 0.4f).m_20702_(8).m_20717_(1).m_20716_().m_20712_("ftboss:turret_flywheel");
            return FLYWHEEL;
        });
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(TYPE, XuanlanTurretEntity.attributes().m_22265_());
        event.put(BIRD, ZhuniEntity.attributes().m_22265_());
    }
}

