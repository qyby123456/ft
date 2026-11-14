package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.sound.BossMusicPacket;
import net.farmingtales.ftboss.util.BossMusicPlayback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class BossMusicClient {
    private static final BossMusicPlayback<SoundInstance> PLAYER = new BossMusicPlayback<>(new BossMusicPlayback.Backend<>() {
        public SoundInstance create(String track) {
            var event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(track));
            return event == null ? null : SimpleSoundInstance.forMusic(event);
        }

        public void play(SoundInstance sound) {
            Minecraft.getInstance().getMusicManager().stopPlaying();
            Minecraft.getInstance().getSoundManager().play(sound);
            FTBossMod.LOGGER.info("[FTBoss][Music] single-channel start {}", sound.getLocation());
        }

        public void stop(SoundInstance sound) {
            Minecraft.getInstance().getSoundManager().stop(sound);
            FTBossMod.LOGGER.info("[FTBoss][Music] single-channel stop {}", sound.getLocation());
        }

        public boolean active(SoundInstance sound) { return Minecraft.getInstance().getSoundManager().isActive(sound); }
    });
    private static BossMusicPacket pending;
    private static long receivedRevision = -1;

    private BossMusicClient() {}

    public static void receive(BossMusicPacket packet) {
        if (packet.revision() <= receivedRevision) return;
        receivedRevision = packet.revision();
        pending = packet;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || PLAYER.owns(sound)) return;
        ResourceLocation location = sound.getLocation();
        if (location != null && location.getNamespace().equals(FTBossMod.MOD_ID)
                && location.getPath().startsWith("boss_music_")
                || PLAYER.busy() && sound.getSource() == SoundSource.MUSIC) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onSourceReady(SoundSourceEvent event) {
        ResourceLocation location = event.getSound().getLocation();
        if (location.getNamespace().equals(FTBossMod.MOD_ID) && location.getPath().startsWith("boss_music_")
                && !PLAYER.owns(event.getSound())) event.getChannel().stop();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) { PLAYER.suspend(); return; }
        if (pending != null && minecraft.level.dimension().location().equals(pending.dimension())) {
            PLAYER.accept(pending.revision(), pending.encounter().toString(), pending.tracks().stream().map(Object::toString).toList());
            pending = null;
        }
        if (minecraft.isPaused()) return;
        if (PLAYER.busy()) minecraft.getMusicManager().stopPlaying();
        PLAYER.tick();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) PLAYER.suspend();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        PLAYER.clear();
        pending = null;
        receivedRevision = -1;
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ReloadEvents {
        private ReloadEvents() {}

        @SubscribeEvent
        public static void soundReload(SoundEngineLoadEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) minecraft.execute(PLAYER::suspend);
        }
    }
}
