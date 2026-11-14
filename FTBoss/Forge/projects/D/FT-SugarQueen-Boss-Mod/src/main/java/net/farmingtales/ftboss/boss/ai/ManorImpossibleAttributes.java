package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorImpossibleAttributes {
    private static final UUID BONUS = UUID.fromString("27564f19-630a-4bf9-b321-e30948cad2e3");

    private ManorImpossibleAttributes() {}

    public static void refresh(EasterBossEntity boss) {
        if (boss.level().isClientSide) return;
        boolean boosted = boss.isAlive() && boss.isImpossibleChallenge() && !boss.isMini() && !boss.isGiant()
                && EasterBossEntity.isManorVariant(boss.getVariant());
        for (var attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            if (attribute == Attributes.MAX_HEALTH || attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED) continue;
            var instance = boss.getAttribute(attribute);
            if (instance == null) continue;
            if (!boosted || attribute == Attributes.MOVEMENT_SPEED || attribute == Attributes.FLYING_SPEED
                    || attribute == ForgeMod.SWIM_SPEED.get()) instance.removeModifier(BONUS);
            else if (instance.getModifier(BONUS) == null) instance.addTransientModifier(new AttributeModifier(
                    BONUS, "ftboss.manor_impossible_attributes", ManorDifficulty.IMPOSSIBLE_ATTRIBUTE_MULTIPLIER - 1.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof EasterBossEntity boss) refresh(boss);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss) refresh(boss);
    }
}
