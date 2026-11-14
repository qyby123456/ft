package net.farmingtales.ftboss.turret;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

/** 绿白月轮炮台独立注册，不依赖旧主类的手工初始化。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class XuanlanTurrets {
    public static final ResourceLocation ID = new ResourceLocation("ftboss", "xuanlan_moon_turret");
    public static EntityType<XuanlanTurretEntity> TYPE;
    public static EntityType<ZhuniEntity> BIRD;
    public static EntityType<TurretFlywheelEntity> FLYWHEEL;

    private XuanlanTurrets() {}

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, ID, () -> TYPE = EntityType.Builder
                .<XuanlanTurretEntity>of(XuanlanTurretEntity::new, MobCategory.MISC).sized(1.1F, 1.8F)
                .clientTrackingRange(8).updateInterval(2).noSave().build(ID.toString()));
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, new ResourceLocation("ftboss", "zhuni"), () -> BIRD = EntityType.Builder
                .<ZhuniEntity>of(ZhuniEntity::new, MobCategory.MISC).sized(0.65F, 1.15F)
                .clientTrackingRange(8).updateInterval(2).noSave().build("ftboss:zhuni"));
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, new ResourceLocation("ftboss", "turret_flywheel"), () -> FLYWHEEL = EntityType.Builder
                .<TurretFlywheelEntity>of(TurretFlywheelEntity::new, MobCategory.MISC).sized(0.4F, 0.4F)
                .clientTrackingRange(8).updateInterval(1).noSave().build("ftboss:turret_flywheel"));
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(TYPE, XuanlanTurretEntity.attributes().build());
        event.put(BIRD, ZhuniEntity.attributes().build());
    }
}
