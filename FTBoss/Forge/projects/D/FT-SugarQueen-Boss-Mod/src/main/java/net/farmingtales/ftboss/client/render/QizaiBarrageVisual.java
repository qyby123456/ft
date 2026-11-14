package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.network.QizaiBarrageNetwork.State;
import net.farmingtales.ftboss.util.QizaiBarrageRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class QizaiBarrageVisual {
    private static final Map<UUID, State> ACTIVE = new LinkedHashMap<>();

    private QizaiBarrageVisual() {}

    public static void accept(State state) {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(state.dimension())) return;
        if (state.cue().isEmpty()) { ACTIVE.remove(state.owner()); return; }
        if (!ACTIVE.containsKey(state.owner()) && ACTIVE.size() >= QizaiBarrageRules.MAX_CASTS)
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        ACTIVE.put(state.owner(), state);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var level = Minecraft.getInstance().level;
        if (level == null) { ACTIVE.clear(); return; }
        ACTIVE.values().removeIf(state -> !level.dimension().location().equals(state.dimension())
                || level.getGameTime() - state.cue().getLong("start") >= state.cue().getInt("duration"));
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ACTIVE.clear(); }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel().isClientSide()) ACTIVE.clear(); }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        var minecraft = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || minecraft.level == null || ACTIVE.isEmpty()) return;
        renderPass(event, minecraft, QizaiContrast.MODEL, true);
        renderPass(event, minecraft, QizaiContrast.TYPE, false);
    }

    private static void renderPass(RenderLevelStageEvent event, Minecraft minecraft, RenderType type, boolean models) {
        var buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(type);
        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        boolean reduced = minecraft.options.particles().get() != ParticleStatus.ALL;
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            for (State state : ACTIVE.values()) {
                double age = minecraft.level.getGameTime() - state.cue().getLong("start") + event.getPartialTick();
                if (age >= 0 && age < state.cue().getInt("duration"))
                    QizaiLaserBarrageVisual.render(state.cue(), age, reduced, pose, vertices, models);
            }
        } finally { pose.popPose(); buffers.endBatch(type); }
    }
}
