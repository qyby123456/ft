package net.farmingtales.ftboss.turret;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

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
   public static void register(RegisterEvent var0) {
      var0.register(
         Registries.ENTITY_TYPE,
         ID,
         () -> TYPE = Builder.of(XuanlanTurretEntity::new, MobCategory.MISC)
               .sized(1.1F, 1.8F)
               .clientTrackingRange(8)
               .updateInterval(2)
               .noSave()
               .build(ID.toString())
      );
      var0.register(
         Registries.ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ftboss", "zhuni"),
         () -> BIRD = Builder.of(ZhuniEntity::new, MobCategory.MISC)
               .sized(0.65F, 1.15F)
               .clientTrackingRange(8)
               .updateInterval(2)
               .noSave()
               .build("ftboss:zhuni")
      );
      var0.register(
         Registries.ENTITY_TYPE,
         ResourceLocation.fromNamespaceAndPath("ftboss", "turret_flywheel"),
         () -> FLYWHEEL = Builder.of(TurretFlywheelEntity::new, MobCategory.MISC)
               .sized(0.4F, 0.4F)
               .clientTrackingRange(8)
               .updateInterval(1)
               .noSave()
               .build("ftboss:turret_flywheel")
      );
   }

   @SubscribeEvent
   public static void attributes(EntityAttributeCreationEvent var0) {
      var0.put(TYPE, XuanlanTurretEntity.attributes().build());
      var0.put(BIRD, ZhuniEntity.attributes().build());
   }
}
