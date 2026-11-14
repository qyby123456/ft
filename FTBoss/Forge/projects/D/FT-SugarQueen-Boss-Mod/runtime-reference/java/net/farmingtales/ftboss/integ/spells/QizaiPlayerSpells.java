/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.registry.SpellRegistry
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.ISpellContainer
 *  io.redspace.ironsspellbooks.registries.ItemRegistry
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.integ.spells.QizaiSpell;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class QizaiPlayerSpells {
    private static final DeferredRegister<AbstractSpell> REGISTER = DeferredRegister.create((ResourceKey)SpellRegistry.SPELL_REGISTRY_KEY, (String)"ftboss");
    private static final Map<Kind, RegistryObject<AbstractSpell>> SPELLS = new EnumMap<Kind, RegistryObject<AbstractSpell>>(Kind.class);

    private QizaiPlayerSpells() {
    }

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
        bus.addListener(QizaiPlayerSpells::creative);
    }

    public static QizaiSpell spell(Kind kind) {
        return (QizaiSpell)((Object)SPELLS.get((Object)kind).get());
    }

    public static ItemStack scroll(Kind kind) {
        ItemStack stack = new ItemStack((ItemLike)ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer((AbstractSpell)QizaiPlayerSpells.spell(kind), (int)1, (ItemStack)stack);
        return stack;
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get()) {
            return;
        }
        for (Kind kind : Kind.values()) {
            if (!QizaiPlayerSpells.spell(kind).isEnabled()) continue;
            event.m_246342_(QizaiPlayerSpells.scroll(kind));
        }
    }

    static {
        for (Kind kind : Kind.values()) {
            SPELLS.put(kind, (RegistryObject<AbstractSpell>)REGISTER.register(kind.id, () -> new QizaiSpell(kind)));
        }
    }

    public static enum Kind {
        STAR("qizai_star_volley", 1, 24, 45, 6, 24),
        CIRCLE("qizai_starfall", 2, 40, 75, 12, 32),
        PRAYER("qizai_crimson_prayer", 3, 64, 130, 28, 60),
        STEP("qizai_starstep", 4, 0, 35, 8, 8);

        public final String id;
        public final int cue;
        public final int damage;
        public final int mana;
        public final int cooldown;
        public final int castTicks;

        private Kind(String id, int cue, int damage, int mana, int cooldown, int castTicks) {
            this.id = id;
            this.cue = cue;
            this.damage = damage;
            this.mana = mana;
            this.cooldown = cooldown;
            this.castTicks = castTicks;
        }
    }
}

