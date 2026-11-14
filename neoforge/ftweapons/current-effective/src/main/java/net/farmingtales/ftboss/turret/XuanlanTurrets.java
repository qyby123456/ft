package net.farmingtales.ftboss.turret;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class XuanlanTurrets {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("ftboss", "xuanlan_moon_turret");
    public static EntityType<XuanlanTurretEntity> TYPE;
    public static EntityType<ZhuniEntity> BIRD;
    public static EntityType<TurretFlywheelEntity> FLYWHEEL;

    private XuanlanTurrets() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(
            Registries.ENTITY_TYPE,
            ID,
            () -> TYPE = Builder.<XuanlanTurretEntity>of(XuanlanTurretEntity::new, MobCategory.MISC)
                    .sized(1.1F, 1.8F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .noSave()
                    .build(ID.toString())
        );
        event.register(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("ftboss", "zhuni"),
            () -> BIRD = Builder.<ZhuniEntity>of(ZhuniEntity::new, MobCategory.MISC)
                    .sized(0.65F, 1.15F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .noSave()
                    .build("ftboss:zhuni")
        );
        event.register(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("ftboss", "turret_flywheel"),
            () -> FLYWHEEL = Builder.<TurretFlywheelEntity>of(TurretFlywheelEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .noSave()
                    .build("ftboss:turret_flywheel")
        );
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(TYPE, XuanlanTurretEntity.attributes().build());
        event.put(BIRD, ZhuniEntity.attributes().build());
    }
}
