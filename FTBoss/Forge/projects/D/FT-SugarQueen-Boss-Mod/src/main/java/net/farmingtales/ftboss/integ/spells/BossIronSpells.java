package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class BossIronSpells {
    private static final DeferredRegister<AbstractSpell> REGISTER = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, FTBossMod.MOD_ID);
    private static final Map<Kind, RegistryObject<AbstractSpell>> SPELLS = new EnumMap<>(Kind.class);

    static {
        for (Kind kind : Kind.values()) SPELLS.put(kind, REGISTER.register(kind.spell, () -> new BossSignatureSpell(kind)));
    }

    private BossIronSpells() {}

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
        bus.addListener(BossIronSpells::creative);
        MinecraftForge.EVENT_BUS.register(BossSpellCombat.class);
        MinecraftForge.EVENT_BUS.register(BossSpellDrops.class);
        MinecraftForge.EVENT_BUS.register(BossSpellEffects.class);
        MinecraftForge.EVENT_BUS.register(BossSecondSpellEffects.class);
        MinecraftForge.EVENT_BUS.register(BossExpandedSpellEffects.class);
    }

    public static BossSignatureSpell spell(Kind kind) { return (BossSignatureSpell) SPELLS.get(kind).get(); }

    public static ItemStack scroll(Kind kind, int level) {
        BossSignatureSpell spell = spell(kind);
        ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, spell.getMaxLevel(), scroll);
        return scroll;
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get()) return;
        for (Kind kind : Kind.values()) {
            if (!spell(kind).isEnabled()) continue;
            event.accept(scroll(kind, spell(kind).getMaxLevel()));
        }
    }
}
