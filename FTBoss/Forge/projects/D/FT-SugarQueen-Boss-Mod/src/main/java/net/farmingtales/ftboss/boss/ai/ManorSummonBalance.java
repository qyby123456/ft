package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.youzi.YouziMiniBalance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class ManorSummonBalance {
    public static final double MAID_HEALTH = 155.0;
    public static final double MAID_ARMOR = 55.0;

    private ManorSummonBalance() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.isCanceled() || event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof EasterBossEntity boss && YouziCloneWeapons.applies(boss)) {
            YouziMiniBalance.restore(boss);
            if (!YouziCloneWeapons.isManorWeapon(boss.getMainHandItem())) {
                YouziCloneWeapons.equipRandom(boss);
            }
        } else if (event.getEntity() instanceof LivingEntity maid && XueliMaids.isGuard(maid)) {
            float previousMaximum = maid.getMaxHealth();
            float previousHealth = maid.getHealth();
            var maximum = maid.getAttribute(Attributes.MAX_HEALTH);
            var armor = maid.getAttribute(Attributes.ARMOR);
            if (maximum != null) maximum.setBaseValue(MAID_HEALTH);
            if (armor != null) armor.setBaseValue(MAID_ARMOR);
            maid.setHealth(previousMaximum > 0.0F
                    ? Math.min(maid.getMaxHealth(), previousHealth / previousMaximum * maid.getMaxHealth()) : previousHealth);
        }
    }
}
