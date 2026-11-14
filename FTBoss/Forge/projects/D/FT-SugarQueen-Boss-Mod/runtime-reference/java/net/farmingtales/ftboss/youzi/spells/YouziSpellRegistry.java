/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 *  net.minecraftforge.registries.RegisterEvent
 */
package net.farmingtales.ftboss.youzi.spells;

import net.farmingtales.ftboss.youzi.spells.YouziStarProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class YouziSpellRegistry {
    private static final ResourceLocation STAR_ID = new ResourceLocation("ftboss", "youzi_homing_star");
    private static EntityType<YouziStarProjectile> star;

    @SubscribeEvent
    public static void register(RegisterEvent registerEvent) {
        registerEvent.register(ForgeRegistries.Keys.ENTITY_TYPES, registerHelper -> {
            star = EntityType.Builder.m_20704_(YouziStarProjectile::new, (MobCategory)MobCategory.MISC).m_20699_(0.2f, 0.2f).m_20716_().m_20719_().setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true).m_20712_(STAR_ID.toString());
            registerHelper.register(STAR_ID, star);
        });
    }

    public static EntityType<YouziStarProjectile> star() {
        if (star == null) {
            throw new IllegalStateException("Youzi star entity type has not been registered");
        }
        return star;
    }
}

