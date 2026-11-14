/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class FTSounds {
    public static final boolean MUSIC_ENABLED = true;
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.SOUND_EVENTS, (String)"ftboss");
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_SAYA = SOUNDS.register("boss_music_saya", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_saya")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_GINGERBREAD = SOUNDS.register("boss_music_gingerbread", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_gingerbread")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_BUTCHER = SOUNDS.register("boss_music_butcher", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_butcher")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_TAVERN = SOUNDS.register("boss_music_tavern", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_tavern")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_RAT = SOUNDS.register("boss_music_rat", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_rat")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_DEFECT = SOUNDS.register("boss_music_defect_robot", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_defect_robot")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST_P2 = SOUNDS.register("boss_music_compost_p2", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_compost_p2")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST = SOUNDS.register("boss_music_compost", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_compost")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_EPIC = SOUNDS.register("boss_music_manor_epic", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_manor_epic")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_OVERTURE = SOUNDS.register("boss_music_overture", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_overture")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MASQUERADE = SOUNDS.register("boss_music_masquerade", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_masquerade")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_FINALE = SOUNDS.register("boss_music_finale", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_finale")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_CURTAIN = SOUNDS.register("boss_music_curtain", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_curtain")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_APPLE = SOUNDS.register("boss_music_apple", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_apple")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_APPLE_P2 = SOUNDS.register("boss_music_apple_p2", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_apple_p2")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_CUE = SOUNDS.register("boss_music_manor_cue", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_manor_cue")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_BATTLE = SOUNDS.register("boss_music_manor_battle", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_manor_battle")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_GUIGNOL = SOUNDS.register("boss_music_manor_guignol", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_manor_guignol")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_DUAL = SOUNDS.register("boss_music_manor_dual", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_manor_dual")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_DUAL_BATTLE = SOUNDS.register("boss_music_manor_dual_battle", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_manor_dual_battle")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_CUE = SOUNDS.register("music_disc_manor_cue", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_manor_cue")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_BATTLE = SOUNDS.register("music_disc_manor_battle", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_manor_battle")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_DUAL = SOUNDS.register("music_disc_manor_dual", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_manor_dual")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_GUIGNOL = SOUNDS.register("music_disc_manor_guignol", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_manor_guignol")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_BENGER = SOUNDS.register("boss_music_benger", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_benger")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_BENGER = SOUNDS.register("music_disc_benger", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_benger")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_APPLE = SOUNDS.register("music_disc_apple", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_apple")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_APPLE_P2 = SOUNDS.register("music_disc_apple_p2", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_apple_p2")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_GINGERBREAD = SOUNDS.register("music_disc_gingerbread", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_gingerbread")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_BUTCHER = SOUNDS.register("music_disc_butcher", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_butcher")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_SAYA = SOUNDS.register("music_disc_saya", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_saya")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_TAVERN = SOUNDS.register("music_disc_tavern", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_tavern")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_RAT = SOUNDS.register("music_disc_rat", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_rat")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_DEFECT = SOUNDS.register("music_disc_defect", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_defect")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_COMPOST = SOUNDS.register("music_disc_compost", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_compost")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_COMPOST_P2 = SOUNDS.register("music_disc_compost_p2", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_compost_p2")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_TIDE_FISH = SOUNDS.register("boss_music_tide_fish", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_tide_fish")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_TIDE_FISH_P2 = SOUNDS.register("boss_music_tide_fish_p2", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "boss_music_tide_fish_p2")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_TIDE_FISH = SOUNDS.register("music_disc_tide_fish", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_tide_fish")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_TIDE_FISH_P2 = SOUNDS.register("music_disc_tide_fish_p2", () -> SoundEvent.m_262824_((ResourceLocation)new ResourceLocation("ftboss", "music_disc_tide_fish_p2")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private FTSounds() {
    }
}

