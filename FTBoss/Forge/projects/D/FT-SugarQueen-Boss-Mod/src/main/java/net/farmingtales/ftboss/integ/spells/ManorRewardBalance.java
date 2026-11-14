package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.farmingtales.ftboss.util.ManorRewardBalanceRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorRewardBalance {
    private ManorRewardBalance() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damage(SpellDamageEvent event) {
        AbstractSpell spell = event.getSpellDamageSource().spell();
        boolean reward = spell instanceof ManorSignatureSpell || spell instanceof QizaiSpell || spell instanceof QizaiBarrageSpell;
        DamageSource source = event.getSpellDamageSource();
        event.setAmount(ManorRewardBalanceRules.damage(source.getEntity() instanceof ServerPlayer,
                reward, event.getAmount()));
    }

    public static float displayDamage(float original, LivingEntity caster) {
        return ManorRewardBalanceRules.damage(caster == null || caster instanceof Player, true, original);
    }
}
