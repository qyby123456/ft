package net.farmingtales.ftboss.effect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 【禁疗】(2026-09-14 v119 按用户要求「禁疗做成一个 buff」)。
 *
 * <p>以前禁疗只是 {@code HealBan} 里的一张计时表 + 借 {@code relics:anti_heal} 当图标;
 * 现在它本身就是**本模组的状态效果**, 玩家能直接看到图标与剩余时间。
 * 真正拦住回血仍然是 {@code HealBan.onHeal}(监听 {@code LivingHealEvent} 把量改成 0) ——
 * 判定与图标**同源**: 身上有这个效果就是被禁疗，不能被牛奶或净化解除，自然到期后恢复。
 */
@Mod.EventBusSubscriber(modid = "ftboss")
public class HealBanEffect extends MobEffect {

    public HealBanEffect() {
        super(MobEffectCategory.HARMFUL, 0xB01A28);   // 暗红
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRemove(MobEffectEvent.Remove event) {
        if (!event.getEntity().level().isClientSide
                && (event.getEffect() instanceof HealBanEffect || event.getEffect() instanceof ShadowErosionEffect)) {
            event.setCanceled(net.farmingtales.ftboss.boss.ai.ManorStatusImmunity.protectRemoval(true, event));
        }
    }
}
