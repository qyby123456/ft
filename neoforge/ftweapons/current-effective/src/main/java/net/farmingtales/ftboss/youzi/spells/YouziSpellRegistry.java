package net.farmingtales.ftboss.youzi.spells;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class YouziSpellRegistry {
    private static final ResourceLocation STAR_ID = ResourceLocation.fromNamespaceAndPath("ftboss", "youzi_homing_star");
    private static EntityType<YouziStarProjectile> star;

    @SubscribeEvent
    public static void register(RegisterEvent var0) {
        var0.register(
            Registries.ENTITY_TYPE,
            var0x -> {
                star = Builder.<YouziStarProjectile>of(YouziStarProjectile::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .noSave()
                    .fireImmune()
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(STAR_ID.toString());
                var0x.register(STAR_ID, star);
            }
        );
    }

    public static EntityType<YouziStarProjectile> star() {
        if (star == null) {
            throw new IllegalStateException("Youzi star entity type has not been registered");
        } else {
            return star;
        }
    }
}
