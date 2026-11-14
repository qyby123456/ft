package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.compat.irons.IronNativeSpellAdapter;
import net.farmingtales.ftboss.client.compat.irons.IronNativeSpellTextures;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class IronNativeSpellBridge {
    private IronNativeSpellBridge() {}

    private static boolean available() { return ModList.get().isLoaded("irons_spellbooks"); }

    public static boolean accept(SpellVisualPacket packet) {
        return available() && IronNativeSpellAdapter.accept(packet);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && available()) IronNativeSpellAdapter.tick();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && available()) IronNativeSpellAdapter.render(event);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide() && available()) IronNativeSpellAdapter.clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (available()) IronNativeSpellAdapter.clear();
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Reload {
        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) resources -> {
                if (available()) IronNativeSpellTextures.clear();
            });
        }
    }
}
