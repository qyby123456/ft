package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ManorIronSpells {
    private static final DeferredRegister<AbstractSpell> REGISTER = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, "ftboss");
    private static final Map<Kind, RegistryObject<AbstractSpell>> SPELLS = new EnumMap<>(Kind.class);

    static {
        for (Kind kind : Kind.values()) SPELLS.put(kind, REGISTER.register(kind.id, () -> new ManorSignatureSpell(kind)));
    }

    private ManorIronSpells() {}

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
        net.farmingtales.ftboss.integ.ManorSpellBridge.install(ManorSpellCombat::replace);
        net.farmingtales.ftboss.integ.QizaiSpellBridge.install(QizaiIronSpells::castBoss);
        /* ★ 2026-09-20 v188: 祁崽还能抽庄园的辅助法术做防御（用户要求「允许使用铁魔法防御」） */
        net.farmingtales.ftboss.integ.QizaiSpellBridge.installSupport(QizaiIronSpells::castSupport);
        net.farmingtales.ftboss.integ.QizaiCrystalBridge.install(new QizaiCrystalIronShield());
        bus.addListener(ManorIronSpells::creative);
        MinecraftForge.EVENT_BUS.register(ManorSpellCombat.class);
        MinecraftForge.EVENT_BUS.register(ManorSpellDrops.class);
        MinecraftForge.EVENT_BUS.register(ManorSpellEffects.class);
        MinecraftForge.EVENT_BUS.register(ManorRewardBalance.class);
    }

    public static ManorSignatureSpell spell(Kind kind) { return (ManorSignatureSpell) SPELLS.get(kind).get(); }

    public static ItemStack scroll(Kind kind, int level) {
        ManorSignatureSpell spell = spell(kind);
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, spell.getMaxLevel(), stack);
        return stack;
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get()) return;
        for (Kind kind : Kind.values()) {
            ManorSignatureSpell spell = spell(kind);
            if (!spell.isEnabled()) continue;
            event.accept(scroll(kind, spell.getMaxLevel()));
        }
    }
}
