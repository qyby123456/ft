package net.farmingtales.ftboss.turret;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;

@EventBusSubscriber(
   modid = "ftboss",
   bus = Bus.MOD
)
public final class XuanlanTurrets {
   public static final ResourceLocation ID = new ResourceLocation("ftboss", "xuanlan_moon_turret");
   public static EntityType<XuanlanTurretEntity> TYPE;
   public static EntityType<ZhuniEntity> BIRD;
   public static EntityType<TurretFlywheelEntity> FLYWHEEL;

   private XuanlanTurrets() {
   }

   @SubscribeEvent
   public static void register(RegisterEvent event) {
      event.register(
         Keys.ENTITY_TYPES,
         ID,
         () -> TYPE = Builder.m_20704_(XuanlanTurretEntity::new, MobCategory.MISC)
               .m_20699_(1.1F, 1.8F)
               .m_20702_(8)
               .m_20717_(2)
               .m_20716_()
               .m_20712_(ID.toString())
      );
      event.register(
         Keys.ENTITY_TYPES,
         new ResourceLocation("ftboss", "zhuni"),
         () -> BIRD = Builder.m_20704_(ZhuniEntity::new, MobCategory.MISC).m_20699_(0.65F, 1.15F).m_20702_(8).m_20717_(2).m_20716_().m_20712_("ftboss:zhuni")
      );
      event.register(
         Keys.ENTITY_TYPES,
         new ResourceLocation("ftboss", "turret_flywheel"),
         () -> FLYWHEEL = Builder.m_20704_(TurretFlywheelEntity::new, MobCategory.MISC)
               .m_20699_(0.4F, 0.4F)
               .m_20702_(8)
               .m_20717_(1)
               .m_20716_()
               .m_20712_("ftboss:turret_flywheel")
      );
   }

   @SubscribeEvent
   public static void attributes(EntityAttributeCreationEvent event) {
      event.put(TYPE, XuanlanTurretEntity.attributes().m_22265_());
      event.put(BIRD, ZhuniEntity.attributes().m_22265_());
   }
}
