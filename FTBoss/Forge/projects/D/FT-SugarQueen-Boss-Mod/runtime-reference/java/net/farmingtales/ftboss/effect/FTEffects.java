/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.effect;

import net.farmingtales.ftboss.effect.ArmorShredEffect;
import net.farmingtales.ftboss.effect.BleedEffect;
import net.farmingtales.ftboss.effect.BloodRageEffect;
import net.farmingtales.ftboss.effect.BloodRageFakeEffect;
import net.farmingtales.ftboss.effect.BloomEffect;
import net.farmingtales.ftboss.effect.BlueFireEffect;
import net.farmingtales.ftboss.effect.HealBanEffect;
import net.farmingtales.ftboss.effect.InfectionEffect;
import net.farmingtales.ftboss.effect.LethalTempoEffect;
import net.farmingtales.ftboss.effect.PoopConfusionEffect;
import net.farmingtales.ftboss.effect.QingyiChargeEffect;
import net.farmingtales.ftboss.effect.ShadowBlindEffect;
import net.farmingtales.ftboss.effect.ShadowErosionEffect;
import net.farmingtales.ftboss.effect.TremorEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class FTEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.MOB_EFFECTS, (String)"ftboss");
    public static final RegistryObject<MobEffect> INFECTION = EFFECTS.register("infection", InfectionEffect::new);
    public static final RegistryObject<MobEffect> POOP_CONFUSION = EFFECTS.register("poop_confusion", PoopConfusionEffect::new);
    public static final RegistryObject<MobEffect> SHADOW_EROSION = EFFECTS.register("shadow_erosion", ShadowErosionEffect::new);
    public static final RegistryObject<MobEffect> HEAL_BAN = EFFECTS.register("heal_ban", HealBanEffect::new);
    public static final RegistryObject<MobEffect> BLOOD_RAGE = EFFECTS.register("blood_rage", BloodRageEffect::new);
    public static final RegistryObject<MobEffect> BLUE_FIRE = EFFECTS.register("blue_fire", BlueFireEffect::new);
    public static final RegistryObject<MobEffect> QINGYI_CHARGE = EFFECTS.register("qingyi_charge", QingyiChargeEffect::new);
    public static final RegistryObject<MobEffect> BLEED = EFFECTS.register("bleed", BleedEffect::new);
    public static final RegistryObject<MobEffect> SHADOW_BLIND = EFFECTS.register("shadow_blind", ShadowBlindEffect::new);
    public static final RegistryObject<MobEffect> TREMOR = EFFECTS.register("tremor", TremorEffect::new);
    public static final RegistryObject<MobEffect> BLOOD_RAGE_FAKE = EFFECTS.register("blood_rage_fake", BloodRageFakeEffect::new);
    public static final RegistryObject<MobEffect> ARMOR_SHRED = EFFECTS.register("armor_shred", ArmorShredEffect::new);
    public static final RegistryObject<MobEffect> LETHAL_TEMPO = EFFECTS.register("lethal_tempo", LethalTempoEffect::new);
    public static final RegistryObject<MobEffect> BLOOM = EFFECTS.register("bloom", BloomEffect::new);

    private FTEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}

