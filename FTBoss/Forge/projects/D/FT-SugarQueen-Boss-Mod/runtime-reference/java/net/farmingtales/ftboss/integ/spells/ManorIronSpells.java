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
 *  net.minecraftforge.common.MinecraftForge
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
import net.farmingtales.ftboss.integ.ManorSpellBridge;
import net.farmingtales.ftboss.integ.QizaiCrystalBridge;
import net.farmingtales.ftboss.integ.QizaiSpellBridge;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellCombat;
import net.farmingtales.ftboss.integ.spells.ManorSpellDrops;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.integ.spells.QizaiCrystalIronShield;
import net.farmingtales.ftboss.integ.spells.QizaiIronSpells;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ManorIronSpells {
    private static final DeferredRegister<AbstractSpell> REGISTER = DeferredRegister.create((ResourceKey)SpellRegistry.SPELL_REGISTRY_KEY, (String)"ftboss");
    private static final Map<ManorSpellRules.Kind, RegistryObject<AbstractSpell>> SPELLS = new EnumMap<ManorSpellRules.Kind, RegistryObject<AbstractSpell>>(ManorSpellRules.Kind.class);

    private ManorIronSpells() {
    }

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
        ManorSpellBridge.install(ManorSpellCombat::replace);
        QizaiSpellBridge.install(QizaiIronSpells::castBoss);
        QizaiSpellBridge.installSupport(QizaiIronSpells::castSupport);
        QizaiCrystalBridge.install(new QizaiCrystalIronShield());
        bus.addListener(ManorIronSpells::creative);
        MinecraftForge.EVENT_BUS.register(ManorSpellCombat.class);
        MinecraftForge.EVENT_BUS.register(ManorSpellDrops.class);
        MinecraftForge.EVENT_BUS.register(ManorSpellEffects.class);
    }

    public static ManorSignatureSpell spell(ManorSpellRules.Kind kind) {
        return (ManorSignatureSpell)((Object)SPELLS.get((Object)kind).get());
    }

    public static ItemStack scroll(ManorSpellRules.Kind kind, int level) {
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        ItemStack stack = new ItemStack((ItemLike)ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer((AbstractSpell)spell, (int)spell.getMaxLevel(), (ItemStack)stack);
        return stack;
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get()) {
            return;
        }
        for (ManorSpellRules.Kind kind : ManorSpellRules.Kind.values()) {
            ManorSignatureSpell spell = ManorIronSpells.spell(kind);
            if (!spell.isEnabled()) continue;
            event.m_246342_(ManorIronSpells.scroll(kind, spell.getMaxLevel()));
        }
    }

    static {
        for (ManorSpellRules.Kind kind : ManorSpellRules.Kind.values()) {
            SPELLS.put(kind, (RegistryObject<AbstractSpell>)REGISTER.register(kind.id, () -> new ManorSignatureSpell(kind)));
        }
    }
}

