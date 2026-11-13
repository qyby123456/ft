package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.DrinkAnimations;
import com.farmingtales.drinkanim.DrinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import java.util.HashMap;
import java.util.Map;

public final class DrinkAudio {
    private static final Map<String,SoundInstance> PLAYING=new HashMap<>();
    public static void playInspection(String name, float volume, float pitch) {
        play(name, volume * 1.70f, pitch);
    }
    public static void play(String name, float volume, float pitch) {
        if(!DrinkConfig.SOUNDS.get()) return;
        var manager=Minecraft.getInstance().getSoundManager();
        var sound=new SimpleSoundInstance(ResourceLocation.fromNamespaceAndPath(DrinkAnimations.ID,name),SoundSource.PLAYERS,
            (float)(balancedVolume(name,volume)*DrinkConfig.VOLUME.get()),pitch,SoundInstance.createUnseededRandom(),false,0,
            SoundInstance.Attenuation.NONE,0,0,0,true);
        var previous=PLAYING.put(name,sound);
        if(previous!=null)manager.stop(previous);
        manager.play(sound);
    }
    private static float balancedVolume(String event,float authored) {
        // Physical samples share the drinking sample's active loudness. Preserve light
        // and firm beats within each gesture; cloth keeps its existing quiet mix.
        return switch(event) {
            case "cork" -> .44f;
            case "can_open" -> .43f;
            case "cork_pull", "cap_twist" -> .32f;
            case "cap_release" -> .34f+.04f*range(authored,.24f,.30f);
            case "inspect_liquid" -> .30f+.07f*range(authored,.15f*1.7f,.30f*1.7f);
            case "inspect_settle" -> .32f+.09f*range(authored,.09f*1.7f,.17f*1.7f);
            case "slosh" -> .23f+.09f*range(authored,.10f,.35f);
            default -> authored;
        };
    }
    private static float range(float value,float low,float high) {
        return Math.max(0,Math.min(1,(value-low)/(high-low)));
    }
    public static void stopAll() {
        var manager=Minecraft.getInstance().getSoundManager();
        for(var sound:PLAYING.values())manager.stop(sound);
        PLAYING.clear();
    }
}
