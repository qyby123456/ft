package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.util.QizaiBarrageRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QizaiBarrageSpells {
    private static QizaiBarrageSpell spell;

    private QizaiBarrageSpells() {}

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(SpellRegistry.SPELL_REGISTRY_KEY, helper -> {
            spell = new QizaiBarrageSpell();
            helper.register(new ResourceLocation("ftboss", QizaiBarrageRules.ID), spell);
        });
    }

    public static ItemStack scroll() {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, 1, stack);
        return stack;
    }

    @SubscribeEvent
    public static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == FTCreativeTabs.MAIN.get() && spell != null && spell.isEnabled()) event.accept(scroll());
    }
}
