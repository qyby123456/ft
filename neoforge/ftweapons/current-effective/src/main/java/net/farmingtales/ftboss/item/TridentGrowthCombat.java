package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.integ.FoodWeaponTridentCarrierReader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class TridentGrowthCombat {
    private TridentGrowthCombat() {
    }

    public static double damageBonus(ItemStack var0) {
        if (var0.getItem() instanceof TridentItem && !FoodWeaponLifecycle.isBroken(var0)) {
            double[] damage = {0.0};
            var0.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                if (attribute.equals(Attributes.ATTACK_DAMAGE)
                    && modifier.operation() == Operation.ADD_VALUE
                    && NativeEquipmentAttributes.tridentDamageId(modifier.id())) {
                    damage[0] += modifier.amount();
                }
            });
            return Double.isFinite(damage[0]) ? damage[0] : 0.0;
        } else {
            return 0.0;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOW
    )
    public static void damage(BeforeMitigation var0) {
        if (!var0.isCanceled()
            && Float.isFinite(var0.getAmount())
            && !(var0.getAmount() <= 0.0F)
            && var0.getSource().getDirectEntity() instanceof ThrownTrident var1
            && var1.getOwner() instanceof ServerPlayer var2
            && var0.getSource().getEntity() == var2
            && var0.getSource().is(DamageTypeTags.IS_PROJECTILE)
            && var0.getEntity() != var2
            && !var0.getEntity().isAlliedTo(var2)) {
            double var6 = (double)var0.getAmount() + damageBonus(FoodWeaponTridentCarrierReader.readOriginalItem(var1));
            if (Double.isFinite(var6)) {
                var0.setAmount((float)Math.max(0.0, var6));
            }
        }
    }
}
