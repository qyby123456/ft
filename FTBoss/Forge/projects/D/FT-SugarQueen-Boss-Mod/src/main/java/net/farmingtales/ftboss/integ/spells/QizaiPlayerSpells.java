package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class QizaiPlayerSpells {
    public enum Kind {
        STAR("qizai_star_volley", QizaiRules.FAN, 24, 45, 6, 24),
        CIRCLE("qizai_starfall", QizaiRules.CIRCLE, 40, 75, 12, 32),
        PRAYER("qizai_crimson_prayer", QizaiRules.PRAYER, 64, 130, 28, 60),
        STEP("qizai_starstep", QizaiRules.STEP, 0, 35, 8, 8);

        public final String id;
        public final int cue;
        public final int damage;
        public final int mana;
        public final int cooldown;
        public final int castTicks;

        Kind(String id, int cue, int damage, int mana, int cooldown, int castTicks) {
            this.id = id;
            this.cue = cue;
            this.damage = damage;
            this.mana = mana;
            this.cooldown = cooldown;
            this.castTicks = net.farmingtales.ftboss.util.ManorSpellCastRules.castTicks(id, damage, true);
        }
    }

    private static final DeferredRegister<AbstractSpell> REGISTER = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, "ftboss");
    private static final Map<Kind, RegistryObject<AbstractSpell>> SPELLS = new EnumMap<>(Kind.class);

    static {
        for (Kind kind : Kind.values()) SPELLS.put(kind, REGISTER.register(kind.id, () -> new QizaiSpell(kind)));
    }

    private QizaiPlayerSpells() {}

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
        bus.addListener(QizaiPlayerSpells::creative);
    }

    public static QizaiSpell spell(Kind kind) { return (QizaiSpell) SPELLS.get(kind).get(); }

    public static ItemStack scroll(Kind kind) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell(kind), 1, stack);
        return stack;
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get()) return;
        for (Kind kind : Kind.values()) if (spell(kind).isEnabled()) event.accept(scroll(kind));
    }
}
