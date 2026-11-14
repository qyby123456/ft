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
import net.farmingtales.ftboss.integ.spells.BossExpandedSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSecondSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellCombat;
import net.farmingtales.ftboss.integ.spells.BossSpellDrops;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.item.FTCreativeTabs;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class BossIronSpells {
    private static final DeferredRegister<AbstractSpell> REGISTER = DeferredRegister.create((ResourceKey)SpellRegistry.SPELL_REGISTRY_KEY, (String)"ftboss");
    private static final Map<BossSpellRules.Kind, RegistryObject<AbstractSpell>> SPELLS = new EnumMap<BossSpellRules.Kind, RegistryObject<AbstractSpell>>(BossSpellRules.Kind.class);

    private BossIronSpells() {
    }

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
        bus.addListener(BossIronSpells::creative);
        MinecraftForge.EVENT_BUS.register(BossSpellCombat.class);
        MinecraftForge.EVENT_BUS.register(BossSpellDrops.class);
        MinecraftForge.EVENT_BUS.register(BossSpellEffects.class);
        MinecraftForge.EVENT_BUS.register(BossSecondSpellEffects.class);
        MinecraftForge.EVENT_BUS.register(BossExpandedSpellEffects.class);
    }

    public static BossSignatureSpell spell(BossSpellRules.Kind kind) {
        return (BossSignatureSpell)((Object)SPELLS.get((Object)kind).get());
    }

    public static ItemStack scroll(BossSpellRules.Kind kind, int level) {
        BossSignatureSpell spell = BossIronSpells.spell(kind);
        ItemStack scroll = new ItemStack((ItemLike)ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer((AbstractSpell)spell, (int)spell.getMaxLevel(), (ItemStack)scroll);
        return scroll;
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != FTCreativeTabs.MAIN.get()) {
            return;
        }
        for (BossSpellRules.Kind kind : BossSpellRules.Kind.values()) {
            if (!BossIronSpells.spell(kind).isEnabled()) continue;
            event.m_246342_(BossIronSpells.scroll(kind, BossIronSpells.spell(kind).getMaxLevel()));
        }
    }

    static {
        for (BossSpellRules.Kind kind : BossSpellRules.Kind.values()) {
            SPELLS.put(kind, (RegistryObject<AbstractSpell>)REGISTER.register(kind.spell, () -> new BossSignatureSpell(kind)));
        }
    }
}

