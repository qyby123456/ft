package net.farmingtales.ftboss.integ;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RagingAttributeCompat {
    private RagingAttributeCompat() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeModificationEvent event) {
        if (!ModList.get().isLoaded("brewinandchewin")) return;
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (key != null && FTBossMod.MOD_ID.equals(key.getNamespace()) && !event.has(type, Attributes.ATTACK_SPEED)) {
                event.add(type, Attributes.ATTACK_SPEED);
            }
        }
    }
}
